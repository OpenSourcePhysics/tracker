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

import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.cabrillo.tracker.CompassStep.DataPoint;
import org.opensourcephysics.cabrillo.tracker.CompassStep.Slider;
import org.opensourcephysics.controls.*;

/**
 * A Compass measures and displays circles and circular arcs.
 *
 * @author Douglas Brown
 */
public class Compass extends TTrack {
	// pig undoable edits
	
  // instance fields
  protected boolean fixedPosition=true, radialLineVisible=false, radialLineEnabled=false;
  protected JCheckBoxMenuItem fixedItem;
  protected JCheckBox radialLineCheckbox;
  protected JLabel clickToMarkLabel;
  protected JLabel xDataPointLabel, yDataPointLabel;
  protected NumberField xDataField, yDataField;
  protected Component xDataPointSeparator, yDataPointSeparator, checkboxSeparator;

  /**
   * Constructs a Compass.
   */
  public Compass() {
		defaultColors = new Color[] {new Color(0, 140, 40)};
    // assign a default name
    setName(TrackerRes.getString("Compass.New.Name")); //$NON-NLS-1$
    // set up footprint choices and color
    setFootprints(new Footprint[]
        {CompassFootprint.getFootprint("CompassFootprint.Circle4"), //$NON-NLS-1$
        CompassFootprint.getFootprint("CompassFootprint.Circle7"), //$NON-NLS-1$
        CompassFootprint.getFootprint("CompassFootprint.Circle4Bold"), //$NON-NLS-1$
        CompassFootprint.getFootprint("CompassFootprint.Circle7Bold")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setColor(defaultColors[0]);
    
    // assign default table variables
    setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("tableVar2", "2"); //$NON-NLS-1$ //$NON-NLS-2$
    // assign default plot variables
  	String center = TrackerRes.getString("Compass.Data.Center"); //$NON-NLS-1$
    setProperty("xVarPlot0", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot0", "x"+center); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("xVarPlot1", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot1", "y"+center); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("xVarPlot2", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot2", "r"); //$NON-NLS-1$ //$NON-NLS-2$
   
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("Compass.Hint.Mark3"); //$NON-NLS-1$
    // initialize the autofill step array
    CompassStep step = new CompassStep(this, 0);
    step.setFootprint(getFootprint());
    steps = new StepArray(step); // autofills
    fixedItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
    fixedItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixed(fixedItem.isSelected());
      }
    });
    
    clickToMarkLabel = new JLabel();
    clickToMarkLabel.setForeground(Color.red.darker());
    
  	if (radialLineEnabled) {
	    radialLineCheckbox = new JCheckBox();
	    radialLineCheckbox.setOpaque(false);
	    radialLineCheckbox.setBorder(BorderFactory.createEmptyBorder());
	    radialLineCheckbox.setSelected(isRadialLineVisible());
	    radialLineCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setRadialLineVisible(radialLineCheckbox.isSelected());
					repaint();
			  	dataValid = false;
			  	firePropertyChange("data", null, null); //$NON-NLS-1$
				}    	
	    });
	    checkboxSeparator = Box.createRigidArea(new Dimension(6, 4));
  	}
    
    // create actions, listeners, labels and fields for data points
    final Action dataPointAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	if (trackerPanel==null) return;
      	if (e!=null && e.getSource()==xDataField && xDataField.getBackground()!=Color.yellow) return;
      	if (e!=null && e.getSource()==yDataField && yDataField.getBackground()!=Color.yellow) return;
        TPoint p = trackerPanel.getSelectedPoint();
        if (!(p instanceof DataPoint)) return;
        double xValue = xDataField.getValue();
        double yValue = yDataField.getValue();
        int n = trackerPanel.getFrameNumber();
        ImageCoordSystem coords = trackerPanel.getCoords();
        double x = coords.worldToImageX(n, xValue, yValue);
        double y = coords.worldToImageY(n, xValue, yValue);
        p.setXY(x, y);
        p.showCoordinates(trackerPanel);
        if (e!=null && e.getSource() instanceof NumberField) {
        	((NumberField)e.getSource()).requestFocusInWindow();
        }
      }
    };
    // focus listener
    FocusListener dataFieldFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (e.getSource()==xDataField && xDataField.getBackground()!=Color.yellow) return;
      	if (e.getSource()==yDataField && yDataField.getBackground()!=Color.yellow) return;
      	dataPointAction.actionPerformed(null);
      }
    };
    xDataPointLabel = new JLabel("selected x"); //$NON-NLS-1$
    xDataPointLabel.setBorder(xLabel.getBorder());
    xDataField = new NumberField(5);
    xDataField.setBorder(fieldBorder);
    xDataField.addActionListener(dataPointAction);
    xDataField.addFocusListener(dataFieldFocusListener);
    yDataPointLabel = new JLabel("y"); //$NON-NLS-1$
    yDataPointLabel.setBorder(xLabel.getBorder());
    yDataField = new NumberField(5);
    yDataField.setBorder(fieldBorder);
    yDataField.addActionListener(dataPointAction);
    yDataField.addFocusListener(dataFieldFocusListener);
    xDataPointSeparator = Box.createRigidArea(new Dimension(6, 4));
    yDataPointSeparator = Box.createRigidArea(new Dimension(6, 4));

    xLabel.setText("center x"); //$NON-NLS-1$
    xField.setPatterns(new String[] {"0.000E0", "0.000", "0.00", "0.0", "0.000E0"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  	xField.setEnabled(false);
    yField.setPatterns(new String[] {"0.000E0", "0.000", "0.00", "0.0", "0.000E0"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  	yField.setEnabled(false);
    magField.setPatterns(new String[] {"0.000E0", "0.000", "0.00", "0.0", "0.000E0"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  	magField.setEnabled(false);
  	xDataField.setPatterns(new String[] {"0.000E0", "0.000", "0.00", "0.0", "0.000E0"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  	yDataField.setPatterns(new String[] {"0.000E0", "0.000", "0.00", "0.0", "0.000E0"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  
  	// set action for angle field
    final Action sliderAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (angleField.getBackground()!=Color.yellow) {
					return;
				}
        double theta = angleField.getValue();
        int n = trackerPanel.getFrameNumber();
        CompassStep step = (CompassStep)getStep(n);
        step.setSliderAngle(theta);
			}  		
    };
  	angleField.addActionListener(sliderAction);
    FocusListener sliderFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	sliderAction.actionPerformed(null);
      }
    };
    angleField.addFocusListener(sliderFocusListener);
  }

  /**
   * Sets the fixed property. When fixed, it has the same position at all times.
   *
   * @param fixed <code>true</code> to fix
   */
  public void setFixed(boolean fixed) {
  	if (fixedPosition == fixed) return;
  	XMLControl control = new XMLControlElement(this);
    if (trackerPanel != null) {
    	trackerPanel.changed = true;
      int n = trackerPanel.getFrameNumber();
      CompassStep source = (CompassStep)getStep(n);
      CompassStep target = (CompassStep)getStep(0);
      target.copy(source);
      trackerPanel.repaint();
    }
    fixedPosition = fixed;
    if (fixed) { // refresh data and post undo only when fixing
    	keyFrames.clear();
    	keyFrames.add(0);
    	dataValid = false;
    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
    	Undo.postTrackEdit(this, control);    	
    }
  }

  /**
   * Gets the fixed property.
   *
   * @return <code>true</code> if fixed
   */
  public boolean isFixed() {
    return fixedPosition;
  }

  /**
   * Sets the visibility of the radial line.
   *
   * @param vis true to draw a radial line
   */
  public void setRadialLineVisible(boolean vis) {
  	boolean changed = vis!=radialLineVisible;
  	radialLineVisible = vis;
  	if (trackerPanel!=null) {
  		int n = trackerPanel.getFrameNumber();
    	steps.getStep(n).repaint();
    	if (changed) {
        trackerPanel.changed = true;
    	}
  	}
  }

  /**
   * Gets the visibility of the radial line.
   *
   * @return true if visible
   */
  public boolean isRadialLineVisible() {
    return radialLineEnabled && radialLineVisible;
  }
  
  @Override
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();    
    if (trackerPanel.getSelectedTrack() == this) {
      if (name.equals("stepnumber")) { //$NON-NLS-1$
      	refreshFields(trackerPanel.getFrameNumber());
      }
//      else if (name.equals("selectedpoint")) { //$NON-NLS-1$
//      	TPoint p = trackerPanel.getSelectedPoint();
//      }
      else if (name.equals("transform")) { //$NON-NLS-1$
      	refreshFields(trackerPanel.getFrameNumber());
      }
    }// pig does code below work or do anything?
    if (name.equals("adjusting") && e.getSource() instanceof TrackerPanel) { //$NON-NLS-1$
			refreshDataLater = (Boolean)e.getNewValue();
			if (!refreshDataLater) {  // stopped adjusting
	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
			}
    }
    super.propertyChange(e);
  }

  @Override
  public void setTrailVisible(boolean visible) {/** empty block */}

  /**
   * Implements createStep but only mimics step creation since
   * steps are created automatically by the autofill StepArray.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the step
   */
  @Override
  public Step createStep(int n, double x, double y) {
  	if (!isFixed()) {
    	keyFrames.add(n);
	    CompassStep step = (CompassStep)steps.getStep(n);
	    step.addDataPoint(x, y, true);
	    return step;
  	}
  	else {
      keyFrames.add(0);      
	    CompassStep step = (CompassStep)steps.getStep(0);
	    step.addDataPoint(x, y, true);
	    return getStep(n);
  	}
  }

  @Override
  public TPoint autoMarkAt(int n, double x, double y) {
  	setFixed(false);
  	CompassStep step = (CompassStep)steps.getStep(n);
  	int i = getTargetIndex();
  	if (i==0) {
  		// pig this isn't right
	    step.center.setLocation(x, y); 		  		
  	}
//  	else if (i==1) {
//	    step.end1.setLocation(x, y); 		
//  	}
//  	else {
//	    step.end2.setLocation(x, y); 		  		
//  	}
  	keyFrames.add(n);
  	step.repaint();
  	return getMarkedPoint(n, i);
  }
  
  /**
   * Overrides TTrack deleteStep method to prevent deletion.
   *
   * @param n the frame number
   * @return the deleted step
   */
  @Override
  public Step deleteStep(int n) {
  	TPoint p = trackerPanel.getSelectedPoint();
    CompassStep step = (CompassStep)steps.getStep(n);
    if (!isFixed()) {
    	step.removeDataPoint(p);
    }
    else { // fixed, so delete corresponding data point in step 0
    	// find index of p
    	int index = -1;
    	for (int i=0; i<step.dataPoints.size(); i++) {
    		if (step.dataPoints.get(i)==p) {
    			index = i;
    			break;
    		}
    	}
    	if (index>-1) {
        step = (CompassStep)steps.getStep(0);
    		p = step.dataPoints.get(index);
      	step.removeDataPoint(p);
    	}
    }
    return null;
  }

  @Override
  public Step getStep(int n) {
    CompassStep step = (CompassStep)steps.getStep(n);
		refreshStep(step);
    return step;
  }

  @Override
  public Step getStep(TPoint point, TrackerPanel trackerPanel) {
    if (point == null) return null;
    Step[] stepArray = steps.array;
    for (Step step: stepArray) {
    	if (step==null) continue;
      TPoint[] points = step.getPoints();
      for (int i = 0; i < points.length; i++) {
        if (points[i]==point) return step;
      }
      CompassStep compassStep = (CompassStep)step;
      for (TPoint p: compassStep.dataPoints) {
      	if (p==point) return step;
      }
    }
    return null;
  }

  @Override
  public int getStepLength() {
  	return CompassStep.getLength();
  }

  @Override
  public int getFootprintLength() {
    return 3;
  }

  @Override
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	Object[] objectsToSize = new Object[]
  			{clickToMarkLabel, xDataPointLabel, yDataPointLabel};
    FontSizer.setFonts(objectsToSize, level);
  	if (radialLineEnabled) {
      FontSizer.setFonts(radialLineCheckbox, level);
  	}
  }

  @Override
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
        
    fixedItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
    fixedItem.setSelected(isFixed());
    fixedItem.setEnabled(attachments==null || (attachments[0]==null && attachments[1]==null && attachments[2]==null));

    // remove end items and last separator
    menu.remove(deleteTrackItem);
    menu.remove(menu.getMenuComponent(menu.getMenuComponentCount()-1));
    menu.add(fixedItem);
    
  	menu.addSeparator();
    menu.add(deleteTrackItem);
    return menu;
  }

  @Override
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
    int n = trackerPanel.getFrameNumber();
  	refreshFields(n);
  	ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    list.add(stepSeparator);
    CompassStep step = (CompassStep)getStep(n);
    if (step.dataPoints.size()>2) {
    	if (radialLineEnabled) {
		  	radialLineCheckbox.setText(TrackerRes.getString("Compass.Checkbox.RadialLine")); //$NON-NLS-1$
		  	radialLineCheckbox.setToolTipText(TrackerRes.getString("Compass.Checkbox.RadialLine.Tooltip")); //$NON-NLS-1$
		    list.add(radialLineCheckbox);
		    list.add(checkboxSeparator);
    	}
	  	xField.setToolTipText(TrackerRes.getString("Compass.Field.CenterX.Tooltip")); //$NON-NLS-1$
	  	yField.setToolTipText(TrackerRes.getString("Compass.Field.CenterY.Tooltip")); //$NON-NLS-1$
	  	magLabel.setText(TrackerRes.getString("Compass.Label.Radius")); //$NON-NLS-1$
	  	magField.setToolTipText(TrackerRes.getString("Compass.Field.Radius.Tooltip")); //$NON-NLS-1$
	    list.add(magLabel);
	    list.add(magField);
	    list.add(magSeparator);
	    list.add(xLabel);
	    list.add(xField);
	    list.add(xSeparator);
	    list.add(yLabel);
	    list.add(yField);
	    list.add(ySeparator);
    }
    else if (trackerPanel.getSelectedPoint()==null) {
	  	clickToMarkLabel.setText(TrackerRes.getString("Compass.Label.MarkPoint")); //$NON-NLS-1$
	    list.add(clickToMarkLabel);
    }
    return list;
  }
  
  @Override
  public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel,
      TPoint point) {
  	ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
    int n = trackerPanel.getFrameNumber();
  	refreshFields(n);
    CompassStep step = (CompassStep)getStep(n);
    if (point==step.slider) {
      list.add(angleLabel);
      list.add(angleField);
      list.add(angleSeparator);
    }
    else {
	    list.add(xDataPointLabel);
	    list.add(xDataField);
	    list.add(xDataPointSeparator);
	    list.add(yDataPointLabel);
	    list.add(yDataField);
	    list.add(yDataPointSeparator);
    }
    if (step.dataPoints.size()<3) {
	  	clickToMarkLabel.setText(TrackerRes.getString("Compass.Label.MarkPoint")); //$NON-NLS-1$
	    list.add(clickToMarkLabel);
    }
  	return list;
  }

  @Override
  public Interactive findInteractive(
    DrawingPanel panel, int xpix, int ypix) {
    if (!(panel instanceof TrackerPanel) || !isVisible())
      return null;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    int n = trackerPanel.getFrameNumber();
    CompassStep step = (CompassStep)getStep(n);
    if (trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
      Interactive ia = step.findInteractive(trackerPanel, xpix, ypix);
      if (ia == null) {
      	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
      	if (step.dataPoints.size()<3) {
      		hint = TrackerRes.getString("Compass.Hint.Mark3"); //$NON-NLS-1$
      	}
      	else {
      		hint = TrackerRes.getString("Compass.Hint.MarkMore"); //$NON-NLS-1$
      	}
      	return null;
      }
      if (ia instanceof DataPoint) {
        partName = TrackerRes.getString("Compass.DataPoint.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("Compass.DataPoint.Hint"); //$NON-NLS-1$
      }
      else if (ia instanceof Slider) {
        partName = TrackerRes.getString("Compass.Slider.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("Compass.Slider.Hint"); //$NON-NLS-1$
      }
      return ia;
    }
    return null;
  }

  @Override
  public String toString() {
    return TrackerRes.getString("Compass.Name"); //$NON-NLS-1$
  }

//__________________________ protected methods ________________________
  
  @Override
  protected void setTrackerPanel(TrackerPanel panel) {
  	if (trackerPanel != null) { 
  		trackerPanel.removePropertyChangeListener("stepnumber", this); //$NON-NLS-1$
  	}
	  super.setTrackerPanel(panel);
  	if (trackerPanel != null) {
  		trackerPanel.addPropertyChangeListener("stepnumber", this); //$NON-NLS-1$
  	}
    setFixed(isFixed());
  }

  @Override
  protected void setAnglesInRadians(boolean radians) {  	
    super.setAnglesInRadians(radians);
    CompassStep step = (CompassStep)getStep(trackerPanel.getFrameNumber());     
    step.repaint(); // refreshes angle readout
  }

  @Override
  protected boolean isAutoTrackable() {
  	return true;
  }
  
  @Override
  protected boolean isAutoTrackable(int pointIndex) {
  	return pointIndex<3;
  }
  
  @Override
  protected String getTargetDescription(int pointIndex) {
  	if (pointIndex==0) return TrackerRes.getString("Protractor.Vertex.Name"); //$NON-NLS-1$
  	String s = TrackerRes.getString("Protractor.End.Name"); //$NON-NLS-1$
  	return s+" "+(pointIndex); //$NON-NLS-1$
  }

  @Override
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
    if (refreshDataLater || trackerPanel == null || data == null) return;
    dataFrames.clear();
    // get the datasets: radius, x_center, y_center, step, frame
    int count = 0;
    Dataset angle = null;
    Dataset x_center = data.getDataset(count++);
    Dataset y_center = data.getDataset(count++);
    Dataset r = data.getDataset(count++);
    Dataset stepNum = data.getDataset(count++);
    Dataset frameNum = data.getDataset(count++);
    if (radialLineEnabled) {
    	angle = data.getDataset(count++);
    }
    // assign column names to the datasets
    String time = "t"; //$NON-NLS-1$
    if (!x_center.getColumnName(0).equals(time)) { // not yet initialized
    	String center = TrackerRes.getString("Compass.Data.Center"); //$NON-NLS-1$
    	x_center.setXYColumnNames(time, "x_"+center); //$NON-NLS-1$
    	y_center.setXYColumnNames(time, "y_"+center); //$NON-NLS-1$
    	r.setXYColumnNames(time, "r"); //$NON-NLS-1$
	    stepNum.setXYColumnNames(time, "step"); //$NON-NLS-1$
	    frameNum.setXYColumnNames(time, "frame"); //$NON-NLS-1$
      if (radialLineEnabled) {
      	angle.setXYColumnNames(time, "$\\theta$"); //$NON-NLS-1$
      }
    }
    else for (int i = 0; i<count; i++) {
    	data.getDataset(i).clear();
    }
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < dataDescriptions.length; i++) {
      dataDescriptions[i] = TrackerRes.getString("Compass.Data.Description."+i); //$NON-NLS-1$
    }
    // look thru steps and get data for those included in clip
    VideoPlayer player = trackerPanel.getPlayer();
    VideoClip clip = player.getVideoClip();
	  int len = clip.getStepCount();
	  double[][] validData = new double[data.getDatasets().size()+1][len];
    for (int n = 0; n < len; n++) {
      int frame = clip.stepToFrame(n);
      CompassStep next = (CompassStep)getStep(frame);
      next.dataVisible = true;
	    // get the step number and time
	    double t = player.getStepTime(n)/1000.0;
			validData[0][n] = t;
			Point2D center = next.getWorldCenter();
			validData[1][n] = center==null? Double.NaN: center.getX();
			validData[2][n] = center==null? Double.NaN: center.getY();
			double theta = next.getSliderAngle();
			double radius = next.getWorldRadius();
			validData[3][n] = radius;
			validData[4][n] = n;
			validData[5][n] = frame;
      dataFrames.add(frame);
      if (radialLineEnabled) {
      	validData[6][n] = isRadialLineVisible()? theta: Double.NaN;
      }
    }
    // append the data to the data set
	  x_center.append(validData[0], validData[1]);
	  y_center.append(validData[0], validData[2]);
	  r.append(validData[0], validData[3]);
    stepNum.append(validData[0], validData[4]);
    frameNum.append(validData[0], validData[5]);
    if (radialLineEnabled) {
    	angle.append(validData[0], validData[6]);
    }
  }

  /**
   * Refreshes a step by setting it equal to a keyframe step.
   *
   * @param step the step to refresh
   */
  protected void refreshStep(CompassStep step) {
  	// compare step with keyStep
  	CompassStep keyStep = getKeyStep(step);
  	if (keyStep==step) {
  		return;
  	}
  	boolean different = keyStep.dataPoints.size()!=step.dataPoints.size();
  	// compare locations of data points
  	if (!different) {
  		for (int i=0; i<keyStep.dataPoints.size(); i++) {
  			TPoint p1 = keyStep.dataPoints.get(i);
  			TPoint p2 = step.dataPoints.get(i);
  			different = different || p1.x!=p2.x || p1.y!=p2.y;
  		}
  	}
    // update step if needed
    if (different) {
    	step.copy(keyStep);
    }
    if (step.slider.x!=keyStep.slider.x || step.slider.y!=keyStep.slider.y) {
    	step.slider.setLocation(keyStep.slider);
    	repaint();
    }
  }
  
  /**
   * Refreshes the toolbar fields.
   */
  protected void refreshFields(int frameNumber) {
    CompassStep step = (CompassStep)getStep(frameNumber);
   	magField.setValue(step.getWorldRadius());
    Point2D worldPt = step.getWorldCenter();
    xField.setValue(worldPt==null? Double.NaN: worldPt.getX());
    yField.setValue(worldPt==null? Double.NaN: worldPt.getY());

    TPoint p = trackerPanel.getSelectedPoint();
   	if (p instanceof DataPoint) {
	    worldPt = p.getWorldPosition(trackerPanel);
	    xDataField.setValue(worldPt.getX());
	    yDataField.setValue(worldPt.getY());
   	}
   	else if (p==step.slider) {
   		angleField.setValue(step.getSliderAngle());
   	}
  }
  
  /**
   * Returns the key step for a given step.
   * @param step the step
   * @return the key step
   */
  protected CompassStep getKeyStep(CompassStep step) {
  	int key = 0;
  	if (!this.isFixed()) {
	  	for (int i: keyFrames) {
	  		if (i<=step.n)
	  			key = i;
	  	}
  	}
  	return (CompassStep)steps.getStep(key);
  }
  
  /**
   * Sets the coordinate system origin to the circle center in all frames.
   */
  protected void setCoordsOriginToCenter() {
  	XMLControl control = new XMLControlElement(trackerPanel.getCoords());
    if (isFixed()) {
    	CompassStep step = (CompassStep)getStep(0);
    	TPoint pt = step.center;
    	int len = trackerPanel.getCoords().getLength();
    	if (trackerPanel.getCoords().isFixedOrigin()) {
        trackerPanel.getCoords().setOriginXY(0, pt.x, pt.y);
    	}
    	else for (int i = 0; i<len; i++) {
    		if (i>0 && !trackerPanel.getCoords().getKeyFrames().contains(i)) {
    			continue;
    		}
        trackerPanel.getCoords().setOriginXY(i, pt.x, pt.y);
    	}
    }
    else {
    	trackerPanel.getCoords().setFixedOrigin(false);
      Step[] stepArray = steps.array;
      for (Step step: stepArray) {
      	if (step==null) continue;
        CompassStep compassStep = (CompassStep)step;
        trackerPanel.getCoords().setOriginXY(step.n, compassStep.center.x, compassStep.center.y);
      }
    }
    trackerPanel.getAxes().setVisible(true);
    // post undoable edit
    Undo.postCoordsEdit(trackerPanel, control);
  }
  
