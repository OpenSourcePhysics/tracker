/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;

/**
 * The Data interface defines methods for obtaining and identifying OSP data.
 *
 * @author Wolfgang Christian, Douglas Brown
 * @version 1.0
 */
public interface Data {
  /**
   * Gets a 2D array of data.
   * The first column, double[0][] often contains x-values;
   * Remaining columns often contain y values.
   * May return null if data not yet generated or object does not support 2D data.
   *
   * @return double[][]
   */
  public double[][] getData2D();

  /**
   * Gets a 3D array of data.
   * May return null if data not yet generated or object does not support 3D data.
   *
   * @return double[][][]
   */
  public double[][][] getData3D();

  /**
   * Gets a list of OSP Datasets.
   * May return null if data not yet generated or object does not support Datasets.
   *
   * @return list of Datasets
   */
  public java.util.ArrayList<Dataset> getDatasets();

  /**
   * The name of the data
   * @return the name
   */
  public String getName();

  /**
   * Line color to use for this data
   * @return a color
   */
  public java.awt.Color[] getLineColors();

  /**
   * Fill color to use for this data
   * @return a color
   */
  public java.awt.Color[] getFillColors();

  /**
   * The column names to be used in the data display tool
   * @return an array of names
   */
  public String[] getColumnNames();

  /**
   * Some Data objects (e.g., a Group) do not contain data, but a list of Data objects which do.
   * This method is used by Data displaying tools to create as many pages as needed.
   * @return a list of Data objects, or null if this object contains data
   */
  public java.util.List<Data> getDataList();

  /**
   * Returns a unique identifier for this Data
   * @return the ID number
   */
  public int getID();

  /**
   * Sets the ID number of this Data
   *
   * @param id the ID number
   */
  public void setID(int id);
  //   /**
  //    * Sets the data to that of a source Data object
  //    *
  //    * @param source the source Data
  //    */
  //   public void setData(Data source);
  //

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
