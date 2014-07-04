/*
 * The tracker.deploy package defines classes for launching and installing Tracker.
 *
 * Copyright (c) 2014  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker.deploy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.opensourcephysics.cabrillo.tracker.TrackerRes;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.DiagnosticsForXuggle;
import org.opensourcephysics.tools.ExtensionsManager;

/**
 * A class to start Tracker. This is the main executable when Tracker is
 * deployed.
 * 
 * @author Douglas Brown
 */
public class TrackerStarter {

	public static final String LOG_FILE_NAME = "tracker_start.log"; //$NON-NLS-1$
	static String prefsFileName = ".tracker.prefs"; //$NON-NLS-1$
	static String starterPrefsFileName = ".tracker_starter.prefs"; //$NON-NLS-1$
	static String newline = "\n"; //$NON-NLS-1$
	static String encoding = "UTF-8"; //$NON-NLS-1$
	static String exceptions = ""; //$NON-NLS-1$
	static String qtJavaWarning, xuggleWarning;
	static String trackerHome, userHome, javaHome, xuggleHome, userDocuments;
	static String startLogPath;
	static FilenameFilter trackerJarFilter = new TrackerJarFilter();
	static File codeBaseDir, starterJarFile;
	static double launchVersionNumber;
	static String launchVersionString;
	static int memorySize, preferredMemorySize;
	static String[] executables;
	static String logText = ""; //$NON-NLS-1$
	static String javaCommand = "java"; //$NON-NLS-1$
	static String preferredVM;
	static String snapshot = "-snapshot"; //$NON-NLS-1$
	// static String preferredEngine;
	static boolean debug = false;
	static boolean log = true;
	static boolean use32BitMode = false;
	// static boolean qtPreferred = false;
	static String version = "4.86"; //$NON-NLS-1$
	static XMLControlElement prefsXMLControl = new XMLControlElement();
	static int port = 12321;
	static Timer timer;

