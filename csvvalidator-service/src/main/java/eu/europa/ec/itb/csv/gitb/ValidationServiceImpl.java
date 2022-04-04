package eu.europa.ec.itb.csv.gitb;

import com.gitb.core.*;
import com.gitb.tr.TAR;
import com.gitb.vs.Void;
import com.gitb.vs.*;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.InputHelper;
import eu.europa.ec.itb.csv.validation.*;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.LocalisationHelper;
import eu.europa.ec.itb.validation.commons.Utils;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * Spring component that realises the validation SOAP service.
 */
@Component
@Scope("prototype")
public class ValidationServiceImpl implements ValidationService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceImpl.class);

    private final DomainConfig domainConfig;

    @Autowired
    private ApplicationContext ctx = null;
    @Autowired
    private FileManager fileManager = null;
    @Autowired
    private InputHelper inputHelper = null;
    @Resource
    private WebServiceContext wsContext;

    /**
     * Constructor.
     *
     * @param domainConfig The domain configuration (each domain has its own instance).
     */
    public ValidationServiceImpl(DomainConfig domainConfig) {
        this.domainConfig = domainConfig;
    }

    /**
     * The purpose of the getModuleDefinition call is to inform its caller on how the service is supposed to be called.
     *
     * @param parameters No parameters are expected.
     * @return The response.
     */
    @Override
    public GetModuleDefinitionResponse getModuleDefinition(@WebParam(name = "GetModuleDefinitionRequest", targetNamespace = "http://www.gitb.com/vs/v1/", partName = "parameters") Void parameters) {
        MDC.put("domain", domainConfig.getDomain());
        GetModuleDefinitionResponse response = new GetModuleDefinitionResponse();
        response.setModule(new ValidationModule());
        response.getModule().setId(domainConfig.getWebServiceId());
        response.getModule().setOperation("V");
        response.getModule().setMetadata(new Metadata());
        response.getModule().getMetadata().setName(domainConfig.getWebServiceId());
        response.getModule().getMetadata().setVersion("1.0.0");
        response.getModule().setInputs(new TypedParameters());
        response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_CONTENT, "binary", UsageEnumeration.R, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_CONTENT)));
        response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_EMBEDDING_METHOD, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_EMBEDDING_METHOD)));
        if (domainConfig.hasMultipleValidationTypes()) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_VALIDATION_TYPE, "string", UsageEnumeration.R, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_VALIDATION_TYPE)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForHeader())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_HAS_HEADERS, "boolean", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_HAS_HEADERS)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDelimiter())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_DELIMITER, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_DELIMITER)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForQuote())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_QUOTE, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_QUOTE)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDifferentInputFieldSequence())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDuplicateInputFields())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForInputFieldCaseMismatch())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForMultipleInputFieldsForSchemaField())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForUnknownInputField())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL)));
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForUnspecifiedSchemaField())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL)));
        }
        if (domainConfig.definesTypeWithExternalSchemas()) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_EXTERNAL_SCHEMA, "map", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_EXTERNAL_SCHEMA)));
        }
        response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_ADD_INPUT_TO_REPORT, "boolean", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_ADD_INPUT_TO_REPORT)));
        response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_LOCALE, "string", UsageEnumeration.O, ConfigurationType.SIMPLE, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_LOCALE)));
        return response;
    }

    /**
     * Validate and returned the provided input for a given CSV syntax setting.
     *
     * @param request The service input.
     * @param inputName The name of the specific input to lookup.
     * @param supportType The level of support for this to be provided as an input.
     * @param fnValueProvider A function to return the value to use.
     * @param <R> The class of the value to be returned.
     * @return The value to consider.
     */
    private <R> R validateAndGetSyntaxInput(ValidateRequest request, String inputName, ExternalArtifactSupport supportType, Function<String, R> fnValueProvider) {
        R result = null;
        if (supportType != ExternalArtifactSupport.NONE) {
            List<AnyContent> inputValues = Utils.getInputFor(request, inputName);
            if (supportType == ExternalArtifactSupport.REQUIRED && inputValues.isEmpty()) {
                throw new ValidatorException("validator.label.exception.requiredInputMissing", inputName);
            } else if (inputValues.size() > 1) {
                throw new ValidatorException("validator.label.exception.multipleInputsProvided", inputName);
            } else if (!inputValues.isEmpty()) {
                String value = inputValues.get(0).getValue();
                if (value.trim().equals("")) {
                    throw new ValidatorException("validator.label.exception.emptyInputProvided", inputName);
                }
                result = fnValueProvider.apply(value);
            }
        }
        return result;
    }

    /**
     * The validate operation is called to validate the input and produce a validation report.
     *
     * The expected input is described for the service's client through the getModuleDefinition call.
     *
     * @param validateRequest The input parameters and configuration for the validation.
     * @return The response containing the validation report.
     */
    @Override
    public ValidationResponse validate(@WebParam(name = "ValidateRequest", targetNamespace = "http://www.gitb.com/vs/v1/", partName = "parameters") ValidateRequest validateRequest) {
        MDC.put("domain", domainConfig.getDomain());
        File tempFolderPath = fileManager.createTemporaryFolderPath();
        var localiser = new LocalisationHelper(domainConfig, Utils.getSupportedLocale(LocaleUtils.toLocale(getInputAsString(validateRequest, ValidationConstants.INPUT_LOCALE, null)), domainConfig));
        try {
            // Validation of the input data
            ValueEmbeddingEnumeration contentEmbeddingMethod = inputHelper.validateContentEmbeddingMethod(validateRequest, ValidationConstants.INPUT_EMBEDDING_METHOD);
            File contentToValidate = inputHelper.validateContentToValidate(validateRequest, ValidationConstants.INPUT_CONTENT, contentEmbeddingMethod, tempFolderPath);
            String validationType = inputHelper.validateValidationType(domainConfig, validateRequest, ValidationConstants.INPUT_VALIDATION_TYPE);
            List<FileInfo> externalSchemas = inputHelper.validateExternalArtifacts(domainConfig, validateRequest, ValidationConstants.INPUT_EXTERNAL_SCHEMA, ValidationConstants.INPUT_EXTERNAL_SCHEMA_CONTENT, ValidationConstants.INPUT_EMBEDDING_METHOD, validationType, null, tempFolderPath);
            boolean addInputToReport = getInputAsBoolean(validateRequest, ValidationConstants.INPUT_ADD_INPUT_TO_REPORT, true);
            // CSV settings.
            Boolean inputHeaders = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_HAS_HEADERS, domainConfig.getCsvOptions().getUserInputForHeader().get(validationType), Boolean::valueOf);
            String inputDelimiter = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_DELIMITER, domainConfig.getCsvOptions().getUserInputForDelimiter().get(validationType), s -> s);
            String inputQuote = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_QUOTE, domainConfig.getCsvOptions().getUserInputForQuote().get(validationType), s -> s);
            ViolationLevel inputDifferentInputFieldCountViolationLevel = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputDifferentInputFieldSequenceViolationLevel = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputDuplicateInputFieldsViolationLevel = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputFieldCaseMismatchViolationLevel = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputMultipleInputFieldsForSchemaFieldViolationLevel = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputUnknownInputViolationLevel = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputUnspecifiedSchemaFieldViolationLevel = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ValidationResponse result = new ValidationResponse();
            // Execute validation
            InputHelper.Inputs inputs = InputHelper.Inputs.newInstance()
                    .withInputHeaders(inputHeaders)
                    .withInputDelimiter(inputDelimiter)
                    .withInputQuote(inputQuote)
                    .withDifferentInputFieldCountViolationLevel(inputDifferentInputFieldCountViolationLevel)
                    .withDifferentInputFieldSequenceViolationLevel(inputDifferentInputFieldSequenceViolationLevel)
                    .withDuplicateInputFieldsViolationLevel(inputDuplicateInputFieldsViolationLevel)
                    .withInputFieldCaseMismatchViolationLevel(inputFieldCaseMismatchViolationLevel)
                    .withMultipleInputFieldsForSchemaFieldViolationLevel(inputMultipleInputFieldsForSchemaFieldViolationLevel)
                    .withUnknownInputFieldViolationLevel(inputUnknownInputViolationLevel)
                    .withUnspecifiedSchemaFieldViolationLevel(inputUnspecifiedSchemaFieldViolationLevel);
            CSVValidator validator = ctx.getBean(CSVValidator.class, ValidationSpecs.builder(contentToValidate, inputHelper.buildCSVSettings(domainConfig, validationType, inputs), localiser, domainConfig)
                    .withValidationType(validationType)
                    .withExternalSchemas(externalSchemas)
                    .build()
            );
            TAR report = validator.validate().getDetailedReport();
            if (addInputToReport) {
                addContext(report, contentToValidate);
            }
            result.setReport(report);
            return result;
        } catch (ValidatorException e) {
            LOG.error(e.getMessageForLog(), e);
            throw new ValidatorException(e.getMessageForDisplay(localiser), true);
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            var message = localiser.localise(ValidatorException.MESSAGE_DEFAULT);
            throw new ValidatorException(message, e, true, (Object[]) null);
        } finally {
            // Cleanup.
            if (tempFolderPath.exists()) {
                FileUtils.deleteQuietly(tempFolderPath);
            }
        }
    }

    /**
     * Add the context map to the provided validation report.
     *
     * @param report The report to add to.
     * @param contentToValidate The input file.
     */
    private void addContext(TAR report, File contentToValidate) {
        if (report != null) {
            if (report.getContext() == null) {
                report.setContext(new AnyContent());
            }
            try {
                var content = Utils.createInputItem(ValidationConstants.INPUT_CONTENT, FileUtils.readFileToString(contentToValidate, StandardCharsets.UTF_8));
                content.setMimeType("text/csv");
                report.getContext().getItem().add(content);
            } catch (IOException e) {
                LOG.warn("Error while adding the "+ValidationConstants.INPUT_CONTENT+" to the report's context", e);
            }
        }
    }

    /**
     * Get the provided (optional) input as a boolean value.
     *
     * @param validateRequest The input parameters.
     * @param inputName The name of the input to look for.
     * @param defaultIfMissing The default value to use if the input is not provided.
     * @return The value to use.
     */
    private boolean getInputAsBoolean(ValidateRequest validateRequest, String inputName, boolean defaultIfMissing) {
        List<AnyContent> input = Utils.getInputFor(validateRequest, inputName);
        if (!input.isEmpty()) {
            return Boolean.parseBoolean(input.get(0).getValue());
        }
        return defaultIfMissing;
    }

    /**
     * Get the provided (optional) input as a string value.
     *
     * @param validateRequest The input parameters.
     * @param inputName The name of the input to look for.
     * @param defaultIfMissing The default value to use if the input is not provided.
     * @return The value to use.
     */
    private String getInputAsString(ValidateRequest validateRequest, String inputName, String defaultIfMissing) {
        List<AnyContent> input = Utils.getInputFor(validateRequest, inputName);
        if (!input.isEmpty()) {
            return input.get(0).getValue();
        }
        return defaultIfMissing;
    }

    /**
     * @return The web service context.
     */
    public WebServiceContext getWebServiceContext(){
        return this.wsContext;
    }

}
