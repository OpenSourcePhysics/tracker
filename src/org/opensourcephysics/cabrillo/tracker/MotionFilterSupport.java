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

import java.util.ArrayList;
import java.util.List;

/**
 * Static methods to support {@link MotionFilter} implementations.
 *
 * @author Tracker Filter contribution
 */
final class MotionFilterSupport {

	private MotionFilterSupport() {}

	/**
	 * Index range [start, end) inclusive of start and exclusive of end.
	 */
	static final class Segment {
		final int start;
		final int end;
		Segment(int start, int end) { this.start = start; this.end = end; }
		int length() { return end - start; }
	}

	/**
	 * Splits {@code valid} into contiguous runs of true entries.
	 */
	static List<Segment> contiguousValidSegments(boolean[] valid) {
		List<Segment> result = new ArrayList<>();
		int i = 0;
		while (i < valid.length) {
			while (i < valid.length && !valid[i]) i++;
			int start = i;
			while (i < valid.length && valid[i]) i++;
			if (i > start) result.add(new Segment(start, i));
		}
		return result;
	}

	/**
	 * Reflect-pad the array {@code src[start..end)} on each side by {@code pad} entries
	 * using the OSP/SciPy "odd" reflection (mirror, value-reflect not gradient-reflect):
	 * out[k] = 2*src[start] - src[start+pad-k] for the left pad. Returns a new array of
	 * length (end-start) + 2*pad.
	 */
	static double[] reflectPad(double[] src, int start, int end, int pad) {
		int n = end - start;
		double[] out = new double[n + 2 * pad];
		for (int i = 0; i < n; i++) out[pad + i] = src[start + i];
		double s0 = src[start];
		double sN = src[end - 1];
		for (int k = 0; k < pad; k++) {
			int srcIdx = Math.min(n - 1, k + 1);
			out[pad - 1 - k] = 2 * s0 - src[start + srcIdx];
			int srcIdx2 = Math.max(0, n - 2 - k);
			out[pad + n + k] = 2 * sN - src[start + srcIdx2];
		}
		return out;
	}

}
