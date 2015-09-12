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

import java.text.*;
import java.util.ArrayList;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.border.Border;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * A TapeMeasure measures and displays its world length and its angle relative
 * to the positive x-axis. It is used to set the scale and angle of an
 * ImageCoordSystem.
 *
 * @author Douglas Brown
 */
public class TapeMeasure extends TTrack {
	
	// static constants
	protected static final double MIN_LENGTH = 1.0E-30;
  @SuppressWarnings("javadoc")
	public static final float[] BROKEN_LINE = new float[] {10, 1};

  // instance fields
  protected boolean fixedPosition = true, fixedLength = true;
  protected JCheckBoxMenuItem fixedPositionItem, fixedLengthItem;
  protected boolean editing = false;
  protected final NumberField inputField;
  protected JPanel inputPanel;
  protected JPanel glassPanel;
  protected NumberFormat format;
  protected MouseListener editListener;
  protected boolean readOnly;
  protected boolean stickMode;
  protected boolean isStepChangingScale;
	protected boolean notYetShown = true;
	protected Footprint[] tapeFootprints, stickFootprints;
  protected TreeSet<Integer> lengthKeyFrames = new TreeSet<Integer>(); // applies to sticks only
  protected JMenuItem attachmentItem;
		
  /**
   * Constructs a TapeMeasure.
   */
  public TapeMeasure() {
    // assign a default name
    setName(TrackerRes.getString("TapeMeasure.New.Name")); //$NON-NLS-1$
		defaultColors = new Color[] {new Color(204, 0, 0)};
		
    // assign default plot variables
    setProperty("xVarPlot0", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot0", "length"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("xVarPlot1", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot1", Tracker.THETA); //$NON-NLS-1$

    // assign default table variables: length and angle
    setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
    
    // set up footprint choices and color
		tapeFootprints = new Footprint[]
		    {LineFootprint.getFootprint("Footprint.DoubleArrow"), //$NON-NLS-1$
		     LineFootprint.getFootprint("Footprint.BoldDoubleArrow"), //$NON-NLS-1$
		     LineFootprint.getFootprint("Footprint.Line"), //$NON-NLS-1$
		     LineFootprint.getFootprint("Footprint.BoldLine")}; //$NON-NLS-1$
		stickFootprints = new Footprint[]
		    {LineFootprint.getFootprint("Footprint.BoldDoubleTarget"), //$NON-NLS-1$
		     LineFootprint.getFootprint("Footprint.DoubleTarget")}; //$NON-NLS-1$
    setColor(defaultColors[0]);
		setViewable(false);
    setStickMode(false); // sets up footprints
    setReadOnly(false); // sets up dashed array
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("TapeMeasure.Hint"); //$NON-NLS-1$
    // create input field and panel
    inputField = new NumberField(9);
    inputField.setBorder(null);
    format = inputField.getFormat();
    inputPanel = new JPanel(null);
    inputPanel.setOpaque(false);
    inputPanel.add(inputField);
    // eliminate minimum of magField
    magField.setMinValue(Double.NaN);
    keyFrames.add(0);
    lengthKeyFrames.add(0);
    // add inputField action listener to exit editing mode
    inputField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (editing) {
          int n = trackerPanel.getFrameNumber();
          TapeStep tape = ((TapeStep)getStep(n));
          setEditing(false, tape);
        }
      }
    });
    // add inputField focus listener
    inputField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        inputField.selectAll();
      }
      public void focusLost(FocusEvent e) {
        if (editing) {
          int n = trackerPanel.getFrameNumber();
          TapeStep tape = ((TapeStep)getStep(n));
          setEditing(false, tape);
        }
      }
    });
    // add mouse listener to toggle editing mode
    editListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (editing) {
          int n = trackerPanel.getFrameNumber();
          TapeStep tape = (TapeStep)getStep(n);
          setEditing(false, tape);
        }
      }
      public void mouseClicked(MouseEvent e) {
        if (isLocked()) return;
        int n = trackerPanel.getFrameNumber();
        TapeStep step = (TapeStep)getStep(n);
        Rectangle bounds = step.layoutBounds.get(trackerPanel);
        if (bounds != null &&
            bounds.contains(e.getPoint())) {
          setEditing(true, step);
        }
      }
    };
    fixedPositionItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
    fixedPositionItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixedPosition(fixedPositionItem.isSelected());
      }
    });
    fixedLengthItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.FixedLength")); //$NON-NLS-1$
    fixedLengthItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixedLength(fixedLengthItem.isSelected());
      }
    });
    final FocusListener magFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (magField.getBackground() == Color.yellow) {
          int n = trackerPanel.getFrameNumber();
        	// if not fixed, add frame number to key frames
          if (!isFixedPosition()) 
          	keyFrames.add(n);
          TapeStep step = (TapeStep)getStep(n);
          // replace with key frame step
          step = getKeyStep(step);
          step.setTapeLength(magField.getValue());
          dataValid = false;
  	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
      	}
      }
    };
    magField.addFocusListener(magFocusListener);
    magField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	magFocusListener.focusLost(null);
        magField.requestFocusInWindow();
      }
    });
    final FocusListener angleFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (angleField.getBackground() == Color.yellow) {
	        int n = trackerPanel.getFrameNumber();
        	// if not fixed, add frame number to key frames
          if (!isFixedPosition()) 
          	keyFrames.add(n);
          TapeStep step = (TapeStep)getStep(n);
          // replace with key frame step
          step = getKeyStep(step);
	        step.setTapeAngle(angleField.getValue());
          dataValid = false;
  	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
	        if (!isReadOnly())
	        	trackerPanel.getAxes().setVisible(true);
      	}
      }
    };
    angleField.addFocusListener(angleFocusListener);
    angleField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	angleFocusListener.focusLost(null);
        angleField.requestFocusInWindow();
      }
    });
  }
  
  /**
   * Sets the fixed position property. When it is fixed, it's ends are in the same
   * position at all times.
   *
   * @param fixed <code>true</code> to fix the position
   */
  public void setFixedPosition(boolean fixed) {
  	if (fixedPosition == fixed) return;
  	XMLControl control = new XMLControlElement(this);
  	boolean hasSteps = false;
    if (trackerPanel != null) {
    	int n = trackerPanel.getFrameNumber();
    	trackerPanel.changed = true;
    	TapeStep keyStep = (TapeStep)getStep(n);
      for (int i = 0; i < steps.length; i++) {
      	TapeStep step = (TapeStep)steps.getStep(i);
      	if (step==null) continue;
        step.getEnd1().setLocation(keyStep.getEnd1());
        step.getEnd2().setLocation(keyStep.getEnd2());
        hasSteps = true;
      }
      trackerPanel.repaint();
    }
    if (fixed) {
    	keyFrames.clear();
    	keyFrames.add(0);
      dataValid = false;
    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
    	erase();
    }
    fixedPosition = fixed;
    if (hasSteps)
    	Undo.postTrackEdit(this, control);
  }

  /**
   * Gets the fixed position property.
   *
   * @return <code>true</code> if position is fixed
   */
  public boolean isFixedPosition() {
    return fixedPosition;
  }

  /**
   * Sets the fixed length property. When it is fixed, it has the same
   * world length at all times. Applies to sticks only.
   *
   * @param fixed <code>true</code> to fix the length
   */
  public void setFixedLength(boolean fixed) {
  	if (fixedLength == fixed) return;
  	XMLControl control = new XMLControlElement(this);
    if (trackerPanel != null) {
    	int n = trackerPanel.getFrameNumber();
    	trackerPanel.changed = true;
    	TapeStep keyStep = (TapeStep)getStep(n);
      for (int i = 0; i < steps.length; i++) {
      	TapeStep step = (TapeStep)steps.getStep(i);
        step.worldLength = keyStep.worldLength;
      }
      trackerPanel.repaint();
    }
    if (fixed) {
    	lengthKeyFrames.clear();
    	lengthKeyFrames.add(0);
    }
  	fixedLength = fixed;
    Undo.postTrackEdit(this, control);
  }

  /**
   * Gets the fixed length property.
   *
   * @return <code>true</code> if length is fixed
   */
  public boolean isFixedLength() {
    return fixedLength;
  }

  /**
   * Sets the readOnly property. When true, the scale and angle are not settable.
   *
   * @param readOnly <code>true</code> to prevent editing
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    for (Footprint footprint: getFootprints()) {
    	if (footprint instanceof DoubleArrowFootprint) {
    		DoubleArrowFootprint line = (DoubleArrowFootprint)footprint;
    		line.setSolidHead(isReadOnly()? false: true);
    	}
    }
  }

  /**
   * Gets the ReadOnly property.
   *
   * @return <code>true</code> if read-only
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Sets the stickMode property. When true, the 'stick" has constant world length
   * and the scale changes when you drag the mouse. When false, the "tape" stretches
   * without changing the scale when you drag the mouse.
   *
   * @param stick <code>true</code> for stick mode, <code>false</code> for tape mode
   */
  public void setStickMode(boolean stick) {
    stickMode = stick;
    Color color = getColor();
    // set footprints and update world lengths
    if (isStickMode()) {
    	setFootprints(stickFootprints);
    }
    else {
    	setFootprints(tapeFootprints);
    }
    defaultFootprint = getFootprint();
    setColor(color);
    // set tip edit triggers
    for (Step step: getSteps()) {
    	if (step!=null) {
    		TapeStep tapeStep = (TapeStep)step;
    		tapeStep.end1.setCoordsEditTrigger(isStickMode());
    		tapeStep.end2.setCoordsEditTrigger(isStickMode());
    	}
    }
    repaint();
  }

  /**
   * Gets the stickMode property.
   *
   * @return <code>true</code> if in stick mode
   */
  public boolean isStickMode() {
    return stickMode;
  }

  /**
   * Overrides TTrack method.
   *
   * @param locked <code>true</code> to lock this
   */
  public void setLocked(boolean locked) {
    super.setLocked(locked);
  	boolean enabled = isFieldsEnabled();
  	magField.setEnabled(enabled);
    angleField.setEnabled(enabled);
  }

  /**
   * Responds to property change events. Overrides TTrack method.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (isStickMode() && name.equals("transform") && !isStepChangingScale) { //$NON-NLS-1$
    	// stretch or squeeze stick to keep constant world length
    	int n = trackerPanel.getFrameNumber();
    	TapeStep step = (TapeStep)getStep(n);
    	step.adjustTipsToLength();
    	if (!isFixedPosition()) {
      	keyFrames.add(n);
    	}      
    }
    if (name.equals("adjusting") && e.getSource() instanceof TrackerPanel) { //$NON-NLS-1$
			refreshDataLater = (Boolean)e.getNewValue();
			if (!refreshDataLater) {  // stopped adjusting
	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
			}
    }
    if (name.equals("stepnumber")) { //$NON-NLS-1$
      if (trackerPanel.getSelectedTrack() == this) {
	      TapeStep step = (TapeStep)getStep(trackerPanel.getFrameNumber());     
	      step.getTapeLength(!isStickMode());
	      boolean enabled = isFieldsEnabled();
	      magField.setEnabled(enabled);
	      angleField.setEnabled(enabled);
      }
    }
    else if (name.equals("locked")) { //$NON-NLS-1$
    	boolean enabled = isFieldsEnabled();
    	magField.setEnabled(enabled);
      angleField.setEnabled(enabled);
    }
    else if (isStickMode() && name.equals("fixed_scale") && e.getNewValue()==Boolean.FALSE) { //$NON-NLS-1$
    	setFixedPosition(false);
    }
    else super.propertyChange(e);
  }

  /**
   * Overrides TTrack setVisible method to change notYetShown flag.
   *
   * @param visible <code>true</code> to show this track
   */
  public void setVisible(boolean visible) {
  	super.setVisible(visible);
  	if (visible) notYetShown = false;
  }

  /**
   * Overrides TTrack setTrailVisible method to keep trails hidden.
   *
   * @param visible ignored
   */
  public void setTrailVisible(boolean visible) {/** empty block */}

  /**
   * Overrides TTrack isLocked method.
   *
   * @return <code>true</code> if this is locked
   */
  public boolean isLocked() {
    boolean locked = super.isLocked();
    if (!readOnly && trackerPanel != null
    		&& !(trackerPanel.getSelectedPoint() instanceof TapeStep.Handle)) {
      locked = locked || trackerPanel.getCoords().isLocked();
    }
    return locked;
  }

  /**
   * Implements createStep but only mimics step creation since
   * steps are created automatically by the autofill StepArray.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the step
   */
  public Step createStep(int n, double x, double y) {
    TapeStep step = (TapeStep)getStep(n);
    TPoint[] pts = step.getPoints();
    TPoint p = trackerPanel==null? null: trackerPanel.getSelectedPoint();
    if (p==null) {
    	p = step.getEnd1();
    	if (trackerPanel!=null)
    		trackerPanel.setSelectedPoint(p);
    }
    if (p==pts[0] || p==pts[1]) {
    	p.setLocation(x, y);
      keyFrames.add(n);
    	step.worldLength = step.getTapeLength(true);
    }
    return step;
  }

  /**
   * Mimics step creation by setting end positions of an existing step.
   *
   * @param n the frame number
   * @param x1 the x coordinate of end1 in image space
   * @param y1 the y coordinate of end1 in image space
   * @param x2 the x coordinate of end2 in image space
   * @param y2 the y coordinate of end2 in image space
   * @return the step
   */
  public Step createStep(int n, double x1, double y1, double x2, double y2) {
    TapeStep step = (TapeStep)steps.getStep(n);
    if (step==null) {
  	  step = new TapeStep(this, n, x1, y1, x2, y2);
	    step.worldLength = step.getTapeLength(true);    	
	    step.setFootprint(getFootprint());
	    steps = new StepArray(step); // autofill
    }
    else {
      step.getEnd1().setLocation(x1, y1);
      step.getEnd2().setLocation(x2, y2);
    }
    keyFrames.add(n);
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
    TapeStep step = (TapeStep)getStep(n);
    int index = getTargetIndex();
    ImageCoordSystem coords = trackerPanel.getCoords();
    coords.setFixedScale(false);
    this.setFixedPosition(false);
    TPoint p = step.getPoints()[index];
  	p.setAdjusting(true);
  	p.setXY(x, y);
  	p.setAdjusting(false);
  	return p;
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
   * Overrides TTrack getStep method to provide fixedTape behavior.
   *
   * @param n the frame number
   * @return the step
   */
  public Step getStep(int n) {
    TapeStep step = (TapeStep)steps.getStep(n);
    refreshStep(step);
    return step;
  }

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return TapeStep.getLength();
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
   * Formats the specified length value.
   *
   * @param length the length value to format
   * @return the formatted length string
   */
  public String getFormattedLength(double length) {
    inputField.setFormatFor(length);
    return format.format(length);
  }

  /**
   * Reports whether or not this is viewable.
   *
   * @return <code>true</code> if this track is viewable
   */
  public boolean isViewable() {
    return isReadOnly() && !isStickMode();
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
   * Determines if the given point index is autotrackable.
   *
   * @param pointIndex the points[] index
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable(int pointIndex) {
  	return pointIndex<2;
  }
  
  /**
   * Returns a description of a target point with a given index.
   *
   * @param pointIndex the index
   * @return the description
   */
  protected String getTargetDescription(int pointIndex) {
  	String s = TrackerRes.getString("Calibration.Point.Name"); //$NON-NLS-1$
  	return s+" "+(pointIndex+1); //$NON-NLS-1$
  }

  /**
   * Returns a menu with items that control this track.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    // assemble the menu
    JMenu menu = super.getMenu(trackerPanel);
  	
    lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());
    
    // remove end items and last separator
    menu.remove(deleteTrackItem);
    menu.remove(menu.getMenuComponent(menu.getMenuComponentCount()-1));
    
    // add items
    fixedPositionItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
    fixedPositionItem.setSelected(isFixedPosition());
    boolean canBeFixed = !isStickMode() || trackerPanel.getCoords().isFixedScale();
    boolean notAttached = attachments==null || (attachments[0]==null && attachments[1]==null);
    fixedPositionItem.setEnabled(canBeFixed && notAttached);
  	menu.add(fixedPositionItem);
//  	if (isStickMode()) {
//	    fixedLengthItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.FixedLength")); //$NON-NLS-1$
//	    fixedLengthItem.setSelected(isFixedLength());
//	  	menu.add(fixedLengthItem);
//  	}
  	
    // add an attachment dialog item
  	attachmentItem = new JMenuItem(TrackerRes.getString("MeasuringTool.MenuItem.Attach")); //$NON-NLS-1$
  	attachmentItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	ImageCoordSystem coords = TapeMeasure.this.trackerPanel.getCoords();
      	if (TapeMeasure.this.isStickMode() && coords.isFixedScale()) {
      		int result = JOptionPane.showConfirmDialog(TapeMeasure.this.trackerPanel.getTFrame(), 
      				TrackerRes.getString("TapeMeasure.Alert.UnfixScale.Message1") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
      				TrackerRes.getString("TapeMeasure.Alert.UnfixScale.Message2"),  //$NON-NLS-1$
      				TrackerRes.getString("TapeMeasure.Alert.UnfixScale.Title"), //$NON-NLS-1$
      				JOptionPane.YES_NO_OPTION,
      				JOptionPane.QUESTION_MESSAGE);
      		if (result!=JOptionPane.YES_OPTION) return;
      	}
    		coords.setFixedScale(false);
      	AttachmentDialog control = TapeMeasure.this.trackerPanel.getAttachmentDialog(TapeMeasure.this);
      	control.setVisible(true);
      }
    });
  	
  	menu.addSeparator();
    menu.add(attachmentItem);
  	
  	menu.addSeparator();
    menu.add(deleteTrackItem);
    return menu;
  }

  /**
   * Returns a list of point-related toolbar components.
   *
   * @param trackerPanel the tracker panel
   * @return a list of components
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
  	ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    magLabel.setText(TrackerRes.getString("TapeMeasure.Label.Length")); //$NON-NLS-1$
    magField.setToolTipText(TrackerRes.getString("TapeMeasure.Field.Magnitude.Tooltip")); //$NON-NLS-1$
    list.add(stepSeparator);
    list.add(magLabel);
    list.add(magField);
		angleLabel.setText(TrackerRes.getString("TapeMeasure.Label.TapeAngle")); //$NON-NLS-1$
		angleField.setToolTipText(TrackerRes.getString("TapeMeasure.Field.TapeAngle.Tooltip")); //$NON-NLS-1$
    list.add(magSeparator);
    list.add(angleLabel);
    list.add(angleField);
    boolean enabled = isFieldsEnabled();
    magField.setEnabled(enabled);
    angleField.setEnabled(enabled);
    return list;
  }

  /**
   * Implements findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step or motion vector that is hit
   */
  public Interactive findInteractive(
    DrawingPanel panel, int xpix, int ypix) {
    if (!(panel instanceof TrackerPanel) || !isVisible())
      return null;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    int n = trackerPanel.getFrameNumber();
    TapeStep step = (TapeStep)getStep(n);
    TPoint[] pts = step.points;
    if (trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
    	TPoint p = trackerPanel.getSelectedPoint();
      Interactive ia = step.findInteractive(trackerPanel, xpix, ypix);
      if (ia == null) {
        if (p==pts[0] || p==pts[1]) {
          partName = TrackerRes.getString("TapeMeasure.End.Name"); //$NON-NLS-1$
          if (isStickMode() && !isReadOnly())
          	hint = TrackerRes.getString("CalibrationStick.End.Hint"); //$NON-NLS-1$
          else
          	hint = TrackerRes.getString("TapeMeasure.End.Hint"); //$NON-NLS-1$
        }
        else {
        	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
        	if (!isReadOnly())
        		hint = TrackerRes.getString("CalibrationTapeMeasure.Hint"); //$NON-NLS-1$
        	else
        		hint = TrackerRes.getString("TapeMeasure.Hint"); //$NON-NLS-1$
        }
      	return null;
      }
      else if (ia instanceof TapeStep.Tip) {
        partName = TrackerRes.getString("TapeMeasure.End.Name"); //$NON-NLS-1$
        if (isStickMode() && !isReadOnly())
        	hint = TrackerRes.getString("CalibrationStick.End.Hint"); //$NON-NLS-1$
        else
        	hint = TrackerRes.getString("TapeMeasure.End.Hint"); //$NON-NLS-1$
      }
      else if (ia instanceof TapeStep.Handle) {
        partName = TrackerRes.getString("TapeMeasure.Handle.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("TapeMeasure.Handle.Hint"); //$NON-NLS-1$
      }
      else if (ia == this) {
        partName = TrackerRes.getString("TapeMeasure.Readout.Magnitude.Name"); //$NON-NLS-1$
      	if (!isReadOnly())
      		hint = TrackerRes.getString("CalibrationTapeMeasure.Readout.Magnitude.Hint"); //$NON-NLS-1$
      	else
      		hint = TrackerRes.getString("TapeMeasure.Readout.Magnitude.Hint"); //$NON-NLS-1$
      	trackerPanel.setMessage(getMessage());
      } 
      return ia;
    }
    return null;
  }

  /**
   * Refreshes the data.
   *
   * @param data the DatasetManager
   * @param trackerPanel the tracker panel
   */
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
    if (refreshDataLater || trackerPanel == null || data == null) return;
    dataFrames.clear();
    // get the datasets
    int count = 0;
    Dataset length = data.getDataset(count++);
    Dataset angle = data.getDataset(count++);
    Dataset stepNum = data.getDataset(count++);
    Dataset frameNum = data.getDataset(count++);
    // assign column names to the datasets
    String time = "t"; //$NON-NLS-1$
    if (!length.getColumnName(0).equals(time)) { // not yet initialized
    	length.setXYColumnNames(time, "length"); //$NON-NLS-1$
    	angle.setXYColumnNames(time, Tracker.THETA);
	    stepNum.setXYColumnNames(time, "step"); //$NON-NLS-1$
	    frameNum.setXYColumnNames(time, "frame"); //$NON-NLS-1$
    }
    else for (int i = 0; i < count; i++) {
    	data.getDataset(i).clear();
    }
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < dataDescriptions.length; i++) {
      dataDescriptions[i] = TrackerRes.getString("TapeMeasure.Data.Description."+i); //$NON-NLS-1$
    }
    // look thru steps and get data for those included in clip
    VideoPlayer player = trackerPanel.getPlayer();
    VideoClip clip = player.getVideoClip();
	  int len = clip.getStepCount();
	  double[][] validData = new double[data.getDatasets().size()+1][len];
    for (int n = 0; n < len; n++) {
      int frame = clip.stepToFrame(n);
      TapeStep next = (TapeStep)getStep(frame);
      if (next==null) continue;
      next.dataVisible = true;
	    // get the step number and time
	    double t = player.getStepTime(n)/1000.0;
			validData[0][n] = t;
			validData[1][n] = next.getTapeLength(true);
			validData[2][n] = next.getTapeAngle();
			validData[3][n] = n;
			validData[4][n] = frame;
      dataFrames.add(frame);
    }
    // append the data to the data set
	  length.append(validData[0], validData[1]);
	  angle.append(validData[0], validData[2]);
    stepNum.append(validData[0], validData[3]);
    frameNum.append(validData[0], validData[4]);
  }

  /**
   * Remarks all steps on the specified panel.
   * Overrides TTrack method.
   *
   * @param trackerPanel the tracker panel
   */
  public void remark(TrackerPanel trackerPanel) {
  	super.remark(trackerPanel);
    displayState();
  }

  /**
   * Overrides Object toString method.
   *
   * @return the name of this track
   */
  public String toString() {
    return TrackerRes.getString("TapeMeasure.Name"); //$NON-NLS-1$
  }

//__________________________ protected and private methods _______________________

  /**
   * Overrides TTrack setTrackerPanel method.
   *
   * @param panel the TrackerPanel
   */
  protected void setTrackerPanel(TrackerPanel panel) {
  	if (trackerPanel != null) { 
  		trackerPanel.removeMouseListener(editListener);
  		trackerPanel.removePropertyChangeListener("stepnumber", this); //$NON-NLS-1$
  	}
	  super.setTrackerPanel(panel);
  	if (trackerPanel != null) {
  		trackerPanel.addMouseListener(editListener);
//  		trackerPanel.addPropertyChangeListener("stepnumber", this); //$NON-NLS-1$
  	}
    boolean canBeFixed = !isStickMode() || trackerPanel.getCoords().isFixedScale();
    setFixedPosition(isFixedPosition() && canBeFixed);
  }
  
  /**
   * Refreshes world lengths at all steps based on current ends and scale.
   */
  protected void refreshWorldLengths() {
    for (int i=0; i < getSteps().length; i++) {
    	TapeStep step = (TapeStep)getSteps()[i];
    	if (step!=null) {
    		refreshStep(step);
    		step.worldLength = step.getTapeLength(true);
    	}
    }
  }
  
  /**
   * Refreshes a step by setting it equal to the previous keyframe step.
   *
   * @param step the step to refresh
   */
  protected void refreshStep(TapeStep step) {
  	if (step==null) return;
  	int positionKey = 0, lengthKey = 0;
  	for (int i: keyFrames) {
  		if (i<=step.n)
  			positionKey = i;
  	}
  	for (int i: lengthKeyFrames) {
  		if (i<=step.n)
  			lengthKey = i;
  	}
  	// compare step with keyStep
  	boolean different = false;
  	boolean changed = false;
		// check position
  	TapeStep keyStep = (TapeStep)steps.getStep(isFixedPosition()? 0: positionKey);
    different = keyStep.getEnd1().getX()!=step.getEnd1().getX()
		    || keyStep.getEnd1().getY()!=step.getEnd1().getY()
		    || keyStep.getEnd2().getX()!=step.getEnd2().getX()
		    || keyStep.getEnd2().getY()!=step.getEnd2().getY();
    if (different) {
    	step.getEnd1().setLocation(keyStep.getEnd1());
    	step.getEnd2().setLocation(keyStep.getEnd2());
    	changed = true;
    }

    // check length only if in stick mode
  	if (isStickMode()) {
  	  keyStep = (TapeStep)steps.getStep(isFixedLength()? 0: lengthKey);
  		different = keyStep.worldLength!=step.worldLength;
      if (different) {
      	step.worldLength = keyStep.worldLength;
      	changed = true;
      }
  	}
    // erase step if changed
    if (changed) {
	    step.erase();
    }
  }

  /**
   * Sets the editing flag.
   *
   * @param edit <code>true</code> to edit the scale
   * @param target the tape step that handles the edit process
   */
  private void setEditing(boolean edit, TapeStep target) {
    editing = edit;
    if ((readOnly || isStickMode()) && !editing) {
    	// if not fixed, add target frame to key frames
      if (!isFixedPosition()) 
      	keyFrames.add(target.n);
      // replace target with key frame step
      target = getKeyStep(target);
    }
    final TapeStep step = target;
    Runnable runner = new Runnable() {
      public void run() {
        if (editing) {
        	trackerPanel.setSelectedTrack(TapeMeasure.this);
          inputField.setForeground(footprint.getColor());
          Rectangle bounds = step.layoutBounds.get(trackerPanel);
          bounds.grow(3, 3);
          bounds.setLocation(bounds.x+1, bounds.y);
          for (Component c: trackerPanel.getComponents()) {
            if (c == trackerPanel.noData) {
              bounds.setLocation(bounds.x, bounds.y-c.getHeight());
            }
          }
          inputField.setBounds(bounds);
          glassPanel = trackerPanel.getGlassPanel();
          trackerPanel.remove(glassPanel);
          trackerPanel.add(inputPanel, BorderLayout.CENTER);
          Border space = BorderFactory.createEmptyBorder(0, 1, 1, 0);
          Color color = getFootprint().getColor();
          Border line = BorderFactory.createLineBorder(color);
          inputField.setBorder(BorderFactory.createCompoundBorder(line, space));
          inputField.setValue(step.getTapeLength(!isStickMode()));
          trackerPanel.revalidate();
          trackerPanel.repaint();
          inputField.requestFocus();
        }
        else { // end editing
        	step.drawLayoutBounds = false;
          step.setTapeLength(inputField.getValue());
        	inputField.setSigFigs(4);
          trackerPanel.add(glassPanel, BorderLayout.CENTER);
          trackerPanel.remove(inputPanel);
          dataValid = false;
  	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
          trackerPanel.revalidate();
          trackerPanel.repaint();
        }
      }
    };
    EventQueue.invokeLater(runner);
  }
  
  /**
   * Returns the key step for a given step. The key step defines the positions of the tape ends.
   * @param step the step
   * @return the key step
   */
  private TapeStep getKeyStep(TapeStep step) {
  	int key = 0;
  	if (!this.isFixedPosition()) {
	  	for (int i: keyFrames) {
	  		if (i<=step.n)
	  			key = i;
	  	}
  	}
  	return (TapeStep)getStep(key);
  }
  
  /**
   * Displays the world coordinates of the currently selected step.
   */
  private void displayState() {
    int n = trackerPanel==null? 0: trackerPanel.getFrameNumber();
    TapeStep step = (TapeStep)getStep(n);
    if (step!=null) {
	    step.getTapeLength(!isStickMode());
    }
  }
  
  /**
   * Determines if the input fields are enabled.
   * 
   * @return true if enabled
   */
  protected boolean isFieldsEnabled() {
  	// false if this tape is locked
  	if (isLocked()) return false;
  	// false if this tape is for calibration and coords are locked
  	if (!isReadOnly() && trackerPanel!=null && trackerPanel.getCoords().isLocked()) return false;
  	// false if both ends are attached to point masses
    int n = trackerPanel==null? 0: trackerPanel.getFrameNumber();
    TapeStep step = (TapeStep)getStep(n);
    if (step!=null && step.end1.isAttached() && step.end2.isAttached()) return false;
  	return true;
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
      TapeMeasure tape = (TapeMeasure)obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save fixed position and length
      control.setValue("fixedtape", tape.isFixedPosition()); //$NON-NLS-1$
      control.setValue("fixedlength", tape.isFixedLength()); //$NON-NLS-1$
      // save readOnly
      control.setValue("readonly", tape.isReadOnly()); //$NON-NLS-1$
      // save stick mode
      control.setValue("stickmode", tape.isStickMode()); //$NON-NLS-1$
      // save step positions
      Step[] steps = tape.getSteps();
      int count = tape.isFixedPosition()? 1: steps.length;
      FrameData[] data = new FrameData[count];
      for (int n = 0; n < count; n++) {
      	// save only position key frames
        if (steps[n] == null || !tape.keyFrames.contains(n)) continue;
        data[n] = new FrameData((TapeStep)steps[n]);
      }
      control.setValue("framedata", data); //$NON-NLS-1$
      // save step world lengths
      count = tape.isFixedLength()? 1: steps.length;
      Double[] lengths = new Double[count];
      for (int n = 0; n < count; n++) {
      	// save only length key frames
        if (steps[n] == null || !tape.lengthKeyFrames.contains(n)) continue;
        lengths[n] = ((TapeStep)steps[n]).worldLength;
      }
      control.setValue("worldlengths", lengths); //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new TapeMeasure();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      TapeMeasure tape = (TapeMeasure)obj;
      tape.notYetShown = false;
      boolean locked = tape.isLocked();
      tape.setLocked(false);
      // load stickMode to set up footprint choices
      tape.setStickMode(control.getBoolean("stickmode")); //$NON-NLS-1$
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      // load position data
      tape.keyFrames.clear();
    	FrameData[] data = (FrameData[])control.getObject("framedata"); //$NON-NLS-1$
    	if (data!=null) {
	      for (int n = 0; n < data.length; n++) {
	        if (data[n] == null) continue;
	        tape.createStep(n, data[n].data[0], data[n].data[1], data[n].data[2], data[n].data[3]);
	      }
    	}
      // load world lengths
      tape.lengthKeyFrames.clear();
    	Double[] lengths = (Double[])control.getObject("worldlengths"); //$NON-NLS-1$
    	if (lengths!=null) {
	      for (int n = 0; n < lengths.length; n++) {
	        if (lengths[n] == null) continue;
	        TapeStep step = (TapeStep)tape.steps.getStep(n);
	        step.worldLength = lengths[n];
	        tape.lengthKeyFrames.add(n);
	      }
    	}
      // load fixed position
      tape.fixedPosition = control.getBoolean("fixedtape"); //$NON-NLS-1$
      // load fixed length
      if (control.getPropertyNames().contains("fixedlength")) //$NON-NLS-1$
      	tape.fixedLength = control.getBoolean("fixedlength"); //$NON-NLS-1$
      // load readOnly
      tape.setReadOnly(control.getBoolean("readonly")); //$NON-NLS-1$
      tape.setLocked(locked);
      tape.displayState();
      return obj;
    }
  }

  /**
   * Inner class containing the tape data for a single frame number.
   * This is here only to read legacy xml files.
   */
  private static class FrameData {
    double[] data = new double[4];
    
    FrameData() {}
    FrameData(TapeStep step) {
      data[0] = step.getEnd1().x;
      data[1] = step.getEnd1().y;
      data[2] = step.getEnd2().x;
      data[3] = step.getEnd2().y;
    }
  }

  /**
   * A class to save and load a FrameData.
   */
  private static class FrameDataLoader
      implements XML.ObjectLoader {

    public void saveObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      control.setValue("x1", data.data[0]); //$NON-NLS-1$
      control.setValue("y1", data.data[1]); //$NON-NLS-1$
      control.setValue("x2", data.data[2]); //$NON-NLS-1$
      control.setValue("y2", data.data[3]); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new FrameData();
    }

    public Object loadObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      if (control.getPropertyNames().contains("x1")) { //$NON-NLS-1$
	      data.data[0] = control.getDouble("x1"); //$NON-NLS-1$
	      data.data[1] = control.getDouble("y1"); //$NON-NLS-1$
	      data.data[2] = control.getDouble("x2"); //$NON-NLS-1$
	      data.data[3] = control.getDouble("y2"); //$NON-NLS-1$
      }
      return obj;
    }
  }
}

