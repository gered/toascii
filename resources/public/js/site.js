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

$(document).ready(function() {
	$('form.api-form').submit(function(e) {
		e.preventDefault();

		var form = $(this);
		var fieldset = form.find('fieldset');

		var params = {};
		form.find('.form-control').filter('[data-fieldname]').each(function() {
			var element = $(this);
			var fieldName = element.data('fieldname');
			var value = element.val();   // TODO: handle other types of elements (e.g. checkbox, radiobutton)
			params[fieldName] = value;
		});

		var textOutputContainer = form.siblings('pre.textOutputContainer');
		var htmlOutputContainer = form.siblings('div.htmlOutputContainer');
		var errorContainer = form.siblings('div.methodErrorContainer');

		var apiEndpoint = form.data('api-endpoint');

		textOutputContainer.text('');
		htmlOutputContainer.html('');
		errorContainer.html('');
		errorContainer.hide();

		fieldset.attr('disabled', true);

		var url = context + 'api' + apiEndpoint + '?' + jQuery.param(params);
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
