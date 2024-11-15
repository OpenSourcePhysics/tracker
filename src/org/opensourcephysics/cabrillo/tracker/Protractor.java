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
 * <https://opensourcephysics.github.io/tracker/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Trackable;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPlayer;

/**
 * A Protractor measures and displays angular arcs and arm lengths.
 *
 * @author Douglas Brown
 */
public class Protractor extends InputTrack {

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
		return "Protractor";
	}

	@Override
	public String getVarDimsImpl(String variable) {
		String[] vars = dataVariables;
		String[] names = formatVariables;
		if (names[1].equals(variable) || vars[2].equals(variable) || vars[3].equals(variable)) {
			return "L"; //$NON-NLS-1$
		}
		if (vars[4].equals(variable) || vars[5].equals(variable)) {
			return "I"; //$NON-NLS-1$
		}
		if (vars[7].equals(variable)) { // omega
			return "A/T"; //$NON-NLS-1$
		}
		if (vars[8].equals(variable)) { // alpha
			return "A/TT"; //$NON-NLS-1$
		}
		return null;
	}

	// static fields
	protected static final String[] dataVariables;
	protected static final String[] fieldVariables;
	protected static final String[] formatVariables;
	protected static final Map<String, String[]> formatMap;
	protected static final Map<String, String> formatDescriptionMap;

	static {
		dataVariables = new String[] { 
				"t", 
				Tracker.THETA, 
				"L_{1}", 
				"L_{2}", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"step", 
				"frame", 
				Tracker.THETA + "_{rot}",
				TeXParser.parseTeX("$\\omega$"), //$NON-NLS-1$
				TeXParser.parseTeX("$\\alpha$")}; //$NON-NLS-1$
		fieldVariables = dataVariables; // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		formatVariables = new String[] { 
				"t", 
				"L", 
				Tracker.THETA,
				TeXParser.parseTeX("$\\omega$"), //$NON-NLS-1$ 8
				TeXParser.parseTeX("$\\alpha$")}; //$NON-NLS-1$ //$NON-NLS-2$

		// assemble format map
		formatMap = new HashMap<>();

		formatMap.put("t", new String[] { "t" });
		formatMap.put("L", new String[] { "L_{1}", "L_{2}" });
		formatMap.put(Tracker.THETA, new String[] { Tracker.THETA, Tracker.THETA + "_{rot}" });

		// assemble format description map
		formatDescriptionMap = new HashMap<String, String>();
		formatDescriptionMap.put(formatVariables[0], TrackerRes.getString("PointMass.Data.Description.0")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[1], TrackerRes.getString("TapeMeasure.Label.Length")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[2], TrackerRes.getString("Vector.Data.Description.4")); //$NON-NLS-1$

	}

	protected final static ArrayList<String> allVariables = createAllVariables(dataVariables, null); // no new field vars

	// instance fields
	protected JMenuItem attachmentItem;
	// for derivatives
	protected int firstDerivSpill = 1;
	protected int secondDerivSpill = 2;
	protected int[] params = new int[4];
	protected double[] rotationAngle = new double[5];
