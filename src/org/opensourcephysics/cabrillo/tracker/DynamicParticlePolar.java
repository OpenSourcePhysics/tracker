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

import org.opensourcephysics.tools.FunctionEditor;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.UserFunction;
import org.opensourcephysics.tools.UserFunctionEditor;
import org.opensourcephysics.controls.*;

/**
 * DynamicParticlePolar models a particle using Newton's 2nd law in polar coordinates.
 *
 * @author D. Brown
 * @version 1.0
 */
public class DynamicParticlePolar extends DynamicParticle {
		
  /**
   * Gets the initial state {x, vx, y, vy, t}.
   * 
   * @return the initial state
   */
  public double[] getInitialState() {
    double[] polar = getInitialValues();
  	// polar is {t, r, theta, vr, omega}
  	double cos = Math.cos(polar[2]);
  	double sin = Math.sin(polar[2]);
  	if (Math.abs(cos) < 0.0000001) cos = 0;
  	if (Math.abs(sin) < 0.0000001) sin = 0;
  	double romega = polar[1]*polar[4];
  	// initial state is  {x, vx, y, vy, t}
  	initialState[0] = polar[1]*cos; // x = r*cos(theta)
  	initialState[1] = polar[3]*cos - romega*sin;	// vx = vr*cos(theta)-r*omega*sin(theta)
  	initialState[2] = polar[1]*sin; // y = r*sin(theta)
  	initialState[3] = polar[3]*sin + romega*cos;	// vy = vr*sin(theta)+r*omega*cos(theta)
  	initialState[4] = polar[0]; // t
    return initialState;
  }

  /**
   * Gets the x- and y-forces based on a specified cartesian state {x, vx, y, vy, t}.
   * 
   * @param cartesianState the state
   * @return the forces
   */
  protected double[] getXYForces(double[] cartesianState) {
  	// cartesianState is {x, vx, y, vy, t}
    UserFunction[] f = getFunctionEditor().getMainFunctions();
  	// get polar state {r, vr, theta, omega, t} to evaluate polar functions 
  	double[] polarState = getPolarState(cartesianState);
    double fr = f[0].evaluate(polarState);
    double ftheta = f[1].evaluate(polarState);
  	double cos = Math.cos(polarState[2]);
  	double sin = Math.sin(polarState[2]);
    double fx = (fr*cos - ftheta*sin);
    double fy = (fr*sin + ftheta*cos);
    return new double[] {fx, fy};
  }
  
	/**
	 * Creates and initializes the ModelFunctionPanel.
	 */
	protected void initializeFunctionPanel() {
		// create panel
	  functionEditor = new UserFunctionEditor();
		functionPanel = new DynamicFunctionPanel(functionEditor, this);
		// create main force functions
		UserFunction[] uf = new UserFunction[2];
	  String[] funcVars = new String[] {"r", "vr", //$NON-NLS-1$ //$NON-NLS-2$ 
	  		FunctionEditor.THETA, FunctionEditor.OMEGA, "t"}; //$NON-NLS-1$
	  uf[0] = new UserFunction("fr"); //$NON-NLS-1$   
	  uf[0].setNameEditable(false);
	  uf[0].setExpression("0", funcVars); //$NON-NLS-1$
	  uf[0].setDescription(TrackerRes.getString("DynamicParticle.ForceFunction.R.Description")); //$NON-NLS-1$
	  String s = "f"+FunctionEditor.THETA;	 //$NON-NLS-1$
	  uf[1] = new UserFunction(s);	  
	  uf[1].setNameEditable(false);
	  uf[1].setExpression("0", funcVars); //$NON-NLS-1$
	  uf[1].setDescription(TrackerRes.getString("DynamicParticle.ForceFunction.Theta.Description")); //$NON-NLS-1$
	  functionEditor.setMainFunctions(uf);
		// create mass and initial time parameters
		createMassAndTimeParameters();
	}
	
	/**
	 * Creates the initial position and velocity parameters.
	 */
	protected void initializeInitEditor() {
		Parameter t = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		Parameter r = new Parameter("r", "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
		r.setNameEditable(false);
		r.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialR.Description")); //$NON-NLS-1$
		Parameter th = new Parameter(FunctionEditor.THETA, "0.0"); //$NON-NLS-1$
		th.setNameEditable(false);
		th.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialTheta.Description")); //$NON-NLS-1$
		Parameter v = new Parameter("vr", "0.0"); //$NON-NLS-1$ //$NON-NLS-2$
		v.setNameEditable(false);
		v.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialVelocityR.Description")); //$NON-NLS-1$
		Parameter w = new Parameter(FunctionEditor.OMEGA, "0.0"); //$NON-NLS-1$
		w.setNameEditable(false);
		w.setDescription(TrackerRes.getString("DynamicParticle.Parameter.InitialOmega.Description")); //$NON-NLS-1$
		getInitEditor().setParameters(new Parameter[] {t, r, th, v, w});
	}
	
  /**
	 * Converts a cartesian state {x, vx, y, vy, t} to polar {r, vr, theta, omega, t},
	 * both relative to the origin.
	 * 
	 * @param state the cartesian state
	 * @return the polar state
	 */
  protected double[] getPolarState(double[] state) {
  	// state is {x, vx, y, vy, t}
  	double[] polarState = new double[5];
  	double dx = state[0];
  	double dy = state[2];
  	double vx = state[1];
  	double vy = state[3];
  	double r = Math.sqrt(dx*dx + dy*dy);
  	double v = Math.sqrt(vx*vx + vy*vy);
  	double rang = Math.atan2(dy, dx);
  	double vang = Math.atan2(vy, vx);
  	double dang = vang-rang;
  	// polar state is {r, vr, theta, omega, t}
  	polarState[0] = r; // r
  	polarState[1] = r == 0? v: v*Math.cos(dang); // vr
  	polarState[2] = r == 0? vang: rang; // theta
  	polarState[3] = r == 0? 0: v*Math.sin(dang)/r; // omega
  	polarState[4] = state[4]; // t
    return polarState;
  }

  /**
	 * Sets the initial conditions to those of the booster.
	 */
  @Override
	protected void boost() {
  	if (this instanceof DynamicSystem) {
  		return;
  	}
		if (modelBooster==null || modelBooster.booster==null)
			return;
		
		int frameNumber = getStartFrame();
  	double[] state = getCartesianState(modelBooster.booster, frameNumber); // {x, vx, y, vy, t}		
  	
  	if (state==null) return;
  	double[] polarState = getPolarState(state);
  	// polar is {r, vr, theta, omega, t}
		
		Parameter[] params = getInitEditor().getParameters();
		for (int i = 0; i < params.length; i++) {
			Parameter param = params[i];
			String name = param.getName();
			double value = Double.NaN; // default
			
			if (name.equals("r")) value = polarState[0]; //$NON-NLS-1$
			else if (name.equals("vr")) value = polarState[1]; //$NON-NLS-1$
			else if (name.equals(FunctionEditor.THETA)) value = polarState[2];
			else if (name.equals(FunctionEditor.OMEGA)) value = polarState[3];
			
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
    	DynamicParticle p = (DynamicParticle)obj;
      // save particle model data
      XML.getLoader(ParticleModel.class).saveObject(control, obj);
      if (p.system!=null) control.setValue("in_system", true); //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the control with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new DynamicParticlePolar();
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
    	XML.getLoader(ParticleModel.class).loadObject(control, obj);
  		p.inSystem = control.getBoolean("in_system"); //$NON-NLS-1$
      return obj;
    }
  }

}
