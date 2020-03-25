/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.Font;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.JTextArea;

/**
 * Title:        ParsableTextArea
 * Description:  JTextArea with the ability to parse parameters.
 */
public class ParsableTextArea extends JTextArea {
  HashMap<String, String> pendingMap = new LinkedHashMap<String, String>();
  HashMap<CharSequence, CharSequence> currentMap = new LinkedHashMap<CharSequence, CharSequence>();
  HashMap<CharSequence, CharSequence> lockedMap = new LinkedHashMap<CharSequence, CharSequence>();
  boolean locked = false;

  /**
   * ParsableTextArea constructor.
   *
   */
  public ParsableTextArea() {
    super(10, 10);
    setFont(new Font("Monospaced", Font.PLAIN, 12)); //$NON-NLS-1$
  }

  /**
   * Gets the stored string parameter associated with the variable name.
   *
   * @param variable
   *
   * @return the string
   *
   * @throws VariableNotFoundException
   */
  public String getValue(String variable) throws VariableNotFoundException {
    if(locked) {
      synchronized(lockedMap) {
        String val = (String) lockedMap.get(variable);
        if(val!=null) {
          return(String) lockedMap.get(variable);
        }
        throw new VariableNotFoundException("Variable "+variable+" not found."); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    synchronized(currentMap) {
      updateCurrentMap();              // gets existing values
      synchronized(pendingMap) {
        currentMap.putAll(pendingMap); // add pending values
      }
      String val = (String) currentMap.get(variable);
      if(val!=null) {
        return val;
      }
    }
    throw new VariableNotFoundException("Variable "+variable+" not found."); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Locks values to those currently on display.
   * @param lock boolean
   */
  public synchronized void setLockValues(boolean lock) {
    if(locked==lock) {
      return; // no change so don't do anything
    }
    locked = lock;
    if(locked) {                        // get all current and pending values
      synchronized(lockedMap) {
        lockedMap.clear();
        synchronized(currentMap) {
          updateCurrentMap();
          lockedMap.putAll(currentMap); // add current values
        }
        synchronized(pendingMap) {
          lockedMap.putAll(pendingMap); // add pending values
        }
      }
    } else {                            // control has just been unlocked
      setValue(null, null);             // updates the text area
    }
  }

  /**
   * Stores a variable name and an associated value.
   *
   * @param variable
   * @param val
   */
  public void setValue(String variable, String val) {
    Runnable doLater = new Runnable() {
      public void run() {
        updateText();
      }

    };
    if(variable!=null) {
      synchronized(pendingMap) {
        pendingMap.put(variable, val);
      }
    }
    if(locked&&(variable!=null)) {
      synchronized(lockedMap) {
        lockedMap.put(variable, val);
      }
    }
    if(!locked) {
      java.awt.EventQueue.invokeLater(doLater);
    }
  }

  public HashMap<CharSequence, CharSequence> getCurrentMap() {
    synchronized(currentMap) {
      updateCurrentMap();                  // gets existing values
      synchronized(pendingMap) {
        currentMap.putAll(pendingMap);     // add pending values
      }
      return new HashMap<CharSequence, CharSequence>(currentMap);
    }
  }

  private synchronized void updateText() { // should only be called from event queue
    synchronized(currentMap) {
      synchronized(pendingMap) {
        if(pendingMap.size()==0) {
          return;
        }
        updateCurrentMap();        // puts existing variables into currentMap
        currentMap.putAll(pendingMap);
        pendingMap.clear();
      }
      Set<CharSequence> set = currentMap.keySet();
      Iterator<CharSequence> it = set.iterator();
      StringBuffer newText = new StringBuffer(set.size()*25);
      while(it.hasNext()) {
        String variable = (String) it.next();
        newText.append(variable);
        newText.append('=');
        newText.append(currentMap.get(variable));
        newText.append('\n');
      }
      setText(newText.toString()); // access text area because we are in event queue
    }
  }

  private void updateCurrentMap() {
    currentMap.clear();
    String text = getText();
    StringTokenizer st = new StringTokenizer(text, "\n"); //$NON-NLS-1$
    while(st.hasMoreTokens()) {
      String aLine = st.nextToken().trim();
      int index = aLine.indexOf("="); //$NON-NLS-1$
      if(index!=-1) {
        currentMap.put(aLine.subSequence(0, index), aLine.subSequence(index+1, aLine.length()));
      }
    }
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new ParsableTextAreaLoader();
  }

  /**
   * A class to save and load data for OSPControls.
   */
  static class ParsableTextAreaLoader implements XML.ObjectLoader {
    /**
     * Saves object data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      Map<CharSequence, CharSequence> map = ((ParsableTextArea) obj).getCurrentMap();
      Iterator<CharSequence> it = map.keySet().iterator();
      while(it.hasNext()) {
        String variable = (String) it.next();
        control.setValue(variable, map.get(variable));
      }
    }

    /**
     * Creates an object using data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new ParsableTextArea();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      ParsableTextArea pta = (ParsableTextArea) obj;
      // iterate over properties and add them to pts
      Iterator<String> it = control.getPropertyNames().iterator();
      pta.setLockValues(true);
      while(it.hasNext()) {
        String variable = it.next();
        pta.setValue(variable, control.getString(variable));
      }
      pta.setLockValues(false);
      return obj;
    }

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
