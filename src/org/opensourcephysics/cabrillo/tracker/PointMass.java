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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionPanel;
import org.opensourcephysics.controls.*;

/**
 * A PointMass tracks the position, velocity and acceleration of a
 * point mass.
 *
 * @author Douglas Brown
 */
public class PointMass extends TTrack {
	
	// static constants
	protected static final int FINITE_DIFF = 0;
	protected static final int BOUNCE_DETECT = 1;
	protected static final double MINIMUM_MASS = 1E-30;
	
	// static fields
  protected static Derivative vDeriv = new FirstDerivative();
  protected static Derivative aDeriv = new SecondDerivative();
  protected static BounceDerivatives bounceDerivs = new BounceDerivatives();

  // instance fields
  protected double mass;
  protected Footprint[] vFootprints;
  protected Footprint vFootprint = LineFootprint.getFootprint("Footprint.Arrow"); //$NON-NLS-1$
  protected Footprint[] aFootprints;
  protected Footprint aFootprint = LineFootprint.getFootprint("Footprint.Arrow"); //$NON-NLS-1$
  protected Map<TrackerPanel, StepArray> vMap  // trackerPanel to StepArray
  		= new HashMap<TrackerPanel, StepArray>();
  protected Map<TrackerPanel, StepArray> aMap  // trackerPanel to StepArray
  		= new HashMap<TrackerPanel, StepArray>();
  protected Map<TrackerPanel, Boolean> xVisMap  // trackerPanel to Boolean
  		= new HashMap<TrackerPanel, Boolean>();
  protected Map<TrackerPanel, Boolean> vVisMap  // trackerPanel to Boolean
  		= new HashMap<TrackerPanel, Boolean>();
  protected Map<TrackerPanel, Boolean> aVisMap  // trackerPanel to Boolean
  		= new HashMap<TrackerPanel, Boolean>();
  protected boolean xVisibleOnAll = false;
  protected boolean vVisibleOnAll = false;
  protected boolean aVisibleOnAll = false;
  // for derivatives
  protected int algorithm = FINITE_DIFF;
  protected int vDerivSpill = 1;
  protected int aDerivSpill = 2;
  protected int bounceDerivsSpill = 3;
  protected int[] params = new int[4];
  protected double[] xData = new double[5];
  protected double[] yData = new double[5];
  protected boolean[] validData = new boolean[5];
  protected Object[] derivData = new Object[] {params, xData, yData, validData};

  // for GUI
  protected JLabel massLabel;
  protected NumberField massField;
  protected Component mSeparator;
  protected JMenu velocityMenu;
  protected JMenu accelerationMenu;
  protected JMenuItem vColorItem;
  protected JMenuItem aColorItem;
  protected JMenu vFootprintMenu;
  protected JMenu aFootprintMenu;
  protected JMenuItem vTailsToOriginItem;
  protected JMenuItem vTailsToPositionItem;
  protected JMenuItem aTailsToOriginItem;
  protected JMenuItem aTailsToPositionItem;
  protected JMenuItem autotrackItem;
  protected JCheckBoxMenuItem vVisibleItem;
  protected JCheckBoxMenuItem aVisibleItem;
  protected boolean vAtOrigin, aAtOrigin;
  protected boolean traceVisible = false;
	protected GeneralPath trace = new GeneralPath();
	protected Stroke traceStroke = new BasicStroke(1);
	
  /**
   * Constructs a PointMass with mass 1.0.
   */
  public PointMass() {
    this(1);
  }

