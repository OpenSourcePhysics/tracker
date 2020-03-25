/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.Collection;
import javax.swing.JFrame;
import org.opensourcephysics.display.OSPFrame;

/**
 * AbstractCalculation is a template for simple calculations.
 *
 * Implement the calculate method to create a calculation.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public abstract class AbstractCalculation implements Calculation {
  protected OSPFrame mainFrame;                                        // the main frame that closed the program
  protected Control control;                                           // the Calculation's control
  protected DecimalFormat decimalFormat = new DecimalFormat("0.00E0"); // display format for messages //$NON-NLS-1$

  /**
   * Sets object that controls this calculation.
   *
   * The calculation should use this method to register its parameters with the control.
   * This insures that the control displays the program's parameters when it appears onscreen.
   *
   * @param control
   */
  public void setControl(Control control) {
    this.control = control;
    mainFrame = null;
    if(control!=null) {
      if(control instanceof MainFrame) {
        mainFrame = ((MainFrame) control).getMainFrame();
      }
      control.setLockValues(true);
      resetCalculation();
      control.setLockValues(false);
      if(control instanceof Frame) {
        ((Frame) control).pack();
      }
    }
  }

  /**
   * Gets the main OSPFrame.  The main frame will usually exit program when it is closed.
   * @return OSPFrame
   */
  public OSPFrame getMainFrame() {
    return mainFrame;
  }

  /**
   * Adds a child frame that depends on the main frame.
   * Child frames are closed when this frame is closed.
   *
   * @param frame JFrame
   */
  public void addChildFrame(JFrame frame) {
    if((mainFrame==null)||(frame==null)) {
      return;
    }
    mainFrame.addChildFrame(frame);
  }

  /**
   * Gets the main OSPFrame.  The main frame will usually exit program when it is closed.
   * @return OSPFrame
   */
  public OSPApplication getOSPApp() {
    if(control instanceof MainFrame) {
      return((MainFrame) control).getOSPApp();
    }
    return null;
  }

  /**
   * Clears the child frames from the main frame.
   */
  public void clearChildFrames() {
    if(mainFrame==null) {
      return;
    }
    mainFrame.clearChildFrames();
  }

  /**
   * Gets a copy of the ChildFrames collection.
   * @return Collection
   */
  public Collection<JFrame> getChildFrames() {
    return mainFrame.getChildFrames();
  }

  /**
   * Does the calculation.
   */
  public abstract void calculate();

  /**
   * Resets the calculation to a predefined state.
   */
  public void resetCalculation() {
    control.clearMessages();
    reset();
  }

  /**
   * Resets the program to its default state.
   *
   * Override this method to set the program's parameters.
   */
  public void reset() {}

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new OSPCalculationLoader();
  }

  /**
   * Default XMLLoader to save and load data for Calculations.
   */
  static class OSPCalculationLoader extends XMLLoader {
    /**
     * Performs the calculate method when a Calculation is loaded.
     *
     * @param control the control
     * @param obj the object
     */
    public Object loadObject(XMLControl control, Object obj) {
      ((Calculation) obj).calculate();
      return obj;
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
