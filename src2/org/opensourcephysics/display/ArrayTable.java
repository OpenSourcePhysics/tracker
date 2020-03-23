/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A JTable to display int, double and String array values.
 * Version 1.1 uses a Timer to coalesce Refresh Table events
 *
 * @author Douglas Brown
 * @author Wolfgang Christian
 * @version 1.1
 */
public class ArrayTable extends JTable implements ActionListener {
  int refreshDelay = 300;                                                     // time in ms to delay refresh events
  javax.swing.Timer refreshTimer = new javax.swing.Timer(refreshDelay, this); // delay for refreshTable
  ArrayTableModel tableModel;
  ArrayIndexRenderer indexRenderer = new ArrayIndexRenderer();
  ArrayCellRenderer cellRenderer = new ArrayCellRenderer();
  java.util.Dictionary<Integer, DecimalFormat> formatDictionary = new java.util.Hashtable<Integer, DecimalFormat>();
  String formatPattern = "0.000";                                             //$NON-NLS-1$
  DecimalFormat defaultFormat = new DecimalFormat(formatPattern);
  Object prevValue;

  /**
   * Constructor for 1D int array.
   *
   * @param array the array
   */
  public ArrayTable(int[] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Constructor for 2D int array.
   *
   * @param array the array
   */
  public ArrayTable(int[][] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Constructor for 1D double array.
   *
   * @param array the array
   */
  public ArrayTable(double[] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Constructor for 2D double array.
   *
   * @param array the array
   */
  public ArrayTable(double[][] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Constructor for 1D String array.
   *
   * @param array the array
   */
  public ArrayTable(String[] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Constructor for 2D String array.
   *
   * @param array the array
   */
  public ArrayTable(String[][] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Constructor for 1D boolean array.
   *
   * @param array the array
   */
  public ArrayTable(boolean[] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Constructor for 2D boolean array.
   *
   * @param array the array
   */
  public ArrayTable(boolean[][] array) {
    tableModel = new ArrayTableModel(array);
    init();
  }

  /**
   * Initializes the table.
   */
  protected void init() {
    refreshTimer.setRepeats(false);
    refreshTimer.setCoalesce(true);
    setModel(tableModel);
    tableModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int col = tableModel.showRowNumber ? e.getColumn()+1 : e.getColumn();
        Object value=getValueAt(row, col);
        if((value!=null)&&!value.equals(prevValue)) {
          // forward the table model event to property change listeners
          ArrayTable.this.firePropertyChange("cell", null, e); //$NON-NLS-1$
        }
      }

    });
    setDefaultRenderer(Object.class, cellRenderer);
    setColumnSelectionAllowed(true);
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setDefaultRenderer(indexRenderer);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    setGridColor(Color.BLACK);
    int width = 24;
    String name;
    TableColumn column;
    if(getColumnCount()>0) {
      // set width of column 0 (row index)
      name = getColumnName(0);
      column = getColumn(name);
      column.setMinWidth(width);
      column.setMaxWidth(2*width);
      column.setWidth(width);
    }
    ((java.util.Hashtable<Integer, DecimalFormat>) formatDictionary).clear(); // empty dictionary during initialization
    // set width of other columns
    width = 60;
    for(int i = 1, n = getColumnCount(); i<n; i++) {
      name = getColumnName(i);
      column = getColumn(name);
      column.setMinWidth(width);
      column.setMaxWidth(3*width);
      column.setWidth(width);
    }
    // Override the default tab behavior
    InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    final Action prevTabAction = getActionMap().get(im.get(tab));
    Action tabAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        // tab to the next editable cell
        prevTabAction.actionPerformed(e);
        JTable table = (JTable) e.getSource();
        int rowCount = table.getRowCount();
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        while(!table.isCellEditable(row, column)) {
          if(column==0) {
            column = 1;
          } else {
            row += 1;
          }
          if(row==rowCount) {
            row = 0;
          }
          if((row==table.getSelectedRow())&&(column==table.getSelectedColumn())) {
            break;
          }
        }
        table.changeSelection(row, column, false, false);
      }

    };
    getActionMap().put(im.get(tab), tabAction);
    // enter key starts editing
    Action enterAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        // start editing
        JTable table = (JTable) e.getSource();
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        table.editCellAt(row, column, e);
        Component comp = table.getEditorComponent();
        if(comp instanceof JTextField) {
          JTextField field = (JTextField) comp;
          field.requestFocus();
          field.selectAll();
        }
      }

    };
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    getActionMap().put(im.get(enter), enterAction);
  }

