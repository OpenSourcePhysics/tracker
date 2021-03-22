/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert Hanson
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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.ConsoleLevel;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.mov.MovieFactory;
import org.opensourcephysics.tools.DataFunctionPanel;
import org.opensourcephysics.tools.Diagnostics;
import org.opensourcephysics.tools.DiagnosticsForThreads;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionEditor;
import org.opensourcephysics.tools.FunctionPanel;
import org.opensourcephysics.tools.JREFinder;
import org.opensourcephysics.tools.LaunchNode;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import javajs.async.Assets;
import javajs.async.AsyncSwingWorker;
import javajs.async.SwingJSUtils.Performance;
import swingjs.api.JSUtilI;

/**
 * This is the default Tracker application.
 *
 * @author Douglas Brown
 */
public class Tracker {

	// This static call to OSPRuntime Ensure that OSP is loaded already, 
	// enabling all resources and setting J2S parameters
	// such as allowed AJAX databases.

	public static final String TRACKER_TEST_URL = "https://physlets.org/tracker/counter/counter.php";
	
	public static boolean doHoldRepaint = true; //BH testing if false

	public static boolean allowDataFunctionControls = !OSPRuntime.isJS;
	
	public static boolean allowTableRefresh = true;
	public static boolean allowPlotRefresh = true; //this was the killer -- Firefox with ctx.save/restore
	public static boolean allowDataRefresh = true;
	public static boolean allowViews = true;
	public static boolean allowMenuRefresh = true;
	public static boolean allowToolbarRefresh = true;
	
	public static boolean loadTabsInSeparateThread = !OSPRuntime.isJS;

	static {
		XML.setLoader(Preferences.class, new Preferences.Loader());
	}

	public static JSUtilI jsutil;

	static {
		try {
			if (OSPRuntime.isJS) {
				OSPRuntime.launcherAllowEJSModel = false;
				jsutil = ((JSUtilI) Class.forName("swingjs.JSUtil").newInstance());
			}
		} catch (Exception e) {
			OSPLog.warning("OSPRuntime could not create jsutil");
		}
	}

	static {
		//Assets.setDebugging(true);
		// Option 1: if we set this to "osp", then osp-assets.zip will never be used
		// otherwise, as it is now, osp-assets will be used for all org/opensourcephysics/resources assets
		OSPRuntime.addAssets("tracker", "tracker-assets.zip", "org/opensourcephysics");
		// Option 2: adds "cabrillo" to only load tracker assets from tracker-assets.zip and still use osp-assets.zip for others
		// OSPRuntime.addAssets("tracker", "tracker-assets.zip", "org/opensourcephysics/cabrillo");
		
	}

	// define static constants
	/** tracker version and copyright */
	// 3/09/21: abandon Tracker.VERSION for OSPRuntime.VERSION for smaller tracker_starter.jar
//	public static final String VERSION = "5.9.20210307"; //$NON-NLS-1$
	public static final String COPYRIGHT = "Copyright (c) 2021 D Brown, W Christian, R Hanson"; //$NON-NLS-1$
	
	/**
	 * Gets an icon from a class resource image.
	 * 
	 * @param imageName the name of the image, with no path
	 * @param resizable true to return a ResizableIcon, otherwise returns ImageIcon
	 */
	public static Icon getResourceIcon(String imageName, boolean resizable) {
		URL url = getClassResource("resources/images/" + imageName);
		if (url == null)  {
			OSPLog.debug("Tracker.getResourceIcon was null for " + imageName);
			return null;
		}
		return (resizable ? new ResizableIcon(url) : new ImageIcon(url));
	}


	/** the tracker icon */
	public static final ImageIcon TRACKER_ICON = (ImageIcon) getResourceIcon("tracker_icon_32.png", false); //$NON-NLS-1$
	/** a larger tracker icon */
	public static final ImageIcon TRACKER_ICON_256 = (ImageIcon) getResourceIcon("tracker_icon_256.png", false); //$NON-NLS-1$

	static final String THETA = TeXParser.parseTeX("$\\theta"); //$NON-NLS-1$
	static final String OMEGA = TeXParser.parseTeX("$\\omega"); //$NON-NLS-1$
	static final String ALPHA = TeXParser.parseTeX("$\\alpha"); //$NON-NLS-1$
	static final String DEGREES = "\u00B0"; //$NON-NLS-1$
	static final String SQUARED = "\u00b2"; //$NON-NLS-1$
	static final String DOT = "\u00b7"; //$NON-NLS-1$
	static final Level DEFAULT_LOG_LEVEL = ConsoleLevel.OUT_CONSOLE;
	static final int DEFAULT_TRAIL_LENGTH_INDEX = 2;

	// for testing
	public static boolean timeLogEnabled = false;
	static boolean testOn = false;
	static String testString;

	// define static fields
	static String trackerHome;
	static String[] fullConfig = { "file.new", "file.open", "file.close", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"file.import", "file.export", "file.save", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"file.saveAs", "file.print", "file.library", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"edit.copyObject", "edit.copyData", "edit.copyImage", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"edit.paste", "edit.matSize", //$NON-NLS-1$ //$NON-NLS-2$
			"edit.clear", "edit.undoRedo", "video.import", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"video.close", "video.visible", "video.filters", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"pageView.edit", "notes.edit", "new.pointMass", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"new.cm", "new.vector", "new.vectorSum", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"new.lineProfile", "new.RGBRegion", //$NON-NLS-1$ //$NON-NLS-2$
			"new.analyticParticle", "new.clone", "new.circleFitter", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"new.dynamicParticle", "new.dynamicTwoBody", //$NON-NLS-1$ //$NON-NLS-2$
			"new.dataTrack", "new.tapeMeasure", "new.protractor", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"calibration.stick", "calibration.tape", //$NON-NLS-1$ //$NON-NLS-2$
			"calibration.points", "calibration.offsetOrigin", //$NON-NLS-1$ //$NON-NLS-2$
			"track.name", "track.description", //$NON-NLS-1$ //$NON-NLS-2$
			"track.color", "track.footprint", //$NON-NLS-1$ //$NON-NLS-2$
			"track.visible", "track.locked", //$NON-NLS-1$ //$NON-NLS-2$
			"track.delete", "track.autoAdvance", //$NON-NLS-1$ //$NON-NLS-2$
			"track.markByDefault", "track.autotrack", //$NON-NLS-1$ //$NON-NLS-2$
			"model.stamp", "help.diagnostics", "coords.locked", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"coords.origin", "coords.angle", "data.algorithm", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"coords.scale", "coords.refFrame", "button.x", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"button.v", "button.a", "button.trails", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"button.labels", "button.stretch", "button.clipSettings", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"button.xMass", "button.axes", "button.path", "button.drawing", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"number.formats", "number.units", "text.columns", "plot.compare", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"config.saveWithData", "data.builder", "data.tool" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	static Set<String> defaultConfig;
	static boolean xuggleCopied;
	static String[] mainArgs;
	static JFrame splash;
	public static Icon trackerLogoIcon;
	static JProgressBar progressBar;
	static String counterPath = "https://physlets.org/tracker/counter/counter.php?"; //$NON-NLS-1$
	static String latestVersion; // last version for which user has been informed
	static String newerVersion; // new version available if non-null
	static boolean checkedForNewerVersion = false; // true if checked for new version
	static String trackerWebsite = "physlets.org/tracker"; //$NON-NLS-1$
	static String trackerDownloadFolder = "/upgrade/"; //$NON-NLS-1$
	static String author = "Douglas Brown"; //$NON-NLS-1$
	static String osp = "Open Source Physics"; //$NON-NLS-1$
	static AbstractAction aboutXuggleAction, aboutThreadsAction;
	static Action aboutTrackerAction, readmeAction;
	static Action aboutJavaAction, startLogAction, trackerPrefsAction;
	private static Tracker sharedTracker;
	static String readmeFileName = "tracker_README.txt"; //$NON-NLS-1$
	static JDialog readmeDialog, startLogDialog, trackerPrefsDialog;
	static JTextArea trackerPrefsTextArea;
	static String prefsPath;
	public static String rootXMLPath = ""; // path to root directory of trk files //$NON-NLS-1$
	static Cursor zoomInCursor, zoomOutCursor, grabCursor;
	static boolean showHints = true;
	static boolean startupHintShown;
	static String pdfHelpPath = "/tracker_help.pdf"; //$NON-NLS-1$
	static JButton pdfHelpButton;
	static ArrayList<String> recentFiles = new ArrayList<String>();
	static int minimumMemorySize = 32;
	static int requestedMemorySize = -1, originalMemoryRequest = 0;
	static long lastMillisChecked;
	static int maxFontLevel = 6;
	private static Locale[] locales;
	static Object[][] incompleteLocales;
	static Locale defaultLocale;
	static ArrayList<String> checkForUpgradeChoices;
	static Map<String, Integer> checkForUpgradeIntervals;

	static Collection<String> initialAutoloadSearchPaths = new TreeSet<String>();

	static java.io.FileFilter xmlFilter;

	// user-settable preferences saved/loaded by Preferences class
	static Level preferredLogLevel = DEFAULT_LOG_LEVEL;
	static boolean showHintsByDefault = true;
	static int recentFilesSize = 6;
	static int preferredMemorySize = -1;
	static String lookAndFeel, preferredLocale, preferredDecimalSeparator;
	static String preferredJRE, preferredTrackerJar, preferredPointMassFootprint;
	static int checkForUpgradeInterval = 0;
	static int preferredFontLevel = 0, preferredFontLevelPlus = 0;
	static boolean isRadians, isXuggleFast;
	static boolean warnXuggleError = true;
	static boolean warnNoVideoEngine = !OSPRuntime.isJS;
	static boolean warnVariableDuration = true;
	static String[] prelaunchExecutables = new String[0];
	static Map<String, String[]> autoloadMap = new TreeMap<String, String[]>();
	static String[] preferredAutoloadSearchPaths;
	static boolean markAtCurrentFrame = true;
	static boolean scrubMouseWheel, centerCalibrationStick = true, hideLabels;
	static boolean enableAutofill = true, showGaps = true;
	static int preferredTrailLengthIndex = DEFAULT_TRAIL_LENGTH_INDEX;

	private static boolean declareLocales = true;//!OSPRuntime.isJS;

	// the only instance field!
	private TFrame frame;

	private static void initClass() {
		if (defaultLocale != null)
			return;
//  	OSPLog.setLevel(ConsoleLevel.ALL);
//  	OSPLog.showLog();
		defaultLocale = Locale.getDefault();
		trackerHome = System.getenv("TRACKER_HOME"); //$NON-NLS-1$
		if (trackerHome == null) {
//			
//			try {
			trackerHome = TrackerStarter.findTrackerHome(false);
//			} catch (Exception e1) {
//			}
		}
		// set system properties for Mac OSX look and feel
//    System.setProperty("apple.laf.useScreenMenuBar", "true");
//    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tracker");

		// get logo icon for splash screen--no need for resizable
		trackerLogoIcon = getResourceIcon("tracker_logo.png", false);

		// create grab cursor
		Image grab = ((ImageIcon) getResourceIcon("grab.gif", false)).getImage();
		grabCursor = GUIUtils.createCustomCursor(grab, new Point(14, 10), "Grab", Cursor.HAND_CURSOR); //$NON-NLS-1$

		// create static objects AFTER they are defined above

		if (!declareLocales) {
			// old SwingJS?
			locales = new Locale[] { Locale.ENGLISH };
			incompleteLocales = new Object[][] {};
		}

		setDefaultConfig(getFullConfig());
		loadPreferences();
		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			// load current version after a delay to allow video engines to load
			// and every 24 hours thereafter (if program is left running)
			Timer timer = new Timer(86400000, (e) -> {
				Thread opener = new Thread(() -> {
					checkedForNewerVersion = false;
					loadCurrentVersion(false, true, true);
				});
				opener.setPriority(Thread.NORM_PRIORITY);
				opener.setDaemon(true);
				opener.start();
			});
			timer.setInitialDelay(10000);
			timer.setRepeats(true);
			timer.start();
		}
		xmlFilter = new java.io.FileFilter() {
			// accept only *.xml files.
			@Override
			public boolean accept(File f) {
				if (f == null || f.isDirectory())
					return false;
				String ext = XML.getExtension(f.getName());
				if (ext != null && "xml".equals(ext.toLowerCase())) //$NON-NLS-1$
					return true;
				return false;
			}
		};

		checkForUpgradeChoices = new ArrayList<String>();
		checkForUpgradeIntervals = new HashMap<String, Integer>();

		if (!OSPRuntime.isJS) /** @j2sNative */
		{

			autoloadDataFunctions();

			// check for upgrade intervals
			String s = "PrefsDialog.Upgrades.Always"; //$NON-NLS-1$
			checkForUpgradeChoices.add(s);
			checkForUpgradeIntervals.put(s, 0);
			s = "PrefsDialog.Upgrades.Weekly"; //$NON-NLS-1$
			checkForUpgradeChoices.add(s);
			checkForUpgradeIntervals.put(s, 7);
			s = "PrefsDialog.Upgrades.Monthly"; //$NON-NLS-1$
			checkForUpgradeChoices.add(s);
			checkForUpgradeIntervals.put(s, 30);
			s = "PrefsDialog.Upgrades.Never"; //$NON-NLS-1$
			checkForUpgradeChoices.add(s);
			checkForUpgradeIntervals.put(s, 10000);

		}

		// create splash frame
		Color darkred = new Color(153, 0, 0);
		Color darkblue = new Color(51, 51, 102);
		Color grayblue = new Color(116, 147, 179);
		Color darkgrayblue = new Color(83, 105, 128);
		Color lightblue = new Color(169, 193, 217);
		Color background = new Color(250, 250, 230);
		splash = new JFrame("Tracker"); //$NON-NLS-1$ // name shown on task bar
		splash.setIconImage(TRACKER_ICON.getImage()); // icon shown on task bar
		splash.setUndecorated(true);
		splash.setAlwaysOnTop(true);
		splash.setResizable(false);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(background);
		contentPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, grayblue, darkgrayblue));
		splash.setContentPane(contentPane);
		MouseInputAdapter splashMouseListener = new MouseInputAdapter() {
			Point mouseLoc;
			Point splashLoc;

			@Override
			public void mousePressed(MouseEvent e) {
				splashLoc = splash.getLocation(); // original screen position of splash
				mouseLoc = e.getPoint(); // original screen position of mouse
				mouseLoc.x += splashLoc.x;
				mouseLoc.y += splashLoc.y;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Point loc = splash.getLocation();
				loc.x += e.getPoint().x;
				loc.y += e.getPoint().y;
				splash.setLocation(splashLoc.x + loc.x - mouseLoc.x, splashLoc.y + loc.y - mouseLoc.y);
			}
		};
		contentPane.addMouseListener(splashMouseListener);
		contentPane.addMouseMotionListener(splashMouseListener);

		// tracker logo north
		JLabel trackerLogoLabel = new JLabel(trackerLogoIcon);
		trackerLogoLabel.setBorder(BorderFactory.createEmptyBorder(12, 24, 4, 24));
		contentPane.add(trackerLogoLabel, BorderLayout.NORTH);

		// tip of the day and progress bar in the center
		String tip = TrackerRes.getString("Tracker.Splash.HelpMessage"); //$NON-NLS-1$
		tip += " " + TrackerRes.getString("TMenuBar.Menu.Help"); //$NON-NLS-1$ //$NON-NLS-2$
		tip += "|" + TrackerRes.getString("TMenuBar.MenuItem.GettingStarted"); //$NON-NLS-1$ //$NON-NLS-2$
		JLabel helpLabel = new JLabel(tip);
		helpLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
		Font font = helpLabel.getFont().deriveFont(Font.PLAIN).deriveFont(14f);
		helpLabel.setFont(font);
		helpLabel.setForeground(darkred);
		helpLabel.setAlignmentX(0.5f);
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.setBorder(BorderFactory.createEmptyBorder(12, 50, 16, 50));
		progressPanel.add(progressBar, BorderLayout.CENTER);
		progressPanel.setOpaque(false);
		Box center = Box.createVerticalBox();
		center.add(helpLabel);
		center.add(progressPanel);
