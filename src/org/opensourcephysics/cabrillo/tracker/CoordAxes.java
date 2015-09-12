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

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

/**
 * A CoordAxes displays and controls the image coordinate system of
 * a specified tracker panel.
 *
 * @author Douglas Brown
 */
public class CoordAxes extends TTrack {
	
	protected static Icon gridOptionsIcon;
	
	protected boolean notyetShown = true;
  protected JLabel originLabel;
  protected WorldGrid grid;
  protected JCheckBox gridCheckbox;
  protected TButton gridButton;
  protected Component gridSeparator;
  protected boolean gridVisible;
  
  static {
  	gridOptionsIcon = new ImageIcon(
        Tracker.class.getResource("resources/images/restore.gif")); //$NON-NLS-1$
  }
	
  /**
   * Constructs a CoordAxes.
   */
  public CoordAxes() {
		defaultColors = new Color[] {new Color(200, 0, 200)};
    setName(TrackerRes.getString("CoordAxes.New.Name")); //$NON-NLS-1$
    // set up footprint choices and color
    setFootprints(new Footprint[]
        {PointShapeFootprint.getFootprint("Footprint.BoldSimpleAxes"), //$NON-NLS-1$
         PointShapeFootprint.getFootprint("Footprint.SimpleAxes")}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setColor(defaultColors[0]);
    setViewable(false); // views ignore this track
    // set initial hint
  	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
  	hint = TrackerRes.getString("CoordAxes.Hint"); //$NON-NLS-1$
    // initialize the step array
    // step 0 is the only step needed
    Step step = new CoordAxesStep(this, 0);
    step.setFootprint(getFootprint());
    steps.setStep(0, step);
    // configure angle field components
    angleField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (trackerPanel == null) return;
        double theta = angleField.getValue();
        // get the origin and handle of the current step
        int n = trackerPanel.getFrameNumber();
        CoordAxesStep step = (CoordAxesStep)CoordAxes.this.getStep(n);
        TPoint origin = step.getOrigin();
        TPoint handle = step.getHandle();
        // move the handle to the new angle at same distance from origin
        double d = origin.distance(handle);
        double x = origin.getX() + d * Math.cos(theta);
        double y = origin.getY() - d * Math.sin(theta);
        handle.setXY(x, y);
        angleField.setValue(trackerPanel.getCoords().getAngle(n));
        angleField.requestFocusInWindow();
      }
    });
    angleField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (trackerPanel == null) return;
        double theta = angleField.getValue();
        // get the origin and handle of the current step
        int n = trackerPanel.getFrameNumber();
        CoordAxesStep step = (CoordAxesStep)CoordAxes.this.getStep(n);
        TPoint origin = step.getOrigin();
        TPoint handle = step.getHandle();
        // move the handle to the new angle at same distance from origin
        double d = origin.distance(handle);
        double x = origin.getX() + d * Math.cos(theta);
        double y = origin.getY() - d * Math.sin(theta);
        handle.setXY(x, y);
        angleField.setValue(trackerPanel.getCoords().getAngle(n));
      }
    });
    originLabel = new JLabel();
    final Action setOriginAction = new AbstractAction() {
    	public void actionPerformed(ActionEvent e) {
      	if (trackerPanel == null) return;
        double x = xField.getValue();
        double y = yField.getValue();
        ImageCoordSystem coords = trackerPanel.getCoords();
	      int n = trackerPanel.getFrameNumber();
	      coords.setOriginXY(n, x, y);
		    xField.setValue(coords.getOriginX(n));
		    yField.setValue(coords.getOriginY(n));
        CoordAxesStep step = (CoordAxesStep)CoordAxes.this.getStep(n);
        TPoint handle = step.getHandle();
	      if (handle==trackerPanel.getSelectedPoint()) {
	      	trackerPanel.setSelectedPoint(null);
	      }
      }
    };
    // configure x and y origin field components
    xField = new DecimalField(5, 2);
    yField = new DecimalField(5, 2);

    xField.addActionListener(setOriginAction);
    yField.addActionListener(setOriginAction);
    xField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	setOriginAction.actionPerformed(null);
      }
    });
    yField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	setOriginAction.actionPerformed(null);
      }
    });
    
    // grid items
  	grid = new WorldGrid();
  	grid.setVisible(gridVisible);

    gridSeparator = Box.createRigidArea(new Dimension(4, 4));
    gridCheckbox = new JCheckBox();
    gridCheckbox.setBorder(BorderFactory.createEmptyBorder());
    gridCheckbox.setOpaque(false);
    gridCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	setGridVisible(gridCheckbox.isSelected());
      }
    });

  	gridButton = new TButton(gridOptionsIcon) {
    	
    	public Dimension getMaximumSize() {
    		Dimension dim = super.getMaximumSize();
    		dim.height = TTrackBar.getTrackbar(trackerPanel).toolbarComponentHeight;
    		return dim;
    	}
    	
    	@Override
      protected JPopupMenu getPopup() {
      	JPopupMenu popup = new JPopupMenu();
      	JMenuItem colorItem = new JMenuItem(TrackerRes.getString("CoordAxes.MenuItem.GridColor")); //$NON-NLS-1$
        colorItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // show the grid if not visible
            if (!grid.isVisible()) {
            	gridCheckbox.doClick(0);
            }

          	Color color = grid.getColor();
          	Color newColor = chooseColor(color, TrackerRes.getString("CoordAxes.Dialog.GridColor.Title")); //$NON-NLS-1$
            if (newColor!=color) {
              grid.setColor(newColor);
              for (TrackerPanel next: panels) {
              	next.repaint();
              }
            }
          }
        });
      	popup.add(colorItem);
      	JMenuItem transparencyItem = new JMenuItem(TrackerRes.getString("CoordAxes.MenuItem.GridOpacity")); //$NON-NLS-1$
      	transparencyItem.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // show the grid if not visible
            if (!grid.isVisible()) {
            	gridCheckbox.doClick(0);
            }

          	// show a dialog with a transparency slider
          	int alpha = grid.getAlpha();
            final JSlider slider = new JSlider(0, 255, alpha);
            slider.setMaximum(255);
            slider.setMinimum(0);
            slider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            slider.addChangeListener(new ChangeListener() {
              public void stateChanged(ChangeEvent e) {
                grid.setAlpha(slider.getValue());
                for (TrackerPanel next: panels) {
                	next.repaint();
                }
              }
            });
            
          	int response = JOptionPane.showConfirmDialog(trackerPanel, slider, 
          			TrackerRes.getString("CoordAxes.Dialog.GridOpacity.Title"),  //$NON-NLS-1$
          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
          	if (response==JOptionPane.CANCEL_OPTION) {
              grid.setAlpha(alpha);
              for (TrackerPanel next: panels) {
              	next.repaint();
              }
          	}
           }
        });
      	popup.add(transparencyItem);
      	FontSizer.setFonts(popup, FontSizer.getLevel());
      	return popup;
      }
    	
    };
  }

  /**
   * Gets the origin.
   *
   * @return the current origin
   */
  public TPoint getOrigin() {
    return ((CoordAxesStep)getStep(0)).getOrigin();
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
   * Overrides TTrack setVisible method to change notyetShown flag.
   *
   * @param visible <code>true</code> to show this track
   */
  public void setVisible(boolean visible) {
  	super.setVisible(visible);
  	if (visible) {
  		notyetShown = false;
  		if (grid!=null) grid.setVisible(gridVisible);
  	}
  	else if (grid!=null) {
  		grid.setVisible(false);
  	}
  }

  /**
   * Sets the grid visibility.
   *
   * @param visible <code>true</code> to show the grid
   */
  public void setGridVisible(boolean visible) {
  	if (gridVisible==visible) return;
  	gridVisible = visible;
    grid.setVisible(gridVisible);
    gridCheckbox.setSelected(gridVisible);
    for (TrackerPanel next: panels) {
    	next.repaint();
    }
  }

  /**
   * Overrides TTrack setTrailVisible method to keep trails hidden.
   *
   * @param visible ignored
   */
  public void setTrailVisible(boolean visible) {/** empty block */}

  /**
   * Mimics step creation by setting the origin position.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the step
   */
  public Step createStep(int n, double x, double y) {
//    Step step = steps.getStep(n);
    Step step = getStep(0);
  	if (trackerPanel.getSelectedPoint() instanceof CoordAxesStep.Handle) {
  		((CoordAxesStep)step).getHandle().setXY(x, y);;
  	}
  	else ((CoordAxesStep)step).getOrigin().setXY(x, y);
    return step;
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
   * Overrides TTrack getStep method. Always return step 0.
   *
   * @param n the frame number (ignored)
   * @return step 0
   */
  public Step getStep(int n) {
    Step step = steps.getStep(0);
    // always erase since step position/shape may change
    // without calls to TPoint setXY method
    step.erase();
    return step;
  }

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public int getStepLength() {
  	return CoordAxesStep.getLength();
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
  	return true;
//  	return pointIndex==0; // origin only
  }
  
  /**
   * Returns a description of the point at a given index. Used by AutoTracker.
   *
   * @param pointIndex the points[] index
   * @return the description
   */
  protected String getTargetDescription(int pointIndex) {
  	if (pointIndex==0) {
  		return TrackerRes.getString("CoordAxes.Origin.Name"); //$NON-NLS-1$
  	}
		return TrackerRes.getString("CoordAxes.Handle.Name"); //$NON-NLS-1$
//  	return null;
  }
  
  @Override
  protected void setTrackerPanel(TrackerPanel panel) {
	  super.setTrackerPanel(panel);
  	trackerPanel.addDrawable(grid);
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
  	ImageCoordSystem coords = trackerPanel.getCoords();
  	if (getTargetIndex()==0) { // origin
  		if (coords.isFixedOrigin()) {
  			coords.setFixedOrigin(false);
  		}
  		TPoint origin = getOrigin();
  		origin.setXY(x, y);
  	}
  	else { // handle
  		if (coords.isFixedAngle()) {
  			coords.setFixedAngle(false);
  		}
			TPoint handle = ((CoordAxesStep)getStep(0)).getHandle();
			handle.setXY(x, y); 		
  	}
  	firePropertyChange("step", null, n); //$NON-NLS-1$
		return getMarkedPoint(n, getTargetIndex());
  }
  
  /**
   * Used by autoTracker to get the marked point for a given frame and index.
   * Overrides TTrack method.
   * 
   * @param n the frame number
   * @param index the index
   * @return a TPoint
   */
  public TPoint getMarkedPoint(final int n, int index) {    
    if (index==0) {
    	return new OriginPoint(n);
    }
		TPoint handle = ((CoordAxesStep)getStep(0)).getHandle();
    return new AnglePoint(handle.getX(), handle.getY(), n);
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
   * Implements findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step that is hit
   */
  public Interactive findInteractive(
      DrawingPanel panel, int xpix, int ypix) {
    if (!(panel instanceof TrackerPanel) ||
        !isVisible() ||
        !isEnabled()) return null;
    ImageCoordSystem coords = ((TrackerPanel)panel).getCoords();
    if (coords instanceof ReferenceFrame) return null;
    // only look at step 0 since getStep(n) returns 0 for every n
    Interactive ia = getStep(0).findInteractive(trackerPanel, xpix, ypix);
    if (ia == null) {
    	partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
    	hint = TrackerRes.getString("CoordAxes.Hint"); //$NON-NLS-1$
    	return null;
    }
    if (ia instanceof CoordAxesStep.Handle) {
    	partName = TrackerRes.getString("CoordAxes.Handle.Name"); //$NON-NLS-1$
    	hint = TrackerRes.getString("CoordAxes.Handle.Hint"); //$NON-NLS-1$
    }
    else {
    	partName = TrackerRes.getString("CoordAxes.Origin.Name"); //$NON-NLS-1$
    	hint = TrackerRes.getString("CoordAxes.Origin.Hint"); //$NON-NLS-1$
    }
    return ia;
  }

  /**
   * Overrides TTrack getMenu method.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    JMenu menu = super.getMenu(trackerPanel);
    menu.remove(deleteTrackItem);
    if (menu.getItemCount()>0)
    	menu.remove(menu.getItemCount()-1); // remove separator
    lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());
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
    int n = trackerPanel.getFrameNumber();
    ImageCoordSystem coords = trackerPanel.getCoords();
    originLabel.setText(TrackerRes.getString("CoordAxes.Origin.Label")); //$NON-NLS-1$
    xField.setToolTipText(TrackerRes.getString("CoordAxes.Origin.Field.Tooltip")); //$NON-NLS-1$
    yField.setToolTipText(TrackerRes.getString("CoordAxes.Origin.Field.Tooltip")); //$NON-NLS-1$
    gridCheckbox.setText(TrackerRes.getString("CoordAxes.Checkbox.Grid")); //$NON-NLS-1$
    gridCheckbox.setToolTipText(TrackerRes.getString("CoordAxes.Checkbox.Grid.Tooltip")); //$NON-NLS-1$
    gridButton.setToolTipText(TrackerRes.getString("CoordAxes.Button.Grid.Tooltip")); //$NON-NLS-1$
    list.add(gridSeparator);
    list.add(gridCheckbox);
    list.add(gridButton);
    list.add(magSeparator);
    list.add(originLabel);
    list.add(xSeparator);
    list.add(xLabel);
    list.add(xField);
    list.add(ySeparator);
    list.add(yLabel);
    list.add(yField);
    xField.setValue(coords.getOriginX(n));
    yField.setValue(coords.getOriginY(n));
        
    angleLabel.setText(TrackerRes.getString("CoordAxes.Label.Angle")); //$NON-NLS-1$
    list.add(stepSeparator);
    list.add(angleLabel);
    list.add(angleField);
    // put coords angle into angle field
    angleField.setValue(coords.getAngle(n));
    
    xField.setEnabled(!isLocked());
    yField.setEnabled(!isLocked());
    angleField.setEnabled(!isLocked());
    return list;
  }

  /**
   * Responds to property change events. This listens for the following
   * events: "stepnumber" & "image" from TrackerPanel.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
  	String name = e.getPropertyName();
    if (name.equals("stepnumber")) { //$NON-NLS-1$
	    int n = trackerPanel.getFrameNumber();
	    ImageCoordSystem coords = trackerPanel.getCoords();
	    angleField.setValue(coords.getAngle(n));
	    xField.setValue(coords.getOriginX(n));
	    yField.setValue(coords.getOriginY(n));
    }
    else super.propertyChange(e);
  }
  
  @Override
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	Object[] objectsToSize = new Object[]
  			{originLabel, gridCheckbox};
    FontSizer.setFonts(objectsToSize, level);
  }

  /**
   * Overrides Object toString method.
   *
   * @return the name of this track
   */
  public String toString() {
    return "Coordinate Axes"; //$NON-NLS-1$
  }
  
  /**
   * A TPoint used by autotracker to check for manually marked angles.
   */
  protected class AnglePoint extends TPoint {
  	int frameNum;
  	
  	public AnglePoint(double x, double y, int n) {
  		super(x, y);
  		frameNum = n;
  	}
  	
		public double getAngle() {
			TPoint origin = getOrigin();
  		return -origin.angle(this);
		}
  }

  /**
   * A TPoint used by autotracker to check for manually marked origins.
   */
  protected class OriginPoint extends TPoint {
  	
  	int frameNum;
  	
  	OriginPoint(int n) {
  		frameNum = n;
  	}
  	
		public double getX() {
	  	ImageCoordSystem coords = trackerPanel.getCoords();
			return coords.getOriginX(frameNum);
		}
		
		public double getY() {
	  	ImageCoordSystem coords = trackerPanel.getCoords();
			return coords.getOriginY(frameNum);
		}
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
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      CoordAxes axes = (CoordAxes)obj;
      if (axes.gridVisible) {
      	control.setValue("grid_visible", true); //$NON-NLS-1$
      }
      if (axes.grid.isCustom()) {
      	control.setValue("grid_alpha", axes.grid.getAlpha()); //$NON-NLS-1$
      	control.setValue("grid_RGB", axes.grid.getColor().getRGB()); //$NON-NLS-1$
      }
    }

    /**
     * Creates a new object.
     *
     * @param control the XMLControl with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new CoordAxes();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      CoordAxes axes = (CoordAxes)obj;
      axes.notyetShown = false;
      
      if (control.getPropertyNames().contains("grid_visible")) { //$NON-NLS-1$
      	axes.setGridVisible(control.getBoolean("grid_visible")); //$NON-NLS-1$
      }
      if (control.getPropertyNames().contains("grid_alpha")) { //$NON-NLS-1$
      	axes.grid.setAlpha(control.getInt("grid_alpha")); //$NON-NLS-1$
      }
      if (control.getPropertyNames().contains("grid_RGB")) { //$NON-NLS-1$
      	Color color = new Color(control.getInt("grid_RGB")); //$NON-NLS-1$
      	axes.grid.setColor(color);
      }
      return obj;
    }
  }

}

