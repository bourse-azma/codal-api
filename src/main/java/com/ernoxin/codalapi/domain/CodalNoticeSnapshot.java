package com.ernoxin.codalapi.domain;

import java.util.List;

public record CodalNoticeSnapshot(
        long refreshedAtEpochMs,
        int totalCount,
        List<CodalModels.NoticeItem> notices
) {
    public CodalNoticeSnapshot {
        totalCount = Math.max(totalCount, notices.size());
        notices = List.copyOf(notices);
    }
}
