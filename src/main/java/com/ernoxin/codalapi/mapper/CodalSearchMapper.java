package com.ernoxin.codalapi.mapper;

import com.ernoxin.codalapi.common.util.CodalMapperSupport;
import com.ernoxin.codalapi.domain.CodalModels;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class CodalSearchMapper {

    private final CodalMapperSupport support;

    CodalModels.NoticeSearchResult toNoticeSearchResult(JsonNode root) {
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

    CodalModels.CompaniesResult toCompaniesResult(JsonNode root) {
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

    CodalModels.IndustryGroupsResult toIndustryGroupsResult(JsonNode root) {
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

    CodalModels.CategoriesResult toCategoriesResult(JsonNode root) {
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

    CodalModels.FinancialYearsResult toFinancialYearsResult(String symbol, JsonNode root) {
        List<String> financialYears = support.textList(root);
        return new CodalModels.FinancialYearsResult(symbol, financialYears);
    }

    CodalModels.AuditorsResult toAuditorsResult(JsonNode root) {
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
}
