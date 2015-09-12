/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.

 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * A Vector draws a series of VectorSteps that represent a generic
 * time-dependent vector.
 *
 * @author Douglas Brown
 */
public class Vector extends TTrack {

  // instance fields
  protected JMenuItem tailsToOriginItem = new JMenuItem();
  protected JCheckBoxMenuItem labelsVisibleItem;
  protected Map<TrackerPanel, Boolean> visMap = new HashMap<TrackerPanel, Boolean>();

  /**
   * Constructs a Vector.
   */
  public Vector() {
    super();
		defaultColors = new Color[] {
				Color.magenta, Color.cyan, Color.blue, Color.red};
    // set up footprint choices
    setFootprints(new Footprint[]
           {LineFootprint.getFootprint("Footprint.BoldArrow"), //$NON-NLS-1$
    				LineFootprint.getFootprint("Footprint.Arrow"), //$NON-NLS-1$
    				LineFootprint.getFootprint("Footprint.BigArrow")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setColor(defaultColors[0]);
    // turn on trail
    setTrailVisible(true);
    // turn on autoadvance
    setAutoAdvance(true);
    // assign a default name
    setName(TrackerRes.getString("Vector.New.Name")); //$NON-NLS-1$
    // assign default plot variables
    setProperty("xVarPlot0", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot0", "x"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("xVarPlot1", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot1", "y"); //$NON-NLS-1$ //$NON-NLS-2$
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("Vector.Unmarked.Hint"); //$NON-NLS-1$
    // prepare toolbar components
    magLabel.setText("mag"); //$NON-NLS-1$
    // xy action
    Action xyAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        setXYComponents();
        ((NumberField)e.getSource()).requestFocusInWindow();
      }
    };
    // xy focus listener
    FocusListener xyFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setXYComponents();
      }
    };
    // magnitude angle action
    Action magAngleAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        setMagnitudeAngle();
        ((NumberField)e.getSource()).requestFocusInWindow();
      }
    };
    // magnitude angle focus listener
    FocusListener magAngleFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setMagnitudeAngle();
      }
    };
    // add action and focus listeners
    xField.addActionListener(xyAction);
    yField.addActionListener(xyAction);
    xField.addFocusListener(xyFocusListener);
    yField.addFocusListener(xyFocusListener);
    magField.addActionListener(magAngleAction);
    angleField.addActionListener(magAngleAction);
    magField.addFocusListener(magAngleFocusListener);
    angleField.addFocusListener(magAngleFocusListener);
    // tails to origin item
    tailsToOriginItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // snap all vectors to the snapPoint
        Iterator<TrackerPanel> it = Vector.this.panels.iterator();
        while (it.hasNext()) {
          TrackerPanel panel = it.next();
          TPoint p = panel.getSnapPoint();
          Step[] steps = Vector.this.getSteps();
          for (int i = 0; i < steps.length; i++) {
            if (steps[i] != null) {
              VectorStep v = (VectorStep)steps[i];
              if (v.chain != null) v.chain.clear();
              // detach any existing point
              v.attach(null);
              v.attach(p);
            }
          }
          panel.repaint();
        }
      }
    });
    // labels visible item
    labelsVisibleItem = new JCheckBoxMenuItem(TrackerRes.getString("Vector.MenuItem.Label")); //$NON-NLS-1$
    labelsVisibleItem.setSelected(true);
    labelsVisibleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Step[] steps = getSteps();
        for (int i = 0; i < steps.length; i++) {
          if (steps[i] != null) {
            VectorStep step = (VectorStep)steps[i];
            step.setLabelVisible(labelsVisibleItem.isSelected());
            step.erase();
          }
        }
        repaint();
      }
    });
  }

  /**
   * Implements createStep. When creating a vector the coordinates define
   * both the tail and tip position, but when re-marking an existing vector
   * they define the tip position only.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the new step
   */
  public Step createStep(int n, double x, double y) {
    VectorStep step = (VectorStep)getStep(n);
    if (step==null)
    	return createStep(n, x, y, 0, 0);
  	XMLControl state = new XMLControlElement(step);
  	step.tip.setXY(x, y);
  	Undo.postStepEdit(step, state);
  	return step;
  }

  /**
   * Creates a vector step with specified tail position and vector
   * components.
   *
   * @param n the frame number
   * @param x the tail x coordinate in image space
   * @param y the tail y coordinate in image space
   * @param xc the x component in image space
   * @param yc the y component in image space
   * @return the new step
   */
  public Step createStep(int n, double x, double y, double xc, double yc) {
    if (locked) return null;
    VectorStep step = (VectorStep)getStep(n);
    step = new VectorStep(this, n, x, y, xc, yc);
    step.setFirePropertyChangeEvents(true);
    steps.setStep(n, step);
    step.setFootprint(getFootprint());    	
    support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
    return step;
  }

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return VectorStep.getLength();
  }

  /**
   * Gets the length of the footprints required by this track.
   *
   * @return the footprint length
   */
  public int getFootprintLength() {
    return 2;
  }

  /**
   * Overrides TTrack draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    if (panel instanceof TrackerPanel) {
      TrackerPanel trackerPanel = (TrackerPanel)panel;
      if (!isVectorsVisible(trackerPanel)) return;
//      // snap after loading 
//      boolean changed = trackerPanel.changed;
//      if (!snapVectors.isEmpty() && panel.getClass().equals(TrackerPanel.class)) {
//      	Iterator it = snapVectors.iterator();
//      	while (it.hasNext()) {
//      		VectorStep step = (VectorStep)it.next();
//      		step.snap(trackerPanel);
//      	}
//      	snapVectors.clear();
//      	trackerPanel.changed = changed;
//      }
      super.draw(panel, _g);
    }
  }

  /**
   * Overrides TTrack setLocked method.
   *
   * @param locked <code>true</code> to lock this
   */
  public void setLocked(boolean locked) {
    super.setLocked(locked);
    Step[] steps = getSteps();
    for (int i = 0; i < steps.length; i++) {
      VectorStep step = (VectorStep)steps[i];
      if (step != null) step.setTipEnabled(!isLocked());
    }
  }

  /**
   * Responds to property change events. PointMass listens for the following
   * events: "transform" from TrackerPanel.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getSource() instanceof TrackerPanel) {
      String name = e.getPropertyName();
			if (name.equals("adjusting")) { //$NON-NLS-1$
				refreshDataLater = (Boolean)e.getNewValue();
				if (!refreshDataLater) {  // stopped adjusting
		    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
				}
			}
    }
  	super.propertyChange(e);
  }

  /**
   * Refreshes the data.
   *
   * @param data the DatasetManager
   * @param trackerPanel the tracker panel
   */
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
    if (refreshDataLater)
    	return;
    dataFrames.clear();
    VideoPlayer player = trackerPanel.getPlayer();
    VideoClip clip = player.getVideoClip();
    ImageCoordSystem coords = trackerPanel.getCoords();
    // define the datasets
    int count = 0;
    Dataset xComp = data.getDataset(count++);
    Dataset yComp = data.getDataset(count++);
    Dataset mag = data.getDataset(count++);
    Dataset ang = data.getDataset(count++);
    Dataset xTail = data.getDataset(count++);
    Dataset yTail = data.getDataset(count++);
    Dataset stepNum = data.getDataset(count++);
    Dataset frameNum = data.getDataset(count++);
    // assign column names to the datasets
    if (xComp.getColumnName(0).equals("x")) { // not yet initialized //$NON-NLS-1$
      xComp.setXYColumnNames("t", "x"); //$NON-NLS-1$ //$NON-NLS-2$
      yComp.setXYColumnNames("t", "y"); //$NON-NLS-1$ //$NON-NLS-2$
      mag.setXYColumnNames("t", "mag"); //$NON-NLS-1$ //$NON-NLS-2$
      ang.setXYColumnNames("t", "$\\theta$"); //$NON-NLS-1$ //$NON-NLS-2$
      xTail.setXYColumnNames("t", "x_{tail}"); //$NON-NLS-1$ //$NON-NLS-2$
      yTail.setXYColumnNames("t", "y_{tail}"); //$NON-NLS-1$ //$NON-NLS-2$
      stepNum.setXYColumnNames("t", "step"); //$NON-NLS-1$ //$NON-NLS-2$
      frameNum.setXYColumnNames("t", "frame"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else for (int i = 0; i < count; i++) {
    	data.getDataset(i).clear();
    }
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < dataDescriptions.length; i++) {
      dataDescriptions[i] = TrackerRes.getString("Vector.Data.Description."+i); //$NON-NLS-1$
    }
    // get data at each non-null step included in the videoclip
    Step[] stepArray = getSteps();
    for (int n = 0; n < stepArray.length; n++) {
      if (stepArray[n] == null) continue;
      VectorStep step = (VectorStep)stepArray[n];
      // get the frame number of the step
      int frame = step.getFrameNumber();
      // check that the frame is included in the clip
      if (!clip.includesFrame(frame)) continue;
      // get the step number and time
      int stepNumber = clip.frameToStep(frame);
      double t = player.getStepTime(stepNumber)/1000.0;
      if (t < 0) continue; // indicates the time is unknown
      // get the x and y component data
      double xcomp = step.getXComponent();
      double ycomp = step.getYComponent();
      double wxc = coords.imageToWorldXComponent(frame, xcomp, ycomp);
      double wyc = coords.imageToWorldYComponent(frame, xcomp, ycomp);
      // append the data to the data sets
      xComp.append(t, wxc);
      yComp.append(t, wyc);
      mag.append(t, Math.sqrt(wxc*wxc + wyc*wyc));
      ang.append(t, Math.atan2(wyc, wxc));
      // get the tail data
      Point2D tailPosition = step.getTail().getWorldPosition(trackerPanel);
      xTail.append(t, tailPosition.getX());
      yTail.append(t, tailPosition.getY());
      stepNum.append(t, stepNumber);
      frameNum.append(t, frame);
      dataFrames.add(new Integer(frame));
    }
  }

  /**
   * Finds the interactive drawable object located at the specified
   * pixel position.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step TPoint that is hit
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	Interactive ia = super.findInteractive(panel, xpix, ypix);
  	if (ia == null) {
      TPoint p = trackerPanel.getSelectedPoint();
      if (p!=null) {
	      if (p instanceof VectorStep.Handle) {
	    		partName = TrackerRes.getString("Vector.Handle.Name"); //$NON-NLS-1$
	  		  partName += " "+TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
	  		  hint = TrackerRes.getString("Vector.HandleSelected.Hint"); //$NON-NLS-1$
	      }
	      else {
	    		partName = TrackerRes.getString("Vector.Tip.Name"); //$NON-NLS-1$
	  		  partName += " "+TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
	  		  hint = TrackerRes.getString("Vector.TipSelected.Hint"); //$NON-NLS-1$
	      }
      }
      else {
	    	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
	      if (getStep(trackerPanel.getFrameNumber())==null)
	        hint = TrackerRes.getString("Vector.Unmarked.Hint"); //$NON-NLS-1$
	      else { 
	      	hint = TrackerRes.getString("Vector.Remark.Hint"); //$NON-NLS-1$
	      }
      }
  		return null;
  	}
  	if (ia instanceof VectorStep.Handle) {
  		VectorStep.Handle handle = (VectorStep.Handle)ia;
  		partName = TrackerRes.getString("Vector.Handle.Name"); //$NON-NLS-1$
  	  hint = handle.isShort()?
  	  		TrackerRes.getString("Vector.ShortHandle.Hint"): //$NON-NLS-1$
  	  		TrackerRes.getString("Vector.Handle.Hint"); //$NON-NLS-1$
  	}
  	else {
  		partName = TrackerRes.getString("Vector.Tip.Name"); //$NON-NLS-1$
  	  hint = TrackerRes.getString("Vector.Tip.Hint"); //$NON-NLS-1$
  	}
  	return ia;
  }
  
  /**
   * Sets the visibility of force vector labels.
   *
   * @param visible <code>true</code> to show all labels
   */
  public void setLabelsVisible(boolean visible) {
    Step[] steps = this.getSteps();
    for (int i = 0; i < steps.length; i++) {
      VectorStep step = (VectorStep)steps[i];
      if (step != null) {
        step.setLabelVisible(visible);
        step.setRolloverVisible(!visible);
      }
    }
  }

  /**
   * Gets the labels visibility.
   *
   * @return <code>true</code> if labels are visible
   */
  public boolean isLabelsVisible() {
    Step[] steps = this.getSteps();
    for (int i = 0; i < steps.length; i++) {
      VectorStep step = (VectorStep)steps[i];
      if (step != null) return step.isLabelVisible();
    }
    return false;
  }

  /**
   * Sets the visibility of the vectors on the specified tracker panel.
   *
   * @param panel the tracker panel
   * @param visible <code>true</code> to show vectors
   */
  public void setVectorsVisible(TrackerPanel panel, boolean visible) {
    if (visible == isVectorsVisible(panel)) return;
    visMap.put(panel, new Boolean(visible));
    if (!visible) {
      Step step = panel.getSelectedStep();
      if (step != null && step == getStep(step.getFrameNumber())) {
        panel.setSelectedPoint(null);
      }
    }
  }

  /**
   * Gets whether the vectors are visible on the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return <code>true</code> if positions are visible
   */
  public boolean isVectorsVisible(TrackerPanel trackerPanel) {
    if (trackerPanel instanceof WorldTView) {
      trackerPanel = ((WorldTView)trackerPanel).getTrackerPanel();
    }
    Boolean vis = visMap.get(trackerPanel);
    if (vis == null) {
      vis = new Boolean(true);        // vectors are visible by default
      visMap.put(trackerPanel, vis);
    }
    return vis.booleanValue();
  }

  /**
   * Overrides TTrack getMenu method.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
    // remove delete item from end
    if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
      menu.remove(deleteTrackItem);
      if (menu.getItemCount() > 0)
        menu.remove(menu.getItemCount()-1); // remove separator
    }
    // add autoAdvance and markByDefault items at end
    if (trackerPanel.isEnabled("track.autoAdvance") || //$NON-NLS-1$
        trackerPanel.isEnabled("track.markByDefault")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0)
        menu.addSeparator();
      if (trackerPanel.isEnabled("track.autoAdvance")) //$NON-NLS-1$
        menu.add(autoAdvanceItem);
      if (trackerPanel.isEnabled("track.markByDefault")) //$NON-NLS-1$
        menu.add(markByDefaultItem);
    }
    // add tailsToOrigin item
    if (menu.getItemCount() > 0)
      menu.addSeparator();
    tailsToOriginItem.setText(TrackerRes.getString("Vector.MenuItem.ToOrigin")); //$NON-NLS-1$
    menu.add(tailsToOriginItem);
    // replace delete item
    if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0)
        menu.addSeparator();
      TPoint p = trackerPanel.getSelectedPoint();
      Step step = getStep(p, trackerPanel);
      deleteStepItem.setEnabled(step!=null);
      menu.add(deleteStepItem);
      menu.add(clearStepsItem);
      menu.add(deleteTrackItem);
    }
    return menu;
  }

  /**
   * Overrides TTrack getToolbarTrackComponents method.
   *
   * @param trackerPanel the tracker panel
   * @return the DataSetManager
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
    ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    return list;
  }

  /**
   * Overrides TTrack getToolbarPointComponents method.
   *
   * @param trackerPanel the tracker panel
   * @param point the TPoint
   * @return a list of components
   */
  public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel,
                                             TPoint point) {
    ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
    list.add(stepLabel);
    list.add(stepValueLabel);
    list.add(tValueLabel);
    list.add(tSeparator);
    list.add(xLabel);
    list.add(xField);
    list.add(xSeparator);
    list.add(yLabel);
    list.add(yField);
    list.add(ySeparator);
    list.add(magLabel);
    list.add(magField);
    list.add(magSeparator);
    list.add(angleLabel);
    list.add(angleField);
    list.add(angleSeparator);
    xField.setEnabled(!isLocked());
    yField.setEnabled(!isLocked());
    magField.setEnabled(!isLocked());
    angleField.setEnabled(!isLocked());
    return list;
  }

  /**
   * Overrides Object toString method.
   *
   * @return a description of this object
   */
  public String toString() {
    return TrackerRes.getString("Vector.Name") + " \"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

//__________________________ static methods ___________________________

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    XML.setLoader(FrameData.class, new FrameDataLoader());
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {

    /**
     * Saves an object's data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      Vector vec = (Vector) obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      Step[] steps = vec.getSteps();
      FrameData[] data = new FrameData[steps.length];
      for (int n = 0; n < steps.length; n++) {
        if (steps[n] == null) continue;
        VectorStep v = (VectorStep)steps[n];
        data[n] = new FrameData(v, vec.isDependent());
      }
      control.setValue("framedata", data); //$NON-NLS-1$
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new Vector();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      Vector vec = (Vector) obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      // load step data
      FrameData[] data = (FrameData[])control.getObject("framedata"); //$NON-NLS-1$
      if (data != null) {
        boolean locked = vec.isLocked();
        vec.setLocked(false);
        for (int n = 0; n < data.length; n++) {
          if (data[n] == null) {
          	vec.steps.setStep(n, null);
          	continue;
          }
          vec.createStep(n, data[n].x, data[n].y, data[n].xc, data[n].yc);
        }
        vec.setLocked(locked);
     }
      return obj;
    }
  }

//__________________________ private methods ___________________________

  /**
   * Sets the components of the currently selected vector based on the values
   * in the x and y fields.
   */
  private void setXYComponents() {
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel trackerPanel = it.next();
      TPoint p = trackerPanel.getSelectedPoint();
      VectorStep step = (VectorStep) getStep(p, trackerPanel);
      if (step != null) {
        ImageCoordSystem coords = trackerPanel.getCoords();
        int n = trackerPanel.getFrameNumber();
        double x = coords.worldToImageXComponent(n,
                                                 xField.getValue(),
                                                 yField.getValue());
        double y = coords.worldToImageYComponent(n,
                                                 xField.getValue(),
                                                 yField.getValue());
        step.setXYComponents(x, y);
        x = coords.imageToWorldXComponent(n,
                                          step.getXComponent(),
                                          step.getYComponent());
        y = coords.imageToWorldYComponent(n,
                                          step.getXComponent(),
                                          step.getYComponent());
        xField.setValue(x);
        yField.setValue(y);
        magField.setValue(Math.sqrt(x*x + y*y));
        double theta = Math.atan2(y, x);
        angleField.setValue(theta);
        p.showCoordinates(trackerPanel);
      }
    }
  }

  /**
   * Sets the components of the currently selected vector based on the values
   * in the mag and angle fields.
   */
  private void setMagnitudeAngle() {
    double theta = angleField.getValue();
    double xval = magField.getValue() * Math.cos(theta);
    double yval = magField.getValue() * Math.sin(theta);
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel trackerPanel = it.next();
      TPoint p = trackerPanel.getSelectedPoint();
      VectorStep step = (VectorStep) getStep(p, trackerPanel);
      if (step != null) {
        ImageCoordSystem coords = trackerPanel.getCoords();
        int n = trackerPanel.getFrameNumber();
        double x = coords.worldToImageXComponent(n, xval, yval);
        double y = coords.worldToImageYComponent(n, xval, yval);
        step.setXYComponents(x, y);
        x = coords.imageToWorldXComponent(n,
                                          step.getXComponent(),
                                          step.getYComponent());
        y = coords.imageToWorldYComponent(n,
                                          step.getXComponent(),
                                          step.getYComponent());
        xField.setValue(x);
        yField.setValue(y);
        magField.setValue(Math.sqrt(x*x + y*y));
        theta = Math.atan2(y, x);
        angleField.setValue(theta);
        p.showCoordinates(trackerPanel);
      }
    }
  }

  /**
   * Inner class containing the vector data for a single frame number.
   */
  public static class FrameData {
    double x, y, xc, yc;
    boolean independent;
    FrameData() {/** empty block */}
    FrameData(VectorStep v, boolean dependent) {
      x = v.getTail().getX();
      y = v.getTail().getY();
      xc = v.getXComponent();
      yc = v.getYComponent();
    	independent = !dependent;
    }
  }

  /**
   * A class to save and load a FrameData.
   */
  private static class FrameDataLoader
      implements XML.ObjectLoader {

    public void saveObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      control.setValue("xtail", data.x); //$NON-NLS-1$
      control.setValue("ytail", data.y); //$NON-NLS-1$
      if (data.independent) {
	      control.setValue("xcomponent", data.xc); //$NON-NLS-1$
	      control.setValue("ycomponent", data.yc); //$NON-NLS-1$
      }
    }

    public Object createObject(XMLControl control) {
      return new FrameData();
    }

    public Object loadObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      double x = control.getDouble("xcomponent"); //$NON-NLS-1$
      if (!Double.isNaN(x)) {
	      data.xc = x;
	      data.yc = control.getDouble("ycomponent"); //$NON-NLS-1$
      }
      data.x = control.getDouble("xtail"); //$NON-NLS-1$
      data.y = control.getDouble("ytail"); //$NON-NLS-1$
      return obj;
    }
  }
}

