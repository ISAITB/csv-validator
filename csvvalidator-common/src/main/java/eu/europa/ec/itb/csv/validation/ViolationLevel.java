package eu.europa.ec.itb.csv.validation;

import eu.europa.ec.itb.validation.commons.error.ValidatorException;

/**
 * Enum for the types of violation levels linked to encountered syntax issues.
 */
public enum ViolationLevel {

    /** Reported as an error in the validation report. */
    ERROR(ViolationLevel.ERROR_VALUE),
    /** Reported as a warning in the validation report. */
    WARNING(ViolationLevel.WARNING_VALUE),
    /** Reported as an information message in the validation report. */
    INFO(ViolationLevel.INFO_VALUE),
    /** Not included in the validation report (i.e. ignored). */
    NONE(ViolationLevel.NONE_VALUE);

    /** The String value of the error level. */
    public static final String ERROR_VALUE = "error";
    /** The String value of the warning level. */
    public static final String WARNING_VALUE = "warning";
    /** The String value of the info level. */
    public static final String INFO_VALUE = "info";
    /** The String value of the none level. */
    public static final String NONE_VALUE = "none";

    private final String name;

    /**
     * Constructor.
     *
     * @param name The level name.
     */
    ViolationLevel(String name) {
        this.name = name;
    }

    /**
     * @return The level's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the enum that maps to the provided violation level name.
     *
     * @param name The name.
     * @return The enum.
     * @throws IllegalArgumentException If the name is unsupported.
     */
    public static ViolationLevel byName(String name) {
        if (name == null) {
            return null;
        } else {
            if (ERROR.name.equals(name)) {
                return ERROR;
            } else if (WARNING.name.equals(name)) {
                return WARNING;
            } else if (INFO.name.equals(name)) {
                return INFO;
            } else if (NONE.name.equals(name)) {
                return NONE;
            } else {
                throw new ValidatorException("validator.label.exception.invalidViolationLevel", name);
            }
        }
    }

}
