package eu.europa.ec.itb.csv.validation;

import eu.europa.ec.itb.csv.MessageFormatter;

public class ReportItem {

    private final MessageFormatter messageFormatter;
    private final String message;
    private final String fieldName;
    private final String value;
    private final long lineNumber;
    private final ViolationLevel violationLevel;

    public ReportItem(MessageFormatter messageFormatter, String message, String fieldName, long lineNumber, String value) {
        this(messageFormatter, message, fieldName, lineNumber, value, ViolationLevel.ERROR);
    }

    public ReportItem(MessageFormatter messageFormatter, String message, String fieldName, long lineNumber, String value, ViolationLevel violationLevel) {
        this.messageFormatter = messageFormatter;
        this.message = message;
        this.fieldName = fieldName;
        this.lineNumber = lineNumber;
        this.value = value;
        this.violationLevel = violationLevel;
    }

    public String getReportMessage() {
        return messageFormatter.formatMessage(lineNumber, fieldName, getMessage());
    }

    public String getMessage() {
        return message;
    }

    public String getFieldName() {
        return fieldName;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public String getValue() {
        return value;
    }

    public ViolationLevel getViolationLevel() {
        return violationLevel;
    }
}
