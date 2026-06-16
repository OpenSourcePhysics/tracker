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

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * Symmetric centered moving-average (boxcar) filter. The window is reduced near
 * segment boundaries so output length is preserved. Window must be odd; an even value
 * is rounded up.
 *
 * @author Tracker Filter contribution
 */
public class MovingAverageFilter implements MotionFilter {

	private int window;

	public MovingAverageFilter() {
		this(5);
	}

	public MovingAverageFilter(int window) {
		setWindow(window);
	}

	public int getWindow() {
		return window;
	}

	public final void setWindow(int w) {
		if (w < 1) w = 1;
		if (w % 2 == 0) w++;
		this.window = w;
	}

	@Override
	public double[] apply(double[] data, boolean[] valid) {
		double[] out = data.clone();
		int half = window / 2;
		for (MotionFilterSupport.Segment seg : MotionFilterSupport.contiguousValidSegments(valid)) {
			for (int i = seg.start; i < seg.end; i++) {
				int lo = Math.max(seg.start, i - half);
				int hi = Math.min(seg.end - 1, i + half);
				double sum = 0;
				int count = 0;
				for (int k = lo; k <= hi; k++) {
					sum += data[k];
					count++;
				}
				out[i] = sum / count;
			}
		}
		return out;
	}

	@Override
	public String toString() {
		return TrackerRes.getString("FilterDialog.MovingAverage.Name") //$NON-NLS-1$
				+ " (window=" + window + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public MotionFilter copy() {
		return new MovingAverageFilter(window);
	}

	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	static class Loader implements XML.ObjectLoader {
		@Override
		public void saveObject(XMLControl control, Object obj) {
			MovingAverageFilter f = (MovingAverageFilter) obj;
			control.setValue("window", f.window); //$NON-NLS-1$
		}
		@Override
		public Object createObject(XMLControl control) {
			return new MovingAverageFilter();
		}
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			MovingAverageFilter f = (MovingAverageFilter) obj;
			if (control.getPropertyNamesRaw().contains("window")) //$NON-NLS-1$
				f.setWindow(control.getInt("window")); //$NON-NLS-1$
			return f;
		}
	}

}
