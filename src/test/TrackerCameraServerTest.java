package test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.opensourcephysics.media.TrackerCameraServer;

/**
 * Unit test for TrackerCameraServer.
 */
public class TrackerCameraServerTest {

	public static void main(String[] args) {
		try {
			testServerLifecycleAndEndpoints();
			System.out.println("PASS: TrackerCameraServerTest completed successfully.");
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	public static void testServerLifecycleAndEndpoints() throws Exception {
		int port = TrackerCameraServer.startServer();
		if (port <= 0) {
			throw new AssertionError("Server did not return a valid port: " + port);
		}

		String base = "http://127.0.0.1:" + port;

		// 1. Test /status endpoint
		HttpURLConnection conn = (HttpURLConnection) new URL(base + "/status").openConnection();
		int status = conn.getResponseCode();
		if (status != 200) {
			throw new AssertionError("/status returned HTTP " + status);
		}
		String statusBody = readString(conn.getInputStream());
		if (!statusBody.contains("\"status\":\"ok\"")) {
			throw new AssertionError("Unexpected /status body: " + statusBody);
		}

		// 2. Test /capture endpoint
		conn = (HttpURLConnection) new URL(base + "/capture").openConnection();
		status = conn.getResponseCode();
		if (status != 200) {
			throw new AssertionError("/capture returned HTTP " + status);
		}
		String htmlBody = readString(conn.getInputStream());
		if (!htmlBody.contains("Tracker Video Capture")) {
			throw new AssertionError("/capture did not contain expected HTML title: " + htmlBody);
		}

		// 3. Test /tracker-mp4-import.js endpoint
		conn = (HttpURLConnection) new URL(base + "/tracker-mp4-import.js").openConnection();
		status = conn.getResponseCode();
		if (status != 200) {
			throw new AssertionError("/tracker-mp4-import.js returned HTTP " + status);
		}
		String jsBody = readString(conn.getInputStream());
		if (!jsBody.contains("TrackerMP4Importer")) {
			throw new AssertionError("/tracker-mp4-import.js missing TrackerMP4Importer: " + jsBody.substring(0, Math.min(200, jsBody.length())));
		}

		// 4. Test /upload MP4 POST endpoint with a mock target
		final AtomicReference<String> receivedId = new AtomicReference<>();
		final AtomicReference<byte[]> receivedData = new AtomicReference<>();
		final AtomicBoolean received = new AtomicBoolean(false);

		MockTarget mockTarget = new MockTarget(receivedId, receivedData, received);
		TrackerCameraServer.setCurrentTarget(mockTarget);

		byte[] testPayload = new byte[] { 0x00, 0x00, 0x00, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2' };
		conn = (HttpURLConnection) new URL(base + "/upload?format=mp4&id=session123").openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "video/mp4");
		try (OutputStream os = conn.getOutputStream()) {
			os.write(testPayload);
		}
		status = conn.getResponseCode();
		if (status != 200) {
			throw new AssertionError("/upload returned HTTP " + status);
		}

		// Wait for SwingUtilities.invokeLater
		for (int i = 0; i < 50; i++) {
			if (received.get()) break;
			Thread.sleep(20);
		}

		if (!received.get()) {
			throw new AssertionError("Mock target did not receive importMP4Capture call");
		}
		if (!"session123".equals(receivedId.get())) {
			throw new AssertionError("Expected session id 'session123' but got " + receivedId.get());
		}
		if (receivedData.get() == null || receivedData.get().length != testPayload.length) {
			throw new AssertionError("Payload length mismatch: expected " + testPayload.length);
		}

		// 5. Test /upload Images POST endpoint
		final AtomicReference<String> imgId = new AtomicReference<>();
		final AtomicReference<byte[][]> imgFrames = new AtomicReference<>();
		final AtomicReference<Double> imgFps = new AtomicReference<>();
		final AtomicBoolean imgReceived = new AtomicBoolean(false);

		mockTarget.setImagesListener((id, frames, fps) -> {
			imgId.set(id);
			imgFrames.set(frames);
			imgFps.set(fps);
			imgReceived.set(true);
		});

		byte[] frame1 = new byte[] { 1, 2, 3, 4 };
		byte[] frame2 = new byte[] { 5, 6, 7, 8, 9, 10 };
		ByteArrayOutputStream stackBytes = new ByteArrayOutputStream();
		java.io.DataOutputStream dos = new java.io.DataOutputStream(stackBytes);
		dos.writeInt(2); // count
		dos.writeInt(frame1.length);
		dos.write(frame1);
		dos.writeInt(frame2.length);
		dos.write(frame2);
		dos.flush();

		conn = (HttpURLConnection) new URL(base + "/upload?format=images&id=stack123&fps=15.0").openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/octet-stream");
		try (OutputStream os = conn.getOutputStream()) {
			os.write(stackBytes.toByteArray());
		}
		status = conn.getResponseCode();
		if (status != 200) {
			throw new AssertionError("/upload images returned HTTP " + status);
		}

		for (int i = 0; i < 50; i++) {
			if (imgReceived.get()) break;
			Thread.sleep(20);
		}

		if (!imgReceived.get()) {
			throw new AssertionError("Mock target did not receive importVideoCapture call");
		}
		if (!"stack123".equals(imgId.get()) || imgFps.get() != 15.0 || imgFrames.get().length != 2) {
			throw new AssertionError("Images import mismatch: id=" + imgId.get() + ", fps=" + imgFps.get());
		}

		// 6. Test stop
		TrackerCameraServer.stop();
		if (TrackerCameraServer.isRunning()) {
			throw new AssertionError("Server is still running after stop()");
		}
	}

	private static String readString(InputStream in) throws Exception {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buf = new byte[4096];
			int n;
			while ((n = in.read(buf)) != -1) {
				baos.write(buf, 0, n);
			}
			return baos.toString(StandardCharsets.UTF_8.name());
		}
	}

	@FunctionalInterface
	public interface ImagesListener {
		void onImages(String id, byte[][] frames, double fps);
	}

	public static class MockTarget {
		private final AtomicReference<String> idRef;
		private final AtomicReference<byte[]> dataRef;
		private final AtomicBoolean flag;
		private ImagesListener imagesListener;

		public MockTarget(AtomicReference<String> idRef, AtomicReference<byte[]> dataRef, AtomicBoolean flag) {
			this.idRef = idRef;
			this.dataRef = dataRef;
			this.flag = flag;
		}

		public void setImagesListener(ImagesListener l) {
			this.imagesListener = l;
		}

		public void importMP4Capture(String id, byte[] data) {
			idRef.set(id);
			dataRef.set(data);
			flag.set(true);
		}

		public void importVideoCapture(String id, byte[][] frames, double fps) {
			if (imagesListener != null) {
				imagesListener.onImages(id, frames, fps);
			}
		}
	}
}

