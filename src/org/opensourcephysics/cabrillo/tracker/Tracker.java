/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.*;

/**
 * This is the default Tracker application.
 *
 * @author Douglas Brown
 */
public class Tracker {

  static {
    XML.setLoader(Preferences.class, new Preferences.Loader());
  }

  // define static constants
  /** tracker version and copyright */
  public static final String VERSION = "5.1.0alpha"; //$NON-NLS-1$
  public static final String COPYRIGHT = "Copyright (c) 2018 Douglas Brown"; //$NON-NLS-1$
  /** the tracker icon */
  public static final ImageIcon TRACKER_ICON = new ImageIcon(
      Tracker.class.getResource("resources/images/tracker_icon_32.png")); //$NON-NLS-1$
  /** a larger tracker icon */
  public static final ImageIcon TRACKER_ICON_256 = new ImageIcon(
      Tracker.class.getResource("resources/images/tracker_icon_256.png")); //$NON-NLS-1$

	static final String THETA = TeXParser.parseTeX("$\\theta"); //$NON-NLS-1$
	static final String OMEGA = TeXParser.parseTeX("$\\omega"); //$NON-NLS-1$
	static final String ALPHA = TeXParser.parseTeX("$\\alpha"); //$NON-NLS-1$
	static final String DEGREES = "°"; //$NON-NLS-1$
	static final String SQUARED = "\u00b2"; //$NON-NLS-1$
	static final String DOT = "\u00b7"; //$NON-NLS-1$
  static final Level DEFAULT_LOG_LEVEL = ConsoleLevel.OUT_CONSOLE;
  
  // for testing
  static boolean timeLogEnabled = false;
  static boolean testOn = false;
  
  // define static fields
  static String trackerHome;
  static String[] fullConfig =
  	{"file.new", "file.open", "file.close",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  	"file.import", "file.export", "file.save",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "file.saveAs", "file.print", "file.library", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "edit.copyObject", "edit.copyData", "edit.copyImage",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "edit.paste", "edit.matSize",  //$NON-NLS-1$ //$NON-NLS-2$
	  "edit.clear", "edit.undoRedo", "video.import",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "video.close", "video.visible", "video.filters",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "pageView.edit", "notes.edit", "new.pointMass", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	  "new.cm", "new.vector", "new.vectorSum",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "new.lineProfile", "new.RGBRegion",  //$NON-NLS-1$ //$NON-NLS-2$
	  "new.analyticParticle", "new.clone", "new.circleFitter", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "new.dynamicParticle", "new.dynamicTwoBody",  //$NON-NLS-1$ //$NON-NLS-2$ 
	  "new.dataTrack", "new.tapeMeasure", "new.protractor",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "calibration.stick", "calibration.tape", //$NON-NLS-1$ //$NON-NLS-2$
	  "calibration.points", "calibration.offsetOrigin", //$NON-NLS-1$ //$NON-NLS-2$
	  "track.name", "track.description",  //$NON-NLS-1$ //$NON-NLS-2$
	  "track.color", "track.footprint",  //$NON-NLS-1$ //$NON-NLS-2$ 
	  "track.visible", "track.locked",  //$NON-NLS-1$ //$NON-NLS-2$
	  "track.delete", "track.autoAdvance",  //$NON-NLS-1$ //$NON-NLS-2$ 
	  "track.markByDefault", "track.autotrack",  //$NON-NLS-1$ //$NON-NLS-2$
	  "model.stamp", "help.diagnostics", "coords.locked",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	  "coords.origin", "coords.angle", "data.algorithm",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "coords.scale", "coords.refFrame", "button.x",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "button.v", "button.a", "button.trails",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	  "button.labels", "button.stretch", "button.clipSettings",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "button.xMass", "button.axes", "button.path", "button.drawing",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	  "number.formats", "number.units", "text.columns", "plot.compare",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	  "config.saveWithData", "data.builder", "data.tool"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  static Set<String> defaultConfig;
  static boolean ffmpegCopied;
  static String[] mainArgs;
  static JFrame splash;
  static Icon trackerLogoIcon, ospLogoIcon;
  static JLabel tipOfTheDayLabel;
  static JProgressBar progressBar;
  static String counterPath = "http://physlets.org/tracker/counter/counter.php?"; //$NON-NLS-1$
  static String newerVersion; // new version available if non-null
  static boolean checkedForNewerVersion = false; // true if checked for new version
  static String trackerWebsite = "physlets.org/tracker"; //$NON-NLS-1$
  static String trackerDownloadFolder = "/upgrade/"; //$NON-NLS-1$
  static String author = "Douglas Brown"; //$NON-NLS-1$
  static String osp = "Open Source Physics"; //$NON-NLS-1$
  static AbstractAction aboutFFMPegAction, aboutThreadsAction;
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
  protected static Locale[] locales;
  protected static Object[][] incompleteLocales;
  static Locale defaultLocale;
  static ArrayList<String> checkForUpgradeChoices;
  static Map<String, Integer> checkForUpgradeIntervals;
  static Collection<String> dataFunctionControlStrings = new HashSet<String>();
  static Collection<String> initialAutoloadSearchPaths = new TreeSet<String>();
  static Map<String, ArrayList<XMLControl>> dataFunctionControls = new TreeMap<String, ArrayList<XMLControl>>();
  static java.io.FileFilter xmlFilter;
  static Registry registry; // used for RMI communication with EJS
  static DataTrackTool dataTrackTool; // used for RMI communication with EJS
  static boolean toolRegistered, toolNotFound;
  
  // user-settable preferences saved/loaded by Preferences class
  static Level preferredLogLevel = DEFAULT_LOG_LEVEL;
  static boolean showHintsByDefault = true;
  static int recentFilesSize = 6;
  static int preferredMemorySize = -1;
  static String lookAndFeel, preferredLocale, preferredDecimalSeparator, additionalDecimalSeparators;
  static String preferredJRE, preferredTrackerJar, preferredPointMassFootprint;
  static int checkForUpgradeInterval = 0;
  static int preferredFontLevel = 0, preferredFontLevelPlus = 0;
  static boolean isRadians, isVideoFast;
  static boolean warnFFMPegError=true, warnNoVideoEngine=true;
  static boolean warnVariableDuration=true;
  static String[] prelaunchExecutables = new String[0];
  static Map<String, String[]> autoloadMap = new TreeMap<String, String[]>();
  static String[] preferredAutoloadSearchPaths;
  static boolean markAtCurrentFrame = true;
  static boolean scrubMouseWheel, centerCalibrationStick, enableAutofill, showGaps, hideLabels;
  static int trailLengthIndex = TToolBar.trailLengths.length-2;

  // the only instance field!
  private TFrame frame;

  static {
//  	OSPLog.setLevel(ConsoleLevel.ALL);
//  	OSPLog.showLog();
  	defaultLocale = Locale.getDefault();
		trackerHome = System.getenv("TRACKER_HOME"); //$NON-NLS-1$
		if (trackerHome==null) {
			try {
				trackerHome = TrackerStarter.findTrackerHome(false);
			} catch (Exception e1) {
			}
		}
    // set system properties for Mac OSX look and feel
//    System.setProperty("apple.laf.useScreenMenuBar", "true");
//    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tracker");
  	
  	// get logo icons with ResourceLoader so launch jar file is identified
    String imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/tracker_logo.png"; //$NON-NLS-1$
    trackerLogoIcon = ResourceLoader.getIcon(imageFile);
    imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/osp_logo_url.png"; //$NON-NLS-1$
    ospLogoIcon = ResourceLoader.getIcon(imageFile);
    
    // create grab cursor
	  imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/grab.gif";  //$NON-NLS-1$
	  Image grab = ResourceLoader.getImage(imageFile); 
	  grabCursor = GUIUtils.createCustomCursor(grab, new Point(14, 10), "Grab", Cursor.HAND_CURSOR); //$NON-NLS-1$
    
  	// create static objects AFTER they are defined above
    locales = new Locale[] { 
			Locale.ENGLISH, 
			new Locale("ar"), // arabic //$NON-NLS-1$
			new Locale("cs"), // czech //$NON-NLS-1$
			new Locale("da"), // danish //$NON-NLS-1$
			Locale.GERMAN,
			new Locale("el", "GR"), // greek //$NON-NLS-1$ //$NON-NLS-2$
			new Locale("es"), // spanish //$NON-NLS-1$
			new Locale("fi"), // finnish //$NON-NLS-1$
			Locale.FRENCH,
			new Locale("hu", "HU"), // hungarian //$NON-NLS-1$ //$NON-NLS-2$
			new Locale("in"), // indonesian //$NON-NLS-1$
			Locale.ITALIAN,
			new Locale("iw", "IL"), // hebrew //$NON-NLS-1$ //$NON-NLS-2$
			new Locale("ko"), // korean //$NON-NLS-1$
			new Locale("ms", "MY"), // malaysian //$NON-NLS-1$ //$NON-NLS-2$ 
			new Locale("nl", "NL"), // dutch //$NON-NLS-1$ //$NON-NLS-2$
			new Locale("pl"), // polish //$NON-NLS-1$
			new Locale("pt", "BR"), // Brazil portuguese //$NON-NLS-1$ //$NON-NLS-2$ 
			new Locale("pt", "PT"), // Portugal portuguese //$NON-NLS-1$ //$NON-NLS-2$ 
			new Locale("sk"), // slovak //$NON-NLS-1$
			new Locale("sl"), // slovenian //$NON-NLS-1$
			new Locale("sv"), // swedish //$NON-NLS-1$
			new Locale("th", "TH"), // Thailand thai //$NON-NLS-1$ //$NON-NLS-2$ 
			new Locale("tr"), // turkish //$NON-NLS-1$
			new Locale("vi", "VN"), // vietnamese //$NON-NLS-1$ //$NON-NLS-2$
			Locale.CHINA, // simplified chinese
			Locale.TAIWAN}; // traditional chinese
    
    // pig last updated March 2018
    incompleteLocales = new Object[][] { 
			{new Locale("cs"), "2013"}, // czech //$NON-NLS-1$ //$NON-NLS-2$
			{new Locale("fi"), "2013"}, // finnish //$NON-NLS-1$ //$NON-NLS-2$
			{new Locale("sk"), "2011"}, // slovak //$NON-NLS-1$ //$NON-NLS-2$
			{new Locale("in"), "2013"}};// indonesian //$NON-NLS-1$ //$NON-NLS-2$

    setDefaultConfig(getFullConfig());
  	loadPreferences();
  	// load current version after a delay to allow video engines to load
  	// and every 24 hours thereafter (if program is left running)
    Timer timer = new Timer(86400000, new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
			  	Runnable runner = new Runnable() {
			  		public void run() {
			  			checkedForNewerVersion = false;
			  			loadCurrentVersion(false, true);
			  		}
			  	};
			    Thread opener = new Thread(runner);
			    opener.setPriority(Thread.NORM_PRIORITY);
			    opener.setDaemon(true);
			    opener.start();
			 }
		 });
    timer.setInitialDelay(10000);
		timer.setRepeats(true);
		timer.start();

