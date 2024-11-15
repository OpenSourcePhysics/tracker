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
 * <https://opensourcephysics.github.io/tracker/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.DataFunction;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.tools.FontSizer;

/**
 * A Dialog for setting the format of number fields and table cells.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class NumberFormatDialog extends JDialog {
	
	// static fields
	protected static String noPattern = TrackerRes.getString("NumberFormatSetter.NoPattern"); //$NON-NLS-1$
	protected static String mixedPattern = TrackerRes.getString("NumberFormatSetter.MixedPattern"); //$NON-NLS-1$
	private static Dimension scrollerDimension = new Dimension(200, 60);
	
	private Integer panelID;
	private TFrame frame;

	// instance fields
	//TrackerPanel trackerPanel;
	
	int trackID = -1;
	JButton closeButton, helpButton, revertButton;
	JComboBox<Object> trackDropdown;
	JLabel patternLabel, sampleLabel;
	JTextField patternField;
	NumberField sampleField;
	java.text.DecimalFormat testFormat;
	String[] displayedNames = new String[0];
	Map<String, String> realNames = new HashMap<String, String>();
	Map<TTrack, TreeMap<String, String>> prevTrackPatterns = new HashMap<TTrack, TreeMap<String, String>>();
	JPanel variablePanel, applyToPanel, unitsPanel, decimalSeparatorPanel;
	JList<String> variableList = new JList<String>();
	JScrollPane variableScroller;
	JRadioButton trackOnlyButton, trackTypeButton, dimensionButton;
	JRadioButton defaultDecimalButton, periodDecimalButton, commaDecimalButton;
	String prevPattern, prevDecimalSeparator;
	TitledBorder variablesBorder, applyToBorder, decimalSeparatorBorder;
	boolean formatsChanged, prevAnglesInRadians;
	Map<Integer, String[]> trackSelectedVariables = new TreeMap<Integer, String[]>();

	/**
	 * Gets the NumberFormatDialog for a TrackerPanel and sets the track and
	 * selected variables.
	 *
	 * @param trackerPanel  the TrackerPanel
	 * @param track         the track
	 * @param selectedNames the initially selected names
	 * @return the NumberFormatDialog
	 */
	protected static NumberFormatDialog getNumberFormatDialog(TrackerPanel trackerPanel, TTrack track,
			String[] selectedNames) {
		NumberFormatDialog dialog = trackerPanel.numberFormatDialog;
		if (dialog == null) {
			trackerPanel.numberFormatDialog = dialog = new NumberFormatDialog(trackerPanel.getTFrame(),trackerPanel.getID());
			dialog.setFontLevel(FontSizer.getLevel());
			// center on screen
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - dialog.getBounds().width) / 2;
			int y = (dim.height - dialog.getBounds().height) / 2;
			dialog.setLocation(x, y);
		}
		dialog.savePrevious();

		if (selectedNames != null) {
			// replace selectedNames with appropriate display names
			HashSet<String> namesToSelect = new HashSet<String>();
			ArrayList<String> displayNames = getDisplayNames(track);
			Map<String, String[]> map = track.getFormatMap();
			for (String var : selectedNames) {
				// remove subscripts with leading space (eg time-based RGB data from LineProfile)
				int k = var.indexOf("_{ "); // note space
				if (k > 0) {
					var = var.substring(0, k);
				}
				namesToSelect.add(getDisplayName(var, displayNames, map));
			}
			selectedNames = namesToSelect.toArray(new String[0]);
		}

		if (track == null) {
			ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
			if (tracks.size() > 0) {
				track = tracks.get(0);
			} else {
				tracks = trackerPanel.getTracksTemp();
				if (tracks.size() > 0) {
					track = tracks.get(0);
				}
				trackerPanel.clearTemp();
			}
		}
		if (track != null) {
			dialog.trackSelectedVariables.put(track.getID(), selectedNames);
		}
		dialog.setTrack(track);
		dialog.setFontLevel(FontSizer.getLevel());
		return dialog;
	}

	private NumberFormatDialog(TFrame frame, Integer panelID) {
		super(frame, true);
		this.frame = frame;
		this.panelID = panelID;
		createGUI();
		refreshGUI();
	}


	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}
	
	/**
	 * Sets the variables and initially selected names displayed in this
	 * NumberFormatDialog.
	 *
	 * @param trackType the track type
	 * @param names     the variable names
	 * @param selected  the initially selected names
	 */
	private void setVariables(TTrack track, ArrayList<String> names, String[] selected) {
		// substitute THETA for any selected name starting with THETA
		if (selected != null) {
			TreeSet<String> select = new TreeSet<String>();
			for (String next : selected) {
				if (next != null && next.startsWith(Tracker.THETA)) {
					next = Tracker.THETA;
				}
				select.add(next);
			}
			selected = select.toArray(new String[select.size()]);
		}
		displayedNames = new String[names.size()];
		realNames.clear();
		// determine how much white space to add
		int len = 5;
		for (String s : names) {
			s = TeXParser.removeSubscripting(s);
			len = Math.max(len, s.length());
		}
		for (int i = 0; i < names.size(); i++) {
			String s = TeXParser.removeSubscripting(names.get(i));
			// add white space for better look
			displayedNames[i] = "   " + s; //$NON-NLS-1$
			for (int j = 0; j < len + 1 - s.length(); j++) {
				displayedNames[i] += " "; //$NON-NLS-1$
			}
			realNames.put(displayedNames[i], names.get(i));
			if (selected != null) {
				for (int j = 0; j < selected.length; j++) {
					if (selected[j] != null && selected[j].equals(names.get(i))) {
						selected[j] = displayedNames[i];
					}
				}
			}
		}

		// create variable list and add to scroller
		variableList = new JList<String>(displayedNames);
		variableList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		variableList.setVisibleRowCount(-1);
		variableList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int[] indices = variableList.getSelectedIndices();
					showNumberFormatAndSample(indices);
					String[] vars = getSelectedVariables(indices);
					trackSelectedVariables.put(trackID, vars);
					refreshGUI();
				}
			}
		});
		variableList.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int index = variableList.locationToIndex(e.getPoint());
				if (index == -1)
					return;
				String displayedName = (String) variableList.getModel().getElementAt(index);
				String name = realNames.get(displayedName);
				String desc = track.getFormatDescMap().get(name);
				variableList.setToolTipText(desc == null ? name : desc);
			}
		});
		variableScroller.setViewportView(variableList);
		FontSizer.setFonts(variableList, FontSizer.getLevel());
		int[] indices = null;
		if (selected != null) {
			// select requested names
			indices = new int[selected.length];
			for (int j = 0; j < indices.length; j++) {
				inner: for (int i = 0; i < displayedNames.length; i++) {
					if (displayedNames[i].equals(selected[j])) {
						indices[j] = i;
						break inner;
					}
				}
			}
			variableList.setSelectedIndices(indices);
		} else {
			showNumberFormatAndSample(indices);
		}
		// refresh GUI to be sure correct track type and unit dimensions are shown
		refreshGUI();
	}

	/**
	 * Applies a pattern to selected variables, tracks and/or dimensions.
	 *
	 * @param pattern the pattern
	 */
	private void applyPattern(String pattern) {
		if (pattern.equals(prevPattern))
			return;
		if (pattern.indexOf(noPattern) > -1)
			pattern = ""; //$NON-NLS-1$

		// substitute period for comma
		pattern = pattern.replaceAll(",", "."); //$NON-NLS-1$ //$NON-NLS-2$

		// clear pattern if it is part of noPattern or mixedPattern
		if (pattern.length() > 1 && (noPattern.startsWith(pattern) || mixedPattern.startsWith(pattern))) {
			pattern = ""; //$NON-NLS-1$
		}

		// substitute capital E for lower case
		pattern = pattern.replaceAll("e", "E"); //$NON-NLS-1$ //$NON-NLS-2$

		// eliminate multiple Es
		if (pattern.indexOf("E") != pattern.lastIndexOf("E")) { //$NON-NLS-1$ //$NON-NLS-2$
			pattern = pattern.substring(0, pattern.length() - 1);
		}

		if (pattern.equals("E")) { //$NON-NLS-1$
			pattern = "0E0"; //$NON-NLS-1$
		} else if (pattern.equals("0E") //$NON-NLS-1$
				|| pattern.equals("E0")) { //$NON-NLS-1$
			if (prevPattern.length() > pattern.length()) {
				pattern = "0"; //$NON-NLS-1$
			} else {
				pattern = "0E0"; //$NON-NLS-1$
			}
		} else if (pattern.contains("0.E")) { //$NON-NLS-1$
			if (prevPattern.length() > pattern.length()) {
				pattern = pattern.replaceAll("0.E", "0E"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				pattern = pattern.replaceAll("0.E", "0.0E"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		// eliminate decimal points in exponent
		if (pattern.contains("E") && pattern.endsWith("0.")) { //$NON-NLS-1$ //$NON-NLS-2$
			pattern = pattern.substring(0, pattern.length() - 1);
		}

		// don't allow ending E
		if (pattern.endsWith("0E")) { //$NON-NLS-1$
			if (prevPattern.length() > pattern.length()) {
				pattern = pattern.substring(0, pattern.length() - 1);
			} else {
				pattern = pattern.substring(0, pattern.length() - 1) + "E0"; //$NON-NLS-1$
			}
		}
		boolean validPattern = true;
		try {
			// convert E to lower case for testing
			pattern = pattern.replaceAll("0E", "0e"); //$NON-NLS-1$ //$NON-NLS-2$
			pattern = pattern.replaceAll("E0", "e0"); //$NON-NLS-1$ //$NON-NLS-2$
			testFormat.applyPattern(pattern);
			// convert to capital E
			pattern = pattern.replaceAll("0e", "0E"); //$NON-NLS-1$ //$NON-NLS-2$
			pattern = pattern.replaceAll("e0", "E0"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception ex) {
			validPattern = false;
		}
		if (validPattern) {
			try {
				// apply pattern to all selected variables
				int[] indices = variableList.getSelectedIndices();
				Object[] selected = new Object[indices.length];
				for (int j = 0; j < indices.length; j++) {
					selected[j] = displayedNames[indices[j]];
				}
				for (Object displayedName : selected) {
					String name = realNames.get(displayedName.toString());
					setFormatPattern(name, pattern);
				}
				patternField.setText(pattern);
				prevPattern = pattern;
			} catch (Exception ex) {
				patternField.setText(prevPattern);
			}
		} else { // invalid pattern
			patternField.setText(prevPattern);
		}

		TTrack track = TTrack.getTrack(trackID);
		if (track == null) {
			showNumberFormatAndSample(pattern, false);
		} else
			showNumberFormatAndSample(variableList.getSelectedIndices());
	}

	/**
	 * Gets an array of unit dimensions for all currently selected variables.
	 *
	 * @return array of unit dimensions
	 */
	private String[] getCurrentDimensions() {
		TTrack track = TTrack.getTrack(trackID);
		if (track == null)
			return new String[0];
		TreeSet<String> dimensions = new TreeSet<String>();
		int[] indices = variableList.getSelectedIndices();
		Object[] selected = new Object[indices.length];
		for (int j = 0; j < indices.length; j++) {
			selected[j] = displayedNames[indices[j]];
		}
		for (Object displayedName : selected) {
			String name = realNames.get(displayedName.toString());
			String dim = TTrack.getVariableDimensions(track, name);
			if (dim != null) {
				dimensions.add(dim);
			}
		}
		return dimensions.toArray(new String[dimensions.size()]);
	}

	/**
	 * Gets an array of display names for a specified track.
	 *
	 * @param track the track
	 * @return array of names to display to the user
	 */
	private static ArrayList<String> getDisplayNames(TTrack track) {
		if (track == null || track.tp == null)
			return new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		String[] vars = track.getFormatVariables();
		if (vars.length > 0) {
			// start with the formatter display names for track type
			for (String name : vars) {
				// skip integer variables
				if (!"I".equals(TTrack.getVariableDimensions(track, name))) { //$NON-NLS-1$
					names.add(name);
				}
			}
			// add names of data functions found in track data
			DatasetManager data = track.getData(track.tp);
			for (int i = 0, n = data.getDatasetsRaw().size(); i < n; i++) {
				Dataset dataset = data.getDataset(i);
				if (!(dataset instanceof DataFunction))
					continue;
				names.add(dataset.getYColumnName());
			}
		}

		return names;
	}

	/**
	 * Gets the display name for a specified variable name.
	 *
	 * @param var
	 * @param displayNames possible display names
	 * @param map          a Map of formatter names to variables for a given track
	 *                     type
	 * @return the display name
	 */
	private static String getDisplayName(String var, ArrayList<String> displayNames, Map<String, String[]> map) {
		if (displayNames.contains(var))
			return var;
		for (String name : map.keySet()) {
			String[] vars = map.get(name);
			if (has(vars, var))
				return name;
		}
		return var;
	}

	private static boolean has(String[] a, String v) {
		for (int i = a.length; --i >= 0;)
			if (a[i].equals(v))
				return true;
		return false;
	}

	/**
	 * Sets the track to be formatted.
	 *
	 * @param track the track
	 */
	private void setTrack(TTrack track) {
		if (track == null) {
			showNumberFormatAndSample("", false); //$NON-NLS-1$
			refreshGUI();
			return;
		}
		trackID = track.getID();
		ArrayList<String> names = getDisplayNames(track);
		String[] selected = trackSelectedVariables.get(trackID);
		setVariables(track, names, selected == null ? new String[0] : selected);
	}

	/**
	 * Saves the previous patterns for reverting.
	 */
	private void savePrevious() {
		// save previous default patterns for all types
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		TTrack.savePatterns(trackerPanel);
		// save previous patterns for all tracks
		prevTrackPatterns.clear();
		TrackerPanel panel = frame.getTrackerPanelForID(panelID);
		for (TTrack next : panel.getTracksTemp()) {
			TreeMap<String, String> patterns = new TreeMap<String, String>();
			for (String name : TTrack.getAllVariables(next.ttype)) {
				patterns.put(name, next.getVarFormatPattern(name));
			}
			prevTrackPatterns.put(next, patterns);
		}
		panel.clearTemp();
		prevAnglesInRadians = frame.isAnglesInRadians();
		prevDecimalSeparator = OSPRuntime.getPreferredDecimalSeparator();
		formatsChanged = false;
	}

	/**
	 * Sets the format pattern for a display name.
	 *
	 * @param displayName the display name
	 * @param pattern     the pattern
	 */
	private void setFormatPattern(String displayName, String pattern) {
		boolean wasChanged = formatsChanged;
		TTrack track = TTrack.getTrack(trackID);

		if (dimensionButton.isSelected()) {
			// apply to all variables with the same unit dimensions
			String dimensions = TTrack.getVariableDimensions(track, displayName);
			BitSet known = new BitSet();
			if (dimensions != null) {
				// apply to trackerPanel.formatPatterns for future tracks
				ArrayList<TTrack> tracks = track.tp.getTracksTemp();
				for (TTrack t : tracks) {
					if (known.get(t.ttype))
						continue;
					known.set(t.ttype);
					TreeMap<String, String> patterns = track.tp.getFormatPatterns(t.ttype);
					for (String nextName : patterns.keySet()) {
						if (dimensions.equals(TTrack.getVariableDimensions(t, nextName))) {
							if (!pattern.equals(patterns.get(nextName))) {
								patterns.put(nextName, pattern);
								formatsChanged = true;
							}
						}
					}
				}
				// apply to all existing tracks
				for (TTrack t : tracks) {
					boolean trackChanged = false;
					for (String nextDisplayName : getDisplayNames(track)) {
						if (dimensions.equals(TTrack.getVariableDimensions(track, nextDisplayName))) {
							if (t.setFormatPattern(nextDisplayName, pattern)) {
								trackChanged = true;
							}
						}
					}
					if (trackChanged) {
//						t.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
						formatsChanged = true;
					}
				}
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				trackerPanel.clearTemp();
			} else { // null dimensions
						// apply to this track
				if (track.setFormatPattern(displayName, pattern)) {
//					track.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
					formatsChanged = true;
				}
				// apply to all other tracks with variable of same name and null dimensions
				ArrayList<TTrack> tracks = track.tp.getTracks();
				for (TTrack t : tracks) {
					for (String var : getDisplayNames(t)) {
						if (var.equals(displayName) && TTrack.getVariableDimensions(t, displayName) == null) {
							if (t.setFormatPattern(displayName, pattern)) {
//								t.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
								formatsChanged = true;
							}
						}
					}
				}
			}
		} else if (trackTypeButton.isSelected()) {
			// apply to the variable in all tracks of same type
			String trackType = track.getBaseType();
			ArrayList<TTrack> tracks = track.tp.getTracks();
			for (TTrack t : tracks) {
				if (t.getBaseType() != trackType)
					continue;
				if (t.setFormatPattern(displayName, pattern)) {
//					t.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
					formatsChanged = true;
				}
			}
			// set pattern in trackerPanel.formatPatterns
			TreeMap<String, String> patterns = track.tp.getFormatPatterns(track.ttype);
			patterns.put(displayName, pattern);
		} else if (track.setFormatPattern(displayName, pattern)) {
//			track.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
			formatsChanged = true;
		}
		if (!wasChanged && formatsChanged) {
			refreshGUI();
		}
		if (formatsChanged)
			track.firePropertyChange(TTrack.PROPERTY_TTRACK_FORMAT, null, null); // $NON-NLS-1$			
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
//		if (!b) 
//			dispose();
	}
	/**
	 * Sets the font level of this dialog.
	 *
	 * @param level the level
	 */
	private void setFontLevel(int level) {
		FontSizer.setFonts(this, FontSizer.getLevel());
		double f = FontSizer.getFactor();
		Dimension dim = new Dimension((int) (scrollerDimension.width * f), (int) (scrollerDimension.height * f));
		variableScroller.setPreferredSize(dim);
		refreshDropdown();
		pack();
	}

	/**
	 * Creates the GUI.
	 */
	private void createGUI() {
		setLayout(new BorderLayout());
		// create test format
		testFormat = (java.text.DecimalFormat) java.text.NumberFormat.getNumberInstance();
		// create buttons
		closeButton = new JButton();
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		revertButton = new JButton();
		final Action resetAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TTrack track = TTrack.getTrack(trackID);
				
				// reset default patterns in trackerPanel.formatPatterns
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				TTrack.restorePatterns(trackerPanel);

				// reset track formats
				ArrayList<TTrack> tracks = track.tp.getTracks();
				for (TTrack next : tracks) {
					TreeMap<String, String> patterns = prevTrackPatterns.get(next);
					if (patterns != null) {
						boolean fireEvent = false;
						ArrayList<String> names = TTrack.getAllVariables(next.ttype);
						for (String name : names) {
							fireEvent = next.setFormatPattern(name, patterns.get(name)) || fireEvent;
							if (fireEvent) {
								next.firePropertyChange(TTrack.PROPERTY_TTRACK_DATA, null, null); // $NON-NLS-1$
							}
						}
					}
				}
				OSPRuntime.setPreferredDecimalSeparator(prevDecimalSeparator);
				track.tframe.setAnglesInRadians(prevAnglesInRadians);
				showNumberFormatAndSample(variableList.getSelectedIndices());
				prevPattern = ""; //$NON-NLS-1$
				formatsChanged = false;
				refreshGUI();
			}
		};
		revertButton.addActionListener(resetAction);
		helpButton = new JButton();
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String tab = "      "; //$NON-NLS-1$
				String nl = System.getProperty("line.separator", "/n"); //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.showMessageDialog(NumberFormatDialog.this,
						DisplayRes.getString("DataTable.NumberFormat.Help.Message1") + nl + //$NON-NLS-1$
				tab + DisplayRes.getString("DataTable.NumberFormat.Help.Message2") + nl + //$NON-NLS-1$
				tab + DisplayRes.getString("DataTable.NumberFormat.Help.Message3") + nl + //$NON-NLS-1$
				tab + DisplayRes.getString("DataTable.NumberFormat.Help.Message4") + nl + //$NON-NLS-1$
				tab + DisplayRes.getString("DataTable.NumberFormat.Help.Message5") + nl + nl + //$NON-NLS-1$
				DisplayRes.getString("DataTable.NumberFormat.Help.Message6") + " PI." + nl + nl + //$NON-NLS-1$ //$NON-NLS-2$
				TrackerRes.getString("NumberFormatSetter.Help.Dimensions.1") + nl + //$NON-NLS-1$
				tab + TrackerRes.getString("NumberFormatSetter.Help.Dimensions.2") + nl + //$NON-NLS-1$
				tab + TrackerRes.getString("NumberFormatSetter.Help.Dimensions.3") + nl + //$NON-NLS-1$
				tab + TrackerRes.getString("NumberFormatSetter.Help.Dimensions.4") + nl + //$NON-NLS-1$
				tab + TrackerRes.getString("NumberFormatSetter.Help.Dimensions.5") + nl + //$NON-NLS-1$
				tab + TrackerRes.getString("NumberFormatSetter.Help.Dimensions.6") + nl + //$NON-NLS-1$
				tab + TrackerRes.getString("NumberFormatSetter.Help.Dimensions.7") + nl + //$NON-NLS-1$
				tab + TrackerRes.getString("NumberFormatSetter.Help.Dimensions.8") + nl, //$NON-NLS-1$
						DisplayRes.getString("DataTable.NumberFormat.Help.Title"), //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE);
			}

		});

		// create trackDropdown early since need it for spinners
		trackDropdown = new JComboBox<Object>() {
			@Override
			public Dimension getPreferredSize() {
				Dimension dim = super.getPreferredSize();
				dim.height -= 1;
				return dim;
			}
		};
		trackDropdown.setRenderer(new TrackRenderer());
		trackDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ("refresh".equals(trackDropdown.getName())) //$NON-NLS-1$
					return;
				Object[] item = (Object[]) trackDropdown.getSelectedItem();
				if (item != null) {
					TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
					TTrack t = trackerPanel.getTrackByName(TTrack.class, (String) item[1]);
					if (t != null) {
						setTrack(t);
						refreshGUI();
					}
				}
			}
		});

		// create labels and text fields
		patternLabel = new JLabel();
		sampleLabel = new JLabel();
		patternField = new JTextField(6);
		patternField.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyPattern(patternField.getText());
			}
		});
		patternField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				final boolean revert = e.getKeyCode() != KeyEvent.VK_ENTER && e.getKeyCode() != KeyEvent.VK_0
						&& e.getKeyCode() != KeyEvent.VK_E && e.getKeyCode() != KeyEvent.VK_RIGHT
						&& e.getKeyCode() != KeyEvent.VK_LEFT && e.getKeyCode() != KeyEvent.VK_BACK_SPACE
						&& e.getKeyCode() != KeyEvent.VK_DELETE && e.getKeyCode() != KeyEvent.VK_PERIOD
						&& e.getKeyCode() != KeyEvent.VK_COMMA;

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					patternField.setBackground(Color.white);
					if (TTrack.getTrack(trackID) != null) {
						showNumberFormatAndSample(variableList.getSelectedIndices());
					} else {
						showNumberFormatAndSample(patternField.getText(), false);
					}
				} else {
					patternField.setBackground(Color.yellow);
					// apply new pattern
					Runnable runner = new Runnable() {
						@Override
						public void run() {
							if (revert)
								patternField.setText(prevPattern);
							applyPattern(patternField.getText());
						}
					};
					SwingUtilities.invokeLater(runner);
				}
			}

		});
		patternField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (patternField.getBackground() == Color.yellow) {
					patternField.setBackground(Color.white);
					patternField.getAction().actionPerformed(null);
				}
			}

		});
		sampleField = new NumberField(6);
		sampleField.setEditable(false);
		// variable scroller (list is instantiated in setVariableNames() method)
		variableScroller = new JScrollPane();
		variableScroller.setPreferredSize(scrollerDimension);
		// "apply to" buttons
		trackOnlyButton = new JRadioButton();
		trackTypeButton = new JRadioButton();
		dimensionButton = new JRadioButton();
		ButtonGroup group = new ButtonGroup();
		group.add(trackOnlyButton);
		group.add(trackTypeButton);
		group.add(dimensionButton);
		trackOnlyButton.setSelected(true);

		// decimal separator buttons
		Action decimalSeparatorAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String separator;
				if (periodDecimalButton.isSelected()) {
					separator = String.valueOf(OSPRuntime.DECIMAL_SEPARATOR_PERIOD); //$NON-NLS-1$
				} else if (commaDecimalButton.isSelected()) {
					separator = String.valueOf(OSPRuntime.DECIMAL_SEPARATOR_COMMA); //$NON-NLS-1$
				} else {
					separator = null;
				}
				OSPRuntime.setPreferredDecimalSeparator(separator);
				sampleField.refreshDecimalSeparators(true);
				showNumberFormatAndSample(variableList.getSelectedIndices());
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				trackerPanel.refreshDecimalSeparators();
				if ((prevDecimalSeparator != null && !prevDecimalSeparator.equals(separator))
						|| (separator != null && !separator.equals(prevDecimalSeparator))) {
					formatsChanged = true;
				}
				refreshGUI();
			}
		};
		defaultDecimalButton = new JRadioButton();
		defaultDecimalButton.addActionListener(decimalSeparatorAction);
		periodDecimalButton = new JRadioButton();
		periodDecimalButton.addActionListener(decimalSeparatorAction);
		commaDecimalButton = new JRadioButton();
		commaDecimalButton.addActionListener(decimalSeparatorAction);
		group = new ButtonGroup();
		group.add(defaultDecimalButton);
		group.add(periodDecimalButton);
		group.add(commaDecimalButton);

		// create borders
		variablesBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("NumberFormatSetter.ApplyToVariables.Text")); //$NON-NLS-1$
		applyToBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("NumberFormatSetter.TitledBorder.ApplyTo.Text")); //$NON-NLS-1$
		decimalSeparatorBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("NumberFormatSetter.TitledBorder.DecimalSeparator.Text")); //$NON-NLS-1$

		// assemble dialog
		JPanel formatPanel = new JPanel(new GridLayout());
		JPanel patternPanel = new JPanel();
		patternPanel.add(patternLabel);
		patternPanel.add(patternField);
		formatPanel.add(patternPanel);
		JPanel samplePanel = new JPanel();
		samplePanel.add(sampleLabel);
		samplePanel.add(sampleField);
		formatPanel.add(samplePanel);
		add(formatPanel, BorderLayout.NORTH);
		variablePanel = new JPanel(new BorderLayout());
		variablePanel.setBorder(variablesBorder);
		JPanel dropdownPanel = new JPanel();
		dropdownPanel.add(trackDropdown);
		variablePanel.add(dropdownPanel, BorderLayout.NORTH);
		variablePanel.add(variableScroller, BorderLayout.CENTER);
		add(variablePanel, BorderLayout.CENTER);
		JPanel south = new JPanel(new BorderLayout());
		add(south, BorderLayout.SOUTH);
		applyToPanel = new JPanel();
		applyToPanel.setBorder(applyToBorder);
		Box box = Box.createVerticalBox();
		box.add(trackOnlyButton);
		box.add(trackTypeButton);
		box.add(dimensionButton);
		applyToPanel.add(box);
		south.add(applyToPanel, BorderLayout.NORTH);

		decimalSeparatorPanel = new JPanel();
		decimalSeparatorPanel.setBorder(decimalSeparatorBorder);
		decimalSeparatorPanel.add(defaultDecimalButton);
		decimalSeparatorPanel.add(commaDecimalButton);
		decimalSeparatorPanel.add(periodDecimalButton);
		south.add(decimalSeparatorPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(helpButton);
		buttonPanel.add(revertButton);
		buttonPanel.add(closeButton);
		south.add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Refreshes the GUI strings.
	 */
	private void refreshGUI() {
		setTitle(TrackerRes.getString("NumberFormatSetter.Title")); //$NON-NLS-1$
		noPattern = TrackerRes.getString("NumberFormatSetter.NoPattern"); //$NON-NLS-1$
		mixedPattern = TrackerRes.getString("NumberFormatSetter.MixedPattern"); //$NON-NLS-1$
		closeButton.setText(DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
		revertButton.setText(TrackerRes.getString("NumberFormatSetter.Button.Revert")); //$NON-NLS-1$
		revertButton.setEnabled(formatsChanged);
		helpButton.setText(DisplayRes.getString("GUIUtils.Help")); //$NON-NLS-1$
		patternLabel.setText(DisplayRes.getString("DataTable.NumberFormat.Dialog.Label.Format")); //$NON-NLS-1$
		sampleLabel.setText(DisplayRes.getString("DataTable.NumberFormat.Dialog.Label.Sample")); //$NON-NLS-1$
		defaultDecimalButton.setText(TrackerRes.getString("NumberFormatSetter.Button.DecimalSeparator.Default")); //$NON-NLS-1$
		periodDecimalButton.setText(TrackerRes.getString("NumberFormatSetter.Button.DecimalSeparator.Period")); //$NON-NLS-1$
		commaDecimalButton.setText(TrackerRes.getString("NumberFormatSetter.Button.DecimalSeparator.Comma")); //$NON-NLS-1$
		defaultDecimalButton.setSelected(OSPRuntime.getPreferredDecimalSeparator() == null);
		periodDecimalButton.setSelected(String.valueOf(OSPRuntime.DECIMAL_SEPARATOR_PERIOD)
				.equals(OSPRuntime.getPreferredDecimalSeparator())); //$NON-NLS-1$
		commaDecimalButton.setSelected(String.valueOf(OSPRuntime.DECIMAL_SEPARATOR_COMMA)
				.equals(OSPRuntime.getPreferredDecimalSeparator())); //$NON-NLS-1$

		TTrack track = TTrack.getTrack(trackID);
		refreshDropdown();
		String trackName = track == null ? "" : track.getName(); //$NON-NLS-1$
		String trackType = track == null ? null : getTrackType(track);
		String trackTypeName = trackType == null ? "" : trackType; //$NON-NLS-1$

		String s = TrackerRes.getString("NumberFormatSetter.Button.ApplyToTrackOnly.Text"); //$NON-NLS-1$
		trackOnlyButton.setText(s + (track == null ? "" : " (" + trackName + ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		s = TrackerRes.getString("NumberFormatSetter.Button.ApplyToTrackType.Text"); //$NON-NLS-1$
		trackTypeButton.setText(s + " " + trackTypeName); //$NON-NLS-1$
		s = TrackerRes.getString("NumberFormatSetter.Button.ApplyToDimension.Text"); //$NON-NLS-1$
		String[] dimensions = getCurrentDimensions();
		if (dimensions.length == 0) {
			dimensionButton.setText(s);
		} else if (dimensions.length == 1 && !"".equals(dimensions[0])) { //$NON-NLS-1$
			dimensionButton.setText(s + " \"" + dimensions[0] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			String dim = dimensions[0];
			for (int i = 1; i < dimensions.length; i++) {
				if ((dim + ", " + dimensions[i]).length() > 7) { //$NON-NLS-1$
					dim += ", " + TrackerRes.getString("NumberFormatSetter.DimensionList.More"); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				}
				dim += ", " + dimensions[i]; //$NON-NLS-1$
			}
			dimensionButton.setText(s + " " + dim); //$NON-NLS-1$
		}
		trackOnlyButton.setEnabled(track != null);
		trackTypeButton.setEnabled(track != null);
		dimensionButton.setEnabled(track != null);
		// set border titles
		variablesBorder.setTitle(TrackerRes.getString("NumberFormatSetter.ApplyToVariables.Text")); //$NON-NLS-1$
		applyToBorder.setTitle(TrackerRes.getString("NumberFormatSetter.TitledBorder.ApplyTo.Text")); //$NON-NLS-1$
		decimalSeparatorBorder.setTitle(TrackerRes.getString("NumberFormatSetter.TitledBorder.DecimalSeparator.Text")); //$NON-NLS-1$
		Dimension dim = getSize();
		if (dim.width > getMinimumSize().width) {
			setSize(dim);
		} else {
			pack();
		}
	}

	/**
	 * Refreshes the dropdown list.
	 */
	private void refreshDropdown() {
		// refresh trackDropdown
		Object toSelect = null;
		trackDropdown.setName("refresh"); //$NON-NLS-1$
		trackDropdown.removeAllItems();
		TTrack track = TTrack.getTrack(trackID);
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		for (TTrack next : trackerPanel.getTracksTemp()) {
			Icon icon = next.getFootprint().getIcon(21, 16);
			Object[] item = new Object[] { icon, next.getName() };
			trackDropdown.addItem(item);
			if (next == track) {
				toSelect = item;
			}
		}
		trackerPanel.clearTemp();
		if (toSelect == null) {
			Object[] emptyItem = new Object[] { null, "           " }; //$NON-NLS-1$
			trackDropdown.insertItemAt(emptyItem, 0);
			toSelect = emptyItem;
		}
		// select desired item
		trackDropdown.setSelectedItem(toSelect);
		trackDropdown.setName(null);
	}

	/**
	 * Displays the format pattern associated with the selected variable indices and
	 * the number PI formatted with the pattern. If the variables have different
	 * patterns, then both the pattern and PI fields are left blank.
	 *
	 * @param selectedIndices the indices of the selected variables in the list
	 */
	private void showNumberFormatAndSample(int[] selectedIndices) {
		TTrack track = TTrack.getTrack(trackID);
		if (selectedIndices == null || selectedIndices.length == 0) {
			showNumberFormatAndSample("", false); //$NON-NLS-1$
		} else if (selectedIndices.length == 1) {
			String name = realNames.get(displayedNames[selectedIndices[0]]);
			String pattern = track.getVarFormatPattern(name);
			boolean degrees = name.startsWith(Tracker.THETA) && !track.tframe.isAnglesInRadians();
			showNumberFormatAndSample(pattern, degrees);
		} else {
			// do all selected indices have same pattern?
			String name = realNames.get(displayedNames[selectedIndices[0]]);
			boolean degrees = name.startsWith(Tracker.THETA) && !track.tframe.isAnglesInRadians();
			String pattern = track.getVarFormatPattern(name);
			if (degrees && (pattern == null || "".equals(pattern))) { //$NON-NLS-1$
				pattern = NumberField.DECIMAL_1_PATTERN;
			}
			for (int i = 1; i < selectedIndices.length; i++) {
				name = realNames.get(displayedNames[selectedIndices[i]]);
				degrees = degrees && name.startsWith(Tracker.THETA);
				String selectedPattern = track.getVarFormatPattern(name);
				if (degrees && (selectedPattern == null || "".equals(selectedPattern))) { //$NON-NLS-1$
					selectedPattern = NumberField.DECIMAL_1_PATTERN;
				}
				if (!pattern.equals(selectedPattern)) {
					pattern = null;
					break;
				}
			}
			if (degrees && NumberField.DECIMAL_1_PATTERN.equals(pattern)) {
				pattern = ""; //$NON-NLS-1$
			}
			showNumberFormatAndSample(pattern, degrees);
		}

	}

	/**
	 * Gets the names of the currently selected variables.
	 *
	 * @param selectedIndices the indices of the selected variables in the list
	 */
	private String[] getSelectedVariables(int[] selectedIndices) {
		if (selectedIndices == null) {
			return new String[0];
		}
		String[] selectedNames = new String[selectedIndices.length];
		for (int i = 0; i < selectedIndices.length; i++) {
			selectedNames[i] = realNames.get(displayedNames[selectedIndices[i]]);
		}
		return selectedNames;
	}

	/**
	 * Displays a specified format pattern and the number PI formatted with the
	 * pattern.
	 *
	 * @param pattern the format pattern
	 * @param degrees true to show PI as 180 degrees
	 */
	private void showNumberFormatAndSample(String pattern, boolean degrees) {
		if (pattern == null) {
			sampleField.setText(""); //$NON-NLS-1$
			patternField.setText(mixedPattern);
			return;
		}

		boolean none = pattern.equals("") || pattern.equals(noPattern); //$NON-NLS-1$
		sampleField.setFixedPattern(!none ? pattern : degrees ? NumberField.DECIMAL_1_PATTERN : null);
		sampleField.setUnits(degrees ? Tracker.DEGREES : null);
		sampleField.setValue(degrees ? 180 : Math.PI);
		if (patternField.getBackground().equals(Color.WHITE)) {
			patternField.setText(none ? noPattern : pattern);
		}
	}

	/**
	 * Determines the class associated with a given track.
	 * 
	 * @param track the track
	 * @return the class
	 */
	protected static String getTrackType(TTrack track) {
		return track.getBaseType();
	}

	@Override
	public void dispose() {
		clear();
		super.dispose();
		
	}

	public void clear() {
		setVisible(false);
		frame = null;
		panelID = null;
	}
	

}
