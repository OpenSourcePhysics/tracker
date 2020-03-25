/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.applet.Applet;
import java.applet.AudioClip;
import org.opensourcephysics.ejs.control.value.Value;

/**
 * An element to play sound according to the state of an internal
 * variable. The element does not change this variable
 */
public class ControlSound extends ControlCheckBox {
  static final int VARIABLE = ControlCheckBox.VARIABLE+1; // shadows superclass field
  private AudioClip clip = null;
  private String audioFile = null;
  private boolean playing = false;

  /**
   * Constructor ControlSound
   * @param _visual
   */
  public ControlSound(Object _visual) {
    super(_visual);
    checkbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent _e) {
        if(checkbox.isSelected()) {
          play();
        } else {
          stop();
        }
      }

    });
  }

  public void setAudioClip(String _codebase, String _audioFile) {
    if(_audioFile==null) {
      stop();
      clip = null;
      return;
    }
    try {
      String prefix = "";                          //$NON-NLS-1$
      if(_codebase==null) {
        prefix = "file:";                          //$NON-NLS-1$
      } else {
        prefix = _codebase.toString();
        if(prefix.startsWith("file:")) {           //$NON-NLS-1$
          prefix = "file:///"+prefix.substring(6); //$NON-NLS-1$
        }
        if(!prefix.endsWith("/")) {                //$NON-NLS-1$
          prefix += "/";                           //$NON-NLS-1$
        }
      }
      String filename = prefix+_audioFile;
      java.net.URL url = new java.net.URL(filename);
      clip = Applet.newAudioClip(url);
    } catch(Exception ex) {
      ex.printStackTrace();
      clip = null;
    }
  }

  public void destroy() {
    if(clip!=null) {
      clip.stop();
    }
    clip = null;
    super.destroy();
  }

  public void play() {
    if(clip==null) {
      return;
    }
    clip.loop();
  }

  public void stop() {
    if(clip!=null) {
      clip.stop();
    }
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  static private java.util.ArrayList<String> infoList = null;

  public java.util.ArrayList<String> getPropertyList() {
    if(infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add("audiofile"); //$NON-NLS-1$
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if(_property.equals("audiofile")) { //$NON-NLS-1$
      return "File|String";             //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  // ------------------------------------------------
  // Set and Get the values of the properties
  // ------------------------------------------------
  public void setValue(int _index, Value _value) {
    switch(_index) {
       case 0 :
         setAudioFile(_value.getString());
         break; // audiofile
       case VARIABLE :
         if(_value.getBoolean()!=playing) {
           playing = _value.getBoolean();
           if(playing) {
             play();
           } else {
             stop();
           }
         }
         super.setValue(ControlCheckBox.VARIABLE, _value);
         break;
       default :
         super.setValue(_index-1, _value);
         break;
    }
  }

  public void setDefaultValue(int _index) {
    switch(_index) {
       case 0 :
         setAudioClip(null, null);
         audioFile = null;
         break;
       default :
         super.setDefaultValue(_index-1);
         break;
    }
  }

  public Value getValue(int _index) {
    switch(_index) {
       case 0 :
         return null;
       default :
         return super.getValue(_index-1);
    }
  }

  // -------------------------------------
  // private methods
  // -------------------------------------
  private void setAudioFile(String _audio) {
    if((audioFile!=null)&&audioFile.equals(_audio)) {
      return; // no need to do it again
    }
    audioFile = _audio;
    if(getProperty("_ejs_codebase")!=null) {              //$NON-NLS-1$
      setAudioClip(getProperty("_ejs_codebase"), _audio); //$NON-NLS-1$
    } else if((getSimulation()!=null)&&(getSimulation().getCodebase()!=null)) {
      setAudioClip(getSimulation().getCodebase().toString(), _audio);
    } else {
      setAudioClip(null, _audio);
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
