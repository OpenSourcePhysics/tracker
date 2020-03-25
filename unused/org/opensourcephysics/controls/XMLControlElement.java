/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

import org.opensourcephysics.js.JSUtil;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This is a basic xml control for storing data.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class XMLControlElement implements XMLControl {
  // static constants
  @SuppressWarnings("javadoc")
	public static final int ALWAYS_DECRYPT = 0;
  @SuppressWarnings("javadoc")
	public static final int PASSWORD_DECRYPT = 3;
  @SuppressWarnings("javadoc")
	public static final int NEVER_DECRYPT = 5;
  // static fields
  @SuppressWarnings("javadoc")
	public static int compactArraySize = 0;
  protected static String encoding = "UTF-8";                             //$NON-NLS-1$
  
  // instance fields
  protected String className = "java.lang.Object";                        //$NON-NLS-1$ //changed by W. Christian
  protected Class<?> theClass = null;
  protected String name;
  protected Map<String, Integer> counts = new HashMap<String, Integer>(); // maps numbered names to counts
  protected Object object;
  protected XMLProperty parent;
  protected int level;
  protected ArrayList<String> propNames = new ArrayList<String>();
  protected ArrayList<XMLProperty> props = new ArrayList<XMLProperty>();
  protected BufferedReader input;
  protected BufferedWriter output;
  @SuppressWarnings("javadoc")
	public boolean canWrite;
  protected boolean valid = false;
  protected boolean readFailed = false;
  protected String version;
  protected String doctype = "osp10.dtd";                                 //$NON-NLS-1$
  private String basepath;
  private String password;
  private int decryptPolicy = ALWAYS_DECRYPT;

  /**
   * Constructs an empty control for the Object class.
   */
  public XMLControlElement() {

  /** empty block */
  }

  /**
   * Constructs an empty control for the specified class.
   *
   * @param type the class.
   */
  public XMLControlElement(Class<?> type) {
    this();
    setObjectClass(type);
  }

  /**
   * Constructs and loads a control with the specified object.
   *
   * @param obj the object.
   */
  public XMLControlElement(Object obj) {
    this();
    setObjectClass(obj.getClass());
    saveObject(obj);
  }

  /**
   * Constructs a control with the specified parent.
   *
   * @param parent the parent.
   */
  public XMLControlElement(XMLProperty parent) {
    this();
    this.parent = parent;
    level = parent.getLevel();
  }


  /**
   * BH we need to read the File object directly, as it will have the data in it already.
   * 
   * @param xmlFile
   */
  public XMLControlElement(File xmlFile) {
	    this();
	    readData(getFileData(xmlFile));
  }
  
  /**
   * BH by far the simplest way to read a file in its entirety as byte[] or String
   * 
   * @param xmlFile
   * @return
   */
  private String getFileData(File xmlFile) {
	  try {
		return new String(Files.readAllBytes(Paths.get(xmlFile.toURI())), "UTF-8");
	} catch (IOException e) {
		return "";
	}
  }

/**
   * Constructs a control and reads the specified input.
   * Input may be a file name or an xml string
   *
   * @param input the input string
   */
  public XMLControlElement(String input) {
    this();
    readData(input);
  }

	private void readData(String input) {
		if (input.startsWith("<?xml")) { //$NON-NLS-1$
			readXML(input);
		} else {
			read(input);
		}
	}

/**
   * Constructs a copy of the specified XMLControl.
   *
   * @param control the XMLControl to copy.
   */
  public XMLControlElement(XMLControl control) {
    this();
    readXML(control.toXML());
  }

  /**
   * Locks the control's interface. Values sent to the control will not
   * update the display until the control is unlocked. Not implemented.
   *
   * @param lock boolean
   */
  public void setLockValues(boolean lock) {

  /** empty block */
  }

  /**
   * Sets a property with the specified name and boolean value.
   *
   * @param name the name
   * @param value the boolean value
   */
  public void setValue(String name, boolean value) {
    if(name==null) {
      return;
    }
    setXMLProperty(name, "boolean", String.valueOf(value), false); //$NON-NLS-1$
  }

  /**
   * Sets a property with the specified name and double value.
   *
   * @param name the name
   * @param value the double value
   */
  public void setValue(String name, double value) {
    if(name==null) {
      return;
    }
    setXMLProperty(name, "double", String.valueOf(value), false); //$NON-NLS-1$
  }

  /**
   * Sets a property with the specified name and int value.
   *
   * @param name the name
   * @param value the int value
   */
  public void setValue(String name, int value) {
    if(name==null) {
      return;
    }
    setXMLProperty(name, "int", String.valueOf(value), false); //$NON-NLS-1$
  }

  /**
   * Sets a property with the specified name and object value.
   *
   * @param name the name
   * @param obj the object
   */
  public void setValue(String name, Object obj) {
  	setValue(name, obj, XMLPropertyElement.defaultWriteNullFinalArrayElements);
  }

  /**
   * Sets a property with the specified name and object value.
   *
   * @param name the name
   * @param obj the object
   * @param writeNullFinalElement true to write a final null array element (if needed)
   */
  public void setValue(String name, Object obj, boolean writeNullFinalElement) {
    if(name==null) {
      return;
    }
    // clear the property if obj is null
    if (obj==null) {
      Iterator<XMLProperty> it = props.iterator();
      while(it.hasNext()) {
        XMLProperty prop = it.next();
        if(name.equals(prop.getPropertyName())) {
          it.remove();
          propNames.remove(name);
          break;
        }
      }
      return;
    }
  	if (obj instanceof Boolean) {
  		setValue(name, ((Boolean)obj).booleanValue());
  		return;
  	}
    String type = XML.getDataType(obj);
    if(type!=null) {
      if(type.equals("int")||type.equals("double")) { //$NON-NLS-1$ //$NON-NLS-2$
        obj = obj.toString();
      }
      setXMLProperty(name, type, obj, writeNullFinalElement);
    }
  }

  /**
   * Gets the boolean value of the specified named property.
   *
   * @param name the name
   * @return the boolean value, or false if none found
   */
  public boolean getBoolean(String name) {
    XMLProperty prop = getXMLProperty(name);
    if (prop!=null && prop.getPropertyType().equals("boolean")) {       //$NON-NLS-1$
      return "true".equals(prop.getPropertyContent().get(0));          //$NON-NLS-1$
    } 
    else if (prop!=null && prop.getPropertyType().equals("string")) { //$NON-NLS-1$
      return "true".equals(prop.getPropertyContent().get(0));          //$NON-NLS-1$
    }
    return false;
  }

  /**
   * Gets the double value of the specified named property.
   *
   * @param name the name
   * @return the double value, or Double.NaN if none found
   */
  public double getDouble(String name) {
    XMLProperty prop = getXMLProperty(name);
    if((prop!=null) && (prop.getPropertyType().equals("double") //$NON-NLS-1$
    		|| prop.getPropertyType().equals("int") //$NON-NLS-1$
    		|| prop.getPropertyType().equals("string"))) { //$NON-NLS-1$
    	try {
    		return Double.parseDouble((String) prop.getPropertyContent().get(0));
    	} catch(Exception ex) {
      	return Double.NaN;
      }
    }
    return Double.NaN;
  }

  /**
   * Gets the int value of the specified named property.
   *
   * @param name the name
   * @return the int value, or Integer.MIN_VALUE if none found
   */
  public int getInt(String name) {
    XMLProperty prop = getXMLProperty(name);
    if((prop!=null) && (prop.getPropertyType().equals("int")  //$NON-NLS-1$
    		|| prop.getPropertyType().equals("string"))) {           //$NON-NLS-1$
    	try {
        return Integer.parseInt((String) prop.getPropertyContent().get(0));
      } catch(Exception ex) {
      	return Integer.MIN_VALUE;
      }
    } else if((prop!=null)&&prop.getPropertyType().equals("object")) { //$NON-NLS-1$
      XMLControl control = (XMLControl) prop.getPropertyContent().get(0);
      if(control.getObjectClass()==OSPCombo.class) {
        OSPCombo combo = (OSPCombo) control.loadObject(null);
        return combo.getSelectedIndex();
      }
    }
    return Integer.MIN_VALUE;
  }

  /**
   * Gets the string value of the specified named property.
   *
   * @param name the name
   * @return the string value, or null if none found
   */
  public String getString(String name) {
    XMLProperty prop = getXMLProperty(name);
    if((prop!=null)&&prop.getPropertyType().equals("string")) { //$NON-NLS-1$
      String content = (String) prop.getPropertyContent().get(0);
      if(content.indexOf(XML.CDATA_PRE)!=-1) {
        content = content.substring(content.indexOf(XML.CDATA_PRE)+XML.CDATA_PRE.length(), content.indexOf(XML.CDATA_POST));
      }
      return content;
    } else if(name.equals("basepath")&&(getRootControl()!=null)) {     //$NON-NLS-1$
      return getRootControl().basepath;
    } else if((prop!=null)&&prop.getPropertyType().equals("object")) { //$NON-NLS-1$
      XMLControl control = (XMLControl) prop.getPropertyContent().get(0);
      if(control.getObjectClass()==OSPCombo.class) {
        OSPCombo combo = (OSPCombo) control.loadObject(null);
        return combo.toString();
      }
    }
    return null;
  }

  /**
   * Gets the object value of the specified named property.
   *
   * @param name the name
   * @return the object, or null if not found
   */
  public Object getObject(String name) {
    XMLProperty prop = getXMLProperty(name);
    if(prop!=null) {
      String type = prop.getPropertyType();
      if(type.equals("object")) {            //$NON-NLS-1$
        return objectValue(prop);
      } else if(type.equals("array")) {      //$NON-NLS-1$
        return arrayValue(prop);
      } else if(type.equals("collection")) { //$NON-NLS-1$
        return collectionValue(prop);
      } else if(type.equals("int")) {        //$NON-NLS-1$
        return new Integer(intValue(prop));
      } else if(type.equals("double")) {     //$NON-NLS-1$
        return new Double(doubleValue(prop));
      } else if(type.equals("boolean")) {    //$NON-NLS-1$
        return new Boolean(booleanValue(prop));
      } else if(type.equals("string")) {     //$NON-NLS-1$
        return stringValue(prop);
      }
    }
    return null;
  }

  /**
   * Gets the set of property names.
   *
   * @return a set of names
   */
  public Collection<String> getPropertyNames() {
    synchronized(propNames) {
      return new ArrayList<String>(propNames);
    }
  }

  /**
   * Gets the type of the specified property. Returns null if the property
   * is not found.
   *
   * @param name the property name
   * @return the type
   */
  public String getPropertyType(String name) {
    XMLProperty prop = getXMLProperty(name);
    if(prop!=null) {
      return prop.getPropertyType();
    }
    return null;
  }

  /**
   * Sets the password. Files are encrypted when the password is non-null.
   *
   * @param pass the password or phrase
   */
  public void setPassword(String pass) {
    password = pass;
    if(getObjectClass()!=Cryptic.class) {
      setValue("xml_password", pass); //$NON-NLS-1$
    }
  }

  /**
   * Gets the password.
   *
   * @return the password
   */
  public String getPassword() {
    if(password==null) {
      password = getString("xml_password"); //$NON-NLS-1$
    }
    return password;
  }

  /**
   * Sets the decryption policy.
   *
   * @param policy the decryption policy: NEVER_DECRYPT, PASSWORD_DECRYPT or ALWAYS_DECRYPT
   */
  public void setDecryptPolicy(int policy) {
    if(policy==NEVER_DECRYPT) {
      decryptPolicy = NEVER_DECRYPT;
    } else if(policy==PASSWORD_DECRYPT) {
      decryptPolicy = PASSWORD_DECRYPT;
    } else {
      decryptPolicy = ALWAYS_DECRYPT;
    }
  }

  /**
   * Reads data into this control from a named source.
   *
   * @param name the name
   * @return the path of the opened document or null if failed
   */
  public String read(String name) {
    OSPLog.finest("reading "+name); //$NON-NLS-1$
    Resource res = ResourceLoader.getResource(name);
    if(res!=null) {
      read(res.openReader());
      String path = XML.getDirectoryPath(name);
      if(!path.equals("")) { //$NON-NLS-1$
        ResourceLoader.addSearchPath(path);
        basepath = path;
      } else {
        basepath = XML.getDirectoryPath(res.getAbsolutePath());
      }
      File file = res.getFile();
      canWrite = ((file!=null)&&file.canWrite());
      return res.getAbsolutePath();
    }
    readFailed = true;
    return null;
  }

  /**
   * Reads the control from an xml string.
   *
   * @param xml the xml string
   */
  public void readXML(String xml) {
    input = new BufferedReader(new StringReader(xml));
    readInput();
    if(!failedToRead()) {
      canWrite = false;
    }
  }

  /**
   * Reads the control from a Reader.
   *
   * @param in the Reader
   */
  public void read(Reader in) {
    if(in instanceof BufferedReader) {
      input = (BufferedReader) in;
    } else {
      input = new BufferedReader(in);
    }
    readInput();
    try {
      input.close();
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Reads data into this control from a named source if the source
   * specifies the same class as the current className.
   *
   * @param name the name
   * @param type the class
   * @return the path of the opened document or null if failed
   */
  public String readForClass(String name, Class<?> type) {
    Resource res = ResourceLoader.getResource(name);
    if(res==null) {
      return null;
    }
    input = new BufferedReader(res.openReader());
    if(!isInputForClass(type)) {
      return null;
    }
    return read(name);
  }

  /**
   * Reads this control from an xml string if the xml specifies the
   * same class as the current className.
   *
   * @param xml the xml string
   * @param type the class
   * @return true if successfully read
   */
  public boolean readXMLForClass(String xml, Class<?> type) {
    input = new BufferedReader(new StringReader(xml));
    if(!isInputForClass(type)) {
      return false;
    }
    readXML(xml);
    return !readFailed;
  }

  /**
   * Returns true if the most recent read operation failed.
   *
   * @return <code>true</code> if the most recent read operation failed
   */
  public boolean failedToRead() {
    return readFailed;
  }

  /**
   * Writes this control as an xml file with the specified name.
   *
   * @param fileName the file name
   * @return the path of the saved document or null if failed
   */
  public String write(String fileName) {
    canWrite = true;
    int n = fileName.lastIndexOf("/"); //$NON-NLS-1$
    if(n<0) {
      n = fileName.lastIndexOf("\\"); //$NON-NLS-1$
    }
    if(n>0) {
      String dir = fileName.substring(0, n+1);
      File file = new File(dir);
      if(!file.exists()&&!file.mkdirs()) {
        canWrite = false;
        return null;
      }
    }
    try {
      File file = new File(fileName);
      if(file.exists()&&!file.canWrite()) {
    		JOptionPane.showMessageDialog(null, 
    				ControlsRes.getString("Dialog.ReadOnly.Message")+": "+file.getPath(),  //$NON-NLS-1$ //$NON-NLS-2$
    				ControlsRes.getString("Dialog.ReadOnly.Title"),  //$NON-NLS-1$
    				JOptionPane.PLAIN_MESSAGE);
        canWrite = false;
        return null;
      }
      
      if (!JSUtil.isJS) {
    
    	  
      FileOutputStream stream = new FileOutputStream(file);
      java.nio.charset.Charset charset = java.nio.charset.Charset.forName(encoding);
      write(new OutputStreamWriter(stream, charset));
      // add search path to ResourceLoader
      if(file.exists()) {
        String path = XML.getDirectoryPath(file.getCanonicalPath());
        ResourceLoader.addSearchPath(path);
      }
      // write dtd if valid
      if(isValid()) {
        // replace xml file name with dtd file name
        if(fileName.indexOf("/")!=-1) {                                                //$NON-NLS-1$
          fileName = fileName.substring(0, fileName.lastIndexOf("/")+1)+getDoctype();  //$NON-NLS-1$
        } else if(fileName.indexOf("\\")!=-1) {                                        //$NON-NLS-1$
          fileName = fileName.substring(0, fileName.lastIndexOf("\\")+1)+getDoctype(); //$NON-NLS-1$
        } else {
          fileName = doctype;
        }
        writeDocType(new FileWriter(fileName));
      }
      
      }
      
      if(file.exists()) {
        return XML.getAbsolutePath(file);
      }
    } catch(IOException ex) {
      canWrite = false;
      OSPLog.warning(ex.getMessage());
    }
    return null;
  }

  /**
   * Writes this control to a Writer.
   *
   * @param out the Writer
   */
  public void write(Writer out) {
    try {
      output = new BufferedWriter(out);
      String xml = toXML();
      // if password-protected, encrypt the xml string and save the cryptic
      if(getPassword()!=null) {
        Cryptic cryptic = new Cryptic(xml);
        XMLControl control = new XMLControlElement(cryptic);
        xml = control.toXML();
      }
      output.write(xml);
      output.flush();
      output.close();
    } catch(IOException ex) {
      OSPLog.info(ex.getMessage());
    }
  }

  /**
   * Writes the DTD to a Writer.
   *
   * @param out the Writer
   */
  public void writeDocType(Writer out) {
    try {
      output = new BufferedWriter(out);
      output.write(XML.getDTD(getDoctype()));
      output.flush();
      output.close();
    } catch(IOException ex) {
      OSPLog.info(ex.getMessage());
    }
  }

  /**
   * Returns this control as an xml string.
   *
   * @return the xml string
   */
  public String toXML() {
    return toString();
  }

  /**
   * Sets the valid property.
   *
   * @param valid <code>true</code> to write the DTD and DocType
   */
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  /**
   * Gets the valid property. When true, this writes the DTD and defines
   * the DocType when writing an xml document. Note: the presence or absense
   * of the DocType header and DTD has no effect on the read() methods--this
   * will always read a well-formed osp document and ignore a non-osp document.
   *
   * @return <code>true</code> if this is valid
   */
  public boolean isValid() {
    return valid&&(XML.getDTD(getDoctype())!=null);
  }

  /**
   * Sets the version.
   *
   * @param vers the version data
   */
  public void setVersion(String vers) {
    version = vers;
  }

  /**
   * Gets the version. May return null.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets the doctype. Not yet implemented since only one doctype is defined.
   *
   * @param name the doctype resource name
   */
  public void setDoctype(String name) {
    if(XML.getDTD(name)!=null) {
      // check that name is accepted, etc
      // could make acceptable names be public String constants?
    }
  }

  /**
   * Gets the doctype. May return null.
   *
   * @return the doctype
   */
  public String getDoctype() {
    return doctype;
  }

  /**
   * Sets the class of the object for which this element stores data.
   *
   * @param type the <code>Class</code> of the object
   */
  public void setObjectClass(Class<?> type) {
    if((object!=null)&&!type.isInstance(object)) {
      throw new RuntimeException(object+" "+ControlsRes.getString("XMLControlElement.Exception.NotInstanceOf")+" "+type); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    className = type.getName();
    theClass = type;
  }

  /**
   * Gets the class of the object for which this element stores data.
   *
   * @return the <code>Class</code> of the object
   */
  public Class<?> getObjectClass() {
    if(className==null) {
      return null;
    }
    if((theClass!=null)&&theClass.getName().equals(className)) {
      return theClass;
    }
    theClass = null;
    try {
      theClass = Class.forName(className);
    } catch(ClassNotFoundException ex) {

    /** empty block */
    }
    ClassLoader loader = XML.getClassLoader();
    if((loader!=null)&&(theClass==null)) {
      try {
        theClass = loader.loadClass(className);
      } catch(ClassNotFoundException ex) {

      /** empty block */
      }
    }
    return theClass;
  }

  /**
   * Gets the name of the object class for which this element stores data.
   *
   * @return the object class name
   */
  public String getObjectClassName() {
    return className;
  }

  /**
   * Saves an object's data in this element.
   *
   * @param obj the object to save.
   */
  public void saveObject(Object obj) {
    if(obj==null) {
      obj = object;
    }
    Class<?> type = getObjectClass();
    if((type==null)||type.equals(Object.class)) {
      if(obj==null) {
        return;
      }
      type = obj.getClass();
    }
    if(type.isInstance(obj)) {
      object = obj;
      className = obj.getClass().getName();
      clearValues();
      XML.ObjectLoader loader = XML.getLoader(type);
      loader.saveObject(this, obj);
    }
  }

  /**
   * Loads an object with data from this element. This asks the user for
   * approval and review before importing data from mismatched classes.
   *
   * @param obj the object to load
   * @return the loaded object
   */
  public Object loadObject(Object obj) {
    return loadObject(obj, false, false);
  }

  /**
   * Loads an object with data from this element. This asks the user to
   * review data from mismatched classes before importing it.
   *
   * @param obj the object to load
   * @param autoImport true to automatically import data from mismatched classes
   * @return the loaded object
   */
  public Object loadObject(Object obj, boolean autoImport) {
    return loadObject(obj, autoImport, false);
  }

  /**
   * Loads an object with data from this element.
   *
   * @param obj the object to load
   * @param autoImport true to automatically import data from mismatched classes
   * @param importAll true to import all importable data
   * @return the loaded object
   */
  public Object loadObject(Object obj, boolean autoImport, boolean importAll) {
    Class<?> type = getObjectClass();
    if(type==null) {
      if(obj!=null) {
        if(!autoImport) {
          int result = JOptionPane.showConfirmDialog(null, ControlsRes.getString("XMLControlElement.Dialog.UnknownClass.Message")+" \""+className+"\""+XML.NEW_LINE //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            +ControlsRes.getString("XMLControlElement.Dialog.MismatchedClass.Query")+" \""+obj.getClass().getName()+"\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              ControlsRes.getString("XMLControlElement.Dialog.MismatchedClass.Title"), //$NON-NLS-1$
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
          if(result!=JOptionPane.YES_OPTION) {
            return obj;
          }
        }
        if(!importInto(obj, importAll)) {
          return obj;
        }
        type = obj.getClass();
      } else {
        return null;
      }
    }
    try {
      if(XML.getLoader(type).getClass()==XML.getLoader(obj.getClass()).getClass()) {
        autoImport = true;
        importAll = true;
      }
    } catch(Exception ex) {

    /** empty block */
    }
    if((obj!=null)&&!type.isInstance(obj)) {
      if(!autoImport) {
        int result = JOptionPane.showConfirmDialog(null, ControlsRes.getString("XMLControlElement.Dialog.MismatchedClass.Message")+" \""+type.getName()+"\""+XML.NEW_LINE //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          +ControlsRes.getString("XMLControlElement.Dialog.MismatchedClass.Query")+" \""+obj.getClass().getName()+"\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            ControlsRes.getString("XMLControlElement.Dialog.MismatchedClass.Title"), //$NON-NLS-1$
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if(result!=JOptionPane.YES_OPTION) {
          return obj;
        }
      }
      if(!importInto(obj, importAll)) {
        return obj;
      }
      type = obj.getClass();
    }
    XML.ObjectLoader loader = XML.getLoader(type);
    if(obj==null) { // if obj is null, try to create a new one
      if(object==null) {
        object = loader.createObject(this);
      }
      obj = object;
    }
    if(obj==null) {
      return null; // unable to create new obj
    }
    if(type.isInstance(obj)) {
      obj = loader.loadObject(this, obj);
      object = obj;
    }
    return obj;
  }

  /**
   * Clears all properties.
   */
  public void clearValues() {
    props.clear();
    propNames.clear();
  }

  /**
   * Method required by the Control interface.
   *
   * @param s the string
   */
  public void println(String s) {
    System.out.println(s);
  }

  /**
   * Method required by the Control interface.
   */
  public void println() {
    System.out.println();
  }

  /**
   * Method required by the Control interface.
   *
   * @param s the string
   */
  public void print(String s) {
    System.out.print(s);
  }

  /**
   * Method required by the Control interface.
   */
  public void clearMessages() {

  /** empty block */
  }

  /**
   * Method required by the Control interface.
   *
   * @param s the string
   */
  public void calculationDone(String s) {

  /** empty block */
  }

  /**
   * Gets the property name.
   *
   * @return a name
   */
  public String getPropertyName() {
    XMLProperty parent = getParentProperty();
    // if no class name, return parent name
    if(className==null) {
      if(parent==null) {
        return "null";                                              //$NON-NLS-1$
      }
      return parent.getPropertyName();
    }
    // else if array or collection item, return numbered class name
    else if (this.isArrayOrCollectionItem()) {
      if (this.name==null) {
        // add numbering or name property
      	String myName = this.getString("name"); //$NON-NLS-1$
      	if (myName!=null && !"".equals(myName)) { //$NON-NLS-1$
          name = className.substring(className.lastIndexOf(".")+1); //$NON-NLS-1$
      		name += " \""+myName+"\""; //$NON-NLS-1$ //$NON-NLS-2$
      	}
      	else {
	        XMLProperty root = this;
	        while(root.getParentProperty()!=null) {
	          root = root.getParentProperty();
	        }
	        if(root instanceof XMLControlElement) {
	          XMLControlElement rootControl = (XMLControlElement) root;
	          name = className.substring(className.lastIndexOf(".")+1); //$NON-NLS-1$
	          name = rootControl.addNumbering(name);
	        }
      	}
      }
      return ""+name;                                          //$NON-NLS-1$
    }
    // else if this has a parent, return its name
    else if(parent!=null) {
      return parent.getPropertyName();
      // else return the short class name
    } else {
      return className.substring(className.lastIndexOf(".")+1);     //$NON-NLS-1$
    }
  }

  /**
   * Gets the property type.
   *
   * @return the type
   */
  public String getPropertyType() {
    return "object"; //$NON-NLS-1$
  }

  /**
   * Gets the property class.
   *
   * @return the class
   */
  public Class<?> getPropertyClass() {
    return getObjectClass();
  }

  /**
   * Gets the immediate parent property, if any.
   *
   * @return the parent
   */
  public XMLProperty getParentProperty() {
    return parent;
  }

  /**
   * Gets the level of this property relative to the root.
   *
   * @return a non-negative integer
   */
  public int getLevel() {
    return level;
  }

  /**
   * Gets the property content of this control.
   *
   * @return a list of XMLProperties
   */
  public List<Object> getPropertyContent() {
    return new ArrayList<Object>(props);
  }

  /**
   * Gets the named XMLControl child of this property. May return null.
   *
   * @param name the property name
   * @return the XMLControl
   */
  public XMLControl getChildControl(String name) {
    XMLControl[] children = getChildControls();
    for(int i = 0; i<children.length; i++) {
      if(children[i].getPropertyName().equals(name)) {
        return children[i];
      }
    }
    return null;
  }

  /**
   * Gets the XMLControl children of this property. The returned array has
   * length for type "object" = 1, "collection" and "array" = 0+, other
   * types = 0.
   *
   * @return an XMLControl array
   */
  public XMLControl[] getChildControls() {
    ArrayList<XMLControl> list = new ArrayList<XMLControl>();
    Iterator<XMLProperty> it = props.iterator();
    while(it.hasNext()) {
      XMLProperty prop = it.next();
      if(prop.getPropertyType().equals("object")) { //$NON-NLS-1$
        list.add((XMLControl) prop.getPropertyContent().get(0));
      }
    }
    return list.toArray(new XMLControl[0]);
  }

  /**
   * Gets the root control.
   *
   * @return the root control
   */
  public XMLControlElement getRootControl() {
    if(parent==null) {
      return this;
    }
    XMLProperty prop = parent;
    while(prop.getParentProperty()!=null) {
      prop = prop.getParentProperty();
    }
    if(prop instanceof XMLControlElement) {
      return(XMLControlElement) prop;
    }
    return null;
  }

  /**
   * Appends numbering to a specified name. Increments the number each time
   * this is called for the same name.
   *
   * @param name the name
   * @return the name with appended numbering
   */
  public String addNumbering(String name) {
    Integer count = counts.get(name);
    if(count==null) {
      count = new Integer(0);
    }
    count = new Integer(count.intValue()+1);
    counts.put(name, count);
    return name+" "+count.toString(); //$NON-NLS-1$
  }

  /**
   * This does nothing since the property type is "object".
   *
   * @param stringValue the string value of a primitive or string property
   */
  public void setValue(String stringValue) {

  /** empty block */
  }

  /**
   * Returns the string xml representation.
   *
   * @return the string xml representation
   */
  public String toString() {
    StringBuffer xml = new StringBuffer(""); //$NON-NLS-1$
    // write the header if this is the top level
    if(getLevel()==0) {
      xml.append("<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");       //$NON-NLS-1$ //$NON-NLS-2$
      if(isValid()) {
        xml.append(XML.NEW_LINE+"<!DOCTYPE object SYSTEM \""+doctype+"\">"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    // write the opening tag
    xml.append(XML.NEW_LINE+indent(getLevel())+"<object class=\""+className+"\""); //$NON-NLS-1$ //$NON-NLS-2$
    // write the version if this is the top level
    if((version!=null)&&(getLevel()==0)) {
      xml.append(" version=\""+version+"\""); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // write the property content and closing tag
    if(props.isEmpty()) {
      xml.append("/>");                                        //$NON-NLS-1$
    } else {
      xml.append(">");                                         //$NON-NLS-1$
      Iterator<XMLProperty> it = props.iterator();
      while(it.hasNext()) {
        xml.append(it.next().toString());
      }
      xml.append(XML.NEW_LINE+indent(getLevel())+"</object>"); //$NON-NLS-1$
    }
    return xml.toString();
  }

  // ____________________________ static methods _________________________________

  /**
   * Returns a list of objects of a specified class within this control.
   *
   * @param type the Class
   * @return the list of objects
   */
  public <T> List<T> getObjects(Class<T> type) {
    return getObjects(type, false);
  }

  /**
   * Returns a list of objects of a specified class within this control.
   *
   * @param type the Class
   * @param useChooser true to allow user to choose
   * @return the list of objects
   */
  public <T> List<T> getObjects(Class<T> type, boolean useChooser) {
    java.util.List<XMLProperty> props;
    if(useChooser) {
      String name = type.getName();
      name = name.substring(name.lastIndexOf(".")+1);                                                                                                                                                                           //$NON-NLS-1$
      // select objects using an xml tree chooser
      XMLTreeChooser chooser = new XMLTreeChooser(ControlsRes.getString("XMLControlElement.Chooser.SelectObjectsOfClass.Title"), ControlsRes.getString("XMLControlElement.Chooser.SelectObjectsOfClass.Label")+" "+name, null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      props = chooser.choose(this, type);
    } else {
      // select all objects of desired type using an xml tree
      XMLTree tree = new XMLTree(this);
      tree.setHighlightedClass(type);
      tree.selectHighlightedProperties();
      props = tree.getSelectedProperties();
    }
    List<T> objects = new ArrayList<T>();
    Iterator<XMLProperty> it = props.iterator();
    while(it.hasNext()) {
      XMLControl prop = (XMLControl) it.next();
      objects.add(type.cast(prop.loadObject(null)));
    }
    return objects;
  }

  /**
   * Returns a copy of this control.
   *
   * @return a clone
   */
  public Object clone() {
    return new XMLControlElement(this);
  }

  // ____________________________ private methods _________________________________

  /**
   * Determines if this is (the child of) an array or collection item.
   *
   * @return true if this is an array or collection item
   */
  private boolean isArrayOrCollectionItem() {
    XMLProperty parent = getParentProperty();
    if(parent!=null) {
      parent = parent.getParentProperty();
      return((parent!=null)&&("arraycollection".indexOf(parent.getPropertyType())>=0)); //$NON-NLS-1$
    }
    return false;
  }

  /**
   * Prepares this control for importing into the specified object.
   *
   * @param obj the importing object
   * @param importAll true to import all
   * @return <code>true</code> if the data is imported
   */
  private boolean importInto(Object obj, boolean importAll) {
    // get the list of importable properties
    XMLControl control = new XMLControlElement(obj);
    Collection<String> list = control.getPropertyNames();
    list.retainAll(this.getPropertyNames());
    // add property values
    Collection<String> names = new ArrayList<String>();
    Collection<Object> values = new ArrayList<Object>();
    for(Iterator<XMLProperty> it = props.iterator(); it.hasNext(); ) {
      XMLProperty prop = it.next();
      String propName = prop.getPropertyName();
      if(!list.contains(propName)) {
        continue;
      }
      names.add(propName);                          // keeps names in same order as values
      if(prop.getPropertyType().equals("object")) { //$NON-NLS-1$
        values.add(prop.getPropertyClass().getSimpleName());
      } else {
        values.add(prop.getPropertyContent().get(0));
      }
    }
    // choose the properties to import
    ListChooser chooser = new ListChooser(ControlsRes.getString("XMLControlElement.Chooser.ImportObjects.Title"), //$NON-NLS-1$
      ControlsRes.getString("XMLControlElement.Chooser.ImportObjects.Label")); //$NON-NLS-1$
    if(names.isEmpty()||importAll||chooser.choose(names, names, values)) {
      // names list now contains property names to keep
      Iterator<XMLProperty> it = props.iterator();
      while(it.hasNext()) {
        XMLProperty prop = it.next();
        if(!names.contains(prop.getPropertyName())) {
          it.remove();
          propNames.remove(prop.getPropertyName());
        }
      }
      // add object properties not in the names list to this control
      Iterator<String> it2 = control.getPropertyNames().iterator();
      while(it2.hasNext()) {
        String name = it2.next();
        if(names.contains(name)) {
          continue;
        }
        String propType = control.getPropertyType(name);
        if(propType.equals("int")) {            //$NON-NLS-1$
          setValue(name, control.getInt(name));
        } else if(propType.equals("double")) {  //$NON-NLS-1$
          setValue(name, control.getDouble(name));
        } else if(propType.equals("boolean")) { //$NON-NLS-1$
          setValue(name, control.getBoolean(name));
        } else if(propType.equals("string")) {  //$NON-NLS-1$
          setValue(name, control.getString(name));
        } else {
          setValue(name, control.getObject(name));
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Sets an XML property.
   *
   * @param name the name
   * @param type the type
   * @param value the value
   * @param writeNullFinalArrayElement true to write a final null array element (if needed)
   */
  private void setXMLProperty(String name, String type, Object value, boolean writeNullFinalArrayElement) {
    // remove any previous property with the same name
    int i = -1;
    if(propNames.contains(name)) {
      Iterator<XMLProperty> it = props.iterator();
      while(it.hasNext()) {
        i++;
        XMLProperty prop = it.next();
        if(prop.getPropertyName().equals(name)) {
          it.remove();
          break;
        }
      }
    } else {
      propNames.add(name);
    }
    if(i>-1) {
      props.add(i, new XMLPropertyElement(this, name, type, value, writeNullFinalArrayElement));
    } else {
      props.add(new XMLPropertyElement(this, name, type, value, writeNullFinalArrayElement));
    }
  }

  /**
   * Gets a named property. May return null.
   *
   * @param name the name
   * @return the XMLProperty
   */
  private XMLProperty getXMLProperty(String name) {
    if(name==null) {
      return null;
    }
    Iterator<XMLProperty> it = props.iterator();
    while(it.hasNext()) {
      XMLProperty prop = it.next();
      if(name.equals(prop.getPropertyName())) {
        return prop;
      }
    }
    return null;
  }

  /**
   * Reads this control from the current input.
   */
  private void readInput() {
    readFailed = false;
    try {
      // get document root opening tag line
      String openingTag = input.readLine();
      int count = 0;
      while (openingTag!=null && openingTag.indexOf("<object class=")==-1) { //$NON-NLS-1$
        count++;
        if (count>9) {
        	// stop reading at 10 lines
        	readFailed = true;
        	return;
        }
        openingTag = input.readLine();
      }
      // read this element from the root
      if(openingTag!=null) {
        // get version, if any
        String xml = openingTag;
        int i = xml.indexOf("version=");                               //$NON-NLS-1$
        if(i!=-1) {
          xml = xml.substring(i+9);
          version = xml.substring(0, xml.indexOf("\""));               //$NON-NLS-1$
        }
        readObject(this, openingTag);
      } else {
        readFailed = true;
        return;
      }
    } catch(Exception ex) {
      readFailed = true;
      OSPLog.warning("Failed to read xml: "+ex.getMessage());          //$NON-NLS-1$
      return;
    }
    // if object class is Cryptic, decrypt and inspect
    if(Cryptic.class.equals(getObjectClass())) {
      Cryptic cryptic = (Cryptic) loadObject(null);
      // get the decrypted xml
      String xml = cryptic.decrypt();
      // return if decrypted xml is not readable by a test control
      XMLControl test = new XMLControlElement(xml);
      if(test.failedToRead()) {
        return;
      }
      // keep current password for possible verification needs
      String pass = password;
      // get the password from the test control
      password = test.getString("xml_password");       //$NON-NLS-1$
      // return if decrypt policy is NEVER or unverified PASSWORD
      switch(decryptPolicy) {
         case NEVER_DECRYPT :
           return;
         case PASSWORD_DECRYPT :
           if((password!=null)&&!password.equals("")&& //$NON-NLS-1$
                        !password.equals(pass)) {
             if(!Password.verify(password, null)) {
               return;
             }
           }
      }
      // otherwise read the decrypted xml into this control
      clearValues();
      object = null;
      className = Object.class.getName();
      theClass = null;
      readXML(xml);
    }
  }

  /**
   * Checks to see if the input is for the specified class.
   */
  private boolean isInputForClass(Class<?> type) {
    try {
      // get document root tag
      String xml = input.readLine();
      while((xml!=null)&&(xml.indexOf("<object")==-1)) {        //$NON-NLS-1$
        xml = input.readLine();
      }
      // check class name
      if(xml!=null) {
        xml = xml.substring(xml.indexOf("class=")+7);           //$NON-NLS-1$
        String className = xml.substring(0, xml.indexOf("\"")); //$NON-NLS-1$
        if(className.equals(type.getName())) {
          return true;
        }
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Reads the current input into an XMLcontrolElement.
   *
   * @param control the control to load
   * @param xml the xml opening tag line
   * @return the loaded element
   * @throws IOException
   */
  private XMLControlElement readObject(XMLControlElement control, String xml) throws IOException {
    control.clearValues();
    // set class name
    xml = xml.substring(xml.indexOf("class=")+7); //$NON-NLS-1$
    String className = xml.substring(0, xml.indexOf("\"")); //$NON-NLS-1$
    // workaround for media package name change
    int i = className.lastIndexOf(".");                     //$NON-NLS-1$
    if(i>-1) {
      String packageName = className.substring(0, i);
      if(packageName.endsWith("org.opensourcephysics.media")) { //$NON-NLS-1$
        className = packageName+".core"+className.substring(i); //$NON-NLS-1$
      }
    }
    control.className = className;
    // look for closing object tag on same line
    if(xml.indexOf("/>")!=-1) { //$NON-NLS-1$
      input.readLine();
      return control;
    }
    // read and process input lines
    XMLProperty prop = control;
    xml = input.readLine();
    while(xml!=null) {
      // closing object tag
      if(xml.indexOf("</object>")!=-1) {      //$NON-NLS-1$
        input.readLine();
        return control;
      }
      // opening property tag
      else if(xml.indexOf("<property")!=-1) { //$NON-NLS-1$
        XMLProperty child = readProperty(new XMLPropertyElement(prop), xml);
        control.props.add(child);
        control.propNames.add(child.getPropertyName());
      }
      xml = input.readLine();
    }
    return control;
  }

  /**
   * Reads the current input into a property element.
   *
   * @param prop the property element to load
   * @param xml the xml opening tag line
   * @return the loaded property element
   * @throws IOException
   */
  private XMLPropertyElement readProperty(XMLPropertyElement prop, String xml) throws IOException {
    // set property name
    prop.name = xml.substring(xml.indexOf("name=")+6, xml.indexOf("type=")-2); //$NON-NLS-1$ //$NON-NLS-2$
    // set property type
    xml = xml.substring(xml.indexOf("type=")+6);                               //$NON-NLS-1$
    prop.type = xml.substring(0, xml.indexOf("\""));                           //$NON-NLS-1$
    // set property content and className
    if(prop.type.equals("array")||prop.type.equals("collection")) {                            //$NON-NLS-1$ //$NON-NLS-2$
      xml = xml.substring(xml.indexOf("class=")+7);                                            //$NON-NLS-1$
      String className = xml.substring(0, xml.indexOf("\""));                                  //$NON-NLS-1$
      // workaround for media package name change
      int i = className.lastIndexOf(".");                                                      //$NON-NLS-1$
      if(i>-1) {
        String packageName = className.substring(0, i);
        if(packageName.endsWith("org.opensourcephysics.media")) {                              //$NON-NLS-1$
          className = packageName+".core"+className.substring(i);                              //$NON-NLS-1$
        }
      }
      prop.className = className;
      if(xml.indexOf("/>")!=-1) {                                                              // property closing tag on same line //$NON-NLS-1$
        return prop;
      }
      xml = input.readLine();
      while(xml.indexOf("<property")!=-1) {                                                    //$NON-NLS-1$
        prop.content.add(readProperty(new XMLPropertyElement(prop), xml));
        xml = input.readLine();
      }
    } else if(prop.type.equals("object")) {                                                    //$NON-NLS-1$
    	// add XMLControl unless value is null
    	if (xml.indexOf(">null</property")==-1) { //$NON-NLS-1$
	      XMLControlElement control = readObject(new XMLControlElement(prop), input.readLine());
	      prop.content.add(control);
	      prop.className = control.className;    		
    	}
    } else {                                                                                   // int, double, boolean or string types
      if(xml.indexOf(XML.CDATA_PRE)!=-1) {
        String s = xml.substring(xml.indexOf(XML.CDATA_PRE));
        while(s.indexOf(XML.CDATA_POST+"</property>")==-1) {                                   // look for end tag //$NON-NLS-1$
          s += XML.NEW_LINE+input.readLine();
        }
        xml = s.substring(0, s.indexOf(XML.CDATA_POST+"</property>")+XML.CDATA_POST.length()); //$NON-NLS-1$
      } else {
        String s = xml.substring(xml.indexOf(">")+1);                                          //$NON-NLS-1$
        while(s.indexOf("</property>")==-1) {                                                  // look for end tag //$NON-NLS-1$
          s += XML.NEW_LINE+input.readLine();
        }
        xml = s.substring(0, s.indexOf("</property>"));                                        //$NON-NLS-1$
      }
      prop.content.add(xml);
    }
    return prop;
  }

  /**
   * Returns a space for indentation.
   *
   * @param level the indent level
   * @return the space
   */
  private String indent(int level) {
    String space = ""; //$NON-NLS-1$
    for(int i = 0; i<XML.INDENT*level; i++) {
      space += " "; //$NON-NLS-1$
    }
    return space;
  }

  /**
   * Returns the object value of the specified property. May return null.
   *
   * @param prop the property
   * @return the array
   */
  private Object objectValue(XMLProperty prop) {
    if(!prop.getPropertyType().equals("object")) { //$NON-NLS-1$
      return null;
    }
    if (prop.getPropertyContent().isEmpty()) 
    	return null;
    XMLControl control = (XMLControl) prop.getPropertyContent().get(0);
    return control.loadObject(null);
  }

  /**
   * Returns the double value of the specified property.
   *
   * @param prop the property
   * @return the value
   */
  private double doubleValue(XMLProperty prop) {
    if(!prop.getPropertyType().equals("double")) { //$NON-NLS-1$
      return Double.NaN;
    }
    return Double.parseDouble((String) prop.getPropertyContent().get(0));
  }

  /**
   * Returns the double value of the specified property.
   *
   * @param prop the property
   * @return the value
   */
  private int intValue(XMLProperty prop) {
    if(!prop.getPropertyType().equals("int")) { //$NON-NLS-1$
      return Integer.MIN_VALUE;
    }
    return Integer.parseInt((String) prop.getPropertyContent().get(0));
  }

  /**
   * Returns the boolean value of the specified property.
   *
   * @param prop the property
   * @return the value
   */
  private boolean booleanValue(XMLProperty prop) {
    return prop.getPropertyContent().get(0).equals("true"); //$NON-NLS-1$
  }

  /**
   * Returns the string value of the specified property.
   *
   * @param prop the property
   * @return the value
   */
  private String stringValue(XMLProperty prop) {
    if(!prop.getPropertyType().equals("string")) { //$NON-NLS-1$
      return null;
    }
    String content = (String) prop.getPropertyContent().get(0);
    if(content.indexOf(XML.CDATA_PRE)!=-1) {
      content = content.substring(content.indexOf(XML.CDATA_PRE)+XML.CDATA_PRE.length(), content.indexOf(XML.CDATA_POST));
    }
    return content;
  }

  /**
   * Returns the array value of the specified property. May return null.
   *
   * @param prop the property
   * @return the array
   */
  private Object arrayValue(XMLProperty prop) {
    if(!prop.getPropertyType().equals("array")) { //$NON-NLS-1$
      return null;
    }
    Class<?> componentType = prop.getPropertyClass().getComponentType();
    List<?> content = prop.getPropertyContent();
    // if no content, return a zero-length array
    if(content.isEmpty()) {
      return Array.newInstance(componentType, 0);
    }
    // determine the format from the first item
    XMLProperty first = (XMLProperty) content.get(0);
    if(first.getPropertyName().equals("array")) { //$NON-NLS-1$
      // create the array from an array string
      Object obj = first.getPropertyContent().get(0);
      if(obj instanceof String) {
        return arrayValue((String) obj, componentType);
      }
      return null;
    }
    // create the array from a list of properties
    // determine the length of the array
    XMLProperty last = (XMLProperty) content.get(content.size()-1);
    String index = last.getPropertyName();
    int n = Integer.parseInt(index.substring(1, index.indexOf("]"))); //$NON-NLS-1$
    // create the array
    Object array = Array.newInstance(componentType, n+1);
    // populate the array
    Iterator<?> it = content.iterator();
    while(it.hasNext()) {
      XMLProperty next = (XMLProperty) it.next();
      index = next.getPropertyName();
      n = Integer.parseInt(index.substring(1, index.indexOf("]"))); //$NON-NLS-1$
      String type = next.getPropertyType();
      if(type.equals("object")) {                                   //$NON-NLS-1$
        Array.set(array, n, objectValue(next));
      } else if(type.equals("int")) {                               //$NON-NLS-1$
        int val = intValue(next);
        if(Object.class.isAssignableFrom(componentType)) {
          Array.set(array, n, new Integer(val));
        } else {
          Array.setInt(array, n, val);
        }
      } else if(type.equals("double")) {                            //$NON-NLS-1$
        double val = doubleValue(next);
        if(Object.class.isAssignableFrom(componentType)) {
          Array.set(array, n, new Double(val));
        } else {
          Array.setDouble(array, n, val);
        }
      } else if(type.equals("boolean")) {                           //$NON-NLS-1$
        boolean val = booleanValue(next);
        if(Object.class.isAssignableFrom(componentType)) {
          Array.set(array, n, new Boolean(val));
        } else {
          Array.setBoolean(array, n, val);
        }
      } else if(type.equals("string")) {                            //$NON-NLS-1$
        Array.set(array, n, stringValue(next));
      } else if(type.equals("array")) {                             //$NON-NLS-1$
        Array.set(array, n, arrayValue(next));
      } else if(type.equals("collection")) {                        //$NON-NLS-1$
        Array.set(array, n, collectionValue(next));
      }
    }
    return array;
  }

  /**
   * Returns the array value of the specified array string. May return null.
   * An array string must start and end with braces and contain only
   * int, double and boolean types.
   *
   * @param arrayString the array string
   * @param componentType the component type of the array
   * @return the array
   */
  private Object arrayValue(String arrayString, Class<?> componentType) {
    if(!(arrayString.startsWith("{")&&arrayString.endsWith("}"))) { //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }
    // trim the outer braces
    String trimmed = arrayString.substring(1, arrayString.length()-1);
    if(componentType.isArray()) {
      // create and collect the array elements from substrings
      ArrayList<Object> list = new ArrayList<Object>();
      ArrayList<Boolean> isNull = new ArrayList<Boolean>();
      Class<?> arrayType = componentType.getComponentType();
             
      int i = trimmed.indexOf("{"); //$NON-NLS-1$
      int j = indexOfClosingBrace(trimmed, i);
      int k = trimmed.indexOf(","); //$NON-NLS-1$
      while(j>0) {
//        if (k<i) { // first comma is before opening brace
        if (k>-1 && k<i) { // first comma is before opening brace
        	isNull.add(true);
          trimmed = trimmed.substring(k+1);
        }
        else {
	        String nextArray = trimmed.substring(i, j+1);
	        Object obj = arrayValue(nextArray, arrayType);
	        list.add(obj);
        	isNull.add(false);
	        trimmed = trimmed.substring(j+1);
	        if (trimmed.startsWith(",")) // comma following closing brace //$NON-NLS-1$
		        trimmed = trimmed.substring(1);
        }
        i = trimmed.indexOf("{"); //$NON-NLS-1$
//        j = trimmed.indexOf("}"); //$NON-NLS-1$
        j = indexOfClosingBrace(trimmed, i);
        k = trimmed.indexOf(","); //$NON-NLS-1$
      }
      // look for trailing null elements
      while (k>-1) {
      	isNull.add(true);
        trimmed = trimmed.substring(k+1);
        k = trimmed.indexOf(","); //$NON-NLS-1$
      }
      if (trimmed.length()>0) { // last element (after final comma) is null
      	isNull.add(true);      	
      }
      // create the array
      Object array = Array.newInstance(componentType, isNull.size());
      // populate the array
      Boolean[] hasNoElement = isNull.toArray(new Boolean[0]);
      Iterator<Object> it = list.iterator();
      for (int n=0; n<hasNoElement.length; n++) {
        if (!hasNoElement[n] && it.hasNext()) {
          Object obj = it.next();
          Array.set(array, n, obj);
        }
      }
      return array;
    }
    // collect element substrings separated by commas
    ArrayList<String> list = new ArrayList<String>();
    while(!trimmed.equals("")) {    //$NON-NLS-1$
      int i = trimmed.indexOf(","); //$NON-NLS-1$
      if(i>-1) {
        list.add(trimmed.substring(0, i));
        trimmed = trimmed.substring(i+1);
      } else {
        list.add(trimmed);
        break;
      }
    }
    // create the array
    Object array = Array.newInstance(componentType, list.size());
    // populate the array
    Iterator<String> it = list.iterator();
    int n = 0;
    while(it.hasNext()) {
      if(componentType==Integer.TYPE) {
        int i = Integer.parseInt(it.next());
        Array.setInt(array, n++, i);
      } else if(componentType==Double.TYPE) {
        double x = Double.parseDouble(it.next());
        Array.setDouble(array, n++, x);
      } else if(componentType==Boolean.TYPE) {
        boolean bool = it.next().equals("true"); //$NON-NLS-1$
        Array.setBoolean(array, n++, bool);
      }
    }
    return array;
  }

  /**
   * Returns the collection value of the specified property. May return null.
   *
   * @param prop the property
   * @return the array
   */
  @SuppressWarnings("unchecked")
  private Object collectionValue(XMLProperty prop) {
    if(!prop.getPropertyType().equals("collection")) { //$NON-NLS-1$
      return null;
    }
    Class<?> classType = prop.getPropertyClass();
    try {
      // create the collection
      Collection<Object> c = (Collection<Object>) classType.newInstance();
      List<Object> content = prop.getPropertyContent();
      // populate the array
      Iterator<Object> it = content.iterator();
      while(it.hasNext()) {
        XMLProperty next = (XMLProperty) it.next();
        String type = next.getPropertyType();
        if(type.equals("object")) {            //$NON-NLS-1$
          c.add(objectValue(next));
        } else if(type.equals("string")) {     //$NON-NLS-1$
          c.add(stringValue(next));
        } else if(type.equals("array")) {      //$NON-NLS-1$
          c.add(arrayValue(next));
        } else if(type.equals("collection")) { //$NON-NLS-1$
          c.add(collectionValue(next));
        }
      }
      return c;
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  /**
   * Returns the index of the closing brace corresponding to the opening
   * brace at the given index in an array string.
   *
   * @param arrayString the array string
   * @param indexOfOpeningBrace the index of the opening brace
   * @return the index of the closing brace
   */
  private int indexOfClosingBrace(String arrayString, int indexOfOpeningBrace) {
    int pointer = indexOfOpeningBrace+1;
    int n = 1; // count up/down for opening/closing braces
    int opening = arrayString.indexOf("{", pointer); //$NON-NLS-1$
    int closing = arrayString.indexOf("}", pointer); //$NON-NLS-1$
    while (n>0) {
    	if (opening>-1 && opening<closing) {
    		n++;
    		pointer = opening+1;
        opening = arrayString.indexOf("{", pointer); //$NON-NLS-1$
    	}
    	else if (closing>-1) {
    		n--;
    		pointer = closing+1;
        closing = arrayString.indexOf("}", pointer);  //$NON-NLS-1$
    	}
    	else return -1;
    }
    return pointer-1;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

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
