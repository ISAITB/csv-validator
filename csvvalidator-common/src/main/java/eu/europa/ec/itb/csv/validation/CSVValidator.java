package eu.europa.ec.itb.csv.validation;

import com.gitb.core.AnyContent;
import com.gitb.tr.*;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.LocalisedMessageFormatter;
import eu.europa.ec.itb.csv.MessageFormatter;
import eu.europa.ec.itb.validation.commons.*;
import eu.europa.ec.itb.validation.commons.config.DomainPluginConfigProvider;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import eu.europa.ec.itb.validation.plugin.PluginManager;
import eu.europa.ec.itb.validation.plugin.ValidationPlugin;
import io.frictionlessdata.tableschema.exception.InvalidCastException;
import io.frictionlessdata.tableschema.field.Field;
import io.frictionlessdata.tableschema.schema.Schema;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.xml.bind.JAXBElement;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Component used to carry out the validation of CSV content.
 */
@Component
@Scope("prototype")
public class CSVValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CSVValidator.class);

    @Autowired
    private FileManager fileManager = null;
    @Autowired
    private DomainPluginConfigProvider<DomainConfig> pluginConfigProvider = null;
    @Autowired
    private PluginManager pluginManager = null;

    private final ValidationSpecs specs;
    private final ObjectFactory objectFactory = new ObjectFactory();
    private final Set<String> charsetsToNotConvert = Set.of(StandardCharsets.UTF_16.name(), StandardCharsets.UTF_16BE.name(), StandardCharsets.UTF_16LE.name());
    private final MessageFormatter messageFormatter;
    private long counterErrors = 0L;
    private long counterWarnings = 0L;
    private long counterInformationMessages = 0L;
    private long counterTotalErrors = 0L;
    private long counterTotalWarnings = 0L;
    private long counterTotalInformationMessages = 0L;

    /**
     * Constructor.
     *
     * @param specs The specifications with which to carry out the validation.
     */
    public CSVValidator(ValidationSpecs specs) {
        this.specs = specs;
        this.messageFormatter = new LocalisedMessageFormatter(specs.getLocalisationHelper());
    }

    /**
     * @return The identifier (folder name) of the current configuration domain.
     */
    public String getDomain() {
        return specs.getDomainConfig().getDomain();
    }

    /**
     * @return The requested validation type.
     */
    public String getValidationType() {
        return specs.getValidationType();
    }

    /**
     * Run the validation and return the validation report.
     *
     * @return The validation report.
     */
    public ReportPair validate() {
        TAR overallReportDetailed;
        TAR overallReportAggregated;
        try {
            fileManager.signalValidationStart(specs.getDomainConfig().getDomainName());
            var coreReports = validateInternal();
            overallReportDetailed = coreReports.getDetailedReport();
            overallReportAggregated = coreReports.getAggregateReport();
        } finally {
            fileManager.signalValidationEnd(specs.getDomainConfig().getDomainName());
        }
        TAR pluginReport = validateAgainstPlugins();
        if (pluginReport != null) {
            overallReportDetailed = Utils.mergeReports(new TAR[] {overallReportDetailed, pluginReport});
            if (specs.isProduceAggregateReport()) {
                overallReportAggregated = Utils.mergeReports(new TAR[] {overallReportAggregated, Utils.toAggregatedTAR(pluginReport, specs.getLocalisationHelper())});
            }
        }
        sortReportItems(overallReportDetailed);
        sortReportItems(overallReportAggregated);
        specs.getDomainConfig().applyMetadata(overallReportDetailed, getValidationType());
        specs.getDomainConfig().applyMetadata(overallReportAggregated, getValidationType());
        return new ReportPair(overallReportDetailed, overallReportAggregated);
    }

    /**
     * Sort the items included in the report by their relevant location in the input.
     *
     * @param report The validation report.
     */
    private void sortReportItems(TAR report) {
        if (report != null && report.getReports() != null) {
            report.getReports().getInfoOrWarningOrError().sort((o1, o2) -> {
                String location1 = ((BAR) o1.getValue()).getLocation();
                String location2 = ((BAR) o2.getValue()).getLocation();
                // Should be contentToValidate:LINE:COLUMN
                if (location1 == null && location2 == null) {
                    return 0;
                } else if (location2 == null) {
                    return 1;
                } else if (location1 == null) {
                    return -1;
                } else {
                    String prefix = ValidationConstants.INPUT_CONTENT + ":";
                    boolean location1Matches = location1.startsWith(prefix);
                    boolean location2Matches = location2.startsWith(prefix);
                    if (location1Matches && location2Matches) {
                        int line1 = Integer.parseInt(location1.substring(prefix.length(), location1.lastIndexOf(':')));
                        int line2 = Integer.parseInt(location2.substring(prefix.length(), location2.lastIndexOf(':')));
                        return line1 - line2;
                    } else if (location1Matches) {
                        return 1;
                    } else if (location2Matches) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }
    }

    /**
     * Internal method that drives the validation against configured and user-provided schemas.
     *
     * @return The validation report.
     */
    private ReportPair validateInternal() {
        List<FileInfo> schemaFiles = fileManager.getPreconfiguredValidationArtifacts(specs.getDomainConfig(), specs.getValidationType());
        if (specs.getExternalSchemas() != null) {
            schemaFiles.addAll(specs.getExternalSchemas());
        }
        List<ReportItem> reportItems = new ArrayList<>();
        if (schemaFiles.isEmpty()) {
            LOG.info("No schemas to validate against");
            reportItems = Collections.emptyList();
        } else {
            prepareInputFile(specs.getInput());
            for (FileInfo aSchemaFile: schemaFiles) {
                LOG.info("Validating against [{}]", aSchemaFile.getFile().getName());
                reportItems.addAll(validateAgainstSchema(specs.getInput(), aSchemaFile.getFile()));
                LOG.info("Validated against [{}]", aSchemaFile.getFile().getName());
            }
        }
        return toTAR(reportItems);
    }

    /**
     * Create a stream for the file's contents.
     *
     * @param file The file to open.
     * @param isInput True if this is the validator's input.
     * @return The stream.
     */
    private InputStream toStream(File file, boolean isInput) {
        try {
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new ValidatorException((isInput?"validator.label.exception.unableToReadInputFile":"validator.label.exception.unableToReadInputSchema"), e);
        }
    }

    /**
     * Get the root cause of the provided exception.
     *
     * @param e The exception.
     * @return The root cause.
     */
    private Throwable getRootCause(Throwable e) {
        return getRootCauseInternal(e, new HashSet<>());
    }

    /**
     * Recursive method to lookup the cause of an exception as far as possible.
     *
     * @param e The exception to process.
     * @param alreadyProcessed The set of already processed exceptions to avoid potential infinite loops.
     * @return The root cause.
     */
    private Throwable getRootCauseInternal(Throwable e, Set<Throwable> alreadyProcessed) {
        if (e == null) {
            return null;
        } else {
            alreadyProcessed.add(e);
            if (e.getCause() != null && !alreadyProcessed.contains(e.getCause())) {
                return getRootCauseInternal(e.getCause(), alreadyProcessed);
            } else {
                return e;
            }
        }
    }

    /**
     * Validate the provided input against a single schema file.
     *
     * @param inputFile The input to validate.
     * @param schemaFile The schema.
     * @return The validation report items.
     */
    private List<ReportItem> validateAgainstSchema(File inputFile, File schemaFile) {
        if (specs.getProgressListener() != null) {
            specs.getProgressListener().schemaValidationStart();
        }
        counterErrors = 0L;
        counterWarnings = 0L;
        counterInformationMessages = 0L;
        List<ReportItem> errors = new ArrayList<>();
        var format = CSVFormat.RFC4180.builder()
                .setIgnoreHeaderCase(false)
                .setAllowDuplicateHeaderNames(true)
                .setIgnoreSurroundingSpaces(true)
                .setRecordSeparator("\n")
                .setDelimiter(specs.getCSVSettings().getDelimiter())
                .setQuote(specs.getCSVSettings().getQuote());
        if (specs.getCSVSettings().isHasHeaders()) {
            format = format.setHeader();
        }
        Schema schema;
        try (InputStream schemaStream = toStream(schemaFile, false)) {
            schema = Schema.fromJsonWithOptions(schemaStream, false, Map.of(Schema.SCHEMA_OPTION_JAVA_BASED_DATE_FORMATS, String.valueOf(specs.getDomainConfig().getJavaBasedDateFormats().get(specs.getValidationType()))));
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatorException("validator.label.exception.providedSchemaCouldNotBeParsed", e, getRootCause(e).getMessage());
        }
        short headerLine = 1;
        try (
                Reader inputReader = new BomStrippingReader(toStream(inputFile, true));
                CSVParser parser = new CSVParser(inputReader, format.build())
        ) {
            Map<Integer, Field<?>> inputFieldIndexToSchemaFieldMap = new HashMap<>();
            if (specs.getCSVSettings().isHasHeaders()) {
                Map<String, FieldInfo> fieldMap = new HashMap<>();
                Map<String, Set<String>> lowerCaseFieldNameMap = new HashMap<>();
                int index = 0;
                for (Field<?> field: schema.getFields()) {
                    if (fieldMap.containsKey(field.getName())) {
                        throw new ValidatorException("validator.label.exception.schemaDefinesTwoFieldsWithSameName", field.getName());
                    } else {
                        fieldMap.put(field.getName(), new FieldInfo(field, index));
                        lowerCaseFieldNameMap.computeIfAbsent(field.getName().toLowerCase(), n -> new HashSet<>()).add(field.getName());
                    }
                    index += 1;
                }
                int headerIndex = 0;
                Set<String> matchedSchemaFields = new HashSet<>();
                Set<String> processedHeaders = new HashSet<>();
                Set<String> duplicateHeaders = new HashSet<>();
                Map<String, List<Integer>> fieldNameToHeaderIndexes = new HashMap<>();
                for (String headerName: parser.getHeaderNames()) {
                    FieldInfo fieldInfo = fieldMap.get(headerName);
                    if (fieldInfo == null) {
                        // No matching field with the same exact name.
                        String lowerCaseHeaderName = headerName.toLowerCase();
                        if (!lowerCaseHeaderName.equals(headerName)) {
                            Set<String> otherNames = lowerCaseFieldNameMap.get(headerName.toLowerCase());
                            if (otherNames != null && !otherNames.isEmpty()) {
                                if (otherNames.size() > 1) {
                                    // Header name is ambiguous - matched multiple schema fields.
                                    handleSyntaxViolation(ViolationLevel.ERROR, headerName,"validator.label.syntax.ambiguousHeader", new Object[] { headerName, otherNames }, headerLine, errors);
                                } else {
                                    // Matched header in schema fields but with different casing.
                                    String schemaFieldName = otherNames.iterator().next();
                                    handleSyntaxViolation(specs.getCSVSettings().getInputFieldCaseMismatchViolationLevel(), headerName, "validator.label.syntax.inputFieldCaseMismatch", new Object[] { headerName, schemaFieldName }, headerLine, errors);
                                    fieldInfo = fieldMap.get(schemaFieldName);
                                }
                            }
                        }
                    }
                    if (fieldInfo == null) {
                        // Could not match header in schema fields.
                        handleSyntaxViolation(specs.getCSVSettings().getUnknownInputFieldViolationLevel(), headerName, "validator.label.syntax.unknownInputField", new Object[] { headerName }, headerLine, errors);
                    } else  {
                        if (fieldInfo.getIndex() != headerIndex) {
                            // Header appears in unexpected position.
                            handleSyntaxViolation(specs.getCSVSettings().getDifferentInputFieldSequenceViolationLevel(), headerName,"validator.label.syntax.differentInputFieldSequence", new Object[] { headerName, headerIndex, fieldInfo.getIndex() }, headerLine, errors);
                        }
                        matchedSchemaFields.add(fieldInfo.getField().getName());
                        fieldNameToHeaderIndexes.computeIfAbsent(fieldInfo.getField().getName(), n -> new ArrayList<>()).add(headerIndex);
                        inputFieldIndexToSchemaFieldMap.put(headerIndex, fieldInfo.getField());
                    }
                    if (processedHeaders.contains(headerName)) {
                        duplicateHeaders.add(headerName);
                    } else {
                        processedHeaders.add(headerName);
                    }
                    headerIndex += 1;
                }
                schema.getFields().forEach(field -> {
                    if (!matchedSchemaFields.contains(field.getName())) {
                        // Schema field not found in input.
                        if (field.getConstraints() != null && (Boolean)field.getConstraints().getOrDefault(Field.CONSTRAINT_KEY_REQUIRED, Boolean.FALSE)) {
                            // Missing required field - always an error.
                            handleSyntaxViolation(ViolationLevel.ERROR, null,"validator.label.syntax.missingRequiredHeader", new Object[] { field.getName() }, headerLine, errors);
                        } else {
                            // Missing optional field - depends on configuration.
                            handleSyntaxViolation(specs.getCSVSettings().getUnspecifiedSchemaFieldViolationLevel(), null, "validator.label.syntax.unspecifiedSchemaField", new Object[] { field.getName() }, headerLine, errors);
                        }
                    }
                });
                if (schema.getFields().size() != parser.getHeaderNames().size()) {
                    // Different field counts.
                    handleSyntaxViolation(specs.getCSVSettings().getDifferentInputFieldCountViolationLevel(), null,"validator.label.syntax.differentInputFieldCount", new Object[] { parser.getHeaderNames().size(), schema.getFields().size() }, headerLine, errors);
                }
                // Header appears multiple times in exactly the same way.
                duplicateHeaders.forEach(headerName -> handleSyntaxViolation(specs.getCSVSettings().getDuplicateInputFieldViolationLevel(), headerName, "validator.label.syntax.duplicateInputField", new Object[] { headerName }, headerLine, errors));
                for (Map.Entry<String, List<Integer>> nameToIndexEntry: fieldNameToHeaderIndexes.entrySet()) {
                    if (nameToIndexEntry.getValue().size() > 1) {
                        // Multiple headers map to the same schema field.
                        List<String> headers = new ArrayList<>(nameToIndexEntry.getValue().size());
                        for (Integer pos: nameToIndexEntry.getValue()) {
                            headers.add(parser.getHeaderNames().get(pos));
                        }
                        handleSyntaxViolation(specs.getCSVSettings().getMultipleInputFieldsForSchemaFieldViolationLevel(), null, "validator.label.syntax.multipleInputFieldsForSchemaField", new Object[] { headers, nameToIndexEntry.getKey() }, headerLine, errors);
                    }
                }
            } else {
                // Add simply the schema fields in their defined sequence.
                int fieldIndex = 0;
                for (Field<?> schemaField: schema.getFields()) {
                    inputFieldIndexToSchemaFieldMap.put(fieldIndex, schemaField);
                    fieldIndex += 1;
                }
            }
            long previousLineNumber = -1;
            // Validation per row.
            long recordCounter = 0L;
            for (CSVRecord csvRecord: parser) {
                long reportedLineNumber = parser.getCurrentLineNumber();
                if (reportedLineNumber == previousLineNumber) {
                    // This can come up in the last record if there is no EOL at the end of the file.
                    reportedLineNumber += 1;
                }
                validateRow(csvRecord, reportedLineNumber, inputFieldIndexToSchemaFieldMap, schema.getFields().size(), specs.getCSVSettings(), errors);
                previousLineNumber = reportedLineNumber;
                recordCounter += 1;
                if (specs.getProgressListener() != null && recordCounter % 1000 == 0) {
                    specs.getProgressListener().schemaValidationUpdate(recordCounter);
                }
            }
            if (specs.getProgressListener() != null) {
                specs.getProgressListener().schemaValidationEnd(recordCounter);
            }
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            if (!checkAndHandleKnownErrors(e, headerLine, errors)) {
                throw new ValidatorException("validator.label.exception.providedInputCouldNotBeParsed", getRootCause(e).getMessage(), e);
            }
        }
        return errors;
    }

    /**
     * Check to see if the raised exception is a known error to be replaced with a user-friendly message.
     *
     * @param e The exception to check.
     * @param headerLine The line number to attach to the error message.
     * @param aggregatedErrors The already collected report messages to add to.
     * @return True of the error was handled (otherwise it should be re-thrown).
     */
    private boolean checkAndHandleKnownErrors(Exception e, short headerLine, List<ReportItem> aggregatedErrors) {
        boolean handled = false;
        if (e != null && e.getMessage() != null && !e.getMessage().isBlank()) {
            for (DomainConfig.ParserError parserError: specs.getDomainConfig().getParserErrors().values()) {
                if (parserError.getPattern().matcher(e.getMessage()).matches()) {
                    handleSyntaxViolation(ViolationLevel.ERROR, null, String.format("validator.parserError.%s.message", parserError.getName()), null, headerLine, aggregatedErrors);
                    handled = true;
                }
            }
        }
        return handled;
    }

    /**
     * Prepare the input file for validation. This method attempts to detect the input file's character encoding to
     * ensure it can be uniformly processed without resulting in bad outputs. If the file is converted then this
     * replaces the provided file with the previous version being deleted.
     *
     * @param inputFile The input file to process.
     */
    private void prepareInputFile(File inputFile) {
        String charsetToUse = getCharsetToUse(inputFile);
        if (!charsetToUse.equals(StandardCharsets.UTF_8.name()) && !charsetsToNotConvert.contains(charsetToUse)) {
            File fileCopy = new File(inputFile.getParentFile(), "_"+inputFile.getName());
            try {
                try (
                    Reader in = new InputStreamReader(new FileInputStream(inputFile), charsetToUse);
                    Writer out = new OutputStreamWriter(new FileOutputStream(fileCopy), StandardCharsets.UTF_8.name())
                ) {
                    char[] buffer = new char[1024];
                    int len;
                    while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
                FileUtils.deleteQuietly(inputFile);
                FileUtils.moveFile(fileCopy, inputFile);
                LOG.info("Converted input file from [{}] to [UTF-8].", charsetToUse);
            } catch (IOException e) {
                throw new ValidatorException("validator.label.exception.unableToReadInputFile", e);
            }
        }
    }

    /**
     * Determine the character encoding to use when reading the provided file.
     *
     * @param inputFile The input file.
     * @return The character encoding to use (default is UTF-8).
     */
    private String getCharsetToUse(File inputFile) {
        String charset = null;
        try (InputStream in = TikaInputStream.get(toStream(inputFile, true))) {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(in);
            CharsetMatch match = detector.detect();
            charset = match.getName();
        } catch (IOException e) {
            LOG.warn("Error while detecting charset", e);
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8.name();
        }
        return charset;
    }

    /**
     * Validate the provided row.
     *
     * @param csvRecord The row to validate.
     * @param lineNumber The current line number.
     * @param inputFieldIndexToSchemaFieldMap The mapping of input fields to schema field definitions.
     * @param schemaFieldCount The number of schema fields.
     * @param csvSettings The CSV syntax settings being considered.
     * @param aggregatedErrors The messages to add to.
     */
    private void validateRow(CSVRecord csvRecord, long lineNumber, Map<Integer, Field<?>> inputFieldIndexToSchemaFieldMap, int schemaFieldCount, CSVSettings csvSettings, List<ReportItem> aggregatedErrors) {
        if (csvSettings.isHasHeaders()) {
            // Parser based on headers.
            if (csvRecord.size() != csvRecord.getParser().getHeaderNames().size()) {
                // Record has wrong number of fields.
                handleFieldViolation(null, "validator.label.field.rowFieldCountDoesNotMatchHeaderCount", new Object[] { csvRecord.size(), csvRecord.getParser().getHeaderNames().size() }, lineNumber, null, aggregatedErrors);
            } else {
                int fieldIndex = 0;
                for (String fieldValue: csvRecord) {
                    Field<?> schemaField = inputFieldIndexToSchemaFieldMap.get(fieldIndex);
                    if (schemaField != null) {
                        // Ignore fields not part of the schema. If this is to be an error this is reported as part of the header checks.
                        validateField(fieldValue, csvRecord.getParser().getHeaderNames().get(fieldIndex), schemaField, lineNumber, aggregatedErrors);
                    }
                    fieldIndex += 1;
                }
            }
        } else {
            // Parse based on index.
            if (csvRecord.size() == schemaFieldCount) {
                int fieldIndex = 0;
                for (String fieldValue: csvRecord) {
                    Field<?> schemaField = inputFieldIndexToSchemaFieldMap.get(fieldIndex);
                    validateField(fieldValue, schemaField.getName(), schemaField, lineNumber, aggregatedErrors);
                    fieldIndex += 1;
                }
            } else {
                handleFieldViolation(null, "validator.label.field.rowFieldCountNotMatchingExpectedCount", new Object[] { csvRecord.size(), schemaFieldCount }, lineNumber, null, aggregatedErrors);
            }
        }
    }

    /**
     * Validate a specific row's field.
     *
     * @param textValue The text value of the field.
     * @param fieldNameToUse The name of the field.
     * @param field The field object as provided by the parsing.
     * @param lineNumber The current line number.
     * @param aggregatedErrors The list of messages to add to.
     */
    private void validateField(String textValue, String fieldNameToUse, Field<?> field, long lineNumber, List<ReportItem> aggregatedErrors) {
        try {
            Object fieldValue = field.castValue(textValue, false, field.getOptions());
            // Check format.
            if (!field.valueHasValidFormat(textValue)) {
                handleFieldViolation(fieldNameToUse, "validator.label.field.invalidFormat", new Object[] { textValue, fieldNameToUse, field.getFormat() }, lineNumber, textValue, aggregatedErrors);
            }
            // Check constraints.
            if (field.getConstraints() != null && !field.getConstraints().isEmpty()) {
                Map<String, Object> violations = field.checkConstraintViolations(fieldValue);
                for (Map.Entry<String, Object> entry: violations.entrySet()) {
                    var messageWithParams = prettifyConstraintFailure(entry.getKey(), entry.getValue(), field, fieldNameToUse, textValue);
                    handleFieldViolation(fieldNameToUse, messageWithParams.getKey(), messageWithParams.getParams(), lineNumber, textValue, aggregatedErrors);
                }
            }
        } catch (InvalidCastException e) {
            handleFieldViolation(fieldNameToUse, "validator.label.field.invalidType", new Object[] { textValue, fieldNameToUse, field.getType() }, lineNumber, textValue, aggregatedErrors);
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatorException("validator.label.exception.unexpectedErrorWhileValidatingRow", e, getRootCause(e).getMessage());
        }
    }

    /**
     * Handle a specific field violation.
     *
     * @param fieldName The name of the field.
     * @param messageKey The message key (to be localised).
     * @param messageParams The parameters to use when localising the message (optional).
     * @param lineNumber The current line number.
     * @param fieldValue The text value of the field.
     * @param aggregatedErrors The list of messages to add to.
     */
    private void handleFieldViolation(String fieldName, String messageKey, Object[] messageParams, long lineNumber, String fieldValue, List<ReportItem> aggregatedErrors) {
        counterErrors += 1;
        counterTotalErrors += 1;
        if (counterTotalErrors + counterTotalWarnings + counterTotalInformationMessages <= specs.getDomainConfig().getMaximumReportsForXmlOutput()) {
            aggregatedErrors.add(new ReportItem(messageFormatter, messageKey, messageParams, fieldName, lineNumber, fieldValue));
        }
    }

    /**
     * Handle a detected syntax violation.
     *
     * @param violationLevel The violation level for this kind of problem.
     * @param fieldName The name of the field.
     * @param messageKey The message key (to be localised).
     * @param messageParams The parameters to use when localising the message (optional).
     * @param lineNumber The current line number.
     * @param aggregatedErrors The list of messages to add to.
     */
    private void handleSyntaxViolation(ViolationLevel violationLevel, String fieldName, String messageKey, Object[] messageParams, long lineNumber, List<ReportItem> aggregatedErrors) {
        if (violationLevel != null && violationLevel != ViolationLevel.NONE) {
            if (violationLevel == ViolationLevel.ERROR) {
                counterErrors += 1;
                counterTotalErrors += 1;
            } else if (violationLevel == ViolationLevel.WARNING) {
                counterWarnings += 1;
                counterTotalWarnings += 1;
            } else if (violationLevel == ViolationLevel.INFO) {
                counterInformationMessages += 1;
                counterTotalInformationMessages += 1;
            }
            if (counterTotalErrors + counterTotalWarnings + counterTotalInformationMessages <= specs.getDomainConfig().getMaximumReportsForXmlOutput()) {
                aggregatedErrors.add(new ReportItem(messageFormatter, messageKey, messageParams, fieldName, lineNumber, null, violationLevel));
            }
        }
    }

    /**
     * Convert the provided list of report items to a TAR validation report.
     *
     * @param errorMessages The messages to process.
     * @return The validation reports (detailed and aggregate).
     */
    private ReportPair toTAR(List<ReportItem> errorMessages) {
        TAR report = new TAR();
        report.setDate(Utils.getXMLGregorianCalendarDateTime());
        report.setCounters(new ValidationCounters());
        report.getCounters().setNrOfErrors(BigInteger.valueOf(counterErrors));
        report.getCounters().setNrOfWarnings(BigInteger.valueOf(counterWarnings));
        report.getCounters().setNrOfAssertions(BigInteger.valueOf(counterInformationMessages));
        report.setReports(new TestAssertionGroupReportsType());
        report.setContext(new AnyContent());
        AggregateReportItems aggregateReportItems = null;
        if (specs.isProduceAggregateReport()) {
            aggregateReportItems = new AggregateReportItems(objectFactory, specs.getLocalisationHelper());
        }
        if (errorMessages != null) {
            for (ReportItem errorMessage : errorMessages) {
                var element = toReportElement(errorMessage);
                if (element != null) {
                    report.getReports().getInfoOrWarningOrError().add(element);
                    if (aggregateReportItems != null) {
                        aggregateReportItems.updateForReportItem(element, e -> String.format("%s|%s|%s", errorMessage.getFieldName(), errorMessage.getViolationLevel(), errorMessage.getMessageKey()));
                    }
                }
            }
        }
        if (counterErrors > 0) {
            report.setResult(TestResultType.FAILURE);
        } else if (counterWarnings > 0) {
            report.setResult(TestResultType.WARNING);
        } else {
            report.setResult(TestResultType.SUCCESS);
        }
        // Create the aggregate report if needed.
        TAR aggregateReport = null;
        if (aggregateReportItems != null) {
            aggregateReport = new TAR();
            aggregateReport.setContext(new AnyContent());
            aggregateReport.setResult(report.getResult());
            aggregateReport.setCounters(report.getCounters());
            aggregateReport.setDate(report.getDate());
            aggregateReport.setName(report.getName());
            aggregateReport.setReports(new TestAssertionGroupReportsType());
            aggregateReport.getReports().getInfoOrWarningOrError().addAll(aggregateReportItems.getReportItems());
        }
        return new ReportPair(report, aggregateReport);
    }

    /**
     * Create a validation report element from the provided error message.
     *
     * @param errorMessage The error message.
     * @return The report item element (may be null if it should be ignored).
     */
    private JAXBElement<TestAssertionReportType> toReportElement(ReportItem errorMessage) {
        BAR error = new BAR();
        error.setDescription(errorMessage.getReportMessage());
        error.setLocation(ValidationConstants.INPUT_CONTENT + ":" + errorMessage.getLineNumber() + ":0");
        error.setValue(errorMessage.getValue());
        switch (errorMessage.getViolationLevel()) {
            case ERROR:
                return objectFactory.createTestAssertionGroupReportsTypeError(error);
            case WARNING:
                return objectFactory.createTestAssertionGroupReportsTypeWarning(error);
            case INFO:
                return objectFactory.createTestAssertionGroupReportsTypeInfo(error);
        }
        return null; // Can be NONE as a violation level.
    }

    /**
     * If a schema field constraint has triggered a failure, return a user-friendly message for the report.
     *
     * @param constraintKey The identifier of the constraint.
     * @param constraintValue The value of the constraint.
     * @param field The field being processed.
     * @param fieldNameToUse The name of the field.
     * @param rowValue The text value of the field in the parsed row.
     * @return The user-friendly error message.
     */
    private MessageWithParams prettifyConstraintFailure(String constraintKey, Object constraintValue, Field<?> field, String fieldNameToUse, String rowValue) {
        MessageWithParams message;
        if (Field.CONSTRAINT_KEY_ENUM.equals(constraintKey)) {
            if (Boolean.TRUE.equals(specs.getDomainConfig().getDisplayEnumValuesInMessages().get(specs.getValidationType()))) {
                message = new MessageWithParams("validator.label.field.notInExpectedValuesWithParam", new Object[] {rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field)});
            } else {
                message = new MessageWithParams("validator.label.field.notInExpectedValues", new Object[] { rowValue, fieldNameToUse} );
            }
        } else if (Field.CONSTRAINT_KEY_MAX_LENGTH.equals(constraintKey)) {
            message = new MessageWithParams("validator.label.field.lengthExceedsMaximum", new Object[] { rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field) });
        } else if (Field.CONSTRAINT_KEY_MIN_LENGTH.equals(constraintKey)) {
            message = new MessageWithParams("validator.label.field.lengthBelowMinimum", new Object[] { rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field) });
        } else if (Field.CONSTRAINT_KEY_MAXIMUM.equals(constraintKey)) {
            message = new MessageWithParams("validator.label.field.valueExceedsMaximum", new Object[] { rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field) });
        } else if (Field.CONSTRAINT_KEY_MINIMUM.equals(constraintKey)) {
            message = new MessageWithParams("validator.label.field.valueBelowMinimum", new Object[] { rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field) });
        } else if (Field.CONSTRAINT_KEY_PATTERN.equals(constraintKey)) {
            message = new MessageWithParams("validator.label.field.valueNotMatchingPattern", new Object[] { rowValue, fieldNameToUse });
        } else if (Field.CONSTRAINT_KEY_REQUIRED.equals(constraintKey)) {
            message = new MessageWithParams("validator.label.field.missingValue", new Object[] { fieldNameToUse} );
        } else if (Field.CONSTRAINT_KEY_UNIQUE.equals(constraintKey)) {
            message = new MessageWithParams("validator.label.field.expectedUniqueValue", new Object[] { rowValue, fieldNameToUse });
        } else {
            message = new MessageWithParams("validator.label.field.constraintViolated", new Object[] { constraintKey, fieldNameToUse, rowValue });
        }
        return message;
    }

    /**
     * Validate the input against any configured custom plugins.
     *
     * @return The resulting validation report.
     */
    private TAR validateAgainstPlugins() {
        TAR pluginReport = null;
        ValidationPlugin[] plugins = pluginManager.getPlugins(pluginConfigProvider.getPluginClassifier(specs.getDomainConfig(), specs.getValidationType()));
        if (plugins != null && plugins.length > 0) {
            if (specs.getProgressListener() != null) {
                specs.getProgressListener().pluginValidationStart();
            }
            File pluginTmpFolder = new File(specs.getInput().getParentFile(), UUID.randomUUID().toString());
            try {
                pluginTmpFolder.mkdirs();
                ValidateRequest pluginInput = preparePluginInput(pluginTmpFolder);
                for (ValidationPlugin plugin: plugins) {
                    String pluginName = plugin.getName();
                    ValidationResponse response = null;
                    try {
                        response = plugin.validate(pluginInput);
                    } catch (Exception e) {
                        // Do not propagate the failure as this will block all validation reporting.
                        LOG.warn(String.format("Unable to correctly process plugin [%s]", e.getMessage()), e);
                    }
                    if (response != null && response.getReport() != null && response.getReport().getCounters() != null) {
                        long pluginErrors = response.getReport().getCounters().getNrOfErrors().longValue();
                        long pluginWarnings = response.getReport().getCounters().getNrOfWarnings().longValue();
                        long pluginInformationMessages = response.getReport().getCounters().getNrOfAssertions().longValue();
                        long pluginTotalReports = pluginErrors + pluginWarnings + pluginInformationMessages;
                        LOG.info("Plugin [{}] produced [{}] report item(s).", pluginName, pluginTotalReports);
                        if (response.getReport().getReports() != null) {
                            long overallTotalReports = counterTotalErrors + counterTotalWarnings + counterTotalInformationMessages;
                            if (overallTotalReports < specs.getDomainConfig().getMaximumReportsForXmlOutput()) {
                                long reportsToKeep = specs.getDomainConfig().getMaximumReportsForXmlOutput() - overallTotalReports;
                                if (response.getReport().getReports().getInfoOrWarningOrError().size() > reportsToKeep) {
                                    response.getReport().getReports().getInfoOrWarningOrError().subList((int)reportsToKeep, response.getReport().getReports().getInfoOrWarningOrError().size()).clear();
                                }
                            } else {
                                response.getReport().getReports().getInfoOrWarningOrError().clear();
                            }
                        }
                        if (pluginReport == null) {
                            pluginReport = response.getReport();
                        } else {
                            pluginReport = Utils.mergeReports(new TAR[] {pluginReport, response.getReport()});
                        }
                        counterTotalErrors += pluginErrors;
                        counterTotalWarnings += pluginWarnings;
                        counterTotalInformationMessages += pluginInformationMessages;
                    }
                }
            } finally {
                // Cleanup plugin tmp folder.
                FileUtils.deleteQuietly(pluginTmpFolder);
                if (specs.getProgressListener() != null) {
                    specs.getProgressListener().pluginValidationEnd();
                }
            }
        }
        return pluginReport;
    }

    /**
     * Prepare the input to pass into the custom validator plugins.
     *
     * @param pluginTmpFolder A temporary folder to use for the plugin's processing.
     * @return The request to pass to the plugin(s).
     */
    private ValidateRequest preparePluginInput(File pluginTmpFolder) {
        File pluginInputFile = new File(pluginTmpFolder, UUID.randomUUID() +".csv");
        try {
            FileUtils.copyFile(specs.getInput(), pluginInputFile);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to copy input file for plugin", e);
        }
        ValidateRequest request = new ValidateRequest();
        request.getInput().add(Utils.createInputItem("contentToValidate", pluginInputFile.getAbsolutePath()));
        request.getInput().add(Utils.createInputItem("domain", specs.getDomainConfig().getDomainName()));
        request.getInput().add(Utils.createInputItem("validationType", specs.getValidationType()));
        request.getInput().add(Utils.createInputItem("tempFolder", pluginTmpFolder.getAbsolutePath()));
        request.getInput().add(Utils.createInputItem("hasHeaders", String.valueOf(specs.getCSVSettings().isHasHeaders())));
        request.getInput().add(Utils.createInputItem("delimiter", String.valueOf(specs.getCSVSettings().getDelimiter())));
        request.getInput().add(Utils.createInputItem("quote", String.valueOf(specs.getCSVSettings().getQuote())));
        request.getInput().add(Utils.createInputItem("locale", specs.getLocalisationHelper().getLocale().toString()));
        return request;
    }

    /**
     * Class to hold a message key and its (optional) parameters.
     */
    static class MessageWithParams {

        private final String key;
        private final Object[] params;

        /**
         * Constructor.
         *
         * @param key The key.
         * @param params The parameters (can be null);
         */
        public MessageWithParams(String key, Object[] params) {
            this.key = Objects.requireNonNull(key);
            this.params = params;
        }

        /**
         * @return The key.
         */
        public String getKey() {
            return key;
        }

        /**
         * @return Thr parameters.
         */
        public Object[] getParams() {
            return params;
        }
    }
}
