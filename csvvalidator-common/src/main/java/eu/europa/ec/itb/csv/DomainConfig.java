package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.config.LabelConfig;
import eu.europa.ec.itb.validation.commons.config.WebDomainConfig;

import java.util.Map;
import java.util.regex.Pattern;

public class DomainConfig extends WebDomainConfig<DomainConfig.Label> {

    private Map<String, Boolean> javaBasedDateFormats;
    private Map<String, Boolean> displayEnumValuesInMessages;
    private Map<String, ParserError> parserErrors;

    private CsvOptions csvOptions = new CsvOptions();

    public CsvOptions getCsvOptions() {
        return csvOptions;
    }

    public void setParserErrors(Map<String, ParserError> parserErrors) {
        this.parserErrors = parserErrors;
    }

    public Map<String, ParserError> getParserErrors() {
        return parserErrors;
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

    public static class ParserError {

        public static final ParserError MISSING_HEADER_NAME = new ParserError(
                "missingHeader",
                "^A header name is missing in \\[.+\\]",
                "The header field names could not be parsed. You have either defined consecutive field delimiter characters (commas) with blank contents or defined a trailing one at the end of the header."
        );
        public static final ParserError WRONG_DELIMITER = new ParserError(
                "wrongDelimiter",
                "^\\(line .+\\) invalid char between encapsulated token and delimiter",
                "The header field names could not be parsed. This is likely due to the field delimiter character (comma) used in the input not being the expected one."
        );

        private Pattern pattern;
        private String message;
        private String name;

        public ParserError(String name, String expression, String message) {
            pattern = Pattern.compile(expression);
            this.message = message;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public String getMessage() {
            return message;
        }
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
        private String differentInputFieldCountViolationLevel;
        private String differentInputFieldSequenceViolationLevel;
        private String duplicateInputFieldsViolationLevel;
        private String fieldCaseMismatchViolationLevel;
        private String multipleInputFieldsForSchemaFieldViolationLevel;
        private String unknownInputViolationLevel;
        private String unspecifiedSchemaFieldViolationLevel;
        private String differentInputFieldCountViolationLevelTooltip;
        private String differentInputFieldSequenceViolationLevelTooltip;
        private String duplicateInputFieldsViolationLevelTooltip;
        private String fieldCaseMismatchViolationLevelTooltip;
        private String multipleInputFieldsForSchemaFieldViolationLevelTooltip;
        private String unknownInputViolationLevelTooltip;
        private String unspecifiedSchemaFieldViolationLevelTooltip;
        private String violationLevelError;
        private String violationLevelWarning;
        private String violationLevelInfo;
        private String violationLevelNone;
        private String violationLevelHeader;

        public String getViolationLevelHeader() {
            return violationLevelHeader;
        }

        public void setViolationLevelHeader(String violationLevelHeader) {
            this.violationLevelHeader = violationLevelHeader;
        }

        public String getDifferentInputFieldCountViolationLevel() {
            return differentInputFieldCountViolationLevel;
        }

        public void setDifferentInputFieldCountViolationLevel(String differentInputFieldCountViolationLevel) {
            this.differentInputFieldCountViolationLevel = differentInputFieldCountViolationLevel;
        }

        public String getDifferentInputFieldSequenceViolationLevel() {
            return differentInputFieldSequenceViolationLevel;
        }

        public void setDifferentInputFieldSequenceViolationLevel(String differentInputFieldSequenceViolationLevel) {
            this.differentInputFieldSequenceViolationLevel = differentInputFieldSequenceViolationLevel;
        }

        public String getDuplicateInputFieldsViolationLevel() {
            return duplicateInputFieldsViolationLevel;
        }

        public void setDuplicateInputFieldsViolationLevel(String duplicateInputFieldsViolationLevel) {
            this.duplicateInputFieldsViolationLevel = duplicateInputFieldsViolationLevel;
        }

        public String getFieldCaseMismatchViolationLevel() {
            return fieldCaseMismatchViolationLevel;
        }

        public void setFieldCaseMismatchViolationLevel(String fieldCaseMismatchViolationLevel) {
            this.fieldCaseMismatchViolationLevel = fieldCaseMismatchViolationLevel;
        }

        public String getMultipleInputFieldsForSchemaFieldViolationLevel() {
            return multipleInputFieldsForSchemaFieldViolationLevel;
        }

        public void setMultipleInputFieldsForSchemaFieldViolationLevel(String multipleInputFieldsForSchemaFieldViolationLevel) {
            this.multipleInputFieldsForSchemaFieldViolationLevel = multipleInputFieldsForSchemaFieldViolationLevel;
        }

        public String getUnknownInputViolationLevel() {
            return unknownInputViolationLevel;
        }

        public void setUnknownInputViolationLevel(String unknownInputViolationLevel) {
            this.unknownInputViolationLevel = unknownInputViolationLevel;
        }

        public String getUnspecifiedSchemaFieldViolationLevel() {
            return unspecifiedSchemaFieldViolationLevel;
        }

        public void setUnspecifiedSchemaFieldViolationLevel(String unspecifiedSchemaFieldViolationLevel) {
            this.unspecifiedSchemaFieldViolationLevel = unspecifiedSchemaFieldViolationLevel;
        }

        public String getDifferentInputFieldCountViolationLevelTooltip() {
            return differentInputFieldCountViolationLevelTooltip;
        }

        public void setDifferentInputFieldCountViolationLevelTooltip(String differentInputFieldCountViolationLevelTooltip) {
            this.differentInputFieldCountViolationLevelTooltip = differentInputFieldCountViolationLevelTooltip;
        }

        public String getDifferentInputFieldSequenceViolationLevelTooltip() {
            return differentInputFieldSequenceViolationLevelTooltip;
        }

        public void setDifferentInputFieldSequenceViolationLevelTooltip(String differentInputFieldSequenceViolationLevelTooltip) {
            this.differentInputFieldSequenceViolationLevelTooltip = differentInputFieldSequenceViolationLevelTooltip;
        }

        public String getDuplicateInputFieldsViolationLevelTooltip() {
            return duplicateInputFieldsViolationLevelTooltip;
        }

        public void setDuplicateInputFieldsViolationLevelTooltip(String duplicateInputFieldsViolationLevelTooltip) {
            this.duplicateInputFieldsViolationLevelTooltip = duplicateInputFieldsViolationLevelTooltip;
        }

        public String getFieldCaseMismatchViolationLevelTooltip() {
            return fieldCaseMismatchViolationLevelTooltip;
        }

        public void setFieldCaseMismatchViolationLevelTooltip(String fieldCaseMismatchViolationLevelTooltip) {
            this.fieldCaseMismatchViolationLevelTooltip = fieldCaseMismatchViolationLevelTooltip;
        }

        public String getMultipleInputFieldsForSchemaFieldViolationLevelTooltip() {
            return multipleInputFieldsForSchemaFieldViolationLevelTooltip;
        }

        public void setMultipleInputFieldsForSchemaFieldViolationLevelTooltip(String multipleInputFieldsForSchemaFieldViolationLevelTooltip) {
            this.multipleInputFieldsForSchemaFieldViolationLevelTooltip = multipleInputFieldsForSchemaFieldViolationLevelTooltip;
        }

        public String getUnknownInputViolationLevelTooltip() {
            return unknownInputViolationLevelTooltip;
        }

        public void setUnknownInputViolationLevelTooltip(String unknownInputViolationLevelTooltip) {
            this.unknownInputViolationLevelTooltip = unknownInputViolationLevelTooltip;
        }

        public String getUnspecifiedSchemaFieldViolationLevelTooltip() {
            return unspecifiedSchemaFieldViolationLevelTooltip;
        }

        public void setUnspecifiedSchemaFieldViolationLevelTooltip(String unspecifiedSchemaFieldViolationLevelTooltip) {
            this.unspecifiedSchemaFieldViolationLevelTooltip = unspecifiedSchemaFieldViolationLevelTooltip;
        }

        public String getViolationLevelError() {
            return violationLevelError;
        }

        public void setViolationLevelError(String violationLevelError) {
            this.violationLevelError = violationLevelError;
        }

        public String getViolationLevelWarning() {
            return violationLevelWarning;
        }

        public void setViolationLevelWarning(String violationLevelWarning) {
            this.violationLevelWarning = violationLevelWarning;
        }

        public String getViolationLevelInfo() {
            return violationLevelInfo;
        }

        public void setViolationLevelInfo(String violationLevelInfo) {
            this.violationLevelInfo = violationLevelInfo;
        }

        public String getViolationLevelNone() {
            return violationLevelNone;
        }

        public void setViolationLevelNone(String violationLevelNone) {
            this.violationLevelNone = violationLevelNone;
        }

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
