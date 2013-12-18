/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2014  Douglas Brown
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

import javax.swing.*;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This displays plot track views selected from a dropdown list.
 *
 * @author Douglas Brown
 */
public class TableTView extends TrackChooserTView {

  // instance fields
  protected Icon icon;
  protected JDialog columnsDialog;
  protected JLabel trackLabel;
  protected JButton defineButton;
  protected JButton closeButton;
  protected JPanel buttonPanel;
  protected boolean dialogVisible;

  /**
   * Constructs a TableTView for the specified tracker panel.
   *
   * @param panel the tracker panel
   */
  public TableTView(TrackerPanel panel) {
    super(panel);
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/datatable.gif")); //$NON-NLS-1$
    getColumnsDialog();
  }

  /**
   * Gets the name of the view
   *
   * @return the name of the view
   */
  public String getViewName() {
    return TrackerRes.getString("TFrame.View.Table"); //$NON-NLS-1$
  }

  /**
   * Gets the icon for this view
   *
   * @return the icon for this view
   */
  public Icon getViewIcon() {
    return icon;
  }

  /**
   * Creates a view for the specified track
   *
   * @param track the track to be viewed
   * @return the view of the track
   */
  protected TrackView createTrackView(TTrack track) {
    return new TableTrackView(track, trackerPanel, this);
  }

  /**
   * Overrides TrackChooserTView method.
   *
   * @param track the track to be selected
   */
  public void setSelectedTrack(TTrack track) {
  	if (track == null) {
    	noDataLabel.setText(TrackerRes.getString("TableTView.Label.NoData")); //$NON-NLS-1$
  	}
  	super.setSelectedTrack(track);
		// refresh or close the columns dialog
  	if (columnsDialog == null || !columnsDialog.isVisible()) return; 
  	else if (getSelectedTrack() == null) columnsDialog.setVisible(false);
  	else showColumnsDialog(getSelectedTrack());
  }
  
  /**
   * Refreshes the dropdown list and track views.
   */
  public void refresh() {
  	super.refresh();
  	if (columnsDialog == null) return;
		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		defineButton.setText(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
    columnsDialog.setTitle(TrackerRes.getString("TableTView.Dialog.TableColumns.Title")); //$NON-NLS-1$
  }
  
  /**
   * Displays the dialog box for selecting data columns.
   *
   * @param track the track
   */
  protected void showColumnsDialog(TTrack track) {
  	if (getColumnsDialog() == null) return;
    Container contentPane = columnsDialog.getContentPane();
    contentPane.removeAll();
    trackLabel.setIcon(track.getFootprint().getIcon(21, 16));
    trackLabel.setText(track.getName());
    contentPane.add(trackLabel);
    TableTrackView trackView = (TableTrackView)getTrackView(track);
    trackView.refreshColumnList();
    contentPane.add(trackView.columnsScroller);
	  contentPane.add(buttonPanel);
    contentPane.setPreferredSize(null);
    Dimension dim = contentPane.getPreferredSize();
    dim.height = Math.min(dim.height, 300);
    contentPane.setPreferredSize(dim);
    columnsDialog.pack();
    Point p0 = new Frame().getLocation();
    if (columnsDialog.getLocation().x == p0.x) {
      // center dialog on the screen
      dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width - columnsDialog.getBounds().width) / 2;
      int y = (dim.height - columnsDialog.getBounds().height) / 2;
      columnsDialog.setLocation(x, y);
    }
    if (!columnsDialog.isVisible()) columnsDialog.setVisible(true);
  }

