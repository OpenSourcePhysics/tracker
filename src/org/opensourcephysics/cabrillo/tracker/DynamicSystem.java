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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionEditor;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.UserFunction;
import org.opensourcephysics.tools.UserFunctionEditor;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DrawingPanel;

/**
 * This models a system of 2 particles that interact via internal forces.
 *
 * @author D. Brown
 * @version 1.0
 */
public class DynamicSystem extends DynamicParticlePolar {
	
	protected DynamicParticle[] particles = new DynamicParticle[0];
	protected ParticleModel[] models = new ParticleModel[0];
	protected double[] particleState = new double[5];
  protected DynamicSystemInspector systemInspector;
  protected JMenuItem systemInspectorItem;
  protected String[] particleNames = new String[0];
  protected StepArray realSteps;
  protected StepArray noSteps;
  protected int systemInspectorX = Integer.MIN_VALUE, systemInspectorY;
  protected TreeMap<Integer,double[]> relativeStates = new TreeMap<Integer,double[]>();
  protected boolean refreshing = false;

	/**
	 * No-arg constructor.
	 */
	public DynamicSystem() {
		this(new DynamicParticle[0]);
	}
  
	/**
	 * Constructor with particles.
	 * 
	 * @param parts an array of up to two dynamic particles
	 */
  public DynamicSystem(DynamicParticle[] parts) {
    super();
		defaultColors = new Color[] {new Color(51, 204, 51)};
    massField.setMinValue(0);
    realSteps = steps;
    noSteps = new StepArray();
		Parameter massParam = (Parameter)getParamEditor().getObject("m"); //$NON-NLS-1$
		massParam.setExpressionEditable(false);
    setName(TrackerRes.getString("DynamicSystem.New.Name")); //$NON-NLS-1$
    setFootprints(new Footprint[]
      {PointShapeFootprint.getFootprint("Footprint.SolidDiamond"), //$NON-NLS-1$
    	 PointShapeFootprint.getFootprint("Footprint.Spot"), //$NON-NLS-1$       
       PointShapeFootprint.getFootprint("Footprint.SolidTriangle"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.SolidCircle"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldVerticalLine"), //$NON-NLS-1$
       PointShapeFootprint.getFootprint("Footprint.BoldHorizontalLine"), //$NON-NLS-1$
       new PositionVectorFootprint(this, "Footprint.BoldPositionVector", 2)}); //$NON-NLS-1$
    defaultFootprint = getFootprint();
    setColor(defaultColors[0]);
    locked = true;
    setParticles(parts);
	}
	
	/**
	 * Overrides DynamicParticle draw method.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g the graphics context on which to draw
	 */
	public void draw(DrawingPanel panel, Graphics _g) {
    if (!(panel instanceof TrackerPanel) || trackerPanel==null) return;
    panels.add((TrackerPanel)panel);   // keep a list of drawing panels
    // add particles in particleNames
    if (particleNames.length > 0) {
    	ArrayList<DynamicParticle> toAdd = new ArrayList<DynamicParticle>();
      ArrayList<DynamicParticle> parts = trackerPanel.getDrawables(DynamicParticle.class);
      for (int i = 0; i < particleNames.length; i++) {
        for (DynamicParticle p: parts) {
          if (p.getName().equals(particleNames[i])) {
          	toAdd.add(p);
            particleNames[i] = null;
          }
        }
      }
      setParticles(toAdd.toArray(new DynamicParticle[0]));
      boolean empty = true;
      for (String name: particleNames) {
        empty = name==null && empty;
      }
      if (empty) particleNames = new String[0];
    }
    getInspector();
    if (systemInspectorX != Integer.MIN_VALUE
				&& trackerPanel.getTFrame() != null) {
			// set system inspector position
    	getSystemInspector();
			TFrame frame = trackerPanel.getTFrame();
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = Math.max(frame.getLocation().x + systemInspectorX, 0);
			x = Math.min(x, dim.width-systemInspector.getWidth());
			int y = Math.max(frame.getLocation().y + systemInspectorY, 0);
			y = Math.min(y, dim.height-systemInspector.getHeight());
			systemInspector.setLocation(x, y);
			systemInspectorX = Integer.MIN_VALUE;
			Runnable runner = new Runnable() {
	    	public void run() {
	    		systemInspector.setVisible(true);
	    	}
	    };
	    SwingUtilities.invokeLater(runner);		
		}
    if (particles.length==0) {
    	return;
    }
    if (trackerPanel.getFrameNumber() > lastValidFrame) {
    	refreshSteps();
    }
    for (ParticleModel next: getModels()) {
    	next.drawMe(panel, _g);
    }
	}

