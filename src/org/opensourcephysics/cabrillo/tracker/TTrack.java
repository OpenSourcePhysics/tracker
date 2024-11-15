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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.opensourcephysics.cabrillo.tracker.TableTrackView.TrackDataTable;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.DataFunction;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DrawableTextLine;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.display.TextLine;
import org.opensourcephysics.media.core.DecimalField;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Trackable;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.FontSizer;

/**
 * A TTrack draws a series of visible Steps on a TrackerPanel. This is an
 * abstract class that cannot be instantiated directly.
 *
 * @author Douglas Brown
 */
public abstract class TTrack extends OSPRuntime.Supported implements Interactive, Trackable, PropertyChangeListener {

	public static final String PROPERTY_TTRACK_FOOTPRINT = "footprint"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_MASS = "mass"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_MODELEND = "model_end"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_MODELSTART = "model_start"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_NAME = "name"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_STEP = "step"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_STEPS = "steps"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_FORMAT = "format"; //$NON-NLS-1$

	public static final String PROPERTY_TTRACK_VISIBLE = "visible"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_DATA = "data"; //$NON-NLS-1$
	public static final String PROPERTY_TTRACK_COLOR = "color"; //$NON-NLS-1$

	public static final String PROPERTY_TTRACK_LOCKED = "locked"; //$NON-NLS-1$

	public static final String PROPERTY_TTRACK_TEXTCOLUMN = "text_column"; //$NON-NLS-1$

	public static final Integer HINT_STEP_ADDED_OR_REMOVED = -2;
	public static final Integer HINT_STEPS_SELECTED = -3;

// For reference only. BH 2021.08.15
//
//	private final static String[] panelEventsOther = new String[] { 
//			
//
// 			Trackable.PROPERTY_ADJUSTING, TTrack(many)
//			TTrack.PROPERTY_TTRACK_LOCKED, // Calibration
//
//			TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, // CenterOfMass
//
//			TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, // LineProfile
//
//			TTrack.PROPERTY_TTRACK_LOCKED, // OffsetOrigin
//
//	 QUESTION HERE! TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, // ParticleDataTrack
//
//			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, // PerspectiveTrack
//			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, // PerspectiveTrack
//
//			ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, // PointMass (also DynamicSystem,ParticleModel)
//			VideoClip.PROPERTY_VIDEOCLIP_STEPSIZE,  // PointMass (and ParticleModel)
//
//			TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, // RGBRegion
//
//			TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, // VectorSum
//
//			//ImageCoordSystem.PROPERTY_COORDS_FIXEDANGLE, 
//			//ImageCoordSystem.PROPERTY_COORDS_FIXEDORIGIN,
//			//TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR,
//			//TrackerPanel.PROPERTY_TRACKERPANEL_LOADED,
//			//TrackerPanel.PROPERTY_TRACKERPANEL_SIZE,
//			//TrackerPanel.PROPERTY_TRACKERPANEL_UNITS, 
//			//TrackerPanel.PROPERTY_TRACKERPANEL_VIDEOVISIBLE, 
//			//TTrack.PROPERTY_TTRACK_FORMAT,
//			//TTrack.PROPERTY_TTRACK_MASS, // only if originating in PointMass  
//			//VideoPanel.PROPERTY_VIDEOPANEL_DATAFILE,
//	};

	private final static String[] panelEventsTTrack = new String[] { 
			TFrame.PROPERTY_TFRAME_RADIANANGLES, 
			TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION, 
			TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, // (Calibration,
															// CircleFitter,CoordAxes,LineProfile,OffsetOrigin,Protractor,RGBRegion,TapeMeasure)
			TTrack.PROPERTY_TTRACK_DATA, 
			Video.PROPERTY_VIDEO_COORDS, 
			ImageCoordSystem.PROPERTY_COORDS_TRANSFORM,
			VideoPanel.PROPERTY_VIDEOPANEL_IMAGESPACE, 
	};

	/**
	 * Responds to property change events fired in TrackerPanel or VideoPanel.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource() instanceof TrackerPanel) {
			TrackerPanel trackerPanel = (TrackerPanel) e.getSource();
			switch (e.getPropertyName()) {
			case TFrame.PROPERTY_TFRAME_RADIANANGLES:
				setAnglesInRadians((Boolean) e.getNewValue());
				break;
			case TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION:
				erase();
				break;
			case Trackable.PROPERTY_ADJUSTING:
			case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
				// see many subclasses
				break;
			case PROPERTY_TTRACK_DATA:
				dataValid = false;
				break;
			case VideoPanel.PROPERTY_VIDEOPANEL_IMAGESPACE:
				erase(trackerPanel.getID());
				break;
			case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
			case Video.PROPERTY_VIDEO_COORDS:
				if (ttype != TTrack.TYPE_POINTMASS) {
					dataValid = false;
				}
				erase();
				TFrame.repaintT(trackerPanel);
				break;
			}
		} else {
			System.out.println("??? TTRack " + e);
		}

	}

	/**
	 * Install the controlling TrackerPanel for this track (by default, the first
	 * TrackerPanel that adds this track to its drawables) and add this track to the
	 * panel's specific (outgoing) listener lists so that the track can respond to
	 * external changes.
	 * 
	 * This method is overridden to add specific TrackerPanel events for subclasses.
	 * 
	 *
	 * @param panel the TrackerPanel
	 */
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null) {
			removePanelEvents(panelEventsTTrack);
		}
		if (panel == null) {
			tp = null;
			tframe = null;
		} else {
			tp = panel.ref(this);
			tframe = panel.getTFrame();
			addPanelEvents(panelEventsTTrack);
		}
	}

	/**
	 * Add specific (incoming) trackPanel events that the track has to respond to.
	 * @param events
	 */
	protected void addPanelEvents(String[] events) {
		for (int i = events.length; --i >= 0;)
			tp.addPropertyChangeListener(events[i], this);
	}

	/**
	 * Remove specific (incoming) trackPanel events that the track was respond to.
	 * @param events
	 */
	protected void removePanelEvents(String[] events) {
		for (int i = events.length; --i >= 0;)
			tp.removePropertyChangeListener(events[i], this);
	}

	/**
	 * Allow the TrackPanel to respond to specific track (outgoing) property changes.
	 * 
	 * @param panel
	 */
	public void addListener(TrackerPanel panel) {
		addPropertyChangeListener(PROPERTY_TTRACK_FORMAT, panel);
		addPropertyChangeListener(PROPERTY_TTRACK_MASS, panel);
		addPropertyChangeListener(PROPERTY_TTRACK_MODELEND, panel);
		addPropertyChangeListener(PROPERTY_TTRACK_MODELSTART, panel);
		addPropertyChangeListener(PROPERTY_TTRACK_NAME, panel);
		addPropertyChangeListener(PROPERTY_TTRACK_FOOTPRINT, panel);
		addStepListener(panel);

	}

	/**
	 * Remove the TrackPanel from specific (outgoing) property change lists.
	 * 
	 * @param panel
	 */
	public void removeListener(TrackerPanel panel) {
		removePropertyChangeListener(PROPERTY_TTRACK_FORMAT, panel);
		removePropertyChangeListener(PROPERTY_TTRACK_MASS, panel);
		removePropertyChangeListener(PROPERTY_TTRACK_MODELEND, panel);
		removePropertyChangeListener(PROPERTY_TTRACK_MODELSTART, panel);
		removePropertyChangeListener(PROPERTY_TTRACK_NAME, panel);
		removePropertyChangeListener(PROPERTY_TTRACK_FOOTPRINT, panel);
		removeStepListener(panel);

		// this one is in updateListenerVisible only -- WorldTView only
		removePropertyChangeListener(PROPERTY_TTRACK_VISIBLE, panel);

	}

	/**
	 * The NAME, COLOR, and FOOTPRINT property changes are of interest to
	 * AutoTracker, DynamicSystemInspector, VectorSumInspector, AttachementDialog,
	 * TrackChooserTView, TrackControl, and TTrackBar. 
	 * 
	 * @param l
	 */
	public void addListenerNCF(PropertyChangeListener l) {
		addPropertyChangeListener(PROPERTY_TTRACK_NAME, l);
		addPropertyChangeListener(PROPERTY_TTRACK_COLOR, l);
		addPropertyChangeListener(PROPERTY_TTRACK_FOOTPRINT, l);
	}

	public void removeListenerNCF(PropertyChangeListener l) {
		removePropertyChangeListener(PROPERTY_TTRACK_NAME, l);
		removePropertyChangeListener(PROPERTY_TTRACK_COLOR, l);
		removePropertyChangeListener(PROPERTY_TTRACK_FOOTPRINT, l);
	}

	/**
	 * STEP and STEPS listeners include TrackerPanel, CenterOfMass, CircleFitter,
	 * DynamicParticle, DynamicSystem, ReferenceFrame, PlotTrackView, and
	 * TrackChooserTView.
	 * 
	 * @param c
	 */
	public void addStepListener(PropertyChangeListener c) {
		addPropertyChangeListener(PROPERTY_TTRACK_STEP, c);
		addPropertyChangeListener(PROPERTY_TTRACK_STEPS, c);
	}

	public void removeStepListener(PropertyChangeListener c) {
		removePropertyChangeListener(PROPERTY_TTRACK_STEP, c);
		removePropertyChangeListener(PROPERTY_TTRACK_STEPS, c);
	}

	/**
	 * TTRACK_VISIBLE is of interest to TToolBar Calibration and Ruler buttons, as
	 * well as WorldTView. This method safely adds the listener, first removing an
	 * existing one by this name if present.
	 * 
	 * @param l
	 */
	public void updateListenerVisible(PropertyChangeListener l) {
		removePropertyChangeListener(PROPERTY_TTRACK_VISIBLE, l);
		addPropertyChangeListener(PROPERTY_TTRACK_VISIBLE, l);
	}

	private static HashMap<Integer, TTrack> panelActiveTracks = new HashMap<Integer, TTrack>();

	final public int ttype;

	protected static JDialog skippedStepWarningDialog;
	protected static JTextPane skippedStepWarningTextpane;
	protected static JCheckBox skippedStepWarningCheckbox;
	protected static JButton closeButton;
	protected static boolean skippedStepWarningOn = true;
	protected static NameDialog nameDialog;
	protected static int nextID = 1;
	// instance fields
	protected String name = TrackerRes.getString("TTrack.Name.None"); //$NON-NLS-1$
	protected String description = ""; //$NON-NLS-1$
	protected boolean visible = true;
	protected boolean trailVisible = false;
	protected int trailLength = 0; // controls trail length
	protected boolean locked = false;
	protected boolean enabled = true;
	protected boolean viewable = true; // determines whether Views include this track
	protected Footprint[] footprints = new Footprint[0];
	protected Footprint footprint;
	protected Footprint defaultFootprint;
	protected Color[] defaultColors = new Color[] { Color.red };
	protected StepArray steps = new StepArray();
	protected HashMap<String, Object> properties = new HashMap<String, Object>();
	protected DatasetManager datasetManager;
