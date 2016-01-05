/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2015  Douglas Brown
 * 
 * Tracker is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Tracker is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Tracker; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston MA 02111-1307 USA or view the license online at
 * <http://www.gnu.org/copyleft/gpl.html>
 * 
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.DataClip;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.tools.Parameter;

/**
 * This is a particle model with steps based on world positions defined in a Data object.
 * The Data object is an "external model" associated with a source (eg path, URL, Tool, null)
 * The Data must define data arrays "x" and "y" and may include a clock array "t" 
 * The Data may include additional data array pairs "x1", "y1", etc.
 *
 * @author Douglas Brown
 */
public class ParticleDataTrack extends ParticleModel implements DataTrack {
	// pig test this with video with frameshift
	private static ArrayList<String> initialFootprintNames; 
	static {
		initialFootprintNames = new ArrayList<String>();
		initialFootprintNames.add("CircleFootprint.FilledCircle#6 outline"); //$NON-NLS-1$
		initialFootprintNames.add("CircleFootprint.Circle#5 outlinebold"); //$NON-NLS-1$
		initialFootprintNames.add("Footprint.Spot"); //$NON-NLS-1$
	}
	
	private DataClip dataClip;
	private Data sourceData;
  private double[] xData={0}, yData={0}, tData={0};
  private Point2D[] tracePosition; // used by getNextTracePositions() method
  private int stepCounter;
  private Object dataSource; // may be ParticleDataTrack leader 
  private boolean useDataTime;
  protected String pointName="", modelName=""; //$NON-NLS-1$ //$NON-NLS-2$
  private ArrayList<ParticleDataTrack> morePoints = new ArrayList<ParticleDataTrack>();
  private JMenu pointsMenu = new JMenu();
	
	/**
	 * Public constructor.
	 * 
	 * @param data the Data object
	 * @param source the data source object (null if data is pasted)
	 * @throws Exception if the data does not define x and y-datasets
	 */
	public ParticleDataTrack(Data data, Object source) throws Exception {
		this(source);
		getDataClip().addPropertyChangeListener(this);
		// next line throws Exception if the data does not define x and y-columns
		setData(data);
	}
	
	/**
	 * Private constructor used by all.
	 * 
	 * @param source the data source
	 */
	private ParticleDataTrack(Object source) {
		dataSource = source;
		tracePosition = new Point2D[] {point};
		tracePtsPerStep = 1;
	}
	
	/**
	 * Private constructor for making additional point tracks.
	 * 
	 * @param data Object[] {String name, double[2][] xyData}
	 * @param parent the parent
	 */
	private ParticleDataTrack(Object[] data, ParticleDataTrack parent) {
		this(parent);
		dataClip = parent.getDataClip();
		getDataClip().addPropertyChangeListener(this);
		setPointName(data[0].toString());
//		setName(pointName);
		double[][] xyData = (double[][])data[1];
		setData(xyData, true);
	}
	
	/**
	 * Private constructor for XMLLoader.
	 * 
	 * @param data the Data object
	 */
	private ParticleDataTrack(double[][] coreData, ArrayList<Object[]> pointData) {
		this(null);
		getDataClip().addPropertyChangeListener(this);
		try {
			setData(coreData, true);
		} catch (Exception e) {}		
		
		for (int i = 0; i< pointData.size(); i++) {
			// get the new data
			Object[] next = pointData.get(i);				
			double[][] xyArray = (double[][])next[1];				
			double[][] dataArray = new double[][] {xyArray[0], xyArray[1], null};		
			ParticleDataTrack target = new ParticleDataTrack(next, this);
			morePoints.add(target);
			target.setTrackerPanel(trackerPanel);
			if (trackerPanel!=null) {
				trackerPanel.addTrack(target);
			}
			
			// set target's data
			target.setData(dataArray, true);
		}
	}
	
	@Override
  public void delete() {
    for (TTrack track: morePoints) {
    	track.delete(false); // don't post undoable edit
    }
    super.delete();
  }

