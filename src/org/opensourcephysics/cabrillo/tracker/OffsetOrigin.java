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
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * An OffsetOrigin controls the origin of an image coordinate system.
 *
 * @author Douglas Brown
 */
public class OffsetOrigin extends TTrack {

  // instance fields
  private Component separator;
  protected boolean fixedCoordinates = true;
  protected JCheckBoxMenuItem fixedCoordinatesItem;
  protected JLabel unmarkedLabel;

  /**
   * Constructs an OffsetOrigin.
   */
  public OffsetOrigin() {
		defaultColors = new Color[] {Color.cyan, Color.magenta, Color.yellow.darker()};
    // set up footprint choices and color
    setFootprints(new Footprint[]
        {PointShapeFootprint.getFootprint("Footprint.BoldCrosshair"), //$NON-NLS-1$
         PointShapeFootprint.getFootprint("Footprint.Crosshair")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    // assign a default name
    setName(TrackerRes.getString("OffsetOrigin.New.Name")); //$NON-NLS-1$ 
    setColor(defaultColors[0]);
    // hide this track from views
    viewable = false;
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("OffsetOrigin.Unmarked.Hint"); //$NON-NLS-1$
  	keyFrames.add(0);
    createGUI();
  }

  /**
   * Creates a new offset origin step with specified image coordinates.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the new step
   */
  public Step createStep(int n, double x, double y) {
    if (isLocked()) return null;
    OffsetOriginStep step = (OffsetOriginStep)getStep(n);
    if (step == null) {
      step = new OffsetOriginStep(this, n, x, y);
      step.setFootprint(getFootprint());
      steps = new StepArray(step);
      support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
    }
    else if (trackerPanel!=null) {
    	XMLControl currentState = new XMLControlElement(this);
  		TPoint p = step.getPosition();
    	p.setLocation(x, y);
			Point2D pt = p.getWorldPosition(trackerPanel);
			step.worldX = pt.getX();
			step.worldY = pt.getY();
			keyFrames.add(n);
  		Undo.postTrackEdit(this, currentState);
    }
    return step;
  }

  /**
   * Used by autoTracker to mark a step at a match target position. 
   * 
   * @param n the frame number
   * @param x the x target coordinate in image space
   * @param y the y target coordinate in image space
   * @return the TPoint that was automarked
   */
  public TPoint autoMarkAt(int n, double x, double y) {
    OffsetOriginStep step = (OffsetOriginStep)getStep(n);
  	// be sure coords have unfixed origin
    ImageCoordSystem coords = trackerPanel.getCoords();
  	coords.setFixedOrigin(false);
    if (step == null) {
	  	step = (OffsetOriginStep)createStep(n, x, y);
	  	if (step!=null) {
	  		return step.getPoints()[0];
	  	}
    }
    else {
	    TPoint p = step.getPoints()[0];
	    if (p!=null) {
	      Mark mark = step.marks.get(trackerPanel);
	      if (mark==null) {
		      // set step location to image position of current world coordinates
		      double xx = coords.worldToImageX(n, step.worldX, step.worldY);
		      double yy = coords.worldToImageY(n, step.worldX, step.worldY);
		      p.setLocation(xx, yy);
	      }
	    	p.setAdjusting(true);
	    	p.setXY(x, y);
	    	p.setAdjusting(false);
	    	firePropertyChange("step", null, n); //$NON-NLS-1$
	    	return p;
	    }
    }
  	return null;
  }
  
  /**
   * Determines if at least one point in this track is autotrackable.
   *
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable() {
  	return true;
  }
  
  /**
   * Determines if the world coordinates are fixed.
   *
   * @return <code>true</code> if fixed
   */
  public boolean isFixedCoordinates() {
    return fixedCoordinates;
  }

  /**
   * Sets the fixed coordinates property. When fixed, the world coordinates 
   * are the same at all times.
   *
   * @param fixed <code>true</code> to fix the coordinates
   */
  public void setFixedCoordinates(boolean fixed) {
  	if (fixedCoordinates == fixed) return;
  	XMLControl control = new XMLControlElement(this);
    if (trackerPanel != null) {
    	trackerPanel.changed = true;
      int n = trackerPanel.getFrameNumber();
      steps = new StepArray(getStep(n));
      trackerPanel.repaint();
    }
    if (fixed) {
    	keyFrames.clear();
    	keyFrames.add(0);
    }
    fixedCoordinates = fixed;
    Undo.postTrackEdit(this, control);
  }

  /**
   * Overrides TTrack getStep method. This refreshes the step before
   * returning it since its position and/or world coordinates may change 
   * due to external factors.
   *
   * @param n the frame number
   * @return the step
   */
  public Step getStep(int n) {
    OffsetOriginStep step = (OffsetOriginStep)steps.getStep(n);
    refreshStep(step);
    return step;
  }

  /**
   * Overrides TTrack isLocked method. Returns true if this is locked or
   * if the coordinate system is locked.
   *
   * @return <code>true</code> if this is locked
   */
  public boolean isLocked() {
    boolean locked = super.isLocked();
    if (trackerPanel != null) {
      locked = locked || trackerPanel.getCoords().isLocked();
    }
    return locked;
  }

  /**
   * Overrides TTrack setTrailVisible method.
   * Offset origin trails are never visible.
   *
   * @param visible ignored
   */
  public void setTrailVisible(boolean visible) {/** empty block */}

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return OffsetOriginStep.getLength();
  }

  /**
   * Gets the length of the footprints required by this track.
   *
   * @return the footprint length
   */
  public int getFootprintLength() {
    return 1;
  }

  /**
   * Overrides TTrack findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the current step or null
   */
  public Interactive findInteractive(
      DrawingPanel panel, int xpix, int ypix) {
    if (!(panel instanceof TrackerPanel) ||
        !isVisible() ||
        !isEnabled()) return null;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Interactive ia = null;
    Step step = getStep(trackerPanel.getFrameNumber());
    if (step != null)
      ia = step.findInteractive(trackerPanel, xpix, ypix);
    if (ia != null) {
    	partName = TrackerRes.getString("OffsetOrigin.Position.Name"); //$NON-NLS-1$
    	hint = TrackerRes.getString("OffsetOrigin.Position.Hint"); //$NON-NLS-1$
    }
    else {
    	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
      hint = TrackerRes.getString("OffsetOrigin.Unmarked.Hint"); //$NON-NLS-1$
    }
    return ia;
  }

  /**
   * Overrides TTrack getMenu method.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
    lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());
    // remove end items and last separator
    menu.remove(deleteTrackItem);
    menu.remove(menu.getMenuComponent(menu.getMenuComponentCount()-1));
    
    // add fixed and delete items
    fixedCoordinatesItem.setText(TrackerRes.getString("OffsetOrigin.MenuItem.Fixed")); //$NON-NLS-1$
    fixedCoordinatesItem.setSelected(isFixedCoordinates());
  	menu.add(fixedCoordinatesItem);
  	menu.addSeparator();
    menu.add(deleteTrackItem);
    return menu;
  }

//  /**
//   * Overrides TTrack getToolbarPointComponents method.
//   *
//   * @param trackerPanel the tracker panel
//   * @param point the TPoint
//   * @return a list of components
//   */
//  public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel,
//                                             TPoint point) {
//    ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
//    list.add(stepSeparator);
//    list.add(xLabel);
//    list.add(xField);
//    list.add(separator);
//    list.add(yLabel);
//    list.add(yField);
//    boolean locked = trackerPanel.getCoords().isLocked() || super.isLocked();
//    xField.setEnabled(!locked);
//    yField.setEnabled(!locked);
//    return list;
//  }

  /**
   * Overrides TTrack method.
   *
   * @param trackerPanel the tracker panel
   * @return a list of components
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
  	ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    list.add(stepSeparator);
    unmarkedLabel.setText(TrackerRes.getString("TTrack.Label.Unmarked")); //$NON-NLS-1$
	  int n = trackerPanel.getFrameNumber();
    Step step = getStep(n);
    
    if (step==null) {
	    list.add(unmarkedLabel);
    }
    else {
	    list.add(xLabel);
	    list.add(xField);
	    list.add(separator);
	    list.add(yLabel);
	    list.add(yField);
    }
    
    boolean locked = trackerPanel.getCoords().isLocked() || super.isLocked();
    xField.setEnabled(!locked);
    yField.setEnabled(!locked);
    displayWorldCoordinates();
    return list;
  }

  /**
   * Responds to property change events. Overrides TTrack method.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("stepnumber")) { //$NON-NLS-1$
      if (trackerPanel.getSelectedTrack() == this) {
      	displayWorldCoordinates();
      }
    }
    else if (name.equals("locked")) { //$NON-NLS-1$
      xField.setEnabled(!isLocked());
      yField.setEnabled(!isLocked());
    }
    else super.propertyChange(e);
  }

  /**
   * Overrides TTrack method.
   *
   * @param locked <code>true</code> to lock this
   */
  public void setLocked(boolean locked) {
    super.setLocked(locked);
    xField.setEnabled(!isLocked());
    yField.setEnabled(!isLocked());
  }

  /**
   * Overrides Object toString method.
   *
   * @return the name of this track
   */
  public String toString() {
    return TrackerRes.getString("OffsetOrigin.Name"); //$NON-NLS-1$
  }

  /**
   * Returns a description of the point at a given index. Used by AutoTracker.
   *
   * @param pointIndex the points[] index
   * @return the description
   */
  protected String getTargetDescription(int pointIndex) {
  	return TrackerRes.getString("OffsetOrigin.Position.Name"); //$NON-NLS-1$
  }

  /**
   * Refreshes a step by setting it equal to the previous keyframe step.
   *
   * @param step the step to refresh
   */
  protected void refreshStep(OffsetOriginStep step) {
  	if (step==null) return;
  	int key = 0;
  	for (int i: keyFrames) {
  		if (i<=step.n)
  			key = i;
  	}
  	// compare step with keyStep
  	OffsetOriginStep keyStep = (OffsetOriginStep)steps.getStep(key);
    boolean different = keyStep.worldX!=step.worldX || keyStep.worldY!=step.worldY;
    // update step if needed
    if (different) {
      step.worldX = keyStep.worldX;
      step.worldY = keyStep.worldY;
    }
    step.erase();
  }

  /**
   * Sets the world values of the currently selected point based on the values
   * in the x and y fields.
   */
  private void setWorldCoordinatesFromFields() {
    if (trackerPanel == null) return;
    OffsetOriginStep step = (OffsetOriginStep)getStep(trackerPanel.getFrameNumber());
    boolean different = step.worldX!=xField.getValue() || step.worldY!=yField.getValue();
    if (different) {
    	XMLControl trackControl = new XMLControlElement(this);
    	XMLControl coordsControl = new XMLControlElement(trackerPanel.getCoords());
	    step.setWorldXY(xField.getValue(), yField.getValue());
	    step.getPosition().showCoordinates(trackerPanel);
    	Undo.postTrackAndCoordsEdit(this, trackControl, coordsControl);
    }
  }

  /**
   * Displays the world coordinates of the currently selected step.
   */
  private void displayWorldCoordinates() {
    int n = trackerPanel==null? 0: trackerPanel.getFrameNumber();
    OffsetOriginStep step = (OffsetOriginStep)getStep(n);
    if (step==null) {
	    xField.setText(null);
	    yField.setText(null);
    }
    else {
	    xField.setValue(step.worldX);
	    yField.setValue(step.worldY);
    }
  }
  
  /**
   * Creates the GUI.
   */
  private void createGUI() {
  	unmarkedLabel = new JLabel();
  	unmarkedLabel.setForeground(Color.red.darker());
    fixedCoordinatesItem = new JCheckBoxMenuItem(TrackerRes.getString("OffsetOrigin.MenuItem.Fixed")); //$NON-NLS-1$
    fixedCoordinatesItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixedCoordinates(fixedCoordinatesItem.isSelected());
      }
    });
    // create xy ActionListener and FocusListener
    ActionListener xyAction = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setWorldCoordinatesFromFields();
        ( (NumberField) e.getSource()).requestFocusInWindow();
      }
    };
    FocusListener xyFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setWorldCoordinatesFromFields();
      }
    };
    xField.addActionListener(xyAction);
    xField.addFocusListener(xyFocusListener);
    yField.addActionListener(xyAction);
    yField.addFocusListener(xyFocusListener);
    separator = Box.createRigidArea(new Dimension(4, 4));
  }

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
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
      OffsetOrigin offset = (OffsetOrigin) obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save fixed coordinates
      control.setValue("fixed_coordinates", offset.isFixedCoordinates()); //$NON-NLS-1$
      // save world coordinates
      if (!offset.steps.isEmpty()) {
	      Step[] steps = offset.getSteps();
	      double[][] stepData = new double[steps.length][];
	      for (int i = 0; i<steps.length; i++) {
	      	// save only key frames
	        if (steps[i] == null || !offset.keyFrames.contains(i)) continue;
	      	OffsetOriginStep step = (OffsetOriginStep)steps[i];
	      	stepData[i] = new double[] {step.worldX, step.worldY};
	      	if (!control.getPropertyNames().contains("worldX")) { //$NON-NLS-1$
	      		// include these for backward compatibility
	  	      control.setValue("worldX", step.worldX); //$NON-NLS-1$
	  	      control.setValue("worldY", step.worldY); //$NON-NLS-1$
	      	}
	      }
	      control.setValue("world_coordinates", stepData); //$NON-NLS-1$
      }
    }

    /**
     * Creates a new object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new OffsetOrigin();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      OffsetOrigin offset = (OffsetOrigin) obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      boolean locked = offset.isLocked();
      offset.setLocked(false);
      // load fixed coordinates
      if (control.getPropertyNames().contains("fixed_coordinates")) //$NON-NLS-1$
      	offset.fixedCoordinates = control.getBoolean("fixed_coordinates"); //$NON-NLS-1$
      offset.keyFrames.clear();
      // create step array if needed
      if (offset.steps.isEmpty())
      	offset.createStep(0, 0, 0);
      offset.keyFrames.clear();
      // load world coordinates
      double[][] stepData = (double[][])control.getObject("world_coordinates"); //$NON-NLS-1$
      if (stepData!=null) {
      	for (int i = 0; i<stepData.length; i++) {
      		if (stepData[i]!=null) {
      			OffsetOriginStep step = (OffsetOriginStep)offset.getStep(i);
      			step.worldX = stepData[i][0];
      			step.worldY = stepData[i][1];
          	offset.keyFrames.add(i);
      		}
      	}
      }
      else { // load legacy files
  			OffsetOriginStep step = (OffsetOriginStep)offset.getStep(0);
  			step.worldX = control.getDouble("worldX"); //$NON-NLS-1$
  			step.worldY = control.getDouble("worldY"); //$NON-NLS-1$
      	offset.keyFrames.add(0);
      }
      offset.setLocked(locked);
      offset.displayWorldCoordinates();
      return obj;
    }
  }
}
