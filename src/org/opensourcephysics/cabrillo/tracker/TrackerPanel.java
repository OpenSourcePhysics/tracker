/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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
import java.awt.Color;
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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
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
import org.opensourcephysics.tools.DataRefreshTool;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.DataToolTab;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionPanel;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.ParamEditor;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.VideoCaptureTool;

import javajs.async.AsyncDialog;
import javajs.async.SwingJSUtils.Performance;

/**
 * This extends VideoPanel to manage and draw TTracks. It is Tracker's main view
 * and repository of a video and its associated tracks.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TrackerPanel extends VideoPanel implements Scrollable {

	public static final String PROPERTY_TRACKERPANEL_CLEAR = "clear";
	public static final String PROPERTY_TRACKERPANEL_COORDS = "coords";
	public static final String PROPERTY_TRACKERPANEL_FUNCTION = "function";
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
	public static final double MAX_ZOOM = 12;
	/** The zoom step size */
	public static final double ZOOM_STEP = Math.pow(2, 1.0 / 6);
	/** The fixed zoom levels */
	public static final double[] ZOOM_LEVELS = { 0.25, 0.5, 1, 2, 4, 8 };
	/** Calibration tool types */
	public static final String STICK = "Stick", TAPE = "CalibrationTapeMeasure", //$NON-NLS-1$ //$NON-NLS-2$
			CALIBRATION = "Calibration", OFFSET = "OffsetOrigin"; //$NON-NLS-1$ //$NON-NLS-2$

	protected static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$

	// instance fields
	protected double defaultImageBorder;
	protected String description = ""; //$NON-NLS-1$
	protected TPoint selectedPoint;
	protected Step selectedStep;
	protected TrackerPanel selectingPanel;
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
	private TFrame frame;
	protected BufferedImage renderedImage, matImage; // for video recording
	protected XMLControl currentState, currentCoords, currentSteps;
	protected TPoint pointState = new TPoint();
	protected MouseEvent mEvent;
	protected TMouseHandler mouseHandler;
	protected JLabel badNameLabel = new JLabel();
	protected TrackDataBuilder dataBuilder;
	protected boolean dataToolVisible;
	protected XMLProperty customViewsProperty; // TFrame loads views
	protected XMLProperty selectedViewsProperty; // TFrame sets selected views--legacy pre-JS
	protected XMLProperty selectedViewTypesProperty; // TFrame sets selected view types--JS
	protected double[] dividerLocs; // TFrame sets dividers
	protected Point zoomCenter; // used when loading
	protected Map<Filter, Point> visibleFilters; // TFrame sets locations of filter inspectors
	protected int trackControlX = Integer.MIN_VALUE, trackControlY; // TFrame sets track control location
	protected int infoX = Integer.MIN_VALUE, infoY; // TFrame sets info dialog location
