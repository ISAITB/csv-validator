package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.CSVSettings;
import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.BaseInputHelper;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import org.springframework.stereotype.Component;

/**
 * Component used to validate and parse user inputs.
 */
@Component
public class InputHelper extends BaseInputHelper<FileManager, DomainConfig, ApplicationConfig> {

    /**
     * Prepare the CSV syntax settings to be considered by this validation run.
     *
     * @param domainConfig The domain configuration.
     * @param validationType The requested validation type.
     * @param inputs The received inputs.
     * @return The syntax settings to use.
     * @throws ValidatorException In case a problem is detected with the provided inputs.
     */
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

    /**
     * Get the violation level to consider for the provided violation type.
     *
     * @param input The requested violation type from the user's inputs.
     * @param configDefault The default violation type defined for the domain.
     * @param inputSupportType The support level for providing the violation level as an input to the validator.
     * @param errorMessageIfMissingAndRequired The error message to return in case the violation level is required but not
     *                                         provided as part of the inputs.
     * @return The violation level to consider.
     * @throws ValidatorException In case the violation level is required but missing from the inputs.
     */
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

    /**
     * Get the character to consider based on the provided inputs and existing configuration.
     *
     * @param input The related input.
     * @param configDefault The related default value from the configuration.
     * @param inputSupportType The support level for providing the character setting as part of the inputs.
     * @param settingName The name of the setting to reflect in potential error messages.
     * @return The character to use.
     * @throws ValidatorException In case of a problem with the provided inputs.
     */
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

    /**
     * Class used to capture the inputs provided linked to CSV syntax settings.
     */
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

        /**
         * Create a new empty instance.
         *
         * @return The instance.
         */
        public static Inputs newInstance() {
            return new Inputs();
        }

        /**
         * Add the header choice.
         *
         * @param inputHeaders The setting to consider.
         * @return The current object.
         */
        public Inputs withInputHeaders(Boolean inputHeaders) {
            this.inputHeaders = inputHeaders;
            return this;
        }

        /**
         * Add the delimiter.
         *
         * @param inputDelimiter The setting to consider.
         * @return The current object.
         */
        public Inputs withInputDelimiter(String inputDelimiter) {
            this.inputDelimiter = inputDelimiter;
            return this;
        }

        /**
         * Add the quote.
         *
         * @param inputQuote The setting to consider.
         * @return The current object.
         */
        public Inputs withInputQuote(String inputQuote) {
            this.inputQuote = inputQuote;
            return this;
        }

        /**
         * Add the violation level for field name case mismatches.
         *
         * @param inputFieldCaseMismatchViolationLevel The setting to consider.
         * @return The current object.
         */
        public Inputs withInputFieldCaseMismatchViolationLevel(ViolationLevel inputFieldCaseMismatchViolationLevel) {
            this.inputFieldCaseMismatchViolationLevel = inputFieldCaseMismatchViolationLevel;
            return this;
        }

        /**
         * Add the violation level for different field counts compared to the schema.
         *
         * @param differentInputFieldCountViolationLevel The setting to consider.
         * @return The current object.
         */
        public Inputs withDifferentInputFieldCountViolationLevel(ViolationLevel differentInputFieldCountViolationLevel) {
            this.differentInputFieldCountViolationLevel = differentInputFieldCountViolationLevel;
            return this;
        }

        /**
         * Add the violation level for a different field sequence in the input.
         *
         * @param differentInputFieldSequenceViolationLevel The setting to consider.
         * @return The current object.
         */
        public Inputs withDifferentInputFieldSequenceViolationLevel(ViolationLevel differentInputFieldSequenceViolationLevel) {
            this.differentInputFieldSequenceViolationLevel = differentInputFieldSequenceViolationLevel;
            return this;
        }

        /**
         * Add the violation level for unknown input fields.
         *
         * @param unknownInputFieldViolationLevel The setting to consider.
         * @return The current object.
         */
        public Inputs withUnknownInputFieldViolationLevel(ViolationLevel unknownInputFieldViolationLevel) {
            this.unknownInputFieldViolationLevel = unknownInputFieldViolationLevel;
            return this;
        }

        /**
         * Add the violation level for non-covered schema fields.
         *
         * @param unspecifiedSchemaFieldViolationLevel The setting to consider.
         * @return The current object.
         */
        public Inputs withUnspecifiedSchemaFieldViolationLevel(ViolationLevel unspecifiedSchemaFieldViolationLevel) {
            this.unspecifiedSchemaFieldViolationLevel = unspecifiedSchemaFieldViolationLevel;
            return this;
        }

        /**
         * Add the violation level for duplicate input fields.
         *
         * @param duplicateInputFieldsViolationLevel The setting to consider.
         * @return The current object.
         */
        public Inputs withDuplicateInputFieldsViolationLevel(ViolationLevel duplicateInputFieldsViolationLevel) {
            this.duplicateInputFieldsViolationLevel = duplicateInputFieldsViolationLevel;
            return this;
        }

        /**
         * Add the violation level for having multiple inputs for a single schema field.
         *
         * @param multipleInputFieldsForSchemaFieldViolationLevel The setting to consider.
         * @return The current object.
         */
        public Inputs withMultipleInputFieldsForSchemaFieldViolationLevel(ViolationLevel multipleInputFieldsForSchemaFieldViolationLevel) {
            this.multipleInputFieldsForSchemaFieldViolationLevel = multipleInputFieldsForSchemaFieldViolationLevel;
            return this;
        }

    }

}
