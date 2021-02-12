/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert Hanson
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

import java.awt.Graphics;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import org.opensourcephysics.media.core.*;
import org.opensourcephysics.numerics.*;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.UserFunction;
import org.opensourcephysics.tools.UserFunctionEditor;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DrawingPanel;

/**
 * DynamicParticle models a particle using Newton's 2nd law.
 *
 * @author W. Christian, D. Brown
 * @version 1.0
 */
public class DynamicParticle extends ParticleModel implements ODE {

	
	// instance fields
	protected boolean inSystem; // used only when loading
	protected String boosterName; // used only when loading
	final static protected String[] cartVars = new String[] {"x", "vx", "y", "vy", "t" };
	protected double[] state = new double[5]; // {x, vx, y, vy, t}
	protected double[] initialState = new double[5]; // {x, vx, y, vy, t}
	protected ODESolver solver = new RK4(this);
	protected int iterationsPerStep = 10;
	protected DynamicSystem system;
	protected HashMap<Integer, double[]> frameStates = new HashMap<Integer, double[]>();
	protected ModelBooster modelBooster = new ModelBooster();

	protected String[] getBoostVars() {
		return cartVars;
	}
	/**
	 * Constructor
	 */
	public DynamicParticle() {
		// create initial condition parameters
		initializeInitEditor();
		points = new Point2D.Double[] { new Point2D.Double() };
	}

