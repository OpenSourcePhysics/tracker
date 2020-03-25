/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;

import javajs.async.SwingJSUtils;
import javajs.async.SwingJSUtils.StateHelper;
import javajs.async.SwingJSUtils.StateMachine;

/**
 * A base interface for a simulation
 */
public abstract class Simulation implements java.lang.Runnable, StateMachine {
  static public final int MAXIMUM_FPS = 25;
  static public final int MINIMUM_FPS = 1;
  private Model model = null;
  private View view = null;
  private java.lang.Thread thread = null;
  private boolean autoplay = false, isPlaying = false;
  private long delay = 0;
  private java.net.URL codebase = null;

  private void errorMessage(String _text) {
    System.err.println(this.getClass().getName()+": "+_text); //$NON-NLS-1$
  }

  private void errorMessage(Exception _exc) {
    System.err.println(this.getClass().getName()+": Exception caught! Text follows:"); //$NON-NLS-1$
    _exc.printStackTrace(System.err);
  }

  // -----------------------------
  // Relationship with its parts
  // -----------------------------
  public Model getModel() {
    return model;
  }

  public void setModel(Model _aModel) {
    model = _aModel;
  }

  public View getView() {
    return view;
  }

  public void setView(View _aView) {
    view = _aView;
  }

  /**
    * Sets the codebase
    */
  public void setCodebase(java.net.URL _codebase) {
    codebase = _codebase;
  }

  /**
    * Returns the codebase
    */
  public java.net.URL getCodebase() {
    return codebase;
  }

  // -----------------------------
  // Controlling the execution
  // -----------------------------

  /**
    * Sets the simulation in play mode
    */
  public void play() {
    if(thread!=null) {
      return;
    }
    thread = new Thread(this);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
    isPlaying = true;
  }

  /**
    * Stops the simulation
    */
  public void pause() {
    thread = null;
    isPlaying = false;
    if(org.opensourcephysics.js.JSUtil.isJS) {
    	if(stateHelper!=null)stateHelper.setState(STATE_DONE);
    	return;
    }
  }
  
	private StateHelper stateHelper;
	private int delayJS = (/** @j2sNative delay || */ (int)delay);
	private final static int STATE_INIT = 0;
	private final static int STATE_LOOP = 1;
	private final static int STATE_DONE = 2;

	public boolean stateLoop() {
		while (thread != null && !thread.isInterrupted() && stateHelper.isAlive()) {
			switch (stateHelper.getState()) {
			default:
			case STATE_INIT:
				stateHelper.setState(STATE_LOOP);
				stateHelper.sleep(delayJS);
				return true;
			case STATE_LOOP:
				step();
				stateHelper.sleep(delayJS);
				return true;
			case STATE_DONE:
				return false;
			}
		}
		return false;
	}

  /**
    * Implementation of the Runnable interface
    */
  public void run() {
	stateHelper = new SwingJSUtils.StateHelper(this);  
	stateHelper.setState(STATE_INIT);
	stateHelper.sleep(0);
	/*	
    while(thread==Thread.currentThread()) {
      step();
      try {
        Thread.sleep(delay);
      } catch(InterruptedException ie) {}
    }*/
  }

  /**
   * Sets the (approximate) number of frames per second for the simulation
   * @param _fps the number of frames per second
   */
  public void setFPS(int _fps) {
    if(_fps<=MINIMUM_FPS) {
      delay = 1000;
    } else if(_fps>=MAXIMUM_FPS) {
      delay = 0;
    } else {
      delay = (long) (1000.0/_fps);
    }
  }

  /**
   * Sets the delay between two steps of the simulation
   * @param _aDelay the number of milliseconds for the delay
   */
  public void setDelay(int _aDelay) {
    if(_aDelay<0) {
      delay = 0;
    } else {
      delay = _aDelay;
    }
  }

  /**
   * Sets whether the simulation should be set to play mode when it is reset.
   * Default is false.
   * @param _play Whether it should play
   */
  public void setAutoplay(boolean _play) {
    autoplay = _play;
  }

  /**
   * Returns whether the simulation is running or not
   */
  public boolean isPlaying() {
    return isPlaying;
  }

  /**
   * Returns whether the simulation is running or not
   */
  public boolean isPaused() {
    return !isPlaying;
  }

  // ------------------------------------
  // Simulation logic based on the model
  // ------------------------------------

  /**
   * Resets the simulation to a complete initial state
   */
  public void reset() {
    pause();
    if(model!=null) {
      model.reset();
      model.initialize();
      model.update();
    }
    if(view!=null) {
      view.reset();
      view.initialize();
      view.update();
    }
    System.gc();
    if(autoplay) {
      play();
    }
  }

