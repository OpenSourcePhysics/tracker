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
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

/**
 * A Calibration controls the ImageCoordSystem of a TrackerPanel.
 *
 * @author Douglas Brown
 */
public class Calibration extends TTrack {

  // static fields
  protected static final int X_AXIS = 2;
  protected static final int XY_AXES = 1;
  protected static final int Y_AXIS = 0;

  // instance fields
  protected NumberField x1Field, y1Field;
  protected JLabel point1Label, point2Label, x1Label, y1Label;
  private Component spinnerSeparator;
  private Component[] fieldSeparators = new Component[3];
  protected boolean[] isWorldDataValid = new boolean[] {false, false};
  protected ArrayList<String> axisList = new ArrayList<String>();
  protected JSpinner axisSpinner;
  protected ChangeListener axisListener;
  protected JLabel axisLabel = new JLabel();
  protected int axes = XY_AXES;
  protected int spinnerTextWidth;
  protected boolean fixedCoordinates = true;
  protected JCheckBoxMenuItem fixedCoordinatesItem;

  /**
   * Constructs a Calibration.
   */
  public Calibration() {
    // set up footprint choices and color
    setFootprints(new Footprint[]
        {PointShapeFootprint.getFootprint("Footprint.BoldCrosshair"), //$NON-NLS-1$
         PointShapeFootprint.getFootprint("Footprint.Crosshair")}); //$NON-NLS-1$
    // assign a default name
    setName(TrackerRes.getString("Calibration.New.Name")); //$NON-NLS-1$
    setColor(defaultColors[0]);
    // hide from views
    viewable = false;
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("Calibration.Unmarked.Hint"); //$NON-NLS-1$
    keyFrames.add(0);
    createGUI();
  }
  
  /**
   * Sets the axis type.
   *
   * @param axis one of the type constants X_AXIS, Y_AXIS or XY_AXES
   */
  public void setAxisType(int axis) {
  	if (axis == X_AXIS || axis == Y_AXIS || axis == XY_AXES) {
	  	axes = axis;
  	}
  }

  /** 
   * Creates, adds a point to, or repositions a calibration step.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the new step
   */
  public Step createStep(int n, double x, double y) {
    if (isLocked()) return null;
    CalibrationStep step = (CalibrationStep)getStep(n);
    if (step == null) {
      step = new CalibrationStep(this, n, x, y);
      step.setFootprint(getFootprint());
      steps = new StepArray(step);
    }
    else if (step.getPoints()[1] == null) {
      if (trackerPanel!=null && trackerPanel.getSelectedPoint()==step.getPoints()[0]) {
      	trackerPanel.setSelectedPoint(null);
      }
      step.addSecondPoint(x, y);
      steps = new StepArray(step);
    }
    else if (trackerPanel!=null) {
  		TPoint p = trackerPanel.getSelectedPoint();
  		if (p==null) {
  			p = step.getPosition(1);
  		}
  		if (p instanceof CalibrationStep.Position) {
      	XMLControl state = new XMLControlElement(step);
				p.setLocation(x, y);
				Point2D pt = p.getWorldPosition(trackerPanel);
				
  			if (step.points[0]==p) { // selected position is 0
  				step.setWorldCoordinates(pt.getX(), pt.getY(), step.worldX1, step.worldY1);
  			}
  			else { // selected position is 1
  				step.setWorldCoordinates(step.worldX0, step.worldY0, pt.getX(), pt.getY());
  			}
    		Undo.postStepEdit(step, state);
  		}
    }
    support.firePropertyChange("step", null, n); //$NON-NLS-1$
    return step;
  }

