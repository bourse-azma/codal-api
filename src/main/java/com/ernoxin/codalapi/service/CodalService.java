package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.domain.CodalFinancialModels;
import com.ernoxin.codalapi.domain.CodalModels;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CodalService {

    private final CodalFetchService fetchService;

    @Cacheable(
            cacheResolver = "codalCacheResolver",
            key = "T(com.ernoxin.codalapi.cache.CodalCacheKeys).notices(#query)",
            sync = true
    )
    public CodalModels.NoticeSearchResult getNotices(CodalModels.NoticeSearchQuery query) {
        return fetchService.getNotices(query);
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.CompaniesResult getCompanies() {
        return fetchService.getCompanies();
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.IndustryGroupsResult getIndustryGroups() {
        return fetchService.getIndustryGroups();
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.CategoriesResult getCategories() {
        return fetchService.getCategories();
    }

    @Cacheable(
            cacheResolver = "codalCacheResolver",
            key = "T(com.ernoxin.codalapi.cache.CodalCacheKeys).financialYears(#symbol)",
            sync = true
    )
    public CodalModels.FinancialYearsResult getFinancialYears(String symbol) {
        return fetchService.getFinancialYears(symbol);
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.AuditorsResult getAuditors() {
        return fetchService.getAuditors();
    }

    @Cacheable(
            cacheResolver = "codalCacheResolver",
            key = "T(com.ernoxin.codalapi.cache.CodalCacheKeys).financialStatements(#query)",
            sync = true
    )
    public CodalFinancialModels.FinancialStatementResult getFinancialStatement(CodalFinancialModels.FinancialStatementQuery query) {
        return fetchService.getFinancialStatement(query);
    }

    public CodalFinancialModels.FinancialStatementBySymbolResult getFinancialStatementBySymbol(String symbol, Integer sheetId) {
        return fetchService.getFinancialStatementBySymbol(symbol, sheetId);
    }

    public CodalFinancialModels.FinancialNoticeListResult getFinancialNoticesBySymbol(String symbol, int page, int size, int length) {
        return fetchService.getFinancialNoticesBySymbol(symbol, page, size, length);
    }

    public CodalFinancialModels.FinancialStatementResult getFinancialStatementByNotice(String letterSerial, Integer sheetId) {
        return fetchService.getFinancialStatementByNotice(letterSerial, sheetId);
    }
}
