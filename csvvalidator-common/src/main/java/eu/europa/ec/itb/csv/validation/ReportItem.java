package eu.europa.ec.itb.csv.validation;

import eu.europa.ec.itb.csv.MessageFormatter;

/**
 * DTO used to wrap the information linked to a single entry in the validation report.
 */
public class ReportItem {

    private final MessageFormatter messageFormatter;
    private final String messageKey;
    private final Object[] messageParams;
    private final String fieldName;
    private final String value;
    private final long lineNumber;
    private final ViolationLevel violationLevel;

    /**
     * Constructor.
     *
     * @param messageFormatter Component responsible for the formatting of this report item's message.
     * @param messageKey The message key (to be localised).
     * @param messageParams The parameters to use when localising the message (optional).
     * @param fieldName The name of the relevant field.
     * @param lineNumber The line number in the input.
     * @param value The value relevant to this report item.
     */
    public ReportItem(MessageFormatter messageFormatter, String messageKey, Object[] messageParams, String fieldName, long lineNumber, String value) {
        this(messageFormatter, messageKey, messageParams, fieldName, lineNumber, value, ViolationLevel.ERROR);
    }

    /**
     * Constructor.
     *
     * @param messageFormatter Component responsible for the formatting of this report item's message.
     * @param messageKey The message key (to be localised).
     * @param messageParams The parameters to use when localising the message (optional - can be null).
     * @param fieldName The name of the relevant field.
     * @param lineNumber The line number in the input.
     * @param value The value relevant to this report item.
     * @param violationLevel The violation level linked to this report item.
     */
    public ReportItem(MessageFormatter messageFormatter, String messageKey, Object[] messageParams, String fieldName, long lineNumber, String value, ViolationLevel violationLevel) {
        this.messageFormatter = messageFormatter;
        this.messageKey = messageKey;
        this.messageParams = messageParams;
        this.fieldName = fieldName;
        this.lineNumber = lineNumber;
        this.value = value;
        this.violationLevel = violationLevel;
    }

    /**
     * @return The formatted message to include in the report.
     */
    public String getReportMessage() {
        return messageFormatter.formatMessage(lineNumber, fieldName, messageKey, messageParams);
    }

    /**
     * @return The original message key.
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @return The relevant field's name.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return The line number from the input.
     */
    public long getLineNumber() {
        return lineNumber;
    }

    /**
     * @return The relevant value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return The violation level to consider.
     */
    public ViolationLevel getViolationLevel() {
        return violationLevel;
    }
}
