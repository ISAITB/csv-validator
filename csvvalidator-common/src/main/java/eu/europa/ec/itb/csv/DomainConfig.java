/*
 * Copyright (C) 2025 European Union
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an
 * "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for
 * the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.TypedValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.config.WebDomainConfig;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class to hold and expose the configuration for a specific validation domain.
 */
public class DomainConfig extends WebDomainConfig {

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
     * Class holding configuration to provide user-friendly error messages for known parser errors.
     *
     * These are errors that are raised from the parsing itself before any validation rules can be executed.
     */
    public static class ParserError {

        /** Header row with missing names. */
        public static final ParserError MISSING_HEADER_NAME = new ParserError(
                "missingHeader",
                "^A header name is missing in \\[.+\\]"
        );
        /** Use of the wrong field delimiter. */
        public static final ParserError WRONG_DELIMITER = new ParserError(
                "wrongDelimiter",
                "^\\(line .+\\) invalid char between encapsulated token and delimiter"
        );

        private final Pattern pattern;
        private final String name;

        /**
         * Constructor.
         *
         * @param name A unique name for this error.
         * @param expression The regular expression to use when determining if a provided error message is a match.
         */
        public ParserError(String name, String expression) {
            pattern = Pattern.compile(expression);
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

}
