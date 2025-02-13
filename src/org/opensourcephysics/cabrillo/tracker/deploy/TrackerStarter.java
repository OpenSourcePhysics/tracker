/*
 * The tracker.deploy package defines classes for launching and installing Tracker.
 *
 * Copyright (c) 2025 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <https://https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker.deploy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.nio.charset.Charset;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.JREFinder;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * A class to start Tracker. This is the main executable when Tracker is
 * deployed.
 * 
 * @author Douglas Brown
 */
public class TrackerStarter {

	public static final String PREFERRED_TRACKER_JAR = "PREFERRED_TRACKER_JAR"; //$NON-NLS-1$
	public static final String PREFERRED_MEMORY_SIZE = "PREFERRED_MEMORY_SIZE"; //$NON-NLS-1$
//	public static final String PREFERRED_JAVA_VM = "PREFERRED_JAVA_VM"; //$NON-NLS-1$
//	public static final String PREFERRED_VM_BITNESS = "PREFERRED_VM_BITNESS"; //$NON-NLS-1$
//	public static final String PREFERRED_TRACKER_PREFS = "PREFERRED_TRACKER_PREFS"; //$NON-NLS-1$
	public static final String TRACKER_RELAUNCH = "TRACKER_RELAUNCH"; //$NON-NLS-1$	
	public static final String TRACKER_NEW_VERSION = "TRACKER_NEW_VERSION"; //$NON-NLS-1$	
	public static final String NEW_INSTALL = "NEW_INSTALL"; //$NON-NLS-1$	
	public static final String LOG_FILE_NAME = "tracker_start.log"; //$NON-NLS-1$
	public static final String LOG_DIAGNOSTICS_NAME = "tracker_start_diagnostics.log"; //$NON-NLS-1$
  public static final int DEFAULT_MEMORY_SIZE = 1024;
	public static final int MINIMUM_MEMORY_SIZE = 64;
	public static final String PREFS_FILE_NAME = "tracker.prefs"; //$NON-NLS-1$
	public static final int INDEX_XUGGLE_57 = 0;
	public static final int INDEX_XUGGLE_34 = 1;
	  
	static String newline = "\n"; //$NON-NLS-1$
	static String encoding = "UTF-8"; //$NON-NLS-1$
	static String exceptions = ""; //$NON-NLS-1$
	static String xuggleWarning, ffmpegWarning, starterWarning;
	static String trackerHome, userHome, javaHome, xuggleHome, userDocuments;
	static String startLogPath;
	static FilenameFilter trackerJarFilter = new TrackerJarFilter();
	static File codeBaseDir, starterJarFile, xuggleServerJar, xuggleJar;
	static String preferredVersionString;
	static String trackerJarPath;
	static int memorySize, preferredMemorySize;
	static String[] executables;
	static String logText = ""; //$NON-NLS-1$
	static String javaCommand = "java"; //$NON-NLS-1$
	static String preferredVM;
	static String[] bundledVMs;
	static String snapshot = "-snapshot"; //$NON-NLS-1$
	static boolean debug = false;
	static boolean log = true;
	static boolean relaunching = false;
	static boolean launching = false;
	static boolean isNewInstall = false;
	static int port = 12321;
	static Thread launchThread, exitThread;
	static boolean abortExit;
	static int exitCounter = 0;
	public static int xuggleVersionIndex;
	public static final String[][] XUGGLE_JAR_NAMES = new String[][] {
		new String[] { // for xuggle 5.7
				"xuggle-xuggler-server-all", 
				"slf4j-api" }, 
//				"slf4j-api", 
//				"logback-classic", 
//				"logback-core", 
//				"commons-cli" },
		new String[] { // for xuggle 3.4
				"xuggle-xuggler", 
				"slf4j-api", 
				"logback-classic", 
				"logback-core"}};
	
