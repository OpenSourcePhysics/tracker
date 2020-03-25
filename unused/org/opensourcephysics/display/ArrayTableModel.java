/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import javax.swing.table.AbstractTableModel;

/**
 * A table model for a 1D and 2D ArrayTable.
 *
 * Column names, column locks, and transposed property added in version 1.1.
 *
 * @author Douglas Brown
 * @author Wolfgang Christian
 * @version 1.1
 */
public class ArrayTableModel extends AbstractTableModel {
  double[] doubleArray1;
  double[][] doubleArray2;
  int[] intArray1;
  int[][] intArray2;
  String[] stringArray1;
  String[][] stringArray2;
  boolean[] booleanArray1;
  boolean[][] booleanArray2;
  boolean editable = false;
  String[] columnNames;
  boolean showRowNumber = true;
  int firstRow = 0, firstCol = 0;
  boolean transposed = false;
  java.util.Dictionary<Integer, Boolean> lockedColumns = new java.util.Hashtable<Integer, Boolean>();

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(int[] array) {
    intArray1 = array;
  }

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(int[][] array) {
    intArray2 = array;
  }

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(double[] array) {
    doubleArray1 = array;
  }

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(double[][] array) {
    doubleArray2 = array;
  }

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(String[] array) {
    stringArray1 = array;
  }

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(String[][] array) {
    stringArray2 = array;
  }

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(boolean[] array) {
    booleanArray1 = array;
  }

  /**
   * Constructor ArrayTableModel
   * @param array
   */
  public ArrayTableModel(boolean[][] array) {
    booleanArray2 = array;
  }

  /**
   * Allows changing the array with minimal changes
   * @param arrayObj
   */
  public void setArray(Object arrayObj) {
    if(arrayObj instanceof double[]) {
      doubleArray1 = (double[]) arrayObj;
    } else if(arrayObj instanceof double[][]) {
      doubleArray2 = (double[][]) arrayObj;
    } else if(arrayObj instanceof int[]) {
      intArray1 = (int[]) arrayObj;
    } else if(arrayObj instanceof int[][]) {
      intArray2 = (int[][]) arrayObj;
    } else if(arrayObj instanceof String[]) {
      stringArray1 = (String[]) arrayObj;
    } else if(arrayObj instanceof String[][]) {
      stringArray2 = (String[][]) arrayObj;
    } else if(arrayObj instanceof boolean[]) {
      booleanArray1 = (boolean[]) arrayObj;
    } else if(arrayObj instanceof boolean[][]) {
      booleanArray2 = (boolean[][]) arrayObj;
    }
  }

  /**
   * Sets the column's lock flag.  Returns true if the column's lock changes.
   *
   * @param column   int
   * @param locked   boolean
   * @return true if change occurred
   */
  public boolean setColumnLock(int columnIndex, boolean locked) {
    Boolean val = lockedColumns.get(columnIndex);
    if((val!=null)&&(locked==val)) {
      return false; // nothing changed
    }
    lockedColumns.put(columnIndex, locked);
    return true;
  }

  /**
   * Sets the lock flag for multiple columns.  Previously set locks are cleared.
   *
   * @param locked   boolean array
   * @return true if change occurred
   */
  public boolean setColumnLocks(boolean[] locked) {
    boolean change = false;
    if(lockedColumns.size()!=locked.length) {
      change = true;
    } else { // lengths are correct so check values
      for(int i = 0, n = locked.length; i<n; i++) {
        Boolean val = lockedColumns.get(i);
        if(!Boolean.valueOf(locked[i]).equals(val)) {
          change = true;
          break;
        }
      }
    }
    if(!change) {
      return false;
    }
    // locked columns have changed; clear old values and add new values
    ((java.util.Hashtable<Integer, Boolean>) lockedColumns).clear();
    for(int i = 0, n = locked.length; i<n; i++) {
      lockedColumns.put(i, locked[i]);
    }
    return true;
  }

  /**
   * Sets the first row's index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    firstRow = index;
  }

  /**
   * Sets the first column's index.
   *
   * @param index
   */
  public void setFirstColIndex(int index) {
    firstRow = index;
  }

