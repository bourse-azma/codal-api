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

    public record FinancialStatementQuery(
            String letterSerial,
            Integer sheetId,
            int reportType,
            int letterType,
            int companyType,
            int fileType
    ) {
    }

    public record FinancialStatementResult(
            String letterSerial,
            String titleFa,
            String titleEn,
            int type,
            int period,
            String periodEndToDate,
            String yearEndToDate,
            boolean isConsolidated,
            boolean isAudited,
            long tracingNumber,
            String registerDateTime,
            String sentDateTime,
            String publishDateTime,
            List<AvailableSheetItem> availableSheets,
            List<FinancialSheet> sheets,
            JsonNode raw
    ) {
    }

    public record FinancialStatementBySymbolResult(
            String symbol,
            String companyName,
            String noticeTitle,
            String publishDateTime,
            long tracingNumber,
            String letterSerial,
            FinancialStatementResult statement
    ) {
    }

    public record FinancialNoticeListResult(
            String symbol,
            int page,
            int size,
            int length,
            int totalCount,
            List<FinancialNoticeItem> notices
    ) {
    }

    public record FinancialNoticeItem(
            long tracingNumber,
            String symbol,
            String companyName,
            String title,
            String letterCode,
            String sentDateTime,
            String publishDateTime,
            String letterSerial,
            String reportUrl,
            boolean hasExcel,
            boolean hasPdf,
            boolean hasAttachment
    ) {
    }

    public record AvailableSheetItem(
            int sheetId,
            String title
    ) {
    }

    public record FinancialSheet(
            int code,
            String titleFa,
            String titleEn,
            String aliasName,
            int sequence,
            boolean isDynamic,
            List<FinancialTable> tables
    ) {
    }

    public record FinancialTable(
            int metaTableId,
            int code,
            String titleFa,
            String titleEn,
            String aliasName,
            int sequence,
            List<FinancialColumn> columns,
            List<FinancialRow> rows
    ) {
    }

    public record FinancialColumn(
            int columnCode,
            int columnSequence,
            String title,
            String periodEndToDate,
            String yearEndToDate
    ) {
    }

    public record FinancialRow(
            int rowCode,
            int rowSequence,
            String title,
            String rowTypeName,
            List<FinancialValue> values
    ) {
    }

    public record FinancialValue(
            int columnCode,
            String address,
            String value,
            String financialConcept,
            String valueTypeName,
            String dataTypeName,
            String periodEndToDate,
            String yearEndToDate
    ) {
    }
}
