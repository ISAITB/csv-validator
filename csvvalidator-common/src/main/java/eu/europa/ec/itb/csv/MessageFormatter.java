package eu.europa.ec.itb.csv;

public interface MessageFormatter {

    String formatMessage(long lineNumber, String fieldName, String message);

}
