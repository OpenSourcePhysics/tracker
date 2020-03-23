/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Sorts table column based on their numeric values if the table's values are
 * numeric.  Otherwise sorts using toString.
 *
 * @author W. Christian
 * @version 1.0
 */
public class SortDecorator implements TableModel, TableModelListener {
  private TableModel realModel;
  private int indexes[];
  private int sortedColumn; // added by D Brown 2010-10-24

  /**
   * Constructor SortDecorator
   * @param model
   */
  public SortDecorator(TableModel model) {
    if(model==null) {
      throw new IllegalArgumentException("null models are not allowed"); //$NON-NLS-1$
    }
    this.realModel = model;
    realModel.addTableModelListener(this);
    allocate();
  }
  
  /**
   * Gets the sorted row number for a given real model row
   * Added by D Brown Dec 2017
   * 
   * @param realModelRow the unsorted row number
   * @return the sorted row number
   */
  public int getSortedRow(int realModelRow) {
  	for (int i=0; i< indexes.length; i++) {
  		if (indexes[i]==realModelRow) return i;
  	}
  	return -1;
  }

  public Object getValueAt(int row, int column) {
  	if (column>=getColumnCount()) { return null; }
    if(indexes.length<=row) {
      allocate();
    }
    return realModel.getValueAt(indexes[row], column);
  }

  public void setValueAt(Object aValue, int row, int column) {
    if(indexes.length<=row) {
      allocate();
    }
    realModel.setValueAt(aValue, indexes[row], column);
  }

  public void tableChanged(TableModelEvent e) {
    allocate();
  }

  public void sort(int column) {
  	sortedColumn = column;
    int rowCount = getRowCount();
    if(indexes.length<=rowCount) {
      allocate();
    }
    
    // new faster sort method added by D Brown 2015-05-16
    try {
			if (realModel.getColumnClass(column)==Double.class
					|| realModel.getColumnClass(column)==Integer.class) {
				Double[][] sortArray = new Double[rowCount][2];
				if (realModel.getColumnClass(column)==Double.class) {
			    for(int i = 0; i<rowCount; i++) {
			    	sortArray[i][0] = (Double)realModel.getValueAt(i, column);
			    	sortArray[i][1] = 1.0*indexes[i];
			    }
				}
				else {
			    for(int i = 0; i<rowCount; i++) {
			    	sortArray[i][0] = ((Integer)realModel.getValueAt(i, column)).doubleValue();
			    	sortArray[i][1] = 1.0*indexes[i];
			    }
				}
			  Arrays.sort(sortArray, new Comparator<Double[]>() {
					public int compare(Double[] a, Double[] b) {
						if (a[0]==null || b[0]==null) {
							return b[0]==a[0]? 0: b[0]==null? -1: 1;
						}
			      return(b[0]<a[0]) ? 1 : ((b[0]>a[0]) ? -1 : 0);
					}	    	
			  });
			  for(int i = 0; i<rowCount; i++) {
			  	indexes[i] = sortArray[i][1].intValue();
			  }	    
			}
			else {  // use older sort method for String data 
			  for(int i = 0; i<rowCount; i++) {
			    for(int j = i+1; j<rowCount; j++) {
			      if(compare(indexes[i], indexes[j], column)<0) {
			        swap(i, j);
			      }
			    }
			  }
			}
		} catch (Exception e) {
		}
  }
  
  // added by D Brown 2010-10-24
  public int getSortedColumn() {
  	return sortedColumn;
  }

  public void swap(int i, int j) {
    int tmp = indexes[i];
    indexes[i] = indexes[j];
    indexes[j] = tmp;
  }

  public int compare(int i, int j, int column) {
    Object io = realModel.getValueAt(i, column);
    Object jo = realModel.getValueAt(j, column);
    if((io!=null)&&(jo==null)) {
      return 1;
    }
    if((io==null)&&(jo!=null)) {
      return -1;
    }
    if((io==null)&&(jo==null)) {
      return 0;
    }
    if((io instanceof Integer)&&(jo instanceof Integer)) {
      int a = ((Integer) io).intValue();
      int b = ((Integer) jo).intValue();
      return(b<a) ? -1 : ((b>a) ? 1 : 0);
    } else if((io instanceof Double)&&(jo instanceof Double)) {
      double a = ((Double) io).doubleValue();
      double b = ((Double) jo).doubleValue();
      return(b<a) ? -1 : ((b>a) ? 1 : 0);
    } else if((io instanceof Integer)&&(jo instanceof Double)) {
      int a = ((Integer) io).intValue();
      double b = ((Double) jo).doubleValue();
      return(b<a) ? -1 : ((b>a) ? 1 : 0);
    } else if((io instanceof Double)&&(jo instanceof Integer)) {
      double a = ((Double) io).doubleValue();
      int b = ((Integer) jo).intValue();
      return(b<a) ? -1 : ((b>a) ? 1 : 0);
    }
    int c = jo.toString().compareTo(io.toString());
    return(c<0) ? -1 : ((c>0) ? 1 : 0);
  }

  private void allocate() {
    indexes = new int[getRowCount()];
    for(int i = 0; i<indexes.length; ++i) {
      indexes[i] = i;
    }
  }
  
  public void reset() {
  	allocate();
  	sortedColumn = -1;
  }

  public int getRowCount() {
    return realModel.getRowCount();
  }

  public int getColumnCount() {
    return realModel.getColumnCount();
  }

  public String getColumnName(int columnIndex) {
  	if (columnIndex>=getColumnCount()) { return "unknown"; } //$NON-NLS-1$
    return realModel.getColumnName(columnIndex);
  }

  public Class<?> getColumnClass(int columnIndex) {
  	if (columnIndex>=getColumnCount()) { return Object.class; }
    return realModel.getColumnClass(columnIndex);
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
  	if (columnIndex>=getColumnCount()) { return false; }
    return realModel.isCellEditable(rowIndex, columnIndex);
  }

  public void addTableModelListener(TableModelListener l) {
    realModel.addTableModelListener(l);
  }

  public void removeTableModelListener(TableModelListener l) {
    realModel.removeTableModelListener(l);
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
