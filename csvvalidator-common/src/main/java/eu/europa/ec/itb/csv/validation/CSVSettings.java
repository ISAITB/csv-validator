package eu.europa.ec.itb.csv.validation;

public class CSVSettings {

    private boolean hasHeaders;
    private char delimiter;
    private char quote;

    private CSVSettings() {
    }

    public boolean isHasHeaders() {
        return hasHeaders;
    }

    public CSVSettings setHasHeaders(boolean hasHeaders) {
        this.hasHeaders = hasHeaders;
        return this;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public CSVSettings setDelimiter(char delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public char getQuote() {
        return quote;
    }

    public CSVSettings setQuote(char quote) {
        this.quote = quote;
        return this;
    }

    public static CSVSettings build() {
        return new CSVSettings();
    }

}
