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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.border.Border;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * A Protractor measures and displays an angular arc.
 *
 * @author Douglas Brown
 */
public class Protractor extends TTrack {
	
  // instance fields
  protected boolean fixedPosition = true;
  protected JCheckBoxMenuItem fixedItem;
  protected JMenuItem attachmentItem;
  protected boolean editing = false;
  protected final DecimalField inputField;
  protected JPanel inputPanel;
  protected JPanel glassPanel;
  protected NumberFormat format;
  protected MouseListener editListener;
	
  /**
   * Constructs a Protractor.
   */
  public Protractor() {
		defaultColors = new Color[] {new Color(0, 140, 40)};
    // assign a default name
    setName(TrackerRes.getString("Protractor.New.Name")); //$NON-NLS-1$
    // set up footprint choices and color
    setFootprints(new Footprint[]
        {ProtractorFootprint.getFootprint("ProtractorFootprint.Circle3"), //$NON-NLS-1$
    		ProtractorFootprint.getFootprint("ProtractorFootprint.Circle5"), //$NON-NLS-1$
    		ProtractorFootprint.getFootprint("ProtractorFootprint.Circle3Bold"), //$NON-NLS-1$
    		ProtractorFootprint.getFootprint("ProtractorFootprint.Circle5Bold")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setColor(defaultColors[0]);
    
    // assign default table variables
    setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    
    keyFrames.add(0);
    // create input field and panel
    inputField = new DecimalField(4, 1);
    inputField.setBorder(null);

    format = inputField.getFormat();
    inputPanel = new JPanel(null);
    inputPanel.setOpaque(false);
    inputPanel.add(inputField);
    // add inputField action listener to exit editing mode
    inputField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (editing) {
          int n = trackerPanel.getFrameNumber();
          ProtractorStep step = ((ProtractorStep)getStep(n));
          setEditing(false, step);
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
          ProtractorStep step = ((ProtractorStep)getStep(n));
          setEditing(false, step);
        }
      }
    });
    // add mouse listener to toggle editing mode
    editListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (editing) {
          int n = trackerPanel.getFrameNumber();
          ProtractorStep tape = (ProtractorStep)getStep(n);
          setEditing(false, tape);
        }
      }
      public void mouseClicked(MouseEvent e) {
        if (isLocked()) return;
        int n = trackerPanel.getFrameNumber();
        ProtractorStep step = (ProtractorStep)getStep(n);
        Rectangle bounds = step.layoutBounds.get(trackerPanel);
        if (bounds != null &&
            bounds.contains(e.getPoint())) {
          setEditing(true, step);
        }
      }
    };
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("Protractor.Hint"); //$NON-NLS-1$
    // initialize the autofill step array
    ProtractorStep step = new ProtractorStep(this, 0, 100, 150, 200, 150);
    step.setFootprint(getFootprint());
    steps = new StepArray(step); // autofills
    fixedItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
    fixedItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixed(fixedItem.isSelected());
      }
    });
    final FocusListener arcFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (angleField.getBackground() == Color.yellow) {
	        int n = trackerPanel.getFrameNumber();
	        ProtractorStep step = (ProtractorStep)getStep(n);
	        if (!isFixed()) {
	        	keyFrames.add(n);
	        }
	        step = getKeyStep(step);
	        double theta = angleField.getValue();
	        step.setProtractorAngle(theta);
          dataValid = false;
  	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
      	}
      }
    };
    angleField.addFocusListener(arcFocusListener);
    angleField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	arcFocusListener.focusLost(null);
      	angleField.requestFocusInWindow();
      }
    });
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
      steps = new StepArray(getStep(n));
      trackerPanel.repaint();
    }
    fixedPosition = fixed;
    if (fixed) { // refresh data and post undo only when fixing
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
   * Responds to property change events. Overrides TTrack method.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();    
    if (trackerPanel.getSelectedTrack() == this) {
      if (name.equals("stepnumber")) { //$NON-NLS-1$
        ProtractorStep step = (ProtractorStep)getStep(trackerPanel.getFrameNumber());     
  	    step.getProtractorAngle(); // refreshes angle field
  	    step.arcHighlight = null;
      }
      else if (name.equals("selectedpoint")) { //$NON-NLS-1$
      	TPoint p = trackerPanel.getSelectedPoint();
      	if (!(p instanceof ProtractorStep.Rotator)) {
    	    ProtractorStep step = (ProtractorStep)getStep(trackerPanel.getFrameNumber());     
    	    step.arcHighlight = null;
      	}
      }
    }
    if (name.equals("adjusting") && e.getSource() instanceof TrackerPanel) { //$NON-NLS-1$
			refreshDataLater = (Boolean)e.getNewValue();
			if (!refreshDataLater) {  // stopped adjusting
	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
			}
    }
    super.propertyChange(e);
  }

  /**
   * Overrides TTrack setTrailVisible method to keep trails hidden.
   *
   * @param visible ignored
   */
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
  public Step createStep(int n, double x, double y) {
    Step step = steps.getStep(n);
    ((ProtractorStep)step).end1.setLocation(x, y);
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
    ProtractorStep step = (ProtractorStep)steps.getStep(n);
    step.end1.setLocation(x1, y1);
    step.end2.setLocation(x2, y2);
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
  	setFixed(false);
  	ProtractorStep step = (ProtractorStep)steps.getStep(n);
  	int i = getTargetIndex();
  	if (i==0) {
	    step.vertex.setLocation(x, y); 		  		
  	}
  	else if (i==1) {
	    step.end1.setLocation(x, y); 		
  	}
  	else {
	    step.end2.setLocation(x, y); 		  		
  	}
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
    ProtractorStep step = (ProtractorStep)steps.getStep(n);
		refreshStep(step);
    return step;
  }

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return ProtractorStep.getLength();
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
   * Determines if the given point index is autotrackable.
   *
   * @param pointIndex the points[] index
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable(int pointIndex) {
  	return pointIndex<3;
  }
  
  /**
   * Returns a description of a target point with a given index.
   *
   * @param pointIndex the index
   * @return the description
   */
  protected String getTargetDescription(int pointIndex) {
  	if (pointIndex==0) return TrackerRes.getString("Protractor.Vertex.Name"); //$NON-NLS-1$
  	String s = TrackerRes.getString("Protractor.End.Name"); //$NON-NLS-1$
  	return s+" "+(pointIndex); //$NON-NLS-1$
  }

  /**
   * Gets the length of the footprints required by this track.
   *
   * @return the footprint length
   */
  public int getFootprintLength() {
    return 3;
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
    Dataset theta = data.getDataset(count++);
    Dataset arm1Length = data.getDataset(count++);
    Dataset arm2Length = data.getDataset(count++);
    Dataset stepNum = data.getDataset(count++);
    Dataset frameNum = data.getDataset(count++);
    // assign column names to the datasets
    String time = "t"; //$NON-NLS-1$
    if (!theta.getColumnName(0).equals(time)) { // not yet initialized
    	theta.setXYColumnNames(time, Tracker.THETA);
    	arm1Length.setXYColumnNames(time, "L_{1}"); //$NON-NLS-1$
    	arm2Length.setXYColumnNames(time, "L_{2}"); //$NON-NLS-1$
	    stepNum.setXYColumnNames(time, "step"); //$NON-NLS-1$
	    frameNum.setXYColumnNames(time, "frame"); //$NON-NLS-1$
    }
    else for (int i = 0; i < count; i++) {
    	data.getDataset(i).clear();
    }
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < dataDescriptions.length; i++) {
      dataDescriptions[i] = TrackerRes.getString("Protractor.Data.Description."+i); //$NON-NLS-1$
    }
    // look thru steps and get data for those included in clip
    VideoPlayer player = trackerPanel.getPlayer();
    VideoClip clip = player.getVideoClip();
	  int len = clip.getStepCount();
	  double[][] validData = new double[data.getDatasets().size()+1][len];
    for (int n = 0; n < len; n++) {
      int frame = clip.stepToFrame(n);
      ProtractorStep next = (ProtractorStep)getStep(frame);
      next.dataVisible = true;
	    // get the step number and time
	    double t = player.getStepTime(n)/1000.0;
			validData[0][n] = t;
			validData[1][n] = next.getProtractorAngle();
			validData[2][n] = next.getArmLength(next.end1);
			validData[3][n] = next.getArmLength(next.end2);
			validData[4][n] = n;
			validData[5][n] = frame;
      dataFrames.add(frame);
    }
    // append the data to the data set
	  theta.append(validData[0], validData[1]);
	  arm1Length.append(validData[0], validData[2]);
	  arm2Length.append(validData[0], validData[3]);
    stepNum.append(validData[0], validData[4]);
    frameNum.append(validData[0], validData[5]);
  }

  /**
   * Returns a menu with items that control this track.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
        
//    lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());
    fixedItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
    fixedItem.setSelected(isFixed());
    fixedItem.setEnabled(attachments==null || (attachments[0]==null && attachments[1]==null && attachments[2]==null));

    // remove end items and last separator
    menu.remove(deleteTrackItem);
    menu.remove(menu.getMenuComponent(menu.getMenuComponentCount()-1));
    menu.add(fixedItem);
    
    // add an attachment dialog item
  	attachmentItem = new JMenuItem(TrackerRes.getString("MeasuringTool.MenuItem.Attach")); //$NON-NLS-1$
  	attachmentItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	AttachmentDialog control = Protractor.this.trackerPanel.getAttachmentDialog(Protractor.this);
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
    list.add(stepSeparator);
  	angleLabel.setText(TrackerRes.getString("Protractor.Label.Angle")); //$NON-NLS-1$
		angleField.setToolTipText(TrackerRes.getString("Protractor.Field.Angle.Tooltip")); //$NON-NLS-1$
		angleField.setEnabled(!isLocked());
    list.add(angleLabel);
    list.add(angleField);
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
    ProtractorStep step = (ProtractorStep)getStep(n);
    if (trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
      Interactive ia = step.findInteractive(trackerPanel, xpix, ypix);
      if (ia == null) {
      	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
        hint = TrackerRes.getString("Protractor.Hint"); //$NON-NLS-1$
      	return null;
      }
      if (ia==step.vertex) {
        partName = TrackerRes.getString("Protractor.Vertex.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("Protractor.Vertex.Hint"); //$NON-NLS-1$
      }
      else if (ia instanceof ProtractorStep.Tip) {
        partName = TrackerRes.getString("Protractor.End.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("Protractor.End.Hint"); //$NON-NLS-1$
      }
      else if (ia==step.handle) {
        partName = TrackerRes.getString("Protractor.Handle.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("Protractor.Handle.Hint"); //$NON-NLS-1$
      }
      else if (ia==step.rotator) {
        partName = TrackerRes.getString("Protractor.Rotator.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("Protractor.Rotator.Hint"); //$NON-NLS-1$
      }
      else if (ia==this) {
        partName = TrackerRes.getString("Protractor.Readout.Name"); //$NON-NLS-1$
        hint = TrackerRes.getString("Protractor.Readout.Hint"); //$NON-NLS-1$
      	trackerPanel.setMessage(getMessage());
      }      
      return ia;
    }
    return null;
  }

  /**
   * Overrides Object toString method.
   *
   * @return the name of this track
   */
  public String toString() {
    return TrackerRes.getString("Protractor.Name"); //$NON-NLS-1$
  }

