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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a TView of a TrackerPanel drawn in world space.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class WorldTView extends TrackerPanel implements TView {

	protected static final Icon WORLDVIEW_ICON = Tracker.getResourceIcon("axes.gif", true); //$NON-NLS-1$ ;

	// instance fields
	protected TrackerPanel trackerPanel;
	protected JMenuItem copyImageItem;
	protected JMenuItem printItem;
	protected JMenuItem helpItem;
	protected JLabel worldViewLabel;
	protected ArrayList<Component> toolbarComponents = new ArrayList<Component>();

	/**
	 * Constructs a WorldTView of the specified TrackerPanel
	 *
	 * @param panel the tracker panel to be viewed
	 */
	public WorldTView(TrackerPanel panel) {
		super(panel.frame);
		trackerPanel = panel;
		init();
		setPlayerVisible(false);
		setDrawingInImageSpace(false);
		setPreferredSize(new Dimension(240, 180));
		setShowCoordinates(false);
		panelAndWorldViews.add(this);
		// world view button
		worldViewLabel = new JLabel();
		worldViewLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 0));
		toolbarComponents.add(worldViewLabel);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (OSPRuntime.isPopupTrigger(e)) {
					createWorldPopup();
					popup.show(WorldTView.this, e.getX(), e.getY());
				}
			}
		});
