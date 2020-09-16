package eu.europa.ec.itb.csv.gitb;

import com.gitb.core.*;
import com.gitb.tr.TAR;
import com.gitb.vs.Void;
import com.gitb.vs.*;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.InputHelper;
import eu.europa.ec.itb.csv.validation.CSVValidator;
import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.csv.validation.ValidationConstants;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.Utils;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.TypedValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.jws.WebParam;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Spring component that realises the validation service.
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
        response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_VALIDATION_TYPE, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_VALIDATION_TYPE)));
        if (definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForHeader())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_HAS_HEADERS, "boolean", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_HAS_HEADERS)));
        }
        if (definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDelimiter())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_DELIMITER, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_DELIMITER)));
        }
        if (definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForQuote())) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_QUOTE, "string", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_QUOTE)));
        }
        if (definesTypeWithExternalSchemas()) {
            response.getModule().getInputs().getParam().add(Utils.createParameter(ValidationConstants.INPUT_EXTERNAL_SCHEMA, "map", UsageEnumeration.O, domainConfig.getWebServiceDescription().get(ValidationConstants.INPUT_EXTERNAL_SCHEMA)));
        }
        return response;
    }

    private boolean definesTypesWithSettingInputs(Map<String, ExternalArtifactSupport> inputSupportMap) {
        for (ExternalArtifactSupport support: inputSupportMap.values()) {
            if (support != ExternalArtifactSupport.NONE) {
                return true;
            }
        }
        return false;
    }

    private boolean definesTypeWithExternalSchemas() {
        for (TypedValidationArtifactInfo info : domainConfig.getArtifactInfo().values()) {
            if (info.get().getExternalArtifactSupport() != ExternalArtifactSupport.NONE) {
                return true;
            }
        }
        return false;
    }

    private <R> R validateAndGetSyntaxInput(ValidateRequest request, String inputName, ExternalArtifactSupport supportType, Function<String, R> fnValueProvider) {
        R result = null;
        if (supportType != ExternalArtifactSupport.NONE) {
            List<AnyContent> inputValues = Utils.getInputFor(request, inputName);
            if (supportType == ExternalArtifactSupport.REQUIRED && inputValues.isEmpty()) {
                throw new ValidatorException("Required input ["+inputName+"] was missing");
            } else if (inputValues.size() > 1) {
                throw new ValidatorException("Multiple values provided for input ["+inputName+"]");
            } else {
                String value = inputValues.get(0).getValue();
                if (value.trim().equals("")) {
                    throw new ValidatorException("An empty value was provided for input ["+inputName+"]");
                }
                result = fnValueProvider.apply(value);
            }
        }
        return result;
    }

    /**
     * The validate operation is called to validate the input and produce a validation report.
     * <p>
     * The expected input is described for the service's client through the getModuleDefinition call.
     *
     * @param validateRequest The input parameters and configuration for the validation.
     * @return The response containing the validation report.
     */
    @Override
    public ValidationResponse validate(@WebParam(name = "ValidateRequest", targetNamespace = "http://www.gitb.com/vs/v1/", partName = "parameters") ValidateRequest validateRequest) {
        MDC.put("domain", domainConfig.getDomain());
        File tempFolderPath = fileManager.createTemporaryFolderPath();
        try {
            // Validation of the input data
            ValueEmbeddingEnumeration contentEmbeddingMethod = inputHelper.validateContentEmbeddingMethod(validateRequest, ValidationConstants.INPUT_EMBEDDING_METHOD);
            File contentToValidate = inputHelper.validateContentToValidate(validateRequest, ValidationConstants.INPUT_CONTENT, contentEmbeddingMethod, tempFolderPath);
            String validationType = inputHelper.validateValidationType(domainConfig, validateRequest, ValidationConstants.INPUT_VALIDATION_TYPE);
            List<FileInfo> externalSchemas = inputHelper.validateExternalArtifacts(domainConfig, validateRequest, ValidationConstants.INPUT_EXTERNAL_SCHEMA, ValidationConstants.INPUT_EXTERNAL_SCHEMA_CONTENT, ValidationConstants.INPUT_EMBEDDING_METHOD, validationType, null, tempFolderPath);
            // CSV settings.
            Boolean inputHeaders = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_HAS_HEADERS, domainConfig.getCsvOptions().getUserInputForHeader().get(validationType), Boolean::valueOf);
            String inputDelimiter = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_DELIMITER, domainConfig.getCsvOptions().getUserInputForDelimiter().get(validationType), (s) -> s);
            String inputQuote = validateAndGetSyntaxInput(validateRequest, ValidationConstants.INPUT_QUOTE, domainConfig.getCsvOptions().getUserInputForQuote().get(validationType), (s) -> s);
            ValidationResponse result = new ValidationResponse();
            // Execute validation
            CSVValidator validator = ctx.getBean(CSVValidator.class, contentToValidate, validationType, externalSchemas, domainConfig, inputHelper.buildCSVSettings(domainConfig, validationType, inputHeaders, inputDelimiter, inputQuote));
            TAR report = validator.validate();
            addContext(report, contentToValidate);
            result.setReport(report);
            return result;
        } catch (ValidatorException e) {
            LOG.error("Validation error", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            throw new ValidatorException(e);
        } finally {
            // Cleanup.
            if (tempFolderPath.exists()) {
                FileUtils.deleteQuietly(tempFolderPath);
            }
        }
    }

    private void addContext(TAR report, File contentToValidate) {
        if (report != null) {
            if (report.getContext() == null) {
                report.setContext(new AnyContent());
            }
            try {
                report.getContext().getItem().add(Utils.createInputItem(ValidationConstants.INPUT_CONTENT, FileUtils.readFileToString(contentToValidate, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                LOG.warn("Error while adding the "+ValidationConstants.INPUT_CONTENT+" to the report's context", e);
            }
        }
    }

}
