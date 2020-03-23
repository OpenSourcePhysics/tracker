/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DataPanel extends JPanel {
  DataRowTable table = new DataRowTable();
  JScrollPane scrollPane = new JScrollPane(table);

  /**
   * Constructor DataRowPanel
   */
  public DataPanel() {
    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
  }
  
  
  /**
   * Sets the font for this component.
   *
   * @param font the desired <code>Font</code> for this component
   * @see java.awt.Component#getFont
   */
  public void setFont(Font font){
	  super.setFont(font);
	  if(table!=null)table.setFont(font);
  }
  
  /**
   * Sets the foreground color of this component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param fg  the desired foreground <code>Color</code> 
   * @see java.awt.Component#getForeground
   */
  public void setForeground(Color color){
	  super.setForeground(color);
	  if(table!=null)table.setForeground(color);
  }

  /**
   * Refresh the data in the tables.
   */
  public void refreshTable() {
    table.refreshTable();
  }

  /**
   * Gets the Table.  Used by EJS to access the table.
   * @return
   */
  public java.awt.Component getVisual() {
    return table;
  }

  /**
   *  Sets the given column name in this table.
   *
   * @param  column  the index
   * @param  name
   */
  public void setColumnNames(int column, String name) {
    if(table.rowModel.setColumnNames(column, name)) { // refresh if the table changed
      refreshTable();
    }
  }

  /**
   * Sets all column names in this table.
   *
   * @param names
   */
  public void setColumnNames(String[] names) {
    boolean changed = false;
    for(int i = 0, n = names.length; i<n; i++) {
      if(table.rowModel.setColumnNames(i, names[i])) {
        changed = true;
      }
    }
    if(changed) {
      refreshTable();
    }
  }

  /**
   *  Sets the display row number flag. Table displays row number.
   *
   * @param  vis  <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    if(table.rowModel.setRowNumberVisible(vis)) { // refresh if the table changed
      refreshTable();
    }
  }

  /**
   * Sets the first row's index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    if(table.rowModel.firstRowIndex!=index) { // refresh if the table changed
      table.rowModel.firstRowIndex = index;
      refreshTable();
    }
  }

  /**
   *  Sets the delay time for table refresh timer.
   *
   * @param  delay  the delay in millisecond
   */
  public void setRefreshDelay(int delay) {
    table.setRefreshDelay(delay);
  }

  /**
   * Appends a two dimensional array to this table.
   *
   * @param obj Object
   * @throws IllegalArgumentException
   */
  public synchronized void appendArray(Object obj) throws IllegalArgumentException {
    if(!obj.getClass().isArray()) {
      throw new IllegalArgumentException(""); //$NON-NLS-1$
    }
    // make sure ultimate component class is acceptable
    Class<?> componentType = obj.getClass().getComponentType();
    while(componentType.isArray()) {
      componentType = componentType.getComponentType();
    }
    String type = componentType.getName();
    if(type.equals("double")) {      //$NON-NLS-1$
      double[][] array = (double[][]) obj;
      double[] row = new double[array.length];
      for(int i = 0, n = array[0].length; i<n; i++) {
        for(int j = 0, m = row.length; j<m; j++) {
          row[j] = array[j][i];
        }
        appendRow(row);
      }
    } else if(type.equals("int")) {  //$NON-NLS-1$
      int[][] array = (int[][]) obj;
      int[] row = new int[array.length];
      for(int i = 0, n = array[0].length; i<n; i++) {
        for(int j = 0, m = row.length; j<m; j++) {
          row[j] = array[j][i];
        }
        appendRow(row);
      }
    } else if(type.equals("byte")) { //$NON-NLS-1$
      byte[][] array = (byte[][]) obj;
      byte[] row = new byte[array.length];
      for(int i = 0, n = array[0].length; i<n; i++) {
        for(int j = 0, m = row.length; j<m; j++) {
          row[j] = array[j][i];
        }
        appendRow(row);
      }
    } else {
      Object[][] array = (Object[][]) obj;
      Object[] row = new Object[array.length];
      for(int i = 0, n = array[0].length; i<n; i++) {
        for(int j = 0, m = row.length; j<m; j++) {
          row[j] = array[j][i];
        }
        appendRow(row);
      }
    }
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(double[] x) {
    table.rowModel.appendDoubles(x);
    if(isShowing()) {
      table.refreshTable();
    }
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(int[] x) {
    table.rowModel.appendInts(x);
    if(isShowing()) {
      table.refreshTable();
    }
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(Object[] x) {
    table.rowModel.appendRow(x);
    if(isShowing()) {
      table.refreshTable();
    }
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(byte[] x) {
    table.rowModel.appendBytes(x);
    if(isShowing()) {
      table.refreshTable();
    }
  }

  /**
   * True if row number numbers are visible.
   * @return
   */
  public boolean isRowNumberVisible() {
    return table.rowModel.rowNumberVisible;
  }

  /**
   * Gets the number of columns currently shown.  The row number column is included in the counting if it is visible.
   *
   * @return the column count
   */
  public int getColumnCount() {
    return table.rowModel.getColumnCount();
  }

  /**
   * Gets the number of rows currently being shown.
   *
   * @return the row count
   */
  public int getRowCount() {
    return table.rowModel.getRowCount();
  }

  /**
   * Gets the total number of rows in the table.
   *
   * @return the row count
   */
  public int getTotalRowCount() {
    return table.rowModel.rowList.size();
  }

  /**
   * Gets the number of rows shown.
   *
   * @return the stride
   */
  public int getStride() {
    return table.rowModel.stride;
  }

  /**
   *  Sets the format for displaying decimals.
   *
   * @param  column  the index
   * @param  format
   */
  public void setColumnFormat(int column, String format) {
    table.setColumnFormat(column, format);
  }

  /**
   * Clears any previous format
   */
  public void clearFormats() {
	table.clearFormats();
  }
  
  /**
   *  Sets the default format pattern for displaying decimals.
   *
   * @param  pattern
   */
  public void setNumericFormat(String pattern) {
    table.setNumericFormat(pattern);
  }

  /**
   *  Sets the maximum number of points to display
   *
   * @param  max
   */
  public void setMaxPoints(int max) {
    table.rowModel.setMaxPoints(max);
  }

  /**
   * Shows or hides this TableFrame depending on the value of parameter
   * <code>vis</code>.
   * @param vis  if <code>true</code>, shows this component;
   * otherwise, hides this component
   */
  public void setVisible(boolean vis) {
    if(vis) {
      table.refreshTable(); // make sure the table shows the current values
    }
    super.setVisible(vis);
  }

  /**
   *  Sets the stride between successive rows.
   *
   * @param  tableModel
   * @param  stride
   */
  public void setStride(int stride) {
    table.setStride(stride);
  }

  /**
   * Clears data from this table.  Column names and format patterns are not affected.
   */
  public void clearData() {
    table.clearData();
  }

  /**
   * Clears data, column names and format patterns.
   */
  public void clear() {
    table.clear();
  }
  
  /**
   * Sets the table's auto resize mode when the table is resized.
   *
   * @param   mode One of 5 legal values:
   *                   AUTO_RESIZE_OFF,
   *                   AUTO_RESIZE_NEXT_COLUMN,
   *                   AUTO_RESIZE_SUBSEQUENT_COLUMNS,
   *                   AUTO_RESIZE_LAST_COLUMN,
   *                   AUTO_RESIZE_ALL_COLUMNS
   */
  public void setAutoResizeMode(int mode) {
    table.setAutoResizeMode(mode); // make sure the table shows the current values
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
