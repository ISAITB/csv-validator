package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.CSVSettings;
import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.ValidatorChannel;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.config.WebDomainConfigCache;
import org.apache.commons.configuration2.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Reads, stores and shares the configuration properties for the validator's domains.
 */
@Component
public class DomainConfigCache extends WebDomainConfigCache<DomainConfig> {

    @Autowired
    private ApplicationConfig appConfig = null;

    /**
     * Create a new and empty domain configuration object.
     *
     * @return The object.
     */
    @Override
    protected DomainConfig newDomainConfig() {
        return new DomainConfig();
    }

    /**
     * Get the channels supported by this validator.
     *
     * @return The channels.
     */
    @Override
    protected ValidatorChannel[] getSupportedChannels() {
        return new ValidatorChannel[] {ValidatorChannel.FORM, ValidatorChannel.SOAP_API};
    }

    /**
     * Initialisation method.
     *
     * @see eu.europa.ec.itb.validation.commons.config.DomainConfigCache#init()
     */
    @PostConstruct
    public void init() {
        super.init();
    }

    /**
     * Add to the provided domain configuration object the properties specific to the CSV validator.
     *
     * @param domainConfig The domain configuration to enrich.
     * @param config The configuration properties.
     */
    @Override
    protected void addDomainConfiguration(DomainConfig domainConfig, Configuration config) {
        super.addDomainConfiguration(domainConfig, config);
        addValidationArtifactInfo("validator.schemaFile", "validator.externalSchema", null, domainConfig, config);
        domainConfig.setJavaBasedDateFormats(parseBooleanMap("validator.javaBasedDateFormats", config, domainConfig.getType(), false));
        domainConfig.setDisplayEnumValuesInMessages(parseBooleanMap("validator.displayEnumValuesInMessages", config, domainConfig.getType(), true));
        // CSV options.
        domainConfig.getCsvOptions().setInputHasHeader(parseBooleanMap("validator.hasHeaders", config, domainConfig.getType(), true));
        domainConfig.getCsvOptions().setDelimiter(parseCharacterMap("validator.delimiter", config, domainConfig.getType(), ','));
        domainConfig.getCsvOptions().setQuote(parseCharacterMap("validator.quote", config, domainConfig.getType(), '"'));
        domainConfig.getCsvOptions().setDifferentInputFieldCount(parseEnumMap("validator.differentInputFieldCountViolation", CSVSettings.DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_COUNT, config, domainConfig.getType(), ViolationLevel::byName));
        domainConfig.getCsvOptions().setDifferentInputFieldSequence(parseEnumMap("validator.differentInputFieldSequenceViolation", CSVSettings.DEFAULT_VIOLATION_LEVEL__DIFFERENT_INPUT_FIELD_SEQUENCE, config, domainConfig.getType(), ViolationLevel::byName));
        domainConfig.getCsvOptions().setUnknownInputField(parseEnumMap("validator.unknownInputFieldViolation", CSVSettings.DEFAULT_VIOLATION_LEVEL__UNKNOWN_INPUT_FIELD, config, domainConfig.getType(), ViolationLevel::byName));
        domainConfig.getCsvOptions().setUnspecifiedSchemaField(parseEnumMap("validator.unspecifiedSchemaFieldViolation", CSVSettings.DEFAULT_VIOLATION_LEVEL__UNSPECIFIED_SCHEMA_FIELD, config, domainConfig.getType(), ViolationLevel::byName));
        domainConfig.getCsvOptions().setInputFieldCaseMismatch(parseEnumMap("validator.inputFieldCaseMismatchViolation", CSVSettings.DEFAULT_VIOLATION_LEVEL__INPUT_FIELD_CASE_MISMATCH, config, domainConfig.getType(), ViolationLevel::byName));
        domainConfig.getCsvOptions().setDuplicateInputFields(parseEnumMap("validator.duplicateInputFieldsViolation", CSVSettings.DEFAULT_VIOLATION_LEVEL__DUPLICATE_INPUT_FIELD, config, domainConfig.getType(), ViolationLevel::byName));
        domainConfig.getCsvOptions().setMultipleInputFieldsForSchemaField(parseEnumMap("validator.multipleInputFieldsForSchemaFieldViolation", CSVSettings.DEFAULT_VIOLATION_LEVEL__MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD, config, domainConfig.getType(), ViolationLevel::byName));
        // CSV option input settings.
        domainConfig.getCsvOptions().setUserInputForHeader(parseEnumMap("validator.input.hasHeaders", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForDelimiter(parseEnumMap("validator.input.delimiter", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForQuote(parseEnumMap("validator.input.quote", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForDifferentInputFieldCount(parseEnumMap("validator.input.differentInputFieldCountViolation", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForDifferentInputFieldSequence(parseEnumMap("validator.input.differentInputFieldSequenceViolation", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForUnknownInputField(parseEnumMap("validator.input.unknownInputFieldViolation", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForUnspecifiedSchemaField(parseEnumMap("validator.input.unspecifiedSchemaFieldViolation", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForInputFieldCaseMismatch(parseEnumMap("validator.input.inputFieldCaseMismatchViolation", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForDuplicateInputFields(parseEnumMap("validator.input.duplicateInputFieldsViolation", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForMultipleInputFieldsForSchemaField(parseEnumMap("validator.input.multipleInputFieldsForSchemaFieldViolation", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        // Labels.
        domainConfig.getLabel().setPopupTitle(config.getString("validator.label.popupTitle", "CSV content"));
        domainConfig.getLabel().setExternalSchemaLabel(config.getString("validator.label.externalSchemaLabel", "Table Schema"));
        domainConfig.getLabel().setExternalSchemaPlaceholder(config.getString("validator.label.externalSchemaPlaceholder", "Select file..."));
        domainConfig.getLabel().setExternalSchemaLabel(config.getString("validator.label.externalSchemaLabel", "Table Schema"));
        domainConfig.getLabel().setIncludeExternalArtefacts(config.getString("validator.label.includeExternalArtefacts", "Include external schemas"));
        domainConfig.getLabel().setExternalArtefactsTooltip(config.getString("validator.label.externalArtefactsTooltip", "Additional schemas that will be considered for the validation"));
        domainConfig.getLabel().setCsvSyntax(config.getString("validator.label.csvSyntax", "CSV syntax"));
        domainConfig.getLabel().setIncludeCsvSyntax(config.getString("validator.label.includeCsvSyntax", "Specify CSV syntax settings"));
        domainConfig.getLabel().setIncludeCsvSyntaxTooltip(config.getString("validator.label.includeCsvSyntaxTooltip", "Define specific settings for the CSV syntax"));
        domainConfig.getLabel().setCsvSyntaxQuotePlaceholder(config.getString("validator.label.csvSyntaxQuotePlaceholder", "Quote"));
        domainConfig.getLabel().setCsvSyntaxQuoteTooltip(config.getString("validator.label.csvSyntaxQuoteTooltip", "The quote character to consider when wrapping field values"));
        domainConfig.getLabel().setCsvSyntaxDelimiterPlaceholder(config.getString("validator.label.csvSyntaxDelimiterPlaceholder", "Delimiter"));
        domainConfig.getLabel().setCsvSyntaxDelimiterTooltip(config.getString("validator.label.csvSyntaxDelimiterTooltip", "The delimiter character used to separate field values"));
        domainConfig.getLabel().setCsvSyntaxHeaders(config.getString("validator.label.csvSyntaxHeaders", "Content has header"));
        domainConfig.getLabel().setCsvSyntaxHeadersTooltip(config.getString("validator.label.csvSyntaxHeadersTooltip", "Whether or not the first line of the content defines the field headers"));
        domainConfig.getLabel().setLineMessagePrefix(config.getString("validator.label.lineMessagePrefix", "Row:").trim());
        if (domainConfig.getLabel().getLineMessagePrefix().length() > 0) {
            domainConfig.getLabel().setLineMessagePrefix(domainConfig.getLabel().getLineMessagePrefix().concat(" "));
        }
        domainConfig.getLabel().setFieldMessagePrefix(config.getString("validator.label.fieldMessagePrefix", "Field:").trim());
        if (domainConfig.getLabel().getFieldMessagePrefix().length() > 0) {
            domainConfig.getLabel().setFieldMessagePrefix(domainConfig.getLabel().getFieldMessagePrefix().concat(" "));
        }
        // Violation level inputs
        domainConfig.getLabel().setViolationLevelHeader(config.getString("validator.label.violationLevelHeader", "Violation levels for findings on input field headers"));
        domainConfig.getLabel().setDifferentInputFieldCountViolationLevel(config.getString("validator.label.differentInputFieldCountViolationLevel", "Number of input fields different from number of schema fields"));
        domainConfig.getLabel().setDifferentInputFieldSequenceViolationLevel(config.getString("validator.label.differentInputFieldSequenceViolationLevel", "Sequence of input fields different from sequence of schema fields"));
        domainConfig.getLabel().setFieldCaseMismatchViolationLevel(config.getString("validator.label.fieldCaseMismatchViolationLevel", "Names of input fields with different casing compared to schema fields"));
        domainConfig.getLabel().setDuplicateInputFieldsViolationLevel(config.getString("validator.label.duplicateInputFieldsViolationLevel", "Multiple input fields defined with the same name"));
        domainConfig.getLabel().setMultipleInputFieldsForSchemaFieldViolationLevel(config.getString("validator.label.multipleInputFieldsForSchemaFieldViolationLevel", "Multiple input fields mapped to the same schema field"));
        domainConfig.getLabel().setUnknownInputViolationLevel(config.getString("validator.label.unknownInputViolationLevel", "Input fields that are not defined in the schema"));
        domainConfig.getLabel().setUnspecifiedSchemaFieldViolationLevel(config.getString("validator.label.unspecifiedSchemaFieldViolationLevel", "Schema fields for which no input fields are defined"));
        domainConfig.getLabel().setDifferentInputFieldCountViolationLevelTooltip(config.getString("validator.label.differentInputFieldCountViolationLevelTooltip", "The violation level in case the input field count doesn't match the schema field count"));
        domainConfig.getLabel().setDifferentInputFieldSequenceViolationLevelTooltip(config.getString("validator.label.differentInputFieldSequenceViolationLevelTooltip", "The violation level in case the input fields' sequence doesn't match the schema fields' sequence"));
        domainConfig.getLabel().setFieldCaseMismatchViolationLevelTooltip(config.getString("validator.label.fieldCaseMismatchViolationLevelTooltip", "The violation level in case input fields match schema fields but with different casing"));
        domainConfig.getLabel().setDuplicateInputFieldsViolationLevelTooltip(config.getString("validator.label.duplicateInputFieldsViolationLevelTooltip", "The violation level in case the duplicate input fields are found"));
        domainConfig.getLabel().setMultipleInputFieldsForSchemaFieldViolationLevelTooltip(config.getString("validator.label.multipleInputFieldsForSchemaFieldViolationLevelTooltip", "The violation level in case multiple input fields map to the same schema field"));
        domainConfig.getLabel().setUnknownInputViolationLevelTooltip(config.getString("validator.label.unknownInputViolationLevelTooltip", "The violation level in case of unknown input fields"));
        domainConfig.getLabel().setUnspecifiedSchemaFieldViolationLevelTooltip(config.getString("validator.label.unspecifiedSchemaFieldViolationLevelTooltip", "The violation level in case schema fields are not defined"));
        // Violation levels
        domainConfig.getLabel().setViolationLevelError(config.getString("validator.label.violationLevelError", "Error"));
        domainConfig.getLabel().setViolationLevelWarning(config.getString("validator.label.violationLevelWarning", "Warning"));
        domainConfig.getLabel().setViolationLevelInfo(config.getString("validator.label.violationLevelInfo", "Information message"));
        domainConfig.getLabel().setViolationLevelNone(config.getString("validator.label.violationLevelNone", "None"));
        addMissingDefaultValues(domainConfig.getWebServiceDescription(), appConfig.getDefaultLabels());
        // Known parser error mappings - start.
        domainConfig.setParserErrors(parseObjectMap("validator.parserError", config, (name, data) -> {
            if (data.containsKey("pattern") && data.containsKey("message")) {
                return new DomainConfig.ParserError(name, data.get("pattern"), data.get("message"));
            } else {
                return null;
            }
        }));
        // Apply defaults if missing.
        domainConfig.getParserErrors().putIfAbsent(DomainConfig.ParserError.MISSING_HEADER_NAME.getName(), DomainConfig.ParserError.MISSING_HEADER_NAME);
        domainConfig.getParserErrors().putIfAbsent(DomainConfig.ParserError.WRONG_DELIMITER.getName(), DomainConfig.ParserError.WRONG_DELIMITER);
        // - end.
    }
}
