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

import com.gitb.core.AnyContent;
import com.gitb.tr.TAR;
import eu.europa.ec.itb.csv.validation.ValidationConstants;
import eu.europa.ec.itb.validation.commons.Utils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Class with common utilities for the CSV validator.
 */
public class CsvValidatorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CsvValidatorUtils.class);

    /**
     * Constructor to prevent instantiation.
     */
    private CsvValidatorUtils() { throw new IllegalStateException("Utility class"); }

    /**
     * Add the context map to the provided validation report.
     *
     * @param report The report to add to.
     * @param contentToValidate The input file.
     */
    public static void addContext(TAR report, File contentToValidate) {
        if (report != null) {
            if (report.getContext() == null) {
                report.setContext(new AnyContent());
            }
            try {
                var content = Utils.createInputItem(ValidationConstants.INPUT_CONTENT, FileUtils.readFileToString(contentToValidate, StandardCharsets.UTF_8));
                content.setMimeType("text/csv");
                report.getContext().getItem().add(content);
            } catch (IOException e) {
                LOG.warn("Error while adding the "+ValidationConstants.INPUT_CONTENT+" to the report's context", e);
            }
        }
    }

}
