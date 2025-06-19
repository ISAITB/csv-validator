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

package eu.europa.ec.itb.csv.validation;

/**
 * Constants linked to the CSV validator.
 */
public class ValidationConstants {

    /**
     * Constructor to prevent instantiation.
     */
    private ValidationConstants() { throw new IllegalStateException("Utility class"); }

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
    public static final String INPUT_ADD_INPUT_TO_REPORT = "addInputToReport";
    /** The locale string to consider. */
    public static final String INPUT_LOCALE = "locale";

}
