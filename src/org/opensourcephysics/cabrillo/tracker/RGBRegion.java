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

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

import javax.swing.*;
import javax.swing.border.Border;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

/**
 * A RGBRegion measures RGB properties in a circular region
 * of a video image.
 *
 * @author Douglas Brown
 */
public class RGBRegion extends TTrack {

  // static constants
	protected static final int MAX_RADIUS = 100;

  // static fields
  protected static int defaultRadius = 10;

  // instance fields
  protected boolean fixedPosition = true; // region has same position at all times
  protected boolean fixedRadius = true; // region has same radius at all times
  protected JCheckBoxMenuItem fixedPositionItem, fixedRadiusItem;
  protected JLabel radiusLabel;
  protected IntegerField radiusField;
  protected boolean firstTimeRadiusUnfixed = true;
  protected ArrayList<RGBStep> validSteps = new ArrayList<RGBStep>();
  protected boolean dataHidden = false;
  protected boolean loading;
  protected TreeSet<Integer> radiusKeyFrames = new TreeSet<Integer>();

  /**
   * Constructs a RGBRegion.
   */
  public RGBRegion() {
    super();
		defaultColors = new Color[] {Color.magenta};
    // assign a default name
    setName(TrackerRes.getString("RGBRegion.New.Name")); //$NON-NLS-1$
    // assign default plot variables
    setProperty("yVarPlot0", "luma"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yMinPlot0", new Double(0)); //$NON-NLS-1$
    setProperty("yMaxPlot0", new Double(255)); //$NON-NLS-1$
    // assign default table variables: x, y and luma
    setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("tableVar2", "5"); //$NON-NLS-1$ //$NON-NLS-2$
    // set up footprint choices and color
    setFootprints(new Footprint[]
       {PointShapeFootprint.getFootprint("Footprint.Circle"), //$NON-NLS-1$
      	PointShapeFootprint.getFootprint("Footprint.BoldCircle")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setColor(defaultColors[0]);
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("RGBRegion.Unmarked.Hint"); //$NON-NLS-1$
    // create toolbar components
    radiusLabel = new JLabel();
    radiusField = new IntegerField(2);
    radiusField.setMinValue(1);
    // radius focus listener
    final FocusListener radiusFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (radiusField.getBackground() == Color.yellow) {
      		setRadius(trackerPanel.getFrameNumber(), radiusField.getIntValue());
      	}
      }
    };
    radiusField.addFocusListener(radiusFocusListener);
    radiusField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	radiusFocusListener.focusLost(null);
        radiusField.selectAll();
        radiusField.requestFocusInWindow();
      }
    });
    fixedPositionItem = new JCheckBoxMenuItem(TrackerRes.getString("RGBRegion.MenuItem.Fixed")); //$NON-NLS-1$
    fixedPositionItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixedPosition(fixedPositionItem.isSelected());
      }
    });
    fixedRadiusItem = new JCheckBoxMenuItem(TrackerRes.getString("RGBRegion.MenuItem.FixedRadius")); //$NON-NLS-1$
    fixedRadiusItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixedRadius(fixedRadiusItem.isSelected());
      }
    });
    radiusField.setBorder(fieldBorder);
    // position action
    Action positionAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	NumberField field = (NumberField)e.getSource();
      	if (field.getBackground() == Color.yellow) setPositionFromFields();
        field.selectAll();
        field.requestFocusInWindow();
      }
    };
    // position focus listener
    FocusListener positionFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	NumberField field = (NumberField)e.getSource();
      	if (field.getBackground() == Color.yellow) setPositionFromFields();
      }
    };
    // add action and focus listeners
    xField.addActionListener(positionAction);
    yField.addActionListener(positionAction);
    xField.addFocusListener(positionFocusListener);
    yField.addFocusListener(positionFocusListener);
  }

  /**
   * Sets the fixed position property. When it is fixed, it is in the same
   * position at all times.
   *
   * @param fixed <code>true</code> to fix the position
   */
  public void setFixedPosition(boolean fixed) {
  	if (fixedPosition == fixed) return;
    if (steps.isEmpty()) {
      fixedPosition = fixed;
    	return;
    }
  	XMLControl control = new XMLControlElement(this);
    if (trackerPanel != null) {
    	trackerPanel.changed = true;
      int n = trackerPanel.getFrameNumber();
      RGBStep keyStep = (RGBStep)getStep(n);
      for (Step next: steps.array) {
      	if (next==null) continue;
      	RGBStep step = (RGBStep)next;
      	step.getPosition().setLocation(keyStep.getPosition());
      }
    }
    fixedPosition = fixed;
    if (fixed) {
  		keyFrames.clear();
	    keyFrames.add(0);
  		clearData();
  		refreshData(data, trackerPanel);
  		support.firePropertyChange("data", null, null); //$NON-NLS-1$
    }
    if (!loading) {
    	Undo.postTrackEdit(this, control);
    }
    repaint();
  }

  /**
   * Gets the fixed position property.
   *
   * @return <code>true</code> if image position is fixed
   */
  public boolean isFixedPosition() {
    return fixedPosition;
  }

  /**
   * Sets the fixed radius property. When fixed, it has the same
   * radius at all times.
   *
   * @param fixed <code>true</code> to fix the radius
   */
  public void setFixedRadius(boolean fixed) {
  	if (fixedRadius == fixed) return;
    if (steps.isEmpty()) {
    	fixedRadius = fixed;
    	return;
    }
  	XMLControl control = new XMLControlElement(this);
    if (trackerPanel != null) {
    	trackerPanel.changed = true;
      int n = trackerPanel.getFrameNumber();
      RGBStep keyStep = (RGBStep)getStep(n);
      for (Step next: steps.array) {
      	if (next==null) continue;
      	RGBStep step = (RGBStep)next;
      	step.setRadius(keyStep.radius);
      }
    }
  	fixedRadius = fixed;
    if (fixed) {
	    radiusKeyFrames.clear();
	    radiusKeyFrames.add(0);
  		clearData();
  		refreshData(data, trackerPanel);
  		support.firePropertyChange("data", null, null); //$NON-NLS-1$
    }
    if (!loading) {
    	Undo.postTrackEdit(this, control);
    }
    repaint();
  }

  /**
   * Gets the fixed radius property.
   *
   * @return <code>true</code> if radius is fixed
   */
  public boolean isFixedRadius() {
    return fixedRadius;
  }

  /**
   * Sets the radius of a step and posts an undoable edit
   *
   * @param n the frame number
   * @param r the desired radius
   */
  protected void setRadius(int n, int r) {
    if (isLocked() || r == Integer.MIN_VALUE || trackerPanel == null) return;
    r = Math.max(r, 0);
    r = Math.min(r, MAX_RADIUS);
    radiusField.setIntValue(r);
    
    RGBStep step = (RGBStep)getStep(n); // target step
    RGBStep keyStep = step; // key step is target if radius not fixed
    if (step!=null && step.radius != r) {
      // deselect selected point to trigger possible undo, then reselect it
      TPoint selection = trackerPanel.getSelectedPoint();
      trackerPanel.setSelectedPoint(null);
    	XMLControl state = new XMLControlElement(step);
    	
      if (isFixedRadius()) {
      	keyStep = (RGBStep)steps.getStep(0); // key step is step 0
    		clearData(); // all data is invalid
        keyStep.setRadius(r);
        refreshStep(step);
      }
      else {
      	radiusKeyFrames.add(n); // step is both target and key
        step.setRadius(r);
        step.dataValid = false; // only target step's data is invalid      
    	}      
    	Undo.postStepEdit(step, state);
      trackerPanel.setSelectedPoint(selection);
  		refreshData(data, trackerPanel);
      step.repaint();
      firePropertyChange("data", null, RGBRegion.this); // to views //$NON-NLS-1$
    }     
  }

  /**
   * Gets the radius.
   *
   * @return the radius
   */
  public int getRadius() {
  	if (isFixedRadius()) {
	    RGBStep step = (RGBStep)getStep(0);
	    if (step != null) return step.radius;
  	}
  	else if (trackerPanel != null && !fixedRadius) {
	    int n = trackerPanel.getFrameNumber();
	    RGBStep step = (RGBStep)getStep(n);
	    if (step != null) return step.radius;
    }
    return defaultRadius;
  }

  /**
   * Overrides TTrack draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
  	if (isMarking && !(trackerPanel.getSelectedPoint() instanceof RGBStep.Position))
  		return;
  	super.draw(panel, _g);
  }

  /**
   * Overrides TTrack findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step or motion vector that is hit
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	Interactive ia = super.findInteractive(panel, xpix, ypix);
    if (ia != null) {
    	partName = TrackerRes.getString("RGBRegion.Position.Name"); //$NON-NLS-1$
      hint = TrackerRes.getString("RGBRegion.Position.Hint"); //$NON-NLS-1$
    }
    else {
    	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
      if (getStep(trackerPanel.getFrameNumber())==null) {
      	hint = TrackerRes.getString("RGBRegion.Unmarked.Hint"); //$NON-NLS-1$
      }
      else hint = TrackerRes.getString("RGBRegion.Hint"); //$NON-NLS-1$
      if (trackerPanel.getVideo() == null) {
      	hint += ", "+TrackerRes.getString("TTrack.ImportVideo.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  	return ia;
  }
  
  /**
   * Overrides TTrack setTrailVisible method to keep trails hidden.
   *
   * @param visible ignored
   */
  public void setTrailVisible(boolean visible) {/** empty block */}

  /**
   * Gets the autoAdvance property. Overrides TTrack method.
   *
   * @return <code>false</code>
   */
  public boolean isAutoAdvance() {
    return !isFixedPosition();
  }

  /**
   * Creates a new step.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the step
   */
  public Step createStep(int n, double x, double y) {
    if (isLocked()) return null;
  	int frame = isFixedPosition()? 0: n;
    RGBStep step = (RGBStep)steps.getStep(frame);
    if (step==null) { // create new step 0 and autofill array
    	int r = (int)radiusField.getValue();
    	step = new RGBStep(this, 0, x, y, r);
      step.setFootprint(getFootprint());
      steps = new StepArray(step);
      keyFrames.add(0);
      radiusKeyFrames.add(0);
    }
    else {
    	XMLControl currentState = new XMLControlElement(this);
      step.getPosition().setLocation(x, y);
      keyFrames.add(n);
  		Undo.postTrackEdit(this, currentState);
    }
    support.firePropertyChange("step", null, n); //$NON-NLS-1$
    return getStep(n);
  }

  /**
   * Overrides TTrack deleteStep method to prevent deletion.
   *
   * @param n the frame number
   * @return the deleted step
   */
  public Step deleteStep(int n) {
    return null;
  }

  /**
   * Overrides TTrack getStep method to provide fixed behavior.
   *
   * @param n the frame number
   * @return the step
   */
  public Step getStep(int n) {
    RGBStep step = (RGBStep)steps.getStep(n);
		refreshStep(step);
    return step;
  }

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return RGBStep.getLength();
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
  	this.setFixedPosition(false);
  	return super.autoMarkAt(n, x, y);
  }
  
  /**
   * Determines if any point in this track is autotrackable.
   *
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable() {
  	return true;
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
   * Clears the data.
   */
  protected void clearData() {
  	if (data == null) return;
  	// clear each dataset
    for (int i = 0; i < 7; i++) {
    	Dataset next = data.getDataset(i);
    	next.clear();
    }
    Step[] steps = getSteps();
    for (int i = 0; i < steps.length; i++) {
    	if (steps[i] == null) continue;
    	steps[i].dataVisible = false;
    	((RGBStep)steps[i]).dataValid = false;
    }
  }
  	
  /**
   * Hides the data.
   */
  protected void hideData() {
    Step[] steps = getSteps();
    for (int i = 0; i < steps.length; i++) {
    	if (steps[i] == null) continue;
    	steps[i].dataVisible = false;
    }
    dataHidden = true;
  }
  	
  /**
   * Refreshes the data.
   *
   * @param data the DatasetManager
   * @param trackerPanel the tracker panel
   */
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
    if (trackerPanel == null || data == null) return;
    dataFrames.clear();
  	// get valid step at current frameNumber
    int frame = trackerPanel.getFrameNumber();
    Step step = getStep(frame);
    if (step != null) {
    	((RGBStep)step).getRGBData(trackerPanel);
    }
    // get the datasets
    int count = 0;
    Dataset x = data.getDataset(count++);
    Dataset y = data.getDataset(count++);
    Dataset r = data.getDataset(count++);
    Dataset g = data.getDataset(count++);
    Dataset b = data.getDataset(count++);
    Dataset luma = data.getDataset(count++);
    Dataset pixels = data.getDataset(count++);
    Dataset stepNum = data.getDataset(count++);
    Dataset frameNum = data.getDataset(count++);
    // assign column names to the datasets
    String time = "t"; //$NON-NLS-1$
    if (!x.getColumnName(0).equals(time)) { // not yet initialized
	    x.setXYColumnNames(time, "x"); //$NON-NLS-1$
	    y.setXYColumnNames(time, "y"); //$NON-NLS-1$
	    r.setXYColumnNames(time, "R"); //$NON-NLS-1$
	    g.setXYColumnNames(time, "G"); //$NON-NLS-1$
	    b.setXYColumnNames(time, "B"); //$NON-NLS-1$
	    luma.setXYColumnNames(time, "luma"); //$NON-NLS-1$
	    pixels.setXYColumnNames(time, "pixels"); //$NON-NLS-1$
	    stepNum.setXYColumnNames(time, "step"); //$NON-NLS-1$
	    frameNum.setXYColumnNames(time, "frame"); //$NON-NLS-1$
    }
    else for (int i = 0; i < count; i++) {
    	data.getDataset(i).clear();
    }
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < dataDescriptions.length; i++) {
      dataDescriptions[i] = TrackerRes.getString("RGBRegion.Data.Description."+i); //$NON-NLS-1$
    }
    // look thru steps and find valid ones (data valid and included in clip)
    Step[] stepArray = getSteps();
    validSteps.clear();
    VideoPlayer player = trackerPanel.getPlayer();
    VideoClip clip = player.getVideoClip();
    for (int n = 0; n < stepArray.length; n++) {
    	RGBStep next = (RGBStep)stepArray[n];
      if (next == null || !next.dataValid
      			|| next.getRGBData(trackerPanel) == null) 
      	continue;
      // get the frame number of the step
      TPoint p = next.getPosition();
      int stepFrame = p.getFrameNumber(trackerPanel);
      // step is valid if frame is included in the clip
      if (clip.includesFrame(stepFrame)) {
      	validSteps.add(next);
      }
      else next.dataVisible = false;
    }
    RGBStep[] valid = validSteps.toArray(new RGBStep[0]);
	  int len = valid.length;
	  // get the valid data
	  double[][] validData = new double[10][len];
	  for (int n = 0; n < len; n++) {
	    // get the rgb data for the step
	    double[] rgb = valid[n].getRGBData(trackerPanel);	
	    // get the frame number of the step
	    TPoint p = valid[n].getPosition();
	    int stepFrame = p.getFrameNumber(trackerPanel);
	    dataFrames.add(new Integer(stepFrame));
	    // get the step number and time
	    int stepNumber = clip.frameToStep(stepFrame);
	    double t = player.getStepTime(stepNumber)/1000.0;
	    // get the world position for the step
	    Point2D pt = p.getWorldPosition(trackerPanel);
	    // put data in validData array
		  for (int j = 2; j < 7; j++) {
				validData[j][n] = rgb[j-2];
		  }
			validData[0][n] = pt.getX();
			validData[1][n] = pt.getY();
			validData[7][n] = stepNumber;
			validData[8][n] = stepFrame;
			validData[9][n] = t;
	  }
    // append the data to the data set
    x.append(validData[9], validData[0]);
    y.append(validData[9], validData[1]);
    r.append(validData[9], validData[2]);
    g.append(validData[9], validData[3]);
    b.append(validData[9], validData[4]);
    luma.append(validData[9], validData[5]);
    pixels.append(validData[9], validData[6]);
    stepNum.append(validData[9], validData[7]);
    frameNum.append(validData[9], validData[8]);
  }

  /**
   * Overrides TTrack getMenu method.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
    fixedPositionItem.setText(TrackerRes.getString("RGBRegion.MenuItem.Fixed")); //$NON-NLS-1$
    fixedPositionItem.setSelected(isFixedPosition());
    fixedRadiusItem.setText(TrackerRes.getString("RGBRegion.MenuItem.FixedRadius")); //$NON-NLS-1$
    fixedRadiusItem.setSelected(isFixedRadius());
    menu.remove(deleteTrackItem);
    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
      menu.addSeparator();
    menu.add(fixedPositionItem);
    menu.add(fixedRadiusItem);
    // replace delete item
    if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
      menu.add(deleteTrackItem);
    }
    return menu;
  }

  /**
   * Overrides TTrack getToolbarTrackComponents method.
   *
   * @param trackerPanel the tracker panel
   * @return a list of components
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
    ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    radiusLabel.setText(TrackerRes.getString("RGBRegion.Label.Radius")); //$NON-NLS-1$
    Border empty = BorderFactory.createEmptyBorder(0, 4, 0, 2);
    radiusLabel.setBorder(empty);
    list.add(radiusLabel);
    radiusField.setIntValue(getRadius());
    radiusField.setEnabled(!isLocked());
    list.add(radiusField);
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
    Step step = getStep(point, trackerPanel);
    ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
    if (step == null) return list;
    int n = step.getFrameNumber();
    n = trackerPanel.getPlayer().getVideoClip().frameToStep(n);
    stepValueLabel.setText("" + n); //$NON-NLS-1$
    xField.setEnabled(!isLocked());
    yField.setEnabled(!isLocked());
    list.add(stepSeparator);
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
    return list;
  }

  @Override
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	Object[] objectsToSize = new Object[]
  			{radiusLabel};
    FontSizer.setFonts(objectsToSize, level);
  }

  /**
   * Responds to property change events. This listens for the following
   * events: "stepnumber" & "image" from TrackerPanel.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
  	if (trackerPanel != null) {
      String name = e.getPropertyName();
      if (name.equals("stepnumber")) { //$NON-NLS-1$
      	dataValid = false;
  	    int n = trackerPanel.getFrameNumber();
  	    RGBStep step = (RGBStep)getStep(n);
  	    if (step != null) radiusField.setIntValue(step.radius);
  	    radiusField.setEnabled(!isLocked() && step != null);
        support.firePropertyChange(e); // to views
      }
      else if (name.equals("image")) { //$NON-NLS-1$
      	dataValid = false;
      	Video vid = trackerPanel.getVideo();
      	if (vid == null) clearData(); // no video
      	else if (!vid.isVisible()) // video invisible
      		hideData();
      	else if (!dataHidden && vid.isVisible()) // video filters
      		clearData();
      	else dataHidden = false;
        support.firePropertyChange(e); // to views
      }
  	}
    super.propertyChange(e); // handled by TTrack
  }

  /**
   * Overrides Object toString method.
   *
   * @return the name of this track
   */
  public String toString() {
    return TrackerRes.getString("RGBRegion.Name"); //$NON-NLS-1$
  }

