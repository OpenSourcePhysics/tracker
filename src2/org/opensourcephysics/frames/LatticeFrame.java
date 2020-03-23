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
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MouseInputAdapter;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display2d.ByteLattice;
import org.opensourcephysics.display2d.CellLattice;
import org.opensourcephysics.display2d.SiteLattice;

/**
 * A DrawingFrame that displays plots using a Lattice.
 *
 * @author W. Christian
 * @version 1.0
 */
public class LatticeFrame extends DrawingFrame {
  JMenuItem cellItem, siteItem;
  protected ByteLattice lattice = new CellLattice(4, 4);
  MouseInputAdapter mouseAdapter;
  int[] editValues = new int[2];
  int dragV;

  /**
   * Constructs a LatticeFrame with the given axes labels and frame title.
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public LatticeFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, null));
    setTitle(frameTitle);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorXGrid(false);
    ((PlottingPanel) drawingPanel).getAxes().setShowMajorYGrid(false);
    addMenuItems();
    drawingPanel.addDrawable(lattice);
    setAnimated(true);
    setAutoclear(true);
  }

  /**
   * Outlines the lattice boundaries with a grid.
   *
   * @param showGridLines
   */
  public void setShowGridLines(boolean showGridLines) {
    lattice.setShowGridLines(showGridLines);
  }

  /**
   * Constructs a LatticeFrame with the given frame title but without axes.
   * @param frameTitle String
   */
  public LatticeFrame(String frameTitle) {
    super(new InteractivePanel());
    setTitle(frameTitle);
    addMenuItems();
    drawingPanel.addDrawable(lattice);
    setAnimated(true);
    setAutoclear(true);
  }

