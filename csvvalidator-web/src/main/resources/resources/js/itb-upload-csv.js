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

addListener('FORM_READY', setupSettingInputs)
addListener('VALIDATION_TYPE_CHANGED', setupSettingInputs);
addListener('SUBMIT_STATUS_VALIDATED', syntaxInputChanged);
addListener('ADDED_EXTERNAL_ARTIFACT_INPUT', addedExternalSchema);
function toggleCsvSettings() {
    var checked = $("#csvSettingsCheck").is(":checked")
    if (checked) {
        $('#csvSettingsForm').removeClass('hidden');
    } else {
        $('#csvSettingsForm').addClass('hidden');
        $('#inputDelimiter').val('');
        $('#inputQuote').val('');
    }
}
function addedExternalSchema(event, info) {
    if (info) {
        var codeMirror = getCodeMirrorNative('#text-editor-'+info.elementId)
        if (codeMirror) {
            codeMirror.setOption("mode", {name: "javascript", json: true});
            codeMirror.refresh();
        }
    }
}
function syntaxInputChanged() {
    var validationType = getCompleteValidationType();
    if (validationType) {
        var delimiter = $('#inputDelimiter').val(),
            quote = $('#inputQuote').val(),
            delimiterRequired = settingInputs.delimiter[validationType].control == 'REQUIRED',
            quoteRequired = settingInputs.quote[validationType].control == 'REQUIRED';
        if ((delimiterRequired && (delimiter == undefined || delimiter == ''))
            || (quoteRequired && (quote == undefined || quote == ''))) {
            $('#inputFileSubmit').prop('disabled', true);
        } else {
            updateSubmitStatus();
        }
    }
}
function headerCheckChanged() {
    var checked = $("#inputHeaders").is(":checked")
    var validationType = getCompleteValidationType();
    if (checked && (anyViolationInputAtLevel(validationType, 'REQUIRED') || anyViolationInputAtLevel(validationType, 'OPTIONAL'))) {
        $('#violationDiv').removeClass('hidden');
    } else {
        $('#violationDiv').addClass('hidden');
    }
}

function anyViolationInputAtLevel(validationType, level) {
    return settingInputs.differentInputFieldCount[validationType].control == level
            || settingInputs.differentInputFieldSequence[validationType].control == level
            || settingInputs.unknownInputField[validationType].control == level
            || settingInputs.unspecifiedSchemaField[validationType].control == level
            || settingInputs.inputFieldCaseMismatch[validationType].control == level
            || settingInputs.duplicateInputFields[validationType].control == level
            || settingInputs.multipleInputFieldsForSchemaField[validationType].control == level
}