//		contentPane.add(center, BorderLayout.CENTER);

		// version south
//		String vers = author + "   " + osp + "   Ver " + OSPRuntime.VERSION; //$NON-NLS-1$ //$NON-NLS-2$
		String vers = "Ver " + OSPRuntime.VERSION; //$NON-NLS-1$ //$NON-NLS-2$
		if (OSPRuntime.VERSION.length() > 7 || testOn)
			vers += " BETA"; //$NON-NLS-1$
		JLabel versionLabel = new JLabel(vers);
		versionLabel.setForeground(darkblue);
		font = font.deriveFont(Font.BOLD).deriveFont(10f);
		versionLabel.setFont(font);
		versionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		versionLabel.setOpaque(false);
		versionLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
		JPanel versionPanel = new JPanel(new BorderLayout());
		versionPanel.setBackground(new Color(212, 230, 247));
		versionPanel.add(versionLabel, BorderLayout.CENTER);
		versionPanel.setBorder(BorderFactory.createLineBorder(lightblue));
		contentPane.add(versionPanel, BorderLayout.SOUTH);

//		splash.pack();
//		Dimension size = splash.getSize();
//		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//		int x = dim.width / 2;
//		int y = 3 * dim.height / 5; // below center
//		splash.setLocation(x - size.width / 2, y - size.height / 2);

		VideoIO.setDefaultXMLExtension("trk"); //$NON-NLS-1$

		// create pdf help button
		pdfHelpButton = new JButton(TrackerRes.getString("Tracker.Button.PDFHelp")); //$NON-NLS-1$
		pdfHelpButton.addActionListener((e) -> {
			try {
				java.net.URL url = new java.net.URL("https://" + trackerWebsite + pdfHelpPath); //$NON-NLS-1$
				org.opensourcephysics.desktop.OSPDesktop.displayURL(url.toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		// find Java VMs in background thread so they are ready when needed
		new Thread(() -> {
			JREFinder.getFinder().getJREs(32);
		}).start();
	}

	public static Locale[] getLocales() {
		if (locales != null)
			return locales;
		locales = new Locale[] { Locale.ENGLISH, // SwingJS -- only this one is declared statically
				new Locale("ar"), // arabic //$NON-NLS-1$
				new Locale("cs"), // czech //$NON-NLS-1$
				new Locale("da"), // danish //$NON-NLS-1$
				new Locale("de"), // Locale.GERMAN,
				new Locale("el", "GR"), // greek //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("es"), // spanish //$NON-NLS-1$
				new Locale("fi"), // finnish //$NON-NLS-1$
				new Locale("fr"), // Locale.FRENCH,
				new Locale("hu", "HU"), // hungarian //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("in"), // indonesian //$NON-NLS-1$
				new Locale("it"), // Locale.ITALIAN,
				new Locale("iw", "IL"), // hebrew //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("ko"), // korean //$NON-NLS-1$
				new Locale("ms", "MY"), // malaysian //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("nl", "NL"), // dutch //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("pl"), // polish //$NON-NLS-1$
				new Locale("pt", "BR"), // Brazil portuguese //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("pt", "PT"), // Portugal portuguese //$NON-NLS-1$
				// BH missing PORTUGUESE?
//		OSPRuntime.PORTUGUESE,
				new Locale("sk"), // slovak //$NON-NLS-1$
				new Locale("sl"), // slovenian //$NON-NLS-1$
				new Locale("sv"), // swedish //$NON-NLS-1$
				new Locale("th", "TH"), // Thailand thai //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("tr"), // turkish //$NON-NLS-1$
//		new Locale("uk"), // ukrainian //$NON-NLS-1$
				new Locale("vi", "VN"), // vietnamese //$NON-NLS-1$ //$NON-NLS-2$
				new Locale("zh", "CN"), // Locale.CHINA, // simplified chinese
				new Locale("zh", "TW"), // Locale.TAIWAN // traditional chinese
		};
		// pig last updated March 2018
		incompleteLocales = new Object[][] { { new Locale("cs"), "2013" }, // czech //$NON-NLS-1$ //$NON-NLS-2$
				{ new Locale("fi"), "2013" }, // finnish //$NON-NLS-1$ //$NON-NLS-2$
				{ new Locale("sk"), "2011" }, // slovak //$NON-NLS-1$ //$NON-NLS-2$
				{ new Locale("in"), "2013" } };// indonesian //$NON-NLS-1$ //$NON-NLS-2$

		return locales;
	}

	/**
	 * If JavaScript, look in an asset zip file; if not, use Tracker.class.getResource() if not.
	 * 
	 * @param resource "resources/...."
	 * @return URL (with byte[ ] in _streamData if OSPRuntime.isJS)
	 */
	public static URL getClassResource(String resource) {
		return ResourceLoader.getClassResource("org/opensourcephysics/cabrillo/tracker/" + resource, Tracker.class);
	}

	/**
	 * Gets the shared Tracker for single-VM use.
	 * 
	 * @param whenLoaded
	 *
	 * @return the shared Tracker
	 */
	public static Tracker getTracker(Runnable whenLoaded) {
		if (sharedTracker == null) {
			OSPLog.fine("creating shared Tracker"); //$NON-NLS-1$
			sharedTracker = new Tracker(null, false, false, whenLoaded);
		}
		return sharedTracker;
	}

	/**
	 * Constructs Tracker with a blank tab and splash.
	 */
	public Tracker() {
		this(null, true, true, null);
	}

	/**
	 * Constructs Tracker with a video.
	 * 
	 * @param video the video
	 */
	public Tracker(Video video) {
		Map<String, Object> options = new HashMap<>();
		options.put("-video", video);
		createFrame(options);
	}

	/**
	 * Constructs Tracker and loads the named TRK or TRZ files.
	 *
	 * @param args       -bounds x y w h, -dim w h, list of TRK, video or TRZ file
	 *                   names
	 * @param whenLoaded
	 */
	private Tracker(String[] args, boolean addTabIfEmpty, boolean showSplash, Runnable whenLoaded) {

		// BH SwingJS This next call was originally a part of a static { } block.
		// but that does not work in JavaScript, because when the class is first loaded
		// there is no JSAppletViewer or JSDummyApplet "top" level object set yet.
		// We run initClass once, based on loading the default locale once (which is
		// always non null).
		initClass();
//		helper = new StateHelper(this);
//		helper.next(STATE_INIT);

		if (showSplash && !OSPRuntime.isJS) {
			// set font level resize and center splash frame
			FontSizer.setFonts(splash);
			splash.pack();
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - splash.getBounds().width) / 2;
			int y = (dim.height - splash.getBounds().height) / 2;
			splash.setLocation(x, y);
		}
		splash.setVisible(showSplash && !OSPRuntime.isJS);

		createFrame(TFrame.parseArgs(args));
		setProgress(5);
		if (args != null) {
			// parse file names
			for (int i = 0; i < args.length; i++) {
				if (args[i] == null)
					continue;
				// set default root path to path of first .trk or .trz file opened
				if ((args[i].endsWith(".trk") || args[i].endsWith(".trz")) //$NON-NLS-1$ //$NON-NLS-2$
						&& args[i].indexOf("/") != -1 //$NON-NLS-1$
						&& rootXMLPath.equals("")) { //$NON-NLS-1$
					rootXMLPath = args[i].substring(0, args[i].lastIndexOf("/") + 1); //$NON-NLS-1$
					OSPLog.fine("Setting rootPath: " + rootXMLPath); //$NON-NLS-1$
				}
				frame.doOpenURL(args[i]);
				addTabIfEmpty = false;
			}
		}
		if (addTabIfEmpty) {
			// add an empty tab if requested
			TrackerPanel trackerPanel = frame.getCleanTrackerPanel();
			frame.addTab(trackerPanel, TFrame.ADD_NOSELECT | TFrame.ADD_REFRESH, () -> {
				if (showHints) {
					startupHintShown = true;
					trackerPanel.setMessage(TrackerRes.getString("Tracker.Startup.Hint")); //$NON-NLS-1$
				}
				setProgress(100);
			});
		}
	}
	
	/**
	 * Replace any open tabs with a single tab loaded with the given path.
	 * JavaScript only?
	 * 
	 * @j2sAlias loadExperimentURL
	 * 
	 * @param path
	 * @author Bob Hanson
	 */
	public void loadExperimentURL(String path) {
		getFrame().loadExperimentURL(path);
	}

	/**
	 * Gets the frame with alias for JavaScript
	 *
	 * @j2sAlias getFrame
	 *
	 * @return the frame
	 */
	public TFrame getFrame() {
		return frame;
	}
	
  /**
   * OSP API to get the main program frame.
   * 
   *  @j2sAlias getMainFrame
   * 
   * @return OSPFrame
   */
  public OSPFrame getMainFrame() {
    return frame;
  }
  
  /**
   * OSP API to get the main Tracker frame size. 
   * 
   * @j2sAlias getMainFrameSize
   * 
   */
  public int[] getMainFrameSize(){
 	 Dimension d=frame.getSize();
 	 return new int[] {d.width,d.height};
  }
  
  /**
   * OSP API to get the main Tracker frame location. 
   * 
   * @j2sAlias getMainFrameLocation
   * 
   */
  public int[] getMainFrameLocation(){
 	 Point d=frame.getLocation();
 	 return new int[] {d.x,d.y};
  }



	/**
	 * Creates the TFrame.
	 */
	private void createFrame(Map<String, Object> options) {
		// create actions
		createActions();
		OSPRuntime.setLookAndFeel(true, lookAndFeel);
		frame = new TFrame(options);
		Diagnostics.setDialogOwner(frame);
		// set up the Java VM exit mechanism when used as application
		if (!OSPRuntime.isApplet) {
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					onWindowClosing();
				}
			});
		}
	}

