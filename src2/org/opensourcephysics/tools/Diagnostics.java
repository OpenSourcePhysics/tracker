/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.Component;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JOptionPane;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This defines static methods for reporting the availability and status of
 * various software resources.
 *
 * @author Doug Brown
 */
public class Diagnostics {
	
	public static final String TRACKER_INSTALLER_URL ="http://www.compadre.org/osp/items/detail.cfm?ID=7365"; //$NON-NLS-1$
	
	final static String JAVA_3D_URL = "http://java3d.java.net/binary-builds.html"; //$NON-NLS-1$
  final static String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

  static String requester;
  static Component dialogOwner;

  public static void aboutJava() {
    String version = System.getProperty("java.version", "unknown version");   //$NON-NLS-1$ //$NON-NLS-2$
    String rtName = System.getProperty("java.runtime.name");                  //$NON-NLS-1$
    String rtVersion = System.getProperty("java.runtime.version");            //$NON-NLS-1$
    String vmName = System.getProperty("java.vm.name");                       //$NON-NLS-1$
    String vmVersion = System.getProperty("java.vm.version");                 //$NON-NLS-1$
    String path = System.getProperty("java.home");              						//$NON-NLS-1$
    String bitness = OSPRuntime.getVMBitness()==64? " (64-bit)": " (32-bit)"; //$NON-NLS-1$ //$NON-NLS-2$
    String aboutString = ToolsRes.getString("Diagnostics.Java.About.Version") //$NON-NLS-1$
                         +" "+version+bitness+NEWLINE                                 //$NON-NLS-1$
                         +rtName+" (build "+rtVersion+")"+NEWLINE             //$NON-NLS-1$ //$NON-NLS-2$
                         +vmName+" (build "+vmVersion+")"+NEWLINE             //$NON-NLS-1$ //$NON-NLS-2$
                         +"JRE path "+path+NEWLINE+NEWLINE   						//$NON-NLS-1$
                         +"Available Processors: "+Runtime.getRuntime().availableProcessors()+NEWLINE  //$NON-NLS-1$
                         +"Total Memory: "+Runtime.getRuntime().totalMemory()/1000000+" MB"+NEWLINE  //$NON-NLS-1$ //$NON-NLS-2$
                         +"Free Memory: "+Runtime.getRuntime().freeMemory()/1000000+" MB"+NEWLINE;  //$NON-NLS-1$ //$NON-NLS-2$
    JOptionPane.showMessageDialog(dialogOwner, 
    		aboutString, 
    		ToolsRes.getString("Diagnostics.Java.About.Title"), //$NON-NLS-1$
    		JOptionPane.INFORMATION_MESSAGE);
  }

	/**
	 * Sets the owner for JOptionPane dialogs.
	 * @param owner a JComponent (may be null)
	 */
  public static void setDialogOwner(Component owner) {
  	dialogOwner = owner;
  }
  
  public static File getJarFile(Class<?> classInJar) throws Exception {
    java.security.CodeSource codeSource = classInJar.getProtectionDomain().getCodeSource();
    File jarFile = null;
    if (codeSource.getLocation() != null) {
//      jarFile = new File(codeSource.getLocation().getPath());
      jarFile = new File(codeSource.getLocation().toURI());
    }
    else {
      String path = classInJar.getResource(classInJar.getSimpleName() + ".class").getPath(); //$NON-NLS-1$
      String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!")); //$NON-NLS-1$ //$NON-NLS-2$
      jarFilePath = java.net.URLDecoder.decode(jarFilePath, "UTF-8"); //$NON-NLS-1$
      jarFile = new File(jarFilePath);
    }
    return jarFile;
  }

