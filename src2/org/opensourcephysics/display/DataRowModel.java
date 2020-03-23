/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class DataRowModel extends AbstractTableModel {
  ArrayList<Object> rowList = new ArrayList<Object>();
  ArrayList<String> colNames = new ArrayList<String>();
  boolean rowNumberVisible = true;
  int colCount = 0, maxRows = -1;
  int firstRowIndex = 0;
  int stride = 1;

  /**
   * Constructor DataRowModel
   */
  public DataRowModel() {
    colNames.add(0, "row"); //default for zero column //$NON-NLS-1$
  }

  /**
   *  Sets the stride between rows.
   *
   * @param  tableModel
   * @param  stride
   */
  public void setStride(int stride) {
    this.stride = stride;
  }

  /**
   * Sets the maximum number of rows the data can hold
   */
  public void setMaxPoints(int max) {
    maxRows = max;
    if((maxRows<=0)||(rowList.size()<=max)) {
      return;
    }
    // Reset the table to that size
    for(int j = 0, n = rowList.size()-max; j<n; j++) {
      rowList.remove(0);
    }
    colCount = 0;
    for(int j = 0, n = rowList.size(); j<n; j++) {
      Object r = rowList.get(j);
      if(!r.getClass().isArray()) {
        continue;
      }
      int length = 0;
      if(r instanceof double[]) {
        length = ((double[]) r).length;
      } else if(r instanceof byte[]) {
        length = ((byte[]) r).length;
      } else if(r instanceof int[]) {
        length = ((int[]) r).length;
      } else if(r instanceof String[]) {
        length = ((String[]) r).length;
      }
      colCount = Math.max(colCount, length);
    }
  }

  /**
   * Clear the data
   */
  public void clear() { // Paco added this method
    rowList.clear();
    colCount = 0;
  }

  /**
   * Appends a  row to this table.
   *
   * @param obj Object
   * @throws IllegalArgumentException
   */
  public synchronized void appendRow(Object obj) throws IllegalArgumentException {
    if(!obj.getClass().isArray()) {
      throw new IllegalArgumentException("A TableData row must be an array."); //$NON-NLS-1$
    }
    // make sure ultimate component class is acceptable
    Class<?> componentType = obj.getClass().getComponentType();
    String type = componentType.getName();
    if(type.equals("double")) {        //$NON-NLS-1$
      appendDoubles((double[]) obj);
    } else if(type.equals("int")) {    //$NON-NLS-1$
      appendInts((int[]) obj);
    } else if(type.equals("byte")) {   //$NON-NLS-1$
      appendBytes((byte[]) obj);
    } else if(type.equals("string")) { //$NON-NLS-1$
      appendStrings((String[]) obj);
    } else {
      Object[] row = (Object[]) obj;
      String[] strings = new String[row.length];
      for(int i = 0, n = row.length; i<n; i++) {
        strings[i] = row[i].toString();
      }
      appendStrings(strings);
    }
  }

  /**
   * Appends a row of data.
   *
   * @param x double[]
   */
  void appendDoubles(double[] x) {
    double[] row;
    if(x==null) {
      return;
    }
    row = new double[x.length];
    System.arraycopy(x, 0, row, 0, x.length);
    if((maxRows>0)&&(rowList.size()>=maxRows)) {
      rowList.remove(0); // Paco added this line
    }
    rowList.add(row);
    colCount = Math.max(colCount, row.length+1);
  }

  /**
   * Appends a row of data.
   *
   * @param x double[]
   */
  void appendInts(int[] x) {
    int[] row;
    if(x==null) {
      return;
    }
    row = new int[x.length];
    System.arraycopy(x, 0, row, 0, x.length);
    if((maxRows>0)&&(rowList.size()>=maxRows)) {
      rowList.remove(0); // Paco added this line
    }
    rowList.add(row);
    colCount = Math.max(colCount, row.length+1);
  }

  /**
   * Appends a row of data.
   *
   * @param x double[]
   */
  void appendBytes(byte[] x) {
    byte[] row;
    if(x==null) {
      return;
    }
    row = new byte[x.length];
    System.arraycopy(x, 0, row, 0, x.length);
    if((maxRows>0)&&(rowList.size()>=maxRows)) {
      rowList.remove(0); // Paco added this line
    }
    rowList.add(row);
    colCount = Math.max(colCount, row.length+1);
  }

  /**
   * Appends a row of data.
   *
   * @param x double[]
   */
  void appendStrings(String[] x) {
    String[] row;
    if(x==null) {
      return;
    }
    row = new String[x.length];
    System.arraycopy(x, 0, row, 0, x.length);
    if((maxRows>0)&&(rowList.size()>=maxRows)) {
      rowList.remove(0); // Paco added this line
    }
    rowList.add(row);
    colCount = Math.max(colCount, row.length+1);
  }

  /**
   *  Sets the display row number flag. Table displays row number.
   *
   * @param  vis  <code>true<\code> if table display row number
   * @return true if table display changed
   */
  public boolean setRowNumberVisible(boolean vis) {
    if(rowNumberVisible==vis) {
      return false;
    }
    rowNumberVisible = vis;
    return true;
  }

  /**
   *  Sets the column names in this table.
   *
   * @param  column  the column index
   * @param  name
   * @return true if name changed or added
   */
  public boolean setColumnNames(int column, String name) {
    name = TeXParser.parseTeX(name);
    if( (colNames==null)|| (column<colNames.size())&& colNames.get(column)!=null && colNames.get(column).equals(name)) {  // W. Christian added null check
      return false;
    }
    while(column>=colNames.size()) {
      colNames.add(""+(char) ('A'+column)); //$NON-NLS-1$
    }
    colNames.set(column, name);
    return true;
  }

  /**
   * Sets the first row's index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    firstRowIndex = index;
  }

  /**
   * Gets the number of columns being shown.
   *
   * @return the column count
   */
  public int getColumnCount() {
	int offset=rowNumberVisible?0:1;
    if(getRowCount()==0) {
      return(colNames==null) ? 0 : colNames.size()-offset;
      //return 0;
    }
    int count = (rowNumberVisible) ? colCount : colCount-1;
    return count;
  }

  /**
   * Gets the name of the specified column.
   *
   * @param column the column index
   * @return the column name
   */
  public String getColumnName(int column) {
    if((column==0)&&rowNumberVisible) {
      return colNames.get(0);
    }
    if(!rowNumberVisible) {
      column++;
    }
    if(column<colNames.size()) {
      return colNames.get(column);
    }
    return ""+(char) ('A'+column-1); //$NON-NLS-1$
  }

  /**
   * Gets the number of rows.
   *
   * @return the row count
   */
  public int getRowCount() {
    return(rowList.size()+stride-1)/stride;
    //return rowList.size();
  }

  /**
   * Gets the value at the given cell.
   *
   * @param row the row index
   * @param column the column index
   * @return the value
   */
  public Object getValueAt(int row, int column) {
    row = row*stride;
    if((column==0)&&rowNumberVisible) {
      return new Integer(row+firstRowIndex);
    }
    if(!rowNumberVisible) {
      column++;
    }
    if(row>=rowList.size()) {
      return ""; //$NON-NLS-1$
    }
    Object r = rowList.get(row);
    if(!r.getClass().isArray()) {
      return ""; //$NON-NLS-1$
    }
    if(r instanceof double[]) {
      double[] array = (double[]) r;
      if(column>array.length) {
        return ""; //$NON-NLS-1$
      }
      return new Double(array[column-1]);
    }
    if(r instanceof byte[]) {
      byte[] array = (byte[]) r;
      if(column>array.length) {
        return ""; //$NON-NLS-1$
      }
      return new Byte(array[column-1]);
    }
    if(r instanceof int[]) {
      int[] array = (int[]) r;
      if(column>array.length) {
        return ""; //$NON-NLS-1$
      }
      return new Integer(array[column-1]);
    }
    if(r instanceof String[]) {
      String[] array = (String[]) r;
      if(column>array.length) {
        return ""; //$NON-NLS-1$
      }
      return array[column-1];
    }
    return ""; //$NON-NLS-1$
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