  /**
   * Shows the color associated with each value.
   * @return the JFrame containing the legend
   */
  public void showLegend() {
    lattice.showLegend();
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
    cellItem = new JRadioButtonMenuItem(DisplayRes.getString("LatticeFrame.MenuItem.CellLattice")); //$NON-NLS-1$
    menubarGroup.add(cellItem);
    cellItem.setSelected(true);
    ActionListener tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToCellLattice();
      }

    };
    cellItem.addActionListener(tableListener);
    menu.add(cellItem);
    // surface plot menu item
    siteItem = new JRadioButtonMenuItem(DisplayRes.getString("LatticeFrame.MenuItem.SiteLattice")); //$NON-NLS-1$
    menubarGroup.add(siteItem);
    tableListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToSiteLattice();
      }

    };
    siteItem.addActionListener(tableListener);
    menu.add(siteItem);
  }

  /**
   * Converts this lattice to a SiteLattice from a CellLattice.
   */
  public void convertToSiteLattice() {
    if(lattice instanceof CellLattice) {
      drawingPanel.removeDrawable(lattice);
      lattice = ((CellLattice) lattice).createSiteLattice();
      drawingPanel.addDrawable(lattice);
      siteItem.setSelected(true);
      drawingPanel.repaint();
    }
  }

  /**
   * Converts this lattice to a Byte lattice from a CellLattice.
   */
  public void convertToCellLattice() {
    if(lattice instanceof SiteLattice) {
      drawingPanel.removeDrawable(lattice);
      lattice = ((SiteLattice) lattice).createCellLattice();
      drawingPanel.addDrawable(lattice);
      cellItem.setSelected(true);
      drawingPanel.repaint();
    }
  }

  /**
   * Clears drawable objects added by the user from this frame.
   */
  public void clearDrawables() {
    drawingPanel.clear(); // removes all drawables
    drawingPanel.addDrawable(lattice);
  }

  /**
   * Gets Drawable objects added by the user to this frame.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    ArrayList<Drawable> list = super.getDrawables();
    list.remove(lattice);
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
    list.remove(lattice);
    return list;
  }

  /**
   * Clears the lattice data by setting all values to zero.
   */
  public void clearData() {
    lattice.setBlock(0, 0, new byte[lattice.getNx()][lattice.getNy()]);
    if(drawingPanel!=null) {
      drawingPanel.invalidateImage();
    }
  }

  /**
   * Sets a block of data to new values.
   *
   * The lattice is resized to fit the new data as needed.
   *
   * @param val
   */
  public void setAll(byte val[][]) {
    if((lattice.getNx()!=val.length)||(lattice.getNy()!=val[0].length)) {
      lattice.resizeLattice(val.length, val[0].length);
    }
    lattice.setBlock(0, 0, val);
    drawingPanel.invalidateImage();
  }

  /**
   * Sets the lattice values and scale.
   *
   * The lattice is resized to fit the new data as needed.
   *
   * @param val int[][] the new values
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void setAll(byte val[][], double xmin, double xmax, double ymin, double ymax) {
    setAll(val);
    lattice.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Randomizes the lattice values.
   */
  public void randomize() {
    lattice.randomize();
  }

  /**
   * Resizes the number of columns and rows in the lattice
   *
   * @param nx int
   * @param ny int
   */
  public void resizeLattice(int nx, int ny) {
    lattice.resizeLattice(nx, ny);
  }

  /**
   * Sets the color for a single index.
   * @param index
   * @param color
   */
  public void setIndexedColor(int index, Color color) {
    lattice.setIndexedColor(index, color);
  }

  /**
   * Sets the color palette.
   *
   * @param colors
   */
  public void setColorPalette(Color[] colors) {
    lattice.setColorPalette(colors);
  }

  /**
   * Sets the ith indexed cell or site of the lattice to store value v
   * Values are in row-major format such that the index corresponds to iy*ny+ix.
   *
   * @param i
   * @param v
   */
  public void setAtIndex(int i, int v) {
    int Nx = lattice.getNx();
    setValue(i%Nx, i/Nx, v);
  }

  /**
   * Sets the (ix,iy) value of the lattice.
   *
   * @param ix
   * @param iy
   * @param v
   */
  public void setValue(int ix, int iy, int v) {
    lattice.setValue(ix, iy, (byte) v);
  }

  /**
   * Sets the lattice values and scale.
   * Values are in row-major format such that the index corresponds to iy*ny+ix.
   *
   * @param val int[][] the new values
   * @param nx
   * @param xmin double
   * @param xmax double
   * @param ymin double
   * @param ymax double
   */
  public void setAll(int val[], int nx, double xmin, double xmax, double ymin, double ymax) {
    if(val.length%nx!=0) {
      throw new IllegalArgumentException("Number of values in lattice (nx*ny) must match number of values."); //$NON-NLS-1$
    }
    resizeLattice(nx, val.length/nx);
    setAll(val);
    lattice.setMinMax(xmin, xmax, ymin, ymax);
  }

  /**
   * Sets an array v of int values into the lattice, starting at (x=0,y=0).
   * Values are in row-major format such that the index corresponds to  iy*ny+ix.
   *
   * @param v
   */
  public void setAll(int[] v) {
    for(int i = 0; i<v.length; i++) {
      setAtIndex(i, v[i]);
    }
  }

  /**
   * Gets the ith indexed cell of the lattice
   * Values are in row-major format such that the index corresponds to  iy*ny+ix.
   *
   * @param    i
   * @return   value indexed by i
   */
  public int getAtIndex(int i) {
    int Nx = lattice.getNx();
    return getValue(i%Nx, i/Nx);
  }

  /**
   * Gets the (x,y) value of the lattice.
   *
   * @param    ix
   * @param    iy
   * @return   value at (ix,iy)
   */
  public int getValue(int ix, int iy) {
    return lattice.getValue(ix, iy);
  }

  /**
   * Gets the entire lattice contents in an int array.
   * Values are in row-major format such that the index corresponds to iy*ny+ix.
   *
   * @return   array containing entire lattice contents
   */
  public int[] getAll() {
    int N = lattice.getNx()*lattice.getNy();
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
   * Determines the lattice index (row-major order) from given x and y world coordinates
   *
   * @param x
   * @param y
   * @return index
   */
  public int indexFromPoint(double x, double y) {
    return lattice.indexFromPoint(x, y);
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