  /**
   * Gets a display name for this model. 
   *
   * @return the display name
   */
  public String getDisplayName() {
		StringBuffer buf  = new StringBuffer(getName());
		buf.append(" ("); //$NON-NLS-1$
		if (particles == null || particles.length==0) {
			buf.append(TrackerRes.getString("DynamicSystem.Empty")); //$NON-NLS-1$
		}
		else {
			for (int i = 0; i < particles.length; i++) {
				if (i > 0) buf.append(" + "); //$NON-NLS-1$
				buf.append(particles[i].getName());
			}
		}
		buf.append(")"); //$NON-NLS-1$
		return buf.toString();
  }
  
  /**
   * Returns a menu with items that control this track.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(TrackerPanel trackerPanel) {
    // create a system inspector item
  	systemInspectorItem = new JMenuItem(TrackerRes.getString("DynamicSystem.MenuItem.Inspector")); //$NON-NLS-1$
  	systemInspectorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	DynamicSystemInspector inspector = getSystemInspector();
        inspector.updateDisplay();
        inspector.setVisible(true);
      }
    });
    // assemble the menu
    JMenu menu = super.getMenu(trackerPanel);
    menu.add(systemInspectorItem, 1);
    return menu;
  }

	/**
	 * Overrides TTrack getToolbarTrackComponents method.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a list of components
	 */
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
		ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
		massField.setEnabled(false);
		return list;
	}

  /**
   * Gets the mass. Overrides PointMass method.
   *
   * @return the mass
   */
  public double getMass() {
  	// set mass to sum of particle masses
  	mass = 0;
  	if (particles==null) 
  		return mass;
    for (int i = 0; i < particles.length; i++) {
    	mass += particles[i].getMass();
    }
    return mass;
  }
  
  /**
   * Overrides TTrack isDependent method to return true.
   *
   * @return <code>true</code> if this track is dependent
   */
  public boolean isDependent() {
    return true;
  }

  /**
   * Adds a particle to this system.
   * 
   * @param particle the particle to add
   * @return true if particle added
   */
  public boolean addParticle(DynamicParticle particle) {
		if (particles.length == 2) return false; // can't exceed two particles
  	for (DynamicParticle next: particles) {
  		if (next == particle) return false; // already contains particle
  	}
		// make a new particles array
		DynamicParticle[] newParticles = new DynamicParticle[particles.length+1];
		System.arraycopy(particles, 0, newParticles, 0, particles.length);
		newParticles[particles.length] = particle;
		return setParticles(newParticles);		
  }
  
  /**
   * Removes a particle from this system.
   * 
   * @param particle the particle to remove
   * @return true if particle removed
   */
  public boolean removeParticle(DynamicParticle particle) {
  	if (particles.length==1 && particles[0]==particle) {
  		return setParticles(new DynamicParticle[0]);
  	}
  	if (particles.length==2) {
  		if (particles[0]==particle)
    		return setParticles(new DynamicParticle[] {particles[1]});
  		if (particles[1]==particle)
    		return setParticles(new DynamicParticle[] {particles[0]});  			
  	}
		return false;
  }

  /**
   * Sets the particles in this system.
   * 
   * @param newParticles an array of zero to two dynamic particles
   * @return true if particles accepted
   */
  public boolean setParticles(DynamicParticle[] newParticles) {
  	if (newParticles==null || newParticles.length>2) {
  		return false;
  	}
  	for (DynamicParticle next: newParticles) {
  		if (next==null) {
  			return false;
  		}
  	}
  	if (newParticles.length==2) {
  		DynamicParticle problem = null;
  		if (newParticles[0].isBoostedBy(newParticles[1])) {
  			problem = newParticles[0];
  		}
  		else if (newParticles[1].isBoostedBy(newParticles[0])) {
  			problem = newParticles[1];
  		}
  		if (problem!=null) {
      	String message = TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Message1")+"\n" //$NON-NLS-1$ //$NON-NLS-2$
      			+TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Message2")+" " //$NON-NLS-1$ //$NON-NLS-2$
      			+problem.getName()+"\n" //$NON-NLS-1$
      			+TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Message3"); //$NON-NLS-1$ 
      	int response = javax.swing.JOptionPane.showConfirmDialog(
      			trackerPanel.getTFrame(), 
      			message,
      			TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Title"), //$NON-NLS-1$ 
      			javax.swing.JOptionPane.OK_CANCEL_OPTION, 
      			javax.swing.JOptionPane.WARNING_MESSAGE);
      	if (response == javax.swing.JOptionPane.YES_OPTION) {
      		problem.setBooster(null);
      	}
      	else return false;
  		}
  	}
  	
  	// clean up particles that will be removed
  	for (DynamicParticle particle: particles) {
  		boolean cleanMe = true;
    	for (DynamicParticle next: newParticles) {
    		if (next == particle) {
    			cleanMe = false;
    		}
    	}
    	if (cleanMe) {
				particle.system = null;
				particle.inSystem = false;
				particle.refreshInitialTime();
				particle.removePropertyChangeListener(this);
				particle.lastValidFrame = -1;
				particle.repaint();
				if (systemInspector!=null) {
					particle.removePropertyChangeListener("name", systemInspector); //$NON-NLS-1$
					particle.removePropertyChangeListener("color", systemInspector); //$NON-NLS-1$
					particle.removePropertyChangeListener("footprint", systemInspector); //$NON-NLS-1$
				}
    	}
  	}
  	particles = new DynamicParticle[newParticles.length];
  	System.arraycopy(newParticles, 0, particles, 0, newParticles.length);
		state = new double[particles.length*4+1];
		initialState = new double[particles.length*4+1];
  	models = new ParticleModel[0];
    // update inspector, if visible
    if (systemInspector != null &&
    		systemInspector.isVisible()) {
    	systemInspector.updateDisplay();
    }
    // make new points arrary
		points = new Point2D[particles.length+1];
		for (int i = 0; i < particles.length; i++) {
			points[i] = new Point2D.Double();
		}		
		points[points.length-1] = point;
		
		for (int i = 0; i < particles.length; i++) {
			particles[i].removePropertyChangeListener(this);
			particles[i].addPropertyChangeListener(this);
			particles[i].system = this;
			particles[i].refreshInitialTime();
			if (systemInspector!=null) {
				particles[i].removePropertyChangeListener("name", systemInspector); //$NON-NLS-1$
				particles[i].removePropertyChangeListener("color", systemInspector); //$NON-NLS-1$
				particles[i].removePropertyChangeListener("footprint", systemInspector); //$NON-NLS-1$
				particles[i].addPropertyChangeListener("name", systemInspector); //$NON-NLS-1$
				particles[i].addPropertyChangeListener("color", systemInspector); //$NON-NLS-1$
				particles[i].addPropertyChangeListener("footprint", systemInspector); //$NON-NLS-1$
			}
		}
    refreshSystemParameters();
    if (inspector != null)
    	inspector.refreshDropdown(null);
    if (particles.length==0 && steps != noSteps) {
    	steps = noSteps;
  		support.firePropertyChange("steps", null, null); //$NON-NLS-1$
    }
    else if (particles.length>0 && steps != realSteps) {
    	steps = realSteps;
  		support.firePropertyChange("steps", null, null); //$NON-NLS-1$
    }
    lastValidFrame = -1;    
    repaint();
		return true;
  }
  
  /**
   * Deletes this system. Overrides DynamicParticle method to clean up
   * particles after deleting.
   * 
   */
  public void delete() {
    setParticles(new DynamicParticle[0]);
    super.delete();
  }

  /**
   * Gets the rate based on a specified state.
   * 
   * @param state the state
   * @param rate the rate of change of the state
   */
	public void getRate(double[] state, double[] rate) {
    rate[rate.length-1] = 1; // dt/dt=1
    if (particles.length == 0) {
    	return;
    }
		// one particle, no interactions: state is {x1, vx1, y1, vy1, t},
    // rate is {vx1, ax1, vy1, ay1, 1}
		if (particles.length == 1) {
    	double[] particleState = getState(particles[0]);
      double[] forces = particles[0].getXYForces(particleState);
      double m = particles[0].getMass();
      rate[0] = state[1]; // dx/dt = vx
      rate[1] = forces[0] / m; // dvx/dt = ax = fx/m
      rate[2] = state[3]; // dy/dt = vy
      rate[3] = forces[1] / m; // dvy/dt = ay = fy/m
			return;
		}
		// two particles, one interaction: state is {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t},
    // rate is {vx1, ax1, vy1, ay1, vx2, ax2, vy2, ay2, 1}
    UserFunction[] f = getFunctionEditor().getMainFunctions();
  	// use relative polar state {r, vr, theta, omega, t} to get interaction forces
  	double[] polarState = getRelativePolarState(state);
  	double cos = Math.cos(polarState[2]);
  	double sin = Math.sin(polarState[2]);
    double fr = f[0].evaluate(polarState);
    double ftheta = f[1].evaluate(polarState);
    // use particle states {x, vx, y, vy, t} to get external forces on particles
    for (int i = 0; i < particles.length; i++) {
    	double[] particleState = getState(particles[i]);
      double[] forces = particles[i].getXYForces(particleState);
      double m = particles[i].getMass();
      int sign = i==0? 1: -1; // polar forces are opposite on particles[1]
      rate[4*i] = state[4*i+1]; // dx/dt = vx
      rate[4*i+1] = (forces[0] + sign*fr*cos - sign*ftheta*sin) / m; // dvx/dt = ax = fx/m
      rate[4*i+2] = state[4*i+3]; // dy/dt = vy
      rate[4*i+3] = (forces[1] + sign*fr*sin + sign*ftheta*cos) / m; // dvy/dt = ay = fy/m
    }
  }
  
	/**
	 * Gets the initial values.
	 * 
	 * @return initial values {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
	 */
	public double[] getInitialValues() {
    double[] state = null;
    // check initial state in case paint manager calls this unexpectedly
    if (initialState.length!=particles.length*4+1)
    	initialState = new double[particles.length*4+1];
		for (int i = 0; i < particles.length; i++) {
			state = particles[i].getInitialState();
	  	// state is {x, vx, y, vy, t}
			System.arraycopy(state, 0, initialState, 4*i, 4);
		}
		if (state != null)
			initialState[initialState.length-1] = state[state.length-1];
		else if (trackerPanel!=null) {
			double t0 = trackerPanel.getPlayer().getVideoClip().getStartTime();
			initialState[initialState.length-1] = t0/1000;
		}
		return initialState;
	}
	
  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
		if (name.equals("transform")) { //$NON-NLS-1$
			// workaround to prevent infinite loop
      ImageCoordSystem coords = trackerPanel.getCoords();
      if (coords instanceof ReferenceFrame) {
      	TTrack track = ((ReferenceFrame)coords).getOriginTrack();
      	if (track==this
      			|| (particles.length>0 && track==particles[0])
      			|| (particles.length>1 && track==particles[1])) {
        	return;
      	}
      }
			lastValidFrame = -1;				
			refreshSteps();
		}
		else super.propertyChange(e);
    if (name.equals("mass") || name.equals("function")) { //$NON-NLS-1$ //$NON-NLS-2$
    	refreshSystemParameters();
    	if (trackerPanel!=null)
    		trackerPanel.repaint();
    }
    else if (name.equals("name")) { //$NON-NLS-1$
    	refreshSystemParameters();
    }
  }
  
  @Override
  public void setFontLevel(int level) {
  	super.setFontLevel(level);
  	if (systemInspector!=null) {
  		FontSizer.setFonts(systemInspector, level);
  		systemInspector.updateDisplay();
  	}
  }

  /**
   * Gets the system inspector.
   *
   * @return the system inspector
   */
  public DynamicSystemInspector getSystemInspector() {
    if (systemInspector == null) {
    	systemInspector = new DynamicSystemInspector(this);
    	systemInspector.setLocation(200, 200);
			addPropertyChangeListener("name", systemInspector); //$NON-NLS-1$
			addPropertyChangeListener("color", systemInspector); //$NON-NLS-1$
			addPropertyChangeListener("footprint", systemInspector); //$NON-NLS-1$
    }
    return systemInspector;
  }

  /**
   * Gets the initial state. Overrides DynamicParticle method.
   * 
   * @return the initial state
   */
  public double[] getInitialState() {
    return getInitialValues();
  }

  /**
   * Refreshes the data. Overrides PointMass method.
   *
   * @param data the DatasetManager
   * @param trackerPanel the tracker panel
   */
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
    int count = 25; // number of datasets
    if (data.getDataset(0).getColumnName(0).equals("x")) { //$NON-NLS-1$
	    // assign column names to the datasets
	    String timeVar = "t"; //$NON-NLS-1$
    	data.getDataset(0).setXYColumnNames(timeVar, "x"); //$NON-NLS-1$
    	data.getDataset(1).setXYColumnNames(timeVar, "y"); //$NON-NLS-1$
    	data.getDataset(2).setXYColumnNames(timeVar, "r"); //$NON-NLS-1$
    	data.getDataset(3).setXYColumnNames(timeVar, "$\\theta$_{r}"); //$NON-NLS-1$
    	data.getDataset(4).setXYColumnNames(timeVar, "v_{x}"); //$NON-NLS-1$
    	data.getDataset(5).setXYColumnNames(timeVar, "v_{y}"); //$NON-NLS-1$
    	data.getDataset(6).setXYColumnNames(timeVar, "v"); //$NON-NLS-1$
    	data.getDataset(7).setXYColumnNames(timeVar, "$\\theta$_{v}"); //$NON-NLS-1$
    	data.getDataset(8).setXYColumnNames(timeVar, "a_{x}"); //$NON-NLS-1$
    	data.getDataset(9).setXYColumnNames(timeVar, "a_{y}"); //$NON-NLS-1$
    	data.getDataset(10).setXYColumnNames(timeVar, "a"); //$NON-NLS-1$
    	data.getDataset(11).setXYColumnNames(timeVar, "$\\theta$_{a}"); //$NON-NLS-1$
    	data.getDataset(12).setXYColumnNames(timeVar, "$\\theta$"); //$NON-NLS-1$
    	data.getDataset(13).setXYColumnNames(timeVar, "$\\omega$"); //$NON-NLS-1$
    	data.getDataset(14).setXYColumnNames(timeVar, "$\\alpha$"); //$NON-NLS-1$
    	data.getDataset(15).setXYColumnNames(timeVar, "step"); //$NON-NLS-1$
    	data.getDataset(16).setXYColumnNames(timeVar, "frame"); //$NON-NLS-1$
    	data.getDataset(17).setXYColumnNames(timeVar, "p_{x}"); //$NON-NLS-1$
    	data.getDataset(18).setXYColumnNames(timeVar, "p_{y}"); //$NON-NLS-1$
    	data.getDataset(19).setXYColumnNames(timeVar, "p"); //$NON-NLS-1$
    	data.getDataset(20).setXYColumnNames(timeVar, "$\\theta$_{p}"); //$NON-NLS-1$
    	data.getDataset(21).setXYColumnNames(timeVar, "r_{rel}"); //$NON-NLS-1$
    	data.getDataset(22).setXYColumnNames(timeVar, "$\\theta$_{rel}"); //$NON-NLS-1$
    	data.getDataset(23).setXYColumnNames(timeVar, "vr_{rel}"); //$NON-NLS-1$
    	data.getDataset(24).setXYColumnNames(timeVar, "$\\omega$_{rel}"); //$NON-NLS-1$
    }
    // fill dataDescriptions array
    dataDescriptions = new String[count+1];
    for (int i = 0; i < count-3; i++) {
      dataDescriptions[i] = TrackerRes.getString("PointMass.Data.Description."+i); //$NON-NLS-1$
    }
    for (int i = 0; i < 4; i++) {
      dataDescriptions[count-3+i] = TrackerRes.getString("DynamicSystem.Data.Description."+i); //$NON-NLS-1$
    }
    // get the rotational data
    Object[] rotationData = getRotationData();
    double[] theta_data = (double[])rotationData[0];
    double[] omega_data = (double[])rotationData[1];
    double[] alpha_data = (double[])rotationData[2];
    // clear datasets
    dataFrames.clear();
    for (int i = 0; i < count;i++) {
      data.getDataset(i).clear();
    }
    // get data at each non-null position step in the videoclip
    VideoPlayer player = trackerPanel.getPlayer();
    VideoClip clip = player.getVideoClip();
    double dt = player.getMeanStepDuration() / 1000.0;
    ImageCoordSystem coords = trackerPanel.getCoords();
    Step[] stepArray = getSteps();
    for (int n = 0; n < stepArray.length; n++) {
      if (stepArray[n] == null || !clip.includesFrame(n)) continue;
      int stepNumber = clip.frameToStep(n);
      double t = player.getStepTime(stepNumber)/1000.0;
      // assemble the data values for this step
      double[] vals = new double[count];
      TPoint p = ((PositionStep)stepArray[n]).getPosition();
      Point2D pt = p.getWorldPosition(trackerPanel);
      vals[0] = pt.getX(); // x
      vals[1] = pt.getY(); // y
      vals[2] = pt.distance(0, 0); //mag
      vals[3] = Math.atan2(pt.getY(), pt.getX()); // ang between +/-pi
      vals[4] = Double.NaN; // vx
      vals[5] = Double.NaN; //vy
      vals[6] = Double.NaN; // vmag
      vals[7] = Double.NaN; // vang
      vals[8] = Double.NaN; // ax
      vals[9] = Double.NaN; // ay
      vals[10] = Double.NaN; // amag
      vals[11] = Double.NaN; // aang
      vals[12] = theta_data[n]; // theta
      vals[13] = omega_data[n]/dt; // omega
      vals[14] = alpha_data[n]/(dt*dt); // alpha
      vals[15] = stepNumber; // step
      vals[16] = n; // frame
      vals[17] = Double.NaN; // px
      vals[18] = Double.NaN; // py
      vals[19] = Double.NaN; // pmag
      vals[20] = Double.NaN; // pang
      vals[21] = Double.NaN; // r_rel
      vals[22] = Double.NaN; // theta_rel
      vals[23] = Double.NaN; // vr_rel
      vals[24] = Double.NaN; // omega_rel
      if (particles.length==2) {
      	double[] relState = relativeStates.get(n); // {r, vr, theta, omega, t}
      	if (relState!=null) {
          vals[21] = relState[0]; // r_rel
          vals[22] = relState[2]; // theta_rel
          vals[23] = relState[1]; // vr_rel
          vals[24] = relState[3]; // omega_rel
      	}
      }
	    VectorStep veloc = getVelocity(n, trackerPanel);
	    if (veloc != null) {
	    	double imageX = veloc.getXComponent();
	    	double imageY = veloc.getYComponent();
	      vals[4] = coords.imageToWorldXComponent(n, imageX, imageY)/dt;
	      vals[5] = coords.imageToWorldYComponent(n, imageX, imageY)/dt;
	      double vsquared = vals[4]*vals[4] + vals[5]*vals[5];
	      vals[6] = Math.sqrt(vsquared);
	      vals[7] = Math.atan2(vals[5], vals[4]);
	      double mass = getMass();
	      vals[17] = mass*vals[4];
	      vals[18] = mass*vals[5];
	      vals[19] = mass*vals[6];
	      vals[20] = mass*vals[7];
	    }
      VectorStep accel = getAcceleration(n, trackerPanel);
      if (accel != null) {
      	double imageX = accel.getXComponent();
      	double imageY = accel.getYComponent();
        vals[8] = coords.imageToWorldXComponent(n, imageX, imageY)/(dt*dt);
        vals[9] = coords.imageToWorldYComponent(n, imageX, imageY)/(dt*dt);
        vals[10] = Math.sqrt(vals[8]*vals[8] + vals[9]*vals[9]);
        vals[11] = Math.atan2(vals[9], vals[8]);
      }
      // append points to datasets
      for (int i = 0; i < count; i++) {
      	data.getDataset(i).append(t, vals[i]);
      }
      dataFrames.add(new Integer(n));
    }
    // store the mass in the data properties
    Double m = getMass();
    String desc = TrackerRes.getString("ParticleModel.Parameter.Mass.Description"); //$NON-NLS-1$
    data.setConstant("m", m, m.toString(), desc); //$NON-NLS-1$
  }

