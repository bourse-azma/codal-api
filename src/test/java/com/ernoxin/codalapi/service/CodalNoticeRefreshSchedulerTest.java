package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.config.CodalNoticeSchedulerProperties;
import com.ernoxin.codalapi.domain.CodalModels;
import com.ernoxin.codalapi.domain.CodalNoticeSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodalNoticeRefreshSchedulerTest {

    @Test
    void duplicateAcrossPageBoundaryUsesOverflowPageInsteadOfDiscardingSnapshot() {
        CodalFetchService fetchService = mock(CodalFetchService.class);
        when(fetchService.getNotices(any())).thenAnswer(invocation -> {
            CodalModels.NoticeSearchQuery query = invocation.getArgument(0);
            List<CodalModels.NoticeItem> notices = switch (query.page()) {
                case 1 -> List.of(notice(1), notice(2));
                case 2 -> List.of(notice(2), notice(3));
                default -> List.of(notice(4));
            };
            return new CodalModels.NoticeSearchResult(100, query.page(), false, notices);
        });

        CodalNoticeSchedulerProperties properties = new CodalNoticeSchedulerProperties(
                true, 900_000, 0, 4, 12, 2
        );
        CodalNoticeRefreshScheduler scheduler = new CodalNoticeRefreshScheduler(
                fetchService,
                mock(CodalNoticeSnapshotStore.class),
                properties
        );

        CodalNoticeSnapshot snapshot = scheduler.fetchSnapshot();

        assertEquals(4, snapshot.notices().size());
        assertEquals(List.of(1L, 2L, 3L, 4L), snapshot.notices().stream()
                .map(CodalModels.NoticeItem::tracingNumber)
                .toList());
    }

    private CodalModels.NoticeItem notice(long tracingNumber) {
        return new CodalModels.NoticeItem(
                tracingNumber, "SYM", "Company", 0,
                new CodalModels.SupervisionState(0, "", List.of()),
                "Title", "Code", "Sent", "Published", true, false,
                "/report", "https://codal.ir/report", false, false, false, false,
                null, null, null, null, null, null, null, null
        );
    }
}
