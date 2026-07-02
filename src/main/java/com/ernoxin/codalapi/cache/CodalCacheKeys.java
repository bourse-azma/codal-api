package com.ernoxin.codalapi.cache;

import com.ernoxin.codalapi.domain.CodalModels;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CodalCacheKeys {

    private static final ObjectMapper KEY_MAPPER = new ObjectMapper();

    private CodalCacheKeys() {
    }

    public static String notices(CodalModels.NoticeSearchQuery query) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(KEY_MAPPER.writeValueAsString(query).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to build notice cache key", ex);
        }
    }

    public static String financialYears(String symbol) {
        return symbol.trim().toUpperCase();
    }

    public static String financialStatements(CodalModels.FinancialStatementQuery query) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(KEY_MAPPER.writeValueAsString(query).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to build financial statement cache key", ex);
        }
    }
}
