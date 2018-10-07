/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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
import java.lang.reflect.Constructor;
import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.*;

/**
 * This displays a table view of a track on a TrackerPanel.
 *
 * @author Douglas Brown
 * @author John Welch
 */
public class TableTrackView extends TrackView {
	
	// static fields
	static final String DEFINED_AS = ": "; //$NON-NLS-1$
	static Icon skipsOffIcon, skipsOnIcon;
	static {
		skipsOnIcon =  new ResizableIcon(Tracker.class.getResource("resources/images/skips_on.gif")); //$NON-NLS-1$
		skipsOffIcon =  new ResizableIcon(Tracker.class.getResource("resources/images/skips_off.gif")); //$NON-NLS-1$
	}

  // instance fields
  protected DatasetManager data;
  protected JScrollPane columnsScroller;
  protected TrackDataTable dataTable;
  protected JCheckBox[] checkBoxes;
  protected boolean refresh = true;
  protected Set<String> textColumnsVisible = new TreeSet<String>();
  private JButton columnsButton, gapsButton;
  private JPanel columnsPanel;
  private DatasetManager tableData;
  private JPopupMenu popup;
  protected JMenu textColumnMenu, deleteTextColumnMenu, renameTextColumnMenu;
  protected JMenuItem createTextColumnItem;
  protected JMenuItem dataToolItem, dataBuilderItem, deleteDataFunctionItem;
  private JMenu numberMenu;
  private JMenuItem goToFrameItem, formatDialogItem, setUnitsItem, showUnitsItem;
  private JMenu copyDataMenu;
  private JMenuItem copyDataRawItem, copyDataFormattedItem;
  private JMenu setDelimiterMenu;
  private ButtonGroup delimiterButtonGroup = new ButtonGroup();
  private JMenuItem addDelimiterItem, removeDelimiterItem;
  private JMenuItem copyImageItem, snapshotItem, printItem, helpItem;
  private boolean highlightVisible = true, refreshed = false, forceRefresh = false;
  private ArrayList<Integer> highlightFrames = new ArrayList<Integer>();
  private ArrayList<Integer> highlightRows = new ArrayList<Integer>();
  private int leadCol;
  private Font font = new JTextField().getFont();
  private TreeSet<Double> selectedIndepVarValues // used when sorting
  		= new TreeSet<Double>();
  private Map<String, TableCellRenderer> degreeRenderers
  		= new HashMap<String, TableCellRenderer>();
  private TextColumnTableModel textColumnModel;
  private TextColumnEditor textColumnEditor;
  private ArrayList<String> textColumnNames = new ArrayList<String>();