  /**
   * Responds to property change events. This listens for
   * events "tab" from TFrame and "function" from FunctionTool.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("tab")) { //$NON-NLS-1$
      if (e.getNewValue() == trackerPanel && this.isVisible()) {
        if (columnsDialog != null) columnsDialog.setVisible(dialogVisible);
      }
      else {
        boolean vis = dialogVisible;
        if (columnsDialog != null) columnsDialog.setVisible(false);
        dialogVisible = vis;
      }
    }
    else if (e.getPropertyName().equals("function")) { //$NON-NLS-1$
      super.propertyChange(e);
      TTrack track = getSelectedTrack();
      if (columnsDialog != null && columnsDialog.isVisible()) {
        showColumnsDialog(track);      	
  	    JViewport port = ((TableTrackView)getTrackView(track)).
  	    		columnsScroller.getViewport();
  	    Dimension dim = port.getViewSize();
  	    int offset = port.getExtentSize().height;
  	    port.setViewPosition(new Point(0, dim.height-offset));
      }
    }
    else super.propertyChange(e);
  }
  
  private JDialog getColumnsDialog() {
  	TFrame frame = trackerPanel.getTFrame();
    if (columnsDialog == null && frame != null) {
      columnsDialog = new JDialog(frame, false) {
        public void setVisible(boolean vis) {
          super.setVisible(vis);
          dialogVisible = vis;
        }    	    	
      };
      columnsDialog.setTitle(TrackerRes.getString("TableTView.Dialog.TableColumns.Title")); //$NON-NLS-1$
      columnsDialog.setResizable(false);
      frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
      JPanel contentPane = new JPanel();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
      columnsDialog.setContentPane(contentPane);
      // create close button
      closeButton = new JButton(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
      closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	columnsDialog.setVisible(false);
        }
      });
      // create data function tool action
      ActionListener dataFunctionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TTrack track = getSelectedTrack();
          if (track != null) {
          	trackerPanel.getDataBuilder().setSelectedPanel(track.getName());
          	trackerPanel.getDataBuilder().setVisible(true);
          }
        }
      };
      // create define button
      defineButton = new JButton(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
      defineButton.addActionListener(dataFunctionListener);
      defineButton.setToolTipText(TrackerRes.getString("Button.Define.Tooltip")); //$NON-NLS-1$
      buttonPanel = new JPanel();
	    buttonPanel.add(defineButton);
	    buttonPanel.add(closeButton);
	    // create track label
	    trackLabel = new JLabel();
	    trackLabel.setBorder(BorderFactory.createEmptyBorder(7, 0, 6, 0));
	    trackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
  	return columnsDialog;
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
      TableTView view = (TableTView)obj;
      TTrack track = view.getSelectedTrack();
      if (track != null) { // contains at least one track
        control.setValue("selected_track", track.getName()); //$NON-NLS-1$
        // save customized tables
        ArrayList<TTrack> customized = new ArrayList<TTrack>();
        Map<TTrack, TrackView> views = view.trackViews;
        for (TTrack next: views.keySet()) {
        	if (views.get(next).isCustomState())
        		customized.add(next);
        }
        if (!customized.isEmpty()) {
        	ArrayList<String[][]> formattedColumns = new ArrayList<String[][]>();
	        String[][] data = new String[customized.size()][];
	        Iterator<TTrack> it = customized.iterator();
	        int i = -1;
	        while (it.hasNext()) {
	        	i++;
	        	track = it.next();
	          TableTrackView trackView = (TableTrackView)view.getTrackView(track);
	          String[] columns = trackView.getVisibleColumns();
	          data[i] = new String[columns.length+1];
	          System.arraycopy(columns, 0, data[i], 1, columns.length);
	          data[i][0] = track.getName();
	          String[][] formats = trackView.getColumnFormats();
	          if (formats.length>0) {
	          	String[][] withName = new String[formats.length][3];
	          	for (int j=0; j<formats.length; j++) {
	          		withName[j][0] = track.getName();
	          		withName[j][1] = formats[j][0];
	          		withName[j][2] = formats[j][1];
	          	}
	          	formattedColumns.add(withName);
	          }
	        }
	        control.setValue("track_columns", data); //$NON-NLS-1$
	        if (!formattedColumns.isEmpty()) {
	        	String[][][] patterns = formattedColumns.toArray(new String[0][0][0]);
		        control.setValue("column_formats", patterns); //$NON-NLS-1$
	        }
        }
      }
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
      TableTView view = (TableTView)obj;
      String[][] data = (String[][])control.getObject("track_columns"); //$NON-NLS-1$
      if (data != null) {
        Map<TTrack, TrackView> views = view.trackViews;
        for (TTrack track: views.keySet()) {
          TableTrackView trackView = (TableTrackView)view.getTrackView(track);
          if (trackView == null) continue;
          for (int i = 0; i < data.length; i++) {
            String[] columns = data[i];
            if (!columns[0].equals(track.getName())) continue;
            trackView.refresh = false; // prevents refreshes
          	for (int j = 0; j < trackView.checkBoxes.length; j++) {
          		trackView.checkBoxes[j].setSelected(false);
          	}
          	for (int j = 1; j < columns.length; j++) {          		
            	if (columns[j].equals("theta") && track instanceof PointMass)  //$NON-NLS-1$
            		columns[j] = "\u03b8"+"r"; //$NON-NLS-1$ //$NON-NLS-2$
            	else if (columns[j].equals("theta"))  //$NON-NLS-1$
            		columns[j] = "\u03b8"; //$NON-NLS-1$
            	else if (columns[j].equals("theta_v"))  //$NON-NLS-1$
            		columns[j] = "\u03b8"+"v"; //$NON-NLS-1$ //$NON-NLS-2$
            	else if (columns[j].equals("theta_a"))  //$NON-NLS-1$
            		columns[j] = "\u03b8"+"a"; //$NON-NLS-1$ //$NON-NLS-2$
            	else if (columns[j].equals("theta_p"))  //$NON-NLS-1$
            		columns[j] = "\u03b8"+"p"; //$NON-NLS-1$ //$NON-NLS-2$
            	else if (columns[j].equals("n") && track instanceof PointMass)  //$NON-NLS-1$
            		columns[j] = "step"; //$NON-NLS-1$
            	else if (columns[j].equals("KE"))  //$NON-NLS-1$
            		columns[j] = "K"; //$NON-NLS-1$
            	else if (columns[j].equals("x-comp"))  //$NON-NLS-1$
            		columns[j] = "x"; //$NON-NLS-1$
            	else if (columns[j].equals("y-comp"))  //$NON-NLS-1$
            		columns[j] = "y"; //$NON-NLS-1$
            	else if (columns[j].equals("x_tail"))  //$NON-NLS-1$
            		columns[j] = "xtail"; //$NON-NLS-1$
            	else if (columns[j].equals("y_tail"))  //$NON-NLS-1$
            		columns[j] = "ytail"; //$NON-NLS-1$
          		trackView.setVisible(columns[j], true);
          	}
            trackView.refresh = true;
          }
        }
      }
      String[][][] formats = (String[][][])control.getObject("column_formats"); //$NON-NLS-1$
      if (formats != null) {
        Map<TTrack, TrackView> views = view.trackViews;
        for (TTrack track: views.keySet()) {
          TableTrackView trackView = (TableTrackView)view.getTrackView(track);
          if (trackView == null) continue;
          for (int i = 0; i < formats.length; i++) {
            String[][] patterns = formats[i];
            if (!patterns[0][0].equals(track.getName())) continue;
            trackView.refresh = false; // prevents refreshes
          	for (int j = 0; j < patterns.length; j++) {
          		trackView.dataTable.setFormatPattern(patterns[j][1], patterns[j][2]);
          	}
            trackView.refresh = true;
          }
        }
      }
      TTrack track = view.getTrack(control.getString("selected_track")); //$NON-NLS-1$
      if (track != null) {
      	view.setSelectedTrack(track);
      	// code below for legacy files??
        TableTrackView trackView = (TableTrackView)view.getTrackView(track);
        String[] columns = (String[])control.getObject("visible_columns"); //$NON-NLS-1$
        if (columns != null) {
          trackView.refresh = false; // prevents refreshes
        	for (int i = 0; i < trackView.checkBoxes.length; i++) {
        		trackView.checkBoxes[i].setSelected(false);
        	}
        	for (int i = 0; i < columns.length; i++) {
        		trackView.setVisible(columns[i], true);
        	}
          trackView.refresh = true;
          trackView.refresh(view.trackerPanel.getFrameNumber());
        }
      }
      return obj;
    }
  }
}