  /**
   * Sets the display row number flag. Table displays row number.
   *
   * @param vis <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    showRowNumber = vis;
  }

  /**
   * Sets the transposed property for the array.
   * A transposed array switches its row and column values in the display.
   *
   * @param transposed
   */
  public void setTransposed(boolean transposed) {
    this.transposed = transposed;
  }

  /**
   * Returns true of the table's row and column values are interchanged.
   * @return
   */
  public boolean isTransposed() {
    return transposed;
  }

  /**
   * Sets the editable property.
   *
   * @param editable true allows editing of the cell values that are not locked.
   */
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  /**
   * Gets the number of columns.
   *
   * @return the column count
   */
  public int getColumnCount() {
    if(getArrayRowCount()==0) {
      return 0; // don't show columns if there aren't any rows
    }
    int offset = showRowNumber // row numbers add a column
                 ? 1 : 0;
    if(transposed) {
      return getArrayRowCount()+offset;
    }
    return getArrayColumnCount()+offset;
  }

  /**
   * Gets the number of columns in the matrix.
   * The second index is the column index in 2D arrays.
   *
   * @return the column count
   */
  int getArrayColumnCount() {
    if((intArray1!=null)||(doubleArray1!=null)||(stringArray1!=null)||(booleanArray1!=null)) {
      return 1;
    }
    if(intArray2!=null) {
      return intArray2[0].length;
    }
    if(doubleArray2!=null) {
      return doubleArray2[0].length;
    }
    if(stringArray2!=null) {
      return stringArray2[0].length;
    }
    if(booleanArray2!=null) {
      return booleanArray2[0].length;
    }
    return 0;
  }

  /**
   * Sets columns names.  Returns true if the table has changed.
   * @param names
   * @return changed
   */
  public boolean setColumnNames(String[] names) {
    if((names==null)&&(columnNames==null)) {
      return false; // neither array exists but there is no change
    }
    if(names!=null) {
      names = names.clone(); // clone the array as the TeX parser may change string values
    }
    boolean changed = false;
    if((names!=null)&&(columnNames!=null)&&(columnNames.length==names.length)) { // arrays exist and have same length
      for(int i = 0, n = names.length; i<n; i++) {
        names[i] = TeXParser.parseTeX(names[i]);                                 // convert TeX within each string
        if(!names[i].equals(columnNames[i])) {                                   // check each name for a change
          changed = true;                                                        // the i-th column name has changed
        }
      }
    } else {                                                                     // arrays have different lengths
      changed = true;
      if(names!=null) {
        for(int i = 0, n = names.length; i<n; i++) {
          names[i] = TeXParser.parseTeX(names[i]);                               // convert TeX within each string
        }
      }
    }
    columnNames = names;
    return changed;
  }

  /**
   * Gets the name of the specified column.
   *
   * @param column the column index
   * @return the column name
   */
  public String getColumnName(int column) {
    if(!showRowNumber) {
      column++;
    }
    if((columnNames!=null)&&(column<columnNames.length)) {
      return columnNames[column];
    }
    if(column==0) {
      return ""; //$NON-NLS-1$
    }
    if((intArray1!=null)||(doubleArray1!=null)||(stringArray1!=null)||(booleanArray1!=null)) {
      return "value"; //$NON-NLS-1$
    }
    return ""+(column-1+firstCol); //$NON-NLS-1$
  }

  /**
   * Gets the number of rows.
   *
   * @return the row count
   */
  public int getRowCount() {
    if(transposed) {
      return getArrayColumnCount();
    }
    return getArrayRowCount();
  }

  /**
   * Gets the number of rows in the array.
   * The first index is the row index.
   *
   * @return the row count
   */
  int getArrayRowCount() {
    if(intArray1!=null) {
      return intArray1.length;
    }
    if(intArray2!=null) {
      return intArray2.length;
    }
    if(doubleArray1!=null) {
      return doubleArray1.length;
    }
    if(doubleArray2!=null) {
      return doubleArray2.length;
    }
    if(stringArray1!=null) {
      return stringArray1.length;
    }
    if(stringArray2!=null) {
      return stringArray2.length;
    }
    if(booleanArray1!=null) {
      return booleanArray1.length;
    }
    if(booleanArray2!=null) {
      return booleanArray2.length;
    }
    return 0;
  }