	static {
		// identify codeBaseDir
		try {
			newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			URL url = TrackerStarter.class.getProtectionDomain().getCodeSource()
					.getLocation();
			starterJarFile = new File(url.toURI());
			codeBaseDir = starterJarFile.getParentFile();
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
		// get user home, java home and xuggle home
		try {
			userHome = System.getProperty("user.home"); //$NON-NLS-1$
			javaHome = System.getProperty("java.home"); //$NON-NLS-1$
			if (OSPRuntime.isWindows()) {
				userDocuments = WinRegistry
						.readString(
								WinRegistry.HKEY_CURRENT_USER,
								"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", //$NON-NLS-1$
								"Personal"); //$NON-NLS-1$
			} else {
				userDocuments = userHome + "/Documents"; //$NON-NLS-1$
				if (!new File(userDocuments).exists()) {
					userDocuments = null;
				}
			}
			xuggleHome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
	}
	
	/**
	 * Relaunches a new instance of Tracker.
	 * @param args array of filenames
	 */
	public static void relaunch(final String[] args) {
		Runnable runner = new Runnable() {
			public void run() {
				TrackerStarter.main(args);
			}
		};
		new Thread(runner).start();
	}

	/**
	 * Main entry point when used as executable
	 * @param args array of filenames
	 */
	public static void main(String[] args) {
		String argString = null;
		if (args != null && args.length > 0) {
			argString = ""; //$NON-NLS-1$
			for (String next : args) {
				argString += "\"" + next + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		showDebugMessage("main arguments: " + argString); //$NON-NLS-1$
		String jarPath = null;

		// find Tracker home
		try {
			findTrackerHome();
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
		if (trackerHome == null) {
			exitGracefully(null);
		}

		// load preferences from trackerHome
		loadPreferences();

		// determine which tracker jar to launch
		try {
			jarPath = getTrackerJarPath();
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
		if (jarPath == null) {
			exitGracefully(null);
		}

		// // set Java VM if there is no preferred video engine
		// try {
		// setVMIfNoPreferredEngine();
		// } catch (Exception ex) {
		//			exceptions += ex.getClass().getSimpleName()+": "+ex.getMessage()+newline; //$NON-NLS-1$
		// }

		// copy video engine files to target VM extensions directory
		try {
			refreshVideoEngines();
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}

		// launch Tracker
		boolean launched = true;
		try {
			memorySize = preferredMemorySize;
			startTracker(jarPath, args);
		} catch (Exception ex) {
			launched = false;
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		} catch (Error er) {
			launched = false;
			exceptions += er.getClass().getSimpleName()
					+ ": " + er.getMessage() + newline; //$NON-NLS-1$
		}

		if (!launched) {
			exitGracefully(jarPath);
		}
		// System.exit(0);
	}

	/**
	 * Finds the Tracker home directory and sets the static variable trackerHome.
	 */
	private static void findTrackerHome() throws Exception {
		// first look for trackerHome in environment variable
		try {
			trackerHome = System.getenv("TRACKER_HOME"); //$NON-NLS-1$
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
		showDebugMessage("environment variable TRACKER_HOME: " + trackerHome); //$NON-NLS-1$
		if (trackerHome != null && !fileExists(trackerHome)) {
			trackerHome = null;
			showDebugMessage("TRACKER_HOME directory no longer exists"); //$NON-NLS-1$
		}

		// determine if code directory is trackerHome
		if (trackerHome == null && codeBaseDir != null) {
			showDebugMessage("code base: " + codeBaseDir.getPath()); //$NON-NLS-1$
			// accept if the code directory is named "Tracker"
			if (codeBaseDir.getPath().toLowerCase().endsWith("tracker")) { //$NON-NLS-1$
				trackerHome = codeBaseDir.getPath();
				showDebugMessage("code base accepted as trackerhome based on name"); //$NON-NLS-1$
			}

			// or if it has any tracker jars in it
			else {
				try {
					String[] fileNames = codeBaseDir.list(trackerJarFilter);
					if (fileNames != null && fileNames.length > 0) {
						trackerHome = codeBaseDir.getPath();
						showDebugMessage("code base accepted as trackerhome based on contents"); //$NON-NLS-1$
					}
				} catch (Exception ex) {
					exceptions += ex.getClass().getSimpleName()
							+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
				}
			}
		}

		// if needed, try another way to see if current directory has tracker.jar
		if (trackerHome == null) {
			File file = new File((String) null, "tracker.jar"); //$NON-NLS-1$
			String dir = file.getAbsoluteFile().getParent();
			if (fileExists(dir)) {
				trackerHome = dir;
				showDebugMessage("parent directory accepted as trackerhome based on contents"); //$NON-NLS-1$
			}
		}
		if (trackerHome == null)
			throw new NullPointerException("trackerhome not found"); //$NON-NLS-1$
		showDebugMessage("using trackerhome: " + trackerHome); //$NON-NLS-1$
	}

	// /**
	// * Finds all available Java VMs and returns their paths in a Set<String>[].
	// * Returned array[0] is 32-bit VMs, array[1] is 64-bit VMs.
	// * @return Set<String>[] of available VMs
	// */
	// private static ArrayList<Set<String>> findVMs() {
	// ArrayList<Set<String>> results = new ArrayList<Set<String>>();
	// ExtensionsManager javaManager = ExtensionsManager.getManager();
	// results.add(javaManager.getAllJREs(32));
	// results.add(javaManager.getAllJREs(64));
	// return results;
	// }
	//
	/**
	 * Exits gracefully by giving information to the user.
	 */
	private static void exitGracefully(String jarPath) {
		if (timer!=null) timer.stop();
		if (exceptions.equals("")) //$NON-NLS-1$
			exceptions = "None"; //$NON-NLS-1$
		String startLogLine = ""; //$NON-NLS-1$
		if (startLogPath!=null) {
			startLogLine = "For more information see " + startLogPath + newline; //$NON-NLS-1$
		}
		if (jarPath != null) {
			JOptionPane
					.showMessageDialog(
							null,
							"Tracker could not be started automatically due to" + newline //$NON-NLS-1$
									+ "the problem(s) listed below.  However, you may be able to" + newline //$NON-NLS-1$
									+ "start it directly by double-clicking the jar file" + newline //$NON-NLS-1$
									+ jarPath
									+ "." + newline + newline //$NON-NLS-1$
									+ startLogLine
									+ "For trouble-shooting or to download the latest installer," + newline //$NON-NLS-1$
									+ "please see www.cabrillo.edu/~dbrown/tracker/." + newline + newline //$NON-NLS-1$
									+ "Problems:" + newline + exceptions, //$NON-NLS-1$
							"TrackerStarter Vers " + version + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.ERROR_MESSAGE);
		} else {
			if (trackerHome == null) {
				if (codeBaseDir != null) {
					JOptionPane
							.showMessageDialog(
									null,
									"It appears you have an incomplete Tracker installation, since" + newline //$NON-NLS-1$
											+ "no directory named \"Tracker\" could be found and " + newline //$NON-NLS-1$
											+ "no tracker.jar or tracker-x.xx.jar file exists in " + newline //$NON-NLS-1$
											+ codeBaseDir
											+ newline
											+ newline
											+ startLogLine
											+ "For trouble-shooting or to download the latest installer," + newline //$NON-NLS-1$
											+ "please see www.cabrillo.edu/~dbrown/tracker/." + newline + newline //$NON-NLS-1$
											+ "Problems:" + newline + exceptions, //$NON-NLS-1$
									"TrackerStarter Vers " + version + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
									JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane
							.showMessageDialog(
									null,
									"It appears you have an incomplete Tracker installation, since" + newline //$NON-NLS-1$
											+ "no directory named \"Tracker\" could be found and " + newline //$NON-NLS-1$
											+ "no tracker.jar or tracker-x.xx.jar file exists in the current directory." + newline + newline //$NON-NLS-1$
											+ startLogLine
											+ "For trouble-shooting or to download the latest installer," + newline //$NON-NLS-1$
											+ "please see www.cabrillo.edu/~dbrown/tracker/." + newline + newline //$NON-NLS-1$
											+ "Problems:" + newline + exceptions, //$NON-NLS-1$
									"TrackerStarter Vers " + version + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
									JOptionPane.ERROR_MESSAGE);
				}
			} else {
				String jarHome = OSPRuntime.isMac() ? codeBaseDir.getAbsolutePath()
						: trackerHome;
				JOptionPane
						.showMessageDialog(
								null,
								"No tracker.jar or tracker-x.xx.jar was found in" + newline //$NON-NLS-1$
										+ jarHome
										+ newline
										+ newline
										+ startLogLine
										+ "For trouble-shooting or to download the latest installer," + newline //$NON-NLS-1$
										+ "please see www.cabrillo.edu/~dbrown/tracker/." + newline + newline //$NON-NLS-1$
										+ "Problems:" + newline + exceptions, //$NON-NLS-1$
								"TrackerStarter Vers " + version + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
								JOptionPane.ERROR_MESSAGE);
			}

		}
		writeUserLog();
		System.exit(0);
	}

	/**
	 * Loads preferences from a preferences file.
	 */
	private static void loadPreferences() {
		// look for prefs file in user home
		File prefsFile = null;
		boolean loaded = false;
		boolean tried = false;
		if (userHome != null) {
			tried = true;
			prefsFile = new File(userHome, prefsFileName);
			prefsXMLControl.read(prefsFile.getPath());
		}
		// look in tracker home if no user home or not read
		if (!tried || prefsXMLControl.failedToRead()) {
			prefsFile = new File(trackerHome, prefsFileName);
			prefsXMLControl.read(prefsFile.getPath());
		}
		// look in code base directory if still not read
		if (prefsXMLControl.failedToRead()) {
			prefsFile = new File(codeBaseDir, prefsFileName);
			prefsXMLControl.read(prefsFile.getPath());
		}
		if (!prefsXMLControl.failedToRead()) {
			showDebugMessage("loading starter preferences from: " + prefsFile.getPath()); //$NON-NLS-1$
			use32BitMode = prefsXMLControl.getBoolean("32-bit"); //$NON-NLS-1$
			//    	qtPreferred = prefsXMLControl.getBoolean("qt_preferred"); //$NON-NLS-1$
			//    	preferredEngine = prefsXMLControl.getString("video_engine"); //$NON-NLS-1$
			if (prefsXMLControl.getPropertyNames().contains("tracker_jar")) { //$NON-NLS-1$
				loaded = true;
				String jar = prefsXMLControl.getString("tracker_jar"); //$NON-NLS-1$
				if (!jar.equals("tracker.jar")) { //$NON-NLS-1$
					int dot = jar.indexOf(".jar"); //$NON-NLS-1$
					String ver = jar.substring(8, dot);
					String versionStr = ver;
	    		int n = ver.toLowerCase().indexOf(snapshot);
	    		if (n>-1) {
	    			ver = ver.substring(0, n);
	    		}
					try {
						launchVersionNumber = Double.parseDouble(ver);
						launchVersionString = versionStr;
						showDebugMessage("preferred version: " + launchVersionString); //$NON-NLS-1$
					} catch (Exception ex) {
						showDebugMessage("version number could not be parsed: " + ver); //$NON-NLS-1$
					}
				}
			}
			if (prefsXMLControl.getPropertyNames().contains("java_vm")) { //$NON-NLS-1$
				loaded = true;
				preferredVM = prefsXMLControl.getString("java_vm"); //$NON-NLS-1$
				File javaFile = OSPRuntime.getJavaFile(preferredVM);
				if (javaFile != null && javaFile.exists()) {
					javaCommand = XML.stripExtension(javaFile.getPath());
					showDebugMessage("preferred java VM: " + javaCommand); //$NON-NLS-1$
				} else {
					showDebugMessage("preferred java VM invalid--using default instead"); //$NON-NLS-1$
					preferredVM = null;
					javaCommand = "java"; //$NON-NLS-1$
				}
			}

			if (prefsXMLControl.getPropertyNames().contains("run")) { //$NON-NLS-1$
				loaded = true;
				executables = (String[]) prefsXMLControl.getObject("run"); //$NON-NLS-1$
				for (int i = 0; i < executables.length; i++) {
					String app = executables[i];
					if (app == null)
						continue;
					File runFile = new File(trackerHome, app); // app is relative address
					if (!runFile.exists()) {
						runFile = new File(app); // app is absolute address
						if (!runFile.exists()) {
							runFile = null;
							showDebugMessage("executable file not found: " + app); //$NON-NLS-1$
						}
					}
					if (runFile != null)
						try {
							showDebugMessage("executing " + runFile.getAbsolutePath()); //$NON-NLS-1$
							ProcessBuilder pb = new ProcessBuilder(runFile.getAbsolutePath());
							pb.directory(new File(trackerHome));
							Process p = pb.start();
							p.waitFor();
						} catch (Exception ex) {
							showDebugMessage("execution failed: " + ex.getClass().getSimpleName() + " " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
						}
				}
			}
			if (prefsXMLControl.getPropertyNames().contains("memory_size")) { //$NON-NLS-1$
				preferredMemorySize = prefsXMLControl.getInt("memory_size"); //$NON-NLS-1$
				showDebugMessage("preferred memory size: " + preferredMemorySize + " MB"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!loaded)
				showDebugMessage("no starter preferences found in " + prefsFile.getPath()); //$NON-NLS-1$      		
		}
		if (loaded) {
			writeStarterPrefs();
			return;
		}

		// if no prefs found in tracker prefs, look for deprecated starterPrefsFile
		if (userHome != null) {
			prefsFile = new File(userHome, starterPrefsFileName);
		}
		// if not found in user home, check TRACKER_HOME
		if (prefsFile == null || !prefsFile.exists()) {
			prefsFile = new File(trackerHome, starterPrefsFileName);
		}
		if (prefsFile.exists())
			showDebugMessage("loading starter preferences from " + prefsFile.getAbsolutePath()); //$NON-NLS-1$
		else {
			showDebugMessage("no starter preferences found"); //$NON-NLS-1$      		
			return;		
		}
		
		StringBuffer buffer = new StringBuffer();
		ArrayList<String> runPaths = new ArrayList<String>();
		Charset charset = Charset.forName(encoding);
		try {
			InputStream stream = new FileInputStream(prefsFile);
			BufferedReader in = new BufferedReader(new InputStreamReader(stream,
					charset));
			String line = in.readLine();
			while (line != null) {
				if (line.startsWith("run ")) { //$NON-NLS-1$
					String app = line.substring(4).trim();
					File runFile = new File(trackerHome, app); // app is relative address
					if (!runFile.exists()) {
						runFile = new File(app);
						if (!runFile.exists()) {
							runFile = null;
							showDebugMessage("run target not found: " + app); //$NON-NLS-1$
						}
					}
					if (runFile != null)
						try {
							showDebugMessage("running " + runFile.getAbsolutePath()); //$NON-NLS-1$
							ProcessBuilder pb = new ProcessBuilder(runFile.getAbsolutePath());
							pb.directory(new File(trackerHome));
							Process p = pb.start();
							p.waitFor();
							runPaths.add(app);
						} catch (Exception ex) {
							showDebugMessage("run failed: " + ex.getClass().getSimpleName() + " " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
						}
				} else if (line.startsWith("version ")) { //$NON-NLS-1$
					String ver = line.substring(8).trim();
					try {
						launchVersionNumber = Double.parseDouble(ver);
						launchVersionString = ver;
						showDebugMessage("preferred version: " + launchVersionString); //$NON-NLS-1$
					} catch (Exception ex) {
						showDebugMessage("version number could not be parsed: " + ver); //$NON-NLS-1$
					}
				} else if (line.startsWith("jre ")) { //$NON-NLS-1$
					javaCommand = line.substring(4).trim();
					showDebugMessage("preferred java VM: " + javaCommand); //$NON-NLS-1$
					// check that loaded jre is valid
					File file = new File(javaCommand).getParentFile();
					if (file == null || !file.exists()) {
						showDebugMessage("preferred java VM invalid--using default instead"); //$NON-NLS-1$
						javaCommand = "java"; //$NON-NLS-1$
					}
				}
				buffer.append(line + newline);
				line = in.readLine();
			}
			in.close();
			executables = runPaths.toArray(new String[0]);
		} catch (Exception ex) {
			showDebugMessage("unable to load preferences: " + ex); //$NON-NLS-1$
		} catch (Error er) {
			showDebugMessage("unable to load preferences: " + er); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the preferred tracker jar path
	 * 
	 * @return the path, or null if none found
	 */
	private static String getTrackerJarPath() throws Exception {
		String jarPath = null;
		String jarHome = OSPRuntime.isMac() ? codeBaseDir.getAbsolutePath()
				: trackerHome;
		if (OSPRuntime.isMac()) {
			showDebugMessage("Mac OSX: looking for tracker jars in " + jarHome); //$NON-NLS-1$
		} else if (OSPRuntime.isWindows()) {
			showDebugMessage("Windows: looking for tracker jars in " + jarHome); //$NON-NLS-1$
		} else {
			showDebugMessage("Linux: looking for tracker jars in " + jarHome); //$NON-NLS-1$
		}
		try {
			File dir = new File(jarHome);
			String[] fileNames = dir.list(trackerJarFilter);
			if (fileNames != null && fileNames.length > 0) {
				String s = "tracker jars found: "; //$NON-NLS-1$
				for (String next : fileNames) {
					s += next + ", "; //$NON-NLS-1$
				}
				showDebugMessage(s.substring(0, s.length() - 2));
				String defaultJar = null;
				String numberedJar = null;
				double version = 0;
				for (int i = 0; i < fileNames.length; i++) {
					if ("tracker.jar".equals(fileNames[i].toLowerCase())) {//$NON-NLS-1$
						defaultJar = fileNames[i];
					}
					try {
						String vers = fileNames[i].substring(8);
						vers = vers.substring(0, vers.length() - 4);
						String versionStr = vers;
		    		int n = vers.toLowerCase().indexOf(snapshot);
		    		if (n>-1) {
		    			vers = vers.substring(0, n);
		    		}

						double nextVersion = Double.parseDouble(vers);
						if (nextVersion == launchVersionNumber && versionStr.equals(launchVersionString)) {
							File file = new File(jarHome, fileNames[i]);
							showDebugMessage("using tracker jar: " + file.getAbsolutePath()); //$NON-NLS-1$
							return file.getAbsolutePath();
						}
						if (nextVersion > version) { // look for latest numbered version
							version = nextVersion;
							numberedJar = fileNames[i];
						}
					} catch (Exception ex) {
					}
				}
				jarPath = defaultJar != null ? defaultJar : numberedJar;
			}
		} catch (Exception ex) { // if file access fails, try unnumbered tracker.jar
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
			showDebugMessage(ex.toString());
			jarPath = "tracker.jar"; //$NON-NLS-1$
		}
		if (jarPath != null) {
			// look in jarHome
			File file = new File(jarHome, jarPath);
			if (file.exists()) {
				showDebugMessage("using tracker jar: " + file.getAbsolutePath()); //$NON-NLS-1$
				return file.getAbsolutePath();
			}
		}
		throw new NullPointerException("No Tracker jar files found in " + jarHome); //$NON-NLS-1$
	}

	/**
	 * Launches the specified tracker jar with a list of arguments
	 * 
	 * @param jarPath the path to the tracker jar
	 * @param args the arguments (may be null)
	 */
	private static void startTracker(String jarPath, String[] args)
			throws Exception {

		// assemble the command
		final ArrayList<String> cmd = new ArrayList<String>();
		if (javaCommand.equals("java") && javaHome != null) { //$NON-NLS-1$
			javaCommand = XML.forwardSlash(javaHome) + "/bin/java"; //$NON-NLS-1$
		}
		cmd.add(javaCommand);

		if (memorySize > 0) {
			cmd.add("-Xms32m"); //$NON-NLS-1$
			cmd.add("-Xmx" + memorySize + "m"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (OSPRuntime.isMac()) {
			cmd.add(use32BitMode ? "-d32" : "-d64"); //$NON-NLS-1$ //$NON-NLS-2$
			cmd.add("-Xdock:name=Tracker"); //$NON-NLS-1$
		}

		cmd.add("-jar"); //$NON-NLS-1$
		cmd.add(jarPath);
		if (args != null && args.length > 0)
			for (String next : args) {
				if (next != null)
					cmd.add(next);
			}

		// prepare to execute the command
		ProcessBuilder builder = new ProcessBuilder(cmd);
		
		// set environment variables for new process
		Map<String, String> env = builder.environment();
		String portVar = "TRACKER_PORT"; //$NON-NLS-1$
		env.put(portVar, String.valueOf(port));
		if (logText.indexOf(portVar)==-1) {
			showDebugMessage("setting environment variable "+portVar+" = " + String.valueOf(port)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (memorySize<preferredMemorySize) {
			env.put("MEMORY_SIZE", String.valueOf(memorySize)); //$NON-NLS-1$
			showDebugMessage("setting environment variable MEMORY_SIZE = " + String.valueOf(memorySize)); //$NON-NLS-1$ 
		}
		if (xuggleWarning!=null) env.put("XUGGLE_WARNING", xuggleWarning); //$NON-NLS-1$ 
		if (qtJavaWarning!=null) env.put("QTJAVA_WARNING", qtJavaWarning); //$NON-NLS-1$ 
		
		// on OS X, add DYLD_LIBRARY_PATH to environment here
		// note TRACKER_HOME, XUGGLE_HOME must be in environment BEFORE running this
		if (OSPRuntime.isMac()
				&& xuggleHome!=null && new File(xuggleHome+"/lib").exists()) { //$NON-NLS-1$
			env.put("DYLD_LIBRARY_PATH", xuggleHome+"/lib"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// assemble command message for log
		String message = ""; //$NON-NLS-1$
		for (String next: cmd) {
			message += next + " "; //$NON-NLS-1$
		}
		showDebugMessage("executing command: " + message); //$NON-NLS-1$ 

		// write codeBase tracker_start log
		writeCodeBaseLog();

		// write the tracker_start log and set environment variable
		startLogPath = writeUserLog();
		if (startLogPath!=null)
			env.put("START_LOG", startLogPath); //$NON-NLS-1$
		
		// set up timer to exit after short delay
		if (timer==null) {
			timer = new Timer(1000, new ActionListener() {
				 public void actionPerformed(ActionEvent e) {
					 System.exit(0);
				 }
			 });
			timer.setRepeats(false);
			timer.start();
		}
		else {
			// reset timer every time a new process is started
			timer.restart();
		}
		
		// start the Tracker process and wait for it to finish
		// note that successful process should not return until Tracker is exited
		final Process process = builder.start();
		int result = process.waitFor();
		
		// if process returns immediately with exit code 1, log it's error and input streams
		if (result > 0) {
      String errors = ""; //$NON-NLS-1$
			try {
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	      String s = reader.readLine();
	    	while (s != null && s.length()>0) {
	    		errors += "\n      "+s; //$NON-NLS-1$
		      s = reader.readLine();
	    	}
	    	reader.close();
	    	reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	      s = reader.readLine();
	    	while (s != null && s.length()>0) {
	    		errors += "\n      "+s; //$NON-NLS-1$
		      s = reader.readLine();
	    	}
	    	reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
	    showDebugMessage("failed to start with memory size "+memorySize+"MB due to the following errors:"+errors); //$NON-NLS-1$ //$NON-NLS-2$
    	
	    // if process failed due to excessive memory size, reduce size and try again
	    if (errors.indexOf("heap")>-1) { //$NON-NLS-1$
    		memorySize *= 0.95;
    		if (memorySize<64) {
    			exceptions += errors + newline;
    			exitGracefully(jarPath);
    		}
    		showDebugMessage("try to start with smaller memory size "+memorySize+"MB"); //$NON-NLS-1$ //$NON-NLS-2$
    		startTracker(jarPath, args);
    	}
	    else {
  			exceptions += errors + newline;
  			exitGracefully(jarPath);
	    }
		}
		else {
			// should never get here--exits via timer
			System.exit(0);
		}
	}

	private static String writeUserLog() {
		if ("".equals(logText)) //$NON-NLS-1$
			return null;

		File file = null;
		if (new File(trackerHome).canWrite())
			file = new File(trackerHome, LOG_FILE_NAME);
		if (userDocuments != null && new File(userDocuments).canWrite()) {
			if (new File(userDocuments + "/Tracker").canWrite()) { //$NON-NLS-1$
				file = new File(userDocuments + "/Tracker", LOG_FILE_NAME); //$NON-NLS-1$
			}
			else {
				file = new File(userDocuments, LOG_FILE_NAME);			
			}
		}
		
		if (file==null) return null;
		
		addLogHeader();
		showDebugMessage("writing start log to "+file.getAbsolutePath()); //$NON-NLS-1$

		try {
			FileOutputStream stream = new FileOutputStream(file);
			Charset charset = Charset.forName(encoding);
			OutputStreamWriter out = new OutputStreamWriter(stream, charset);
			BufferedWriter writer = new BufferedWriter(out);
			writer.write(logText);
			writer.flush();
			writer.close();
		} catch (IOException ex) {
			return null;
		}
		return file.getAbsolutePath();
	}
	
	private static void addLogHeader() {
		if (!logText.startsWith("TrackerStarter")) { //$NON-NLS-1$
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  MMM dd yyyy"); //$NON-NLS-1$
			Calendar cal = Calendar.getInstance();
			logText = "TrackerStarter version " + version + "  " //$NON-NLS-1$ //$NON-NLS-2$
					+ sdf.format(cal.getTime()) + newline + newline + logText;
		}		
	}

	private static void writeCodeBaseLog() {
		// writes log file to codeBaseDir 
		if (codeBaseDir!=null && codeBaseDir.canWrite()) {
			addLogHeader();
			File file = new File(codeBaseDir, LOG_FILE_NAME);
			showDebugMessage("writing start log to "+file.getAbsolutePath()); //$NON-NLS-1$
			try {
				FileOutputStream stream = new FileOutputStream(file);
				Charset charset = Charset.forName(encoding);
				OutputStreamWriter out = new OutputStreamWriter(stream, charset);
				BufferedWriter writer = new BufferedWriter(out);
				writer.write(logText);
				writer.flush();
				writer.close();
			} catch (IOException ex) {
			}
		}
	}

	/**
	 * Writes starter preferences.
	 */
	private static void writeStarterPrefs() {
		File starterPrefsFile = new File(userHome, starterPrefsFileName);
		if (starterPrefsFile.exists() && !starterPrefsFile.canWrite()) {
			return;
		}
		StringBuffer buf = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  MMM dd yyyy"); //$NON-NLS-1$
		Calendar cal = Calendar.getInstance();
		buf.append("TrackerStarter version " + version + "  " //$NON-NLS-1$ //$NON-NLS-2$
				+ sdf.format(cal.getTime()));
		// add notice of deprecation
		buf.append("\nNote: .tracker_starter.prefs is deprecated as of version 4.62."); //$NON-NLS-1$
		buf.append("\nThis file is for backward compatibility only."); //$NON-NLS-1$
		// add jre path
		if (!"java".equals(javaCommand)) { //$NON-NLS-1$
			buf.append("\n\njre " + javaCommand); //$NON-NLS-1$
		}
		// add tracker version
		if (launchVersionNumber > 2.5) {
			buf.append("\nversion " + launchVersionString); //$NON-NLS-1$
		}
		// add executable paths
		if (executables != null) {
			for (String path : executables) {
				if (path == null || "".equals(path))continue; //$NON-NLS-1$
				buf.append("\nrun " + path); //$NON-NLS-1$
			}
		}
		if (buf.length() > 0)
			try {
				FileOutputStream stream = new FileOutputStream(starterPrefsFile);
				Charset charset = Charset.forName(encoding);
				Writer out = new OutputStreamWriter(stream, charset);
				BufferedWriter output = new BufferedWriter(out);
				output.write(buf.toString());
				output.flush();
				output.close();
				showDebugMessage("writing backup starter preferences to " + starterPrefsFile.getPath()); //$NON-NLS-1$
			} catch (IOException ex) {
			}
	}

	private static boolean fileExists(String path) {
		File file = new File(path);
		try {
			if (file.exists()) {
				return true;
			}
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		} catch (Error er) {
			exceptions += er.getClass().getSimpleName()
					+ ": " + er.getMessage() + newline; //$NON-NLS-1$
		}
		return false;
	}

	private static void showDebugMessage(String message) {
		if (log) {
			logText += " - " + message + newline; //$NON-NLS-1$
		}
		if (debug) {
			JOptionPane.showMessageDialog(null, message,
					"debug", JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
		}
	}

	/**
	 * Copies Xuggle jars and QTJava.zip to target VM extensions directory.
	 */
	private static void refreshVideoEngines() throws Exception {
		ExtensionsManager manager = ExtensionsManager.getManager();
		String jrePath = preferredVM != null ? preferredVM : javaHome;
		File extDir = new File(jrePath, "lib/ext"); //$NON-NLS-1$
		
		// Xuggle
		if (manager.copyXuggleJarsTo(extDir)) {
			showDebugMessage("copied xuggle jars to " + extDir.getAbsolutePath()); //$NON-NLS-1$
		}
		else {
	    File extFile = new File(extDir, "xuggle-xuggler.jar"); //$NON-NLS-1$
	    if (extFile.exists()) {
				showDebugMessage("xuggle jars found in " + extDir.getAbsolutePath()); //$NON-NLS-1$	
	    }
	    else {
	    	String xuggleHome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
	    	if (xuggleHome==null || !new File(xuggleHome+"/share/java/jars/xuggle-xuggler.jar").exists()) {  //$NON-NLS-1$
					String message = "xuggle jars not found"; //$NON-NLS-1$
					if (xuggleHome==null) message += ": XUGGLE_HOME is undefined"; //$NON-NLS-1$
					else message += " in "+xuggleHome; //$NON-NLS-1$
	    		showDebugMessage(message);
	    	}
	    	else {
	    		// failed to copy xuggle jars to ext directory--permissions problem?
	    		String xuggleSourceDir = new File(xuggleHome+"/share/java/jars").getAbsolutePath(); //$NON-NLS-1$
					showDebugMessage("unable to copy xuggle jars from "+xuggleSourceDir+" to "+extDir.getAbsolutePath());    		 //$NON-NLS-1$ //$NON-NLS-2$
					
					// assemble xuggleWarning to pass to Tracker as an environment variable
					xuggleWarning = TrackerRes.getString("TrackerStarter.Warning.FailedToCopy1");  //$NON-NLS-1$ 
					xuggleWarning += "\n"+TrackerRes.getString("TrackerStarter.Warning.FailedToCopy2");  //$NON-NLS-1$ //$NON-NLS-2$
					xuggleWarning += "\n \n"+TrackerRes.getString("TrackerStarter.Warning.FilesToCopy")+" ";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					for (String next: DiagnosticsForXuggle.getXuggleJarNames()) {
						xuggleWarning += next+", "; //$NON-NLS-1$
					}
					xuggleWarning = xuggleWarning.substring(0, xuggleWarning.lastIndexOf(", ")); //$NON-NLS-1$
					xuggleWarning += "\n"+TrackerRes.getString("TrackerStarter.Warning.CopyFrom")+" "+xuggleSourceDir; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					xuggleWarning += "\n"+TrackerRes.getString("TrackerStarter.Warning.CopyTo")+" "+extDir;  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	    	}
	    }
	    
		}
		
		// QuickTime
		if (manager.copyQTJavaTo(extDir)) {
			showDebugMessage("copied QTJava.zip to " + extDir.getAbsolutePath()); //$NON-NLS-1$
		}
		else {
	    File extFile = new File(extDir, "QTJava.zip"); //$NON-NLS-1$
	    if (extFile.exists()) {
				showDebugMessage("QTJava.zip found in " + extDir.getAbsolutePath()); //$NON-NLS-1$
	    }
	    else {
		    File qtSource = manager.getQTJavaZip(); // file to be copied
	    	if (qtSource==null) {
					showDebugMessage("QTJava.zip not found"); //$NON-NLS-1$
	    	}
	    	else {
	    		// Windows Vista special case--fails to launch Tracker if qtJavaWarning code is executed...
	    		if (System.getProperty("os.name", "").toLowerCase().contains("vista")) return; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		
	    		// failed to copy QTJava to ext directory--permissions problem?
					showDebugMessage("unable to copy "+qtSource.getAbsolutePath()+" to "+extDir.getAbsolutePath());  //$NON-NLS-1$ //$NON-NLS-2$
					
					// assemble qtJavaWarning to pass to Tracker as an environment variable
					qtJavaWarning = TrackerRes.getString("TrackerStarter.Warning.FailedToCopy1");  //$NON-NLS-1$ 
					qtJavaWarning += "\n"+TrackerRes.getString("TrackerStarter.Warning.FailedToCopy2");  //$NON-NLS-1$ //$NON-NLS-2$
					qtJavaWarning += "\n \n"+TrackerRes.getString("TrackerStarter.Warning.FilesToCopy")+" QTJava.zip";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					qtJavaWarning += "\n"+TrackerRes.getString("TrackerStarter.Warning.CopyFrom")+" "+qtSource.getParent(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					qtJavaWarning += "\n"+TrackerRes.getString("TrackerStarter.Warning.CopyTo")+" "+extDir;  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	    	}
	    }
	    
		}
	}

	// /**
	// * Sets an appropriate Java VM if no preferred video engine is defined.
	// * pig: this doesn't work yet
	// */
	// private static void setVMIfNoPreferredEngine() throws Exception {
	// if (preferredEngine==null) { // should only happen the first time Tracker
	// is used
	// String jrePath = preferredVM!=null? preferredVM: javaHome;
	// ExtensionsManager manager = ExtensionsManager.getManager();
	// boolean qtInstalled = manager.getQTJavaZip()!=null;
	// double xuggleVersion = VideoIO.guessXuggleVersion();
	// // OSX: use 32-bit VM unless Xuggle version is 3.4
	// if (OSPRuntime.isMac()) {
	// use32BitMode = xuggleVersion!=3.4;
	// }
	// // Windows: use 32-bit VM unless no QuickTime and Xuggle version is 5.4
	// else if (OSPRuntime.isWindows()) {
	// if (!qtInstalled && xuggleVersion==5.4) {
	// if (manager.is32BitVM(jrePath)) {
	// // switch to default 64-bit VM, if any
	// String jre64 = manager.getDefaultJRE(64);
	// if (jre64!=null) {
	// File javaFile = OSPRuntime.getJavaFile(jre64);
	// if (javaFile!=null && javaFile.exists()) {
	// preferredVM = jre64;
	// javaCommand = XML.stripExtension(javaFile.getPath());
	//			    			showDebugMessage("initial startup java VM: "+javaCommand); //$NON-NLS-1$
	// }
	// }
	// }
	// }
	// else if (!manager.is32BitVM(jrePath)) {
	// // switch to 32-bit VM
	// String jre32 = manager.getDefaultJRE(32);
	// if (jre32!=null) {
	// File javaFile = OSPRuntime.getJavaFile(jre32);
	// if (javaFile!=null && javaFile.exists()) {
	// preferredVM = jre32;
	// javaCommand = XML.stripExtension(javaFile.getPath());
	//		    			showDebugMessage("initial startup java VM: "+javaCommand); //$NON-NLS-1$
	// }
	// }
	//
	// }
	// }
	// }
	// }
	//

}
