package eu.europa.ec.itb.csv;

import eu.europa.ec.itb.validation.commons.ValidatorChannel;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.config.WebDomainConfigCache;
import org.apache.commons.configuration2.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DomainConfigCache extends WebDomainConfigCache<DomainConfig> {

    @Autowired
    private ApplicationConfig appConfig = null;

    @Override
    protected DomainConfig newDomainConfig() {
        return new DomainConfig();
    }

    @Override
    protected ValidatorChannel[] getSupportedChannels() {
        return new ValidatorChannel[] {ValidatorChannel.FORM, ValidatorChannel.SOAP_API};
    }

    @PostConstruct
    public void init() {
        super.init();
    }

    @Override
    protected void addDomainConfiguration(DomainConfig domainConfig, Configuration config) {
        super.addDomainConfiguration(domainConfig, config);
        addValidationArtifactInfo("validator.schemaFile", "validator.externalSchema", null, domainConfig, config);
        // CSV options.
        domainConfig.getCsvOptions().setInputHasHeader(parseBooleanMap("validator.hasHeaders", config, domainConfig.getType(), true));
        domainConfig.getCsvOptions().setDelimiter(parseCharacterMap("validator.delimiter", config, domainConfig.getType(), ','));
        domainConfig.getCsvOptions().setQuote(parseCharacterMap("validator.quote", config, domainConfig.getType(), '"'));
        // CSV option input settings.
        domainConfig.getCsvOptions().setUserInputForHeader(parseEnumMap("validator.input.hasHeader", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForDelimiter(parseEnumMap("validator.input.delimiter", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        domainConfig.getCsvOptions().setUserInputForQuote(parseEnumMap("validator.input.quote", ExternalArtifactSupport.NONE, config, domainConfig.getType(), ExternalArtifactSupport::byName));
        // Labels.
        domainConfig.getLabel().setPopupTitle(config.getString("validator.label.popupTitle", "CSV content"));
        domainConfig.getLabel().setExternalSchemaLabel(config.getString("validator.label.externalSchemaLabel", "Table Schema"));
        domainConfig.getLabel().setExternalSchemaPlaceholder(config.getString("validator.label.externalSchemaPlaceholder", "Select file..."));
        domainConfig.getLabel().setExternalSchemaLabel(config.getString("validator.label.externalSchemaLabel", "Table Schema"));
        domainConfig.getLabel().setIncludeExternalArtefacts(config.getString("validator.label.includeExternalArtefacts", "Include external schemas"));
        domainConfig.getLabel().setExternalArtefactsTooltip(config.getString("validator.label.externalArtefactsTooltip", "Additional schemas that will be considered for the validation"));
        domainConfig.getLabel().setCsvSyntax(config.getString("validator.label.csvSyntax", "CSV syntax"));
        domainConfig.getLabel().setIncludeCsvSyntax(config.getString("validator.label.includeCsvSyntax", "Specify CSV syntax settings"));
        domainConfig.getLabel().setIncludeCsvSyntaxTooltip(config.getString("validator.label.includeCsvSyntaxTooltip", "Define specific settings for the CSV syntax"));
        domainConfig.getLabel().setCsvSyntaxQuotePlaceholder(config.getString("validator.label.csvSyntaxQuotePlaceholder", "Quote"));
        domainConfig.getLabel().setCsvSyntaxQuoteTooltip(config.getString("validator.label.csvSyntaxQuoteTooltip", "The quote character to consider when wrapping field values"));
        domainConfig.getLabel().setCsvSyntaxDelimiterPlaceholder(config.getString("validator.label.csvSyntaxDelimiterPlaceholder", "Delimiter"));
        domainConfig.getLabel().setCsvSyntaxDelimiterTooltip(config.getString("validator.label.csvSyntaxDelimiterTooltip", "The delimiter character used to separate field values"));
        domainConfig.getLabel().setCsvSyntaxHeaders(config.getString("validator.label.csvSyntaxHeaders", "Content has header"));
        domainConfig.getLabel().setCsvSyntaxHeadersTooltip(config.getString("validator.label.csvSyntaxHeadersTooltip", "Whether or not the first line of the content defines the field headers"));
        addMissingDefaultValues(domainConfig.getWebServiceDescription(), appConfig.getDefaultLabels());
    }
}
