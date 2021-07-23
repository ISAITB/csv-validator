package eu.europa.ec.itb.csv.validation;

/**
 * Enum for the types of violation levels linked to encountered syntax issues.
 */
public enum ViolationLevel {

    /** Reported as an error in the validation report. */
    ERROR("error"),
    /** Reported as a warning in the validation report. */
    WARNING("warning"),
    /** Reported as an information message in the validation report. */
    INFO("info"),
    /** Not included in the validation report (i.e. ignored). */
    NONE("none");

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
                throw new IllegalArgumentException("Unknown violation level ["+name+"]");
            }
        }
    }

}
