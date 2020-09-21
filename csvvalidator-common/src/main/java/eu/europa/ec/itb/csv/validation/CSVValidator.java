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

    private TAR validateAgaintSchema(File inputFile, File schemaFile) {
        List<ReportItem> errors = new ArrayList<>();
        CSVFormat format = CSVFormat.RFC4180
                .withIgnoreSurroundingSpaces(true)
                .withRecordSeparator("\n")
                .withDelimiter(csvSettings.getDelimiter())
                .withQuote(csvSettings.getQuote());
        if (csvSettings.isHasHeaders()) {
            format = format.withHeader();
        }
        Schema schema;
        try (InputStream schemaStream = toStream(schemaFile, false)) {
            schema = Schema.fromJson(schemaStream, false);
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatorException("The provided schema could not be parsed [" + getRootCause(e).getMessage()+ "]", e);
        }
        Field<?>[] fields = schema.getFields().toArray(new Field[] {});
        Map<String, Object> castOptions = Collections.emptyMap();
        int lineCounter = 1;
        if (csvSettings.isHasHeaders()) {
            lineCounter += 1;
        }
        try (
            Reader inputReader = new BomStrippingReader(toStream(inputFile, true));
            CSVParser parser = new CSVParser(inputReader, format)
        ) {
            for (CSVRecord record: parser) {
                String[] rowValues = new String[record.size()];
                for (int i=0; i < record.size(); i++) {
                    rowValues[i] = record.get(i);
                }
                validateRow(rowValues, lineCounter, fields, castOptions, errors);
                lineCounter += 1;
            }
        } catch (ValidatorException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidatorException("The provided input could not be parsed ["+ getRootCause(e).getMessage()+"]", e);
        }
        return toTAR(errors);
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

    private void validateRow(String[] rowValues, int lineNumber, Field<?>[] fields, Map<String, Object> castOptions, List<ReportItem> aggregatedErrors) {
        if (rowValues.length == fields.length) {
            for (int i=0; i < fields.length; i++) {
                try {
                    Object value = fields[i].castValue(rowValues[i], false, castOptions);
                    // Check format.
                    if (!fields[i].valueHasValidFormat(rowValues[i])) {
                        aggregatedErrors.add(new ReportItem(String.format("Value '%s' provided for field '%s' is invalid for format '%s'.", rowValues[i], fields[i].getName(), fields[i].getFormat()), fields[i].getName(), lineNumber, rowValues[i]));
                    }
                    // Check constraints.
                    if (fields[i].getConstraints() != null && !fields[i].getConstraints().isEmpty()) {
                        Map<String, Object> violations = fields[i].checkConstraintViolations(value);
                        for (Map.Entry<String, Object> entry: violations.entrySet()) {
                            aggregatedErrors.add(new ReportItem(prettifyConstraintFailure(entry.getKey(), entry.getValue(), fields[i], rowValues[i]), fields[i].getName(), lineNumber, rowValues[i]));
                        }
                    }
                } catch (InvalidCastException e) {
                    aggregatedErrors.add(new ReportItem(String.format("Value '%s' provided for field '%s' could not be parsed. Expected type was '%s'.", rowValues[i], fields[i].getName(), fields[i].getType()), fields[i].getName(), lineNumber, rowValues[i]));
                } catch (Exception e) {
                    throw new ValidatorException("Unexpected error while validating row ["+getRootCause(e).getMessage()+"]", e);
                }
            }
        } else {
            aggregatedErrors.add(new ReportItem("The row field count ["+rowValues.length+"] does not match the expected count ["+fields.length+"].", null, lineNumber, null));
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
        if (errorMessages == null || errorMessages.isEmpty()) {
            report.setResult(TestResultType.SUCCESS);
            report.getCounters().setNrOfErrors(BigInteger.ZERO);
        } else {
            report.setResult(TestResultType.FAILURE);
            report.getCounters().setNrOfErrors(BigInteger.valueOf(errorMessages.size()));
            for (ReportItem errorMessage: errorMessages) {
                BAR error = new BAR();
                error.setDescription(errorMessage.getReportMessage());
                error.setLocation(ValidationConstants.INPUT_CONTENT+":"+errorMessage.getLineNumber()+":0");
                error.setValue(errorMessage.getValue());
                report.getReports().getInfoOrWarningOrError().add(objectFactory.createTestAssertionGroupReportsTypeError(error));
            }
        }
        return report;
    }

    private String prettifyConstraintFailure(String constraintKey, Object constraintValue, Field<?> field, String rowValue) {
        String message;
        if (Field.CONSTRAINT_KEY_ENUM.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' is not in the list of expected values %s.", rowValue, field.getName(), Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_MAX_LENGTH.equals(constraintKey)) {
            message = String.format("The length of value '%s' for field '%s' exceeds the maximum allowed length of %s.", rowValue, field.getName(), Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_MIN_LENGTH.equals(constraintKey)) {
            message = String.format("The length of value '%s' for field '%s' is less than the minimum allowed length of %s.", rowValue, field.getName(), Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_MAXIMUM.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' exceeds the allowed maximum of %s.", rowValue, field.getName(), Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_MINIMUM.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' is less than the minimum of %s.", rowValue, field.getName(), Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_PATTERN.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' does not match the expected pattern '%s'.", rowValue, field.getName(), Field.formatValueAsString(constraintValue, field));
        } else if (Field.CONSTRAINT_KEY_REQUIRED.equals(constraintKey)) {
            message = String.format("No value was provided for required field '%s'.", field.getName());
        } else if (Field.CONSTRAINT_KEY_UNIQUE.equals(constraintKey)) {
            message = String.format("Value '%s' for field '%s' must be unique.", rowValue, field.getName());
        } else {
            message = String.format("Violation of constraint [%s] for field '%s'. Provided value was '%s'.", constraintKey, field.getName(), rowValue);
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
