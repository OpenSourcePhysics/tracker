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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;

/**
 * A Vector draws a series of VectorSteps that represent a generic
 * time-dependent vector.
 *
 * @author Douglas Brown
 */
public class Vector extends TTrack {

	@Override
	public String[] getFormatVariables() {
		return formatVariables;
	}
	
	@Override
	public Map<String, String[]> getFormatMap() {
		return formatMap;
	}

	@Override
	public Map<String, String> getFormatDescMap() {
		return formatDescriptionMap;
	}

	@Override
	public String getBaseType() {
		return "Vector";
	}
 
	@Override
	public String getVarDimsImpl(String variable) {
	  	String[] vars = dataVariables;
	  	String[] names = formatVariables;
	  		if (vars[1].equals(variable) || vars[2].equals(variable) || vars[3].equals(variable) 
	  				|| vars[5].equals(variable) || vars[6].equals(variable) 
	  				|| names[1].equals(variable)) { // BH was "2", but that is THETA; this is "xy"
	  			return "L"; //$NON-NLS-1$
	  		}  		
	  		if (vars[7].equals(variable) || vars[8].equals(variable)) { // step and frame
	  			return "I"; //$NON-NLS-1$
	  		}  		
		return null;
	}

	// static fields
	protected final static String[] dataVariables = new String[] {
	  	"t", //$NON-NLS-1$ 0
	  	"x", //$NON-NLS-1$ 1
	  	"y", //$NON-NLS-1$ 2
	  	"mag", //$NON-NLS-1$ 3
	  	Tracker.THETA, // 4
	  	"x_{tail}", //$NON-NLS-1$ 5
	  	"y_{tail}", //$NON-NLS-1$ 6
	  	"step", //$NON-NLS-1$ 7
	  	"frame", //$NON-NLS-1$ 8
	};
	protected final static String[] formatVariables = new String[] {
	  	"t", //$NON-NLS-1$ 0
	  	"xy", //$NON-NLS-1$ 1
	  	Tracker.THETA, // 4
	};
	protected final static String[] fieldVariables = new String[] {
		  	"t", //$NON-NLS-1$ 0
		  	"x", //$NON-NLS-1$ 1
		  	"y", //$NON-NLS-1$ 2
		  	"mag", //$NON-NLS-1$ 3
		  	Tracker.THETA, // 4
	};
	protected final static Map<String, String[]> formatMap;
	protected final static Map<String, String> formatDescriptionMap;

	static {

		// assemble format variables

		// assemble format map
		formatMap = new HashMap<>();
		formatMap.put("t", new String[] {"t"});
		formatMap.put("xy", new String[] {"x", "y",
			  	"mag", //$NON-NLS-1$ 3
			  	"x_{tail}", //$NON-NLS-1$ 5
			  	"y_{tail}", //$NON-NLS-1$ 6
		});
		formatMap.put(Tracker.THETA, new String[] {Tracker.THETA});

		// assemble format description map
		formatDescriptionMap = new HashMap<String, String>();
		formatDescriptionMap.put("t", TrackerRes.getString("Vector.Data.Description.0")); //$NON-NLS-1$
		formatDescriptionMap.put("xy", TrackerRes.getString("Vector.Description.Magnitudes")); //$NON-NLS-1$
		formatDescriptionMap.put(Tracker.THETA, TrackerRes.getString("Vector.Data.Description.4")); //$NON-NLS-1$
	}

	protected final static ArrayList<String> allVariables = createAllVariables(dataVariables, null); // no new field vars

	// instance fields
	protected JMenuItem tailsToOriginItem = new JMenuItem();
	protected JCheckBoxMenuItem labelsVisibleItem;
	protected static final Map<Integer, Boolean> visMap = new HashMap<Integer, Boolean>();

