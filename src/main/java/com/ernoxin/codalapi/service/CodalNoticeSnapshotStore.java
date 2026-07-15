package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.cache.CodalCacheKeys;
import com.ernoxin.codalapi.config.CacheProperties;
import com.ernoxin.codalapi.config.CodalNoticeSchedulerProperties;
import com.ernoxin.codalapi.domain.CodalModels;
import com.ernoxin.codalapi.domain.CodalNoticeSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CodalNoticeSnapshotStore {

    static final String SNAPSHOT_KEY = "latest";

    private final CacheManager cacheManager;
    private final CacheProperties cacheProperties;
    private final CodalNoticeSchedulerProperties schedulerProperties;

    public void publish(CodalNoticeSnapshot snapshot) {
        Cache cache = snapshotCache();
        if (cache == null) {
            throw new IllegalStateException("CODAL notice snapshot cache is not configured");
        }
        // One cache put replaces the complete snapshot; readers never observe a partially refreshed list.
        cache.put(SNAPSHOT_KEY, snapshot);
        evictOldBoardPages();
    }

    /**
     * Rewrites the current snapshot using the active cache policy. This also migrates a
     * snapshot created with the old expiring TTL to the new persistent policy, without
     * requiring Codal to be reachable during deployment.
     */
    public boolean preserveCurrent() {
        Cache cache = snapshotCache();
        CodalNoticeSnapshot snapshot = cache == null ? null : cache.get(SNAPSHOT_KEY, CodalNoticeSnapshot.class);
        if (snapshot == null) {
            return false;
        }
        cache.put(SNAPSHOT_KEY, snapshot);
        return true;
    }

    public Optional<CodalModels.NoticeSearchResult> find(CodalModels.NoticeSearchQuery query) {
        if (!isSnapshotQuery(query)) {
            return Optional.empty();
        }

        Cache cache = snapshotCache();
        CodalNoticeSnapshot snapshot = cache == null ? null : cache.get(SNAPSHOT_KEY, CodalNoticeSnapshot.class);
        if (snapshot == null) {
            return Optional.empty();
        }

        int pageSize = schedulerProperties.upstreamPageSize();
        long offset = (long) (query.page() - 1) * pageSize;
        int fromIndex = (int) Math.min(snapshot.notices().size(), offset);
        // The snapshot only accelerates its cached prefix. Once a request moves past that
        // prefix, let CodalService fetch the page from upstream instead of returning an
        // empty cached page and making the client believe pagination has ended.
        if (fromIndex >= snapshot.notices().size() && snapshot.totalCount() > snapshot.notices().size()) {
            return Optional.empty();
        }
        int toIndex = Math.min(snapshot.notices().size(), fromIndex + pageSize);
        return Optional.of(new CodalModels.NoticeSearchResult(
                snapshot.totalCount(),
                query.page(),
                false,
                snapshot.notices().subList(fromIndex, toIndex)
        ));
    }

    Cache snapshotCache() {
        return cacheManager.getCache(cacheProperties.names().noticeSnapshot());
    }

    private boolean isSnapshotQuery(CodalModels.NoticeSearchQuery query) {
        return query.includeAudited()
                && query.auditorRef() == -1
                && query.categoryCode() == -1
                && query.includeChildCategories()
                && query.companyState() == -1
                && query.companyType() == -1
                && query.includeConsolidated()
                && !query.isNotAuditedFilter()
                && query.length() == schedulerProperties.length()
                && query.letterType() == -1
                && query.includeMainCategories()
                && query.includeNotAudited()
                && query.includeNotConsolidated()
                && !query.publisher()
                && query.reportingType() == -1
                && (query.symbol() == null || query.symbol().isBlank())
                && query.tracingNumber() == -1
                && !query.searchMode();
    }

    private void evictOldBoardPages() {
        Cache noticesCache = cacheManager.getCache(cacheProperties.names().notices());
        if (noticesCache == null) {
            return;
        }
        try {
            for (int page = 1; page <= schedulerProperties.pageCount(); page++) {
                noticesCache.evict(CodalCacheKeys.notices(snapshotQuery(page)));
            }
        } catch (RuntimeException ex) {
            // The snapshot is already safely published. Old page entries will naturally expire by TTL.
            log.warn("Published CODAL snapshot but could not evict old board pages: {}", ex.getMessage());
        }
    }

    private CodalModels.NoticeSearchQuery snapshotQuery(int page) {
        return new CodalModels.NoticeSearchQuery(
                true, -1, -1, true, -1, -1, true, false,
                schedulerProperties.length(), -1, true, true, true, page,
                false, -1, "", -1, false
        );
    }
}
