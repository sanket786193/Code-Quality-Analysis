package com.sanket.AI.Code.Review.Platform.report.html;

import com.sanket.AI.Code.Review.Platform.report.dto.response.*;
import org.springframework.stereotype.Component;

@Component
public class HtmlReportGenerator {

    /**
     * Generates a modern, responsive HTML report string suitable for browser view or PDF rendering.
     */
    public String generateHtmlReport(ReportResponse report) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Review Report - ").append(escapeHtml(report.getReportReference())).append("</title>\n");
        html.append("<style>\n");
        html.append("""
            :root {
                --bg: #0f172a;
                --surface: #1e293b;
                --surface-hover: #334155;
                --text: #f8fafc;
                --text-muted: #94a3b8;
                --border: #334155;
                --primary: #6366f1;
                --critical: #ef4444;
                --high: #f97316;
                --medium: #eab308;
                --low: #3b82f6;
                --success: #10b981;
            }
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: var(--bg); color: var(--text); padding: 32px; line-height: 1.6; }
            .container { max-width: 1200px; margin: 0 auto; }
            .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border); padding-bottom: 24px; margin-bottom: 32px; }
            .title-area h1 { font-size: 28px; font-weight: 700; color: #fff; }
            .badge { display: inline-block; padding: 4px 12px; border-radius: 9999px; font-size: 13px; font-weight: 600; text-transform: uppercase; }
            .badge-approve { background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid #10b981; }
            .badge-changes { background: rgba(234, 179, 8, 0.2); color: #facc15; border: 1px solid #eab308; }
            .badge-reject { background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid #ef4444; }
            .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 20px; margin-bottom: 32px; }
            .card { background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 20px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.2); }
            .card h3 { font-size: 14px; text-transform: uppercase; color: var(--text-muted); margin-bottom: 8px; letter-spacing: 0.05em; }
            .card .val { font-size: 26px; font-weight: 700; color: #fff; }
            .section { margin-bottom: 40px; }
            .section-title { font-size: 20px; font-weight: 600; margin-bottom: 16px; border-left: 4px solid var(--primary); padding-left: 12px; }
            .finding-item { background: var(--surface); border: 1px solid var(--border); border-radius: 8px; padding: 18px; margin-bottom: 14px; }
            .finding-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
            .file-loc { font-family: monospace; font-size: 14px; color: #38bdf8; font-weight: 600; }
            .code-box { background: #0b1120; border: 1px solid #1e293b; border-radius: 6px; padding: 12px; font-family: 'Courier New', monospace; font-size: 13px; overflow-x: auto; margin-top: 8px; }
            .diff-container { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 12px; }
            .diff-before { background: rgba(239, 68, 68, 0.08); border-left: 3px solid var(--critical); padding: 12px; border-radius: 4px; font-family: monospace; font-size: 13px; white-space: pre-wrap; }
            .diff-after { background: rgba(16, 185, 129, 0.08); border-left: 3px solid var(--success); padding: 12px; border-radius: 4px; font-family: monospace; font-size: 13px; white-space: pre-wrap; }
            .ai-fix { background: rgba(99, 102, 241, 0.08); border-left: 3px solid var(--primary); padding: 12px; border-radius: 4px; font-family: monospace; font-size: 13px; white-space: pre-wrap; margin-top: 10px; }
            .footer { text-align: center; color: var(--text-muted); font-size: 12px; border-top: 1px solid var(--border); padding-top: 20px; margin-top: 40px; }
        """);
        html.append("</style>\n</head>\n<body>\n<div class=\"container\">\n");

        // Header
        html.append("<div class=\"header\">\n");
        html.append("<div class=\"title-area\">\n");
        html.append("<h1>AI Code Review & Governance Report</h1>\n");
        html.append("<p style=\"color: var(--text-muted);\">Ref: ").append(escapeHtml(report.getReportReference())).append(" | Job: ").append(escapeHtml(report.getJobReference())).append("</p>\n");
        html.append("</div>\n");

        String recClass = "badge-changes";
        if (report.getRecommendation() != null && report.getRecommendation().getRecommendation() != null) {
            recClass = switch (report.getRecommendation().getRecommendation()) {
                case APPROVE -> "badge-approve";
                case REJECT -> "badge-reject";
                default -> "badge-changes";
            };
        }
        html.append("<div><span class=\"badge ").append(recClass).append("\">")
                .append(report.getRecommendation() != null ? report.getRecommendation().getRecommendation() : "IN_REVIEW")
                .append("</span></div>\n");
        html.append("</div>\n");

        // 1. Summary Card
        ReportSummaryCardDto card = report.getSummaryCard();
        html.append("<div class=\"grid\">\n");
        html.append("<div class=\"card\"><h3>PR Title</h3><div class=\"val\" style=\"font-size: 18px;\">").append(escapeHtml(card != null ? card.getPullRequestTitle() : "")).append("</div></div>\n");
        html.append("<div class=\"card\"><h3>Author</h3><div class=\"val\" style=\"font-size: 18px;\">@").append(escapeHtml(card != null ? card.getAuthor() : "")).append("</div></div>\n");
        html.append("<div class=\"card\"><h3>Branch</h3><div class=\"val\" style=\"font-size: 16px; font-family: monospace;\">").append(escapeHtml(card != null ? card.getBranch() : "")).append("</div></div>\n");
        html.append("<div class=\"card\"><h3>Overall Score</h3><div class=\"val\" style=\"color: #6366f1;\">").append(card != null && card.getOverallScore() != null ? String.format("%.1f", card.getOverallScore()) : "0").append(" / 100</div></div>\n");
        html.append("</div>\n");

