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

import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.validation.commons.FileInfo;
import eu.europa.ec.itb.validation.commons.LocalisationHelper;

import java.io.File;
import java.util.List;

/**
 * Class used to wrap the specifications with which to carry out a validation.
 */
public class ValidationSpecs {

    private File input;
    private String validationType;
    private List<FileInfo> externalSchemas;
    private DomainConfig domainConfig;
    private CSVSettings csvSettings;
    private LocalisationHelper localisationHelper;
    private boolean produceAggregateReport;
    private ProgressListener progressListener;

    /**
     * Private constructor to prevent direct initialisation.
     */
    private ValidationSpecs() {}

    /**
     * @return The CSV content to validate.
     */
    public File getInput() {
        return input;
    }

    /**
     * @return The requested validation type.
     */
    public String getValidationType() {
        if (validationType == null) {
            validationType = domainConfig.getType().get(0);
        }
        return validationType;
    }

    /**
     * @return The user-provided schemas to consider.
     */
    public List<FileInfo> getExternalSchemas() {
        return externalSchemas;
    }

    /**
     * @return The current domain configuration.
     */
    public DomainConfig getDomainConfig() {
        return domainConfig;
    }

    /**
     * @return The CSV syntax settings to consider.
     */
    public CSVSettings getCSVSettings() {
        return csvSettings;
    }

    /**
     * @return Helper class to facilitate translation lookups.
     */
    public LocalisationHelper getLocalisationHelper() {
        return localisationHelper;
    }

    /**
     * @return True if an aggregate report should also be produced.
     */
    public boolean isProduceAggregateReport() {
        return produceAggregateReport;
    }

    /**
     * @return An observer to be notified of the validation progress.
     */
    public ProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Build the validation specifications.
     *
     * @param input The CSV content to validate.
     * @param csvSettings The CSV syntax settings to consider.
     * @param localisationHelper Helper class to facilitate translation lookups.
     * @param domainConfig The current domain configuration.
     * @return The specification builder.
     */
    public static Builder builder(File input, CSVSettings csvSettings, LocalisationHelper localisationHelper, DomainConfig domainConfig) {
        return new Builder(input, csvSettings, localisationHelper, domainConfig);
    }

    /**
     * Builder class used to incrementally create a specification instance.
     */
    public static class Builder {

        private final ValidationSpecs instance;

        /**
         * Constructor.
         *
         * @param input The CSV content to validate.
         * @param csvSettings The CSV syntax settings to consider.
         * @param localisationHelper Helper class to facilitate translation lookups.
         * @param domainConfig The current domain configuration.
         */
        Builder(File input, CSVSettings csvSettings, LocalisationHelper localisationHelper, DomainConfig domainConfig) {
            instance = new ValidationSpecs();
            instance.input = input;
            instance.localisationHelper = localisationHelper;
            instance.domainConfig = domainConfig;
            instance.csvSettings = csvSettings;
        }

        /**
         * @return The specification instance to use.
         */
        public ValidationSpecs build() {
            return instance;
        }

        /**
         * @param validationType Set the validation type to consider.
         * @return The builder.
         */
        public Builder withValidationType(String validationType) {
            instance.validationType = validationType;
            return this;
        }

        /**
         * @param externalSchemas Set the user-provided schemas to consider.
         * @return The builder.
         */
        public Builder withExternalSchemas(List<FileInfo> externalSchemas) {
            instance.externalSchemas = externalSchemas;
            return this;
        }

        /**
         * @param progressListener Set the progress listener to be notified of updates.
         * @return The builder.
         */
        public Builder withProgressListener(ProgressListener progressListener) {
            instance.progressListener = progressListener;
            return this;
        }

        /**
         * Generate also the aggregate report.
         *
         * @return The builder.
         */
        public Builder produceAggregateReport() {
            instance.produceAggregateReport = true;
            return this;
        }
    }

}