//______________________________ protected methods __________________________
	
  /**
   * Cleans up associated resources when this track is deleted or cleared.
   */
  protected void cleanup() {
  	super.cleanup();
		if (systemInspector != null) systemInspector.dispose();
  }

  /**
	 * Refreshes initial time parameter for this model. Overrides ParticleModel.
	 */
	protected void refreshInitialTime() {
		super.refreshInitialTime();
		for (ParticleModel next: particles) {
			next.refreshInitialTime();
		}
	}
	
	/**
	 * Refreshes the initial positions, velocities and particle masses
	 * based on the values for the particles in this system.
	 */
	protected void refreshSystemParameters() {
		if (refreshing) return;
		refreshing = true;
  	double[] polarState; // polar state is {r, vr, theta, omega, t}
  	if (particles.length==2) {
  		polarState = getRelativePolarState(getInitialState());
  	}
  	else {
  		polarState = new double[] {0, 0, 0, 0, 0};
  	}
		double zeroPosition = 1E-12;
		if (trackerPanel != null) {
			zeroPosition = .001/trackerPanel.getCoords().getScaleX(0);
		}
		double zeroVelocity = 1E-11;
		if (trackerPanel != null) {
			zeroVelocity = 1000*zeroPosition/trackerPanel.getPlayer().getMeanStepDuration();
		}
		double zeroAngle = 1E-5;
		double zeroOmega = 1E-4;
		if (trackerPanel != null) {
			zeroOmega = 1000*zeroAngle/trackerPanel.getPlayer().getMeanStepDuration();
		}
	  String relative = "_"+TrackerRes.getString("DynamicSystem.Parameter.Name.Relative"); //$NON-NLS-1$ //$NON-NLS-2$
		String particleNames = " "; //$NON-NLS-1$
		if (particles.length>0) {
			particleNames += TrackerRes.getString("DynamicSystem.Parameter.Of")+" "; //$NON-NLS-1$ //$NON-NLS-2$
			particleNames += particles[0].getName()+" "; //$NON-NLS-1$
			particleNames += TrackerRes.getString("DynamicSystem.Parameter.RelativeTo")+" "; //$NON-NLS-1$ //$NON-NLS-2$
			particleNames += particles.length>1? particles[1].getName(): particles[0].getName();
		}
		// particle masses
		String desc = TrackerRes.getString("DynamicSystem.Parameter.Mass.Description"); //$NON-NLS-1$
		getParamEditor().setExpression("m", String.valueOf(getMass()), false); //$NON-NLS-1$
		getParamEditor().setDescription("m", desc); //$NON-NLS-1$
		Parameter m1 = (Parameter)getParamEditor().getObject("m1"); //$NON-NLS-1$
		Parameter m2 = (Parameter)getParamEditor().getObject("m2"); //$NON-NLS-1$
		desc = TrackerRes.getString("DynamicSystem.Parameter.ParticleMass.Description"); //$NON-NLS-1$
		if (particles.length==0) {
			if (m1!=null) {
				// must set name and expression editable before removing parameter
				m1.setNameEditable(true);
				m1.setExpressionEditable(true);
				getParamEditor().removeObject(m1, false);
			}
			if (m2!=null) {
				// must set name and expression editable before removing parameter
				m2.setNameEditable(true);
				m2.setExpressionEditable(true);
				getParamEditor().removeObject(m2, false);
			}
		}
		else {
			String value = FunctionEditor.format(particles[0].getMass(), 0);
			if (m1==null) {
				m1 = createParameter("m1", value, desc+" "+particles[0].getName()); //$NON-NLS-1$ //$NON-NLS-2$
				getParamEditor().addObject(m1, 1, false, false);
			}
			else 
				getParamEditor().setExpression("m1", value, false); //$NON-NLS-1$
			if (particles.length>1) {
				value = FunctionEditor.format(particles[1].getMass(), 0);
				if (m2==null) {
					m2 = createParameter("m2", value, desc+" "+particles[1].getName()); //$NON-NLS-1$ //$NON-NLS-2$
					getParamEditor().addObject(m2, 2, false, false);
				}
				else 
					getParamEditor().setExpression("m2", value, false); //$NON-NLS-1$
			}
			else {			
				if (m2!=null) {
					// must set name and expression editable before removing parameter
					m2.setNameEditable(true);
					m2.setExpressionEditable(true);
					getParamEditor().removeObject(m2, false);
				}
			}
		}
		for (DynamicParticle particle: particles) {
			if (particle.modelBooster!=null) {
				particle.modelBooster.setBooster(particle.modelBooster.booster);
			}
		}
		// initial values
		Parameter t = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		String value = FunctionEditor.format(polarState[0], zeroPosition);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialR.Description"); //$NON-NLS-1$
		Parameter r = createParameter("r"+relative, value, desc+particleNames); //$NON-NLS-1$
		value = FunctionEditor.format(polarState[2], zeroAngle);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialTheta.Description"); //$NON-NLS-1$
		Parameter theta = createParameter(FunctionEditor.THETA+relative, value, desc+particleNames);
		value = FunctionEditor.format(polarState[1], zeroVelocity);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialVelocityR.Description"); //$NON-NLS-1$
		Parameter vr = createParameter("vr"+relative, value, desc+particleNames); //$NON-NLS-1$
		value = FunctionEditor.format(polarState[3], zeroOmega);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialOmega.Description"); //$NON-NLS-1$
		Parameter omega = createParameter(FunctionEditor.OMEGA+relative, value, desc+particleNames);
		getInitEditor().setParameters(new Parameter[] {t, r, theta, vr, omega});
		refreshing = false;
	}
	
  /**
	 * Sets the positions of the trace points based on a specified state.
	 * 
	 * @param state the state
	 */
	protected void setTracePositions(double[] state) {
  	// state is {t} if no particles
  	// or {x1, vx1, y1, vy1, t} if one particle
  	// or {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t} if two particles
		if (particles.length==0) {
			return;
		}
		for (int i = 0; i < points.length-1; i++) {
	    points[i].setLocation(state[4*i], state[4*i+2]); // current ODE state
		}
		double mass = 0, xcm = 0, ycm = 0;
    for (int i = 0; i < particles.length; i++) {
      double m = particles[i].getMass();
      mass += m;
      xcm += m * state[4*i];
      ycm += m * state[4*i+2];
    }
		points[points.length-1].setLocation(xcm/mass, ycm/mass);
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
	  String internal = TrackerRes.getString("DynamicSystem.Force.Name.Internal"); //$NON-NLS-1$
	  uf[0] = new UserFunction("fr_"+internal); //$NON-NLS-1$   
	  uf[0].setNameEditable(false);
	  uf[0].setExpression("0", funcVars); //$NON-NLS-1$
	  uf[0].setDescription(TrackerRes.getString("DynamicSystem.ForceFunction.R.Description")); //$NON-NLS-1$
	  String s = "f"+FunctionEditor.THETA+"_"+internal;	 //$NON-NLS-1$ //$NON-NLS-2$
	  uf[1] = new UserFunction(s);	  
	  uf[1].setNameEditable(false);
	  uf[1].setExpression("0", funcVars); //$NON-NLS-1$
	  uf[1].setDescription(TrackerRes.getString("DynamicSystem.ForceFunction.Theta.Description")); //$NON-NLS-1$
	  functionEditor.setMainFunctions(uf);
		// create mass and initial time parameters
		createMassAndTimeParameters();
	}
	
  /**
	 * Gets the state of this system based on the states of its particles.
	 * 
	 * @param state the particle state {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
	 * @return the system state {x_cm, vx_cm, y_cm, vy_cm, t}
	 */
  protected double[] getSystemState(double[] state) {
		double mass = 0;
		for (int i = 0; i < particleState.length; i++) {
			particleState[i] = 0;
		}
    particleState[4] = state[state.length-1];	// time
		if (particles.length>0) {
			// state is {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
			// systemState is {x_cm, vx_cm, y_cm, vy_cm, t}
	    for (int i = 0; i < particles.length; i++) {
	      double m = particles[i].getMass();
	      mass += m;
	      particleState[0] += m * state[4*i];
	      particleState[1] += m * state[4*i+1];
	      particleState[2] += m * state[4*i+2];
	      particleState[3] += m * state[4*i+3];
	    }
	    particleState[0] /= mass;	// cm x coordinate
	    particleState[1] /= mass;	// cm y coordinate
	    particleState[2] /= mass;	// cm x velocity
	    particleState[3] /= mass;	// cm y velocity
		}
    return particleState;
	}
	
  /**
   * Gets the current state of the specified particle.
   * 
   * @param particle the particle
   * @return the state of the particle {x, vx, y, vy, t}
   */
	protected double[] getState(DynamicParticle particle) {
    for (int i = 0; i < particles.length; i++) {
    	if (particles[i] == particle) {
      	// state is {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
      	// particleState is {x, vx, y, vy, t}
	      particleState[0] = state[4*i];      
	      particleState[1] = state[4*i+1];      
	      particleState[2] = state[4*i+2];      
	      particleState[3] = state[4*i+3];
	      particleState[4] = state[state.length-1];
	      return particleState;
    	}
    }
    return null;
	}
	
  /**
	 * Gets the particle models associated with this model.
	 * 
	 * @return an array of particle models
	 */
	protected ParticleModel[] getModels() {
		if (models.length != particles.length+1) {
			models = new ParticleModel[particles.length+1];
			for (int i = 0; i < models.length-1; i++) {
				models[i] = particles[i];
			}
			models[models.length-1] = this;
		}
		return models;
	}
	
  /**
	 * Converts a two-body cartesian state {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
	 * to a relative polar state {r, vr, theta, omega, t}.
	 * 
	 * @param state the cartesian state of both particles
	 * @return the polar state of particle 1 relative to particle 2
	 */
  protected double[] getRelativePolarState(double[] state) {
  	// cartesian state is {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
  	double[] polarState = new double[5];
  	double dx = state[0]-state[4];
  	double dy = state[2]-state[6];
  	double vx = state[1]-state[5];
  	double vy = state[3]-state[7];
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
  	polarState[4] = state[8]; // t
  	double[] toSave = new double[polarState.length];
  	System.arraycopy(polarState, 0, toSave, 0, polarState.length);
  	int frameNum = trackerPanel.getFrameNumber();
  	relativeStates.put(frameNum, toSave);
    return polarState;
  }
  
