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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.border.Border;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;

import javajs.async.AsyncDialog;

import org.opensourcephysics.cabrillo.tracker.CircleFitterStep.DataPoint;
import org.opensourcephysics.cabrillo.tracker.CircleFitterStep.CenterPoint;
import org.opensourcephysics.controls.*;

/**
 * A CircleFitter track fits and measures circles and their centers.
 *
 * @author Douglas Brown
 */
public class CircleFitter extends TTrack {

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
		return "CircleFitter";
	}

	@Override
	public String getVarDimsImpl(String variable) {
		String[] vars = dataVariables;
		String[] names = formatVariables;
		if (names[1].equals(variable) // xy
				|| names[2].equals(variable) // R 
				|| vars[1].equals(variable)
				|| vars[2].equals(variable) 
				|| vars[3].equals(variable) 
				|| vars[6].equals(variable)
				|| vars[7].equals(variable)) {
			return "L"; //$NON-NLS-1$
		}
		if (vars[4].equals(variable) 
				|| vars[5].equals(variable) 
				|| vars[8].equals(variable)) {
			return "I"; //$NON-NLS-1$
		}
		return null;
	}
	
	protected final static int maxDataPointCount = 50;
	
	protected final static String[] dataVariables;
	protected final static String[] fieldVariables; // associated with number fields
	protected final static String[] formatVariables; // used by NumberFormatSetter
	protected final static Map<String, String[]> formatMap;
	protected final static Map<String, String> formatDescriptionMap;

	static {
		String center = TrackerRes.getString("CircleFitter.Data.Center") + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		String selected = TrackerRes.getString("TTrack.Selected.Hint") + "}"; //$NON-NLS-1$ //$NON-NLS-2$
		String points = TrackerRes.getString("CircleFitter.Data.PointCount"); //$NON-NLS-1$
		
		dataVariables = new String[] {
				"t", //$NON-NLS-1$ 0
				"x_{" + center, //$NON-NLS-1$ 1
				"y_{" + center, //$NON-NLS-1$ 2
				"R", //$NON-NLS-1$ 3
				"step", //$NON-NLS-1$ 4
				"frame", //$NON-NLS-1$ 5
				"x_{" + selected, //$NON-NLS-1$ 6
				"y_{" + selected, //$NON-NLS-1$ 7
				points, // 8
			};

		fieldVariables = new String[] {
				"t", //$NON-NLS-1$ 0
				"x_{" + center, //$NON-NLS-1$ 1
				"y_{" + center, //$NON-NLS-1$ 2
				"R", //$NON-NLS-1$ 3
				"x_{" + selected, //$NON-NLS-1$ 6
				"y_{" + selected, //$NON-NLS-1$ 7
			};

		formatVariables = new String[] { "t", "xy", "R" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// assemble format map
		formatMap = new HashMap<>();
		formatMap.put("t", new String[] {"t"});
		formatMap.put("xy", new String[] {
				"x_{" + center, //$NON-NLS-1$ 1
				"y_{" + center, //$NON-NLS-1$ 2
				"x_{" + selected, //$NON-NLS-1$ 6
				"y_{" + selected, //$NON-NLS-1$ 7
		});
		formatMap.put("R", new String[] {
				"R", //$NON-NLS-1$ 3
		});

		// assemble format description map
		formatDescriptionMap = new HashMap<String, String>();
		formatDescriptionMap.put(formatVariables[0], TrackerRes.getString("PointMass.Data.Description.0")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[1], TrackerRes.getString("CircleFitter.Description.Positions")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[2], TrackerRes.getString("CircleFitter.Label.Radius")); //$NON-NLS-1$

	}

	protected final static ArrayList<String> allVariables = createAllVariables(dataVariables, null); // no new field vars

	// instance fields
	protected boolean fixedPosition = true;
	protected JLabel clickToMarkLabel;
	protected JLabel xDataPointLabel, yDataPointLabel;
	protected NumberField xDataField, yDataField;
	protected Component xDataPointSeparator, yDataPointSeparator;
	protected JMenuItem clearPointsItem;
	protected JMenuItem setRadiusItem, setRadiusAllItem;
	protected JMenuItem originToCenterItem, originToCenterAllItem;
	protected JMenu originToCenterMenu, setRadiusMenu;
	protected JMenuItem attachmentItem;
	protected JButton pointCountButton;
	protected boolean attachToSteps = false, isRelativeFrameNumbers = false;
	protected int absoluteStart = 0, relativeStart = -2, attachmentFrameCount = 5;
	protected TTrack[] attachmentForSteps;
	protected String stepAttachmentName;
	private boolean refreshingAttachments, abortRefreshAttachments, loadingAttachments;
	public static final String PROPERTY_CIRCLEFITTER_DATAPOINT = "dataPoint";

	/**
	 * Constructs a CircleFitter.
	 */
	@SuppressWarnings("serial")
	public CircleFitter() {
		super(TYPE_CIRCLEFITTER);
		defaultColors = new Color[] { new Color(0, 140, 40) };
		// assign a default name
		setName(TrackerRes.getString("CircleFitter.New.Name")); //$NON-NLS-1$
		// set up footprint choices and color
		setFootprints(new Footprint[] { CircleFitterFootprint.getFootprint("CircleFitterFootprint.Circle4"), //$NON-NLS-1$
				CircleFitterFootprint.getFootprint("CircleFitterFootprint.Circle7"), //$NON-NLS-1$
				CircleFitterFootprint.getFootprint("CircleFitterFootprint.Circle4Bold"), //$NON-NLS-1$
				CircleFitterFootprint.getFootprint("CircleFitterFootprint.Circle7Bold"), //$NON-NLS-1$
				CircleFitterFootprint.getFootprint("CircleFitterFootprint.Circle4.PointsOnly") }); //$NON-NLS-1$
		defaultFootprint = getFootprint();
		setColor(defaultColors[0]);

		// assign default table variables
		setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("tableVar2", "2"); //$NON-NLS-1$ //$NON-NLS-2$
		// assign default plot variables
		setProperty("xVarPlot0", dataVariables[0]); //$NON-NLS-1$
		setProperty("yVarPlot0", dataVariables[3]); //$NON-NLS-1$
		setProperty("xVarPlot1", dataVariables[0]); //$NON-NLS-1$
		setProperty("yVarPlot1", dataVariables[1]); //$NON-NLS-1$
		setProperty("xVarPlot2", dataVariables[0]); //$NON-NLS-1$
		setProperty("yVarPlot2", dataVariables[2]); //$NON-NLS-1$

		// set initial hint
		partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
		hint = TrackerRes.getString("CircleFitter.Hint.Mark3"); //$NON-NLS-1$
		// initialize the autofill step array
		CircleFitterStep step = new CircleFitterStep(this, 0);
		step.setFootprint(getFootprint());
		steps = new StepArray(step); // autofills
		keyFrames.add(0);
		fixedItem = new JCheckBoxMenuItem(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
		fixedItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setFixed(fixedItem.isSelected());
			}
		});

		// create attachment dialog item
		attachmentItem = new JMenuItem(TrackerRes.getString("MeasuringTool.MenuItem.Attach")); //$NON-NLS-1$
		attachmentItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttachmentDialog control = tp.getAttachmentDialog(CircleFitter.this);
				control.setVisible(true);
			}
		});

		clickToMarkLabel = new JLabel();
		clickToMarkLabel.setForeground(Color.red.darker());

		// create actions, listeners, labels and fields for data points
		final Action dataPointAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tp == null)
					return;
				if (e != null && e.getSource() == xDataField && xDataField.getBackground() != Color.yellow)
					return;
				if (e != null && e.getSource() == yDataField && yDataField.getBackground() != Color.yellow)
					return;
				TPoint p = tp.getSelectedPoint();
				if (!(p instanceof DataPoint))
					return;
				if (p.isAttached())
					return;

				double xValue = xDataField.getValue();
				double yValue = yDataField.getValue();
				int n = tp.getFrameNumber();
				ImageCoordSystem coords = tp.getCoords();
				double x = coords.worldToImageX(n, xValue, yValue);
				double y = coords.worldToImageY(n, xValue, yValue);
				p.setXY(x, y);
				p.showCoordinates(tp);
				if (e != null && e.getSource() instanceof NumberField) {
					((NumberField) e.getSource()).requestFocusInWindow();
				}
			}
		};
		// focus listener
		FocusListener dataFieldFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.getSource() == xDataField && xDataField.getBackground() != Color.yellow)
					return;
				if (e.getSource() == yDataField && yDataField.getBackground() != Color.yellow)
					return;
				dataPointAction.actionPerformed(null);
			}
		};
		xDataPointLabel = new JLabel();
		xDataPointLabel.setBorder(tLabel.getBorder());
		xDataField = new NumberField(5);
		xDataField.setBorder(fieldBorder);
		xDataField.addActionListener(dataPointAction);
		xDataField.addFocusListener(dataFieldFocusListener);
		xDataField.addMouseListener(formatMouseListener);
		yDataPointLabel = new JLabel();
		yDataPointLabel.setBorder(tLabel.getBorder());
		yDataField = new NumberField(5);
		yDataField.setBorder(fieldBorder);
		yDataField.addActionListener(dataPointAction);
		yDataField.addFocusListener(dataFieldFocusListener);
		yDataField.addMouseListener(formatMouseListener);
		xDataPointSeparator = Box.createRigidArea(new Dimension(4, 4));
		yDataPointSeparator = Box.createRigidArea(new Dimension(4, 4));

		xLabel.setText("center x"); //$NON-NLS-1$
		xField.setPatterns(new String[] { "0.000E0", "0.000", "0.00", "0.0", "0.000E0" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		xField.setEnabled(false);
		yField.setPatterns(new String[] { "0.000E0", "0.000", "0.00", "0.0", "0.000E0" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		yField.setEnabled(false);
		magField.setPatterns(new String[] { "0.000E0", "0.000", "0.00", "0.0", "0.000E0" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		magField.setEnabled(false);
		xDataField.setPatterns(new String[] { "0.000E0", "0.000", "0.00", "0.0", "0.000E0" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		yDataField.setPatterns(new String[] { "0.000E0", "0.000", "0.00", "0.0", "0.000E0" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		// originToCenter menu and menuitems
		originToCenterMenu = new JMenu();
		originToCenterItem = new JMenuItem();
		originToCenterItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCoordsOriginToCenter(false);
			}
		});
		originToCenterAllItem = new JMenuItem();
		originToCenterAllItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCoordsOriginToCenter(true);
			}
		});
		
		// calibrateRadius menu and menuitems
		setRadiusMenu = new JMenu();
		setRadiusItem = new JMenuItem();
		setRadiusItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCoordsScaleFromRadius(false);
			}
		});
		setRadiusAllItem = new JMenuItem();
		setRadiusAllItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCoordsScaleFromRadius(true);
			}
		});

		// clearPoints item
		clearPointsItem = new JMenuItem();
		clearPointsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				XMLControl control = new XMLControlElement(CircleFitter.this);
				boolean changed = false;
				for (Step step : getSteps()) {
					CircleFitterStep next = (CircleFitterStep) step;
					if (next.dataPoints.length == 0)
						continue;
					changed = true;
					next.dataPoints = new DataPoint[2][0];
					next.refreshCircle();
				}
				if (changed) {
					repaint();
					invalidateData(this);
					if (tp != null) {
						tp.changed = true;
						tp.refreshTrackBar();
					}
					//TTrackBar.getTrackbar(trackerPanel).refresh();
					Undo.postTrackEdit(CircleFitter.this, control);
				}
			}
		});

		// pointCountButton
		pointCountButton = new TButton() {
			@Override
			protected JPopupMenu getPopup() {
				// get and save currently selected TPoint to restore if needed
				final TPoint selected = tp.getSelectedPoint();
				JPopupMenu popup = new JPopupMenu();
				// add item for each data point, both marked and attached
				int n = tp.getFrameNumber();
				CircleFitterStep step = (CircleFitterStep) getStep(n);
				final ArrayList<DataPoint> pts = step.getValidDataPoints();
				VideoClip clip = tp.getPlayer().getVideoClip();
				ActionListener selector = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int i = Integer.parseInt(e.getActionCommand());
						DataPoint p = pts.get(i);
						tp.setSelectedPoint(p);
					}
				};
				// create string buffer to copy data point positions to clipboard
				final StringBuffer buf = new StringBuffer();
				buf.append(step.getTrack().getName() + "_" + stepLabel.getText() + "_" + tp.getStepNumber()); //$NON-NLS-1$ //$NON-NLS-2$
				buf.append(XML.NEW_LINE);
				buf.append("x" + TrackerIO.getDelimiter() + "y"); //$NON-NLS-1$ //$NON-NLS-2$
				buf.append(XML.NEW_LINE);
				for (int i = 0; i < pts.size(); i++) {
					DataPoint p = pts.get(i);
					Point2D worldPt = p.getWorldPosition(tp);

					// add formatted data to the buffer for the clipboard
					DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
					nf.applyPattern("0.000000E0"); //$NON-NLS-1$
					nf.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
					String formattedX = nf.format(worldPt.getX());
					String formattedY = nf.format(worldPt.getY());
					buf.append(formattedX + TrackerIO.getDelimiter() + formattedY);
					if (i < pts.size() - 1) {
						buf.append(XML.NEW_LINE);
					}

					// create point description string
					String s = ""; //$NON-NLS-1$

					// first add marked point items
					if (i < step.dataPoints[0].length) {
						s = TrackerRes.getString("CircleFitter.MenuItem.MarkedPoint"); //$NON-NLS-1$
					}
					// then add attached points
					else if (attachments != null) {
						Step targetStep = p.getAttachedStep();
						// show name of attached track
						s = targetStep.getTrack().getName() + " "; //$NON-NLS-1$
						s += TrackerRes.getString("TTrack.Label.Step") + " "; //$NON-NLS-1$ //$NON-NLS-2$
						// add step number
						int frame = targetStep.getFrameNumber();
						s += clip.frameToStep(frame);
					}
					// add the coordinates after the point description

					s += " (" + xDataField.format(worldPt.getX()) //$NON-NLS-1$
							+ ", " + yDataField.format(worldPt.getY()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					JMenuItem item = new JMenuItem(s);
					item.setActionCommand(String.valueOf(i));
					item.addActionListener(selector);
					final int index = i;
					item.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseEntered(MouseEvent e) {
							DataPoint p = pts.get(index);
							tp.setSelectedPoint(p);
						}

						@Override
						public void mouseExited(MouseEvent e) {
							tp.setSelectedPoint(selected);
						}
					});
					popup.add(item);
				}
				// add "copy to clipboard" menu item
				JMenuItem item = new JMenuItem(TrackerRes.getString("CircleFitter.MenuItem.CopyToClipboard.Text")); //$NON-NLS-1$
				item.setToolTipText(TrackerRes.getString("CircleFitter.MenuItem.CopyToClipboard.Tooltip")); //$NON-NLS-1$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// copy buffer to the clipboard
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						StringSelection stringSelection = new StringSelection(buf.toString());
						clipboard.setContents(stringSelection, stringSelection);
					}
				});
				popup.addSeparator();
				popup.add(item);

				FontSizer.setFonts(popup, FontSizer.getLevel());
				return popup;
			}
		};
		pointCountButton.setOpaque(false);
		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
		Border line = BorderFactory.createLineBorder(Color.GRAY);
		pointCountButton.setBorder(BorderFactory.createCompoundBorder(line, space));
	}

	/**
	 * Sets the fixed property. When fixed, it has the same data points at all
	 * times.
	 *
	 * @param fixed <code>true</code> to fix
	 */
	public void setFixed(boolean fixed) {
		if (fixedPosition == fixed)
			return;
		XMLControl control = new XMLControlElement(this);
		if (tp != null) {
			tp.changed = true;
			int n = tp.getFrameNumber();
			CircleFitterStep source = (CircleFitterStep) getStep(n);
			CircleFitterStep target = (CircleFitterStep) getStep(0);
			target.copy(source);
			TFrame.repaintT(tp);
		}
		fixedPosition = fixed;
		if (fixed) { // refresh data and post undo only when fixing
			keyFrames.clear();
			keyFrames.add(0);
			invalidateData(null);
			Undo.postTrackEdit(this, control);
		}
	}

	/**
	 * Gets the fixed property.
	 *
	 * @return <code>true</code> if fixed
	 */
	public boolean isFixed() {
		return fixedPosition;
	}

	/**
	 * Determines if a valid circle exists at a specified frame number.
	 *
	 * @return <code>true</code> if valid
	 */
	public boolean isValidCircle(int frame) {
		CircleFitterStep step = (CircleFitterStep) getStep(frame);
		return step != null && step.isValidCircle();
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		// events are via TrackerPanel
		boolean isSelectedTrack = (tp.getSelectedTrack() == this);
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
			if (isSelectedTrack) {
				tp.refreshTrackBar();
//					TTrackBar.getTrackbar(trackerPanel).refresh();
			}
		break;
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
			if (isSelectedTrack) {
					refreshFields(tp.getFrameNumber());
			}
			break;
		case VideoClip.PROPERTY_VIDEOCLIP_STARTFRAME:
		case VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT:
		case VideoClip.PROPERTY_VIDEOCLIP_STEPSIZE:
		case PROPERTY_TTRACK_STEP:
		case PROPERTY_TTRACK_STEPS:
			if (attachments != null) {
				refreshAttachments();
			}
			break;
		case Trackable.PROPERTY_ADJUSTING: // VideoClip
			refreshDataLater = (Boolean) e.getNewValue();
			if (!refreshDataLater) { // stopped adjusting
				firePropertyChange(PROPERTY_TTRACK_DATA, null, null);
			}
			break;
		}
		super.propertyChange(e);
	}

	@Override
	public void setTrailVisible(boolean visible) {
		/** empty block */
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
		if (!isFixed()) {
			keyFrames.add(n);
			CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
			step.addDataPoint(step.new DataPoint(x, y), true);
			return step;
		}
		keyFrames.add(0);
		CircleFitterStep step = (CircleFitterStep) steps.getStep(0);
		step.addDataPoint(step.new DataPoint(x, y), true);

		return getStep(n);
	}

	/**
	 * Overrides TTrack deleteStep method to delete selected data points.
	 *
	 * @param n the frame number
	 * @return null since the step itself is never deleted
	 */
	@Override
	public Step deleteStep(int n) {
		if (isLocked()) {
			return null;
		}
		TPoint p = tp.getSelectedPoint();
		if (p != null && !(p instanceof DataPoint))
			return null;

		DataPoint data = (DataPoint) p; // may be null?
		CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
		if (!isFixed()) {
			step.removeDataPoint(data, true, true);
		} else { // fixed, so delete corresponding data point in step 0
					// find index of p
			int row = -1, column = 0;
			for (int i = 0; i < step.dataPoints.length; i++) {
				DataPoint[] points = step.dataPoints[i];
				for (int j = 0; j < points.length; j++) {
					if (data == points[j]) {
						column = i;
						row = j;
						break;
					}
				}
			}
			if (row > -1) {
				step = (CircleFitterStep) steps.getStep(0);
				data = step.dataPoints[column][row];
				step.removeDataPoint(data, true, true);
			}
		}
		return null;
	}

	@Override
	public Step getStep(int n) {
		CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
		refreshStep(step);
		return step;
	}

	@Override
	public Step getStep(TPoint point, TrackerPanel trackerPanel) {
		if (point == null)
			return null;
		Step[] stepArray = steps.array;
		for (Step step : stepArray) {
			if (step == null)
				continue;
			TPoint[] points = step.getPoints();
			for (int i = 0; i < points.length; i++) {
				if (points[i] == point)
					return step;
			}
			CircleFitterStep circleStep = (CircleFitterStep) step;
			for (DataPoint[] pts : circleStep.dataPoints) {
				for (DataPoint p : pts) {
					if (p == point)
						return step;
				}
			}
		}
		return null;
	}

	@Override
	public int getStepLength() {
		return CircleFitterStep.getLength();
	}

	@Override
	public int getFootprintLength() {
		return 3;
	}

	@Override
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		Object[] objectsToSize = new Object[] { clickToMarkLabel, xDataPointLabel, yDataPointLabel, pointCountButton,
				xDataField, yDataField };
		FontSizer.setFonts(objectsToSize, level);
	}

	@Override
	public void dispose() {
		for (TTrack t : TTrack.getValues()) {
			t.removeStepListener(this);
		}
		if (attachmentForSteps != null) {
			for (int i = 0; i < attachmentForSteps.length; i++) {
				attachmentForSteps[i] = null;
			}
		}
		if (attachments != null) {
			for (int i = 0; i < attachments.length; i++) {
				attachments[i] = null;
			}
		}
		// remove attachments from all dataPoints[1] of each step
		Step[] steps = getSteps();
		for (Step next : steps) {
			if (next == null)
				continue;
			CircleFitterStep step = (CircleFitterStep) next;
			for (int i = 0; i <= step.dataPoints[1].length; i++) {
				DataPoint p = step.getDataPoint(1, i); // may return null
				if (p != null) {
					p.detach();
				}
			}
		}
		attachmentNames = null;
		properties.clear();
		datasetManager = null;
		setTrackerPanel(null);
	}

	/**
	 * Determines if this is attached to one or more tracks.
	 *
	 * @return true if attached
	 */
	public boolean isNoPoints(int frameNumber) {
		if (!isRelativeFrameNumbers)
			return false;
		int start = frameNumber + relativeStart;
		int end = frameNumber + relativeStart + attachmentFrameCount - 1;
		return end < 0 || start > tp.getPlayer().getVideoClip().getLastFrameNumber();
	}

	/**
	 * Sets the start frame for single track attachments.
	 *
	 * @param n the desired start frame
	 */
	public void setAttachmentStartFrame(int n) {
//  	int min = 0;
//  	int max = trackerPanel.getPlayer().getVideoClip().getFrameCount()-1;
		if (isRelativeFrameNumbers) {
			int count = tp.getPlayer().getVideoClip().getFrameCount();
			n = Math.max(n, 1 - count);
			n = Math.min(n, count - 1);
			relativeStart = n;
		} else { // absolute frame numbers
			int min = tp.getPlayer().getVideoClip().getFirstFrameNumber();
			int max = tp.getPlayer().getVideoClip().getLastFrameNumber();
			n = Math.max(n, min);
			n = Math.min(n, max);
			absoluteStart = n;
		}
	}

	/**
	 * Gets the start frame for single track attachments.
	 * 
	 * @param frameNumber the frame number
	 * @return the start frame
	 */
	public int getAttachmentStartFrame(int frameNumber) {
		if (isRelativeFrameNumbers) {
			int n = Math.max(0, frameNumber + relativeStart); // not less than first frame
			n = Math.min(n, tp.getPlayer().getVideoClip().getLastFrameNumber()); // not more than last frame
			return n;
		}
		return absoluteStart;
	}

	/**
	 * Sets the attachment frame count for single track attachments.
	 *
	 * @param n the desired frame count
	 */
	public void setAttachmentFrameCount(int n) {
		n = Math.min(n, maxDataPointCount);
		n = Math.max(n, 1);
		attachmentFrameCount = n;
	}

	/**
	 * Gets the attachment frame count.
	 *
	 * @return the frame count
	 */
	public int getAttachmentFrameCount() {
		return attachmentFrameCount;
	}

	/**
	 * Gets the end frame for single track attachments.
	 *
	 * @param frameNumber the current frame number
	 * @return the end frame
	 */
	public int getAttachmentEndFrame(int frameNumber) {
		int n = Math.max(0, absoluteStart + attachmentFrameCount - 1);
		if (isRelativeFrameNumbers) {
			n = Math.max(0, frameNumber + relativeStart + attachmentFrameCount - 1);
		}
		n = Math.min(n, tp.getPlayer().getVideoClip().getLastFrameNumber()); // not more than last frame
		return n;
	}

	@Override
	protected int getAttachmentLength() {
		// Variable number here. 
		return (attachments == null || attachments.length == 0 || attachToSteps ? 1 : attachments.length);
	}
	
	@Override
	public TTrack[] getAttachments() {
		// check existing attachments array and modify if necessary
		super.getAttachments();
		if (attachToSteps) {
			if (attachmentForSteps == null) {
				attachmentForSteps = new TTrack[] { attachments[0] };
			}
			return attachmentForSteps;
		}
		boolean ready = true;
		// Check that all attachments are non-null except the last, 
		// and that the last is null
		for (int i = attachments.length, inew = i - 1; --i >= 0;) {
			if ((i < inew) == (attachments[i] == null)) {
				ready = false;
				break;
			}
		}
		if (ready)
			return attachments;

		// eliminate null attachments
		for (int i = attachments.length; --i >= 0;) {
			if (attachments[i] == null) {
				TTrack[] newAttachments = new TTrack[attachments.length - 1];
				System.arraycopy(attachments, 0, newAttachments, 0, i);
				System.arraycopy(attachments, i + 1, newAttachments, i, attachments.length - i - 1);
				attachments = newAttachments;
			}
		}

		// include "new" element if not attached to steps
		if (attachments.length == 0) {
			attachments = new TTrack[1];
		} else if (!attachToSteps) {
			TTrack[] newAttachments = new TTrack[attachments.length + 1];
			System.arraycopy(attachments, 0, newAttachments, 0, attachments.length);
			attachments = newAttachments;
		}
		return attachments;
	}

	/**
	 * Returns the description of a particular attachment point.
	 * 
	 * @param n the attachment point index
	 * @return the description
	 */
	@Override
	public String getAttachmentDescription(int n) {
		if (attachToSteps) {
			// attaching to steps
			return attachmentForSteps[0] == null ? // "new" when attachment is null
					TrackerRes.getString("CircleFitter.Label.NewPoint") : //$NON-NLS-1$
					TrackerRes.getString("CircleFitter.Label.Points"); //$NON-NLS-1$
		}
		return n == attachments.length - 1 ? // last row is always "new"
				TrackerRes.getString("CircleFitter.Label.NewPoint") : //$NON-NLS-1$
				TrackerRes.getString("CircleFitter.Label.Point"); //$NON-NLS-1$

	}

	/**
	 * Refreshes the attachments for this track. 
	 *This manages attached data points
	 * (dataPoints[1]) and ignores marked data points (dataPoints[0])
	 */
	@Override
	protected void refreshAttachments() {
		if (refreshingAttachments) {
			abortRefreshAttachments = true;
			while (refreshingAttachments) {
				// wait for current thread to abort/finish
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Runnable runner = new Runnable() {
			@Override
			public void run() {
				refreshAttachmentsAync();
			}
		};

		new Thread(runner).start();
	}

	
	
	protected void refreshAttachmentsAync() {
		abortRefreshAttachments = false;
		refreshingAttachments = true;
		TTrack[] attachments = getAttachments();

		// look for non-null attachments and if so set fixed to false
		boolean hasAttachments = false;
		for (int i = 0; i < attachments.length; i++) {
			if (attachments[i] != null) {
				hasAttachments = true;
				// refresh listeners
				attachments[i].removeStepListener(this);
				attachments[i].addStepListener(this);
							}
		}
		if (hasAttachments) {
			boolean change = tp.changed;
			setFixed(false);
			tp.changed = change;
		}

		VideoClip clip = tp.getPlayer().getVideoClip();
		CircleFitterStep.doRefresh = false;
		TreeSet<Integer> framesToRefresh = new TreeSet<Integer>();
		if (!attachToSteps) {
			for (int n = clip.getStartFrameNumber(); n <= clip.getEndFrameNumber(); n++) {
				if (abortRefreshAttachments) {
					refreshingAttachments = false;
					return;
				}
				CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
				if (step.trimAttachedPointsToLength(attachments.length - 1)) {
					framesToRefresh.add(n);
				}
			}

			// each CircleFitter step attaches to same-frame step in attachment tracks
			for (int i = 0; i < attachments.length; i++) {
				TTrack targetTrack = attachments[i];
				if (targetTrack != null) {
					// attach/detach points to each CircleFitter step
					for (int n = clip.getStartFrameNumber(); n <= clip.getEndFrameNumber(); n++) {
						if (abortRefreshAttachments) {
							refreshingAttachments = false;
							return;
						}
						Step targetStep = targetTrack.getStep(n);
						CircleFitterStep step = (CircleFitterStep) steps.getStep(n); // no refresh
						DataPoint p = step.getDataPoint(1, i); // may return null
						if (targetStep == null) {
							// target step is null, so detach any data point and set null CircleFitterStep
							// array element
							if (p != null) {
								p.detach();
								p = null;
								step.setDataPoint(null, 1, i, false, false); // element set to mull, not
																				// compacted
								framesToRefresh.add(n);
							}
						} else {
							// targetStep exists, so attach data point to target position and set
							// CircleFitterStep array element
							TPoint target = targetStep.getPoints()[0];
							if (p == null) {
								p = step.new DataPoint(target.x, target.y);
								step.setDataPoint(p, 1, i, false, false);
							}
							if (p.attachTo(target)) { // returns true if a change has occurred
								framesToRefresh.add(n);
							}
						}
					}
				} else { // target track is null so detach all data points and eliminate array elements
					for (int n = clip.getStartFrameNumber(); n <= clip.getEndFrameNumber(); n++) {
						if (abortRefreshAttachments) {
							refreshingAttachments = false;
							return;
						}
						CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
						DataPoint p = step.getDataPoint(1, i); // may return null
						if (p != null) {
							p.detach();
							step.setDataPoint(null, 1, i, false, true); // array is compacted (null element
																		// eliminated)
							framesToRefresh.add(n);
						}
					}
				}
			}
		} else {
			// each CircleFitter step attaches to multiple steps in a single track
			TTrack targetTrack = attachments[0];
			if (targetTrack != null) {
				// target track not null so check DataPoints[1] array
				// if relative frame numbers, do every step in the clip
				// otherwise, do only step 0 and let refreshStep() do the rest (faster?)
				int stepCount = isRelativeFrameNumbers ? clip.getStepCount() : 0;
				for (int stepNum = 0; stepNum <= stepCount; stepNum++) {
					if (abortRefreshAttachments) {
						refreshingAttachments = false;
						return;
					}
					int n = clip.stepToFrame(stepNum);
					CircleFitterStep circleStep = (CircleFitterStep) steps.getStep(n);
					// in and out frames for attachments
					int in = getAttachmentStartFrame(n);
					int out = getAttachmentEndFrame(n);
					boolean noPoints = in < out ? false : isNoPoints(n);

					// check candidate target steps at every existing frame from in to out
					for (int frame = in; frame <= out; frame++) {
						if (abortRefreshAttachments) {
							refreshingAttachments = false;
							return;
						}
						Step targetStep = targetTrack.getStep(frame);
						int dataPointIndex = frame - in;
						DataPoint p = circleStep.getDataPoint(1, dataPointIndex); // may be null

						if (targetStep == null || noPoints) {
							// target step is null, so detach any data point and set null array element
							if (p != null) {
								p.detach();
								p = null;
								framesToRefresh.add(n);
							}
						} else {
							// targetStep not null, so attach data point to target and set CircleFitterStep
							// array element
							TPoint target = targetStep.getPoints()[0];
							if (p == null) {
								p = circleStep.new DataPoint(target.x, target.y);
							}
							if (p.attachTo(target)) { // returns true if a change has occurred
								framesToRefresh.add(n);
							}
						}
						circleStep.setDataPoint(p, 1, dataPointIndex, false, false); // sets data point, keeps
																						// null elements
					}

					// truncate dataPoints[1] array if needed
					DataPoint[] existingPts = circleStep.dataPoints[1];
					if (existingPts.length > out - in + 1) {
						DataPoint[] newPts = new DataPoint[out - in + 1];
						System.arraycopy(existingPts, 0, newPts, 0, newPts.length);
						circleStep.dataPoints[1] = newPts;
						for (int k = newPts.length; k < existingPts.length; k++) {
							if (existingPts[k] != null) {
								framesToRefresh.add(n);
							}
						}
					}

				}
			} else { // target track is null so detach all data points and compact the array
				for (int stepNum = 0; stepNum <= clip.getStepCount(); stepNum++) {
					if (abortRefreshAttachments) {
						refreshingAttachments = false;
						return;
					}
					int n = clip.stepToFrame(stepNum);
					CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
					for (int i = 0; i <= step.dataPoints[1].length; i++) {
						DataPoint p = step.getDataPoint(1, i); // may return null
						if (p != null) {
							p.detach();
							framesToRefresh.add(n);
						}
					}
					step.dataPoints[1] = new DataPoint[0]; // reset
				}
			}
		}

		// refresh circles
		CircleFitterStep.doRefresh = true;

		for (int n : framesToRefresh) {
			if (abortRefreshAttachments) {
				refreshingAttachments = false;
				return;
			}
			CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
			step.refreshCircle();
		}
		tp.refreshTrackBar();
		//TTrackBar.getTrackbar(trackerPanel).refresh();
		erase();
		invalidateData(this);
		refreshingAttachments = false;
		if (loadingAttachments) {
			tp.changed = false;
			loadingAttachments = false;
		}
	}

	@Override
	public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu0) {
		JMenu menu = super.getMenu(trackerPanel, menu0);
		if (menu0 == null)
			return menu;

		originToCenterItem.setText(TrackerRes.getString("CircleFitter.MenuItem.OriginToCenter")); //$NON-NLS-1$
		originToCenterMenu.setText(TrackerRes.getString("CircleFitter.MenuItem.OriginToCenter")); //$NON-NLS-1$
		originToCenterAllItem.setText(TrackerRes.getString("CircleFitter.MenuItem.AllFrames")); //$NON-NLS-1$
		setRadiusItem.setText(TrackerRes.getString("CircleFitter.Dialog.SetRadius.Title") + "..."); //$NON-NLS-1$
		setRadiusMenu.setText(TrackerRes.getString("CircleFitter.Dialog.SetRadius.Title")); //$NON-NLS-1$
		setRadiusAllItem.setText(TrackerRes.getString("CircleFitter.MenuItem.AllFrames")); //$NON-NLS-1$
		deleteStepItem.setText(TrackerRes.getString("CircleFitter.MenuItem.DeletePoint")); //$NON-NLS-1$
		clearPointsItem.setText(TrackerRes.getString("CircleFitter.MenuItem.ClearPoints")); //$NON-NLS-1$
		clearPointsItem.setEnabled(!isLocked());
		fixedItem.setText(TrackerRes.getString("TapeMeasure.MenuItem.Fixed")); //$NON-NLS-1$
		fixedItem.setSelected(isFixed());
		fixedItem.setEnabled(attachments == null || !isAttached());

		// add attachment item and separator at beginning
		menu.insert(attachmentItem, 0);
		menu.insertSeparator(1);

		// put fixed item after locked item
		addFixedItem(menu);

		// remove end items and add new items
		removeDeleteTrackItem(menu);

		// add originToCenter and setRadius items and/or menus below inspector item
		menu.addSeparator();
		boolean fixedOrigin = trackerPanel.getCoords().isFixedOrigin();
		boolean fixedScale = trackerPanel.getCoords().isFixedScale();	
		CircleFitterStep step = (CircleFitterStep) getStep(trackerPanel.getFrameNumber());
		boolean valid = step != null && step.isValidCircle();
		if (fixedOrigin) {
			originToCenterItem.setEnabled(!trackerPanel.getCoords().isLocked() && valid);
			menu.add(originToCenterItem);
		} else {
			originToCenterMenu.setEnabled(!trackerPanel.getCoords().isLocked() && valid);
			menu.add(originToCenterMenu);
			originToCenterItem.setText(TrackerRes.getString("CircleFitter.MenuItem.ThisFrame")); //$NON-NLS-1$
			originToCenterMenu.add(originToCenterItem);
			originToCenterMenu.add(originToCenterAllItem);					
		}
		if (fixedScale) {
			setRadiusItem.setEnabled(!trackerPanel.getCoords().isLocked() && valid);
			menu.add(setRadiusItem);
		} else {
			setRadiusMenu.setEnabled(!trackerPanel.getCoords().isLocked() && valid);
			menu.add(setRadiusMenu);
			setRadiusItem.setText(TrackerRes.getString("CircleFitter.MenuItem.ThisFrame")); //$NON-NLS-1$
			setRadiusMenu.add(setRadiusItem);
			setRadiusMenu.add(setRadiusAllItem);				
		}		
		
		menu.addSeparator();
		menu.add(deleteStepItem);
		menu.add(clearPointsItem);
		menu.add(deleteTrackItem);
		return menu;
	}

	@Override
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		// refresh fields
		int n = trackerPanel.getFrameNumber();
		refreshFields(n);

		// refresh pointCountButton
		CircleFitterStep step = (CircleFitterStep) getStep(n);
		ArrayList<DataPoint> pts = step.getValidDataPoints();
		int dataCount = pts.size();
		pointCountButton.setText(dataCount + " " + TrackerRes.getString("CircleFitter.Button.DataPoints")); //$NON-NLS-1$ //$NON-NLS-2$
		// refresh step number label
		stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
		stepValueLabel.setText(trackerPanel.getStepNumber() + ":"); //$NON-NLS-1$

		ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
		list.add(stepLabel);
		list.add(stepValueLabel);
		list.add(tSeparator);
		list.add(pointCountButton);
		list.add(stepSeparator);

		if (dataCount > 2) {
			xLabel.setText(dataVariables[1]);
			yLabel.setText(dataVariables[2]);
			xField.setToolTipText(TrackerRes.getString("CircleFitter.Field.CenterX.Tooltip")); //$NON-NLS-1$
			yField.setToolTipText(TrackerRes.getString("CircleFitter.Field.CenterY.Tooltip")); //$NON-NLS-1$
			magLabel.setText(TrackerRes.getString("CircleFitter.Label.Radius")); //$NON-NLS-1$
			magField.setToolTipText(TrackerRes.getString("CircleFitter.Field.Radius.Tooltip")); //$NON-NLS-1$
			xField.setUnits(trackerPanel.getUnits(this, dataVariables[1]));
			yField.setUnits(trackerPanel.getUnits(this, dataVariables[2]));
			magField.setUnits(trackerPanel.getUnits(this, dataVariables[3]));
			list.add(magLabel);
			list.add(magField);
			list.add(magSeparator);
			list.add(xLabel);
			list.add(xField);
			list.add(xSeparator);
			list.add(yLabel);
			list.add(yField);
			list.add(ySeparator);
		} else if (trackerPanel.getSelectedPoint() == null) {
			clickToMarkLabel.setText(TrackerRes.getString("CircleFitter.Label.MarkPoint")); //$NON-NLS-1$
			list.add(clickToMarkLabel);
		}
		return list;
	}

	@Override
	public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel, TPoint point) {
		ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
		if (!(point instanceof DataPoint)) {
			return list;
		}
		int n = trackerPanel.getFrameNumber();
		refreshFields(n);
		stepValueLabel.setText(trackerPanel.getStepNumber() + ":"); //$NON-NLS-1$
		CircleFitterStep step = (CircleFitterStep) getStep(n);
		xDataPointLabel.setText("x"); //$NON-NLS-1$
		yDataPointLabel.setText("y"); //$NON-NLS-1$
		list.add(xDataPointLabel);
		list.add(xDataField);
		list.add(xDataPointSeparator);
		list.add(yDataPointLabel);
		list.add(yDataField);
		list.add(yDataPointSeparator);
		// count valid points
		ArrayList<DataPoint> pts = step.getValidDataPoints();
		int dataCount = pts.size();
		if (dataCount < 3) {
			clickToMarkLabel.setText(TrackerRes.getString("CircleFitter.Label.MarkPoint")); //$NON-NLS-1$
			list.add(clickToMarkLabel);
		}
		return list;
	}

	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		if (!(panel instanceof TrackerPanel) || !isVisible())
			return null;
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		int n = trackerPanel.getFrameNumber();
		if (trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
			CircleFitterStep step = (CircleFitterStep) steps.getStep(n);
			Interactive ia = step.findInteractive(trackerPanel, xpix, ypix);
			if (ia == null) {
				partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
				hint = TrackerRes.getString("CircleFitter.Hint.Mark3"); //$NON-NLS-1$
				return null;
			}
			if (ia instanceof DataPoint) {
				partName = TrackerRes.getString("CircleFitter.DataPoint.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("CircleFitter.DataPoint.Hint"); //$NON-NLS-1$
			} else if (ia instanceof CenterPoint) {
				partName = TrackerRes.getString("CircleFitter.Center.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("CircleFitter.Center.Hint"); //$NON-NLS-1$
			} else if (ia == step.edge) {
				partName = TrackerRes.getString("CircleFitter.Circle.Name"); //$NON-NLS-1$
				hint = TrackerRes.getString("CircleFitter.Circle.Hint"); //$NON-NLS-1$
				ia = step.center;
			}
			return ia;
		}
		return null;
	}

	@Override
	public String toString() {
		return TrackerRes.getString("CircleFitter.Name"); //$NON-NLS-1$
	}

	@Override
	public Map<String, NumberField[]> getNumberFields() {
		numberFields.clear();
		// dataset column names set in refreshData() method
		numberFields.put(dataVariables[0], new NumberField[] { tField });
		numberFields.put(dataVariables[1], new NumberField[] { xField });
		numberFields.put(dataVariables[2], new NumberField[] { yField });
		numberFields.put(dataVariables[3], new NumberField[] { magField });
		numberFields.put(dataVariables[6], new NumberField[] { xDataField });
		numberFields.put(dataVariables[7], new NumberField[] { yDataField });
		return numberFields;
	}

//__________________________ protected methods ________________________

	private final static String[] panelEventsCircleFitter = new String[] { 
			ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, // CircleFitter
			VideoClip.PROPERTY_VIDEOCLIP_STARTFRAME, // CircleFitter
			VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT, // CircleFitter
			VideoClip.PROPERTY_VIDEOCLIP_STEPSIZE,  // CircleFitter
	};

	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null)
			removePanelEvents(panelEventsCircleFitter);
		super.setTrackerPanel(panel);
		if (tp != null) {
			addPanelEvents(panelEventsCircleFitter);
			setFixed(isFixed());
		}
	}

	@Override
	protected void setAnglesInRadians(boolean radians) {
		super.setAnglesInRadians(radians);
		CircleFitterStep step = (CircleFitterStep) getStep(tp.getFrameNumber());
		step.repaint(); // refreshes angle readout
	}

	@Override
	protected String getTargetDescription(int pointIndex) {
		if (pointIndex == 0)
			return TrackerRes.getString("Protractor.Vertex.Name"); //$NON-NLS-1$
		String s = TrackerRes.getString("Protractor.End.Name"); //$NON-NLS-1$
		return s + " " + (pointIndex); //$NON-NLS-1$
	}

	@Override
	protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
		if (refreshDataLater || trackerPanel == null || data == null)
			return;
		// get the datasets: radius, x_center, y_center, step, frame, datapoints
		// look thru steps and get data for those included in clip
		// assign column names to the datasets
