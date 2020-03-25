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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MouseInputAdapter;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display2d.ByteRaster;

/**
 * A DrawingFrame that displays data using a ByteRaster.
 *
 * @author W. Christian
 * @version 1.0
 */
public class RasterFrame extends DrawingFrame {
  protected ByteRaster raster = new ByteRaster(1, 1);
  MouseInputAdapter mouseAdapter;
  int[] editValues = new int[2];
  int dragV;
  Color[] customColors;

  /**
   * Constructs a RasterFrame with the given axes labels and frame title.
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public RasterFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, null));
    setTitle(frameTitle);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorXGrid(false);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorYGrid(false);
    addMenuItems();
    drawingPanel.addDrawable(raster);
    setAnimated(true);
    setAutoclear(true);
  }

  /**
   * Constructs a RasterFrame with the given frame title but without axes.
   * @param frameTitle String
   */
  public RasterFrame(String frameTitle) {
    super(new InteractivePanel());
    setTitle(frameTitle);
    addMenuItems();
    drawingPanel.addDrawable(raster);
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
    // add a menu item to show the data table
    JMenuItem menuItem = new JMenuItem(DisplayRes.getString("RasterFrame.MenuItem.Color")); //$NON-NLS-1$
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setColorPalette();
      }

    };
    menuItem.addActionListener(actionListener);
    menu.add(menuItem);
    // add a menu item to show the data table
    menuItem = new JMenuItem(DisplayRes.getString("RasterFrame.MenuItem.B&W")); //$NON-NLS-1$
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setBWPalette();
      }

    };
    menuItem.addActionListener(actionListener);
    menu.add(menuItem);
  }

  /**
   * Sets black and white palette.
   */
  public void setBWPalette() {
    raster.setBWPalette();
    repaint();
  }

  public void setColorPalette() {
    if(customColors==null) {
      raster.createDefaultColors();
    } else {
      raster.setColorPalette(customColors);
    }
    repaint();
  }

  public void showLegend() {
    raster.showLegend();
  }

  /**
   * Sets the color palette.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    customColors = colors;
    raster.setColorPalette(colors);
    repaint();
  }

  /**
 * Clears drawable objects added by the user from this frame.
 */
  public void clearDrawables() {
    drawingPanel.clear(); // removes all drawables
    drawingPanel.addDrawable(raster);
  }

  /**
   * Gets Drawable objects added by the user to this frame.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    ArrayList<Drawable> list = super.getDrawables();
    list.remove(raster);
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
    list.remove(raster);
    return list;
  }

  /**
   * Clears the lattice data by setting all values to zero.
   */
  public void clearData() {
    raster.setBlock(0, 0, new byte[raster.getNx()][raster.getNy()]);
    drawingPanel.invalidateImage();
  }

  /**
   * Randomizes the lattice values.
   */
  public void randomize() {
    raster.randomize();
  }

  /**
   * Resizes the number of columns and rows in the raster and centers the raster.
   *
   * @param nx int
   * @param ny int
   */
  private void resizeRaster(int nx, int ny) {
    drawingPanel.setPreferredSize(new java.awt.Dimension(Math.max(nx+drawingPanel.getLeftGutter()+drawingPanel.getRightGutter(), 50), Math.max(ny+drawingPanel.getTopGutter()+drawingPanel.getBottomGutter(), 50)));
    pack();
    raster.resizeLattice(nx, ny);
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
  }

  /**
 * Sets the color for a single index.
 * @param index
 * @param color
 */
  public void setIndexedColor(int index, Color color) {
    raster.setIndexedColor(index, color);
  }

  /**
 * Sets the raster's values and scale.
 *
 * @param val int[][] the new values
 * @param xmin double
 * @param xmax double
 * @param ymin double
 * @param ymax double
 */
  public void setAll(byte val[][], double xmin, double xmax, double ymin, double ymax) {
    setAll(val);
    raster.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
 * Sets the raster's values using byte values.
 *
 * @param val
 */
  public void setAll(byte val[][]) {
    if((val.length!=raster.getNx())||(val[0].length!=raster.getNy())) {
      this.resizeRaster(val.length, val[0].length);
    }
    raster.setBlock(0, 0, val);
  }

  /**
   * Sets the raster's values using integer values.
   *
   * @param val
   */
  public void setAll(int val[][]) {
    if((val.length!=raster.getNx())||(val[0].length!=raster.getNy())) {
      resizeRaster(val.length, val[0].length);
    }
    raster.setBlock(0, 0, val);
  }

  /**
   * Sets the raster's values and scale.
   *
   * @param val int[][] the new values
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void setAll(int val[][], double xmin, double xmax, double ymin, double ymax) {
    setAll(val);
    raster.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
 * Sets the raster's values and scale.
 *
 * @param val int[] the new values
 * @param nx
 * @param xmin double
 * @param xmax double
 * @param ymin double
 * @param ymax double
 */
  public void setAll(int val[], int nx, double xmin, double xmax, double ymin, double ymax) {
    if(val.length%nx!=0) {
      throw new IllegalArgumentException("Raster dimension must match number of values."); //$NON-NLS-1$
    }
    resizeRaster(nx, val.length/nx);
    setAll(val);
    raster.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Sets an array v of int values into the raster, starting at (x=0,y=0).
   * Values are in row-major format such that the index corresponds to iy*ny+ix.
   *
   * @param v
   */
  public void setAll(int[] v) {
    if(v.length!=raster.getNx()*raster.getNy()) {
      throw new IllegalArgumentException("Raster size must be set before using row-major format."); //$NON-NLS-1$
    }
    for(int i = 0; i<v.length; i++) {
      setAtIndex(i, v[i]);
    }
  }

  /**
  * Sets the ith indexed pixel to value v
  * Values are in row-major format such that the index corresponds to iy*ny+ix.
  *
  * @param i
  * @param v
  */
  public void setAtIndex(int i, int v) {
    int nx = raster.getNx();
    setValue(i%nx, i/nx, v);
  }

  /**
  * Sets the (ix,iy) cell or the site of the raster to store value v
  *
  * @param ix
  * @param iy
  * @param v
  */
  public void setValue(int ix, int iy, int v) {
    raster.setValue(ix, iy, (byte) v);
  }

  /**
  * Gets the ith indexed pixel of the raster
  * Values are in row-major format such that the index corresponds to iy*ny+ix.
  *
  * @param    i
  * @return   value indexed by i
  */
  public int getAtIndex(int i) {
    int Nx = raster.getNx();
    return get(i%Nx, i/Nx);
  }

  /**
  * Gets the (x,y) pixel of the raster
  *
  * @param    ix
  * @param    iy
  * @return   value at (ix,iy)
  */
  public int get(int ix, int iy) {
    return(raster.getValue(ix, iy)+128);
  }

  /**
   * Gets the entire raster contents in an int array
   * Values are in row-major format such that the index corresponds to iy*ny+ix.
   *
   * @return   array containing entire lattice contents
   */
  public int[] getAll() {
    int N = raster.getNx()*raster.getNy();
    int[] ret = new int[N];
    for(int i = 0; i<N; i++) {
      ret[i] = getAtIndex(i);
    }
    return ret;
  }

  /**
   * Sets an action to toggle the grid betweem the given values when the mouse is pressed.
   * @param enable boolean
   * @param v1 int
   * @param v2 int
   */
  public void setToggleOnClick(boolean enable, int v1, int v2) {
    editValues = new int[] {v1, v2};
    if(enable) {
      drawingPanel.addMouseListener(getMouseAdapter());
      drawingPanel.addMouseMotionListener(getMouseAdapter());
    } else {
      drawingPanel.removeMouseListener(getMouseAdapter());
      drawingPanel.removeMouseMotionListener(getMouseAdapter());
    }
  }

  void mouse(MouseEvent e, boolean pressed) {
    // button three is used for popup menu
    if(e.getButton()==MouseEvent.BUTTON3) {
      return;
    }
    double x = drawingPanel.pixToX(e.getX());
    double y = drawingPanel.pixToY(e.getY());
    int i = indexFromPoint(x, y);
    if(i==-1) {
      return;
    }
    if(pressed) {
      dragV = editValues[0];
      int len = editValues.length;
      for(int j = 0; j<len; j++) {
        if(getAtIndex(i)==editValues[j]) {
          dragV = editValues[(j+1)%len];
        }
      }
    }
    if(getAtIndex(i)!=dragV) {
      setAtIndex(i, dragV);
      drawingPanel.render();
    }
  }

  /**
   * Gets the x coordinate for the given index.
   *
   * @param i int
   * @return double the x coordiante
   */
  public double indexToX(int i) {
    if(raster==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return raster.indexToX(i);
  }

  /**
   * Gets the index that is closest to the given x value
   *
   * @return double the x coordiante
   */
  public int xToIndex(double x) {
    if(raster==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return raster.xToIndex(x);
  }

  /**
   * Gets the index that is closest to the given y value
   *
   * @return double the y coordiante
   */
  public int yToIndex(double y) {
    if(raster==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return raster.yToIndex(y);
  }

  /**
   * Gets the y coordinate for the given index.
   *
   * @param i int
   * @return double the y coordiante
   */
  public double indexToY(int i) {
    if(raster==null) {
      throw new IllegalStateException("Data has not been set.  Invoke setAll before invoking this method."); //$NON-NLS-1$
    }
    return raster.indexToY(i);
  }

  /**
   * Determines the lattice index (row-major order) from given x and y world coordinates
   *
   * @param x
   * @param y
   * @return index
   */
  public int indexFromPoint(double x, double y) {
    return raster.indexFromPoint(x, y);
  }

  synchronized MouseInputAdapter getMouseAdapter() {
    if(mouseAdapter==null) {
      return new MouseInputAdapter() {
        public void mousePressed(MouseEvent e) {
          mouse(e, true);
        }
        public void mouseDragged(MouseEvent e) {
          mouse(e, false);
        }

      };
    }
    return mouseAdapter;
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