	/**
	 * Constructs a Vector.
	 */
	public Vector() {
		super(TYPE_VECTOR);
		defaultColors = new Color[] { Color.magenta, Color.cyan, Color.blue, Color.red };
		// set up footprint choices
		setFootprints(new Footprint[] { LineFootprint.getFootprint("Footprint.BoldArrow"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.Arrow"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.BigArrow") }); //$NON-NLS-1$
		defaultFootprint = getFootprint();
		setColor(defaultColors[0]);
		// turn on trail
		setTrailVisible(true);
		// turn on autoadvance
		setAutoAdvance(true);
		// assign a default name
		setName(TrackerRes.getString("Vector.New.Name")); //$NON-NLS-1$
		// assign default plot variables
		setProperty("xVarPlot0", dataVariables[0]); //$NON-NLS-1$
		setProperty("yVarPlot0", dataVariables[1]); //$NON-NLS-1$
		setProperty("xVarPlot1", dataVariables[0]); //$NON-NLS-1$
		setProperty("yVarPlot1", dataVariables[2]); //$NON-NLS-1$
		// set initial hint
		partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
		hint = TrackerRes.getString("Vector.Unmarked.Hint"); //$NON-NLS-1$
		// prepare toolbar components
		magLabel.setText("mag"); //$NON-NLS-1$
		// xy action
		Action xyAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setXYComponents();
				((NumberField) e.getSource()).requestFocusInWindow();
			}
		};
		// xy focus listener
		FocusListener xyFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setXYComponents();
			}
		};
		// magnitude angle action
		Action magAngleAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setMagnitudeAngle();
				((NumberField) e.getSource()).requestFocusInWindow();
			}
		};
		// magnitude angle focus listener
		FocusListener magAngleFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setMagnitudeAngle();
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
		// tails to origin item
		tailsToOriginItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// snap all vectors to the snapPoint
				TPoint p = tp.getSnapPoint();
				Step[] steps = Vector.this.getSteps();
				for (int i = 0; i < steps.length; i++) {
					if (steps[i] != null) {
						VectorStep v = (VectorStep) steps[i];
						if (v.chain != null)
							v.chain.clear();
						// detach any existing point
						v.attach(null);
						v.attach(p);
					}
				}
				tp.repaint();
			}
		});
		// labels visible item
		labelsVisibleItem = new JCheckBoxMenuItem(TrackerRes.getString("Vector.MenuItem.Label")); //$NON-NLS-1$
		labelsVisibleItem.setSelected(true);
		labelsVisibleItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Step[] steps = getSteps();
				for (int i = 0; i < steps.length; i++) {
					if (steps[i] != null) {
						VectorStep step = (VectorStep) steps[i];
						step.setLabelVisible(labelsVisibleItem.isSelected());
						step.erase();
					}
				}
				repaint();
			}
		});
	}

	/**
	 * Implements createStep. When creating a vector the coordinates define both the
	 * tail and tip position, but when re-marking an existing vector they define the
	 * tip position only.
	 *
	 * @param n the frame number
	 * @param x the x coordinate in image space
	 * @param y the y coordinate in image space
	 * @return the new step
	 */
	@Override
	public Step createStep(int n, double x, double y) {
		VectorStep step = (VectorStep) getStep(n);
		if (step == null)
			return createStep(n, x, y, 0, 0);
		XMLControl state = new XMLControlElement(step);
		step.tip.setXY(x, y);
		Undo.postStepEdit(step, state);
		return step;
	}

	/**
	 * Creates a vector step with specified tail position and vector components.
	 *
	 * @param n  the frame number
	 * @param x  the tail x coordinate in image space
	 * @param y  the tail y coordinate in image space
	 * @param xc the x component in image space
	 * @param yc the y component in image space
	 * @return the new step
	 */
	public Step createStep(int n, double x, double y, double xc, double yc) {
		if (locked)
			return null;
		VectorStep step = (VectorStep) getStep(n);
		step = new VectorStep(this, n, x, y, xc, yc, Step.TYPE_UNKNOWN);
		step.setFirePropertyChangeEvents(true);
		steps.setStep(n, step);
		step.setFootprint(getFootprint());
		firePropertyChange(PROPERTY_TTRACK_STEP, HINT_STEP_ADDED_OR_REMOVED, new Integer(n)); // $NON-NLS-1$
		return step;
	}

	/**
	 * Gets the length of the steps created by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getStepLength() {
		return VectorStep.getLength();
	}

	/**
	 * Gets the length of the footprints required by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getFootprintLength() {
		return 2;
	}

	/**
	 * Overrides TTrack draw method.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		if (panel instanceof TrackerPanel) {
			if (!isVectorsVisible((TrackerPanel) panel))
				return;
//      // snap after loading 
//      boolean changed = trackerPanel.changed;
//      if (!snapVectors.isEmpty() && panel.getClass().equals(TrackerPanel.class)) {
//      	Iterator it = snapVectors.iterator();
//      	while (it.hasNext()) {
//      		VectorStep step = (VectorStep)it.next();
//      		step.snap(trackerPanel);
//      	}
//      	snapVectors.clear();
//      	trackerPanel.changed = changed;
//      }
			super.draw(panel, _g);
		}
	}

	/**
	 * Overrides TTrack setLocked method.
	 *
	 * @param locked <code>true</code> to lock this
	 */
	@Override
	public void setLocked(boolean locked) {
		super.setLocked(locked);
		Step[] steps = getSteps();
		for (int i = 0; i < steps.length; i++) {
			VectorStep step = (VectorStep) steps[i];
			if (step != null)
				step.setTipEnabled(!isLocked());
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
		case Trackable.PROPERTY_ADJUSTING:
//			if (e.getSource() instanceof TrackerPanel) {
				refreshDataLater = (Boolean) e.getNewValue();
				if (!refreshDataLater) { // stopped adjusting
					firePropertyChange(PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
				}
//			}
			break;
		}
		super.propertyChange(e);
	}

	@Override
	protected void setMarking(boolean marking) {
		super.setMarking(marking);
		repaint(tp);
	}

	/**
	 * Refreshes the data.
	 *
	 * @param data         the DatasetManager
	 * @param trackerPanel the tracker panel
	 */
	@Override
	protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
		if (refreshDataLater || trackerPanel == null || data == null)
			return;
		dataFrames.clear();
		VideoPlayer player = trackerPanel.getPlayer();
		VideoClip clip = player.getVideoClip();
		ImageCoordSystem coords = trackerPanel.getCoords();
		// define the datasets
//		Dataset xComp = data.getDataset(count++);
//		Dataset yComp = data.getDataset(count++);
//		Dataset mag = data.getDataset(count++);
//		Dataset ang = data.getDataset(count++);
//		Dataset xTail = data.getDataset(count++);
//		Dataset yTail = data.getDataset(count++);
//		Dataset stepNum = data.getDataset(count++);
//		Dataset frameNum = data.getDataset(count++);
		int count = 8;
		// get data at each non-null step included in the videoclip
		Step[] stepArray = getSteps();
		int len = stepArray.length;
		double[][] validData = new double[count + 1][len];
		int pt = 0;
		for (int i = 0; i < len; i++) {
			// get the frame number of the step
			// check that the frame is included in the clip
			// check that the time > 0
			int frame, stepNumber;
			VectorStep step = (VectorStep) stepArray[i];
			if (step == null
					|| !clip.includesFrame(frame = step.getFrameNumber())
					|| player.getStepTime(stepNumber = clip.frameToStep(frame)) < 0
					)
				continue;
			double t = player.getStepTime(stepNumber) / 1000.0;
			if (t < 0)
				continue; // indicates the time is unknown
			// get the x and y component data
			double xcomp = step.getXComponent();
			double ycomp = step.getYComponent();
			double wxc = coords.imageToWorldXComponent(frame, xcomp, ycomp);
			double wyc = coords.imageToWorldYComponent(frame, xcomp, ycomp);
			Point2D tailPosition = step.getTail().getWorldPosition(trackerPanel);
			// append the data to the data sets
			validData[0][pt] = wxc; // x
			validData[1][pt] = wyc; // y
			validData[2][pt] = Math.sqrt(wxc * wxc + wyc * wyc); // mag
			validData[3][pt] = Math.atan2(wyc, wxc); // ang
			validData[4][pt] = tailPosition.getX(); // xTail
			validData[5][pt] = tailPosition.getY(); // yTail
			validData[6][pt] = stepNumber; // step
			validData[7][pt] = frame; // frame
			validData[8][pt] = t;
			dataFrames.add(frame);
			pt++;
		}
		clearColumns(data, count, dataVariables, "Vector.Data.Description.", validData, pt);
	}

	/**
	 * Finds the interactive drawable object located at the specified pixel
	 * position.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position on the panel
	 * @param ypix  the y pixel position on the panel
	 * @return the first step TPoint that is hit
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		Interactive ia = super.findInteractive(panel, xpix, ypix);
		if (ia == null) {
			TPoint p = tp.getSelectedPoint();
			if (p != null) {
				if (p instanceof VectorStep.Handle) {
					partName = TrackerRes.getString("Vector.Handle.Name"); //$NON-NLS-1$
					partName += " " + TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
					hint = TrackerRes.getString("Vector.HandleSelected.Hint"); //$NON-NLS-1$
				} else {
					partName = TrackerRes.getString("Vector.Tip.Name"); //$NON-NLS-1$
					partName += " " + TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
					hint = TrackerRes.getString("Vector.TipSelected.Hint"); //$NON-NLS-1$
				}
			} else {
				partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
				if (getStep(tp.getFrameNumber()) == null)
					hint = TrackerRes.getString("Vector.Unmarked.Hint"); //$NON-NLS-1$
				else {
					hint = TrackerRes.getString("Vector.Remark.Hint"); //$NON-NLS-1$
				}
			}
			return null;
		}
		if (ia instanceof VectorStep.Handle) {
			VectorStep.Handle handle = (VectorStep.Handle) ia;
			partName = TrackerRes.getString("Vector.Handle.Name"); //$NON-NLS-1$
			hint = handle.isShort() ? TrackerRes.getString("Vector.ShortHandle.Hint") : //$NON-NLS-1$
					TrackerRes.getString("Vector.Handle.Hint"); //$NON-NLS-1$
		} else {
			partName = TrackerRes.getString("Vector.Tip.Name"); //$NON-NLS-1$
			hint = TrackerRes.getString("Vector.Tip.Hint"); //$NON-NLS-1$
		}
		return ia;
	}

	/**
	 * Sets the visibility of force vector labels.
	 *
	 * @param visible <code>true</code> to show all labels
	 */
	public void setLabelsVisible(boolean visible) {
		Step[] steps = this.getSteps();
		for (int i = 0; i < steps.length; i++) {
			VectorStep step = (VectorStep) steps[i];
			if (step != null) {
				step.setLabelVisible(visible);
				step.setRolloverVisible(!visible);
			}
		}
	}

	/**
	 * Gets the labels visibility.
	 *
	 * @return <code>true</code> if labels are visible
	 */
	public boolean isLabelsVisible() {
		Step[] steps = this.getSteps();
		for (int i = 0; i < steps.length; i++) {
			VectorStep step = (VectorStep) steps[i];
			if (step != null)
				return step.isLabelVisible();
		}
		return false;
	}

	/**
	 * Sets the visibility of the vectors on the specified tracker panel.
	 *
	 * @param panel   the tracker panel
	 * @param visible <code>true</code> to show vectors
	 */
	public void setVectorsVisible(TrackerPanel panel, boolean visible) {
		if (visible == isVectorsVisible(panel))
			return;
		visMap.put(panel.getID(), Boolean.valueOf(visible));
		if (!visible) {
			Step step = panel.getSelectedStep();
			if (step != null && step == getStep(step.getFrameNumber())) {
				panel.setSelectedPoint(null);
				panel.selectedSteps.clear();
			}
		}
	}

	/**
	 * Gets whether the vectors are visible on the specified panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return <code>true</code> if positions are visible
	 */
	public boolean isVectorsVisible(TrackerPanel trackerPanel) {
		trackerPanel = trackerPanel.getMainPanel();
		Boolean vis = visMap.get(trackerPanel.getID());
		if (vis == null) {
			vis = Boolean.valueOf(true); // vectors are visible by default
			visMap.put(trackerPanel.getID(), vis);
		}
		return vis.booleanValue();
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

		// remove delete item from end
		if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
			removeDeleteTrackItem(menu); // remove separator
		}
		// add autoAdvance and markByDefault items at end
		if (trackerPanel.isEnabled("track.autoAdvance") || //$NON-NLS-1$
				trackerPanel.isEnabled("track.markByDefault")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
			if (trackerPanel.isEnabled("track.autoAdvance")) //$NON-NLS-1$
				menu.add(autoAdvanceItem);
			if (trackerPanel.isEnabled("track.markByDefault")) //$NON-NLS-1$
				menu.add(markByDefaultItem);
		}
		// add tailsToOrigin item
		TMenuBar.checkAddMenuSep(menu);
		tailsToOriginItem.setText(TrackerRes.getString("Vector.MenuItem.ToOrigin")); //$NON-NLS-1$
		menu.add(tailsToOriginItem);
		// replace delete item
		if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
			TPoint p = trackerPanel.getSelectedPoint();
			Step step = getStep(p, trackerPanel);
			deleteStepItem.setEnabled(step != null);
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
	 * @return the DataSetManager
	 */
	@Override
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
		return list;
	}

	/**
	 * Overrides TTrack getToolbarPointComponents method.
	 *
	 * @param trackerPanel the tracker panel
	 * @param point        the TPoint
	 * @return a list of components
	 */
	@Override
	public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel, TPoint point) {
		xLabel.setText(dataVariables[1]);
		yLabel.setText(dataVariables[2]);
		magLabel.setText(dataVariables[3]);
		angleLabel.setText(dataVariables[4]);
		xField.setUnits(trackerPanel.getUnits(this, dataVariables[1]));
		yField.setUnits(trackerPanel.getUnits(this, dataVariables[2]));
		magField.setUnits(trackerPanel.getUnits(this, dataVariables[3]));

		ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
		list.add(stepSeparator);
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
		xField.setEnabled(!isLocked());
		yField.setEnabled(!isLocked());
		magField.setEnabled(!isLocked());
		angleField.setEnabled(!isLocked());
		return list;
	}

	/**
	 * Overrides Object toString method.
	 *
	 * @return a description of this object
	 */
	@Override
	public String toString() {
		return TrackerRes.getString("Vector.Name") + " \"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public Map<String, NumberField[]> getNumberFields() {
		if (numberFields.isEmpty()) {
			numberFields.put(dataVariables[0], new NumberField[] { tField });
			numberFields.put(dataVariables[1], new NumberField[] { xField });
			numberFields.put(dataVariables[2], new NumberField[] { yField });
			numberFields.put(dataVariables[3], new NumberField[] { magField });
			numberFields.put(dataVariables[4], new NumberField[] { angleField });
		}
		return numberFields;
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
		 * @param obj     the object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			Vector vec = (Vector) obj;
			// save track data
			XML.getLoader(TTrack.class).saveObject(control, obj);
			Step[] steps = vec.getSteps();
			FrameData[] data = new FrameData[steps.length];
			for (int n = 0; n < steps.length; n++) {
				if (steps[n] == null)
					continue;
				VectorStep v = (VectorStep) steps[n];
				data[n] = new FrameData(v, vec.isDependent());
			}
			control.setValue("framedata", data); //$NON-NLS-1$
		}

		/**
		 * Creates a new object with data from an XMLControl.
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return new Vector();
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
			Vector vec = (Vector) obj;
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			// load step data
			FrameData[] data = (FrameData[]) control.getObject("framedata"); //$NON-NLS-1$
			if (data != null) {
				boolean locked = vec.isLocked();
				vec.setLocked(false);
				for (int n = 0; n < data.length; n++) {
					if (data[n] == null) {
						vec.steps.setStep(n, null);
						continue;
					}
					vec.createStep(n, data[n].x, data[n].y, data[n].xc, data[n].yc);
				}
				vec.setLocked(locked);
			}
			return obj;
		}
	}

//__________________________ private methods ___________________________

	/**
	 * Sets the components of the currently selected vector based on the values in
	 * the x and y fields.
	 */
	private void setXYComponents() {
		TPoint p = tp.getSelectedPoint();
		VectorStep step = (VectorStep) getStep(p, tp);
		if (step != null) {
			ImageCoordSystem coords = tp.getCoords();
			int n = tp.getFrameNumber();
			double x = coords.worldToImageXComponent(n, xField.getValue(), yField.getValue());
			double y = coords.worldToImageYComponent(n, xField.getValue(), yField.getValue());
			step.setXYComponents(x, y);
			x = coords.imageToWorldXComponent(n, step.getXComponent(), step.getYComponent());
			y = coords.imageToWorldYComponent(n, step.getXComponent(), step.getYComponent());
			xField.setValue(x);
			yField.setValue(y);
			magField.setValue(Math.sqrt(x * x + y * y));
			double theta = Math.atan2(y, x);
			angleField.setValue(theta);
			p.showCoordinates(tp);
		}
	}

	/**
	 * Sets the components of the currently selected vector based on the values in
	 * the mag and angle fields.
	 */
	private void setMagnitudeAngle() {
		double theta = angleField.getValue();
		double xval = magField.getValue() * Math.cos(theta);
		double yval = magField.getValue() * Math.sin(theta);
		TPoint p = tp.getSelectedPoint();
		VectorStep step = (VectorStep) getStep(p, tp);
		if (step != null) {
			ImageCoordSystem coords = tp.getCoords();
			int n = tp.getFrameNumber();
			double x = coords.worldToImageXComponent(n, xval, yval);
			double y = coords.worldToImageYComponent(n, xval, yval);
			step.setXYComponents(x, y);
			x = coords.imageToWorldXComponent(n, step.getXComponent(), step.getYComponent());
			y = coords.imageToWorldYComponent(n, step.getXComponent(), step.getYComponent());
			xField.setValue(x);
			yField.setValue(y);
			magField.setValue(Math.sqrt(x * x + y * y));
			theta = Math.atan2(y, x);
			angleField.setValue(theta);
			p.showCoordinates(tp);
		}
	}

	/**
	 * Inner class containing the vector data for a single frame number.
	 */
	public static class FrameData {
		double x, y, xc, yc;
		boolean independent;

		FrameData() {
			/** empty block */
		}

		FrameData(VectorStep v, boolean dependent) {
			x = v.getTail().getX();
			y = v.getTail().getY();
			xc = v.getXComponent();
			yc = v.getYComponent();
			independent = !dependent;
		}
	}

	/**
	 * A class to save and load a FrameData.
	 */
	private static class FrameDataLoader implements XML.ObjectLoader {

		@Override
		public void saveObject(XMLControl control, Object obj) {
			FrameData data = (FrameData) obj;
			control.setValue("xtail", data.x); //$NON-NLS-1$
			control.setValue("ytail", data.y); //$NON-NLS-1$
			if (data.independent) {
				control.setValue("xcomponent", data.xc); //$NON-NLS-1$
				control.setValue("ycomponent", data.yc); //$NON-NLS-1$
			}
		}

		@Override
		public Object createObject(XMLControl control) {
			return new FrameData();
		}

		@Override
		public Object loadObject(XMLControl control, Object obj) {
			FrameData data = (FrameData) obj;
			double x = control.getDouble("xcomponent"); //$NON-NLS-1$
			if (!Double.isNaN(x)) {
				data.xc = x;
				data.yc = control.getDouble("ycomponent"); //$NON-NLS-1$
			}
			data.x = control.getDouble("xtail"); //$NON-NLS-1$
			data.y = control.getDouble("ytail"); //$NON-NLS-1$
			return obj;
		}
	}
	
}
