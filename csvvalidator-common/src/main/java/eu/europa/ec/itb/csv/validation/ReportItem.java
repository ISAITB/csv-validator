package eu.europa.ec.itb.csv.validation;

public class ReportItem {

    private String message;
    private String fieldName;
    private String value;
    private int lineNumber;

    public ReportItem(String message, String fieldName, int lineNumber, String value) {
        this.message = message;
        this.fieldName = fieldName;
        this.lineNumber = lineNumber;
        this.value = value;
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

    public int getLineNumber() {
        return lineNumber;
    }

    public String getValue() {
        return value;
    }
}
