package eu.europa.ec.itb.csv.standalone;

import com.gitb.tr.TAR;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.InputHelper;
import eu.europa.ec.itb.csv.validation.*;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.LocalisationHelper;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import eu.europa.ec.itb.validation.commons.jar.BaseValidationRunner;
import eu.europa.ec.itb.validation.commons.jar.FileReport;
import eu.europa.ec.itb.validation.commons.jar.ValidationInput;
import eu.europa.ec.itb.validation.commons.report.ReportGeneratorBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Component that handles the actual triggering of validation and resulting reporting.
 */
@Component
@Scope("prototype")
public class ValidationRunner extends BaseValidationRunner<DomainConfig> implements ProgressListener {

    private static final String FLAG__NO_REPORTS = "-noreports";
    private static final String FLAG__VALIDATION_TYPE = "-"+ValidationConstants.INPUT_VALIDATION_TYPE;
    private static final String FLAG__INPUT = "-input";
    private static final String FLAG__SCHEMA = "-"+ValidationConstants.INPUT_EXTERNAL_SCHEMA;
    private static final String FLAG__NO_HEADERS = "-noHeaders";
    private static final String FLAG__DELIMITER = "-"+ValidationConstants.INPUT_DELIMITER;
    private static final String FLAG__QUOTE = "-"+ValidationConstants.INPUT_QUOTE;
    private static final String FLAG__DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL;
    private static final String FLAG__DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL;
    private static final String FLAG__DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL;
    private static final String FLAG__FIELD_CASE_MISMATCH_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL;
    private static final String FLAG__MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL;
    private static final String FLAG__UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL;
    private static final String FLAG__UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL;

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private InputHelper inputHelper;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ReportGeneratorBean reportGenerator;

