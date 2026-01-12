/*
 * Copyright (C) 2026 European Union
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an
 * "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for
 * the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.itb.csv.validation;

import io.frictionlessdata.tableschema.field.Field;

/**
 * DTO that wraps a schema field adding to it its sequence index.
 */
public class FieldInfo <T> {

    private final Field<T> field;
    private final int index;

    /**
     * Constructor.
     *
     * @param field The field to wrap.
     * @param index The index of the field.
     */
    public FieldInfo(Field<T> field, int index) {
        this.field = field;
        this.index = index;
    }

    /**
     * @return The field.
     */
    public Field<T> getField() {
        return field;
    }

    /**
     * @return The index.
     */
    public int getIndex() {
        return index;
    }
}
