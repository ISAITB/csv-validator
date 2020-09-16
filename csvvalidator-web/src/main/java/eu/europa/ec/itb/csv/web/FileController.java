package eu.europa.ec.itb.csv.web;

import eu.europa.ec.itb.csv.ApplicationConfig;
import eu.europa.ec.itb.csv.DomainConfigCache;
import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.validation.commons.web.BaseFileController;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileController extends BaseFileController<FileManager, ApplicationConfig, DomainConfigCache> {

    @Override
    public String getInputFileName(String id) {
        return fileManager.getInputFileName(id);
    }

    @Override
    public String getReportFileNameXml(String id) {
        return fileManager.getReportFileNameXml(id);
    }

    @Override
    public String getReportFileNamePdf(String id) {
        return fileManager.getReportFileNamePdf(id);
    }

}
