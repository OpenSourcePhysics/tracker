/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Data adapter class implements the Data interface for double[][] arrays.
 *
 * @author Wolfgang Christian
 * @author Doug Brown
 */
public class DataAdapter implements Data {
  protected String[] colNames;
  protected String name = "Array Data"; //$NON-NLS-1$
  protected double[][] data;
  protected int ID = hashCode();

  /**
   * Constructor DataAdapter
   * @param array
   */
  public DataAdapter(double[][] array) {
    data = array;
  }

  /**
   * Gets column names.  Client should assign colors.
   * Implementation of Data interface.
   */
  public String[] getColumnNames() {
    return colNames;
  }

  /**
   * Sets the column names.
   * @param names
   */
  public void setColumnNames(String[] names) {
	if(names==null) return;
    colNames = names.clone();
  }

  /**
   * Gets the name of the Data.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the Data.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the double[][] array.
   * Implementation of Data interface.
   */
  public double[][] getData2D() {
    return data;
  }

  /**
   * Not used.
   * Implementation of Data interface method.
   */
  public double[][][] getData3D() {
    return null;
  }

  /**
   * Not used.
   * Implementation of Data interface.
   */
  public List<Data> getDataList() {
    return null;
  }

  /**
   * Not used Data because is stored in 2D array.
   * Implementation of Data interface.
   */
  public ArrayList<Dataset> getDatasets() {
    return null;
  }

  /**
   * Fill colors for columns are not specified. Client should assign colors.
   * Implementation of Data interface.
   */
  public Color[] getFillColors() {
    return null;
  }

  /**
   * Lines colors for columns are not specified.  Client should assign colors.
   * Implementation of Data interface.
   */
  public Color[] getLineColors() {
    return null;
  }

  /**
   * Gets the Data ID.
   */
  public int getID() {
    return ID;
  }

  /**
   * Sets the Data ID.
   */
  public void setID(int id) {
    ID = id;
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