function setupSettingInputs(eventType, eventData) {
    $('.toggleCsvSettings').off().on('click', toggleCsvSettings);
    $('.syntaxInputChanged').off().on('change', syntaxInputChanged);
    $('.headerCheckChanged').off().on('change', headerCheckChanged);
    $("#csvSettingsCheck").prop('checked', false);
    $('#inputDelimiter').val('');
    $('#inputQuote').val('');
    var validationType = getCompleteValidationType(),
        hasOptional = false, hasRequired = false,
        showHeaders = false, showDelimiter = false, showQuote = false,
        showDifferentInputFieldCount = false,
        showDifferentInputFieldSequence = false,
        showUnknownInputField = false,
        showUnspecifiedSchemaField = false,
        showInputFieldCaseMismatch = false,
        showDuplicateInputFields = false,
        showMultipleInputFieldsForSchemaField = false,
        delimiterRequired = false, quoteRequired = false;
    if (validationType && validationType.trim() != '') {
        delimiterRequired = settingInputs.delimiter[validationType].control == 'REQUIRED';
        quoteRequired = settingInputs.quote[validationType].control == 'REQUIRED';
        if (settingInputs.headers[validationType].control == 'REQUIRED'
            || anyViolationInputAtLevel(validationType, 'REQUIRED')
            || delimiterRequired
            || quoteRequired) {
            hasRequired = true;
        } else if (settingInputs.headers[validationType].control == 'OPTIONAL'
            || anyViolationInputAtLevel(validationType, 'OPTIONAL')
            || settingInputs.delimiter[validationType].control == 'OPTIONAL'
            || settingInputs.quote[validationType].control == 'OPTIONAL'
           ) {
           hasOptional = true;
        }
        showHeaders = settingInputs.headers[validationType].control != 'NONE';
        showDifferentInputFieldCount = settingInputs.differentInputFieldCount[validationType].control != 'NONE';
        showDifferentInputFieldSequence = settingInputs.differentInputFieldSequence[validationType].control != 'NONE';
        showUnknownInputField = settingInputs.unknownInputField[validationType].control != 'NONE';
        showUnspecifiedSchemaField = settingInputs.unspecifiedSchemaField[validationType].control != 'NONE';
        showInputFieldCaseMismatch = settingInputs.inputFieldCaseMismatch[validationType].control != 'NONE';
        showDuplicateInputFields = settingInputs.duplicateInputFields[validationType].control != 'NONE';
        showMultipleInputFieldsForSchemaField = settingInputs.multipleInputFieldsForSchemaField[validationType].control != 'NONE';
        showDelimiter = settingInputs.delimiter[validationType].control != 'NONE';
        showQuote = settingInputs.quote[validationType].control != 'NONE';
    }
    if (!hasOptional && !hasRequired) {
        $('#csvSettingsDiv').addClass('hidden');
    } else {
        $('#csvSettingsDiv').removeClass('hidden');
        if (hasRequired) {
            $("#csvSettingsCheck").prop('checked', true);
            $('#csvSettingsCheckDiv').addClass('hidden');
            $('#csvSettingsForm').removeClass('hidden');
        } else {
            $('#csvSettingsCheckDiv').removeClass('hidden');
            $('#csvSettingsForm').addClass('hidden');
        }
        if (showHeaders) {
            $('#csvSettingsFormHeaders').removeClass('hidden');
            $("#inputHeaders").prop('checked', settingInputs.headers[validationType].defaultValue);
        } else {
            $('#csvSettingsFormHeaders').addClass('hidden');
        }
        if (showDifferentInputFieldCount) {
            $('#csvSettingsFormDifferentInputFieldCount').removeClass('hidden');
            $('#csvSettingsFormDifferentInputFieldCountControl').val(settingInputs.differentInputFieldCount[validationType].defaultValue);
        } else {
            $('#csvSettingsFormDifferentInputFieldCount').addClass('hidden');
        }
        if (showDifferentInputFieldSequence) {
            $('#csvSettingsFormDifferentInputFieldSequence').removeClass('hidden');
            $('#csvSettingsFormDifferentInputFieldSequenceControl').val(settingInputs.differentInputFieldSequence[validationType].defaultValue);
        } else {
            $('#csvSettingsFormDifferentInputFieldSequence').addClass('hidden');
        }
        if (showUnknownInputField) {
            $('#csvSettingsFormUnknownInputField').removeClass('hidden');
            $('#csvSettingsFormUnknownInputFieldControl').val(settingInputs.unknownInputField[validationType].defaultValue);
        } else {
            $('#csvSettingsFormUnknownInputField').addClass('hidden');
        }
        if (showUnspecifiedSchemaField) {
            $('#csvSettingsFormUnspecifiedSchemaField').removeClass('hidden');
            $('#csvSettingsFormUnspecifiedSchemaFieldControl').val(settingInputs.unspecifiedSchemaField[validationType].defaultValue);
        } else {
            $('#csvSettingsFormUnspecifiedSchemaField').addClass('hidden');
        }
        if (showInputFieldCaseMismatch) {
            $('#csvSettingsFormInputFieldCaseMismatch').removeClass('hidden');
            $('#csvSettingsFormInputFieldCaseMismatchControl').val(settingInputs.inputFieldCaseMismatch[validationType].defaultValue);
        } else {
            $('#csvSettingsFormInputFieldCaseMismatch').addClass('hidden');
        }
        if (showDuplicateInputFields) {
            $('#csvSettingsFormDuplicateInputFields').removeClass('hidden');
            $('#csvSettingsFormDuplicateInputFieldsControl').val(settingInputs.duplicateInputFields[validationType].defaultValue);
        } else {
            $('#csvSettingsFormDuplicateInputFields').addClass('hidden');
        }
        if (showMultipleInputFieldsForSchemaField) {
            $('#csvSettingsFormMultipleInputFieldsForSchemaField').removeClass('hidden');
            $('#csvSettingsFormMultipleInputFieldsForSchemaFieldControl').val(settingInputs.multipleInputFieldsForSchemaField[validationType].defaultValue);
        } else {
            $('#csvSettingsFormMultipleInputFieldsForSchemaField').addClass('hidden');
        }
        if (showDelimiter) {
            toggleRequiredPlaceholderText('inputDelimiter', delimiterRequired)
            $('#csvSettingsFormDelimiter').removeClass('hidden');
        } else {
            $('#csvSettingsFormDelimiter').addClass('hidden');
        }
        if (showQuote) {
            toggleRequiredPlaceholderText('inputQuote', quoteRequired)
            $('#csvSettingsFormQuote').removeClass('hidden');
        } else {
            $('#csvSettingsFormQuote').addClass('hidden');
        }

        if (showDifferentInputFieldCount
            || showDifferentInputFieldSequence
            || showUnknownInputField
            || showUnspecifiedSchemaField
            || showInputFieldCaseMismatch
            || showDuplicateInputFields
            || showMultipleInputFieldsForSchemaField) {
            if (!showDelimiter && !showQuote && !showHeaders) {
                $('#settingsDiv').addClass('hidden');
                $('#violationDivContainer').removeClass('with-top-margin');
            } else {
                $('#settingsDiv').removeClass('hidden');
                $('#violationDivContainer').addClass('with-top-margin');
            }
            if ((showHeaders && $('#inputHeaders').is(":checked")) || (!showHeaders && settingInputs.headers[validationType].defaultValue)) {
                $('#violationDiv').removeClass('hidden');
            } else {
                $('#violationDiv').addClass('hidden');
            }
        } else {
            $('#violationDiv').addClass('hidden');
        }
    }
}

function toggleRequiredPlaceholderText(inputId, required) {
    var placeholder = $('#'+inputId).attr('placeholder');
    var hasRequiredMark = endsWith(placeholder, ' *');
    if (required && !hasRequiredMark) {
        $('#'+inputId).attr('placeholder', (placeholder += ' *'));
    } else if (!required && hasRequiredMark) {
        $('#'+inputId).attr('placeholder', placeholder.substring(0, placeholder.length-2));
    }
}
function endsWith(string1, string2) {
    return (string1.lastIndexOf(string2) == (string1.length - string2.length) && string1.lastIndexOf(string2) >= 0);
}
