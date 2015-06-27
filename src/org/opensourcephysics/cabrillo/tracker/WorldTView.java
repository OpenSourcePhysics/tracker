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

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.*;
import org.opensourcephysics.controls.XMLControlElement;

import java.rmi.RemoteException;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This is a TView of a TrackerPanel drawn in world space.
 *
 * @author Douglas Brown
 */
public class WorldTView extends TrackerPanel implements TView {

  // instance fields
  protected TrackerPanel trackerPanel;
  protected Icon icon;
  protected JMenuItem copyImageItem;
  protected JMenuItem printItem;
  protected JMenuItem helpItem;
  protected JButton worldViewButton;
  protected ArrayList<Component> components = new ArrayList<Component>();
  protected OverlayTool overlayTool = new OverlayTool();

  /**
   * Constructs a WorldTView of the specified TrackerPanel
   *
   * @param panel the tracker panel to be viewed
   */
  public WorldTView(TrackerPanel panel) {
    super(null);
    trackerPanel = panel;
    init();
    setPlayerVisible(false);
    setDrawingInImageSpace(false);
    setPreferredSize(new Dimension(240, 180));
    setShowCoordinates(false);
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/axes.gif")); //$NON-NLS-1$
    // world view button
    worldViewButton = new TButton();
    components.add(worldViewButton);
    // copy image item
    Action copyImageAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        new TrackerIO.ComponentImage(WorldTView.this).copyToClipboard();
      }
    };
    copyImageItem = new JMenuItem(copyImageAction);
    // print menu item
    Action printAction = new AbstractAction(
    		TrackerRes.getString("TActions.Action.Print"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        new TrackerIO.ComponentImage(WorldTView.this).print();
      }
    };
    printItem = new JMenuItem(printAction);
    // help item
    helpItem = new JMenuItem();
    helpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
	        frame.showHelp("world", 0); //$NON-NLS-1$
        }
      }
    });
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (OSPRuntime.isPopupTrigger(e)) {
      		popup.removeAll();
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
          popup.show(WorldTView.this, e.getX(), e.getY());
        }
      }
    });
    this.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        refresh();
      }
    });
  }

  /**
   * Refreshes all tracks
   */
  public void refresh() {
    // axes & tape items
  	CoordAxes axes = trackerPanel.getAxes();
    if (axes!=null) {
    	axes.removePropertyChangeListener("visible", this); //$NON-NLS-1$
    	axes.addPropertyChangeListener("visible", this); //$NON-NLS-1$
    }
    if (!trackerPanel.calibrationTools.isEmpty()) {
    	for (TTrack next: trackerPanel.getTracks()) {
    		if (trackerPanel.calibrationTools.contains(next)) {
    			next.removePropertyChangeListener("visible", this); //$NON-NLS-1$
    			next.addPropertyChangeListener("visible", this); //$NON-NLS-1$
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
    repaint();
  }

  /**
   * Initializes this view
   */
  public void init() {
    cleanup();
    // add this view to tracker panel listeners
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("size", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("transform", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("stepnumber", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("video", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("image", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("videoVisible", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("data", this); //$NON-NLS-1$
    // add this view to track listeners
    for (TTrack track: trackerPanel.getTracks()) {
      track.addPropertyChangeListener("color", this); //$NON-NLS-1$
    }
  }

  /**
   * Cleans up this view
   */
  public void cleanup() {
    // remove this listener from tracker panel
    trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("transform", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("stepnumber", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("image", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("videoVisible", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("data", this); //$NON-NLS-1$
    // remove this listener from tracks
    for (TTrack track: trackerPanel.getTracks()) {
      track.removePropertyChangeListener("color", this); //$NON-NLS-1$
    }
  }

  /**
   * Gets the tracker panel being viewed
   *
   * @return the tracker panel being viewed
   */
  public TrackerPanel getTrackerPanel() {
    return trackerPanel;
  }

  /**
   * Overrides TrackerPanel getSnapPoint method.
   *
   * @return the snap point
   */
  public TPoint getSnapPoint() {
    return trackerPanel.getSnapPoint();
  }

  /**
   * Overrides TrackerPanel getSelectedTrack method. Gets the selected track of
   * trackerPanel.
   *
   * @return the selected track
   */
  public TTrack getSelectedTrack() {
    return trackerPanel.getSelectedTrack();
  }

  /**
   * Sets the selected track
   *
   * @param track the track to select
   */
  public void setSelectedTrack(TTrack track) {
    trackerPanel.setSelectedTrack(track);
  }

  /**
   * Gets the name of the view
   *
   * @return the name of the view
   */
  public String getViewName() {
    return TrackerRes.getString("TFrame.View.World"); //$NON-NLS-1$
  }

  /**
   * Gets the icon for this view
   *
   * @return the icon for the view
   */
  public Icon getViewIcon() {
    return icon;
  }

  /**
   * Gets the toolbar components
   *
   * @return an ArrayList of components to be added to a toolbar
   */
  public ArrayList<Component> getToolBarComponents() {
  	worldViewButton.setText(TrackerRes.getString("WorldTView.Button.World")); //$NON-NLS-1$
    return components;
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
    if (name.equals("track")) {           // track has been added or removed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("stepnumber") ||   // stepnumber has changed //$NON-NLS-1$
             name.equals("color") ||        // track color changed //$NON-NLS-1$
             name.equals("visible") ||      // tape/axes visibility changed //$NON-NLS-1$
             name.equals("image") ||        // video image has changed //$NON-NLS-1$
             name.equals("video") ||        // video has changed //$NON-NLS-1$
             name.equals("videoVisible")) { // video visibility has changed //$NON-NLS-1$
      repaint();
    }
    else if (name.equals("transform")) {    // coords have changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("size")) {         // image size has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("data")) {         // data has changed //$NON-NLS-1$
      refresh();
    }
  }

  /**
   * Overrides DrawingPanel getDrawables method. Returns all drawables in
   * the tracker panel plus those in this world view.
   *
   * @return a list of Drawable objects
   */
  public ArrayList<Drawable> getDrawables() {
    // return all drawables in tracker panel plus those in this world view
    ArrayList<Drawable> list = trackerPanel.getDrawables();
    list.addAll(super.getDrawables());
    // put mat behind everything
    TMat mat = trackerPanel.getMat();
    if (mat != null && list.get(0) != mat) {
      list.remove(mat);
      list.add(0, mat);
    }
    // remove noData message if trackerPanel is not empty
    if (!trackerPanel.isEmpty) remove(noData);
    return list;
  }

  /**
   * Overrides VideoPanel getPlayer method. Returns the tracker panel's player.
   *
   * @return the video player
   */
  public VideoPlayer getPlayer() {
    // workaround to prevent null pointer exception during instantiation
    if (trackerPanel == null) return super.getPlayer();
    return trackerPanel.getPlayer();
  }

  /**
   * Overrides VideoPanel getCoords method. Returns the tracker panel's coords.
   *
   * @return the current image coordinate system
   */
  public ImageCoordSystem getCoords() {
    // workaround to prevent null pointer exception during instantiation
    if (trackerPanel == null) return super.getCoords();
    return trackerPanel.getCoords();
  }

  /**
   * Overrides TrackerPanel repaintDirtyRegion method. WorldView requires
   * a full repaint every time since it autoscales.
   */
  public void repaintDirtyRegion() {
    if (dirty != null) {
      repaint();
      dirty = null;
    }
  }

  /**
   * Overrides InteractivePanel getInteractive method.
   * 
   * @return null
   */
  public Interactive getInteractive() {
  	return null;
  }
  
  /**
   * Configures this panel. Overrides TrackerPanel method.
   */
  protected void configure() {
    // set tiny preferred size so auto zooms to very small
    setPreferredSize(new Dimension(1, 1));
    coords.addPropertyChangeListener(this);
    // remove DrawingPanel option controller
    removeOptionController();
  }
  
  /**
   * Returns true if this view is selected in it's parent TViewChooser.
   * 
   * @return true if selected
   */
  protected boolean isSelectedView() {
  	Container c = getParent();
  	while (c!=null) {
  		if (c instanceof TViewChooser) {
  			TViewChooser chooser = (TViewChooser)c;
  			if (chooser.getSelectedView() == this)
  				return true;
  		}
  		c = c.getParent();
  	}
  	return false;
  }

  /**
   * Inner tool class to receive and draw jobs from animation tools.
   */
  class OverlayTool implements Tool {

    JobManager jobManager = new JobManager(this);
    XMLControlElement control = new XMLControlElement();

    /**
     * Sends a job to this tool and specifies a tool to reply to. The job must
     * contain osp xml data for a Drawable object.
     *
     * @param job the Job
     * @param replyTo the tool to notify when the job is complete (may be null)
     * @throws RemoteException
     */
    public void send(Job job, Tool replyTo) throws RemoteException {
      // load and draw the Drawable specified in the job's xml
      Drawable drawable = null;
      Object[] array = jobManager.getObjects(job);
      if (array != null && array.length > 0) {
        // job has been sent previously, so load previous drawable (array[0])
        drawable = (Drawable)array[0];
      }
      control.readXML(job.getXML());
      if (control.failedToRead() || control.getObjectClass() == Object.class)
        return;
      boolean firstTime = (drawable == null);
      drawable = (Drawable)control.loadObject(drawable);
      if (firstTime && drawable != null) {
        addDrawable(drawable);
        jobManager.log(job, replyTo);
        jobManager.associate(job, drawable);
      }
      repaint();
    }

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
    public void saveObject(XMLControl control, Object obj) {/** empty block */}

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
      return obj;
    }
  }
}
