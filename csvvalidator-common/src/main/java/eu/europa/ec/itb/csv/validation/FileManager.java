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

import eu.europa.ec.itb.csv.ApplicationConfig;
import eu.europa.ec.itb.validation.commons.BaseFileManager;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Component used to carry out file-system operations in support of the validator's processing.
 */
@Component
public class FileManager extends BaseFileManager<ApplicationConfig> {

    /**
     * Get the file extension for CSV input files.
     *
     * @param contentType The content type to consider (ignored).
     * @return Always "csv".
     */
    @Override
    public String getFileExtension(String contentType) {
        return "csv";
    }

    /**
     * Check whether the provided stream is an accepted CSV input.
     *
     * @param stream The stream to process.
     * @return True if the stream is accepted.
     * @throws IOException If an IO error occurs.
     */
    public boolean checkFileType(InputStream stream) throws IOException {
        Tika tika = new Tika();
        var metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, "text/csv");
        String type = tika.detect(stream, metadata);
        return config.getAcceptedMimeTypes().contains(type);
    }

    /**
     * Store the provided CSV input file in the validator's report folder to allow it to be retrieved.
     *
     * @param domain The domain configuration.
     * @param inputFile The input file.
     * @return A unique identifier that can be used to lookup this file.
     * @throws IOException If an IO error occurs.
     */
    public String writeCSV(String domain, File inputFile) throws IOException {
        UUID fileUUID = UUID.randomUUID();
        String inputID = domain+"_"+ fileUUID;
        File outputFile = new File(getReportFolder(), getInputFileName(inputID));
        outputFile.getParentFile().mkdirs();
        FileUtils.copyFile(inputFile, outputFile);
        return inputID;
    }

    /**
     * Construct an input file name based on the provided unique identifier.
     *
     * @param uuid The identifier.
     * @return The file name to use.
     */
    public String getInputFileName(String uuid) {
        return "ITB-"+uuid+".csv";
    }

    /**
     * Construct a PDF report file name based on the provided unique identifier.
     *
     * @param uuid The identifier.
     * @param aggregate Whether the report is an aggregate.
     * @return The file name to use.
     */
    public String getReportFileNamePdf(String uuid, boolean aggregate) {
        return "TAR-"+uuid+(aggregate?"_aggregate":"")+".pdf";
    }

    /**
     * Construct a XML report file name based on the provided unique identifier.
     *
     * @param uuid The identifier.
     * @param aggregate Whether the report is an aggregate.
     * @return The file name to use.
     */
    public String getReportFileNameXml(String uuid, boolean aggregate) {
        return "TAR-"+uuid+(aggregate?"_aggregate":"")+".xml";
    }

    /**
     * Returns the name of a CSV report file based on the provided identifier.
     *
     * @param uuid The UUID to consider.
     * @param aggregate Whether the report is an aggregate.
     * @return The file name.
     */
    public String getReportFileNameCsv(String uuid, boolean aggregate) {
        return "TAR-"+uuid+(aggregate?"_aggregate":"")+".csv";
    }

}
