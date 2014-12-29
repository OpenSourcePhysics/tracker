/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
package org.opensourcephysics.cabrillo.tracker;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;

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
  /** the tracker icon */
  public static final ImageIcon TRACKER_ICON = new ImageIcon(
      Tracker.class.getResource("resources/images/tracker_icon_32.png")); //$NON-NLS-1$
  /** a larger tracker icon */
  public static final ImageIcon TRACKER_ICON_256 = new ImageIcon(
      Tracker.class.getResource("resources/images/tracker_icon_256.png")); //$NON-NLS-1$
	static final String THETA = TeXParser.parseTeX("$\\theta"); //$NON-NLS-1$
	static final String OMEGA = TeXParser.parseTeX("$\\omega"); //$NON-NLS-1$
	static final String ALPHA = TeXParser.parseTeX("$\\alpha"); //$NON-NLS-1$
	static final String DEGREES = "º"; //$NON-NLS-1$
  static String trackerHome;
  static final Level DEFAULT_LOG_LEVEL = ConsoleLevel.OUT_CONSOLE;
  
  // for testing
  static boolean timeLogEnabled = false;
  static boolean testOn = false;
  static TrackerPanel testPanel;
  
  // define static fields
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
	  "new.analyticParticle", "new.clone",  //$NON-NLS-1$ //$NON-NLS-2$
	  "new.dynamicParticle", "new.dynamicTwoBody",  //$NON-NLS-1$ //$NON-NLS-2$ 
	  "new.tapeMeasure", "new.protractor",  //$NON-NLS-1$ //$NON-NLS-2$
	  "calibration.stick", "calibration.tape", //$NON-NLS-1$ //$NON-NLS-2$
	  "calibration.points", "calibration.offsetOrigin", //$NON-NLS-1$ //$NON-NLS-2$
	  "track.name", "track.description",  //$NON-NLS-1$ //$NON-NLS-2$
	  "track.color", "track.footprint",  //$NON-NLS-1$ //$NON-NLS-2$ 
	  "track.visible", "track.locked",  //$NON-NLS-1$ //$NON-NLS-2$
	  "track.delete", "track.autoAdvance",  //$NON-NLS-1$ //$NON-NLS-2$ 
	  "track.markByDefault", "track.autotrack", "coords.locked",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	  "coords.origin", "coords.angle", "data.algorithm",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "coords.scale", "coords.refFrame", "button.x",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "button.v", "button.a", "button.trails",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	  "button.labels", "button.stretch", "button.clipSettings",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "button.xMass", "button.axes", "button.path",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  "config.saveWithData", "data.builder", "data.tool"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  static Set<String> defaultConfig;
  static boolean xuggleCopied;
  static String[] mainArgs;
  static JFrame splash;
  static Icon trackerLogoIcon, ospLogoIcon;
  static JLabel tipOfTheDayLabel;
  static JProgressBar progressBar;
  static String version = "4.87"; //$NON-NLS-1$
  static String newerVersion; // new version available if non-null
  static String copyright = "Copyright (c) 2015 Douglas Brown"; //$NON-NLS-1$
  static String trackerWebsite = "www.cabrillo.edu/~dbrown/tracker"; //$NON-NLS-1$
  static String author = "Douglas Brown"; //$NON-NLS-1$
  static String osp = "Open Source Physics"; //$NON-NLS-1$
  static AbstractAction aboutQTAction, aboutXuggleAction, aboutThreadsAction;
  static Action aboutTrackerAction, readmeAction;
  static Action aboutJavaAction, startLogAction, trackerPrefsAction;
  private static Tracker tracker;
  static Process rmiProc;
  static String readmeFileName = "tracker_README.txt"; //$NON-NLS-1$
  static JDialog readmeDialog, startLogDialog, trackerPrefsDialog;
  static JTextArea trackerPrefsTextArea;
  static String prefsFileName = ".tracker.prefs"; //$NON-NLS-1$
  static String prefsPath;
  @SuppressWarnings("javadoc")
	public static String rootXMLPath = ""; // path to root directory of trk files //$NON-NLS-1$
  static Cursor zoomInCursor, zoomOutCursor, grabCursor, grabbedCursor;
  static boolean showHints = true;
  static boolean startupHintShown;
  static String pdfHelpPath = "/tracker_help.pdf"; //$NON-NLS-1$
  static JButton pdfHelpButton;
  static ArrayList<String> recentFiles = new ArrayList<String>();
  static int minimumMemorySize = 32;
  static int requestedMemorySize = -1, originalMemoryRequest = 0;
  static long lastMillisChecked;
	static boolean is64BitVM;
  protected static Locale[] locales;
  static Locale defaultLocale;
  static ArrayList<String> checkForUpgradeChoices;
  static Map<String, Integer> checkForUpgradeIntervals;
  static Collection<String> dataFunctionControlStrings = new HashSet<String>();
  static Collection<XMLControl> dataFunctionControls = new HashSet<XMLControl>();
  static java.io.FileFilter xmlFilter;
  
  // user-settable preferences saved/loaded by Preferences class
  static Level preferredLogLevel = DEFAULT_LOG_LEVEL;
  static boolean showHintsByDefault = true;
  static int recentFilesSize = 6;
  static int preferredMemorySize = -1;
  static String lookAndFeel, preferredLocale;
  static String preferredJRE, preferredJRE32, preferredJRE64;
  static String preferredTrackerJar;
  static int checkForUpgradeInterval = 0;
  static int preferredFontLevel = 0;
  static boolean isRadians, isXuggleFast, engineKnown=true;
  static boolean warnXuggleError=true, warnNoVideoEngine=true, use32BitMode=false;
  static boolean warnVariableDuration=true;
  static String[] prelaunchExecutables = new String[0];

  // the only instance field!
  private TFrame frame;

  static {
//  	OSPLog.setLevel(ConsoleLevel.ALL);
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
    imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/grab.gif";  //$NON-NLS-1$
    Image im = ResourceLoader.getImage(imageFile);    
    grabCursor = Toolkit.getDefaultToolkit().createCustomCursor(
    		im, new Point(14, 10), "Grab"); //$NON-NLS-1$
    imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/grabbing.gif";  //$NON-NLS-1$
    im = ResourceLoader.getImage(imageFile);    
    grabbedCursor = Toolkit.getDefaultToolkit().createCustomCursor(
    		im, new Point(12, 8), "Grabbed"); //$NON-NLS-1$  	
  	// create static objects AFTER they are defined above
    locales = new Locale[] { 
			Locale.ENGLISH, 
			new Locale("ar"), // arabic //$NON-NLS-1$
			new Locale("cs"), // czech //$NON-NLS-1$
			new Locale("in"), // indonesian //$NON-NLS-1$
			new Locale("da"), // danish //$NON-NLS-1$
			Locale.GERMAN,
			new Locale("el", "GR"), // greek //$NON-NLS-1$ //$NON-NLS-2$
			new Locale("es"), // spanish //$NON-NLS-1$
			new Locale("fi"), // finnish //$NON-NLS-1$
			Locale.FRENCH,
			new Locale("iw", "IL"), // hebrew //$NON-NLS-1$ //$NON-NLS-2$
			Locale.ITALIAN,
			new Locale("ko"), // korean //$NON-NLS-1$
//			new Locale("nl", "NL"), // dutch //$NON-NLS-1$ //$NON-NLS-2$
			new Locale("pt", "PT"), // Portugal portuguese //$NON-NLS-1$ //$NON-NLS-2$ 
			new Locale("pt", "BR"), // Brazil portuguese //$NON-NLS-1$ //$NON-NLS-2$ 
			new Locale("sk"), // slovak //$NON-NLS-1$
			new Locale("sl"), // slovenian //$NON-NLS-1$
			new Locale("sv"), // swedish //$NON-NLS-1$
			Locale.TAIWAN, // traditional chinese
			Locale.CHINA}; // simplified chinese
  	setDefaultConfig(getFullConfig());
  	loadPreferences();
  	loadCurrentVersion(false);
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
    tipOfTheDayLabel = new JLabel(tip);
    tipOfTheDayLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
    Font font = tipOfTheDayLabel.getFont().deriveFont(Font.PLAIN).deriveFont(14f);
    tipOfTheDayLabel.setFont(font);
    tipOfTheDayLabel.setForeground(darkred);
    tipOfTheDayLabel.setAlignmentX(0.5f);
    progressBar = new JProgressBar(0, 100);
    progressBar.setValue(0);
    JPanel progressPanel = new JPanel(new BorderLayout());
    progressPanel.setBorder(BorderFactory.createEmptyBorder(12, 50, 16, 50));
    progressPanel.add(progressBar, BorderLayout.CENTER);
    progressPanel.setOpaque(false);
    Box center = Box.createVerticalBox();
    center.add(tipOfTheDayLabel);
    center.add(progressPanel);
    contentPane.add(center, BorderLayout.CENTER);

    // version south
    String vers = author+"   "+osp+"   Ver "+version; //$NON-NLS-1$ //$NON-NLS-2$
		if (version.length()>5 || testOn) vers += " BETA"; //$NON-NLS-1$
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
  	// this list should agree with xuggle video types below
  	for (String ext: VideoIO.VIDEO_EXTENSIONS) { // {"mov", "avi", "mp4"}
      ResourceLoader.addExtractExtension(ext);
  	}
    
  	is64BitVM = OSPRuntime.getVMBitness()==64;
  	
    // add Xuggle video types, if available, using reflection
  	try {
			String xuggleIOName = "org.opensourcephysics.media.xuggle.XuggleIO"; //$NON-NLS-1$
			Class<?> xuggleIOClass = Class.forName(xuggleIOName);
			Method method = xuggleIOClass.getMethod("registerWithVideoIO", (Class[]) null);  //$NON-NLS-1$
			method.invoke(null, (Object[]) null);
		} catch (Exception ex) {
		}    
    
    // add QT video types, if available, using reflection
    if (!OSPRuntime.isLinux()) {
    	// do this in a separate thread since can be time-consuming
      Runnable runner = new Runnable() {
      	public void run() {
          try {
//          	VideoType qtType = new QTVideoType();
//            VideoIO.addVideoEngine(qtType);
//          	for (String ext: VideoIO.VIDEO_EXTENSIONS) { // {"mov", "avi", "mp4"}
//            	VideoFileFilter filter = new VideoFileFilter(ext, new String[] {ext});
//              qtType = new QTVideoType(filter);
//              VideoIO.addVideoType(qtType);
//          	}
          	
          	String qtTypeName = "org.opensourcephysics.media.quicktime.QTVideoType"; //$NON-NLS-1$
            Class<?> qtClass = Class.forName(qtTypeName);
            Constructor<?>[] constructors = qtClass.getDeclaredConstructors();
          	Constructor<?> cNoArgs = null, cFilterArg = null;
          	for (int i = 0; i < constructors.length; i++) {
          		Type[] argTypes = constructors[i].getGenericParameterTypes();
        	    if (argTypes.length==0) {
        	    	cNoArgs = constructors[i];
        	    }
        	    else if (argTypes.length==1 && argTypes[0].equals(VideoFileFilter.class)) {
        	    	cFilterArg = constructors[i];        	    	
        	    }
          	}
          	if (cNoArgs==null) throw new Exception("QuickTime not found"); //$NON-NLS-1$
          	
            VideoType qtType = (VideoType)cNoArgs.newInstance();
            VideoIO.addVideoEngine(qtType);
            
            if (cFilterArg==null) return;
          	for (String ext: VideoIO.VIDEO_EXTENSIONS) { // {"mov", "avi", "mp4"}
            	VideoFileFilter filter = new VideoFileFilter(ext, new String[] {ext});
              qtType = (VideoType)cFilterArg.newInstance(filter);
              VideoIO.addVideoType(qtType);
          	}

          }
          catch (Exception ex) { // QT for Java not working
          	OSPLog.config("QuickTime exception: "+ex.toString()); //$NON-NLS-1$
          }
          catch (Error er) { // QT for Java not working
          	OSPLog.config("QuickTime error: "+er.toString()); //$NON-NLS-1$
          }
        }
      };
      Thread opener = new Thread(runner);
      opener.setPriority(Thread.NORM_PRIORITY);
      opener.setDaemon(true);
      opener.start();    	
    }

    VideoIO.setDefaultXMLExtension("trk"); //$NON-NLS-1$
    
    // create pdf help button
    pdfHelpButton = new JButton(TrackerRes.getString("Tracker.Button.PDFHelp")); //$NON-NLS-1$
    pdfHelpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
        	java.net.URL url = new java.net.URL("http://"+trackerWebsite+pdfHelpPath); //$NON-NLS-1$
        	org.opensourcephysics.desktop.OSPDesktop.displayURL(url.toString());
        }
        catch(Exception ex) { ex.printStackTrace(); }
      }
    });
    
    // find Java VMs in background thread so they are ready when needed
    Runnable runner = new Runnable() {
    	public void run() {
		    ExtensionsManager.getManager().getAllJREs(32);    		
    	}
    };
    new Thread(runner).start();
  }

  /**
   * Gets the shared Tracker for single-VM use.
   *
   * @return the tracker panel
   */
  public static Tracker getTracker() {
    if (tracker == null) {
      tracker = new Tracker(null, false);
    }
    return tracker;
  }

  /**
   * Constructs Tracker with a blank tab.
   */
  public Tracker() {
    this(null, true);
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
  private Tracker(String[] names, boolean addTabIfEmpty) {
    splash.setVisible(true);
    createFrame();
    Tracker.setProgress(5);
    if (names != null) {
      // parse file names
      for (int i = 0; i < names.length; i++) {
        if (names[i] == null) continue;
        String name = XML.getName(names[i]);
        splash(TrackerRes.getString("Tracker.Splash.Loading")+" \""+name+"\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    DiagnosticsForXuggle.setDialogOwner(frame);
    // set up the Java VM exit mechanism when used as application
    if ( org.opensourcephysics.display.OSPRuntime.applet == null) {
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
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
          if (rmiProc != null) rmiProc.destroy();
          
          // hide the frame
          frame.setVisible(false);

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
   * Gets an image for the OSX dock.
   * @return a Tracker icon image
   */
  public static Image getOSXDockImage() {
  	return TRACKER_ICON_256.getImage();
  }
  
  /**
   * Shows the About Tracker dialog.
   */
  public static void showAboutTracker() {
  	String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
  	String vers = Tracker.version;
		if (vers.length()>5 || testOn) vers += " BETA"; //$NON-NLS-1$
		String date = OSPRuntime.getLaunchJarBuildDate();
		if (date!=null) 
			vers = vers+"   "+date; //$NON-NLS-1$
    String aboutString = "Tracker "  //$NON-NLS-1$
    		+ vers + newline
        + Tracker.copyright + newline
        + Tracker.trackerWebsite + newline + newline
        + TrackerRes.getString("Tracker.About.ProjectOf") + newline //$NON-NLS-1$
        + "Open Source Physics" + newline //$NON-NLS-1$
        + "www.opensourcephysics.org" + newline; //$NON-NLS-1$
    String translator = TrackerRes.getString("Tracker.About.Translator"); //$NON-NLS-1$
    if (!translator.equals("")) { //$NON-NLS-1$
    	aboutString += newline+TrackerRes.getString("Tracker.About.TranslationBy") //$NON-NLS-1$
    			+" "+ translator + newline; //$NON-NLS-1$
    }
    JOptionPane.showMessageDialog(null,
    															aboutString,
                                  TrackerRes.getString("Tracker.Dialog.AboutTracker.Title"), //$NON-NLS-1$
                                  JOptionPane.INFORMATION_MESSAGE);
  }
  
	/**
	 * Finds data functions in all DataBuilder XMLControl files found in a specified directory.
	 * This returns a map for which the keys are paths to DataBuilder xml files and the values
	 * are lists of data functions as Object[] {function name, expression, selected, tracktype}
	 * 
	 * @param dirPath the directory path
	 * @return map of file path to list of data functions
	 */
	public static Map<String, ArrayList<Object[]>> findDataFunctions(String dirPath) {
		Map<String, ArrayList<Object[]>> results = new TreeMap<String, ArrayList<Object[]>>();
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
		    if (type!=null && DataBuilder.class.isAssignableFrom(type)) {
    			ArrayList<Object[]> expandedFunctions = new ArrayList<Object[]>();
		    	
		      // look through XMLControl for data functions            	
	        for (Object next: control.getPropertyContent()) {
	        	if (next instanceof XMLProperty 
	        			&& ((XMLProperty)next).getPropertyName().equals("functions")) { //$NON-NLS-1$
	        		// found DataFunctionPanels
	        		XMLControl[] panels = ((XMLProperty)next).getChildControls();
	        		inner: for (XMLControl panelControl: panels) {
	        			String trackType = panelControl.getString("description"); //$NON-NLS-1$
	        			ArrayList<String[]> functions = (ArrayList<String[]>)panelControl.getObject("functions"); //$NON-NLS-1$
	        			if (trackType==null || functions==null || functions.isEmpty()) continue inner;
	        			
	        			// add localized trackType name to function arrays
	        			for (String[] f: functions) {
	        				Object[] data = new Object[4];
	        				System.arraycopy(f, 0, data, 0, 2);
	        				data[2] = !panelControl.getBoolean("autoload_off_"+data[0]); //$NON-NLS-1$
	        				// use XML.getExtension method to get short name of track type
	        				String trackName = XML.getExtension(trackType);
	        				String localized = TrackerRes.getString(trackName+".Name"); //$NON-NLS-1$
	        				if (!localized.startsWith("!")) //$NON-NLS-1$
	        					trackName = localized;
	        				data[3] = trackName;
	        				expandedFunctions.add(data);
	        			}
	        		} // end inner loop
	        	}
	        } // end outer loop
	        
	        // add entry to the results map
	        results.put(file.getAbsolutePath(), expandedFunctions);
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
    aboutQTAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutQT"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	Diagnostics.aboutQTJava("Tracker"); //$NON-NLS-1$
      }
    };
    aboutXuggleAction = new AbstractAction(TrackerRes.getString("Tracker.Action.AboutXuggle"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	DiagnosticsForXuggle.aboutXuggle("Tracker"); //$NON-NLS-1$
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
  	String[] dirs = new String[] 
  			{System.getProperty("user.home"), OSPRuntime.getLaunchJarDirectory()}; //$NON-NLS-1$
	  for (String dirPath: dirs) {
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
			    if (type!=null && DataBuilder.class.isAssignableFrom(type)) {
		        for (Object next: control.getPropertyContent()) {
		        	if (next instanceof XMLProperty 
		        			&& ((XMLProperty)next).getPropertyName().equals("functions")) { //$NON-NLS-1$
		        		// found DataFunctionPanels
		        		XMLControl[] panels = ((XMLProperty)next).getChildControls();
		        		inner: for (XMLControl panelControl: panels) {
		        			String trackType = panelControl.getString("description"); //$NON-NLS-1$
		        			ArrayList<String[]> functions = (ArrayList<String[]>)panelControl.getObject("functions"); //$NON-NLS-1$
		        			if (trackType==null || functions==null || functions.isEmpty()) 
		        				continue inner;
		        			
		        			// add panel to dataFunctionControls
		        			dataFunctionControls.add(panelControl);
		        		} // end inner loop
		        	}
		        } // end next loop
			    }					
				} // end file loop
			}
	  } // end dirPath loop
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
   * Checks and updates QuickTime resources.
   * 
   * @return true if any resources were updated
   */
  protected static boolean updateResources() {
  	boolean updated = false;
  	// copy xuggle files to Tracker home, if needed
		try {
			File trackerDir = new File(TrackerStarter.findTrackerHome(false));
			updated = ExtensionsManager.getManager().copyXuggleJarsTo(trackerDir);
		} catch (Exception e) {
		}
  	// OSX doesn't need QTJava updating
  	if (OSPRuntime.isMac()) return updated;
  	
  	// copy newer QTJava, if found, to current Java extensions
    String jre = System.getProperty("java.home"); //$NON-NLS-1$
    File extDir = new File(jre, "lib/ext"); //$NON-NLS-1$
    if (extDir.exists()) {
    	updated = ExtensionsManager.getManager().copyQTJavaTo(extDir) || updated;
    }
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
  
  protected static void loadCurrentVersion(boolean ignoreInterval) {  	
  	if (!ignoreInterval) {
	  	// check to see if upgrade interval has passed
	  	long millis = System.currentTimeMillis();
	  	long days = (millis-lastMillisChecked)/86400000;
	  	if (days<checkForUpgradeInterval) return;
  	}
//  	if (true) return; // for PLATO
  	
  	// interval has passed, so check for upgrades and save current time
  	// read current version from version.txt file on tracker home
  	Runnable runner = new Runnable() {
  		public void run() {
  	  	double vers = Double.parseDouble(version);
  	    try {
  	    	URL url = new URL("http://"+trackerWebsite+"/version.txt"); //$NON-NLS-1$ //$NON-NLS-2$
  	    	InputStream is = url.openStream();
  	    	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
  	      String s = reader.readLine();
  	    	while (s != null && s.length()>0) { // typical version: "4.00" or "4.61111227"
  	    		try {
  	    			// convert version string to double and compare with this version
  						double current = Double.parseDouble(s);
  						if (current>vers) {
  							newerVersion = s;
  						}
  						s = null;
  					} catch (Exception e) { // parse failed, so discard first character
  						s = s.substring(1);
  					}
  	    	}	      	
  	    } catch (Exception e) { // url connection failed
  	    }
  		}
  	};
    Thread opener = new Thread(runner);
    opener.setPriority(Thread.NORM_PRIORITY);
    opener.setDaemon(true);
    opener.start();    	  	
  }

  /**
   * Loads preferences from a preferences file, if any.
   */
  protected static void loadPreferences() {
    // look for prefs file in (1) user home, (2) TRACKER_HOME, (3) current directory
    XMLControl control = null;
  	String loadedPath = null;
    // check user home
    String userhome = System.getProperty("user.home"); //$NON-NLS-1$
    if (userhome!=null) {
      prefsPath = userhome+"/"+prefsFileName; //$NON-NLS-1$
      control = new XMLControlElement(prefsPath);
      if (!control.failedToRead()) loadedPath = prefsPath;
    }
    // if not loaded, check TRACKER_HOME
    if (loadedPath==null) {
      if (trackerHome!=null) {
	      String path = trackerHome+"/"+prefsFileName; //$NON-NLS-1$
	      control = new XMLControlElement(path);
	      if (!control.failedToRead()) loadedPath = path;
	      if (prefsPath==null)
	      	prefsPath = path;
      }
    }
    // if not loaded, check launch jar directory
    if (loadedPath==null) {
      String dir = OSPRuntime.getLaunchJarDirectory();
      if (dir!=null) {
        String path = dir+"/"+prefsFileName; //$NON-NLS-1$
        control = new XMLControlElement(path);
	      if (!control.failedToRead()) loadedPath = path;
        if (prefsPath==null && loadedPath!=null)
        	prefsPath = path;   	
      }
    }
    // check current directory
    if (loadedPath==null) {
      File file = new File(prefsFileName);
      if (file.exists()) {
        String path = file.getAbsolutePath();
        control = new XMLControlElement(path);
	      if (!control.failedToRead()) loadedPath = path;
        if (prefsPath==null && loadedPath!=null)
        	prefsPath = path;   	
      }
    }
    if (loadedPath!=null) {
    	OSPLog.getOSPLog();
    	OSPLog.info("loading preferences from "+XML.getAbsolutePath(new File(loadedPath))); //$NON-NLS-1$
    	control.loadObject(null);  // the loader itself sets the values
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
		control.write(prefsPath);
		
		// also write prefs to current directory if it already exists and is writable
    File file = new File(prefsFileName);
    if (file.exists() && file.canWrite()) {
    	control.write(file.getAbsolutePath());
    }
    
		return prefsPath;
  }

  /**
   * Gets the zoomInCursor.
   *
   * @return the cursor
   */
  protected static Cursor getZoomInCursor() {
    if (zoomInCursor == null) {
	    // create cursor
	    String imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/zoom_in.gif";  //$NON-NLS-1$
	    Image im = ResourceLoader.getImage(imageFile);    
	    zoomInCursor = Toolkit.getDefaultToolkit().createCustomCursor(
	    		im, new Point(12, 12), "Zoom In"); //$NON-NLS-1$
    }
    return zoomInCursor;
  }

  /**
   * Gets the zoomOutCursor.
   *
   * @return the cursor
   */
  protected static Cursor getZoomOutCursor() {
    if (zoomOutCursor == null) {
	    // create cursor
    	String imageFile = "/org/opensourcephysics/cabrillo/tracker/resources/images/zoom_out.gif";  //$NON-NLS-1$
    	Image im = ResourceLoader.getImage(imageFile);    
	    zoomOutCursor = Toolkit.getDefaultToolkit().createCustomCursor(
	    		im, new Point(12, 12), "Zoom Out"); //$NON-NLS-1$
    }
    return zoomOutCursor;
  }

  /**
   * Main entry point when used as application.
   *
   * @param args array of tracker or video file names
   */
  public static void main(String[] args) {
//		String[] vars = {"TRACKER_HOME", "XUGGLE_HOME", "DYLD_LIBRARY_PATH"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
//		for (String next: vars) {
//			OSPLog.warning("Environment variable "+next+": "+System.getenv(next)); //$NON-NLS-1$ //$NON-NLS-2$
//		}

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

    
    // get current memory (maximum heap) size
		java.lang.management.MemoryMXBean memory
				= java.lang.management.ManagementFactory.getMemoryMXBean();
    long size = memory.getHeapMemoryUsage().getMax()/(1024*1024);
    
		if (!isRelaunch) {
	    String javaCommand = System.getProperty("java.home");              						//$NON-NLS-1$
	    javaCommand = XML.forwardSlash(javaCommand)+"/bin/java"; //$NON-NLS-1$
	    String javaPath = preferredJRE;
	    if (javaPath!=null) {
	    	File javaFile = OSPRuntime.getJavaFile(javaPath);
	  		if (javaFile!=null && javaFile.exists()) {
	  			javaPath = XML.stripExtension(XML.forwardSlash(javaFile.getPath()));
	  		}
	  		else javaPath = null;
	    }
	    boolean needsJavaVM = javaPath!=null && !javaCommand.equals(javaPath);
	    
			// update resources like QuickTime
			boolean updated = updateResources();
			
			// compare memory with requested size(s)
	    String mem = System.getenv("MEMORY_SIZE"); //$NON-NLS-1$
	    if (mem!=null) {
	    	originalMemoryRequest = requestedMemorySize;
	    	requestedMemorySize = Integer.parseInt(mem);
	    }

	    boolean needsMemory = requestedMemorySize>10 &&
					(size<9*requestedMemorySize/10 || size>11*requestedMemorySize/10);
	    
	    // check environment
	    boolean needsEnvironment = false;
	    try {
				String trackerDir = TrackerStarter.findTrackerHome(false);
		    String trackerEnv = System.getenv("TRACKER_HOME"); //$NON-NLS-1$
				if (trackerDir!=null && !trackerDir.equals(trackerEnv)) {
					needsEnvironment = true;
				}
				else {
					String xuggleDir = TrackerStarter.findXuggleHome(trackerDir, false);
					String xuggleEnv = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
					if (xuggleDir!=null && !xuggleDir.equals(xuggleEnv)) {
						needsEnvironment = true;					
					}
					else {
						if (xuggleDir!=null) {
							String subdir = OSPRuntime.isWindows()? "bin": "lib"; //$NON-NLS-1$ //$NON-NLS-2$
							String xugglePath = xuggleDir+File.separator+subdir;
							String pathName = OSPRuntime.isWindows()? "Path":  //$NON-NLS-1$
								OSPRuntime.isMac()? "DYLD_LIBRARY_PATH": "LD_LIBRARY_PATH"; //$NON-NLS-1$ //$NON-NLS-2$
							String pathEnv = System.getenv(pathName);
							if (pathEnv==null || !pathEnv.contains(xugglePath)) {
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
	    		requestedMemorySize = (int)size;
	    	}
	    	System.setProperty(TrackerStarter.PREFERRED_MEMORY_SIZE, String.valueOf(requestedMemorySize));
	    	System.setProperty(TrackerStarter.PREFERRED_TRACKER_JAR, OSPRuntime.getLaunchJarPath());

	    	TrackerStarter.relaunch(mainArgs, true);
		    return;
			}
		}
    preferredMemorySize = requestedMemorySize;
    if (requestedMemorySize<0)
    	requestedMemorySize = (int)(size+2);
    start(args);
  }

  /**
   * Starts a new Tracker.
   *
   * @param args array of tracker or video file names
   */
  private static void start(String[] args) {
    Tracker tracker = null;
    if (args == null || args.length == 0) tracker = new Tracker();
    else tracker = new Tracker(args, true);
    
  	if (OSPRuntime.isMac()) {
			// instantiate the OSXServices class by reflection
			String className = "org.opensourcephysics.cabrillo.tracker.deploy.OSXServices"; //$NON-NLS-1$
	    try {
				Class<?> OSXClass = Class.forName(className);
				Constructor<?> constructor = OSXClass.getConstructor(Tracker.class);
				constructor.newInstance(tracker);
			} catch (Exception ex) {
			}
		}
 
  	FontSizer.setLevel(preferredFontLevel);
  	TFrame frame = tracker.getFrame();
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
    	boolean xuggleInstalled = VideoIO.guessXuggleVersion()!=0;
    	boolean qtInstalled = ExtensionsManager.getManager().getQTJavaZip()!=null;
    	
    	ArrayList<String> message = new ArrayList<String>();    	
			boolean showRelaunchDialog = false;
	    	
    	// no engine installed
    	if (!xuggleInstalled && !qtInstalled) {
    		message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message1")); //$NON-NLS-1$
    		message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message2")); //$NON-NLS-1$
    		message.add(" "); //$NON-NLS-1$
    		message.add(TrackerRes.getString("Tracker.Dialog.NoVideoEngine.Message3")); //$NON-NLS-1$
    	}
    	
    	// engines installed on Windows but no 32-bit VM
    	else if (OSPRuntime.isWindows() && ExtensionsManager.getManager().getDefaultJRE(32)==null) {
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
  				PrefsDialog prefs = frame.getPrefsDialog();
  				prefs.vm32Button.setSelected(true); // also sets default video engine
  				// check that not canceled by user
  				if (!"cancel".equals(prefs.vm32Button.getName())) { //$NON-NLS-1$
  					prefs.relaunchButton.doClick(0);
  				}
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
	    int vmBitness = OSPRuntime.getVMBitness();
			use32BitMode = vmBitness==32;
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

    testPanel = frame.getTrackerPanel(0);
  }

  /**
   * Displays a message in the splash screen.
   * @param message 
   */
  protected static void splash(String message) {
  	if (tipOfTheDayLabel != null) tipOfTheDayLabel.setText(message);
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
   * Loads preferences from the deprecated tracker_starter prefs file.
   */
  protected static void loadStarterPrefs() {
	    // look for starter prefs file in user home
  	File starterPrefsFile = null;
  	String userHome = System.getProperty("user.home"); //$NON-NLS-1$
  	if (userHome!=null) {
  		starterPrefsFile = new File(userHome, ".tracker_starter.prefs"); //$NON-NLS-1$
  	}
  	// if not found, check trackerHome
  	if (trackerHome!=null && (starterPrefsFile==null || !starterPrefsFile.exists())) {
  		starterPrefsFile = new File(trackerHome, ".tracker_starter.prefs"); //$NON-NLS-1$
  	}

  	if (starterPrefsFile.exists()) try {
  		String encoding = "UTF-8"; //$NON-NLS-1$
  	  java.nio.charset.Charset charset = java.nio.charset.Charset.forName(encoding);
    	InputStream stream =  new FileInputStream(starterPrefsFile);
    	BufferedReader in = new BufferedReader(new InputStreamReader(stream, charset));
      String line = in.readLine();
      ArrayList<String> runPaths = new ArrayList<String>();
      while(line!=null) {
       	if (line.startsWith("jre ")) { //$NON-NLS-1$
      		File javaFile = new File(line.substring(4).trim());
      		String jre = OSPRuntime.getJREPath(javaFile);
      		Tracker.preferredJRE = "".equals(jre)? null: jre; //$NON-NLS-1$
      	}
       	else if (line.startsWith("run ")) { //$NON-NLS-1$
      		String app = line.substring(4).trim();
        	if (!"".equals(app)) runPaths.add(app);        	 //$NON-NLS-1$
       	}
       	else if (line.startsWith("version ")) { //$NON-NLS-1$
      		String ver = line.substring(8).trim();
					try {
						Double.parseDouble(ver);
						Tracker.preferredTrackerJar = "tracker-"+ver+".jar"; //$NON-NLS-1$ //$NON-NLS-2$
					} catch (Exception ex) {}
       	}
        line = in.readLine();
      }
      in.close();
      Tracker.prelaunchExecutables = runPaths.toArray(new String[0]);
    } 
		catch (Exception ex) {}
  }

//	static class TrackerOSXHandler implements com.apple.eawt.AboutHandler, 
//			com.apple.eawt.QuitHandler, com.apple.eawt.PreferencesHandler {
//		Tracker tracker;
//		
//		TrackerOSXHandler(Tracker app) {
//			tracker = app;
//		}
//		
//		public void handleAbout(com.apple.eawt.AppEvent.AboutEvent e) {
//			aboutTrackerAction.actionPerformed(null);
//		} 
//		
//		public void handleQuitRequestWith(com.apple.eawt.AppEvent.QuitEvent e,
//				com.apple.eawt.QuitResponse response) {
//      TFrame frame = tracker.getFrame();
//      if (frame != null) {
//        for (int i = 0; i < frame.getTabCount(); i++) {
//        	// save tabs in try/catch block so always closes
//          try {
//						if (!frame.getTrackerPanel(i).save()) {
//						  response.cancelQuit();
//						}
//					} catch (Exception ex) {
//					}
//        }
//      }
//			response.performQuit();
//		}
//		
//		public void handlePreferences(com.apple.eawt.AppEvent.PreferencesEvent e) {
//			TFrame frame = tracker.getFrame();
//    	if (frame.prefsDialog!=null) {
//    		frame.prefsDialog.refreshGUI();
//    	}
//    	else {
//        // create PrefsDialog
//    		frame.prefsDialog = new PrefsDialog(null, frame);
//        // center on screen
//        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//        int x = (dim.width - frame.prefsDialog.getBounds().width) / 2;
//        int y = (dim.height - frame.prefsDialog.getBounds().height) / 2;
//        frame.prefsDialog.setLocation(x, y);
//    	}
//    	frame.prefsDialog.setVisible(true);
//    	frame.prefsDialog.requestFocus();
//		}
//		
//	}
//
  
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
      	if (Tracker.isXuggleFast) // false by default
      		control.setValue("xuggle_fast", Tracker.isXuggleFast); //$NON-NLS-1$
      	if (!Tracker.warnNoVideoEngine) // true by default
      		control.setValue("warn_no_engine", Tracker.warnNoVideoEngine); //$NON-NLS-1$
      	if (!Tracker.warnVariableDuration) // true by default
      		control.setValue("warn_variable_frame_duration", Tracker.warnVariableDuration); //$NON-NLS-1$
      	if (!Tracker.warnXuggleError) // true by default
      		control.setValue("warn_xuggle_error", Tracker.warnXuggleError); //$NON-NLS-1$
      	// always save preferred tracker.jar
      	String jar = Tracker.preferredTrackerJar==null? 
      			"tracker.jar": Tracker.preferredTrackerJar; //$NON-NLS-1$
      	control.setValue("tracker_jar", jar); //$NON-NLS-1$
      	if (Tracker.preferredJRE!=null)
      		control.setValue("java_vm", Tracker.preferredJRE); //$NON-NLS-1$
      	if (Tracker.preferredJRE32!=null)
      		control.setValue("java_vm_32", Tracker.preferredJRE32); //$NON-NLS-1$
      	if (Tracker.preferredJRE64!=null)
      		control.setValue("java_vm_64", Tracker.preferredJRE64); //$NON-NLS-1$
      	if (Tracker.use32BitMode)
      		control.setValue("32-bit", Tracker.use32BitMode); //$NON-NLS-1$
      	if (Tracker.preferredMemorySize>-1) // -1 by default
      		control.setValue("memory_size", Tracker.preferredMemorySize); //$NON-NLS-1$
      	if (Tracker.lookAndFeel!=null)
      		control.setValue("look_feel", Tracker.lookAndFeel); //$NON-NLS-1$
      	if (Tracker.prelaunchExecutables.length>0)
      		control.setValue("run", Tracker.prelaunchExecutables); //$NON-NLS-1$
      	if (Tracker.preferredLocale!=null)
      		control.setValue("locale", Tracker.preferredLocale); //$NON-NLS-1$
      	if (Tracker.preferredFontLevel>0)
      		control.setValue("font_size", Tracker.preferredFontLevel); //$NON-NLS-1$
      	if (ResourceLoader.getOSPCache()!=null) {
      		File cache = ResourceLoader.getOSPCache();
      		control.setValue("cache", cache.getPath()); //$NON-NLS-1$
      	}
      	if (Tracker.checkForUpgradeInterval>0) {
      		control.setValue("upgrade_interval", Tracker.checkForUpgradeInterval); //$NON-NLS-1$
      		int lastChecked = (int)(Tracker.lastMillisChecked/1000L);
      		control.setValue("last_checked", lastChecked); //$NON-NLS-1$
      	}
      	JFileChooser chooser = VideoIO.getChooser();
      	File file = chooser.getCurrentDirectory();
        String userDir = System.getProperty("user.dir"); //$NON-NLS-1$
        if (!file.getAbsolutePath().equals(userDir)) // user.dir by default
        	control.setValue("file_chooser_directory", XML.getAbsolutePath(file)); //$NON-NLS-1$
        
        // qt_preferred--legacy preference for pre-version 4.75
        if (VideoIO.getEngine().equals(VideoIO.ENGINE_QUICKTIME))
      		control.setValue("qt_preferred", true); //$NON-NLS-1$
        
        // video_engine--used by version 4.75+
        if (Tracker.engineKnown) // true by default
        	control.setValue("video_engine", VideoIO.getEngine()); //$NON-NLS-1$
        if (!VideoIO.getPreferredExportExtension().equals(VideoIO.DEFAULT_PREFERRED_EXPORT_EXTENSION))
        	control.setValue("export_extension", VideoIO.getPreferredExportExtension()); //$NON-NLS-1$
        if (!ExportZipDialog.preferredExtension.equals(ExportZipDialog.DEFAULT_VIDEO_EXTENSION))
        	control.setValue("zip_export_extension", ExportZipDialog.preferredExtension); //$NON-NLS-1$

        if (Tracker.recentFilesSize!=6) // 6 items by default
      		control.setValue("max_recent", Tracker.recentFilesSize); //$NON-NLS-1$
      	if (!Tracker.recentFiles.isEmpty()) // empty by default
      		control.setValue("recent_files", Tracker.recentFiles); //$NON-NLS-1$
      	if (!Tracker.dataFunctionControlStrings.isEmpty()) {
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
        }
      	Tracker.isRadians = control.getBoolean("radians"); //$NON-NLS-1$
      	Tracker.isXuggleFast = control.getBoolean("xuggle_fast"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("warn_no_engine")) //$NON-NLS-1$
      		Tracker.warnNoVideoEngine = control.getBoolean("warn_no_engine"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("warn_xuggle_error")) //$NON-NLS-1$
      		Tracker.warnXuggleError = control.getBoolean("warn_xuggle_error"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("warn_variable_frame_duration")) //$NON-NLS-1$
      		Tracker.warnVariableDuration = control.getBoolean("warn_variable_frame_duration"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("show_hints")) { //$NON-NLS-1$
      		Tracker.showHintsByDefault = control.getBoolean("show_hints"); //$NON-NLS-1$
      		Tracker.showHints = Tracker.showHintsByDefault;
      		Tracker.startupHintShown = !Tracker.showHints;
      	}
      	if (control.getPropertyNames().contains("java_vm")) //$NON-NLS-1$
      		Tracker.preferredJRE = control.getString("java_vm"); //$NON-NLS-1$
    		Tracker.preferredJRE32 = control.getString("java_vm_32"); //$NON-NLS-1$
    		Tracker.preferredJRE64 = control.getString("java_vm_64"); //$NON-NLS-1$
      	Tracker.use32BitMode = control.getBoolean("32-bit"); //$NON-NLS-1$
  	    if (control.getPropertyNames().contains("memory_size")) //$NON-NLS-1$
      		Tracker.requestedMemorySize = control.getInt("memory_size"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("look_feel")) //$NON-NLS-1$
      		Tracker.lookAndFeel = control.getString("look_feel"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("run")) //$NON-NLS-1$
      		Tracker.prelaunchExecutables = (String[])control.getObject("run"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("locale")) //$NON-NLS-1$
      		Tracker.setPreferredLocale(control.getString("locale")); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("font_size")) //$NON-NLS-1$
      		Tracker.preferredFontLevel = control.getInt("font_size"); //$NON-NLS-1$
      	// set cache only if it has not yet been set
      	if (ResourceLoader.getOSPCache()==null) {
      		Tracker.setCache(control.getString("cache")); //$NON-NLS-1$
      	}
      	if (control.getPropertyNames().contains("upgrade_interval")) { //$NON-NLS-1$
      		Tracker.checkForUpgradeInterval = control.getInt("upgrade_interval"); //$NON-NLS-1$
      		Tracker.lastMillisChecked = control.getInt("last_checked")*1000L; //$NON-NLS-1$
      	}
      	if (control.getPropertyNames().contains("file_chooser_directory")) //$NON-NLS-1$
      		OSPRuntime.chooserDir = control.getString("file_chooser_directory"); //$NON-NLS-1$
      	
      	// preferred video engine
      	// qt_preferred is legacy variable
//      	if (control.getPropertyNames().contains("qt_preferred")) //$NON-NLS-1$
//      		VideoIO.setQTLast(false);
      	// VideoIO engine is new variable
      	VideoIO.setEngine(control.getString("video_engine")); //$NON-NLS-1$
      	VideoIO.setPreferredExportExtension(control.getString("export_extension")); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("zip_export_extension")) //$NON-NLS-1$
      		ExportZipDialog.preferredExtension = control.getString("zip_export_extension"); //$NON-NLS-1$

      	if (control.getPropertyNames().contains("max_recent")) //$NON-NLS-1$
      		recentFilesSize = control.getInt("max_recent"); //$NON-NLS-1$
      	if (control.getPropertyNames().contains("recent_files")) { //$NON-NLS-1$
  	    	ArrayList<?> recent = ArrayList.class.cast(control.getObject("recent_files")); //$NON-NLS-1$
  	    	for (Object next: recent) {
  	    	  addRecent(next.toString(), true); // add at end
  	    	}
      	}
      	// load data functions (deprecated: as of Dec 2014, autoloadDataFunctions() used instead)
      	if (control.getPropertyNames().contains("data_functions")) { //$NON-NLS-1$
      		Tracker.dataFunctionControlStrings.addAll((Collection<String>)control.getObject("data_functions")); //$NON-NLS-1$
      	}
    		XMLControl child = control.getChildControl("configuration"); //$NON-NLS-1$
    		if (child!=null) {
    			Configuration config = (Configuration)child.loadObject(null);
    			setDefaultConfig(config.enabled);
    		}
      	// always load "tracker_jar"
      	Tracker.preferredTrackerJar = control.getString("tracker_jar"); //$NON-NLS-1$
      	if (Tracker.preferredTrackerJar==null)
      		Tracker.loadStarterPrefs();
      	return obj;
      }
    }  	
  }
  
}
