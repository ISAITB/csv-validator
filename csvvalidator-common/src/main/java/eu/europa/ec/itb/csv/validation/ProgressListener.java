package eu.europa.ec.itb.csv.validation;

/**
 * Observer of validation's progress.
 */
public interface ProgressListener {

    /**
     * Schema validation has begun.
     */
    void schemaValidationStart();

    /**
     * Schema validation has processed a given number of lines.
     *
     * @param counter The currently processed lines.
     */
    void schemaValidationUpdate(long counter);

    /**
     * Schema validation has finished.
     *
     * @param counter The final number of lined processed.
     */
    void schemaValidationEnd(long counter);

    /**
     * Custom plugin validation has started.
     */
    void pluginValidationStart();

    /**
     * Custom plugin validation has completed.
     */
    void pluginValidationEnd();

}
