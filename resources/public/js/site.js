function isHtmlResponse(xhr) {
	var contentType = xhr.getResponseHeader('content-type');
	return contentType.indexOf('text/html') !== -1;
}

function isTextResponse(xhr) {
	var contentType = xhr.getResponseHeader('content-type');
	return contentType.indexOf('text/plain') !== -1;
}

function isJsonResponse(xhr) {
	var contentType = xhr.getResponseHeader('content-type');
	return contentType.indexOf('application/json') !== -1;
}

function getValueOf(element) {
	if (element.prop('tagName') === 'INPUT' && element.attr('type') === 'checkbox')
		return element.is(':checked') ? element.val() : element.data('unchecked-value');
	else
		return element.val();
}

function getMethodParams(form) {
	var params = {};
	form.find('.form-control, input[type=checkbox]').filter('[data-fieldname]').each(function() {
		var element = $(this);
		var fieldName = element.data('fieldname');
		var value = getValueOf(element);

		if (value && value.length > 0)
			params[fieldName] = value;
	});
	return params;
}

function getAdditionalUrl(form) {
	var urlParts = [];
	form.find('.form-control, input[type=checkbox]').filter('[data-uri-path-order]').each(function() {
		var element = $(this);
		var order = parseInt(element.data('uri-path-order'));
		var value = getValueOf(element);

		if (value && value.length > 0)
			urlParts.push({order: order, value: value});
	});

	urlParts.sort(function(a, b) {
		if (a.order < b.order)
			return -1;
		else if (a.order > b.order)
			return 1;
		else
			return 0;
	})

	var urlPartStrings = [];
	urlParts.forEach(function(value) {
		urlPartStrings.push(value.value);
	});

	if (urlPartStrings.length > 0)
		return '/' + urlPartStrings.join('/');
	else
		return '';
}

$(document).ready(function() {
	$('form.api-form').submit(function(e) {
		e.preventDefault();

		var form = $(this);
		var fieldset = form.find('fieldset');
		var params = getMethodParams(form)
		var additionalUrl = getAdditionalUrl(form);

		var textOutputContainer = form.siblings('pre.textOutputContainer');
		var htmlOutputContainer = form.siblings('div.htmlOutputContainer');
		var errorContainer = form.siblings('div.methodErrorContainer');
		var methodCallDisplay = form.siblings('div.methodCallDisplay');
		var methodCallLink = methodCallDisplay.find('a.url');

		var apiEndpoint = form.data('api-endpoint') + additionalUrl;

		textOutputContainer.text('');
		htmlOutputContainer.html('');
		errorContainer.html('');
		errorContainer.hide();

		fieldset.attr('disabled', true);

		var url = context + 'api' + apiEndpoint + '?' + jQuery.param(params);
		methodCallLink.text('GET ' + url).attr('href', url);
		methodCallDisplay.show();

		$.get(url)
			.done(
			function (data, status, xhr) {
				if (isHtmlResponse(xhr)) {
					textOutputContainer.hide();
					htmlOutputContainer.html(data);
					htmlOutputContainer.show();
				} else {
					htmlOutputContainer.hide();
					textOutputContainer.text(data);
					textOutputContainer.show();
				}

				fieldset.attr('disabled', null);
			})
			.fail(
			function (response) {
				textOutputContainer.hide();
				htmlOutputContainer.hide();

				errorContainer.html(
					'<strong>HTTP ' + response.status + ' ' + response.statusText + ':</strong> ' +
					response.responseText
				);
				errorContainer.show();

				fieldset.attr('disabled', null);
			});
	})

});
