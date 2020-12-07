package eu.europa.ec.itb.csv.validation;

public interface ProgressListener {

    void schemaValidationStart();
    void schemaValidationUpdate(long counter);
    void schemaValidationEnd(long counter);
    void pluginValidationStart();
    void pluginValidationEnd();

}