//		Dataset x_center = data.getDataset(0);
//		Dataset y_center = data.getDataset(1);
//		Dataset r = data.getDataset(2);
//		Dataset dataCount = data.getDataset(3);
//		Dataset stepNum = data.getDataset(4);
//		Dataset frameNum = data.getDataset(5);
		int count = 6;
		String time = dataVariables[0];
		if (!data.getDataset(0).getColumnName(0).equals(time)) { // not yet initialized
			data.setXYColumnNames(0, time, dataVariables[1]); // x_center
			data.setXYColumnNames(1, time, dataVariables[2]); // y_center
			data.setXYColumnNames(2, time, dataVariables[3]); // r
			data.setXYColumnNames(3, time, dataVariables[8]); // dataCount   NOTE!
			data.setXYColumnNames(4, time, dataVariables[4]); // step
			data.setXYColumnNames(5, time, dataVariables[5]); // frame
		}
		VideoPlayer player = trackerPanel.getPlayer();
		VideoClip clip = player.getVideoClip();
		int len = clip.getStepCount();
		double[][] validData = new double[count + 1][len];
		dataFrames.clear();
		for (int i = 0; i < len; i++) {
			int frame = clip.stepToFrame(i);
			CircleFitterStep step = (CircleFitterStep) getStep(frame);
			step.dataVisible = true;
			double t = player.getStepTime(i) / 1000.0;
			Point2D center = step.getWorldCenter();
			validData[0][i] = center == null ? Double.NaN : center.getX();
			validData[1][i] = center == null ? Double.NaN : center.getY();
			validData[2][i] = step.getWorldRadius();
			validData[3][i] = step.getValidDataPoints().size();
			validData[4][i] = i;
			validData[5][i] = frame;
			validData[6][i] = t;
			dataFrames.add(frame);
		}
		// don't mess with these assignments
		clearColumns(data, count, null, "CircleFitter.Data.Description.", validData, len);
	}

	/**
	 * Refreshes a step by setting it equal to a keyframe step.
	 *
	 * @param step the step to refresh
	 */
	protected void refreshStep(CircleFitterStep step) {
		boolean changed = false;
		// refresh attached data points if attached to steps and not relative frame
		// numbers
		if (attachToSteps && !isRelativeFrameNumbers && isAttached()) {
			int firstFrame = tp.getPlayer().getVideoClip().stepToFrame(0);
			CircleFitterStep keyStep = (CircleFitterStep) steps.getStep(firstFrame);
			if (keyStep != step) {
				DataPoint[] keyPts = keyStep.dataPoints[1];
				DataPoint[] pts = step.dataPoints[1];
				if (keyPts.length != pts.length) {
					changed = true;
					pts = new DataPoint[keyPts.length];
					for (int i = 0; i < pts.length; i++) {
						DataPoint next = keyPts[i];
						pts[i] = next == null ? null : step.new DataPoint(next.x, next.y);
					}
					step.dataPoints[1] = pts;
				} else { // arrays have same length
					for (int i = 0; i < pts.length; i++) {
						if (keyPts[i] == null) {
							changed = changed || pts[i] != null;
							pts[i] = null;
						} else {
							if (pts[i] == null) {
								changed = true;
								pts[i] = step.new DataPoint(0, 0);
							}
							changed = changed || keyPts[i].x != pts[i].x || keyPts[i].y != pts[i].y;
							pts[i].setLocation(keyPts[i]);
						}
					}
				}
			}
		}

		// refresh user-marked points: compare step with keyStep
		CircleFitterStep keyStep = getKeyStep(step);
		if (keyStep == step) {
			if (changed) {
				step.refreshCircle();
			}
			return;
		}
		boolean different = keyStep.dataPoints.length != step.dataPoints.length;
		if (!different) {
			different = keyStep.dataPoints[0].length != step.dataPoints[0].length;
		}
		// compare locations of user-marked data points
		if (!different) {
			DataPoint[] keyPts = keyStep.dataPoints[0];
			DataPoint[] pts = step.dataPoints[0];
			for (int i = 0; i < keyPts.length; i++) {
				DataPoint p1 = keyPts[i];
				DataPoint p2 = pts[i];
				if (p1 != null && p2 != null) {
					if (p1.x != p2.x || p1.y != p2.y) {
						different = true;
						break;
					}					
				} else 	if (p1 != null || p2 != null) {
					different = true;
					break;
				}
			}
		}
		// update step if needed
		if (different) {
			step.copy(keyStep); // copies only user-marked points
		} else if (changed) {
			step.refreshCircle();
		}
	}

	/**
	 * Refreshes the toolbar fields.
	 */
	protected void refreshFields(int frameNumber) {
		if (tp == null)
			return;
		CircleFitterStep step = (CircleFitterStep) getStep(frameNumber);
		magField.setValue(step.getWorldRadius());
		Point2D worldPt = step.getWorldCenter();
		xField.setValue(worldPt == null ? Double.NaN : worldPt.getX());
		yField.setValue(worldPt == null ? Double.NaN : worldPt.getY());

		TPoint p = tp.getSelectedPoint();
		if (p instanceof DataPoint) {
			worldPt = p.getWorldPosition(tp);
			xDataField.setValue(worldPt.getX());
			yDataField.setValue(worldPt.getY());
			xDataField.setEnabled(!p.isAttached() && !isLocked());
			yDataField.setEnabled(!p.isAttached() && !isLocked());
		}
	}

	/**
	 * Returns the key step for a given step.
	 * 
	 * @param step the step
	 * @return the key step
	 */
	protected CircleFitterStep getKeyStep(CircleFitterStep step) {
		int key = 0;
		if (!this.isFixed()) {
			for (int i : keyFrames) {
				if (i <= step.n)
					key = i;
			}
		}
		return (CircleFitterStep) steps.getStep(key);
	}

	/**
	 * Loads the attachments for this track.
	 * 
	 * @param refresh true to refresh attachments after loading
	 */
	@Override
	protected boolean loadAttachmentsFromNames(boolean refresh) {
		boolean loaded = super.loadAttachmentsFromNames(false);
		if (!loaded && stepAttachmentName == null)
			return false;

		loadingAttachments = true;
		TTrack track = tp.getTrack(stepAttachmentName);
		if (track != null) {
			loaded = true;
			attachmentForSteps = new TTrack[] { track };
			stepAttachmentName = null;
		}

		if (loaded && refresh) {
			refreshAttachmentsLater();
		} else {
			loadingAttachments = false;
		}
		return loaded;
	}

	/**
	 * Sets the coordinate system origin to the circle center.
	 * @param all true to set origin in all frames, else only in the current frame
	 */
	protected void setCoordsOriginToCenter(boolean all) {
		if (tp.getCoords().isLocked()) {
			return;
		}
		XMLControl control = new XMLControlElement(tp.getCoords());
		int n = tp.getFrameNumber();
		CircleFitterStep step = (CircleFitterStep) getStep(n);
		if (step == null || !step.isValidCircle()) {
			// make no change
			return;
		}
		TPoint pt = step.center;
		
		if (tp.getCoords().isFixedOrigin()) {
			tp.getCoords().setOriginXY(0, pt.x, pt.y);
		}
		else if (all) { // apply to all frames
			// apply each keyframe position to coord system
			Step[] steps = getSteps();
			for (int i = 0; i < steps.length; i++) {
				step = (CircleFitterStep) steps[i];
				if (step != null && step.isValidCircle() && 
						(keyFrames.contains(i) || tp.getCoords().getKeyFrames().contains(i))) {
					pt = step.center;
				}
				tp.getCoords().setOriginXY(i, pt.x, pt.y);
			}				
		} 
		else { // apply to current frame only
			tp.getCoords().setOriginXY(n, pt.x, pt.y);
		}
				
		tp.getAxes().setVisible(true);
		// post undoable edit
		Undo.postCoordsEdit(tp, control);
	}

	/**
	 * Brings up a dialog to sets the coordinate system scale by setting the circle radius.
	 * @param all true to set the scale in all frames, else only in the current frame.
	 */
	protected void setCoordsScaleFromRadius(boolean all) {
		if (tp.getCoords().isLocked()) {
			return;
		}
		XMLControl control = new XMLControlElement(tp.getCoords());
		int n = tp.getFrameNumber();
		CircleFitterStep step = (CircleFitterStep) getStep(n);
		if (step == null || !step.isValidCircle()) {
			// make no change
			return;
		}
		double r = step.getWorldRadius();
		String init = magField.getText();
		if (init.contains(" ")) { // trim units
			init = init.substring(0, init.indexOf(" "));
		}
		double[] targetR = new double[] {r};
		// get desired radius
		new AsyncDialog().showInputDialog(null,
				TrackerRes.getString("CircleFitter.Dialog.SetRadius.Message"), //$NON-NLS-1$
				TrackerRes.getString("CircleFitter.Dialog.SetRadius.Title"), //$NON-NLS-1$
				JOptionPane.PLAIN_MESSAGE, null, null, init, (e) -> {
					String s = e.getActionCommand();
					if (s != null) {
						// eliminate units if any
						if (s.contains(" ")) {
							s = s.substring(0, s.indexOf(" "));
						}
						try {
							targetR[0] = Double.valueOf(s);
						} catch (NumberFormatException e1) {
							setCoordsScaleFromRadius(all);
						}
					}
				});
		if (r == targetR[0])
			return;
		
		double scale = tp.getCoords().getScaleX(n) * r / targetR[0];
		
		if (tp.getCoords().isFixedScale()) {
			tp.getCoords().setScaleXY(0, scale, scale);			
		}
		else if (all) { // apply to all frames
			// apply each keyframe radius to coord system
			Step[] steps = getSteps();
			for (int i = 0; i < steps.length; i++) {
				step = (CircleFitterStep) steps[i];
				if (step != null && step.isValidCircle() && 
						(keyFrames.contains(i) || tp.getCoords().getKeyFrames().contains(i))) {
					r = step.getWorldRadius();
					scale = tp.getCoords().getScaleX(i) * r / targetR[0];				
				}
				tp.getCoords().setScaleXY(i, scale, scale);			
			}				
		} 
		else { // apply to current frame only
			tp.getCoords().setScaleXY(n, scale, scale);			
		}
		
		// post undoable edit
		Undo.postCoordsEdit(tp, control);
	}

