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
import org.opensourcephysics.display2d.ColorMapper;
import org.opensourcephysics.display2d.ContourPlot;
import org.opensourcephysics.display2d.GrayscalePlot;
import org.opensourcephysics.display2d.GridData;
import org.opensourcephysics.display2d.GridPlot;
import org.opensourcephysics.display2d.GridTableFrame;
import org.opensourcephysics.display2d.InterpolatedPlot;
import org.opensourcephysics.display2d.Plot2D;
import org.opensourcephysics.display2d.SurfacePlot;
import org.opensourcephysics.display2d.SurfacePlotMouseController;

/**
 * A DrawingFrame that displays 2D plots of scalar fields.
 *
 * @author W. Christian
 * @version 1.0
 */
public class Scalar2DFrame extends DrawingFrame {
  String plotType = ""; //$NON-NLS-1$
  int paletteType = ColorMapper.SPECTRUM;
  boolean expanded = false;
  double expansionFactor = 1.0;
  boolean showGrid = true;
  GridData gridData;
  Plot2D plot = new GridPlot(null);
  SurfacePlotMouseController surfacePlotMC;
  JMenuItem surfaceItem, contourItem, gridItem, interpolatedItem, grayscaleItem;
  GridTableFrame tableFrame;
  //int[] gutters;

