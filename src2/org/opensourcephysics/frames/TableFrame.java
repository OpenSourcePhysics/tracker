/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import org.opensourcephysics.display.DataPanel;
import org.opensourcephysics.display.OSPFrame;

public class TableFrame extends OSPFrame {
  DataPanel tableData = new DataPanel();

  /**
   * Constructs a TableFrame with the given title.
   * @param frameTitle String
   */
  public TableFrame(String frameTitle) {
    super(frameTitle);
    setAnimated(true);
    setAutoclear(true);
    setContentPane(tableData);
    setRowNumberVisible(true);
    setSize(400, 500);
  }

  /**
   *  Sets the delay time for table refresh timer.
   *
   * @param  delay  the delay in millisecond
   */
  public void setRefreshDelay(int delay) {
    tableData.setRefreshDelay(delay);
  }

  /**
   *  Sets the stride between rows.
   *
   * @param  tableModel
   * @param  stride
   */
  public void setStride(int stride) {
    tableData.setStride(stride);
  }

  /**
   *  Sets the display row number flag. Table displays row number.
   *
   * @param  vis  <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    tableData.setRowNumberVisible(vis);
  }

  /**
   * Sets the column names in this JTable.
   *
   * @param names
   */
  public void setColumnNames(String[] names) {
    tableData.setColumnNames(names);
  }

  /**
   * Appends a two dimensional array to this table.
   *
   * @param obj Object
   * @throws IllegalArgumentException
   */
  public synchronized void appendArray(Object obj) {
    tableData.appendArray(obj);
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(double[] x) {
    tableData.appendRow(x);
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(int[] x) {
    tableData.appendRow(x);
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(Object[] x) {
    tableData.appendRow(x);
  }

  /**
   * Appends a row of data with the given values to the table.
   * @param x double[]
   */
  public synchronized void appendRow(byte[] x) {
    tableData.appendRow(x);
  }

  /**
   *  Sets the column names in a JTable.
   *
   * @param  column  the index
   * @param  name
   */
  public void setColumnNames(int column, String name) {
    tableData.setColumnNames(column, name);
  }

  /**
   *  Sets the format for displaying decimals.
   *
   * @param  column  the index
   * @param  format
   */
  public void setColumnFormat(int column, String format) {
    tableData.setColumnFormat(column, format);
  }

  /**
   *  Sets the maximum number of points to display
   *
   * @param  max
   */
  public void setMaxPoints(int max) {
    tableData.setMaxPoints(max);
  }

  /**
   * Sets the first row's index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    tableData.setFirstRowIndex(index);
  }

  /**
   * Shows or hides this TableFrame depending on the value of parameter
   * <code>vis</code>.
   * @param vis  if <code>true</code>, shows this component;
   * otherwise, hides this component
   */
  public void setVisible(boolean vis) {
    boolean wasVisible = super.isVisible();
    super.setVisible(vis);
    if(vis&&!wasVisible) {      // refresh if the table was NOT visible and is now visible.
      tableData.refreshTable(); // make sure the table shows the current values
    }
  }

  /**
   * Refresh the data in the table.
   */
  public void refreshTable() {
    tableData.refreshTable();
  }

  /**
   * Clears data from drawing objects within this frame.
   *
   * The default method does nothing.
   * Override this method to select the object(s) and the data to be cleared.
   */
  public synchronized void clearData() {
    tableData.clearData();
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
