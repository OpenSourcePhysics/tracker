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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

import javax.swing.*;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.*;
import org.opensourcephysics.tools.FontSizer;

/**
 * This displays plot views of a track.
 *
 * @author Douglas Brown
 */
public class PlotTrackView extends TrackView {

  // instance fields
  protected DatasetManager data;
  protected JComponent mainView; //
  protected TrackPlottingPanel[] plots = new TrackPlottingPanel[3];
  protected JButton plotsButton;
  protected JCheckBox linkCheckBox;
  protected JPopupMenu popup;
  protected JRadioButtonMenuItem[] plotCountItems;
  protected ButtonGroup plotCountGroup;
  protected boolean highlightVisible = true;
  protected int defaultPlotCount = 1;
  private boolean isCustom;
  protected boolean xAxesLinked;


  /**
   * Constructs a PlotTrackView for the specified track and trackerPanel.
   *
   * @param track the track being viewed
   * @param panel the tracker panel interpreting the track
   */
  public PlotTrackView(TTrack track, TrackerPanel panel) {
    super(track, panel);
    // get the track data object (DatasetManager)
    data = track.getData(trackerPanel);
    // create the GUI
    createGUI();
    // set the track-specified initial plot properties
    for (int i = 0; i < plots.length; i++) {
      highlightVisible = !"false".equals(track.getProperty("highlights")); //$NON-NLS-1$ //$NON-NLS-2$
      plots[i].setXVariable((String)track.getProperty("xVarPlot"+i)); //$NON-NLS-1$
      plots[i].setYVariable((String)track.getProperty("yVarPlot"+i)); //$NON-NLS-1$
      boolean lines = !"false".equals(track.getProperty("connectedPlot"+i)); //$NON-NLS-1$ //$NON-NLS-2$
      plots[i].dataset.setConnected(lines);
      plots[i].linesItem.setSelected(lines);
      boolean pts = !"false".equals(track.getProperty("pointsPlot"+i)); //$NON-NLS-1$ //$NON-NLS-2$
      plots[i].dataset.setMarkerShape(pts? Dataset.SQUARE: Dataset.NO_MARKER);
      plots[i].pointsItem.setSelected(pts);
      plots[i].dataset.setMarkerColor(track.getColor());
      Double D = (Double)track.getProperty("yMinPlot"+i); //$NON-NLS-1$
      if (D != null) {
        plots[i].setPreferredMinMaxY(D.doubleValue(), plots[i].getPreferredYMax());
      }
      D = (Double)track.getProperty("yMaxPlot"+i); //$NON-NLS-1$
      if (D != null) {
        plots[i].setPreferredMinMaxY(plots[i].getPreferredYMin(), D.doubleValue());
      }
      plots[i].isCustom = false;
    }
  }

  /**
   * Refreshes this view.
   *
   * @param frameNumber the frame number
   */
  public void refresh(int frameNumber) {
  	if (!isRefreshEnabled()) return;
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh "+frameNumber); //$NON-NLS-1$
    
    track.getData(trackerPanel);
    for (int i = 0; i < plots.length; i++) {
      HighlightableDataset data = plots[i].getDataset();
      data.setMarkerColor(track.getColor());
      if (highlightVisible) {
        data.setHighlightColor(track.getColor());
        plots[i].setHighlighted(frameNumber);
      }
      else plots[i].setHighlighted(-1); // hides all highlights
      plots[i].plotData();
    }
    mainView.repaint();
  }

  /**
   * Refreshes the GUI.
   */
  void refreshGUI(){
  	linkCheckBox.setText(TrackerRes.getString("PlotTrackView.Checkbox.Synchronize")); //$NON-NLS-1$
  	linkCheckBox.setToolTipText(TrackerRes.getString("PlotTrackView.Checkbox.Synchronize.Tooltip")); //$NON-NLS-1$
    plotsButton.setText(TrackerRes.getString("PlotTrackView.Button.PlotCount")); //$NON-NLS-1$
    plotsButton.setToolTipText(TrackerRes.getString("PlotTrackView.Button.PlotCount.ToolTip")); //$NON-NLS-1$
    track.getData(trackerPanel); // load the current data
    for (int i = 0; i < plots.length; i++) {
    	boolean custom = plots[i].isCustom;
      plots[i].createVarChoices();
      plots[i].isCustom = custom;
    }
  }

  void dispose() {/** empty block */}
  
