package com.ernoxin.codalapi.common.util;

import com.ernoxin.codalapi.common.exception.UpstreamApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CodalMapperSupport {

    private static final String CODAL_BASE_URL = "https://codal.ir/";

    private static final Pattern DATASOURCE_PATTERN =
            Pattern.compile("datasource\\s*=\\s*(\\{)", Pattern.CASE_INSENSITIVE);

    private static final Pattern SHEET_SELECT_PATTERN =
            Pattern.compile("<select[^>]*id=\"ddlTable\"[^>]*>(.*?)</select>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern SHEET_OPTION_PATTERN =
            Pattern.compile("<option[^>]*value=\"(-?\\d+)\"[^>]*>\\s*([^<\\n\\r]+)",
                    Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public JsonNode extractDatasource(String html) {
        if (html == null || html.isBlank()) {
            throw new UpstreamApiException("Empty CODAL letter page", HttpStatus.BAD_GATEWAY);
        }

        Matcher matcher = DATASOURCE_PATTERN.matcher(html);
        if (!matcher.find()) {
            throw new UpstreamApiException(
                    "Could not locate financial statement data in CODAL letter page",
                    HttpStatus.BAD_GATEWAY
            );
        }

        String json = extractBalancedObject(html, matcher.start(1));
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new UpstreamApiException(
                    "Failed to parse CODAL datasource payload: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    /**
     * Parses the {@code <select id="ddlTable">} dropdown so callers know every financial statement
     * sheet available for a letter (sheetId -&gt; Persian title), preserving the page ordering.
     */
    public Map<Integer, String> extractAvailableSheets(String html) {
        Map<Integer, String> sheets = new LinkedHashMap<>();
        if (html == null || html.isBlank()) {
            return sheets;
        }

        Matcher selectMatcher = SHEET_SELECT_PATTERN.matcher(html);
        if (!selectMatcher.find()) {
            return sheets;
        }

        Matcher optionMatcher = SHEET_OPTION_PATTERN.matcher(selectMatcher.group(1));
        while (optionMatcher.find()) {
            try {
                int sheetId = Integer.parseInt(optionMatcher.group(1).trim());
                String title = optionMatcher.group(2).trim();
                sheets.putIfAbsent(sheetId, title);
            } catch (NumberFormatException ignored) {
                // Skip malformed options.
            }
        }

        return sheets;
    }

    private String extractBalancedObject(String source, int start) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = start; i < source.length(); i++) {
            char c = source.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(start, i + 1);
                }
            }
        }

        throw new UpstreamApiException(
                "Malformed financial statement data in CODAL letter page",
                HttpStatus.BAD_GATEWAY
        );
    }
}
