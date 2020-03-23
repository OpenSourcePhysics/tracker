/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import javax.swing.table.AbstractTableModel;
import org.opensourcephysics.controls.ControlUtils;

public class GridTableModel extends AbstractTableModel {
  GridData griddata;
  int component;

  /**
   * Constructor GridTableModel
   * @param griddata
   * @param component
   */
  public GridTableModel(GridData griddata, int component) {
    this.griddata = griddata;
    this.component = component;
  }

  /**
   * Gets the number of columns.
   *
   * @return int
   */
  public int getColumnCount() {
    return griddata.getNx()+1;
  }

  /**
   * Gets the number of columns.
   *
   * @return int
   */
  public String getColumnName(int c) {
    if(c==0) {
      return "j\\i"; //$NON-NLS-1$
    }
    return ""+(c-1); //$NON-NLS-1$
  }

  /**
   * Gets the number of rows.
   *
   * @return int
   */
  public int getRowCount() {
    return griddata.getNx();
  }

  /**
   * Gets the value at the given grid location.
   *
   * @param rowIndex int
   * @param columnIndex int
   * @return Object
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    // return new Double(griddata.getValue(rowIndex,columnIndex,0));
    if(columnIndex==0) {
      return new Integer(rowIndex);
    }
    return ControlUtils.f3(griddata.getValue(columnIndex-1, rowIndex, component));
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
