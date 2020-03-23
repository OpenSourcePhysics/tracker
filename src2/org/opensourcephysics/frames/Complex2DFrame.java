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
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display2d.ArrayData;
import org.opensourcephysics.display2d.ComplexColorMapper;
import org.opensourcephysics.display2d.ComplexGridPlot;
import org.opensourcephysics.display2d.ComplexInterpolatedPlot;
import org.opensourcephysics.display2d.ComplexSurfacePlot;
import org.opensourcephysics.display2d.GridData;
import org.opensourcephysics.display2d.GridTableFrame;
import org.opensourcephysics.display2d.Plot2D;
import org.opensourcephysics.display2d.SurfacePlotMouseController;

/**
 * A DrawingFrame that displays 2D plots of complex scalar fields.
 *
 * @author W. Christian
 * @version 1.0
 */
public class Complex2DFrame extends DrawingFrame {
  GridData gridData;
  boolean showGrid = true;
  Plot2D plot = new ComplexGridPlot(null);
  SurfacePlotMouseController surfacePlotMC;
  JMenuItem surfaceItem, gridItem, interpolatedItem;
  GridTableFrame tableFrame;

  /**
   * Constructs a Complex2DFrame with the given axes labels and frame title.
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public Complex2DFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, null));
    drawingPanel.setPreferredSize(new Dimension(350, 350));
    setTitle(frameTitle);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorXGrid(false);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorYGrid(false);
    drawingPanel.addDrawable(plot);
    addMenuItems();
    setAnimated(true);
    setAutoclear(true);
    //gutters = drawingPanel.getGutters();
  }

  /**
   * Sets the autoscale flag and the floor and ceiling values for the intensity.
   *
   * @param isAutoscale
   * @param floor
   * @param ceil
   */
  public void setAutoscaleZ(boolean isAutoscale, double floor, double ceil) {
    plot.setAutoscaleZ(isAutoscale, floor, ceil);
  }

  /**
   * Sets the buffered image option.
   *
   * Buffered panels copy the offscreen image into the panel during a repaint unless the image
   * has been invalidated.  Use the render() method to draw the image immediately.
   *
   * @param b
    */
  public void setBuffered(boolean b) {
    drawingPanel.setBuffered(b);
  }

  /**
   * Outlines the data grid's boundaries.
   *
   * @param showGrid
   */
  public void setShowGrid(boolean show) {
    showGrid = show;
    plot.setShowGridLines(show);
  }

  /**
   * True if the data grid's boundaries are shown.
   *
   * @return showGrid
   */
  public boolean isShowGrid() {
    return showGrid;
  }