  /**
   * Creates a new calibration step with two calibration points.
   *
   * @param n the frame number
   * @param x1 the x coordinate of point 1 in image space
   * @param y1 the y coordinate of point 1 in image space
   * @param x2 the x coordinate of point 2 in image space
   * @param y2 the y coordinate of point 2 in image space
   * @return the step
   */
  public Step createStep(int n, double x1, double y1, double x2, double y2) {
    createStep(n, x1, y1); // first point
    Step step = createStep(n, x2, y2); // adds second point
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
    CalibrationStep step = (CalibrationStep)getStep(n);
    int index = getTargetIndex();
    ImageCoordSystem coords = trackerPanel.getCoords();
    coords.setFixedOrigin(false);
    coords.setFixedAngle(false);
    coords.setFixedScale(false);
    if (step == null) {
	  	step = (CalibrationStep)createStep(n, x, y);
	  	if (step!=null) 
	  		return step.getPoints()[index];
    }
    else {    	
	    TPoint p = step.getPoints()[index];
	    if (p==null) {
	        if (trackerPanel!=null && trackerPanel.getSelectedPoint()==step.getPoints()[0]) {
	        	trackerPanel.setSelectedPoint(null);
	        }
	        step.addSecondPoint(x, y);
	        steps = new StepArray(step);
	        return step.getPoints()[index];
	    }

      Mark mark = step.marks.get(trackerPanel);
      if (mark==null) {
      	double worldX = index==0? step.worldX0: step.worldX1;
      	double worldY = index==0? step.worldY0: step.worldY1;
	      // set step location to image position of current world coordinates
	      double xx = coords.worldToImageX(n, worldX, worldY);
	      double yy = coords.worldToImageY(n, worldX, worldY);
	      p.setLocation(xx, yy);
      }
    	p.setAdjusting(true);
    	p.setXY(x, y);
    	p.setAdjusting(false);
    	return p;

    }
  	return null;
  }
  
  /**
   * Overrides TTrack getStep method.
   *
   * @param n the frame number
   * @return the step
   */
  public Step getStep(int n) {
  	CalibrationStep step = (CalibrationStep)steps.getStep(n);
    refreshStep(step);
    return step;
  }

  /**
   * Overrides TTrack isLocked method.
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
   * Calibration trails are never visible.
   *
   * @param visible ignored
   */
  public void setTrailVisible(boolean visible) {/** empty block */}

  /**
   * Determines if at least one point in this track is autotrackable.
   *
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable() {
  	return true;
  }
  
  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return CalibrationStep.getLength();
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
      Step step = getStep(n);
      if (step!=null) {
      	steps = new StepArray(getStep(n));
      }
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
   * Overrides TTrack setFootprint to handle PointAxesFootprints.
   *
   * @param name the name of the desired footprint
   */
  public void setFootprint(String name) {
    super.setFootprint(name);
    setAxisType(axes);
  }

  /**
   * Implements findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first calibration point that is hit
   */
  public Interactive findInteractive(
      DrawingPanel panel, int xpix, int ypix) {
    if (!(panel instanceof TrackerPanel) ||
        !isVisible() ||
        !isEnabled()) return null;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Interactive ia = null;
    int n = trackerPanel.getFrameNumber();
    Step step = getStep(n);
    if (step != null &&
        trackerPanel.getPlayer().getVideoClip().includesFrame(n))
      ia = step.findInteractive(trackerPanel, xpix, ypix);
    if (ia != null) {
    	partName = TrackerRes.getString("Calibration.Point.Name"); //$NON-NLS-1$
    	hint = TrackerRes.getString("Calibration.Point.Hint"); //$NON-NLS-1$
    }
    else {
    	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
      hint = TrackerRes.getString("Calibration.Halfmarked.Hint"); //$NON-NLS-1$
    }
    return ia;
  }

