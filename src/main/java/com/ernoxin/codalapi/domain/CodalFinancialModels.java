package com.ernoxin.codalapi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public final class CodalFinancialModels {

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
