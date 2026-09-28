package org.opensourcephysics.media.cv;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.Diagnostics;
import org.opensourcephysics.tools.JREFinder;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * Checks to see if AVP is installed and working.
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
	static String codeBase, xuggleHome;
	static File[] codeBaseJars, xuggleHomeJars;
	static String pathEnvironment, pathValue;
	static String requester;
	
	static { // added by W. Christian
		if (!OSPRuntime.isJS) {
			vmBitness = OSPRuntime.getVMBitness();

			// get code base and and XUGGLE_HOME
			try {
				URL url = CVDiagnostics.class.getProtectionDomain().getCodeSource().getLocation();
				File myJarFile = new File(url.toURI());
				codeBase = myJarFile.getParent();
			} catch (Exception e) {
			}

			xuggleHome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
			if (xuggleHome == null) {
				xuggleHome = (String) OSPRuntime.getPreference("XUGGLE_HOME"); //$NON-NLS-1$
			}
			String[] xuggleNames = TrackerStarter.getXuggleJarNames(OSPRuntime.getLaunchJarPath());
			xuggleHomeJars = new File[xuggleNames.length];
			codeBaseJars = new File[xuggleNames.length];
		}
	}

	private CVDiagnostics() {
	}

	/**
	 * Displays the About CVVideo dialog. If working correctly, shows version, etc.
	 * If not working, shows a diagnostic message.
	 */
	public static void aboutCV() {

		int status = getStatusCode();
//	 * 0 working correctly 
//	 * -1 none of the above

		String message = "CV is working correctly";
	
		JOptionPane.showMessageDialog(dialogOwner, message, 
			"About CV Video", //$NON-NLS-1$
			JOptionPane.INFORMATION_MESSAGE);


//		if (true || OSPLog.getLevelValue() <= Level.CONFIG.intValue()) {
//			OSPLog.config("status code = " + status); //$NON-NLS-1$
//			// log XUGGLE_HOME and PATH environment variables
//			OSPLog.config("XUGGLE_HOME = " + xuggleHome); //$NON-NLS-1$
//			OSPLog.config("Code base = " + codeBase); //$NON-NLS-1$
//
//			// log current java VM
//			String javaHome = System.getProperty("java.home"); //$NON-NLS-1$
//			String bitness = "(" + vmBitness + "-bit): "; //$NON-NLS-1$ //$NON-NLS-2$
//			OSPLog.config("Java VM " + bitness + javaHome); //$NON-NLS-1$
//
//		if (xuggleHome != null || true) {
//			
//		// display appropriate dialog
//		if (status == 0) { // AVP working correctly
//			String fileInfo = newline;
//			String path = " " + AVPRes.getString("Xuggle.Dialog.Unknown"); //$NON-NLS-1$ //$NON-NLS-2$
//
//			String className = "com.avpkit.core.IContainer"; //$NON-NLS-1$
//			try {
//				Class<?> avpClass = Class.forName(className);
//				URL url = avpClass.getProtectionDomain().getCodeSource().getLocation();
////				File codeFile = new File(url.getPath());
//				File codeFile = new File(url.toURI());
//				path = " " + codeFile.getAbsolutePath(); //$NON-NLS-1$
//				DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
//				Date date = new Date(codeFile.lastModified());
//				long size = codeFile.length();
//				fileInfo = " (" + format.format(date) + ", " + size + " bytes)"; //$NON-NLS-1$ //$NON-NLS-2$
//			} catch (Exception ex) {
//			}
//
//			String version = getXuggleVersion();
//			String message = AVPRes.getString("Xuggle.Dialog.AboutXuggle.Message.Version") //$NON-NLS-1$
//					+ " " + version + fileInfo + newline //$NON-NLS-1$
//					+ AVPRes.getString("Xuggle.Dialog.AboutXuggle.Message.Home") //$NON-NLS-1$
//					+ " " + xuggleHome + newline //$NON-NLS-1$
//					+ AVPRes.getString("Xuggle.Dialog.AboutXuggle.Message.Path") //$NON-NLS-1$
//					+ path;
//			message = AVPRes.getString("AVP.Dialog.AboutAVP.Message.Version") //$NON-NLS-1$
//					+ " " + version + fileInfo + newline //$NON-NLS-1$
//					+ AVPRes.getString("AVP.Dialog.AboutAVP.Message.Path") //$NON-NLS-1$
//					+ path;
//			
//			JOptionPane.showMessageDialog(dialogOwner, message, 
//					AVPRes.getString("AVP.Dialog.AboutAVP.Title"), //$NON-NLS-1$
//					JOptionPane.INFORMATION_MESSAGE);
//		}
//
//		else { // xuggle not working
////			String[] diagnostic = getDiagnosticMessage(status, requester);
////			Box box = Box.createVerticalBox();
////			box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
////			for (String line : diagnostic) {
////				box.add(new JLabel(line));
////			}
////			boolean showPrefsQuestionForTracker = false;
////			if (status == 7 && "Tracker".equals(requester) && dialogOwner != null) { //$NON-NLS-1$
////				// wrong VM bitness: show Preferences dialog for Tracker if appropriate
////				if (OSPRuntime.isWindows()) {
////					Collection<File> jreDirs = JREFinder.getFinder().getJREs(32);
////					showPrefsQuestionForTracker = !jreDirs.isEmpty();
////				} else if (OSPRuntime.isMac()) {
////					showPrefsQuestionForTracker = true;
////				}
////			}
////			boolean showCopyJarsQuestionForTracker = false;
////			if (status == 5 && REQUEST_TRACKER.equals(requester) && dialogOwner != null) { //$NON-NLS-1$
////				showCopyJarsQuestionForTracker = true;
////			}
////			if (showPrefsQuestionForTracker) {
////				box.add(new JLabel("  ")); //$NON-NLS-1$
////				String question = XuggleRes.getString("Xuggle.Dialog.AboutXuggle.ShowPrefs.Question"); //$NON-NLS-1$
////				box.add(new JLabel(question));
////
////				int response = JOptionPane.showConfirmDialog(dialogOwner, box,
////						XuggleRes.getString("Xuggle.Dialog.BadXuggle.Title"), //$NON-NLS-1$
////						JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
////				if (response == JOptionPane.YES_OPTION) {
////					// call Tracker method by reflection
////					try {
////						Class<?> trackerClass = Class.forName("org.opensourcephysics.cabrillo.tracker.TFrame"); //$NON-NLS-1$
////						if (dialogOwner.getClass().equals(trackerClass)) {
////							Method m = trackerClass.getMethod("showPrefsDialog", String.class); //$NON-NLS-1$
////							m.invoke(dialogOwner, "runtime"); //$NON-NLS-1$
////						}
////					} catch (Exception e) {
////					}
////				}
////			} 
////			else if (showCopyJarsQuestionForTracker) {
////				box.add(new JLabel("  ")); //$NON-NLS-1$
////				String question = XuggleRes.getString("Xuggle.Dialog.AboutXuggle.CopyJars.Question"); //$NON-NLS-1$
////				box.add(new JLabel(question));
////
////				int response = JOptionPane.showConfirmDialog(dialogOwner, box,
////						XuggleRes.getString("Xuggle.Dialog.BadXuggle.Title"), //$NON-NLS-1$
////						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
////				if (response == JOptionPane.YES_OPTION) {
////					String source = XML.forwardSlash(xuggleHome); //$NON-NLS-1$
////					if (!xuggleNames[0].contains("-server-"))
////						source += "/share/java/jars";
////					// copy jars to codebase directory
////					if (!TrackerStarter.copyXuggleJarsTo(codeBase, source)) {
////						JOptionPane.showMessageDialog(dialogOwner, "Unable to copy xuggle jars", "Copy Failure", //$NON-NLS-1$
////								JOptionPane.ERROR_MESSAGE);
////					}
////				}
////			} else {
////				JOptionPane.showMessageDialog(dialogOwner, box, XuggleRes.getString("Xuggle.Dialog.BadXuggle.Title"), //$NON-NLS-1$
////						JOptionPane.WARNING_MESSAGE);
////			}
//		}
//		}
////		}
//
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
	 * Gets a status code that identifies the current state of the Xuggle video
	 * engine. Codes are: 
	 * 0 working correctly 
	 * 1 not installed (XUGGLE_HOME==null, no xuggle jar in code base) 
	 * 2 can't find xuggle home (XUGGLE_HOME==null but xuggle jar found in code base) 
	 * 3 XUGGLE_HOME incomplete: missing xuggle jar in XUGGLE_HOME 
	 * 4 unused. was XUGGLE_HOME OK, but incorrect "PATH", "DYLD_LIBRARY_PATH", or "LD_LIBRARY_PATH" 
	 * 5 XUGGLE_HOME OK, but no xuggle jars in code base 
	 * 6 XUGGLE_HOME OK, but mismatched xuggle versions in code base
	 * 7 XUGGLE_HOME OK, but wrong Java VM bitness 
	 * -1 none of the above
	 * 
	 * @return status code
	 */
	public static int getStatusCode() {
		
		try {
			Java2DFrameConverter imageConverter = new Java2DFrameConverter();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error er) {
			er.printStackTrace();
		}
			
		return -1;
	}

	/**
	 * Gets a diagnostic message when Xuggle is not working.
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
	 * Gets the version as a String. Returns "Unknown' if version is missing
	 * or unidentified.
	 * 
	 * @return JavaCV version
	 */
	public static String getVersion() {
		String version = "Unknown"; //$NON-NLS-1$
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
