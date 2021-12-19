/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
import org.opensourcephysics.tools.FunctionTool;
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
	protected TreeMap<Integer, double[]> frameRelativeStates = new TreeMap<Integer, double[]>();
	protected boolean refreshing = false;

	protected final static String[] dataVariables = new String[] {
			 "t",	// 0  //$NON-NLS-1$
			 "x",	// 1  //$NON-NLS-1$
			 "y",	// 2  //$NON-NLS-1$
			 "r",	// 3  //$NON-NLS-1$
			 "$\\theta$_{r}",	// 4  //$NON-NLS-1$
			 "v_{x}",	// 5  //$NON-NLS-1$
			 "v_{y}",	// 6  //$NON-NLS-1$
			 "v",	// 7  //$NON-NLS-1$
			 "$\\theta$_{v}",	// 8  //$NON-NLS-1$
			 "a_{x}",	// 9  //$NON-NLS-1$
			 "a_{y}",	// 10  //$NON-NLS-1$
			 "a",	// 11  //$NON-NLS-1$
			 "$\\theta$_{a}",	// 12  //$NON-NLS-1$
			 "$\\theta$",	// 13  //$NON-NLS-1$
			 "$\\omega$",	// 14  //$NON-NLS-1$
			 "$\\alpha$",	// 15  //$NON-NLS-1$
			 "step",	// 16  //$NON-NLS-1$
			 "frame",	// 17  //$NON-NLS-1$
			 "p_{x}",	// 18  //$NON-NLS-1$
			 "p_{y}",	// 19  //$NON-NLS-1$
			 "p",	// 20  //$NON-NLS-1$
			 "$\\theta$_{p}",	// 21  //$NON-NLS-1$
			 "r_{rel}",	// 22  //$NON-NLS-1$
			 "$\\theta$_{rel}",	// 23  //$NON-NLS-1$
			 "vr_{rel}",	// 24  //$NON-NLS-1$
			 "$\\omega$_{rel}",	// 25  //$NON-NLS-1$
	};

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
		defaultColors = new Color[] { new Color(51, 204, 51) };
		massField.setMinValue(0);
		realSteps = steps;
		noSteps = new StepArray();
		Parameter massParam = (Parameter) getParamEditor().getObject("m"); //$NON-NLS-1$
		massParam.setExpressionEditable(false);
		setName(TrackerRes.getString("DynamicSystem.New.Name")); //$NON-NLS-1$
		setFootprints(new Footprint[] { PointShapeFootprint.getFootprint("Footprint.SolidDiamond"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.Spot"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.SolidTriangle"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.SolidCircle"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.BoldVerticalLine"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.BoldHorizontalLine"), //$NON-NLS-1$
				PointShapeFootprint.getFootprint("Footprint.BoldPositionVector") }); //$NON-NLS-1$
		defaultFootprint = getFootprint();
		setColor(defaultColors[0]);
		locked = true;
		setParticles(parts);
	}

	/**
	 * Overrides DynamicParticle draw method.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		if (!(panel instanceof TrackerPanel) || tp == null)
			return;
		if (!initialized)
			initialize(tp);
		getModelBuilder();
		if (systemInspectorX != Integer.MIN_VALUE && tframe != null) {
			// set system inspector position
			getSystemInspector();
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = Math.max(tframe.getLocation().x + systemInspectorX, 0);
			x = Math.min(x, dim.width - systemInspector.getWidth());
			int y = Math.max(tframe.getLocation().y + systemInspectorY, 0);
			y = Math.min(y, dim.height - systemInspector.getHeight());
			systemInspector.setLocation(x, y);
			systemInspectorX = Integer.MIN_VALUE;
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					systemInspector.setVisible(true);
				}
			};
			SwingUtilities.invokeLater(runner);
		}
		if (particles.length == 0) {
			return;
		}
		if (tp.getFrameNumber() > getLastValidFrame()) {
			refreshSteps("DyamSys draw");
		}
		for (ParticleModel next : getModels()) {
			next.drawMe(panel, _g);
		}
	}

	@Override
	public void initialize(TrackerPanel trackerPanel) {
		if (initialized)
			return;
		ArrayList<DynamicParticle> toAdd = new ArrayList<DynamicParticle>();
		ArrayList<DynamicParticle> parts = trackerPanel.getDrawablesTemp(DynamicParticle.class);
		for (int i = 0; i < particleNames.length; i++) {
			for (DynamicParticle p : parts) {
				if (p.getName().equals(particleNames[i])) {
					toAdd.add(p);
					particleNames[i] = null;
				}
			}
		}
		parts.clear();
		setParticles(toAdd.toArray(new DynamicParticle[0]));
		boolean empty = true;
		for (String name : particleNames) {
			empty &= (name == null);
		}
		if (empty) {
			initialized = true;
			particleNames = new String[0];
		}
	}

	/**
	 * Gets a display name for this model.
	 *
	 * @return the display name
	 */
	@Override
	public String getDisplayName() {
		StringBuffer buf = new StringBuffer(getName());
		buf.append(" ("); //$NON-NLS-1$
		if (particles == null || particles.length == 0) {
			buf.append(TrackerRes.getString("DynamicSystem.Empty")); //$NON-NLS-1$
		} else {
			for (int i = 0; i < particles.length; i++) {
				if (i > 0)
					buf.append(" + "); //$NON-NLS-1$
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
	@Override
	public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu0) {
		// create a system inspector item
		systemInspectorItem = new JMenuItem(TrackerRes.getString("DynamicSystem.MenuItem.Inspector")); //$NON-NLS-1$
		systemInspectorItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DynamicSystemInspector inspector = getSystemInspector();
				inspector.updateDisplay();
				inspector.setVisible(true);
			}
		});
		// assemble the menu
		JMenu menu = super.getMenu(trackerPanel, menu0);
		menu.add(systemInspectorItem, 1);
		return menu;
	}

	/**
	 * Overrides TTrack getToolbarTrackComponents method.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a list of components
	 */
	@Override
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
	@Override
	public double getMass() {
		// set mass to sum of particle masses
		mass = 0;
		if (particles == null)
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
	@Override
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
		if (particles.length == 2)
			return false; // can't exceed two particles
		for (DynamicParticle next : particles) {
			if (next == particle)
				return false; // already contains particle
		}
		// make a new particles array
		DynamicParticle[] newParticles = new DynamicParticle[particles.length + 1];
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
		if (particles.length == 1 && particles[0] == particle) {
			return setParticles(new DynamicParticle[0]);
		}
		if (particles.length == 2) {
			if (particles[0] == particle)
				return setParticles(new DynamicParticle[] { particles[1] });
			if (particles[1] == particle)
				return setParticles(new DynamicParticle[] { particles[0] });
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
		if (newParticles == null || newParticles.length > 2) {
			return false;
		}
		for (DynamicParticle next : newParticles) {
			if (next == null) {
				return false;
			}
		}
		if (newParticles.length == 2) {
			DynamicParticle problem = null;
			if (newParticles[0].isBoostedBy(newParticles[1])) {
				problem = newParticles[0];
			} else if (newParticles[1].isBoostedBy(newParticles[0])) {
				problem = newParticles[1];
			}
			if (problem != null) {
				String message = TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Message1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
						+ TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Message2") + " " //$NON-NLS-1$ //$NON-NLS-2$
						+ problem.getName() + "\n" //$NON-NLS-1$
						+ TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Message3"); //$NON-NLS-1$
				int response = javax.swing.JOptionPane.showConfirmDialog(tframe, message,
						TrackerRes.getString("DynamicSystem.Dialog.RemoveBooster.Title"), //$NON-NLS-1$
						javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE);
				if (response == javax.swing.JOptionPane.YES_OPTION) {
					problem.setBooster(null);
				} else
					return false;
			}
		}

		// clean up particles that will be removed
		for (DynamicParticle particle : particles) {
			boolean cleanMe = true;
			for (DynamicParticle next : newParticles) {
				if (next == particle) {
					cleanMe = false;
				}
			}
			if (cleanMe) {
				particle.system = null;
				particle.inSystem = false;
				particle.refreshInitialTime();
				particle.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, this); //$NON-NLS-1$
				particle.removeStepListener(this);
				particle.setLastValidFrame(-1);
				particle.repaint();
				if (systemInspector != null) {
					particle.removeListenerNCF(systemInspector);
				}
			}
		}
		particles = new DynamicParticle[newParticles.length];
		System.arraycopy(newParticles, 0, particles, 0, newParticles.length);
		state = new double[particles.length * 4 + 1];
		initialState = new double[particles.length * 4 + 1];
		models = new ParticleModel[0];
		// update inspector, if visible
		if (systemInspector != null && systemInspector.isVisible()) {
			systemInspector.updateDisplay();
		}
		// make new points arrary
		int n = myPoint = particles.length;
		points = new Point2D.Double[n + 1];
		points[n] = new Point2D.Double();
		for (int i = 0; i < n; i++) {
			points[i] = new Point2D.Double();
			particles[i].removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, this); //$NON-NLS-1$
			particles[i].removeStepListener(this);
			particles[i].addPropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, this); //$NON-NLS-1$
			particles[i].addStepListener(this);
			particles[i].system = this;
			particles[i].refreshInitialTime();
			if (systemInspector != null) {
				particles[i].removeListenerNCF(systemInspector);
				particles[i].addListenerNCF(systemInspector);
			}
		}
		refreshSystemParameters();
		if (modelBuilder != null)
			modelBuilder.refreshDropdown(null);
		if (n == 0 && steps != noSteps) {
			steps = noSteps;
			fireStepsChanged();
		} else if (n > 0 && steps != realSteps) {
			steps = realSteps;
			fireStepsChanged();
		}
		setLastValidFrame(-1);
		repaint();
		return true;
	}

	/**
	 * Deletes this system. Overrides DynamicParticle method to clean up particles
	 * after deleting.
	 * 
	 */
	@Override
	public void delete() {
		setParticles(new DynamicParticle[0]);
		super.delete();
	}

	private double[] temp = new double[2];

	/**
	 * Gets the rate based on a specified state.
	 * 
	 * @param state the state
	 * @param rate  the rate of change of the state
	 */
	@Override
	public void getRate(double[] state, double[] rate) {
		rate[rate.length - 1] = 1; // dt/dt=1
		if (particles.length == 0) {
			return;
		}
		// one particle, no interactions: state is {x1, vx1, y1, vy1, t},
		// rate is {vx1, ax1, vy1, ay1, 1}
		if (particles.length == 1) {
			double[] particleState = getState(particles[0]);
			particles[0].getXYForces(particleState, temp);
			double m = particles[0].getMass();
			rate[0] = state[1]; // dx/dt = vx
			rate[1] = temp[0] / m; // dvx/dt = ax = fx/m
			rate[2] = state[3]; // dy/dt = vy
			rate[3] = temp[1] / m; // dvy/dt = ay = fy/m
			return;
		}
		// two particles, one interaction: state is {x1, vx1, y1, vy1, x2, vx2, y2, vy2,
		// t},
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
			particles[i].getXYForces(particleState, temp);
			double m = particles[i].getMass();
			int sign = i == 0 ? 1 : -1; // polar forces are opposite on particles[1]
			rate[4 * i] = state[4 * i + 1]; // dx/dt = vx
			rate[4 * i + 1] = (temp[0] + sign * fr * cos - sign * ftheta * sin) / m; // dvx/dt = ax = fx/m
			rate[4 * i + 2] = state[4 * i + 3]; // dy/dt = vy
			rate[4 * i + 3] = (temp[1] + sign * fr * sin + sign * ftheta * cos) / m; // dvy/dt = ay = fy/m
		}
	}

	/**
	 * Gets the initial values.
	 * 
	 * @return initial values {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
	 */
	@Override
	public double[] getInitialValues() {
		double[] state = null;
		// check initial state in case paint manager calls this unexpectedly
		if (initialState.length != particles.length * 4 + 1)
			initialState = new double[particles.length * 4 + 1];
		for (int i = 0; i < particles.length; i++) {
			state = particles[i].getInitialState();
			// state is {x, vx, y, vy, t}
			System.arraycopy(state, 0, initialState, 4 * i, 4);
		}
		if (state != null)
			initialState[initialState.length - 1] = state[state.length - 1];
		else if (tp != null) {
			double t0 = tp.getPlayer().getVideoClip().getStartTime();
			initialState[initialState.length - 1] = t0 / 1000;
		}
		return initialState;
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
			// workaround to prevent infinite loop
			ImageCoordSystem coords = tp.getCoords();
			if (coords instanceof ReferenceFrame) {
				TTrack track = ((ReferenceFrame) coords).getOriginTrack();
				if (track == this || (particles.length > 0 && track == particles[0])
						|| (particles.length > 1 && track == particles[1])) {
					return;
				}
			}
			setLastValidFrame(-1);
			refreshSteps("DynSys.property change transform ");
			break;
		case PROPERTY_TTRACK_MASS:
		case FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION: 
			super.propertyChange(e);
			refreshSystemParameters();
			if (tp != null)
				TFrame.repaintT(tp);
			break;
		case PROPERTY_TTRACK_NAME:
			super.propertyChange(e);
			refreshSystemParameters();
			break;
		default:
			super.propertyChange(e);
			break;
		}
	}

	@Override
	public void setFontLevel(int level) {
		super.setFontLevel(level);
		if (systemInspector != null) {
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
			addListenerNCF(systemInspector);
		}
		return systemInspector;
	}

	/**
	 * Gets the initial state. Overrides DynamicParticle method.
	 * 
	 * @return the initial state
	 */
	@Override
	public double[] getInitialState() {
		return getInitialValues();
	}

	/**
	 * Refreshes the data. Overrides PointMass method.
	 *
	 * @param data         the DatasetManager
	 * @param panel the tracker panel
	 */
	@Override
	protected void refreshData(DatasetManager data, TrackerPanel panel) {
		if (refreshDataLater || panel == null || data == null)
			return;
		int count = 25; // number of datasets	
		Integer panelID = panel.getID();
		// get the rotational data
		Object[] rotationData = getRotationData();
		double[] theta_data = (double[]) rotationData[0];
		double[] omega_data = (double[]) rotationData[1];
		double[] alpha_data = (double[]) rotationData[2];
		// clear datasets
		// get data at each non-null position step in the videoclip
		VideoPlayer player = panel.getPlayer();
		VideoClip clip = player.getVideoClip();
		double dt = player.getMeanStepDuration() / 1000.0;
		ImageCoordSystem coords = panel.getCoords();
		Step[] stepArray = getSteps();
		int pt = 0;
		int len = stepArray.length;
		double[][] validData = new double[count + 1][len];
		dataFrames.clear();
		for (int i = 0; i < len; i++) {
			if (stepArray[i] == null || !clip.includesFrame(i))
				continue;
			int stepNumber = clip.frameToStep(i);
			double t = player.getStepTime(stepNumber) / 1000.0;
			// assemble the data values for this step
			TPoint p = ((PositionStep) stepArray[i]).getPosition();
			Point2D wp = p.getWorldPosition(panel);
			validData[0][pt] = wp.getX(); // x
			validData[1][pt] = wp.getY(); // y
			validData[2][pt] = wp.distance(0, 0); // mag
			validData[3][pt] = Math.atan2(wp.getY(), wp.getX()); // ang between +/-pi
			validData[12][pt] = theta_data[i]; // theta
			validData[13][pt] = omega_data[i] / dt; // omega
			validData[14][pt] = alpha_data[i] / (dt * dt); // alpha
			validData[15][pt] = stepNumber; // step
			validData[16][pt] = i; // frame
			VectorStep veloc = getVelocity(i, panelID);
			if (veloc == null) {
				validData[4][pt] = Double.NaN; // vx
				validData[5][pt] = Double.NaN; // vy
				validData[6][pt] = Double.NaN; // vmag
				validData[7][pt] = Double.NaN; // vang
				validData[17][pt] = Double.NaN; // px
				validData[18][pt] = Double.NaN; // py
				validData[19][pt] = Double.NaN; // pmag
				validData[20][pt] = Double.NaN; // pang
			} else {
				double imageX = veloc.getXComponent();
				double imageY = veloc.getYComponent();
				double x = validData[4][pt] = coords.imageToWorldXComponent(i, imageX, imageY) / dt;
				double y = validData[5][pt] = coords.imageToWorldYComponent(i, imageX, imageY) / dt;
				double r = validData[6][pt] = Math.sqrt(x * x + y * y);
				double slope = validData[7][pt] = Math.atan2(y, x);
				double mass = getMass();
				validData[17][pt] = mass * x;
				validData[18][pt] = mass * y;
				validData[19][pt] = mass * r;
				validData[20][pt] = mass * slope;
			}
			VectorStep accel = getAcceleration(i, panelID);
			if (accel == null) {
				validData[8][pt] = Double.NaN; // ax
				validData[9][pt] = Double.NaN; // ay
				validData[10][pt] = Double.NaN; // amag
				validData[11][pt] = Double.NaN; // aang
			} else {
				double imageX = accel.getXComponent();
				double imageY = accel.getYComponent();
				double x = validData[8][pt] = coords.imageToWorldXComponent(i, imageX, imageY) / (dt * dt);
				double y = validData[9][pt] = coords.imageToWorldYComponent(i, imageX, imageY) / (dt * dt);
				validData[10][pt] = Math.sqrt(x * x + y * y);
				validData[11][pt] = Math.atan2(y, x);
			}
			double[] relState;
			if (particles.length == 2 && (relState = frameRelativeStates.get(i)) != null) {
				validData[21][pt] = relState[0]; // r_rel
				validData[22][pt] = relState[2]; // theta_rel
				validData[23][pt] = relState[1]; // vr_rel
				validData[24][pt] = relState[3]; // omega_rel
			} else {
				validData[21][pt] = Double.NaN; // r_rel
				validData[22][pt] = Double.NaN; // theta_rel
				validData[23][pt] = Double.NaN; // vr_rel
				validData[24][pt] = Double.NaN; // omega_rel
			}
			validData[count][pt] = t;
			dataFrames.add(i);
			pt++;
		}
		clearColumns(data, count, dataVariables, "PointMass.Data.Description.", validData, pt);
		for (int i0 = count - 3, i = i0; i < count; i++) {
			dataDescriptions[i] = TrackerRes.getString("DynamicSystem.Data.Description." + (i - i0)); //$NON-NLS-1$
		}
		// store the mass in the data properties
		Double m = getMass();
		String desc = TrackerRes.getString("ParticleModel.Parameter.Mass.Description"); //$NON-NLS-1$
		data.setConstant("m", m, m.toString(), desc); //$NON-NLS-1$
	}