//__________________________ static methods ___________________________

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
			CircleFitter circleFitter = (CircleFitter) obj;
			// save track data
			XML.getLoader(TTrack.class).saveObject(control, obj);
			// save fixed property
			control.setValue("fixed", circleFitter.isFixed()); //$NON-NLS-1$
			// save steps
			Step[] steps = circleFitter.getSteps();
			ArrayList<double[]> dataList = new ArrayList<double[]>();
			for (int n = 0; n < steps.length; n++) {
				// save only key frames
				if (steps[n] == null || !circleFitter.keyFrames.contains(n))
					continue;
				CircleFitterStep step = (CircleFitterStep) steps[n];
				DataPoint[] pts = step.dataPoints[0];
				int len = pts.length;
				// data array: first element = frame number,
				// remaining elements = (x,y) user-marked data point positions
				if (len == 0) {
					dataList.add(new double[] { n });
					continue;
				}
				double[] stepData = new double[2 * len + 1];
				stepData[0] = n;
				for (int i = 0; i < len; i++) {
					DataPoint p = pts[i];
					stepData[2 * i + 1] = p.x;
					stepData[2 * i + 2] = p.y;
				}
				dataList.add(stepData);
			}
			double[][] data = dataList.toArray(new double[dataList.size()][]);
			control.setValue("framedata", data); //$NON-NLS-1$

			// save attachment data (attachments array saved by TTrack)
			if (circleFitter.attachToSteps) {
				control.setValue("attach_to_steps", true); //$NON-NLS-1$
			}
			if (circleFitter.isRelativeFrameNumbers) {
				control.setValue("relative_frames", true); //$NON-NLS-1$
			}
			control.setValue("absolute_start", circleFitter.absoluteStart); //$NON-NLS-1$
			control.setValue("attachment_framecount", circleFitter.attachmentFrameCount); //$NON-NLS-1$
			control.setValue("relative_start", circleFitter.relativeStart); //$NON-NLS-1$
			// save step attachment track name
			if (circleFitter.attachmentForSteps != null && circleFitter.attachmentForSteps.length > 0
					&& circleFitter.attachmentForSteps[0] != null) {
				control.setValue("step_attachment", circleFitter.attachmentForSteps[0].getName()); //$NON-NLS-1$
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
			return new CircleFitter();
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
			// check version
			double version = 4.91;
			XMLProperty parent = control.getParentProperty();
			while (parent != null) {
				if (parent.getPropertyName().equals("TrackerPanel") && parent instanceof XMLControl) { //$NON-NLS-1$
					XMLControl trackerControl = (XMLControl) parent;
					version = trackerControl.getDouble("version"); //$NON-NLS-1$
				}
				parent = parent.getParentProperty();
			}
			CircleFitter circleFitter = (CircleFitter) obj;
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			boolean locked = circleFitter.isLocked();
			circleFitter.setLocked(false);
			// load fixed property
			circleFitter.fixedPosition = control.getBoolean("fixed"); //$NON-NLS-1$
			// load attachment data
			circleFitter.attachToSteps = control.getBoolean("attach_to_steps"); //$NON-NLS-1$
			circleFitter.isRelativeFrameNumbers = control.getBoolean("relative_frames"); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("absolute_start")) //$NON-NLS-1$
				circleFitter.absoluteStart = control.getInt("absolute_start"); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("attachment_framecount")) //$NON-NLS-1$
				circleFitter.attachmentFrameCount = control.getInt("attachment_framecount"); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("relative_start")) //$NON-NLS-1$
				circleFitter.relativeStart = control.getInt("relative_start"); //$NON-NLS-1$
			// load step attachment track
			String name = control.getString("step_attachment"); //$NON-NLS-1$
			if (name != null) {
				circleFitter.stepAttachmentName = name;
			}

			// load step data for key frames
			circleFitter.keyFrames.clear();
			circleFitter.keyFrames.add(0);
			double[][] data = (double[][]) control.getObject("framedata"); //$NON-NLS-1$
			for (int i = 0; i < data.length; i++) {
				if (data[i] == null || data[i].length < 1)
					continue;

				// first element in the array is the frame number (as double)
				int n = (int) data[i][0];
				circleFitter.keyFrames.add(n);
				if (n > 0) {
					circleFitter.fixedPosition = false; // must be false
				}
				CircleFitterStep step = (CircleFitterStep) circleFitter.steps.getStep(n);

				// remaining elements are DataPoint (x, y) pairs
				int prevCount = step.dataPoints[0].length;
				if (data[i].length == 1) { // no data points
					step.dataPoints[0] = new DataPoint[0];
					if (prevCount > 0) {
						step.refreshCircle(); // refresh only if changed
					}
					continue;
				}

				// data saved with version 4.91 includes deprecated slider after data points
				int pointCount = version < 4.92 ? (data[i].length - 3) / 2 : (data[i].length - 1) / 2;
				DataPoint[] loadedPoints = new DataPoint[pointCount];
				for (int j = 0; j < pointCount; j++) {
					loadedPoints[j] = step.new DataPoint(data[i][2 * j + 1], data[i][2 * j + 2]);
				}
				step.dataPoints[0] = loadedPoints;

				// refresh circle
				step.refreshCircle();
			}
			circleFitter.setLocked(locked);
			circleFitter.invalidateData(circleFitter);
			return obj;
		}
	}

}