    /**
     * Run the validation.
     *
     * @param args The command-line arguments.
     * @param parentFolder The temporary folder to use for this validator's run.
     */
    @Override
    protected void bootstrapInternal(String[] args, File parentFolder) {
        // Process input arguments
        boolean typeRequired = domainConfig.hasMultipleValidationTypes();
        List<ValidationInput> inputs = new ArrayList<>();
        List<FileInfo> externalSchemaFileInfo = Collections.emptyList();
        boolean noReports = false;
        String validationType = null;
        Boolean inputHeaders = null;
        String inputDelimiter = null;
        String inputQuote = null;
        ViolationLevel inputDifferentInputFieldCountViolationLevel = null;
        ViolationLevel inputDifferentInputFieldSequenceViolationLevel = null;
        ViolationLevel inputDuplicateInputFieldsViolationLevel = null;
        ViolationLevel inputFieldCaseMismatchViolationLevel = null;
        ViolationLevel inputMultipleInputFieldsForSchemaFieldViolationLevel = null;
        ViolationLevel inputUnknownInputViolationLevel = null;
        ViolationLevel inputUnspecifiedSchemaFieldViolationLevel = null;
        try {
            int i = 0;
            while (i < args.length) {
                if (FLAG__NO_REPORTS.equalsIgnoreCase(args[i])) {
                    noReports = true;
                } else if (FLAG__VALIDATION_TYPE.equalsIgnoreCase(args[i])) {
                    validationType = argumentAsString(args, i);
                    if (validationType != null && !domainConfig.getType().contains(validationType)) {
                        throw new IllegalArgumentException("Unknown validation type. One of [" + String.join("|", domainConfig.getType()) + "] is needed.");
                    }
                } else if (FLAG__INPUT.equalsIgnoreCase(args[i])) {
                    if (args.length > i+1) {
                        String path = args[++i];
                        inputs.add(new ValidationInput(getContent(path, parentFolder), path));
                    }
                } else if (FLAG__SCHEMA.equalsIgnoreCase(args[i])) {
                    if (args.length > i + 1) {
                        externalSchemaFileInfo = List.of(new FileInfo(getContent(args[++i], parentFolder)));
                    }
                } else if (FLAG__NO_HEADERS.equalsIgnoreCase(args[i])) {
                    inputHeaders = Boolean.FALSE;
                } else if (FLAG__DELIMITER.equalsIgnoreCase(args[i])) {
                    inputDelimiter = argumentAsString(args, i);
                } else if (FLAG__QUOTE.equalsIgnoreCase(args[i])) {
                    inputQuote = argumentAsString(args, i);
                } else if (FLAG__DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputDifferentInputFieldCountViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG__DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputDifferentInputFieldSequenceViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG__DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputDuplicateInputFieldsViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG__FIELD_CASE_MISMATCH_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputFieldCaseMismatchViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG__MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputMultipleInputFieldsForSchemaFieldViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG__UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputUnknownInputViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG__UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputUnspecifiedSchemaFieldViolationLevel = argumentAsViolationLevel(args, i);
                }
                i++;
            }
            if (validationType == null) {
                if (typeRequired) {
                    throw new IllegalArgumentException("Unknown validation type. One of [" + String.join("|", domainConfig.getType()) + "] is needed.");
                } else {
                    validationType = domainConfig.getType().get(0);
                }
            }
        } catch (ValidatorException e) {
            LOGGER_FEEDBACK.info("\nInvalid arguments provided: "+e.getMessageForDisplay(new LocalisationHelper(domainConfig, Locale.ENGLISH))+"\n");
            LOGGER.error("Invalid arguments provided: "+e.getMessageForLog(), e);
            inputs.clear();
        } catch (IllegalArgumentException e) {
            LOGGER_FEEDBACK.info("\nInvalid arguments provided: "+e.getMessage()+"\n");
            LOGGER.error("Invalid arguments provided: "+e.getMessage(), e);
            inputs.clear();
        } catch (Exception e) {
            LOGGER_FEEDBACK.info("\nAn error occurred while processing the provided arguments.\n");
            LOGGER.error("An error occurred while processing the provided arguments.", e);
            inputs.clear();
        }
        if (inputs.isEmpty()) {
            printUsage(typeRequired);
        } else {
            // Do validation
            StringBuilder summary = new StringBuilder();
            summary.append("\n");
            int i = 0;
            for (ValidationInput input: inputs) {
                LOGGER_FEEDBACK.info(String.format("\nValidating %s of %s ...", i+1, inputs.size()));
                try {
                    InputHelper.Inputs settingInputs = InputHelper.Inputs.newInstance()
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
                    CSVValidator validator = ctx.getBean(CSVValidator.class, input.getInputFile(), validationType, externalSchemaFileInfo, domainConfig, inputHelper.buildCSVSettings(domainConfig, validationType, settingInputs), this, new LocalisationHelper(domainConfig, Locale.ENGLISH));
                    TAR report = validator.validate();
                    if (report == null) {
                        summary.append("\nNo validation report was produced.\n");
                    } else {
                        int itemCount = 0;
                        if (report.getReports() != null && report.getReports().getInfoOrWarningOrError() != null) {
                            itemCount = report.getReports().getInfoOrWarningOrError().size();
                        }
                        FileReport reportData = new FileReport(input.getFileName(), report, typeRequired, validationType);
                        summary.append("\n").append(reportData).append("\n");
                        if (!noReports) {
                            File xmlReportFile = new File(Path.of(System.getProperty("user.dir")).toFile(), "report."+i+".xml");
                            Files.deleteIfExists(xmlReportFile.toPath());
                            // Create XML report
                            fileManager.saveReport(report, xmlReportFile, domainConfig);
                            if (itemCount <= domainConfig.getMaximumReportsForDetailedOutput()) {
                                // Create PDF report
                                File pdfReportFile = new File(xmlReportFile.getParentFile(), "report."+i+".pdf");
                                Files.deleteIfExists(pdfReportFile.toPath());
                                reportGenerator.writeReport(xmlReportFile, pdfReportFile, new LocalisationHelper(domainConfig, Locale.ENGLISH));
                                summary.append("- Detailed reports in [").append(xmlReportFile.getAbsolutePath()).append("] and [").append(pdfReportFile.getAbsolutePath()).append("] \n");
                            } else if (report.getCounters() != null && (report.getCounters().getNrOfAssertions().longValue() + report.getCounters().getNrOfErrors().longValue() + report.getCounters().getNrOfWarnings().longValue()) <= domainConfig.getMaximumReportsForXmlOutput()) {
                                summary.append("- Detailed report in [").append(xmlReportFile.getAbsolutePath()).append("] (PDF report skipped due to large number of report items) \n");
                            } else {
                                summary.append("- Detailed report in [").append(xmlReportFile.getAbsolutePath()).append("] (report limited to first ").append(domainConfig.getMaximumReportsForXmlOutput()).append(" items and PDF report skipped) \n");
                            }
                        }
                    }
                } catch (ValidatorException e) {
                    LOGGER_FEEDBACK.info("\nAn error occurred while executing the validation: "+e.getMessageForDisplay(new LocalisationHelper(domainConfig, Locale.ENGLISH)));
                    LOGGER.error("An error occurred while executing the validation: "+e.getMessageForLog(), e);
                    break;
                } catch (Exception e) {
                    LOGGER_FEEDBACK.info("\nAn error occurred while executing the validation.");
                    LOGGER.error("An error occurred while executing the validation: "+e.getMessage(), e);
                    break;
                }
                i++;
                LOGGER_FEEDBACK.info(" Done.");
            }
            LOGGER_FEEDBACK.info(summary.toString());
            LOGGER_FEEDBACK_FILE.info(summary.toString());
        }
    }

