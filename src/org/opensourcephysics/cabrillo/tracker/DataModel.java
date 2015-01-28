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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.DataClip;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.Parameter;

/**
 * This is a particle model with steps based on world positions defined in a Data object.
 * The Data object is an "external model" associated with a source (path, URL, Tool)
 * The Data must define data arrays "x" and "y" and may include a clock array "t" 
 * The Data may include additional data array pairs "x1", "y1", etc.
 *
 * @author Douglas Brown
 */
//public class DataModel extends ParticleModel implements Tool {
public class DataModel extends ParticleModel {
	// pig test this with video with frameshift
	
	private DataClip dataClip;
	private Data myData;
  private double[] xData, yData, tData;
  private Point2D[] tracePosition; // used by getNextTracePositions() method
  private int stepCounter;
  private boolean useDataTime = false;
  private Object dataSource;
	
	/**
	 * Constructor.
	 * 
	 * @param data the Data object
	 * @param source the data source object (null if data is pasted)
	 * @throws Exception if the data does not define x and y-datasets
	 */
	DataModel(Data data, Object source) throws Exception {
		dataSource = source;
		getDataClip().addPropertyChangeListener(this);
		// setData() may throw exception
		setData(data);
		tracePosition = new Point2D[] {point};
		tracePtsPerStep = 1;
	}
	
	/**
	 * Sets the Data. Data must define datasets "x" and "y".
	 * If time data is included, it is assumed to be in seconds.
	 * 
	 * @param data the Data object
	 * @throws Exception if the data does not define x and y-datasets
	 */
	public void setData(Data data) throws Exception {
		if (data==null) throw new Exception("Data is null"); //$NON-NLS-1$
		ArrayList<Dataset> datasets = data.getDatasets();
		if (datasets== null) throw new Exception("Data contains no datasets"); //$NON-NLS-1$
		double[] prevxData = xData;
		double[] prevyData = yData;
		double[] prevtData = tData;
		xData = yData = tData = null;
		for (Dataset dataset: datasets) {
			if (xData==null) {
				if (dataset.getXColumnName().equals("x"))	xData = dataset.getXPoints(); //$NON-NLS-1$
				else if (dataset.getYColumnName().equals("x"))	xData = dataset.getYPoints(); //$NON-NLS-1$
			}
			if (yData==null) {
				if (dataset.getXColumnName().equals("y"))	yData = dataset.getXPoints(); //$NON-NLS-1$
				else if (dataset.getYColumnName().equals("y"))	yData = dataset.getYPoints(); //$NON-NLS-1$
			}
			if (tData==null) {
				if (dataset.getXColumnName().equals("t"))	tData = dataset.getXPoints(); //$NON-NLS-1$
				else if (dataset.getYColumnName().equals("t"))	tData = dataset.getYPoints(); //$NON-NLS-1$
			}
		}
		if (xData==null || yData==null) {
			xData = prevxData;
			yData = prevyData;
			tData = prevtData;
			throw new Exception("No position data found"); //$NON-NLS-1$
		}
		int length = Math.min(xData.length, yData.length);
		if (tData!=null) {
			length = Math.min(length, tData.length);
		}
		dataClip.setDataLength(length);
		myData = data;
		firePropertyChange("dataclip", null, dataClip); //$NON-NLS-1$
		lastValidFrame = -1;
		if (trackerPanel!=null) trackerPanel.repaint();
	}
	
	/**
	 * Gets the data source.
	 * 
	 * @return the source
	 */
	public Object getSource() {
		return dataSource;
	}
	