//______________________________ protected methods __________________________

	@Override
	public void dispose() {
		for (int i = 0; i < particles.length; i++) {
			particles[i].removePropertyChangeListener(TTrack.PROPERTY_TTRACK_MASS, this); //$NON-NLS-1$
			particles[i].removeStepListener(this);
			particles[i].system = null;
		}
		super.dispose();
		if (systemInspector != null)
			systemInspector.dispose();
	}

	/**
	 * Refreshes initial time parameter for this model. Overrides ParticleModel.
	 */
	@Override
	protected void refreshInitialTime() {
		super.refreshInitialTime();
		for (ParticleModel next : particles) {
			next.refreshInitialTime();
		}
	}

	/**
	 * Refreshes the initial positions, velocities and particle masses based on the
	 * values for the particles in this system.
	 */
	protected void refreshSystemParameters() {
		if (refreshing)
			return;
		refreshing = true;
		double[] polarState; // polar state is {r, vr, theta, omega, t}
		if (particles.length == 2) {
			polarState = getRelativePolarState(getInitialState());
		} else {
			polarState = new double[] { 0, 0, 0, 0, 0 };
		}
		double zeroPosition = 1E-12;
		if (tp != null) {
			zeroPosition = .001 / tp.getCoords().getScaleX(0);
		}
		double zeroVelocity = 1E-11;
		if (tp != null) {
			zeroVelocity = 1000 * zeroPosition / tp.getPlayer().getMeanStepDuration();
		}
		double zeroAngle = 1E-5;
		double zeroOmega = 1E-4;
		if (tp != null) {
			zeroOmega = 1000 * zeroAngle / tp.getPlayer().getMeanStepDuration();
		}
		String relative = "_" + TrackerRes.getString("DynamicSystem.Parameter.Name.Relative"); //$NON-NLS-1$ //$NON-NLS-2$
		String particleNames = " "; //$NON-NLS-1$
		if (particles.length > 0) {
			particleNames += TrackerRes.getString("DynamicSystem.Parameter.Of") + " "; //$NON-NLS-1$ //$NON-NLS-2$
			particleNames += particles[0].getName() + " "; //$NON-NLS-1$
			particleNames += TrackerRes.getString("DynamicSystem.Parameter.RelativeTo") + " "; //$NON-NLS-1$ //$NON-NLS-2$
			particleNames += particles.length > 1 ? particles[1].getName() : particles[0].getName();
		}
		// particle masses
		String desc = TrackerRes.getString("DynamicSystem.Parameter.Mass.Description"); //$NON-NLS-1$
		getParamEditor().setExpression("m", String.valueOf(getMass()), false); //$NON-NLS-1$
		getParamEditor().setDescription("m", desc); //$NON-NLS-1$
		Parameter m1 = (Parameter) getParamEditor().getObject("m1"); //$NON-NLS-1$
		Parameter m2 = (Parameter) getParamEditor().getObject("m2"); //$NON-NLS-1$
		desc = TrackerRes.getString("DynamicSystem.Parameter.ParticleMass.Description"); //$NON-NLS-1$
		if (particles.length == 0) {
			if (m1 != null) {
				// must set name and expression editable before removing parameter
				m1.setNameEditable(true);
				m1.setExpressionEditable(true);
				getParamEditor().removeObject(m1, false);
			}
			if (m2 != null) {
				// must set name and expression editable before removing parameter
				m2.setNameEditable(true);
				m2.setExpressionEditable(true);
				getParamEditor().removeObject(m2, false);
			}
		} else {
			String value = FunctionEditor.format(particles[0].getMass(), 0);
			if (m1 == null) {
				m1 = createParameter("m1", value, desc + " " + particles[0].getName()); //$NON-NLS-1$ //$NON-NLS-2$
				getParamEditor().addObject(m1, 1, false, false);
			} else
				getParamEditor().setExpression("m1", value, false); //$NON-NLS-1$
			if (particles.length > 1) {
				value = FunctionEditor.format(particles[1].getMass(), 0);
				if (m2 == null) {
					m2 = createParameter("m2", value, desc + " " + particles[1].getName()); //$NON-NLS-1$ //$NON-NLS-2$
					getParamEditor().addObject(m2, 2, false, false);
				} else
					getParamEditor().setExpression("m2", value, false); //$NON-NLS-1$
			} else {
				if (m2 != null) {
					// must set name and expression editable before removing parameter
					m2.setNameEditable(true);
					m2.setExpressionEditable(true);
					getParamEditor().removeObject(m2, false);
				}
			}
		}
		for (DynamicParticle particle : particles) {
			if (particle.modelBooster != null) {
				particle.modelBooster.setBooster(particle.modelBooster.booster);
			}
		}
		// initial values
		Parameter t = (Parameter) getInitEditor().getObject("t"); //$NON-NLS-1$
		String value = FunctionEditor.format(polarState[0], zeroPosition);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialR.Description"); //$NON-NLS-1$
		Parameter r = createParameter("r" + relative, value, desc + particleNames); //$NON-NLS-1$
		value = FunctionEditor.format(polarState[2], zeroAngle);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialTheta.Description"); //$NON-NLS-1$
		Parameter theta = createParameter(FunctionEditor.THETA + relative, value, desc + particleNames);
		value = FunctionEditor.format(polarState[1], zeroVelocity);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialVelocityR.Description"); //$NON-NLS-1$
		Parameter vr = createParameter("vr" + relative, value, desc + particleNames); //$NON-NLS-1$
		value = FunctionEditor.format(polarState[3], zeroOmega);
		desc = TrackerRes.getString("DynamicParticle.Parameter.InitialOmega.Description"); //$NON-NLS-1$
		Parameter omega = createParameter(FunctionEditor.OMEGA + relative, value, desc + particleNames);
		getInitEditor().setParameters(new Parameter[] { t, r, theta, vr, omega });
		refreshing = false;
	}

	/**
	 * Sets the positions of the trace points based on a specified state.
	 * 
	 * @param state the state
	 */
	@Override
	protected void setTracePositions(double[] state) {
		// state is {t} if no particles
		// or {x1, vx1, y1, vy1, t} if one particle
		// or {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t} if two particles

		int n = particles.length;
		if (n == 0) {
			return;
		}
		double mass = 0, xcm = 0, ycm = 0;
		for (int i = 0; i < n; i++) {
			points[i].setLocation(state[4 * i], state[4 * i + 2]); // current ODE state
			double m = particles[i].getMass();
			mass += m;
			xcm += m * state[4 * i];
			ycm += m * state[4 * i + 2];
		}
		points[n].setLocation(xcm / mass, ycm / mass);
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
		UserFunction[] uf = new UserFunction[2];
		String[] funcVars = new String[] { "r", "vr", //$NON-NLS-1$ //$NON-NLS-2$
				FunctionEditor.THETA, FunctionEditor.OMEGA, "t" }; //$NON-NLS-1$
		String internal = TrackerRes.getString("DynamicSystem.Force.Name.Internal"); //$NON-NLS-1$
		uf[0] = new UserFunction("fr_" + internal, //$NON-NLS-1$
			funcVars, TrackerRes.getString("DynamicSystem.ForceFunction.R.Description")); //$NON-NLS-1$
		uf[1] = new UserFunction("f" + FunctionEditor.THETA + "_" + internal, //$NON-NLS-1$ //$NON-NLS-2$
			funcVars, TrackerRes.getString("DynamicSystem.ForceFunction.Theta.Description")); //$NON-NLS-1$
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
		particleState[4] = state[state.length - 1]; // time
		if (particles.length > 0) {
			// state is {x1, vx1, y1, vy1, x2, vx2, y2, vy2, t}
			// systemState is {x_cm, vx_cm, y_cm, vy_cm, t}
			for (int i = 0; i < particles.length; i++) {
				double m = particles[i].getMass();
				mass += m;
				particleState[0] += m * state[4 * i];
				particleState[1] += m * state[4 * i + 1];
				particleState[2] += m * state[4 * i + 2];
				particleState[3] += m * state[4 * i + 3];
			}
			particleState[0] /= mass; // cm x coordinate
			particleState[1] /= mass; // cm y coordinate
			particleState[2] /= mass; // cm x velocity
			particleState[3] /= mass; // cm y velocity
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
				particleState[0] = state[4 * i];
				particleState[1] = state[4 * i + 1];
				particleState[2] = state[4 * i + 2];
				particleState[3] = state[4 * i + 3];
				particleState[4] = state[state.length - 1];
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
	@Override
	protected ParticleModel[] getModels() {
		if (models.length != particles.length + 1) {
			models = new ParticleModel[particles.length + 1];
			for (int i = 0; i < models.length - 1; i++) {
				models[i] = particles[i];
			}
			models[models.length - 1] = this;
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
		double dx = state[0] - state[4];
		double dy = state[2] - state[6];
		double vx = state[1] - state[5];
		double vy = state[3] - state[7];
		double r = Math.sqrt(dx * dx + dy * dy);
		double v = Math.sqrt(vx * vx + vy * vy);
		double rang = Math.atan2(dy, dx);
		double vang = Math.atan2(vy, vx);
		double dang = vang - rang;
		// polar state is {r, vr, theta, omega, t}
		polarState[0] = r; // r
		polarState[1] = r == 0 ? v : v * Math.cos(dang); // vr
		polarState[2] = r == 0 ? vang : rang; // theta
		polarState[3] = r == 0 ? 0 : v * Math.sin(dang) / r; // omega
		polarState[4] = state[8]; // t
		double[] toSave = new double[polarState.length];
		System.arraycopy(polarState, 0, toSave, 0, polarState.length);
		int frameNum = tp.getFrameNumber();
		frameRelativeStates.put(frameNum, toSave);
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
		 * @param obj     the object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			DynamicSystem system = (DynamicSystem) obj;
			// save particle names, if any
			if (system.particles.length > 0) {
				String[] names = new String[system.particles.length];
				for (int i = 0; i < names.length; i++) {
					names[i] = system.particles[i].getName();
				}
				control.setValue("particles", names); //$NON-NLS-1$
			}
			// save system inspector location if visible
			if (system.systemInspector != null && system.systemInspector.isVisible()) {
				Point p = system.systemInspector.getLocation();
				// save location relative to frame
				TFrame frame = system.tframe;
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
		@Override
		public Object createObject(XMLControl control) {
			return new DynamicSystem();
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
			XML.getLoader(ParticleModel.class).loadObject(control, obj);
			DynamicSystem system = (DynamicSystem) obj;
			// load mass names
			String[] names = (String[]) control.getObject("particles"); //$NON-NLS-1$
			if (names != null) {
				system.particleNames = names;
				system.initialized = false;
			}
			system.systemInspectorX = control.getInt("system_inspector_x"); //$NON-NLS-1$
			system.systemInspectorY = control.getInt("system_inspector_y"); //$NON-NLS-1$
			return obj;
		}
	}

}
