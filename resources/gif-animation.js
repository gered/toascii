setTimeout(function() {
	var scripts = document.getElementsByTagName('script');
	var thisScript = scripts[scripts.length - 1];

	var frameContainer = thisScript.previousSibling;
	var frames = frameContainer.children;
	var currentFrame = frames[0];

	function animate() {
		currentFrame.style.display = 'block';
		var delay = parseInt(currentFrame.dataset['delay']);
		setTimeout(function() {
			currentFrame.style.display = 'none';
			currentFrame = currentFrame.nextSibling;
			if (currentFrame == null)
				currentFrame = frames[0];
			animate();
		}, delay);
	}

	animate();

}, 100);