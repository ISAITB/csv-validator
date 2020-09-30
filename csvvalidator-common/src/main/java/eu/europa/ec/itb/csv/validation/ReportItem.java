package eu.europa.ec.itb.csv.validation;

public class ReportItem {

    private String message;
    private String fieldName;
    private String value;
    private long lineNumber;
    private ViolationLevel violationLevel;

    public ReportItem(String message, String fieldName, long lineNumber, String value) {
        this(message, fieldName, lineNumber, value, ViolationLevel.ERROR);
    }

    public ReportItem(String message, String fieldName, long lineNumber, String value, ViolationLevel violationLevel) {
        this.message = message;
        this.fieldName = fieldName;
        this.lineNumber = lineNumber;
        this.value = value;
        this.violationLevel = violationLevel;
    }

    public String getReportMessage() {
        if (fieldName == null) {
            return String.format("[%s]: %s", lineNumber, getMessage());
        } else {
            return String.format("[%s][%s]: %s", lineNumber, fieldName, getMessage());
        }
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
