package com.eip.document.service;

import com.eip.document.domain.DocumentType;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Map;

@Service
@Slf4j
public class PdfGeneratorService {

    public byte[] generatePdf(DocumentType documentType, Map<String, String> data) {
        return switch (documentType) {
            case POLICY_CERTIFICATE -> generatePolicyCertificate(data);
            case CLAIMS_SETTLEMENT_LETTER -> generateClaimsSettlementLetter(data);
            case INVOICE -> generateInvoicePdf(data);
            default -> generateGenericDocument(documentType, data);
        };
    }

    private byte[] generatePolicyCertificate(Map<String, String> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("INSURANCE POLICY CERTIFICATE").setBold().setFontSize(18));
            document.add(new Paragraph("Issued by: Enterprise Insurance Platform").setFontSize(12));
            document.add(new Paragraph("Date: " + LocalDate.now()).setFontSize(10));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            addTableRow(table, "Policy Number", data.getOrDefault("policyId", "N/A"));
            addTableRow(table, "Insured Name", data.getOrDefault("insuredName", "N/A"));
            addTableRow(table, "Customer ID", data.getOrDefault("customerId", "N/A"));
            addTableRow(table, "Line of Business", data.getOrDefault("lineOfBusiness", "N/A"));
            addTableRow(table, "Coverage Limit", data.getOrDefault("coverageLimit", "N/A"));
            addTableRow(table, "Deductible", data.getOrDefault("deductible", "N/A"));
            addTableRow(table, "Effective Date", data.getOrDefault("effectiveDate", "N/A"));
            addTableRow(table, "Expiration Date", data.getOrDefault("expirationDate", "N/A"));
            addTableRow(table, "Annual Premium", data.getOrDefault("premiumAmount", "N/A"));
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("This certificate confirms the existence of insurance coverage as described above.")
                    .setFontSize(9).setItalic());

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate policy certificate PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private byte[] generateClaimsSettlementLetter(Map<String, String> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CLAIMS SETTLEMENT LETTER").setBold().setFontSize(18));
            document.add(new Paragraph("Date: " + LocalDate.now()).setFontSize(10));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Dear " + data.getOrDefault("insuredName", "Policyholder") + ",").setFontSize(12));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                "We are pleased to inform you that your claim (ID: " +
                data.getOrDefault("claimId", "N/A") +
                ") has been approved for settlement in the amount of $" +
                data.getOrDefault("approvedAmount", "0.00") + "."
            ).setFontSize(12));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            addTableRow(table, "Claim ID", data.getOrDefault("claimId", "N/A"));
            addTableRow(table, "Policy Number", data.getOrDefault("policyId", "N/A"));
            addTableRow(table, "Date of Loss", data.getOrDefault("dateOfLoss", "N/A"));
            addTableRow(table, "Settlement Amount", "$" + data.getOrDefault("approvedAmount", "0.00"));
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate claims settlement PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private byte[] generateInvoicePdf(Map<String, String> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("INSURANCE INVOICE").setBold().setFontSize(18));
            document.add(new Paragraph("Invoice #: " + data.getOrDefault("invoiceId", "N/A")).setFontSize(12));
            document.add(new Paragraph("Date: " + LocalDate.now()).setFontSize(10));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            addTableRow(table, "Policy Number", data.getOrDefault("policyId", "N/A"));
            addTableRow(table, "Amount Due", "$" + data.getOrDefault("amount", "0.00"));
            addTableRow(table, "Due Date", data.getOrDefault("dueDate", "N/A"));
            addTableRow(table, "Grace Period Ends", data.getOrDefault("gracePeriodEndsAt", "N/A"));
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private byte[] generateGenericDocument(DocumentType type, Map<String, String> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph(type.name().replace("_", " ")).setBold().setFontSize(18));
            document.add(new Paragraph("Date: " + LocalDate.now()).setFontSize(10));
            document.add(new Paragraph(" "));

            Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            data.forEach((k, v) -> addTableRow(table, k, v));
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate generic PDF: {}", e.getMessage(), e);
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Paragraph(label).setBold());
        table.addCell(new Paragraph(value != null ? value : ""));
    }
}