//__________________________ protected methods ________________________
  
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
  		trackerPanel.addPropertyChangeListener("stepnumber", this); //$NON-NLS-1$
  	}
    setFixed(isFixed());
  }

  /**
   * Overrides TTrack method.
   *
   * @param radians <code>true</code> for radians, false for degrees
   */
  @Override
  protected void setAnglesInRadians(boolean radians) {  	
    super.setAnglesInRadians(radians);
    inputField.setDecimalPlaces(radians? 3: 1);
    inputField.setConversionFactor(radians? 1.0: 180/Math.PI);
    ProtractorStep step = (ProtractorStep)getStep(trackerPanel.getFrameNumber());     
    step.repaint(); // refreshes angle readout
  }

  /**
   * Refreshes a step by setting it equal to a keyframe step.
   *
   * @param step the step to refresh
   */
  protected void refreshStep(ProtractorStep step) {
  	// compare step with keyStep
  	ProtractorStep keyStep = getKeyStep(step);
  	boolean different = 
  				 keyStep.vertex.getX()!=step.vertex.getX()
  			|| keyStep.vertex.getY()!=step.vertex.getY()
  			|| keyStep.end1.getX()!=step.end1.getX()
  			|| keyStep.end1.getY()!=step.end1.getY()
  			|| keyStep.end2.getX()!=step.end2.getX()
  			|| keyStep.end2.getY()!=step.end2.getY();
    // update step if needed
    if (different) {
	    step.vertex.setLocation(keyStep.vertex);
	    step.end1.setLocation(keyStep.end1);
	    step.end2.setLocation(keyStep.end2);
	    step.erase();
    }
  }
  
  /**
   * Returns the key step for a given step.
   * @param step the step
   * @return the key step
   */
  private ProtractorStep getKeyStep(ProtractorStep step) {
  	int key = 0;
  	if (!this.isFixed()) {
	  	for (int i: keyFrames) {
	  		if (i<=step.n)
	  			key = i;
	  	}
  	}
  	return (ProtractorStep)steps.getStep(key);
  }
  


  /**
   * Sets the editing flag.
   *
   * @param edit <code>true</code> to edit the angle
   * @param target the step that handles the edit process
   */
  private void setEditing(boolean edit, ProtractorStep target) {
    editing = edit;
    if (!editing) {
    	// if not fixed, add target frame to key frames
      if (!isFixed()) 
      	keyFrames.add(target.n);
      // replace target with key frame step
      target = getKeyStep(target);
    }
    final ProtractorStep step = target;
    Runnable runner = new Runnable() {
      public void run() {
        if (editing) {
        	trackerPanel.setSelectedTrack(Protractor.this);
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
          inputField.setValue(step.getProtractorAngle());
          trackerPanel.revalidate();
          trackerPanel.repaint();
          inputField.requestFocus();
        }
        else { // end editing
        	step.drawLayoutBounds = false;
          step.setProtractorAngle(inputField.getValue());
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
      Protractor protractor = (Protractor)obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save fixed property
      control.setValue("fixed", protractor.isFixed()); //$NON-NLS-1$
      // save steps
      Step[] steps = protractor.getSteps();
      int count = steps.length;
      if (protractor.isFixed()) count = 1;
      double[][] data = new double[count][];
      for (int n = 0; n < count; n++) {
      	// save only key frames
        if (steps[n] == null || !protractor.keyFrames.contains(n)) continue;
        ProtractorStep pStep = (ProtractorStep)steps[n];
        double[] stepData = new double[6];
        stepData[0] = pStep.end1.getX();
        stepData[1] = pStep.end1.getY();
        stepData[2] = pStep.end2.getX();
        stepData[3] = pStep.end2.getY();
        stepData[4] = pStep.vertex.getX();
        stepData[5] = pStep.vertex.getY();
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
      return new Protractor();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	Protractor protractor = (Protractor)obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      boolean locked = protractor.isLocked();
      protractor.setLocked(false);
      // load fixed property
      protractor.fixedPosition = control.getBoolean("fixed"); //$NON-NLS-1$
      // load step data
      protractor.keyFrames.clear();
      double[][] data = (double[][])control.getObject("framedata"); //$NON-NLS-1$
      for (int n = 0; n < data.length; n++) {
        if (data[n] == null) continue;
        Step step = protractor.createStep(n, data[n][0], data[n][1], data[n][2], data[n][3]);
        ProtractorStep tapeStep = (ProtractorStep)step;
        // set vertex position
        tapeStep.vertex.setLocation(data[n][4], data[n][5]); 
        tapeStep.erase();
      }
      protractor.setLocked(locked);
      return obj;
    }
  }

}

