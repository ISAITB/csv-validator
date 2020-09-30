package eu.europa.ec.itb.csv.validation;

public enum ViolationLevel {

    ERROR("error"), WARNING("warning"), INFO("info"), NONE("none");

    private String name;

    ViolationLevel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ViolationLevel byName(String name) {
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
