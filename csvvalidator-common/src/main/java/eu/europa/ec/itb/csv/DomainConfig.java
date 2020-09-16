package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.config.LabelConfig;
import eu.europa.ec.itb.validation.commons.config.WebDomainConfig;

import java.util.Map;

public class DomainConfig extends WebDomainConfig<DomainConfig.Label> {

    private CsvOptions csvOptions = new CsvOptions();

    public CsvOptions getCsvOptions() {
        return csvOptions;
    }

    @Override
    protected Label newLabelConfig() {
        return new Label();
    }

    public ValidationArtifactInfo getSchemaInfo(String validationType) {
        return getArtifactInfo().get(validationType).get();
    }

    public static class CsvOptions {

        private Map<String, ExternalArtifactSupport> userInputForHeader;
        private Map<String, ExternalArtifactSupport> userInputForDelimiter;
        private Map<String, ExternalArtifactSupport> userInputForQuote;

        private Map<String, Boolean> inputHasHeader;
        private Map<String, Character> delimiter;
        private Map<String, Character> quote;

        public Map<String, ExternalArtifactSupport> getUserInputForHeader() {
            return userInputForHeader;
        }

        public void setUserInputForHeader(Map<String, ExternalArtifactSupport> userInputForHeader) {
            this.userInputForHeader = userInputForHeader;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForDelimiter() {
            return userInputForDelimiter;
        }

        public void setUserInputForDelimiter(Map<String, ExternalArtifactSupport> userInputForDelimiter) {
            this.userInputForDelimiter = userInputForDelimiter;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForQuote() {
            return userInputForQuote;
        }

        public void setUserInputForQuote(Map<String, ExternalArtifactSupport> userInputForQuote) {
            this.userInputForQuote = userInputForQuote;
        }

        public Map<String, Boolean> getInputHasHeader() {
            return inputHasHeader;
        }

        public void setInputHasHeader(Map<String, Boolean> inputHasHeader) {
            this.inputHasHeader = inputHasHeader;
        }

        public Map<String, Character> getDelimiter() {
            return delimiter;
        }

        public void setDelimiter(Map<String, Character> delimiter) {
            this.delimiter = delimiter;
        }

        public Map<String, Character> getQuote() {
            return quote;
        }

        public void setQuote(Map<String, Character> quote) {
            this.quote = quote;
        }
    }

    public static class Label extends LabelConfig {
        private String externalSchemaLabel;
        private String externalSchemaPlaceholder;
        private String csvSyntax;
        private String includeCsvSyntax;
        private String includeCsvSyntaxTooltip;
        private String csvSyntaxQuotePlaceholder;
        private String csvSyntaxQuoteTooltip;
        private String csvSyntaxDelimiterPlaceholder;
        private String csvSyntaxDelimiterTooltip;
        private String csvSyntaxHeaders;
        private String csvSyntaxHeadersTooltip;

        public String getCsvSyntax() {
            return csvSyntax;
        }

        public void setCsvSyntax(String csvSyntax) {
            this.csvSyntax = csvSyntax;
        }

        public String getIncludeCsvSyntax() {
            return includeCsvSyntax;
        }

        public void setIncludeCsvSyntax(String includeCsvSyntax) {
            this.includeCsvSyntax = includeCsvSyntax;
        }

        public String getIncludeCsvSyntaxTooltip() {
            return includeCsvSyntaxTooltip;
        }

        public void setIncludeCsvSyntaxTooltip(String includeCsvSyntaxTooltip) {
            this.includeCsvSyntaxTooltip = includeCsvSyntaxTooltip;
        }

        public String getCsvSyntaxQuotePlaceholder() {
            return csvSyntaxQuotePlaceholder;
        }

        public void setCsvSyntaxQuotePlaceholder(String csvSyntaxQuotePlaceholder) {
            this.csvSyntaxQuotePlaceholder = csvSyntaxQuotePlaceholder;
        }

        public String getCsvSyntaxQuoteTooltip() {
            return csvSyntaxQuoteTooltip;
        }

        public void setCsvSyntaxQuoteTooltip(String csvSyntaxQuoteTooltip) {
            this.csvSyntaxQuoteTooltip = csvSyntaxQuoteTooltip;
        }

        public String getCsvSyntaxDelimiterPlaceholder() {
            return csvSyntaxDelimiterPlaceholder;
        }

        public void setCsvSyntaxDelimiterPlaceholder(String csvSyntaxDelimiterPlaceholder) {
            this.csvSyntaxDelimiterPlaceholder = csvSyntaxDelimiterPlaceholder;
        }

        public String getCsvSyntaxDelimiterTooltip() {
            return csvSyntaxDelimiterTooltip;
        }

        public void setCsvSyntaxDelimiterTooltip(String csvSyntaxDelimiterTooltip) {
            this.csvSyntaxDelimiterTooltip = csvSyntaxDelimiterTooltip;
        }

        public String getCsvSyntaxHeaders() {
            return csvSyntaxHeaders;
        }

        public void setCsvSyntaxHeaders(String csvSyntaxHeaders) {
            this.csvSyntaxHeaders = csvSyntaxHeaders;
        }

        public String getCsvSyntaxHeadersTooltip() {
            return csvSyntaxHeadersTooltip;
        }

        public void setCsvSyntaxHeadersTooltip(String csvSyntaxHeadersTooltip) {
            this.csvSyntaxHeadersTooltip = csvSyntaxHeadersTooltip;
        }

        public String getExternalSchemaLabel() {
            return externalSchemaLabel;
        }

        public void setExternalSchemaLabel(String externalSchemaLabel) {
            this.externalSchemaLabel = externalSchemaLabel;
        }

        public String getExternalSchemaPlaceholder() {
            return externalSchemaPlaceholder;
        }

        public void setExternalSchemaPlaceholder(String externalSchemaPlaceholder) {
            this.externalSchemaPlaceholder = externalSchemaPlaceholder;
        }
    }

}
