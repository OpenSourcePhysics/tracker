package org.opensourcephysics.media.cv;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JOptionPane;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.tools.Diagnostics;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.ffmpeg.global.avutil;

/**
 * Checks to see if CV Video is installed and working.
 * 
 * @author Wolfgang Christian
 * @author Douglas Brown
 * @version 1.0
 */
public class CVDiagnostics extends Diagnostics {

	@SuppressWarnings("javadoc")
	public static final String REQUEST_TRACKER = "Tracker"; //$NON-NLS-1$

	static String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	static int vmBitness;
	static String codeBase;
	static File[] codeBaseJars, cvHomeJars;
	static String pathEnvironment, pathValue;
	static String requester;
	static File myJarFile;
	
	static {
		if (!OSPRuntime.isJS) {
			vmBitness = OSPRuntime.getVMBitness();

			// get code base
			try {
				URL url = org.bytedeco.javacv.JavaCV.class.getProtectionDomain().getCodeSource().getLocation();				
				myJarFile = new File(url.toURI());
				codeBase = myJarFile.getParent();
			} catch (Exception e) {
			}

		}
	}

	private CVDiagnostics() {
	}

	/**
	 * Displays the About CVVideo dialog. If working correctly, shows version, etc.
	 * If not working, shows a diagnostic message.
	 */
	public static void aboutCV() {
		JOptionPane.showMessageDialog(dialogOwner, getAboutMessage(),
				MediaRes.getString("CVDiagnostics.Dialog.Title"), //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
	}

	/** Returns the version and runtime details displayed by About JavaCV. */
	public static String getAboutMessage() {
		int status = getStatusCode();
		if (status != 0)
			return String.join(newline, getDiagnosticMessage(status, requester));
		StringBuilder message = new StringBuilder(MediaRes.getString("CVDiagnostics.Message.Description")); //$NON-NLS-1$
		message.append(newline).append("JavaCV: ").append(getVersion()); //$NON-NLS-1$
		String javaCPPVersion = Loader.class.getPackage().getImplementationVersion();
		if (javaCPPVersion == null) {
			try {
				javaCPPVersion = Loader.getVersion();
			} catch (java.io.IOException ex) {
				javaCPPVersion = "unknown"; //$NON-NLS-1$
			}
		}
		message.append(newline).append("JavaCPP: ").append(javaCPPVersion); //$NON-NLS-1$
		message.append(newline).append("FFmpeg: ").append(avutil.av_version_info().getString()); //$NON-NLS-1$
		message.append(newline).append(MediaRes.getString("CVDiagnostics.Message.Platform")) //$NON-NLS-1$
				.append(": ").append(Loader.getPlatform()); //$NON-NLS-1$
		message.append(newline).append("Java: ").append(System.getProperty("java.version")) //$NON-NLS-1$ //$NON-NLS-2$
				.append(" (").append(System.getProperty("os.arch")).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (myJarFile != null) {
			message.append(newline).append(MediaRes.getString("CVDiagnostics.Message.Path")) //$NON-NLS-1$
					.append(": ").append(myJarFile.getAbsolutePath()); //$NON-NLS-1$
			if (myJarFile.isFile()) {
				DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
				message.append(newline).append(format.format(new Date(myJarFile.lastModified())))
						.append(", ").append(myJarFile.length()).append(" bytes"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return message.toString();
	}

	/**
	 * Displays the About dialog for Tracker or other requester.
	 * 
	 * @param request currently only "Tracker" is supported
	 */
	public static void aboutCV(String request) {
		requester = request;
		aboutCV();
	}

	/**
	 * Gets a status code that identifies the current state of the CV video
	 * engine. Codes are: 
	 * 0 working correctly 
	 * -1 none of the above
	 * 
	 * @return status code
	 */
	public static int getStatusCode() {
		
		try (Java2DFrameConverter imageConverter = new Java2DFrameConverter()) {
			FFmpegFrameGrabber.tryLoad();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error er) {
			er.printStackTrace();
		}
			
		return -1;
	}

	/**
	 * Gets a diagnostic message when CV is not working.
	 * 
	 * @param status the status code from getStatusCode() method
	 * @param        requester--currently only "Tracker" is supported
	 * @return an array strings containing the message lines
	 */
	public static String[] getDiagnosticMessage(int status, String requester) {

		if (status == 0)
			return new String[] { "OK" }; //$NON-NLS-1$

		ArrayList<String> message = new ArrayList<String>();
		switch (status) {
		default: // none of the above
			message.add("CVVideo is not working"); //$NON-NLS-1$
		}

		return message.toArray(new String[message.size()]);
	}

	/**
	 * Gets the version as a String.
	 * 
	 * @return JavaCV version
	 */
	public static String getVersion() {
		String version = org.bytedeco.javacv.JavaCV.class.getPackage().getImplementationVersion();
		return version == null ? "unknown" : version; //$NON-NLS-1$
	}

	/**
	 * Tests this class.
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
		System.out.println(getVersion());		
		aboutCV("Tracker");
	}
}
