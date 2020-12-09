/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;

/**
 * This is the main video view for Tracker. It puts the tracker panel in a
 * zoomable scrollpane and puts the player in a detachable toolbar.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class MainTView extends JPanel implements TView {

	// instance fields
	private TrackerPanel trackerPanel;
	JScrollPane scrollPane;
	Rectangle scrollRect = new Rectangle();
	private Point zoomCenter = new Point();
	private JToolBar playerBar;
	private MouseAdapter mouseAdapter;
	KeyAdapter keyAdapter;
	
	/**
	 * Constructs a main view of a tracker panel.
	 *
	 * @param panel the tracker panel
	 */
	public MainTView(TrackerPanel panel) {
		trackerPanel = panel;
		init();
		setLayout(new BorderLayout());
		scrollPane = new JScrollPane();
		scrollPane.addComponentListener(new ComponentAdapter() {
			Dimension lastDim;
			@Override
			public void componentResized(ComponentEvent e) {
				if (!getTopLevelAncestor().isVisible())
					return;
				Dimension d;
				if ((d = scrollPane.getSize()).equals(lastDim))
					return;
				lastDim = d;
				TToolBar.getToolbar(trackerPanel).refreshZoomButton();
				trackerPanel.eraseAll();
			//OSPLog.debug("MainTView testing no repaint");	
			//TFrame.repaintT(trackerPanel);
			}
		});
		SwingUtilities.replaceUIActionMap(scrollPane, null);
		add(scrollPane, BorderLayout.CENTER);
		
		// add trackbar north
		add(TTrackBar.getTrackbar(trackerPanel), BorderLayout.NORTH);

		// add player to the playerBar
		playerBar = new JToolBar();
		playerBar.setFloatable(false);
		add(playerBar, BorderLayout.SOUTH);
		trackerPanel.getPlayer().setBorder(null);
		trackerPanel.setPlayerVisible(false);
		playerBar.add(trackerPanel.getPlayer());
		// add trackerPanel to scrollPane
		scrollPane.setViewportView(trackerPanel);
		trackerPanel.setScrollPane(scrollPane);

		mouseAdapter = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				zoomCenter.setLocation(e.getPoint());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// handle zoom actions
				if (Tracker.isZoomOutCursor(trackerPanel.getCursor())) {
					zoomOut(false);
				} else if (Tracker.isZoomInCursor(trackerPanel.getCursor())) {
					zoomIn(false);
				}
			}

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				boolean invert = e.isControlDown() && !e.isShiftDown();
				boolean zoom = (!Tracker.scrubMouseWheel && !invert) || (Tracker.scrubMouseWheel && invert);
				if (zoom)
					zoomCenter.setLocation(e.getPoint());
				int n = trackerPanel.getPlayer().getStepNumber();
				if (e.getWheelRotation() > 0) {
					if (zoom)
						zoomOut(true); // zoom by a step
					else {
						if (e.isAltDown())
							trackerPanel.getPlayer().setStepNumber(n - 10);
						else
							trackerPanel.getPlayer().back();
					}
				} else {
					if (zoom)
						zoomIn(true); // zoom by a step
					else {
						if (e.isAltDown())
							trackerPanel.getPlayer().setStepNumber(n + 10);
						else
							trackerPanel.getPlayer().step();
					}
				}
			}
		};

		keyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				JButton z = trackerPanel.getTFrame().getToolBar(trackerPanel).zoomButton;
				int d = trackerPanel.getSelectedPoint() == null ? 10 : 0;
				Rectangle rect = scrollPane.getViewport().getViewRect();
				switch (e.getKeyCode()) {
				case KeyEvent.VK_Z:
					if (!e.isControlDown())
						z.setSelected(true);
					break;
				case KeyEvent.VK_ALT:
					break;
				case KeyEvent.VK_PAGE_UP:
					if (!trackerPanel.getPlayer().isEnabled())
						return;
					if (e.isShiftDown()) {
						int n = trackerPanel.getPlayer().getStepNumber() - 5;
						trackerPanel.getPlayer().setStepNumber(n);
					} else
						trackerPanel.getPlayer().back();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					if (!trackerPanel.getPlayer().isEnabled())
						return;
					if (e.isShiftDown()) {
						int n = trackerPanel.getPlayer().getStepNumber() + 5;
						trackerPanel.getPlayer().setStepNumber(n);
					} else
						trackerPanel.getPlayer().step();
					break;
				case KeyEvent.VK_HOME:
					if (!trackerPanel.getPlayer().isEnabled())
						return;
					trackerPanel.getPlayer().setStepNumber(0);
					break;
				case KeyEvent.VK_END:
					if (!trackerPanel.getPlayer().isEnabled())
						return;
					VideoClip clip = trackerPanel.getPlayer().getVideoClip();
					trackerPanel.getPlayer().setStepNumber(clip.getStepCount() - 1);
					break;
				case KeyEvent.VK_UP:
					rect.y -= d;
					trackerPanel.scrollRectToVisible(rect);
					break;
				case KeyEvent.VK_DOWN:
					rect.y += d;
					trackerPanel.scrollRectToVisible(rect);
					break;
				case KeyEvent.VK_RIGHT:
					rect.x += d;
					trackerPanel.scrollRectToVisible(rect);
					break;
				case KeyEvent.VK_LEFT:
					rect.x -= d;
					trackerPanel.scrollRectToVisible(rect);
					break;
				case KeyEvent.VK_A:
					if (Tracker.enableAutofill && !PointMass.isAutoKeyDown) {
						PointMass.isAutoKeyDown = true;
						if (trackerPanel.getSelectedTrack() != null
								&& trackerPanel.getSelectedTrack() instanceof PointMass) {
							PointMass m = (PointMass) trackerPanel.getSelectedTrack();
							m.setAutoFill(!m.isAutofill);
							trackerPanel.getSelectedTrack().repaint(trackerPanel);
						}
					}
					break;
				}
				if (z.isSelected()) {
					trackerPanel.setCursor(e.isAltDown() ? Tracker.getZoomOutCursor() : Tracker.getZoomInCursor());
				}
			}

			@Override
			public void keyReleased(final KeyEvent e) {
				final JButton z = trackerPanel.getTFrame().getToolBar(trackerPanel).zoomButton;
				if (e.getKeyCode() == KeyEvent.VK_Z) {
					z.setSelected(false);
					trackerPanel.setCursor(Cursor.getDefaultCursor());
				}
				if (e.getKeyCode() == KeyEvent.VK_A) {
					PointMass.isAutoKeyDown = false;
				}
				if (z.isSelected()) {
					Runnable runner = new Runnable() {
						@Override
						public synchronized void run() {
							trackerPanel
									.setCursor(e.isAltDown() ? Tracker.getZoomOutCursor() : Tracker.getZoomInCursor());
						}
					};
					SwingUtilities.invokeLater(runner);
				}
			}
		};
		// add mouse and key listeners
		trackerPanel.addMouseListener(mouseAdapter);
		trackerPanel.addMouseWheelListener(mouseAdapter);
		trackerPanel.addKeyListener(keyAdapter);

	}

	/**
	 * Gets the popup menu when right-clicked.
	 *
	 * @return the popup menu
	 */
	JPopupMenu getPopupMenu() {
		OSPLog.debug("MainTView.getPopupMenu " + Tracker.allowMenuRefresh);
		if (!Tracker.allowMenuRefresh)
			return null;

		if (Tracker.isZoomInCursor(trackerPanel.getCursor()) || Tracker.isZoomOutCursor(trackerPanel.getCursor())) {
			return null;
		}
		return trackerPanel.updateMainPopup();
	}

	/**
	 * Sets the position of the zoom center point in image coordinates.
	 *
	 * @param x
	 * @param y
	 */
	public void setZoomCenter(int x, int y) {
		zoomCenter.setLocation(x, y);
	}

	/**
	 * Scrolls to the zoom center after changing the magnification.
	 *
	 * @param size     the current size of the TrackerPanel
	 * @param prevSize the previous size of the TrackerPanel
	 * @param panelLoc the current location of the TrackerPanel relative to the
	 *                 scrollPane viewport.
	 */
	public void scrollToZoomCenter(Dimension size, Dimension prevSize, Point panelLoc) {
		if (zoomCenter.x == 0 && zoomCenter.y == 0)
			return;
		double xRatio = size.getWidth() / prevSize.getWidth();
		double yRatio = size.getHeight() / prevSize.getHeight();
		final Rectangle rect = scrollPane.getViewport().getViewRect();
		if (prevSize.width < rect.width || prevSize.height < rect.height) {
			rect.setLocation((int) (-xRatio * panelLoc.x), (int) (-yRatio * panelLoc.y));
		}
		double x = rect.x + (xRatio - 1) * zoomCenter.getX();
		double y = rect.y + (yRatio - 1) * zoomCenter.getY();
		rect.setLocation((int) x, (int) y);
		scrollRect.setBounds(rect);
	    System.out.println("prev size "+prevSize);
	    System.out.println("size "+size);
	    System.out.println("initial rect "+rect);
	    System.out.println("zoomcenter "+zoomCenter);
	    System.out.println("zoom by "+xRatio+" to "+trackerPanel.getMagnification());
	    System.out.println("zoom rect "+rect);
	    System.out.println("zoom viewport "+ scrollPane.getViewport().getViewRect());
		trackerPanel.scrollRectToVisible(scrollRect);
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				Rectangle rect = scrollPane.getViewport().getViewRect();
			    System.out.println("zoom rect1 "+rect);
			    System.out.println("zoom scrollRect "+ scrollRect);
		
				if (!rect.equals(scrollRect)) {
					trackerPanel.scrollRectToVisible(scrollRect);
				}
				trackerPanel.eraseAll();
				TFrame.repaintT(trackerPanel);
			}
		};
		SwingUtilities.invokeLater(runner);
	}

	/**
	 * Gets the toolbar containing the player.
	 *
	 * @return the player toolbar
	 */
	public JToolBar getPlayerBar() {
		return playerBar;
	}

	/**
	 * Refreshes this view.
	 */
	@Override
	public void refresh() {
		init();
	}

	/**
	 * Initializes this view
	 */
	@Override
	public void init() {
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); // $NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); // $NON-NLS-1$
		// add this listener to tracks
		for (TTrack track : trackerPanel.getTracks()) {
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
			track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
		}
	}

	/**
	 * Cleans up this view
	 */
	@Override
	public void cleanup() {
		// remove this listener from tracker panel
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); // $NON-NLS-1$
		// remove this listener from all tracks
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); //$NON-NLS-1$
		}
	}

	/**
	 * Disposes of the view
	 */
	@Override
	public void dispose() {
		cleanup();
		// dispose of floating player, if any
		// note main view not finalized when player is floating
		Container frame = playerBar.getTopLevelAncestor();
		if (frame instanceof JDialog) {
			frame.removeAll();
			((JDialog) frame).dispose();
		}
		playerBar.removeAll();
		playerBar = null;

		// DB! maybe don't need below here
		// remove mouse and key listeners
		trackerPanel.removeMouseListener(mouseAdapter);
		trackerPanel.removeMouseWheelListener(mouseAdapter);
		trackerPanel.removeKeyListener(keyAdapter);

		scrollPane.setViewportView(null);
		scrollPane = null;
		removeAll();
		trackerPanel = null;
	}

	@Override
	public void finalize() {
		OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

	/**
	 * Gets the TrackerPanel containing the track data
	 *
	 * @return the tracker panel containing the data to be viewed
	 */
	@Override
	public TrackerPanel getTrackerPanel() {
		return trackerPanel;
	}

	/**
	 * Gets the name of the view
	 *
	 * @return the name of the view
	 */
	@Override
	public String getViewName() {
		return TrackerRes.getString("TFrame.View.Video"); //$NON-NLS-1$
	}

	/**
	 * Gets the icon for this view
	 *
	 * @return the icon for the view
	 */
	@Override
	public Icon getViewIcon() {
		return Tracker.getResourceIcon("video_on.gif", true); //$NON-NLS-1$
	}

	/**
	 * Gets the toolbar components for this view
	 *
	 * @return an ArrayList of components to be added to a toolbar
	 */
	@Override
	public ArrayList<Component> getToolBarComponents() {
		return new ArrayList<Component>();
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
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			refresh();
			break;
		case TTrack.PROPERTY_TTRACK_COLOR:
			TFrame.repaintT(this);
			break;
		}
	}

	/**
	 * Zooms in.
	 * 
	 * @param step true to zoom by a step
	 */
	public void zoomIn(boolean step) {
		DrawingPanel.ZoomBox zoomBox = trackerPanel.getZoomBox();
		double m1 = trackerPanel.getMagnification(); // initial magnification
		double m2 = TrackerPanel.ZOOM_STEP * m1;
		if (step) {
			// zoom by small factor, but set m2 to nearest defined zoom level if close
			double dm = Math.sqrt(TrackerPanel.ZOOM_STEP);
			for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i] < m2 * dm && TrackerPanel.ZOOM_LEVELS[i] > m2 / dm) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
		} else if (!zoomBox.isDragged()) {
			// zoom in to defined zoom levels
			for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i] >= m2 && TrackerPanel.ZOOM_LEVELS[i] < m2 * 2) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
			if (m2 > TrackerPanel.ZOOM_LEVELS[TrackerPanel.ZOOM_LEVELS.length - 1]) {
				m2 = TrackerPanel.MAX_ZOOM;
			}
		} else { // zoom to box
					// get pre-zoom viewport (v) and zoom (z) rectangles
			Rectangle vRect = scrollPane.getViewport().getViewRect();
			Rectangle zRect = zoomBox.reportZoom();
			// get trackerPanel (t) rectangle
			Dimension tDim = trackerPanel.getPreferredSize();
			Point p1 = new TPoint(0, 0).getScreenPosition(trackerPanel);
			if (tDim.width == 1 && tDim.height == 1) { // zoomed to fit
				double w = trackerPanel.getImageWidth();
				double h = trackerPanel.getImageHeight();
				Point p2 = new TPoint(w, h).getScreenPosition(trackerPanel);
				tDim.width = p2.x - p1.x;
				tDim.height = p2.y - p1.y;
			}
			Rectangle tRect = new Rectangle(p1.x, p1.y, tDim.width, tDim.height);
			if (1.0 * vRect.width / tDim.width < 1) { // trackerPanel x is outside view
				tRect.x = -vRect.x;
			}
			if (1.0 * vRect.height / tDim.height < 1) { // trackerPanel y is outside view
				tRect.y = -vRect.y;
			}
			zRect = zRect.intersection(tRect);

			// determine zoom factor and new magnification
			double fX = 1.0 * vRect.width / zRect.width;
			double fY = 1.0 * vRect.height / zRect.height;
			double xyRatio = fX / fY;
			double factor = xyRatio < 1 ? fX : fY;
			m2 = m1 * factor;
			double dm = 1.011; // set m2 to defined zoom level if within 1%
			for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i] < m2 * dm && TrackerPanel.ZOOM_LEVELS[i] > m2 / dm) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					factor = m2 / m1;
					break;
				}
			}

			// adjust zoom rect & set zoom center if trackerPanel > viewRect
			if (factor * tDim.width > vRect.width || factor * tDim.height > vRect.height) {
				// adjust zoom rect dimensions
				if (xyRatio < 1) { // short/wide-->increase height and move up
					zRect.height = (int) (zRect.height / xyRatio);
					zRect.y -= (int) (0.5 * zRect.height * (1 - xyRatio));
					zRect.y = Math.max(zRect.y, tRect.y);
					zRect.y = Math.min(zRect.y, tRect.y + tRect.height - zRect.height);
				} else { // tall/narrow-->increase width and move left
					zRect.width = (int) (zRect.width * xyRatio);
					zRect.x -= (int) (0.5 * zRect.width * (1 - 1 / xyRatio));
					zRect.x = Math.max(zRect.x, tRect.x);
					zRect.x = Math.min(zRect.x, tRect.x + tRect.width - zRect.width);
				}

				// set location of zoom center
				boolean small = m1 * tDim.width < vRect.width && m1 * tDim.height < vRect.height;
				double d = small ? 0 : m1 * p1.x / (m2 - m1);
				double x = m2 * zRect.x / (m2 - m1) + d;
				d = small ? 0 : m1 * p1.y / (m2 - m1);
				double y = m2 * zRect.y / (m2 - m1) + d;
				zoomCenter.setLocation(x, y);
			}
		}
		trackerPanel.setMagnification(m2);
	}

	/**
	 * Zooms out.
	 * 
	 * @param step true to zoom by a step
	 */
	public void zoomOut(boolean step) {
		double m1 = trackerPanel.getMagnification(); // initial magnification
		double m2 = m1 / TrackerPanel.ZOOM_STEP;
		if (step) {
			double dm = Math.sqrt(TrackerPanel.ZOOM_STEP);
			for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i] < m2 * dm && TrackerPanel.ZOOM_LEVELS[i] > m2 / dm) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
		} else {
			// zoom out to defined zoom levels
			for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i] <= m2 && TrackerPanel.ZOOM_LEVELS[i] > m2 / 2) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
			if (m2 < TrackerPanel.ZOOM_LEVELS[0]) {
				m2 = TrackerPanel.MIN_ZOOM;
			}
		}
		trackerPanel.setMagnification(m2);
	}

	@Override
	public int getViewType() {
		return TView.VIEW_MAIN;
	}
	
	@Override
	public void refreshPopup(JPopupMenu popup) {
		// does nothing
	}


}
