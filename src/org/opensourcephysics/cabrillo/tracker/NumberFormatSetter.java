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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opensourcephysics.cabrillo.tracker.TableTrackView.TrackDataTable;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.NumberField;

/**
 * A Dialog for setting the format of number fields and table cells
 * that display track variables.
 *
 * @author Douglas Brown
 */
public class NumberFormatSetter extends JDialog {

  static final String NO_PATTERN = TrackerRes.getString("NumberFormatSetter.NoPattern"); //$NON-NLS-1$
  static Map<Class<? extends TTrack>, TreeMap<String, String>> defaultFormatPatterns;
  static TFrame frame;
  private static NumberFormatSetter formatSetter;
  static {
		// set default number patterns
		defaultFormatPatterns = new HashMap<Class<? extends TTrack>, TreeMap<String,String>>();
		HashSet<Class<? extends TTrack>> types = new HashSet<Class<? extends TTrack>>();
		types.add(CoordAxes.class);
		types.add(PointMass.class);
		types.add(Vector.class);
		types.add(LineProfile.class);
		types.add(RGBRegion.class);
		types.add(TapeMeasure.class);
		types.add(Protractor.class);
		types.add(CircleFitter.class);
		types.add(Calibration.class);
		types.add(OffsetOrigin.class);
		for (Class<? extends TTrack> trackType: types) {
			TreeMap<String, String> patterns = new TreeMap<String, String>();
			boolean isLine = LineProfile.class==trackType;
			boolean isRGB = isLine || RGBRegion.class==trackType;
			boolean isStep = RGBRegion.class==trackType || PointMass.class==trackType
					|| Vector.class==trackType || Protractor.class==trackType
					|| CircleFitter.class==trackType || TapeMeasure.class==trackType;
			if (isStep) {
				patterns.put("t", NumberField.DECIMAL_3_PATTERN); //$NON-NLS-1$
				patterns.put("step", NumberField.INTEGER_PATTERN); //$NON-NLS-1$
				patterns.put("frame", NumberField.INTEGER_PATTERN); //$NON-NLS-1$
			}
			if (isLine) {
				patterns.put("n", NumberField.INTEGER_PATTERN); //$NON-NLS-1$
			}
			if (isRGB) {
	  		patterns.put("pixels", NumberField.INTEGER_PATTERN); //$NON-NLS-1$  		
	  		patterns.put("R", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
	  		patterns.put("G", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
	  		patterns.put("B", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
	  		patterns.put("luma", NumberField.DECIMAL_1_PATTERN); //$NON-NLS-1$
			}
			defaultFormatPatterns.put(trackType, patterns);
		}
  }

  int trackID = -1;
  JButton closeButton, cancelButton, helpButton, resetButton;
  JLabel patternLabel, sampleLabel;
  JTextField patternField;
  NumberField sampleField;
  java.text.DecimalFormat testFormat;
  String[] displayedNames;
  Map<String, String> realNames = new HashMap<String, String>();
  Map<String, String> prevDefaultPatterns = new HashMap<String, String>();
  Map<TTrack, TreeMap<String, String>> prevTrackPatterns 
  	= new HashMap<TTrack, TreeMap<String, String>>();
  JPanel variablePanel;
  JList variableList;
  JScrollPane variableScroller;
  JCheckBox applyToAllCheckbox;
  String prevPattern;

  /**
   * Gets the format setter.
   *
   * @param track the track
   * @param selectedNames the initially selected names
   * @return the format setter
   */
  public static NumberFormatSetter getFormatSetter(TTrack track, String[] selectedNames) {
    if(formatSetter==null) {
    	formatSetter = new NumberFormatSetter(track);
      // center on screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width - formatSetter.getBounds().width) / 2;
      int y = (dim.height - formatSetter.getBounds().height) / 2;
      formatSetter.setLocation(x, y);
    }
  	formatSetter.trackID = track.getID();
    String[] names = getVariableNames(track);
    formatSetter.setVariables(names, selectedNames);
    return formatSetter;
  }

  /**
   * Private constructor.
   *
   * @param track a track
   */
  private NumberFormatSetter(TTrack track) {
    super(JOptionPane.getFrameForComponent(track.trackerPanel), true);
  	trackID = track.getID();
    setLayout(new BorderLayout());
    // create test format
    testFormat = (java.text.DecimalFormat) java.text.NumberFormat.getNumberInstance();
    // create buttons
    closeButton = new JButton(); 
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    resetButton = new JButton();
    final Action resetAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
      	TTrack track = TTrack.getTrack(trackID);
        for(String displayedName : displayedNames) {
        	String name = realNames.get(displayedName);
      		// reset pattern in trackerPanel.formatPatterns
      		Class<? extends TTrack> trackType = getTrackType(track);
      		TreeMap<String, String> patterns = track.trackerPanel.formatPatterns.get(trackType);
      		if (patterns!=null) {
      			patterns.put(name, prevDefaultPatterns.get(name));
      		}
        	// reset track formats
      		ArrayList<TTrack> tracks = track.trackerPanel.getTracks();
      		for (TTrack next: tracks) {
      			if (!next.getClass().isAssignableFrom(trackType)) continue;
          	patterns = prevTrackPatterns.get(next);
            if (patterns!=null && setFormatPattern(next, name, patterns.get(name))) {
            	next.firePropertyChange("data", null, null); //$NON-NLS-1$
            }
      		}
        }
    		showNumberFormatAndSample(variableList.getSelectedIndices());
    		prevPattern = ""; //$NON-NLS-1$
			}
    };
    resetButton.addActionListener(resetAction);
    cancelButton = new JButton(); 
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetAction.actionPerformed(null);
        setVisible(false);
      }
    });
    helpButton = new JButton(); 
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String tab = "      ";                                                                                          //$NON-NLS-1$
        String nl = System.getProperty("line.separator", "/n");                                                         //$NON-NLS-1$ //$NON-NLS-2$
        JOptionPane.showMessageDialog(NumberFormatSetter.this, DisplayRes.getString("DataTable.NumberFormat.Help.Message1")+nl+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message2")+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message3")+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message4")+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message5")+nl+nl+ //$NON-NLS-1$
          DisplayRes.getString("DataTable.NumberFormat.Help.Message6")+" PI.", //$NON-NLS-1$ //$NON-NLS-2$
          DisplayRes.getString("DataTable.NumberFormat.Help.Title"), //$NON-NLS-1$
          JOptionPane.INFORMATION_MESSAGE);
      }

    });
    // create labels and text fields
    patternLabel = new JLabel(); 
    sampleLabel = new JLabel();  
    patternField = new JTextField(6);
    patternField.setAction(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
        applyPattern(patternField.getText());
      }
    });
    patternField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          patternField.setBackground(Color.white);
      		showNumberFormatAndSample(variableList.getSelectedIndices());
        } 
        else {
          patternField.setBackground(Color.yellow);
          // apply new pattern
          Runnable runner = new Runnable() {
            public void run() {
		          applyPattern(patternField.getText());
            }
          };
          SwingUtilities.invokeLater(runner);
        }
      }

    });
    patternField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
  			if (patternField.getBackground()==Color.yellow) {
	        patternField.setBackground(Color.white);
	        patternField.getAction().actionPerformed(null);
  			}
      }

    });
    sampleField = new NumberField(6);
    sampleField.setEditable(false);
    // variable scroller (list is instantiated in setVariableNames() method)
    variableScroller = new JScrollPane();
    variableScroller.setPreferredSize(new Dimension(160, 160));
    // apply to all checkbox
    applyToAllCheckbox = new JCheckBox();
    applyToAllCheckbox.setSelected(true);
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
    variablePanel.add(variableScroller, BorderLayout.CENTER);
    add(variablePanel, BorderLayout.CENTER);
    JPanel south = new JPanel(new BorderLayout());
    add(south, BorderLayout.SOUTH);
    JPanel applyToAllPanel = new JPanel();
    applyToAllPanel.add(applyToAllCheckbox);
    south.add(applyToAllPanel, BorderLayout.NORTH);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(helpButton);
    buttonPanel.add(resetButton);
    buttonPanel.add(closeButton);
    buttonPanel.add(cancelButton);
    south.add(buttonPanel, BorderLayout.SOUTH);
    refreshGUI();
    pack();
  }

  /**
   * Sets the variables and initially selected names displayed in this NumberFormatSetter.
   *
   * @param names the variable names
   * @param selected the intially selected names
   */
  protected void setVariables(String[] names, String[] selected) {
  	TTrack track = TTrack.getTrack(trackID);
    displayedNames = new String[names.length];
    realNames.clear();
  	for (int i=0; i<names.length; i++) {
      String s = TeXParser.removeSubscripting(names[i]);
  		// add white space for better look
  		displayedNames[i] = "   "+s+" "; //$NON-NLS-1$ //$NON-NLS-2$
  		realNames.put(displayedNames[i], names[i]);
    	if (selected!=null) {
	    	for (int j=0; j<selected.length; j++) {
	    		if (selected[j]!=null && selected[j].equals(names[i])) {
	    			selected[j] = displayedNames[i];
	    		}
	    	}
    	}
  	}
  	// save previous default format patterns
    prevDefaultPatterns.clear();
		Class<? extends TTrack> trackType = getTrackType(track);
		TreeMap<String, String> patterns = track.trackerPanel.formatPatterns.get(trackType);
		if (patterns!=null) {
			prevDefaultPatterns.putAll(patterns);
		}

  	// save previous track format patterns
    prevTrackPatterns.clear();
		ArrayList<TTrack> tracks = track.trackerPanel.getTracks();
		for (TTrack next: tracks) {
			if (!next.getClass().isAssignableFrom(trackType)) continue;
			patterns = new TreeMap<String, String>(); 
	    for(String name : names) {
	      patterns.put(name, getFormatPattern(next, name));
	    }
	    prevTrackPatterns.put(next, patterns);
    }

    // create variable list and add to scroller
    variableList = new JList(displayedNames);
    variableList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    variableList.setVisibleRowCount(-1);
    variableList.addListSelectionListener(new ListSelectionListener() {
    	public void valueChanged(ListSelectionEvent e) {
    		if (!e.getValueIsAdjusting()) {
      		showNumberFormatAndSample(variableList.getSelectedIndices());
    		}
    	}
    });
    variableScroller.setViewportView(variableList);
    pack();
    int[] indices = null;
    if (selected!=null) {
      // select requested names
      indices = new int[selected.length];
      for (int j=0; j<indices.length; j++) {
      	inner:
	      for (int i = 0; i< displayedNames.length; i++) {
	      	if (displayedNames[i].equals(selected[j])) {
	      		indices[j] = i;
	      		break inner;
	      	}
	      }
      }
    	variableList.setSelectedIndices(indices);
    }
    else {
      showNumberFormatAndSample(indices);
    }
    // refresh GUI to be sure correct track type is shown
    refreshGUI();
  }
  
  private void applyPattern(String pattern) {
    if (pattern.equals(prevPattern)) return;
    if (pattern.indexOf(NO_PATTERN)>-1)
    	pattern = ""; //$NON-NLS-1$
    // substitute 0 for other digits
    for (int i = 1; i< 10; i++) {
    	pattern = pattern.replaceAll(String.valueOf(i), "0"); //$NON-NLS-1$
    }
    if (pattern.equalsIgnoreCase("e")) { //$NON-NLS-1$
    	pattern = "0E0"; //$NON-NLS-1$
    }
    else if (pattern.equalsIgnoreCase("0e") //$NON-NLS-1$
    		|| pattern.equalsIgnoreCase("e0")) { //$NON-NLS-1$
    	if (prevPattern.length()>pattern.length()) {
    		pattern = "0"; //$NON-NLS-1$
    	}
    	else {
    		pattern = "0E0"; //$NON-NLS-1$
    	}
    }
    if (pattern.toLowerCase().endsWith("0e")) { //$NON-NLS-1$
    	if (prevPattern.length()>pattern.length()) {
      	pattern = pattern.substring(0, pattern.length()-1);
    	}
    	else {
      	pattern = pattern.substring(0, pattern.length()-1)+"E0"; //$NON-NLS-1$
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
				for (int j=0; j<indices.length; j++) {
					selected[j] = displayedNames[indices[j]];
				}
				for(Object displayedName : selected) {
					String name = realNames.get(displayedName.toString());
				  setFormatPattern(name, pattern);
				}
				patternField.setText(pattern);
				prevPattern = pattern;
			} catch (Exception ex) {
	    	patternField.setText(prevPattern);
			}
    }
    else { // invalid pattern
    	patternField.setText(prevPattern);
    }
		showNumberFormatAndSample(variableList.getSelectedIndices());
  }

  /**
   * Gets an array of all variable names for a specified track.
   *
   * @param track the track
   * @return array of variable names
   */
  protected static String[] getVariableNames(TTrack track) {
  	if (track==null || track.trackerPanel==null) return new String[0];
  	if (track.getVariableList()!=null) {
  		return track.getVariableList();
  	}
  	ArrayList<String> names = new ArrayList<String>();
  	
  	if (track.isViewable()) {
	  	DatasetManager data = track.getData(track.trackerPanel);
	  	// add independent variable
	    Dataset dataset = data.getDataset(0);
	    String name = dataset.getXColumnName();
	    names.add(name);
	    // then add other variables
	    ArrayList<Integer> dataOrder = track.getPreferredDataOrder();
	    ArrayList<Integer> added = new ArrayList<Integer>();
	    // first add in preferred order
	  	for (int i = 0; i < dataOrder.size(); i++) {
	  		dataset = data.getDataset(dataOrder.get(i));
	      name = dataset.getYColumnName();
	      names.add(name);
	    	added.add(dataOrder.get(i));
	    }
	  	// then add any that were missed
			for (int i = 0; i<data.getDatasets().size(); i++) {
				if (!added.contains(i)) {
		  		dataset = data.getDataset(i);
		      name = dataset.getYColumnName();
		      names.add(name);
				}
			}
  	}
  	// finally add fields that are NOT yet in list
  	Map<String, NumberField[]> numberFields = track.getNumberFields();
  	for (String key: numberFields.keySet()) {
  		if (!names.contains(key)) {
  			names.add(key);
  		}
  	}
    return names.toArray(new String[0]);
  }

  /**
   * Gets the format pattern for a specified track and variable.
   *
   * @param track the track
   * @param name the variable name
   * @return the pattern
   */
  protected static String getFormatPattern(TTrack track, String name) {
  	// get pattern from track NumberField if possible
  	NumberField[] fields = track.getNumberFields().get(name);
  	if (fields!=null) {
  		return fields[0].getFixedPattern();
  	}
  	// get pattern from table if no NumberField
  	DataTable table = getDataTable(track);
  	return table==null? "": table.getFormatPattern(name); //$NON-NLS-1$
  }

  /**
   * Gets the format patterns for a specified track.
   *
   * @param track the track
   * @return array with variable names and patterns
   */
  protected static String[] getFormatPatterns(TTrack track) {
  	ArrayList<String> patterns = new ArrayList<String>();
  	for (String name: getVariableNames(track)) {
  		patterns.add(name);
  		patterns.add(getFormatPattern(track, name));
  	}
  	return patterns.toArray(new String[patterns.size()]);
  }

  /**
   * Sets the format pattern for a specified track and variable.
   *
   * @param track the track
   * @param name the variable name
   * @param pattern the pattern
   * @return true if any changes were made
   */
  protected static boolean setFormatPattern(TTrack track, String name, String pattern) {
  	ArrayList<TableTrackView> tableViews = getTableViews(track);
  	boolean changed = false;
  	if (track.isViewable()) {
	  	// set pattern in track tables
	  	for (TableTrackView view: tableViews) {
	  		if (view==null) continue;
	    	DataTable table = view.getDataTable();
	    	if (!table.getFormatPattern(name).equals(pattern)) {
	    		table.setFormatPattern(name, pattern);
	    		changed = true;
	    	}
	  	}
  	}
  	// set pattern in track NumberFields
  	NumberField[] fields = track.getNumberFields().get(name);
  	if (fields!=null) {
	  	for (NumberField field: fields) {
	  		if (!field.getFixedPattern().equals(pattern)) {
	  			field.setFixedPattern(pattern);
	  			changed = true;
	  		}
	  	}
  	}
  	return changed;
  }
  
  /**
   * Gets the custom format patterns for a specified track.
   *
   * @param track the track
   * @return array with variable names and custom patterns
   */
  protected static String[] getCustomFormatPatterns(TTrack track) {
  	String[] patterns = getFormatPatterns(track);
  	Class<? extends TTrack> type = track.getClass();
		TreeMap<String, String> map = track.trackerPanel.formatPatterns.get(type);
  	ArrayList<String> customPatterns = new ArrayList<String>();
  	if (map!=null) {
	  	for (int i=0; i<patterns.length-1; i=i+2) {
	  		String name = patterns[i];
	  		String pattern = map.get(name)==null? "": map.get(name); //$NON-NLS-1$
	  		if (!pattern.equals(patterns[i+1])) {
	  			customPatterns.add(name);
	  			customPatterns.add(patterns[i+1]);
	  		}
	  	} 
  	}
  	return customPatterns.toArray(new String[customPatterns.size()]);
  }

  /**
   * Gets the custom format patterns for a specified track.
   *
   * @param track the track
   * @return array with variable names and custom patterns
   */
  protected static String[][] getCustomFormatPatterns(TrackerPanel trackerPanel) {
    ArrayList<String[]> formats = new ArrayList<String[]>();
    // look at all track types defined in defaultFormatPatterns
    for (Class<? extends TTrack> type: NumberFormatSetter.defaultFormatPatterns.keySet()) {    	
    	TreeMap<String, String> defaultPatterns = NumberFormatSetter.defaultFormatPatterns.get(type);
    	TreeMap<String, String> patterns = trackerPanel.formatPatterns.get(type);
    	// if patterns are null then go to next track type
    	if (patterns==null) continue;
    	ArrayList<String> customPatterns = new ArrayList<String>();
    	for (String name: defaultPatterns.keySet()) {
    		String defaultPattern = defaultPatterns.get(name);
    		String pattern = patterns.get(name);
    		if (!defaultPattern.equals(pattern)) {
    			if (customPatterns.isEmpty()) {
    				customPatterns.add(type.getName());
    			}
    			customPatterns.add(name);
    			customPatterns.add(pattern==null? "": pattern); //$NON-NLS-1$
    		}
    	} 
    	for (String name: patterns.keySet()) {
    		String defaultPattern = defaultPatterns.get(name);
    		String pattern = patterns.get(name);
    		if (!pattern.equals(defaultPattern) && !customPatterns.contains(name)) {
    			if (customPatterns.isEmpty()) {
    				customPatterns.add(type.getName());
    			}
    			customPatterns.add(name);
    			customPatterns.add(pattern);
    		}
    	}
    	if (!customPatterns.isEmpty()) {
    		formats.add(customPatterns.toArray(new String[customPatterns.size()]));
    	}
    }
    return formats.toArray(new String[formats.size()][]);
  }

  /**
   * Sets the format pattern for a named variable.
   *
   * @param name the variable name
   * @param pattern the pattern
   */
  protected void setFormatPattern(String name, String pattern) {
  	TTrack track = TTrack.getTrack(trackID);
  	setFormatPattern(track, name, pattern);
    track.firePropertyChange("data", null, null); //$NON-NLS-1$
  	
  	if (applyToAllCheckbox.isSelected()) {
  		Class<? extends TTrack> trackType = getTrackType(track);
  		ArrayList<TTrack> tracks = track.trackerPanel.getTracks();
	  	// set pattern in tables and fields of other tracks of same type 
  		for (TTrack next: tracks) {
  			if (next==track) continue; // don't re-apply to selected track
  			if (!next.getClass().isAssignableFrom(trackType)) continue;
  			setFormatPattern(next, name, pattern);
  	    next.firePropertyChange("data", null, null); //$NON-NLS-1$
  		}
  		// set pattern in trackerPanel.formatPatterns
  		TreeMap<String, String> patterns = track.trackerPanel.formatPatterns.get(trackType);
  		if (patterns!=null) {
  			patterns.put(name, pattern);
  		}
  	}
  }
  
  /**
   * Gets all table views for a specified track.
   *
   * @param track the track
   * @return ArrayList of table views
   */
  protected static ArrayList<TableTrackView> getTableViews(TTrack track) {
  	ArrayList<TableTrackView> dataViews = new ArrayList<TableTrackView>();
  	if (track==null || track.trackerPanel==null || track.trackerPanel.getTFrame()==null)
  		return dataViews;
    Container[] c = track.trackerPanel.getTFrame().getViews(track.trackerPanel);
    for (int i = 0; i < c.length; i++) {
    	if (c[i] instanceof TViewChooser) {
        TViewChooser chooser = (TViewChooser)c[i];
        for (TView tview: chooser.getViews()) {
	        if (tview instanceof TableTView) {
	          TableTView tableView = (TableTView)tview;
	          TableTrackView trackView = (TableTrackView)tableView.getTrackView(track);
	    			dataViews.add(trackView);
	        }
        }
      }
    }
    return dataViews;
  }
  
  /**
   * Refreshes the GUI strings.
   */
  protected void refreshGUI() {
    TTrack track = TTrack.getTrack(trackID);
  	String trackName = track==null? "": track.getName(); //$NON-NLS-1$
    setTitle(DisplayRes.getString("DataTable.NumberFormat.Dialog.Title")+":  "+trackName); //$NON-NLS-1$ //$NON-NLS-2$
    closeButton.setText(DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
    resetButton.setText(TrackerRes.getString("AutoTracker.Wizard.Button.Reset")); //$NON-NLS-1$
    cancelButton.setText(DisplayRes.getString("GUIUtils.Cancel")); //$NON-NLS-1$
    helpButton.setText(DisplayRes.getString("GUIUtils.Help")); //$NON-NLS-1$
    patternLabel.setText(DisplayRes.getString("DataTable.NumberFormat.Dialog.Label.Format")); //$NON-NLS-1$
    sampleLabel.setText(DisplayRes.getString("DataTable.NumberFormat.Dialog.Label.Sample"));  //$NON-NLS-1$
    variablePanel.setBorder(BorderFactory.createTitledBorder(
    		TrackerRes.getString("NumberFormatSetter.ApplyToVariables.Text"))); //$NON-NLS-1$
	  String s = TrackerRes.getString("NumberFormatSetter.ApplyToTracksOfType.Text"); //$NON-NLS-1$
    if (track!=null) {
  		Class<? extends TTrack> trackType = getTrackType(track);
	    s += " "+trackType.getSimpleName(); //$NON-NLS-1$
    }
    applyToAllCheckbox.setText(s);
  }

  /**
   * Gets a DataTable associated with a specified track.
   *
   * @param track the track
   * @return the DataTable
   */
  protected static TrackDataTable getDataTable(TTrack track) {
  	ArrayList<TableTrackView> tableViews = getTableViews(track);
  	if (tableViews.isEmpty()) return null;
  	TableTrackView view = tableViews.get(0);
  	if (view==null) return null;
  	return view.getDataTable();
  }

  /**
   * Displays the format pattern associated with the selected variable indices
   * and the number PI formatted with the pattern. If the variables have
   * different patterns, then both the pattern and PI fields are left blank.
   *
   * @param selectedIndices the indices of the selected variables in the list
   */
  private void showNumberFormatAndSample(int[] selectedIndices) {
  	TTrack track = TTrack.getTrack(trackID);
    if (selectedIndices==null || selectedIndices.length==0) {
    	showNumberFormatAndSample("", false); //$NON-NLS-1$
    }
    else if (selectedIndices.length==1) {
    	String name = realNames.get(displayedNames[selectedIndices[0]]);
    	String pattern = getFormatPattern(track, name);
      boolean degrees = name.startsWith(Tracker.THETA) && !track.trackerPanel.getTFrame().anglesInRadians;
    	showNumberFormatAndSample(pattern, degrees);
    }
    else {
    	// do all selected indices have same pattern?
    	String name = realNames.get(displayedNames[selectedIndices[0]]);
      boolean degrees = name.startsWith(Tracker.THETA) && !track.trackerPanel.getTFrame().anglesInRadians;       		
    	String pattern = getFormatPattern(track, name);
      if (degrees && (pattern==null || "".equals(pattern))) { //$NON-NLS-1$
      	pattern = NumberField.DECIMAL_1_PATTERN;
      }
    	for (int i=1; i<selectedIndices.length; i++) {
      	name = realNames.get(displayedNames[selectedIndices[i]]);
        degrees = degrees && name.startsWith(Tracker.THETA); 
        String selectedPattern = getFormatPattern(track, name);
        if (degrees && (selectedPattern==null || "".equals(selectedPattern))) { //$NON-NLS-1$
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
   * Displays a specified format pattern and the number PI formatted with the pattern.
   *
   * @param pattern the format pattern
   * @param degrees true to show PI as 180 degrees
   */
  private void showNumberFormatAndSample(String pattern, boolean degrees) {
  	if (pattern==null) {
      sampleField.setText(""); //$NON-NLS-1$
      patternField.setText(""); //$NON-NLS-1$
      return;
  	}
  	
  	boolean noPattern = pattern.equals("") || pattern.equals(NO_PATTERN); //$NON-NLS-1$    
		sampleField.setFixedPattern(!noPattern? pattern: degrees? NumberField.DECIMAL_1_PATTERN: null);
    sampleField.setUnits(degrees? Tracker.DEGREES: null);
		sampleField.setValue(degrees? 180: Math.PI);
    if (patternField.getBackground().equals(Color.WHITE)) {
    	patternField.setText(noPattern? NO_PATTERN: pattern);
    }   
  }

  /**
   * Determines the class associated with a given track.
   * 
   * @param track the track
   * @return the class
   */
  protected static Class<? extends TTrack> getTrackType(TTrack track) {
		Class<? extends TTrack> type = track.getClass();
		if (PointMass.class.isAssignableFrom(type)) {
			type = PointMass.class;
		}
		else if (Vector.class.isAssignableFrom(type)) {
			type = Vector.class;
		}
		return type;
  }

}