  /**
   * Constructs a PointMass with specified mass.
   *
   * @param mass the mass
   */
  public PointMass(double mass) {
    super();
		defaultColors = new Color[] {
				Color.red,Color.cyan,Color.magenta,new Color(153, 153, 255)};
    setFootprints(new Footprint[]
      {PointShapeFootprint.getFootprint("Footprint.Diamond"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.Triangle"), //$NON-NLS-1$
       CircleFootprint.getFootprint("CircleFootprint.Circle"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.VerticalLine"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.HorizontalLine"), //$NON-NLS-1$       
       PointShapeFootprint.getFootprint("Footprint.PositionVector"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.Spot"), //$NON-NLS-1$
    	 PointShapeFootprint.getFootprint("Footprint.BoldDiamond"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldTriangle"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldVerticalLine"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldHorizontalLine"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldPositionVector")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setVelocityFootprints(new Footprint[]
      {LineFootprint.getFootprint("Footprint.Arrow"), //$NON-NLS-1$
       LineFootprint.getFootprint("Footprint.BoldArrow"), //$NON-NLS-1$
       LineFootprint.getFootprint("Footprint.BigArrow")}); //$NON-NLS-1$
    setAccelerationFootprints(new Footprint[]
      {LineFootprint.getFootprint("Footprint.Arrow"), //$NON-NLS-1$
       LineFootprint.getFootprint("Footprint.BoldArrow"), //$NON-NLS-1$
       LineFootprint.getFootprint("Footprint.BigArrow")}); //$NON-NLS-1$
    // assign a default name
    setName(TrackerRes.getString("PointMass.New.Name")); //$NON-NLS-1$
    // assign default plot variables
    setProperty("xVarPlot0", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot0", "x"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("xVarPlot1", "t"); //$NON-NLS-1$ //$NON-NLS-2$
    setProperty("yVarPlot1", "y"); //$NON-NLS-1$ //$NON-NLS-2$
    // set the mass
    this.mass = Math.abs(mass);
    // make trail visible
    setTrailVisible(true);
    // turn on auto advance
    setAutoAdvance(true);
    hint = TrackerRes.getString("PointMass.Hint") //$NON-NLS-1$
    		+ TrackerRes.getString("PointMass.Unmarked.Hint"); //$NON-NLS-1$
    // create GUI components
    createGUI();
  }

  /**
   * Overrides TTrack setColor method.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
  	setVelocityColor(color);
  	setAccelerationColor(color);
    super.setColor(color);
  }

  /**
   * Sets the velocity color.
   *
   * @param color the desired color
   */
  public void setVelocityColor(Color color) {
    for (int i = 0; i < vFootprints.length; i++) {
      vFootprints[i].setColor(color);
    }
  }

  /**
   * Sets the acceleration color.
   *
   * @param color the desired color
   */
  public void setAccelerationColor(Color color) {
    for (int i = 0; i < aFootprints.length; i++) {
      aFootprints[i].setColor(color);
    }
  }

  /**
   * Creates a new position step.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the new step
   */
  public Step createStep(int n, double x, double y) {
    if (isLocked()) return null;
    boolean firstStep = steps.isEmpty();
    if (firstStep && trackerPanel!=null) { // only true when first marked
    	stepSizeWhenFirstMarked = trackerPanel.getPlayer().getVideoClip().getStepSize();
    }
    PositionStep step = (PositionStep)getStep(n);
    if (step==null) {
	    step = new PositionStep(this, n, x, y);
	    steps.setStep(n, step);
	    step.setFootprint(getFootprint());
    }
    else if (x!=step.getPosition().x || y!=step.getPosition().y) {
    	XMLControl state = new XMLControlElement(step);
    	step.getPosition().setLocation(x, y);
    	Undo.postStepEdit(step, state);
    	step.erase();
    }
    if (!autoTrackerMarking) {
	    updateDerivatives(n);
    }
	  support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
    // check independent point masses for skipped steps during marking
    if (skippedStepWarningOn && steps.isPreceded(n) && trackerPanel!=null && !isDependent()) {
    	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    	int stepNumber = clip.frameToStep(n);
    	if (stepNumber>0) {    		
    		Step prev = getStep(clip.stepToFrame(stepNumber-1));
    		if (prev==null) {
        	JDialog warning = getSkippedStepWarningDialog();
        	if (warning!=null)
        		warning.setVisible(true);    			
    		}
    	}
    }
    return step;
  }

  /**
   * Overrides TTrack deleteStep method.
   *
   * @param n the frame number
   * @return the deleted step
   */
  public Step deleteStep(int n) {
    Step step = super.deleteStep(n);
    if (step != null) updateDerivatives(n);
    AutoTracker autoTracker = trackerPanel.getAutoTracker();
    if (autoTracker.getTrack()==this)
    	autoTracker.delete(n);
    return step;
  }

  /**
   * Overrides TTrack getStep method.
   *
   * @param point a TPoint
   * @param trackerPanel the tracker panel
   * @return the step containing the TPoint
   */
  public Step getStep(TPoint point, TrackerPanel trackerPanel) {
  	if (point==null) return null;
    Step[] stepArray = null;
    for (int n = 0; n < 3; n++) {
      switch(n) {
        case 0: stepArray = steps.array; break;
        case 1: stepArray = getVelocities(trackerPanel); break;
        case 2: stepArray = getAccelerations(trackerPanel);
      }
      for (int j = 0; j < stepArray.length; j++)
        if (stepArray[j] != null) {
          TPoint[] points = stepArray[j].getPoints();
          for (int i = 0; i < points.length; i++)
            if (points[i] == point) return stepArray[j];
        }
    }
    return null;
  }

  /**
   * Gets next step after the specified step. May return null.
   *
   * @param step the step
   * @param trackerPanel the tracker panel
   * @return the next step
   */
  public Step getNextVisibleStep(Step step, TrackerPanel trackerPanel) {
    Step[] steps = getSteps();
    if (isVelocity(step)) steps = getVelocities(trackerPanel);
    if (isAcceleration(step)) steps = getAccelerations(trackerPanel);
    boolean found = false;
    for (int i = 0; i < steps.length; i++) {
      // return first step after found
      if (found && steps[i] != null &&
          isStepVisible(steps[i], trackerPanel)) return steps[i];
      // find specified step
      if (steps[i] == step) found = true;
    }
    // cycle back to beginning if next step not yet identified
    if (found) {
      for (int i = 0; i < steps.length; i++) {
        // return first visible step
        if (steps[i] != null && steps[i] != step &&
            isStepVisible(steps[i], trackerPanel))
          return steps[i];
      }
    }
    return null;
  }

  /**
   * Gets step before the specified step. May return null.
   *
   * @param step the step
   * @param trackerPanel the tracker panel
   * @return the previous step
   */
  public Step getPreviousVisibleStep(Step step, TrackerPanel trackerPanel) {
    Step[] steps = getSteps();
    if (isVelocity(step)) steps = getVelocities(trackerPanel);
    if (isAcceleration(step)) steps = getAccelerations(trackerPanel);
    boolean found = false;
    for (int i = steps.length-1; i > -1; i--) {
      // return first step after found
      if (found && steps[i] != null &&
          isStepVisible(steps[i], trackerPanel)) return steps[i];
      // find specified step
      if (steps[i] == step) found = true;
    }
    // cycle back to beginning if next step not yet identified
    if (found) {
      for (int i = steps.length-1; i > -1; i--) {
        // return first visible step
        if (steps[i] != null && steps[i] != step &&
            isStepVisible(steps[i], trackerPanel))
          return steps[i];
      }
    }
    return null;
  }

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return PositionStep.getLength();
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
   * Gets the velocity footprint choices.
   *
   * @return the velocity footprint choices
   */
  public Footprint[] getVelocityFootprints() {
    return vFootprints;
  }

  /**
   * Sets the velocity footprint choices.
   *
   * @param choices the velocity footprint choices
   */
  public void setVelocityFootprints(Footprint[] choices) {
    Collection<Footprint> valid = new ArrayList<Footprint>();
    for (int i = 0; i < choices.length; i++) {
      if (choices[i] != null &&
          choices[i].getLength() == vFootprint.getLength()) {
        choices[i].setColor(vFootprint.getColor());
        valid.add(choices[i]);
      }
    }
    if (valid.size() > 0) {
      vFootprints = valid.toArray(new Footprint[0]);
      setVelocityFootprint(vFootprints[0].getName());
    }
  }

  /**
   * Gets the current velocity footprint.
   *
   * @return the velocity footprint
   */
  public Footprint getVelocityFootprint() {
    return vFootprint;
  }

  /**
   * Sets the velocity footprint.
   *
   * @param name the name of the desired footprint
   */
  public void setVelocityFootprint(String name) {
    for (int i = 0; i < vFootprints.length; i++)
      if (name.equals(vFootprints[i].getName())) {
        vFootprint = vFootprints[i];
        Iterator<TrackerPanel> it = panels.iterator();
        while (it.hasNext()) {
          TrackerPanel panel = it.next();
          Step[] stepArray = getVArray(panel).array;
          for (int j = 0; j < stepArray.length; j++)
            if (stepArray[j] != null)
              stepArray[j].setFootprint(vFootprint);
        }
        support.firePropertyChange("footprint", null, vFootprint); //$NON-NLS-1$
        repaint();
        return;
      }
  }

  /**
   * Gets the acceleration footprint choices.
   *
   * @return the acceleration footprint choices
   */
  public Footprint[] getAccelerationFootprints() {
    return aFootprints;
  }

  /**
   * Sets the acceleration footprint choices.
   *
   * @param choices the acceleration footprint choices
   */
  public void setAccelerationFootprints(Footprint[] choices) {
    Collection<Footprint> valid = new ArrayList<Footprint>();
    for (int i = 0; i < choices.length; i++) {
      if (choices[i] != null &&
          choices[i].getLength() == aFootprint.getLength()) {
        choices[i].setColor(aFootprint.getColor());
        valid.add(choices[i]);
      }
    }
    if (valid.size() > 0) {
      aFootprints = valid.toArray(new Footprint[0]);
      setAccelerationFootprint(aFootprints[0].getName());
    }
  }

  /**
   * Gets the current acceleration footprint.
   *
   * @return the acceleration footprint
   */
  public Footprint getAccelerationFootprint() {
    return aFootprint;
  }

  /**
   * Sets the aceleration footprint.
   *
   * @param name the name of the desired footprint
   */
  public void setAccelerationFootprint(String name) {
    for (int i = 0; i < aFootprints.length; i++)
      if (name.equals(aFootprints[i].getName())) {
        aFootprint = aFootprints[i];
        Iterator<TrackerPanel> it = panels.iterator();
        while (it.hasNext()) {
          TrackerPanel panel = it.next();
          Step[] stepArray = getAArray(panel).array;
          for (int j = 0; j < stepArray.length; j++)
            if (stepArray[j] != null)
              stepArray[j].setFootprint(aFootprint);
        }
        repaint();
        support.firePropertyChange("footprint", null, aFootprint); //$NON-NLS-1$
        return;
      }
  }

  /**
   * Gets the footprint choices. Overrides TTrack.
   *
   * @param step the step that identifies the step array
   * @return the array of Footprints available
   */
  public Footprint[] getFootprints(Step step) {
    if (step == null) return getFootprints();
    else if (isVelocity(step)) return getVelocityFootprints();
    else if (isAcceleration(step)) return getAccelerationFootprints();
    return getFootprints();
  }

  /**
   * Sets the footprint to the specified choice. Overrides TTrack.
   *
   * @param name the name of the desired footprint
   * @param step the step that identifies the step array
   */
  public void setFootprint(String name, Step step) {
    if (step == null) setFootprint(name);
    else if (isVelocity(step)) setVelocityFootprint(name);
    else if (isAcceleration(step)) setAccelerationFootprint(name);
    else setFootprint(name);
  }

  /**
   * Gets the current footprint of the specified step. Overrides TTrack.
   *
   * @param step the step that identifies the step array
   * @return the footprint
   */
  public Footprint getFootprint(Step step) {
    if (step != null) return step.footprint;
    return getFootprint();
  }

  /**
   * Sets the marking flag. Flag should be true when ready to be marked by user.
   * 
   * @param marking true when marking
   */
  @Override
  protected void setMarking(boolean marking) {
  	super.setMarking(marking);
  	repaint(trackerPanel);
  }
  
  /**
   * Gets the mass.
   *
   * @return the mass
   */
  public double getMass() {
    return mass;
  }

  /**
   * Sets the mass.
   *
   * @param mass the mass
   */
  public void setMass(double mass) {
  	if (mass==this.mass) return;
  	mass = Math.abs(mass);
  	mass = Math.max(mass, MINIMUM_MASS);
    this.mass = mass;
    dataValid = false;
    firePropertyChange("mass", null, new Double(mass)); //$NON-NLS-1$
    firePropertyChange("data", null, PointMass.this); // to views //$NON-NLS-1$
    // store the mass in the data properties
    if (data != null) {
      Double m = getMass();
      String desc = TrackerRes.getString("ParticleModel.Parameter.Mass.Description"); //$NON-NLS-1$
      data.setConstant("m", m, m.toString(), desc); //$NON-NLS-1$
    }
  }

  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	Object[] objectsToSize = new Object[]
  			{massLabel, massField};
    FontSizer.setFonts(objectsToSize, level);
  }

  /**
   * Gets the world position for the specified frame number and panel.
   * May return null;
   *
   * @param n the frame number
   * @param trackerPanel the tracker panel
   * @return a Point2D containing the position components
   */
  public Point2D getWorldPosition(int n, TrackerPanel trackerPanel) {
    PositionStep step = (PositionStep)getStep(n);
    if (step != null) {
      return step.getPosition().getWorldPosition(trackerPanel);
    }
    return null;
  }

  /**
   * Gets the world velocity for the specified frame number and panel.
   * May return null;
   *
   * @param n the frame number
   * @param trackerPanel the tracker panel
   * @return a Point2D containing the velocity components
   */
  public Point2D getWorldVelocity(int n, TrackerPanel trackerPanel) {
    ImageCoordSystem coords = trackerPanel.getCoords();
    double dt = trackerPanel.getPlayer().getMeanStepDuration() / 1000.0;
    VectorStep veloc = getVelocity(n, trackerPanel);
    if (veloc != null) {
      double imageX = veloc.getXComponent();
      double imageY = veloc.getYComponent();
      double worldX = coords.imageToWorldXComponent(n, imageX, imageY) / dt;
      double worldY = coords.imageToWorldYComponent(n, imageX, imageY) / dt;
      return new Point2D.Double(worldX, worldY);
    }
    return null;
  }

  /**
   * Gets the world acceleration for the specified frame number and panel.
   * May return null;
   *
   * @param n the frame number
   * @param trackerPanel the tracker panel
   * @return a Point2D containing the acceleration components
   */
  public Point2D getWorldAcceleration(int n, TrackerPanel trackerPanel) {
    ImageCoordSystem coords = trackerPanel.getCoords();
    double dt = trackerPanel.getPlayer().getMeanStepDuration() / 1000.0;
    VectorStep accel = getAcceleration(n, trackerPanel);
    if (accel != null) {
      double imageX = accel.getXComponent();
      double imageY = accel.getYComponent();
      double worldX = coords.imageToWorldXComponent(n, imageX, imageY) / (dt*dt);
      double worldY = coords.imageToWorldYComponent(n, imageX, imageY) / (dt*dt);
      return new Point2D.Double(worldX, worldY);
    }
    return null;
  }
  
  /**
   * Sets the derivative algorithm type.
   * Defined types: FINITE_DIFF, BOUNCE_DETECT
   *
   * @param type one of the defined algorithm types
   */
  public void setAlgorithm(int type) {
  	if (type==algorithm) return;
  	if (type==FINITE_DIFF || type==BOUNCE_DETECT) {
  		algorithm = type;
  		refreshDataLater = false;
  		updateDerivatives();
      support.firePropertyChange("steps", null, null); //$NON-NLS-1$
  	}
  }

  @Override
  protected void dispose() {
  	if (trackerPanel!=null) {
  		removePropertyChangeListener("mass", trackerPanel.massChangeListener); //$NON-NLS-1$
  		if (trackerPanel.dataBuilder!=null) {
  			FunctionPanel functionPanel = trackerPanel.dataBuilder.getPanel(getName());
  			if (functionPanel!=null) {
  				functionPanel.getParamEditor().removePropertyChangeListener("edit", trackerPanel.massParamListener); //$NON-NLS-1$
  			}
  		}  	
  	}
  	super.dispose();
  }

  /**
   * Returns a description of the point at a given index. Used by AutoTracker.
   *
   * @param pointIndex the points[] index
   * @return the description
   */
  protected String getTargetDescription(int pointIndex) {
  	return TrackerRes.getString("PointMass.Position.Name"); //$NON-NLS-1$
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
    int baseCount = 23;
    int count = baseCount; // number of datasets
    if (!getClass().equals(CenterOfMass.class)
    		&& !getClass().equals(DynamicSystem.class)) {
    	count++; // extra dataset for KE
    }
    if (data.getDataset(0).getColumnName(0).equals("x")) { //$NON-NLS-1$
	    // assign column names to the datasets
	    String timeVar = "t"; //$NON-NLS-1$
    	data.getDataset(0).setXYColumnNames(timeVar, "x"); //$NON-NLS-1$
    	data.getDataset(1).setXYColumnNames(timeVar, "y"); //$NON-NLS-1$
    	data.getDataset(2).setXYColumnNames(timeVar, "r"); //$NON-NLS-1$
    	data.getDataset(3).setXYColumnNames(timeVar, "$\\theta$_{r}"); //$NON-NLS-1$
    	data.getDataset(4).setXYColumnNames(timeVar, "v_{x}"); //$NON-NLS-1$
    	data.getDataset(5).setXYColumnNames(timeVar, "v_{y}"); //$NON-NLS-1$
    	data.getDataset(6).setXYColumnNames(timeVar, "v"); //$NON-NLS-1$
    	data.getDataset(7).setXYColumnNames(timeVar, "$\\theta$_{v}"); //$NON-NLS-1$
    	data.getDataset(8).setXYColumnNames(timeVar, "a_{x}"); //$NON-NLS-1$
    	data.getDataset(9).setXYColumnNames(timeVar, "a_{y}"); //$NON-NLS-1$
    	data.getDataset(10).setXYColumnNames(timeVar, "a"); //$NON-NLS-1$
    	data.getDataset(11).setXYColumnNames(timeVar, "$\\theta$_{a}"); //$NON-NLS-1$
    	data.getDataset(12).setXYColumnNames(timeVar, "$\\theta$"); //$NON-NLS-1$
    	data.getDataset(13).setXYColumnNames(timeVar, "$\\omega$"); //$NON-NLS-1$
    	data.getDataset(14).setXYColumnNames(timeVar, "$\\alpha$"); //$NON-NLS-1$
    	data.getDataset(15).setXYColumnNames(timeVar, "step"); //$NON-NLS-1$
    	data.getDataset(16).setXYColumnNames(timeVar, "frame"); //$NON-NLS-1$
    	data.getDataset(17).setXYColumnNames(timeVar, "p_{x}"); //$NON-NLS-1$
    	data.getDataset(18).setXYColumnNames(timeVar, "p_{y}"); //$NON-NLS-1$
    	data.getDataset(19).setXYColumnNames(timeVar, "p"); //$NON-NLS-1$
    	data.getDataset(20).setXYColumnNames(timeVar, "$\\theta$_{p}"); //$NON-NLS-1$
    	data.getDataset(21).setXYColumnNames(timeVar, "pixel_{x}"); //$NON-NLS-1$
    	data.getDataset(22).setXYColumnNames(timeVar, "pixel_{y}"); //$NON-NLS-1$
    	if (count>baseCount)
  	    data.getDataset(baseCount).setXYColumnNames(timeVar, "K"); //$NON-NLS-1$
    }
    // create preferred column order
    if (count==baseCount) 
    	preferredColumnOrder = new int[] 
    	    {0,1,2,3,4,5,6,7,8,9,10,11,17,18,19,20,12,13,14};
    else 
    	preferredColumnOrder = new int[] 
    	    {0,1,2,3,4,5,6,7,8,9,10,11,17,18,19,20,12,13,14,baseCount};
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < dataDescriptions.length; i++) {
    	if (i<22) dataDescriptions[i] = TrackerRes.getString("PointMass.Data.Description."+i); //$NON-NLS-1$
    	else if (i==22) dataDescriptions[i] = TrackerRes.getString("PointMass.Data.Description.PixelX"); //$NON-NLS-1$
    	else if (i==23) dataDescriptions[i] = TrackerRes.getString("PointMass.Data.Description.PixelY"); //$NON-NLS-1$
    	else dataDescriptions[i] = TrackerRes.getString("PointMass.Data.Description.22"); //$NON-NLS-1$
    }
    // get the rotational data
    Object[] rotationData = getRotationData();
    double[] theta_data = (double[])rotationData[0];
    double[] omega_data = (double[])rotationData[1];
    double[] alpha_data = (double[])rotationData[2];
    // clear datasets
    dataFrames.clear();
    for (int i = 0; i < count;i++) {
      data.getDataset(i).clear();
    }
    // get data at each non-null position step in the videoclip
    VideoPlayer player = trackerPanel.getPlayer();
    VideoClip clip = player.getVideoClip();
    ImageCoordSystem coords = trackerPanel.getCoords();
    Step[] stepArray = getSteps();
    for (int n = 0; n < stepArray.length; n++) {
      if (stepArray[n] == null || !clip.includesFrame(n)) continue;
      int stepNumber = clip.frameToStep(n);
      double t = player.getStepTime(stepNumber)/1000.0;
    	double tf = player.getStepTime(stepNumber+vDerivSpill)/1000.0;
    	double to = player.getStepTime(stepNumber-vDerivSpill)/1000.0;
    	double dt_v = (tf-to)/(2*vDerivSpill);
    	tf = player.getStepTime(stepNumber+aDerivSpill)/1000.0;
    	to = player.getStepTime(stepNumber-aDerivSpill)/1000.0;
    	double dt_a2 = (tf-to)*(tf-to)/(4*aDerivSpill*aDerivSpill);
      // assemble the data values for this step
      double[] vals = new double[count];
      TPoint p = ((PositionStep)stepArray[n]).getPosition();
      Point2D pt = p.getWorldPosition(trackerPanel);
      vals[0] = pt.getX(); // x
      vals[1] = pt.getY(); // y
      vals[2] = pt.distance(0, 0); //mag
      vals[3] = Math.atan2(pt.getY(), pt.getX()); // ang between +/-pi
      vals[4] = Double.NaN; // vx
      vals[5] = Double.NaN; //vy
      vals[6] = Double.NaN; // vmag
      vals[7] = Double.NaN; // vang
      vals[8] = Double.NaN; // ax
      vals[9] = Double.NaN; // ay
      vals[10] = Double.NaN; // amag
      vals[11] = Double.NaN; // aang
      vals[12] = theta_data[n]; // theta
      vals[13] = omega_data[n]/dt_v; // omega
      vals[14] = alpha_data[n]/dt_a2; // alpha
      vals[15] = stepNumber; // step
      vals[16] = n; // frame
      vals[17] = Double.NaN; // px
      vals[18] = Double.NaN; // py
      vals[19] = Double.NaN; // pmag
      vals[20] = Double.NaN; // pang
      vals[21] = p.x; // pixel x
      vals[22] = p.y; // pixel y
      if (count>baseCount) vals[baseCount] = Double.NaN; // KE
	    VectorStep veloc = getVelocity(n, trackerPanel);
	    if (veloc != null) {
	    	double imageX = veloc.getXComponent();
	    	double imageY = veloc.getYComponent();
	      vals[4] = coords.imageToWorldXComponent(n, imageX, imageY)/dt_v;
	      vals[5] = coords.imageToWorldYComponent(n, imageX, imageY)/dt_v;
	      double vsquared = vals[4]*vals[4] + vals[5]*vals[5];
	      vals[6] = Math.sqrt(vsquared);
	      vals[7] = Math.atan2(vals[5], vals[4]);
	      double mass = getMass();
	      vals[17] = mass*vals[4];
	      vals[18] = mass*vals[5];
	      vals[19] = mass*vals[6];
	      vals[20] = mass*vals[7];
	      if (count>baseCount) vals[baseCount] = 0.5*mass*vsquared;
	    }
      VectorStep accel = getAcceleration(n, trackerPanel);
      if (accel != null) {
      	double imageX = accel.getXComponent();
      	double imageY = accel.getYComponent();
        vals[8] = coords.imageToWorldXComponent(n, imageX, imageY)/dt_a2;
        vals[9] = coords.imageToWorldYComponent(n, imageX, imageY)/dt_a2;
        vals[10] = Math.sqrt(vals[8]*vals[8] + vals[9]*vals[9]);
        vals[11] = Math.atan2(vals[9], vals[8]);
      }
      // append points to datasets
      for (int i = 0; i < count; i++) {
      	data.getDataset(i).append(t, vals[i]);
      }
      dataFrames.add(new Integer(n));
    }
    // store the mass in the data properties
    Double m = getMass();
    String desc = TrackerRes.getString("ParticleModel.Parameter.Mass.Description"); //$NON-NLS-1$
    data.setConstant("m", m, m.toString(), desc); //$NON-NLS-1$
  }

  /**
   * Overrides TTrack draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    if (!(panel instanceof TrackerPanel) || !visible) return;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Graphics2D g = (Graphics2D)_g;
    panels.add(trackerPanel);   // keep a list of drawing panels
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    int n = trackerPanel.getFrameNumber();
    int stepSize = clip.getStepSize();
    if (trailVisible) {
    	boolean shortTrail = getTrailLength() > 0;
      Step[] stepArray = steps.array;
      for (int i = 0; i < stepArray.length; i++) {
      	if (shortTrail && (n-i > (getTrailLength()-1)*stepSize || i>n))
      		continue;
        if (stepArray[i] != null) {
        	if (isStepVisible(stepArray[i], trackerPanel)) {
            stepArray[i].draw(trackerPanel, g);        		
        	}
          Step v = getVelocity(i, trackerPanel);
          if (v != null && isStepVisible(v, trackerPanel)) {
          	v.draw(trackerPanel, g);
          }
          Step a = getAcceleration(i, trackerPanel);
          if (a != null && isStepVisible(a, trackerPanel)) {
          	a.draw(trackerPanel, g);
          }
        }
      }
    }
    else {
      Step step = getStep(n);
      if (step != null) {
      	if (isStepVisible(step, trackerPanel)) {
      		step.draw(trackerPanel, g);        		
      	}
        Step v = getVelocity(n, trackerPanel);
        if (v != null && isStepVisible(v, trackerPanel)) {
        	v.draw(trackerPanel, g);
        }
        Step a = getAcceleration(n, trackerPanel);
        if (a != null && isStepVisible(a, trackerPanel)) {
        	a.draw(trackerPanel, g);
        }
      }
    }
    if (isTraceVisible() && !(this instanceof ParticleModel)) {
    	Color c = g.getColor();
    	Stroke s = g.getStroke();
    	g.setColor(getColor());
    	g.setStroke(traceStroke);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    	trace.reset();
      int startFrame = clip.getStartFrameNumber();
      int endFrame = clip.getEndFrameNumber();
      boolean reset = true;
      for (int i = startFrame; i <= endFrame; i+=stepSize) {
      	PositionStep step = (PositionStep)getStep(i);
      	if (step==null) {
        	g.draw(trace);
          reset = true;
          continue;
      	}
				Point p = step.getPosition().getScreenPosition(trackerPanel);
				if (reset) {
		    	trace.reset();
					trace.moveTo((float) p.getX(), (float) p.getY());
					reset = false;
				}
				else trace.lineTo((float) p.getX(), (float) p.getY());
      }
    	g.draw(trace);
    	g.setColor(c);
    	g.setStroke(s);
    }
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
    if (!(panel instanceof TrackerPanel) || !visible) return null;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Interactive iad = null;
    int n = trackerPanel.getFrameNumber();
    int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
    if (trailVisible) {
    	boolean shortTrail = getTrailLength() > 0;
    	Step[] stepArray = steps.array;
      for (int i = 0; i < stepArray.length; i++) {
      	if (shortTrail && (n-i > (getTrailLength()-1)*stepSize || i>n))
      		continue;
        if (stepArray[i] != null) {
        	if (isStepVisible(stepArray[i], trackerPanel)) {
            iad = stepArray[i].findInteractive(trackerPanel, xpix, ypix);
            if (iad != null) {
              partName = TrackerRes.getString("PointMass.Position.Name"); //$NON-NLS-1$
              hint = TrackerRes.getString("PointMass.Position.Hint"); //$NON-NLS-1$
            	return iad;
            }
        	}
          Step v = getVelocity(i, trackerPanel);
          if (v != null && isStepVisible(v, trackerPanel)) {
            iad = v.findInteractive(trackerPanel, xpix, ypix);
            if (iad != null) {
              partName = TrackerRes.getString("PointMass.Velocity.Name"); //$NON-NLS-1$
              hint = TrackerRes.getString("PointMass.Vector.Hint"); //$NON-NLS-1$
            	return iad;
            }
          }
          Step a = getAcceleration(i, trackerPanel);
          if (a != null && isStepVisible(a, trackerPanel)) {
            iad = a.findInteractive(trackerPanel, xpix, ypix);
            if (iad != null) {
              partName = TrackerRes.getString("PointMass.Acceleration.Name"); //$NON-NLS-1$
              hint = TrackerRes.getString("PointMass.Vector.Hint"); //$NON-NLS-1$
            	return iad;
            }
          }
        }
      }
    }
    else {
      Step step = getStep(n);
      if (step != null) {
      	if (isStepVisible(step, trackerPanel)) {
          iad = step.findInteractive(trackerPanel, xpix, ypix);
          if (iad != null) {
            partName = TrackerRes.getString("PointMass.Position.Name"); //$NON-NLS-1$
            hint = TrackerRes.getString("PointMass.Position.Hint"); //$NON-NLS-1$
          	return iad;
          }
      	}
        Step v = getVelocity(n, trackerPanel);
        if (v != null && isStepVisible(v, trackerPanel)) {
          iad = v.findInteractive(trackerPanel, xpix, ypix);
          if (iad != null) {
            partName = TrackerRes.getString("PointMass.Velocity.Name"); //$NON-NLS-1$
            hint = TrackerRes.getString("PointMass.Vector.Hint"); //$NON-NLS-1$
          	return iad;
          }
        }
        Step a = getAcceleration(n, trackerPanel);
        if (a != null && isStepVisible(a, trackerPanel)) {
          iad = a.findInteractive(trackerPanel, xpix, ypix);
          if (iad != null) {
            partName = TrackerRes.getString("PointMass.Acceleration.Name"); //$NON-NLS-1$
            hint = TrackerRes.getString("PointMass.Vector.Hint"); //$NON-NLS-1$
          	return iad;
          }
        }
      }
    }
    // null iad case
    Step selectedStep = trackerPanel.getSelectedStep();
    if (selectedStep!=null) {
	    if (selectedStep instanceof PositionStep) {
		  	partName = TrackerRes.getString("PointMass.Position.Name"); //$NON-NLS-1$
		  	partName += " "+TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
		    hint = TrackerRes.getString("PointMass.PositionSelected.Hint"); //$NON-NLS-1$
		  }
	    else if (selectedStep instanceof VectorStep) {
	    	if (isVelocity(selectedStep))
	    		partName = TrackerRes.getString("PointMass.Velocity.Name"); //$NON-NLS-1$
	    	else
	    		partName = TrackerRes.getString("PointMass.Acceleration.Name"); //$NON-NLS-1$
		  	partName += " "+TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
		    hint = TrackerRes.getString("PointMass.VectorSelected.Hint"); //$NON-NLS-1$
		  }
    }
    else {
	  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
	    hint = TrackerRes.getString("PointMass.Hint"); //$NON-NLS-1$
	    if (getStep(trackerPanel.getFrameNumber())==null)
	      hint += TrackerRes.getString("PointMass.Unmarked.Hint"); //$NON-NLS-1$
	    else
	      hint += TrackerRes.getString("PointMass.Remark.Hint"); //$NON-NLS-1$
	  }
    return null;
  }
  
  /**
   * Shows and hides this track.
   *
   * @param visible <code>true</code> to show this track
   */
  @Override
  public void setVisible(boolean visible) {
  	if (!visible) {
  		for (Step step: getSteps()) {
  			if (step!=null && trackerPanel!=null) {
  				trackerPanel.selectedSteps.remove(step);
  			}
  		}
  	}
    super.setVisible(visible);
  }
  


  /**
   * Reports whether or not the specified step is visible.
   *
   * @param step the step
   * @param trackerPanel the tracker panel
   * @return <code>true</code> if the step is visible
   */
  public boolean isStepVisible(Step step, TrackerPanel trackerPanel) {
    if (!isVisible()) return false;
    int n = step.getFrameNumber();
    if (!trackerPanel.getPlayer().getVideoClip().includesFrame(n)) return false;
    if (isPosition(step) && !isPositionVisible(trackerPanel)) return false;
    if (isVelocity(step) && !isVVisible(trackerPanel)) return false;
    if (isAcceleration(step) && !isAVisible(trackerPanel)) return false;
//   	AutoTracker autoTracker = trackerPanel.autoTracker;
//    if (trackerPanel.getFrameNumber() == n
//    		&& autoTracker.wizard.isVisible()
//    		&& autoTracker.getTrack()==this
//    		&& trackerPanel==autoTracker.trackerPanel
//    		&& !autoTracker.isManuallyMarked(n)) {
//    	return false;
//    }
    return trailVisible || trackerPanel.getFrameNumber() == n;
  }

  /**
   * Sets whether velocities are visible on all tracker panels.
   *
   * @param visible <code>true</code> to show velocities
   */
  public void setVVisibleOnAll(boolean visible) {
    vVisibleOnAll = visible;
  }

  /**
   * Shows and hides velocities on the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @param visible <code>true</code> to show velocities
   */
  public void setVVisible(TrackerPanel trackerPanel, boolean visible) {
    if (visible == isVVisible(trackerPanel)) return;
    vVisMap.put(trackerPanel, new Boolean(visible));
//    if (visible) updateDerivatives();
    if (!visible) {
      Step step = trackerPanel.getSelectedStep();
      if (step != null && isVelocity(step)) {
        trackerPanel.setSelectedPoint(null);
      }
    }
  }

  /**
   * Gets whether the velocities are visible on the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return <code>true</code> if velocities are visible
   */
  public boolean isVVisible(TrackerPanel trackerPanel) {
    if (vVisibleOnAll) return true;
    if (trackerPanel instanceof WorldTView) {
      trackerPanel = ((WorldTView)trackerPanel).getTrackerPanel();
    }
    Boolean vis = vVisMap.get(trackerPanel);
    if (vis == null) {
      vis = new Boolean(false);        // not visible by default
      vVisMap.put(trackerPanel, vis);
    }
    return vis.booleanValue();
  }

  /**
   * Sets whether positions are visible on all tracker panels.
   *
   * @param visible <code>true</code> to show positions
   */
  public void setPositionVisibleOnAll(boolean visible) {
    xVisibleOnAll = visible;
  }

  /**
   * Sets whether traces are visible.
   *
   * @param visible <code>true</code> to show traces
   */
  public void setTraceVisible(boolean visible) {
    traceVisible = visible;
  }

  /**
   * Gets trace visibility.
   *
   * @return <code>true</code> if traces are visible
   */
  public boolean isTraceVisible() {
    return traceVisible;
  }

  /**
   * Determines whether the specified step is a position step.
   *
   * @param step the step
   * @return <code>true</code> if the step is a position
   */
  public boolean isPosition(Step step) {
    return step instanceof PositionStep;
  }

  /**
   * Shows and hides positions on the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @param visible <code>true</code> to show positions
   */
  public void setPositionVisible(TrackerPanel trackerPanel, boolean visible) {
  	Boolean vis = xVisMap.get(trackerPanel);
    if (vis != null && vis.booleanValue()==visible) return;
    xVisMap.put(trackerPanel, new Boolean(visible));
    if (!visible) {
      Step step = trackerPanel.getSelectedStep();
      if (step != null && step == getStep(step.getFrameNumber())) {
        trackerPanel.setSelectedPoint(null);
      }
      trackerPanel.selectedSteps.clear();
    }
  }

  /**
   * Gets whether the positions are visible on the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return <code>true</code> if positions are visible
   */
  public boolean isPositionVisible(TrackerPanel trackerPanel) {
    if (xVisibleOnAll) return true;
    if (trackerPanel instanceof WorldTView) {
      trackerPanel = ((WorldTView)trackerPanel).getTrackerPanel();
    }
    Boolean vis = xVisMap.get(trackerPanel);
    if (vis == null) {
      vis = new Boolean(true);        // positions are visible by default
      xVisMap.put(trackerPanel, vis);
    }
    return vis.booleanValue();
  }

  /**
   * Gets the velocity for the specified frame number and panel.
   *
   * @param n the frame number
   * @param trackerPanel the tracker panel
   * @return the velocity
   */
  public VectorStep getVelocity(int n, TrackerPanel trackerPanel) {
    return (VectorStep)getVArray(trackerPanel).getStep(n);
  }

  /**
   * Gets the velocity step array for the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return the velocity step array
   */
  public Step[] getVelocities(TrackerPanel trackerPanel) {
    return getVArray(trackerPanel).array;
  }

  /**
   * Determines whether the specified step is a velocity step.
   *
   * @param step the step
   * @return <code>true</code> if the step is a velocity VectorStep
   */
  public boolean isVelocity(Step step) {
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel panel = it.next();
      boolean isV = getVArray(panel).contains(step);
      if (isV) return true;
    }
    return false;
  }

  /**
   * Sets whether accelerations are visible on all tracker panels.
   *
   * @param visible <code>true</code> to show accelerations
   */
  public void setAVisibleOnAll(boolean visible) {
    aVisibleOnAll = visible;
  }

  /**
   * Shows and hides accelerations on the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @param visible <code>true</code> to show accelerations
   */
  public void setAVisible(TrackerPanel trackerPanel, boolean visible) {
    if (visible == isAVisible(trackerPanel)) return;
    aVisMap.put(trackerPanel, new Boolean(visible));
//    if (visible) updateDerivatives();
    if (!visible) {
      Step step = trackerPanel.getSelectedStep();
      if (step != null && isAcceleration(step)) {
        trackerPanel.setSelectedPoint(null);
      }
    }
  }

  /**
   * Gets whether the accelerations are visible on the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return <code>true</code> if accelerations are visible
   */
  public boolean isAVisible(TrackerPanel trackerPanel) {
    if (aVisibleOnAll) return true;
    if (trackerPanel instanceof WorldTView) {
      trackerPanel = ((WorldTView)trackerPanel).getTrackerPanel();
    }
    Boolean vis = aVisMap.get(trackerPanel);
    if (vis == null) {
      vis = new Boolean(false);     // not visible by default
      aVisMap.put(trackerPanel, vis);
    }
    return vis.booleanValue();
  }

  /**
   * Gets the acceleration for the specified frame number and panel.
   *
   * @param n the frame number
   * @param trackerPanel the tracker panel
   * @return the acceleration vector
   */
  public VectorStep getAcceleration(int n, TrackerPanel trackerPanel) {
    return (VectorStep)getAArray(trackerPanel).getStep(n);
  }

  /**
   * Gets the acceleration step array for the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return the acceleration step array
   */
  public Step[] getAccelerations(TrackerPanel trackerPanel) {
    return getAArray(trackerPanel).array;
  }

  /**
   * Determines whether the specified step is an acceleration step.
   *
   * @param step the step
   * @return <code>true</code> if the step is an acceleration VectorStep
   */
  public boolean isAcceleration(Step step) {
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel panel = it.next();
      boolean isA = getAArray(panel).contains(step);
      if (isA) return true;
    }
    return false;
  }

  /**
   * Sets the visibility of labels on the specified panel.
   *
   * @param panel the tracker panel
   * @param visible <code>true</code> to show all labels
   */
  public void setLabelsVisible(TrackerPanel panel, boolean visible) {
    Step[] steps = this.getSteps();
    for (int i = 0; i < steps.length; i++) {
      PositionStep step = (PositionStep)steps[i];
      if (step != null) {
        step.setLabelVisible(visible);
        step.setRolloverVisible(!visible);
      }
    }
    steps = this.getVelocities(panel);
    for (int i = 0; i < steps.length; i++) {
      VectorStep step = (VectorStep)steps[i];
      if (step != null) {
        step.setLabelVisible(visible);
        step.setRolloverVisible(!visible);
      }
    }
    steps = this.getAccelerations(panel);
    for (int i = 0; i < steps.length; i++) {
      VectorStep step = (VectorStep)steps[i];
      if (step != null) {
        step.setLabelVisible(visible);
        step.setRolloverVisible(!visible);
      }
    }
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel next = it.next();
      if (next instanceof WorldTView) {
        WorldTView view = (WorldTView)next;
        if (view.getTrackerPanel() == panel) {
          setLabelsVisible(view, visible);
        }
      }
    }
  }

