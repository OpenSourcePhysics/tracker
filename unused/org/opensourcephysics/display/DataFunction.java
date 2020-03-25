/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.numerics.ParsedMultiVarFunction;
import org.opensourcephysics.numerics.ParserException;
import org.opensourcephysics.tools.ToolsRes;

/**
 * This is a dataset whose values are determined by a multivariable function
 * evaluated with input constants and linked datasets.
 *
 * @author Douglas Brown
 */
public class DataFunction extends Dataset {
  // instance fields
  DatasetManager inputData;
  ParsedMultiVarFunction function;
  String functionString;
  String inputString; // recent attempted function string, successful or not
  int varCount;
  ArrayList<double[]> data = new ArrayList<double[]>();

  /**
   * Constructs a DataFunction for the specified input data.
   *
   * @param input the input data
   */
  public DataFunction(DatasetManager input) {
    inputData = input;
    String name = ToolsRes.getString("DataFunction.DefaultName"); //$NON-NLS-1$
    setXYColumnNames(input.getDataset(0).getXColumnName(), name);
    setXColumnVisible(false);
    setExpression(""); //$NON-NLS-1$
  }

  /**
   * Constructs a DataFunction for the specified input data, name and expression.
   *
   * @param input the input data
   * @param name
   * @param expression
   */
  public DataFunction(DatasetManager input, String name, String expression) {
    inputData = input;
    setXYColumnNames(input.getDataset(0).getXColumnName(), name);
    setXColumnVisible(false);
    setExpression(expression);
  }

  /**
   * Sets the expression.
   *
   * @param e the expression string
   */
  public void setExpression(String e) {
    // set the variable count for refresh purposes
    varCount = getVarCount();
    try {
      function = new ParsedMultiVarFunction(e, getVarNames());
      functionString = e;
      inputString = e;
      refreshFunctionData();
    } catch(ParserException ex) {
      setExpression("0"); //$NON-NLS-1$
      inputString = e;    // unsuccessful string
      refreshFunctionData();
    }
  }

  /**
   * Gets the expression.
   *
   * @return the expression string
   */
  public String getExpression() {
    return functionString;
  }

  /**
   * Sets the y-column name (ie the function name).
   *
   * @param name the name
   */
  public void setYColumnName(String name) {
    if((name==null)||name.equals("")) { //$NON-NLS-1$
      return; 
    }
    setXYColumnNames(getXColumnName(), name);
  }

  /**
   * Gets the current input string. If the last attempt to set the function
   * was unsuccessful, this is different from the function string.
   *
   * @return the input string
   */
  public String getInputString() {
    return inputString;
  }

  /**
   * Refreshes the data points.
   */
  public void refreshFunctionData() {
    super.clear();
    if(function==null) {
      return;
    }
    // watch for change in inputs
    if(varCount!=getVarCount()) {
      setExpression(inputString);
      return;
    }
    double[][] data = getFunctionData();
    if(data.length==0) {
      return;
    }
    double[] fData = new double[data.length];
    int len = data[0].length;
    for(int n = 0; n<len; n++) {               // number of data values for each variable
      for(int i = 0; i<data.length; i++) {     // number of variables
        if(n<data[i].length) {
          fData[i] = data[i][n];
        } else {
          fData[i] = Double.NaN;
        }
      }
      double val = Double.NaN;
      if(!"0".equals(functionString)||"0".equals(inputString)) { //$NON-NLS-1$ //$NON-NLS-2$
      	val = function.evaluate(fData);
      	if (function.evaluatedToNaN()) {
          val = Double.NaN;
      	}
//        String[] names = getVarNames();
//        for(int i = 0; i<names.length; i++) {
//          if(expressionContainsName(names[i]) && Double.isNaN(fData[i])) {
//            val = Double.NaN;
//          }
//        }
      }
      super.append(fData[0], val);
    }
  }

  /**
   * @return the name of this DataFunction
   */
  public String toString() {
    return getYColumnName();
  }

  /**
   * Overrides Dataset methods. DatasetFunction manages its own data.
   */
  public void append(double x, double y) {

  /** empty block */
  }

