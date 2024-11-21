/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

/**
 * An OffsetOrigin controls the origin of an image coordinate system.
 *
 * @author Douglas Brown
 */
public class OffsetOrigin extends TTrack implements MarkingRequired {

	@Override
	public Map<String, String[]> getFormatMap() {
		return formatMap;
	}

	@Override
	public String[] getFormatVariables() {
		return formatVariables;
	}

	@Override
	public Map<String, String> getFormatDescMap() {
		return formatDescriptionMap;
	}

	@Override
	public String getBaseType() {
		return "OffsetOrigin";
	}

	@Override
	public String getVarDimsImpl(String variable) {
		return "L"; //$NON-NLS-1$
	}

	// static fields
	protected final static String[] dataVariables;
	protected final static String[] formatVariables; // used by NumberFormatSetter
	protected final static Map<String, String[]> formatMap;
	protected final static Map<String, String> formatDescriptionMap;

	static {
		dataVariables = new String[] { "x", "y" }; //$NON-NLS-1$ //$NON-NLS-2$
		formatVariables = new String[] { "xy" }; //$NON-NLS-1$

		// assemble format map
		formatMap = new HashMap<>();
		formatMap.put("xy", dataVariables);

		// assemble format description map
		formatDescriptionMap = new HashMap<String, String>();
		formatDescriptionMap.put(formatVariables[0], TrackerRes.getString("PointMass.Position.Name")); //$NON-NLS-1$

	}
	protected final static ArrayList<String> allVariables = createAllVariables(dataVariables, null);

	// instance fields
	private Component separator;
	protected boolean fixedCoordinates = true;
	protected JCheckBoxMenuItem fixedCoordinatesItem;
	protected JLabel unmarkedLabel;

