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

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Trackable;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;

/**
 * A TapeMeasure measures and displays its world length and its angle relative
 * to the positive x-axis. It is used to set the scale and angle of an
 * ImageCoordSystem.
 *
 * @author Douglas Brown
 */
public class TapeMeasure extends InputTrack {

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
		return "TapeMeasure";
	}

	@Override
	public String getVarDimsImpl(String variable) {
		String[] vars = dataVariables;
		String[] names = formatVariables;
		if (names[1].equals(variable)
		// same || vars[1].equals(variable)
		) {
			return "L"; //$NON-NLS-1$
		}
		if (vars[3].equals(variable) || vars[4].equals(variable)) {
			return "I"; //$NON-NLS-1$
		}
		return null;
	}

	// static constants
	protected static final double MIN_LENGTH = 1.0E-30;
	@SuppressWarnings("javadoc")
	public static final float[] BROKEN_LINE = new float[] { 10, 1 };

	protected static final String[] dataVariables;
	protected static final String[] formatVariables; // also used for fieldVariables
	protected static final Map<String, String[]> formatMap;
	protected static final Map<String, String> formatDescriptionMap;

	static {
		dataVariables = new String[] { "t", "L", Tracker.THETA, "step", "frame" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		formatVariables = new String[] { "t", "L", Tracker.THETA }; //$NON-NLS-1$ //$NON-NLS-2$

		// assemble format map
		formatMap = new HashMap<>();
		formatMap.put("t", new String[] { "t" });
		formatMap.put("L", new String[] { "L" });
		formatMap.put(Tracker.THETA, new String[] { Tracker.THETA });

		// assemble format description map
		formatDescriptionMap = new HashMap<String, String>();
		formatDescriptionMap.put(formatVariables[0], TrackerRes.getString("PointMass.Data.Description.0")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[1], TrackerRes.getString("TapeMeasure.Label.Length")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[2], TrackerRes.getString("TapeMeasure.Label.TapeAngle")); //$NON-NLS-1$
	}

	protected final static ArrayList<String> allVariables = createAllVariables(dataVariables, null); // no field vars

	// instance fields
	protected boolean fixedLength = true;
	protected boolean readOnly;
	protected boolean stickMode;
	protected boolean isStepChangingScale;
	protected boolean notYetShown = true;
	protected boolean isIncomplete, isCalibrator;
	protected JLabel end1Label, end2Label, lengthLabel;
	protected Footprint[] tapeFootprints, stickFootprints;
	protected TreeSet<Integer> lengthKeyFrames = new TreeSet<Integer>(); // applies to sticks only
	protected JMenuItem attachmentItem;
//	protected JCheckBoxMenuItem fixedLengthItem;
	protected Double calibrationLength;

	/**
	 * Constructs a TapeMeasure.
	 */
	public TapeMeasure() {
		super(TYPE_TAPEMEASURE);
		// assign a default name
		setName(TrackerRes.getString("TapeMeasure.New.Name")); //$NON-NLS-1$
		defaultColors = new Color[] { new Color(204, 0, 0) };

		// assign default plot variables
		setProperty("xVarPlot0", dataVariables[0]); //$NON-NLS-1$
		setProperty("yVarPlot0", dataVariables[1]); //$NON-NLS-1$
		setProperty("xVarPlot1", dataVariables[0]); //$NON-NLS-1$
		setProperty("yVarPlot1", dataVariables[2]); //$NON-NLS-1$

		// assign default table variables: length and angle
		setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$

		// set up footprint choices and color
		tapeFootprints = new Footprint[] { LineFootprint.getFootprint("Footprint.DoubleArrow"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.BoldDoubleArrow"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.Line"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.BoldLine") }; //$NON-NLS-1$
		stickFootprints = new Footprint[] { LineFootprint.getFootprint("Footprint.BoldDoubleTarget"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.DoubleTarget") }; //$NON-NLS-1$
		setViewable(false);
		setStickMode(false); // sets up footprint
		setReadOnly(false); // sets up dashed array
		setColor(defaultColors[0]);

//		// create ruler AFTER setting footprints and color
//		ruler = new WorldRuler(this);
//		
		// set initial hint
		partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
		hint = TrackerRes.getString("TapeMeasure.Hint"); //$NON-NLS-1$
		// eliminate minimum of magField
		magField.setMinValue(Double.NaN);
		end1Label = new JLabel();
		end2Label = new JLabel();
		lengthLabel = new JLabel();
		end1Label.setBorder(xLabel.getBorder());
		end2Label.setBorder(xLabel.getBorder());
		lengthLabel.setBorder(xLabel.getBorder());
		keyFrames.add(0);
		lengthKeyFrames.add(0);
		final FocusListener magFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (magField.getBackground() == Color.yellow) {
					int n = tp.getFrameNumber();
					// if not fixed, add frame number to key frames
					if (!isFixedPosition())
						keyFrames.add(n);
					TapeStep step = (TapeStep) getStep(n);
					// replace with key frame step
					step = (TapeStep) getKeyStep(step);
					String rawText = magField.getText();
					if (!TapeMeasure.this.isReadOnly()) {
						checkLengthUnits(rawText);
					}
					step.setTapeLength(magField.getValue());
					invalidateData(null);
					if (isFixedPosition())
						fireStepsChanged();
					else
						firePropertyChange(PROPERTY_TTRACK_STEP, null, new Integer(n)); // $NON-NLS-1$
					if (tp.getSelectedPoint() instanceof TapeStep.Rotator)
						tp.setSelectedPoint(null);
				}
			}
		};
		magField.addFocusListener(magFocusListener);
		magField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				magFocusListener.focusLost(null);
				magField.requestFocusInWindow();
			}
		});
		final FocusListener angleFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (angleField.getBackground() == Color.yellow) {
					int n = tp.getFrameNumber();
					// if not fixed, add frame number to key frames
					if (!isFixedPosition())
						keyFrames.add(n);
					TapeStep step = (TapeStep) getStep(n);
					// replace with key frame step
					step = (TapeStep) getKeyStep(step);
					step.setTapeAngle(angleField.getValue());
					invalidateData(null);
					if (isFixedPosition())
						fireStepsChanged();
					else
						firePropertyChange(PROPERTY_TTRACK_STEP, null, new Integer(n)); // $NON-NLS-1$
					if (!isReadOnly())
						tp.getAxes().setVisible(true);
				}
			}
		};
		angleField.addFocusListener(angleFocusListener);
		angleField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				angleFocusListener.focusLost(null);
				angleField.requestFocusInWindow();
			}
		});
	}

	/**
	 * Sets the fixed length property. When it is fixed, it has the same world
	 * length at all times. Applies to sticks only.
	 *
	 * @param fixed <code>true</code> to fix the length
	 */
	public void setFixedLength(boolean fixed) {
		if (fixedLength == fixed)
			return;
		XMLControl control = new XMLControlElement(this);
		if (tp != null) {
			int n = tp.getFrameNumber();
			tp.changed = true;
			TapeStep keyStep = (TapeStep) getStep(n);
			for (int i = 0; i < steps.array.length; i++) {
				TapeStep step = (TapeStep) steps.getStep(i);
				if (step == null || keyStep == null)
					continue;
				step.worldLength = keyStep.worldLength;
			}
			TFrame.repaintT(tp);
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
		for (Footprint footprint : getFootprints()) {
			if (footprint instanceof DoubleArrowFootprint) {
				DoubleArrowFootprint line = (DoubleArrowFootprint) footprint;
				line.setSolidHead(isReadOnly() ? false : true);
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
	 * and the scale changes when you drag the mouse. When false, the "tape"
	 * stretches without changing the scale when you drag the mouse.
	 *
	 * @param stick <code>true</code> for stick mode, <code>false</code> for tape
	 *              mode
	 */
	public void setStickMode(boolean stick) {
		stickMode = stick;
		// set footprints and update world lengths
		if (isStickMode()) {
			setFootprints(stickFootprints);
		} else {
			setFootprints(tapeFootprints);
		}
		defaultFootprint = getFootprint();
		if (ruler != null) {
			ruler.setStrokeWidth(defaultFootprint.getStroke().getLineWidth());
		}
		// set tip edit triggers
		for (Step step : getSteps()) {
			if (step != null) {
				TapeStep tapeStep = (TapeStep) step;
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
	 * Sets this to be a calibration tape or stick.
	 *
	 * @param worldLength the initial length of a calibration stick (ignored by
	 *                    tape)
	 */
	public void setCalibrator(Double worldLength) {
		isCalibrator = true;
		calibrationLength = worldLength;
	}

	@Override
	public boolean isMarkByDefault() {
		boolean incomplete = getStep(0) == null || isIncomplete;
		return (isCalibrator && incomplete) || super.isMarkByDefault();
	}

	/**
	 * Overrides TTrack method.
	 *
	 * @param locked <code>true</code> to lock this
	 */
	@Override
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
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
			if (isStickMode() && !isStepChangingScale) {
				// stretch or squeeze stick to keep constant world length
				int n = tp.getFrameNumber();
				TapeStep step = (TapeStep) getStep(n);
				if (step != null)
					step.adjustTipsToLength();
				if (!isFixedPosition()) {
					keyFrames.add(n);
				}
			}
			repaint();
			break;
		case Trackable.PROPERTY_ADJUSTING:
			if (e.getSource() instanceof TrackerPanel) { // $NON-NLS-1$
				refreshDataLater = (Boolean) e.getNewValue();
				if (!refreshDataLater) { // stopped adjusting
					firePropertyChange(PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
				}
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
			if (tp.getSelectedTrack() == this) {
				TapeStep step = (TapeStep) getStep(tp.getFrameNumber());
				if (step != null)
					step.getTapeLength(!isStickMode());
				boolean enabled = isFieldsEnabled();
				magField.setEnabled(enabled);
				angleField.setEnabled(enabled);
				stepValueLabel.setText(e.getNewValue() + ":"); //$NON-NLS-1$
			}
			break;
		case PROPERTY_TTRACK_LOCKED:
			boolean enabled = isFieldsEnabled();
			magField.setEnabled(enabled);
			angleField.setEnabled(enabled);
			break;
		case ImageCoordSystem.PROPERTY_COORDS_FIXEDSCALE:
			if (isStickMode() && e.getNewValue() == Boolean.FALSE) {
				setFixedPosition(false);
			}
			break;
		case PROPERTY_TTRACK_STEP:
		case PROPERTY_TTRACK_STEPS:
			refreshAttachments();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT:
			TapeStep step = (TapeStep) getStep(tp.getFrameNumber());
			step.rotatorDrawShapes[0] = null;
			step.rotatorDrawShapes[1] = null;
		default:
			super.propertyChange(e);
		}
	}

	/**
	 * Overrides TTrack setVisible method to change notYetShown flag.
	 *
	 * @param visible <code>true</code> to show this track
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			notYetShown = false;
	}

	/**
	 * Overrides TTrack isLocked method.
	 *
	 * @return <code>true</code> if this is locked
	 */
	@Override
	public boolean isLocked() {
		boolean locked = super.isLocked();
		if (!readOnly && tp != null && !(tp.getSelectedPoint() instanceof TapeStep.Handle)) {
			locked = locked || tp.getCoords().isLocked();
		}
		return locked;
	}

	/**
	 * Implements createStep but only mimics step creation since steps are created
	 * automatically by the autofill StepArray.
	 *
	 * @param n the frame number
	 * @param x the x coordinate in image space
	 * @param y the y coordinate in image space
	 * @return the step
	 */
	@Override
	public Step createStep(int n, double x, double y) {
		TapeStep step = (TapeStep) getStep(n);
		isIncomplete = false;
		if (step == null) {
			isIncomplete = true;
			// create new step of length zero
			step = new TapeStep(this, n, x, y, x, y);
//			step.worldLength = step.getTapeLength(true); // sets to zero
			step.worldLength = 0; // sets to zero
			step.setFootprint(getFootprint());
			steps = new StepArray(step); // autofill
			step = (TapeStep) getStep(n); // must do this since line above changes n to 0
		} else if (step.worldLength == 0) {
			TapeStep step0 = (TapeStep) getStep(0);
			// mark both target step and step 0 when initializing
			TapeStep targetStep = tp.getCoords().isFixedScale() ? step0 : (TapeStep) getStep(n);
			// set location of end2
			targetStep.getEnd2().setLocation(x, y);
			step0.getEnd2().setLocation(x, y); // this establishes the initial ANGLE of the tape
			// set world length of step 0 since initially all will have same value
			double worldLen = targetStep.getTapeLength(true);
			step0.worldLength = worldLen;
			if (calibrationLength != null) {
				// update coords
				targetStep.setTapeLength(calibrationLength);
				calibrationLength = null;
			}
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					tp.setSelectedPoint(null);
				}
			});

//			final TapeStep theStep = step;
//			Timer timer = new Timer(100, new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					setEditing(true, theStep, null);
//				}
//			});
//			timer.setRepeats(false);
//			timer.start();
		} else {
			TPoint[] pts = step.getPoints();
			TPoint p = tp == null ? null : tp.getSelectedPoint();
			if (p == null) {
				p = pts[0];
			}
			if (p == pts[0] || p == pts[1]) {
				p.setXY(x, y);
				if (tp != null) {
					tp.setSelectedPoint(p);
				}
			}
		}
		return step;
	}

	/**
	 * Mimics step creation by setting end positions of an existing step. If no
	 * existing step, creates one and autofills array
	 *
	 * @param n  the frame number
	 * @param x1 the x coordinate of end1 in image space
	 * @param y1 the y coordinate of end1 in image space
	 * @param x2 the x coordinate of end2 in image space
	 * @param y2 the y coordinate of end2 in image space
	 * @return the step
	 */
	public Step createStep(int n, double x1, double y1, double x2, double y2) {
		TapeStep step = (TapeStep) steps.getStep(n);
		if (step == null) {
			step = new TapeStep(this, n, x1, y1, x2, y2);
			step.worldLength = step.getTapeLength(true);
			step.setFootprint(getFootprint());
			steps = new StepArray(step); // autofill
			if (calibrationLength != null) {
				// update coords
				step.setTapeLength(calibrationLength);
				calibrationLength = null;
			}
		} else {
			if (isIncomplete) {
				step.getEnd2().setLocation(x2, y2);
				repaint();
			} else {
				step.getEnd1().setLocation(x1, y1);
				step.getEnd2().setLocation(x2, y2);
			}
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
	@Override
	public TPoint autoMarkAt(int n, double x, double y) {
		TapeStep step = (TapeStep) getStep(n);
		if (step == null || step.worldLength == 0) {
			return null;
		}
		int index = getTargetIndex();
		TPoint p = step.getPoints()[index];
		if (p == null)
			return null;
		setFixedPosition(false);
		if (isStickMode()) {
			ImageCoordSystem coords = tp.getCoords();
			coords.setFixedScale(false);
		}
		p.setAdjusting(true);
		p.setXY(x, y);
		p.setAdjusting(false);
		return p;
	}

	/**
	 * Gets the length of the steps created by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getStepLength() {
		return TapeStep.getLength();
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
	 * Formats the specified length value.
	 *
	 * @param length the length value to format
	 * @return the formatted length string
	 */
	public String getFormattedLength(double length) {
		inputField.setFormatFor(length);
		return inputField.format(length);
	}

	/**
	 * Reports whether or not this is viewable.
	 *
	 * @return <code>true</code> if this track is viewable
	 */
	@Override
	public boolean isViewable() {
		return isReadOnly() && !isStickMode();
	}

	/**
	 * Determines if at least one point in this track is autotrackable.
	 *
	 * @return true if autotrackable
	 */
	@Override
	protected boolean isAutoTrackable() {
		TapeStep step = (TapeStep) getStep(tp.getFrameNumber());
		if (step == null || step.worldLength == 0)
			return false;
		return true;
	}

	/**
	 * Returns a description of a target point with a given index.
	 *
	 * @param pointIndex the index
	 * @return the description
	 */
	@Override
	protected String getTargetDescription(int pointIndex) {
		String s = TrackerRes.getString("Calibration.Point.Name"); //$NON-NLS-1$
		return s + " " + (pointIndex + 1); //$NON-NLS-1$
	}

	/**
	 * Returns a menu with items that control this track.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	@Override
	public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu0) {
		// assemble the menu
		JMenu menu = super.getMenu(trackerPanel, menu0);
		if (menu0 == null)
			return menu;

		getMenuItems();

		lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());

		// remove end items and last separator
		removeDeleteTrackItem(menu);

		TapeStep step = (TapeStep) steps.getStep(0);
		// add items
		// put fixed position item after locked item
		boolean fixedScale = trackerPanel.getCoords().isFixedScale();
		boolean canBeFixed = !lockedItem.isSelected() && (fixedScale || !isStickMode());
		fixedItem.setEnabled(canBeFixed && step != null && step.worldLength > 0 && !isAttached());
		fixedItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
		fixedItem.setSelected(isFixedPosition() && fixedScale);
		addFixedItem(menu);

		// insert the attachments dialog item at beginning
		attachmentItem.setEnabled(step != null && step.worldLength > 0);
		attachmentItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.Attach")); //$NON-NLS-1$
		menu.insert(attachmentItem, 0);
		menu.insertSeparator(1);

//  	if (isStickMode()) {
//	    fixedLengthItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.FixedLength")); //$NON-NLS-1$
//	    fixedLengthItem.setSelected(isFixedLength());
//	  	menu.add(fixedLengthItem);
//  	}

		menu.addSeparator();
		menu.add(deleteTrackItem);
		return menu;
	}

	@Override
	protected void getMenuItems() {
		if (fixedItem != null)
			return;
		super.getMenuItems();
		fixedItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
		fixedItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setFixedPosition(fixedItem.isSelected());
			}
		});
//		fixedLengthItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.FixedLength")); //$NON-NLS-1$
//		fixedLengthItem.addItemListener(new ItemListener() {
//			@Override
//			public void itemStateChanged(ItemEvent e) {
//				setFixedLength(fixedLengthItem.isSelected());
//			}
//		});
		attachmentItem = new JMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.Attach")); //$NON-NLS-1$
		attachmentItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageCoordSystem coords = TapeMeasure.this.tp.getCoords();
				if (TapeMeasure.this.isStickMode() && coords.isFixedScale()) {
					int result = JOptionPane.showConfirmDialog(tframe,
							TrackerRes.getString("TapeMeasure.Alert.UnfixScale.Message1") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
					TrackerRes.getString("TapeMeasure.Alert.UnfixScale.Message2"), //$NON-NLS-1$
							TrackerRes.getString("TapeMeasure.Alert.UnfixScale.Title"), //$NON-NLS-1$
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result != JOptionPane.YES_OPTION)
						return;
					coords.setFixedScale(false);
				}
				AttachmentDialog control = TapeMeasure.this.tp.getAttachmentDialog(TapeMeasure.this);
				control.setVisible(true);
			}
		});
	}

	/**
	 * Returns a list of point-related toolbar components.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a list of components
	 */
	@Override
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
		magLabel.setText(TrackerRes.getString("TapeMeasure.Label.Length")); //$NON-NLS-1$
		magField.setToolTipText(TrackerRes.getString("TapeMeasure.Field.Magnitude.Tooltip")); //$NON-NLS-1$
		magField.setUnits(trackerPanel.getUnits(this, dataVariables[1]));

		// put step number into label and add to list
		stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int n = clip.frameToStep(trackerPanel.getFrameNumber());
		stepValueLabel.setText(n + ":"); //$NON-NLS-1$
		list.add(stepSeparator);

		// look for newly created tapes
		n = trackerPanel.getFrameNumber();
		TapeStep step = (TapeStep) getStep(n);

		// add world coordinate fields and labels
		boolean exists = step != null;
		boolean complete = exists && step.worldLength > 0;
		String unmarked = isStickMode() ? TrackerRes.getString("TapeMeasure.Label.UnmarkedStick")
				: TrackerRes.getString("TapeMeasure.Label.UnmarkedTape"); //$NON-NLS-1$
		if (!exists) {
			end1Label.setText(unmarked); // $NON-NLS-1$
			end1Label.setForeground(Color.green.darker());
			list.add(end1Label);
		} else if (!complete) {
			end1Label.setText(unmarked); // $NON-NLS-1$ //$NON-NLS-2$
			end1Label.setForeground(Color.green.darker());
			list.add(end1Label);
		} else {
			rulerCheckbox.setText(TrackerRes.getString("InputTrack.Checkbox.Ruler")); //$NON-NLS-1$
			rulerCheckbox.setToolTipText(TrackerRes.getString("InputTrack.Checkbox.Ruler.Tooltip")); //$NON-NLS-1$
			rulerCheckbox.setSelected(ruler != null && ruler.isVisible());
			list.add(rulerCheckbox);

			list.add(stepLabel);
			list.add(stepValueLabel);
			list.add(tSeparator);
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
		}
		return list;
	}

	/**
	 * Implements findInteractive method.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position on the panel
	 * @param ypix  the y pixel position on the panel
	 * @return the first step or motion vector that is hit
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		if (!isVisible() || !(panel instanceof TrackerPanel))
			return null;
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		int n = trackerPanel.getFrameNumber();
		TapeStep step = (TapeStep) getStep(n);
		if (step == null) {
			partName = null;
			hint = TrackerRes.getString("TapeMeasure.MarkEnd.Hint") + " 1"; //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		if (trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
			TPoint[] pts = step.points;
			TPoint p = trackerPanel.getSelectedPoint();
			Interactive ia = step.findInteractive(trackerPanel, xpix, ypix);
			if (ruler != null && ruler.isVisible()) {
				boolean b = (ia == ruler.getHandle() || p == ruler.getHandle());
				if (ruler.hitShapeVisible != b) {
					ruler.setHitShapeVisible(b);
					// BH 2020.12.10 repaint is necessary because just setting the message label
					// does not trigger a panel repaint in JavaScript (by design)
					if (OSPRuntime.isJS)
						TFrame.repaintT(trackerPanel);
				}
			}
			if (step.worldLength == 0) {
				if (ia instanceof TapeStep.Tip || ia instanceof TapeStep.Handle) {
					ia = step.handle;
					partName = TrackerRes.getString("TapeMeasure.End.Name") + " 1"; //$NON-NLS-1$ //$NON-NLS-2$
					hint = TrackerRes.getString("TapeMeasure.Handle.Hint"); //$NON-NLS-1$
				} else {
					partName = null;
					hint = TrackerRes.getString("TapeMeasure.MarkEnd.Hint") + " 2"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if (ia == null) {
				if (p == pts[0] || p == pts[1]) {
					partName = TrackerRes.getString("TapeMeasure.End.Name"); //$NON-NLS-1$
					if (isStickMode() && !isReadOnly())
						hint = TrackerRes.getString("CalibrationStick.End.Hint"); //$NON-NLS-1$
					else
						hint = TrackerRes.getString("TapeMeasure.End.Hint"); //$NON-NLS-1$
				} else {
					partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
					if (!isReadOnly())
						hint = TrackerRes.getString("CalibrationTapeMeasure.Hint"); //$NON-NLS-1$
					else
						hint = TrackerRes.getString("TapeMeasure.Hint"); //$NON-NLS-1$
				}
				return null;
			} else if (ia instanceof TapeStep.Tip) {
				partName = TrackerRes.getString("TapeMeasure.End.Name"); //$NON-NLS-1$
				if (isStickMode() && !isReadOnly())
					hint = TrackerRes.getString("CalibrationStick.End.Hint"); //$NON-NLS-1$
				else
					hint = TrackerRes.getString("TapeMeasure.End.Hint"); //$NON-NLS-1$
			} else if (ia instanceof TapeStep.Handle) {
				partName = TrackerRes.getString("TapeMeasure.Handle.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("TapeMeasure.Handle.Hint"); //$NON-NLS-1$
			} else if (ia instanceof TapeStep.Rotator) {
				partName = TrackerRes.getString("TapeMeasure.Rotator.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("TapeMeasure.Rotator.Hint"); //$NON-NLS-1$
				// DB 2020.12.26 repaint is required in JavaScript?
				if (OSPRuntime.isJS)
					TFrame.repaintT(trackerPanel);
			} else if (ruler != null && ia == ruler.getHandle()) {
				partName = TrackerRes.getString("TapeMeasure.Ruler.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("TapeMeasure.Ruler.Hint"); //$NON-NLS-1$
			} else if (ia == this) {
				partName = TrackerRes.getString("TapeMeasure.Readout.Magnitude.Name"); //$NON-NLS-1$
				if (!isReadOnly())
					hint = TrackerRes.getString("CalibrationTapeMeasure.Readout.Magnitude.Hint"); //$NON-NLS-1$
				else
					hint = TrackerRes.getString("TapeMeasure.Readout.Magnitude.Hint"); //$NON-NLS-1$
			}
			return isLocked() ? null : ia;
		}
		return null;
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
//		Dataset length = data.getDataset(count++);
//		Dataset angle = data.getDataset(count++);
//		Dataset stepNum = data.getDataset(count++);
//		Dataset frameNum = data.getDataset(count++);
//		String time = dataVariables[0];
//		if (!length.getColumnName(0).equals(time)) { // not yet initialized
//			length.setXYColumnNames(time, dataVariables[1]);
//			angle.setXYColumnNames(time, dataVariables[2]);
//			stepNum.setXYColumnNames(time, dataVariables[3]);
//			frameNum.setXYColumnNames(time, dataVariables[4]);
//		} else
//			for (int i = 0; i < count; i++) {
//				data.getDataset(i).clear();
//			}
//		dataDescriptions = new String[count + 1];
//		for (int i = 0; i < dataDescriptions.length; i++) {
//			dataDescriptions[i] = TrackerRes.getString("TapeMeasure.Data.Description." + i); //$NON-NLS-1$
//		}

		// get the datasets
		// assign column names to the datasets
		// fill dataDescriptions array
		int count = 4;
		// look thru steps and get data for those included in clip
		VideoPlayer player = trackerPanel.getPlayer();
		VideoClip clip = player.getVideoClip();
		int len = clip.getStepCount();
		double[][] validData = new double[count + 1][len];
		dataFrames.clear();
		for (int i = 0; i < len; i++) {
			int frame = clip.stepToFrame(i);
			TapeStep step = (TapeStep) getStep(frame);
			if (step == null)
				continue;
			step.dataVisible = true;
			// get the step number and time
			double t = player.getStepTime(i) / 1000.0;
			validData[0][i] = step.getTapeLength(true);
			validData[1][i] = step.getTapeAngle();
			validData[2][i] = i;
			validData[3][i] = frame;
			validData[4][i] = t;
			dataFrames.add(frame);
		}
		clearColumns(data, count, dataVariables, "TapeMeasure.Data.Description.", validData, len);
	}

	/**
	 * Remarks all steps on the specified panel. Overrides TTrack method.
	 *
	 * @param trackerPanel the tracker panel
	 */
	@Override
	public void remark(TrackerPanel trackerPanel) {
		super.remark(trackerPanel);
		displayState();
	}

	/**
	 * Overrides Object toString method.
	 *
	 * @return the name of this track
	 */
	@Override
	public String toString() {
		return TrackerRes.getString("TapeMeasure.Name"); //$NON-NLS-1$
	}

	@Override
	public Map<String, NumberField[]> getNumberFields() {
		if (numberFields.isEmpty()) {
			numberFields.put(dataVariables[0], new NumberField[] { tField });
			numberFields.put(dataVariables[1], new NumberField[] { magField, inputField });
			numberFields.put(dataVariables[2], new NumberField[] { angleField });
		}
		return numberFields;
	}

	/**
	 * Returns a popup menu for the input field (readout).
	 *
	 * @return the popup menu
	 */
	protected JPopupMenu getInputFieldPopup() {
		JPopupMenu popup = new JPopupMenu();
		if (tp.isEnabled("number.formats") || tp.isEnabled("number.units")) { //$NON-NLS-1$ //$NON-NLS-2$
			JMenu numberMenu = new JMenu(TrackerRes.getString("Popup.Menu.Numbers")); //$NON-NLS-1$
			popup.add(numberMenu);
			if (tp.isEnabled("number.formats")) { //$NON-NLS-1$
				JMenuItem item = new JMenuItem();
				final String[] selected = new String[] { dataVariables[1] };
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						NumberFormatDialog.getNumberFormatDialog(tp, TapeMeasure.this, selected)
								.setVisible(true);
					}
				});
				item.setText(TrackerRes.getString("Popup.MenuItem.Formats") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				numberMenu.add(item);
			}
			if (tp.isEnabled("number.units")) { //$NON-NLS-1$
				JMenuItem item = new JMenuItem();
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						UnitsDialog dialog = tp.getUnitsDialog();
						dialog.setVisible(true);
					}
				});
				item.setText(TrackerRes.getString("Popup.MenuItem.Units") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				numberMenu.add(item);
			}
			popup.addSeparator();
		}

		boolean hasLengthUnit = tp.lengthUnit != null;
		boolean hasMassUnit = tp.massUnit != null;
		if (hasLengthUnit && hasMassUnit) {
			JMenuItem item = new JMenuItem();
			final boolean vis = tp.isUnitsVisible();
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tp.setUnitsVisible(!vis);
					tp.refreshTrackBar();
//					TTrackBar.getTrackbar(trackerPanel).refresh();
					Step step = getStep(tp.getFrameNumber());
					step.repaint();
				}
			});
			item.setText(vis ? TrackerRes.getString("TTrack.MenuItem.HideUnits") : //$NON-NLS-1$
					TrackerRes.getString("TTrack.MenuItem.ShowUnits")); //$NON-NLS-1$
			popup.add(item);
		}
		return popup;
	}

	@Override
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		Object[] objectsToSize = new Object[] { end1Label, end2Label, lengthLabel };
		FontSizer.setFonts(objectsToSize);
	}

