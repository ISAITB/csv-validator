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

package eu.europa.ec.itb.csv;

/**
 * Component that accepts raw error messages and formats them for final use in validation reports.
 */
public interface MessageFormatter {

    /**
     * Given a raw message return its formatted version based on the provided metadata.
     *
     * @param lineNumber The line number linked to the message for positioning.
     * @param fieldName The name of the relevant field (provided as null if input lacks a header row).
     * @param messageKey The message key (to be localised).
     * @param messageParams The parameters to use when localising the message (optional - can be null).
     * @return The formatted message.
     */
    String formatMessage(long lineNumber, String fieldName, String messageKey, Object[] messageParams);

}