//	protected HashMap<Integer, double[]> panelWorldBounds;
//	
//	private HashMap<Integer, double[]> getPanelWorldBounds() {
//		return (panelWorldBounds == null ? (panelWorldBounds = new HashMap<Integer, double[]>()) : panelWorldBounds);
//	}
	protected final Point2D.Double[] points = new Point2D.Double[] { new Point2D.Double() };
	protected ArrayList<Component> toolbarTrackComponents = new ArrayList<Component>();
	protected ArrayList<Component> toolbarPointComponents = new ArrayList<Component>();
	protected Map<String, NumberField[]> numberFields = new TreeMap<String, NumberField[]>();
	protected boolean autoAdvance;
	protected boolean markByDefault = false, isMarking = false;
	protected TextLineLabel xLabel, yLabel, magLabel, angleLabel;
	protected boolean undoEnabled = true;

	protected ActionListener footprintListener, circleFootprintListener;

	protected Font labelFont = new Font("arial", Font.PLAIN, 12); //$NON-NLS-1$
	protected TrackerPanel tp; // 900 references!
	protected TFrame tframe;
	protected XMLProperty dataProp;
	protected Object[][] constantsLoadedFromXML;
	protected String[] dataDescriptions;

	/**
	 * set false if data needs to be initialized, e.g. CenterOfMass from TRZ
	 */
	protected boolean initialized = true;

	protected boolean dataValid; // true if data is valid
	protected boolean refreshDataLater;
	protected int[] preferredColumnOrder;
	// dataFrames lists frame numbers included in the current data
	protected ArrayList<Integer> dataFrames = new ArrayList<Integer>();
	protected String partName, hint;
	protected int stepSizeWhenFirstMarked;
	protected TreeSet<Integer> keyFrames = new TreeSet<Integer>();
	// for autotracking
	protected boolean autoTrackerMarking;
	protected int targetIndex;
	// attached tracks--used by AttachmentDialog with TapeMeasure, Protractor and
	// CircleFitter tracks
	protected TTrack[] attachments;
	protected String[] attachmentNames; // used when loading attachments
	// user-editable text columns shown in DataTable view
	protected Map<String, String[]> textColumnEntries = new TreeMap<String, String[]>();
	protected ArrayList<String> textColumnNames = new ArrayList<String>();
	// mouse listener for number fields
	protected MouseAdapter formatMouseListener, formatAngleMouseListener;
	protected String[] customNumberFormats;
	private int ID; // unique ID number


	// GUI
	protected JLabel tLabel, stepLabel, tValueLabel, stepValueLabel;
	protected NumberField tField, xField, yField, magField;
	protected DecimalField angleField;
	protected NumberField[] positionFields;
	protected Border fieldBorder;
	protected JSpinner xSpinner, ySpinner;

	protected JMenu footprintMenu;

	protected Component tSeparator, xSeparator, ySeparator, magSeparator, angleSeparator, stepSeparator;

	protected JCheckBoxMenuItem visibleItem;
	protected JCheckBoxMenuItem trailVisibleItem;
	protected JCheckBoxMenuItem markByDefaultItem;
	protected JCheckBoxMenuItem autoAdvanceItem;
	protected JCheckBoxMenuItem lockedItem, fixedItem;
	protected JMenuItem nameItem;
	protected JMenuItem colorItem;
	protected JMenuItem deleteTrackItem, deleteStepItem, clearStepsItem;
	protected JMenuItem descriptionItem;
	protected JMenuItem dataBuilderItem;
	/**
	 * PointMass and Vector are base types for their subtypes; all others are their
	 * own type
	 */
	private final static String[] baseTrackTypes = new String[] { "Calibaration", // 0
			"CircleFitter", // 1
			"CoordAxes", // 2
			"LineProfile", // 3
			"OffsetOrigin", // 4
			"PointMass", // 5 includes CenterOfMass and ParticleModels (Analytical, Dynamic, and
							// ParticleDataTrack
			"Protractor", // 6
			"RGBRegion", // 7
			"TapeMeasure", // 8
			"Vector", // 9 includes VectorSum
			"Perspective" // 10 for completeness
	};

	public static String getBaseTrackName(int ttype) {
		return (ttype >= 0 ? baseTrackTypes[ttype] : null);
	}

	@SuppressWarnings("unchecked")
	private final static TreeMap<String, String>[] defaultFormatPatterns = new TreeMap[baseTrackTypes.length];

	public static TreeMap<String, String>[] getDefaultFormatPatterns() {
		return defaultFormatPatterns;
	}

	@SuppressWarnings("unchecked")
	private final static TreeMap<String, String>[] prevDefaultPatterns = new TreeMap[baseTrackTypes.length];

	public static void savePatterns(TrackerPanel panel) {
		for (int ttype = baseTrackTypes.length; --ttype >= 0;) {
			TreeMap<String, String> prevPatterns = new TreeMap<String, String>();
			prevPatterns.putAll(panel.getFormatPatterns(ttype));
			prevDefaultPatterns[ttype] = prevPatterns;
		}
	}

	public static void restorePatterns(TrackerPanel panel) {
		TreeMap<String, String>[] patterns = panel.formatPatterns;
		for (int ttype = baseTrackTypes.length; --ttype >= 0;) {
			patterns[ttype] = prevDefaultPatterns[ttype];
		}
	}

	public final static int TYPE_UNKNOWN = -1;
	public final static int TYPE_CALIBRATION = 0;
	public final static int TYPE_CIRCLEFITTER = 1;
	public final static int TYPE_COORDAXES = 2;
	public final static int TYPE_LINEPROFILE = 3;
	public final static int TYPE_OFFSETORIGIN = 4;
	public final static int TYPE_POINTMASS = 5;
	public final static int TYPE_PROTRACTOR = 6;
	public final static int TYPE_RGBREGION = 7;
	public final static int TYPE_TAPEMEASURE = 8;
	public final static int TYPE_VECTOR = 9;
	public final static int TYPE_PERSPECTIVE = 10;

	/**
	 * Constructs a TTrack.
	 * 
	 * @param type
	 */
	protected TTrack(int ttype) {
		this.ttype = ttype;
		OSPLog.notify(this, "<init>");
		ID = nextID++;
		// create toolbar components
		stepLabel = new JLabel();
		stepLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		stepValueLabel = new JLabel();
		stepValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		tLabel = new JLabel();
		tLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
		tValueLabel = new JLabel();
		tValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
		tField = new TrackDecimalField(3) {
			@Override
			public void setValue(double value) {
				super.setValue(value);
				tValueLabel.setText("(" + tField.getText() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
		tField.setUnits("s"); //$NON-NLS-1$
		// create spinners
		SpinnerModel model = new SpinnerNumberModel(0, -100, 100, 0.1);
		xSpinner = new JSpinner(model);
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(xSpinner, "0.00"); //$NON-NLS-1$
		editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
		xSpinner.setEditor(editor);
		model = new SpinnerNumberModel(0, -100, 100, 0.1);
		ySpinner = new JSpinner(model);
		editor = new JSpinner.NumberEditor(ySpinner, "0.00"); //$NON-NLS-1$
		editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
		ySpinner.setEditor(editor);
		stepSeparator = Box.createRigidArea(new Dimension(4, 4));
		tSeparator = Box.createRigidArea(new Dimension(6, 4));
		xSeparator = Box.createRigidArea(new Dimension(6, 4));
		ySeparator = Box.createRigidArea(new Dimension(6, 4));
		magSeparator = Box.createRigidArea(new Dimension(6, 4));
		angleSeparator = Box.createRigidArea(new Dimension(6, 4));

		// create mouse listeners for fields
		formatMouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (OSPRuntime.isPopupTrigger(e)) {
					showFormatPopup((NumberField) e.getSource());
				}
			}
		};
		formatAngleMouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// BH Q: How can e be null here?
				if (e == null || OSPRuntime.isPopupTrigger(e)) {
					showAnglePopup(e == null ? angleField : (NumberField) e.getSource());
				}
			}
		};

		// create labels and fields
		xLabel = new TextLineLabel();
		yLabel = new TextLineLabel();
		magLabel = new TextLineLabel();
		angleLabel = new TextLineLabel();
		
		xField = new TrackNumberField();
		yField = new TrackNumberField();
		magField = new TrackNumberField();
		magField.setMinValue(0);
		xField.addMouseListener(formatMouseListener);
		yField.addMouseListener(formatMouseListener);
		magField.addMouseListener(formatMouseListener);
		angleField = new TrackDecimalField(1);
		angleField.addMouseListener(formatAngleMouseListener);
		Border empty = BorderFactory.createEmptyBorder(0, 3, 0, 3);
		Color grey = new Color(102, 102, 102);
		Border etch = BorderFactory.createEtchedBorder(Color.white, grey);
		fieldBorder = BorderFactory.createCompoundBorder(etch, empty);
		tField.setBorder(fieldBorder);
		xField.setBorder(fieldBorder);
		yField.setBorder(fieldBorder);
		magField.setBorder(fieldBorder);
		angleField.setBorder(fieldBorder);
		positionFields = new NumberField[] { xField, yField, magField, angleField };
		footprintListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String footprintName = e.getActionCommand();
				if (getFootprint().getName().equals(footprintName))
					return;
				XMLControl control = new XMLControlElement(new TrackProperties(TTrack.this));
				setFootprint(footprintName);
				Undo.postTrackDisplayEdit(TTrack.this, control);
			}
		};
		circleFootprintListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				footprintListener.actionPerformed(e);
				CircleFootprint cfp = (CircleFootprint) getFootprint();
				cfp.showProperties(TTrack.this);
			}
		};

	}

	protected void showAnglePopup(NumberField field) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem();
		final boolean radians = field.getConversionFactor() == 1;
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tframe.setAnglesInRadians(!radians);
			}
		});
		item.setText(radians ? TrackerRes.getString("TTrack.AngleField.Popup.Degrees") : //$NON-NLS-1$
				TrackerRes.getString("TTrack.AngleField.Popup.Radians")); //$NON-NLS-1$
		popup.add(item);
		popup.addSeparator();

		if (tp.isEnabled("number.formats")) { //$NON-NLS-1$
			item = new JMenuItem();
			final String[] selected = new String[] { getNumberFieldName0(field) };
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					NumberFormatDialog.getNumberFormatDialog(tp, TTrack.this, selected).setVisible(true);
				}
			});
			item.setText(TrackerRes.getString("TTrack.MenuItem.NumberFormat")); //$NON-NLS-1$
			popup.add(item);
		}

		FontSizer.setFonts(popup, FontSizer.getLevel());
		popup.show(field, 0, angleField.getHeight());
	}

	protected void showFormatPopup(NumberField field) {
		String[] fieldName = null;
		boolean hasUnits = false;
		String name = getNumberFieldName0(field);
		if (name != null) {
			fieldName = new String[] { name };
			String s = getVariableDimensions(this, name);
			hasUnits = s.contains("L") || s.contains("M") || s.contains("T"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		JPopupMenu popup = new JPopupMenu();
		if (tp.isEnabled("number.formats") || tp.isEnabled("number.units")) { //$NON-NLS-1$ //$NON-NLS-2$
			JMenu numberMenu = new JMenu(TrackerRes.getString("Popup.Menu.Numbers")); //$NON-NLS-1$
			popup.add(numberMenu);
			if (tp.isEnabled("number.formats")) { //$NON-NLS-1$
				JMenuItem item = new JMenuItem();
				final String[] selected = fieldName;
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						NumberFormatDialog.getNumberFormatDialog(tp, TTrack.this, selected).setVisible(true);
					}
				});
				item.setText(TrackerRes.getString("Popup.MenuItem.Formats") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				numberMenu.add(item);
			}

			if (hasUnits && tp.isEnabled("number.units")) { //$NON-NLS-1$
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
				}
			});
			item.setText(vis ? TrackerRes.getString("TTrack.MenuItem.HideUnits") : //$NON-NLS-1$
					TrackerRes.getString("TTrack.MenuItem.ShowUnits")); //$NON-NLS-1$
			if (popup.getComponentCount() > 0)
				popup.addSeparator();
			popup.add(item);
		}

		FontSizer.setFonts(popup, FontSizer.getLevel());
		popup.show(field, 0, field.getHeight());
	}

	private String getNumberFieldName0(NumberField field) {
		for (String name : getNumberFields().keySet()) {
			if (numberFields.get(name)[0] == field) {
				return name;
			}
		}
		return null;
	}

	/**
	 * Shows and hides this track.
	 *
	 * @param visible <code>true</code> to show this track
	 */
	public void setVisible(boolean visible) {
		Boolean prev = Boolean.valueOf(this.visible);
		this.visible = visible;
		firePropertyChange(PROPERTY_TTRACK_VISIBLE, prev, Boolean.valueOf(visible)); // $NON-NLS-1$
		if (tp != null)
			TFrame.repaintT(tp);
	}

	/**
	 * Removes this track from all panels that draw it. If no other objects have a
	 * reference to it, this should then be garbage-collected.
	 */
	public void delete() {
		delete(true);
	}

	/**
	 * Removes this track from all panels that draw it. If no other objects have a
	 * reference to it, this should then be garbage-collected.
	 * 
	 * @param postEdit true to post an undoable edit
	 */
	protected void delete(boolean postEdit) {
		if (isLocked() && !isDependent())
			return;
		if (tp != null) {
			tp.setSelectedPoint(null);
			tp.selectedSteps.clear();
			// handle case when this is the origin of current reference frame
			ImageCoordSystem coords = tp.getCoords();
			if (coords instanceof ReferenceFrame && ((ReferenceFrame) coords).getOriginTrack() == this) {
				// set coords to underlying coords
				coords = ((ReferenceFrame) coords).getCoords();
				tp.setCoords(coords);
			}
		}
		if (postEdit) {
			Undo.postTrackDelete(this); // posts undoable edit
		}
		erase();
		for (int j = 0; j < tp.andWorld.size(); j++) {
			TrackerPanel panel = panel(tp.andWorld.get(j));
			panel.removeTrack(this);
		}
		dispose();
	}

    TrackerPanel panel(Integer panelID) {
		return tframe.getTrackerPanelForID(panelID);
	}

	/**
	 * Reports whether or not this is visible.
	 *
	 * @return <code>true</code> if this track is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Shows and hides the trail. If the trail is shown, all steps are visible. If
	 * not, only the current step is visible.
	 *
	 * @param visible <code>true</code> to show trail
	 */
	public void setTrailVisible(boolean visible) {
		trailVisible = visible;
	}

	/**
	 * Gets the trail visibility.
	 *
	 * @return <code>true</code> if trail is visible
	 */
	public boolean isTrailVisible() {
		return trailVisible;
	}

	/**
	 * Sets the trail length.
	 *
	 * @param steps the trail length
	 */
	public void setTrailLength(int steps) {
		trailLength = Math.max(0, steps);
	}

	/**
	 * Gets the trail length.
	 *
	 * @return trail length
	 */
	public int getTrailLength() {
		if (isMarking)
			return 1;
		return trailLength;
	}

	/**
	 * Locks and unlocks this track. When locked, no changes are allowed.
	 *
	 * @param locked <code>true</code> to lock this
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
		firePropertyChange(PROPERTY_TTRACK_LOCKED, null, Boolean.valueOf(locked)); // $NON-NLS-1$
	}

	/**
	 * Gets the locked property.
	 *
	 * @return <code>true</code> if this is locked
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Sets the autoAdvance property.
	 *
	 * @param auto <code>true</code> to request that the video autoadvance while
	 *             marking.
	 */
	public void setAutoAdvance(boolean auto) {
		autoAdvance = auto;
	}

	/**
	 * Gets the autoAdvance property.
	 *
	 * @return <code>true</code> if this is autoadvance
	 */
	public boolean isAutoAdvance() {
		return autoAdvance;
	}

	/**
	 * Sets the markByDefault property. When true, the mouse handler should mark a
	 * point whenever the active track reports itself incomplete.
	 *
	 * @param mark <code>true</code> to mark by default
	 */
	public void setMarkByDefault(boolean mark) {
		markByDefault = mark;
	}

	/**
	 * Gets the markByDefault property. When true, the mouse handler should mark a
	 * point whenever the active track reports itself incomplete.
	 *
	 * @return <code>true</code> if this marks by default
	 */
	public boolean isMarkByDefault() {
		return markByDefault;
	}

	/**
	 * Gets the color.
	 *
	 * @return the current color
	 */
	public Color getColor() {
		if (footprint == null)
			return defaultColors[0];
		return footprint.getColor();
	}

	/**
	 * Sets the color.
	 *
	 * @param color the desired color
	 */
	public void setColor(Color color) {
		if (color == null)
			color = defaultColors[0];
		for (int i = 0; i < footprints.length; i++)
			footprints[i].setColor(color);
		erase();
		if (tp != null) {
			tp.changed = true;
			if (tp.modelBuilder != null) {
				tp.modelBuilder.refreshDropdown(null);
			}
			if (tp.dataBuilder != null) {
				org.opensourcephysics.tools.FunctionPanel panel = tp.dataBuilder.getPanel(getName());
				if (panel != null) {
					panel.setIcon(getIcon(21, 16, "track")); //$NON-NLS-1$
					tp.dataBuilder.refreshDropdown(null);
				}
			}
		}
		firePropertyChange(PROPERTY_TTRACK_COLOR, null, color); // $NON-NLS-1$
	}

	/**
	 * Sets the color to one of the default colors[].
	 *
	 * @param index the color index
	 */
	public void setColorToDefault(int index) {
		setColor(defaultColors[index % defaultColors.length]);
	}

	/**
	 * Sets the default name and color for a specified tracker panel.
	 *
	 * @param trackerPanel the TrackerPanel
	 * @param connector    the string connector between the name and letter suffix
	 */
	public void setDefaultNameAndColor(TrackerPanel trackerPanel, String connector) {
		String name = trackerPanel.getNextName(getName(), connector);
		setName(name);
		setColorToDefault((int) name.charAt(name.length() - 1) - 65);
	}

	/**
	 * Gets the ID number of this track.
	 *
	 * @return the ID number
	 */
	public int getID() {
		return ID;
	}

	/**
	 * Gets the name of this track.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the name of this track.
	 * 
	 * @param context ignored by default
	 *
	 * @return the name
	 */
	public String getName(String context) {
		return getName();
	}

	/**
	 * Sets the name of this track.
	 *
	 * @param newName the new name of this track
	 */
	public void setName(String newName) {
		if (newName != null && !newName.trim().equals("")) { //$NON-NLS-1$
			String prevName = name;
			name = newName;
			repaint();
			if (tp != null) {
				tp.changed = true;
				if (tp.dataBuilder != null) {
					tp.dataBuilder.renamePanel(prevName, newName);
				}
				if (tp.modelBuilder != null) {
					tp.modelBuilder.refreshBoosterDropdown();
				}
			}
			firePropertyChange(PROPERTY_TTRACK_NAME, prevName, name); // $NON-NLS-1$
		}
	}

	/**
	 * Gets the description of this track.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this track.
	 *
	 * @param desc a description
	 */
	public void setDescription(String desc) {
		if (desc == null)
			desc = ""; //$NON-NLS-1$
		description = desc;
	}

	/**
	 * Overrides Object toString method.
	 *
	 * @return a description of this object
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + name + " " + ID; //$NON-NLS-1$
	}

	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}

	/**
	 * Gets a message about this track to display in a message box.
	 *
	 * @return the message
	 */
	public String getMessage() {
		String s = getName();
		if (partName != null)
			s += " " + partName; //$NON-NLS-1$
		if (isLocked() && !TrackerRes.getString("PointMass.Position.Locked.Hint").equals(hint)) { //$NON-NLS-1$
			hint = TrackerRes.getString("TTrack.Locked.Hint"); //$NON-NLS-1$
		}
		if (Tracker.showHints && hint != null)
			s += " (" + hint + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}

	/**
	 * Determines whether views and track menu include this track.
	 *
	 * @param viewable <code>true</code> to include this track in views
	 */
	public void setViewable(boolean viewable) {
		this.viewable = viewable;
	}

	/**
	 * Reports whether or not this is viewable.
	 *
	 * @return <code>true</code> if this track is viewable
	 */
	public boolean isViewable() {
		return viewable;
	}

	/**
	 * Reports whether or not this is dependent. A dependent track gets some or all
	 * of its data from other tracks. Dependent tracks should override this method
	 * to return true.
	 *
	 * @return <code>true</code> if this track is dependent
	 */
	public boolean isDependent() {
		return false;
	}

	/**
	 * Sets the footprint choices. The footprint is set to the first choice.
	 *
	 * @param choices the array of Footprints available to this track
	 */
	public void setFootprints(Footprint[] choices) {
		Collection<Footprint> valid = new ArrayList<Footprint>();
		for (int i = 0; i < choices.length; i++) {
			if (choices[i] != null && choices[i].getLength() <= getFootprintLength()) {
				if (getFootprint() != null)
					choices[i].setColor(getColor());
				valid.add(choices[i]);
			}
		}
		if (valid.size() > 0) {
			footprints = valid.toArray(new Footprint[0]);
			setFootprint(footprints[0].getName());
		}
	}

	/**
	 * Sets the footprint choices. The footprint is set to the first choice. The
	 * step parameter may be used to set the footprints of secondary step arrays
	 * (veloc, accel, etc).
	 *
	 * @param choices the array of Footprints available to this track
	 * @param step    the step that identifies the step array
	 */
	public void setFootprints(Footprint[] choices, Step step) {
		setFootprints(choices);
	}

	/**
	 * Gets the footprint choices.
	 *
	 * @return the array of Footprints available to this track
	 */
	public Footprint[] getFootprints() {
		return footprints;
	}

	/**
	 * Gets the footprint choices. The step parameter may be used to get the
	 * footprints of secondary step arrays (veloc, accel, etc).
	 *
	 * @param step the step that identifies the step array
	 * @return the array of Footprints available to this track
	 */
	public Footprint[] getFootprints(Step step) {
		return footprints;
	}

	/**
	 * Adds a new footprint to the current choices.
	 *
	 * @param footprint the footprint
	 */
	public void addFootprint(Footprint footprint) {
		if (footprint.getLength() == getFootprintLength()) {
			Footprint[] prints = new Footprint[footprints.length + 1];
			System.arraycopy(footprints, 0, prints, 0, footprints.length);
			prints[footprints.length] = footprint;
			footprints = prints;
		}
	}

	/**
	 * Sets the footprint to the specified choice.
	 *
	 * @param name the name of the desired footprint
	 */
	public void setFootprint(String name) {
		if (name == null)
			return;
		String props = null;
		int n = name.indexOf("#"); //$NON-NLS-1$
		if (n > -1) {
			props = name.substring(n + 1);
			name = name.substring(0, n);
		}
		for (int i = 0; i < footprints.length; i++) {
			if (name.equals(footprints[i].getName())) {
				footprint = footprints[i];
				if (footprint instanceof CircleFootprint) {
					((CircleFootprint) footprint).setProperties(props);
				}
				Step[] stepArray = steps.array;
				for (int j = 0; j < stepArray.length; j++)
					if (stepArray[j] != null)
						stepArray[j].setFootprint(footprint);
				repaint();
				if (tp != null) {
					tp.changed = true;
					if (tp.modelBuilder != null) {
						tp.modelBuilder.refreshDropdown(null);
					}
					if (tp.dataBuilder != null) {
						org.opensourcephysics.tools.FunctionPanel panel = tp.dataBuilder.getPanel(getName());
						if (panel != null) {
							panel.setIcon(getIcon(21, 16, "track")); //$NON-NLS-1$
							tp.dataBuilder.refreshDropdown(null);
						}
					}
				}
				firePropertyChange(PROPERTY_TTRACK_FOOTPRINT, null, footprint); // $NON-NLS-1$
				return;
			}
		}
	}

	/**
	 * Gets the full name of the current footprint, including properties if
	 * available
	 *
	 * @return the footprint name
	 */
	public String getFootprintName() {
		Footprint fp = getFootprint();
		String s = fp.getName();
		if (fp instanceof CircleFootprint) {
			CircleFootprint cfp = (CircleFootprint) fp;
			s += "#" + cfp.getProperties(); //$NON-NLS-1$
		}
		return s;
	}

	/**
	 * Gets the current footprint.
	 *
	 * @return the footprint
	 */
	public Footprint getFootprint() {
		return footprint;
	}

	/**
	 * Sets the footprint to the specified choice. The step parameter may be used to
	 * set the footprints of secondary step arrays (veloc, accel, etc).
	 *
	 * @param name the name of the desired footprint
	 * @param step the step that identifies the step array
	 */
	public void setFootprint(String name, Step step) {
		setFootprint(name);
	}

	/**
	 * Gets the current footprint. The step parameter may be used to get the
	 * footprints of secondary step arrays (veloc, accel, etc).
	 *
	 * @param step the step that identifies the step array
	 * @return the footprint
	 */
	public Footprint getFootprint(Step step) {
		return getFootprint();
	}

	/**
	 * Gets this track's current icon.
	 * 
	 * @param w       the icon width
	 * @param h       the icon height
	 * @param context
	 *
	 * @return the icon
	 */
	public Icon getIcon(int w, int h, String context) {
		return getFootprint().getIcon(w, h);
	}

	/**
	 * Gets the length of the steps created by this track.
	 *
	 * @return the footprint length
	 */
	public abstract int getStepLength();

	/**
	 * Gets the length of the footprints required by this track.
	 *
	 * @return the footprint length
	 */
	public abstract int getFootprintLength();

	/**
	 * Creates a new step.
	 *
	 * @param n the frame number
	 * @param x the x coordinate in image space
	 * @param y the y coordinate in image space
	 * @return the new step
	 */
	public abstract Step createStep(int n, double x, double y);

	/**
	 * Deletes a step.
	 *
	 * @param n the frame number
	 * @return the deleted step
	 */
	public Step deleteStep(int n) {
		if (locked)
			return null;
		Step step = steps.getStep(n);
		if (step != null) {
			XMLControl control = new XMLControlElement(this);
			steps.setStep(n, null);
			for (String columnName : textColumnNames) {
				String[] entries = textColumnEntries.get(columnName);
				if (entries.length > n) {
					entries[n] = null;
				}
			}
			if (!this.isDependent())
				Undo.postTrackEdit(this, control);
			firePropertyChange(PROPERTY_TTRACK_STEP, HINT_STEP_ADDED_OR_REMOVED, new Integer(n)); // $NON-NLS-1$
		}
		return step;
	}

	/**
	 * Gets a step specified by frame number. May return null.
	 *
	 * @param n the frame number
	 * @return the step
	 */
	public Step getStep(int n) {
		return steps.getStep(n);
	}

	/**
	 * Gets next visible step after the specified step. May return null.
	 *
	 * @param step         the step
	 * @param trackerPanel the tracker panel
	 * @return the next visiblestep
	 */
	public Step getNextVisibleStep(Step step, TrackerPanel panel) {
		Step[] steps = getSteps();
		boolean found = false;
		for (int i = 0; i < steps.length; i++) {
			// return first step after found
			if (found && steps[i] != null && isStepVisible(steps[i], panel))
				return steps[i];
			// find specified step
			if (steps[i] == step)
				found = true;
		}
		// cycle back to beginning if next step not yet identified
		if (found) {
			for (int i = 0; i < steps.length; i++) {
				// return first visible step
				if (steps[i] != null && steps[i] != step && isStepVisible(steps[i], panel))
					return steps[i];
			}
		}
		return null;
	}

	/**
	 * Gets first visible step before the specified step. May return null.
	 *
	 * @param step         the step
	 * @param trackerPanel the tracker panel
	 * @return the previous visible step
	 */
	public Step getPreviousVisibleStep(Step step, TrackerPanel trackerPanel) {
		Step[] steps = getSteps();
		boolean found = false;
		for (int i = steps.length - 1; i > -1; i--) {
			// return first step after found
			if (found && steps[i] != null && isStepVisible(steps[i], trackerPanel))
				return steps[i];
			// find specified step
			if (steps[i] == step)
				found = true;
		}
		// cycle back to end if previous step not yet identified
		if (found) {
			for (int i = steps.length - 1; i > -1; i--) {
				// return first visible step
				if (steps[i] != null && steps[i] != step && isStepVisible(steps[i], trackerPanel))
					return steps[i];
			}
		}
		return null;
	}

	/**
	 * Gets a step containing a TPoint. May return null.
	 *
	 * @param point        a TPoint
	 * @param trackerPanel ignored
	 * @return the step containing the TPoint
	 */
	public Step getStep(TPoint point, TrackerPanel trackerPanel) {
		if (point == null)
			return null;
		Step[] stepArray = steps.array;
		for (int j = 0; j < stepArray.length; j++)
			if (stepArray[j] != null) {
				TPoint[] points = stepArray[j].getPoints();
				for (int i = 0; i < points.length; i++)
					if (points[i] == point)
						return stepArray[j];
			}
		return null;
	}

	/**
	 * Gets the step array. Some or all elements may be null.
	 *
	 * @return the step array
	 */
	public Step[] getSteps() {
		return steps.array;
	}

	/**
	 * Returns true if the step at the specified frame number is complete. Points
	 * may be created or remarked if false.
	 *
	 * @param n the frame number
	 * @return <code>true</code> if the step is complete, otherwise false
	 */
	public boolean isStepComplete(int n) {
		return false; // enables remarking
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
		createStep(n, x, y);
		return getMarkedPoint(n, getTargetIndex());
	}

	/**
	 * Used by autoTracker to get the marked point for a given frame and index.
	 * 
	 * @param n     the frame number
	 * @param index the index
	 * @return the step TPoint at the index
	 */
	public TPoint getMarkedPoint(int n, int index) {
		Step step = getStep(n);
		if (step == null)
			return null;
		return step.getPoints()[index];
	}

	/**
	 * Returns the target index for the autotracker.
	 *
	 * @return the point index
	 */
	protected int getTargetIndex() {
		return targetIndex;
	}

	/**
	 * Sets the target index for the autotracker.
	 *
	 * @param index the point index
	 */
	protected void setTargetIndex(int index) {
		if (isAutoTrackable(index))
			targetIndex = index;
	}

	/**
	 * Sets the target index for the autotracker.
	 *
	 * @param description the description of the target
	 */
	protected void setTargetIndex(String description) {
		for (int i = 0; i < getStepLength(); i++) {
			if (description.equals(getTargetDescription(i))) {
				setTargetIndex(i);
				break;
			}
		}
	}

	/**
	 * Sets the target index for the autotracker.
	 *
	 * @param p a TPoint associated with a step in this track
	 */
	protected void setTargetIndex(TPoint p) {
		Step step = getStep(p, tp);
		if (step != null)
			setTargetIndex(step.getPointIndex(p));
	}

	/**
	 * Returns a description of a target point with a given index.
	 *
	 * @param pointIndex the index
	 * @return the description
	 */
	protected String getTargetDescription(int pointIndex) {
		return null;
	}

	/**
	 * Determines if the given point index is autotrackable.
	 *
	 * @param pointIndex the points[] index
	 * @return true if autotrackable
	 */
	protected boolean isAutoTrackable(int pointIndex) {
		return true; // true by default--subclasses override
	}

	/**
	 * Determines if at least one point in this track is autotrackable.
	 *
	 * @return true if autotrackable
	 */
	protected boolean isAutoTrackable() {
		return false; // false by default--subclasses override
	}

	/**
	 * Returns true if this track contains no steps.
	 *
	 * @return <code>true</code> if this contains no steps
	 */
	public boolean isEmpty() {
		Step[] array = steps.array;
		for (int n = 0; n < array.length; n++)
			if (array[n] != null)
				return false;
		return true;
	}

	/**
	 * Returns an array of NumberFields {x, y, magnitude, angle} for a given step.
	 *
	 * @param step the step
	 * @return the number fields
	 */
	protected NumberField[] getNumberFieldsForStep(Step step) {
		return positionFields;
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	public void setFontLevel(int level) {
		Object[] objectsToSize = new Object[] { tLabel, xLabel, yLabel, magLabel, angleLabel, stepLabel, tValueLabel,
				stepValueLabel, tField, xField, yField, magField, angleField };
		FontSizer.setFonts(objectsToSize);
		erase();
	}

	/**
	 * Returns the DatasetManager.
	 *
	 * @param panel the tracker panel
	 * @return the DatasetManager
	 */
	public DatasetManager getData(TrackerPanel panel) {
		if (datasetManager == null) {
			datasetManager = new DatasetManager(true);
			datasetManager.setSorted(true);
		}
		if (refreshDataLater || dataValid)
			return datasetManager;
		dataValid = true;
		// refresh track data
		refreshData(datasetManager, panel);
		// check for newly loaded dataFunctions
		if (constantsLoadedFromXML != null) {
			for (int i = 0; i < constantsLoadedFromXML.length; i++) {
				String name = (String) constantsLoadedFromXML[i][0];
				double val = (Double) constantsLoadedFromXML[i][1];
				String expression = (String) constantsLoadedFromXML[i][2];
				String desc = constantsLoadedFromXML[i].length < 4 ? null : (String) constantsLoadedFromXML[i][3];
				datasetManager.setConstant(name, val, expression, desc);
			}
			constantsLoadedFromXML = null;
		}
		if (dataProp != null) {
			XMLControl[] children = dataProp.getChildControls();
			outer: for (int i = 0; i < children.length; i++) {
				// compare function name with existing datasets to avoid duplications
				String name = children[i].getString("function_name"); //$NON-NLS-1$
				for (Dataset next : datasetManager.getDatasetsRaw()) {
					if (next instanceof DataFunction && next.getYColumnName().equals(name)) {
						continue outer;
					}
				}
				DataFunction f = new DataFunction(datasetManager);
				children[i].loadObject(f);
				f.setXColumnVisible(false);
				datasetManager.addDataset(f);
			}
			dataProp = null;
		}
		// refresh dataFunctions
		ArrayList<Dataset> datasets = datasetManager.getDatasetsRaw();
		for (int i = 0; i < datasets.size(); i++) {
			if (datasets.get(i) instanceof DataFunction) {
				((DataFunction) datasets.get(i)).refreshFunctionData();
			}
		}
		DataTool tool = DataTool.getTool(false);
		if (panel != null && tool != null && tool.isVisible() && tool.getSelectedTab() != null
				&& tool.getSelectedTab().isInterestedIn(datasetManager)) {
			tool.getSelectedTab().refreshData();
		}
		return datasetManager;
	}
	
	/**
	 * Returns the DatasetManager for a specified Dataset index, if supported.
	 * This default implementation ignores the index.
	 *
	 * @param panel the tracker panel
	 * @param datasetIndex
	 * @return the DatasetManager
	 */
	public DatasetManager getData(TrackerPanel panel, int datasetIndex) {
		return getData(panel);
	}

	/**
	 * Refreshes the data in the specified DatasetManager. Subclasses should use
	 * this method to refresh track-specific data sets.
	 *
	 * @param data         the DatasetManager
	 * @param trackerPanel the tracker panel
	 */
	protected void refreshData(DatasetManager data, TrackerPanel panel) {
		/** empty block */
	}

	/**
	 * Refreshes the data for a specified frame range. This default implementation
	 * ignores the range arguments.
	 *
	 * @param data         the DatasetManager
	 * @param trackerPanel the tracker panel
	 * @param startFrame   the start frame
	 * @param stepCount    the step count
	 */
	protected void refreshData(DatasetManager data, TrackerPanel trackerPanel, int startFrame, int stepCount) {
		refreshData(data, trackerPanel);
	}

	/**
	 * Gets the name of a data variable. Index zero is the shared x-variable,
	 * indices 1-n+1 are the y-variables.
	 *
	 * @param index the dataset index
	 * @return a String data name
	 */
	public String getDataName(int index) {
		if (index == 0) { // shared x-variable
			return datasetManager.getDataset(0).getXColumnName();
		}
		if (index < datasetManager.getDatasetsRaw().size() + 1) {
			return datasetManager.getDataset(index - 1).getYColumnName();
		}
		return null;
	}

	/**
	 * Gets the description of a data variable. Index zero is the shared x-variable,
	 * indices 1-n+1 are the y-variables. Subclasses should override to provide
	 * correct descriptions.
	 *
	 * @param index the dataset index
	 * @return a String data description
	 */
	public String getDataDescription(int index) {
		if (dataDescriptions == null)
			return ""; //$NON-NLS-1$
		if (index >= dataDescriptions.length) {
			ArrayList<Dataset> datasets = datasetManager.getDatasetsRaw();
			index--;
			if (index < datasets.size() && datasets.get(index) instanceof DataFunction) {
				String desc = datasets.get(index).getYColumnDescription();
				if (desc == null)
					desc = ""; //$NON-NLS-1$
				return desc;
			}
			return ""; //$NON-NLS-1$
		}
		return dataDescriptions[index];
	}

	/**
	 * Gets the preferred order of data table columns.
	 * 
	 * @return a list of column indices in preferred order
	 */
	public ArrayList<Integer> getPreferredDataOrder() {
		ArrayList<Integer> orderedData = new ArrayList<Integer>();
		int n = datasetManager.getDatasetsRaw().size();
		if (preferredColumnOrder != null) {
			// first add preferred indices
			for (int i = 0; i < preferredColumnOrder.length; i++) {
				if (!orderedData.contains(preferredColumnOrder[i]) // prevent duplicates
						&& preferredColumnOrder[i] < n) // prevent invalid indices
					orderedData.add(preferredColumnOrder[i]);
			}
		}
		// add indices not yet in array
		for (int i = 0; i < n; i++) {
			if (!orderedData.contains(i)) {
				orderedData.add(i);
			}
		}
		return orderedData;
	}

	/**
	 * Gets the frame number associated with specified variables and values.
	 *
	 * @param xVar     the x-variable name (required)
	 * @param yVar     the y-variable name (may be null)
	 * @param xyValues values array (length 1 or 2)
	 * @return the frame number, or -1 if not found
	 */
	public int getFrameForData(String xVar, String yVar, double[] xyValues) {
		if (dataFrames.isEmpty() || datasetManager.getDatasetsRaw().isEmpty())
			return -1;
		Dataset dataset = datasetManager.getDataset(0);
		double x = xyValues[0];
		if (xVar.equals(dataset.getXColumnName())) {
			int nf = dataFrames.size();
			// for independent variable, ignore yVar
			double[] vals = dataset.getXPointsRaw();
			for (int i = 0, n = dataset.getIndex(); i < n; i++) {
				if (x == vals[i]) {
					return (i < nf ? dataFrames.get(i).intValue() : -1);
				}
			}
			return -1;
		}
		// not independent variable, so find match in xVar dataset
		int index = datasetManager.getDatasetIndex(xVar);
		if (index < 0) {
			return -1;
		}
		dataset = datasetManager.getDataset(index);
		double[] xVals = dataset.getYPointsRaw();
		double[] yVals = null;
		double y = (yVar == null ? Double.NaN : xyValues[1]);
		for (int i = 0, n = dataset.getIndex(); i < n; i++) {
			if (x == xVals[i]) {
				// found matching value
				int frame = (i < dataFrames.size() ? dataFrames.get(i).intValue() : -1);
				// if yVar value is given, verify it matches as well
				if (yVar != null) {
					if (yVals == null) {
						yVals = datasetManager.getDataset(datasetManager.getDatasetIndex(yVar)).getYPoints();
					}
					// if y value doesn't also match, reject and continue searching
					if (y != yVals[i]) {
						continue;
					}
				}
				return frame;
			}
		}
		return -1;
	}
	
	public void refreshDecimalSeparators() {
		for (String key: numberFields.keySet()) {
			NumberField[] fields = numberFields.get(key);
			for (int i = 0; i < fields.length; i++) {
				fields[i].refreshDecimalSeparators(true);
			}
		}
	}


	/**
	 * Gets a map of number fields by name.
	 * 
	 * @return a map of name to NumberField.
	 */
	protected Map<String, NumberField[]> getNumberFields() {
		return numberFields;
	}

	/**
	 * Gets a list of all variable names for a given track type.
	 * 
	 * @return an ArrayList of names. May be empty.
	 */
	protected static ArrayList<String> getAllVariables(int ttype) {
		switch (ttype) {
		case TYPE_CALIBRATION:
			return Calibration.allVariables;
		case TYPE_CIRCLEFITTER:
			return CircleFitter.allVariables;
		case TYPE_COORDAXES:
			return CoordAxes.allVariables;
		case TYPE_LINEPROFILE:
			return LineProfile.allVariables;
		case TYPE_OFFSETORIGIN:
			return OffsetOrigin.allVariables;
		case TYPE_POINTMASS:
			return PointMass.allVariables;
		case TYPE_PROTRACTOR:
			return Protractor.allVariables;
		case TYPE_RGBREGION:
			return RGBRegion.allVariables;
		case TYPE_TAPEMEASURE:
			return TapeMeasure.allVariables;
		case TYPE_VECTOR:
			return Vector.allVariables;
		default:
		case TYPE_PERSPECTIVE:
			return NOVARA;
		}
	}

	public static int getBaseTypeInt(String type) {
		type = type.substring(type.lastIndexOf(".") + 1);
		for (int i = baseTrackTypes.length; --i >= 0;)
			if (baseTrackTypes[i].equals(type))
				return i;
		return TYPE_UNKNOWN;
	}

	protected static ArrayList<String> createAllVariables(String[] datavars, String[] fieldvars) {
		ArrayList<String> list = new ArrayList<String>();
		if (datavars != null)
			for (String next : datavars) {
				list.add(next);
			}
		if (fieldvars != null)
			for (String next : fieldvars) {
				if (!list.contains(next)) {
					list.add(next);
				}
			}
		return list;
	}

	/**
	 * Gets an array of variables to format for a given track type and formatter
	 * display name.
	 * 
	 * @return an array of variables. May be null.
	 */
	protected String[] getVariablesFromFormatterDisplayName(String name) {
		return getFormatMap().get(name);
	}

	/**
	 * Gets the text column names.
	 * 
	 * @return list of column names.
	 */
	public ArrayList<String> getTextColumnNames() {
		return textColumnNames;
	}

	/**
	 * Adds a new text column.
	 * 
	 * @param name the name
	 * @return true if a new column was added
	 */
	public boolean addTextColumn(String name) {
		// only add new, non-null names
		if (name == null || name.trim().equals("")) //$NON-NLS-1$
			return false;
		name = name.trim();
		for (String next : textColumnNames) {
			if (next.equals(name))
				return false;
		}
		XMLControl control = new XMLControlElement(this);
		control.setValue("isTextColumn", true);
		textColumnNames.add(name);
		textColumnEntries.put(name, new String[0]);
		Undo.postTrackEdit(this, control);
		tp.changed = true;
		firePropertyChange(PROPERTY_TTRACK_TEXTCOLUMN, null, name);
		return true;
	}

	/**
	 * Removes a named text column.
	 * 
	 * @param name the name
	 * @return true if the column was removed
	 */
	public boolean removeTextColumn(String name) {
		if (name == null)
			return false;
		name = name.trim();
		for (String next : textColumnNames) {
			if (next.equals(name)) {
				XMLControl control = new XMLControlElement(this);
				textColumnEntries.remove(name);
				textColumnNames.remove(name);
				Undo.postTrackEdit(this, control);
				tp.changed = true;
				firePropertyChange(PROPERTY_TTRACK_TEXTCOLUMN, name, null); // $NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * Renames a text column.
	 * 
	 * @param name    the existing name
	 * @param newName the new name
	 * @return true if renamed
	 */
	public boolean renameTextColumn(String name, String newName) {
		if (name == null)
			return false;
		name = name.trim();
		if (newName == null || newName.trim().equals("")) //$NON-NLS-1$
			return false;
		newName = newName.trim();
		for (String next : textColumnNames) {
			if (next.equals(newName))
				return false;
		}
		for (int i = 0; i < textColumnNames.size(); i++) {
			String next = textColumnNames.get(i);
			if (name.equals(next)) {
				// found column to change
				XMLControl control = new XMLControlElement(this);
				textColumnNames.remove(name);
				textColumnNames.add(i, newName);
				String[] entries = textColumnEntries.remove(name);
				textColumnEntries.put(newName, entries);
				Undo.postTrackEdit(this, control);
			}
		}
		tp.changed = true;
		firePropertyChange(PROPERTY_TTRACK_TEXTCOLUMN, name, newName); // $NON-NLS-1$
		return true;
	}

	/**
	 * Gets the entry in a text column for a specified frame.
	 * 
	 * @param columnName  the column name
	 * @param frameNumber the frame number
	 * @return the text entry (may be null)
	 */
	public String getTextColumnEntry(String columnName, int frameNumber) {
		// return null if frame number out of bounds
		if (frameNumber < 0)
			return null;
		String[] entries = textColumnEntries.get(columnName);
		// return null if text column or entry index not defined
		if (entries == null)
			return null;
		if (frameNumber > entries.length - 1)
			return null;
		return entries[frameNumber];
	}

	/**
	 * Sets the text in a text column for a specified frame.
	 * 
	 * @param columnName  the column name
	 * @param frameNumber the frame number
	 * @param text        the text (may be null)
	 * @return true if the text was changed
	 */
	public boolean setTextColumnEntry(String columnName, int frameNumber, String text) {
		if (isLocked())
			return false;
		// return if frame number out of bounds
		if (frameNumber < 0)
			return false;
		String[] entries = textColumnEntries.get(columnName);
		// return if text column not defined
		if (entries == null)
			return false;

		if (text.trim().equals("")) //$NON-NLS-1$
			text = null;
		else
			text = text.trim();

		XMLControl control = new XMLControlElement(this);
		if (frameNumber > entries.length - 1) {
			// increase size of entries array
			String[] newEntries = new String[frameNumber + 1];
			System.arraycopy(entries, 0, newEntries, 0, entries.length);
			entries = newEntries;
			textColumnEntries.put(columnName, entries);
		}

		String prev = entries[frameNumber];
		if (prev == text || (prev != null && prev.equals(text)))
			return false;
		// change text entry and fire property change
		entries[frameNumber] = text;
		Undo.postTrackEdit(this, control);
		tp.changed = true;
		firePropertyChange(PROPERTY_TTRACK_TEXTCOLUMN, null, null); // $NON-NLS-1$
		return true;
	}

	protected int getAttachmentLength() {
		return 0;
	}

	/**
	 * Returns the array of attachments for this track. Returns null only if the
	 * specified number of attachments == 0.
	 * 
	 * @return the attachments array
	 */
	public TTrack[] getAttachments() {
		int n = getAttachmentLength();
		if (n > 0) {
			if (attachments == null) {
				attachments = new TTrack[n];
			}
			if (attachments.length < n) {
				TTrack[] newAttachments = new TTrack[n];
				System.arraycopy(attachments, 0, newAttachments, 0, attachments.length);
				attachments = newAttachments;
			}
		}
		return attachments;
	}

	/**
	 * Returns the description of a particular attachment point.
	 * 
	 * @param n the attachment point index
	 * @return the description
	 */
	public String getAttachmentDescription(int n) {
		return TrackerRes.getString("AttachmentInspector.Label.End") + " " + (n + 1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Loads the attachments for this track based on attachmentNames array, if any.
	 * 
	 * @param refresh true to refresh attachments after loading
	 * @return true if attachments were loaded
	 */
	protected boolean loadAttachmentsFromNames(boolean refresh) {
		// if track attachmentNames is not null then find tracks and populate
		// attachments
		int n;
		if (attachmentNames == null || (n = attachmentNames.length) == 0)
			return false;
		boolean foundAll = true;
		TTrack[] temp = new TTrack[n];
		ArrayList<TTrack> tracks = tp.getTracksTemp();
		for (int i = 0; i < n; i++) {
			// BH 2020.10.17 OK?
			String name = attachmentNames[i];
			if (name == null)
				continue;
			TTrack track = tp.getTrack(name, tracks);
			if (track == null) {
				foundAll = false;
				break;
			}
			temp[i] = track;
//			TTrack track = trackerPanel.getTrack(attachmentNames[i]);
//			if (track != null) {
//				temp[i] = track;
//			} else if (attachmentNames[i] != null) {
//				foundAll = false;
//			}
		}
		tracks.clear();
		if (foundAll) {
			attachments = temp;
			attachmentNames = null;
			if (refresh)
				refreshAttachmentsLater();
		}
		return foundAll;
	}

	/**
	 * Refreshes the attachments for this track after a delay. This should be used
	 * only when loading attachments from Names during loading
	 */
	protected void refreshAttachmentsLater() {
		// use timer with 2 second delay
		OSPRuntime.trigger(2000, (e) -> {
				// save changed state
				boolean changed = tp != null && tp.changed;
				refreshAttachments();
				if (tp != null) {
					// restore changed state
					tp.changed = changed;
				}
		});
	}

	/**
	 * Determines if all attachments are non-null
	 * 
	 * @return true if all attachments are non-null.
	 */
	protected boolean isFullyAttached() {
		int n = getAttachmentLength();
		if (n > 0) {
			TTrack[] attached = getAttachments();
			for (int i = 0; i < n; i++) {
				if (attached[i] == null)
					return false;
			}
		}
		return true;
	}

	/**
	 * Determines if this is attached to one or more tracks.
	 *
	 * @return true if attached
	 */
	public boolean isAttached() {
		TTrack[] attachments = getAttachments();
		for (int i = 0; i < attachments.length; i++) {
			if (attachments[i] != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Refreshes the attachments for this track.
	 */
	protected void refreshAttachments() {
		if (attachments == null || getAttachmentLength() == 0)
			return;

		// unfix the track if it has attachments
		if (isAttached())
			setFixedPosition(false);

		VideoClip clip = tp.getPlayer().getVideoClip();
		for (int i = 0; i < attachments.length; i++) {
			TTrack targetTrack = attachments[i];
			if (targetTrack != null) {
				targetTrack.removeStepListener(this); // $NON-NLS-1$
				targetTrack.addStepListener(this); // $NON-NLS-1$
				// attach/detach points
				for (int n = clip.getStartFrameNumber(); n <= clip.getEndFrameNumber(); n++) {
					Step targetStep = targetTrack.getStep(n);
					Step step = getStep(n);
					if (step == null)
						continue;
					TPoint p = getPoint(step, i); // not for CircleFitter--see overridden method
//					if (targetStep == null || !targetStep.valid) {
					if (targetStep == null) {
						if (p != null) {
							p.detach();
						}
					} else if (p != null) {
						TPoint target = targetStep.getPoints()[0];
						p.attachTo(target);
					}
				}
			} else { // target track is null
				for (int n = clip.getStartFrameNumber(); n <= clip.getEndFrameNumber(); n++) {
					Step step = getStep(n);
					if (step == null)
						continue;
					TPoint p = getPoint(step, i);
					if (p != null) {
						p.detach();
					}
				}
			}
		}
		tp.refreshTrackBar();
//		TTrackBar.getTrackbar(trackerPanel).refresh();
//	refreshFields(trackerPanel.getFrameNumber());
	}

	private TPoint getPoint(Step step, int i) {
		TPoint[] pts = step.points;
		return (pts == null || i >= pts.length ? null : pts[i]);
	}

	protected void setFixedPosition(boolean b) {
		// see TapeMeasure and Protractor
	}

	/**
	 * Prepares menu items and adds them to a menu. Subclasses should override this
	 * method and add track-specific menu items.
	 *
	 * @param trackerPanel the tracker panel
	 * @param menu         the menu. If null, a dynamic menu is returned that adds
	 *                     items only when selected
	 * @return a menu
	 */
	public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu) {
		if (menu == null) {
			// dynamic
			JMenu menu0 = new JMenu();
			menu0.setText(getName("track"));
			menu0.setIcon(getFootprint().getIcon(21, 16));
			menu0.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
//					if (visibleItem != null)
//						return;
					menu0.removeAll();
					getMenu(trackerPanel, menu0);
					FontSizer.setMenuFonts(menu0);
				}

				@Override
				public void menuDeselected(MenuEvent e) {
				}

				@Override
				public void menuCanceled(MenuEvent e) {
				}

			});
			return menu0;
		}
		menu.setText(getName("track"));
		menu.setIcon(getFootprint().getIcon(21, 16));
		// prepare menu items
		getMenuItems();
		visibleItem.setText(TrackerRes.getString("TTrack.MenuItem.Visible")); //$NON-NLS-1$
		trailVisibleItem.setText(TrackerRes.getString("TTrack.MenuItem.TrailVisible")); //$NON-NLS-1$
		autoAdvanceItem.setText(TrackerRes.getString("TTrack.MenuItem.Autostep")); //$NON-NLS-1$
		markByDefaultItem.setText(TrackerRes.getString("TTrack.MenuItem.MarkByDefault")); //$NON-NLS-1$
		lockedItem.setText(TrackerRes.getString("TTrack.MenuItem.Locked")); //$NON-NLS-1$
		deleteTrackItem.setText(TrackerRes.getString("TTrack.MenuItem.Delete")); //$NON-NLS-1$
		deleteStepItem.setText(TrackerRes.getString("TTrack.MenuItem.DeletePoint")); //$NON-NLS-1$
		clearStepsItem.setText(TrackerRes.getString("TTrack.MenuItem.ClearSteps")); //$NON-NLS-1$
		colorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
		nameItem.setText(TrackerRes.getString("TTrack.MenuItem.Name")); //$NON-NLS-1$
		footprintMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
		descriptionItem.setText(TrackerRes.getString("TTrack.MenuItem.Description")); //$NON-NLS-1$
		dataBuilderItem.setText(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
		visibleItem.setSelected(isVisible());
		lockedItem.setSelected(isLocked());
		trailVisibleItem.setSelected(isTrailVisible());
		markByDefaultItem.setSelected(isMarkByDefault());
		autoAdvanceItem.setSelected(isAutoAdvance());
		lockedItem.setEnabled(true);
		boolean cantDeleteSteps = isLocked() || isDependent();
		TPoint p = trackerPanel.getSelectedPoint();
		Step step = getStep(p, trackerPanel);

		deleteStepItem.setEnabled(!cantDeleteSteps && step != null);
		clearStepsItem.setEnabled(!cantDeleteSteps);
		deleteTrackItem.setEnabled(!(isLocked() && !isDependent()));
		nameItem.setEnabled(!(isLocked() && !isDependent()));
		footprintMenu.removeAll();
		Footprint[] fp = getFootprints();
		JMenuItem item;
		for (int i = 0; i < fp.length; i++) {
			item = new JMenuItem(fp[i].getDisplayName(), fp[i].getIcon(21, 16));
			item.setActionCommand(fp[i].getName());
			if (fp[i] instanceof CircleFootprint) {
				item.setText(fp[i].getDisplayName() + "..."); //$NON-NLS-1$
				item.addActionListener(circleFootprintListener);
			} else {
				item.addActionListener(footprintListener);
			}
			if (fp[i] == footprint) {
				item.setBorder(BorderFactory.createLineBorder(item.getBackground().darker()));
			}
			footprintMenu.add(item);
		}
		// add name and description items
		if (trackerPanel.isEnabled("track.name") || //$NON-NLS-1$
				trackerPanel.isEnabled("track.description")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
			if (trackerPanel.isEnabled("track.name")) //$NON-NLS-1$
				menu.add(nameItem);
			if (trackerPanel.isEnabled("track.description")) //$NON-NLS-1$
				menu.add(descriptionItem);
		}
		// add color and footprint items
		if (trackerPanel.isEnabled("track.color") || //$NON-NLS-1$
				trackerPanel.isEnabled("track.footprint")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
			if (trackerPanel.isEnabled("track.color")) //$NON-NLS-1$
				menu.add(colorItem);
			if (trackerPanel.isEnabled("track.footprint")) //$NON-NLS-1$
				menu.add(footprintMenu);
		}
		// add visible, trail and locked items
		if (trackerPanel.isEnabled("track.visible") || //$NON-NLS-1$
				trackerPanel.isEnabled("track.locked")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
			if (trackerPanel.isEnabled("track.visible")) //$NON-NLS-1$
				menu.add(visibleItem);
			if (trackerPanel.isEnabled("track.locked")) //$NON-NLS-1$
				menu.add(lockedItem);
		}
		// add dataBuilder item if viewable and enabled
		if (this.isViewable() && trackerPanel.isEnabled("data.builder")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
			menu.add(dataBuilderItem);

		}
		// add clear steps and delete items
		if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
			TMenuBar.checkAddMenuSep(menu);
			menu.add(deleteTrackItem);
		}
		return menu;
	}

	protected void getMenuItems() {
		if (visibleItem != null)
			return;
		// create menu items
		visibleItem = new JCheckBoxMenuItem();
		trailVisibleItem = new JCheckBoxMenuItem();
		autoAdvanceItem = new JCheckBoxMenuItem();
		markByDefaultItem = new JCheckBoxMenuItem();
		lockedItem = new JCheckBoxMenuItem();
		deleteTrackItem = new JMenuItem();
		deleteStepItem = new JMenuItem();
		clearStepsItem = new JMenuItem();
		colorItem = new JMenuItem();
		colorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = getColor();
				OSPRuntime.chooseColor(color, TrackerRes.getString("TTrack.Dialog.Color.Title"), (newColor) -> { //$NON-NLS-1$
				if (newColor != color) {
					XMLControl control = new XMLControlElement(new TrackProperties(TTrack.this));
					setColor(newColor);
					Undo.postTrackDisplayEdit(TTrack.this, control);
				}
				});
			}
		});

		nameItem = new JMenuItem();
		nameItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getNameDialog().setVisible(true);
			}
		});
		footprintMenu = new JMenu();
		descriptionItem = new JMenuItem();
		descriptionItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tp != null && tframe != null) {
					if (tframe.notesVisible()) {
						tframe.getNotesDialog().setVisible(true);
					} else
						tp.getToolBar(true).doNotesAction();
				}
			}
		});
		dataBuilderItem = new JMenuItem();
		dataBuilderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tp != null) {
					tp.getDataBuilder().setSelectedPanel(getName());
					tp.getDataBuilder().setVisible(true);
				}
			}
		});
		visibleItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setVisible(visibleItem.isSelected());
				repaint();
			}
		});
		trailVisibleItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setTrailVisible(trailVisibleItem.isSelected());
				if (!TTrack.this.isTrailVisible()) {
					for (int j = 0; j < tp.andWorld.size(); j++) {
						TrackerPanel panel = panel(tp.andWorld.get(j));
						Step step = panel.getSelectedStep();
						if (step != null && step.getTrack() == TTrack.this) {
							if (!(step.getFrameNumber() == panel.getFrameNumber())) {
								panel.setSelectedPoint(null);
								panel.selectedSteps.clear();
							}
						}
					}
				}
				TTrack.this.repaint();
			}
		});
		markByDefaultItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setMarkByDefault(markByDefaultItem.isSelected());
			}
		});
		autoAdvanceItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setAutoAdvance(autoAdvanceItem.isSelected());
			}
		});
		lockedItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLocked(lockedItem.isSelected());
			}
		});
		deleteTrackItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		});
		deleteStepItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tp.deletePoint(tp.getSelectedPoint());
			}
		});
		clearStepsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isLocked())
					return;
				XMLControl control = new XMLControlElement(TTrack.this);
				for (int n = 0; n < getSteps().length; n++) {
					steps.setStep(n, null);
				}
				for (String columnName : textColumnNames) {
					textColumnEntries.put(columnName, new String[0]);
				}
				Undo.postTrackEdit(TTrack.this, control);
				if (TTrack.this.ttype == TTrack.TYPE_POINTMASS) {
					PointMass p = (PointMass) TTrack.this;
					p.updateDerivatives();
				}
				AutoTracker autoTracker = tp.getAutoTracker(false);
				if (autoTracker != null) {
					if (autoTracker.getTrack() == TTrack.this)
						autoTracker.reset();
					autoTracker.getWizard().setVisible(false);
				}
				fireStepsChanged();
				TFrame.repaintT(tp);
			}
		});

	}

	/**
	 * Returns an empty list of track-related toolbar components. Subclasses should
	 * override this method and add track-specific components.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a collection of components
	 */
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		String tooltip = TrackerRes.getString("TTrack.NumberField.Format.Tooltip"); //$NON-NLS-1$
		if (OSPRuntime.isMac()) {
			tooltip = TrackerRes.getString("TTrack.NumberField.Format.Tooltip.OSX"); //$NON-NLS-1$
		}
		for (NumberField[] fields : getNumberFields().values()) {
			for (int i = 0; i < fields.length; i++) {
				fields[i].setToolTipText(tooltip);
			}
		}
		tField.setUnits(trackerPanel.getUnits(this, "t")); //$NON-NLS-1$
		toolbarTrackComponents.clear();
		return toolbarTrackComponents;
	}

	/**
	 * Returns an empty list of point-related toolbar components. Subclasses should
	 * override this method and add point-specific components.
	 *
	 * @param trackerPanel the tracker panel
	 * @param point        the TPoint
	 * @return a list of components
	 */
	public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel, TPoint point) {
		toolbarPointComponents.clear();
		stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
		// put step time into tField
		Step step = getStep(point, trackerPanel);
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		if (step != null && clip.includesFrame(step.getFrameNumber())) {
			int n = clip.frameToStep(step.getFrameNumber());
			stepValueLabel.setText(n + ":"); //$NON-NLS-1$
			double t = trackerPanel.getPlayer().getStepTime(n) / 1000;
			if (t >= 0) {
				tField.setValue(t);
			}
		}
		// set tooltip for angle field
		angleField.setToolTipText(
				angleField.getConversionFactor() == 1 ? TrackerRes.getString("TTrack.AngleField.Radians.Tooltip") : //$NON-NLS-1$
						TrackerRes.getString("TTrack.AngleField.Degrees.Tooltip")); //$NON-NLS-1$
		return toolbarPointComponents;
	}

	/**
	 * Erases all steps on all panels.
	 */
	public void erase() {
		Step[] stepArray = steps.array;
		for (int j = 0; j < stepArray.length; j++)
			if (stepArray[j] != null)
				stepArray[j].erase();
		if (tp != null && tp.autoTracker != null) {
			AutoTracker autoTracker = tp.getAutoTracker(false);
			if (autoTracker != null && autoTracker.getWizard().isVisible() && autoTracker.getTrack() == this) {
				autoTracker.erase();
			}
		}
	}

	/**
	 * Remarks all steps on all panels.
	 */
	public void remark() {
		Step[] stepArray = steps.array;
		for (int j = 0; j < stepArray.length; j++)
			if (stepArray[j] != null)
				stepArray[j].remark();
	}

	/**
	 * Repaints all steps on all panels.
	 */
	public void repaint() {
		if (tp == null || !tp.isPaintable())
			return;
		remark();
		for (int i = 0; i < tp.andWorld.size(); i++) {
			panel(tp.andWorld.get(i)).repaintDirtyRegion();
		}
	}
	
	/**
	 * Schedule repainting of all panel and world views associated with this track.
	 */
	protected void repaintAll() {
		if (tp != null)
			for (int i = 0; i < tp.andWorld.size(); i++) {
				panel(tp.andWorld.get(i)).repaint();
			}
	}


	/**
	 * Erases all steps on the specified panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void erase(Integer panelID) {
		Step[] stepArray = steps.array;
		for (int j = 0; j < stepArray.length; j++)
			if (stepArray[j] != null)
				stepArray[j].erase(panelID);
		TrackerPanel panel = panel(panelID);
		AutoTracker autoTracker = panel.autoTracker;
		if (autoTracker != null) {
			if (autoTracker.getWizard().isVisible() && autoTracker.getTrack() == this) {
				autoTracker.erase();
			}
		}
	}

	/**
	 * Remarks all steps on the specified panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void remark(Integer panelID) {
		Step[] stepArray = steps.array;
		for (int j = 0; j < stepArray.length; j++)
			if (stepArray[j] != null)
				stepArray[j].remark(panelID);
	}

	/**
	 * Repaints all steps on the specified panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void repaint(Integer panelID) {
		remark(panelID);
		panel(panelID).repaintDirtyRegion();
	}

//	/**
//	 * Repaints all steps on the specified panel.
//	 *
//	 * @param trackerPanel the tracker panel
//	 */
//	public void repaint(TrackerPanel panel) {
//		remark(panel.getID());
//		panel.repaintDirtyRegion();
//	}

	/**
	 * Repaints the specified step on all panels. This should be used instead of the
	 * Step.repaint() method to paint a new step on all panels for the first time,
	 * since a new step does not know what panels it is drawn on whereas the track
	 * does.
	 *
	 * @param step the step
	 */
	public void repaintStep(Step step) {
		for (int j = 0; j < tp.andWorld.size(); j++) {
			step.repaint(tp.andWorld.get(j));
		}
	}

	/**
	 * Draws the steps on the tracker panel.
	 *
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		loadAttachmentsFromNames(true);
		if (!visible || !(panel instanceof TrackerPanel))
			return;
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		Graphics2D g = (Graphics2D) _g;
		int n = trackerPanel.getFrameNumber();
		int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
		if (trailVisible) {
			boolean shortTrail = getTrailLength() > 0;
			Step[] stepArray = steps.array;
			for (int frame = 0; frame < stepArray.length; frame++) {
				if (shortTrail && (n - frame > (getTrailLength() - 1) * stepSize || frame > n))
					continue;
				if (stepArray[frame] != null && trackerPanel.getPlayer().getVideoClip().includesFrame(frame))
					stepArray[frame].draw(trackerPanel, g);
			}
		} else {
			Step step = getStep(n);
			if (step != null)
				step.draw(trackerPanel, g);
		}
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
		if (!(panel instanceof TrackerPanel) || !visible)
			return null;
		TrackerPanel trackerPanel = (TrackerPanel) panel;
		Interactive iad = null;
		int n = trackerPanel.getFrameNumber();
		int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
		if (trailVisible) {
			boolean shortTrail = getTrailLength() > 0;
			Step[] stepArray = steps.array;
			for (int frame = 0; frame < stepArray.length; frame++) {
				if (shortTrail && (n - frame > (getTrailLength() - 1) * stepSize || frame > n))
					continue;
				if (stepArray[frame] != null && trackerPanel.getPlayer().getVideoClip().includesFrame(frame)) {
					iad = stepArray[frame].findInteractive(trackerPanel, xpix, ypix);
					if (iad != null)
						return iad;
				}
			}
		} else {
			Step step = getStep(n);
			if (step != null && trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
				iad = step.findInteractive(trackerPanel, xpix, ypix);
				if (iad != null)
					return iad;
			}
		}
		return null;
	}

	/**
	 * Gets x. Tracks have no meaningful position, so returns 0.
	 *
	 * @return 0
	 */
	@Override
	public double getX() {
		return 0;
	}

	/**
	 * Gets y. Tracks have no meaningful position, so returns 0.
	 *
	 * @return 0
	 */
	@Override
	public double getY() {
		return 0;
	}

	/**
	 * Empty setX method.
	 *
	 * @param x the x position
	 */
	@Override
	public void setX(double x) {
		/** implemented by subclasses */
	}

	/**
	 * Empty setY method.
	 *
	 * @param y the y position
	 */
	@Override
	public void setY(double y) {
		/** implemented by subclasses */
	}

	/**
	 * Empty setXY method.
	 *
	 * @param x the x position
	 * @param y the y position
	 */
	@Override
	public void setXY(double x, double y) {
		/** implemented by subclasses */
	}

	/**
	 * Sets whether this responds to mouse hits.
	 *
	 * @param enabled <code>true</code> if this responds to mouse hits.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Gets whether this responds to mouse hits.
	 *
	 * @return <code>true</code> if this responds to mouse hits.
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Reports whether information is available to set min/max values.
	 *
	 * @return <code>false</code> since TTrack knows only its image coordinates
	 */
	@Override
	public boolean isMeasured() {
		return !isEmpty();
	}

	/**
	 * Gets the minimum x needed to draw this object.
	 *
	 * @return 0
	 */
	@Override
	public double getXMin() {
		return getX();
	}

	/**
	 * Gets the maximum x needed to draw this object.
	 *
	 * @return 0
	 */
	@Override
	public double getXMax() {
		return getX();
	}

	/**
	 * Gets the minimum y needed to draw this object.
	 *
	 * @return 0
	 */
	@Override
	public double getYMin() {
		return getY();
	}

	/**
	 * Gets the maximum y needed to draw this object.
	 *
	 * @return 0
	 */
	@Override
	public double getYMax() {
		return getY();
	}

