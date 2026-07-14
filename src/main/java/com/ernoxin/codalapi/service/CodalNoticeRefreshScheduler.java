package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.config.CodalNoticeSchedulerProperties;
import com.ernoxin.codalapi.domain.CodalModels;
import com.ernoxin.codalapi.domain.CodalNoticeSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = {"cache.enabled", "scheduler.notices.enabled"}, havingValue = "true", matchIfMissing = true)
public class CodalNoticeRefreshScheduler {

    private final CodalFetchService fetchService;
    private final CodalNoticeSnapshotStore snapshotStore;
    private final CodalNoticeSchedulerProperties properties;
    private final AtomicBoolean refreshInProgress = new AtomicBoolean();

    @Scheduled(
            fixedDelayString = "${scheduler.notices.refresh-ms}",
            initialDelayString = "${scheduler.notices.initial-delay-ms}"
    )
    public void refresh() {
        if (!refreshInProgress.compareAndSet(false, true)) {
            log.info("Skipping CODAL notice refresh because the previous refresh is still running");
            return;
        }

        try {
            CodalNoticeSnapshot snapshot = fetchSnapshot();
            snapshotStore.publish(snapshot);
            log.info("Published CODAL notice snapshot with {} notices", snapshot.notices().size());
        } catch (RuntimeException ex) {
            // Publishing happens only after every required page succeeds, so the last good snapshot survives.
            log.warn("CODAL notice snapshot refresh failed; keeping the previous snapshot: {}", ex.getMessage());
        } finally {
            refreshInProgress.set(false);
        }
    }

    CodalNoticeSnapshot fetchSnapshot() {
        Map<String, CodalModels.NoticeItem> uniqueNotices = new LinkedHashMap<>();
        int upstreamTotal = Integer.MAX_VALUE;

        for (int page = 1; page <= properties.pageCount(); page++) {
            CodalModels.NoticeSearchResult result = fetchService.getNotices(snapshotQuery(page));
            if (result.attackerDetected()) {
                throw new IllegalStateException("CODAL marked the request as an attacker");
            }

            upstreamTotal = Math.max(0, result.totalCount());
            for (CodalModels.NoticeItem notice : result.notices()) {
                uniqueNotices.putIfAbsent(noticeKey(notice), notice);
                if (uniqueNotices.size() >= properties.targetCount()) {
                    break;
                }
            }

            if (uniqueNotices.size() >= properties.targetCount()
                    || uniqueNotices.size() >= upstreamTotal
                    || result.notices().isEmpty()) {
                break;
            }
        }

        List<CodalModels.NoticeItem> notices = new ArrayList<>(uniqueNotices.values());
        if (notices.size() > properties.targetCount()) {
            notices = notices.subList(0, properties.targetCount());
        }
        if (notices.isEmpty()) {
            throw new IllegalStateException("CODAL returned an empty notice snapshot");
        }
        return new CodalNoticeSnapshot(System.currentTimeMillis(), notices);
    }

    private CodalModels.NoticeSearchQuery snapshotQuery(int page) {
        return new CodalModels.NoticeSearchQuery(
                true, -1, -1, true, -1, -1, true, false,
                properties.length(), -1, true, true, true, page,
                false, -1, "", -1, false
        );
    }

    private String noticeKey(CodalModels.NoticeItem notice) {
        return notice.tracingNumber() + "|" + notice.symbol() + "|" + notice.publishDateTime();
    }
}