//________________________________  static methods ____________________________

	protected void onWindowClosing() {

		if (OSPRuntime.isJS) {
			exit();
			return; // Necessary for SwingJS
		}

		// save preferences, but first clean up autoloadMap
		ArrayList<String> dirs = new ArrayList<String>();
		if (preferredAutoloadSearchPaths != null) {
			for (String path : preferredAutoloadSearchPaths)
				dirs.add(path);
		} else
			dirs.addAll(getDefaultAutoloadSearchPaths());

		for (Iterator<String> it = autoloadMap.keySet().iterator(); it.hasNext();) {
			String filePath = it.next();
			String parentPath = XML.getDirectoryPath(filePath);
			boolean keep = false;
			for (String dir : dirs) {
				keep = keep || parentPath.equals(dir);
			}
			if (!keep || !new File(filePath).exists()) {
				it.remove();
			}
		}
		savePreferences();

		boolean doClose = (frame.wishesToExit()
				&& frame.getDefaultCloseOperation() == WindowConstants.DISPOSE_ON_CLOSE);

		if (frame.libraryBrowser != null) {
			boolean canceled = !frame.libraryBrowser.exit();
			if (canceled) {
				// exiting is canceled so temporarily change close operation
				// to DO_NOTHING and return
				final int op = frame.getDefaultCloseOperation();
				final boolean exit = frame.wishesToExit();
				frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				EventQueue.invokeLater(() -> {
					if (exit)
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setDefaultCloseOperation(op);
				});
				return;
			}
		}

		// exit the system if frame wishes to exit
		if (doClose) {
			exit();
		} else {
			// use else here for SwingJS.
			// remove the tabs but don't close if canceled
			frame.saveAllTabs(new Function<TrackerPanel, Void>() {
				// for each approved, remove tab
				@Override
				public Void apply(TrackerPanel trackerPanel) {
					frame.new TabRemover(trackerPanel).execute();
					return null;
				}

			}, null, () -> {
				// if canceled
					// exiting is canceled so temporarily change close operation
					// to DO_NOTHING before the frame finishes closing
					int op = frame.getDefaultCloseOperation();
					boolean exit = frame.wishesToExit();
					frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

					// use AsyncSwingWorker to restore frame visibility and DefaultCloseOperation
					new AsyncSwingWorker(null, null, 2, 0, 1) { // 2 ms delay

						@Override
						public void initAsync() {
						}

						@Override
						public int doInBackgroundAsync(int i) {
							return 1;
						}

						@Override
						public void doneAsync() {
							frame.setVisible(true);
							frame.setDefaultCloseOperation(exit ? JFrame.EXIT_ON_CLOSE : op);
						}

					}.execute();
				// frame will always close now
			});
		}
	}

	public static void exit() {
		OSPRuntime.exit();
		System.exit(0);
	}

	/**
	 * Compares version strings.
	 * 
	 * @param ver1 version 1
	 * @param ver2 version 2
	 * @return 0 if equal, 1 if ver1>ver2, -1 if ver1<ver2
	 */
	public static int compareVersions(String ver1, String ver2) {
		// deal with null values
		if (ver1 == null || ver2 == null) {
			return 0;
		}
		// typical newer semantic version "4.9.10" or 5.0.7.190504 beta or 5.0.7190504
		// beta
		// typical older version "4.97"
		String[] v1 = ver1.trim().split("\\."); //$NON-NLS-1$
		String[] v2 = ver2.trim().split("\\."); //$NON-NLS-1$
		// newer beta version arrays have length 4
		// newer semantic version arrays have length 3
		// older version arrays have length 2
		// older beta arrays may have length 3--truncate last number string to one digit

		// truncate beta versions to length 3
		if (v1.length == 4) {
			v1 = new String[] { v1[0], v1[1], v1[2] };
		}
		if (v2.length == 4) {
			v2 = new String[] { v2[0], v2[1], v2[2] };
		}

		// truncate last number if a long beta number
		if (v1.length == 3 && v1[2].length() > 2) {
			v1[2] = v1[2].substring(0, 1);
		}
		if (v2.length == 3 && v2[2].length() > 2) {
			v2[2] = v2[2].substring(0, 1);
		}

		// verify that both versions can be parsed to integers
		for (int i = 0; i < v1.length; i++) {
			Integer.parseInt(v1[i]);
		}
		for (int i = 0; i < v2.length; i++) {
			Integer.parseInt(v2[i]);
		}

		if (v2.length > v1.length) {
			// v1 is older version, v2 is newer
			return -1;
		}
		if (v1.length > v2.length) {
			// v2 is older version, v1 is newer
			return 1;
		}
		// both arrays have the same length
		for (int i = 0; i < v1.length; i++) {
			if (Integer.parseInt(v1[i]) < Integer.parseInt(v2[i])) {
				return -1;
			} else if (Integer.parseInt(v1[i]) > Integer.parseInt(v2[i])) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * Shows the About Tracker dialog.
	 */
	public static void showAboutTracker() {
		String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		String vers = OSPRuntime.VERSION;
		// typical beta version 5.2.20200314
		if (vers.length() > 7 || testOn)
			vers += " BETA"; //$NON-NLS-1$
		String date = OSPRuntime.getLaunchJarBuildDate();
		if (date != null) vers = vers + "\nBuild date " + date; //$NON-NLS-1$
		
		if(OSPRuntime.isJS) {
			vers += "\n\nJavaScript transcription created using the\n" + "java2script/SwingJS framework developed at\n"
					+ "St. Olaf College.\n";
		}
		String aboutString = "Version " //$NON-NLS-1$
				+ vers + newline + COPYRIGHT + newline + "https://" + trackerWebsite + newline + newline //$NON-NLS-1$
				+ TrackerRes.getString("Tracker.About.ProjectOf") + " " //$NON-NLS-1$
				+ "Open Source Physics" + newline //$NON-NLS-1$
				+ "www.compadre.org/osp" + newline; //$NON-NLS-1$
		String translator = TrackerRes.getString("Tracker.About.Translator"); //$NON-NLS-1$
		if (!translator.equals("")) { //$NON-NLS-1$
			aboutString += newline + TrackerRes.getString("Tracker.About.TranslationBy") //$NON-NLS-1$
					+ " " + translator + newline; //$NON-NLS-1$
		}
		if (trackerHome != null) {
			aboutString += newline + TrackerRes.getString("Tracker.About.TrackerHome") //$NON-NLS-1$
					+ newline + trackerHome + newline;
		}
		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			loadCurrentVersion(true, false, false);
			if (newerVersion != null) {
				aboutString += newline + TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message1") //$NON-NLS-1$
						+ " " + newerVersion + " " //$NON-NLS-1$ //$NON-NLS-2$
						+ TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message2") //$NON-NLS-1$
						+ newline; //$NON-NLS-1$
			} else {
				aboutString += newline + TrackerRes.getString("PrefsDialog.Dialog.NewVersion.None.Message"); //$NON-NLS-1$
			}
		}
		JOptionPane.showMessageDialog(null, aboutString, TrackerRes.getString("Tracker.Dialog.AboutTracker.Title"), //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Finds data functions in all DataBuilder XMLControl files found in a specified
	 * directory. This returns a map for which the keys are names of DataBuilder xml
	 * files and the values are lists of data functions as String[] {function name,
	 * expression, tracktype}
	 * 
	 * @param dirPath the directory path
	 * @return map of file name to list of data functions
	 */
	public static Map<String, ArrayList<String[]>> findDataFunctions(String dirPath) {
		Map<String, ArrayList<String[]>> results = new TreeMap<String, ArrayList<String[]>>();
		if (dirPath == null)
			return results;

		File dir = new File(dirPath);
		if (!dir.exists())
			return results;

		File[] files = dir.listFiles(xmlFilter);
		if (files != null) {
			for (File file : files) {
				XMLControl control = new XMLControlElement(file.getPath());
				if (control.failedToRead()) {
					continue;
				}

				Class<?> type = control.getObjectClass();
				if (type != null && TrackDataBuilder.class.isAssignableFrom(type)) {
					ArrayList<String[]> expandedFunctions = new ArrayList<String[]>();

					// look through XMLControl for data functions
					for (XMLProperty next : control.getPropsRaw()) {
						if (next.getPropertyName().equals("functions")) { //$NON-NLS-1$
							// found DataFunctionPanels
							XMLControl[] panels = next.getChildControls();
							inner: for (XMLControl panelControl : panels) {
								String trackType = panelControl.getString("description"); //$NON-NLS-1$
								@SuppressWarnings("unchecked")
								ArrayList<String[]> functions = (ArrayList<String[]>) panelControl
										.getObject("functions"); //$NON-NLS-1$
								if (trackType == null || functions == null || functions.isEmpty())
									continue inner;

								// add localized trackType name to function arrays
								for (String[] f : functions) {
									String[] data = new String[3];
									System.arraycopy(f, 0, data, 0, 2);
									// use XML.getExtension method to get short name of track type
									String trackName = XML.getExtension(trackType);
									String localized = TrackerRes.getString(trackName + ".Name"); //$NON-NLS-1$
									if (!localized.startsWith("!")) //$NON-NLS-1$
										trackName = localized;
									data[2] = trackName;
									expandedFunctions.add(data);
								}
							} // end inner loop
						}
					} // end outer loop

					// add entry to the results map
					results.put(file.getName(), expandedFunctions);
				}

			}
		}
		return results;
	}

	/**
	 * Creates the actions.
	 */
	protected static void createActions() {
		// about Tracker
		aboutTrackerAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutTracker"), null) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				showAboutTracker();
			}
		};

		if (prefsPath != null) {
			trackerPrefsAction = new AbstractAction(TrackerRes.getString("Tracker.Prefs.MenuItem.Text") + "...", null) { //$NON-NLS-1$ //$NON-NLS-2$
				@Override
				public void actionPerformed(ActionEvent e) {
					if (trackerPrefsDialog == null) {
						String s = ResourceLoader.getString(prefsPath);
						if (s == null || "".equals(s)) { //$NON-NLS-1$
							s = TrackerRes.getString("Tracker.Prefs.NotFound") + ": " + prefsPath; //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.showMessageDialog(null, s, TrackerRes.getString("Tracker.Prefs.NotFound"), //$NON-NLS-1$
									JOptionPane.WARNING_MESSAGE);
							return;
						}
						trackerPrefsDialog = new JDialog((JFrame) null, true);
						trackerPrefsDialog.setTitle(TrackerRes.getString("ConfigInspector.Title") + ": " + //$NON-NLS-1$ //$NON-NLS-2$
						XML.forwardSlash(prefsPath));
						trackerPrefsTextArea = new JTextArea();
						trackerPrefsTextArea.setEditable(false);
						trackerPrefsTextArea.setTabSize(2);
						trackerPrefsTextArea.setLineWrap(true);
						trackerPrefsTextArea.setWrapStyleWord(true);
						JScrollPane scroller = new JScrollPane(trackerPrefsTextArea);
						trackerPrefsDialog.setContentPane(scroller);
						trackerPrefsTextArea.setText(s);
						trackerPrefsTextArea.setCaretPosition(0);
						FontSizer.setFonts(trackerPrefsDialog, FontSizer.getLevel());
						trackerPrefsDialog.setSize(800, 400);
						// center on screen
						Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
						int x = (dim.width - trackerPrefsDialog.getBounds().width) / 2;
						int y = (dim.height - trackerPrefsDialog.getBounds().height) / 2;
						trackerPrefsDialog.setLocation(x, y);
					} else {
						String s = ResourceLoader.getString(prefsPath);
						trackerPrefsTextArea.setText(s);
						trackerPrefsTextArea.setCaretPosition(0);
					}
					trackerPrefsDialog.setVisible(true);
				}
			};
		}

		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			// Tracker README
			readmeAction = new AbstractAction(TrackerRes.getString("Tracker.Readme") + "...", null) { //$NON-NLS-1$ //$NON-NLS-2$
				@Override
				public void actionPerformed(ActionEvent e) {
					if (readmeDialog == null && trackerHome != null) {
						String slash = System.getProperty("file.separator", "/"); //$NON-NLS-1$//$NON-NLS-2$
						String path = trackerHome + slash + readmeFileName;
						if (OSPRuntime.isMac()) {
							// OSX trackerHome=/Applications/Tracker.app/Contents/Java
							// but we want /usr/local/tracker
							path = "/usr/local/tracker/" + readmeFileName; //$NON-NLS-1$
						}
						String s = ResourceLoader.getString(path);
						if (s == null || "".equals(s)) { //$NON-NLS-1$
							s = TrackerRes.getString("Tracker.Readme.NotFound") + ": " + path; //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.showMessageDialog(null, s, TrackerRes.getString("Tracker.Readme.NotFound"), //$NON-NLS-1$
									JOptionPane.WARNING_MESSAGE);
							return;
						}
						readmeDialog = new JDialog((JFrame) null, true);
						readmeDialog.setTitle(TrackerRes.getString("Tracker.Readme")); //$NON-NLS-1$
						JTextArea textPane = new JTextArea();
						textPane.setEditable(false);
						textPane.setTabSize(2);
						textPane.setLineWrap(true);
						textPane.setWrapStyleWord(true);
						JScrollPane scroller = new JScrollPane(textPane);
						readmeDialog.setContentPane(scroller);
						textPane.setText(s);
						textPane.setCaretPosition(0);
						readmeDialog.setSize(600, 600);
						FontSizer.setFonts(readmeDialog, FontSizer.getLevel());
						// center on screen
						Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
						int x = (dim.width - readmeDialog.getBounds().width) / 2;
						int y = (dim.height - readmeDialog.getBounds().height) / 2;
						readmeDialog.setLocation(x, y);
					}
					readmeDialog.setVisible(true);
				}
			};

			// Start log
			final String startLogPath = System.getenv("START_LOG"); //$NON-NLS-1$
			if (startLogPath != null) {
				startLogAction = new AbstractAction(TrackerRes.getString("Tracker.StartLog") + "...", null) { //$NON-NLS-1$ //$NON-NLS-2$
					@Override
					public void actionPerformed(ActionEvent e) {
						if (startLogDialog == null) {
							String s = ResourceLoader.getString(startLogPath);
							if (s == null || "".equals(s)) { //$NON-NLS-1$
								s = TrackerRes.getString("Tracker.StartLog.NotFound") + ": " + startLogPath; //$NON-NLS-1$ //$NON-NLS-2$
								JOptionPane.showMessageDialog(null, s,
										TrackerRes.getString("Tracker.startLogPath.NotFound"), //$NON-NLS-1$
										JOptionPane.WARNING_MESSAGE);
								return;
							}
							startLogDialog = new JDialog((JFrame) null, true);
							startLogDialog.setTitle(TrackerRes.getString("Tracker.StartLog")); //$NON-NLS-1$
							JTextArea textPane = new JTextArea();
							textPane.setEditable(false);
							textPane.setTabSize(2);
							textPane.setLineWrap(true);
							textPane.setWrapStyleWord(true);
							JScrollPane scroller = new JScrollPane(textPane);
							startLogDialog.setContentPane(scroller);
							textPane.setText(s);
							textPane.setCaretPosition(0);
							FontSizer.setFonts(startLogDialog, FontSizer.getLevel());
							startLogDialog.setSize(600, 600);
							// center on screen
							Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
							int x = (dim.width - startLogDialog.getBounds().width) / 2;
							int y = (dim.height - startLogDialog.getBounds().height) / 2;
							startLogDialog.setLocation(x, y);
						}
						startLogDialog.setVisible(true);
					}
				};
			}

			// about Java
			aboutJavaAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutJava"), null) { //$NON-NLS-1$
				@Override
				public void actionPerformed(ActionEvent e) {
					Diagnostics.aboutJava();
				}
			};

			// about Xuggle--only if xuggle resources present?
			if (MovieFactory.xuggleIsPresent || true) {
				aboutXuggleAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutXuggle"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						MovieFactory.showAbout(MovieFactory.ENGINE_XUGGLE, "Tracker"); //$NON-NLS-1$
					}
				};
			}

			// about threads
			aboutThreadsAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutThreads"), null) { //$NON-NLS-1$
				@Override
				public void actionPerformed(ActionEvent e) {
					DiagnosticsForThreads.aboutThreads();
				}
			};
		}

	}

