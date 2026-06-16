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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

/**
 * A MotionFilter pre-processes a 1-D signal (such as worldspace x or y position) before
 * derivatives are computed. Implementations should operate independently on each
 * contiguous run of indices flagged true in {@code valid}; entries flagged false
 * must remain unchanged.
 *
 * @author Tracker MotionFilter contribution
 */
public interface MotionFilter {

	/**
	 * Returns a filtered copy of the input. The returned array has the same length as
	 * {@code data}. Indices where {@code valid[i]} is false carry over unchanged from
	 * {@code data[i]}.
	 *
	 * @param data the input signal
	 * @param valid which entries are valid (must have same length as data)
	 * @return a new array containing the filtered signal
	 */
	double[] apply(double[] data, boolean[] valid);

	/**
	 * @return a deep copy of this filter
	 */
	MotionFilter copy();

}
