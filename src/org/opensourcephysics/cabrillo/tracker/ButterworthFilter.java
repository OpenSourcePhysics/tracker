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
 * Digital lowpass Butterworth filter applied forward-backward (zero phase, "filtfilt").
 * Coefficients are computed from analog prototype poles via the bilinear transform with
 * frequency pre-warping, then arranged as a cascade of biquad sections (direct form II
 * transposed) for numerical stability at higher orders.
 *
 * <p>The filter operates separately on each contiguous run of valid samples. Reflection
 * padding is applied on both ends of each segment to suppress edge transients. Because
 * the filter is applied twice (forward, then backward), the effective magnitude response
 * is squared and the effective filter order is twice the configured order.
 *
 * @author Tracker Filter contribution
 */
public class ButterworthFilter implements MotionFilter {

	private int order;
	private double cutoffHz;
	private double sampleRateHz;

	public ButterworthFilter() {
		this(4, 6.0, 30.0);
	}

	public ButterworthFilter(int order, double cutoffHz, double sampleRateHz) {
		setOrder(order);
		setCutoffHz(cutoffHz);
		setSampleRateHz(sampleRateHz);
	}

	public int getOrder() { return order; }

	public final void setOrder(int n) {
		if (n < 1) n = 1;
		if (n > 8) n = 8;
		this.order = n;
	}

	public double getCutoffHz() { return cutoffHz; }

	public final void setCutoffHz(double fc) {
		if (fc <= 0) fc = 1.0;
		this.cutoffHz = fc;
	}

	public double getSampleRateHz() { return sampleRateHz; }

	public final void setSampleRateHz(double fs) {
		if (fs <= 0) fs = 1.0;
		this.sampleRateHz = fs;
	}

	@Override
	public double[] apply(double[] data, boolean[] valid) {
		double[] out = data.clone();
		double nyquist = sampleRateHz / 2.0;
		if (cutoffHz >= nyquist) {
			return out;
		}
		Biquad[] sections = designBiquads(order, cutoffHz, sampleRateHz);
		int minLen = 3 * (sections.length * 2 + 1);
		for (MotionFilterSupport.Segment seg : MotionFilterSupport.contiguousValidSegments(valid)) {
			if (seg.length() < 3) continue;
			int pad = Math.min(seg.length() - 1, Math.max(minLen, sections.length * 6));
			double[] padded = MotionFilterSupport.reflectPad(data, seg.start, seg.end, pad);
			double[] forward = filterCascade(sections, padded);
			double[] reversed = reverse(forward);
			double[] backward = filterCascade(sections, reversed);
			double[] result = reverse(backward);
			for (int i = 0; i < seg.length(); i++) {
				out[seg.start + i] = result[pad + i];
			}
		}
		return out;
	}

	@Override
	public String toString() {
		return TrackerRes.getString("FilterDialog.Butterworth.Name") //$NON-NLS-1$
				+ " (order=" + order //$NON-NLS-1$
				+ ", cutoff=" + cutoffHz + " Hz" //$NON-NLS-1$ //$NON-NLS-2$
				+ ", fs=" + sampleRateHz + " Hz)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public MotionFilter copy() {
		return new ButterworthFilter(order, cutoffHz, sampleRateHz);
	}

	private static double[] reverse(double[] x) {
		double[] r = new double[x.length];
		for (int i = 0; i < x.length; i++) r[i] = x[x.length - 1 - i];
		return r;
	}

	private static double[] filterCascade(Biquad[] sections, double[] x) {
		double[] y = x;
		for (Biquad s : sections) {
			y = s.filter(y);
		}
		return y;
	}

