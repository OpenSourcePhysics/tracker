package org.opensourcephysics.media;

import java.awt.Desktop;
import java.awt.Window;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import org.opensourcephysics.cabrillo.tracker.TFrame;
import org.opensourcephysics.cabrillo.tracker.Tracker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Local web server bridge for camera capture in desktop Java Tracker.
 * Serves the camera capture HTML and tracker-mp4-import.js to the system default browser,
 * and receives the captured MP4 video or image stack via HTTP POST to import into Tracker.
 * 
 * @j2sIgnore
 */
public class TrackerCameraServer {

	private static HttpServer server;
	private static int serverPort = 0;
	private static Object currentTarget;
	private static java.util.concurrent.ExecutorService executor;

	/**
	 * Starts the local server (if not already running) and opens the camera capture page
	 * in the default browser.
	 * 
	 * @param appOrFrame the target Tracker, TFrame, or capture handler
	 */
	public static synchronized void open(Object appOrFrame) {
		currentTarget = appOrFrame;
		int port = startServer();
		if (port > 0) {
			String url = "http://127.0.0.1:" + port + "/capture";
			openInBrowser(url);
		}
	}

	/**
	 * Starts the embedded HTTP server on localhost using an ephemeral port.
	 * 
	 * @return the listening port number, or 0 on error
	 */
	public static synchronized int startServer() {
		if (server != null && serverPort > 0) {
			return serverPort;
		}
		try {
			// Bind to 127.0.0.1 on port 0 (OS picks a free ephemeral port)
			server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			server.createContext("/capture", new CapturePageHandler());
			server.createContext("/tracker-mp4-import.js", new ScriptHandler());
			server.createContext("/ES6/tracker-mp4-import.js", new ScriptHandler());
			server.createContext("/upload", new UploadHandler());
			server.createContext("/status", new StatusHandler());
			server.createContext("/", new RootHandler());
			executor = Executors.newCachedThreadPool(r -> {
				Thread t = new Thread(r, "TrackerCameraServer-Worker");
				t.setDaemon(true);
				return t;
			});
			server.setExecutor(executor);
			server.start();
			serverPort = server.getAddress().getPort();
			System.out.println("TrackerCameraServer started on http://127.0.0.1:" + serverPort);
			return serverPort;
		} catch (IOException e) {
			System.err.println("Could not start TrackerCameraServer: " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Stops the server if running.
	 */
	public static synchronized void stop() {
		if (server != null) {
			try {
				server.stop(0);
				System.out.println("TrackerCameraServer stopped.");
			} catch (Throwable ignored) {
			}
			server = null;
			serverPort = 0;
		}
		if (executor != null) {
			try {
				executor.shutdownNow();
			} catch (Throwable ignored) {
			}
			executor = null;
		}
	}

	public static int getPort() {
		return serverPort;
	}

	public static boolean isRunning() {
		return server != null && serverPort > 0;
	}

	public static void setCurrentTarget(Object target) {
		currentTarget = target;
	}

	private static void openInBrowser(String url) {
		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(new URI(url));
				return;
			}
		} catch (Throwable ignored) {
		}
		try {
			String os = System.getProperty("os.name", "").toLowerCase();
			if (os.contains("mac")) {
				Runtime.getRuntime().exec(new String[] { "open", url });
			} else if (os.contains("win")) {
				Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url });
			} else {
				Runtime.getRuntime().exec(new String[] { "xdg-open", url });
			}
		} catch (Throwable t) {
			System.err.println("Could not open browser to " + url + ": " + t.getMessage());
		}
	}

	private static class RootHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String path = exchange.getRequestURI().getPath();
			if ("/".equals(path) || "/index.html".equals(path)) {
				new CapturePageHandler().handle(exchange);
				return;
			}
			sendResponse(exchange, 404, "text/plain", "Not Found".getBytes(StandardCharsets.UTF_8));
		}
	}

	private static class CapturePageHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			byte[] content = loadResourceBytes("ES6/tracker-capture.html");
			if (content == null || content.length == 0) {
				content = getDefaultCaptureHtml().getBytes(StandardCharsets.UTF_8);
			}
			sendResponse(exchange, 200, "text/html; charset=utf-8", content);
		}
	}

	private static class ScriptHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			byte[] content = loadResourceBytes("ES6/tracker-mp4-import.js");
			if (content == null || content.length == 0) {
				sendResponse(exchange, 404, "text/plain", "Script not found".getBytes(StandardCharsets.UTF_8));
				return;
			}
			sendResponse(exchange, 200, "application/javascript; charset=utf-8", content);
		}
	}

	private static class StatusHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			byte[] json = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
			sendResponse(exchange, 200, "application/json; charset=utf-8", json);
		}
	}

	private static class UploadHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
				exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
				exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
				exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
				exchange.sendResponseHeaders(204, -1);
				exchange.close();
				return;
			}
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
				return;
			}

			Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
			String format = query.getOrDefault("format", "mp4");
			String id = query.getOrDefault("id", Long.toString(System.currentTimeMillis(), 36));

			try {
				if ("mp4".equalsIgnoreCase(format)) {
					byte[] data = readAllBytes(exchange.getRequestBody());
					if (data == null || data.length == 0) {
						sendResponse(exchange, 400, "text/plain", "Empty video data".getBytes(StandardCharsets.UTF_8));
						return;
					}
					dispatchImportMP4(id, data);
					sendResponse(exchange, 200, "text/plain; charset=utf-8", "Video imported".getBytes(StandardCharsets.UTF_8));
				} else if ("images".equalsIgnoreCase(format)) {
					double fps = Double.parseDouble(query.getOrDefault("fps", "10"));
					DataInputStream dis = new DataInputStream(exchange.getRequestBody());
					int count = dis.readInt();
					byte[][] frames = new byte[count][];
					for (int i = 0; i < count; i++) {
						int len = dis.readInt();
						frames[i] = new byte[len];
						dis.readFully(frames[i]);
					}
					dispatchImportImages(id, frames, fps);
					sendResponse(exchange, 200, "text/plain; charset=utf-8", "Image stack imported".getBytes(StandardCharsets.UTF_8));
				} else {
					sendResponse(exchange, 400, "text/plain", ("Unsupported format: " + format).getBytes(StandardCharsets.UTF_8));
				}
			} catch (Exception e) {
				e.printStackTrace();
				byte[] err = ("Import error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
				sendResponse(exchange, 500, "text/plain; charset=utf-8", err);
			}
		}
	}

	private static void dispatchImportMP4(String id, byte[] data) {
		SwingUtilities.invokeLater(() -> {
			try {
				Object target = currentTarget;
				if (target == null) {
					target = Tracker.getTracker(null);
				}
				if (target instanceof TFrame) {
					Object customTarget = ((TFrame) target).getCaptureTarget();
					if (customTarget != null) {
						target = customTarget;
					}
				}
				if (target instanceof Tracker) {
					((Tracker) target).importMP4Capture(id, data);
					bringToFront(((Tracker) target).getFrame());
				} else if (target instanceof TFrame) {
					TFrame frame = (TFrame) target;
					if (frame.getTracker() != null) {
						frame.getTracker().importMP4Capture(id, data);
					} else {
						File file = new File(System.getProperty("java.io.tmpdir"), "capture-" + id + ".mp4");
						try (FileOutputStream out = new FileOutputStream(file)) {
							out.write(data);
						}
						frame.loadVideo(file.getAbsolutePath(), false, null, null, 0, -1);
					}
					bringToFront(frame);
				} else if (target != null) {
					// Custom target (such as TrackerCameraTest)
					Method m = target.getClass().getMethod("importMP4Capture", String.class, byte[].class);
					m.setAccessible(true);
					m.invoke(target, id, data);
				}
			} catch (Exception e) {
				System.err.println("Failed to import MP4 capture: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	private static void dispatchImportImages(String id, byte[][] data, double fps) {
		SwingUtilities.invokeLater(() -> {
			try {
				Object target = currentTarget;
				if (target == null) {
					target = Tracker.getTracker(null);
				}
				if (target instanceof TFrame) {
					Object customTarget = ((TFrame) target).getCaptureTarget();
					if (customTarget != null) {
						target = customTarget;
					}
				}
				if (target instanceof Tracker) {
					((Tracker) target).importVideoCapture(id, data, fps);
					bringToFront(((Tracker) target).getFrame());
				} else if (target instanceof TFrame) {
					TFrame frame = (TFrame) target;
					if (frame.getTracker() != null) {
						frame.getTracker().importVideoCapture(id, data, fps);
					} else {
						String firstPath = null;
						String tempDir = System.getProperty("java.io.tmpdir");
						for (int i = 0; i < data.length; i++) {
							String index = "0000" + i;
							String path = tempDir + "capture-" + id + "-" + index.substring(index.length() - 4) + ".jpg";
							File file = new File(path);
							try (FileOutputStream fos = new FileOutputStream(file)) {
								fos.write(data[i]);
							}
							if (firstPath == null) firstPath = file.getAbsolutePath();
						}
						frame.loadVideo(firstPath, false, null, null, fps, data.length);
					}
					bringToFront(frame);
				} else if (target != null) {
					Method m = target.getClass().getMethod("importVideoCapture", String.class, byte[][].class, double.class);
					m.setAccessible(true);
					m.invoke(target, id, data, fps);
				}
			} catch (Exception e) {
				System.err.println("Failed to import image stack capture: " + e.getMessage());
				e.printStackTrace();
			}
		});
	}

	private static void bringToFront(Window window) {
		if (window != null) {
			window.toFront();
			window.requestFocus();
		}
	}

	private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] data) throws IOException {
		exchange.getResponseHeaders().set("Content-Type", contentType);
		exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
		exchange.sendResponseHeaders(statusCode, data.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(data);
		}
	}

	private static Map<String, String> parseQuery(String query) {
		Map<String, String> map = new HashMap<>();
		if (query == null || query.isEmpty()) return map;
		for (String param : query.split("&")) {
			int eq = param.indexOf('=');
			if (eq > 0) {
				String key = URLDecoder.decode(param.substring(0, eq), StandardCharsets.UTF_8);
				String val = URLDecoder.decode(param.substring(eq + 1), StandardCharsets.UTF_8);
				map.put(key, val);
			} else {
				map.put(URLDecoder.decode(param, StandardCharsets.UTF_8), "");
			}
		}
		return map;
	}

	private static byte[] readAllBytes(InputStream in) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buf = new byte[8192];
			int n;
			while ((n = in.read(buf)) != -1) {
				out.write(buf, 0, n);
			}
			return out.toByteArray();
		}
	}

	public static byte[] loadResourceBytes(String path) {
		InputStream in = TrackerCameraServer.class.getResourceAsStream(path.startsWith("/") ? path : "/" + path);
		if (in == null) {
			in = TrackerCameraServer.class.getClassLoader().getResourceAsStream(path.startsWith("/") ? path.substring(1) : path);
		}
		if (in == null) {
			File f = new File(path.startsWith("/") ? path.substring(1) : path);
			if (!f.exists()) {
				f = new File("resources/" + (path.startsWith("/") ? path.substring(1) : path));
			}
			if (!f.exists()) {
				f = new File("bin/" + (path.startsWith("/") ? path.substring(1) : path));
			}
			if (f.exists()) {
				try {
					in = new java.io.FileInputStream(f);
				} catch (IOException ignored) {}
			}
		}
		if (in == null) return null;
		try {
			return readAllBytes(in);
		} catch (IOException e) {
			return null;
		}
	}

	private static String getDefaultCaptureHtml() {
		return "<!DOCTYPE html>\n" +
			"<html lang=\"en\">\n" +
			"<head>\n" +
			"  <meta charset=\"utf-8\">\n" +
			"  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
			"  <title>Tracker Video Capture</title>\n" +
			"  <style>\n" +
			"    * { box-sizing: border-box; }\n" +
			"    html, body {\n" +
			"      margin: 0; padding: 0; width: 100%; height: 100%;\n" +
			"      background: #141619; color: #e5e7eb;\n" +
			"      font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;\n" +
			"      display: flex; align-items: center; justify-content: center;\n" +
			"    }\n" +
			"    .container {\n" +
			"      width: 100%; max-width: 520px; padding: 32px; text-align: center;\n" +
			"      background: #1f242d; border: 1px solid #323946; border-radius: 14px;\n" +
			"      box-shadow: 0 20px 40px rgba(0,0,0,0.5);\n" +
			"    }\n" +
			"    .status-box {\n" +
			"      background: #11141a; border: 1px solid #282f3c; border-radius: 8px;\n" +
			"      padding: 14px 16px; font-size: 14px; color: #d1d5db; margin: 20px 0;\n" +
			"    }\n" +
			"    .btn {\n" +
			"      font: inherit; font-size: 14px; font-weight: 500; padding: 10px 20px;\n" +
			"      border-radius: 8px; cursor: pointer; border: none; background: #2563eb; color: #fff;\n" +
			"    }\n" +
			"  </style>\n" +
			"</head>\n" +
			"<body>\n" +
			"  <div class=\"container\">\n" +
			"    <h1>Tracker Video Capture</h1>\n" +
			"    <div id=\"status-box\" class=\"status-box\">Connecting to camera dialog...</div>\n" +
			"    <button class=\"btn\" onclick=\"reopenDialog()\">Open Capture Dialog</button>\n" +
			"  </div>\n" +
			"  <script>\n" +
			"    window.J2S = window.J2S || { _toBytes: function(b) { return new Uint8Array(b); } };\n" +
			"    function updateStatus(msg, type) {\n" +
			"      var el = document.getElementById('status-box');\n" +
			"      if (el) el.textContent = msg;\n" +
			"    }\n" +
			"    var trackerBridge = {\n" +
			"      importMP4Capture: function(id, bytes) {\n" +
			"        updateStatus('Uploading MP4 video to Tracker...');\n" +
			"        fetch('/upload?format=mp4&id=' + encodeURIComponent(id), {\n" +
			"          method: 'POST',\n" +
			"          headers: { 'Content-Type': 'video/mp4' },\n" +
			"          body: bytes\n" +
			"        }).then(function(r) {\n" +
			"          if (r.ok) {\n" +
			"            updateStatus('Video imported! You can close this tab.');\n" +
			"            setTimeout(function() { window.close(); }, 1200);\n" +
			"          } else updateStatus('Import error: ' + r.statusText);\n" +
			"        }).catch(function(e) { updateStatus('Upload error: ' + e); });\n" +
			"      },\n" +
			"      importVideoCapture: function(id, frames, fps) {\n" +
			"        updateStatus('Uploading ' + frames.length + ' frames to Tracker...');\n" +
			"        var total = 4;\n" +
			"        for (var i = 0; i < frames.length; i++) total += 4 + frames[i].length;\n" +
			"        var buf = new Uint8Array(total);\n" +
			"        var view = new DataView(buf.buffer);\n" +
			"        view.setUint32(0, frames.length, false);\n" +
			"        var off = 4;\n" +
			"        for (var i = 0; i < frames.length; i++) {\n" +
			"          view.setUint32(off, frames[i].length, false);\n" +
			"          off += 4;\n" +
			"          buf.set(frames[i], off);\n" +
			"          off += frames[i].length;\n" +
			"        }\n" +
			"        fetch('/upload?format=images&id=' + encodeURIComponent(id) + '&fps=' + encodeURIComponent(fps), {\n" +
			"          method: 'POST',\n" +
			"          headers: { 'Content-Type': 'application/octet-stream' },\n" +
			"          body: buf\n" +
			"        }).then(function(r) {\n" +
			"          if (r.ok) {\n" +
			"            updateStatus('Frames imported! You can close this tab.');\n" +
			"            setTimeout(function() { window.close(); }, 1200);\n" +
			"          } else updateStatus('Import error: ' + r.statusText);\n" +
			"        }).catch(function(e) { updateStatus('Upload error: ' + e); });\n" +
			"      }\n" +
			"    };\n" +
			"    function reopenDialog() {\n" +
			"      if (window.J2S && window.J2S.TrackerMP4Importer) {\n" +
			"        window.J2S.TrackerMP4Importer.createDialog(trackerBridge);\n" +
			"      }\n" +
			"    }\n" +
			"  </script>\n" +
			"  <script src=\"/tracker-mp4-import.js\"></script>\n" +
			"  <script>reopenDialog();</script>\n" +
			"</body>\n" +
			"</html>\n";
	}
}
