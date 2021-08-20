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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.HighlightableDataset;
import org.opensourcephysics.tools.FontSizer;

/**
 * This displays plot views of a track.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class PlotTrackView extends TrackView {

	// data model	
	protected DatasetManager datasetManager;
	private boolean isCustom;
	protected boolean xAxesLinked;
	private int selectedPlot;

	// GUI
	
	private JPanel mainView;
	protected TrackPlottingPanel[] plots = new TrackPlottingPanel[3];

	/**
	 * for TrackChooserTView.viewButton
	 */
	protected JButton plotsButton;

	/**
	 * for toolbarComponents
	 */
	private JCheckBox linkCheckBox;

	

	/**
	 * Constructs a PlotTrackView for the specified track and trackerPanel.
	 *
	 * @param track the track being viewed
	 * @param panel the tracker panel interpreting the track
	 */
	public PlotTrackView(TTrack track, TrackerPanel panel, PlotTView view) {
		super(track, panel, view, TView.VIEW_PLOT);
//		OSPLog.debug(Performance.timeCheckStr("PlotTrackView constr0 for " + track, Performance.TIME_MARK));
		// get the track data object (DatasetManager)
		datasetManager = track.getData(trackerPanel);
		// create the GUI
		createGUI();
		// set the track-specified initial plot properties
		highlightVisible = !"false".equals(track.getProperty("highlights")); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < plots.length; i++) {
			plots[i].setXVariable((String) track.getProperty("xVarPlot" + i)); //$NON-NLS-1$
			plots[i].setYVariable((String) track.getProperty("yVarPlot" + i)); //$NON-NLS-1$
			boolean lines = !"false".equals(track.getProperty("connectedPlot" + i)); //$NON-NLS-1$ //$NON-NLS-2$
			plots[i].dataset.setConnected(lines);
			plots[i].linesItemSelected = lines;
			boolean pts = !"false".equals(track.getProperty("pointsPlot" + i)); //$NON-NLS-1$ //$NON-NLS-2$
			plots[i].dataset.setMarkerShape(pts ? Dataset.SQUARE : Dataset.NO_MARKER);
			plots[i].pointsItemSelected = pts;
			plots[i].dataset.setMarkerColor(track.getColor());
			Double D = (Double) track.getProperty("yMinPlot" + i); //$NON-NLS-1$
			if (D != null) {
				plots[i].setPreferredMinMaxY(D.doubleValue(), plots[i].getPreferredYMax());
			}
			D = (Double) track.getProperty("yMaxPlot" + i); //$NON-NLS-1$
			if (D != null) {
				plots[i].setPreferredMinMaxY(plots[i].getPreferredYMin(), D.doubleValue());
			}
			plots[i].isCustom = false;
		}