  /**
   * Initialize model using user interface changes
   */
  public void initialize() {
    if(view!=null) {
      view.read();
    }
    if(model!=null) {
      model.initialize();
      model.update();
    }
    if(view!=null) {
      // view.reset();
      view.initialize();
      view.update();
    }
  }

  /**
   * apply user interface changes
   */
  public void apply() {
    if(view!=null) {
      view.read();
    }
    update();
  }

  /**
   * apply user interface changes. Yes, exactly the same as apply() (I need it somewhere else :-)
   */
  public void applyAll() {
    // if (view!=null) Commented for optimization
    view.read();
    update();
  }

  /**
   * apply a single change in the user interface
   */
  public void apply(String _variable) {
    if(view!=null) {
      view.read(_variable);
    }
    // update(); // Should be called by the user
  }

  /**
   * update
   */
  public void update() {
    if(model!=null) {
      model.update();
    }
    if(view!=null) {
      view.update(); // View could be null
    }
  }

  /**
   * step
   */
  public void step() {
    // if (model!=null) Commented for optimization
    model.step();
    update();
  }

  // --------------------------------------------------------
  // Accesing model methods
  // --------------------------------------------------------
  public void updateAfterModelAction() {
    // if (view!=null) view.initialize(); // If initialize sends all the data, this is redundant
    update();
  }

  // --------------------------------------------------------
  // Accesing model variables
  // --------------------------------------------------------
  static private final String dummy = ""; //$NON-NLS-1$
  static private final Class<?> strClass = dummy.getClass();

  /**
     * This method returns a String with the value of a public variable of the
     * model. If the variable is an array, individual element values are
     * separated by a comma. Only public variables of primitive or String
     * type can be accessed.
     * @param _name The name of a public variable of the model
     * @return The value of the variable as a String.
     */
  public String getVariable(String _name) {
    return getVariable(_name, ","); //$NON-NLS-1$
  }

  /**
   * This method returns a String with the value of a public variable of the
   * model. If the variable is an array, individual element values are
   * separated by the specified separator string. Only public variables
   * of primitive or String type can be accessed.
   * @param _name The name of a public variable of the model
   * @param _sep A separator string to use for array variables
   * @return The value of the variable
   */
  public String getVariable(String _name, String _sep) {
    if(model==null) {
      return null;
    }
    try {
      Field field = model.getClass().getField(_name);
      if(field.getType().isArray()) {
        String txt = ""; //$NON-NLS-1$
        Object array = field.get(model);
        int l = Array.getLength(array);
        for(int i = 0; i<l; i++) {
          if(i>0) {
            txt += _sep+Array.get(array, i).toString();
          } else {
            txt += Array.get(array, i).toString();
          }
        }
        return txt;
      }
      return field.get(model).toString();
    } catch(Exception exc) {
      errorMessage(exc);
      return null;
    }
  }

  /**
   * This method sets the value of a public variable of the model. If the
   * variable is an array, individual element values must separated by a
   * comma.
   * In this case, if the number of values specifies differs with the
   * length of the array (a warning may be issued and) either the extra values
   * are ignored (if there are more) or the last elements of the array will be
   * left unmodified (if there are less).
   * If the values provided cannot be parsed to the variable type (an error
   * message may be issued and) the method returns false.
   * Only public variables of primitive or String type can be accessed.
   * @param _name the name of a public variable of the model
   * @param _value the value to be given to the variable
   * @return true if the process was completed sucessfully, false otherwise
   */
  public boolean setVariable(String _name, String _value) {
    return setVariable(_name, _value, ","); //$NON-NLS-1$
  }

