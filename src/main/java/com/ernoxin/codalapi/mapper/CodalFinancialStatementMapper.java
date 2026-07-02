package com.ernoxin.codalapi.mapper;

import com.ernoxin.codalapi.common.util.CodalMapperSupport;
import com.ernoxin.codalapi.domain.CodalFinancialModels;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
class CodalFinancialStatementMapper {

    private final CodalMapperSupport support;

    CodalFinancialModels.FinancialStatementResult toFinancialStatementResult(String letterSerial, String html) {
        JsonNode datasource = support.extractDatasource(html);

        List<CodalFinancialModels.AvailableSheetItem> availableSheets = extractAvailableSheets(html);

        List<CodalFinancialModels.FinancialSheet> sheets = new ArrayList<>();
        for (JsonNode sheetNode : support.list(datasource.path("sheets"))) {
            sheets.add(toFinancialSheet(sheetNode));
        }

        return new CodalFinancialModels.FinancialStatementResult(
                letterSerial,
                support.text(datasource, "title_Fa"),
                support.text(datasource, "title_En"),
                support.integer(datasource, "type"),
                support.integer(datasource, "period"),
                support.text(datasource, "periodEndToDate"),
                support.text(datasource, "yearEndToDate"),
                support.bool(datasource, "isConsolidated"),
                support.bool(datasource, "isAudited"),
                support.lng(datasource, "tracingNo"),
                support.text(datasource, "registerDateTime"),
                support.text(datasource, "sentDateTime"),
                support.text(datasource, "publishDateTime"),
                availableSheets,
                sheets,
                datasource
        );
    }

    List<CodalFinancialModels.AvailableSheetItem> extractAvailableSheets(String html) {
        List<CodalFinancialModels.AvailableSheetItem> availableSheets = new ArrayList<>();
        support.extractAvailableSheets(html).forEach((sheetId, title) ->
                availableSheets.add(new CodalFinancialModels.AvailableSheetItem(sheetId, title)));
        return availableSheets;
    }

    CodalFinancialModels.FinancialStatementResult combineFinancialStatements(
            String letterSerial,
            List<CodalFinancialModels.AvailableSheetItem> availableSheets,
            List<CodalFinancialModels.FinancialStatementResult> parsed
    ) {
        List<CodalFinancialModels.FinancialSheet> sheets = new ArrayList<>();
        for (CodalFinancialModels.FinancialStatementResult result : parsed) {
            sheets.addAll(result.sheets());
        }

        CodalFinancialModels.FinancialStatementResult metadataSource = parsed.isEmpty() ? null : parsed.getFirst();

        return new CodalFinancialModels.FinancialStatementResult(
                letterSerial,
                metadataSource != null ? metadataSource.titleFa() : "",
                metadataSource != null ? metadataSource.titleEn() : "",
                metadataSource != null ? metadataSource.type() : 0,
                metadataSource != null ? metadataSource.period() : 0,
                metadataSource != null ? metadataSource.periodEndToDate() : "",
                metadataSource != null ? metadataSource.yearEndToDate() : "",
                metadataSource != null && metadataSource.isConsolidated(),
                metadataSource != null && metadataSource.isAudited(),
                metadataSource != null ? metadataSource.tracingNumber() : 0L,
                metadataSource != null ? metadataSource.registerDateTime() : "",
                metadataSource != null ? metadataSource.sentDateTime() : "",
                metadataSource != null ? metadataSource.publishDateTime() : "",
                availableSheets,
                sheets,
                metadataSource != null ? metadataSource.raw() : null
        );
    }

    private CodalFinancialModels.FinancialSheet toFinancialSheet(JsonNode sheetNode) {
        List<CodalFinancialModels.FinancialTable> tables = new ArrayList<>();
        for (JsonNode tableNode : support.list(sheetNode.path("tables"))) {
            tables.add(toFinancialTable(tableNode));
        }

        return new CodalFinancialModels.FinancialSheet(
                support.integer(sheetNode, "code"),
                support.text(sheetNode, "title_Fa"),
                support.text(sheetNode, "title_En"),
                support.text(sheetNode, "aliasName"),
                support.integer(sheetNode, "sequence"),
                support.bool(sheetNode, "isDynamic"),
                tables
        );
    }

