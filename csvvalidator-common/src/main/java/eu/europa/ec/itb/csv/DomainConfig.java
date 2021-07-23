package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.TypedValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.config.LabelConfig;
import eu.europa.ec.itb.validation.commons.config.WebDomainConfig;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class to hold and expose the configuration for a specific validation domain.
 */
public class DomainConfig extends WebDomainConfig<DomainConfig.Label> implements MessageFormatter {

    private Map<String, Boolean> javaBasedDateFormats;
    private Map<String, Boolean> displayEnumValuesInMessages;
    private Map<String, ParserError> parserErrors;

    private final CsvOptions csvOptions = new CsvOptions();

    /**
     * @return The configuration for CSV syntax options.
     */
    public CsvOptions getCsvOptions() {
        return csvOptions;
    }

    /**
     * @param parserErrors The map of known parsing errors to provide meaningful error messages.
     */
    public void setParserErrors(Map<String, ParserError> parserErrors) {
        this.parserErrors = parserErrors;
    }

    /**
     * @return The map of known parsing errors to provide meaningful error messages.
     */
    public Map<String, ParserError> getParserErrors() {
        return parserErrors;
    }

    /**
     * Check to see if this domain defines validation types that allow or expect CSV syntax settings to be provided as inputs.
     *
     * @param inputSupportMap The map of validation type to support level.
     * @return The check result.
     */
    public boolean definesTypesWithSettingInputs(Map<String, ExternalArtifactSupport> inputSupportMap) {
        for (ExternalArtifactSupport support: inputSupportMap.values()) {
            if (support != ExternalArtifactSupport.NONE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if this domain defines validation types that support or require user-provided schemas.
     *
     * @return True if user-provided schemas are supported or required.
     */
    public boolean definesTypeWithExternalSchemas() {
        for (TypedValidationArtifactInfo info : getArtifactInfo().values()) {
            if (info.get().getExternalArtifactSupport() != ExternalArtifactSupport.NONE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initialise a new and empty label configuration object.
     *
     * @return The label configuration instance.
     */
    @Override
    protected Label newLabelConfig() {
        return new Label();
    }

    /**
     * Get the schema configuration information for a given validation type.
     *
     * @param validationType The validation type.
     * @return The configuration information.
     */
    public ValidationArtifactInfo getSchemaInfo(String validationType) {
        return getArtifactInfo().get(validationType).get();
    }

    /**
     * Get the map of validation type to whether expected values in schema fields should be printed as-is.
     *
     * @return The map.
     */
    public Map<String, Boolean> getDisplayEnumValuesInMessages() {
        return displayEnumValuesInMessages;
    }

    /**
     * Set the map of validation type to whether expected values in schema fields should be printed as-is.
     *
     * @param displayEnumValuesInMessages The map.
     */
    public void setDisplayEnumValuesInMessages(Map<String, Boolean> displayEnumValuesInMessages) {
        this.displayEnumValuesInMessages = displayEnumValuesInMessages;
    }

    /**
     * Get the map of validation type to whether or not date formats should be java-based (versus python-based).
     *
     * @return The map.
     */
    public Map<String, Boolean> getJavaBasedDateFormats() {
        return javaBasedDateFormats;
    }

    /**
     * Set the map of validation type to whether or not date formats should be java-based (versus python-based).
     *
     * @param javaBasedDateFormats The map.
     */
    public void setJavaBasedDateFormats(Map<String, Boolean> javaBasedDateFormats) {
        this.javaBasedDateFormats = javaBasedDateFormats;
    }

    /**
     * Format a error message for inclusion in the validation report.
     *
     * @param lineNumber The line number to include as the location.
     * @param fieldName The relevant field name (null is no header fields are defined).
     * @param message The message.
     * @return The formatted message.
     */
    @Override
    public String formatMessage(long lineNumber, String fieldName, String message) {
        if (fieldName == null) {
            return String.format("[%s%s]: %s", getLabel().getLineMessagePrefix(), lineNumber, message);
        } else {
            return String.format("[%s%s][%s%s]: %s", getLabel().getLineMessagePrefix(), lineNumber, getLabel().getFieldMessagePrefix(), fieldName, message);
        }
    }

    /**
     * Class holding configuration to provide user-friendly error messages for known parser errors.
     *
     * These are errors that are raised from the parsing itself before any validation rules can be executed.
     */
    public static class ParserError {

        /** Header row with missing names. */
        public static final ParserError MISSING_HEADER_NAME = new ParserError(
                "missingHeader",
                "^A header name is missing in \\[.+\\]",
                "The header field names could not be parsed. You have either defined consecutive field delimiter characters (commas) with blank contents or defined a trailing one at the end of the header."
        );
        /** Use of the wrong field delimiter. */
        public static final ParserError WRONG_DELIMITER = new ParserError(
                "wrongDelimiter",
                "^\\(line .+\\) invalid char between encapsulated token and delimiter",
                "The header field names could not be parsed. This is likely due to the field delimiter character (comma) used in the input not being the expected one."
        );

        private final Pattern pattern;
        private final String message;
        private final String name;

        /**
         * Constructor.
         *
         * @param name A unique name for this error.
         * @param expression The regular expression to use when determining if a provided error message is a match.
         * @param message The user-friendly message to display.
         */
        public ParserError(String name, String expression, String message) {
            pattern = Pattern.compile(expression);
            this.message = message;
            this.name = name;
        }

        /**
         * @return The unique name for this error.
         */
        public String getName() {
            return name;
        }

        /**
         * @return The regular expression to use when determining if a provided error message is a match.
         */
        public Pattern getPattern() {
            return pattern;
        }

        /**
         * @return The user-friendly message to display.
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Class holding the configuration options linked to CSV syntax settings.
     */
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

        /**
         * @return The mapping from validation type to the violation level for duplicate input fields.
         */
        public Map<String, ViolationLevel> getDuplicateInputFields() {
            return duplicateInputFields;
        }

        /**
         * @param duplicateInputFields The mapping from validation type to the violation level for duplicate input fields.
         */
        public void setDuplicateInputFields(Map<String, ViolationLevel> duplicateInputFields) {
            this.duplicateInputFields = duplicateInputFields;
        }

        /**
         * @return The mapping from validation type to the violation level for having multiple input fields map to the
         * same schema field.
         */
        public Map<String, ViolationLevel> getMultipleInputFieldsForSchemaField() {
            return multipleInputFieldsForSchemaField;
        }

        /**
         * @param multipleInputFieldsForSchemaField The mapping from validation type to the violation level for having
         *                                          multiple input fields map to the same schema field.
         */
        public void setMultipleInputFieldsForSchemaField(Map<String, ViolationLevel> multipleInputFieldsForSchemaField) {
            this.multipleInputFieldsForSchemaField = multipleInputFieldsForSchemaField;
        }

        /**
         * @return The mapping from validation type to the level of support for the input specifying the violation level
         * for duplicate inputs.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForDuplicateInputFields() {
            return userInputForDuplicateInputFields;
        }

        /**
         * @param userInputForDuplicateInputFields The mapping from validation type to the level of support for the input
         *                                         specifying the violation level for duplicate inputs.
         */
        public void setUserInputForDuplicateInputFields(Map<String, ExternalArtifactSupport> userInputForDuplicateInputFields) {
            this.userInputForDuplicateInputFields = userInputForDuplicateInputFields;
        }

        /**
         * @return The mapping from validation type to the level of support for the input specifying the violation level
         * when multiple input fields map to the same schema field.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForMultipleInputFieldsForSchemaField() {
            return userInputForMultipleInputFieldsForSchemaField;
        }

        /**
         * @param userInputForMultipleInputFieldsForSchemaField  The mapping from validation type to the level of support
         *                                                       for the input specifying the violation level when multiple
         *                                                       input fields map to the same schema field.
         */
        public void setUserInputForMultipleInputFieldsForSchemaField(Map<String, ExternalArtifactSupport> userInputForMultipleInputFieldsForSchemaField) {
            this.userInputForMultipleInputFieldsForSchemaField = userInputForMultipleInputFieldsForSchemaField;
        }

        /**
         * @return The mapping from validation type to the level of support for the input specifying the violation level
         * when the number of input fields doesn't match the number of fields in the schema.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForDifferentInputFieldCount() {
            return userInputForDifferentInputFieldCount;
        }

        /**
         * @param userInputForDifferentInputFieldCount The mapping from validation type to the level of support for the
         *                                             input specifying the violation level when the number of input fields
         *                                             doesn't match the number of fields in the schema.
         */
        public void setUserInputForDifferentInputFieldCount(Map<String, ExternalArtifactSupport> userInputForDifferentInputFieldCount) {
            this.userInputForDifferentInputFieldCount = userInputForDifferentInputFieldCount;
        }

        /**
         * @return The mapping from validation type to the level of support for the input specifying the violation level
         * when the input field sequence does not match that of the schema.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForDifferentInputFieldSequence() {
            return userInputForDifferentInputFieldSequence;
        }

        /**
         * @param userInputForDifferentInputFieldSequence The mapping from validation type to the level of support for the
         *                                                input specifying the violation level when the input field sequence
         *                                                does not match that of the schema.
         */
        public void setUserInputForDifferentInputFieldSequence(Map<String, ExternalArtifactSupport> userInputForDifferentInputFieldSequence) {
            this.userInputForDifferentInputFieldSequence = userInputForDifferentInputFieldSequence;
        }

        /**
         * @return The mapping from validation type to the level of support for the input specifying the violation level
         * when the an input field is unknown.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForUnknownInputField() {
            return userInputForUnknownInputField;
        }

        /**
         * @param userInputForUnknownInputField The mapping from validation type to the level of support for the input
         *                                      specifying the violation level when the an input field is unknown.
         */
        public void setUserInputForUnknownInputField(Map<String, ExternalArtifactSupport> userInputForUnknownInputField) {
            this.userInputForUnknownInputField = userInputForUnknownInputField;
        }

        /**
         * @return The mapping from validation type to the level of support for the input specifying the violation level
         * when a schema field is not mapped in the input.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForUnspecifiedSchemaField() {
            return userInputForUnspecifiedSchemaField;
        }

        /**
         * @param userInputForUnspecifiedSchemaField The mapping from validation type to the level of support for the
         *                                           input specifying the violation level when a schema field is not
         *                                           mapped in the input.
         */
        public void setUserInputForUnspecifiedSchemaField(Map<String, ExternalArtifactSupport> userInputForUnspecifiedSchemaField) {
            this.userInputForUnspecifiedSchemaField = userInputForUnspecifiedSchemaField;
        }

        /**
         * @return The mapping from validation type to the level of support for the input specifying the violation level
         * when the case of the input's header fields does not match that of the schema.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForInputFieldCaseMismatch() {
            return userInputForInputFieldCaseMismatch;
        }

        /**
         * @param userInputForInputFieldCaseMismatch The mapping from validation type to the level of support for the
         *                                           input specifying the violation level when the case of the input's
         *                                           header fields does not match that of the schema.
         */
        public void setUserInputForInputFieldCaseMismatch(Map<String, ExternalArtifactSupport> userInputForInputFieldCaseMismatch) {
            this.userInputForInputFieldCaseMismatch = userInputForInputFieldCaseMismatch;
        }

        /**
         * @return The mapping from validation type to the violation level for having a different number of input fields
         * compared to headers.
         */
        public Map<String, ViolationLevel> getDifferentInputFieldCount() {
            return differentInputFieldCount;
        }

        /**
         * @param differentInputFieldCount The mapping from validation type to the violation level for having a different
         *                                 number of input fields compared to headers.
         */
        public void setDifferentInputFieldCount(Map<String, ViolationLevel> differentInputFieldCount) {
            this.differentInputFieldCount = differentInputFieldCount;
        }

        /**
         * @return The mapping from validation type to the violation level for having a different sequence of fields compared
         * to the schema.
         */
        public Map<String, ViolationLevel> getDifferentInputFieldSequence() {
            return differentInputFieldSequence;
        }

        /**
         * @param differentInputFieldSequence The mapping from validation type to the violation level for having a different
         *                                    sequence of fields compared to the schema.
         */
        public void setDifferentInputFieldSequence(Map<String, ViolationLevel> differentInputFieldSequence) {
            this.differentInputFieldSequence = differentInputFieldSequence;
        }

        /**
         * @return The mapping from validation type to the violation level for having an unknown input.
         */
        public Map<String, ViolationLevel> getUnknownInputField() {
            return unknownInputField;
        }

        /**
         * @param unknownInputField The mapping from validation type to the violation level for having an unknown input.
         */
        public void setUnknownInputField(Map<String, ViolationLevel> unknownInputField) {
            this.unknownInputField = unknownInputField;
        }

        /**
         * @return The mapping from validation type to the violation level for having a schema field not specified in the
         * input.
         */
        public Map<String, ViolationLevel> getUnspecifiedSchemaField() {
            return unspecifiedSchemaField;
        }

        /**
         * @param unspecifiedSchemaField The mapping from validation type to the violation level for having a schema field
         *                               not specified in the input.
         */
        public void setUnspecifiedSchemaField(Map<String, ViolationLevel> unspecifiedSchemaField) {
            this.unspecifiedSchemaField = unspecifiedSchemaField;
        }

        /**
         * @return The mapping from validation type to the violation level for having the case of input field headers not
         * matching the schema.
         */
        public Map<String, ViolationLevel> getInputFieldCaseMismatch() {
            return inputFieldCaseMismatch;
        }

        /**
         * @param inputFieldCaseMismatch The mapping from validation type to the violation level for having the case of
         *                               input field headers not matching the schema.
         */
        public void setInputFieldCaseMismatch(Map<String, ViolationLevel> inputFieldCaseMismatch) {
            this.inputFieldCaseMismatch = inputFieldCaseMismatch;
        }

        /**
         * @return The mapping from validation type to the level of support for the input of the choice on whether the
         * input includes a header or not.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForHeader() {
            return userInputForHeader;
        }

        /**
         * @param userInputForHeader The mapping from validation type to the level of support for the input of the choice
         *                           on whether the input includes a header or not.
         */
        public void setUserInputForHeader(Map<String, ExternalArtifactSupport> userInputForHeader) {
            this.userInputForHeader = userInputForHeader;
        }

        /**
         * @return The mapping from validation type to the level of support for the input of the delimiter character.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForDelimiter() {
            return userInputForDelimiter;
        }

        /**
         * @param userInputForDelimiter The mapping from validation type to the level of support for the input of the
         *                              delimiter character.
         */
        public void setUserInputForDelimiter(Map<String, ExternalArtifactSupport> userInputForDelimiter) {
            this.userInputForDelimiter = userInputForDelimiter;
        }

        /**
         * @return The mapping from validation type to the level of support for the input of the quote character.
         */
        public Map<String, ExternalArtifactSupport> getUserInputForQuote() {
            return userInputForQuote;
        }

        /**
         * @param userInputForQuote The mapping from validation type to the level of support for the input of the quote
         *                          character.
         */
        public void setUserInputForQuote(Map<String, ExternalArtifactSupport> userInputForQuote) {
            this.userInputForQuote = userInputForQuote;
        }

        /**
         * @return The mapping from validation type to the default setting on whether inputs are expected to include a
         * header row.
         */
        public Map<String, Boolean> getInputHasHeader() {
            return inputHasHeader;
        }

        /**
         * @param inputHasHeader The mapping from validation type to the default setting on whether inputs are expected
         *                       to include a header row.
         */
        public void setInputHasHeader(Map<String, Boolean> inputHasHeader) {
            this.inputHasHeader = inputHasHeader;
        }

        /**
         * @return The mapping from validation type to the default delimiter character.
         */
        public Map<String, Character> getDelimiter() {
            return delimiter;
        }

        /**
         * @param delimiter The mapping from validation type to the default delimiter character.
         */
        public void setDelimiter(Map<String, Character> delimiter) {
            this.delimiter = delimiter;
        }

        /**
         * @return The mapping from validation type to the default quote character.
         */
        public Map<String, Character> getQuote() {
            return quote;
        }

        /**
         * @param quote The mapping from validation type to the default quote character.
         */
        public void setQuote(Map<String, Character> quote) {
            this.quote = quote;
        }
    }

    /**
     * Class holding the configuration for user interface labels.
     */
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
        private String lineMessagePrefix;
        private String fieldMessagePrefix;

        /**
         * @return The prefix for the line to include in report messages.
         */
        public String getLineMessagePrefix() {
            return lineMessagePrefix;
        }

        /**
         * @param lineMessagePrefix The prefix for the line to include in report messages.
         */
        public void setLineMessagePrefix(String lineMessagePrefix) {
            this.lineMessagePrefix = lineMessagePrefix;
        }

        /**
         * @return The prefix for the field to include in report messages.
         */
        public String getFieldMessagePrefix() {
            return fieldMessagePrefix;
        }

        /**
         * @param fieldMessagePrefix The prefix for the field to include in report messages.
         */
        public void setFieldMessagePrefix(String fieldMessagePrefix) {
            this.fieldMessagePrefix = fieldMessagePrefix;
        }

        /**
         * @return The header for the section configuring violation levels.
         */
        public String getViolationLevelHeader() {
            return violationLevelHeader;
        }

        /**
         * @param violationLevelHeader The header for the section configuring violation levels.
         */
        public void setViolationLevelHeader(String violationLevelHeader) {
            this.violationLevelHeader = violationLevelHeader;
        }

        /**
         * @return The label for the violation level of different input field counts.
         */
        public String getDifferentInputFieldCountViolationLevel() {
            return differentInputFieldCountViolationLevel;
        }

        /**
         * @param differentInputFieldCountViolationLevel The label for the violation level of different input field counts.
         */
        public void setDifferentInputFieldCountViolationLevel(String differentInputFieldCountViolationLevel) {
            this.differentInputFieldCountViolationLevel = differentInputFieldCountViolationLevel;
        }

        /**
         * @return The label for the violation level of different input field sequences.
         */
        public String getDifferentInputFieldSequenceViolationLevel() {
            return differentInputFieldSequenceViolationLevel;
        }

        /**
         * @param differentInputFieldSequenceViolationLevel The label for the violation level of different input field sequences.
         */
        public void setDifferentInputFieldSequenceViolationLevel(String differentInputFieldSequenceViolationLevel) {
            this.differentInputFieldSequenceViolationLevel = differentInputFieldSequenceViolationLevel;
        }

        /**
         * @return The label for the violation level of duplicate input fields.
         */
        public String getDuplicateInputFieldsViolationLevel() {
            return duplicateInputFieldsViolationLevel;
        }

        /**
         * @param duplicateInputFieldsViolationLevel The label for the violation level of duplicate input fields.
         */
        public void setDuplicateInputFieldsViolationLevel(String duplicateInputFieldsViolationLevel) {
            this.duplicateInputFieldsViolationLevel = duplicateInputFieldsViolationLevel;
        }

        /**
         * @return The label for the violation level of field name case mismatches.
         */
        public String getFieldCaseMismatchViolationLevel() {
            return fieldCaseMismatchViolationLevel;
        }

        /**
         * @param fieldCaseMismatchViolationLevel  The label for the violation level of field name case mismatches.
         */
        public void setFieldCaseMismatchViolationLevel(String fieldCaseMismatchViolationLevel) {
            this.fieldCaseMismatchViolationLevel = fieldCaseMismatchViolationLevel;
        }

        /**
         * @return The label for the violation level of multiple input fields for a single schema field.
         */
        public String getMultipleInputFieldsForSchemaFieldViolationLevel() {
            return multipleInputFieldsForSchemaFieldViolationLevel;
        }

        /**
         * @param multipleInputFieldsForSchemaFieldViolationLevel The label for the violation level of multiple input fields for a single schema field.
         */
        public void setMultipleInputFieldsForSchemaFieldViolationLevel(String multipleInputFieldsForSchemaFieldViolationLevel) {
            this.multipleInputFieldsForSchemaFieldViolationLevel = multipleInputFieldsForSchemaFieldViolationLevel;
        }

        /**
         * @return The label for the violation level of unknown inputs.
         */
        public String getUnknownInputViolationLevel() {
            return unknownInputViolationLevel;
        }

        /**
         * @param unknownInputViolationLevel The label for the violation level of unknown inputs.
         */
        public void setUnknownInputViolationLevel(String unknownInputViolationLevel) {
            this.unknownInputViolationLevel = unknownInputViolationLevel;
        }

        /**
         * @return The label for the violation level of non-covered schema fields.
         */
        public String getUnspecifiedSchemaFieldViolationLevel() {
            return unspecifiedSchemaFieldViolationLevel;
        }

        /**
         * @param unspecifiedSchemaFieldViolationLevel The label for the violation level of non-covered schema fields.
         */
        public void setUnspecifiedSchemaFieldViolationLevel(String unspecifiedSchemaFieldViolationLevel) {
            this.unspecifiedSchemaFieldViolationLevel = unspecifiedSchemaFieldViolationLevel;
        }

        /**
         * @return The tooltip for the violation level of different input field counts.
         */
        public String getDifferentInputFieldCountViolationLevelTooltip() {
            return differentInputFieldCountViolationLevelTooltip;
        }

        /**
         * @param differentInputFieldCountViolationLevelTooltip The tooltip for violation level of different input field
         *                                                      counts.
         */
        public void setDifferentInputFieldCountViolationLevelTooltip(String differentInputFieldCountViolationLevelTooltip) {
            this.differentInputFieldCountViolationLevelTooltip = differentInputFieldCountViolationLevelTooltip;
        }

        /**
         * @return The tooltip for the violation level of different input field sequences.
         */
        public String getDifferentInputFieldSequenceViolationLevelTooltip() {
            return differentInputFieldSequenceViolationLevelTooltip;
        }

        /**
         * @param differentInputFieldSequenceViolationLevelTooltip The tooltip for the violation level of different input
         *                                                         field sequences.
         */
        public void setDifferentInputFieldSequenceViolationLevelTooltip(String differentInputFieldSequenceViolationLevelTooltip) {
            this.differentInputFieldSequenceViolationLevelTooltip = differentInputFieldSequenceViolationLevelTooltip;
        }

        /**
         * @return The tooltip for the violation level of duplicate input fields.
         */
        public String getDuplicateInputFieldsViolationLevelTooltip() {
            return duplicateInputFieldsViolationLevelTooltip;
        }

        /**
         * @param duplicateInputFieldsViolationLevelTooltip The tooltip for the violation level of duplicate input fields.
         */
        public void setDuplicateInputFieldsViolationLevelTooltip(String duplicateInputFieldsViolationLevelTooltip) {
            this.duplicateInputFieldsViolationLevelTooltip = duplicateInputFieldsViolationLevelTooltip;
        }

        /**
         * @return The tooltip for the violation level of field name case mismatches.
         */
        public String getFieldCaseMismatchViolationLevelTooltip() {
            return fieldCaseMismatchViolationLevelTooltip;
        }

        /**
         * @param fieldCaseMismatchViolationLevelTooltip The tooltip for the violation level of field name case mismatches.
         */
        public void setFieldCaseMismatchViolationLevelTooltip(String fieldCaseMismatchViolationLevelTooltip) {
            this.fieldCaseMismatchViolationLevelTooltip = fieldCaseMismatchViolationLevelTooltip;
        }

        /**
         * @return The tooltip for the violation level of multiple input fields for a single schema field.
         */
        public String getMultipleInputFieldsForSchemaFieldViolationLevelTooltip() {
            return multipleInputFieldsForSchemaFieldViolationLevelTooltip;
        }

        /**
         * @param multipleInputFieldsForSchemaFieldViolationLevelTooltip The tooltip for the violation level of multiple
         *                                                               input fields for a single schema field.
         */
        public void setMultipleInputFieldsForSchemaFieldViolationLevelTooltip(String multipleInputFieldsForSchemaFieldViolationLevelTooltip) {
            this.multipleInputFieldsForSchemaFieldViolationLevelTooltip = multipleInputFieldsForSchemaFieldViolationLevelTooltip;
        }

        /**
         * @return The tooltip for the violation level of unknown inputs.
         */
        public String getUnknownInputViolationLevelTooltip() {
            return unknownInputViolationLevelTooltip;
        }

        /**
         * @param unknownInputViolationLevelTooltip The tooltip for the violation level of unknown inputs.
         */
        public void setUnknownInputViolationLevelTooltip(String unknownInputViolationLevelTooltip) {
            this.unknownInputViolationLevelTooltip = unknownInputViolationLevelTooltip;
        }

        /**
         * @return The tooltip for the violation level of non-covered schema fields.
         */
        public String getUnspecifiedSchemaFieldViolationLevelTooltip() {
            return unspecifiedSchemaFieldViolationLevelTooltip;
        }

        /**
         * @param unspecifiedSchemaFieldViolationLevelTooltip The tooltip for the violation level of non-covered schema
         *                                                    fields.
         */
        public void setUnspecifiedSchemaFieldViolationLevelTooltip(String unspecifiedSchemaFieldViolationLevelTooltip) {
            this.unspecifiedSchemaFieldViolationLevelTooltip = unspecifiedSchemaFieldViolationLevelTooltip;
        }

        /**
         * @return The text for the error violation level.
         */
        public String getViolationLevelError() {
            return violationLevelError;
        }

        /**
         * @param violationLevelError  The text for the error violation level.
         */
        public void setViolationLevelError(String violationLevelError) {
            this.violationLevelError = violationLevelError;
        }

        /**
         * @return The text for the warning violation level.
         */
        public String getViolationLevelWarning() {
            return violationLevelWarning;
        }

        /**
         * @param violationLevelWarning The text for the warning violation level.
         */
        public void setViolationLevelWarning(String violationLevelWarning) {
            this.violationLevelWarning = violationLevelWarning;
        }

        /**
         * @return The text for the info violation level.
         */
        public String getViolationLevelInfo() {
            return violationLevelInfo;
        }

        /**
         * @param violationLevelInfo The text for the info violation level.
         */
        public void setViolationLevelInfo(String violationLevelInfo) {
            this.violationLevelInfo = violationLevelInfo;
        }

        /**
         * @return The text for the "none" violation level.
         */
        public String getViolationLevelNone() {
            return violationLevelNone;
        }

        /**
         * @param violationLevelNone The text for the "none" violation level.
         */
        public void setViolationLevelNone(String violationLevelNone) {
            this.violationLevelNone = violationLevelNone;
        }

        /**
         * @return The CSV syntax label.
         */
        public String getCsvSyntax() {
            return csvSyntax;
        }

        /**
         * @param csvSyntax The CSV syntax label.
         */
        public void setCsvSyntax(String csvSyntax) {
            this.csvSyntax = csvSyntax;
        }

        /**
         * @return The include CSV syntax label.
         */
        public String getIncludeCsvSyntax() {
            return includeCsvSyntax;
        }

        /**
         * @param includeCsvSyntax The include CSV syntax label.
         */
        public void setIncludeCsvSyntax(String includeCsvSyntax) {
            this.includeCsvSyntax = includeCsvSyntax;
        }

        /**
         * @return The include CSV syntax tooltip.
         */
        public String getIncludeCsvSyntaxTooltip() {
            return includeCsvSyntaxTooltip;
        }

        /**
         * @param includeCsvSyntaxTooltip The include CSV syntax tooltip.
         */
        public void setIncludeCsvSyntaxTooltip(String includeCsvSyntaxTooltip) {
            this.includeCsvSyntaxTooltip = includeCsvSyntaxTooltip;
        }

        /**
         * @return The placeholder for the quote input.
         */
        public String getCsvSyntaxQuotePlaceholder() {
            return csvSyntaxQuotePlaceholder;
        }

        /**
         * @param csvSyntaxQuotePlaceholder The placeholder for the quote input.
         */
        public void setCsvSyntaxQuotePlaceholder(String csvSyntaxQuotePlaceholder) {
            this.csvSyntaxQuotePlaceholder = csvSyntaxQuotePlaceholder;
        }

        /**
         * @return The tooltip for the quote input.
         */
        public String getCsvSyntaxQuoteTooltip() {
            return csvSyntaxQuoteTooltip;
        }

        /**
         * @param csvSyntaxQuoteTooltip The tooltip for the quote input.
         */
        public void setCsvSyntaxQuoteTooltip(String csvSyntaxQuoteTooltip) {
            this.csvSyntaxQuoteTooltip = csvSyntaxQuoteTooltip;
        }

        /**
         * @return The placeholder for the delimiter input.
         */
        public String getCsvSyntaxDelimiterPlaceholder() {
            return csvSyntaxDelimiterPlaceholder;
        }

        /**
         * @param csvSyntaxDelimiterPlaceholder The placeholder for the delimiter input.
         */
        public void setCsvSyntaxDelimiterPlaceholder(String csvSyntaxDelimiterPlaceholder) {
            this.csvSyntaxDelimiterPlaceholder = csvSyntaxDelimiterPlaceholder;
        }

        /**
         * @return The tooltip for the delimiter input.
         */
        public String getCsvSyntaxDelimiterTooltip() {
            return csvSyntaxDelimiterTooltip;
        }

        /**
         * @param csvSyntaxDelimiterTooltip The tooltip for the delimiter input.
         */
        public void setCsvSyntaxDelimiterTooltip(String csvSyntaxDelimiterTooltip) {
            this.csvSyntaxDelimiterTooltip = csvSyntaxDelimiterTooltip;
        }

        /**
         * @return The headers' choice text.
         */
        public String getCsvSyntaxHeaders() {
            return csvSyntaxHeaders;
        }

        /**
         * @param csvSyntaxHeaders The headers' choice text.
         */
        public void setCsvSyntaxHeaders(String csvSyntaxHeaders) {
            this.csvSyntaxHeaders = csvSyntaxHeaders;
        }

        /**
         * @return The headers' choice tooltip.
         */
        public String getCsvSyntaxHeadersTooltip() {
            return csvSyntaxHeadersTooltip;
        }

        /**
         * @param csvSyntaxHeadersTooltip The headers' choice tooltip.
         */
        public void setCsvSyntaxHeadersTooltip(String csvSyntaxHeadersTooltip) {
            this.csvSyntaxHeadersTooltip = csvSyntaxHeadersTooltip;
        }

        /**
         * @return The label for user-provided schemas.
         */
        public String getExternalSchemaLabel() {
            return externalSchemaLabel;
        }

        /**
         * @param externalSchemaLabel The label for user-provided schemas.
         */
        public void setExternalSchemaLabel(String externalSchemaLabel) {
            this.externalSchemaLabel = externalSchemaLabel;
        }

        /**
         * @return The placeholder for user-provided schemas inputs.
         */
        public String getExternalSchemaPlaceholder() {
            return externalSchemaPlaceholder;
        }

        /**
         * @param externalSchemaPlaceholder The placeholder for user-provided schemas inputs.
         */
        public void setExternalSchemaPlaceholder(String externalSchemaPlaceholder) {
            this.externalSchemaPlaceholder = externalSchemaPlaceholder;
        }
    }

}
