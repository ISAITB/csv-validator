package eu.europa.ec.itb.csv.validation;

import com.gitb.core.AnyContent;
import com.gitb.tr.*;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.validation.commons.BomStrippingReader;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.Utils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.*;

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

    private final File inputFileToValidate;
    private final DomainConfig domainConfig;
    private String validationType;
    private final List<FileInfo> externalSchemaFileInfo;
    private final CSVSettings csvSettings;
    private final ObjectFactory objectFactory = new ObjectFactory();

    public CSVValidator(File inputFileToValidate, String validationType, List<FileInfo> externalSchemaFileInfo, DomainConfig domainConfig, CSVSettings csvSettings) {
        this.inputFileToValidate = inputFileToValidate;
        this.domainConfig = domainConfig;
        this.validationType = validationType;
        this.externalSchemaFileInfo = externalSchemaFileInfo;
        this.csvSettings = csvSettings;
        if (validationType == null) {
            this.validationType = domainConfig.getType().get(0);
        }
    }

    public TAR validate() {
        TAR validationResult;
        try {
            fileManager.signalValidationStart(domainConfig.getDomainName());
            validationResult = validateInternal();
        } finally {
            fileManager.signalValidationEnd(domainConfig.getDomainName());
        }
        TAR pluginResult = validateAgainstPlugins();
        if (pluginResult != null) {
            validationResult = Utils.mergeReports(new TAR[] {validationResult, pluginResult});
        }
        if (validationResult != null) {
            sortReportItems(validationResult);
        }
        return validationResult;
    }

    private void sortReportItems(TAR report) {
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

    private TAR validateInternal() {
        List<FileInfo> schemaFiles = fileManager.getPreconfiguredValidationArtifacts(domainConfig, validationType);
        if (externalSchemaFileInfo != null) {
            schemaFiles.addAll(externalSchemaFileInfo);
        }
        if (schemaFiles.isEmpty()) {
            LOG.info("No schemas to validate against");
            return null;
        } else {
            List<TAR> reports = new ArrayList<>();
            for (FileInfo aSchemaFile: schemaFiles) {
                LOG.info("Validating against ["+aSchemaFile.getFile().getName()+"]");
                TAR report = validateAgaintSchema(inputFileToValidate, aSchemaFile.getFile());
                reports.add(report);
                LOG.info("Validated against ["+aSchemaFile.getFile().getName()+"]");
            }
            return Utils.mergeReports(reports);
        }
    }

    private InputStream toStream(File file, boolean isInput) {
        try {
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new ValidatorException("Unable to read the provided "+(isInput?"input":"schema")+" file", e);
        }
    }

    private Throwable getRootCause(Throwable e) {
        return getRootCauseInternal(e, new HashSet<>());
    }

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

    private TAR validateAgaintSchema(File inputFile, File schemaFile) {
        List<ReportItem> errors = new ArrayList<>();
        CSVFormat format = CSVFormat.RFC4180
                .withIgnoreHeaderCase(false)
                .withAllowDuplicateHeaderNames(true)
                .withIgnoreSurroundingSpaces(true)
                .withRecordSeparator("\n")
                .withDelimiter(csvSettings.getDelimiter())
                .withQuote(csvSettings.getQuote());
        if (csvSettings.isHasHeaders()) {
            format = format.withHeader();
        }
        Schema schema;
        try (InputStream schemaStream = toStream(schemaFile, false)) {
            schema = Schema.fromJsonWithOptions(schemaStream, false, Map.of(Schema.SCHEMA_OPTION_JAVA_BASED_DATE_FORMATS, String.valueOf(domainConfig.getJavaBasedDateFormats().get(validationType))));
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatorException("The provided schema could not be parsed [" + getRootCause(e).getMessage()+ "]", e);
        }
        try (
                Reader inputReader = new BomStrippingReader(toStream(inputFile, true));
                CSVParser parser = new CSVParser(inputReader, format)
        ) {
            Map<Integer, Field<?>> inputFieldIndexToSchemaFieldMap = new HashMap<>();
            if (csvSettings.isHasHeaders()) {
                Map<String, FieldInfo> fieldMap = new HashMap<>();
                Map<String, Set<String>> lowerCaseFieldNameMap = new HashMap<>();
                int index = 0;
                for (Field<?> field: schema.getFields()) {
                    if (fieldMap.containsKey(field.getName())) {
                        throw new ValidatorException("The schema defines two fields with the same name ["+field.getName()+"].");
                    } else {
                        fieldMap.put(field.getName(), new FieldInfo(field, index));
                        lowerCaseFieldNameMap.computeIfAbsent(field.getName().toLowerCase(), n -> new HashSet<>()).add(field.getName());
                    }
                }
                short headerLine = 1;
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
                                    handleSyntaxViolation(ViolationLevel.ERROR, headerName, String.format("Header '%s' is ambiguous as it may refer to multiple schema fields '%s'.", headerName, otherNames.toString()), headerLine, errors);
                                } else {
                                    // Matched header in schema fields but with different casing.
                                    String schemaFieldName = otherNames.iterator().next();
                                    handleSyntaxViolation(csvSettings.getInputFieldCaseMismatchViolationLevel(), headerName, String.format("Header '%s' has different casing that the expected '%s'.", headerName, schemaFieldName), headerLine, errors);
                                    fieldInfo = fieldMap.get(schemaFieldName);
                                }
                            }
                        }
                    }
                    if (fieldInfo == null) {
                        // Could not match header in schema fields.
                        handleSyntaxViolation(csvSettings.getUnknownInputFieldViolationLevel(), headerName, String.format("Found unexpected header '%s'.", headerName), headerLine, errors);
                    } else  {
                        if (fieldInfo.getIndex() != headerIndex) {
                            // Header appears in unexpected position.
                            handleSyntaxViolation(csvSettings.getDifferentInputFieldSequenceViolationLevel(), headerName, String.format("Header '%s' is defined at position [%s] which is not the expected position [%s].", headerName, headerIndex, fieldInfo.getIndex()), headerLine, errors);
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
                schema.getFields().forEach((f) -> {
                    if (!matchedSchemaFields.contains(f.getName())) {
                        // Schema field not found in input.
                        handleSyntaxViolation(csvSettings.getUnspecifiedSchemaFieldViolationLevel(), null, String.format("The defined header fields do not include the expected field '%s'.", f.getName()), headerLine, errors);
                    }
                });
                if (schema.getFields().size() != parser.getHeaderNames().size()) {
                    // Different field counts.
                    handleSyntaxViolation(csvSettings.getDifferentInputFieldCountViolationLevel(), null, String.format("The header field count [%s] does not match the expected count [%s].", parser.getHeaderNames().size(), schema.getFields().size()), headerLine, errors);
                }
                duplicateHeaders.forEach((headerName) -> {
                    // Header appears multiple times in exactly the same way.
                    handleSyntaxViolation(csvSettings.getDuplicateInputFieldViolationLevel(), headerName, String.format("Header '%s' is defined multiple times.", headerName), headerLine, errors);
                });
                for (Map.Entry<String, List<Integer>> nameToIndexEntry: fieldNameToHeaderIndexes.entrySet()) {
                    if (nameToIndexEntry.getValue().size() > 1) {
                        // Multiple headers map to the same schema field.
                        List<String> headers = new ArrayList<>(nameToIndexEntry.getValue().size());
                        for (Integer pos: nameToIndexEntry.getValue()) {
                            headers.add(parser.getHeaderNames().get(pos));
                        }
                        handleSyntaxViolation(csvSettings.getMultipleInputFieldsForSchemaFieldViolationLevel(), null, String.format("Multiple headers %s map to the same schema field '%s'.", headers, nameToIndexEntry.getKey()), headerLine, errors);
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
            // Validation per row.
            for (CSVRecord record: parser) {
                validateRow(record, parser.getCurrentLineNumber(), inputFieldIndexToSchemaFieldMap, schema.getFields().size(), csvSettings, errors);
            }
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatorException("The provided input could not be parsed ["+ getRootCause(e).getMessage()+"]", e);
        }
        return toTAR(errors);
    }

    private void validateRow(CSVRecord record, long lineNumber, Map<Integer, Field<?>> inputFieldIndexToSchemaFieldMap, int schemaFieldCount, CSVSettings csvSettings, List<ReportItem> aggregatedErrors) {
        if (csvSettings.isHasHeaders()) {
            // Parser based on headers.
            if (record.size() != record.getParser().getHeaderNames().size()) {
                // Record has wrong number of fields.
                aggregatedErrors.add(new ReportItem("The row field count ["+record.size()+"] does not match the number of defined headers ["+record.getParser().getHeaderNames().size()+"].", null, lineNumber, null));
            } else {
                int fieldIndex = 0;
                for (String fieldValue: record) {
                    Field<?> schemaField = inputFieldIndexToSchemaFieldMap.get(fieldIndex);
                    if (schemaField != null) {
                        // Ignore fields not part of the schema. If this is to be an error this is reported as part of the header checks.
                        validateField(fieldValue, record.getParser().getHeaderNames().get(fieldIndex), schemaField, lineNumber, aggregatedErrors);
                    }
                    fieldIndex += 1;
                }
            }
        } else {
            // Parse based on index.
            if (record.size() == schemaFieldCount) {
                int fieldIndex = 0;
                for (String fieldValue: record) {
                    Field<?> schemaField = inputFieldIndexToSchemaFieldMap.get(fieldIndex);
                    validateField(fieldValue, schemaField.getName(), schemaField, lineNumber, aggregatedErrors);
                    fieldIndex += 1;
                }
            } else {
                aggregatedErrors.add(new ReportItem("The row field count ["+record.size()+"] does not match the expected count ["+schemaFieldCount+"]. This is required when no headers are defined in the input.", null, lineNumber, null));
            }
        }
    }

    private void validateField(String textValue, String fieldNameToUse, Field<?> field, long lineNumber, List<ReportItem> aggregatedErrors) {
        try {
            Object fieldValue = field.castValue(textValue, false, field.getOptions());
            // Check format.
            if (!field.valueHasValidFormat(textValue)) {
                aggregatedErrors.add(new ReportItem(String.format("Value '%s' provided for field '%s' is invalid for format '%s'.", textValue, fieldNameToUse, field.getFormat()), fieldNameToUse, lineNumber, textValue));
            }
            // Check constraints.
            if (field.getConstraints() != null && !field.getConstraints().isEmpty()) {
                Map<String, Object> violations = field.checkConstraintViolations(fieldValue);
                for (Map.Entry<String, Object> entry: violations.entrySet()) {
                    aggregatedErrors.add(new ReportItem(prettifyConstraintFailure(entry.getKey(), entry.getValue(), field, fieldNameToUse, textValue), fieldNameToUse, lineNumber, textValue));
                }
            }
        } catch (InvalidCastException e) {
            aggregatedErrors.add(new ReportItem(String.format("Value '%s' provided for field '%s' could not be parsed. Expected type was '%s'.", textValue, fieldNameToUse, field.getType()), fieldNameToUse, lineNumber, textValue));
        } catch (Exception e) {
            throw new ValidatorException("Unexpected error while validating row ["+getRootCause(e).getMessage()+"]", e);
        }
    }

    private void handleSyntaxViolation(ViolationLevel violationLevel, String fieldName, String message, long lineNumber, List<ReportItem> aggregatedErrors) {
        if (violationLevel != null && violationLevel != ViolationLevel.NONE) {
            aggregatedErrors.add(new ReportItem(message, fieldName, lineNumber, null, violationLevel));
        }
    }

    private TAR toTAR(List<ReportItem> errorMessages) {
        TAR report = new TAR();
        report.setDate(Utils.getXMLGregorianCalendarDateTime());
        report.setCounters(new ValidationCounters());
        report.getCounters().setNrOfWarnings(BigInteger.ZERO);
        report.getCounters().setNrOfAssertions(BigInteger.ZERO);
        report.setReports(new TestAssertionGroupReportsType());
        report.setContext(new AnyContent());
        long errors = 0;
        long warnings = 0;
        long infos = 0;
        if (errorMessages != null) {
            for (ReportItem errorMessage : errorMessages) {
                BAR error = new BAR();
                error.setDescription(errorMessage.getReportMessage());
                error.setLocation(ValidationConstants.INPUT_CONTENT + ":" + errorMessage.getLineNumber() + ":0");
                error.setValue(errorMessage.getValue());
                switch (errorMessage.getViolationLevel()) {
                    case ERROR:
                        report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeError(error));
                        errors += 1;
                        break;
                    case WARNING:
                        report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeWarning(error));
                        warnings += 1;
                        break;
                    case INFO:
                        report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeInfo(error));
                        infos += 1;
                        break;
                    case NONE:
                        // Nothing.
                        break;
                }
            }
        }
        if (errors > 0) {
            report.setResult(TestResultType.FAILURE);
        } else if (warnings > 0) {
            report.setResult(TestResultType.WARNING);
        } else {
            report.setResult(TestResultType.SUCCESS);
        }
        report.getCounters().setNrOfErrors(BigInteger.valueOf(errors));
        report.getCounters().setNrOfWarnings(BigInteger.valueOf(warnings));
        report.getCounters().setNrOfAssertions(BigInteger.valueOf(infos));
        return report;
    }

    private String prettifyConstraintFailure(String constraintKey, Object constraintValue, Field<?> field, String fieldNameToUse, String rowValue) {
        String message;
        if (Field.CONSTRAINT_KEY_ENUM.equals(constraintKey)) {
            if (domainConfig.getDisplayEnumValuesInMessages().get(validationType)) {
                message = String.format("Value '%s' for field '%s' is not in the list of expected values %s.", rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field));
            } else {
                message = String.format("Value '%s' for field '%s' is not in the list of expected values.", rowValue, fieldNameToUse);
            }
        } else if (Field.CONSTRAINT_KEY_MAX_LENGTH.equals(constraintKey)) {
            message = String.format("The length of value '%s' for field '%s' exceeds the maximum allowed length of %s.", rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_MIN_LENGTH.equals(constraintKey)) {
            message = String.format("The length of value '%s' for field '%s' is less than the minimum allowed length of %s.", rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_MAXIMUM.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' exceeds the allowed maximum of %s.", rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_MINIMUM.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' is less than the minimum of %s.", rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_PATTERN.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' does not match the expected pattern '%s'.", rowValue, fieldNameToUse, Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_REQUIRED.equals(constraintKey)) {
            message = String.format("No value was provided for required field '%s'.", fieldNameToUse);
        } else if (Field.CONSTRAINT_KEY_UNIQUE.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' must be unique.", rowValue, fieldNameToUse);
        } else {
            message = String.format("Violation of constraint [%s] for field '%s'. Provided value was '%s'.", constraintKey, fieldNameToUse, rowValue);
        }
        return message;
    }

    private TAR validateAgainstPlugins() {
        TAR pluginReport = null;
        ValidationPlugin[] plugins = pluginManager.getPlugins(pluginConfigProvider.getPluginClassifier(domainConfig, validationType));
        if (plugins != null && plugins.length > 0) {
            File pluginTmpFolder = new File(inputFileToValidate.getParentFile(), UUID.randomUUID().toString());
            try {
                pluginTmpFolder.mkdirs();
                ValidateRequest pluginInput = preparePluginInput(pluginTmpFolder);
                for (ValidationPlugin plugin: plugins) {
                    String pluginName = plugin.getName();
                    ValidationResponse response = plugin.validate(pluginInput);
                    if (response != null && response.getReport() != null && response.getReport().getReports() != null) {
                        LOG.info("Plugin [{}] produced [{}] report item(s).", pluginName, response.getReport().getReports().getInfoOrWarningOrError().size());
                        if (pluginReport == null) {
                            pluginReport = response.getReport();
                        } else {
                            pluginReport = Utils.mergeReports(new TAR[] {pluginReport, response.getReport()});
                        }
                    }
                }
            } finally {
                // Cleanup plugin tmp folder.
                FileUtils.deleteQuietly(pluginTmpFolder);
            }
        }
        return pluginReport;
    }

    private ValidateRequest preparePluginInput(File pluginTmpFolder) {
        File pluginInputFile = new File(pluginTmpFolder, UUID.randomUUID().toString()+".csv");
        try {
            FileUtils.copyFile(inputFileToValidate, pluginInputFile);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to copy input file for plugin", e);
        }
        ValidateRequest request = new ValidateRequest();
        request.getInput().add(Utils.createInputItem("contentToValidate", pluginInputFile.getAbsolutePath()));
        request.getInput().add(Utils.createInputItem("domain", domainConfig.getDomainName()));
        request.getInput().add(Utils.createInputItem("validationType", validationType));
        request.getInput().add(Utils.createInputItem("tempFolder", pluginTmpFolder.getAbsolutePath()));
        request.getInput().add(Utils.createInputItem("hasHeaders", String.valueOf(csvSettings.isHasHeaders())));
        request.getInput().add(Utils.createInputItem("delimiter", String.valueOf(csvSettings.getDelimiter())));
        request.getInput().add(Utils.createInputItem("quote", String.valueOf(csvSettings.getQuote())));
        return request;
    }

}
