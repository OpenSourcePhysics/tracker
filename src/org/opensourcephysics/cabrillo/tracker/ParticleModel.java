/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
 * 
 * Tracker is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Tracker is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Tracker; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston MA 02111-1307 USA or view the license online at
 * <http://www.gnu.org/copyleft/gpl.html>
 * 
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Trackable;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FunctionEditor;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.InitialValueEditor;
import org.opensourcephysics.tools.ParamEditor;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.UserFunction;
import org.opensourcephysics.tools.UserFunctionEditor;

import javajs.async.SwingJSUtils.Performance;

/**
 * A ParticleModel is a point mass whose positions are determined by a model.
 * 
 * @author Douglas Brown
 */
abstract public class ParticleModel extends PointMass {

	// static fields
	protected static int tracePtsPerStep = 10;
	protected static boolean loading = false;
	protected static Point2D.Double nan = new Point2D.Double(Double.NaN, Double.NaN);
	protected static double xLimit = 8000, yLimit = 6000; // too far off screen
	protected static NumberFormat timeFormat = NumberFormat.getNumberInstance();

	static {
		timeFormat.setMinimumIntegerDigits(1);
		timeFormat.setMaximumFractionDigits(3);
		timeFormat.setMinimumFractionDigits(3);
		timeFormat.setGroupingUsed(false);
	}

	// instance fields
	protected FunctionTool modelBuilder;
	protected ModelFunctionPanel functionPanel;
	protected UserFunctionEditor functionEditor;
	protected int inspectorX = Integer.MIN_VALUE, inspectorY, inspectorH = Integer.MIN_VALUE;
	protected boolean showModelBuilder;
	protected boolean refreshing = false;
	protected double[] traceX = {}, traceY = {};
	protected double[] prevX, prevY;
	protected TPoint tracePt = new TPoint();
	private int lastValidFrame = -1; // used in draw method
	protected double t0, dt = 0.1, time;
	protected boolean refreshDerivsLater, refreshStepsLater;
	protected boolean invalidWarningShown, startFrameUndefined;
	protected int startFrame, endFrame = Integer.MAX_VALUE;
	protected boolean useDefaultReferenceFrame;

	protected JMenuItem modelBuilderItem, useDefaultRefFrameItem, stampItem;

	protected PropertyChangeListener massParamListener, timeParamListener;

	protected void setLastValidFrame(int i) {
		lastValidFrame = i;
	}

	protected int getLastValidFrame() {
		return lastValidFrame;
	}

	/**
	 * Constructs a ParticleModel.
	 */
	public ParticleModel() {
		drawsTrace = true;
		Footprint[] footprints = super.getFootprints();
		Footprint[] newprints = new Footprint[footprints.length + 1];
		newprints[0] = CircleFootprint.getFootprint("CircleFootprint.FilledCircle"); //$NON-NLS-1$
		for (int i = 0; i < footprints.length; i++) {
			newprints[i + 1] = footprints[i];
		}
		setFootprints(newprints);
		defaultFootprint = newprints[0];
		setFootprint(defaultFootprint.getName());
		// assign a meaningful initial name
		setName(TrackerRes.getString("ParticleModel.New.Name")); //$NON-NLS-1$
		//long t0 = Performance.now(0);
		initializeFunctionPanel();
		// OSPLog.debug("!!! " + Performance.now(t0) + "
		// ParticleModel.initializeFunctionPanel");
		// set initial hint
		hint = TrackerRes.getString("ParticleModel.Hint"); //$NON-NLS-1$
	}

