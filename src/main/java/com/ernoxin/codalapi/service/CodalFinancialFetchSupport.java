package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.client.CodalWebClient;
import com.ernoxin.codalapi.domain.CodalFinancialModels;
import com.ernoxin.codalapi.domain.CodalModels;
import com.ernoxin.codalapi.mapper.CodalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
class CodalFinancialFetchSupport {

    private final CodalWebClient webClient;
    private final CodalMapper mapper;

    CodalFinancialModels.FinancialStatementResult fetchAllFinancialStatements(CodalFinancialModels.FinancialStatementQuery query) {
        // The default landing sheet (no sheetId) is not guaranteed to be a tabular statement, so it is
        // only used to discover the list of available sheets from the dropdown.
        String baseHtml = fetchLetterHtml(query, null);
        List<CodalFinancialModels.AvailableSheetItem> availableSheets = mapper.extractAvailableSheets(baseHtml);

        List<Integer> sheetIds = availableSheets.stream()
                .map(CodalFinancialModels.AvailableSheetItem::sheetId)
                .distinct()
                .toList();

        List<CompletableFuture<CodalFinancialModels.FinancialStatementResult>> futures = sheetIds.stream()
                .map(sheetId -> CompletableFuture.supplyAsync(() -> tryParseSheet(query, sheetId)))
                .toList();

        List<CodalFinancialModels.FinancialStatementResult> parsed = futures.stream()
                .map(CompletableFuture::join)
                .filter(java.util.Objects::nonNull)
                .toList();

        return mapper.combineFinancialStatements(query.letterSerial(), availableSheets, parsed);
    }

    /**
     * Some sheets in the dropdown (auditor opinion, board members, subsidiaries, ...) are not tabular
     * financial statements and do not embed a datasource payload. Those are skipped rather than failing
     * the whole "all statements" request.
     */
    CodalFinancialModels.FinancialStatementResult tryParseSheet(CodalFinancialModels.FinancialStatementQuery query, Integer sheetId) {
        try {
            return mapper.toFinancialStatementResult(query.letterSerial(), fetchLetterHtml(query, sheetId));
        } catch (com.ernoxin.codalapi.common.exception.UpstreamApiException ex) {
            return null;
        }
    }

    String fetchLetterHtml(CodalFinancialModels.FinancialStatementQuery query, Integer sheetId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("LetterSerial", query.letterSerial());
        params.put("rt", query.reportType());
        params.put("let", query.letterType());
        params.put("ct", query.companyType());
        params.put("ft", query.fileType());
        if (sheetId != null) {
            params.put("sheetId", sheetId);
        }

        return webClient.getHtml("/Reports/Decision.aspx", params);
    }

    boolean isFinancialNotice(CodalModels.NoticeItem item) {
        String title = item.title() == null ? "" : item.title();
        return title.contains("صورت") && title.contains("مالی");
    }

    Map<String, Object> buildFinancialNoticeQueryParams(
            String symbol,
            int length,
            int pageNumber,
            CodalModels.CompanyItem company
    ) {
        Map<String, Object> queryParams = new LinkedHashMap<>();
        queryParams.put("Audited", true);
        queryParams.put("AuditorRef", -1);
        queryParams.put("Category", -1);
        queryParams.put("Childs", false);
        queryParams.put("CompanyState", company != null ? company.companyState() : 0);
        queryParams.put("CompanyType", -1);
        queryParams.put("Consolidatable", true);
        queryParams.put("IsNotAudited", false);
        queryParams.put("Length", length);
        queryParams.put("LetterType", -1);
        queryParams.put("Mains", true);
        queryParams.put("NotAudited", false);
        queryParams.put("NotConsolidatable", true);
        queryParams.put("PageNumber", pageNumber);
        queryParams.put("Publisher", false);
        queryParams.put("ReportingType", company != null ? company.reportingType() : -1);
        queryParams.put("Symbol", symbol);
        queryParams.put("TracingNo", -1);
        queryParams.put("search", true);
        if (company != null) {
            queryParams.put("Name", company.companyName());
            queryParams.put("name", company.companyName());
            queryParams.put("IndustryGroup", company.industryGroupCode());
        }
        return queryParams;
    }
}