    /**
     * Get the input argument for the provided counter as a violation level.
     *
     * @param args All command-line inputs.
     * @param argCounter The index of the argument to look for.
     * @return The violation level.
     */
    private ViolationLevel argumentAsViolationLevel(String[] args, int argCounter) {
        return ViolationLevel.byName(argumentAsString(args, argCounter));
    }

    /**
     * Get the CSV content to validate based ont he provided path (can be a URL or file reference).
     *
     * @param contentPath The path to process.
     * @param parentFolder The validation run's temporary folder.
     * @return The file with the CSV content to use for the validation.
     * @throws IOException If an IO error occurs.
     */
    private File getContent(String contentPath, File parentFolder) throws IOException {
        File fileToUse;
        if (isValidURL(contentPath)) {
            // Value is a URL.
            try {
                fileToUse = fileManager.getFileFromURL(parentFolder, contentPath);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to read file from URL ["+contentPath+"]");
            }
        } else {
            // Value is a local file. Copy this in the tmp folder as we may later be changing it (e.g. encoding updates).
            Path inputFile = Paths.get(contentPath);
            if (!Files.exists(inputFile) || !Files.isRegularFile(inputFile) || !Files.isReadable(inputFile)) {
                throw new IllegalArgumentException("Unable to read file ["+contentPath+"]");
            }
            Path finalInputFile = Paths.get(parentFolder.getAbsolutePath(), inputFile.getFileName().toString());
            Files.createDirectories(finalInputFile.getParent());
            fileToUse = Files.copy(inputFile, finalInputFile).toFile();
        }
        return fileToUse;
    }