//		OSPLog.debug(Performance.timeCheckStr("PlotTrackView constr1 for " + track + " plots=" + plots.length,
//				Performance.TIME_MARK));
//		selectedPlot = DEFAULT_PLOT_COUNT - 1;
		setPlotCount(getDefaultPlotCount());
		refresh(trackerPanel.getFrameNumber(), 0);
	}

	/**
	 * @param frameNumber
	 * @param mode 
	 */
	@Override
	public void refresh(int frameNumber, int mode) {
		if (mode == DataTable.MODE_TRACK_CHOOSE) {
			FontSizer.setFonts(plotsButton);
			FontSizer.setFonts(linkCheckBox);
		}
		TTrack track;
		if (!isRefreshEnabled() 
				|| !viewParent.isViewPaneVisible()
				|| (track = getTrack()) == null)
			return;
		track.getData(trackerPanel);
		boolean haveSelection = (trackerPanel.selectedSteps.size() > 0);
		Color trackColor = track.getColor();
		Color mc = (trackColor.equals(Color.WHITE) ? Color.GRAY : trackColor);
		Color hc = (haveSelection ? trackColor : Color.GRAY);
		highlightFrames(frameNumber);
		for (int i = 0; i < plots.length; i++) {
			HighlightableDataset data = plots[i].getDataset();
			data.setMarkerColor(mc);
			data.setHighlightColor(hc);
			plots[i].setHighlights(highlightFrames);
			plots[i].plotData();
		}
		mainView.repaint();
	}

	@Override
	protected void dispose() {
		datasetManager = null;
		for (TrackPlottingPanel next : plots) {
			next.dispose();
		}
		plots = null;
		mainView.removeAll();
		viewParent = null;
		super.dispose();
	}

	/**
	 * Refreshes the GUI.
	 */
	@Override
	void refreshGUI() {
		linkCheckBox.setText(TrackerRes.getString("PlotTrackView.Checkbox.Synchronize")); //$NON-NLS-1$
		linkCheckBox.setToolTipText(TrackerRes.getString("PlotTrackView.Checkbox.Synchronize.Tooltip")); //$NON-NLS-1$
		plotsButton.setText(TrackerRes.getString("PlotTrackView.Button.PlotCount")); //$NON-NLS-1$
		plotsButton.setToolTipText(TrackerRes.getString("PlotTrackView.Button.PlotCount.ToolTip")); //$NON-NLS-1$
// BH: still no luck here. Some very obscure CSS oddity that causes an image less than 6(?) pixels high to 
//     not center vertically.  
		plotsButton.setVerticalAlignment(SwingConstants.CENTER);
		plotsButton.setHorizontalTextPosition(SwingConstants.LEADING);
		plotsButton.setHorizontalAlignment(SwingConstants.LEFT);
		TTrack track = getTrack();
		track.getData(trackerPanel); // load the current data
		for (int i = 0; i < plots.length; i++) {
			boolean custom = plots[i].isCustom;
			plots[i].setVariables();
			plots[i].isCustom = custom;
		}
	}

	/**
	 * Gets the view button for TrackChooserTView
	 *
	 * @return the view button
	 */
	@Override
	public JButton getViewButton() {
		return plotsButton;
	}

	/**
	 * Returns true if this trackview is in a custom state.
	 *
	 * @return true if in a custom state, false if in the default state
	 */
	@Override
	public boolean isCustomState() {
		int n = mainView.getComponentCount();
		if (isCustom || n != getDefaultPlotCount())
			return true;
		for (int i = 0; i < n; i++) {
			if (plots[i].isCustom)
				return true;
		}
		return false;
	}
	
	private int getDefaultPlotCount() {
		TTrack track = getTrack();
		return track instanceof LineProfile || track instanceof RGBRegion
				 || track instanceof CircleFitter || track instanceof Protractor? 1: 2;
	}



	/**
	 * Sets the number of plots.
	 *
	 * @param plotCount the number of plot panels desired
	 */
	public void setPlotCount(int plotCount) {
		if (plotCount == mainView.getComponentCount())
			return;
		TTrack track = getTrack();
		track.trackerPanel.changed = true;
		plotCount = Math.min(plotCount, plots.length);
		selectedPlot = plotCount - 1;
		mainView.removeAll();
		mainView.add(plots[0]);
		for (int i = 1; i < plotCount; i++) {
			mainView.add(plots[i]);
		}
		mainView.validate();
		if (plotCount > 1)
			toolbarComponents.add(linkCheckBox);
		else
			toolbarComponents.remove(linkCheckBox);
		Runnable runner = new Runnable() {
			@Override
			public synchronized void run() {
				TViewChooser chooser = plots[0].getOwner();
				if (chooser != null)
					chooser.refreshToolbar();
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
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_UNITS:
			for (TrackPlottingPanel plot : plots) {
				plot.plotData();
			}
			return;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getNewValue() != null // $NON-NLS-1$
					&& !(e.getSource() instanceof WorldTView)) {
				// track added
				for (TrackPlottingPanel plot : getPlots()) {
					plot.plotAxes.hideScaleSetter();
				}
				for (TrackPlottingPanel plot : plots) {
					plot.clearPopup();
				}
				return;
			}
			break;
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
		if (linked)
			syncXAxesTo(plots[0]);
	}

	/**
	 * Syncs the x-axes to another plot if xAxesLinked is true.
	 * 
	 * @param plot the plot to sync to
	 */
	protected void syncXAxesTo(TrackPlottingPanel plot) {
		if (!xAxesLinked)
			return;
		xAxesLinked = false; // to prevent repeated calls
		// determine x-axis variable of plot
		String var = plot.getXVariable();
		for (TrackPlottingPanel next : plots) {
			if (next != plot) {
				double xMin = plot.isAutoscaleXMin() ? Double.NaN : plot.getPreferredXMin();
				double xMax = plot.isAutoscaleXMax() ? Double.NaN : plot.getPreferredXMax();
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
	 * 
	 * @param plots the plots to sync
	 */
	protected void syncYAxes(TrackPlottingPanel... plots) {
		// determine min and max y on plots
		double yMin = Double.NaN;
		double yMax = Double.NaN;
		boolean sync = false;
		for (TrackPlottingPanel plot : plots) {
			if (!Double.isNaN(yMin))
				sync = true;
			yMin = Double.isNaN(yMin) ? plot.getPreferredYMin() : Math.min(plot.getPreferredYMin(), yMin);
			yMax = Double.isNaN(yMax) ? plot.getPreferredYMax() : Math.max(plot.getPreferredYMax(), yMax);
		}
		// set preferred min and max y on all plots
		if (sync)
			for (TrackPlottingPanel plot : plots) {
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
		TTrack track = getTrack();
		TrackPlottingPanel plotPanel = new TrackPlottingPanel(track, datasetManager);
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
			if (plots[i] == null)
				plots[i] = createPlotPanel();
		}
		// set plot count to default after completing GUI
//		for (int i = 0; i < DEFAULT_PLOT_COUNT; i++) {
//			mainView.add(plots[i]);
//		}
		setViewportView(mainView);
		
		// create link checkbox
		linkCheckBox = new JCheckBox();
		linkCheckBox.setOpaque(false);
		linkCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setXAxesLinked(linkCheckBox.isSelected());
			}
		});
		// plots button
		plotsButton = new TButton() {
			// override getMaximumSize method so has same height as chooser button
			@Override
			public Dimension getMaximumSize() {
				return TViewChooser.getButtonMaxSize(this, 
						super.getMaximumSize(), 
						getMinimumSize().height);
			}

			// override getPopup method to return plotcount popup
			@Override
			public JPopupMenu getPopup() {
				JPopupMenu plotsPopup = rebuildPlotsPopup();
				FontSizer.setFonts(plotsPopup, FontSizer.getLevel());
				return plotsPopup;
			}
		};
		plotsButton.setIcon(TViewChooser.DOWN_ARROW_ICON);
		plotsButton.setHorizontalTextPosition(SwingConstants.LEFT);
		plotsButton.setHorizontalAlignment(SwingConstants.LEFT);
		plotsButton.setVerticalTextPosition(SwingConstants.CENTER);
		plotsButton.setVerticalAlignment(SwingConstants.TOP);

	}

	private JPopupMenu rebuildPlotsPopup() {

		ActionListener plotCountSetter = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();
				setPlotCount(Integer.parseInt(item.getText()));
				refresh(trackerPanel.getFrameNumber(), 0);
			}
		};
		// create plotCount menuitems
		JRadioButtonMenuItem[] plotCountItems = new JRadioButtonMenuItem[plots.length];
		ButtonGroup plotCountGroup = new ButtonGroup();
		JPopupMenu plotsPopup = new JPopupMenu();
		for (int i = 0; i < plots.length; i++) {
			plotCountItems[i] = new JRadioButtonMenuItem(String.valueOf(i + 1));
			plotCountItems[i].addActionListener(plotCountSetter);
			plotsPopup.add(plotCountItems[i]);
			plotCountGroup.add(plotCountItems[i]);
		}
		plotCountItems[selectedPlot].setSelected(true);
		return plotsPopup;
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
		 * @param obj     the TrackerPanel object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			PlotTrackView trackView = (PlotTrackView) obj;
			control.setValue(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, trackView.getTrack().getName()); // $NON-NLS-1$
			TrackPlottingPanel[] plots = trackView.getPlots();
			for (int i = 0; i < plots.length; i++) {
				control.setValue("plot" + i, plots[i]); //$NON-NLS-1$
			}
			control.setValue("linked", trackView.xAxesLinked); //$NON-NLS-1$
		}

		/**
		 * Creates an object.
		 *
		 * @param control the control
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
			PlotTrackView trackView = (PlotTrackView) obj;
			trackView.setXAxesLinked(control.getBoolean("linked")); //$NON-NLS-1$
			TrackPlottingPanel[] plots = trackView.plots;
			int plotCount = 1;
			for (int i = 0; i < plots.length; i++) {
				XMLControl child = control.getChildControl("plot" + i); //$NON-NLS-1$
				if (child != null) {
					child.loadObject(plots[i]);
					plotCount = i + 1;
				}
			}
			trackView.setPlotCount(plotCount);
			trackView.isCustom = true;
			return obj;
		}
	}

	public Dimension getPanelSize() {
		return mainView.getSize();
	}

	public BufferedImage exportImage(int w, int h) {		
		BufferedImage image = (BufferedImage) mainView.createImage(w, h); 
		if (image == null)
			return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		mainView.paint(g2);
		g2.dispose();
		return image;
	}
	
	public int getPlotCount() {
		return mainView.getComponentCount();
	}

	@Override
	protected boolean isRefreshEnabled() {
		return super.isRefreshEnabled() && Tracker.allowPlotRefresh;
	}

	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}


}