  /**
   * Gets the value at the given cell.
   *
   * @param row the row index
   * @param column the column index
   * @return the value
   */
  public Object getValueAt(int row, int column) {
    if(showRowNumber&&(column==0)) {
      return new Integer(row+firstRow);
    }
    int offset = showRowNumber ? 1 : 0;
    if(transposed) {
      int temp = row;
      row = column-offset;
      column = temp;
    }
    if(intArray1!=null) {
      return new Integer(intArray1[row]);
    }
    if(intArray2!=null) {
      int col = transposed ? column : column-offset;
      if((row>intArray2.length-1)||(col>intArray2[row].length-1)||(col<0)) {
        return null;
      }
      return new Integer(intArray2[row][col]);
    }
    if(doubleArray1!=null) {
      return new Double(doubleArray1[row]);
    }
    if(doubleArray2!=null) {
      int col = transposed ? column : column-offset;
      if((row>doubleArray2.length-1)||(col>doubleArray2[row].length-1)||(col<0)) {
        return null;
      }
      return new Double(doubleArray2[row][col]);
    }
    if(stringArray1!=null) {
      return stringArray1[row];
    }
    if(stringArray2!=null) {
      int col = transposed ? column : column-offset;
      if((row>stringArray2.length-1)||(col>stringArray2[row].length-1)||(col<0)) {
        return null;
      }
      return stringArray2[row][col];
    }
    if(booleanArray1!=null) {
      return new Boolean(booleanArray1[row]);
    }
    if(booleanArray2!=null) {
      int col = transposed ? column : column-offset;
      if((row>booleanArray2.length-1)||(col>booleanArray2[row].length-1)||(col<0)) {
        return null;
      }
      return new Boolean(booleanArray2[row][col]);
    }
    return null;
  }

  /**
   * Sets the value at the given cell.
   *
   * @param value the value
   * @param row the row index
   * @param col the column index
   */
  public void setValueAt(Object value, int row, int col) {
    int offset = showRowNumber ? 1 : 0;  // added by WC
    if(transposed) {
      int temp = row;
      row = col-offset;
      col = temp;
    }
    try {
      if(value instanceof String) {
        String val = (String) value;
        col = transposed ? col : col-offset;  // added by WC
        if(intArray1!=null) {
          intArray1[row] = Integer.parseInt(val);
        } else if(intArray2!=null) {
          intArray2[row][col] = Integer.parseInt(val);
        } else if(doubleArray1!=null) {
          doubleArray1[row] = Double.parseDouble(val);
        } else if(doubleArray2!=null) {
          doubleArray2[row][col] = Double.parseDouble(val);
        } else if(stringArray1!=null) {
          stringArray1[row] = val;
        } else if(stringArray2!=null) {
          stringArray2[row][col] = val;
        } else if(booleanArray1!=null) {
          booleanArray1[row] = val.toLowerCase().startsWith("t");      //$NON-NLS-1$
        } else if(booleanArray2!=null) {
          booleanArray2[row][col] = val.toLowerCase().startsWith("t"); //$NON-NLS-1$
        }
        if(transposed)fireTableCellUpdated(col, row);
        else fireTableCellUpdated(row, col);
      }
    } catch(Exception ex) {
      //System.out.println("set value exception "+ex);
      // do nothing on numeric format exceptions and out of bounds exceptions                       
    }
  }

  /**
   * Determines whether the given cell is editable.
   *
   * @param row the row index
   * @param col the column index
   * @return true if editable
   */
  public boolean isCellEditable(int row, int col) {
    if(showRowNumber&&(col==0)) {
      return false; // row numbers are never editable
    }
    col = showRowNumber ? col-1 : col;
    Boolean val = lockedColumns.get(col);
    if(val==null) { // columns that are not in the dictionary are editable
      return editable;
    }
    return !val&&editable;
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
