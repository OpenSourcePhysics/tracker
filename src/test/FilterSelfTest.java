package test;

import org.opensourcephysics.cabrillo.tracker.ButterworthFilter;
import org.opensourcephysics.cabrillo.tracker.MotionFilter;
import org.opensourcephysics.cabrillo.tracker.MovingAverageFilter;
import org.opensourcephysics.cabrillo.tracker.SavitzkyGolayFilter;

/**
 * Standalone runnable that exercises the three filter implementations against synthetic
 * signals and reports a pass/fail summary. Compile against the tracker classes.
 */
public class FilterSelfTest {

	private static int passed = 0;
	private static int failed = 0;

	public static void main(String[] args) {
		testMovingAverageReducesNoise();
		testMovingAverageHandlesInvalidGap();
		testButterworthAttenuatesAboveCutoff();
		testButterworthPreservesDC();
		testButterworthZeroPhase();
		testSavitzkyGolayPreservesQuadratic();
		System.out.println();
		System.out.println("Passed: " + passed + ", Failed: " + failed);
		if (failed > 0) System.exit(1);
	}

	static void testMovingAverageReducesNoise() {
		int n = 200;
		double[] x = new double[n];
		boolean[] valid = new boolean[n];
		long seed = 42L;
		double seedState = seed;
		for (int i = 0; i < n; i++) {
			seedState = (seedState * 1103515245.0 + 12345.0) % (1L << 31);
			double noise = ((seedState / (double)(1L << 31)) - 0.5) * 0.4;
			x[i] = Math.sin(2 * Math.PI * i / 50.0) + noise;
			valid[i] = true;
		}
		MotionFilter f = new MovingAverageFilter(7);
		double[] y = f.apply(x, valid);
		double rmsX = rms(x, valid);
		double rmsY = rms(y, valid);
		check("MovingAverage reduces RMS energy of noisy signal",
				rmsY < rmsX);
	}

	static void testMovingAverageHandlesInvalidGap() {
		double[] x = { 0, 1, 2, 100, 100, 5, 6, 7 };
		boolean[] v = { true, true, true, false, false, true, true, true };
		MotionFilter f = new MovingAverageFilter(3);
		double[] y = f.apply(x, v);
		// Invalid entries pass through; the segment after the gap is averaged within itself.
		// y[5] uses [5,6] (clamped to segment start) -> (5+6)/2 = 5.5
		check("MovingAverage skips invalid gap (preserves invalid, edge-clamps after gap)",
				y[3] == 100 && y[4] == 100
				&& Math.abs(y[5] - 5.5) < 1e-9
				&& Math.abs(y[6] - 6.0) < 1e-9
				&& Math.abs(y[7] - 6.5) < 1e-9);
	}

	static void testButterworthAttenuatesAboveCutoff() {
		double fs = 100.0;
		double fcut = 5.0;
		int n = 1024;
		double[] x = new double[n];
		boolean[] v = new boolean[n];
		for (int i = 0; i < n; i++) {
			x[i] = Math.sin(2 * Math.PI * 20.0 * i / fs);
			v[i] = true;
		}
		MotionFilter f = new ButterworthFilter(4, fcut, fs);
		double[] y = f.apply(x, v);
		double inAmp = peakAmp(x, 50, n - 50);
		double outAmp = peakAmp(y, 50, n - 50);
		check("Butterworth attenuates 20 Hz with 5 Hz cutoff (out/in < 0.05): "
				+ String.format("ratio=%.4f", outAmp / inAmp),
				outAmp / inAmp < 0.05);
	}

	static void testButterworthPreservesDC() {
		int n = 256;
		double[] x = new double[n];
		boolean[] v = new boolean[n];
		for (int i = 0; i < n; i++) { x[i] = 3.7; v[i] = true; }
		MotionFilter f = new ButterworthFilter(4, 5.0, 100.0);
		double[] y = f.apply(x, v);
		double maxErr = 0;
		for (int i = 20; i < n - 20; i++) maxErr = Math.max(maxErr, Math.abs(y[i] - 3.7));
		check("Butterworth preserves DC (max error < 1e-6): " + maxErr, maxErr < 1e-6);
	}

	static void testButterworthZeroPhase() {
		double fs = 100.0;
		int n = 512;
		double[] x = new double[n];
		boolean[] v = new boolean[n];
		for (int i = 0; i < n; i++) {
			x[i] = Math.sin(2 * Math.PI * 2.0 * i / fs);
			v[i] = true;
		}
		MotionFilter f = new ButterworthFilter(4, 10.0, fs);
		double[] y = f.apply(x, v);
		int zeroX = -1, zeroY = -1;
		for (int i = 100; i < 200; i++) {
			if (zeroX < 0 && x[i] >= 0 && x[i + 1] < 0) zeroX = i;
			if (zeroY < 0 && y[i] >= 0 && y[i + 1] < 0) zeroY = i;
		}
		check("Butterworth zero-phase (lag < 2 samples): zeroX=" + zeroX + " zeroY=" + zeroY,
				zeroX > 0 && zeroY > 0 && Math.abs(zeroX - zeroY) <= 2);
	}

	static void testSavitzkyGolayPreservesQuadratic() {
		int n = 50;
		double[] x = new double[n];
		boolean[] v = new boolean[n];
		for (int i = 0; i < n; i++) {
			double t = i;
			x[i] = 1.0 + 2.0 * t + 0.5 * t * t;
			v[i] = true;
		}
		MotionFilter f = new SavitzkyGolayFilter(7, 2);
		double[] y = f.apply(x, v);
		double maxErr = 0;
		for (int i = 0; i < n; i++) maxErr = Math.max(maxErr, Math.abs(y[i] - x[i]));
		check("SavitzkyGolay (poly=2) reproduces quadratic exactly: maxErr=" + maxErr,
				maxErr < 1e-6);
	}

	private static double rms(double[] x, boolean[] v) {
		double sum = 0; int count = 0;
		for (int i = 0; i < x.length; i++) if (v[i]) { sum += x[i] * x[i]; count++; }
		return Math.sqrt(sum / count);
	}

	private static double peakAmp(double[] x, int from, int to) {
		double peak = 0;
		for (int i = from; i < to; i++) peak = Math.max(peak, Math.abs(x[i]));
		return peak;
	}

	private static void check(String label, boolean cond) {
		if (cond) { passed++; System.out.println("[PASS] " + label); }
		else { failed++; System.out.println("[FAIL] " + label); }
	}

}
