/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import org.opensourcephysics.controls.Calculation;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;

/**
 * An EJS control object for Calculations.
 * @author Wolfgang Christian
 * @version 1.0
 */
public class EjsCalculationControl extends EjsControlFrame {
  protected JPanel controlPanel;
  protected DrawingPanel drawingPanel;
  protected Calculation model; // shadows superclass field

  /**
   * Constructor EjsCalculationControl
   * @param model
   * @param frame
   * @param args
   */
  public EjsCalculationControl(Calculation model, DrawingFrame frame, String[] args) {
    super(model, "name=controlFrame;title=QM Superposition;location=400,0;layout=border;exit=true; visible=false"); //$NON-NLS-1$
    this.model = model;
    addTarget("control", this); //$NON-NLS-1$
    addTarget("model", model);  //$NON-NLS-1$
    if(frame!=null) {
      getMainFrame().setAnimated(frame.isAnimated());
      getMainFrame().setAutoclear(frame.isAutoclear());
      getMainFrame().setBackground(frame.getBackground());
      getMainFrame().setTitle(frame.getTitle());
      drawingPanel = frame.getDrawingPanel();
      addObject(drawingPanel, "Panel", "name=drawingPanel; parent=controlFrame; position=center"); //$NON-NLS-1$ //$NON-NLS-2$
      frame.setDrawingPanel(null);
      frame.dispose();
    }
    add("Panel", "name=controlPanel; parent=controlFrame; layout=border; position=south");                                                                                  //$NON-NLS-1$ //$NON-NLS-2$
    add("Panel", "name=buttonPanel;position=west;parent=controlPanel;layout=flow");                                                                                         //$NON-NLS-1$ //$NON-NLS-2$
    //add("Button", "parent=buttonPanel; text=Calculate; action=control.calculate()");
    //add("Button", "parent=buttonPanel; text=Reset; action=control.resetCalculation()");
    add("Button", "parent=buttonPanel;tooltip=Calculate;image=/org/opensourcephysics/resources/controls/images/play.gif; action=control.calculate();name=calculateButton"); //$NON-NLS-1$ //$NON-NLS-2$
    add("Button", "parent=buttonPanel; tooltip=Reset calculation;image=/org/opensourcephysics/resources/controls/images/reset.gif; action=control.resetCalculation()"); //$NON-NLS-1$ //$NON-NLS-2$
    controlPanel = ((JPanel) getElement("controlPanel").getComponent()); //$NON-NLS-1$
    controlPanel.setBorder(new EtchedBorder());
    customize();
    model.setControl(this);
    model.resetCalculation();
    loadXML(args);
    java.awt.Container cont = (java.awt.Container) getElement("controlFrame").getComponent(); //$NON-NLS-1$
    if(!org.opensourcephysics.display.OSPRuntime.appletMode) {
      cont.setVisible(true);
    }
    if(model instanceof PropertyChangeListener) {
      addPropertyChangeListener((PropertyChangeListener) model);
    }
    getMainFrame().pack();
    getMainFrame().doLayout();
    GUIUtils.showDrawingAndTableFrames();
  }

  /**
   * Override this method to customize this EjsSimulationControl.
   */
  protected void customize() {}

  /**
   * Resets the calculation.
   */
  public void resetCalculation() {
    messageArea.setText(""); //$NON-NLS-1$
    GUIUtils.clearDrawingFrameData(true);
    model.resetCalculation();
    if(xmlDefault!=null) { // the default xml loader invokes calculate.
      xmlDefault.loadObject(getOSPApp());
    } else {               // do the calculation to bring everything up to date
      model.calculate();
    }
    GUIUtils.showDrawingAndTableFrames();
  }

  /**
   * Does the calculation.
   */
  public void calculate() {
    GUIUtils.clearDrawingFrameData(true);
    model.calculate();
    GUIUtils.showDrawingAndTableFrames();
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