  /**
   * Constructs a Complex2DFrame with the given frame title but without axes.
   * @param frameTitle String
   */
  public Complex2DFrame(String frameTitle) {
    super(new InteractivePanel());
    setTitle(frameTitle);
    drawingPanel.addDrawable(plot);
    addMenuItems();
    setAnimated(true);
    setAutoclear(true);
    //gutters = drawingPanel.getGutters();
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
    ButtonGroup menubarGroup = new ButtonGroup();
    // grid plot menu item
    gridItem = new JRadioButtonMenuItem(DisplayRes.getString("2DFrame.MenuItem.GridPlot")); //$NON-NLS-1$
    menubarGroup.add(gridItem);
    gridItem.setSelected(true);
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToGridPlot();
      }

    };
    gridItem.addActionListener(actionListener);
    menu.add(gridItem);
    // surface plot menu item
    surfaceItem = new JRadioButtonMenuItem(DisplayRes.getString("2DFrame.MenuItem.SurfacePlot")); //$NON-NLS-1$
    menubarGroup.add(surfaceItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToSurfacePlot();
      }

    };
    surfaceItem.addActionListener(actionListener);
    menu.add(surfaceItem);
    // interpolated plot menu item
    interpolatedItem = new JRadioButtonMenuItem(DisplayRes.getString("2DFrame.MenuItem.InterpolatedPlot")); //$NON-NLS-1$
    menubarGroup.add(interpolatedItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToInterpolatedPlot();
      }

    };
    interpolatedItem.addActionListener(actionListener);
    menu.add(interpolatedItem);
    // add phase legend to tool menu
    menu.addSeparator();
    JMenuItem legendItem = new JMenuItem(DisplayRes.getString("GUIUtils.PhaseLegend")); //$NON-NLS-1$
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ComplexColorMapper.showPhaseLegend();
      }

    };
    legendItem.addActionListener(actionListener);
    menu.add(legendItem);
    // a grid data table
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
      drawingPanel.getPopupMenu().add(item);
    }
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
   * Removes drawable objects added by the user from this frame.
   */
  public void clearDrawables() {
    drawingPanel.clear(); // removes all drawables
    drawingPanel.addDrawable(plot);
  }

  /**
   * Clears data by setting the scalar field to zero.
   */
  public void clearData() {
    if(gridData!=null) {
      setAll(new double[2][gridData.getNx()][gridData.getNy()]);
    }
    if(drawingPanel!=null) {
      drawingPanel.invalidateImage();
    }
  }

  /*
   * Converts to an InterpolatedPlot plot.
   */
  public void convertToInterpolatedPlot() {
    if(!(plot instanceof ComplexInterpolatedPlot)) {
      if(surfacePlotMC!=null) {
        drawingPanel.removeMouseListener(surfacePlotMC);
        drawingPanel.removeMouseMotionListener(surfacePlotMC);
        surfacePlotMC = null;
        //drawingPanel.setGutters(gutters);
        drawingPanel.resetGutters();
        drawingPanel.setClipAtGutter(true);
        if(drawingPanel instanceof PlottingPanel) {
          ((PlottingPanel) drawingPanel).getAxes().setVisible(true);
        }
        drawingPanel.setShowCoordinates(true);
      }
      drawingPanel.removeDrawable(plot);
      plot = new ComplexInterpolatedPlot(gridData);
      drawingPanel.addDrawable(plot);
      drawingPanel.invalidateImage();
      drawingPanel.repaint();
      interpolatedItem.setSelected(true);
    }
  }

  /*
   * Converts to a GridPlot plot.
   */
  public void convertToGridPlot() {
    if(!(plot instanceof ComplexGridPlot)) {
      if(surfacePlotMC!=null) {
        drawingPanel.removeMouseListener(surfacePlotMC);
        drawingPanel.removeMouseMotionListener(surfacePlotMC);
        surfacePlotMC = null;
        drawingPanel.resetGutters();
        drawingPanel.setClipAtGutter(true);
        //drawingPanel.setGutters(gutters);
        if(drawingPanel instanceof PlottingPanel) {
          ((PlottingPanel) drawingPanel).getAxes().setVisible(true);
        }
        drawingPanel.setShowCoordinates(true);
      }
      drawingPanel.removeDrawable(plot);
      plot = new ComplexGridPlot(gridData);
      plot.setShowGridLines(showGrid);
      drawingPanel.addDrawable(plot);
      drawingPanel.invalidateImage();
      drawingPanel.repaint();
      gridItem.setSelected(true);
    }
  }

  /**
 * Converts to a SurfacePlot plot.
 */
  public void convertToSurfacePlot() {
    if(!(plot instanceof ComplexSurfacePlot)) {
      Plot2D oldPlot = plot;
      try {
        Plot2D newPlot = new ComplexSurfacePlot(gridData);
        plot = newPlot;
      } catch(IllegalArgumentException ex) {
        surfaceItem.setEnabled(false);
        gridItem.setSelected(true);
        convertToGridPlot();
        return;
      }
      //gutters = drawingPanel.getGutters();
      if(drawingPanel instanceof PlottingPanel) {
        ((PlottingPanel) drawingPanel).getAxes().setVisible(false);
      }
      drawingPanel.setShowCoordinates(false);
      drawingPanel.setGutters(0, 0, 0, 0);
      drawingPanel.setClipAtGutter(false);
      boolean isAutoscaleZ = oldPlot.isAutoscaleZ();
      double floor = oldPlot.getFloor();
      double ceil = oldPlot.getCeiling();
      plot.setAutoscaleZ(isAutoscaleZ, floor, ceil);
      drawingPanel.replaceDrawable(oldPlot, plot);
      plot.update();
      if((tableFrame!=null)&&tableFrame.isShowing()) {
        tableFrame.refreshTable();
      }
      drawingPanel.repaint();
      if(surfacePlotMC==null) {
        surfacePlotMC = new SurfacePlotMouseController(drawingPanel, plot);
      }
      drawingPanel.addMouseListener(surfacePlotMC);
      drawingPanel.addMouseMotionListener(surfacePlotMC);
      surfaceItem.setSelected(true);
      drawingPanel.invalidateImage();
      drawingPanel.repaint();
    }
  }

  /**
   * Resizes the grid used to store the field using the panel's preferred min/max values.
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
    gridData.setComponentName(0, DisplayRes.getString("Complex2DFrame.GridData.Magnitude")); //$NON-NLS-1$
    gridData.setComponentName(1, DisplayRes.getString("Complex2DFrame.GridData.Real"));      //$NON-NLS-1$
    gridData.setComponentName(2, DisplayRes.getString("Complex2DFrame.GridData.Imaginary")); //$NON-NLS-1$
    if(cellScale) {
      gridData.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      gridData.setScale(xmin, xmax, ymin, ymax);
    }
    if(nx!=ny) {
      surfaceItem.setEnabled(false);
      if(plot instanceof ComplexSurfacePlot) {
        convertToGridPlot();
      }
    } else {
      surfaceItem.setEnabled(true);
    }
    plot.setGridData(gridData);
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /*
   * Sets the data in the given row.
   *
   * vals[0][] is assumed to contain the real components of the row.
   * vals[1][] is assumed to contain the imaginary components of the row.
   *
   *
   * @param row  int the index for this row
   * @param vals double[][] complex field values
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
    drawingPanel.invalidateImage();
  }

  /**
   * Sets the complex field's values and scale..
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
   * Sets the complex field's values.
   *
   * vals[0][][] is assumed to contain the real components of the field.
   * vals[1][][] is assumed to contain the imaginary components of the field.
   *
   * @param vals double[][][] complex field values
   */
  public void setAll(double[][][] vals) {
    resizeGrid(vals[0].length, vals[0][0].length);
    double[][] mag = gridData.getData()[0];
    double[][] reData = gridData.getData()[1];
    double[][] imData = gridData.getData()[2];
    // current grid has correct size
    int ny = vals[0][0].length;
    for(int i = 0, nx = vals[0].length; i<nx; i++) {
      System.arraycopy(vals[0][i], 0, reData[i], 0, ny);
      System.arraycopy(vals[1][i], 0, imData[i], 0, ny);
      for(int j = 0; j<ny; j++) {
        mag[i][j] = Math.sqrt(vals[0][i][j]*vals[0][i][j]+vals[1][i][j]*vals[1][i][j]);
      }
    }
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
    drawingPanel.invalidateImage();
  }

  /**
   * Sets the comples field's data and scale.
   *
   * The array is assumed to contain complex numbers in row-major format.
   *
   * @param vals int[][][] the new values
   * @param nx
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void setAll(double[] vals, int nx, double xmin, double xmax, double ymin, double ymax) {
    if((vals.length/2)%nx!=0) {
      throw new IllegalArgumentException("Number of values in grid (nx*ny) must match number of values."); //$NON-NLS-1$
    }
    resizeGrid(nx, vals.length/nx);
    setAll(vals);
    if(gridData.isCellData()) {
      gridData.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      gridData.setScale(xmin, xmax, ymin, ymax);
    }
  }

  /**
   * Sets the comples field's data using the given array.
   *
   * The array is assumed to contain complex numbers in row-major format.
   *
   * @param vals double[] complex field values
   */
  public void setAll(double[] vals) {
    if(gridData==null) {
      throw new IllegalArgumentException("Grid size must be set before using row-major format."); //$NON-NLS-1$
    }
    int nx = gridData.getNx(), ny = gridData.getNy();
    if(vals.length!=2*nx*ny) {
      throw new IllegalArgumentException("Grid does not have the correct size."); //$NON-NLS-1$
    }
    double[][] mag = gridData.getData()[0]; // magnitude maps to color
    double[][] reData = gridData.getData()[1];
    double[][] imData = gridData.getData()[2];
    for(int j = 0; j<ny; j++) {
      int offset = 2*j*nx;
      for(int i = 0; i<nx; i++) {
        double re = vals[offset+2*i];
        double im = vals[offset+2*i+1];
        mag[i][j] = Math.sqrt(re*re+im*im);
        reData[i][j] = re;
        imData[i][j] = im;
      }
    }
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
    drawingPanel.invalidateImage();
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
        tableFrame.setTitle(DisplayRes.getString("Complex2DFrame.TableFrame.Title")); //$NON-NLS-1$
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