//	protected JPanel noData = new JPanel();
//	protected JLabel[] noDataLabels = new JLabel[2];
//	protected boolean isEmpty;
	protected String defaultSavePath, openedFromPath;
	protected ModelBuilder modelBuilder;
	protected TrackControl trackControl;
	protected boolean isModelBuilderVisible;
	protected boolean isShiftKeyDown, isControlKeyDown;
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
	private boolean isAutoRefresh = true;
	protected TreeSet<String> supplementalFilePaths = new TreeSet<String>(); // HTML/PDF URI paths
	protected Map<String, String> pageViewFilePaths = new HashMap<String, String>();
	protected StepSet selectedSteps = new StepSet(this);
	protected boolean hideDescriptionWhenLoaded;
	protected PropertyChangeListener massParamListener, massChangeListener;
	protected Map<String, TreeMap<String, String>> formatPatterns = new HashMap<>();
	protected String lengthUnit = "m", massUnit = "kg", timeUnit = "s"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	protected boolean unitsVisible = true; // visible by default
	protected TCoordinateStringBuilder coordStringBuilder;
	protected ArrayList<TrackerPanel> panelAndWorldViews = new ArrayList<TrackerPanel>();
	protected double[] dividerFractions = new double[4];

	private int enabledCount;
	
	public NumberFormatDialog numberFormatDialog;


	public String id;
	private static int ids;
	
	private ArrayList<TTrack> userTracks;

	/**
	 * Constructs a blank TrackerPanel with a player.
	 */
	public TrackerPanel() {
		this(null);
	}

	/**
	 * Constructs a TrackerPanel with a video and player.
	 *
	 * @param video the video
	 */
	public TrackerPanel(Video video) {
		super(video);
		displayCoordsOnMouseMoved = true;
		
		id = "TP" + ++ids;
		zoomBox.setShowUndraggedBox(false);
		// remove the interactive panel mouse controller
		removeMouseListener(mouseController);
		removeMouseMotionListener(mouseController);
		// create and add a new mouse controller for tracker
		mouseController = new TMouseController();
		addMouseListener(mouseController);
		addMouseMotionListener(mouseController);
		// set new CoordinateStringBuilder
		coordStringBuilder = new TCoordinateStringBuilder();
		setCoordinateStringBuilder(coordStringBuilder);

		// set fonts of message boxes and noDataLabels
//		Font font = new JTextField().getFont();

		badNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//		Box box = Box.createVerticalBox();
//		noData.add(box);
//		for (int i = 0; i < 2; i++) {
//			noDataLabels[i] = new JLabel();
//			noDataLabels[i].setFont(font);
//			noDataLabels[i].setAlignmentX(0.5f);
//			box.add(noDataLabels[i]);
//		}
//		noData.setOpaque(false);
		player.setInspectorButtonVisible(false);
		player.addActionListener(this);
		// BH! VideoPanel has already done this: player.addFrameListener(this);

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

		//
		if (!(this instanceof WorldTView)) {
			// don't getDataBuilder before adding to TFrame
//			if (Tracker.haveDataFunctions())
//				getDataBuilder(); 
//			// so autoloaded datafunctions are available to tracks
			panelAndWorldViews.add(this);
		}
		configure();
	}

	/**
	 * Overrides VideoPanel setVideo method.
	 *
	 * @param newVideo the video
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
			TActions.getAction("clearFilters", this).actionPerformed(null); //$NON-NLS-1$
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
			return getDataFile().getName();
		}
		if (defaultFileName != null) {
			return defaultFileName;
		}
		if (getVideo() != null) {
			String name = (String) getVideo().getProperty("name"); //$NON-NLS-1$
			if (name != null) {
				name = XML.forwardSlash(name);
				int i = name.lastIndexOf("/"); //$NON-NLS-1$
				if (i >= 0)
					name = name.substring(i + 1);
				return name;
			}
		}
		return TrackerRes.getString("TrackerPanel.NewTab.Name"); //$NON-NLS-1$
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
			modelBuilder.setFontLevel(FontSizer.getLevel());
			modelBuilder.refreshLayout();
			modelBuilder.addPropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_PANEL, this);
			// show model builder
			try {
				// place near top right corner of frame
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				TFrame frame = getTFrame();
				Point frameLoc = frame.getLocationOnScreen();
				int w = modelBuilder.getWidth() + 8;
				int x = Math.min(screen.width - w, frameLoc.x + frame.getWidth() - w);
				int y = getLocationOnScreen().y;
				modelBuilder.setLocation(x, y);
			} catch (Exception ex) {
				/** empty block */
			}
			TFrame frame = getTFrame();
			if (frame != null) {
				frame.addFollower(modelBuilder, frame.getLocation());
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
//			synchronized (dirty) {
//				dirty.grow(2, 2);
			TFrame.repaintT(this);
//				repaint(dirty);
//			}
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
		tracks.removeAll(getDrawables(PerspectiveTrack.class));

		// remove child ParticleDataTracks
		ArrayList<ParticleDataTrack> list = getDrawables(ParticleDataTrack.class);
		for (int m = 0, n = list.size(); m < n; m++) {
			ParticleDataTrack track = list.get(m);
			if (track.getLeader() != track) {
				tracks.remove(track);
			}
		}
		return userTracks = tracks;
	}

	/**
	 * Gets the list of TTracks to save with this panel.
	 *
	 * @return a list of tracks to save
	 */
	public ArrayList<TTrack> getTracksToSave() {
		// remove child ParticleDataTracks
		ArrayList<TTrack> tracks = getTracks();
		ArrayList<ParticleDataTrack> list = getDrawables(ParticleDataTrack.class);
		for (int m = 0, n = list.size(); m < n; m++) {
			ParticleDataTrack track = list.get(m);
			if (track.getLeader() != track) {
				tracks.remove(track);
			}
		}
		return tracks;
	}

	public TTrack getTrack(String name) {
		return getTrack(name, getTracks());
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
		// BH 2020.07.09 
		userTracks = null;
		TTrack.activeTracks.put(track.getID(), track);
		// set trackerPanel property if not yet set
		if (track.trackerPanel == null) {
			track.setTrackerPanel(this);
		}
		boolean showTrackControl = true;
		// set angle format of the track
		if (getTFrame() != null)
			track.setAnglesInRadians(getTFrame().anglesInRadians);
		// special case: axes
		if (track instanceof CoordAxes) {
			showTrackControl = false;
			if (getAxes() != null)
				removeDrawable(getAxes()); // only one axes at a time
			super.addDrawable(track);
			moveToBack(track);
			TMat mat = getMat();
			if (mat != null) {
				moveToBack(mat); // put mat behind grid
			}
		}
		// special case: same calibration tool added again?
		else if (calibrationTools.contains(track)) {
			showTrackControl = false;
			super.addDrawable(track);
		}
		// special case: tape measure
		else if (track instanceof TapeMeasure) {
			showTrackControl = false;
			TapeMeasure tape = (TapeMeasure) track;
			if (!tape.isReadOnly()) { // calibration tape or stick
				calibrationTools.add(tape);
				visibleCalibrationTools.add(tape);
			}
			else { // tape measure
				measuringTools.add(tape);
				visibleMeasuringTools.add(tape);
			}
			super.addDrawable(track);
		}
		// special case: offset origin or calibration points
		else if (track instanceof OffsetOrigin || track instanceof Calibration) {
			showTrackControl = false;
			calibrationTools.add(track);
			visibleCalibrationTools.add(track);
			super.addDrawable(track);
		}
		// special case: protractor or circlefitter
		else if (track instanceof Protractor || track instanceof CircleFitter) {
			showTrackControl = false;
			measuringTools.add(track);
			visibleMeasuringTools.add(track);
			super.addDrawable(track);
		}
		// special case: perspective track
		else if (track instanceof PerspectiveTrack) {
			showTrackControl = false;
			super.addDrawable(track);
		}
		// special case: ParticleDataTrack may add extra points
		else if (track instanceof ParticleDataTrack) {
			super.addDrawable(track);
			final ParticleDataTrack dt = (ParticleDataTrack) track;
			if (dt.allPoints().size() > 1) {
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						for (ParticleDataTrack child : dt.allPoints()) {
							if (child == dt)
								continue;
							addTrack(child);
						}
						TFrame frame = getTFrame();
						if (frame != null && TrackerPanel.this.isShowing()) {
							TView[][] views = frame.getTViews(TrackerPanel.this);
							for (TView[] next : views) {
								for (TView view : next) {
									if (view != null && view instanceof TrackChooserTView) {
										((TrackChooserTView) view).setSelectedTrack(dt);
									}
								}
							}
						}
					}
				};
				SwingUtilities.invokeLater(runner);
			}
		}
		// all other tracks (point mass, vector, particle model, line profile, etc)
		else {
			// set track name--prevents duplicate names
			setTrackName(track, track.getName(), false);
			super.addDrawable(track);
		}

		// DB TTrack.setTrackerPanel() now responsible for adding the track to this TrackerPanel's listeners		
		// here we add this TrackerPanel to the track listeners
		track.addListener(this);
		if (this == track.trackerPanel) {
		}
		
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
		firePropertyChange(PROPERTY_TRACKERPANEL_TRACK, null, track); // to views //$NON-NLS-1$

		// set default NumberField format patterns
		if (getTFrame() != null) {
			track.setInitialFormatPatterns(this);
		}

		changed = true;
		if (showTrackControl && getTFrame() != null && this.isShowing()) {
			TrackControl.getControl(this).setVisible(true);
		}

		// select new track in autotracker
		if (autoTracker != null && track != getAxes()) {
			autoTracker.setTrack(track);
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
			TView[][] views = frame.getTViews(TrackerPanel.this);
			for (TView[] next : views) {
				for (int i = 0; i < next.length; i++) {
					TView view = next[i];
					if (view != null
							&& (view.getViewType() == TView.VIEW_PLOT || view.getViewType() == TView.VIEW_TABLE)
							&& ((TrackChooserTView) view).isTrackViewDisplayed(track)) {
						return true;
					}
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
		FunctionPanel panel = new DataFunctionPanel(data);
		panel.setIcon(track.getIcon(21, 16, "point")); //$NON-NLS-1$
		final ParamEditor paramEditor = panel.getParamEditor();
		// Check for PointMass and Vector, which might be subclassed
		switch(track.getBaseType()) {
		case "PointMass":
			panel.setDescription(PointMass.class.getName());
			PointMass pm = (PointMass) track;
			Parameter param = (Parameter) paramEditor.getObject("m"); //$NON-NLS-1$
			if (param == null) {
				param = new Parameter("m", String.valueOf(pm.getMass())); //$NON-NLS-1$
				param.setDescription(TrackerRes.getString("ParticleModel.Parameter.Mass.Description")); //$NON-NLS-1$
				paramEditor.addObject(param, false);
			}
			param.setNameEditable(false); // mass name not editable
			paramEditor.addPropertyChangeListener("edit", massParamListener); //$NON-NLS-1$
			pm.addPropertyChangeListener("mass", massChangeListener); //$NON-NLS-1$
			break;
		case "Vector":
			panel.setDescription(Vector.class.getName());
		break;
// BH - unnecessary - these are not subclassed
//		case "RGBRegion":
//			panel.setDescription(RGBRegion.class.getName());
//			break;
//		case "LineProfile":
//			panel.setDescription(LineProfile.class.getName());
//			break;
		default:
			panel.setDescription(track.getClass().getName());
			break;
		}
		return panel;
	}

	/**
	 * Removes a track.
	 *
	 * @param track the track to remove
	 */
	public synchronized void removeTrack(TTrack track) {
		if (!getDrawables(track.getClass()).contains(track))
			return;
		userTracks = null;
		removeMyListenerFrom(track);
		super.removeDrawable(track);
		if (dataBuilder != null)
			dataBuilder.removePanel(track.getName());
//    if (modelBuilder != null) modelBuilder.removePanel(track.getName());
		if (getSelectedTrack() == track)
			setSelectedTrack(null);
		// notify views and other listeners
		firePropertyChange(PROPERTY_TRACKERPANEL_TRACK, track, null);
		TTrack.activeTracks.remove(track.getID());
		changed = true;
	}

	private void removeMyListenerFrom(TTrack track) {
		removePropertyChangeListener(track);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_STEP, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_STEPS, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MODELSTART, this);
		track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MODELEND, this);
		if (track instanceof ParticleModel) {
			TFrame frame = getTFrame();
			if (frame != null)
				frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, track);
		}
	}

	/**
	 * Determines if the specified track is in this tracker panel.
	 *
	 * @param track the track to look for
	 * @return <code>true</code> if this contains the track
	 */
	public boolean containsTrack(TTrack track) {
		ArrayList<TTrack> list = getTracks();
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack next = list.get(it);
			if (track == next)
				return true;
		}
		return false;
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
	public void save(Runnable whenSaved, Runnable whenCanceled) {
		if (!changed || OSPRuntime.isApplet) {
			whenSaved.run();
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
							if (whenSaved != null)
								whenSaved.run();
							break;
						case JOptionPane.NO_OPTION:
							if (whenSaved != null)
								whenSaved.run();
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
		
		// DB 10.20.20 no longer need this since 
//		// make GUI changes later in EventQueue
//    SwingUtilities.invokeLater(() -> {
//  		// show noData message if panel is empty
//  		if (getVideo() == null && (userTracks == null || userTracks.isEmpty())) {
//  			isEmpty = true;
//  			if (this instanceof WorldTView) {
//  				noDataLabels[0].setText(TrackerRes.getString("WorldTView.Label.NoData")); //$NON-NLS-1$
//  				noDataLabels[1].setText(null);
//  			} else {
//  				noDataLabels[0].setText(TrackerRes.getString("TrackerPanel.Message.NoData0")); //$NON-NLS-1$
//  				noDataLabels[1].setText(TrackerRes.getString("TrackerPanel.Message.NoData1")); //$NON-NLS-1$
//  			}
//  			add(noData, BorderLayout.NORTH);
//  		} else {
//  			isEmpty = false;
//  			remove(noData);
//  		}
//    });

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
				removeMyListenerFrom((TTrack) removed.get(i));
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
	
	synchronized void clear(boolean andSetCoords) {
		long t0 = Performance.now(0);
		setSelectedTrack(null);
		selectedPoint = null;
		ArrayList<TTrack> list = getTracks();
			for (int i = 0, n = list.size(); i < n; i++) {
				TTrack track = list.get(i);
				removeMyListenerFrom(track);
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
		firePropertyChange(PROPERTY_TRACKERPANEL_CLEAR, null, null);
		// remove tracks from TTrack.activeTracks
		for (int it = 0, n = list.size(); it < n; it++) {
			TTrack track = list.get(it);
			TTrack.activeTracks.remove(track.getID());
		}
		changed = true;
		OSPLog.debug("!!! " + Performance.now(t0) + " TrackerPanel.clear");
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
			_coords.addPropertyChangeListener(this);
			coords = _coords;
			int n = getFrameNumber();
			getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));
			try {
				firePropertyChange(PROPERTY_TRACKERPANEL_COORDS, null, coords);
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
		PointMass pm = null;
		ArrayList<PointMass> list = getDrawables(PointMass.class);
		for (int i = 0, n = list.size(); i < n; i++) {
			PointMass m = list.get(i);
			if (m.getName().equals(trackName)) {
				pm = m;
				break;
			}
		}
		final PointMass thePM = pm;
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
		ArrayList<CoordAxes> list = getDrawables(CoordAxes.class);
		if (!list.isEmpty())
			return list.get(0);
		return null;
	}

	/**
	 * Gets the mat.
	 *
	 * @return the first TMat in the drawable list
	 */
	public TMat getMat() {
		ArrayList<TMat> list = getDrawables(TMat.class);
		if (!list.isEmpty())
			return list.get(0);
		return null;
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
			prevPoint.setAdjusting(false);
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
				ArrayList<TTrack> list = getTracks();
				for (int it = 0, n = list.size(); it < n; it++) {
					TTrack track = list.get(it);
					step = track.getStep(point, this);
					if (step != null) {
						newStepSelected = selectedSteps.contains(step);
						break;
					}
				}
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
			if (prevPointChanged) {
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
			selectingPanel = null;
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
			selectingPanel = this;
			requestFocusInWindow();
		}
		if (selectedStep != null)
			selectedSteps.add(selectedStep);
		selectedSteps.isModified = false;
		firePropertyChange(PROPERTY_TRACKERPANEL_SELECTEDPOINT, prevPoint, point);
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
	 * Gets the selecting tracker panel.
	 *
	 * @return the selecting tracker panel
	 */
	public TrackerPanel getSelectingPanel() {
		return selectingPanel;
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
//			if (view != null && view.getTrackerPanel() != null) {
//				double oldMag = view.getTrackerPanel().getMagnification();
//				Dimension oldDim = view.getTrackerPanel().getSize();
//			}
			int w = (int) (imageWidth * zoom);
			int h = (int) (imageHeight * zoom);
			d = new Dimension(w, h);
		}
		setPreferredSize(d);
		firePropertyChange(PROPERTY_TRACKERPANEL_MAGNIFICATION, prevZoom, getMagnification());
		// scroll and revalidate
		MainTView view = (getTFrame() == null ? null : getTFrame().getMainView(this));
		if (view != null) {
			view.scrollPane.revalidate();
			// this will fire a full panel repaint
			view.scrollToZoomCenter(getPreferredSize(), prevSize, p1);
			eraseAll();
// should not be necessary			TFrame.repaintT(this);
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
		TToolBar toolbar = TToolBar.getToolbar(this);
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
		TTrackBar.getTrackbar(this).refresh();
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
		TTrackBar.getTrackbar(this).refresh();
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
		TTrackBar.getTrackbar(this).refresh();
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
			String angUnit = frame != null && frame.anglesInRadians ? "" : Tracker.DEGREES; //$NON-NLS-1$
			return sp + angUnit + "/" + timeUnit + sq; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns true if mouse coordinates are displayed. Overrides VideoPanel method
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
		DatasetManager datasetManager = DataTool.parseData(dataString, null);
		if (datasetManager == null) {

			// assume dataString is a resource path, read the resource and call this again
			// with path as source
			String path = dataString;
			importDataAsync(ResourceLoader.getString(path), path, whenDone);
			return;
		}
		DataTrack dt = importDatasetManager(datasetManager, source);
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
				int i = getDrawables(PointMass.class).size();
				dataTrack.setColorToDefault(i);
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
			JOptionPane.showMessageDialog(frame, TrackerRes.getString("TrackerPanel.Dialog.Exception.Message") + ":" //$NON-NLS-1$ //$NON-NLS-2$
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
		firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // causes full view rebuild
		isAutoRefresh = auto;
	}

	@Override
	protected void refreshDecimalSeparators() {
		super.refreshDecimalSeparators();
		// refresh the trackbar decimal separators
		TTrackBar.getTrackbar(this).refreshDecimalSeparators();

		// refresh all plot and table views
		// just repaint--no data change at all
		refreshTrackData(DataTable.MODE_FORMAT);

		// refresh modelbuilder and databuilder
		if (modelBuilder != null) {
			modelBuilder.refreshGUI();
		}
		if (dataBuilder != null) {
			dataBuilder.refreshGUI();
		}
		// refresh DataTool
		if (getTFrame() != null && frame.getSelectedPanel() == this
				&& DataTool.getTool(false) != null) {
			DataTool.getTool(false).refreshDecimalSeparators();
		}

		// repaint tracks with readouts
		ArrayList<TapeMeasure> tapes = getDrawables(TapeMeasure.class);

		for (int i = 0, n = tapes.size(); i < n; i++) {
			TapeMeasure tape = tapes.get(i);
			tape.inputField.getFormat(); // sets decimal separator
			tape.repaint(this);
		}
		ArrayList<Protractor> prots = getDrawables(Protractor.class);
		for (int i = 0, n = prots.size(); i < n; i++) {
			Protractor p = prots.get(i);
			p.inputField.getFormat(); // sets decimal separator
			p.xField.getFormat(); // sets decimal separator
			p.yField.getFormat(); // sets decimal separator
			p.repaint(this);
		}

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
				if (vis) {
				} else {
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
	 * Refreshes the TFrame info dialog if visible.
	 */
	protected void refreshNotesDialog() {
		TFrame frame = getTFrame();
		if (frame != null && frame.notesDialog.isVisible()) {
			frame.saveNotesAction.actionPerformed(null);
			TTrack track = getSelectedTrack();
			if (track != null) {
				frame.notesTextPane.setText(track.getDescription());
				frame.notesDialog.setName(track.getName());
				frame.notesDialog.setTitle(TrackerRes.getString("TActions.Dialog.Description.Title") //$NON-NLS-1$
						+ " \"" + track.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				frame.notesTextPane.setText(getDescription());
				frame.notesDialog.setName(null);
				String tabName = frame.getTabTitle(frame.getSelectedTab());
				frame.notesDialog.setTitle(TrackerRes.getString("TActions.Dialog.Description.Title") //$NON-NLS-1$
						+ " \"" + tabName + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			frame.notesTextPane.setBackground(Color.WHITE);
			frame.cancelNotesDialogButton.setEnabled(false);
			frame.closeNotesDialogButton.setEnabled(true);
			TrackerPanel panel = frame.getSelectedPanel();
			frame.displayWhenLoadedCheckbox.setEnabled(panel != null);
			if (panel != null) {
				frame.displayWhenLoadedCheckbox.setSelected(!panel.hideDescriptionWhenLoaded);
			}

			frame.notesTextPane.setEditable(isEnabled("notes.edit")); //$NON-NLS-1$
		}
	}

	/**
	 * Gets the alphabet index for setting the name letter suffix and color of a
	 * track.
	 * 
	 * @param name      the default name with no letter suffix
	 * @param connector the string connecting the name and letter
	 * @return the index of the first available letter suffix
	 */
	protected int getAlphabetIndex(String name, String connector) {
		for (int i = 0; i < alphabet.length(); i++) {
			String letter = alphabet.substring(i, i + 1);
			String proposed = name + connector + letter;
			boolean isTaken = false;
			ArrayList<TTrack> list = getTracks();
			for (int it = 0, n = list.size(); it < n; it++) {
				TTrack track = list.get(it);
				String nextName = track.getName();
				isTaken = isTaken || proposed.equals(nextName);
			}
			if (!isTaken)
				return i;
		}
		return 0;
	}

	/**
	 * Restores the views to a non-maximized state.
	 */
	protected void restoreViews() {

		TFrame frame = getTFrame();
		if (frame != null) {
			int n = frame.maximizedView;
			if (n < 0)
				return;
			if (n == TView.VIEW_MAIN) {
				TTrackBar.getTrackbar(this).maximizeButton.doClick(0);
			}
			else {
				TViewChooser viewChooser = frame.getViewChoosers(this)[n];
				viewChooser.restore();				
			}
//			frame.restoreViews(this);
		}
	}

	/**
	 * Configures this panel.
	 */
	protected void configure() {
		coords.removePropertyChangeListener(this);
		coords.addPropertyChangeListener(this);
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
						&& getCursor() == selectedTrack.getMarkingCursor(e) && getFrameNumber() > 0) {
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
								selectedTrack.repaint(clone);
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

	/**
	 * Sets the cursor to a crosshair when the selected track is marking and is
	 * unmarked on the current frame. Also displays hints as a side effect.
	 *
	 * @param invert true to invert the normal state
	 * @param e      an input event
	 * @return true if marking (ie next mouse click will mark a TPoint)
	 */
	protected boolean setCursorForMarking(boolean invert, InputEvent e) {
		if (isClipAdjusting() 
				|| Tracker.isZoomInCursor(getCursor()) || Tracker.isZoomOutCursor(getCursor()))
			return false;
		boolean markable = false;
		boolean marking = false;
		selectedTrack = getSelectedTrack();
		int n = getFrameNumber();
		if (selectedTrack != null) {
			markable = !(selectedTrack.isStepComplete(n) || selectedTrack.isLocked() || popup != null && popup.isVisible());
			marking = markable && (selectedTrack.isMarkByDefault() != invert);
		}
		Interactive iad = getTracks().isEmpty() || mouseEvent == null ? null : getInteractive();
		if (marking) {
			setMouseCursor(selectedTrack.getMarkingCursor(e));
			if (Tracker.showHints) {
				if (selectedTrack instanceof PointMass) {
					if (selectedTrack.getStep(n) == null)
						setMessage(TrackerRes.getString("PointMass.Hint.Marking")); //$NON-NLS-1$
					else
						setMessage(TrackerRes.getString("PointMass.Remarking.Hint")); //$NON-NLS-1$
				} else if (selectedTrack instanceof Vector)
					if (selectedTrack.getStep(n) == null)
						setMessage(TrackerRes.getString("Vector.Hint.Marking")); //$NON-NLS-1$
					else
						setMessage(TrackerRes.getString("Vector.Remarking.Hint")); //$NON-NLS-1$
				else if (selectedTrack instanceof LineProfile)
					setMessage(TrackerRes.getString("LineProfile.Hint.Marking")); //$NON-NLS-1$
				else if (selectedTrack instanceof RGBRegion)
					setMessage(TrackerRes.getString("RGBRegion.Hint.Marking")); //$NON-NLS-1$
			} else
				setMessage(""); //$NON-NLS-1$
		} else if (iad instanceof TPoint) {
			setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			// identify associated track and display its hint
			ArrayList<TTrack> list = getTracks();
			for (int it = 0, ni = list.size(); it < ni; it++) {
				TTrack track = list.get(it);
				Step step = track.getStep((TPoint) iad, this);
				if (step != null) {
					setMessage(track.getMessage());
					break;
				}
			}
		} else { // no point selected
			setMouseCursor(Cursor.getDefaultCursor());
			// display selected track hint
			if (Tracker.showHints && selectedTrack != null) {
				setMessage(selectedTrack.getMessage());
			} else if (!Tracker.startupHintShown || getVideo() != null
					|| (userTracks != null && !userTracks.isEmpty())) {
				Tracker.startupHintShown = false;
				if (!Tracker.showHints)
					setMessage(""); //$NON-NLS-1$
				// show hints
				else if (getVideo() == null) // no video
					setMessage(TrackerRes.getString("TrackerPanel.NoVideo.Hint")); //$NON-NLS-1$
				else if (TToolBar.getToolbar(this).notYetCalibrated) {
					if (getVideo().getWidth() == 720 && getVideo().getFilterStack().isEmpty()) // DV video format
						setMessage(TrackerRes.getString("TrackerPanel.DVVideo.Hint")); //$NON-NLS-1$
					else if (getPlayer().getVideoClip().isDefaultState())
						setMessage(TrackerRes.getString("TrackerPanel.SetClip.Hint")); //$NON-NLS-1$
					else
						setMessage(TrackerRes.getString("TrackerPanel.CalibrateVideo.Hint")); //$NON-NLS-1$
				} else if (getAxes() != null && getAxes().notyetShown)
					setMessage(TrackerRes.getString("TrackerPanel.ShowAxes.Hint")); //$NON-NLS-1$
				else if (userTracks == null || userTracks.isEmpty())
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
			TFrame frame = getTFrame();
			if (frame != null) {
				if (selectedTrack == null)
					frame.showHelp("help", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof CoordAxes)
					frame.showHelp("axes", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof TapeMeasure)
					frame.showHelp("tape", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof OffsetOrigin)
					frame.showHelp("offset", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof Calibration)
					frame.showHelp("calibration", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof PointMass)
					frame.showHelp("pointmass", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof CenterOfMass)
					frame.showHelp("cm", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof Vector)
					frame.showHelp("vector", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof VectorSum)
					frame.showHelp("vectorsum", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof LineProfile)
					frame.showHelp("profile", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof RGBRegion)
					frame.showHelp("rgbregion", 0); //$NON-NLS-1$
				else if (selectedTrack instanceof ParticleModel)
					frame.showHelp("particle", 0); //$NON-NLS-1$
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
			if (selectedPoint != null && selectingPanel == this) {
				deletePoint(selectedPoint);
			}
			else {
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
				TTrackBar.getTrackbar(this).refresh();
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
			firePropertyChange(PROPERTY_TRACKERPANEL_COORDS, oldCoords, coords); // to tracks //$NON-NLS-1$
			firePropertyChange(PROPERTY_TRACKERPANEL_VIDEO, null, null); // to TMenuBar & views //$NON-NLS-1$
			if (getMat() != null) {
				getMat().isValidMeasure = false;
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
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						renderMat();
						VideoGrabber.getTool().addFrame(matImage);
					}
				};
				EventQueue.invokeLater(runner);
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
			firePropertyChange(PROPERTY_TRACKERPANEL_COORDS, null, coords); // to tracks //$NON-NLS-1$
			firePropertyChange(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, null, null); // to tracks/views //$NON-NLS-1$
			doSnap = true;
			break;
		case Video.PROPERTY_VIDEO_IMAGE: // from video //$NON-NLS-1$
			firePropertyChange(PROPERTY_TRACKERPANEL_IMAGE, null, null); // to tracks/views //$NON-NLS-1$
			TMenuBar.getMenuBar(this).checkMatSize();
			TFrame.repaintT(this);
			break;
		case Video.PROPERTY_VIDEO_FILTERCHANGED: // from video //$NON-NLS-1$
			Filter filter = (Filter) e.getNewValue();
			String prevState = (String) e.getOldValue();
			XMLControl control = new XMLControlElement(prevState);
			Undo.postFilterEdit(this, filter, control);
			break;
		case Video.PROPERTY_VIDEO_VIDEOVISIBLE: // from video //$NON-NLS-1$
			firePropertyChange(PROPERTY_TRACKERPANEL_VIDEOVISIBLE, null, null); // to views //$NON-NLS-1$
			TFrame.repaintT(this);
			break;
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM: // from coords //$NON-NLS-1$
			changed = true;
			doSnap = true;

			firePropertyChange(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, null, null); // to tracks/views //$NON-NLS-1$
			break;
		case ImageCoordSystem.PROPERTY_COORDS_LOCKED: // from coords //$NON-NLS-1$
			firePropertyChange(TTrack.PROPERTY_TTRACK_LOCKED, null, null); // to tracker frame //$NON-NLS-1$
			break;
		case VideoPlayer.PROPERTY_VIDEOPLAYER_PLAYING: // from player //$NON-NLS-1$
			if (!((Boolean) e.getNewValue()).booleanValue()) {
				ArrayList<ParticleModel> list = getDrawables(ParticleModel.class);
				for (int m = 0, n = list.size(); m < n; m++) {
					list.get(m).refreshDerivsIfNeeded();
				}
			}
			break;
		case Trackable.PROPERTY_ADJUSTING: // from videoClip //$NON-NLS-1$
			isAdjusting = true;
		case VideoClip.PROPERTY_VIDEOCLIP_STARTFRAME: // from videoClip //$NON-NLS-1$
		case VideoClip.PROPERTY_VIDEOCLIP_STEPSIZE: // from videoClip //$NON-NLS-1$
		case VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT: // from videoClip //$NON-NLS-1$
		case VideoClip.PROPERTY_VIDEOCLIP_STARTTIME: // from videoClip //$NON-NLS-1$
		case ClipControl.PROPERTY_CLIPCONTROL_FRAMEDURATION: {// from clipControl //$NON-NLS-1$
			changed = true;
			if (modelBuilder != null)
				modelBuilder.refreshSpinners();
			if (getMat() != null) {
				getMat().isValidMeasure = false;
			}
			if (getVideo() != null) {
				getVideo().setProperty("measure", null); //$NON-NLS-1$
			}
			// BH added e.newValue  (Boolean.TRUE or Boolean.FALSE)
			firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, e.getOldValue(), isAdjusting ? e.getNewValue() : null); // to views //$NON-NLS-1$
			// to particle models
			firePropertyChange(name, e.getSource(), name == Trackable.PROPERTY_ADJUSTING ? e.getNewValue() : null); 
			if (getSelectedPoint() != null) {
				getSelectedPoint().showCoordinates(this);
				TFrame frame = getTFrame();
				if (frame != null)
					frame.getTrackBar(this).refresh();
			}
			ArrayList<TTrack> list = getUserTracks();
			for (int it = 0, ni = list.size(); it < ni; it++) {
				list.get(it).erase(this);
			}
			TFrame.repaintT(this);
		}
			break;
		case "framecount": //$NON-NLS-1$
			if (getVideo() == null && modelBuilder != null)
				modelBuilder.refreshSpinners();
			break;
		case FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION: // from DataBuilder //$NON-NLS-1$
			changed = true;
			firePropertyChange(PROPERTY_TRACKERPANEL_FUNCTION, null, e.getNewValue()); // to views //$NON-NLS-1$
			break;
		case FunctionTool.PROPERTY_FUNCTIONTOOL_PANEL:
			if (e.getSource() == modelBuilder) {
				FunctionPanel panel = (FunctionPanel) e.getNewValue();
				if (panel != null) { // new particle model panel added
					track = getTrack(panel.getName());
					if (track != null) {
//    			setSelectedTrack(track);
						ParticleModel model = (ParticleModel) track;
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
		case TTrack.PROPERTY_TTRACK_MODELSTART: {
			ParticleModel model = (ParticleModel) e.getSource();
			if (model.getName().equals(getModelBuilder().getSelectedName())) {
				modelBuilder.setSpinnerStartFrame(e.getNewValue());
			}
		}
			break;
		case TTrack.PROPERTY_TTRACK_MODELEND: {
			ParticleModel model = (ParticleModel) e.getSource();
			if (model.getName().equals(getModelBuilder().getSelectedName())) {
				int end = (Integer) e.getNewValue();
				if (end == Integer.MAX_VALUE) {
					end = getPlayer().getVideoClip().getLastFrameNumber();
				}
				modelBuilder.setSpinnerEndFrame(end);
			}
		}
			break;
		case TFrame.PROPERTY_TFRAME_RADIANANGLES: // angle format has changed //$NON-NLS-1$
			firePropertyChange(TFrame.PROPERTY_TFRAME_RADIANANGLES, null, e.getNewValue()); // to tracks //$NON-NLS-1$
			break;
		case "fixed_origin": //$NON-NLS-1$
		case "fixed_angle": //$NON-NLS-1$
		case "fixed_scale": //$NON-NLS-1$
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
		case "perspective": //$NON-NLS-1$
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
		TMat mat = getMat();
		if (mat != null) {
			xOffset = mat.getXOffset();
			yOffset = mat.getYOffset();
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
		TFrame frame = getTFrame();
		if (frame != null && trackControl == null)
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
		TView[][] views = frame.getTViews(this);
		if (views == null)
			return;
		for (int i = 0, n = views.length; i < n; i++) {
			if (views[i] != null) {
				for (int j = 0, nj = views[i].length; j < nj; j++)
					if (views[i][j] != null)
						views[i][j].refresh();
			}
		}
		TTrackBar trackbar = TTrackBar.getTrackbar(this);
		trackbar.setFontLevel(level);
		trackbar.refresh();
//		TToolBar.getToolbar(this).refresh(false);
		// replace the menubar to get new accelerator fonts
		// TMenuBar menubar =

		// select the correct fontSize menu radiobutton
//		if (menubar.fontSizeGroup != null) {
//			Enumeration<AbstractButton> e = menubar.fontSizeGroup.getElements();
//			for (; e.hasMoreElements();) {
//				AbstractButton button = e.nextElement();
//				int i = Integer.parseInt(button.getActionCommand());
//				if (i == FontSizer.getLevel()) {
//					button.setSelected(true);
//				}
//			}
//		}

		ArrayList<TTrack> list = getTracks();
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
		mEvent = mouseEvent; // to provide visibility to Tracker package
		Interactive iad = null;
		TTrack track = getSelectedTrack();
		if (track != null && this.getCursor() == track.getMarkingCursor(mEvent))
			return null;
		if (track != null && (track.isDependent() || track == getAxes())) {
			iad = getAxes().findInteractive(this, mouseEvent.getX(), mouseEvent.getY());
		}
		if (iad == null && track != null && track != getAxes() && !calibrationTools.contains(track)) {
			iad = track.findInteractive(this, mouseEvent.getX(), mouseEvent.getY());
		}
		if (iad != null)
			return iad;
		return super.getInteractive();
	}

	@Override
	public XYCoordinateStringBuilder getXYCoordinateStringBuilder(TPoint point) {
		return coordStringBuilder;
	}

	@Override
	public void finalize() {
		OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

//	protected void addCalibrationTool(String name, TTrack tool) {
//		calibrationTools.add(tool);
//		addTrack(tool);
//	}
//
	protected BufferedImage renderMat() {
		if (renderedImage == null || renderedImage.getWidth() != getWidth()
				|| renderedImage.getHeight() != getHeight()) {
			renderedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		}
		render(renderedImage);
		Rectangle rect = getMat().drawingBounds;
		if (matImage == null || matImage.getWidth() != rect.width || matImage.getHeight() != rect.height) {
			matImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
		}
		Graphics g = matImage.getGraphics();
		g.drawImage(renderedImage, -rect.x, -rect.y, null);
		return matImage;
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
				if (track instanceof PointMass) {
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
//			System.out.println("TrackerPanel not paintable");
			return;
		}

		// BH moved this up, because why paint if you are going to paint again?
		if (zoomCenter != null && isShowing() && getTFrame() != null && scrollPane != null) {
			
			final Rectangle rect = scrollPane.getViewport().getViewRect();
			int x = zoomCenter.x - rect.width / 2;
			int y = zoomCenter.y - rect.height / 2;
			rect.setLocation(x, y);
			zoomCenter = null;
			scrollRectToVisible(rect);
			return;
		}

//		long t0 = Performance.now(0);

		 //OSPLog.debug(Performance.timeCheckStr("TrackerPanel.paintComp 0",
		 //Performance.TIME_MARK));

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
	protected TreeMap<String, String> getFormatPatterns(String trackType) {
		TreeMap<String, String> patterns = formatPatterns.get(trackType);
		if (patterns == null) {
			patterns = new TreeMap<String, String>();
			formatPatterns.put(trackType, patterns);
			// initialize with default patterns
			TreeMap<String, String> defaultPatterns = TTrack.getDefaultFormatPatterns(trackType);
			if (defaultPatterns != null) {
				patterns.putAll(defaultPatterns);
			}
			// initialize for additional trackType variables
			ArrayList<String> vars = TTrack.getAllVariables(trackType);
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
		ArrayList<TTrack> list = getTracks();
		for (int it = 0, n = list.size(); it < n; it++) {
			list.get(it).setInitialFormatPatterns(this);
		}
	}


	/**
	 * Disposes of this panel
	 */
	@Override
	protected void dispose() {

		long t0 = Performance.now(0);
		super.dispose();

		refreshTimer.stop();
		zoomTimer.stop();
		refreshTimer = zoomTimer = null;
		offscreenImage = null;
		workingImage = null;

		removeMouseListener(mouseController);
		removeMouseMotionListener(mouseController);
		mouseController = null;
		removeMouseListener(optionController);
		removeMouseMotionListener(optionController);
		optionController = null;
		VideoClip clip = player.getVideoClip();
		clip.removePropertyChangeListener(player);
		clip.removeListener(this);
		if (video != null) {
			video.removeListener(this);
		}
		ClipControl clipControl = player.getClipControl();
		clipControl.removePropertyChangeListener(player);
		player.removeActionListener(this);
		player.removeFrameListener(this);
		coords.removePropertyChangeListener(this);
		coords = null;
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			removePropertyChangeListener(track);
			track.removeListener(this);
		}
		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose removeListeners",
		// Performance.TIME_MARK));

		player.stop();
		remove(player);
		player = null;

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
			algorithmDialog.trackerPanel = null;
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
				&& ExportDataDialog.dataExporter.trackerPanel == this) {
			ExportDataDialog.dataExporter.trackerPanel = null;
			ExportDataDialog.dataExporter.tableDropdown.removeAllItems();
			ExportDataDialog.dataExporter.tables.clear();
			ExportDataDialog.dataExporter.trackNames.clear();
		}
		if (TFrame.haveExportDialog && ExportVideoDialog.videoExporter != null
				&& ExportVideoDialog.videoExporter.trackerPanel == this) {
			ExportVideoDialog.videoExporter.trackerPanel = null;
			ExportVideoDialog.videoExporter.views.clear();
		}
		if (TFrame.haveExportDialog)
			ExportZipDialog.dispose(this);

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose export dialogs",
		// Performance.TIME_MARK));

		if (TFrame.haveThumbnailDialog && ThumbnailDialog.thumbnailDialog != null
				&& ThumbnailDialog.thumbnailDialog.trackerPanel == this) {
			ThumbnailDialog.thumbnailDialog.trackerPanel = null;
		}

		if (numberFormatDialog != null) {
			numberFormatDialog.setVisible(false);
			numberFormatDialog.trackerPanel = null;
		}
		filterClasses.clear();
		selectingPanel = null;
		frame = null;
		renderedImage = null;
		matImage = null;
		selectedSteps = null;

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose number, thumbnail
		// dialogs", Performance.TIME_MARK));

		removeAll();

		// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.dispose removeall",
		// Performance.TIME_MARK));

		OSPLog.debug("!!! " + Performance.now(t0) + " TrackerPanel.dispose");
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
		TMenuBar.refreshMenus(this, TMenuBar.REFRESH_TPANEL_SETTRACKNAME);
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
				int x = Math.max(p.x + (frame == null ? 0 : frame.getLocation().x), 0);
				x = Math.min(x, dim.width - inspector.getWidth());
				int y = Math.max(p.y + (frame == null ? 0 : frame.getLocation().y), 0);
				y = Math.min(y, dim.height - inspector.getHeight());
				inspector.setLocation(x, y);
				inspector.setVisible(true);
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
				messages.setMessage(null, 0); // BL message box
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
		 * Creates an object.
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return new TrackerPanel();
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
			// BH adds early setting of frame.
			asyncloader = (AsyncLoader) ((XMLControlElement) control).getData();
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
					result = Tracker.compareVersions(fileVersion, Tracker.VERSION);
				} catch (Exception e) {
				}
				if (result > 0) { // file is newer version than Tracker
					JOptionPane.showMessageDialog(trackerPanel,
							TrackerRes.getString("TrackerPanel.Dialog.Version.Message1") //$NON-NLS-1$
									+ " " + fileVersion + " " //$NON-NLS-1$ //$NON-NLS-2$
									+ TrackerRes.getString("TrackerPanel.Dialog.Version.Message2") //$NON-NLS-1$
									+ "\n" + TrackerRes.getString("TrackerPanel.Dialog.Version.Message3") //$NON-NLS-1$ //$NON-NLS-2$
									+ " (" + Tracker.VERSION + ")." //$NON-NLS-1$ //$NON-NLS-2$
									+ "\n\n" + TrackerRes.getString("TrackerPanel.Dialog.Version.Message4") //$NON-NLS-1$ //$NON-NLS-2$
									+ " https://" + Tracker.trackerWebsite + ".", //$NON-NLS-1$ //$NON-NLS-2$
							TrackerRes.getString("TrackerPanel.Dialog.Version.Title"), //$NON-NLS-1$
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
			// load the description
			trackerPanel.hideDescriptionWhenLoaded = control.getBoolean("hide_description"); //$NON-NLS-1$
			String desc = control.getString("description"); //$NON-NLS-1$
			if (desc != null) {
				trackerPanel.setDescription(desc);
				trackerPanel.getTFrame().showNotes(trackerPanel);
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
					TreeMap<String, String> patternMap = trackerPanel.getFormatPatterns(next[0]);
					for (int i = 1; i < next.length - 1; i = i + 2) {
						patternMap.put(next[i], next[i + 1]);
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
			for (int n = 0, i = props.size(); --i >= 0 && n < 2;) {
				// n < 2 since "selected_views" & "selected_view_types" should never BOTH exist
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
				case "views":
					trackerPanel.customViewsProperty = prop;
					n++;
					break;
				}
			}

			super.loadObject(control, obj);
			return trackerPanel;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void finalizeLoading() {
			videoPanel.setLoader(null);
			long t0 = Performance.now(0);
			OSPLog.debug(Performance.timeCheckStr("TrackerPanel.finalizeLoading1", Performance.TIME_MARK));
			TrackerPanel trackerPanel = (TrackerPanel) videoPanel;
			try {
				OSPLog.debug("TrackerPanel.finalizeLoading start");
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
				OSPLog.debug(
						Performance.timeCheckStr("TrackerPanel.finalizeLoading finalizeClip", Performance.TIME_MARK));


				// load the clip control
				child = control.getChildControl("clipcontrol"); //$NON-NLS-1$
				if (child != null) {
					child.loadObject(trackerPanel.getPlayer().getClipControl());
				}

				OSPLog.debug(
						Performance.timeCheckStr("TrackerPanel.finalizeLoading clipControl ", Performance.TIME_MARK));

				// load the toolbar
				child = control.getChildControl("toolbar"); //$NON-NLS-1$
				if (child != null) {
					TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
					child.loadObject(toolbar);
				}

				OSPLog.debug(Performance.timeCheckStr("TrackerPanel.finalizeLoading toolbar", Performance.TIME_MARK));

				// load the coords
				child = control.getChildControl("coords"); //$NON-NLS-1$
				if (child != null) {
					ImageCoordSystem coords = trackerPanel.getCoords();
					child.loadObject(coords);
					int n = trackerPanel.getFrameNumber();
					trackerPanel.getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));
				}

				// load the tracks
				ArrayList<?> tracks = ArrayList.class.cast(control.getObject("tracks")); //$NON-NLS-1$
				if (tracks != null) {
					for (int i = 0, n = tracks.size(); i < n; i++) {
						trackerPanel.addTrack((TTrack) tracks.get(i));
					}
					// wait until all tracks are added, then finalize the loading
					// for those that need it -- CenterOfMass, DyanamicSystem, and VectorSum
					for (int i = 0, n = tracks.size(); i < n; i++) {
						((TTrack) tracks.get(i)).initialize(trackerPanel);
					}
				}

				OSPLog.debug(
						Performance.timeCheckStr("TrackerPanel.finalizeLoading load tracks", Performance.TIME_MARK));

				// load drawing scenes saved in vers 4.11.0+
				ArrayList<PencilScene> scenes = (ArrayList<PencilScene>) control.getObject("drawing_scenes"); //$NON-NLS-1$
				if (scenes != null) {
					PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
					drawer.setDrawingsVisible(control.getBoolean("drawings_visible")); //$NON-NLS-1$
					// replace previous scenes
					drawer.setScenes(scenes);
				}
				// load drawings saved with vers 4.10.0
				ArrayList<PencilDrawing> drawings = (ArrayList<PencilDrawing>) control.getObject("drawings"); //$NON-NLS-1$
				if (drawings != null) {
					PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
					drawer.setDrawingsVisible(control.getBoolean("drawings_visible")); //$NON-NLS-1$
					// clear previous scenes and add drawings to new one
					drawer.clearScenes();
					for (int i = 0, n = drawings.size(); i < n; i++) {
						drawer.addDrawingtoSelectedScene(drawings.get(i));
					}
				}

				// OSPLog.debug(Performance.timeCheckStr("TrackerPanel.finalizeLoading scenes
				// and pencil ", Performance.TIME_MARK));

				// load the reference frame
				String rfName = control.getString("referenceframe"); //$NON-NLS-1$
				if (rfName != null) {
					trackerPanel.setReferenceFrame(rfName);
				}
				// set selected track
				String name = control.getString(PROPERTY_TRACKERPANEL_SELECTEDTRACK); //$NON-NLS-1$
				trackerPanel.setSelectedTrack(name == null ? null : trackerPanel.getTrack(name));

			} finally {
				OSPLog.debug("!!! " + Performance.now(t0) + " TrackerPanel.finalizeLoading");
				OSPLog.debug("TrackerPanel.finalizeLoading done");
			}
			if (asyncloader != null) {
				asyncloader.finalized(trackerPanel);
				asyncloader = null;
			}
		}

		protected void setDataTabs(TrackerPanel trackerPanel, ArrayList<DataToolTab> addedTabs) {
			final DataToolTab tab = addedTabs.get(0);

			// set the owner of the tab to the specified track
			String trackname = tab.getOwnerName();
			TTrack track = trackerPanel.getTrack(trackname);
			if (track == null)
				return;
			Data data = track.getData(trackerPanel);
			tab.setOwner(trackname, data);

			// set up a DataRefreshTool and send it to the tab
			final DataRefreshTool refresher = DataRefreshTool.getTool(data);
			DatasetManager toSend = new DatasetManager();
			toSend.setID(data.getID());
			tab.send(new LocalJob(toSend), refresher);

			// set the tab column IDs to the track data IDs and add track data to the
			// refresher
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for (TTrack tt : trackerPanel.getTracks()) {
						Data trackData = tt.getData(trackerPanel);
						if (tab.setOwnedColumnIDs(tt.getName(), trackData)) {
							// true if track owns one or more columns
							refresher.addData(trackData);
						}
					}
				}
			});
			// tab is now fully "wired" for refreshing by tracks
		}

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
//      control.setValue("version", Tracker.VERSION); //$NON-NLS-1$
			// changed to semantic version June 15 2017
			control.setValue("semantic_version", Tracker.VERSION); //$NON-NLS-1$
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
			String[][] customPatterns = trackerPanel.getCustomFormatPatterns();
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
				for (int i = 0; i < dividerLocations.length; i++) {
					JSplitPane pane = frame.getSplitPane(trackerPanel, i);
					if (i == 0)
						w = pane.getMaximumDividerLocation();
					int max = i == 3 ? w : pane.getMaximumDividerLocation();
					dividerLocations[i] = Math.min(1.0, 1.0 * pane.getDividerLocation() / max);
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

				// save the toolbar for button states
				TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
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
				if (frame.notesDialog.isVisible()) {
					int x = frame.notesDialog.getLocation().x - frame.getLocation().x;
					int y = frame.notesDialog.getLocation().y - frame.getLocation().y;
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

	}

	public boolean isAutoRefresh() {
		return isAutoRefresh && Tracker.allowDataRefresh;
	}

	/**
	 * Gets the custom format patterns for a specified TrackerPanel for
	 * TrackerPanel.Loader.saveObject
	 *
	 * @return array of arrays with variable names and custom patterns
	 */
	protected String[][] getCustomFormatPatterns() {
		String path = PointMass.class.getName();
		path = path.substring(0, path.lastIndexOf(".") + 1);
		ArrayList<String[]> formats = new ArrayList<String[]>();
		// look at all track types defined in defaultFormatPatterns
		for (String type : TTrack.defaultFormatPatterns.keySet()) {
			TreeMap<String, String> defaultPatterns = TTrack.defaultFormatPatterns.get(type);
			TreeMap<String, String> patterns = getFormatPatterns(type);
			ArrayList<String> customPatterns = new ArrayList<String>();
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
				Iterator<TTrack> it = getTracks().iterator();
				while (it.hasNext()) {
					track = it.next();
					step = track.getStep(p, this);
					if (step != null)
						break;
				}
				if (step != null) { // found clicked track
					Step prev = selectedStep;
					selectedStep = step;
					if (track instanceof ParticleDataTrack) {
						popup = ((ParticleDataTrack) track).getPointMenu(this).getPopupMenu();
					} else {
						popup = track.getMenu(this, new JMenu()).getPopupMenu();
					}
					selectedStep = prev;
					getZoomBox().setVisible(false);
					return popup;
				}
			} else if (iad instanceof TTrack) {
				// look for direct track clicks
				TTrack track = (TTrack) iad;
				if (track instanceof TapeMeasure) {
					popup = ((TapeMeasure) track).getInputFieldPopup();
				} else if (track instanceof Protractor) {
					popup = ((Protractor) track).getInputFieldPopup();
				} else {
					popup = track.getMenu(this, null).getPopupMenu();
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
					TToolBar toolbar = TToolBar.getToolbar(TrackerPanel.this);
					toolbar.refreshZoomButton();
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
			Action vidPropsAction = TActions.getAction("aboutVideo", this); //$NON-NLS-1$
			JMenuItem propertiesItem = new JMenuItem(vidPropsAction);
			popup.addSeparator();
			propertiesItem.setText(TrackerRes.getString("TActions.AboutVideo")); //$NON-NLS-1$
			popup.add(propertiesItem);

			// print menu item
			if (isEnabled("file.print")) { //$NON-NLS-1$
				if (popup.getComponentCount() > 0)
					popup.addSeparator();
				Action printAction = TActions.getAction("print", this); //$NON-NLS-1$
				popup.add(printAction);
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
			if (!((PointMass) track).isPositionVisible(this))
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
			if (!((PointMass) track).isPositionVisible(this))
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

	public void paint(Graphics g) {
		System.out.println("TrackerPanel.paint");
		super.paint(g);
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

	public void setTFrame(TFrame frame) {
		this.frame = frame;
	}

	public void notifyLoadingComplete() {
		setIgnoreRepaint(false);
		firePropertyChange(PROPERTY_TRACKERPANEL_LOADED, null, null);
	}

//	@Override
//	public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
//		if (listener instanceof TrackView)
//			OSPLog.debug("Trackerpanel add " + name + listener);
//		super.addPropertyChangeListener(name, listener);
//	}

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
		DatasetManager datasetManager = DataTool.parseData(dataString, null);
		if (datasetManager != null) {
			String dataName = datasetManager.getName().replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$ ;
			boolean foundMatch = false;
			ArrayList<DataTrack> dataTracks = this.getDrawables(DataTrack.class);
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
						track.setData(datasetManager);
						track.prevDataString = dataString;
					} else {
						// set pending data
						track.setPendingDataString(dataString);
					}
					break;
				}
			}
			// if no matching track was found then create new track
			if (!foundMatch && frame.alwaysListenToClipboard) {
				dt = importDatasetManager(datasetManager, null);
				if (dt != null && dt instanceof ParticleDataTrack) {
					ParticleDataTrack track = (ParticleDataTrack) dt;
					track.prevDataString = track.pendingDataString = dataString;
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
		addFilter(BrightnessFilter.class);
		addFilter(BaselineFilter.class);
		addFilter(SumFilter.class);
		addFilter(ResizeFilter.class);
		addFilter(RotateFilter.class);
		addFilter(PerspectiveFilter.class);
		addFilter(RadialDistortionFilter.class);
	}


}