	/**
	 * Overrides PointMass draw method.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	@Override
	public void draw(DrawingPanel panel, Graphics _g) {
		if (!(panel instanceof TrackerPanel) || trackerPanel == null)
			return;
		// OSPLog.debug("ParticleModel.draw frame " + trackerPanel.getFrameNumber() +
		// "/" + lastValidFrame + " " + isVisible() );
		//long t0 = Performance.now(0);

		if (isVisible() && trackerPanel.getFrameNumber() > lastValidFrame) {
				refreshSteps("draw");
		}
		// OSPLog.debug("!!! " + Performance.now(t0) + "
		// ParticleModel.paintComponent-draw-refreshsteps");
		t0 = Performance.now(0);
		drawMe(panel, _g);
		// OSPLog.debug("!!! " + Performance.now(t0) + "
		// ParticleModel.paintComponent-drawme");
	}

	/**
	 * Draws this without refreshing steps.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g    the graphics context on which to draw
	 */
	public void drawMe(DrawingPanel panel, Graphics _g) {
		// position and show model builder if requested during loading
		if (inspectorX != Integer.MIN_VALUE && trackerPanel != null && trackerPanel.getTFrame() != null) {
			if (showModelBuilder) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						positionModelBuilder();
						showModelBuilder = false;
						modelBuilder.setVisible(true);
					}
				});
			}
		}
		if (isVisible() && isTraceVisible()) {
			// draw trace only if fixed coords & (non-worldview or no ref frame)
			TrackerPanel tPanel = (TrackerPanel) panel;
			ImageCoordSystem coords = tPanel.getCoords(); // get active coords
			boolean isRefFrame = coords instanceof ReferenceFrame;
			if (isRefFrame) {
				coords = ((ReferenceFrame) coords).getCoords();
			}
			boolean fixed = coords.isFixedAngle() && coords.isFixedOrigin() && coords.isFixedScale();
			if (fixed && (!(tPanel instanceof WorldTView) || !isRefFrame)) {
				trace.reset();
				for (int i = 0; i < traceX.length; i++) {
					if (Double.isNaN(traceX[i]))
						continue;
					tracePt.setLocation(traceX[i], traceY[i]);
					java.awt.Point p = tracePt.getScreenPosition(tPanel);
					if (trace.getCurrentPoint() == null)
						trace.moveTo((float) p.getX(), (float) p.getY());
					else
						trace.lineTo((float) p.getX(), (float) p.getY());
				}
				Graphics2D g2 = (Graphics2D) _g;
				Color color = g2.getColor();
				Stroke stroke = g2.getStroke();
				g2.setColor(getFootprint().getColor());
				g2.setStroke(traceStroke);
				if (OSPRuntime.setRenderingHints)
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
				g2.draw(trace);
				// restore original color and stroke
				g2.setColor(color);
				g2.setStroke(stroke);
			}
		}
		super.draw(panel, _g);
	}

	/**
	 * Removes this particle from all panels that draw it. Overrides TTrack method.
	 */
	@Override
	public void delete() {
		FunctionTool modelBuilder = null;
		if (trackerPanel != null && trackerPanel.modelBuilder != null) {
			ArrayList<ParticleModel> list = trackerPanel.getDrawablesTemp(ParticleModel.class);
			if (list.size() == 1)
				modelBuilder = trackerPanel.modelBuilder;
			list.clear();
		}
		super.delete();
		if (modelBuilder != null) {
			modelBuilder.setVisible(false);
		}
	}

	/**
	 * Responds to property change events.
	 * 
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		if (trackerPanel == null)
			return;
		boolean dorefresh = (!refreshing && isModelsVisible());
		String resetMe = null;
		String name = e.getPropertyName();
		switch (name) {
//		System.out.println(name);
		case FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION:
			if (!loading)
				trackerPanel.changed = true;
			resetMe = "repaint";
			break;
		case TFrame.PROPERTY_TFRAME_TAB: // $NON-NLS-1$
			if (modelBuilder != null) {
				if (trackerPanel != null && e.getNewValue() == trackerPanel && trackerPanel.isModelBuilderVisible) {
					modelBuilder.setVisible(true);
				} else if (modelBuilder.isVisible() && e.getNewValue() != null) {
					modelBuilder.setVisible(false);
					trackerPanel.isModelBuilderVisible = true;
				}
			}
			if (this instanceof ParticleDataTrack
//					&& ((ParticleDataTrack)this).isAutoPasteEnabled()
					&& trackerPanel != null && trackerPanel.getTFrame() != null
//					&& trackerPanel.getTFrame().clipboardListener!=null
					&& trackerPanel == e.getNewValue()) {
				trackerPanel.getTFrame().getClipboardListener().processContents(trackerPanel);
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK:
			if (e.getNewValue() == this && modelBuilder != null && !modelBuilder.getSelectedName().equals(getName())) {
				modelBuilder.setSelectedPanel(getName());
			}
			break;
		case VideoClip.PROPERTY_VIDEOCLIP_STARTTIME:
		case ClipControl.PROPERTY_CLIPCONTROL_FRAMEDURATION:
		case DataTrack.PROPERTY_DATATRACK_STARTFRAME:
			resetMe = "time";
			break;
		case VideoClip.PROPERTY_VIDEOCLIP_STEPSIZE:
			resetMe = "refresh";
			break;
		case VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT:
			// no reset to -1
			if (dorefresh) {
				refreshInitialTime();
				refreshSteps(name);
			}
			break;
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM: // $NON-NLS-1$
			// workaround to prevent infinite loop
			ImageCoordSystem coords = trackerPanel.getCoords();
			if (!(coords instanceof ReferenceFrame && ((ReferenceFrame) coords).getOriginTrack() == this)) {
				resetMe = "refresh";
			}
			break;
		case Trackable.PROPERTY_ADJUSTING: // $NON-NLS-1$
			if (dorefresh) {
				refreshStepsLater = (Boolean) e.getNewValue();
				if (!refreshStepsLater) { // stopped adjusting, so refresh steps
					refreshSteps(name);
				}
			}
			break;
		}
		if (resetMe != null) {
			setLastValidFrame(-1);
			if (dorefresh) {
				switch (resetMe) {
				case "repaint":
					repaint();
					break;
				case "refresh":
					refreshSteps(name);
					break;
				case "time":
					refreshInitialTime();
					refreshSteps(name);
					break;
				}
			}
		}
	}

	/**
	 * Gets the mass. Overrides PointMass method.
	 * 
	 * @return the mass
	 */
	@Override
	public double getMass() {
		Parameter massParam = (Parameter) getParamEditor().getObject("m"); //$NON-NLS-1$
		if (massParam != null)
			return massParam.getValue();
		return super.getMass();
	}

	/**
	 * Sets the mass. Overrides PointMass method.
	 * 
	 * @param mass the mass
	 */
	@Override
	public void setMass(double mass) {
		super.setMass(mass);
		mass = super.getMass();
		massField.setValue(mass);
		// refresh mass parameter in paramPanel if changed
		Parameter massParam = (Parameter) getParamEditor().getObject("m"); //$NON-NLS-1$
		if (massParam != null && massParam.getValue() != mass) {
			functionPanel.getParamEditor().setExpression("m", String.valueOf(mass), false); //$NON-NLS-1$
			refreshSteps("setMass");
		}
	}

	/**
	 * Sets the name. Overrides TTrack method.
	 * 
	 * @param name the name
	 */
	@Override
	public void setName(String name) {
		String prevName = getName();
		super.setName(name);
		if (modelBuilder != null) {
			modelBuilder.renamePanel(prevName, name);
		}
	}

	/**
	 * Gets a display name for this model. The default is the model name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return getName("model"); //$NON-NLS-1$
	}

	/**
	 * Determines if any point in this track is autotrackable.
	 *
	 * @return true if autotrackable
	 */
	@Override
	protected boolean isAutoTrackable() {
		return false;
	}

	/**
	 * Overrides PointMass findInteractive method.
	 *
	 * @param panel the drawing panel
	 * @param xpix  the x pixel position on the panel
	 * @param ypix  the y pixel position on the panel
	 * @return the first step or motion vector that is hit
	 */
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		Interactive ia = super.findInteractive(panel, xpix, ypix);
		if (ia instanceof PositionStep.Position) {
			hint = TrackerRes.getString("PointMass.Position.Locked.Hint"); //$NON-NLS-1$
		} else if (ia == null)
			hint = TrackerRes.getString("ParticleModel.Hint"); //$NON-NLS-1$
		return ia;
	}

	/**
	 * Overrides TTrack setLocked method. ParticleModel is always locked.
	 * 
	 * @param locked ignored
	 */
	@Override
	public void setLocked(boolean locked) {/** empty block */
	}

	/**
	 * Overrides TTrack method to report that this is a dependent track.
	 * 
	 * @return <code>true</code> if this track is dependent
	 */
	@Override
	public boolean isDependent() {
		return true;
	}

	/**
	 * Overrides TTrack isStepComplete method. Always returns true.
	 * 
	 * @param n the frame number
	 * @return <code>true</code> always since gets data from model
	 */
	@Override
	public boolean isStepComplete(int n) {
		return true;
	}

	/**
	 * Returns a menu with items that control this track.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	@Override
	public JMenu getMenu(TrackerPanel trackerPanel, JMenu menu0) {
		if (modelBuilderItem == null) {
			// create the model item
			modelBuilderItem = new JMenuItem();
			modelBuilderItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					positionModelBuilder();
					getModelBuilder().setVisible(true);
				}
			});
			// create the useDefaultRefFrameItem item
			useDefaultRefFrameItem = new JCheckBoxMenuItem();
			useDefaultRefFrameItem.setSelected(!useDefaultReferenceFrame);
			useDefaultRefFrameItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setUseDefaultReferenceFrame(!useDefaultRefFrameItem.isSelected());
					if (ParticleModel.this.trackerPanel.getCoords() instanceof ReferenceFrame) {
						setLastValidFrame(-1);
						refreshSteps("useDefRefFrameItem action");
					}
				}
			});
			// create the stamp item
			stampItem = new JMenuItem();
			stampItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doStamp();
				}
			});
		}
		modelBuilderItem.setText(TrackerRes.getString("ParticleModel.MenuItem.InspectModel")); //$NON-NLS-1$
		useDefaultRefFrameItem.setText(TrackerRes.getString("ParticleModel.MenuItem.UseDefaultReferenceFrame")); //$NON-NLS-1$
		String stamp = TrackerRes.getString("ParticleModel.MenuItem.Stamp"); //$NON-NLS-1$
		String pm = TrackerRes.getString("PointMass.Name"); //$NON-NLS-1$
		stampItem.setText(stamp + " " + pm); //$NON-NLS-1$
		stampItem.setToolTipText(TrackerRes.getString("ParticleModel.MenuItem.Stamp.Tooltip")); //$NON-NLS-1$
		// assemble the menu
		JMenu menu = super.getMenu(trackerPanel, menu0);

		// remove unwanted menu items and separators
		menu.remove(autotrackItem);
		menu.remove(deleteStepItem);
		menu.remove(clearStepsItem);

		// find acceleration menu and insert stampItem after it
		if (trackerPanel.isEnabled("model.stamp")) { //$NON-NLS-1$
			for (int i = menu.getItemCount(); --i >= 0;) {
				if (menu.getMenuComponent(i) == accelerationMenu) {
					menu.insert(stampItem, ++i);
					menu.insertSeparator(i);
					break;
				}
			}
		}
		return assembleMenu(menu, modelBuilderItem);
	}

	protected void doStamp() {
		refreshSteps("stampItem action");
		PointMass pm = new PointMass();
		String proposed = getName() + " " + TrackerRes.getString("ParticleModel.Stamp.Name") + "1"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (TTrack track : trackerPanel.getTracksTemp()) {
			if (proposed.equals(track.getName())) {
				try {
					int n = Integer.parseInt(proposed.substring(proposed.length() - 1));
					proposed = proposed.substring(0, proposed.length() - 1) + (n + 1);
				} catch (NumberFormatException ex) {
					continue;
				}
			}
		}
		trackerPanel.clearTemp();
		pm.setName(proposed);
		pm.setColor(getColor().darker());
		trackerPanel.addTrack(pm);
		for (Step step : getSteps()) {
			if (step == null)
				continue;
			TPoint pt = step.getPoints()[0];
			int n = pt.getFrameNumber(trackerPanel);
			pm.createStep(n, pt.x, pt.y);
		}
		TFrame.repaintT(trackerPanel);
	}

	/**
	 * Overrides TTrack getToolbarPointComponents method.
	 * 
	 * @param trackerPanel the tracker panel
	 * @param point        the TPoint
	 * @return a list of components
	 */
	@Override
	public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel, TPoint point) {
		ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
		xField.setEnabled(false);
		yField.setEnabled(false);
		magField.setEnabled(false);
		angleField.setEnabled(false);
		return list;
	}

	/**
	 * Sets the start frame for this model. Also sets the initial time to the video
	 * clip time at the start frame.
	 * 
	 * @param n the desired start frame
	 */
	public void setStartFrame(int n) {
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		n = Math.max(n, clip.getFirstFrameNumber()); // not less than first frame
		int end = clip.getLastFrameNumber();
		n = Math.min(n, end); // not greater than last frame
		n = Math.min(n, getEndFrame()); // not greater than endFrame
		if (n == startFrame)
			return;
		startFrame = n;
		refreshInitialTime();
		setLastValidFrame(-1);
		refreshSteps("setStartFrame " + n);
		TFrame.repaintT(trackerPanel);
		firePropertyChange(PROPERTY_TTRACK_MODELSTART, null, getStartFrame()); // $NON-NLS-1$
	}

	/**
	 * Gets the start frame for this model.
	 * 
	 * @return the start frame
	 */
	public int getStartFrame() {
		return startFrame;
	}

	/**
	 * Sets the end frame for this model.
	 * 
	 * @param n the desired end frame
	 */
	public void setEndFrame(int n) {
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int end = clip.getLastFrameNumber();
		n = Math.max(n, 0); // not less than zero
		n = Math.max(n, getStartFrame()); // not less than startFrame
		if (n == getEndFrame())
			return;
		endFrame = n < end ? n : Integer.MAX_VALUE;
		if (n < lastValidFrame) {
			trimSteps();
		} else {
			refreshSteps("setEndFrame " + endFrame);
		}
		TFrame.repaintT(trackerPanel);
		firePropertyChange(PROPERTY_TTRACK_MODELEND, null, getEndFrame()); // $NON-NLS-1$
	}

	/**
	 * Gets the end frame for this model.
	 * 
	 * @return the end frame
	 */
	public int getEndFrame() {
		return endFrame;
	}

	/**
	 * Identifies the controlling TrackerPanel for this track (by default, the first
	 * TrackerPanel that adds this track to its drawables).
	 *
	 * @param panel the TrackerPanel
	 */
	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		super.setTrackerPanel(panel);
		if (panel != null) {
			if (startFrameUndefined) {
				int n = panel.getPlayer().getVideoClip().getStartFrameNumber();
				setStartFrame(n);
				startFrameUndefined = false;
			}
			if (panel.getTFrame() != null) {
				boolean radians = panel.getTFrame().anglesInRadians;
				functionPanel.initEditor.setAnglesInDegrees(!radians);
			}
		}
	}

	/**
	 * Sets the display format for angles.
	 *
	 * @param radians <code>true</code> for radians, false for degrees
	 */
	@Override
	protected void setAnglesInRadians(boolean radians) {
		super.setAnglesInRadians(radians);
		functionPanel.initEditor.setAnglesInDegrees(!radians);
		String s = TrackerRes.getString("DynamicParticle.Parameter.InitialTheta.Description") + " "; //$NON-NLS-1$ //$NON-NLS-2$
		s += radians ? TrackerRes.getString("TableTrackView.Radians.Tooltip") : //$NON-NLS-1$
				TrackerRes.getString("TableTrackView.Degrees.Tooltip"); //$NON-NLS-1$
		functionPanel.initEditor.setDescription(FunctionEditor.THETA, s);
		s = TrackerRes.getString("DynamicParticle.Parameter.InitialOmega.Description") + " "; //$NON-NLS-1$ //$NON-NLS-2$
		s += radians ? TrackerRes.getString("TableTrackView.RadiansPerSecond.Tooltip") : //$NON-NLS-1$
				TrackerRes.getString("TableTrackView.DegreesPerSecond.Tooltip"); //$NON-NLS-1$
		functionPanel.initEditor.setDescription(FunctionEditor.OMEGA, s);
	}

	@Override
	protected void dispose() {
		if (trackerPanel != null) {
			trackerPanel.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this); // $NON-NLS-1$
			if (trackerPanel.getTFrame() != null) {
				trackerPanel.getTFrame().removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			}
		}
		if (modelBuilder != null) {
			getParamEditor().removePropertyChangeListener(massParamListener);
			getInitEditor().removePropertyChangeListener(timeParamListener);
			functionPanel.dispose();

			modelBuilder.removePanel(getName());
			modelBuilder.removePropertyChangeListener(this);
			if (modelBuilder.isEmpty()) {
				modelBuilder.setVisible(false);
			}
			modelBuilder = null;
			functionPanel = null;
			functionEditor = null;
		}
		super.dispose();
	}

	/**
	 * Gets the next trace positions. Subclasses override to get positions based on
	 * model. Value is stored in the points field
	 * 
	 * @return TODO
	 */
	abstract boolean getNextTracePositions();

	/**
	 * Resets model parameters and sets position(s) for start frame. Most of the
	 * work in this method must be done by subclasses.
	 */
	protected void reset() {
//		invalidWarningShown = false;	
	}

	protected Point2D.Double[] points;
	protected int myPoint = 0;

	private ParticleModel[] me = new ParticleModel[] { this };

	/**
	 * Gets the particle models associated with this model. By default this model is
	 * returned.
	 * 
	 * @return array of particle models associated with this model
	 */
	protected ParticleModel[] getModels() {
		return me;
	}

	protected boolean isModelsVisible() {
		for (ParticleModel model : getModels()) {
			if (model.isTraceVisible() || (model.isVisible() && (model.isPositionVisible(trackerPanel)
					|| model.isVVisible(trackerPanel) || model.isAVisible(trackerPanel)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Refreshes initial time parameter for this model.
	 */
	protected void refreshInitialTime() {
		if (trackerPanel == null)
			return;
		double t0 = trackerPanel.getPlayer().getFrameTime(getStartFrame()) / 1000;
		String t = timeFormat.format(t0);
		Parameter param = (Parameter) getInitEditor().getObject("t"); //$NON-NLS-1$
		if (param.getValue() != t0) {
			boolean prev = refreshing;
			refreshing = true;
			getInitEditor().setExpression("t", t, false); //$NON-NLS-1$
			refreshing = prev;
		}
	}

	protected static int nCalc = 0;

	/**
	 * Refreshes step positions.
	 */
	protected void refreshSteps(String why) {
		locked = true;
		//OSPLog.debug(Performance.timeCheckStr("ParticleModel.refreshSteps00 " + why, Performance.TIME_MARK));

		// return if this is an empty dynamic system
		if (refreshStepsLater || trackerPanel == null
				|| this instanceof DynamicSystem && ((DynamicSystem) this).particles.length == 0)
			return;
		refreshDerivsLater = trackerPanel.getPlayer().getClipControl().isPlaying();
//		trackerPanel.getTFrame().holdPainting(true);
		int n = trackerPanel.getFrameNumber();
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		// determine last frame to be marked (must satisfy both model and clip)
		int end = Math.min(getEndFrame(), n);
		int start = getStartFrame();
		while (end > start && !clip.includesFrame(end)) {
			end--;
		}
		if (end <= lastValidFrame)
			return;
		if (lastValidFrame == -1) {
			reset(); // initializes model, sets lastValidFrame to marked frame, if any
			if (lastValidFrame == -1 || end <= lastValidFrame)
				return;
		}
		holdPainting(true);
		start = lastValidFrame;
		//OSPLog.debug(Performance.timeCheckStr("ParticleModel.refreshSteps0 " + start + " " + end + " " + nCalc,
		//		Performance.TIME_MARK));
		if (Tracker.timeLogEnabled)
			Tracker.logTime(
					this.getClass().getSimpleName() + this.hashCode() + " refreshing steps " + start + " to " + end); //$NON-NLS-1$ //$NON-NLS-2$
		boolean singleStep = (end - start == 1);
		// step forward to end
		ImageCoordSystem coords = trackerPanel.getCoords();
		// get underlying coords if appropriate
		boolean useDefault = isUseDefaultReferenceFrame();
		while (useDefault && coords instanceof ReferenceFrame) {
			coords = ((ReferenceFrame) coords).getCoords();
		}
		double startTime = t0 + dt * tracePtsPerStep * (start - getStartFrame()) / clip.getStepSize();
		double stepSize = 1.0 * clip.getStepSize() / tracePtsPerStep;
		int stepCount = (tracePtsPerStep * (end - start)) / clip.getStepSize();
		ParticleModel[] models = getModels();
		// prepare larger trace arrays and copy existing points into them
		int nmodels = models.length;
		for (int i = 0; i < nmodels; i++) {
			ParticleModel model = models[i];
			model.locked = false;
			int traceLength = model.traceX.length + stepCount;
			model.prevX = model.traceX;
			model.prevY = model.traceY;
			model.traceX = Arrays.copyOf(model.prevX, traceLength);
			model.traceY = Arrays.copyOf(model.prevY, traceLength);
//			model.traceY = new double[traceLength];
//			System.arraycopy(model.prevX, 0, model.traceX, 0, model.prevX.length);
//			System.arraycopy(model.prevY, 0, model.traceY, 0, model.prevY.length);
		}

		for (int i = 0; i < stepCount; i++) {
			int stepNumber = i + 1;
			int frameNumber = start + (int) (stepNumber * stepSize);
			time = startTime + stepNumber * dt;
			if (!getNextTracePositions())
				continue;
			AffineTransform transform = coords.getToImageTransform(frameNumber);
			for (int j = 0; j < nmodels; j++) {
				transform.transform(points[j], points[j]);
				// determine if point is invalid due to out of bounds
				boolean valid = Math.abs(points[j].x) < xLimit && Math.abs(points[j].y) < yLimit;
				if (!valid && !invalidWarningShown) {
					invalidWarningShown = true;
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(trackerPanel,
								TrackerRes.getString("ParticleModel.Dialog.Offscreen.Message1") + XML.NEW_LINE //$NON-NLS-1$
										+ TrackerRes.getString("ParticleModel.Dialog.Offscreen.Message2"), //$NON-NLS-1$
								TrackerRes.getString("ParticleModel.Dialog.Offscreen.Title"), //$NON-NLS-1$
								JOptionPane.WARNING_MESSAGE);
					});
				}
				models[j].traceX[models[j].prevX.length + i] = valid ? points[j].x : Double.NaN;
				models[j].traceY[models[j].prevY.length + i] = valid ? points[j].y : Double.NaN;
				if (stepNumber % tracePtsPerStep == 0) { // refresh position step
					saveState(frameNumber);
					PositionStep step = (PositionStep) models[j].getStep(frameNumber);
					if (step == null) {
						step = createPositionStep(models[j], frameNumber, 0, 0);
						step.setFootprint(models[j].getFootprint());
						models[j].steps.setStep(frameNumber, step);
					}
					step.getPosition().setPosition(valid ? points[j] : nan); // this method is fast
				}
			}
		}
		int count = 4 + (end - start);
		int startUpdate = start;
		// step back twice to pick up possible valid derivatives
		if (startUpdate > clip.getStepSize())
			startUpdate -= clip.getStepSize();
		if (startUpdate > clip.getStepSize())
			startUpdate -= clip.getStepSize();
		setLastValidFrame(end);
		for (int m = 0; m < nmodels; m++) {
			ParticleModel model = models[m];
			model.steps.setLength(end + 1);
			coords = trackerPanel.getCoords(); // get active coords
			// special treatment if this is the origin of current reference frame
			if (coords instanceof ReferenceFrame && ((ReferenceFrame) coords).getOriginTrack() == model) {
				// set origins of reference frame
				boolean prev = model.refreshing; // save refreshing value
				model.refreshing = true;
				((ReferenceFrame) coords).setOrigins();
				// then set positions to zero wrt origins
				for (int i = 0, ns = clip.getStepCount(); i < ns; i++) {
					int frameNumber = clip.stepToFrame(i);
					PositionStep step = (PositionStep) model.getStep(frameNumber);
					if (step == null)
						continue;
					AffineTransform transform = coords.getToImageTransform(frameNumber);
					Point2D.Double point = model.points[model.myPoint];
					point.setLocation(0, 0);
					transform.transform(point, point);
					step.getPosition().setPosition(point); // this method is fast
				}
				model.refreshing = prev; // restore refreshing value
//				if (!refreshDerivsLater) {
//					model.updateDerivatives(startUpdate, count);
//				}
// BH this was duplicated with an else { } clause next.
			}
			if (!refreshDerivsLater) {
				model.updateDerivatives(startUpdate, count);
			}
			if (model.vAtOrigin)
				model.vTailsToOriginItem.doClick();
			if (model.aAtOrigin)
				model.aTailsToOriginItem.doClick();
			if (!refreshDerivsLater && singleStep) {
				model.firePropertyChange(TTrack.PROPERTY_TTRACK_STEP, null, new Integer(n));
			}
			// erase refreshed steps
			for (int i = start + 1; i <= end; i++) {
				Step step = model.getStep(i);
				if (step != null)
					step.erase();
			}
			model.locked = true;
		}

		//OSPLog.debug(Performance.timeCheckStr("ParticleModel.refreshSteps " + nCalc, Performance.TIME_MARK));
		holdPainting(false);
		if (!refreshDerivsLater && !singleStep) {
			fireStepsChanged();
		}
		TFrame.repaintT(trackerPanel);
	}

	protected void holdPainting(boolean b) {
		trackerPanel.getTFrame().holdPainting(b);
	}

	@Override
	public void fireStepsChanged() {
		ParticleModel[] models = getModels();
		for (int m = 0, n = models.length - 1; m < n; m++) {
			models[m].fireStepsChanged();
		}
		super.fireStepsChanged();
	}

	/**
	 * Creates a position step with image coordinates. Overridden by
	 * ParticleDataTrack.
	 *
	 * @param track the PointMass track
	 * @param n     the frame number
	 * @param x     the x coordinate
	 * @param y     the y coordinate
	 * 
	 * @return the PositionStep
	 */
	protected PositionStep createPositionStep(PointMass track, int n, double x, double y) {
		PositionStep newStep = new PositionStep(track, n, x, y);
		newStep.valid = !Double.isNaN(x) && !Double.isNaN(y);
		return newStep;
	}

	/**
	 * Refreshes the derivatives if they have not been refreshed in the
	 * refreshSteps() method (ie if the variable "refreshDerivsLater" is false).
	 */
	protected void refreshDerivsIfNeeded() {
		if (!refreshDerivsLater)
			return;
		refreshDerivsLater = false;
		for (ParticleModel part : getModels()) {
			part.updateDerivatives();
		}
		fireStepsChanged();
	}

	/**
	 * Trims all steps after endFrame.
	 */
	protected void trimSteps() {
		// return if trimming not needed
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int n = clip.getFrameCount() - 1;
		int end = getEndFrame() == Integer.MAX_VALUE ? n : getEndFrame();
		while (end > getStartFrame() && !clip.includesFrame(end)) {
			end--;
		}
		if (end >= lastValidFrame)
			return;
		int trimCount = (tracePtsPerStep * (lastValidFrame - end)) / clip.getStepSize();
		ParticleModel[] models = getModels();
		for (ParticleModel next : models) {
			// create smaller trace arrays and copy existing points into them
			next.locked = false;
			int traceLength = next.traceX.length - trimCount;
			if (traceLength < 0)
				return; // trap for error during closing
			next.prevX = next.traceX;
			next.prevY = next.traceY;
			next.traceX = new double[traceLength];
			next.traceY = new double[traceLength];
			System.arraycopy(next.prevX, 0, next.traceX, 0, traceLength);
			System.arraycopy(next.prevY, 0, next.traceY, 0, traceLength);
			// reduce number of steps
			next.steps.setLength(end + 1);
			// refresh derivatives
			next.updateDerivatives(end - 2, lastValidFrame - end + 2);
			// restore state
			restoreState(end);
			next.locked = true;
		}
		fireStepsChanged();
		setLastValidFrame(end);
		repaint();
//		TFrame.repaintT(trackerPanel);
	}

	/**
	 * Saves the current state. Does nothing by default, but DynamicParticle
	 * overrides.
	 * 
	 * @param frameNumber the frame number
	 */
	protected void saveState(int frameNumber) {
	}

	/**
	 * Restores the state to a previously saved state, if any. Does nothing by
	 * default, but DynamicParticle overrides.
	 * 
	 * @param frameNumber the frame number
	 * @return true if state successfully restored
	 */
	protected boolean restoreState(int frameNumber) {
		return false;
	}

	/**
	 * Determines if the default reference frame is used to determine step
	 * positions.
	 * 
	 * @return true if the default reference frame is used
	 */
	protected boolean isUseDefaultReferenceFrame() {
		ImageCoordSystem coords = trackerPanel.getCoords();
		if (coords instanceof ReferenceFrame && ((ReferenceFrame) coords).getOriginTrack() == this) {
			return true;
		}
		return useDefaultReferenceFrame;
	}

	/**
	 * Sets the useDefaultReferenceFrame flag.
	 * 
	 * @param useDefault true to use the default reference frame
	 */
	public void setUseDefaultReferenceFrame(boolean useDefault) {
		useDefaultReferenceFrame = useDefault;
	}

	/**
	 * Gets the model builder.
	 * 
	 * @return the model builder
	 */
	public FunctionTool getModelBuilder() {
		if (trackerPanel == null)
			return null;
		if (modelBuilder == null) {
			modelBuilder = trackerPanel.getModelBuilder();
			modelBuilder.addPanel(getName(), functionPanel);
			modelBuilder.addPropertyChangeListener(this);
			if (trackerPanel.getTFrame() != null) {
				trackerPanel.getTFrame().addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			}
			if (getInitEditor().getValues()[0] == 0) {
				refreshInitialTime();
				getInitEditor().getTable().clearSelection();
			}
		}
		return modelBuilder;
	}

	/**
	 * Initializes the ModelFunctionPanel. The panel must created by subclasses
	 * before calling this method.
	 */
	protected void initializeFunctionPanel() {
		createMassAndTimeParameters();
	}

	/**
	 * This adds the mass and initial time parameters to the function panel.
	 */
	protected void createMassAndTimeParameters() {
		Parameter param = new Parameter("m", String.valueOf(getMass())); //$NON-NLS-1$
		param.setNameEditable(false);
		param.setDescription(TrackerRes.getString("ParticleModel.Parameter.Mass.Description")); //$NON-NLS-1$
		getParamEditor().addObject(param, false);
		param = new Parameter("t", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		param.setNameEditable(false);
		param.setDescription(TrackerRes.getString("ParticleModel.Parameter.InitialTime.Description")); //$NON-NLS-1$
		functionPanel.getInitEditor().addObject(param, false);
		massParamListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if ("m".equals(e.getOldValue())) { //$NON-NLS-1$
					double m = ((Parameter) getParamEditor().getObject("m")).getValue(); //$NON-NLS-1$
					if (mass != m) {
						setMass(m);
					}
				}
			}
		};
		getParamEditor().addPropertyChangeListener(massParamListener);

		timeParamListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (refreshing)
					return;
				if ("t".equals(e.getOldValue()) && trackerPanel != null) { //$NON-NLS-1$
					VideoClip clip = trackerPanel.getPlayer().getVideoClip();
					double timeOffset = ((Parameter) getInitEditor().getObject("t")).getValue() * 1000
							- clip.getStartTime();
					double dt = trackerPanel.getPlayer().getMeanStepDuration();
					int n = clip.getStartFrameNumber();
					boolean mustRound = timeOffset % dt > 0;
					n += clip.getStepSize() * (int) Math.round(timeOffset / dt);
					setStartFrame(n);
					if (getStartFrame() != n || mustRound)
						Toolkit.getDefaultToolkit().beep();
				}
			}
		};
		getInitEditor().addPropertyChangeListener(timeParamListener);
	}

	/**
	 * Gets the initial values.
	 * 
	 * @return initial values
	 */
	public double[] getInitialValues() {
		return functionPanel.getInitEditor().getValues();
	}

	/**
	 * Gets the parameter editor.
	 * 
	 * @return ParamEditor
	 */
	public ParamEditor getParamEditor() {
		return functionPanel.getParamEditor();
	}

	/**
	 * Gets the initial value editor.
	 * 
	 * @return the editor
	 */
	public InitialValueEditor getInitEditor() {
		return functionPanel.getInitEditor();
	}

	/**
	 * Gets the function editor.
	 * 
	 * @return UserFunctionEditor
	 */
	public UserFunctionEditor getFunctionEditor() {
		return functionPanel.getUserFunctionEditor();
	}

	private void positionModelBuilder() {
		if (inspectorX != Integer.MIN_VALUE) {
			// trackerPanel will select this track when getModelBuilder() is called
			// only if loader has set the showModelBuilder flag (so refreshing is false)
			refreshing = !showModelBuilder;
			loading = true; // prevents setting trackerPanel changed flag
			getModelBuilder();
			refreshing = loading = false;
			TFrame frame = trackerPanel.getTFrame();
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			if (inspectorH != Integer.MIN_VALUE)
				modelBuilder.setSize(modelBuilder.getWidth(), Math.min(inspectorH, dim.height));
			int x = Math.max(frame.getLocation().x + inspectorX, 0);
			x = Math.min(x, dim.width - modelBuilder.getWidth());
			int y = Math.max(frame.getLocation().y + inspectorY, 0);
			y = Math.min(y, dim.height - modelBuilder.getHeight());
			modelBuilder.setLocation(x, y);
			inspectorX = Integer.MIN_VALUE;
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
			ParticleModel p = (ParticleModel) obj;
			// save mass
			control.setValue("mass", p.getMass()); //$NON-NLS-1$
			// save track data
			XML.getLoader(TTrack.class).saveObject(control, obj);
			// save velocity and acceleration footprint and color if not default
			Footprint fp = p.getVelocityFootprint();
			if (!fp.getColor().equals(p.getColor())) {
				control.setValue("velocity_color", fp.getColor()); //$NON-NLS-1$
			}
			if (!fp.getName().equals(p.getVelocityFootprints()[0].getName())) {
				control.setValue("velocity_footprint", fp.getName()); //$NON-NLS-1$
			}
			fp = p.getAccelerationFootprint();
			if (!fp.getColor().equals(p.getColor())) {
				control.setValue("acceleration_color", fp.getColor()); //$NON-NLS-1$
			}
			if (!fp.getName().equals(p.getAccelerationFootprints()[0].getName())) {
				control.setValue("acceleration_footprint", fp.getName()); //$NON-NLS-1$
			}
			// save parameters, initial values and functions
			Parameter[] params = p.getParamEditor().getParameters();
			control.setValue("user_parameters", params); //$NON-NLS-1$
			Parameter[] inits = p.getInitEditor().getParameters();
			control.setValue("initial_values", inits); //$NON-NLS-1$
			UserFunction[] functions = p.getFunctionEditor().getMainFunctions();
			control.setValue("main_functions", functions); //$NON-NLS-1$
			functions = p.getFunctionEditor().getSupportFunctions();
			if (functions.length > 0)
				control.setValue("support_functions", functions); //$NON-NLS-1$
			// save start and end frames (if custom)
			if (p.startFrame > 0)
				control.setValue("start_frame", p.startFrame); //$NON-NLS-1$
			if (p.endFrame < Integer.MAX_VALUE)
				control.setValue("end_frame", p.endFrame); //$NON-NLS-1$
			// save model builder size and position
			if (p.modelBuilder != null && p.trackerPanel != null && p.trackerPanel.getTFrame() != null) {
				// save builder location relative to frame
				TFrame frame = p.trackerPanel.getTFrame();
				int x = p.modelBuilder.getLocation().x - frame.getLocation().x;
				int y = p.modelBuilder.getLocation().y - frame.getLocation().y;
				control.setValue("inspector_x", x); //$NON-NLS-1$
				control.setValue("inspector_y", y); //$NON-NLS-1$
				control.setValue("inspector_h", p.modelBuilder.getHeight()); //$NON-NLS-1$
				control.setValue("inspector_visible", p.modelBuilder.isVisible()); //$NON-NLS-1$
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
			return null;
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
			// load track data
			XML.getLoader(TTrack.class).loadObject(control, obj);
			ParticleModel p = (ParticleModel) obj;
			// load mass
			double m = control.getDouble("mass"); //$NON-NLS-1$
			if (m != Double.NaN) {
				p.mass = m;
			}
			// load velocity and acceleration footprint and color
			Color c = (Color) control.getObject("velocity_color"); //$NON-NLS-1$
			if (c != null)
				p.setVelocityColor(c);
			else
				p.setVelocityColor(p.getColor());
			String s = control.getString("velocity_footprint"); //$NON-NLS-1$
			if (s != null)
				p.setVelocityFootprint(s);
			else
				p.setVelocityFootprint(p.getVelocityFootprints()[0].getName());

			c = (Color) control.getObject("acceleration_color"); //$NON-NLS-1$
			if (c != null)
				p.setAccelerationColor(c);
			else
				p.setAccelerationColor(p.getColor());
			s = control.getString("acceleration_footprint"); //$NON-NLS-1$
			if (s != null)
				p.setAccelerationFootprint(s);
			else
				p.setAccelerationFootprint(p.getAccelerationFootprints()[0].getName());

			p.inspectorX = control.getInt("inspector_x"); //$NON-NLS-1$
			p.inspectorY = control.getInt("inspector_y"); //$NON-NLS-1$
			p.inspectorH = control.getInt("inspector_h"); //$NON-NLS-1$
			p.showModelBuilder = control.getBoolean("inspector_visible"); //$NON-NLS-1$
			Parameter[] params = (Parameter[]) control.getObject("user_parameters"); //$NON-NLS-1$
			p.getParamEditor().setParameters(params);
			params = (Parameter[]) control.getObject("initial_values"); //$NON-NLS-1$
			// remove trailing "0" from initial condition parameters
			for (int i = 0; i < params.length; i++) {
				Parameter param = params[i];
				String name = param.getName();
				int n = name.lastIndexOf("0"); //$NON-NLS-1$
				if (n > -1) {
					// replace parameter with new one
					name = name.substring(0, n);
					Parameter newParam = new Parameter(name, param.getExpression());
					newParam.setDescription(param.getDescription());
					newParam.setNameEditable(false);
					params[i] = newParam;
				}
			}
			p.getInitEditor().setParameters(params);
			UserFunction[] functions = (UserFunction[]) control.getObject("main_functions"); //$NON-NLS-1$
			p.getFunctionEditor().setMainFunctions(functions);
			functions = (UserFunction[]) control.getObject("support_functions"); //$NON-NLS-1$
			if (functions != null) {
				for (int i = 0; i < functions.length; i++) {
					p.getFunctionEditor().addObject(functions[i], false);
				}
			}
			p.functionPanel.refreshFunctions();
			int n = control.getInt("start_frame"); //$NON-NLS-1$
			if (n != Integer.MIN_VALUE)
				p.startFrame = n;
			else {
				p.startFrameUndefined = true;
			}
			n = control.getInt("end_frame"); //$NON-NLS-1$
			if (n != Integer.MIN_VALUE)
				p.endFrame = n;
			return obj;
		}
	}

}
