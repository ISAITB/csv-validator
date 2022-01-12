package eu.europa.ec.itb.csv.gitb;

import com.gitb.core.TypedParameter;
import com.gitb.core.UsageEnumeration;
import com.gitb.vs.Void;
import eu.europa.ec.itb.csv.DomainConfig;
import eu.europa.ec.itb.csv.validation.ValidationConstants;
import eu.europa.ec.itb.validation.commons.artifact.ExternalArtifactSupport;
import eu.europa.ec.itb.validation.commons.artifact.TypedValidationArtifactInfo;
import eu.europa.ec.itb.validation.commons.artifact.ValidationArtifactInfo;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class ValidationServiceImplTest {

    @Test
    void testGetModuleDefinition() {
        var domainConfig = mock(DomainConfig.class);
        doReturn("domain1").when(domainConfig).getDomain();
        doReturn("service1").when(domainConfig).getWebServiceId();
        doReturn(true).when(domainConfig).hasMultipleValidationTypes();
        doReturn(true).when(domainConfig).definesTypesWithSettingInputs(any());
        doReturn(mock(DomainConfig.CsvOptions.class)).when(domainConfig).getCsvOptions();
        doReturn(true).when(domainConfig).definesTypeWithExternalSchemas();
        doAnswer((Answer<?>) ctx -> {
            var info = Map.of("type1", new TypedValidationArtifactInfo(), "type2", new TypedValidationArtifactInfo());
            info.get("type1").add(TypedValidationArtifactInfo.DEFAULT_TYPE, new ValidationArtifactInfo());
            info.get("type1").get().setExternalArtifactSupport(ExternalArtifactSupport.NONE);
            info.get("type2").add(TypedValidationArtifactInfo.DEFAULT_TYPE, new ValidationArtifactInfo());
            info.get("type2").get().setExternalArtifactSupport(ExternalArtifactSupport.NONE);
            return info;
        }).when(domainConfig).getArtifactInfo();
        doReturn(Map.ofEntries(
                descriptionEntryOf(ValidationConstants.INPUT_CONTENT),
                descriptionEntryOf(ValidationConstants.INPUT_EMBEDDING_METHOD),
                descriptionEntryOf(ValidationConstants.INPUT_VALIDATION_TYPE),
                descriptionEntryOf(ValidationConstants.INPUT_HAS_HEADERS),
                descriptionEntryOf(ValidationConstants.INPUT_DELIMITER),
                descriptionEntryOf(ValidationConstants.INPUT_QUOTE),
                descriptionEntryOf(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL),
                descriptionEntryOf(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL),
                descriptionEntryOf(ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL),
                descriptionEntryOf(ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL),
                descriptionEntryOf(ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL),
                descriptionEntryOf(ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL),
                descriptionEntryOf(ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL),
                descriptionEntryOf(ValidationConstants.INPUT_EXTERNAL_SCHEMA),
                descriptionEntryOf(ValidationConstants.INPUT_ADD_INPUT_TO_REPORT),
                descriptionEntryOf(ValidationConstants.INPUT_LOCALE)
        )).when(domainConfig).getWebServiceDescription();
        var service = new ValidationServiceImpl(domainConfig);
        var result = service.getModuleDefinition(new Void());
        assertNotNull(result);
        assertNotNull(result.getModule());
        assertEquals("service1", result.getModule().getId());
        assertNotNull(result.getModule().getInputs());
        assertEquals(16, result.getModule().getInputs().getParam().size());
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_CONTENT, result.getModule().getInputs().getParam().get(0), UsageEnumeration.R);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_EMBEDDING_METHOD, result.getModule().getInputs().getParam().get(1), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_VALIDATION_TYPE, result.getModule().getInputs().getParam().get(2), UsageEnumeration.R);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_HAS_HEADERS, result.getModule().getInputs().getParam().get(3), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_DELIMITER, result.getModule().getInputs().getParam().get(4), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_QUOTE, result.getModule().getInputs().getParam().get(5), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_COUNT_VIOLATION_LEVEL, result.getModule().getInputs().getParam().get(6), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_DIFFERENT_INPUT_FIELD_SEQUENCE_VIOLATION_LEVEL, result.getModule().getInputs().getParam().get(7), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_DUPLICATE_INPUT_FIELDS_VIOLATION_LEVEL, result.getModule().getInputs().getParam().get(8), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_FIELD_CASE_MISMATCH_VIOLATION_LEVEL, result.getModule().getInputs().getParam().get(9), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_MULTIPLE_INPUT_FIELDS_FOR_SCHEMA_FIELD_VIOLATION_LEVEL, result.getModule().getInputs().getParam().get(10), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_UNKNOWN_INPUT_FIELD_VIOLATION_LEVEL, result.getModule().getInputs().getParam().get(11), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_UNSPECIFIED_SCHEMA_FIELD_VIOLATION_LEVEL, result.getModule().getInputs().getParam().get(12), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_EXTERNAL_SCHEMA, result.getModule().getInputs().getParam().get(13), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_ADD_INPUT_TO_REPORT, result.getModule().getInputs().getParam().get(14), UsageEnumeration.O);
        assertWebServiceInputDocumentation(ValidationConstants.INPUT_LOCALE, result.getModule().getInputs().getParam().get(15), UsageEnumeration.O);
    }

    private Map.Entry<String, String> descriptionEntryOf(String inputName) {
        return Map.entry(inputName, "Description of "+inputName);
    }

    private void assertWebServiceInputDocumentation(String inputName, TypedParameter parameter, UsageEnumeration usage) {
        assertEquals(inputName, parameter.getName());
        assertEquals("Description of "+inputName, parameter.getDesc());
        assertEquals(usage, parameter.getUse());
    }
}
