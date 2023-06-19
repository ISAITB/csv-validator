package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.ValidationConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Application-level configuration properties.
 */
@Component
@ConfigurationProperties("validator")
public class ApplicationConfig extends eu.europa.ec.itb.validation.commons.config.ApplicationConfig {

    private Set<String> acceptedSchemaExtensions;
    private final Map<String, String> defaultLabels = new HashMap<>();
    private Set<String> acceptedMimeTypes;
    private String defaultContentToValidateDescription;
    private String defaultEmbeddingMethodDescription;
    private String defaultValidationTypeDescription;
    private String defaultExternalSchemaDescription;
    private String defaultInputHasHeadersDescription;
    private String defaultInputDelimiterDescription;
    private String defaultInputQuoteDescription;
    private String defaultDifferentInputFieldCountViolationLevelDescription;
    private String defaultDifferentInputFieldSequenceViolationLevelDescription;
    private String defaultDuplicateInputFieldsViolationLevelDescription;
    private String defaultFieldCaseMismatchViolationLevelDescription;
    private String defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription;
    private String defaultUnknownInputFieldViolationLevelDescription;
    private String defaultUnspecifiedSchemaFieldDescription;
    private String defaultAddInputToReportDescription;
    private String defaultLocaleDescription;

    /**
     * @return The default web service input description for the violation level linked to different field counts.
     */
    public String getDefaultDifferentInputFieldCountViolationLevelDescription() {
        return defaultDifferentInputFieldCountViolationLevelDescription;
    }

    /**
     * @param defaultDifferentInputFieldCountViolationLevelDescription  The default web service input description for the violation level linked to different field counts.
     */
    public void setDefaultDifferentInputFieldCountViolationLevelDescription(String defaultDifferentInputFieldCountViolationLevelDescription) {
        this.defaultDifferentInputFieldCountViolationLevelDescription = defaultDifferentInputFieldCountViolationLevelDescription;
    }

    /**
     * @return The default web service input description for the violation level linked to different field sequences.
     */
    public String getDefaultDifferentInputFieldSequenceViolationLevelDescription() {
        return defaultDifferentInputFieldSequenceViolationLevelDescription;
    }

    /**
     * @param defaultDifferentInputFieldSequenceViolationLevelDescription  The default web service input description for the violation level linked to different field sequences.
     */
    public void setDefaultDifferentInputFieldSequenceViolationLevelDescription(String defaultDifferentInputFieldSequenceViolationLevelDescription) {
        this.defaultDifferentInputFieldSequenceViolationLevelDescription = defaultDifferentInputFieldSequenceViolationLevelDescription;
    }

    /**
     * @return The default web service input description for the violation level linked to duplicate fields.
     */
    public String getDefaultDuplicateInputFieldsViolationLevelDescription() {
        return defaultDuplicateInputFieldsViolationLevelDescription;
    }

    /**
     * @param defaultDuplicateInputFieldsViolationLevelDescription The default web service input description for the violation level linked to duplicate fields.
     */
    public void setDefaultDuplicateInputFieldsViolationLevelDescription(String defaultDuplicateInputFieldsViolationLevelDescription) {
        this.defaultDuplicateInputFieldsViolationLevelDescription = defaultDuplicateInputFieldsViolationLevelDescription;
    }

    /**
     * @return The default web service input description for the violation level linked to field name case mismatches.
     */
    public String getDefaultFieldCaseMismatchViolationLevelDescription() {
        return defaultFieldCaseMismatchViolationLevelDescription;
    }

    /**
     * @param defaultFieldCaseMismatchViolationLevelDescription  The default web service input description for the violation level linked to field name case mismatches.
     */
    public void setDefaultFieldCaseMismatchViolationLevelDescription(String defaultFieldCaseMismatchViolationLevelDescription) {
        this.defaultFieldCaseMismatchViolationLevelDescription = defaultFieldCaseMismatchViolationLevelDescription;
    }

    /**
     * @return The default web service input description for the violation level linked to having multiple inputs fields map to the same schema field.
     */
    public String getDefaultMultipleInputFieldsForSchemaFieldViolationLevelDescription() {
        return defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription;
    }

