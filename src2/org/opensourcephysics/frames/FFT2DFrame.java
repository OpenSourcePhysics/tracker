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
import org.opensourcephysics.display2d.Plot2D;
import org.opensourcephysics.display2d.SurfacePlotMouseController;
import org.opensourcephysics.numerics.FFT2D;

/**
 * FFT2DFrame computes a 2D FFT and displays the result as a complex grid plot.
 *
 * @author W. Christian
 * @version 1.0
 */
public class FFT2DFrame extends DrawingFrame {
  static final double PI2 = Math.PI*2;
  public static final int MODE = 0;
  public static final int FREQ = 1;
  public static final int OMEGA = 2;
  public static final int WAVENUMBER = 3;
  public static final int MOMENTUM = 4;
  protected int domainType = WAVENUMBER;
  GridData gridData;
  FFT2D fft;
  double[] fftData;
  Plot2D plot = new ComplexGridPlot(null);
  // Plot2D plot = new ComplexInterpolatedPlot(null);

  SurfacePlotMouseController surfacePlotMC;
  JMenuItem gridItem, interpolatedItem, surfaceItem;

  /**
   * Constructs a Complex2DFrame with the given axes labels and frame title.
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public FFT2DFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, null));
    drawingPanel.setPreferredSize(new Dimension(350, 350));
    setTitle(frameTitle);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorXGrid(false);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorYGrid(false);
    drawingPanel.addDrawable(plot);
    addMenuItems();
    setAnimated(true);
    setAutoclear(true);
  }

  /**
   * Constructs a Complex2DFrame with the given frame title but without axes.
   * @param frameTitle String
   */
  public FFT2DFrame(String frameTitle) {
    super(new InteractivePanel());
    setTitle(frameTitle);
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
    ButtonGroup menubarGroup = new ButtonGroup();
    // grid plot menu item
    gridItem = new JRadioButtonMenuItem(DisplayRes.getString("Scalar2DFrame.MenuItem.GridPlot")); //$NON-NLS-1$
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
    surfaceItem = new JRadioButtonMenuItem(DisplayRes.getString("Scalar2DFrame.MenuItem.SurfacePlot")); //$NON-NLS-1$
    menubarGroup.add(surfaceItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToSurfacePlot();
      }

    };
    surfaceItem.addActionListener(actionListener);
    menu.add(surfaceItem);
    // interpolated plot menu item
    interpolatedItem = new JRadioButtonMenuItem(DisplayRes.getString("Scalar2DFrame.MenuItem.InterpolatedPlot")); //$NON-NLS-1$
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
    JMenuItem phaseItem = new JMenuItem(DisplayRes.getString("GUIUtils.PhaseLegend")); //$NON-NLS-1$
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ComplexColorMapper.showPhaseLegend();
      }

    };
    phaseItem.addActionListener(actionListener);
    menu.add(phaseItem);
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
      }
      drawingPanel.removeDrawable(plot);
      plot = new ComplexInterpolatedPlot(gridData);
      drawingPanel.addDrawable(plot);
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
      }
      drawingPanel.removeDrawable(plot);
      plot = new ComplexGridPlot(gridData);
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
      drawingPanel.removeDrawable(plot);
      plot = new ComplexSurfacePlot(gridData);
      drawingPanel.addDrawable(plot);
      drawingPanel.invalidateImage();
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
   * Resizes the grid used to store the fft using the given  spacial min/max values.
   *
   * @param nx int
   * @param ny int
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  private void resizeGrid(int nx, int ny) {
    fftData = new double[2*nx*ny];
    fft = new FFT2D(nx, ny);
    gridData = new ArrayData(nx, ny, 3); // a grid with three data components
    plot.setGridData(gridData);
    plot.update();
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /**
   * Sets the units for the FFT output.
   * Domain types are:  MODE, FREQ, OMEGA, WAVENUMBER, MOMENTUM
   *
   * @param type int
   */
  public void setDomainType(int type) {
    domainType = type;
    switch(domainType) {
       case MODE :
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel("x mode");      //$NON-NLS-1$
         }
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setYLabel("y mode");      //$NON-NLS-1$
         }
         break;
       case FREQ :
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel("x frequency"); //$NON-NLS-1$
         }
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setYLabel("y frequency"); //$NON-NLS-1$
         }
         break;
       case OMEGA :
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel("x omega");     //$NON-NLS-1$
         }
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setYLabel("y omega");     //$NON-NLS-1$
         }
         break;
       case WAVENUMBER :
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel("k_x");         //$NON-NLS-1$
         }
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setYLabel("k_y");         //$NON-NLS-1$
         }
         break;
       case MOMENTUM :
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel("p_x");         //$NON-NLS-1$
         }
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setYLabel("p_y");         //$NON-NLS-1$
         }
         break;
    }
  }

  /**
   * Does an FFT of the given data array and repaints the panel.
   *
   * data[0][][] is assumed to contain the real components of the field.
   * data[1][][] is assumed to contain the imaginary components of the field.
   *
   * @param data double[][][] complex field values
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void doFFT(double[][][] data, double xmin, double xmax, double ymin, double ymax) {
    if(gridData==null) {
      throw new IllegalStateException("Grid must be set before using row-major format."); //$NON-NLS-1$
    }
    int nx = gridData.getNx(), ny = gridData.getNy();
    if((data[0].length!=nx)||(data[0][0].length!=ny)) {
      throw new IllegalArgumentException("Grid does not have the correct size."); //$NON-NLS-1$
    }
    double[][] reData = data[0];
    double[][] imData = data[1];
    int offX = (int) (nx*xmin/(xmax-xmin));
    offX = Math.abs(offX);
    int offY = (int) (ny*ymin/(ymax-ymin));
    offY = Math.abs(offY);
    for(int i = 0; i<nx; i++) {
      int ii = (offX+i)%nx;
      int offset = 2*ii*nx;
      for(int j = 0; j<ny; j++) {
        int jj = (offX+j)%ny;
        fftData[offset+2*jj] = reData[i][j];
        fftData[offset+2*jj+1] = imData[i][j];
      }
    }
    fft.transform(fftData);
    fft.toNaturalOrder(fftData);
    // double[] fx= fft.getNaturalOmegaX(xmin,xmax);
    // double[] fy= fft.getNaturalOmegaY(ymin,ymax);
    // gridData.setCellScale(fx[0], fx[nx-1],fy[0], fy[ny-1] );
    double a1 = -nx/2, a2 = (nx+1)/2-1, b1 = -ny/2, b2 = (ny+1)/2-1;
    switch(domainType) {
       case MODE :
         break;
       case FREQ :
         a2 = fft.getFreqMax(xmin, xmax, nx);
         a1 = fft.getFreqMin(xmin, xmax, nx);
         b2 = fft.getFreqMax(ymin, ymax, ny);
         b1 = fft.getFreqMin(ymin, ymax, ny);
         break;
       case OMEGA :
       case MOMENTUM :
       case WAVENUMBER :
         a2 = PI2*fft.getFreqMax(xmin, xmax, nx);
         a1 = PI2*fft.getFreqMin(xmin, xmax, nx);
         b2 = PI2*fft.getFreqMax(ymin, ymax, ny);
         b1 = PI2*fft.getFreqMin(ymin, ymax, ny);
         break;
    }
    gridData.setCenteredCellScale(a1, a2, b2, b1);
    fillGrid(nx, ny, fftData);
    plot.update();
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /*
   * Does an FFT of the given data array and repaints the panel.
   *
   * The data array is assumed to contain complex numbers in row-major format.
   * The given array remains unchanged.
   *
   * @param nx int
   * @param data double[]
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void doFFT(double[] data, int nx, double xmin, double xmax, double ymin, double ymax) throws IllegalArgumentException {
    if((data.length/2)%nx!=0) {
      throw new IllegalArgumentException("Number of values in grid (nx*ny) must match number of values."); //$NON-NLS-1$
    }
    int ny = data.length/nx/2;
    resizeGrid(nx, ny);
    // int nx = gridData.getNx(), ny = gridData.getNy();
    if(data.length!=2*nx*ny) {
      throw new IllegalArgumentException("Grid does not have the correct size."); //$NON-NLS-1$
    }
    int offX = (int) (nx*xmin/(xmax-xmin));
    offX = Math.abs(offX);
    int offY = (int) (ny*ymin/(ymax-ymin));
    offY = Math.abs(offY);
    for(int j = 0; j<ny; j++) {
      int jj = (offY+j)%ny;
      int offset = 2*j*nx;
      int offset2 = 2*jj*nx;
      for(int i = 0; i<nx; i++) {
        int ii = (offX+i)%nx;
        fftData[offset+2*ii] = data[offset2+2*i];
        fftData[offset+2*ii+1] = data[offset2+2*i+1];
      }
    }
    fft.transform(fftData);
    fft.toNaturalOrder(fftData);
    // double[] fx= fft.getNaturalModes(nx);
    // double[] fy= fft.getNaturalModes(ny);
    // gridData.setCellScale(fx[0], fx[nx-1], fy[ny-1],fy[0] );
    double a1 = -nx/2, a2 = (nx+1)/2-1, b1 = -ny/2, b2 = (ny+1)/2-1;
    switch(domainType) {
       case MODE :
         break;
       case FREQ :
         a2 = fft.getFreqMax(xmin, xmax, nx);
         a1 = fft.getFreqMin(xmin, xmax, nx);
         b2 = fft.getFreqMax(ymin, ymax, ny);
         b1 = fft.getFreqMin(ymin, ymax, ny);
         break;
       case OMEGA :
       case MOMENTUM :
       case WAVENUMBER :
         a2 = PI2*fft.getFreqMax(xmin, xmax, nx);
         a1 = PI2*fft.getFreqMin(xmin, xmax, nx);
         b2 = PI2*fft.getFreqMax(ymin, ymax, ny);
         b1 = PI2*fft.getFreqMin(ymin, ymax, ny);
         break;
    }
    gridData.setCenteredCellScale(a1, a2, b2, b1);
    fillGrid(nx, ny, fftData);
    plot.update();
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /**
   * Removes drawable objects added by the user from this frame.
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

  private void fillGrid(int nx, int ny, double[] vals) {
    double[][] mag = gridData.getData()[0]; // magnitude maps to intensity
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
