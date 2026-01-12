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