	/**
	 * Returns a menu with items that control this track.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	public JMenu getMenu(TrackerPanel trackerPanel) {
		if (getLeader()!=this) {
			return getPointMenu(trackerPanel);
		}
		
		JMenu menu = super.getMenu(trackerPanel);
		menu.setIcon(getIcon(21, 16, "model")); //$NON-NLS-1$
		menu.removeAll();
		
		// refresh points menu
		pointsMenu.setText(TrackerRes.getString("ParticleDataTrack.Menu.Points")); //$NON-NLS-1$
		pointsMenu.removeAll();		
		// add point menus
		for (ParticleDataTrack next: allPoints()) {
			JMenu pointMenu = next.getPointMenu(trackerPanel);
			pointsMenu.add(pointMenu);
		}

		// assemble menu
		menu.add(inspectorItem);
		menu.add(pointsMenu);
		menu.addSeparator();
		menu.add(descriptionItem);
		menu.add(visibleItem);
//		menu.addSeparator();
//		menu.add(dataBuilderItem);
		menu.addSeparator();
		menu.add(deleteTrackItem);
		return menu;
	}
	
	/**
	 * Returns a menu with items associated with this track's point properties.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	protected JMenu getPointMenu(TrackerPanel trackerPanel) {
    // prepare menu items
    colorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
    footprintMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
    velocityMenu.setText(TrackerRes.getString("PointMass.MenuItem.Velocity")); //$NON-NLS-1$
    accelerationMenu.setText(TrackerRes.getString("PointMass.MenuItem.Acceleration")); //$NON-NLS-1$
		JMenu menu = getLeader()!=this? super.getMenu(trackerPanel): new JMenu();
		menu.setText(getPointName());
    menu.setIcon(getFootprint().getIcon(21, 16));
		menu.removeAll();
		menu.add(colorItem);
		menu.add(footprintMenu);
		menu.addSeparator();
		menu.add(velocityMenu);
		menu.add(accelerationMenu);
		return menu;
	}
	
  @Override
	public Icon getIcon(int w, int h, String context) {
  	// for point context, return footprint icon
		if (context.contains("point")) { //$NON-NLS-1$
			return getFootprint().getIcon(w, h);
		}
		// for other contexts, return combination icon
		ArrayList<ShapeIcon> shapeIcons = new ArrayList<ShapeIcon>();
		for (TTrack track: getLeader().allPoints()) {
			Icon icon = track.getFootprint().getIcon(w, h);
			if (icon instanceof ShapeIcon) {
				shapeIcons.add((ShapeIcon)icon);
			}
		}
		return new ComboIcon(shapeIcons);
	}
	
  @Override
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
    ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    if (trackerPanel.getSelectedPoint()==null) {
	    list.remove(massLabel);
	    list.remove(massField);
	    list.remove(mSeparator);
    }
    return list;
  }
  
	/**
	 * Gets the point name. The point name is appended to the 
	 * lead track name for most buttons and dropdowns.
	 * 
	 * @return the point name
	 */
  protected String getPointName() {
  	if (pointName==null) {
  		pointName = ""; //$NON-NLS-1$
  	}
  	if ("".equals(pointName)) { //$NON-NLS-1$
  		ArrayList<ParticleDataTrack> pts = getLeader().allPoints();
			for (int i=0; i<pts.size(); i++) {
				if (pts.get(i)==this) {
					return alphabet.substring(i, i+1);
				}
			}
  	}
  	// count duplicates
  	int count = 0, i = 0;
  	for (ParticleDataTrack next: getLeader().allPoints()) {
			if (pointName.equals(next.pointName)) {
				count++;
				if (next==this) {
					i = count;
				}
			}
		}
  	if (count>1) {
  		return pointName+" "+i; //$NON-NLS-1$
  	}
  	return pointName;
  }

	/**
	 * Sets the point name. The point name is appended to the 
	 * leader's track name for most buttons and dropdowns.
	 * 
	 * @param newName the point name
	 */
  protected void setPointName(String newName) {
  	if (newName==null) newName = ""; //$NON-NLS-1$
  	boolean changed = !newName.equals(pointName);
  	pointName = newName;
  	if (changed) {
  		for (ParticleDataTrack next: allPoints()) {
  			next.name = next.getFullName();
  		}
  		support.firePropertyChange("name", null, null); //$NON-NLS-1$
  	}
  }

	/**
	 * Gets a list of all points in this track.
	 * 
	 * @return the points
	 */
  protected ArrayList<ParticleDataTrack> allPoints() {
  	ArrayList<ParticleDataTrack> points = new ArrayList<ParticleDataTrack>();
  	points.add(this);
  	if (morePoints!=null) {
  		points.addAll(morePoints);
  	}
  	return points;
  }

	/**
	 * Gets the full name (model & point) for this track.
	 * 
	 * @return the full name
	 */
  public String getFullName() {
  	return getLeader().modelName+" "+getPointName(); //$NON-NLS-1$
  }
  
  @Override
  public String getName(String context) {
  	// point context: full name (eg "example A" or "example elbow")
  	if (context.contains("point")) { //$NON-NLS-1$
  		return getName();
  	}
  	// for other contexts, return modelName only (eg "example")
  	return getLeader().modelName;  	  	
  }
  
  @Override
  public void setName(String newName) {  	
  	// set the model name if this is the leader
  	if (getLeader()==this) {
    	// ignore if newName equals current full name
    	if (getFullName().equals(newName)) return;
  		modelName = newName;
  		// set name of all points
  		for (ParticleDataTrack next: allPoints()) {
  			next.name = next.getFullName();
  		}
  	}
  	// do nothing for other points
  }

  @Override
  public void setColor(Color color) {
  	super.setColor(color);
  	if (getLeader()!=this) {
  		getLeader().support.firePropertyChange("color", null, color); //$NON-NLS-1$
  	}
  }
  
