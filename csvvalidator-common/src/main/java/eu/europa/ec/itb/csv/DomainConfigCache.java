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
        addMissingDefaultValues(domainConfig.getWebServiceDescription(), appConfig.getDefaultLabels());
        // Known parser error mappings - start.
        domainConfig.setParserErrors(parseObjectMap("validator.parserError", config, (name, data) -> {
            if (data.containsKey("pattern")) {
                return new DomainConfig.ParserError(name, data.get("pattern"));
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
