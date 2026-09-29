(function (global, J2S) {
	"use strict";
	
	// This code is loaded only when needed
	// from a call to new TrackerCamera().
	// The J2S.trackerApp variable is set in Tracker.start().
	// BH 2026.09.20

	// SwingJS replaces window.Error with java.lang.Error. Its plain JS constructor
	// loses the message; use the native constructor saved by the runtime.
	var CaptureError = global.Clazz && global.Clazz._Error || global.Error;

	function reportError(error) {
		if (global.console && global.console.error) {
			global.console.error("Tracker capture failed:", error, error && error.stack || "");
		}
		var message = error && (error.message ||
			(typeof error.getMessage$ === "function" && error.getMessage$()));
		setStatus(message || String(error), true);
	}

	var MAX_FRAMES = 1800;
	var DEFAULT_FPS = 10;
	var SCREEN_COUNTDOWN_SECONDS = 4;
	var JPEG_QUALITY = 0.92;
	var state = {
		format: "images",
		recorder: null,
		recorderStream: null,
		recordingResult: null,
		importing: false,
		session: 0,
		stream: null,
		sourceKind: null,
		selectedSource: "camera",
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
		var status = element("tracker-mp4-status");
		if (!status) return;
		status.textContent = message;
		status.classList.toggle("tracker-mp4-error", !!isError);
	}

	function setControls() {
		var hasStream = !!state.stream;
		var isBusy = state.capturing || state.countingDown || state.importing;
		element("tracker-mp4-fps").disabled = isBusy;
		document.querySelectorAll('input[name="tracker-capture-format"]').forEach(function (input) {
			input.disabled = isBusy;
		});
		var openButton = element("tracker-mp4-open");
		var screenButton = element("tracker-mp4-screen-open");
		var recordButton = element("tracker-mp4-record");
		var cropToggle = element("tracker-mp4-crop-toggle");
		var clearCropButton = element("tracker-mp4-crop-clear");
		var cameraSource = element("tracker-mp4-source-camera");
		var screenSource = element("tracker-mp4-source-screen");
		if (cameraSource) {
			cameraSource.checked = state.selectedSource === "camera";
			cameraSource.disabled = isBusy;
		}
		if (screenSource) {
			screenSource.checked = state.selectedSource === "screen";
			screenSource.disabled = isBusy || isIOSOrAndroid() ||
				!navigator.mediaDevices || !navigator.mediaDevices.getDisplayMedia;
		}
		if (openButton) {
			openButton.disabled = isBusy;
			openButton.hidden = state.selectedSource !== "camera";
		}
		if (screenButton) screenButton.hidden = state.selectedSource !== "screen";
		if (screenButton) screenButton.disabled = isBusy;
		if (recordButton) {
			recordButton.textContent = state.capturing || state.countingDown ? "Stop" : "Record";
			recordButton.disabled = !hasStream || state.importing;
		}
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
		var preview = element("tracker-mp4-preview");
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
		var preview = element("tracker-mp4-preview");
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
		var preview = element("tracker-mp4-preview");
		return {
			x: bounds.xmin / preview.videoWidth, y: bounds.ymin / preview.videoHeight,
			width: (bounds.xmax - bounds.xmin) / preview.videoWidth,
			height: (bounds.ymax - bounds.ymin) / preview.videoHeight
		};
	}

	function updateCropCoordinates() {
		var bounds = getCropBounds(state.cropEnabled ? state.cropDraft || state.crop : null);
		var preview = element("tracker-mp4-preview");
		["xmin", "xmax", "ymin", "ymax"].forEach(function (key) {
			var input = element("tracker-mp4-" + key);
			if (!input) return;
			input.value = bounds ? bounds[key] : "";
			input.disabled = !bounds || !state.cropEnabled || state.capturing || state.countingDown || state.importing;
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
			var key = input.id.substring("tracker-mp4-".length);
			bounds[key] = Math.max(Number(input.min), Math.min(Number(input.max), Math.round(value)));
			state.crop = cropFromBounds(bounds);
		}
		setControls();
	}

	function updateCropVisual() {
		updateCropCoordinates();
		var selection = element("tracker-mp4-selection");
		var layer = element("tracker-mp4-selection-layer");
		if (!selection || !layer) return;
		layer.classList.toggle("tracker-mp4-selection-active",
			state.cropEnabled && !!state.stream && !state.capturing && !state.countingDown && !state.importing);
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
		var countdown = element("tracker-mp4-countdown");
		if (countdown) countdown.hidden = true;
	}

	function stopTracks() {
		cancelScreenCountdown();
		if (state.stream) {
			state.stream.getTracks().forEach(function (track) { track.stop(); });
			state.stream = null;
		}
		state.sourceKind = null;
		var preview = element("tracker-mp4-preview");
		if (preview) preview.srcObject = null;
	}

	async function attachStream(stream, sourceKind) {
		state.stream = stream;
		state.sourceKind = sourceKind;
		var preview = element("tracker-mp4-preview");
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

		var cropToggle = element("tracker-mp4-crop-toggle");
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
					if (state.format === "mp4" || state.frames.length) {
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
			throw new CaptureError("Camera access is not supported by this browser.");
		}
		if (!global.isSecureContext) {
			throw new CaptureError("Camera access requires HTTPS or localhost.");
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
			throw new CaptureError("Screen capture is not supported by this browser.");
		}
		if (!global.isSecureContext) {
			throw new CaptureError("Screen capture requires HTTPS or localhost.");
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
				else reject(new CaptureError("The browser could not encode a video frame."));
			}, "image/jpeg", JPEG_QUALITY);
		});
	}

	async function captureFrame() {
		if (!state.capturing || state.capturePending || state.frames.length >= MAX_FRAMES) return;
		var preview = element("tracker-mp4-preview");
		if (!preview || !preview.videoWidth || !preview.videoHeight) return;

		state.capturePending = true;
		var session = state.session;
		var reachedLimit = false;
		try {
			var canvas = element("tracker-mp4-canvas");
			var bounds = getCropBounds(state.cropEnabled ? state.crop : null);
			var sourceX = bounds.xmin;
			var sourceY = bounds.ymin;
			var sourceWidth = bounds.xmax - bounds.xmin;
			var sourceHeight = bounds.ymax - bounds.ymin;
			if (canvas.width !== sourceWidth) canvas.width = sourceWidth;
			if (canvas.height !== sourceHeight) canvas.height = sourceHeight;
			canvas.getContext("2d", { alpha: false }).drawImage(
				preview,
				sourceX, sourceY, sourceWidth, sourceHeight,
				0, 0, sourceWidth, sourceHeight
			);
			if (state.format === "mp4") {
				setStatus("Recording MP4: " + ((performance.now() - state.captureStartedAt) / 1000).toFixed(1) + " seconds");
				return;
			}
			var frame = await canvasToBlob(canvas);
			if (session !== state.session) return;
			state.frames.push(frame);
			setStatus("Recording: " + state.frames.length + " frames captured");
			reachedLimit = state.frames.length >= MAX_FRAMES;
		} catch (error) {
			state.capturing = false;
			global.clearInterval(state.captureTimer);
			state.captureTimer = null;
			if (state.recorder && state.recorder.state !== "inactive") state.recorder.stop();
			releaseRecorderStream();
			reportError(error);
			setControls();
		} finally {
			if (session === state.session) state.capturePending = false;
		}
		if (reachedLimit) stopAndImport();
	}

	function mp4MimeType() {
		if (!global.MediaRecorder) return null;
		return ["video/mp4;codecs=avc1.42E01E", "video/mp4;codecs=avc1", "video/mp4"].find(function (type) {
			return global.MediaRecorder.isTypeSupported(type);
		}) || null;
	}

	function releaseRecorderStream() {
		if (state.recorderStream) state.recorderStream.getTracks().forEach(function (track) { track.stop(); });
		state.recorderStream = null;
	}

	function beginCapture() {
		if (!state.stream || state.capturing || state.countingDown || state.importing) return;
		var fps = Number(element("tracker-mp4-fps").value) || DEFAULT_FPS;
		state.format = element("tracker-format-mp4").checked ? "mp4" : "images";
		state.frames = [];
		state.captureStartedAt = performance.now();
		state.captureStoppedAt = 0;
		try {
			if (state.format === "mp4") {
				var preview = element("tracker-mp4-preview");
				if (!preview.videoWidth || !preview.videoHeight) throw new CaptureError("Wait for the video preview before recording.");
				var mimeType = mp4MimeType();
				var canvas = element("tracker-mp4-canvas");
				if (!mimeType || !canvas.captureStream) throw new CaptureError("MP4 recording is unavailable in this browser. Select Images instead.");
				// Draw the first frame before capturing the canvas, including the selected crop.
				state.capturing = true;
				captureFrame();
				if (!state.capturing) return;
				state.recorderStream = canvas.captureStream(fps);
				var recorder = state.recorder = new global.MediaRecorder(state.recorderStream, { mimeType: mimeType });
				var chunks = [];
				var recordingError = null;
				state.recordingResult = new Promise(function (resolve) {
					recorder.ondataavailable = function (event) { if (event.data.size) chunks.push(event.data); };
					recorder.onerror = function (event) { recordingError = event.error || new CaptureError("MP4 recording failed."); if (state.recorder === recorder && state.capturing) stopAndImport(); };
					recorder.onstop = function () {
						resolve({ blob: new Blob(chunks, { type: recorder.mimeType }), error: recordingError });
						if (state.recorder === recorder && state.capturing) stopAndImport();
					};
				});
				recorder.start(1000);
			} else {
				state.capturing = true;
				captureFrame();
			}
			state.captureTimer = global.setInterval(captureFrame, Math.round(1000 / fps));
			setStatus("Recording " + (state.format === "mp4" ? "MP4" : "images") + "...");
		} catch (error) {
			state.capturing = false;
			state.recorder = null;
			releaseRecorderStream();
			reportError(error);
		}
		setControls();
	}

	function startScreenCountdown() {
		cancelScreenCountdown();
		state.countingDown = true;
		state.countdownRemaining = SCREEN_COUNTDOWN_SECONDS;
		var countdown = element("tracker-mp4-countdown");
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

	function toggleCapture() {
		if (state.importing) return;
		if (state.countingDown) {
			cancelScreenCountdown();
			setStatus("Countdown cancelled. Press Record to start again.");
			setControls();
			return;
		}
		if (state.capturing) return stopAndImport();
		startCapture();
	}

	function startCapture() {
		if (!state.stream || state.capturing || state.countingDown || state.importing) return;
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
		state.importing = true;
		var session = state.session;
		state.captureStoppedAt = performance.now();
		global.clearInterval(state.captureTimer);
		state.captureTimer = null;
		setControls();
		setStatus("Preparing capture for Tracker...");

		try {
			await waitForPendingCapture();
			if (session !== state.session) return;
			if (state.format === "mp4") {
				if (state.recorder.state !== "inactive") state.recorder.stop();
				var result = await state.recordingResult;
				if (session !== state.session) return;
				if (result.error) throw result.error;
				if (!result.blob.size) throw new CaptureError("No MP4 video was captured.");
				var bytes = J2S._toBytes(await result.blob.arrayBuffer());
				if (session !== state.session) return;
				trackerApp.importMP4Capture(Date.now().toString(36), bytes);
				setStatus("MP4 sent to Tracker for import.");
			} else {
				if (!state.frames.length) throw new CaptureError("No video frames were captured.");
				var requestedFps = Number(element("tracker-mp4-fps").value) || DEFAULT_FPS;
				var elapsedSeconds = (state.captureStoppedAt - state.captureStartedAt) / 1000;
				var fps = state.frames.length > 1 && elapsedSeconds > 0
					? (state.frames.length - 1) / elapsedSeconds
					: requestedFps;
				var data = await getImageStack(state.frames, fps);
				if (session !== state.session) return;
				trackerApp.importVideoCapture(Date.now().toString(36), data, fps);
				setStatus("Sent " + state.frames.length + " captured frames to Tracker for import.");
			}
			stopTracks();
			global.setTimeout(function () { if (session === state.session) closeDialog(); }, 700);
		} catch (error) {
			if (session !== state.session) return;
			state.importing = false;
			reportError(error);
			setControls();
		} finally {
			if (session === state.session) releaseRecorderStream();
		}
	}

	function closeDialog() {
		state.session++;
		state.importing = false;
		if (state.recorder && state.recorder.state !== "inactive") state.recorder.stop();
		state.recorder = null;
		state.recordingResult = null;
		releaseRecorderStream();
		dialogDrag = null;
		if (state.capturing) {
			state.capturing = false;
			global.clearInterval(state.captureTimer);
		}
		state.captureTimer = null;
		state.capturePending = false;
		state.frames = [];
		stopTracks();
		var dialog = element("tracker-mp4-dialog");
		if (dialog) dialog.hidden = true;
		setControls();
	}

	var trackerApp;
	
	var dialogOffset = { x: 0, y: 0 };
	var dialogDrag = null;

	function positionCameraDialog(allowPartial) {
		var dialog = element("tracker-mp4-dialog");
		if (!dialog || dialog.hidden) return;
		var viewport = global.visualViewport;
		var width = Math.min(global.innerWidth, viewport ? viewport.width : global.innerWidth);
		var height = Math.min(global.innerHeight, viewport ? viewport.height : global.innerHeight);
		var scale = viewport ? viewport.scale : 1;
		var compact = width > height && height * scale <= 500;
		dialog.classList.toggle("tracker-mp4-landscape", compact);
		dialog.style.left = (viewport ? viewport.offsetLeft : 0) + "px";
		dialog.style.top = (viewport ? viewport.offsetTop : 0) + "px";
		dialog.style.width = width + "px";
		dialog.style.height = height + "px";
		var content = element("tracker-mp4-content");
		var padding = global.getComputedStyle(dialog);
		var availableHeight = Math.max(0, height - parseFloat(padding.paddingTop) - parseFloat(padding.paddingBottom));
		// Explicit pixel limits also work when Safari changes its browser bars or keyboard.
		content.style.maxHeight = availableHeight + "px";
		content.style.maxWidth = Math.max(0, width - parseFloat(padding.paddingLeft) - parseFloat(padding.paddingRight)) + "px";
		var contentStyle = global.getComputedStyle(content);
		var title = element("tracker-mp4-header");
		var titleStyle = global.getComputedStyle(title);
		element("tracker-mp4-body").style.maxHeight = Math.max(0, availableHeight
			- parseFloat(contentStyle.paddingTop) - parseFloat(contentStyle.paddingBottom)
			- title.offsetHeight - parseFloat(titleStyle.marginBottom)) + "px";
		element("tracker-mp4-preview").style.maxHeight = Math.floor(height * (compact ? 0.5 : 0.62)) + "px";
		content.style.transform = "translate(" + dialogOffset.x + "px," + dialogOffset.y + "px)";
		var bounds = dialog.getBoundingClientRect();
		var rect = content.getBoundingClientRect();
		var left = bounds.left + parseFloat(padding.paddingLeft);
		var right = bounds.right - parseFloat(padding.paddingRight);
		var top = bounds.top + parseFloat(padding.paddingTop);
		var bottom = bounds.bottom - parseFloat(padding.paddingBottom);
		if (allowPartial === true) {
			// A full-screen-sized dialog must still move under the finger.
			// Keep enough title visible to drag it back; resize/open fit the whole dialog.
			var gripWidth = Math.min(120, rect.width);
			dialogOffset.x += Math.max(left + gripWidth - rect.right, Math.min(0, right - gripWidth - rect.left));
			dialogOffset.y += Math.max(top - rect.top, Math.min(0, bottom - rect.top
				- title.offsetHeight - parseFloat(contentStyle.paddingTop)));
		} else {
			dialogOffset.x += Math.max(left - rect.left, Math.min(0, right - rect.right));
			dialogOffset.y += Math.max(top - rect.top, Math.min(0, bottom - rect.bottom));
		}
		content.style.transform = "translate(" + dialogOffset.x + "px," + dialogOffset.y + "px)";
		updateCropVisual();
	}

	function enableDialogDragging() {
		var handle = element("tracker-mp4-header");
		function consume(event) {
			if (event.cancelable) event.preventDefault();
			event.stopPropagation();
		}
		function start(event, point, kind, id) {
			if (event.target.closest("#tracker-mp4-source")) return;
			consume(event);
			dialogDrag = { kind: kind, id: id, x: point.clientX, y: point.clientY,
				left: dialogOffset.x, top: dialogOffset.y };
		}
		function move(event, point) {
			consume(event);
			dialogOffset.x = dialogDrag.left + point.clientX - dialogDrag.x;
			dialogOffset.y = dialogDrag.top + point.clientY - dialogDrag.y;
			positionCameraDialog(true);
		}
		// Use native Touch Events on iOS. Do not depend on Safari maintaining
		// pointer capture while the touched title is itself being moved.
		handle.addEventListener("touchstart", function (event) {
			if (event.touches.length !== 1) { dialogDrag = null; return; }
			var touch = event.touches[0];
			start(event, touch, "touch", touch.identifier);
		}, { passive: false });
		handle.addEventListener("touchmove", function (event) {
			if (!dialogDrag || dialogDrag.kind !== "touch") return;
			for (var i = 0; i < event.touches.length; i++) {
				if (event.touches[i].identifier === dialogDrag.id) {
					move(event, event.touches[i]);
					break;
				}
			}
		}, { passive: false });
		["touchend", "touchcancel"].forEach(function (name) {
			handle.addEventListener(name, function (event) {
				if (!dialogDrag || dialogDrag.kind !== "touch") return;
				consume(event);
				dialogDrag = null;
			}, { passive: false });
		});
		handle.addEventListener("pointerdown", function (event) {
			if (event.pointerType === "touch" && "ontouchstart" in global) return;
			if (event.isPrimary === false || event.button > 0) return;
			start(event, event, "pointer", event.pointerId);
		});
		// Listen outside the title so mouse/pen drags continue after leaving it.
		global.addEventListener("pointermove", function (event) {
			if (!dialogDrag || dialogDrag.kind !== "pointer" || dialogDrag.id !== event.pointerId) return;
			move(event, event);
		}, true);
		["pointerup", "pointercancel"].forEach(function (name) {
			global.addEventListener(name, function (event) {
				if (!dialogDrag || dialogDrag.kind !== "pointer" || dialogDrag.id !== event.pointerId) return;
				consume(event);
				dialogDrag = null;
			}, true);
		});
		handle.addEventListener("keydown", function (event) {
			if (event.target.closest("#tracker-mp4-source")) return;
			var directions = { ArrowLeft: [-10, 0], ArrowRight: [10, 0],
				ArrowUp: [0, -10], ArrowDown: [0, 10] };
			var delta = directions[event.key];
			if (!delta) return;
			event.preventDefault();
			event.stopPropagation();
			dialogOffset.x += delta[0];
			dialogOffset.y += delta[1];
			positionCameraDialog(true);
		});
	}

	function showDialog(app) {
		trackerApp = app;
		var dialog = element("tracker-mp4-dialog") || createDialog();
		dialog.hidden = false;
		element("tracker-mp4-body").scrollTop = 0;
		dialogOffset = { x: 0, y: 0 };
		dialogDrag = null;
		positionCameraDialog();
		dialog.focus();
		setStatus("");
		setControls();
	}

	function createDialog() {
		var showScreenCapture = !isIOSOrAndroid();
		var screenHelp = '<p id="tracker-mp4-screen-capture-help" class="tracker-mp4-help">' +
			'Select <strong>Camera</strong>, press Start to open the camera, then press Record. ' +
			(showScreenCapture ? 'Select <strong>Screen</strong>, press Start, select a browser window from the list, ' +
				'and press record for a four-second count-down. ' : '') +
			'Choose frames/sec and Images or MP4, and optionally select a region before recording. MP4 requires browser recording support.</p>';
		var screenButton = showScreenCapture
			? '<button id="tracker-mp4-screen-open" type="button">Start</button>'
			: "";
		var dialog = document.createElement("div");
		dialog.id = "tracker-mp4-dialog";
		dialog.hidden = true;
		dialog.tabIndex = -1;
		dialog.setAttribute("role", "dialog");
		dialog.setAttribute("aria-modal", "true");
		dialog.setAttribute("aria-label", "Capture video for Tracker");
		dialog.setAttribute("aria-describedby", "tracker-mp4-screen-capture-help");
		dialog.innerHTML =
			'<div id="tracker-mp4-content" class="tracker-mp4-content">' +
			'<div id="tracker-mp4-header"><h2 id="tracker-mp4-title" tabindex="0" title="Drag to move; use arrow keys when focused">Capture video</h2>' +
			'<div id="tracker-mp4-source" role="radiogroup" aria-label="Capture source">' +
			'<label><input type="radio" name="tracker-mp4-source" id="tracker-mp4-source-camera" value="camera" checked> Camera</label>' +
			'<label><input type="radio" name="tracker-mp4-source" id="tracker-mp4-source-screen" value="screen"> Screen</label></div><span id="tracker-mp4-drag-hint">Drag to move</span></div>' +
			'<div id="tracker-mp4-body" class="tracker-mp4-body">' +
			screenHelp +
			'<div class="tracker-mp4-workspace"><div id="tracker-mp4-preview-wrap">' +
			'<video id="tracker-mp4-preview" playsinline muted></video>' +
			'<div id="tracker-mp4-selection-layer" aria-label="Video capture region">' +
			'<div id="tracker-mp4-selection" hidden></div></div>' +
			'<div id="tracker-mp4-countdown" aria-hidden="true" hidden></div></div>' +
			'<canvas id="tracker-mp4-canvas" hidden></canvas><div class="tracker-mp4-settings">' +
			'<div class="tracker-mp4-controls">' +
			'<button id="tracker-mp4-open" type="button">Start</button>' +
			screenButton +
			'<button id="tracker-mp4-record" type="button">Record</button>' +
			'<button id="tracker-mp4-cancel" type="button">Cancel</button>' +
			'<label title="This is the target rate, not a guarantee—browser performance and source updates can reduce the actual rate. MP4 retains its encoded timing; image stacks use an estimated rate based on captured frames and elapsed time.">Frames/sec <select id="tracker-mp4-fps" title="This is the target rate, not a guarantee—browser performance and source updates can reduce the actual rate. MP4 retains its encoded timing; image stacks use an estimated rate based on captured frames and elapsed time."><option>5</option><option selected>10</option><option>15</option><option>30</option></select></label>' +
			'</div><div class="tracker-mp4-controls">' +
			'<span role="radiogroup" aria-label="Video format"><label><input type="radio" name="tracker-capture-format" id="tracker-format-images" value="images" checked> Images</label> <label><input type="radio" name="tracker-capture-format" id="tracker-format-mp4" value="mp4"> MP4</label></span>' +
			'<label><input id="tracker-mp4-crop-toggle" type="checkbox" disabled> Capture region</label>' +
			'</div><div class="tracker-mp4-coordinates" aria-label="Capture coordinates">' +
			'<div class="tracker-mp4-controls tracker-mp4-coordinates-heading">' +
			'<span>Video pixels (origin: top left)</span>' +
			'<button id="tracker-mp4-crop-clear" type="button" disabled>Clear region</button></div>' +
			'<label>X min <input id="tracker-mp4-xmin" type="number" step="1" disabled></label>' +
			'<label>X max <input id="tracker-mp4-xmax" type="number" step="1" disabled></label>' +
			'<label>Y min <input id="tracker-mp4-ymin" type="number" step="1" disabled></label>' +
			'<label>Y max <input id="tracker-mp4-ymax" type="number" step="1" disabled></label>' +
			'</div><p id="tracker-mp4-status" role="status" aria-live="polite"></p></div></div></div></div>';
		element("tracker-mp4-host").appendChild(dialog);

		["camera", "screen"].forEach(function (source) {
			element("tracker-mp4-source-" + source).addEventListener("change", function (event) {
				if (!event.currentTarget.checked || event.currentTarget.disabled) return;
				stopTracks();
				state.selectedSource = source;
				state.cropEnabled = source === "screen";
				element("tracker-mp4-crop-toggle").checked = state.cropEnabled;
				clearCropSelection();
				setStatus(source === "screen" ? "Press Start to choose a window or screen." : "Press Start to open the camera.");
				setControls();
			});
		});
		element("tracker-mp4-open").addEventListener("click", function () {
			openCamera("environment").catch(function (error) {
				reportError(error);
				setControls();
			});
		});
		var screenOpenButton = element("tracker-mp4-screen-open");
		if (screenOpenButton) {
			screenOpenButton.addEventListener("click", function () {
				openScreen().catch(function (error) {
					reportError(error);
					setControls();
				});
			});
		}
		element("tracker-mp4-record").addEventListener("click", toggleCapture);
		element("tracker-mp4-cancel").addEventListener("click", closeDialog);
		element("tracker-mp4-crop-toggle").addEventListener("change", function (event) {
			state.cropEnabled = event.currentTarget.checked;
			updateCropVisual();
			setStatus(state.cropEnabled
				? "Drag over the video preview to select the capture region."
				: "The full video source will be captured.");
		});
		element("tracker-mp4-crop-clear").addEventListener("click", function () {
			clearCropSelection();
			setStatus("Drag over the video preview to select a new capture region.");
			setControls();
		});
		["xmin", "xmax", "ymin", "ymax"].forEach(function (key) {
			var input = element("tracker-mp4-" + key);
			input.addEventListener("change", editCropCoordinate);
			input.addEventListener("keydown", function (event) {
				if (event.key === "Enter") editCropCoordinate(event);
			});
		});
		var selectionLayer = element("tracker-mp4-selection-layer");
		selectionLayer.addEventListener("pointerdown", beginCrop);
		selectionLayer.addEventListener("pointermove", moveCrop);
		selectionLayer.addEventListener("pointerup", finishCrop);
		selectionLayer.addEventListener("pointercancel", cancelCrop);
		element("tracker-mp4-preview").addEventListener("resize", positionCameraDialog);
		dialog.addEventListener("keydown", function (event) {
			if (event.key === "Escape") closeDialog();
		});
		enableDialogDragging();
		if (global.ResizeObserver) {
			new global.ResizeObserver(positionCameraDialog).observe(element("tracker-mp4-content"));
		}
		return dialog;
	}

	function addStyles() {
		var style = document.createElement("style");
		style.textContent =
			"#tracker-mp4-dialog{position:fixed;z-index:1000000;inset:0;box-sizing:border-box;overflow:hidden;display:flex;align-items:center;justify-content:center;padding:16px;padding-left:max(16px,env(safe-area-inset-left));padding-right:max(16px,env(safe-area-inset-right));background:#0009}" +
			"#tracker-mp4-dialog[hidden]{display:none}" +
			".tracker-mp4-content{box-sizing:border-box;width:720px;max-width:100%;max-height:100%;display:flex;flex-direction:column;overflow:hidden;padding:16px;border-radius:10px;background:#fff;box-shadow:0 12px 45px #0008;font:15px/1.35 sans-serif;color:#222}" +
			"#tracker-mp4-header{display:flex;flex-wrap:wrap;align-items:center;justify-content:flex-start;gap:8px 16px;flex:none;margin-bottom:8px;cursor:move;touch-action:none}" +
			"#tracker-mp4-source{display:flex;gap:12px;align-items:center;font:normal 14px/1.4 sans-serif;white-space:nowrap}" +
			"#tracker-mp4-source label{display:inline-flex;align-items:center;gap:4px;cursor:pointer}" +
			"#tracker-mp4-title{flex:none;display:flex;align-items:center;justify-content:space-between;gap:12px;margin:0;padding:4px 0;min-height:24px;font:bold 16px/1.4 sans-serif;cursor:move;touch-action:none;user-select:none;-webkit-user-select:none}" +
			"#tracker-mp4-drag-hint{margin-left:auto;font:normal 12px/1.4 sans-serif;color:#555;white-space:nowrap;user-select:none;-webkit-user-select:none}" +
			".tracker-mp4-body{min-height:0;overflow:auto;overscroll-behavior:contain}" +
			".tracker-mp4-help{margin:0 0 10px}" +
			"#tracker-mp4-preview-wrap{position:relative;overflow:hidden;border-radius:6px;background:#111}" +
			"#tracker-mp4-preview{display:block;width:100%;max-height:62vh;background:#111;border-radius:6px;object-fit:contain}" +
			"#tracker-mp4-selection-layer{position:absolute;inset:0;pointer-events:none}" +
			"#tracker-mp4-selection-layer.tracker-mp4-selection-active{pointer-events:auto;cursor:crosshair;touch-action:none}" +
			"#tracker-mp4-selection{position:absolute;box-sizing:border-box;border:2px solid #ffe600;background:#ffe60022;box-shadow:0 0 0 9999px #0007}" +
			"#tracker-mp4-countdown{position:absolute;z-index:2;left:50%;top:50%;transform:translate(-50%,-50%);min-width:1.4em;text-align:center;color:#fff;font:bold clamp(64px,18vw,150px)/1 sans-serif;text-shadow:0 3px 12px #000;background:#0008;border-radius:14px;padding:.08em .18em}" +
			".tracker-mp4-controls{display:flex;flex-wrap:wrap;gap:8px;align-items:center;margin-top:12px}" +
			".tracker-mp4-coordinates{display:flex;flex-wrap:wrap;gap:8px;align-items:center;margin-top:12px}" +
			".tracker-mp4-coordinates>.tracker-mp4-coordinates-heading{flex-basis:100%;margin-top:0}" +
			".tracker-mp4-coordinates input{width:5em;font:inherit;padding:4px}" +
			".tracker-mp4-controls button,.tracker-mp4-controls select{font:inherit;padding:7px 10px}" +
			"#tracker-mp4-status{min-height:1.4em;margin:10px 0 0}" +
			".tracker-mp4-error{color:#a40000;font-weight:600}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape{padding-top:8px;padding-bottom:8px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-content{width:900px;padding:10px;font-size:13px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape #tracker-mp4-title{font-size:14px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-help{font-size:12px;margin-bottom:6px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-workspace{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1fr);gap:12px;align-items:start}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape #tracker-mp4-preview{max-height:55vh}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-controls,#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-coordinates{gap:4px;margin-top:6px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-settings>.tracker-mp4-controls:first-child{margin-top:0}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-controls button,#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-controls select{padding:4px 6px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-coordinates input{width:4em;padding:2px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape #tracker-mp4-status{margin-top:4px}" +
			"#tracker-mp4-dialog.tracker-mp4-landscape .tracker-mp4-settings{min-width:0}";
		document.head.appendChild(style);
	}

	function mount() {
		if (element("tracker-mp4-host")) return;
		addStyles();
		var host = document.createElement("div");
		host.id = "tracker-mp4-host";
		global.addEventListener("resize", positionCameraDialog);
		global.addEventListener("orientationchange", function () {
			dialogOffset = { x: 0, y: 0 };
			dialogDrag = null;
			global.requestAnimationFrame(positionCameraDialog);
		});
		if (global.visualViewport) {
			global.visualViewport.addEventListener("resize", positionCameraDialog);
			global.visualViewport.addEventListener("scroll", positionCameraDialog);
		}
		document.body.appendChild(host);
	}
	J2S.TrackerMP4Importer = {
		createDialog: function(app) {
			showDialog(app) 
		}
	};
	mount();
})(window, J2S);