  public void append(double x, double y, double dx, double dy) {

  /** empty block */
  }

  public void append(double[] x, double[] y) {

  /** empty block */
  }

  public void append(double[] x, double[] y, double[] dx, double[] dy) {

  /** empty block */
  }

  public void clear() {

  /** empty block */
  }

  //______________________________ private methods ___________________________
  
  // returns arrays of values
  private double[][] getFunctionData() {
    int length = 0;
    data.clear();
    Iterator<Dataset> it = inputData.getDatasets().iterator();
    while(it.hasNext()) {
      Dataset dataset = it.next();
      if(dataset==this) {
        continue;
      }
      // add linked variable (x-column) first
      if(data.isEmpty()) {
        double[] xPoints = dataset.getXPoints();
        length = xPoints.length;
        data.add(xPoints);
      }
      // add y-columns
      data.add(dataset.getYPoints());
    }
    String[] names = inputData.getConstantNames();
    for(String next: names) {
      double[] points = new double[length];
      double val = inputData.getConstantValue(next);
      for(int i = 0; i<length; i++) {
        points[i] = val;
      }
      data.add(points);
    }
    return data.toArray(new double[0][0]);
  }

  private int getVarCount() {
    ArrayList<Dataset> list = inputData.getDatasets();
    int count = list.contains(this) ? list.size() : list.size()+1;
    return count+inputData.getConstantNames().length;
//    return count+inputData.getProperties().size();
  }

  private String[] getVarNames() {
    List<String> names = new ArrayList<String>();
    Iterator<Dataset> it = inputData.getDatasets().iterator();
    while(it.hasNext()) {
      Dataset dataset = it.next();
      if(dataset==this) {
        continue;
      }
      String name = null;
      if(names.isEmpty()) {
        name = TeXParser.removeSubscripting(dataset.getXColumnName());
        names.add(name);
      }
      name = TeXParser.removeSubscripting(dataset.getYColumnName());
      names.add(name);
    }
    for (String name: inputData.getConstantNames()) {
    	names.add(name);
    }
//    names.addAll(inputData.getProperties().keySet());
    return names.toArray(new String[0]);
  }
  
  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load DataFunction data in an XMLControl.
   */
  protected static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      DataFunction data = (DataFunction) obj;
      control.setValue("function_name", data.getYColumnName()); //$NON-NLS-1$
      control.setValue("function", data.getInputString());      //$NON-NLS-1$
      // save Dataset properties for loading into Datasets
      XML.getLoader(Dataset.class).saveObject(control, obj);
    }

    public Object createObject(XMLControl control) {
      // must be instantiated with DatasetManager
      return null;
    }

    public Object loadObject(XMLControl control, Object obj) {
      DataFunction data = (DataFunction) obj;
      data.setYColumnName(control.getString("function_name")); //$NON-NLS-1$
      data.setExpression(control.getString("function"));       //$NON-NLS-1$
      data.setYColumnDescription(control.getString("y_description")); //$NON-NLS-1$
      data.setID(control.getInt("datasetID"));                 //$NON-NLS-1$
      // load Dataset display properties but not data itself
      if(control.getPropertyNames().contains("marker_shape")) { //$NON-NLS-1$
        data.setMarkerShape(control.getInt("marker_shape"));    //$NON-NLS-1$
      }
      if(control.getPropertyNames().contains("marker_size")) { //$NON-NLS-1$
        data.setMarkerSize(control.getInt("marker_size"));     //$NON-NLS-1$
      }
      data.setSorted(control.getBoolean("sorted"));       //$NON-NLS-1$
      data.setConnected(control.getBoolean("connected")); //$NON-NLS-1$
      Color color = (Color) control.getObject("line_color"); //$NON-NLS-1$
      if(color!=null) {
        data.setLineColor(color);
      }
      Color fill = (Color) control.getObject("fill_color"); //$NON-NLS-1$
      color = (Color) control.getObject("edge_color"); //$NON-NLS-1$
      if(fill!=null) {
        if(color!=null) {
          data.setMarkerColor(fill, color);
        } else {
          data.setMarkerColor(fill);
        }
      }
      return obj;
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