  /**
   * This method sets the value of a public variable of the model.
   * If the variable is an array, individual element values must separated by
   * the specified separator string.
   * In this case, if the number of values specifies differs with the
   * length of the array (a warning may be issued and) either the extra values
   * are ignored (if there are more) or the last elements of the array will be
   * left unmodified (if there are less).
   * Only public variables of primitive or String type can be accessed.
   * If the values provided cannot be parsed to the variable type (an error
   * message may be issued and) the method returns false.
   * @param _variable the name of a public variable of the model
   * @param _value the value to be given to the variable
   * @param _sep the separator string for arrays
   * @return true if the process was completed sucessfully, false otherwise
   */
  public boolean setVariable(String _variable, String _value, String _sep) {
    if(model==null) {
      return false;
    }
    try {
      Field field = model.getClass().getField(_variable);
      if(field.getType().isArray()) {
        boolean result = true;
        Object array = field.get(model);
        int i = 0, l = Array.getLength(array);
        Class<?> type = field.getType().getComponentType();
        java.util.StringTokenizer line = new java.util.StringTokenizer(_value, _sep);
        if(l<line.countTokens()) {
          errorMessage("Warning: there are less elements in the array than values provided!"); //$NON-NLS-1$
        } else if(l>line.countTokens()) {
          errorMessage("Warning: there are more elements in the array than values provided!"); //$NON-NLS-1$
        }
        while(line.hasMoreTokens()&&(i<l)) {
          String token = line.nextToken();
          if(type.equals(Double.TYPE)) {
            Array.setDouble(array, i, Double.parseDouble(token));
          } else if(type.equals(Float.TYPE)) {
            Array.setFloat(array, i, Float.parseFloat(token));
          } else if(type.equals(Byte.TYPE)) {
            Array.setByte(array, i, Byte.parseByte(token));
          } else if(type.equals(Short.TYPE)) {
            Array.setShort(array, i, Short.parseShort(token));
          } else if(type.equals(Integer.TYPE)) {
            Array.setInt(array, i, Integer.parseInt(token));
          } else if(type.equals(Long.TYPE)) {
            Array.setLong(array, i, Long.parseLong(token));
          } else if(type.equals(Boolean.TYPE)) {
            if(token.trim().toLowerCase().equals("true")) {                                    //$NON-NLS-1$
              Array.setBoolean(array, i, true);
            } else {
              Array.setBoolean(array, i, false);
            }
          } else if(type.equals(Character.TYPE)) {
            Array.setChar(array, i, token.charAt(0));
          } else if(type.equals(strClass)) {
            Array.set(array, i, token);
          } else {
            result = false;
          }
          i++;
        }
        return result;
      }
      Class<?> type = field.getType();
      if(type.equals(Double.TYPE)) {
        field.setDouble(model, Double.parseDouble(_value));
      } else if(type.equals(Float.TYPE)) {
        field.setFloat(model, Float.parseFloat(_value));
      } else if(type.equals(Byte.TYPE)) {
        field.setByte(model, Byte.parseByte(_value));
      } else if(type.equals(Short.TYPE)) {
        field.setShort(model, Short.parseShort(_value));
      } else if(type.equals(Integer.TYPE)) {
        field.setInt(model, Integer.parseInt(_value));
      } else if(type.equals(Long.TYPE)) {
        field.setLong(model, Long.parseLong(_value));
      } else if(type.equals(Boolean.TYPE)) {
        if(_value.trim().toLowerCase().equals("true")) {                                       //$NON-NLS-1$
          field.setBoolean(model, true);
        } else {
          field.setBoolean(model, false);
        }
      } else if(type.equals(Character.TYPE)) {
        field.setChar(model, _value.charAt(0));
      } else if(type.equals(strClass)) {
        field.set(model, _value);
      } else {
        return false;
      }
      return true;
    } catch(Exception exc) {
      errorMessage(exc);
      return false;
    }
  }

