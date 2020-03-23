/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class GridDataTable extends JTable implements ActionListener {
  static final Color PANEL_BACKGROUND = javax.swing.UIManager.getColor("Panel.background"); //$NON-NLS-1$
  int refreshDelay = 0;                                                                     // time in ms to delay refresh events
  javax.swing.Timer refreshTimer = new javax.swing.Timer(refreshDelay, this);               // delay for refreshTable
  GridTableModel tableModel;
  RowNumberRenderer rowNumberRenderer = new RowNumberRenderer();

  /**
   * Constructor GridDataTable
   * @param griddata
   * @param component
   */
  public GridDataTable(GridData griddata, int component) {
    super();
    refreshTimer.setRepeats(false);
    refreshTimer.setCoalesce(true);
    tableModel = new GridTableModel(griddata, component);
    setModel(tableModel);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    String name = getColumnName(0);
    TableColumn column = getColumn(name);
    int width = 20;
    column.setMinWidth(width);
    column.setResizable(true);
    // column.setMaxWidth(width);
    column.setWidth(width);
    width = 60;
    for(int i = 1, n = getColumnCount(); i<n; i++) {
      name = getColumnName(i);
      column = getColumn(name);
      column.setMinWidth(width);
      column.setWidth(width);
      column.setResizable(true);
    }
    sizeColumnsToFit(0);
  }

  /**
   *  Performs the action for the refresh timer by refreshing the data in the DataTable.
   *
   * @param  evt
   */
  public void actionPerformed(ActionEvent evt) {
    tableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
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
          tableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
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
   *  Returns an appropriate renderer for the cell specified by this row and
   *  column. If the <code>TableColumn</code> for this column has a non-null
   *  renderer, returns that. If the <code>TableColumn</code> for this column has
   *  the same name as a name specified in the setMaximumFractionDigits method,
   *  returns the appropriate renderer. If not, finds the class of the data in
   *  this column (using <code>getColumnClass</code>) and returns the default
   *  renderer for this type of data.
   *
   * @param  row     Description of Parameter
   * @param  column  Description of Parameter
   * @return         The cellRenderer value
   */
  public TableCellRenderer getCellRenderer(int row, int column) {
    int i = convertColumnIndexToModel(column);
    if(i==0) {
      return rowNumberRenderer;
    }
    return getDefaultRenderer(getColumnClass(column));
  }

  private static class RowNumberRenderer extends JLabel implements TableCellRenderer {
    //JTable table;

    /**
     *  RowNumberRenderer constructor
     *
     * @param  _table  Description of Parameter
     */
    public RowNumberRenderer() {
      super();
      //table = _table;
      setHorizontalAlignment(SwingConstants.RIGHT);
      setOpaque(true); // make background visible.
      setForeground(Color.BLACK);
      setBackground(PANEL_BACKGROUND);
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
      if(column==0) {
        setBackground(PANEL_BACKGROUND);
      }
      setText(value.toString());
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
