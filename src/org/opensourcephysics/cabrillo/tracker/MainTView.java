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

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is the main video view for Tracker. It puts the tracker panel in a zoomable
 * scrollpane and puts the player in a detachable toolbar.
 *
 * @author Douglas Brown
 */
public class MainTView extends JPanel implements TView {
	
  // instance fields
  private TrackerPanel trackerPanel;
  JScrollPane scrollPane;
  Rectangle scrollRect = new Rectangle();
  private Point zoomCenter = new Point();
  private JToolBar playerBar;

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
    	public void componentResized(ComponentEvent e) {
    		TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
    		toolbar.refreshZoomButton();
    		trackerPanel.eraseAll();
    		trackerPanel.repaint();
    	}
    });
  	SwingUtilities.replaceUIActionMap(scrollPane, null);
    add(scrollPane, BorderLayout.CENTER);
    // add player to the playerBar
    playerBar = new JToolBar();
    add(playerBar, BorderLayout.SOUTH);
    trackerPanel.getPlayer().setBorder(null);
    trackerPanel.setPlayerVisible(false);
    playerBar.add(trackerPanel.getPlayer());
    // add trackerPanel to scrollPane
    scrollPane.setViewportView(trackerPanel);
    trackerPanel.setScrollPane(scrollPane);
    
