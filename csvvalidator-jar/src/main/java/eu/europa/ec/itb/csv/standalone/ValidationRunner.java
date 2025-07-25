/*
 * Copyright (C) 2025 European Union
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an
 * "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for
 * the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.itb.csv.standalone;

import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.InputHelper;
import eu.europa.ec.itb.csv.validation.*;
import eu.europa.ec.itb.validation.commons.CsvReportGenerator;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.LocalisationHelper;
import eu.europa.ec.itb.validation.commons.Utils;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import eu.europa.ec.itb.validation.commons.jar.BaseValidationRunner;
import eu.europa.ec.itb.validation.commons.jar.FileReport;
import eu.europa.ec.itb.validation.commons.jar.ValidationInput;
import eu.europa.ec.itb.validation.commons.report.ReportGeneratorBean;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
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

    private static final String FLAG_NO_REPORTS = "-noreports";
    private static final String FLAG_VALIDATION_TYPE = "-"+ValidationConstants.INPUT_VALIDATION_TYPE;
    private static final String FLAG_INPUT = "-input";
    private static final String FLAG_SCHEMA = "-"+ValidationConstants.INPUT_EXTERNAL_SCHEMA;
    private static final String FLAG_NO_HEADERS = "-noHeaders";
    private static final String FLAG_DELIMITER = "-"+ValidationConstants.INPUT_DELIMITER;
    private static final String FLAG_QUOTE = "-"+ValidationConstants.INPUT_QUOTE;
    private static final String FLAG_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL;
    private static final String FLAG_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL;
    private static final String FLAG_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL;
    private static final String FLAG_FIELD_CASE_MISMATCH_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL;
    private static final String FLAG_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL;
    private static final String FLAG_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL;
    private static final String FLAG_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL = "-"+ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL;
    private static final String FLAG_LOCALE = "-locale";
    private static final String PLACEHOLDER_VIOLATION_LEVEL = "VIOLATION_LEVEL";

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private InputHelper inputHelper;
    @Autowired
    private FileManager fileManager;
    @Autowired
    private ReportGeneratorBean reportGenerator;
    @Autowired
    private CsvReportGenerator csvReportGenerator;

    /**
     * Run the validation.
     *
     * @param args The command-line arguments.
     * @param parentFolder The temporary folder to use for this validator's run.
     */
    @Override
    protected void bootstrapInternal(String[] args, File parentFolder) {
        // Process input arguments
        boolean typeRequired = domainConfig.hasMultipleValidationTypes() && domainConfig.getDefaultType() == null;
        List<ValidationInput> inputs = new ArrayList<>();
        List<FileInfo> externalSchemaFileInfo = Collections.emptyList();
        boolean noReports = false;
        String validationType = null;
        Boolean inputHeaders = null;
        String inputDelimiter = null;
        String inputQuote = null;
        String locale = null;
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
                if (FLAG_NO_REPORTS.equalsIgnoreCase(args[i])) {
                    noReports = true;
                } else if (FLAG_VALIDATION_TYPE.equalsIgnoreCase(args[i])) {
                    validationType = argumentAsString(args, i);
                } else if (FLAG_INPUT.equalsIgnoreCase(args[i])) {
                    if (args.length > i+1) {
                        String path = args[++i];
                        inputs.add(new ValidationInput(getContent(path, parentFolder, domainConfig.getHttpVersion()), path));
                    }
                } else if (FLAG_SCHEMA.equalsIgnoreCase(args[i])) {
                    if (args.length > i + 1) {
                        externalSchemaFileInfo = List.of(new FileInfo(getContent(args[++i], parentFolder, domainConfig.getHttpVersion())));
                    }
                } else if (FLAG_NO_HEADERS.equalsIgnoreCase(args[i])) {
                    inputHeaders = Boolean.FALSE;
                } else if (FLAG_DELIMITER.equalsIgnoreCase(args[i])) {
                    inputDelimiter = argumentAsString(args, i);
                } else if (FLAG_QUOTE.equalsIgnoreCase(args[i])) {
                    inputQuote = argumentAsString(args, i);
                } else if (FLAG_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputDifferentInputFieldCountViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputDifferentInputFieldSequenceViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputDuplicateInputFieldsViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG_FIELD_CASE_MISMATCH_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputFieldCaseMismatchViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputMultipleInputFieldsForSchemaFieldViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputUnknownInputViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL.equalsIgnoreCase(args[i])) {
                    inputUnspecifiedSchemaFieldViolationLevel = argumentAsViolationLevel(args, i);
                } else if (FLAG_LOCALE.equalsIgnoreCase(args[i]) && args.length > i+1) {
                    locale = args[++i];
                }
                i++;
            }
            validationType = inputHelper.validateValidationType(domainConfig, validationType);
        } catch (ValidatorException e) {
            LOGGER_FEEDBACK.info("\nInvalid arguments provided: {}\n", e.getMessageForDisplay(new LocalisationHelper(domainConfig, Locale.ENGLISH)));
            LOGGER.error("Invalid arguments provided: {}", e.getMessageForLog(), e);
            inputs.clear();
        } catch (IllegalArgumentException e) {
            LOGGER_FEEDBACK.info("\nInvalid arguments provided: {}\n", e.getMessage());
            LOGGER.error("Invalid arguments provided: {}", e.getMessage(), e);
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
            var localiser = new LocalisationHelper(domainConfig, Utils.getSupportedLocale(LocaleUtils.toLocale(locale), domainConfig));
            for (ValidationInput input: inputs) {
                LOGGER_FEEDBACK.info("\nValidating {} of {} ...", i+1, inputs.size());
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
                    CSVValidator validator = ctx.getBean(CSVValidator.class, ValidationSpecs.builder(input.getInputFile(), inputHelper.buildCSVSettings(domainConfig, validationType, settingInputs), localiser, domainConfig)
                            .withValidationType(validationType)
                            .withExternalSchemas(externalSchemaFileInfo)
                            .withProgressListener(this)
                            .build());
                    var report = validator.validate().getDetailedReport();
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
                                // Create PDF and CSV reports
                                File pdfReportFile = new File(xmlReportFile.getParentFile(), "report."+i+".pdf");
                                File csvReportFile = new File(xmlReportFile.getParentFile(), "report."+i+".csv");
                                Files.deleteIfExists(pdfReportFile.toPath());
                                Files.deleteIfExists(csvReportFile.toPath());
                                reportGenerator.writeReport(xmlReportFile, pdfReportFile, localiser, domainConfig.isRichTextReports());
                                csvReportGenerator.writeReport(xmlReportFile, csvReportFile, localiser, domainConfig);
                                summary.append("- Detailed reports in [").append(xmlReportFile.getAbsolutePath()).append("], [").append(pdfReportFile.getAbsolutePath()).append("] and [").append(csvReportFile.getAbsolutePath()).append("]\n");
                            } else if (report.getCounters() != null && (report.getCounters().getNrOfAssertions().longValue() + report.getCounters().getNrOfErrors().longValue() + report.getCounters().getNrOfWarnings().longValue()) <= domainConfig.getMaximumReportsForXmlOutput()) {
                                summary.append("- Detailed report in [").append(xmlReportFile.getAbsolutePath()).append("] (PDF and CSV reports skipped due to large number of report items) \n");
                            } else {
                                summary.append("- Detailed report in [").append(xmlReportFile.getAbsolutePath()).append("] (report limited to first ").append(domainConfig.getMaximumReportsForXmlOutput()).append(" items, and skipped PDF and CSV reports) \n");
                            }
                        }
                    }
                } catch (ValidatorException e) {
                    LOGGER_FEEDBACK.info("\nAn error occurred while executing the validation: {}", e.getMessageForDisplay(localiser));
                    LOGGER.error("An error occurred while executing the validation: {}", e.getMessageForLog(), e);
                    break;
                } catch (Exception e) {
                    LOGGER_FEEDBACK.info("\nAn error occurred while executing the validation.");
                    LOGGER.error("An error occurred while executing the validation: {}", e.getMessage(), e);
                    break;
                }
                i++;
                LOGGER_FEEDBACK.info(" Done.");
            }
            var summaryString = summary.toString();
            LOGGER_FEEDBACK.info(summaryString);
            LOGGER_FEEDBACK_FILE.info(summaryString);
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
     * @param httpVersion The HTTP version to use.
     * @return The file with the CSV content to use for the validation.
     * @throws IOException If an IO error occurs.
     */
    private File getContent(String contentPath, File parentFolder, HttpClient.Version httpVersion) throws IOException {
        File fileToUse;
        if (isValidURL(contentPath)) {
            // Value is a URL.
            try {
                fileToUse = fileManager.getFileFromURL(parentFolder, contentPath, httpVersion);
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
        usageMessage.append("\nExpected usage: java -jar validator.jar ").append(FLAG_INPUT).append(" FILE_OR_URI_1 ... [").append(FLAG_INPUT).append(" FILE_OR_URI_N] [").append(FLAG_NO_REPORTS).append("] [").append(FLAG_LOCALE).append(" LOCALE]");
        if (requireType) {
            usageMessage.append(" [").append(FLAG_VALIDATION_TYPE).append(" VALIDATION_TYPE]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- VALIDATION_TYPE is the type of validation to perform, one of [").append(String.join("|", domainConfig.getType())).append("].");
        } else if (domainConfig.hasMultipleValidationTypes()) {
            usageMessage.append(" [").append(FLAG_VALIDATION_TYPE).append(" VALIDATION_TYPE]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- VALIDATION_TYPE is the type of validation to perform, one of [").append(String.join("|", domainConfig.getType())).append("] (default is ").append(domainConfig.getDefaultType()).append(").");
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForHeader())) {
            usageMessage.append(" [").append(FLAG_NO_HEADERS).append("]");
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDelimiter())) {
            usageMessage.append(" [").append(FLAG_DELIMITER).append(" DELIMITER]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- DELIMITER is the delimiter character to use.");
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForQuote())) {
            usageMessage.append(" [").append(FLAG_QUOTE).append(" QUOTE]");
            parametersMessage.append("\n").append(PAD).append(PAD).append("- QUOTE is the quote character to use.");
        }
        boolean hasViolationLevel = false;
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDifferentInputFieldCount())) {
            usageMessage.append(" [").append(FLAG_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL).append(" ").append(PLACEHOLDER_VIOLATION_LEVEL).append("]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDifferentInputFieldSequence())) {
            usageMessage.append(" [").append(FLAG_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL).append(" ").append(PLACEHOLDER_VIOLATION_LEVEL).append("]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForDuplicateInputFields())) {
            usageMessage.append(" [").append(FLAG_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL).append(" ").append(PLACEHOLDER_VIOLATION_LEVEL).append("]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForInputFieldCaseMismatch())) {
            usageMessage.append(" [").append(FLAG_FIELD_CASE_MISMATCH_VIOLATION_LEVEL).append(" ").append(PLACEHOLDER_VIOLATION_LEVEL).append("]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForMultipleInputFieldsForSchemaField())) {
            usageMessage.append(" [").append(FLAG_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL).append(" ").append(PLACEHOLDER_VIOLATION_LEVEL).append("]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForUnknownInputField())) {
            usageMessage.append(" [").append(FLAG_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL).append(" ").append(PLACEHOLDER_VIOLATION_LEVEL).append("]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypesWithSettingInputs(domainConfig.getCsvOptions().getUserInputForUnspecifiedSchemaField())) {
            usageMessage.append(" [").append(FLAG_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL).append(" ").append(PLACEHOLDER_VIOLATION_LEVEL).append("]");
            hasViolationLevel = true;
        }
        if (domainConfig.definesTypeWithExternalSchemas()) {
            usageMessage.append(" [").append(FLAG_SCHEMA).append(" SCHEMA_FILE_OR_URI]");
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
        usageMessage.append("\n").append(PAD).append(PAD).append("- LOCALE is the language code to consider for reporting of results. If the provided locale is not supported by the validator the default locale will be used instead (e.g. 'fr', 'fr_FR').");
        usageMessage.append(parametersMessage);
        usageMessage.append("\n\nThe summary of each validation will be printed and the detailed reports produced in the current directory (as \"report.X.xml\", \"report.X.pdf\" and \"report.X.csv\").");
        System.out.println(usageMessage);
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
            LOGGER.info("Validated {} records", counter);
        }
    }

    /**
     * Log the end of the schema validation.
     *
     * @param counter The final number of lined processed.
     */
    @Override
    public void schemaValidationEnd(long counter) {
        LOGGER.info("Validated {} records", counter);
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
