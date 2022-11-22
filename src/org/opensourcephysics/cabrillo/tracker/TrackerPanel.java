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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
//import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.opensourcephysics.cabrillo.tracker.AutoTracker.Wizard;
import org.opensourcephysics.cabrillo.tracker.TrackerIO.AsyncLoader;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.controls.XMLPropertyElement;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.MessageDrawable;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.AsyncVideoI;
import org.opensourcephysics.media.core.BaselineFilter;
import org.opensourcephysics.media.core.BrightnessFilter;
import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.ClipInspector;
import org.opensourcephysics.media.core.DarkGhostFilter;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.DeinterlaceFilter;
import org.opensourcephysics.media.core.Filter;
import org.opensourcephysics.media.core.FilterStack;
import org.opensourcephysics.media.core.GhostFilter;
import org.opensourcephysics.media.core.GrayScaleFilter;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.ImageVideo;
import org.opensourcephysics.media.core.LogFilter;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.NegativeFilter;
import org.opensourcephysics.media.core.PerspectiveFilter;
import org.opensourcephysics.media.core.RadialDistortionFilter;
import org.opensourcephysics.media.core.ResizeFilter;
import org.opensourcephysics.media.core.RotateFilter;
import org.opensourcephysics.media.core.StrobeFilter;
import org.opensourcephysics.media.core.SumFilter;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Trackable;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoGrabber;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.media.core.XYCoordinateStringBuilder;
import org.opensourcephysics.media.mov.SmoothPlayable;
import org.opensourcephysics.tools.DataFunctionPanel;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.DataToolTab;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionEditor;
import org.opensourcephysics.tools.FunctionPanel;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.ParamEditor;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.VideoCaptureTool;

import javajs.async.AsyncDialog;

/**
 * This extends VideoPanel to manage and draw TTracks. It is Tracker's main view
 * and repository of a video and its associated tracks.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TrackerPanel extends VideoPanel implements Scrollable {

	public static final String PROPERTY_TRACKERPANEL_CLEAR = "clear";
	public static final String PROPERTY_TRACKERPANEL_IMAGE = "image";
	public static final String PROPERTY_TRACKERPANEL_LOADED = "loaded";
	public static final String PROPERTY_TRACKERPANEL_MAGNIFICATION = "magnification";
	public static final String PROPERTY_TRACKERPANEL_SELECTEDPOINT = "selectedpoint";
	public static final String PROPERTY_TRACKERPANEL_SELECTEDTRACK = "selectedtrack";
	public static final String PROPERTY_TRACKERPANEL_SIZE = "size";
	public static final String PROPERTY_TRACKERPANEL_STEPNUMBER = "stepnumber";
	public static final String PROPERTY_TRACKERPANEL_TRACK = "track";
	public static final String PROPERTY_TRACKERPANEL_UNITS = "units";
	public static final String PROPERTY_TRACKERPANEL_VIDEO = "video";
	public static final String PROPERTY_TRACKERPANEL_VIDEOVISIBLE = "videovisible";

// static fields
	/** The minimum zoom level */
	public static final double MIN_ZOOM = 0.15;
	/** The maximum zoom level */
	public static final double MAX_ZOOM = 20;
	/** The zoom step size */
	public static final double ZOOM_STEP = Math.pow(2, 1.0 / 6);
	/** The fixed zoom levels */
	public static final double[] ZOOM_LEVELS = { 0.25, 0.5, 1, 2, 4, 8, 12, 20 };
	/** Calibration tool types */
	public static final String STICK = "Stick", TAPE = "CalibrationTapeMeasure", //$NON-NLS-1$ //$NON-NLS-2$
			CALIBRATION = "Calibration", OFFSET = "OffsetOrigin"; //$NON-NLS-1$ //$NON-NLS-2$

	// instance fields
	protected TFrame frame;
	/**
	 * a unique identifier for this TrackerPanel
	 */
	protected Integer panelID; 
	
	public Integer getID() {
		return panelID;
	}
	

	private double defaultImageBorder;
	private String description = ""; //$NON-NLS-1$
	protected TPoint selectedPoint;
	protected Step selectedStep;
	protected Integer selectingPanelID;
	protected TTrack selectedTrack;
	protected TPoint newlyMarkedPoint;
	protected Rectangle dirty;
	private boolean tainted;
	protected AffineTransform prevPixelTransform;
	protected double zoom = 1;
	protected JScrollPane scrollPane;
	protected JPopupMenu popup;
	protected Set<String> enabled; // enabled GUI features (subset of full_config)
	protected TPoint snapPoint; // used for origin snap
	private BufferedImage renderedImage;  // for video export
	private BufferedImage mattedImage; // for video export
	protected XMLControl currentState, currentCoords, currentSteps;
	protected TPoint pointState = new TPoint();
	protected TMouseHandler mouseHandler;
	protected JLabel badNameLabel;
	protected TrackDataBuilder dataBuilder;
	protected boolean dataToolVisible;
	protected XMLProperty customViewsProperty; // TFrame loads views
	protected XMLProperty selectedViewsProperty; // TFrame sets selected views--legacy pre-JS
	protected XMLProperty selectedViewTypesProperty; // TFrame sets selected view types--JS
	protected XMLProperty selectedTrackViewsProperty; // TFrame sets selected track views
	protected double[] dividerLocs; // TFrame sets dividers
	protected Point zoomCenter; // used when loading
	protected Map<Filter, Point> visibleFilters; // TFrame sets locations of filter inspectors
	protected int trackControlX = Integer.MIN_VALUE, trackControlY; // TFrame sets track control location
	protected int infoX = Integer.MIN_VALUE, infoY; // TFrame sets info dialog location
	protected String defaultSavePath, openedFromPath;
	protected ModelBuilder modelBuilder;
	protected TrackControl trackControl;
	protected boolean isModelBuilderVisible;
	protected boolean isShiftKeyDown, isControlKeyDown;
	
	private int cursorType;
	private boolean showTrackControlDelayed;

	/**
	 * changeable TapeMeasure, Calibration, OffsetOrigin
	 */
	protected ArrayList<TTrack> calibrationTools = new ArrayList<TTrack>();
	protected Set<TTrack> visibleCalibrationTools = new HashSet<TTrack>();
	protected Set<TTrack> measuringTools = new HashSet<TTrack>();
	protected Set<TTrack> visibleMeasuringTools = new HashSet<TTrack>();
	protected String author, contact;
	protected AutoTracker autoTracker;
	protected DerivativeAlgorithmDialog algorithmDialog;
	protected AttachmentDialog attachmentDialog;
	protected PlotGuestDialog guestsDialog;
	protected UnitsDialog unitsDialog;
	protected PasteDataDialog pasteDataDialog;
	private boolean isAutoRefresh = true;
	protected boolean isNotesVisible = false;
	protected TreeSet<String> supplementalFilePaths = new TreeSet<String>(); // HTML/PDF URI paths
	protected Map<String, String> pageViewFilePaths = new HashMap<String, String>();
	protected StepSet selectedSteps;
	protected boolean hideDescriptionWhenLoaded;
	protected PropertyChangeListener massParamListener, massChangeListener;
	@SuppressWarnings("unchecked")
	protected TreeMap<String, String>[] formatPatterns = new TreeMap[TTrack.getDefaultFormatPatterns().length];
	protected String lengthUnit = "m", massUnit = "kg", timeUnit = "s"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	protected boolean unitsVisible = true; // visible by default
	protected TCoordinateStringBuilder coordStringBuilder;
	protected ArrayList<Integer> andWorld = new ArrayList<Integer>();
	protected double[] dividerFractions = new double[4];

	private int enabledCount;
	
	public NumberFormatDialog numberFormatDialog;


	private ArrayList<TTrack> userTracks, exportableTracks;
	private Map<String, AbstractAction> actions;
	protected String title;

	/**
	 * Constructs a blank TrackerPanel with a player.
	 * 
	 * We need a frame -  at the very least, new TFrame()
	 */
	public TrackerPanel() {
		this(null, null, null);
		// no gui, no frame, no panelID. 
	}

	/**
	 * Constructs a blank TrackerPanel with a player and GUI.
	 */
	public TrackerPanel(TFrame frame) {
		this(frame, null, null);
	}

	/**
	 * Constructs a TrackerPanel with a video and player.
	 *
	 * @param video the video
	 */
	public TrackerPanel(TFrame frame, Video video) {
		this(frame, video, null);
	}

	public TrackerPanel(TFrame frame, TrackerPanel panel) {
		this(frame, null, panel);
	}

	public TrackerPanel(TFrame frame, Video video, TrackerPanel panel) {
		super(video);
		setTFrame(frame == null ? new TFrame() : frame);
		if (panel == null) {
			andWorld.add(panelID);
		} else {
//			this.view = view;
			panel.andWorld.add(panelID);
		}
//		this.view = view;
		selectedSteps = new StepSet(frame, panelID);		
		setGUI();
	}
	
	public void setTFrame(TFrame frame) {
		this.frame = frame;
		panelID = frame.allocatePanel(this); 
		System.out.println("TrackerPanel " + this + " created");
		// If have GUI.... what?
	}

	public boolean isWorldPanel() {
		return this.getClass() != TrackerPanel.class;
	}

	public Map<String, AbstractAction> getActions() {
		return actions;
	}
	
	protected void setGUI() {
		actions = TActions.createActions(this);
		displayCoordsOnMouseMoved = true;		
		zoomBox.setShowUndraggedBox(false);
		// set new CoordinateStringBuilder
		coordStringBuilder = new TCoordinateStringBuilder();
		setCoordinateStringBuilder(coordStringBuilder);

		// set fonts of message boxes and noDataLabels
//		Font font = new JTextField().getFont();

		badNameLabel = new JLabel();
		badNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		
		massParamListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if ("m".equals(e.getOldValue())) { //$NON-NLS-1$
					ParamEditor paramEditor = (ParamEditor) e.getSource();
					Parameter param = (Parameter) paramEditor.getObject("m"); //$NON-NLS-1$
					FunctionPanel panel = paramEditor.getFunctionPanel();
					PointMass m = (PointMass) getTrack(panel.getName());
					if (m != null && m.getMass() != param.getValue()) {
						m.setMass(param.getValue());
						m.massField.setValue(m.getMass());
					}
				}
			}
		};
		massChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				PointMass pm = (PointMass) e.getSource();
				FunctionPanel panel =(dataBuilder == null ? null : dataBuilder.getPanel(pm.getName()));
				if (panel == null)
					return;
				ParamEditor paramEditor = panel.getParamEditor();
				Parameter param = (Parameter) paramEditor.getObject("m"); //$NON-NLS-1$
				double newMass = (Double) e.getNewValue();
				if (newMass != param.getValue()) {
					paramEditor.setExpression("m", String.valueOf(newMass), false); //$NON-NLS-1$
				}
			}
		};
		coords.addPropertyChangeListenerSafely(this);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					if (!isShiftKeyDown) {
						isShiftKeyDown = true;
						boolean marking = setCursorForMarking(true, e);
						if (selectedTrack != null && marking != selectedTrack.isMarking) {
							selectedTrack.setMarking(marking);
						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					if (!isControlKeyDown) {
						isControlKeyDown = true;
						boolean marking = setCursorForMarking(isShiftKeyDown, e);
						if (selectedTrack != null && marking != selectedTrack.isMarking) {
							selectedTrack.setMarking(marking);
						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER && selectedTrack != null
						&& cursorType == selectedTrack.getMarkingCursorType(e) && getFrameNumber() > 0) {
					int n = getFrameNumber();
					Step step = selectedTrack.getStep(n - 1);
					if (step != null) {
						Step clone = null;
						if (selectedTrack.getClass() == PointMass.class) {
							TPoint p = ((PositionStep) step).getPosition();
							clone = selectedTrack.createStep(n, p.x, p.y);
							((PointMass) selectedTrack).keyFrames.add(n);
						} else if (selectedTrack.getClass() == Vector.class) {
							VectorStep s = (VectorStep) step;
							TPoint tail = s.getTail();
							TPoint tip = s.getTip();
							Vector vector = (Vector) selectedTrack;
							double dx = tip.x - tail.x;
							double dy = tip.y - tail.y;
							clone = vector.createStep(n, tail.x, tail.y, dx, dy);
						}
						if (clone != null && selectedTrack.isAutoAdvance()) {
							getPlayer().step();
							hideMouseBox();
						} else {
							setMouseCursor(Cursor.getDefaultCursor());
							if (clone != null) {
								setSelectedPoint(clone.getDefaultPoint());
								selectedTrack.repaintStep(clone);
							}
						}
					}
				} else
					handleKeyPress(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					isShiftKeyDown = false;
					boolean marking = setCursorForMarking(false, e);
					if (selectedTrack != null && marking != selectedTrack.isMarking) {
						selectedTrack.setMarking(marking);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					isControlKeyDown = false;
					boolean marking = setCursorForMarking(isShiftKeyDown, e);
					if (selectedTrack != null && marking != selectedTrack.isMarking) {
						selectedTrack.setMarking(marking);
					}
				}
			}
		});
		// set default properties
		setDrawingInImageSpace(true);
		setPreferredSize(new Dimension(1, 1));
		// load default configuration file
		enabled = Tracker.getDefaultConfig();
		enabledCount++;
		changed = false;
	}

	@Override
	protected void addVideoPlayer() {
		super.addVideoPlayer();
		player.setInspectorButtonVisible(false);
		player.addActionListener(this);		
	}

	@Override
	protected void setMouseListeners() {
		// create and add a new mouse controller for tracker
		mouseController = new TMouseController();
		addMouseListener(mouseController);
		addMouseMotionListener(mouseController);
		addOptionController();
	}

	/**
	 * Overrides VideoPanel setVideo method.
	 *
	 * @param newVideo the video; may be null
	 */
	@Override
	public void setVideo(Video newVideo) {
		XMLControl state = null;
		boolean undoable = true;
		Video oldVideo = getVideo();
		if (newVideo != oldVideo && oldVideo instanceof ImageVideo) {
			ImageVideo vid = (ImageVideo) getVideo();
			vid.saveInvalidImages();
			undoable = vid.isFileBased();
		}
		if (newVideo != oldVideo && undoable) {
			state = new XMLControlElement(getPlayer().getVideoClip());
		}
		if (newVideo != oldVideo && oldVideo != null) {
			// clear filters from old video
			TActions.clearFiltersAction(this, false);
		}
		super.setVideo(newVideo, true); // play all steps by default
		if (state != null) {
			state = new XMLControlElement(state.toXML());
			Undo.postVideoReplace(this, state);
		}
		TMat mat = getMat();
		if (mat != null && newVideo != null)
			mat.refresh();
		if (modelBuilder != null) {
			modelBuilder.refreshSpinners();
		}
		firePropertyChange(PROPERTY_TRACKERPANEL_IMAGE, null, null); // to tracks & views //$NON-NLS-1$
	}

	/**
	 * Gets the title for tabs, menus, etc.
	 *
	 * @return the title
	 */
	public String getTitle() {
		if (getDataFile() != null) {
			title = getDataFile().getName();
		} else if (defaultFileName != null) {
			title = defaultFileName;
		} else if (getVideo() != null && (title = (String) getVideo().getProperty("name")) // $NON-NLS-0$
				!= null) {
			title = XML.forwardSlash(title);
			int i = title.lastIndexOf("/"); //$NON-NLS-1$
			if (i >= 0)
				title = title.substring(i + 1);
		} else {
			title = TrackerRes.getString("TrackerPanel.NewTab.Name"); //$NON-NLS-1$
		}
		return title;
	}

	/**
	 * Gets the path used as tooltip for the tab.
	 *
	 * @return the path
	 */
	public String getToolTipPath() {
		if (getDataFile() != null) {
			return XML.forwardSlash(getDataFile().getPath());
		}
		if (openedFromPath != null) {
			return openedFromPath;
		}
		if (getVideo() != null) {
			String path = (String) getVideo().getProperty("absolutePath"); //$NON-NLS-1$
			if (path != null) {
				return XML.forwardSlash(path);
			}
		}
		return null;
	}

	/**
	 * Gets the description of this panel.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this panel.
	 *
	 * @param desc a description
	 */
	public void setDescription(String desc) {
		description = (desc == null ? "" : desc);
	}

	/**
	 * Gets the model builder.
	 *
	 * @return the model builder
	 */
	public ModelBuilder getModelBuilder() {
		if (modelBuilder == null) {
			// create and size model builder
			modelBuilder = new ModelBuilder(this);
			// show model builder
			try {
				// place near top right corner of frame
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				Point frameLoc = frame.getLocationOnScreen();
				int w = modelBuilder.getWidth() + 20;
				int h = modelBuilder.getHeight() + 100;
				int x = Math.min(screen.width - w, frameLoc.x + frame.getWidth() - w);
				x = Math.max(x, 0);
				int y = Math.min(screen.height - h, frameLoc.y);
				y = Math.max(y, 0);
				modelBuilder.setLocation(x, y);
			} catch (Exception ex) {
				/** empty block */
			}

		}
		return modelBuilder;
	}

	/**
	 * Adds the specified rectangle to the dirty region. The dirty region is
	 * repainted when repaintDirtyRegion is called. A null dirtyRect argument is
	 * ignored.
	 *
	 * @param dirtyRect the dirty rectangle
	 */
	public void addDirtyRegion(Rectangle dirtyRect) {

		tainted = true;

		if (dirty == null)
			dirty = dirtyRect;
		return;
//		else if (dirtyRect != null)
//			dirty.add(dirtyRect);
	}

	/**
	 * Repaints the dirty region.
	 */
	public void repaintDirtyRegion() {
		if (getHeight() >= 0 && (tainted || dirty != null)) {
			TFrame.repaintT(this);
		}
	}

	/**
	 * Gets a list of TTracks being drawn on this panel.
	 *
	 * @return a list of tracks
	 */
	public ArrayList<TTrack> getTracks() {
		return getDrawables(TTrack.class);
	}

	/**
	 * Gets a list of TTracks being drawn on this panel.
	 *
	 * @return a list of tracks
	 */
	public ArrayList<TTrack> getTracksTemp() {
		return getDrawablesTemp(TTrack.class);
	}

	/**
	 * Gets the list of user-controlled TTracks on this panel.
	 *
	 * @return a list of tracks under direct user control
	 */
	public ArrayList<TTrack> getUserTracks() {
		if (userTracks != null)
			return userTracks;
		ArrayList<TTrack> tracks = getTracks();
		tracks.remove(getAxes());
		tracks.removeAll(calibrationTools);
		tracks.removeAll(measuringTools);
		tracks.removeAll(getDrawablesTemp(PerspectiveTrack.class));

		// remove child ParticleDataTracks
		ArrayList<ParticleDataTrack> list = getDrawablesTemp(ParticleDataTrack.class);
		for (int m = 0, n = list.size(); m < n; m++) {
			ParticleDataTrack track = list.get(m);
			if (track.getLeader() != track) {
				tracks.remove(track);
			}
		}
		list.clear();
		return userTracks = tracks;
	}

	/**
	 * Gets the list of TTracks with exportable data on this panel.
	 *
	 * @return a list of tracks with exportable data
	 */
	public ArrayList<TTrack> getExportableTracks() {
		if (exportableTracks != null)
			return exportableTracks;
		ArrayList<TTrack> tracks = getTracks();
		tracks.remove(getAxes());
		tracks.removeAll(calibrationTools);
		tracks.removeAll(getDrawablesTemp(PerspectiveTrack.class));

//		// remove child ParticleDataTracks
//		ArrayList<ParticleDataTrack> list = getDrawablesTemp(ParticleDataTrack.class);
//		for (int m = 0, n = list.size(); m < n; m++) {
//			ParticleDataTrack track = list.get(m);
//			if (track.getLeader() != track) {
//				tracks.remove(track);
//			}
//		}
//		list.clear();
		return exportableTracks = tracks;
	}

	/**
	 * Gets the list of TTracks to save with this panel.
	 *
	 * @return a list of tracks to save
	 */
	public ArrayList<TTrack> getTracksToSave() {
		// remove child ParticleDataTracks
		ArrayList<TTrack> tracks = getTracks();
		ArrayList<ParticleDataTrack> list = getDrawablesTemp(ParticleDataTrack.class);
		for (int m = 0, n = list.size(); m < n; m++) {
			ParticleDataTrack track = list.get(m);
			if (track.getLeader() != track) {
				tracks.remove(track);
			}
		}
		list.clear();
		return tracks;
	}

	public TTrack getTrack(String name) {
		TTrack t = getTrack(name, getTracksTemp());
		clearTemp();
		return t;
	}
	/**
	 * Gets the first track with the specified name
	 *
	 * @param name the name of the track
	 * @return the track
	 */
	public TTrack getTrack(String name, ArrayList<TTrack> list) {
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack track = list.get(it);
			if (track.getName().equals(name) || track.getName("track").equals(name)) //$NON-NLS-1$
				return track;
		}
		return null;
	}

	/**
	 * Adds a track.
	 *
	 * @param track the track to add
	 */
	public synchronized void addTrack(TTrack track) {
		if (track == null)
			return;
		boolean firstTrack = userTracks == null || userTracks.isEmpty();
		boolean isUserTrack = false;
		// BH 2020.07.09
		userTracks = null;
		exportableTracks = null;
		track.setActive();
		// set trackerPanel property if not yet set
		if (track.tp == null) {
			track.setTrackerPanel(this);
			// add this TrackerPanel to the track's listener list
			track.addListener(this);
		}

		// set angle format of the track
		track.setAnglesInRadians(frame.isAnglesInRadians());
		showTrackControlDelayed = true;
		boolean doAddDrawable = true;
		if (track instanceof ParticleDataTrack) {
			// special case: ParticleDataTrack may add extra points
			ParticleDataTrack pdt = (ParticleDataTrack) track;
			super.addDrawable(pdt);
			if (pdt.morePoints.size() > 0) {
				SwingUtilities.invokeLater(() -> {
					addDataTrackPoints(pdt);
				});
			}
			doAddDrawable = false;
			isUserTrack = true;
		} else if (calibrationTools.contains(track)) {
			// special case: same calibration tool added again?
			showTrackControlDelayed = false;
		} else {
			switch (track.getBaseType()) {
			case "PerspectiveTrack":
				showTrackControlDelayed = false;
				break;
			case "TapeMeasure":
				showTrackControlDelayed = false;
				TapeMeasure tape = (TapeMeasure) track;
				if (tape.isReadOnly()) {
					// tape measure
					measuringTools.add(tape);
					visibleMeasuringTools.add(tape);
					isUserTrack = true;
				} else {
					// calibration tape or stick
					calibrationTools.add(tape);
					visibleCalibrationTools.add(tape);
				}
				break;
			case "OffsetOrigin":
			case "Calibration":
				showTrackControlDelayed = false;
				calibrationTools.add(track);
				visibleCalibrationTools.add(track);
				break;
			case "CoordAxes":
				showTrackControlDelayed = false;
				if (getAxes() != null)
					removeDrawable(getAxes()); // only one axes at a time
				super.addDrawable(track);
				moveToBack(track);
				TMat mat = getMat();
				if (mat != null) {
					moveToBack(mat); // put mat behind grid
				}
				doAddDrawable = false;
				break;
			case "Protractor":
			case "CircleFitter":
				showTrackControlDelayed = false;
				measuringTools.add(track);
				visibleMeasuringTools.add(track);
				isUserTrack = true;
				break;
			default:
				// all other tracks (point mass, vector, particle model, line profile, etc)
				// set track name--prevents duplicate names
				setTrackName(track, track.getName(), false);
				isUserTrack = true;
				break;
			}
		}
		if (doAddDrawable)
			super.addDrawable(track);
		
		// update track control and dataBuilder
		if (trackControl != null && trackControl.isVisible())
			trackControl.refresh();
		if (dataBuilder != null && !getSystemDrawables().contains(track)) {
			FunctionPanel panel = createFunctionPanel(track);
			dataBuilder.addPanel(track.getName(), panel);
			dataBuilder.setSelectedPanel(track.getName());
		}
		// set length of coord system before firing property change (speeds loading up
		// of very long tracks)
		int len = track.getSteps().length;
		len = Math.max(len, getCoords().getLength());
		getCoords().setLength(len);

		// set font level
		track.setFontLevel(FontSizer.getLevel());

		// notify views, also TrackControl
		// note that this callback will 
		firePropertyChange(PROPERTY_TRACKERPANEL_TRACK, null, track); // to views //$NON-NLS-1$

		// set default NumberField format patterns
		if (frame != null) {
			track.setInitialFormatPatterns(this);
		}

		changed = true;
		// select new track in autotracker
		if (autoTracker != null && track != getAxes()) {
			autoTracker.setTrack(track);
		}
		
		if (firstTrack && isUserTrack && !frame.areViewsVisible(TFrame.DEFAULT_VIEWS, this)) {
			if (!TFrame.isPortraitOrientation)
				frame.setDividerLocation(this, TFrame.SPLIT_MAIN_RIGHT, TFrame.DEFAULT_MAIN_DIVIDER); 			
			else 
				frame.setDividerLocation(this, TFrame.SPLIT_MAIN_BOTTOM, TFrame.DEFAULT_BOTTOM_DIVIDER); 
		}

	}

	private void addDataTrackPoints(ParticleDataTrack dt) {
		for (ParticleDataTrack child : dt.morePoints) {
			addTrack(child);
		}
		if (frame != null && isShowing()) {
			List<TView> views = frame.getTViews(panelID, TView.VIEW_PLOT, null);
			frame.getTViews(panelID, TView.VIEW_TABLE, views);
			for (int i = 0; i < views.size(); i++) {
				((TrackChooserTView) views.get(i)).setSelectedTrack(dt);
			}
		}
	}

	/**
	 * Determines if the specified track is currently displayed in a table or plot
	 * view.
	 * 
	 * @param track the track
	 * @return true if displayed in a plot or table view
	 */
	protected boolean isTrackViewDisplayed(TTrack track) {
		TFrame frame = getTFrame();
		if (frame != null && TrackerPanel.this.isShowing()) {
			List<TView> views = frame.getTViews(panelID, TView.VIEW_PLOT, null);
			frame.getTViews(panelID, TView.VIEW_TABLE, views);
			for (int i = 0; i < views.size(); i++) {
				TView view = views.get(i);
				if (((TrackChooserTView) view).isTrackViewDisplayed(track)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates a new FunctionPanel for a track.
	 * 
	 * @param track the track
	 * @return the FunctionPanel
	 */
	protected FunctionPanel createFunctionPanel(TTrack track) {
		DatasetManager data = track.getData(this);
		FunctionPanel functionPanel = new DataFunctionPanel(data);
		functionPanel.setIcon(track.getIcon(21, 16, "point")); //$NON-NLS-1$
		final ParamEditor paramEditor = functionPanel.getParamEditor();
		// Check for PointMass and Vector, which might be subclassed
		switch(track.getBaseType()) {
		case "PointMass":
			functionPanel.setDescription(PointMass.class.getName());
			PointMass pm = (PointMass) track;
			Parameter param = (Parameter) paramEditor.getObject("m"); //$NON-NLS-1$
			if (param == null) {
				param = new Parameter("m", String.valueOf(pm.getMass())); //$NON-NLS-1$
				param.setDescription(TrackerRes.getString("ParticleModel.Parameter.Mass.Description")); //$NON-NLS-1$
				paramEditor.addObject(param, false);
			}
			param.setNameEditable(false); // mass name not editable
			paramEditor.addPropertyChangeListener(FunctionEditor.PROPERTY_FUNCTIONEDITOR_EDIT, massParamListener); //$NON-NLS-1$
			pm.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, massChangeListener); //$NON-NLS-1$
			break;
		case "Vector":
			functionPanel.setDescription(Vector.class.getName());
			break;
		default:
			functionPanel.setDescription(track.getClass().getName());
			break;
		}
		return functionPanel;
	}

	public void removePointMassListeners(PointMass pointMass) {
		pointMass.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, massChangeListener);
		if (dataBuilder != null) {
			FunctionPanel functionPanel = dataBuilder.getPanel(getName());
			if (functionPanel != null) {
				functionPanel.getParamEditor().removePropertyChangeListener(FunctionEditor.PROPERTY_FUNCTIONEDITOR_EDIT, massParamListener); 
			}
		}
	}

	/**
	 * Removes a track.
	 *
	 * @param track the track to remove
	 */
	public synchronized void removeTrack(TTrack track) {
		if (getTrackByName(track.getClass(), track.getName()) == null)
			return;
		userTracks = null;
		exportableTracks = null;
		track.removeListener(this);
		super.removeDrawable(track);
		if (dataBuilder != null)
			dataBuilder.removePanel(track.getName());
//    if (modelBuilder != null) modelBuilder.removePanel(track.getName());
		if (getSelectedTrack() == track)
			setSelectedTrack(null);
		// notify views and other listeners
		firePropertyChange(PROPERTY_TRACKERPANEL_TRACK, track, null);
		TTrack.removeActiveTrack(track.getID());
		changed = true;
	}

	/**
	 * Determines if the specified track is in this tracker panel.
	 *
	 * @param track the track to look for
	 * @return <code>true</code> if this contains the track
	 */
	public boolean containsTrack(TTrack track) {
		ArrayList<TTrack> list = getTracksTemp();
		boolean ret = false;;
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack next = list.get(it);
			if (track == next) {
				ret = true;
				break;
			}
		}
		clearTemp();
		return ret;
	}

	/**
	 * Erases all tracks in this tracker panel.
	 */
	public void eraseAll() {
		ArrayList<TTrack> list = getTracks();
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack track = list.get(it);
			track.erase();
		}
	}

	/**
	 * Saves this TrackerPanel if changed, then runs the appropriate Runnable
	 */
	public void askSaveIfChanged(Function<Boolean, Void> whenClosed, Runnable whenCanceled) {
		if (!changed) {
			whenClosed.apply(false);
			return;
		}
		String name = getTitle();
		// eliminate extension if no data file
		if (getDataFile() == null) {
			int i = name.lastIndexOf('.');
			if (i > 0) {
				name = name.substring(0, i);
			}
		}
		new AsyncDialog().showConfirmDialog(frame,
				TrackerRes.getString("TrackerPanel.Dialog.SaveChanges.Message") + " \"" + name + "\"?",
				TrackerRes.getString("TrackerPanel.Dialog.SaveChanges.Title"), new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						switch (e.getID()) {
						case JOptionPane.YES_OPTION:
							restoreViews();
							File file = VideoIO.save(getDataFile(), TrackerPanel.this);
							if (file == null) {
								if (whenCanceled != null) {
									whenCanceled.run();
									break;
								}
							}
							changed = false;
							if (whenClosed != null)
								whenClosed.apply(true);
							break;
						case JOptionPane.NO_OPTION:
							if (whenClosed != null)
								whenClosed.apply(false);
							break;
						default: // canceled
							if (whenCanceled != null)
								whenCanceled.run();
						}
					}
				});
