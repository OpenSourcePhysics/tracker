/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Graphics;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
public class DynamicParticle
    extends ParticleModel implements ODE {
	
	// instance fields
	protected boolean inSystem; // used only when loading
	protected String boosterName; // used only when loading
  protected double[] state = new double[5]; // {x, vx, y, vy, t}
  protected double[] initialState = new double[5]; // {x, vx, y, vy, t}
  protected ODESolver solver = new RK4(this);
  protected int iterationsPerStep = 100;
  protected DynamicSystem system;
  protected Point2D[] points;
  protected HashMap<Integer, double[]> frameStates = new HashMap<Integer, double[]>();
  protected ModelBooster modelBooster = new ModelBooster();
  
  /**
   * Constructor
   */
	public DynamicParticle() {
		// create initial condition parameters
		initializeInitEditor();
		points = new Point2D[] {point};
	}

	/**
	 * Overrides ParticleModel draw method.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g the graphics context on which to draw
	 */
	public void draw(DrawingPanel panel, Graphics _g) {
		// if a booster is named, set the booster to the named point mass
		if (boosterName!=null && panel instanceof TrackerPanel) {
			for (PointMass track: ((TrackerPanel)panel).getDrawables(PointMass.class)) {
				if (track.getName().equals(boosterName)) {
					setBooster(track);
					boosterName = null;
					break;
				}
			}
		}
		// if this is part of a system, then the system draws it
		if (system==null && !inSystem)
			super.draw(panel, _g);
	}
	
  /**
   * Gets a display name for this model. 
   *
   * @return the display name
   */
  public String getDisplayName() {
		String s = getName();
		if (system == null) return s;
		String in = TrackerRes.getString("DynamicParticle.System.In"); //$NON-NLS-1$
		return s+" ("+in+" "+system.getName()+")";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Deletes this particle. Overrides ParticleModel method to warn user if this
   * is part of a DynamicSystem.
   * 
   */
  public void delete() {
    // if this is part of a system, warn user
    if (system !=null) {
    	String message = TrackerRes.getString("DynamicParticle.Dialog.Delete.Message"); //$NON-NLS-1$ 
    	int response = javax.swing.JOptionPane.showConfirmDialog(
    			trackerPanel.getTFrame(), 
    			message,
    			TrackerRes.getString("DynamicParticle.Dialog.Delete.Title"), //$NON-NLS-1$ 
    			javax.swing.JOptionPane.OK_CANCEL_OPTION, 
    			javax.swing.JOptionPane.WARNING_MESSAGE);
    	if (response == javax.swing.JOptionPane.YES_OPTION) {
    		system.removeParticle(this);
    	}
    	else return;
    }
    super.delete();
  }

  /**
	 * Refreshes step positions.
	 */
	protected void refreshSteps() {
		if (system==null)
			super.refreshSteps();
	}
	
	/**
	 * Resets parameters, initializes solver and sets position(s) for start frame
	 * or first clip frame following.
	 */
	public void reset() {
		if (system!=null) return;
		super.reset();
  	resetState(); // resets state to initial state (ie at startFrame)
  	double[] state = getState();
  	// state is {x, vx, y, vy, t} but may be different in subclasses
    t0 = state[state.length-1]; // time at start frame
    setTracePositions(state);
	  if (trackerPanel != null) {
	  	erase();
	    dt = trackerPanel.getPlayer().getMeanStepDuration() / (1000*tracePtsPerStep);
	    dt /= iterationsPerStep;	
	    solver.initialize(dt);
	  	ParticleModel[] models = getModels();
      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
      // find last frame included in both model and clip
  		int end = Math.min(getEndFrame(), clip.getFrameCount()-1);
  		while (end>getStartFrame() && !clip.includesFrame(end)) {
  			end--;
  		}
	    // determine if this is an empty dynamic system
	    boolean emptySystem = false;
	    if (this instanceof DynamicSystem) {
	    	DynamicSystem system = (DynamicSystem)this;
	    	emptySystem = system.particles.length==0;
	    }
    	// clear all steps if empty system or no frames included in clip
  		if (emptySystem ||
  				(end==getStartFrame() && !clip.includesFrame(getStartFrame()))) {
  			for (int i = 0; i < models.length; i++) {
  		  	models[i].steps.setLength(1);
	  			models[i].steps.setStep(0, null);
	      	for (TrackerPanel panel: panels) {
	      		models[i].getVArray(panel).setLength(0);
	      		models[i].getAArray(panel).setLength(0);
	      	}
			    models[i].traceX = new double[0];
			    models[i].traceY = new double[0];
			    models[i].support.firePropertyChange("steps", null, null); //$NON-NLS-1$
  			}
  	    return;
  		}
    	// find first frame included in both model and clip
    	int firstFrameInClip = getStartFrame();
    	while (firstFrameInClip<end && !clip.includesFrame(firstFrameInClip)) {
    		firstFrameInClip++;
    	}
	    ImageCoordSystem coords = trackerPanel.getCoords();
	    // get underlying coords if reference frame
	    while (coords instanceof ReferenceFrame) {
	      coords = ( (ReferenceFrame) coords).getCoords();
	    }
    	// step solver forward to first frame in clip
	    int count = (firstFrameInClip-getStartFrame())*tracePtsPerStep*iterationsPerStep/clip.getStepSize();
    	for (int i=0; i<count; i++) {
  			solver.step();
    	}
  		setTracePositions(getState());
	    AffineTransform transform = coords.getToImageTransform(firstFrameInClip);
			for (int i = 0; i < models.length; i++) {
				models[i].lastValidFrame = firstFrameInClip;
		  	models[i].steps.setLength(firstFrameInClip+1);
		    PositionStep step = (PositionStep)models[i].getStep(firstFrameInClip);
		  	for (int j = 0; j < models[i].steps.length;j++) {
		  		if (j<firstFrameInClip)
		  			models[i].steps.setStep(j, null);
		  		else if (step==null) {
	      		step = new PositionStep(models[i], firstFrameInClip, 0, 0);
	      		step.setFootprint(models[i].getFootprint());	  			
	      		models[i].steps.setStep(firstFrameInClip, step);
		  		}	  			
		  	}
      	for (TrackerPanel panel: panels) {
      		models[i].getVArray(panel).setLength(0);
      		models[i].getAArray(panel).setLength(0);
      	}
		    transform.transform(points[i], points[i]);
		    models[i].traceX = new double[] {points[i].getX()};
		    models[i].traceY = new double[] {points[i].getY()};
		    step.getPosition().setPosition(points[i]); // this method is fast
		    models[i].support.firePropertyChange("step", null, firstFrameInClip); //$NON-NLS-1$
			}
	  }
  }

  /**
   * Gets the current state {x, vx, y, vy, t}.
   * 
   * @return the state
   */
  public double[] getState() {
  	if (system!=null) {
  		return system.getState(this);
  	}
    return state;
  }

	/**
	 * Saves the current state.
	 * 
	 * @param frameNumber the frame number
	 */
	protected void saveState(int frameNumber) {
		frameStates.put(frameNumber, getState().clone());
	}
	
	/**
	 * Restores the state to a previously saved state, if any.
	 * 
	 * @param frameNumber the frame number
	 * @return true if state successfully restored
	 */
	protected boolean restoreState(int frameNumber) {
		double[] savedState = frameStates.get(frameNumber);
		if (savedState!=null) {
			System.arraycopy(savedState, 0, state, 0, state.length);
			return true;
		}
		return false;
	}
	
  /**
   * Gets the rate {vx, ax, vy, ay, 1} based on a specified state {x, vx, y, vy, t}.
   * 
   * @param state the state
   * @param rate the rate of change of the state
   */
  public void getRate(double[] state, double[] rate) {
  	double[] f = getXYForces(state);
    // rate is {vx, ax, vy, ay, 1}
    rate[0] = state[1]; // dx/dt = vx
    rate[1] = f[0] / getMass(); // dvx/dt = ax
    rate[2] = state[3]; // dy/dt = vy
    rate[3] = f[1] / getMass(); // dvy/dt = ay
    rate[4] = 1; // dt/dt = 1
  }
  
  /**
   * Sets the ODESolver type.
   * 
   * @param solverClass the solver class
   */
  public void setSolver(Class<?> solverClass) {
    Class<?>[] c = {ODE.class};
    Object[] o = {this};
    try { // create the solver by reflection
      java.lang.reflect.Constructor<?> constructor = solverClass.getDeclaredConstructor(c);
      solver = (ODESolver) constructor.newInstance(o);
      reset();
    } catch(Exception ex) {
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
	public int getStartFrame() {
		if (system!=null)
			return system.getStartFrame();
		return startFrame;
	}
	
  /**
   * Sets the start frame for this model. Overrides ParticleModel method.
   * 
   * @param n the desired start frame
   */
	public void setStartFrame(int n) {
		if (system!=null) {
			system.setStartFrame(n);
			system.refreshSystemParameters();
		}
		else {
			super.setStartFrame(n);
			if (modelBooster!=null) {
				modelBooster.setBooster(modelBooster.booster);
			}
		}
	}
	
  /**
   * Gets the end frame for this model. Overrides ParticleModel method.
   * 
   * @return the end frame
   */
	public int getEndFrame() {
		if (system!=null)
			return system.getEndFrame();
		return endFrame;
	}
	
  /**
   * Sets the end frame for this model. Overrides ParticleModel method.
   * 
   * @param n the desired end frame
   */
	public void setEndFrame(int n) {
		if (system!=null)
			system.setEndFrame(n);
		else super.setEndFrame(n);
	}
	
  /**
   * Gets the x- and y-forces based on a specified cartesian state {x, vx, y, vy, t}.
   * 
   * @param cartesianState the state
   * @return the forces
   */
  protected double[] getXYForces(double[] cartesianState) {
    UserFunction[] f = getFunctionEditor().getMainFunctions();
  	// state is {x, vx, y, vy, t}
    double fx = f[0].evaluate(cartesianState);
    double fy = f[1].evaluate(cartesianState);
    return new double[] {fx, fy};
  }
  
  /**
	 * Resets the state variables {x, vx, y, vy, t}.
	 */
	protected void resetState() {
		if (system!=null)
			system.resetState();
		else
			System.arraycopy(getInitialState(), 0, state, 0, state.length);
	}
	
	/**
	 * Creates the initial position and velocity parameters.
	 */
	protected void initializeInitEditor() {
		Parameter t = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
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
		getInitEditor().setParameters(new Parameter[] {t, x, y, vx, vy});
	}

	/**
	 * Creates and initializes the ModelFunctionPanel.
	 */
	protected void initializeFunctionPanel() {
		// create panel
	  functionEditor = new UserFunctionEditor();
		functionPanel = new DynamicFunctionPanel(functionEditor, this);
		// create main force functions
		String[] funcVars = new String[] {"x", "vx", //$NON-NLS-1$ //$NON-NLS-2$ 
  			"y", "vy", "t"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
	protected Point2D[] getNextTracePositions() {
		for (int i = 0; i < iterationsPerStep; i++) {
			solver.step();
		}
		setTracePositions(getState());
		return points;
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
	 * Gets the cartesian state {x, vx, y, vy, t} of a PointMass at a specified frame number.
	 * 
	 * @param target the PointMass
	 * @param frameNumber the frame number
	 * @return the state, or null if the point mass is not marked at the frame number
	 */
	protected double[] getCartesianState(PointMass target, int frameNumber) {
		DatasetManager data = target.getData(trackerPanel);
		
		// determine the dataset index for the specified frame number
		Dataset ds = data.getDataset(data.getDatasetIndex("frame")); //$NON-NLS-1$
		int index = -1;
		double[] frames = ds.getYPoints();
		for (int i=0; i<frames.length; i++) {
			if (frames[i]==frameNumber) {
				index = i;
				break;
			}
		}
		if (index==-1) return null;
		
		double[] state = new double[5]; // {x, vx, y, vy, t}
		// x
		ds = data.getDataset(data.getDatasetIndex("x")); //$NON-NLS-1$
		Object val = ds.getValueAt(index, 1);	
		state[0] = val==null? Double.NaN: (Double)val;		
		// vx
		ds = data.getDataset(data.getDatasetIndex("v_{x}")); //$NON-NLS-1$
		val = ds.getValueAt(index, 1);	
		state[1] = val==null? Double.NaN: (Double)val;		
		// y
		ds = data.getDataset(data.getDatasetIndex("y")); //$NON-NLS-1$
		val = ds.getValueAt(index, 1);	
		state[2] = val==null? Double.NaN: (Double)val;		
		// vy
		ds = data.getDataset(data.getDatasetIndex("v_{y}")); //$NON-NLS-1$
		val = ds.getValueAt(index, 1);
		state[3] = val==null? Double.NaN: (Double)val;		
		// t
		val = ds.getValueAt(index, 0);	
		state[4] = val==null? Double.NaN: (Double)val;		
		
		return state;		
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
	 * Determines if a specified point mass is a booster of this particle
	 * (or a booster of a booster, etc).
	 * 
	 * @param target a point mass
	 * @return true if the target is a booster
	 */
	protected boolean isBoostedBy(PointMass target) {
		if (modelBooster==null || modelBooster.booster==null)
			return false;
		if (modelBooster.booster==target)
			return true;
		if (modelBooster.booster instanceof DynamicParticle) {
			DynamicParticle dp = (DynamicParticle)modelBooster.booster;
			return dp.isBoostedBy(target);
		}
		return false;
	}
	
  /**
	 * Sets the initial conditions to those of the booster at the current start frame.
	 */
	protected void boost() {
		if (modelBooster==null || modelBooster.booster==null)
			return;
		
		int frameNumber = getStartFrame();
  	double[] state = getCartesianState(modelBooster.booster, frameNumber); // {x, vx, y, vy, t}		
  	if (state==null) return;
		
		Parameter[] params = getInitEditor().getParameters();
		for (int i = 0; i < params.length; i++) {
			Parameter param = params[i];
			String name = param.getName();
			double value = Double.NaN; // default
			
			if (name.equals("x")) value = state[0]; //$NON-NLS-1$
			else if (name.equals("vx")) value = state[1]; //$NON-NLS-1$
			else if (name.equals("y")) value = state[2]; //$NON-NLS-1$
			else if (name.equals("vy")) value = state[3]; //$NON-NLS-1$
			
			// replace parameter with new one if not null
			if (!Double.isNaN(value)) {
				Parameter newParam = new Parameter(name, String.valueOf(value));
				newParam.setDescription(param.getDescription());
				newParam.setNameEditable(false);
				params[i] = newParam;
			}
		}
		getInitEditor().setParameters(params);
		if (system!=null) {
			system.refreshSystemParameters();
			system.lastValidFrame = -1;				
			system.refreshSteps();
		}
		else {
			reset();
		}
		repaint();

	}
  /**
   * A ModelBooster manages a "booster" PointMass used to set initial values of this model.
   * To use the booster, call the DynamicParticle boost() method.
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
  		if (booster!=null) {
  			booster.removePropertyChangeListener(this);
  		}
  		booster = pm;
  		if (booster!=null) {
				boost();
				booster.addPropertyChangeListener(this);  			
  		}
  	}
  	
    /**
     * Implements PropertyChangeListener.
     * 
     * @param e the event
     */
		public void propertyChange(PropertyChangeEvent e) {
			if (booster==null) return;
			
			String propName = e.getPropertyName();
			
			if (propName.equals("adjusting")) { //$NON-NLS-1$
				adjusting = (Boolean)e.getNewValue();
				// change property to "steps" so update will be triggered below when adjusting is false
				propName = "steps"; //$NON-NLS-1$
			}
			if (adjusting) {
				return;
			}

			if (!(propName.contains("step") || propName.equals("data"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return; 
			}
			
			if (e.getPropertyName().equals("steps") && booster instanceof ParticleModel) { //$NON-NLS-1$
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
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      // save particle model data
    	DynamicParticle p = (DynamicParticle)obj;
      XML.getLoader(ParticleModel.class).saveObject(control, obj);
      if (p.system!=null) control.setValue("in_system", true); //$NON-NLS-1$
      if (p.modelBooster!=null && p.modelBooster.booster!=null) {
      	control.setValue("booster", p.modelBooster.booster.getName()); //$NON-NLS-1$
      }
    }

    /**
     * Creates a new object.
     *
     * @param control the control with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new DynamicParticle();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	DynamicParticle p = (DynamicParticle)obj;
    	try {
    		XML.getLoader(ParticleModel.class).loadObject(control, obj);
    		p.inSystem = control.getBoolean("in_system"); //$NON-NLS-1$
    		p.boosterName = control.getString("booster"); //$NON-NLS-1$
    	} catch(Exception ex) {
    		// load legacy xml
	    	String solver = control.getString("solver"); //$NON-NLS-1$
	    	if (solver != null) {
	        try { // load the solver class
	          Class<?> solverClass = Class.forName(solver);
	          p.setSolver(solverClass);
	        } catch(Exception ex2) {/** empty block */}  	    		
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
