  /*
   * DBPrefs: Retrieves database and server settings from a specified xml file
   * Copyright (C) 2004  Dr. Chris Upton University of Victoria
   *
   * This program is free software; you can redistribute it and/or
   * modify it under the terms of the GNU General Public License
   * as published by the Free Software Foundation; either version 2
   * of the License, or (at your option) any later version.
   * 
   * This program is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   * 
   * You should have received a copy of the GNU General Public License
   * along with this program; if not, write to the Free Software
   * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
   */
  
  package test.bsml;
  
  import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
  
  /**
   * This class of methods is used to provide applications 
   * with db and server info from a specified xml file.
   * @author Sameer Girn
   */
  public class DBPrefs {
    private static final String XML_ROOT = "/ca/virology/";
    private static final String VOCS_DB = "/ca/virology/vocsdatabases";
    private static String PREFIX; 
    protected static DBPrefs myinstance = null;
  
  /**
   * Constructor which sets the xml file location and imports the preferences
   * @param xmlFileLoc - the location of the xml file containing the preferences
   */
  public DBPrefs(String xmlFileLoc) {
    myinstance = this;
    PREFIX = null;
    loadPrefs(xmlFileLoc);
  }

  /**
   * Constructor which sets the xml file location and imports the preferences
   * @param xmlFileLoc - the location of the xml file containing the preferences
   */
  public DBPrefs(String xmlFileLoc, String prefix) {
    myinstance = this;
    if ((prefix == null) || (prefix.length() < 1)) PREFIX = null;
    else PREFIX = prefix;
    loadPrefs(xmlFileLoc);
 } 
 
 /**
  * Set the xml file location and import the preferences
  * @param xmlFileLoc - the location of the xml file containing the preferences
  */
 private void loadPrefs(String xmlFileLoc) { 
    final String XML_LOC = xmlFileLoc;
    try {
      //remove any existing preferences if they exist (on the client's machine)
      if (PREFIX == null) {
        if (Preferences.userRoot().nodeExists(VOCS_DB)) {
          Preferences.userRoot().node(VOCS_DB).removeNode();
          Preferences.userRoot().node(VOCS_DB).flush();
        }
      } else {
        if (Preferences.userRoot().nodeExists("/" + PREFIX + "/" + VOCS_DB)) {
          Preferences.userRoot().node("/" + PREFIX + "/"  + VOCS_DB).removeNode();
          Preferences.userRoot().node("/" + PREFIX + "/"  + VOCS_DB).flush();
        }
      }
    } catch (BackingStoreException e) {
      System.out.println("BackingStoreException");
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
    try {
      URL url = new URL(XML_LOC);
      Preferences.importPreferences(url.openStream());
    } catch (MalformedURLException e) {
      System.out.println("MalformedURLException");
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    } catch (IOException e ) {
      System.out.println("IOException");
      System.out.println("Error: Could not find "+e.getMessage());
      e.printStackTrace();
    } catch (InvalidPreferencesFormatException e) {
      System.out.println("InvalidPreferencesFormatException");
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  /**
   * Gets the instance of the attribute of the DBPrefs class
   * @return The instance value
   */
  public static DBPrefs getInstance() {
    return myinstance;
  }
  
 
  /**
   * Get a list of all node names (ie display names of databases)
   */
  public String [] getAllDatabaseDisplayNames() {
    Preferences vocsNodes;
    String children[];
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    children = null;
    try {
      children = vocsNodes.childrenNames();
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    } 
    return children;
  }

  /**
   * Given the database name, check whether the name exists
   * @param dbname - the database name
   * @return true, if it exists - false otherwise
   */
  public boolean checkDBExists(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      System.out.println(vocsNodes.toString());
      
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        String s = prefs.get("host", "?");
        assert(s.equals("52.35.121.80"));
        if (prefs.get("database", "").equals(dbname) ) {
          return true;
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return false;
  }
  
  /**
   * Given the database name, return the corresponding host address
   * @param dbname - the database name
   * @return host address
   */
  public String getVOCSHost(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("host", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name, return the corresponding port number
   * @param dbname - the database name
   * @return port number
   */
  public int getVOCSPort(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return Integer.parseInt(prefs.get("port", "-1"));
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return -1;
  }
  
  /**
   * Given the database name, return the corresponding user name
   * @param dbname - the database name
   * @return user name
   */
  public String getVOCSUserName(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("user", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name, return the corresponding password
   * @param dbname - the database name
   * @return password
   */
  public String getVOCSPassword(String dbname) {
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("pass", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name, determine whether the database is public
   * @param dbname - the database name
   * @return isPublic
   */
  public boolean isPublic(String dbname) {
    boolean db_is_public = true;
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (children[i].equals(dbname)) {
          if (prefs.get("isPublic", "").equalsIgnoreCase("true")) {
            db_is_public = true;
          } else {
            db_is_public = false;
          }
          return db_is_public;
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return db_is_public;
  }


  /**
   * Given the database name, return the corresponding databse type
   * @param dbname - the database name
   * @return database type 
   */
  public String getDBType(String dbname) {
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("dbType", "mysql");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
    return "mysql";
  }

  /**
   * Given the database name, return the corresponding databse type
   * @param dbname - the database name
   * @return database type 
   */
  public String getDBHost(String dbname) {
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("dbhost", "localhost");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
    return "localhost";
  }
  
  /**
   * Given the database name, determine whether the virus is single stranded
   * @param dbname - the database name
   * @return isSingleStranded
   */
  public boolean isSingleStranded(String dbname) {
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.getBoolean("isSingleStranded", false);
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return false;  //default value is false
  }
  
  /**
   * Given the database name, determine whether the virus is circular
   * @param dbname - the database name
   * @return isCircular
   */
  public boolean isCircular(String dbname) {
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.getBoolean("isCircular", false);
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return false;  //default value is false
  }

  /**
   * Given the database name, return the corresponding upstream seq length
   * @param dbname - the database name
   * @return port number
   */
  public int getUpstreamSeqLength(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return Integer.parseInt(prefs.get("upstream_seq_length", "100"));
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return 100;
  }

  /**
   * Given the database name, return the corresponding upstream seq length
   * @param dbname - the database name
   * @return port number
   */
  public int getDownstreamSeqLength(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return Integer.parseInt(prefs.get("downstream_seq_length", "100"));
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return 100;
  }
  
  /**
   * Given the database name, get the blastDNADB name
   * @param dbname - the database name
   * @return blastDBNADB
   */
  public String get_blastDNADB(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("blastDNADB", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name, get the blastProteinDB name
   * @param dbname - the database name
   * @return blastProteinDB
   */
  public String get_blastProteinDB(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("blastProteinDB", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name, get the default virus to display
   * @param dbname - the database name
   * @return defaultVirus
   */
  public String get_defaultVirus(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("defaultVirus", "");
        }
      }

    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name, get the itr value
   * @param dbname - the database name
   * @return true if the db has Itr, false otherwise
   */
  public boolean get_Itr(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.getBoolean("Itr", false);  //default value is false
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return false;
  }
  
  /**
   * Given the database name, get the path to precomputed dotter files
   * @param dbname - the database name
   * @return path to dotter files
   */
  public String get_dotterDir(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return prefs.get("dotterDir", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  

  
  /**
   * get the virus names which have available dot plots 
   * @return names
   */
  public Vector get_availableJDotterPlots() {
    Vector names = new Vector();
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.getBoolean("jdotterFiles", false)) {
          names.addElement(children[i]);
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return names;
  }
  
  /**
   * Given the abstract dbnode name in the xml file, get the real database name
   * @param abstractDBName the node name
   * @return dbname
   */
  public String get_actualDBName(String abstractDBName) {
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
        Preferences prefs = vocsNodes.node(abstractDBName);
        return prefs.get("database", "");
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name in the xml file, get the real database name
   * @param dbname - the database name
   * @return dbname
   */
  public String get_physicalDBName(String dbName) {
    Preferences vocsNodes;
    String privateDB = "test";
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbName) ) {
          return prefs.get("physicaldatabase", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * Given the database name in the xml file, get the virus family name
   * @param dbname - the database name
   * @return genome family_id
   */
  public int get_genomeFamily(String dbName) {
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbName) ) {
          return Integer.parseInt(prefs.get("genomefamily", "-1"));
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return -1;
  }
  
  /**
   * Given the database name, get the preferred display name
   * @param dbname - the database name
   * @return the database name to be displayed to the client
   */
  public String get_displayDBName(String dbname) {
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("database", "").equals(dbname) ) {
          return children[i];
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return "";
  }
  
  /**
   * get the available database names 
   * @return names
   */
  public Vector get_databases() {
    Vector names = new Vector();
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
          names.addElement(prefs.get("database", ""));
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return names;
  }

  /**
   * Gets the display DB for actual rehab DB
   * @param rehabDB is rehab database name 
   * @return name to be displayed
   */
  public String get_RehabDBtoDB(String rehabDB) {
    Vector names = new Vector();
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("rehab", "").equals(rehabDB)) {
          return children[i];
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
  
  /**
   * Gets the actual rehab DB name for displayed DB
   * @param dbname is database name 
   * @return rehab db name (actual db name)
   */
  public String get_DBtoRehabDB(String dbname) {
    Vector names = new Vector();
    Preferences vocsNodes;
    
    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (children[i].equals(dbname)) {
          return prefs.get("rehab", "");
        }
      }
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Return a vector that contains all rehab databses
   * @return vector of rehab databases
   */
  public Vector get_rehabDBs() {
    Vector rehab_DB = new Vector();
    Preferences vocsNodes;

    if (PREFIX == null) vocsNodes = Preferences.userRoot().node(VOCS_DB);
    else vocsNodes = Preferences.userRoot().node("/" + PREFIX + "/" + VOCS_DB);
    try {
      String children[] = vocsNodes.childrenNames();
      for (int i = 0; i < children.length; i++) {
        Preferences prefs = vocsNodes.node(children[i]);
        if (prefs.get("isPublic", "true").equalsIgnoreCase("true") && 
           (!prefs.get("rehab", "").equals("")) ) {
              rehab_DB.addElement(prefs.get("rehab", ""));
        }
      }
      return rehab_DB;
    } catch (Exception e) {
      System.out.println("Error: "+e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
}