  /**
   * Gets the labels visibility.
   *
   * @param panel the tracker panel
   * @return <code>true</code> if labels are visible
   */
  public boolean isLabelsVisible(TrackerPanel panel) {
    Step[] steps = this.getSteps();
    for (int i = 0; i < steps.length; i++) {
    	PositionStep step = (PositionStep)steps[i];
      if (step != null) return step.isLabelVisible();
    }
    return false;
  }

  /**
   * Updates all velocity and acceleration steps on all TrackerPanels.
   */
  protected void updateDerivatives() {
  	if (isEmpty() || refreshDataLater) return;
    // for each panel, update entire current videoclip
  	for (TrackerPanel trackerPanel: vMap.keySet()) {
      updateDerivatives(trackerPanel);
    }
  }

  /**
   * Updates velocity and acceleration steps for a specified
   * start frame and step count.
   * 
   * @param startFrame the start frame
   * @param stepCount the step count
   */
  protected void updateDerivatives(int startFrame, int stepCount) {
  	if (isEmpty() || refreshDataLater) return;
    Tracker.logTime(this.getClass().getSimpleName()+this.hashCode()+" update derivatives "+startFrame+" steps "+stepCount); //$NON-NLS-1$ //$NON-NLS-2$
  	for (TrackerPanel trackerPanel: vMap.keySet()) {
      updateDerivatives(trackerPanel, startFrame, stepCount);
    }
  }