//	protected double[] alpha = new double[5];
	protected boolean[] validData = new boolean[5];
	protected Object[] derivData = new Object[] { params, rotationAngle, null, validData };

	/**
	 * Constructs a Protractor.
	 */
	public Protractor() {
		super(TYPE_PROTRACTOR);
		defaultColors = new Color[] { new Color(0, 140, 40) };
		// assign a default name
		setName(TrackerRes.getString("Protractor.New.Name")); //$NON-NLS-1$
		// set up footprint choices and color
		setFootprints(new Footprint[] { ProtractorFootprint.getFootprint("ProtractorFootprint.Circle3"), //$NON-NLS-1$
				ProtractorFootprint.getFootprint("ProtractorFootprint.Circle5"), //$NON-NLS-1$
				ProtractorFootprint.getFootprint("ProtractorFootprint.Circle3Bold"), //$NON-NLS-1$
				ProtractorFootprint.getFootprint("ProtractorFootprint.Circle5Bold") }); //$NON-NLS-1$
		defaultFootprint = getFootprint();
		setColor(defaultColors[0]);

//		// create ruler AFTER setting footprints and color
//		ruler = new AngleRuler(this);
//
		// assign default table variables
		setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$

		keyFrames.add(0);
		// set initial hint
		partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
		hint = TrackerRes.getString("Protractor.Hint"); //$NON-NLS-1$
		// initialize the autofill step array
		ProtractorStep step = new ProtractorStep(this, 0, 100, 150, 200, 150);
		step.setFootprint(getFootprint());
		steps = new StepArray(step); // autofills
		fixedItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
		fixedItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setFixedPosition(fixedItem.isSelected());
			}
		});
		attachmentItem = new JMenuItem(TrackerRes.getString("MeasuringTool.MenuItem.Attach")); //$NON-NLS-1$
		attachmentItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttachmentDialog control = tp.getAttachmentDialog(Protractor.this);
				control.setVisible(true);
			}
		});
		final FocusListener arcFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (angleField.getBackground() == Color.yellow) {
					int n = tp.getFrameNumber();
					ProtractorStep step = (ProtractorStep) getStep(n);
					if (!isFixedPosition()) {
						keyFrames.add(n);
					}
					step = (ProtractorStep) getKeyStep(step);
					double theta = angleField.getValue();
					if (theta != step.getProtractorAngle(false)) {
						step.setProtractorAngle(theta);
						dataValid = false;
						if (isFixedPosition())
							fireStepsChanged();
						else
							firePropertyChange(PROPERTY_TTRACK_STEP, null, new Integer(n));
						tp.repaint();
					}
				}
			}
		};
		angleField.addFocusListener(arcFocusListener);
		angleField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				arcFocusListener.focusLost(null);
				angleField.requestFocusInWindow();
			}
		});

		final FocusListener lengthFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				NumberField field = (NumberField) e.getSource();
				int n = tp.getFrameNumber();
				ProtractorStep step = (ProtractorStep) getStep(n);
				if (!isFixedPosition()) {
					keyFrames.add(n);
				}
				step = (ProtractorStep) getKeyStep(step);
				double length = field.getValue();
				TPoint end = field == xField ? step.end1 : step.end2;
				if (length != step.getArmLength(end)) {
					step.setArmLength(end, length);
					dataValid = false;
					if (isFixedPosition())
						fireStepsChanged();
					else
						firePropertyChange(PROPERTY_TTRACK_STEP, null, new Integer(n)); // $NON-NLS-1$
					tp.repaint();
				}
			}
		};
		ActionListener lengthAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NumberField field = (NumberField) e.getSource();
				lengthFocusListener.focusLost(new FocusEvent(field, FocusEvent.FOCUS_LOST));
				field.requestFocusInWindow();
			}
		};
		xField.addFocusListener(lengthFocusListener);
		xField.addActionListener(lengthAction);
		yField.addFocusListener(lengthFocusListener);
		yField.addActionListener(lengthAction);

	}

	/**
	 * Responds to property change events. Overrides TTrack method.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		boolean isSelectedTrack = (tp.getSelectedTrack() == this);
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
			if (isSelectedTrack) {
				ProtractorStep step = (ProtractorStep) getStep(tp.getFrameNumber());
				step.getProtractorAngle(true); // refreshes angle field
				step.getFormattedLength(step.end1); // refreshes x field
				step.getFormattedLength(step.end2); // refreshes y field
				stepValueLabel.setText(e.getNewValue() + ":"); //$NON-NLS-1$
			}
			break;
		case Trackable.PROPERTY_ADJUSTING: // via TrackerPanel
			if (e.getSource() instanceof TrackerPanel) {
				refreshDataLater = (Boolean) e.getNewValue();
				if (!refreshDataLater) { // stopped adjusting
					firePropertyChange(PROPERTY_TTRACK_DATA, null, null);
				}
			}
			break;
		case PROPERTY_TTRACK_STEP:
		case PROPERTY_TTRACK_STEPS:
			refreshAttachments();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK:
			if (e.getOldValue() == this && e.getNewValue() != this) {
				repaint();
			}
			break;
		}
		super.propertyChange(e);
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
		Step step = steps.getStep(n);
		TPoint[] pts = step.getPoints();
		TPoint p = tp == null ? null : tp.getSelectedPoint();
		if (p == null) {
			p = pts[2];
		}
		if (p == pts[0] || p == pts[1] || p == pts[2]) {
			p.setXY(x, y);
			if (tp != null) {
				tp.setSelectedPoint(p);
				step.defaultIndex = p == pts[0] ? 0 : p == pts[1] ? 1 : 2;
			}
		}
		return step;
	}

	/**
	 * Mimics step creation by setting end positions of an existing step.
	 *
	 * @param n  the frame number
	 * @param x1 the x coordinate of end1 in image space
	 * @param y1 the y coordinate of end1 in image space
	 * @param x2 the x coordinate of end2 in image space
	 * @param y2 the y coordinate of end2 in image space
	 * @return the step
	 */
	public Step createStep(int n, double x1, double y1, double x2, double y2) {
		ProtractorStep step = (ProtractorStep) steps.getStep(n);
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
	@Override
	public TPoint autoMarkAt(int n, double x, double y) {
		setFixedPosition(false);
		ProtractorStep step = (ProtractorStep) steps.getStep(n);
		int i = getTargetIndex();
		if (i == 0) {
			step.vertex.setLocation(x, y);
		} else if (i == 1) {
			step.end1.setLocation(x, y);
		} else {
			step.end2.setLocation(x, y);
		}
		keyFrames.add(n);
		step.repaint();
		return getMarkedPoint(n, i);
	}

	/**
	 * Gets the length of the steps created by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getStepLength() {
		return ProtractorStep.getLength();
	}

	/**
	 * Determines if any point in this track is autotrackable.
	 *
	 * @return true if autotrackable
	 */
	@Override
	protected boolean isAutoTrackable() {
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
		if (pointIndex == 0)
			return TrackerRes.getString("Protractor.Vertex.Name"); //$NON-NLS-1$
		if (pointIndex == 1)
			return TrackerRes.getString("Protractor.Base.Name"); //$NON-NLS-1$
		return TrackerRes.getString("Protractor.End.Name"); //$NON-NLS-1$
	}

	/**
	 * Gets the length of the footprints required by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getFootprintLength() {
		return 3;
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

		// get the datasets
//		Dataset angle = data.getDataset(count++);
//		Dataset arm1Length = data.getDataset(count++);
//		Dataset arm2Length = data.getDataset(count++);
//		Dataset stepNum = data.getDataset(count++);
//		Dataset frameNum = data.getDataset(count++);
//		Dataset rotationAngle = data.getDataset(count++);
		// assign column names to the datasets
		int count = dataVariables.length - 1;
		dataFrames.clear();
		VideoPlayer player = trackerPanel.getPlayer();
		VideoClip clip = player.getVideoClip();
		int len = clip.getStepCount();
		double[][] validData = new double[count + 1][len];
//		double rotation = 0, prevAngle = 0;
		for (int i = 0; i < len; i++) {
			int frame = clip.stepToFrame(i);
			ProtractorStep next = (ProtractorStep) getStep(frame);
			next.dataVisible = true;
			double theta = next.getProtractorAngle(false);
//			// determine the cumulative rotation angle
//			double delta = theta - prevAngle;
//			if (delta < -Math.PI)
//				delta += 2 * Math.PI;
//			else if (delta > Math.PI)
//				delta -= 2 * Math.PI;
//			rotation += delta;
			// get the step number and time
			double t = player.getStepTime(i) / 1000.0;
			validData[0][i] = theta;
			validData[1][i] = next.getArmLength(next.end1);
			validData[2][i] = next.getArmLength(next.end2);
			validData[3][i] = i;
			validData[4][i] = frame;
//			validData[5][i] = rotation;
			validData[8][i] = t;
			dataFrames.add(frame);
//			prevAngle = theta;
		}
		// get the rotational data
		Object[] rotationData = getRotationData();
		double[] theta = (double[]) rotationData[0];
		double[] omega = (double[]) rotationData[1];
		double[] alpha = (double[]) rotationData[2];
		double dt = player.getMeanStepDuration() / 1000;
		for (int i = 0; i < len; i++) {
			validData[5][i] = theta[clip.stepToFrame(i)];
			validData[6][i] = omega[clip.stepToFrame(i)] / dt;
			validData[7][i] = alpha[clip.stepToFrame(i)] / (dt * dt);
		}
		clearColumns(data, count, dataVariables, "Protractor.Data.Description.", validData, len);
	}
	
	/**
	 * Gets the rotational data.
	 * 
	 * @return Object[] {theta, omega, alpha}
	 */
	protected Object[] getRotationData() {
		// initialize data arrays once, for all panels
		if (rotationAngle.length < steps.array.length) {
			derivData[1] = rotationAngle = new double[steps.array.length + 5];
			derivData[3] = validData = new boolean[steps.array.length + 5];
		}
		for (int i = 0; i < validData.length; i++)
			validData[i] = false;
		// set up derivative parameters
		VideoClip clip = tp.getPlayer().getVideoClip();
		params[1] = clip.getStartFrameNumber();
		params[2] = clip.getStepSize();
		params[3] = clip.getStepCount();
		// set up angular position data
		Step[] stepArray = steps.array;
		double rotation = 0;
		double prevAngle = 0;
		for (int n = 0; n < stepArray.length; n++) {
			if (stepArray[n] != null && clip.includesFrame(n)) {
				ProtractorStep next = (ProtractorStep) stepArray[n];
				
				double theta = next.getProtractorAngle(false);
				double delta = theta - prevAngle;
				if (delta < -Math.PI)
					delta += 2 * Math.PI;
				else if (delta > Math.PI)
					delta -= 2 * Math.PI;
				rotation += delta;
				prevAngle = theta;
				rotationAngle[n] = rotation;
				validData[n] = true;
			} else
				rotationAngle[n] = Double.NaN;
		}
		// unlock track while updating
		boolean isLocked = locked; // save for later restoration
		locked = false;

		// evaluate first derivative
		params[0] = firstDerivSpill; // spill
		Object[] result = PointMass.vDeriv.evaluate(derivData);
		double[] omega = (double[]) result[0];

		// evaluate second derivative
		params[0] = secondDerivSpill; // spill
		result = PointMass.aDeriv.evaluate(derivData);
		double[] alpha = (double[]) result[2];

		// restore locked state
		locked = isLocked;
		return new Object[] { rotationAngle, omega, alpha };
	}



	/**
	 * Returns the description of a particular attachment point.
	 * 
	 * @param n the attachment point index
	 * @return the description
	 */
	@Override
	public String getAttachmentDescription(int n) {
		// end1 is "base", end2 is "arm"
		return TrackerRes.getString(n == 0 ? "AttachmentInspector.Label.Vertex" : //$NON-NLS-1$
				n == 1 ? "Protractor.Attachment.Base" : //$NON-NLS-1$
						"Protractor.Attachment.Arm"); //$NON-NLS-1$
	}

	/**
	 * Returns a menu with items that control this track.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	@Override
	public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu0) {
		JMenu menu = super.getMenu(trackerPanel, menu0);
		if (menu0 == null)
			return menu;

//    lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());
		fixedItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
		fixedItem.setSelected(isFixedPosition());
		fixedItem.setEnabled(!isAttached());

//    // remove end items and last separator
//    menu.remove(deleteTrackItem);
//    menu.remove(menu.getMenuComponent(menu.getItemCount()-1));

		// put fixed item after locked item
		addFixedItem(menu);
		// insert the attachments dialog item at beginning
		attachmentItem.setText(TrackerRes.getString("MeasuringTool.MenuItem.Attach")); //$NON-NLS-1$
		menu.insert(attachmentItem, 0);
		menu.insertSeparator(1);

//  	menu.addSeparator();
//    menu.add(deleteTrackItem);
		return menu;
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

		stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
		angleLabel.setText(TrackerRes.getString("Protractor.Label.Angle")); //$NON-NLS-1$
		angleField.setToolTipText(TrackerRes.getString("Protractor.Field.Angle.Tooltip")); //$NON-NLS-1$
		xLabel.setText(dataVariables[2]);
		yLabel.setText(dataVariables[3]);
		xField.setUnits(trackerPanel.getUnits(this, dataVariables[2]));
		yField.setUnits(trackerPanel.getUnits(this, dataVariables[3]));
		ProtractorStep step = (ProtractorStep) getStep(trackerPanel.getFrameNumber());
		xField.setEnabled(!step.end1.isAttached() || !step.vertex.isAttached());
		yField.setEnabled(!step.end2.isAttached() || !step.vertex.isAttached());
	

		// put step number into label
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int n = clip.frameToStep(trackerPanel.getFrameNumber());
		stepValueLabel.setText(n + ":"); //$NON-NLS-1$

		angleField.setEnabled(!isFullyAttached() && !isLocked());

		rulerCheckbox.setText(TrackerRes.getString("InputTrack.Checkbox.Ruler")); //$NON-NLS-1$
		rulerCheckbox.setToolTipText(TrackerRes.getString("InputTrack.Checkbox.Ruler.Tooltip")); //$NON-NLS-1$
		rulerCheckbox.setSelected(ruler != null && ruler.isVisible());
		list.add(rulerCheckbox);
		list.add(stepSeparator);
		list.add(stepLabel);
		list.add(stepValueLabel);
		list.add(tSeparator);
		list.add(angleLabel);
		list.add(angleField);
		list.add(xSeparator);
		list.add(xLabel);
		list.add(xField);
		list.add(ySeparator);
		list.add(yLabel);
		list.add(yField);
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
		if (!(panel instanceof TrackerPanel) || !isVisible())
			return null;
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		int n = trackerPanel.getFrameNumber();
		ProtractorStep step = (ProtractorStep) getStep(n);
		if (trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
			Interactive ia = step.findInteractive(trackerPanel, xpix, ypix);
			if (ia == null) {
				partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
				hint = TrackerRes.getString("Protractor.Hint"); //$NON-NLS-1$
				return null;
			}
			if (ia == step.vertex) {
				partName = TrackerRes.getString("Protractor.Vertex.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("Protractor.Vertex.Hint"); //$NON-NLS-1$
			} else if (ia == step.end1) {
				partName = TrackerRes.getString("Protractor.Base.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("Protractor.Base.Hint"); //$NON-NLS-1$
			} else if (ia == step.end2) {
				partName = TrackerRes.getString("Protractor.End.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("Protractor.End.Hint"); //$NON-NLS-1$
			} else if (ia == step.handle) {
				partName = TrackerRes.getString("Protractor.Handle.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("Protractor.Handle.Hint"); //$NON-NLS-1$
			} else if (ia == step.rotator) {
				partName = TrackerRes.getString("Protractor.Rotator.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("Protractor.Rotator.Hint"); //$NON-NLS-1$
			} else if (ia == this) {
				partName = TrackerRes.getString("Protractor.Readout.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("Protractor.Readout.Hint"); //$NON-NLS-1$
				trackerPanel.setMessage(getMessage());
			}
			return isLocked()? null: ia;
		}
		return null;
	}

	/**
	 * Overrides Object toString method.
	 *
	 * @return the name of this track
	 */
	@Override
	public String toString() {
		return TrackerRes.getString("Protractor.Name"); //$NON-NLS-1$
	}

	@Override
	public Map<String, NumberField[]> getNumberFields() {
		if (numberFields.isEmpty()) {
			numberFields.put(dataVariables[0], new NumberField[] { tField });
			numberFields.put(dataVariables[1], new NumberField[] { angleField, inputField });
			numberFields.put(dataVariables[2], new NumberField[] { xField }); // L1
			numberFields.put(dataVariables[3], new NumberField[] { yField }); // L2
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
		JMenuItem item = new JMenuItem();
		final boolean radians = angleField.getConversionFactor() == 1;
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tframe.setAnglesInRadians(!radians);
			}
		});
		item.setText(radians ? TrackerRes.getString("TTrack.AngleField.Popup.Degrees") : //$NON-NLS-1$
				TrackerRes.getString("TTrack.AngleField.Popup.Radians")); //$NON-NLS-1$
		popup.add(item);
		if (tp.isEnabled("number.formats")) { //$NON-NLS-1$
			popup.addSeparator();
			item = new JMenuItem();
			final String[] selected = new String[] { Tracker.THETA };
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					NumberFormatDialog.getNumberFormatDialog(tp, Protractor.this, selected)
							.setVisible(true);
				}
			});
			item.setText(TrackerRes.getString("TTrack.MenuItem.NumberFormat")); //$NON-NLS-1$
			popup.add(item);
		}
		// add "change to radians" item
		return popup;
	}

