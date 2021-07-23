package eu.europa.ec.itb.csv.validation;

import io.frictionlessdata.tableschema.field.Field;

/**
 * DTO that wraps a schema field adding to it its sequence index.
 */
public class FieldInfo {

    private final Field<?> field;
    private final int index;

    /**
     * Constructor.
     *
     * @param field The field to wrap.
     * @param index The index of the field.
     */
    public FieldInfo(Field<?> field, int index) {
        this.field = field;
        this.index = index;
    }

    /**
     * @return The field.
     */
    public Field<?> getField() {
        return field;
    }

    /**
     * @return The index.
     */
    public int getIndex() {
        return index;
    }
}