  /**
   * This method is used to set more than one variables of the model
   * at once. Pairs of the type 'variable=value' must be separated
   * by semicolons. Then they will be tokenized and sent to setVariable().
   * @param _valueList the string containing the pairs 'variable=value'
   * @return true if all the variables are correctly set by setVariable()
   * @see setVariable(String,String)
   *
   */
  public boolean setVariables(String _valueList) {
    return setVariables(_valueList, ";", ","); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * This method is used to set more than one variables of the model
   * at once. Pairs of the type 'variable=value' must be separated
   * by the separator string _sep. Then they will be tokenized and
   * sent to setVariable(), using _arraySep as separator string for
   * values of array variables.
   * @param _valueList the string containing the pairs 'variable=value'
   * @param _sep the separator string between pairs
   * @param _arraySep the separator string for values of array variables
   * @return true if all the variables are correctly set by setVariable()
   * @see setVariable(String,String)
   *
   */
  public boolean setVariables(String _valueList, String _sep, String _arraySep) {
    boolean result = true;
    String name = "", value = ""; //$NON-NLS-1$ //$NON-NLS-2$
    java.util.StringTokenizer line = new java.util.StringTokenizer(_valueList, _sep);
    while(line.hasMoreTokens()) {
      String token = line.nextToken();
      int index = token.indexOf('=');
      if(index<0) {
        result = false;
        continue;
      }
      name = token.substring(0, index).trim();
      value = token.substring(index+1).trim();
      boolean partial = setVariable(name, value, _arraySep);
      if(partial==false) {
        result = false;
      }
    }
    update(); // Should this be called by the user explicitly?
    return result;
  }

  // --------------------------------------------------------
  // Input /Output
  // --------------------------------------------------------
  static private java.util.Hashtable<String, Object> memory = new java.util.Hashtable<String, Object>();

  /**
   * Saves the state of the model either to a file on the disk or to memory.
   * If the name of the file starts with the prefix "ejs:", then the
   * state of the model will be saved to memory, otherwise it will be
   * dumped to disk.
   * Security considerations apply when running the simulation as
   * an applet.
   * <p>
   * The state of the model is saved by writing to disk all its public
   * fields which implement the java.io.Serializable interface. This
   * includes primitives and arrays.
   * @param _filename the name of a file (either in disk or in memory)
   * @return true if the file was correctly saved
   */
  public boolean saveState(String _filename) {
    if(model==null) {
      return false;
    }
    try {
      java.io.OutputStream out;
      if(_filename.startsWith("ejs:")) {                   //$NON-NLS-1$
        out = new java.io.ByteArrayOutputStream();
      } else {
        out = new java.io.FileOutputStream(_filename);
      }
      java.io.BufferedOutputStream bout = new java.io.BufferedOutputStream(out);
      java.io.ObjectOutputStream dout = new java.io.ObjectOutputStream(bout);
      java.lang.reflect.Field[] fields = model.getClass().getFields();
      for(int i = 0; i<fields.length; i++) {
        if(fields[i].get(model) instanceof java.io.Serializable) {
          dout.writeObject(fields[i].get(model));
        }
      }
      dout.close();
      if(_filename.startsWith("ejs:")) {                   //$NON-NLS-1$
        memory.put(_filename, ((java.io.ByteArrayOutputStream) out).toByteArray());
      }
      return true;
    } catch(java.lang.Exception ioe) {
      errorMessage("Error when trying to save"+_filename); //$NON-NLS-1$
      ioe.printStackTrace(System.err);
      return false;
    }
  }

  public boolean readState(String _filename) {
    return readState(_filename, null);
  }

  /**
   * Reads the state of the model either from a file on the disk, from memory
   * or from a url location.
   * If the name of the file starts with the prefix "ejs:", then the
   * state of the model will be read from a memory file that must have been
   * created previously with the corresponding call to saveState().
   * If the name of the file starts with "url:" it will be considered
   * a url location and the method will attempt to read the file from this
   * url (either locally or through the network).
   * This file must have been previously created with a call to saveState()
   * with destination a disk file, and then this file must have been
   * copied at the right url location.
   * dumped to disk.
   * If the name of the file does not start with any of those prefixes,
   * then it will be considered to be a file.
   * Security considerations apply when running the simulation as
   * an applet.
   * <p>
   * The state of the model is read by reading from disk all its public
   * fields which implement the java.io.Serializable interface. This
   * includes primitives and arrays.
   * @param _filename the name of a file (either in disk , in memory or a url)
   * @return true if the file was correctly read
   */
  public boolean readState(String _filename, java.net.URL _codebase) {
    if(model==null) {
      return false;
    }
    try {
      java.io.InputStream in;
      // System.out.println ("filename = "+_filename);
      // System.out.println ("codebase = "+_codebase);
      if(_filename.startsWith("ejs:")) {                    //$NON-NLS-1$
        in = new java.io.ByteArrayInputStream((byte[]) memory.get(_filename));
      } else if(_filename.startsWith("url:")) {             //$NON-NLS-1$
        String url = _filename.substring(4);
        // System.out.println ("url = "+url);
        // System.out.println ("codebase = "+_codebase);
        if((_codebase==null)||url.startsWith("http:")) {    //$NON-NLS-1$
          // Do nothing
        } else {
          url = _codebase+url;
        }
        // System.out.println ("URL = "+url.toString());
        in = (new java.net.URL(url)).openStream();
      } else {
        in = new java.io.FileInputStream(_filename);
      }
      java.io.BufferedInputStream bin = new java.io.BufferedInputStream(in);
      java.io.ObjectInputStream din = new java.io.ObjectInputStream(bin);
      java.lang.reflect.Field[] fields = model.getClass().getFields();
      for(int i = 0; i<fields.length; i++) {
        if(fields[i].get(model) instanceof java.io.Serializable) {
          fields[i].set(model, din.readObject());
        }
      }
      din.close();
      if(view!=null) {
        view.initialize();
      }
      update();
      return true;
    } catch(java.lang.Exception ioe) {
      errorMessage("Error when trying to read "+_filename); //$NON-NLS-1$
      ioe.printStackTrace(System.err);
      return false;
    }
  }

} // End of class

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
