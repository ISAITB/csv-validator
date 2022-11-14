package eu.europa.ec.itb.csv.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.FileContent;
import eu.europa.ec.itb.validation.commons.web.rest.model.SchemaInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * The input to trigger a new validation via the validator's REST API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "The content and metadata specific to input content that is to be validated.")
public class Input {

    @Schema(description = "The content to validate, provided as a normal string, a URL, or a BASE64-encoded string.")
    private String contentToValidate;
    @Schema(description = "The way in which to interpret the contentToValidate. If not provided, the method will be determined from the contentToValidate value.", allowableValues = FileContent.EMBEDDING_STRING+","+FileContent.EMBEDDING_URL+","+FileContent.EMBEDDING_BASE_64)
    private String embeddingMethod;
    @Schema(description = "The type of validation to perform (e.g. the profile to apply or the version to validate against). This can be skipped if a single validation type is supported by the validator. Otherwise, if multiple are supported, the service should fail with an error.")
    private String validationType;
    @Schema(description = "Any user-provided schemas to consider for the validation (i.e. provided at the time of the call).")
    private List<SchemaInfo> externalSchemas;
    @Schema(description = "Whether the CSV content to validate includes a header row.", defaultValue = "true")
    private Boolean hasHeaders;
    @Schema(description = "The field delimiter to consider.", defaultValue = ",")
    private String delimiter;
    @Schema(description = "The field quote character to consider.", defaultValue = "\"")
    private String quote;
    @Schema(description = "The violation level in case the input field count doesn't match the schema field count.", allowableValues = ViolationLevel.ERROR_VALUE+","+ViolationLevel.WARNING_VALUE+","+ViolationLevel.INFO_VALUE+","+ViolationLevel.NONE_VALUE, defaultValue = ViolationLevel.ERROR_VALUE)
    private String differentInputFieldCountViolationLevel;
    @Schema(description = "The violation level in case the input fields' sequence doesn't match the schema fields' sequence.", allowableValues = ViolationLevel.ERROR_VALUE+","+ViolationLevel.WARNING_VALUE+","+ViolationLevel.INFO_VALUE+","+ViolationLevel.NONE_VALUE, defaultValue = ViolationLevel.ERROR_VALUE)
    private String differentInputFieldSequenceViolationLevel;
    @Schema(description = "The violation level in case duplicate input fields are found.", allowableValues = ViolationLevel.ERROR_VALUE+","+ViolationLevel.WARNING_VALUE+","+ViolationLevel.INFO_VALUE+","+ViolationLevel.NONE_VALUE, defaultValue = ViolationLevel.ERROR_VALUE)
    private String duplicateInputFieldsViolationLevel;
    @Schema(description = "The violation level in case input fields match schema fields but with different casing.", allowableValues = ViolationLevel.ERROR_VALUE+","+ViolationLevel.WARNING_VALUE+","+ViolationLevel.INFO_VALUE+","+ViolationLevel.NONE_VALUE, defaultValue = ViolationLevel.ERROR_VALUE)
    private String fieldCaseMismatchViolationLevel;
    @Schema(description = "The violation level in case multiple input fields map to the same schema field.", allowableValues = ViolationLevel.ERROR_VALUE+","+ViolationLevel.WARNING_VALUE+","+ViolationLevel.INFO_VALUE+","+ViolationLevel.NONE_VALUE, defaultValue = ViolationLevel.ERROR_VALUE)
    private String multipleInputFieldsForSchemaFieldViolationLevel;
    @Schema(description = "The violation level in case of unknown input fields.", allowableValues = ViolationLevel.ERROR_VALUE+","+ViolationLevel.WARNING_VALUE+","+ViolationLevel.INFO_VALUE+","+ViolationLevel.NONE_VALUE, defaultValue = ViolationLevel.ERROR_VALUE)
    private String unknownInputFieldViolationLevel;
    @Schema(description = "The violation level in case schema fields are not defined.", allowableValues = ViolationLevel.ERROR_VALUE+","+ViolationLevel.WARNING_VALUE+","+ViolationLevel.INFO_VALUE+","+ViolationLevel.NONE_VALUE, defaultValue = ViolationLevel.ERROR_VALUE)
    private String unspecifiedSchemaField;
    @Schema(description = "Whether to include the validated input in the resulting report's context section.", defaultValue = "false")
    private Boolean addInputToReport;
    @Schema(description = "Locale (language code) to use for reporting of results. If the provided locale is not supported by the validator the default locale will be used instead (e.g. 'fr', 'fr_FR').")
    private String locale;

    /**
     * @return The string representing the content to validate (string as-is, URL or base64 content).
     */
    public String getContentToValidate() { return this.contentToValidate; }

    /**
     * @return The embedding method to consider to determine how the provided content input is to be processed.
     */
    public String getEmbeddingMethod() { return this.embeddingMethod; }

    /**
     * @return The validation type to trigger for this domain.
     */
    public String getValidationType() { return this.validationType; }

    /**
     * @param contentToValidate The string representing the content to validate (string as-is, URL or base64 content).
     */
    public void setContentToValidate(String contentToValidate) {
        this.contentToValidate = contentToValidate;
    }

    /**
     * @param embeddingMethod  The embedding method to consider to determine how the provided content input is to be processed.
     */
    public void setEmbeddingMethod(String embeddingMethod) {
        this.embeddingMethod = embeddingMethod;
    }

