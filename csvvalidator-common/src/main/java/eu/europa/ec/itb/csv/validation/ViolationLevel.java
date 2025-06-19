/*
 * Copyright (C) 2025 European Union
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
