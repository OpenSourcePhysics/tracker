package org.opensourcephysics.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.opensourcephysics.media.core.VideoRecorder;
import org.opensourcephysics.tools.JarTool;

/** Browser download handling for completed ZIP and TRZ archives. */
public class BrowserZipExport {

	public static boolean compress(ArrayList<File> files, File target) {
		Object cleanup = install(target.getPath());
		try {
			return JarTool.compress(files, target, null);
		} finally {
			restore(cleanup);
		}
	}

	public static String saveVideo(VideoRecorder recorder) throws IOException {
		Object cleanup = install(recorder.getFileName());
		try {
			return recorder.saveVideo();
		} finally {
			restore(cleanup);
		}
	}

	/**
	 * The archive is ready only after compression. On iOS, use a Blob link and
	 * a fresh user tap instead of the default asynchronous base64 download.
	 * Temporary writes still go to the SwingJS cache; other saves are delegated.
	 */
	private static Object install(String targetPath) {
		/**
		 * @j2sNative
		 * if (!targetPath || !/\.(zip|trz)$/i.test(targetPath)) return null;
		 * var previousSave = J2S._localFileSaveFunction;
		 * var projectSave = null;
		 * var ua = navigator.userAgent || "";
		 * if (/iPad|iPhone|iPod/.test(ua) ||
		 *     (/Macintosh/.test(ua) && navigator.maxTouchPoints > 1)) {
		 *   projectSave = function(filename, data) {
		 *     if (filename !== targetPath) {
		 *       return previousSave ? previousSave.apply(this, arguments) : false;
		 *     }
		 *     var name = filename.substring(filename.lastIndexOf("/") + 1);
		 *     var blob = new Blob([new Uint8Array(data)], {type: "application/zip"});
		 *     var url = URL.createObjectURL(blob);
		 *     var previousFocus = document.activeElement;
		 *     var overlay = document.createElement("div");
		 *     overlay.setAttribute("role", "dialog");
		 *     overlay.setAttribute("aria-modal", "true");
		 *     overlay.setAttribute("aria-label", "Save archive");
		 *     overlay.style.cssText = "position:fixed;inset:0;z-index:1000001;display:flex;align-items:center;justify-content:center;background:#0009";
		 *     var panel = document.createElement("div");
		 *     panel.style.cssText = "background:white;color:#222;padding:24px;border-radius:10px;max-width:85vw;font:16px/1.4 sans-serif";
		 *     var message = document.createElement("p");
		 *     message.textContent = "Your archive is ready. Tap Download to save " + name + ".";
		 *     var download = document.createElement("a");
		 *     download.href = url;
		 *     download.download = name;
		 *     download.type = "application/zip";
		 *     download.textContent = "Download " + name.substring(name.lastIndexOf(".") + 1).toUpperCase();
		 *     download.style.cssText = "display:inline-block;padding:12px;margin-right:16px";
		 *     var close = document.createElement("button");
		 *     close.textContent = "Close";
		 *     close.style.cssText = "font:inherit;padding:12px";
		 *     var dismiss = function() {
		 *       overlay.remove();
		 *       // Do not revoke while Safari may still be starting the download.
		 *       window.setTimeout(function() { URL.revokeObjectURL(url); }, 60000);
		 *       if (previousFocus && previousFocus.isConnected) previousFocus.focus();
		 *     };
		 *     close.addEventListener("click", dismiss);
		 *     overlay.addEventListener("keydown", function(event) {
		 *       if (event.key === "Escape") { event.stopPropagation(); dismiss(); }
		 *       if (event.key === "Tab") {
		 *         event.preventDefault();
		 *         (document.activeElement === download ? close : download).focus();
		 *       }
		 *     });
		 *     panel.appendChild(message);
		 *     panel.appendChild(download);
		 *     panel.appendChild(close);
		 *     overlay.appendChild(panel);
		 *     document.body.appendChild(overlay);
		 *     download.focus();
		 *     return true;
		 *   };
		 *   J2S._localFileSaveFunction = projectSave;
		 * }
		 * return function() {
		 *   if (projectSave && J2S._localFileSaveFunction === projectSave)
		 *     J2S._localFileSaveFunction = previousSave;
		 * };
		 */
		return null;
	}

	private static void restore(Object cleanup) {
		/**
		 * @j2sNative
		 * if (cleanup) cleanup();
		 */
	}
}