//__________________________ static methods ___________________________

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
      Compass compass = (Compass)obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save fixed property
      control.setValue("fixed", compass.isFixed()); //$NON-NLS-1$
      // save steps
      Step[] steps = compass.getSteps();
      int count = steps.length;
      if (compass.isFixed()) count = 1;
      double[][] data = new double[count][];
      for (int n = 0; n < count; n++) {
      	// save only key frames
        if (steps[n] == null || !compass.keyFrames.contains(n)) continue;
        CompassStep step = (CompassStep)steps[n];
        int len = step.dataPoints.size();
        if (len==0) continue;
        double[] stepData = new double[2*len+3];
        stepData[0] = n;
        for (int i=0; i<len; i++) {
        	DataPoint p = step.dataPoints.get(i);
        	stepData[2*i+1] = p.x;
        	stepData[2*i+2] = p.y;
        }
        stepData[stepData.length-2] = step.slider.x;
        stepData[stepData.length-1] = step.slider.y;
        data[n] = stepData;
      }
      control.setValue("framedata", data); //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new Compass();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	Compass compass = (Compass)obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      boolean locked = compass.isLocked();
      compass.setLocked(false);
      // load fixed property
      compass.fixedPosition = control.getBoolean("fixed"); //$NON-NLS-1$
      // load step data
      compass.keyFrames.clear();
      double[][] data = (double[][])control.getObject("framedata"); //$NON-NLS-1$
      for (int i = 0; i < data.length; i++) {
        if (data[i] == null) continue;
        int n = (int)data[i][0];
        CompassStep step = (CompassStep)compass.getStep(n);
        step.dataPoints.clear();
        int pointCount = (data[i].length-3)/2;
        for (int j=0; j<pointCount; j++) {
        	step.addDataPoint(data[i][2*j+1], data[i][2*j+2], false);
        }
        // refresh circle and set slider position
  	    step.refreshCircle();
        int last = data[i].length-1;
    		step.slider.setLocation(data[i][last-1], data[i][last]);
      }
      compass.setLocked(locked);
	  	compass.dataValid = false;
	  	compass.firePropertyChange("data", null, compass); //$NON-NLS-1$
      return obj;
    }
  }

}