//		int i = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(),
//				TrackerRes.getString("TrackerPanel.Dialog.SaveChanges.Message") + " \"" + name + "\"?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//				TrackerRes.getString("TrackerPanel.Dialog.SaveChanges.Title"), //$NON-NLS-1$
//				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
//		if (i == JOptionPane.YES_OPTION) {
//			restoreViews();
//			File file = VideoIO.save(getDataFile(), this);
//			if (file == null)
//				return false;
//		} else if (i == JOptionPane.CLOSED_OPTION || i == JOptionPane.CANCEL_OPTION) {
//			return false;
//		}
//		changed = false;
//		return true;
	}

	/**
	 * Overrides VideoPanel getDrawables method.
	 *
	 * @return a list of Drawable objects
	 */
	@Override
	public ArrayList<Drawable> getDrawables() {
		ArrayList<Drawable> list = super.getDrawables();
		TTrack track = getSelectedTrack();
		if (track != null && list.contains(track) && track != getAxes()) {
			// put selected track at the front so paints on top
			list.remove(track);
			list.add(track);
		}
		// put mat behind everything
		TMat mat = getMat();
		if (mat != null && list.get(0) != mat) {
			list.remove(mat);
			list.add(0, mat);
		}
		
		return list;
	}

	/**
	 * Gets the list of system Drawables.
	 *
	 * @return a list of Drawable objects
	 */
	public ArrayList<Drawable> getSystemDrawables() {
		ArrayList<Drawable> list = new ArrayList<Drawable>();
		Drawable drawable = getMat();
		if (drawable != null)
			list.add(drawable);
		drawable = getAxes();
		if (drawable != null)
			list.add(drawable);
		for (TTrack next : calibrationTools) {
			list.add(next);
		}
		return list;
	}

	/**
	 * Overrides VideoPanel addDrawable method.
	 *
	 * @param drawable the drawable object
	 */
	@Override
	public synchronized void addDrawable(Drawable drawable) {
		if (drawable instanceof TTrack) {
			addTrack((TTrack) drawable);
		} else {
			super.addDrawable(drawable);
		}
	}

	/**
	 * Moves a drawable behind all others except the video.
	 *
	 * @param drawable the drawable object
	 */
	public synchronized void moveToBack(Drawable drawable) {
		if (drawable != null && drawableList.contains(drawable)) {
			synchronized (drawableList) {
				drawableList.remove(drawable);
				if (drawable instanceof TMat) // put mat at back
					drawableList.add(0, drawable);
				else {
					int index = getMat() == null ? 0 : 1; // put in front of mat, if any
					if (getVideo() != null)
						index++; // put in front of video, if any
					drawableList.add(index, drawable);
				}
			}
		}
	}

	/**
	 * Overrides VideoPanel removeDrawable method.
	 *
	 * @param drawable the drawable object
	 */
	@Override
	public synchronized void removeDrawable(Drawable drawable) {
		if (drawable instanceof TTrack)
			removeTrack((TTrack) drawable);
		else
			super.removeDrawable(drawable);
	}

	/**
	 * Overrides VideoPanel removeObjectsOfClass method.
	 *
	 * @param c the class to remove
	 */
	@Override
	public synchronized <T extends Drawable> void removeObjectsOfClass(Class<T> c) {
		if (TTrack.class.isAssignableFrom(c)) { // objects are TTracks
			// remove propertyChangeListeners
			ArrayList<T> removed = getObjectOfClass(c);
			for (int i = 0, n = removed.size(); i < n; i++) {
				((TTrack) removed.get(i)).removeListener(this);
			}
			super.removeObjectsOfClass(c);
			// notify views
			for (Object next : removed) {
				TTrack track = (TTrack) next;
				firePropertyChange(PROPERTY_TRACKERPANEL_TRACK, track, null);
			}
			changed = true;
		} else
			super.removeObjectsOfClass(c);
	}

	/**
	 * Overrides VideoPanel clear method.
	 */
	@Override
	public void clear() {
		clear(true);
	}

	/**
	 * Clear all drawables.
	 * 
	 * @param andSetCoords in general course of replacing them; false for dispose
	 */
	synchronized private void clear(boolean andSetCoords) {
		//long t0 = Performance.now(0);
		setSelectedTrack(null);
		selectedPoint = null;
		ArrayList<TTrack> list = getTracks();
			for (int i = 0, n = list.size(); i < n; i++) {
				TTrack track = list.get(i);
				track.removeListener(this);
				// handle case when track is the origin of current reference frame
				ImageCoordSystem coords = getCoords();
				if (andSetCoords && coords instanceof ReferenceFrame && ((ReferenceFrame) coords).getOriginTrack() == track) {
					// set coords to underlying coords
					coords = ((ReferenceFrame) coords).getCoords();
					setCoords(coords);
				}
			}
		TMat mat = getMat();
		if (mat != null) {
			mat.cleanup();
		}
		super.clear(); // clears all drawables except video
		if (dataBuilder != null) {
			dataBuilder.clearPanels();
			dataBuilder.setVisible(false);
		}
		if (modelBuilder != null) {
			modelBuilder.clearPanels();
			modelBuilder.setVisible(false);
		}
		// notify views and other listeners
		if (!isDisposed)
			firePropertyChange(PROPERTY_TRACKERPANEL_CLEAR, null, null);
		// remove tracks from TTrack.activeTracks
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack.removeActiveTrack(list.get(it).getID());
		}
		changed = true;
		//OSPLog.debug("!!! " + Performance.now(t0) + " TrackerPanel.clear");
	}

	/**
	 * Clears all tracks.
	 */
	public synchronized void clearTracks() {
		ArrayList<TTrack> list = getTracks();
		// get background drawables to replace after clearing
		ArrayList<Drawable> keepers = getSystemDrawables();
		clear();
		// replace keepers
		for (int i = 0, n = keepers.size(); i < n; i++) {
			Drawable drawable = keepers.get(i);
			if (drawable instanceof TMat) {
				((TMat) drawable).setTrackerPanel(this);
			}
			addDrawable(drawable);
			list.remove(drawable);
		}
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack track = list.get(it);
			track.dispose();
		}
	}

	/**
	 * Overrides VideoPanel setCoords method.
	 *
	 * @param _coords the new image coordinate system
	 */
	@Override
	public void setCoords(ImageCoordSystem _coords) {
		if (_coords == null || _coords == coords)
			return;
		if (video == null) {
			coords.removePropertyChangeListener(this);
			coords = _coords;
			coords.addPropertyChangeListener(this);
			int n = getFrameNumber();
			getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));
			try {
				firePropertyChange(Video.PROPERTY_VIDEO_COORDS, null, coords);
				firePropertyChange(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, null, null);
			} catch (Exception e) {
			}
		} else {
			// BH note that video.setCoords will loop around and fire ImageCoordSystem.PROPERTY_COORDS_TRANSFORM itself
			video.setCoords(_coords);
		}
	}

	/**
	 * Sets the reference frame by name. If the name is null or not found, the
	 * default reference frame is used.
	 *
	 * @param trackName the name of a point mass
	 */
	public void setReferenceFrame(String trackName) {
		PointMass thePM = getTrackByName(PointMass.class, trackName);
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (thePM != null) {
					ImageCoordSystem coords = getCoords();
					boolean wasRefFrame = coords instanceof ReferenceFrame;
					while (coords instanceof ReferenceFrame) {
						coords = ((ReferenceFrame) coords).getCoords();
					}
					setCoords(new ReferenceFrame(coords, thePM));
					// special case: if pm is a particle model and wasRefFrame is true,
					// refresh steps of pm after setting new ReferenceFrame
					if (thePM instanceof ParticleModel && wasRefFrame) {
						((ParticleModel) thePM).setLastValidFrame(-1);
						((ParticleModel) thePM).refreshSteps("referenceFrame change");
					}
					setSelectedPoint(null);
					selectedSteps.clear();
					TFrame.repaintT(TrackerPanel.this);
				} else {
					ImageCoordSystem coords = getCoords();
					if (coords instanceof ReferenceFrame) {
						coords = ((ReferenceFrame) coords).getCoords();
						setCoords(coords);
						setSelectedPoint(null);
						selectedSteps.clear();
						TFrame.repaintT(TrackerPanel.this);
					}
				}

			}
		};
		runner.run();