//__________________________ protected methods ________________________

	/**
	 * Overrides TTrack setTrackerPanel method.
	 *
	 * @param panel the TrackerPanel
	 */
	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null)
			removePanelEvents(panelEventsProtractor);
		super.setTrackerPanel(panel);
		if (panel != null) {
			setFixedPosition(isFixedPosition());
			addPanelEvents(panelEventsProtractor);
		}
	}

	/**
	 * Overrides TTrack method.
	 *
	 * @param radians <code>true</code> for radians, false for degrees
	 */
	@Override
	protected void setAnglesInRadians(boolean radians) {
		super.setAnglesInRadians(radians);
//    inputField.setDecimalPlaces(radians? 3: 1);
		inputField.setConversionFactor(radians ? 1.0 : 180 / Math.PI);
		Step step = getStep(tp.getFrameNumber());
		step.repaint(); // refreshes angle readout
	}

	// __________________________ InputTrack ___________________________

	@Override
	protected Ruler getRuler() {
		if (ruler == null)
			ruler = new AngleRuler(this);
		return ruler;
	}

	/**
	 * Refreshes a step by setting it equal to a keyframe step.
	 *
	 * @param step the step to refresh
	 */
	@Override
	protected void refreshStep(Step step) {
		// compare step with keyStep
		ProtractorStep p = (ProtractorStep) step;
		ProtractorStep k = (ProtractorStep) getKeyStep(p);
		boolean different = k.vertex.getX() != p.vertex.getX() || k.vertex.getY() != p.vertex.getY()
				|| k.end1.getX() != p.end1.getX() || k.end1.getY() != p.end1.getY() || k.end2.getX() != p.end2.getX()
				|| k.end2.getY() != p.end2.getY();
		// update step if needed
		if (different) {
			p.vertex.setLocation(k.vertex);
			p.end1.setLocation(k.end1);
			p.end2.setLocation(k.end2);
			p.erase();
		}
	}

	@SuppressWarnings("serial")
	@Override
	protected NumberField createInputField() {
		// create input field and panel
		return new NumberField(9) {
			@Override
			public void setFixedPattern(String pattern) {
				super.setFixedPattern(pattern);
				setMagValue();
			}
		};

	}

	@Override
	protected Rectangle getLayoutBounds(Step step) {
		return ((ProtractorStep) step).panelLayoutBounds.get(tp.getID());
	}

	@Override
	protected boolean checkKeyFrame() {
		return !editing;
	}

	@Override
	protected void endEditing(Step step, String rawText) {
		ProtractorStep p = (ProtractorStep) step;
		p.drawLayoutBounds = false;
		p.setProtractorAngle(inputField.getValue());
		inputField.setSigFigs(4);
	}

	@Override
	protected void setInputValue(Step step) {
		inputField.setValue(((ProtractorStep) step).getProtractorAngle(false));
	}

