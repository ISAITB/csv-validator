package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.csv.validation.CSVSettings;
import eu.europa.ec.itb.csv.validation.FileManager;
import eu.europa.ec.itb.validation.commons.BaseInputHelper;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.error.ValidatorException;
import org.springframework.stereotype.Component;

@Component
public class InputHelper extends BaseInputHelper<FileManager, DomainConfig, ApplicationConfig> {

    public CSVSettings buildCSVSettings(DomainConfig domainConfig, String validationType, Boolean inputHeaders, String inputDelimiter, String inputQuote) {
        Boolean hasHeaders = domainConfig.getCsvOptions().getInputHasHeader().get(validationType);
        Character delimiter = domainConfig.getCsvOptions().getDelimiter().get(validationType);
        Character quote = domainConfig.getCsvOptions().getQuote().get(validationType);
        ExternalArtifactSupport hasHeadersInputSupport = domainConfig.getCsvOptions().getUserInputForHeader().get(validationType);
        ExternalArtifactSupport delimiterInputSupport = domainConfig.getCsvOptions().getUserInputForDelimiter().get(validationType);
        ExternalArtifactSupport quoteInputSupport = domainConfig.getCsvOptions().getUserInputForQuote().get(validationType);
        if (hasHeadersInputSupport != ExternalArtifactSupport.NONE) {
            if (hasHeadersInputSupport == ExternalArtifactSupport.REQUIRED && inputHeaders == null) {
                throw new ValidatorException("You are required to provide your choice on whether or not the input has a header row.");
            }
            if (inputHeaders != null) {
                hasHeaders = inputHeaders;
            }
        }
        if (delimiterInputSupport != ExternalArtifactSupport.NONE) {
            if (delimiterInputSupport == ExternalArtifactSupport.REQUIRED && (inputDelimiter == null || inputDelimiter.isBlank())) {
                throw new ValidatorException("You are required to provide the delimiter character.");
            }
            if (inputDelimiter != null && !inputDelimiter.isEmpty()) {
                if (inputDelimiter.length() > 1) {
                    throw new ValidatorException("A single character is expected for the delimiter.");
                }
                delimiter = inputDelimiter.charAt(0);
            }
        }
        if (quoteInputSupport != ExternalArtifactSupport.NONE) {
            if (quoteInputSupport == ExternalArtifactSupport.REQUIRED && (inputQuote == null || inputQuote.isBlank())) {
                throw new ValidatorException("You are required to provide the quote character.");
            }
            if (inputQuote != null && !inputQuote.isEmpty()) {
                if (inputQuote.length() > 1) {
                    throw new ValidatorException("A single character is expected for the quote.");
                }
                quote = inputQuote.charAt(0);
            }
        }
        return CSVSettings.build()
                .setHasHeaders(hasHeaders)
                .setDelimiter(delimiter)
                .setQuote(quote);
    }

}