  /**
   * Constructs a Scalar2DFrame with the given axes labels and frame title.
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public Scalar2DFrame(String xlabel, String ylabel, String frameTitle) {
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
   * Constructs a Scalar2DFrame with the given frame title but without axes.
   * @param frameTitle String
   */
  public Scalar2DFrame(String frameTitle) {
    super(new InteractivePanel());
    setTitle(frameTitle);
    drawingPanel.addDrawable(plot);
    addMenuItems();
    setAnimated(true);
    setAutoclear(true);
    //gutters = drawingPanel.getGutters();
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
   * Gets the maximum z value of the plot.
   * @return zmax
   */
  public double getCeiling() {
    return plot.getCeiling();
  }

  /**
   * Gets the minimum z value of the plot.
   * @return zmin
   */
  public double getFloor() {
    return plot.getFloor();
  }

  /**
   * Gets the autoscale flag for z.
   *
   * @return boolean
  */
  public boolean isAutoscaleZ() {
    return plot.isAutoscaleZ();
  }

  /**
   * Expands the z scale so as to enhance values close to zero.
   *
   * @param expanded boolean
   * @param expansionFactor double
   */
  public void setExpandedZ(boolean expanded, double expansionFactor) {
    this.expansionFactor = expansionFactor;
    this.expanded = expanded;
    plot.setExpandedZ(expanded, expansionFactor);
  }

  /**
   * Sets the color palette that will be used to color the scalar field.  Palette types are defined in ColorMapper.
   * @param type
   */
  public void setPaletteType(int type) {
    paletteType = type;
    plot.setPaletteType(type);
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
    ActionListener tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToGridPlot();
      }

    };
    gridItem.addActionListener(tableListener);
    menu.add(gridItem);
    // contour plot menu item
    contourItem = new JRadioButtonMenuItem(DisplayRes.getString("2DFrame.MenuItem.ContourPlot")); //$NON-NLS-1$
    menubarGroup.add(contourItem);
    tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToContourPlot();
      }

    };
    contourItem.addActionListener(tableListener);
    menu.add(contourItem);
    // surface plot menu item
    surfaceItem = new JRadioButtonMenuItem(DisplayRes.getString("2DFrame.MenuItem.SurfacePlot")); //$NON-NLS-1$
    menubarGroup.add(surfaceItem);
    tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToSurfacePlot();
      }

    };
    surfaceItem.addActionListener(tableListener);
    menu.add(surfaceItem);
    // interpolated plot menu item
    interpolatedItem = new JRadioButtonMenuItem(DisplayRes.getString("2DFrame.MenuItem.InterpolatedPlot")); //$NON-NLS-1$
    menubarGroup.add(interpolatedItem);
    tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToInterpolatedPlot();
      }

    };
    interpolatedItem.addActionListener(tableListener);
    menu.add(interpolatedItem);
    // grayscale plot menu item
    grayscaleItem = new JRadioButtonMenuItem(DisplayRes.getString("2DFrame.MenuItem.GrayscalePlot")); //$NON-NLS-1$
    menubarGroup.add(grayscaleItem);
    tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToGrayscalePlot();
      }

    };
    grayscaleItem.addActionListener(tableListener);
    menu.add(grayscaleItem);
    menu.addSeparator();
    // a grid data table
    JMenuItem tableItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
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
      setAll(new double[gridData.getNx()][gridData.getNy()]);
    }
    drawingPanel.invalidateImage();
  }

  /**
   * Sets the type of plot type so that it cannot be changed from the menu.
   * @param type String
   */
  public void setPlotType(String type) {
    plotType = type;
    if(type.toLowerCase().equals("contour")) {             //$NON-NLS-1$
      convertToContourPlot();
      surfaceItem.setEnabled(false);
      contourItem.setEnabled(true);
      gridItem.setEnabled(false);
      interpolatedItem.setEnabled(false);
      grayscaleItem.setEnabled(false);
    } else if(type.toLowerCase().equals("grayscale")) {    //$NON-NLS-1$
      convertToGrayscalePlot();
      surfaceItem.setEnabled(false);
      contourItem.setEnabled(false);
      gridItem.setEnabled(false);
      interpolatedItem.setEnabled(false);
      grayscaleItem.setEnabled(true);
    } else if(type.toLowerCase().equals("grid")) {         //$NON-NLS-1$
      convertToGridPlot();
      surfaceItem.setEnabled(false);
      contourItem.setEnabled(false);
      gridItem.setEnabled(true);
      interpolatedItem.setEnabled(false);
      grayscaleItem.setEnabled(false);
    } else if(type.toLowerCase().equals("interpolated")) { //$NON-NLS-1$
      convertToInterpolatedPlot();
      surfaceItem.setEnabled(false);
      contourItem.setEnabled(false);
      gridItem.setEnabled(false);
      interpolatedItem.setEnabled(true);
      grayscaleItem.setEnabled(false);
    } else if(type.toLowerCase().equals("surface")) {      //$NON-NLS-1$
      convertToSurfacePlot();
      surfaceItem.setEnabled(true);
      contourItem.setEnabled(false);
      gridItem.setEnabled(false);
      interpolatedItem.setEnabled(false);
      grayscaleItem.setEnabled(false);
    } else {
      plotType = "";                                       //$NON-NLS-1$
      surfaceItem.setEnabled(true);
      contourItem.setEnabled(true);
      gridItem.setEnabled(true);
      interpolatedItem.setEnabled(true);
      grayscaleItem.setEnabled(true);
    }
  }

  /**
   * Converts to a contour plot.
   */
  public void convertToContourPlot() {
    if(!(plot instanceof ContourPlot)) {
      if(surfacePlotMC!=null) {
        drawingPanel.removeMouseListener(surfacePlotMC);
        drawingPanel.removeMouseMotionListener(surfacePlotMC);
        surfacePlotMC = null;
        // drawingPanel.setGutters(gutters);
        drawingPanel.resetGutters();
        drawingPanel.setClipAtGutter(true);
        if(drawingPanel instanceof PlottingPanel) {
          ((PlottingPanel) drawingPanel).getAxes().setVisible(true);
        }
        drawingPanel.setShowCoordinates(true);
      }
      boolean isAutoscaleZ = plot.isAutoscaleZ();
      double floor = plot.getFloor();
      double ceil = plot.getCeiling();
      Plot2D oldPlot = plot;
      plot = new ContourPlot(gridData);
      plot.setPaletteType(paletteType);
      if(expanded) {
        plot.setExpandedZ(expanded, expansionFactor);
      }
      plot.setAutoscaleZ(isAutoscaleZ, floor, ceil);
      drawingPanel.replaceDrawable(oldPlot, plot);
      plot.update();
      if((tableFrame!=null)&&tableFrame.isShowing()) {
        tableFrame.refreshTable();
      }
      drawingPanel.repaint();
      contourItem.setSelected(true);
    }
  }

  /*
   * Converts to an InterpolatedPlot plot.
   */
  public void convertToInterpolatedPlot() {
    if(!(plot instanceof InterpolatedPlot)) {
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
      boolean isAutoscaleZ = plot.isAutoscaleZ();
      double floor = plot.getFloor();
      double ceil = plot.getCeiling();
      Plot2D oldPlot = plot;
      plot = new InterpolatedPlot(gridData);
      plot.setPaletteType(paletteType);
      if(expanded) {
        plot.setExpandedZ(expanded, expansionFactor);
      }
      plot.setAutoscaleZ(isAutoscaleZ, floor, ceil);
      drawingPanel.replaceDrawable(oldPlot, plot);
      plot.update();
      if((tableFrame!=null)&&tableFrame.isShowing()) {
        tableFrame.refreshTable();
      }
      drawingPanel.repaint();
      interpolatedItem.setSelected(true);
    }
  }

  /*
   * Converts to a GridPlot plot.
   */
  public void convertToGridPlot() {
    if(!(plot instanceof GridPlot)) {
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
      boolean isAutoscaleZ = plot.isAutoscaleZ();
      double floor = plot.getFloor();
      double ceil = plot.getCeiling();
      Plot2D oldPlot = plot;
      plot = new GridPlot(gridData);
      if(expanded) {
        plot.setExpandedZ(expanded, expansionFactor);
      }
      plot.setShowGridLines(showGrid);
      plot.setPaletteType(paletteType);
      plot.setAutoscaleZ(isAutoscaleZ, floor, ceil);
      drawingPanel.replaceDrawable(oldPlot, plot);
      plot.update();
      if((tableFrame!=null)&&tableFrame.isShowing()) {
        tableFrame.refreshTable();
      }
      drawingPanel.repaint();
      gridItem.setSelected(true);
    }
  }

  /*
   * Converts to a GrayscalePlot plot.
   */
  public void convertToGrayscalePlot() {
    if(!(plot instanceof GrayscalePlot)) {
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
      boolean isAutoscaleZ = plot.isAutoscaleZ();
      double floor = plot.getFloor();
      double ceil = plot.getCeiling();
      Plot2D oldPlot = plot;
      plot = new GrayscalePlot(gridData);
      if(expanded) {
        plot.setExpandedZ(expanded, expansionFactor);
      }
      plot.setPaletteType(paletteType);
      plot.setAutoscaleZ(isAutoscaleZ, floor, ceil);
      drawingPanel.replaceDrawable(oldPlot, plot);
      plot.update();
      if((tableFrame!=null)&&tableFrame.isShowing()) {
        tableFrame.refreshTable();
      }
      drawingPanel.repaint();
      grayscaleItem.setSelected(true);
    }
  }

  /**
   * Converts to a SurfacePlot plot.
   */
  public void convertToSurfacePlot() {
    if(!(plot instanceof SurfacePlot)) {
      Plot2D oldPlot = plot;
      try {
        SurfacePlot newPlot = new SurfacePlot(gridData);
        if(drawingPanel instanceof PlottingPanel) {
          String xLabel = ((PlottingPanel) drawingPanel).getAxes().getXLabel();
          String yLabel = ((PlottingPanel) drawingPanel).getAxes().getYLabel();
          newPlot.setAxisLabels(xLabel, yLabel, null);
        }
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
      plot.setPaletteType(paletteType);
      if(expanded) {
        plot.setExpandedZ(expanded, expansionFactor);
      }
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
    }
  }

  /**
   * Resizes the number of columns and rows.
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
    gridData = new ArrayData(nx, ny, 1); // a grid with one data component
    gridData.setComponentName(0, "Amp"); //$NON-NLS-1$
    if(nx!=ny) {
      surfaceItem.setEnabled(false);
      if(plot instanceof SurfacePlot) {
        convertToGridPlot();
      }
    } else {
      if(plotType.equals("")) { //$NON-NLS-1$
        surfaceItem.setEnabled(true);
      }
    }
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
  public void setRow(int row, double[] vals) throws IllegalArgumentException {
    if(gridData.getNx()!=vals.length) {
      throw new IllegalArgumentException("Row data length does not match grid size."); //$NON-NLS-1$
    }
    double[] rowData = gridData.getData()[0][row];
    System.arraycopy(vals, 0, rowData, 0, vals.length);
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
    drawingPanel.invalidateImage();
  }

  /**
   * Sets the scalar field's values and scale..
   *
   * @param vals int[][] the new values
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void setAll(double[][] vals, double xmin, double xmax, double ymin, double ymax) {
    setAll(vals);
    if(gridData.isCellData()) {
      gridData.setCellScale(xmin, xmax, ymin, ymax);
    } else {
      gridData.setScale(xmin, xmax, ymin, ymax);
    }
  }

  /**
   * Sets the scalar field's values.
   *
   * @param vals double[][] new values
   */
  public void setAll(double[][] vals) {
    if((gridData==null)||(gridData.getNx()!=vals.length)||(gridData.getNy()!=vals[0].length)) {
      resizeGrid(vals.length, vals[0].length);
    }
    double[][] data = gridData.getData()[0];
    // current grid has correct size
    int ny = vals[0].length;
    for(int i = 0, nx = data.length; i<nx; i++) {
      System.arraycopy(vals[i], 0, data[i], 0, ny);
    }
    plot.update();
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      tableFrame.refreshTable();
    }
    drawingPanel.invalidateImage();
  }

  /**
   * Sets all the scalar field values using the given array.
   *
   * The array is assumed to contain numbers in row-major format.
   *
   * @param vals double[] field values
   */
  public void setAll(double[] vals) {
    if(gridData==null) {
      throw new IllegalArgumentException("Grid size must be set before using row-major format."); //$NON-NLS-1$
    }
    int nx = gridData.getNx(), ny = gridData.getNy();
    if(vals.length!=nx*ny) {
      throw new IllegalArgumentException("Grid does not have the correct size."); //$NON-NLS-1$
    }
    double[][] mag = gridData.getData()[0]; // magnitude
    for(int j = 0; j<ny; j++) {
      int offset = j*nx;
      for(int i = 0; i<nx; i++) {
        mag[i][j] = vals[offset+i];
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
        tableFrame.setTitle(DisplayRes.getString("Scalar2DFrame.Table.Title")); //$NON-NLS-1$
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
