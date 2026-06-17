package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.client.CodalClient;
import com.ernoxin.codalapi.domain.CodalModels;
import com.ernoxin.codalapi.mapper.CodalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CodalFetchService {

    private final CodalClient client;
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
}
