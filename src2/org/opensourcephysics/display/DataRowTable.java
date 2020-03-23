/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A JTable to display rows of integers, doubles and Strings.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class DataRowTable extends JTable implements ActionListener {
  static final Color PANEL_BACKGROUND = javax.swing.UIManager.getColor("Panel.background"); //$NON-NLS-1$
  static final Color LIGHT_BLUE = new Color(204, 204, 255);
  protected int labelColumnWidth = 40;
  DataRowModel rowModel = new DataRowModel();
  RowNumberRenderer indexRenderer = new RowNumberRenderer();
  CellRenderer cellRenderer = new CellRenderer();
  String formatPattern = "0.000";                                                           //$NON-NLS-1$
  DecimalFormat defaultFormat = new DecimalFormat(formatPattern);
  int refreshDelay = 0;                                                                     // time in ms to delay refresh events
  javax.swing.Timer refreshTimer = new javax.swing.Timer(refreshDelay, this);               // delay for refreshTable
  java.util.Dictionary<Integer, DecimalFormat> formats = new java.util.Hashtable<Integer, DecimalFormat>();

  /**
   * Constructor DataRowTable
   */
  public DataRowTable() {
    init();
  }

  /**
   * Initializes the table.
   */
  protected void init() {
    refreshTimer.setRepeats(false);
    refreshTimer.setCoalesce(true);
    setModel(rowModel);
    setColumnSelectionAllowed(true);
    setGridColor(Color.blue);
    setSelectionBackground(LIGHT_BLUE);
    setSelectionForeground(Color.red);                         // foreground color for selected cells
    setColumnModel(new DataTableColumnModel());
    setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    setColumnSelectionAllowed(true);
    rowModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        // forward the table model event to property change listeners
        DataRowTable.this.firePropertyChange("cell", null, e); //$NON-NLS-1$
      }

    });
    setDefaultRenderer(Object.class, cellRenderer);
    getTableHeader().setForeground(Color.blue); // set text color
    getTableHeader().setReorderingAllowed(true);
    getTableHeader().setDefaultRenderer(new HeaderRenderer());
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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
    // set width of other columns
    width = 60;
    for(int i = 1, n = getColumnCount(); i<n; i++) {
      name = getColumnName(i);
      column = getColumn(name);
      column.setMinWidth(width);
      column.setMaxWidth(3*width);
      column.setWidth(width);
    }
  }

  /**
   * Sets the <code>Timer</code>'s initial time delay (in milliseconds)
   * to wait after the timer is started
   * before firing the first event.
   * @param delay
   */
  public void setRefreshDelay(int delay) {
    if(delay>0) {
      refreshTimer.setDelay(delay);
      refreshTimer.setInitialDelay(delay);
    } else if(delay<=0) {
      refreshTimer.stop();
    }
    refreshDelay = delay;
  }

  public void clearFormats() {
	formats = new java.util.Hashtable<Integer, DecimalFormat>();
  }
  
  /**
   * Sets the default numeric display format pattern.
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
   *  Sets the column decimal format.
   *
   * @param  column  the column index
   * @param  format  the format
   */
  public void setColumnFormat(int column, String format) {
    DecimalFormat f = new DecimalFormat(format);
    DecimalFormat val = formats.get(column);
    if((val!=null)&&val.equals(f)) {
      return; // nothing changed
    }
    formats.put(column, f);
    refreshTable();
  }

  /**
   * Clears data from this table.  Column names and format patterns are not affected.
   */
  public synchronized void clearData() {
    rowModel.rowList.clear();
    rowModel.colCount = 0;
    refreshTable();
  }

  /**
   * Clears data, column names, and format patterns.
   */
  public synchronized void clear() {
    rowModel.rowList.clear();
    rowModel.colNames.clear();
    rowModel.colCount = 0;
    formats = new java.util.Hashtable<Integer, DecimalFormat>();
    refreshTable();
  }

  /**
   *  Sets the stride between rows.
   *
   * @param  tableModel
   * @param  stride
   */
  public void setStride(int stride) {
    stride = Math.max(1, stride);
    if(rowModel.stride==stride) {
      return; // nothing changed
    }
    rowModel.setStride(stride);
    refreshTable();
  }

  /**
   *  Refresh the data in the DataTable, as well as other changes to the table,
   *  such as row number visibility. Changes to the TableModels displayed in the
   *  table will not be visible until this method is called.
   */
  public void refreshTable() {
    if(refreshDelay>0) {
      refreshTimer.start();
    } else {
      Runnable doRefreshTable = new Runnable() {
        public synchronized void run() {
          tableChanged(new TableModelEvent(getModel(), TableModelEvent.HEADER_ROW));
        }

      };
      if(SwingUtilities.isEventDispatchThread()) {
        doRefreshTable.run();
      } else {
        SwingUtilities.invokeLater(doRefreshTable);
      }
    }
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
    if((i==0)&&rowModel.rowNumberVisible) {
      return indexRenderer;
    }
    return cellRenderer;
    //return getDefaultRenderer(getColumnClass(column));
  }

  /**
   *  Performs the action for the refresh timer by refreshing the data in the DataTable.
   *
   * @param  evt
   */
  public void actionPerformed(ActionEvent evt) {
    tableChanged(new TableModelEvent(getModel(), TableModelEvent.HEADER_ROW));
  }

  private class CellRenderer extends DefaultTableCellRenderer {
    /**
     * PrecisionRenderer constructor
     *
     */
    public CellRenderer() {
      super();
      setHorizontalAlignment(SwingConstants.RIGHT);
      setBorder(new CellBorder(new Color(224, 224, 224)));
      setBackground(Color.WHITE);
    }

    /**
     *  returns a JLabel that is highlighted if the row is selected.
     *
     * @param  table
     * @param  value
     * @param  isSelected
     * @param  hasFocus
     * @param  row
     * @param  column
     * @return
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if(!rowModel.rowNumberVisible) {
        column++;
      }
      DecimalFormat f = formats.get(column);
      if(f==null) {
        f = defaultFormat;
      }
      if(value==null) {
        setText(""); //$NON-NLS-1$
      } else if(value instanceof String) {
        setText((String) value);
      } else if(f==null) {
        setText(value.toString());
      } else {
        try {
          setText(f.format(value));
        } catch(IllegalArgumentException ex) {
          setText(value.toString());
        }
      }
      return this;
    }

  }

  private class HeaderRenderer extends RowNumberRenderer {
    /**
     * Constructor HeaderRenderer
     */
    public HeaderRenderer() {
      super();
      setHorizontalAlignment(SwingConstants.CENTER);
      setForeground(Color.BLUE);
      setBorder(javax.swing.BorderFactory.createLineBorder(new Color(224, 224, 224)));
    }

  }

  private static class RowNumberRenderer extends JLabel implements TableCellRenderer {
    /**
     *  RowNumberRenderer constructor
     *
     */
    public RowNumberRenderer() {
      super();
      setHorizontalAlignment(SwingConstants.RIGHT);
      setOpaque(true); // make background visible.
      setForeground(Color.BLACK);
      setBackground(PANEL_BACKGROUND);
      setBorder(new CellBorder(new Color(224, 224, 224)));
    }

    /**
     *  returns a JLabel that is highlighted if the row is selected.
     *
     * @param  table
     * @param  value
     * @param  isSelected
     * @param  hasFocus
     * @param  row
     * @param  column
     * @return
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if(table.isRowSelected(row)) {
        int[] i = table.getSelectedColumns();
        if((i.length==1)&&(table.convertColumnIndexToModel(i[0])==0)) {
          setBackground(PANEL_BACKGROUND);
        } else {
          setBackground(Color.gray);
        }
      } else {
        setBackground(PANEL_BACKGROUND);
      }
      if(value==null){ // added by W. Christian
    	  setText("???");  //$NON-NLS-1$
      }else{
    	  setText(value.toString());
      }
      return this;
    }

  }

  private class DataTableColumnModel extends DefaultTableColumnModel {
    /**
     *  Method getColumn
     *
     * @param  columnIndex
     * @return
     */
    public TableColumn getColumn(int columnIndex) {
      TableColumn tableColumn;
      try {
        tableColumn = super.getColumn(columnIndex);
      } catch(Exception ex) { // return an empty column if the columnIndex is not valid.
        return new TableColumn();
      }
      String headerValue = (String) tableColumn.getHeaderValue();
      if(headerValue==null) {
        return tableColumn;
      } else if(headerValue.equals("row")) { //$NON-NLS-1$
        tableColumn.setMaxWidth(labelColumnWidth);
        tableColumn.setMinWidth(labelColumnWidth);
        tableColumn.setResizable(true);
      }
      return tableColumn;
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