//    trackerPanel.addOptionController();
    // add mouse listener for zoom
    trackerPanel.addMouseListener(new MouseAdapter() {
    	public void mousePressed(MouseEvent e) {
    		zoomCenter.setLocation(e.getPoint());
    	}
      public void mouseReleased(MouseEvent e) {
      	// handle zoom actions
      	if (trackerPanel.getCursor() == Tracker.zoomOutCursor) 
      		zoomOut(false);  
        else if (trackerPanel.getCursor() == Tracker.zoomInCursor) 
        	zoomIn(false);   
      }
    });
    trackerPanel.addMouseWheelListener(new MouseWheelListener() {
    	public void mouseWheelMoved(MouseWheelEvent e) {
    		zoomCenter.setLocation(e.getPoint());
        if (e.getWheelRotation() > 0) {
        	zoomOut(true);  // zoom by a step
        }
        else {
        	zoomIn(true);  // zoom by a step
        }
    	}
    });
    trackerPanel.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	JButton z = trackerPanel.getTFrame().getToolBar(trackerPanel).zoomButton;
      	int d = trackerPanel.getSelectedPoint() == null? 10: 0;
        Rectangle rect = scrollPane.getViewport().getViewRect();
      	switch (e.getKeyCode()) {
	    		case KeyEvent.VK_Z:
	    			if (!e.isControlDown()) z.setSelected(true);
	    			break;
	    		case KeyEvent.VK_ALT:
	    			break;
      		case KeyEvent.VK_PAGE_UP:
      			if (!trackerPanel.getPlayer().isEnabled()) return;
      			if (e.isShiftDown()) {
      				int n = trackerPanel.getPlayer().getStepNumber()-5;
      				trackerPanel.getPlayer().setStepNumber(n);
      			}
      			else trackerPanel.getPlayer().back();
      			break;
      		case KeyEvent.VK_PAGE_DOWN:
      			if (!trackerPanel.getPlayer().isEnabled()) return;
      			if (e.isShiftDown()) {
      				int n = trackerPanel.getPlayer().getStepNumber()+5;
      				trackerPanel.getPlayer().setStepNumber(n);
      			}
      			else trackerPanel.getPlayer().step();
      			break;
      		case KeyEvent.VK_HOME:      			
      			if (!trackerPanel.getPlayer().isEnabled()) return;
      			trackerPanel.getPlayer().setStepNumber(0);
      			break;
      		case KeyEvent.VK_END:
      			if (!trackerPanel.getPlayer().isEnabled()) return;
      			VideoClip clip = trackerPanel.getPlayer().getVideoClip();
      			trackerPanel.getPlayer().setStepNumber(clip.getStepCount()-1);
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
      	}
        if(z.isSelected()) { 
      		trackerPanel.setCursor(e.isAltDown()? 
      				Tracker.getZoomOutCursor(): 
      				Tracker.getZoomInCursor());
        }
      }
      public void keyReleased(final KeyEvent e) {
      	final JButton z = trackerPanel.getTFrame().getToolBar(trackerPanel).zoomButton;
        if(e.getKeyCode()==KeyEvent.VK_Z) {
        	z.setSelected(false);
      		trackerPanel.setCursor(Cursor.getDefaultCursor()); 
        }
        if(z.isSelected()) {
	        Runnable runner = new Runnable() {
	          public synchronized void run() {
	        		trackerPanel.setCursor(e.isAltDown()? 
	        				Tracker.getZoomOutCursor(): 
	        				Tracker.getZoomInCursor());
	          }
	        };
	        SwingUtilities.invokeLater(runner);
        }
      }
    });
  }
  
  /**
   * Gets the popup menu when right-clicked.
   *
   * @return the popup menu
   */
  JPopupMenu getPopupMenu() {
  	if (trackerPanel.getCursor() == Tracker.zoomInCursor
  			|| trackerPanel.getCursor() == Tracker.zoomOutCursor) {
  		return null;
  	}
  	JPopupMenu popup = trackerPanel.popup;
    // see if a track has been clicked
    boolean trackClicked = false;
    Interactive iad = trackerPanel.getInteractive();
    if (iad instanceof TPoint) {
      TPoint p = (TPoint)iad;
      TTrack track = null;
      Step step = null;
      Iterator<TTrack> it = trackerPanel.getTracks().iterator();
      while(it.hasNext()) {
        track = it.next();
        step = track.getStep(p, trackerPanel);
        if (step != null) break;
      }
      if (step != null) { // found clicked track
      	trackClicked = true;
        Step prev = trackerPanel.selectedStep;
        trackerPanel.selectedStep = step;
        popup = track.getMenu(trackerPanel).getPopupMenu();
        trackerPanel.selectedStep = prev;
      }
    }
    if (!trackClicked) { // video or non-track TPoint was clicked
      popup.removeAll();
      final Video vid = trackerPanel.getVideo();
      // add zoom menus
      JMenuItem item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ZoomIn")); //$NON-NLS-1$
      popup.add(item);
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	zoomIn(false);
        }
      });
      item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ZoomOut")); //$NON-NLS-1$
      popup.add(item);
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	zoomOut(false);
        }
      });
      item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ZoomToFit")); //$NON-NLS-1$
      popup.add(item);
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	trackerPanel.setMagnification(-1);
      		TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
      		toolbar.refreshZoomButton();
        }
      });
      
      // selection items
    	DrawingPanel.ZoomBox zoomBox = trackerPanel.getZoomBox();
    	if (zoomBox.isDragged() && isStepsInZoomBox()) {
	      popup.addSeparator();
	      item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.Select"));  //$NON-NLS-1$
	      item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	handleStepsInZoomBox(true);
	        }
	      });
	      popup.add(item);     
	      item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.Deselect"));  //$NON-NLS-1$ 
	      item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	handleStepsInZoomBox(false);
	        }
	      });
	      popup.add(item); 
    	}
    		
      // clip setting item
      if (trackerPanel.isEnabled("button.clipSettings")) {//$NON-NLS-1$
	      if (popup.getComponentCount() > 0)
	        popup.addSeparator();
	      item = new JMenuItem(MediaRes.getString("ClipInspector.Title")+"...");  //$NON-NLS-1$ //$NON-NLS-2$ 
	      item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
	        	ClipControl clipControl = trackerPanel.getPlayer().getClipControl();
	          TFrame frame = trackerPanel.getTFrame();
	          ClipInspector inspector = clip.getClipInspector(clipControl, frame);
	          if(inspector.isVisible()) {
	            return;
	          }
	          FontSizer.setFonts(inspector, FontSizer.getLevel());	          
	          inspector.pack();
	          Point p0 = new Frame().getLocation();
	          Point loc = inspector.getLocation();
	          if((loc.x==p0.x)&&(loc.y==p0.y)) {
	            // center inspector on the main view
	          	Rectangle rect = trackerPanel.getVisibleRect();
	            Point p = frame.getMainView(trackerPanel).scrollPane.getLocationOnScreen();
	            int x = p.x+(rect.width-inspector.getBounds().width)/2;
	            int y = p.y+(rect.height-inspector.getBounds().height)/2;
	            inspector.setLocation(x, y);
	          }
	          inspector.initialize();
	          inspector.setVisible(true);
	          refresh();
	        }
	      });
	      popup.add(item);
      }
      if (trackerPanel.isEnabled("edit.copyImage")) { //$NON-NLS-1$
        popup.addSeparator();
        // copy image item
        Action copyImageAction = new AbstractAction(TrackerRes.getString("TMenuBar.Menu.CopyImage")) { //$NON-NLS-1$
          public void actionPerformed(ActionEvent e) {
          	BufferedImage image = new TrackerIO.ComponentImage(trackerPanel).getImage();
          	DrawingPanel.ZoomBox zoomBox = trackerPanel.getZoomBox();
          	if (zoomBox.isDragged()) {
        	  	Rectangle zRect = zoomBox.reportZoom();
        	  	BufferedImage image2 = new BufferedImage(zRect.width, zRect.height, image.getType());
        	  	Graphics2D g = image2.createGraphics();
        	  	g.drawImage(image, -zRect.x, -zRect.y, null);
        	  	TrackerIO.copyImage(image2);
          	}
          	else TrackerIO.copyImage(image);
          }
        };
        JMenuItem copyImageItem = new JMenuItem(copyImageAction);
        popup.add(copyImageItem);
        // snapshot item
        Action snapshotAction = new AbstractAction(
        		DisplayRes.getString("DisplayPanel.Snapshot_menu_item")) { //$NON-NLS-1$
          public void actionPerformed(ActionEvent e) {
          	trackerPanel.snapshot();
          }
        };
        JMenuItem snapshotItem = new JMenuItem(snapshotAction);
        popup.add(snapshotItem);
      }
      
      TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
      // video filters menu
      if (vid != null && trackerPanel.isEnabled("video.filters")) { //$NON-NLS-1$
      	JMenu filtersMenu = menubar.filtersMenu;
        if (filtersMenu.getItemCount() > 0) {
          popup.addSeparator();
          popup.add(filtersMenu);
        }
      }
      JMenu tracksMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Tracks")); //$NON-NLS-1$
    	if (menubar.createMenu.getItemCount() == 0)
        for (Component c: menubar.newTrackItems) {
        	menubar.createMenu.add(c);    
      	}
      if (menubar.createMenu.getItemCount() > 0) 
      	tracksMenu.add(menubar.createMenu);
      if (menubar.cloneMenu.getItemCount() > 0
      		&& trackerPanel.isEnabled("new.clone")) //$NON-NLS-1$
      	tracksMenu.add(menubar.cloneMenu);
      // get list of tracks for track menus
      TTrack track = null;
      CoordAxes axes = trackerPanel.getAxes();
      ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
      // add track items
      if (!tracks.isEmpty()) {
        if (tracksMenu.getItemCount() > 0)
          tracksMenu.addSeparator();
        Iterator<TTrack> it = tracks.iterator();
        while (it.hasNext()) {
          tracksMenu.add(menubar.getMenu(it.next()));
        }
      }
      // add axes and calibration tool items
      if (trackerPanel.isEnabled("button.axes") //$NON-NLS-1$
      		|| trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
      		|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
      		|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
      		|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
        if (tracksMenu.getItemCount() > 0)
          tracksMenu.addSeparator();
        if (axes != null && trackerPanel.isEnabled("button.axes")) { //$NON-NLS-1$
          track = axes;
          tracksMenu.add(menubar.getMenu(track));
        }

        if (!trackerPanel.calibrationTools.isEmpty()) {
        	for (TTrack next: trackerPanel.getTracks()) {
        		if (trackerPanel.calibrationTools.contains(next)) {
        			if (next instanceof TapeMeasure) {
        				TapeMeasure tape = (TapeMeasure)next;
        				if (tape.isStickMode()
        						&& !trackerPanel.isEnabled("calibration.stick")) //$NON-NLS-1$
        					continue;
        				if (!tape.isStickMode()
        						&& !trackerPanel.isEnabled("calibration.tape")) //$NON-NLS-1$
        					continue;
        			}
        			if (next instanceof Calibration
        					&& !trackerPanel.isEnabled("calibration.points")) //$NON-NLS-1$
        				continue;
        			if (next instanceof OffsetOrigin
        					&& !trackerPanel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
        				continue;
        				tracksMenu.add(menubar.getMenu(next));
        		}
        	}
        }
      }
      if (tracksMenu.getItemCount() > 0) {
        popup.addSeparator();
        popup.add(tracksMenu);
      }
      // video properties item
      Action vidPropsAction = TActions.getAction("aboutVideo", trackerPanel); //$NON-NLS-1$
      JMenuItem propertiesItem = new JMenuItem(vidPropsAction);
      popup.addSeparator();
    	propertiesItem.setText(TrackerRes.getString("TActions.AboutVideo")); //$NON-NLS-1$
      popup.add(propertiesItem);
      
      // print menu item
      if (trackerPanel.isEnabled("file.print")) { //$NON-NLS-1$
        if (popup.getComponentCount() > 0)
          popup.addSeparator();
        Action printAction = TActions.getAction("print", trackerPanel); //$NON-NLS-1$
        popup.add(printAction);
      }
      // add help item
      if (popup.getComponentCount() > 0)
        popup.addSeparator();
      JMenuItem helpItem = new JMenuItem(TrackerRes.getString("Tracker.Popup.MenuItem.Help")); //$NON-NLS-1$
      helpItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TFrame frame = trackerPanel.getTFrame();
          if (frame != null) {
  	        frame.showHelp("GUI", 0); //$NON-NLS-1$
          }
        }
      });
      popup.add(helpItem);
    }
    FontSizer.setFonts(popup, FontSizer.getLevel());
  	return popup;
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
   * @param size the current size of the TrackerPanel
   * @param prevSize the previous size of the TrackerPanel
   * @param panelLoc the current location of the TrackerPanel relative to the scrollPane viewport.
   */
  public void scrollToZoomCenter(Dimension size, Dimension prevSize, Point panelLoc) {
    double xRatio = size.getWidth() / prevSize.getWidth();
    double yRatio = size.getHeight() / prevSize.getHeight();
    final Rectangle rect = scrollPane.getViewport().getViewRect();
	  if (prevSize.width<rect.width || prevSize.height<rect.height) {
			rect.setLocation((int)(-xRatio*panelLoc.x), (int)(-yRatio*panelLoc.y));
		}
//    System.out.println("prev size "+prevSize);
//    System.out.println("size "+size);
//    System.out.println("initial rect "+rect);
//    System.out.println("zoomcenter "+zoomCenter);
//    System.out.println("zoom by "+xRatio+" to "+trackerPanel.getMagnification());
    double x = rect.x + (xRatio-1)*zoomCenter.getX();
    double y = rect.y + (yRatio-1)*zoomCenter.getY();
    rect.setLocation((int)x, (int)y);
		scrollRect.setBounds(rect);
		trackerPanel.scrollRectToVisible(scrollRect);
    Runnable runner = new Runnable() {
			public void run() {
				Rectangle rect = scrollPane.getViewport().getViewRect();
				if (!rect.equals(scrollRect)) {
					trackerPanel.scrollRectToVisible(scrollRect);
				}
		    trackerPanel.eraseAll();
		    trackerPanel.repaint();    
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
  public void refresh() {
    init();
  }

  /**
   * Initializes this view
   */
  public void init() {
    cleanup(); // removes previous listeners
    // add this listener to tracker panel
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    // add this listener to tracks
    for (TTrack track: trackerPanel.getTracks()){
      track.addPropertyChangeListener("color", this); //$NON-NLS-1$
    }
  }

  /**
   * Cleans up this view
   */
  public void cleanup() {
    // remove this listener from tracker panel
    trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
    // remove this listener from tracks
    for (TTrack track: trackerPanel.getTracks()){
      track.removePropertyChangeListener("color", this); //$NON-NLS-1$
    }
  }

  /**
   * Gets the TrackerPanel containing the track data
   *
   * @return the tracker panel containing the data to be viewed
   */
  public TrackerPanel getTrackerPanel() {
    return trackerPanel;
  }

  /**
   * Gets the name of the view
   *
   * @return the name of the view
   */
  public String getViewName() {
    return TrackerRes.getString("TFrame.View.Video"); //$NON-NLS-1$
  }

  /**
   * Gets the icon for this view
   *
   * @return the icon for the view
   */
  public Icon getViewIcon() {
    return new ImageIcon(
        Tracker.class.getResource("resources/images/video_on.gif")); //$NON-NLS-1$
  }

  /**
   * Gets the toolbar components for this view
   *
   * @return an ArrayList of components to be added to a toolbar
   */
  public ArrayList<Component> getToolBarComponents() {
    return new ArrayList<Component>();
  }

  /**
   * Returns true if this view is in a custom state.
   *
   * @return false
   */
  public boolean isCustomState() {
  	return false;
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("track")) { // track has been added or removed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("color")) { // track color has changed //$NON-NLS-1$
      repaint();
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
		double m2 = TrackerPanel.ZOOM_STEP*m1;
  	if (step) {
  		// zoom by small factor, but set m2 to nearest defined zoom level if close			
  		double dm = Math.sqrt(TrackerPanel.ZOOM_STEP);
			for (int i = 0; i< TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i]<m2*dm && TrackerPanel.ZOOM_LEVELS[i]>m2/dm) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
  	}
  	else if (!zoomBox.isDragged()) {
  		// zoom in to defined zoom levels			
			for (int i = 0; i< TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i]>=m2 && TrackerPanel.ZOOM_LEVELS[i]<m2*2) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
			if (m2>TrackerPanel.ZOOM_LEVELS[TrackerPanel.ZOOM_LEVELS.length-1]) {
				m2 = TrackerPanel.MAX_ZOOM;
			}
  	}
  	else { // zoom to box
  		// get pre-zoom viewport (v) and zoom (z) rectangles
      Rectangle vRect = scrollPane.getViewport().getViewRect();
	  	Rectangle zRect = zoomBox.reportZoom();
    	// get trackerPanel (t) rectangle
      Dimension tDim = trackerPanel.getPreferredSize();
    	Point p1 = new TPoint(0, 0).getScreenPosition(trackerPanel);
      if (tDim.width==1 && tDim.height==1) { // zoomed to fit
      	double w = trackerPanel.getImageWidth();
      	double h = trackerPanel.getImageHeight();
      	Point p2 = new TPoint(w, h).getScreenPosition(trackerPanel);
      	tDim.width = p2.x-p1.x;
      	tDim.height =  p2.y-p1.y;
      }
  		Rectangle tRect = new Rectangle(p1.x, p1.y, tDim.width, tDim.height);
      if (1.0*vRect.width/tDim.width<1) { // trackerPanel x is outside view
      	tRect.x = -vRect.x;
      }
      if (1.0*vRect.height/tDim.height<1) { // trackerPanel y is outside view
      	tRect.y = -vRect.y;
      }
  		zRect = zRect.intersection(tRect);
	  	
    	// determine zoom factor and new magnification
	  	double fX = 1.0*vRect.width/zRect.width;
	  	double fY = 1.0*vRect.height/zRect.height;
	  	double xyRatio = fX/fY;
	  	double factor = xyRatio<1? fX: fY;
      m2 = m1*factor;
  		double dm = 1.011; // set m2 to defined zoom level if within 1%
			for (int i = 0; i< TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i]<m2*dm && TrackerPanel.ZOOM_LEVELS[i]>m2/dm) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					factor = m2/m1;
					break;
				}
			}
	  	
	  	// adjust zoom rect & set zoom center if trackerPanel > viewRect
    	if (factor*tDim.width>vRect.width || factor*tDim.height>vRect.height) {
    		// adjust zoom rect dimensions
		  	if (xyRatio<1) { // short/wide-->increase height and move up
		  		zRect.height = (int)(zRect.height/xyRatio);
		  		zRect.y -= (int)(0.5*zRect.height*(1-xyRatio));
		  		zRect.y = Math.max(zRect.y, tRect.y);
		  		zRect.y = Math.min(zRect.y, tRect.y+tRect.height-zRect.height);
		  	}
		  	else { // tall/narrow-->increase width and move left
		  		zRect.width = (int)(zRect.width*xyRatio);
		  		zRect.x -= (int)(0.5*zRect.width*(1-1/xyRatio));
		  		zRect.x = Math.max(zRect.x, tRect.x);
		  		zRect.x = Math.min(zRect.x, tRect.x+tRect.width-zRect.width);
		  	}
	    	
	      // set location of zoom center
	    	boolean small = m1*tDim.width<vRect.width && m1*tDim.height<vRect.height;
	    	double d = small? 0: m1*p1.x/(m2-m1);
	      double x = m2*zRect.x/(m2-m1)+d;
	      d = small? 0: m1*p1.y/(m2-m1);
	      double y = m2*zRect.y/(m2-m1)+d;
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
		double m2 = m1/TrackerPanel.ZOOM_STEP;
		if (step) {
			double dm = Math.sqrt(TrackerPanel.ZOOM_STEP);
			for (int i = 0; i< TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i]<m2*dm && TrackerPanel.ZOOM_LEVELS[i]>m2/dm) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
		}
		else {
  		// zoom out to defined zoom levels			
			for (int i = 0; i< TrackerPanel.ZOOM_LEVELS.length; i++) {
				if (TrackerPanel.ZOOM_LEVELS[i]<=m2 && TrackerPanel.ZOOM_LEVELS[i]>m2/2) {
					m2 = TrackerPanel.ZOOM_LEVELS[i];
					break;
				}
			}
			if (m2<TrackerPanel.ZOOM_LEVELS[0]) {
				m2 = TrackerPanel.MIN_ZOOM;
			}
		}
		trackerPanel.setMagnification(m2);  
  }
  
  protected boolean isStepsInZoomBox() {
  	// look for a step in the zoom box
  	DrawingPanel.ZoomBox zoomBox = trackerPanel.getZoomBox();
  	Rectangle zRect = zoomBox.reportZoom();
  	ArrayList<TTrack> tracks = trackerPanel.getTracks();
  	for (TTrack track: tracks) {
  		// search only visible PointMass tracks for now
  		if (!track.isVisible() || track.getClass()!=PointMass.class) continue;
  		if (!((PointMass)track).isPositionVisible(trackerPanel)) continue;
  		for (Step step: track.getSteps()) {
  			if (step==null) continue;
  			// need look only at points[0] for PositionStep
  	    TPoint p = step.getPoints()[0];
	      if (p==null || Double.isNaN(p.getX())) continue;
	      if (zRect.contains(p.getScreenPosition(trackerPanel))) {
	      	return true;
	      }
  		}
  	}
  	return false;
  }
  
  protected void handleStepsInZoomBox(boolean add) {
  	// determine what steps are in selection (zoom) box
  	DrawingPanel.ZoomBox zoomBox = trackerPanel.getZoomBox();
  	Rectangle zRect = zoomBox.reportZoom();
  	ArrayList<TTrack> tracks = trackerPanel.getTracks();
  	for (TTrack track: tracks) {
  		// search only visible PointMass tracks for now
  		if (!track.isVisible() || track.getClass()!=PointMass.class) continue;
  		if (!((PointMass)track).isPositionVisible(trackerPanel)) continue;
  		for (Step step: track.getSteps()) {
  			if (step==null) continue;
  			// need look only at points[0] for PositionStep
  	    TPoint p = step.getPoints()[0];
	      if (p==null || Double.isNaN(p.getX())) continue;
	      if (zRect.contains(p.getScreenPosition(trackerPanel))) {
	      	if (add) {
	      		trackerPanel.selectedSteps.add(step);
	      	}
	      	else {
	      		trackerPanel.selectedSteps.remove(step);
	      	}
	      	step.erase();
	      }
  		}
  	}
  	if (add && trackerPanel.selectedSteps.size()==1) {
  		Step step = trackerPanel.selectedSteps.toArray(new Step[1])[0];
  		trackerPanel.setSelectedPoint(step.points[0]);
  	}
  	else if (trackerPanel.selectedSteps.size()>1) {
  		trackerPanel.setSelectedPoint(null);
  	}
  }

}
