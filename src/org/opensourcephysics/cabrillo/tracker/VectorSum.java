/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * A VectorSum draws a series of VectorSteps that represent a
 * vector sum of its set of vectors.
 *
 * @author Douglas Brown
 */
public class VectorSum extends Vector {

  // instance fields
  protected Vector[] vectors;
  protected ArrayList<String> vectorNames = new ArrayList<String>();
  protected JMenuItem inspectorItem;
  protected Map<Integer, TPoint> tails = new HashMap<Integer, TPoint>();
  protected VectorSumInspector inspector;

  /**
   * Constructs an empty VectorSum.
   */
  public VectorSum() {
    this(new Vector[0]);
  }

  /**
   * Constructs a VectorSum with specified vectors.
   *
   * @param vectors an array of vectors
   */
  public VectorSum(Vector[] vectors) {
    super();
		defaultColors = new Color[] {new Color(51, 204, 51)};
    setName(TrackerRes.getString("VectorSum.New.Name")); //$NON-NLS-1$
    setFootprints(new Footprint[]
           {LineFootprint.getFootprint("Footprint.BoldArrow"), //$NON-NLS-1$
    				LineFootprint.getFootprint("Footprint.Arrow"), //$NON-NLS-1$
    				LineFootprint.getFootprint("Footprint.BigArrow")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    Footprint[] footprint = getFootprints();
    for (int i = 0; i < footprint.length; i++) {
      if (footprint[i] instanceof ArrowFootprint) {
        ArrowFootprint arrow = (ArrowFootprint) footprint[i];
        arrow.setDashArray(LineFootprint.DASHED_LINE);
      }
    }
    this.vectors = vectors;
    setColor(defaultColors[0]);
    for (int i = 0; i < vectors.length; i++) {
      vectors[i].addPropertyChangeListener(PROPERTY_TTRACK_STEP, this); //$NON-NLS-1$
    }
    locked = true;
    // set initial hint
    if (vectors.length == 0)
    	hint = TrackerRes.getString("VectorSum.Empty.Hint"); //$NON-NLS-1$
    update();
  }

	/**
	 * Overrides Vector draw method.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		if (!initialized)
			initialize((TrackerPanel) panel);
		super.draw(panel, _g);
	}

	@Override
	public void initialize(TrackerPanel panel) {
		if (initialized)
			return;
		for (int i = 0, ni = vectorNames.size(); i < ni; i++) {
			String name = vectorNames.get(i);
			Vector v = tp.getTrackByName(Vector.class, name);
			if (v != null)
				addVector(v);
		}
		vectorNames.clear();
		initialized = true;
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
  @Override
public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	Interactive ia = super.findInteractive(panel, xpix, ypix);
  	if (ia instanceof VectorStep.Handle) {
  	  hint = TrackerRes.getString("Vector.Handle.Hint"); //$NON-NLS-1$
  	}
    else if (vectors.length == 0) {
      hint = TrackerRes.getString("CenterOfMass.Empty.Hint"); //$NON-NLS-1$
    }
    else hint = null;
  	return ia;
  }
  
  /**
   * Adds a vector to this sum.
   *
   * @param vec the vector
   */
  public void addVector(Vector vec) {
    synchronized(vectors) {
      // don't add if already present
      for (int i = 0; i < vectors.length; i++) {
        if (vectors[i] == vec) return;
      }
      Vector[] newVectors = new Vector[vectors.length + 1];
      System.arraycopy(vectors, 0, newVectors, 0, vectors.length);
      newVectors[vectors.length] = vec;
      vectors = newVectors;
      vec.addPropertyChangeListener(PROPERTY_TTRACK_STEP, this); //$NON-NLS-1$
    }
    update();
  }

  /**
   * Removes a vector from this sum.
   *
   * @param vec the vector
   */
  public void removeVector(Vector vec) {
    synchronized(vectors) {
      for (int i = 0; i < vectors.length; i++)
        if (vectors[i] == vec) {
          vec.removePropertyChangeListener(PROPERTY_TTRACK_STEP, this); //$NON-NLS-1$
          Vector[] newVectors = new Vector[vectors.length - 1];
          System.arraycopy(vectors, 0, newVectors, 0, i);
          System.arraycopy(vectors, i+1, newVectors, i, newVectors.length-i);
          vectors = newVectors;
          break;
        }
    }
    update();
  }

  /**
   * Gets the array of vectors in this sum.
   *
   * @return a shallow clone of the vectors array
   */
  public Vector[] getVectors() {
    synchronized(vectors) {
      return vectors.clone();
    }
  }

  /**
   * Determines if the specified vector is in this sum.
   *
   * @param vec the vector
   * @return <code>true</code> if vector is in this sum
   */
  public boolean contains(Vector vec) {
    synchronized(vectors) {
      for (int i = 0; i < vectors.length; i++) {
        if (vectors[i] == vec) return true;
      }
      return false;
    }
  }

  /**
   * Overrides vector method.
   * Saves the specified tail position, then updates the step.
   *
   * @param n the frame number
   * @param x the tail x coordinate in image space
   * @param y the tail y coordinate in image space
   * @param xc ignored
   * @param yc ignored
   * @return the new step
   */
  @Override
public Step createStep(int n, double x, double y, double xc, double yc) {
    if (isLocked()) {
      tails.put(new Integer(n), new TPoint(x, y));
      update(n);
      return getStep(n);
    }
    return super.createStep(n, x, y, xc, yc);
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
   * Overrides TTrack setLocked method. VectorSum is always locked.
   *
   * @param locked ignored
   */
  @Override
public void setLocked(boolean locked) {/** empty block */}

  /**
   * Overrides TTrack isStepComplete method. Always returns true.
   *
   * @param n the frame number
   * @return <code>true</code> always since sum gets data from vectors
   */
  @Override
public boolean isStepComplete(int n) {
    return true;
  }

  /**
   * Overrides TTrack isDependent method to return true.
   *
   * @return <code>true</code> since sum is dependent on its vectors
   */
  @Override
public boolean isDependent() {
    return true;
  }

	/**
	 * Adds events for TrackerPanel.
	 * 
	 * @param panel the new TrackerPanel
	 */
	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null) {			
			tp.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this);
		}
		super.setTrackerPanel(panel);
		if (tp != null) {
			tp.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this);
		}
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getOldValue() != null) { // track deleted //$NON-NLS-1$
				TTrack track = (TTrack) e.getOldValue();
				if (track.ttype == TTrack.TYPE_VECTOR)
					removeVector((Vector) track);
			}
			break;
		case PROPERTY_TTRACK_STEP:
			if (e.getSource() instanceof Vector) {
				int n = ((Integer) e.getNewValue()).intValue();
				update(n);
				return;
			}
			break;
		}
		super.propertyChange(e);
	}

  /**
   * Cleans up associated resources when this track is deleted or cleared.
   */
  @Override	
  public void dispose() {
  	super.dispose();
    for (Vector v: vectors) {
      if (v!=null) {
      	v.removePropertyChangeListener(PROPERTY_TTRACK_STEP, this); //$NON-NLS-1$
      }
    }
    vectors = new Vector[0];
		if (inspector != null) inspector.dispose();
  }

  /**
   * Updates all steps.
   */
  private void update() {
    int length = getSteps().length;
    for (int n = 0; n < length; n++)
      update(n);
  }

  /**
   * Updates the specified step.
   *
   * @param n the frame number
   */
  private void update(int n) {
    if (vectors.length == 0) {   // delete step, if any
      locked = false;
      VectorStep deletedStep = (VectorStep)deleteStep(n);
      if (deletedStep != null) {
        deletedStep.attach(null);
        repaintStep(deletedStep);
      }
      locked = true;
      return;
    }
    double x = 0, y = 0; // x and y components in imagespace
    // add components in imagespace
    for (int i = 0; i < vectors.length; i++) {
      VectorStep step = (VectorStep)vectors[i].getStep(n);
      if (step == null) {           // if any vector missing,
        if (getStep(n) != null) {   // delete existing step if any
          locked = false;
          VectorStep deletedStep = (VectorStep)deleteStep(n);
          tails.put(new Integer(n), deletedStep.getTail());
          deletedStep.attach(null);
          repaintStep(deletedStep);
          locked = true;
        }
        return;
      }

      x += step.getXComponent();
      y += step.getYComponent();
    }

    // create step if none exists
    VectorStep step = (VectorStep)getStep(n);
    if (step == null) {
      locked = false;
      VectorStep newStep = null;
      Integer i = new Integer(n);
      TPoint tail = tails.get(i);
      if (tail != null) {
        newStep = (VectorStep) createStep(n, tail.getX(), tail.getY(), x, y);
        tails.remove(i);
      }
      else {
        newStep = (VectorStep) createStep(n, 0, 0, x, y);
        newStep.attach(tp.getSnapPoint());
      }
      newStep.setTipEnabled(false);
      newStep.setDefaultPointIndex(2); // handle
      repaintStep(newStep);
      locked = true;
    }
    // or set components of existing step
    else {
      locked = false;
      step.setXYComponents(x, y);
      locked = true;
    }
  }

  /**
   * Returns a menu with items that control this track.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  @Override
public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu0) {
    // create a vector sum inspector item
    inspectorItem = new JMenuItem(
        TrackerRes.getString("VectorSum.MenuItem.Inspector")); //$NON-NLS-1$
    inspectorItem.addActionListener(new ActionListener() {
      @Override
	public void actionPerformed(ActionEvent e) {
        VectorSumInspector inspector = getInspector();
        inspector.updateDisplay();
        inspector.setVisible(true);
      }
    });
    return assembleMenu(super.getMenu(trackerPanel, menu0), inspectorItem);
  }

/**
   * Overrides TTrack getToolbarPointComponents method.
   *
   * @param trackerPanel the tracker panel
   * @param point the TPoint
   * @return a list of components
   */
  @Override
