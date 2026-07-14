package com.ernoxin.codalapi.domain;

import java.util.List;

public record CodalNoticeSnapshot(
        long refreshedAtEpochMs,
        List<CodalModels.NoticeItem> notices
) {
    public CodalNoticeSnapshot {
        notices = List.copyOf(notices);
    }
}