  public static void aboutJava3D() {
	// Mac test added by W. Christian
	if(OSPRuntime.isMac() && !OSPRuntime.hasJava3D()){
	   return;  // Java version >= 1.7 on Mac does not have J3D
	}
  	// try to load j3d properties
  	Map<?, ?> props = null;
    try {
      Class<?> type = Class.forName("javax.media.j3d.VirtualUniverse");                                      //$NON-NLS-1$
      Method method = type.getMethod("getProperties", (Class[]) null);                                       //$NON-NLS-1$
      props = (Map<?, ?>) method.invoke(null, (Object[]) null);
    } catch(Exception ex) {
    } catch(Error err) {
    } 
    
    // get a list of java extension paths
    String extdirs = System.getProperty("java.ext.dirs"); //$NON-NLS-1$
    String separator = System.getProperty("path.separator"); //$NON-NLS-1$
    ArrayList<String> pathList = new ArrayList<String>();
    int n = extdirs.indexOf(separator);
    while (n>-1) {
    	pathList.add(extdirs.substring(0, n));
    	extdirs = extdirs.substring(n+1);
      n = extdirs.indexOf(separator);
    }
    if (!"".equals(extdirs)) {//$NON-NLS-1$
    	pathList.add(extdirs);
    }
    
    // look for j3d jar in extension paths
    File j3djar = null;
    String slash = System.getProperty("file.separator", "/"); //$NON-NLS-1$ //$NON-NLS-2$
    for (String path: pathList) {
	    j3djar = new File(path+slash+"j3dcore.jar"); //$NON-NLS-1$
	    if (!j3djar.exists()) 
	    	j3djar = null;
    }    
    
    if (props!=null) {
      String version = (String)props.get("j3d.version");  //$NON-NLS-1$
      String vendor = (String)props.get("j3d.vendor");  //$NON-NLS-1$
      String jarPath = j3djar==null? 
      		ToolsRes.getString("Diagnostics.About.Unknown"): //$NON-NLS-1$
      		j3djar.getPath();
      String aboutString = ToolsRes.getString("Diagnostics.Java3D.About.Version") //$NON-NLS-1$
          +" "+version+NEWLINE+vendor+NEWLINE //$NON-NLS-1$
          +ToolsRes.getString("Diagnostics.Java3D.About.JarPath")+" "+jarPath; //$NON-NLS-1$ //$NON-NLS-2$
      JOptionPane.showMessageDialog(dialogOwner, 
      		aboutString, 
      		ToolsRes.getString("Diagnostics.Java3D.About.Title"), //$NON-NLS-1$
      		JOptionPane.INFORMATION_MESSAGE);    	
    }
    else if (j3djar!=null) { // not working but jar exists
      JOptionPane.showMessageDialog(dialogOwner, 
      		ToolsRes.getString("Diagnostics.Java3D.Error.Message"), //$NON-NLS-1$
      		ToolsRes.getString("Diagnostics.Java3D.About.Title"), //$NON-NLS-1$
          JOptionPane.WARNING_MESSAGE);    	
    }
    else { // not working and jar not found
	    JOptionPane.showMessageDialog(dialogOwner, 
	    		ToolsRes.getString("Diagnostics.Java3D.NotFound.Message1") //$NON-NLS-1$
	    		+NEWLINE
	        +ToolsRes.getString("Diagnostics.Download.Message") //$NON-NLS-1$
	    		+NEWLINE+JAVA_3D_URL,
	        ToolsRes.getString("Diagnostics.Java3D.About.Title"),     //$NON-NLS-1$
	        JOptionPane.WARNING_MESSAGE);
    }  	
  }

  public static void aboutJOGL() {
  	// try to load javax.media.opengl.glu.GLU and get version string
  	String version = null;
    try {
      Class<?> type = Class.forName("javax.media.opengl.glu.GLU");                                         //$NON-NLS-1$
      Field field = type.getField("versionString");                                                        //$NON-NLS-1$
      version = (String) field.get(null);
    } catch(Exception ex) {
    } catch(Error err) {
    }
    
    // get a list of java extensions paths
    String extdirs = System.getProperty("java.ext.dirs"); //$NON-NLS-1$
    String separator = System.getProperty("path.separator"); //$NON-NLS-1$
    ArrayList<String> pathList = new ArrayList<String>();
    int n = extdirs.indexOf(separator);
    while (n>-1) {
    	pathList.add(extdirs.substring(0, n));
    	extdirs = extdirs.substring(n+1);
      n = extdirs.indexOf(separator);
    }
    if (!"".equals(extdirs)) {//$NON-NLS-1$
    	pathList.add(extdirs);
    }
    
    // look for jogl.jar in extension paths
    File jogljar = null;
    String slash = System.getProperty("file.separator", "/"); //$NON-NLS-1$ //$NON-NLS-2$
    for (String path: pathList) {
    	jogljar = new File(path+slash+"jogl.jar"); //$NON-NLS-1$
	    if (!jogljar.exists()) 
	    	jogljar = null;
    }    
    
    String jarPath = jogljar==null? 
      		ToolsRes.getString("Diagnostics.About.Unknown"): //$NON-NLS-1$
      		jogljar.getPath();
    if (version!=null) {
      String aboutString = ToolsRes.getString("Diagnostics.JOGL.About.Version")                            //$NON-NLS-1$
      		+" "+version+NEWLINE //$NON-NLS-1$
      		+ToolsRes.getString("Diagnostics.JOGL.About.JarPath") //$NON-NLS-1$
      		+" "+jarPath; //$NON-NLS-1$
      JOptionPane.showMessageDialog(dialogOwner, 
      		aboutString, 
      		ToolsRes.getString("Diagnostics.JOGL.About.Title"), //$NON-NLS-1$
      		JOptionPane.INFORMATION_MESSAGE);    	
    }
    else if (jogljar!=null) { // not working but jar exists
      String aboutString = ToolsRes.getString("Diagnostics.JOGL.Error.Message") //$NON-NLS-1$
  		+NEWLINE+ToolsRes.getString("Diagnostics.JOGL.About.JarPath") //$NON-NLS-1$
  		+" "+jarPath; //$NON-NLS-1$
     JOptionPane.showMessageDialog(dialogOwner, 
      		aboutString,
      		ToolsRes.getString("Diagnostics.JOGL.About.Title"), //$NON-NLS-1$
          JOptionPane.WARNING_MESSAGE);    	
    }
    else { // not working and jar not found
	    JOptionPane.showMessageDialog(dialogOwner, 
	    		ToolsRes.getString("Diagnostics.JOGL.NotFound.Message1") //$NON-NLS-1$
	    		+NEWLINE
	        +ToolsRes.getString("Diagnostics.JOGL.NotFound.Message2"), //$NON-NLS-1$
	    		ToolsRes.getString("Diagnostics.JOGL.About.Title"),     //$NON-NLS-1$
	        JOptionPane.WARNING_MESSAGE);
    }  	
  }

