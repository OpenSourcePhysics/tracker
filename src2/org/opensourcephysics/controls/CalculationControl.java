/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;

/**
 *  A Control class for Calculations. The GUI consisting of an input table
 *  and calculate and reset buttons designed to control a Calculation.
 *
 * @author       Wolfgang Christian
 * @author       Joshua Gould
 * @version 1.0
 */
public class CalculationControl extends OSPControl {
  protected Calculation calculation;
  final static String resetToolTipText = ControlsRes.CALCULATION_RESET_TIP;
  final static String calcToolTipText = ControlsRes.CALCULATION_CALC_TIP;
  JButton calcBtn;
  JButton resetBtn;

  /**
   *  The CalculationControl constructor.
   *
   * @param  _calculation
   */
  public CalculationControl(Calculation _calculation) {
    super(_calculation);
    calculation = _calculation;
    if(model!=null) {
      String name = model.getClass().getName();
      setTitle(name.substring(1+name.lastIndexOf("."))+" "          //$NON-NLS-1$ //$NON-NLS-2$
               +ControlsRes.getString("CalculationControl.Title")); //$NON-NLS-1$
    }
    calcBtn = new JButton(ControlsRes.CALCULATION_CALC);
    calcBtn.setToolTipText(calcToolTipText);
    resetBtn = new JButton(ControlsRes.CALCULATION_RESET);
    resetBtn.setToolTipText(resetToolTipText);
    calcBtn.addActionListener(new CalcBtnListener());
    resetBtn.addActionListener(new ResetBtnListener());
    buttonPanel.add(calcBtn);
    buttonPanel.add(resetBtn);
    validate();
    pack();
  }

  /**
 * Gets this frame.  Implementation of MainFrame interface.
 * @return OSPFrame
 */
  public OSPFrame getMainFrame() {
    return this;
  }

  /**
   * Refreshes the user interface in response to display changes such as the Language.
   */
  protected void refreshGUI() {
    super.refreshGUI();
    if(calcBtn==null) {
      return;
    }
    calcBtn.setText(ControlsRes.CALCULATION_CALC);
    resetBtn.setText(ControlsRes.CALCULATION_RESET);
  }

  private class CalcBtnListener implements ActionListener {
    /**
     *  Performs the calculation and shows all drawing frames.
     *
     * @param  e
     */
    public void actionPerformed(ActionEvent e) {
      GUIUtils.clearDrawingFrameData(false);
      if(calculation==null) {
        println("The CalculationControl's model is null."); //$NON-NLS-1$
        return;
      }
      calculation.calculate();
      org.opensourcephysics.display.GUIUtils.showDrawingAndTableFrames();
    }

  }

  private class ResetBtnListener implements ActionListener {
    /**
     *  Resets the calculation.
     *
     * @param  e
     */
    public void actionPerformed(ActionEvent e) {
      GUIUtils.clearDrawingFrameData(true);
      if(calculation==null) {
        println("The CalculationControl's model is null."); //$NON-NLS-1$
        return;
      }
      calculation.resetCalculation();
      if(xmlDefault!=null) {
        xmlDefault.loadObject(getOSPApp(), true, true);
      }
      table.refresh();
    }

  }

  /**
   * Creates a calculation control and establishes communication between the control and the model.
   *
   * @param model
   * @return CalculationControl
   */
  public static CalculationControl createApp(Calculation model) {
    CalculationControl control = new CalculationControl(model);
    model.setControl(control);
    return control;
  }

  /**
   * Creates a calculation control and establishes communication between the control and the model.
   * Initial parameters are set using the xml data.
   *
   * @param model Calculation
   * @param xml String[]
   * @return CalculationControl
   */
  public static CalculationControl createApp(Calculation model, String[] xml) {
    CalculationControl control = createApp(model);
    control.loadXML(xml);
    return control;
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
