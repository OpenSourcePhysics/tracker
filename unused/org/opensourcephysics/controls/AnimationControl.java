/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.opensourcephysics.display.GUIUtils;

/**
 *  A GUI consisting of an input text area, a message area, and various buttons
 *  to initialize and control an Animation.
 *
 * @author       Wolfgang Christian
 * @author       Joshua Gould
 * @version 1.0
 */
public class AnimationControl extends OSPControl {
  String resetToolTipText = ControlsRes.ANIMATION_RESET_TIP;
  String initToolTipText = ControlsRes.ANIMATION_INIT_TIP;
  String startToolTipText = ControlsRes.ANIMATION_START_TIP;
  String stopToolTipText = ControlsRes.ANIMATION_STOP_TIP;
  String newToolTipText = ControlsRes.ANIMATION_NEW_TIP;
  String stepToolTipText = ControlsRes.ANIMATION_STEP_TIP;
  String initText = ControlsRes.ANIMATION_INIT;
  String startText = ControlsRes.ANIMATION_START;
  String stopText = ControlsRes.ANIMATION_STOP;
  String resetText = ControlsRes.ANIMATION_RESET;
  String newText = ControlsRes.ANIMATION_NEW;
  boolean stepModeEditing = true;                              // enables input editing while single stepping
  JButton startBtn = new JButton(ControlsRes.ANIMATION_INIT);  // changes to start, stop
  JButton stepBtn = new JButton(ControlsRes.ANIMATION_STEP);
  JButton resetBtn = new JButton(ControlsRes.ANIMATION_RESET); // changes to new

