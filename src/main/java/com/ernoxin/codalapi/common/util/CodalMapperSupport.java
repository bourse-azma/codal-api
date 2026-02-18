package com.ernoxin.codalapi.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CodalMapperSupport {

    private static final String CODAL_BASE_URL = "https://codal.ir/";

    public String text(JsonNode node, String field) {
        return node.path(field).asText("").trim();
    }

    public int integer(JsonNode node, String field) {
        return node.path(field).asInt(0);
    }

    public long lng(JsonNode node, String field) {
        return node.path(field).asLong(0L);
    }

    public boolean bool(JsonNode node, String field) {
        return node.path(field).asBoolean(false);
    }

    public List<JsonNode> list(JsonNode arrayNode) {
        List<JsonNode> items = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(items::add);
        }
        return items;
    }

    public List<String> textList(JsonNode node) {
        List<String> items = new ArrayList<>();
        if (node == null || node.isNull() || node.isMissingNode()) {
            return items;
        }

        if (node.isArray()) {
            node.forEach(item -> {
                String value = item.asText("").trim();
                if (!value.isEmpty()) {
                    items.add(value);
                }
            });
            return items;
        }

        String single = node.asText("").trim();
        if (!single.isEmpty()) {
            items.add(single);
        }

        return items;
    }

    public String absoluteCodalUrl(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return "";
        }

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }

        String relativePath = normalized.startsWith("/") ? normalized.substring(1) : normalized;
        return CODAL_BASE_URL + relativePath;
    }
}
