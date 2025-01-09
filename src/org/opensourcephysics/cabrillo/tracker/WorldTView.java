/*
* The tracker package defines a set of video/image analysis tools
* built on the Open Source Physics framework by Wolfgang Christian.
*
* Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
* <https://opensourcephysics.github.io/tracker-website/>.
*/
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.opensourcephysics.cabrillo.tracker.TrackerIO.ComponentImage;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionTool;

/**
 * This is a TView of a TrackerPanel drawn in world space. 
 * It is a zoomable TView with a single component, worldPanel(). 
 * 
 * An unusual TView,
 * WorldTView is not just a JPanel, it is a full TrackerPanel. A distinction is made for several tracks, including CenterOfMass, PointMass,
 * Vector, and VectorStep, all of which call getDisplayedPanel() in order to get the displayed panel. 
 *
 * 
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class WorldTView extends ZoomTView {

	protected static final Icon WORLDVIEW_ICON = Tracker.getResourceIcon("axes.gif", true); //$NON-NLS-1$ ;

	private static final String[] panelProps = new String[] { TrackerPanel.PROPERTY_TRACKERPANEL_SIZE,
			TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO,
			TrackerPanel.PROPERTY_TRACKERPANEL_IMAGE, TrackerPanel.PROPERTY_TRACKERPANEL_VIDEOVISIBLE,
			TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION, ImageCoordSystem.PROPERTY_COORDS_TRANSFORM,
			TTrack.PROPERTY_TTRACK_DATA };
	
	protected static final double ZOOM_MIN = 0.25;
	protected static final double ZOOM_MAX = 8;
	private static Point viewLoc = new Point();
	private static Point mousePtRelativeToViewRect = new Point();

	private Integer worldPanelID;
	protected JLabel worldViewLabel;
	protected TButton	zoomButton;
	private AbstractAction zoomAction;
	
	/**
	 * Constructs a WorldTView of the specified TrackerPanel
	 *
	 * @param panel the tracker panel to be viewed
	 */
	public WorldTView(TrackerPanel panel) {
		// just create a local panel; we will refer to it by its panelID.		
		super(new WorldPanel(panel));
		WorldPanel worldPanel = (WorldPanel)super.getTrackerPanel();
		worldPanelID = worldPanel.getID();
		worldPanel.view = this;
		Icon zoomIcon = Tracker.getResourceIcon("zoom.gif", true); //$NON-NLS-1$
		zoomAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// set zoom center to center of current viewport before zooming
				Rectangle rect = scrollPane.getViewport().getViewRect();
				zoomCenter.setLocation(rect.x + rect.width / 2, rect.y + rect.height / 2);
				String name = e.getActionCommand();
				if (name.equals("auto")) { //$NON-NLS-1$
					worldPanel().setMagnification(-1);
				} else {
					double mag = Double.parseDouble(name);
					worldPanel().setMagnification(mag/100);
				}
			}
		};

		zoomButton = new TButton(zoomIcon) {
			@Override
			protected JPopupMenu getPopup() {
				return refreshZoomPopup(new JPopupMenu());
			}
		};

		worldViewLabel = new JLabel();
		worldViewLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 0));
		toolbarComponents.add(worldViewLabel);
		toolbarComponents.add(Box.createHorizontalStrut(8));
		toolbarComponents.add(zoomButton);		
	}
	
	@Override
	protected boolean doResized() {
		if (!super.doResized())
			return false;
		TrackerPanel trackerPanel = worldPanel().getMainPanel();
		trackerPanel.eraseAll();
		refreshZoomButton();
		TFrame.repaintT(worldPanel());
		return true;
	}

	@Override
	protected void doMouseDragged(MouseEvent e) {
		Rectangle rect = scrollPane.getViewport().getViewRect();
		Dimension dim = new Dimension();
		scrollPane.getViewport().getView().getSize(dim);
		int dx = mousePtRelativeToViewRect.x - e.getPoint().x + rect.x;
		int dy = mousePtRelativeToViewRect.y - e.getPoint().y + rect.y;
		if (e.isAltDown()) {
			zoomCenter.setLocation(e.getPoint());
			boolean zoomed = false;
			if (dy - dx > 4) {
				zoomIn(true); // zoom by a step
				zoomed = true;
			} else if (dx - dy > 4) {
				zoomOut(true); // zoom by a step
				zoomed = true;
			}
			if (zoomed) {
				viewLoc.setLocation(rect.getLocation());
				mousePtRelativeToViewRect.setLocation(e.getPoint().x - rect.x, e.getPoint().y - rect.y);
			}
			return;
		}
		int x = Math.max(0, viewLoc.x + dx);
		x = Math.min(x, dim.width - rect.width);
		int y = Math.max(0, viewLoc.y + dy);
		y = Math.min(y, dim.height - rect.height);
		if (x != rect.x || y != rect.y) {
			worldPanel().setMouseCursor(Tracker.grabCursor);
			rect.x = x;
			rect.y = y;
			worldPanel().scrollRectToVisible(rect);
		} else {
			viewLoc.setLocation(rect.getLocation());
			mousePtRelativeToViewRect.setLocation(e.getPoint().x - rect.x, e.getPoint().y - rect.y);
		}		
	}
	
	@Override
	protected void doMouseReleased(MouseEvent e) {
		worldPanel().setMouseCursor(Cursor.getDefaultCursor());
	}
	
	@Override
	protected void doMousePressed(MouseEvent e) {
		zoomCenter.setLocation(e.getPoint());
		Rectangle rect = scrollPane.getViewport().getViewRect();
		worldPanel().setMouseCursor(Tracker.grabCursor);
		viewLoc.setLocation(rect.getLocation());
		mousePtRelativeToViewRect.setLocation(e.getPoint().x - rect.x, e.getPoint().y - rect.y);
	}

	@Override
	protected void refreshZoomButton() {
		Runnable runner = new Runnable() {
			@Override
			public synchronized void run() {
				if (worldPanel() == null) return;
				scrollPane.getViewport().setView(worldPanel()); // is this needed?
				Dimension full = worldPanel().getFullSize();
				Dimension dim = worldPanel().getSize();
				double zoom = Math.min(100*dim.height/full.height, 100*dim.width/full.width); // actual
				zoomButton.setText(TToolBar.zoomFormat.format(zoom) + "%"); //$NON-NLS-1$
				if (zoom > 105 * worldPanel().getMagnification())
					worldPanel().setMagnification(-1);
			}
		};
		SwingUtilities.invokeLater(runner);
	}
	
	protected JPopupMenu refreshZoomPopup(JPopupMenu popup) {
		popup.removeAll();
		JMenuItem item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ToFit")); //$NON-NLS-1$
		item.setActionCommand("auto"); //$NON-NLS-1$
		item.addActionListener(zoomAction);
		popup.add(item);
		popup.addSeparator();
		for (int i = 0, nz = TrackerPanel.ZOOM_LEVELS.length; i < nz; i++) {
			if (TrackerPanel.ZOOM_LEVELS[i] > ZOOM_MAX
					|| TrackerPanel.ZOOM_LEVELS[i] < ZOOM_MIN)
				continue;
			int n = (int) (100 * TrackerPanel.ZOOM_LEVELS[i]);
			String m = String.valueOf(n);
			item = new JMenuItem(m + "%"); //$NON-NLS-1$
			item.setActionCommand(m);
			item.addActionListener(zoomAction);
			popup.add(item);
		}
		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	public BufferedImage render(BufferedImage image) {
		return worldPanel().render(image);
	}

	@Override
	public void refresh() {
		if (!isViewPaneVisible())
			return;
		worldViewLabel.setText(TrackerRes.getString("TFrame.View.World")); //$NON-NLS-1$
		worldPanel().refresh();
	}

	@Override
	public void init() {
		//worldPanel().initWP();
	}

	@Override
	public void cleanup() {
		super.cleanup();
		if (worldPanel() != null)
			worldPanel().cleanup();
	}
	
	@Override
	public void dispose() {
		super.dispose();		
		worldPanel().dispose();
		worldPanelID = null;
	}
	
	@Override
	public boolean isCustomState() {
		Dimension dim = worldPanel().getPreferredSize();
		return dim.width > 1;
	}

	@Override
	public TrackerPanel getTrackerPanel() {
		return worldPanel().getMainPanel();
	}

	@Override
	public String getViewName() {
		return TrackerRes.getString("TFrame.View.World"); //$NON-NLS-1$
	}

	@Override
	public Icon getViewIcon() {
		return WORLDVIEW_ICON;
	}

	@Override
	public int getViewType() {
		return TView.VIEW_WORLD;
	}

	@Override
	public ArrayList<Component> getToolBarComponents() {
		worldViewLabel.setText(TrackerRes.getString("TFrame.View.World")); //$NON-NLS-1$
		refreshZoomButton();
		return super.getToolBarComponents();
	}

	@Override
	public Dimension getSize() {
		return scrollPane.getViewport().getExtentSize();
//		return super.getSize();
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		propertyChangeImpl(e);
	}

	private void propertyChangeImpl(PropertyChangeEvent e) {
		// coming to WorldPanel or WorldView
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getOldValue() != null) { // track removed
				TTrack removed = (TTrack) e.getOldValue();
				removed.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
				removed.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
			}
			refresh();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			for (TTrack track : TTrack.getValues()) {
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
			TFrame.repaintT(worldPanel());
			break;
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
		case TrackerPanel.PROPERTY_TRACKERPANEL_SIZE:
		case TTrack.PROPERTY_TTRACK_DATA:
			refresh();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION:
			break;
		default:
			System.err.println("WoldTView.propertyChange " + e.getPropertyName() + " " + e.getSource());
			break;
		}
		// no sending to super? 
		// no worldPanel().propertyChange(e); ? Original did not pass, either
	}

	private WorldPanel worldPanel() {
		return (WorldPanel) frame.getTrackerPanelForID(worldPanelID);
	}

	static class WorldPanel extends TrackerPanel {

		protected JMenuItem copyImageItem;
		protected JMenuItem printItem;
		protected JMenuItem helpItem;
		protected Integer mainPanelID;
		protected double zoomFactor = ZOOM_MIN;
		protected Rectangle scrollRect = new Rectangle();
		protected WorldTView view;
		
		private WorldPanel(TrackerPanel panel) {
			super(panel.frame, panel);
			mainPanelID = panel.getID();
			cleanup();
			// add this view to tracker panel listeners
			// note "track" and "clear" not needed since forwarded from TViewChooser
			TrackerPanel trackerPanel = getMainPanel();
			trackerPanel.addListeners(panelProps, this);
			// add this view to track listeners
			for (TTrack track : trackerPanel.getTracks()) {
				track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
			}
			setPlayerVisible(false);
			setDrawingInImageSpace(false);
			setShowCoordinates(false);
		}
		
		@Override
		public void setMagnification(double magnification) {
			if (magnification == 0 || Double.isNaN(magnification))
				return;
//			double prevZoom = getMagnification();
			Dimension prevSize = getPreferredSize();
			Point p1 = new TPoint(0, 0).getScreenPosition(this);
			if (prevSize.width == 1 && prevSize.height == 1) { // zoomed to fit
				double w = getImageWidth();
				double h = getImageHeight();
				Point p2 = new TPoint(w, h).getScreenPosition(this);
				prevSize.width = p2.x - p1.x;
				prevSize.height = p2.y - p1.y;
			}
			Dimension d;
			if (magnification < 0) {
				d = new Dimension(1, 1);
			} else {
				zoom = Math.min(Math.max(magnification, MIN_ZOOM), MAX_ZOOM);
				int w = (int) (imageWidth * zoom);
				int h = (int) (imageHeight * zoom);
				d = new Dimension(w, h);
			}
			setPreferredSize(d);
//			firePropertyChange(PROPERTY_TRACKERPANEL_MAGNIFICATION, Double.valueOf(prevZoom), Double.valueOf(getMagnification()));
			// scroll and revalidate
			if (view != null) {
				view.scrollPane.revalidate();
				// this will fire a full panel repaint
				view.scrollToZoomCenter(getPreferredSize(), prevSize, p1);
				eraseAll();
			}
		}

		public Dimension getFullSize() {
			int w = (int)getImageWidth();
			int h = (int)getImageHeight();
			return new Dimension(w, h);
		}

		@Override
		protected void setGUI() {
			// set tiny preferred size so auto zooms to very small
			setPreferredSize(new Dimension(1, 1));
		}

		@Override
		protected void setMouseListeners() {
			// create and add a mouse listener to handle pressed and dragging
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (OSPRuntime.isPopupTrigger(e)) {
						createWorldPopup();
						popup.show(WorldPanel.this, e.getX(), e.getY());
					}
				}
			});
			setInteractiveMouseHandler(null);
		}

		protected void createWorldPopup() {
			getPopup().removeAll();
			getMenuItems();
			TrackerPanel trackerPanel = getMainPanel();
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
					copyImage("clipboard");
				}
			};
			copyImageItem = new JMenuItem(copyImageAction);
			// print menu item
			Action printAction = new AbstractAction(TrackerRes.getString("TActions.Action.Print")) { //$NON-NLS-1$
				@Override
				public void actionPerformed(ActionEvent e) {
					copyImage("print");
				}
			};
			printItem = new JMenuItem(printAction);
			// help item
			helpItem = new JMenuItem();
			helpItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (frame != null) {
						frame.showHelp("worldview", 0); //$NON-NLS-1$
					}
				}
			});
			
			snapshotItem = getSnapshotItem(new PopupmenuListener());
		}

		protected void copyImage(String where) {
			ComponentImage img = new TrackerIO.ComponentImage(this);
			switch (where) {
			case "clipboard":
				img.copyToClipboard();
				break;
			case "print":
				img.print();
				break;
			}
		}

		public void refresh() {
			// axes & tape items
			TrackerPanel trackerPanel = getMainPanel();
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
					track.erase(panelID);
				}
			}
			TFrame.repaintT(this);
		}

		@Override
		public TrackerPanel getMainPanel() {
			return frame.getTrackerPanelForID(mainPanelID);
		}


		@Override
		public TPoint getSnapPoint() {
			return getMainPanel().getSnapPoint();
		}

		@Override
		public TTrack getSelectedTrack() {
			return getMainPanel().getSelectedTrack();
		}

		@Override
		public void setSelectedTrack(TTrack track) {
			if (mainPanelID != null)
				getMainPanel().setSelectedTrack(track);
		}

		@Override
		public ArrayList<Drawable> getDrawables() {
			if (mainPanelID == null) {
				return super.getDrawables();
			}
			TrackerPanel trackerPanel = getMainPanel();
			// return all drawables in trackerPanel except PencilScenes 
			// add drawables in this world panel, if any
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
//			// remove noData message if trackerPanel is not empty
//			if (!trackerPanel.isEmpty)
//				remove(noData);
			return list;
		}

		@Override
		public VideoPlayer getPlayer() {
			// workaround to prevent null pointer exception during instantiation
			return (mainPanelID == null ? super.getPlayer() : getMainPanel().getPlayer());
		}

		@Override
		public ImageCoordSystem getCoords() {
			// workaround to prevent null pointer exception during instantiation
			return (mainPanelID == null ? super.getCoords() : getMainPanel().getCoords());
		}

		@Override
		protected boolean unTracked() {
			return false;
		}

		@Override
		public Interactive getInteractive() {
			return null;
		}

		public void cleanup() {
			// remove this listener from tracker panel
			if (mainPanelID != null) {
				getMainPanel().removeListeners(panelProps, this);
				// remove this listener from tracks
				for (TTrack t : TTrack.getValues()) {
					t.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
				}
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (view != null)
				view.propertyChangeImpl(e);
		}
		
		@Override
		public void dispose() {
			cleanup();
			title = getMainPanel().getTitle();
			if (mainPanelID != null) {
				TrackerPanel trackerPanel = getMainPanel();
				trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this);
				trackerPanel.removePropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION, this);
				mainPanelID = null;
			}
			
			frame.deallocatePanelID(panelID);
			super.dispose();
		}

		public WorldTView getWorldView() {
			return view;
		}

		public boolean isActive() {
			if (view == null)
				return false;
			return (TViewChooser.isSelectedView(view) && view.isViewPaneVisible());
		}

	} // end WorldPanel
		
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
			WorldTView view = (WorldTView)obj;
			control.setValue("zoom", view.worldPanel().getMagnification());
			Rectangle rect = view.scrollPane.getViewport().getViewRect();
			int[] rectData = new int[] {rect.x, rect.y, rect.width, rect.height};
			control.setValue("viewrect", rectData);
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
			WorldTView view = (WorldTView)obj;
			if (control.getPropertyNamesRaw().contains("zoom")) {
				view.worldPanel().setMagnification(control.getDouble("zoom"));
				int[] d = (int[]) control.getObject("viewrect");
				Rectangle rect = new Rectangle(d[0], d[1], d[2], d[3]);
				view.worldPanel().scrollRectToVisible(rect);						
			}
			return obj;
		}

	}
}
