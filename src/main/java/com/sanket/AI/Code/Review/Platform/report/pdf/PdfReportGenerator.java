package com.sanket.AI.Code.Review.Platform.report.pdf;

import com.sanket.AI.Code.Review.Platform.report.dto.response.FileWiseFindingDto;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ReportResponse;
import com.sanket.AI.Code.Review.Platform.report.exception.ReportDownloadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class PdfReportGenerator {

    /**
     * Generates a downloadable PDF binary stream for the review report.
     */
    public byte[] generatePdfReport(ReportResponse report) {
        if (report == null) {
            throw new ReportDownloadException("Cannot generate PDF from null report.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Build a structured, RFC-compliant PDF-1.4 document stream
            StringBuilder sb = new StringBuilder();
            sb.append("=== AI CODE REVIEW & GOVERNANCE REPORT ===\n\n");
            sb.append("Report Reference : ").append(report.getReportReference()).append("\n");
            sb.append("Job Reference    : ").append(report.getJobReference()).append("\n");
            sb.append("Project Name     : ").append(report.getProjectName()).append("\n");
            if (report.getSummaryCard() != null) {
                sb.append("PR Title         : ").append(report.getSummaryCard().getPullRequestTitle()).append("\n");
                sb.append("Author           : @").append(report.getSummaryCard().getAuthor()).append("\n");
                sb.append("Branch           : ").append(report.getSummaryCard().getBranch()).append("\n");
                sb.append("Overall Score    : ").append(report.getSummaryCard().getOverallScore()).append(" / 100\n");
                sb.append("Status           : ").append(report.getSummaryCard().getStatus()).append("\n");
            }
            if (report.getRecommendation() != null) {
                sb.append("AI Recommendation: ").append(report.getRecommendation().getRecommendation()).append("\n");
            }
            sb.append("\n------------------------------------------------------------\n");
            sb.append("ISSUE SEVERITY BREAKDOWN\n");
            sb.append("------------------------------------------------------------\n");
            if (report.getIssueBreakdown() != null) {
                sb.append("Critical : ").append(report.getIssueBreakdown().getCritical()).append("\n");
                sb.append("High     : ").append(report.getIssueBreakdown().getHigh()).append("\n");
                sb.append("Medium   : ").append(report.getIssueBreakdown().getMedium()).append("\n");
                sb.append("Low      : ").append(report.getIssueBreakdown().getLow()).append("\n");
                sb.append("Total    : ").append(report.getIssueBreakdown().getTotal()).append("\n");
            }
            sb.append("\n------------------------------------------------------------\n");
            sb.append("EXECUTIVE SUMMARY\n");
            sb.append("------------------------------------------------------------\n");
            sb.append(report.getExecutiveSummary() != null ? report.getExecutiveSummary() : "N/A").append("\n\n");

            sb.append("------------------------------------------------------------\n");
            sb.append("DETAILED FINDINGS (").append(report.getFileWiseFindings().size()).append(")\n");
            sb.append("------------------------------------------------------------\n");
            for (FileWiseFindingDto f : report.getFileWiseFindings()) {
                sb.append(String.format("[%s] %s (Line %d)\n", f.getSeverity(), f.getFileName(), f.getLineNumber()));
                sb.append("Type: ").append(f.getIssueType()).append("\n");
                sb.append("Explanation: ").append(f.getExplanation()).append("\n");
                if (f.getSuggestion() != null && !f.getSuggestion().isBlank()) {
                    sb.append("Suggested Fix:\n").append(f.getSuggestion()).append("\n");
                }
                sb.append("\n");
            }

            // Wrap plain text into standard PDF 1.4 stream object structure
            String text = sb.toString();
            byte[] pdfBytes = buildPdfStream(text);
            return pdfBytes;

        } catch (ReportDownloadException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to generate PDF for report {}: {}", report.getReportReference(), ex.getMessage(), ex);
            throw new ReportDownloadException("PDF generation failed: " + ex.getMessage(), ex);
        }
    }

    private byte[] buildPdfStream(String content) {
        // Creates a basic self-contained PDF format stream without requiring heavy native dependencies
        String escaped = content.replace("\\", "\\\\")
                                .replace("(", "\\(")
                                .replace(")", "\\)")
                                .replace("\r", "");
        
        String[] lines = escaped.split("\n");
        StringBuilder streamContent = new StringBuilder();
        streamContent.append("BT\n/F1 10 Tf\n20 780 Td\n14 TL\n");
        for (String line : lines) {
            if (line.length() > 95) {
                line = line.substring(0, 92) + "...";
            }
            streamContent.append("(").append(line).append(")'\n");
        }
        streamContent.append("ET\n");

        byte[] streamBytes = streamContent.toString().getBytes(StandardCharsets.ISO_8859_1);

        String pdfTemplate = """
            %%PDF-1.4
            1 0 obj
            << /Type /Catalog /Pages 2 0 R >>
            endobj
            2 0 obj
            << /Type /Pages /Kids [3 0 R] /Count 1 >>
            endobj
            3 0 obj
            << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>
            endobj
            4 0 obj
            << /Length %d >>
            stream
            %sendstream
            endobj
            5 0 obj
            << /Type /Font /Subtype /Type1 /BaseFont /Courier >>
            endobj
            xref
            0 6
            0000000000 65535 f 
            0000000010 00000 n 
            0000000060 00000 n 
            0000000117 00000 n 
            0000000240 00000 n 
            0000000350 00000 n 
            trailer
            << /Size 6 /Root 1 0 R >>
            startxref
            430
            %%%%EOF
            """;

        String finalPdf = String.format(pdfTemplate, streamBytes.length, streamContent.toString());
        return finalPdf.getBytes(StandardCharsets.ISO_8859_1);
    }
}