//	/**
//	 * Never called.
//	 * 
//	 * Gets the minimum world x needed to draw this object on the specified
//	 * TrackerPanel.
//	 *
//	 * @param panel the TrackerPanel drawing this track
//	 * @return the minimum world x
//	 */
//	public double getXMin(TrackerPanel panel) {
//		double[] bounds = getWorldBounds(panel);
//		return bounds[2];
//	}
//
//	/**
//	 * 
//	 * Never called.
//	 * 
//	 * Gets the maximum world x needed to draw this object on the specified
//	 * TrackerPanel.
//	 *
//	 * @param panel the TrackerPanel drawing this track
//	 * @return the maximum x of any step's footprint
//	 */
//	public double getXMax(TrackerPanel panel) {
//		double[] bounds = getWorldBounds(panel);
//		return bounds[0];
//	}
//
//	/**
//	 * Never called.
//	 * 
//	 * Gets the minimum world y needed to draw this object on the specified
//	 * TrackerPanel.
//	 *
//	 * @param panel the TrackerPanel drawing this track
//	 * @return the minimum y of any step's footprint
//	 */
//	public double getYMin(TrackerPanel panel) {
//		double[] bounds = getWorldBounds(panel);
//		return bounds[3];
//	}
//
//	/**
//	 * Never called.
//	 * 
//	 * Gets the maximum world y needed to draw this object on the specified
//	 * TrackerPanel.
//	 *
//	 * @param panel the TrackerPanel drawing this track
//	 * @return the maximum y of any step's footprint
//	 */
//	public double getYMax(TrackerPanel panel) {
//		double[] bounds = getWorldBounds(panel);
//		return bounds[1];
//	}

	/**
	 * Sets a user property of the track.
	 *
	 * @param name  the name of the property
	 * @param value the value of the property
	 */
	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}

	/**
	 * Gets a user property of the track. May return null.
	 *
	 * @param name the name of the property
	 * @return the value of the property
	 */
	public Object getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * Gets a collection of user property names for the track.
	 *
	 * @return a collection of property names
	 */
	public Collection<String> getPropertyNames() {
		return properties.keySet();
	}

	/**
	 * Reports whether or not the specified step is visible.
	 *
	 * @param step         the step
	 * @param trackerPanel the tracker panel
	 * @return <code>true</code> if the step is visible
	 */
	public boolean isStepVisible(Step step, TrackerPanel trackerPanel) {
		if (!isVisible())
			return false;
		int n = step.getFrameNumber();
		if (!trackerPanel.getPlayer().getVideoClip().includesFrame(n))
			return false;
		int frame = trackerPanel.getFrameNumber();
		if (n == frame)
			return true;
		if (!trailVisible)
			return false;
		if (getTrailLength() == 0)
			return true;
		int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
		return (frame - n) > -1 && (frame - n) < getTrailLength() * stepSize;
	}

	// ___________________________ protected methods ____________________________

