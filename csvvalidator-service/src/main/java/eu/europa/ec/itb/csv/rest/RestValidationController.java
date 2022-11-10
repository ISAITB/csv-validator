package eu.europa.ec.itb.csv.rest;

import com.gitb.tr.TAR;
import eu.europa.ec.itb.csv.ApplicationConfig;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.InputHelper;
import eu.europa.ec.itb.csv.rest.model.Input;
import eu.europa.ec.itb.csv.validation.*;
import eu.europa.ec.itb.validation.commons.LocalisationHelper;
import eu.europa.ec.itb.validation.commons.Utils;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import eu.europa.ec.itb.validation.commons.web.errors.NotFoundException;
import eu.europa.ec.itb.validation.commons.web.rest.BaseRestController;
import eu.europa.ec.itb.validation.commons.web.rest.model.Output;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Function;

import static eu.europa.ec.itb.csv.CsvValidatorUtils.addContext;

/**
 * REST controller to allow triggering the validator via its REST API.
 */
@Tag(name = "/{domain}/api", description = "Operations for the validation of CSV content based on Table Schema(s).")
@RestController
public class RestValidationController extends BaseRestController<DomainConfig, ApplicationConfig, FileManager, InputHelper> {

    @Autowired
    private ApplicationContext ctx = null;
    @Autowired
    private FileManager fileManager = null;

