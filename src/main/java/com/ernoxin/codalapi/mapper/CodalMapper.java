package com.ernoxin.codalapi.mapper;

import com.ernoxin.codalapi.common.util.CodalMapperSupport;
import com.ernoxin.codalapi.domain.CodalModels;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class CodalMapper {

    private final CodalMapperSupport support;

    public CodalModels.NoticeSearchResult toNoticeSearchResult(JsonNode root) {
        List<CodalModels.NoticeItem> notices = new ArrayList<>();
        for (JsonNode node : support.list(root.path("Letters"))) {
            String reportPath = support.text(node, "Url");
            String attachmentPath = support.text(node, "AttachmentUrl");
            String pdfPath = support.text(node, "PdfUrl");

            notices.add(new CodalModels.NoticeItem(
                    support.lng(node, "TracingNo"),
                    support.text(node, "Symbol"),
                    support.text(node, "CompanyName"),
                    support.integer(node, "UnderSupervision"),
                    toSupervisionState(node.path("SuperVision")),
                    support.text(node, "Title"),
                    support.text(node, "LetterCode"),
                    support.text(node, "SentDateTime"),
                    support.text(node, "PublishDateTime"),
                    support.bool(node, "HasHtml"),
                    support.bool(node, "IsEstimate"),
                    reportPath,
                    support.absoluteCodalUrl(reportPath),
                    support.bool(node, "HasExcel"),
                    support.bool(node, "HasPdf"),
                    support.bool(node, "HasXbrl"),
                    support.bool(node, "HasAttachment"),
                    attachmentPath,
                    support.absoluteCodalUrl(attachmentPath),
                    pdfPath,
                    support.absoluteCodalUrl(pdfPath),
                    support.absoluteCodalUrl(support.text(node, "ExcelUrl")),
                    support.absoluteCodalUrl(support.text(node, "XbrlUrl")),
                    support.absoluteCodalUrl(support.text(node, "TedanUrl")),
                    node
            ));
        }

        return new CodalModels.NoticeSearchResult(
                support.integer(root, "Total"),
                support.integer(root, "Page"),
                support.bool(root, "IsAttacker"),
                notices
        );
    }

    public CodalModels.CompaniesResult toCompaniesResult(JsonNode root) {
        List<CodalModels.CompanyItem> companies = new ArrayList<>();

        for (JsonNode node : support.list(root)) {
            companies.add(new CodalModels.CompanyItem(
                    support.text(node, "sy"),
                    support.text(node, "n"),
                    support.text(node, "i"),
                    support.integer(node, "t"),
                    support.integer(node, "st"),
                    support.integer(node, "IG"),
                    support.integer(node, "RT"),
                    node
            ));
        }

        return new CodalModels.CompaniesResult(companies);
    }

    public CodalModels.IndustryGroupsResult toIndustryGroupsResult(JsonNode root) {
        List<CodalModels.IndustryGroupItem> industryGroups = new ArrayList<>();

        for (JsonNode node : support.list(root)) {
            industryGroups.add(new CodalModels.IndustryGroupItem(
                    support.integer(node, "Id"),
                    support.text(node, "Name"),
                    node
            ));
        }

        return new CodalModels.IndustryGroupsResult(industryGroups);
    }

    public CodalModels.CategoriesResult toCategoriesResult(JsonNode root) {
        List<CodalModels.CategoryItem> categories = new ArrayList<>();

        for (JsonNode node : support.list(root)) {
            categories.add(new CodalModels.CategoryItem(
                    support.integer(node, "Code"),
                    support.text(node, "Name"),
                    toPublisherTypes(node.path("PublisherTypes")),
                    node
            ));
        }

        return new CodalModels.CategoriesResult(categories);
    }

    public CodalModels.FinancialYearsResult toFinancialYearsResult(String symbol, JsonNode root) {
        List<String> financialYears = support.textList(root);
        return new CodalModels.FinancialYearsResult(symbol, financialYears);
    }

    public CodalModels.AuditorsResult toAuditorsResult(JsonNode root) {
        List<CodalModels.AuditorItem> auditors = new ArrayList<>();

        for (JsonNode node : support.list(root)) {
            auditors.add(new CodalModels.AuditorItem(
                    support.text(node, "n"),
                    support.integer(node, "c"),
                    node
            ));
        }

        return new CodalModels.AuditorsResult(auditors);
    }

    public CodalModels.FinancialStatementResult toFinancialStatementResult(String letterSerial, String html) {
        JsonNode datasource = support.extractDatasource(html);

        List<CodalModels.AvailableSheetItem> availableSheets = extractAvailableSheets(html);

        List<CodalModels.FinancialSheet> sheets = new ArrayList<>();
        for (JsonNode sheetNode : support.list(datasource.path("sheets"))) {
            sheets.add(toFinancialSheet(sheetNode));
        }

        return new CodalModels.FinancialStatementResult(
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

    public List<CodalModels.AvailableSheetItem> extractAvailableSheets(String html) {
        List<CodalModels.AvailableSheetItem> availableSheets = new ArrayList<>();
        support.extractAvailableSheets(html).forEach((sheetId, title) ->
                availableSheets.add(new CodalModels.AvailableSheetItem(sheetId, title)));
        return availableSheets;
    }

    public CodalModels.FinancialStatementResult combineFinancialStatements(
            String letterSerial,
            List<CodalModels.AvailableSheetItem> availableSheets,
            List<CodalModels.FinancialStatementResult> parsed
    ) {
        List<CodalModels.FinancialSheet> sheets = new ArrayList<>();
        for (CodalModels.FinancialStatementResult result : parsed) {
            sheets.addAll(result.sheets());
        }

        CodalModels.FinancialStatementResult metadataSource = parsed.isEmpty() ? null : parsed.getFirst();

        return new CodalModels.FinancialStatementResult(
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

    private CodalModels.FinancialSheet toFinancialSheet(JsonNode sheetNode) {
        List<CodalModels.FinancialTable> tables = new ArrayList<>();
        for (JsonNode tableNode : support.list(sheetNode.path("tables"))) {
            tables.add(toFinancialTable(tableNode));
        }

        return new CodalModels.FinancialSheet(
                support.integer(sheetNode, "code"),
                support.text(sheetNode, "title_Fa"),
                support.text(sheetNode, "title_En"),
                support.text(sheetNode, "aliasName"),
                support.integer(sheetNode, "sequence"),
                support.bool(sheetNode, "isDynamic"),
                tables
        );
    }

    private CodalModels.FinancialTable toFinancialTable(JsonNode tableNode) {
        Map<Integer, CodalModels.FinancialColumn> columns = new LinkedHashMap<>();
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
                    columns.putIfAbsent(columnCode, new CodalModels.FinancialColumn(
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

            row.values.add(new CodalModels.FinancialValue(
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

        List<CodalModels.FinancialColumn> sortedColumns = columns.values().stream()
                .sorted(Comparator.comparingInt(CodalModels.FinancialColumn::columnSequence))
                .toList();

        List<CodalModels.FinancialRow> sortedRows = rows.values().stream()
                .sorted(Comparator.comparingInt(row -> row.rowSequence))
                .map(RowAccumulator::toRow)
                .toList();

        return new CodalModels.FinancialTable(
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

    private CodalModels.SupervisionState toSupervisionState(JsonNode node) {
        return new CodalModels.SupervisionState(
                support.integer(node, "UnderSupervision"),
                support.text(node, "AdditionalInfo"),
                support.textList(node.path("Reasons"))
        );
    }

    private List<CodalModels.PublisherTypeItem> toPublisherTypes(JsonNode root) {
        List<CodalModels.PublisherTypeItem> publisherTypes = new ArrayList<>();

        for (JsonNode node : support.list(root)) {
            publisherTypes.add(new CodalModels.PublisherTypeItem(
                    support.integer(node, "Code"),
                    support.text(node, "Name"),
                    toLetterTypes(node.path("LetterTypes")),
                    node
            ));
        }

        return publisherTypes;
    }

    private List<CodalModels.LetterTypeItem> toLetterTypes(JsonNode root) {
        List<CodalModels.LetterTypeItem> letterTypes = new ArrayList<>();

        for (JsonNode node : support.list(root)) {
            letterTypes.add(new CodalModels.LetterTypeItem(
                    support.integer(node, "Id"),
                    support.text(node, "Name"),
                    support.text(node, "Code"),
                    node
            ));
        }

        return letterTypes;
    }

    private static final class RowAccumulator {
        private final int rowCode;
        private final int rowSequence;
        private final String rowTypeName;
        private final List<CodalModels.FinancialValue> values = new ArrayList<>();
        private String title = "";

        private RowAccumulator(int rowCode, int rowSequence, String rowTypeName) {
            this.rowCode = rowCode;
            this.rowSequence = rowSequence;
            this.rowTypeName = rowTypeName;
        }

        private CodalModels.FinancialRow toRow() {
            values.sort(Comparator.comparingInt(CodalModels.FinancialValue::columnCode));
            return new CodalModels.FinancialRow(rowCode, rowSequence, title, rowTypeName, values);
        }
    }
}
