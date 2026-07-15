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
    private final AtomicBoolean codalUnavailable = new AtomicBoolean();

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
            refreshSnapshot();
        } finally {
            refreshInProgress.set(false);
        }
    }

    private void refreshSnapshot() {
        CodalNoticeSnapshot snapshot;
        try {
            snapshot = fetchSnapshot();
        } catch (RuntimeException ex) {
            codalUnavailable.set(true);
            boolean retained = preservePreviousSnapshot();
            log.warn("CODAL notice snapshot refresh failed; previous snapshot retained={}: {}",
                    retained, ex.getMessage());
            return;
        }

        try {
            snapshotStore.publish(snapshot);
        } catch (RuntimeException cacheException) {
            // A failed Redis write does not imply that Codal is unavailable. Cache put is atomic,
            // so readers either keep seeing the old snapshot or receive the complete new one.
            log.error("CODAL was fetched successfully but the notice snapshot could not be published: {}",
                    cacheException.getMessage());
            return;
        }

        boolean recovered = codalUnavailable.getAndSet(false);
        log.info("Published CODAL notice snapshot with {} cached notices out of {} total",
                snapshot.notices().size(), snapshot.totalCount());
        if (recovered) {
            log.info("CODAL is available again; the retained notice snapshot was refreshed successfully");
        }
    }

    private boolean preservePreviousSnapshot() {
        try {
            return snapshotStore.preserveCurrent();
        } catch (RuntimeException cacheException) {
            log.error("Could not preserve the previous CODAL notice snapshot: {}", cacheException.getMessage());
            return false;
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
            if (upstreamTotal == 0) {
                throw new IllegalStateException("CODAL returned an invalid zero notice total");
            }
            for (CodalModels.NoticeItem notice : result.notices()) {
                uniqueNotices.putIfAbsent(noticeKey(notice), notice);
                if (uniqueNotices.size() >= properties.targetCount()) {
                    break;
                }
            }

            if (result.notices().isEmpty()
                    && uniqueNotices.size() < Math.min(properties.targetCount(), upstreamTotal)) {
                throw new IllegalStateException("CODAL returned an incomplete empty notice page: " + page);
            }

            if (uniqueNotices.size() >= properties.targetCount()
                    || uniqueNotices.size() >= upstreamTotal) {
                break;
            }
        }

        List<CodalModels.NoticeItem> notices = new ArrayList<>(uniqueNotices.values());
        if (notices.size() > properties.targetCount()) {
            notices = notices.subList(0, properties.targetCount());
        }
        int requiredCount = Math.min(properties.targetCount(), upstreamTotal);
        if (notices.size() < requiredCount) {
            throw new IllegalStateException(
                    "CODAL returned an incomplete notice snapshot: expected "
                            + requiredCount + ", received " + notices.size()
            );
        }
        return new CodalNoticeSnapshot(System.currentTimeMillis(), upstreamTotal, notices);
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