	/**
	 * Direct-form II transposed biquad with steady-state initial conditions. The filter
	 * states are initialized to the values they would have if the input had been a
	 * constant equal to {@code x[0]} forever in the past. For a unity-DC-gain section
	 * this eliminates the startup transient on a constant signal and minimizes it on a
	 * slowly varying signal.
	 */
	private static final class Biquad {
		final double b0, b1, b2, a1, a2;
		Biquad(double b0, double b1, double b2, double a1, double a2) {
			this.b0 = b0; this.b1 = b1; this.b2 = b2; this.a1 = a1; this.a2 = a2;
		}
		double[] filter(double[] x) {
			if (x.length == 0) return x.clone();
			double v = x[0];
			double s1 = v * (1.0 - b0);
			double s2 = v * (b2 - a2);
			double[] y = new double[x.length];
			for (int i = 0; i < x.length; i++) {
				double xi = x[i];
				double yi = b0 * xi + s1;
				s1 = b1 * xi - a1 * yi + s2;
				s2 = b2 * xi - a2 * yi;
				y[i] = yi;
			}
			return y;
		}
	}

	/**
	 * Designs an N-th order digital Butterworth lowpass as a cascade of biquads. Uses
	 * the bilinear transform with frequency pre-warping. Each conjugate analog pole pair
	 * becomes a biquad with zeros at z=-1 (double); for odd N a real pole becomes a
	 * leading first-order section (a2=0, b2=0) with a single zero at z=-1. Each section
	 * is individually normalized to unity gain at DC by setting b0 directly from the
	 * resulting denominator.
	 */
	static Biquad[] designBiquads(int order, double fc, double fs) {
		double omegaPrewarped = 2.0 * fs * Math.tan(Math.PI * fc / fs);
		double k2 = 2.0 * fs;
		int nSections = (order + 1) / 2;
		Biquad[] sections = new Biquad[nSections];
		int idx = 0;
		if (order % 2 == 1) {
			double pole = -omegaPrewarped;
			double zPole = (k2 + pole) / (k2 - pole);
			double a1 = -zPole;
			double b0 = (1.0 + a1) / 2.0;
			sections[idx++] = new Biquad(b0, b0, 0.0, a1, 0.0);
		}
		int pairs = order / 2;
		for (int k = 1; k <= pairs; k++) {
			double theta = Math.PI * (2 * k - 1 + order) / (2.0 * order);
			double pr = omegaPrewarped * Math.cos(theta);
			double pi = omegaPrewarped * Math.sin(theta);
			double aReal = k2 - pr;
			double aImag = -pi;
			double bReal = k2 + pr;
			double bImag = pi;
			double denomMag = aReal * aReal + aImag * aImag;
			double zReal = (bReal * aReal + bImag * aImag) / denomMag;
			double zImag = (bImag * aReal - bReal * aImag) / denomMag;
			double a1 = -2.0 * zReal;
			double a2 = zReal * zReal + zImag * zImag;
			double b0 = (1.0 + a1 + a2) / 4.0;
			double b1 = 2.0 * b0;
			double b2 = b0;
			sections[idx++] = new Biquad(b0, b1, b2, a1, a2);
		}
		return sections;
	}

	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	static class Loader implements XML.ObjectLoader {
		@Override
		public void saveObject(XMLControl control, Object obj) {
			ButterworthFilter f = (ButterworthFilter) obj;
			control.setValue("order", f.order); //$NON-NLS-1$
			control.setValue("cutoff_hz", f.cutoffHz); //$NON-NLS-1$
			control.setValue("sample_rate_hz", f.sampleRateHz); //$NON-NLS-1$
		}
		@Override
		public Object createObject(XMLControl control) {
			return new ButterworthFilter();
		}
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			ButterworthFilter f = (ButterworthFilter) obj;
			if (control.getPropertyNamesRaw().contains("order")) //$NON-NLS-1$
				f.setOrder(control.getInt("order")); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("cutoff_hz")) //$NON-NLS-1$
				f.setCutoffHz(control.getDouble("cutoff_hz")); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("sample_rate_hz")) //$NON-NLS-1$
				f.setSampleRateHz(control.getDouble("sample_rate_hz")); //$NON-NLS-1$
			return f;
		}
	}

}
