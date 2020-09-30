package eu.europa.ec.itb.csv.validation;

public class CSVSettings {

    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_COUNT = ViolationLevel.ERROR;
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_SEQUENCE = ViolationLevel.ERROR;
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__UNKNOWN_INPUT_FIELD = ViolationLevel.ERROR;
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__UNSPECIFIED_SCHEMA_FIELD = ViolationLevel.ERROR;
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__INPUT_FIELD_CASE_MISMATCH = ViolationLevel.ERROR;
    public static final ViolationLevel DEFAULT_VIOLATION_LEVEL__DUPLICATE_INPUT_FIELD = ViolationLevel.ERROR;
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

    private CSVSettings() {
    }

    public boolean isHasHeaders() {
        return hasHeaders;
    }

    public CSVSettings setHasHeaders(boolean hasHeaders) {
        this.hasHeaders = hasHeaders;
        return this;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public CSVSettings setDelimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public char getQuote() {
        return quote;
    }

    public CSVSettings setQuote(char quote) {
        this.quote = quote;
        return this;
    }

    public ViolationLevel getDifferentInputFieldCountViolationLevel() {
        return differentInputFieldCountViolationLevel;
    }

    public CSVSettings setDifferentInputFieldCountViolationLevel(ViolationLevel differentInputFieldCountViolationLevel) {
        this.differentInputFieldCountViolationLevel = differentInputFieldCountViolationLevel;
        return this;
    }

    public ViolationLevel getDifferentInputFieldSequenceViolationLevel() {
        return differentInputFieldSequenceViolationLevel;
    }

    public CSVSettings setDifferentInputFieldSequenceViolationLevel(ViolationLevel differentInputFieldSequenceViolationLevel) {
        this.differentInputFieldSequenceViolationLevel = differentInputFieldSequenceViolationLevel;
        return this;
    }

    public ViolationLevel getUnknownInputFieldViolationLevel() {
        return unknownInputFieldViolationLevel;
    }

    public CSVSettings setUnknownInputFieldViolationLevel(ViolationLevel unknownInputFieldViolationLevel) {
        this.unknownInputFieldViolationLevel = unknownInputFieldViolationLevel;
        return this;
    }

    public ViolationLevel getUnspecifiedSchemaFieldViolationLevel() {
        return unspecifiedSchemaFieldViolationLevel;
    }

    public CSVSettings setUnspecifiedSchemaFieldViolationLevel(ViolationLevel unspecifiedSchemaFieldViolationLevel) {
        this.unspecifiedSchemaFieldViolationLevel = unspecifiedSchemaFieldViolationLevel;
        return this;
    }

    public ViolationLevel getInputFieldCaseMismatchViolationLevel() {
        return inputFieldCaseMismatchViolationLevel;
    }

    public CSVSettings setInputFieldCaseMismatchViolationLevel(ViolationLevel inputFieldCaseMismatchViolationLevel) {
        this.inputFieldCaseMismatchViolationLevel = inputFieldCaseMismatchViolationLevel;
        return this;
    }

    public ViolationLevel getDuplicateInputFieldViolationLevel() {
        return duplicateInputFieldViolationLevel;
    }

    public CSVSettings setDuplicateInputFieldViolationLevel(ViolationLevel duplicateInputFieldViolationLevel) {
        this.duplicateInputFieldViolationLevel = duplicateInputFieldViolationLevel;
        return this;
    }

    public ViolationLevel getMultipleInputFieldsForSchemaFieldViolationLevel() {
        return multipleInputFieldsForSchemaFieldViolationLevel;
    }

    public CSVSettings setMultipleInputFieldsForSchemaFieldViolationLevel(ViolationLevel multipleInputFieldsForSchemaFieldViolationLevel) {
        this.multipleInputFieldsForSchemaFieldViolationLevel = multipleInputFieldsForSchemaFieldViolationLevel;
        return this;
    }

    public static CSVSettings build() {
        return new CSVSettings();
    }

}