//	/**
//	 * Gets the world bounds of this track on the specified TrackerPanel.
//	 * 
//	 * Never used?
//	 *
//	 * @param panel the TrackerPanel
//	 * @return a double[] containing xMax, yMax, xMin, yMin
//	 */
//	protected double[] getWorldBounds(TrackerPanel panel) {
//		double[] bounds
//		   // ? = panelWorldBounds.get(panel.getID())
//		;
//		// if (bounds != null) return bounds;
//		// make a rectangle containing the world positions of the TPoints in this track
//		// then convert it into world units
//		bounds = new double[4];
//		Rectangle2D rect = new Rectangle2D.Double();
//		Step[] array = steps.array;
//		for (int n = 0; n < array.length; n++) {
//			if (array[n] != null) {
//				TPoint[] points = array[n].getPoints();
//				for (int i = 0; i < points.length; i++) {
//					if (points[i] == null)
//						continue;
//					rect.add(points[i].getWorldPosition(panel));
//				}
//			}
//		}
//		// increase bounds to make room for footprint shapes
//		bounds[0] = rect.getX() + 1.05 * rect.getWidth(); // xMax
//		bounds[1] = rect.getY() + 1.05 * rect.getHeight(); // yMax
//		bounds[2] = rect.getX() - 0.05 * rect.getWidth(); // xMin
//		bounds[3] = rect.getY() - 0.05 * rect.getHeight(); // yMin
//		getPanelWorldBounds().put(panel.getID(), bounds);
//		return bounds;
//	}

	/**
	 * Sets the display format for angles.
	 *
	 * @param radians <code>true</code> for radians, false for degrees
	 */
	protected void setAnglesInRadians(boolean radians) {
		angleField.setUnits(radians ? null : Tracker.DEGREES);
		angleField.setDecimalPlaces(radians ? 3 : 1);
		angleField.setConversionFactor(radians ? 1.0 : 180 / Math.PI);
		angleField.setToolTipText(radians ? TrackerRes.getString("TTrack.AngleField.Radians.Tooltip") : //$NON-NLS-1$
				TrackerRes.getString("TTrack.AngleField.Degrees.Tooltip")); //$NON-NLS-1$
	}

	/**
	 * Disposes of resources when this track is deleted or cleared.
	 */
	@Override
	public void dispose() {
		OSPLog.notify(this, "disposing");
		properties.clear();
//		if (panelWorldBounds != null)
//			panelWorldBounds.clear();
		datasetManager = null;
		if (attachments != null) {
			for (int i = 0; i < attachments.length; i++) {
				TTrack targetTrack = attachments[i];
				if (targetTrack != null) {
					targetTrack.removePropertyChangeListener(PROPERTY_TTRACK_STEP, this); // $NON-NLS-1$
					targetTrack.removePropertyChangeListener(PROPERTY_TTRACK_STEPS, this); // $NON-NLS-1$
				}
				attachments[i] = null;
			}
			refreshAttachments();
		}
		attachments = null;
		attachmentNames = null;
		for (Step step : steps.array) {
			if (step != null) {
				step.dispose();
			}
		}

// shouldn't be necessary now that DrawingPanel.messages has lazy initialization
//		xLabel.dispose();
//		yLabel.dispose();
//		magLabel.dispose();
//		angleLabel.dispose();

		steps = null;
		setTrackerPanel(null);
		super.dispose();
	}

	/**
	 * Sets the marking flag. Flag should be true when ready to be marked by user.
	 * 
	 * @param marking true when marking
	 */
	protected void setMarking(boolean marking) {
		isMarking = marking;
	}

	/**
	 * Determines if this track is marking.
	 * 
	 * @param marking true when marking
	 */
	protected boolean isMarking() {
		return isMarking;
	}

	/**
	 * Gets the cursor used for marking new steps.
	 * 
	 * @param e the input event triggering this call (may be null)
	 * @return the marking cursor
	 */
	protected Cursor getMarkingCursor(InputEvent e) {
		switch (getMarkingCursorType(e)) {
		case TMouseHandler.STATE_AUTO:
			return TMouseHandler.autoTrackCursor;
		case TMouseHandler.STATE_AUTOMARK:
			return TMouseHandler.autoTrackMarkCursor;
		case TMouseHandler.STATE_MARK:
		default:
			return TMouseHandler.markPointCursor;
		}
	}

	int getMarkingCursorType(InputEvent e) {
		boolean autotrackEneabled = tp.isEnabled("track.autotrack");
		if (autotrackEneabled
				&& e != null 
				&& AutoTracker.isAutoTrackTrigger(e) 
				&& tp.getVideo() != null
				&& isAutoTrackable(getTargetIndex())) {
			Step step = getStep(tp.getFrameNumber());
			TPoint[] pts = (step == null ? null : step.getPoints());
			if (pts == null || pts[pts.length - 1] == null) {
				return TMouseHandler.STATE_AUTOMARK;
			}
			switch (ttype) {
			case TTrack.TYPE_COORDAXES:
			case TTrack.TYPE_PERSPECTIVE:
			case TTrack.TYPE_TAPEMEASURE:
			case TTrack.TYPE_PROTRACTOR:
				// BH! requires autotracker?
				AutoTracker autoTracker = tp.getAutoTracker(true);
				if (autoTracker.getTrack() == null || autoTracker.getTrack() == this) {
					int n = tp.getFrameNumber();
					if (autoTracker.getOrCreateFrameData(n).getKeyFrameData() == null)
						return TMouseHandler.STATE_AUTOMARK;
				}
				break;
			}
			return TMouseHandler.STATE_AUTO;
		}
		return TMouseHandler.STATE_MARK;
	}

	protected void createWarningDialog() {
		if (skippedStepWarningDialog == null && tp != null && tframe != null) {
			skippedStepWarningDialog = new JDialog(tframe, true);
			skippedStepWarningDialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					skippedStepWarningOn = !skippedStepWarningCheckbox.isSelected();
				}
			});
			JPanel contentPane = new JPanel(new BorderLayout());
			skippedStepWarningDialog.setContentPane(contentPane);
			skippedStepWarningTextpane = new JTextPane();
			skippedStepWarningTextpane.setEditable(false);
			skippedStepWarningTextpane.setOpaque(false);
			skippedStepWarningTextpane.setPreferredSize(new Dimension(400, 120));
			skippedStepWarningTextpane.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
			skippedStepWarningTextpane.setContentType("text"); //$NON-NLS-1$
			skippedStepWarningTextpane.setFont(new JLabel().getFont());
			contentPane.add(skippedStepWarningTextpane, BorderLayout.CENTER);
			skippedStepWarningCheckbox = new JCheckBox();
			skippedStepWarningCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
			closeButton = new JButton();
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					skippedStepWarningOn = !skippedStepWarningCheckbox.isSelected();
					skippedStepWarningDialog.setVisible(false);
				}
			});
			JPanel buttonbar = new JPanel();
			buttonbar.add(skippedStepWarningCheckbox);
			buttonbar.add(closeButton);
			contentPane.add(buttonbar, BorderLayout.SOUTH);
		}
	}

	protected JDialog getStepSizeWarningDialog() {
		createWarningDialog();
		if (skippedStepWarningDialog == null)
			return null;

		skippedStepWarningDialog.setTitle(TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Title")); //$NON-NLS-1$
		String m1 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message1"); //$NON-NLS-1$
		String m2 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message2"); //$NON-NLS-1$
		String m3 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message3"); //$NON-NLS-1$
		skippedStepWarningTextpane.setText(m1 + "  " + m2 + "  " + m3); //$NON-NLS-1$ //$NON-NLS-2$
		skippedStepWarningCheckbox.setText(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Checkbox")); //$NON-NLS-1$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		skippedStepWarningDialog.pack();
		// center on screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - skippedStepWarningDialog.getBounds().width) / 2;
		int y = (dim.height - skippedStepWarningDialog.getBounds().height) / 2;
		skippedStepWarningDialog.setLocation(x, y);

		return skippedStepWarningDialog;
	}

	protected JDialog getSkippedStepWarningDialog() {
		createWarningDialog();
		if (skippedStepWarningDialog == null)
			return null;

		skippedStepWarningDialog.setTitle(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Title")); //$NON-NLS-1$
		String m1 = TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Message1"); //$NON-NLS-1$
		String m3 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message3"); //$NON-NLS-1$
		skippedStepWarningTextpane.setText(m1 + "  " + m3); //$NON-NLS-1$
		skippedStepWarningCheckbox.setText(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Checkbox")); //$NON-NLS-1$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		FontSizer.setFonts(skippedStepWarningDialog, FontSizer.getLevel());
		skippedStepWarningDialog.pack();
		// center on screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - skippedStepWarningDialog.getBounds().width) / 2;
		int y = (dim.height - skippedStepWarningDialog.getBounds().height) / 2;
		skippedStepWarningDialog.setLocation(x, y);

		return skippedStepWarningDialog;
	}

	protected Dataset convertTextToDataColumn(String textColumnName) {
		if (textColumnName == null || tp == null)
			return null;
		// find named text column
		String[] entries = this.textColumnEntries.get(textColumnName);
		// entries index is frame number
		if (entries != null && entries.length > 0) {
			DatasetManager data = getData(tp);
			double[] x = data.getDataset(0).getXPoints();
			int len = data.getDataset(0).getIndex();
			
			ArrayList<Dataset> datasets = data.getDatasetsRaw();
			boolean isFrames = this.getClass() != LineProfile.class;
			int frameIndex = isFrames? -1: 0;
			for (int i = 0; i < datasets.size(); i++) {
				if (datasets.get(i).getYColumnName().equals("frame")) {
					frameIndex = i;
					break;
				}
			}

			double[] values = new double[len];
			for (int i = 0; i < values.length; i++) {
				// get frame number = entries index
				int frame = frameIndex < 0? i: (int)datasets.get(frameIndex).getY(i);
				if (entries.length > frame) {
					if (entries[frame] == null) {
						values[i] = Double.NaN;
					} else
						try {
							values[i] = Double.parseDouble(entries[frame]);
						} catch (Exception ex) {
							return null;
						}
				} else
					values[i] = Double.NaN;
			}
			Dataset dataset = new Dataset();
			dataset.append(x, values);
			dataset.setXYColumnNames(data.getDataset(0).getXColumnName(), textColumnName, getName());
			dataset.setMarkerColor(getColor());
			return dataset;
		}
		return null;
	}

//______________________ inner StepArray class _______________________

	protected class StepArray {

		// instance fields
		protected int delta = 5;
		protected Step[] array = new Step[delta];
		private boolean autofill = false;

		/**
		 * Constructs a default StepArray.
		 */
		public StepArray() {
			/** empty block */
		}

		/**
		 * Constructs an autofill StepArray and fills the array with clones of the
		 * specified step.
		 *
		 * @param step the step to fill the array with
		 */
		public StepArray(Step step) {
			autofill = true;
			step.n = 0;
			array[0] = step;
			fill(array, step);
		}

		/**
		 * Constructs an autofill StepArray and fills the array with clones of the
		 * specified step.
		 *
		 * @param step      the step to fill the array with
		 * @param increment the array sizing increment
		 */
		public StepArray(Step step, int increment) {
			this(step);
			delta = increment;
		}

		/**
		 * Gets the step at the specified index. May return null.
		 *
		 * @param n the array index
		 * @return the step
		 */
		public Step getStep(int n) {
			if (n >= array.length) {
				int len = Math.max(n + delta, n - array.length + 1);
				setLength(len);
			}
			return array[n];
		}

		/**
		 * Sets the step at the specified index. Accepts a null step argument for
		 * non-autofill arrays.
		 *
		 * @param n    the array index
		 * @param step the new step
		 */
		public void setStep(int n, Step step) {
			if (autofill && step == null)
				return;
			if (n >= array.length) {
				int len = Math.max(n + delta, n - array.length + 1);
				setLength(len);
			}
			synchronized (array) {
				array[n] = step;
			}
		}

		/**
		 * Determines if this step array contains the specified step.
		 *
		 * @param step the new step
		 * @return <code>true</code> if this contains the step
		 */
		public boolean contains(Step step) {
			synchronized (array) {
				for (int i = 0; i < array.length; i++)
					if (array[i] == step)
						return true;
			}
			return false;
		}

		/**
		 * Sets the length of the array.
		 *
		 * @param len the new length of the array
		 */
		public void setLength(int len) {
			Step[] newArray = new Step[len];
			System.arraycopy(array, 0, newArray, 0, Math.min(len, array.length));
			if (len > array.length && autofill) {
				Step step = array[array.length - 1];
				fill(newArray, step);
			}
			array = newArray;
		}

		/**
		 * Determines if this is empty.
		 *
		 * @return true if empty
		 */
		public boolean isEmpty() {
			synchronized (array) {
				for (int i = 0; i < array.length; i++)
					if (array[i] != null)
						return false;
			}
			return true;
		}

		/**
		 * Determines if the specified step is preceded by a lower index step.
		 * 
		 * @param n the step index
		 * @return true if the step is preceded
		 */
		public boolean isPreceded(int n) {
			synchronized (array) {
				int k = Math.min(n, array.length);
				for (int i = 0; i < k; i++)
					if (array[i] != null)
						return true;
			}
			return false;
		}

		public boolean isAutofill() {
			return autofill;
		}

		// __________________________ private methods _________________________

		/**
		 * Replaces null elements of the the array with clones of the specified step.
		 *
		 * @param array the Step[] to fill
		 * @param step  the step to clone
		 */
		private void fill(Step[] array, Step step) {
			for (int n = 0; n < array.length; n++) {
				if (array[n] == null) {
					Step clone = (Step) step.clone();
					clone.n = n;
					array[n] = clone;
				}
			}
		}
	} // end StepArray class

	/**
	 * A NumberField that resizes itself for display on a TTrackBar.
	 */
	protected class TrackNumberField extends NumberField {

		TrackNumberField() {
			super(0);
		}

		@Override
		public void setText(String t) {
			super.setText(t);
			if (tp != null) {
				tp.getTrackBar(true).resizeField(this);
			}
		}

	}

	/**
	 * A DecimalField that resizes itself for display on a TTrackBar.
	 */
	protected class TrackDecimalField extends DecimalField {

		TrackDecimalField(int places) {
			super(0, places);
		}

		@Override
		public void setText(String t) {
			super.setText(t);
			if (tp != null) {
				TTrackBar tbar = tp.getTrackBar(false);
				if (tbar != null)
					tbar.resizeField(this);
			}
		}

	}

	/**
	 * A DrawingPanel that mimics the look of a JLabel but can display subscripts.
	 */
	protected static class TextLineLabel extends DrawingPanel {
		DrawableTextLine textLine;
		JLabel label;
		int w;

		/**
		 * Constructor
		 */
		TextLineLabel() {
			textLine = new DrawableTextLine("", 0, -4.3); //$NON-NLS-1$
			textLine.setJustification(TextLine.CENTER);
			addDrawable(textLine);
			label = new JLabel();
			textLine.setFont(label.getFont());
			textLine.setColor(label.getForeground());
			setShowCoordinates(false);
		}

		/**
		 * Constructor with initial text
		 */
		TextLineLabel(String text) {
			this();
			setText(text);
		}

		/**
		 * Sets the text to be displayed. Accepts subscript notation eg v_{x}.
		 * 
		 * @param text the text
		 */
		void setText(String text) {
			if (text == null)
				text = ""; //$NON-NLS-1$
			if (text.equals(textLine.getText()))
				return;
			w = -1;
			textLine.setText(text);
			setToolTipText(text);
			if (text.contains("_{")) { //$NON-NLS-1$
				text = TeXParser.removeSubscripting(text);
			}
			// use label to set initial preferred size
			label.setText(text);
			java.awt.Dimension dim = label.getPreferredSize();
			dim.width += 4;
			setPreferredSize(dim);
		}

		@Override
		public Font getFont() {
			if (textLine != null)
				return textLine.getFont();
			return super.getFont();
		}

		@Override
		public void setFont(Font font) {
			if (textLine != null) {
				textLine.setFont(font);
				w = -1;
			} else
				super.setFont(font);
		}

		@Override
		public void paintComponent(Graphics g) {
			setPixelScale(); // sets the pixel scale and the world-to-pixel AffineTransform
			if (OSPRuntime.setRenderingHints)
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			textLine.draw(this, g);
			// BH ouch! adjusting size while painting??
			if (w == -1) {
				// check preferred size and adjust if needed
				w = textLine.getWidth(g);
				Dimension dim = getPreferredSize();
				if (dim.width > w + 4 || dim.width < w + 4) {
					dim.width = w + 4;
					setPreferredSize(dim);
					JToolBar c = GUIUtils.getParentToolBar(this);
					if (c != null)
						((TTrackBar) c).refresh();
				}
			}
		}

	}

	/**
	 * A dialog used to set the name of a track.
	 */
	protected class NameDialog extends JDialog {

		JLabel nameLabel;
		JTextField nameField;
		TTrack target;

		
		// constructor
		NameDialog() {
			super(tframe, null, true);
			
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					String newName = nameField.getText();
					if (target != null && tp != null)
						tp.setTrackName(target, newName, true);
				}
			});
			nameField = new JTextField(20);
			nameField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String newName = nameField.getText();
					if (target != null)
						tp.setTrackName(target, newName, true);
				}
			});
			nameLabel = new JLabel();
			JToolBar bar = new JToolBar();
			bar.setFloatable(false);
			bar.add(nameLabel);
			bar.add(nameField);
			JPanel contentPane = new JPanel(new BorderLayout());
			contentPane.add(bar, BorderLayout.CENTER);
			setContentPane(contentPane);
		}

		/**
		 * Sets the track.
		 * 
		 * @param track the track
		 */
		void setTrack(TTrack track) {
			target = track;
			// initial text is current track name
			FontSizer.setFonts(this, FontSizer.getLevel());
			setTitle(TrackerRes.getString("TTrack.Dialog.Name.Title")); //$NON-NLS-1$
			nameLabel.setText(TrackerRes.getString("TTrack.Dialog.Name.Label")); //$NON-NLS-1$
			nameField.setText(track.getName());
			nameField.selectAll();
			pack();
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
		 * @param obj     the object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			TTrack track = (TTrack) obj;
			// name
			control.setValue("name", track.getName()); //$NON-NLS-1$
			// description
			if (!track.description.equals("")) //$NON-NLS-1$
				control.setValue("description", track.description); //$NON-NLS-1$
			// color
			control.setValue("color", track.getColor()); //$NON-NLS-1$
			// footprint name
			control.setValue("footprint", track.getFootprintName()); //$NON-NLS-1$
			// visible
			control.setValue("visible", track.isVisible()); //$NON-NLS-1$
			// trail
			control.setValue("trail", track.isTrailVisible()); //$NON-NLS-1$
			// locked
			if (track.isLocked())
				control.setValue("locked", track.isLocked()); //$NON-NLS-1$
			// number formats
			String[] customPatterns = track.getCustomFormatPatterns();
			if (customPatterns.length > 0) {
				control.setValue("number_formats", customPatterns); //$NON-NLS-1$
			}
			// text columns
			if (!track.getTextColumnNames().isEmpty()) {
				String[] names = track.getTextColumnNames().toArray(new String[0]);
				control.setValue("text_column_names", names); //$NON-NLS-1$
				String[][] entries = new String[names.length][];
				for (int i = 0; i < names.length; i++) {
					entries[i] = track.textColumnEntries.get(names[i]);
				}
				control.setValue("text_column_entries", entries); //$NON-NLS-1$
			}
			// data functions
			if (track.tp != null) {
				ArrayList<Dataset> list = new ArrayList<Dataset>();
				DatasetManager data = track.getData(track.tp);
				ArrayList<Dataset> datasets = data.getDatasetsRaw();
				for (int i = 0, n = datasets.size(); i < n; i++) {
					Dataset dataset = datasets.get(i);
					if (dataset instanceof DataFunction) {
						list.add(dataset);
					}
				}
				if (!list.isEmpty()) {
					ArrayList<String> names = data.getConstantNames();
					int n = names.size();
					if (n > 0) {
						Object[][] paramArray = new Object[n][4];
						int i = 0;
						for (String key : names) {
							paramArray[i][0] = key;
							paramArray[i][1] = data.getConstantValue(key);
							paramArray[i][2] = data.getConstantExpression(key);
							paramArray[i][3] = data.getConstantDescription(key);
							i++;
						}
						control.setValue("constants", paramArray); //$NON-NLS-1$
					}
					DataFunction[] f = list.toArray(new DataFunction[0]);
					control.setValue("data_functions", f); //$NON-NLS-1$
				}
			}
			TTrack[] att = track.attachments;
			if (att != null && att.length > 0) {
				String[] names = new String[att.length];
				boolean notNull = false;
				for (int i = 0; i < att.length; i++) {
					TTrack next = att[i];
					names[i] = next == null ? null : next.getName();
					notNull = notNull || names[i] != null;
				}
				if (notNull) {
					control.setValue("attachments", names); //$NON-NLS-1$
				}
			}
		}

		/**
		 * Creates a new object.
		 *
		 * @param control the XMLControl with the object data
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return null;
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
			TTrack track = (TTrack) obj;
			boolean locked = track.isLocked();
			track.setLocked(false);
			// name
			track.setName(control.getString("name")); //$NON-NLS-1$
			// description
			track.setDescription(control.getString("description")); //$NON-NLS-1$
			// color
			track.setColor((Color) control.getObject("color")); //$NON-NLS-1$
			// footprint
			String s = control.getString("footprint"); //$NON-NLS-1$
			if (s != null)
				track.setFootprint(s.trim());
			// visible and trail
			track.setVisible(control.getBoolean("visible")); //$NON-NLS-1$
			int index = Tracker.preferredTrailLengthIndex;
			if (track.tp != null && track.tp.getTFrame() != null) {
					TToolBar toolbar = track.tp.getTFrame().getToolBar(track.tp.getID(), false);
					index = toolbar.trailLengthIndex;
			}						
			track.setTrailLength(TToolBar.trailLengths[index]);
			track.setTrailVisible(control.getBoolean("trail")); //$NON-NLS-1$
			// number formats
			track.customNumberFormats = (String[]) control.getObject("number_formats"); //$NON-NLS-1$
			// text columns
			track.textColumnNames.clear();
			track.textColumnEntries.clear();
			String[] columnNames = (String[]) control.getObject("text_column_names"); //$NON-NLS-1$
			if (columnNames != null) {
				String[][] columnEntries = (String[][]) control.getObject("text_column_entries"); //$NON-NLS-1$
				if (columnEntries != null) {
					for (int i = 0; i < columnNames.length; i++) {
						track.textColumnNames.add(columnNames[i]);
						track.textColumnEntries.put(columnNames[i], columnEntries[i]);
					}
				}
			}
			// data functions and constants
			track.constantsLoadedFromXML = (Object[][]) control.getObject("constants"); //$NON-NLS-1$
			Iterator<XMLProperty> it = control.getPropsRaw().iterator();
			while (it.hasNext()) {
				XMLProperty prop = it.next();
				if (prop.getPropertyName().equals("data_functions")) { //$NON-NLS-1$
					track.dataProp = prop;
				}
			}
			// attachments
			String[] names = (String[]) control.getObject("attachments"); //$NON-NLS-1$
			if (names != null) {
				track.attachmentNames = names;
			}
			// locked
			track.setLocked(locked || control.getBoolean("locked")); //$NON-NLS-1$
			return obj;
		}
	}

	protected NameDialog getNameDialog() {
		if (nameDialog == null) {
			nameDialog = new NameDialog();
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - nameDialog.getBounds().width) / 2;
			int y = (dim.height - nameDialog.getBounds().height) / 2;
			nameDialog.setLocation(x, y);
		}
		// prepare dialog
		nameDialog.setTrack(this);
		return nameDialog;
	}

	public void setActive() {
		panelActiveTracks.put(ID, this);
	}

	protected static TTrack getTrack(int ID) {
		return panelActiveTracks.get(ID);
	}

	public static void removeActiveTrack(int id) {
		panelActiveTracks.remove(id);
	}
	
	public static Collection<TTrack> getValues() {
		return panelActiveTracks.values();
	}


	public void invalidateData(Object newValue) {
		dataValid = false;
		if (newValue != Boolean.FALSE)
			firePropertyChange(PROPERTY_TTRACK_DATA, null, newValue == Boolean.TRUE ? null : newValue);
	}
	
	public boolean isDataValid() {
		return dataValid;
	}

//	public void notifyUndoLoaded() {
//		notifySteps();
//		// TrackEdit is also used for text column edits
//		firePropertyChange(PROPERTY_TTRACK_TEXTCOLUMN, null, null);
//	}
//
	/**
	 * Finish up any unfinished loading business that for whatever reason was not
	 * finished upon loading a track. For example, adding masses to a center-of-mass
	 * system, or adding particles to a DynamicSystem.
	 * 
	 * @param panel
	 */
	public void initialize(TrackerPanel panel) {
		// for subclasses
	}

	public void fireStepsChanged() {
		// this call will update TrackPlottingPanel
		firePropertyChange(PROPERTY_TTRACK_STEPS, null, null);
	}

	protected final static Map<String, String[]> NOMAP = new HashMap<>();
	protected final static Map<String, String> NOMAPS = new HashMap<>();
	protected final static String[] NOVARS = new String[0];
	protected final static ArrayList<String> NOVARA = new ArrayList<String>();

	abstract public Map<String, String[]> getFormatMap();

	abstract public Map<String, String> getFormatDescMap();

	abstract public String[] getFormatVariables();

	abstract public String getVarDimsImpl(String variable);

	abstract public String getBaseType();

	/**
	 * Gets a DataTable associated with a specified track.
	 *
	 * @param track the track
	 * @return the DataTable
	 */
	private TrackDataTable getDataTable() {
		ArrayList<TableTrackView> tableViews = getTableViews();
		TableTrackView view;
		return (tableViews.isEmpty() || (view = tableViews.get(0)) == null ? null : view.getDataTable());
	}

	/**
	 * Gets the custom format patterns for a specified track for
	 * TTrack.Loader.saveObject
	 *
	 * @param track the track
	 * @return array with variable names and custom patterns
	 */
	protected String[] getCustomFormatPatterns() {
		if (tp == null)
			return new String[0];
		String[] patterns = getFormatPatterns();
		TreeMap<String, String> defaultPatterns = tp.getFormatPatterns(ttype);
		ArrayList<String> customPatterns = new ArrayList<String>();
		for (int i = 0; i < patterns.length - 1; i = i + 2) {
			String name = patterns[i];
			String pattern = defaultPatterns.get(name) == null ? "" : defaultPatterns.get(name); //$NON-NLS-1$
			if (!pattern.equals(patterns[i + 1])) {
				customPatterns.add(name);
				customPatterns.add(patterns[i + 1]);
			}
		}
		return customPatterns.toArray(new String[customPatterns.size()]);
	}

	/**
	 * Gets all table views for a specified track.
	 *
	 * @param track the track
	 * @return ArrayList of table views
	 */
	protected ArrayList<TableTrackView> getTableViews() {
		ArrayList<TableTrackView> tableTrackViews = new ArrayList<TableTrackView>();
		if (tp == null || tframe == null) {
			return tableTrackViews;
		}
		TViewChooser[] choosers = tframe.getViewChoosers(tp);
		for (int i = 0; i < choosers.length; i++) {
			if (choosers[i] == null)
				continue;
			TableTView tableView = (TableTView) choosers[i].getView(TView.VIEW_TABLE);
			if (tableView != null) {
				tableTrackViews.add((TableTrackView) tableView.getTrackView(this));
			}
		}
		return tableTrackViews;
	}

	/**
	 * Gets all plot views for a specified track.
	 *
	 * @param track the track
	 * @return ArrayList of plot views
	 */
	protected ArrayList<PlotTrackView> getPlotViews() {
		ArrayList<PlotTrackView> plotTrackViews = new ArrayList<PlotTrackView>();
		if (tp == null || tframe == null) {
			return plotTrackViews;
		}
		TViewChooser[] choosers = tframe.getViewChoosers(tp);
		for (int i = 0; i < choosers.length; i++) {
			if (choosers[i] == null)
				continue;
			PlotTView plotView = (PlotTView) choosers[i].getView(TView.VIEW_PLOT);
			if (plotView != null) {
				plotTrackViews.add((PlotTrackView) plotView.getTrackView(this));
			}
		}
		return plotTrackViews;
	}
	
	/**
	 * Gets the format patterns for a specified track.
	 *
	 * @param track the track
	 * @return array with variable names and patterns
	 */
	public String[] getFormatPatterns() {
		ArrayList<String> patterns = new ArrayList<String>();
		for (String name : getAllVariables(ttype)) {
			patterns.add(name);
			patterns.add(getVarFormatPattern(name));
		}
		return patterns.toArray(new String[patterns.size()]);
	}

	/**
	 * Sets the format pattern for a specified track and name. Name may point to
	 * multiple variables.
	 *
	 * @param track   the track
	 * @param name    the name
	 * @param pattern the pattern
	 * @return true if the pattern was changed
	 */
	protected boolean setFormatPattern(String name, String pattern) {
		boolean changed = false;
		// set pattern for variables identified by the name, if any
		String[] vars = getVariablesFromFormatterDisplayName(name);
		if (vars != null) {
			for (String var : vars) {
				changed = setFormatPatternForVariable(var, pattern) || changed;
			}
			return changed;
		} else
			return setFormatPatternForVariable(name, pattern);
	}

	/**
	 * Sets the format pattern for a specified track and single variable.
	 *
	 * @param track   the track
	 * @param var     the variable
	 * @param pattern the pattern
	 * @return true if the pattern was changed
	 */
	private boolean setFormatPatternForVariable(String var, String pattern) {
		boolean changed = false;
		boolean found = false;
		if (isViewable()) {
			found = true;
			// set pattern in track tables
			ArrayList<TableTrackView> tableViews = getTableViews();
			for (TableTrackView view : tableViews) {
				if (view == null)
					continue;
				DataTable table = view.getDataTable();
				if (!table.getFormatPattern(var).equals(pattern)) {
					table.setFormatPattern(var, pattern);
					changed = true;
				}
			}
		}
		// set pattern in track NumberFields
		Map<String, NumberField[]> fieldMap = getNumberFields();
		NumberField[] fields = fieldMap.get(var);
		if (fields != null) {
			found = true;
			for (NumberField field : fields) {
				if (!field.getFixedPattern().equals(pattern)) {
					field.setFixedPattern(pattern);
					changed = true;
				}
			}
		}
		if (!found) {
			if (!pattern.equals(getProperty(var))) {
				setProperty(var, pattern);
				changed = true;
			}
		}
		if (changed && (var.equals("x") || var.equals("y")) //$NON-NLS-1$ //$NON-NLS-2$
				&& tp != null && tp.getSelectedTrack() == this) {
			tp.coordStringBuilder.setUnitsAndPatterns(this, "x", "y"); //$NON-NLS-1$ //$NON-NLS-2$
			if (tp.getSelectedPoint() != null) {
				tp.getSelectedPoint().showCoordinates(tp);
			}
		}
		return changed;
	}

	/**
	 * Gets the format pattern for a specified track and variable.
	 *
	 * @param track the track
	 * @param name  the variable name
	 * @return the pattern
	 */
	protected String getVarFormatPattern(String name) {
		String val;
		// change formatter display name to variable if needed
		if (!getAllVariables(ttype).contains(name)) {
			String[] vars = getVariablesFromFormatterDisplayName(name);
			if (vars != null && vars.length > 0) {
				name = vars[0];
			}
		}

		// get pattern from track NumberField if possible
		NumberField[] fields = getNumberFields().get(name);
		if (fields != null && fields.length > 0) {
			return fields[0].getFixedPattern();
		}

		// get pattern from table if no NumberField
		DataTable table = getDataTable();
		if (table != null && (val = table.getFormatPattern(name)) != null && !"".equals(val.trim())) {
			return val;
		}

		// get pattern from track properties
		if ((val = (String) getProperty(name)) != null) {
			return val;
		}

		// get pattern for track type

		// look in trackerPanel formatPatterns
		TreeMap<String, String> patterns = tp.getFormatPatterns(ttype);
		if ((val = patterns.get(name)) != null) {
			return val;
		}

		// look in defaultFormatPatterns
		patterns = getDefaultFormatPatterns(ttype);
		if (patterns != null && (val = patterns.get(name)) != null) {
			return val;
		}

		return ""; //$NON-NLS-1$
	}

	protected static TreeMap<String, String> getDefaultFormatPatterns(int ttype) {
		TreeMap<String, String> patterns = defaultFormatPatterns[ttype];
		if (patterns != null)
			return patterns;
		patterns = new TreeMap<String, String>();
		defaultFormatPatterns[ttype] = patterns;
		switch (ttype) {
		case TYPE_CIRCLEFITTER:
			patterns.put(TrackerRes.getString("CircleFitter.Data.PointCount"), NumberField.INTEGER_PATTERN);
			// fall through
		case TYPE_PROTRACTOR:
			patterns.put(Tracker.THETA, NumberField.DECIMAL_1_PATTERN);
		case TYPE_POINTMASS:
		case TYPE_RGBREGION:
		case TYPE_TAPEMEASURE:
		case TYPE_VECTOR:
			// steps
			patterns.put("t", NumberField.DECIMAL_3_PATTERN); //$NON-NLS-1$
			patterns.put("step", NumberField.INTEGER_PATTERN); //$NON-NLS-1$
			patterns.put("frame", NumberField.INTEGER_PATTERN); //$NON-NLS-1$
			break;
		}
		// some cross-over here, so we do two switches
		switch (ttype) {
		case TYPE_LINEPROFILE:
			patterns.put("n", NumberField.INTEGER_PATTERN); //$NON-NLS-1$
			// fall through
		case TYPE_RGBREGION:
			patterns.put("pixels", NumberField.INTEGER_PATTERN); //$NON-NLS-1$
			patterns.put("R", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
			patterns.put("G", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
			patterns.put("B", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
			patterns.put("luma", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
			break;
		}
		return patterns;
	}

	/**
	 * Determines the unit dimensions associated with a given track variable.
	 * Dimensions include L (length), T (time), M (mass), A (angle), I (integer), P
	 * (pixels), C (color 8 bit). Dimensions are often combinations of MLT. May
	 * return null.
	 * 
	 * @param track    the track
	 * @param variable the variable name
	 * @return the dimensions or null if unknown
	 */
	public static String getVariableDimensions(TTrack track, String variable) {
		if (variable.startsWith(Tracker.THETA)) {
			return "A"; //$NON-NLS-1$
		}
		if (variable.equals("t")) { //$NON-NLS-1$
			return "T"; //$NON-NLS-1$
		}
		return track.getVarDimsImpl(variable);
	}

	public void setInitialFormatPatterns(TrackerPanel trackerPanel) {
		// set default NumberField format patterns
		TreeMap<String, String> patterns = trackerPanel.getFormatPatterns(ttype);
		for (String name : patterns.keySet()) {
			setFormatPattern(name, patterns.get(name));
		}
		// set custom formats AFTER setting default patterns
		if (customNumberFormats != null) {
			getData(trackerPanel);
			for (int i = 0; i < customNumberFormats.length - 1; i = i + 2) {
				String name = customNumberFormats[i];
				String pattern = customNumberFormats[i + 1];
				setFormatPattern(name, pattern);
			}
			customNumberFormats = null;
		}
	}

	/**
	 * Refreshes data by clearing previous data and appending new valid data.
	 * Also refreshes data descriptions and initializes dataset names if needed.
	 * 
	 * @param data the DatasetManager with datasets to refresh
	 * @param count the number of datasets (columns) to refresh
	 * @param dataVariables array of variable names (length=count+1 since 1st dataset includes indep var)
	 * @param desc prefix of String resources defined in tracker.properties
	 * @param validData array of data arrays to be appended (length=count+1 since last array is indep var)
	 * @param len length of the data arrays 
	 */
	protected void clearColumns(DatasetManager data, int count, String[] dataVariables, String desc,
			double[][] validData, int len) {
		// get the independent variable
		String v0 = (dataVariables == null ? null : dataVariables[0]);
		// if already initialized, clear existing datasets
		if (v0 == null || data.getDataset(0).getColumnName(0).equals(v0)) {
			for (int i = 0; i < count; i++) {
				data.getDataset(i).clear();
			}
		} else if (dataVariables != null) {
			// needs initialization, so set variable xy names
			for (int i = 0; i < count; i++)
				data.setXYColumnNames(i, v0, dataVariables[i + 1]);
		}
		// refresh the data descriptions
		dataDescriptions = new String[count + 1];
		if (desc != null) {
			for (int i = 0; i <= count; i++) {
				// typical String resources:
				// "PointMass.Data.Description.0", "PointMass.Data.Description.1", etc  
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

	protected void addFixedItem(JMenu menu) {
		for (int i = menu.getItemCount(); --i >= 0;) {
			if (menu.getItem(i) == lockedItem) {
				menu.insert(fixedItem, i + 1);
				break;
			}
		}
	}

	/**
	 * Remove unwanted menu items and separators, then add the top item, a separator
	 * if needed, then clean out duplicate separators.
	 * 
	 * @param menu
	 * @param topItem
	 * @return
	 */
	protected JMenu assembleMenu(JMenu menu, JMenuItem topItem) {
		menu.remove(lockedItem);
		menu.remove(autoAdvanceItem);
		menu.remove(markByDefaultItem);
		menu.insert(topItem, 0);
		if (menu.getItemCount() > 1)
			menu.insertSeparator(1);
		Object prevItem = topItem;
		for (int j = menu.getItemCount(); --j >= 0;) {
			Object item = menu.getItem(j);
			if (item == null && prevItem == null) {
				// found extra separator
				menu.remove(j);
			} else {
				prevItem = item;
			}
		}
		return menu;
	}

	/**
	 * remove the last item and the separator before it, provided that item is deleteTrackItem.
	 * 
	 * @param menu
	 */
	protected void removeDeleteTrackItem(JMenu menu) {
		int n = menu.getItemCount();
		if (n > 0 && menu.getItem(n - 1) == deleteTrackItem) {
			menu.remove(--n);
			if (n > 0 && menu.getItem(n - 1) == null) {
				// not a JMenuItem, so must be a separator
				menu.remove(n - 1);
			}
		}
	}

}