        // 2. Issue Breakdown
        IssueBreakdownDto issues = report.getIssueBreakdown();
        if (issues != null) {
            html.append("<div class=\"section\">\n");
            html.append("<h2 class=\"section-title\">Issue Severity Breakdown</h2>\n");
            html.append("<div class=\"grid\">\n");
            html.append("<div class=\"card\"><h3 style=\"color: var(--critical);\">Critical</h3><div class=\"val\" style=\"color: var(--critical);\">").append(issues.getCritical()).append("</div></div>\n");
            html.append("<div class=\"card\"><h3 style=\"color: var(--high);\">High</h3><div class=\"val\" style=\"color: var(--high);\">").append(issues.getHigh()).append("</div></div>\n");
            html.append("<div class=\"card\"><h3 style=\"color: var(--medium);\">Medium</h3><div class=\"val\" style=\"color: var(--medium);\">").append(issues.getMedium()).append("</div></div>\n");
            html.append("<div class=\"card\"><h3 style=\"color: var(--low);\">Low</h3><div class=\"val\" style=\"color: var(--low);\">").append(issues.getLow()).append("</div></div>\n");
            html.append("</div>\n</div>\n");
        }

        // Executive Summary
        if (report.getExecutiveSummary() != null) {
            html.append("<div class=\"section\">\n");
            html.append("<h2 class=\"section-title\">Executive Summary</h2>\n");
            html.append("<div class=\"card\"><p>").append(escapeHtml(report.getExecutiveSummary())).append("</p></div>\n");
            html.append("</div>\n");
        }

        // 3. File-Wise Findings
        html.append("<div class=\"section\">\n");
        html.append("<h2 class=\"section-title\">File-Wise Detailed Findings (").append(report.getFileWiseFindings().size()).append(")</h2>\n");
        for (FileWiseFindingDto f : report.getFileWiseFindings()) {
            html.append("<div class=\"finding-item\">\n");
            html.append("<div class=\"finding-head\">\n");
            html.append("<span class=\"file-loc\">").append(escapeHtml(f.getFileName())).append(" : Line ").append(f.getLineNumber()).append("</span>\n");
            html.append("<span class=\"badge\" style=\"background: #334155; color: #fff;\">").append(f.getSeverity()).append(" | ").append(escapeHtml(f.getIssueType())).append("</span>\n");
            html.append("</div>\n");
            html.append("<p style=\"margin-bottom: 8px;\">").append(escapeHtml(f.getExplanation())).append("</p>\n");
            if (f.getSuggestion() != null && !f.getSuggestion().isBlank()) {
                html.append("<div class=\"code-box\"><strong>Suggested Fix:</strong><br>").append(escapeHtml(f.getSuggestion())).append("</div>\n");
            }
            html.append("</div>\n");
        }
        html.append("</div>\n");

        // 4. Comparison Section
        if (!report.getComparisonSections().isEmpty()) {
            html.append("<div class=\"section\">\n");
            html.append("<h2 class=\"section-title\">Code Comparisons & AI Fix Proposals</h2>\n");
            for (ComparisonSectionDto comp : report.getComparisonSections()) {
                html.append("<div class=\"finding-item\">\n");
                html.append("<div class=\"file-loc\">").append(escapeHtml(comp.getFileName())).append(" (Line ").append(comp.getLineNumber()).append(")</div>\n");
                html.append("<div class=\"diff-container\">\n");
                html.append("<div class=\"diff-before\"><strong>Current / Before:</strong>\n").append(escapeHtml(comp.getBeforeChange())).append("</div>\n");
                html.append("<div class=\"diff-after\"><strong>PR Change / After:</strong>\n").append(escapeHtml(comp.getAfterChange())).append("</div>\n");
                html.append("</div>\n");
                html.append("<div class=\"ai-fix\"><strong>AI Recommended Refactor:</strong>\n").append(escapeHtml(comp.getAiSuggestion())).append("</div>\n");
                if (comp.getActualImpact() != null) {
                    html.append("<p style=\"margin-top: 8px; color: var(--text-muted); font-size: 13px;\"><em>Impact: ").append(escapeHtml(comp.getActualImpact())).append("</em></p>\n");
                }
                html.append("</div>\n");
            }
            html.append("</div>\n");
        }

        // Footer
        html.append("<div class=\"footer\">\n");
        html.append("Generated by AI Code Review & Governance Platform | ").append(report.getGeneratedAt() != null ? report.getGeneratedAt().toString() : "").append("\n");
        html.append("</div>\n");

        html.append("</div>\n</body>\n</html>");
        return html.toString();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
