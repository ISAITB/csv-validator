addListener('FORM_READY', setupSettingInputs)
addListener('VALIDATION_TYPE_CHANGED', setupSettingInputs);
addListener('SUBMIT_STATUS_VALIDATED', syntaxInputChanged)
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
function syntaxInputChanged() {
    var validationType = $('#validationType').val();
    if (validationType && validationType.trim() != '') {
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
function setupSettingInputs(eventType, eventData) {
    $("#csvSettingsCheck").prop('checked', false);
    $('#inputDelimiter').val('');
    $('#inputQuote').val('');
    var validationType = $('#validationType').val(),
        hasOptional = false, hasRequired = false,
        showHeaders = false, showDelimiter = false, showQuote = false,
        delimiterRequired = false, quoteRequired = false;
    if (validationType && validationType.trim() != '') {
        delimiterRequired = settingInputs.delimiter[validationType].control == 'REQUIRED';
        quoteRequired = settingInputs.quote[validationType].control == 'REQUIRED';
        if (settingInputs.headers[validationType].control == 'REQUIRED'
            || delimiterRequired
            || quoteRequired) {
            hasRequired = true;
        } else if (settingInputs.headers[validationType].control == 'OPTIONAL'
           || settingInputs.delimiter[validationType].control == 'OPTIONAL'
           || settingInputs.quote[validationType].control == 'OPTIONAL') {
           hasOptional = true;
        }
        showHeaders = settingInputs.headers[validationType].control != 'NONE';
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
        } else {
            $('#csvSettingsFormHeaders').addClass('hidden');
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