//______________________________ private methods __________________________
	
	private Parameter createParameter(String name, String expression, String description) {
    Parameter p = new Parameter(name, expression);
    p.setExpressionEditable(false);
    p.setNameEditable(false);
    p.setDescription(description);
    return p;
	}
	
//________________________ static methods and classes ______________________
	
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
    	DynamicSystem system = (DynamicSystem)obj;
      // save particle names, if any
    	if (system.particles.length>0) {
        String[] names = new String[system.particles.length];
        for (int i = 0; i < names.length; i++) {
        	names[i] = system.particles[i].getName();
        }
        control.setValue("particles", names); //$NON-NLS-1$
    	}
    	// save system inspector location if visible
    	if (system.systemInspector!=null && system.systemInspector.isVisible()) {
    		Point p = system.systemInspector.getLocation();
  			// save location relative to frame
  			TFrame frame = system.trackerPanel.getTFrame();
    		control.setValue("system_inspector_x", p.x - frame.getLocation().x); //$NON-NLS-1$
    		control.setValue("system_inspector_y", p.y - frame.getLocation().y); //$NON-NLS-1$
    	}
      // save particle model data
      XML.getLoader(ParticleModel.class).saveObject(control, obj);
    }

    /**
     * Creates a new object.
     *
     * @param control the control with the object data
     * @return the newly created object
     */
    public Object createObject(XMLControl control){
      return new DynamicSystem();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	XML.getLoader(ParticleModel.class).loadObject(control, obj);
    	DynamicSystem system = (DynamicSystem)obj;
      // load mass names
      String[] names = (String[])control.getObject("particles"); //$NON-NLS-1$
      if (names != null) {
        system.particleNames = names;
      }
      system.systemInspectorX = control.getInt("system_inspector_x"); //$NON-NLS-1$
      system.systemInspectorY = control.getInt("system_inspector_y"); //$NON-NLS-1$
      return obj;
    }
  }

  
}
