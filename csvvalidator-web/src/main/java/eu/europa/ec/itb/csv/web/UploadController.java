package eu.europa.ec.itb.csv.web;

import com.gitb.tr.TAR;
import eu.europa.ec.itb.csv.ApplicationConfig;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.DomainConfigCache;
import eu.europa.ec.itb.csv.InputHelper;
import eu.europa.ec.itb.csv.validation.CSVValidator;
import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.csv.validation.ValidationConstants;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.ValidatorChannel;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import eu.europa.ec.itb.validation.commons.web.errors.NotFoundException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
public class UploadController {

    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);
    private static final String IS_MINIMAL              = "isMinimal";
    private static final String CONTENT_TYPE__FILE      = "fileType" ;
    private static final String CONTENT_TYPE__URI       = "uriType" ;
    private static final String CONTENT_TYPE__STRING    = "stringType" ;

    @Autowired
    private DomainConfigCache domainConfigs = null;
    @Autowired
    private ApplicationConfig appConfig = null;
    @Autowired
    private FileManager fileManager = null;
    @Autowired
    private InputHelper inputHelper = null;
    @Autowired
    private BeanFactory beans = null;

    @RequestMapping(method = RequestMethod.GET, value = "/{domain}/upload")
    public ModelAndView upload(@PathVariable("domain") String domain, Model model, HttpServletRequest request) {
        setMinimalUIFlag(request, false);
        DomainConfig config = domainConfigs.getConfigForDomainName(domain);
        if (config == null || !config.getChannels().contains(ValidatorChannel.FORM)) {
            throw new NotFoundException();
        }
        MDC.put("domain", domain);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("config", config);
        attributes.put("appConfig", appConfig);
        attributes.put("minimalUI", false);
        attributes.put("externalArtifactInfo", config.getExternalArtifactInfoMap());
        return new ModelAndView("uploadForm", attributes);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{domain}/upload")
    public ModelAndView handleUpload(@PathVariable("domain") String domain,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "uri", defaultValue = "") String uri,
                                     @RequestParam(value = "text-editor", defaultValue = "") String string,
                                     @RequestParam(value = "validationType", defaultValue = "") String validationType,
                                     @RequestParam(value = "contentType", defaultValue = "") String contentType,
                                     @RequestParam(value = "contentType-external_default", required = false) String[] externalSchemaContentType,
                                     @RequestParam(value = "inputFile-external_default", required = false) MultipartFile[] externalSchemaFiles,
                                     @RequestParam(value = "uri-external_default", required = false) String[] externalSchemaUri,
                                     @RequestParam(value = "csvSettingsCheck", required = false, defaultValue = "false") Boolean csvSettingsCheck,
                                     @RequestParam(value = "inputHeaders", required = false, defaultValue = "false") Boolean inputHeaders,
                                     @RequestParam(value = "inputDelimiter", required = false) String inputDelimiter,
                                     @RequestParam(value = "inputQuote", required = false) String inputQuote,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {
        setMinimalUIFlag(request, false);
        DomainConfig domainConfig = domainConfigs.getConfigForDomainName(domain);
        if (domainConfig == null || !domainConfig.getChannels().contains(ValidatorChannel.FORM)) {
            throw new NotFoundException();
        }
        MDC.put("domain", domain);
        InputStream stream = null;
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("config", domainConfig);
        attributes.put("minimalUI", false);
        attributes.put("externalArtifactInfo", domainConfig.getExternalArtifactInfoMap());

        if (StringUtils.isNotBlank(validationType)) {
            attributes.put("validationTypeLabel", domainConfig.getTypeLabel().get(validationType));
        }
        attributes.put("appConfig", appConfig);
        try (InputStream fis = getInputStream(contentType, file.getInputStream(), uri, string)) {
            if (fileManager.checkFileType(fis)) {
                stream = getInputStream(contentType, file.getInputStream(), uri, string);
            } else {
                attributes.put("message", "Provided input is not a CSV file");
            }
        } catch (IOException e) {
            LOG.error("Error while reading uploaded file [" + e.getMessage() + "]", e);
            attributes.put("message", "Error in upload [" + e.getMessage() + "]");
        }
        if (StringUtils.isBlank(validationType)) {
            validationType = domainConfig.getType().get(0);
        }
        if (domainConfig.hasMultipleValidationTypes() && (validationType == null || !domainConfig.getType().contains(validationType))) {
            // A validation type is required.
            attributes.put("message", "Provided validation type is not valid");
        }
        File tempFolderForRequest = fileManager.createTemporaryFolderPath();
        try {
            if (stream != null) {
                File contentToValidate = fileManager.getFileFromInputStream(tempFolderForRequest, stream, null, UUID.randomUUID().toString()+".json");
                List<FileInfo> externalSchemas = new ArrayList<>();
                boolean proceedToValidate = true;
                try {
                    externalSchemas = getExternalFiles(externalSchemaContentType, externalSchemaFiles, externalSchemaUri, domainConfig.getSchemaInfo(validationType), validationType, tempFolderForRequest);
                } catch (Exception e) {
                    LOG.error("Error while reading uploaded file [" + e.getMessage() + "]", e);
                    attributes.put("message", "Error in upload [" + e.getMessage() + "]");
                    proceedToValidate = false;
                }
                if (proceedToValidate) {
                    if (!csvSettingsCheck) {
                        inputHeaders = null;
                        inputDelimiter = null;
                        inputQuote = null;
                    }
                    CSVValidator validator = beans.getBean(CSVValidator.class, contentToValidate, validationType, externalSchemas, domainConfig, inputHelper.buildCSVSettings(domainConfig, validationType, inputHeaders, inputDelimiter, inputQuote));
                    TAR report = validator.validate();
                    attributes.put("report", report);
                    attributes.put("date", report.getDate().toString());
                    if (contentType.equals(CONTENT_TYPE__FILE)) {
                        attributes.put("fileName", file.getOriginalFilename());
                    } else if(contentType.equals(CONTENT_TYPE__URI)) {
                        attributes.put("fileName", uri);
                    } else {
                        attributes.put("fileName", "-");
                    }
                    // Cache detailed report.
                    try {
                        String inputID = fileManager.writeCSV(domainConfig.getDomainName(), contentToValidate);
                        attributes.put("inputID", inputID);
                        fileManager.saveReport(report, inputID);
                    } catch (IOException e) {
                        LOG.error("Error generating detailed report [" + e.getMessage() + "]", e);
                        attributes.put("message", "Error generating detailed report: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("An error occurred during the validation [" + e.getMessage() + "]", e);
            attributes.put("message", "An error occurred during the validation: " + e.getMessage());
        } finally {
            // Cleanup temporary resources for request.
            if (tempFolderForRequest.exists()) {
                FileUtils.deleteQuietly(tempFolderForRequest);
            }
        }
        return new ModelAndView("uploadForm", attributes);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{domain}/uploadm")
    public ModelAndView uploadm(@PathVariable("domain") String domain, Model model, HttpServletRequest request) {
        setMinimalUIFlag(request, true);

        DomainConfig config = domainConfigs.getConfigForDomainName(domain);
        if (config == null || !config.getChannels().contains(ValidatorChannel.FORM)) {
            throw new NotFoundException();
        }

        if(!config.isSupportMinimalUserInterface()) {
            LOG.error("Minimal user interface is not supported in this domain [" + domain + "].");
            throw new NotFoundException();
        }

        MDC.put("domain", domain);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("config", config);
        attributes.put("appConfig", appConfig);
        attributes.put("minimalUI", true);
        attributes.put("externalArtifactInfo", config.getExternalArtifactInfoMap());
        return new ModelAndView("uploadForm", attributes);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{domain}/uploadm")
    public ModelAndView handleUploadM(@PathVariable("domain") String domain,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "uri", defaultValue = "") String uri,
                                      @RequestParam(value = "text-editor", defaultValue = "") String string,
                                      @RequestParam(value = "validationType", defaultValue = "") String validationType,
                                      @RequestParam(value = "contentType", defaultValue = "") String contentType,
                                      @RequestParam(value = "contentType-external_default", required = false) String[] externalSchema,
                                      @RequestParam(value = "inputFile-external_default", required= false) MultipartFile[] externalSchemaFiles,
                                      @RequestParam(value = "uriToValidate-external_default", required = false) String[] externalSchemaUri,
                                      @RequestParam(value = "csvSettingsCheck", required = false, defaultValue = "false") Boolean csvSettingsCheck,
                                      @RequestParam(value = "inputHeaders", required = false, defaultValue = "false") Boolean inputHeaders,
                                      @RequestParam(value = "inputDelimiter", required = false) String inputDelimiter,
                                      @RequestParam(value = "inputQuote", required = false) String inputQuote,
                                      RedirectAttributes redirectAttributes,
                                      HttpServletRequest request) {

        setMinimalUIFlag(request, true);
        ModelAndView mv = handleUpload(domain, file, uri, string, validationType, contentType, externalSchema, externalSchemaFiles, externalSchemaUri, csvSettingsCheck, inputHeaders, inputDelimiter, inputQuote, redirectAttributes, request);
        Map<String, Object> attributes = mv.getModel();
        attributes.put("minimalUI", true);
        return new ModelAndView("uploadForm", attributes);
    }

    private List<FileInfo> getExternalFiles(String[] externalContentType, MultipartFile[] externalFiles, String[] externalUri,
                                            ValidationArtifactInfo schemaInfo, String validationType, File parentFolder) throws Exception {
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
            throw new Exception("An error occurred during the validation of the external Schema(s).");
        }

    }

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

    private File getInputFile(String contentType, MultipartFile inputFile, String inputUri, File parentFolder) throws IOException {
        File f = null;
        switch (contentType) {
            case CONTENT_TYPE__FILE:
                if (inputFile!=null && !inputFile.isEmpty()) {
                    f = this.fileManager.getFileFromInputStream(parentFolder, inputFile.getInputStream(), null, inputFile.getOriginalFilename());
                }
                break;
            case CONTENT_TYPE__URI:
                if (!inputUri.isEmpty()) {
                    f = this.fileManager.getFileFromURL(parentFolder, inputUri);
                }
                break;
        }
        return f;
    }

    private void setMinimalUIFlag(HttpServletRequest request, boolean isMinimal) {
        if (request.getAttribute(IS_MINIMAL) == null) {
            request.setAttribute(IS_MINIMAL, isMinimal);
        }
    }

    private InputStream getInputStream(String contentType, InputStream inputStream, String uri, String string) {
        InputStream is = null;
        switch (contentType) {
            case CONTENT_TYPE__FILE:
                is = inputStream;
                break;
            case CONTENT_TYPE__URI:
                is = this.fileManager.getInputStreamFromURL(uri);
                break;
            case CONTENT_TYPE__STRING:
                is = new ByteArrayInputStream(string.getBytes());
                break;
        }
        return is;
    }

}