  /**
   * Gets the toolbar components
   *
   * @return an ArrayList of components to be added to a toolbar
   */
  public ArrayList<Component> getToolBarComponents() {
    return super.getToolBarComponents();
  }

  /**
   * Gets the view button
   *
   * @return the view button
   */
  public JButton getViewButton() {
  	return plotsButton;
  }
  
  /**
   * Returns true if this trackview is in a custom state.
   *
   * @return true if in a custom state, false if in the default state
   */
  public boolean isCustomState() {
  	int n = mainView.getComponentCount();
  	if (isCustom || n != defaultPlotCount) return true;
  	for (int i = 0; i < n; i++) {
  		if (plots[i].isCustom) return true;
  	}
  	return false;
  }

  /**
   * Sets the number of plots.
   *
   * @param plotCount the number of plot panels desired
   */
  public void setPlotCount(int plotCount) {
  	if (plotCount==mainView.getComponentCount())
  		return;
  	track.trackerPanel.changed = true;
    plotCount = Math.min(plotCount, plots.length);
    plotCountItems[plotCount-1].setSelected(true);
    mainView.removeAll();
    mainView.add(plots[0]);
    for (int i = 1; i < plotCount; i++) {
      mainView.add(plots[i]);
    }
    mainView.validate();
    if (plotCount > 1) toolbarComponents.add(linkCheckBox);
    else toolbarComponents.remove(linkCheckBox);
    Runnable runner = new Runnable() {
      public synchronized void run() {
        TViewChooser chooser = plots[0].getOwner();
        if (chooser != null) chooser.refreshToolbar();
      }
    };
    SwingUtilities.invokeLater(runner);
  }

