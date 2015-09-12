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

import java.awt.geom.*;

import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.tools.UserFunction;
import org.opensourcephysics.tools.UserFunctionEditor;

/**
 * AnalyticParticle models a particle using time functions.
 *
 * @author W. Christian, D. Brown
 */
public class AnalyticParticle
    extends ParticleModel {
	
	UserFunction[] functions;
	
	/**
	 * Constructor.
	 */
  public AnalyticParticle() {
  }

	/**
	 * Creates and initializes an AnalyticFunctionPanel.
	 */
	protected void initializeFunctionPanel() {
		// create panel
	  functionEditor = new UserFunctionEditor();
		functionPanel = new AnalyticFunctionPanel(functionEditor, this);
		// add mass and initial time parameters
		createMassAndTimeParameters();
		// set main position functions "x" and "y"
		String[] t = new String[] {"t"}; //$NON-NLS-1$
	  UserFunction[] ff = new UserFunction[2];
	  ff[0] = new UserFunction("x"); //$NON-NLS-1$	    
	  ff[0].setNameEditable(false);
	  ff[0].setExpression("0", t); //$NON-NLS-1$
	  ff[0].setDescription(TrackerRes.getString("AnalyticParticle.PositionFunction.X.Description")); //$NON-NLS-1$
	  ff[1] = new UserFunction("y"); //$NON-NLS-1$
	  ff[1].setNameEditable(false);
	  ff[1].setExpression("0", t); //$NON-NLS-1$
	  ff[1].setDescription(TrackerRes.getString("AnalyticParticle.PositionFunction.Y.Description")); //$NON-NLS-1$
	  functionEditor.setMainFunctions(ff);
	}
	
  /**
	 * Gets the next trace position. Subclasses override to get positions based
	 * on model.
	 */
	protected Point2D[] getNextTracePositions() {
    double x = functions[0].evaluate(time);
    double y = functions[1].evaluate(time);
    point.setLocation(x, y);
		return new Point2D[] {point};
	}

  /**
	 * Resets model parameters and sets position(s) for start frame.
	 */
	protected void reset() {
		super.reset();
    t0 = getInitialValues()[0]; // time at start frame
  	functions = getFunctionEditor().getMainFunctions();
	  if (trackerPanel != null) {
	  	erase();
    	dt = trackerPanel.getPlayer().getMeanStepDuration() / (1000*tracePtsPerStep);
      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
      // find last frame included in both model and clip
  		int end = Math.min(getEndFrame(), clip.getFrameCount()-1);
  		while (end>getStartFrame() && !clip.includesFrame(end)) {
  			end--;
  		}
    	// check if end and start frame are same
  		if (end==getStartFrame() && !clip.includesFrame(getStartFrame())) {
  			// no frames to be marked, so clear! 				
  			steps.setLength(1);
	  		steps.setStep(0, null);
      	for (TrackerPanel panel: panels) {
      		getVArray(panel).setLength(0);
      		getAArray(panel).setLength(0);
      	}
		    traceX = new double[0];
		    traceY = new double[0];
		    support.firePropertyChange("steps", null, null); //$NON-NLS-1$
  	    return;
  		}
    	// find first frame included in both model and clip
    	int firstFrameInClip = getStartFrame();
    	while (firstFrameInClip<end && !clip.includesFrame(firstFrameInClip)) {
    		firstFrameInClip++;
    	}
    	// mark a step at firstFrameInClip
	  	steps.setLength(firstFrameInClip+1);
	    PositionStep step = (PositionStep)getStep(firstFrameInClip);
	  	for (int i = 0; i<steps.length; i++) {
	  		if (i<firstFrameInClip)
	  			steps.setStep(i, null);
	  		else if (step==null) {
      		step = new PositionStep(this, firstFrameInClip, 0, 0);
      		step.setFootprint(getFootprint());	  			
          steps.setStep(firstFrameInClip, step);
	  		}	  			
	  	}
	  	getVArray(trackerPanel).setLength(0);
	  	getAArray(trackerPanel).setLength(0);
	  	// set position of step at firstFrameInClip
	    ImageCoordSystem coords = trackerPanel.getCoords();
	    // get underlying coords if reference frame
	    while (coords instanceof ReferenceFrame) {
	      coords = ( (ReferenceFrame) coords).getCoords();
	    }
	    AffineTransform transform = coords.getToImageTransform(firstFrameInClip); 
    	UserFunction[] functions = getFunctionEditor().getMainFunctions();
    	// get time at firstFrameInClip
    	time = trackerPanel.getPlayer().getFrameTime(firstFrameInClip)/1000;
      double x = functions[0].evaluate(time);
      double y = functions[1].evaluate(time);
      point.setLocation(x, y);
	    transform.transform(point, point);
	    traceX = new double[] {point.getX()};
	    traceY = new double[] {point.getY()};
	    step.getPosition().setPosition(point); // this method is fast
	    lastValidFrame = firstFrameInClip;
	    support.firePropertyChange("step", null, firstFrameInClip); //$NON-NLS-1$
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
  static class Loader extends ParticleModel.Loader {

    public Object createObject(XMLControl control){
      return new AnalyticParticle();
    }
    
    public Object loadObject(XMLControl control, Object obj) {
    	try {
    		XML.getLoader(ParticleModel.class).loadObject(control, obj);
    	} catch(Exception ex) {
    		// load legacy xml
	      AnalyticParticle p = (AnalyticParticle)obj;
	  		String t = control.getString("t0"); //$NON-NLS-1$
	  		p.getInitEditor().setExpression("t", t, false); //$NON-NLS-1$
	  		String x = control.getString("x"); //$NON-NLS-1$
	  		p.getFunctionEditor().setExpression("x", x, false); //$NON-NLS-1$
	  		String y = control.getString("y"); //$NON-NLS-1$
	  		p.getFunctionEditor().setExpression("y", y, false); //$NON-NLS-1$
	  		p.reset();
    	}
      return obj;
    }
    
  }

}
