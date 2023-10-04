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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.opensourcephysics.controls.ControlsRes;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;

import javajs.async.AsyncDialog;

/**
 * A dialog to export data from one or more tracks.
 * Data can be copied to the clipboard or saved to a file.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class ExportDataDialog extends JDialog {
	
	protected static ExportDataDialog dataExporter; // singleton
	
	private static String fullPrecisionPattern = "0.000000E0";
	private static File lastSaved = new File("");
	private static Color buttonColor = Color.WHITE;
	// map of PanelID to BitSet of selected tracks
	private static HashMap<Integer, BitSet> selectedTracksBSMap;
	// map of trackType to list of all column names
	private static HashMap<Class<? extends TTrack>, ArrayList<String>> allColumnsMap;
	// map of trackType to BitSet of selected columns
	private static HashMap<Class<? extends TTrack>, BitSet> selectedColumnsBSMap;
	// map of PanelID to Boolean userHasSetData
	private static HashMap<Integer, Boolean> userHasSetDataMap;

	protected Integer panelID;
	private ArrayList<Integer> allTracks = new ArrayList<Integer>();
	private TFrame frame;
	private JButton saveAsButton, closeButton, copyButton;
	private JComponent tracksPanel, columnsPanel, delimiterPanel, formatPanel;
	private MyButton tracksButton, columnsButton, delimiterButton, formatButton;
	private TracksDialog tracksDialog;
	private ColumnsDialog columnsDialog;
	private JPopupMenu popup;
	private NumberField.NumberFormatter defaultFormatter;
	private Class<? extends TTrack> trackType;
	private boolean asFormatted = true;
	private int firstTextColumnIndex = -1;
	private int firstSelectedTextColumnIndex = -1;
	private ActionListener checkboxListener = (e) -> {
		showColumnsDialogAction(Integer.parseInt(e.getActionCommand()));
	};

	/**
	 * Returns the singleton ExportDataDialog for a specified TrackerPanel.
	 * 
	 * @param panel the TrackerPanel
	 * @return the ExportDataDialog
	 */
	public static ExportDataDialog getDialog(TrackerPanel panel) {
		if (dataExporter == null) {
			dataExporter = new ExportDataDialog(panel);
		} else {
			dataExporter.setTrackerPanel(panel);
			dataExporter.refreshGUI();
		}
		return dataExporter;
	}

	/**
	 * Constructs a ExportDataDialog.
	 *
	 * @param panel a TrackerPanel to supply the tracks
	 */
	private ExportDataDialog(TrackerPanel panel) {
		super(panel.getTFrame(), true);
		frame = panel.getTFrame();
		setResizable(false);
		defaultFormatter = new NumberField.NumberFormatter(false);
		defaultFormatter.setSigFigs(4); // same as typ track field default
		createGUI();
		setTrackerPanel(panel);
		refreshGUI();
		// center dialog on the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - getBounds().width) / 2;
		int y = (dim.height - getBounds().height) / 2;
		setLocation(x, y);
	}