  /**
   *  AnimationControl constructor.
   *
   * @param  animation  the animation for this AnimationControl
   */
  public AnimationControl(Animation animation) {
    super(animation);
    if(model!=null) {
      String name = model.getClass().getName();
      setTitle(name.substring(1+name.lastIndexOf("."))+" Controller"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    startBtn.addActionListener(new StartBtnListener());
    startBtn.setToolTipText(initToolTipText);
    stepBtn.addActionListener(new StepBtnListener());
    stepBtn.setToolTipText(stepToolTipText);
    resetBtn.addActionListener(new ResetBtnListener());
    resetBtn.setToolTipText(resetToolTipText);
    stepBtn.setEnabled(false);
    buttonPanel.add(startBtn);
    buttonPanel.add(stepBtn);
    buttonPanel.add(resetBtn);
    validate();
    pack();
  }

  /**
   * Refreshes the user interface in response to display changes such as Language.
   */
  protected void refreshGUI() {
    super.refreshGUI();
    resetToolTipText = ControlsRes.ANIMATION_RESET_TIP;
    initToolTipText = ControlsRes.ANIMATION_INIT_TIP;
    startToolTipText = ControlsRes.ANIMATION_START_TIP;
    stopToolTipText = ControlsRes.ANIMATION_STOP_TIP;
    newToolTipText = ControlsRes.ANIMATION_NEW_TIP;
    stepToolTipText = ControlsRes.ANIMATION_STEP_TIP;
    if(stepBtn==null) {
      return;
    }
    stepBtn.setText(ControlsRes.ANIMATION_STEP);
    stepBtn.setToolTipText(stepToolTipText);
    if(startBtn.getText().equals(startText)) {
      startBtn.setText(ControlsRes.ANIMATION_START);
      startBtn.setToolTipText(startToolTipText);
    } else if(startBtn.getText().equals(stopText)) {
      startBtn.setText(ControlsRes.ANIMATION_STOP);
      startBtn.setToolTipText(stopToolTipText);
    } else {
      startBtn.setText(ControlsRes.ANIMATION_INIT);
      startBtn.setToolTipText(initToolTipText);
    }
    if(resetBtn.getText().equals(newText)) {
      resetBtn.setText(ControlsRes.ANIMATION_NEW);
      resetBtn.setToolTipText(newToolTipText);
    } else {
      resetBtn.setText(ControlsRes.ANIMATION_RESET);
      resetBtn.setToolTipText(resetToolTipText);
    }
    initText = ControlsRes.ANIMATION_INIT;
    startText = ControlsRes.ANIMATION_START;
    resetText = ControlsRes.ANIMATION_RESET;
    stopText = ControlsRes.ANIMATION_STOP;
    newText = ControlsRes.ANIMATION_NEW;
  }

  /**
   * Disposes all resources.
   */
  public void dispose() {
    if(model instanceof AbstractAnimation) {
      // stops the animation
      ((AbstractAnimation) model).animationThread = null;
      if(((AbstractAnimation) model).swingTimer!=null) {
        ((AbstractAnimation) model).swingTimer.stop();
      }
    }
    super.dispose();
  }

  /**
   *  Signals the control that the animation has completed.
   *  The control should reset itself in preparation for a new
   *  animation. The given message is printed in the message area.
   *
   * @param  message
   */
  public void calculationDone(final String message) {
    // always update a Swing component from the event thread
    if(model instanceof Animation) {
      ((Animation) model).stopAnimation();
    }
    Runnable doNow = new Runnable() {
      public void run() {
        startBtnActionPerformed(new ActionEvent(this, 0, stopText));
        resetBtnActionPerformed(new ActionEvent(this, 0, newText));
        resetBtn.setEnabled(true);
        org.opensourcephysics.display.GUIUtils.enableMenubars(true);
        if(message!=null) {
          println(message);
        }
      }

    };
    try {
      if(SwingUtilities.isEventDispatchThread()) {
        doNow.run();
      } else { // paint within the event thread
        SwingUtilities.invokeAndWait(doNow);
      }
    } catch(java.lang.reflect.InvocationTargetException ex1) {}
    catch(InterruptedException ex1) {}
  }

  /**
   * Method startBtnActionPerformed
   *
   * @param e
   */
  void startBtnActionPerformed(ActionEvent e) {
    // table.getDefaultEditor(Object.class).stopCellEditing();
    if(e.getActionCommand().equals(initText)) {
      stepBtn.setEnabled(true);
      startBtn.setText(startText);
      startBtn.setToolTipText(startToolTipText);
      resetBtn.setText(newText);
      resetBtn.setToolTipText(newToolTipText);
      resetBtn.setEnabled(true);
      readItem.setEnabled(stepModeEditing);
      table.setEnabled(stepModeEditing);
      messageTextArea.setEditable(false);
      GUIUtils.clearDrawingFrameData(false);
      if(model==null) {
        println("This AnimationControl's model is null."); //$NON-NLS-1$
      } else {
        ((Animation) model).initializeAnimation();
      }
      org.opensourcephysics.display.GUIUtils.showDrawingAndTableFrames();
    } else if(e.getActionCommand().equals(startText)) {
      setCustomButtonsEnabled(false);
      startBtn.setText(stopText);
      startBtn.setToolTipText(stopToolTipText);
      stepBtn.setEnabled(false);
      resetBtn.setEnabled(false);
      readItem.setEnabled(false);
      table.setEnabled(false);
      org.opensourcephysics.display.GUIUtils.enableMenubars(false);
      ((Animation) model).startAnimation();
    } else {                                               // action command = Stop
      startBtn.setText(startText);
      setCustomButtonsEnabled(true);
      startBtn.setToolTipText(startToolTipText);
      stepBtn.setEnabled(true);
      resetBtn.setEnabled(true);
      org.opensourcephysics.display.GUIUtils.enableMenubars(true);
      readItem.setEnabled(stepModeEditing);
      table.setEnabled(stepModeEditing);
      ((Animation) model).stopAnimation();
    }
  }

  /**
   * Method resetBtnActionPerformed
   *
   * @param e
   */
  void resetBtnActionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals(resetText)) {
      GUIUtils.clearDrawingFrameData(true);
      if(model==null) {
        println("This AnimationControl's model is null."); //$NON-NLS-1$
        return;
      }
      ((Animation) model).resetAnimation();
      if(xmlDefault!=null) {
        xmlDefault.loadObject(getOSPApp(), true, true);
      }
      table.refresh();
    } else {                                               // action command = New
      startBtn.setText(initText);
      startBtn.setToolTipText(initToolTipText);
      resetBtn.setText(resetText);
      resetBtn.setToolTipText(resetToolTipText);
      stepBtn.setEnabled(false);
      readItem.setEnabled(true);
      table.setEnabled(true);
      messageTextArea.setEditable(true);
      setCustomButtonsEnabled(true);
    }
  }

