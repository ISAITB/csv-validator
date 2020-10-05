package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.CSVSettings;
import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.BaseInputHelper;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import org.springframework.stereotype.Component;

@Component
public class InputHelper extends BaseInputHelper<FileManager, DomainConfig, ApplicationConfig> {

    public CSVSettings buildCSVSettings(DomainConfig domainConfig, String validationType, Inputs inputs) {
        Boolean hasHeaders = domainConfig.getCsvOptions().getInputHasHeader().get(validationType);
        ExternalArtifactSupport hasHeadersInputSupport = domainConfig.getCsvOptions().getUserInputForHeader().get(validationType);
        if (hasHeadersInputSupport != ExternalArtifactSupport.NONE) {
            if (hasHeadersInputSupport == ExternalArtifactSupport.REQUIRED && inputs.inputHeaders == null) {
                throw new ValidatorException("You are required to provide your choice on whether or not the input has a header row.");
            }
            if (inputs.inputHeaders != null) {
                hasHeaders = inputs.inputHeaders;
            }
        }
        return CSVSettings.build()
                // Has headers
                .setHasHeaders(hasHeaders)
                // Delimiter
                .setDelimiter(getCharacterSetting(
                        inputs.inputDelimiter,
                        domainConfig.getCsvOptions().getDelimiter().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForDelimiter().get(validationType),
                        "delimiter"))
                // Quote
                .setQuote(getCharacterSetting(
                        inputs.inputQuote,
                        domainConfig.getCsvOptions().getQuote().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForQuote().get(validationType),
                        "quote"))
                // Different input field count violation level
                .setDifferentInputFieldCountViolationLevel(getViolationLevelSetting(
                        inputs.differentInputFieldCountViolationLevel,
                        domainConfig.getCsvOptions().getDifferentInputFieldCount().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType),
                        "You are required to define the violation level in case the input field count differs from the expected field count."
                        ))
                // Different input field sequence violation level
                .setDifferentInputFieldSequenceViolationLevel(getViolationLevelSetting(
                        inputs.differentInputFieldSequenceViolationLevel,
                        domainConfig.getCsvOptions().getDifferentInputFieldSequence().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForDifferentInputFieldSequence().get(validationType),
                        "You are required to define the violation level in case the input fields are provided in a different sequence than expected."
                ))
                // Unknown input field violation level
                .setUnknownInputFieldViolationLevel(getViolationLevelSetting(
                        inputs.unknownInputFieldViolationLevel,
                        domainConfig.getCsvOptions().getUnknownInputField().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForUnknownInputField().get(validationType),
                        "You are required to define the violation level in case an unexpected input field is provided."
                ))
                // Unspecified schema field violation level
                .setUnspecifiedSchemaFieldViolationLevel(getViolationLevelSetting(
                        inputs.unspecifiedSchemaFieldViolationLevel,
                        domainConfig.getCsvOptions().getUnspecifiedSchemaField().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForUnspecifiedSchemaField().get(validationType),
                        "You are required to define the violation level in case an expected field is missing."
                ))
                // Input field and schema field case mismatch violation level
                .setInputFieldCaseMismatchViolationLevel(getViolationLevelSetting(
                        inputs.inputFieldCaseMismatchViolationLevel,
                        domainConfig.getCsvOptions().getInputFieldCaseMismatch().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForInputFieldCaseMismatch().get(validationType),
                        "You are required to define the violation level in case a field name doesn't match its expected casing."
                ))
                // Duplicate input fields
                .setDuplicateInputFieldViolationLevel(getViolationLevelSetting(
                        inputs.duplicateInputFieldsViolationLevel,
                        domainConfig.getCsvOptions().getDuplicateInputFields().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForDuplicateInputFields().get(validationType),
                        "You are required to define the violation level in case duplicate input fields are found."
                ))
                // Multiple inputs for same schema field
                .setMultipleInputFieldsForSchemaFieldViolationLevel(getViolationLevelSetting(
                        inputs.multipleInputFieldsForSchemaFieldViolationLevel,
                        domainConfig.getCsvOptions().getMultipleInputFieldsForSchemaField().get(validationType),
                        domainConfig.getCsvOptions().getUserInputForMultipleInputFieldsForSchemaField().get(validationType),
                        "You are required to define the violation level in case multiple input fields map to the same schema field."
                ))
                ;
    }

    private ViolationLevel getViolationLevelSetting(ViolationLevel input, ViolationLevel configDefault, ExternalArtifactSupport inputSupportType, String errorMessageIfMissingAndRequired) {
        ViolationLevel result = configDefault;
        if (inputSupportType != ExternalArtifactSupport.NONE) {
            if (inputSupportType == ExternalArtifactSupport.REQUIRED && input == null) {
                throw new ValidatorException(errorMessageIfMissingAndRequired);
            }
            if (input != null) {
                result = input;
            }
        }
        return result;
    }

    private Character getCharacterSetting(String input, Character configDefault, ExternalArtifactSupport inputSupportType, String settingName) {
        if (inputSupportType != ExternalArtifactSupport.NONE) {
            if (inputSupportType == ExternalArtifactSupport.REQUIRED && (input == null || input.isBlank())) {
                throw new ValidatorException("You are required to provide the "+settingName+" character.");
            }
            if (input != null && !input.isEmpty()) {
                if (input.length() > 1) {
                    throw new ValidatorException("A single character is expected for the "+settingName+".");
                }
                return input.charAt(0);
            }
        }
        return configDefault;
    }

    public static class Inputs {

        Boolean inputHeaders;
        String inputDelimiter;
        String inputQuote;
        ViolationLevel differentInputFieldCountViolationLevel;
        ViolationLevel differentInputFieldSequenceViolationLevel;
        ViolationLevel unknownInputFieldViolationLevel;
        ViolationLevel unspecifiedSchemaFieldViolationLevel;
        ViolationLevel inputFieldCaseMismatchViolationLevel;
        ViolationLevel duplicateInputFieldsViolationLevel;
        ViolationLevel multipleInputFieldsForSchemaFieldViolationLevel;

        public static Inputs newInstance() {
            return new Inputs();
        }

        public Inputs withInputHeaders(Boolean inputHeaders) {
            this.inputHeaders = inputHeaders;
            return this;
        }

        public Inputs withInputDelimiter(String inputDelimiter) {
            this.inputDelimiter = inputDelimiter;
            return this;
        }

        public Inputs withInputQuote(String inputQuote) {
            this.inputQuote = inputQuote;
            return this;
        }

        public Inputs withInputFieldCaseMismatchViolationLevel(ViolationLevel inputFieldCaseMismatchViolationLevel) {
            this.inputFieldCaseMismatchViolationLevel = inputFieldCaseMismatchViolationLevel;
            return this;
        }

        public Inputs withDifferentInputFieldCountViolationLevel(ViolationLevel differentInputFieldCountViolationLevel) {
            this.differentInputFieldCountViolationLevel = differentInputFieldCountViolationLevel;
            return this;
        }

        public Inputs withDifferentInputFieldSequenceViolationLevel(ViolationLevel differentInputFieldSequenceViolationLevel) {
            this.differentInputFieldSequenceViolationLevel = differentInputFieldSequenceViolationLevel;
            return this;
        }

        public Inputs withUnknownInputFieldViolationLevel(ViolationLevel unknownInputFieldViolationLevel) {
            this.unknownInputFieldViolationLevel = unknownInputFieldViolationLevel;
            return this;
        }

        public Inputs withUnspecifiedSchemaFieldViolationLevel(ViolationLevel unspecifiedSchemaFieldViolationLevel) {
            this.unspecifiedSchemaFieldViolationLevel = unspecifiedSchemaFieldViolationLevel;
            return this;
        }

        public Inputs withDuplicateInputFieldsViolationLevel(ViolationLevel duplicateInputFieldsViolationLevel) {
            this.duplicateInputFieldsViolationLevel = duplicateInputFieldsViolationLevel;
            return this;
        }

        public Inputs withMultipleInputFieldsForSchemaFieldViolationLevel(ViolationLevel multipleInputFieldsForSchemaFieldViolationLevel) {
            this.multipleInputFieldsForSchemaFieldViolationLevel = multipleInputFieldsForSchemaFieldViolationLevel;
            return this;
        }

    }

}
