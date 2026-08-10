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
 * Savitzky-Golay smoothing filter. Each output sample is a least-squares polynomial
 * fit of order {@code polyOrder} over a centered window of {@code window} points,
 * evaluated at the center. Boundary samples use one-sided fits of the same window
 * length to avoid losing data at segment edges.
 *
 * @author Tracker Filter contribution
 */
public class SavitzkyGolayFilter implements MotionFilter {

	private int window;
	private int polyOrder;

	public SavitzkyGolayFilter() {
		this(7, 2);
	}

	public SavitzkyGolayFilter(int window, int polyOrder) {
		setParameters(window, polyOrder);
	}

	public int getWindow() { return window; }

	public int getPolyOrder() { return polyOrder; }

	public final void setParameters(int window, int polyOrder) {
		if (window < 3) window = 3;
		if (window % 2 == 0) window++;
		if (polyOrder < 1) polyOrder = 1;
		if (polyOrder >= window) polyOrder = window - 1;
		this.window = window;
		this.polyOrder = polyOrder;
	}

	@Override
	public double[] apply(double[] data, boolean[] valid) {
		double[] out = data.clone();
		for (MotionFilterSupport.Segment seg : MotionFilterSupport.contiguousValidSegments(valid)) {
			int n = seg.length();
			if (n < polyOrder + 1) continue;
			int eff = Math.min(window, n);
			if (eff % 2 == 0) eff--;
			int halfEff = eff / 2;
			int p = Math.min(polyOrder, eff - 1);
			double[][] centered = computeCoefficients(eff, p, 0);
			double[] center = centered[0];
			for (int i = halfEff; i < n - halfEff; i++) {
				double sum = 0;
				for (int k = 0; k < eff; k++) sum += center[k] * data[seg.start + i - halfEff + k];
				out[seg.start + i] = sum;
			}
			for (int i = 0; i < halfEff; i++) {
				int offsetFromLeft = i - halfEff;
				double[][] left = computeCoefficients(eff, p, offsetFromLeft);
				double[] coef = left[0];
				double sum = 0;
				for (int k = 0; k < eff; k++) sum += coef[k] * data[seg.start + k];
				out[seg.start + i] = sum;
			}
			for (int i = n - halfEff; i < n; i++) {
				int offsetFromRight = (i - (n - 1)) + halfEff;
				double[][] right = computeCoefficients(eff, p, offsetFromRight);
				double[] coef = right[0];
				double sum = 0;
				for (int k = 0; k < eff; k++) sum += coef[k] * data[seg.start + n - eff + k];
				out[seg.start + i] = sum;
			}
			if (n < window) {
			}
		}
		return out;
	}

	@Override
	public String toString() {
		return TrackerRes.getString("FilterDialog.SavitzkyGolay.Name") //$NON-NLS-1$
				+ " (window=" + window + ", poly=" + polyOrder + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public MotionFilter copy() {
		return new SavitzkyGolayFilter(window, polyOrder);
	}

	/**
	 * Computes Savitzky-Golay coefficients for a window of size {@code w} and polynomial
	 * order {@code p}, with the evaluation point at integer offset {@code t} from the
	 * window center (t in [-w/2, w/2]). Returns a 1xw matrix in row 0.
	 */
	static double[][] computeCoefficients(int w, int p, int t) {
		int half = w / 2;
		double[][] J = new double[w][p + 1];
		for (int i = 0; i < w; i++) {
			double xi = i - half;
			double power = 1.0;
			for (int j = 0; j <= p; j++) {
				J[i][j] = power;
				power *= xi;
			}
		}
		double[][] JtJ = new double[p + 1][p + 1];
		for (int a = 0; a <= p; a++) {
			for (int b = 0; b <= p; b++) {
				double sum = 0;
				for (int i = 0; i < w; i++) sum += J[i][a] * J[i][b];
				JtJ[a][b] = sum;
			}
		}
		double[][] inv = invert(JtJ);
		double[] tPow = new double[p + 1];
		double tp = 1.0;
		for (int j = 0; j <= p; j++) { tPow[j] = tp; tp *= t; }
		double[] eval = new double[p + 1];
		for (int a = 0; a <= p; a++) {
			double sum = 0;
			for (int b = 0; b <= p; b++) sum += inv[a][b] * tPow[b];
			eval[a] = sum;
		}
		double[] coef = new double[w];
		for (int i = 0; i < w; i++) {
			double sum = 0;
			for (int a = 0; a <= p; a++) sum += eval[a] * J[i][a];
			coef[i] = sum;
		}
		return new double[][] { coef };
	}

	private static double[][] invert(double[][] m) {
		int n = m.length;
		double[][] a = new double[n][2 * n];
		for (int i = 0; i < n; i++) {
			System.arraycopy(m[i], 0, a[i], 0, n);
			a[i][n + i] = 1.0;
		}
		for (int col = 0; col < n; col++) {
			int pivot = col;
			double pivotMag = Math.abs(a[col][col]);
			for (int r = col + 1; r < n; r++) {
				if (Math.abs(a[r][col]) > pivotMag) { pivot = r; pivotMag = Math.abs(a[r][col]); }
			}
			if (pivot != col) { double[] tmp = a[col]; a[col] = a[pivot]; a[pivot] = tmp; }
			double diag = a[col][col];
			if (diag == 0) throw new IllegalStateException("Singular matrix in SG coefficient solve"); //$NON-NLS-1$
			for (int j = 0; j < 2 * n; j++) a[col][j] /= diag;
			for (int r = 0; r < n; r++) {
				if (r == col) continue;
				double factor = a[r][col];
				if (factor == 0) continue;
				for (int j = 0; j < 2 * n; j++) a[r][j] -= factor * a[col][j];
			}
		}
		double[][] inv = new double[n][n];
		for (int i = 0; i < n; i++) System.arraycopy(a[i], n, inv[i], 0, n);
		return inv;
	}

	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	static class Loader implements XML.ObjectLoader {
		@Override
		public void saveObject(XMLControl control, Object obj) {
			SavitzkyGolayFilter f = (SavitzkyGolayFilter) obj;
			control.setValue("window", f.window); //$NON-NLS-1$
			control.setValue("poly_order", f.polyOrder); //$NON-NLS-1$
		}
		@Override
		public Object createObject(XMLControl control) {
			return new SavitzkyGolayFilter();
		}
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			SavitzkyGolayFilter f = (SavitzkyGolayFilter) obj;
			int w = control.getPropertyNamesRaw().contains("window") ? control.getInt("window") : f.window; //$NON-NLS-1$ //$NON-NLS-2$
			int p = control.getPropertyNamesRaw().contains("poly_order") ? control.getInt("poly_order") : f.polyOrder; //$NON-NLS-1$ //$NON-NLS-2$
			f.setParameters(w, p);
			return f;
		}
	}

}