//		this.addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentResized(ComponentEvent e) {
//				refresh();
//			}
//		});
	}

	protected void createWorldPopup() {
		getPopup().removeAll();
		getMenuItems();
		if (trackerPanel.isEnabled("edit.copyImage")) { //$NON-NLS-1$
			copyImageItem.setText(TrackerRes.getString("TMenuBar.Menu.CopyImage")); //$NON-NLS-1$
			popup.add(copyImageItem);
			popup.add(snapshotItem);
		}
		if (trackerPanel.isEnabled("file.print")) { //$NON-NLS-1$
			if (popup.getComponentCount() > 0)
				popup.addSeparator();
			printItem.setText(TrackerRes.getString("TActions.Action.Print")); //$NON-NLS-1$
			popup.add(printItem);
		}
		if (popup.getComponentCount() > 0)
			popup.addSeparator();
		helpItem.setText(TrackerRes.getString("Tracker.Popup.MenuItem.Help")); //$NON-NLS-1$
		popup.add(helpItem);
		FontSizer.setFonts(popup, FontSizer.getLevel());
	}

	protected void getMenuItems() {
		if (copyImageItem != null)
			return;
		// copy image item
		Action copyImageAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new TrackerIO.ComponentImage(WorldTView.this).copyToClipboard();
			}
		};
		copyImageItem = new JMenuItem(copyImageAction);
		// print menu item
		Action printAction = new AbstractAction(TrackerRes.getString("TActions.Action.Print"), null) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				new TrackerIO.ComponentImage(WorldTView.this).print();
			}
		};
		printItem = new JMenuItem(printAction);
		// help item
		helpItem = new JMenuItem();
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TFrame frame = trackerPanel.getTFrame();
				if (frame != null) {
					frame.showHelp("world", 0); //$NON-NLS-1$
				}
			}
		});
	}

	/**
	 * Refreshes all tracks
	 */
	@Override
	public void refresh() {
		if (!isViewPaneVisible())
			return;
		worldViewLabel.setText(TrackerRes.getString("WorldTView.Button.World")); //$NON-NLS-1$
		// axes & tape items
		CoordAxes axes = trackerPanel.getAxes();
		if (axes != null) {
			axes.updateListenerVisible(this);
		}
		if (!trackerPanel.calibrationTools.isEmpty()) {
			for (TTrack next : trackerPanel.getTracks()) {
				if (trackerPanel.calibrationTools.contains(next)) {
					next.updateListenerVisible(this);
				}
			}
		}
		Iterator<Drawable> it = getDrawables().iterator();
		while (it.hasNext()) {
			Object next = it.next();
			if (next instanceof TTrack) {
				TTrack track = (TTrack) next;
				track.erase(this);
			}
		}
		TFrame.repaintT(this);
	}

	/**
	 * Initializes this view
	 */
	@Override
	public void init() {
		cleanup();
		// add this view to tracker panel listeners
		// note "track" and "clear" not needed since forwarded from TViewChooser
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SIZE, this); // $NON-NLS-1$
		trackerPanel.addPropertyChangeListener(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, this); // $NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEOVISIBLE, this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this); // $NON-NLS-1$
		// add this view to track listeners
		for (TTrack track : trackerPanel.getTracks()) {
			track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); //$NON-NLS-1$
		}
	}

	/**
	 * Cleans up this view
	 */
	@Override
	public void cleanup() {
		// remove this listener from tracker panel
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SIZE, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEOVISIBLE, this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_DATA, this); // $NON-NLS-1$
		// remove this listener from tracks
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack.activeTracks.get(n).removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); //$NON-NLS-1$
		}
	}

	/**
	 * Disposes of the view
	 */
	@Override
	public void dispose() {
		cleanup();
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_FUNCTION, this);
		coords.removePropertyChangeListener(this);
		trackerPanel = null;
		super.dispose();
	}

	@Override
	public void finalize() {
		OSPLog.finest(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

	/**
	 * Gets the tracker panel being viewed
	 *
	 * @return the tracker panel being viewed
	 */
	@Override
	public TrackerPanel getTrackerPanel() {
		return trackerPanel;
	}

	/**
	 * Overrides TrackerPanel getSnapPoint method.
	 *
	 * @return the snap point
	 */
	@Override
	public TPoint getSnapPoint() {
		return trackerPanel.getSnapPoint();
	}

	/**
	 * Overrides TrackerPanel getSelectedTrack method. Gets the selected track of
	 * trackerPanel.
	 *
	 * @return the selected track
	 */
	@Override
	public TTrack getSelectedTrack() {
		return trackerPanel.getSelectedTrack();
	}

	/**
	 * Sets the selected track
	 *
	 * @param track the track to select
	 */
	@Override
	public void setSelectedTrack(TTrack track) {
		trackerPanel.setSelectedTrack(track);
	}

	/**
	 * Gets the name of the view
	 *
	 * @return the name of the view
	 */
	@Override
	public String getViewName() {
		return TrackerRes.getString("TFrame.View.World"); //$NON-NLS-1$
	}

	/**
	 * Gets the icon for this view
	 *
	 * @return the icon for the view
	 */
	@Override
	public Icon getViewIcon() {
		return WORLDVIEW_ICON;
	}

	/**
	 * Gets the type of view
	 *
	 * @return one of the defined types
	 */
	@Override
	public int getViewType() {
		return TView.VIEW_WORLD;
	}

	/**
	 * Gets the toolbar components
	 *
	 * @return an ArrayList of components to be added to a toolbar
	 */
	@Override
	public ArrayList<Component> getToolBarComponents() {
		worldViewLabel.setText(TrackerRes.getString("WorldTView.Button.World")); //$NON-NLS-1$
		return toolbarComponents;
	}

	@Override
	public void refreshPopup(JPopupMenu popup) {
		// does nothing
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		switch (name) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getOldValue() != null) { // track removed
				TTrack removed = (TTrack) e.getOldValue();
				removed.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
				removed.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
			}
			refresh();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			for (Integer n : TTrack.activeTracks.keySet()) {
				TTrack track = TTrack.activeTracks.get(n);
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
			}
			refresh();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER:
		case TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE:
		case TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO:
		case TrackerPanel.PROPERTY_TRACKERPANEL_VIDEOVISIBLE:
		case TTrack.PROPERTY_TTRACK_COLOR:
		case TTrack.PROPERTY_TTRACK_VISIBLE:
			TFrame.repaintT(this);
			break;
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
		case TrackerPanel.PROPERTY_TRACKERPANEL_SIZE:
		case TTrack.PROPERTY_TTRACK_DATA:
			refresh();
			break;
		}
	}

	/**
	 * Overrides DrawingPanel getDrawables method. Returns all drawables in the
	 * tracker panel plus those in this world view.
	 *
	 * @return a list of Drawable objects
	 */
	@Override
	public ArrayList<Drawable> getDrawables() {
		if (trackerPanel == null) {
			return super.getDrawables();
		}
		// return all drawables in trackerPanel (except PencilScenes) plus those in this
		// world view
		ArrayList<Drawable> list = trackerPanel.getDrawables();
		list.addAll(super.getDrawables());
		// remove PencilScenes
		list.removeAll(trackerPanel.getDrawablesTemp(PencilScene.class));
		trackerPanel.clearTemp();
		// put mat behind everything
		TMat mat = trackerPanel.getMat();
		if (mat != null && list.get(0) != mat) {
			list.remove(mat);
			list.add(0, mat);
		}
//		// remove noData message if trackerPanel is not empty
//		if (!trackerPanel.isEmpty)
//			remove(noData);
		return list;
	}

	/**
	 * Overrides VideoPanel getPlayer method. Returns the tracker panel's player.
	 *
	 * @return the video player
	 */
	@Override
	public VideoPlayer getPlayer() {
		// workaround to prevent null pointer exception during instantiation
		if (trackerPanel == null)
			return super.getPlayer();
		return trackerPanel.getPlayer();
	}

	/**
	 * Overrides VideoPanel getCoords method. Returns the tracker panel's coords.
	 *
	 * @return the current image coordinate system
	 */
	@Override
	public ImageCoordSystem getCoords() {
		// workaround to prevent null pointer exception during instantiation
		if (trackerPanel == null)
			return super.getCoords();
		return trackerPanel.getCoords();
	}

	@Override
	protected boolean unTracked() {
		return false;
	}

//  /**
//   * Overrides TrackerPanel repaintDirtyRegion method. WorldView requires
//   * a full repaint every time since it autoscales.
//   */
//  @Override
//public void repaintDirtyRegion() {
//    if (dirty != null) {
//     TFrame.repaintT(this);
//      dirty = null;
//    }
//  }

	/**
	 * Overrides InteractivePanel getInteractive method.
	 * 
	 * @return null
	 */
	@Override
	public Interactive getInteractive() {
		return null;
	}

	/**
	 * Configures this panel. Overrides TrackerPanel method.
	 */
	@Override
	protected void configure() {
		// set tiny preferred size so auto zooms to very small
		setPreferredSize(new Dimension(1, 1));
		coords.addPropertyChangeListener(this);
		// remove DrawingPanel option controller
		removeOptionController();
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
			/** empty block */
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
			return obj;
		}
	
	}

}