  public static void aboutLaunchJar() {
    if(OSPRuntime.getLaunchJarPath()!=null) {
      // create a JarFile
      JarFile jar = OSPRuntime.getLaunchJar();
      try {
        if(jar!=null) {
          String aboutString = ToolsRes.getString("Diagnostics.Jar.About.Message.JarFile")                        //$NON-NLS-1$
                               +" \""+XML.getName(OSPRuntime.getLaunchJarPath())+"\". ";                          //$NON-NLS-1$ //$NON-NLS-2$
          // iterate thru JarFile entries and look for dsa file
          for(Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
            String name = entry.getName().toLowerCase();
            if(name.endsWith(".dsa")&&name.startsWith("meta-inf")) {                                              //$NON-NLS-1$ //$NON-NLS-2$
              aboutString += ToolsRes.getString("Diagnostics.Jar.About.Message.Signed");                          //$NON-NLS-1$
              JOptionPane.showMessageDialog(dialogOwner, aboutString, ToolsRes.getString("Diagnostics.Jar.About.Title"), //$NON-NLS-1$
                JOptionPane.INFORMATION_MESSAGE);
              return;
            }
          }
          aboutString += ToolsRes.getString("Diagnostics.Jar.About.Message.NotSigned");                       //$NON-NLS-1$
          JOptionPane.showMessageDialog(dialogOwner, aboutString, ToolsRes.getString("Diagnostics.Jar.About.Title"), //$NON-NLS-1$
            JOptionPane.INFORMATION_MESSAGE);
        }
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    } else {
      JOptionPane.showMessageDialog(dialogOwner, ToolsRes.getString("Diagnostics.Jar.About.Message.NoJarFile"), //$NON-NLS-1$
        ToolsRes.getString("Diagnostics.Jar.About.Title"),                                               //$NON-NLS-1$
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  public static void aboutOS() {
    String osName = System.getProperty("os.name");                       //$NON-NLS-1$
    String version = System.getProperty("os.version");                   //$NON-NLS-1$
    String aboutString = ToolsRes.getString("Diagnostics.OS.About.Name") //$NON-NLS-1$
                         +" "+osName+NEWLINE;                            //$NON-NLS-1$
    aboutString += ToolsRes.getString("Diagnostics.OS.About.Version") //$NON-NLS-1$
                   +" "+version+NEWLINE;                              //$NON-NLS-1$
    Enumeration<?> e = System.getProperties().propertyNames();
    while(e.hasMoreElements()) {
      String next = (String) e.nextElement();
      if(next.startsWith("os.")) {               //$NON-NLS-1$
        String val = System.getProperty(next);
        if(!val.equals(osName)&&!val.equals(version)) {
          aboutString += next+":  "+val+NEWLINE; //$NON-NLS-1$
        }
      }
    }
    JOptionPane.showMessageDialog(dialogOwner, aboutString, ToolsRes.getString("Diagnostics.OS.About.Title"), //$NON-NLS-1$
      JOptionPane.INFORMATION_MESSAGE);
  }
  
	// test program
	public static void main(String[] args) {
		aboutJava();
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
