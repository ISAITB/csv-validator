package eu.europa.ec.itb.csv.web;

import eu.europa.ec.itb.csv.ApplicationConfig;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.DomainConfigCache;
import eu.europa.ec.itb.csv.InputHelper;
import eu.europa.ec.itb.csv.validation.CSVValidator;
import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.csv.validation.ValidationSpecs;
import eu.europa.ec.itb.csv.validation.ViolationLevel;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.LocalisationHelper;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import eu.europa.ec.itb.validation.commons.web.BaseUploadController;
import eu.europa.ec.itb.validation.commons.web.Constants;
import eu.europa.ec.itb.validation.commons.web.dto.Translations;
import eu.europa.ec.itb.validation.commons.web.dto.UploadResult;
import eu.europa.ec.itb.validation.commons.web.locale.CustomLocaleResolver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static eu.europa.ec.itb.validation.commons.web.Constants.*;

/**
 * Controller to manage the validator's web user interface.
 */
@Controller
public class UploadController extends BaseUploadController<DomainConfig, DomainConfigCache> {

    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);
    private static final String IS_MINIMAL = "isMinimal";

    @Autowired
    private ApplicationConfig appConfig = null;
    @Autowired
    private FileManager fileManager = null;
    @Autowired
    private InputHelper inputHelper = null;
    @Autowired
    private BeanFactory beans = null;
    @Autowired
    private CustomLocaleResolver localeResolver = null;

    /**
     * Prepare the upload page.
     *
     * @param domain The domain name.
     * @param request The received request.
     * @param response The HTTP response.
     * @return The model and view information.
     */
    @GetMapping(value = "/{domain}/upload")
    public ModelAndView upload(@PathVariable("domain") String domain, HttpServletRequest request, HttpServletResponse response) {
        var config = getDomainConfig(request);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(PARAM_DOMAIN_CONFIG, config);
        attributes.put(PARAM_APP_CONFIG, appConfig);
        attributes.put(PARAM_MINIMAL_UI, isMinimalUI(request));
        attributes.put(PARAM_EXTERNAL_ARTIFACT_INFO, config.getExternalArtifactInfoMap());
        var localisationHelper = new LocalisationHelper(config, localeResolver.resolveLocale(request, response, config, appConfig));
        attributes.put(PARAM_LOCALISER, localisationHelper);
        attributes.put(PARAM_HTML_BANNER_EXISTS, localisationHelper.propertyExists("validator.bannerHtml"));
        return new ModelAndView(VIEW_UPLOAD_FORM, attributes);
    }

    /**
     * Prepare the upload page (minimal UI version).
     *
     * @param domain The domain name.
     * @param request The received request.
     * @param response The HTTP response.
     * @return The model and view information.
     */
    @GetMapping(value = "/{domain}/uploadm")
    public ModelAndView uploadMinimal(@PathVariable("domain") String domain, HttpServletRequest request, HttpServletResponse response) {
        return upload(domain, request, response);
    }

    /**
     * Handle the upload form's submission.
     *
     * @param domain The domain name.
     * @param file The input file (if provided via file upload).
     * @param uri The input URI (if provided via remote URI).
     * @param string The input content (if provided via editor).
     * @param validationType The validation type.
     * @param contentType The type of the provided content.
     * @param externalSchemaContentType The content type of the user-provided schemas.
     * @param externalSchemaFiles The user-provided schemas (those provided as file uploads).
     * @param externalSchemaUri The user-provided schemas (those provided as URIs).
     * @param csvSettingsCheck The flag to provide CSV syntax settings.
     * @param inputHeaders The flag on whether the input includes a header row.
     * @param inputDelimiter The delimiter character.
     * @param inputQuote The quote character.
     * @param inputDifferentInputFieldCountViolationLevel The violation level for a different count of input fields compared to the schema.
     * @param inputDifferentInputFieldSequenceViolationLevel The violation level for a different sequence of input fields compared to the schema.
     * @param inputDuplicateInputFieldsViolationLevel The violation level for duplicate input fields.
     * @param inputFieldCaseMismatchViolationLevel The violation level for header name case mismatches compared to the schema.
     * @param inputMultipleInputFieldsForSchemaFieldViolationLevel The violation level when multiple input fields map to the same schema field.
     * @param inputUnknownInputViolationLevel The violation level for unknown input fields.
     * @param inputUnspecifiedSchemaFieldViolationLevel The violation level for schema fields for which no input fields are provided.
     * @param redirectAttributes Redirect attributes.
     * @param request The received request.
     * @param response The HTTP response.
     * @return The model and view information.
     */
    @PostMapping(value = "/{domain}/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UploadResult<Translations> handleUpload(@PathVariable("domain") String domain,
                                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                                   @RequestParam(value = "uri", defaultValue = "") String uri,
                                                   @RequestParam(value = "text", defaultValue = "") String string,
                                                   @RequestParam(value = "validationType", defaultValue = "") String validationType,
                                                   @RequestParam(value = "contentType", defaultValue = "") String contentType,
                                                   @RequestParam(value = "contentType-external_default", required = false) String[] externalSchemaContentType,
                                                   @RequestParam(value = "inputFile-external_default", required = false) MultipartFile[] externalSchemaFiles,
                                                   @RequestParam(value = "uri-external_default", required = false) String[] externalSchemaUri,
                                                   @RequestParam(value = "csvSettingsCheck", required = false, defaultValue = "false") Boolean csvSettingsCheck,
                                                   @RequestParam(value = "inputHeaders", required = false, defaultValue = "false") Boolean inputHeaders,
                                                   @RequestParam(value = "inputDelimiter", required = false) String inputDelimiter,
                                                   @RequestParam(value = "inputQuote", required = false) String inputQuote,
                                                   @RequestParam(value = "inputDifferentInputFieldCountViolationLevel", required = false) String inputDifferentInputFieldCountViolationLevel,
                                                   @RequestParam(value = "inputDifferentInputFieldSequenceViolationLevel", required = false) String inputDifferentInputFieldSequenceViolationLevel,
                                                   @RequestParam(value = "inputDuplicateInputFieldsViolationLevel", required = false) String inputDuplicateInputFieldsViolationLevel,
                                                   @RequestParam(value = "inputFieldCaseMismatchViolationLevel", required = false) String inputFieldCaseMismatchViolationLevel,
                                                   @RequestParam(value = "inputMultipleInputFieldsForSchemaFieldViolationLevel", required = false) String inputMultipleInputFieldsForSchemaFieldViolationLevel,
                                                   @RequestParam(value = "inputUnknownInputViolationLevel", required = false) String inputUnknownInputViolationLevel,
                                                   @RequestParam(value = "inputUnspecifiedSchemaFieldViolationLevel", required = false) String inputUnspecifiedSchemaFieldViolationLevel,
                                                   RedirectAttributes redirectAttributes,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) {
        var domainConfig = getDomainConfig(request);
        contentType = checkInputType(contentType, file, uri, string);
        var localisationHelper = new LocalisationHelper(domainConfig, localeResolver.resolveLocale(request, response, domainConfig, appConfig));
        var result = new UploadResult<>();

        if (StringUtils.isBlank(validationType)) {
            validationType = domainConfig.getType().get(0);
        }
        if (domainConfig.hasMultipleValidationTypes() && (validationType == null || !domainConfig.getType().contains(validationType))) {
            // A validation type is required.
            result.setMessage(localisationHelper.localise("validator.label.exception.providedValidationTypeNotValid"));
        } else {
            InputStream stream = null;
            try {
                try (InputStream fis = getInputStream(contentType, file, uri, string)) {
                    if (fileManager.checkFileType(fis)) {
                        stream = getInputStream(contentType, file, uri, string);
                    } else {
                        result.setMessage(localisationHelper.localise("validator.label.exception.providedInputNotJSON"));
                    }
                }
            } catch (ValidatorException e) {
                LOG.error(e.getMessageForLog(), e);
                result.setMessage(e.getMessageForDisplay(localisationHelper));
            } catch (Exception e) {
                LOG.error("Error while reading provided content [" + e.getMessage() + "]", e);
                result.setMessage(localisationHelper.localise("validator.label.exception.errorReadingContent", e.getMessage()));
            }
            if (stream != null) {
                var proceed = true;
                File tempFolderForRequest = fileManager.createTemporaryFolderPath();
                try {
                    File contentToValidate = fileManager.getFileFromInputStream(tempFolderForRequest, stream, null, UUID.randomUUID() + ".json");
                    List<FileInfo> externalSchemas = new ArrayList<>();
                    try {
                        externalSchemas = getExternalFiles(externalSchemaContentType, externalSchemaFiles, externalSchemaUri, domainConfig.getSchemaInfo(validationType), tempFolderForRequest);
                    } catch (ValidatorException e) {
                        LOG.error(e.getMessageForLog(), e);
                        result.setMessage(e.getMessageForDisplay(localisationHelper));
                        proceed = false;
                    } catch (Exception e) {
                        LOG.error("Error while reading uploaded file [" + e.getMessage() + "]", e);
                        result.setMessage(localisationHelper.localise("validator.label.exception.errorInUpload", e.getMessage()));
                        proceed = false;
                    }
                    if (proceed) {
                        if (Boolean.FALSE.equals(csvSettingsCheck)) {
                            inputHeaders = null;
                            inputDelimiter = null;
                            inputQuote = null;
                            inputDifferentInputFieldCountViolationLevel = null;
                            inputDifferentInputFieldSequenceViolationLevel = null;
                            inputDuplicateInputFieldsViolationLevel = null;
                            inputFieldCaseMismatchViolationLevel = null;
                            inputMultipleInputFieldsForSchemaFieldViolationLevel = null;
                            inputUnknownInputViolationLevel = null;
                            inputUnspecifiedSchemaFieldViolationLevel = null;
                        }
                        InputHelper.Inputs inputs = InputHelper.Inputs.newInstance()
                                .withInputHeaders(inputHeaders)
                                .withInputDelimiter(inputDelimiter)
                                .withInputQuote(inputQuote)
                                .withDifferentInputFieldCountViolationLevel(ViolationLevel.byName(inputDifferentInputFieldCountViolationLevel))
                                .withDifferentInputFieldSequenceViolationLevel(ViolationLevel.byName(inputDifferentInputFieldSequenceViolationLevel))
                                .withDuplicateInputFieldsViolationLevel(ViolationLevel.byName(inputDuplicateInputFieldsViolationLevel))
                                .withInputFieldCaseMismatchViolationLevel(ViolationLevel.byName(inputFieldCaseMismatchViolationLevel))
                                .withMultipleInputFieldsForSchemaFieldViolationLevel(ViolationLevel.byName(inputMultipleInputFieldsForSchemaFieldViolationLevel))
                                .withUnknownInputFieldViolationLevel(ViolationLevel.byName(inputUnknownInputViolationLevel))
                                .withUnspecifiedSchemaFieldViolationLevel(ViolationLevel.byName(inputUnspecifiedSchemaFieldViolationLevel));
                        CSVValidator validator = beans.getBean(CSVValidator.class, ValidationSpecs.builder(contentToValidate, inputHelper.buildCSVSettings(domainConfig, validationType, inputs), localisationHelper, domainConfig)
                                .withValidationType(validationType)
                                .withExternalSchemas(externalSchemas)
                                .produceAggregateReport()
                                .build()
                        );
                        var reports = validator.validate();
                        // Cache detailed report.
                        try {
                            String inputID = fileManager.writeCSV(domainConfig.getDomainName(), contentToValidate);
                            fileManager.saveReport(reports.getDetailedReport(), inputID, domainConfig);
                            fileManager.saveReport(reports.getAggregateReport(), inputID, domainConfig, true);
                            String fileName;
                            if (contentType.equals(CONTENT_TYPE_FILE)) {
                                fileName = file.getOriginalFilename();
                            } else if (contentType.equals(CONTENT_TYPE_URI)) {
                                fileName = uri;
                            } else {
                                fileName = "-";
                            }
                            result.populateCommon(localisationHelper, validationType, domainConfig, inputID, fileName,
                                    reports.getDetailedReport(), reports.getAggregateReport(),
                                    new Translations(localisationHelper, reports.getDetailedReport(), domainConfig));
                        } catch (IOException e) {
                            LOG.error("Error generating detailed report [" + e.getMessage() + "]", e);
                            result.setMessage(localisationHelper.localise("validator.label.exception.errorGeneratingReport", e.getMessage()));
                        }
                    }
                } catch (ValidatorException e) {
                    LOG.error(e.getMessageForLog(), e);
                    result.setMessage(e.getMessageForDisplay(localisationHelper));
                } catch (Exception e) {
                    LOG.error("An error occurred during the validation [" + e.getMessage() + "]", e);
                    result.setMessage(localisationHelper.localise("validator.label.exception.errorDuringValidation", e.getMessage()));
                } finally {
                    // Cleanup temporary resources for request.
                    if (tempFolderForRequest.exists()) {
                        FileUtils.deleteQuietly(tempFolderForRequest);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Handle the upload form's submission when the user interface is minimal.
     *
     * @see UploadController#handleUpload(String, MultipartFile, String, String, String, String, String[], MultipartFile[], String[], Boolean, Boolean, String, String, String, String, String, String, String, String, String, RedirectAttributes, HttpServletRequest, HttpServletResponse)
     */
    @PostMapping(value = "/{domain}/uploadm", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public UploadResult<Translations> handleUploadMinimal(@PathVariable("domain") String domain,
                                      @RequestParam(value = "file", required = false) MultipartFile file,
                                      @RequestParam(value = "uri", defaultValue = "") String uri,
                                      @RequestParam(value = "text", defaultValue = "") String string,
                                      @RequestParam(value = "validationType", defaultValue = "") String validationType,
                                      @RequestParam(value = "contentType", defaultValue = "") String contentType,
                                      @RequestParam(value = "contentType-external_default", required = false) String[] externalSchema,
                                      @RequestParam(value = "inputFile-external_default", required= false) MultipartFile[] externalSchemaFiles,
                                      @RequestParam(value = "uriToValidate-external_default", required = false) String[] externalSchemaUri,
                                      @RequestParam(value = "csvSettingsCheck", required = false, defaultValue = "false") Boolean csvSettingsCheck,
                                      @RequestParam(value = "inputHeaders", required = false, defaultValue = "false") Boolean inputHeaders,
                                      @RequestParam(value = "inputDelimiter", required = false) String inputDelimiter,
                                      @RequestParam(value = "inputQuote", required = false) String inputQuote,
                                      @RequestParam(value = "inputDifferentInputFieldCountViolationLevel", required = false) String inputDifferentInputFieldCountViolationLevel,
                                      @RequestParam(value = "inputDifferentInputFieldSequenceViolationLevel", required = false) String inputDifferentInputFieldSequenceViolationLevel,
                                      @RequestParam(value = "inputDuplicateInputFieldsViolationLevel", required = false) String inputDuplicateInputFieldsViolationLevel,
                                      @RequestParam(value = "inputFieldCaseMismatchViolationLevel", required = false) String inputFieldCaseMismatchViolationLevel,
                                      @RequestParam(value = "inputMultipleInputFieldsForSchemaFieldViolationLevel", required = false) String inputMultipleInputFieldsForSchemaFieldViolationLevel,
                                      @RequestParam(value = "inputUnknownInputViolationLevel", required = false) String inputUnknownInputViolationLevel,
                                      @RequestParam(value = "inputUnspecifiedSchemaFieldViolationLevel", required = false) String inputUnspecifiedSchemaFieldViolationLevel,
                                      RedirectAttributes redirectAttributes,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        return handleUpload(
                domain, file, uri, string, validationType, contentType,
                externalSchema, externalSchemaFiles, externalSchemaUri,
                csvSettingsCheck, inputHeaders, inputDelimiter, inputQuote,
                inputDifferentInputFieldCountViolationLevel, inputDifferentInputFieldSequenceViolationLevel,
                inputDuplicateInputFieldsViolationLevel, inputFieldCaseMismatchViolationLevel,
                inputMultipleInputFieldsForSchemaFieldViolationLevel, inputUnknownInputViolationLevel,
                inputUnspecifiedSchemaFieldViolationLevel,
                redirectAttributes, request, response);
    }

    /**
     * Handle the upload form's submission when the user interface is embedded in another web page.
     *
     * @see UploadController#handleUpload(String, MultipartFile, String, String, String, String, String[], MultipartFile[], String[], Boolean, Boolean, String, String, String, String, String, String, String, String, String, RedirectAttributes, HttpServletRequest, HttpServletResponse)
     */
    @PostMapping(value = "/{domain}/upload", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView handleUploadEmbedded(@PathVariable("domain") String domain,
                                             @RequestParam(value = "file", required = false) MultipartFile file,
                                             @RequestParam(value = "uri", defaultValue = "") String uri,
                                             @RequestParam(value = "text", defaultValue = "") String string,
                                             @RequestParam(value = "validationType", defaultValue = "") String validationType,
                                             @RequestParam(value = "contentType", defaultValue = "") String contentType,
                                             @RequestParam(value = "contentType-external_default", required = false) String[] externalSchema,
                                             @RequestParam(value = "inputFile-external_default", required= false) MultipartFile[] externalSchemaFiles,
                                             @RequestParam(value = "uriToValidate-external_default", required = false) String[] externalSchemaUri,
                                             @RequestParam(value = "csvSettingsCheck", required = false, defaultValue = "false") Boolean csvSettingsCheck,
                                             @RequestParam(value = "inputHeaders", required = false, defaultValue = "false") Boolean inputHeaders,
                                             @RequestParam(value = "inputDelimiter", required = false) String inputDelimiter,
                                             @RequestParam(value = "inputQuote", required = false) String inputQuote,
                                             @RequestParam(value = "inputDifferentInputFieldCountViolationLevel", required = false) String inputDifferentInputFieldCountViolationLevel,
                                             @RequestParam(value = "inputDifferentInputFieldSequenceViolationLevel", required = false) String inputDifferentInputFieldSequenceViolationLevel,
                                             @RequestParam(value = "inputDuplicateInputFieldsViolationLevel", required = false) String inputDuplicateInputFieldsViolationLevel,
                                             @RequestParam(value = "inputFieldCaseMismatchViolationLevel", required = false) String inputFieldCaseMismatchViolationLevel,
                                             @RequestParam(value = "inputMultipleInputFieldsForSchemaFieldViolationLevel", required = false) String inputMultipleInputFieldsForSchemaFieldViolationLevel,
                                             @RequestParam(value = "inputUnknownInputViolationLevel", required = false) String inputUnknownInputViolationLevel,
                                             @RequestParam(value = "inputUnspecifiedSchemaFieldViolationLevel", required = false) String inputUnspecifiedSchemaFieldViolationLevel,
                                             RedirectAttributes redirectAttributes,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        var uploadForm = upload(domain, request, response);
        var uploadResult = handleUpload(
                domain, file, uri, string, validationType, contentType,
                externalSchema, externalSchemaFiles, externalSchemaUri,
                csvSettingsCheck, inputHeaders, inputDelimiter, inputQuote,
                inputDifferentInputFieldCountViolationLevel, inputDifferentInputFieldSequenceViolationLevel,
                inputDuplicateInputFieldsViolationLevel, inputFieldCaseMismatchViolationLevel,
                inputMultipleInputFieldsForSchemaFieldViolationLevel, inputUnknownInputViolationLevel,
                inputUnspecifiedSchemaFieldViolationLevel,
                redirectAttributes, request, response);
        uploadForm.getModel().put(Constants.PARAM_REPORT_DATA, writeResultToString(uploadResult));
        return uploadForm;
    }

    /**
     * Handle the upload form's submission when the user interface is minimal and embedded in another web page.
     *
     * @see UploadController#handleUpload(String, MultipartFile, String, String, String, String, String[], MultipartFile[], String[], Boolean, Boolean, String, String, String, String, String, String, String, String, String, RedirectAttributes, HttpServletRequest, HttpServletResponse)
     */
    @PostMapping(value = "/{domain}/uploadm", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView handleUploadMinimalEmbedded(@PathVariable("domain") String domain,
                                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                                    @RequestParam(value = "uri", defaultValue = "") String uri,
                                                    @RequestParam(value = "text", defaultValue = "") String string,
                                                    @RequestParam(value = "validationType", defaultValue = "") String validationType,
                                                    @RequestParam(value = "contentType", defaultValue = "") String contentType,
                                                    @RequestParam(value = "contentType-external_default", required = false) String[] externalSchema,
                                                    @RequestParam(value = "inputFile-external_default", required= false) MultipartFile[] externalSchemaFiles,
                                                    @RequestParam(value = "uriToValidate-external_default", required = false) String[] externalSchemaUri,
                                                    @RequestParam(value = "csvSettingsCheck", required = false, defaultValue = "false") Boolean csvSettingsCheck,
                                                    @RequestParam(value = "inputHeaders", required = false, defaultValue = "false") Boolean inputHeaders,
                                                    @RequestParam(value = "inputDelimiter", required = false) String inputDelimiter,
                                                    @RequestParam(value = "inputQuote", required = false) String inputQuote,
                                                    @RequestParam(value = "inputDifferentInputFieldCountViolationLevel", required = false) String inputDifferentInputFieldCountViolationLevel,
                                                    @RequestParam(value = "inputDifferentInputFieldSequenceViolationLevel", required = false) String inputDifferentInputFieldSequenceViolationLevel,
                                                    @RequestParam(value = "inputDuplicateInputFieldsViolationLevel", required = false) String inputDuplicateInputFieldsViolationLevel,
                                                    @RequestParam(value = "inputFieldCaseMismatchViolationLevel", required = false) String inputFieldCaseMismatchViolationLevel,
                                                    @RequestParam(value = "inputMultipleInputFieldsForSchemaFieldViolationLevel", required = false) String inputMultipleInputFieldsForSchemaFieldViolationLevel,
                                                    @RequestParam(value = "inputUnknownInputViolationLevel", required = false) String inputUnknownInputViolationLevel,
                                                    @RequestParam(value = "inputUnspecifiedSchemaFieldViolationLevel", required = false) String inputUnspecifiedSchemaFieldViolationLevel,
                                                    RedirectAttributes redirectAttributes,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {
        return handleUploadEmbedded(
                domain, file, uri, string, validationType, contentType,
                externalSchema, externalSchemaFiles, externalSchemaUri,
                csvSettingsCheck, inputHeaders, inputDelimiter, inputQuote,
                inputDifferentInputFieldCountViolationLevel, inputDifferentInputFieldSequenceViolationLevel,
                inputDuplicateInputFieldsViolationLevel, inputFieldCaseMismatchViolationLevel,
                inputMultipleInputFieldsForSchemaFieldViolationLevel, inputUnknownInputViolationLevel,
                inputUnspecifiedSchemaFieldViolationLevel,
                redirectAttributes, request, response);
    }

    /**
     * Validate and get the user-provided schemas.
     *
     * @param externalContentType The directly provided schemas.
     * @param externalFiles The schemas provided as files.
     * @param externalUri The schemas provided as URIs.
     * @param schemaInfo The schema information from the domain.
     * @param parentFolder The temporary folder to use for file system storage.
     * @return The list of user-provided artifacts.
     * @throws IOException If an error occurs.
     */
    private List<FileInfo> getExternalFiles(String[] externalContentType, MultipartFile[] externalFiles, String[] externalUri,
                                            ValidationArtifactInfo schemaInfo, File parentFolder) throws IOException {
        List<FileInfo> externalArtifacts = new ArrayList<>();
        if (externalContentType != null) {
            for(int i=0; i < externalContentType.length; i++) {
                File inputFile;
                MultipartFile currentExtFile = null;
                String currentExtUri = "";
                if (externalFiles != null && externalFiles.length>i) {
                    currentExtFile = externalFiles[i];
                }
                if (externalUri != null && externalUri.length>i) {
                    currentExtUri = externalUri[i];
                }
                inputFile = getInputFile(externalContentType[i], currentExtFile, currentExtUri, parentFolder);
                FileInfo fi = new FileInfo(inputFile);
                externalArtifacts.add(fi);
            }
        }
        if (validateExternalFiles(externalArtifacts, schemaInfo)) {
            return externalArtifacts;
        } else {
            LOG.error("An error occurred during the validation of the external schema(s).");
            throw new ValidatorException("validator.label.exception.errorValidatingExternalSchema");
        }
    }

    /**
     * Validate the list of user-provided schemas.
     *
     * @param externalArtifacts The schemas.
     * @param schemaInfo The schema information from the domain configuration.
     * @return True for correctly provided schemas.
     */
    private boolean validateExternalFiles(List<FileInfo> externalArtifacts, ValidationArtifactInfo schemaInfo) {
        ExternalArtifactSupport externalArtifactSupport = schemaInfo.getExternalArtifactSupport();
        boolean validated = false;
        switch (externalArtifactSupport) {
            case REQUIRED:
                if (externalArtifacts != null && !externalArtifacts.isEmpty()) {
                    validated = true;
                }
                break;
            case OPTIONAL:
                validated = true;
                break;
            case NONE:
                if (externalArtifacts == null || externalArtifacts.isEmpty()) {
                    validated = true;
                }
                break;
        }
        return validated;
    }

    /**
     * Get the content to validate as a file.
     *
     * @param contentType The directly provided content.
     * @param inputFile The uploaded content file.
     * @param inputUri The provided URI to load the content from.
     * @param parentFolder The temporary folder to use.
     * @return The input content's file.
     * @throws IOException If an error occurs.
     */
    private File getInputFile(String contentType, MultipartFile inputFile, String inputUri, File parentFolder) throws IOException {
        File file = null;
        if (CONTENT_TYPE_FILE.equals(contentType)) {
            if (inputFile!=null && !inputFile.isEmpty()) {
                file = this.fileManager.getFileFromInputStream(parentFolder, inputFile.getInputStream(), null, inputFile.getOriginalFilename());
            }
        } else if (CONTENT_TYPE_URI.equals(contentType)) {
            if (!inputUri.isEmpty()) {
                file = this.fileManager.getFileFromURL(parentFolder, inputUri);
            }
        }
        return file;
    }

    /**
     * Load a stream from the provided input.
     *
     * @param contentType The type of input provided.
     * @param file The file.
     * @param uri The URI.
     * @param string The text content
     * @return The stream to read.
     */
    private InputStream getInputStream(String contentType, MultipartFile file, String uri, String string) throws IOException {
        InputStream is = null;
        switch (contentType) {
            case CONTENT_TYPE_FILE:
                is = file.getInputStream();
                break;
            case CONTENT_TYPE_URI:
                is = this.fileManager.getInputStreamFromURL(uri);
                break;
            case CONTENT_TYPE_STRING:
                is = new ByteArrayInputStream(string.getBytes());
                break;
        }
        return is;
    }

}