    /**
     * @param defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription  The default web service input description for the violation level linked to having multiple inputs fields map to the same schema field.
     */
    public void setDefaultMultipleInputFieldsForSchemaFieldViolationLevelDescription(String defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription) {
        this.defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription = defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription;
    }

    /**
     * @return The default web service input description for the violation level linked to having unknown input fields.
     */
    public String getDefaultUnknownInputFieldViolationLevelDescription() {
        return defaultUnknownInputFieldViolationLevelDescription;
    }

    /**
     * @param defaultUnknownInputFieldViolationLevelDescription  The default web service input description for the violation level linked to having unknown input fields.
     */
    public void setDefaultUnknownInputFieldViolationLevelDescription(String defaultUnknownInputFieldViolationLevelDescription) {
        this.defaultUnknownInputFieldViolationLevelDescription = defaultUnknownInputFieldViolationLevelDescription;
    }

    /**
     * @return The default web service input description for the violation level linked to having unspecified schema fields.
     */
    public String getDefaultUnspecifiedSchemaFieldDescription() {
        return defaultUnspecifiedSchemaFieldDescription;
    }

    /**
     * @param defaultUnspecifiedSchemaFieldDescription  The default web service input description for the violation level linked to having unspecified schema fields.
     */
    public void setDefaultUnspecifiedSchemaFieldDescription(String defaultUnspecifiedSchemaFieldDescription) {
        this.defaultUnspecifiedSchemaFieldDescription = defaultUnspecifiedSchemaFieldDescription;
    }

    /**
     * @return The default web service input description for the choice on whether the input contains a header line.
     */
    public String getDefaultInputHasHeadersDescription() {
        return defaultInputHasHeadersDescription;
    }

    /**
     * @param defaultInputHasHeadersDescription  The default web service input description for the choice on whether the input contains a header line.
     */
    public void setDefaultInputHasHeadersDescription(String defaultInputHasHeadersDescription) {
        this.defaultInputHasHeadersDescription = defaultInputHasHeadersDescription;
    }

    /**
     * @return The default web service input description for the delimiter character.
     */
    public String getDefaultInputDelimiterDescription() {
        return defaultInputDelimiterDescription;
    }

    /**
     * @param defaultInputDelimiterDescription  The default web service input description for the delimiter character.
     */
    public void setDefaultInputDelimiterDescription(String defaultInputDelimiterDescription) {
        this.defaultInputDelimiterDescription = defaultInputDelimiterDescription;
    }

    /**
     * @return The default web service input description for the quote character.
     */
    public String getDefaultInputQuoteDescription() {
        return defaultInputQuoteDescription;
    }

    /**
     * @param defaultInputQuoteDescription  The default web service input description for the quote character.
     */
    public void setDefaultInputQuoteDescription(String defaultInputQuoteDescription) {
        this.defaultInputQuoteDescription = defaultInputQuoteDescription;
    }

    /**
     * @return The default web service input description for the validation type to perform.
     */
    public String getDefaultValidationTypeDescription() {
        return defaultValidationTypeDescription;
    }

    /**
     * @param defaultValidationTypeDescription  The default web service input description for the validation type to perform.
     */
    public void setDefaultValidationTypeDescription(String defaultValidationTypeDescription) {
        this.defaultValidationTypeDescription = defaultValidationTypeDescription;
    }

    /**
     * @return The set of accepted file extension for schemas when looking up in a filesystem folder.
     */
    public Set<String> getAcceptedSchemaExtensions() {
        return acceptedSchemaExtensions;
    }

    /**
     * @param acceptedSchemaExtensions  The set of accepted file extension for schemas when looking up in a filesystem folder.
     */
    public void setAcceptedSchemaExtensions(Set<String> acceptedSchemaExtensions) {
        this.acceptedSchemaExtensions = acceptedSchemaExtensions;
    }

    /**
     * @return The default web service input description labels.
     */
    public Map<String, String> getDefaultLabels() {
        return defaultLabels;
    }

