package com.ernoxin.codalapi.common.api;

import java.util.List;

public record ErrorResult(
        List<String> details
) {
}
