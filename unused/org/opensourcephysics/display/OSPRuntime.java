/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.Translator;

import javajs.async.AsyncFileChooser;

/**
 * This class defines static methods related to the runtime environment.
 *
 * @author Douglas Brown
 * @author Wolfgang Christian
 * @version 1.0
 */
public class OSPRuntime {
  public static final String VERSION = "5.0.0";                                                                            //$NON-NLS-1$

  public static boolean isJS = /** @j2sNative true || */ false;
  
  /** Disables drawing for faster start-up and to avoid screen flash in Drawing Panels. */
  volatile public static boolean disableAllDrawing = false;

  /** Load Video Tool, if available. */
  public static boolean loadVideoTool = true;

  /** Load Export Tool, if available. */
  public static boolean loadExportTool = true;

  /** Load Data Tool, if available. */
  public static boolean loadDataTool = true;

  /** Load Fourier Tool, if available. */
  public static boolean loadFourierTool = true;

  /** Load Translator Tool, if available. */
  public static boolean loadTranslatorTool = true;

  /** Load OSP Log, if available. */
  public static boolean loadOSPLog = true;

  /** Shared Translator after loading, if available. */
  private static Translator translator;                                                                     // shared Translator

  /** Array of default OSP Locales. */
  public static Locale[] defaultLocales = new Locale[] {Locale.ENGLISH, new Locale("es"), new Locale("de"), //$NON-NLS-1$ //$NON-NLS-2$
    new Locale("da"), new Locale("sk"), Locale.TAIWAN};                                                       //$NON-NLS-1$ //$NON-NLS-2$

  /** Set <I>true</I> if a program is being run within Launcher. */
  protected static boolean launcherMode = false;

  /** True if text components should try and anti-alias text. */
  public static Boolean antiAliasText = false;

  /** True if running as an applet. */
  public static boolean appletMode;

  /** Static reference to an applet for document/code base access. */
  public static JApplet applet;

  /** True if launched by WebStart. */
  public static boolean webStart;
  
  /** True if launched by WebStart. */
//  public static boolean J3D;

  /** True if users allowed to author internal parameters such as Locale strings. */
  protected static boolean authorMode = true;

  /** Path of the launch jar, if any. */
  private static String launchJarPath;

  /** Path of the launch jar, if any. */
  private static String launchJarName;

  /** The launch jar, if any. */
  private static JarFile launchJar = null;

  /** Build date of the launch jar, if known. */
  private static String buildDate;

  /** The default decimal separator */
  private static char defaultDecimalSeparator;

  /** The preferred decimal separator, if any */
  private static String preferredDecimalSeparator;

  /** File Chooser starting directory. */
  public static String chooserDir;

  /** User home directory. */
  public static String userhomeDir;

  /** Location of OSP icon. */
  public static final String OSP_ICON_FILE = "/org/opensourcephysics/resources/controls/images/osp_icon.gif"; //$NON-NLS-1$

  /** True if always launching in single vm (applet mode, etc). */
  public static boolean launchingInSingleVM;
  // look and feel types
  @SuppressWarnings("javadoc")
	public final static String CROSS_PLATFORM_LF = "CROSS_PLATFORM";                    //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String NIMBUS_LF = "NIMBUS";                                    //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String SYSTEM_LF = "SYSTEM";                                    //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String METAL_LF = "METAL";                                      //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String GTK_LF = "GTK";                                          //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String MOTIF_LF = "MOTIF";                                      //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String WINDOWS_LF = "WINDOWS";                                  //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String DEFAULT_LF = "DEFAULT";                                  //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static LookAndFeel DEFAULT_LOOK_AND_FEEL = UIManager.getLookAndFeel(); // save the default before we change LnF
  @SuppressWarnings("javadoc")
	public final static boolean DEFAULT_LOOK_AND_FEEL_DECORATIONS = JFrame.isDefaultLookAndFeelDecorated();
  @SuppressWarnings("javadoc")
	public final static HashMap<String, String> LOOK_AND_FEEL_TYPES = new HashMap<String, String>();

  /** Preferences XML control */
  private static XMLControl prefsControl;

  /** Preferences path */
  private static String prefsPath;
  
  /** Preferences filename */
  private static String prefsFileName = "osp.prefs"; //$NON-NLS-1$

