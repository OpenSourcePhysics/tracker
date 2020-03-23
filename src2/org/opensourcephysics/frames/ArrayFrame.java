/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import org.opensourcephysics.display.ArrayPanel;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPFrame;

/**
 * A frame that displays arrays.
 *
 * @author Wolfgang Christian
 *
 */
public class ArrayFrame extends OSPFrame {
  ArrayPanel arrayPanel;

  /**
   * Constructs the ArrayFrame for the given array.
   * @param arrayObj
   */
  public ArrayFrame(Object arrayObj) {
    this(arrayObj, DisplayRes.getString("ArrayFrame.Title")); //$NON-NLS-1$
  }

  /**
   * Constructs the ArrayFrame for the given array and frame title.
   * @param arrayObj
   * @param title
   */
  public ArrayFrame(Object arrayObj, String title) {
    super(title);
    arrayPanel = ArrayPanel.getArrayPanel(arrayObj);
    setContentPane(arrayPanel);
    pack();
  }

  /**
   * Sets the first row's starting index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    arrayPanel.setFirstRowIndex(index);
  }

  /**
   * Sets the first column's starting index.
   *
   * @param index
   */
  public void setFirstColIndex(int index) {
    arrayPanel.setFirstColIndex(index);
  }

  /**
   * Sets the editable property for the entire panel.
   *
   * @param editable true to allow editing of the cell values
   */
  public void setEditable(boolean editable) {
    arrayPanel.setEditable(editable);
  }

  /**
   * Sets the transposed property for the array.
   * A transposed array switches its row and column values in the display.
   *
   * @param transposed
   */
  public void setTransposed(boolean transposed) {
    arrayPanel.setTransposed(transposed);
  }

  /**
   * Sets the lock flag for a single column.
   *
   * @param column   int
   * @param locked   boolean
   */
  public void setColumnLock(int columnIndex, boolean locked) {
    arrayPanel.setColumnLock(columnIndex, locked);
  }

  /**
   * Sets the lock flag for multiple columns.  Previously set locks are cleared.
   *
   * @param locked   boolean array
   */
  public void setColumnLocks(boolean[] locked) {
    arrayPanel.setColumnLocks(locked);
  }

  /**
   * Sets the column names in a JTable.
   *
   * @param names
   */
  public void setColumnNames(String[] names) {
    arrayPanel.setColumnNames(names);
  }

  /**
   * Sets the column names in each table model separately.
   *
   * @param names
   */
  public void setColumnNames(String[][] names) {
    arrayPanel.setColumnNames(names);
  }

  /**
   * Sets the default numeric display format for the table.
   *
   * @param format
   */
  public void setNumericFormat(String format) {
    arrayPanel.setNumericFormat(format);
  }

  /**
   * Sets the display row number flag. Table displays row number.
   *
   * @param vis <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    arrayPanel.setRowNumberVisible(vis);
  }

  /**
   * Sets the format for displaying decimals.
   *
   * @param column  the index
   * @param format
   */
  public void setColumnFormat(int column, String format) {
    // arrayPanel.setColumnFormat(column, format);
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
