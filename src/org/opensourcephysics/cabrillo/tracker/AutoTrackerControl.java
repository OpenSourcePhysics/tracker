package org.opensourcephysics.cabrillo.tracker;

import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;

import java.awt.image.BufferedImage;

public interface AutoTrackerControl {

	/**
	 * Steps one frame forward
	 */
	void step();

	/**
	 * @return number of current frame
	 */
	int getFrameNumber();

	/**
	 * @return number of current frame
	 * according to the point p
	 */
	int getFrameNumber(TPoint p);

	/**
	 * Gets the image of current frame
	 */
	BufferedImage getImage();

	/**
	 * @return true, if can perform step(), false otherwise
	 */
	boolean canStep();

	/**
	 * @return true if the video should be played in reverted direction
	 */
	boolean isReverse();

	/**
	 * Converts step number to frame number.
	 *
	 * @param stepNumber the step number
	 * @return the frame number
	 */
	int stepToFrame(int stepNumber);

	/**
	 * Converts frame number to step number. A frame number that falls
	 * between two steps maps to the previous step.
	 *
	 * @param frameNumber the frame number
	 * @return the step number
	 */
	int frameToStep(int frameNumber);

	/**
	 * @return total quantity of frames
	 */
	int getFrameCount();

	/**
	 * @return ImageCoordinateSystem
	 */
	ImageCoordSystem getCoords();

	boolean isVideoValid();
}