    /**
     * @param validationType The validation type to trigger for this domain.
     */
    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    /**
     * @return The set of user-provided schemas.
     */
    public List<SchemaInfo> getExternalSchemas() {
        return externalSchemas;
    }

    /**
     * @param externalSchemas The set of user-provided schemas.
     */
    public void setExternalSchemas(List<SchemaInfo> externalSchemas) {
        this.externalSchemas = externalSchemas;
    }

    /**
     * @return Whether to include the validated input in the resulting report's context section.
     */
    public Boolean getAddInputToReport() {
        return addInputToReport;
    }

    /**
     * @param addInputToReport Whether to include the validated input in the resulting report's context section.
     */
    public void setAddInputToReport(Boolean addInputToReport) {
        this.addInputToReport = addInputToReport;
    }

    /**
     * @return The locale string.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param locale The locale string to set.
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return Whether the provided input contains as its first line the header definitions.
     */
    public Boolean getHasHeaders() {
        return hasHeaders;
    }

    /**
     * @param hasHeaders Whether the provided input contains as its first line the header definitions.
     */
    public void setHasHeaders(Boolean hasHeaders) {
        this.hasHeaders = hasHeaders;
    }

    /**
     * @return The character to be used as the field delimiter.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * @param delimiter The character to be used as the field delimiter.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * @return The character to be used as the quote character.
     */
    public String getQuote() {
        return quote;
    }

    /**
     * @param quote The character to be used as the quote character.
     */
    public void setQuote(String quote) {
        this.quote = quote;
    }

    /**
     * @return The violation level in case the input field count doesn't match the schema field count.
     */
    public String getDifferentInputFieldCountViolationLevel() {
        return differentInputFieldCountViolationLevel;
    }

    /**
     * @param differentInputFieldCountViolationLevel The violation level in case the input field count doesn't match the schema field count.
     */
    public void setDifferentInputFieldCountViolationLevel(String differentInputFieldCountViolationLevel) {
        this.differentInputFieldCountViolationLevel = differentInputFieldCountViolationLevel;
    }

    /**
     * @return The violation level in case the input fields' sequence doesn't match the schema fields' sequence.
     */
    public String getDifferentInputFieldSequenceViolationLevel() {
        return differentInputFieldSequenceViolationLevel;
    }

    /**
     * @param differentInputFieldSequenceViolationLevel The violation level in case the input fields' sequence doesn't match the schema fields' sequence
     */
    public void setDifferentInputFieldSequenceViolationLevel(String differentInputFieldSequenceViolationLevel) {
        this.differentInputFieldSequenceViolationLevel = differentInputFieldSequenceViolationLevel;
    }

    /**
     * @return The violation level in case duplicate input fields are found.
     */
    public String getDuplicateInputFieldsViolationLevel() {
        return duplicateInputFieldsViolationLevel;
    }

    /**
     * @param duplicateInputFieldsViolationLevel The violation level in case duplicate input fields are found.
     */
    public void setDuplicateInputFieldsViolationLevel(String duplicateInputFieldsViolationLevel) {
        this.duplicateInputFieldsViolationLevel = duplicateInputFieldsViolationLevel;
    }

    /**
     * @return The violation level in case input fields match schema fields but with different casing.
     */
    public String getFieldCaseMismatchViolationLevel() {
        return fieldCaseMismatchViolationLevel;
    }

    /**
     * @param fieldCaseMismatchViolationLevel The violation level in case input fields match schema fields but with different casing.
     */
    public void setFieldCaseMismatchViolationLevel(String fieldCaseMismatchViolationLevel) {
        this.fieldCaseMismatchViolationLevel = fieldCaseMismatchViolationLevel;
    }

    /**
     * @return The violation level in case multiple input fields map to the same schema field.
     */
    public String getMultipleInputFieldsForSchemaFieldViolationLevel() {
        return multipleInputFieldsForSchemaFieldViolationLevel;
    }

    /**
     * @param multipleInputFieldsForSchemaFieldViolationLevel The violation level in case multiple input fields map to the same schema field.
     */
    public void setMultipleInputFieldsForSchemaFieldViolationLevel(String multipleInputFieldsForSchemaFieldViolationLevel) {
        this.multipleInputFieldsForSchemaFieldViolationLevel = multipleInputFieldsForSchemaFieldViolationLevel;
    }

    /**
     * @return The violation level in case of unknown input fields.
     */
    public String getUnknownInputFieldViolationLevel() {
        return unknownInputFieldViolationLevel;
    }

    /**
     * @param unknownInputFieldViolationLevel The violation level in case of unknown input fields.
     */
    public void setUnknownInputFieldViolationLevel(String unknownInputFieldViolationLevel) {
        this.unknownInputFieldViolationLevel = unknownInputFieldViolationLevel;
    }

    /**
     * @return The violation level in case schema fields are not defined.
     */
    public String getUnspecifiedSchemaField() {
        return unspecifiedSchemaField;
    }

    /**
     * @param unspecifiedSchemaField The violation level in case schema fields are not defined.
     */
    public void setUnspecifiedSchemaField(String unspecifiedSchemaField) {
        this.unspecifiedSchemaField = unspecifiedSchemaField;
    }
}
