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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.border.Border;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a panel with a toolbar for selecting and controlling TViews.
 *
 * @author Douglas Brown
 */
public class TViewChooser extends JPanel implements PropertyChangeListener, OSPRuntime.Disposable {

	// static fields

	protected final static Icon MAXIMIZE_ICON = Tracker.getResourceIcon("maximize.gif", true); //$NON-NLS-1$
	protected final static Icon	RESTORE_ICON = Tracker.getResourceIcon("restore.gif", true); //$NON-NLS-1$
	protected final static Icon DOWN_ARROW_ICON = Tracker.getResourceIcon("triangle_down.gif", true); //$NON-NLS-1$

	// instance fields

	protected TFrame frame;
  protected Integer panelID;

	// data model
	
	protected TView[] tViews = new TView[4]; // views are null until needed
	protected TView selectedView;

	protected int selectedType = TView.VIEW_UNSET;
	
	// GUI

	private JToolBar toolbar;
	private Component toolbarFiller = Box.createHorizontalGlue();
	private JButton maximizeButton;
	private JPanel viewPanel;
	private JButton chooserButton;
	
	public boolean isMaximized() {
		return frame != null && (frame.getMaximizedView() != TView.VIEW_UNSET);
	}
	
	// popup menu
	
	protected JPopupMenu popup = new JPopupMenu();
	protected boolean ignoreSelectedTrack;
		
