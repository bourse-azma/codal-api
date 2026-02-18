package com.ernoxin.codalapi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public final class CodalModels {

    public record NoticeSearchQuery(
            boolean includeAudited,
            int auditorRef,
            int categoryCode,
            boolean includeChildCategories,
            int companyState,
            int companyType,
            boolean includeConsolidated,
            boolean isNotAuditedFilter,
            int length,
            int letterType,
            boolean includeMainCategories,
            boolean includeNotAudited,
            boolean includeNotConsolidated,
            int page,
            boolean publisher,
            int reportingType,
            String symbol,
            int tracingNumber,
            boolean searchMode
    ) {
    }

    public record NoticeSearchResult(
            int totalCount,
            int page,
            boolean attackerDetected,
            List<NoticeItem> notices
    ) {
    }

    public record NoticeItem(
            long tracingNumber,
            String symbol,
            String companyName,
            int underSupervision,
            SupervisionState supervision,
            String title,
            String letterCode,
            String sentDateTime,
            String publishDateTime,
            boolean hasHtml,
            boolean estimate,
            String reportPath,
            String reportUrl,
            boolean hasExcel,
            boolean hasPdf,
            boolean hasXbrl,
            boolean hasAttachment,
            String attachmentPath,
            String attachmentUrl,
            String pdfPath,
            String pdfUrl,
            String excelUrl,
            String xbrlUrl,
            String tedanUrl,
            JsonNode raw
    ) {
    }

    public record SupervisionState(
            int underSupervision,
            String additionalInfo,
            List<String> reasons
    ) {
    }

    public record CompaniesResult(
            List<CompanyItem> companies
    ) {
    }

    public record CompanyItem(
            String symbol,
            String companyName,
            String companyId,
            int companyType,
            int companyState,
            int industryGroupCode,
            int reportingType,
            JsonNode raw
    ) {
    }

    public record IndustryGroupsResult(
            List<IndustryGroupItem> industryGroups
    ) {
    }

    public record IndustryGroupItem(
            int id,
            String name,
            JsonNode raw
    ) {
    }

    public record CategoriesResult(
            List<CategoryItem> categories
    ) {
    }

    public record CategoryItem(
            int categoryCode,
            String categoryName,
            List<PublisherTypeItem> publisherTypes,
            JsonNode raw
    ) {
    }

    public record PublisherTypeItem(
            int publisherTypeCode,
            String publisherTypeName,
            List<LetterTypeItem> letterTypes,
            JsonNode raw
    ) {
    }

    public record LetterTypeItem(
            int letterTypeId,
            String letterTypeName,
            String letterTypeCode,
            JsonNode raw
    ) {
    }

    public record FinancialYearsResult(
            String symbol,
            List<String> financialYears
    ) {
    }

    public record AuditorsResult(
            List<AuditorItem> auditors
    ) {
    }

    public record AuditorItem(
            String auditorName,
            int auditorCode,
            JsonNode raw
    ) {
    }
}