  /**
   * Updates velocity and acceleration steps around a give frame number.
   * 
   * @param frameNumber the frame number
   */
  protected void updateDerivatives(int frameNumber) {
  	if (isEmpty() || refreshDataLater) return;  
  	for (TrackerPanel trackerPanel: vMap.keySet()) {
      updateDerivatives(trackerPanel, frameNumber);
    }
  }

  /**
   * Updates velocity and acceleration steps around a give frame number on a TrackerPanel.
   * 
   * @param trackerPanel the TrackerPanel
   * @param frameNumber the frame number
   */
  protected void updateDerivatives(TrackerPanel trackerPanel, int frameNumber) {
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    int startFrame = Math.max(frameNumber-2*clip.getStepSize(), clip.getStartFrameNumber());
    updateDerivatives(trackerPanel, startFrame, 5);
  }
  
  /**
   * Updates all velocity and acceleration steps on a TrackerPanel.
   * 
   * @param trackerPanel the TrackerPanel
   */
  protected void updateDerivatives(TrackerPanel trackerPanel) {
    if (refreshDataLater)
    	return;
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    updateDerivatives(trackerPanel, clip.getStartFrameNumber(), clip.getStepCount());
  }

  /**
   * Updates velocity and acceleration steps for a specified
   * start frame and step count.
   * 
   * @param trackerPanel the TrackerPanel
   * @param startFrame the start frame
   * @param stepCount the step count
   */
  protected void updateDerivatives(TrackerPanel trackerPanel,
  		int startFrame, int stepCount) {
		if (trackerPanel instanceof WorldTView
				&& !((WorldTView)trackerPanel).isSelectedView())
			return;
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    // initialize data arrays
    if (xData.length < steps.length) {
      derivData[1] = xData = new double[steps.length + 5];
      derivData[2] = yData = new double[steps.length + 5];
      derivData[3] = validData = new boolean[steps.length + 5];
    }
    // set up derivative parameters
    params[1] = startFrame;
    params[2] = clip.getStepSize();
    params[3] = stepCount;
    // set up position data
    for (int i = 0; i < validData.length; i++)
      validData[i] = false;
    Step[] stepArray = steps.array;
    for (int n = 0; n < stepArray.length; n++) {
      if (stepArray[n] != null && clip.includesFrame(n)) {
        PositionStep step = (PositionStep) stepArray[n];
        Point2D p = step.getPosition().getWorldPosition(trackerPanel);
        xData[n] = p.getX(); // worldspace position
        yData[n] = p.getY(); // worldspace position
        validData[n] = true;
      }
    }
    // unlock track while updating
    boolean isLocked = locked; // save for later restoration
    locked = false;
    boolean labelsVisible = isLabelsVisible(trackerPanel);
    
    // evaluate derivatives in worldspace coordinates
    double[] xDeriv1; // first deriv
    double[] yDeriv1; // first deriv
    double[] xDeriv2; // second deriv
    double[] yDeriv2; // second deriv
    if (algorithm==1) {
	    params[0] = bounceDerivsSpill; // spill
	    Object[] result = bounceDerivs.evaluate(derivData);    	
	    xDeriv1 = (double[]) result[0];
	    yDeriv1 = (double[]) result[1];
	    xDeriv2 = (double[]) result[2];
	    yDeriv2 = (double[]) result[3];
    }
    else {
	    params[0] = vDerivSpill; // spill
	    Object[] result = vDeriv.evaluate(derivData);    	
	    xDeriv1 = (double[]) result[0];
	    yDeriv1 = (double[]) result[1];
	    params[0] = aDerivSpill; // spill
	    result = aDeriv.evaluate(derivData);
	    xDeriv2 = (double[]) result[2];
	    yDeriv2 = (double[]) result[3];
    }

    // create, delete and/or set components of velocity vectors
    StepArray array = vMap.get(trackerPanel);
    int endFrame = startFrame+(stepCount-1)*clip.getStepSize();
    int end = Math.min(endFrame, xDeriv1.length-1);
    for (int n = startFrame; n <= end; n++) {
      VectorStep v = (VectorStep) array.getStep(n);
      if ((Double.isNaN(xDeriv1[n])  || !validData[n]) && v == null)
        continue;
      if (!Double.isNaN(xDeriv1[n]) && validData[n]) {
        double x = trackerPanel.getCoords().
            worldToImageXComponent(n, xDeriv1[n], yDeriv1[n]);
        double y = trackerPanel.getCoords().
            worldToImageYComponent(n, xDeriv1[n], yDeriv1[n]);
        if (v == null) { // create new vector
          TPoint p = ( (PositionStep) getStep(n)).getPosition();
          v = new VectorStep(this, n, p.getX(), p.getY(), x, y);
          v.setTipEnabled(false);
          v.getHandle().setStepEditTrigger(true);
          v.setDefaultPointIndex(2); // handle
          v.setFootprint(vFootprint);
          v.setLabelVisible(labelsVisible);
          v.setRolloverVisible(!labelsVisible);
          v.attach(p);
          array.setStep(n, v);
          trackerPanel.addDirtyRegion(v.getBounds(trackerPanel));
        }
        else if ( (int) (100 * v.getXComponent()) != (int) (100 * x) ||
                 (int) (100 * v.getYComponent()) != (int) (100 * y)) {
          trackerPanel.addDirtyRegion(v.getBounds(trackerPanel));
          v.attach(v.getAttachmentPoint());
          v.setXYComponents(x, y);
          trackerPanel.addDirtyRegion(v.getBounds(trackerPanel));
        }
        else
          v.attach(v.getAttachmentPoint());
      }
      else {
        array.setStep(n, null);
        trackerPanel.addDirtyRegion(v.getBounds(trackerPanel));
      }
    }
    
    // create, delete and/or set components of accel vectors
    array = aMap.get(trackerPanel);
    end = Math.min(endFrame, xDeriv2.length-1);
    for (int n = startFrame; n <= end; n++) {
      VectorStep a = (VectorStep) array.getStep(n);
      if ((Double.isNaN(xDeriv2[n]) || !validData[n]) && a == null)
        continue;
      if (!Double.isNaN(xDeriv2[n]) && validData[n]) {
        double x = trackerPanel.getCoords().
            worldToImageXComponent(n, xDeriv2[n], yDeriv2[n]);
        double y = trackerPanel.getCoords().
            worldToImageYComponent(n, xDeriv2[n], yDeriv2[n]);
        if (a == null) {
          TPoint p = ( (PositionStep) getStep(n)).getPosition();
          a = new VectorStep(this, n, p.getX(), p.getY(), x, y);
          a.getHandle().setStepEditTrigger(true);
          a.setTipEnabled(false);
          a.setDefaultPointIndex(2); // handle
          a.setFootprint(aFootprint);
          a.setLabelVisible(labelsVisible);
          a.setRolloverVisible(!labelsVisible);
          a.attach(p);
          array.setStep(n, a);
          trackerPanel.addDirtyRegion(a.getBounds(trackerPanel));
        }
        else if ( (int) (100 * a.getXComponent()) != (int) (100 * x) ||
                 (int) (100 * a.getYComponent()) != (int) (100 * y)) {
          trackerPanel.addDirtyRegion(a.getBounds(trackerPanel));
          a.attach(a.getAttachmentPoint());
          a.setXYComponents(x, y);
          trackerPanel.addDirtyRegion(a.getBounds(trackerPanel));
        }
        else
          a.attach(a.getAttachmentPoint());
      }
      else {
        array.setStep(n, null);
        trackerPanel.addDirtyRegion(a.getBounds(trackerPanel));
      }
    }
    // restore locked state
    locked = isLocked;
    // repaint dirty region
    trackerPanel.repaintDirtyRegion();
  }