//__________________________ static methods ___________________________

	private final static String[] panelEventsProtractor = new String[] { 
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK
	};

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
			Protractor protractor = (Protractor) obj;
			// save track data
			XML.getLoader(TTrack.class).saveObject(control, obj);
			// save fixed property
			control.setValue("fixed", protractor.isFixedPosition()); //$NON-NLS-1$
			// save steps
			Step[] steps = protractor.getSteps();
			int count = steps.length;
			if (protractor.isFixedPosition())
				count = 1;
			double[][] data = new double[count][];
			for (int n = 0; n < count; n++) {
				// save only key frames
				if (steps[n] == null || !protractor.keyFrames.contains(n))
					continue;
				ProtractorStep pStep = (ProtractorStep) steps[n];
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
			control.setValue("ruler_visible", protractor.ruler != null && protractor.ruler.isVisible()); //$NON-NLS-1$
		}

		/**
		 * Creates a new object.
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return new Protractor();
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
			Protractor protractor = (Protractor) obj;
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			boolean locked = protractor.isLocked();
			protractor.setLocked(false);
			// load fixed property
			protractor.fixedPosition = control.getBoolean("fixed"); //$NON-NLS-1$
			// load step data
			protractor.keyFrames.clear();
			double[][] data = (double[][]) control.getObject("framedata"); //$NON-NLS-1$
			for (int n = 0; n < data.length; n++) {
				if (data[n] == null)
					continue;
				Step step = protractor.createStep(n, data[n][0], data[n][1], data[n][2], data[n][3]);
				ProtractorStep tapeStep = (ProtractorStep) step;
				// set vertex position
				tapeStep.vertex.setLocation(data[n][4], data[n][5]);
				tapeStep.erase();
			}
			// load ruler properties
			if (control.getPropertyNamesRaw().contains("ruler_visible")) { //$NON-NLS-1$
				protractor.getRuler().setVisible(control.getBoolean("ruler_visible"));
			}

			protractor.setLocked(locked);
			return obj;
		}
	}

}
