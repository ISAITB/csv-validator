package eu.europa.ec.itb.csv.validation;

import io.frictionlessdata.tableschema.field.Field;

public class FieldInfo {

    private Field<?> field;
    private int index;

    public FieldInfo(Field<?> field, int index) {
        this.field = field;
        this.index = index;
    }

    public Field<?> getField() {
        return field;
    }

    public int getIndex() {
        return index;
    }
}
