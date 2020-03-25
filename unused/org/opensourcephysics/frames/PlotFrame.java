/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.DataTableFrame;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.display.dialogs.LogAxesInspector;
import org.opensourcephysics.display.dialogs.ScaleInspector;

/**
 * PlotFrame displays a plot using a dedicated DatasetManager.
 *
 * PlotFrame is a composite object that forwards methods to other objects such as a DatasetManager
 * or a DataTable.
 *
 * @author W. Christian
 * @version 1.0
 */
public class PlotFrame extends DrawingFrame {
  protected DatasetManager datasetManager = new DatasetManager();
  protected DataTable dataTable = new DataTable();
  protected DataTableFrame tableFrame;

  /**
   * Constructs the PlottingFrame with the given frame title and axes labels.
   *
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public PlotFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, null));
    setTitle(frameTitle);
    drawingPanel.addDrawable(datasetManager);
    datasetManager.setXPointsLinked(true);
    dataTable.add(datasetManager);
    setAnimated(true);
    setAutoclear(true);
    addMenuItems();
  }

  /**
   *  Sets the name of this component and the Dataset Manager.
   */
  public void setName(String name) {
    name = TeXParser.parseTeX(name);
    super.setName(name);
    datasetManager.setName(name);
  }

  /**
   * Adds Views menu items on the menu bar.
   */
  protected void addMenuItems() {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return;
    }
    JMenu helpMenu = this.removeMenu(DisplayRes.getString("DrawingFrame.Help_menu_item")); //$NON-NLS-1$
    JMenu menu = getMenu(DisplayRes.getString("DrawingFrame.Views_menu"));                 //$NON-NLS-1$
    if(menu==null) {
      menu = new JMenu(DisplayRes.getString("DrawingFrame.Views_menu")); //$NON-NLS-1$
      menuBar.add(menu);
      menuBar.validate();
    } else {                                                             // add a separator if tools already exists
      menu.addSeparator();
    }
    if(helpMenu!=null) {
      menuBar.add(helpMenu);
    }
    // add a scale option
    JMenuItem scaleItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Scale_menu_item")); //$NON-NLS-1$
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        scale();
      }

    };
    scaleItem.addActionListener(actionListener);
    menu.add(scaleItem);
    // add a log scale option
    JMenuItem logItem = new JMenuItem(DisplayRes.getString("DrawingFrame.LogAxes_menu_item")); //$NON-NLS-1$
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        logAxes();
      }

    };
    logItem.addActionListener(actionListener);
    menu.add(logItem);
    menu.addSeparator();
    // add a data table item to show the data table
    JMenuItem tableItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
    tableItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_SHORTCUT_KEY_MASK));
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showDataTable(true);
      }

    };
    tableItem.addActionListener(actionListener);
    menu.add(tableItem);
    // add data table to the popup menu
    if((drawingPanel!=null)&&(drawingPanel.getPopupMenu()!=null)) {
      JMenuItem item = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
      item.addActionListener(actionListener);
      drawingPanel.getPopupMenu().addSeparator();
      drawingPanel.getPopupMenu().add(item);
    }
  }

  /**
   * Sets the log scale property for the x axis.
   *
   * @param log boolean
   */
  public void setLogScaleX(boolean log) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setLogScaleX(log);
    }
  }

  /**
   * Sets the log scale property for the y axis.
   *
   * @param log boolean
   */
  public void setLogScaleY(boolean log) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setLogScaleY(log);
    }
  }

  protected void scale() {
    ScaleInspector plotInspector = new ScaleInspector(drawingPanel);
    plotInspector.setLocationRelativeTo(drawingPanel);
    plotInspector.updateDisplay();
    plotInspector.setVisible(true);
  }

  protected void logAxes() {
    if(!(drawingPanel instanceof PlottingPanel)) {
      return;
    }
    LogAxesInspector logAxesInspector = new LogAxesInspector((PlottingPanel) drawingPanel);
    logAxesInspector.setLocationRelativeTo(drawingPanel);
    logAxesInspector.updateDisplay();
    logAxesInspector.setVisible(true);
  }

  /**
   *  Appends an (x,y) datum to the Dataset with the given index.
   *
   * @param  datasetIndex  Description of Parameter
   * @param  x
   * @param  y
   */
  public void append(int datasetIndex, double x, double y) {
    datasetManager.append(datasetIndex, x, y);
    // may be too slow if lots of data is being added to the table
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   *  Appends a data point and its uncertainty to the Dataset.
   *
   * @param datasetIndex
   * @param  x
   * @param  y
   * @param  delx
   * @param  dely
   *
   */
  public void append(int datasetIndex, double x, double y, double delx, double dely) {
    datasetManager.append(datasetIndex, x, y, delx, dely);
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   * Appends (x,y) arrays to the Dataset.
   *
   * @param  datasetIndex  Description of Parameter
   * @param  xpoints
   * @param  ypoints
   */
  public void append(int datasetIndex, double[] xpoints, double[] ypoints) {
    datasetManager.append(datasetIndex, xpoints, ypoints);
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   *  Appends arrays of data points and uncertainties to the Dataset.
   *
   * @param datasetIndex
   * @param  xpoints
   * @param  ypoints
   * @param  delx
   * @param  dely
   */
  public void append(int datasetIndex, double[] xpoints, double[] ypoints, double[] delx, double[] dely) {
    datasetManager.append(datasetIndex, xpoints, ypoints, delx, dely);
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   *  Sets the connected flag for all datasets.
   *
   * @param connected true if connected; false otherwise
   */
  public void setConnected(boolean connected) {
    datasetManager.setConnected(connected);
  }

  /**
   * Sets the maximum number of allowed datapoints.
   *
   * Points will be dropped from the beginning of the dataset after the maximum number has been reached.
   *
   * @param maxPoints int
   */
  public void setMaximumPoints(int datasetIndex, int maxPoints) {
    datasetManager.getDataset(datasetIndex).setMaximumPoints(maxPoints);
  }

  /**
   * Sets a custom marker shape.
   *
   * @param datasetIndex int
   * @param marker Shape
   */
  public void setCustomMarker(int datasetIndex, Shape marker) {
    datasetManager.setCustomMarker(datasetIndex, marker);
  }

  /**
   *  Sets the data point marker shape. Shapes are: NO_MARKER, CIRCLE, SQUARE,
   *  AREA, PIXEL, BAR, POST
   *
   * @param  datasetIndex  The new markerShape value
   * @param  markerShape
   */
  public void setMarkerShape(int datasetIndex, int markerShape) {
    datasetManager.setMarkerShape(datasetIndex, markerShape);
  }

  /**
   *  Sets the half-width of the data point marker.
   *
   * @param  datasetIndex
   * @param  markerSize   in pixels
   */
  public void setMarkerSize(int datasetIndex, int markerSize) {
    datasetManager.setMarkerSize(datasetIndex, markerSize);
  }

  /**
   * Sets the data marker color for the given index.
   *
   * @param datasetIndex int
   * @param color Color
   */
  public void setMarkerColor(int datasetIndex, Color color) {
    datasetManager.setMarkerColor(datasetIndex, color);
  }

  /**
   * Sets the data line color for the given index.
   *
   * @param datasetIndex int
   * @param color Color
   */
  public void setLineColor(int datasetIndex, Color color) {
    datasetManager.setLineColor(datasetIndex, color);
  }

  /**
   * Sets the background color of this component.
   */
  public void setBackground(Color color) {
    super.setBackground(color);
    if(drawingPanel!=null) {
      drawingPanel.setBackground(color);
    }
  }

  /**
   * Sets the marker's fill and edge colors.
   *
   * The error bar color is set equal to the edge color.
   *
   * @param datasetIndex
   * @param fillColor
   * @param edgeColor
   */
  public void setMarkerColor(int datasetIndex, Color fillColor, Color edgeColor) {
    datasetManager.setMarkerColor(datasetIndex, fillColor, edgeColor);
  }

  /**
   * Sets the data connected flag. Points are connected by straight lines.
   *
   * @param  datasetIndex  The new connected value
   * @param  connected    <code>true<\code> if points are connected
   */
  public void setConnected(int datasetIndex, boolean connected) {
    datasetManager.setConnected(datasetIndex, connected);
  }

  /**
   * Sets the linked flag. X data for datasets > 0 will not be shown in a table view.
   *
   * @param  linked  The new value
   */
  public void setXPointsLinked(boolean linked) {
    datasetManager.setXPointsLinked(linked);
  }

  /**
   *  Sets the column names and the dataset name.
   *
   * @param  datasetIndex  The new xYColumnNames value
   * @param  xColumnName
   * @param  yColumnName
   * @param datasetName
   */
  public void setXYColumnNames(int datasetIndex, String xColumnName, String yColumnName, String datasetName) {
    datasetManager.setXYColumnNames(datasetIndex, xColumnName, yColumnName, datasetName);
  }

  /**
   *  Sets the column names when rendering this dataset in a JTable.
   *
   * @param  datasetIndex  The new xYColumnNames value
   * @param  xColumnName
   * @param  yColumnName
   */
  public void setXYColumnNames(int datasetIndex, String xColumnName, String yColumnName) {
    datasetManager.setXYColumnNames(datasetIndex, xColumnName, yColumnName);
  }

  /**
   *  Sets the maximum number of fraction digits to display for cells that have
   *  type Double
   *
   * @param  maximumFractionDigits  - maximum number of fraction digits to display
   */
  public void setMaximumFractionDigits(int maximumFractionDigits) {
    dataTable.setMaximumFractionDigits(maximumFractionDigits);
  }

  /**
   *  Sets the maximum number of fraction digits to display in all data table columns with
   *  cthe given columnName.
   *
   * @param  maximumFractionDigits  - maximum number of fraction digits to display
   * @param  columnName             The new maximumFractionDigits value
   */
  public void setMaximumFractionDigits(String columnName, int maximumFractionDigits) {
    dataTable.setMaximumFractionDigits(columnName, maximumFractionDigits);
  }

  /**
   *  Sets the display row number flag. Table displays row number.
   *
   * @param  vis  <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    dataTable.setRowNumberVisible(vis);
  }

  /**
   * Clears drawable objects added by the user to this frame.
   */
  public void clearDrawables() {
	  if(drawingPanel!=null) {
        drawingPanel.clear(); // removes all drawables
        drawingPanel.addDrawable(datasetManager);
	  }
  }

  /**
   *  Gets a dataset with the given index.
   *
   * @param index
   * @return    Dataset
   *
   */
  public Dataset getDataset(int index) {
    return datasetManager.getDataset(index);
  }

  /**
   *  Gets the dataset manager.
   *
   * @return    DatasetManager
   *
   */
  public DatasetManager getDatasetManager() {
    return datasetManager;
  }

  /**
   * Gets Drawable objects added by the user to this frame.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    ArrayList<Drawable> list = super.getDrawables();
    list.remove(datasetManager);
    return list;
  }

  /**
   * Gets Drawable objects added by the user of an assignable type. The list contains
   * objects that are assignable from the class or interface.
   *
   * @param c the type of Drawable object
   *
   * @return the cloned list
   *
   * @see #getObjectOfClass(Class c)
   */
  public synchronized <T extends Drawable> ArrayList<T> getDrawables(Class<T> c) {
    ArrayList<T> list = super.getDrawables(c);
    list.remove(datasetManager);
    return list;
  }

  /**
   * Clears the data from all datasets.
   * Dataset properties are preserved because only the data is cleared.
   */
  public void clearData() {
    datasetManager.clear();
    dataTable.refreshTable();
    if(drawingPanel!=null) {
      drawingPanel.invalidateImage();
    }
  }

  /**
   * Removes all Datasets and removes all objects from the drawing panel except the dataset manager.
   * Datasets are removed from the manager and dataset properties are not preserved.
   */

  /**
   * Removes datasets from the manager. New datasets will be created with default properties as needed.
   */
  public void removeDatasets() {
    datasetManager.removeDatasets();
    dataTable.refreshTable();
    if(drawingPanel!=null) {
      drawingPanel.invalidateImage();
    }
  }

  /**
   * Shows or hides the data table.
   *
   * @param show boolean
   */
  public synchronized void showDataTable(boolean show) {
    if(show) {
      if((tableFrame==null)||!tableFrame.isDisplayable()) {
        tableFrame = new DataTableFrame(getTitle()+" "+DisplayRes.getString("TableFrame.TitleAddOn.Data"), dataTable); //$NON-NLS-1$ //$NON-NLS-2$
        tableFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      }
      dataTable.refreshTable();
      tableFrame.setVisible(true);
    } else {
      tableFrame.setVisible(false);
      tableFrame.dispose();
      tableFrame = null;
    }
  }

  /**
   * Returns an XML.ObjectLoader to save and load data.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new PlotFrameLoader();
  }

  static protected class PlotFrameLoader extends DrawingFrame.DrawingFrameLoader {
    /**
    * Creates a PlotFame.
    *
    * @param control XMLControl
    * @return Object
    */
    public Object createObject(XMLControl control) {
      PlotFrame frame = new PlotFrame("x", "y", DisplayRes.getString("PlotFrame.Title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return frame;
    }

    /**
     * Loads the object with data from the control.
     *
     * @param control XMLControl
     * @param obj Object
     * @return Object
     */
    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      PlotFrame frame = ((PlotFrame) obj);
      ArrayList<?> list = frame.getObjectOfClass(DatasetManager.class);
      if(list.size()>0) { // assume the first DatasetManager is the manager for this frame
        frame.datasetManager = (DatasetManager) list.get(0);
        frame.dataTable.clear();
        frame.dataTable.add(frame.datasetManager);
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
