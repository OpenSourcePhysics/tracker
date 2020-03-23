/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.DataTableFrame;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.Histogram;
import org.opensourcephysics.display.HistogramDataset;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.Tool;

/**
 * HistogramFrame displays a histogram using a dedicated Histogram object.
 *
 * @author W. Christian
 * @version 1.0
 */
public class HistogramFrame extends DrawingFrame {
  protected Histogram histogram = new Histogram();
  protected DataTable dataTable = new DataTable();
  protected DataTableFrame tableFrame;
  DataTool tool;
  HistogramDataset dataset;
  JCheckBoxMenuItem logItem;

  /**
   * A DrawingFrame with a Histogram as its drawable.
   *
   * @param xlabel String
   * @param ylabel String
   * @param title String
   */
  public HistogramFrame(String xlabel, String ylabel, String title) {
    super(new PlottingPanel(xlabel, ylabel, null));
    // histogram.setDiscrete(false) ;
    drawingPanel.addDrawable(histogram);
    setTitle(title);
    dataTable.add(histogram);
    setAnimated(true);
    setAutoclear(true);
    addMenuItems();
  }

  /**
   *  Sets the column names and the dataset name.
   *
   * @param  datasetIndex  The new xYColumnNames value
   * @param  xColumnName
   * @param  yColumnName
   * @param datasetName
   */
  public void setXYColumnNames(String xColumnName, String yColumnName, String histogramName) {
    histogram.setXYColumnNames(xColumnName, yColumnName, histogramName);
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
    } else {                                                             // add a separator if views exists
      menu.addSeparator();
    }
    if(helpMenu!=null) {
      menuBar.add(helpMenu);
    }
    // add a menu item to show the data table
    JMenuItem tableItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
    tableItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_SHORTCUT_KEY_MASK));
    ActionListener tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showDataTable(true);
      }

    };
    tableItem.addActionListener(tableListener);
    menu.add(tableItem);
    // log scale
    menu.addSeparator();
    logItem = new JCheckBoxMenuItem(DisplayRes.getString("HistogramFrame.MenuItem.LogScale"), false); //$NON-NLS-1$
    logItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        histogram.logScale = logItem.isSelected();
        drawingPanel.repaint();
      }

    });
    //menu.add(logItem);
    // add to popup menu
    JMenuItem item = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
    item.addActionListener(tableListener);
    if((drawingPanel!=null)&&(drawingPanel.getPopupMenu()!=null)) {
      drawingPanel.getPopupMenu().add(item);
    }
  }

  /**
   * Adds launchable tools to the specified menu.
   *
   */
  protected JMenu loadToolsMenu() {
	if(org.opensourcephysics.js.JSUtil.isJS) {  // external tools not supported in JavaScript.
		  return null;
	}
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    // add menu item
    JMenu toolsMenu = new JMenu(DisplayRes.getString("DrawingFrame.Tools_menu_title")); //$NON-NLS-1$
    menuBar.add(toolsMenu);
    // test dataset tool
    JMenuItem datasetItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DatasetTool_menu_item")); //$NON-NLS-1$
    toolsMenu.add(datasetItem);
    /*
     * datasetItem.addActionListener(new ActionListener() {
     *  public void actionPerformed(ActionEvent e) {
     *      tool=DataTool.getTool();
     *      if(tool==null || !tool.isDisplayable()){
     *        tool = new DataTool(histogram, histogram.getName());
     *      }else{
     *        tool.addTab(histogram, histogram.getName());
     *      }
     *      tool.setVisible(true);
     *    }
     *    });
     */
    Class<?> datasetToolClass = null;
    if(OSPRuntime.loadDataTool) {
      try {
        datasetToolClass = Class.forName("org.opensourcephysics.tools.DataTool");      //$NON-NLS-1$
      } catch(Exception ex) {
        OSPRuntime.loadDataTool = false;
        datasetItem.setEnabled(false);
        OSPLog.finest("Cannot instantiate data analysis tool class:\n"+ex.toString()); //$NON-NLS-1$
      }
    }
    final Class<?> finalDatasetToolClass = datasetToolClass; // class must be final for action listener
    datasetItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Method m = finalDatasetToolClass.getMethod("getTool", (Class[]) null); //$NON-NLS-1$
          Tool tool = (Tool) m.invoke(null, (Object[]) null);
          tool.send(new LocalJob(drawingPanel), reply);
          if(tool instanceof OSPFrame) {
            ((OSPFrame) tool).setKeepHidden(false);
          }
          ((JFrame) tool).setVisible(true);
        } catch(Exception ex) {}
      }

    });
    return toolsMenu;
  }

  /**
   *  Gets an array containing the bin centers.
   *
   * @return   the bins
   */
  public double[] getXPoints() {
    return histogram.getXPoints();
  }

  /**
   * Gets an array containing the values stored in the bins.
   *
   * @return    the values of the bins
   */
  public double[] getYPoints() {
    return histogram.getYPoints();
  }

  /**
   * Gets an array containing the log values of the values stored in the bins.
   *
   * @return    the values of the bins
   */
  public double[][] getLogPoints() {
    return histogram.getLogPoints();
  }

  /**
   * Gets a data array containing both the bin centers and the values within the bins.
   *
   * @return a double[index][2] array of data
   */
  public double[][] getPoints() {
    return histogram.getPoints();
  }

  /**
   * Removes drawable objects added by the user from this frame.
   */
  public void clearDrawables() {
    drawingPanel.clear();                // removes all drawables
    drawingPanel.addDrawable(histogram); // puts complex dataset back into panel
    showDataTable(false);
  }

  /**
   * Gets Drawable objects added by the user to this frame.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    ArrayList<Drawable> list = super.getDrawables();
    list.remove(histogram);
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
    list.remove(histogram);
    return list;
  }

  /**
   * Clears all the data stored.
   */
  public void clearData() {
    histogram.clear();
    dataTable.refreshTable();
    if(drawingPanel!=null) {
      drawingPanel.invalidateImage();
    }
  }

  /**
   * Appends a data point to the histogram.
   * @param v  data point
   */
  public void append(double v) {
    histogram.append(v);
    // this may be slow if the table is large
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   *  Append a value with number of occurences to the Histogram.
   *
   * @param  value
   * @param  numberOfOccurences
   */
  public void append(double value, double numberOfOccurences) {
    histogram.append(value, numberOfOccurences);
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   *  Appends an array of values with 1 occurence.
   *
   * @param  values
   */
  public void append(double[] values) {
    histogram.append(values);
    // this may be slow if the table is large
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   *  Sets the discrete flag in the histogram.
   *
   * @param  b  <code>true<\code> if bins are discrete, <code>false<\code> if bins are continuous.
   */
  public void setDiscrete(boolean b) {
    histogram.setDiscrete(b);
  }

  /**
   * Histogram uses logarithmic scale (true/false)
   */
  public void setLogScale(boolean b) {
    histogram.logScale = b;
    logItem.setSelected(b);
  }

  /**
   * Gets the histogram's log scale value.
   * @return boolean
   */
  public boolean isLogScale() {
    return histogram.logScale;
  }

  /**
   *  Sets the width of the bins.
   *
   * @param  binWidth
   */
  public void setBinWidth(double binWidth) {
    histogram.setBinWidth(binWidth);
  }

  /**
   * Gets the width of the bins.
   *
   * @param  binWidth
   */
  public double getBinWidth() {
    return histogram.getBinWidth();
  }

  /**
   * Sets the bin's fill and edge colors.  If the fill color is null the bin is not filled.
   *
   * @param fillColor
   * @param edgeColor
   */
  public void setBinColor(Color fillColor, Color edgeColor) {
    histogram.setBinColor(fillColor, edgeColor);
  }

  /**
   *  Sets the style for drawing this histogram. Options are DRAW_POINT, which
   *  draws a point at the top of the bin, and DRAW_BIN which draws the entire
   *  bin down to the x axis. Default is DRAW_BIN.
   *
   * @param  style
   */
  public void setBinStyle(short style) {
    histogram.setBinStyle(style);
  }

  /**
   * Sets the offset of the bins. Default is 0.
   * A value will be appended to bin n if
   * n*binWidth +binOffset <= value < (n+1)*binWidth +binOffset
   *
   * @param  binOffset
   */
  public void setBinOffset(double binOffset) {
    histogram.setBinOffset(binOffset);
  }

  /**
   * Normalizes the occurrences in this histogram to one (true/false).
   */
  public void setNormalizedToOne(boolean b) {
    histogram.setNormalizedToOne(b);
    histogram.adjustForWidth = b;
  }

  /**
   * Makes the x axis positive by default.
   */
  public void positiveX() {
    boolean b = drawingPanel.isAutoscaleX();
    drawingPanel.setPreferredMinMaxX(0, drawingPanel.getPreferredXMax());
    drawingPanel.setAutoscaleX(b);
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
      dataTable.sort(0);
      tableFrame.setVisible(true);
    } else {
      tableFrame.setVisible(false);
      tableFrame.dispose();
      tableFrame = null;
    }
  }

  public static XML.ObjectLoader getLoader() {
    return new HistogramFrameLoader();
  }

  static protected class HistogramFrameLoader extends DrawingFrame.DrawingFrameLoader {
    /**
     * Creates a PlotFame.
     *
     * @param control XMLControl
     * @return Object
     */
    public Object createObject(XMLControl control) {
      HistogramFrame frame = new HistogramFrame("x", "y", //$NON-NLS-1$ //$NON-NLS-2$
        DisplayRes.getString("HistogramFrame.Title"));    //$NON-NLS-1$
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
      HistogramFrame frame = ((HistogramFrame) obj);
      ArrayList<?> list = frame.getObjectOfClass(Histogram.class);
      if(list.size()>0) { // assume the first Histogram belongs to this frame
        frame.histogram = (Histogram) list.get(0);
        frame.histogram.clear();
        frame.dataTable.add(frame.histogram);
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
 *
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