  /**
   * Starts editing the cell at <code>row</code> and <code>column</code>.
   * Overrides JTable method.
   *
   * @param  row the row to be edited
   * @param  column the column to be edited
   * @param  e ignored
   * @return false if the cell cannot be edited or the indices are invalid
   */
  public boolean editCellAt(int row, int column, EventObject e) {
    boolean editing = super.editCellAt(row, column, e);
    // save current value for comparison with value after editing
    if(editing) {
      prevValue = getValueAt(row, column);
    }
    return editing;
  }

  /*
  public void refreshTableOLD() {
    Runnable refresh = new Runnable() {
      public synchronized void run() {
        tableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
      }

    };
    SwingUtilities.invokeLater(refresh);
  }

*/

  /**
   *  Performs the action for the refresh timer by refreshing the data in the DataTable.
   *
   * @param  evt
   */
  public void actionPerformed(ActionEvent evt) {
    tableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
  }

  /**
   * Sets the <code>Timer</code>'s initial time delay (in milliseconds)
   * to wait after the timer is started
   * before firing the first event.
   * @param delay
   */
  public void setRefreshDelay(int delay) {
    refreshTimer.setInitialDelay(delay);
  }

  /**
   *  Refresh the data in the table.
   */
  public void refreshTable() {
    refreshTimer.start();
  }

  /**
   * Sets the default numeric display format for all columns
   * @param defaultFormat
   */
  public void setNumericFormat(String str) {
    if((str!=null)&&!str.equals(formatPattern)) { // format has changed
      formatPattern = str;
      defaultFormat = new DecimalFormat(str);
      refreshTable();
    }
  }

  /**
   * Sets the numeric display format for each column
   * @param defaultFormat
   */
  public void setNumericFormat(String[] str) {
    ((java.util.Hashtable<Integer, DecimalFormat>) formatDictionary).clear();
    for(int i = 0, n = str.length; i<n; i++) {
      formatDictionary.put(i, new DecimalFormat(str[i]));
    }
    refreshTable();
  }

  /**
   * Sets the first row's index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    if(index==tableModel.firstRow) {
      return; // nothing changed
    }
    tableModel.setFirstRowIndex(index);
    refreshTable();
  }

  /**
   * Sets the first column's index.
   *
   * @param index
   */
  public void setFirstColIndex(int index) {
    if(index==tableModel.firstCol) {
      return; // nothing changed
    }
    tableModel.setFirstColIndex(index);
    refreshTable();
  }

  /**
   * Sets the display row number flag. Table displays row number.
   *
   * @param vis <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    if(vis==tableModel.showRowNumber) {
      return; // nothing changed
    }
    tableModel.setRowNumberVisible(vis);
    refreshTable();
  }

  /**
   * Sets the editable property.
   *
   * @param editable true allows editing of the cell values that are not locked.
   */
  public void setEditable(boolean editable) {
    if(editable==tableModel.editable) {
      return; // nothing changed
    }
    tableModel.setEditable(editable);
    refreshTable();
  }

  /**
   * Returns true of the table's row and column values are interchanged.
   * @return
   */
  public boolean isTransposed() {
    return tableModel.isTransposed();
  }

  /**
   * Sets the transposed property for the array.
   * A transposed array switches its row and column values in the display.
   *
   * @param transposed
   */
  public void setTransposed(boolean transposed) {
    if(transposed==tableModel.transposed) {
      return; // nothing changed
    }
    tableModel.transposed = transposed;
    refreshTable();
  }

  /**
   * Sets columns names.
   * @param names
   */
  public void setColumnNames(String[] names) {
    if(tableModel.setColumnNames(names)) {
      refreshTable(); // refresh if there has been a change
    }
  }

  /**
   * Sets the column's locked flag.
   *
   * @param column   int
   * @param locked   boolean
   */
  public void setColumnLock(int columnIndex, boolean locked) {
    if(tableModel.setColumnLock(columnIndex, locked)) {
      refreshTable(); // refresh if there has been a change
    }
  }

  /**
   * Sets the lock flag for multiple columns.  Previously set locks are cleared.
   *
   * @param locked   boolean array
   */
  public void setColumnLocks(boolean[] locked) {
    if(tableModel.setColumnLocks(locked)) {
      refreshTable(); // refresh if there has been a change
    }
  }