  /**
   * Gets the rotational data.
   * 
   * @return Object[] {theta, omega, alpha}
   */
  protected Object[] getRotationData() {
    // initialize data arrays once, for all panels
    if (xData.length < steps.length) {
      derivData[1] = xData = new double[steps.length + 5];
      derivData[2] = yData = new double[steps.length + 5];
      derivData[3] = validData = new boolean[steps.length + 5];
    }
    for (int i = 0; i < steps.length; i++)
      validData[i] = false;
    // set up derivative parameters
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    params[1] = clip.getStartFrameNumber();
    params[2] = clip.getStepSize();
    params[3] = clip.getStepCount();
    // set up angular position data
    Step[] stepArray = steps.array;
    double rotation = 0;
    double prevAngle = 0;
    for (int n = 0; n < stepArray.length; n++) {
      if (stepArray[n] != null && clip.includesFrame(n)) {
        PositionStep step = (PositionStep) stepArray[n];
        Point2D p = step.getPosition().getWorldPosition(trackerPanel);
        double angle = Math.atan2(p.getY(), p.getX()); // between +/-pi
        // determine the cumulative rotation angle
        double delta = angle-prevAngle;
        if (delta < -Math.PI) delta += 2*Math.PI;
        else if (delta > Math.PI) delta -= 2*Math.PI;
  	    rotation += delta;
        prevAngle = angle;
        xData[n] = rotation;
        yData[n] = 0; // ignored
        validData[n] = true;
      }
      else xData[n] = Double.NaN;
    }
    // unlock track while updating
    boolean isLocked = locked; // save for later restoration
    locked = false;
    
    // evaluate first derivative
    params[0] = vDerivSpill; // spill
    Object[] result = vDeriv.evaluate(derivData);
    double[] omega = (double[]) result[0];

    // evaluate second derivative
    params[0] = aDerivSpill; // spill
    result = aDeriv.evaluate(derivData);
    double[] alpha = (double[]) result[2];

    // restore locked state
    locked = isLocked;
  	return new Object[] {xData, omega, alpha};
  }

