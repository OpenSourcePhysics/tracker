(function (global) {
	"use strict";

	var MAX_FRAMES = 1800;
	var DEFAULT_FPS = 10;
	var JPEG_QUALITY = 0.92;
	var state = {
		stream: null,
		capturing: false,
		captureTimer: null,
		capturePending: false,
		captureStartedAt: 0,
		captureStoppedAt: 0,
		frames: [],
		trackerReady: false
	};

	function element(id) {
		return document.getElementById(id);
	}

	function setStatus(message, isError) {
		var status = element("tracker-camera-status");
		if (!status) return;
		status.textContent = message;
		status.classList.toggle("tracker-camera-error", !!isError);
	}

	function setControls() {
		var hasStream = !!state.stream;
		var openButton = element("tracker-camera-open");
		var recordButton = element("tracker-camera-record");
		var stopButton = element("tracker-camera-stop");
		var switchButton = element("tracker-camera-switch");
		if (openButton) openButton.disabled = state.capturing;
		if (recordButton) recordButton.disabled = !hasStream || state.capturing;
		if (stopButton) stopButton.disabled = !state.capturing;
		if (switchButton) switchButton.disabled = !hasStream || state.capturing;
	}

	function stopTracks() {
		if (state.stream) {
			state.stream.getTracks().forEach(function (track) { track.stop(); });
			state.stream = null;
		}
		var preview = element("tracker-camera-preview");
		if (preview) preview.srcObject = null;
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
		state.stream = await navigator.mediaDevices.getUserMedia({
			audio: false,
			video: {
				facingMode: { ideal: facingMode || "environment" },
				width: { ideal: 1920 },
				height: { ideal: 1080 }
			}
		});
		var preview = element("tracker-camera-preview");
		preview.srcObject = state.stream;
		await preview.play();
		setStatus("Camera ready. Keep the phone steady, then start recording.");
		setControls();
	}

	function canvasToBlob(canvas) {
		return new Promise(function (resolve, reject) {
			canvas.toBlob(function (blob) {
				if (blob) resolve(blob);
				else reject(new Error("The browser could not encode a camera frame."));
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
			canvas.width = preview.videoWidth;
			canvas.height = preview.videoHeight;
			canvas.getContext("2d", { alpha: false }).drawImage(preview, 0, 0);
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

	function startCapture() {
		if (!state.stream || state.capturing) return;
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
		return "camera-" + sessionId + "-" + String(index).padStart(4, "0") + ".jpg";
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
			if (!state.frames.length) throw new Error("No camera frames were captured.");
			var requestedFps = Number(element("tracker-camera-fps").value) || DEFAULT_FPS;
			var elapsedSeconds = (state.captureStoppedAt - state.captureStartedAt) / 1000;
			var fps = state.frames.length > 1 && elapsedSeconds > 0
				? (state.frames.length - 1) / elapsedSeconds
				: requestedFps;
			var stack = await cacheImageStack(state.frames, fps);
			Tracker.app.importVideo(stack.firstPath, stack.fps);
			setStatus("Imported " + state.frames.length + " camera frames into Tracker.");
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
		setStatus("Select Start camera to grant access and preview the video.");
		setControls();
	}

	function createDialog() {
		var dialog = document.createElement("div");
		dialog.id = "tracker-camera-dialog";
		dialog.hidden = true;
		dialog.tabIndex = -1;
		dialog.setAttribute("role", "dialog");
		dialog.setAttribute("aria-modal", "true");
		dialog.setAttribute("aria-label", "Import video from camera");
		dialog.innerHTML =
			'<div class="tracker-camera-content">' +
			'<video id="tracker-camera-preview" playsinline muted></video>' +
			'<canvas id="tracker-camera-canvas" hidden></canvas>' +
			'<div class="tracker-camera-controls">' +
			'<button id="tracker-camera-open" type="button">Start camera</button>' +
			'<button id="tracker-camera-switch" type="button">Switch camera</button>' +
			'<label>Frames/sec <select id="tracker-camera-fps"><option>5</option><option selected>10</option><option>15</option><option>30</option></select></label>' +
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
		element("tracker-camera-switch").addEventListener("click", function () {
			var current = state.stream && state.stream.getVideoTracks()[0];
			var facing = current && current.getSettings().facingMode === "user" ? "environment" : "user";
			openCamera(facing).catch(function (error) {
				setStatus(error.message || String(error), true);
				setControls();
			});
		});
		element("tracker-camera-record").addEventListener("click", startCapture);
		element("tracker-camera-stop").addEventListener("click", stopAndImport);
		element("tracker-camera-cancel").addEventListener("click", closeDialog);
		dialog.addEventListener("keydown", function (event) {
			if (event.key === "Escape") closeDialog();
		});
		return dialog;
	}

	function addStyles() {
		var style = document.createElement("style");
		style.textContent =
			"#tracker-camera-host{position:fixed;z-index:1000001;top:12px;right:12px}" +
			"#tracker-camera-launch{padding:9px 16px;font:600 15px sans-serif;cursor:pointer}" +
			"#tracker-camera-dialog{position:fixed;z-index:1000000;inset:0;display:flex;align-items:center;justify-content:center;padding:16px;background:#0009}" +
			"#tracker-camera-dialog[hidden]{display:none}" +
			".tracker-camera-content{box-sizing:border-box;width:min(92vw,720px);padding:16px;border-radius:10px;background:#fff;box-shadow:0 12px 45px #0008;font:15px/1.35 sans-serif;color:#222}" +
			"#tracker-camera-preview{display:block;width:100%;max-height:62vh;background:#111;border-radius:6px;object-fit:contain}" +
			".tracker-camera-controls{display:flex;flex-wrap:wrap;gap:8px;align-items:center;margin-top:12px}" +
			".tracker-camera-controls button,.tracker-camera-controls select{font:inherit;padding:7px 10px}" +
			"#tracker-camera-status{min-height:1.4em;margin:10px 0 0}" +
			".tracker-camera-error{color:#a40000;font-weight:600}";
		document.head.appendChild(style);
	}

	function mount() {
		if (element("tracker-camera-launch")) return;
		addStyles();
		var host = document.createElement("div");
		host.id = "tracker-camera-host";
		var launch = document.createElement("button");
		launch.id = "tracker-camera-launch";
		launch.type = "button";
		launch.textContent = "Import from camera";
		launch.disabled = !state.trackerReady;
		launch.addEventListener("click", showDialog);
		host.appendChild(launch);

		var anchor = document.querySelector("body > p[align='center']") || document.body.firstChild;
		if (anchor && anchor.parentNode) anchor.parentNode.insertBefore(host, anchor.nextSibling);
		else document.body.appendChild(host);

	}

	global.TrackerCameraImporter = {
		mount: mount,
		setTrackerReady: function () {
			state.trackerReady = true;
			var launch = element("tracker-camera-launch");
			if (launch) launch.disabled = false;
		}
	};

	if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", mount);
	else mount();
})(window);
