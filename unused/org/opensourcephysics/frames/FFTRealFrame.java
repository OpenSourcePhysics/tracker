/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.border.EtchedBorder;
import org.opensourcephysics.analysis.FourierSinCosAnalysis;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.ejs.control.GroupControl;

/**
 * FFTRealFrame computes the FFT or real data and displays the result.
 *
 * @author W. Christian
 * @version 1.0
 */
public class FFTRealFrame extends PlotFrame {
  public static final int FREQ = 1;
  public static final int OMEGA = 2;
  protected int domainType = FREQ;
  protected int gutter = 0;
  private FourierSinCosAnalysis fft = new FourierSinCosAnalysis();
  JMenuItem connectedItem, postItem;
  GroupControl gui;
  double[] x, data;

  /**
   * A DrawingFrame that displays a FFT as its drawable.
   *
   * @param xlabel String
   * @param ylabel String
   * @param title String
   */
  public FFTRealFrame(String xlabel, String ylabel, String title) {
    super(xlabel, ylabel, title);
    setConnected(false);
    setMarkerShape(0, Dataset.POST);
    setMarkerColor(0, Color.DARK_GRAY);
    limitAutoscaleY(-1.E-5, 1.E-5);
    setXYColumnNames(0, DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$ 
      DisplayRes.getString("FourierSinCosAnalysis.Column.Power"),                 //$NON-NLS-1$ 
        DisplayRes.getString("FourierSinCosAnalysis.PowerSpectrum"));             //$NON-NLS-1$
    setMarkerShape(1, Dataset.POST);
    setMarkerColor(1, Color.RED);
    setXYColumnNames(1, DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$ 
      DisplayRes.getString("FourierAnalysis.Column.Real"),                        //$NON-NLS-1$ 
        DisplayRes.getString("FourierAnalysis.RealCoefficients"));                //$NON-NLS-1$
    setMarkerShape(2, Dataset.POST);
    setMarkerColor(2, Color.BLUE);
    setXYColumnNames(2, DisplayRes.getString("FourierAnalysis.Column.Frequency"), //$NON-NLS-1$ 
      DisplayRes.getString("FourierAnalysis.Column.Imaginary"),                   //$NON-NLS-1$  
        DisplayRes.getString("FourierAnalysis.ImaginaryCoefficients"));           //$NON-NLS-1$
    dataTable.setRowNumberVisible(true);
    buildUserInterface();
    showPower();
  }

  /**
   * Builds the user interface.
   */
  void buildUserInterface() {
    setSize(350, 300);
    JPanel inputPanel = new JPanel();
    inputPanel.setBorder(new EtchedBorder());
    gui = new GroupControl(this); // use Easy Java Simulation components to build a user interface
    gui.addObject(inputPanel, "Panel", "name=inputPanel;layout=flow");                                        //$NON-NLS-1$ //$NON-NLS-2$
    gui.add("Panel", "name=radioPanel;parent=inputPanel");                                                    //$NON-NLS-1$ //$NON-NLS-2$
    gui.add("RadioButton", "parent=radioPanel;text= sin;action=showSin()");                                   //$NON-NLS-1$ //$NON-NLS-2$
    gui.add("RadioButton", "parent=radioPanel;text= cos;action=showCos()");                                   //$NON-NLS-1$ //$NON-NLS-2$
    gui.add("RadioButton", "parent=radioPanel;text= power;action=showPower();selected=true");                 //$NON-NLS-1$ //$NON-NLS-2$
    gui.add("Panel", "name=numberPanel; parent= inputPanel; layout=flow");                                    //$NON-NLS-1$ //$NON-NLS-2$
    gui.add("Label", "parent=numberPanel; text=added points=");                                               //$NON-NLS-1$ //$NON-NLS-2$
    gui.add("NumberField", "parent=numberPanel; variable=gutter; format=000; action=setGutter();size=40,16"); //$NON-NLS-1$ //$NON-NLS-2$
    getContentPane().add(inputPanel, BorderLayout.SOUTH);
  }

  public void showSin() {
    getDataset(0).setVisible(false);
    getDataset(1).setVisible(false);
    getDataset(2).setVisible(true);
    invalidateImage();
    repaint();
  }

  public void showCos() {
    getDataset(0).setVisible(false);
    getDataset(1).setVisible(true);
    getDataset(2).setVisible(false);
    invalidateImage();
    repaint();
  }

  public void showPower() {
    getDataset(0).setVisible(true);
    getDataset(1).setVisible(false);
    getDataset(2).setVisible(false);
    invalidateImage();
    repaint();
  }

