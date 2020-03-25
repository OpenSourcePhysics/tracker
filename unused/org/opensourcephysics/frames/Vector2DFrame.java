/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display2d.ArrayData;
import org.opensourcephysics.display2d.GridData;
import org.opensourcephysics.display2d.GridTableFrame;
import org.opensourcephysics.display2d.VectorPlot;

/**
 * A DrawingFrame that displays 2D plots of vector fields.
 *
 * @author W. Christian
 * @version 1.0
 */
public class Vector2DFrame extends DrawingFrame {
  GridData gridData;
  VectorPlot plot = new VectorPlot(null);
  GridTableFrame tableFrame;

  /**
   * Constructs a Vector2DFrame with the given axes labels and frame title.
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public Vector2DFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, null));
    drawingPanel.setPreferredSize(new Dimension(350, 350));
    setTitle(frameTitle);
    plot.setShowGridLines(false);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorXGrid(false);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorYGrid(false);
    drawingPanel.addDrawable(plot);
    addMenuItems();
    setAnimated(true);
    setAutoclear(true);
  }

  /**
   * Constructs a Vector2DFrame with the given frame title but without axes.
   * @param frameTitle String
   */
  public Vector2DFrame(String frameTitle) {
    super(new InteractivePanel());
    setTitle(frameTitle);
    plot.setShowGridLines(false);
    drawingPanel.addDrawable(plot);
    addMenuItems();
    setAnimated(true);
    setAutoclear(true);
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
    // add phase legend to tool menu
    JMenuItem tableItem = new JMenuItem(DisplayRes.getString("GUIUtils.Legend")); //$NON-NLS-1$
    ActionListener tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        plot.showLegend();
      }

    };
    tableItem.addActionListener(tableListener);
    menu.add(tableItem);
    menu.addSeparator();
    // a grid data table
    tableItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
    tableItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_SHORTCUT_KEY_MASK));
    ActionListener actionListener = new ActionListener() {
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
      drawingPanel.getPopupMenu().add(item);
    }
  }

  /**
   * Clears drawable objects added by the user from this frame.
   */
  public void clearDrawables() {
    drawingPanel.clear(); // removes all drawables
    drawingPanel.addDrawable(plot);
  }

  /**
   * Gets Drawable objects added by the user to this frame.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    ArrayList<Drawable> list = super.getDrawables();
    list.remove(plot);
    return list;
  }

  /**
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordiante
   */
  public double indexToX(int i) {
    if(gridData==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return gridData.indexToX(i);
  }

  /*
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordiante
   */
  public double indexToY(int i) {
    if(gridData==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return gridData.indexToY(i);
  }

  /**
   * Gets the index that is closest to the given x value
   *
   * @return double the x coordiante
   */
  public int xToIndex(double x) {
    if(gridData==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return gridData.xToIndex(x);
  }

  /**
   * Gets the index that is closest to the given y value
   *
   * @return double the y coordiante
   */
  public int yToIndex(double y) {
    if(gridData==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return gridData.yToIndex(y);
  }

  /**
   * Gets the number of x entries.
   * @return nx
   */
  public int getNx() {
    if(gridData==null) {
      return 0;
    }
    return gridData.getNx();
  }

  /**
   * Gets the number of y entries.
   * @return nx
   */
  public int getNy() {
    if(gridData==null) {
      return 0;
    }
    return gridData.getNy();
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
    list.remove(plot);
    return list;
  }

  /**
   * Sets the scalar field to zero.
   */
  public void clearData() {
    if(gridData!=null) {
      setAll(new double[2][gridData.getNx()][gridData.getNy()]);
    }
    drawingPanel.invalidateImage();
  }

  /**
   * Resizes the number of columns and rows in the vector plot.
   *
   * @param nx int
   * @param ny int
   */
  public void resizeGrid(int nx, int ny) {
    double xmin, xmax, ymin, ymax;
    boolean cellScale = false;
    if(gridData==null) {
      xmin = drawingPanel.getPreferredXMin();
      xmax = drawingPanel.getPreferredXMax();
      ymin = drawingPanel.getPreferredYMin();
      ymax = drawingPanel.getPreferredYMax();
    } else {
      xmin = gridData.getLeft();
      xmax = gridData.getRight();
      ymin = gridData.getBottom();
      ymax = gridData.getTop();
      cellScale = gridData.isCellData();
    }
    gridData = new ArrayData(nx, ny, 3); // a grid with three data components
    gridData.setComponentName(0, "magnitude");   //$NON-NLS-1$
    gridData.setComponentName(1, "x component"); //$NON-NLS-1$
    gridData.setComponentName(2, "y component"); //$NON-NLS-1$
    if(cellScale) {
      gridData.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      gridData.setScale(xmin, xmax, ymin, ymax);
    }
    plot.setGridData(gridData);
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /**
   * Sets the data in the given row to new values.
   *
   * @param row  int
   * @param vals double[] new values
   * @throws IllegalArgumentException if array length does not match grid size.
   */
  public void setRow(int row, double[][] vals) throws IllegalArgumentException {
    if(gridData.getNx()!=vals.length) {
      throw new IllegalArgumentException("Row data length does not match grid size."); //$NON-NLS-1$
    }
    double[] re = gridData.getData()[1][row];
    double[] im = gridData.getData()[2][row];
    double[] phase = gridData.getData()[0][row];
    System.arraycopy(vals[0], 0, re, 0, vals.length);
    System.arraycopy(vals[1], 0, im, 0, vals.length);
    for(int j = 0, ny = phase.length; j<ny; j++) {
      phase[j] = Math.atan2(re[j], im[j]);
    }
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
  }

  /**
   * Sets the vector field's values and scale..
   *
   * @param vals int[][][] the new values
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void setAll(double[][][] vals, double xmin, double xmax, double ymin, double ymax) {
    setAll(vals);
    if(gridData.isCellData()) {
      gridData.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      gridData.setScale(xmin, xmax, ymin, ymax);
    }
  }

  /**
   * Sets the vector field's values.
   *
   * Values are stored in an array with dimension val[2][n][n].
   *
   * @param vals double[][][] new values
   */
  public void setAll(double[][][] vals) {
    if((gridData==null)||(gridData.getNx()!=vals.length)||(gridData.getNy()!=vals[0].length)) {
      resizeGrid(vals[0].length, vals[0][0].length);
    }
    double[][] colorValue = gridData.getData()[0];
    double[][] xComp = gridData.getData()[1];
    double[][] yComp = gridData.getData()[2];
    int ny = vals[0][0].length;
    for(int i = 0, nx = vals[0].length; i<nx; i++) {
      for(int j = 0; j<ny; j++) {
        // map vector magniture to color
        colorValue[i][j] = Math.sqrt(vals[0][i][j]*vals[0][i][j]+vals[1][i][j]*vals[1][i][j]);
        // normalize vector lengths
        xComp[i][j] = (colorValue[i][j]==0) ? 0 : vals[0][i][j]/colorValue[i][j];
        yComp[i][j] = (colorValue[i][j]==0) ? 0 : vals[1][i][j]/colorValue[i][j];
      }
    }
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
    drawingPanel.invalidateImage();
  }

  /**
   * Sets the autoscale flag and the floor and ceiling values for the colors.
   *
   * If autoscaling is true, then the min and max values of z are span the colors.
   *
   * If autoscaling is false, then floor and ceiling values limit the colors.
   * Values below min map to the first color; values above max map to the last color.
   *
   * @param isAutoscale
   * @param floor
   * @param ceil
   */
  public void setZRange(boolean isAutoscale, double floor, double ceil) {
    plot.setAutoscaleZ(isAutoscale, floor, ceil);
  }

  /**
   * Shows or hides the data table.
   *
   * @param show boolean
   */
  public synchronized void showDataTable(boolean show) {
    if(show) {
      if((tableFrame==null)||!tableFrame.isDisplayable()) {
        if(gridData==null) {
          return;
        }
        tableFrame = new GridTableFrame(gridData);
        tableFrame.setTitle(DisplayRes.getString("Vector2DFrame.Title")); //$NON-NLS-1$
        tableFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      }
      tableFrame.refreshTable();
      tableFrame.setVisible(true);
    } else {
      tableFrame.setVisible(false);
      tableFrame.dispose();
      tableFrame = null;
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