//__________________________ private methods ___________________________

  /**
   * Sets the position of the currently selected point based on the values
   * in the x and y fields.
   */
  private void setPositionFromFields() {
    double xValue = xField.getValue();
    double yValue = yField.getValue();
    TPoint p = trackerPanel.getSelectedPoint();
    int n = trackerPanel.getFrameNumber();
    Step step = getStep(n);
    if (step != null && p != null) {
      ImageCoordSystem coords = trackerPanel.getCoords();
      double x = coords.worldToImageX(n, xValue, yValue);
      double y = coords.worldToImageY(n, xValue, yValue);
      p.setXY(x, y);
      Point2D worldPt = p.getWorldPosition(trackerPanel);
      xField.setValue(worldPt.getX());
      yField.setValue(worldPt.getY());
    }
  }

  /**
   * Refreshes a step by setting it equal to a keyframe step.
   *
   * @param step the step to refresh
   */
  protected void refreshStep(RGBStep step) {
  	if (step==null)
  		return;
  	// find key steps
  	int key = 0;
  	if (!isFixedPosition()) {
	  	for (int i: keyFrames) {
	  		if (i<=step.n)
	  			key = i;
	  	}
  	}
  	int radiusKey = 0;
  	if (!isFixedRadius()) {
	  	for (int i: radiusKeyFrames) {
	  		if (i<=step.n)
	  			radiusKey = i;
	  	}
  	}
  	// compare step with keySteps and update if needed
  	RGBStep positionKeyStep = (RGBStep)steps.getStep(key);
  	double x = positionKeyStep.getPosition().getX();
    double y = positionKeyStep.getPosition().getY();
    boolean differentPosition = 
    			 x!=step.getPosition().getX() 
    		|| y!=step.getPosition().getY();
    if (differentPosition) {
	    step.getPosition().setLocation(x, y);
	    step.erase();
	    step.dataValid = false;
    }
  	RGBStep radiusKeyStep = (RGBStep)steps.getStep(radiusKey);
    int r = radiusKeyStep.radius;
    if (r!=step.radius) {
	    step.setRadius(r);
	    step.erase();
	    step.dataValid = false;
    }
  }