  /**
   * Sets default properties for OSP.
   */
  static {
    try { // set the user home and default directory for the chooser                                                                             // system properties may not be readable in some contexts
      OSPRuntime.chooserDir = System.getProperty("user.dir", null);                            //$NON-NLS-1$
      String userhome = getUserHome();
      if (userhome!=null) {
      	userhomeDir = XML.forwardSlash(userhome);
      }
    } catch(Exception ex) {
      OSPRuntime.chooserDir = null;
    }
    // fill the look and feel map
    LOOK_AND_FEEL_TYPES.put(METAL_LF, "javax.swing.plaf.metal.MetalLookAndFeel");              //$NON-NLS-1$
    LOOK_AND_FEEL_TYPES.put(NIMBUS_LF, "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");    //$NON-NLS-1$
    LOOK_AND_FEEL_TYPES.put(GTK_LF, "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");             //$NON-NLS-1$
    LOOK_AND_FEEL_TYPES.put(MOTIF_LF, "com.sun.java.swing.plaf.motif.MotifLookAndFeel");       //$NON-NLS-1$
    LOOK_AND_FEEL_TYPES.put(WINDOWS_LF, "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //$NON-NLS-1$
    LOOK_AND_FEEL_TYPES.put(CROSS_PLATFORM_LF, UIManager.getCrossPlatformLookAndFeelClassName());
    LOOK_AND_FEEL_TYPES.put(SYSTEM_LF, UIManager.getSystemLookAndFeelClassName());
    LOOK_AND_FEEL_TYPES.put(DEFAULT_LF, DEFAULT_LOOK_AND_FEEL.getClass().getName());

    NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
    if (format instanceof DecimalFormat) {
      defaultDecimalSeparator = ((DecimalFormat)format).getDecimalFormatSymbols().getDecimalSeparator();
    }
    else {
      defaultDecimalSeparator = new DecimalFormat().getDecimalFormatSymbols().getDecimalSeparator();
    }

//	try {
//	  Class.forName("com.sun.j3d.utils.universe.SimpleUniverse"); //$NON-NLS-1$
//	  J3D= true; 
//	} catch (NoClassDefFoundError e) { // Do not complain
//	  J3D=  false; 
//	} catch (ClassNotFoundException e) {
//	  J3D=  false; 
//	}
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private OSPRuntime() {
    /** empty block */
  }
  
  /**
   * Gets the user home directory.
   * @return the user home 
   */
  public static String getUserHome() {
    String home = System.getProperty("user.home"); //$NON-NLS-1$
    if (isLinux()) {
  		String homeEnv = System.getenv("HOME"); //$NON-NLS-1$
  		if (homeEnv!=null) {
  			home = homeEnv;
  		}
    }
    return home==null? ".": home; //$NON-NLS-1$
  }

  /**
   * Gets the download directory.
   * @return the download directory
   */
  public static File getDownloadDir() {
  	String home = getUserHome();
		File downloadDir = new File(home+"/Downloads"); //$NON-NLS-1$
		if (isLinux()) {
			// get XDG_DOWNLOAD_DIR if possible--usually "$HOME/xxx" but may be absolute
			String xdgDir = home+"/.config/user-dirs.dirs"; //$NON-NLS-1$
			String xdgText = ResourceLoader.getString(xdgDir);
			if (xdgText!=null) {
				String[] split = xdgText.split("XDG_"); //$NON-NLS-1$
				for (String next: split) {
					if (next.contains("DOWNLOAD_DIR")) { //$NON-NLS-1$
						// get name between quotes
						int n = next.indexOf("\""); //$NON-NLS-1$
						if (n>-1) {
							next = next.substring(n+1);
							n = next.indexOf("\""); //$NON-NLS-1$
							if (n>-1) {
								next = next.substring(0, n);
								// substitute home for $HOME
	  	    			if (next.startsWith("$HOME")) { //$NON-NLS-1$
	  	    				next = home+next.substring(5);
	  	    			}
								File f = new File(next);
								if (f.exists()) {
									downloadDir = f;
								}
							}
						}
					}
				}
			}
		}
    return downloadDir;
  }

  /**
   * Shows the about dialog.
   * @param parent 
   */
  public static void showAboutDialog(Component parent) {
		String date = OSPRuntime.getLaunchJarBuildDate();
		date ="February 1, 2020";
		String vers = "JavaScript OSP Library "+VERSION; //$NON-NLS-1$
		vers +="\n\nJavaScript transcription created using the\n"+
		"java2script/SwingJS framework developed at\n"+
				"St. Olaf College.\n";
		if (date!=null) {
			vers += " released "+date; //$NON-NLS-1$
		}
    String aboutString = vers+"\n"           //$NON-NLS-1$
                         +"Open Source Physics Project \n"+"www.opensourcephysics.org"; //$NON-NLS-1$ //$NON-NLS-2$
    JOptionPane.showMessageDialog(parent, aboutString, "About Open Source Physics", JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
  }

  /**
   * Sets the look and feel of the user interface.  Look and feel user interfaces are:
   *
   * NIMBUS_LF: com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
   * METAL_LF: javax.swing.plaf.metal.MetalLookAndFeel
   * GTK_LF: com.sun.java.swing.plaf.gtk.GTKLookAndFeel
   * MOTIF_LF: com.sun.java.swing.plaf.motif.MotifLookAndFeel
   * WINDOWS_LF: com.sun.java.swing.plaf.windows.WindowsLookAndFeel
   * DEFAULT_LF: the default look and feel in effect when this class was loaded
   * CROSS_PLATFORM_LF: the cross platform look and feel; usually METAL_LF
   * SYSTEM_LF: the operating system look and feel
   *
   * @param useDefaultLnFDecorations
   * @param lookAndFeel
   *
   * @return true if successful
   */
  public static boolean setLookAndFeel(boolean useDefaultLnFDecorations, String lookAndFeel) {
    boolean found = true;
    LookAndFeel currentLookAndFeel = UIManager.getLookAndFeel();
    try {
      if((lookAndFeel==null)||lookAndFeel.equals(DEFAULT_LF)) {
        UIManager.setLookAndFeel(DEFAULT_LOOK_AND_FEEL);
        useDefaultLnFDecorations = DEFAULT_LOOK_AND_FEEL_DECORATIONS;
      } else if(lookAndFeel.equals(CROSS_PLATFORM_LF)) {
        lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
        UIManager.setLookAndFeel(lookAndFeel);
      } else if(lookAndFeel.equals(SYSTEM_LF)) {
        lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(lookAndFeel);
      } else if(lookAndFeel.equals(NIMBUS_LF)) {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");   //$NON-NLS-1$
      } else if(lookAndFeel.equals(METAL_LF)) {
        //        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        UIManager.setLookAndFeel(new MetalLookAndFeel());
      } else if(lookAndFeel.equals(GTK_LF)) {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");         //$NON-NLS-1$
      } else if(lookAndFeel.equals(MOTIF_LF)) {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");     //$NON-NLS-1$
      } else if(lookAndFeel.equals(WINDOWS_LF)) {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //$NON-NLS-1$
      } else {
        UIManager.setLookAndFeel(lookAndFeel);                                          // LnF can be set using a fully qualified path
      }
      JFrame.setDefaultLookAndFeelDecorated(useDefaultLnFDecorations);
      JDialog.setDefaultLookAndFeelDecorated(useDefaultLnFDecorations);
    } catch(Exception ex) {
      found = false;
    }
    if(!found) { // keep current look and feel
      try {
        UIManager.setLookAndFeel(currentLookAndFeel);
      } catch(Exception e) {}
    }
    return found;
  }

  /**
   * Returns true if newly created <code>JFrame</code>s or <code>JDialog</code>s should have their
   * Window decorations provided by the current look and feel. This is only
   * a hint, as certain look and feels may not support this feature.
   *
   * @return true if look and feel should provide Window decorations.
   * @since 1.4
   */
  public static boolean isDefaultLookAndFeelDecorated() {
    return JFrame.isDefaultLookAndFeelDecorated();
  }

  /**
   * Determines if OS is Windows
   *
   * @return true if Windows
   */
  public static boolean isWindows() {
    try {                                                                            // system properties may not be readable in some environments
      return(System.getProperty("os.name", "").toLowerCase().startsWith("windows")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch(SecurityException ex) {
      return false;
    }
  }

  /**
   * Determines if OS is Mac
   *
   * @return true if Mac
   */
  public static boolean isMac() {
    try {                                                                        // system properties may not be readable in some environments
      return(System.getProperty("os.name", "").toLowerCase().startsWith("mac")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch(SecurityException ex) {
      return false;
    }
  }

  /**
   * Determines if OS is Linux
   *
   * @return true if Linux
   */
  public static boolean isLinux() {
    try {                                                                          // system properties may not be readable in some environments
      return(System.getProperty("os.name", "").toLowerCase().startsWith("linux")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch(SecurityException ex) {
      return false;
    }
  }

  /**
   * Determines if OS is Vista
   *
   * @return true if Vista
   */
  static public boolean isVista() {
    if(System.getProperty("os.name", "").toLowerCase().indexOf("vista")>-1) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return true;
    }
    return false;
  }
  
  static public boolean hasJava3D() {
    try{
      if (OSPRuntime.isMac()) {  // extra testing for Mac
        boolean tryIt=true;
        String home = System.getProperty("java.home");//$NON-NLS-1$
        String version = System.getProperty("java.version"); //$NON-NLS-1$
        if (version.indexOf("1.7")<0  && version.indexOf("1.8")<0) tryIt = true; //$NON-NLS-1$ //$NON-NLS-2$
        else tryIt = (new java.io.File(home+"/lib/ext/j3dcore.jar")).exists(); //$NON-NLS-1$
        if (!tryIt) return false;
      }
    } catch (Exception exc) { return false; } // Any problem! Do not complain and quit
    // look for J3D class
 	try {
      Class.forName("com.sun.j3d.utils.universe.SimpleUniverse"); //$NON-NLS-1$
      return true; 
	} catch (Error e) {  // do not not complain
		  return false; 
    }
 	catch (Exception e) {  // do not not complain
		  return false; 
    }
} 
  
  /**
   * Determines if an InputEvent is a popup trigger.
   * @param e the input event
   * @return true if event is a popup trigger
   */
  static public boolean isPopupTrigger(InputEvent e) {
    if(e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      if (me.isShiftDown()) return false;
      return (me.isPopupTrigger())
      		||(me.getButton()==MouseEvent.BUTTON3)
      		||(me.isControlDown()&&isMac());
    }
    return false;
  }

  /**
   * Determines if launched by WebStart
   *
   * @return true if launched by WebStart
   */
  public static boolean isWebStart() {
    if(!webStart) {
      try {
        webStart = System.getProperty("javawebstart.version")!=null; //$NON-NLS-1$
        // once true, remains true
      } catch(Exception ex) {
      }
    }
    return webStart;
  }

  /**
   * Determines if running as an applet
   *
   * @return true if running as an applet
   */
  public static boolean isAppletMode() {
    return appletMode;
  }

  /**
   * Determines if running in author mode
   *
   * @return true if running in author mode
   */
  public static boolean isAuthorMode() {
    return authorMode;
  }

  /**
   * Sets the authorMode property.
   * AuthorMode allows users to author internal parameters such as Locale strings.
   *
   * @param b boolean
   */
  public static void setAuthorMode(boolean b) {
    authorMode = b;
  }

  /**
   * Sets the launcherMode property to true if applications in this VM are launched by Launcher.
   * LauncherMode disables access to properties, such as Locale, that affect the VM.
   *
   * @param b boolean
   */
  public static void setLauncherMode(boolean b) {
    launcherMode = b;
  }

  /**
   * Gets the launcherMode property. Returns true if applications in this VM are launched by Launcher.
   * LauncherMode disables access to properties, such as Locale, that affect the VM.
   *
   * @return boolean
   */
  public static boolean isLauncherMode() {
	  return launcherMode || "true".equals(System.getProperty("org.osp.launcher")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Sets the launch jar path.
   * @param path the path
   */
  public static void setLaunchJarPath(String path) {
    if((path==null)||(launchJarPath!=null)) {
      return;
    }
    // make sure the path ends with or contains a jar file
    if(!path.endsWith(".jar")&&!path.endsWith(".exe")) { //$NON-NLS-1$ //$NON-NLS-2$
      int n = path.indexOf(".jar!");                     //$NON-NLS-1$
      if(n==-1) {
        n = path.indexOf(".exe!");                       //$NON-NLS-1$
      }
      if(n>-1) {
        path = path.substring(0, n+4);
      } else {
        return;
      }
    }
		if (path.startsWith("jar:")) { //$NON-NLS-1$
			path = path.substring(4, path.length());
		}
		try {
			// check that file exists and set launchJarPath to file path
			File file = new File(path);
			if (!file.exists())  return;
			path = XML.forwardSlash(file.getCanonicalPath());
		} catch (Exception ex) {
		}
    OSPLog.finer("Setting launch jar path to "+path); //$NON-NLS-1$
    launchJarPath = path;
    launchJarName = path.substring(path.lastIndexOf("/")+1); //$NON-NLS-1$
  }

  /**
   * Gets the launch jar name, if any.
   * @return launch jar path, or null if not launched from a jar
   */
  public static String getLaunchJarName() {
    return launchJarName;
  }

  /**
   * Gets the launch jar path, if any.
   * @return launch jar path, or null if not launched from a jar
   */
  public static String getLaunchJarPath() {
    return launchJarPath;
  }

  /**
   * Gets the launch jar directory, if any.
   * @return path to the directory containing the launch jar. May be null.
   */
  public static String getLaunchJarDirectory() {
    if(applet!=null) {
      return null;
    }
    return(launchJarPath==null) ? null : XML.getDirectoryPath(launchJarPath);
  }

  /**
   * Gets the jar from which the progam was launched.
   * @return JarFile
   */
  public static JarFile getLaunchJar() {
    if(launchJar!=null) {
      return launchJar;
    }
    if(launchJarPath==null) {
      return null;
    }
    boolean isWebFile = launchJarPath.startsWith("http:"); //$NON-NLS-1$
    if (!isWebFile) {
    	launchJarPath = ResourceLoader.getNonURIPath(launchJarPath);
    }
    try {
      if((OSPRuntime.applet==null)&&!isWebFile) {         // application mode
        launchJar = new JarFile(launchJarPath);
      } else {                                            // applet mode
        URL url;
        if(isWebFile) {
          // create a URL that refers to a jar file on the web
          url = new URL("jar:"+launchJarPath+"!/");       //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          // create a URL that refers to a local jar file
          url = new URL("jar:file:/"+launchJarPath+"!/"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // get the jar
        JarURLConnection conn = (JarURLConnection) url.openConnection();
        launchJar = conn.getJarFile();
      }
    } catch(Exception ex) {
      //ex.printStackTrace();
      OSPLog.fine(ex.getMessage());
    }
    return launchJar;
  }

  /**
   * Gets the launch jar build date.
   * @return the build date, or null if not launched from a jar or date not known
   */
  public static String getLaunchJarBuildDate() {
  	if (buildDate==null) {
	    try {
				JarFile jarfile = getLaunchJar();
				java.util.jar.Attributes att = jarfile.getManifest().getMainAttributes();
				buildDate = att.getValue("Build-Date"); //$NON-NLS-1$
			} catch (Exception ex) {} 		
  	}
    return buildDate;
  }

  /**
   * Gets the java executable file for a given jre path. May return null.
   * 
   * @param jrePath the path to a java jre or jdk VM
   * @return the Java executable
   */
  public static File getJavaFile(String jrePath) {
  	if (jrePath==null) return null;
  	File file = new File(jrePath);
  	jrePath = XML.forwardSlash(jrePath);
  	if (jrePath.endsWith("/lib/ext")) { //$NON-NLS-1$
  		jrePath = jrePath.substring(0, jrePath.length()-8);
  		file = new File(jrePath);
  	}
  	if (!jrePath.endsWith("/bin/java") && !jrePath.endsWith("/bin/java.exe")) { //$NON-NLS-1$ //$NON-NLS-2$
    	if (jrePath.endsWith("/bin")) { //$NON-NLS-1$
    		file = file.getParentFile();
    	}
	  	if (OSPRuntime.isWindows()) {
	  		// typical jdk: Program Files\Java\jdkX.X.X_XX\jre\bin\java.exe
	  		// typical jre: Program Files\Java\jreX.X.X_XX\bin\java.exe
	  		//           or Program Files\Java\jreX\bin\java.exe
	  		// typical 32-bit jdk in 64-bit Windows: Program Files(x86)\Java\jdkX.X.X_XX\jre\bin\java.exe
	  		if (file.getParentFile()!=null
	  				&& file.getParentFile().getName().indexOf("jre")>-1) { //$NON-NLS-1$
	  			file = file.getParentFile();
	  		}
	  		if (file.getParentFile()!=null
	  				&& file.getParentFile().getName().indexOf("jdk")>-1) { //$NON-NLS-1$
	  			file = file.getParentFile();
	  		}
	  		if (file.getName().indexOf("jdk")>-1) //$NON-NLS-1$
	  			file = new File(file, "jre/bin/java.exe"); //$NON-NLS-1$
	  		else if (file.getName().indexOf("jre")>-1) { //$NON-NLS-1$
	  			file = new File(file, "bin/java.exe"); //$NON-NLS-1$
	  		}
	  		else file = null;
	  	}
	  	else if (OSPRuntime.isMac()) {
	  		// typical jdk public: /System/Library/Java/JavaVirtualMachines/X.X.X.jdk/Contents/Home/jre
	  		// jdk private: /System/Library/Java/JavaVirtualMachines/X.X.X.jdk/Contents/Home
	  		// in Tracker.app: /Applications/Tracker.app/Contents/PlugIns/Java.runtime/Contents/Home/jre
	  		// also in /Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home
	  		// also sometimes in /Library/Java...??
	  		// symlink at: /Library/Java/Home/bin/java??
	  		if (file.getName().endsWith("jdk")) { //$NON-NLS-1$
	  			File parent = file;
	  			file = new File(parent, "Contents/Home/jre/bin/java"); //$NON-NLS-1$
	  			if (!file.exists()) {
	  				file = new File(parent, "Contents/Home/bin/java"); //$NON-NLS-1$)
	  			}
	  		}
	  		else file = new File(file, "bin/java"); //$NON-NLS-1$
	  	}
	  	else if (OSPRuntime.isLinux()) {
	  		// typical: /usr/lib/jvm/java-X-openjdk/jre/bin/java 
	  		// bundled: /opt/tracker/jre/bin/java 
	  		// symlink at: /usr/lib/jvm/java-X.X.X-openjdk/jre/bin/java
	  		// sun versions: java-X-sun and java-X.X.X-sun
	  		if ("jre".equals(file.getName())) { //$NON-NLS-1$
	  			file = new File(file, "bin/java"); //$NON-NLS-1$
	  		}
	  		else {
		  		if (file.getParentFile()!=null
		  				&& file.getParentFile().getName().indexOf("jre")>-1) { //$NON-NLS-1$
		  			file = file.getParentFile();
		  		}
		  		if (file.getParentFile()!=null
		  				&& file.getParentFile().getName().indexOf("jdk")>-1) { //$NON-NLS-1$
		  			file = file.getParentFile();
		  		}
		  		if (file.getParentFile()!=null
		  				&& file.getParentFile().getName().indexOf("sun")>-1) { //$NON-NLS-1$
		  			file = file.getParentFile();
		  		}
		  		if (file.getName().indexOf("jdk")>-1  //$NON-NLS-1$
		  				|| file.getName().indexOf("sun")>-1) //$NON-NLS-1$
		  			file = new File(file, "jre/bin/java"); //$NON-NLS-1$
		  		else file = null;
	  		}
	  	}
  	}
    // resolve symlinks to their targets
  	if (file!=null) {
  		try {
				file = file.getCanonicalFile();
			} catch (IOException e) {
				file = null;
			}
  	}
  	if (file!=null && file.exists()) return file;
  	return null;
  }
  
  /**
   * Gets the bitness of the current Java VM. Note this identifies only
   * 32- and 64-bit VMs as of Jan 2011.
   * 
   * @return 64 if 64-bit VM, otherwise 32
   */
  public static int getVMBitness() {
  	String s = System.getProperty("java.vm.name"); //$NON-NLS-1$
  	s += "-"+System.getProperty("os.arch"); //$NON-NLS-1$ //$NON-NLS-2$
  	s += "-"+System.getProperty("sun.arch.data.model"); //$NON-NLS-1$ //$NON-NLS-2$
  	return s.indexOf("64")>-1? 64: 32; //$NON-NLS-1$
  }
  
  /**
   * Gets the java VM path for a given Java executable file.
   * 
   * @param javaFile the Java executable file
   * @return the VM path
   */
  public static String getJREPath(File javaFile) {
  	if (javaFile==null) return null;
  	String javaPath = XML.forwardSlash(javaFile.getAbsolutePath());
  	// all java command files should end with /bin/java or /bin/java.exe
		if (XML.stripExtension(javaPath).endsWith("/bin/java")) //$NON-NLS-1$
			return javaFile.getParentFile().getParent();
  	return ""; //$NON-NLS-1$
  }
  
  /**
   * Gets Locales for languages that have properties files in the core library.
   * @return Locale[]
   */
  public static Locale[] getDefaultLocales() {
    return defaultLocales;
  }

  /**
   * Gets Locales for languages that have properties files in the core library.
   * Locales are returned with English first, then in alphabetical order.
   * @return Locale[]
   */
  public static Locale[] getInstalledLocales() {
    ArrayList<Locale> list = new ArrayList<Locale>();
    if(org.opensourcephysics.js.JSUtil.isJS) return list.toArray(new Locale[0]);
    java.util.TreeMap<String, Locale> languages = new java.util.TreeMap<String, Locale>();
    list.add(Locale.ENGLISH); // english is first in list
    if(getLaunchJarPath()!=null) {
      // find available locales
      JarFile jar = getLaunchJar();
      if(jar!=null) {
        for(Enumeration<?> e = jar.entries(); e.hasMoreElements(); ) {
          JarEntry entry = (JarEntry) e.nextElement();
          String path = entry.toString();
          int n = path.indexOf(".properties");    //$NON-NLS-1$
          if(path.indexOf(".properties")>-1) {    //$NON-NLS-1$
            int m = path.indexOf("display_res_"); //$NON-NLS-1$
            if(m>-1) {
              String loc = path.substring(m+12, n);
              if(loc.equals("zh_TW")) {           //$NON-NLS-1$
                Locale next = Locale.TAIWAN;
                languages.put(getDisplayLanguage(next).toLowerCase(), next);
              } 
              else if(loc.equals("zh_CN")) {           //$NON-NLS-1$
                Locale next = Locale.CHINA;
                languages.put(getDisplayLanguage(next).toLowerCase(), next);
              } 
              else if(loc.equals("en_US")) {           //$NON-NLS-1$
              	continue;
              } 
              else {
              	Locale next;
              	if (!loc.contains("_")) next = new Locale(loc); //$NON-NLS-1$
              	else {
              		String lang = loc.substring(0, 2);
              		String country = loc.substring(3);
              		next = new Locale(lang, country, ""); //$NON-NLS-1$
              	}
                if (!next.equals(Locale.ENGLISH)) {
                  languages.put(getDisplayLanguage(next).toLowerCase(), next);
                }
              }
            }
          }
        }
        for(String s : languages.keySet()) {
          list.add(languages.get(s));
        }
      } else {
        defaultLocales = new Locale[] {Locale.ENGLISH};
        return defaultLocales;
      }
    }
    return list.toArray(new Locale[0]);
  }
  
  /**
   * Gets the display language for a given Locale. This returns the language name
   * in the locale's own language, but substitutes the equivalent of
   * SIMPLIFIED_CHINESE and TRADITIONAL_CHINESE for those locales.
   * @param locale the Locale
   * @return the display language
   */
  public static String getDisplayLanguage(Locale locale) {
	if(org.opensourcephysics.js.JSUtil.isJS) return "English";
  	if (locale.equals(Locale.CHINA))
  		return "\u7b80\u4f53\u4e2d\u6587"; //$NON-NLS-1$
  	if (locale.equals(Locale.TAIWAN))
  		return "\u7e41\u4f53\u4e2d\u6587"; //$NON-NLS-1$
  	return locale.getDisplayLanguage(locale);
  }
  
	/**
	 * Gets DecimalFormatSymbols that use the preferred decimal separator, if any.
	 * If no preference, the default separator for the current locale is used.
	 * 
	 * @return the DecimalFormatSymbols
	 */
  public static DecimalFormatSymbols getDecimalFormatSymbols() {
  	DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
  	char c = defaultDecimalSeparator;
    if (preferredDecimalSeparator!=null && preferredDecimalSeparator.length()>0) {
  		c = preferredDecimalSeparator.charAt(0);
  	}
    decimalFormatSymbols.setDecimalSeparator(c); 	
  	return decimalFormatSymbols;
  }
  
	/**
	 * Sets the default decimal separator.
	 * 
	 * @param c a decimal separator
	 */
  public static void setDefaultDecimalSeparator(char c) {
  	defaultDecimalSeparator = c;
  }
  
	/**
	 * Sets the preferred decimal separator. May be null.
	 * 
	 * @param separator a decimal separator
	 */
  public static void setPreferredDecimalSeparator(String separator) {
  	preferredDecimalSeparator = separator;
  }
  
	/**
	 * Gets the preferred decimal separator. May return null.
	 * 
	 * @return the separator, if any
	 */
  public static String getPreferredDecimalSeparator() {
  	return preferredDecimalSeparator;
  }
  
	/**
	 * Gets the default search paths, typically used for autoloading.
	 * Search paths are platform-specific "appdata", user home and code base, in that order.
	 * 
	 * @return the default search paths
	 */
  public static ArrayList<String> getDefaultSearchPaths() {
  	ArrayList<String> paths = new ArrayList<String>();
  	if (isWindows()) {
	  	String appdata = System.getenv("LOCALAPPDATA"); //$NON-NLS-1$
	    if (appdata!=null) {
	    	File dir = new File(appdata, "OSP"); //$NON-NLS-1$
  			if (!dir.exists()) dir.mkdir();
  			if (dir.exists()) {
  				paths.add(XML.forwardSlash(dir.getAbsolutePath()));
  			}
	    }  		
  	}
  	else if (userhomeDir!=null && isMac()) {
  		File dir = new File(userhomeDir, "Library/Application Support"); //$NON-NLS-1$
  		if (dir.exists()) {
  			dir = new File(dir, "OSP"); //$NON-NLS-1$
  			if (!dir.exists()) dir.mkdir();
  			if (dir.exists()) {
  				paths.add(XML.forwardSlash(dir.getAbsolutePath()));
  			}
  		}
  	}
  	else if (userhomeDir!=null && isLinux()) {
  		File dir = new File(userhomeDir, ".config"); //$NON-NLS-1$
  		if (dir.exists()) {
  			dir = new File(dir, "OSP"); //$NON-NLS-1$
  			if (!dir.exists()) dir.mkdir();
  			if (dir.exists()) {
  				paths.add(XML.forwardSlash(dir.getAbsolutePath()));
  			}
  		}
  	}
    if (userhomeDir!=null) {
    	paths.add(userhomeDir);
    }  		
    String codebase = OSPRuntime.getLaunchJarDirectory();
    if (codebase!=null) {
    	paths.add(XML.forwardSlash(codebase));
    }
  	return paths;
  }
  
	/**
	 * Gets a named preference object. The object must be cast to the correct type
	 * by the user.
	 * 
	 * @param name the name of the preference
	 * @return the object (may be null)
	 */
  public static Object getPreference(String name) {
  	XMLControl control = getPrefsControl();
  	return control.getObject(name);
  }

	/**
	 * Sets a named preference object. The object can be anything storable
	 * in an XMLControl--eg, String, Collection, OSP object, Boolean, Double, Integer
	 * 
	 * @param name the name of the preference
	 * @param pref the object (may be null)
	 */
  public static void setPreference(String name, Object pref) {
  	XMLControl control = getPrefsControl();
  	control.setValue(name, pref);
  }
  
	/**
	 * Saves the current preference XMLControl by writing to a file.
	 */
  public static void savePreferences() {
  	XMLControl control = getPrefsControl();
  	File file = new File(prefsPath, prefsFileName);
  	control.write(file.getAbsolutePath());
  }

	/**
	 * Gets the preferences XML file if it exists.
	 * 
	 * @return the file, or null if none exists
	 */
  public static File getPreferencesFile() {
  	getPrefsControl(); // ensures the prefs path is defined and writes file if needed	
  	File file = new File(prefsPath, prefsFileName);
  	if (file.exists()) return file;
  	return null;
  }

	/**
	 * Gets the preference XMLControl. This will load the control
	 * from the prefs file if it can be found, otherwise create a new one.
	 * 
	 * @return the XMLControl
	 */
  private static XMLControl getPrefsControl() {
  	if (prefsControl==null) {
  		// try to load prefs control from default search paths
  		ArrayList<String> dirs = getDefaultSearchPaths();
  		for (String dir: dirs) {
    		File file = new File(dir, prefsFileName);
      	if (!file.exists()) {
      		// try with leading dot
      		file = new File(dir, "."+prefsFileName); //$NON-NLS-1$
      		if (file.exists()) {
      			prefsFileName = "."+prefsFileName; //$NON-NLS-1$
      		}
      	}
    		if (file.exists()) {
    			XMLControl test = new XMLControlElement(file.getAbsolutePath());
    			if (!test.failedToRead()) {
    				prefsControl = test;
    				prefsPath = XML.forwardSlash(dir);
    				break;
    			}
    		}
  		}
  		if (prefsControl==null) {
  			prefsControl = new XMLControlElement();
  			// by default, save prefs in first default search directory
				prefsPath = XML.forwardSlash(dirs.get(0));
				if (prefsPath.equals(userhomeDir)) {
					// if saving in user home, add leading dot to hide
					prefsFileName = "."+prefsFileName; //$NON-NLS-1$
				}
		  	File file = new File(prefsPath, prefsFileName);
		  	prefsControl.write(file.getAbsolutePath());
  		}
  	}
  	return prefsControl;
  }

  /**
   * Gets the translator, if any.
   * @return translator, or null if none available
   */
  public static Translator getTranslator() {
	  
	 if(org.opensourcephysics.js.JSUtil.isJS) { // translator tool not supported in JavaScript.
		 return null;
	 }
    if((translator==null)&&loadTranslatorTool) {
      // creates the shared Translator
      try {
        Class<?> translatorClass = Class.forName("org.opensourcephysics.tools.TranslatorTool"); //$NON-NLS-1$
        Method m = translatorClass.getMethod("getTool", (Class[]) null);                        //$NON-NLS-1$
        translator = (Translator) m.invoke(null, (Object[]) null);
      } catch(Exception ex) {
        loadTranslatorTool = false;
        OSPLog.finest("Cannot instantiate translator tool class:\n"+ex.toString());             //$NON-NLS-1$
      }
    }
    return translator;
  }

  /**
   * BH AsyncFileChooser extends JFileChooser, so all of the methods of JFileChooser are still available. In particular,
   * the SAVE action needs no changes. But File reading requires asynchronous action in SwingJS. 
   * 
   */
  private static AsyncFileChooser chooser;

  /**
   * Gets a file chooser.
   * The choose is static and will therefore be the same for all OSPFrames.
   *
   * @return the chooser
   */
  public static AsyncFileChooser getChooser() {
    if(chooser!=null) {
    	FontSizer.setFonts(chooser, FontSizer.getLevel());
      return chooser;
    }
    try {
      chooser = (OSPRuntime.chooserDir==null) ? new AsyncFileChooser() : new AsyncFileChooser(new File(OSPRuntime.chooserDir));
    } catch(Exception e) {
      System.err.println("Exception in OSPFrame getChooser="+e); //$NON-NLS-1$
      return null;
    }
    javax.swing.filechooser.FileFilter defaultFilter = chooser.getFileFilter();
    javax.swing.filechooser.FileFilter xmlFilter = new javax.swing.filechooser.FileFilter() {
      // accept all directories and *.xml files.
      public boolean accept(File f) {
        if(f==null) {
          return false;
        }
        if(f.isDirectory()) {
          return true;
        }
        String extension = null;
        String name = f.getName();
        int i = name.lastIndexOf('.');
        if((i>0)&&(i<name.length()-1)) {
          extension = name.substring(i+1).toLowerCase();
        }
        if((extension!=null)&&(extension.equals("xml"))) { //$NON-NLS-1$
          return true;
        }
        return false;
      }
      // the description of this filter
      public String getDescription() {
        return DisplayRes.getString("OSPRuntime.FileFilter.Description.XML"); //$NON-NLS-1$
      }

    };
    javax.swing.filechooser.FileFilter txtFilter = new javax.swing.filechooser.FileFilter() {
      // accept all directories and *.txt files.
      public boolean accept(File f) {
        if(f==null) {
          return false;
        }
        if(f.isDirectory()) {
          return true;
        }
        String extension = null;
        String name = f.getName();
        int i = name.lastIndexOf('.');
        if((i>0)&&(i<name.length()-1)) {
          extension = name.substring(i+1).toLowerCase();
        }
        if((extension!=null)&&extension.equals("txt")) { //$NON-NLS-1$
          return true;
        }
        return false;
      }
      // the description of this filter
      public String getDescription() {
        return DisplayRes.getString("OSPRuntime.FileFilter.Description.TXT"); //$NON-NLS-1$
      }

    };
    chooser.addChoosableFileFilter(xmlFilter);
    chooser.addChoosableFileFilter(txtFilter);
    chooser.setFileFilter(defaultFilter);
  	FontSizer.setFonts(chooser, FontSizer.getLevel());
    return chooser;
  }

  /**
   * Uses a JFileChooser to ask for a name.
   * @param chooser JFileChooser
   * @return String The absolute pah of the filename. Null if cancelled
   */
  static public String chooseFilename(JFileChooser chooser) {
    return chooseFilename(chooser, null, true);
  }

  /**
   * Uses a JFileChooser to ask for a name.
   * @param chooser JFileChooser
   * @param parent Parent component for messages
   * @param toSave true if we will save to the chosen file, false if we will read from it
   * @return String The absolute pah of the filename. Null if cancelled
   */
  static public String chooseFilename(JFileChooser chooser, Component parent, boolean toSave) {
    String fileName = null;
    int result;
    if(toSave) {
      result = chooser.showSaveDialog(parent);
    } else {
      result = chooser.showOpenDialog(parent);
    }
    if(result==JFileChooser.APPROVE_OPTION) {
      OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
      File file = chooser.getSelectedFile();
      // check to see if file exists
      if(toSave) {                                                                                                                             // saving: check if the file will be overwritten
        if(file.exists()) {
          int selected = JOptionPane.showConfirmDialog(parent, DisplayRes.getString("DrawingFrame.ReplaceExisting_message")+" "+file.getName() //$NON-NLS-1$ //$NON-NLS-2$
            +DisplayRes.getString("DrawingFrame.QuestionMark"), DisplayRes.getString(                                //$NON-NLS-1$
            "DrawingFrame.ReplaceFile_option_title"),                                                                //$NON-NLS-1$
              JOptionPane.YES_NO_CANCEL_OPTION);
          if(selected!=JOptionPane.YES_OPTION) {
            return null;
          }
        }
      } else {                                                                                                       // Reading: check if thefile actually exists
        if(!file.exists()) {
          JOptionPane.showMessageDialog(parent, DisplayRes.getString("GUIUtils.FileDoesntExist")+" "+file.getName(), //$NON-NLS-1$ //$NON-NLS-2$
            DisplayRes.getString("GUIUtils.FileChooserError"), //$NON-NLS-1$
              JOptionPane.ERROR_MESSAGE);
          return null;
        }
      }
      fileName = file.getAbsolutePath();
      if((fileName==null)||fileName.trim().equals("")) {       //$NON-NLS-1$
        return null;
      }
    }
    return fileName;
  }

  /**
   * Creates a JFileChooser with given title, description and extensions
   * @param title the title
   * @param description a description string
   * @param extensions an array of allowed extensions
   * @return the JFileChooser
   */
  static public javax.swing.JFileChooser createChooser(String title, String description, String[] extensions) {
  	javax.swing.JFileChooser chooser = createChooser(description, extensions, null);
  	chooser.setDialogTitle(title);
  	return chooser;
  }

  /**
   * Creates a JFileChooser with given description and extensions
   * @param description String A description string
   * @param extensions String[] An array of allowed extensions
   * @return JFileChooser
   */
  static public javax.swing.JFileChooser createChooser(String description, String[] extensions) {
    return createChooser(description, extensions, null);
  }

  /**
   * Creates a JFileChooser with given description and extensions
   * @param description String A description string
   * @param extensions String[] An array of allowed extensions
   * @param homeDir File The target directory when the user clicks the home icon
   * @return JFileChooser
   */
  static public javax.swing.JFileChooser createChooser(String description, String[] extensions, final File homeDir) {
    javax.swing.JFileChooser chooser = new javax.swing.JFileChooser(new File(OSPRuntime.chooserDir));
    ExtensionFileFilter filter = new ExtensionFileFilter();
    for(int i = 0; i<extensions.length; i++) {
      filter.addExtension(extensions[i]);
    }
    filter.setDescription(description);
    if(homeDir!=null) {
      chooser.setFileSystemView(new javax.swing.filechooser.FileSystemView() {
        public File createNewFolder(File arg0) throws IOException {
          return javax.swing.filechooser.FileSystemView.getFileSystemView().createNewFolder(arg0);
        }
        public File getHomeDirectory() {
          return homeDir;
        }

      });
    }
    chooser.setFileFilter(filter);
  	FontSizer.setFonts(chooser, FontSizer.getLevel());
    return chooser;
  }

  /**
   * This file filter matches all files with a given set of
   * extensions.
   */
  static private class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
    private String description = ""; //$NON-NLS-1$
    private java.util.ArrayList<String> extensions = new java.util.ArrayList<String>();

    /**
     *  Adds an extension that this file filter recognizes.
     *  @param extension a file extension (such as ".txt" or "txt")
     */
    public void addExtension(String extension) {
      if(!extension.startsWith(".")) { //$NON-NLS-1$
        extension = "."+extension;     //$NON-NLS-1$
      }
      extensions.add(extension.toLowerCase());
    }

    public String toString() {
      return description;
    }
    
    /**
     *  Sets a description for the file set that this file filter
     *  recognizes.
     *  @param aDescription a description for the file set
     */
    public void setDescription(String aDescription) {
      description = aDescription;
    }

    /**
     *  Returns a description for the file set that this file
     *  filter recognizes.
     *  @return a description for the file set
     */
    public String getDescription() {
      return description;
    }

    public boolean accept(File f) {
    	if (f==null) return false;
      if(f.isDirectory()) {
        return true;
      }
      String name = f.getName().toLowerCase();
      // check if the file name ends with any of the extensions
      for(int i = 0; i<extensions.size(); i++) {
        if(name.endsWith(extensions.get(i))) {
          return true;
        }
      }
      return false;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
