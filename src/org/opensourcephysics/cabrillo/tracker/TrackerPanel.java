/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
import java.io.File;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.*;

/**
 * This extends VideoPanel to manage and draw TTracks. It is Tracker's main view
 * and repository of a video and its associated tracks.
 *
 * @author Douglas Brown
 */
public class TrackerPanel extends VideoPanel implements Scrollable {

  // static fields
  /** The minimum zoom level */
  public static final double MIN_ZOOM = 0.15;
  /** The maximum zoom level */
  public static final double MAX_ZOOM = 12;
  /** The zoom step size */
  public static final double ZOOM_STEP = Math.pow(2, 1.0/6);
  /** The fixed zoom levels */
  public static final double[] ZOOM_LEVELS = {0.25, 0.5, 1, 2, 4, 8}; 
  /** Calibration tool types */
	@SuppressWarnings("javadoc")
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
  protected AffineTransform prevPixelTransform;
  protected double zoom = 1;
  protected JScrollPane scrollPane;
  protected JPopupMenu popup;
  protected Set<String> enabled; // enabled GUI features (subset of full_config)
  protected TPoint snapPoint; // used for origin snap
  protected TFrame frame;
  protected BufferedImage renderedImage, matImage; // for video recording
  protected XMLControl currentState, currentCoords, currentSteps;
  protected TPoint pointState = new TPoint();
  protected MouseEvent mEvent;
  protected TMouseHandler mouseHandler;
  protected JLabel badNameLabel = new JLabel();
  protected TrackDataBuilder dataBuilder;
  protected boolean dataToolVisible;
  protected XMLProperty viewsProperty; // TFrame loads views
  protected XMLProperty selectedViewsProperty; // TFrame sets selected views
  protected double[] dividerLocs; // TFrame sets dividers
  protected Point zoomCenter; // used when loading
  protected Map<Filter, Point> visibleFilters; // TFrame sets locations of filter inspectors
  protected int trackControlX = Integer.MIN_VALUE, trackControlY; // TFrame sets track control location
  protected int infoX = Integer.MIN_VALUE, infoY; // TFrame sets info dialog location
  protected JPanel noData = new JPanel();
  protected JLabel[] noDataLabels = new JLabel[2];
  protected boolean isEmpty;
  protected String defaultSavePath, openedFromPath;
  protected ModelBuilder modelBuilder;
  protected TrackControl trackControl;
  protected boolean isModelBuilderVisible;
  protected boolean isShiftKeyDown, isControlKeyDown;
  protected ArrayList<TTrack>calibrationTools = new ArrayList<TTrack>();
  protected Set<TTrack>visibleTools = new HashSet<TTrack>();
  protected String author, contact;
  protected AutoTracker autoTracker;
  protected DerivativeAlgorithmDialog algorithmDialog;
  protected AttachmentDialog attachmentDialog;
  protected boolean isAutoRefresh = true;
	protected TreeSet<String> supplementalFilePaths = new TreeSet<String>(); // HTML/PDF URI paths
	protected Map<String, String> pageViewFilePaths = new HashMap<String, String>();
  protected StepSet selectedSteps = new StepSet(this);
  protected boolean hideDescriptionWhenLoaded;
  protected PropertyChangeListener massParamListener, massChangeListener;

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
    popup = new JPopupMenu() {
  		public void setVisible(boolean vis) {
  			super.setVisible(vis);
  			if (!vis) zoomBox.hide();
  		}
  	};
    zoomBox.setShowUndraggedBox(false);
    // remove the interactive panel mouse controller
    removeMouseListener(mouseController);
    removeMouseMotionListener(mouseController);
    // create and add a new mouse controller for tracker
    mouseController = new TMouseController();
    addMouseListener(mouseController);
    addMouseMotionListener(mouseController);
    badNameLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    Box box = Box.createVerticalBox();
    noData.add(box);
    Font font = new JTextField().getFont();
    for (int i = 0; i < 2; i++) {
    	noDataLabels[i] = new JLabel();
    	noDataLabels[i].setFont(font);
    	noDataLabels[i].setAlignmentX(0.5f);
    	box.add(noDataLabels[i]);
    }
  	noData.setOpaque(false);
    player.setInspectorButtonVisible(false);
    player.addPropertyChangeListener("stepbutton", this); //$NON-NLS-1$
    player.addPropertyChangeListener("backbutton", this); //$NON-NLS-1$
    player.addPropertyChangeListener("inframe", this); //$NON-NLS-1$
    player.addPropertyChangeListener("outframe", this); //$NON-NLS-1$
    player.addPropertyChangeListener("slider", this); //$NON-NLS-1$
    player.addPropertyChangeListener("playing", this); //$NON-NLS-1$

