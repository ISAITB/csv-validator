package eu.europa.ec.itb.csv.validation;

/**
 * Constants linked to the CSV validator.
 */
public class ValidationConstants {

    /** The name of the web service input for the content to validate. */
    public static final String INPUT_CONTENT = "contentToValidate";
    /** The name of the web service input for the content's explicit embedding method. */
    public static final String INPUT_EMBEDDING_METHOD = "embeddingMethod";
    /** The name of the web service input for the requested validation type. */
    public static final String INPUT_VALIDATION_TYPE = "validationType";
    /** The name of the web service input for a user-provided schema. */
    public static final String INPUT_EXTERNAL_SCHEMA = "schema";
    /** The name of the web service input for a user-provided schema's content. */
    public static final String INPUT_EXTERNAL_SCHEMA_CONTENT = "content";
    /** The name of the web service input for the flag specifying if the input contains a header row. */
    public static final String INPUT_HAS_HEADERS = "hasHeaders";
    /** The name of the web service input for the delimiter character. */
    public static final String INPUT_DELIMITER = "delimiter";
    /** The name of the web service input for the quote character. */
    public static final String INPUT_QUOTE = "quote";
    /** The name of the web service input for the violation level on different field counts between the input and the schema. */
    public static final String INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL = "differentInputFieldCountViolationLevel";
    /** The name of the web service input for the violation level on a different field sequence between the input and the schema. */
    public static final String INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL = "differentInputFieldSequenceViolationLevel";
    /** The name of the web service input for the violation level for duplicate inputs. */
    public static final String INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL = "duplicateInputFieldsViolationLevel";
    /** The name of the web service input for the violation level for case mismatches in field names between input and schema. */
    public static final String INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL = "fieldCaseMismatchViolationLevel";
    /** The name of the web service input for the violation level when multiple input fields map to the same schema field. */
    public static final String INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL = "multipleInputFieldsForSchemaFieldViolationLevel";
    /** The name of the web service input for the violation level for unknown input fields. */
    public static final String INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL = "unknownInputFieldViolationLevel";
    /** The name of the web service input for the violation level for schema fields that are not covered by the input. */
    public static final String INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL = "unspecifiedSchemaField";
    /** Whether the validated content should be added to the TAR report. */
    public static String INPUT_ADD_INPUT_TO_REPORT = "addInputToReport";

}