//  /**
//   * Attempts to relaunch Tracker with specified runtime parameters.
//   *
//   * @param memorySize the desired memory size in MB
//   * @param javaPath the java executable path
//   */
//  protected static void relaunch(int memorySize, String javaPath) {
////    try {
////			JarFile jarfile = OSPRuntime.getLaunchJar();
////			java.util.jar.Attributes att = jarfile.getManifest().getMainAttributes();
////			Object mainclass = att.getValue("Main-Class"); //$NON-NLS-1$
////			isTracker = mainclass.toString().endsWith("Tracker"); //$NON-NLS-1$
////		} catch (Exception ex) {
////		}
////		// save tracker panels
////		ArrayList<String> filenames = new ArrayList<String>();
////		if (frame!=null) {
////			for (int i = 0; i<frame.getTabCount(); i++) {
////				TrackerPanel next = frame.getTrackerPanel(i);
////				if (!next.save()) return;
////				File datafile = next.getDataFile();
////				if (datafile!=null && isTracker) {
////	    		String fileName = XML.getAbsolutePath(next.getDataFile());
////	    		filenames.add(fileName);
////				}
////			}
////		}
//	final int prevSize = Tracker.preferredMemorySize;
//	int newSize = memorySize>minimumMemorySize? memorySize: -1;
////		Tracker.preferredMemorySize = newSize;
////		Tracker.savePreferences();
//		
//    final ArrayList<String> cmd = new ArrayList<String>();
//    cmd.add(javaPath==null? "java": javaPath); //$NON-NLS-1$
//    if (newSize>-1) {
//	    cmd.add("-Xms32m"); //$NON-NLS-1$
//	    cmd.add("-Xmx"+newSize+"m"); //$NON-NLS-1$ //$NON-NLS-2$
//    }
//    if (OSPRuntime.isMac()) {
//	    cmd.add(use32BitMode? "-d32": "-d64"); //$NON-NLS-1$ //$NON-NLS-2$
//    	cmd.add("-Xdock:name=Tracker"); //$NON-NLS-1$
//    }
//    cmd.add("-jar"); //$NON-NLS-1$
//  	String jar = OSPRuntime.getLaunchJarPath();
//    cmd.add(jar);
////    if (frame!=null) {
////	    for (String next: filenames) {
////	    	cmd.add(next);
////	    }
////    }
//    if (mainArgs!=null) {
//	    for (String next: mainArgs) {
//	    	cmd.add(next);
//	    }
//    }
//  	cmd.add("relaunch"); //$NON-NLS-1$
//    // create a timer to exit the system if relaunch is successful
//    final Timer timer = new Timer(500, new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        System.exit(0);
//      }
//    });
//    timer.setRepeats(false);
//    timer.start();
//    // create a thread to launch in separate VM
//    Runnable launchRunner = new Runnable() {
//      public void run() {
//        // log command for debugging
//        String log = ""; //$NON-NLS-1$
//        for (int i=0; i< cmd.size(); i++) {
//        	log += cmd.get(i)+" "; //$NON-NLS-1$
//        }
//        writeRelaunchLog(log);
//        try { 
//        	ProcessBuilder builder = new ProcessBuilder(cmd);
//        	Process proc = builder.start();
//          BufferedInputStream errStream=new BufferedInputStream(proc.getErrorStream());
//          errStream.read(); // blocks if no errors
//          byte[] b = new byte[1024];
//          int bytesRead=0;
//          String strFileContents;
//          while( (bytesRead = errStream.read(b)) != -1){
//          	strFileContents = new String(b, 0, bytesRead);
//          	OSPLog.warning(strFileContents);
//          }
//          timer.stop();
//          errStream.close();
//          Tracker.preferredMemorySize = prevSize;
//        	Tracker.start(mainArgs);
//        } catch(Exception ex) {}
//      }
//
//    };
//    Thread relauncher = new Thread(launchRunner);
//    relauncher.setPriority(Thread.NORM_PRIORITY);
//    relauncher.start();           
//  }

//  protected static void writeRelaunchLog(String cmd) {
//    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  MMM dd yyyy"); //$NON-NLS-1$
//    Calendar cal = Calendar.getInstance();
//    String logText = "Tracker version "+version+"  " //$NON-NLS-1$ //$NON-NLS-2$
//		+sdf.format(cal.getTime())+"\n\n"; //$NON-NLS-1$
//  	logText += "java command: "+cmd; //$NON-NLS-1$
//    File file = new File(trackerHome, "tracker_relaunch.log"); //$NON-NLS-1$
//    try {
//      FileOutputStream stream = new FileOutputStream(file);
//      Charset charset = Charset.forName("UTF-8"); //$NON-NLS-1$
//      OutputStreamWriter out = new OutputStreamWriter(stream, charset);
//    	BufferedWriter writer = new BufferedWriter(out);
//      writer.write(logText);
//      writer.flush();
//      writer.close();
//    } catch(IOException ex) {}
//  }

	/**
	 * Gets the full set of configuration properties.
	 *
	 * @return the full configuration set
	 */
	protected static Set<String> getFullConfig() {
		TreeSet<String> set = new TreeSet<String>();
		for (String next : fullConfig) {
			set.add(next);
		}
		return set;
	}

	/**
	 * Gets the default set of configuration properties.
	 *
	 * @return the default configuration set
	 */
	protected static Set<String> getDefaultConfig() {
		if (defaultConfig == null)
			defaultConfig = getFullConfig();
		TreeSet<String> set = new TreeSet<String>();
		for (String next : defaultConfig) {
			set.add(next);
		}
		return set;
	}

	/**
	 * Sets the default set of configuration properties.
	 *
	 * @param config a set of configuration properties
	 */
	protected static void setDefaultConfig(Set<String> config) {
		if (defaultConfig == null)
			defaultConfig = new TreeSet<String>();
		defaultConfig.clear();
		for (String next : config) {
			defaultConfig.add(next);
		}
	}

	/**
	 * @j2sIngnore
	 * 
	 * Autoloads data functions found in the user home and code base directories.
	 * This loads DataFunctionPanel XMLControls into a static collection that is
	 * accessed when need by DataBuilder.
	 */
	protected static void autoloadDataFunctions() {
		dataFunctionControls.clear();
		for (String dirPath : getInitialSearchPaths()) {
			if (dirPath == null)
				continue;

			File dir = new File(dirPath);
			if (!dir.exists())
				continue;

			File[] files = dir.listFiles(xmlFilter);
			if (files != null) {
				for (File file : files) {
					XMLControl control = new XMLControlElement(file.getPath());
					if (control.failedToRead()) {
						continue;
					}

					Class<?> type = control.getObjectClass();
					if (type != null && TrackDataBuilder.class.isAssignableFrom(type)) {
						for (XMLProperty next : control.getPropsRaw()) {
							if (next.getPropertyName().equals("functions")) { //$NON-NLS-1$
								// found DataFunctionPanels
								ArrayList<XMLControl> controls = new ArrayList<XMLControl>();
								XMLControl[] panels = ((XMLProperty) next).getChildControls();
								inner: for (XMLControl panelControl : panels) {
									String trackType = panelControl.getString("description"); //$NON-NLS-1$
									@SuppressWarnings("unchecked")
									ArrayList<String[]> functions = (ArrayList<String[]>) panelControl
											.getObject("functions"); //$NON-NLS-1$
									if (trackType == null || functions == null || functions.isEmpty())
										continue inner;

									// add panel to dataFunctionControls
									controls.add(panelControl);
								} // end inner loop

								String filePath = XML.forwardSlash(file.getAbsolutePath());
								dataFunctionControls.put(filePath, controls);
							}
						} // end next loop
					}
				} // end file loop
			}
		} // end dirPath loop
	}

	/**
	 * Gets the default autoload search paths.
	 * 
	 * @return the default search paths
	 */
	public static Collection<String> getDefaultAutoloadSearchPaths() {
		return OSPRuntime.getDefaultSearchPaths();
	}

