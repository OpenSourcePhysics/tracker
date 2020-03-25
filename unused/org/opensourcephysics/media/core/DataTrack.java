package org.opensourcephysics.media.core;

import java.beans.PropertyChangeListener;

import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.DataClip;

/**
 * An interface for a Trackable object that gets its position (and optional time)
 * data from an external source. Since it is drawn on a TrackerPanel, DataTrack
 * includes methods related to the associated Video (start frame) and VideoPanel
 * as well as the Data (DataClip, source, time, etc).
 * 
 * @author Douglas Brown
 */
public interface DataTrack extends Trackable {

	/**
	 * Sets the video start frame at which the first data point is displayed.
	 * 
	 * @param start the start frame number
	 */
	public void setStartFrame(int start);

	/**
	 * Gets the video start frame at which the first data point is displayed.
	 * 
	 * @return the start frame number
	 */
	public int getStartFrame();
	
	/**
	 * Sets the Data for the track. Data must define "x" and "y" positions,
	 * and may define "t". Optional source may be a JPanel control panel.
	 * 
	 * @param data the Data
	 * @param source a source object (may be null)
	 */
	public void setData(Data data, Object source) throws Exception;
	
	/**
	 * Gets the Data.
	 * 
	 * @return the Data
	 */
	public Data getData();
	
	/**
	 * Gets the source.
	 * 
	 * @return the source (may be null)
	 */
	public Object getSource();
	
	/**
	 * Gets the DataClip, which defines the start index, stepcount and stride.
	 * 
	 * @return the DataClip
	 */
	public DataClip getDataClip(); // defines start index, clip length, stride
	
	/**
	 * Gets the VideoPanel on which this DataTrack is drawn.
	 * 
	 * @return the VideoPanel
	 */
	public VideoPanel getVideoPanel();  // use to add (and control) a video. 
	
	/**
	 * Adds a PropertyChangeListener.
	 * 
	 * @param listener the PropertyChangeListener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * Determines if time is defined by the Data.
	 * 
	 * @return true if time data is available
	 */
  public boolean isTimeDataAvailable();

	/**
	 * Gets the data-based video start time in seconds if available
	 * 
	 * @return the start time, or Double.NaN if undefined
	 */
  public double getVideoStartTime();

	/**
	 * Gets the data-based video frame duration in seconds if available
	 * 
	 * @return the frame duration, or Double.NaN if undefined
	 */
  public double getFrameDuration();

}
