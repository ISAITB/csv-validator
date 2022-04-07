package eu.europa.ec.itb.csv;

/**
 * Component that accepts raw error messages and formats them for final use in validation reports.
 */
public interface MessageFormatter {

    /**
     * Given a raw message return its formatted version based on the provided metadata.
     *
     * @param lineNumber The line number linked to the message for positioning.
     * @param fieldName The name of the relevant field (provided as null if input lacks a header row).
     * @param messageKey The message key (to be localised).
     * @param messageParams The parameters to use when localising the message (optional - can be null).
     * @return The formatted message.
     */
    String formatMessage(long lineNumber, String fieldName, String messageKey, Object[] messageParams);

}
