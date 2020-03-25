/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
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
import org.opensourcephysics.display.ComplexDataset;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.DataTableFrame;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.numerics.FFT;
import org.opensourcephysics.numerics.Function;

/**
 * FFTFrame computes the FFT and displays the result using a ComplexDataset.
 *
 * @author W. Christian
 * @version 1.0
 */
public class FFTFrame extends DrawingFrame {
  public static final int MODE = 0;
  public static final int FREQ = 1;
  public static final int OMEGA = 2;
  public static final int WAVENUMBER = 3;
  public static final int MOMENTUM = 4;
  protected int domainType = FREQ;
  protected ComplexDataset complexDataset = new ComplexDataset();
  protected DataTable dataTable = new DataTable();
  protected DataTableFrame tableFrame;
  private double[] fftData = new double[1];
  private FFT fft = new FFT(1);
  JMenuItem ampPhaseItem, postItem, barItem;

  /**
   * A DrawingFrame that displays a FFT as its drawable.
   *
   * @param xlabel String
   * @param ylabel String
   * @param title String
   */
  public FFTFrame(String xlabel, String ylabel, String title) {
    super(new PlottingPanel(xlabel, ylabel, null));
    complexDataset.setMarkerShape(ComplexDataset.PHASE_POST);
    complexDataset.setXYColumnNames(xlabel, "re", "im"); //$NON-NLS-1$ //$NON-NLS-2$
    drawingPanel.addDrawable(complexDataset);
    setTitle(title);
    dataTable.add(complexDataset);
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
    // post view
    postItem = new JRadioButtonMenuItem(DisplayRes.getString("ComplexPlotFrame.MenuItem.PostView")); //$NON-NLS-1$
    menubarGroup.add(postItem);
    postItem.setSelected(true);
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToPostView();
      }

    };
    postItem.addActionListener(actionListener);
    menu.add(postItem);
    // bar view
    barItem = new JRadioButtonMenuItem(DisplayRes.getString("ComplexPlotFrame.MenuItem.BarView")); //$NON-NLS-1$
    menubarGroup.add(barItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToPhaseBarView();
      }

    };
    barItem.addActionListener(actionListener);
    menu.add(barItem);
    // amp and phase
    ampPhaseItem = new JRadioButtonMenuItem(DisplayRes.getString("ComplexPlotFrame.MenuItem.AmpPhase")); //$NON-NLS-1$
    menubarGroup.add(ampPhaseItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToAmpAndPhaseView();
      }

    };
    ampPhaseItem.addActionListener(actionListener);
    menu.add(ampPhaseItem);
    menu.addSeparator();
    JMenuItem tableItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
    tableItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_SHORTCUT_KEY_MASK));
    ActionListener tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showDataTable(true);
      }

    };
    tableItem.addActionListener(tableListener);
    menu.add(tableItem);
    JMenuItem item = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
    item.addActionListener(tableListener);
    if((drawingPanel!=null)&&(drawingPanel.getPopupMenu()!=null)) {
      drawingPanel.getPopupMenu().add(item);
    }
    // add phase legend to tool menu
    menu.addSeparator();
    tableItem = new JMenuItem(DisplayRes.getString("GUIUtils.PhaseLegend")); //$NON-NLS-1$
    tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        complexDataset.showLegend();
      }

    };
    tableItem.addActionListener(tableListener);
    menu.add(tableItem);
  }

  /**
   * Sets the labels on the data table.
   * The xlabel resets the x axis label on the plot.
   *
   * @param xlabel String
   * @param reLabel String
   * @param imLabel String
   */
  public void setXYColumnNames(String xlabel, String reLabel, String imLabel) {
    complexDataset.setXYColumnNames(xlabel, reLabel, imLabel);
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setXLabel(xlabel);
    }
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
         complexDataset.setXYColumnNames("mode", "re", "im");                                                //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel(DisplayRes.getString("FFTFrame.Plot.XLabel.Mode"));      //$NON-NLS-1$
         }
         break;
       case FREQ :
         complexDataset.setXYColumnNames("f", "re", "im");                                                   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel(DisplayRes.getString("FFTFrame.Plot.XLabel.Frequency")); //$NON-NLS-1$
         }
         break;
       case OMEGA :
         complexDataset.setXYColumnNames("omega", "re", "im");                                                //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel(DisplayRes.getString("FFTFrame.Plot.XLabel.Omega"));      //$NON-NLS-1$
         }
         break;
       case WAVENUMBER :
         complexDataset.setXYColumnNames("k", "re", "im");                                                    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel(DisplayRes.getString("FFTFrame.Plot.XLabel.WaveNumber")); //$NON-NLS-1$
         }
         break;
       case MOMENTUM :
         complexDataset.setXYColumnNames("p", "re", "im");                                                  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel(DisplayRes.getString("FFTFrame.Plot.XLabel.Momentum")); //$NON-NLS-1$
         }
         break;
    }
  }

  public void convertToPostView() {
    complexDataset.setMarkerShape(ComplexDataset.PHASE_POST);
    drawingPanel.invalidateImage();
    postItem.setSelected(true);
    drawingPanel.repaint();
  }

  public void convertToAmpAndPhaseView() {
    complexDataset.setMarkerShape(ComplexDataset.PHASE_CURVE);
    ampPhaseItem.setSelected(true);
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  public void convertToPhaseBarView() {
    complexDataset.setMarkerShape(ComplexDataset.PHASE_BAR);
    barItem.setSelected(true);
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /**
   * Does an FFT of the given real and imaginary function.
   * The function is evaulauted on the interval [xmin, xmax).  Note that the endpoint, xmax, is excluded.
   *
   * @param reF function  the real part of the function.
   * @param imF function  the imaginary part of the function.
   * @param xmin double
   * @param xmax double
   * @param n int number of compelx data points
   */
  public void doFFT(Function reF, Function imF, double xmin, double xmax, int n) {
    if(2*n!=fftData.length) {
      // complex data requires twice the storage of real data
      fftData = new double[2*n];
      fft = new FFT(n);
    }
    int off = (int) (n*xmin/(xmax-xmin));
    off = Math.abs(off);
    double xi = xmin, dx = (xmax-xmin)/n;
    for(int i = 0; i<n; i++) {
      int ii = (off+i)%n;
      fftData[2*ii] = (reF==null) ? 0 : reF.evaluate(xi);
      fftData[2*ii+1] = (imF==null) ? 0 : imF.evaluate(xi);
      xi += dx;
    }
    fft.transform(fftData);
    fft.toNaturalOrder(fftData);
    double[] domain = null;
    switch(domainType) {
       case MODE :
         domain = fft.getNaturalModes();
         break;
       case FREQ :
         domain = fft.getNaturalFreq(xmin, xmax);
         break;
       case OMEGA :
       case MOMENTUM :
       case WAVENUMBER :
         domain = fft.getNaturalOmega(xmin, xmax);
         break;
    }
    complexDataset.clear();
    complexDataset.append(domain, fftData);
  }

  /**
   * Does an FFT of the given data.
   *
   * The array is assumed to contain real numbers.
   * The given array remains unchanged.
   *
   * @param data double[]
   * @param xmin double
   * @param xmax double
   */
  public void doRealFFT(double[] data, double xmin, double xmax) {
    int n = fft.getN();
    if(2*data.length!=fftData.length) {
      n = data.length; // number of complex data points
      // complex data requires twice the storage of real data
      fftData = new double[2*n];
      fft = new FFT(n);
    }
    int off = (int) (n*xmin/(xmax-xmin));
    off = Math.abs(off);
    for(int i = 0; i<n; i++) {
      int ii = (off+i)%n;
      fftData[2*ii] = data[i];
      fftData[2*ii+1] = 0;
    }
    fft.transform(fftData);
    fft.toNaturalOrder(fftData);
    double[] domain = null;
    switch(domainType) {
       case MODE :
         domain = fft.getNaturalModes();
         break;
       case FREQ :
         domain = fft.getNaturalFreq(xmin, xmax);
         break;
       case OMEGA :
       case MOMENTUM :
       case WAVENUMBER :
         domain = fft.getNaturalOmega(xmin, xmax);
         break;
    }
    complexDataset.clear();
    complexDataset.append(domain, fftData);
  }

  /**
   * Does an FFT of the given data array.
   *
   * The data array is assumed to contain complex numbers stored as
   * successive (re,im) pairs.
   * The given array remains unchanged.
   *
   * @param data double[]
   * @param xmin double
   * @param xmax double
   */
  public void doFFT(double[] data, double xmin, double xmax) {
    int n = fft.getN();
    if(data.length!=fftData.length) {
      fftData = new double[data.length];
      n = data.length/2; // number of data points
      fft = new FFT(n);
    }
    int off = (int) (n*xmin/(xmax-xmin));
    off = Math.abs(off);
    for(int i = 0; i<n; i++) {
      int ii = (off+i)%n;
      fftData[2*ii] = data[2*i];
      fftData[2*ii+1] = data[2*i+1];
    }
    fft.transform(fftData);
    fft.toNaturalOrder(fftData);
    double[] domain = null;
    switch(domainType) {
       case MODE :
         domain = fft.getNaturalModes();
         break;
       case FREQ :
         domain = fft.getNaturalFreq(xmin, xmax);
         break;
       case OMEGA :
       case MOMENTUM :
       case WAVENUMBER :
         domain = fft.getNaturalOmega(xmin, xmax);
         break;
    }
    complexDataset.clear();
    complexDataset.append(domain, fftData);
  }

  /**
   * Gets Drawable objects added by the user to this frame.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    ArrayList<Drawable> list = super.getDrawables();
    list.remove(complexDataset);
    return list;
  }

  /**
   * Removes drawable objects added by the user from this frame.
   */
  public void clearDrawables() {
    drawingPanel.clear();                     // removes all drawables
    drawingPanel.addDrawable(complexDataset); // puts complex dataset back into panel
    showDataTable(false);
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
    list.remove(complexDataset);
    return list;
  }

  /**
   * Clears all the stored complex data.
   */
  public void clearData() {
    complexDataset.clear();
    dataTable.refreshTable();
    drawingPanel.invalidateImage();
  }

  /**
   * Sets the axes to use a logarithmetic scale.
   */
  public void setLogScale(boolean xlog, boolean ylog) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setLogScale(xlog, ylog);
    }
  }

  /**
   * Shows how phase angle is mapped to color.
   */
  public void showLegend() {
    complexDataset.showLegend();
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