//		new Thread(runner).start();
//    if (pm != null) {
//      ImageCoordSystem coords = getCoords();
//      boolean wasRefFrame = coords instanceof ReferenceFrame;
//      while (coords instanceof ReferenceFrame) {
//        coords = ( (ReferenceFrame) coords).getCoords();
//      }
//      setCoords(new ReferenceFrame(coords, pm));
//      // special case: if pm is a particle model and wasRefFrame is true,
//      // refresh steps of pm after setting new ReferenceFrame
//      if (pm instanceof ParticleModel && wasRefFrame) {
//      	((ParticleModel)pm).lastValidFrame = -1;
//      	((ParticleModel)pm).refreshSteps();
//      }      
//      setSelectedPoint(null);
//      selectedSteps.clear();
//     TFrame.repaintT(this);
//    }
//    else {
//      ImageCoordSystem coords = getCoords();
//      if (coords instanceof ReferenceFrame) {
//        coords = ( (ReferenceFrame) coords).getCoords();
//        setCoords(coords);
//        setSelectedPoint(null);
//        selectedSteps.clear();
//       TFrame.repaintT(this);
//      }
//    }

	}

	/**
	 * Gets the coordinate axes.
	 *
	 * @return the CoordAxes
	 */
	public CoordAxes getAxes() {
		return getFirstDrawable(CoordAxes.class);
	}

	/**
	 * Gets the mat.
	 *
	 * @return the first TMat in the drawable list
	 */
	public TMat getMat() {
		TMat mat = getFirstDrawable(TMat.class);
		if (mat != null)
			mat.checkVideo(this);
		return mat;
	}

	/**
	 * Gets the origin snap point.
	 *
	 * @return the snap point
	 */
	public TPoint getSnapPoint() {
		if (snapPoint == null)
			snapPoint = new TPoint();
		return snapPoint;
	}

	@Override
	public void setCursor(Cursor c) {
		if (c == TMouseHandler.autoTrackCursor)
			cursorType = TMouseHandler.STATE_AUTO;
		else if (c == TMouseHandler.autoTrackMarkCursor)
			cursorType = TMouseHandler.STATE_AUTOMARK;
		else if (c == TMouseHandler.markPointCursor)
			cursorType = TMouseHandler.STATE_MARK;
		else 
			cursorType = 0;
		super.setCursor(c);
	}
	/**
	 * Sets the selected track.
	 *
	 * @param track the track to select
	 */
	public void setSelectedTrack(TTrack track) {
		if (selectedTrack == track)
			return;
		if (track != null && track instanceof ParticleModel && ((ParticleModel) track).refreshing)
			return;
		TTrack prevTrack = selectedTrack;
		selectedTrack = track;
		if (Tracker.showHints && track != null)
			setMessage(track.getMessage());
		else
			setMessage(""); //$NON-NLS-1$
		firePropertyChange(PROPERTY_TRACKERPANEL_SELECTEDTRACK, prevTrack, track);
		coordStringBuilder.setUnitsAndPatterns(track, "x", "y"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Gets the selected track.
	 *
	 * @return the selected track
	 */
	public TTrack getSelectedTrack() {
		return selectedTrack;
	}

	/**
	 * Sets the selected point. Also sets the selected step, track, and selecting
	 * panel.
	 *
	 * @param point the point to receive actions
	 */
	public void setSelectedPoint(TPoint point) {
		if (point == selectedPoint && point == null)
			return;
		TPoint prevPoint = selectedPoint;
		if (prevPoint != null) {
			prevPoint.setAdjusting(false, null);
		}
		selectedPoint = point;
		// determine if selected steps or previous point has changed
		boolean stepsChanged = !selectedSteps.isEmpty() && selectedSteps.isChanged();
		// determine if newly selected step is in selectedSteps
		if (selectedSteps.size() > 1) {
			boolean newStepSelected = false;
			if (point != null) {
				// find associated step
				Step step = null;
				ArrayList<TTrack> list = getTracksTemp();
				for (int it = 0, n = list.size(); it < n; it++) {
					TTrack track = list.get(it);
					step = track.getStep(point, this);
					if (step != null) {
						newStepSelected = selectedSteps.contains(step);
						break;
					}
				}
				list.clear();
			}
			if (newStepSelected) {
				firePropertyChange(PROPERTY_TRACKERPANEL_SELECTEDPOINT, prevPoint, point);
				selectedSteps.isModified = false;
				return;
			}
		}
		boolean prevPointChanged = currentState != null && prevPoint != null && prevPoint != point
				&& prevPoint != newlyMarkedPoint && (prevPoint.x != pointState.x || prevPoint.y != pointState.y);
		if (selectedPoint == null) {
			newlyMarkedPoint = null;
		}
		// post undo edit if selectedSteps or previous point has changed
		if (stepsChanged || prevPointChanged) {
			boolean trackEdit = false;
			boolean coordsEdit = false;
			if (prevPointChanged && prevPoint != null) {
				trackEdit = prevPoint.isTrackEditTrigger() && getSelectedTrack() != null;
				coordsEdit = prevPoint.isCoordsEditTrigger();
			} else { // steps have changed
				trackEdit = selectedSteps.getTracks().length == 1;
			}
			if (trackEdit && coordsEdit) {
				Undo.postTrackAndCoordsEdit(getSelectedTrack(), currentState, currentCoords);
			} else if (trackEdit) {
				if (stepsChanged) {
					if (!selectedSteps.isModified) {
						selectedSteps.clear(); // posts undoable edit if changed
					}
				} else {
					Undo.postTrackEdit(getSelectedTrack(), currentState);
				}
			} else if (coordsEdit) {
				Undo.postCoordsEdit(this, currentState);
			} else if (prevPoint != null && prevPoint.isStepEditTrigger()) {
				Undo.postStepEdit(selectedStep, currentState);
			} else if (prevPoint instanceof LineProfileStep.LineEnd) {
				prevPoint.setTrackEditTrigger(true);
			}
		}
		if (selectedStep != null)
			selectedStep.repaint();
		if (point == null) {
			selectedStep = null;
			selectingPanelID = null;
			currentState = null;
			currentCoords = null;
		} else { // find track and step (if any) associated with selected point
			Step step = null;
			TTrack track = null;
			ArrayList<TTrack> list = getTracks();
			for (int it = 0, n = list.size(); it < n; it++) {
				track = list.get(it);
				step = track.getStep(point, this);
				if (step != null)
					break;
			}
			selectedStep = step;
			if (step == null) { // non-track TPoint was selected
				boolean ignore = autoTracker != null && autoTracker.getWizard().isVisible()
						&& (point instanceof AutoTracker.Corner || point instanceof AutoTracker.Handle
								|| point instanceof AutoTracker.Target);
				if (!ignore)
					setSelectedTrack(null);
			} else { // TPoint is associated with a step and track
				setSelectedTrack(track);
				step.repaint();
				// save position and state of newly selected point and/or track
				if (prevPoint != point) {
					boolean trackEdit = point.isTrackEditTrigger();
					boolean coordsEdit = point.isCoordsEditTrigger();
					pointState.setLocation(point);
					if (trackEdit && coordsEdit) {
						currentState = new XMLControlElement(track);
						currentCoords = new XMLControlElement(getCoords());
					} else if (trackEdit) {
						currentState = new XMLControlElement(track);
						if (!selectedSteps.contains(step) && !selectedSteps.isModified) {
							selectedSteps.clear();
						}
					} else if (coordsEdit) {
						currentState = new XMLControlElement(getCoords());
					} else if (point.isStepEditTrigger()) {
						currentState = new XMLControlElement(step);
					}
				}
			}
			selectingPanelID = panelID;
			requestFocusInWindow();
		}
		if (selectedStep != null)
			selectedSteps.add(selectedStep);
		selectedSteps.isModified = false;
		firePropertyChange(PROPERTY_TRACKERPANEL_SELECTEDPOINT, prevPoint, point);
	}

	/**
	 * Returns pointID if this panel is actively selecting. 
	 *
	 * @return
	 */
	public Integer getSelectingPanelID() {
		return selectingPanelID;
	}

	/**
	 * Gets the selected point.
	 *
	 * @return the selected point
	 */
	public TPoint getSelectedPoint() {
		return selectedPoint;
	}

	/**
	 * Gets the selected step.
	 *
	 * @return the selected step
	 */
	public Step getSelectedStep() {
		return selectedStep;
	}

	/**
	 * Sets the magnification.
	 *
	 * @param magnification the desired magnification
	 */
	public void setMagnification(double magnification) {
		if (magnification == 0 || Double.isNaN(magnification))
			return;
		double prevZoom = getMagnification();
		Dimension prevSize = getPreferredSize();
		Point p1 = new TPoint(0, 0).getScreenPosition(this);
		if (prevSize.width == 1 && prevSize.height == 1) { // zoomed to fit
			double w = getImageWidth();
			double h = getImageHeight();
			Point p2 = new TPoint(w, h).getScreenPosition(this);
			prevSize.width = p2.x - p1.x;
			prevSize.height = p2.y - p1.y;
		}
		Dimension d;
		if (magnification < 0) {
			d = new Dimension(1, 1);
		} else {
			zoom = Math.min(Math.max(magnification, MIN_ZOOM), MAX_ZOOM);
			int w = (int) (imageWidth * zoom);
			int h = (int) (imageHeight * zoom);
			d = new Dimension(w, h);
		}
		setPreferredSize(d);
		firePropertyChange(PROPERTY_TRACKERPANEL_MAGNIFICATION, Double.valueOf(prevZoom), Double.valueOf(getMagnification()));
		// scroll and revalidate
		MainTView view = (getTFrame() == null ? null : getTFrame().getMainView(this));
		if (view != null) {
			view.scrollPane.revalidate();
			// this will fire a full panel repaint
			view.scrollToZoomCenter(getPreferredSize(), prevSize, p1);
			eraseAll();
		}
		zoomBox.hide();
	}

	/**
	 * Gets the magnification.
	 *
	 * @return the magnification
	 */
	public double getMagnification() {
		if (getPreferredSize().width == 1) { // zoomed to fit
			double w = getImageWidth();
			double h = getImageHeight();
			Dimension size = getSize();
			return Math.min(size.width / w, size.height / h);
		}
		return zoom;
	}

	/**
	 * Sets the image width in image units. Overrides VideoPanel method.
	 *
	 * @param w the width
	 */
	@Override
	public void setImageWidth(double w) {
		setImageSize(w, getImageHeight());
	}

	/**
	 * Sets the image height in image units. Overrides VideoPanel method.
	 *
	 * @param h the height
	 */
	@Override
	public void setImageHeight(double h) {
		setImageSize(getImageWidth(), h);
	}

	/**
	 * Sets the image size in image units.
	 *
	 * @param w the width
	 * @param h the height
	 */
	public void setImageSize(double w, double h) {
		super.setImageWidth(w);
		super.setImageHeight(h);
		TMat mat = getMat();
		if (mat != null)
			mat.refresh();
		if (getPreferredSize().width > 10) {
			setMagnification(getMagnification());
		}
		eraseAll();
		TFrame.repaintT(this);
		firePropertyChange(PROPERTY_TRACKERPANEL_SIZE, null, null);
	}

	/**
	 * Sets the visibility of the clip settings dialog.
	 *
	 * @param vis null to toggle visibility, otherwise usual true/false
	 */
	public ClipInspector setClipSettingsVisible(Boolean vis) {
		VideoClip clip = getPlayer().getVideoClip();
		ClipControl clipControl = getPlayer().getClipControl();
		TFrame frame = getTFrame();
		ClipInspector inspector = clip.getClipInspector(clipControl, frame);
		if ((vis == null || vis == Boolean.FALSE) && inspector.isVisible()) {
			inspector.setVisible(false);
			return inspector;
		}
		if (vis == Boolean.FALSE) {
			return inspector;
		}
		FontSizer.setFonts(inspector, FontSizer.getLevel());
		inspector.pack();
		TToolBar toolbar = getToolBar(true);
		if (!inspector.isPositioned) {
			inspector.isPositioned = true;
			// center inspector on the main view
			Rectangle rect = getVisibleRect();
			Point p = frame.getMainView(this).scrollPane.getLocationOnScreen();
			int x = p.x + (rect.width - inspector.getBounds().width) / 2;
			int y = p.y + (rect.height - inspector.getBounds().height) / 2;
			inspector.setLocation(x, y);	
			ComponentListener clipSettingsListener = new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent e) {
					toolbar.refresh(TToolBar.REFRESH__CLIP_SETTINGS_HIDDEN);
				}
			};
			inspector.addComponentListener(clipSettingsListener);
		}
		inspector.initialize();
		inspector.setVisible(true);
		toolbar.refresh(TToolBar.REFRESH__CLIP_SETTINGS_SHOWN);
		return inspector;
	}

	/**
	 * Sets the scroll pane.
	 *
	 * @param scroller the scroll pane containing this panel
	 */
	public void setScrollPane(JScrollPane scroller) {
		scrollPane = scroller;
	}

	/**
	 * Gets the preferred scrollable viewport size.
	 *
	 * @return the preferred scrollable viewport size
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * Gets the scrollable unit increment.
	 *
	 * @param visibleRect the rectangle currently visible in the scrollpane
	 * @param orientation the orientation of the scrollbar
	 * @param direction   the direction of movement of the scrollbar
	 * @return the scrollable unit increment
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 20;
	}

	/**
	 * Gets the scrollable block increment.
	 *
	 * @param visibleRect the rectangle currently visible in the scrollpane
	 * @param orientation the orientation of the scrollbar
	 * @param direction   the direction of movement of the scrollbar
	 * @return the scrollable block increment
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		int unitIncrement = getScrollableUnitIncrement(visibleRect, orientation, direction);
		if (orientation == SwingConstants.HORIZONTAL)
			return visibleRect.width - unitIncrement;
		return visibleRect.height - unitIncrement;
	}

	/**
	 * Gets whether this tracks the viewport width in a scrollpane.
	 *
	 * @return <code>true</code> if this tracks the width
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		if (scrollPane == null)
			return true;
		Dimension panelDim = getPreferredSize();
		Rectangle viewRect = scrollPane.getViewport().getViewRect();
		return viewRect.width > panelDim.width;
	}

	/**
	 * Gets whether this tracks the viewport height.
	 *
	 * @return <code>true</code> if this tracks the height
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		if (scrollPane == null)
			return true;
		Dimension panelDim = getPreferredSize();
		Rectangle viewRect = scrollPane.getViewport().getViewRect();
		return viewRect.height > panelDim.height;
	}

	/**
	 * Gets the units visibility.
	 *
	 * @return <code>true</code> if units are displayed
	 */
	public boolean isUnitsVisible() {
		return unitsVisible && lengthUnit != null && massUnit != null;
	}

	/**
	 * Sets the units visibility.
	 *
	 * @param visible <code>true</code> to display units
	 */
	public void setUnitsVisible(boolean visible) {
		if (visible == unitsVisible)
			return;
		unitsVisible = visible;
		refreshTrackBar();
		coordStringBuilder.setUnitsAndPatterns(getSelectedTrack(), "x", "y"); //$NON-NLS-1$ //$NON-NLS-2$
		if (getSelectedPoint() != null) {
			getSelectedPoint().showCoordinates(this);
		}
		firePropertyChange(PROPERTY_TRACKERPANEL_UNITS, false, true);
	}

	/**
	 * Gets the mass unit.
	 *
	 * @return the mass unit
	 */
	public String getMassUnit() {
		return massUnit;
	}

	/**
	 * Sets the mass unit.
	 *
	 * @param unit the mass unit
	 * @return true if unit was changed
	 */
	public boolean setMassUnit(String unit) {
		if (unit != null)
			unit = unit.trim();
		if ("".equals(unit)) //$NON-NLS-1$
			unit = null;
		if (massUnit != null && massUnit.equals(unit))
			return false;
		if (massUnit == null && unit == null)
			return false;
		// prevent numbers being set as units
		try {
			Double.parseDouble(unit);
			return false;
		} catch (Exception e) {
		}
		massUnit = unit;
		refreshTrackBar();
		//getTrackBar().refresh();
		firePropertyChange(PROPERTY_TRACKERPANEL_UNITS, false, true);
		return true;
	}

	/**
	 * Gets the length unit.
	 *
	 * @return the length unit
	 */
	public String getLengthUnit() {
		return lengthUnit;
	}

	/**
	 * Sets the length unit.
	 *
	 * @param unit the length unit
	 * @return true if unit was changed
	 */
	public boolean setLengthUnit(String unit) {
		if (unit != null)
			unit = unit.trim();
		if ("".equals(unit)) //$NON-NLS-1$
			unit = null;
		if (lengthUnit != null && lengthUnit.equals(unit))
			return false;
		if (lengthUnit == null && unit == null)
			return false;
		// prevent numbers being set as units
		try {
			Double.parseDouble(unit);
			return false;
		} catch (Exception e) {
		}
		lengthUnit = unit;
		refreshTrackBar();
		//getTrackBar().refresh();
		coordStringBuilder.setUnitsAndPatterns(getSelectedTrack(), "x", "y"); //$NON-NLS-1$ //$NON-NLS-2$
		if (getSelectedPoint() != null) {
			getSelectedPoint().showCoordinates(this);
		}
		firePropertyChange(PROPERTY_TRACKERPANEL_UNITS, false, true);
		return true;
	}

	/**
	 * Gets the units for a given track and variable.
	 *
	 * @param track the track
	 * @param var   the variable
	 * @return the units
	 */
	public String getUnits(TTrack track, String var) {
		if (!isUnitsVisible())
			return ""; //$NON-NLS-1$
		String dimensions = TTrack.getVariableDimensions(track, var);
		if (dimensions == null)
			return ""; //$NON-NLS-1$
		String sq = (dimensions.endsWith("TT") ? Tracker.SQUARED : "");
		String sp = " "; //$NON-NLS-1$
		switch (dimensions) {
		case "T": //$NON-NLS-1$
			return sp + timeUnit;
		case "M": //$NON-NLS-1$
			return sp + massUnit;
		case "L": //$NON-NLS-1$
			return sp + lengthUnit;
		case "L/T": //$NON-NLS-1$
		case "L/TT": //$NON-NLS-1$
			return sp + lengthUnit + "/" + timeUnit + sq; //$NON-NLS-1$
		case "ML/T": //$NON-NLS-1$
		case "ML/TT": //$NON-NLS-1$
			return sp + massUnit + Tracker.DOT + lengthUnit + "/" + timeUnit + sq; //$NON-NLS-1$
		case "MLL/TT": //$NON-NLS-1$
			return sp + massUnit + Tracker.DOT + lengthUnit + sq + "/" + timeUnit + sq; //$NON-NLS-1$
		case "A/T":
		case "A/TT":
			TFrame frame = getTFrame();
			String angUnit = frame != null && frame.isAnglesInRadians() ? "" : Tracker.DEGREES; //$NON-NLS-1$
			return sp + angUnit + "/" + timeUnit + sq; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns true if mouse coordinates are displayed. Overrides DrawingPanel method
	 * to report false if a point is selected.
	 *
	 * @return <code>true</code> if mouse coordinates are displayed
	 */
	@Override
	public boolean isShowCoordinates() {
		return showCoordinates && getSelectedPoint() == null;
	}

	/**
	 * Shows a message in BR corner. Overrides DrawingPanel method.
	 *
	 * @param msg the message
	 */
	@Override
	public void setMessage(String msg) {
		// BH 2020.04.06 this is a VERY expensive operation.
		if (!OSPRuntime.isJS && !OSPRuntime.isMac())
			super.setMessage(msg);
	}

	/**
	 * See importDataAsync
	 * 
	 * @param dataString
	 * @param source
	 */
	@Deprecated
	protected void importData(String dataString, Object source) {
		importDataAsync(dataString, source, null);
	}
	
	/**
	 * Imports Data from a data string (delimited fields) into a DataTrack. The data
	 * string must be parsable by DataTool. If the string is a path, an attempt is
	 * made to get the data string with ResourceLoader.
	 * 
	 * Optionally asynchronous (required async for JavaScript)
	 * 
	 * Source object (model) may be String path, JPanel controlPanel, Tool tool, etc
	 * 
	 * @param dataString delimited fields parsable by DataTool, or a path to a
	 *                   Resource
	 * @param source     the data source (may be null)
	 * @param whenDone   Runnable to run when complete
	 * @return the DataTrack with the Data (may return null)
	 */
	public void importDataAsync(String dataString, Object source, Runnable whenDone) {
		if (dataString == null) {
			// inform user
			JOptionPane.showMessageDialog(frame, TrackerRes.getString("TrackerPanel.Dialog.NoData.Message"), //$NON-NLS-1$
					TrackerRes.getString("TrackerPanel.Dialog.NoData.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		// if dataString is parsable data, parse and import it
		DatasetManager[] datasetManager = DataTool.parseData(dataString, null);
		if (datasetManager == null) {

			// assume dataString is a resource path, read the resource and call this again
			// with path as source
			String path = dataString;
			importDataAsync(ResourceLoader.getString(path), path, whenDone);
			return;
		}
		DataTrack dt = importDatasetManager(datasetManager[0], source);
		if (dt instanceof ParticleDataTrack) {
			((ParticleDataTrack) dt).prevDataString = dataString;
		}
		if (whenDone != null)
			whenDone.run();
	}

	/**
	 * Imports DatasetManager from a source into a DataTrack. Data must include "x" and "y"
	 * columns (may be unnamed), may include "t". DataTrack is the first one found
	 * that matches the Data name or ID. If none found, a new DataTrack is created.
	 * Source object (model) may be String path, JPanel controlPanel, Tool tool,
	 * null, etc
	 * 
	 * @param data   the Data to import
	 * @param source the data source (may be null)
	 * @return the DataTrack with the Data (may return null)
	 */
	@Override
	public DataTrack importData(Data data, Object source) {
		return importDatasetManager((DatasetManager) data, source);
	}

	private DataTrack importDatasetManager(DatasetManager data, Object source) {
		if (data == null)
			return null;
		// find DataTrack with matching name or ID
		
		ParticleDataTrack dataTrack = ParticleDataTrack.getTrackForData(data, this);

		// load data into DataTrack
		try {
			// create a new DataTrack if none exists
			if (dataTrack == null) {
				dataTrack = new ParticleDataTrack(data, source);
				dataTrack.setColorToDefault(getDrawablesTemp(PointMass.class).size());
				clearTemp();
				addTrack(dataTrack);
				setSelectedPoint(null);
				selectedSteps.clear();
				setSelectedTrack(dataTrack);
				dataTrack.getDataClip().setClipLength(-1); // sets clip length to data length
				VideoClip videoClip = getPlayer().getVideoClip();
				dataTrack.setStartFrame(videoClip.getStartFrameNumber());
				dataTrack.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null);
				dataTrack.getModelBuilder().setVisible(true);
				final ParticleDataTrack dt = dataTrack;
				EventQueue.invokeLater(() -> {
						dt.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null);
				});
			} else {
				// set data for existing DataTrack
				dataTrack.setData(data);
			}
		} catch (Exception e) {
			// inform user
			JOptionPane.showMessageDialog(frame, TrackerRes.getString("TrackerPanel.Dialog.Exception.Message") + ":\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ e.getClass().getSimpleName() + ": " + e.getMessage(), //$NON-NLS-1$
					TrackerRes.getString("TrackerPanel.Dialog.Exception.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			OSPLog.warning(e.getClass().getSimpleName() + ": " + e.getMessage()); //$NON-NLS-1$
			dataTrack = null;
		}
		return dataTrack;
	}


	/**
	 * Refreshes all data in tracks and views.
	 */
	protected void refreshTrackData(int mode) {
		// turn on autorefresh
		//OSPLog.debug("TrackerPanel.refreshTrackData " + Tracker.allowDataRefresh);
		boolean auto = isAutoRefresh;
		isAutoRefresh = true;
		firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, mode, null); // causes full view rebuild
		isAutoRefresh = auto;
	}

	@Override
	protected void refreshDecimalSeparators() {
		super.refreshDecimalSeparators();
		if (coordStringBuilder != null)
			coordStringBuilder.refreshDecimalSeparators();
		if (getSelectedPoint() != null) {
			getSelectedPoint().showCoordinates(this);
		}

		// refresh all track fields
		ArrayList<TTrack> tracks = getTracksTemp();
		for (int i = 0, n = tracks.size(); i < n; i++) {
			tracks.get(i).refreshDecimalSeparators();
		}
		tracks.clear();

		// refresh all plot and table views
		// just repaint--no data change at all
		refreshTrackData(DataTable.MODE_FORMAT);

		// refresh modelbuilder and databuilder
		if (modelBuilder != null) {
			modelBuilder.repaint();
		}
		if (dataBuilder != null) {
			dataBuilder.repaint();
		}
		// refresh DataTool
		if (getTFrame() != null && frame.getSelectedPanel() == this
				&& DataTool.getTool(false) != null) {
			DataTool.getTool(false).refreshDecimalSeparators();
		}
		
		// repaint tracks with readouts
		ArrayList<TapeMeasure> tapes = getDrawablesTemp(TapeMeasure.class);
		for (int i = 0, n = tapes.size(); i < n; i++) {
			TapeMeasure tape = tapes.get(i);
			tape.repaint(panelID);
		}
		tapes.clear();
		
		ArrayList<Protractor> prots = getDrawablesTemp(Protractor.class);
		for (int i = 0, n = prots.size(); i < n; i++) {
			Protractor p = prots.get(i);
			p.repaint(panelID);
		}
		prots.clear();
		
	}

	/**
	 * Gets the most recent mouse event.
	 *
	 * @return the MouseEvent
	 */
	protected MouseEvent getMouseEvent() {
		return mouseEvent;
	}

	/**
	 * Gets the popup menu. Overrides DrawingPanel method.
	 */
	@Override
	public JPopupMenu getPopupMenu() {
		//OSPLog.debug("TrackerPanel.getPopupMenu " + Tracker.allowMenuRefresh);
		if (!Tracker.allowMenuRefresh)
			return null;

		if (getTFrame() == null)
			return super.getPopupMenu();
		MainTView mainView = getTFrame().getMainView(this);
		return mainView.getPopupMenu();
	}

	protected JPopupMenu getPopup() {
		return (popup != null ? popup: ( popup = new JPopupMenu() {
			@Override
			public void setVisible(boolean vis) {
				if (!vis) {
					zoomBox.hide();
				}
				super.setVisible(vis);
			}
		}));
	}
	
	/**
	 * Gets the units dialog.
	 * 
	 * @return the units dialog
	 */
	public UnitsDialog getUnitsDialog() {
		if (unitsDialog == null) {
			unitsDialog = new UnitsDialog(this);
			unitsDialog.setFontLevel(FontSizer.getLevel());
			// center on screen
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - unitsDialog.getBounds().width) / 2;
			int y = (dim.height - unitsDialog.getBounds().height) / 2;
			unitsDialog.setLocation(x, y);
		} else {
			unitsDialog.setFontLevel(FontSizer.getLevel());
		}
		return unitsDialog;
	}

	/**
	 * Gets the attachment dialog for attaching measuring tool points to point
	 * masses.
	 * 
	 * @param track a measuring tool
	 * @return the attachment dialog
	 */
	public AttachmentDialog getAttachmentDialog(TTrack track) {
		if (attachmentDialog == null) {
			attachmentDialog = new AttachmentDialog(track);
			attachmentDialog.setFontLevel(FontSizer.getLevel());
			// center on screen
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - attachmentDialog.getBounds().width) / 2;
			int y = (dim.height - attachmentDialog.getBounds().height) / 2;
			attachmentDialog.setLocation(x, y);
		} else {
			attachmentDialog.setFontLevel(FontSizer.getLevel());
			attachmentDialog.setMeasuringTool(track);
		}
		return attachmentDialog;
	}

	/**
	 * Gets the PasteDataDialog for pasting data into TrackerJS.
	 * 
	 * @return the PasteDataDialog
	 */
	public PasteDataDialog getPasteDataDialog() {
		if (pasteDataDialog == null) {
			pasteDataDialog = new PasteDataDialog(this);
		}
		pasteDataDialog.setFontLevel(FontSizer.getLevel());
		return pasteDataDialog;
	}

	/**
	 * Gets the plot guest dialog for comparing multiple track data in a single
	 * plot.
	 * 
	 * @param plot a TrackPlottingPanel
	 * @return the plot guest dialog
	 */
	public PlotGuestDialog getPlotGuestDialog(TrackPlottingPanel plot) {
		if (guestsDialog == null) {
			guestsDialog = new PlotGuestDialog(this);
			guestsDialog.setPlot(plot);
			FontSizer.setFonts(guestsDialog, FontSizer.getLevel());
			// center on screen
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - guestsDialog.getBounds().width) / 2;
			int y = (dim.height - guestsDialog.getBounds().height) / 2;
			guestsDialog.setLocation(x, y);
		} else {
			guestsDialog.setPlot(plot);
			FontSizer.setFonts(guestsDialog, FontSizer.getLevel());
		}
		guestsDialog.pack();
		return guestsDialog;
	}

	/**
	 * Gets the data builder for defining custom data functions.
	 * 
	 * @return the data builder
	 */
	protected FunctionTool getDataBuilder() {
		if (dataBuilder == null) { // create new tool if none exists
			dataBuilder = new TrackDataBuilder(this);
			dataBuilder.setHelpPath("data_builder_help.html"); //$NON-NLS-1$
			dataBuilder.addPropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_PANEL, this);
			dataBuilder.addPropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION, this);
			dataBuilder.addPropertyChangeListener(FunctionEditor.PROPERTY_FUNCTIONEDITOR_DESCRIPTION, this);
			dataBuilder.addPropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_VISIBLE, this);
			dataBuilder.setFontLevel(FontSizer.getLevel());
		}
		return dataBuilder;
	}

	/**
	 * Gets the Algorithms dialog.
	 *
	 * @return the properties dialog
	 */
	protected DerivativeAlgorithmDialog getAlgorithmDialog() {
		if (algorithmDialog == null) {
			algorithmDialog = new DerivativeAlgorithmDialog(this);
			algorithmDialog.setFontLevel(FontSizer.getLevel());
		}
		return algorithmDialog;
	}

	/**
	 * Gets the next available name (and color, based on the attached suffix) for a track.
	 * 
	 * @param name      the default name with no letter suffix
	 * @param connector the string connecting the name and letter
	 * @return name + connector + letter or null
	 */
	protected String getNextName(String name, String connector) {
		String p = name + connector;
		ArrayList<TTrack> list = getTracksTemp();
		int n = list.size();
		String proposed = null;
		// test A-Z
		for (int i = 65; i <= 90 && proposed == null; i++) {
			proposed = p + (char) i;
			for (int it = 0; it < n; it++) {
				if (proposed.equals(list.get(it).getName())) {
					proposed = null;
					break;
				}
			}
		}
		clearTemp();
		return proposed;
	}

	/**
	 * Restores the views to a non-maximized state.
	 */
	protected void restoreViews() {
		TFrame frame = getTFrame();
		if (frame != null) {
			int n = frame.getMaximizedView();
			switch (n) {
			case TView.VIEW_UNSET:
				return;
			case TView.VIEW_MAIN:
				getTrackBar(true).maximizeButton.doClick(0);
				break;
			default:
				TViewChooser viewChooser = frame.getViewChoosers(this)[n];
				viewChooser.restore();
				break;
			}
		}
	}

	/**
	 * Sets the cursor to a crosshair when the selected track is marking and is
	 * unmarked on the current frame. Also displays hints as a side effect.
	 *
	 * @param invert true to invert the normal state
	 * @param e      an input event
	 * @return true if marking (ie next mouse click will mark a TPoint)
	 */
	protected boolean setCursorForMarking(boolean invert, InputEvent e) {
		if (isClipAdjusting() || Tracker.isZoomInCursor(getCursor()) || Tracker.isZoomOutCursor(getCursor()))
			return false;
		boolean markable = false;
		boolean marking = false;
		selectedTrack = getSelectedTrack();
		int n = getFrameNumber();
		if (selectedTrack != null) {
			markable = !(selectedTrack.isStepComplete(n) || selectedTrack.isLocked()
					|| popup != null && popup.isVisible());
			marking = markable && (selectedTrack.isMarkByDefault() != invert);
		}
		Interactive iad = getTracksTemp().isEmpty() || mouseEvent == null ? null : getInteractive();
		clearTemp();
		if (marking) {
			setMouseCursor(selectedTrack.getMarkingCursor(e));
			if (Tracker.showHints) {
				String msg = null;
				switch (selectedTrack.ttype) {
				case TTrack.TYPE_POINTMASS:
					msg = (selectedTrack.getStep(n) == null ?
						"PointMass.Hint.Marking" //$NON-NLS-1$
						: "PointMass.Remarking.Hint"); //$NON-NLS-1$
					break;
				case TTrack.TYPE_VECTOR:
					msg = (selectedTrack.getStep(n) == null ?
						"Vector.Hint.Marking" //$NON-NLS-1$
					  : "Vector.Remarking.Hint"); //$NON-NLS-1$
					break;
				case TTrack.TYPE_RGBREGION:
					msg = "RGBRegion.Hint.Marking"; //$NON-NLS-1$
					break;
				case TTrack.TYPE_LINEPROFILE:
					msg = "LineProfile.Hint.Marking"; //$NON-NLS-1$
					break;
				}
				if (msg != null)
					setMessage(TrackerRes.getString(msg));
			} else
				setMessage(""); //$NON-NLS-1$
		} else if (iad instanceof TPoint) {
			setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			// identify associated track and display its hint
			ArrayList<TTrack> list = getTracksTemp();
			for (int it = 0, ni = list.size(); it < ni; it++) {
				TTrack track = list.get(it);
				Step step = track.getStep((TPoint) iad, this);
				if (step != null) {
					setMessage(track.getMessage());
					break;
				}
			}
			clearTemp();
		} else { // no point selected
			setMouseCursor(Cursor.getDefaultCursor());
			// display selected track hint
			getExportableTracks(); // needed for correct hint
			if (Tracker.showHints && selectedTrack != null) {
				setMessage(selectedTrack.getMessage());
			} else if (!Tracker.startupHintShown || getVideo() != null
					|| (exportableTracks != null && !exportableTracks.isEmpty())) {
				Tracker.startupHintShown = false;
				if (!Tracker.showHints)
					setMessage(""); //$NON-NLS-1$
				// show hints
				else if (getVideo() == null) // no video
					setMessage(TrackerRes.getString("TrackerPanel.NoVideo.Hint")); //$NON-NLS-1$
				else if (hasToolBar() && getToolBar(true).notYetCalibrated) {
					if (getVideo().getWidth() == 720 && getVideo().getFilterStack().isEmpty()) // DV video format
						setMessage(TrackerRes.getString("TrackerPanel.DVVideo.Hint")); //$NON-NLS-1$
					else if (getPlayer().getVideoClip().isDefaultState())
						setMessage(TrackerRes.getString("TrackerPanel.SetClip.Hint")); //$NON-NLS-1$
					else
						setMessage(TrackerRes.getString("TrackerPanel.CalibrateVideo.Hint")); //$NON-NLS-1$
				} else if (getAxes() != null && getAxes().notyetShown)
					setMessage(TrackerRes.getString("TrackerPanel.ShowAxes.Hint")); //$NON-NLS-1$
				else if (exportableTracks == null || exportableTracks.isEmpty())
					setMessage(TrackerRes.getString("TrackerPanel.NoTracks.Hint")); //$NON-NLS-1$
				else
					setMessage(""); //$NON-NLS-1$
			}
		}
		return marking;
	}
	
	private boolean isClipAdjusting() {
		return (getPlayer() != null && getPlayer().getVideoClip().isAdjusting());
	}

	/**
	 * Handles keypress events for selected points.
	 *
	 * @param e the key event
	 */
	protected void handleKeyPress(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F1) {
			// help
			// !BH 2021.12.15 problem with ordering of "instanceof" checks
			TFrame frame = getTFrame();
			if (frame != null) {
				String key = null;
				if (selectedTrack == null) {
					key = "help"; //$NON-NLS-1$
				} else {
					switch (selectedTrack.ttype) {
					case TTrack.TYPE_CALIBRATION:
						key = "calibration"; //$NON-NLS-1$
						break;
					case TTrack.TYPE_CIRCLEFITTER:
						break;
					case TTrack.TYPE_COORDAXES:
						key = "axes"; //$NON-NLS-1$
						break;
					case TTrack.TYPE_LINEPROFILE:
						key = "profile"; //$NON-NLS-1$
						break;
					case TTrack.TYPE_OFFSETORIGIN:
						key = "offset"; //$NON-NLS-1$
						break;
					case TTrack.TYPE_PERSPECTIVE:
						break;
					case TTrack.TYPE_POINTMASS:
						key = (selectedTrack instanceof CenterOfMass ? "cm" //$NON-NLS-1$
								: selectedTrack instanceof ParticleModel ?
								// includes AnalyticalParticle, DynamicParticle, DynamicParticlePolar,
								// ParticleDataTrack
										"particle" //$NON-NLS-1$
										: "pointmass"); // $NON-NLS-2$
						break;
					case TTrack.TYPE_PROTRACTOR:
						break;
					case TTrack.TYPE_RGBREGION:
						key = "rgbregion"; //$NON-NLS-1$
						break;
					case TTrack.TYPE_TAPEMEASURE:
						key = "tape"; //$NON-NLS-1$
						break;
					case TTrack.TYPE_VECTOR:
						key = (selectedTrack instanceof VectorSum ? "vectorsum" //$NON-NLS-1$
								: "vector"); //$NON-NLS-1$
						break;
					}
				}
				if (key != null)
					frame.showHelp(key, 0); // $NON-NLS-1$
			}
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			TTrack track = getSelectedTrack();
			if (track != null) {
				Step step = getSelectedStep();
				if (step != null) {
					if (e.isControlDown() || e.isShiftDown())
						step = track.getPreviousVisibleStep(step, this);
					else
						step = track.getNextVisibleStep(step, this);
					if (step != null) {
						TPoint p = step.getDefaultPoint();
						p.showCoordinates(this);
						setSelectedPoint(p);
					}
				}
			}
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			// delete selected steps
			if (selectedPoint != null && selectingPanelID == panelID) {
				deletePoint(selectedPoint);
			} else {
				deleteSelectedSteps();
			}
			return;
		}

		// move selected point(s) when arrow key pressed
		double delta = e.isShiftDown() ? 10 : 1;
		double dx = 0, dy = 0;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			dy = -delta;
			break;

		case KeyEvent.VK_DOWN:
			dy = delta;
			break;

		case KeyEvent.VK_RIGHT:
			dx = delta;
			break;

		case KeyEvent.VK_LEFT:
			dx = -delta;
			break;
		}

		if (dx == 0 && dy == 0)
			return;
		selectedSteps.setChanged(true);
		for (Step step : selectedSteps) {
			TPoint point = step.points[0];
			if (point == selectedPoint)
				continue;
			Point p = point.getScreenPosition(this);
			p.setLocation(p.x + dx, p.y + dy);
			point.setScreenPosition(p.x, p.y, this, e);
		}
		if (selectedPoint != null) {
			Point p = selectedPoint.getScreenPosition(this);
			p.setLocation(p.x + dx, p.y + dy);
			selectedPoint.setScreenPosition(p.x, p.y, this, e);
		}
		// check selected point since setting screen position can deselect it!
		if (selectedPoint != null)
			selectedPoint.showCoordinates(this);
		else
			setMessage("", MessageDrawable.BOTTOM_LEFT); //$NON-NLS-1$
		if (selectedStep == null)
			TFrame.repaintT(this);
	}

	/**
	 * Returns true if this is the default configuration.
	 *
	 * @return true if this is the default configuration
	 */
	public boolean isDefaultConfiguration() {
		return Tracker.areEqual(getEnabled(), Tracker.defaultConfig);
	}

	/**
	 * Gets the enabled property set.
	 *
	 * @return the set of enabled properties
	 */
	public Set<String> getEnabled() {
		if (enabled == null)
			enabled = new TreeSet<String>();
		return enabled;
	}

	/**
	 * Sets the enabled property set.
	 *
	 * @param enable the set of enabled properties
	 */
	public void setEnabled(Set<String> enable) {
		if (enable != null) {
			enabled = getEnabled();
			enabled.clear();
			enabled.addAll(enable);
			enabledCount++;
		}
	}

	/**
	 * Gets the enabled state for the specified key.
	 *
	 * @param key the string key
	 * @return true if enabled
	 */
	public boolean isEnabled(String key) {
		if (key == null)
			return false;
		return getEnabled().contains(key);
	}

	/**
	 * Sets the enabled state for the specified key.
	 *
	 * @param key    the string key
	 * @param enable true to enable the key
	 */
	public void setEnabled(String key, boolean enable) {
		if (key == null)
			return;
		if (enable)
			getEnabled().add(key);
		else
			getEnabled().remove(key);
	}

	/**
	 * REturns true if any new.trackType is enabled.
	 *
	 * @return true if enabled
	 */
	public boolean isCreateTracksEnabled() {
		return isEnabled("new.pointMass") //$NON-NLS-1$
				|| isEnabled("new.cm") //$NON-NLS-1$
				|| isEnabled("new.vector") //$NON-NLS-1$
				|| isEnabled("new.vectorSum") //$NON-NLS-1$
				|| isEnabled("new.lineProfile") //$NON-NLS-1$
				|| isEnabled("new.RGBRegion") //$NON-NLS-1$
				|| isEnabled("new.tapeMeasure") //$NON-NLS-1$
				|| isEnabled("new.protractor") //$NON-NLS-1$
				|| isEnabled("new.circleFitter") //$NON-NLS-1$
				|| isEnabled("new.analyticParticle") //$NON-NLS-1$
				|| isEnabled("new.dynamicParticle") //$NON-NLS-1$
				|| isEnabled("new.dynamicTwoBody") //$NON-NLS-1$
				|| isEnabled("new.dataTrack"); //$NON-NLS-1$
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		boolean doSnap = false;
		boolean isAdjusting = false;
		String name = e.getPropertyName();
		if (Tracker.timeLogEnabled)
			Tracker.logTime(getClass().getSimpleName() + hashCode() + " property change " + name); //$NON-NLS-1$
		TTrack track;
		ParticleModel model;
		TMenuBar mbar;
		switch (name) {
		case Video.PROPERTY_VIDEO_SIZE:
			super.propertyChange(e);
			getTFrame().holdPainting(false);
			notifyLoadingComplete();
			break;
		case AsyncVideoI.PROPERTY_ASYNCVIDEOI_READY:
			if (loader == null || ((Loader) loader).clip.getVideo() != e.getSource()) {
				return;
			}
			super.propertyChange(e);
			getTFrame().holdPainting(false);
			notifyLoadingComplete();
//			TFrame.repaintT(this);
			break;
		case TTrack.PROPERTY_TTRACK_STEP:
		case TTrack.PROPERTY_TTRACK_STEPS: // from tracks //$NON-NLS-1$
			track = (TTrack) e.getSource();
			track.invalidateData(Boolean.FALSE);
			if (!track.isDependent()) { // ignore dependent tracks
				changed = true;
			}
			if (track == getSelectedTrack()) {
				TPoint p = getSelectedPoint();
				if (p != null)
					p.showCoordinates(this);
			}
			TFrame.repaintT(this);
			if (name == TTrack.PROPERTY_TTRACK_STEPS) {
				refreshTrackBar();
//				getTrackBar().refresh();
			}
			break;
		case TTrack.PROPERTY_TTRACK_MASS: // from point masses //$NON-NLS-1$
			firePropertyChange(TTrack.PROPERTY_TTRACK_MASS, null, null); // to motion control //$NON-NLS-1$
			break;
		case TTrack.PROPERTY_TTRACK_NAME: // from tracks //$NON-NLS-1$
			refreshNotesDialog();
			break;
		case TTrack.PROPERTY_TTRACK_FOOTPRINT: // from tracks //$NON-NLS-1$
			Footprint footprint = (Footprint) e.getNewValue();
			if (footprint instanceof ArrowFootprint)
				firePropertyChange(TTrack.PROPERTY_TTRACK_MASS, null, null); // to track control //$NON-NLS-1$
			break;
		case VideoPlayer.PROPERTY_VIDEOPLAYER_VIDEOCLIP: // from videoPlayer //$NON-NLS-1$
			// replace coords and videoclip listeners
			ImageCoordSystem oldCoords = coords;
			coords.removePropertyChangeListener(this);
			super.propertyChange(e); // replaces video, videoclip listeners, (possibly) coords
			coords.addPropertyChangeListener(this);
			firePropertyChange(Video.PROPERTY_VIDEO_COORDS, oldCoords, coords); // to tracks //$NON-NLS-1$
			firePropertyChange(PROPERTY_TRACKERPANEL_VIDEO, null, null); // to TMenuBar & views //$NON-NLS-1$
			TMat mat = getMat();
			if (mat != null) {
				mat.invalidate();
			}
			if (video != null) {
				video.setProperty("measure", null); //$NON-NLS-1$
				if (video instanceof SmoothPlayable) {
					// if xuggle video, set smooth play per preferences
					((SmoothPlayable) video).setSmoothPlay(!Tracker.isXuggleFast);
				}
			}
			doSnap = true;
			changed = true;
			break;
		case VideoPlayer.PROPERTY_VIDEOPLAYER_STEPNUMBER: // from videoPlayer //$NON-NLS-1$
			// overrides VideoPanel repaint
			setSelectedPoint(null);
			selectedSteps.clear();
			if (getVideo() != null && !getVideo().getFilterStack().isEmpty()) {
				ArrayList<Filter> filters = getVideo().getFilterStack().getFilters();
				for (int i = 0, n = filters.size(); i < n; i++) {
					Filter next = filters.get(i);
					if (next instanceof SumFilter) {
						((SumFilter) next).addNextImage();
					}
				}
			}
			TFrame.repaintT(this);
			VideoCaptureTool grabber = VideoGrabber.VIDEO_CAPTURE_TOOL;
			if (grabber != null && grabber.isVisible() && grabber.isRecording()) {
				EventQueue.invokeLater(() -> {
					VideoGrabber.getTool().addFrame(getMattedImage());
				});
			}

			// show crosshair cursor if shift key down or automarking
			boolean invertCursor = isShiftKeyDown;
			setCursorForMarking(invertCursor, null);
			firePropertyChange(PROPERTY_TRACKERPANEL_STEPNUMBER, null, e.getNewValue()); // to views //$NON-NLS-1$
			doSnap = true;
			break;
		case Video.PROPERTY_VIDEO_COORDS: // from video //$NON-NLS-1$
			// replace coords and listeners
			coords.removePropertyChangeListener(this);
			coords = (ImageCoordSystem) e.getNewValue();
			coords.addPropertyChangeListener(this);
			firePropertyChange(Video.PROPERTY_VIDEO_COORDS, null, coords); // to tracks //$NON-NLS-1$
			firePropertyChange(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, null, null); // to tracks/views //$NON-NLS-1$
			doSnap = true;
			break;
		case Video.PROPERTY_VIDEO_IMAGE: // from video //$NON-NLS-1$
			firePropertyChange(PROPERTY_TRACKERPANEL_IMAGE, null, null); // to tracks/views //$NON-NLS-1$
			mbar = getMenuBar(false);
			if (mbar != null)
				mbar.checkMatSize();
			TFrame.repaintT(this);
			changed = true;
			break;
		case Video.PROPERTY_VIDEO_FILTERCHANGED: // from video //$NON-NLS-1$
			Filter filter = (Filter) e.getNewValue();
			String prevState = (String) e.getOldValue();
			XMLControl control = new XMLControlElement(prevState);
			Undo.postFilterEdit(this, filter, control);
			break;
		case Video.PROPERTY_VIDEO_VIDEOVISIBLE: // from video //$NON-NLS-1$
			firePropertyChange(PROPERTY_TRACKERPANEL_VIDEOVISIBLE, null, null); // to views //$NON-NLS-1$
			break;
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM: // from coords //$NON-NLS-1$
			changed = true;
			doSnap = true;
			// invalidate user track data BEFORE informing views
			ArrayList<TTrack> tracks = getUserTracks();
			for (int i = 0; i < tracks.size(); i++) {
				tracks.get(i).dataValid = false;
			}
			// pass this on to TView classes
			firePropertyChange(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, null, null); // to tracks/views //$NON-NLS-1$
			break;
		case ImageCoordSystem.PROPERTY_COORDS_LOCKED: // from coords //$NON-NLS-1$
			// pass this on
			firePropertyChange(TTrack.PROPERTY_TTRACK_LOCKED, null, null); // to tracker frame //$NON-NLS-1$
			break;
		case VideoPlayer.PROPERTY_VIDEOPLAYER_PLAYING: // from player //$NON-NLS-1$
			if (!((Boolean) e.getNewValue()).booleanValue()) {
				ArrayList<ParticleModel> list = getDrawablesTemp(ParticleModel.class);
				for (int m = 0, n = list.size(); m < n; m++) {
					list.get(m).refreshDerivsIfNeeded();
				}
				list.clear();
			}
			break;
		case Trackable.PROPERTY_ADJUSTING: // from videoClip //$NON-NLS-1$
			isAdjusting = true;
			// fall through
		case VideoClip.PROPERTY_VIDEOCLIP_STARTFRAME: // from videoClip //$NON-NLS-1$
		case VideoClip.PROPERTY_VIDEOCLIP_STEPSIZE: // from videoClip //$NON-NLS-1$
		case VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT: // from videoClip //$NON-NLS-1$
		case VideoClip.PROPERTY_VIDEOCLIP_STARTTIME: // from videoClip //$NON-NLS-1$
		case ClipControl.PROPERTY_CLIPCONTROL_FRAMEDURATION: // from clipControl //$NON-NLS-1$
			changed = true;
			if (modelBuilder != null)
				modelBuilder.refreshSpinners();
			if (getMat() != null) {
				getMat().invalidate();
			}
			if (getVideo() != null) {
				getVideo().setProperty("measure", null); //$NON-NLS-1$
			}
			firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, e.getOldValue(), isAdjusting ? e.getNewValue() : null); // to
																													// views
																													// //$NON-NLS-1$
			// pass this on to particle models and PencilControl
			firePropertyChange(name, e.getSource(), name == Trackable.PROPERTY_ADJUSTING ? e.getNewValue() : null);
			if (getSelectedPoint() != null) {
				getSelectedPoint().showCoordinates(this);
				TFrame frame = getTFrame();
				if (frame != null) {
					// BH Q: Is this possible??
					refreshTrackBar();
					// getTrackBar().refresh();
				}
			}
			ArrayList<TTrack> list = getUserTracks();
			for (int it = 0, ni = list.size(); it < ni; it++) {
				list.get(it).erase(panelID);
			}
			TFrame.repaintT(this);
			break;
		case VideoClip.PROPERTY_VIDEOCLIP_FRAMECOUNT: // $NON-NLS-1$
			if (getVideo() == null && modelBuilder != null)
				modelBuilder.refreshSpinners();
			break;
		case FunctionEditor.PROPERTY_FUNCTIONEDITOR_DESCRIPTION: // from DataBuilder //$NON-NLS-1$
		case FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION: // from DataBuilder //$NON-NLS-1$
			changed = true;
			firePropertyChange(FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION, null, e.getNewValue()); // to views //$NON-NLS-1$
			break;
		case FunctionTool.PROPERTY_FUNCTIONTOOL_PANEL:
			if (e.getSource() == modelBuilder) {
				FunctionPanel panel = (FunctionPanel) e.getNewValue();
				if (panel != null) { // new particle model panel added
					track = getTrack(panel.getName());
					if (track != null) {
//    			setSelectedTrack(track);
						model = (ParticleModel) track;
						modelBuilder.setSpinnerStartFrame(model.getStartFrame());
						int end = model.getEndFrame();
						if (end == Integer.MAX_VALUE) {
							end = getPlayer().getVideoClip().getLastFrameNumber();
						}
						modelBuilder.setSpinnerEndFrame(end);
					}
				}
				modelBuilder.refreshSpinners();
				String title = TrackerRes.getString("TrackerPanel.ModelBuilder.Title"); //$NON-NLS-1$
				panel = modelBuilder.getSelectedPanel();
				if (panel != null) {
					track = getTrack(panel.getName());
					if (track != null) {
						String type = track.getClass().getSimpleName();
						title += ": " + TrackerRes.getString(type + ".Builder.Title"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				modelBuilder.setTitle(title);
			}
			break;
		case TTrack.PROPERTY_TTRACK_MODELSTART:
			model = (ParticleModel) e.getSource();
			if (model.getName().equals(getModelBuilder().getSelectedName())) {
				modelBuilder.setSpinnerStartFrame(e.getNewValue());
			}
			break;
		case TTrack.PROPERTY_TTRACK_MODELEND:
			model = (ParticleModel) e.getSource();
			if (model.getName().equals(getModelBuilder().getSelectedName())) {
				int end = (Integer) e.getNewValue();
				if (end == Integer.MAX_VALUE) {
					end = getPlayer().getVideoClip().getLastFrameNumber();
				}
				modelBuilder.setSpinnerEndFrame(end);
			}
			break;
		case TTrack.PROPERTY_TTRACK_FORMAT: // data format has changed
			firePropertyChange(TTrack.PROPERTY_TTRACK_FORMAT, null, null); // to views //$NON-NLS-1$
			break;
		case TFrame.PROPERTY_TFRAME_RADIANANGLES: // angle format has changed //$NON-NLS-1$
			firePropertyChange(TFrame.PROPERTY_TFRAME_RADIANANGLES, null, e.getNewValue()); // to tracks //$NON-NLS-1$
			break;
		case ImageCoordSystem.PROPERTY_COORDS_FIXEDORIGIN:
		case ImageCoordSystem.PROPERTY_COORDS_FIXEDANGLE:
		case ImageCoordSystem.PROPERTY_COORDS_FIXEDSCALE:
			changed = true;
			firePropertyChange(name, e.getOldValue(), e.getNewValue()); // to tracks
			break;
		case FunctionTool.PROPERTY_FUNCTIONTOOL_VISIBLE:
			if (e.getSource() == dataBuilder)
				dataToolVisible = ((Boolean) e.getNewValue()).booleanValue();
			break;
		case Filter.PROPERTY_FILTER_VISIBLE:
			setSelectedPoint(null);
			selectedSteps.clear();
			break;
		case PerspectiveFilter.PROPERTY_PERSPECTIVEFILTER_PERSPECTIVE: // $NON-NLS-1$
			if (e.getNewValue() != null) {
				PerspectiveFilter filt = (PerspectiveFilter) e.getNewValue();
				track = new PerspectiveTrack(filt);
				addTrack(track);
			} else if (e.getOldValue() != null) {
				// clean up deleted perspective track and filter
				PerspectiveFilter filt1 = (PerspectiveFilter) e.getOldValue();
				PerspectiveTrack trk = PerspectiveTrack.filterMap.get(filt1);
				if (trk != null) {
					removeTrack(trk);
					trk.dispose();
					filt1.setVideoPanel(null);
				}
			}
			break;
		case VideoPlayer.PROPERTY_VIDEOPLAYER_STEPBUTTON:
		case VideoPlayer.PROPERTY_VIDEOPLAYER_BACKBUTTON:
		case VideoPlayer.PROPERTY_VIDEOPLAYER_SLIDER:
		case "inframe": // BH! never fired??
		case "outframe":// BH! never fired??
			if (Tracker.showHints) {
				Tracker.startupHintShown = false;
				String msg = "";
				if (e.getNewValue() == Boolean.TRUE) {
					switch (name) {
					case VideoPlayer.PROPERTY_VIDEOPLAYER_STEPBUTTON:
						msg = (TrackerRes.getString("VideoPlayer.Step.Hint")); //$NON-NLS-1$
						break;
					case VideoPlayer.PROPERTY_VIDEOPLAYER_BACKBUTTON:
						msg = (TrackerRes.getString("VideoPlayer.Back.Hint")); //$NON-NLS-1$
						break;
					case VideoPlayer.PROPERTY_VIDEOPLAYER_SLIDER:
						msg = (TrackerRes.getString("VideoPlayer.Slider.Hint")); //$NON-NLS-1$
						break;
					case "inframe": //$NON-NLS-1$
						// BH! never fired??
						msg = (TrackerRes.getString("VideoPlayer.StartFrame.Hint")); //$NON-NLS-1$
						break;
					case "outframe": //$NON-NLS-1$
						msg = (TrackerRes.getString("VideoPlayer.EndFrame.Hint")); //$NON-NLS-1$
						break;
					}
				}
				setMessage(msg);
			}
		}
		if (doSnap) {
			// move vector snap point if origin may have moved
			int n = getFrameNumber();
			getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));

		}
		if (Tracker.timeLogEnabled)
			Tracker.logTime("end TrackerPanel property change " + name); //$NON-NLS-1$
	}

	/**
	 * Overrides VideoPanel setImageBorder method to set the image border.
	 *
	 * @param borderFraction the border fraction
	 */
	@Override
	public void setImageBorder(double borderFraction) {
		super.setImageBorder(borderFraction);
		defaultImageBorder = getImageBorder();
	}

	/**
	 * Overrides VideoPanel getFilePath method.
	 *
	 * @return the relative path to the file
	 */
	@Override
	public String getFilePath() {
		if (defaultSavePath == null)
			return super.getFilePath();
		return defaultSavePath;
	}

	/**
	 * Overrides DrawingPanel scale method.
	 */
	@Override
	public void scale() {
		Rectangle mat = getMatBounds();
		if (mat != null) {
			xOffset = mat.x;
			yOffset = mat.y;
		}
		super.scale();
		// erase all tracks if pixel transform has changed
		if (!pixelTransform.equals(prevPixelTransform)) {
			if (prevPixelTransform == null)
				prevPixelTransform = new AffineTransform();
			getPixelTransform(prevPixelTransform);
			eraseAll();
		}
		// load track control if TFrame is known
		if (trackControl == null && getTFrame() != null)
			trackControl = TrackControl.getControl(this);
	}

	/**
	 * Overrides DrawingPanel setMouseCursor method. This blocks the crosshair
	 * cursor (from iad mouse controller) so that Tracker can set cursors for
	 * marking tracks.
	 *
	 * @param cursor the requested cursor
	 */
	@Override
	public void setMouseCursor(Cursor cursor) {
		if (PencilDrawer.isDrawing(this) && cursor == Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) {
			return;
		}
		if (cursor != Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) && !Tracker.isZoomInCursor(cursor)
				&& !Tracker.isZoomOutCursor(cursor)) {
			super.setMouseCursor(cursor);
		}
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	@Override
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		if (getTFrame() == null)
			return;
		// refresh views
		List<TView> views = frame.getTViews(panelID, TView.VIEW_UNSET, null);
		for (int i = views.size(); --i >= 0;) {
			views.get(i).refresh();
		}
		TMenuBar menubar = getMenuBar(false);
		FontSizer.setFonts(menubar, level);
		getTrackBar(true).setFontLevel(level);
		refreshTrackBar();
		ArrayList<TTrack> list = getTracksTemp();
		for (int it = 0, n = list.size(); it < n; it++) {
			list.get(it).setFontLevel(level);
		}
		TrackControl.getControl(this).refresh();
		if (modelBuilder != null) {
			modelBuilder.setFontLevel(level);
		}
		if (dataBuilder != null) {
			dataBuilder.setFontLevel(level);
		}
		if (autoTracker != null) {
			autoTracker.getWizard().setFontLevel(level);
		}
		if (attachmentDialog != null) {
			attachmentDialog.setFontLevel(level);
		}
		PencilDrawer drawer = PencilDrawer.getDrawer(this);
		if (drawer.drawingControl != null && drawer.drawingControl.isVisible()) {
			drawer.drawingControl.setFontLevel(level);
		}
		Video video = getVideo();
		if (video != null) {
			ArrayList<Filter> filters = video.getFilterStack().getFilters();
			for (int i = 0, n = filters.size(); i < n; i++) {
				Filter filter = filters.get(i);
				JDialog inspector = filter.getInspector();
				if (inspector != null) {
					FontSizer.setFonts(inspector, level);
					if (filter instanceof BaselineFilter) {
						// resize the thumbnail, if any
						BaselineFilter bf = (BaselineFilter)filter;
						bf.resizeThumbnail();
					}
					else
						inspector.pack();
				}
			}
		}
		if (algorithmDialog != null) {
			algorithmDialog.setFontLevel(level);
		}
	}

	/**
	 * Returns true if an event starts or ends a zoom operation. Used by
	 * OptionController. Overrides DrawingPanel method.
	 *
	 * @param e a mouse event
	 * @return true if a zoom event
	 */
	@Override
	public boolean isZoomEvent(MouseEvent e) {
		return super.isZoomEvent(e) || Tracker.isZoomInCursor(getCursor());
	}

	/**
	 * Overrides InteractivePanel getInteractive method. This checks the selected
	 * track (if any) first.
	 *
	 * @return the interactive drawable identified by the most recent mouse event
	 */
	@Override
	public Interactive getInteractive() {
		TTrack track = getSelectedTrack();
		boolean isMarking = (track != null && cursorType == track.getMarkingCursorType(mouseEvent));
		if (isMarking)
			return null;
		if (track != null) {
			Interactive o = null;
			// check selected track first unless it's a calibration tool
			if (track != getAxes() && !calibrationTools.contains(track)
					&& (o = track.findInteractive(this, mouseEvent.getX(), mouseEvent.getY())) != null) {
				return o;
			}
			if ((track.isDependent() || track == getAxes())
							&& (o = getAxes().findInteractive(this, mouseEvent.getX(), mouseEvent.getY())) != null) {
				return o;
			}
		}
		return super.getInteractive();
	}

	@Override
	public XYCoordinateStringBuilder getXYCoordinateStringBuilder(TPoint point) {
		return coordStringBuilder;
	}

	protected BufferedImage getMattedImage() {
		if (renderedImage == null || renderedImage.getWidth() != getWidth()
				|| renderedImage.getHeight() != getHeight()) {
			renderedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		}
		render(renderedImage);
		Rectangle2D rect = getMat().getDrawingBounds();
		int w = (int) rect.getWidth();
		int h = (int) rect.getHeight();
		if (mattedImage == null || mattedImage.getWidth() != w || mattedImage.getHeight() != h) {
			mattedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g = mattedImage.getGraphics();
		g.drawImage(renderedImage, (int) -rect.getX(), (int) -rect.getY(), null);
		return mattedImage;
	}

	/**
	 * Deletes a point.
	 *
	 * @param pt the point to delete
	 */
	protected void deletePoint(TPoint pt) {
		ArrayList<TTrack> list = getTracks();
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack track = list.get(it);
			Step step = track.getStep(pt, this);
			if (step != null) {
				step = track.deleteStep(step.n);
				if (step == null)
					return;
				setSelectedPoint(null);
				selectedSteps.clear();
				hideMouseBox();
				return;
			}
		}
	}

	/**
	 * Deletes the selected steps, if any.
	 */
	protected void deleteSelectedSteps() {
		ArrayList<Object[]> changes = new ArrayList<Object[]>();
		int nMin = Integer.MAX_VALUE, nMax = -1;
		ArrayList<TTrack> list = getTracks();
		for (int it = 0, ni = list.size(); it < ni; it++) {
			TTrack track = list.get(it);
			boolean isChanged = false;
			XMLControl control = new XMLControlElement(track);
			for (Step step : selectedSteps) {
				if (step.getTrack() == track) {
					if (track.isLocked()) {
						step.erase();
					} else {
						int n = step.getFrameNumber();
						track.steps.setStep(n, null);
						for (String columnName : track.textColumnNames) {
							String[] entries = track.textColumnEntries.get(columnName);
							if (entries.length > n) {
								entries[n] = null;
							}
						}
						AutoTracker autoTracker = getAutoTracker(false);
						if (autoTracker != null && autoTracker.getTrack() == track) {
							autoTracker.delete(n);
						}
						nMin = Math.min(nMin, n);
						nMax = Math.max(nMax, n);
						isChanged = true;
					}
				}
			}
			if (isChanged) {
				changes.add(new Object[] { track, control });
				if (track.ttype == TTrack.TYPE_POINTMASS) {
					VideoClip clip = getPlayer().getVideoClip();

					int startFrame = Math.max(nMin - 2 * clip.getStepSize(), clip.getStartFrameNumber());
					int stepCount = 4 + (nMax - nMin) / clip.getStepSize();
					((PointMass) track).updateDerivatives(startFrame, stepCount);

				}
				track.fireStepsChanged();
			}
		}
		selectedSteps.clear();
		if (!changes.isEmpty()) {
			Undo.postMultiTrackEdit(changes);
		}
	}

	/**
	 * Overrides VideoPanel scale method to handle zoom
	 *
	 * @param drawables the list of drawable objects
	 */
	@Override
	protected void scale(ArrayList<Drawable> drawables) {
		if (drawingInImageSpace) {
			if (getPreferredSize().width < 2) // zoomed to fit
				super.setImageBorder(defaultImageBorder);
			else {
				// set image border so video size remains fixed
				double w = getMagnification() * imageWidth;
				double wBorder = (getWidth() - w) * 0.5 / w;
				double h = getMagnification() * imageHeight;
				double hBorder = (getHeight() - h) * 0.5 / h;
				double border = Math.min(wBorder, hBorder);
				super.setImageBorder(Math.max(border, defaultImageBorder));
			}
		}
		super.scale(drawables);
	}

	/**
	 * Paints this component. Overrides DrawingPanel method to log times
	 * 
	 * @param g the graphics context
	 */
	@Override
	public void paintComponent(Graphics g) {
		if (!isPaintable()) {
			return;
		}
		boolean justScroll = (zoomCenter != null && isShowing() && getTFrame() != null
				&& (scrollPane.getVerticalScrollBar().isVisible() || scrollPane.getHorizontalScrollBar().isVisible()));
		// BH moved this up, because why paint if you are going to paint again?
		if (justScroll) {
			final Rectangle rect = scrollPane.getViewport().getViewRect();
			int x = zoomCenter.x - rect.width / 2;
			int y = zoomCenter.y - rect.height / 2;
			rect.setLocation(x, y);
			zoomCenter = null;
			scrollRectToVisible(rect);
			return;
		}

//		long t0 = Performance.now(0);

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.paintComp 0",
		// Performance.TIME_MARK));

		super.paintComponent(g);
		showFilterInspectors();
//		OSPLog.debug("!!! " + Performance.now(t0) + " TrackerPanel.paintComponent");
//		 OSPLog.debug(Performance.timeCheckStr("TrackerPanel.paintCOmp 1",
//		 Performance.TIME_MARK));
	}

	/**
	 * Gets the default image width for new empty panels
	 * 
	 * @return width
	 */
	protected static double getDefaultImageWidth() {
		return defaultWidth;
	}

	/**
	 * Gets the default image height for new empty panels
	 * 
	 * @return height
	 */
	protected static double getDefaultImageHeight() {
		return defaultHeight;
	}

	/**
	 * Gets the TFrame parent of this panel
	 * 
	 * @return the TFrame, if any
	 */
	public TFrame getTFrame() {
		if (frame == null) {
			Container c = getTopLevelAncestor();
			if (c instanceof TFrame) {
				frame = (TFrame) c;
			}
		}
		return frame;
	}

	/**
	 * Gets the autotracker for this panel
	 * 
	 * @return the autotracker, if any
	 */
	protected AutoTracker getAutoTracker(boolean forceNew) {
		if (autoTracker == null && forceNew) {
			autoTracker = new AutoTracker(this);

			Wizard wizard = autoTracker.getWizard();
			FontSizer.setFonts(wizard);
		}
		return autoTracker;
	}

	/**
	 * Gets the default format patterns for a specified track type
	 * 
	 * @param trackType the track type
	 * @return a map of variable name to pattern
	 */
	protected TreeMap<String, String> getFormatPatterns(int ttype) {
		TreeMap<String, String> patterns = formatPatterns[ttype];
		if (patterns == null) {
			patterns = new TreeMap<String, String>();
			formatPatterns[ttype] = patterns;
			// initialize with default patterns
			TreeMap<String, String> defaultPatterns = TTrack.getDefaultFormatPatterns(ttype);
			if (defaultPatterns != null) {
				patterns.putAll(defaultPatterns);
			}
			// initialize for additional trackType variables
			ArrayList<String> vars = TTrack.getAllVariables(ttype);
			for (int i = 0, n = vars.size(); i < n; i++) {
				String v = vars.get(i);
				if (!patterns.containsKey(v)) {
					patterns.put(v, ""); //$NON-NLS-1$
				}
			}
		}
		return patterns;
	}

	/**
	 * Sets the initial default format patterns for all track types and existing
	 * tracks
	 */
	protected void setInitialFormatPatterns() {
// BH There is no need to initialize all 11 base track types
//		String[] types = TTrack.getFormattableTrackTypes();
//		for (int i = 0, n = types.length; i < n; i++) {
//			getFormatPatterns(types[i]);
//		}
		ArrayList<TTrack> list = getTracksTemp();
		for (int it = 0, n = list.size(); it < n; it++) {
			list.get(it).setInitialFormatPatterns(this);
		}
		list.clear();
	}


	/**
	 * Disposes of this panel permanently. Only to be used upon tab removal, and only to be run once.
	 */
	@Override
	public void dispose() {
		if (isDisposed)
			return;
		isDisposed = true; // stop all firing of events
//		view = null;
		// remove property change listeners
		if (frame != null) {
			removePropertyChangeListener(VideoPanel.PROPERTY_VIDEOPANEL_DATAFILE, frame); // $NON-NLS-1$
			removePropertyChangeListener(PROPERTY_TRACKERPANEL_VIDEO, frame); // $NON-NLS-1$
		}
		if (coords != null)
			coords.removePropertyChangeListener(this);
		selectedPoint = null;
		selectedStep = null;
		selectedTrack = null;

		// inform non-modal dialogs so they close: AutoTracker, CMInspector,
		// DynamicSystemInspector,
		// AttachmentDialog, ExportZipDialog, PencilControl, TableTView, TrackControl,
		// VectorSumInspector

		// clean up mouse handler
		if (mouseHandler != null) {
			mouseHandler.selectedTrack = null;
			mouseHandler.selectedPoint = null;
			mouseHandler.iad = null;
		}
		// clear filter classes
		clearFilters();
		// remove transfer handler
		setTransferHandler(null);

		setScrollPane(null);

		// clear the drawables AFTER disposing of main view
		ArrayList<TTrack> tracks = getTracks();
		clear(false);
		for (TTrack track : tracks) {
			track.dispose();
		}

		// dispose of the track control, clip inspector and player bar
//		TrackControl.getControl(trackerPanel).dispose();
		VideoPlayer player = getPlayer();
		ClipInspector ci = (player == null ? null : player.getVideoClip().getClipInspector());
		if (ci != null) {
			ci.dispose();
		}

		if (video != null) {
			// WAS MEMORY LEAK
			video.dispose();
			video = null;
			// set the video to null
			//setVideo(null);
		}


		coordinateStrBuilder = null;
		if (selectedSteps != null)
			selectedSteps.dispose();
		selectedSteps = null;
		// long t0 = Performance.now(0);
		offscreenImage = null;
		workingImage = null;

		if (player != null) {
			VideoClip clip = player.getVideoClip();
			clip.removePropertyChangeListener(player);
			clip.removeListener(this);
			ClipControl clipControl = player.getClipControl();
			clipControl.removePropertyChangeListener(player);
			player.removeActionListener(this);
			player.removeFrameListener(this);
			player.stop();
			remove(player);
			player.dispose();
			player = null;

		}
		if (video != null) {
			video.removeListener(this);
		}
		for (TTrack track : TTrack.getValues()) {
			removePropertyChangeListener(track);
			track.removeListener(this);
		}
		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose removeListeners",
		// Performance.TIME_MARK));

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose stop player",
		// Performance.TIME_MARK));

		// dispose of autotracker, modelbuilder, databuilder, other dialogs
		if (autoTracker != null) {
			autoTracker.dispose();
			autoTracker = null;
		}
		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose autoTracker",
		// Performance.TIME_MARK));
		if (modelBuilder != null) {
			modelBuilder.removePropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_PANEL, this);
			modelBuilder.dispose();
			modelBuilder = null;
		}
		if (dataBuilder != null) {
			dataBuilder.removePropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_PANEL, this);
			dataBuilder.removePropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION, this);
			dataBuilder.removePropertyChangeListener(FunctionEditor.PROPERTY_FUNCTIONEDITOR_DESCRIPTION, this);
			dataBuilder.removePropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_VISIBLE, this);
			dataBuilder.dispose();
			dataBuilder = null;
		}
		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose builders",
		// Performance.TIME_MARK));
		if (attachmentDialog != null) {
			attachmentDialog.dispose();
			attachmentDialog = null;
		}
		if (algorithmDialog != null) {
			algorithmDialog.dispose(); // bh changed to dispose -- OK?
			algorithmDialog = null;
		}
		if (guestsDialog != null) {
			guestsDialog.dispose();
			guestsDialog = null;
		}
		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose dialogs",
		// Performance.TIME_MARK));

		PencilDrawer.dispose(this);
		if (TFrame.haveExportDialog && ExportDataDialog.dataExporter != null
				&& ExportDataDialog.dataExporter.panelID == panelID) {
			ExportDataDialog.dataExporter.clear();
		}
		if (TFrame.haveExportDialog && ExportVideoDialog.videoExporter != null
				&& ExportVideoDialog.videoExporter.panelID == panelID) {
			ExportVideoDialog.videoExporter.clear();
		}
		if (TFrame.haveExportDialog)
			ExportZipDialog.clear(this);

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose export dialogs",
		// Performance.TIME_MARK));

		if (numberFormatDialog != null) {
			numberFormatDialog.dispose();
			numberFormatDialog = null;
		}
		filterClasses.clear();
		selectingPanelID = null;
		frame = null;
		renderedImage = null;
		mattedImage = null;
		if (frame != null)
			frame.disposeOf(this);
		//menuBar.dispose(this);
		frame = null;

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose number, thumbnail
		// dialogs", Performance.TIME_MARK));

		removeAll();

		ArrayList<TTrack> list = getDrawables(TTrack.class);
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack track = list.get(it);
			track.dispose();
		}

 		super.dispose();
 		
 		System.gc();
 		System.gc();
 		System.gc();
 		System.gc();
 		

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose removeall",
		// Performance.TIME_MARK));

		// OSPLog.debug("!!! " + Performance.now(t0) + " TrackerPanel.dispose " + panelID);
	}

	/**
	 * Sets the name of a track. This checks the name against those of existing
	 * tracks and prompts the user for a new name if a duplicate is found. After
	 * three failed attempts, a unique name is formed by appending a number.
	 * 
	 * @param track    the track to name
	 * @param newName  the proposed name
	 * @param postEdit true to post an undoable edit
	 */
	protected void setTrackName(TTrack track, String newName, boolean postEdit) {
		ArrayList<Drawable> drawables = getDrawablesNoClone();
		for (int i = 0, n = drawables.size(); i < n; i++) {
			Drawable next = drawables.get(i);
			if (next == track)
				continue;
			if (next instanceof TTrack) {
				String nextName = ((TTrack) next).getName();
				if (newName.equals(nextName)) {
					Toolkit.getDefaultToolkit().beep();
					String s = "\"" + newName + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
					badNameLabel.setText(s + TrackerRes.getString("TTrack.Dialog.Name.BadName")); //$NON-NLS-1$
					TTrack.NameDialog nameDialog = track.getNameDialog();
					nameDialog.getContentPane().add(badNameLabel, BorderLayout.SOUTH);
					nameDialog.pack();
					nameDialog.setVisible(true);
					return;
				}
			}
		}
		XMLControl control = new XMLControlElement(new TrackProperties(track));
		track.setName(newName);
		if (postEdit)
			Undo.postTrackDisplayEdit(track, control);
		if (TTrack.nameDialog != null) {
			TTrack.nameDialog.setVisible(false);
			TTrack.nameDialog.getContentPane().remove(badNameLabel);
		}
		frame.refreshMenus(this, TMenuBar.REFRESH_TPANEL_SETTRACKNAME);
	}

	/**
	 * Shows the visible filter inspectors, if any.
	 */
	protected void showFilterInspectors() {
		// show filter inspectors
		if (visibleFilters != null && visibleFilters.size() > 0) {
			TFrame frame = getTFrame();
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			for (Filter filter : visibleFilters.keySet()) {
				Point p = visibleFilters.get(filter);
				JDialog inspector = filter.getInspector();
				// must display inspector first to have non-zero size
				inspector.setVisible(true);
				int x = Math.max(p.x + (frame == null ? 0 : frame.getLocation().x), 0);
				x = Math.min(x, dim.width - inspector.getWidth());
				int y = Math.max(p.y + (frame == null ? 0 : frame.getLocation().y), 0);
				y = Math.min(y, dim.height - inspector.getHeight());
				inspector.setLocation(x, y);
			}
			visibleFilters.clear();
			visibleFilters = null;
		}
	}

	/**
	 * This inner class extends IADMouseController to set the cursor and show
	 * selected point coordinates.
	 */
	private class TMouseController extends IADMouseController {
		/**
		 * Handle the mouse released event.
		 * 
		 * @param e the mouse event
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			super.mouseReleased(e); // hides blmessagebox
			if (getSelectedPoint() != null) {
				getSelectedPoint().showCoordinates(TrackerPanel.this);
			}
		}

		/**
		 * Handle the mouse entered event.
		 *
		 * @param e the mouse event
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			super.mouseEntered(e);
			if (PencilDrawer.isDrawing(TrackerPanel.this)) {
				setMouseCursor(PencilDrawer.getDrawer(TrackerPanel.this).getPencilCursor());
			} else
				setMouseCursor(Cursor.getDefaultCursor());
		}

		/**
		 * Handle the mouse exited event.
		 *
		 * @param e the mouse event
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			super.mouseExited(e);
			isShiftKeyDown = false;
			if (getSelectedPoint() == null) {
				setMessage(null, 0); // BL message box
			}
			setMouseCursor(Cursor.getDefaultCursor());
		}

		/**
		 * Handle the mouse entered event.
		 *
		 * @param e the mouse event
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			super.mouseMoved(e);
		}

	}

	/**
	 * Returns an XML.ObjectLoader to save and load object data.
	 *
	 * @return the XML.ObjectLoader
	 */
	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	/**
	 * A class to save and load object data.
	 */
	static class Loader extends VideoPanel.Loader implements XML.ObjectLoader {

		/**
		 * Returns an ObjectLoader to save and load data for this class.
		 *
		 * @return the object loader
		 */
		public static XML.ObjectLoader getLoader() {
			return new Loader();
		}

		private AsyncLoader asyncloader;

		/**
		 * Creates an object having no frame or video (yet)
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return new TrackerPanel(null, (Video)null);
		}

		/**
		 * Loads an object with data from an XMLControl.
		 *
		 * FinalizableLoader just extracts all necessary information from the control
		 * for AsyncVideoI
		 *
		 * * @param control the control
		 * 
		 * @param obj the object
		 * @return the loaded object
		 */
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			// load the video clip
			TrackerPanel trackerPanel = (TrackerPanel) obj;	
			asyncloader = (AsyncLoader) ((XMLControlElement) control).getData();
			asyncloader.setLoader(this);
			this.control = (XMLControlElement) control;
			switch (trackerPanel.progress) {
			case VideoIO.PROGRESS_LOAD_INIT:
				// immediately set the frame
				trackerPanel.frame = asyncloader.getFrame();
				trackerPanel.frame.holdPainting(true);
				// load the dividers
				trackerPanel.dividerLocs = (double[]) control.getObject("dividers"); //$NON-NLS-1$
				// load the track control location
				trackerPanel.trackControlX = control.getInt("track_control_x"); //$NON-NLS-1$
				trackerPanel.trackControlY = control.getInt("track_control_y"); //$NON-NLS-1$
				// load the info dialog location
				trackerPanel.infoX = control.getInt("info_x"); //$NON-NLS-1$
				trackerPanel.infoY = control.getInt("info_y"); //$NON-NLS-1$
				// load the image size
				if (control.getPropertyNamesRaw().contains("width")) { //$NON-NLS-1$
					trackerPanel.setImageWidth(control.getDouble("width")); //$NON-NLS-1$
				}
				if (control.getPropertyNamesRaw().contains("height")) { //$NON-NLS-1$
					trackerPanel.setImageHeight(control.getDouble("height")); //$NON-NLS-1$
				}
				// load the zoom center and magnification
				trackerPanel.setMagnification(control.getDouble("magnification")); //$NON-NLS-1$
				if (control.getPropertyNamesRaw().contains("center_x")) { //$NON-NLS-1$
					int x = control.getInt("center_x"); //$NON-NLS-1$
					int y = control.getInt("center_y"); //$NON-NLS-1$
					trackerPanel.zoomCenter = new Point(x, y);
				}
				// load and check if a newer Tracker version created this file
				String fileVersion = control.getString("semantic_version"); //$NON-NLS-1$
				// if ver is null then must be an older version
				if (fileVersion != null && !OSPRuntime.isJS) {
					int result = 0;
					try {
						result = Tracker.compareVersions(fileVersion, OSPRuntime.VERSION);
					} catch (Exception e) {
					}
					if (result > 0) { // file is newer version than Tracker
						JOptionPane.showMessageDialog(trackerPanel,
								TrackerRes.getString("TrackerPanel.Dialog.Version.Message1") //$NON-NLS-1$
										+ " " + fileVersion + " " //$NON-NLS-1$ //$NON-NLS-2$
										+ TrackerRes.getString("TrackerPanel.Dialog.Version.Message2") //$NON-NLS-1$
										+ "\n" + TrackerRes.getString("TrackerPanel.Dialog.Version.Message3") //$NON-NLS-1$ //$NON-NLS-2$
										+ " (" + OSPRuntime.VERSION + ")." //$NON-NLS-1$ //$NON-NLS-2$
										+ "\n\n" + TrackerRes.getString("TrackerPanel.Dialog.Version.Message4") //$NON-NLS-1$ //$NON-NLS-2$
										+ " https://" + Tracker.trackerWebsite + ".", //$NON-NLS-1$ //$NON-NLS-2$
								TrackerRes.getString("TrackerPanel.Dialog.Version.Title"), //$NON-NLS-1$
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
				trackerPanel.progress = TrackerIO.PROGRESS_PANEL_READY;
				break;
			case TrackerIO.PROGRESS_PANEL_READY:
				// load the description
				trackerPanel.hideDescriptionWhenLoaded = control.getBoolean("hide_description"); //$NON-NLS-1$
				String desc = control.getString("description"); //$NON-NLS-1$
				if (desc != null) {
					trackerPanel.setDescription(desc);
				}
				// load the metadata
				trackerPanel.author = control.getString("author"); //$NON-NLS-1$
				trackerPanel.contact = control.getString("contact"); //$NON-NLS-1$

				// load units and unit visibility
				if (control.getPropertyNamesRaw().contains("length_unit")) { //$NON-NLS-1$
					trackerPanel.lengthUnit = control.getString("length_unit"); //$NON-NLS-1$
				}
				if (control.getPropertyNamesRaw().contains("mass_unit")) { //$NON-NLS-1$
					trackerPanel.massUnit = control.getString("mass_unit"); //$NON-NLS-1$
				}
				if (control.getPropertyNamesRaw().contains("units_visible")) { //$NON-NLS-1$
					trackerPanel.unitsVisible = control.getBoolean("units_visible"); //$NON-NLS-1$
				}

				// load custom number formats
				String[][] patterns = (String[][]) control.getObject("number_formats"); //$NON-NLS-1$
				if (patterns != null) {
					for (int ip = 0; ip < patterns.length; ip++) {
						String[] next = patterns[ip];
						int ttype = TTrack.getBaseTypeInt(next[0]);
						if (ttype < 0)
							continue;
						TreeMap<String, String> patternMap = trackerPanel.getFormatPatterns(ttype);
						for (int i = 1; i < next.length;) {
							patternMap.put(next[i++], next[i++]);
						}
					}
				}
				// load the configuration
				Configuration config = (Configuration) control.getObject("configuration"); //$NON-NLS-1$
				if (config != null) {
					trackerPanel.enabled = config.enabled;
				}

				// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.finalizeLoading ref and
				// config ", Performance.TIME_MARK));

				// load the selected_& custom views properties
				List<XMLProperty> props = control.getPropsRaw();
				trackerPanel.selectedViewsProperty = null;
				
				trackerPanel.customViewsProperty = null;
				for (int n = 0, i = props.size(); --i >= 0 && n < 3;) { 
					// n < 3, not 4, since "selected_views" & "selected_view_types" should never BOTH exist
					XMLProperty prop = props.get(i);
					switch (prop.getPropertyName()) {
					case "selected_views": 
						trackerPanel.selectedViewsProperty = prop;
						n++;
						break;
					case "selected_view_types":
						trackerPanel.selectedViewTypesProperty = prop;
						n++;
						break;
					case "selected_track_views":
						trackerPanel.selectedTrackViewsProperty = prop;
						n++;
						break;
					case "views":
						trackerPanel.customViewsProperty = prop;
						n++;
						break;
					}
				}
				trackerPanel.progress = VideoIO.PROGRESS_VIDEO_LOADING;
				break;
			default:
				super.loadObject(control, obj);	// loads video
			}

			return trackerPanel;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void finalizeLoading() {
			// long t0 = Performance.now(0);
			// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.finalizeLoading1",
			// Performance.TIME_MARK));
			TrackerPanel trackerPanel = (TrackerPanel) videoPanel;
			if (trackerPanel.progress < VideoIO.PROGRESS_VIDEO_READY) {
				return;
			}
			videoPanel.setLoader(null);
			try {
				switch (trackerPanel.progress) {
				case VideoIO.PROGRESS_VIDEO_READY: // VideoPanel finished getting video clip
					XMLControl child;
					Video video = finalizeClip();
					if (video != null) {
						FilterStack stack = video.getFilterStack();
						ArrayList<Filter> filters = stack.getFilters();
						for (int i = 0, n = filters.size(); i < n; i++) {
							Filter filter = filters.get(i);
							filter.setVideoPanel(trackerPanel);
							if (filter.inspectorX != Integer.MIN_VALUE) {
								filter.inspectorVisible = true;
								if (trackerPanel.visibleFilters == null) {
									trackerPanel.visibleFilters = new HashMap<Filter, Point>();
								}
								Point p = new Point(filter.inspectorX, filter.inspectorY);
								trackerPanel.visibleFilters.put(filter, p);
							}
						}
					}
					// load the clip control
					child = control.getChildControl("clipcontrol"); //$NON-NLS-1$
					if (child != null) {
						child.loadObject(trackerPanel.getPlayer().getClipControl());
					}
					trackerPanel.progress = TrackerIO.PROGRESS_VIDEO_LOADED;
					break;
				case TrackerIO.PROGRESS_VIDEO_LOADED:
					// load the toolbar
					child = control.getChildControl("toolbar"); //$NON-NLS-1$
					if (child != null) {
						TToolBar toolbar = new TToolBar(trackerPanel);
						child.loadObject(toolbar);
						trackerPanel.frame.setToolBar(trackerPanel, toolbar);
					}
					// load the coords
					child = control.getChildControl("coords"); //$NON-NLS-1$
					if (child != null) {
						ImageCoordSystem coords = trackerPanel.getCoords();
						child.loadObject(coords);
						int n = trackerPanel.getFrameNumber();
						trackerPanel.getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));
					}
					trackerPanel.progress = TrackerIO.PROGRESS_TOOLBAR_AND_COORD_READY;
					break;
				case TrackerIO.PROGRESS_TOOLBAR_AND_COORD_READY:
					// load the tracks
					ArrayList<?> tracks = ArrayList.class.cast(control.getObject("tracks")); //$NON-NLS-1$
					if (tracks == null) {
						trackerPanel.progress = TrackerIO.PROGRESS_TRACKS_INITIALIZED;
						break;
					}
					for (int i = 0, n = tracks.size(); i < n; i++) {
						trackerPanel.addTrack((TTrack) tracks.get(i));
					}
					trackerPanel.progress = TrackerIO.PROGRESS_TRACKS_ADDED;
					break;
				case TrackerIO.PROGRESS_TRACKS_ADDED:
//					ArrayList<?> tracks2 = ArrayList.class.cast(control.getObject("tracks")); //$NON-NLS-1$
					ArrayList<TTrack> traks = trackerPanel.getTracks(); //$NON-NLS-1$
					// initialize tracks only after loading all of them
					// required by CenterOfMass, DyanamicSystem, and VectorSum
					for (int i = 0, n = traks.size(); i < n; i++) {
						traks.get(i).initialize(trackerPanel);
					}
					// load drawing scenes saved in vers 4.11.0+
					ArrayList<PencilScene> scenes = (ArrayList<PencilScene>) control.getObject("drawing_scenes"); //$NON-NLS-1$
					if (scenes != null) {
						PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
						drawer.setDrawingsVisible(control.getBoolean("drawings_visible"), false); //$NON-NLS-1$
						// replace previous scenes
						drawer.setScenes(scenes);
					}
					// load drawings saved with vers 4.10.0
					ArrayList<PencilDrawing> drawings = (ArrayList<PencilDrawing>) control.getObject("drawings"); //$NON-NLS-1$
					if (drawings != null) {
						PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
						drawer.setDrawingsVisible(control.getBoolean("drawings_visible"), false); //$NON-NLS-1$
						// clear previous scenes and add drawings to new one
						drawer.clearScenes(false);
						for (int i = 0, n = drawings.size(); i < n; i++) {
							drawer.addDrawingtoSelectedScene(drawings.get(i));
						}
					}
					trackerPanel.progress = TrackerIO.PROGRESS_TRACKS_INITIALIZED;
					break;
				case TrackerIO.PROGRESS_TRACKS_INITIALIZED:
					// load the reference frame
					String rfName = control.getString("referenceframe"); //$NON-NLS-1$
					if (rfName != null) {
						trackerPanel.setReferenceFrame(rfName);
					}
					// set selected track
					String name = control.getString(PROPERTY_TRACKERPANEL_SELECTEDTRACK); // $NON-NLS-1$
					trackerPanel.setSelectedTrack(name == null ? null : trackerPanel.getTrack(name));
					trackerPanel.progress = VideoIO.PROGRESS_COMPLETE;
					break;
				}
			} finally {
				// OSPLog.debug("!!! " + Performance.now(t0) + " TrackerPanel.finalizeLoading");
				// OSPLog.debug("TrackerPanel.finalizeLoading done");
			}
			System.out.println("TrackerPanel.loader progress " + trackerPanel.progress + " " + OSPRuntime.getMemoryStr());
			if (trackerPanel.progress == VideoIO.PROGRESS_COMPLETE) {
				if (asyncloader != null)
					asyncloader.finalized(trackerPanel);
				dispose();

			}
		}

		@Override
		public void dispose() {
			// clear the object
			asyncloader = null;
			super.dispose();

		}
		
// BH data tab loading disabled by Doug f30bd68 2020-07-07
//		protected void setDataTabs(TrackerPanel trackerPanel, ArrayList<DataToolTab> addedTabs) {
//			final DataToolTab tab = addedTabs.get(0);
//
//			// set the owner of the tab to the specified track
//			String trackname = tab.getOwnerName();
//			TTrack track = trackerPanel.getTrack(trackname);
//			if (track == null)
//				return;
//			Data data = track.getData(trackerPanel);
//			tab.setOwner(trackname, data);
//
//			// set up a DataRefreshTool and send it to the tab
//			final DataRefreshTool refresher = DataRefreshTool.getTool(data);
//			DatasetManager toSend = new DatasetManager();
//			toSend.setID(data.getID());
//			tab.send(new LocalJob(toSend), refresher);
//
//			// set the tab column IDs to the track data IDs and add track data to the
//			// refresher
//			SwingUtilities.invokeLater(new Runnable() {
//				@Override
//				public void run() {
//					ArrayList<TTrack> tracks = trackerPanel.getTracks();
//					for (TTrack tt : tracks) {
//						Data trackData = tt.getData(trackerPanel);
//						if (tab.setOwnedColumnIDs(tt.getName(), trackData)) {
//							// true if track owns one or more columns
//							refresher.addData(trackData);
//						}
//					}
//				}
//			});
//			// tab is now fully "wired" for refreshing by tracks
//		}

		/**
		 * Saves object data.
		 *
		 * @param control the control to save to
		 * @param obj     the TrackerPanel object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			// turn off XML writing of null final array elements
			boolean writeNullFinalArrayElements = XMLPropertyElement.defaultWriteNullFinalArrayElements;
			XMLPropertyElement.defaultWriteNullFinalArrayElements = false;

			TrackerPanel trackerPanel = (TrackerPanel) obj;
			// save the version
//      control.setValue("version", OSPRuntime.VERSION); //$NON-NLS-1$
			// changed to semantic version June 15 2017
			control.setValue("semantic_version", OSPRuntime.VERSION); //$NON-NLS-1$
			// save the image size
			control.setValue("width", trackerPanel.getImageWidth()); //$NON-NLS-1$
			control.setValue("height", trackerPanel.getImageHeight()); //$NON-NLS-1$
			// save the magnification
			double zoom = trackerPanel.getPreferredSize().width > 10 ? trackerPanel.getMagnification() : -1;
			control.setValue("magnification", zoom); //$NON-NLS-1$
			if (trackerPanel.getTFrame() != null) {
				MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
				Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
				control.setValue("center_x", (int) rect.getCenterX()); //$NON-NLS-1$
				control.setValue("center_y", (int) rect.getCenterY()); //$NON-NLS-1$
			}
			// save the description, if any
			if (trackerPanel.hideDescriptionWhenLoaded) {
				control.setValue("hide_description", true); //$NON-NLS-1$
			}
			if (!trackerPanel.description.trim().equals("")) { //$NON-NLS-1$
				control.setValue("description", trackerPanel.description); //$NON-NLS-1$
			}
			// save the metadata, if any
			if (trackerPanel.author != null) {
				control.setValue("author", trackerPanel.author); //$NON-NLS-1$
			}
			if (trackerPanel.contact != null) {
				control.setValue("contact", trackerPanel.contact); //$NON-NLS-1$
			}
			// save the video clip, clip control and coords
			control.setValue("videoclip", trackerPanel.getPlayer().getVideoClip()); //$NON-NLS-1$
			control.setValue("clipcontrol", trackerPanel.getPlayer().getClipControl()); //$NON-NLS-1$
			ImageCoordSystem coords = trackerPanel.getCoords();
			while (coords instanceof ReferenceFrame) {
				// save reference frame
				ReferenceFrame refFrame = (ReferenceFrame) coords;
				TTrack track = refFrame.getOriginTrack();
				control.setValue("referenceframe", track.getName()); //$NON-NLS-1$
				coords = refFrame.getCoords();
			}
			control.setValue("coords", coords); //$NON-NLS-1$
			// save custom number formats
			String[][] customPatterns = getSaveCustomFormatPatterns(trackerPanel);
			if (customPatterns.length > 0) {
				control.setValue("number_formats", customPatterns); //$NON-NLS-1$
			}
			// save units and unit visibility
			control.setValue("length_unit", trackerPanel.lengthUnit); //$NON-NLS-1$
			control.setValue("mass_unit", trackerPanel.massUnit); //$NON-NLS-1$
			control.setValue("units_visible", trackerPanel.unitsVisible); //$NON-NLS-1$

			// save the tracks
			control.setValue("tracks", trackerPanel.getTracksToSave()); //$NON-NLS-1$
			// save the selected track
			TTrack track = trackerPanel.getSelectedTrack();
			if (track != null) {
				control.setValue(PROPERTY_TRACKERPANEL_SELECTEDTRACK, track.getName()); // $NON-NLS-1$
			}
			// save the drawings and drawing visibility
			if (PencilDrawer.hasDrawings(trackerPanel)) {
				PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
				control.setValue("drawing_scenes", drawer.scenes); //$NON-NLS-1$
				control.setValue("drawings_visible", drawer.areDrawingsVisible()); //$NON-NLS-1$
			}

			// save custom configurations
			if (!trackerPanel.isDefaultConfiguration() && trackerPanel.isEnabled("config.saveWithData")) { //$NON-NLS-1$
				control.setValue("configuration", new Configuration(trackerPanel)); //$NON-NLS-1$
			}

			// save frame-related properties
			TFrame frame = trackerPanel.getTFrame();
			if (frame != null) {
				// save the split pane dividers
				double[] dividerLocations = new double[4];
				int w = 0;
				int[] order = TFrame.isPortraitLayout() ? TFrame.PORTRAIT_DIVIDER_ORDER : TFrame.DEFAULT_ORDER;
				for (int i = 0; i < dividerLocations.length; i++) {
					JSplitPane pane = frame.getSplitPane(trackerPanel, i);
					if (i == TFrame.SPLIT_MAIN_RIGHT)
						w = pane.getMaximumDividerLocation();
					int max = i == TFrame.SPLIT_WORLD_PAGE ? w : pane.getMaximumDividerLocation();
					double loc = Math.min(1.0, 1.0 * pane.getDividerLocation() / max);
					dividerLocations[order[i]] = frame.getConvertedDividerLoc(order[i], loc);
				}
				control.setValue("dividers", dividerLocations); //$NON-NLS-1$
				// save the custom views
				TView[][] customViews = frame.getTViews(trackerPanel, true);
				for (int i = 0; i < customViews.length; i++) {
					if (customViews[i] == null)
						continue;
					control.setValue("views", customViews); //$NON-NLS-1$
					break;
				}
				// save the selected view types
				int[] selectedViewTypes = frame.getSelectedViewTypes(trackerPanel);
				control.setValue("selected_view_types", selectedViewTypes); //$NON-NLS-1$
				// save the selected trackviews
				String selectedTrackViews = frame.getSelectedTrackViews(trackerPanel);
				control.setValue("selected_track_views", selectedTrackViews); //$NON-NLS-1$

				// save the toolbar for button states
				TToolBar toolbar = trackerPanel.getToolBar(true);
				control.setValue("toolbar", toolbar); //$NON-NLS-1$
				// save the visibility and location of the track control
				TrackControl tc = trackerPanel.trackControl;
				if (tc != null && tc.isVisible()) {
					int x = tc.getLocation().x - frame.getLocation().x;
					int y = tc.getLocation().y - frame.getLocation().y;
					control.setValue("track_control_x", x); //$NON-NLS-1$
					control.setValue("track_control_y", y); //$NON-NLS-1$
				}
				// save the location of the info dialog if visible
				if (frame.notesVisible()) {
					int x = frame.getNotesDialog().getLocation().x - frame.getLocation().x;
					int y = frame.getNotesDialog().getLocation().y - frame.getLocation().y;
					control.setValue("info_x", x); //$NON-NLS-1$
					control.setValue("info_y", y); //$NON-NLS-1$
				}
			}

			// save DataTool tabs
			if (DataTool.getTool(false) != null) {
				ArrayList<DataToolTab> tabs = new ArrayList<DataToolTab>();
				List<DataToolTab> tools = DataTool.getTool(true).getTabs();
				int n = tools.size();
				if (n > 0) {
					ArrayList<TTrack> tracks = trackerPanel.getTracks();
					for (int i = 0; i < n; i++) {
						DataToolTab tab = tools.get(i);
						for (TTrack next : tracks) {
							DatasetManager data = next.getData(trackerPanel);
							if (tab.isOwnedBy(data)) {
								// prepare tab for saving by setting owner and saving owned column names
								tab.setOwner(next.getName(), data);
								for (TTrack tt : tracks) {
									tab.saveOwnedColumnNames(tt.getName(), tt.getData(trackerPanel));
								}
								tabs.add(tab);
							}
						}
					}
					if (!tabs.isEmpty()) {
						DataToolTab[] tabArray = tabs.toArray(new DataToolTab[tabs.size()]);
						control.setValue("datatool_tabs", tabArray); //$NON-NLS-1$
					}
				}
			}
			// restore XML writing of null final array elements
			XMLPropertyElement.defaultWriteNullFinalArrayElements = writeNullFinalArrayElements;
		}
		
		/**
		 * Gets the custom format patterns for a specified TrackerPanel for
		 * TrackerPanel.Loader.saveObject
		 *
		 * @return array of arrays with variable names and custom patterns
		 */
		private static String[][] getSaveCustomFormatPatterns(TrackerPanel panel) {
			String path = PointMass.class.getName();
			path = path.substring(0, path.lastIndexOf(".") + 1);
			ArrayList<String[]> formats = new ArrayList<String[]>();
			// look at all track types defined in defaultFormatPatterns
			TreeMap<String, String>[] dpatterns = TTrack.getDefaultFormatPatterns();
			for (int ttype = 0, n = dpatterns.length; ttype < n; ttype++) {
				TreeMap<String, String> defaultPatterns = dpatterns[ttype];
				if (defaultPatterns == null)
					continue;
				TreeMap<String, String> patterns = panel.getFormatPatterns(ttype);
				ArrayList<String> customPatterns = new ArrayList<String>();
				String type = TTrack.getBaseTrackName(ttype);
				for (String name : defaultPatterns.keySet()) {
					String defaultPattern = defaultPatterns.get(name);
					String pattern = patterns.get(name);
					if (!defaultPattern.equals(pattern)) {
						if (customPatterns.isEmpty()) {
							customPatterns.add(path + type);
						}
						customPatterns.add(name);
						customPatterns.add(pattern == null ? "" : pattern); //$NON-NLS-1$
					}
				}
				for (String name : patterns.keySet()) {
					String defaultPattern = defaultPatterns.get(name);
					if (defaultPattern == null) {
						defaultPattern = ""; //$NON-NLS-1$
					}
					String pattern = patterns.get(name);
					if (!pattern.equals(defaultPattern) && !customPatterns.contains(name)) {
						if (customPatterns.isEmpty()) {
							customPatterns.add(path + type);
						}
						customPatterns.add(name);
						customPatterns.add(pattern);
					}
				}
				if (!customPatterns.isEmpty()) {
					formats.add(customPatterns.toArray(new String[customPatterns.size()]));
				}
			}
			return formats.toArray(new String[formats.size()][]);
		}




	}

	public boolean isAutoRefresh() {
		return isAutoRefresh && Tracker.allowDataRefresh;
	}

	public void setAutoRefresh(boolean b) {
		if (Tracker.allowDataRefresh)
			isAutoRefresh = b;
	}

	public JPopupMenu updateMainPopup() {
		JPopupMenu popup = getPopup();
		try {
			// see if a track has been clicked
			Interactive iad = getInteractive();
			// first look at TPoints
			if (iad instanceof TPoint) {
				TPoint p = (TPoint) iad;
				TTrack track = null;
				Step step = null;
				for (TTrack t: getTracksTemp()) {
					step = t.getStep(p, this);
					if (step != null) {
						track = t;
						break;
					}
				}
				clearTemp();
				if (step != null) { // found clicked track
					Step prev = selectedStep;
					selectedStep = step;
					if (track instanceof ParticleDataTrack) {
						popup = ((ParticleDataTrack) track).getPointMenu(this).getPopupMenu();
					} else if (track != null) {
						popup = track.getMenu(this, new JMenu()).getPopupMenu();
					}
					selectedStep = prev;
					getZoomBox().setVisible(false);
					return popup;
				}
			} else if (iad instanceof TTrack) {
				// look for direct track clicks
				TTrack track = (TTrack) iad;
				switch (track.ttype) {
				case TTrack.TYPE_TAPEMEASURE:
					popup = ((TapeMeasure) track).getInputFieldPopup();
					break;
				case TTrack.TYPE_PROTRACTOR:
					popup = ((Protractor) track).getInputFieldPopup();
					break;
			    default:
					popup = track.getMenu(this, null).getPopupMenu();
			    	break;
				}
				getZoomBox().setVisible(false);
				return popup;
			}
			// video or non-track TPoint was clicked
			popup.removeAll();
			// add zoom menus
			JMenuItem item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ZoomIn")); //$NON-NLS-1$
			popup.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getTFrame().getMainView(TrackerPanel.this).zoomIn(false);
				}
			});
			item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ZoomOut")); //$NON-NLS-1$
			popup.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getTFrame().getMainView(TrackerPanel.this).zoomOut(false);
				}
			});
			item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ZoomToFit")); //$NON-NLS-1$
			popup.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setMagnification(-1);
