/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This provides a simple way to package files in a single JAR or ZIP file
 *
 * @author Francisco Esquembre (http://fem.um.es)
 * @author Doug Brown
 * @version 1.0
 */
public class JarTool implements Tool, Runnable {
  static public final int YES = 0;
  static public final int NO = 1;
  static public final int YES_TO_ALL = 2;
  static public final int NO_TO_ALL = 3;
  static public final int CANCEL = 4;
  
  // ---- Localization
  static private final String BUNDLE_NAME = "org.opensourcephysics.resources.tools.tools"; //$NON-NLS-1$
  static private ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME);

  static public void setLocale(Locale locale) {
    res = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  static public String getString(String key) {
    try {
      return res.getString(key);
    } catch(MissingResourceException e) {
      return '!'+key+'!';
    }
  }

  // --- End of localization

  /**
   * The singleton shared translator tool.
   */
  private static JarTool TOOL = new JarTool();
  private static JFileChooser chooser;
  private static int overwritePolicy = NO;
  private static Frame ownerFrame = null;
  private static Map<String, Map<String, ZipEntry>> jarContents = new HashMap<String, Map<String, ZipEntry>>(); // added by D Brown 2007-10-31

  /**
   * Gets the shared JarTool.
   * @return the shared JarTool
   */
  public static JarTool getTool() {
    if(TOOL==null) {
      TOOL = new JarTool();
    }
    return TOOL;
  }

  /**
   * Private constructor.
   */
  private JarTool() {
    String name = "JarTool"; //$NON-NLS-1$
    chooser = OSPRuntime.createChooser("JAR, ZIP", new String[] {"zip", "jar", "trz"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    Toolbox.addTool(name, this);
  }

  private ArrayList<String> instanceSources;
  private File instanceParent;
  private File instanceTarget;
  private Manifest instanceManifest;
  private OverwriteValue instancePolicy;
  private Frame instanceOwnerFrame;

  public void run() {
    compressList(instanceSources, instanceParent, instanceTarget, instanceManifest, instancePolicy, instanceOwnerFrame);
  }

  private JarTool(ArrayList<String> aSources, File aParent, File aTarget, Manifest aManifest, OverwriteValue aPolicy, Frame _anOwner) {
    this();
    instanceSources = aSources;
    instanceParent = aParent;
    instanceTarget = aTarget;
    instanceManifest = aManifest;
    instancePolicy = aPolicy;
    instanceOwnerFrame = _anOwner;
  }

  /**
   * Sends a job to this tool and specifies a tool to reply to.
   *
   * @param job the Job
   * @param replyTo the tool to notify when the job is complete (may be null)
   * @throws RemoteException
   */
  public void send(Job job, Tool replyTo) throws RemoteException {}

  // -----------------------------------
  // Main user methods
  // -----------------------------------

  /**
   * This method is kept for backwards compatibility. It is equivalent to
   * alwaysOverwrite().
   */
  static public void disableWarning() {
    alwaysOverwrite();
  }

  /**
   * Sets the overwrite feature to false.
   * By default, the packaging methods create() and append() will warn the user
   * before overwritting a duplicated file. Calling this method before running
   * those methods disables this warning and makes sure a new file will NEVER
   * overwrite an older one. The methods create() and append() set the warning
   * feature back.
   * @see alwayOverwrite
   */
  static public void neverOverwrite() {
    overwritePolicy = NO_TO_ALL;
  }

  /**
   * Sets the overwrite feature to true.
   * By default, the packaging methods create() and append() will warn the user
   * before overwritting a duplicated file. Calling this method before running
   * those methods disables this warning AND makes sure a new file will ALWAYS
   * overwrite an older one. The methods create() and append() set the warning
   * feature back.
   * @see neverOverwrite
   */
  static public void alwaysOverwrite() {
    overwritePolicy = YES_TO_ALL;
  }

  /**
   * Sets the owner frame for progress dialogs that may appear when creating
   * a JAR file.
   * @param owner Frame
   */
  static public void setOwnerFrame(Frame owner) {
    ownerFrame = owner;
  }

  /**
   * Creates a JAR file out of the list of contents provided.
   * Each entry in the sources list can be either a single file, a directory,
   * or a compressed (ZIP, JAR or TRZ) file.
   * Regular files and directories must exist under the given parent directory,
   * and are saved with the same relative path as provided.
   * As an exception, you can also add files in parent directories, such as,
   * "../../../WhateverDir/WhateverFile.xml", but ALL leading "../" will be removed
   * when saved.
   * Directories are added recursively.
   * Compressed files can, on the contrary, be placed anywhere and their contents
   * are stored with the same directory structure as in the original jar file, irrespective
   * of the original location of the source compressed file.
   * If a file appears more than once, succesive files could overwrite previous ones.
   * The user will be warned of this, except if either neverOverwrite() or alwaysOverwrite()
   * have been invoked immediately before calling this method.
   * (Calling this method resets the warning feature back.)
   *
   * @param sources ArrayList The list of content files to add.
   * Each item in the list is a String with the relative name of a
   * file or directory under the given parent directory, or of
   * a compressed file anywhere in the hard disk.
   * @param parent File The parent directory for all relative filenames
   * @param target File The target compressed file.
   * Its name must end in .zip, .jar or .trz.
   * The user will be prompted to confirm the target.
   * @param manifest Manifest A manifest for the newly created JAR file.
   * @return File The file that will eventually be created. Note that
   * the main work is done using a separate thread, hence the
   * method returns BEFORE the JAR file is actually created.
   * @see java.util.jar.Manifest
   */
  public File create(ArrayList<String> sources, File parent, File target, Manifest manifest) {
    OverwriteValue policy = new OverwriteValue(overwritePolicy);
    overwritePolicy = NO;
    if(sources.size()<=0) {
      return null;
    }
    try {
      boolean warnBeforeOverwritting = true;
      if(target!=null) {
        chooser.setCurrentDirectory(target.getParentFile());
        chooser.setSelectedFile(target);
      } else {
        chooser.setSelectedFile(new File("default.jar")); //$NON-NLS-1$
      }
      String targetName = OSPRuntime.chooseFilename(chooser);
      if(targetName==null) {
        return null;
      }
      if(!(targetName.toLowerCase().endsWith(".jar")||    //$NON-NLS-1$
      	targetName.toLowerCase().endsWith(".trz")||    //$NON-NLS-1$
        targetName.toLowerCase().endsWith(".zip"))) {     //$NON-NLS-1$   
        targetName = targetName+".jar";                   //$NON-NLS-1$
      } else {
        warnBeforeOverwritting = false;                   // the chooser already checked if the target file exists
      }
      target = new File(targetName);
      if(org.opensourcephysics.controls.XML.forwardSlash(target.getAbsolutePath()).equals(OSPRuntime.getLaunchJarPath())) {
        String[] message = new String[] {res.getString("JarTool.JarNotCreated"), res.getString("JarTool.FileIsForbidden")+" "+target}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
        return create(sources, parent, target, manifest);                                                                    // added by D Brown June 2007
      }
      if(warnBeforeOverwritting&&target.exists()) {
        int selected = JOptionPane.showConfirmDialog(null, DisplayRes.getString("DrawingFrame.ReplaceExisting_message")+" "+ //$NON-NLS-1$ //$NON-NLS-2$
          target.getName()+DisplayRes.getString("DrawingFrame.QuestionMark"), //$NON-NLS-1$
            DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"),    //$NON-NLS-1$
              JOptionPane.YES_NO_CANCEL_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return null;
        }
      }
      if(org.opensourcephysics.js.JSUtil.isJS) {
      	System.err.println("Warning:  JarTool not supported in JavaScript.");
      }else {
	      JarTool builder = new JarTool(sources, parent, target, manifest, policy, ownerFrame);
	      java.lang.Thread thread = new Thread(builder);
	      thread.setPriority(Thread.NORM_PRIORITY);
	      thread.start();
      }
      return target;
      //       return compressList(sources,parent,target,manifest,policy,ownerFrame);
    } catch(Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  /**
   * Appends to an existing compressed file the list of contents provided.
   * Works similarly to create(), but uses an existing compressed file
   * and respects its manifest (if a JAR file).
   * @param sources ArrayList The list of content files to add.
   * Each item in the list is a String with the relative name of a
   * file or directory under the current parent directory, or of
   * a compressed file anywhere in the hard disk.
   * @param parent File The parent directory for all relative filenames
   * @param target String The name of an existing compressed file, relative
   * to the parent directory.
   */
  public File append(ArrayList<String> sources, File parent, String target) {
    OverwriteValue policy = new OverwriteValue(overwritePolicy);
    overwritePolicy = NO;
    if(sources.size()<=0) {
      return null;
    }
    try {
      File targetFile = new File(parent, target);
      if(!targetFile.exists()) {
        String[] message = new String[] {res.getString("JarTool.JarNotCreated"), res.getString("JarTool.FileDoesntExist")+" "+target}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
        return null;
      }
      if(!sources.contains(target)) {
        sources.add(0, target);
      }
      return compressList(sources, parent, targetFile, getManifest(targetFile), policy, ownerFrame);
    } catch(Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  /**
   * Creates a Manifest for a JAR file with the given parameters
   * @param classpath String
   * @param mainclass String
   * @return Manifest
   */
  static public Manifest createManifest(String classpath, String mainclass) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy"); //$NON-NLS-1$
    Calendar cal = Calendar.getInstance();
    String date = sdf.format(cal.getTime());
    try {
      StringBuffer manifestStr = new StringBuffer();
      manifestStr.append("Manifest-Version: 1.0\n");                                        //$NON-NLS-1$
      manifestStr.append("Built-By: Open Source Physics JarTool\n");                        //$NON-NLS-1$
      manifestStr.append("Build-Date: "+date+"\n"); //$NON-NLS-1$ //$NON-NLS-2$
      if(classpath!=null) {
        classpath = classpath.replace(';', ' ');
        classpath = classpath.replace(',', ' ');
        classpath = classpath.replace(':', ' ');
        manifestStr.append("Class-Path: "+classpath+"\n");                                  //$NON-NLS-1$ //$NON-NLS-2$
      }
      if(mainclass!=null) {
        manifestStr.append("Main-Class: "+mainclass+"\n");                                  //$NON-NLS-1$ //$NON-NLS-2$
      }
      manifestStr.append("\n");                                                             //$NON-NLS-1$
      InputStream mis = new ByteArrayInputStream(manifestStr.toString().getBytes("UTF-8")); //$NON-NLS-1$
      return(new Manifest(mis));
    } catch(Exception exc) {
      exc.printStackTrace();
      return null;
    }
  }

  /**
   * Gets the Manifest of an existing JAR file
   * @param file File the jar file from which to obtain the manifest
   * @return Manifest the manifest found, null if failed.
   */
  static public Manifest getManifest(File file) {
    try {
      JarFile jar = new JarFile(file);
      Manifest manifest = jar.getManifest();
      jar.close();
      return manifest;
    } catch(Exception exc) {
      return null;
    } // Do not complain
  }

  /**
   * Extracts a given file from a compressed (ZIP, JAR or TRZ) file
   * @param source File The compressed file to extract the file from
   * @param filename String The path of the file to extract
   * @param destination String The full (or relative to whatever the current
   * user directory is) path where to save the extracted file
   * @return File The extracted file, null if failed
   */
  static public File extract(File source, String filename, String destination) {
    return extract(source, filename, new File(destination));
  }

  /**
   * Extracts a given file from a compressed (ZIP, JAR or TRZ) file
   * Extensive changes by D Brown 2007-10-31
   * @param source File The compressed file to extract the file from
   * @param filename String The path of the file to extract
   * @param target File The target file for the extracted file
   * @return File The extracted file, null if failed
   */
  static public File extract(File source, String filename, File target) {
    if((source.exists()==false)||(filename==null)||(filename.trim().length()<1)||(target==null)) {
      return null;
    }
    boolean isDirectory = (filename.lastIndexOf("/")==filename.length()-1); //$NON-NLS-1$
    try {
      // get contents Map of filename to ZipEntry for source jar
      Map<String, ZipEntry> contents = jarContents.get(source.getPath());
      if(contents==null) {
        // create new Map and fill it
        contents = new HashMap<String, ZipEntry>();
        jarContents.put(source.getPath(), contents);
        ZipInputStream input = new ZipInputStream(new FileInputStream(source));
        ZipEntry zipEntry = null;
        while((zipEntry = input.getNextEntry())!=null) {
          if(zipEntry.isDirectory()) {
            continue;                               // don't include directories
          }
          contents.put(zipEntry.getName(), zipEntry);
        }
        input.close();
      }
      if(isDirectory) {
        // target is a directory: extract all contained files
        Iterator<String> it = contents.keySet().iterator();
        while(it.hasNext()) {
          String next = it.next();
          if(next.startsWith(filename)) {
            // next is in the directory, so extract it
            ZipEntry zipEntry = contents.get(next);
            // construct new target for the file
            int n = filename.length();
            File newTarget = new File(target, zipEntry.getName().substring(n));
            extract(source, next, newTarget);
          }
        }
        return target;
      }
      // target is a file
      ZipEntry entry = contents.get(filename);
      ZipFile input = new ZipFile(source);
      InputStream in = input.getInputStream(entry); // A stream to read the entry
      File parent = target.getParentFile();
      if(parent!=null) {
        parent.mkdirs();
      }
      int bytesRead;
      byte[] buffer = new byte[1024];
      FileOutputStream output = new FileOutputStream(target);
      while((bytesRead = in.read(buffer))!=-1) {
        output.write(buffer, 0, bytesRead);
      }
      output.close();
      input.close();
      return target;
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Extracts a file using the given class loader
   * @param _classLoader ClassLoader The class loader to extract the files from
   * @param filename String The path of the file to extract
   * @param target File The target file for the extracted file
   * @return File The extracted file, null if failed
   */
  static public File extract(ClassLoader classLoader, String filename, File target) {
    if((filename==null)||(filename.trim().length()<=0)||(target==null)) {
      return null;
    }
    try {
      URL url = classLoader.getResource(filename);
      if(url==null) {
        return null;
      }
      target.getParentFile().mkdirs();
      int bytesRead;
      byte[] buffer = new byte[1024];
      FileOutputStream output = new FileOutputStream(target);
      BufferedInputStream input = new BufferedInputStream(url.openStream());
      while((bytesRead = input.read(buffer))!=-1) {
        output.write(buffer, 0, bytesRead);
      }
      output.close();
      input.close();
      return target;
    } catch(Exception exc) {
      exc.printStackTrace();
      return null;
    }
  }

  /**
   * Extracts a file using the ResourceLoader utility
   * @param filename String The path of the file to extract
   * @param target File The target file for the extracted file
   * @return File The extracted file, null if failed
   */
  static public File extract(String filename, File target) {
	//System.out.println("Extract filename="+filename); //$NON-NLS-1$
    if((filename==null)||(filename.trim().length()<=0)||(target==null)) {
      return null;
    }
    try {
      InputStream inputStream=null;
      if(OSPRuntime.applet!=null) {// added by Wolfgang Christian
        //URL url=OSPRuntime.applet.getClass().getResource(filename);
        //inputStream=url.openStream();
        inputStream=OSPRuntime.applet.getClass().getResourceAsStream(filename);
      }
      if(inputStream==null) {  // use resource loader when not an applet
        if(filename.indexOf("http:")>-1) {  //$NON-NLS-1$
          int n = filename.toLowerCase().indexOf(".zip!/"); //$NON-NLS-1$
          if (n==-1) n = filename.toLowerCase().indexOf(".jar!/");     //$NON-NLS-1$
          if (n==-1) n = filename.toLowerCase().indexOf(".trz!/");     //$NON-NLS-1$
          if (n>-1) {
          	File extracted = ResourceLoader.extractFileFromZIP(filename, target, false);
          	if (extracted!=null) return extracted;
          }        	
        }
        else {
          inputStream = ResourceLoader.getResource(filename, false).openInputStream();
        }
      }
      if(inputStream==null) {
        return null;
      }
      BufferedInputStream input = new BufferedInputStream(inputStream);
      target.getParentFile().mkdirs();
      int bytesRead;
      byte[] buffer = new byte[1024];
      FileOutputStream output = new FileOutputStream(target);
      while((bytesRead = input.read(buffer))!=-1) {
        output.write(buffer, 0, bytesRead);
      }
      output.close();
      input.close();
      return target;
    } catch(Exception exc) {
      System.err.println("JarTool extract resource error.  Filename="+filename); //$NON-NLS-1$
      exc.printStackTrace();
      return null;
    }
  }

  /**
   * Extract a list of files (given by their relative names) to the given target directory.
   * If files exist, the user will be warned.
   * @param source Object Either a compressed java.io.File with the given resources,
   * a ClassLoader object which will be used to extract the files, or null, in which case,
   * the ResourceLoader will be used.
   * @param files AbstractList The list of String with the relative names of the files to extract
   * @param targetDirectory File The target directory where to extract the files
   * @return boolean
   */
  static public boolean extract(Object source, java.util.List<?> files, File targetDirectory) {
    if(files.size()<=0) {
      return true;
    }
    if(!((source==null)||(source instanceof File)||(source instanceof ClassLoader))) {
      String[] message = new String[] {res.getString("JarTool.FileNotExtracted"), res.getString("JarTool.SourceRequirement")+" null, java.io.File, java.lang.ClassLoader."}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return false;
    }
    if(!(targetDirectory.exists()&&targetDirectory.isDirectory())) {
      String[] message = new String[] {res.getString("JarTool.FileNotExtracted"), res.getString("JarTool.FileDoesntExist")+" "+targetDirectory.getName()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return false;
    }
    int policy = NO;
    for(Iterator<?> it = files.iterator(); it.hasNext(); ) {                                                                                                // Copy or uncompress all sources to the temporary directory
      String filename = it.next().toString();
      File targetFile = new File(targetDirectory, filename);
      if(targetFile.exists()) {
        switch(policy) {
           case NO_TO_ALL :
             continue;
           case YES_TO_ALL :
             break;                                                                                                                                         // will overwrite
           default :
             switch(policy = confirmOverwrite(filename)) {
                case NO_TO_ALL :
                case NO :
                  continue;
//                case CANCEL : return false;
                default :                                                                                                                                   // Do nothing, i.e., will overwrite the file
             }
        }
      }
      File result = null;
      if(source==null) {
        result = extract(filename, targetFile);                                                                                                             // Use the ResourceLoader
      } else if(source instanceof File) {
        result = extract((File) source, filename, targetFile);
      } else if(source instanceof ClassLoader) {
        result = extract((ClassLoader) source, filename, targetFile);
      }
      if(result==null) {
        String[] message = new String[] {res.getString("JarTool.FileNotExtracted"), filename+" "+res.getString("JarTool.FileNotExtractedFrom")+" "+source}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
        return false;
      }
    }
    return true;
  }

  // -----------------------------------
  // Different public File-based utilities
  // -----------------------------------

  /**
   * Copies a file. If the target file exists, it will be overwritten.
   * @param source File The file to copy
   * @param target File destination file
   * @return boolean true if successful
   */
  static public boolean copy(File source, File target) {
    try {
      if(!source.exists()) {
        return false;
      }
      target.getParentFile().mkdirs();
      InputStream input = new FileInputStream(source);
      OutputStream output = new FileOutputStream(target);
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while((len = input.read(buf))>0) {
        output.write(buf, 0, len);
      }
      input.close();
      output.close();
      return true;
    } catch(Exception exc) {
      exc.printStackTrace();
      return false;
    }
  }

  /**
   * Compresses a directory into a single JAR or ZIP file.
   * If the target file exists it will be overwritten.
   * @param source File The directory to compress
   * @param target File The output file
   * @param manifest Manifest The manifest (in case of a JAR file)
   * @return boolean
   */
  static public boolean compress(File source, File target, Manifest manifest) {
    try {
      if(!(source.exists()&source.isDirectory())) {
        return false;
      }
      if(target.exists()) {
        target.delete();                                               // Remove the previous JAR file
      }
      ZipOutputStream output = null;
      boolean isJar = target.getName().toLowerCase().endsWith(".jar"); //$NON-NLS-1$
      if(isJar) {
        // Remove any existing META-INF directory
        File manifestDir = new File(source, "META-INF");               //$NON-NLS-1$
        remove(manifestDir);
        if(manifest!=null) {
          output = new JarOutputStream(new FileOutputStream(target), manifest);
        } else {
          output = new JarOutputStream(new FileOutputStream(target));
        }
      } else {
        output = new ZipOutputStream(new FileOutputStream(target));
      }
      // Get the list of files
      java.util.Collection<File> list = getContents(source);
      String baseDir = source.getAbsolutePath().replace('\\', '/');
      if(!baseDir.endsWith("/")) {                                     //$NON-NLS-1$   
        baseDir = baseDir+"/";                                         //$NON-NLS-1$ 
      }
      int baseDirLength = baseDir.length();
      // Create the compressed file
      byte[] buffer = new byte[1024];                                  // Allocate a buffer for reading entry data.
      int bytesRead;
      for(File file : list) {
        FileInputStream f_in = new FileInputStream(file);
        // Read the entry and make it relative
        String filename = file.getAbsolutePath().replace('\\', '/');
        if(filename.startsWith(baseDir)) {
          filename = filename.substring(baseDirLength);
        }
        // Write the entry to the new compressed file.
        if(isJar) {
          output.putNextEntry(new JarEntry(filename));
        } else {
          output.putNextEntry(new ZipEntry(filename));
        }
        while((bytesRead = f_in.read(buffer))!=-1) {
          output.write(buffer, 0, bytesRead);
        }
        f_in.close();
        output.closeEntry();
      }
      output.close();
    } catch(Exception exc) {
      exc.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Compresses a list of files and/or directories into a single JAR or ZIP file.
   * All files/dirs must be in the same directory.
   * If the target file exists it will be overwritten.
   * @param sources ArrayList The list of files or directories to compress
   * @param target File The output file
   * @param manifest Manifest The manifest (in case of a JAR file)
   * @return boolean
   */
  static public boolean compress(ArrayList<File> sources, File target, Manifest manifest) {
    try {
      if((sources==null)||(sources.size()==0)) {
        return false;
      }
      if(target.exists()) {
        target.delete();                                                      // Remove the previous JAR file
      }
      ZipOutputStream output = null;
      boolean isJar = target.getName().toLowerCase().endsWith(".jar");        //$NON-NLS-1$
      if(isJar) {
        if(manifest!=null) {
          output = new JarOutputStream(new FileOutputStream(target), manifest);
        } else {
          output = new JarOutputStream(new FileOutputStream(target));
        }
      } else {
        output = new ZipOutputStream(new FileOutputStream(target));
      }
      // Recall all files must have the same parent dir
      String baseDir = sources.get(0).getParentFile().getAbsolutePath().replace('\\', '/');
      if(!baseDir.endsWith("/")) {                                            //$NON-NLS-1$   
        baseDir = baseDir+"/";                                                //$NON-NLS-1$ 
      }
      int baseDirLength = baseDir.length();
      // Get the list of files
      ArrayList<File> list = new ArrayList<File>();
      for(Iterator<File> it = sources.iterator(); it.hasNext(); ) {
        File fileOrDir = it.next();
        if(isJar&&(manifest!=null)&&fileOrDir.getName().equals("META-INF")) { //$NON-NLS-1$   
          continue;                                                           // Ignore any existing META-INF directory 
        }
        if(fileOrDir.isDirectory()) {
          list.addAll(getContents(fileOrDir));
        } else {
          list.add(fileOrDir);
        }
      }
      // Create the compressed file
      byte[] buffer = new byte[1024];                                         // Allocate a buffer for reading entry data.
      int bytesRead;
      for(int i = 0, n = list.size(); i<n; i++) {
        File file = list.get(i);
        FileInputStream f_in = new FileInputStream(file);
        // Read the entry and make it relative
        String filename = file.getAbsolutePath().replace('\\', '/');
        if(filename.startsWith(baseDir)) {
          filename = filename.substring(baseDirLength);
        }
        // Write the entry to the new compressed file.
        if(isJar) {
          output.putNextEntry(new JarEntry(filename));
        } else {
          output.putNextEntry(new ZipEntry(filename));
        }
        while((bytesRead = f_in.read(buffer))!=-1) {
          output.write(buffer, 0, bytesRead);
        }
        f_in.close();
        output.closeEntry();
      }
      output.close();
    } catch(Exception exc) {
      exc.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Completely removes a directory (without warning!)
   * @param directory File The directory to delete
   */
  static public boolean remove(File directory) {
    if(directory.exists()&&directory.isDirectory()) {
      return recursiveClearDirectory(directory, FileSystemView.getFileSystemView());
    }
    return false;
  }

  /**
   * Returns all the files under a given directory
   * @param directory File
   * @return ArrayList
   */
  static public java.util.Collection<File> getContents(File directory) {
    if(directory.exists()&&directory.isDirectory()) {
      return recursiveGetDirectory(directory, FileSystemView.getFileSystemView());
    }
    return new HashSet<File>();
  }

  /**
   * Uncompresses a ZIP or JAR file into a given directory.
   * Duplicated files will be overwritten.
   * @param source File The compressed file to uncompress
   * @param targetDirectory File The target directory
   * @return boolean
   * @see unzipNoOverwrite
   * @see unzipWithAWarning
   */
  static public boolean unzip(File source, File targetDirectory) {
    return unzipWithWarning(source, targetDirectory, new OverwriteValue(YES_TO_ALL));
  }

  /**
   * Uncompresses a ZIP or JAR file into a given directory.
   * Duplicated files will NOT be overwriten.
   * @param source File The compressed file to uncompress
   * @param targetDirectory File The target directory
   * @return boolean
   * @see unzip
   * @see unzipWithAWarning
   */
  static public boolean unzipNoOverwrite(File source, File targetDirectory) {
    return unzipWithWarning(source, targetDirectory, new OverwriteValue(NO_TO_ALL));
  }

  /**
   * Uncompresses a ZIP or JAR file into a given directory.
   * The system will issue a warning before duplicating existing files.
   * @param source File The compressed file to uncompress
   * @param targetDirectory File The target directory
   * @return boolean
   * @see unzip
   * @see unzipNoOverwrite
   */
  static public boolean unzipWithAWarning(File source, File targetDirectory) {
    return unzipWithWarning(source, targetDirectory, new OverwriteValue(NO));
  }

  // -----------------------------------
  //        Private methods
  // -----------------------------------

  static public int confirmOverwrite(String filename) {
    return confirmOverwrite(filename, false);
  }

  /**
   * Whether to overwrite an existing file.
   * @param file File
   * @return boolean
   */
  static public int confirmOverwrite(String filename, boolean canCancel) {
    final JDialog dialog = new JDialog();
    final OverwriteValue returnValue = new OverwriteValue(NO);
    java.awt.event.MouseAdapter mouseListener = new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if(aCmd.equals("yes")) {             //$NON-NLS-1$   
          returnValue.value = YES;
        } else if(aCmd.equals("no")) {       //$NON-NLS-1$   
          returnValue.value = NO;
        } else if(aCmd.equals("yesToAll")) { //$NON-NLS-1$   
          returnValue.value = YES_TO_ALL;
        } else if(aCmd.equals("noToAll")) {  //$NON-NLS-1$   
          returnValue.value = NO_TO_ALL;
        } else if(aCmd.equals("cancel")) {  //$NON-NLS-1$   
          returnValue.value = CANCEL;
        }
        dialog.setVisible(false);
      }

    };
    JButton yesButton = new JButton(res.getString("JarTool.Yes")); //$NON-NLS-1$
    yesButton.setActionCommand("yes"); //$NON-NLS-1$
    yesButton.addMouseListener(mouseListener);
    JButton noButton = new JButton(res.getString("JarTool.No")); //$NON-NLS-1$
    noButton.setActionCommand("no"); //$NON-NLS-1$
    noButton.addMouseListener(mouseListener);
    JButton yesToAllButton = new JButton(res.getString("JarTool.YesToAll")); //$NON-NLS-1$
    yesToAllButton.setActionCommand("yesToAll"); //$NON-NLS-1$
    yesToAllButton.addMouseListener(mouseListener);
    JButton noToAllButton = new JButton(res.getString("JarTool.NoToAll")); //$NON-NLS-1$
    noToAllButton.setActionCommand("noToAll"); //$NON-NLS-1$
    noToAllButton.addMouseListener(mouseListener);
    JButton cancelButton = new JButton(res.getString("JarTreeDialog.Button.Cancel")); //$NON-NLS-1$
    cancelButton.setActionCommand("cancel"); //$NON-NLS-1$
    cancelButton.addMouseListener(mouseListener);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(yesButton);
    buttonPanel.add(yesToAllButton);
    buttonPanel.add(noButton);
    buttonPanel.add(noToAllButton);
    if (canCancel) buttonPanel.add(cancelButton);
    JLabel label = new JLabel(DisplayRes.getString("DrawingFrame.ReplaceExisting_message")+" "+ //$NON-NLS-1$ //$NON-NLS-2$
      filename+DisplayRes.getString("DrawingFrame.QuestionMark")); //$NON-NLS-1$
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
    dialog.setTitle(DisplayRes.getString("DrawingFrame.ReplaceFile_option_title")); //$NON-NLS-1$
    dialog.getContentPane().setLayout(new java.awt.BorderLayout(5, 0));
    dialog.getContentPane().add(label, java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent event) {
        returnValue.value = NO;
      }

    });
    dialog.validate();
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setModal(true);
    dialog.setVisible(true);
    return returnValue.value;
  }

  /**
   * Uncompresses a ZIP or JAR file into a given directory.
   * policy.value indicates what to do on duplicated files.
   * @param source File The compressed file to uncompress
   * @param targetDirectory File The target directory
   * @param policy OverwriteValue
   * @return boolean
   */
  static private boolean unzipWithWarning(File source, File targetDirectory, OverwriteValue policy) {
    try {
      if(!source.exists()) {
        return false;
      }
      ZipInputStream input = new ZipInputStream(new FileInputStream(source));
      ZipEntry zipEntry = null;
      byte[] buffer = new byte[1024];
      while((zipEntry = input.getNextEntry())!=null) {
        if(zipEntry.isDirectory()) {
          continue;
        }
        File newFile = new File(targetDirectory, zipEntry.getName());
        if(newFile.exists()) {
          switch(policy.value) {
             case NO_TO_ALL :
               continue;
             case YES_TO_ALL :
               break;       // will overwrite
             default :
               switch(policy.value = confirmOverwrite(zipEntry.getName())) {
                  case NO_TO_ALL :
                  case NO :
                    continue;
//                  case CANCEL : return false;
                  default : // Do nothing, i.e., will overwrite the file
               }
          }
        }
        newFile.getParentFile().mkdirs();
        int bytesRead;
        FileOutputStream output = new FileOutputStream(newFile);
        while((bytesRead = input.read(buffer))!=-1) {
          output.write(buffer, 0, bytesRead);
        }
        output.close();
        input.closeEntry();
      }
      input.close();
    } catch(Exception exc) {
      exc.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Uncompresses a ZIP or JAR file into a given directory.
   * policy.value indicates what to do on duplicated files.
   * @param zipStream The InputStream to read from
   * @param targetDirectory File The target directory
   * @param label An optional JLabel to display messages
   * @param prefix A prefix to add to the extracted file in order to create the message
   * @return java.util.List<File> the set of files extracted, null if cancelled
   */
  static public java.util.List<File> unzipWithWarning(InputStream zipStream, File targetDirectory, javax.swing.JLabel label, String prefix) {
    try {
      OverwriteValue policy = new OverwriteValue(NO);
      BufferedInputStream bufIn = new BufferedInputStream(zipStream);
      ZipInputStream input = new ZipInputStream(bufIn);
      ZipEntry zipEntry = null;
      ArrayList<File> fileSet = new ArrayList<File>();

      byte[] buffer = new byte[1024];
      while((zipEntry = input.getNextEntry())!=null) {
        if (zipEntry.isDirectory()) continue;
        if (label!=null) label.setText(prefix+zipEntry.getName());
        File newFile = new File(targetDirectory, zipEntry.getName());
        if(newFile.exists()) {
          switch(policy.value) {
             case NO_TO_ALL :
               continue;
             case YES_TO_ALL :
               break;       // will overwrite
             default :
               switch(policy.value = confirmOverwrite(zipEntry.getName(),true)) {
                  case NO_TO_ALL :
                  case NO :
                    continue;
                  case CANCEL : return null;
                  default : // Do nothing, i.e., will overwrite the file
               }
          }
        }
        newFile.getParentFile().mkdirs();
        int bytesRead;
        FileOutputStream output = new FileOutputStream(newFile);
        while((bytesRead = input.read(buffer))!=-1) {
          output.write(buffer, 0, bytesRead);
        }
        output.close();
        input.closeEntry();
        fileSet.add(newFile);
      }
      input.close();
      return fileSet;
    } catch(Exception exc) {
      exc.printStackTrace();
      return null;
    }
  }

  /**
   * Compresses a list of sources to a final compressed file.
   * policy.value indicates what to do on duplicated files.
   * @param sources ArrayList A list of relative filenames
   * @param parent File The parent directory to which relative filenames are given
   * @param target File The target compressed file
   * @param policy OverwriteValue What to do on duplicated entries
   * @param manifest Manifest The (optional) manifest for JAR files
   * @return File
   */
  static private File compressList(ArrayList<String> sources, File parent, File target, Manifest manifest, OverwriteValue policy, Frame owner) {
    // Create a temporary directory
    File temporaryDirectory = null;
    try {
      temporaryDirectory = File.createTempFile("JarTool", ".tmp", target.getParentFile()); // Get a unique name for our temporary directory //$NON-NLS-1$ //$NON-NLS-2$
      temporaryDirectory.delete(); // remove the created file
    } catch(Exception exc) {
      temporaryDirectory = null;
    }
    if((temporaryDirectory==null)||!temporaryDirectory.mkdirs()) {                                                       // and re-create it as a directory
      String[] message = new String[] {res.getString("JarTool.JarNotCreated"), res.getString("JarTool.CantCreateTemp")}; //$NON-NLS-1$ //$NON-NLS-2$
      JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return null;
    }
    StringBuffer errorMessage = new StringBuffer();
    int steps = sources.size(), interval = 1, counter = 0;
    if(steps>10) {
      interval = Math.round(steps/10.0f);
      steps = 10;
    }
    ProgressDialog pD = new ProgressDialog(owner, steps+2, "JarTool", new Dimension(350, 150)); //$NON-NLS-1$
    String pdMessage = res.getString("JarTool.ProcessingFile");                                 //$NON-NLS-1$
    for(Iterator<String> it = sources.iterator(); it.hasNext(); ) { // Copy or uncompress all sources to the temporary directory
      if(counter%interval==0) {
        pD.reportProgress(pdMessage);
      }
      counter++;
      String filename = it.next().toString();
      if(filename!=null) {
        errorMessage.append(processFile(filename, new File(parent, filename), temporaryDirectory, policy));
      }
    }
    boolean success = false;
    String error = errorMessage.toString().trim();
    if(error.length()>0) {
      String[] message = new String[] {res.getString("JarTool.JarNotCreated"), error};                                    //$NON-NLS-1$
      JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
    }
    // Now pack everything into a single compressed file
    else {
      pD.reportProgress(res.getString("JarTool.CompressingFile"));                                                                                    //$NON-NLS-1$
      if(compress(temporaryDirectory, target, manifest)) {
        success = true;
      } else {
        String[] message = new String[] {res.getString("JarTool.JarNotCreated"), res.getString("JarTool.CantCompress")+" "+target.getAbsolutePath()}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        JOptionPane.showMessageDialog((JFrame) null, message, res.getString("JarTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      }
    }
    pD.reportProgress(res.getString("JarTool.CleaningTempFile")); //$NON-NLS-1$
    remove(temporaryDirectory);
    pD.dispose();
    if(success) {
      return target;
    }
    return null;
  }

  /**
   * Processes a regular file, a compressed file, or (recursively) a directory
   *
   * @param filename String the relative filename to process
   * @param parent File The actual file to process
   * @param targetDirectory File The target directory
   * @param policy OverwriteValue What to do on duplicated entries
   * @return StringBuffer A list of errors
   */
  static private StringBuffer processFile(String filename, File file, File targetDirectory, OverwriteValue policy) {
    // changed by D Brown June 2007 to handle zip/jar entries
    if(!file.exists()&&(filename.indexOf("!")==-1)) {                                                     //$NON-NLS-1$
      return new StringBuffer(res.getString("JarTool.FileDoesntExist")+" "+file.getAbsolutePath()+".\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } // end change by D Brown
    if(file.isDirectory()) {
      // recursively process each file in the directory
      StringBuffer errorMessage = new StringBuffer();
      FileSystemView fsView = FileSystemView.getFileSystemView();
      File filesInDir[] = fsView.getFiles(file, false);
      for(int i = 0, n = filesInDir.length; i<n; i++) {
        errorMessage.append(processFile(filename+"/"+filesInDir[i].getName(), filesInDir[i], targetDirectory, policy)); //$NON-NLS-1$
      }
      return errorMessage;
    }
    // Check for a ZIP or JAR file
    String filenameLowerCase = file.getName().toLowerCase();
    if(filenameLowerCase.endsWith(".jar")||filenameLowerCase.endsWith(".zip")||filenameLowerCase.endsWith(".trz")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if(unzipWithWarning(file, targetDirectory, policy)) {
        return new StringBuffer();
      }
      return new StringBuffer(res.getString("JarTool.CantUncompress")+" "+file.getAbsolutePath()+".\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    // code added by D Brown June 2007 to handle zip/jar entries
    int n = filename.indexOf("!"); //$NON-NLS-1$
    if(n>-1) {
      String entry = filename.substring(n+2);
      // next 2 lines added/modified by W Christian/D Brown for Linux 2007-11-05
      String filepath = file.getAbsolutePath();
      File zipFile = new File(filepath.substring(0, filepath.indexOf("!")));                                           //$NON-NLS-1$
      File target = new File(targetDirectory, entry);
      if(extract(zipFile, entry, target)!=null) {
        return new StringBuffer();
      }
      return new StringBuffer(res.getString("JarTool.CantCopy")+" "+filename+" --> "+targetDirectory.getName()+".\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    } // end code added by D Brown
    // File is a normal file. Just copy it
    // If the file has leading "../" remove them from the name
    while(filename.startsWith("../")) { //$NON-NLS-1$   
      filename = filename.substring(3);
    }
    File target = new File(targetDirectory, filename);
    if(target.exists()) {
      switch(policy.value) {
         case NO_TO_ALL :
           return new StringBuffer();
         case YES_TO_ALL :
           break;       // will overwrite
         default :
           switch(policy.value = confirmOverwrite(filename)) {
              case NO_TO_ALL :
              case NO :
                return new StringBuffer();
//              case CANCEL : return new StringBuffer(); 
              default : // Do nothing, i.e., will overwrite the file
           }
      }
    }
    if(copy(file, target)) {
      return new StringBuffer();
    }
    return new StringBuffer(res.getString("JarTool.CantCopy")+" "+filename+" --> "+targetDirectory.getName()+".\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  /**
   * Used by removeDirectory (File)
   * @param directory File
   * @param fsView FileSystemView
   */
  static private boolean recursiveClearDirectory(File directory, FileSystemView fsView) {
    File files[] = fsView.getFiles(directory, false);
    for(int i = 0; i<files.length; i++) {
      if(files[i].isDirectory()) {
        if(!recursiveClearDirectory(files[i], fsView)) {
          return false;
        }
      } else if(!files[i].delete()) {
        return false;
      }
    }
    return directory.delete();
  }

  /**
   * Used by getFilesUnderDirectory (File)
   * @param directory File
   * @param fsView FileSystemView
   */
  static private java.util.Collection<File> recursiveGetDirectory(File directory, FileSystemView fsView) {
    File files[] = fsView.getFiles(directory, false);
    java.util.Collection<File> list = new ArrayList<File>();
    for(int i = 0; i<files.length; i++) {
      if(files[i].isDirectory()) {
        list.addAll(recursiveGetDirectory(files[i], fsView));
      } else {
        list.add(files[i]);
      }
    }
    return list;
  }

  static private class OverwriteValue {
    int value = NO;

    OverwriteValue(int val) {
      value = val;
    }

  }

  static private class ProgressDialog extends JDialog {
    private int totalSteps;
    private int currentStep = 0;
    private JLabel progressLabel = null;
    private JProgressBar progressBar = null;

    /**
     * Constructor ProgressDialog
     * @param _owner
     * @param _steps
     * @param _title
     * @param _size
     */
    public ProgressDialog(Frame _owner, int _steps, String _title, Dimension _size) {
      super(_owner);
      totalSteps = _steps;
      setTitle(_title);
      setSize(_size);
      setModal(false);
      getContentPane().setLayout(new java.awt.BorderLayout());
      JPanel progressPanel = new JPanel() {
        public Insets getInsets() {
          return new Insets(15, 10, 5, 10);
        }

      };
      progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
      getContentPane().add(progressPanel, BorderLayout.CENTER);
      Dimension d = new Dimension(_size.width, 20);
      progressLabel = new JLabel(_title);
      progressLabel.setAlignmentX(CENTER_ALIGNMENT);
      progressLabel.setMaximumSize(d);
      progressLabel.setPreferredSize(d);
      progressPanel.add(progressLabel);
      progressPanel.add(Box.createRigidArea(new Dimension(1, 20)));
      progressBar = new JProgressBar(0, totalSteps);
      progressBar.setStringPainted(true);
      progressLabel.setLabelFor(progressBar);
      progressBar.setAlignmentX(CENTER_ALIGNMENT);
      progressPanel.add(progressBar);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation((screenSize.width-_size.width)/2, (screenSize.height-_size.width)/2);
      getContentPane().add(progressPanel, BorderLayout.CENTER);
      setCursor(new Cursor(Cursor.WAIT_CURSOR));
      setVisible(true);
    }

    public void reportProgress(String _process) {
      currentStep++;
      progressBar.setValue(currentStep);
      progressLabel.setText(_process);
    }

  }

} // End of class

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
