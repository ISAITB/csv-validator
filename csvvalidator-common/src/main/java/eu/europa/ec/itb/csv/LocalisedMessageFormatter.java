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

package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.validation.commons.LocalisationHelper;

/**
 * Class used to format report messages in a locale-sensitive manner.
 */
public class LocalisedMessageFormatter implements MessageFormatter {

    private final LocalisationHelper localiser;

    /**
     * Constructor.
     *
     * @param localiser The helper class to help with translations.
     */
    public LocalisedMessageFormatter(LocalisationHelper localiser) {
        this.localiser = localiser;
    }

    /**
     * Format a error message for inclusion in the validation report.
     *
     * @param lineNumber The line number to include as the location.
     * @param fieldName The relevant field name (null is no header fields are defined).
     * @param messageKey The message key (to be localised).
     * @param messageParams The parameters to use when localising the message (optional).
     * @return The formatted message.
     */
    @Override
    public String formatMessage(long lineNumber, String fieldName, String messageKey, Object[] messageParams) {
        if (fieldName == null) {
            return String.format("[%s %s]: %s", localiser.localise("validator.label.lineMessagePrefix"), lineNumber, localiser.localise(messageKey, messageParams));
        } else {
            return String.format("[%s %s][%s %s]: %s", localiser.localise("validator.label.lineMessagePrefix"), lineNumber, localiser.localise("validator.label.fieldMessagePrefix"), fieldName, localiser.localise(messageKey, messageParams));
        }
    }

}