  @Override
  public void setFootprint(String name) {
  	super.setFootprint(name);
  	if (getLeader()!=this) {
  		getLeader().support.firePropertyChange("footprint", null, getLeader().footprint); //$NON-NLS-1$
  	}
//    support.firePropertyChange("footprint", null, footprint); //$NON-NLS-1$
  }
	/**
	 * Returns the lead track (index=0)
	 * 
	 * @return the leader (may be this track)
	 */
	public ParticleDataTrack getLeader() {
		if (dataSource instanceof ParticleDataTrack) {
			return (ParticleDataTrack)dataSource;
		}
		return this;
	}
	
	/**
	 * Sets the Data. Data must define columns "x" and "y".
	 * If time data is included, it is assumed to be in seconds.
	 * 
	 * @param data the Data object
	 * @throws Exception if the data does not define x and y-columns
	 */
	public void setData(Data data) throws Exception {
		OSPLog.finer("Setting new data"); //$NON-NLS-1$
		
		// the following line throws an exception if (x, y) data is not found
		ArrayList<Object[]> pointData = getPointData(data);
		sourceData = data;
		
		// set {x,y,t} data for this lead track
		Object[] coreData = pointData.get(0);
		setPointName(coreData[0].toString());
		double[][] xyArray = (double[][])coreData[1];				
		double[] timeArray = getTimeData(data);				
		if (timeArray!=null && xyArray[0].length!=timeArray.length) {
			throw new Exception("Time data has incorrect array length"); //$NON-NLS-1$
		}
		
		double[][] dataArray = new double[][] {xyArray[0], xyArray[1], timeArray};		
		setData(dataArray, true);
		
		// set {x,y} for additional points
		for (int i = 1; i< pointData.size(); i++) {
			// get the new data
			Object[] next = pointData.get(i);				
			xyArray = (double[][])next[1];				
			dataArray = new double[][] {xyArray[0], xyArray[1], null};		
				
			// if needed, create new track
			if (i>morePoints.size()) {
				ParticleDataTrack target = new ParticleDataTrack(next, this);
				morePoints.add(target);
				target.setTrackerPanel(trackerPanel);
				if (trackerPanel!=null) {
					trackerPanel.addTrack(target);
				}
			}
			else {
				ParticleDataTrack target = morePoints.get(i-1);
				// set target's data
				target.setData(dataArray, true);
				// set target's pointName
				target.setPointName(next[0].toString());
			}
		}
	}
	
	/**
	 * Gets the model data. This can return null if loaded from XMLControl.
	 * 
	 * @return the data (may return null)
	 */
	public Data getData() {
		return sourceData;
	}
	
	/**
	 * Gets the data source.
	 * 
	 * @return the source (may return null)
	 */
	public Object getSource() {
		return dataSource;
	}
	
	/**
	 * Sets the data source.
	 * 
	 * @return the source (may return null)
	 */
	public void setSource(Object source) {
		dataSource = source;
	}
	
	/**
	 * Gets the data clip.
	 * 
	 * @return the data clip
	 */
	public DataClip getDataClip() {
		if (dataClip==null) {
			dataClip = new DataClip();
		}
		return dataClip;
	}
	
	/**
	 * Gets the trackerPanel video clip.
	 * 
	 * @return the video clip (null if not yet added to TrackerPanel)
	 */
	public VideoClip getVideoClip() {
		if (trackerPanel==null) {
			return null;
		}
		return trackerPanel.getPlayer().getVideoClip();
	}
	
