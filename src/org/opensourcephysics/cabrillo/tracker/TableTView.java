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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionTool;

/**
 * This JPanel is the only child of TViewChooser viewPanel. It presents a JTable
 * selected from a dropdown list and maintains the JDialog for column choosing
 * for that table. It does not maintain the JTable -- that is TableTrackView (a
 * JScrollPane).
 *
 * @author Douglas Brown
 */
public class TableTView extends TrackChooserTView {

	protected static final Icon TABLEVIEW_ICON = Tracker.getResourceIcon("datatable.gif", true); //$NON-NLS-1$ ;

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
	 * We want to do this once, specifically as soon as we are attached through to
	 * the JFrame top ancestor.
	 */
	@Override
	public void addNotify() {
		super.addNotify();
		frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this);
		frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this);
	}

	/**
	 * ...and remove listener when we are detached.
	 */
	@Override
	public void removeNotify() {
		if (panelID != null && frame != null) {
			frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this);
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

	@Override
	public void refreshPopup(JPopupMenu popup) {
		// pass to selected TableTrackView
		TableTrackView trackview = (TableTrackView) getTrackView(selectedTrack);
		if (trackview != null) {
			trackview.refreshToolbarPopup(popup);
		}
	}

	/**
	 * Creates a view for the specified track
	 *
	 * @param track the track to be viewed
	 * @return the view of the track
	 */
	@Override
	protected TrackView createTrackView(TTrack track) {
		TableTrackView trackView = new TableTrackView(track, getPanel(), this);
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

//	/**
//	 * Overrides TrackChooserTView method.
//	 *
//	 * @param track the track to be selected
//	 */
//	@Override
//	public void setSelectedTrack(TTrack track) {
//		super.setSelectedTrack(track);
////		refreshColumnsDialog(track, true);
//	}

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
		TrackerPanel panel = getPanel();
		switch (e.getPropertyName()) {
		case TTrack.PROPERTY_TTRACK_FORMAT:
			// format has changed
			TrackView view = null;
			TTrack track = getSelectedTrack();
			if (track != null && (view = getTrackView(track)) != null) {
				int frameNo = panel.getFrameNumber();
				view.refresh(frameNo, DataTable.MODE_TRACK_REFRESH);
			}
			break;
		case TFrame.PROPERTY_TFRAME_TAB:
			if (e.getNewValue() != null && !frame.isRemovingAll()) {
				TableTrackView trackview = (TableTrackView) getTrackView(selectedTrack);
				if (trackview != null) {
					trackview.setDialogAsLastVisible(e.getNewValue() == panel && isVisible());
				}
			}
			break;
		case FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION: // $NON-NLS-1$
			super.propertyChange(e);
//			if (getSelectedTrack() != null) {
//				TableTrackView trackView = (TableTrackView) getTrackView(selectedTrack);
//				trackView.refreshNameMaps();
//				trackView.buildForNewFunction();
//			}
			// refresh all trackviews, not just selected track
			if (trackViews != null) {
				for (TrackView next : trackViews.values()) {
					TableTrackView trackView = (TableTrackView) next;
					trackView.refreshNameMaps();
					trackView.buildForNewFunction();
				}
			}
			break;
		default:
			super.propertyChange(e);
		}
	}

	/**
	 * Cleans up this view
	 */
	@Override
	public void cleanup() {
		super.cleanup();
		if (panelID != null && frame != null) {
			frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
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

		private TableTrackView tableTrackView;
		private TTrack track;
		private TableTView view;
		private Map<TTrack, TrackView> trackViews;

		/**
		 * Saves object data.
		 *
		 * @param control the control to save to
		 * @param obj     the TrackerPanel object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			TableTView view = (TableTView) obj;
			TTrack selectedTrack = view.getSelectedTrack();
			if (selectedTrack != null) { // contains at least one track
				control.setValue("selected_track", selectedTrack.getName()); //$NON-NLS-1$
				// save customized tables
				ArrayList<TTrack> customized = new ArrayList<TTrack>();
				Map<TTrack, TrackView> views = view.trackViews;
				for (TTrack next : views.keySet()) {
					if (views.get(next).isCustomState()) {
						customized.add(next);
					}
				}
				if (!customized.isEmpty()) {
					ArrayList<String[][]> formattedColumns = new ArrayList<String[][]>();
					String[][] data = new String[customized.size()][];
					ArrayList<String[]> datasetIndices = new ArrayList<String[]>();
					for (int i = 0, n = customized.size(); i < n; i++) {
						TTrack track = customized.get(i);
						String name= track.getName();
						TableTrackView trackView = (TableTrackView) view.getTrackView(track);
						String[] columns = trackView.getOrderedVisibleColumns();
						data[i] = new String[columns.length + 1];
						data[i][0] = name;
						System.arraycopy(columns, 0, data[i], 1, columns.length);
						String[][] formats = trackView.getColumnFormats();
						if (formats.length > 0) {
							String[][] withName = new String[formats.length][3];
							for (int j = 0; j < formats.length; j++) {
								withName[j][0] = name;
								withName[j][1] = formats[j][0];
								withName[j][2] = formats[j][1];
							}
							formattedColumns.add(withName);
						}
						if (trackView.myDatasetIndex > -1) {
							datasetIndices.add(new String[] { name, Integer.toString(trackView.myDatasetIndex) });
						}
					}
					control.setValue("track_columns", data); //$NON-NLS-1$
					if (!formattedColumns.isEmpty()) {
						String[][][] patterns = formattedColumns.toArray(new String[formattedColumns.size()][][]);
						control.setValue("column_formats", patterns); //$NON-NLS-1$
					}
					if (!datasetIndices.isEmpty()) {
						String[][] indices = datasetIndices.toArray(new String[datasetIndices.size()][]);
						control.setValue("dataset_indices", indices); //$NON-NLS-1$
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
			view = (TableTView) obj;
			trackViews = view.trackViews;
			String[][] data = (String[][]) control.getObject("track_columns"); //$NON-NLS-1$
			if (data != null && trackViews == null) {
				// new view has never been refreshed
				view.refresh();
				trackViews = view.trackViews;
			}
			if (trackViews != null && data != null) {
				for (int i = 0; i < data.length; i++) {
					String[] columns = data[i];
					if (columns != null && setTrackAndTableView(columns[0])) {
						tableTrackView.setRefreshing(false); // prevents refreshes
						// start by unchecking all checkboxes
						tableTrackView.bsCheckBoxes.clear();
						tableTrackView.bsTextColumnsVisible.clear();
						// now select checkboxes specified in track_columns
//    							Map<String, Integer> htOrder = new HashMap<String, Integer>(); // BH! never used
						columns = fixColumnList(columns, track);
						for (int j = 1; j < columns.length; j++) {
							// htOrder.put(name, j);
							tableTrackView.setVisible(columns[j] = fixColumnName(columns[j], track), true);
						}
						setColumnOrder(tableTrackView, track, columns);
						tableTrackView.setRefreshing(true);
					}
				}
				String[][][] formats = (String[][][]) control.getObject("column_formats"); //$NON-NLS-1$
				if (formats != null) {
					for (int i = 0; i < formats.length; i++) {
						String[][] patterns = formats[i];
						if (setTrackAndTableView(patterns[0][0])) {
							tableTrackView.setRefreshing(false); // prevents refreshes
							for (int j = 0; j < patterns.length; j++) {
								tableTrackView.dataTable.setFormatPattern(patterns[j][1], patterns[j][2]);
							}
							tableTrackView.setRefreshing(true);
						}
					}
				}
				String[][] datasetIndices = (String[][]) control.getObject("dataset_indices"); //$NON-NLS-1$
				if (datasetIndices != null) {
					for (int i = 0; i < datasetIndices.length; i++) {
						String[] indices = datasetIndices[i];
						if (setTrackAndTableView(indices[0])) {
							tableTrackView.setRefreshing(false); // prevents refreshes
							tableTrackView.setDatasetIndex(Integer.parseInt(indices[1]));
							tableTrackView.setRefreshing(true);
						}
					}
				}
			}
			TTrack selectedTrack = view.getTrack(control.getString("selected_track")); //$NON-NLS-1$
			if (selectedTrack != null) {
				view.setSelectedTrack(selectedTrack);
				// code below for legacy files??
				String[] visibleColumns = (String[]) control.getObject("visible_columns"); //$NON-NLS-1$
				if (visibleColumns != null) {
					tableTrackView = (TableTrackView) view.getTrackView(selectedTrack);
					tableTrackView.setRefreshing(false); // prevents refreshes
					tableTrackView.bsCheckBoxes.clear();
					for (int i = 0; i < visibleColumns.length; i++) {
						tableTrackView.setVisible(fixColumnName(visibleColumns[i], selectedTrack), true);
					}
					tableTrackView.setRefreshing(true);
					tableTrackView.refresh(view.getPanel().getFrameNumber(), DataTable.MODE_TRACK_LOADER);
				}
			}
			tableTrackView = null;
			view = null;
			trackViews = null;
			return obj;
		}

		private boolean setTrackAndTableView(String name) {
			for (TTrack track : trackViews.keySet()) {
				if ((tableTrackView = (TableTrackView) view.getTrackView(track)) != null
						&& name.equals(track.getName())) {
					this.track = track;
					return true;
				}
			}
			tableTrackView = null;
			return false;
		}

		private static String fixColumnName(String name, TTrack track) {
			switch (name) {
			case "theta":
				return (track.ttype == TTrack.TYPE_POINTMASS ? "\u03b8r"//$NON-NLS-1$
						: "\u03b8"); //$NON-NLS-1$
			case "theta_v": //$NON-NLS-1$
				return "\u03b8v"; //$NON-NLS-1$ //$NON-NLS-2$
			case "theta_a": //$NON-NLS-1$
				return "\u03b8a"; //$NON-NLS-1$ //$NON-NLS-2$
			case "theta_p": //$NON-NLS-1$
				return "\u03b8p"; //$NON-NLS-1$ //$NON-NLS-2$
			case "n":
				return (track.ttype == TTrack.TYPE_POINTMASS ? "step" : name);
			case "KE": //$NON-NLS-1$
				return "K"; //$NON-NLS-1$
			case "x-comp": //$NON-NLS-1$
				return "x"; //$NON-NLS-1$
			case "y-comp": //$NON-NLS-1$
				return "y"; //$NON-NLS-1$
			case "x_tail": //$NON-NLS-1$
				return "xtail"; //$NON-NLS-1$
			case "y_tail": //$NON-NLS-1$
				return "ytail"; //$NON-NLS-1$
			case "vx":
			case "vy":
			case "ax":
			case "ay":
			case "px":
			case "py":
			case "pixelx":
			case "pixely":
				return name.substring(0, name.length() - 1) + "_{" + name.charAt(name.length() - 1) + "}";
			}
			return name;
		}

		/**
		 * Older versions might not include t column.
		 * 
		 * @param columns
		 * @return
		 */
		private static String[] fixColumnList(String[] columns, TTrack track) {
			String indepVar = track.getDataName(0);
			if (columns.length < 2 || indepVar.equals(columns[1]))
				return columns;
			String[] newCols = new String[columns.length + 1];
			newCols[0] = columns[0];
			newCols[1] = indepVar;
			for (int i = 1; i < columns.length; i++) {
				newCols[i + 1] = columns[i];
			}
			return newCols;
		}

		/**
		 * Move columns so the table column order matches the saved track_columns order
		 * 
		 * @param tableView
		 * @param track
		 * @param columns
		 */
		private void setColumnOrder(TableTrackView tableView, TTrack track, String[] columns) {

			// BH? There has to be a much easier way of doing this.
			// Why not just use DataTable.setModelColumnOrder(int[])?

			// get list of checked boxes--doesn't include independent variable

			String[] checkedBoxes = tableView.getVisibleColumns();
			if (checkedBoxes.length == 0)
				return;

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
			SwingUtilities.invokeLater(() -> {
				try {
					tableView.dataTable.setModelColumnOrder(desiredIndexes);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("TableTView.Loader invokelater exception " + Arrays.toString(desiredIndexes));
				}
// BH not necessary to use general JTable calls here?
//				outer: for (int targetIndex = 0; targetIndex < d.length; targetIndex++) {
//					// find column with modelIndex and move to targetIndex
//					for (int k = 0; k < d.length; k++) {
//						if (k != targetIndex // BH added this check 2022.01.03
//								&& model.getColumn(k).getModelIndex() == d[targetIndex]) {
//							try {
//								model.moveColumn(k, targetIndex);
//							} catch (Exception e) {
//								System.err.println("TableTView.Loader failed to move column " + k + " to " + targetIndex);
//							}
//							continue outer;
//						}
//					}
//				}
			});
		}

	}

	@Override
	protected void refreshMenus() {
	}

	@Override
	public void dispose() {
		frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this);
		super.dispose();
	}

}
