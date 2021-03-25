package org.opensourcephysics.media.xuggle;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.tools.Diagnostics;
import org.opensourcephysics.tools.JREFinder;

import com.xuggle.xuggler.IContainer;

/**
 * Checks to see if Xuggle is installed and working.
 * 
 * @author Wolfgang Christian
 * @author Douglas Brown
 * @version 1.0
 */
public class DiagnosticsForXuggle extends Diagnostics {

	@SuppressWarnings("javadoc")
	public static final String REQUEST_TRACKER = "Tracker"; //$NON-NLS-1$

	static String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	static String[] xuggleJarNames = new String[] { "xuggle-xuggler-server-all.jar", "slf4j-api.jar" }; //$NON-NLS-1$
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
				URL url = DiagnosticsForXuggle.class.getProtectionDomain().getCodeSource().getLocation();
				File myJarFile = new File(url.toURI());
				codeBase = myJarFile.getParent();
			} catch (Exception e) {
			}

			xuggleHome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
			if (xuggleHome == null) {
				xuggleHome = (String) OSPRuntime.getPreference("XUGGLE_HOME"); //$NON-NLS-1$
			}

			xuggleHomeJars = new File[xuggleJarNames.length];
			codeBaseJars = new File[xuggleJarNames.length];
		}
	}

	private DiagnosticsForXuggle() {
	}

	/**
	 * Displays the About Xuggle dialog. If working correctly, shows version, etc.
	 * If not working, shows a diagnostic message.
	 */
	public static void aboutXuggle() {

		int status = getStatusCode();
//	 * 0 working correctly 
//	 * 1 not installed (XUGGLE_HOME==null, no xuggle jar in code base) 
//	 * 2 can't find xuggle home (XUGGLE_HOME==null but xuggle jar found in code base) 
//	 * 3 XUGGLE_HOME incomplete: missing xuggle jar in XUGGLE_HOME 
//	 * 4 unused 
//	 * 5 XUGGLE_HOME OK, but no xuggle jars or mismatched in code base 
//	 * 6 unused
//	 * 7 XUGGLE_HOME OK, but wrong Java VM bitness 
//	 * -1 none of the above
		

		if (OSPLog.getLevelValue() <= Level.CONFIG.intValue()) {
			OSPLog.config("status code = " + status); //$NON-NLS-1$
			// log XUGGLE_HOME and PATH environment variables
			OSPLog.config("XUGGLE_HOME = " + xuggleHome); //$NON-NLS-1$
			OSPLog.config("Code base = " + codeBase); //$NON-NLS-1$

			// log current java VM
			String javaHome = System.getProperty("java.home"); //$NON-NLS-1$
			String bitness = "(" + vmBitness + "-bit): "; //$NON-NLS-1$ //$NON-NLS-2$
			OSPLog.config("Java VM " + bitness + javaHome); //$NON-NLS-1$

		if (xuggleHome != null) {
			
			File[] xuggleJars = new File(xuggleHome).listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return file.getName().startsWith("xuggle-xuggler") ||
							file.getName().equals("slf4j-api.jar");
				}				
			});
			// log xuggle home jars
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String[] jarDates = new String[xuggleJarNames.length];
			String[] jarSizes = new String[xuggleJarNames.length];
			String fileData = "Xuggle home files: "; //$NON-NLS-1$
			for (int i = 0; i < Math.min(jarSizes.length, xuggleJars.length); i++) {
				jarDates[i] = xuggleJars[i] == null ? "" : 
					" (modified " + sdf.format(xuggleJars[i].lastModified()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			for (int i = 0; i < Math.min(jarSizes.length, xuggleJars.length); i++) {
				jarSizes[i] = xuggleJars[i] == null ? ")" : 
					", size " + (xuggleJars[i].length() / 1024) + "kB) "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			for (int i = 0; i < Math.min(jarSizes.length, xuggleJars.length); i++) {
				if (i > 0)
					fileData += ", "; //$NON-NLS-1$
				fileData += xuggleJars[i] == null? "null": xuggleJars[i].getName() + jarDates[i] + jarSizes[i]; //$NON-NLS-1$
			}
			OSPLog.config(fileData);

			// log codeBase jars
			xuggleJars = getXuggleJarFiles(codeBase);
			fileData = "Code base files: "; //$NON-NLS-1$
			for (int i = 0; i < jarDates.length; i++) {
				jarDates[i] = xuggleJars[i] == null ? "" : " (modified " + sdf.format(xuggleJars[i].lastModified()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			for (int i = 0; i < Math.min(jarSizes.length, xuggleJars.length); i++) {
				jarSizes[i] = xuggleJars[i] == null ? ")" : ", size " + (xuggleJars[i].length() / 1024) + "kB) "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			for (int i = 0; i < jarSizes.length; i++) {
				if (i > 0)
					fileData += ", "; //$NON-NLS-1$
				fileData += xuggleJars[i] == null? xuggleJarNames[i] + " NOT FOUND": xuggleJars[i].getName() + jarDates[i] + jarSizes[i]; //$NON-NLS-1$
			}
			OSPLog.config(fileData);

		// display appropriate dialog
		if (status == 0) { // xuggle working correctly
			String fileInfo = newline;
			String path = " " + XuggleRes.getString("Xuggle.Dialog.Unknown"); //$NON-NLS-1$ //$NON-NLS-2$

			String className = "com.xuggle.xuggler.IContainer"; //$NON-NLS-1$
			try {
				Class<?> xuggleClass = Class.forName(className);
				URL url = xuggleClass.getProtectionDomain().getCodeSource().getLocation();
//				File codeFile = new File(url.getPath());
				File codeFile = new File(url.toURI());
				path = " " + codeFile.getAbsolutePath(); //$NON-NLS-1$
				DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
				Date date = new Date(codeFile.lastModified());
				fileInfo = " (" + format.format(date) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception ex) {
			}

			String version = getXuggleVersion();
			String message = XuggleRes.getString("Xuggle.Dialog.AboutXuggle.Message.Version") //$NON-NLS-1$
					+ " " + version + fileInfo + newline //$NON-NLS-1$
					+ XuggleRes.getString("Xuggle.Dialog.AboutXuggle.Message.Home") //$NON-NLS-1$
					+ " " + xuggleHome + newline //$NON-NLS-1$
					+ XuggleRes.getString("Xuggle.Dialog.AboutXuggle.Message.Path") //$NON-NLS-1$
					+ path;
			JOptionPane.showMessageDialog(dialogOwner, message, XuggleRes.getString("Xuggle.Dialog.AboutXuggle.Title"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		}

		else { // xuggle not working
			String[] diagnostic = getDiagnosticMessage(status, requester);
			Box box = Box.createVerticalBox();
			box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			for (String line : diagnostic) {
				box.add(new JLabel(line));
			}
			boolean showPrefsQuestionForTracker = false;
			if (status == 7 && "Tracker".equals(requester) && dialogOwner != null) { //$NON-NLS-1$
				// wrong VM bitness: show Preferences dialog for Tracker if appropriate
				if (OSPRuntime.isWindows()) {
					Collection<File> jreDirs = JREFinder.getFinder().getJREs(32);
					showPrefsQuestionForTracker = !jreDirs.isEmpty();
				} else if (OSPRuntime.isMac()) {
					showPrefsQuestionForTracker = true;
				}
			}
			boolean showCopyJarsQuestionForTracker = false;
			if (status == 5 && REQUEST_TRACKER.equals(requester) && dialogOwner != null) { //$NON-NLS-1$
				showCopyJarsQuestionForTracker = true;
			}
			if (showPrefsQuestionForTracker) {
				box.add(new JLabel("  ")); //$NON-NLS-1$
				String question = XuggleRes.getString("Xuggle.Dialog.AboutXuggle.ShowPrefs.Question"); //$NON-NLS-1$
				box.add(new JLabel(question));

				int response = JOptionPane.showConfirmDialog(dialogOwner, box,
						XuggleRes.getString("Xuggle.Dialog.BadXuggle.Title"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
				if (response == JOptionPane.YES_OPTION) {
					// call Tracker method by reflection
					try {
						Class<?> trackerClass = Class.forName("org.opensourcephysics.cabrillo.tracker.TFrame"); //$NON-NLS-1$
						if (dialogOwner.getClass().equals(trackerClass)) {
							Method m = trackerClass.getMethod("showPrefsDialog", String.class); //$NON-NLS-1$
							m.invoke(dialogOwner, "runtime"); //$NON-NLS-1$
						}
					} catch (Exception e) {
					}
				}
			} else if (showCopyJarsQuestionForTracker) {
				box.add(new JLabel("  ")); //$NON-NLS-1$
				String question = XuggleRes.getString("Xuggle.Dialog.AboutXuggle.CopyJars.Question"); //$NON-NLS-1$
				box.add(new JLabel(question));

				int response = JOptionPane.showConfirmDialog(dialogOwner, box,
						XuggleRes.getString("Xuggle.Dialog.BadXuggle.Title"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (response == JOptionPane.YES_OPTION) {
					// copy jars to codebase directory
					File dir = new File(codeBase);
					copyXuggleJarsTo(dir);
				}
			} else {
				JOptionPane.showMessageDialog(dialogOwner, box, XuggleRes.getString("Xuggle.Dialog.BadXuggle.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			}
		}
		}
		}

	}

	/**
	 * Displays the About Xuggle dialog for Tracker or other requester.
	 * 
	 * @param request currently only "Tracker" is supported
	 */
	public static void aboutXuggle(String request) {
		requester = request;
		aboutXuggle();
	}

	/**
	 * Gets the xuggle jar files (named in xuggleJarNames) found in a given
	 * directory. Always returns the array, but individual elements may be null.
	 * 
	 * @param dir the directory
	 * @return the array of jar files found
	 */
	public static File[] getXuggleJarFiles(String dir) {
		File[] jarFiles = new File[xuggleJarNames.length];
		for (int i = 0; i < jarFiles.length; i++) {
			String next = xuggleJarNames[i];
			File file = new File(dir, next);
			jarFiles[i] = file.exists() ? file : null;
		}
		return jarFiles;
	}

	/**
	 * Gets the xuggle jar names.
	 * 
	 * @return an array of jar names
	 */
	public static String[] getXuggleJarNames() {
		return xuggleJarNames;
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
		
		codeBaseJars = getXuggleJarFiles(codeBase);
		pathEnvironment = OSPRuntime.isWindows() ? "Path" //$NON-NLS-1$
				: OSPRuntime.isMac() ? "DYLD_LIBRARY_PATH" : "LD_LIBRARY_PATH"; //$NON-NLS-1$ //$NON-NLS-2$
		pathValue = System.getenv(pathEnvironment);

		// return 0 if xuggle classes available
		try {
			IContainer.make(); // throws exception if xuggle not available
			return 0;
		} catch (Exception e) {
//			e.printStackTrace();
		} catch (Error er) {
//			er.printStackTrace();
		}

		boolean completeCodeBase = codeBaseJars[0] != null;
		for (int i = 1; i < codeBaseJars.length; i++) {
			completeCodeBase = completeCodeBase && codeBaseJars[i] != null;
		}

		if (xuggleHome == null) {
			return completeCodeBase ? 2 : 1;
		}

		// get xuggle home jars
		String jarName = XML.stripExtension(xuggleJarNames[0]);
//		xuggleHomeJars = getXuggleJarFiles(xuggleHome); //$NON-NLS-1$
		xuggleHomeJars = new File(xuggleHome).listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.getName().startsWith(jarName);
			}				
		});
		boolean completeHome = xuggleHomeJars.length > 0 && xuggleHomeJars[0] != null;
//		for (int i = 1; i < xuggleHomeJars.length; i++) {
//			completeHome = completeHome && xuggleHomeJars[i] != null;
//		}

		if (!completeHome)
			return 3; // no xuggle jars in XUGGLE_HOME

		// from this point on XUGGLE_HOME is OK
		if (!completeCodeBase)
			return 5; // no xuggle jars in code base

		// code base files mismatched?
		boolean mismatched = xuggleHomeJars[0].length() != codeBaseJars[0].length();
		if (mismatched)
			return 5; // mismatched xuggle jars in code base--treat as 5

		// Xuggle requires 64-bit VM
		if (vmBitness == 32)
			return 7;
			
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

		case 1: // not installed (XUGGLE_HOME==null, missing xuggle jars in code base and
				// extensions)
			message.add(XuggleRes.getString("Xuggle.Dialog.NoXuggle.Message1")); //$NON-NLS-1$
			message.add(" "); //$NON-NLS-1$
			if (REQUEST_TRACKER.equals(requester)) {
				message.add(XuggleRes.getString("Xuggle.Dialog.ReplaceXuggle.Message3")); //$NON-NLS-1$
				message.add(" "); //$NON-NLS-1$
				message.add(XuggleRes.getString("Xuggle.Dialog.AboutXuggle.TrackerInstallerPath.Message")); //$NON-NLS-1$
				message.add(Diagnostics.TRACKER_INSTALLER_URL);
			}
			break;

		case 2: // can't find xuggle home (XUGGLE_HOME==null, but xuggle jars found in code base
				// and/or extensions)
			message.add(XuggleRes.getString("Xuggle.Dialog.BadXuggle.Message")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NoXuggleHome.Message1")); //$NON-NLS-1$
			if (REQUEST_TRACKER.equals(requester)) {
				message.add(XuggleRes.getString("Xuggle.Dialog.ReplaceXuggle.Message3")); //$NON-NLS-1$
				message.add(" "); //$NON-NLS-1$
				message.add(XuggleRes.getString("Xuggle.Dialog.AboutXuggle.TrackerInstallerPath.Message")); //$NON-NLS-1$
				message.add(Diagnostics.TRACKER_INSTALLER_URL);
			}
			break;

		case 3: // XUGGLE_HOME incomplete: missing or bad xuggle jar in XUGGLE_HOME
			message.add(XuggleRes.getString("Xuggle.Dialog.BadXuggle.Message")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.IncompleteXuggle.Message1")); //$NON-NLS-1$
			if (REQUEST_TRACKER.equals(requester)) {
				message.add(XuggleRes.getString("Xuggle.Dialog.ReplaceXuggle.Message3")); //$NON-NLS-1$
				message.add(" "); //$NON-NLS-1$
				message.add(XuggleRes.getString("Xuggle.Dialog.AboutXuggle.TrackerInstallerPath.Message")); //$NON-NLS-1$
				message.add(Diagnostics.TRACKER_INSTALLER_URL);
			}
			break;

		case 4: // XUGGLE_HOME OK, but incorrect "PATH", "DYLD_LIBRARY_PATH", or
				// "LD_LIBRARY_PATH"
			message.add(XuggleRes.getString("Xuggle.Dialog.MissingEnvironmentVariable.Message1")); //$NON-NLS-1$
			message.add("\"" + pathEnvironment + "\" " //$NON-NLS-1$ //$NON-NLS-2$
					+ XuggleRes.getString("Xuggle.Dialog.MissingEnvironmentVariable.Message2")); //$NON-NLS-1$
			break;

		case 5: // XUGGLE_HOME OK, but no xuggle jars in code base
			String missingJars = ""; //$NON-NLS-1$
			for (int i = 0; i < xuggleJarNames.length; i++) {
				if (codeBaseJars[i] == null) {
					if (missingJars.length() > 1)
						missingJars += ", "; //$NON-NLS-1$
					missingJars += xuggleJarNames[i];
				}
			}
			String source = XML.forwardSlash(xuggleHome); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.NotWorking.Message")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.Missing.Message")); //$NON-NLS-1$
			message.add(" "); //$NON-NLS-1$
			message.add(missingJars);
			message.add(" "); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.CopyToCodeBase.Message1")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.CopyToCodeBase.Message2")); //$NON-NLS-1$
			message.add(" "); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.SourceDirectory.Message") + " " + source); //$NON-NLS-1$ //$NON-NLS-2$
			message.add(XuggleRes.getString("Xuggle.Dialog.TargetDirectory.Message") + " " + codeBase); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		case 6: // XUGGLE_HOME OK, but mismatched xuggle versions in code base
			missingJars = ""; //$NON-NLS-1$
			for (int i = 0; i < xuggleJarNames.length; i++) {
				if (codeBaseJars[i] == null) {
					if (missingJars.length() > 1)
						missingJars += ", "; //$NON-NLS-1$
					missingJars += xuggleJarNames[i];
				}
			}
			source = XML.forwardSlash(xuggleHome); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.NotWorking.Message")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.Mismatched.Message")); //$NON-NLS-1$
			message.add(" "); //$NON-NLS-1$
			message.add(missingJars);
			message.add(" "); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.CopyToCodeBase.Message1")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.NeedJars.CopyToCodeBase.Message2")); //$NON-NLS-1$
			message.add(" "); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.SourceDirectory.Message") + " " + source); //$NON-NLS-1$ //$NON-NLS-2$
			message.add(XuggleRes.getString("Xuggle.Dialog.TargetDirectory.Message") + " " + codeBase); //$NON-NLS-1$ //$NON-NLS-2$
			break;

		case 7: // XUGGLE_HOME OK, but wrong Java VM bitness--should be 64-bit
			message.add(XuggleRes.getString("Xuggle.Dialog.WrongVMMac.Message1")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.WrongVMMac.Message2")); //$NON-NLS-1$
			if (REQUEST_TRACKER.equals(requester)) {
				message.add(" "); //$NON-NLS-1$
				message.add(XuggleRes.getString("Xuggle.Dialog.WrongVMMac.Message3")); //$NON-NLS-1$
				message.add(XuggleRes.getString("Xuggle.Dialog.WrongVMMac.Message4")); //$NON-NLS-1$
			}
			break;

		case 8:
			message.add(XuggleRes.getString("Xuggle.Dialog.UnsupportedVersion.Message1")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.UnsupportedVersion.Message2")); //$NON-NLS-1$
			break;

		default: // none of the above
			message.add(XuggleRes.getString("Xuggle.Dialog.BadXuggle.Message")); //$NON-NLS-1$
			message.add(XuggleRes.getString("Xuggle.Dialog.UnknownProblem.Message")); //$NON-NLS-1$
//			if (REQUEST_TRACKER.equals(requester)) {
//				message.add(XuggleRes.getString("Xuggle.Dialog.ReplaceXuggle.Message3")); //$NON-NLS-1$
//				message.add(" "); //$NON-NLS-1$
//				message.add(XuggleRes.getString("Xuggle.Dialog.AboutXuggle.TrackerInstallerPath.Message")); //$NON-NLS-1$
//				message.add(Diagnostics.TRACKER_INSTALLER_URL);
//			} else {
//				message.add(XuggleRes.getString("Xuggle.Dialog.NoXuggleHome.Message2")); //$NON-NLS-1$
//				message.add(" "); //$NON-NLS-1$
//				message.add(XuggleRes.getString("Xuggle.Dialog.AboutXuggle.Message.InstallerPath")); //$NON-NLS-1$
//				message.add(XUGGLE_INSTALLER_URL);
//			}

		}

		return message.toArray(new String[message.size()]);
	}

	/**
	 * Gets the Xuggle version as a String. Returns "Unknown' if Xuggle is missing
	 * or unidentified.
	 * 
	 * @return Xuggle version
	 */
	public static String getXuggleVersion() {
		String xuggleVersion = XuggleRes.getString("Xuggle.Dialog.Unknown"); //$NON-NLS-1$
		int status = getStatusCode();
		if (status == 0) { // xuggle working correctly
			try {
				String name = "com.xuggle.xuggler.Version"; //$NON-NLS-1$
				Class<?> xuggleClass = Class.forName(name);
				Method method = xuggleClass.getMethod("getVersionString"); //$NON-NLS-1$
				xuggleVersion = (String) method.invoke(null, (Object[]) null);
			} catch (Exception ex) {
			} catch (Error err) {
			}
		}
		return xuggleVersion;
	}

//	/**
//	 * Returns the best guess Xuggle version as a double based on file size. For an
//	 * exact version number, use DiagnosticsForXuggle (requires Xuggle to be
//	 * running).
//	 * 
//	 * @return 3.4 or 5.4 if xuggle installed, otherwise 0.0
//	 */
//	public static double guessXuggleVersion() {
//		File xuggleJar = getXuggleJar();
//		if (xuggleJar != null) {
//			return xuggleJar.length() < XUGGLE_54_FILE_LENGTH ? 3.4 : 5.4;
//		}
//		return 0;
//	}
//
	/**
	 * Gets the xuggle-xuggler.jar from the xuggleHome directory, if it exists.
	 *
	 * @return xuggle-xuggler.jar
	 */
	public static File getXuggleJar() {
		if (xuggleHome == null) {
			return null;
		}

		File[] jars = new File(xuggleHome).listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().startsWith(TrackerStarter.XUGGLE_SERVER_NAME);
			}				
		});
		if (jars.length > 0) {
			return jars[0]; //$NON-NLS-1$
		}
//		File xuggleJar = new File(xuggleHome + "/xuggle-xuggler.jar"); //$NON-NLS-1$
//		if (xuggleJar.exists()) {
//			return xuggleJar;
//		}
		return null;
	}

	/**
	 * Copies Xuggle jar files to a target directory. Does nothing if the target
	 * files exist and are the same size.
	 *
	 * @param dir the directory
	 * @return true if jars are copied
	 */
	public static boolean copyXuggleJarsTo(File dir) {
		if (xuggleHome == null || dir == null) {
			return false;
		}
		File xuggleJarDir = new File(xuggleHome); //$NON-NLS-1$
		File[] xuggleJars = xuggleJarDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().startsWith(XML.stripExtension(xuggleJarNames[0]));
			}				
		});
		boolean copied = false;
		// todo: if more than one in XuggleJars, get latest
		for (int i = 0; i < xuggleJarNames.length; i++) {
			File xuggleFile = i == 0? xuggleJars[0]: new File(xuggleJarDir, xuggleJarNames[i]);
			long fileLength = xuggleFile.length();
			File target = new File(dir, xuggleJarNames[i]);
			// copy jar
			if (!target.exists() || target.length() != fileLength) {
				copied = VideoIO.copyFile(xuggleFile, target) || copied;
			}
		}
		return copied;
	}

	/**
	 * Tests this class.
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
		System.out.println(getXuggleVersion());		
		aboutXuggle("Tracker");
	}
}
