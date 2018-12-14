package org.opensourcephysics.cabrillo.tracker;


public class StepArray {

	// instance fields
	protected int length = 5;
	protected Step[] array = new Step[length];
	private boolean autofill = false;
	protected int delta = 5;

	/**
	 * Constructs a default StepArray.
	 */
	public StepArray() {/** empty block */}

	/**
	 * Constructs an autofill StepArray and fills the array with clones
	 * of the specified step.
	 *
	 * @param step the step to fill the array with
	 */
	public StepArray(Step step) {
		autofill = true;
		step.n = 0;
		array[0] = step;
		fill(step);
	}

	/**
	 * Constructs an autofill StepArray and fills the array with clones
	 * of the specified step.
	 *
	 * @param step      the step to fill the array with
	 * @param increment the array sizing increment
	 */
	public StepArray(Step step, int increment) {
		autofill = true;
		step.n = 0;
		array[0] = step;
		length = increment;
		delta = increment;
		fill(step);
	}

	/**
	 * Gets the step at the specified index. May return null.
	 *
	 * @param n the array index
	 * @return the step
	 */
	public Step getStep(int n) {
		if (n >= length) {
			int len = Math.max(n + delta, n - length + 1);
			setLength(len);
		}
		return array[n];
	}

	/**
	 * Sets the step at the specified index. Accepts a null step argument
	 * for non-autofill arrays.
	 *
	 * @param n    the array index
	 * @param step the new step
	 */
	public void setStep(int n, Step step) {
		if (autofill && step == null) return;
		if (n >= length) {
			int len = Math.max(n + delta, n - length + 1);
			setLength(len);
		}
		synchronized (array) {
			array[n] = step;
		}
	}

	/**
	 * Determines if this step array contains the specified step.
	 *
	 * @param step the new step
	 * @return <code>true</code> if this contains the step
	 */
	public boolean contains(Step step) {
		synchronized (array) {
			for (int i = 0; i < array.length; i++)
				if (array[i] == step) return true;
		}
		return false;
	}

	/**
	 * Sets the length of the array.
	 *
	 * @param len the new length of the array
	 */
	public void setLength(int len) {
		synchronized (array) {
			Step[] newArray = new Step[len];
			System.arraycopy(array, 0, newArray, 0, Math.min(len, length));
			array = newArray;
			if (len > length && autofill) {
				Step step = array[length - 1];
				length = len;
				fill(step);
			} else length = len;
		}
	}

	/**
	 * Determines if this is empty.
	 *
	 * @return true if empty
	 */
	public boolean isEmpty() {
		synchronized (array) {
			for (int i = 0; i < array.length; i++)
				if (array[i] != null) return false;
		}
		return true;
	}

	/**
	 * Determines if the specified step is preceded by a lower index step.
	 *
	 * @param n the step index
	 * @return true if the step is preceded
	 */
	public boolean isPreceded(int n) {
		synchronized (array) {
			int k = Math.min(n, array.length);
			for (int i = 0; i < k; i++)
				if (array[i] != null) return true;
		}
		return false;
	}

	public boolean isAutofill() {
		return autofill;
	}

	//__________________________ private methods _________________________

	/**
	 * Replaces null elements of the the array with clones of the
	 * specified step.
	 *
	 * @param step the step to clone
	 */
	private void fill(Step step) {
		for (int n = 0; n < length; n++)
			if (array[n] == null) {
				Step clone = (Step) step.clone();
				clone.n = n;
				array[n] = clone;
			}
	}
} // end StepArray class


