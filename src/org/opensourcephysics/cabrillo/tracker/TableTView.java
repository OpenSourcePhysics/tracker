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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumnModel;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.tools.FontSizer;

/**
 * This JPanel is the only child of TViewChooser viewPanel. It presents a JTable
 * selected from a dropdown list and maintains the JDialog for column choosing
 * for that table. It does not maintain the JTable -- that is
 * TableTrackView (a JScrollPane).
 *
 * @author Douglas Brown
 */
public class TableTView extends TrackChooserTView {

	protected static final Icon TABLEVIEW_ICON = Tracker.getResourceIcon("datatable.gif", true); //$NON-NLS-1$ ;
	private boolean dialogLastVisible;

	// instance fields

	/**
	 * Constructs a TableTView for the specified tracker panel.
	 *
	 * @param panel the tracker panel
	 */
	public TableTView(TrackerPanel panel) {
		super(panel);
	}

	/**
	 * We want to do this once, specifically as soon as we are attached through to the
	 * JFrame top ancestor.
	 */
	@Override
	public void addNotify() {
		super.addNotify();
		trackerPanel.getTFrame().removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); 
		trackerPanel.getTFrame().addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); 
	}
	
	/**
	 * ...and remove listener when we are detached.
	 */
	@Override
	public void removeNotify() {
		if (trackerPanel != null && trackerPanel.getTFrame() != null) {
			trackerPanel.getTFrame().removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); 
		}
		super.removeNotify();
	}

	/**
	 * Gets the name of the view
	 *
	 * @return the name of the view
	 */
	@Override
	public String getViewName() {
		return TrackerRes.getString("TFrame.View.Table"); //$NON-NLS-1$
	}

	/**
	 * Gets the icon for this view
	 *
	 * @return the icon for this view
	 */
	@Override
	public Icon getViewIcon() {
		return TABLEVIEW_ICON;
	}

	/**
	 * Gets the type of view
	 *
	 * @return one of the defined types
	 */
	@Override
	public int getViewType() {
		return TView.VIEW_TABLE;
	}

	/**
	 * Creates a view for the specified track
	 *
	 * @param track the track to be viewed
	 * @return the view of the track
	 */
	@Override
	protected TrackView createTrackView(TTrack track) {
		TableTrackView trackView = new TableTrackView(track, trackerPanel, this);
		FontSizer.setFonts(trackView); // for resizable icon only
//		
//		addComponentListener(new ComponentListener() {
//
//			@Override
//			public void componentResized(ComponentEvent e) {
//				OSPLog.debug("TrackChooserTView " + getBounds());
//				if (getSelectedTrack() != null && getHeight() > 0) {
//					DataTable t = ((TableTrackView) getTrackView(selectedTrack))
//					.getDataTable();
//					OSPLog.debug("TrackChooserTView " + t);
//					
//					//.repaint();
//				}
//				//	.refreshTable();
////				refresh();
//			}
//
//			@Override
//			public void componentMoved(ComponentEvent e) {
//			}
//
//			@Override
//			public void componentShown(ComponentEvent e) {
//			}
//
//			@Override
//			public void componentHidden(ComponentEvent e) {
//			}
//			
//		});

		return trackView;
	}

	/**
	 * Overrides TrackChooserTView method.
	 *
	 * @param track the track to be selected
	 */
	@Override
	public void setSelectedTrack(TTrack track) {
		super.setSelectedTrack(track);
		refreshColumnsDialog(track, true);
	}

	/**
	 * Displays the dialog box for selecting data columns.
	 *
	 * @param track the track
	 */
	protected void refreshColumnsDialog(TTrack track, boolean onlyIfVisible) {
		TableTrackView tableView = (TableTrackView) getTrackView(track);
		if (tableView != null)
			tableView.refreshColumnDialog(track, onlyIfVisible);
	}

	/**
	 * Responds to property change events. This listens for events
	 * TFrame.PROPERTY_TFRAME_TAB and "function" from FunctionTool.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TFrame.PROPERTY_TFRAME_TAB:
			if (e.getNewValue() != null) {
				TableTrackView trackview = (TableTrackView) getTrackView(selectedTrack);
				if (trackview != null) {
					dialogLastVisible = trackview.setDialogVisible(
							e.getNewValue() == trackerPanel && isVisible(), dialogLastVisible);
				}
			}
			break;
		case "function": //$NON-NLS-1$
			super.propertyChange(e);
			if (getSelectedTrack() != null)
				((TableTrackView) getTrackView(selectedTrack)).buildForNewFunction();
			break;
		default:
			super.propertyChange(e);
			break;
		}
	}

	/**
	 * Cleans up this view
	 */
	@Override
	public void cleanup() {
		super.cleanup();
		if (trackerPanel != null && trackerPanel.getTFrame() != null) {
			trackerPanel.getTFrame().removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
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
		 * @param obj     the TrackerPanel object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			TableTView view = (TableTView) obj;
			TTrack track = view.getSelectedTrack();
			if (track != null) { // contains at least one track
				control.setValue("selected_track", track.getName()); //$NON-NLS-1$
				// save customized tables
				ArrayList<TTrack> customized = new ArrayList<TTrack>();
				Map<TTrack, TrackView> views = view.trackViews;
				for (TTrack next : views.keySet()) {
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
						TableTrackView trackView = (TableTrackView) view.getTrackView(track);
						String[] columns = trackView.getOrderedVisibleColumns();
						data[i] = new String[columns.length + 1];
						System.arraycopy(columns, 0, data[i], 1, columns.length);
						data[i][0] = track.getName();
						String[][] formats = trackView.getColumnFormats();
						if (formats.length > 0) {
							String[][] withName = new String[formats.length][3];
							for (int j = 0; j < formats.length; j++) {
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
			TableTView view = (TableTView) obj;
			String[][] data = (String[][]) control.getObject("track_columns"); //$NON-NLS-1$
			if (data != null) {
				Map<TTrack, TrackView> views = view.trackViews;
				for (TTrack track : views.keySet()) {
					TableTrackView tableView = (TableTrackView) view.getTrackView(track);
					if (tableView == null)
						continue;
					for (int i = 0; i < data.length; i++) {
						String[] columns = data[i];
						if (!columns[0].equals(track.getName()))
							continue;
						tableView.setRefreshing(false); // prevents refreshes
						// start by unchecking all checkboxes
						tableView.bsCheckBoxes.clear();
						tableView.textColumnsVisible.clear();
						// then select checkboxes specified in track_columns
						Map<String, Integer> htOrder = new HashMap<String, Integer>();
						for (int j = 1; j < columns.length; j++) {
							String name = columns[j];
							switch (name) {
							case "theta":
								name = (track instanceof PointMass ? "\u03b8r"//$NON-NLS-1$
										: "\u03b8"); //$NON-NLS-1$
								break;
							case "theta_v": //$NON-NLS-1$
								name = "\u03b8v"; //$NON-NLS-1$ //$NON-NLS-2$
								break;
							case "theta_a": //$NON-NLS-1$
								name = "\u03b8a"; //$NON-NLS-1$ //$NON-NLS-2$
								break;
							case "theta_p": //$NON-NLS-1$
								name = "\u03b8p"; //$NON-NLS-1$ //$NON-NLS-2$
								break;
							case "n":
								if (track instanceof PointMass) // $NON-NLS-1$
									name = "step"; //$NON-NLS-1$
								break;
							case "KE": //$NON-NLS-1$
								name = "K"; //$NON-NLS-1$
								break;
							case "x-comp": //$NON-NLS-1$
								name = "x"; //$NON-NLS-1$
								break;
							case "y-comp": //$NON-NLS-1$
								name = "y"; //$NON-NLS-1$
								break;
							case "x_tail": //$NON-NLS-1$
								name = "xtail"; //$NON-NLS-1$
								break;
							case "y_tail": //$NON-NLS-1$
								name = "ytail"; //$NON-NLS-1$
								break;
							}
							htOrder.put(name, j);
							tableView.setVisible(columns[j] = name, true);
						}
						
						// BH? There has to be a much easier way of doing this. 
						
						// move columns so the table column order matches the saved track_columns order
						// get list of checked boxes--doesn't include independent variable
						String[] checkedBoxes = tableView.getVisibleColumns();
						// expand to include independent variable
						String[] visibleColumns = new String[checkedBoxes.length + 1];
						visibleColumns[0] = track.getDataName(0);
						System.arraycopy(checkedBoxes, 0, visibleColumns, 1, checkedBoxes.length);
						// create desiredOrder from track_columns array by omitting track name
						String[] desiredOrder = new String[columns.length - 1];
						System.arraycopy(columns, 1, desiredOrder, 0, desiredOrder.length);
						// convert desiredOrder names to desiredIndexes
						final int[] desiredIndexes = new int[desiredOrder.length];
						for (int k = 0; k < desiredOrder.length; k++) {
							String name = desiredOrder[k];
							for (int g = 0; g < visibleColumns.length; g++) {
								if (visibleColumns[g].equals(name)) {
									desiredIndexes[k] = g;
								}
							}
						}
						// move table columns after table is fully constructed
						final TableColumnModel model = tableView.dataTable.getColumnModel();
						Runnable runner = new Runnable() {
							@Override
							public void run() {
								outer: for (int targetIndex = 0; targetIndex < desiredIndexes.length; targetIndex++) {
									// find column with modelIndex and move to targetIndex
									for (int k = 0; k < desiredIndexes.length; k++) {
										if (model.getColumn(k).getModelIndex() == desiredIndexes[targetIndex]) {
											try {
												model.moveColumn(k, targetIndex);
											} catch (Exception e) {
											}
											continue outer;
										}
									}
								}

							}
						};
						SwingUtilities.invokeLater(runner);
						tableView.setRefreshing(true);
					}
				}
			}
			String[][][] formats = (String[][][]) control.getObject("column_formats"); //$NON-NLS-1$
			if (formats != null) {
				Map<TTrack, TrackView> views = view.trackViews;
				for (TTrack track : views.keySet()) {
					TableTrackView tableView = (TableTrackView) view.getTrackView(track);
					if (tableView == null)
						continue;
					for (int i = 0; i < formats.length; i++) {
						String[][] patterns = formats[i];
						if (!patterns[0][0].equals(track.getName()))
							continue;
						tableView.setRefreshing(false); // prevents refreshes
						for (int j = 0; j < patterns.length; j++) {
							tableView.dataTable.setFormatPattern(patterns[j][1], patterns[j][2]);
						}
						tableView.setRefreshing(true);
					}
				}
			}
			TTrack track = view.getTrack(control.getString("selected_track")); //$NON-NLS-1$
			if (track != null) {
				view.setSelectedTrack(track);
				// code below for legacy files??
				TableTrackView trackView = (TableTrackView) view.getTrackView(track);
				String[] columns = (String[]) control.getObject("visible_columns"); //$NON-NLS-1$
				if (columns != null) {
					trackView.setRefreshing(false); // prevents refreshes
					trackView.bsCheckBoxes.clear();
					for (int i = 0; i < columns.length; i++) {
						trackView.setVisible(columns[i], true);
					}
					trackView.setRefreshing(true);
					trackView.refresh(view.trackerPanel.getFrameNumber(), DataTable.MODE_TRACK_LOADER);
				}
			}
			return obj;
		}
	}

	@Override
	protected void refreshMenus() {
	}

}