//  /**
//   * Imports Data from a source into a DataTrack. 
//   * Data must include "x" and "y" columns, may include "t". 
//   * The returned DataTrack is the first one found in the selected TrackerPanel
//   * that matches the Data name or ID. If none found, a new DataTrack is created.
//   * The source Object may be a String path, JPanel controlPanel, Tool tool, etc
//   * 
//   * @param data the Data to import
//   * @param source the data source (may be null)
//   * @return the DataTrack with the Data (may return null)
//   */
//  public static DataTrack importData(Data data, Object source) {
//  	// get shared Tracker  
//  	Tracker tracker = getTracker();
//  	TFrame frame = tracker.getFrame();
//  	frame.setVisible(true);
//  	
//  	// look for matching DataTrack in selected TrackerPanel?
//  	DataTrack model = null;
//  	TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());  	
//  	if (trackerPanel!=null) {
//	  	model = trackerPanel.importData(data, source);
//  	}
//  	
//  	// create new tab
//  	if (model==null) {
//	  	trackerPanel = new TrackerPanel();
//	  	frame.addTab(trackerPanel);
//	
//	  	// pass the data and source to the TrackerPanel and get the DataTrack it creates
//	  	model = trackerPanel.importData(data, source);
//  	}
//  	if (model==null) {
//	  	frame.setVisible(false);
//	  	return null;
//  	}
//  	return model;
//  }
//  
	/**
	 * Gets the starting autoload search paths. Search paths may be later modified
	 * by the user.
	 * 
	 * @return the search paths
	 */
	protected static Collection<String> getInitialSearchPaths() {
		if (initialAutoloadSearchPaths.isEmpty()) {
			if (preferredAutoloadSearchPaths != null) {
				for (String next : preferredAutoloadSearchPaths) {
					initialAutoloadSearchPaths.add(next);
				}
			} else {
				for (String next : getDefaultAutoloadSearchPaths()) {
					initialAutoloadSearchPaths.add(next);
				}
			}
		}
		return initialAutoloadSearchPaths;
	}

	/**
	 * Sets the preferred locale.
	 * 
	 * @param localeName the name of the locale
	 */
	protected static void setPreferredLocale(String localeName) {
		if (localeName == null) {
			Locale.setDefault(defaultLocale);
			preferredLocale = null;
		} else {
			getLocales();
			for (Locale locale : locales) {
				if (locale.toString().equals(localeName)) {
					Locale.setDefault(locale);
					preferredLocale = localeName;
					break;
				}
			}
		}
		// set the default decimal separator
		char separator = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
		// deal with special case pt_PT
		if ("pt_PT".equals(localeName)) { //$NON-NLS-1$
			separator = ',';
		}
		OSPRuntime.setDefaultDecimalSeparator(separator);
	}

	/**
	 * Sets the cache path.
	 * 
	 * @param cachePath the cache path
	 */
	protected static void setCache(String cachePath) {
		File cacheDir = cachePath == null || cachePath.trim().equals("") ? ResourceLoader.getDefaultOSPCache() //$NON-NLS-1$
				: new File(cachePath);
		ResourceLoader.setOSPCache(cacheDir);
	}

	/**
	 * Checks and updates video engine resources if needed.
	 * 
	 * @return true if any resources were updated
	 */
	protected static boolean updateResources() {
		String[] updatedEngines = MovieFactory.getUpdatedVideoEngines();
		return updatedEngines != null && updatedEngines.length > 0
				&& updatedEngines[0].equals(MovieFactory.ENGINE_XUGGLE);
	}

	/**
	 * Determines if two sets contain the same elements.
	 * 
	 * @param set1
	 * @param set2
	 * @return true if the sets are equal
	 */
	protected static boolean areEqual(Set<?> set1, Set<?> set2) {
		for (Object next : set1) {
			if (!set2.contains(next))
				return false;
		}
		for (Object next : set2) {
			if (!set1.contains(next))
				return false;
		}
		return true;
	}

	/**
	 * Check for upgrades and show a dialog with upgrade info. Also refresh toolbar
	 * associated with TrackerPanel, if any.
	 * 
	 * @param trackerPanel a TrackerPanel (may be null)
	 */
	protected static void showUpgradeStatus(TrackerPanel trackerPanel) {
		checkedForNewerVersion = false;
		boolean userInformed = loadCurrentVersion(true, false, true);
//		if (trackerPanel != null)
//			trackerPanel.refreshTrackBar();
			//TTrackBar.getTrackbar(trackerPanel).refresh();
		if (!userInformed) {
			String message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.None.Message"); //$NON-NLS-1$
			if (Tracker.newerVersion != null) { // new version available
				message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message1") //$NON-NLS-1$
						+ " " + Tracker.newerVersion + " " //$NON-NLS-1$ //$NON-NLS-2$
						+ TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message2") //$NON-NLS-1$
						+ XML.NEW_LINE + "https://" + trackerWebsite; //$NON-NLS-1$
			}
			TFrame frame = trackerPanel == null ? null : trackerPanel.getTFrame();
			JOptionPane.showMessageDialog(frame, message, TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Title"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Loads the current (latest) Tracker version number and compares it with this
	 * version.
	 * 
	 * @param ignoreInterval true to load/compare immediately
	 * @param logToFile      true to log in to the PHP counter
	 * @param dialogOK true to notify user if newer version available
	 * @return true if a newer version is found and user informed 
	 */
	protected static boolean loadCurrentVersion(boolean ignoreInterval, boolean logToFile, boolean dialogOK) {
		if (OSPRuntime.isJS
				|| TRACKER_TEST_URL == null
				|| !ResourceLoader.isURLAvailable(TRACKER_TEST_URL)
				) {
			return false;
		}
		if (checkedForNewerVersion)
			return false;
		checkedForNewerVersion = true;

		// check to see how much time has passed
		long millis = System.currentTimeMillis();
		double days = (millis - lastMillisChecked) / 86400000.0;

		// don't log to file more often than every 2 hours no matter what
		if (logToFile && days < 0.0833)
			logToFile = false;

		// send data as page name to get latest version from PHP script
		// typical pre-4.97 version: "4.90" or "4.61111227"
		// typical post-4.97 version: "4.9.8" or "4.10.0.170504" or "5.0.1"
		String pageName = getPHPPageName(logToFile);
		String newVersion = loginGetLatestVersion(pageName);

		if (!ignoreInterval) {
			// check to see if upgrade interval has passed
			double interval = checkForUpgradeInterval == 0 ? 0.0833 : checkForUpgradeInterval;
			if (days < interval) {
				return false;
			}
		}

		// interval has passed or ignored, so check for upgrades
		lastMillisChecked = millis;
		if (testOn && testString != null) {
			newVersion = testString;
		}
		int result = 0;
		try {
			result = compareVersions(newVersion, OSPRuntime.VERSION);
		} catch (Exception e) {
		}
		if (result > 0) { // newer version available
			newerVersion = newVersion;
			TFrame tFrame = null;
			Frame[] frames = Frame.getFrames();
			for (int i = 0, n = frames.length; i < n; i++) {
				if (frames[i] instanceof TFrame) {
					tFrame = (TFrame) frames[i];
					TrackerPanel trackerPanel = tFrame.getSelectedPanel();
					if (trackerPanel != null) {
						trackerPanel.taintEnabled();
						TToolBar.getToolbar(trackerPanel).refresh(TToolBar.REFRESH__NEW_VERSION);
//						trackerPanel.refreshTrackBar();
					}
				}
			}
			// show dialog if this is a first-time-seen upgrade version
//	    if (testOn) latestVersion = null; // pig for testing only
			String testVersion = latestVersion == null ? OSPRuntime.VERSION : latestVersion;
			result = 0;
			try {
				result = compareVersions(newVersion, testVersion);
			} catch (Exception e) {
			}
			if (result == 1 && tFrame != null && dialogOK) {
				Object[] options = new Object[] { TrackerRes.getString("Tracker.Dialog.NewVersion.Button.Upgrade"), //$NON-NLS-1$
						TrackerRes.getString("TTrackBar.Popup.MenuItem.LearnMore"), //$NON-NLS-1$
						TrackerRes.getString("Tracker.Dialog.NewVersion.Button.Later") }; //$NON-NLS-1$
				String message = TrackerRes.getString("Tracker.Dialog.NewVersion.Message1") + " " + newVersion; //$NON-NLS-1$ //$NON-NLS-2$
				message += " " + TrackerRes.getString("Tracker.Dialog.NewVersion.Message2"); //$NON-NLS-1$//$NON-NLS-2$
				message += "  " + TrackerRes.getString("Tracker.Dialog.NewVersion.Message3"); //$NON-NLS-1$//$NON-NLS-2$
				message += "\n" + TrackerRes.getString("Tracker.Dialog.NewVersion.Message4"); //$NON-NLS-1$//$NON-NLS-2$
				JTextPane pane = new JTextPane();
				pane.setOpaque(false);
				pane.setText(message);
				JCheckBox checkbox = new JCheckBox(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Checkbox")); //$NON-NLS-1$

				Box b = Box.createHorizontalBox();
				b.add(checkbox);
				b.add(Box.createHorizontalGlue());
				Box box = Box.createVerticalBox();
				box.add(pane);
				box.add(b);
				int response = JOptionPane.showOptionDialog(tFrame, box,
						TrackerRes.getString("Tracker.Dialog.NewVersion.Title"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, TRACKER_ICON, options, options[0]);

				if (response == 0) {
					// upgrade
					new Upgrader(tFrame).upgrade();
				} else if (response == 1) {
					// go to Tracker change log
					String websiteURL = "https://" + trackerWebsite + "/change_log.html"; //$NON-NLS-1$ //$NON-NLS-2$
					OSPDesktop.displayURL(websiteURL);
				}
				if (checkbox.isSelected()) {
					latestVersion = newVersion;
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the "page name" to send to the PHP counter.
	 * 
	 * @param logToFile true to assemble a page name that will be counted/logged
	 * @return the page name
	 */
	private static String getPHPPageName(boolean logToFile) {
		String page = "version"; //$NON-NLS-1$
		if (logToFile) {
			// assemble "page" to send to counter
			Locale locale = Locale.getDefault();
			String language = locale.getLanguage();
			String country = locale.getCountry();
			String engine = MovieFactory.getMovieEngineName(false);
			String os = "unknownOS"; //$NON-NLS-1$
			try { // system properties may not be readable in some environments
				os = System.getProperty("os.name", "unknownOS").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (SecurityException ex) {
			}
			os = os.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (os.indexOf("windows") > -1) { //$NON-NLS-1$
				os = "windows"; //$NON-NLS-1$
			}
			page = "log_" + OSPRuntime.VERSION + "_" + os + "_" + engine; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!"".equals(language)) { //$NON-NLS-1$
				if (!"".equals(country)) { //$NON-NLS-1$
					language += "-" + country; //$NON-NLS-1$
				}
				page += "_" + language; //$NON-NLS-1$
			}
		}
		return page;
	}

	/**
	 * Logs a specified page and returns the latest version of Tracker.
	 * 
	 * @param page a page name
	 * @return the latest available version as a string
	 */
	private static String loginGetLatestVersion(String page) {
		String path = counterPath + "page=" + page; //$NON-NLS-1$
		try {
			URL url = new URL(path);
			Resource res = new Resource(url);
			String version = res.getString().trim();
			OSPLog.finer(path + ":   " + version); //$NON-NLS-1$
			return version;
		} catch (Exception e) {
		}
		return OSPRuntime.VERSION;
	}

	/**
	 * Loads preferences from a preferences file, if any.
	 */
	protected static void loadPreferences() {

		XMLControl prefsControl = TrackerStarter.findPreferences();
		if (prefsControl != null) {
			prefsPath = prefsControl.getString("prefsPath"); //$NON-NLS-1$
			if (prefsPath != null) {
				OSPLog.getOSPLog();
				OSPLog.info("preferences loaded from " + XML.getAbsolutePath(new File(prefsPath))); //$NON-NLS-1$
			}
			prefsControl.loadObject(null); // the loader itself reads the values
			return;
		}

		/**
		 * @j2sNative return;
		 */
		{
			// unable to find prefs, so write new one(s) if possible
			String recommendedPath = null;
			String fileName = TrackerStarter.PREFS_FILE_NAME;
			if (!OSPRuntime.isWindows()) {
				// add leading dot to hide file on OSX and Linux
				fileName = "." + fileName; //$NON-NLS-1$
			}
			for (String path : OSPRuntime.getDefaultSearchPaths()) {
				String prefs_path = new File(path, fileName).getAbsolutePath();
				if (recommendedPath == null)
					recommendedPath = prefs_path;
				else
					recommendedPath += " or " + prefs_path; //$NON-NLS-1$
				XMLControl control = new XMLControlElement(new Preferences());

				if (control.write(prefs_path) != null) {
					prefsPath = prefs_path;
					OSPLog.getOSPLog();
					OSPLog.info("wrote new preferences file to " + XML.getAbsolutePath(new File(prefsPath))); //$NON-NLS-1$
				}
			}
			if (prefsPath == null) {
				// unable to read or write prefs
				OSPLog.getOSPLog();
				if (recommendedPath != null) {
					OSPLog.warning(
							"administrator action required: unable to write preferences file to " + recommendedPath); //$NON-NLS-1$
				} else {
					OSPLog.warning("unable to find or create preferences file " + TrackerStarter.PREFS_FILE_NAME); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Saves the current preferences.
	 * 
	 * @return the path to the saved file
	 */
	protected static String savePreferences() {
		XMLControl control = new XMLControlElement(new Preferences());
		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			// save prefs file in current preferences path
			if (prefsPath != null) {
				control.write(prefsPath);
			}

			// save other existing prefs files
			for (int i = 0; i < 2; i++) {
				String fileName = TrackerStarter.PREFS_FILE_NAME;
				if (i == 1) {
					fileName = "." + fileName; //$NON-NLS-1$
				}
				// update prefs files in OSPRuntime search paths, if any
				for (String path : OSPRuntime.getDefaultSearchPaths()) {
					File prefsFile = new File(path, fileName);
					if (prefsFile.getAbsolutePath().equals(prefsPath)) {
						continue;
					}
					if (prefsFile.exists() && prefsFile.canWrite()) {
						control.write(prefsFile.getAbsolutePath());
					}
				}
				// update prefs in current directory, if any
				File prefsFile = new File(fileName);
				if (prefsFile.getAbsolutePath().equals(prefsPath)) {
					continue;
				}
				if (prefsFile.exists() && prefsFile.canWrite()) {
					control.write(prefsFile.getAbsolutePath());
				}
			}

			// save current trackerHome and xuggleHome in OSP preferences
			if (trackerHome != null && new File(trackerHome, "tracker.jar").exists()) { //$NON-NLS-1$
				OSPRuntime.setPreference("TRACKER_HOME", trackerHome); //$NON-NLS-1$
			}
			String xuggleHome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
			if (xuggleHome != null) {
				OSPRuntime.setPreference("XUGGLE_HOME", xuggleHome); //$NON-NLS-1$
			}
			OSPRuntime.savePreferences();
			return prefsPath;
		} else { // JS
					// localStorage.setItem("trackerprefs", control.toXML());
		}
		return null;
	}

	/**
	 * Gets the zoomInCursor.
	 *
	 * @return the cursor
	 */
	protected static Cursor getZoomInCursor() {
		if (zoomInCursor == null) {
			String imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/zoom_in.gif"; //$NON-NLS-1$
			Image zoom = ResourceLoader.getImage(imageFile);
			zoomInCursor = GUIUtils.createCustomCursor(zoom, new Point(12, 12), "Zoom In", Cursor.DEFAULT_CURSOR); //$NON-NLS-1$
		}
		return zoomInCursor;
	}

	/**
	 * Determines if a cursor is the zoomInCursor.
	 *
	 * @return true if the cursor is zoonIn
	 */
	protected static boolean isZoomInCursor(Cursor cursor) {
		return cursor == zoomInCursor && zoomInCursor != Cursor.getDefaultCursor();
	}

	/**
	 * Gets the zoomOutCursor.
	 *
	 * @return the cursor
	 */
	protected static Cursor getZoomOutCursor() {
		if (zoomOutCursor == null) {
			String imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/zoom_out.gif"; //$NON-NLS-1$
			Image zoom = ResourceLoader.getImage(imageFile);
			zoomOutCursor = GUIUtils.createCustomCursor(zoom, new Point(12, 12), "Zoom Out", Cursor.DEFAULT_CURSOR); //$NON-NLS-1$
		}
		return zoomOutCursor;
	}

	/**
	 * Determines if a cursor is the zoomOutCursor.
	 *
	 * @return true if the cursor is zoomOut
	 */
	protected static boolean isZoomOutCursor(Cursor cursor) {
		return cursor == zoomOutCursor && zoomOutCursor != Cursor.getDefaultCursor();
	}

	/**
	 * Main entry point when used as application.
	 *
	 * @param args array of tracker or video file names
	 */
	public static void main(String[] args) {

		OSPLog.debug(Performance.timeCheckStr("Tracker.main start", Performance.TIME_RESET));

		initClass();

//		String[] vars = {"TRACKER_HOME", "XUGGLE_HOME", "DYLD_LIBRARY_PATH"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
//		for (String next: vars) {
//			OSPLog.warning("Environment variable "+next+": "+System.getenv(next)); //$NON-NLS-1$ //$NON-NLS-2$
//		}
//
//  	Map<String, String> map = System.getenv();
//  	for (String key: map.keySet()) {
//  		System.out.println("environment "+key+" = "+map.get(key));
//			OSPLog.config("Environment variable "+key+": "+map.get(key)); //$NON-NLS-1$ //$NON-NLS-2$
//  	}
//  	for (Object key: System.getProperties().keySet()) {
//  		OSPLog.config("property "+key+" = "+System.getProperties().get(key));
//  	}

		// determine if this is tracker.jar (Tracker main class)
		// bypassed in JavaScript
		if (initializeJava(args))
			start(args);
	}

	/**
	 * 
	 * @param args
	 * @return true if we can start
	 */
	private static boolean initializeJava(String[] args) {

		if (OSPRuntime.isJS) {
			originalMemoryRequest = requestedMemorySize;
			return true;
		}

		/**
		 * Java only; transpiler can ignore.
		 * 
		 * @j2sNative
		 * 
		 */
		{
			boolean isTrackerJar = false;
			JarFile jarfile = OSPRuntime.getLaunchJar();
			if (jarfile != null) {
				try {
					java.util.jar.Attributes att = jarfile.getManifest().getMainAttributes();
					Object mainclass = att.getValue("Main-Class"); //$NON-NLS-1$
					isTrackerJar = mainclass.toString().endsWith("Tracker"); //$NON-NLS-1$
				} catch (Exception ex) {
				}

			}
			// determine if this is a relaunch or if relaunch is needed
			boolean isRelaunch = args != null && args.length > 0 && "relaunch".equals(args[args.length - 1]); //$NON-NLS-1$
			if (isRelaunch) {
				args[args.length - 1] = null;
			} else {
				// versions 4.87+ use environment variable to indicate relaunch
				String s = System.getenv(TrackerStarter.TRACKER_RELAUNCH);
				isRelaunch = "true".equals(s); //$NON-NLS-1$
			}

			// get memory size requested in environment, if any
			String memoryEnvironment = System.getenv("MEMORY_SIZE"); //$NON-NLS-1$
			// get current memory (maximum heap) size
			java.lang.management.MemoryMXBean memory = java.lang.management.ManagementFactory.getMemoryMXBean();
			long currentMemory = memory.getHeapMemoryUsage().getMax() / (1024 * 1024);

			if (!isRelaunch) {
				// should never run in 32-bit VM
				if (JREFinder.getFinder().is32BitVM(preferredJRE))
					preferredJRE = null;
				boolean needsJavaVM = OSPRuntime.getVMBitness() == 32;
				if (!needsJavaVM) {
					String javaCommand = System.getProperty("java.home"); //$NON-NLS-1$
					javaCommand = XML.forwardSlash(javaCommand) + "/bin/java"; //$NON-NLS-1$
					String javaPath = preferredJRE;
					if (javaPath != null) {
						if (JREFinder.getFinder().is32BitVM(javaPath))
							javaPath = null;
						else {
						File javaFile = OSPRuntime.getJavaFile(javaPath);
						if (javaFile != null) {
							javaPath = XML.stripExtension(XML.forwardSlash(javaFile.getPath()));
						} else
							javaPath = null;
						}
					}
					needsJavaVM = javaPath != null && !javaCommand.equals(javaPath);
				}

				// update video engine resources
				boolean updated = updateResources();

				// compare memory with requested size(s)
				if (memoryEnvironment != null) {
					originalMemoryRequest = requestedMemorySize;
					requestedMemorySize = Integer.parseInt(memoryEnvironment);
				}

				boolean needsMemory = requestedMemorySize > 10 && (currentMemory < 9 * requestedMemorySize / 10
						|| currentMemory > 11 * requestedMemorySize / 10);

				boolean needsEnvironment = false;
				try {
					// BH SwingJS just avoiding unnecessary exception triggering
					String trackerDir = TrackerStarter.findTrackerHome(false);
					if (trackerDir != null) {
						String trackerEnv = System.getenv("TRACKER_HOME"); //$NON-NLS-1$
						if (trackerDir != null && !trackerDir.equals(trackerEnv)) {
							needsEnvironment = true;
						} 
//						else {
//							String xuggleDir = TrackerStarter.findXuggleHome(trackerDir, false);
//							String xuggleEnv = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
//							if (xuggleDir != null && !xuggleDir.equals(xuggleEnv)) {
//								needsEnvironment = true;
//							} else {
//								if (xuggleDir != null) {
//									String xuggleServer = System.getenv("XUGGLE_SERVER"); //$NON-NLS-1$
//									if (xuggleServer == null) {
//										String subdir = OSPRuntime.isWindows() ? "bin" : "lib"; //$NON-NLS-1$ //$NON-NLS-2$
//										String xugglePath = xuggleDir + File.separator + subdir;
//										String pathName = OSPRuntime.isWindows() ? "Path" : //$NON-NLS-1$
//												OSPRuntime.isMac() ? "DYLD_LIBRARY_PATH" : "LD_LIBRARY_PATH"; //$NON-NLS-1$ //$NON-NLS-2$
//										String pathEnv = System.getenv(pathName);
//										if (pathEnv == null || !pathEnv.contains(xugglePath)) {
//											needsEnvironment = true;
//										}
//									}
//								}
//							}
//						}
					}

				} catch (Exception e) {
				}

				// attempt to relaunch if needed
				if (isTrackerJar && (needsJavaVM || needsMemory || needsEnvironment || updated)) {
					TrackerStarter.logMessage("relaunch required"); //$NON-NLS-1$
					TrackerStarter.logMessage("needs Java VM? " + needsJavaVM); //$NON-NLS-1$
					TrackerStarter.logMessage("needs memory? " + needsMemory); //$NON-NLS-1$
					TrackerStarter.logMessage("needs environment? " + needsEnvironment); //$NON-NLS-1$

					mainArgs = args;
					if (requestedMemorySize <= 10) {
						requestedMemorySize = TrackerStarter.DEFAULT_MEMORY_SIZE;
					}
					System.setProperty(TrackerStarter.PREFERRED_MEMORY_SIZE, String.valueOf(requestedMemorySize));
					System.setProperty(TrackerStarter.PREFERRED_TRACKER_JAR, OSPRuntime.getLaunchJarPath());

					TrackerStarter.relaunch(mainArgs, true);
					return false;
				}
			}
			preferredMemorySize = requestedMemorySize;
			if (requestedMemorySize < 0)
				requestedMemorySize = (int) (currentMemory + 2);
			return true;
		}
	}

	/**
	 * Starts a new Tracker.
	 *
	 * @param args array of tracker or video file names
	 */
	private static void start(String[] args) {
		FontSizer.setLevel(preferredFontLevel + preferredFontLevelPlus);
		Dataset.maxPointsMultiplier = 6; // increase max points in dataset
		Tracker tracker = new Tracker(args, true, true, null);
		OSPRuntime.setAppClass(tracker);
		if (OSPRuntime.isMac()) {
			// instantiate the OSXServices class by reflection
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tracker"); //$NON-NLS-1$ //$NON-NLS-2$
			String className = "org.opensourcephysics.cabrillo.tracker.deploy.OSXServices"; //$NON-NLS-1$
			try {
				Class<?> OSXClass = Class.forName(className);
				Constructor<?> constructor = OSXClass.getConstructor(Tracker.class);
				constructor.newInstance(tracker);
			} catch (Exception ex) {
			} catch (Error err) {
			}
		}

		final TFrame frame = tracker.getFrame();
		if (!OSPRuntime.isJS)
			frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		LaunchNode node = OSPRuntime.activeNode;
		if (node != null) {
			frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		}
		TTrackBar.refreshMemoryButton();

		// inform user if memory size was reduced
		if (!OSPRuntime.isJS && originalMemoryRequest > requestedMemorySize) {
			JOptionPane.showMessageDialog(frame, TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message1") + " " //$NON-NLS-1$ //$NON-NLS-2$
					+ originalMemoryRequest + "MB\n" + //$NON-NLS-1$
					TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message2") + " " + requestedMemorySize //$NON-NLS-1$ //$NON-NLS-2$
					+ "MB.\n\n" + //$NON-NLS-1$
					TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message3"), //$NON-NLS-1$
					TrackerRes.getString("Tracker.Dialog.MemoryReduced.Title"), //$NON-NLS-1$
					JOptionPane.INFORMATION_MESSAGE);
		}

//    warnNoVideoEngine = false; // for PLATO

		if (warnNoVideoEngine && !MovieFactory.hasVideoEngine()) {
			// warn user that there is no working video engine
			boolean xuggleInstalled = !OSPRuntime.isJS && MovieFactory.hasVideoEngine();

			ArrayList<String> message = new ArrayList<String>();
			boolean showRelaunchDialog = false;

			// no engine installed
			if (!xuggleInstalled) {
				message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message1")); //$NON-NLS-1$
				message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message2")); //$NON-NLS-1$
				message.add(" "); //$NON-NLS-1$
				message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message3")); //$NON-NLS-1$
			}

//			// engines installed on Windows but no 32-bit VM
//			else if (OSPRuntime.isWindows() && JREFinder.getFinder().getDefaultJRE(32, trackerHome, true) == null) {
//				message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message1")); //$NON-NLS-1$
//				message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message2")); //$NON-NLS-1$
//				message.add(" "); //$NON-NLS-1$
//				message.add(TrackerRes.getString("Tracker.Dialog.Install32BitVM.Message")); //$NON-NLS-1$
//				message.add(TrackerRes.getString("PrefsDialog.Dialog.No32bitVM.Message")); //$NON-NLS-1$
//			}

			// engines installed on Windows but running in 32-bit VM
			else if (OSPRuntime.isWindows() && OSPRuntime.getVMBitness() == 32) {
				message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message1")); //$NON-NLS-1$
				message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message2")); //$NON-NLS-1$
				message.add(" "); //$NON-NLS-1$
				message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Question")); //$NON-NLS-1$
				showRelaunchDialog = true;
			}

			// engines installed but not working
			else {
				message.add(TrackerRes.getString("Tracker.Dialog.EngineProblems.Message1")); //$NON-NLS-1$
				message.add(TrackerRes.getString("Tracker.Dialog.EngineProblems.Message2")); //$NON-NLS-1$
			}

			Box box = Box.createVerticalBox();
			for (String line : message) {
				box.add(new JLabel(line));
			}

			// add "don't show again" checkbox
			box.add(new JLabel("  ")); //$NON-NLS-1$
			final JCheckBox checkbox = new JCheckBox(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Checkbox")); //$NON-NLS-1$
			checkbox.addActionListener((e) -> {
				warnNoVideoEngine = !checkbox.isSelected();
			});
			box.add(checkbox);
			box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

			if (showRelaunchDialog) {
				// provide immediate way to change to 64-bit VM and relaunch
				Object[] options = new Object[] { TrackerRes.getString("Tracker.Dialog.Button.RelaunchNow"), //$NON-NLS-1$
						TrackerRes.getString("Tracker.Dialog.Button.ContinueWithoutEngine") }; //$NON-NLS-1$
				int response = JOptionPane.showOptionDialog(frame, box,
						TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Title"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				if (response == 0) {
					// use prefs dialog to switch to 32-bit VM/default engine and relaunch
					SwingUtilities.invokeLater(() -> {
						PrefsDialog prefs = frame.getPrefsDialog();
						prefs.relaunch64Bit();
					});
				}
			} else {
				JOptionPane.showMessageDialog(frame, box, TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Title"), //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE);
			}

		}

		if (System.getenv("STARTER_WARNING") != null) { //$NON-NLS-1$
			// possible cause: running VM in 64-bits even though preference is 32-bit
			// if so, change preference
			String warningString = System.getenv("STARTER_WARNING"); //$NON-NLS-1$
			String[] lines = warningString.split("\n"); //$NON-NLS-1$
			Box box = Box.createVerticalBox();
			for (String line : lines) {
				box.add(new JLabel(line));
			}

			box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

			JOptionPane.showMessageDialog(null, box, TrackerRes.getString("Tracker.Dialog.StarterWarning.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
		}
		if (!OSPRuntime.isJS) /** @j2sNative */
		{

			final String newVersionURL = System.getenv(TrackerStarter.TRACKER_NEW_VERSION);
			if (newVersionURL != null) {
				Timer timer = new Timer(2000, (e) -> {
					if (OSPRuntime.isWindows()) {
						File target = new File(trackerHome, "tracker.jar"); //$NON-NLS-1$
						ResourceLoader.download(newVersionURL, target, true);
					}
					// check preferences: if not default tracker.jar, ask user to change to default
					if (preferredTrackerJar != null && !"tracker.jar".equals(preferredTrackerJar)) { //$NON-NLS-1$
						String prefVers = preferredTrackerJar.substring(8, preferredTrackerJar.lastIndexOf(".")); //$NON-NLS-1$
						String s1 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message1") //$NON-NLS-1$
								+ " " //$NON-NLS-1$
								+ OSPRuntime.VERSION;
						String s2 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message2") //$NON-NLS-1$
								+ " " //$NON-NLS-1$
								+ prefVers;
						String s3 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message3"); //$NON-NLS-1$
						String title = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Title"); //$NON-NLS-1$
						int response = JOptionPane.showConfirmDialog(null, s1 + XML.NEW_LINE + s2 + XML.NEW_LINE + s3,
								title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (response == JOptionPane.YES_OPTION) {
							preferredTrackerJar = null;
						}
//  					preferredJRE = null;  // reset preferredJRE to the bundled JRE // no longer needed?
						savePreferences();
					}
				});
				timer.setRepeats(false);
				timer.start();
			}

			Timer memoryTimer = new Timer(5000, (e) -> {
					TTrackBar.refreshMemoryButton();
			});
			memoryTimer.setRepeats(true);
			memoryTimer.start();
		}
		if (OSPRuntime.isJS)
			frame.setVisible(true);
	}

	/**
	 * Sets the progress in percent for splash display.
	 * 
	 * @param progress a number from 0 (start) to 100 (done)
	 */
	protected static void setProgress(int progress) {
		// BH 2020.02.11 Allow for multiple identifyable cycles
//		if (progress > STATE_INIT && progress < STATE_DONE)
//			progressBar.setValue(progress % 1000);
	}

	/**
	 * Logs the current time (to milliseconds) with a message.
	 * 
	 * @param message
	 */
	protected static void logTime(String message) {
		if (timeLogEnabled) {
			SimpleDateFormat sdf = new SimpleDateFormat("ss.SSS"); //$NON-NLS-1$
			Calendar cal = Calendar.getInstance();
			OSPLog.info(sdf.format(cal.getTime()) + ": " + message); //$NON-NLS-1$
		}
	}

	/**
	 * Adds a path to the list of recent files.
	 * 
	 * @param filename the absolute path to a recently opened or saved file.
	 * @param atEnd    true to add at end of the list
	 */
	protected static void addRecent(String filename, boolean atEnd) {
		synchronized (recentFiles) {
			while (recentFiles.contains(filename))
				recentFiles.remove(filename);
			if (atEnd)
				recentFiles.add(filename);
			else
				recentFiles.add(0, filename);
			while (recentFiles.size() > recentFilesSize) {
				recentFiles.remove(recentFiles.size() - 1);
			}
		}
	}

	/**
	 * Sets the maximum size of the recent files list. Limited to 12 or less.
	 * 
	 * @param max the desired maximum size.
	 */
	protected static void setRecentSize(int max) {
		max = Math.min(max, 12);
		recentFilesSize = Math.max(max, 0);
		while (recentFiles.size() > recentFilesSize) {
			recentFiles.remove(recentFiles.size() - 1);
		}
	}

	/**
	 * A class to save and load Tracker preferences. The preference data are static
	 * Tracker fields.
	 */
	static class Preferences {

		/**
		 * Returns an ObjectLoader to save and load data for this class.
		 *
		 * @return the object loader
		 */
		public static XML.ObjectLoader getLoader() {
			return new Loader();
		}

		/**
		 * A class to save and load data for this class.
		 */
		static class Loader implements XML.ObjectLoader {

			/**
			 * Saves an object's data to an XMLControl.
			 *
			 * @param control the control to save to
			 * @param obj     the object to save
			 */
			@Override
			public void saveObject(XMLControl control, Object obj) {
				// save only non-default values
				if (!preferredLogLevel.equals(DEFAULT_LOG_LEVEL)) // true by default
					control.setValue("log_level", preferredLogLevel.getName()); //$NON-NLS-1$
				if (!showHintsByDefault) // true by default
					control.setValue("show_hints", showHintsByDefault); //$NON-NLS-1$
				if (isRadians) // false by default
					control.setValue("radians", isRadians); //$NON-NLS-1$
				if (!markAtCurrentFrame) // true by default
					control.setValue("mark_current_frame", markAtCurrentFrame); //$NON-NLS-1$
				if (scrubMouseWheel) // false by default
					control.setValue("scrub_mousewheel", scrubMouseWheel); //$NON-NLS-1$
				if (!showGaps) // true by default
					control.setValue("show_gaps", showGaps); //$NON-NLS-1$
				if (!enableAutofill) // true by default
					control.setValue("enable_autofill", enableAutofill); //$NON-NLS-1$
				if (preferredTrailLengthIndex != DEFAULT_TRAIL_LENGTH_INDEX)
					control.setValue("trail_length", TToolBar.trailLengthNames[preferredTrailLengthIndex]); //$NON-NLS-1$
				if (!centerCalibrationStick) // true by default
					control.setValue("center_stick", centerCalibrationStick); //$NON-NLS-1$
				if (!isXuggleFast) // true by default
					control.setValue("xuggle_smooth", true); //$NON-NLS-1$
				if (!warnNoVideoEngine) // true by default
					control.setValue("warn_no_engine", warnNoVideoEngine); //$NON-NLS-1$
				if (!warnVariableDuration) // true by default
					control.setValue("warn_variable_frame_duration", warnVariableDuration); //$NON-NLS-1$
				if (!warnXuggleError) // true by default
					control.setValue("warn_xuggle_error", warnXuggleError); //$NON-NLS-1$
				// always save preferred tracker.jar
				String jar = preferredTrackerJar == null ? "tracker.jar" : preferredTrackerJar; //$NON-NLS-1$
				control.setValue("tracker_jar", jar); //$NON-NLS-1$
				if (preferredJRE != null)
					control.setValue("java_vm", preferredJRE); //$NON-NLS-1$
				if (preferredPointMassFootprint != null)
					control.setValue("pointmass_footprint", preferredPointMassFootprint); //$NON-NLS-1$
				if (preferredMemorySize > -1) // -1 by default
					control.setValue("memory_size", preferredMemorySize); //$NON-NLS-1$
				if (lookAndFeel != null)
					control.setValue("look_feel", lookAndFeel); //$NON-NLS-1$
				if (prelaunchExecutables.length > 0)
					control.setValue("run", prelaunchExecutables); //$NON-NLS-1$
				if (preferredLocale != null)
					control.setValue("locale", preferredLocale); //$NON-NLS-1$
				if (preferredDecimalSeparator != null)
					control.setValue("decimal_separator", preferredDecimalSeparator); //$NON-NLS-1$
				if (preferredFontLevel > 0) {
					control.setValue("font_size", preferredFontLevel); //$NON-NLS-1$
				}
				if (preferredFontLevelPlus > 0) {
					control.setValue("font_size_plus", preferredFontLevelPlus); //$NON-NLS-1$
				}
				if (ResourceLoader.getOSPCache() != null) {
					File cache = ResourceLoader.getOSPCache();
					control.setValue("cache", cache.getPath()); //$NON-NLS-1$
				}
				control.setValue("upgrade_interval", checkForUpgradeInterval); //$NON-NLS-1$
				int lastChecked = (int) (lastMillisChecked / 1000L);
				control.setValue("last_checked", lastChecked); //$NON-NLS-1$
				if (latestVersion != null) {
					control.setValue("latest_version", latestVersion); //$NON-NLS-1$
				}
				JFileChooser chooser = VideoIO.getChooser();
				File file = chooser.getCurrentDirectory();
				String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
				if (!file.getAbsolutePath().equals(userDir)) // user.dir by default
					control.setValue("file_chooser_directory", XML.getAbsolutePath(file)); //$NON-NLS-1$

				// video_engine--used by version 4.75+
				if (!VideoIO.getPreferredExportExtension().equals(VideoIO.DEFAULT_PREFERRED_EXPORT_EXTENSION))
					control.setValue("export_extension", VideoIO.getPreferredExportExtension()); //$NON-NLS-1$
				if (!ExportZipDialog.preferredExtension.equals(VideoIO.DEFAULT_VIDEO_EXTENSION))
					control.setValue("zip_export_extension", ExportZipDialog.preferredExtension); //$NON-NLS-1$

				if (recentFilesSize != 6) // 6 items by default
					control.setValue("max_recent", recentFilesSize); //$NON-NLS-1$
				if (!recentFiles.isEmpty()) // empty by default
					control.setValue("recent_files", recentFiles); //$NON-NLS-1$
				if (preferredAutoloadSearchPaths != null) {
					// added Dec 2104
					control.setValue("autoload_search_paths", preferredAutoloadSearchPaths); //$NON-NLS-1$
				}
				if (!autoloadMap.isEmpty()) {
					// added Dec 2104
					String[][] autoloadData = new String[autoloadMap.size()][];
					int i = 0;
					for (String filePath : autoloadMap.keySet()) {
						String[] functions = autoloadMap.get(filePath);
						String[] fileAndFunctions = new String[functions.length + 1];
						fileAndFunctions[0] = filePath;
						System.arraycopy(functions, 0, fileAndFunctions, 1, functions.length);
						autoloadData[i] = fileAndFunctions;
						i++;
					}
					control.setValue("autoload_exclusions", autoloadData); //$NON-NLS-1$
				}
				if (!dataFunctionControlStrings.isEmpty()) {
					// deprecated Dec 2014: this is for legacy preferences
					control.setValue("data_functions", dataFunctionControlStrings); //$NON-NLS-1$
				}
				if (defaultConfig != null && !areEqual(defaultConfig, getFullConfig())) { // defaultConfig by default
					Configuration config = new Configuration(defaultConfig);
					control.setValue("configuration", config); //$NON-NLS-1$
				}
			}

			/**
			 * Creates a new object.
			 *
			 * @param control the XMLControl with the object data
			 * @return the newly created object
			 */
			@Override
			public Object createObject(XMLControl control) {
				return new Preferences();
			}

			/**
			 * Loads an object with data from an XMLControl.
			 *
			 * @param control the control
			 * @param obj     the object
			 * @return the loaded object
			 */
			@Override
			@SuppressWarnings("unchecked")
			public Object loadObject(XMLControl control, Object obj) {
				Level logLevel = OSPLog.parseLevel(control.getString("log_level")); //$NON-NLS-1$
				if (logLevel != null) {
					preferredLogLevel = logLevel;
					OSPLog.setLevel(logLevel);
					if (logLevel == Level.ALL) {
						OSPLog.showLogInvokeLater();
					}
				}
				isRadians = control.getBoolean("radians"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("mark_current_frame")) //$NON-NLS-1$
					markAtCurrentFrame = control.getBoolean("mark_current_frame"); //$NON-NLS-1$
				scrubMouseWheel = control.getBoolean("scrub_mousewheel"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("enable_autofill")) //$NON-NLS-1$
					enableAutofill = control.getBoolean("enable_autofill"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("show_gaps")) //$NON-NLS-1$
					showGaps = control.getBoolean("show_gaps"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("center_stick")) //$NON-NLS-1$
					centerCalibrationStick = control.getBoolean("center_stick"); //$NON-NLS-1$
				isXuggleFast = !control.getBoolean("xuggle_smooth"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("trail_length")) { //$NON-NLS-1$
					String name = control.getString("trail_length"); //$NON-NLS-1$
					for (int i = 0; i < TToolBar.trailLengthNames.length; i++) {
						if (TToolBar.trailLengthNames[i].equals(name))
							preferredTrailLengthIndex = i;
					}
				}
				if (control.getPropertyNamesRaw().contains("warn_no_engine")) //$NON-NLS-1$
					warnNoVideoEngine = control.getBoolean("warn_no_engine"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("warn_xuggle_error")) //$NON-NLS-1$
					warnXuggleError = control.getBoolean("warn_xuggle_error"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("warn_variable_frame_duration")) //$NON-NLS-1$
					warnVariableDuration = control.getBoolean("warn_variable_frame_duration"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("show_hints")) { //$NON-NLS-1$
					showHintsByDefault = control.getBoolean("show_hints"); //$NON-NLS-1$
					showHints = showHintsByDefault;
					startupHintShown = !showHints;
				}
				if (control.getPropertyNamesRaw().contains("java_vm")) { //$NON-NLS-1$
					preferredJRE = control.getString("java_vm"); //$NON-NLS-1$
					if (OSPRuntime.getJavaFile(preferredJRE) == null) {
						preferredJRE = null;
					}
				}
				preferredPointMassFootprint = control.getString("pointmass_footprint"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("memory_size")) //$NON-NLS-1$
					requestedMemorySize = control.getInt("memory_size"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("look_feel")) //$NON-NLS-1$
					lookAndFeel = control.getString("look_feel"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("decimal_separator")) { //$NON-NLS-1$
					preferredDecimalSeparator = control.getString("decimal_separator"); //$NON-NLS-1$
					OSPRuntime.setPreferredDecimalSeparator(preferredDecimalSeparator);
				}
				if (control.getPropertyNamesRaw().contains("run")) //$NON-NLS-1$
					prelaunchExecutables = (String[]) control.getObject("run"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("locale")) //$NON-NLS-1$
					setPreferredLocale(control.getString("locale")); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("font_size")) { //$NON-NLS-1$
					preferredFontLevel = control.getInt("font_size"); //$NON-NLS-1$
					preferredFontLevelPlus = control.getInt("font_size_plus"); //$NON-NLS-1$
					if (preferredFontLevelPlus == Integer.MIN_VALUE) {
						preferredFontLevelPlus = 0;
					}
				}
				// set cache only if it has not yet been set
				if (ResourceLoader.getOSPCache() == null) {
					setCache(control.getString("cache")); //$NON-NLS-1$
				}
				if (control.getPropertyNamesRaw().contains("upgrade_interval")) { //$NON-NLS-1$
					checkForUpgradeInterval = control.getInt("upgrade_interval"); //$NON-NLS-1$
					lastMillisChecked = control.getInt("last_checked") * 1000L; //$NON-NLS-1$
				}
				latestVersion = control.getString("latest_version"); //$NON-NLS-1$

				if (control.getPropertyNamesRaw().contains("file_chooser_directory")) //$NON-NLS-1$
					OSPRuntime.chooserDir = control.getString("file_chooser_directory"); //$NON-NLS-1$

				// preferred video engine
				VideoIO.setPreferredExportExtension(control.getString("export_extension")); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("zip_export_extension")) //$NON-NLS-1$
					ExportZipDialog.preferredExtension = control.getString("zip_export_extension"); //$NON-NLS-1$

				if (control.getPropertyNamesRaw().contains("max_recent")) //$NON-NLS-1$
					recentFilesSize = control.getInt("max_recent"); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("recent_files")) { //$NON-NLS-1$
					ArrayList<?> recent = ArrayList.class.cast(control.getObject("recent_files")); //$NON-NLS-1$
					for (Object next : recent) {
						addRecent(next.toString(), true); // add at end
					}
				}
				// added Dec 2104
				preferredAutoloadSearchPaths = (String[]) control.getObject("autoload_search_paths"); //$NON-NLS-1$
				// load autoload_exclusions: added Dec 2014
				if (control.getPropertyNamesRaw().contains("autoload_exclusions")) { //$NON-NLS-1$
					String[][] autoloadData = (String[][]) control.getObject("autoload_exclusions"); //$NON-NLS-1$
					for (String[] next : autoloadData) {
						String filePath = XML.forwardSlash(next[0]);
						String[] functions = new String[next.length - 1];
						System.arraycopy(next, 1, functions, 0, functions.length);
						autoloadMap.put(filePath, functions);
					}
				}

				// load autoloadable data function strings (deprecated Dec 2014: this is for
				// legacy files)
				if (control.getPropertyNamesRaw().contains("data_functions")) { //$NON-NLS-1$
					Collection<String> autoloads = (Collection<String>) control.getObject("data_functions"); //$NON-NLS-1$
					dataFunctionControlStrings.addAll(autoloads);
				}

				XMLControl child = control.getChildControl("configuration"); //$NON-NLS-1$
				if (child != null) {
					Configuration config = (Configuration) child.loadObject(null);
					setDefaultConfig(config.enabled);
				}
				// always load "tracker_jar"
				preferredTrackerJar = control.getString("tracker_jar"); //$NON-NLS-1$
				return obj;
			}
		}
	}

	// dataFunctionControls  
	
	// BH ? Allow in SwingJS?
	
	static private Collection<String> dataFunctionControlStrings = new HashSet<String>();
	static private Map<String, ArrayList<XMLControl>> dataFunctionControls = new TreeMap<String, ArrayList<XMLControl>>();

	public static boolean haveDataFunctions() {
		return (!allowDataFunctionControls ? false : !dataFunctionControlStrings.isEmpty() 
			|| !dataFunctionControls.isEmpty());
	}

	/**
	 * @j2sIgnore
	 * 
	 * @param trackType
	 * @param panel
	 */
	public static void loadControlStringObjects(Class<?> trackType, FunctionPanel panel) {
		// load from Strings read from tracker.prefs (deprecated Dec 2014)
		for (String xml : Tracker.dataFunctionControlStrings) {
			XMLControl control = new XMLControlElement(xml);
			// determine what track type the control is for
			Class<?> controlTrackType = null;
			try {
				controlTrackType = Class.forName(control.getString("description")); //$NON-NLS-1$ );
			} catch (Exception ex) {
			}

			if (controlTrackType == trackType) {
				control.loadObject(panel);
			}
		}
	}

	/**
	 * @j2sIgnore
	 * 
	 * @param reload
	 */
	public static void loadControlStrings(Runnable reload) {
		if (dataFunctionControlStrings.isEmpty())
			return;
		// convert and save in user platform-dependent default search directory
		ArrayList<String> searchPaths = OSPRuntime.getDefaultSearchPaths();
		final String directory = searchPaths.size() > 0 ? searchPaths.get(0) : null;
		if (directory != null) {
			SwingUtilities.invokeLater(() -> {
				int response = JOptionPane.showConfirmDialog(null,
						TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Message1") //$NON-NLS-1$
								+ "\n" //$NON-NLS-1$
								+ TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Message2") //$NON-NLS-1$
								+ "\n\n" //$NON-NLS-1$
								+ TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Message3"), //$NON-NLS-1$
						TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Title"), //$NON-NLS-1$
						JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					TrackDataBuilder builder = new TrackDataBuilder(new TrackerPanel());
					int i = 0;
					for (String next : dataFunctionControlStrings) {
						XMLControl panelControl = new XMLControlElement(next);
						DataFunctionPanel panel = new DataFunctionPanel(new DatasetManager());
						panelControl.loadObject(panel);
						builder.addPanelWithoutAutoloading("panel" + i, panel); //$NON-NLS-1$
						i++;
					}
					File file = new File(directory, "TrackerConvertedAutoloadFunctions.xml"); //$NON-NLS-1$
					XMLControl control = new XMLControlElement(builder);
					control.write(file.getAbsolutePath());
					dataFunctionControlStrings.clear();
					reload.run();
				}
			});
		}
	}

	/**
	 * @j2sIgnore
	 * 
	 * @param trackType
	 * @param panel
	 */
	public static void loadControls(Class<?> trackType, FunctionPanel panel) {
		for (String path : dataFunctionControls.keySet()) {
			ArrayList<XMLControl> controls = dataFunctionControls.get(path);
			for (XMLControl control : controls) {
				// determine what track type the control is for
				Class<?> controlTrackType = null;
				try {
					controlTrackType = Class.forName(control.getString("description")); //$NON-NLS-1$ );
				} catch (Exception ex) {
				}

				if (controlTrackType == trackType) {
					// copy the control for modification if any functions are autoload_off
					XMLControl copyControl = new XMLControlElement(control);
					eliminateExcludedFunctions(copyControl, path);
					// change duplicate function names without requiring user confirmation
					FunctionEditor editor = panel.getFunctionEditor();
					boolean confirmChanges = editor.getConfirmChanges();
					editor.setConfirmChanges(false);
					copyControl.loadObject(panel);
					editor.setConfirmChanges(confirmChanges);
				}
			}
		}

	}

	/**
	 * 
	 * @j2sIgnore
	 * 
	 * Eliminates excluded function entries from a DataFunctionPanel XMLControl.
	 * Typical (but incomplete) control:
	 * 
	 * <object class="org.opensourcephysics.tools.DataFunctionPanel">
	 * <property name="description" type=
	 * "string">org.opensourcephysics.cabrillo.tracker.PointMass</property>
	 * <property name="functions" type="collection" class="java.util.ArrayList">
	 * <property name="item" type="array" class="[Ljava.lang.String;">
	 * <property name="[0]" type="string">Ug</property>
	 * <property name="[1]" type="string">m*g*y</property> </property> </property>
	 * <property name="autoload_off_Ug" type="boolean">true</property> </object>
	 *
	 * @param panelControl the XMLControl to modify
	 * @param filePath     the path to the XML file read by the XMLControl
	 */
	private static void eliminateExcludedFunctions(XMLControl panelControl, String filePath) {
		for (XMLProperty functions : panelControl.getPropsRaw()) {
			if (functions.getPropertyName().equals("functions")) { //$NON-NLS-1$
				java.util.List<Object> items = functions.getPropertyContent();
				ArrayList<XMLProperty> toRemove = new ArrayList<XMLProperty>();
				for (Object child : items) {
					XMLProperty item = (XMLProperty) child;
					XMLProperty nameProp = (XMLProperty) item.getPropertyContent().get(0);
					String functionName = (String) nameProp.getPropertyContent().get(0);
					if (isFunctionExcluded(filePath, functionName)) {
						toRemove.add(item);
					}
				}
				for (XMLProperty next : toRemove) {
					items.remove(next);
				}
			}
		}
	}

	/**
	 * Determines if a named function is excluded from autoloading.
	 *
	 * @param filePath     the path to the file defining the function
	 * @param functionName the function name
	 * @return true if the function is excluded
	 */
	private static boolean isFunctionExcluded(String filePath, String functionName) {
		String[] functions = autoloadMap.get(filePath);
		if (functions == null)
			return false;
		for (String name : functions) {
			if (name.equals("*")) //$NON-NLS-1$
				return true;
			if (name.equals(functionName))
				return true;
		}
		return false;
	}

}
