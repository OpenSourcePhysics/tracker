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
import org.opensourcephysics.controls.Simulation;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;

public class EjsSimulationControl extends EjsControlFrame {
  protected Simulation model; // shadows superclass field
  protected DrawingPanel drawingPanel;
  protected JPanel controlPanel;

  /**
   * Constructor EjsSimulationControl
   * @param model
   * @param frame
   * @param args
   */
  public EjsSimulationControl(Simulation model, DrawingFrame frame, String[] args) {
    super(model, "name=controlFrame;title=OSP Simulation;location=400,0;layout=border;exit=true; visible=false"); //$NON-NLS-1$
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
    add("Panel", "name=controlPanel; parent=controlFrame; layout=border; position=south");                                                                                                //$NON-NLS-1$ //$NON-NLS-2$
    add("Panel", "name=buttonPanel;position=west;parent=controlPanel;layout=flow");                                                                                                       //$NON-NLS-1$ //$NON-NLS-2$
    //add("Button", "parent=buttonPanel; text=Start; action=control.runSimulation();name=runButton");
    //add("Button", "parent=buttonPanel; text=Step; action=control.stepAnimation()");
    //add("Button", "parent=buttonPanel; text=Reset; action=control.resetAnimation()");
    add("Button", "parent=buttonPanel;tooltip=Start and stop simulation;image=/org/opensourcephysics/resources/controls/images/play.gif; action=control.runSimulation();name=runButton"); //$NON-NLS-1$ //$NON-NLS-2$
    add("Button", "parent=buttonPanel;tooltip=Step simulation;image=/org/opensourcephysics/resources/controls/images/step.gif; action=control.stepSimulation();name=stepButton"); //$NON-NLS-1$ //$NON-NLS-2$
    add("Button", "parent=buttonPanel; tooltip=Reset simulation;image=/org/opensourcephysics/resources/controls/images/reset.gif; action=control.resetSimulation();name=resetButton"); //$NON-NLS-1$ //$NON-NLS-2$
    controlPanel = ((JPanel) getElement("controlPanel").getComponent()); //$NON-NLS-1$
    controlPanel.setBorder(new EtchedBorder());
    customize();
    model.setControl(this);
    initialize();
    loadXML(args);
    java.awt.Container cont = (java.awt.Container) getElement("controlFrame").getComponent(); //$NON-NLS-1$
    if(!org.opensourcephysics.display.OSPRuntime.appletMode) {
      cont.setVisible(true);
    }
    if(model instanceof PropertyChangeListener) {
      addPropertyChangeListener((PropertyChangeListener) model);
    }
    getMainFrame().pack(); // make sure everything is showing
    getMainFrame().doLayout();
    GUIUtils.showDrawingAndTableFrames();
  }

  /**
   * Override this method to customize the EjsSimulationControl.
   */
  protected void customize() {}

  /**
   * Renders (draws) the panel immediately.
   *
   * Unlike repaint, the render method is draws the panel within the calling method's thread.
   * This method is called automatically if the frame is animated.
   */
  public void render() {
    if(drawingPanel!=null) {
      drawingPanel.render(); // simulations should render their panels at every time step
    }
  }

  /**
   * Clears the current XML default.
   */
  public void clearDefaultXML() {
    if((xmlDefault==null)||(model==null)) {
      return;
    }
    xmlDefault = null;
    clearItem.setEnabled(false);
    resetSimulation();
  }

  /**
   * Resets the model and switches the text on the run button.
   */
  public void resetSimulation() {
    model.stopAnimation();
    messageArea.setText(""); //$NON-NLS-1$
    GUIUtils.clearDrawingFrameData(true);
    model.resetAnimation();
    if(xmlDefault!=null) { // loading an object should initialize the model
      xmlDefault.loadObject(getOSPApp());
    } else {
      initialize();
    }
    //getControl("runButton").setProperty("text", "Start");
    getControl("runButton").setProperty("image", "/org/opensourcephysics/resources/controls/images/play.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GUIUtils.showDrawingAndTableFrames();
  }

  public void stepSimulation() {
    if(model.isRunning()) {
      model.stopAnimation();
    }
    //getControl("runButton").setProperty("text", "Start");
    getControl("runButton").setProperty("image", "/org/opensourcephysics/resources/controls/images/play.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    model.stepAnimation();
    GUIUtils.repaintAnimatedFrames();
  }

  /**
   * Runs the Simulation switches the text on the run button
   */
  public void runSimulation() {
    if(model.isRunning()) {
      model.stopSimulation();
      //getControl("runButton").setProperty("text", "Start");
      getControl("runButton").setProperty("image", "/org/opensourcephysics/resources/controls/images/play.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } else {
      //getControl("runButton").setProperty("text", "Stop");
      getControl("runButton").setProperty("image", "/org/opensourcephysics/resources/controls/images/pause.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      model.startSimulation();
    }
  }

  /**
   * Does the calculation.
   */
  public void initialize() {
    model.stopAnimation();
    getControl("runButton").setProperty("image", "/org/opensourcephysics/resources/controls/images/play.gif"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GUIUtils.clearDrawingFrameData(true);
    model.initializeAnimation();
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