	@Override
	public boolean isVisible() {
		if (getLeader()!=this) {
			return getLeader().isVisible();
		}
		return super.isVisible();
	}
	
	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		if (getLeader()!=this && vis!=getLeader().isVisible()) {
			getLeader().setVisible(vis);
		}
		for (TTrack next: morePoints) {
			next.setVisible(vis);
		}
	}
	
	/**
	 * Gets the end data index.
	 * 
	 * @return the end index
	 */
  public int getEndIndex() {
  	// determine the end index corresponding to the end frame
  	int stepCount = getEndFrame()-getStartFrame();
  	int index = dataClip.getStartIndex() + stepCount*dataClip.getStride();
  	return Math.min(index, dataClip.getDataLength()-1);
  }

	/**
	 * Gets the (start) time for a given step.
	 * 
	 * @return the time
	 */
  public double getStepTime(int step) {
  	if (tData==null) return Double.NaN;
  	int index = getDataClip().stepToIndex(step);
  	if (index<tData.length) return tData[index];
  	return Double.NaN;
  }
  
	/**
	 * Determines if time is defined by the Data.
	 * 
	 * @return true if time data is available
	 */
  public boolean isTimeDataAvailable() {
  	if (dataClip==null || getVideoClip()==null) return false;
  	int n = Math.max(dataClip.getStride(), dataClip.getStartIndex());
  	return tData!=null && tData.length>n;
  }
  
	/**
	 * Gets the data-based video start time in seconds if available
	 * 
	 * @return the start time (assumed in seconds), or Double.NaN if unavailable
	 */
  public double getVideoStartTime() {
  	if (!isTimeDataAvailable()) return Double.NaN;
	  double t0 = tData[getDataClip().getStartIndex()];
	  double duration = getFrameDuration();
		return t0-duration*(getStartFrame()-getVideoClip().getStartFrameNumber());  	
  }

	/**
	 * Gets the data-based frame duration in seconds if available
	 * 
	 * @return the frame duration (assumed in seconds), or Double.NaN if unavailable
	 */
  public double getFrameDuration() {
  	if (!isTimeDataAvailable()) return Double.NaN;
		return tData[getDataClip().getStride()]-tData[0];
  }

  @Override
	public void setStartFrame(int n) {
		if (n==getStartFrame()) return;
		n = Math.max(n, 0); // not less than zero
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int end = clip.getFrameCount()-1;
		n = Math.min(n, end); // not greater than clip end
		startFrame = n;
		refreshInitialTime();
    extendVideoClip();
		lastValidFrame = -1;
		for (ParticleDataTrack next: morePoints) {
			next.lastValidFrame = -1;
		}
//		refreshSteps();
		trackerPanel.repaint();
		firePropertyChange("startframe", null, getStartFrame()); //$NON-NLS-1$
		if (trackerPanel!=null) {
			trackerPanel.getModelBuilder().refreshSpinners();
			Video video = trackerPanel.getVideo();
			if (video!=null) video.setFrameNumber(getStartFrame());
		}
	}
  
  @Override
	public void setEndFrame(int n) {
  	// set dataclip length
  	getDataClip().setClipLength((n-getStartFrame()+1));
  	trackerPanel.getModelBuilder().refreshSpinners();
	}
  
  @Override
	protected void refreshInitialTime() {
  	if (trackerPanel==null || trackerPanel.getPlayer()==null) {
	  	super.refreshInitialTime();
  		return;			
  	}
		if (!ClipControl.isTimeSource(this) || !isTimeDataAvailable()) {
	  	super.refreshInitialTime();
  		return;			
		}
		
		// this DataTrack is the current time source, so set it again to refresh values
		ClipControl clipControl = trackerPanel.getPlayer().getClipControl();
		clipControl.setTimeSource(this); // refreshes start time and frame duration

		// refresh init editor to show data start time
		Parameter param = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		double tZero = tData[getDataClip().getStartIndex()];
		String t = timeFormat.format(tZero);
		if (!timeFormat.format(param.getValue()).equals(t)) {
			boolean prev = refreshing;
			refreshing = true;
			getInitEditor().setExpression("t", t, false); //$NON-NLS-1$
			refreshing = prev;
		}
	}

  @Override
  public int getStartFrame() {
  	if (getLeader()!=this) {
  		return getLeader().getStartFrame();
  	}
		return startFrame;
	}
	
  @Override
	public int getEndFrame() {
  	// determine end frame based on start frame and clip length
  	int clipEnd = getStartFrame()+getDataClip().getClipLength()-1;
  	int videoEnd = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
  	return Math.min(clipEnd, videoEnd);
	}
  
	@Override
	Point2D[] getNextTracePositions() {
		stepCounter++;
		int videoStepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
		int modelStepNumber = stepCounter*videoStepSize;
		int index = getDataClip().stepToIndex(modelStepNumber);
		if (index>=xData.length || index>=yData.length) {
			return null;
		}
    point.setLocation(xData[index], yData[index]);
		return tracePosition;
	}
	
	@Override
  public void setColorToDefault(int index) {
  	super.setColorToDefault(index);
		for (TTrack next: morePoints) {
			next.setColor(this.getColor());
		}
		// set initial footprints too
		ArrayList<ParticleDataTrack> pts = allPoints();
		for (int i=0; i<pts.size(); i++) {
			TTrack next = pts.get(i);
	  	int m = Math.min(i, initialFootprintNames.size()-1);
	  	next.setFootprint(initialFootprintNames.get(m));
		}
  }

	@Override
	protected void setTrackerPanel(TrackerPanel panel) {
		super.setTrackerPanel(panel);
		for (TTrack next: morePoints) {
			if (next==this) continue;
			next.setTrackerPanel(panel);
		}
		if (panel==null) return;
		
		VideoClip videoClip = panel.getPlayer().getVideoClip();
		videoClip.addPropertyChangeListener(this);
		int length = videoClip.getLastFrameNumber()-videoClip.getFirstFrameNumber()+1;
		dataClip.setClipLength(Math.min(length, dataClip.getClipLength()));
		firePropertyChange("videoclip", null, null); //$NON-NLS-1$
		if (useDataTime) {
			panel.getPlayer().getClipControl().setTimeSource(this);
			firePropertyChange("timedata", null, null); //$NON-NLS-1$			
		}
	}
	
	public DatasetManager getData(TrackerPanel panel) {
		DatasetManager data = super.getData(panel);
		return data;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		// listen for changes to the video clip
		if (e.getSource() instanceof VideoClip || e.getPropertyName().equals("video")) { //$NON-NLS-1$
			if (e.getPropertyName().equals("frameshift")) { //$NON-NLS-1$
//				int frameshift = (Integer)e.getNewValue();
				VideoClip videoClip = getVideoPanel().getPlayer().getVideoClip();

				int startFrame = getStartFrame();
				startFrame = Math.max(startFrame, videoClip.getFirstFrameNumber());
				startFrame = Math.min(startFrame, videoClip.getLastFrameNumber());
						
				setStartFrame(startFrame);
			}
			firePropertyChange("videoclip", null, null); //$NON-NLS-1$
	    lastValidFrame = -1;
	    repaint();
		}
		// listen for changes to the dataclip
		else if (e.getSource()==dataClip) {
			refreshInitialTime();
	    extendVideoClip();
			firePropertyChange("dataclip", null, null); //$NON-NLS-1$
	    lastValidFrame = -1;
	    repaint();
		}
	}
	
	@Override
	protected void initializeFunctionPanel() {
		// create panel
		functionPanel = new ParticleDataTrackFunctionPanel(this);
		// create mass and initial time parameters
		createTimeParameter();
	}
	
  @Override
	protected void reset() {
		// clear existing steps
		for (int i=0; i<steps.length; i++) {
			Step step = steps.getStep(i);
			if (step!=null) {
				step.erase();
			}
			steps.setStep(i, null);
		}
		int index = getDataClip().stepToIndex(0);
    point.setLocation(xData[index], yData[index]);
		ImageCoordSystem coords = trackerPanel.getCoords();
    // get underlying coords if appropriate
    boolean useDefault = isUseDefaultReferenceFrame();
    while (useDefault && coords instanceof ReferenceFrame) {
      coords = ( (ReferenceFrame) coords).getCoords();
    }
		
	  int firstFrameInClip = getStartFrame();
		AffineTransform transform = coords.getToImageTransform(firstFrameInClip);
	  transform.transform(point, point);
	  
  	// mark a step at firstFrameInClip unless dataclip length is zero
  	steps.setLength(firstFrameInClip+1);
  	for (int i = 0; i<steps.length; i++) {
  		if (i<firstFrameInClip || dataClip.getClipLength()==0)
  			steps.setStep(i, null);
  		else {
  			PositionStep step = new PositionStep(this, firstFrameInClip, point.getX(), point.getY());
    		step.setFootprint(getFootprint());	  			
        steps.setStep(firstFrameInClip, step);
  		}	  			
  	}
  	
  	// reset v and a arrays
  	getVArray(trackerPanel).setLength(0);
  	getAArray(trackerPanel).setLength(0);
	  
  	// reset trace data
    traceX = new double[] {point.getX()};
    traceY = new double[] {point.getY()};
		lastValidFrame = firstFrameInClip;
		stepCounter = 0;
	}
  
	@Override
	public void setData(Data data, Object source) throws Exception {
		setData(data);
		setSource(source);
	}

	@Override
	public VideoPanel getVideoPanel() {
		return trackerPanel;
	}
	
	/**
	 * Gets the data array.
	 * 
	 * @return double[][] {x, y, t}
	 */
	public double[][] getDataArray() {
		return new double[][] {xData, yData, tData};
	}
	
	/**
	 * Informs this track that values have been appended to the Data.
	 * 
	 * @param data Data containing newly appended values
	 * @throws Exception if (x, y) data not found
	 */
	public void appendData(Data data) throws Exception {
		// following line throws exception if (x, y) not found
		Object[] pointData = getPointData(data).get(0);
		double[][] xyArray = (double[][])pointData[1];				
		double[][] oldData = getDataArray();
		int n = oldData[0].length;
		if (xyArray[0].length<=n) {
			// inform user that no new data was found
			TFrame frame = trackerPanel!=null? trackerPanel.getTFrame(): null;
			JOptionPane.showMessageDialog(frame, 
					TrackerRes.getString("ParticleDataTrack.Dialog.NoNewData.Message"), //$NON-NLS-1$
					TrackerRes.getString("ParticleDataTrack.Dialog.NoNewData.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		double[] timeArray = getTimeData(data); // may be null
		double[][] newData = new double[][] {xyArray[0], xyArray[1], timeArray};
		for (int i=0; i<newData.length; i++) {
			if (newData[i]!=null && oldData[i]!=null) {
				System.arraycopy(oldData[i], 0, newData[i], 0, n);
			}
		}
		sourceData = data;
		setData(newData, false);
	}
	
	/**
	 * Gets the time data from a Data object.
	 * 
	 * @param data the Data object
	 * @return the t array, or null if none found
	 */
	private static double[] getTimeData(Data data) {
		ArrayList<Dataset> datasets = data.getDatasets();
		for (Dataset dataset: datasets) {
			if (dataset.getXColumnName().toLowerCase().equals("t"))	return dataset.getXPoints(); //$NON-NLS-1$
			else if (dataset.getYColumnName().toLowerCase().equals("t"))	return dataset.getYPoints(); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Gets named (x, y) point data from a Data object.
	 * 
	 * @param data the Data object
	 * @return list of Object[] {String name, double[2][] xyData}
	 * @throws Exception if (x, y) data not defined, empty or inconsistent
	 */
	private static ArrayList<Object[]> getPointData(Data data) throws Exception {
		if (data==null) throw new Exception("Data is null"); //$NON-NLS-1$
		ArrayList<Dataset> datasets = data.getDatasets();
		if (datasets==null) throw new Exception("Data contains no datasets"); //$NON-NLS-1$

		ArrayList<Object[]> results = new ArrayList<Object[]>();
//		boolean foundX = false, foundY = false;
		String colName = null;
		Dataset prevDataset = null;
		for (Dataset dataset: datasets) {
			
			// look for columns with paired xy names
			double[][] xy = new double[2][];
			if (colName==null) {
				if (xy[0]==null && dataset.getXColumnName().toLowerCase().startsWith("x"))	{ //$NON-NLS-1$
					colName = dataset.getXColumnName().substring(1).trim();
					xy[0] = dataset.getXPoints();
				}
				else if (xy[0]==null && dataset.getYColumnName().toLowerCase().startsWith("x")) { //$NON-NLS-1$
					colName = dataset.getXColumnName().substring(1).trim();
					xy[0] = dataset.getYPoints();
				}
				if (xy[1]==null && dataset.getXColumnName().toLowerCase().startsWith("y"))	{ //$NON-NLS-1$
					if (colName==null) {
						xy[1] = dataset.getXPoints();
						colName = dataset.getXColumnName().substring(1).trim();
					}
					else if (dataset.getXColumnName().substring(1).equals(colName)) {
						// match
						xy[1] = dataset.getXPoints();
					}
				}
				else if (xy[1]==null && dataset.getYColumnName().toLowerCase().startsWith("y")) { //$NON-NLS-1$
					if (colName==null) {
						colName = dataset.getYColumnName().substring(1).trim();
						xy[1] = dataset.getYPoints();
					}
					else if (dataset.getYColumnName().substring(1).equals(colName)) {
						// match
						xy[1] = dataset.getYPoints();
					}
				}
			}
			
			// if all data are present, add to results and continue to next dataset
			if (xy[0]!=null && xy[1]!=null && colName!=null) {
				results.add(new Object[] {colName, xy});
				colName = null;
				continue;
			}
			
			// not all data is present
			if (colName!=null && prevDataset!=null) { // partial data is present, so look at previous dataset
				if (xy[0]==null && prevDataset.getXColumnName().toLowerCase().startsWith("x") //$NON-NLS-1$
						&& prevDataset.getXColumnName().substring(1).equals(colName))	{
					xy[0] = prevDataset.getXPoints();
				}
				else if (xy[0]==null && prevDataset.getYColumnName().toLowerCase().startsWith("x") //$NON-NLS-1$
						&& prevDataset.getYColumnName().substring(1).equals(colName))	{
					xy[0] = prevDataset.getYPoints();
				}
				if (xy[1]==null && prevDataset.getXColumnName().toLowerCase().startsWith("y") //$NON-NLS-1$
						&& prevDataset.getXColumnName().substring(1).equals(colName))	{
					xy[1] = prevDataset.getXPoints();
				}
				else if (xy[1]==null && prevDataset.getYColumnName().toLowerCase().startsWith("y") //$NON-NLS-1$
						&& prevDataset.getYColumnName().substring(1).equals(colName))	{
					xy[1] = prevDataset.getYPoints();
				}				
			}
			
			prevDataset = dataset;
			// if all data are present, add to results
			if (xy[0]!=null && xy[1]!=null && colName!=null) {
				results.add(new Object[] {colName, xy});
				prevDataset = null;
			}
			
			colName = null;
		}	// end for loop
		
		if (results.isEmpty()) {
			throw new Exception("Position data (x, y) not defined"); //$NON-NLS-1$
		}
		// check first data array for matching data length, etc
		Object[] result = results.get(0);
		double[][] dataArray = (double[][])result[1];
		
		if (dataArray[0].length==0 || dataArray[1].length==0) {
			throw new Exception("Position data is empty"); //$NON-NLS-1$
		}
		if (dataArray[0].length!=dataArray[1].length) {
			throw new Exception("X and Y data have different array lengths"); //$NON-NLS-1$
		}
		
		return results;
	}
	

  //___________________________________  private methods ____________________________
  
	/**
	 * This adds the initial time parameter to the function panel.
	 */
	private void createTimeParameter() {		
		Parameter param = new Parameter("t", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		param.setNameEditable(false);
		param.setDescription(TrackerRes.getString("ParticleModel.Parameter.InitialTime.Description")); //$NON-NLS-1$
		functionPanel.getInitEditor().addObject(param, false);
		getInitEditor().addPropertyChangeListener(new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent e) {
		  	if (refreshing) return;
		  	if ("t".equals(e.getOldValue()) && trackerPanel != null) { //$NON-NLS-1$
		  		Parameter param = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		      double timeOffset = param.getValue()*1000 - clip.getStartTime();
		      double dt = trackerPanel.getPlayer().getMeanStepDuration();
		      int n = clip.getStartFrameNumber();
		      boolean mustRound = timeOffset%dt>0;
		      n += clip.getStepSize()*(int)Math.round(timeOffset/dt);
		      setStartFrame(n);
		      if (getStartFrame()!=n || mustRound)
		      	Toolkit.getDefaultToolkit().beep();
		  	}
		  }
		});
	}
	
	/**
	 * Sets the data as array {x, y, t}. If time data is included, it is assumed to be in seconds.
	 * The t array may be null, but x and y are required.
	 * 
	 * @param data the data array {x, y, t}
	 * @param reset true to redraw all frames
	 */
	private void setData(double[][] data, boolean reset) {
		xData = data[0];
		yData = data[1];
		tData = data.length>2? data[2]: null;
		getDataClip().setDataLength(data[0].length);
		firePropertyChange("dataclip", null, dataClip); //$NON-NLS-1$
    extendVideoClip();
		if (reset) {
			lastValidFrame = -1;
			refreshSteps();
			firePropertyChange("steps", null, null); //$NON-NLS-1$
		}
		invalidWarningShown = true;
		repaint();
	}
	
	/**
	 * Extends the video clip if it currently ends at the last frame 
	 * and the data clip extends past that point.
	 * 
	 * @return true if the video clip was extended
	 */
	private boolean extendVideoClip() {
		if (trackerPanel==null) return false;
		// determine if video clip ends at last frame
		VideoClip vidClip = trackerPanel.getPlayer().getVideoClip();
		int videoEndFrame = vidClip.getEndFrameNumber();
		boolean isLast = videoEndFrame==vidClip.getLastFrameNumber();
		int dataEndFrame = getStartFrame()+getDataClip().getAvailableClipLength()-1;
		if (isLast && dataEndFrame>videoEndFrame) {
			vidClip.extendEndFrameNumber(dataEndFrame);
		}
		return false;
	}
	
//___________________________________ inner classes _________________________________
	
	class ComboIcon implements Icon {
		
		ArrayList<ShapeIcon> shapeIcons;
		
		ComboIcon(ArrayList<ShapeIcon> icons) {
			shapeIcons = icons;
		}
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (shapeIcons.size()==1) {
				shapeIcons.get(0).paintIcon(c, g, x, y);
			}
			else {
				Graphics2D g2 = (Graphics2D)g;
				AffineTransform restoreTransform = g2.getTransform();
				int w = getIconWidth();
				int h = getIconHeight();
				g2.scale(0.7, 0.7);
				int n = shapeIcons.size();
				for (int i=0; i<n; i++) {
					if (i%2==0) { // even points above
						shapeIcons.get(i).paintIcon(c, g, x+i*w/(n), y);
					}
					else { // odd points below
						shapeIcons.get(i).paintIcon(c, g, x+i*w/(n), y+h/2);
					}					
				}
				g2.setTransform(restoreTransform);
			}
		}
	
		@Override
		public int getIconWidth() {
			return shapeIcons.get(0).getIconWidth();
		}
	
		@Override
		public int getIconHeight() {
			return shapeIcons.get(0).getIconHeight();
		}
		
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

    public void saveObject(XMLControl control, Object obj) {
      ParticleDataTrack dataTrack = (ParticleDataTrack)obj;
      // save mass
      control.setValue("mass", dataTrack.getMass()); //$NON-NLS-1$
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      //save model name as name
      control.setValue("name", dataTrack.modelName); //$NON-NLS-1$
//      // save initial values
//      Parameter[] inits = model.getInitEditor().getParameters();
//    	control.setValue("initial_values", inits); //$NON-NLS-1$
      // save the data
	    control.setValue("x", dataTrack.xData); //$NON-NLS-1$
	    control.setValue("y", dataTrack.yData); //$NON-NLS-1$
	    control.setValue("t", dataTrack.tData); //$NON-NLS-1$
	    // save point name
	    control.setValue("pointname", dataTrack.pointName); //$NON-NLS-1$
	    // save additional point data: x, y, mass, point name, color, footprint
	    for (int i=0; i<dataTrack.morePoints.size(); i++) {
	    	ParticleDataTrack pointTrack = dataTrack.morePoints.get(i);
	      // save the data
		    control.setValue("x"+i, pointTrack.xData); //$NON-NLS-1$
		    control.setValue("y"+i, pointTrack.yData); //$NON-NLS-1$
		    // save point name
	      control.setValue("mass"+i, pointTrack.getMass()); //$NON-NLS-1$
		    control.setValue("pointname"+i, pointTrack.pointName); //$NON-NLS-1$
	      // save color
	      control.setValue("color"+i, pointTrack.getColor()); //$NON-NLS-1$
	      // footprint name
	      Footprint fp = pointTrack.getFootprint();
	      String s = fp.getName();
	      if (fp instanceof CircleFootprint) {
	      	CircleFootprint cfp = (CircleFootprint)fp;
	      	s+="#"+cfp.getProperties(); //$NON-NLS-1$
	      }
	      control.setValue("footprint"+i, s); //$NON-NLS-1$
	    }
	    
	    // save the dataclip
	    control.setValue("dataclip", dataTrack.getDataClip()); //$NON-NLS-1$
      // save start and end frames (if custom)
      if (dataTrack.getStartFrame()>0)
      	control.setValue("start_frame", dataTrack.getStartFrame()); //$NON-NLS-1$
	    // save useDataTime flag
	    control.setValue("use_data_time", ClipControl.isTimeSource(dataTrack)); //$NON-NLS-1$
  		// save inspector size and position
  		if (dataTrack.inspector != null &&
  						dataTrack.trackerPanel != null && 
  						dataTrack.trackerPanel.getTFrame() != null) {
  			// save inspector location relative to frame
  			TFrame frame = dataTrack.trackerPanel.getTFrame();
  			int x = dataTrack.inspector.getLocation().x - frame.getLocation().x;
  			int y = dataTrack.inspector.getLocation().y - frame.getLocation().y;
    		control.setValue("inspector_x", x); //$NON-NLS-1$
    		control.setValue("inspector_y", y); //$NON-NLS-1$  			
    		control.setValue("inspector_h", dataTrack.inspector.getHeight()); //$NON-NLS-1$ 
    		control.setValue("inspector_visible", dataTrack.inspector.isVisible()); //$NON-NLS-1$
  		}
    }

    public Object createObject(XMLControl control){
    	double[][] coreData = new double[3][];
    	coreData[0] = (double[])control.getObject("x"); //$NON-NLS-1$
    	coreData[1] = (double[])control.getObject("y"); //$NON-NLS-1$
    	coreData[2] = (double[])control.getObject("t"); //$NON-NLS-1$
    	int i = 0;
    	ArrayList<Object[]> pointData = new ArrayList<Object[]>();
    	double[][] next = new double[2][];
    	next[0] = (double[])control.getObject("x"+i); //$NON-NLS-1$
    	while (next[0]!=null) {
    		next[1] = (double[])control.getObject("y"+i); //$NON-NLS-1$)
    		String name = control.getString("pointname"+i); //$NON-NLS-1$
    		pointData.add(new Object[] {name, next});
    		i++;
    		next = new double[2][];
    		next[0] = (double[])control.getObject("x"+i); //$NON-NLS-1$
    	}
      return new ParticleDataTrack(coreData, pointData);
    }

    public Object loadObject(XMLControl control, Object obj) {
    	ParticleDataTrack dataTrack = (ParticleDataTrack)obj;
      // load track data and mass
      XML.getLoader(TTrack.class).loadObject(control, obj);
      dataTrack.mass = control.getDouble("mass"); //$NON-NLS-1$
      // load pointname
      dataTrack.setPointName(control.getString("pointname")); //$NON-NLS-1$
      // load dataclip
      XMLControl dataClipControl = control.getChildControl("dataclip"); //$NON-NLS-1$
      if (dataClipControl!=null) {
      	dataClipControl.loadObject(dataTrack.getDataClip());
      }
      // load point properties: mass, color, footprint
      for (int i=0; i<dataTrack.morePoints.size(); i++) {
      	ParticleDataTrack child = dataTrack.morePoints.get(i);
      	child.setMass(control.getDouble("mass"+i)); //$NON-NLS-1$
      	child.setColor((Color)control.getObject("color"+i)); //$NON-NLS-1$
      	child.setFootprint(control.getString("footprint"+i)); //$NON-NLS-1$
      }
      // load useDataTime flag
      dataTrack.useDataTime = control.getBoolean("use_data_time"); //$NON-NLS-1$
      // load start frame
      int n = control.getInt("start_frame"); //$NON-NLS-1$
  		if (n!=Integer.MIN_VALUE)
  			dataTrack.startFrame = n;
  		else {
  			dataTrack.startFrameUndefined = true;
  		}
      dataTrack.inspectorX = control.getInt("inspector_x"); //$NON-NLS-1$
      dataTrack.inspectorY = control.getInt("inspector_y"); //$NON-NLS-1$
      dataTrack.inspectorH = control.getInt("inspector_h"); //$NON-NLS-1$
      dataTrack.showInspector = control.getBoolean("inspector_visible"); //$NON-NLS-1$
      return dataTrack;
    }
  }

}