  /**
   * Gets the rotational data for a range of frame numbers.
   * 
   * @param startFrame the start frame
   * @param stepCount the number of steps
   * @return Object[] {theta, omega, alpha}
   */
  protected Object[] getRotationData(int startFrame, int stepCount) {
    // initialize data arrays once, for all panels
    if (xData.length < steps.length) {
      derivData[1] = xData = new double[steps.length + 5];
      derivData[2] = yData = new double[steps.length + 5];
      derivData[3] = validData = new boolean[steps.length + 5];
    }
    for (int i = 0; i < steps.length; i++)
      validData[i] = false;
    // set up derivative parameters
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    params[1] = startFrame;
    params[2] = clip.getStepSize();
    params[3] = stepCount;
    // set up angular position data
    Step[] stepArray = steps.array;
    double rotation = 0;
    double prevAngle = 0;
    for (int n = 0; n < stepArray.length; n++) {
      if (stepArray[n] != null && clip.includesFrame(n)) {
        PositionStep step = (PositionStep) stepArray[n];
        Point2D p = step.getPosition().getWorldPosition(trackerPanel);
        double angle = Math.atan2(p.getY(), p.getX()); // between +/-pi
        // determine the cumulative rotation angle
        double delta = angle-prevAngle;
        if (delta < -Math.PI) delta += 2*Math.PI;
        else if (delta > Math.PI) delta -= 2*Math.PI;
  	    rotation += delta;
        prevAngle = angle;
        xData[n] = rotation;
        yData[n] = 0; // ignored
        validData[n] = true;
      }
      else xData[n] = Double.NaN;
    }
    // unlock track while updating
    boolean isLocked = locked; // save for later restoration
    locked = false;
    // evaluate first derivative
    params[0] = vDerivSpill; // spill
    Object[] result = vDeriv.evaluate(derivData);
    double[] omega = (double[]) result[0];
    boolean[] validDeriv = (boolean[]) result[2];
    for (int i = 0; i < omega.length; i++) {
    	if (!validDeriv[i]) omega[i] = Double.NaN;
    }
    // evaluate second derivative
    params[0] = aDerivSpill; // spill
    result = aDeriv.evaluate(derivData);
    double[] alpha = (double[]) result[2];
    // restore locked state
    locked = isLocked;
  	return new Object[] {xData, omega, alpha};
  }

