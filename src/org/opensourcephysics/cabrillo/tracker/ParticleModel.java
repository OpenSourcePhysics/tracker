/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2019  Douglas Brown
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

import java.text.NumberFormat;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.tools.*;

/**
 * A ParticleModel is a point mass whose positions are determined by a model.
 * 
 * @author Douglas Brown
 */
abstract public class ParticleModel extends PointMass {

  // static fields
	protected static int tracePtsPerStep = 10;
	protected static boolean loading = false;
	protected static Point2D nan = new Point2D.Double(Double.NaN, Double.NaN);
	protected static double xLimit=8000, yLimit=6000; // too far off screen
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
	protected int inspectorX = Integer.MIN_VALUE, 
		inspectorY, inspectorH = Integer.MIN_VALUE;
	protected boolean showModelBuilder;
	protected boolean refreshing = false;
	protected double[] traceX={}, traceY={};
	protected double[] prevX, prevY;
	protected TPoint tracePt = new TPoint();
	protected int lastValidFrame = -1;  // used in draw method
	protected double t0, dt = 0.1, time;
	protected boolean refreshDerivsLater, refreshStepsLater;
	protected boolean invalidWarningShown, startFrameUndefined;
	protected int startFrame, endFrame=Integer.MAX_VALUE;
	protected boolean useDefaultReferenceFrame;
	protected JMenuItem modelBuilderItem, useDefaultRefFrameItem, stampItem;
  protected PropertyChangeListener massParamListener, timeParamListener;
	
  /**
	 * Constructs a ParticleModel.
	 */
	public ParticleModel() {
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
		initializeFunctionPanel();
		// set initial hint
  	hint = TrackerRes.getString("ParticleModel.Hint"); //$NON-NLS-1$
	}

	/**
	 * Overrides PointMass draw method.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g the graphics context on which to draw
	 */
	public void draw(DrawingPanel panel, Graphics _g) {
    if (!(panel instanceof TrackerPanel) 
    			|| trackerPanel == null) return;
    if (isVisible() && trackerPanel.getFrameNumber() > lastValidFrame) {
    	refreshSteps();
    }
    drawMe(panel, _g);
	}