	/**
	 * Constructs a TViewChooser.
	 *
	 * @param panel the tracker panel being viewed
	 * @param type the view type 
	 */
	public TViewChooser(TrackerPanel panel, int type) {
		super(new BorderLayout());
		setName("TViewChooser " + type);
		// don't set selectedType here--it is set in setSelectedViewType()
		//OSPLog.debug(Performance.timeCheckStr("TViewChooser " + type, Performance.TIME_MARK));

		frame = panel.getTFrame();
		panelID = panel.getID();
		panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); // $NON-NLS-1$
		// viewPanel
		viewPanel = new JPanel(new CardLayout());
		viewPanel.setBorder(BorderFactory.createEtchedBorder());
		add(viewPanel, BorderLayout.CENTER);
		// toolbar along the bottom when maximized, along the top when restored
		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				toolbar.requestFocusInWindow();
				if (e.getClickCount() == 2) {
					maximizeButton.doClick(0);
				}
				if (OSPRuntime.isPopupTrigger(e)) {
					showToolbarPopup(e.getX(), e.getY());
				}
			}
		});
		toolbar.setBorder(BorderFactory.createEtchedBorder());
		add(toolbar, BorderLayout.NORTH);
		// chooser button
		chooserButton = new TButton() {
			@Override
			protected JPopupMenu getPopup() {
				return getChooserPopup();
			}
		};
		// maximize button
		Border empty = BorderFactory.createEmptyBorder(7, 3, 7, 3);
		Border etched = BorderFactory.createEtchedBorder();
		maximizeButton = new TButton(MAXIMIZE_ICON, RESTORE_ICON);
		maximizeButton.setBorder(BorderFactory.createCompoundBorder(etched, empty));
		maximizeButton.setToolTipText(TrackerRes.getString("TViewChooser.Maximize.Tooltip")); //$NON-NLS-1$
		maximizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isMaximized()) {
					maximize();
				} else
					restore();
			}
		});
		setSelectedViewType(type);
	}
	
	private void refreshMaximizeButton() {
		boolean maximized = isMaximized();
		maximizeButton.setSelected(maximized);
		if (OSPRuntime.isJS) {
			maximizeButton.setIcon(maximized? RESTORE_ICON: MAXIMIZE_ICON);
		}
		maximizeButton.setToolTipText(maximized ? 
				TrackerRes.getString("TViewChooser.Restore.Tooltip") : //$NON-NLS-1$
				TrackerRes.getString("TViewChooser.Maximize.Tooltip")); //$NON-NLS-1$
	}

	protected void showToolbarPopup(int x, int y) {
		final TView view = getSelectedView();
		if (view == null)
			return;
		JPopupMenu popup = new JPopupMenu();
		view.refreshPopup(popup);

		JMenuItem helpItem = new JMenuItem(TrackerRes.getString("Dialog.Button.Help") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(view.getViewType()) {
				case TView.VIEW_PAGE:
					frame.showHelp("textview", 0); //$NON-NLS-1$
					break;
				case TView.VIEW_TABLE:
					frame.showHelp("datatable", 0); //$NON-NLS-1$
					break;
				case TView.VIEW_PLOT:
					frame.showHelp("plot", 0); //$NON-NLS-1$
					break;
				case TView.VIEW_WORLD:
					frame.showHelp("GUI", 0); //$NON-NLS-1$
					break;
				}
			}
		});
		popup.add(helpItem);
		FontSizer.setFonts(popup, FontSizer.getLevel());
		popup.show(toolbar, x, y);
	}

	protected JPopupMenu getChooserPopup() {				// inner popup menu listener class
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// select the view type
				int i = Integer.parseInt(e.getActionCommand());
				setSelectedViewType(i);
			}
		};
		// add view items to popup
		popup.removeAll();
		JMenuItem item;
		for (int i = 0; i<TView.VIEW_NAMES.length; i++) {
			String name = TrackerRes.getString(TView.VIEW_NAMES[i]);
			item = new JMenuItem(name, TView.VIEW_ICONS[i]);
			item.setActionCommand("" + i);
			item.addActionListener(listener);
			popup.add(item);
		}
		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	/**
	 * gets the TrackerPanel containing the tracks
	 *
	 * @return the tracker panel
	 */
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(0, 0);
	}

	/**
	 * gets the TrackerPanel containing the tracks
	 *
	 * @return the tracker panel
	 */
	public TrackerPanel getTrackerPanel() {
		return frame.getTrackerPanelForID(panelID);
	}

	/**
	 * Gets the array of TViews.
	 *
	 * @return TView[]
	 */
	public TView[] getTViews() {
		return tViews;
	}

	/**
	 * Gets the view of the specified class. Will create view if none exists.
	 *
	 * @param c class PlotTView, TableTView, WorldTView, PageTView
	 * @return the view
	 */
	public TView getTView(Class<? extends TView> c) {
		// look for existing view
		for (TView view : tViews) {
			if (view != null && view.getClass() == c)
				return view;
		}
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		// create new view
		if (c == PlotTView.class) {
			return tViews[TView.VIEW_PLOT] = new PlotTView(trackerPanel);
		}
		if (c == TableTView.class) {
			return tViews[TView.VIEW_TABLE] = new TableTView(trackerPanel);
		}
		if (c == WorldTView.class) {
			return tViews[TView.VIEW_WORLD] = new WorldTView(trackerPanel);
		}
		if (c == PageTView.class) {
			return tViews[TView.VIEW_PAGE] = new PageTView(trackerPanel);
		}
		return null;
	}

	/**
	 * Gets the selected view
	 *
	 * @return the selected view
	 */
	public TView getSelectedView() {
		return selectedView;
	}

	/**
	 * Gets the selected view type
	 *
	 * @return the selected view
	 */
	public int getSelectedViewType() {
		return selectedType;
	}

	/**
	 * Selects a view
	 *
	 * @param view the view to select
	 */
	public void setSelectedView(TView view, boolean newView) {
		if (view == null || selectedView == view)
			return;

		if (newView) {
			// determine type of view
			selectedType = view instanceof PlotTView? TView.VIEW_PLOT:
					view instanceof TableTView? TView.VIEW_TABLE:
					view instanceof WorldTView? TView.VIEW_WORLD:
					TView.VIEW_PAGE;
			tViews[selectedType] = view;
			refreshViewPanel();
		}

		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		trackerPanel.changed = true;
		TTrack selectedTrack = null;
		// clean up previously selected view
		if (selectedView != null) {
			selectedView.cleanup();
			((Component) selectedView).removePropertyChangeListener(TView.PROPERTY_TVIEW_TRACKVIEW, this);
			// if switching selection, keep track same
			boolean isTrackChooser = (selectedView instanceof TrackChooserTView);
			if (isTrackChooser && !ignoreSelectedTrack) {
				selectedTrack = ((TrackChooserTView) selectedView).getSelectedTrack();
			}
			ignoreSelectedTrack = false;
		}
		selectedView = view; // cannot be null
		

		// initialize and refresh newly selected view
		view.init();
		((Component) view).addPropertyChangeListener(TView.PROPERTY_TVIEW_TRACKVIEW, this);
		if (selectedTrack != null && selectedView instanceof TrackChooserTView) {
			((TrackChooserTView) selectedView).setSelectedTrack(selectedTrack);
		}
		view.refresh();
		// put icon in button
		chooserButton.setIcon(view.getViewIcon());
		// show the view on the viewPanel
		CardLayout cl = (CardLayout) (viewPanel.getLayout());
		cl.show(viewPanel, TView.VIEW_NAMES[selectedType]);
		repaint();
		// refresh the toolbar
		refreshToolbar();
	}

	/**
	 * Selects the specified view type
	 * Null TViews are created in this method when requested
	 *
	 * @param type int
	 */
	public void setSelectedViewType(int type) {
		if (type<0 || type>3)  {
			ignoreSelectedTrack = false;
			return;
		}
		TView view = tViews[type];
		if (type==selectedType)  {
			ignoreSelectedTrack = false;
			if (view != null) {
				view.refresh();
				return;
			}
		}
		selectedType = type;
		
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		if (view == null) {
			// create new TView
			switch (type) {
			default:
			case TView.VIEW_PLOT: 
				view = new PlotTView(trackerPanel);
				break;
			case TView.VIEW_TABLE:
				view = new TableTView(trackerPanel);
				break;
			case TView.VIEW_WORLD: 
				view = new WorldTView(trackerPanel);
				break;
			case TView.VIEW_PAGE:
				view = new PageTView(trackerPanel);
			}
			tViews[type] = view;
			refreshViewPanel();
		}
		if (((Component)view).getParent() == null)
			refreshViewPanel();
		view.refresh();
		setSelectedView(view, false);
	}

	/**
	 * Removes a view of a specified type
	 *
	 * @param type one of the TView viewType constants
	 * @return the removed view, if any
	 */
	public TView removeViewType(int viewType) {
		TView view = null;
		if (viewType > -1 && viewType < 4) {
			view = tViews[viewType];
			tViews[viewType] = null;
			if (viewType == selectedType) {
				selectedView = null;
				TViewChooser[] viewChoosers = frame.getViewChoosers(panelID);
				for (int i = 0; i < viewChoosers.length; i++) {
					if (viewChoosers[i] == this) {
						setSelectedViewType(i);
						break;
					}
				}				
				
			}
			refreshViewPanel();
		}
		return view;
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			for (TView view : tViews) {
				if (view != null)
					view.propertyChange(e);
			}
			refreshToolbar();
			break;
		case TView.PROPERTY_TVIEW_TRACKVIEW:
			refreshToolbar();
			break;
		}
	}

	/**
	 * Refreshes this chooser and its current view.
	 */
	public void refresh() {
		chooserButton.setToolTipText(TrackerRes.getString("TViewChooser.Button.Choose.Tooltip")); //$NON-NLS-1$
		if (selectedView !=null)
			selectedView.refresh();
	}

	/**
	 * Refreshes the popup menus of the views.
	 */
	public void refreshMenus() {
		for (int i = 0; i < 2; i++) {
			if (tViews[i] != null) {
				TrackChooserTView chooser = (TrackChooserTView) tViews[i];
					chooser.refreshMenus();
			}
		}
	}

	/**
	 * Maximizes this chooser and its views.
	 */
	public void maximize() {
		if (isMaximized())
			return;
		// save divider locations and size
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		frame.saveCurrentDividerLocations(trackerPanel);
		JToolBar player = frame.getMainView(trackerPanel).getPlayerBar();
		if (player.getTopLevelAncestor()==frame) {
			add(player, BorderLayout.SOUTH);
		}
		TViewChooser[] choosers = frame.getViewChoosers(trackerPanel);
		for (int i = 0; i < choosers.length; i++) {
			if (choosers[i] == this) {
				frame.maximizeView(trackerPanel, i);
				break;
			}
		}
		refreshMaximizeButton();
	}

	/**
	 * Restores this chooser and its views.
	 */
	public void restore() {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		MainTView mainView = frame.getMainView(trackerPanel);
		JToolBar player = mainView.getPlayerBar();
		if (player.getParent() == this) {
			mainView.add(player, BorderLayout.SOUTH);
		}
		frame.restoreViews(trackerPanel);
		refreshMaximizeButton();
	}
	
	/**
	 * Refreshes the toolbar
	 */
	protected void refreshToolbar() {
		// BH THIS IS A PROBLEM
		// DB 7/11/20 I added checks to call this only when required
		toolbar.removeAll();
		toolbar.add(chooserButton);
		if (selectedView != null) {
			ArrayList<Component> list = selectedView.getToolBarComponents();
			if (list != null) {
				for (Component c : list) {
					toolbar.add(c);
					FontSizer.setFont(c);
				}
			}
		}
		toolbar.add(toolbarFiller);
		toolbar.add(maximizeButton);
//		FontSizer.setFonts(toolbar);
		toolbar.repaint();
	}

	/**
	 * Refreshes the viewPanel.
	 */
	private void refreshViewPanel() {
		viewPanel.removeAll();
		for (int i = 0; i< 4; i++) {
			TView view = tViews[i];
			if (view != null) {
				viewPanel.add((JPanel) view, TView.VIEW_NAMES[i]);
			}
		}
		// repaint selected view, if any
		if (selectedView != null) {
			repaint();
//			// refresh the toolbar
//			refreshToolbar();
		}
		// otherwise select the current type
		else 
			setSelectedViewType(selectedType);
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
			TViewChooser chooser = (TViewChooser) obj;
			// save the selected view
			control.setValue("selected_view", chooser.selectedView); //$NON-NLS-1$
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
			TViewChooser chooser = (TViewChooser) obj;
			TView view = (TView) control.getObject("selected_view"); //$NON-NLS-1$
			if (view != null) {
				chooser.setSelectedView(view, true);
			}
			return obj;
		}
	}
	
	/**
	 * Get the view of the specified type. May return null..
	 *
	 * @param type one of the define TView types
	 * @return the TView. May be null.
	 */
	TView getView(int type) {
		if (type >= 0 && type < tViews.length) {
			return tViews[type];
		}
		return null;
	}

	@Override
	public void paint(Graphics g) {
		if (panelID == null || !frame.getTrackerPanelForID(panelID).isPaintable()) {
		  return;
		}
		super.paint(g);
	}
	
		public static TViewChooser getChooserParent(Container c) {
		while ((c = c.getParent()) != null && !(c instanceof TViewChooser)) {}
		return (TViewChooser) c;
	}

	/**
	 * Adjust maximum size height and width for standard view.
	 * 
	 * @param c the parent of the button being checked for maximum size
	 * @param max default size
	 * @param minHeight button's minimum height
	 * @return new Dimension with height based on chooserButton height
	 */
	static Dimension getButtonMaxSize(Container c, Dimension max, int minHeight) {
		c = getChooserParent(c);
		return (c == null ? max 
				: new Dimension(max.width, Math.max(minHeight, ((TViewChooser) c).chooserButton.getHeight())));
	}

	/**
	 * Returns true if this view is selected in it's parent TViewChooser.
	 * 
	 * @return true if selected
	 */
	public static boolean isSelectedView(TView view) {
			TViewChooser c = getChooserParent((Container) view);
			return (c != null && view == c.getSelectedView());
	}

	@Override
	public void finalize() {
		//		OSPLog.finer
		//System.out.println(getClass().getSimpleName() + " " + getName() + " finalized"); //$NON-NLS-1$
	}

	/**
	 * Disposes of this chooser
	 */
	@Override
	public void dispose() {
		CardLayout cl = (CardLayout) viewPanel.getLayout();
		for (TView view : tViews) {
			if (view != null) {
				((Component) view).removePropertyChangeListener("trackview", this); //$NON-NLS-1$
				cl.removeLayoutComponent((JComponent) view);
				OSPRuntime.Disposable.deallocate(view);
			}
		}
		tViews = null;
		selectedView = null;

		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); // $NON-NLS-1$
		viewPanel.removeAll();
		toolbar.removeAll();
		panelID = null;
		frame = null;
	}

	@Override
	public String toString() {
		return getName() + " " + Arrays.toString(tViews);
	}

}