    massParamListener = new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent e) {
		  	if ("m".equals(e.getOldValue())) { //$NON-NLS-1$
		  		ParamEditor paramEditor = (ParamEditor)e.getSource();
		  		Parameter param = (Parameter)paramEditor.getObject("m"); //$NON-NLS-1$
		  		FunctionPanel panel = paramEditor.getFunctionPanel();		  		
		  		PointMass m = (PointMass)getTrack(panel.getName());
		  		if (m!=null && m.getMass()!=param.getValue()) {
		      	m.setMass(param.getValue());
		      	m.massField.setValue(m.getMass());
		      }
		  	}
		  }
		};
    massChangeListener = new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent e) {
		  	PointMass pm = (PointMass)e.getSource();
		  	FunctionPanel panel = dataBuilder.getPanel(pm.getName());
		  	if (panel==null) return;
		  	ParamEditor paramEditor = panel.getParamEditor();
		  	Parameter param = (Parameter)paramEditor.getObject("m"); //$NON-NLS-1$
		  	double newMass = (Double)e.getNewValue();
		    if (newMass != param.getValue()) {
		    	paramEditor.setExpression("m", String.valueOf(newMass), false); //$NON-NLS-1$
		  	}
		  }
		};
    
    configure();
  }

  /**
   * Overrides VideoPanel setVideo method.
   *
   * @param newVideo the video
   */
  public void setVideo(Video newVideo) {
  	XMLControl state = null;
  	boolean undoable = true;
  	Video oldVideo = getVideo();
  	if (newVideo!=oldVideo && oldVideo instanceof ImageVideo) {
  		ImageVideo vid = (ImageVideo)getVideo();
  		vid.saveInvalidImages();
  		undoable = vid.isFileBased();
  	}
  	if (newVideo!=oldVideo && undoable) {
  		state = new XMLControlElement(getPlayer().getVideoClip());
  	}  	
  	if (newVideo!=oldVideo && oldVideo!=null) {
  		// clear filters from old video
  		TActions.getAction("clearFilters", this).actionPerformed(null); //$NON-NLS-1$
  	}
    super.setVideo(newVideo, true); // play all steps by default
    if (state != null) {
  		state = new XMLControlElement(state.toXML());
  		Undo.postVideoReplace(this, state);
    }
    TMat mat = getMat();
    if (mat != null) mat.refresh();
    if (modelBuilder!=null) {
    	modelBuilder.refreshSpinners();
    }
    firePropertyChange("image", null, null);  // to tracks & views //$NON-NLS-1$
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
        if (i >= 0) name = name.substring(i + 1);
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
  	if (desc == null) desc = ""; //$NON-NLS-1$
    description = desc;
  }

  /**
   * Gets the model builder.
   *
   * @return the model builder
   */
  public ModelBuilder getModelBuilder() {
  	if (modelBuilder == null) {
//  		// create start and end frame spinners
//  	  Font font = new JSpinner().getFont();
//  	  int n = getPlayer().getVideoClip().getFrameCount()-1;
//  	  FontRenderContext frc = new FontRenderContext(null, false, false);
//  	  String s = String.valueOf(Math.max(n, 200));
//  	  TextLayout layout = new TextLayout(s, font, frc);
//  	  int w = (int)layout.getBounds().getWidth()+4;
//  	  if (n>1000) w+=3;
//  		startFrameLabel = new JLabel();
//  		startFrameLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
//  		endFrameLabel = new JLabel();
//  		endFrameLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 2));
//  		SpinnerNumberModel model = new SpinnerNumberModel(0, 0, n, 1); // init, min, max, step
//  		startFrameSpinner = new ModelFrameSpinner(model);
//  		startFrameSpinner.prefWidth = w;
//  		model = new SpinnerNumberModel(n, 0, n, 1); // init, min, max, step
//  		endFrameSpinner = new ModelFrameSpinner(model);
//  		endFrameSpinner.prefWidth = w;
//  		
//  		// create booster label and dropdown
//      boosterLabel = new JLabel();
//      boosterLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 2));
//      boosterDropdown = new JComboBox();
//      boosterDropdown.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
//      boosterDropdown.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent e) {
//        	if (!boosterDropdown.isEnabled()) return;
//      	  FunctionPanel panel = modelBuilder.getSelectedPanel();
//      	  if (panel!=null) {
//      	  	ParticleModel part = ((ModelFunctionPanel)panel).model;
//      	  	if (!(part instanceof DynamicParticle)) return;
//      	  	DynamicParticle model = (DynamicParticle)part;
//      	  	
//      	  	Object item = boosterDropdown.getSelectedItem();
//	          if(item!=null) {
//	          	Object[] array = (Object[])item;
//	          	PointMass target = (PointMass)array[1]; // null if "none" selected
//		      		model.setBooster(target);
//		      		if (target!=null) {
//			      		Step step = getSelectedStep();
//			      		if (step!=null && step instanceof PositionStep) {
//			      			PointMass pm = (PointMass)((PositionStep)step).track;
//			      			if (pm==target) {
//			      				model.setStartFrame(step.getFrameNumber());
//			      			}
//			      		}
//		      		}
//	          }
//      	  }
//        }
//      });
//      
  		// create and size model builder
  		modelBuilder = new ModelBuilder(this);  			
  		modelBuilder.setFontLevel(FontSizer.getLevel());
  		modelBuilder.refreshLayout();
			modelBuilder.addPropertyChangeListener("panel", this); //$NON-NLS-1$
			// show model builder
			try {
				Point p = getLocationOnScreen();
				TFrame frame = getTFrame();
				if (frame != null) {
					MainTView view = frame.getMainView(this);
					p = view.getLocationOnScreen();
				}
				modelBuilder.setLocation(p.x+160, p.y);
			}
			catch(Exception ex) {/** empty block */}
  	}
  	return modelBuilder;
  }
  
  /**
   * Adds the specified rectangle to the dirty region. The dirty region
   * is repainted when repaintDirtyRegion is called. A null dirtyRect
   * argument is ignored.
   *
   * @param dirtyRect the dirty rectangle
   */
  public void addDirtyRegion(Rectangle dirtyRect) {
    if (dirty == null) dirty = dirtyRect;
    else if (dirtyRect != null) dirty.add(dirtyRect);
  }

  /**
   * Repaints the dirty region.
   */
  public void repaintDirtyRegion() {
    if (dirty != null) {
    	synchronized(dirty) {
	      dirty.grow(2, 2);
	      repaint(dirty);
	    }
      dirty = null;
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
    ArrayList<TTrack> tracks = getTracks();
    tracks.remove(getAxes());
    tracks.removeAll(calibrationTools);
    ArrayList<PerspectiveTrack> list = getDrawables(PerspectiveTrack.class);
    tracks.removeAll(list);
    // remove child ParticleDataTracks
    for (ParticleDataTrack next: getDrawables(ParticleDataTrack.class)) {
    	if (next.getLeader()!=next) {
    		tracks.remove(next);
    	}
    }
    return tracks;
  }

  /**
   * Gets the list of TTracks to save with this panel.
   *
   * @return a list of tracks to save
   */
  public ArrayList<TTrack> getTracksToSave() {
    ArrayList<TTrack> tracks = getTracks();
    // remove child ParticleDataTracks
    for (ParticleDataTrack next: getDrawables(ParticleDataTrack.class)) {
    	if (next.getLeader()!=next) {
    		tracks.remove(next);
    	}
    }
    return tracks;
  }

  /**
   * Gets the first track with the specified name
   *
   * @param name the name of the track
   * @return the track
   */
  public TTrack getTrack(String name) {
    for (TTrack track: getTracks()) {
      if (track.getName().equals(name)) return track;
      if (track.getName("track").equals(name)) return track; //$NON-NLS-1$
    }
    return null;
  }

  /**
   * Adds a track.
   *
   * @param track the track to add
   */
  public synchronized void addTrack(TTrack track) {
  	if (track == null) return;
    TTrack.activeTracks.put(track.getID(), track);
    // set trackerPanel property if not yet set
    if (track.trackerPanel == null) {
      track.setTrackerPanel(this);
    }
    boolean showTrackControl = true;
    // set angle format of the track
    if (getTFrame()!=null)
    	track.setAnglesInRadians(getTFrame().anglesInRadians);
    // special case: axes
    if (track instanceof CoordAxes) {
    	showTrackControl = false;
      if (getAxes()!=null) 
      	removeDrawable(getAxes()); // only one axes at a time
      super.addDrawable(track);
      moveToBack(track);
      WorldGrid grid = getGrid();
      if (grid != null) {
        moveToBack(grid); // put grid behind axes
      }
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
    // special case: calibration tape or stick
    else if (track instanceof TapeMeasure 
    		&& !((TapeMeasure)track).isReadOnly()) {
    	showTrackControl = false;
      calibrationTools.add(track);
      visibleTools.add(track);
    	super.addDrawable(track);
    }
    // special case: offset origin or calibration points
    else if (track instanceof OffsetOrigin
    		|| track instanceof Calibration) { 
    	showTrackControl = false;
      calibrationTools.add(track);
      visibleTools.add(track);
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
    	final ParticleDataTrack dt = (ParticleDataTrack)track;
    	if (dt.allPoints().size()>1) {
	    	Runnable runner = new Runnable() {
	    		public void run() {
	      		for (ParticleDataTrack child: dt.allPoints()) {
	      			if (child==dt) continue;
	      			addTrack(child);
	      		}
		  			TFrame frame = getTFrame();
		  			if (TrackerPanel.this.isShowing()) {
			        TView[][] views = frame.getTViews(TrackerPanel.this);
			        for (TView[] next: views) {
			        	for  (TView view: next) {
			        		if (view instanceof TrackChooserTView) {
			        			((TrackChooserTView)view).setSelectedTrack(dt);
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
    addPropertyChangeListener(track); // track listens for all properties
    track.addPropertyChangeListener("step", this); //$NON-NLS-1$
    track.addPropertyChangeListener("steps", this); //$NON-NLS-1$
    track.addPropertyChangeListener("name", this); //$NON-NLS-1$
    track.addPropertyChangeListener("mass", this); //$NON-NLS-1$
    track.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
    track.addPropertyChangeListener("model_start", this); //$NON-NLS-1$
    track.addPropertyChangeListener("model_end", this); //$NON-NLS-1$
    // update track control and dataTool
    if (trackControl!=null && trackControl.isVisible()) trackControl.refresh();
    if (getDataBuilder() != null && !getSystemDrawables().contains(track)) {   	
    	FunctionPanel panel = createFunctionPanel(track);
    	dataBuilder.addPanel(track.getName(), panel);
    	dataBuilder.setSelectedPanel(track.getName());  	
    }
    // set length of coord system before firing property change (speeds loading up of very long tracks)
    int len = track.getSteps().length;
    len = Math.max(len, getCoords().getLength());
    getCoords().setLength(len);
    
    // set font level
    track.setFontLevel(FontSizer.getLevel());
    
    // notify views
    firePropertyChange("track", null, track); // to views //$NON-NLS-1$
    changed = true;
    if (showTrackControl && getTFrame()!=null && this.isShowing()) {
    	TrackControl.getControl(this).setVisible(true);
    }
    
    // select new track in autotracker
    if (autoTracker!=null && track!=getAxes()) {
    	autoTracker.setTrack(track);
    }
  }
  
  protected FunctionPanel createFunctionPanel(TTrack track) {
  	DatasetManager data = track.getData(this);
    FunctionPanel panel = new DataFunctionPanel(data);
  	panel.setIcon(track.getIcon(21, 16, "point")); //$NON-NLS-1$
  	Class<?> type = track.getClass();
  	if (PointMass.class.isAssignableFrom(type))
  		panel.setDescription(PointMass.class.getName());
  	else if (Vector.class.isAssignableFrom(type))
  		panel.setDescription(Vector.class.getName());
  	else if (RGBRegion.class.isAssignableFrom(type))
  		panel.setDescription(RGBRegion.class.getName());
  	else if (LineProfile.class.isAssignableFrom(type))
  		panel.setDescription(LineProfile.class.getName());
  	else panel.setDescription(type.getName());
    final ParamEditor paramEditor = panel.getParamEditor();
    if (track instanceof PointMass) {
    	PointMass pm = (PointMass)track;
	  	Parameter param = (Parameter)paramEditor.getObject("m"); //$NON-NLS-1$
	  	if (param==null) {
	  		param = new Parameter("m", String.valueOf(pm.getMass())); //$NON-NLS-1$
	  		param.setDescription(TrackerRes.getString("ParticleModel.Parameter.Mass.Description")); //$NON-NLS-1$
	      paramEditor.addObject(param, false);
	  	}
  		param.setNameEditable(false); // mass name not editable
  		paramEditor.addPropertyChangeListener("edit", massParamListener); //$NON-NLS-1$
  		pm.addPropertyChangeListener("mass", massChangeListener); //$NON-NLS-1$
    }
    return panel;
  }

  /**
   * Removes a track.
   *
   * @param track the track to remove
   */
  public synchronized void removeTrack(TTrack track) {
  	if (!getDrawables(track.getClass()).contains(track)) return;
    removePropertyChangeListener(track);
    track.removePropertyChangeListener("step", this); //$NON-NLS-1$
    track.removePropertyChangeListener("steps", this); //$NON-NLS-1$
    track.removePropertyChangeListener("name", this); //$NON-NLS-1$
    track.removePropertyChangeListener("mass", this); //$NON-NLS-1$
    track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
    track.removePropertyChangeListener("model_start", this); //$NON-NLS-1$
    track.removePropertyChangeListener("model_end", this); //$NON-NLS-1$
  	getTFrame().removePropertyChangeListener("tab", track); //$NON-NLS-1$
    super.removeDrawable(track);
    if (dataBuilder != null) dataBuilder.removePanel(track.getName());
//    if (modelBuilder != null) modelBuilder.removePanel(track.getName());
    if (getSelectedTrack()==track)
    	setSelectedTrack(null);
    // notify views and other listeners
    firePropertyChange("track", track, null); //$NON-NLS-1$
    TTrack.activeTracks.remove(track.getID());
    changed = true;
  }

  /**
   * Determines if the specified track is in this tracker panel.
   *
   * @param track the track to look for
   * @return <code>true</code> if this contains the track
   */
  public boolean containsTrack(TTrack track) {
    for (TTrack next: getTracks()) {
      if (track == next) return true;
    }
    return false;
  }

  /**
   * Erases all tracks in this tracker panel.
   */
  public void eraseAll() {
    for (TTrack track: getTracks()) {
      track.erase();
    }
  }

  /**
   * Gives the user an opportunity to save this to a trk file if changed.
   *
   * @return <code>false</code> if the user cancels, otherwise <code>true</code>
   */
  public boolean save() {
    if (!changed) return true;
    if (org.opensourcephysics.display.OSPRuntime.applet != null) return true;
    String name = getTitle();
    // eliminate extension if no data file
    if (getDataFile() == null) {
      int i = name.lastIndexOf('.');
      if (i > 0) {
        name = name.substring(0, i);
      }
    }
    int i = JOptionPane.showConfirmDialog(this.getTopLevelAncestor(),
                                          TrackerRes.getString("TrackerPanel.Dialog.SaveChanges.Message") + " \"" + name + "\"?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                          TrackerRes.getString("TrackerPanel.Dialog.SaveChanges.Title"), //$NON-NLS-1$
                                          JOptionPane.YES_NO_CANCEL_OPTION,
                                          JOptionPane.QUESTION_MESSAGE);
    if (i == JOptionPane.YES_OPTION) {
    	restoreViews();
      File file = VideoIO.save(getDataFile(), this);
      if (file==null) return false;
    }
    else if (i == JOptionPane.CLOSED_OPTION || i == JOptionPane.CANCEL_OPTION) {
      return false;
    }
    changed = false;
    return true;
  }

  /**
   * Overrides VideoPanel getDrawables method.
   *
   * @return a list of Drawable objects
   */
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
    // show noData message if panel is empty
    if (getVideo() == null && getUserTracks().isEmpty()) {
    	isEmpty = true;
    	if (this instanceof WorldTView) {
	    	noDataLabels[0].setText(TrackerRes.getString("WorldTView.Label.NoData")); //$NON-NLS-1$
	    	noDataLabels[1].setText(null);    		
    	}
    	else {
      	noDataLabels[0].setText(TrackerRes.getString("TrackerPanel.Message.NoData0")); //$NON-NLS-1$
      	noDataLabels[1].setText(TrackerRes.getString("TrackerPanel.Message.NoData1")); //$NON-NLS-1$
    	}
      add(noData, BorderLayout.NORTH);
    }
    else {
    	isEmpty = false;
    	remove(noData);
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
   	for (TTrack next: calibrationTools) {
   		list.add(next);
  	}
    return list;
  }

  /**
   * Overrides VideoPanel addDrawable method.
   *
   * @param drawable the drawable object
   */
  public synchronized void addDrawable(Drawable drawable) {
    if (drawable instanceof TTrack) {
    	addTrack((TTrack)drawable);
    }
    else {
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
      synchronized(drawableList) {
	      drawableList.remove(drawable);
	      if (drawable instanceof TMat) // put mat at back
	      	drawableList.add(0, drawable);
	      else {
	      	int index = getMat() == null? 0: 1; // put in front of mat, if any
	      	if (getVideo() != null) index++; // put in front of video, if any
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
  public synchronized void removeDrawable(Drawable drawable) {
    if (drawable instanceof TTrack) removeTrack((TTrack)drawable);
    else super.removeDrawable(drawable);
  }


  /**
   * Overrides VideoPanel removeObjectsOfClass method.
   *
   * @param c the class to remove
   */
  public synchronized <T extends Drawable> void removeObjectsOfClass(Class<T> c) {
    if (TTrack.class.isAssignableFrom(c)) { // objects are TTracks
      // remove propertyChangeListeners
    	ArrayList<T> removed = getObjectOfClass(c);
      Iterator<T> it = removed.iterator();
      while(it.hasNext()) {
        TTrack track = (TTrack)it.next();
        removePropertyChangeListener(track);
        track.removePropertyChangeListener("step", this); //$NON-NLS-1$
        track.removePropertyChangeListener("steps", this); //$NON-NLS-1$
        track.removePropertyChangeListener("name", this); //$NON-NLS-1$
        track.removePropertyChangeListener("mass", this); //$NON-NLS-1$
        track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
        track.removePropertyChangeListener("model_start", this); //$NON-NLS-1$
        track.removePropertyChangeListener("model_end", this); //$NON-NLS-1$
      	getTFrame().removePropertyChangeListener("tab", track); //$NON-NLS-1$
      }
      super.removeObjectsOfClass(c);
      // notify views
      for (Object next: removed) {
      	TTrack track = (TTrack)next;
	      firePropertyChange("track", track, null); //$NON-NLS-1$
      }
      changed = true;
    }
    else super.removeObjectsOfClass(c);
  }

  /**
   * Overrides VideoPanel clear method.
   */
  public synchronized void clear() {
  	setSelectedTrack(null);
  	selectedPoint = null;
  	ArrayList<TTrack> tracks = getTracks();
    for (TTrack track: tracks) {
      removePropertyChangeListener(track);
      track.removePropertyChangeListener("step", this); //$NON-NLS-1$
      track.removePropertyChangeListener("steps", this); //$NON-NLS-1$
      track.removePropertyChangeListener("name", this); //$NON-NLS-1$
      track.removePropertyChangeListener("mass", this); //$NON-NLS-1$
      track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      track.removePropertyChangeListener("model_start", this); //$NON-NLS-1$
      track.removePropertyChangeListener("model_end", this); //$NON-NLS-1$
    	getTFrame().removePropertyChangeListener("tab", track); //$NON-NLS-1$

      // handle case when track is the origin of current reference frame
    	ImageCoordSystem coords = getCoords();
      if (coords instanceof ReferenceFrame && 
      				((ReferenceFrame)coords).getOriginTrack() == track) {
        // set coords to underlying coords
        coords = ( (ReferenceFrame) coords).getCoords();
      	setCoords(coords);
      }    	
    }
    TMat mat = getMat();
    if (mat!=null) {
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
    firePropertyChange("clear", null, null); //$NON-NLS-1$
    // remove tracks from TTrack.activeTracks
    for (TTrack track: tracks) {
	    TTrack.activeTracks.remove(track.getID());   	
    }
    changed = true;
  }

  /**
   * Clears all tracks.
   */
  public synchronized void clearTracks() {
  	ArrayList<TTrack> removed = getTracks();
    // get background drawables to replace after clearing
    ArrayList<Drawable> keepers = getSystemDrawables();
    clear();
    // replace keepers
    for (Drawable drawable: keepers) {
    	if (drawable instanceof TMat) {
    		((TMat)drawable).setTrackerPanel(this);
    	}
      addDrawable(drawable);
      removed.remove(drawable);
    }
    for (TTrack track: removed) {
   	 track.dispose();
    }
  }

  /**
   * Overrides VideoPanel setCoords method.
   *
   * @param _coords the new image coordinate system
   */
  public void setCoords(ImageCoordSystem _coords) {
    if (_coords == null || _coords == coords) return;
    if (video == null) {
      coords.removePropertyChangeListener(this);
      coords = _coords;
      coords.addPropertyChangeListener(this);
      int n = getFrameNumber();
      getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));
      firePropertyChange("coords", null, coords); //$NON-NLS-1$
      firePropertyChange("transform", null, null); //$NON-NLS-1$
    }
    else video.setCoords(_coords);
  }
  
  /**
   * Sets the reference frame by name. If the name is null or not found, 
   * the default reference frame is used.
   *
   * @param trackName the name of a point mass
   */
  public void setReferenceFrame(String trackName) {
    PointMass pm = null;
    for (PointMass m: getDrawables(PointMass.class)) {
      if (m.getName().equals(trackName)) {
        pm = m;
        break;
      }
    }
    if (pm != null) {
      ImageCoordSystem coords = getCoords();
      boolean wasRefFrame = coords instanceof ReferenceFrame;
      while (coords instanceof ReferenceFrame) {
        coords = ( (ReferenceFrame) coords).getCoords();
      }
      setCoords(new ReferenceFrame(coords, pm));
      // special case: if pm is a particle model and wasRefFrame is true,
      // refresh steps of pm after setting new ReferenceFrame
      if (pm instanceof ParticleModel && wasRefFrame) {
      	((ParticleModel)pm).lastValidFrame = -1;
      	((ParticleModel)pm).refreshSteps();
      }      
      setSelectedPoint(null);
      repaint();
    }
    else {
      ImageCoordSystem coords = getCoords();
      if (coords instanceof ReferenceFrame) {
        coords = ( (ReferenceFrame) coords).getCoords();
        setCoords(coords);
        setSelectedPoint(null);
        repaint();
      }
    }

  }

  /**
   * Gets the coordinate axes.
   *
   * @return the CoordAxes
   */
  public CoordAxes getAxes() {
  	ArrayList<CoordAxes> list = getDrawables(CoordAxes.class);
    if (!list.isEmpty()) return list.get(0);
    return null;
  }

  /**
   * Gets the mat.
   *
   * @return the first TMat in the drawable list
   */
  public TMat getMat() {
    ArrayList<TMat> list = getDrawables(TMat.class);
    if (!list.isEmpty()) return list.get(0);
    return null;
  }

  /**
   * Gets the grid.
   *
   * @return the first Grid in the drawable list
   */
  public WorldGrid getGrid() {
    ArrayList<WorldGrid> list = getDrawables(WorldGrid.class);
    if (!list.isEmpty()) return list.get(0);
    return null;
  }

  /**
   * Gets the origin snap point.
   *
   * @return the snap point
   */
  public TPoint getSnapPoint() {
  	if (snapPoint == null) snapPoint = new TPoint();
    return snapPoint;
  }

  /**
   * Sets the selected track.
   *
   * @param track the track to select
   */
  public void setSelectedTrack(TTrack track) {
    if (selectedTrack == track) return;
    if (track!=null 
    		&& track instanceof ParticleModel
    		&& ((ParticleModel)track).refreshing)
    	return;
    TTrack prevTrack = selectedTrack;
    selectedTrack = track;
    if (Tracker.showHints && track != null) setMessage(track.getMessage());
    else setMessage(""); //$NON-NLS-1$
    firePropertyChange("selectedtrack", prevTrack, track); //$NON-NLS-1$
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
   * Sets the selected point. Also sets the selected step, track, and selecting panel.
   *
   * @param point the point to receive actions
   */
  public void setSelectedPoint(TPoint point) {
    if (point == selectedPoint && point == null) return;
    Tracker.logTime("set selected point"); //$NON-NLS-1$
    TPoint prevPoint = selectedPoint;
    if (prevPoint!=null) {
    	prevPoint.setAdjusting(false);
    }
    selectedPoint = point;
    // determine if selected steps or previous point has changed 
    boolean stepsChanged = !selectedSteps.isEmpty() && selectedSteps.isChanged();
    // determine if newly selected step is in selectedSteps
    if (selectedSteps.size()>1) {
      boolean newStepSelected = false;
      if (point!=null) {
    		// find associated step
    		Step step = null;
        for (TTrack track: getTracks()) {
        	step = track.getStep(point, this);
        	if (step != null) {
        		newStepSelected = selectedSteps.contains(step);
        		break;
        	}
        }
      }
      if (newStepSelected) {
        firePropertyChange("selectedpoint", prevPoint, point); //$NON-NLS-1$
      	selectedSteps.isModified = false;
        return;
      }
    }
    boolean prevPointChanged = currentState!=null && prevPoint != null && prevPoint != point && prevPoint != newlyMarkedPoint
    				&& (prevPoint.x != pointState.x || prevPoint.y != pointState.y);
    if (selectedPoint==null) {
    	newlyMarkedPoint = null;
    }
    // post undo edit if selectedSteps or previous point has changed 
    if (stepsChanged || prevPointChanged) {
    	boolean trackEdit = false;
    	boolean coordsEdit = false;
    	if (prevPointChanged) {
	    	trackEdit = prevPoint.isTrackEditTrigger() && getSelectedTrack() != null;
	    	coordsEdit = prevPoint.isCoordsEditTrigger();
    	}
    	else { // steps have changed 
    		trackEdit = selectedSteps.getTrack()!=null;
    	}
    	if (trackEdit && coordsEdit) {
    		Undo.postTrackAndCoordsEdit(getSelectedTrack(), currentState, currentCoords);    		
    	}
    	else if (trackEdit) {
    		if (stepsChanged) {
      		if (!selectedSteps.isModified) {
      			selectedSteps.clear(); // posts undoable edit if changed
      		}
    		}
    		else {
    			Undo.postTrackEdit(getSelectedTrack(), currentState);
    		}
    	}
    	else if (coordsEdit) {
    		Undo.postCoordsEdit(this, currentState);
    	}
    	else if (prevPoint!=null && prevPoint.isStepEditTrigger()) {
    		Undo.postStepEdit(selectedStep, currentState);
    	}
    	else if (prevPoint instanceof LineProfileStep.LineEnd) {
    		prevPoint.setTrackEditTrigger(true);
    	}
    }
    if (selectedStep != null) selectedStep.repaint();
    if (point == null) {
      selectedStep = null;
      selectingPanel = null;
      currentState = null;
      currentCoords = null;
    }
    else {  // find track and step (if any) associated with selected point
      TTrack track = null;
      Step step = null;
      Iterator<TTrack> it = getTracks().iterator();
      while(it.hasNext()) {
        track = it.next();
        step = track.getStep(point, this);
        if (step != null) break;
      }
      selectedStep = step;
      if (step == null) { // non-track TPoint was selected
        boolean ignore = autoTracker!=null 
        	&& autoTracker.getWizard().isVisible()
        	&& (point instanceof AutoTracker.Corner
        			|| point instanceof AutoTracker.Handle
        			|| point instanceof AutoTracker.Target);
        if (!ignore) setSelectedTrack(null);
      }
      else { // TPoint is associated with a step and track
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
	        }
	        else if (trackEdit) {
	        	currentState = new XMLControlElement(track);
	      		if (!selectedSteps.contains(step) && !selectedSteps.isModified) {
	        		selectedSteps.clear();
	        	}
	        }
	        else if (coordsEdit) {
	        	currentState = new XMLControlElement(getCoords());
	        }
	        else if (point.isStepEditTrigger()) {
	        	currentState = new XMLControlElement(step);
	        }
        }
      }
      selectingPanel = this;
      requestFocusInWindow();
    }
  	selectedSteps.isModified = false;
    firePropertyChange("selectedpoint", prevPoint, point); //$NON-NLS-1$
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
  	if (Double.isNaN(magnification)) return;
  	if (magnification==0) return;
    double prevZoom = getMagnification();
    Dimension prevSize = getPreferredSize();
		Point p1 = new TPoint(0, 0).getScreenPosition(this);
    if (prevSize.width==1 && prevSize.height==1) { // zoomed to fit
    	double w = getImageWidth();
    	double h = getImageHeight();
    	Point p2 = new TPoint(w, h).getScreenPosition(this);
    	prevSize.width = p2.x-p1.x;
    	prevSize.height =  p2.y-p1.y;
    }
  	if (magnification < 0) {
      setPreferredSize(new Dimension(1, 1));
  	}
  	else {
      zoom = Math.max(magnification, MIN_ZOOM);
      zoom = Math.min(zoom, MAX_ZOOM);
      int w = (int)(imageWidth*zoom);
      int h = (int)(imageHeight*zoom);
      Dimension size = new Dimension(w, h);
      setPreferredSize(size);
  	}
    firePropertyChange("magnification", prevZoom, getMagnification()); //$NON-NLS-1$
  	// scroll and revalidate
  	MainTView view = getTFrame()==null? null: getTFrame().getMainView(this);
  	if (view != null) {
  		view.scrollPane.revalidate();
  		view.scrollToZoomCenter(getPreferredSize(), prevSize, p1);
      eraseAll();
      repaint();
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
			return Math.min(size.width/w, size.height/h);
		}
    return zoom;
  }

  /**
   * Sets the image width in image units. Overrides VideoPanel method.
   *
   * @param w the width
   */
  public void setImageWidth(double w) {
    setImageSize(w, getImageHeight());
  }

  /**
   * Sets the image height in image units. Overrides VideoPanel method.
   *
   * @param h the height
   */
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
    if (mat != null) mat.refresh();
    if (getPreferredSize().width > 10) {
      setMagnification(getMagnification());
    }
    eraseAll();
    repaint();
    firePropertyChange("size", null, null); //$NON-NLS-1$
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
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  /**
   * Gets the scrollable unit increment.
   *
   * @param visibleRect the rectangle currently visible in the scrollpane
   * @param orientation the orientation of the scrollbar
   * @param direction the direction of movement of the scrollbar
   * @return the scrollable unit increment
   */
  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction) {
    return 20;
  }

  /**
   * Gets the scrollable block increment.
   *
   * @param visibleRect the rectangle currently visible in the scrollpane
   * @param orientation the orientation of the scrollbar
   * @param direction the direction of movement of the scrollbar
   * @return the scrollable block increment
   */
  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction) {
    int unitIncrement = getScrollableUnitIncrement(
                        visibleRect, orientation, direction);
    if (orientation == SwingConstants.HORIZONTAL)
        return visibleRect.width - unitIncrement;
    return visibleRect.height - unitIncrement;
  }

  /**
   * Gets whether this tracks the viewport width in a scrollpane.
   *
   * @return <code>true</code> if this tracks the width
   */
  public boolean getScrollableTracksViewportWidth() {
    if (scrollPane == null) return true;
    Dimension panelDim = getPreferredSize();
    Rectangle viewRect = scrollPane.getViewport().getViewRect();
    return viewRect.width > panelDim.width;
  }

  /**
   * Gets whether this tracks the viewport height.
   *
   * @return <code>true</code> if this tracks the height
   */
  public boolean getScrollableTracksViewportHeight() {
    if (scrollPane == null) return true;
    Dimension panelDim = getPreferredSize();
    Rectangle viewRect = scrollPane.getViewport().getViewRect();
    return viewRect.height > panelDim.height;
  }

  /**
   * Returns true if mouse coordinates are displayed. Overrides VideoPanel
   * method to report false if a point is selected.
   *
   * @return <code>true</code> if mouse coordinates are displayed
   */
  public boolean isShowCoordinates() {
    return showCoordinates && getSelectedPoint() == null;
  }

  /**
   * Shows a message in BR corner. Overrides DrawingPanel method.
   *
   * @param msg the message
   */
  public void setMessage(String msg) {
  	if (!OSPRuntime.isMac()) super.setMessage(msg);
  }
  
  /**
   * Imports Data from a data string (delimited fields) into a DataTrack.
   * The data string must be parsable by DataTool. If the string is a path,
   * an attempt is made to get the data string with ResourceLoader. 
   * 
   * Source object (model) may be String path, JPanel controlPanel, Tool tool, etc
   * 
   * @param dataString delimited fields parsable by DataTool, or a path to a Resource
   * @param source the data source (may be null)
   * @return the DataTrack with the Data (may return null)
   */
  protected DataTrack importData(String dataString, Object source) {
  	if (dataString==null) {
  		// inform user
			JOptionPane.showMessageDialog(frame, 
					TrackerRes.getString("TrackerPanel.Dialog.NoData.Message"), //$NON-NLS-1$
					TrackerRes.getString("TrackerPanel.Dialog.NoData.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
  		return null;
  	}
  	// if dataString is parsable data, parse and import it
		DatasetManager data = DataTool.parseData(dataString, null);
		if (data!=null) {
      DataTrack dt = importData(data, source);
      if (dt instanceof ParticleDataTrack) {
      	ParticleDataTrack pdt = (ParticleDataTrack)dt;
      	pdt.prevDataString = dataString;
      }
      return dt;
    }
  	
  	// assume dataString is a resource path, read the resource and call this again with path as source
		String path = dataString;
  	return importData(ResourceLoader.getString(path), path);
  }
  
  /**
   * Imports Data from a source into a DataTrack. 
   * Data must include "x" and "y" columns, may include "t". 
   * DataTrack is the first one found that matches the Data name or ID.
   * If none found, a new DataTrack is created.
   * Source object (model) may be String path, JPanel controlPanel, Tool tool, etc
   * 
   * @param data the Data to import
   * @param source the data source (may be null)
   * @return the DataTrack with the Data (may return null)
   */
  @Override
  public DataTrack importData(Data data, Object source) {
  	if (data==null) return null;
  	
  	// find DataTrack with matching name or ID
  	ParticleDataTrack dataTrack = ParticleDataTrack.getTrackForData(data, this);
  	
  	// load data into DataTrack
  	try {
	  	// create a new DataTrack if none exists
    	if (dataTrack==null) {
    		dataTrack = new ParticleDataTrack(data, source);
				int i = getDrawables(PointMass.class).size();
				dataTrack.setColorToDefault(i);
				addTrack(dataTrack);
				setSelectedPoint(null);
				setSelectedTrack(dataTrack);
				dataTrack.getDataClip().setClipLength(-1); // sets clip length to data length
				VideoClip videoClip = getPlayer().getVideoClip();
				dataTrack.setStartFrame(videoClip.getStartFrameNumber());
//				dataTrack.getInspector().setVisible(true);
				dataTrack.firePropertyChange("data", null, null); //$NON-NLS-1$
    	}
    	else {
      	// set data for existing DataTrack
  			dataTrack.setData(data);
    	}
		} catch (Exception e) {
			// inform user
			JOptionPane.showMessageDialog(frame, 
					TrackerRes.getString("TrackerPanel.Dialog.Exception.Message")+":" //$NON-NLS-1$ //$NON-NLS-2$
					+e.getClass().getSimpleName()+": "+e.getMessage(), //$NON-NLS-1$
					TrackerRes.getString("TrackerPanel.Dialog.Exception.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			OSPLog.warning(e.getClass().getSimpleName()+": "+e.getMessage()); //$NON-NLS-1$
			dataTrack = null;
		}
		return dataTrack;
  }

  /**
   * Refreshes all data in tracks and views.
   */
  protected void refreshTrackData() {
  	// turn on autorefresh
  	boolean auto = isAutoRefresh;
  	isAutoRefresh = true;
    firePropertyChange("transform", null, null); //$NON-NLS-1$
    // restore autorefresh
  	isAutoRefresh = auto;
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
  public JPopupMenu getPopupMenu() {
  	MainTView mainView = getTFrame().getMainView(this);
  	return mainView.getPopupMenu();
  }

  /**
   * Gets the attachment dialog for attaching measuring tool points to point masses.
   * 
   * @param track a measuing tool
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
    }
    else {
    	attachmentDialog.setFontLevel(FontSizer.getLevel());
    	attachmentDialog.setMeasuringTool(track);
    }
    return attachmentDialog;
  }

  /**
   * Gets the data builder for defining custom data functions.
   * @return the data builder
   */
  protected FunctionTool getDataBuilder() {
  	if (dataBuilder == null) { // create new tool if none exists
  		dataBuilder = new TrackDataBuilder(this);
			dataBuilder.setHelpPath("data_builder_help.html"); //$NON-NLS-1$
  		dataBuilder.addPropertyChangeListener("panel", this); //$NON-NLS-1$
  		dataBuilder.addPropertyChangeListener("function", this); //$NON-NLS-1$
  		dataBuilder.addPropertyChangeListener("visible", this); //$NON-NLS-1$
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
  	if (algorithmDialog==null) {
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
    	}
    	else {
    		frame.notesTextPane.setText(getDescription());
        frame.notesDialog.setName(null);
        String tabName = frame.getTabTitle(frame.getSelectedTab());
        frame.notesDialog.setTitle(TrackerRes.getString("TActions.Dialog.Description.Title") //$NON-NLS-1$
        				+ " \"" + tabName + "\""); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    	frame.notesTextPane.setBackground(Color.WHITE);
    	frame.cancelNotesDialogButton.setEnabled(false);
    	frame.closeNotesDialogButton.setEnabled(true);
    	TrackerPanel panel = frame.getTrackerPanel(frame.getSelectedTab());
    	frame.displayWhenLoadedCheckbox.setEnabled(panel!=null);
    	if (panel!=null) {
    		frame.displayWhenLoadedCheckbox.setSelected(!panel.hideDescriptionWhenLoaded);
    	}

    	frame.notesTextPane.setEditable(isEnabled("notes.edit")); //$NON-NLS-1$
    }
  }
  
  /**
   * Gets the alphabet index for setting the name letter suffix and color
   * of a track.
   * 
   * @param name the default name with no letter suffix
   * @param connector the string connecting the name and letter
   * @return the index of the first available letter suffix
   */
  protected int getAlphabetIndex(String name, String connector) {
  	for (int i=0; i< alphabet.length(); i++) {
	  	String letter = alphabet.substring(i, i+1);
	  	String proposed = name+connector+letter;
	  	boolean isTaken = false;
	  	for (TTrack track: getTracks()) {
				String nextName = track.getName();
				isTaken = isTaken || proposed.equals(nextName); 
	  	}
	  	if (!isTaken) return i;
  	}
  	return 0;
  }
  
  /**
   * Restores the views to a non-maximized state.
   */
  protected void restoreViews() {
  	// find maximized view and restore
  	Container[] views = getTFrame().getViews(this);
  	for (int i = 0; i<views.length; i++) {
    	if (views[i] instanceof TViewChooser) {
    		TViewChooser chooser = (TViewChooser)views[i];
    		if (chooser.maximized) {
    			chooser.restore();
    			break;
    		}
    	}
    }
  }

  /**
   * Configures this panel.
   */
  protected void configure() {
    coords.removePropertyChangeListener(this);
    coords.addPropertyChangeListener(this);
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
        	if (!isShiftKeyDown) {
        		isShiftKeyDown = true;
	        	boolean marking = setCursorForMarking(true, e);
	          if (selectedTrack!=null && marking!=selectedTrack.isMarking) {
	          	selectedTrack.setMarking(marking);
	          }
        	}
        }
        else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
        	if (!isControlKeyDown) {
        		isControlKeyDown = true;
	        	boolean marking = setCursorForMarking(isShiftKeyDown, e);
	          if (selectedTrack!=null && marking!=selectedTrack.isMarking) {
	          	selectedTrack.setMarking(marking);
	          }
        	}
        }
        else if (e.getKeyCode() == KeyEvent.VK_ENTER
        		&& selectedTrack!=null
        		&& getCursor() == selectedTrack.getMarkingCursor(e)
        		&& getFrameNumber()>0) {
        	int n = getFrameNumber();
        	Step step = selectedTrack.getStep(n-1);
        	if (step!=null) {
        		Step clone = null;
        		if (selectedTrack.getClass()==PointMass.class) {
	        		TPoint p = ((PositionStep)step).getPosition();
	            clone = selectedTrack.createStep(n, p.x, p.y);
        		}
        		else if (selectedTrack.getClass()==Vector.class) {
        			VectorStep s = (VectorStep)step;
	        		TPoint tail = s.getTail();
	        		TPoint tip = s.getTip();
	        		Vector vector = (Vector)selectedTrack;
	        		double dx = tip.x-tail.x;
	        		double dy = tip.y-tail.y;
	            clone = vector.createStep(n, tail.x, tail.y, dx, dy);
        		}
						if (clone!=null && selectedTrack.isAutoAdvance()) {
		          getPlayer().step();
 		          hideMouseBox();
						}
						else {
	            setMouseCursor(Cursor.getDefaultCursor());
	            if (clone!=null) {
		            setSelectedPoint(clone.getDefaultPoint());
		            selectedTrack.repaint(clone);
	            }
						}
        	}
        }
        else handleKeyPress(e);
      }
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      		isShiftKeyDown = false;
        	boolean marking = setCursorForMarking(false, e);
          if (selectedTrack!=null && marking!=selectedTrack.isMarking) {
          	selectedTrack.setMarking(marking);
          }
        }
        else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      		isControlKeyDown = false;
        	boolean marking = setCursorForMarking(isShiftKeyDown, e);
          if (selectedTrack!=null && marking!=selectedTrack.isMarking) {
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
    changed = false;
  }

  /**
   * Sets the cursor to a crosshair when the selected
   * track is marking and is unmarked on the current frame.
   * Also displays hints as a side effect.
   *
   * @param invert true to invert the normal state
   * @param e an input event
   * @return true if marking (ie next mouse click will mark a TPoint)
   */
  protected boolean setCursorForMarking(boolean invert, InputEvent e) {
  	if (getCursor() == Tracker.zoomInCursor
  			|| getCursor() == Tracker.zoomOutCursor) return false;
    boolean markable = false;
    boolean marking = false;
    selectedTrack = getSelectedTrack();
    int n = getFrameNumber();
    if (selectedTrack != null) {
      markable = !(selectedTrack.isStepComplete(n)
                || selectedTrack.isLocked()
                || popup.isVisible());
      marking = markable
                && (selectedTrack.isMarkByDefault() != invert);
    }
    Interactive iad = getTracks().isEmpty() || mouseEvent==null? null: getInteractive();
    if (marking) {
      setMouseCursor(selectedTrack.getMarkingCursor(e));
      if (Tracker.showHints) {
	      if (selectedTrack instanceof PointMass) {
	      	if (selectedTrack.getStep(n)==null)
	      		setMessage(TrackerRes.getString("PointMass.Hint.Marking")); //$NON-NLS-1$
	      	else
	      		setMessage(TrackerRes.getString("PointMass.Remarking.Hint")); //$NON-NLS-1$
	      }
	      else if (selectedTrack instanceof Vector)
	      	if (selectedTrack.getStep(n)==null)
	      		setMessage(TrackerRes.getString("Vector.Hint.Marking")); //$NON-NLS-1$
	      	else
	      		setMessage(TrackerRes.getString("Vector.Remarking.Hint")); //$NON-NLS-1$
	      else if (selectedTrack instanceof LineProfile)
	      	setMessage(TrackerRes.getString("LineProfile.Hint.Marking")); //$NON-NLS-1$
	      else if (selectedTrack instanceof RGBRegion)
	      	setMessage(TrackerRes.getString("RGBRegion.Hint.Marking")); //$NON-NLS-1$
      }
      else setMessage(""); //$NON-NLS-1$
    }
    else if (iad instanceof TPoint) {
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.
          HAND_CURSOR));
      // identify associated track and display its hint
      for (TTrack track: getTracks()) {
      	Step step = track.getStep((TPoint)iad, this);
        if (step != null) {
        	setMessage(track.getMessage());
        	break;
        }
      }
    }
    else {  // no point selected
      setMouseCursor(Cursor.getDefaultCursor());
      // display selected track hint
      if (Tracker.showHints && selectedTrack != null) {
      	setMessage(selectedTrack.getMessage());
      }
      else if (!Tracker.startupHintShown || getVideo() != null 
      		|| !getUserTracks().isEmpty()) {
      	Tracker.startupHintShown = false;
      	if (!Tracker.showHints) setMessage(""); //$NON-NLS-1$
        // show hints
      	else if (getVideo() == null) // no video
    			setMessage(TrackerRes.getString("TrackerPanel.NoVideo.Hint")); //$NON-NLS-1$
    		else if (TToolBar.getToolbar(this).notYetCalibrated) {
        	if (getVideo().getWidth() == 720
        			&& getVideo().getFilterStack().isEmpty()) // DV video format
      			setMessage(TrackerRes.getString("TrackerPanel.DVVideo.Hint")); //$NON-NLS-1$
        	else if (getPlayer().getVideoClip().isDefaultState())
      			setMessage(TrackerRes.getString("TrackerPanel.SetClip.Hint")); //$NON-NLS-1$
        	else setMessage(TrackerRes.getString("TrackerPanel.CalibrateVideo.Hint")); //$NON-NLS-1$
    		}
      	else if (getAxes()!=null && getAxes().notyetShown)
    			setMessage(TrackerRes.getString("TrackerPanel.ShowAxes.Hint")); //$NON-NLS-1$
    		else if (getUserTracks().isEmpty())
    			setMessage(TrackerRes.getString("TrackerPanel.NoTracks.Hint")); //$NON-NLS-1$
      	else setMessage(""); //$NON-NLS-1$
      }
    }
    return marking;
  }

  /**
   * Handles keypress events for selected points.
   *
   * @param e the key event
   */
  protected void handleKeyPress(KeyEvent e) {
    if (e.getKeyCode()== KeyEvent.VK_F1) {
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
    
    if (e.getKeyCode()== KeyEvent.VK_SPACE) {
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
    
    if (e.getKeyCode()== KeyEvent.VK_DELETE) {
      // delete selected steps
      deleteSelectedSteps();      
      if (selectedPoint!=null && selectingPanel==this) {
      	deletePoint(selectedPoint);
      }
      return;
    }
    

    // move selected point(s) when arrow key pressed
    double delta = e.isShiftDown()? 10: 1;
    double dx = 0, dy = 0;
    switch(e.getKeyCode()) {
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

    if (dx == 0 && dy == 0) return;
    selectedSteps.setChanged(true);
    for (Step step: selectedSteps) {
    	TPoint point = step.points[0];
    	if (point==selectedPoint) continue;
	    Point p = point.getScreenPosition(this);
	    p.setLocation(p.x + dx, p.y + dy);
	    point.setScreenPosition(p.x, p.y, this, e);
    }
    if (selectedPoint!=null) {
	    Point p = selectedPoint.getScreenPosition(this);
	    p.setLocation(p.x + dx, p.y + dy);
	    selectedPoint.setScreenPosition(p.x, p.y, this, e);
    }
    // check selected point since setting screen position can deselect it!
    if (selectedPoint != null)
    	selectedPoint.showCoordinates(this);
    else setMessage("", 0);  //$NON-NLS-1$
    if (selectedStep == null) repaint();
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
  	if (enabled == null) enabled = new TreeSet<String>();
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
  	}
  }

  /**
   * Gets the enabled state for the specified key.
   *
   * @param key the string key
   * @return true if enabled
   */
  public boolean isEnabled(String key) {
    if (key == null) return false;
    return getEnabled().contains(key);
  }

  /**
   * Sets the enabled state for the specified key.
   *
   * @param key the string key
   * @param enable true to enable the key
   */
  public void setEnabled(String key, boolean enable) {
    if (key == null) return;
    if (enable) getEnabled().add(key);
    else getEnabled().remove(key);
  }
  
  /**
   * REturns true if any new.trackType is enabled.
   *
   * @return true if enabled
   */
  public boolean isCreateTracksEnabled() {
  	return isEnabled("new.pointMass")  //$NON-NLS-1$
  			|| isEnabled("new.cm")  //$NON-NLS-1$
        || isEnabled("new.vector")  //$NON-NLS-1$
        || isEnabled("new.vectorSum")  //$NON-NLS-1$
				|| isEnabled("new.lineProfile")  //$NON-NLS-1$
				|| isEnabled("new.RGBRegion")  //$NON-NLS-1$
				|| isEnabled("new.tapeMeasure")  //$NON-NLS-1$
				|| isEnabled("new.protractor")  //$NON-NLS-1$
				|| isEnabled("new.circleFitter")  //$NON-NLS-1$
				|| isEnabled("new.analyticParticle")  //$NON-NLS-1$
				|| isEnabled("new.dynamicParticle")  //$NON-NLS-1$
				|| isEnabled("new.dynamicTwoBody")  //$NON-NLS-1$
				|| isEnabled("new.dataTrack");  //$NON-NLS-1$
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" property change "+name); //$NON-NLS-1$
    if (name.equals("size")) super.propertyChange(e); //$NON-NLS-1$
    if (name.equals("step") || name.equals("steps")) { // from tracks/steps //$NON-NLS-1$ //$NON-NLS-2$
      TTrack track = (TTrack)e.getSource();
      track.dataValid = false;
      if (!track.isDependent()) {    // ignore dependent tracks
        changed = true;
      }
      if (track==getSelectedTrack()) {
      	TPoint p = getSelectedPoint();
      	if (p!=null)
      		p.showCoordinates(this);
      }
      repaint();
      if (name.equals("steps")) { //$NON-NLS-1$
		  	TTrackBar.getTrackbar(this).refresh();
      }
    }
    else if (name.equals("mass")) {                    // from point masses //$NON-NLS-1$
      firePropertyChange("mass", null, null);          // to motion control //$NON-NLS-1$
    }
    else if (name.equals("name")) {                    // from tracks //$NON-NLS-1$
      refreshNotesDialog(); 
    }
    else if (name.equals("footprint")) {               // from tracks //$NON-NLS-1$
      Footprint footprint = (Footprint)e.getNewValue();
      if (footprint instanceof ArrowFootprint)
        firePropertyChange("footprint", null, null);   // to track control //$NON-NLS-1$
    }
    else if (name.equals("videoclip")) {               // from videoPlayer //$NON-NLS-1$
      // replace coords and videoclip listeners
      ImageCoordSystem oldCoords = coords;
      coords.removePropertyChangeListener(this);
      super.propertyChange(e);       // replaces video, videoclip listeners, (possibly) coords
      coords.addPropertyChangeListener(this);
      firePropertyChange("coords", oldCoords, coords); // to tracks //$NON-NLS-1$
      firePropertyChange("video", null, null);        // to TMenuBar & views //$NON-NLS-1$
      if (getMat() != null) {
        getMat().isValidMeasure = false;
      }
      if (getVideo() != null) {
        getVideo().setProperty("measure", null); //$NON-NLS-1$
        // if xuggle video, set smooth play per preferences
        VideoType videoType = (VideoType)video.getProperty("video_type"); //$NON-NLS-1$
        if (videoType!=null	&& videoType.getClass().getSimpleName().contains(VideoIO.ENGINE_XUGGLE)) {
          boolean smooth = !Tracker.isXuggleFast;
        	try {
      			String xuggleName = "org.opensourcephysics.media.xuggle.XuggleVideo"; //$NON-NLS-1$
      			Class<?> xuggleClass = Class.forName(xuggleName);
      			Method method = xuggleClass.getMethod("setSmoothPlay", new Class[] {Boolean.class});  //$NON-NLS-1$
      			method.invoke(video, new Object[] {smooth});
      		} catch (Exception ex) {
      		}    
        }
      }
      changed = true;
    }
    else if (name.equals("stepnumber")) {              // from videoPlayer //$NON-NLS-1$
      setSelectedPoint(null);
      if (getVideo() != null && !getVideo().getFilterStack().isEmpty()) {
        Iterator<Filter> it = getVideo().getFilterStack().getFilters().iterator();
        while (it.hasNext()) {
        	Object next = it.next();
        	if (next instanceof SumFilter) {
        		SumFilter f = (SumFilter)next;
        		f.addNextImage();
        	}
        }
      }
      repaint();
      VideoCaptureTool grabber = VideoGrabber.VIDEO_CAPTURE_TOOL;
      if (grabber != null && grabber.isVisible() && grabber.isRecording()) {
      	Runnable runner = new Runnable() {
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

      firePropertyChange("stepnumber", null, e.getNewValue());    // to views //$NON-NLS-1$
    }
    else if (name.equals("coords")) {                  // from video //$NON-NLS-1$
      // replace coords and listeners
      coords.removePropertyChangeListener(this);
      coords  = (ImageCoordSystem)e.getNewValue();
      coords.addPropertyChangeListener(this);
      firePropertyChange("coords", null, coords);       // to tracks //$NON-NLS-1$
      firePropertyChange("transform", null, null);      // to tracks/views //$NON-NLS-1$
    }
    else if (name.equals("image")) {                    // from video //$NON-NLS-1$
      firePropertyChange("image", null, null);          // to tracks/views //$NON-NLS-1$
      
      Video video = getVideo();
      TMenuBar.getMenuBar(this).refreshMatSizes(video);
      repaint();
    }
    else if (name.equals("filterChanged")) {            // from video //$NON-NLS-1$
    	Filter filter = (Filter)e.getNewValue();
  		String prevState = (String)e.getOldValue();
	    XMLControl control = new XMLControlElement(prevState);	
	    Undo.postFilterEdit(this, filter, control);
    }
    else if (name.equals("videoVisible")) {             // from video //$NON-NLS-1$
      firePropertyChange("videoVisible", null, null);   // to views //$NON-NLS-1$
      repaint();
    }
    else if (name.equals("transform")) {                // from coords //$NON-NLS-1$
      changed = true;
      firePropertyChange("transform", null, null);      // to tracks/views //$NON-NLS-1$
    }
    else if (name.equals("locked")) {                   // from coords //$NON-NLS-1$
      firePropertyChange("locked", null, null);         // to tracker frame //$NON-NLS-1$
    }
    else if (name.equals("playing")) {                  // from player //$NON-NLS-1$
      if (!((Boolean) e.getNewValue()).booleanValue()) {
	    	for (ParticleModel next: getDrawables(ParticleModel.class)) {
	    		next.refreshDerivsIfNeeded();
	    	}
      }
    }
    else if (name.equals("startframe") ||               // from videoClip //$NON-NLS-1$
             name.equals("stepsize") ||                 // from videoClip //$NON-NLS-1$
             name.equals("stepcount") ||                // from videoClip //$NON-NLS-1$
             name.equals("starttime") ||                // from videoClip //$NON-NLS-1$
             name.equals("adjusting") ||                // from videoClip //$NON-NLS-1$
             name.equals("frameduration")) {            // from clipControl //$NON-NLS-1$
      changed = true;
  		if (modelBuilder!=null) modelBuilder.refreshSpinners();
      if (getMat() != null) {
        getMat().isValidMeasure = false;
      }
      if (getVideo() != null) {
        getVideo().setProperty("measure", null); //$NON-NLS-1$
      }
      firePropertyChange("data", null, null);           // to views //$NON-NLS-1$
      if (name.equals("stepsize") //$NON-NLS-1$
      		|| name.equals("stepcount") //$NON-NLS-1$
      		|| name.equals("starttime") //$NON-NLS-1$
      		|| name.equals("frameduration") //$NON-NLS-1$
      		|| name.equals("startframe")) //$NON-NLS-1$
        firePropertyChange(name, null, null);     // to pointmass
      else if (name.equals("adjusting")) //$NON-NLS-1$
        firePropertyChange("adjusting", null, e.getNewValue()); // to particle models //$NON-NLS-1$
      if (getSelectedPoint() != null) {
        getSelectedPoint().showCoordinates(this);
        getTFrame().getTrackBar(this).refresh();
      }
      for (TTrack track: getUserTracks())
      	track.erase(this);
      repaint();
    }
    else if (getVideo()==null && name.equals("framecount")) { //$NON-NLS-1$
  		if (modelBuilder!=null) modelBuilder.refreshSpinners();
    }
    else if (name.equals("function")) {  // from DataBuilder //$NON-NLS-1$
    	changed = true;
      firePropertyChange("function", null, e.getNewValue()); // to views //$NON-NLS-1$
    }
    else if (name.equals("panel") && e.getSource() == modelBuilder) { //$NON-NLS-1$
    	FunctionPanel panel = (FunctionPanel)e.getNewValue();
    	if (panel != null) { // new particle model panel added
    		TTrack track = getTrack(panel.getName());
    		if (track != null) {
//    			setSelectedTrack(track);
    			ParticleModel model = (ParticleModel)track;
    			modelBuilder.setSpinnerStartFrame(model.getStartFrame());
      		int end = model.getEndFrame();
      		if (end==Integer.MAX_VALUE) {
      			end = getPlayer().getVideoClip().getLastFrameNumber();
      		}
      		modelBuilder.setSpinnerEndFrame(end);
    		}
    	}
  		modelBuilder.refreshSpinners();
  		String title = TrackerRes.getString("TrackerPanel.ModelBuilder.Title"); //$NON-NLS-1$  
    	panel = modelBuilder.getSelectedPanel();
    	if (panel!=null) {
    		TTrack track = getTrack(panel.getName());
    		if (track != null) {
    			String type = track.getClass().getSimpleName();
    			title += ": "+TrackerRes.getString(type+".Builder.Title"); //$NON-NLS-1$ //$NON-NLS-2$
    		}
    	}
  		modelBuilder.setTitle(title);
    }
    else if (name.equals("model_start")) { //$NON-NLS-1$
    	ParticleModel model = (ParticleModel)e.getSource();
    	if (model.getName().equals(getModelBuilder().getSelectedName())) {
    		modelBuilder.setSpinnerStartFrame(e.getNewValue());
    	}
    }
    else if (name.equals("model_end")) { //$NON-NLS-1$
    	ParticleModel model = (ParticleModel)e.getSource();
    	if (model.getName().equals(getModelBuilder().getSelectedName())) {
	  		int end = (Integer)e.getNewValue();
	  		if (end==Integer.MAX_VALUE) {
	  			end = getPlayer().getVideoClip().getLastFrameNumber();
	  		}
	  		modelBuilder.setSpinnerEndFrame(end);
    	}
    }
//    else if (name.equals("frameshift")) {                  // from video clip //$NON-NLS-1$
//      firePropertyChange("frameshift", null, e.getNewValue()); // to tracks //$NON-NLS-1$    	
//    }
    else if (name.equals("radian_angles")) { // angle format has changed //$NON-NLS-1$
      firePropertyChange("radian_angles", null, e.getNewValue()); // to tracks //$NON-NLS-1$    	
    }
    else if (name.equals("fixed_origin") //$NON-NLS-1$
    		|| name.equals("fixed_angle") //$NON-NLS-1$
    		|| name.equals("fixed_scale")) { //$NON-NLS-1$
    	changed = true;
      firePropertyChange(name, e.getOldValue(), e.getNewValue()); // to tracks
    }
    else if (e.getSource() == dataBuilder && name.equals("visible")) { //$NON-NLS-1$
      dataToolVisible = ((Boolean)e.getNewValue()).booleanValue();
    }
    else if (e.getSource() instanceof Filter && name.equals("visible")) { //$NON-NLS-1$
    	setSelectedPoint(null);
    }
    else if (name.equals("perspective")) { //$NON-NLS-1$
    	if (e.getNewValue()!=null) {
    		PerspectiveFilter filter = (PerspectiveFilter)e.getNewValue();
    		TTrack track = new PerspectiveTrack(filter);
    		addTrack(track);
    	}
    	else if (e.getOldValue()!=null) {
      	// clean up deleted perspective track and filter
    		PerspectiveFilter filter = (PerspectiveFilter)e.getOldValue();
      	PerspectiveTrack track = PerspectiveTrack.filterMap.get(filter);
    		if (track!=null) {
    			removeTrack(track);
    			track.dispose();
    			filter.setVideoPanel(null);
    		}
    	}
    }
    else if (Tracker.showHints) {
    	Tracker.startupHintShown = false;
    	if (name.equals("stepbutton")) { //$NON-NLS-1$
      	if (!((Boolean)e.getNewValue()).booleanValue()) setMessage(""); //$NON-NLS-1$
      	else setMessage(TrackerRes.getString("VideoPlayer.Step.Hint")); //$NON-NLS-1$	
      }
      else if (name.equals("backbutton")) { //$NON-NLS-1$    	
      	if (!((Boolean)e.getNewValue()).booleanValue()) setMessage(""); //$NON-NLS-1$
      	else setMessage(TrackerRes.getString("VideoPlayer.Back.Hint")); //$NON-NLS-1$	
      }
      else if (name.equals("inframe")) { //$NON-NLS-1$  
      	if (!((Boolean)e.getNewValue()).booleanValue()) setMessage(""); //$NON-NLS-1$
      	else setMessage(TrackerRes.getString("VideoPlayer.StartFrame.Hint")); //$NON-NLS-1$	
      }
      else if (name.equals("outframe")) { //$NON-NLS-1$  
      	if (!((Boolean)e.getNewValue()).booleanValue()) setMessage(""); //$NON-NLS-1$
      	else setMessage(TrackerRes.getString("VideoPlayer.EndFrame.Hint")); //$NON-NLS-1$	
      }
      else if (name.equals("slider")) { //$NON-NLS-1$  
      	if (!((Boolean)e.getNewValue()).booleanValue()) setMessage(""); //$NON-NLS-1$
      	else setMessage(TrackerRes.getString("VideoPlayer.Slider.Hint")); //$NON-NLS-1$	
      }
    }
    // move vector snap point if origin may have moved
    if (name.equals("videoclip") ||  //$NON-NLS-1$
    				name.equals("transform") || //$NON-NLS-1$
    				name.equals("stepnumber") || //$NON-NLS-1$
    				name.equals("coords")) { //$NON-NLS-1$
      int n = getFrameNumber();
      getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));
    }
    Tracker.logTime("end TrackerPanel property change "+name); //$NON-NLS-1$
  }

  /**
   * Overrides VideoPanel setImageBorder method to set the image border.
   *
   * @param borderFraction the border fraction
   */
  public void setImageBorder(double borderFraction) {
    super.setImageBorder(borderFraction);
    defaultImageBorder = getImageBorder();
  }

  /**
   * Overrides VideoPanel getFilePath method.
   *
   * @return the relative path to the file
   */
  public String getFilePath() {
  	if (defaultSavePath == null) return super.getFilePath();
    return defaultSavePath;
  }

  /**
   * Overrides DrawingPanel scale method.
   */
  public void scale() {
  	TMat mat = getMat();
  	if (mat != null) {
  		xOffset = mat.getXOffset();
  		yOffset = mat.getYOffset();
  	}
    super.scale();
    // erase all tracks if pixel transform has changed
    if (!pixelTransform.equals(prevPixelTransform)) {
      prevPixelTransform = getPixelTransform();
      eraseAll();
    }
    // load track control if TFrame is known
    TFrame frame = getTFrame();
    if (frame !=null && trackControl==null) 
    	trackControl = TrackControl.getControl(this);
    
  }

  /**
   * Overrides DrawingPanel setMouseCursor method.
   * This blocks the crosshair cursor (from iad mouse controller)
   * so that Tracker can set cursors for marking tracks.
   *
   * @param cursor the requested cursor
   */
  public void setMouseCursor(Cursor cursor) {
    if (cursor != Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
    		&& getCursor() != Tracker.zoomOutCursor
    		&& getCursor() != Tracker.zoomInCursor)
    	super.setMouseCursor(cursor);
  }

	/**
	* Sets the font level.
	*
	* @param level the desired font level
	*/
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		if (frame==null) return;
		// refresh views
    TView[][] views = frame.getTViews(this);
    if (views==null) return;
    for (TView[] viewset: views) {
    	if (viewset==null) continue;
    	for (TView view: viewset) {
    		view.refresh();
    	}
    }
    TTrackBar trackbar = TTrackBar.getTrackbar(this);
    trackbar.setFontLevel(level);
    trackbar.refresh();
    // select the correct fontSize menu radiobutton
    TMenuBar menubar = TMenuBar.getMenuBar(this);
    if (menubar.fontSizeGroup!=null) {
	    Enumeration<AbstractButton> e = menubar.fontSizeGroup.getElements();
	    for (; e.hasMoreElements();) {
	      AbstractButton button = e.nextElement();
	      int i = Integer.parseInt(button.getActionCommand());
	      if(i==FontSizer.getLevel()) {
	        button.setSelected(true);
	      }
	    }
    }
    
    for (TTrack track: getTracks()) {
    	track.setFontLevel(level);      	
    }
    TrackControl.getControl(this).refresh();
    if (modelBuilder!=null) {
    	modelBuilder.setFontLevel(level);
    }
    if (dataBuilder!=null) {
    	dataBuilder.setFontLevel(level);
    }
    if (autoTracker!=null) {
    	autoTracker.getWizard().setFontLevel(level);
    }
    if (attachmentDialog!=null) {
    	attachmentDialog.setFontLevel(level);
    }
    Video video = getVideo();
    if (video!=null) {
    	FilterStack filterStack = video.getFilterStack();
    	for (Filter filter: filterStack.getFilters()) {
        JDialog inspector = filter.getInspector();
        if (inspector != null) {
          FontSizer.setFonts(inspector, level);
          inspector.pack();
        }
    	}
    }
    if (algorithmDialog!=null) {
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
  public boolean isZoomEvent(MouseEvent e) {
  	return super.isZoomEvent(e) || getCursor()==Tracker.zoomInCursor;
  }

  /**
   * Overrides InteractivePanel getInteractive method.
   * This checks the selected track (if any) first.
   *
   * @return the interactive drawable identified by the most recent mouse event
   */
  public Interactive getInteractive() {
  	mEvent = mouseEvent; // to provide visibility to Tracker package
    Interactive iad = null;
    TTrack track = getSelectedTrack();
    if (track!=null && this.getCursor()==track.getMarkingCursor(mEvent))
    	return null;
    if (track!=null && (track.isDependent() || track == getAxes())) {
      iad = getAxes().findInteractive(
      				this, mouseEvent.getX(), mouseEvent.getY());
    }
    if (iad==null && track!=null && track!=getAxes()
        && !calibrationTools.contains(track)) {
      iad = track.findInteractive(this, mouseEvent.getX(), mouseEvent.getY());
    }
    if (iad!=null) return iad;
    return super.getInteractive();
  }

  @Override
  public void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" recycled by garbage collector"); //$NON-NLS-1$
  }
  
  /**
   * Main entry point when used as application.  Note: only args[0] is read.
   *
   * @param args args[0] may be an xml file
   */
  public static void main(String[] args) {
  	if (args==null || args.length==0) {
  		Tracker.main(args);
  		return;
  	}

  	Frame launcherFrame = null;
    Frame[] frames = Frame.getFrames();
    for(int i = 0, n = frames.length; i<n; i++) {
       if (frames[i].getName().equals("LauncherTool")) { //$NON-NLS-1$
      	 launcherFrame = frames[i];
      	 break;
       }
    }
    if (launcherFrame != null)
    	launcherFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//    Tracker.updateResources();
    // get the shared tracker and add tabs
    Tracker tracker = Tracker.getTracker();
    final TFrame frame = tracker.getFrame();
  	final String path = args[0];
    final LaunchNode node = Launcher.activeNode;
  	frame.addPropertyChangeListener("tab", new PropertyChangeListener() { //$NON-NLS-1$
		  public void propertyChange(PropertyChangeEvent e) {
		  	TrackerPanel trackerPanel = (TrackerPanel)e.getNewValue();
		  	if (trackerPanel.defaultFileName.equals(XML.getName(path))) {
  		  	frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
          frame.showTrackControl(trackerPanel);
          frame.showNotes(trackerPanel);
          frame.refresh();
          final int n = frame.getTab(trackerPanel);
          // set up the LaunchNode action and listener
          if (node != null) {
            final Action action = new javax.swing.AbstractAction() {
              public void actionPerformed(ActionEvent e) {
              	TrackerPanel trackerPanel = frame.getTrackerPanel(n);
                frame.removeTab(trackerPanel);
                if (frame.getTabCount() == 0) frame.setVisible(false);
              }
            };
            node.addTerminateAction(action);
            frame.tabbedPane.addContainerListener(new java.awt.event.ContainerAdapter() {
              public void componentRemoved(ContainerEvent e) {
                Component tab = frame.tabbedPane.getComponentAt(n);
                if (e.getChild() == tab) {
                  node.terminate(action);
                }
              }
            });
          }
		  	}
		  }
		});
    TrackerIO.open(path, frame);
    frame.setVisible(true);
    if (frame.isIconified()) frame.setState(Frame.NORMAL);
    if (launcherFrame != null)
    	launcherFrame.setCursor(Cursor.getDefaultCursor());
  }
  
  protected void addCalibrationTool(String name, TTrack tool) {
  	calibrationTools.add(tool);
  	addTrack(tool);
  }
  
  protected BufferedImage renderMat() {
		if (renderedImage == null
				|| renderedImage.getWidth() != getWidth() 
				|| renderedImage.getHeight() != getHeight()) {
			renderedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		}
		render(renderedImage);
  	Rectangle rect = getMat().drawingBounds;
		if (matImage == null
				|| matImage.getWidth() != rect.width 
				|| matImage.getHeight() != rect.height) {
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
    Iterator<TTrack> it = getTracks().iterator();
    while (it.hasNext()) {
      TTrack track = it.next();
      Step step = track.getStep(pt, this);
      if (step != null) {
        step = track.deleteStep(step.n);
        if (step == null) return;
        setSelectedPoint(null);
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
		for (TTrack track: getTracks()) {
			boolean isChanged = false;
			XMLControl control = new XMLControlElement(track);
			for (Step step: selectedSteps) {
		   	if (step.getTrack()==track) {
		   		if (track.isLocked()) {
		   			step.erase();
		   		}
		   		else {
			    	int n = step.getFrameNumber();
			      track.steps.setStep(n, null);
			      for (String columnName: track.textColumnNames) {
			      	String[] entries = track.textColumnEntries.get(columnName);
			      	if (entries.length>n) {
			      		entries[n] = null;
			      	}
			      }
			      isChanged = true;
		   		}
		   	}
		  }
		  if (isChanged) {
		    changes.add(new Object[] {track, control});
		    track.firePropertyChange("steps", null, null); //$NON-NLS-1$
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
   * @param g the graphics context
   */
  public void paintComponent(Graphics g) {
//    Tracker.logTime(getClass().getSimpleName()+hashCode()+" painting"); //$NON-NLS-1$
    super.paintComponent(g);
    if (zoomCenter!=null && isShowing() && getTFrame()!=null && scrollPane!=null) {
      final Rectangle rect = scrollPane.getViewport().getViewRect();
      int x = zoomCenter.x - rect.width/2;
      int y = zoomCenter.y -rect.height/2;
      rect.setLocation(x, y);
    	zoomCenter = null;
  		scrollRectToVisible(rect);
    }
    showFilterInspectors();
  }
  
  /**
   * Gets the default image width for new empty panels
   * @return width
   */
  protected static double getDefaultImageWidth() {
  	return defaultWidth;
  }

  /**
   * Gets the default image height for new empty panels
   * @return height
   */
  protected static double getDefaultImageHeight() {
  	return defaultHeight;
  }
  
  /**
   * Gets the TFrame parent of this panel
   * @return the TFrame, if any
   */
  protected TFrame getTFrame() {
  	if (frame == null) {
	    Container c = getTopLevelAncestor();
	    if (c instanceof TFrame) {
	    	frame = (TFrame)c;
	    }
  	}
    return frame;
  }
  
  /**
   * Gets the autotracker for this panel
   * @return the autotracker, if any
   */
  protected AutoTracker getAutoTracker() {
  	if (autoTracker==null) {
  		autoTracker = new AutoTracker(this);
  		autoTracker.getWizard().setFontLevel(FontSizer.getLevel());
  	}
  	return autoTracker;
  }
  
  /**
   * Disposes of this panel
   */
  protected void dispose() {

  	refreshTimer.stop();
  	zoomTimer.stop();
  	refreshTimer = zoomTimer = null;
  	offscreenImage = null;
  	workingImage = null;
  			
  	FontSizer.removePropertyChangeListener("level", guiChangeListener); //$NON-NLS-1$
    ToolsRes.removePropertyChangeListener("locale", guiChangeListener); //$NON-NLS-1$
    removeMouseListener(mouseController);
    removeMouseMotionListener(mouseController);
    mouseController = null;
    removeMouseListener(optionController);
    removeMouseMotionListener(optionController);
    optionController = null;
    VideoClip clip = player.getVideoClip();
    clip.removePropertyChangeListener(player);
    clip.removePropertyChangeListener("startframe", this); //$NON-NLS-1$
    clip.removePropertyChangeListener("stepsize", this);   //$NON-NLS-1$
    clip.removePropertyChangeListener("stepcount", this);  //$NON-NLS-1$
    clip.removePropertyChangeListener("framecount", this);  //$NON-NLS-1$
    clip.removePropertyChangeListener("starttime", this);  //$NON-NLS-1$
    clip.removePropertyChangeListener("adjusting", this);  //$NON-NLS-1$
    if(video!=null) {
      video.removePropertyChangeListener("coords", this);          //$NON-NLS-1$
      video.removePropertyChangeListener("image", this);           //$NON-NLS-1$
      video.removePropertyChangeListener("filterChanged", this);           //$NON-NLS-1$
      video.removePropertyChangeListener("videoVisible", this);    //$NON-NLS-1$
      video.removePropertyChangeListener("size", this);            //$NON-NLS-1$
    }
    ClipControl clipControl = player.getClipControl();
    clipControl.removePropertyChangeListener(player);
    player.removePropertyChangeListener("stepbutton", this); //$NON-NLS-1$
    player.removePropertyChangeListener("backbutton", this); //$NON-NLS-1$
    player.removePropertyChangeListener("inframe", this); //$NON-NLS-1$
    player.removePropertyChangeListener("outframe", this); //$NON-NLS-1$
    player.removePropertyChangeListener("slider", this); //$NON-NLS-1$
    player.removePropertyChangeListener("playing", this); //$NON-NLS-1$
    player.removePropertyChangeListener("videoclip", this);     //$NON-NLS-1$
    player.removePropertyChangeListener("stepnumber", this);    //$NON-NLS-1$
    player.removePropertyChangeListener("frameduration", this); //$NON-NLS-1$
    player.stop();
    remove(player);
    player = null;
    coords.removePropertyChangeListener(this);
    coords = null;
    for (Integer n: TTrack.activeTracks.keySet()) {
    	TTrack track = TTrack.activeTracks.get(n);
    	removePropertyChangeListener(track);
      track.removePropertyChangeListener("step", this); //$NON-NLS-1$
      track.removePropertyChangeListener("steps", this); //$NON-NLS-1$
      track.removePropertyChangeListener("name", this); //$NON-NLS-1$
      track.removePropertyChangeListener("mass", this); //$NON-NLS-1$
      track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      track.removePropertyChangeListener("model_start", this); //$NON-NLS-1$
      track.removePropertyChangeListener("model_end", this); //$NON-NLS-1$
      track.removePropertyChangeListener("visible", this); //$NON-NLS-1$
      track.removePropertyChangeListener("stepnumber", this); //$NON-NLS-1$
      track.removePropertyChangeListener("image", this); //$NON-NLS-1$
      track.removePropertyChangeListener("data", this); //$NON-NLS-1$
    }
    // dispose of autotracker, modelbuilder, databuilder, other dialogs    
    if (autoTracker!=null) {
    	autoTracker.dispose();
    	autoTracker = null;
    }
    if (modelBuilder!=null) {
			modelBuilder.removePropertyChangeListener("panel", this); //$NON-NLS-1$
			modelBuilder.dispose();
			modelBuilder = null;
    }
    if (dataBuilder!=null) {
  		dataBuilder.removePropertyChangeListener("panel", this); //$NON-NLS-1$
  		dataBuilder.removePropertyChangeListener("function", this); //$NON-NLS-1$
  		dataBuilder.removePropertyChangeListener("visible", this); //$NON-NLS-1$
  		dataBuilder.dispose();
  		dataBuilder = null;
    }
    if (attachmentDialog!=null) {
    	attachmentDialog.dispose();
    	attachmentDialog = null;
    }
    if (algorithmDialog!=null) {
    	algorithmDialog.trackerPanel = null;
    	algorithmDialog = null;
    }
    if (ExportDataDialog.dataExporter!=null && ExportDataDialog.dataExporter.trackerPanel==this) {
    	ExportDataDialog.dataExporter.trackerPanel = null;
    	ExportDataDialog.dataExporter.tableDropdown.removeAllItems();
    	ExportDataDialog.dataExporter.tables.clear();
    	ExportDataDialog.dataExporter.trackNames.clear();
    }
    if (ExportVideoDialog.videoExporter!=null && ExportVideoDialog.videoExporter.trackerPanel==this) {
    	ExportVideoDialog.videoExporter.trackerPanel = null;
    	ExportVideoDialog.videoExporter.views.clear();
    }
    if (ExportZipDialog.zipExporter!=null && ExportZipDialog.zipExporter.trackerPanel==this) {
    	ExportZipDialog.zipExporter.trackerPanel = null;
    	ExportZipDialog.zipExporter.badModels.clear();
    	ExportZipDialog.zipExporter.videoExporter.trackerPanel = null;
    	ExportZipDialog.zipExporter.videoExporter.views.clear();
    }
    if (ThumbnailDialog.thumbnailDialog!=null && ThumbnailDialog.thumbnailDialog.trackerPanel==this) {
    	ThumbnailDialog.thumbnailDialog.trackerPanel = null;   	
    }
    filterClasses.clear();
    selectingPanel = null;
    frame = null;
    renderedImage = null;
    matImage = null;
    selectedSteps = null;
    removeAll();
  }
  
  /**
   * Sets the name of a track. This checks the name against those of existing 
   * tracks and prompts the user for a new name if a duplicate is found.
   * After three failed attempts, a unique name is formed by appending a number.
   * 
   * @param track the track to name
   * @param newName the proposed name
   * @param postEdit true to post an undoable edit
   */
  protected void setTrackName(TTrack track, String newName, boolean postEdit) {
		for (Drawable next: getDrawables()) {
			if (next == track) continue;
			if (next instanceof TTrack) {
				String nextName = ((TTrack)next).getName();
				if (newName.equals(nextName)) {
					Toolkit.getDefaultToolkit().beep();
					String s = "\"" + newName + "\" "; //$NON-NLS-1$ //$NON-NLS-2$
					badNameLabel.setText(s + TrackerRes.getString("TTrack.Dialog.Name.BadName")); //$NON-NLS-1$
					TTrack.NameDialog nameDialog = TTrack.getNameDialog(track);
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
    if (TTrack.nameDialog!=null) {
			TTrack.nameDialog.setVisible(false);
			TTrack.nameDialog.getContentPane().remove(badNameLabel);
    }
		TMenuBar.getMenuBar(this).refresh();
  }

  /**
   * Shows the visible filter inspectors, if any.
   */
  protected void showFilterInspectors() {
	  // show filter inspectors
	  if (visibleFilters != null) {
	  	TFrame frame = getTFrame();
	    Iterator<Filter> it = visibleFilters.keySet().iterator();
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    while (it.hasNext()) {
	    	Filter filter = it.next();
	    	Point p = visibleFilters.get(filter);
	    	Dialog inspector = filter.getInspector();
				int x = Math.max(p.x + frame.getLocation().x, 0);
				x = Math.min(x, dim.width-inspector.getWidth());
				int y = Math.max(p.y + frame.getLocation().y, 0);
				y = Math.min(y, dim.height-inspector.getHeight());
	    	inspector.setLocation(x, y);
	    	inspector.setVisible(true);
	    }
	    visibleFilters.clear();
	    visibleFilters = null;
	  }
  }
  
	/**
   * This inner class extends IADMouseController to set the cursor
   * and show selected point coordinates.
   */
  private class TMouseController extends IADMouseController {
      /**
       * Handle the mouse released event.
       * 
       * @param e the mouse event
       */
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
      public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        setMouseCursor(Cursor.getDefaultCursor());
      }

      /**
       * Handle the mouse exited event.
       *
       * @param e the mouse event
       */
      public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        if (getSelectedPoint()==null) {
        	blMessageBox.setText(null);
        }
        setMouseCursor(Cursor.getDefaultCursor());
      }

      /**
       * Handle the mouse entered event.
       *
       * @param e the mouse event
       */
      public void mouseMoved(MouseEvent e) {
        if(showCoordinates && getSelectedPoint()==null) {
          String s = coordinateStrBuilder.getCoordinateString(TrackerPanel.this, e);
          blMessageBox.setText(s);
        }
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
  static class Loader implements XML.ObjectLoader {

    /**
     * Saves object data.
     *
     * @param control the control to save to
     * @param obj the TrackerPanel object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      // turn off XML writing of null final array elements
    	boolean writeNullFinalArrayElements = XMLPropertyElement.defaultWriteNullFinalArrayElements;
    	XMLPropertyElement.defaultWriteNullFinalArrayElements = false;
    	
      TrackerPanel trackerPanel = (TrackerPanel)obj;
      // save the version
      control.setValue("version", Tracker.VERSION); //$NON-NLS-1$
      // save the image size
      control.setValue("width", trackerPanel.getImageWidth()); //$NON-NLS-1$
      control.setValue("height", trackerPanel.getImageHeight()); //$NON-NLS-1$
      // save the magnification
      double zoom = trackerPanel.getPreferredSize().width > 10? 
      				trackerPanel.getMagnification(): -1;
      control.setValue("magnification", zoom); //$NON-NLS-1$
      if (trackerPanel.getTFrame()!=null) {
      	MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
      	Rectangle rect = mainView.scrollPane.getViewport().getViewRect(); 
        control.setValue("center_x", (int)rect.getCenterX()); //$NON-NLS-1$
        control.setValue("center_y", (int)rect.getCenterY()); //$NON-NLS-1$
      }
      // save the description, if any
      if (trackerPanel.hideDescriptionWhenLoaded) {
      	control.setValue("hide_description", true); //$NON-NLS-1$
      }
      if (!trackerPanel.description.trim().equals("")) { //$NON-NLS-1$
        control.setValue("description", trackerPanel.description); //$NON-NLS-1$
      }
      // save the metadata, if any
      if (trackerPanel.author!=null) {
        control.setValue("author", trackerPanel.author); //$NON-NLS-1$
      }
      if (trackerPanel.contact!=null) {
        control.setValue("contact", trackerPanel.contact); //$NON-NLS-1$
      }
      // save the video clip, clip control and coords
      control.setValue("videoclip", trackerPanel.getPlayer().getVideoClip()); //$NON-NLS-1$
      control.setValue("clipcontrol", trackerPanel.getPlayer().getClipControl()); //$NON-NLS-1$
      ImageCoordSystem coords = trackerPanel.getCoords();
      while (coords instanceof ReferenceFrame) {
        // save reference frame
      	ReferenceFrame refFrame = (ReferenceFrame)coords;
      	TTrack track = refFrame.getOriginTrack();
        control.setValue("referenceframe", track.getName()); //$NON-NLS-1$
        coords = refFrame.getCoords();
      }
      control.setValue("coords", coords); //$NON-NLS-1$
      // save the tracks
      control.setValue("tracks", trackerPanel.getTracksToSave()); //$NON-NLS-1$
      // save the selected track
      TTrack track = trackerPanel.getSelectedTrack();
      if (track != null) {
        control.setValue("selectedtrack", track.getName()); //$NON-NLS-1$
      }

      // save custom configurations
      if (!trackerPanel.isDefaultConfiguration() && 
      				trackerPanel.isEnabled("config.saveWithData")) { //$NON-NLS-1$
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
          if (i == 0) w = pane.getMaximumDividerLocation();
          int max = i==3? w: pane.getMaximumDividerLocation();
          dividerLocations[i] = Math.min(1.0, 1.0*pane.getDividerLocation()/max);
        }
        control.setValue("dividers", dividerLocations); //$NON-NLS-1$
        // save the custom views
        TView[][] customViews = frame.getTViews(trackerPanel, true);
        for (int i = 0; i < customViews.length; i++) {
          if (customViews[i] == null) continue;
          control.setValue("views", customViews); //$NON-NLS-1$
          break;
        }
        // save the selected views
        String[] selectedViews = frame.getSelectedTViews(trackerPanel);
        for (int i = 0; i < selectedViews.length; i++) {
          if (selectedViews[i] == null) continue;
          control.setValue("selected_views", selectedViews); //$NON-NLS-1$
          break;
        }
        // save the toolbar for button states
        TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
        control.setValue("toolbar", toolbar); //$NON-NLS-1$
        // save the visibility and location of the track control
        TrackControl tc = trackerPanel.trackControl;
        if (tc!= null && tc.isVisible()) {
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
      ArrayList<DataToolTab> tabs = new ArrayList<DataToolTab>();
    	DataTool tool = DataTool.getTool();
    	for (DataToolTab tab: tool.getTabs()) {
      	ArrayList<TTrack> tracks = trackerPanel.getTracks();
      	for (TTrack next: tracks) {
      		Data data = next.getData(trackerPanel);
      		if (tab.isOwnedBy(data)) {      			
      			// prepare tab for saving by setting owner and saving owned column names
      			tab.setOwner(next.getName(), data);
      			for (TTrack tt: trackerPanel.getTracks()) {
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

      // restore XML writing of null final array elements
    	XMLPropertyElement.defaultWriteNullFinalArrayElements = writeNullFinalArrayElements;
    }

    /**
     * Creates an object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new TrackerPanel();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      TrackerPanel trackerPanel = (TrackerPanel)obj;
	    // load and check the Tracker version that created this file
	    String ver = control.getString("version"); //$NON-NLS-1$
	    if (ver!=null) {
	    	double xmlVersion = Double.parseDouble(ver);
	    	double version = Double.parseDouble(Tracker.VERSION);
	    	if (xmlVersion-version>0.2) {
	    		JOptionPane.showMessageDialog(trackerPanel, 
	    				TrackerRes.getString("TrackerPanel.Dialog.Version.Message1") //$NON-NLS-1$
	    				+ " "+ver+" " //$NON-NLS-1$ //$NON-NLS-2$
	    				+ TrackerRes.getString("TrackerPanel.Dialog.Version.Message2") //$NON-NLS-1$
	    				+ "\n"+TrackerRes.getString("TrackerPanel.Dialog.Version.Message3") //$NON-NLS-1$ //$NON-NLS-2$
	    				+ " ("+Tracker.VERSION+")." //$NON-NLS-1$ //$NON-NLS-2$
	    				+ "\n\n"+TrackerRes.getString("TrackerPanel.Dialog.Version.Message4") //$NON-NLS-1$ //$NON-NLS-2$
	    				+" "+Tracker.trackerWebsite+".",  //$NON-NLS-1$ //$NON-NLS-2$
	    				TrackerRes.getString("TrackerPanel.Dialog.Version.Title"),  //$NON-NLS-1$
	    				JOptionPane.INFORMATION_MESSAGE);
	    	}	    	
	    }
      // load the description
    	trackerPanel.hideDescriptionWhenLoaded = control.getBoolean("hide_description"); //$NON-NLS-1$
      String desc = control.getString("description"); //$NON-NLS-1$
      if (desc != null) {
      	trackerPanel.setDescription(desc);
      }
      // load the metadata
      trackerPanel.author = control.getString("author"); //$NON-NLS-1$
      trackerPanel.contact = control.getString("contact"); //$NON-NLS-1$
      // load the video clip
      XMLControl child = control.getChildControl("videoclip"); //$NON-NLS-1$
      if (child != null) {
//      	Video existingVideo = trackerPanel.getVideo();
        VideoClip clip = (VideoClip) control.getObject("videoclip"); //$NON-NLS-1$
      	// if newly loaded clip has no video use existing video, if any
//        if (clip.getVideo()==null && existingVideo!=null) {
//        	VideoClip existingClip = trackerPanel.getPlayer().getVideoClip();
//        	existingClip.setStartFrameNumber(clip.getStartFrameNumber());
//        	existingClip.setStepSize(clip.getStepSize());
//        	existingClip.setStepCount(clip.getStepCount());
//        }
//        else {
	        trackerPanel.getPlayer().setVideoClip(clip);
	        Video vid = clip.getVideo();
	        if (vid != null) {
	          FilterStack stack = vid.getFilterStack();
	          Iterator<Filter> it = stack.getFilters().iterator();
	          while (it.hasNext()) {
	          	Filter filter = it.next();
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
//        }
      }
      // load the clip control
      child = control.getChildControl("clipcontrol"); //$NON-NLS-1$
      if (child != null) {
        ClipControl clipControl = trackerPanel.getPlayer().getClipControl();
        child.loadObject(clipControl);
      }
      // load the toolbar
      child = control.getChildControl("toolbar"); //$NON-NLS-1$
      if (child != null) {
        TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
        child.loadObject(toolbar);
      }
      // load the coords
      child = control.getChildControl("coords"); //$NON-NLS-1$
      if (child != null) {
        ImageCoordSystem coords = trackerPanel.getCoords();
        child.loadObject(coords);
        int n = trackerPanel.getFrameNumber();
        trackerPanel.getSnapPoint().setXY(coords.getOriginX(n), coords.getOriginY(n));
      }
    	// kludge to prevent a freeze (deadlock?) when loading QT videos and DataTracks
      Video vid = trackerPanel.getVideo();
    	if (vid!=null && vid.getClass().getSimpleName().contains("QT") //$NON-NLS-1$
    			&& control.toXML().contains("ParticleDataTrack")) try { //$NON-NLS-1$
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
      // load the tracks
      ArrayList<?> tracks = ArrayList.class.cast(control.getObject("tracks")); //$NON-NLS-1$
      if (tracks != null) {
      	for (Object next: tracks) {
      		TTrack track = (TTrack)next;
          trackerPanel.addTrack(track);
      	}
      }
      // load the reference frame
      String rfName = control.getString("referenceframe"); //$NON-NLS-1$
      if (rfName!=null) {
      	trackerPanel.setReferenceFrame(rfName);
      }
      // load the configuration
      Configuration config = (Configuration)control.getObject("configuration"); //$NON-NLS-1$
      if (config != null) {
        trackerPanel.enabled = config.enabled;
      }
	    // load the selected_views property
      java.util.List<Object> props = control.getPropertyContent();
      int n = -1;
	    for (int i = 0; i < props.size(); i++) {
	      XMLProperty prop = (XMLProperty)props.get(i);
	      if (prop.getPropertyName().equals("selected_views")) { //$NON-NLS-1$
	        n = i;
	        break;
	      }
	    }
	    trackerPanel.selectedViewsProperty = n>-1? (XMLProperty)props.get(n): null;
      // load the views property
      n = -1;
      for (int i = 0; i < props.size(); i++) {
        XMLProperty prop = (XMLProperty)props.get(i);
        if (prop.getPropertyName().equals("views")) { //$NON-NLS-1$
          n = i;
          break;
        }
      }
      trackerPanel.viewsProperty = n>-1? (XMLProperty)props.get(n): null;
      // load the dividers
      trackerPanel.dividerLocs = (double[])control.getObject("dividers"); //$NON-NLS-1$
      // load the track control location
      trackerPanel.trackControlX = control.getInt("track_control_x"); //$NON-NLS-1$
      trackerPanel.trackControlY = control.getInt("track_control_y"); //$NON-NLS-1$
      // load the info dialog location
      trackerPanel.infoX = control.getInt("info_x"); //$NON-NLS-1$
      trackerPanel.infoY = control.getInt("info_y"); //$NON-NLS-1$
      // load the image size
      if (control.getPropertyNames().contains("width")) { //$NON-NLS-1$
        trackerPanel.setImageWidth(control.getDouble("width")); //$NON-NLS-1$
      }
      if (control.getPropertyNames().contains("height")) { //$NON-NLS-1$
        trackerPanel.setImageHeight(control.getDouble("height")); //$NON-NLS-1$
      }
      // load the zoom center and magnification
      trackerPanel.setMagnification(control.getDouble("magnification")); //$NON-NLS-1$
      if (control.getPropertyNames().contains("center_x")) { //$NON-NLS-1$
      	int x = control.getInt("center_x"); //$NON-NLS-1$
      	int y = control.getInt("center_y"); //$NON-NLS-1$
        trackerPanel.zoomCenter = new Point(x, y);
      }
      // set selected track
      String name = control.getString("selectedtrack"); //$NON-NLS-1$
      trackerPanel.setSelectedTrack(name==null? null: trackerPanel.getTrack(name));
      
      // load DataTool tabs
      if (control.getPropertyNames().contains("datatool_tabs")) { //$NON-NLS-1$
  			DataTool tool = DataTool.getTool();
      	for (Object o: control.getPropertyContent()) {
      		if (o instanceof XMLProperty) {
      			XMLProperty prop = (XMLProperty)o;
    				if (prop.getPropertyName().equals("datatool_tabs")) { //$NON-NLS-1$
	      			for (XMLControl tabControl: prop.getChildControls()) {
	      				// pass the tab control to the DataTool and get back the newly added tab
	      				ArrayList<DataToolTab> addedTabs = null;
								try {
									addedTabs = tool.addTabs(tabControl);
								} catch (Exception e) {
								}
	      				if (addedTabs==null || addedTabs.isEmpty()) continue;
	      				DataToolTab tab = addedTabs.get(0);
	      				
	      				// set the owner of the tab to the specified track
	      				String trackname = tab.getOwnerName();
	      				TTrack track = trackerPanel.getTrack(trackname);
	      				if (track==null) continue;
	      				Data data = track.getData(trackerPanel);
	      				tab.setOwner(trackname, data);
	      				
	      				// set up a DataRefreshTool and send it to the tab
	              DataRefreshTool refresher = DataRefreshTool.getTool(data);	              
	            	DatasetManager toSend = new DatasetManager();
	            	toSend.setID(data.getID());
	              try {
	                tab.send(new LocalJob(toSend), refresher);
	              }
	              catch (RemoteException ex) {ex.printStackTrace();}
	              
	              // set the tab column IDs to the track data IDs and add track data to the refresher
	              for (TTrack tt: trackerPanel.getTracks()) {
	              	Data trackData = tt.getData(trackerPanel);
	              	if (tab.setOwnedColumnIDs(tt.getName(), trackData)) { // true if track owns one or more columns
	              		refresher.addData(trackData);
	              	}
	              }
	              // tab is now fully "wired" for refreshing by tracks
	      			}    					
    				}
      		}
      	}
      }

      return obj;
    }
  }
}