  public void setGutter() {
    gutter = gui.getInt("gutter"); //$NON-NLS-1$
    doFFT();
  }

  /**
   * Adds extra points to the data before performing the Fourier analysis using the FFT.
   * @param n int
   */
  public void setGutter(int n) {
    gutter = n;
    gui.setValue("gutter", n); //$NON-NLS-1$
    doFFT();
  }

  /**
   * Sets the x-label on the plot.
   *
   * @param xlabel String
   */
  public void setXLabel(String xlabel) {
    setXYColumnNames(0, xlabel, DisplayRes.getString("FourierSinCosAnalysis.Column.Power"), //$NON-NLS-1$
      DisplayRes.getString("FourierSinCosAnalysis.PowerSpectrum"));                         //$NON-NLS-1$
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setXLabel(xlabel);
    }
  }

  /**
   * Adds Views menu items on the menu bar.
   */
  protected void addMenuItems() {
    super.addMenuItems();
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return;
    }
    JMenu menu = getMenu(DisplayRes.getString("DrawingFrame.Views_menu")); //$NON-NLS-1$
    if(menu==null) {
      menu = new JMenu(DisplayRes.getString("DrawingFrame.Views_menu")); //$NON-NLS-1$
      menuBar.add(menu);
      menuBar.validate();
    } else {                                                             // add a separator if tools already exists
      menu.addSeparator();
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
    // connected view
    connectedItem = new JRadioButtonMenuItem(DisplayRes.getString("FFTRealFrame.MenuItem.ConnectedView")); //$NON-NLS-1$
    menubarGroup.add(connectedItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToConnectedView();
      }

    };
    connectedItem.addActionListener(actionListener);
    menu.add(connectedItem);
  }

  /**
   * Sets the units for the FFT output.
   * Domain types are:  MODE, FREQ, OMEGA
   *
   * @param type int
   */
  public void setDomainType(int type) {
    domainType = type;
    switch(domainType) {
       case FREQ :
         fft.useRadians(false);
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel(DisplayRes.getString("FourierAnalysis.Column.Frequency")); //$NON-NLS-1$
         }
         break;
       case OMEGA :
         fft.useRadians(true);
         if(drawingPanel instanceof PlottingPanel) {
           ((PlottingPanel) drawingPanel).setXLabel("$\\omega$"); //$NON-NLS-1$
         }
         break;
    }
  }

  public void convertToPostView() {
    setConnected(false);
    setMarkerShape(0, Dataset.POST);
    setMarkerShape(1, Dataset.POST);
    setMarkerShape(2, Dataset.POST);
    drawingPanel.invalidateImage();
    postItem.setSelected(true);
    drawingPanel.repaint();
  }

  public void convertToConnectedView() {
    setConnected(true);
    setMarkerShape(0, Dataset.NO_MARKER);
    setMarkerShape(1, Dataset.NO_MARKER);
    setMarkerShape(2, Dataset.NO_MARKER);
    connectedItem.setSelected(true);
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /**
   * Does an FFT of the given data array.
   *
   * The data array is assumed to contain complex numbers stored as
   * successive (re,im) pairs.
   * The given array remains unchanged.
   *
   * @param datasetManager double[]
   * @param xmin double
   * @param xmax double
   */
  public void doFFT(double[] xNew, double[] dataNew, int gutter) {
    x = new double[2*(xNew.length/2)]; // only even number of points allowed
    System.arraycopy(xNew, 0, x, 0, x.length);
    data = new double[2*(dataNew.length/2)]; // only even number of points allowed
    System.arraycopy(dataNew, 0, data, 0, data.length);
    this.gutter = gutter;
    gui.setValue("gutter", gutter); //$NON-NLS-1$
    doFFT();
  }

  void doFFT() {
    if(x==null) {
      return;
    }
    fft.doAnalysis(x, data, gutter);
    clearData();
    double[][] arrayData = fft.getData2D();
    append(0, arrayData[0], arrayData[1]); // power
    append(1, arrayData[0], arrayData[2]); // cos coef
    append(2, arrayData[0], arrayData[3]); // sin coef
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
    invalidateImage();
    repaint();
  }

  /**
   * Sets the axes to use a logarithmetic scale.
   */
  public void setLogScale(boolean xlog, boolean ylog) {
    if(drawingPanel instanceof PlottingPanel) {
      ((PlottingPanel) drawingPanel).setLogScale(xlog, ylog);
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
