package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.config.LabelConfig;
import eu.europa.ec.itb.validation.commons.config.WebDomainConfig;

import java.util.Map;

public class DomainConfig extends WebDomainConfig<DomainConfig.Label> {

    private Map<String, Boolean> javaBasedDateFormats;
    private Map<String, Boolean> displayEnumValuesInMessages;

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

    public Map<String, Boolean> getDisplayEnumValuesInMessages() {
        return displayEnumValuesInMessages;
    }

    public void setDisplayEnumValuesInMessages(Map<String, Boolean> displayEnumValuesInMessages) {
        this.displayEnumValuesInMessages = displayEnumValuesInMessages;
    }

    public Map<String, Boolean> getJavaBasedDateFormats() {
        return javaBasedDateFormats;
    }

    public void setJavaBasedDateFormats(Map<String, Boolean> javaBasedDateFormats) {
        this.javaBasedDateFormats = javaBasedDateFormats;
    }

    public static class CsvOptions {

        private Map<String, Boolean> inputHasHeader;
        private Map<String, Character> delimiter;
        private Map<String, Character> quote;
        private Map<String, ViolationLevel> differentInputFieldCount;
        private Map<String, ViolationLevel> differentInputFieldSequence;
        private Map<String, ViolationLevel> unknownInputField;
        private Map<String, ViolationLevel> unspecifiedSchemaField;
        private Map<String, ViolationLevel> inputFieldCaseMismatch;
        private Map<String, ViolationLevel> duplicateInputFields;
        private Map<String, ViolationLevel> multipleInputFieldsForSchemaField;

        private Map<String, ExternalArtifactSupport> userInputForHeader;
        private Map<String, ExternalArtifactSupport> userInputForDelimiter;
        private Map<String, ExternalArtifactSupport> userInputForQuote;
        private Map<String, ExternalArtifactSupport> userInputForDifferentInputFieldCount;
        private Map<String, ExternalArtifactSupport> userInputForDifferentInputFieldSequence;
        private Map<String, ExternalArtifactSupport> userInputForUnknownInputField;
        private Map<String, ExternalArtifactSupport> userInputForUnspecifiedSchemaField;
        private Map<String, ExternalArtifactSupport> userInputForInputFieldCaseMismatch;
        private Map<String, ExternalArtifactSupport> userInputForDuplicateInputFields;
        private Map<String, ExternalArtifactSupport> userInputForMultipleInputFieldsForSchemaField;

        public Map<String, ViolationLevel> getDuplicateInputFields() {
            return duplicateInputFields;
        }

        public void setDuplicateInputFields(Map<String, ViolationLevel> duplicateInputFields) {
            this.duplicateInputFields = duplicateInputFields;
        }

        public Map<String, ViolationLevel> getMultipleInputFieldsForSchemaField() {
            return multipleInputFieldsForSchemaField;
        }

        public void setMultipleInputFieldsForSchemaField(Map<String, ViolationLevel> multipleInputFieldsForSchemaField) {
            this.multipleInputFieldsForSchemaField = multipleInputFieldsForSchemaField;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForDuplicateInputFields() {
            return userInputForDuplicateInputFields;
        }

        public void setUserInputForDuplicateInputFields(Map<String, ExternalArtifactSupport> userInputForDuplicateInputFields) {
            this.userInputForDuplicateInputFields = userInputForDuplicateInputFields;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForMultipleInputFieldsForSchemaField() {
            return userInputForMultipleInputFieldsForSchemaField;
        }

        public void setUserInputForMultipleInputFieldsForSchemaField(Map<String, ExternalArtifactSupport> userInputForMultipleInputFieldsForSchemaField) {
            this.userInputForMultipleInputFieldsForSchemaField = userInputForMultipleInputFieldsForSchemaField;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForDifferentInputFieldCount() {
            return userInputForDifferentInputFieldCount;
        }

        public void setUserInputForDifferentInputFieldCount(Map<String, ExternalArtifactSupport> userInputForDifferentInputFieldCount) {
            this.userInputForDifferentInputFieldCount = userInputForDifferentInputFieldCount;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForDifferentInputFieldSequence() {
            return userInputForDifferentInputFieldSequence;
        }

        public void setUserInputForDifferentInputFieldSequence(Map<String, ExternalArtifactSupport> userInputForDifferentInputFieldSequence) {
            this.userInputForDifferentInputFieldSequence = userInputForDifferentInputFieldSequence;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForUnknownInputField() {
            return userInputForUnknownInputField;
        }

        public void setUserInputForUnknownInputField(Map<String, ExternalArtifactSupport> userInputForUnknownInputField) {
            this.userInputForUnknownInputField = userInputForUnknownInputField;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForUnspecifiedSchemaField() {
            return userInputForUnspecifiedSchemaField;
        }

        public void setUserInputForUnspecifiedSchemaField(Map<String, ExternalArtifactSupport> userInputForUnspecifiedSchemaField) {
            this.userInputForUnspecifiedSchemaField = userInputForUnspecifiedSchemaField;
        }

        public Map<String, ExternalArtifactSupport> getUserInputForInputFieldCaseMismatch() {
            return userInputForInputFieldCaseMismatch;
        }

        public void setUserInputForInputFieldCaseMismatch(Map<String, ExternalArtifactSupport> userInputForInputFieldCaseMismatch) {
            this.userInputForInputFieldCaseMismatch = userInputForInputFieldCaseMismatch;
        }

        public Map<String, ViolationLevel> getDifferentInputFieldCount() {
            return differentInputFieldCount;
        }

        public void setDifferentInputFieldCount(Map<String, ViolationLevel> differentInputFieldCount) {
            this.differentInputFieldCount = differentInputFieldCount;
        }

        public Map<String, ViolationLevel> getDifferentInputFieldSequence() {
            return differentInputFieldSequence;
        }

        public void setDifferentInputFieldSequence(Map<String, ViolationLevel> differentInputFieldSequence) {
            this.differentInputFieldSequence = differentInputFieldSequence;
        }

        public Map<String, ViolationLevel> getUnknownInputField() {
            return unknownInputField;
        }

        public void setUnknownInputField(Map<String, ViolationLevel> unknownInputField) {
            this.unknownInputField = unknownInputField;
        }

        public Map<String, ViolationLevel> getUnspecifiedSchemaField() {
            return unspecifiedSchemaField;
        }

        public void setUnspecifiedSchemaField(Map<String, ViolationLevel> unspecifiedSchemaField) {
            this.unspecifiedSchemaField = unspecifiedSchemaField;
        }

        public Map<String, ViolationLevel> getInputFieldCaseMismatch() {
            return inputFieldCaseMismatch;
        }

        public void setInputFieldCaseMismatch(Map<String, ViolationLevel> inputFieldCaseMismatch) {
            this.inputFieldCaseMismatch = inputFieldCaseMismatch;
        }

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