//__________________________ protected and private methods _______________________

	private final static String[] panelEventsTapeMeasure = new String[] { 
			ImageCoordSystem.PROPERTY_COORDS_FIXEDSCALE,  // TapeMeasure
			ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, // TapeMeasure
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, // TapeMeasure
			TTrack.PROPERTY_TTRACK_LOCKED, // TapeMeasure
	};

	/**
	 * Overrides TTrack setTrackerPanel method.
	 *
	 * @param panel the TrackerPanel
	 */
	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null)
			removePanelEvents(panelEventsTapeMeasure);
		super.setTrackerPanel(panel);
		if (tp != null) {
			addPanelEvents(panelEventsTapeMeasure);
			boolean canBeFixed = !isStickMode() || tp.getCoords().isFixedScale();
			setFixedPosition(isFixedPosition() && canBeFixed);
		}
	}



	/**
	 * Refreshes world lengths at all steps based on current ends and scale.
	 */
	protected void refreshWorldLengths() {
		for (int i = 0; i < getSteps().length; i++) {
			TapeStep step = (TapeStep) getSteps()[i];
			if (step != null) {
				refreshStep(step);
				step.worldLength = step.getTapeLength(true);
			}
		}
	}

	private void checkLengthUnits(String rawText) {
		String[] split = rawText.split(" "); //$NON-NLS-1$
		if (split.length > 1) {
			// find first character not ""
			for (int i = 1; i < split.length; i++) {
				if (!"".equals(split[i])) { //$NON-NLS-1$
					if (split[i].equals(tp.getLengthUnit())) {
						tp.setUnitsVisible(true);
					} else {
						int response = JOptionPane.showConfirmDialog(tframe,
								TrackerRes.getString("TapeMeasure.Dialog.ChangeLengthUnit.Message") //$NON-NLS-1$
										+ " \"" + split[i] + "\" ?", //$NON-NLS-1$ //$NON-NLS-2$
								TrackerRes.getString("TapeMeasure.Dialog.ChangeLengthUnit.Title"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION);
						if (response == JOptionPane.YES_OPTION) {
							tp.setLengthUnit(split[i]);
							tp.setUnitsVisible(true);
						}
					}
					break;
				}
			}
		}

	}

	/**
	 * Displays the world coordinates of the currently selected step.
	 */
	private void displayState() {
		int n = tp == null ? 0 : tp.getFrameNumber();
		TapeStep step = (TapeStep) getStep(n);
		if (step != null) {
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
		if (isLocked())
			return false;
		// false if this tape is for calibration and coords are locked
		if (!isReadOnly() && tp != null && tp.getCoords().isLocked())
			return false;
		// false if both ends are attached to point masses
		int n = tp == null ? 0 : tp.getFrameNumber();
		TapeStep step = (TapeStep) getStep(n);
		if (step != null && step.end1.isAttached() && step.end2.isAttached())
			return false;
		return true;
	}

	// __________________________ InputTrack ___________________________

	@Override
	protected Ruler getRuler() {
		if (ruler == null)
			ruler = new WorldRuler(this);
		return ruler;
	}

	/**
	 * Refreshes a step by setting it equal to the previous keyframe step.
	 *
	 * @param step the step to refresh
	 */
	@Override
	protected void refreshStep(Step step) {
		if (step == null || isIncomplete)
			return;
		int positionKey = 0, lengthKey = 0;
		for (int i : keyFrames) {
			if (i <= step.n)
				positionKey = i;
		}
		for (int i : lengthKeyFrames) {
			if (i <= step.n)
				lengthKey = i;
		}
		// compare step with keyStep
		boolean different = false;
		boolean changed = false;
		// check position
		TapeStep t = (TapeStep) step;
		TapeStep k = (TapeStep) steps.getStep(isFixedPosition() ? 0 : positionKey);
		if (k != t) {
			different = (int) (1000000 * k.getEnd1().x) != (int) (1000000 * t.getEnd1().x)
					|| (int) (1000000 * k.getEnd1().y) != (int) (1000000 * t.getEnd1().y)
					|| (int) (1000000 * k.getEnd2().x) != (int) (1000000 * t.getEnd2().x)
					|| (int) (1000000 * k.getEnd2().y) != (int) (1000000 * t.getEnd2().y);
			if (different) {
				t.getEnd1().setLocation(k.getEnd1());
				t.getEnd2().setLocation(k.getEnd2());
				changed = true;
			}
		}

		// check length only if in stick mode
		if (isStickMode() || t.worldLength == 0) {
			k = (TapeStep) steps.getStep(isFixedLength() ? 0 : lengthKey);
			different = k.worldLength != t.worldLength;
			if (different) {
				t.worldLength = k.worldLength;
				changed = true;
			}
		}
		// erase step if changed
		if (changed) {
			t.erase();
		}
	}

	@SuppressWarnings("serial")
	@Override
	protected NumberField createInputField() {
		return new TrackNumberField() {
			@Override
			public void setFixedPattern(String pattern) {
				super.setFixedPattern(pattern);
				setMagValue();
			}

		};
	}

	@Override
	protected Rectangle getLayoutBounds(Step step) {
		return ((TapeStep) step).panelLayoutBounds.get(tp.getID());
	}

	@Override
	protected boolean checkKeyFrame() {
		return (!editing && (readOnly || isStickMode()));
	}

	@Override
	protected void endEditing(Step step, String rawText) {
		// pass in raw text from InputField since changes before this is called
		TapeStep t = (TapeStep) step;
		t.drawLayoutBounds = false;
		if (!this.isReadOnly()) {
			checkLengthUnits(rawText);
		}
		if (t.worldLength > 0) {
			t.setTapeLength(inputField.getValue());
			t.repaint(tp);
		}
		inputField.setSigFigs(4);
	}

	@Override
	protected void setInputValue(Step step) {
		inputField.setValue(((TapeStep) step).getTapeLength(!isStickMode()));
		inputField.setUnits(tp.getUnits(this, dataVariables[1]));
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
			TapeMeasure tape = (TapeMeasure) obj;
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
			int count = tape.isFixedPosition() ? 1 : steps.length;
			FrameData[] data = new FrameData[count];
			for (int n = 0; n < count; n++) {
				// save only position key frames
				if (steps[n] == null || !tape.keyFrames.contains(n))
					continue;
				data[n] = new FrameData((TapeStep) steps[n]);
			}
			control.setValue("framedata", data); //$NON-NLS-1$
			// save step world lengths
			count = tape.isFixedLength() ? 1 : steps.length;
			Double[] lengths = new Double[count];
			for (int n = 0; n < count; n++) {
				// save only length key frames
				if (steps[n] == null || !tape.lengthKeyFrames.contains(n))
					continue;
				lengths[n] = ((TapeStep) steps[n]).worldLength;
			}
			control.setValue("worldlengths", lengths); //$NON-NLS-1$
			if (tape.ruler != null && tape.ruler.isVisible()) {
				control.setValue("rulersize", tape.ruler.getRulerSize()); //$NON-NLS-1$
				control.setValue("rulerspacing", tape.ruler.getLineSpacing()); //$NON-NLS-1$
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
			return new TapeMeasure();
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
			TapeMeasure tape = (TapeMeasure) obj;
			tape.notYetShown = false;
			boolean locked = tape.isLocked();
			tape.setLocked(false);
			// load stickMode to set up footprint choices
			tape.setStickMode(control.getBoolean("stickmode")); //$NON-NLS-1$
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			// load position data
			tape.keyFrames.clear();
			FrameData[] data = (FrameData[]) control.getObject("framedata"); //$NON-NLS-1$
			if (data != null) {
				for (int n = 0; n < data.length; n++) {
					if (data[n] == null)
						continue;
					tape.createStep(n, data[n].data[0], data[n].data[1], data[n].data[2], data[n].data[3]);
				}
			}
			// load world lengths
			tape.lengthKeyFrames.clear();
			Double[] lengths = (Double[]) control.getObject("worldlengths"); //$NON-NLS-1$
			if (lengths != null) {
				for (int n = 0; n < lengths.length; n++) {
					if (lengths[n] == null)
						continue;
					TapeStep step = (TapeStep) tape.steps.getStep(n);
					step.worldLength = lengths[n];
					tape.lengthKeyFrames.add(n);
				}
			}
			// load fixed position
			tape.fixedPosition = control.getBoolean("fixedtape"); //$NON-NLS-1$
			// load fixed length
			if (control.getPropertyNamesRaw().contains("fixedlength")) //$NON-NLS-1$
				tape.fixedLength = control.getBoolean("fixedlength"); //$NON-NLS-1$
			// load ruler properties
			if (control.getPropertyNamesRaw().contains("rulersize")) { //$NON-NLS-1$
				tape.getRuler().setVisible(true);
				tape.ruler.setRulerSize(control.getDouble("rulersize")); //$NON-NLS-1$
				tape.ruler.setLineSpacing(control.getDouble("rulerspacing")); //$NON-NLS-1$
			}
			// load readOnly
			tape.setReadOnly(control.getBoolean("readonly")); //$NON-NLS-1$
			tape.setLocked(locked);
			tape.displayState();
			return obj;
		}
	}

	/**
	 * Inner class containing the tape data for a single frame number.
	 */
	public static class FrameData {
		double[] data = new double[4];

		FrameData() {
		}

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
	private static class FrameDataLoader implements XML.ObjectLoader {

		@Override
		public void saveObject(XMLControl control, Object obj) {
			FrameData data = (FrameData) obj;
			control.setValue("x1", data.data[0]); //$NON-NLS-1$
			control.setValue("y1", data.data[1]); //$NON-NLS-1$
			control.setValue("x2", data.data[2]); //$NON-NLS-1$
			control.setValue("y2", data.data[3]); //$NON-NLS-1$
		}

		@Override
		public Object createObject(XMLControl control) {
			return new FrameData();
		}

		@Override
		public Object loadObject(XMLControl control, Object obj) {
			FrameData data = (FrameData) obj;
			if (control.getPropertyNamesRaw().contains("x1")) { //$NON-NLS-1$
				data.data[0] = control.getDouble("x1"); //$NON-NLS-1$
				data.data[1] = control.getDouble("y1"); //$NON-NLS-1$
				data.data[2] = control.getDouble("x2"); //$NON-NLS-1$
				data.data[3] = control.getDouble("y2"); //$NON-NLS-1$
			}
			return obj;
		}
	}

}