    private CodalFinancialModels.FinancialTable toFinancialTable(JsonNode tableNode) {
        Map<Integer, CodalFinancialModels.FinancialColumn> columns = new LinkedHashMap<>();
        Map<Integer, RowAccumulator> rows = new LinkedHashMap<>();

        for (JsonNode cell : support.list(tableNode.path("cells"))) {
            if (!support.bool(cell, "isVisible")) {
                continue;
            }

            int rowCode = support.integer(cell, "rowCode");
            int columnCode = support.integer(cell, "columnCode");
            String group = support.text(cell, "cellGroupName");
            String value = support.text(cell, "value");

            boolean isHeaderCell = "Header".equalsIgnoreCase(group);
            boolean isLabelColumn = columnCode <= 1;

            if (isHeaderCell) {
                if (!isLabelColumn) {
                    columns.putIfAbsent(columnCode, new CodalFinancialModels.FinancialColumn(
                            columnCode,
                            support.integer(cell, "columnSequence"),
                            value,
                            support.text(cell, "periodEndToDate"),
                            support.text(cell, "yearEndToDate")
                    ));
                }
                // Column-1 header cells are the "شرح"/description caption, not a data row.
                continue;
            }

            RowAccumulator row = rows.computeIfAbsent(rowCode, code -> new RowAccumulator(
                    code,
                    support.integer(cell, "rowSequence"),
                    support.text(cell, "rowTypeName")
            ));

            if (isLabelColumn) {
                if (row.title.isEmpty() && !value.isEmpty()) {
                    row.title = value;
                }
                continue;
            }

            row.values.add(new CodalFinancialModels.FinancialValue(
                    columnCode,
                    support.text(cell, "address"),
                    value,
                    optionalText(cell, "financialConcept"),
                    support.text(cell, "valueTypeName"),
                    optionalText(cell, "dataTypeName"),
                    support.text(cell, "periodEndToDate"),
                    support.text(cell, "yearEndToDate")
            ));
        }

        List<CodalFinancialModels.FinancialColumn> sortedColumns = columns.values().stream()
                .sorted(Comparator.comparingInt(CodalFinancialModels.FinancialColumn::columnSequence))
                .toList();

        List<CodalFinancialModels.FinancialRow> sortedRows = rows.values().stream()
                .sorted(Comparator.comparingInt(row -> row.rowSequence))
                .map(RowAccumulator::toRow)
                .toList();

        return new CodalFinancialModels.FinancialTable(
                support.integer(tableNode, "metaTableId"),
                support.integer(tableNode, "code"),
                support.text(tableNode, "title_Fa"),
                support.text(tableNode, "title_En"),
                support.text(tableNode, "aliasName"),
                support.integer(tableNode, "sequence"),
                sortedColumns,
                sortedRows
        );
    }

    private String optionalText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) {
            return null;
        }
        return value.asText("").trim();
    }

    private static final class RowAccumulator {
        private final int rowCode;
        private final int rowSequence;
        private final String rowTypeName;
        private final List<CodalFinancialModels.FinancialValue> values = new ArrayList<>();
        private String title = "";

        private RowAccumulator(int rowCode, int rowSequence, String rowTypeName) {
            this.rowCode = rowCode;
            this.rowSequence = rowSequence;
            this.rowTypeName = rowTypeName;
        }

        private CodalFinancialModels.FinancialRow toRow() {
            values.sort(Comparator.comparingInt(CodalFinancialModels.FinancialValue::columnCode));
            return new CodalFinancialModels.FinancialRow(rowCode, rowSequence, title, rowTypeName, values);
        }
    }
}