public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel,
                                             TPoint point) {
    ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
    xField.setEnabled(false);
    yField.setEnabled(false);
    return list;
  }

  /**
   * Overrides Object toString method.
   *
   * @return a description of this object
   */
  @Override
public String toString() {
    return TrackerRes.getString("VectorSum.Name") + " \"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    @Override
	public void saveObject(XMLControl control, Object obj) {
      VectorSum sum = (VectorSum) obj;
      // save names of vectors in this sum
      ArrayList<String> list = new ArrayList<String>();
      Vector[] vectors = sum.getVectors();
      for (int i = 0; i < vectors.length; i++) {
        list.add(vectors[i].getName());
      }
      control.setValue("vectors", list); //$NON-NLS-1$
      // save this vector data
      XML.getLoader(Vector.class).saveObject(control, obj);
    }

    /**
     * Creates a new object.
     *
     * @param control the XMLControl with the object data
     * @return the newly created object
     */
    @Override
	public Object createObject(XMLControl control) {
      return new VectorSum();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the element
     * @param obj the object
     * @return the loaded object
     */
    @Override
	public Object loadObject(XMLControl control, Object obj) {
      VectorSum sum = (VectorSum) obj;
      // load this vector data
      XML.getLoader(Vector.class).loadObject(control, obj);
      // load names of vectors in this sum
      ArrayList<?> names = ArrayList.class.cast(control.getObject("vectors")); //$NON-NLS-1$
      Iterator<?> it = names.iterator();
      while (it.hasNext()) {
        sum.vectorNames.add(it.next().toString());
      }
      return obj;
    }
  }

  /**
   * Gets the vector sum inspector.
   *
   * @return the vector sum inspector
   */
  public VectorSumInspector getInspector() {
    if (inspector == null) {
      inspector = new VectorSumInspector(this);
      inspector.setLocation(200, 200);
    }
    return inspector;
  }

}