    /**
     * Service to trigger one validation for the provided input and settings.
     *
     * @param domain The relevant domain for the validation.
     * @param in The input for the validation.
     * @param request The HTTP request.
     * @return The result of the validator.
     */
    @Operation(summary = "Validate a single CSV document.", description="Validate a single CSV document. The content can be provided either within the request as a BASE64 encoded string or remotely as a URL.")
    @ApiResponse(responseCode = "200", description = "Success (for successful validation)", content = @Content(mediaType = MediaType.APPLICATION_XML_VALUE))
    @ApiResponse(responseCode = "500", description = "Error (If a problem occurred with processing the request)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "404", description = "Not found (for an invalid domain value)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @PostMapping(value = "/{domain}/api/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<StreamingResponseBody> validate(
            @Parameter(required = true, name = "domain", description = "A fixed value corresponding to the specific validation domain.")
            @PathVariable("domain") String domain,
            @Parameter(required = true, name = "input", description = "The input for the validation (content and metadata for one CSV document).")
            @RequestBody Input in,
            HttpServletRequest request
    ) {
        DomainConfig domainConfig = validateDomain(domain);
        /*
         * Important: We call executeValidationProcess here and not in the return statement because the StreamingResponseBody
         * uses a separate thread. Doing so would break the ThreadLocal used in the statistics reporting.
         */
        var report = executeValidationProcess(in, domainConfig);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(outputStream -> fileManager.saveReport(report, outputStream, domainConfig));
    }

    /**
     * Execute the process to validate the content.
     *
     * @param in The input for the validation of one CSV document.
     * @param domainConfig The validation domain.
     * @return The report.
     */
    private TAR executeValidationProcess(Input in, DomainConfig domainConfig) {
        var parentFolder = fileManager.createTemporaryFolderPath();
        var localiser = new LocalisationHelper(domainConfig, Utils.getSupportedLocale(LocaleUtils.toLocale(in.getLocale()), domainConfig));
        try {
            // Extract and validate inputs.
            var validationType = inputHelper.validateValidationType(domainConfig, in.getValidationType());
            var addInputToReport = Objects.requireNonNullElse(in.getAddInputToReport(), false);
            var contentEmbeddingMethod = inputHelper.getEmbeddingMethod(in.getEmbeddingMethod());
            var externalSchemas = getExternalSchemas(domainConfig, in.getExternalSchemas(), validationType, null, parentFolder);
            var contentToValidate = inputHelper.validateContentToValidate(in.getContentToValidate(), contentEmbeddingMethod, parentFolder);
            // CSV settings.
            Boolean inputHeaders = validateAndGetSyntaxInput(in.getHasHeaders(), ValidationConstants.INPUT_HAS_HEADERS, domainConfig.getCsvOptions().getUserInputForHeader().get(validationType), Boolean::valueOf);
            String inputDelimiter = validateAndGetSyntaxInput(in.getDelimiter(), ValidationConstants.INPUT_DELIMITER, domainConfig.getCsvOptions().getUserInputForDelimiter().get(validationType), s -> s);
            String inputQuote = validateAndGetSyntaxInput(in.getQuote(), ValidationConstants.INPUT_QUOTE, domainConfig.getCsvOptions().getUserInputForQuote().get(validationType), s -> s);
            ViolationLevel inputDifferentInputFieldCountViolationLevel = validateAndGetSyntaxInput(in.getDifferentInputFieldCountViolationLevel(), ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputDifferentInputFieldSequenceViolationLevel = validateAndGetSyntaxInput(in.getDifferentInputFieldSequenceViolationLevel(), ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputDuplicateInputFieldsViolationLevel = validateAndGetSyntaxInput(in.getDuplicateInputFieldsViolationLevel(), ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputFieldCaseMismatchViolationLevel = validateAndGetSyntaxInput(in.getFieldCaseMismatchViolationLevel(), ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputMultipleInputFieldsForSchemaFieldViolationLevel = validateAndGetSyntaxInput(in.getMultipleInputFieldsForSchemaFieldViolationLevel(), ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputUnknownInputViolationLevel = validateAndGetSyntaxInput(in.getUnknownInputFieldViolationLevel(), ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
            ViolationLevel inputUnspecifiedSchemaFieldViolationLevel = validateAndGetSyntaxInput(in.getUnspecifiedSchemaField(), ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL, domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount().get(validationType), ViolationLevel::byName);
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
            // Perform validation.
            CSVValidator validator = ctx.getBean(CSVValidator.class, ValidationSpecs.builder(contentToValidate, inputHelper.buildCSVSettings(domainConfig, validationType, inputs), localiser, domainConfig)
                    .withValidationType(validationType)
                    .withExternalSchemas(externalSchemas)
                    .build()
            );
            TAR report = validator.validate().getDetailedReport();
            if (addInputToReport) {
                addContext(report, contentToValidate);
            }
            return report;
        } catch (ValidatorException | NotFoundException e) {
            // Localisation of the ValidatorException takes place in the ErrorHandler.
            throw e;
        } catch (Exception e) {
            // Localisation of the ValidatorException takes place in the ErrorHandler.
            throw new ValidatorException(e);
        } finally {
            FileUtils.deleteQuietly(parentFolder);
        }
    }

    /**
     * Validate multiple CSV inputs considering their settings and producing separate validation reports.
     *
     * @param domain The domain where the validator is executed.
     * @param inputs The input for the validation (content and metadata for one or more CSV documents).
     * @param request The HTTP request.
     * @return The validation result.
     */
    @Operation(summary = "Validate multiple CSV documents.", description="Validate multiple CSV documents. The content for each instance can be provided either within the request as a BASE64 encoded string or remotely as a URL.")
    @ApiResponse(responseCode = "200", description = "Success (for successful validation)", content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Output.class))) })
    @ApiResponse(responseCode = "500", description = "Error (If a problem occurred with processing the request)", content = @Content)
    @ApiResponse(responseCode = "404", description = "Not found (for an invalid domain value)", content = @Content)
    @PostMapping(value = "/{domain}/api/validateMultiple", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Output[] validateMultiple(
            @Parameter(required = true, name = "domain", description = "A fixed value corresponding to the specific validation domain.")
            @PathVariable("domain") String domain,
            @Parameter(required = true, name = "input", description = "The input for the validation (content and metadata for one or more CSV documents).")
            @RequestBody Input[] inputs,
            HttpServletRequest request
    ) {
        DomainConfig domainConfig = validateDomain(domain);
        var outputs = new ArrayList<Output>(inputs.length);
        for (Input input: inputs) {
            Output output = new Output();
            var report = executeValidationProcess(input, domainConfig);
            try (var bos = new ByteArrayOutputStream()) {
                fileManager.saveReport(report, bos, domainConfig);
                output.setReport(Base64.getEncoder().encodeToString(bos.toByteArray()));
                outputs.add(output);
            } catch (IOException e) {
                throw new ValidatorException(e);
            }
        }
        return outputs.toArray(new Output[] {});
    }

    /**
     * Validate and returned the provided input for a given CSV syntax setting.
     *
     * @param inputValue The service input.
     * @param inputName The name of the specific input to lookup.
     * @param supportType The level of support for this to be provided as an input.
     * @param fnValueProvider A function to return the value to use.
     * @param <R> The class of the value to be returned.
     * @return The value to consider.
     */
    private <R, Z> R validateAndGetSyntaxInput(Z inputValue, String inputName, ExternalArtifactSupport supportType, Function<Z, R> fnValueProvider) {
        R result = null;
        if (supportType != ExternalArtifactSupport.NONE) {
            if (supportType == ExternalArtifactSupport.REQUIRED && inputValue == null) {
                throw new ValidatorException("validator.label.exception.requiredInputMissing", inputName);
            } else if (inputValue != null) {
                result = fnValueProvider.apply(inputValue);
            }
        }
        return result;
    }

}