	/**
	 * Constructs an OffsetOrigin.
	 */
	public OffsetOrigin() {
		super(TYPE_OFFSETORIGIN);
		defaultColors = new Color[] { Color.cyan, Color.magenta, Color.yellow.darker() };
		// set up footprint choices and color
		setFootprints(new Footprint[] { PointShapeFootprint.getFootprint("Footprint.BoldCrosshair"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.Crosshair") }); //$NON-NLS-1$
		defaultFootprint = getFootprint();
		// assign a default name
		setName(TrackerRes.getString("OffsetOrigin.New.Name")); //$NON-NLS-1$
		setColor(defaultColors[0]);
		// hide this track from views
		viewable = false;
		// set initial hint
		partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
		hint = TrackerRes.getString("OffsetOrigin.Unmarked.Hint"); //$NON-NLS-1$
		keyFrames.add(0);
		createGUI();
	}

	/**
	 * Creates a new offset origin step with specified image coordinates.
	 *
	 * @param n the frame number
	 * @param x the x coordinate in image space
	 * @param y the y coordinate in image space
	 * @return the new step
	 */
	@Override
	public Step createStep(int n, double x, double y) {
		if (isLocked())
			return null;
		OffsetOriginStep step = (OffsetOriginStep) getStep(n);
		if (step == null) {
			step = new OffsetOriginStep(this, n, x, y);
			step.setFootprint(getFootprint());
			steps = new StepArray(step);
			firePropertyChange(PROPERTY_TTRACK_STEP, null, new Integer(n)); // $NON-NLS-1$
		} else if (tp != null) {
			XMLControl currentState = new XMLControlElement(this);
			TPoint p = step.getPosition();
			p.setLocation(x, y);
			Point2D pt = p.getWorldPosition(tp);
			step.worldX = pt.getX();
			step.worldY = pt.getY();
			keyFrames.add(n);
			Undo.postTrackEdit(this, currentState);
		}
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
		OffsetOriginStep step = (OffsetOriginStep) getStep(n);
		// be sure coords have unfixed origin
		ImageCoordSystem coords = tp.getCoords();
		coords.setFixedOrigin(false);
		if (step == null) {
			step = (OffsetOriginStep) createStep(n, x, y);
			if (step != null) {
				return step.getPoints()[0];
			}
		} else {
			TPoint p = step.getPoints()[0];
			if (p != null) {
				Mark mark = step.panelMarks.get(tp.getID());
				if (mark == null) {
					// set step location to image position of current world coordinates
					double xx = coords.worldToImageX(n, step.worldX, step.worldY);
					double yy = coords.worldToImageY(n, step.worldX, step.worldY);
					p.setLocation(xx, yy);
				}
				p.setAdjusting(true, null);
				p.setXY(x, y);
				p.setAdjusting(false, null);
				firePropertyChange(PROPERTY_TTRACK_STEP, null, n); // $NON-NLS-1$
				return p;
			}
		}
		return null;
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

	/**
	 * Determines if the world coordinates are fixed.
	 *
	 * @return <code>true</code> if fixed
	 */
	public boolean isFixedCoordinates() {
		return fixedCoordinates;
	}

	/**
	 * Sets the fixed coordinates property. When fixed, the world coordinates are
	 * the same at all times.
	 *
	 * @param fixed <code>true</code> to fix the coordinates
	 */
	public void setFixedCoordinates(boolean fixed) {
		if (fixedCoordinates == fixed)
			return;
		XMLControl control = new XMLControlElement(this);
		if (tp != null) {
			tp.changed = true;
			int n = tp.getFrameNumber();
			steps = new StepArray(getStep(n));
			TFrame.repaintT(tp);
		}
		if (fixed) {
			keyFrames.clear();
			keyFrames.add(0);
		}
		fixedCoordinates = fixed;
		Undo.postTrackEdit(this, control);
	}

	/**
	 * Overrides TTrack getStep method. This refreshes the step before returning it
	 * since its position and/or world coordinates may change due to external
	 * factors.
	 *
	 * @param n the frame number
	 * @return the step
	 */
	@Override
	public Step getStep(int n) {
		OffsetOriginStep step = (OffsetOriginStep) steps.getStep(n);
		refreshStep(step);
		return step;
	}

	/**
	 * Overrides TTrack isLocked method. Returns true if this is locked or if the
	 * coordinate system is locked.
	 *
	 * @return <code>true</code> if this is locked
	 */
	@Override
	public boolean isLocked() {
		boolean locked = super.isLocked();
		if (tp != null) {
			locked = locked || tp.getCoords().isLocked();
		}
		return locked;
	}

	/**
	 * Overrides TTrack setTrailVisible method. Offset origin trails are never
	 * visible.
	 *
	 * @param visible ignored
	 */
	@Override
	public void setTrailVisible(boolean visible) {
		/** empty block */
	}

	/**
	 * Gets the length of the steps created by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getStepLength() {
		return OffsetOriginStep.getLength();
	}

	/**
	 * Gets the length of the footprints required by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getFootprintLength() {
		return 1;
	}

	/**
	 * Overrides TTrack findInteractive method.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position on the panel
	 * @param ypix  the y pixel position on the panel
	 * @return the current step or null
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		if (!(panel instanceof TrackerPanel) || !isVisible() || !isEnabled())
			return null;
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		Interactive ia = null;
		Step step = getStep(trackerPanel.getFrameNumber());
		if (step != null)
			ia = step.findInteractive(trackerPanel, xpix, ypix);
		if (ia != null) {
			partName = TrackerRes.getString("OffsetOrigin.Position.Name"); //$NON-NLS-1$
			hint = TrackerRes.getString("OffsetOrigin.Position.Hint"); //$NON-NLS-1$
		} else {
			partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
			hint = TrackerRes.getString("OffsetOrigin.Unmarked.Hint"); //$NON-NLS-1$
		}
		return ia;
	}

	/**
	 * Overrides TTrack getMenu method.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	@Override
	public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu0) {
		JMenu menu = super.getMenu(trackerPanel, menu0);
		if (menu0 == null)
			return menu;

		lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());
		// remove end items and last separator
		removeDeleteTrackItem(menu); 
		// add fixed and delete items
		fixedCoordinatesItem.setText(TrackerRes.getString("OffsetOrigin.MenuItem.Fixed")); //$NON-NLS-1$
		fixedCoordinatesItem.setSelected(isFixedCoordinates());
		menu.add(fixedCoordinatesItem);
		menu.addSeparator();
		menu.add(deleteTrackItem);
		return menu;
	}

	/**
	 * Overrides TTrack method.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a list of components
	 */
	@Override
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
		int n = trackerPanel.getFrameNumber();
		Step step = getStep(n);

		list.add(stepSeparator);
		if (step == null) {
			unmarkedLabel.setText(TrackerRes.getString("TTrack.Label.Unmarked")); //$NON-NLS-1$
			list.add(unmarkedLabel);
		} else {
			// put step number into label
			stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
			VideoClip clip = trackerPanel.getPlayer().getVideoClip();
			n = clip.frameToStep(n);
			stepValueLabel.setText(n + ":"); //$NON-NLS-1$

			list.add(stepLabel);
			list.add(stepValueLabel);
			list.add(tSeparator);
			xLabel.setText(dataVariables[0]);
			yLabel.setText(dataVariables[1]);
			xField.setUnits(trackerPanel.getUnits(this, dataVariables[0]));
			yField.setUnits(trackerPanel.getUnits(this, dataVariables[1]));
			boolean locked = trackerPanel.getCoords().isLocked() || super.isLocked();
			xField.setEnabled(!locked);
			yField.setEnabled(!locked);
			displayWorldCoordinates();
			list.add(xLabel);
			list.add(xField);
			list.add(separator);
			list.add(yLabel);
			list.add(yField);
		}

		return list;
	}

	/**
	 * Adds events for TrackerPanel.
	 * 
	 * @param panel the new TrackerPanel
	 */
	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null) {			
			tp.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this);
		}
		super.setTrackerPanel(panel);
		if (tp != null) {
			tp.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this);
		}
	}

	/**
	 * Responds to property change events. Overrides TTrack method.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
			if (tp.getSelectedTrack() == this) {
				displayWorldCoordinates();
				stepValueLabel.setText(e.getNewValue() + ":"); //$NON-NLS-1$
			}
			return;
		case TTrack.PROPERTY_TTRACK_LOCKED:
			xField.setEnabled(!isLocked());
			yField.setEnabled(!isLocked());
			return;
		}
		super.propertyChange(e);
	}
	

	/**
	 * Overrides TTrack method.
	 *
	 * @param locked <code>true</code> to lock this
	 */
	@Override
	public void setLocked(boolean locked) {
		super.setLocked(locked);
		xField.setEnabled(!isLocked());
		yField.setEnabled(!isLocked());
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	@Override
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		Object[] objectsToSize = new Object[] { unmarkedLabel, fixedCoordinatesItem };
		FontSizer.setFonts(objectsToSize, level);
	}

	@Override
	public boolean isMarkByDefault() {
		return requiresMarking() || super.isMarkByDefault();
	}

	/**
	 * Implements MarkingRequired interface.
	 */
	@Override
	public boolean requiresMarking() {
		return getStep(0) == null;		
	}

	/**
	 * Overrides Object toString method.
	 *
	 * @return the name of this track
	 */
	@Override
	public String toString() {
		return TrackerRes.getString("OffsetOrigin.Name"); //$NON-NLS-1$
	}

	@Override
	public Map<String, NumberField[]> getNumberFields() {
		numberFields.clear();
		numberFields.put(dataVariables[0], new NumberField[] { xField });
		numberFields.put(dataVariables[1], new NumberField[] { yField });
		return numberFields;
	}

	/**
	 * Returns a description of the point at a given index. Used by AutoTracker.
	 *
	 * @param pointIndex the points[] index
	 * @return the description
	 */
	@Override
	protected String getTargetDescription(int pointIndex) {
		return TrackerRes.getString("OffsetOrigin.Position.Name"); //$NON-NLS-1$
	}

	/**
	 * Refreshes a step by setting it equal to the previous keyframe step.
	 *
	 * @param step the step to refresh
	 */
	protected void refreshStep(OffsetOriginStep step) {
		if (step == null)
			return;
		int key = 0;
		for (int i : keyFrames) {
			if (i <= step.n)
				key = i;
		}
		// compare step with keyStep
		OffsetOriginStep keyStep = (OffsetOriginStep) steps.getStep(key);
		boolean different = keyStep.worldX != step.worldX || keyStep.worldY != step.worldY;
		// update step if needed
		if (different) {
			step.worldX = keyStep.worldX;
			step.worldY = keyStep.worldY;
		}
		step.erase();
	}

	/**
	 * Sets the world values of the currently selected point based on the values in
	 * the x and y fields.
	 */
	private void setWorldCoordinatesFromFields() {
		if (tp == null)
			return;
		OffsetOriginStep step = (OffsetOriginStep) getStep(tp.getFrameNumber());
		boolean different = step.worldX != xField.getValue() || step.worldY != yField.getValue();
		if (different) {
			XMLControl trackControl = new XMLControlElement(this);
			XMLControl coordsControl = new XMLControlElement(tp.getCoords());
			step.setWorldXY(xField.getValue(), yField.getValue());
			step.getPosition().showCoordinates(tp);
			Undo.postTrackAndCoordsEdit(this, trackControl, coordsControl);
		}
	}

	/**
	 * Displays the world coordinates of the currently selected step.
	 */
	private void displayWorldCoordinates() {
		int n = tp == null ? 0 : tp.getFrameNumber();
		OffsetOriginStep step = (OffsetOriginStep) getStep(n);
		if (step == null) {
			xField.setText(null);
			yField.setText(null);
		} else {
			xField.setValue(step.worldX);
			yField.setValue(step.worldY);
		}
	}

	/**
	 * Creates the GUI.
	 */
	private void createGUI() {
		unmarkedLabel = new JLabel();
		unmarkedLabel.setForeground(Color.red.darker());
		fixedCoordinatesItem = new JCheckBoxMenuItem(TrackerRes.getString("OffsetOrigin.MenuItem.Fixed")); //$NON-NLS-1$
		fixedCoordinatesItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setFixedCoordinates(fixedCoordinatesItem.isSelected());
			}
		});
		// create xy ActionListener and FocusListener
		ActionListener xyAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setWorldCoordinatesFromFields();
				((NumberField) e.getSource()).requestFocusInWindow();
			}
		};
		FocusListener xyFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setWorldCoordinatesFromFields();
			}
		};
		xField.addActionListener(xyAction);
		xField.addFocusListener(xyFocusListener);
		yField.addActionListener(xyAction);
		yField.addFocusListener(xyFocusListener);
		separator = Box.createRigidArea(new Dimension(4, 4));
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
		 * @param obj     the object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			OffsetOrigin offset = (OffsetOrigin) obj;
			// save track data
			XML.getLoader(TTrack.class).saveObject(control, obj);
			// save fixed coordinates
			control.setValue("fixed_coordinates", offset.isFixedCoordinates()); //$NON-NLS-1$
			// save world coordinates
			if (!offset.steps.isEmpty()) {
				Step[] steps = offset.getSteps();
				double[][] stepData = new double[steps.length][];
				for (int i = 0; i < steps.length; i++) {
					// save only key frames
					if (steps[i] == null || !offset.keyFrames.contains(i))
						continue;
					OffsetOriginStep step = (OffsetOriginStep) steps[i];
					stepData[i] = new double[] { step.worldX, step.worldY };
					if (!control.getPropertyNamesRaw().contains("worldX")) { //$NON-NLS-1$
						// include these for backward compatibility
						control.setValue("worldX", step.worldX); //$NON-NLS-1$
						control.setValue("worldY", step.worldY); //$NON-NLS-1$
					}
				}
				control.setValue("world_coordinates", stepData); //$NON-NLS-1$
			}
		}

		/**
		 * Creates a new object.
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return new OffsetOrigin();
		}

		/**
		 * Loads an object with data from an XMLControl.
		 *
		 * @param control the control
		 * @param obj     the object
		 * @return the loaded object
		 */
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			OffsetOrigin offset = (OffsetOrigin) obj;
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			boolean locked = offset.isLocked();
			offset.setLocked(false);
			// load fixed coordinates
			if (control.getPropertyNamesRaw().contains("fixed_coordinates")) //$NON-NLS-1$
				offset.fixedCoordinates = control.getBoolean("fixed_coordinates"); //$NON-NLS-1$
			offset.keyFrames.clear();
			// create step array if needed
			if (offset.steps.isEmpty())
				offset.createStep(0, 0, 0);
			offset.keyFrames.clear();
			// load world coordinates
			double[][] stepData = (double[][]) control.getObject("world_coordinates"); //$NON-NLS-1$
			if (stepData != null) {
				for (int i = 0; i < stepData.length; i++) {
					if (stepData[i] != null) {
						OffsetOriginStep step = (OffsetOriginStep) offset.getStep(i);
						step.worldX = stepData[i][0];
						step.worldY = stepData[i][1];
						offset.keyFrames.add(i);
					}
				}
			} else { // load legacy files
				OffsetOriginStep step = (OffsetOriginStep) offset.getStep(0);
				step.worldX = control.getDouble("worldX"); //$NON-NLS-1$
				step.worldY = control.getDouble("worldY"); //$NON-NLS-1$
				offset.keyFrames.add(0);
			}
			offset.setLocked(locked);
			offset.displayWorldCoordinates();
			return obj;
		}
	}
}
