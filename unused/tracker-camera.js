(function (global) {
	"use strict";

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
		cropPointerId: null,
		trackerReady: false
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

	function updateCropVisual() {
		var selection = element("tracker-camera-selection");
		var layer = element("tracker-camera-selection-layer");
		if (!selection || !layer) return;
		layer.classList.toggle("tracker-camera-selection-active",
			state.cropEnabled && !!state.stream && !state.capturing && !state.countingDown);
		var crop = state.cropDraft || state.crop;
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
			state.crop = crop;
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
			var crop = state.cropEnabled && state.crop ? state.crop : { x: 0, y: 0, width: 1, height: 1 };
			var sourceX = Math.round(crop.x * preview.videoWidth);
			var sourceY = Math.round(crop.y * preview.videoHeight);
			var sourceWidth = Math.max(1, Math.round(crop.width * preview.videoWidth));
			var sourceHeight = Math.max(1, Math.round(crop.height * preview.videoHeight));
			sourceWidth = Math.min(sourceWidth, preview.videoWidth - sourceX);
			sourceHeight = Math.min(sourceHeight, preview.videoHeight - sourceY);
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

	function frameName(sessionId, index) {
		return "capture-" + sessionId + "-" + String(index).padStart(4, "0") + ".jpg";
	}

	async function cacheImageStack(frames, fps) {
		if (!global.J2S || !global.Clazz || !global.swingjs || !global.Tracker || !Tracker.app) {
			throw new Error("Tracker Online is still starting. Please try again in a moment.");
		}
		if (typeof Tracker.app.importVideo !== "function") {
			throw new Error("This Tracker Online build does not include camera import support.");
		}

		Clazz.loadClass("swingjs.JSUtil");
		var JavaFile = Clazz.load("java.io.File");
		var directory = J2S.getGlobal("j2s.tmpdir") + "OPEN/";
		var sessionId = Date.now().toString(36);
		var firstPath = null;

		for (var i = 0; i < frames.length; i++) {
			var path = directory + frameName(sessionId, i);
			var file = Clazz.new_(JavaFile.c$$S, [path]);
			var data = await frames[i].arrayBuffer();
			swingjs.JSUtil.setFileBytesStatic$O$O(file, J2S._toBytes(data));
			if (firstPath === null) firstPath = path;
		}
		return { firstPath: firstPath, fps: fps };
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
			var stack = await cacheImageStack(state.frames, fps);
			Tracker.app.importVideo(stack.firstPath, stack.fps);
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

	function showDialog() {
		var dialog = element("tracker-camera-dialog") || createDialog();
		dialog.hidden = false;
		dialog.focus();
		setStatus(isIOSOrAndroid()
			? "Start the camera to preview the video."
			: "Start a camera, or select Capture screen and choose a window.");
		setControls();
	}

	function createDialog() {
		var showScreenCapture = !isIOSOrAndroid();
		var screenHelp = showScreenCapture
			? '<p id="tracker-screen-capture-help" class="tracker-camera-help"><strong>Screen capture:</strong> ' +
				'choose a window in the browser list, select a region, then press Record for a four-second countdown.</p>'
			: "";
		var screenButton = showScreenCapture
			? '<button id="tracker-screen-open" type="button">Capture screen</button>'
			: "";
		var dialog = document.createElement("div");
		dialog.id = "tracker-camera-dialog";
		dialog.hidden = true;
		dialog.tabIndex = -1;
		dialog.setAttribute("role", "dialog");
		dialog.setAttribute("aria-modal", "true");
		dialog.setAttribute("aria-label", "Capture video for Tracker");
		if (showScreenCapture) dialog.setAttribute("aria-describedby", "tracker-screen-capture-help");
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
			'<label><input id="tracker-camera-crop-toggle" type="checkbox" disabled> Capture selected region only</label>' +
			'<button id="tracker-camera-crop-clear" type="button" disabled>Clear region</button>' +
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

	global.TrackerCameraImporter = {
		mount: mount,
		createDialog: showDialog,
		setTrackerReady: function () {
			state.trackerReady = true;
		}
	};

	if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", mount);
	else mount();
})(window);
