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

	private static Map<TrackerPanel, NumberFormatSetter> formatSetters = new HashMap<TrackerPanel, NumberFormatSetter>();
  
	// static fields
  protected static final String NO_PATTERN = TrackerRes.getString("NumberFormatSetter.NoPattern"); //$NON-NLS-1$
  protected static Map<Class<? extends TTrack>, TreeMap<String, String>> defaultFormatPatterns;
  protected static TFrame frame;
  protected static ArrayList<Class<? extends TTrack>> formattableTrackTypes;
  
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
		// create list of formattable track types
		formattableTrackTypes = new ArrayList<Class<? extends TTrack>>();
		formattableTrackTypes.add(PointMass.class);
		formattableTrackTypes.add(Vector.class);
		formattableTrackTypes.add(LineProfile.class);
		formattableTrackTypes.add(RGBRegion.class);
		formattableTrackTypes.add(TapeMeasure.class);
		formattableTrackTypes.add(Protractor.class);
		formattableTrackTypes.add(CircleFitter.class);
		formattableTrackTypes.add(Calibration.class);
		formattableTrackTypes.add(OffsetOrigin.class);
		formattableTrackTypes.add(CoordAxes.class);
  }

  // instance fields
  TrackerPanel trackerPanel;
  int trackID = -1;
  JButton closeButton, helpButton, revertButton;
  JComboBox trackDropdown;
  JLabel patternLabel, sampleLabel;
  JTextField patternField;
  NumberField sampleField;
  java.text.DecimalFormat testFormat;
  String[] displayedNames;
  Map<String, String> realNames = new HashMap<String, String>();
  HashMap<Class<? extends TTrack>, TreeMap<String,String>> prevDefaultPatterns = new HashMap<Class<? extends TTrack>, TreeMap<String,String>>();
  Map<TTrack, TreeMap<String, String>> prevTrackPatterns 
  	= new HashMap<TTrack, TreeMap<String, String>>();
  JPanel variablePanel, applyToPanel, unitsPanel;
  JList variableList;
  JScrollPane variableScroller;
  JRadioButton trackOnlyButton, trackTypeButton, dimensionButton;
  JRadioButton degreesButton, radiansButton;
  String prevPattern;
  boolean formatsChanged;
  Map<Integer, String[]> selectedVariables = new TreeMap<Integer, String[]>();

  /**
   * Gets the format setter.
   *
   * @param track the track
   * @param selectedNames the initially selected names
   * @return the format setter
   */
  public static NumberFormatSetter getFormatSetter(TTrack track, String[] selectedNames) {
    if (frame==null) {
    	frame = track.trackerPanel.getTFrame();
    }
  	NumberFormatSetter setter = formatSetters.get(track.trackerPanel);
    if(setter==null) {
    	setter = new NumberFormatSetter(track.trackerPanel);
    	formatSetters.put(track.trackerPanel, setter);
      // center on screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width - setter.getBounds().width) / 2;
      int y = (dim.height - setter.getBounds().height) / 2;
      setter.setLocation(x, y);
    }
    setter.savePrevious();
    setter.selectedVariables.put(track.getID(), selectedNames);
  	setter.setTrack(track);
    return setter;
  }

  /**
   * Disposes of a format setter.
   *
   * @param trackerPanel a TrackerPanel
   */
  public static void dispose(TrackerPanel trackerPanel) {
  	NumberFormatSetter setter = formatSetters.get(trackerPanel);
    if(setter!=null) {
    	setter.setVisible(false);
    	setter.trackerPanel = null;
    	formatSetters.remove(trackerPanel);
    }
  }

  /**
   * Private constructor.
   *
   * @param track a track
   */
  private NumberFormatSetter(TrackerPanel tPanel) {
    super(frame, true);
    trackerPanel = tPanel;
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
    revertButton = new JButton();
    final Action resetAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
      	TTrack track = TTrack.getTrack(trackID);
      	
    		// reset default patterns in trackerPanel.formatPatterns
      	TreeMap<String, String> patterns, prevPatterns;
      	for (Class<? extends TTrack> type: formattableTrackTypes) {
      		prevPatterns = prevDefaultPatterns.get(type);
      		trackerPanel.formatPatterns.put(type, prevPatterns);
      	}
      	
      	// reset track formats
    		ArrayList<TTrack> tracks = track.trackerPanel.getTracks();
    		for (TTrack next: tracks) {
        	patterns = prevTrackPatterns.get(next);
          if (patterns!=null) {
          	boolean fireEvent = false;
          	String[] names = getVariableNames(next);
            for(String name : names) {
	          	fireEvent = setFormatPattern(next, name, patterns.get(name)) || fireEvent;
	          	if (fireEvent) {
	          		next.firePropertyChange("data", null, null); //$NON-NLS-1$
	          	}
	          }
          }
    		}
    		showNumberFormatAndSample(variableList.getSelectedIndices());
    		prevPattern = ""; //$NON-NLS-1$
    		formatsChanged = false;
    		refreshGUI();
			}
    };
    revertButton.addActionListener(resetAction);
    helpButton = new JButton(); 
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String tab = "      ";                                                                                          //$NON-NLS-1$
        String nl = System.getProperty("line.separator", "/n");                                                         //$NON-NLS-1$ //$NON-NLS-2$
        JOptionPane.showMessageDialog(NumberFormatSetter.this, DisplayRes.getString("DataTable.NumberFormat.Help.Message1")+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message2")+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message3")+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message4")+nl+ //$NON-NLS-1$
          tab+DisplayRes.getString("DataTable.NumberFormat.Help.Message5")+nl+nl+ //$NON-NLS-1$
          DisplayRes.getString("DataTable.NumberFormat.Help.Message6")+" PI."+nl+nl+ //$NON-NLS-1$ //$NON-NLS-2$
          TrackerRes.getString("NumberFormatSetter.Help.Dimensions.1")+nl+ //$NON-NLS-1$
          tab+TrackerRes.getString("NumberFormatSetter.Help.Dimensions.2")+nl+ //$NON-NLS-1$
          tab+TrackerRes.getString("NumberFormatSetter.Help.Dimensions.3")+nl+ //$NON-NLS-1$
          tab+TrackerRes.getString("NumberFormatSetter.Help.Dimensions.4")+nl+ //$NON-NLS-1$
          tab+TrackerRes.getString("NumberFormatSetter.Help.Dimensions.5")+nl+ //$NON-NLS-1$
          tab+TrackerRes.getString("NumberFormatSetter.Help.Dimensions.6")+nl+ //$NON-NLS-1$
          tab+TrackerRes.getString("NumberFormatSetter.Help.Dimensions.7")+nl+ //$NON-NLS-1$
          tab+TrackerRes.getString("NumberFormatSetter.Help.Dimensions.8")+nl, //$NON-NLS-1$
          DisplayRes.getString("DataTable.NumberFormat.Help.Title"), //$NON-NLS-1$
          JOptionPane.INFORMATION_MESSAGE);
      }

    });
    
    // create trackDropdown early since need it for spinners
    trackDropdown = new JComboBox() {
      public Dimension getPreferredSize() {
    		Dimension dim = super.getPreferredSize();
    		dim.height-=1;
    		return dim;
      }
    };
    trackDropdown.setRenderer(new TrackRenderer());
    trackDropdown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if ("refresh".equals(trackDropdown.getName())) return; //$NON-NLS-1$
        Object[] item = (Object[])trackDropdown.getSelectedItem();
        if (item!=null) {
        	for (TTrack next: trackerPanel.getTracks()) {
        		if (item[1].equals(next.getName())) {
        			setTrack(next);
        			refreshGUI();
        		}
        	}
        }
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
    variableScroller.setPreferredSize(new Dimension(280, 120));
    // "apply to" buttons
    trackOnlyButton = new JRadioButton();
    trackTypeButton = new JRadioButton();
    dimensionButton = new JRadioButton();
    ButtonGroup group = new ButtonGroup();
    group.add(trackOnlyButton);
    group.add(trackTypeButton);
    group.add(dimensionButton);
    trackOnlyButton.setSelected(true);
    
    // angle unit buttons
    Action angleUnitAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
      	frame.setAnglesInRadians(radiansButton.isSelected());
    		showNumberFormatAndSample(variableList.getSelectedIndices());
			}
    };
    degreesButton = new JRadioButton();
    degreesButton.addActionListener(angleUnitAction);
    radiansButton = new JRadioButton();
    radiansButton.addActionListener(angleUnitAction);
    group = new ButtonGroup();
    group.add(degreesButton);
    group.add(radiansButton);
    degreesButton.setSelected(!frame.anglesInRadians);
    radiansButton.setSelected(frame.anglesInRadians);
    
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
    JPanel dropdownPanel = new JPanel();
    dropdownPanel.add(trackDropdown);
    variablePanel.add(dropdownPanel, BorderLayout.NORTH);
    variablePanel.add(variableScroller, BorderLayout.CENTER);
    add(variablePanel, BorderLayout.CENTER);
    JPanel south = new JPanel(new BorderLayout());
    add(south, BorderLayout.SOUTH);
    applyToPanel = new JPanel();
    Box box = Box.createVerticalBox();
    box.add(trackOnlyButton);
    box.add(trackTypeButton);
    box.add(dimensionButton);
    applyToPanel.add(box);
    south.add(applyToPanel, BorderLayout.NORTH);
    
    unitsPanel = new JPanel();
    unitsPanel.add(degreesButton);
    unitsPanel.add(radiansButton);
    south.add(unitsPanel, BorderLayout.CENTER);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(helpButton);
    buttonPanel.add(revertButton);
    buttonPanel.add(closeButton);
    south.add(buttonPanel, BorderLayout.SOUTH);
    pack();
    refreshGUI();
  }
  
  /**
   * Sets the variables and initially selected names displayed in this NumberFormatSetter.
   *
   * @param names the variable names
   * @param selected the intially selected names
   */
  protected void setVariables(String[] names, String[] selected) {
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

    // create variable list and add to scroller
    variableList = new JList(displayedNames);
    variableList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    variableList.setVisibleRowCount(-1);
    variableList.addListSelectionListener(new ListSelectionListener() {
    	public void valueChanged(ListSelectionEvent e) {
    		if (!e.getValueIsAdjusting()) {
    			int[] indices = variableList.getSelectedIndices();
      		showNumberFormatAndSample(indices);
    			String[] vars = getSelectedVariables(indices);
      		selectedVariables.put(trackID, vars);      		
      		refreshGUI();
    		}
    	}
    });
    variableScroller.setViewportView(variableList);
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
    degreesButton.setSelected(!frame.anglesInRadians);
    radiansButton.setSelected(frame.anglesInRadians);
    // refresh GUI to be sure correct track type and unit dimensions are shown
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
   * Gets an array of unit dimensions for all currently selected variables.
   *
   * @return array of unit dimensions
   */
  private String[] getCurrentDimensions() {
    TTrack track = TTrack.getTrack(trackID);
    Class<? extends TTrack> trackType = getTrackType(track);
  	TreeSet<String> dimensions = new TreeSet<String>(); 
		int[] indices = variableList.getSelectedIndices();
		Object[] selected = new Object[indices.length];
		for (int j=0; j<indices.length; j++) {
			selected[j] = displayedNames[indices[j]];
		}
		for (Object displayedName : selected) {
			String name = realNames.get(displayedName.toString());
  		String dim = getVariableDimensions(trackType, name);
  		if (dim!=null) {
  			dimensions.add(dim);
  		}
		}
		return dimensions.toArray(new String[dimensions.size()]);
  }

  /**
   * Gets an array of all variable names for a specified track.
   *
   * @param track the track
   * @return array of variable names
   */
  protected static String[] getVariableNames(TTrack track) {
  	if (track==null || track.trackerPanel==null) return new String[0];
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
	  	// then add any columns that were missed
			for (int i = 0; i<data.getDatasets().size(); i++) {
				if (!added.contains(i)) {
		  		dataset = data.getDataset(i);
		      name = dataset.getYColumnName();
		      names.add(name);
				}
			}
  	}
  	// finally add variables that are NOT yet in list
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
  	if (table!=null && table.getFormatPattern(name)!=null) {
  		return table.getFormatPattern(name);
  	}
  	// get pattern from track properties
  	if (track.getProperty(name)!=null) {
  		return (String)track.getProperty(name);
  	}
  	// get pattern for track type
  	Class<? extends TTrack> type = getTrackType(track);
    // look in trackerPanel formatPatterns
    TreeMap<String, String> patterns = track.trackerPanel.getFormatPatterns(type);
    if (patterns.get(name)!=null) {
    	return patterns.get(name);
    }
    // look in defaultFormatPatterns 
    patterns = NumberFormatSetter.defaultFormatPatterns.get(type);
    if (patterns.get(name)!=null) {
    	return patterns.get(name);
    }
  	return ""; //$NON-NLS-1$
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
   * @return true if the pattern was changed
   */
  protected static boolean setFormatPattern(TTrack track, String name, String pattern) {
  	ArrayList<TableTrackView> tableViews = getTableViews(track);
  	boolean changed = false;
  	boolean found = false;
  	if (track.isViewable()) {
  		found = true;
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
  		found = true;
	  	for (NumberField field: fields) {
	  		if (!field.getFixedPattern().equals(pattern)) {
	  			field.setFixedPattern(pattern);
	  			changed = true;
	  		}
	  	}
  	}
  	if (!found) {
  		if (!pattern.equals(track.getProperty(name))) {
  			track.setProperty(name, pattern);
  			changed = true;
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
    TreeMap<String, String> defaultPatterns = track.trackerPanel.getFormatPatterns(type);
  	ArrayList<String> customPatterns = new ArrayList<String>();
  	for (int i=0; i<patterns.length-1; i=i+2) {
  		String name = patterns[i];
  		String pattern = defaultPatterns.get(name)==null? "": defaultPatterns.get(name); //$NON-NLS-1$
  		if (!pattern.equals(patterns[i+1])) {
  			customPatterns.add(name);
  			customPatterns.add(patterns[i+1]);
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
    	TreeMap<String, String> patterns = trackerPanel.getFormatPatterns(type);
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
    		if (defaultPattern==null) {
    			defaultPattern = ""; //$NON-NLS-1$
    		}
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
   * Gets a list of track types for which number formatting is available. 
   * 
   * @return a list of track types
   */
  public static ArrayList<Class<? extends TTrack>> getFormattableTrackTypes() {
  	return formattableTrackTypes;
  }
  
  /**
   * Sets the track to be formatted.
   *
   * @param track the track
   */
  protected void setTrack(TTrack track) {
  	trackID = track.getID();  	
    String[] names = getVariableNames(track);
    String[] selected = selectedVariables.get(trackID);
    if (selected==null) {
    	String[] vars = TTrack.getVariableList(getTrackType(track));
    	selected = vars.length>0? new String[] {vars[0]}: new String[0];
    }
    setVariables(names, selected);
  }
  
  protected void savePrevious() {
  	// save previous default patterns for all types
  	TreeMap<String, String> patterns;
  	for (Class<? extends TTrack> type: formattableTrackTypes) {
  	  TreeMap<String, String> prevPatterns = new TreeMap<String, String>();
  		patterns = trackerPanel.getFormatPatterns(type);
  		prevPatterns.putAll(patterns);
  		prevDefaultPatterns.put(type, prevPatterns);
  	}

  	// save previous patterns for all tracks
    prevTrackPatterns.clear();
		ArrayList<TTrack> tracks = trackerPanel.getTracks();
		for (TTrack next: tracks) {
			patterns = new TreeMap<String, String>();			
	    for(String name : getVariableNames(next)) {
	      patterns.put(name, getFormatPattern(next, name));
	    }
	    prevTrackPatterns.put(next, patterns);
    }
    formatsChanged = false;
  }
  
  /**
   * Sets the format pattern for a named variable.
   *
   * @param name the variable name
   * @param pattern the pattern
   */
  protected void setFormatPattern(String name, String pattern) {
  	boolean wasChanged = formatsChanged;
  	TTrack track = TTrack.getTrack(trackID);
  	
  	if (dimensionButton.isSelected()) {
  		// apply to all variables with the same unit dimensions
  		Class<? extends TTrack> trackType = getTrackType(track);
  		String dimensions = getVariableDimensions(trackType, name);
  		if (dimensions!=null) {
    		// apply to trackerPanel.formatPatterns for future tracks
  			for (Class<? extends TTrack> nextType: track.trackerPanel.formatPatterns.keySet()) {
	        TreeMap<String, String> patterns = track.trackerPanel.getFormatPatterns(nextType);
	        for (String nextName: patterns.keySet()) {
	  				if (dimensions.equals(getVariableDimensions(nextType, nextName))) {
	  	    		if (!pattern.equals(patterns.get(nextName))) {
	  	    			patterns.put(nextName, pattern);
	      	  		formatsChanged = true;
	  	    		}
	  				}
	        }
  			}

  			// apply to all existing tracks
    		ArrayList<TTrack> tracks = track.trackerPanel.getTracks();
    		for (TTrack next: tracks) {
    			trackType = getTrackType(next);
    			boolean trackChanged = false;
    			for (String nextName: TTrack.getVariableList(trackType)) {
    				if (dimensions.equals(getVariableDimensions(trackType, nextName))) {
        			if (setFormatPattern(next, nextName, pattern)) {
        				trackChanged = true;
        			}       			
    				}
    			}
    			if (trackChanged) {
    				next.firePropertyChange("data", null, null); //$NON-NLS-1$   					
    	  		formatsChanged = true;
    			}
    		}
  		}
  	}
  	else if (trackTypeButton.isSelected()) {
	  	// apply to the variable in all tracks of same type 
  		Class trackType = getTrackType(track);
  		ArrayList<TTrack> tracks = track.trackerPanel.getTracks();
  		for (TTrack next: tracks) {
  			if (!next.getClass().isAssignableFrom(trackType)) continue;
  			if (setFormatPattern(next, name, pattern)) {
  	  		next.firePropertyChange("data", null, null); //$NON-NLS-1$
  	  		formatsChanged = true;
  	  	}
  		}
  		// set pattern in trackerPanel.formatPatterns
  		TreeMap<String, String> patterns = track.trackerPanel.getFormatPatterns(trackType);
  		patterns.put(name, pattern);
  	}
  	else if (setFormatPattern(track, name, pattern)) {
  		// apply to only this track
  		track.firePropertyChange("data", null, null); //$NON-NLS-1$
  		formatsChanged = true;
  	}
  	if (!wasChanged && formatsChanged) {
  		refreshGUI();
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
  	if (track==null || track.trackerPanel==null || frame==null)
  		return dataViews;
    Container[] c = frame.getViews(track.trackerPanel);
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
    if (track==null) return;
    refreshDropdown();
  	String trackName = track.getName();
		Class<? extends TTrack> trackType = getTrackType(track);
  	String trackTypeName = trackType.getSimpleName();
    setTitle(TrackerRes.getString("NumberFormatSetter.Title")); //$NON-NLS-1$
    closeButton.setText(DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
    revertButton.setText(TrackerRes.getString("NumberFormatSetter.Button.Revert")); //$NON-NLS-1$
    revertButton.setEnabled(formatsChanged);
    helpButton.setText(DisplayRes.getString("GUIUtils.Help")); //$NON-NLS-1$
    patternLabel.setText(DisplayRes.getString("DataTable.NumberFormat.Dialog.Label.Format")); //$NON-NLS-1$
    sampleLabel.setText(DisplayRes.getString("DataTable.NumberFormat.Dialog.Label.Sample"));  //$NON-NLS-1$

    degreesButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Degrees")); //$NON-NLS-1$
    radiansButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Radians")); //$NON-NLS-1$
    
    String s = TrackerRes.getString("NumberFormatSetter.Button.ApplyToTrackOnly.Text"); //$NON-NLS-1$
    trackOnlyButton.setText(s+" ("+trackName+")"); //$NON-NLS-1$ //$NON-NLS-2$
	  s = TrackerRes.getString("NumberFormatSetter.Button.ApplyToTrackType.Text"); //$NON-NLS-1$
    trackTypeButton.setText(s+" "+trackTypeName); //$NON-NLS-1$
	  s = TrackerRes.getString("NumberFormatSetter.Button.ApplyToDimension.Text"); //$NON-NLS-1$
	  String[] dimensions = getCurrentDimensions();
	  if (dimensions.length==0) {
	  	dimensionButton.setText(s); 
	  }
	  else if (dimensions.length==1 && !"".equals(dimensions[0])) { //$NON-NLS-1$
	  	dimensionButton.setText(s+" \""+dimensions[0]+"\""); //$NON-NLS-1$ //$NON-NLS-2$
	  }
	  else {
	  	String dim = dimensions[0];
	  	for (int i=1; i<dimensions.length; i++) {
	  		if ((dim+", "+dimensions[i]).length()>7) { //$NON-NLS-1$
	  			dim += ", "+TrackerRes.getString("NumberFormatSetter.DimensionList.More"); //$NON-NLS-1$ //$NON-NLS-2$
	  			break;
	  		}
	  		dim += ", "+ dimensions[i]; //$NON-NLS-1$
	  	}
	  	dimensionButton.setText(s+" "+dim); //$NON-NLS-1$
	  }
	  // set titled borders
    variablePanel.setBorder(BorderFactory.createTitledBorder(
    		TrackerRes.getString("NumberFormatSetter.ApplyToVariables.Text"))); //$NON-NLS-1$
    applyToPanel.setBorder(BorderFactory.createTitledBorder(
    		TrackerRes.getString("NumberFormatSetter.TitledBorder.ApplyTo.Text"))); //$NON-NLS-1$
    unitsPanel.setBorder(BorderFactory.createTitledBorder(
    		TrackerRes.getString("NumberFormatSetter.TitledBorder.Units.Text"))); //$NON-NLS-1$
    Dimension dim = getSize();
		if (dim.width>getMinimumSize().width) {
			setSize(dim);
		}
		else {
			pack();
		}
  }

  /**
   * Refreshes the dropdown list.
   */
  protected void refreshDropdown() {
  	// refresh trackDropdown
    Object toSelect = null;
    trackDropdown.setName("refresh"); //$NON-NLS-1$
    trackDropdown.removeAllItems();
    TTrack track = TTrack.getTrack(trackID);
    for (TTrack next: trackerPanel.getTracks()) {
    	Icon icon = next.getFootprint().getIcon(21, 16);
      Object[] item = new Object[] {icon, next.getName()};
      trackDropdown.addItem(item);
      if (next==track) {
      	toSelect = item;
      }
    }
    if (track==null) {
    	Object[] emptyItem = new Object[] {null, "           "}; //$NON-NLS-1$
    	trackDropdown.insertItemAt(emptyItem, 0);
    	toSelect = emptyItem;
    }
    // select desired item
    if (toSelect!=null) {
    	trackDropdown.setSelectedItem(toSelect);
    }
    trackDropdown.setName(null);
    
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
      boolean degrees = name.startsWith(Tracker.THETA) && !frame.anglesInRadians;
    	showNumberFormatAndSample(pattern, degrees);
    }
    else {
    	// do all selected indices have same pattern?
    	String name = realNames.get(displayedNames[selectedIndices[0]]);
      boolean degrees = name.startsWith(Tracker.THETA) && !frame.anglesInRadians;       		
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
   * Gets the names of the currently selected variables.
   *
   * @param selectedIndices the indices of the selected variables in the list
   */
  private String[] getSelectedVariables(int[] selectedIndices) {
    if (selectedIndices==null) {
    	return new String[0];
    }
    String[] selectedNames = new String[selectedIndices.length];
  	for (int i=0; i<selectedIndices.length; i++) {
  		selectedNames[i] = realNames.get(displayedNames[selectedIndices[i]]);
  	}
  	return selectedNames;
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

  /**
   * Determines the unit dimensions associated with a given track variable.
   * Dimensions include L (length), T (time), M (mass), A (angle), I (integer), P (pixels), C (color 8 bit).
   * Dimensions are often combinations of MLT. May return null.
   * 
   * @param type the track type
   * @param variable the variable name
   * @return the dimensions or null if unknown
   */
  protected static String getVariableDimensions(Class<? extends TTrack> type, String variable) {
  	if (variable.startsWith(Tracker.THETA)) {
  		return "A"; //$NON-NLS-1$
  	}
  	if (variable.equals("t")) { //$NON-NLS-1$
  		return "T"; //$NON-NLS-1$
  	}
  	if (PointMass.class.isAssignableFrom(type)) {
  		String[] vars = PointMass.variableList;
  		if (vars[1].equals(variable) || vars[2].equals(variable) || vars[3].equals(variable)) { // position
  			return "L"; //$NON-NLS-1$
  		}  		
  		if (vars[5].equals(variable) || vars[6].equals(variable) || vars[7].equals(variable)) { // veloc
  			return "L/T"; //$NON-NLS-1$
  		}  		
  		if (vars[9].equals(variable) || vars[10].equals(variable) || vars[11].equals(variable)) { // accel
  			return "L/TT"; //$NON-NLS-1$
  		}  		
  		if (vars[14].equals(variable)) { // omega
  			return "A/T"; //$NON-NLS-1$
  		}  		
  		if (vars[15].equals(variable)) { // alpha
  			return "A/TT"; //$NON-NLS-1$
  		}  		
  		if (vars[16].equals(variable) || vars[17].equals(variable)) { // step and frame
  			return "I"; //$NON-NLS-1$
  		}  		
  		if (vars[18].equals(variable) || vars[19].equals(variable) || vars[20].equals(variable)) { // momentum
  			return "ML/T"; //$NON-NLS-1$
  		}  		
  		if (vars[22].equals(variable) || vars[23].equals(variable)) { // pixel positions
  			return "P"; //$NON-NLS-1$
  		}  		
  		if (vars[24].equals(variable)) { // KE
  			return "MLL/TT"; //$NON-NLS-1$
  		}  		
  		if (vars[25].equals(variable)) { // mass
  			return "M"; //$NON-NLS-1$
  		}  		
  	}
  	else if (Vector.class.isAssignableFrom(type)) {
  		String[] vars = Vector.variableList;
  		if (vars[1].equals(variable) || vars[2].equals(variable) || vars[3].equals(variable) 
  				|| vars[5].equals(variable) || vars[6].equals(variable)) {
  			return "L"; //$NON-NLS-1$
  		}  		
  		if (vars[7].equals(variable) || vars[8].equals(variable)) { // step and frame
  			return "I"; //$NON-NLS-1$
  		}  		
  	}
  	else if (LineProfile.class.isAssignableFrom(type)) {
  		String[] vars = LineProfile.variableList;
  		if (vars[0].equals(variable) || vars[7].equals(variable)) {
  			return "I"; //$NON-NLS-1$
  		}  		
  		if (vars[1].equals(variable) || vars[2].equals(variable)) {
  			return "L"; //$NON-NLS-1$
  		}  		
  		if (vars[3].equals(variable) || vars[4].equals(variable) 
  				|| vars[5].equals(variable) || vars[6].equals(variable)) {
  			return "C"; //$NON-NLS-1$
  		}  		
  	}
  	else if (RGBRegion.class.isAssignableFrom(type)) {
  		String[] vars = RGBRegion.variableList;
  		if (vars[1].equals(variable) || vars[2].equals(variable)) {
  			return "L"; //$NON-NLS-1$
  		}  		
  		if (vars[3].equals(variable) || vars[4].equals(variable) 
  				|| vars[5].equals(variable) || vars[6].equals(variable)) {
  			return "C"; //$NON-NLS-1$
  		}  		
  		if (vars[7].equals(variable) || vars[8].equals(variable) 
  				|| vars[9].equals(variable)) {
  			return "I"; //$NON-NLS-1$
  		}  		
  	}
  	else if (TapeMeasure.class.isAssignableFrom(type)) {
  		String[] vars = TapeMeasure.variableList;
  		if (vars[1].equals(variable)) {
  			return "L"; //$NON-NLS-1$
  		}  		
  		if (vars[3].equals(variable) || vars[4].equals(variable)) {
  			return "I"; //$NON-NLS-1$
  		}  		
  	}
  	else if (Protractor.class.isAssignableFrom(type)) {
  		String[] vars = Protractor.variableList;
  		if (vars[2].equals(variable) || vars[3].equals(variable)) {
  			return "L"; //$NON-NLS-1$
  		}  		
  		if (vars[4].equals(variable) || vars[5].equals(variable)) {
  			return "I"; //$NON-NLS-1$
  		}  		
  	}
  	else if (CircleFitter.class.isAssignableFrom(type)) {
  		String[] vars = CircleFitter.variableList;
  		if (vars[1].equals(variable) || vars[2].equals(variable) || vars[3].equals(variable)
  				|| vars[6].equals(variable)	|| vars[7].equals(variable)) {
  			return "L"; //$NON-NLS-1$
  		}  		
  		if (vars[4].equals(variable) || vars[5].equals(variable)) {
  			return "I"; //$NON-NLS-1$
  		}  		
  	}
  	else if (Calibration.class.isAssignableFrom(type) || OffsetOrigin.class.isAssignableFrom(type)) {
  		// every field is length/position
  		return "L";		 //$NON-NLS-1$
  	}
  	else if (CoordAxes.class.isAssignableFrom(type)) {
  		String[] vars = CoordAxes.variableList;
  		if (vars[0].equals(variable) || vars[1].equals(variable)) {
  			// pixel coordinates
  			return "P"; //$NON-NLS-1$
  		}  		
  	}
		return null;
  }

}