  /**
   * Overrides TTrack method.
   *
   * @param locked <code>true</code> to lock this
   */
  public void setLocked(boolean locked) {
    super.setLocked(locked);
  	boolean enabled = !isLocked();
    xField.setEnabled(enabled);
    yField.setEnabled(enabled);
    x1Field.setEnabled(enabled);
    y1Field.setEnabled(enabled);
    if (axisSpinner!=null)
    	axisSpinner.setEnabled(enabled);
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

  /**
   * Overrides TTrack getToolbarTrackComponents method.
   *
   * @param trackerPanel the tracker panel
   * @return a list of components
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
  	ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
  	
    // rebuild the axis spinner
    axisList.clear();
    String y = TrackerRes.getString("Calibration.Axes.YOnly"); //$NON-NLS-1$
    axisList.add(y);
    String xy = TrackerRes.getString("Calibration.Axes.XY"); //$NON-NLS-1$
    axisList.add(xy);
    String x = TrackerRes.getString("Calibration.Axes.XOnly"); //$NON-NLS-1$
    axisList.add(x);
    SpinnerListModel axisModel = new SpinnerListModel(axisList);
    axisSpinner = new JSpinner(axisModel) {
    	public Dimension getPreferredSize() {
    		Dimension dim = super.getPreferredSize();
    		dim.width += spinnerTextWidth/2;
    		return dim;
    	}
    };
    JTextField field = ((JSpinner.DefaultEditor)axisSpinner.getEditor()).getTextField();  
    field.setDisabledTextColor(NumberField.DISABLED_COLOR);     
    String longest = xy;
    longest = x.length()>longest.length()? x: longest;
    longest = y.length()>longest.length()? y: longest;
		Font font = axisSpinner.getFont().deriveFont(Font.PLAIN);
    FontSizer.setFonts(axisSpinner, FontSizer.getLevel());		
    TextLayout layout = new TextLayout(longest, font, frc);
    spinnerTextWidth = (int)layout.getBounds().getWidth();
    // set correct value
    axisModel.setValue(axes == XY_AXES? xy: axes == X_AXIS? x: y);
    axisSpinner.addChangeListener(axisListener);
    axisSpinner.setToolTipText(TrackerRes.getString("Calibration.Spinner.Axes.Tooltip")); //$NON-NLS-1$
    axisLabel.setText(TrackerRes.getString("Calibration.Label.Axes")); //$NON-NLS-1$
    Border empty = BorderFactory.createEmptyBorder(0, 4, 0, 2);
    axisLabel.setBorder(empty);
    list.add(axisLabel);
    list.add(axisSpinner);
    list.add(spinnerSeparator);
    
	  int n = trackerPanel.getFrameNumber();
    Step step = getStep(n);
    
    // add world coordinate fields and labels
    String s = TrackerRes.getString("Calibration.Label.Point"); //$NON-NLS-1$
    String unmarked = TrackerRes.getString("TTrack.Label.Unmarked"); //$NON-NLS-1$
    point1Label.setText(s+" 1: "); //$NON-NLS-1$
    point2Label.setText(s+" 2: "); //$NON-NLS-1$
    point1Label.setForeground(yLabel.getForeground());
    point2Label.setForeground(yLabel.getForeground());
    
    boolean exists = step!=null;
    boolean complete = exists && step.getPoints()[1]!=null;
    if (!exists) {
      point1Label.setText(s+" 1: "+unmarked); //$NON-NLS-1$
      point1Label.setForeground(Color.red.darker());
    }
    else if (!complete){
      point2Label.setText(s+" 2: "+unmarked); //$NON-NLS-1$
      point2Label.setForeground(Color.red.darker());
    }
    if (axes == Y_AXIS) {
      list.add(point1Label);
      if (exists) {
	      list.add(yLabel);
	      list.add(yField); 
	      list.add(fieldSeparators[1]);
	      list.add(point2Label);
	      if (complete) {
		      list.add(y1Label);
		      list.add(y1Field);
	      }
      }
    }
    else if (axes == X_AXIS) {
      list.add(point1Label);
      if (exists) {
	      list.add(xLabel);
	      list.add(xField); 
	      list.add(fieldSeparators[1]);
	      list.add(point2Label);
	      if (complete) {
		      list.add(x1Label);
		      list.add(x1Field);
	      }
      }
    }
    else {
      list.add(point1Label);
      if (exists) {
        list.add(xLabel);
        list.add(xField);
        list.add(fieldSeparators[0]);
        list.add(yLabel);
        list.add(yField);    	
        list.add(fieldSeparators[1]);
        list.add(point2Label);
	      if (complete) {
	        list.add(x1Label);
	        list.add(x1Field);
	        list.add(fieldSeparators[2]);
	        list.add(y1Label);
	        list.add(y1Field);	      	
	      }
      }
    }
    
    boolean locked = trackerPanel.getCoords().isLocked() || super.isLocked();
    xField.setEnabled(!locked);
    yField.setEnabled(!locked);
    x1Field.setEnabled(!locked);
    y1Field.setEnabled(!locked);
    axisSpinner.setEnabled(!locked);
    // display world coordinates in fields
    displayWorldCoordinates();
    return list;
  }

  /**
   * Refreshes a step by setting it equal to the previous keyframe step.
   *
   * @param step the step to refresh
   */
  protected void refreshStep(CalibrationStep step) {
  	if (step==null) return;
  	int key = 0;
  	for (int i: keyFrames) {
  		if (i<=step.n)
  			key = i;
  	}
  	// compare step with keyStep
  	CalibrationStep keyStep = (CalibrationStep)steps.getStep(key);
    boolean different = keyStep.worldX0!=step.worldX0 || keyStep.worldY0!=step.worldY0
    		|| keyStep.worldX1!=step.worldX1 || keyStep.worldY1!=step.worldY1;
    // update step if needed
    if (different) {
      step.worldX0 = keyStep.worldX0;
      step.worldY0 = keyStep.worldY0;
      step.worldX1 = keyStep.worldX1;
      step.worldY1 = keyStep.worldY1;
    }
    step.erase();
  }

  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	Object[] objectsToSize = new Object[]
  			{point1Label, point2Label, x1Label, y1Label, x1Field, y1Field, axisLabel,
  			fixedCoordinatesItem};
    FontSizer.setFonts(objectsToSize, level);
  }