		xmlFilter = new java.io.FileFilter() {
      // accept only *.xml files.
      public boolean accept(File f) {
        if (f==null || f.isDirectory()) return false;
        String ext = XML.getExtension(f.getName());
        if (ext!=null && "xml".equals(ext.toLowerCase())) return true; //$NON-NLS-1$
        return false;
      }
    };
    autoloadDataFunctions();
  	
  	// check for upgrade intervals
    checkForUpgradeChoices = new ArrayList<String>();
  	checkForUpgradeIntervals = new HashMap<String, Integer>();
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
    contentPane.setBorder(BorderFactory.createBevelBorder(
    		BevelBorder.RAISED, grayblue, darkgrayblue));
    splash.setContentPane(contentPane);
    MouseInputAdapter splashMouseListener = new MouseInputAdapter() {
    	Point mouseLoc;
    	Point splashLoc;
      public void mousePressed(MouseEvent e) {
      	splashLoc = splash.getLocation(); // original screen position of splash
      	mouseLoc = e.getPoint(); // original screen position of mouse
      	mouseLoc.x += splashLoc.x;
      	mouseLoc.y += splashLoc.y;
      }
      public void mouseDragged(MouseEvent e) {
      	Point loc = splash.getLocation();
      	loc.x += e.getPoint().x;
      	loc.y += e.getPoint().y;
      	splash.setLocation(splashLoc.x+loc.x-mouseLoc.x, splashLoc.y+loc.y-mouseLoc.y);
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
    tip += " "+TrackerRes.getString("TMenuBar.Menu.Help"); //$NON-NLS-1$ //$NON-NLS-2$
    tip += "|"+TrackerRes.getString("TMenuBar.MenuItem.GettingStarted"); //$NON-NLS-1$ //$NON-NLS-2$
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
    contentPane.add(center, BorderLayout.CENTER);

    // version south
    String vers = author+"   "+osp+"   Ver "+VERSION; //$NON-NLS-1$ //$NON-NLS-2$
		if (VERSION.length()>7 || testOn) vers += " BETA"; //$NON-NLS-1$
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
    
    splash.pack();
    Dimension size = splash.getSize();
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = dim.width / 2;
    int y = 3*dim.height/5;  // below center
    splash.setLocation(x-size.width/2, y-size.height/2);

  	// set up videos extensions to extract from jars
  	// this list should agree with ffmpeg video types below
  	for (String ext: VideoIO.VIDEO_EXTENSIONS) { // {"mov", "avi", "mp4"}
      ResourceLoader.addExtractExtension(ext);
  	}
    
    // add FFMPeg video types, if available, using reflection
  	try {
			String ffmpegIOName = "org.opensourcephysics.media.ffmpeg.FFMPegIO"; //$NON-NLS-1$
			Class<?> ffmpegIOClass = Class.forName(ffmpegIOName);
			Method method = ffmpegIOClass.getMethod("registerWithVideoIO", (Class[]) null);  //$NON-NLS-1$
			method.invoke(null, (Object[]) null);
		} catch (Exception ex) {
		}    
    
    VideoIO.setDefaultXMLExtension("trk"); //$NON-NLS-1$
    
    // create pdf help button
    pdfHelpButton = new JButton(TrackerRes.getString("Tracker.Button.PDFHelp")); //$NON-NLS-1$
    pdfHelpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
        	java.net.URL url = new java.net.URL("https://"+trackerWebsite+pdfHelpPath); //$NON-NLS-1$
        	org.opensourcephysics.desktop.OSPDesktop.displayURL(url.toString());
        }
        catch(Exception ex) { ex.printStackTrace(); }
      }
    });
    
    // find Java VMs in background thread so they are ready when needed
    Runnable runner = new Runnable() {
    	public void run() {
		    JREFinder.getFinder().getJREs(32);    		
    	}
    };
    new Thread(runner).start();
  }

  /**
   * Gets the shared Tracker for single-VM use.
   *
   * @return the shared Tracker
   */
  public static Tracker getTracker() {
    if (sharedTracker == null) {
    	OSPLog.fine("creating shared Tracker"); //$NON-NLS-1$
      sharedTracker = new Tracker(null, false, false);
    }
    return sharedTracker;
  }

  /**
   * Constructs Tracker with a blank tab and splash.
   */
  public Tracker() {
    this(null, true, true);
  }

  /**
	 * Constructs Tracker with a video.
	 * 
	 * @param video the video
	 */
  public Tracker(Video video) {
    createFrame();
    // add a tracker panel with the video
    TrackerPanel trackerPanel = new TrackerPanel(video);
    frame.addTab(trackerPanel);
  }

  /**
   * Constructs Tracker and loads the named xml files.
   *
   * @param names an array of xml, video or zip file names
   */
  private Tracker(String[] names, boolean addTabIfEmpty, boolean showSplash) {
  	// set font level resize and center splash frame
  	FontSizer.setFonts(splash, FontSizer.getLevel());
  	splash.pack();
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - splash.getBounds().width) / 2;
    int y = (dim.height - splash.getBounds().height) / 2;
    splash.setLocation(x, y);
    splash.setVisible(showSplash);
    createFrame();
    Tracker.setProgress(5);
    if (names != null) {
      // parse file names
      for (int i = 0; i < names.length; i++) {
        if (names[i] == null) continue;
        // set default root path to path of first .trk or .trz file opened
        if ((names[i].endsWith(".trk") || names[i].endsWith(".trz")) //$NON-NLS-1$ //$NON-NLS-2$
        		&& names[i].indexOf("/") != -1 //$NON-NLS-1$
            && rootXMLPath.equals("")) { //$NON-NLS-1$
          rootXMLPath = names[i].substring(0, names[i].lastIndexOf("/") + 1); //$NON-NLS-1$
          OSPLog.fine("Setting rootPath: " + rootXMLPath); //$NON-NLS-1$
        }
      	TrackerIO.open(names[i], frame);
      }
    }
    // add an empty tab if requested
    else if (addTabIfEmpty) {
	    TrackerPanel trackerPanel = new TrackerPanel();
	    frame.addTab(trackerPanel);
      JSplitPane pane = frame.getSplitPane(trackerPanel, 0);
      pane.setDividerLocation(frame.defaultRightDivider);
      if (showHints) {
      	startupHintShown = true;
      	trackerPanel.setMessage(TrackerRes.getString("Tracker.Startup.Hint")); //$NON-NLS-1$
      }
	    Tracker.setProgress(100);
    }
  }

  /**
   * Gets the frame.
   *
   * @return the frame
   */
  public TFrame getFrame() {
    return frame;
  }

  /**
   * Creates the TFrame.
   */
  private void createFrame() {
    // create actions
    createActions();
    Tracker.setProgress(5);
		OSPRuntime.setLookAndFeel(true, lookAndFeel);
    frame = new TFrame();
    Diagnostics.setDialogOwner(frame);
    DiagnosticsForFFMPeg.setDialogOwner(frame);
    // set up the Java VM exit mechanism when used as application
    if ( org.opensourcephysics.display.OSPRuntime.applet == null) {
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
        	// save preferences, but first clean up autoloadMap
        	ArrayList<String> dirs = new ArrayList<String>();
        	if (preferredAutoloadSearchPaths!=null) {
        		for (String path: preferredAutoloadSearchPaths) dirs.add(path);
        	}
        	else dirs.addAll(getDefaultAutoloadSearchPaths());
        	
        	for (Iterator<String> it = autoloadMap.keySet().iterator(); it.hasNext();) {
        		String filePath = it.next();
        		String parentPath = XML.getDirectoryPath(filePath);
        		boolean keep = false;
        		for (String dir: dirs) {
        			keep = keep || parentPath.equals(dir);
        		}
        		if (!keep || !new File(filePath).exists()) {
        			it.remove();
        		}
        	}
        	savePreferences();
        	if (frame.libraryBrowser!=null) {
        		boolean canceled = !frame.libraryBrowser.exit();
        		if (canceled) {
						  // exiting is canceled so temporarily change close operation
						  // to DO_NOTHING and return
							final int op = frame.getDefaultCloseOperation();
						  final boolean exit = frame.wishesToExit();
						  frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
						  Runnable runner = new Runnable() {
						    public void run() {
						     if (exit) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						     frame.setDefaultCloseOperation(op);
						    }
						  };
						  EventQueue.invokeLater(runner);
						  return;
        		}
        	}
          // remove all tabs
          for (int i = frame.getTabCount()-1; i >= 0; i--) {
          	// save/close tabs in try/catch block so always closes
            try {
							if (!frame.getTrackerPanel(i).save()) {
							  // exiting is canceled so temporarily change close operation
							  // to DO_NOTHING and return
								final int op = frame.getDefaultCloseOperation();
							  final boolean exit = frame.wishesToExit();
							  frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
							  Runnable runner = new Runnable() {
							    public void run() {
							     if (exit) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							     frame.setDefaultCloseOperation(op);
							    }
							  };
							  EventQueue.invokeLater(runner);
							  return;
							}
							frame.removeTab(frame.getTrackerPanel(i));
						} catch (Exception ex) {
						}
          }
          
          // hide the frame
          frame.setVisible(false);
          
          // unregister the DataTrackTool and inform RMI clients
					dataTrackTool.trackerExiting();
					unregisterRemoteTool(dataTrackTool);

          // exit the system if frame wishes to exit 
          if (frame.wishesToExit() && 
          				frame.getDefaultCloseOperation() == WindowConstants.DISPOSE_ON_CLOSE) {
          	System.exit(0);
          }
        }
      });
    }
  }

