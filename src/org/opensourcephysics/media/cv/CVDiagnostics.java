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

		int status = getStatusCode(); // 0=working, -1=unknown error 
		String message = "";
		// display appropriate dialog
		if (status == 0) { // AVP working correctly
			try {
				message = MediaRes.getString("CVDiagnostics.Message.Description") + newline;
				message += MediaRes.getString("CVDiagnostics.Message.Version") + " ";
				message += getVersion();
				DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
				Date date = new Date(myJarFile.lastModified());
				long size = myJarFile.length();
				message += " (" + format.format(date) + ", " + size + " bytes)"; //$NON-NLS-1$ //$NON-NLS-2$
				message += newline + MediaRes.getString("CVDiagnostics.Message.Path") + ": ";
				message += myJarFile.getAbsolutePath();
			} catch (Exception ex) {}			
		}
		else {
			String[] m = getDiagnosticMessage(status, requester);
			message = m[0];		
			for (int i = 1; i < m.length; i++) {
				message += "/n" + m[i];
			}
		}

		JOptionPane.showMessageDialog(dialogOwner, message, 
				MediaRes.getString("CVDiagnostics.Dialog.Title"), //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
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
		return version;
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
