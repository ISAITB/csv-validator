package eu.europa.ec.itb.csv.validation;

import eu.europa.ec.itb.csv.ApplicationConfig;
import eu.europa.ec.itb.validation.commons.BaseFileManager;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class FileManager extends BaseFileManager<ApplicationConfig> {

    @Override
    public String getFileExtension(String contentType) {
        return "csv";
    }

    public boolean checkFileType(InputStream stream) throws IOException {
        Tika tika = new Tika();
        String type = tika.detect(stream);
        return config.getAcceptedMimeTypes().contains(type);
    }

    public String writeCSV(String domain, File inputFile) throws IOException {
        UUID fileUUID = UUID.randomUUID();
        String inputID = domain+"_"+fileUUID.toString();
        File outputFile = new File(getReportFolder(), getInputFileName(inputID));
        outputFile.getParentFile().mkdirs();
        FileUtils.copyFile(inputFile, outputFile);
        return inputID;
    }

    public String getInputFileName(String uuid) {
        return "ITB-"+uuid+".csv";
    }

    public String getReportFileNamePdf(String uuid) {
        return "TAR-"+uuid+".pdf";
    }

    public String getReportFileNameXml(String uuid) {
        return "TAR-"+uuid+".xml";
    }

}
