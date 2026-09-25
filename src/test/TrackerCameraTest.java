package test;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.opensourcephysics.cabrillo.tracker.TFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.TrackerCamera;

/**
 * Transpile and run test.TrackerCameraTest to test the browser camera dialog.
 * Uses the existing _ES6/tracker-camera-import.js resource in the SwingJS site.
 * After starting a source, check the video-pixel X/Y min/max fields. Enable
 * "Capture selected region only" to edit them or drag a selection. Edits must
 * stay within the source dimensions and preserve at least 10 pixels per axis.
 * The fields should reflect the full frame when cropping is disabled and be
 * locked during recording and the screen-capture countdown.
 */
public class TrackerCameraTest {

	private final JLabel status = new JLabel("Press Open Camera to test the camera dialog.");

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new TrackerCameraTest());
	}

	public TrackerCameraTest() {
		// TrackerCamera requires a TFrame to locate its SwingJS applet.
		TFrame frame = new TFrame();
		frame.setTitle("Tracker Camera Test");
		frame.setJMenuBar(null);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JButton cameraButton = new JButton("Open Camera");
		cameraButton.addActionListener(e -> {
			if (OSPRuntime.isJS) {
				new TrackerCamera(frame);
			} else {
				status.setText("Run the transpiled app in a browser to use the camera.");
			}
		});

		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.add(cameraButton, BorderLayout.NORTH);
		panel.add(status, BorderLayout.CENTER);
		frame.setContentPane(panel);
		frame.setSize(520, 160);
		OSPRuntime.setAppClass(this);
		frame.setVisible(true);
	}

	/**
	 * Receives the camera dialog's captured frames without launching Tracker.
	 *
	 * @j2sAlias importVideoCapture
	 */
	public void importVideoCapture(String id, byte[][] data, double frameRate) {
		int count = data == null ? 0 : data.length;
		status.setText("Received " + count + " frames at " + frameRate + " fps.");
	}
}
