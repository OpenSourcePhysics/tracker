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

import java.util.*;
import java.rmi.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.*;
import org.opensourcephysics.display.DataTable.NumberFormatDialog;
import org.opensourcephysics.frames.ImageFrame;
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

  // instance fields
  protected TableTView parentView;
  protected DataTable dataTable;
  protected JButton columnsButton;
  protected JPopupMenu popup;
  protected JPanel columnsPanel;
  protected JScrollPane columnsScroller;
  protected DatasetManager data;
  protected DatasetManager tableData;
  protected JCheckBox[] checkBoxes;
  protected JMenuItem formatDialogItem;
  protected JMenu copyDataMenu;
  protected JMenuItem copyDataRawItem, copyDataFormattedItem;
  protected JMenu setDelimiterMenu;
  ButtonGroup delimiterButtonGroup = new ButtonGroup();
  protected JMenuItem addDelimiterItem, removeDelimiterItem;
  protected JMenuItem copyImageItem, snapshotItem, printItem, helpItem;
  protected JMenuItem dataToolItem, dataBuilderItem, deleteDataFunctionItem;
  protected JMenuItem createTextColumnItem;
  protected JMenu textColumnMenu, deleteTextColumnMenu, renameTextColumnMenu;
  protected String xVar, yVar;
  protected boolean refresh = true;
  protected boolean highlightVisible = true;
  protected int highlightRow; // highlighted table row, or -1
  protected int leadCol;
  protected Font font = new JTextField().getFont();
  protected TreeSet<Double> selectedIndepVarValues // used when sorting
  		= new TreeSet<Double>();
  protected Map<String, TableCellRenderer> degreeRenderers
  		= new HashMap<String, TableCellRenderer>();
  protected TextColumnTableModel textColumnModel;
  protected TextColumnEditor textColumnEditor;
  protected Set<String> textColumnsVisible = new TreeSet<String>();
  protected ArrayList<String> textColumnNames = new ArrayList<String>();

  /**
   * Constructs a TrackTableView of the specified track on the specified tracker panel.
   *
   * @param track the track
   * @param panel the tracker panel
   * @param view the TableTView that will display this
   */
  public TableTrackView(TTrack track, TrackerPanel panel, TableTView view) {
    super(track, panel);
    parentView = view;
    track.addPropertyChangeListener("text_column", this); //$NON-NLS-1$
    textColumnNames.addAll(track.getTextColumnNames());
    for (String name: track.getTextColumnNames()) {
  		textColumnsVisible.add(name);
    }
    // create the DataTable
    textColumnEditor = new TextColumnEditor();
    dataTable = new DataTable() {
      public void refreshTable() {
        // save selected rows and columns
        int[] rows = getSelectedRows();
        int[] cols = getSelectedColumns();
        // refresh table
        super.refreshTable();
        // restore selected rows and columns
        for (int i = 0; i < rows.length; i++) {
        	if (rows[i] < getRowCount())
        		addRowSelectionInterval(rows[i], rows[i]);
        }
        for (int i = 0; i < cols.length; i++) {
        	if (cols[i] < getColumnCount())
        		addColumnSelectionInterval(cols[i], cols[i]);
        }
      }
      public TableCellEditor getCellEditor(int row, int column) {
      	// only text columns are editable, so always return textColumnEditor
        return textColumnEditor;
      }
      public boolean isCellEditable(int row, int col) {
      	// true only for text (String) columns
        int i = dataTable.convertColumnIndexToModel(col);
        return dataTable.getModel().getColumnClass(i).equals(String.class);
      }

    };   
    
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
  }

  /**
   * Refreshes this view.
   *
   * @param frameNumber the frame number
   */
  public void refresh(int frameNumber) {
  	if (!isRefreshEnabled()) return;
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh "+frameNumber); //$NON-NLS-1$
    
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
          		dataTable.setFormatPattern(yTitle, "0.0"); //$NON-NLS-1$
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
    // display the table
    dataTable.refreshTable();
    setHighlighted(frameNumber);
  }

  /**
   * Refreshes the GUI.
   */
  void refreshGUI(){
  	columnsButton.setText(TrackerRes.getString("TableTrackView.Button.SelectTableData")); //$NON-NLS-1$
    columnsButton.setToolTipText(TrackerRes.getString("TableTrackView.Button.SelectTableData.ToolTip")); //$NON-NLS-1$
    track.dataValid = false; // triggers data refresh
    track.getData(trackerPanel); // load the current data
    refreshColumnCheckboxes();    
    refresh(trackerPanel.getFrameNumber());
  }

  /**
   * Implements TrackView interface.
   */
  void dispose() {/** empty block */}
  
  /**
   * Gets the datatable.
   *
   * @return the datatable
   */
  public DataTable getDataTable() {
    return dataTable;
  }

  /**
   * Gets the toolbar components
   *
   * @return an ArrayList of components to be added to a toolbar
   */
  public ArrayList<Component> getToolBarComponents() {
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
  	// check displayed data columns--default is columns 0 and 1 only
  	int n = data.getDatasets().size();
  	for (int i = 0; i < n; i++) {
  		boolean selected = checkBoxes[i].isSelected();
  		boolean shouldBe = i < 2;
  		if ((shouldBe && !selected) || (!shouldBe && selected)) return true;
  	}
  	// check displayed text columns--default is all are displayed
  	for (String name: track.getTextColumnNames()) {
  		if (!textColumnsVisible.contains(name)) return true;
  	}
  	// check for formatting--default is no formatting
  	if (dataTable.getFormattedColumnNames().length>0)
  		return true;
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

  /**
   * Sets the highlighted point.
   *
   * @param frameNumber the frame number
   */
  protected void setHighlighted(int frameNumber) {
    // assume no highlights
    highlightRow = -1;
    if (!highlightVisible) return;
    Step[] steps = track.getSteps();
    int row = -1;
    VideoClip clip = null;
    if (track.trackerPanel != null) {
    	clip = track.trackerPanel.getPlayer().getVideoClip();
    }
    // look for row to highlight
    for (int i = 0; i < steps.length; i++) {
      if (steps[i] != null 
      			&& steps[i].dataVisible 
      			&& clip != null 
      			&& clip.includesFrame(steps[i].getFrameNumber())) {
        row++;
        if (steps[i].getFrameNumber() == frameNumber) {
        	if (row >= dataTable.getRowCount()) return;
        	highlightRow = row;
          break;
        }
      }
    }
    // select highlighted row, or clear selection if none found
    Runnable runner = new Runnable() {
      public synchronized void run() {
      	if (highlightRow >= dataTable.getRowCount() || highlightRow < 0) {
      		dataTable.clearSelection();
      		return;
      	}
        dataTable.setRowSelectionInterval(highlightRow, highlightRow);
        int cols = dataTable.getColumnCount();
        dataTable.setColumnSelectionInterval(0, cols-1);
      }
    };
    if(SwingUtilities.isEventDispatchThread()) runner.run();
    else SwingUtilities.invokeLater(runner);

  }

  /**
   * Gets an array of visible column names. Used for saving/loading xml
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
    if (parentView.columnsDialog != null && parentView.columnsDialog.isVisible() 
    			&& e.getPropertyName().equals("track") //$NON-NLS-1$
    			&& e.getNewValue() == track) {
      parentView.showColumnsDialog(getTrack());
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
  		else if (added!=null) {
  			// new column is visible by default
    		textColumnsVisible.add(added);    			
  		}
  		else if (removed!=null) {
    		textColumnsVisible.remove(removed);    			
  		}
    	// else a text entry was changed    		
    	// refresh table and column visibility dialog, if visible
    	dataTable.refreshTable();
    	if (getParent() instanceof TableTView) {
    		TableTView view = (TableTView)getParent();
    		if (view.dialogVisible) {
    			view.showColumnsDialog(track);
    		}
    	}
    	// update local list of names
    	textColumnNames.clear();
      textColumnNames.addAll(track.getTextColumnNames());

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
    ImageFrame frame = new ImageFrame(mi);
    frame.setTitle(DisplayRes.getString("Snapshot.Title")); //$NON-NLS-1$
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setKeepHidden(false);    
    frame.setVisible(true);
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
	    	Container c = getParent();
	    	while (c!=null) {
	  			if (c instanceof TViewChooser) {
		  			dim.height = ((TViewChooser)c).chooserButton.getHeight();
	  				break;
	  			}
	  			c = c.getParent();
	    	}
	      return dim;
	    }    	    	
    };
    columnsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        parentView.showColumnsDialog(getTrack());
      }
    });
    // create column list
    refreshColumnCheckboxes();
    // create popup and add menu items
    popup = new JPopupMenu();
    
    deleteDataFunctionItem = new JMenuItem();
    deleteDataFunctionItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	int index = Integer.parseInt(e.getActionCommand());
        FunctionTool tool = trackerPanel.getDataBuilder();
        FunctionPanel panel = tool.getPanel(track.getName());
        Dataset dataset = data.getDataset(index);
        // next line posts undo edit to FunctionPanel
        panel.getFunctionEditor().removeObject(dataset, true);
      }
    });

    formatDialogItem = new JMenuItem();
    formatDialogItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String[] cols = getDataColumnNames();
        int[] selected = dataTable.getSelectedColumns();
        String[] selectedNames = new String[selected.length];
        for (int i=0; i<selectedNames.length; i++) {
        	String name = dataTable.getColumnName(selected[i]);
        	selectedNames[i] = name;
        }
        NumberFormatDialog dialog = dataTable.getFormatDialog(cols, selectedNames);
  	    dialog.setVisible(true);
  	  }	
    });
    copyDataMenu = new JMenu();
    Action copyRawAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        TrackerIO.copyTable(dataTable, false, track.getName());
      }
    };
    copyDataRawItem = new JMenuItem(copyRawAction);
    Action copyFormattedAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
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
        track.addTextColumn(name);
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
      	trackerPanel.getDataBuilder().setSelectedPanel(track.getName());
      	trackerPanel.getDataBuilder().setVisible(true);
      }
    });
    // add dataTool item
    dataToolItem = new JMenuItem();
    dataToolItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
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
      	if (dataTable.getRowCount()==0) return;        	
        java.awt.Point mousePt = e.getPoint();
        int col = dataTable.columnAtPoint(mousePt);
        if (OSPRuntime.isPopupTrigger(e)) {
        	if (dataTable.getSelectedRowCount()==0) {
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
    // add right button mouse listener to copy data and double-click to select all
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
          getPopup().show(TableTrackView.this, e.getX()-10, e.getY());
        }
      }
    });
    // override the datatable CTRL-C behavior
    InputMap im = dataTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
    Action newAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
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
    formatDialogItem.setText(ToolsRes.getString("DataToolTable.Popup.MenuItem.NumberFormat")); //$NON-NLS-1$
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
  	if (!"".equals(deleteDataFunctionItem.getActionCommand())) { //$NON-NLS-1$
	  	popup.add(deleteDataFunctionItem);
	  	popup.addSeparator();
  	}
    popup.add(formatDialogItem);
    if (track!=null && track.trackerPanel!=null
    		&& track.trackerPanel.isEnabled("edit.copyData")) { //$NON-NLS-1$
	    popup.addSeparator();
	    popup.add(copyDataMenu);
    }
    popup.addSeparator();
    // textColumnMenu
    textColumnMenu.removeAll();
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
            	track.renameTextColumn(prev, name);            	
            }
          }
        });
    	}
    }
    textColumnMenu.setEnabled(!track.isLocked());
    
    if (track!=null && track.trackerPanel!=null
    		&& track.trackerPanel.isEnabled("edit.copyImage")) { //$NON-NLS-1$
	    popup.addSeparator();
	    popup.add(copyImageItem);
	    popup.add(snapshotItem);
    }
    if (track!=null && track.trackerPanel!=null
    		&& (track.trackerPanel.isEnabled("data.builder") //$NON-NLS-1$
  			|| track.trackerPanel.isEnabled("data.tool"))) { //$NON-NLS-1$    
    	popup.addSeparator();
    	if (track.trackerPanel.isEnabled("data.builder")) //$NON-NLS-1$
    		popup.add(dataBuilderItem);
    	if (track.trackerPanel.isEnabled("data.tool")) //$NON-NLS-1$
    		popup.add(dataToolItem);
    }
    if (track!=null && track.trackerPanel!=null
    		&& track.trackerPanel.isEnabled("file.print")) { //$NON-NLS-1$
	    popup.addSeparator();
	    popup.add(printItem);
    }
    popup.addSeparator();
    popup.add(helpItem);
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
		  		|| (prev != null && datasetCount >= prev.length 
		  		&& i < prev.length && prev[i].isSelected());
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



}