  /**
   * Method stepBtnActionPerformed
   *
   * @param e
   */
  void stepBtnActionPerformed(ActionEvent e) {
    ((Animation) model).stepAnimation();
  }

  private void setCustomButtonsEnabled(boolean enabled) {
    if(customButtons!=null) {
      for(Iterator<JButton> it = customButtons.iterator(); it.hasNext(); ) {
        (it.next()).setEnabled(enabled);
      }
    }
  }

  /**
   * Class StartBtnListener
   */
  class StartBtnListener implements ActionListener {
    /**
     * Method actionPerformed
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      startBtnActionPerformed(e);
    }

  }

  /**
   * Class ResetBtnListener
   */
  class ResetBtnListener implements ActionListener {
    /**
     * Method actionPerformed
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      resetBtnActionPerformed(e);
    }

  }

  /**
   * Class StepBtnListener
   */
  class StepBtnListener implements ActionListener {
    /**
     * Method actionPerformed
     *
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
      stepBtnActionPerformed(e);
    }

  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new AnimationControlLoader();
  }

  /**
   * A class to save and load data for OSPControls.
   */
  static class AnimationControlLoader extends OSPControlLoader {
    /**
     * Saves object data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      AnimationControl ac = (AnimationControl) obj;
      if(ac.startBtn.getText().equals(ac.stopText)) {
        ac.startBtn.doClick(); // stop the animation if it is running
      }
      control.setValue("initialize_mode", ac.startBtn.getText().equals(ac.initText)); //$NON-NLS-1$
      super.saveObject(control, obj);
    }

    /**
     * Creates an object using data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new AnimationControl(null);
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      AnimationControl ac = (AnimationControl) obj;
      if(ac.startBtn.getText().equals(ac.stopText)) {
        ac.startBtn.doClick(); // stop the animation if it is running
      }
      boolean initMode = control.getBoolean("initialize_mode"); //$NON-NLS-1$
      control.setValue("initialize_mode", null); // don't show this internal parameter //$NON-NLS-1$
      super.loadObject(control, obj);            // load the control's parameters and the model
      // put the animation into the initialize state if it was in this state when it was stopped
      if(initMode) {
        control.setValue("initialize_mode", true); //$NON-NLS-1$
      }
      if(initMode&&ac.startBtn.getText().equals(ac.startText)) {
        ac.resetBtn.doClick();
      }
      if(!initMode&&ac.startBtn.getText().equals(ac.initText)) {
        ac.startBtn.doClick();
      }
      ac.clearMessages();
      return obj;
    }

  }

  /**
   * Creates an animation control and establishes communication between the control and the model.
   *
   * @param model Animation
   * @return AnimationControl
   */
  public static AnimationControl createApp(Animation model) {
    AnimationControl control = new AnimationControl(model);
    model.setControl(control);
    return control;
  }

  /**
   * Creates a animation control and establishes communication between the control and the model.
   * Initial parameters are set using the xml data.
   *
   * @param model Animation
   * @param xml String[]
   * @return AnimationControl
   */
  public static AnimationControl createApp(Animation model, String[] xml) {
    AnimationControl control = createApp(model);
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
