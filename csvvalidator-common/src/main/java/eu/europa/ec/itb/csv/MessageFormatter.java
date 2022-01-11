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
     * @param message The message to process (expected to be already localised and ready for display).
     * @return The formatted message.
     */
    String formatMessage(long lineNumber, String fieldName, String message);

}