	public static FileFilter xuggleFileFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			String[] xuggleNames = XUGGLE_JAR_NAMES[xuggleVersionIndex];
			for (int i = 0; i < xuggleNames.length; i++) {
				if (file.getName().startsWith(xuggleNames[i]))
					return true;
			}
			return false;
		}				
	};
	static HashMap<String, Boolean> usesXuggleServer = new HashMap<String, Boolean>();
	
	static {
		// identify codeBaseDir
		newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		/**
		 * @j2sNative 
		 */
		{
		try {			
			URL url = TrackerStarter.class.getProtectionDomain().getCodeSource().getLocation();
			java.net.URI uri = url.toURI();
			String path = uri.toString();
			if (path.startsWith("jar:")) { //$NON-NLS-1$
				path = path.substring(4, path.length());
			}
			uri = new java.net.URI(path);
			starterJarFile = new File(uri);
			codeBaseDir = starterJarFile.getParentFile();
			OSPRuntime.setLaunchJarPath(starterJarFile.getAbsolutePath());
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
		}
		// get user home, java home and user documents
		try {
			userHome = OSPRuntime.getUserHome();
			javaHome = System.getProperty("java.home"); //$NON-NLS-1$
			
			if (OSPRuntime.isWindows()) {
				userDocuments = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
			} 
			else {
				userDocuments = userHome + "/Documents"; //$NON-NLS-1$
			}
			if (!new File(userDocuments).exists()) {
				userDocuments = null;
			}
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
	}
	
	/**
	 * Main entry point when used as executable
	 * @param args array of filenames
	 */
	public static void main(final String[] args) {
		relaunching = false;
		logText = ""; //$NON-NLS-1$
		logMessage("launch initiated by user"); //$NON-NLS-1$
		
  	if (OSPRuntime.isMac()) {
  		// create launchThread to instantiate OSXServices and launch Tracker
		  launchThread = new Thread(new Runnable() {
				@Override
				public void run() {
					// instantiate OSXServices
					String className = "org.opensourcephysics.cabrillo.tracker.deploy.OSXServices"; //$NON-NLS-1$
					try {
						Class<?> OSXClass = Class.forName(className);
						Constructor<?> constructor = OSXClass.getConstructor();
						Object OSXServices = constructor.newInstance();
						Method m = OSXClass.getDeclaredMethod("getStatus", (Class<?>[])null); //$NON-NLS-1$
						Object status = m.invoke(OSXServices, (Object[])null);	
						logMessage(""+status); //$NON-NLS-1$
					} catch (Exception ex) {
						logMessage("OSXServices failed"); //$NON-NLS-1$
					} catch (Error err) {
						logMessage("OSXServices failed"); //$NON-NLS-1$
					}
					// wait a short time for OSXServices to handle openFile event
					// and launch Tracker with file arguments (sets launchThread to null)
					int i = 0;
					while(launchThread!=null && i<5) {
						try {
							Thread.sleep(100);
							i++;
						} catch (InterruptedException e) {
						}
					};
					// launch Tracker with default args if launchThread is not null 
					if (launchThread!=null) {
						launchTracker(args);	
					}
				}
			});
			launchThread.start();				  
		}
  	else {
  		// for Windows and Linux, launch Tracker immediately with default args
			launchTracker(args);					 
  	}
	}

	/**
	 * Launches a new instance of Tracker.
	 * @param args array of filenames. The first arg may be a preferred tracker.jar.
	 */
	public static void launchTracker(String[] args) {
		if (launching) return;
		launching = true;
		logMessage("TrackerStarter running in jre: " + javaHome); //$NON-NLS-1$
		launchThread = null;
		
		// look for new version tracker jar as first argument
		if (args!=null && args.length>0 && (args[0].contains("tracker.jar")  //$NON-NLS-1$
					|| (args[0].contains("tracker-") && args[0].contains(".jar")))) { //$NON-NLS-1$ //$NON-NLS-2$
    	System.setProperty(TrackerStarter.PREFERRED_TRACKER_JAR, args[0]);
    	System.setProperty(TrackerStarter.TRACKER_NEW_VERSION, args[0]);
    	String[] newArgs = new String[args.length-1];
    	if (newArgs.length>0) {
    		System.arraycopy(args, 1, newArgs, 0, newArgs.length);
    		args = newArgs;
    	}
    	else args = null;
		}
		
		String argString = null;
		if (args != null && args.length > 0) {
			argString = ""; //$NON-NLS-1$
			for (String next : args) {
				argString += "\"" + next + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		logMessage("launching with main arguments: " + argString); //$NON-NLS-1$
		

		// find Tracker home
		try {
			trackerHome = findTrackerHome(true);
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
		if (trackerHome == null) {
			exitGracefully(null);
		}

		// find Xuggle home
		try {
			xuggleHome = findXuggleHome(trackerHome, true);
			if (xuggleHome != null) {
				// check for xuggle-xuggler.jar (ver 3.4) and xuggle-xuggler-server-all.jar in xugglehome
				xuggleJar = new File(trackerHome, XUGGLE_JAR_NAMES[1][0]+".jar");				
				if (xuggleJar.exists()) {
					logMessage("xuggle 3.4 found: " + xuggleJar); //$NON-NLS-1$					
				}
				xuggleServerJar = new File(trackerHome, XUGGLE_JAR_NAMES[0][0]+".jar");
				if (xuggleServerJar.exists()) {
					logMessage("xuggle 5.7 found: " + xuggleServerJar); //$NON-NLS-1$					
				}

			}

		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}

		// load preferences
		loadPreferences();

		// determine which tracker jar to launch
		String jarPath = null;		
		try {
			jarPath = getTrackerJarPath();
		} catch (Exception ex) {
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
		}
		if (jarPath == null) {
			exitGracefully(null);
		}

		// copy appropriate xuggle jars
		boolean usesServer = TrackerStarter.usesXuggleServer(jarPath);
		xuggleVersionIndex = usesServer? INDEX_XUGGLE_57: INDEX_XUGGLE_34;
		String xuggleVers = usesServer? "5.7": "3.4";
		String source = XML.forwardSlash(xuggleHome); //$NON-NLS-1$
		if (xuggleVersionIndex == INDEX_XUGGLE_34)
			source += "/share/java/jars";
		if (copyXuggleJarsTo(trackerHome, source)) {
			logMessage("xuggle "+xuggleVers+" files up to date "); //$NON-NLS-1$			
		}
		else {
			logMessage("xuggle "+xuggleVers+" files missing or not up to date "); //$NON-NLS-1$			
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
	}


	
	/**
	 * Relaunches a new instance of Tracker.
	 * @param args array of filenames
	 * @param secondTry true to flag this as a second try in the new process
	 */
	public static void relaunch(final String[] args, boolean secondTry) {
		relaunching = secondTry;
		launching = false;
		Runnable runner = new Runnable() {
			@Override
			public void run() {
//				logText = ""; //$NON-NLS-1$
				logMessage("relaunch initiated by Tracker"); //$NON-NLS-1$
				launchTracker(args);
			}
		};
		new Thread(runner).start();
	}

	/**
	 * Finds the Tracker home directory and sets/returns the static variable
	 * trackerHome.
	 * 
	 * @param writeToLog true to write the results to the start log
	 */
	public static String findTrackerHome(boolean writeToLog) {// throws Exception {
		if (trackerHome != null || OSPRuntime.isJS) {
			if (writeToLog) {
				logMessage("using trackerhome: " + trackerHome); //$NON-NLS-1$
			}
			return trackerHome;
		}

		/**
		 * Java only; transpiler may ignore
		 * 
		 * @j2sNative
		 * 
		 */
		{
			// first determine if code base directory is trackerHome
			if (codeBaseDir != null) {
				if (writeToLog) {
					logMessage("TrackerStarter jar: " + starterJarFile); //$NON-NLS-1$
				}
				// accept if the directory has any tracker jars in it
				try {
					String[] fileNames = codeBaseDir.list(trackerJarFilter);
					if (fileNames != null && fileNames.length > 0) {
						trackerHome = codeBaseDir.getPath();
						if (writeToLog)
							logMessage("code base accepted as trackerhome based on contents"); //$NON-NLS-1$
					}
				} catch (Exception ex) {
					exceptions += ex.getClass().getSimpleName() + ": " + ex.getMessage() + newline; //$NON-NLS-1$
				}
			}

			// if needed, try another way to see if current directory has tracker.jar
			if (trackerHome == null) {
				File file = new File((String) null, "tracker.jar"); //$NON-NLS-1$
				String dir = file.getAbsoluteFile().getParent();
				if (fileExists(file.getAbsolutePath())) {
					trackerHome = dir;
					if (writeToLog)
						logMessage("parent directory accepted as trackerhome based on contents"); //$NON-NLS-1$
				}
			}

			// if not found locally, look for (legacy) environment variable
			if (trackerHome == null) {
				try {
					trackerHome = System.getenv("TRACKER_HOME"); //$NON-NLS-1$
				} catch (Exception ex) {
					exceptions += ex.getClass().getSimpleName() + ": " + ex.getMessage() + newline; //$NON-NLS-1$
				}
				if (writeToLog)
					logMessage("environment variable TRACKER_HOME: " + trackerHome); //$NON-NLS-1$
				if (trackerHome != null && !fileExists(trackerHome)) {
					trackerHome = null;
					if (writeToLog)
						logMessage("TRACKER_HOME directory no longer exists"); //$NON-NLS-1$
				}
			}
			
			// if not found, check OSP preferences
			if (trackerHome==null) {
				trackerHome = (String)OSPRuntime.getPreference("TRACKER_HOME"); //$NON-NLS-1$
				if (writeToLog) logMessage("osp.prefs TRACKER_HOME: " + trackerHome); //$NON-NLS-1$
				if (trackerHome!=null && !fileExists(trackerHome)) {
					trackerHome = null;
					if (writeToLog) logMessage("TRACKER_HOME directory no longer exists"); //$NON-NLS-1$
				}	
			}

			if (writeToLog)
				logMessage("using trackerhome: " + trackerHome); //$NON-NLS-1$
		}
		
		// BH more graceful to return null here
		return trackerHome;
	}
	
	/**
	 * Finds and loads the preferences file into an XMLControl.
	 * 
	 * @return the loaded XMLControl, or null if no preferences file found
	 */
	public static XMLControl findPreferences() {
		if (OSPRuntime.isJS)
			return null;

		// look for all prefs files in OSPRuntime.getDefaultSearchPaths()
		// and in current directory
		Map<File, XMLControl> controls = new HashMap<File, XMLControl>();
		File firstFileFound = null, newestFileFound = null;
		long modified = 0;

		for (int i = 0; i < 2; i++) {
			String prefsFileName = PREFS_FILE_NAME;
			if (i == 1) {
				// add leading dot to fileName
				prefsFileName = "." + prefsFileName; //$NON-NLS-1$
			}
			for (String path : OSPRuntime.getDefaultSearchPaths()) {
				String prefsPath = new File(path, prefsFileName).getAbsolutePath();
				File file = new File(prefsPath);
				if (file.exists()) {
					XMLControl control = new XMLControlElement(file);
					if (!control.failedToRead() && control.getObjectClassName().endsWith("Preferences")) { //$NON-NLS-1$
						if (file.lastModified() > modified + 50) {
							newestFileFound = file;
							modified = file.lastModified();
						}
						controls.put(file, control);
						if (firstFileFound == null) {
							firstFileFound = file;
						}
					}
				}
			}
			// look in current directory
			File file = new File(prefsFileName);
			if (file.exists()) {
				XMLControl control = new XMLControlElement(file);
				if (!control.failedToRead() && control.getObjectClassName().endsWith("Preferences")) { //$NON-NLS-1$
					if (file.lastModified() > modified + 50) {
						newestFileFound = file;
						modified = file.lastModified();
					}
					controls.put(file, control);
					if (firstFileFound == null) {
						firstFileFound = file;
					}
				}
			}
		}
		// replace first file with newest if different
		if (newestFileFound != null && newestFileFound != firstFileFound) {
			ResourceLoader.copyAllFiles(newestFileFound, firstFileFound);
			controls.put(firstFileFound, controls.get(newestFileFound));
		}

		// return control associated with first file found
		if (firstFileFound != null) {
			XMLControl control = controls.get(firstFileFound);
			control.setValue("prefsPath", firstFileFound.getAbsolutePath()); //$NON-NLS-1$
			return control;
		}
		return null;
	}

	/**
	 * Finds the Xuggle home directory and sets/returns the static variable xuggleHome.
	 * 
	 * @param trackerHome the Tracker home directory (may be null)
	 * @param writeToLog true to write results to the start log
	 */
	public static String findXuggleHome(String trackerHome, boolean writeToLog) throws Exception {
		// first see if xuggleHome is child or sibling of trackerHome
		if (trackerHome!=null) {
			File trackerHomeDir = new File(trackerHome);
			File f = new File(trackerHomeDir, "Xuggle"); //$NON-NLS-1$
			if (!f.exists() || !f.isDirectory()) {
				f = new File(trackerHomeDir.getParentFile(), "Xuggle"); //$NON-NLS-1$
			}
			if ((!f.exists()||!f.isDirectory()) && OSPRuntime.isMac()) {
				f = new File("/usr/local/xuggler"); //$NON-NLS-1$
			}
			if (f.exists() && f.isDirectory()) {
				xuggleHome = f.getPath();
				if (writeToLog) logMessage("xugglehome found relative to trackerhome: "+xuggleHome); //$NON-NLS-1$
			}
		}
		
		// if not found, check OSP preferences
		if (xuggleHome==null) {
			xuggleHome = (String)OSPRuntime.getPreference("XUGGLE_HOME"); //$NON-NLS-1$
			if (writeToLog) logMessage("osp.prefs XUGGLE_HOME: " + xuggleHome); //$NON-NLS-1$
			if (xuggleHome!=null && !fileExists(xuggleHome)) {
				xuggleHome = null;
				if (writeToLog) logMessage("XUGGLE_HOME directory no longer exists"); //$NON-NLS-1$
			}	
		}

		// if still not found, look for xuggleHome in environment variable
		if (xuggleHome==null) {
			try {
				xuggleHome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
			} catch (Exception ex) {
				exceptions += ex.getClass().getSimpleName()
						+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
			}
			if (writeToLog) logMessage("environment variable XUGGLE_HOME: " + xuggleHome); //$NON-NLS-1$
			if (xuggleHome!=null && !fileExists(xuggleHome)) {
				xuggleHome = null;
				if (writeToLog) logMessage("XUGGLE_HOME directory no longer exists"); //$NON-NLS-1$
			}			
		}

		if (xuggleHome==null)
			throw new NullPointerException("xugglehome not found"); //$NON-NLS-1$
		if (writeToLog) logMessage("using xugglehome: " + xuggleHome); //$NON-NLS-1$
		return xuggleHome;
	}
	
	/**
	 * Returns the Xuggle server jar (version 5.7+), if any
	 * 
	 * @return the xuggle server jar
	 */
	public static File getXuggleServerJar() {
		return xuggleServerJar;
	}

	
	/**
	 * Finds the bundled Java VMs. Always returns array but some strings may be null.
	 */
	public static String[] findBundledVMs() {
		if (bundledVMs!=null) return bundledVMs;
		try {
			findTrackerHome(false);
		} catch (Exception e) {}
		
		if (OSPRuntime.isWindows()) {
			File jre = JREFinder.getFinder().getDefaultJRE(64, trackerHome, false, "OpenJDK");
			String jrepath = jre == null? null: jre.getPath();
			File jre32 = JREFinder.getFinder().getDefaultJRE(32, trackerHome, false, "OpenJDK");
			String jre32path = jre32 == null? null: jre32.getPath();
			return new String[] {jrepath, jre32path};
		}
		else if (OSPRuntime.isMac()) {
			File home = new File(trackerHome);
			String path = home.getParent()+"/runtime"; //$NON-NLS-1$
			File jre = JREFinder.getFinder().getDefaultJRE(64, path, false, null);
			return new String[] {jre == null? null: jre.getPath()};
		}
		else {
			File jre = JREFinder.getFinder().getDefaultJRE(64, trackerHome, false, "OpenJDK");
			return new String[] {jre == null? null: jre.getPath()};
		}
	}


	/**
	 * Exits gracefully by giving information to the user.
	 */
	private static void exitGracefully(String jarPath) {
		if (exitThread!=null) abortExit = true;
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
							"Tracker could not be started due to the problem(s) listed below." + newline //$NON-NLS-1$
									+ "However, you may be able to start it by double-clicking the file" + newline //$NON-NLS-1$
									+ jarPath
									+ "." + newline + newline //$NON-NLS-1$
									+ startLogLine
									+ "For trouble-shooting or to download the latest installer," + newline //$NON-NLS-1$
									+ "please see https://opensourcephysics.github.io/tracker-website/." + newline + newline //$NON-NLS-1$
									+ "Problems:" + newline + exceptions, //$NON-NLS-1$
							"TrackerStarter Vers " + OSPRuntime.VERSION + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
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
											+ "please see https://https://opensourcephysics.github.io/tracker-website/." + newline + newline //$NON-NLS-1$
											+ "Problems:" + newline + exceptions, //$NON-NLS-1$
									"TrackerStarter Vers " + OSPRuntime.VERSION + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
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
											+ "please see https://https://opensourcephysics.github.io/tracker-website/." + newline + newline //$NON-NLS-1$
											+ "Problems:" + newline + exceptions, //$NON-NLS-1$
									"TrackerStarter Vers " + OSPRuntime.VERSION + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
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
										+ "please see https://https://opensourcephysics.github.io/tracker-website/." + newline + newline //$NON-NLS-1$
										+ "Problems:" + newline + exceptions, //$NON-NLS-1$
								"TrackerStarter Vers " + OSPRuntime.VERSION + ": Error Starting Tracker", //$NON-NLS-1$ //$NON-NLS-2$
								JOptionPane.ERROR_MESSAGE);
			}

		}
		writeUserLog();
		writeCodeBaseLog(LOG_FILE_NAME);
		OSPRuntime.exit();
		System.exit(0);
	}
	
	static boolean isNewInstall() {
		Object curVersion = OSPRuntime.getPreference("local_OSP_version"); //$NON-NLS-1$
		logMessage("local OSP version " + curVersion); //$NON-NLS-1$
		if (curVersion == null)
			return true;

		String prev = curVersion.toString();
		String[] v1 = OSPRuntime.VERSION.split("\\."); //$NON-NLS-1$
		String[] v2 = prev.split("\\."); //$NON-NLS-1$
		for (int i = 0; i < 3; i++) {
			int current = Integer.parseInt(v1[i]);
			int old = Integer.parseInt(v2[i]);
			if (current > old) {
				return true;
			} 
			else if (current < old) {
				// unlikely! 
				break;
			}
		}			
		return false;
	}

	/**
	 * Loads preferences from a preferences file.
	 */
	private static void loadPreferences() {
		trackerJarPath = null;
		boolean loaded = false;
		XMLControl prefsXMLControl = findPreferences();
		isNewInstall = prefsXMLControl==null || isNewInstall();
		if (isNewInstall) {
			OSPRuntime.setPreference("local_OSP_version", OSPRuntime.VERSION);
			OSPRuntime.savePreferences();
			logMessage("a new Tracker version has been installed"); //$NON-NLS-1$ 
		}
		if (prefsXMLControl==null) {
			return;
		}
		String prefsPath = prefsXMLControl.getString("prefsPath"); //$NON-NLS-1$
		
		// now read the preferences from the prefsXMLControl
		// but also check environment preferences which override prefs file
		if (!prefsXMLControl.failedToRead()) {
			logMessage("loading starter preferences from: " + prefsPath); //$NON-NLS-1$

			String jar = null; // preferred jar name to be determined
			
			// preferred tracker jar
			String systemProperty = System.getProperty(PREFERRED_TRACKER_JAR);

			if (systemProperty != null) {
				loaded = true;
				trackerJarPath = systemProperty;
				jar = XML.getName(trackerJarPath);
				logMessage("system property "+PREFERRED_TRACKER_JAR+" = " + systemProperty); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else if (prefsXMLControl.getPropertyNamesRaw().contains("tracker_jar")) { //$NON-NLS-1$
				loaded = true;
				jar = prefsXMLControl.getString("tracker_jar"); //$NON-NLS-1$
			}

			String versionStr = OSPRuntime.VERSION; // default is current version
			boolean useDefaultTrackerJar = jar == null;
			if (jar!=null) { 
				if (!jar.equals("tracker.jar")) { //$NON-NLS-1$
					int dot = jar.indexOf(".jar"); //$NON-NLS-1$
					String ver = jar.substring(8, dot);
					versionStr = ver;
	    		int n = ver.toLowerCase().indexOf(snapshot);
	    		if (n>-1) {
	    			ver = ver.substring(0, n);
	    		}
					if (new OSPRuntime.Version(ver).isValid()) {
						if (new File(trackerHome, jar).exists()) {
							preferredVersionString = versionStr;
							logMessage("preferred Tracker version: " + preferredVersionString); //$NON-NLS-1$
							useDefaultTrackerJar = false;
						}
						else {
							useDefaultTrackerJar = true; 
							logMessage("preferred Tracker not found: " + jar); //$NON-NLS-1$
						}
					} 
					else {
						logMessage("version number not valid: " + ver); //$NON-NLS-1$
					}
				}
				else {
					logMessage("preferred Tracker version: tracker.jar"); //$NON-NLS-1$				
				}
			}
			if (isNewInstall) {
				useDefaultTrackerJar = true; 
				if (jar != null && !"tracker.jar".equals(jar))
				logMessage("new installation--preferred Tracker version ignored"); //$NON-NLS-1$
				jar = "tracker.jar";
				preferredVersionString = null;
			}

			if (useDefaultTrackerJar) {
				if (jar == null)
					logMessage("no preferred Tracker version, using tracker.jar (presumed "+OSPRuntime.VERSION+")"); //$NON-NLS-1$	
				else
					logMessage("using default tracker.jar (presumed "+OSPRuntime.VERSION+")"); //$NON-NLS-1$	
			}
			
			// determine if preferred tracker will use Xuggle 3.4 or Xuggle server
			String jarName = useDefaultTrackerJar? "tracker.jar": jar;
			String jarHome = OSPRuntime.isMac() ? codeBaseDir.getAbsolutePath() : trackerHome;
			String jarPath = new File(jarHome, jarName).getAbsolutePath();			
			boolean requestXuggleServer = usesXuggleServer(jarPath);
			logMessage("preferred xuggle version: "+ (requestXuggleServer? "5.7 server": "3.4")); //$NON-NLS-1$				
			
			// preferred java vm
			preferredVM = null;
			if (prefsXMLControl.getPropertyNamesRaw().contains("java_vm")) { //$NON-NLS-1$
				loaded = true;
				preferredVM = prefsXMLControl.getString("java_vm"); //$NON-NLS-1$
				logMessage("preferred java VM: "+preferredVM); //$NON-NLS-1$
			}
			// if requesting xuggle server and preferredVM is 32-bit, set preferredVM to null
			if (requestXuggleServer && xuggleServerJar != null &&
					preferredVM != null && JREFinder.getFinder().is32BitVM(preferredVM)) {
				logMessage("preferred VM ignored since xuggle 5.7 requires a 64 bit java VM"); //$NON-NLS-1$
				preferredVM = null;
			}
			// if Windows, using Xuggle 3.4 and preferredVM is 64-bit, set preferredVM to null
			if (OSPRuntime.isWindows() && !requestXuggleServer && xuggleJar != null &&
					preferredVM != null && !JREFinder.getFinder().is32BitVM(preferredVM)) {
				logMessage("preferred VM ignored since xuggle 3.4 requires a 32 bit java VM"); //$NON-NLS-1$
				preferredVM = null;
			}
			if (isNewInstall && preferredVM != null) {
				logMessage("new installation--preferred VM ignored"); //$NON-NLS-1$
				preferredVM = null;
			}
			if (preferredVM!=null) {
				File javaFile = OSPRuntime.getJavaFile(preferredVM);
				if (javaFile != null) {
					javaCommand = XML.stripExtension(javaFile.getPath());
				} 
				else {
					logMessage("preferred java VM invalid"); //$NON-NLS-1$
					preferredVM = null;
				}
			}
			if (preferredVM==null) {
				// look for bundled VMs
				bundledVMs = findBundledVMs();
				// is xuggle server requested and available?
				if (requestXuggleServer && xuggleServerJar != null) {
					if (bundledVMs[0] == null) {
						// if no bundled 64-bit use default 64-bit
						File vm = JREFinder.getFinder().getDefaultJRE(64, trackerHome, true, null);
						if (vm != null) {						
							File javaFile = OSPRuntime.getJavaFile(vm.getPath());
							if (javaFile!=null) {
								logMessage("no bundled VM, using default VM: "+vm.getPath()); //$NON-NLS-1$
								javaCommand = XML.stripExtension(javaFile.getPath());
							}
						}
					}
					else {
						File javaFile = OSPRuntime.getJavaFile(bundledVMs[0]);
						if (javaFile!=null) {
							logMessage("using bundled VM: "+bundledVMs[0]); //$NON-NLS-1$
							javaCommand = XML.stripExtension(javaFile.getPath());
						}
					}
				}
				// is xuggle 3.4 requested and available?
				else if (!requestXuggleServer && xuggleJar != null) {
					int index = OSPRuntime.isWindows()? 1: 0;
					int bitness = OSPRuntime.isWindows()? 32: 64;
					if (bundledVMs.length <= index || bundledVMs[index] == null) {
						File vm = JREFinder.getFinder().getDefaultJRE(bitness, trackerHome, true, null);
						if (vm != null) {						
							File javaFile = OSPRuntime.getJavaFile(vm.getPath());
							if (javaFile!=null) {
								logMessage("no bundled VM, using default VM: "+vm.getPath()); //$NON-NLS-1$
								javaCommand = XML.stripExtension(javaFile.getPath());
							}
						}
					}
					else {
						File javaFile = OSPRuntime.getJavaFile(bundledVMs[index]);
						if (javaFile!=null) {
							logMessage("using bundled VM: "+bundledVMs[index]); //$NON-NLS-1$
							javaCommand = XML.stripExtension(javaFile.getPath());
						}						
					}
				}
				else {
					logMessage("no bundled java VM, using current VM"); //$NON-NLS-1$
				}
			}


			// preferred executables to run prior to starting Tracker
			if (prefsXMLControl.getPropertyNamesRaw().contains("run")) { //$NON-NLS-1$
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
							logMessage("executable file not found: " + app); //$NON-NLS-1$
						}
					}
					if (runFile != null)
						try {
							logMessage("executing " + runFile.getAbsolutePath()); //$NON-NLS-1$
							ProcessBuilder pb = new ProcessBuilder(runFile.getAbsolutePath());
							pb.directory(new File(trackerHome));
							Process p = pb.start();
							p.waitFor();
						} catch (Exception ex) {
							logMessage("execution failed: " + ex.getClass().getSimpleName() + " " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
						}
				}
			}
			
			// preferred memory size
			preferredMemorySize = 0; // default
			systemProperty = System.getProperty(PREFERRED_MEMORY_SIZE);
			if (systemProperty!=null) {
				loaded = true;
				try {
					preferredMemorySize = Integer.parseInt(systemProperty);
					logMessage("system property "+PREFERRED_MEMORY_SIZE+" = " + systemProperty); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (NumberFormatException e) {
				}
			}
			else if (prefsXMLControl.getPropertyNamesRaw().contains("memory_size")) { //$NON-NLS-1$
				preferredMemorySize = prefsXMLControl.getInt("memory_size"); //$NON-NLS-1$
			}
			if (preferredMemorySize>0) {
				logMessage("preferred memory size: " + preferredMemorySize + " MB"); //$NON-NLS-1$ //$NON-NLS-2$				
			}
			else {
				preferredMemorySize = DEFAULT_MEMORY_SIZE;
				logMessage("using default memory size: " + preferredMemorySize + " MB"); //$NON-NLS-1$ //$NON-NLS-2$				
			}
			

			if (!loaded)
				logMessage("no starter preferences found in " + prefsPath); //$NON-NLS-1$      		
		}
	}
	
	/**
	 * Gets the xuggle jar names for a specified jarpath.
	 * 
	 * @return an array of jar names
	 */
	public static String[] getXuggleJarNames(String jarpath) {		
		xuggleVersionIndex = jarpath == null? INDEX_XUGGLE_57: 
			usesXuggleServer(jarpath)? INDEX_XUGGLE_57: INDEX_XUGGLE_34;
		return XUGGLE_JAR_NAMES[xuggleVersionIndex];
	}



	/**
	 * Gets the preferred tracker jar path.
	 * 
	 * @return the path, or null if none found
	 */
	private static String getTrackerJarPath() throws Exception {
		if (trackerJarPath!=null) {
			return trackerJarPath;
		}
		String jarPath = null;
		/**
		 * @j2sNative
		 */
		{
		String jarHome = OSPRuntime.isMac() ? codeBaseDir.getAbsolutePath()
				: trackerHome;
		if (OSPRuntime.isMac()) {
			logMessage("Mac OSX: looking for tracker jars in " + jarHome); //$NON-NLS-1$
		} else if (OSPRuntime.isWindows()) {
			logMessage("Windows: looking for tracker jars in " + jarHome); //$NON-NLS-1$
		} else {
			logMessage("Linux: looking for tracker jars in " + jarHome); //$NON-NLS-1$
		}
		try {
			File dir = new File(jarHome);
			String[] fileNames = dir.list(trackerJarFilter);
			if (fileNames != null && fileNames.length > 0) {
				String s = "tracker jars found: "; //$NON-NLS-1$
				for (String next : fileNames) {
					s += next + ", "; //$NON-NLS-1$
				}
				logMessage(s.substring(0, s.length() - 2));
				String defaultJar = null;
				String numberedJar = null;
				OSPRuntime.Version newestVersion = null;
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

		    		OSPRuntime.Version v = new OSPRuntime.Version(vers);
						if (v.isValid()) {
							if (versionStr.equals(preferredVersionString)) {
								File file = new File(jarHome, fileNames[i]);
								logMessage("using tracker jar: " + file.getAbsolutePath()); //$NON-NLS-1$
								return file.getAbsolutePath();
							}
							if (newestVersion==null || newestVersion.compareTo(v)<0) {
								newestVersion = v;
								numberedJar = fileNames[i];
							}
						}
					} catch (Exception ex) {
					}
				}
				jarPath = defaultJar != null ? defaultJar : numberedJar;
			}
		} catch (Exception ex) { // if file access fails, try unnumbered tracker.jar
			exceptions += ex.getClass().getSimpleName()
					+ ": " + ex.getMessage() + newline; //$NON-NLS-1$
			logMessage(ex.toString());
			jarPath = "tracker.jar"; //$NON-NLS-1$
		}
		if (jarPath != null) {
			// look in jarHome
			File file = new File(jarHome, jarPath);
			if (file.exists()) {
				String path = XML.forwardSlash(file.getAbsolutePath());
				logMessage("using tracker jar: " + path); //$NON-NLS-1$
				return path;
			}
		}
		throw new NullPointerException("No Tracker jar files found in " + jarHome); //$NON-NLS-1$
		}
	}
	
	/**
	 * Copies Xuggle jar files to a target directory. Does nothing and returns true 
	 * if the target files exist and are the same size.
	 *
	 * @param targetDir the directory
	 * @param xuggleDir the Xuggle directory containing source jar files
	 * @return true if jars are copied
	 */
	public static boolean copyXuggleJarsTo(String targetDir, String xuggleDir) {
		if (xuggleDir == null || targetDir == null) {
			return false;
		}
		File xuggleJarDir = new File(xuggleDir); //$NON-NLS-1$
		xuggleVersionIndex = xuggleDir.contains("share/java/jars")? INDEX_XUGGLE_34: INDEX_XUGGLE_57;
		File[] xuggleJars = xuggleJarDir.listFiles(xuggleFileFilter);
		boolean upToDate = true;
		String[] xuggleNames = XUGGLE_JAR_NAMES[xuggleVersionIndex];
		// if more than one with same (root) xuggleJarName, choose most recent
		for (int i = 0; i < xuggleNames.length; i++) {
			File xuggleFile = null;
			long modified = 0;
			for (int j = 0; j < xuggleJars.length; j++) {
				if (!xuggleJars[j].getName().startsWith(xuggleNames[i]))
					continue;
				if (xuggleJars[j].lastModified() > modified) {
					xuggleFile = xuggleJars[j];
					modified = xuggleFile.lastModified();
				}
			}
			if (xuggleFile != null) {
				File target = new File(targetDir, xuggleNames[i] + ".jar");
				// copy jar
				if (!target.exists() || target.lastModified() < modified) {
					upToDate = ResourceLoader.copyFile(xuggleFile, target, 100000) && upToDate;
				}				
			}
			
		}
		return upToDate;
	}



	/**
	 * Launches the specified tracker jar with a list of arguments
	 * 
	 * @param jarPath the path to the tracker jar
	 * @param args the arguments (may be null)
	 */
	private static void startTracker(String jarPath, String[] args)
			throws Exception {

		String newVersionURL = System.getProperty(TRACKER_NEW_VERSION);

//		// if new version has been installed, run in bundled JRE
//		if (newVersionURL!=null &&  bundledVM != null) {
//			File javaFile = OSPRuntime.getJavaFile(bundledVM);
//			if (javaFile!=null) {
//				javaCommand = XML.stripExtension(javaFile.getPath());
//			} 			
//		}
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
		// code below not functional for dock name in newer MacOS
		// but may still work for menu listings
		if (OSPRuntime.isMac()) {
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
//		String portVar = "TRACKER_PORT"; //$NON-NLS-1$
//		env.put(portVar, String.valueOf(port));
//		if (logText.indexOf(portVar)==-1) {
//			showDebugMessage("setting environment variable "+portVar+" = " + String.valueOf(port)); //$NON-NLS-1$ //$NON-NLS-2$
//		}
		
		if (memorySize<preferredMemorySize) {
			env.put("MEMORY_SIZE", String.valueOf(memorySize)); //$NON-NLS-1$
			logMessage("setting environment variable MEMORY_SIZE = " + String.valueOf(memorySize)); //$NON-NLS-1$ 
		}
		else env.remove("MEMORY_SIZE"); //$NON-NLS-1$ 

		// remove xuggle and ffmpeg warnings that may have been set by previous versions of TrackerStarter
		env.remove("XUGGLE_WARNING"); //$NON-NLS-1$ 
		env.remove("FFMPEG_WARNING"); //$NON-NLS-1$ 
		if (starterWarning!=null) {
			env.put("STARTER_WARNING", starterWarning); //$NON-NLS-1$ 
		}
		else env.remove("STARTER_WARNING"); //$NON-NLS-1$ 
		
		// add TRACKER_HOME, XUGGLE_HOME and PATH to environment
		if (trackerHome!=null) { 
			env.put("TRACKER_HOME", trackerHome); //$NON-NLS-1$ 
			logMessage("setting TRACKER_HOME = " + trackerHome); //$NON-NLS-1$
		}
		if (xuggleHome!=null) {
			env.put("XUGGLE_HOME", xuggleHome); //$NON-NLS-1$ 
			logMessage("setting XUGGLE_HOME = " + xuggleHome); //$NON-NLS-1$
			
//			// set XUGGLE_SERVER if exists
//			if (xuggleServerJar.exists()) {
//				env.put("XUGGLE_SERVER", "true"); //$NON-NLS-1$ 
//				logMessage("setting XUGGLE_SERVER = true"); //$NON-NLS-1$
//			} 
			
			// set path, etc, only if xuggle-xuggler.jar (ver 3.4) is present?
//			if (xuggleJar.exists() && new File(xuggleHome).exists()) {
//
//				String pathEnvironment = OSPRuntime.isWindows()? "Path":  //$NON-NLS-1$
//					OSPRuntime.isMac()? "DYLD_LIBRARY_PATH": "LD_LIBRARY_PATH"; //$NON-NLS-1$ //$NON-NLS-2$
//				
//				// get current PATH
//				String pathValue = env.get(pathEnvironment);
//				if (pathValue==null) pathValue = ""; //$NON-NLS-1$
//				
//				// add xuggle path at beginning of current PATH
//				if (!pathValue.startsWith(xuggleHome)) {
//					pathValue = xuggleHome+File.pathSeparator+pathValue;
//				}
//				
//				env.put(pathEnvironment, pathValue);
//				logMessage("added to "+pathEnvironment+": " + xuggleHome); //$NON-NLS-1$ //$NON-NLS-2$
//			}

			String subdir = OSPRuntime.isWindows()? "bin": "lib"; //$NON-NLS-1$ //$NON-NLS-2$
			if (xuggleJar.exists() && new File(xuggleHome, subdir).exists()) {
	
				String pathEnvironment = OSPRuntime.isWindows()? "Path":  //$NON-NLS-1$
					OSPRuntime.isMac()? "DYLD_LIBRARY_PATH": "LD_LIBRARY_PATH"; //$NON-NLS-1$ //$NON-NLS-2$
				
				String xugglePath = xuggleHome+File.separator+subdir;
				// get current PATH
				String pathValue = env.get(pathEnvironment);
				if (pathValue==null) pathValue = ""; //$NON-NLS-1$
				
				// add xuggle path at beginning of current PATH
				if (!pathValue.startsWith(xugglePath)) {
					pathValue = xugglePath+File.pathSeparator+pathValue;
				}
				
				env.put(pathEnvironment, pathValue);
				logMessage("adding to "+pathEnvironment+": " + xugglePath); //$NON-NLS-1$ //$NON-NLS-2$		
			}
		}
		
//		if (ffmpegHome!=null && new File(ffmpegHome).exists()) {
//			env.put("FFMPEG_HOME", ffmpegHome); //$NON-NLS-1$ 
//			logMessage("setting FFMPEG_HOME = " + ffmpegHome); //$NON-NLS-1$
//		}
		
		// add TRACKER_RELAUNCH to process environment if relaunching
		if (relaunching) {
			env.put(TRACKER_RELAUNCH, "true"); //$NON-NLS-1$
		}
		else env.remove(TRACKER_RELAUNCH);
		
		// add NEW_INSTALL to environment if isNewInstall
		if (isNewInstall) {
			env.put(NEW_INSTALL, "true"); //$NON-NLS-1$
		}
		else env.remove(NEW_INSTALL);
		
		// add TRACKER_NEW_VERSION to process environment if launching a new version
		if (newVersionURL!=null) {
			logMessage("setting "+TRACKER_NEW_VERSION+" = " + newVersionURL); //$NON-NLS-1$ //$NON-NLS-2$ 
			env.put(TRACKER_NEW_VERSION, newVersionURL);
		}
		else env.remove(TRACKER_NEW_VERSION);
		
		// assemble command message for log
		String message = ""; //$NON-NLS-1$
		for (String next: cmd) {
			message += next + " "; //$NON-NLS-1$
		}
		logMessage("executing command: " + message); //$NON-NLS-1$ 

		// write codeBase tracker_start log
		writeCodeBaseLog(LOG_FILE_NAME);

		// write the user tracker_start log and set log environment variables
		String prevLogText = System.getenv("START_LOG_TEXT"); //$NON-NLS-1$
		if (prevLogText != null)
			logText = prevLogText + "\n" + logText;
		env.put("START_LOG_TEXT", logText); //$NON-NLS-1$
		startLogPath = writeUserLog();
		if (startLogPath!=null)
			env.put("START_LOG", startLogPath); //$NON-NLS-1$
		
		// start exit thread that waits 2 seconds before exiting 
		// to give time to start the new process
		exitCounter = 0;
		if (exitThread==null) {
			exitThread = new Thread(new Runnable() {
				@Override
				public void run() {
					abortExit = false;
					while (exitCounter<20) {
						try {
							if (abortExit) return;
							Thread.sleep(100);
							exitCounter++;
						} catch (InterruptedException e) {
						}
					}					
					OSPRuntime.exit();
					System.exit(0);
				}
			});
			exitThread.setDaemon(true);
			exitThread.start();
		}
		
		// start the new Tracker process and wait for it to finish
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
			
	    logMessage("failed to start with memory size "+memorySize+"MB due to the following errors:"+errors); //$NON-NLS-1$ //$NON-NLS-2$
    	
	    // if process failed due to excessive memory size, reduce size and try again
	    if (errors.indexOf("heap")>-1) { //$NON-NLS-1$
    		memorySize *= 0.95;
    		if (memorySize<64) {
    			exceptions += errors + newline;
    			exitGracefully(jarPath);
    		}
    		logMessage("try to start with smaller memory size "+memorySize+"MB"); //$NON-NLS-1$ //$NON-NLS-2$
    		startTracker(jarPath, args);
    	}
	    else {
  			exceptions += errors + newline;
  			exitGracefully(jarPath);
	    }
		}
		else {
			// should never get here--exits via timer
			OSPRuntime.exit();
			System.exit(0);
		}
	}

	private static String writeUserLog() {
		if ("".equals(logText) || trackerHome==null) { //$NON-NLS-1$
			return null;
		}	

		// define "user log" file
		File file = null;
		// check if can write to OSP.prefs directory
		File ospPrefsFile = OSPRuntime.getPreferencesFile();
		if (ospPrefsFile != null && ospPrefsFile.getParentFile().canWrite()) {
			file = new File(ospPrefsFile.getParentFile(), LOG_FILE_NAME);
		}
		// check if can write to Tracker file in user documents
		if (file == null && userDocuments != null && new File(userDocuments+"/Tracker").canWrite()) { //$NON-NLS-1$
			file = new File(userDocuments+"/Tracker", LOG_FILE_NAME); //$NON-NLS-1$
		}		
		// check if can write to tracker home
		if (file==null && new File(trackerHome).canWrite()) {
			file = new File(trackerHome, LOG_FILE_NAME);
		}
		
		if (file==null) return null;
		
		addLogHeader();
		logMessage("writing user start log to "+file.getAbsolutePath()); //$NON-NLS-1$

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
			
//			// use code below for testing
//			Properties props = System.getProperties();
//			props.forEach((k, v) -> logMessage(k+": "+v));			
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  MMM dd yyyy"); //$NON-NLS-1$
			Calendar cal = Calendar.getInstance();
			logText = "TrackerStarter version " + OSPRuntime.VERSION + "  " //$NON-NLS-1$ //$NON-NLS-2$
					+ sdf.format(cal.getTime()) + newline + newline + logText;
		}		
	}

	private static void writeCodeBaseLog(String fileName) {
		// writes log file to codeBaseDir 
		if (codeBaseDir!=null && codeBaseDir.canWrite()) {
			addLogHeader();
			File file = new File(codeBaseDir, fileName);
			logMessage("writing code base start log "+file.getAbsolutePath()); //$NON-NLS-1$
			try {
				FileOutputStream stream = new FileOutputStream(file);
				Charset charset = Charset.forName(encoding);
				OutputStreamWriter out = new OutputStreamWriter(stream, charset);
				BufferedWriter writer = new BufferedWriter(out);
				writer.write(logText);
				writer.flush();
				writer.close();
			} catch (IOException ex) {
				logMessage("exception writing code base start log (access denied?)"); //$NON-NLS-1$
			}
		}
		else {
			logMessage("unable to write code base start log"); //$NON-NLS-1$
		}
	}
	
	public static boolean usesXuggleServer(String jarpath) {
		jarpath = XML.forwardSlash(jarpath);
		Boolean b = usesXuggleServer.get(jarpath);
		if (b != null)
			return b;
		boolean usesServer = false;
		try {
			JarFile jarfile = new JarFile(jarpath);
			String classpath = OSPRuntime.getManifestAttribute(jarfile, "Class-Path");
			usesServer = classpath.contains("-server-");
			usesXuggleServer.put(jarpath, usesServer);
		} catch (Exception ex) {
			// ex.printStackTrace();
//			OSPLog.warning(ex.getMessage());
		}
		return usesServer;
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

	public static void logMessage(String message) {
		if (log) {
			logText += " - " + message + newline; //$NON-NLS-1$
		}
		if (debug) {
			System.out.println(message);
		}
	}

}
