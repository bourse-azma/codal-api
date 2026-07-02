package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.client.CodalClient;
import com.ernoxin.codalapi.client.CodalWebClient;
import com.ernoxin.codalapi.common.util.RequestParamSupport;
import com.ernoxin.codalapi.domain.CodalModels;
import com.ernoxin.codalapi.mapper.CodalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CodalFetchService {

    private static final int UPSTREAM_PAGE_SIZE = 20;
    private final CodalClient client;
    private final CodalWebClient webClient;
    private final CodalMapper mapper;

    public CodalModels.NoticeSearchResult getNotices(CodalModels.NoticeSearchQuery query) {
        return mapper.toNoticeSearchResult(client.get("/search/v2/q", Map.ofEntries(
                Map.entry("Audited", query.includeAudited()),
                Map.entry("AuditorRef", query.auditorRef()),
                Map.entry("Category", query.categoryCode()),
                Map.entry("Childs", query.includeChildCategories()),
                Map.entry("CompanyState", query.companyState()),
                Map.entry("CompanyType", query.companyType()),
                Map.entry("Consolidatable", query.includeConsolidated()),
                Map.entry("IsNotAudited", query.isNotAuditedFilter()),
                Map.entry("Length", query.length()),
                Map.entry("LetterType", query.letterType()),
                Map.entry("Mains", query.includeMainCategories()),
                Map.entry("NotAudited", query.includeNotAudited()),
                Map.entry("NotConsolidatable", query.includeNotConsolidated()),
                Map.entry("PageNumber", query.page()),
                Map.entry("Publisher", query.publisher()),
                Map.entry("ReportingType", query.reportingType()),
                Map.entry("Symbol", query.symbol()),
                Map.entry("TracingNo", query.tracingNumber()),
                Map.entry("search", query.searchMode())
        )));
    }

    public CodalModels.CompaniesResult getCompanies() {
        return mapper.toCompaniesResult(client.get("/search/v1/companies"));
    }

    public CodalModels.IndustryGroupsResult getIndustryGroups() {
        return mapper.toIndustryGroupsResult(client.get("/search/v1/IndustryGroup"));
    }

    public CodalModels.CategoriesResult getCategories() {
        return mapper.toCategoriesResult(client.get("/search/v1/categories"));
    }

    public CodalModels.FinancialYearsResult getFinancialYears(String symbol) {
        return mapper.toFinancialYearsResult(symbol,
                client.get("/search/v1/financialYears", Map.of("Symbol", symbol))
        );
    }

    public CodalModels.AuditorsResult getAuditors() {
        return mapper.toAuditorsResult(client.get("/search/v1/auditors"));
    }

    public CodalModels.FinancialStatementResult getFinancialStatement(CodalModels.FinancialStatementQuery query) {
        if (query.sheetId() != null) {
            String html = fetchLetterHtml(query, query.sheetId());
            return mapper.toFinancialStatementResult(query.letterSerial(), html);
        }

        return fetchAllFinancialStatements(query);
    }

    public CodalModels.FinancialStatementBySymbolResult getFinancialStatementBySymbol(String symbol, Integer sheetId) {
        String resolvedSymbol = RequestParamSupport.require("symbol", symbol);

        CodalModels.NoticeSearchQuery noticeQuery = new CodalModels.NoticeSearchQuery(
                true,
                -1,
                -1,
                false,
                -1,
                -1,
                true,
                false,
                12,
                -1,
                true,
                false,
                true,
                1,
                false,
                -1,
                resolvedSymbol,
                -1,
                true
        );

        CodalModels.NoticeSearchResult notices = getNotices(noticeQuery);
        CodalModels.NoticeItem selected = notices.notices().stream()
                .filter(this::isFinancialNotice)
                .filter(item -> !extractLetterSerial(item).isEmpty())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No audited main-company financial statement found for symbol: " + resolvedSymbol
                ));

        String letterSerial = extractLetterSerial(selected);
        CodalModels.FinancialStatementResult statement = getFinancialStatement(new CodalModels.FinancialStatementQuery(
                letterSerial,
                sheetId,
                0,
                6,
                0,
                -1
        ));

        return new CodalModels.FinancialStatementBySymbolResult(
                resolvedSymbol,
                selected.companyName(),
                selected.title(),
                selected.publishDateTime(),
                selected.tracingNumber(),
                letterSerial,
                statement
        );
    }

    public CodalModels.FinancialNoticeListResult getFinancialNoticesBySymbol(String symbol, int page, int size, int length) {
        String resolvedSymbol = RequestParamSupport.require("symbol", symbol);
        int safePage = Math.max(1, page);
        int safeSize = Math.min(200, Math.max(1, size));
        int safeLength = length == -1 ? -1 : Math.min(12, Math.max(1, length));

        CodalModels.CompanyItem company = getCompanies().companies().stream()
                .filter(item -> resolvedSymbol.equalsIgnoreCase(item.symbol()))
                .findFirst()
                .orElse(null);

        List<CodalModels.FinancialNoticeItem> allFinancialNotices = new java.util.ArrayList<>();
        int upstreamPage = 1;
        int upstreamTotal = 0;
        int upstreamPages = 1;

        while (upstreamPage <= upstreamPages) {
            CodalModels.NoticeSearchResult noticesResult = mapper.toNoticeSearchResult(
                    client.get("/search/v2/q", buildFinancialNoticeQueryParams(
                            resolvedSymbol, safeLength, upstreamPage, company
                    ))
            );

            upstreamTotal = noticesResult.totalCount();
            upstreamPages = Math.max(1, (int) Math.ceil(upstreamTotal / (double) UPSTREAM_PAGE_SIZE));

            noticesResult.notices().stream()
                    .filter(this::isFinancialNotice)
                    .map(item -> new CodalModels.FinancialNoticeItem(
                            item.tracingNumber(),
                            item.symbol(),
                            item.companyName(),
                            item.title(),
                            item.letterCode(),
                            item.sentDateTime(),
                            item.publishDateTime(),
                            extractLetterSerial(item),
                            item.reportUrl(),
                            item.hasExcel(),
                            item.hasPdf(),
                            item.hasAttachment()
                    ))
                    .forEach(allFinancialNotices::add);

            if (noticesResult.notices().isEmpty()) {
                break;
            }
            upstreamPage++;
        }

        int fromIndex = Math.min(allFinancialNotices.size(), (safePage - 1) * safeSize);
        int toIndex = Math.min(allFinancialNotices.size(), fromIndex + safeSize);
        List<CodalModels.FinancialNoticeItem> pageItems = allFinancialNotices.subList(fromIndex, toIndex);

        return new CodalModels.FinancialNoticeListResult(
                resolvedSymbol,
                safePage,
                safeSize,
                safeLength,
                allFinancialNotices.size(),
                pageItems
        );
    }

    public CodalModels.FinancialStatementResult getFinancialStatementByNotice(String letterSerial, Integer sheetId) {
        String resolvedLetterSerial = RequestParamSupport.require("letterSerial", letterSerial);
        return getFinancialStatement(new CodalModels.FinancialStatementQuery(
                resolvedLetterSerial,
                sheetId,
                0,
                6,
                0,
                -1
        ));
    }

    private CodalModels.FinancialStatementResult fetchAllFinancialStatements(CodalModels.FinancialStatementQuery query) {
        // The default landing sheet (no sheetId) is not guaranteed to be a tabular statement, so it is
        // only used to discover the list of available sheets from the dropdown.
        String baseHtml = fetchLetterHtml(query, null);
        List<CodalModels.AvailableSheetItem> availableSheets = mapper.extractAvailableSheets(baseHtml);

        List<Integer> sheetIds = availableSheets.stream()
                .map(CodalModels.AvailableSheetItem::sheetId)
                .distinct()
                .toList();

        List<CompletableFuture<CodalModels.FinancialStatementResult>> futures = sheetIds.stream()
                .map(sheetId -> CompletableFuture.supplyAsync(() -> tryParseSheet(query, sheetId)))
                .toList();

        List<CodalModels.FinancialStatementResult> parsed = futures.stream()
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
    private CodalModels.FinancialStatementResult tryParseSheet(CodalModels.FinancialStatementQuery query, Integer sheetId) {
        try {
            return mapper.toFinancialStatementResult(query.letterSerial(), fetchLetterHtml(query, sheetId));
        } catch (com.ernoxin.codalapi.common.exception.UpstreamApiException ex) {
            return null;
        }
    }

    private String fetchLetterHtml(CodalModels.FinancialStatementQuery query, Integer sheetId) {
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

    private boolean isFinancialNotice(CodalModels.NoticeItem item) {
        String title = item.title() == null ? "" : item.title();
        return title.contains("صورت") && title.contains("مالی");
    }

    private String extractLetterSerial(CodalModels.NoticeItem item) {
        String path = item.reportPath();
        if (path == null || path.isBlank()) {
            return "";
        }
        try {
            String serial = UriComponentsBuilder.fromUriString(path)
                    .build(true)
                    .getQueryParams()
                    .getFirst("LetterSerial");
            if (serial == null || serial.isBlank()) {
                return "";
            }
            return URLDecoder.decode(serial.trim(), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }

    private Map<String, Object> buildFinancialNoticeQueryParams(
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
