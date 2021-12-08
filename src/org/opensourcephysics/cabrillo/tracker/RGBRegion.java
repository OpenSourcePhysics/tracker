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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.IntegerField;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;

/**
 * A RGBRegion measures RGB properties in a user-defined region of a video image.
 *
 * @author Douglas Brown
 */
public class RGBRegion extends TTrack {

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
		return "RGBRegion";
	}

	@Override
	public String getVarDimsImpl(String variable) {
		String[] vars = dataVariables;
		String[] names = formatVariables;
		if (names[1].equals(variable) || vars[1].equals(variable) || vars[2].equals(variable)) {
			return "L"; //$NON-NLS-1$
		}
		if (vars[7].equals(variable) || vars[8].equals(variable) || vars[9].equals(variable)) {
			return "I"; //$NON-NLS-1$
		}
		if (names[2].equals(variable) || vars[3].equals(variable) || vars[4].equals(variable)
				|| vars[5].equals(variable) || vars[6].equals(variable) || vars[10].equals(variable)
				|| vars[11].equals(variable) || vars[12].equals(variable)) {
			return "C"; //$NON-NLS-1$
		}
		return null;
	}

	// static fields
	protected final static int defaultEdgeLength = 20;
	protected final static int defaultMaxEdgeLength = 200;
	protected final static String[] dataVariables;
	protected final static String[] fieldVariables; // associated with number fields
	protected final static String[] formatVariables; // used by NumberFormatSetter
	protected final static Map<String, String[]> formatMap;
	protected final static Map<String, String> formatDescriptionMap;
	protected final static int SHAPE_ELLIPSE = 0;
	protected final static int SHAPE_RECTANGLE = 1;
	protected final static int SHAPE_POLYGON = 2;

	static {
		dataVariables = new String[] { "t", "x", "y", "R", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"G", "B", "luma", "pixels", "step", "frame", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"Rsd", "Gsd", "Bsd" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		fieldVariables = new String[] { "t", "x", "y" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		formatVariables = new String[] { "t", "xy", "RGB" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// assemble format map
		formatMap = new HashMap<>();
		formatMap.put("t", new String[] { "t" });
		formatMap.put("xy", new String[] { "x", "y" });
		formatMap.put("RGB", new String[] { "R", "G", "B", "luma", "Rsd", "Gsd", "Bsd" });

		// assemble format description map
		formatDescriptionMap = new HashMap<String, String>();
		formatDescriptionMap.put(formatVariables[0], TrackerRes.getString("PointMass.Data.Description.0")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[1], TrackerRes.getString("PointMass.Position.Name")); //$NON-NLS-1$
		formatDescriptionMap.put(formatVariables[2], TrackerRes.getString("LineProfile.Description.RGB")); //$NON-NLS-1$
//		formatDescriptionMap.put(formatVariables[3], TrackerRes.getString("LineProfile.Data.Brightness")); //$NON-NLS-1$ 

	}

	protected final static ArrayList<String> allVariables = createAllVariables(dataVariables, null); // no new field
																										// vars

	// instance fields
	protected boolean fixedPosition = true; // region has same position at all times
	protected boolean fixedShape = true; // region has same shape and size at all times
	protected JCheckBoxMenuItem fixedPositionItem, fixedShapeItem;
	protected JLabel widthLabel, heightLabel, helpLabel;
	protected TButton editPolygonButton;
	protected int maxEdgeLength = defaultMaxEdgeLength;
	protected int shapeType = SHAPE_ELLIPSE;
	protected JComboBox<String> shapeTypeDropdown;
	protected IntegerField widthField, heightField;
	protected ArrayList<RGBStep> validSteps = new ArrayList<RGBStep>();
	protected boolean dataHidden = false;
	protected boolean loading;
	protected TreeSet<Integer> shapeKeyFrames = new TreeSet<Integer>();

	/**
	 * Constructs a RGBRegion.
	 */
	public RGBRegion() {
		super(TYPE_RGBREGION);
		defaultColors = new Color[] { Color.magenta };
		// assign a default name
		setName(TrackerRes.getString("RGBRegion.New.Name")); //$NON-NLS-1$
		// assign default plot variables
		setProperty("yVarPlot0", dataVariables[6]); //$NON-NLS-1$
		setProperty("yMinPlot0", new Double(0)); //$NON-NLS-1$
		setProperty("yMaxPlot0", new Double(255)); //$NON-NLS-1$
		// assign default table variables: x, y and luma
		setProperty("tableVar0", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("tableVar1", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("tableVar2", "5"); //$NON-NLS-1$ //$NON-NLS-2$
		// set up footprint choices and color
		setFootprints(new Footprint[] { PointShapeFootprint.getFootprint("Footprint.Shape"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.BoldShape") }); //$NON-NLS-1$
		defaultFootprint = getFootprint();
		setColor(defaultColors[0]);
		// set initial hint
		partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
		hint = TrackerRes.getString("RGBRegion.Unmarked.Hint"); //$NON-NLS-1$
		// create toolbar components
		widthLabel = new JLabel();
		widthLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
		heightLabel = new JLabel("h");
		heightLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
		helpLabel = new JLabel();
		helpLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
		
		editPolygonButton = new TButton() {

			@Override
			public Dimension getMaximumSize() {
				Dimension dim = super.getMaximumSize();
				dim.height = tp.getTrackBar(true).toolbarComponentHeight;
				return dim;
			}
			
			@Override
			protected JPopupMenu getPopup() {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem backItem = new JMenuItem(TrackerRes.getString("RGBRegion.PopupMenu.Item.RemoveLast")); //$NON-NLS-1$
				backItem.addActionListener((e) -> {
					int n = tp.getFrameNumber();
					RGBStep step = (RGBStep) getStep(n);
					if (!isFixedShape() && !shapeKeyFrames.contains(n)) {
						shapeKeyFrames.add(n);
						step.rgbShape = step.polygon = step.polygon.copy();
					}
					step.polygon.removeEndPoint();
					tp.setSelectedPoint(step.position);
					step.repaint();
					tp.getTrackBar(false).refresh();
				});
				popup.add(backItem);
				JMenuItem resetItem = new JMenuItem(TrackerRes.getString("RGBRegion.PopupMenu.Item.RemoveAll")); //$NON-NLS-1$
				resetItem.addActionListener((e) -> {
					int n = tp.getFrameNumber();
					RGBStep step = (RGBStep) getStep(n);
					if (!isFixedShape() && !shapeKeyFrames.contains(n)) {
						shapeKeyFrames.add(n);
						step.rgbShape = step.polygon = step.polygon.copy();
					}
					step.polygon.startOver();
					step.repaint();
					tp.getTrackBar(false).refresh();
				});
				popup.add(resetItem);
				FontSizer.setFonts(popup, FontSizer.getLevel());
				return popup;
			}

		};
		
		// size focus listener
		FocusListener sizeFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				refreshShapeSize((IntegerField)e.getSource());
			}
		};
		ActionListener sizeActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IntegerField field = (IntegerField)e.getSource();
				refreshShapeSize(field);
				field.selectAll();
				field.requestFocusInWindow();
			}			
		};
		
		widthField = new IntegerField(2);
		widthField.setMinValue(1);
		widthField.addFocusListener(sizeFocusListener);
		widthField.addActionListener(sizeActionListener);
		widthField.addMouseListener(formatMouseListener);
		
		heightField = new IntegerField(2);
		heightField.setMinValue(1);
		heightField.addFocusListener(sizeFocusListener);
		heightField.addActionListener(sizeActionListener);
		heightField.addMouseListener(formatMouseListener);
		
		// shapeTypeDropdown
		shapeTypeDropdown = new JComboBox<String>();
		shapeTypeDropdown.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		shapeTypeDropdown.setOpaque(false);
		shapeTypeDropdown.setEditable(false);
		shapeTypeDropdown.addItem(TrackerRes.getString("RGBRegion.ShapeType.Ellipse")); //$NON-NLS-1$
		shapeTypeDropdown.addItem(TrackerRes.getString("RGBRegion.ShapeType.Rectangle")); //$NON-NLS-1$
		shapeTypeDropdown.addItem(TrackerRes.getString("RGBRegion.ShapeType.Polygon")); //$NON-NLS-1$
		shapeTypeDropdown.addActionListener((e) -> {
			setShapeType(shapeTypeDropdown.getSelectedIndex());
		});
		
		fixedPositionItem = new JCheckBoxMenuItem(TrackerRes.getString("RGBRegion.MenuItem.Fixed")); //$NON-NLS-1$
		fixedPositionItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setFixedPosition(fixedPositionItem.isSelected());
			}
		});
		fixedShapeItem = new JCheckBoxMenuItem(TrackerRes.getString("RGBRegion.MenuItem.FixedShape")); //$NON-NLS-1$
		fixedShapeItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setFixedShape(fixedShapeItem.isSelected());
			}
		});
		// position action
		Action positionAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NumberField field = (NumberField) e.getSource();
				if (field.getBackground() == Color.yellow)
					setPositionFromFields();
				field.selectAll();
				field.requestFocusInWindow();
			}
		};
		// position focus listener
		FocusListener positionFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				NumberField field = (NumberField) e.getSource();
				if (field.getBackground() == Color.yellow)
					setPositionFromFields();
			}
		};
		// add action and focus listeners
		xField.addActionListener(positionAction);
		yField.addActionListener(positionAction);
		xField.addFocusListener(positionFocusListener);
		yField.addFocusListener(positionFocusListener);
	}
	
	private void refreshShapeSize(IntegerField field) {
		if (field.getBackground() == Color.yellow) {
			setShapeSize(tp.getFrameNumber(), widthField.getIntValue(), heightField.getIntValue());						
		}

	}

	/**
	 * Sets the fixed position property. When it is fixed, it is in the same
	 * position at all times.
	 *
	 * @param fixed <code>true</code> to fix the position
	 */
	@Override
	public void setFixedPosition(boolean fixed) {
		if (fixedPosition == fixed)
			return;
		if (steps.isEmpty()) {
			fixedPosition = fixed;
			return;
		}
		XMLControl control = new XMLControlElement(this);
		if (tp != null) {
			tp.changed = true;
			int n = tp.getFrameNumber();
			RGBStep keyStep = (RGBStep) getStep(n);
			for (Step next : steps.array) {
				if (next == null)
					continue;
				RGBStep step = (RGBStep) next;
				step.getPosition().setLocation(keyStep.getPosition());
			}
		}
		fixedPosition = fixed;
		if (fixed) {
			keyFrames.clear();
			keyFrames.add(0);
			clearData();
			refreshData(datasetManager, tp);
			firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
		}
		if (!loading) {
			Undo.postTrackEdit(this, control);
		}
		repaint();
	}

	/**
	 * Gets the fixed position property.
	 *
	 * @return <code>true</code> if image position is fixed
	 */
	public boolean isFixedPosition() {
		return fixedPosition;
	}

	@Override
	protected String getTargetDescription(int pointIndex) {
		return TrackerRes.getString("PointMass.Position.Name"); //$NON-NLS-1$
	}
	
	protected void setShapeType(int type) {
		if (shapeType == type)
			return;
		XMLControl currentState = new XMLControlElement(this);
		shapeType = type;
		Undo.postTrackEdit(this, currentState);
		for (int i = 0; i < steps.array.length; i++) {
			RGBStep step = (RGBStep)steps.getStep(i);
			if (step != null) {
				step.rgbShape = null; // forces rgbShape refresh
				step.dataValid = false;
			}
		}
    Shape shape = shapeType == SHAPE_ELLIPSE?
    		new Ellipse2D.Double(-5, -5, 10, 10):
    		shapeType == SHAPE_RECTANGLE?	
    		new Rectangle(-5, -5, 10, 10):
    		new Rectangle(-5, -5, 10, 10);
    for (int i = 0; i < getFootprints().length; i++) {
    	((PointShapeFootprint) getFootprints()[i]).setShape(shape);
    }
		erase();
		if (tp != null) {
			TTrackBar trackBar = tp.getTrackBar(false);
			if (trackBar != null)
				trackBar.refresh();			
			tp.changed = true;
			tp.refreshTrackData(DataTable.MODE_REFRESH);
		}
		firePropertyChange(PROPERTY_TTRACK_FOOTPRINT, null, footprint); // $NON-NLS-1$
	}

	/**
	 * Sets the fixed shape property. When fixed, it has the same 
	 * shape and size at all times.
	 *
	 * @param fixed <code>true</code> to fix the shape
	 */
	public void setFixedShape(boolean fixed) {
		if (fixedShape == fixed)
			return;
		if (steps.isEmpty()) {
			fixedShape = fixed;
			return;
		}
		XMLControl control = new XMLControlElement(this);
		if (tp != null) {
			tp.changed = true;
			int n = tp.getFrameNumber();
			RGBStep keyStep = (RGBStep) getStep(n);
			for (Step next : steps.array) {
				if (next == null)
					continue;
				RGBStep step = (RGBStep) next;
				step.setShapeSize(keyStep.width, keyStep.height);
			}
		}
		fixedShape = fixed;
		if (fixed) {
			shapeKeyFrames.clear();
			shapeKeyFrames.add(0);
			clearData();
			refreshData(datasetManager, tp);
			firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
		}
		if (!loading) {
			Undo.postTrackEdit(this, control);
		}
		repaint();
	}

	/**
	 * Gets the fixed shape property.
	 *
	 * @return <code>true</code> if shape and size are fixed
	 */
	public boolean isFixedShape() {
		return fixedShape;
	}

	/**
	 * Sets the shape size of a step and posts an undoable edit
	 *
	 * @param n the frame number
	 * @param height the desired height
	 * @param width the desired width
	 */
	protected void setShapeSize(int n, int width, int height) {
		if (isLocked() || height == Integer.MIN_VALUE || width == Integer.MIN_VALUE || tp == null)
			return;
		width = Math.max(width, 0);
		width = Math.min(width, maxEdgeLength);
		widthField.setIntValue(width);
		height = Math.max(height, 0);
		height = Math.min(height, maxEdgeLength);
		heightField.setIntValue(height);

		RGBStep step = (RGBStep) getStep(n); // target step
		RGBStep keyStep = step; // key step is target if shape not fixed
		if (step != null && (step.height != height || step.width != width)) {
			// deselect selected point to trigger possible undo, then reselect it
			TPoint selection = tp.getSelectedPoint();
			tp.setSelectedPoint(null);
			tp.selectedSteps.clear();
			XMLControl state = new XMLControlElement(step);

			if (isFixedShape()) {
				keyStep = (RGBStep) steps.getStep(0); // key step is step 0
				clearData(); // all data is invalid
				keyStep.setShapeSize(width, height);
				refreshStep(step);
			} else {
				shapeKeyFrames.add(n); // step is both target and key
				step.setShapeSize(width, height);
				step.dataValid = false; // only target step's data is invalid
			}
			Undo.postStepEdit(step, state);
			tp.setSelectedPoint(selection);
			refreshData(datasetManager, tp);
			step.repaint();
			tp.changed = true;
//			firePropertyChange(PROPERTY_TTRACK_DATA, null, RGBRegion.this); // to views //$NON-NLS-1$
      firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, new Integer(n)); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the shape size.
	 *
	 * @return Dimension width, height
	 */
	public Dimension getShapeSize() {
		if (isFixedShape()) {
			RGBStep step = (RGBStep) getStep(0);
			if (step != null)
				return new Dimension(step.width, step.height);
		} else if (tp != null && !fixedShape) {
			int n = tp.getFrameNumber();
			RGBStep step = (RGBStep) getStep(n);
			if (step != null)
				return new Dimension(step.width, step.height);
		}
		return new Dimension(defaultEdgeLength, defaultEdgeLength);
	}

	/**
	 * Overrides TTrack draw method.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
//		if (isMarking() && !(tp.getSelectedPoint() instanceof RGBStep.Position))
//			return;
		super.draw(panel, _g);
	}

	/**
	 * Overrides TTrack findInteractive method.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position on the panel
	 * @param ypix  the y pixel position on the panel
	 * @return the first step or motion vector that is hit
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		Interactive ia = super.findInteractive(panel, xpix, ypix);
		if (ia != null) {
			partName = TrackerRes.getString("RGBRegion.Position.Name"); //$NON-NLS-1$
			hint = TrackerRes.getString("RGBRegion.Position.Hint"); //$NON-NLS-1$
		} else {
			partName = TrackerRes.getString("TTrack.Selected.Hint"); //$NON-NLS-1$
			if (getStep(tp.getFrameNumber()) == null) {
				hint = TrackerRes.getString("RGBRegion.Unmarked.Hint"); //$NON-NLS-1$
			} else
				hint = TrackerRes.getString("RGBRegion.Hint"); //$NON-NLS-1$
			if (tp.getVideo() == null) {
				hint += ", " + TrackerRes.getString("TTrack.ImportVideo.Hint"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return ia;
	}

	/**
	 * Sets the marking flag. Flag should be true when ready to be marked by user.
	 * 
	 * @param marking true when marking
	 */
	@Override
	protected void setMarking(boolean marking) {
		super.setMarking(marking);
		repaint(tp.getID());
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
	 * Gets the autoAdvance property. Overrides TTrack method.
	 *
	 * @return <code>false</code>
	 */
	@Override
	public boolean isAutoAdvance() {
		return !isFixedPosition() && !isEditingPolygon();
	}

	/**
	 * Creates a new step or moves/extends an existing one.
	 *
	 * @param n the frame number
	 * @param x the x coordinate in image space
	 * @param y the y coordinate in image space
	 * @return the step
	 */
	@Override
	public Step createStep(int n, double x, double y) {
		if (isLocked())
			return null;
		int frame = isFixedPosition() && isFixedShape()? 0 : n;
		RGBStep step = (RGBStep) steps.getStep(frame);
		if (step == null) { // create new step 0 and autofill array
			int w = widthField.getIntValue();
			int h = heightField.getIntValue();
			if (w == 0)
				w = h = defaultEdgeLength;
			step = new RGBStep(this, 0, x, y, w, h);
			step.setFootprint(getFootprint());
			steps = new StepArray(step);
			keyFrames.add(0);
			shapeKeyFrames.add(0);
		} else {
			XMLControl currentState = new XMLControlElement(this);
			// the following should occur only when marking polygons with the mouse
			if (!loading && shapeType == SHAPE_POLYGON && !step.isPolygonClosed()) {
				// if fixed shape then all share the same polygon so can append to any step
				step = (RGBStep) getStep(n); // refreshes step so polygon never null
				step.append(x, y);
				if (!isFixedShape())
					shapeKeyFrames.add(n);
			}
			else {
				// if fixed position, must set position of step 0
				if (isFixedPosition())
					((RGBStep) steps.getStep(0)).getPosition().setLocation(x, y);
				else {
					step.getPosition().setLocation(x, y);
					keyFrames.add(n);
				}
			}
			if (!loading)
				Undo.postTrackEdit(this, currentState);
		}
		firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, HINT_STEP_ADDED_OR_REMOVED, n); // $NON-NLS-1$
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
	 * Overrides TTrack getStep method to provide fixed behavior.
	 *
	 * @param n the frame number
	 * @return the step
	 */
	@Override
	public Step getStep(int n) {
		RGBStep step = (RGBStep) steps.getStep(n);
		refreshStep(step);
		return step;
	}

	/**
	 * Gets the length of the steps created by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getStepLength() {
		return RGBStep.getLength();
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
		this.setFixedPosition(false);
		return super.autoMarkAt(n, x, y);
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
	 * Gets the length of the footprints required by this track.
	 *
	 * @return the footprint length
	 */
	@Override
	public int getFootprintLength() {
		return 1;
	}

	/**
	 * Clears the data.
	 */
	protected void clearData() {
		if (datasetManager == null)
			return;
		// clear each dataset
		for (int i = 0; i < 7; i++) {
			Dataset next = datasetManager.getDataset(i);
			next.clear();
		}
		Step[] steps = getSteps();
		for (int i = 0; i < steps.length; i++) {
			if (steps[i] == null)
				continue;
			steps[i].dataVisible = false;
			((RGBStep) steps[i]).dataValid = false;
		}
	}

	/**
	 * Hides the data.
	 */
	protected void hideData() {
		Step[] steps = getSteps();
		for (int i = 0; i < steps.length; i++) {
			if (steps[i] == null)
				continue;
			steps[i].dataVisible = false;
		}
		dataHidden = true;
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
		// get valid step at current frameNumber
		int frame = trackerPanel.getFrameNumber();
		Step step = getStep(frame);
		if (step != null) {
			((RGBStep) step).getRGBData(trackerPanel);
		}
		// get the datasets
		int count = 12;
//    Dataset x = data.getDataset(count++);
//    Dataset y = data.getDataset(count++);
//    Dataset r = data.getDataset(count++);
//    Dataset g = data.getDataset(count++);
//    Dataset b = data.getDataset(count++);
//    Dataset luma = data.getDataset(count++);
//    Dataset pixels = data.getDataset(count++);
//    Dataset stepNum = data.getDataset(count++);
//    Dataset frameNum = data.getDataset(count++);
		// assign column names to the datasets
		// fill dataDescriptions array
		// look thru steps and find valid ones (data valid and included in clip)
		Step[] stepArray = getSteps();
		validSteps.clear();
		VideoPlayer player = trackerPanel.getPlayer();
		VideoClip clip = player.getVideoClip();
		for (int n = 0; n < stepArray.length; n++) {
			RGBStep next = (RGBStep) stepArray[n];
			if (next == null || !next.dataValid || next.getRGBData(trackerPanel) == null)
				continue;
			// get the frame number of the step
			TPoint p = next.getPosition();
			int stepFrame = p.getFrameNumber(trackerPanel);
			// step is valid if frame is included in the clip
			if (clip.includesFrame(stepFrame)) {
				validSteps.add(next);
			} else
				next.dataVisible = false;
		}
		RGBStep[] valid = validSteps.toArray(new RGBStep[0]);
		int len = valid.length;
		double[][] validData = new double[count + 1][len];
		// get the valid data
		for (int i = 0; i < len; i++) {
			// get the rgb data for the step
			double[] rgb = valid[i].getRGBData(trackerPanel);
			// get the frame number of the step
			TPoint p = valid[i].getPosition();
			int stepFrame = p.getFrameNumber(trackerPanel);
			dataFrames.add(new Integer(stepFrame));
			// get the step number and time
			int stepNumber = clip.frameToStep(stepFrame);
			double t = player.getStepTime(stepNumber) / 1000.0;
			// get the world position for the step
			Point2D pt = p.getWorldPosition(trackerPanel);
			// put data in validData array
			validData[0][i] = pt.getX();
			validData[1][i] = pt.getY();
			for (int j = 2; j < 7; j++) {
				validData[j][i] = rgb[j - 2];
			}
			validData[7][i] = stepNumber;
			validData[8][i] = stepFrame;
			for (int j = 9; j < 12; j++) {
				validData[j][i] = rgb[j - 4];
			}
			validData[12][i] = t;
		}
		clearColumns(data, count, dataVariables, "RGBRegion.Data.Description.", validData, len);
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

		fixedPositionItem.setText(TrackerRes.getString("RGBRegion.MenuItem.Fixed")); //$NON-NLS-1$
		fixedPositionItem.setSelected(isFixedPosition());
		fixedShapeItem.setText(TrackerRes.getString("RGBRegion.MenuItem.FixedShape")); //$NON-NLS-1$
		fixedShapeItem.setSelected(isFixedShape());
		menu.remove(deleteTrackItem);
		TMenuBar.checkAddMenuSep(menu);
		menu.add(fixedPositionItem);
		menu.add(fixedShapeItem);
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
	 * @return a list of components
	 */
	@Override
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
		
		shapeTypeDropdown.setSelectedIndex(shapeType);
		FontSizer.setFonts(shapeTypeDropdown, FontSizer.getLevel()); // pig?
		list.add(shapeTypeDropdown);
		
		int n = trackerPanel.getFrameNumber();
		RGBStep step = (RGBStep) getStep(n);
		
		if (shapeType == SHAPE_POLYGON) { 
			editPolygonButton.setText(TrackerRes.getString("RGBRegion.Button.Edit.Text"));
			if (step == null || !step.isPolygonClosed()) {
				if (step != null && step.getPolygonVertexCount() > 1) {
					list.add(editPolygonButton);
				}
				helpLabel.setText(TrackerRes.getString("RGBRegion.Label.MarkPolygon.Text")); //$NON-NLS-1$
				list.add(helpLabel);
			}
			else {
				list.add(editPolygonButton);					
			}
		}
		else {
			widthLabel.setText("w"); //$NON-NLS-1$
			list.add(widthLabel);
			Dimension dim = getShapeSize();
			widthField.setIntValue(dim.width);
			widthField.setEnabled(!isLocked());
			list.add(widthField);
			list.add(heightLabel);
			heightField.setIntValue(dim.height);
			heightField.setEnabled(!isLocked());
			list.add(heightField);
		}
		
		if (step == null)
			return list;
		
		if (shapeType == SHAPE_POLYGON && !step.isPolygonClosed())
			return list;

		stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
		xLabel.setText(dataVariables[1]);
		yLabel.setText(dataVariables[2]);
		xField.setUnits(trackerPanel.getUnits(this, dataVariables[1]));
		yField.setUnits(trackerPanel.getUnits(this, dataVariables[2]));

		xField.setEnabled(!isLocked());
		yField.setEnabled(!isLocked());

		// put step number into label
		stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		n = clip.frameToStep(n);
		stepValueLabel.setText(n + ":"); //$NON-NLS-1$

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

		ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
//    if (getStep(point, trackerPanel)==null) return list;
//
//  	xLabel.setText(dataVariables[1]); 
//  	yLabel.setText(dataVariables[2]); 
//    xField.setUnits(trackerPanel.getUnits(this, dataVariables[1]));
//    yField.setUnits(trackerPanel.getUnits(this, dataVariables[2]));
//
//    xField.setEnabled(!isLocked());
//    yField.setEnabled(!isLocked());
//    list.add(stepSeparator);
//    list.add(stepLabel);
//    list.add(stepValueLabel);
//    list.add(tSeparator);
//    list.add(xLabel);
//    list.add(xField);
//    list.add(xSeparator);
//    list.add(yLabel);
//    list.add(yField);
//    list.add(ySeparator);
		return list;
	}

	@Override
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		Object[] objectsToSize = new Object[] { widthLabel };
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
	 * Responds to property change events. This listens for the following events:
	 * "stepnumber" & "image" from TrackerPanel.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (tp != null) {
			if (maxEdgeLength == defaultMaxEdgeLength && tp.getVideo() != null) {
				setMaxEdgeLength(tp.getVideo());
			}
			switch (e.getPropertyName()) {
			case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
				invalidateData(Boolean.FALSE);
				int n = tp.getFrameNumber();
				RGBStep step = (RGBStep) getStep(n);
				if (step != null) {
					widthField.setIntValue(step.width);
					heightField.setIntValue(step.height);
					Point2D p = step.position.getWorldPosition(tp);
					xField.setValue(p.getX());
					yField.setValue(p.getY());
				}
				stepValueLabel.setText(e.getNewValue() + ":"); //$NON-NLS-1$
//        firePropertyChange(e); // to views
				if (!isFixedShape() && shapeType == SHAPE_POLYGON) {
					tp.getTrackBar(true).refresh();
				}
				break;
			case TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE:
				invalidateData(Boolean.FALSE);
				Video vid = tp.getVideo();
				if (vid == null)
					clearData(); // no video
				else if (!vid.isVisible()) // video invisible
					hideData();
				else if (!dataHidden && vid.isVisible()) // video filters
					clearData();
				else
					dataHidden = false;
				if (vid != null) {
					setMaxEdgeLength(vid);
				}
				firePropertyChange(e); // to views
				break;
			}
		}
		super.propertyChange(e); // handled by TTrack
	}

	private void setMaxEdgeLength(Video video) {
		Dimension d = video.getImageSize();
		maxEdgeLength = Math.min(d.height, d.width) - 1;
	}

	/**
	 * Overrides Object toString method.
	 *
	 * @return the name of this track
	 */
	@Override
	public String toString() {
		return TrackerRes.getString("RGBRegion.Name"); //$NON-NLS-1$
	}

	@Override
	public Map<String, NumberField[]> getNumberFields() {
		if (numberFields.isEmpty()) {
			numberFields.put(dataVariables[0], new NumberField[] { tField });
			numberFields.put(dataVariables[1], new NumberField[] { xField });
			numberFields.put(dataVariables[2], new NumberField[] { yField });
		}
		return numberFields;
	}

//__________________________ private methods ___________________________

	/**
	 * Sets the position of the current step based on the values in the x and y
	 * fields.
	 */
	private void setPositionFromFields() {
		double xValue = xField.getValue();
		double yValue = yField.getValue();
		int n = tp.getFrameNumber();
		RGBStep step = (RGBStep) getStep(n);
		if (step != null) {
			TPoint p = step.position;
			ImageCoordSystem coords = tp.getCoords();
			double x = coords.worldToImageX(n, xValue, yValue);
			double y = coords.worldToImageY(n, xValue, yValue);
			p.setXY(x, y);
			Point2D worldPt = p.getWorldPosition(tp);
			xField.setValue(worldPt.getX());
			yField.setValue(worldPt.getY());
		}
	}

	/**
	 * Refreshes a step by setting it equal to a keyframe step.
	 *
	 * @param step the step to refresh
	 */
	protected void refreshStep(RGBStep step) {
		if (step == null)
			return;
		// find key steps
		int key = 0;
		if (!isFixedPosition()) {
			for (int i : keyFrames) {
				if (i <= step.n)
					key = i;
			}
		}
		int shapeKey = 0;
		if (!isFixedShape()) {
			for (int i : shapeKeyFrames) {
				if (i <= step.n)
					shapeKey = i;
			}
		}
		// compare step with keySteps and update if needed
		RGBStep positionKeyStep = (RGBStep) steps.getStep(key);
		double x = positionKeyStep.getPosition().getX();
		double y = positionKeyStep.getPosition().getY();
		boolean differentPosition = x != step.getPosition().getX() || y != step.getPosition().getY();
		if (differentPosition) {
			step.getPosition().setLocation(x, y);
			step.erase();
			step.dataValid = false;
		}
		RGBStep shapeKeyStep = (RGBStep) steps.getStep(shapeKey);
		if (shapeType == SHAPE_POLYGON && shapeKeyStep.polygon == null) {
			// use first step with non-null polygon, if any
			for (int i = 0; i < steps.array.length; i++) {
				shapeKeyStep = (RGBStep) steps.getStep(i);
				if (shapeKeyStep.polygon != null)
					break;
			}
		}
		if (isDifferentShape(step, shapeKeyStep)) {
			if (shapeType == SHAPE_POLYGON) {
				step.rgbShape = step.polygon = shapeKeyStep.polygon;
			}
			else {
				step.setShapeSize(shapeKeyStep.width, shapeKeyStep.height);
			}
			step.erase();
			step.dataValid = false;
		}
	}
	
	private boolean isDifferentShape(RGBStep step, RGBStep keyStep) {
		if (shapeType == SHAPE_POLYGON) {
			return step.polygon != keyStep.polygon;
		}
		return keyStep.width != step.width || keyStep.height != step.height;
	}

	private boolean isEditingPolygon() {
		if (shapeType != SHAPE_POLYGON)
			return false;
		RGBStep step = (RGBStep) steps.getStep(tp.getFrameNumber());
		return step != null && !step.isPolygonClosed();
	}

//__________________________ static methods ___________________________

	/**
	 * Returns the luma (perceived brightness) of a video RGB color.
	 *
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @return the video luma
	 */
	public static double getLuma(double r, double g, double b) {
		// following code based on CCIR 601 specs
		return 0.299 * r + 0.587 * g + 0.114 * b;
	}

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
			RGBRegion region = (RGBRegion) obj;
			// save track data
			XML.getLoader(TTrack.class).saveObject(control, obj);
			// save fixed position
			control.setValue("fixed", region.isFixedPosition()); //$NON-NLS-1$
			control.setValue("fixed_shape", region.isFixedShape()); //$NON-NLS-1$
			// save shape type
			control.setValue("shape_type", region.shapeType);
			// save step data, if any
			if (!region.steps.isEmpty()) {
				Step[] steps = region.getSteps();
				int count = region.isFixedPosition() ? 1 : steps.length;
				// save position data			
				double[][] positions = new double[count][];
//				FrameData[] data = new FrameData[count];
				for (int n = 0; n < count; n++) {
					// save only position key frames
					if (steps[n] == null || !region.keyFrames.contains(n))
						continue;
//					data[n] = new FrameData((RGBStep) steps[n]);
					RGBStep.Position next = ((RGBStep) steps[n]).position;
					positions[n] = new double[] {next.x, next.y};
				}
//				control.setValue("framedata", data); //$NON-NLS-1$
				control.setValue("positions", positions); //$NON-NLS-1$
				// save shape sizes/vertices
				count = region.isFixedShape() ? 1 : steps.length;
				double[][][] shapes = new double[count][][];
				for (int n = 0; n < count; n++) {
					// save only shape key frames
					if (steps[n] == null || !region.shapeKeyFrames.contains(n))
						continue;
					RGBStep step = (RGBStep) steps[n];
					if (region.shapeType == RGBRegion.SHAPE_POLYGON)
						shapes[n] = step.getPolygonVertices();
					else
						shapes[n] = new double[][] {{step.width, step.height}};
				}
				control.setValue("shapes", shapes); //$NON-NLS-1$
				// save RGB values
				count = steps.length;
				int first = 0;
				int last = count - 1;
				if (region.tp != null) {
					first = region.tp.getPlayer().getVideoClip().getStartFrameNumber();
					last = region.tp.getPlayer().getVideoClip().getEndFrameNumber();
				}
				double[][] rgb = new double[last + 1][];
				double[] stepRGB;
				for (int n = first; n <= last; n++) {
					// save RGB and pixel count data for all valid frames in clip
					if (n > steps.length - 1 || steps[n] == null)
						continue;
					if (((RGBStep) steps[n]).dataValid) {
						stepRGB = ((RGBStep) steps[n]).rgbData;
						rgb[n] = new double[7];
						System.arraycopy(stepRGB, 0, rgb[n], 0, 3);
						System.arraycopy(stepRGB, 4, rgb[n], 3, 4);
					}
				}
				control.setValue("rgb", rgb); //$NON-NLS-1$
			}
		}

		/**
		 * Creates a new object with data from an XMLControl.
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			RGBRegion region = new RGBRegion();
			return region;
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
			RGBRegion region = (RGBRegion) obj;
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			boolean locked = region.isLocked();
			region.setLocked(false);
			region.loading = true;
			// load fixed position
			region.fixedPosition = control.getBoolean("fixed"); //$NON-NLS-1$
			// load fixed shape (legacy: radius)
			if (control.getPropertyNamesRaw().contains("fixed_radius"))
				region.fixedShape = control.getBoolean("fixed_radius"); //$NON-NLS-1$
			else
				region.fixedShape = control.getBoolean("fixed_shape"); //$NON-NLS-1$
			// load shape type
			int type = control.getInt("shape_type");
			region.shapeType = type == Integer.MIN_VALUE? SHAPE_ELLIPSE: type;
			// load step data
			region.keyFrames.clear();
			region.shapeKeyFrames.clear();
			
			// vers 6.0.4+ code
			double[][] positions = (double[][])control.getObject("positions");
			if (positions != null) {
				for (int n = 0; n < positions.length; n++) {
					if (positions[n] == null)
						continue;
					region.createStep(n, positions[n][0], positions[n][1]);
				}
			}
			double[][][] shapes = (double[][][])control.getObject("shapes");
			if (shapes != null) {
				region.shapeKeyFrames.clear();
				for (int n = 0; n < shapes.length; n++) {
					if (shapes[n] == null)
						continue;
					RGBStep step = (RGBStep) region.steps.getStep(n);
					if (region.shapeType == RGBRegion.SHAPE_POLYGON)
						step.setPolygonVertices(shapes[n]);
					else
						step.setShapeSize(shapes[n][0][0], shapes[n][0][1]);
					region.shapeKeyFrames.add(n);
				}					
			}
			// end vers 6.0.4+ code
			
			// pre-vers 6.0.4 code
			if (control.getPropertyNamesRaw().contains("framedata")) {
				Object dataObj = control.getObject("framedata"); //$NON-NLS-1$
				FrameData[] data = null;
				if (dataObj instanceof FrameData) { // legacy
					data = new FrameData[] { (FrameData) dataObj };
				} else { // dataObj instanceof FrameData[]
					data = (FrameData[]) dataObj;
				}
				if (data != null) {
					for (int n = 0; n < data.length; n++) {
						if (data[n] == null)
							continue;
						RGBStep step = (RGBStep) region.createStep(n, data[n].x, data[n].y);
						if (data[n].r != Integer.MIN_VALUE) {
							step.setShapeSize(2 * data[n].r, 2 * data[n].r);
							region.shapeKeyFrames.add(n); // for legacy compatibility
						}
					}
				}
			}
			Integer[] radii = (Integer[]) control.getObject("radii"); //$NON-NLS-1$
			if (radii != null) {
				region.shapeKeyFrames.clear();
				for (int n = 0; n < radii.length; n++) {
					if (radii[n] == null)
						continue;
					RGBStep step = (RGBStep) region.steps.getStep(n);
					int side = 2 * radii[n];
					step.setShapeSize(side, side);
					region.shapeKeyFrames.add(n);
				}
			}
			// end pre-vers 6.0.4 code

			double[][] rgb = (double[][]) control.getObject("rgb"); //$NON-NLS-1$
			if (rgb != null) {
				for (int n = 0; n < rgb.length; n++) {
					if (rgb[n] == null)
						continue;
					RGBStep step = (RGBStep) region.steps.getStep(n);
					System.arraycopy(rgb[n], 0, step.rgbData, 0, 3);					
					System.arraycopy(rgb[n], 3, step.rgbData, 4, rgb[n].length >= 7? 4: 1);
					step.rgbData[3] = getLuma(rgb[n][0], rgb[n][1], rgb[n][2]);
					region.refreshStep(step);
					step.dataValid = true;
				}
			}
			region.setLocked(locked);
			region.loading = false;
			region.repaint();
			return obj;
		}
	}

	/**
	 * Inner class containing the rgb data for a single frame number.
	 * Now used only for legacy pre-vers 6.0.4 TRK files
	 */
	private static class FrameData {
		double x, y;
		int r;

		FrameData() {
			/** empty block */
		}

	}

	/**
	 * A class to save and load a FrameData.
	 */
	private static class FrameDataLoader implements XML.ObjectLoader {

		@Override
		public void saveObject(XMLControl control, Object obj) {
		}

		@Override
		public Object createObject(XMLControl control) {
			return new FrameData();
		}

		@Override
		public Object loadObject(XMLControl control, Object obj) {
			FrameData data = (FrameData) obj;
			data.x = control.getDouble("x"); //$NON-NLS-1$
			data.y = control.getDouble("y"); //$NON-NLS-1$
			data.r = control.getInt("r"); //$NON-NLS-1$
			return obj;
		}
	}

}
