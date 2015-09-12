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

import javax.swing.*;
import javax.swing.border.Border;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

/**
 * A LineProfile measures pixel brightness along a line on a video image.
 *
 * @author Douglas Brown
 */
public class LineProfile extends TTrack {

  // static constants
  /** The maximum allowed spread */
  public static final int MAX_SPREAD = 100;

  // instance fields
  protected boolean fixedLine = true; // line is the same at all times
  protected JCheckBoxMenuItem fixedLineItem;
  protected JMenu orientationMenu;
  protected JMenuItem horizOrientationItem;
  protected JMenuItem xaxisOrientationItem;
  protected int spread = 0;
  protected JLabel spreadLabel;
  protected IntegerField spreadField;
  protected boolean isHorizontal = true;
  protected boolean loading;

  /**
   * Constructs a LineProfile.
   */
  public LineProfile() {
    super();
		defaultColors = new Color[] {Color.magenta};
    // assign a default name
    setName(TrackerRes.getString("LineProfile.New.Name")); //$NON-NLS-1$
    // assign default plot variables
    setProperty("highlights", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("xVarPlot0", "x"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot0", "luma"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("pointsPlot0", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yMinPlot0", new Double(0)); //$NON-NLS-1$
    setProperty("yMaxPlot0", new Double(255)); //$NON-NLS-1$
    // assign default table variables: x, y and luma
    setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("tableVar2", "5"); //$NON-NLS-1$ //$NON-NLS-2$
    // set up footprint choices and color
    setFootprints(new Footprint[]
      {LineFootprint.getFootprint("Footprint.Outline"), //$NON-NLS-1$
       LineFootprint.getFootprint("Footprint.BoldOutline")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setColor(defaultColors[0]);
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    hint = TrackerRes.getString("LineProfile.Unmarked.Hint"); //$NON-NLS-1$
    // create toolbar components
    spreadLabel = new JLabel();
    spreadField = new IntegerField(3);
    spreadField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setSpread(spreadField.getIntValue());
        spreadField.setIntValue(getSpread());
        spreadField.selectAll();
        spreadField.requestFocusInWindow();
        firePropertyChange("data", null, LineProfile.this); // to views //$NON-NLS-1$
      }
    });
    spreadField.addFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        spreadField.selectAll();
      }
      public void focusLost(FocusEvent e) {
        setSpread(spreadField.getIntValue());
        spreadField.setIntValue(getSpread());
        firePropertyChange("data", null, LineProfile.this); // to views //$NON-NLS-1$
      }
    });
    spreadField.setBorder(fieldBorder);
    // create fixed line item
    fixedLineItem = new JCheckBoxMenuItem(TrackerRes.getString("LineProfile.MenuItem.Fixed")); //$NON-NLS-1$
    fixedLineItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setFixed(fixedLineItem.isSelected());
      }
    });
    // create orientation items
    orientationMenu = new JMenu(TrackerRes.getString("LineProfile.Menu.Orientation")); //$NON-NLS-1$
    ButtonGroup group = new ButtonGroup();
    horizOrientationItem = new JRadioButtonMenuItem(
    		TrackerRes.getString("LineProfile.MenuItem.Horizontal")); //$NON-NLS-1$
    horizOrientationItem.setSelected(true);
    horizOrientationItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
      	if (trackerPanel == null) return;
      	XMLControl control = new XMLControlElement(LineProfile.this);
        isHorizontal = horizOrientationItem.isSelected();
        if (!steps.isEmpty()) {
	        int n = trackerPanel.getFrameNumber();
	        LineProfileStep step = (LineProfileStep)steps.getStep(n);
	        refreshStep(step);
	        trackerPanel.repaint();
	        if (!loading)
	        	Undo.postTrackEdit(LineProfile.this, control);
        }
        trackerPanel.getTFrame().getToolBar(trackerPanel).refresh(false);
        dataValid = false;
        support.firePropertyChange("data", null, null); // to views //$NON-NLS-1$
      }
    });
    orientationMenu.add(horizOrientationItem);
    group.add(horizOrientationItem);
    xaxisOrientationItem = new JRadioButtonMenuItem(
    		TrackerRes.getString("LineProfile.MenuItem.XAxis")); //$NON-NLS-1$
    orientationMenu.add(xaxisOrientationItem);
    group.add(xaxisOrientationItem);
  }

  /**
   * Sets the fixed property. When it is fixed, it is in the same
   * position at all times.
   *
   * @param fixed <code>true</code> to fix the line
   */
  public void setFixed(boolean fixed) {
  	if (fixed==fixedLine) return;
    if (steps.isEmpty()) {
      fixedLine = fixed;
    	return;
    }
  	XMLControl control = new XMLControlElement(this);
    fixedLine = fixed;
    if (trackerPanel != null) {
    	trackerPanel.changed = true;
      int n = trackerPanel.getFrameNumber();
      Step step = getStep(n);
      if (step!=null) {
	      steps = new StepArray(getStep(n));
	      trackerPanel.repaint();
      }
    }
    if (fixed) {
    	keyFrames.clear();
    	keyFrames.add(0);
    }
    if (!loading)
    	Undo.postTrackEdit(this, control);
  	repaint();
  }

  /**
   * Gets the fixed property.
   *
   * @return <code>true</code> if line is fixed
   */
  public boolean isFixed() {
    return fixedLine;
  }

  /**
   * Sets the spread. Spread determines how many pixels
   * on each side of the line are given full weight in the average.
   *
   * @param spread the desired spread
   */
  public void setSpread(int spread) {
    if (isLocked() || this.spread==spread) return;
  	XMLControl control = new XMLControlElement(this);
    spread = Math.max(spread, 0);
    this.spread = Math.min(spread, MAX_SPREAD);
    if (!loading)
    	Undo.postTrackEdit(this, control);
    repaint();
  	dataValid = false;
  }

  /**
   * Gets the spread. Spread determines how many pixels
   * on each side of the line are given full weight in the average.
   *
   * @return the spread
   */
  public int getSpread() {
    return spread;
  }

  /**
   * Overrides TTrack draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    super.draw(panel, _g);
  }

  /**
   * Overrides TTrack setTrailVisible method to keep trails hidden.
   *
   * @param visible ignored
   */
  public void setTrailVisible(boolean visible) {/** empty block */}

  /**
   * Creates a new step.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the step
   */
  public Step createStep(int n, double x, double y) {
  	return createStep(n, x, y, x, y);
  }

  /**
   * Creates a new step or sets end positions of an existing step.
   *
   * @param n the frame number
   * @param x1 the x coordinate of end1 in image space
   * @param y1 the y coordinate of end1 in image space
   * @param x2 the x coordinate of end2 in image space
   * @param y2 the y coordinate of end2 in image space
   * @return the step
   */
  public Step createStep(int n, double x1, double y1, double x2, double y2) {
    if (isLocked()) return null;
  	int frame = isFixed()? 0: n;
    LineProfileStep step = (LineProfileStep)steps.getStep(frame);
    if (step == null) { 
      keyFrames.add(0);
    	// create new step 0 and autofill array
    	double xx = x2, yy = y2;
    	if (x1 == x2 && y1 == y2) { // occurs when initially mouse-marked
    		// make a step of length 50 for the step array to clone
    		if (trackerPanel != null) {
    			double theta = -trackerPanel.getCoords().getAngle(n);
    			if (isHorizontal) theta = 0;
    			xx = x1+50*Math.cos(theta);
    			yy = y1+50*Math.sin(theta);
    		}
    		else xx = x1 + 50;
    	}
    	step = new LineProfileStep(this, 0, x1, y1, xx, yy);
      step.setFootprint(getFootprint());
      steps = new StepArray(step);
      // set location of line ends
    	if (x1 == x2 && y1 == y2) { // mouse-marked step    		
    		step = (LineProfileStep)getStep(frame);
        step.getLineEnd1().setLocation(x2, y2);
        if (trackerPanel != null) {
      		step = (LineProfileStep)getStep(n);
          step.getLineEnd0().setTrackEditTrigger(false);
        	trackerPanel.setSelectedPoint(step.getDefaultPoint());
        }
    	}
    }
    else {
      keyFrames.add(frame);
      step.getLineEnd0().setLocation(x1, y1);
      step.getLineEnd1().setLocation(x2, y2);    	
    }
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
   * Overrides TTrack getStep method to provide fixedLine behavior.
   *
   * @param n the frame number
   * @return the step
   */
  public Step getStep(int n) {
    LineProfileStep step = (LineProfileStep)steps.getStep(n);
    refreshStep(step);
    return step;
  }

  /**
   * Returns true if the step at the specified frame number is complete.
   *
   * @param n the frame number
   * @return <code>true</code> if the step is complete, otherwise false
   */
  public boolean isStepComplete(int n) {
    return getStep(n) != null;
  }
  
  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return LineProfileStep.getLength();
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
   * Implements findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step or motion vector that is hit
   */
  public Interactive findInteractive(
    DrawingPanel panel, int xpix, int ypix) {
    if (!(panel instanceof TrackerPanel) || !isVisible() || isLocked())
      return null;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Interactive ia = null;
    int n = trackerPanel.getFrameNumber();
    Step step = getStep(n);
    if (step != null &&
        trackerPanel.getPlayer().getVideoClip().includesFrame(n))
      ia = step.findInteractive(trackerPanel, xpix, ypix);
    if (ia == null) {
    	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
      if (step==null) {
      	hint = TrackerRes.getString("LineProfile.Unmarked.Hint"); //$NON-NLS-1$
      }
      else hint = TrackerRes.getString("LineProfile.Hint"); //$NON-NLS-1$
      if (trackerPanel.getVideo() == null) {
      	hint += ", "+TrackerRes.getString("TTrack.ImportVideo.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
      }
    	return null;
    }
    if (ia instanceof LineProfileStep.LineEnd) {
      partName = TrackerRes.getString("LineProfile.End.Name"); //$NON-NLS-1$
      hint = TrackerRes.getString("LineProfile.End.Hint"); //$NON-NLS-1$
    }
    else if (ia instanceof LineProfileStep.Handle) {
      partName = TrackerRes.getString("LineProfile.Handle.Name"); //$NON-NLS-1$
      hint = TrackerRes.getString("LineProfile.Handle.Hint"); //$NON-NLS-1$
    }
    return ia;
  }

  /**
   * Refreshes the data.
   *
   * @param data the DatasetManager
   * @param trackerPanel the tracker panel
   */
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
    // get the datasets
    int count = 0;
    Dataset x = data.getDataset(count++);
    Dataset y = data.getDataset(count++);
    Dataset r = data.getDataset(count++);
    Dataset g = data.getDataset(count++);
    Dataset b = data.getDataset(count++);
    Dataset luma = data.getDataset(count++);
    Dataset w = data.getDataset(count++);
    // assign column names to the datasets
    String pixelNum = "n"; //$NON-NLS-1$
    if (!x.getColumnName(0).equals(pixelNum)) { // not yet initialized
	    x.setXYColumnNames(pixelNum, "x"); //$NON-NLS-1$
	    y.setXYColumnNames(pixelNum, "y"); //$NON-NLS-1$
	    r.setXYColumnNames(pixelNum, "R"); //$NON-NLS-1$
	    g.setXYColumnNames(pixelNum, "G"); //$NON-NLS-1$
	    b.setXYColumnNames(pixelNum, "B"); //$NON-NLS-1$
	    luma.setXYColumnNames(pixelNum, "luma"); //$NON-NLS-1$
	    w.setXYColumnNames(pixelNum, "pixels"); //$NON-NLS-1$
    }
    else for (int i = 0; i < count; i++) {
    	data.getDataset(i).clear();
    }
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < dataDescriptions.length; i++) {
      dataDescriptions[i] = TrackerRes.getString("LineProfile.Data.Description."+i); //$NON-NLS-1$
    }
    // get data from current line profile step if video is visible
    if (trackerPanel.getVideo() != null &&
        trackerPanel.getVideo().isVisible()) {
      int n = trackerPanel.getPlayer().getFrameNumber();
      LineProfileStep step = (LineProfileStep)getStep(n);
      if (step != null) {
        double[][] profile = step.getProfileData(trackerPanel);
        // append the data to the data set
        if (profile != null) {
          for (int i = 0; i < profile[0].length; i++) {
            x.append(i, profile[0][i]);
            y.append(i, profile[1][i]);
            r.append(i, profile[2][i]);
            g.append(i, profile[3][i]);
            b.append(i, profile[4][i]);
            luma.append(i, profile[5][i]);
            w.append(i, profile[6][i]);
          }
        }
      }
    }
  }

  /**
   * Overrides TTrack getMenu method.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
    fixedLineItem.setText(TrackerRes.getString("LineProfile.MenuItem.Fixed")); //$NON-NLS-1$
    fixedLineItem.setSelected(isFixed());
    menu.remove(deleteTrackItem);
    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
      menu.addSeparator();
    menu.add(orientationMenu);
    menu.addSeparator();
    menu.add(fixedLineItem);
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
   * @return a collection of components
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
    ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    spreadLabel.setText(TrackerRes.getString("LineProfile.Label.Spread")); //$NON-NLS-1$
    Border empty = BorderFactory.createEmptyBorder(0, 4, 0, 2);
    spreadLabel.setBorder(empty);
    list.add(spreadLabel);
    spreadField.setIntValue(getSpread());
    spreadField.setEnabled(!isLocked());
    list.add(spreadField);
    return list;
  }

  @Override
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	Object[] objectsToSize = new Object[]
  			{spreadLabel};
    FontSizer.setFonts(objectsToSize, level);
  }

  /**
   * Responds to property change events. LineProfile listens for the following
   * events: "stepnumber", "image" and "transform" from TrackerPanel.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
  	if (trackerPanel != null) {
      String name = e.getPropertyName();
      if (name.equals("stepnumber")) { //$NON-NLS-1$
      	dataValid = false;
        support.firePropertyChange(e); // to views
      }
      else if (name.equals("image")) { //$NON-NLS-1$
      	dataValid = false;
        support.firePropertyChange(e); // to views
      }
      else if (name.equals("transform") && !steps.isEmpty()) { //$NON-NLS-1$
    		int n = trackerPanel.getFrameNumber();
        LineProfileStep step = (LineProfileStep)steps.getStep(n);
        refreshStep(step);
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
    return TrackerRes.getString("LineProfile.Name"); //$NON-NLS-1$
  }

//_______________________ private and protected methods _______________________

  /**
   * Refreshes a step by setting it equal to a keyframe step.
   *
   * @param step the step to refresh
   */
  protected void refreshStep(LineProfileStep step) {
  	if (step==null) return;
  	int key = 0;
  	for (int i: keyFrames) {
  		if (i<=step.n)
  			key = i;
  	}
  	// compare step with keyStep
  	LineProfileStep keyStep = (LineProfileStep)steps.getStep(key);
    boolean different = 
    			 keyStep.getLineEnd0().getX()!=step.getLineEnd0().getX()
		    || keyStep.getLineEnd0().getY()!=step.getLineEnd0().getY()
		    || keyStep.getLineEnd1().getX()!=step.getLineEnd1().getX()
		    || keyStep.getLineEnd1().getY()!=step.getLineEnd1().getY();
    // update step if needed
    if (different) {
	    step.getLineEnd0().setLocation(keyStep.getLineEnd0());
	    step.getLineEnd1().setLocation(keyStep.getLineEnd1());
	    step.getHandle().setLocation(keyStep.getHandle());
	    step.erase();
    }
    step.getLineEnd0().setTrackEditTrigger(true);
    step.rotate();
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
      LineProfile profile = (LineProfile) obj;
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save spread
      control.setValue("spread", profile.getSpread()); //$NON-NLS-1$
      // save fixed
      control.setValue("fixed", profile.isFixed()); //$NON-NLS-1$
      // save step data
      Step[] steps = profile.getSteps();
      int count = steps.length;
      if (profile.isFixed()) count = 1;
      FrameData[] data = new FrameData[count];
      for (int n = 0; n < count; n++) {
      	// save only key frames
        if (steps[n] == null || !profile.keyFrames.contains(n)) continue;
        data[n] = new FrameData((LineProfileStep)steps[n]);
      }
      control.setValue("framedata", data); //$NON-NLS-1$
      // save orientation
      control.setValue("horizontal", profile.isHorizontal); //$NON-NLS-1$
    }

    /**
     * Creates a new object with data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
    	LineProfile profile = new LineProfile();
      return profile;
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	LineProfile profile = (LineProfile) obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      boolean locked = profile.isLocked();
      profile.setLocked(false);
      profile.loading = true;
      // load orientation
      if (control.getPropertyNames().contains("horizontal")) //$NON-NLS-1$
      	profile.isHorizontal = control.getBoolean("horizontal"); //$NON-NLS-1$
      else profile.isHorizontal = !control.getBoolean("rotates"); //$NON-NLS-1$
      if (profile.isHorizontal)
      	profile.horizOrientationItem.setSelected(true);
      else profile.xaxisOrientationItem.setSelected(true);
      // load spread
      int i = control.getInt("spread"); //$NON-NLS-1$
      if (i != Integer.MIN_VALUE) {
        profile.setSpread(i);
      }
      // load fixed before steps data
      if (control.getPropertyNames().contains("fixed")) //$NON-NLS-1$
      	profile.fixedLine = control.getBoolean("fixed"); //$NON-NLS-1$
      // load step data
      profile.keyFrames.clear();
      FrameData[] data = (FrameData[])control.getObject("framedata"); //$NON-NLS-1$
      if (data != null) {
        for (int n = 0; n < data.length; n++) {
          if (data[n] != null) {
            profile.createStep(n, data[n].data[0], data[n].data[1], data[n].data[2], data[n].data[3]);
          }
        }
      }
      profile.spreadField.setIntValue(profile.getSpread());
      profile.setLocked(locked);
      profile.loading = false;
      profile.repaint();
      return obj;
    }
  }

  /**
   * Inner class containing the profile data for a single frame number.
   */
  private static class FrameData {
    double[] data = new double[4];    
    FrameData() {}
    FrameData(LineProfileStep step) {
      data[0] = step.getLineEnd0().x;
      data[1] = step.getLineEnd0().y;
      data[2] = step.getLineEnd1().x;
      data[3] = step.getLineEnd1().y;
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