  /**
   * Overrides Object toString method.
   *
   * @return the name of this track
   */
  public String toString() {
    return TrackerRes.getString("Calibration.Name"); //$NON-NLS-1$
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
    	boolean enabled = !isLocked();
      xField.setEnabled(enabled);
      yField.setEnabled(enabled);
      x1Field.setEnabled(enabled);
      y1Field.setEnabled(enabled);
      axisSpinner.setEnabled(enabled);
    }
    else super.propertyChange(e);
  }

  /**
   * Overrides TTrack method
   *
   * @return the point index
   */
  protected int getTargetIndex() {
//    int n = trackerPanel.getFrameNumber();
//    CalibrationStep step = (CalibrationStep)getStep(n);
//    if (step!=null) {
//	    for (int i=0; i<step.getPoints().length; i++) {
//		    if (step.getPoints()[i]==null) {
//		    	return i;
//		    }	    	
//	    }
//    }
  	return super.getTargetIndex();
  }

  /**
   * Returns a description of the point at a given index. Used by AutoTracker.
   *
   * @param pointIndex the points[] index
   * @return the description
   */
  protected String getTargetDescription(int pointIndex) {
  	String s = TrackerRes.getString("Calibration.Point.Name"); //$NON-NLS-1$
  	return s+" "+(pointIndex+1); //$NON-NLS-1$
  }

  /**
   * Reads the world coordinate fields and, if different, sets the coordinates
   * of the currently selected point.
   */
  private void setWorldCoordinatesFromFields() {
    if (trackerPanel == null) return;
    double x1 = xField.getValue();
    double y1 = yField.getValue();
    double x2 = x1Field.getValue();
    double y2 = y1Field.getValue();
    int n = trackerPanel.getFrameNumber();
    CalibrationStep step = (CalibrationStep)getStep(n);
    boolean different = step.worldX0!=x1 || step.worldY0!=y1
    		|| step.worldX1!=x2 || step.worldY1!=y2;
    if (different) {
    	XMLControl trackControl = new XMLControlElement(this);
    	XMLControl coordsControl = new XMLControlElement(trackerPanel.getCoords());
    	step.setWorldCoordinates(x1, y1, x2, y2);
    	Undo.postTrackAndCoordsEdit(this, trackControl, coordsControl);
    }
  }
  
  /**
   * Displays the world coordinates of the currently selected step.
   */
  protected void displayWorldCoordinates() {
    int n = trackerPanel==null? 0: trackerPanel.getFrameNumber();
    CalibrationStep step = (CalibrationStep)getStep(n);
    if (step==null) {
	    xField.setText(null);
	    yField.setText(null);
	    x1Field.setText(null);
	    y1Field.setText(null);    	
    }
    else if (step.getPoints()[1]==null){
	    xField.setValue(step.worldX0);
	    yField.setValue(step.worldY0);
	    x1Field.setText(null);
	    y1Field.setText(null);    	
    }
    else {
	    xField.setValue(step.worldX0);
	    yField.setValue(step.worldY0);
	    x1Field.setValue(step.worldX1);
	    y1Field.setValue(step.worldY1);
    }
  }
  
  /**
   * Creates the GUI.
   */
  private void createGUI() {
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
        ((NumberField)e.getSource()).requestFocusInWindow();
      }
    };
    FocusListener xyFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setWorldCoordinatesFromFields();
      }
    };
    x1Field = new NumberField(5);
    x1Field.setBorder(fieldBorder);
    y1Field = new NumberField(5);
    y1Field.setBorder(fieldBorder);
    xField.addActionListener(xyAction);
    xField.addFocusListener(xyFocusListener);
    yField.addActionListener(xyAction);
    yField.addFocusListener(xyFocusListener);
    x1Field.addActionListener(xyAction);
    x1Field.addFocusListener(xyFocusListener);
    y1Field.addActionListener(xyAction);
    y1Field.addFocusListener(xyFocusListener);
    point1Label = new JLabel();
    point2Label = new JLabel();
    point1Label.setBorder(xLabel.getBorder());
    point2Label.setBorder(yLabel.getBorder());
    x1Label = new JLabel("x"); //$NON-NLS-1$
    y1Label = new JLabel("y"); //$NON-NLS-1$
    x1Label.setBorder(xLabel.getBorder());
    y1Label.setBorder(yLabel.getBorder());
    fieldSeparators[0] = Box.createRigidArea(new Dimension(4, 4));
    fieldSeparators[1] = Box.createRigidArea(new Dimension(8, 4));
    fieldSeparators[2] = Box.createRigidArea(new Dimension(4, 4));
    axisListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String choice = (String)axisSpinner.getValue();
        for(int i = 0;i<axisList.size();i++) {
          if(axisList.get(i).equals(choice)) {
          	if (axes == i) return;
          	// check for invalid coords if step is complete
          	if (trackerPanel != null) {
            	int n = trackerPanel.getFrameNumber();
          		CalibrationStep step = (CalibrationStep)getStep(n);            	
            	boolean isComplete = step!=null && step.getPoints()[1]!=null;
            	if (isComplete) {
              	if (i == X_AXIS && step.worldX0 == step.worldX1) {
              		JOptionPane.showMessageDialog(trackerPanel, 
                      				TrackerRes.getString("Calibration.Dialog.InvalidXCoordinates.Message"), //$NON-NLS-1$
                      				TrackerRes.getString("Calibration.Dialog.InvalidCoordinates.Title"), //$NON-NLS-1$
                      				JOptionPane.WARNING_MESSAGE);
              		axisSpinner.setValue(axisList.get(axes));
              		return;
              	}
              	else if (i == Y_AXIS && step.worldY0 == step.worldY1) {
                  JOptionPane.showMessageDialog(trackerPanel, 
                          		TrackerRes.getString("Calibration.Dialog.InvalidYCoordinates.Message"), //$NON-NLS-1$
                          		TrackerRes.getString("Calibration.Dialog.InvalidCoordinates.Title"), //$NON-NLS-1$
                          		JOptionPane.WARNING_MESSAGE);
                  axisSpinner.setValue(axisList.get(axes));
                  return;
                }
            	}
          	}
          	setAxisType(i);
          	if (trackerPanel != null) {
          		trackerPanel.getTFrame().getTrackBar(trackerPanel).refresh();
          	}
          }
        }
      }
    };
    spinnerSeparator = Box.createRigidArea(new Dimension(8, 4));
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
      Calibration cal = (Calibration) obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save fixed coordinates
      control.setValue("fixed_coordinates", cal.isFixedCoordinates()); //$NON-NLS-1$
      // save world coordinates
      if (!cal.steps.isEmpty()) {
	      Step[] steps = cal.getSteps();
	      double[][] stepData = new double[steps.length][];
	      for (int i = 0; i<steps.length; i++) {
	      	// save only key frames
	        if (steps[i]==null || !cal.keyFrames.contains(i)) continue;
	        CalibrationStep step = (CalibrationStep)steps[i];
	      	stepData[i] = new double[] {step.worldX0, step.worldY0, step.worldX1, step.worldY1};
	      	if (!control.getPropertyNames().contains("worldX0")) { //$NON-NLS-1$
	      		// include these for backward compatibility
	  	      control.setValue("worldX0", step.worldX0); //$NON-NLS-1$
	  	      control.setValue("worldY0", step.worldY0); //$NON-NLS-1$
	  	      control.setValue("worldX1", step.worldX1); //$NON-NLS-1$
	  	      control.setValue("worldY1", step.worldY1); //$NON-NLS-1$
	      	}
	      }
	      control.setValue("world_coordinates", stepData); //$NON-NLS-1$
      }
      // save axis type
      String type = cal.axes == Calibration.X_AXIS? "X":  //$NON-NLS-1$
      	cal.axes == Calibration.Y_AXIS? "Y": "XY"; //$NON-NLS-1$ //$NON-NLS-2$
      control.setValue("axes", type); //$NON-NLS-1$
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new Calibration();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      Calibration cal = (Calibration) obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      boolean locked = cal.isLocked();
      cal.setLocked(false);
      // load axis type
      String type = control.getString("axes"); //$NON-NLS-1$
      if (type != null) {
      	cal.setAxisType(type.equals("X")? Calibration.X_AXIS:  //$NON-NLS-1$
      		type.equals("Y")? Calibration.Y_AXIS:  //$NON-NLS-1$
      			Calibration.XY_AXES);
      }
      // load fixed coordinates
      if (control.getPropertyNames().contains("fixed_coordinates")) //$NON-NLS-1$
      	cal.fixedCoordinates = control.getBoolean("fixed_coordinates"); //$NON-NLS-1$
      cal.keyFrames.clear();
      // create step array if needed
      if (cal.steps.isEmpty())
      	cal.createStep(0, 0, 0, 1, 0);
      // load world coordinates
      double[][] stepData = (double[][])control.getObject("world_coordinates"); //$NON-NLS-1$
      if (stepData!=null) {
      	for (int i = 0; i<stepData.length; i++) {
      		if (stepData[i]!=null) {
      			CalibrationStep step = (CalibrationStep)cal.getStep(i);
      			step.worldX0 = stepData[i][0];
      			step.worldY0 = stepData[i][1];
      			step.worldX1 = stepData[i][2];
      			step.worldY1 = stepData[i][3];
      			cal.keyFrames.add(i);
      		}
      	}
      }
      else { // load legacy files
	      CalibrationStep step = (CalibrationStep)cal.getStep(0);
	      step.worldX0 = control.getDouble("worldX0"); //$NON-NLS-1$
	      step.worldY0 = control.getDouble("worldY0"); //$NON-NLS-1$
	      step.worldX1 = control.getDouble("worldX1"); //$NON-NLS-1$
	      step.worldY1 = control.getDouble("worldY1"); //$NON-NLS-1$
  			cal.keyFrames.add(0);
      }
      cal.setLocked(locked);
      cal.displayWorldCoordinates();
      return obj;
    }
  }
}
