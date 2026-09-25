(function (global, J2S) {
	"use strict";
	
	// This code is loaded only when needed
	// from a call to new TrackerCamera().
	// The J2S.trackerApp variable is set in Tracker.start().
	// BH 2026.09.20

	var MAX_FRAMES = 1800;
	var DEFAULT_FPS = 10;
	var SCREEN_COUNTDOWN_SECONDS = 4;
	var JPEG_QUALITY = 0.92;
	var state = {
		stream: null,
		sourceKind: null,
		capturing: false,
		captureTimer: null,
		countingDown: false,
		countdownTimer: null,
		countdownRemaining: 0,
		capturePending: false,
		captureStartedAt: 0,
		captureStoppedAt: 0,
		frames: [],
		cropEnabled: false,
		crop: null,
		cropDraft: null,
		cropStart: null,
		cropPointerId: null
	};

	function element(id) {
		return document.getElementById(id);
	}

	function isIOSOrAndroid() {
		var userAgent = navigator.userAgent || "";
		var userAgentData = navigator.userAgentData;
		return !!(userAgentData && userAgentData.mobile) ||
			/Android|iPhone|iPad|iPod/i.test(userAgent) ||
			(/Macintosh/i.test(userAgent) && navigator.maxTouchPoints > 1);
	}

	function setStatus(message, isError) {
		var status = element("tracker-camera-status");
		if (!status) return;
		status.textContent = message;
		status.classList.toggle("tracker-camera-error", !!isError);
	}

	function setControls() {
		var hasStream = !!state.stream;
		var isBusy = state.capturing || state.countingDown;
		var openButton = element("tracker-camera-open");
		var screenButton = element("tracker-screen-open");
		var recordButton = element("tracker-camera-record");
		var stopButton = element("tracker-camera-stop");
		var cropToggle = element("tracker-camera-crop-toggle");
		var clearCropButton = element("tracker-camera-crop-clear");
		if (openButton) openButton.disabled = isBusy;
		if (screenButton) screenButton.disabled = isBusy;
		if (recordButton) recordButton.disabled = !hasStream || isBusy;
		if (stopButton) stopButton.disabled = !state.capturing;
		if (cropToggle) cropToggle.disabled = !hasStream || isBusy;
		if (clearCropButton) clearCropButton.disabled = !hasStream || isBusy || !state.crop;
		updateCropVisual();
	}

	function clearCropSelection() {
		state.crop = null;
		state.cropDraft = null;
		state.cropStart = null;
		state.cropPointerId = null;
		updateCropVisual();
	}

	function getVideoDisplayMetrics() {
		var preview = element("tracker-camera-preview");
		if (!preview || !preview.videoWidth || !preview.videoHeight) return null;
		var rect = preview.getBoundingClientRect();
		var scale = Math.min(rect.width / preview.videoWidth, rect.height / preview.videoHeight);
		var width = preview.videoWidth * scale;
		var height = preview.videoHeight * scale;
		return {
			left: (rect.width - width) / 2,
			top: (rect.height - height) / 2,
			width: width,
			height: height,
			pageLeft: rect.left,
			pageTop: rect.top
		};
	}

	function normalizedPointerPosition(event) {
		var metrics = getVideoDisplayMetrics();
		if (!metrics) return null;
		var x = (event.clientX - metrics.pageLeft - metrics.left) / metrics.width;
		var y = (event.clientY - metrics.pageTop - metrics.top) / metrics.height;
		return {
			x: Math.max(0, Math.min(1, x)),
			y: Math.max(0, Math.min(1, y))
		};
	}

	function cropFromPoints(start, end) {
		return {
			x: Math.min(start.x, end.x),
			y: Math.min(start.y, end.y),
			width: Math.abs(end.x - start.x),
			height: Math.abs(end.y - start.y)
		};
	}

	// Source-video pixels, with a top-left origin and exclusive maximum bounds.
	function getCropBounds(crop) {
		var preview = element("tracker-camera-preview");
		if (!state.stream || !preview || !preview.videoWidth || !preview.videoHeight) return null;
		crop = crop || { x: 0, y: 0, width: 1, height: 1 };
		function axis(start, size, limit) {
			var minimumSize = Math.min(10, limit);
			var min = Math.max(0, Math.min(limit - minimumSize, Math.round(start * limit)));
			var max = Math.max(min + minimumSize, Math.min(limit, Math.round((start + size) * limit)));
			return [min, max];
		}
		var x = axis(crop.x, crop.width, preview.videoWidth);
		var y = axis(crop.y, crop.height, preview.videoHeight);
		return { xmin: x[0], xmax: x[1], ymin: y[0], ymax: y[1] };
	}

	function cropFromBounds(bounds) {
		var preview = element("tracker-camera-preview");
		return {
			x: bounds.xmin / preview.videoWidth, y: bounds.ymin / preview.videoHeight,
			width: (bounds.xmax - bounds.xmin) / preview.videoWidth,
			height: (bounds.ymax - bounds.ymin) / preview.videoHeight
		};
	}

	function updateCropCoordinates() {
		var bounds = getCropBounds(state.cropEnabled ? state.cropDraft || state.crop : null);
		var preview = element("tracker-camera-preview");
		["xmin", "xmax", "ymin", "ymax"].forEach(function (key) {
			var input = element("tracker-camera-" + key);
			if (!input) return;
			input.value = bounds ? bounds[key] : "";
			input.disabled = !bounds || !state.cropEnabled || state.capturing || state.countingDown;
			if (!bounds) return;
			var axis = key.charAt(0);
			var limit = axis === "x" ? preview.videoWidth : preview.videoHeight;
			var minimumSize = Math.min(10, limit);
			input.min = key === axis + "min" ? 0 : bounds[axis + "min"] + minimumSize;
			input.max = key === axis + "min" ? bounds[axis + "max"] - minimumSize : limit;
		});
	}

	function editCropCoordinate(event) {
		var input = event.currentTarget;
		if (input.disabled) return;
		var bounds = getCropBounds(state.crop);
		if (!bounds) return;
		var value = input.value === "" ? NaN : Number(input.value);
		if (Number.isFinite(value)) {
			var key = input.id.substring("tracker-camera-".length);
			bounds[key] = Math.max(Number(input.min), Math.min(Number(input.max), Math.round(value)));
			state.crop = cropFromBounds(bounds);
		}
		setControls();
	}

	function updateCropVisual() {
		updateCropCoordinates();
		var selection = element("tracker-camera-selection");
		var layer = element("tracker-camera-selection-layer");
		if (!selection || !layer) return;
		layer.classList.toggle("tracker-camera-selection-active",
			state.cropEnabled && !!state.stream && !state.capturing && !state.countingDown);
		var crop = state.cropDraft || state.crop;
		var bounds = getCropBounds(crop);
		if (crop && bounds) crop = cropFromBounds(bounds);
		var metrics = getVideoDisplayMetrics();
		if (!state.cropEnabled || !crop || !metrics) {
			selection.hidden = true;
			return;
		}
		selection.hidden = false;
		selection.style.left = (metrics.left + crop.x * metrics.width) + "px";
		selection.style.top = (metrics.top + crop.y * metrics.height) + "px";
		selection.style.width = (crop.width * metrics.width) + "px";
		selection.style.height = (crop.height * metrics.height) + "px";
	}

	function beginCrop(event) {
		if (!state.cropEnabled || !state.stream || state.capturing || state.countingDown) return;
		var point = normalizedPointerPosition(event);
		if (!point) return;
		event.preventDefault();
		state.cropPointerId = event.pointerId;
		state.cropStart = point;
		state.cropDraft = cropFromPoints(point, point);
		event.currentTarget.setPointerCapture(event.pointerId);
		updateCropVisual();
	}

	function moveCrop(event) {
		if (state.cropPointerId !== event.pointerId || !state.cropDraft) return;
		var point = normalizedPointerPosition(event);
		if (!point) return;
		event.preventDefault();
		state.cropDraft = cropFromPoints(state.cropStart, point);
		updateCropVisual();
	}

	function finishCrop(event) {
		if (state.cropPointerId !== event.pointerId || !state.cropDraft) return;
		event.preventDefault();
		var metrics = getVideoDisplayMetrics();
		var crop = state.cropDraft;
		state.cropPointerId = null;
		state.cropStart = null;
		state.cropDraft = null;
		if (metrics && crop.width * metrics.width >= 8 && crop.height * metrics.height >= 8) {
			state.crop = cropFromBounds(getCropBounds(crop));
			setStatus("Region selected. Record will capture only the outlined area.");
		} else {
			state.crop = null;
			setStatus("Drag a larger rectangle over the video preview.", true);
		}
		updateCropVisual();
		setControls();
	}

	function cancelCrop(event) {
		if (state.cropPointerId !== event.pointerId) return;
		state.cropPointerId = null;
		state.cropStart = null;
		state.cropDraft = null;
		updateCropVisual();
	}

	function cancelScreenCountdown() {
		if (state.countdownTimer !== null) {
			global.clearInterval(state.countdownTimer);
		}
		state.countdownTimer = null;
		state.countingDown = false;
		state.countdownRemaining = 0;
		var countdown = element("tracker-camera-countdown");
		if (countdown) countdown.hidden = true;
	}

	function stopTracks() {
		cancelScreenCountdown();
		if (state.stream) {
			state.stream.getTracks().forEach(function (track) { track.stop(); });
			state.stream = null;
		}
		state.sourceKind = null;
		var preview = element("tracker-camera-preview");
		if (preview) preview.srcObject = null;
	}

	async function attachStream(stream, sourceKind) {
		state.stream = stream;
		state.sourceKind = sourceKind;
		var preview = element("tracker-camera-preview");
		preview.srcObject = stream;
		try {
			await preview.play();
		} catch (error) {
			stream.getTracks().forEach(function (track) { track.stop(); });
			if (state.stream === stream) {
				state.stream = null;
				state.sourceKind = null;
				preview.srcObject = null;
			}
			throw error;
		}

		var cropToggle = element("tracker-camera-crop-toggle");
		if (sourceKind === "screen") {
			state.cropEnabled = true;
			cropToggle.checked = true;
		}
		clearCropSelection();
		setStatus(state.cropEnabled
			? (sourceKind === "screen" ? "Screen ready. " : "Camera ready. ") +
				"Drag over the preview to select the capture region."
			: "Camera ready. Keep the phone steady, then start recording.");
		setControls();

		var videoTrack = stream.getVideoTracks()[0];
		if (videoTrack) {
			videoTrack.addEventListener("ended", function () {
				if (state.stream !== stream) return;
				cancelScreenCountdown();
				if (state.capturing) {
					if (state.frames.length) {
						stopAndImport();
						return;
					}
					state.capturing = false;
					global.clearInterval(state.captureTimer);
					state.captureTimer = null;
				}
				state.stream = null;
				state.sourceKind = null;
				preview.srcObject = null;
				setStatus(sourceKind === "screen" ? "Screen sharing ended." : "Camera stopped.");
				setControls();
			});
		}
	}

	async function openCamera(facingMode) {
		if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
			throw new Error("Camera access is not supported by this browser.");
		}
		if (!global.isSecureContext) {
			throw new Error("Camera access requires HTTPS or localhost.");
		}

		stopTracks();
		setStatus("Requesting camera access...");
		var stream = await navigator.mediaDevices.getUserMedia({
			audio: false,
			video: {
				facingMode: { ideal: facingMode || "environment" },
				width: { ideal: 1920 },
				height: { ideal: 1080 }
			}
		});
		await attachStream(stream, "camera");
	}

	async function openScreen() {
		if (!navigator.mediaDevices || !navigator.mediaDevices.getDisplayMedia) {
			throw new Error("Screen capture is not supported by this browser.");
		}
		if (!global.isSecureContext) {
			throw new Error("Screen capture requires HTTPS or localhost.");
		}

		stopTracks();
		setStatus("Select a window in the browser's screen-capture list...");
		var stream = await navigator.mediaDevices.getDisplayMedia({
			audio: false,
			video: { displaySurface: "window" },
			preferCurrentTab: false,
			selfBrowserSurface: "exclude",
			surfaceSwitching: "exclude"
		});
		await attachStream(stream, "screen");
	}

	function canvasToBlob(canvas) {
		return new Promise(function (resolve, reject) {
			canvas.toBlob(function (blob) {
				if (blob) resolve(blob);
				else reject(new Error("The browser could not encode a video frame."));
			}, "image/jpeg", JPEG_QUALITY);
		});
	}

	async function captureFrame() {
		if (!state.capturing || state.capturePending || state.frames.length >= MAX_FRAMES) return;
		var preview = element("tracker-camera-preview");
		if (!preview || !preview.videoWidth || !preview.videoHeight) return;

		state.capturePending = true;
		var reachedLimit = false;
		try {
			var canvas = element("tracker-camera-canvas");
			var bounds = getCropBounds(state.cropEnabled ? state.crop : null);
			var sourceX = bounds.xmin;
			var sourceY = bounds.ymin;
			var sourceWidth = bounds.xmax - bounds.xmin;
			var sourceHeight = bounds.ymax - bounds.ymin;
			canvas.width = sourceWidth;
			canvas.height = sourceHeight;
			canvas.getContext("2d", { alpha: false }).drawImage(
				preview,
				sourceX, sourceY, sourceWidth, sourceHeight,
				0, 0, sourceWidth, sourceHeight
			);
			state.frames.push(await canvasToBlob(canvas));
			setStatus("Recording: " + state.frames.length + " frames captured");
			reachedLimit = state.frames.length >= MAX_FRAMES;
		} catch (error) {
			state.capturing = false;
			global.clearInterval(state.captureTimer);
			state.captureTimer = null;
			setStatus(error.message || String(error), true);
			setControls();
		} finally {
			state.capturePending = false;
		}
		if (reachedLimit) stopAndImport();
	}

	function beginCapture() {
		if (!state.stream || state.capturing || state.countingDown) return;
		var fps = Number(element("tracker-camera-fps").value) || DEFAULT_FPS;
		state.frames = [];
		state.capturing = true;
		state.captureStartedAt = performance.now();
		state.captureStoppedAt = 0;
		captureFrame();
		state.captureTimer = global.setInterval(captureFrame, Math.round(1000 / fps));
		setStatus("Recording: 0 frames captured");
		setControls();
	}

	function startScreenCountdown() {
		cancelScreenCountdown();
		state.countingDown = true;
		state.countdownRemaining = SCREEN_COUNTDOWN_SECONDS;
		var countdown = element("tracker-camera-countdown");
		countdown.textContent = state.countdownRemaining;
		countdown.hidden = false;
		setStatus("Screen recording starts in " + state.countdownRemaining + " seconds...");
		setControls();
		state.countdownTimer = global.setInterval(function () {
			if (!state.stream || state.sourceKind !== "screen") {
				cancelScreenCountdown();
				setControls();
				return;
			}
			state.countdownRemaining--;
			if (state.countdownRemaining > 0) {
				countdown.textContent = state.countdownRemaining;
				setStatus("Screen recording starts in " + state.countdownRemaining + " seconds...");
				return;
			}
			cancelScreenCountdown();
			beginCapture();
		}, 1000);
	}

	function startCapture() {
		if (!state.stream || state.capturing || state.countingDown) return;
		if (state.cropEnabled && !state.crop) {
			setStatus("Drag over the preview to select a capture region first.", true);
			return;
		}
		if (state.sourceKind === "screen") startScreenCountdown();
		else beginCapture();
	}

	function waitForPendingCapture() {
		return new Promise(function (resolve) {
			function check() {
				if (!state.capturePending) resolve();
				else global.setTimeout(check, 20);
			}
			check();
		});
	}

	async function getImageStack(frames, fps) {
		var data = [];
		for (var i = 0; i < frames.length; i++) {
			data.push(J2S._toBytes(await frames[i].arrayBuffer()));
		}
		return data;
	}

	async function stopAndImport() {
		if (!state.capturing) return;
		state.capturing = false;
		state.captureStoppedAt = performance.now();
		global.clearInterval(state.captureTimer);
		state.captureTimer = null;
		setControls();
		setStatus("Preparing " + state.frames.length + " frames for Tracker...");

		try {
			await waitForPendingCapture();
			if (!state.frames.length) throw new Error("No video frames were captured.");
			var requestedFps = Number(element("tracker-camera-fps").value) || DEFAULT_FPS;
			var elapsedSeconds = (state.captureStoppedAt - state.captureStartedAt) / 1000;
			var fps = state.frames.length > 1 && elapsedSeconds > 0
				? (state.frames.length - 1) / elapsedSeconds
				: requestedFps;
			var data = await getImageStack(state.frames, fps);
			trackerApp.importVideoCapture(Date.now().toString(36), data, fps);
			setStatus("Imported " + state.frames.length + " captured frames into Tracker.");
			stopTracks();
			global.setTimeout(closeDialog, 700);
		} catch (error) {
			setStatus(error.message || String(error), true);
			setControls();
		}
	}

	function closeDialog() {
		if (state.capturing) {
			state.capturing = false;
			global.clearInterval(state.captureTimer);
		}
		state.captureTimer = null;
		state.capturePending = false;
		state.frames = [];
		stopTracks();
		var dialog = element("tracker-camera-dialog");
		if (dialog) dialog.hidden = true;
		setControls();
	}

	var trackerApp;
	
	function showDialog(app) {
		trackerApp = app;
		var dialog = element("tracker-camera-dialog") || createDialog();
		dialog.hidden = false;
		dialog.focus();
		setStatus("");
		setControls();
	}

	function createDialog() {
		var showScreenCapture = !isIOSOrAndroid();
		var screenHelp = '<p id="tracker-screen-capture-help" class="tracker-camera-help">' +
			'For <strong>Camera capture</strong> start the camera and press record. ' +
			(showScreenCapture ? 'For <strong>Screen capture</strong> press that button, select a browser window from the list, ' +
				'and press record for a four-second count-down. ' : '') +
			'You can choose frames/sec or select a region before recording a video.</p>';
		var screenButton = showScreenCapture
			? '<button id="tracker-screen-open" type="button">Screen capture</button>'
			: "";
		var dialog = document.createElement("div");
		dialog.id = "tracker-camera-dialog";
		dialog.hidden = true;
		dialog.tabIndex = -1;
		dialog.setAttribute("role", "dialog");
		dialog.setAttribute("aria-modal", "true");
		dialog.setAttribute("aria-label", "Capture video for Tracker");
		dialog.setAttribute("aria-describedby", "tracker-screen-capture-help");
		dialog.innerHTML =
			'<div class="tracker-camera-content">' +
			screenHelp +
			'<div id="tracker-camera-preview-wrap">' +
			'<video id="tracker-camera-preview" playsinline muted></video>' +
			'<div id="tracker-camera-selection-layer" aria-label="Video capture region">' +
			'<div id="tracker-camera-selection" hidden></div></div>' +
			'<div id="tracker-camera-countdown" aria-hidden="true" hidden></div></div>' +
			'<canvas id="tracker-camera-canvas" hidden></canvas>' +
			'<div class="tracker-camera-controls">' +
			'<button id="tracker-camera-open" type="button">Start camera</button>' +
			screenButton +
			'<label>Frames/sec <select id="tracker-camera-fps"><option>5</option><option selected>10</option><option>15</option><option>30</option></select></label>' +
			'</div><div class="tracker-camera-controls">' +
			'<button id="tracker-camera-crop-clear" type="button" disabled>Clear region</button>' +
			'<label><input id="tracker-camera-crop-toggle" type="checkbox" disabled> Capture selected region only</label>' +
			'</div><div class="tracker-camera-coordinates" aria-label="Capture coordinates">' +
			'<span>Video pixels (origin: top left)</span>' +
			'<label>X min <input id="tracker-camera-xmin" type="number" step="1" disabled></label>' +
			'<label>X max <input id="tracker-camera-xmax" type="number" step="1" disabled></label>' +
			'<label>Y min <input id="tracker-camera-ymin" type="number" step="1" disabled></label>' +
			'<label>Y max <input id="tracker-camera-ymax" type="number" step="1" disabled></label>' +
			'</div><div class="tracker-camera-controls">' +
			'<button id="tracker-camera-record" type="button">Record</button>' +
			'<button id="tracker-camera-stop" type="button">Stop and import</button>' +
			'<button id="tracker-camera-cancel" type="button">Cancel</button>' +
			'</div><p id="tracker-camera-status" role="status" aria-live="polite"></p></div>';
		element("tracker-camera-host").appendChild(dialog);

		element("tracker-camera-open").addEventListener("click", function () {
			openCamera("environment").catch(function (error) {
				setStatus(error.message || String(error), true);
				setControls();
			});
		});
		var screenOpenButton = element("tracker-screen-open");
		if (screenOpenButton) {
			screenOpenButton.addEventListener("click", function () {
				openScreen().catch(function (error) {
					setStatus(error.message || String(error), true);
					setControls();
				});
			});
		}
		element("tracker-camera-record").addEventListener("click", startCapture);
		element("tracker-camera-stop").addEventListener("click", stopAndImport);
		element("tracker-camera-cancel").addEventListener("click", closeDialog);
		element("tracker-camera-crop-toggle").addEventListener("change", function (event) {
			state.cropEnabled = event.currentTarget.checked;
			updateCropVisual();
			setStatus(state.cropEnabled
				? "Drag over the video preview to select the capture region."
				: "The full video source will be captured.");
		});
		element("tracker-camera-crop-clear").addEventListener("click", function () {
			clearCropSelection();
			setStatus("Drag over the video preview to select a new capture region.");
			setControls();
		});
		["xmin", "xmax", "ymin", "ymax"].forEach(function (key) {
			var input = element("tracker-camera-" + key);
			input.addEventListener("change", editCropCoordinate);
			input.addEventListener("keydown", function (event) {
				if (event.key === "Enter") editCropCoordinate(event);
			});
		});
		var selectionLayer = element("tracker-camera-selection-layer");
		selectionLayer.addEventListener("pointerdown", beginCrop);
		selectionLayer.addEventListener("pointermove", moveCrop);
		selectionLayer.addEventListener("pointerup", finishCrop);
		selectionLayer.addEventListener("pointercancel", cancelCrop);
		element("tracker-camera-preview").addEventListener("resize", updateCropVisual);
		dialog.addEventListener("keydown", function (event) {
			if (event.key === "Escape") closeDialog();
		});
		return dialog;
	}

	function addStyles() {
		var style = document.createElement("style");
		style.textContent =
			"#tracker-camera-dialog{position:fixed;z-index:1000000;inset:0;display:flex;align-items:center;justify-content:center;padding:16px;background:#0009}" +
			"#tracker-camera-dialog[hidden]{display:none}" +
			".tracker-camera-content{box-sizing:border-box;width:min(92vw,720px);padding:16px;border-radius:10px;background:#fff;box-shadow:0 12px 45px #0008;font:15px/1.35 sans-serif;color:#222}" +
			".tracker-camera-help{margin:0 0 10px}" +
			"#tracker-camera-preview-wrap{position:relative;overflow:hidden;border-radius:6px;background:#111}" +
			"#tracker-camera-preview{display:block;width:100%;max-height:62vh;background:#111;border-radius:6px;object-fit:contain}" +
			"#tracker-camera-selection-layer{position:absolute;inset:0;pointer-events:none}" +
			"#tracker-camera-selection-layer.tracker-camera-selection-active{pointer-events:auto;cursor:crosshair;touch-action:none}" +
			"#tracker-camera-selection{position:absolute;box-sizing:border-box;border:2px solid #ffe600;background:#ffe60022;box-shadow:0 0 0 9999px #0007}" +
			"#tracker-camera-countdown{position:absolute;z-index:2;left:50%;top:50%;transform:translate(-50%,-50%);min-width:1.4em;text-align:center;color:#fff;font:bold clamp(64px,18vw,150px)/1 sans-serif;text-shadow:0 3px 12px #000;background:#0008;border-radius:14px;padding:.08em .18em}" +
			".tracker-camera-controls{display:flex;flex-wrap:wrap;gap:8px;align-items:center;margin-top:12px}" +
			".tracker-camera-coordinates{display:flex;flex-wrap:wrap;gap:8px;align-items:center;margin-top:12px}" +
			".tracker-camera-coordinates span{flex-basis:100%}" +
			".tracker-camera-coordinates input{width:5em;font:inherit;padding:4px}" +
			".tracker-camera-controls button,.tracker-camera-controls select{font:inherit;padding:7px 10px}" +
			"#tracker-camera-status{min-height:1.4em;margin:10px 0 0}" +
			".tracker-camera-error{color:#a40000;font-weight:600}";
		document.head.appendChild(style);
	}

	function mount() {
		if (element("tracker-camera-host")) return;
		addStyles();
		var host = document.createElement("div");
		host.id = "tracker-camera-host";
		global.addEventListener("resize", updateCropVisual);
		document.body.appendChild(host);
	}
	J2S.TrackerCameraImporter = {
		createDialog: function(app) {
			showDialog(app) 
		}
	};
	mount();
})(window, J2S);