//_____________________________ private methods ____________________________

	/**
	 * Creates the visible components of this dialog.
	 */
	private void createGUI() {
		popup = new JPopupMenu();
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		Box settingsPanel = Box.createVerticalBox();
		contentPane.add(settingsPanel, BorderLayout.CENTER);
		JPanel upper = new JPanel(new GridLayout(1, 2));
		JPanel lower = new JPanel(new GridLayout(1, 2));

		// tracks panel
		tracksPanel = new JPanel(new GridLayout(0, 1));
		tracksButton = new MyButton();
		tracksButton.addActionListener((e) -> {
			showTracksDialogAction();
		});
		tracksPanel.add(tracksButton);

		// delimiter panel
		delimiterPanel = new JPanel(new GridLayout(0, 1));
		delimiterButton = new MyButton() {
			@Override
			protected JPopupMenu getPopup() {
				return getDelimiterMenu();
			}
		};
		delimiterPanel.add(delimiterButton);

		// columns panel
		columnsPanel = new JPanel(new GridLayout(0, 1));
		columnsButton = new MyButton();
		columnsButton.addActionListener((e) -> {
			showColumnsDialogAction(-1);
		});
		columnsPanel.add(columnsButton);

		// format panel
		formatPanel = new JPanel(new GridLayout(0, 1));
		formatButton = new MyButton() {
			@Override
			protected JPopupMenu getPopup() {
				return getFormatMenu();
			}
		};
		formatPanel.add(formatButton);
		
		// assemble
		settingsPanel.add(upper);
		settingsPanel.add(lower);
		upper.add(tracksPanel);
		upper.add(columnsPanel);
		lower.add(formatPanel);
		lower.add(delimiterPanel);

		// buttons
		saveAsButton = new JButton();
		saveAsButton.setForeground(new Color(0, 0, 102));
		saveAsButton.addActionListener((e) -> {
			saveAsAction();
		});
		copyButton = new JButton();
		copyButton.setForeground(new Color(0, 0, 102));
		copyButton.addActionListener((e) -> {
			String data = getDataString();
			if (data == null) {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			OSPRuntime.copy(data, null);
		});
		closeButton = new JButton();
		closeButton.setForeground(new Color(0, 0, 102));
		closeButton.addActionListener((e) -> {
			// clear tracks so no lingering references to them
			allTracks.clear();
			setVisible(false);
		});
		
		// buttonbar
		JPanel buttonbar = new JPanel();
		contentPane.add(buttonbar, BorderLayout.SOUTH);
		buttonbar.add(saveAsButton);
		buttonbar.add(copyButton);
		buttonbar.add(closeButton);
		
		allColumnsMap = new HashMap<Class<? extends TTrack>, ArrayList<String>>();
		userHasSetDataMap = new HashMap<Integer, Boolean>();
	}
	
	private void setTrackerPanel(TrackerPanel panel) {
		// update available tracks
		ArrayList<TTrack> newTracks = panel.getExportableTracks();
		if (newTracks.isEmpty()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		allTracks.clear();
//		allColumnsMap.clear();
    for (int i = 0; i < newTracks.size(); i++) {
    	TTrack next = newTracks.get(i);
    	allTracks.add(next.getID());
    	Class<? extends TTrack> type = getTrackType(next);
    	ArrayList<String> allColumns = allColumnsMap.get(type);
    	if (allColumns == null) {
  			allColumns = new ArrayList<String>();
  			allColumnsMap.put(type, allColumns);
  			ArrayList<Dataset> sets = next.getData(panel).getDatasetsRaw();
  			for (int j = 0; j < sets.size(); j++) {
  				Dataset dataset = sets.get(j);
    			allColumns.add(dataset.getYColumnName()); 				
  			}
    	}
			
			// add text column names, if any
			ArrayList<String> textColNames = next.getTextColumnNames();
			for (int m = 0; m < textColNames.size(); m++) {
				if (!allColumns.contains(textColNames.get(m))) {
					if (firstTextColumnIndex==-1)
						firstTextColumnIndex = allColumns.size();
					allColumns.add(textColNames.get(m)); 				
				}
			}

    }
    // if this is a new panel, clear selected tracks and select default
		if (panel.getID() != panelID) {
			panelID = panel.getID();
			if (!userHasSetDataMap.containsKey(panelID)) {
				BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
				selectedTracksBS.clear();
				selectTrack(TTrack.getTrack(allTracks.get(0)), true);
			}
		}
		if (!userHasSetDataMap.containsKey(panelID))
			userHasSetDataMap.put(panelID, false);
		if (!userHasSetDataMap.get(panelID)) {
			selectDefaultColumns(true);
		}
	}
	
	private void selectTrack(TTrack track, boolean selected) {
		BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
		selectedTracksBS.set(track.getID(), selected);
		if (selectedTracksBS.isEmpty()) {
			trackType = null;
		}
		else if (selectedTracksBS.cardinality() == 1) {
			track = TTrack.getTrack(selectedTracksBS.nextSetBit(0));
			trackType = getTrackType(track);			
		}		
		else {
			// if previous trackType was cm, check new track
			if (trackType == CenterOfMass.class) {
				trackType = getTrackType(track); 
				// changes trackType to PointMass.class if non-cm type track was added
			}
		}
	}
	
	private void selectDefaultColumns(boolean selectTrack) {
		TViewChooser[] choosers = frame.getVisibleChoosers(panelID);
		for (int i = 0; i < choosers.length; i++) {
			if (choosers[i] == null)
				continue;
			TView view = choosers[i].getSelectedView();
			if (view.getViewType() == TView.VIEW_TABLE) {
				TableTView tView = (TableTView) view;
				TTrack track = tView.getSelectedTrack();
				if (track != null && getTrackType(track) == trackType) {
					if (selectTrack) {
						BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
						selectedTracksBS.clear();
						selectTrack(track, true);
					}
					TableTrackView tableView = (TableTrackView) tView.getTrackView(track);
					String[] cols = tableView.getVisibleColumns();
					BitSet selectedColsBS = getSelectedColumnsBitSet(trackType);
					selectedColsBS.clear();
					ArrayList<String> allColumnNames = allColumnsMap.get(trackType);
					for (int j = 0; j < cols.length; j++) {
						String var = cols[j];
						for (int k = 0; k < allColumnNames.size(); k++) {
							if (allColumnNames.get(k).equals(var)) {
								selectedColsBS.set(k);
								break;
							}
						}
					}
					break;
				}			
			}
		}
	}
	
	private BitSet getSelectedTracksBitSet(Integer panelID) {
		if (selectedTracksBSMap == null)
			selectedTracksBSMap = new HashMap<Integer, BitSet>();
		BitSet selected = selectedTracksBSMap.get(panelID);
		if (selected == null) {
			selected = new BitSet();
			selectedTracksBSMap.put(panelID, selected);
		}
		return selected;
	}
	
	private BitSet getSelectedColumnsBitSet(Class<? extends TTrack> type) {
		if (selectedColumnsBSMap == null)
			selectedColumnsBSMap = new HashMap<Class<? extends TTrack>, BitSet>();		
		BitSet namesBS = selectedColumnsBSMap.get(type);
		if (namesBS == null) {
			namesBS = new BitSet();
			selectedColumnsBSMap.put(type, namesBS);
		}
		return namesBS;
	}
	
	private TTrack getFirstSelectedTrack() {
		BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);		
		return selectedTracksBS.isEmpty()? null: TTrack.getTrack(selectedTracksBS.nextSetBit(0));
	}

	protected void setDelimiterAction(String delimName) {
		boolean isAdd = delimName.equals(TrackerRes.getString("ExportDataDialog.Delimiter.Add")); //$NON-NLS-1$
		boolean isRemove = delimName.equals(TrackerRes.getString("ExportDataDialog.Delimiter.Remove")); //$NON-NLS-1$
		String delimiter = TrackerIO.getDelimiter();
		if (isAdd) {
			String response = GUIUtils.showInputDialog(ExportDataDialog.this,
					TrackerRes.getString("TableTrackView.Dialog.CustomDelimiter.Message"), //$NON-NLS-1$
					TrackerRes.getString("TableTrackView.Dialog.CustomDelimiter.Title"), //$NON-NLS-1$
					JOptionPane.PLAIN_MESSAGE, delimiter);
			if (response != null && !"".equals(response.toString())) { //$NON-NLS-1$
				String s = response.toString();
				TrackerIO.setDelimiter(s);
				TrackerIO.addCustomDelimiter(s);
			}
		} else if (isRemove) {
			String[] choices = TrackerIO.customDelimiters.values().toArray(new String[1]);
			new AsyncDialog().showInputDialog(ExportDataDialog.this,
					TrackerRes.getString("TableTrackView.Dialog.RemoveDelimiter.Message"), //$NON-NLS-1$
					TrackerRes.getString("TableTrackView.Dialog.RemoveDelimiter.Title"), //$NON-NLS-1$
					JOptionPane.PLAIN_MESSAGE, null, choices, null, (e) -> {
						String s = e.getActionCommand();
						if (s != null) {
							TrackerIO.removeCustomDelimiter(s);
						}
					});
		} else {
			if (TrackerIO.getDelimiters().keySet().contains(delimName))
				TrackerIO.setDelimiter(TrackerIO.getDelimiters().get(delimName));
			else if (TrackerIO.customDelimiters.keySet().contains(delimName))
				TrackerIO.setDelimiter(TrackerIO.customDelimiters.get(delimName));
		}
		refreshGUI();		
	}

	private void setFormatAction(String format) {
		asFormatted = format.equals(TrackerRes.getString("TableTrackView.MenuItem.Formatted")); //$NON-NLS-1$
		refreshGUI();
	}
	
	private String getPatternFromTable(String var) {
		TViewChooser[] choosers = frame.getVisibleChoosers(panelID);
		for (int i = 0; i < choosers.length; i++) {
			if (choosers[i] == null)
				continue;
			TView view = choosers[i].getSelectedView();
			if (view.getViewType() == TView.VIEW_TABLE) {
				TableTView tableTView = (TableTView) view;
				TTrack track = tableTView.getSelectedTrack();
				if (track != null) {
					TableTrackView trackView = (TableTrackView)tableTView.getTrackView(track);
					return trackView.getDataTable().getFormatPattern(var);
				}
			}
		}
		return null;
	}

	private void showTracksDialogAction() {
		if (tracksDialog == null)
			tracksDialog = new TracksDialog();
		
    tracksDialog.refreshDisplay();
		FontSizer.setFonts(tracksDialog, FontSizer.getLevel());
		tracksDialog.pack();
    Point p = tracksButton.getLocationOnScreen();
    p.x -= tracksButton.getLocation().x;
    p.y += tracksButton.getHeight();
    tracksDialog.setLocation(p);
		tracksDialog.setVisible(true);
	}

	private void showColumnsDialogAction(int num) {
		if (num < 0) { // show columns dialog
			if (columnsDialog == null)
				columnsDialog = new ColumnsDialog();
			
			columnsDialog.rebuild();
			FontSizer.setFonts(columnsDialog, FontSizer.getLevel());
			columnsDialog.pack();
	    Point p = columnsButton.getLocationOnScreen();
	    p.x -= columnsButton.getLocation().x;
	    p.y += columnsButton.getHeight();
	    columnsDialog.setLocation(p);
	    columnsDialog.setVisible(true);
		}
		else { // set names BitSet
			boolean add = columnsDialog.checkBoxes[num].isSelected();
			// trackType should always be non-null here
			BitSet namesBS = getSelectedColumnsBitSet(trackType);
			namesBS.set(num, add);
			userHasSetDataMap.put(panelID, true);
			refreshGUI();
		}
	}
	
	private Class<? extends TTrack> getTrackType(TTrack track) {
		return PointMass.class.isAssignableFrom(track.getClass())?
				track.getClass() == CenterOfMass.class || track.getClass() == DynamicSystem.class?
				CenterOfMass.class:
  			PointMass.class: 
  			Vector.class.isAssignableFrom(track.getClass())?
  			Vector.class: 
  			track.getClass();
	}

	private void saveAsAction() {
		JFileChooser chooser = TrackerIO.getChooser();
		chooser.setSelectedFile(lastSaved); //$NON-NLS-1$
		File[] files = TrackerIO.getChooserFilesAsync(frame, "save data", null); //$NON-NLS-1$
		if (files == null || files.length == 0)
			return;
		File file = files[0];
		if (XML.getExtension(file.getName()) == null) {
			file = new File(file.getAbsolutePath() + ".txt");
		}
		if (!VideoIO.canWrite(file))
			return;
		// get export string and write to output file
		String output = getDataString();
		if (output == null)
			return;
		write(file, output);
		lastSaved = file;
	}
	
	@SuppressWarnings("unchecked")
	private String getDataString() {
		// trackType should always be non-null here		
		StringBuffer buf = new StringBuffer();
		TrackerPanel panel = frame.getTrackerPanelForID(panelID);	
		// which tracks to export
		BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
		if (selectedTracksBS.isEmpty())
			return null;
		TTrack[] selectedTracks = new TTrack[selectedTracksBS.cardinality()];
		int n = 0;
		for (int k = selectedTracksBS.nextSetBit(0); k >= 0; k = selectedTracksBS.nextSetBit(k + 1)) {
			selectedTracks[n++] = TTrack.getTrack(k);
		}

		ArrayList<String> allColumnNames = allColumnsMap.get(trackType);
		// allColumnNames includes text columns if any
		int datasetCount = allColumnNames.size();
		// get column names to export		
		BitSet namesBS = getSelectedColumnsBitSet(trackType);
		String[] selectedColumnNames = new String[namesBS.cardinality()];
		n = 0;
		firstSelectedTextColumnIndex = -1;
		for (int k = namesBS.nextSetBit(0); k >= 0; k = namesBS.nextSetBit(k + 1)) {
			selectedColumnNames[n++] = allColumnNames.get(k);
			if (firstTextColumnIndex > -1 
					&& k >= firstTextColumnIndex 
					&& firstSelectedTextColumnIndex == -1) {
				firstSelectedTextColumnIndex = n-1;
			}
		}
		
		String xVar = null;  // set below
		// collect selected track data
		int selectedTrackCount = selectedTracks.length;
		ArrayList<Dataset>[] selectedTrackData = new ArrayList[selectedTrackCount];
		n = 0;
		for (int i = 0; i < selectedTracks.length; i++) {
			TTrack track = selectedTracks[i];
			selectedTrackData[i] = track.getData(panel).getDatasetsRaw();
			if (xVar == null)
				xVar = selectedTrackData[i].get(0).getXColumnName();
		}
		
		// find dataset index of frame or pixel (lineprofile) variable
		boolean isFrames = trackType != LineProfile.class;
		int frameIndex = isFrames? -1: 0;
		for (int i = 0; i < datasetCount; i++) {
			if (allColumnNames.get(i).equals("frame")) {
				frameIndex = i;
				break;
			}
		}
		if (frameIndex == -1)
			return null;
		
		// set up arrays for data values, text values and frame numbers
		int colsPerTrack = selectedColumnNames.length;
		double[][][] dataValues = new double[selectedTrackCount][colsPerTrack][];
		String[][][] textValues = new String[selectedTrackCount][colsPerTrack][];
		double[][] frameNumbers = new double[selectedTrackCount][];
		for (int i = 0; i < selectedTrackCount; i++) {
			TTrack track = selectedTracks[i];
			// for each selected track, get list of datasets
			ArrayList<Dataset> datasets = selectedTrackData[i];
			
			Dataset dataset = datasets.get(frameIndex);
			frameNumbers[i] = getPoints(dataset, isFrames);
			// for each selected column name look for dataset with same name
			outer: for (int k = 0; k < selectedColumnNames.length; k++) {
				String colName = selectedColumnNames[k];
				for (int j = 0; j < datasetCount; j++) {
					// look thru all datasets to find column with colName
					if (datasets.size() <= j)
						break;
					dataset = datasets.get(j);					
					if (dataset.getYColumnName().equals(colName)) {
						dataValues[i][k] = getPoints(dataset, true);
						// convert angles to degrees if needed
						if (!frame.isAnglesInRadians() && 
								(colName.startsWith(Tracker.THETA) || 
									colName.startsWith(Tracker.OMEGA) || 
									colName.startsWith(Tracker.ALPHA))) {
							double[] angles = dataValues[i][k];
							for (int m = 0; m < angles.length; m++ ) {
								angles[m] *= (180 / Math.PI);
							}
						}
						continue outer;
					}
				}
				// if column name not found, look for text column
  			ArrayList<String> textColNames = track.getTextColumnNames();
  			for (int m = 0; m < textColNames.size(); m++) {
    			if (textColNames.get(m).equals(colName)) {
    				textValues[i][k] = new String[frameNumbers[i].length];
    				for (int a = 0; a < frameNumbers[i].length; a++) {
    					textValues[i][k][a] = track.getTextColumnEntry(colName, (int)frameNumbers[i][a]);
    				}
    			}
  			}

			}
		}
		
		// determine frame order
		// find min and max frame numbers in all track data
		int min = Integer.MAX_VALUE, max = -1;
		for (int i = 0; i < selectedTrackCount; i++) {
			// some tracks may have no data!
			if (frameNumbers[i].length == 0)
				continue;
			min = (int)Math.min(min, frameNumbers[i][0]);
			max = (int)Math.max(max, frameNumbers[i][frameNumbers[i].length - 1]);
		}
		boolean hasData = max >= 0;

		int frameCount = hasData? (max - min + 1): 0;
		int[][] indices = new int[selectedTrackCount][frameCount];
		String timePattern = null;
		String[][] patterns = new String[selectedTrackCount][colsPerTrack];
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		nf.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
		
		if (hasData) {
			// assemble trackDataset indices of corresponding frames
			for (int i = 0; i < selectedTrackCount; i++) {
				Arrays.fill(indices[i], -1); // -1 means no data at that frame
				n = 0;
				for (int j = min; j <= max; j++) {
					if (frameNumbers[i].length > 0 && frameNumbers[i][n] == j) {
						indices[i][j - min] = n;
						n++;
						if (n >= frameNumbers[i].length)
							break;
					}
				}
			}
					
			// set up number patterns
			// find representative track
			TTrack track = null;
			for (int i = 0; i < selectedTracks.length; i++) {
				if (selectedTracks[i].getClass() == trackType) {
					track = selectedTracks[i];
					break;
				}
			}
			if (track == null)
				track = selectedTracks[0];
			Map<String, String> defaultPatternMap = TTrack.getDefaultFormatPatterns(track.ttype);
			if (asFormatted) {
				for (int i = 0; i < selectedTrackCount; i++) {
					track = selectedTracks[i];
					Map<String, NumberField[]> fieldMap = track.getNumberFields();
					// get pattern for xVar
					NumberField[] fields = fieldMap.get(xVar);
					timePattern = fields != null && fields.length > 0?
							fields[0].getFixedPattern():
							getPatternFromTable(xVar);
					
					for (int j = 0; j < colsPerTrack; j++) {
						// get patterns for column vars
						String var = selectedColumnNames[j];
						String pattern = null;
						if (var != null) {
							fields = fieldMap.get(var);
							pattern = fields != null && fields.length > 0?
									fields[0].getFixedPattern():
									getPatternFromTable(var);
							if (pattern == null) {
								pattern = defaultPatternMap.get(var);
							}
						}
						patterns[i][j] = pattern;
					}
				}
			}
			else {
				timePattern = isFrames? fullPrecisionPattern: "0";
				for (int i = 0; i < selectedTrackCount; i++) {
					for (int j = 0; j < colsPerTrack; j++) {
						String var = selectedColumnNames[j];
						String pattern = defaultPatternMap.get(var);
						patterns[i][j] = "0".equals(pattern)? pattern: "0.000000E0";
					}
				}			
			}
		}		
		// add "multi:" gnuPlotComment if multiple tracks
		if (selectedTrackCount > 1) {
			buf.append("#multi:");
			buf.append(XML.NEW_LINE);
		}
		// assemble data string by rows in frame order
		// add track name row
		buf.append(TrackerIO.getDelimiter());
		for (int i = 0; i < selectedTrackCount; i++) {
			TTrack track = selectedTracks[i];
			buf.append(track.getName());
			for (int j = 0; j < colsPerTrack; j++) {
				if (isUndefinedTextColumn(j, textValues[i][j]))
					continue;				
				buf.append(TrackerIO.getDelimiter());
			}
		}
		buf.append(XML.NEW_LINE);
		// add column name row
		buf.append(xVar);
		buf.append(TrackerIO.getDelimiter());
		for (int i = 0; i < selectedTrackCount; i++) {
			for (int j = 0; j < colsPerTrack; j++) {
				if (selectedColumnNames[j] == null) {
					buf.append(TrackerIO.getDelimiter());
					continue;
				}
//				String units = "";
//				if (!frame.isAnglesInRadians() && 
//						(selectedColumnNames[j].startsWith(Tracker.THETA) || 
//							selectedColumnNames[j].startsWith(Tracker.OMEGA) || 
//							selectedColumnNames[j].startsWith(Tracker.ALPHA))) {
//					units = "("+Tracker.DEGREES+")";
//				}
//				buf.append(TeXParser.removeSubscripting(selectedColumnNames[j]) + units);
				
				if (isUndefinedTextColumn(j, textValues[i][j]))
					continue;

				buf.append(TeXParser.removeSubscripting(selectedColumnNames[j]));
				buf.append(TrackerIO.getDelimiter());
			}
		}
		buf.append(XML.NEW_LINE);
		if (hasData) {
			// add data rows
			VideoPlayer player = panel.getPlayer();
			for (int i = min; i <= max; i++) {
				// time or n is first in row
				double value = isFrames? player.getFrameTime(i) / 1000: i;
				if (timePattern != null && !"".equals(timePattern)) {
					nf.applyPattern(timePattern); //$NON-NLS-1$
					buf.append(nf.format(value));
				}
				else {
					buf.append(defaultFormatter.getText(value));
				}
	
				for (int j = 0; j < selectedTrackCount; j++) {
					double[][] data = dataValues[j];
					for (int k = 0; k < colsPerTrack; k++) {
						if (isUndefinedTextColumn(k, textValues[j][k]))
							continue;

						buf.append(TrackerIO.getDelimiter());
						if (indices[j][i - min] > -1) {
							if (data[k] == null) {
								// could be a text column
								String[][] text = textValues[j];
								if (text[k] != null) {
									String s = text[k][indices[j][i - min]];
									if (s != null && !s.equals("null"))
										buf.append(s);
								}
								continue;
							}
							value = data[k][indices[j][i - min]];
							if (!Double.isNaN(value)) {
								String pattern = patterns[j][k];
								if (pattern != null && !"".equals(pattern)) {
									nf.applyPattern(pattern); //$NON-NLS-1$
									buf.append(nf.format(value));
								}
								else {
									buf.append(defaultFormatter.getText(value));
								}
							}
						}
					}
				}
				buf.append(XML.NEW_LINE);
			}
		}
		
		return buf.toString();
	}
	
	private boolean isUndefinedTextColumn(int col, String[] textEntries) {
		if (firstSelectedTextColumnIndex > -1 && col >= firstSelectedTextColumnIndex) {
			return textEntries == null;
		}
		return false;
	}
	
	private double[] getPoints(Dataset dataset, boolean isY) {
		double[] p = isY? dataset.getYPointsRaw(): dataset.getXPointsRaw();
		return Arrays.copyOf(p, dataset.getIndex());
	}
	
	private JPopupMenu getDelimiterMenu() {
		popup.removeAll();
		// standard delimiters
		for (String key : TrackerIO.getDelimiters().keySet()) {
			JMenuItem item = new JMenuItem(key); //$NON-NLS-1$
			item.addActionListener((e) -> {
				setDelimiterAction(key);
			});
			popup.add(item);
		}
		// custom delimiters
		boolean hasCustom = !TrackerIO.customDelimiters.isEmpty();
		if (hasCustom) {
			popup.addSeparator();
			for (String key : TrackerIO.customDelimiters.keySet()) {
				JMenuItem item = new JMenuItem(key); //$NON-NLS-1$
				item.addActionListener((e) -> {
					setDelimiterAction(key);
				});
				popup.add(item);
			}
		}
		// add and remove delimiter items
		popup.addSeparator();
		final String add = TrackerRes.getString("ExportDataDialog.Delimiter.Add"); //$NON-NLS-1$
		JMenuItem item = new JMenuItem(add); //$NON-NLS-1$
		item.addActionListener((e) -> {
			setDelimiterAction(add);
		});
		popup.add(item);
		if (hasCustom) {
			final String rem = TrackerRes.getString("ExportDataDialog.Delimiter.Remove"); //$NON-NLS-1$
			item = new JMenuItem(rem); //$NON-NLS-1$
			item.addActionListener((e) -> {
				setDelimiterAction(rem);
			});
			popup.add(item);
		}

		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	private JPopupMenu getFormatMenu() {
		popup.removeAll();
		String form = TrackerRes.getString("TableTrackView.MenuItem.Formatted");
		String unform = TrackerRes.getString("TableTrackView.MenuItem.Unformatted");
		
		JMenuItem item = new JMenuItem(form); //$NON-NLS-1$
		item.addActionListener((e) -> {
			setFormatAction(form);
		});
		popup.add(item);
		item = new JMenuItem(unform); //$NON-NLS-1$
		item.addActionListener((e) -> {
			setFormatAction(unform);
		});
		popup.add(item);

		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	/**
	 * Refreshes the visible components of this dialog.
	 */
	private void refreshGUI() {
		// refresh title, titled borders and buttons
		String title = TrackerRes.getString("ExportDataDialog.Title"); //$NON-NLS-1$
		setTitle(title);
		title = TrackerRes.getString("Undo.Description.Tracks"); //$NON-NLS-1$
		Border space = BorderFactory.createEmptyBorder(0, 4, 6, 4);
		Border titled = BorderFactory.createTitledBorder(title);
		FontSizer.setFonts(titled, FontSizer.getLevel());
		tracksPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		title = TrackerRes.getString("ExportDataDialog.Subtitle.Delimiter"); //$NON-NLS-1$
		titled = BorderFactory.createTitledBorder(title);
		FontSizer.setFonts(titled, FontSizer.getLevel());
		delimiterPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		title = TrackerRes.getString("TableTrackView.Button.SelectTableData"); //$NON-NLS-1$
		titled = BorderFactory.createTitledBorder(title);
		FontSizer.setFonts(titled, FontSizer.getLevel());
		columnsPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		title = TrackerRes.getString("ExportDataDialog.Subtitle.Format"); //$NON-NLS-1$
		titled = BorderFactory.createTitledBorder(title);
		FontSizer.setFonts(titled, FontSizer.getLevel());
		formatPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		saveAsButton.setText(TrackerRes.getString("ExportVideoDialog.Button.SaveAs")); //$NON-NLS-1$
		copyButton.setText(TrackerRes.getString("CircleFitter.MenuItem.CopyToClipboard.Text")); //$NON-NLS-1$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		
		// tracks button
		BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
		TTrack track = getFirstSelectedTrack();
		String s = track == null? "": track.getName();
		if (selectedTracksBS.cardinality() > 1)
			s += " + " + (selectedTracksBS.cardinality() - 1);
		tracksButton.setText(s);
		s = "";
		for (int k = selectedTracksBS.nextSetBit(0); k >= 0; k = selectedTracksBS.nextSetBit(k + 1)) {
			s += TTrack.getTrack(k).getName() + ", "; //$NON-NLS-1$
		}
		// stop the track list at 300 in extreme cases
		int end = Math.min(s.length() - 2, 300);
		tracksButton.setToolTipText(s.length() > 1? s.substring(0, end): null);
		
		// delimiter button
		String delim = TrackerIO.getDelimiter();
		for (String key : TrackerIO.getDelimiters().keySet()) {
			if (delim.equals(TrackerIO.getDelimiters().get(key)))
				delimiterButton.setText(key);
		}
		for (String key : TrackerIO.customDelimiters.keySet()) {
			if (delim.equals(TrackerIO.customDelimiters.get(key)))
				delimiterButton.setText(key);
		}
		delimiterButton.setToolTipText(delimiterButton.getText()); //$NON-NLS-1$
		
		// format button
		String form = TrackerRes.getString("TableTrackView.MenuItem.Formatted");
		String unform = TrackerRes.getString("TableTrackView.MenuItem.Unformatted");
		formatButton.setText(asFormatted? form: unform);	
		formatButton.setToolTipText(formatButton.getText());

		// columns button
		s = "";
		if (track != null) {
			// trackType should always be non-null here
			ArrayList<String> allColumnNames = allColumnsMap.get(trackType);
			String xVar = s = track.getClass().getSimpleName().contains("LineProfile")?
					"n": "t";
			BitSet selectedColsBS = getSelectedColumnsBitSet(trackType);
			int count = 0;
			int max = 2;
			for (int i = 0; i < allColumnNames.size(); i++) {
				String name = allColumnNames.get(i);
				if (selectedColsBS.get(i)) {
					count++;
					if (count <= max)
						s += ", " + TeXParser.removeSubscripting(name);
				}
			}
			if (count > max)
				s += " + " + (count - max);
			// tooltip is entire list of selected columns including t or n
			String tooltip = xVar;
			for (int k = selectedColsBS.nextSetBit(0); k >= 0; k = selectedColsBS.nextSetBit(k + 1)) {
				tooltip += ", "+allColumnNames.get(k); //$NON-NLS-1$
			}
			columnsButton.setToolTipText(tooltip);
		}
		columnsButton.setText(s);

		FontSizer.setFonts(this, FontSizer.getLevel());
		pack();
	}

	/**
	 * Writes a string to a file.
	 *
	 * @param file the file
	 * @param content the string to write
	 * @return the path of the saved file or null if failed
	 */
	private String write(File file, String content) {
		if (file.exists() && !file.canWrite()) {
			JOptionPane.showMessageDialog(frame, ControlsRes.getString("Dialog.ReadOnly.Message"), //$NON-NLS-1$
					ControlsRes.getString("Dialog.ReadOnly.Title"), //$NON-NLS-1$
					JOptionPane.PLAIN_MESSAGE);
			return null;
		}
		try {
			FileOutputStream stream = new FileOutputStream(file);
			java.nio.charset.Charset charset = java.nio.charset.Charset.forName("UTF-8"); //$NON-NLS-1$
			Writer out = new BufferedWriter(new OutputStreamWriter(stream, charset));
			out.write(content);
			out.flush();
			out.close();
			if (file.exists()) {
				return XML.getAbsolutePath(file);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * A button used to set properties of the exported data
	 */
	class MyButton extends TButton {
		
		MyButton() {
			setIcon(TViewChooser.DOWN_ARROW_ICON);
			setHorizontalTextPosition(SwingConstants.LEADING);
			alwaysShowBorder(true);
			setBackground(buttonColor);
		}
		
		@Override
		public Dimension getPreferredSize() {
			Dimension dim = super.getPreferredSize();
			int w = (int)(FontSizer.getFactor() * 100);
			dim.width = Math.max(w, dim.width);
			return dim;
		}
		
	}
	
	/**
	 * A dialog to select tracks for exporting.
	 */
	@SuppressWarnings("serial")
	public class TracksDialog extends JDialog {

	  // instance fields
		private JButton okButton, selectAllButton, selectNoneButton;
		private JPanel checkboxPanel;
		private ActionListener checkboxListener;
		private TitledBorder instructions;
		private boolean allTracksSelected;

	  /**
	   * Constructs a TracksDialog.
	   *
	   * @param panel a TrackerPanel
	   */
	  public TracksDialog() {
	    super(frame, true);
	    setResizable(false);
	    createGUI();
	    refreshDisplay();
	    pack();
	  }
	  
		@Override
		public void setVisible(boolean vis) {
			BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
			if (vis)
				refreshDisplay();
			else if (selectedTracksBS.isEmpty()) {
				selectTrack(TTrack.getTrack(allTracks.get(0)), true);
				refreshGUI();
			}
			super.setVisible(vis);
		}
		
	  /**
	   * Creates the visible components of this panel.
	   */
	  private void createGUI() {
	    JPanel inspectorPanel = new JPanel(new BorderLayout());
	    setContentPane(inspectorPanel);
	    // create checkboxPanel and action listener
	    checkboxPanel = new JPanel(new GridLayout(0, 2));
	    Border etched = BorderFactory.createEtchedBorder();
	    instructions = BorderFactory.createTitledBorder(etched,""); //$NON-NLS-1$
	    checkboxPanel.setBorder(instructions);
	    inspectorPanel.add(checkboxPanel, BorderLayout.CENTER);
	    checkboxListener = new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	      	int id = Integer.parseInt(e.getActionCommand());
		      TTrack track = TTrack.getTrack(id);
	      	JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)e.getSource();
	      	selectTrack(track, checkbox.isSelected());	      	
	      	refreshDisplay();
	  			userHasSetDataMap.put(panelID, true);
	      }
	    };
	    
	    // create ok button
	    okButton = new JButton(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
	    okButton.setForeground(new Color(0, 0, 102));
	    okButton.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        setVisible(false);
	      }
	    });
	    // create selectAllButton
	    selectAllButton = new JButton(TrackerRes.getString("PlotGuestDialog.Button.SelectAll.Text")); //$NON-NLS-1$
	    selectAllButton.setForeground(new Color(0, 0, 102));
	    selectAllButton.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
			  	BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
	      	for (Integer id: allTracks) {
	      		TTrack track = TTrack.getTrack(id);
	      		if (trackType.isAssignableFrom(getTrackType(track)) ||
	      				getTrackType(track).isAssignableFrom(trackType)) {
	      			selectedTracksBS.set(id);
	      		}
	      	}
	      	refreshDisplay();
	      }
	    });
	    // create selectNoneButton
	    selectNoneButton = new JButton(TrackerRes.getString("PlotGuestDialog.Button.SelectNone.Text")); //$NON-NLS-1$
	    selectNoneButton.setForeground(new Color(0, 0, 102));
	    selectNoneButton.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	      	BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
	      	selectedTracksBS.clear();
      		trackType = null;
	      	refreshDisplay();
	      }
	    });
	    // create buttonbar at bottom
	    JPanel buttonbar = new JPanel();
	    buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
	    inspectorPanel.add(buttonbar, BorderLayout.SOUTH);
	    buttonbar.add(selectAllButton);
	    buttonbar.add(selectNoneButton);
	    buttonbar.add(okButton);
	  }

	  /**
	   * Lays out, selects and enables appropriate checkboxes.
	   */
	  protected void refreshDisplay() {
	    setTitle(TrackerRes.getString("ExportDataDialog.TracksDialog.Title"));
	    instructions.setTitle(TrackerRes.getString("ExportDataDialog.TracksDialog.Instructions")); //$NON-NLS-1$
	    // make checkboxes for all tracks in trackerPanel
	    int tracksPerRow = 3;
	    int rows = 1 + allTracks.size() / tracksPerRow;
	    checkboxPanel.setLayout(new GridLayout(rows, 0));
	    checkboxPanel.removeAll(); 
			BitSet selectedTracksBS = getSelectedTracksBitSet(panelID);
	    allTracksSelected = !selectedTracksBS.isEmpty();
	    for (int i = 0; i < allTracks.size(); i++) {
	    	TTrack next = TTrack.getTrack(allTracks.get(i));
	      JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(
	          next.getName(), next.getFootprint().getIcon(21, 16));
	      checkbox.setBorderPainted(false);
	      // check the checkbox if next is in trackNames
	      boolean selected = selectedTracksBS.get(next.getID());
	      checkbox.setSelected(selected);
	      checkbox.setEnabled(trackType == null || 
	      	trackType.isAssignableFrom(getTrackType(next)) || 
	      	getTrackType(next).isAssignableFrom(trackType));
	      if (checkbox.isEnabled()) {
	      	allTracksSelected = allTracksSelected && selected;
	      }
	      checkbox.setActionCommand(String.valueOf(next.getID()));
	      checkbox.addActionListener(checkboxListener);
	      checkboxPanel.add(checkbox);	      
	    }
	    
	    okButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
	    okButton.setEnabled(!selectedTracksBS.isEmpty()); //$NON-NLS-1$
	    
	    boolean isEnabled = !selectedTracksBS.isEmpty() && !allTracksSelected;
	    Class<? extends TTrack> type = trackType == CenterOfMass.class?
	    		PointMass.class: trackType;
	    selectAllButton.setText(TrackerRes.getString("PlotGuestDialog.Button.SelectAll.Text")
	    		+ (trackType != null? " " + type.getSimpleName(): "")); //$NON-NLS-1$
	    selectAllButton.setEnabled(isEnabled); //$NON-NLS-1$
	    selectNoneButton.setText(TrackerRes.getString("PlotGuestDialog.Button.SelectNone.Text")); //$NON-NLS-1$
	    selectNoneButton.setEnabled(!selectedTracksBS.isEmpty()); //$NON-NLS-1$
	  	
	    FontSizer.setFonts(checkboxPanel, FontSizer.getLevel());
	    pack();
	    TFrame.repaintT(this);
      ExportDataDialog.this.refreshGUI();
	  }

	}


	/**
	 * A dialog to select data columns to export
	 */
	private class ColumnsDialog extends JDialog {

		private JPanel contentPane;
		private JCheckBox[] checkBoxes;
		private JPanel columnsPanel;
		private JButton okButton;
		private TitledBorder instructions;

		private ColumnsDialog() {
			super(frame, true);
			createGUI();
		}

		private void createGUI() {
			columnsPanel = new JPanel();
			columnsPanel.setLayout(new GridLayout(0, 4));
			columnsPanel.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 4));
			
			JScrollPane columnsScroller = new JScrollPane(columnsPanel);
			Border etched = BorderFactory.createEtchedBorder();
	    instructions = BorderFactory.createTitledBorder(etched,""); //$NON-NLS-1$
	    columnsScroller.setBorder(instructions);
			
			okButton = new JButton();
			okButton.addActionListener((e) -> {
				setVisible(false);
			});
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(okButton);

			contentPane = new JPanel(new BorderLayout()) {
				@Override
				public Dimension getPreferredSize() {
					Dimension dim = super.getPreferredSize();
					dim.height = (int)(dim.height * 1.1);
					return dim;
				}
			};
			setContentPane(contentPane);
			contentPane.add(columnsScroller, BorderLayout.CENTER);
			contentPane.add(buttonPanel, BorderLayout.SOUTH);
		}

		private void refreshDisplay() {
			okButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
			setTitle(TrackerRes.getString("ExportDataDialog.ColumnsDialog.Title")); //$NON-NLS-1$
	    instructions.setTitle(TrackerRes.getString("ExportDataDialog.TracksDialog.Instructions")); //$NON-NLS-1$
	    refreshGUI();
		}

		/**
		 * Refreshes the column checkboxes.
		 */
		private void refreshCheckboxes() {
			// trackType should always be non-null here
			ArrayList<String> allColumnNames = allColumnsMap.get(trackType);
			if (checkBoxes == null || allColumnNames.size() != checkBoxes.length)
				checkBoxes = new JCheckBox[allColumnNames.size()];
			BitSet namesBS = getSelectedColumnsBitSet(trackType);
			columnsPanel.removeAll();
			for (int i = 0; i < allColumnNames.size(); i++) {
				String name = allColumnNames.get(i);
				if (checkBoxes[i] == null) {
					checkBoxes[i] = new JCheckBox();
					checkBoxes[i].setBackground(Color.white);
					checkBoxes[i].setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 0));
					checkBoxes[i].setActionCommand("" + i);
					checkBoxes[i].addActionListener(checkboxListener);
					checkBoxes[i].setOpaque(false);
				}
				checkBoxes[i].setSelected(namesBS.get(i));
				checkBoxes[i].setName(name);
				checkBoxes[i].setText(TeXParser.removeSubscripting(name));
				columnsPanel.add(checkBoxes[i]);
			}
			refreshDisplay();
		}

		private void rebuild() {
			FontSizer.setFonts(this);
			setResizable(true);
			refreshCheckboxes(); // also refreshes display and GUI
			pack();
		}
	}
		
	protected void clear() {
		frame = null;
		panelID = null;
	}
	
	@Override
	public void dispose() {
		clear();
		super.dispose();
	}


}
