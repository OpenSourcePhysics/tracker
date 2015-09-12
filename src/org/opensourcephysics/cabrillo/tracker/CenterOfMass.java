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

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * A CenterOfMass tracks the position, velocity and acceleration
 * of the center of mass of a set of point mass objects.
 *
 * @author Douglas Brown
 */
public class CenterOfMass extends PointMass {
	
  // instance fields
  protected PointMass[] masses;
  protected ArrayList<String> massNames = new ArrayList<String>();
  protected JMenuItem inspectorItem;
  protected DatasetManager newData;
  protected CenterOfMassInspector inspector;

  /**
   * Constructs an empty CenterOfMass.
   */
  public CenterOfMass() {
    this(new PointMass[0]);
  }

  /**
   * Constructs a CenterOfMass with specified masses.
   *
   * @param masses an array of point masses
   */
  public CenterOfMass(PointMass[] masses) {
    super();
		defaultColors = new Color[] {new Color(51, 204, 51)};
    massField.setMinValue(0);
    setName(TrackerRes.getString("CenterOfMass.New.Name")); //$NON-NLS-1$
    setFootprints(new Footprint[]
      {PointShapeFootprint.getFootprint("Footprint.Spot"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.SolidDiamond"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.SolidTriangle"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.SolidCircle"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldVerticalLine"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldHorizontalLine"), //$NON-NLS-1$
       new PositionVectorFootprint(this, "Footprint.BoldPositionVector", 2)}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    this.masses = masses;
    setColor(defaultColors[0]);
    for (int i = 0; i < masses.length; i++)
      masses[i].addPropertyChangeListener(this);
    locked = true;
    // set initial hint
    if (masses.length == 0)
    	hint = TrackerRes.getString("CenterOfMass.Empty.Hint"); //$NON-NLS-1$
    update();
  }

  /**
   * Overrides PointMass draw method.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    // add masses listed in massNames
    if (!massNames.isEmpty() && panel instanceof TrackerPanel) {
      TrackerPanel trackerPanel = (TrackerPanel) panel;
      if (trackerPanel instanceof WorldTView) {
      	trackerPanel = ((WorldTView)trackerPanel).getTrackerPanel();
      }
      ArrayList<PointMass> masses = trackerPanel.getDrawables(PointMass.class);
      Iterator<String> it = massNames.iterator();
      while (it.hasNext()) {
        String name = it.next();
        for (PointMass mass: masses) {
          if (mass.getName().equals(name))
            addMass(mass);
        }
      }
      massNames.clear();
    }
    super.draw(panel, _g);
  }

  /**
   * Adds a mass to the cm system.
   *
   * @param m the mass
   */
  public void addMass(PointMass m) {
    synchronized(masses) {
      // don't add if already present
      for (int i = 0; i < masses.length; i++) {
        if (masses[i] == m) return;
      }
      PointMass[] newMasses = new PointMass[masses.length + 1];
      System.arraycopy(masses, 0, newMasses, 0, masses.length);
      newMasses[masses.length] = m;
      masses = newMasses;
      m.addPropertyChangeListener(this);
    }
    update();
  }

  /**
   * Removes a mass from the cm system.
   *
   * @param m the mass
   */
  public void removeMass(PointMass m) {
    synchronized(masses) {
      for (int i = 0; i < masses.length; i++)
        if (masses[i] == m) {
          m.removePropertyChangeListener(this);
          PointMass[] newMasses = new PointMass[masses.length - 1];
          System.arraycopy(masses, 0, newMasses, 0, i);
          System.arraycopy(masses, i+1, newMasses, i, newMasses.length-i);
          masses = newMasses;
          break;
        }
    }
    update();
  }

  /**
   * Gets the array of masses in this cm.
   *
   * @return a shallow clone of the masses array
   */
  public PointMass[] getMasses() {
    synchronized(masses) {
      return masses.clone();
    }
  }

  /**
   * Determines if the specified point mass is in this center of mass.
   *
   * @param m the point mass
   * @return <code>true</code> if m is in this cm
   */
  public boolean containsMass(PointMass m) {
    synchronized(masses) {
      for (int i = 0; i < masses.length; i++) {
        if (masses[i] == m) return true;
      }
      return false;
    }
  }

  /**
   * Overrides PointMass findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step or motion vector that is hit
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	Interactive ia = super.findInteractive(panel, xpix, ypix);
    if (ia instanceof PositionStep.Position) {
      hint = TrackerRes.getString("PointMass.Position.Locked.Hint"); //$NON-NLS-1$
    }
    else if (masses.length == 0) {
      hint = TrackerRes.getString("CenterOfMass.Empty.Hint"); //$NON-NLS-1$
    }
    else hint = null;
  	return ia;
  }
  
  @Override
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	if (inspector!=null && inspector.isVisible()) {
  		// call setVisible to force inspector to resize itself
  		inspector.setVisible(true);
  	}
  }

  /**
   * Overrides TTrack setLocked method. CenterOfMass is always locked.
   *
   * @param locked ignored
   */
  public void setLocked(boolean locked) {/** empty block */}

  /**
   * Overrides PointMass setMass method. Mass is determined by masses.
   *
   * @param mass ignored
   */
  public void setMass(double mass) {/** empty block */}

  /**
   * Overrides TTrack isStepComplete method. Always returns true.
   *
   * @param n the frame number
   * @return <code>true</code> always since cm gets data from point masses
   */
  public boolean isStepComplete(int n) {
    return true;
  }

  /**
   * Overrides TTrack isDependent method to return true.
   *
   * @return <code>true</code> if this track is dependent
   */
  public boolean isDependent() {
    return true;
  }

  /**
   * Determines if any point in this track is autotrackable.
   *
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable() {
  	return false;
  }
  
  /**
   * Responds to property change events. CenterOfMass responds to the
   * following events: "track" from tracker panel, "mass" and "step"
   * from PointMass, "steps" from ParticleModel.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("track") && e.getNewValue() == null) { // track deleted //$NON-NLS-1$
      TTrack track = (TTrack)e.getOldValue();
      if (track instanceof PointMass)
        removeMass((PointMass)track);
    }
    if (e.getSource() instanceof PointMass) {
      if (name.equals("mass")) //$NON-NLS-1$
        update();
      else if (name.equals("step")){ //$NON-NLS-1$
        int n = ((Integer)e.getNewValue()).intValue();
        update(n, true);
      }
      else if (name.equals("steps")){ //$NON-NLS-1$
        update();
      }
    }
    else super.propertyChange(e);
  }

  /**
   * Cleans up associated resources when this track is deleted or cleared.
   */
  protected void cleanup() {
  	super.cleanup();
		if (inspector != null) inspector.dispose();
  }

  /**
   * Updates all cm steps.
   */
  private void update() {
    // update mass and count steps
    mass = 0;
    int length = getSteps().length;
    for (int i = 0; i < masses.length; i++) {
      mass += masses[i].getMass();
      length = Math.max(length, masses[i].getSteps().length);
    }
    // update steps
    for (int n = 0; n < length; n++)
      update(n, false);
	  updateDerivatives();
	  support.firePropertyChange("steps", null, null); //$NON-NLS-1$
    repaint();
    // update inspector, if visible
    if (inspector != null &&
        inspector.isVisible()) {
      inspector.updateDisplay();
    }
  }

  /**
   * Updates the specified cm step.
   *
   * @param n the frame number
   */
  private void update(int n, boolean firePropertyChange) {
    if (mass == 0) {   // delete cm step, if any
    	if (firePropertyChange) {
	      locked = false;
	      deleteStep(n);
    	}
    	else {
        steps.setStep(n, null);    		
    	}
      locked = true;
      return;
    }
    double x = 0, y = 0; // cm x and y coordinates in imagespace
    // determine cm step position in imagespace
    for (int i = 0; i < masses.length; i++) {
      PositionStep step = (PositionStep)masses[i].getStep(n);
      if (step == null) {           // if any mass data missing,
        if (getStep(n) != null) {   // delete existing cm step if any
        	if (firePropertyChange) {
    	      locked = false;
            Step deletedStep = deleteStep(n);
            repaint(deletedStep);
        	}
        	else {
            steps.setStep(n, null);    		
        	}
          locked = true;
        }
        return;
      }

      double m = masses[i].getMass();
      x += m * step.getPosition().getX();
      y += m * step.getPosition().getY();
    }

    x /= mass;	// cm x coordinate
    y /= mass;	// cm y coordinate

    // create cm step if none exists
    PositionStep cmStep = (PositionStep)getStep(n);
    if (cmStep == null) {
      if (firePropertyChange) {
	      locked = false;
      	cmStep = (PositionStep)createStep(n, x, y);
        repaint(cmStep);
      }
      else {
        cmStep = new PositionStep(this, n, x, y);
        steps.setStep(n, cmStep);
        cmStep.setFootprint(getFootprint());
      }
    }
    // or set position of existing cm step
    else {
      if (firePropertyChange) {
        locked = false;
      	cmStep.getPosition().setXY(x, y);
      }
      else {
      	point.setLocation(x, y);
      	cmStep.getPosition().setPosition(point);
      }
    }
    locked = true;
  }

  /**
   * Returns a menu with items that control this track.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    // create a cm inspector item
    inspectorItem = new JMenuItem(TrackerRes.getString("CenterOfMass.MenuItem.Inspector")); //$NON-NLS-1$
    inspectorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CenterOfMassInspector inspector = getInspector();
        inspector.updateDisplay();
        inspector.setVisible(true);
      }
    });
    // assemble the menu
    JMenu menu = super.getMenu(trackerPanel);
    // remove unwanted menu items and separators
    menu.remove(lockedItem);
    menu.remove(autoAdvanceItem);
    menu.remove(markByDefaultItem);
    menu.insert(inspectorItem, 0);
    if (menu.getItemCount() > 1)
      menu.insertSeparator(1);
    // eliminate any double separators
    Object prevItem = inspectorItem;
    int n = menu.getItemCount();
    for (int j = 1; j < n; j++) {
    	Object item = menu.getItem(j);
      if (item == null && prevItem == null) { // found extra separator
      	menu.remove(j-1);
      	j = j-1;
      	n = n-1;
      }
      prevItem = item;
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
    massField.setEnabled(false);
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
    xField.setEnabled(false);
    yField.setEnabled(false);
    return list;
  }

  /**
   * Overrides PointMass toString method.
   *
   * @return a description of this object
   */
  public String toString() {
    return TrackerRes.getString("CenterOfMass.Name"); //$NON-NLS-1$
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
      CenterOfMass cm = (CenterOfMass)obj;
      // save mass names
      ArrayList<String> list = new ArrayList<String>();
      PointMass[] masses = cm.getMasses();
      for (int i = 0; i < masses.length; i++) {
        list.add(masses[i].getName());
      }
      control.setValue("masses", list); //$NON-NLS-1$
      // save point mass data
      XML.getLoader(PointMass.class).saveObject(control, obj);
    }

    /**
     * Creates a new object.
     *
     * @param control an XMLControl
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new CenterOfMass();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      CenterOfMass cm = (CenterOfMass)obj;
      XML.getLoader(PointMass.class).loadObject(control, obj);
      // load mass names
      Collection<?> names = Collection.class.cast(control.getObject("masses")); //$NON-NLS-1$
      Iterator<?> it = names.iterator();
      while (it.hasNext()) {
        cm.massNames.add((String)it.next());
      }
      return obj;
    }
  }

  /**
   * Gets the center of mass inspector.
   *
   * @return the center of mass inspector
   */
  public CenterOfMassInspector getInspector() {
    if (inspector == null) {
      inspector = new CenterOfMassInspector(this);
      inspector.setLocation(200, 200);
    }
    return inspector;
  }

}