//__________________________ static methods ___________________________

  /**
   * Returns the luma (perceived brightness) of a video RGB color.
   *
   * @param r red component
   * @param g green component
   * @param b blue component
   * @return the video luma
   */
  public static double getLuma(double r, double g, double b) {
    // following code based on CCIR 601 specs
    return 0.299*r + 0.587*g + 0.114*b;
  }

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
    	RGBRegion region = (RGBRegion) obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save fixed position
    	control.setValue("fixed", region.isFixedPosition()); //$NON-NLS-1$
      // save fixed radius
    	control.setValue("fixed_radius", region.isFixedRadius()); //$NON-NLS-1$
      // save step data, if any
    	if (!region.steps.isEmpty()) {
	      Step[] steps = region.getSteps();
	      int count = region.isFixedPosition()? 1: steps.length;
	      FrameData[] data = new FrameData[count];
	      for (int n = 0; n < count; n++) {
	      	// save only position key frames
	        if (steps[n] == null || !region.keyFrames.contains(n)) continue;
	        data[n] = new FrameData((RGBStep)steps[n]);
	      }
	      control.setValue("framedata", data); //$NON-NLS-1$   
	    	// save radius
	      count = region.isFixedRadius()? 1: steps.length;
	      Integer[] radii = new Integer[count];
	      for (int n = 0; n < count; n++) {
	      	// save only radius key frames
	        if (steps[n] == null || !region.radiusKeyFrames.contains(n)) continue;
	        radii[n] = ((RGBStep)steps[n]).radius;
	      }
	      control.setValue("radii", radii); //$NON-NLS-1$
    	}
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
    	RGBRegion region = new RGBRegion();
      return region;
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	RGBRegion region = (RGBRegion) obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      boolean locked = region.isLocked();
      region.setLocked(false);
      region.loading = true;
      // load fixed position
      region.fixedPosition = control.getBoolean("fixed"); //$NON-NLS-1$
      // load fixed radius
      region.fixedRadius = control.getBoolean("fixed_radius"); //$NON-NLS-1$
      // load step data
      region.keyFrames.clear();
      region.radiusKeyFrames.clear();
      Object dataObj = control.getObject("framedata"); //$NON-NLS-1$
      FrameData[] data = null;
      if (dataObj instanceof FrameData) { // legacy
      	data = new FrameData[] {(FrameData)dataObj};
      }
      else { // dataObj instanceof FrameData[]
      	data = (FrameData[])dataObj;
      }
      if (data!=null) {
	      for (int n = 0; n < data.length; n++) {
	        if (data[n] == null) continue;
	        RGBStep step = (RGBStep)region.createStep(n, data[n].x, data[n].y);
	        if (data[n].r != Integer.MIN_VALUE) {
	        	step.radius = data[n].r;
	          region.radiusKeyFrames.add(n); // for legacy compatibility
	        }
	      }
      }
      Integer[] radii = (Integer[])control.getObject("radii"); //$NON-NLS-1$
      if (radii!=null) {
        region.radiusKeyFrames.clear();
	      for (int n = 0; n < radii.length; n++) {
	        if (radii[n] == null) continue;
	        RGBStep step = (RGBStep)region.steps.getStep(n);
	        step.radius = radii[n];
	        region.radiusKeyFrames.add(n);
	      }
      }
      region.setLocked(locked);
      region.loading = false;
      region.repaint();
      return obj;
    }
  }

  /**
   * Inner class containing the rgb data for a single frame number.
   */
  private static class FrameData {
    double x, y;
    int r;
    
    FrameData() {/** empty block */}
    
    FrameData(RGBStep s) {
      x = s.getPosition().getX();
      y = s.getPosition().getY();
      r = s.radius;
    }
  }

  /**
   * A class to save and load a FrameData.
   */
  private static class FrameDataLoader
      implements XML.ObjectLoader {

    public void saveObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      control.setValue("x", data.x); //$NON-NLS-1$
      control.setValue("y", data.y); //$NON-NLS-1$
      control.setValue("r", data.r); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new FrameData();
    }

    public Object loadObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      data.x = control.getDouble("x"); //$NON-NLS-1$
      data.y = control.getDouble("y"); //$NON-NLS-1$
      data.r = control.getInt("r"); //$NON-NLS-1$
      return obj;
    }
  }
}