  /**
   * Gets the default font of this component.
   * @return this component's font
   */
  public Font getFont() {
    if(indexRenderer==null) {
      indexRenderer = new ArrayIndexRenderer();
    }
    return indexRenderer.getFont();
  }

  
  /**
   * Sets the font for this component.
   *
   * @param font the desired <code>Font</code> for this component
   * @see java.awt.Component#getFont
   */
  public void setFont(Font font){ // Added by Paco
    super.setFont(font);
    if(indexRenderer==null) {
      indexRenderer = new ArrayIndexRenderer();
    }
    if(cellRenderer==null) {
      cellRenderer = new ArrayCellRenderer();
    }
    indexRenderer.setFont(font);
    cellRenderer.setFont(font);
  }
  
  /**
   * Sets the foreground color of this component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param color  the desired foreground <code>Color</code> 
   * @see java.awt.Component#getForeground
   */
  public void setForeground(Color color){
    super.setForeground(color);
    if(indexRenderer==null) {
      indexRenderer = new ArrayIndexRenderer();
    }
    indexRenderer.setForeground(color);
  }
  
  /**
   * Sets the foreground color of the cell rendering component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param color  the desired foreground <code>Color</code> 
   */
  public void setDataForeground(Color color){
    if(cellRenderer==null) {
      cellRenderer = new ArrayCellRenderer();
    }
    cellRenderer.setForeground(color);
  }

  /**
   * Sets the background color of this component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param color  the desired background <code>Color</code> 
   * @see java.awt.Component#getBackground
   */
  public void setBackground(Color color){
    super.setBackground(color);
    if(indexRenderer==null) {
      indexRenderer = new ArrayIndexRenderer();
    }
    indexRenderer.setBackground(color);
  }
  
  /**
   * Sets the background color of the data cell rendering component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param color  the desired background <code>Color</code> 
   */
  public void setDataBackground(Color color){
    if(cellRenderer==null) {
      cellRenderer = new ArrayCellRenderer();
    }
    cellRenderer.setBackground(color);
  }

  /**
   * Returns the renderer for a cell specified by row and column.
   *
   * @param row the row number
   * @param column the column number
   * @return the cell renderer
   */
  public TableCellRenderer getCellRenderer(int row, int column) {
    int i = convertColumnIndexToModel(column);
    if((i==0)&&tableModel.showRowNumber) {
      return indexRenderer;
    }
    return getDefaultRenderer(getColumnClass(column));
  }

  /**
   * A cell renderer for array cells.
   */
  static class ArrayCellRenderer extends DefaultTableCellRenderer {
    /**
     * Constructor
     */
    public ArrayCellRenderer() {
      super();
      setForeground(Color.BLACK);
      setHorizontalAlignment(SwingConstants.RIGHT);
      setBackground(Color.WHITE);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      boolean editable = table.isCellEditable(row, column);
      setEnabled(editable);
      ArrayTable arrayTable = (ArrayTable) table;
      DecimalFormat cellFormat = arrayTable.formatDictionary.get(column); // does column have a special format?
      if(cellFormat==null) {
        cellFormat = arrayTable.defaultFormat; // use default format
      }
      if(value==null) {
        setText("");                           //$NON-NLS-1$
      } else if(cellFormat==null) {            // default format not set
        setText(value.toString());
      } else {
        try {
          setText(cellFormat.format(value));
        } catch(IllegalArgumentException ex) { // convert to string if value cannot be formatted
          setText(value.toString());
        }
      }
      setBorder(new CellBorder(new Color(224, 224, 224)));
      return this;
    }

  }

  /**
   * A cell renderer for array indices.
   */
  static class ArrayIndexRenderer extends JLabel implements TableCellRenderer {

    /**
     * Constructor
     */
    public ArrayIndexRenderer() {
      super();
      setBorder(BorderFactory.createEtchedBorder());
      setOpaque(true);                                       // make background visible
      setForeground(Color.BLACK);
      setBackground(UIManager.getColor("Panel.background")); //$NON-NLS-1$
    }

    /**
     * Returns a label for the specified cell.
     *
     * @param table ignored
     * @param value the row number to be displayed
     * @param isSelected ignored
     * @param hasFocus ignored
     * @param row ignored
     * @param column the column number
     * @return a label with the row number
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if(column==0) {
        setHorizontalAlignment(SwingConstants.RIGHT);
      } else {
        setHorizontalAlignment(SwingConstants.CENTER);
      }
      if(value==null) {
        setText(""); //$NON-NLS-1$
      } else {
        setText(value.toString());
      }
      setPreferredSize(new Dimension(20, 18));
      return this;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