  /**
   * Overrides TTrack erase method to include v and a.
   */
  public void erase() {
    super.erase(); // erases all steps on all panels
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel trackerPanel = it.next();
      // erase velocity and acceleration steps
      if (vMap.get(trackerPanel)!=null) {
        Step[] stepArray = getVelocities(trackerPanel);
        for (int i = 0; i < stepArray.length; i++)
          if (stepArray[i] != null) stepArray[i].erase(trackerPanel);
        stepArray = getAccelerations(trackerPanel);
        for (int j = 0; j < stepArray.length; j++)
          if (stepArray[j] != null) stepArray[j].erase(trackerPanel);
      }
    }
  }

  /**
   * Overrides TTrack remark method.
   */
  public void remark() {
    super.remark();
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel trackerPanel = it.next();
      Step[] stepArray = getVelocities(trackerPanel);
      for (int i = 0; i < stepArray.length; i++)
        if (stepArray[i] != null) {
        	stepArray[i].remark(trackerPanel);
        }
      stepArray = getAccelerations(trackerPanel);
      for (int j = 0; j < stepArray.length; j++)
        if (stepArray[j] != null) stepArray[j].remark(trackerPanel);
    }
  }

  /**
   * Overrides TTrack erase method.
   *
   * @param trackerPanel the tracker panel
   */
  public void erase(TrackerPanel trackerPanel) {
    super.erase(trackerPanel);
    Step[] stepArray = getVelocities(trackerPanel);
    for (int i = 0; i < stepArray.length; i++)
      if (stepArray[i] != null) stepArray[i].erase(trackerPanel);
    stepArray = getAccelerations(trackerPanel);
    for (int j = 0; j < stepArray.length; j++)
      if (stepArray[j] != null) stepArray[j].erase(trackerPanel);
  }

  /**
   * Overrides TTrack remark method.
   *
   * @param trackerPanel the tracker panel
   */
  public void remark(TrackerPanel trackerPanel) {
    super.remark(trackerPanel);
    Step[] stepArray = getVelocities(trackerPanel);
    for (int i = 0; i < stepArray.length; i++)
      if (stepArray[i] != null) {
      	stepArray[i].remark(trackerPanel);
      }
    stepArray = getAccelerations(trackerPanel);
    for (int j = 0; j < stepArray.length; j++)
      if (stepArray[j] != null) stepArray[j].remark(trackerPanel);
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
      if (name.equals("transform")) { //$NON-NLS-1$
      	dataValid = false;
        updateDerivatives();
	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
      }
      else if (name.equals("stepsize")) { //$NON-NLS-1$
        dataValid = false;
        updateDerivatives();
	    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
        int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
        if (skippedStepWarningOn
        		&& stepSizeWhenFirstMarked>1
        		&& stepSize!=stepSizeWhenFirstMarked) {
        	JDialog warning = getStepSizeWarningDialog();
        	if (warning!=null)
        		warning.setVisible(true);
        }
      }
			else if (name.equals("adjusting")) { //$NON-NLS-1$
				refreshDataLater = (Boolean)e.getNewValue();
				if (!refreshDataLater) {  // stopped adjusting
					updateDerivatives();
		    	support.firePropertyChange("data", null, null); //$NON-NLS-1$
				}
			}
    }
  	super.propertyChange(e);
  }

  /**
   * Returns a menu with items that control this track.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
    // remove delete item from end
    if (menu.getItemCount() > 0) {
	    JMenuItem item = menu.getItem(menu.getItemCount()-1);
	    if (item == deleteTrackItem) {
	      menu.remove(deleteTrackItem);
	      if (menu.getItemCount() > 0)
	        menu.remove(menu.getItemCount()-1); // remove separator
	    }
    }
    // prepare vector footprint menus
    vFootprintMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
    aFootprintMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
    vFootprintMenu.removeAll();
    final ActionListener vFootprintListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String footprintName = e.getActionCommand();
        if (getVelocityFootprint().getName().equals(footprintName)) return;
        XMLControl control = new XMLControlElement(PointMass.this);
        setVelocityFootprint(footprintName);
        Undo.postTrackEdit(PointMass.this, control);
      }
    };
    Footprint[] fp = getVelocityFootprints();
    JMenuItem item;
    for (int i = 0; i < fp.length; i++) {
    	BasicStroke stroke = fp[i].getStroke();
    	fp[i].setStroke(new BasicStroke(stroke.getLineWidth(),
          BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8, null, 0));
      item = new JMenuItem(fp[i].getDisplayName(), fp[i].getIcon(21, 16));
      if (fp[i]==vFootprint) {
        item.setBorder(BorderFactory.createLineBorder(item.getBackground().darker()));
      }
      item.setActionCommand(fp[i].getName());
      item.addActionListener(vFootprintListener);
      vFootprintMenu.add(item);
      fp[i].setStroke(stroke);
    }
    aFootprintMenu.removeAll();
    final ActionListener aFootprintListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String footprintName = e.getActionCommand();
        if (getAccelerationFootprint().getName().equals(footprintName)) return;
        XMLControl control = new XMLControlElement(PointMass.this);
        setAccelerationFootprint(footprintName);
        Undo.postTrackEdit(PointMass.this, control);
      }
    };
    fp = getAccelerationFootprints();
    for (int i = 0; i < fp.length; i++) {
    	BasicStroke stroke = fp[i].getStroke();
    	fp[i].setStroke(new BasicStroke(stroke.getLineWidth(),
          BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 8, null, 0));
      item = new JMenuItem(fp[i].getDisplayName(), fp[i].getIcon(21, 16));
      if (fp[i]==aFootprint) {
        item.setBorder(BorderFactory.createLineBorder(item.getBackground().darker()));
      }
      item.setActionCommand(fp[i].getName());
      item.addActionListener(aFootprintListener);
      aFootprintMenu.add(item);
      fp[i].setStroke(stroke);
    }
    // if video is not null, add autotrack item just above dataColumnsItem item
    if (trackerPanel.isEnabled("track.autotrack")) { //$NON-NLS-1$
	    autotrackItem.setText(TrackerRes.getString("PointMass.MenuItem.Autotrack")); //$NON-NLS-1$	    
	    autotrackItem.setEnabled(trackerPanel.getVideo()!=null);
	    boolean added = false;
	    for (int i = 0; i < menu.getItemCount(); i++) {
	    	JMenuItem next = menu.getItem(i);
	    	if (next == dataBuilderItem) {
	  	    menu.insert(autotrackItem, i);
	  	    added = true;
	    	}
	    }
	    if (!added) menu.add(autotrackItem); // just in case
    }
    // add autoAdvance and markByDefault items
    if (trackerPanel.isEnabled("track.autoAdvance") || //$NON-NLS-1$
        trackerPanel.isEnabled("track.markByDefault")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
      if (trackerPanel.isEnabled("track.autoAdvance")) //$NON-NLS-1$
        menu.add(autoAdvanceItem);
      if (trackerPanel.isEnabled("track.markByDefault")) //$NON-NLS-1$
        menu.add(markByDefaultItem);
    }
    // add velocity and accel menus
    if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
      menu.addSeparator();
    velocityMenu.setText(TrackerRes.getString("PointMass.MenuItem.Velocity")); //$NON-NLS-1$
    accelerationMenu.setText(TrackerRes.getString("PointMass.MenuItem.Acceleration")); //$NON-NLS-1$
    vColorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
    aColorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
    vTailsToOriginItem.setText(TrackerRes.getString("Vector.MenuItem.ToOrigin")); //$NON-NLS-1$
    aTailsToOriginItem.setText(TrackerRes.getString("Vector.MenuItem.ToOrigin")); //$NON-NLS-1$
    vTailsToPositionItem.setText(TrackerRes.getString("PointMass.MenuItem.VectorsToPosition")); //$NON-NLS-1$
    aTailsToPositionItem.setText(TrackerRes.getString("PointMass.MenuItem.VectorsToPosition")); //$NON-NLS-1$
    vVisibleItem.setText(TrackerRes.getString("TTrack.MenuItem.Visible")); //$NON-NLS-1$
    aVisibleItem.setText(TrackerRes.getString("TTrack.MenuItem.Visible")); //$NON-NLS-1$
    menu.add(velocityMenu);
    menu.add(accelerationMenu);
    // replace delete item
    if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
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
   * @return a list of components
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
    ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    list.add(massLabel);
    if (this.getClass()==PointMass.class)
    	massField.setEnabled(!isLocked());
    massField.setValue(getMass());
    list.add(massField);
    list.add(mSeparator);
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
    if (step instanceof VectorStep) {
    	boolean xMass = TToolBar.getToolbar(trackerPanel).xMassButton.isSelected();    	
      if (isVelocity(step)) {
        if (xMass) {
        	xLabel.setText("px"); //$NON-NLS-1$
        	yLabel.setText("py"); //$NON-NLS-1$
        	magLabel.setText("p"); //$NON-NLS-1$
        }
        else {
        	xLabel.setText("vx"); //$NON-NLS-1$
        	yLabel.setText("vy"); //$NON-NLS-1$
        	magLabel.setText("v"); //$NON-NLS-1$
        }
      }
      else if (isAcceleration(step)) {
        if (xMass) {
        	xLabel.setText("Fx"); //$NON-NLS-1$
        	yLabel.setText("Fy"); //$NON-NLS-1$
        	magLabel.setText("F"); //$NON-NLS-1$
        }
        else {
        	xLabel.setText("ax"); //$NON-NLS-1$
        	yLabel.setText("ay"); //$NON-NLS-1$
        	magLabel.setText("a"); //$NON-NLS-1$
        }
      }
      xField.setEnabled(false);
      yField.setEnabled(false);
      magField.setEnabled(false);
      angleField.setEnabled(false);
    }
    else {
    	xLabel.setText("x"); //$NON-NLS-1$
    	yLabel.setText("y"); //$NON-NLS-1$
    	magLabel.setText("r"); //$NON-NLS-1$
      xField.setEnabled(!isLocked());
      yField.setEnabled(!isLocked());
      magField.setEnabled(!isLocked());
      angleField.setEnabled(!isLocked());
    }

    list.add(stepLabel);
    list.add(stepValueLabel);
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
    return list;
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

    public void saveObject(XMLControl control, Object obj) {
      PointMass p = (PointMass)obj;
      // save mass
      control.setValue("mass", p.getMass()); //$NON-NLS-1$
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      // save velocity and acceleration footprint and color if not default
      Footprint fp = p.getVelocityFootprint();
      if (!fp.getColor().equals(p.getColor())) {
    	  control.setValue("velocity_color", fp.getColor()); //$NON-NLS-1$
      }
      if (!fp.getName().equals(p.getVelocityFootprints()[0].getName())) {
    	  control.setValue("velocity_footprint", fp.getName()); //$NON-NLS-1$
      }
      fp = p.getAccelerationFootprint();
      if (!fp.getColor().equals(p.getColor())) {
    	  control.setValue("acceleration_color", fp.getColor()); //$NON-NLS-1$
      }
      if (!fp.getName().equals(p.getAccelerationFootprints()[0].getName())) {
    	  control.setValue("acceleration_footprint", fp.getName()); //$NON-NLS-1$
      }
      // save step data if not dependent
      if (!p.isDependent()) {
	      Step[] steps = p.getSteps();
	      FrameData[] data = new FrameData[steps.length];
	      for (int n = 0; n < steps.length; n++) {
	        if (steps[n] == null) continue;
	        data[n] = new FrameData((PositionStep)steps[n]);
	      }
	      control.setValue("framedata", data); //$NON-NLS-1$
      }
    }

    public Object createObject(XMLControl control){
      return new PointMass();
    }

    public Object loadObject(XMLControl control, Object obj) {
      PointMass p = (PointMass)obj;
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      boolean locked = p.isLocked();
      p.setLocked(false);
      // load mass
      double m = control.getDouble("mass"); //$NON-NLS-1$
      if (m != Double.NaN) {
        p.setMass(m);
      }
      // load velocity and acceleration footprint and color
      Color c = (Color)control.getObject("velocity_color"); //$NON-NLS-1$
      if (c!=null) p.setVelocityColor(c);
      else p.setVelocityColor(p.getColor());
      String s = control.getString("velocity_footprint"); //$NON-NLS-1$
      if (s!=null) p.setVelocityFootprint(s);
      else p.setVelocityFootprint(p.getVelocityFootprints()[0].getName());
      
      c = (Color)control.getObject("acceleration_color"); //$NON-NLS-1$
      if (c!=null) p.setAccelerationColor(c);
      else p.setAccelerationColor(p.getColor());
      s = control.getString("acceleration_footprint"); //$NON-NLS-1$
      if (s!=null) p.setAccelerationFootprint(s);
      else p.setAccelerationFootprint(p.getAccelerationFootprints()[0].getName());
      
      // load step data
      FrameData[] data = (FrameData[])control.getObject("framedata"); //$NON-NLS-1$
      if (data != null) {
        for (int n = 0; n < data.length; n++) {
          if (data[n] == null) {
          	p.steps.setStep(n, null);
          	continue;
          }
          PositionStep step = (PositionStep)p.getStep(n);
          if (step!=null) {
          	step.getPosition().setLocation(data[n].x, data[n].y);
          	step.erase();
          }
          else {
          	p.createStep(n, data[n].x, data[n].y);
          }
        }
        if (!p.isDependent()) {
	        // delete existing steps, if any, beyond the frame data length
		      Step[] steps = p.getSteps();
		      for (int n = data.length; n < steps.length; n++) {
	        	p.steps.setStep(n, null);
		      }
        }
        
        p.updateDerivatives();
    	  p.support.firePropertyChange("data", null, null); //$NON-NLS-1$
      }
      p.setLocked(locked);
      return obj;
    }
  }

//__________________________ protected methods ___________________________

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    // create toolbar components
    // mass field
    massLabel = new JLabel("m"); //$NON-NLS-1$
    massLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
    massField = new NumberField(5);
    massField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	      setMass(massField.getValue());
        massField.setValue(getMass());
        massField.requestFocusInWindow();
      }
    });
    // add focus listener
    massField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setMass(massField.getValue());
        massField.setValue(getMass());
      }
    });
    massField.setMinValue(MINIMUM_MASS);
    massField.setBorder(xField.getBorder());
    ChangeListener xyListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        setXY();
      }
    };
    xSpinner.addChangeListener(xyListener);
    // xy action
    Action xyAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        setXY();
        ((NumberField)e.getSource()).requestFocusInWindow();
      }
    };
    // xy focus listener
    FocusListener xyFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setXY();
      }
    };
    // magAngle action
    Action magAngleAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        setMagAngle();
        ((NumberField)e.getSource()).requestFocusInWindow();
      }
    };
    // magAngle focus listener
    FocusListener magAngleFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setMagAngle();
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
    mSeparator = Box.createRigidArea(new Dimension(4, 4));
    autotrackItem = new JMenuItem(TrackerRes.getString("PointMass.MenuItem.Autotrack")); //$NON-NLS-1$
    autotrackItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	AutoTracker autotracker = trackerPanel.getAutoTracker();
      	autotracker.setTrack(PointMass.this);
      	autotracker.getWizard().setVisible(true);
        trackerPanel.repaint();
      }
    });
    vFootprintMenu = new JMenu();
    aFootprintMenu = new JMenu();
    velocityMenu = new JMenu();
    accelerationMenu = new JMenu();
    vColorItem = new JMenuItem();
    vColorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // show color chooser dialog with color of velocity footprint
	    	Color c = getVelocityFootprint().getColor();
	    	Color newColor = chooseColor(c, TrackerRes.getString("Velocity.Dialog.Color.Title")); //$NON-NLS-1$
        if (newColor != null) {
        	XMLControl control = new XMLControlElement(PointMass.this);
        	for (Footprint footprint: getVelocityFootprints()) {
	        	footprint.setColor(newColor);        		
        	}
        	Undo.postTrackEdit(PointMass.this, control);
        	repaint();
        }
      }
    });
    aColorItem = new JMenuItem();
    aColorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // show color chooser dialog with color of acceleration footprint
      	Color c = getAccelerationFootprint().getColor();
	    	Color newColor = chooseColor(c, TrackerRes.getString("Acceleration.Dialog.Color.Title")); //$NON-NLS-1$
        if (newColor != null) {
        	XMLControl control = new XMLControlElement(PointMass.this);
        	for (Footprint footprint: getAccelerationFootprints()) {
	        	footprint.setColor(newColor);        		
        	}
        	Undo.postTrackEdit(PointMass.this, control);
        	repaint();
        }
      }
    });

    vTailsToOriginItem = new JMenuItem();
    aTailsToOriginItem = new JMenuItem();
    vTailsToPositionItem = new JMenuItem();
    aTailsToPositionItem = new JMenuItem();
    vVisibleItem = new JCheckBoxMenuItem();
    aVisibleItem = new JCheckBoxMenuItem();
    velocityMenu.add(vColorItem); 
    velocityMenu.add(vFootprintMenu);
    velocityMenu.addSeparator();
    velocityMenu.add(vTailsToOriginItem);
    velocityMenu.add(vTailsToPositionItem);
    accelerationMenu.add(aColorItem); 
    accelerationMenu.add(aFootprintMenu);
    accelerationMenu.addSeparator();
    accelerationMenu.add(aTailsToOriginItem);
    accelerationMenu.add(aTailsToPositionItem);
    vVisibleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // set velocity visibility on all panels that draw this
        Iterator<TrackerPanel> it = PointMass.this.panels.iterator();
        while (it.hasNext()) {
          TrackerPanel panel = it.next();
          setVVisible(panel, vVisibleItem.isSelected());
          panel.repaint();
        }
      }
    });
    aVisibleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // set accel visibility on all panels
        Iterator<TrackerPanel> it = PointMass.this.panels.iterator();
        while (it.hasNext()) {
          TrackerPanel panel = it.next();
          setAVisible(panel, aVisibleItem.isSelected());
          panel.repaint();
        }
      }
    });
    vTailsToOriginItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        snapToOrigin("v"); //$NON-NLS-1$
      }
    });
    aTailsToOriginItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        snapToOrigin("a"); //$NON-NLS-1$
      }
    });
    vTailsToPositionItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        snapToPosition("v"); //$NON-NLS-1$
      }
    });
    aTailsToPositionItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        snapToPosition("a"); //$NON-NLS-1$
      }
    });

  }

  /**
   * Gets the velocity StepArray for the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return the velocity StepArray
   */
  protected StepArray getVArray(TrackerPanel trackerPanel) {
    StepArray v = vMap.get(trackerPanel);
    if (v == null) {
      v = new StepArray();
      vMap.put(trackerPanel, v);
      StepArray a = new StepArray();
      aMap.put(trackerPanel, a);
      updateDerivatives(trackerPanel);
    }
    return v;
  }

  /**
   * Gets the acceleration StepArray for the specified panel.
   *
   * @param trackerPanel the tracker panel
   * @return the acceleration StepArray
   */
  protected StepArray getAArray(TrackerPanel trackerPanel) {
    StepArray a = aMap.get(trackerPanel);
    if (a == null) {
      StepArray v = new StepArray();
      vMap.put(trackerPanel, v);
      a = new StepArray();
      aMap.put(trackerPanel, a);
    }
    return a;
  }

  /**
   * Sets the position of the currently selected point based on the values
   * in the x and y fields.
   */
  private void setXY() {
    double xValue = xField.getValue();
    double yValue = yField.getValue();
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel trackerPanel = it.next();
      TPoint p = trackerPanel.getSelectedPoint();
      Step step = getStep(p, trackerPanel);
      if (step != null) {
        ImageCoordSystem coords = trackerPanel.getCoords();
        double x = coords.worldToImageX(
            trackerPanel.getFrameNumber(), xValue, yValue);
        double y = coords.worldToImageY(
            trackerPanel.getFrameNumber(), xValue, yValue);
        p.setXY(x, y);
        Point2D worldPt = p.getWorldPosition(trackerPanel);
        xField.setValue(worldPt.getX());
        yField.setValue(worldPt.getY());
        magField.setValue(worldPt.distance(0, 0));
        double theta = Math.atan2(worldPt.getY(), worldPt.getX());
        angleField.setValue(theta);
        p.showCoordinates(trackerPanel);
      }
    }
  }

  /**
   * Sets the position of the currently selected point based on the values
   * in the magnitude and angle fields.
   */
  private void setMagAngle() {
    double theta = angleField.getValue();
    double xval = magField.getValue() * Math.cos(theta);
    double yval = magField.getValue() * Math.sin(theta);
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel trackerPanel = it.next();
      TPoint p = trackerPanel.getSelectedPoint();
      Step step = getStep(p, trackerPanel);
      if (step != null) {
        ImageCoordSystem coords = trackerPanel.getCoords();
        double x = coords.worldToImageX(trackerPanel.getFrameNumber(),
                                        xval,
                                        yval);
        double y = coords.worldToImageY(trackerPanel.getFrameNumber(),
                                        xval,
                                        yval);
        p.setXY(x, y);
        Point2D worldPt = p.getWorldPosition(trackerPanel);
        xField.setValue(worldPt.getX());
        yField.setValue(worldPt.getY());
        magField.setValue(worldPt.distance(0, 0));
        theta = Math.atan2(worldPt.getY(), worldPt.getX());
        angleField.setValue(theta);
        p.showCoordinates(trackerPanel);
      }
    }
  }

  /**
   * Snaps vectors to the origin.
   */
  private void snapToOrigin(String type) {
    // snap all vectors to the snapPoint at origin
    Iterator<TrackerPanel> it = PointMass.this.panels.iterator();
    while (it.hasNext()) {
      TrackerPanel panel = it.next();
      TPoint p = panel.getSnapPoint();
      Step[] steps = null;
      if (type.equals("v")) steps = PointMass.this.getVelocities(panel); //$NON-NLS-1$
      else steps = PointMass.this.getAccelerations(panel);
      for (int i = 0; i < steps.length; i++) {
        if (steps[i] != null) {
          VectorStep a = (VectorStep)steps[i];
          if (a.chain != null) a.chain.clear();
          // detach any existing point
          a.attach(null);
          a.attach(p);
        }
      }
      panel.repaint();
    }
    if (type.equals("v")) vAtOrigin = true; //$NON-NLS-1$
    else aAtOrigin = true;
  }
  
  /**
   * Snaps vectors to the origin.
   */
  private void snapToPosition(String type) {
    // snap all vectors to the snapPoint
    Iterator<TrackerPanel> it = PointMass.this.panels.iterator();
    while (it.hasNext()) {
      TrackerPanel panel = it.next();
      Step[] steps = null;
      if (type.equals("v")) steps = PointMass.this.getVelocities(panel); //$NON-NLS-1$
      else steps = PointMass.this.getAccelerations(panel);
      for (int i = 0; i < steps.length; i++) {
        if (steps[i] != null) {
          VectorStep v = (VectorStep)steps[i];
          PositionStep p = (PositionStep)getStep(v.n);
          if (v.chain != null) v.chain.clear();
          // detach any existing point
          v.attach(null);
          v.attach(p.getPosition());
        }
      }
      panel.repaint();
    }
    if (type.equals("v")) vAtOrigin = false; //$NON-NLS-1$
    else aAtOrigin = false;
  }
  
  /**
   * Inner class containing the position data for a single frame number.
   */
  public static class FrameData {
    double x, y;
    FrameData() {/** empty block */}
    FrameData(PositionStep p) {
      x = p.getPosition().getX();
      y = p.getPosition().getY();
    }
  }

  /**
   * A class to save and load a FrameData.
   */
  private static class FrameDataLoader implements XML.ObjectLoader {

    public void saveObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      control.setValue("x", data.x); //$NON-NLS-1$
      control.setValue("y", data.y); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new FrameData();
    }

    public Object loadObject(XMLControl control, Object obj) {
      FrameData data = (FrameData) obj;
      data.x = control.getDouble("x"); //$NON-NLS-1$
      data.y = control.getDouble("y"); //$NON-NLS-1$
      return obj;
    }
  }

}