//					getToolBar(true).refreshZoomButton();
				}
			});

			// selection items
			DrawingPanel.ZoomBox zoomBox = getZoomBox();
			if (zoomBox.isDragged() && isStepsInZoomBox()) {
				popup.addSeparator();
				item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.Select")); //$NON-NLS-1$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						handleStepsInZoomBox(true);
					}
				});
				popup.add(item);
				item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.Deselect")); //$NON-NLS-1$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						handleStepsInZoomBox(false);
					}
				});
				popup.add(item);
			}

			// clip setting item
			if (isEnabled("button.clipSettings")) {//$NON-NLS-1$
				if (popup.getComponentCount() > 0)
					popup.addSeparator();
				item = new JMenuItem(MediaRes.getString("ClipInspector.Title") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						VideoClip clip = getPlayer().getVideoClip();
						ClipControl clipControl = getPlayer().getClipControl();
						TFrame frame = getTFrame();
						ClipInspector inspector = clip.getClipInspector(clipControl, frame);
						if (inspector.isVisible()) {
							return;
						}
						FontSizer.setFonts(inspector, FontSizer.getLevel());
						inspector.pack();
						Point p0 = new JFrame().getLocation();
						Point loc = inspector.getLocation();
						if ((loc.x == p0.x) && (loc.y == p0.y)) {
							// center inspector on the main view
							Rectangle rect = getVisibleRect();
							Point p = frame.getMainView(TrackerPanel.this).scrollPane.getLocationOnScreen();
							int x = p.x + (rect.width - inspector.getBounds().width) / 2;
							int y = p.y + (rect.height - inspector.getBounds().height) / 2;
							inspector.setLocation(x, y);
						}
						inspector.initialize();
						inspector.setVisible(true);
						getTFrame().getMainView(TrackerPanel.this).refresh();
					}
				});
				popup.add(item);
			}
			if (isEnabled("edit.copyImage")) { //$NON-NLS-1$
				popup.addSeparator();
				// copy image item
				Action copyImageAction = new AbstractAction(TrackerRes.getString("TMenuBar.Menu.CopyImage")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						BufferedImage image = new TrackerIO.ComponentImage(TrackerPanel.this).getImage();
						DrawingPanel.ZoomBox zoomBox = getZoomBox();
						if (zoomBox.isDragged()) {
							Rectangle zRect = zoomBox.reportZoom();
							BufferedImage image2 = new BufferedImage(zRect.width, zRect.height, image.getType());
							Graphics2D g = image2.createGraphics();
							g.drawImage(image, -zRect.x, -zRect.y, null);
							TrackerIO.copyImage(image2);
						} else
							TrackerIO.copyImage(image);
					}
				};
				JMenuItem copyImageItem = new JMenuItem(copyImageAction);
				popup.add(copyImageItem);
				// snapshot item
				Action snapshotAction = new AbstractAction(DisplayRes.getString("DisplayPanel.Snapshot_menu_item")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						snapshot();
					}
				};
				JMenuItem snapshotItem = new JMenuItem(snapshotAction);
				popup.add(snapshotItem);
			}

			TMenuBar.refreshPopup(this, TMenuBar.POPUPMENU_MAINTVIEW_POPUP, popup);
			// video properties item
			JMenuItem propertiesItem = new JMenuItem(actions.get("aboutVideo"));
			popup.addSeparator();
			propertiesItem.setText(TrackerRes.getString("TActions.AboutVideo")); //$NON-NLS-1$
			popup.add(propertiesItem);

			// print menu item
			if (isEnabled("file.print")) { //$NON-NLS-1$
				if (popup.getComponentCount() > 0)
					popup.addSeparator();
				popup.add(actions.get("print"));
			}
			// add help item
			if (popup.getComponentCount() > 0)
				popup.addSeparator();
			JMenuItem helpItem = new JMenuItem(TrackerRes.getString("Tracker.Popup.MenuItem.Help")); //$NON-NLS-1$
			helpItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TFrame frame = getTFrame();
					if (frame != null) {
						frame.showHelp("GUI", 0); //$NON-NLS-1$
					}
				}
			});
			popup.add(helpItem);
			return popup;
		} finally {
			FontSizer.setFonts(popup, FontSizer.getLevel());
		}
	}

	private void handleStepsInZoomBox(boolean add) {
		// determine what steps are in selection (zoom) box
		DrawingPanel.ZoomBox zoomBox = getZoomBox();
		Rectangle zRect = zoomBox.reportZoom();
		ArrayList<TTrack> tracks = getTracks();
		HashSet<TTrack> changedTracks = new HashSet<TTrack>();
		for (TTrack track : tracks) {
			// search only visible PointMass tracks for now
			if (!track.isVisible() || track.getClass() != PointMass.class)
				continue;
			if (!((PointMass) track).isPositionVisible())
				continue;
			for (Step step : track.getSteps()) {
				if (step == null || !track.isStepVisible(step, this))
					continue;
				// need look only at points[0] for PositionStep
				TPoint p = step.getPoints()[0];
				if (p == null || Double.isNaN(p.getX()))
					continue;
				if (zRect.contains(p.getScreenPosition(this))) {
					changedTracks.add(track);
					if (add) {
						selectedSteps.add(step);
					} else {
						selectedSteps.remove(step);
					}
					step.erase();
				}
			}
		}
		if (add && selectedSteps.size() == 1) {
			Step step = selectedSteps.toArray(new Step[1])[0];
			setSelectedPoint(step.points[0]);
		} else if (selectedSteps.size() > 1) {
			setSelectedPoint(null);
		}
		for (TTrack track : changedTracks) {
			track.fireStepsChanged();
		}
	}

	protected boolean isStepsInZoomBox() {
		// look for a step in the zoom box
		DrawingPanel.ZoomBox zoomBox = getZoomBox();
		Rectangle zRect = zoomBox.reportZoom();
		ArrayList<TTrack> tracks = getTracks();
		for (TTrack track : tracks) {
			// search only visible PointMass tracks for now
			if (!track.isVisible() || track.getClass() != PointMass.class)
				continue;
			if (!((PointMass) track).isPositionVisible())
				continue;
			for (Step step : track.getSteps()) {
				if (step == null)
					continue;
				// need look only at points[0] for PositionStep
				TPoint p = step.getPoints()[0];
				if (p == null || Double.isNaN(p.getX()))
					continue;
				if (zRect.contains(p.getScreenPosition(this))) {
					return true;
				}
			}
		}
		return false;
	}

	public void setVideoVisible(boolean visible) {
		if (video == null || visible == video.isVisible())
			return;
		video.setVisible(visible);
		getPlayer().getClipControl().videoVisible = visible;
		setVideo(video); // triggers image change event
	}

	public boolean isPaintable() {
		if (getTopLevelAncestor() == null 
				|| !isVisible() 
				|| getHeight() <= 0 
				|| getIgnoreRepaint() 
				|| getTFrame() == null 
				|| !frame.isPaintable() // BH generally the problem
				|| isClipAdjusting()) {
			return false;
		}
		return true;
	}

	@Override
	public void repaint() {
		if (!isPaintable())
			return;
		super.repaint();
	}