	/**
	 * Gets the external data.
	 * 
	 * @return the data
	 */
	public Data getExternalData() {
		return myData;
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
	
	/**
	 * Gets the data frame duration, or NaN if no time data available.
	 * 
	 * @return the frame duration (assumed in seconds)
	 */
	public double getDataFrameDuration() {
		if (tData==null || tData.length<dataClip.getStride()+1) {
			return Double.NaN;
		}
		return tData[dataClip.getStride()]-tData[0];
	}
	
	/**
	 * Gets the start time (data time at index corresponding to video frame 0),
	 * or NaN if no time data available.
	 * 
	 * @return the start time (assumed in seconds)
	 */
	public double getDataStartTime() {
		if (tData==null || tData.length<dataClip.getStartIndex()+1) {
			return Double.NaN;
		}
		int start = getStartFrame();
		double t = tData[dataClip.getStartIndex()];
		return t-start*getDataFrameDuration();
	}
	
//	@Override
//	public void send(Job job, Tool replyTo) throws RemoteException {
//		// implements Tool
//    XMLControlElement control = new XMLControlElement();
//    try {
//      control.readXML(job.getXML());
//    } catch (RemoteException ex) {ex.printStackTrace();}
//    Iterator<Data> it = control.getObjects(Data.class).iterator();
//    while (it.hasNext()) {
//      Data newData = it.next();
//      try {
//				setData(newData); // throws exception if fails
//				break; // load only one
//			} catch (Exception e) {
//			}
//    }
//    repaint();
//	}
//
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
  
  public void setDataTime(boolean useData) {
  	useDataTime = useData;
  	refreshInitialTime();
  }

  public boolean isDataTime() {
  	return useDataTime && isDataTimeEnabled();
  }

  public boolean isDataTimeEnabled() {
  	return tData!=null && tData.length>1;
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
		lastValidFrame = -1;
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
  	dataClip.setClipLength((n-getStartFrame()+1));
  	trackerPanel.getModelBuilder().refreshSpinners();
	}
  
  @Override
	protected void refreshInitialTime() {
  	if (!isDataTime()) {
	  	super.refreshInitialTime();
  		return;
  	}
  		// set video frame duration and start time to correspond to current data clip
	  double t0 = getDataStartTime()*1000; // convert s to ms
	  double duration = getDataFrameDuration()*1000; // convert s to ms
		if (Double.isNaN(t0) || Double.isNaN(duration)) {
			super.refreshInitialTime();
			return;
		}
		VideoPlayer player = trackerPanel.getPlayer();
		player.getVideoClip().setStartTime(t0);
		player.getClipControl().setFrameDuration(duration);
		
		// refresh init editor
		String t = timeFormat.format(t0/1000);
		Parameter param = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		if (param.getValue() != t0) {
			boolean prev = refreshing;
			refreshing = true;
			getInitEditor().setExpression("t", t, false); //$NON-NLS-1$
			refreshing = prev;
		}
	}

  @Override
	public int getEndFrame() {
  	// determine end frame based on start frame and clip length
  	int clipEnd = getStartFrame()+dataClip.getClipLength()-1;
  	int videoEnd = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
  	return Math.min(clipEnd, videoEnd);
	}
  
	@Override
	Point2D[] getNextTracePositions() {		
		stepCounter++;
		int videoStepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
		int modelStepNumber = stepCounter*videoStepSize;
		int index = dataClip.stepToIndex(modelStepNumber);
		if (index>=xData.length || index>=yData.length) {
			return null;
		}
    point.setLocation(xData[index], yData[index]);
		return tracePosition;
	}
	
	@Override
	protected void setTrackerPanel(TrackerPanel panel) {
		super.setTrackerPanel(panel);
		
		VideoClip videoClip = panel.getPlayer().getVideoClip();
		videoClip.addPropertyChangeListener(this);
		int length = videoClip.getLastFrameNumber()-videoClip.getFirstFrameNumber()+1;
		dataClip.setClipLength(Math.min(length, dataClip.getClipLength()));
		firePropertyChange("videoclip", null, null); //$NON-NLS-1$
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		// listen for changes to the video clip
		if (e.getSource() instanceof VideoClip || e.getPropertyName().equals("video")) { //$NON-NLS-1$
			firePropertyChange("videoclip", null, null); //$NON-NLS-1$
		}
		// listen for changes to the dataclip
		if (e.getSource()==dataClip) {
			refreshInitialTime();
			firePropertyChange("dataclip", null, null); //$NON-NLS-1$
		}
	}
	
	@Override
	protected void initializeFunctionPanel() {
		// create panel
		functionPanel = new DataModelFunctionPanel(this);
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
		int index = dataClip.stepToIndex(0);
    point.setLocation(xData[index], yData[index]);
		ImageCoordSystem coords = trackerPanel.getCoords();
		// get underlying coords if reference frame
		while (coords instanceof ReferenceFrame) {
		  coords = ( (ReferenceFrame) coords).getCoords();
		}
		
	  int firstFrameInClip = getStartFrame();
		AffineTransform transform = coords.getToImageTransform(firstFrameInClip);
	  transform.transform(point, point);
	  
  	// mark a step at firstFrameInClip
  	steps.setLength(firstFrameInClip+1);
    PositionStep step = (PositionStep)getStep(0);
  	for (int i = 0; i<steps.length; i++) {
  		if (i<firstFrameInClip)
  			steps.setStep(i, null);
  		else {
    		step = new PositionStep(this, firstFrameInClip, point.getX(), point.getY());
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
}
