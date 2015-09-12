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

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;

import javax.swing.JMenu;

import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.PerspectiveFilter;
import org.opensourcephysics.media.core.TPoint;

/**
 * This is a track used for autotracking perspective filter corners.
 *
 * @author Douglas Brown
 */
public class PerspectiveTrack extends TTrack {
	
	PerspectiveFilter filter;
	
	/**
	 * Constructor requires a PerspectiveFilter to control.
	 *
	 * @param filter the filter
	 */
	public PerspectiveTrack(PerspectiveFilter filter) {
		this.filter = filter;
		this.viewable = false;
		CircleFootprint c = (CircleFootprint) CircleFootprint.getFootprint("CircleFootprint.Circle"); //$NON-NLS-1$
		c.setColor(filter.getColor());
		c.setSpotShown(false);
		c.setAlpha(0);
    setFootprints(new Footprint[] {c});
    setName(MediaRes.getString("Filter.Perspective.Title").toLowerCase()); //$NON-NLS-1$
    Step step = new PerspectiveStep(this, 0, 0, 0);
    step.setFootprint(getFootprint());
    steps = new StepArray(step);
    filter.addPropertyChangeListener("color", this); //$NON-NLS-1$
    filter.addPropertyChangeListener("visible", this); //$NON-NLS-1$
    filter.addPropertyChangeListener("enabled", this); //$NON-NLS-1$
    filter.addPropertyChangeListener("tab", this); //$NON-NLS-1$
    filter.addPropertyChangeListener("cornerlocation", this); //$NON-NLS-1$
	}
	
  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
  	String name = e.getPropertyName();
  	if (e.getSource()==filter) {
	  	if (name.equals("color")) { //$NON-NLS-1$
	  		setColor((Color)e.getNewValue());
	  	}
	  	else if (name.equals("enabled") || name.equals("tab") || name.equals("visible")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  		boolean visible = filter.hasInspector() && filter.getInspector().isVisible();
	  		boolean isInput = filter.isInputEnabled();
	  		boolean isActive = filter.isActive();
	  		boolean nullPoint = trackerPanel.getSelectedPoint()==null;
	  		if (visible && isActive && isInput && nullPoint) {
	  			trackerPanel.setSelectedTrack(this);
	  		}
	  		else if (trackerPanel.getSelectedTrack()==this) {
	  			trackerPanel.setSelectedTrack(null);  			
  				trackerPanel.setSelectedPoint(null);
	  		}
	  	}
	  	else if (name.equals("cornerlocation") && filter.isInputEnabled()) { //$NON-NLS-1$
	  		PerspectiveFilter.Corner filtercorner = (PerspectiveFilter.Corner)e.getNewValue();
	  		int i = filter.getCornerIndex(filtercorner);
	  		int n = trackerPanel.getFrameNumber();
	  		getStep(n).points[i].setXY(filtercorner.getX(), filtercorner.getY());
	  	}
  	}
  	if (name.equals("selectedtrack")) { //$NON-NLS-1$
  		if (e.getNewValue()==this) {
  			if (!filter.isEnabled()) filter.setEnabled(true);
  			if (!filter.isInputEnabled()) filter.setInputEnabled(true);
  			if (filter.hasInspector() && !filter.getInspector().isVisible()) filter.getInspector().setVisible(true);
  		}
  	}
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
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	return null;
  }
  /**
   * Prepares menu items and returns a new menu.
   * Subclasses should override this method and add track-specific menu items.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(final TrackerPanel trackerPanel) {
    menu = new JMenu(getName());
    menu.setIcon(getFootprint().getIcon(21, 16));
    return menu;
  }
	
  /**
   * Gets the step associated with a TPoint.
   *
   * @param p a TPoint
   * @param trackerPanel the tracker panel holding the TPoint
   * @return the step associated with the TPoint
   */
  public Step getStep(TPoint p, TrackerPanel trackerPanel) {
  	if (filter.isEnabled()) return null;
    if (p instanceof PerspectiveFilter.Corner) {
    	PerspectiveFilter.Corner corner = (PerspectiveFilter.Corner)p;
    	int i = filter.getCornerIndex(corner);
    	if (i>-1) {
    		return getStep(trackerPanel.getFrameNumber());
    	}
    }
    return super.getStep(p, trackerPanel);
  }

  /**
   * Deletes a step. This removes the perspective filter key frame data.
   *
   * @param n the frame number
   * @return the deleted step
   */
  public Step deleteStep(int n) {
    if (locked) return null;
    TPoint p = trackerPanel.getSelectedPoint();
    if (p instanceof PerspectiveFilter.Corner) {
      XMLControl control = new XMLControlElement(filter);
    	PerspectiveFilter.Corner corner = (PerspectiveFilter.Corner)p;
    	filter.deleteKeyFrame(n, corner);
      Undo.postFilterEdit(trackerPanel, filter, control);
    	trackerPanel.repaint();
    }
    Step step = getStep(n);
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
	@Override
  public TPoint autoMarkAt(int n, double x, double y) {
		int index = getTargetIndex();
		PerspectiveStep step = (PerspectiveStep)getStep(n);
		step.points[index].setXY(x, y);
  	filter.setCornerLocation(n, index, x, y);
  	return getMarkedPoint(n, index);
  }
  
  /**
   * Used by autoTracker to get the marked point for a given frame and index. 
   * 
   * @param n the frame number
   * @param index the index
   * @return the step TPoint at the index
   */
  public TPoint getMarkedPoint(int n, int index) {
  	Step step = getStep(n);
  	return step.points[index];
  }
  
  /**
   * Sets the target index for the autotracker.
   *
   * @param p a TPoint associated with this track
   */
	@Override
  protected void setTargetIndex(TPoint p) {
		Step step = getStep(p, trackerPanel);
		if (step!=null)
  		setTargetIndex(step.getPointIndex(p));  	
  }

  /**
   * Returns a description of a target point with a given index.
   *
   * @param pointIndex the index
   * @return the description
   */
	@Override
  protected String getTargetDescription(int pointIndex) {
  	return TrackerRes.getString("PerspectiveTrack.Corner")+" "+pointIndex; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Determines if the given point index is autotrackable.
   *
   * @param pointIndex the points[] index
   * @return true if autotrackable
   */
	@Override
  protected boolean isAutoTrackable(int pointIndex) {
  	return pointIndex<4;
  }
  
  /**
   * Determines if at least one point in this track is autotrackable.
   *
   * @return true if autotrackable
   */
	@Override
  protected boolean isAutoTrackable() {
  	return true;
  }
  

	
	@Override
  public void draw(DrawingPanel panel, Graphics _g) {
  }

	@Override
	public int getStepLength() {
		return 4;
	}

	@Override
	public int getFootprintLength() {
		return 1;
	}

	@Override
	public Step createStep(int n, double x, double y) {
		autoMarkAt(n, x, y);
		return getStep(n);
	}
	
	@Override
  public void remark(TrackerPanel trackerPanel) {
  }



}
