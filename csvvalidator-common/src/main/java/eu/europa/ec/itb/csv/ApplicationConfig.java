package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.ValidationConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public String getDefaultDifferentInputFieldCountViolationLevelDescription() {
        return defaultDifferentInputFieldCountViolationLevelDescription;
    }

    public void setDefaultDifferentInputFieldCountViolationLevelDescription(String defaultDifferentInputFieldCountViolationLevelDescription) {
        this.defaultDifferentInputFieldCountViolationLevelDescription = defaultDifferentInputFieldCountViolationLevelDescription;
    }

    public String getDefaultDifferentInputFieldSequenceViolationLevelDescription() {
        return defaultDifferentInputFieldSequenceViolationLevelDescription;
    }

    public void setDefaultDifferentInputFieldSequenceViolationLevelDescription(String defaultDifferentInputFieldSequenceViolationLevelDescription) {
        this.defaultDifferentInputFieldSequenceViolationLevelDescription = defaultDifferentInputFieldSequenceViolationLevelDescription;
    }

    public String getDefaultDuplicateInputFieldsViolationLevelDescription() {
        return defaultDuplicateInputFieldsViolationLevelDescription;
    }

    public void setDefaultDuplicateInputFieldsViolationLevelDescription(String defaultDuplicateInputFieldsViolationLevelDescription) {
        this.defaultDuplicateInputFieldsViolationLevelDescription = defaultDuplicateInputFieldsViolationLevelDescription;
    }

    public String getDefaultFieldCaseMismatchViolationLevelDescription() {
        return defaultFieldCaseMismatchViolationLevelDescription;
    }

    public void setDefaultFieldCaseMismatchViolationLevelDescription(String defaultFieldCaseMismatchViolationLevelDescription) {
        this.defaultFieldCaseMismatchViolationLevelDescription = defaultFieldCaseMismatchViolationLevelDescription;
    }

    public String getDefaultMultipleInputFieldsForSchemaFieldViolationLevelDescription() {
        return defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription;
    }

    public void setDefaultMultipleInputFieldsForSchemaFieldViolationLevelDescription(String defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription) {
        this.defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription = defaultMultipleInputFieldsForSchemaFieldViolationLevelDescription;
    }

    public String getDefaultUnknownInputFieldViolationLevelDescription() {
        return defaultUnknownInputFieldViolationLevelDescription;
    }

    public void setDefaultUnknownInputFieldViolationLevelDescription(String defaultUnknownInputFieldViolationLevelDescription) {
        this.defaultUnknownInputFieldViolationLevelDescription = defaultUnknownInputFieldViolationLevelDescription;
    }

    public String getDefaultUnspecifiedSchemaFieldDescription() {
        return defaultUnspecifiedSchemaFieldDescription;
    }

    public void setDefaultUnspecifiedSchemaFieldDescription(String defaultUnspecifiedSchemaFieldDescription) {
        this.defaultUnspecifiedSchemaFieldDescription = defaultUnspecifiedSchemaFieldDescription;
    }

    public String getDefaultInputHasHeadersDescription() {
        return defaultInputHasHeadersDescription;
    }

    public void setDefaultInputHasHeadersDescription(String defaultInputHasHeadersDescription) {
        this.defaultInputHasHeadersDescription = defaultInputHasHeadersDescription;
    }

    public String getDefaultInputDelimiterDescription() {
        return defaultInputDelimiterDescription;
    }

    public void setDefaultInputDelimiterDescription(String defaultInputDelimiterDescription) {
        this.defaultInputDelimiterDescription = defaultInputDelimiterDescription;
    }

    public String getDefaultInputQuoteDescription() {
        return defaultInputQuoteDescription;
    }

    public void setDefaultInputQuoteDescription(String defaultInputQuoteDescription) {
        this.defaultInputQuoteDescription = defaultInputQuoteDescription;
    }

    public String getDefaultValidationTypeDescription() {
        return defaultValidationTypeDescription;
    }

    public void setDefaultValidationTypeDescription(String defaultValidationTypeDescription) {
        this.defaultValidationTypeDescription = defaultValidationTypeDescription;
    }

    public Set<String> getAcceptedSchemaExtensions() {
        return acceptedSchemaExtensions;
    }

    public void setAcceptedSchemaExtensions(Set<String> acceptedSchemaExtensions) {
        this.acceptedSchemaExtensions = acceptedSchemaExtensions;
    }

    public Map<String, String> getDefaultLabels() {
        return defaultLabels;
    }

    public Set<String> getAcceptedMimeTypes() {
        return acceptedMimeTypes;
    }

    public void setAcceptedMimeTypes(Set<String> acceptedMimeTypes) {
        this.acceptedMimeTypes = acceptedMimeTypes;
    }

    public String getDefaultContentToValidateDescription() {
        return defaultContentToValidateDescription;
    }

    public void setDefaultContentToValidateDescription(String defaultContentToValidateDescription) {
        this.defaultContentToValidateDescription = defaultContentToValidateDescription;
    }

    public String getDefaultEmbeddingMethodDescription() {
        return defaultEmbeddingMethodDescription;
    }

    public void setDefaultEmbeddingMethodDescription(String defaultEmbeddingMethodDescription) {
        this.defaultEmbeddingMethodDescription = defaultEmbeddingMethodDescription;
    }

    public String getDefaultExternalSchemaDescription() {
        return defaultExternalSchemaDescription;
    }

    public void setDefaultExternalSchemaDescription(String defaultExternalSchemaDescription) {
        this.defaultExternalSchemaDescription = defaultExternalSchemaDescription;
    }

    @PostConstruct
    public void init() {
        super.init();
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
    }
}
