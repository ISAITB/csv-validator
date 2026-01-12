/*
 * Copyright (C) 2026 European Union
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

package eu.europa.ec.itb.csv.validation;

/**
 * Class encapsulating the CSV syntax settings to be considered for a given validation run.
 */
public class CSVSettings {

    /** The default violation level when the count of input fields does not match the schema. */
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_COUNT = ViolationLevel.ERROR;
    /** The default violation level when the sequence of input fields does not match the schema. */
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_SEQUENCE = ViolationLevel.ERROR;
    /** The default violation level when unknown input fields are encountered. */
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__UNKNOWN_INPUT_FIELD = ViolationLevel.ERROR;
    /** The default violation level when a schema field has not been provided for in the input. */
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__UNSPECIFIED_SCHEMA_FIELD = ViolationLevel.ERROR;
    /** The default violation level when the case of a field header in the input does not match the schema. */
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__INPUT_FIELD_CASE_MISMATCH = ViolationLevel.ERROR;
    /** The default violation level when duplicate input fields are detected. */
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__DUPLICATE_INPUT_FIELD = ViolationLevel.ERROR;
    /** The default violation level when multiple input fields map to the same schema field. */
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD = ViolationLevel.ERROR;

    private boolean hasHeaders;
    private char delimiter;
    private char quote;

    private ViolationLevel differentInputFieldCountViolationLevel = DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_COUNT;
    private ViolationLevel differentInputFieldSequenceViolationLevel = DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_SEQUENCE;
    private ViolationLevel unknownInputFieldViolationLevel = DEFAULT_VIOLATION_LEVEL__UNKNOWN_INPUT_FIELD;
    private ViolationLevel unspecifiedSchemaFieldViolationLevel = DEFAULT_VIOLATION_LEVEL__UNSPECIFIED_SCHEMA_FIELD;
    private ViolationLevel inputFieldCaseMismatchViolationLevel = DEFAULT_VIOLATION_LEVEL__INPUT_FIELD_CASE_MISMATCH;
    private ViolationLevel duplicateInputFieldViolationLevel = DEFAULT_VIOLATION_LEVEL__DUPLICATE_INPUT_FIELD;
    private ViolationLevel multipleInputFieldsForSchemaFieldViolationLevel = DEFAULT_VIOLATION_LEVEL__MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD;

    /**
     * Constructor.
     */
    private CSVSettings() {
    }

    /**
     * @return True if a header row is expected as part of the input.
     */
    public boolean isHasHeaders() {
        return hasHeaders;
    }

    /**
     * @param hasHeaders True if a header row is expected as part of the input.
     * @return The current object.
     */
    public CSVSettings setHasHeaders(boolean hasHeaders) {
        this.hasHeaders = hasHeaders;
        return this;
    }

    /**
     * @return The field delimiter to use.
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * @param delimiter The field delimiter to use.
     * @return The current object.
     */
    public CSVSettings setDelimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * @return The quote character to use.
     */
    public char getQuote() {
        return quote;
    }

    /**
     * @param quote The quote character to use.
     * @return The current object.
     */
    public CSVSettings setQuote(char quote) {
        this.quote = quote;
        return this;
    }

    /**
     * @return The violation level when the count of input fields does not match the schema.
     */
    public ViolationLevel getDifferentInputFieldCountViolationLevel() {
        return differentInputFieldCountViolationLevel;
    }

    /**
     * @param differentInputFieldCountViolationLevel The violation level when the count of input fields does not match the schema.
     * @return The current object.
     */
    public CSVSettings setDifferentInputFieldCountViolationLevel(ViolationLevel differentInputFieldCountViolationLevel) {
        this.differentInputFieldCountViolationLevel = differentInputFieldCountViolationLevel;
        return this;
    }

    /**
     * @return The violation level when the sequence of input fields does not match the schema.
     */
    public ViolationLevel getDifferentInputFieldSequenceViolationLevel() {
        return differentInputFieldSequenceViolationLevel;
    }

    /**
     * @param differentInputFieldSequenceViolationLevel The violation level when the sequence of input fields does not match the schema.
     * @return The current object.
     */
    public CSVSettings setDifferentInputFieldSequenceViolationLevel(ViolationLevel differentInputFieldSequenceViolationLevel) {
        this.differentInputFieldSequenceViolationLevel = differentInputFieldSequenceViolationLevel;
        return this;
    }

    /**
     * @return The violation level for unknown input fields.
     */
    public ViolationLevel getUnknownInputFieldViolationLevel() {
        return unknownInputFieldViolationLevel;
    }

    /**
     * @param unknownInputFieldViolationLevel The violation level for unknown input fields.
     * @return The current object.
     */
    public CSVSettings setUnknownInputFieldViolationLevel(ViolationLevel unknownInputFieldViolationLevel) {
        this.unknownInputFieldViolationLevel = unknownInputFieldViolationLevel;
        return this;
    }

    /**
     * @return The violation level for schema fields not provided for in the input.
     */
    public ViolationLevel getUnspecifiedSchemaFieldViolationLevel() {
        return unspecifiedSchemaFieldViolationLevel;
    }

    /**
     * @param unspecifiedSchemaFieldViolationLevel The violation level for schema fields not provided for in the input.
     * @return The current object.
     */
    public CSVSettings setUnspecifiedSchemaFieldViolationLevel(ViolationLevel unspecifiedSchemaFieldViolationLevel) {
        this.unspecifiedSchemaFieldViolationLevel = unspecifiedSchemaFieldViolationLevel;
        return this;
    }

    /**
     * @return The violation level when input field header names do not match the casing in the schema.
     */
    public ViolationLevel getInputFieldCaseMismatchViolationLevel() {
        return inputFieldCaseMismatchViolationLevel;
    }

    /**
     * @param inputFieldCaseMismatchViolationLevel The violation level when input field header names do not match the
     *                                             casing in the schema.
     * @return The current object.
     */
    public CSVSettings setInputFieldCaseMismatchViolationLevel(ViolationLevel inputFieldCaseMismatchViolationLevel) {
        this.inputFieldCaseMismatchViolationLevel = inputFieldCaseMismatchViolationLevel;
        return this;
    }

    /**
     * @return The violation level for duplicate input fields.
     */
    public ViolationLevel getDuplicateInputFieldViolationLevel() {
        return duplicateInputFieldViolationLevel;
    }

    /**
     * @param duplicateInputFieldViolationLevel The violation level for duplicate input fields.
     * @return The current object.
     */
    public CSVSettings setDuplicateInputFieldViolationLevel(ViolationLevel duplicateInputFieldViolationLevel) {
        this.duplicateInputFieldViolationLevel = duplicateInputFieldViolationLevel;
        return this;
    }

    /**
     * @return The violation level when multiple input fields match the same schema field.
     */
    public ViolationLevel getMultipleInputFieldsForSchemaFieldViolationLevel() {
        return multipleInputFieldsForSchemaFieldViolationLevel;
    }

    /**
     * @param multipleInputFieldsForSchemaFieldViolationLevel The violation level when multiple input fields match the
     *                                                        same schema field.
     * @return The current object.
     */
    public CSVSettings setMultipleInputFieldsForSchemaFieldViolationLevel(ViolationLevel multipleInputFieldsForSchemaFieldViolationLevel) {
        this.multipleInputFieldsForSchemaFieldViolationLevel = multipleInputFieldsForSchemaFieldViolationLevel;
        return this;
    }

    /**
     * Build a new settings object.
     *
     * @return The object.
     */
    public static CSVSettings build() {
        return new CSVSettings();
    }

}
