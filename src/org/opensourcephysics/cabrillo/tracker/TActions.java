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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

import javax.swing.*;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This creates a map of action name to action for many common tracker actions.
 *
 * @author Douglas Brown
 */
public class TActions {

  // static fields
  static Map<TrackerPanel, Map<String, AbstractAction>> actionMaps 
  		= new HashMap<TrackerPanel, Map<String, AbstractAction>>(); // maps trackerPanel to actions map
  static String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

  /**
   * Private constructor.
   */
  private TActions() {/** empty block */}

  /**
   * Gets an action for a TrackerPanel.
   * 
   * @param key the name of the action
   * @param trackerPanel the TrackerPanel
   * @return the Action
   */
  public static Action getAction(String key, final TrackerPanel trackerPanel) {
    return getActions(trackerPanel).get(key);
  }

  /**
   * Clears all actions. This forces creation of new ones using new locale.
   */
  public static void clear() {
  	actionMaps.clear();
  }
  
  /**
   * Gets the action map for a TrackerPanel.
   * 
   * @param trackerPanel the TrackerPanel
   * @return the Map
   */
  public static Map<String, AbstractAction> getActions(final TrackerPanel trackerPanel) {
    Map<String, AbstractAction> actions = actionMaps.get(trackerPanel);
    if (actions != null) return actions;
    // create new actionMap
    actions = new HashMap<String, AbstractAction>();
    actionMaps.put(trackerPanel, actions);
    // clear tracks
    final AbstractAction clearTracksAction = new AbstractAction(TrackerRes.getString("TActions.Action.ClearTracks"), null) { //$NON-NLS-1$
     public void actionPerformed(ActionEvent e) {
       // check for locked tracks and get list of xml strings for undoableEdit
     	 ArrayList<String> xml= new ArrayList<String>();
       boolean locked = false;
       ArrayList<org.opensourcephysics.display.Drawable> keepers = trackerPanel.getSystemDrawables();
       Iterator<TTrack> it = trackerPanel.getTracks().iterator();
       while(it.hasNext()) {
         TTrack track = it.next();
         if (keepers.contains(track)) continue;
         xml.add(new XMLControlElement(track).toXML());
         locked = locked || (track.isLocked() && !track.isDependent());
       }
       if (locked) {
         int i = JOptionPane.showConfirmDialog(trackerPanel,
                 TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Message"), //$NON-NLS-1$
                 TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Title"), //$NON-NLS-1$
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.WARNING_MESSAGE);
         if (i != 0) return;
       }
       // post edit and clear tracks
       Undo.postTrackClear(trackerPanel, xml);
       trackerPanel.clearTracks();
      }
    };
    actions.put("clearTracks", clearTracksAction); //$NON-NLS-1$
    // new tab
    AbstractAction newTabAction = new AbstractAction(TrackerRes.getString("TActions.Action.NewTab"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
          TrackerPanel newPanel = new TrackerPanel();
          frame.addTab(newPanel);
          frame.setSelectedTab(newPanel);
          JSplitPane pane = frame.getSplitPane(newPanel, 0);
          pane.setDividerLocation(frame.defaultRightDivider);
          frame.refresh();
        }
      }
    };
    actions.put("newTab", newTabAction); //$NON-NLS-1$
    // pastexml
    AbstractAction pasteAction = new AbstractAction(TrackerRes.getString("TActions.Action.Paste")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        if (!TrackerIO.pasteXML(trackerPanel)) {
        	// pasting XML failed, so try to paste data
        	String dataString = DataTool.paste();
        	trackerPanel.importData(dataString, null); // returns DataTrack if successful
        }
      }
    };
    actions.put("paste", pasteAction); //$NON-NLS-1$
    // open
    Icon icon = new ResizableIcon(Tracker.class.getResource("resources/images/open.gif")); //$NON-NLS-1$
    final AbstractAction openAction = new AbstractAction(TrackerRes.getString("TActions.Action.Open"), icon) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        trackerPanel.setSelectedPoint(null);
      	trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
        	frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          TrackerIO.open((File)null, frame);
        	frame.setCursor(Cursor.getDefaultCursor());
        }
      }
    };
    actions.put("open", openAction); //$NON-NLS-1$
    // open url
    final AbstractAction openURLAction = new AbstractAction(TrackerRes.getString("TActions.Action.OpenURL")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Object input = JOptionPane.showInputDialog(trackerPanel.getTFrame(), 
        		TrackerRes.getString("TActions.Dialog.OpenURL.Message") //$NON-NLS-1$
        		+":                             ", //$NON-NLS-1$
        		TrackerRes.getString("TActions.Dialog.OpenURL.Title"),   //$NON-NLS-1$
            JOptionPane.PLAIN_MESSAGE, null, null, null);
        if(input==null || input.toString().trim().equals("")) { //$NON-NLS-1$
          return;
        }
        Resource res = ResourceLoader.getResource(input.toString().trim());
        if (res==null || res.getURL()==null) {
    	    JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
    	        TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Message") //$NON-NLS-1$
    	        +"\n\""+input.toString().trim()+"\"",  //$NON-NLS-1$ //$NON-NLS-2$
    	        TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Title"),  //$NON-NLS-1$ 
    	        JOptionPane.ERROR_MESSAGE);
        	return;
        }
        URL url = res.getURL();
        trackerPanel.setSelectedPoint(null);
      	trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
        	frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          TrackerIO.open(url, frame);
        	frame.setCursor(Cursor.getDefaultCursor());
        }
      }
    };
    actions.put("openURL", openURLAction); //$NON-NLS-1$
    // openBrowser
    icon = new ResizableIcon(Tracker.class.getResource("resources/images/open_catalog.gif")); //$NON-NLS-1$
    final AbstractAction openBrowserAction = new AbstractAction(TrackerRes.getString("TActions.Action.OpenBrowser"), icon) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
	      	frame.getLibraryBrowser().setVisible(true);
        }
      }
    };
    actions.put("openBrowser", openBrowserAction); //$NON-NLS-1$
    // properties
    final AbstractAction propertiesAction = new AbstractAction(TrackerRes.getString("TActions.Action.Properties")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
	      	frame.getPropertiesDialog(trackerPanel).setVisible(true);
        }
      }
    };
    actions.put("properties", propertiesAction); //$NON-NLS-1$
    // close tab
    AbstractAction closeAction = new AbstractAction(TrackerRes.getString("TActions.Action.Close")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
          frame.removeTab(trackerPanel);
        }
      }
    };
    actions.put("close", closeAction); //$NON-NLS-1$
    // close all tabs
    AbstractAction closeAllAction = new AbstractAction(TrackerRes.getString("TActions.Action.CloseAll")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
          frame.removeAllTabs();
        }
      }
    };
    actions.put("closeAll", closeAllAction); //$NON-NLS-1$
    // import file
    AbstractAction importAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TrackerIO.importFile(trackerPanel);
      }
    };
    actions.put("import", importAction); //$NON-NLS-1$
    // import data
    AbstractAction importDataAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportData")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	getAction("dataTrack", trackerPanel).actionPerformed(e); //$NON-NLS-1$
      }
    };
    actions.put("importData", importDataAction); //$NON-NLS-1$
    // save current tab
    icon = new ResizableIcon(Tracker.class.getResource("resources/images/save.gif")); //$NON-NLS-1$
    AbstractAction saveAction = new AbstractAction(TrackerRes.getString("TActions.Action.Save"), icon) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TrackerIO.save(trackerPanel.getDataFile(), trackerPanel);
        trackerPanel.refreshNotesDialog();
      }
    };
    actions.put("save", saveAction); //$NON-NLS-1$
    // save tab as
    AbstractAction saveAsAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveAs"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TrackerIO.save(null, trackerPanel);
        trackerPanel.refreshNotesDialog();
      }
    };
    actions.put("saveAs", saveAsAction); //$NON-NLS-1$
    // save zip resource
    icon = new ResizableIcon(Tracker.class.getResource("resources/images/save_zip.gif")); //$NON-NLS-1$
    AbstractAction saveZipAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveZip")+"...", icon) { //$NON-NLS-1$ //$NON-NLS-2$
      public void actionPerformed(ActionEvent e) {
      	ExportZipDialog zipDialog = ExportZipDialog.getDialog(trackerPanel);
      	zipDialog.setVisible(true);
      }
    };
    actions.put("saveZip", saveZipAction); //$NON-NLS-1$
    // save tabset as
    AbstractAction saveTabsetAsAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveFrame"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TrackerIO.saveTabset(null, trackerPanel.getTFrame());
        trackerPanel.refreshNotesDialog();
      }
    };
    actions.put("saveTabsetAs", saveTabsetAsAction); //$NON-NLS-1$
    // save video
    AbstractAction saveVideoAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveVideoAs")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TrackerIO.saveVideo(null, trackerPanel);
      }
    };
    actions.put("saveVideo", saveVideoAction); //$NON-NLS-1$
    // export file
    AbstractAction exportAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TrackerIO.exportFile(trackerPanel);
      }
    };
    actions.put("export", exportAction); //$NON-NLS-1$
    // delete track
    AbstractAction deleteTrackAction = new AbstractAction(TrackerRes.getString("TActions.Action.Delete"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        // action command is name of track to delete
        TTrack track = trackerPanel.getTrack(e.getActionCommand());
        if (track != null) track.delete();
      }
    };
    actions.put("deleteTrack", deleteTrackAction); //$NON-NLS-1$
    AbstractAction configAction = new AbstractAction(TrackerRes.getString("TActions.Action.Config"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	TFrame frame = trackerPanel.getTFrame();
      	frame.showPrefsDialog();
      }
    };
    actions.put("config", configAction); //$NON-NLS-1$
    // axesVisible
    icon = new ResizableIcon(Tracker.class.getResource("resources/images/axes.gif")); //$NON-NLS-1$
    AbstractAction axesVisibleAction = new AbstractAction(TrackerRes.getString("TActions.Action.AxesVisible"), icon) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	CoordAxes axes = trackerPanel.getAxes();
        if (axes == null) return;
        boolean visible = !axes.isVisible();
        axes.setVisible(visible);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.hideMouseBox();
        if (visible && trackerPanel.getSelectedTrack() == null)
        	trackerPanel.setSelectedTrack(axes);
        else if (!visible && trackerPanel.getSelectedTrack() == axes)
        	trackerPanel.setSelectedTrack(null);
        trackerPanel.repaint();
      }
    };
    actions.put("axesVisible", axesVisibleAction); //$NON-NLS-1$
    // videoFilter
    AbstractAction videoFilterAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Video video = trackerPanel.getVideo();
        if (video == null) return;
        FilterStack filterStack = video.getFilterStack();
        Filter filter = null;
        Map<String, Class<? extends Filter>> filterClasses = trackerPanel.getFilters();
        Class<? extends Filter> filterClass = filterClasses.get(e.getActionCommand());
        if (filterClass != null) {
          try {
            filter = filterClass.newInstance();
          }
          catch (Exception ex) {
          	ex.printStackTrace();
          }
          if (filter != null) {
            filterStack.addFilter(filter);
            filter.setVideoPanel(trackerPanel);
            JDialog inspector = filter.getInspector();
            if (inspector != null) {
              FontSizer.setFonts(inspector, FontSizer.getLevel());
              inspector.pack();
              inspector.setVisible(true);
            }
          }
        }
        trackerPanel.repaint();
      }
    };
    actions.put("videoFilter", videoFilterAction); //$NON-NLS-1$
    // about video
    AbstractAction aboutVideoAction = new AbstractAction(
    		TrackerRes.getString("TActions.AboutVideo"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
	      	PropertiesDialog dialog = frame.getPropertiesDialog(trackerPanel);
	      	if (trackerPanel.getVideo()!=null)
	      		dialog.tabbedPane.setSelectedIndex(1);
	      	dialog.setVisible(true);
        }
      }
    };
    actions.put("aboutVideo", aboutVideoAction); //$NON-NLS-1$
    // print
    AbstractAction printAction = new AbstractAction(TrackerRes.getString("TActions.Action.Print"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        new TrackerIO.ComponentImage(trackerPanel).print();
      }
    };
    actions.put("print", printAction); //$NON-NLS-1$
    // exit
    AbstractAction exitAction = new AbstractAction(TrackerRes.getString("TActions.Action.Exit"), null) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
          for (int i = 0; i < frame.getTabCount(); i++) {
          	// save tabs in try/catch block so always closes
            try {
							if (!frame.getTrackerPanel(i).save()) {
							  return;
							}
						} catch (Exception ex) {
						}
          }
        }
        System.exit(0);
      }
    };
    actions.put("exit", exitAction); //$NON-NLS-1$
    // new point mass
    AbstractAction pointMassAction = new AbstractAction(TrackerRes.getString("PointMass.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        PointMass pointMass = new PointMass();
        pointMass.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(pointMass);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(pointMass);
        trackerPanel.getPlayer().setStepNumber(0);
        // offer to add new mass if single cm exists
        ArrayList<CenterOfMass> list = trackerPanel.getDrawables(CenterOfMass.class);
        if (list.size() == 1) {
          CenterOfMass cm = list.get(0);
          int result = JOptionPane.showConfirmDialog(
              trackerPanel,
              "Add " + pointMass.getName() + " to center of mass \"" + //$NON-NLS-1$ //$NON-NLS-2$
              cm.getName() + "\"?" + newline + //$NON-NLS-1$
              "Note: \"" + cm.getName() + "\" will disappear until  " + //$NON-NLS-1$ //$NON-NLS-2$
              pointMass.getName() + " is marked!", //$NON-NLS-1$
              TrackerRes.getString("TActions.Dialog.NewPointMass.Title"), //$NON-NLS-1$
              JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE);
          if (result == JOptionPane.YES_OPTION) {
            cm.addMass(pointMass);
          }
        }
      }
    };
    actions.put("pointMass", pointMassAction); //$NON-NLS-1$
    // new center of mass
    AbstractAction cmAction = new AbstractAction(TrackerRes.getString("CenterOfMass.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        CenterOfMass cm = new CenterOfMass();
        cm.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(cm);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(cm);
        CenterOfMassInspector cmInspector = cm.getInspector();
        cmInspector.setVisible(true);
      }
    };
    actions.put("cm", cmAction); //$NON-NLS-1$
    // new vector
    AbstractAction vectorAction = new AbstractAction(
        TrackerRes.getString("Vector.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Vector vec = new Vector();
        vec.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(vec);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(vec);
        trackerPanel.getPlayer().setStepNumber(0);
      }
    };
    actions.put("vector", vectorAction); //$NON-NLS-1$
    // new vector sum
    AbstractAction vectorSumAction = new AbstractAction(
        TrackerRes.getString("VectorSum.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        VectorSum sum = new VectorSum();
        sum.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(sum);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(sum);
        VectorSumInspector sumInspector = sum.getInspector();
        sumInspector.setVisible(true);
      }
    };
    actions.put("vectorSum", vectorSumAction); //$NON-NLS-1$
    // new offset origin item
    AbstractAction offsetOriginAction = new AbstractAction(TrackerRes.getString("OffsetOrigin.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        OffsetOrigin offset = new OffsetOrigin();
        offset.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(offset);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(offset);
        trackerPanel.getAxes().setVisible(true);
      }
    };
    actions.put("offsetOrigin", offsetOriginAction); //$NON-NLS-1$
    // new calibration item
    AbstractAction calibrationAction = new AbstractAction(TrackerRes.getString("Calibration.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Calibration cal = new Calibration();
        cal.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(cal);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(cal);
        trackerPanel.getAxes().setVisible(true);
      }
    };
    actions.put("calibration", calibrationAction); //$NON-NLS-1$
    // new line profile item
    AbstractAction lineProfileAction = new AbstractAction(TrackerRes.getString("LineProfile.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TTrack lineProfile = new LineProfile();
        lineProfile.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(lineProfile);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(lineProfile);
      }
    };
    actions.put("lineProfile", lineProfileAction); //$NON-NLS-1$
    // new RGBRegion item
    AbstractAction rgbRegionAction = new AbstractAction(TrackerRes.getString("RGBRegion.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TTrack rgb = new RGBRegion();
        rgb.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(rgb);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(rgb);
        trackerPanel.getPlayer().setStepNumber(0);
      }
    };
    actions.put("rgbRegion", rgbRegionAction); //$NON-NLS-1$
    // new analytic particle item
    AbstractAction analyticParticleAction = new AbstractAction(TrackerRes.getString("AnalyticParticle.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        final AnalyticParticle model = new AnalyticParticle();
        model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(model);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(model);
        FunctionTool inspector = model.getInspector();
        model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
        inspector.setVisible(true);
      }
    };
    actions.put("analyticParticle", analyticParticleAction); //$NON-NLS-1$
    // new dynamic particle item
    AbstractAction dynamicParticleAction = new AbstractAction(TrackerRes.getString("DynamicParticle.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        DynamicParticle model = new DynamicParticle();
        model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(model);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(model);
        FunctionTool inspector = model.getInspector();
        model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
        inspector.setVisible(true);
      }
    };
    actions.put("dynamicParticle", dynamicParticleAction); //$NON-NLS-1$
    // new dynamic particle polar item
    AbstractAction dynamicParticlePolarAction = new AbstractAction(TrackerRes.getString("DynamicParticlePolar.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        DynamicParticle model = new DynamicParticlePolar();
        model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(model);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(model);
        FunctionTool inspector = model.getInspector();
        model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
        inspector.setVisible(true);
      }
    };
    actions.put("dynamicParticlePolar", dynamicParticlePolarAction); //$NON-NLS-1$
    // new dynamic system item
    AbstractAction dynamicSystemAction = new AbstractAction(TrackerRes.getString("DynamicSystem.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	DynamicSystem model = new DynamicSystem();
        model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(model);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(model);
        FunctionTool inspector = model.getInspector();
        model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
        inspector.setVisible(true);
        DynamicSystemInspector systemInspector = model.getSystemInspector();
        systemInspector.setVisible(true);
      }
    };
    actions.put("dynamicSystem", dynamicSystemAction); //$NON-NLS-1$
    // new DataTrack item
    AbstractAction dataTrackAction = new AbstractAction(TrackerRes.getString("ParticleDataTrack.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        // choose file and get its data
      	File[] files = TrackerIO.getChooserFiles("open data"); //$NON-NLS-1$
        if (files==null) {
        	return;
        }
        String filePath = files[0].getAbsolutePath();
        String ext = XML.getExtension(filePath);
        if ("jar".equals(ext)) { //$NON-NLS-1$
        	if (!DataTrackTool.isDataSource(filePath)) {
        		String jarName = TrackerRes.getString("TActions.Action.DataTrack.Unsupported.JarFile") //$NON-NLS-1$
        				+ " \""+XML.getName(filePath)+"\" "; //$NON-NLS-1$ //$NON-NLS-2$
      			JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
      					jarName+TrackerRes.getString("TActions.Action.DataTrack.Unsupported.Message")+".", //$NON-NLS-1$ //$NON-NLS-2$
      					TrackerRes.getString("TActions.Action.DataTrack.Unsupported.Title"), //$NON-NLS-1$
      					JOptionPane.WARNING_MESSAGE);
      			return;
        	}
        	DataTrackTool.launchDataSource(filePath, true);
        }
        else {
	        trackerPanel.importData(filePath, null);
        }        
      }
    };
    actions.put("dataTrack", dataTrackAction); //$NON-NLS-1$
    // new (read-only) tape measure
    String s = TrackerRes.getString("TapeMeasure.Name"); //$NON-NLS-1$
    AbstractAction tapeAction = new AbstractAction(s, null) {
      public void actionPerformed(ActionEvent e) {
        TapeMeasure tape = new TapeMeasure();
        tape.setReadOnly(true);
        tape.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
      	// place tape above center of mat
      	Rectangle rect = trackerPanel.getMat().mat;
        double x = rect.width/2;
        double y = rect.height/2;
				tape.createStep(0, x-50, y-20, x+50, y-20);
        trackerPanel.addTrack(tape);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(tape);
      }
    };
    actions.put("tape", tapeAction); //$NON-NLS-1$
    // new protractor
    AbstractAction protractorAction = new AbstractAction(
        TrackerRes.getString("Protractor.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
      	Protractor protractor = new Protractor();
        protractor.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
      	// place protractor above center of mat
      	Rectangle rect = trackerPanel.getMat().mat;
        double x = rect.width/2;
        double y = rect.height/2;
        ProtractorStep step = (ProtractorStep)protractor.getStep(0);
        step.handle.setXY(x, y-30);        	
        trackerPanel.addTrack(protractor);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(protractor);
      }
    };
    actions.put("protractor", protractorAction); //$NON-NLS-1$
    // new circle track
    AbstractAction circleFitterAction = new AbstractAction(TrackerRes.getString("CircleFitter.Name"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        CircleFitter track = new CircleFitter();
        track.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
        trackerPanel.addTrack(track);
        trackerPanel.setSelectedPoint(null);
        trackerPanel.setSelectedTrack(track);
      }
    };
    actions.put("circleFitter", circleFitterAction); //$NON-NLS-1$
    // clone track action
    AbstractAction cloneTrackAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	String name = e.getActionCommand();
      	TTrack track = trackerPanel.getTrack(name);
      	if (track != null) {
      		// add digit to end of name
      		int n = 1;
      		try {
      			String number = name.substring(name.length()-1);
      			n = Integer.parseInt(number)+1;
      			name = name.substring(0, name.length()-1);
      		} catch (Exception ex) {}
      		// increment digit if necessary
      		Set <String> names = new HashSet<String>();
      		for (TTrack next: trackerPanel.getTracks()) {
      			names.add(next.getName());
      		}
      		try {
      			while (names.contains(name+n)) {
      				n++;
      			}
      		} catch (Exception ex) {}
      		// create XMLControl of track, assign new name, and copy to clipboard
          XMLControl control = new XMLControlElement(track);          
          control.setValue("name", name+n); //$NON-NLS-1$
          StringSelection data = new StringSelection(control.toXML());
          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          clipboard.setContents(data, data);
          // now paste
          TrackerIO.pasteXML(trackerPanel);
      	}
      }
    };
    actions.put("cloneTrack", cloneTrackAction); //$NON-NLS-1$
    // clear filters action
    AbstractAction clearFiltersAction = new AbstractAction(TrackerRes.getString("TActions.Action.ClearFilters"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Video video = trackerPanel.getVideo();
        if (video != null) {
        	ArrayList<String> xml = new ArrayList<String>();
        	Iterator<Filter> it = video.getFilterStack().getFilters().iterator();
        	while (it.hasNext()) {
        		Filter filter = it.next();
        		xml.add(new XMLControlElement(filter).toXML());
          	PerspectiveTrack track = PerspectiveTrack.filterMap.get(filter);
        		if (track!=null) {
        			PerspectiveTrack.filterMap.remove(filter);
        			trackerPanel.removeTrack(track);
        			track.setTrackerPanel(null);
        			track.filter = null;
        			filter.setVideoPanel(null);
        		}

        	}
          video.getFilterStack().clear();
          Undo.postFilterClear(trackerPanel, xml);
        }
      }
    };
    actions.put("clearFilters", clearFiltersAction); //$NON-NLS-1$
    // open video
    AbstractAction openVideoAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportVideo"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        TrackerIO.importVideo(trackerPanel);
      }
    };
    actions.put("openVideo", openVideoAction); //$NON-NLS-1$
    // close video
    AbstractAction closeVideoAction = new AbstractAction(TrackerRes.getString("TActions.Action.CloseVideo"), null) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        trackerPanel.setVideo(null);
        trackerPanel.repaint();
        trackerPanel.setImageSize(640, 480);
        TMenuBar.getMenuBar(trackerPanel).refresh();
      }
    };
    actions.put("closeVideo", closeVideoAction); //$NON-NLS-1$
    // reference frame
    AbstractAction refFrameAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        JMenuItem item = (JMenuItem) e.getSource();
        trackerPanel.setReferenceFrame(item.getActionCommand());
      }
    };
    actions.put("refFrame", refFrameAction); //$NON-NLS-1$
    return actions;
  }
}
