package com.ernoxin.codalapi.mapper;

import com.ernoxin.codalapi.domain.CodalFinancialModels;
import com.ernoxin.codalapi.domain.CodalModels;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CodalMapper {

    private final CodalSearchMapper searchMapper;
    private final CodalFinancialStatementMapper financialStatementMapper;

    public CodalModels.NoticeSearchResult toNoticeSearchResult(JsonNode root) {
        return searchMapper.toNoticeSearchResult(root);
    }

    public CodalModels.CompaniesResult toCompaniesResult(JsonNode root) {
        return searchMapper.toCompaniesResult(root);
    }

    public CodalModels.IndustryGroupsResult toIndustryGroupsResult(JsonNode root) {
        return searchMapper.toIndustryGroupsResult(root);
    }

    public CodalModels.CategoriesResult toCategoriesResult(JsonNode root) {
        return searchMapper.toCategoriesResult(root);
    }

    public CodalModels.FinancialYearsResult toFinancialYearsResult(String symbol, JsonNode root) {
        return searchMapper.toFinancialYearsResult(symbol, root);
    }

    public CodalModels.AuditorsResult toAuditorsResult(JsonNode root) {
        return searchMapper.toAuditorsResult(root);
    }

    public CodalFinancialModels.FinancialStatementResult toFinancialStatementResult(String letterSerial, String html) {
        return financialStatementMapper.toFinancialStatementResult(letterSerial, html);
    }

    public List<CodalFinancialModels.AvailableSheetItem> extractAvailableSheets(String html) {
        return financialStatementMapper.extractAvailableSheets(html);
    }

    public CodalFinancialModels.FinancialStatementResult combineFinancialStatements(
            String letterSerial,
            List<CodalFinancialModels.AvailableSheetItem> availableSheets,
            List<CodalFinancialModels.FinancialStatementResult> parsed
    ) {
        return financialStatementMapper.combineFinancialStatements(letterSerial, availableSheets, parsed);
    }
}