  /**
   * Gets the visible plots.
   *
   * @return the visible plot panels
   */
  public TrackPlottingPanel[] getPlots() {
    int n = mainView.getComponentCount();
    TrackPlottingPanel[] visiblePlots = new TrackPlottingPanel[n];
    for (int i = 0; i < n; i++) {
    	visiblePlots[i] = plots[i];
    }
    return visiblePlots;
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("track") && e.getNewValue()!=null //$NON-NLS-1$ // track added
    		&& !(e.getSource() instanceof WorldTView)) {
      for (TrackPlottingPanel plot: getPlots()) {
      	plot.plotAxes.hideScaleSetter();
      }
      for (TrackPlottingPanel plot: plots) {
      	plot.buildPopupmenu();
      }
    }
    super.propertyChange(e);
  }
  
  /**
   * Sets the xAxesLinked property.
   *
   * @param linked true to link x-axes of all plots
   */
  protected void setXAxesLinked(boolean linked) {
  	xAxesLinked = linked;
  	linkCheckBox.setSelected(linked);
  	if (linked) syncXAxesTo(plots[0]);
  }

  /**
   * Syncs the x-axes to another plot if xAxesLinked is true.
   * @param plot the plot to sync to
   */
  protected void syncXAxesTo(TrackPlottingPanel plot) {
  	if (!xAxesLinked) return;
  	xAxesLinked = false; // to prevent repeated calls
  	// determine x-axis variable of plot
  	String var = plot.getXVariable();
  	for (TrackPlottingPanel next: plots) {
  		if (next != plot) {
        double xMin = plot.isAutoscaleXMin()? Double.NaN: plot.getPreferredXMin();
        double xMax = plot.isAutoscaleXMax()? Double.NaN: plot.getPreferredXMax();
  			next.setXVariable(var);
        next.setPreferredMinMaxX(xMin, xMax);
  			next.scale();
  			next.repaint();
  		}
  	}
  	xAxesLinked = true;
  }

  /**
   * Syncs the y-axes of two or more plots.
   * @param plots the plots to sync
   */
  protected void syncYAxes(TrackPlottingPanel... plots) {
  	// determine min and max y on plots
  	double yMin = Double.NaN;
  	double yMax = Double.NaN;
  	boolean sync = false;
    for (TrackPlottingPanel plot : plots) {
    	if (!Double.isNaN(yMin)) sync = true;
      yMin = Double.isNaN(yMin)? plot.getPreferredYMin(): Math.min(plot.getPreferredYMin(), yMin);
      yMax = Double.isNaN(yMax)? plot.getPreferredYMax(): Math.max(plot.getPreferredYMax(), yMax);
    }
  	// set preferred min and max y on all plots
		if (sync)
			for (TrackPlottingPanel plot: plots) {
				plot.setPreferredMinMaxY(yMin, yMax);
				plot.scale();
				plot.repaint();
			}
  }

  /**
   * Gets an empty plot panel.
   *
   * @return a new empty plot panel
   */
  private TrackPlottingPanel createPlotPanel() {
    TrackPlottingPanel plotPanel = new TrackPlottingPanel(track, data);
    plotPanel.enableInspector(true);
    plotPanel.setAutoscaleX(true);
    plotPanel.setAutoscaleY(true);
    plotPanel.setPreferredSize(new Dimension(140, 140));
    plotPanel.setMinimumSize(new Dimension(100, 100));
    plotPanel.setPlotTrackView(this);
    return plotPanel;
  }

  /**
   * Creates the GUI.
   */
  private void createGUI() {
    // stack up to three plot panels vertically
    mainView = new JPanel();
    mainView.setDoubleBuffered(true);
    mainView.setLayout(new BoxLayout(mainView, BoxLayout.Y_AXIS));
    for (int i = 0; i < plots.length; i++) {
      plots[i] = createPlotPanel();
    }
    for (int i = 0; i < defaultPlotCount; i++) {
      mainView.add(plots[i]);
    }
    setViewportView(mainView);
    // create popup menu
    popup = new JPopupMenu();
    // make a listener for plot count items
    ActionListener plotCountSetter = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JMenuItem item = (JMenuItem)e.getSource();
        setPlotCount(Integer.parseInt(item.getText()));
        refresh(trackerPanel.getFrameNumber());
      }
    };
    // create plotCount menuitems
    plotCountItems = new JRadioButtonMenuItem[plots.length];
    plotCountGroup = new ButtonGroup();
    for (int i = 0; i < plots.length; i++) {
      plotCountItems[i] = new JRadioButtonMenuItem(String.valueOf(i+1));
      plotCountItems[i].addActionListener(plotCountSetter);
      popup.add(plotCountItems[i]);
      plotCountGroup.add(plotCountItems[i]);
    }
    plotCountItems[0].setSelected(true);
    // create link checkbox
    linkCheckBox = new JCheckBox();
    linkCheckBox.setOpaque(false);
    linkCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	setXAxesLinked(linkCheckBox.isSelected());
      }
    });
    // plots button
    plotsButton = new TButton() {
    	// override getMaximumSize method so has same height as chooser button
	    public Dimension getMaximumSize() {
	      Dimension dim = super.getMaximumSize();
	      Dimension min = getMinimumSize();
	    	Container c = getParent().getParent();
	  		if (c instanceof TViewChooser) {
	  			int h = ((TViewChooser)c).chooserButton.getHeight();
	  			dim.height = Math.max(h, min.height);
	  		}
	      return dim;
	    } 
	    
	    // override getPopup method to return plotcount popup
	    public JPopupMenu getPopup() {
        FontSizer.setFonts(popup, FontSizer.getLevel());
	    	return popup;
	    }
    };
  }
  
  /**
   * Returns an XML.ObjectLoader to save and load object data.
   *
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load object data.
   */
  static class Loader implements XML.ObjectLoader {

    /**
     * Saves object data.
     *
     * @param control the control to save to
     * @param obj the TrackerPanel object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      PlotTrackView trackView = (PlotTrackView)obj;
      control.setValue("track", trackView.track.getName()); //$NON-NLS-1$
      TrackPlottingPanel[] plots = trackView.getPlots();
      for (int i = 0; i < plots.length; i++) {
        control.setValue("plot"+i, plots[i]); //$NON-NLS-1$        	
      }
      control.setValue("linked", trackView.xAxesLinked); //$NON-NLS-1$        	
    }

    /**
     * Creates an object.
     *
     * @param control the control
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
      PlotTrackView trackView = (PlotTrackView)obj;
      trackView.setXAxesLinked(control.getBoolean("linked")); //$NON-NLS-1$
      TrackPlottingPanel[] plots = trackView.plots;
      int plotCount = 1;
      for (int i = 0; i < plots.length; i++) {
        XMLControl child = control.getChildControl("plot"+i); //$NON-NLS-1$ 
        if (child != null) {
        	child.loadObject(plots[i]);
        	plotCount = i+1;
        }
      }
      trackView.setPlotCount(plotCount);
      trackView.isCustom = true;
      return obj;
    }
  }
  
}

