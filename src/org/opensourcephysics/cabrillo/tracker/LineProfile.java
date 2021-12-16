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
import javax.swing.border.Border;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import javajs.async.AsyncDialog;
import org.opensourcephysics.controls.*;

/**
 * A LineProfile measures pixel brightness along a line on a video image.
 *
 * @author Douglas Brown
 */
public class LineProfile extends TTrack {

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
		return "LineProfile";
	}

	@Override
	public String getVarDimsImpl(String variable) {
		String[] vars = dataVariables;
		String[] names = formatVariables;
		if (vars[0].equals(variable) || vars[7].equals(variable)) {
			return "I"; //$NON-NLS-1$
		}
		if (names[0].equals(variable) || vars[1].equals(variable) || vars[2].equals(variable)) {
			return "L"; //$NON-NLS-1$
		}
		if (names[1].equals(variable) || names[2].equals(variable) || vars[3].equals(variable)
				|| vars[4].equals(variable) || vars[5].equals(variable) || vars[6].equals(variable)) {
			return "C"; //$NON-NLS-1$
		}
		return null;
	}

	// static constants
	/** The maximum allowed spread */
	public static final int MAX_SPREAD = 100;
	protected final static String[] dataVariables;
	protected final static String[] fieldVariables;
	protected final static String[] formatVariables;
	protected final static Map<String, String[]> formatMap;
	protected final static Map<String, String> formatDescriptionMap;

	static {
		dataVariables = new String[] { "n", "x", "y", "R", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"G", "B", "luma", "pixels" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		fieldVariables = new String[0]; // no number fields used except integer spread
		formatVariables = new String[] { "t", "xy", "RGB", "luma" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// assemble format map
		formatMap = new HashMap<>();
		formatMap.put("t", new String[] { "t" });
		formatMap.put("xy", new String[] { "x", "y" });
		formatMap.put("RGB", new String[] { "R", "G", "B" });
		formatMap.put("luma", new String[] { "luma" });

		// assemble format description map
		formatDescriptionMap = new HashMap<String, String>();
		formatDescriptionMap.put(formatVariables[0], TrackerRes.getString("PointMass.Data.Description.0")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[1], TrackerRes.getString("PointMass.Position.Name")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[2], TrackerRes.getString("LineProfile.Description.RGB")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[3], TrackerRes.getString("LineProfile.Data.Brightness")); //$NON-NLS-1$

	}

	protected final static ArrayList<String> allVariables = createAllVariables(dataVariables, null); // no field vars

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
	protected boolean showTimeData = false;
	protected int datasetIndex = -1; // positive for time data

	/**
	 * Constructs a LineProfile.
	 */
	public LineProfile() {
		super(TYPE_LINEPROFILE);
		defaultColors = new Color[] { Color.magenta };
		// assign a default name
		setName(TrackerRes.getString("LineProfile.New.Name")); //$NON-NLS-1$
		// assign default plot variables
		setProperty("highlights", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("xVarPlot0", dataVariables[1]); //$NON-NLS-1$
		setProperty("yVarPlot0", dataVariables[6]); //$NON-NLS-1$
		setProperty("pointsPlot0", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("yMinPlot0", 0.0); //$NON-NLS-1$
		setProperty("yMaxPlot0", 255.0); //$NON-NLS-1$
		// assign default table variables: x, y and luma
		setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("tableVar2", "5"); //$NON-NLS-1$ //$NON-NLS-2$
		// set up footprint choices and color
		setFootprints(new Footprint[] { LineFootprint.getFootprint("Footprint.Outline"), //$NON-NLS-1$
				LineFootprint.getFootprint("Footprint.BoldOutline") }); //$NON-NLS-1$
		defaultFootprint = getFootprint();
		setColor(defaultColors[0]);
		// set initial hint
		partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
		hint = TrackerRes.getString("LineProfile.Unmarked.Hint"); //$NON-NLS-1$
		// create toolbar components
		spreadLabel = new JLabel();
		Border empty = BorderFactory.createEmptyBorder(0, 4, 0, 2);
		spreadLabel.setBorder(empty);
		spreadField = new IntegerField(3);
		spreadField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSpread(spreadField.getIntValue());
				spreadField.setIntValue(getSpread());
				spreadField.selectAll();
				spreadField.requestFocusInWindow();
				firePropertyChange(PROPERTY_TTRACK_DATA, null, LineProfile.this); // to views //$NON-NLS-1$
			}
		});
		spreadField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				spreadField.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
				setSpread(spreadField.getIntValue());
				spreadField.setIntValue(getSpread());
				firePropertyChange(PROPERTY_TTRACK_DATA, null, LineProfile.this); // to views //$NON-NLS-1$
			}
		});
		spreadField.setBorder(fieldBorder);
		spreadField.addMouseListener(formatMouseListener);
		// create fixed line item
		fixedLineItem = new JCheckBoxMenuItem(TrackerRes.getString("LineProfile.MenuItem.Fixed")); //$NON-NLS-1$
		fixedLineItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setFixed(fixedLineItem.isSelected());
			}
		});
		// create orientation items
		orientationMenu = new JMenu(TrackerRes.getString("LineProfile.Menu.Orientation")); //$NON-NLS-1$
		ButtonGroup group = new ButtonGroup();
		horizOrientationItem = new JRadioButtonMenuItem(TrackerRes.getString("LineProfile.MenuItem.Horizontal")); //$NON-NLS-1$
		horizOrientationItem.setSelected(true);
		horizOrientationItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (tp == null)
					return;
				XMLControl control = new XMLControlElement(LineProfile.this);
				isHorizontal = horizOrientationItem.isSelected();
				if (!steps.isEmpty()) {
					int n = tp.getFrameNumber();
					LineProfileStep step = (LineProfileStep) steps.getStep(n);
					refreshStep(step);
					TFrame.repaintT(tp);
					if (!loading)
						Undo.postTrackEdit(LineProfile.this, control);
				}
				tp.getToolBar(true).refresh(TToolBar.REFRESH_LINEPROFILE);
				invalidateData(null);
			}
		});
		orientationMenu.add(horizOrientationItem);
		group.add(horizOrientationItem);
		xaxisOrientationItem = new JRadioButtonMenuItem(TrackerRes.getString("LineProfile.MenuItem.XAxis")); //$NON-NLS-1$
		orientationMenu.add(xaxisOrientationItem);
		group.add(xaxisOrientationItem);
	}

	/**
	 * Sets the fixed property. When it is fixed, it is in the same position at all
	 * times.
	 *
	 * @param fixed <code>true</code> to fix the line
	 */
	public void setFixed(boolean fixed) {
		if (fixed == fixedLine)
			return;
		ArrayList<TableTrackView> tableViews = getTableViews();
		if (fixedLine && !fixed) {
			boolean hasTimeView = false;
			for (int i = 0; i < tableViews.size(); i++) {
				hasTimeView = hasTimeView || tableViews.get(i).myDatasetIndex > -1;
			}
			if (hasTimeView) {
				boolean[] ok = new boolean[] {true};
				new AsyncDialog().showConfirmDialog(null, 
						TrackerRes.getString("TableTrackView.Dialog.TimeDataUnsupported.Message1")+"\n"+
						TrackerRes.getString("TableTrackView.Dialog.TimeDataUnsupported.Message2"),
						TrackerRes.getString("TableTrackView.Dialog.TimeDataUnsupported.Title"), JOptionPane.YES_NO_OPTION, (ev) -> {
							int sel = ev.getID();
							switch (sel) {
							case JOptionPane.YES_OPTION:
								for (int i = 0; i < tableViews.size(); i++) {
									if (tableViews.get(i).myDatasetIndex > -1)
										tableViews.get(i).lineProfileDatatypeButton.doClick(0);
								}
								break;

							case JOptionPane.NO_OPTION:
								ok[0] = false;
							}
						});
				if (!ok[0])
					return;
			}
		}
		if (steps.isEmpty()) {
			fixedLine = fixed;
		}
		else {
			XMLControl control = new XMLControlElement(this);
			fixedLine = fixed;
			if (tp != null) {
				tp.changed = true;
				int n = tp.getFrameNumber();
				Step step = getStep(n);
				if (step != null) {
					steps = new StepArray(getStep(n));
					TFrame.repaintT(tp);
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
		for (int i = 0; i < tableViews.size(); i++) {
			tableViews.get(i).refreshGUI();
		}
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
	 * Sets the spread. Spread determines how many pixels on each side of the line
	 * are given full weight in the average.
	 *
	 * @param spread the desired spread
	 */
	public void setSpread(int spread) {
		if (isLocked() || this.spread == spread)
			return;
		XMLControl control = new XMLControlElement(this);
		spread = Math.max(spread, 0);
		this.spread = Math.min(spread, MAX_SPREAD);
		if (!loading)
			Undo.postTrackEdit(this, control);
		clearStepData();
		repaint();
		invalidateData(Boolean.FALSE);
		if (tp != null)
			firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, tp.getFrameNumber()); // $NON-NLS-1$
	}

	/**
	 * Gets the spread. Spread determines how many pixels on each side of the line
	 * are given full weight in the average.
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
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		super.draw(panel, _g);
	}

	/**
	 * Overrides TTrack setTrailVisible method to keep trails hidden.
	 *
	 * @param visible ignored
	 */
	@Override
	public void setTrailVisible(boolean visible) {
		/** empty block */
	}

	/**
	 * Creates a new step.
	 *
	 * @param n the frame number
	 * @param x the x coordinate in image space
	 * @param y the y coordinate in image space
	 * @return the step
	 */
	@Override
	public Step createStep(int n, double x, double y) {
		return createStep(n, x, y, x, y);
	}

	/**
	 * Creates a new step or sets end positions of an existing step.
	 *
	 * @param n  the frame number
	 * @param x1 the x coordinate of end1 in image space
	 * @param y1 the y coordinate of end1 in image space
	 * @param x2 the x coordinate of end2 in image space
	 * @param y2 the y coordinate of end2 in image space
	 * @return the step
	 */
	public Step createStep(int n, double x1, double y1, double x2, double y2) {
		if (isLocked())
			return null;
		int frame = isFixed() ? 0 : n;
		LineProfileStep step = (LineProfileStep) steps.getStep(frame);
		if (step == null) {
			keyFrames.add(0);
			// create new step 0 and autofill array
			double xx = x2, yy = y2;
			if (x1 == x2 && y1 == y2) { // occurs when initially mouse-marked
				// make a step of length 50 for the step array to clone
				if (tp != null) {
					double theta = -tp.getCoords().getAngle(n);
					if (isHorizontal)
						theta = 0;
					xx = x1 + 50 * Math.cos(theta);
					yy = y1 + 50 * Math.sin(theta);
				} else
					xx = x1 + 50;
			}
			step = new LineProfileStep(this, 0, x1, y1, xx, yy);
			step.setFootprint(getFootprint());
			steps = new StepArray(step);
			// set location of line ends
			if (x1 == x2 && y1 == y2) { // mouse-marked step
				step = (LineProfileStep) getStep(frame);
				step.getLineEnd1().setLocation(x2, y2);
				if (tp != null) {
					step = (LineProfileStep) getStep(n);
					step.getLineEnd0().setTrackEditTrigger(false);
					tp.setSelectedPoint(step.getDefaultPoint());
				}
			}
		} else {
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
	@Override
	public Step deleteStep(int n) {
		return null;
	}

	/**
	 * Overrides TTrack getStep method to provide fixedLine behavior.
	 *
	 * @param n the frame number
	 * @return the step
	 */
	@Override
	public Step getStep(int n) {
		LineProfileStep step = (LineProfileStep) steps.getStep(n);
		refreshStep(step);
		return step;
	}

	/**
	 * Returns true if the step at the specified frame number is complete.
	 *
	 * @param n the frame number
	 * @return <code>true</code> if the step is complete, otherwise false
	 */
	@Override
	public boolean isStepComplete(int n) {
		return getStep(n) != null;
	}

	/**
	 * Gets the length of the steps created by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getStepLength() {
		return LineProfileStep.getLength();
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
	 * Implements findInteractive method.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position on the panel
	 * @param ypix  the y pixel position on the panel
	 * @return the first step or motion vector that is hit
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		if (!(panel instanceof TrackerPanel) || !isVisible() || isLocked())
			return null;
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		Interactive ia = null;
		int n = trackerPanel.getFrameNumber();
		Step step = getStep(n);
		if (step != null && trackerPanel.getPlayer().getVideoClip().includesFrame(n))
			ia = step.findInteractive(trackerPanel, xpix, ypix);
		if (ia == null) {
			partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
			if (step == null) {
				hint = TrackerRes.getString("LineProfile.Unmarked.Hint"); //$NON-NLS-1$
			} else
				hint = TrackerRes.getString("LineProfile.Hint"); //$NON-NLS-1$
			if (trackerPanel.getVideo() == null) {
				hint += ", " + TrackerRes.getString("TTrack.ImportVideo.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return null;
		}
		if (ia instanceof LineProfileStep.LineEnd) {
			partName = TrackerRes.getString("LineProfile.End.Name"); //$NON-NLS-1$
			hint = TrackerRes.getString("LineProfile.End.Hint"); //$NON-NLS-1$
		} else if (ia instanceof LineProfileStep.Handle) {
			partName = TrackerRes.getString("LineProfile.Handle.Name"); //$NON-NLS-1$
			hint = TrackerRes.getString("LineProfile.Handle.Hint"); //$NON-NLS-1$
		}
		return ia;
	}

	/**
	 * Refreshes the data to display multiple variables (columns) for all pixels (rows).
	 *
	 * @param data         the DatasetManager
	 * @param trackerPanel the tracker panel
	 */
	@Override
	protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
		if (refreshDataLater || trackerPanel == null || data == null)
			return;
		
		if (showTimeData()) {
			refreshTimeData(data, trackerPanel);
			return;
		}

		int count = 7;
		double[][] validData;
		// get data from current line profile step only if video is visible
		Video video = trackerPanel.getVideo();
		if (video == null || !video.isVisible()) {
			validData = new double[count+1][0]; // empty data
		} else {
			LineProfileStep step = (LineProfileStep) getStep(trackerPanel.getPlayer().getFrameNumber());
			if (step == null || (validData = step.getProfileData(trackerPanel)) == null) {
				if (step != null) {
					
					// that is, validData == null
					// this can happen in JavaScript
					// because the image has not been fully loaded yet.
					
					delayedUpdate(trackerPanel, this, video);
				}
				validData = new double[count+1][0]; // empty data
			}
		}

		// append the data to the data set
		clearColumns(data, count, dataVariables, "LineProfile.Data.Description.", validData, validData[0].length);
	}

	private boolean updating;

	private static void delayedUpdate(TrackerPanel panel, LineProfile profile, Video video) {
		if (!profile.updating) {
			profile.updating = true;
			SwingUtilities.invokeLater(() -> {
				// force a rebuild of the buffered image from the raw image and then notify
				// plot and table to revalidate
				video.invalidateVideoAndFilter();
				profile.invalidateData(null);
				panel.refreshTrackData(0);
				profile.updating = false;
			});
		}
	}

	@Override
	protected void clearColumns(DatasetManager data, int count, String[] dataVariables, String desc,
			double[][] validData, int len) {
		// get the x and y variables of the first dataset
		String v0 = dataVariables[0];
		// if already initialized and count unchanged, clear existing datasets
		if (data.getDataset(0).getColumnName(0).equals(v0) &&
				data.getDataset(0).getColumnName(1).equals(dataVariables[1]) &&
				data.getDatasetsRaw().size() == count) {
			for (int i = 0; i < count; i++) {
				data.clear(i);
			}
		} else {			
			// needs (re)initialization, so eliminate excess datasets, clear others and set variable xy names
			int n = data.getDatasetsRaw().size();
			for (int i = n-1; i >= count; i--) {
				if (data.getDataset(i).getClass() == Dataset.class)
					data.removeDataset(i);
			}
			for (int i = 0; i < count; i++) {
				data.clear(i);
				if (data.getDataset(i).getClass() == Dataset.class)
					data.setXYColumnNames(i, v0, dataVariables[i + 1]);
			}
		}
		// refresh the data descriptions
		dataDescriptions = new String[count + 1];
		if (showTimeData()) {
			dataDescriptions[0] = TrackerRes.getString("PointMass.Data.Description.0"); // time
			for (int i = 1; i <= count; i++) {
				dataDescriptions[i] = TrackerRes.getString(desc + (datasetIndex+1)); // $NON-NLS-1$
			}			
		}
		else {
			for (int i = 0; i <= count; i++) {
				dataDescriptions[i] = TrackerRes.getString(desc + i); // $NON-NLS-1$
			}
		}
		if (validData != null) {
			// indep var is last array in validData
			double[] t = validData[count];
			for (int i = 0; i < count; i++) {
				data.getDataset(i).append(t, validData[i], len);
			}
		}
	}

	/**
	 * Refreshes the data to display a single variable at all line positions (columns) and times (rows).
	 *
	 * @param data         the DatasetManager
	 * @param trackerPanel the tracker panel
	 */
	private void refreshTimeData(DatasetManager data, TrackerPanel trackerPanel) {
		int count = 0;
		ArrayList<double[]> collectedData = new ArrayList<double[]>();
		ArrayList<Double> times = new ArrayList<Double>(); 
		double[][] validData;
		String[] varNames = new String[] {"t", "empty"};
		// get data from line profile steps only if video is visible
		if (trackerPanel.getVideo() == null || !trackerPanel.getVideo().isVisible()) {
			validData = new double[count+1][0]; // empty data
		}
		else {
			VideoPlayer player = trackerPanel.getPlayer();
			VideoClip clip = player.getVideoClip();
			// get specified dataset index at all steps
			Step[] stepArray = getSteps();
			if (stepArray.length < trackerPanel.getFrameNumber()) {
				steps.setLength(trackerPanel.getFrameNumber() + 1);
				stepArray = getSteps();
			}
			int k = 0;
			for (int i = 0; i < stepArray.length; i++) {
				LineProfileStep step = (LineProfileStep)stepArray[i];
				if (step != null && clip.includesFrame(step.n)) {
					double[][] next = step.getProfileData(trackerPanel);
					if (next != null && next.length > datasetIndex) {
						if (k == 0)
							k = next[0].length;
						if (k != next[0].length)
							return;
						collectedData.add(next[datasetIndex]);
						int stepNumber = clip.frameToStep(i);
						double t = player.getStepTime(stepNumber) / 1000.0;
						times.add(t);
						count = next[datasetIndex].length;
					}
				}
			}
			if (collectedData.size() > 0) {
				// transpose rows and columns
				double[][] orig = collectedData.toArray(new double[collectedData.size()][count]);
				validData = new double[count+1][orig.length];
				varNames = new String[count+1];
				varNames[0] = "t";
				for (int row = 0; row < orig.length; row++) {
					for (int col = 0; col < count; col++) {
						varNames[col+1] = dataVariables[datasetIndex+1]+"_{ "+String.valueOf(col)+"}";
						validData[col][row] = orig[row][col];
					}
					validData[count][row] = times.get(row);
				}
				
			}
			else 
				validData = new double[count+1][0]; // empty data
		}
		// append the data to the data set
		clearColumns(data, count, varNames, "LineProfile.Data.Description.", validData, validData[0].length);
	}
	
	@Override
	public DatasetManager getData(TrackerPanel panel, int datasetIndex) {
		setDatasetIndex(datasetIndex);
		return getData(panel);
	}
	
	protected void setDatasetIndex(int index) {
		if (index == datasetIndex)
			return;
		datasetIndex = index;
		invalidateData(Boolean.FALSE);
	}

	protected boolean showTimeData() {
		return (datasetIndex >= 0);
	}

	protected void clearStepData() {
		Step[] steps = getSteps();
		for (int i = 0; i < steps.length; i++) {
			LineProfileStep step = (LineProfileStep) steps[i];
			if (step != null) {
				step.clearData();
			}
		}
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

		fixedLineItem.setText(TrackerRes.getString("LineProfile.MenuItem.Fixed")); //$NON-NLS-1$
		fixedLineItem.setSelected(isFixed());
		menu.remove(deleteTrackItem);
		TMenuBar.checkAddMenuSep(menu);
		menu.add(orientationMenu);
		menu.addSeparator();
		menu.add(fixedLineItem);
		// replace delete item
		if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
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
	@Override
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
		spreadLabel.setText(TrackerRes.getString("LineProfile.Label.Spread")); //$NON-NLS-1$
		list.add(spreadLabel);
		spreadField.setIntValue(getSpread());
		spreadField.setEnabled(!isLocked());
		list.add(spreadField);
		return list;
	}

	@Override
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		Object[] objectsToSize = new Object[] { spreadLabel };
		FontSizer.setFonts(objectsToSize, level);
	}

	/**
	 * Adds events for TrackerPanel.
	 * 
	 * @param panel the new TrackerPanel
	 */
	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null) {			
			tp.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, this);
		}
		super.setTrackerPanel(panel);
		if (tp != null) {
			tp.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, this);
		}
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (tp != null) {
			switch (e.getPropertyName()) {
			case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
				invalidateData(Boolean.FALSE);
				break;
			case TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE:
				clearStepData();
				invalidateData(Boolean.FALSE);
				firePropertyChange(e); // to view
				break;
			case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
				if (!steps.isEmpty()) { // $NON-NLS-1$
					int n = tp.getFrameNumber();
					LineProfileStep step = (LineProfileStep) steps.getStep(n);
					refreshStep(step);
				}
				break;
			}
		}
		super.propertyChange(e); // handled by TTrack
	}

	/**
	 * Overrides Object toString method.
	 *
	 * @return the name of this track
	 */
	@Override
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
		if (step == null)
			return;
		int key = 0;
		for (int i : keyFrames) {
			if (i <= step.n)
				key = i;
		}
		// compare step with keyStep
		LineProfileStep keyStep = (LineProfileStep) steps.getStep(key);
		boolean different = keyStep.getLineEnd0().getX() != step.getLineEnd0().getX()
				|| keyStep.getLineEnd0().getY() != step.getLineEnd0().getY()
				|| keyStep.getLineEnd1().getX() != step.getLineEnd1().getX()
				|| keyStep.getLineEnd1().getY() != step.getLineEnd1().getY();
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
		 * @param obj     the object to save
		 */
		@Override
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
			if (profile.isFixed())
				count = 1;
			FrameData[] data = new FrameData[count];
			for (int n = 0; n < count; n++) {
				// save only key frames
				if (steps[n] == null || !profile.keyFrames.contains(n))
					continue;
				data[n] = new FrameData((LineProfileStep) steps[n]);
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
		@Override
		public Object createObject(XMLControl control) {
			LineProfile profile = new LineProfile();
			return profile;
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
			LineProfile profile = (LineProfile) obj;
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			boolean locked = profile.isLocked();
			profile.setLocked(false);
			profile.loading = true;
			// load orientation
			if (control.getPropertyNamesRaw().contains("horizontal")) //$NON-NLS-1$
				profile.isHorizontal = control.getBoolean("horizontal"); //$NON-NLS-1$
			else
				profile.isHorizontal = !control.getBoolean("rotates"); //$NON-NLS-1$
			if (profile.isHorizontal)
				profile.horizOrientationItem.setSelected(true);
			else
				profile.xaxisOrientationItem.setSelected(true);
			// load spread
			int i = control.getInt("spread"); //$NON-NLS-1$
			if (i != Integer.MIN_VALUE) {
				profile.setSpread(i);
			}
			// load fixed before steps data
			if (control.getPropertyNamesRaw().contains("fixed")) //$NON-NLS-1$
				profile.fixedLine = control.getBoolean("fixed"); //$NON-NLS-1$
			// load step data
			profile.keyFrames.clear();
			FrameData[] data = (FrameData[]) control.getObject("framedata"); //$NON-NLS-1$
			if (data != null && data.length > 0) {
				if (profile.fixedLine && data[0] != null) {
					profile.createStep(0, data[0].data[0], data[0].data[1], data[0].data[2], data[0].data[3]);
				} else
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

		FrameData() {
		}

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
