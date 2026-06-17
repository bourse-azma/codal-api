package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.domain.CodalModels;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
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

    @CachePut(
            cacheResolver = "codalCacheResolver",
            key = "T(com.ernoxin.codalapi.cache.CodalCacheKeys).notices(#query)"
    )
    public CodalModels.NoticeSearchResult refreshNotices(CodalModels.NoticeSearchQuery query) {
        return fetchService.getNotices(query);
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.CompaniesResult getCompanies() {
        return fetchService.getCompanies();
    }

    @CachePut(cacheResolver = "codalCacheResolver", key = "'all'")
    public CodalModels.CompaniesResult refreshCompanies() {
        return fetchService.getCompanies();
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.IndustryGroupsResult getIndustryGroups() {
        return fetchService.getIndustryGroups();
    }

    @CachePut(cacheResolver = "codalCacheResolver", key = "'all'")
    public CodalModels.IndustryGroupsResult refreshIndustryGroups() {
        return fetchService.getIndustryGroups();
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.CategoriesResult getCategories() {
        return fetchService.getCategories();
    }

    @CachePut(cacheResolver = "codalCacheResolver", key = "'all'")
    public CodalModels.CategoriesResult refreshCategories() {
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

    @CachePut(
            cacheResolver = "codalCacheResolver",
            key = "T(com.ernoxin.codalapi.cache.CodalCacheKeys).financialYears(#symbol)"
    )
    public CodalModels.FinancialYearsResult refreshFinancialYears(String symbol) {
        return fetchService.getFinancialYears(symbol);
    }

    @Cacheable(cacheResolver = "codalCacheResolver", key = "'all'", sync = true)
    public CodalModels.AuditorsResult getAuditors() {
        return fetchService.getAuditors();
    }

    @CachePut(cacheResolver = "codalCacheResolver", key = "'all'")
    public CodalModels.AuditorsResult refreshAuditors() {
        return fetchService.getAuditors();
    }
}