//________________________________  static methods ____________________________
  
  /**
   * Compares version strings.
   * 
   * @param ver1 version 1
   * @param ver2 version 2
   * @return 0 if equal, 1 if ver1>ver2, -1 if ver1<ver2
   */
  public static int compareVersions(String ver1, String ver2) {
  	try {
		// deal with null values
		if (ver1 == null || ver2 == null) {
			return 0;
		}
		// typical newer semantic version "4.9.10" or 5.0.0.171230
		// typical older version "4.97"
		String[] v1 = ver1.trim().split("\\."); //$NON-NLS-1$
		String[] v2 = ver2.trim().split("\\."); //$NON-NLS-1$
		// beta version arrays have length 4
		// newer semantic version arrays have length 3
		// older version arrays have length 2

		// truncate beta versions to length 3
		if (v1.length == 4) {
			v1 = new String[]{v1[0], v1[1], v1[2]};
		}
		if (v2.length == 4) {
			v2 = new String[]{v2[0], v2[1], v2[2]};
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
	}catch(Exception e){
  		return 0;
	}
  }
  

  /**
   * Shows the About Tracker dialog.
   */
  public static void showAboutTracker() {
  	String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
  	String vers = Tracker.VERSION;
  	// typical beta version 4.10.0170514
		if (vers.length()>7 || testOn) vers += " BETA"; //$NON-NLS-1$
		String date = OSPRuntime.getLaunchJarBuildDate();
		if (date!=null) 
			vers = vers+"   "+date; //$NON-NLS-1$
    String aboutString = "Tracker "  //$NON-NLS-1$
    		+ vers + newline
        + Tracker.COPYRIGHT + newline
        + "https://"+Tracker.trackerWebsite + newline + newline //$NON-NLS-1$
        + TrackerRes.getString("Tracker.About.ProjectOf") + newline //$NON-NLS-1$
        + "Open Source Physics" + newline //$NON-NLS-1$
        + "www.opensourcephysics.org" + newline; //$NON-NLS-1$
    String translator = TrackerRes.getString("Tracker.About.Translator"); //$NON-NLS-1$
    if (!translator.equals("")) { //$NON-NLS-1$
    	aboutString += newline+TrackerRes.getString("Tracker.About.TranslationBy") //$NON-NLS-1$
    			+" "+ translator + newline; //$NON-NLS-1$
    }
    if (Tracker.trackerHome!=null) {
    	aboutString += newline+TrackerRes.getString("Tracker.About.TrackerHome") //$NON-NLS-1$
    			+newline+ Tracker.trackerHome + newline;
    }
    loadCurrentVersion(true, false);
    if (newerVersion!=null) {
    	aboutString += newline+TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message1") //$NON-NLS-1$
					+" "+newerVersion+" " //$NON-NLS-1$ //$NON-NLS-2$
					+TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message2") //$NON-NLS-1$
					+newline+"https://"+trackerWebsite+newline; //$NON-NLS-1$
    }
    else {
    	aboutString += newline+TrackerRes.getString("PrefsDialog.Dialog.NewVersion.None.Message"); //$NON-NLS-1$
    }
    JOptionPane.showMessageDialog(null,
    															aboutString,
                                  TrackerRes.getString("Tracker.Dialog.AboutTracker.Title"), //$NON-NLS-1$
                                  JOptionPane.INFORMATION_MESSAGE);
  }
  
	/**
	 * Finds data functions in all DataBuilder XMLControl files found in a specified directory.
	 * This returns a map for which the keys are names of DataBuilder xml files and the values
	 * are lists of data functions as String[] {function name, expression, tracktype}
	 * 
	 * @param dirPath the directory path
	 * @return map of file name to list of data functions
	 */
	public static Map<String, ArrayList<String[]>> findDataFunctions(String dirPath) {
		Map<String, ArrayList<String[]>> results = new TreeMap<String, ArrayList<String[]>>();
		if (dirPath==null) return results;
		
		File dir = new File(dirPath);
		if (!dir.exists()) return results;
		
		File[] files = dir.listFiles(xmlFilter);
		if (files!=null) {
			for (File file: files) {
		    XMLControl control = new XMLControlElement(file.getPath());    
		    if (control.failedToRead()) {
		      continue;
		    }

		    Class<?> type = control.getObjectClass();
		    if (type!=null && TrackDataBuilder.class.isAssignableFrom(type)) {
    			ArrayList<String[]> expandedFunctions = new ArrayList<String[]>();

		      // look through XMLControl for data functions            	
	        for (Object next: control.getPropertyContent()) {
	        	if (next instanceof XMLProperty 
	        			&& ((XMLProperty)next).getPropertyName().equals("functions")) { //$NON-NLS-1$
	        		// found DataFunctionPanels
	        		XMLControl[] panels = ((XMLProperty)next).getChildControls();
	        		inner: for (XMLControl panelControl: panels) {
	        			String trackType = panelControl.getString("description"); //$NON-NLS-1$
	        			@SuppressWarnings("unchecked")
								ArrayList<String[]> functions = (ArrayList<String[]>)panelControl.getObject("functions"); //$NON-NLS-1$
	        			if (trackType==null || functions==null || functions.isEmpty()) continue inner;
	        			
	        			// add localized trackType name to function arrays
	        			for (String[] f: functions) {
	        				String[] data = new String[3];
	        				System.arraycopy(f, 0, data, 0, 2);
	        				// use XML.getExtension method to get short name of track type
	        				String trackName = XML.getExtension(trackType);
	        				String localized = TrackerRes.getString(trackName+".Name"); //$NON-NLS-1$
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
    aboutTrackerAction = new AbstractAction(
        TrackerRes.getString("Tracker.Action.AboutTracker"), null) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				showAboutTracker();
      }
    };
    // Tracker README
    readmeAction = new AbstractAction(
        TrackerRes.getString("Tracker.Readme")+"...", null) { //$NON-NLS-1$ //$NON-NLS-2$
			public void actionPerformed(ActionEvent e) {
				if (readmeDialog==null && Tracker.trackerHome!=null) {
		      String slash = System.getProperty("file.separator", "/"); //$NON-NLS-1$//$NON-NLS-2$
	        String path = Tracker.trackerHome+slash+readmeFileName;
	        if (OSPRuntime.isMac()) {
	        	String dir = new File(Tracker.trackerHome).getParent();
	        	path = dir+slash+readmeFileName;
	        }
	        String s = ResourceLoader.getString(path);
	        if (s==null || "".equals(s)) { //$NON-NLS-1$
	        	s = TrackerRes.getString("Tracker.Readme.NotFound")+": "+path; //$NON-NLS-1$ //$NON-NLS-2$
	          JOptionPane.showMessageDialog(null, s,
	          TrackerRes.getString("Tracker.Readme.NotFound"), //$NON-NLS-1$
	          JOptionPane.WARNING_MESSAGE);
	          return;
	        }
					readmeDialog = new JDialog((Frame)null, true);
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
		if (startLogPath!=null) {
	    startLogAction = new AbstractAction(
	        TrackerRes.getString("Tracker.StartLog")+"...", null) { //$NON-NLS-1$ //$NON-NLS-2$
				public void actionPerformed(ActionEvent e) {
					if (startLogDialog==null) {
		        String s = ResourceLoader.getString(startLogPath);
		        if (s==null || "".equals(s)) { //$NON-NLS-1$
		        	s = TrackerRes.getString("Tracker.StartLog.NotFound")+": "+startLogPath; //$NON-NLS-1$ //$NON-NLS-2$
		          JOptionPane.showMessageDialog(null, s,
		          TrackerRes.getString("Tracker.startLogPath.NotFound"), //$NON-NLS-1$
		          JOptionPane.WARNING_MESSAGE);
		          return;
		        }
		        startLogDialog = new JDialog((Frame)null, true);
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

		if (prefsPath!=null) {
	    trackerPrefsAction = new AbstractAction(
	        TrackerRes.getString("Tracker.Prefs.MenuItem.Text")+"...", null) { //$NON-NLS-1$ //$NON-NLS-2$
				public void actionPerformed(ActionEvent e) {
					if (trackerPrefsDialog==null) {
		        String s = ResourceLoader.getString(prefsPath);
		        if (s==null || "".equals(s)) { //$NON-NLS-1$
		        	s = TrackerRes.getString("Tracker.Prefs.NotFound")+": "+prefsPath; //$NON-NLS-1$ //$NON-NLS-2$
		          JOptionPane.showMessageDialog(null, s,
		          TrackerRes.getString("Tracker.Prefs.NotFound"), //$NON-NLS-1$
		          JOptionPane.WARNING_MESSAGE);
		          return;
		        }
		        trackerPrefsDialog = new JDialog((Frame)null, true);
		        trackerPrefsDialog.setTitle(TrackerRes.getString("ConfigInspector.Title")+": "+ //$NON-NLS-1$ //$NON-NLS-2$
		        		XML.forwardSlash(Tracker.prefsPath));
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
					}
					else {
		        String s = ResourceLoader.getString(prefsPath);
		        trackerPrefsTextArea.setText(s);
		        trackerPrefsTextArea.setCaretPosition(0);
					}
					trackerPrefsDialog.setVisible(true);
	      }
	    };
		}

		// about Java
    aboutJavaAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutJava"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Diagnostics.aboutJava();
      }
    };
    aboutFFMPegAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutFFMPeg"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	DiagnosticsForFFMPeg.aboutFFMPeg("Tracker"); //$NON-NLS-1$
      }
    };
    aboutThreadsAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutThreads"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	DiagnosticsForThreads.aboutThreads();
      }
    };
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
    for (String next: fullConfig) {
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
  	if (defaultConfig==null)
  		defaultConfig = getFullConfig();
    TreeSet<String> set = new TreeSet<String>();
    for (String next: defaultConfig) {
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
  	if (defaultConfig==null)
  		defaultConfig = new TreeSet<String>();
  	defaultConfig.clear();
    for (String next: config) {
    	defaultConfig.add(next);
    }
  }
  
  /**
   * Autoloads data functions found in the user home and code base directories.
   * This loads DataFunctionPanel XMLControls into a static collection that is
   * accessed when need by DataBuilder.
   */
  protected static void autoloadDataFunctions() {
  	dataFunctionControls.clear();
	  for (String dirPath: getInitialSearchPaths()) {
			if (dirPath==null) continue;
			
			File dir = new File(dirPath);
			if (!dir.exists()) continue;
			
			File[] files = dir.listFiles(xmlFilter);
			if (files!=null) {
				for (File file: files) {
			    XMLControl control = new XMLControlElement(file.getPath());    
			    if (control.failedToRead()) {
			      continue;
			    }
	
			    Class<?> type = control.getObjectClass();
			    if (type!=null && TrackDataBuilder.class.isAssignableFrom(type)) {
		        for (Object next: control.getPropertyContent()) {
		        	if (next instanceof XMLProperty 
		        			&& ((XMLProperty)next).getPropertyName().equals("functions")) { //$NON-NLS-1$
		        		// found DataFunctionPanels
		        		ArrayList<XMLControl> controls = new ArrayList<XMLControl>();
		        		XMLControl[] panels = ((XMLProperty)next).getChildControls();
		        		inner: for (XMLControl panelControl: panels) {
		        			String trackType = panelControl.getString("description"); //$NON-NLS-1$
		        			@SuppressWarnings("unchecked")
									ArrayList<String[]> functions = (ArrayList<String[]>)panelControl.getObject("functions"); //$NON-NLS-1$
		        			if (trackType==null || functions==null || functions.isEmpty()) 
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
	 * Gets the starting autoload search paths. Search paths may be later modified by the user.
	 * 
	 * @return the search paths
	 */
  protected static Collection<String> getInitialSearchPaths() {
  	if (initialAutoloadSearchPaths.isEmpty()) {
  		if (preferredAutoloadSearchPaths!=null) {
  			for (String next: preferredAutoloadSearchPaths) {
  				initialAutoloadSearchPaths.add(next);
  			}
  		}
  		else {
  			for (String next: getDefaultAutoloadSearchPaths()) {
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
  	if (localeName==null) {
    	Locale.setDefault(defaultLocale);
    	preferredLocale = null;
  	}
  	else for (Locale locale: locales) {
    	if (locale.toString().equals(localeName)) {
      	Locale.setDefault(locale);
      	preferredLocale = localeName;
    		break;
    	}
    }
    // set the default decimal separator
    OSPRuntime.setDefaultDecimalSeparator(
    		new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator());
  }

  /**
   * Sets the cache path.
   * 
   * @param cachePath the cache path
   */
  protected static void setCache(String cachePath) {
  	File cacheDir = cachePath==null || cachePath.trim().equals("")? ResourceLoader.getDefaultOSPCache(): new File(cachePath); //$NON-NLS-1$
  	ResourceLoader.setOSPCache(cacheDir);
  }

  /**
   * Checks and updates FFMPeg resources.
   * 
   * @return true if any resources were updated
   */
  protected static boolean updateResources() {
  	boolean updated = false;
    return updated; 	
  }

  /**
   * Determines if two sets contain the same elements.
   * 
   * @param set1 
   * @param set2 
   * @return true if the sets are equal
   */
  protected static boolean areEqual(Set<?> set1, Set<?> set2) {
    for (Object next: set1) {
    	if (!set2.contains(next)) return false;
    }
    for (Object next: set2) {
    	if (!set1.contains(next)) return false;
    }
    return true;
  }
  
  /**
   * Check for upgrades and show a dialog with upgrade info. 
   * Also refresh toolbar associated with TrackerPanel, if any. 
   * 
   * @param trackerPanel a TrackerPanel (may be null)
   */
  protected static void showUpgradeStatus(TrackerPanel trackerPanel) {
		checkedForNewerVersion = false;
		loadCurrentVersion(true, false);
		if (trackerPanel!=null) TTrackBar.getTrackbar(trackerPanel).refresh();
		String message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.None.Message"); //$NON-NLS-1$
		if (Tracker.newerVersion!=null) { // new version available
			message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message1") //$NON-NLS-1$
					+" "+Tracker.newerVersion+" " //$NON-NLS-1$ //$NON-NLS-2$
					+TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message2") //$NON-NLS-1$
					+XML.NEW_LINE+"https://"+Tracker.trackerWebsite; //$NON-NLS-1$
		}
		TFrame frame = trackerPanel==null? null: trackerPanel.getTFrame();
		JOptionPane.showMessageDialog(frame, 
				message, 
				TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Title"),  //$NON-NLS-1$
				JOptionPane.INFORMATION_MESSAGE);  	
  }
  
  /**
   * Loads the current (latest) Tracker version number and compares it with this version.
   * 
   * @param ignoreInterval true to load/compare immediately
   * @param logToFile true to log in to the PHP counter 
   */
  protected static void loadCurrentVersion(boolean ignoreInterval, boolean logToFile) {  	
		if (!ResourceLoader.isURLAvailable("http://www.opensourcephysics.org")) { //$NON-NLS-1$
			return;
		}
  	if (checkedForNewerVersion) return;
		checkedForNewerVersion = true;
		
  	// check to see how much time has passed
  	long millis = System.currentTimeMillis();
  	double days = (millis-lastMillisChecked)/86400000.0;
  	
  	// don't log to file more often than every 2 hours no matter what
  	if (logToFile && days<0.0833) logToFile = false;
  	
	 	// send data as page name to get latest version from PHP script
	  // typical pre-4.97 version: "4.90" or "4.61111227"
  	// typical post-4.97 version: "4.9.8" or "4.10.0170504" or "5.0.1"
		String pageName = getPHPPageName(logToFile);
		String latestVersion = loginGetLatestVersion(pageName);
		
  	if (!ignoreInterval) {
	  	// check to see if upgrade interval has passed
  		double interval = checkForUpgradeInterval==0? 0.0833: checkForUpgradeInterval;
	  	if (days<interval) {
	  		return;
	  	}
  	}
  	
  	// interval has passed or ignored, so check for upgrades  	
		lastMillisChecked = millis;
		int result = compareVersions(latestVersion, VERSION);
		if (result>0) { // newer version available
			newerVersion = latestVersion;
			TFrame tFrame = null;
	    Frame[] frames = Frame.getFrames();
	    for(int i = 0, n = frames.length; i<n; i++) {
	       if (frames[i] instanceof TFrame) {
	      	 tFrame = (TFrame)frames[i];
	   			 TrackerPanel trackerPanel = tFrame.getTrackerPanel(tFrame.getSelectedTab());
	  			 if (trackerPanel!=null) {
	  				 TTrackBar trackbar = TTrackBar.getTrackbar(trackerPanel);
	  				 trackbar.refresh();
	  			 }
	       }
	    }
		}		
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
      String engine = VideoIO.getEngine();
    	String os = "unknownOS"; //$NON-NLS-1$
	    try { // system properties may not be readable in some environments
	      os = System.getProperty("os.name", "unknownOS").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
	    } catch(SecurityException ex) {}
      os = os.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
      if (os.indexOf("windows")>-1) { //$NON-NLS-1$
      	os = "windows"; //$NON-NLS-1$
      }
      page = "log_"+VERSION+"_"+os+"_"+engine; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (!"".equals(language)) { //$NON-NLS-1$
	      if (!"".equals(country)) { //$NON-NLS-1$
	      	language += "-"+country; //$NON-NLS-1$
	      }
      	page += "_"+language; //$NON-NLS-1$
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
  	String path = counterPath+"page="+page; //$NON-NLS-1$
    try {
			URL url = new URL(path);
			Resource res = new Resource(url);
	    String version = res.getString().trim();
	    OSPLog.finer(path+":   "+version); //$NON-NLS-1$
	    return version;
		} catch (Exception e) {
		}
  	return VERSION;
  }


  /**
   * Loads preferences from a preferences file, if any.
   */
  protected static void loadPreferences() {
  	
  	XMLControl prefsControl = TrackerStarter.findPreferences();
  	if (prefsControl!=null) {
  		prefsPath = prefsControl.getString("prefsPath"); //$NON-NLS-1$
  		if (prefsPath!=null) {
	    	OSPLog.getOSPLog();
	    	OSPLog.info("preferences loaded from "+XML.getAbsolutePath(new File(prefsPath))); //$NON-NLS-1$
  		}
    	prefsControl.loadObject(null);  // the loader itself reads the values
    	return;
  	}

		// unable to find prefs, so write new one(s) if possible
		String recommendedPath = null;
		String fileName = TrackerStarter.PREFS_FILE_NAME;
		if (!OSPRuntime.isWindows()) {
			// add leading dot to hide file on OSX and Linux
			fileName = "."+fileName; //$NON-NLS-1$
		}
  	for (String path: OSPRuntime.getDefaultSearchPaths()) {
      String prefs_path = new File(path, fileName).getAbsolutePath();
      if (recommendedPath==null) recommendedPath = prefs_path;
      else recommendedPath += " or "+prefs_path; //$NON-NLS-1$
    	XMLControl control = new XMLControlElement(new Preferences());
      if (control.write(prefs_path)!=null) {
      	prefsPath = prefs_path;
	    	OSPLog.getOSPLog();
	    	OSPLog.info("wrote new preferences file to "+XML.getAbsolutePath(new File(prefsPath))); //$NON-NLS-1$
      }
  	}
		if (prefsPath==null) {
  		// unable to read or write prefs 			
    	OSPLog.getOSPLog();
    	if (recommendedPath!=null) {
	    	OSPLog.warning("administrator action required: unable to write preferences file to "+recommendedPath); //$NON-NLS-1$
  		}
    	else {
	    	OSPLog.warning("unable to find or create preferences file "+TrackerStarter.PREFS_FILE_NAME); //$NON-NLS-1$
    	}
		}  	
  }

  /**
   * Saves the current preferences.
   * 
   * @return the path to the saved file
   */
  protected static String savePreferences() {
  	// save prefs file in current preferences path
  	XMLControl control = new XMLControlElement(new Preferences());
  	if (prefsPath!=null) {
  		control.write(prefsPath);
  	}
  	
  	// save other existing prefs files
		for (int i=0; i<2; i++) {
			String fileName = TrackerStarter.PREFS_FILE_NAME;
			if (i==1) {
				fileName = "."+fileName; //$NON-NLS-1$
			}
	  	// update prefs files in OSPRuntime search paths, if any
			for (String path: OSPRuntime.getDefaultSearchPaths()) {
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
    
    // save current trackerHome and ffmpegHome in OSP preferences 
    if (trackerHome!=null && new File(trackerHome, "tracker.jar").exists()) {   	 //$NON-NLS-1$
    	OSPRuntime.setPreference("TRACKER_HOME", trackerHome); //$NON-NLS-1$
    }
  	String ffmpegHome = System.getenv("FFMPEG_HOME"); //$NON-NLS-1$
    if (ffmpegHome!=null) {
    	OSPRuntime.setPreference("FFMPEG_HOME", ffmpegHome); //$NON-NLS-1$
    }
    OSPRuntime.savePreferences();
    
		return prefsPath;
  }

  /**
   * Gets the zoomInCursor.
   *
   * @return the cursor
   */
  protected static Cursor getZoomInCursor() {
  	if (zoomInCursor==null) {
  	  String imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/zoom_in.gif";  //$NON-NLS-1$
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
    return cursor==Tracker.zoomInCursor && Tracker.zoomInCursor!=Cursor.getDefaultCursor();
  }

  /**
   * Gets the zoomOutCursor.
   *
   * @return the cursor
   */
  protected static Cursor getZoomOutCursor() {
  	if (zoomOutCursor==null) {
  		String imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/zoom_out.gif";  //$NON-NLS-1$
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
    return cursor==Tracker.zoomOutCursor && Tracker.zoomOutCursor!=Cursor.getDefaultCursor();
  }

  /**
   * Main entry point when used as application.
   *
   * @param args array of tracker or video file names
   */
  public static void main(String[] args) {
//		String[] vars = {"TRACKER_HOME", "FFMPEG_HOME", "DYLD_LIBRARY_PATH"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
//		for (String next: vars) {
//			OSPLog.warning("Environment variable "+next+": "+System.getenv(next)); //$NON-NLS-1$ //$NON-NLS-2$
//		}
  	
//  	Map<String, String> map = System.getenv();
//  	for (String key: map.keySet()) {
//  		System.out.println("environment "+key+" = "+map.get(key));
//  	}
//  	for (Object key: System.getProperties().keySet()) {
//  		System.out.println("property "+key+" = "+System.getProperties().get(key));
//  	}

    // determine if this is tracker.jar (Tracker main class)
    boolean isTracker = false;
		JarFile jarfile = OSPRuntime.getLaunchJar();
    try {
			java.util.jar.Attributes att = jarfile.getManifest().getMainAttributes();
			Object mainclass = att.getValue("Main-Class"); //$NON-NLS-1$
			isTracker = mainclass.toString().endsWith("Tracker"); //$NON-NLS-1$
		} catch (Exception ex) {}
		
		// determine if relaunch is needed
  	boolean isRelaunch = args!=null && args.length>0 && "relaunch".equals(args[args.length-1]); //$NON-NLS-1$
    if (isRelaunch) {
    	String[] newargs = new String[args.length-1];
    	System.arraycopy(args, 0, newargs, 0, newargs.length);
    	args = newargs;
    }
    else {
    	// versions 4.87+ use environment variable to indicate relaunch
    	String s = System.getenv(TrackerStarter.TRACKER_RELAUNCH);
    	isRelaunch = "true".equals(s); //$NON-NLS-1$
    }

    // get memory size requested in environment, if any
    String memoryEnvironment = System.getenv("MEMORY_SIZE"); //$NON-NLS-1$
    // get current memory (maximum heap) size
		java.lang.management.MemoryMXBean memory
				= java.lang.management.ManagementFactory.getMemoryMXBean();
    long currentMemory = memory.getHeapMemoryUsage().getMax()/(1024*1024);
    
		if (!isRelaunch) {
	    String javaCommand = System.getProperty("java.home");              						//$NON-NLS-1$
	    javaCommand = XML.forwardSlash(javaCommand)+"/bin/java"; //$NON-NLS-1$
	    String javaPath = preferredJRE;
	    if (javaPath!=null) {
	    	File javaFile = OSPRuntime.getJavaFile(javaPath);
	  		if (javaFile!=null) {
	  			javaPath = XML.stripExtension(XML.forwardSlash(javaFile.getPath()));
	  		}
	  		else javaPath = null;
	    }
	    boolean needsJavaVM = javaPath!=null && !javaCommand.equals(javaPath);
	    
			// update FFMPeg
			boolean updated = updateResources();
			
			// compare memory with requested size(s)
	    if (memoryEnvironment!=null) {
	    	originalMemoryRequest = requestedMemorySize;
	    	requestedMemorySize = Integer.parseInt(memoryEnvironment);
	    }

	    boolean needsMemory = requestedMemorySize>10 &&
					(currentMemory<9*requestedMemorySize/10 || currentMemory>11*requestedMemorySize/10);
	    
	    // check environment
	    boolean needsEnvironment = false;
	    try {
				String trackerDir = TrackerStarter.findTrackerHome(false);
		    String trackerEnv = System.getenv("TRACKER_HOME"); //$NON-NLS-1$
				if (trackerDir!=null && !trackerDir.equals(trackerEnv)) {
					needsEnvironment = true;
				}
				else {
					String ffmpegDir = TrackerStarter.findFFMPegHome(trackerDir, false);
					String ffmpegEnv = System.getenv("FFMPEG_HOME"); //$NON-NLS-1$
					if (ffmpegDir!=null && !ffmpegDir.equals(ffmpegEnv)) {
						needsEnvironment = true;					
					}
					else {
						if (ffmpegDir!=null && !OSPRuntime.isLinux()) {
							String subdir = OSPRuntime.isWindows()? "bin":"lib" ; //$NON-NLS-1$ //$NON-NLS-2$
							String ffmpegPath = ffmpegDir+File.separator+subdir;
							String pathName = OSPRuntime.isWindows()? "Path":  //$NON-NLS-1$
								OSPRuntime.isMac()? "DYLD_LIBRARY_PATH": "LD_LIBRARY_PATH"; //$NON-NLS-1$ //$NON-NLS-2$
							String pathEnv = System.getenv(pathName);
							if (pathEnv==null || !pathEnv.contains(ffmpegPath)) {
								needsEnvironment = true;					
							}
						}
					}
				}
					
			} catch (Exception e) {
			}
	    

	    // attempt to relaunch if needed	    
	    if (isTracker && (needsJavaVM || needsMemory || needsEnvironment || updated)) {
	    	mainArgs = args;
	    	if (requestedMemorySize<=10) {
	    		requestedMemorySize = TrackerStarter.DEFAULT_MEMORY_SIZE;
	    	}
	    	System.setProperty(TrackerStarter.PREFERRED_MEMORY_SIZE, String.valueOf(requestedMemorySize));
	    	System.setProperty(TrackerStarter.PREFERRED_TRACKER_JAR, OSPRuntime.getLaunchJarPath());

	    	TrackerStarter.relaunch(mainArgs, true);
		    return;
			}
		}
    preferredMemorySize = requestedMemorySize;
    if (requestedMemorySize<0)
    	requestedMemorySize = (int)(currentMemory+2);
    start(args);
  }

  /**
   * Starts a new Tracker. 
   *
   * @param args array of tracker or video file names
   */
  private static void start(String[] args) {
  	FontSizer.setLevel(preferredFontLevel+preferredFontLevelPlus);
  	Dataset.maxPointsMultiplier = 6; // increase max points in dataset
    Tracker tracker = null;
    if (args == null || args.length == 0) tracker = new Tracker();
    else tracker = new Tracker(args, true, true);
    
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
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    // create and register DataTrackTool
		Runnable runner = new Runnable() {
			public void run() {
	  		try {
			    dataTrackTool = new DataTrackTool(frame);
					registerRemoteTool(dataTrackTool);
				} catch (RemoteException e) {
				}
			}
		};
		new Thread(runner).start();
    
    LaunchNode node = Launcher.activeNode;
    if (node != null) {
      frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }
    TTrackBar.refreshMemoryButton();
    
    // inform user if memory size was reduced
  	if (originalMemoryRequest>requestedMemorySize) {
    	JOptionPane.showMessageDialog(frame, 
    			TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message1")+" "+originalMemoryRequest+"MB\n"+  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    			TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message2")+" "+requestedMemorySize+"MB.\n\n"+  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    			TrackerRes.getString("Tracker.Dialog.MemoryReduced.Message3"),  //$NON-NLS-1$
    			TrackerRes.getString("Tracker.Dialog.MemoryReduced.Title"),  //$NON-NLS-1$
    			JOptionPane.INFORMATION_MESSAGE);
  	}
  	

//    warnNoVideoEngine = false; // for PLATO
    if (warnNoVideoEngine && VideoIO.getDefaultEngine().equals(VideoIO.ENGINE_NONE)) {    	
    	// warn user that there is no working video engine
    	boolean ffmpegInstalled = DiagnosticsForFFMPeg.hasFFMPegJars();
    	
    	ArrayList<String> message = new ArrayList<String>();    	
			boolean showRelaunchDialog = false;
	    	
    	// no engine installed
    	if (!ffmpegInstalled) {
    		message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message1")); //$NON-NLS-1$
    		message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message2")); //$NON-NLS-1$
    		message.add(" "); //$NON-NLS-1$
    		message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message3")); //$NON-NLS-1$
    	}
    	
    	// engines installed on Windows but no 32-bit VM
    	else if (OSPRuntime.isWindows() && JREFinder.getFinder().getDefaultJRE(32, trackerHome, true)==null) {
    		message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message1")); //$NON-NLS-1$
    		message.add(TrackerRes.getString("Tracker.Dialog.SwitchTo32BitVM.Message2")); //$NON-NLS-1$
    		message.add(" "); //$NON-NLS-1$
    		message.add(TrackerRes.getString("Tracker.Dialog.Install32BitVM.Message")); //$NON-NLS-1$	    		
    		message.add(TrackerRes.getString("PrefsDialog.Dialog.No32bitVM.Message")); //$NON-NLS-1$	    		
    	}
    	
    	// engines installed on Windows but running in 64-bit VM
    	else if (OSPRuntime.isWindows() && OSPRuntime.getVMBitness()==64) {
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
  		for (String line: message) {    			
  			box.add(new JLabel(line));
  		}
    	
    	// add "don't show again" checkbox
    	box.add(new JLabel("  ")); //$NON-NLS-1$
    	final JCheckBox checkbox = new JCheckBox(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Checkbox")); //$NON-NLS-1$
    	checkbox.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			warnNoVideoEngine = !checkbox.isSelected();
    		}
    	});   	
    	box.add(checkbox);
    	box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    	
    	if (showRelaunchDialog) {
    		// provide immediate way to change to 32-bit VM and relaunch
  			Object[] options = new Object[] {
  					TrackerRes.getString("Tracker.Dialog.Button.RelaunchNow"),    //$NON-NLS-1$
            TrackerRes.getString("Tracker.Dialog.Button.ContinueWithoutEngine")}; //$NON-NLS-1$
  			int response = JOptionPane.showOptionDialog(frame, box,
            TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Title"), //$NON-NLS-1$
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
  			if (response==0) {
  				// use prefs dialog to switch to 32-bit VM/default engine and relaunch
  				Runnable launcher = new Runnable() {
  					public void run() {
  						PrefsDialog prefs = frame.getPrefsDialog();
  						prefs.relaunch32Bit();
  					}
  				}; 					
  				SwingUtilities.invokeLater(launcher);

  			}
    	}
    	else {
	    	JOptionPane.showMessageDialog(frame, box,
	    			TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Title"),  //$NON-NLS-1$
	    			JOptionPane.INFORMATION_MESSAGE);
    	}
    		
    }
        
		if (System.getenv("STARTER_WARNING")!=null) { //$NON-NLS-1$
			// possible cause: running VM in 64-bits even though preference is 32-bit
			// if so, change preference
		  String warningString = System.getenv("STARTER_WARNING"); //$NON-NLS-1$
		  String[] lines = warningString.split("\n"); //$NON-NLS-1$
			Box box = Box.createVerticalBox();
			for (String line: lines) {    			
				box.add(new JLabel(line));
			}
			
			box.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		  JOptionPane.showMessageDialog(null, 
		  		box, 
		  		TrackerRes.getString("Tracker.Dialog.StarterWarning.Title"), //$NON-NLS-1$
		      JOptionPane.WARNING_MESSAGE);
		}

		final String newVersionURL = System.getenv(TrackerStarter.TRACKER_NEW_VERSION);
		if (newVersionURL!=null) {
  		final File target = new File(trackerHome, "tracker.jar"); //$NON-NLS-1$
      Timer timer = new Timer(2000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
	    		ResourceLoader.download(newVersionURL, target, true);
	    		// check preferences: if not default tracker.jar, ask user to change to default
	    		if (Tracker.preferredTrackerJar!=null && !"tracker.jar".equals(Tracker.preferredTrackerJar)) { //$NON-NLS-1$
	    			String prefVers = Tracker.preferredTrackerJar.substring(8, Tracker.preferredTrackerJar.lastIndexOf(".")); //$NON-NLS-1$
	    			String s1 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message1")+" "+Tracker.VERSION; //$NON-NLS-1$ //$NON-NLS-2$
	    			String s2 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message2")+" "+prefVers; //$NON-NLS-1$ //$NON-NLS-2$
	    			String s3 = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Message3"); //$NON-NLS-1$
	    			String title = TrackerRes.getString("Tracker.Dialog.ChangePrefVersionAfterUpgrade.Title"); //$NON-NLS-1$
		    		int response = JOptionPane.showConfirmDialog(null, s1+XML.NEW_LINE+s2+XML.NEW_LINE+s3, 
		    				title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		    		if (response==JOptionPane.YES_OPTION) {
		    			Tracker.preferredTrackerJar = null;
		    			Tracker.savePreferences();
		    		}
	    		}
        }
      });
      timer.setRepeats(false);
      timer.start();		      
		}

		Timer memoryTimer = new Timer(5000, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	TTrackBar.refreshMemoryButton();
      }				    	      
    });
  	memoryTimer.setRepeats(true);
  	memoryTimer.start();
  }

  /**
   * Sets the progress in percent for splash display.
   * 
   * @param progress a number from 0 (start) to 100 (done)
   */
  protected static void setProgress(int progress) {
    progressBar.setValue(progress);
  }

  /**
   * Logs the current time (to milliseconds) with a message.
   * @param message 
   */
  protected static void logTime(String message) {
  	if (timeLogEnabled) {
			SimpleDateFormat sdf = new SimpleDateFormat("ss.SSS"); //$NON-NLS-1$
		  Calendar cal = Calendar.getInstance();
  		OSPLog.info(sdf.format(cal.getTime())+": "+message); //$NON-NLS-1$
  	}
  }
  
  /**
   * Registers a Remote tool with the RMI registry.
   * 
   * @param remoteTool the Remote
   * @return true if successfully registered
   */
  protected static boolean registerRemoteTool(Remote remoteTool) {
		final String toolname = remoteTool.getClass().getSimpleName();
		
//		// create thread to see if registry is running and tool registered
//    Thread registryThread = new Thread() {
//      public void run() {
//        toolRegistered = false;
//      	toolNotFound = false;
//        try { 
//          registry = java.rmi.registry.LocateRegistry.getRegistry(DataTrackSupport.PORT);
//          registry.lookup(toolname);
//	        toolRegistered = true;
//        }
//        catch (Exception exc) {
//        	toolNotFound = true;
//        }       	
//      }      
//    };
//    
//    // start thread and check every half-second to see if completed
//    registryThread.setPriority(Thread.NORM_PRIORITY);
//    registryThread.start();
//    int attempts = 0;
//    int maxAttempts = 8;
//    while (attempts<=maxAttempts) {
//      attempts++;
//      if (toolRegistered || toolNotFound) {
//        break;
//      }
//      try { Thread.sleep(500); }
//      catch(Exception exc) {}
//    }
//    if (toolRegistered) {
//      OSPLog.finest("Registry thread found registered tool "+toolname); //$NON-NLS-1$
//    	return true;
//    }
//    
//    OSPLog.finest("Killing registry thread and registering tool "+toolname); //$NON-NLS-1$
//    registryThread.interrupt();
        
  	// register tool
  	try {
  		// create registry if needed
  		if (registry==null) {
	 			registry = java.rmi.registry.LocateRegistry.createRegistry(DataTrackSupport.PORT); 			
  		}
			registry.rebind(toolname, remoteTool);
  		OSPLog.fine(toolname+" successfully registered"); //$NON-NLS-1$
  		return true;
		} catch (Exception ex) {
  		OSPLog.warning(ex.getMessage());
		}    
  	return false;
  }
  
  /**
   * Unregisters a Remote tool with the RMI registry.
   * 
   * @param remoteTool the Remote
   * @return true if successfully unregistered
   */
  protected static boolean unregisterRemoteTool(Remote remoteTool) {
  	if (registry==null || remoteTool==null) return false;
  	try {
			String name = remoteTool.getClass().getSimpleName();
			registry.unbind(name);
  		OSPLog.fine(name+" successfully unregistered"); //$NON-NLS-1$
  		return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
  	return false;
  }
  
  /**
   * Adds a path to the list of recent files.
   * 
   * @param filename the absolute path to a recently opened or saved file.
   * @param atEnd true to add at end of the list
   */
  protected static void addRecent(String filename, boolean atEnd) {
  	synchronized(recentFiles) {
	  	while (recentFiles.contains(filename))
	  		recentFiles.remove(filename);
	  	if (atEnd)
	  		recentFiles.add(filename);
	  	else
	      recentFiles.add(0, filename);
	    while (recentFiles.size()>recentFilesSize) {
	    	recentFiles.remove(recentFiles.size()-1);
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
    while (recentFiles.size()>recentFilesSize) {
    	recentFiles.remove(recentFiles.size()-1);
    }
  }
  
  /**
   * A class to compare version strings.
   */
  public static class Version implements Comparable {
  	String ver;
  	
    /**
     * Constructor
     * 
     * @param version the version string
     */
  	public Version(String version) {
  		ver = version;
  	}
  	
  	public boolean isValid() {
	    String[] v = this.ver.trim().split("\\."); //$NON-NLS-1$
	    if (v.length==2 || v.length==3) {
	    	for (int i=0; i<v.length; i++) {
	    		try {
	    			Integer.parseInt(v[i].trim());
	    		} catch (Exception ex) {
	    			return false;
	    		}
	    	}
    		return true;
	    }
  		return false;
  	}

		@Override
		public int compareTo(Object o) {
	  	// typical newer semantic version "4.9.10" 
	  	// typical older version "4.97"
			
			// split at decimal points
	    String[] v1 = this.ver.trim().split("\\."); //$NON-NLS-1$
	    String[] v2 = ((Version)o).ver.trim().split("\\."); //$NON-NLS-1$
	    // newer semantic version arrays have length 3
	    // older version arrays have length 2
	 
	  	if (v2.length>v1.length) {
	  		// v1 is older version, v2 is newer
	  		return -1;
	  	}
	  	if (v1.length>v2.length) {
	  		// v2 is older version, v1 is newer
	  		return 1;
	  	}
	  	// both arrays have the same length
	    for (int i=0; i<v1.length; i++) {
	      if (Integer.parseInt(v1[i]) < Integer.parseInt(v2[i])) {
	        return -1;
	      }
	      else if (Integer.parseInt(v1[i]) > Integer.parseInt(v2[i])) {
	        return 1;
	      }
	    }
			return 0;
		}
  }
  
  
  /**
   * A class to save and load Tracker preferences. The preference data are static Tracker fields.
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
       * @param obj the object to save
       */
      public void saveObject(XMLControl control, Object obj) {
      	// save only non-default values
      	if (!Tracker.preferredLogLevel.equals(Tracker.DEFAULT_LOG_LEVEL)) // true by default
      		control.setValue("log_level", Tracker.preferredLogLevel.getName()); //$NON-NLS-1$
      	if (!Tracker.showHintsByDefault) // true by default
      		control.setValue("show_hints", Tracker.showHintsByDefault); //$NON-NLS-1$
      	if (Tracker.isRadians) // false by default
      		control.setValue("radians", Tracker.isRadians); //$NON-NLS-1$
      	if (Tracker.markAtCurrentFrame) // false by default
      		control.setValue("mark_current_frame", Tracker.markAtCurrentFrame); //$NON-NLS-1$
      	if (Tracker.scrubMouseWheel) // false by default
      		control.setValue("scrub_mousewheel", Tracker.scrubMouseWheel); //$NON-NLS-1$
      	if (Tracker.enableAutofill) // false by default
      		control.setValue("enable_autofill", Tracker.enableAutofill); //$NON-NLS-1$
      	if (Tracker.showGaps) // false by default
      		control.setValue("show_gaps", Tracker.showGaps); //$NON-NLS-1$
      	if (Tracker.trailLengthIndex!=TToolBar.trailLengths.length-2)
      		control.setValue("trail_length", TToolBar.trailLengthNames[Tracker.trailLengthIndex]); //$NON-NLS-1$
      	if (Tracker.centerCalibrationStick) // false by default
      		control.setValue("center_stick", Tracker.centerCalibrationStick); //$NON-NLS-1$
      	if (!Tracker.warnNoVideoEngine) // true by default
      		control.setValue("warn_no_engine", Tracker.warnNoVideoEngine); //$NON-NLS-1$
      	if (!Tracker.warnVariableDuration) // true by default
      		control.setValue("warn_variable_frame_duration", Tracker.warnVariableDuration); //$NON-NLS-1$
      	if (!Tracker.warnFFMPegError) // true by default
      		control.setValue("warn_ffmpeg_error", Tracker.warnFFMPegError); //$NON-NLS-1$
      	// always save preferred tracker.jar
      	String jar = Tracker.preferredTrackerJar==null? 
      			"tracker.jar": Tracker.preferredTrackerJar; //$NON-NLS-1$
      	control.setValue("tracker_jar", jar); //$NON-NLS-1$
      	if (Tracker.preferredJRE!=null)
      		control.setValue("java_vm", Tracker.preferredJRE); //$NON-NLS-1$
      	if (Tracker.preferredPointMassFootprint!=null)
      		control.setValue("pointmass_footprint", Tracker.preferredPointMassFootprint); //$NON-NLS-1$
      	if (Tracker.preferredMemorySize>-1) // -1 by default
      		control.setValue("memory_size", Tracker.preferredMemorySize); //$NON-NLS-1$
      	if (Tracker.lookAndFeel!=null)
      		control.setValue("look_feel", Tracker.lookAndFeel); //$NON-NLS-1$
      	if (Tracker.prelaunchExecutables.length>0)
      		control.setValue("run", Tracker.prelaunchExecutables); //$NON-NLS-1$
      	if (Tracker.preferredLocale!=null)
      		control.setValue("locale", Tracker.preferredLocale); //$NON-NLS-1$
      	if (Tracker.preferredDecimalSeparator!=null)
      		control.setValue("decimal_separator", Tracker.preferredDecimalSeparator); //$NON-NLS-1$
		if (Tracker.additionalDecimalSeparators!=null)
			control.setValue("additional_decimal_separators", Tracker.additionalDecimalSeparators); //$NON-NLS-1$
      	if (Tracker.preferredFontLevel>0) {
      		control.setValue("font_size", Tracker.preferredFontLevel); //$NON-NLS-1$
      	}
      	if (Tracker.preferredFontLevelPlus>0) {
      		control.setValue("font_size_plus", Tracker.preferredFontLevelPlus); //$NON-NLS-1$
      	}
      	if (ResourceLoader.getOSPCache()!=null) {
      		File cache = ResourceLoader.getOSPCache();
      		control.setValue("cache", cache.getPath()); //$NON-NLS-1$
      	}
      	control.setValue("upgrade_interval", Tracker.checkForUpgradeInterval); //$NON-NLS-1$
      	int lastChecked = (int)(Tracker.lastMillisChecked/1000L);
      	control.setValue("last_checked", lastChecked); //$NON-NLS-1$
      	JFileChooser chooser = VideoIO.getChooser();
      	File file = chooser.getCurrentDirectory();
        String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
        if (!file.getAbsolutePath().equals(userDir)) // user.dir by default
        	control.setValue("file_chooser_directory", XML.getAbsolutePath(file)); //$NON-NLS-1$
        
        // video_engine--used by version 4.75+
        if (!VideoIO.getPreferredExportExtension().equals(VideoIO.DEFAULT_PREFERRED_EXPORT_EXTENSION))
        	control.setValue("export_extension", VideoIO.getPreferredExportExtension()); //$NON-NLS-1$
        if (!ExportZipDialog.preferredExtension.equals(ExportZipDialog.DEFAULT_VIDEO_EXTENSION))
        	control.setValue("zip_export_extension", ExportZipDialog.preferredExtension); //$NON-NLS-1$

        if (Tracker.recentFilesSize!=6) // 6 items by default
      		control.setValue("max_recent", Tracker.recentFilesSize); //$NON-NLS-1$
      	if (!Tracker.recentFiles.isEmpty()) // empty by default
      		control.setValue("recent_files", Tracker.recentFiles); //$NON-NLS-1$
      	if (Tracker.preferredAutoloadSearchPaths!=null) {
      		// added Dec 2104
      		control.setValue("autoload_search_paths", preferredAutoloadSearchPaths); //$NON-NLS-1$
      	}
      	if (!Tracker.autoloadMap.isEmpty()) {
      		// added Dec 2104      		
      		String[][] autoloadData = new String[Tracker.autoloadMap.size()][];
      		int i = 0;
      		for (String filePath: Tracker.autoloadMap.keySet()) {
      			String[] functions = Tracker.autoloadMap.get(filePath);
      			String[] fileAndFunctions = new String[functions.length+1];
      			fileAndFunctions[0] = filePath;
      			System.arraycopy(functions, 0, fileAndFunctions, 1, functions.length);
      			autoloadData[i] = fileAndFunctions;
      			i++;
      		}
      		control.setValue("autoload_exclusions", autoloadData); //$NON-NLS-1$
      	}
      	if (!Tracker.dataFunctionControlStrings.isEmpty()) {
      		// deprecated Dec 2014: this is for legacy preferences
      		control.setValue("data_functions", Tracker.dataFunctionControlStrings); //$NON-NLS-1$
      	}
      	if (defaultConfig!=null && !areEqual(defaultConfig, getFullConfig())) { // defaultConfig by default
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
      public Object createObject(XMLControl control){
        return new Preferences();
      }

      /**
       * Loads an object with data from an XMLControl.
       *
       * @param control the control
       * @param obj the object
       * @return the loaded object
       */
      @SuppressWarnings("unchecked")
			public Object loadObject(XMLControl control, Object obj) {
        Level logLevel = OSPLog.parseLevel(control.getString("log_level")); //$NON-NLS-1$
        if(logLevel!=null) {
        	Tracker.preferredLogLevel = logLevel;
        	OSPLog.setLevel(logLevel);
        	if (logLevel==Level.ALL) {
        		OSPLog.showLogInvokeLater();
        	}
        }
        Tracker.isRadians = control.getBoolean("radians"); //$NON-NLS-1$
        Tracker.markAtCurrentFrame = control.getBoolean("mark_current_frame"); //$NON-NLS-1$
        Tracker.scrubMouseWheel = control.getBoolean("scrub_mousewheel"); //$NON-NLS-1$
        Tracker.enableAutofill = control.getBoolean("enable_autofill"); //$NON-NLS-1$
        Tracker.showGaps = control.getBoolean("show_gaps"); //$NON-NLS-1$
        Tracker.centerCalibrationStick = control.getBoolean("center_stick"); //$NON-NLS-1$
    		Tracker.isVideoFast = control.getBoolean("ffmpeg_fast"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("trail_length")) { //$NON-NLS-1$
      		String name = control.getString("trail_length"); //$NON-NLS-1$
      		for (int i=0; i<TToolBar.trailLengthNames.length; i++) {
      			if (TToolBar.trailLengthNames[i].equals(name)) Tracker.trailLengthIndex = i;
      		}
      	}
      	if (control.getPropertyNames().contains("warn_no_engine")) //$NON-NLS-1$
      		Tracker.warnNoVideoEngine = control.getBoolean("warn_no_engine"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("warn_ffmpeg_error")) //$NON-NLS-1$
      		Tracker.warnFFMPegError = control.getBoolean("warn_ffmpeg_error"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("warn_variable_frame_duration")) //$NON-NLS-1$
      		Tracker.warnVariableDuration = control.getBoolean("warn_variable_frame_duration"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("show_hints")) { //$NON-NLS-1$
      		Tracker.showHintsByDefault = control.getBoolean("show_hints"); //$NON-NLS-1$
      		Tracker.showHints = Tracker.showHintsByDefault;
      		Tracker.startupHintShown = !Tracker.showHints;
      	}
      	if (control.getPropertyNames().contains("java_vm")) { //$NON-NLS-1$
      		Tracker.preferredJRE = control.getString("java_vm"); //$NON-NLS-1$
  				if (OSPRuntime.getJavaFile(Tracker.preferredJRE)==null) {
  					Tracker.preferredJRE = null;
  				}  				
      	}
      	Tracker.preferredPointMassFootprint = control.getString("pointmass_footprint"); //$NON-NLS-1$
  	    if (control.getPropertyNames().contains("memory_size")) //$NON-NLS-1$
  	    	Tracker.requestedMemorySize = control.getInt("memory_size"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("look_feel")) //$NON-NLS-1$
      		Tracker.lookAndFeel = control.getString("look_feel"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("decimal_separator")) { //$NON-NLS-1$
      		Tracker.preferredDecimalSeparator = control.getString("decimal_separator"); //$NON-NLS-1$
      		OSPRuntime.setPreferredDecimalSeparator(preferredDecimalSeparator);
      	}
  	    if (control.getPropertyNames().contains("additional_decimal_separators")) { //$NON-NLS-1$
			Tracker.additionalDecimalSeparators = control.getString("additional_decimal_separators"); //$NON-NLS-1$
			OSPRuntime.setAdditionalDecimalSeparators(additionalDecimalSeparators);
	    }
      	if (control.getPropertyNames().contains("run")) //$NON-NLS-1$
      		Tracker.prelaunchExecutables = (String[])control.getObject("run"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("locale")) //$NON-NLS-1$
      		setPreferredLocale(control.getString("locale")); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("font_size")) { //$NON-NLS-1$
      		Tracker.preferredFontLevel = control.getInt("font_size"); //$NON-NLS-1$
      		Tracker.preferredFontLevelPlus = control.getInt("font_size_plus"); //$NON-NLS-1$
      		if (Tracker.preferredFontLevelPlus==Integer.MIN_VALUE) {
      			Tracker.preferredFontLevelPlus = 0;
      		}
      	}
      	// set cache only if it has not yet been set
      	if (ResourceLoader.getOSPCache()==null) {
      		setCache(control.getString("cache")); //$NON-NLS-1$
      	}
      	if (control.getPropertyNames().contains("upgrade_interval")) { //$NON-NLS-1$
      		Tracker.checkForUpgradeInterval = control.getInt("upgrade_interval"); //$NON-NLS-1$
      		Tracker.lastMillisChecked = control.getInt("last_checked")*1000L; //$NON-NLS-1$
      	}
      	if (control.getPropertyNames().contains("file_chooser_directory")) //$NON-NLS-1$
      		OSPRuntime.chooserDir = control.getString("file_chooser_directory"); //$NON-NLS-1$
      	
      	// preferred video engine
      	VideoIO.setPreferredExportExtension(control.getString("export_extension")); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("zip_export_extension")) //$NON-NLS-1$
      		ExportZipDialog.preferredExtension = control.getString("zip_export_extension"); //$NON-NLS-1$

      	if (control.getPropertyNames().contains("max_recent")) //$NON-NLS-1$
      		Tracker.recentFilesSize = control.getInt("max_recent"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("recent_files")) { //$NON-NLS-1$
  	    	ArrayList<?> recent = ArrayList.class.cast(control.getObject("recent_files")); //$NON-NLS-1$
  	    	for (Object next: recent) {
  	    	  addRecent(next.toString(), true); // add at end
  	    	}
      	}
    		// added Dec 2014
      	Tracker.preferredAutoloadSearchPaths = (String[])control.getObject("autoload_search_paths"); //$NON-NLS-1$
      	// load autoload_exclusions: added Dec 2014
      	if (control.getPropertyNames().contains("autoload_exclusions")) { //$NON-NLS-1$
  	    	String[][] autoloadData = (String[][])control.getObject("autoload_exclusions"); //$NON-NLS-1$
  	    	for (String[] next: autoloadData) {
  	    		String filePath = XML.forwardSlash(next[0]);
  	    		String[] functions = new String[next.length-1];
  	    		System.arraycopy(next, 1, functions, 0, functions.length);
  	    		Tracker.autoloadMap.put(filePath, functions);
  	    	}
      	}
      	
      	// load autoloadable data function strings (deprecated Dec 2014: this is for legacy files)
      	if (control.getPropertyNames().contains("data_functions")) { //$NON-NLS-1$
      		Collection<String> autoloads = (Collection<String>)control.getObject("data_functions"); //$NON-NLS-1$
      		Tracker.dataFunctionControlStrings.addAll(autoloads);
      	}

    		XMLControl child = control.getChildControl("configuration"); //$NON-NLS-1$
    		if (child!=null) {
    			Configuration config = (Configuration)child.loadObject(null);
    			setDefaultConfig(config.enabled);
    		}
      	// always load "tracker_jar"
    		Tracker.preferredTrackerJar = control.getString("tracker_jar"); //$NON-NLS-1$
      	return obj;
      }
    }  	
  }
  
}
