/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.Color;

/**
 * Graphical User Interfaces implement the Control interface.
 *
 * @author       Joshua Gould
 * @author       Wolfgang Christian
 * @version 1.0
 */
public interface Control {
  static final Color NOT_EDITABLE_BACKGROUND = Color.WHITE;

  /**
   * Locks the control's interface. Values sent to the control will not
   * update the display until the control is unlocked.
   *
   * @param lock boolean
   */
  public void setLockValues(boolean lock);

  /**
   * Stores a name and a value in the control.  GUI controls will usually display
   * the name followed by an equal sign followed by the <code>toString<\code> representation
   * of the object.
   *
   * @param name
   * @param val
   */
  public void setValue(String name, Object val);

  /**
   * Stores a name and a double value in the control.  GUI controls will usually display
   * the name followed by an equal sign followed by the  <code>toString<\code> representation
   * of the double.
   *
   * @param name
   * @param val
   */
  public void setValue(String name, double val);

  /**
   * Stores a name and an integer value in the control.  GUI controls will usually display
   * the name followed by an equal sign followed by the  <code>toString<\code> representation
   * of the integer.
   *
   * @param name
   * @param val
   */
  public void setValue(String name, int val);

  /**
   * Stores a name and a boolean value in the control.  GUI controls will usually display
   * the name followed by an equal sign followed by the  <code>toString<\code> representation
   * of the integer.
   *
   * @param name
   * @param val
   */
  public void setValue(String name, boolean val);

  /**
   * Gets a stored integer value from the control.  GUI controls will usually allow the
   * user to edit the value of the parameter.
   *
   * @param name
   *
   * @return the value of the parameter
   */
  public int getInt(String name);

  /**
   * Gets a stored double value from the control.  GUI controls will usually allow the
   * user to edit the value of the parameter.
   *
   * @param name
   *
   * @return the value of the parameter
   */
  public double getDouble(String name);

  /**
   * Gets the object with the specified property name.
   *
   * @param name the name
   * @return the object
   */
  public Object getObject(String name);

  /**
   * Gets a stored string from the control.  Srings have usually been initialized
   * with the <code>setValue (String name, Object val)<\code> method.
   * GUI controls will usually allow the user to edit the value of the parameter.
   *
   * @param name
   *
   * @return the value of the parameter
   */
  public String getString(String name);

  /**
   * Gets a stored boolean from the control.  Srings have usually been initialized
   * with the <code>setValue (String name, Object val)<\code> method.
   * GUI controls will usually allow the user to edit the value of the parameter.
   *
   * @param name
   *
   * @return the value of the parameter
   */
  public boolean getBoolean(String name);

  /**
   * Gets the names of all properties stored in this control.
   *
   * @return List
   */
  public java.util.Collection<String> getPropertyNames();

  /**
   * Prints a string in the control's message area followed by a CR and LF.
   * GUI controls will usually display messages in a non-editable text area.
   *
   * @param s
   */
  public void println(String s);

  /**
   * Prints a blank line in the control's message area.  GUI controls will usually display
   * messages in a non-editable text area.
   */
  public void println();

  /**
   * Prints a string in the control's message area.
   * GUI controls will usually display messages in a non-editable text area.
   *
   * @param s
   */
  public void print(String s);

  /**
   * Clears all text from the control's message area.
   */
  public void clearMessages();

  /**
   * Clears all text from the control's data input area.
   */
  public void clearValues();

  /**
   * Notifies the control when a calculation  has completed.
   * Some controls, such as the animation control, change their appearance
   * during a calculation.  A completed calculation, such as when a
   * predetermined tolerance is reached, can call this method.  The message will
   * be displayed in the control's message area.
   *
   * @param message
   */
  public void calculationDone(String message);

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