    /**
     * Print the usage string for the validator.
     *
     * @param requireType True if the validation type should be included in the message.
     */
    private void printUsage(boolean requireType) {
        StringBuilder usageMessage = new StringBuilder();
        StringBuilder parametersMessage = new StringBuilder();
        usageMessage.append("\nExpected usage: java -jar validator.jar ").append(FLAG__INPUT).append(" FILE_OR_URI_1 ... [").append(FLAG__INPUT).append(" FILE_OR_URI_N] [").append(FLAG__NO_REPORTS).append("]");
        if (requireType) {
            usageMessage.append(" [").append(FLAG__VALIDATION_TYPE).append(" VALIDATION_TYPE]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- VALIDATION_TYPE is the type of validation to perform, one of [").append(String.join("|", domainConfig.getType())).append("].");
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForHeader())) {
            usageMessage.append(" [").append(FLAG__NO_HEADERS).append("]");
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDelimiter())) {
            usageMessage.append(" [").append(FLAG__DELIMITER).append(" DELIMITER]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- DELIMITER is the delimiter character to use.");
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForQuote())) {
            usageMessage.append(" [").append(FLAG__QUOTE).append(" QUOTE]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- QUOTE is the quote character to use.");
        }
        boolean hasViolationLevel = false;
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount())) {
            usageMessage.append(" [").append(FLAG__DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL).append(" VIOLATION_LEVEL]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDifferentInputFieldSequence())) {
            usageMessage.append(" [").append(FLAG__DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL).append(" VIOLATION_LEVEL]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDuplicateInputFields())) {
            usageMessage.append(" [").append(FLAG__DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL).append(" VIOLATION_LEVEL]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForInputFieldCaseMismatch())) {
            usageMessage.append(" [").append(FLAG__FIELD_CASE_MISMATCH_VIOLATION_LEVEL).append(" VIOLATION_LEVEL]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForMultipleInputFieldsForSchemaField())) {
            usageMessage.append(" [").append(FLAG__MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL).append(" VIOLATION_LEVEL]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForUnknownInputField())) {
            usageMessage.append(" [").append(FLAG__UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL).append(" VIOLATION_LEVEL]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForUnspecifiedSchemaField())) {
            usageMessage.append(" [").append(FLAG__UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL).append(" VIOLATION_LEVEL]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypeWithExternalSchemas()) {
            usageMessage.append(" [").append(FLAG__SCHEMA).append(" SCHEMA_FILE_OR_URI]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- SCHEMA_FILE_OR_URI is the full file path or URI to a schema for the validation.");
        }
        if (hasViolationLevel) {
            parametersMessage.append("\n").append(PAD).append(PAD).append("- VIOLATION_LEVEL is the level to consider for detected violations. Value is one of [")
                    .append(ViolationLevel.ERROR.getName()).append("|")
                    .append(ViolationLevel.WARNING.getName()).append("|")
                    .append(ViolationLevel.INFO.getName()).append("|")
                    .append(ViolationLevel.NONE.getName()).append("].");
        }
        usageMessage.append("\n").append(PAD).append("Where:");
        usageMessage.append("\n").append(PAD).append(PAD).append("- FILE_OR_URI_X is the full file path or URI to the content to validate.");
        usageMessage.append(parametersMessage);
        usageMessage.append("\n\nThe summary of each validation will be printed and the detailed reports produced in the current directory (as \"report.X.xml\" and \"report.X.pdf\").");
        System.out.println(usageMessage.toString());
    }

    /**
     * Log the start of schema validation.
     */
    @Override
    public void schemaValidationStart() {
        LOGGER.info("Starting schema validation");
    }

    /**
     * Log an update to the schema validation.
     *
     * @param counter The number of validated lines.
     */
    @Override
    public void schemaValidationUpdate(long counter) {
        if (counter % 50000 == 0) {
            LOGGER.info(String.format("Validated %s records", counter));
        }
    }

    /**
     * Log the end of the schema validation.
     *
     * @param counter The final number of lined processed.
     */
    @Override
    public void schemaValidationEnd(long counter) {
        LOGGER.info(String.format("Validated %s records", counter));
        LOGGER.info("Finished schema validation");
    }

    /**
     * Log the start of custom plugin validation.
     */
    @Override
    public void pluginValidationStart() {
        LOGGER.info("Starting plugin validation");
    }

    /**
     * Log the end of custom plugin validation.
     */
    @Override
    public void pluginValidationEnd() {
        LOGGER.info("Finished plugin validation");
    }
}