//	private static int repaintCount = 0;

	/**
	 * All repaints funnel through this method
	 * 
	 */
	@Override
	public void repaint(long time, int x, int y, int w, int h) {
		if (!isPaintable())
			return;
		// BH note that this check can prevent 85 repaint requests when
		// car.trz is loaded!

//		String s = /** @j2sNative  Clazz._getStackTrace() || */null;

//		OSPLog.debug("TrackerPanel repaint " + (++repaintCount));

		super.repaint(time, x, y, w, h);
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
	}

	public void notifyLoadingComplete() {
		setIgnoreRepaint(false);
		firePropertyChange(PROPERTY_TRACKERPANEL_LOADED, null, null);
	}

	public void taintEnabled() {
		enabledCount++;
	}

	public int getEnabledCount() {
		return enabledCount;
	}

	public void clearTainted() {
		tainted = false;
		dirty = null;
	}

	public void processPaste(String dataString) throws Exception {
		DataTrack dt = ParticleDataTrack.getTrackForDataString(dataString, this);
		// if track exists with the same data string, return
		if (dt != null) {
			// clipboard data has already been pasted
			return;
		}
		// parse the data and find data track
		DatasetManager[] datasetManager = DataTool.parseData(dataString, null);
		if (datasetManager != null) {
			String dataName = datasetManager[0].getName().replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$ ;
			boolean foundMatch = false;
			ArrayList<DataTrack> dataTracks = this.getDrawablesTemp(DataTrack.class);
			for (DataTrack next : dataTracks) {
				if (!(next instanceof ParticleDataTrack))
					continue;
				ParticleDataTrack track = (ParticleDataTrack) next;
				String trackName = track.getName("model"); //$NON-NLS-1$
				if (trackName.equals(dataName) || ("".equals(dataName) && //$NON-NLS-1$
						trackName.equals(TrackerRes.getString("ParticleDataTrack.New.Name")))) { //$NON-NLS-1$
					// found the data track
					foundMatch = true;
					if (track.isAutoPasteEnabled()) {
						// set new data immediately
						track.setData(datasetManager[0]);
						track.prevDataString = dataString;
					}
					break;
				}
			}
			dataTracks.clear();
			// if no matching track was found then create new track
			if (!foundMatch && frame.getAlwaysListenToClipboard()) {
				dt = importDatasetManager(datasetManager[0], null);
				if (dt != null && dt instanceof ParticleDataTrack) {
					ParticleDataTrack track = (ParticleDataTrack) dt;
					track.prevDataString = dataString;
				}
			}
		}
	}

	public void initialize(FileDropHandler fileDropHandler) {
		if (fileDropHandler == null) {
			// phase II, after setting of dividers
			// set track control location
			if (trackControlX != Integer.MIN_VALUE) {				
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				TrackControl tc = TrackControl.getControl(this);
				int x = Math.max(getTFrame().getLocation().x + trackControlX, 0);
				x = Math.min(x, dim.width - tc.getWidth());
				int y = Math.max(getTFrame().getLocation().y + trackControlY, 0);
				y = Math.min(y, dim.height - tc.getHeight());
				tc.setLocation(x, y);
				tc.positioned = true;
			}
			setInteractiveMouseHandler(mouseHandler = new TMouseHandler());
			// show filter inspectors
			showFilterInspectors();
			// set initial format patterns for existing tracks
			setInitialFormatPatterns();
			return;
		}
		// phase I
		// add a background mat if none exists
		setTransferHandler(fileDropHandler);
		if (getMat() == null) {
			addDrawable(new TMat(this)); // constructor adds mat to panel
		}
		// add coordinate axes if none exists
		if (getAxes() == null) {
			CoordAxes axes = new CoordAxes();
			axes.setVisible(false);
			addTrack(axes);
		}
		// add video filters to the tracker panel
		addFilter(DeinterlaceFilter.class);
		addFilter(GhostFilter.class);
		addFilter(StrobeFilter.class);
		addFilter(DarkGhostFilter.class);
		addFilter(NegativeFilter.class);
		addFilter(GrayScaleFilter.class);
		addFilter(LogFilter.class);
		addFilter(BrightnessFilter.class);
		addFilter(BaselineFilter.class);
		addFilter(SumFilter.class);
		addFilter(ResizeFilter.class);
		addFilter(RotateFilter.class);
		addFilter(PerspectiveFilter.class);
		addFilter(RadialDistortionFilter.class);
	}

	private ArrayList<Drawable> tempA;
	
	@SuppressWarnings("unchecked")
	public <T extends Drawable> ArrayList<T> getDrawablesTemp(Class<T> type) {
		if (tempA == null)
			tempA = new ArrayList<>();
		if (!tempA.isEmpty()) {
			tempA.clear();
		}
		return (type == null ? null : getDrawables(type, true, null, (ArrayList<T>) tempA));
	}

	@SuppressWarnings("unchecked")
	public <T extends TTrack> T getTrackByName(Class<T> type, String name) {
		synchronized (drawableList) {
			for (int i = 0, n = drawableList.size(); i < n; i++) {
				Drawable d = drawableList.get(i);
				if (type.isInstance(d) && name.equals(((TTrack)d).getName())) {
					return (T) d;
				}
			}
			return null;
		}
	}

	public void clearTemp() {
		if (tempA != null)
			tempA.clear();
	}


	@Override
	public void addNotify() {
		super.addNotify();
		if (OSPRuntime.isJS) {
			OSPRuntime.setJSClipboardPasteListener(this, frame.getDataDropHandler());
		}
	}
	
	@Override
	public void paint(Graphics g) {
		if (unTracked())
			return;
		getMat(); // ensures checked
		super.paint(g);
	}

	public void doPaste(String data) {
		if (data != null && !pasteXML(data))
			importDataAsync(data, null, null);
	}

	public void cloneNamed(String name) {
		TTrack track = getTrack(name);
		if (track == null)
			return;
		// add digit to end of name
		int n = 1;
		try {
			String number = name.substring(name.length() - 1);
			n = Integer.parseInt(number) + 1;
			name = name.substring(0, name.length() - 1);
		} catch (Exception ex) {
		}
		// increment digit if necessary
		Set<String> names = new HashSet<String>();
		for (TTrack next : getTracksTemp()) {
			names.add(next.getName());
		}
		clearTemp();
		try {
			while (names.contains(name + n)) {
				n++;
			}
		} catch (Exception ex) {
		}
		// create XMLControl of track, assign new name, and copy to clipboard
		XMLControl control = new XMLControlElement(track);
		control.setValue("name", name + n); //$NON-NLS-1$
		// now paste
		pasteXML(control.toXML());
	}

	/**
	 * Try to read data as XML, returning true if successful.
	 * 
	 * @param data
	 * @return true if successfully read as XML.
	 */
	private boolean pasteXML(String data) {
		try {
			XMLControl control = new XMLControlElement();
			control.readXML(data);
			Class<?> type = control.getObjectClass();
			if (type == null || control.failedToRead()) {
				return false;
			}
			if (TrackerPanel.class.isAssignableFrom(type)) {
				control.loadObject(this);
				return true;
			}
			if (ImageCoordSystem.class.isAssignableFrom(type)) {
				XMLControl state = new XMLControlElement(getCoords());
				control.loadObject(getCoords());
				Undo.postCoordsEdit(this, state);
				return true;
			}
			Object o = control.loadObject(null);
			if (o instanceof TTrack) {
				TTrack track = (TTrack) o;
				addTrack(track);
				setSelectedTrack(track);
				return true;
			}
			if (o instanceof VideoClip) {
				VideoClip clip = (VideoClip) o;
				VideoClip prev = getPlayer().getVideoClip();
				XMLControl state = new XMLControlElement(prev);
				// make new XMLControl with no stored object
				state = new XMLControlElement(state.toXML());
				getPlayer().setVideoClip(clip);
				Undo.postVideoReplace(this, state);
				return true;
			}
		} catch (Exception ex) {
		}
		return false;
	}

	/**
	 * Check for locked tracks and get list of xml strings for undoableEdit.
	 * 
	 * From clearTracks action.
	 */
	public void checkAndClearTracks() {
		ArrayList<String> xml = new ArrayList<String>();
		boolean locked = false;
		ArrayList<org.opensourcephysics.display.Drawable> keepers = getSystemDrawables();
		for (TTrack track : getTracksTemp()) {
			if (keepers.contains(track))
				continue;
			xml.add(new XMLControlElement(track).toXML());
			locked = locked || (track.isLocked() && !track.isDependent());
		}
		clearTemp();
		if (locked) {
			int i = JOptionPane.showConfirmDialog(this,
					TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Message"), //$NON-NLS-1$
					TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Title"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (i != 0)
				return;
		}
		
		// post edit and clear tracks
		Undo.postTrackClear(this, xml);
		clearTracks();
	}

	public void openURLFromDialog() {
		String input = GUIUtils.showInputDialog(getTFrame(),
				TrackerRes.getString("TActions.Dialog.OpenURL.Message") //$NON-NLS-1$
						+ ":                             ", //$NON-NLS-1$
				TrackerRes.getString("TActions.Dialog.OpenURL.Title"), //$NON-NLS-1$
				JOptionPane.PLAIN_MESSAGE, null);
		if (input == null || input.trim().equals("")) { //$NON-NLS-1$
			return;
		}
		Resource res = ResourceLoader.getResource(input.toString().trim());
		if (res == null || res.getURL() == null) {
			JOptionPane.showMessageDialog(getTFrame(),
					TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Message") //$NON-NLS-1$
							+ "\n\"" + input.toString().trim() + "\"", //$NON-NLS-1$ //$NON-NLS-2$
					TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Title"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		setSelectedPoint(null);
		selectedSteps.clear();
		setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		TFrame frame = getTFrame();
		if (frame != null) {
			// remove the initial empty tab if any
			frame.removeEmptyTabIfTabCountGreaterThan(0);
			frame.doOpenURL(res.getURL().toExternalForm());
		}
	}

	public void toggleAxesVisible() {
		CoordAxes axes = getAxes();
		if (axes == null)
			return;
		boolean visible = !axes.isVisible();
		axes.setVisible(visible);
		setSelectedPoint(null);
		selectedSteps.clear();
		hideMouseBox();
		if (visible) {
			if (getSelectedTrack() == null)
				setSelectedTrack(axes);
		} else {
			if (getSelectedTrack() == axes)
				setSelectedTrack(null);		
		}
	}

	public void addVideoFilter(String type) {
		Video video = getVideo();
		if (video == null)
			return;
		setVideoVisible(true);
		FilterStack filterStack = video.getFilterStack();
		Filter filter = null;
		Map<String, Class<? extends Filter>> filterClasses = getFilters();
		Class<? extends Filter> filterClass = filterClasses.get(type);
		if (filterClass != null) {
			try {
				filter = filterClass.newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (filter != null) {
				filterStack.addFilter(filter);
				filter.setVideoPanel(this);
				JDialog inspector = filter.getInspector();
				if (inspector != null) {
					inspector.setVisible(true);
				}
			}
			TFrame.repaintT(this);
		}
	}

	/**
	 * @j2sIgnore
	 */
	@Override
	protected void offerReloadVM(String ext, String message) {
		// used to be in VideoIO, so messages are of that type here.
		for (int i = 0; i < TrackerIO.XUGGLE_VIDEO_EXTENSIONS.length; i++) {
			if (TrackerIO.XUGGLE_VIDEO_EXTENSIONS[i].equals(ext)) {
				message += "<br><br>" + MediaRes.getString("VideoIO.Dialog.WrongVM.Message.Fix1");
				message += "<br>" + MediaRes.getString("VideoIO.Dialog.WrongVM.Message.Fix2");
				message += "<br><br>" + MediaRes.getString("VideoIO.Dialog.WrongVM.Message.Restart");
				if (JOptionPane.showConfirmDialog(null, new VideoIO.EditorPaneMessage(message),
						MediaRes.getString("VideoIO.Dialog.UnsupportedVideo.Title"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					// relaunch in 64-bit VM using Tracker PrefsDialog by reflection
					SwingUtilities.invokeLater(() -> {
						try {
							frame.getPrefsDialog().relaunch64Bit();
						} catch (Exception e) {
						}
					});
				}
			}
		}
	}

	/**
	 * Refresh the trackbar; in some cases, this may occur after disposal (from TFrame timer hack)
	 */
	void refreshTrackBar() {
		if (frame != null) {
			TTrackBar tbar = getTrackBar(false);
			if (tbar != null)
				tbar.refresh();
		}
	}

	public void onLoaded() {
		if (showTrackControlDelayed && isShowing()) {
			showTrackControlDelayed = false;
			TrackControl.getControl(this).setVisible(true);
		}
		TToolBar tbar = getToolBar(true);
		if (tbar != null) {
			final JButton button = tbar.notesButton;
			TTrack track = getSelectedTrack();
			if (!hideDescriptionWhenLoaded
					&& (track == null ? getDescription() != null && getDescription().trim().length() != 0
							: track.getDescription() != null && track.getDescription().trim().length() > 0)) {
				getToolBar(true).doNotesAction();
			} else if (button.isSelected())
				button.setSelected(false);//doClick();
		}
	}
	
	public String getTabName() {
		return (frame == null ? "<removed>" : getTFrame().getTabTitle(getTFrame().getTab(panelID)));
	}

	public void onBlocked() {
		if (trackControl != null) {
			boolean vis = trackControl.wasVisible;
			trackControl.setVisible(false);
			trackControl.wasVisible = vis;
		}
		if (modelBuilder != null) {
			TrackerPanel tp = frame.getTrackerPanelForID(panelID);
			boolean vis = tp.isModelBuilderVisible;
			modelBuilder.setVisible(false);
			tp.isModelBuilderVisible = vis;
		}
	}

	public void addListeners(String[] names, PropertyChangeListener listener) {
		for (int i = names.length; --i >= 0;)
			addPropertyChangeListener(names[i], listener);
	}

	public void removeListeners(String[] names, PropertyChangeListener listener) {
		for (int i = names.length; --i >= 0;)
			removePropertyChangeListener(names[i], listener);
	}

	public void refreshNotesDialog() {
		if (frame != null)
			frame.updateNotesDialog(this);
	}

	
	
	public static void main(String[] args) {

		TrackerPanel p = new TrackerPanel(null, (Video)null);

		try {

			Thread.sleep(100);
			p.dispose();

			p = null;
			
			Thread.sleep(1000);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	/**
	 * Identifying only for debugging purposes. Currently just TTrack.
	 * @param o
	 * @return
	 */
	public TrackerPanel ref(Object o) {
		return this;
	}


	/**
	 * Return the actual panel, which in the case of WorldTView is not this, rather the TrackerPanel it was initialized with.
	 * 
	 * @return
	 */
	public TrackerPanel getMainPanel() {
		return this;
	}

	public void refreshMenus(String why) {
		frame.refreshMenus(this, why);
	}

	protected boolean unTracked() {
		return hasTrackBar() && getTrackBar(true).getComponentCount() == 0;
	}

	boolean hasToolBar() {
		return (frame.getToolBar(panelID, false) != null);
	}

	boolean hasMenuBar() {
		return (frame.getMenuBar(panelID, false) != null);
	}

	boolean hasTrackBar() {
		return (frame.getTrackBar(panelID, false) != null);
	}

	public TMenuBar getMenuBar(boolean forceNew) {
		return frame.getMenuBar(panelID, forceNew);
	}

	public TToolBar getToolBar(boolean forceNew) {
		return frame.getToolBar(panelID, forceNew);
	}

	public TTrackBar getTrackBar(boolean forceNew) {
		return frame.getTrackBar(panelID, forceNew);
	}

	@Override
	public String toString() {
		return "[" + this.getClass().getSimpleName() + " " + panelID + " " + title + "]";
	}

	@Override
	public void finalize() {
		System.out.println("-------HOORAY!!!!!!!----finalized!------------ " + this);
		OSPLog.finalized(this);
	}

	public Rectangle getMatBounds() {
		TMat mat = getMat();
		return (mat == null ? null : mat.getBounds());
	}

}