  /**
   * Constructs a TrackTableView of the specified track on the specified tracker panel.
   *
   * @param track the track
   * @param panel the tracker panel
   * @param view the TableTView that will display this
   */
  public TableTrackView(TTrack track, TrackerPanel panel, TableTView view) {
    super(track, panel, view);
    track.addPropertyChangeListener("text_column", this); //$NON-NLS-1$
    textColumnNames.addAll(track.getTextColumnNames());
    // create the DataTable
    textColumnEditor = new TextColumnEditor();
    dataTable = new TrackDataTable();
    data = track.getData(trackerPanel);
    tableData = new DatasetManager();
    tableData.setXPointsLinked(true);
    dataTable.add(tableData);
    textColumnModel = new TextColumnTableModel();
    dataTable.add(textColumnModel);
    setViewportView(dataTable);
    dataTable.setPreferredScrollableViewportSize(new Dimension(160, 200));
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	dataTable.clearSelection();
      }
      public void mouseEntered(MouseEvent e) {
      	dataTable.requestFocusInWindow();
      }
    });
    dataTable.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
      	dataTable.requestFocusInWindow();
      }
    });
    // add key listener to start editing text column cells with space key
    dataTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_SPACE) {
        	int row = dataTable.getSelectedRow();
        	int col = dataTable.getSelectedColumn();
          dataTable.editCellAt(row, col);
          textColumnEditor.field.selectAll();
          Runnable runner = new Runnable() {
            public synchronized void run() {
              textColumnEditor.field.requestFocusInWindow();
            }
          };
          SwingUtilities.invokeLater(runner);
        }
      }

    });

    ListSelectionModel selectionModel = dataTable.getSelectionModel();
    selectionModel.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        selectedIndepVarValues.clear();
        int[] rows = dataTable.getSelectedRows(); // selected view rows
        for(int i = 0; i<rows.length; i++) {
          double val = getIndepVarValueAtRow(rows[i]);
          if (!Double.isNaN(val))
          	selectedIndepVarValues.add(val);
        }
      }

    });
    setToolTipText(ToolsRes.getString("DataToolTab.Scroller.Tooltip")); //$NON-NLS-1$
    highlightVisible = !(track instanceof LineProfile);
    // create the GUI
    createGUI();
    // show the track-specified default columns
    boolean useDefault = true;
  	for (int i=0; i<4; i++) {
  		String col = (String)track.getProperty("tableVar"+i); //$NON-NLS-1$
	    if (col!=null) {
    		setVisible(Integer.parseInt(col), true);
    		useDefault = false;
    	}
    }
    if (useDefault) {
	    // show the default columns
	    setVisible(0, true);
	    setVisible(1, true);
    }
    // set the default number formats, if any
    Class<? extends TTrack> trackType = NumberFormatDialog.getTrackType(track);
    TreeMap<String, String> patterns = trackerPanel.getFormatPatterns(trackType);
    DataTable table = getDataTable();
  	for (String name: patterns.keySet()) {
    	table.setFormatPattern(name, patterns.get(name));
  	}
  }

  /**
   * Refreshes this view.
   *
   * @param frameNumber the frame number
   */
  public void refresh(int frameNumber) {
  	if (!forceRefresh && !isRefreshEnabled()) return;
  	forceRefresh = false;
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh "+frameNumber); //$NON-NLS-1$
		dataTable.clearSelection();
  	TTrack track = getTrack();
    try {
		  track.getData(trackerPanel);
		  // copy datasets into table data based on checkbox states
		  tableData.clear();
		  int colCount = 0;
		  ArrayList<Dataset> datasets = data.getDatasets();
			dataTable.setUnits(datasets.get(0).getXColumnName(), "", track.getDataDescription(0)); //$NON-NLS-1$
			int count = datasets.size();
	    for (int i = 0; i < count; i++) {
	      if (checkBoxes[i].isSelected()) {
	      	Dataset in = datasets.get(i);
		    	String xTitle = in.getXColumnName();
		    	String yTitle = in.getYColumnName();
			    boolean yIsAngle = yTitle.startsWith(Tracker.THETA)
			    		|| yTitle.startsWith(Tracker.OMEGA)
			    		|| yTitle.startsWith(Tracker.ALPHA);
			    boolean degrees = trackerPanel.getTFrame()!=null
	    				&& !trackerPanel.getTFrame().anglesInRadians;
		    	Dataset local = tableData.getDataset(colCount++);
		    	double[] yPoints = in.getYPoints();
		    	String tooltip = track.getDataDescription(i+1)+" "; //$NON-NLS-1$
		    	String units = ""; //$NON-NLS-1$
		    	if (yIsAngle) { // angle columns
	  	    	if (yTitle.startsWith(Tracker.THETA)) {
	  	    		if (degrees) {
	  	    			units = Tracker.DEGREES;
	  	  	    	tooltip += TrackerRes.getString("TableTrackView.Degrees.Tooltip"); //$NON-NLS-1$
	  	    		}
	  	    		else {
	  	    			tooltip += TrackerRes.getString("TableTrackView.Radians.Tooltip"); //$NON-NLS-1$
	  	    		}
	  	    	}
	  	    	else if (yTitle.startsWith(Tracker.OMEGA)) {
	  	    		if (degrees) {
	  	  	    	tooltip += TrackerRes.getString("TableTrackView.DegreesPerSecond.Tooltip"); //$NON-NLS-1$
	  	    		}
	  	    		else {
	  	    			tooltip += TrackerRes.getString("TableTrackView.RadiansPerSecond.Tooltip"); //$NON-NLS-1$
	  	    		}
	  	    	}
	  	    	else if (yTitle.startsWith(Tracker.ALPHA)) {
	  	    		if (degrees) {
	  	  	    	tooltip += TrackerRes.getString("TableTrackView.DegreesPerSecondSquared.Tooltip"); //$NON-NLS-1$
	  	    		}
	  	    		else {
	  	    			tooltip += TrackerRes.getString("TableTrackView.RadiansPerSecondSquared.Tooltip"); //$NON-NLS-1$
	  	    		}
	  	    	}
	      		TableCellRenderer precisionRenderer = dataTable.getPrecisionRenderer(yTitle);
	        	if (degrees) {
	        		// convert values from radians to degrees
	        		for (int k = 0; k<yPoints.length; k++) {
	        			if (!Double.isNaN(yPoints[k])) {
	        				yPoints[k] *= 180/Math.PI;
	        			}
	        		}
	         		// set default degrees precision
	        		if (precisionRenderer==null) {
	          		dataTable.setFormatPattern(yTitle, NumberField.DECIMAL_1_PATTERN);
	          		degreeRenderers.put(yTitle, dataTable.getPrecisionRenderer(yTitle));
	        		}
	         	}
	        	else if (precisionRenderer!=null){ // radians display
	        		if (precisionRenderer==degreeRenderers.get(yTitle)) {
	          		dataTable.setFormatPattern(yTitle, null);
	          		degreeRenderers.remove(yTitle);        			
	        		}        			
	        	}
	    		}
		    	if ("".equals(tooltip.trim())) tooltip = ""; //$NON-NLS-1$ //$NON-NLS-2$
	      	dataTable.setUnits(yTitle, units, tooltip);
		    	local.append(in.getXPoints(), yPoints);
		    	local.setXYColumnNames(xTitle, yTitle);
		    	local.setYColumnVisible(true);
	      }
	    }
	    for (int i = colCount; i < tableData.getDatasets().size(); i++) {
	    	tableData.setYColumnVisible(i, false);
	    }
	    if (colCount==0) {
	    	// show independent variable
	    	Dataset in = datasets.get(0);
	    	String xTitle = in.getXColumnName();
	    	Dataset local = tableData.getDataset(colCount++);
	    	double[] x = in.getXPoints();
	    	local.append(x, x);
	    	local.setXYColumnNames(xTitle, xTitle);
	    	local.setYColumnVisible(false);
	    }
			dataTable.refreshTable();
			refreshed = true;
		} catch (Exception e) {
		}
    // set the highlighted rows
		highlightFrames.clear();
  	if (trackerPanel.selectedSteps.size()>0) {
  		for (Step step: trackerPanel.selectedSteps) {
  			if (step.getTrack()!=this.getTrack()) continue;
  			highlightFrames.add(step.getFrameNumber());
  		}
  	}  
  	else {
			highlightFrames.add(frameNumber);
  	}
		setHighlighted(highlightFrames);
  }

  /**
   * Refreshes the GUI.
   */
  void refreshGUI(){
  	TTrack track = getTrack();
  	columnsButton.setText(TrackerRes.getString("TableTrackView.Button.SelectTableData")); //$NON-NLS-1$
    columnsButton.setToolTipText(TrackerRes.getString("TableTrackView.Button.SelectTableData.ToolTip")); //$NON-NLS-1$
//  	skippedFramesButton.setText(skippedFramesButton.isSelected()?
//  		TrackerRes.getString("TableTrackView.Button.SkippedFrames.On"): //$NON-NLS-1$
//    		TrackerRes.getString("TableTrackView.Button.SkippedFrames.Off")); //$NON-NLS-1$
  	gapsButton.setToolTipText(TrackerRes.getString("TableTrackView.Button.SkippedFrames.ToolTip")); //$NON-NLS-1$
//    track.dataValid = false; // triggers data refresh
    track.getData(trackerPanel); // load the current data
    refreshColumnCheckboxes();    
    refresh(trackerPanel.getFrameNumber());
  }

  /**
   * Gets the datatable.
   *
   * @return the datatable
   */
  public TrackDataTable getDataTable() {
    return dataTable;
  }

  /**
   * Gets the toolbar components
   *
   * @return an ArrayList of components to be added to a toolbar
   */
  public ArrayList<Component> getToolBarComponents() {
  	toolbarComponents.remove(gapsButton);
  	// determine if track has gaps
  	TTrack track = getTrack();
  	if (track instanceof PointMass) {
  		PointMass p = (PointMass)track;  		
  		if (p.getGapCount()>0 || p.skippedSteps.size()>0) {
  	  	toolbarComponents.add(gapsButton);
  		}
  	}
    return toolbarComponents;
  }

  /**
   * Gets the view button
   *
   * @return the view button
   */
  public JButton getViewButton() {
  	return columnsButton;
  }
  
  /**
   * Returns true if this trackview is in a custom state.
   *
   * @return true if in a custom state, false if in the default state
   */
  public boolean isCustomState() {
  	if (!refreshed) {
  		forceRefresh = true;
  		refresh(trackerPanel.getFrameNumber());
  	}
  	// check displayed data columns--default is columns 0 and 1 only
  	int n = checkBoxes.length;
  	for (int i = 0; i < n; i++) {
  		boolean selected = checkBoxes[i].isSelected();
  		boolean shouldBe = i < 2;
  		if ((shouldBe && !selected) || (!shouldBe && selected)) return true;
  	}
  	
  	// ignore formatting since now handled by NumberFormatSetter
//  	if (dataTable.getFormattedColumnNames().length>0)
//  		return true;
  	
  	// check for reordered columns
		TableColumnModel model = dataTable.getColumnModel();
		int count = model.getColumnCount();
		if (count==0) return true; // should never happen...
		int index = model.getColumn(0).getModelIndex();
  	for (int i=1; i<count; i++) {
  		if (model.getColumn(i).getModelIndex()<index) {
  			return true;
  		}
  		index = model.getColumn(i).getModelIndex();
  	}
  	return false;
  }

  /**
   * Sets the visibility of a dataset specified by index
   *
   * @param index the index of the column
   * @param visible <code>true</code> to show the dataset column in the table
   */
  public void setVisible(int index, boolean visible) {
    if (index < checkBoxes.length) {
      checkBoxes[index].setSelected(visible);
    }
    int n = data.getDatasets().size();
    if (index>=n) {
    	TTrack track = getTrack();
    	String name = track.getTextColumnNames().get(index-n);
    	textColumnsVisible.add(name);
    }
    refresh(trackerPanel.getFrameNumber());
  }

  /**
   * Sets the visibility of a data or text column specified by name
   *
   * @param name the name of the column
   * @param visible <code>true</code> to show the column in the table
   */
  public void setVisible(String name, boolean visible) {
    for (int i = 0; i < checkBoxes.length; i++) {
    	String s = checkBoxes[i].getActionCommand();
      if (s.equals(name) || TeXParser.removeSubscripting(s).equals(name)) {
        setVisible(i, visible);
        break;
      }
    }
  }

  protected void dispose() {
    data = null;
    getTrack().removePropertyChangeListener("text_column", this); //$NON-NLS-1$
    setViewportView(null);
    columnsPanel.removeAll();
    tableData.clear();
    tableData = null;
    dataTable.clear();
    dataTable.setRefreshDelay(-1); // stops the refresh timer
    dataTable = null;
    parent = null;
    super.dispose();
  }

  /**
   * Sets the highlighted frame numbers.
   *
   * @param frameNumbers the frame numbers
   */
  protected void setHighlighted(ArrayList<Integer> frameNumbers) {
    // assume no highlights
    if (!highlightVisible) return;
    
    // get rows to highlight
    highlightRows.clear();
    for (int i=0; i< frameNumbers.size(); i++) {
    	int row = getRowFromFrame(frameNumbers.get(i));
    	if (row<dataTable.getRowCount() && row>-1) {
    		highlightRows.add(row);
    	}
    }
    // set highlighted rows if found
    Runnable runner = new Runnable() {
      public synchronized void run() {
    		dataTable.clearSelection();
      	if (highlightRows.isEmpty()) {
      		return;
      	}
        try {
        	for (int row: highlightRows) {
        		dataTable.addRowSelectionInterval(row, row);
        	}
        	if (highlightRows.size()==1) {
        		dataTable.scrollRectToVisible(dataTable.getCellRect(highlightRows.get(0), 0, true));
        	}
				} catch (Exception e) {
					// occasionally throws exception during loading!
				}
				int cols = dataTable.getColumnCount();
				dataTable.setColumnSelectionInterval(0, cols-1);
      }
    };
    if(SwingUtilities.isEventDispatchThread()) runner.run();
    else SwingUtilities.invokeLater(runner);

  }

  /**
   * Gets an array of visible column names.
   *
   * @return the visible columns
   */
  String[] getVisibleColumns() {
  	ArrayList<String> list = new ArrayList<String>();
    for (int i = 0; i < checkBoxes.length; i++) {
    	if (checkBoxes[i].isSelected()) {
    		String var = checkBoxes[i].getText();
    		int n = var.indexOf(DEFINED_AS);
    		if (n > -1) var = var.substring(0, n);
    		list.add(var);
    	}
    }
    return list.toArray(new String[0]);
  }

  /**
   * Returns the visible column names in the order displayed in the table. 
   * Used for saving/loading xml.
   *
   * @return the visible columns in order
   */
  String[] getOrderedVisibleColumns() {
  	// get array of column model indexes in table order
		TableColumnModel model = dataTable.getColumnModel();
		Integer[] modelIndexes = new Integer[model.getColumnCount()];
  	for (int i=0; i<modelIndexes.length; i++) {
  		modelIndexes[i] = model.getColumn(i).getModelIndex();
  	}
  	// get array of visible (dependent variable) column names
  	String[] dependentVars = getVisibleColumns();
  	// expand array to include independent variable 
  	String[] columnNames = new String[dependentVars.length+1];
  	TTrack track = getTrack();
  	columnNames[0] = track.getDataName(0);
  	System.arraycopy(dependentVars, 0, columnNames, 1, dependentVars.length);
  	// create array of names in table order
  	String[] ordered = new String[columnNames.length];
  	for (int i=0; i<ordered.length; i++) {
  		if (i>=modelIndexes.length || modelIndexes[i]>=columnNames.length)
  			continue;
  		ordered[i] = columnNames[modelIndexes[i]];
  	}
    return ordered;
  }

  /**
   * Gets an array of column names and formats. Used for saving/loading xml
   *
   * @return String[][] each element is {colName, format}
   */
  String[][] getColumnFormats() {
  	String[] colNames = dataTable.getFormattedColumnNames();
  	String[][] colFormats = new String[colNames.length][2];
  	for (int i = 0; i<colNames.length; i++)  {
  		colFormats[i][0] = colNames[i];
  		colFormats[i][1] = dataTable.getFormatPattern(colNames[i]);
  	}
    return colFormats;
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
  	TTrack track = getTrack();
    if (((TableTView)parent).columnsDialog != null 
    			&& e.getPropertyName().equals("track") //$NON-NLS-1$
    			&& e.getNewValue()==track) {
    	((TableTView)parent).refreshColumnsDialog(track);
    }
    if (e.getPropertyName().equals("text_column")) { //$NON-NLS-1$
  		// look for added and removed column names
  		String added = null;
  		for (String name: track.getTextColumnNames()) {
  			if (!textColumnNames.contains(name)) added = name;
  		}
  		String removed = null;
  		for (String name: textColumnNames) {
  			if (!track.getTextColumnNames().contains(name)) removed = name;
  		}
  		if (added!=null && removed!=null) {
  			// name changed    			
    		if (textColumnsVisible.contains(removed)) {
	    		textColumnsVisible.remove(removed);
	    		textColumnsVisible.add(added);
    		}
  		}
//  		else if (added!=null) {
//  			// new column is visible by default
//    		textColumnsVisible.add(added);    			
//  		}
  		else if (removed!=null) {
    		textColumnsVisible.remove(removed);    			
  		}
    	// else a text entry was changed    		
    	// refresh table and column visibility dialog
    	dataTable.refreshTable();
    	if (getParent() instanceof TableTView) {
    		TableTView view = (TableTView)getParent();
    		view.refreshColumnsDialog(track);
    	}
    	// update local list of names
    	textColumnNames.clear();
      textColumnNames.addAll(track.getTextColumnNames());

    }
    else if (e.getPropertyName().equals("units")) { // from trackerPanel //$NON-NLS-1$
    	dataTable.getTableHeader().repaint();
    }
    else super.propertyChange(e);
  }

  /**
   * Creates a snapshot of this view or its parent TViewChooser, if any.
   */
  public void snapshot() {
  	Component comp = this;
  	Container c = getParent();
  	while (c!=null) {
			if (c instanceof TViewChooser) {
				comp = c;
				break;
			}
			c = c.getParent();
  	}
    TrackerIO.ComponentImage ci = new TrackerIO.ComponentImage(comp);
    BufferedImage image = ci.getImage();
    int w = image.getWidth();
    int h = image.getHeight();
    if((w==0)||(h==0)) {
      return;
    }
    MeasuredImage mi = new MeasuredImage(image, 0, w, h, 0);
    
    // create ImageFrame using reflection
    OSPFrame frame = null;
    try {
			Class<?> type = Class.forName("org.opensourcephysics.frames.ImageFrame"); //$NON-NLS-1$
			Constructor<?>[] constructors = type.getConstructors();
			for(int i = 0; i<constructors.length; i++) {
			  Class<?>[] parameters = constructors[i].getParameterTypes();
			  if(parameters.length==1 && parameters[0]==MeasuredImage.class) {
			    frame = (OSPFrame) constructors[i].newInstance(new Object[] {mi});
			    break;
			  }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    if (frame==null) return;
    
    frame.setTitle(DisplayRes.getString("Snapshot.Title")); //$NON-NLS-1$
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setKeepHidden(false);    
    FontSizer.setFonts(frame, FontSizer.getLevel());
    frame.pack();
    frame.setVisible(true);
  }
  
  @Override
  public void setFont(Font font) {
    super.setFont(font);
    if (dataTable!=null) {
    	dataTable.setRowHeight(font.getSize()+4);
    	dataTable.getTableHeader().setFont(font);
    }
  }
 
  /**
   * Gets the TViewChooser that owns (displays) this view.
   * @return the TViewChooser
   */
  protected TViewChooser getOwner() {
  	// find TViewChooser with this view and copy that
  	TFrame frame = trackerPanel.getTFrame();
  	Container[] views = frame.getViews(trackerPanel);
  	for (int i = 0; i < views.length; i++) {
      if (views[i] instanceof TViewChooser) {
        TViewChooser chooser = (TViewChooser)views[i];
        if (chooser.getSelectedView() instanceof TableTView) {
        	TableTView tableView = (TableTView)chooser.getSelectedView();
        	TrackView view = tableView.getTrackView(tableView.getSelectedTrack());
        	if (view.equals(TableTrackView.this)) {
            return chooser;
        	}
        }
      }
  	}
  	return null;
  }
  
  /**
   * Gets the frame number for a view row. Returns -1 if not found.
   *
   * @param row the table row
   * @return the frame number
   */
  protected int getFrameAtRow(int row) {
  	// get value of independent variable at row
  	double val = getIndepVarValueAtRow(row);
  	TTrack track = getTrack();
  	String xVar = track.data.getDataset(0).getXColumnName();
  	int frameNum = track.getFrameForData(xVar, null, new double[] {val});
    return frameNum;
  }

  /**
   * Gets the view row for a given frame number. Returns -1 if none found.
   *
   * @param row the table row
   * @return the frame number
   */
  protected int getRowFromFrame(int frame) {
  	// look for "frame" dataset in data
  	ArrayList<Dataset> temp = data.getDatasets();
  	for (int i=0; i<temp.size(); i++) {
  		if ("frame".equals(temp.get(i).getYColumnName())) { //$NON-NLS-1$
  			double[] vals = temp.get(i).getYPoints();
  			for (int j=0; j<vals.length; j++) {
  				if (vals[j]==frame) {
  		  		SortDecorator decorator = (SortDecorator)dataTable.getModel();
  		  		return decorator.getSortedRow(j);
  				}
  			}
  		}
  	}
  	return -1;
  }

   /**
   * Gets the independent variable value at a view row.
   *
   * @param row the table row
   * @return the value
   */
  protected double getIndepVarValueAtRow(int row) {
    int col = dataTable.convertColumnIndexToView(0);
    Double val = null;
		try {
			val = (Double)dataTable.getValueAt(row, col);
		} catch (Exception e) {}
    return val==null? Double.NaN: val;
  }
  
 /**
   * Gets the view row at which an independent variable value is found.
   *
   * @param indepVarValue the value
   * @return the view row
   */
  protected int getRowFromIndepVarValue(double indepVarValue) {
    int col = dataTable.convertColumnIndexToView(0);
    for(int i = 0; i<dataTable.getRowCount(); i++) {
      if(indepVarValue==(Double)dataTable.getValueAt(i, col)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Gets the selected independent variable values.
   *
   * @return double[] of selected values
   */
  protected double[] getSelectedIndepVarValues() {
  	Double[] d = selectedIndepVarValues.toArray(new Double[0]);
    double[] vals = new double[d.length];
    for (int i=0; i<d.length; i++) {
    	vals[i] = d[i];
    }
    return vals;
  }

  /**
   * Sets the selected independent variable values.
   *
   * @param vals the values to select
   */
  protected void setSelectedIndepVarValues(double[] vals) {
    if(dataTable.getRowCount()<1) {
      return;
    }
    dataTable.removeRowSelectionInterval(0, dataTable.getRowCount()-1);
    for(int i = 0; i<vals.length; i++) {
      int row = getRowFromIndepVarValue(vals[i]);
      if(row>-1) {
      	dataTable.addRowSelectionInterval(row, row);
      }
    }
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    columnsPanel = new JPanel();
    columnsPanel.setBackground(Color.WHITE);
    columnsPanel.setLayout(new GridLayout(0, 4));
    columnsScroller = new JScrollPane(columnsPanel);
    javax.swing.border.Border empty = BorderFactory.createEmptyBorder(0, 3, 0, 2);
    javax.swing.border.Border etched = BorderFactory.createEtchedBorder();
    columnsScroller.setBorder(BorderFactory.createCompoundBorder(empty, etched));
    // button to open column selection dialog box
    columnsButton = new TButton() {
    	// override getMaximumSize method so has same height as chooser button
	    public Dimension getMaximumSize() {
	      Dimension dim = super.getMaximumSize();
	      Dimension min = getMinimumSize();
	    	Container c = getParent();
	    	while (c!=null) {
	  			if (c instanceof TViewChooser) {
		  			int h = ((TViewChooser)c).chooserButton.getHeight();
		  			dim.height = Math.max(h, min.height);
	  				break;
	  			}
	  			c = c.getParent();
	    	}
	      return dim;
	    }    	    	
    };
    columnsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	((TableTView)parent).showColumnsDialog(getTrack());
      }
    });
    // create column list
    refreshColumnCheckboxes();
    
    // button to show gaps in data (skipped frames)  
    gapsButton = new TButton() {    	
    	// override getMaximumSize method so has same height as chooser button
	    public Dimension getMaximumSize() {
	      Dimension dim = super.getMaximumSize();
	      Dimension min = getMinimumSize();
	    	Container c = getParent();
	    	while (c!=null) {
	  			if (c instanceof TViewChooser) {
		  			int h = ((TViewChooser)c).chooserButton.getHeight();
		  			dim.height = Math.max(h, min.height);
	  				break;
	  			}
	  			c = c.getParent();
	    	}
	      return dim;
	    }
	    
	    @Override
	    protected JPopupMenu getPopup() {
	    	JPopupMenu popup = new JPopupMenu();
	    	JCheckBoxMenuItem item = new JCheckBoxMenuItem(TrackerRes.getString("TableTrackView.MenuItem.Gaps.GapsVisible")); //$NON-NLS-1$
	    	item.setSelected(gapsButton.isSelected());
		    item.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		      	gapsButton.setSelected(!gapsButton.isSelected());
		      	dataTable.skippedFramesRenderer.setVisible(gapsButton.isSelected());
		      	if (gapsButton.isSelected()) {
		  	  		SortDecorator decorator = (SortDecorator)dataTable.getModel();
		  	  		decorator.reset();
		      	}
		      	dataTable.repaint();
		      	dataTable.getTableHeader().resizeAndRepaint();
		      }
		    });	    	
	    	popup.add(item);
	    	if (Tracker.enableAutofill) {
		    	item = new JCheckBoxMenuItem(TrackerRes.getString("TableTrackView.MenuItem.Gaps.AutoFill")); //$NON-NLS-1$
			    item.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
	  	      	PointMass p = (PointMass)TableTrackView.this.getTrack();
	  	      	p.setAutoFill(!p.isAutofill);
	  	      	p.repaint();
			      }
			    });
	      	PointMass p = (PointMass)TableTrackView.this.getTrack();
			    item.setSelected(p.isAutofill);
			    popup.addSeparator();
		    	popup.add(item);
	    	}
	    	FontSizer.setFonts(popup, FontSizer.getLevel());
	    	return popup;
	    }

    };
    gapsButton.setText(TrackerRes.getString("TableTrackView.Button.Gaps.Text")); //$NON-NLS-1$
    gapsButton.setSelected(Tracker.showGaps);
    
    // create popup and add menu items
    popup = new JPopupMenu();
    
    deleteDataFunctionItem = new JMenuItem();
    deleteDataFunctionItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	int index = Integer.parseInt(e.getActionCommand());
      	TTrack track = getTrack();
        FunctionTool tool = trackerPanel.getDataBuilder();
        FunctionPanel panel = tool.getPanel(track.getName());
        Dataset dataset = data.getDataset(index);
        // next line posts undo edit to FunctionPanel
        panel.getFunctionEditor().removeObject(dataset, true);
      }
    });

    goToFrameItem = new JMenuItem();
    goToFrameItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
					int row = Integer.parseInt(e.getActionCommand());
					int frameNum = getFrameAtRow(row);
					if (frameNum>-1) {
						VideoClip clip = trackerPanel.getPlayer().getVideoClip();
						int stepNum = clip.frameToStep(frameNum);
						trackerPanel.getPlayer().setStepNumber(stepNum);
					}
				} catch (Exception ex) {
				}
  	  }	
    });
    numberMenu = new JMenu();
    formatDialogItem = new JMenuItem();
    formatDialogItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int[] selected = dataTable.getSelectedColumns();        
        String[] selectedNames = new String[selected.length];
        for (int i=0; i<selectedNames.length; i++) {
        	String name = dataTable.getColumnName(selected[i]);
        	selectedNames[i] = name;
        }
        NumberFormatDialog dialog = NumberFormatDialog.getNumberFormatDialog(trackerPanel, getTrack(), selectedNames);
  	    dialog.setVisible(true);
  	  }	
    });
    showUnitsItem = new JMenuItem();
    showUnitsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackerPanel.setUnitsVisible(!trackerPanel.isUnitsVisible());
  	  }	
    });
    setUnitsItem = new JMenuItem();
    setUnitsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        UnitsDialog dialog = trackerPanel.getUnitsDialog();
  	    dialog.setVisible(true);
  	  }	
    });
    copyDataMenu = new JMenu();
    Action copyRawAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	TTrack track = getTrack();
        TrackerIO.copyTable(dataTable, false, track.getName());
      }
    };
    copyDataRawItem = new JMenuItem(copyRawAction);
    Action copyFormattedAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	TTrack track = getTrack();
        TrackerIO.copyTable(dataTable, true, track.getName());
      }
    };
    copyDataFormattedItem = new JMenuItem(copyFormattedAction);
    final Action setDelimiterAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        TrackerIO.setDelimiter(e.getActionCommand());        
        refreshGUI();
      }
    };
    setDelimiterMenu = new JMenu(setDelimiterAction);
    for (String key: TrackerIO.delimiters.keySet()) {
    	String delimiter = TrackerIO.delimiters.get(key);
    	JMenuItem item = new JRadioButtonMenuItem(key);
    	item.setActionCommand(delimiter);
    	item.addActionListener(setDelimiterAction);
    	delimiterButtonGroup.add(item);
    }
    Action addDelimiterAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	String delimiter = TrackerIO.delimiter;
      	Object response = JOptionPane.showInputDialog(TableTrackView.this, 
            TrackerRes.getString("TableTrackView.Dialog.CustomDelimiter.Message"),      //$NON-NLS-1$
            TrackerRes.getString("TableTrackView.Dialog.CustomDelimiter.Title"),        //$NON-NLS-1$
            JOptionPane.PLAIN_MESSAGE, null, null, delimiter);
        if (response!=null) {
        	String s = response.toString();
          TrackerIO.setDelimiter(s);
          TrackerIO.addCustomDelimiter(s);
          refreshGUI();
        }
      }
    };
    addDelimiterItem = new JMenuItem(addDelimiterAction);
    Action removeDelimiterAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	String[] choices = TrackerIO.customDelimiters.values().toArray(new String[1]);
      	Object response = JOptionPane.showInputDialog(TableTrackView.this, 
            TrackerRes.getString("TableTrackView.Dialog.RemoveDelimiter.Message"),      //$NON-NLS-1$
            TrackerRes.getString("TableTrackView.Dialog.RemoveDelimiter.Title"),        //$NON-NLS-1$
            JOptionPane.PLAIN_MESSAGE, null, choices, null);
        if (response!=null) {
        	String s = response.toString();
          TrackerIO.removeCustomDelimiter(s);
          refreshGUI();
        }
      }
    };
    removeDelimiterItem = new JMenuItem(removeDelimiterAction);
    Action copyImageAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	// find TViewChooser that owns this view and copy it
      	TViewChooser chooser = getOwner();
        if (chooser != null) {
          new TrackerIO.ComponentImage(chooser).copyToClipboard();
        }
      }
    };
    copyImageItem = new JMenuItem(copyImageAction);
    Action snapshotAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        snapshot();
      }
    };
    // add and remove text column items
    createTextColumnItem = new JMenuItem();
    createTextColumnItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String name = getUniqueColumnName(null, false);
      	TTrack track = getTrack();
        track.addTextColumn(name);
        // new column is visible by default
        textColumnsVisible.add(name);
      	// refresh table and column visibility dialog
      	dataTable.refreshTable();
      	if (getParent() instanceof TableTView) {
      		TableTView view = (TableTView)getParent();
      		view.refreshColumnsDialog(track);
      	}
      }
    });
    textColumnMenu = new JMenu();
    deleteTextColumnMenu = new JMenu();
    renameTextColumnMenu = new JMenu();

    snapshotItem = new JMenuItem(snapshotAction);
    // add dataBuilder item
    dataBuilderItem = new JMenuItem();
    dataBuilderItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	TTrack track = getTrack();
      	trackerPanel.getDataBuilder().setSelectedPanel(track.getName());
      	trackerPanel.getDataBuilder().setVisible(true);
      }
    });
    // add dataTool item
    dataToolItem = new JMenuItem();
    dataToolItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	TTrack track = getTrack();
      	DatasetManager toSend = new DatasetManager();
      	toSend.setID(data.getID());
      	toSend.setName(track.getName());
      	toSend.setXPointsLinked(true);
    	  int colCount = 0;
    	  ArrayList<Dataset> datasets = data.getDatasets();
    	  // always include linked independent variable first
      	Dataset next = datasets.get(0);
      	XMLControlElement control = new XMLControlElement(next);
      	next = toSend.getDataset(colCount++);
	    	control.loadObject(next, true, true);
	    	next.setYColumnVisible(false);
	    	next.setConnected(false);
	    	next.setMarkerShape(Dataset.NO_MARKER);
        for (int i = 0; i < checkBoxes.length; i++) {
          if (checkBoxes[i].isSelected()) {
          	if (i>=datasets.size()) {
          		next = track.convertTextToDataColumn(checkBoxes[i].getActionCommand());
          		if (next==null)	continue;
          	}
          	else next = datasets.get(i);
          	control = new XMLControlElement(next);
          	next = toSend.getDataset(colCount++);
    	    	control.loadObject(next, true, true);
    	    	next.setMarkerColor(track.getColor());
    	    	next.setConnected(true);
    	    	next.setXColumnVisible(false);
          }
        }
        DataTool tool = DataTool.getTool();
        tool.setUseChooser(false);
        tool.setSaveChangesOnClose(false);
        DataRefreshTool refresher = DataRefreshTool.getTool(data);
        try {
          tool.send(new LocalJob(toSend), refresher);
          tool.setVisible(true);
        }
        catch (RemoteException ex) {ex.printStackTrace();}
      }
    });
    // add print item
    printItem = new JMenuItem();
    printItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	// find TViewChooser that owns this view and print it
      	TViewChooser chooser = getOwner();
        if (chooser != null) {
          new TrackerIO.ComponentImage(chooser).print();
        }
      }
    });
    // add help item last
    helpItem = new JMenuItem();
    helpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null) {
	        frame.showHelp("datatable", 0); //$NON-NLS-1$
        }
      }
    });
    dataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  	dataTable.getTableHeader().setToolTipText(TrackerRes.getString("TableTrackView.Header.Tooltip"));          //$NON-NLS-1$
    dataTable.getTableHeader().addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        java.awt.Point mousePt = e.getPoint();
        int col = dataTable.columnAtPoint(mousePt);
        if (OSPRuntime.isPopupTrigger(e)) {
        	if (dataTable.getRowCount()>0 && dataTable.getSelectedRowCount()==0) {
	          dataTable.setColumnSelectionInterval(col, col);
	          dataTable.setRowSelectionInterval(0, dataTable.getRowCount()-1);
        	}
        	deleteDataFunctionItem.setActionCommand(""); //$NON-NLS-1$
        	// set action command of delete item if data function column selected
          String colName = dataTable.getColumnName(col);
          int index = data.getDatasetIndex(colName);
          if (index>-1) {
            Dataset dataset = data.getDataset(index);
            if (dataset instanceof DataFunction) {
            	deleteDataFunctionItem.setActionCommand(String.valueOf(index));
            	String s = TrackerRes.getString("TableTrackView.MenuItem.DeleteDataFunction"); //$NON-NLS-1$
              deleteDataFunctionItem.setText(s+" \""+colName+"\""); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
          
          goToFrameItem.setEnabled(false);
          getPopup().show(dataTable.getTableHeader(), e.getX(), e.getY()+8);
        }
        else {
          // double-click: select column and all rows
          if(e.getClickCount()==2) {
          	dataTable.setRowSelectionInterval(0, dataTable.getRowCount()-1); // all rows
          	dataTable.setColumnSelectionInterval(col, col);
            leadCol = col;
            // sort by independent variable
            dataTable.sort(0);
          }
          // control-click: add/remove columns to selection
          else if (e.isControlDown()) {
            if(dataTable.isColumnSelected(col)) {
            	dataTable.removeColumnSelectionInterval(col, col);
            } 
            else {
            	dataTable.addColumnSelectionInterval(col, col);
              if(dataTable.getSelectedColumns().length==1) {
                leadCol = col;
              }
            }
          }
          // shift-click: extend selection
          else if(e.isShiftDown() && dataTable.getSelectedRows().length>0) {
            if(leadCol<dataTable.getColumnCount()) {
            	dataTable.setColumnSelectionInterval(col, leadCol);
            }
          }
        }
      }
      
      public void mouseClicked(MouseEvent e) {
      	if (e.getClickCount()==2 || OSPRuntime.isPopupTrigger(e))
      		return;
        // single click: refresh selected rows
        double[] vals = getSelectedIndepVarValues();
        setSelectedIndepVarValues(vals);     	
      }
    });
    // data table: add right button mouse listener to copy data and double-click to select all
    dataTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (e.getClickCount() == 2) {
      		dataTable.selectAll();
      	}
      	if (OSPRuntime.isPopupTrigger(e)) {
          java.awt.Point mousePt = e.getPoint();
          int col = dataTable.columnAtPoint(mousePt);
        	deleteDataFunctionItem.setActionCommand(""); //$NON-NLS-1$
        	// set action command of delete item if data function column selected
          String colName = dataTable.getColumnName(col);
          int index = data.getDatasetIndex(colName);
          if (index>-1) {
            Dataset dataset = data.getDataset(index);
            if (dataset instanceof DataFunction) {
            	deleteDataFunctionItem.setActionCommand(String.valueOf(index));
            	String s = TrackerRes.getString("TableTrackView.MenuItem.DeleteDataFunction"); //$NON-NLS-1$
              deleteDataFunctionItem.setText(s+" \""+colName+"\""); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
          // set action command and title of goToFrame item
          int row = dataTable.rowAtPoint(mousePt);
          goToFrameItem.setEnabled(row>-1);
          if (goToFrameItem.isEnabled()) {
            goToFrameItem.setActionCommand(String.valueOf(row));
            String s = TrackerRes.getString("TableTrackView.Popup.Menuitem.GoToStep"); //$NON-NLS-1$
            int frameNum = getFrameAtRow(row);
						VideoClip clip = trackerPanel.getPlayer().getVideoClip();
						int stepNum = clip.frameToStep(frameNum);
            s += " "+stepNum; //$NON-NLS-1$
            goToFrameItem.setText(s);
          }         
          
          getPopup().show(dataTable, e.getX()+4, e.getY());
        }
      }
    });
    // override the datatable CTRL-C behavior
    InputMap im = dataTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
    Action newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	TTrack track = getTrack();
        TrackerIO.copyTable(dataTable, false, track.getName()); // copy raw data
      }
    };
    ActionMap am = dataTable.getActionMap();
    am.put(im.get(k), newAction);
    // override the pageUp and pageDown behaviors
    k = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
    newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
  			if (!trackerPanel.getPlayer().isEnabled()) return;
      	trackerPanel.getPlayer().back();
      }
    };
    am.put(im.get(k), newAction);
    k = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_DOWN_MASK);
    newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
  			if (!trackerPanel.getPlayer().isEnabled()) return;
				int n = trackerPanel.getPlayer().getStepNumber()-5;
				trackerPanel.getPlayer().setStepNumber(n);
      }
    };
    am.put(im.get(k), newAction);
    k = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
    newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
  			if (!trackerPanel.getPlayer().isEnabled()) return;
      	trackerPanel.getPlayer().step();
      }
    };
    am.put(im.get(k), newAction);
    k = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_DOWN_MASK);
    newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
  			if (!trackerPanel.getPlayer().isEnabled()) return;
				int n = trackerPanel.getPlayer().getStepNumber()+5;
				trackerPanel.getPlayer().setStepNumber(n);
      }
    };
    am.put(im.get(k), newAction);
    k = KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0);
    newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
  			if (!trackerPanel.getPlayer().isEnabled()) return;
  			trackerPanel.getPlayer().setStepNumber(0);
      }
    };
    am.put(im.get(k), newAction);
    k = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
    newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
  			if (!trackerPanel.getPlayer().isEnabled()) return;
  			VideoClip clip = trackerPanel.getPlayer().getVideoClip();
  			trackerPanel.getPlayer().setStepNumber(clip.getStepCount()-1);
      }
    };
    am.put(im.get(k), newAction);
  }
  
  protected JPopupMenu getPopup() {
  	numberMenu.setText(TrackerRes.getString("Popup.Menu.Numbers")); //$NON-NLS-1$
    formatDialogItem.setText(TrackerRes.getString("Popup.MenuItem.Formats")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    setUnitsItem.setText(TrackerRes.getString("Popup.MenuItem.Units")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    copyImageItem.setText(TrackerRes.getString("TMenuBar.Menu.CopyImage")); //$NON-NLS-1$
    snapshotItem.setText(DisplayRes.getString("DisplayPanel.Snapshot_menu_item")); //$NON-NLS-1$
    printItem.setText(TrackerRes.getString("TActions.Action.Print")); //$NON-NLS-1$
    helpItem.setText(TrackerRes.getString("Tracker.Popup.MenuItem.Help")); //$NON-NLS-1$
    createTextColumnItem.setText(TrackerRes.getString("TableTrackView.Action.CreateTextColumn.Text")); //$NON-NLS-1$
    textColumnMenu.setText(TrackerRes.getString("TableTrackView.Menu.TextColumn.Text")); //$NON-NLS-1$
    deleteTextColumnMenu.setText(TrackerRes.getString("TableTrackView.Action.DeleteTextColumn.Text")); //$NON-NLS-1$
    renameTextColumnMenu.setText(TrackerRes.getString("TableTrackView.Action.RenameTextColumn.Text")); //$NON-NLS-1$
    dataBuilderItem.setText(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
    dataToolItem.setText(TrackerRes.getString("TableTrackView.Popup.MenuItem.Analyze")); //$NON-NLS-1$
  	refreshCopyDataMenu(copyDataMenu);
  	popup.removeAll();
  	if (goToFrameItem.isEnabled()) {
      popup.add(goToFrameItem);
  	}

  	TTrack track = getTrack();
  	if (track==null) {
      if (trackerPanel.isEnabled("number.formats") || trackerPanel.isEnabled("number.units")) { //$NON-NLS-1$ //$NON-NLS-2$
    		if (popup.getComponentCount()>0) popup.addSeparator();
    		popup.add(numberMenu);
    		numberMenu.removeAll();
  	    if (trackerPanel.isEnabled("number.formats")) numberMenu.add(formatDialogItem); //$NON-NLS-1$
  	    if (trackerPanel.isEnabled("number.units")) numberMenu.add(setUnitsItem); //$NON-NLS-1$
      }
  		return popup;
  	}
    if (track.trackerPanel!=null && track.trackerPanel.isEnabled("edit.copyData")) { //$NON-NLS-1$
      if (popup.getComponentCount()>0) popup.addSeparator();
	    popup.add(copyDataMenu);
    }
    if (trackerPanel.isEnabled("number.formats") || trackerPanel.isEnabled("number.units") //$NON-NLS-1$ //$NON-NLS-2$
    		&& track.trackerPanel!=null) { 
  		if (popup.getComponentCount()>0) popup.addSeparator();
  		popup.add(numberMenu);
  		numberMenu.removeAll();
	    if (trackerPanel.isEnabled("number.formats")) numberMenu.add(formatDialogItem); //$NON-NLS-1$
	    if (trackerPanel.isEnabled("number.units")) numberMenu.add(setUnitsItem); //$NON-NLS-1$
    }
    
    // textColumnMenu
    if (trackerPanel.isEnabled("text.columns")) { //$NON-NLS-1$
    	textColumnMenu.removeAll();
	    if (popup.getComponentCount()>0) popup.addSeparator();
	    popup.add(textColumnMenu);
	    textColumnMenu.add(createTextColumnItem);
	    if (track.getTextColumnNames().size()>0) {
	    	deleteTextColumnMenu.removeAll();
	    	textColumnMenu.add(deleteTextColumnMenu);
	    	for (String next: track.getTextColumnNames()) {
	    		JMenuItem item = new JMenuItem(next);
	    		deleteTextColumnMenu.add(item);
	    		item.setActionCommand(next);
	    		item.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	          	TTrack track = getTrack();
	            track.removeTextColumn(e.getActionCommand());
	          }
	        });
	    	}
	    	renameTextColumnMenu.removeAll();
	    	textColumnMenu.add(renameTextColumnMenu);
	    	for (String next: track.getTextColumnNames()) {
	    		JMenuItem item = new JMenuItem(next);
	    		renameTextColumnMenu.add(item);
	    		item.setActionCommand(next);
	    		item.addActionListener(new ActionListener() {
	          public void actionPerformed(ActionEvent e) {
	          	String prev = e.getActionCommand();
	            String name = getUniqueColumnName(prev, false);
	            if (name!=null && !name.equals("") && !name.equals(prev)) { //$NON-NLS-1$
	            	// name has changed
	            	TTrack track = getTrack();
	            	track.renameTextColumn(prev, name);            	
	            }
	          }
	        });
	    	}
	    }
    }
    textColumnMenu.setEnabled(!track.isLocked());
    
  	if (!"".equals(deleteDataFunctionItem.getActionCommand())) { //$NON-NLS-1$
	  	popup.addSeparator();
	  	popup.add(deleteDataFunctionItem);
  	}

  	if (track.trackerPanel!=null
    		&& track.trackerPanel.isEnabled("edit.copyImage")) { //$NON-NLS-1$
	    popup.addSeparator();
	    popup.add(copyImageItem);
	    popup.add(snapshotItem);
    }
    if (track.trackerPanel!=null
    		&& (track.trackerPanel.isEnabled("data.builder") //$NON-NLS-1$
  			|| track.trackerPanel.isEnabled("data.tool"))) { //$NON-NLS-1$    
    	popup.addSeparator();
    	if (track.trackerPanel.isEnabled("data.builder")) //$NON-NLS-1$
    		popup.add(dataBuilderItem);
    	if (track.trackerPanel.isEnabled("data.tool")) //$NON-NLS-1$
    		popup.add(dataToolItem);
    }
    if (track.trackerPanel!=null
    		&& track.trackerPanel.isEnabled("file.print")) { //$NON-NLS-1$
	    popup.addSeparator();
	    popup.add(printItem);
    }
    if (popup.getComponentCount()>0) popup.addSeparator();
    popup.add(helpItem);
    FontSizer.setFonts(popup, FontSizer.getLevel());
    return popup;
  }
  
  /**
   * Gets a unique new name for a text column.
   *
   * @param previous the previous name (may be null)
   * @return the new name
   */
  protected String getUniqueColumnName(String previous, boolean tryAgain) {
  	if (previous==null) previous = ""; //$NON-NLS-1$
  	Object input = null;
  	TTrack track = getTrack();
  	if (tryAgain) {
	    input = JOptionPane.showInputDialog(track.trackerPanel.getTFrame(), 
	    		TrackerRes.getString("TableTrackView.Dialog.NameColumn.TryAgain")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
	    		TrackerRes.getString("TableTrackView.Dialog.NameColumn.Message"), //$NON-NLS-1$
	    		TrackerRes.getString("TableTrackView.Dialog.NameColumn.Title"),         //$NON-NLS-1$
	        JOptionPane.WARNING_MESSAGE, null, null, previous);  		
  	}
  	else {
	    input = JOptionPane.showInputDialog(track.trackerPanel.getTFrame(), 
	    		TrackerRes.getString("TableTrackView.Dialog.NameColumn.Message"), //$NON-NLS-1$
	    		TrackerRes.getString("TableTrackView.Dialog.NameColumn.Title"),         //$NON-NLS-1$
	        JOptionPane.QUESTION_MESSAGE, null, null, previous);
  	}
    if(input==null) {
      return null;
    }
    String name = ((String)input).trim();
    if (name.equals(previous)) return name;
    // check name for uniqueness
    boolean  unique = true;
    for (String next: getDataColumnNames()) {
    	if (next.equals(name)) {
    		unique = false;
    		break;
    	}
    }
    if (unique) {
	    for (String next: track.getTextColumnNames()) {
	    	if (next.equals(name)) {
	    		unique = false;
	    		break;
	    	}
	    }   	
    }
    if (!unique) return getUniqueColumnName(previous, true);
    return name;
  }

  /**
   * Refreshes a menu with appropriate copy data items for this view.
   *
   * @param menu the menu to refresh
   * @return the refreshed menu
   */
  protected JMenu refreshCopyDataMenu(JMenu menu) {
  	menu.removeAll();
  	menu.add(copyDataRawItem);
  	menu.add(copyDataFormattedItem);
  	menu.addSeparator();
  	menu.add(setDelimiterMenu);
  	if (dataTable.getSelectedRowCount()==0)
  		menu.setText(TrackerRes.getString("TableTrackView.Action.CopyData")); //$NON-NLS-1$
  	else
  		menu.setText(TrackerRes.getString("TableTrackView.MenuItem.CopySelectedData")); //$NON-NLS-1$
  	copyDataRawItem.setText(TrackerRes.getString("TableTrackView.MenuItem.Unformatted")); //$NON-NLS-1$
    copyDataFormattedItem.setText(TrackerRes.getString("TableTrackView.MenuItem.Formatted")); //$NON-NLS-1$
    setDelimiterMenu.setText(TrackerRes.getString("TableTrackView.Menu.SetDelimiter")); //$NON-NLS-1$
    addDelimiterItem.setText(TrackerRes.getString("TableTrackView.MenuItem.AddDelimiter")); //$NON-NLS-1$
    removeDelimiterItem.setText(TrackerRes.getString("TableTrackView.MenuItem.RemoveDelimiter")); //$NON-NLS-1$
  	// refresh delimiter menu
  	setDelimiterMenu.removeAll();
  	String delimiter = TrackerIO.getDelimiter();
  	// remove all custom delimiter items from button group
  	Enumeration<AbstractButton> en = delimiterButtonGroup.getElements();
  	for (; en.hasMoreElements();) {
  		JMenuItem item = (JMenuItem)en.nextElement();
  		String delim = item.getActionCommand();
  		if (!TrackerIO.delimiters.containsValue(delim))
  			delimiterButtonGroup.remove(item);
  	}
  	// add all button group items to menu
  	en = delimiterButtonGroup.getElements();
  	for (; en.hasMoreElements();) {
  		JMenuItem item = (JMenuItem)en.nextElement();
     	setDelimiterMenu.add(item);
    	if (delimiter.equals(item.getActionCommand()))
    		item.setSelected(true);
  	}
  	// add new custom delimiter items
  	boolean hasCustom = !TrackerIO.customDelimiters.isEmpty();
    if (hasCustom) {
    	setDelimiterMenu.addSeparator();
	  	for (String key: TrackerIO.customDelimiters.keySet()) {
	    	JMenuItem item = new JRadioButtonMenuItem(key);
	    	item.setActionCommand(TrackerIO.customDelimiters.get(key));
	    	item.addActionListener(new AbstractAction() {
	        public void actionPerformed(ActionEvent e) {
	          TrackerIO.setDelimiter(e.getActionCommand());        
	        }
	      });
	    	delimiterButtonGroup.add(item);
	     	setDelimiterMenu.add(item);
	    	if (delimiter.equals(item.getActionCommand()))
	    		item.setSelected(true);
	  	}
    }
  	setDelimiterMenu.addSeparator();
  	setDelimiterMenu.add(addDelimiterItem);
    if (hasCustom)
    	setDelimiterMenu.add(removeDelimiterItem);
  	return menu;
  }

  /**
   * Gets an array of all column names.
   *
   * @return the column names
   */
  protected String[] getDataColumnNames() {
  	ArrayList<String> names = new ArrayList<String>();
  	// first add independent variable
    Dataset dataset = data.getDataset(0);
    String name = dataset.getXColumnName();
    names.add(name);
    // then add other variables
  	TTrack track = getTrack();
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
    return names.toArray(new String[0]);
  }

  /**
   * Refreshes the column visibility checkboxes.
   *
   * @return a JScrollPane with the refreshed column checkboxes
   */
  protected JScrollPane refreshColumnCheckboxes() {
  	TTrack track = getTrack();
  	JCheckBox[] prev = checkBoxes;
    // create check box array: one item per dataset plus one per textColumn
    int datasetCount = data.getDatasets().size();
    int textColumnCount = track.getTextColumnNames().size();
    // keep selected column names
    ArrayList<String> names = new ArrayList<String>();
    if (prev != null) {
    	for (int i = 0; i < prev.length; i++) {
    		if (prev[i].isSelected()) names.add(prev[i].getText());
    	}
    }
    checkBoxes = new JCheckBox[datasetCount+textColumnCount];
    // data column checkboxes
    for (int i = 0; i < datasetCount; i++) {
      Dataset dataset = data.getDataset(i);
      String name = dataset.getYColumnName();
      String s = TeXParser.removeSubscripting(name);
      checkBoxes[i] = new JCheckBox(s);
      boolean selected = names.contains(s) 
		  		|| (prev != null && datasetCount >= prev.length-textColumnCount 
		  		&& i < prev.length-textColumnCount && prev[i].isSelected());
      checkBoxes[i].setBackground(Color.white);
      checkBoxes[i].setFont(font);
      checkBoxes[i].setSelected(selected);
      checkBoxes[i].setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 0));
      checkBoxes[i].setActionCommand(name);
      checkBoxes[i].setToolTipText(track.getDataDescription(i+1));
      checkBoxes[i].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	if (refresh) refresh(trackerPanel.getFrameNumber());
        	trackerPanel.changed = true;
        }
      });
      checkBoxes[i].setOpaque(false);
    }
    // text column checkboxes
    for (int i = datasetCount; i < datasetCount+textColumnCount; i++) {
    	String name = track.getTextColumnNames().get(i-datasetCount);
      String s = TeXParser.removeSubscripting(name);
      checkBoxes[i] = new JCheckBox(s);
      checkBoxes[i].setBackground(Color.white);
      checkBoxes[i].setFont(font);
      checkBoxes[i].setSelected(textColumnsVisible.contains(name));
      checkBoxes[i].setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 0));
      checkBoxes[i].setActionCommand(name);
      checkBoxes[i].addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	JCheckBox item = (JCheckBox)e.getSource();
        	if (item.isSelected()) {
        		textColumnsVisible.add(e.getActionCommand());
        	}
        	else {
        		textColumnsVisible.remove(e.getActionCommand());
        	}
        	if (refresh) refresh(trackerPanel.getFrameNumber());
        	trackerPanel.changed = true;
        }
      });
      checkBoxes[i].setOpaque(false);
    }
    columnsPanel.removeAll();
    ArrayList<Integer> dataOrder = track.getPreferredDataOrder();
    ArrayList<JCheckBox> added = new ArrayList<JCheckBox>();
    // first add in preferred order
  	for (int i = 0; i < dataOrder.size(); i++) {
  		columnsPanel.add(checkBoxes[dataOrder.get(i)]);
    	added.add(checkBoxes[dataOrder.get(i)]);
    }
  	// then add any that were missed
		for (int j = 0; j<checkBoxes.length; j++) {
			if (!added.contains(checkBoxes[j])) {
  			columnsPanel.add(checkBoxes[j]);
			}
		}    	
    return columnsScroller;
  }
  
  /**
   * A class to provide textColumn data for the dataTable.
   */
  class TextColumnTableModel extends AbstractTableModel {
    public String getColumnName(int col) {    	
    	int i = 0;    	
    	TTrack track = getTrack();
    	for (String name: track.getTextColumnNames()) {
    		if (textColumnsVisible.contains(name)) {
    			if (i==col) return name;
    			i++;
    		}
    	}
      return "unknown"; //$NON-NLS-1$
    }

    public int getRowCount() {
      return tableData.getRowCount();
    }

    public int getColumnCount() {
      return textColumnsVisible.size();
    }

    public Object getValueAt(int row, int col) {
      String columnName = getColumnName(col);
    	TTrack track = getTrack();
      // convert row to frame number
      DatasetManager data = track.getData(track.trackerPanel);
      int index = data.getDatasetIndex("frame"); //$NON-NLS-1$
      if (index>-1) {
      	double frame = data.getDataset(index).getYPoints()[row];
	      return track.getTextColumnEntry(columnName, (int)frame);      	
      }
      // if no frame numbers defined (eg line profile), use row number
	    return track.getTextColumnEntry(columnName, row);
    }
    
    /**
     * Sets the value at the given cell.
     *
     * @param value the value
     * @param row the row index
     * @param col the column index
     */
    public void setValueAt(Object value, int row, int col) {
      String columnName = getColumnName(col);
    	TTrack track = getTrack();
      // convert row to frame number
      DatasetManager data = track.getData(track.trackerPanel);
      int index = data.getDatasetIndex("frame"); //$NON-NLS-1$
      if (index>-1) {
      	double frame = data.getDataset(index).getYPoints()[row];
	      if (track.setTextColumnEntry(columnName, (int)frame, (String)value)) {
	      	trackerPanel.changed = true;
	      }
	      return;
      }
      // if no frame numbers defined (eg line profile), use row number
      if (track.setTextColumnEntry(columnName, row, (String)value)) {
      	trackerPanel.changed = true;
      }
    }


    public boolean isCellEditable(int row, int col) {
    	TTrack track = getTrack();
      return !track.isLocked();
    }

    public Class<?> getColumnClass(int col) {
      return String.class;
    }

  }

  /**
   * A cell editor for textColumn cells.
   */
  class TextColumnEditor extends AbstractCellEditor implements TableCellEditor {
  	Color defaultEditingColor;
    JPanel panel = new JPanel(new BorderLayout());
    JTextField field = new JTextField();

    // Constructor.
    TextColumnEditor() {
      defaultEditingColor = field.getSelectionColor();
      panel.add(field, BorderLayout.CENTER);
      panel.setOpaque(false);
      field.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 0));
      field.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            stopCellEditing();
          } else if(field.isEnabled()) {
            field.setBackground(Color.yellow);
          }
        }

      });
      field.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
        	// request focus immediately to keep it
          field.requestFocusInWindow();
        }
      });
    }

    // Gets the component to be displayed while editing.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      field.setBackground(Color.white);
      field.setSelectionColor(defaultEditingColor);
      field.setEditable(true);
      if (value==null) value = ""; //$NON-NLS-1$
      field.setText(value.toString());
      return panel;
    }
    
    // Determines when editing starts.
    public boolean isCellEditable(EventObject e) {
      if(e==null || e instanceof MouseEvent) {
      	TTrack track = getTrack();
      	return !track.isLocked();
      }
      return false;
    }

    // Called when editing is completed.
    public Object getCellEditorValue() {
      dataTable.requestFocusInWindow();
      if(field.getBackground()!=Color.white) {
        field.setBackground(Color.white);
      }
      return field.getText();
    }

  }
  
	// the default table cell renderer when no PrecisionRenderer is used
  class NumberFieldRenderer extends NumberField implements TableCellRenderer {
  	
  	DefaultTableCellRenderer defaultRenderer;

		public NumberFieldRenderer() {
			super(1);
			defaultRenderer = new DefaultTableCellRenderer();
			defaultRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = defaultRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);
			if (value instanceof Double && c instanceof JLabel) {
				// show number as formatted by this NumberField
				setValue((Double)value);
				((JLabel)c).setText(getText());				
			}
			return c;
		}
  	
  }
  
	// the default table cell renderer when no PrecisionRenderer is used
  class SkippedFramesRenderer implements TableCellRenderer {
  	
  	TableCellRenderer baseRenderer;
  	Border belowBorder, aboveBorder;
  	boolean visible = Tracker.showGaps;

		public SkippedFramesRenderer() {
			belowBorder = BorderFactory.createMatteBorder(0,0,1,0,Color.red);
			Border space = BorderFactory.createEmptyBorder(0, 1, 0, 1);
			belowBorder = BorderFactory.createCompoundBorder(belowBorder, space);
			aboveBorder = BorderFactory.createMatteBorder(1,0,0,0,Color.red);
			space = BorderFactory.createEmptyBorder(0, 1, 1, 1);
			aboveBorder = BorderFactory.createCompoundBorder(aboveBorder, space);
		}
		
		public void setBaseRenderer(TableCellRenderer renderer) {
			baseRenderer = renderer;
		}
		
		public void setVisible(boolean vis) {
			visible = vis;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = baseRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);
			if (visible) {
				// add red above or below border to identify skipped frames
	    	TTrack track = getTrack();
	    	if (track instanceof PointMass) {
	    		PointMass p = (PointMass)track;
	    		if (p.trackerPanel!=null) {
	    			VideoClip clip = p.trackerPanel.getPlayer().getVideoClip();
	      		int frameNum = getFrameAtRow(row);
	      		int stepNum = clip.frameToStep(frameNum);
	      		for (int i: p.skippedSteps) {
	      			if (stepNum+1==i) {
	      				((JLabel)c).setBorder(belowBorder);
	      			}
	      			else if (stepNum-1==i) {
	      				((JLabel)c).setBorder(aboveBorder);
	      			}
	      		}
	    		}				
				}
			}
			return c;
		}
  	
  }
  
  class TrackDataTable extends DataTable {
  	
  	NumberFieldRenderer numberFieldRenderer = new NumberFieldRenderer();
  	SkippedFramesRenderer skippedFramesRenderer = new SkippedFramesRenderer(); 
  	
  	TrackDataTable() {
  		super();
  		TableCellRenderer renderer = getTableHeader().getDefaultRenderer();
  		if (renderer instanceof DataTable.HeaderRenderer) {
  			renderer = ((DataTable.HeaderRenderer)renderer).getBaseRenderer();
  		}
      TableCellRenderer headerRenderer = new HeaderUnitsRenderer(this, renderer);
      getTableHeader().setDefaultRenderer(headerRenderer);
  	}
  	
  	@Override
    public void refreshTable() {
  		// model for this table assumed to be a SortDecorator
  		// always reset the decorator before changing table structure
  		SortDecorator decorator = (SortDecorator)getModel();
  		int col = decorator.getSortedColumn();
  		decorator.reset();
      // save selected rows and columns
      int[] rows = getSelectedRows();
      int[] cols = getSelectedColumns();
      // refresh table
      super.refreshTable();
      // sort if needed
      if (col>-1) sort(col);
      // restore selected rows and columns
      for (int i = 0; i < rows.length; i++) {
      	if (rows[i] < getRowCount())
      		addRowSelectionInterval(rows[i], rows[i]);
      }
      for (int i = 0; i < cols.length; i++) {
      	if (cols[i] < getColumnCount())
      		addColumnSelectionInterval(cols[i], cols[i]);
      }
    	// find TViewChooser with this view
    	TFrame frame = trackerPanel.getTFrame();
    	if (frame!=null) {
	    	Container[] views = frame.getViews(trackerPanel);
	    	for (int i = 0; i < views.length; i++) {
	        if (views[i] instanceof TViewChooser) {
	          TViewChooser chooser = (TViewChooser)views[i];
	          if (chooser.getSelectedView() instanceof TableTView) {
	          	TableTView tableView = (TableTView)chooser.getSelectedView();
	          	TrackView view = tableView.getTrackView(tableView.getSelectedTrack());
	          	if (view != null && view.equals(TableTrackView.this)) {
	          		chooser.refreshToolbar();
	          	}
	          }
	        }
	    	}
    	}

    }
    
  	@Override
    public TableCellEditor getCellEditor(int row, int column) {
    	// only text columns are editable, so always return textColumnEditor
      return textColumnEditor;
    }
    
  	@Override
    public boolean isCellEditable(int row, int col) {
    	// true only for text (String) columns
      int i = dataTable.convertColumnIndexToModel(col);
      return dataTable.getModel().getColumnClass(i).equals(String.class);
    }
  	
  	@Override
  	public TableCellRenderer getDefaultRenderer(Class<?> type) {
  		if (type.isAssignableFrom(Double.class)) {
  			return numberFieldRenderer;
  		}
  		return super.getDefaultRenderer(type);
  	}
  	
  	@Override
    public TableCellRenderer getCellRenderer(int row, int column) {
    	TableCellRenderer renderer = super.getCellRenderer(row, column);
  		skippedFramesRenderer.setBaseRenderer(renderer);
  		return skippedFramesRenderer;
    }
  	
    @Override
    public void sort(int col) {
      if (col>0 && gapsButton.isSelected()) {
      	gapsButton.doClick(0);
      }
      super.sort(col);
    }

  }
  
  public class HeaderUnitsRenderer extends DataTable.HeaderRenderer {

		public HeaderUnitsRenderer(DataTable table, TableCellRenderer renderer) {
			table.super(renderer);
		}
		
		@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			TTrack track = getTrack();
			if (track.trackerPanel!=null) {
				String var = textLine.getText();
				String units = track.trackerPanel.getUnits(track, var);
				if (!"".equals(units)) { //$NON-NLS-1$
		      if (OSPRuntime.isMac()) {
		      	var = TeXParser.removeSubscripting(var);
		      }
					var += " ("+units.trim()+")"; //$NON-NLS-1$ //$NON-NLS-2$
		      if (OSPRuntime.isMac()) {
		        if (c instanceof JLabel) {
		          ((JLabel)c).setText(var);
		        }
		      }
		      textLine.setText(var);
				}
			}
			return c;
		}
  	
  }

}