    /**
     * @return The accepted mime types for provided input files.
     */
    public Set<String> getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }

    /**
     * @param acceptedMimeTypes  The accepted mime types for provided input files.
     */
    public void setAcceptedMimeTypes(Set<String> acceptedMimeTypes) {
        this.acceptedMimeTypes = acceptedMimeTypes;
    }

    /**
     * @return The default web service input description for the content to validate.
     */
    public String getDefaultContentToValidateDescription() {
        return defaultContentToValidateDescription;
    }

    /**
     * @param defaultContentToValidateDescription  The default web service input description for the content to validate.
     */
    public void setDefaultContentToValidateDescription(String defaultContentToValidateDescription) {
        this.defaultContentToValidateDescription = defaultContentToValidateDescription;
    }

    /**
     * @return The default web service input description for the content's embedding method.
     */
    public String getDefaultEmbeddingMethodDescription() {
        return defaultEmbeddingMethodDescription;
    }

    /**
     * @param defaultEmbeddingMethodDescription  The default web service input description for the content's embedding method.
     */
    public void setDefaultEmbeddingMethodDescription(String defaultEmbeddingMethodDescription) {
        this.defaultEmbeddingMethodDescription = defaultEmbeddingMethodDescription;
    }

    /**
     * @return The default web service input description for the user-provided schemas.
     */
    public String getDefaultExternalSchemaDescription() {
        return defaultExternalSchemaDescription;
    }

    /**
     * @param defaultExternalSchemaDescription  The default web service input description for the user-provided schemas.
     */
    public void setDefaultExternalSchemaDescription(String defaultExternalSchemaDescription) {
        this.defaultExternalSchemaDescription = defaultExternalSchemaDescription;
    }

    /**
     * @return The default web service input description for the locale to use.
     */
    public String getDefaultLocaleDescription() {
        return defaultLocaleDescription;
    }

    /**
     * @param defaultLocaleDescription The default web service input description for the locale to use.
     */
    public void setDefaultLocaleDescription(String defaultLocaleDescription) {
        this.defaultLocaleDescription = defaultLocaleDescription;
    }

    /**
     * @return The default web service input description for the add input to report option.
     */
    public String getDefaultAddInputToReportDescription() {
        return defaultAddInputToReportDescription;
    }

    /**
     * @param defaultAddInputToReportDescription The default web service input description for the add input to report option.
     */
    public void setDefaultAddInputToReportDescription(String defaultAddInputToReportDescription) {
        this.defaultAddInputToReportDescription = defaultAddInputToReportDescription;
    }

    /**
     * Initialisation method.
     *
     * @see eu.europa.ec.itb.validation.commons.config.ApplicationConfig#init()
     */
    @Override
    @PostConstruct
    public void init() {
        super.init();
        setSupportsAdditionalInformationInReportItems(false);
        setSupportsTestDefinitionInReportItems(false);
        // Default labels.
        defaultLabels.put(ValidationConstants.INPUT_CONTENT, defaultContentToValidateDescription);
        defaultLabels.put(ValidationConstants.INPUT_EMBEDDING_METHOD, defaultEmbeddingMethodDescription);
        defaultLabels.put(ValidationConstants.INPUT_VALIDATION_TYPE, defaultValidationTypeDescription);
        defaultLabels.put(ValidationConstants.INPUT_EXTERNAL_SCHEMA, defaultExternalSchemaDescription);
        defaultLabels.put(ValidationConstants.INPUT_HAS_HEADERS, defaultInputHasHeadersDescription);
        defaultLabels.put(ValidationConstants.INPUT_DELIMITER, defaultInputDelimiterDescription);
        defaultLabels.put(ValidationConstants.INPUT_QUOTE, defaultInputQuoteDescription);
        defaultLabels.put(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL, defaultDifferentInputFieldCountViolationLevelDescription);
        defaultLabels.put(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL, defaultDifferentInputFieldSequenceViolationLevelDescription);
        defaultLabels.put(ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL, defaultDuplicateInputFieldsViolationLevelDescription);
        defaultLabels.put(ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL, defaultFieldCaseMismatchViolationLevelDescription);
        defaultLabels.put(ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL, defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription);
        defaultLabels.put(ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL, defaultUnknownInputFieldViolationLevelDescription);
        defaultLabels.put(ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL, defaultUnspecifiedSchemaFieldDescription);
        defaultLabels.put(ValidationConstants.INPUT_ADD_INPUT_TO_REPORT, defaultAddInputToReportDescription);
        defaultLabels.put(ValidationConstants.INPUT_LOCALE, defaultLocaleDescription);
    }
}
