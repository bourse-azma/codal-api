package com.ernoxin.codalapi.controller;

import com.ernoxin.codalapi.common.api.ApiResponse;
import com.ernoxin.codalapi.common.util.RequestParamSupport;
import com.ernoxin.codalapi.domain.CodalModels;
import com.ernoxin.codalapi.service.CodalService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/codal")
@RequiredArgsConstructor
@Validated
public class CodalController {

    private final CodalService service;

    @GetMapping("/notices")
    public ApiResponse<CodalModels.NoticeSearchResult> getNotices(
            @RequestParam(defaultValue = "true") boolean includeAudited,
            @RequestParam(defaultValue = "-1") int auditorRef,
            @RequestParam(defaultValue = "-1") int categoryCode,
            @RequestParam(defaultValue = "true") boolean includeChildCategories,
            @RequestParam(defaultValue = "-1") int companyState,
            @RequestParam(defaultValue = "-1") int companyType,
            @RequestParam(defaultValue = "true") boolean includeConsolidated,
            @RequestParam(defaultValue = "false") boolean isNotAuditedFilter,
            @RequestParam(defaultValue = "1") int length,
            @RequestParam(defaultValue = "-1") int letterType,
            @RequestParam(defaultValue = "true") boolean includeMainCategories,
            @RequestParam(defaultValue = "true") boolean includeNotAudited,
            @RequestParam(defaultValue = "true") boolean includeNotConsolidated,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "false") boolean publisher,
            @RequestParam(defaultValue = "-1") int reportingType,
            @RequestParam(required = false) String symbol,
            @RequestParam(defaultValue = "-1") int tracingNumber,
            @RequestParam(required = false) Boolean searchMode
    ) {
        String normalizedSymbol = RequestParamSupport.firstNonBlank(symbol);
        boolean resolvedSearchMode = searchMode != null ? searchMode : !normalizedSymbol.isEmpty();
        int safeLength = Math.min(12, Math.max(1, length));
        int safePage = Math.max(1, page);

        CodalModels.NoticeSearchQuery query = new CodalModels.NoticeSearchQuery(
                includeAudited,
                auditorRef,
                categoryCode,
                includeChildCategories,
                companyState,
                companyType,
                includeConsolidated,
                isNotAuditedFilter,
                safeLength,
                letterType,
                includeMainCategories,
                includeNotAudited,
                includeNotConsolidated,
                safePage,
                publisher,
                reportingType,
                normalizedSymbol,
                tracingNumber,
                resolvedSearchMode
        );

        return ApiResponse.success(service.getNotices(query));
    }

    @GetMapping("/companies")
    public ApiResponse<CodalModels.CompaniesResult> getCompanies() {
        return ApiResponse.success(service.getCompanies());
    }

    @GetMapping("/industry-groups")
    public ApiResponse<CodalModels.IndustryGroupsResult> getIndustryGroups() {
        return ApiResponse.success(service.getIndustryGroups());
    }

    @GetMapping("/categories")
    public ApiResponse<CodalModels.CategoriesResult> getCategories() {
        return ApiResponse.success(service.getCategories());
    }

    @GetMapping("/financial-years")
    public ApiResponse<CodalModels.FinancialYearsResult> getFinancialYears(
            @RequestParam(name = "symbol", required = false) String symbol,
            @RequestParam(name = "ticker", required = false) String ticker
    ) {
        return ApiResponse.success(service.getFinancialYears(RequestParamSupport.require("symbol", symbol, ticker)));
    }

    @GetMapping("/auditors")
    public ApiResponse<CodalModels.AuditorsResult> getAuditors() {
        return ApiResponse.success(service.getAuditors());
    }
}