	/**
	 * Draws this without refreshing steps.
	 * 
	 * @param panel the drawing panel requesting the drawing
	 * @param _g the graphics context on which to draw
	 */
	public void drawMe(DrawingPanel panel, Graphics _g) {
		// position and show model builder if requested during loading
    if (inspectorX != Integer.MIN_VALUE && trackerPanel != null
						&& trackerPanel.getTFrame() != null) {
    	positionModelBuilder();
	    Runnable runner = new Runnable() {
	    	public void run() {
	      	showModelBuilder = false;
	    		modelBuilder.setVisible(true);
	    	}
	    };
    	if (showModelBuilder)
    		SwingUtilities.invokeLater(runner);
    }
		if (isVisible() && isTraceVisible()) {
  		// draw trace only if fixed coords & (non-worldview or no ref frame)
  		TrackerPanel tPanel = (TrackerPanel) panel;
  		ImageCoordSystem coords = tPanel.getCoords(); // get active coords
  		boolean isRefFrame = coords instanceof ReferenceFrame;
  		if (isRefFrame) {
  			coords = ((ReferenceFrame) coords).getCoords();
  		}
  		boolean fixed = coords.isFixedAngle() && coords.isFixedOrigin()
  					&& coords.isFixedScale();
  		if (fixed && (!(tPanel instanceof WorldTView) || !isRefFrame)) {
  			trace.reset();
  			for (int i = 0; i < traceX.length; i++) {
  				if (Double.isNaN(traceX[i])) continue;
  				tracePt.setLocation(traceX[i], traceY[i]);
  				java.awt.Point p = tracePt.getScreenPosition(tPanel);
  				if (trace.getCurrentPoint()==null) 
  					trace.moveTo((float) p.getX(), (float) p.getY());
  				else trace.lineTo((float) p.getX(), (float) p.getY());
  			}
  			Graphics2D g2 = (Graphics2D) _g;
  			Color color = g2.getColor();
  			Stroke stroke = g2.getStroke();
  			g2.setColor(getFootprint().getColor());
  			g2.setStroke(traceStroke);
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
  public void delete() {
  	FunctionTool modelBuilder = null;
  	if (trackerPanel!=null
  			&& trackerPanel.modelBuilder!=null) {
  		ArrayList<ParticleModel> list = trackerPanel.getDrawables(ParticleModel.class);
  		if (list.size()==1)
  			modelBuilder = trackerPanel.modelBuilder;
  	}  	
  	super.delete();
  	if (modelBuilder!=null) {
  		modelBuilder.setVisible(false);
  	}
  }

	/**
	 * Responds to property change events.
	 * 
	 * @param e the property change event
	 */
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		if (trackerPanel == null) return;
		String name = e.getPropertyName();
//		System.out.println(name);
		if (name.equals("function") && !loading) { //$NON-NLS-1$
			trackerPanel.changed = true;
		}
		else if (name.equals("tab")) { //$NON-NLS-1$
			if (modelBuilder != null) {
				if (trackerPanel != null 
						&& e.getNewValue() == trackerPanel 
						&& trackerPanel.isModelBuilderVisible) {
					modelBuilder.setVisible(true);				
				}
				else if (modelBuilder.isVisible() && e.getNewValue()!=null) {
					modelBuilder.setVisible(false);
					trackerPanel.isModelBuilderVisible = true;
				}
			}
			if (this instanceof ParticleDataTrack
//					&& ((ParticleDataTrack)this).isAutoPasteEnabled()
					&& trackerPanel!=null && trackerPanel.getTFrame()!=null
//					&& trackerPanel.getTFrame().clipboardListener!=null
					&& trackerPanel==e.getNewValue()) {
				trackerPanel.getTFrame().getClipboardListener().processContents(trackerPanel);
			}
		}
		else if (name.equals("selectedtrack") //$NON-NLS-1$
					&& e.getNewValue() == this && modelBuilder != null
					&& !modelBuilder.getSelectedName().equals(getName())) {
			modelBuilder.setSelectedPanel(getName());
		}
		if (name.equals("function") //$NON-NLS-1$
				|| name.equals("starttime") //$NON-NLS-1$
				|| name.equals("frameduration") //$NON-NLS-1$
				|| name.equals("startframe") //$NON-NLS-1$
				|| name.equals("stepsize")) { //$NON-NLS-1$
			lastValidFrame = -1;		
		}
		if (name.equals("transform")) { //$NON-NLS-1$
			// workaround to prevent infinite loop
      ImageCoordSystem coords = trackerPanel.getCoords();
			if (!(coords instanceof ReferenceFrame && 
    				((ReferenceFrame)coords).getOriginTrack()==this)) {
				lastValidFrame = -1;		
			}
		}
		if (!refreshing && isModelsVisible()) {
			if (name.equals("function")) { //$NON-NLS-1$
				repaint();
			}
			else if (name.equals("adjusting")) { //$NON-NLS-1$
				refreshStepsLater = (Boolean)e.getNewValue();
				if (!refreshStepsLater) {  // stopped adjusting, so refresh steps
					refreshSteps();
				}
			}
			if (name.equals("transform")) { //$NON-NLS-1$
				// workaround to prevent infinite loop
	      ImageCoordSystem coords = trackerPanel.getCoords();
				if (!(coords instanceof ReferenceFrame && 
	    				((ReferenceFrame)coords).getOriginTrack()==this)) {
					refreshSteps();
				}
			}
			else if (name.equals("starttime")  //$NON-NLS-1$
					|| name.equals("frameduration") //$NON-NLS-1$
					|| name.equals("startframe")) { //$NON-NLS-1$
				refreshInitialTime();
				refreshSteps();
			}
			else if (name.equals("stepsize")) { //$NON-NLS-1$
				refreshSteps();
			}
		}
	}

	/**
	 * Gets the mass. Overrides PointMass method.
	 * 
	 * @return the mass
	 */
	public double getMass() {
		Parameter massParam = (Parameter)getParamEditor().getObject("m"); //$NON-NLS-1$
		if (massParam != null) return massParam.getValue();
		return super.getMass();
	}

	/**
	 * Sets the mass. Overrides PointMass method.
	 * 
	 * @param mass the mass
	 */
	public void setMass(double mass) {
		super.setMass(mass);
		mass = super.getMass();
		massField.setValue(mass);
		// refresh mass parameter in paramPanel if changed
		Parameter massParam = (Parameter)getParamEditor().getObject("m"); //$NON-NLS-1$
		if (massParam!=null && massParam.getValue() != mass) {
			functionPanel.getParamEditor().setExpression("m", String.valueOf(mass), false); //$NON-NLS-1$
			refreshSteps();
		}
	}

	/**
	 * Sets the name. Overrides TTrack method.
	 * 
	 * @param name the name
	 */
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
  protected boolean isAutoTrackable() {
  	return false;
  }
  
  /**
   * Overrides PointMass findInteractive method.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step or motion vector that is hit
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
  	Interactive ia = super.findInteractive(panel, xpix, ypix);
    if (ia instanceof PositionStep.Position) {
      hint = TrackerRes.getString("PointMass.Position.Locked.Hint"); //$NON-NLS-1$
    }
    else if (ia == null)
    	hint = TrackerRes.getString("ParticleModel.Hint"); //$NON-NLS-1$
  	return ia;
  }
  
	/**
	 * Overrides TTrack setLocked method. ParticleModel is always locked.
	 * 
	 * @param locked ignored
	 */
	public void setLocked(boolean locked) {/** empty block */
	}

	/**
	 * Overrides TTrack method to report that this is a dependent track.
	 * 
	 * @return <code>true</code> if this track is dependent
	 */
	public boolean isDependent() {
		return true;
	}

	/**
	 * Overrides TTrack isStepComplete method. Always returns true.
	 * 
	 * @param n the frame number
	 * @return <code>true</code> always since gets data from model
	 */
	public boolean isStepComplete(int n) {
		return true;
	}

	/**
	 * Returns a menu with items that control this track.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	public JMenu getMenu(TrackerPanel trackerPanel) {
		if (modelBuilderItem==null) {
			// create the model  item
			modelBuilderItem = new JMenuItem();
			modelBuilderItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					positionModelBuilder();
					getModelBuilder().setVisible(true);
				}
			});
			// create the useDefaultRefFrameItem item
			useDefaultRefFrameItem = new JCheckBoxMenuItem();
			useDefaultRefFrameItem.setSelected(!useDefaultReferenceFrame);
			useDefaultRefFrameItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setUseDefaultReferenceFrame(!useDefaultRefFrameItem.isSelected());
					if (ParticleModel.this.trackerPanel.getCoords() instanceof ReferenceFrame) {
		      	lastValidFrame = -1;
		      	refreshSteps();						
					}
				}
			});
			// create the stamp item
			stampItem = new JMenuItem();
			stampItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					refreshSteps();
          PointMass pm = new PointMass();
          String proposed = getName()+" "+TrackerRes.getString("ParticleModel.Stamp.Name")+"1"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          for (TTrack track: ParticleModel.this.trackerPanel.getTracks()) {
          	if (proposed.equals(track.getName())) {
          		try {
								int n = Integer.parseInt(proposed.substring(proposed.length()-1));
								proposed = proposed.substring(0, proposed.length()-1) + (n+1);
							} catch (NumberFormatException ex) {
								continue;
							}
          	}
          }
      		pm.setName(proposed);
      		pm.setColor(getColor().darker());
          ParticleModel.this.trackerPanel.addTrack(pm);
      		for (Step step: getSteps()) {
      			if (step==null) continue;
      			TPoint pt = step.getPoints()[0];
      			int n = pt.getFrameNumber(ParticleModel.this.trackerPanel);
      			pm.createStep(n, pt.x, pt.y);
      		}
      		ParticleModel.this.trackerPanel.repaint();
				}
			});
		}
		modelBuilderItem.setText(TrackerRes.getString("ParticleModel.MenuItem.InspectModel")); //$NON-NLS-1$
		useDefaultRefFrameItem.setText(TrackerRes.getString("ParticleModel.MenuItem.UseDefaultReferenceFrame")); //$NON-NLS-1$
		String stamp = TrackerRes.getString("ParticleModel.MenuItem.Stamp"); //$NON-NLS-1$
		String pm = TrackerRes.getString("PointMass.Name"); //$NON-NLS-1$
		stampItem.setText(stamp+" "+pm); //$NON-NLS-1$
		stampItem.setToolTipText(TrackerRes.getString("ParticleModel.MenuItem.Stamp.Tooltip")); //$NON-NLS-1$
		// assemble the menu
		JMenu menu = super.getMenu(trackerPanel);

		// remove unwanted menu items and separators
		menu.remove(autotrackItem);
		menu.remove(deleteStepItem);
		menu.remove(clearStepsItem);
		menu.remove(lockedItem);
		menu.remove(autoAdvanceItem);
		menu.remove(markByDefaultItem);
		menu.insert(modelBuilderItem, 0);
		if (menu.getItemCount() > 1) menu.insertSeparator(1);
		
		// find acceleration menu and insert stampItem after it
    if (trackerPanel.isEnabled("model.stamp")) { //$NON-NLS-1$
			for (int i=0; i<menu.getMenuComponentCount(); i++) {
				if (menu.getMenuComponent(i)==accelerationMenu) {
					menu.insert(stampItem, i+1);
				  menu.insertSeparator(i+1);
					break;
				}
			}
    }
		
//		// find visible item and insert useDefaultRefFrameItem after it
//		for (int i=0; i<menu.getMenuComponentCount(); i++) {
//			if (menu.getMenuComponent(i)==visibleItem) {
//				menu.insert(useDefaultRefFrameItem, i+1);
//				break;
//			}
//		}
		
		// eliminate any double separators
		Object prevItem = modelBuilderItem;
		int n = menu.getItemCount();
		for (int j = 1; j < n; j++) {
			Object item = menu.getItem(j);
			if (item == null && prevItem == null) { // found extra separator
				menu.remove(j - 1);
				j = j - 1;
				n = n - 1;
			}
			prevItem = item;
		}
		return menu;
	}

	/**
	 * Overrides TTrack getToolbarPointComponents method.
	 * 
	 * @param trackerPanel the tracker panel
	 * @param point the TPoint
	 * @return a list of components
	 */
	public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel,
					TPoint point) {
		ArrayList<Component> list = super.getToolbarPointComponents(trackerPanel, point);
		xField.setEnabled(false);
		yField.setEnabled(false);
		magField.setEnabled(false);
		angleField.setEnabled(false);
		return list;
	}
	
  /**
   * Sets the start frame for this model. Also sets the initial time to 
   * the video clip time at the start frame.
   * 
   * @param n the desired start frame
   */
	public void setStartFrame(int n) {
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		n = Math.max(n, clip.getFirstFrameNumber()); // not less than first frame
		int end = clip.getLastFrameNumber();
		n = Math.min(n, end); // not greater than last frame
		n = Math.min(n, getEndFrame()); // not greater than endFrame
		if (n==startFrame) return;
		startFrame = n;
		refreshInitialTime();
		lastValidFrame = -1;
		refreshSteps();
		trackerPanel.repaint();
		firePropertyChange("model_start", null, getStartFrame()); //$NON-NLS-1$
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
		if (n==getEndFrame()) return;
		endFrame = n<end? n: Integer.MAX_VALUE;
		if (n<lastValidFrame) {
			trimSteps();
		}
		else {
			refreshSteps();
		}
		trackerPanel.repaint();
		firePropertyChange("model_end", null, getEndFrame()); //$NON-NLS-1$
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
   * Identifies the controlling TrackerPanel for this track (by default,
   * the first TrackerPanel that adds this track to its drawables).
   *
   * @param panel the TrackerPanel
   */
  protected void setTrackerPanel(TrackerPanel panel) {
	  super.setTrackerPanel(panel);
	  if (panel!=null) {
	  	if (startFrameUndefined) {
	  		int n = panel.getPlayer().getVideoClip().getStartFrameNumber();
	  		setStartFrame(n);
	  		startFrameUndefined = false;
	  	}
		  if (panel.getTFrame()!=null) {
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
  protected void setAnglesInRadians(boolean radians) {
    super.setAnglesInRadians(radians);
		functionPanel.initEditor.setAnglesInDegrees(!radians);
		String s = TrackerRes.getString("DynamicParticle.Parameter.InitialTheta.Description")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		s += radians?
				TrackerRes.getString("TableTrackView.Radians.Tooltip"):  //$NON-NLS-1$
				TrackerRes.getString("TableTrackView.Degrees.Tooltip");  //$NON-NLS-1$
		functionPanel.initEditor.setDescription(FunctionEditor.THETA, s);
		s = TrackerRes.getString("DynamicParticle.Parameter.InitialOmega.Description")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		s += radians?
				TrackerRes.getString("TableTrackView.RadiansPerSecond.Tooltip"):  //$NON-NLS-1$
				TrackerRes.getString("TableTrackView.DegreesPerSecond.Tooltip");  //$NON-NLS-1$
				functionPanel.initEditor.setDescription(FunctionEditor.OMEGA, s);
  }

  @Override
  protected void dispose() {
  	if (trackerPanel!=null) {
			trackerPanel.removePropertyChangeListener("data", this); //$NON-NLS-1$
	    if (trackerPanel.getTFrame() != null) {
	    	trackerPanel.getTFrame().removePropertyChangeListener("tab", this); //$NON-NLS-1$
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
	 * Gets the next trace positions. Subclasses override to get positions based
	 * on model.
	 */
	abstract Point2D[] getNextTracePositions();

  /**
	 * Resets model parameters and sets position(s) for start frame.
	 * Most of the work in this method must be done by subclasses.
	 */
	protected void reset() {
//		invalidWarningShown = false;	
	}

  /**
	 * Gets the particle models associated with this model.
	 * By default this model is returned.
   * @return array of particle models associated with this model
	 */
	protected ParticleModel[] getModels() {
		return new ParticleModel[] {this};
	}
	
	protected boolean isModelsVisible() {
		for (ParticleModel model: getModels()) {
			if (model.isTraceVisible() || (model.isVisible()
					&& (model.isPositionVisible(trackerPanel)
								|| model.isVVisible(trackerPanel) 
								|| model.isAVisible(trackerPanel)))) {
				return true;
			}
		}
		return false;
	}

  /**
	 * Refreshes initial time parameter for this model.
	 */
	protected void refreshInitialTime() {
		if (trackerPanel==null) return;
		double t0 = trackerPanel.getPlayer().getFrameTime(getStartFrame())/1000;
		String t = timeFormat.format(t0);
		Parameter param = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		if (param.getValue() != t0) {
			boolean prev = refreshing;
			refreshing = true;
			getInitEditor().setExpression("t", t, false); //$NON-NLS-1$
			refreshing = prev;
		}
	}
	
	/**
	 * Refreshes step positions.
	 */
	protected void refreshSteps() {
		locked = true;
		if (refreshStepsLater)
			return;
    // return if this is an empty dynamic system
    if (this instanceof DynamicSystem) {
    	DynamicSystem system = (DynamicSystem)this;
    	if (system.particles.length==0)
    		return;
    }
  	if (trackerPanel != null) {
  		refreshDerivsLater = trackerPanel.getPlayer().getClipControl().isPlaying();
  		int n = trackerPanel.getFrameNumber();
      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
      // determine last frame to be marked (must satisfy both model and clip)
  		int end = Math.min(getEndFrame(), n);
  		while (end>getStartFrame() && !clip.includesFrame(end)) {
  			end--;
  		}
      if (end<=lastValidFrame) return;
    	if (lastValidFrame == -1) {
    		reset(); // initializes model, sets lastValidFrame to marked frame, if any
    		if (lastValidFrame==-1 || end<=lastValidFrame) return;
    	}
    	int start = lastValidFrame;
      Tracker.logTime(this.getClass().getSimpleName()+this.hashCode()+" refreshing steps "+start+" to "+end); //$NON-NLS-1$ //$NON-NLS-2$
      boolean singleStep = (end-start==1);
      // step forward to end
      ImageCoordSystem coords = trackerPanel.getCoords();
      // get underlying coords if appropriate
      boolean useDefault = isUseDefaultReferenceFrame();
      while (useDefault && coords instanceof ReferenceFrame) {
        coords = ( (ReferenceFrame) coords).getCoords();
      }
      double startTime = t0 + dt*tracePtsPerStep*
      		(start-getStartFrame())/clip.getStepSize();
      double stepSize = 1.0*clip.getStepSize()/tracePtsPerStep;
      int stepCount = (tracePtsPerStep*(end-start))/clip.getStepSize();
      ParticleModel[] models = getModels();
      // prepare larger trace arrays and copy existing points into them
      for (ParticleModel next: models) {
        next.locked = false;
        int traceLength = next.traceX.length+stepCount;
      	next.prevX = next.traceX;
      	next.prevY = next.traceY;
      	next.traceX = new double[traceLength];
      	next.traceY = new double[traceLength];
        System.arraycopy(next.prevX, 0, next.traceX, 0, next.prevX.length);
        System.arraycopy(next.prevY, 0, next.traceY, 0, next.prevY.length);
      }
      for (int i = 0; i < stepCount; i++) {
      	int stepNumber = i+1;
      	int frameNumber = start+(int)(stepNumber*stepSize);
        time = startTime + stepNumber*dt;
        Point2D[] points = getNextTracePositions();
        if (points==null) continue;
      	AffineTransform transform = coords.getToImageTransform(frameNumber);
        for (int j = 0; j < models.length; j++) {
          transform.transform(points[j], points[j]);
          // determine if point is invalid due to out of bounds
        	boolean valid = Math.abs(points[j].getX())<xLimit 
        			&& Math.abs(points[j].getY())<yLimit;
        	if (!valid && !invalidWarningShown) {
        		invalidWarningShown = true;
            Runnable runner = new Runnable() { // avoids deadlock?
            	public void run() {
//            		if (invalidWarningShown) return;
            		JOptionPane.showMessageDialog(trackerPanel, 
            				TrackerRes.getString("ParticleModel.Dialog.Offscreen.Message1")+XML.NEW_LINE  //$NON-NLS-1$
            				+ TrackerRes.getString("ParticleModel.Dialog.Offscreen.Message2"),  //$NON-NLS-1$
            				TrackerRes.getString("ParticleModel.Dialog.Offscreen.Title"), //$NON-NLS-1$
            				JOptionPane.WARNING_MESSAGE);
            	}
            };
            SwingUtilities.invokeLater(runner);
        	}
          models[j].traceX[models[j].prevX.length+i] = 
          		valid? points[j].getX(): Double.NaN;
          models[j].traceY[models[j].prevY.length+i] = 
          		valid? points[j].getY(): Double.NaN;
        	if (stepNumber%tracePtsPerStep == 0) { // refresh position step
        		saveState(frameNumber);
            PositionStep step = (PositionStep)models[j].getStep(frameNumber);
            if (step==null) {
          		step = createPositionStep(models[j], frameNumber, 0, 0);
          		step.setFootprint(models[j].getFootprint());
          		models[j].steps.setStep(frameNumber, step);
            }
            step.getPosition().setPosition(valid? points[j]: nan); // this method is fast
        	}
        }
      }
      int count = 4+(end-start);
      int startUpdate = start;
      // step back twice to pick up possible valid derivatives
      if (startUpdate>clip.getStepSize())
      	startUpdate -= clip.getStepSize();
      if (startUpdate>clip.getStepSize())
      	startUpdate -= clip.getStepSize();
      lastValidFrame = end;
      for (ParticleModel next: models) {
      	next.steps.setLength(end+1);
  	    coords = trackerPanel.getCoords(); // get active coords
  	    // special treatment if this is the origin of current reference frame
  	    if (coords instanceof ReferenceFrame && 
  	    				((ReferenceFrame)coords).getOriginTrack() == next) {
  	    	// set origins of reference frame
  	    	boolean prev = next.refreshing; // save refreshing value
  	    	next.refreshing = true;
  	    	((ReferenceFrame)coords).setOrigins();
  	    	// then set positions to zero wrt origins
  	      for (int i = 0; i < clip.getStepCount(); i++) {
  	      	int frameNumber = clip.stepToFrame(i);
  	        PositionStep step = (PositionStep)next.getStep(frameNumber);
  	        if (step==null) continue;
  	        AffineTransform transform = coords.getToImageTransform(frameNumber); 
  	        next.point.setLocation(0, 0);
  	        transform.transform(next.point, next.point);
  	        step.getPosition().setPosition(next.point); // this method is fast
  	      }
  	      next.refreshing = prev; // restore refreshing value
  	      if (!refreshDerivsLater) {
  	      	next.updateDerivatives(startUpdate, count);
  	      }
  	    }
  	    else if (!refreshDerivsLater) {
  	    	next.updateDerivatives(startUpdate, count);
  	    }
  	    if (next.vAtOrigin) next.vTailsToOriginItem.doClick();
  	    if (next.aAtOrigin) next.aTailsToOriginItem.doClick();
  	    if (!refreshDerivsLater) {
	  	    if (singleStep)
	    	    next.support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
	  	    else
	  	    	next.support.firePropertyChange("steps", null, null); //$NON-NLS-1$
  	    }
  	    // erase refreshed steps
  	    for (int i=start+1; i<=end; i++) {
  	    	Step step = next.getStep(i);
  	    	if (step!=null) step.erase();
  	    }
  	    next.locked = true;
      }
    	trackerPanel.repaint();
  	}
	}
	
	/**
	 * Creates a position step with image coordinates. Overridden by ParticleDataTrack.
   *
   * @param track the PointMass track
   * @param n the frame number
   * @param x the x coordinate
   * @param y the y coordinate
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
		if (!refreshDerivsLater) return;
		refreshDerivsLater = false;
    for (ParticleModel part: getModels()) {
    	part.updateDerivatives();
    	part.firePropertyChange("steps", null, null); //$NON-NLS-1$
    }
	}

	/**
	 * Trims all steps after endFrame.
	 */
	protected void trimSteps() {
		 		// return if trimming not needed
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int n = clip.getFrameCount()-1;
		int end = getEndFrame()==Integer.MAX_VALUE? n: getEndFrame();
		while (end>getStartFrame() && !clip.includesFrame(end)) {
			end--;
		}
		if (end>=lastValidFrame) return;
    int trimCount = (tracePtsPerStep*(lastValidFrame-end))/clip.getStepSize();
    ParticleModel[] models = getModels();
    for (ParticleModel next: models) {
      // create smaller trace arrays and copy existing points into them
      next.locked = false;
      int traceLength = next.traceX.length-trimCount;
      if (traceLength<0) return;  // trap for error during closing
    	next.prevX = next.traceX;
    	next.prevY = next.traceY;
    	next.traceX = new double[traceLength];
    	next.traceY = new double[traceLength];
      System.arraycopy(next.prevX, 0, next.traceX, 0, traceLength);
      System.arraycopy(next.prevY, 0, next.traceY, 0, traceLength);
      // reduce number of steps      
  		next.steps.setLength(end+1);
  		// refresh derivatives
      next.updateDerivatives(end-2, lastValidFrame-end+2);
      // restore state
      restoreState(end);
    	next.support.firePropertyChange("steps", null, null); //$NON-NLS-1$
      next.locked = true;
    }
		lastValidFrame = end;
		repaint();
//		trackerPanel.repaint();
	}
	
	/**
	 * Saves the current state.
	 * Does nothing by default, but DynamicParticle overrides.
	 * 
	 * @param frameNumber the frame number
	 */
	protected void saveState(int frameNumber) {		
	}
	
	/**
	 * Restores the state to a previously saved state, if any.
	 * Does nothing by default, but DynamicParticle overrides.
	 * 
	 * @param frameNumber the frame number
	 * @return true if state successfully restored
	 */
	protected boolean restoreState(int frameNumber) {
		return false;
	}
	
	/**
	 * Determines if the default reference frame is used to determine step positions.
	 * 
	 * @return true if the default reference frame is used
	 */
	protected boolean isUseDefaultReferenceFrame() {
		ImageCoordSystem coords = trackerPanel.getCoords();
		if (coords instanceof ReferenceFrame
				&& ((ReferenceFrame)coords).getOriginTrack()==this) {
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
		if (trackerPanel == null) return null;
		if (modelBuilder == null) {
			modelBuilder = trackerPanel.getModelBuilder();
			modelBuilder.addPanel(getName(), functionPanel);
			modelBuilder.addPropertyChangeListener(this);
      if (trackerPanel.getTFrame() != null) {
      	trackerPanel.getTFrame().addPropertyChangeListener("tab", this); //$NON-NLS-1$
      }
      if (getInitEditor().getValues()[0] == 0) {
      	refreshInitialTime();
    		getInitEditor().getTable().clearSelection();
      }
		}
		return modelBuilder;
	}

	/**
	 * Initializes the ModelFunctionPanel. The panel must created by
	 * subclasses before calling this method.
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
		  public void propertyChange(PropertyChangeEvent e) {
		  	if ("m".equals(e.getOldValue())) { //$NON-NLS-1$
			  	if ("m".equals(e.getOldValue())) { //$NON-NLS-1$
			  		Parameter param = (Parameter)getParamEditor().getObject("m"); //$NON-NLS-1$
			      if (ParticleModel.super.getMass() != param.getValue()) {
			      	setMass(param.getValue());
			      }
			  	}
		  	}
		  }
		};
		getParamEditor().addPropertyChangeListener(massParamListener);

    timeParamListener = new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent e) {
		  	if (refreshing) return;
		  	if ("t".equals(e.getOldValue()) && trackerPanel != null) { //$NON-NLS-1$
		  		Parameter param = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		      double timeOffset = param.getValue()*1000 - clip.getStartTime();
		      double dt = trackerPanel.getPlayer().getMeanStepDuration();
		      int n = clip.getStartFrameNumber();
		      boolean mustRound = timeOffset%dt>0;
		      n += clip.getStepSize()*(int)Math.round(timeOffset/dt);
		      setStartFrame(n);
		      if (getStartFrame()!=n || mustRound)
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
			x = Math.min(x, dim.width-modelBuilder.getWidth());
			int y = Math.max(frame.getLocation().y + inspectorY, 0);
			y = Math.min(y, dim.height-modelBuilder.getHeight());
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
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
    	ParticleModel p = (ParticleModel)obj;
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
      if (p.startFrame>0)
      	control.setValue("start_frame", p.startFrame); //$NON-NLS-1$
      if (p.endFrame<Integer.MAX_VALUE)
      	control.setValue("end_frame", p.endFrame); //$NON-NLS-1$
  		// save model builder size and position
  		if (p.modelBuilder != null &&
  						p.trackerPanel != null && 
  						p.trackerPanel.getTFrame() != null) {
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
    public Object createObject(XMLControl control){
      return null;
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      // load track data
      XML.getLoader(TTrack.class).loadObject(control, obj);
      ParticleModel p = (ParticleModel)obj;
      // load mass
      double m = control.getDouble("mass"); //$NON-NLS-1$
      if (m != Double.NaN) {
        p.mass = m;
      }
      // load velocity and acceleration footprint and color
      Color c = (Color)control.getObject("velocity_color"); //$NON-NLS-1$
      if (c!=null) p.setVelocityColor(c);
      else p.setVelocityColor(p.getColor());
      String s = control.getString("velocity_footprint"); //$NON-NLS-1$
      if (s!=null) p.setVelocityFootprint(s);
      else p.setVelocityFootprint(p.getVelocityFootprints()[0].getName());
      
      c = (Color)control.getObject("acceleration_color"); //$NON-NLS-1$
      if (c!=null) p.setAccelerationColor(c);
      else p.setAccelerationColor(p.getColor());
      s = control.getString("acceleration_footprint"); //$NON-NLS-1$
      if (s!=null) p.setAccelerationFootprint(s);
      else p.setAccelerationFootprint(p.getAccelerationFootprints()[0].getName());
      
  		p.inspectorX = control.getInt("inspector_x"); //$NON-NLS-1$
  		p.inspectorY = control.getInt("inspector_y"); //$NON-NLS-1$
  		p.inspectorH = control.getInt("inspector_h"); //$NON-NLS-1$
  		p.showModelBuilder = control.getBoolean("inspector_visible"); //$NON-NLS-1$
  		Parameter[] params = (Parameter[])control.getObject("user_parameters"); //$NON-NLS-1$
  		p.getParamEditor().setParameters(params);
  		params = (Parameter[])control.getObject("initial_values"); //$NON-NLS-1$
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
  		UserFunction[] functions = (UserFunction[])control.getObject("main_functions"); //$NON-NLS-1$
  		p.getFunctionEditor().setMainFunctions(functions);
  		functions = (UserFunction[])control.getObject("support_functions"); //$NON-NLS-1$
  		if (functions != null) {
  			for (int i = 0; i < functions.length; i++) {
    			p.getFunctionEditor().addObject(functions[i], false);
  			}
  		}
  		p.functionPanel.refreshFunctions();
  		int n = control.getInt("start_frame"); //$NON-NLS-1$
  		if (n!=Integer.MIN_VALUE)
  			p.startFrame = n;
  		else {
  			p.startFrameUndefined = true;
  		}
  		n = control.getInt("end_frame"); //$NON-NLS-1$
  		if (n!=Integer.MIN_VALUE)
  			p.endFrame = n;
      return obj;
    }
  }

}
