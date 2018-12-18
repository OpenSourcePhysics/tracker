package org.opensourcephysics.cabrillo.tracker;

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
	 * Gets the image of current frame
	 */
	BufferedImage getImage();

	/**
	 * @return true, if can perform step(), false otherwise
	 */
	boolean canStep();
}
