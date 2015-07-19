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
 * The Data object is an "external model" associated with a source (path, URL, Tool)
 * The Data must define data arrays "x" and "y" and may include a clock array "t" 
 * The Data may include additional data array pairs "x1", "y1", etc.
 *
 * @author Douglas Brown
 */
public class ParticleDataTrack extends ParticleModel implements DataTrack {
	// pig test this with video with frameshift
	
	private DataClip dataClip;
	private Data sourceData;
  private double[] xData={0}, yData={0}, tData={0};
  private Point2D[] tracePosition; // used by getNextTracePositions() method
  private int stepCounter;
  private Object dataSource;
  private boolean useDataTime;
	
	/**
	 * Constructor.
	 * 
	 * @param data the Data object
	 * @param source the data source object (null if data is pasted)
	 * @throws Exception if the data does not define x and y-datasets
	 */
	ParticleDataTrack(Data data, Object source) throws Exception {
		dataSource = source;
		getDataClip().addPropertyChangeListener(this);
		tracePosition = new Point2D[] {point};
		tracePtsPerStep = 1;
		// next line throws Exception if the data does not define x and y-columns
		setData(data);
	}
	
	/**
	 * Constructor for XMLLoader.
	 * 
	 * @param data the Data object
	 */
	private ParticleDataTrack(double[] x, double[] y, double[] t) {
		getDataClip().addPropertyChangeListener(this);
		tracePosition = new Point2D[] {point};
		tracePtsPerStep = 1;
		try {
			setData(new double[][] {x, y, t}, true);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Sets the Data. Data must define columns "x" and "y".
	 * If time data is included, it is assumed to be in seconds.
	 * 
	 * @param data the Data object
	 * @throws Exception if the data does not define x and y-columns
	 */
	public void setData(Data data) throws Exception {
		// the following line throws an exception if (x, y) data is not found
		double[][] dataArray = getDataArray(data);
		
		OSPLog.fine("Setting new data"); //$NON-NLS-1$
		sourceData = data;
		setData(dataArray, true);
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
	  double t0 = tData[dataClip.getStartIndex()];
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
		return tData[dataClip.getStride()]-tData[0];
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
		double tZero = tData[dataClip.getStartIndex()];
		String t = timeFormat.format(tZero);
		if (!timeFormat.format(param.getValue()).equals(t)) {
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
		int index = dataClip.stepToIndex(0);
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
		double[][] newData = ParticleDataTrack.getDataArray(data);
		double[][] oldData = getDataArray();
		int n = oldData[0].length;
		if (newData[0].length<=n) {
			// inform user that no new data was found
			TFrame frame = trackerPanel!=null? trackerPanel.getTFrame(): null;
			JOptionPane.showMessageDialog(frame, 
					TrackerRes.getString("ParticleDataTrack.Dialog.NoNewData.Message"), //$NON-NLS-1$
					TrackerRes.getString("ParticleDataTrack.Dialog.NoNewData.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		for (int i=0; i<newData.length; i++) {
			if (newData[i]!=null && oldData[i]!=null) {
				System.arraycopy(oldData[i], 0, newData[i], 0, n);
			}
		}
		sourceData = data;
		setData(newData, false);
	}
	
	/**
	 * Gets the {x, y, t} data array from a Data object.
	 * 
	 * @param data the Data object
	 * @return the data array {x, y, t}
	 * @throws Exception if (x, y) data not defined, empty or inconsistent
	 */
	public static double[][] getDataArray(Data data) throws Exception {
		if (data==null) throw new Exception("Data is null"); //$NON-NLS-1$
		ArrayList<Dataset> datasets = data.getDatasets();
		if (datasets==null) throw new Exception("Data contains no datasets"); //$NON-NLS-1$
		double[][] array = new double[3][];
		for (Dataset dataset: datasets) {
			if (array[0]==null) {
				if (dataset.getXColumnName().equals("x"))	array[0] = dataset.getXPoints(); //$NON-NLS-1$
				else if (dataset.getYColumnName().equals("x"))	array[0] = dataset.getYPoints(); //$NON-NLS-1$
			}
			if (array[1]==null) {
				if (dataset.getXColumnName().equals("y"))	array[1] = dataset.getXPoints(); //$NON-NLS-1$
				else if (dataset.getYColumnName().equals("y"))	array[1] = dataset.getYPoints(); //$NON-NLS-1$
			}
			if (array[2]==null) {
				if (dataset.getXColumnName().equals("t"))	array[2] = dataset.getXPoints(); //$NON-NLS-1$
				else if (dataset.getYColumnName().equals("t"))	array[2] = dataset.getYPoints(); //$NON-NLS-1$
			}
		}
		if (array[0]==null || array[1]==null) {
			throw new Exception("Position data (x, y) not defined"); //$NON-NLS-1$
		}
		if (array[0].length==0 || array[1].length==0) {
			throw new Exception("Position data is empty"); //$NON-NLS-1$
		}
		if (array[0].length!=array[1].length) {
			throw new Exception("X and Y data have different array lengths"); //$NON-NLS-1$
		}
		if (array[2]!=null && array[0].length!=array[2].length) {
			throw new Exception("Time data has incorrect array length"); //$NON-NLS-1$
		}
		return array;
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
	 * Sets the data. If time data is included, it is assumed to be in seconds.
	 * 
	 * @param x the x array
	 * @param y the y array
	 * @param t the t array (may be null)
	 * @param reset true to redraw all frames
	 */
	private void setData(double[][] data, boolean reset) {
		xData = data[0];
		yData = data[1];
		tData = data[2];
		dataClip.setDataLength(data[0].length);
		firePropertyChange("dataclip", null, dataClip); //$NON-NLS-1$
    extendVideoClip();
		if (reset) {
			lastValidFrame = -1;
			refreshSteps();
			firePropertyChange("steps", null, null); //$NON-NLS-1$
		}
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
		int dataEndFrame = getStartFrame()+dataClip.getAvailableClipLength()-1;
		if (isLast && dataEndFrame>videoEndFrame) {
			vidClip.extendEndFrameNumber(dataEndFrame);
		}
		return false;
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
//      // save initial values
//      Parameter[] inits = model.getInitEditor().getParameters();
//    	control.setValue("initial_values", inits); //$NON-NLS-1$
      // save the data
	    control.setValue("x", dataTrack.xData); //$NON-NLS-1$
	    control.setValue("y", dataTrack.yData); //$NON-NLS-1$
	    control.setValue("t", dataTrack.tData); //$NON-NLS-1$
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
    	double[] x = (double[])control.getObject("x"); //$NON-NLS-1$
    	double[] y = (double[])control.getObject("y"); //$NON-NLS-1$
    	double[] t = (double[])control.getObject("t"); //$NON-NLS-1$
      return new ParticleDataTrack(x, y, t);
    }

    public Object loadObject(XMLControl control, Object obj) {
    	ParticleDataTrack dataTrack = (ParticleDataTrack)obj;
      // load track data and mass
      XML.getLoader(TTrack.class).loadObject(control, obj);
      dataTrack.mass = control.getDouble("mass"); //$NON-NLS-1$
      // load dataclip
      XMLControl dataClipControl = control.getChildControl("dataclip"); //$NON-NLS-1$
      if (dataClipControl!=null) {
      	dataClipControl.loadObject(dataTrack.getDataClip());
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