	/**
	 * Overrides ParticleModel draw method.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		// if a booster is named, set the booster to the named point mass
		if (boosterName != null && panel instanceof TrackerPanel) {
			PointMass m = ((TrackerPanel) panel).getTrackByName(PointMass.class, boosterName);
			if (m != null) {
				setBooster(m);
				boosterName = null;
			}
		}
		// if this is part of a system, then the system draws it
		if (system == null && !inSystem)
			super.draw(panel, _g);
	}

	/**
	 * Gets a display name for this model.
	 *
	 * @return the display name
	 */
	@Override
	public String getDisplayName() {
		String s = getName();
		if (system == null)
			return s;
		String in = TrackerRes.getString("DynamicParticle.System.In"); //$NON-NLS-1$
		return s + " (" + in + " " + system.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Deletes this particle. Overrides ParticleModel method to warn user if this is
	 * part of a DynamicSystem.
	 */
	@Override
	public void delete() {
		// if this is part of a system, warn user
		if (system != null) {
			String message = TrackerRes.getString("DynamicParticle.Dialog.Delete.Message"); //$NON-NLS-1$
			int response = javax.swing.JOptionPane.showConfirmDialog(trackerPanel.getTFrame(), message,
					TrackerRes.getString("DynamicParticle.Dialog.Delete.Title"), //$NON-NLS-1$
					javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
			if (response == javax.swing.JOptionPane.YES_OPTION) {
				system.removeParticle(this);
			} else
				return;
		}
		super.delete();
	}

	@Override
	protected void dispose() {
		setBooster(null);
		super.dispose();
	}

	/**
	 * Refreshes step positions.
	 */
	@Override
	protected void refreshSteps(String why) {
		if (system == null)
			super.refreshSteps(why);
	}

	/**
	 * Resets parameters, initializes solver and sets position(s) for start frame or
	 * first clip frame following.
	 */
	@Override
	public void reset() {
		if (system != null)
			return;
		super.reset();
		resetState(); // resets state to initial state (ie at startFrame)
		double[] state = getState();
		// state is {x, vx, y, vy, t} but may be different in subclasses
		t0 = state[state.length - 1]; // time at start frame
		setTracePositions(state);
		if (trackerPanel != null) {
			erase();
			dt = trackerPanel.getPlayer().getMeanStepDuration() / (1000 * tracePtsPerStep);
			dt /= iterationsPerStep;
			solver.initialize(dt);
			ParticleModel[] models = getModels();
			VideoClip clip = trackerPanel.getPlayer().getVideoClip();
			// find last frame included in both model and clip
			int end = Math.min(getEndFrame(), clip.getLastFrameNumber());
			while (end > getStartFrame() && !clip.includesFrame(end)) {
				end--;
			}
			// determine if this is an empty dynamic system
			boolean emptySystem = false;
			if (this instanceof DynamicSystem) {
				DynamicSystem system = (DynamicSystem) this;
				emptySystem = system.particles.length == 0;
			}
			// clear all steps if empty system or no frames included in clip
			if (emptySystem || (end == getStartFrame() && !clip.includesFrame(getStartFrame()))) {
				for (int i = 0; i < models.length; i++) {
					models[i].steps.setLength(1);
					models[i].steps.setStep(0, null);
					for (int j = 0; j < trackerPanel.panelAndWorldViews.size(); j++) {
						TrackerPanel panel = trackerPanel.panelAndWorldViews.get(j);
						models[i].getVArray(panel).setLength(0);
						models[i].getAArray(panel).setLength(0);
					}
					models[i].traceX = new double[0];
					models[i].traceY = new double[0];
				}
				fireStepsChanged();
				return;
			}
			// find first frame included in both model and clip
			int firstFrameInClip = getStartFrame();
			while (firstFrameInClip < end && !clip.includesFrame(firstFrameInClip)) {
				firstFrameInClip++;
			}
			ImageCoordSystem coords = trackerPanel.getCoords();
			// get underlying coords if appropriate
			boolean useDefault = isUseDefaultReferenceFrame();
			while (useDefault && coords instanceof ReferenceFrame) {
				coords = ((ReferenceFrame) coords).getCoords();
			}
			// step solver forward to first frame in clip
			int count = (firstFrameInClip - getStartFrame()) * tracePtsPerStep * iterationsPerStep / clip.getStepSize();
			for (int i = 0; i < count; i++) {
				solver.step();
			}
			setTracePositions(getState());
			AffineTransform transform = coords.getToImageTransform(firstFrameInClip);
			for (int i = 0; i < models.length; i++) {
				models[i].setLastValidFrame(firstFrameInClip);
				models[i].steps.setLength(firstFrameInClip + 1);
				PositionStep step = (PositionStep) models[i].getStep(firstFrameInClip);
				for (int j = 0; j < models[i].steps.array.length; j++) {
					if (j < firstFrameInClip)
						models[i].steps.setStep(j, null);
					else if (step == null) {
						step = new PositionStep(models[i], firstFrameInClip, 0, 0);
						step.setFootprint(models[i].getFootprint());
						models[i].steps.setStep(firstFrameInClip, step);
					}
				}
				for (int j = 0; j < trackerPanel.panelAndWorldViews.size(); j++) {
					TrackerPanel panel = trackerPanel.panelAndWorldViews.get(j);
					models[i].getVArray(panel).setLength(0);
					models[i].getAArray(panel).setLength(0);
				}
				transform.transform(points[i], points[i]);
				models[i].traceX = new double[] { points[i].getX() };
				models[i].traceY = new double[] { points[i].getY() };
				step.getPosition().setPosition(points[i]); // this method is fast
				models[i].firePropertyChange(PROPERTY_TTRACK_STEP, null, firstFrameInClip); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Gets the current state {x, vx, y, vy, t}.
	 * 
	 * @return the state
	 */
	@Override
	public double[] getState() {
		if (system != null) {
			return system.getState(this);
		}
		return state;
	}

	/**
	 * Saves the current state.
	 * 
	 * @param frameNumber the frame number
	 */
	@Override
	protected void saveState(int frameNumber) {
		frameStates.put(frameNumber, getState().clone());
	}

	/**
	 * Restores the state to a previously saved state, if any.
	 * 
	 * @param frameNumber the frame number
	 * @return true if state successfully restored
	 */
	@Override
	protected boolean restoreState(int frameNumber) {
		double[] savedState = frameStates.get(frameNumber);
		if (savedState != null) {
			System.arraycopy(savedState, 0, state, 0, state.length);
			return true;
		}
		return false;
	}

	protected double[] temp = new double[5];
	
	/**
	 * Gets the rate {vx, ax, vy, ay, 1} based on a specified state {x, vx, y, vy,
	 * t}.
	 * 
	 * @param state the state
	 * @param rate  the rate of change of the state
	 */
	@Override
	public void getRate(double[] state, double[] rate) {
	    getXYForces(state, temp);
		// rate is {vx, ax, vy, ay, 1}
		rate[0] = state[1]; // dx/dt = vx
		rate[1] = temp[0] / getMass(); // dvx/dt = ax
		rate[2] = state[3]; // dy/dt = vy
		rate[3] = temp[1] / getMass(); // dvy/dt = ay
		rate[4] = 1; // dt/dt = 1
	}

	/**
	 * Sets the ODESolver type.
	 * 
	 * @param solverClass the solver class
	 */
	public void setSolver(Class<?> solverClass) {
		Class<?>[] c = { ODE.class };
		Object[] o = { this };
		try { // create the solver by reflection
			java.lang.reflect.Constructor<?> constructor = solverClass.getDeclaredConstructor(c);
			solver = (ODESolver) constructor.newInstance(o);
			reset();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Gets the initial state {x, vx, y, vy, t}.
	 * 
	 * @return the initial state
	 */
	public double[] getInitialState() {
		double[] init = getInitialValues();
		// init is {t, x, y, vx, vy}
		initialState[0] = init[1];
		initialState[1] = init[3];
		initialState[2] = init[2];
		initialState[3] = init[4];
		initialState[4] = init[0];
		return initialState;
	}

	/**
	 * Gets the start frame for this model. Overrides ParticleModel method.
	 * 
	 * @return the start frame
	 */
	@Override
	public int getStartFrame() {
		return (system == null ? startFrame : system.getStartFrame());
	}

	/**
	 * Sets the start frame for this model. Overrides ParticleModel method.
	 * 
	 * @param n the desired start frame
	 */
	@Override
	public void setStartFrame(int n) {
		if (system != null) {
			system.setStartFrame(n);
			system.refreshSystemParameters();
		} else {
			super.setStartFrame(n);
			if (modelBooster != null) {
				modelBooster.setBooster(modelBooster.booster);
			}
		}
	}

	/**
	 * Gets the end frame for this model. Overrides ParticleModel method.
	 * 
	 * @return the end frame
	 */
	@Override
	public int getEndFrame() {
		if (system != null)
			return system.getEndFrame();
		return endFrame;
	}

	/**
	 * Sets the end frame for this model. Overrides ParticleModel method.
	 * 
	 * @param n the desired end frame
	 */
	@Override
	public void setEndFrame(int n) {
		if (system != null)
			system.setEndFrame(n);
		else
			super.setEndFrame(n);
	}

	/**
	 * Gets the x- and y-forces based on a specified cartesian state {x, vx, y, vy,
	 * t}.
	 * 
	 * @param cartesianState the state
	 * @param ret the forces [fx, fy]
	 */
	protected void getXYForces(double[] cartesianState, double[] ret) {
		UserFunction[] f = getFunctionEditor().getMainFunctions();
		// state is {x, vx, y, vy, t}
		f[0].clear();
		f[1].clear();
		ret[0] = f[0].evaluateMyVal(cartesianState);
		ret[1] = f[1].evaluateMyVal(cartesianState);
		f[0].clear();
		f[1].clear();
		nCalc += 2;
	}
	
	/**
	 * Resets the state variables {x, vx, y, vy, t}.
	 */
	protected void resetState() {
		if (system != null)
			system.resetState();
		else
			System.arraycopy(getInitialState(), 0, state, 0, state.length);
	}

	/**
	 * Creates the initial position and velocity parameters.
	 */
	protected void initializeInitEditor() {
		Parameter t = (Parameter) getInitEditor().getObject("t"); //$NON-NLS-1$
		Parameter x = new Parameter("x", "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
		x.setNameEditable(false);
		x.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialX.Description")); //$NON-NLS-1$
		Parameter y = new Parameter("y", "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
		y.setNameEditable(false);
		y.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialY.Description")); //$NON-NLS-1$
		Parameter vx = new Parameter("vx", "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
		vx.setNameEditable(false);
		vx.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialVelocityX.Description")); //$NON-NLS-1$
		Parameter vy = new Parameter("vy", "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
		vy.setNameEditable(false);
		vy.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialVelocityY.Description")); //$NON-NLS-1$
		getInitEditor().setParameters(new Parameter[] { t, x, y, vx, vy });
	}

	/**
	 * Creates and initializes the ModelFunctionPanel.
	 */
	@Override
	protected void initializeFunctionPanel() {
		// create panel
		functionEditor = new UserFunctionEditor();
		functionPanel = new DynamicFunctionPanel(functionEditor, this);
		// create main force functions
		String[] funcVars = new String[] { "x", "vx", //$NON-NLS-1$ //$NON-NLS-2$
				"y", "vy", "t" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UserFunction[] uf = new UserFunction[2];
		uf[0] = new UserFunction("fx"); //$NON-NLS-1$
		uf[0].setNameEditable(false);
		uf[0].setExpression("0", funcVars); //$NON-NLS-1$
		uf[0].setDescription(TrackerRes.getString("DynamicParticle.ForceFunction.X.Description")); //$NON-NLS-1$
		uf[1] = new UserFunction("fy"); //$NON-NLS-1$
		uf[1].setNameEditable(false);
		uf[1].setExpression("0", funcVars); //$NON-NLS-1$
		uf[1].setDescription(TrackerRes.getString("DynamicParticle.ForceFunction.Y.Description")); //$NON-NLS-1$
		functionEditor.setMainFunctions(uf);
		// create mass and initial time parameters
		createMassAndTimeParameters();
	}

	/**
	 * Gets the next trace positions.
	 * 
	 * @return an array of points at the trace positions
	 */
	@Override
	protected boolean getNextTracePositions() {
		for (int i = 0; i < iterationsPerStep; i++) {
			solver.step();
		}
		setTracePositions(getState());
		return true;
	}

	/**
	 * Sets the positions of the trace points based on a specified state.
	 * 
	 * @param state the state
	 */
	protected void setTracePositions(double[] state) {
		// state is {x, vx, y, vy, t}
		points[0].setLocation(state[0], state[2]);
	}

	/**
	 * Gets the cartesian state {x, vx, y, vy, t} of a PointMass at a specified
	 * frame number.
	 * 
	 * @param target      the PointMass
	 * @param frameNumber the frame number
	 * @return the state, or null if the point mass is not marked at the frame
	 *         number
	 */
	protected double[] getBoostState(PointMass target, int frameNumber) {
		DatasetManager data = target.getData(trackerPanel);

		// determine the dataset index for the specified frame number
		Dataset ds = data.getFrameDataset();
		double[] frames = ds.getYPoints();
		for (int i = 0, n = frames.length; i < n; i++) {
			if (frames[i] == frameNumber) {
				temp[0] = data.get("x", i, 1); //$NON-NLS-1$
				temp[1] = data.get("v_{x}", i, 1); //$NON-NLS-1$
				temp[2] = data.get("y", i, 1); //$NON-NLS-1$
				temp[3] = data.get("v_{y}", i, 1); //$NON-NLS-1$
				temp[4] = data.getValueAt(i, 0); // $NON-NLS-1$
				return temp;
			}
		}
		return null;
	}

	/**
	 * Gets the booster point mass.
	 * 
	 * @return the booster
	 */
	protected PointMass getBooster() {
		return modelBooster.booster;
	}

	/**
	 * Sets the booster point mass.
	 * 
	 * @param booster the new booster (may be null)
	 */
	protected void setBooster(PointMass booster) {
		modelBooster.setBooster(booster);
	}

	/**
	 * Determines if a specified point mass is a booster of this particle (or a
	 * booster of a booster, etc).
	 * 
	 * @param target a point mass
	 * @return true if the target is a booster
	 */
	protected boolean isBoostedBy(PointMass target) {
		if (modelBooster == null || modelBooster.booster == null)
			return false;
		if (modelBooster.booster == target)
			return true;
		if (modelBooster.booster instanceof DynamicParticle) {
			DynamicParticle dp = (DynamicParticle) modelBooster.booster;
			return dp.isBoostedBy(target);
		}
		return false;
	}

	/**
	 * Sets the initial conditions to those of the booster at the current start
	 * frame.
	 */
	protected void boost() {
		if (modelBooster == null || modelBooster.booster == null)
			return;
		double[] state = getBoostState(modelBooster.booster, getStartFrame()); // {x, vx, y, vy, t}
		if (state == null)
			return;

		Parameter[] params = getInitEditor().getParameters();
		String[] boostVars = getBoostVars();
		for (int i = 0; i < params.length; i++) {
			Parameter param = params[i];
			String name = param.getName();
			for (int j = 0; j < 4; j++) {
				if (name.equals(boostVars[j])) {
					double value = state[j]; // default
					if (!Double.isNaN(value)) {
						Parameter newParam = params[i] = new Parameter(name, String.valueOf(value));
						newParam.setDescription(param.getDescription());
						newParam.setNameEditable(false);
					}
					break;
				}
			}
		}
		getInitEditor().setParameters(params);
		if (system != null) {
			system.refreshSystemParameters();
			system.setLastValidFrame(-1);
			system.refreshSteps("DP boost");
		} else {
			reset();
		}
		repaint();

	}

	/**
	 * A ModelBooster manages a "booster" PointMass used to set initial values of
	 * this model. To use the booster, call the DynamicParticle boost() method.
	 */
	class ModelBooster implements PropertyChangeListener {

		PointMass booster;
		boolean adjusting = false;

		/**
		 * Sets the booster PointMass.
		 * 
		 * @param pm the point mass (may be null)
		 */
		public void setBooster(PointMass pm) {
			if (booster != null) {
				booster.removeStepListener(this);
			}
			booster = pm;
			if (booster != null) {
				boost();
				booster.addStepListener(this);
			}
		}

		/**
		 * Implements PropertyChangeListener.
		 * 
		 * @param e the event
		 */
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (booster == null)
				return;

			String propName = e.getPropertyName();

			if (propName.equals(Trackable.PROPERTY_ADJUSTING)) { //$NON-NLS-1$
				adjusting = (Boolean) e.getNewValue();
				// change property to PROPERTY_TTRACK_STEPS so update will be triggered below when adjusting
				// is false
				propName =PROPERTY_TTRACK_STEPS; //$NON-NLS-1$
			}
			if (adjusting) {
				return;
			}

			if (!(propName == TTrack.PROPERTY_TTRACK_STEP || 
					propName == TTrack.PROPERTY_TTRACK_STEPS ||
					propName == TTrack.PROPERTY_TTRACK_DATA)) { //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}

			if (e.getPropertyName().equals(PROPERTY_TTRACK_STEPS) && booster instanceof ParticleModel) { //$NON-NLS-1$
				DatasetManager data = booster.getData(trackerPanel);
				booster.refreshData(data, trackerPanel);
			}

			boost();
		}

	}

	/**
	 * Returns an ObjectLoader to save and load data for this class.
	 *
	 * @return the object loader
	 */
	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	/**
	 * A class to save and load data for this class.
	 */
	static class Loader implements XML.ObjectLoader {

		/**
		 * Saves an object's data to an XMLControl.
		 *
		 * @param control the control to save to
		 * @param obj     the object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			// save particle model data
			DynamicParticle p = (DynamicParticle) obj;
			XML.getLoader(ParticleModel.class).saveObject(control, obj);
			if (p.system != null)
				control.setValue("in_system", true); //$NON-NLS-1$
			if (p.modelBooster != null && p.modelBooster.booster != null) {
				control.setValue("booster", p.modelBooster.booster.getName()); //$NON-NLS-1$
			}
		}

		/**
		 * Creates a new object.
		 *
		 * @param control the control with the object data
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return new DynamicParticle();
		}

		/**
		 * Loads an object with data from an XMLControl.
		 *
		 * @param control the control
		 * @param obj     the object
		 * @return the loaded object
		 */
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			DynamicParticle p = (DynamicParticle) obj;
			try {
				XML.getLoader(ParticleModel.class).loadObject(control, obj);
				p.inSystem = control.getBoolean("in_system"); //$NON-NLS-1$
				p.boosterName = control.getString("booster"); //$NON-NLS-1$
			} catch (Exception ex) {
				// load legacy xml
				String solver = control.getString("solver"); //$NON-NLS-1$
				if (solver != null) {
					try { // load the solver class
						Class<?> solverClass = Class.forName(solver);
						p.setSolver(solverClass);
					} catch (Exception ex2) {
						/** empty block */
					}
				}
				String t = control.getString("t0"); //$NON-NLS-1$
				p.getInitEditor().setExpression("t", t, false); //$NON-NLS-1$
				String x = control.getString("x"); //$NON-NLS-1$
				p.getInitEditor().setExpression("x", x, false); //$NON-NLS-1$
				String y = control.getString("y"); //$NON-NLS-1$
				p.getInitEditor().setExpression("y", y, false); //$NON-NLS-1$
				String vx = control.getString("vx"); //$NON-NLS-1$
				p.getInitEditor().setExpression("vx", vx, false); //$NON-NLS-1$
				String vy = control.getString("vy"); //$NON-NLS-1$
				p.getInitEditor().setExpression("vy", vy, false); //$NON-NLS-1$
				String fx = control.getString("force x"); //$NON-NLS-1$
				p.getFunctionEditor().setExpression("fx", fx, false); //$NON-NLS-1$
				String fy = control.getString("force y"); //$NON-NLS-1$
				p.getFunctionEditor().setExpression("fy", fy, false); //$NON-NLS-1$
				p.reset();
			}
			return obj;
		}
	}

}
