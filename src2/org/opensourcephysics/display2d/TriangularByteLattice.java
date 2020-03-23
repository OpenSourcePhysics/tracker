/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.Measurable;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

/**
 *  A TriangularByteLattice is an array where each array element can assume one
 *  of 256 values. Values can be set between -128 and 127. Because byte values
 *  larger than 127 overflow to negative, values can also be set between 0 and
 *  255.
 *
 * @author     Joshua Gould
 * @author     Wolfgang Christian
 * @created    May 13, 2003
 * @version    1.0
 */
public class TriangularByteLattice implements Measurable {
  int nrow, ncol;
  byte[][] data;
  double xmin, ymin, xmax, ymax;
  double xminLattice, yminLattice, xmaxLattice, ymaxLattice;
  final static double SQRT3_OVER2 = Math.sqrt(3)/2.0;
  boolean visible = true;
  Color[] colors = new Color[256];
  final static int radius = 3;
  final static int diameter = radius*2;
  JFrame legendFrame;

  /**
   *  Constructs a byte lattice with the given size. Cell values are -128 to 127.
   *
   * @param  _row  the number of rows
   * @param  _col  the number of columns
   */
  public TriangularByteLattice(int _row, int _col) {
    nrow = _row;
    ncol = _col;
    createDefaultColors();
    data = new byte[nrow][ncol];
    // col in x direction, row in y direction
    xminLattice=xmin = 0;
    xmaxLattice =xmax = ncol-0.5;
    ymin = (nrow-1)*SQRT3_OVER2;
    if(ymin==0) {
      ymin = SQRT3_OVER2; // FIXME
    }
    yminLattice = ymin;
    ymaxLattice=ymax = 0; // zero is on top
  }

  public void resizeLattice(int _row, int _col) {
    nrow = _row;
    ncol = _col;
    data = new byte[nrow][ncol];
    // col in x direction, row in y direction
    xminLattice=xmin = 0;
    xmaxLattice =xmax = ncol-0.5;
    ymin = (nrow-1)*SQRT3_OVER2;
    if(ymin==0) {
      ymin = SQRT3_OVER2; // FIXME
    }
    yminLattice = ymin;
    ymaxLattice=ymax = 0; // zero is on top
  }
  
  public void setVisible (boolean _vis) {
    this.visible = _vis;  
  }
  
  public void setMinMax(double xmin, double xmax, double ymin, double ymax) {
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;
  }

  /**
   *  Draws the lattice.
   *
   * @param  panel
   * @param  g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    Shape oldClip = g.getClip();
    g.setClip(null);
    double xScale=(xmax-xmin)/(xmaxLattice-xminLattice);
    double yScale=-(ymax-ymin)/(ymaxLattice-yminLattice);
    for(int yi = 0; yi<nrow; yi++) {
      for(int xi = 0; xi<ncol; xi++) {
        byte val = data[yi][xi];
        g.setColor(colors[val&0xFF]);
        // each row is sqrt(3)/2 offset previous row
        if((yi%2)==1) {
          double x=(xi+0.5)*xScale+xmin;
          double y=yi*SQRT3_OVER2*yScale+ymin;
          g.fillOval(panel.xToPix(x)-radius, panel.yToPix(y)-radius, diameter, diameter); // shift to right by 0.5
        } else {
          double x=xi*xScale+xmin;
          double y=yi*SQRT3_OVER2*yScale+ymin;
          g.fillOval(panel.xToPix(x)-radius, panel.yToPix(y)-radius, diameter, diameter);
        }
      }
    }
    g.setClip(oldClip);
  }

  /**
   *  Sets a block of cells to new values.
   *
   * @param  row_offset
   * @param  col_offset
   * @param  val
   */
  public void setBlock(int row_offset, int col_offset, byte val[][]) {
    if((row_offset<0)||(row_offset+val.length>nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    if((col_offset<0)||(col_offset+val[0].length>ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    for(int rindex = row_offset, nr = val.length+row_offset; rindex<nr; rindex++) {
      for(int cindex = col_offset, nc = val[0].length+col_offset; cindex<nc; cindex++) {
        data[rindex][cindex] = val[rindex-row_offset][cindex-col_offset];
      }
    }
  }
  
  /**
   *  Sets a block of cells to new values.
   *
   * @param  row_offset
   * @param  col_offset
   * @param  val
   */
  public void setBlock(int row_offset, int col_offset, int val[][]) {
    if((row_offset<0)||(row_offset+val.length>nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    if((col_offset<0)||(col_offset+val[0].length>ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    for(int rindex = row_offset, nr = val.length+row_offset; rindex<nr; rindex++) {
      for(int cindex = col_offset, nc = val[0].length+col_offset; cindex<nc; cindex++) {
        data[rindex][cindex] = (byte) val[rindex-row_offset][cindex-col_offset];
      }
    }
  }

  /**
   *  Sets a column of cells to new values.
   *
   * @param  row_offset
   * @param  col
   * @param  val
   */
  public void setCol(int row_offset, int col, byte val[]) {
    if((row_offset<0)||(row_offset+val.length>nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    if((col<0)||(col>=ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    for(int rindex = row_offset, nr = val.length+row_offset; rindex<nr; rindex++) {
      data[rindex][col] = val[rindex-row_offset];
    }
  }

  /**
   *  Sets a row of cells to new values.
   *
   * @param  row
   * @param  col_offset
   * @param  val
   */
  public void setRow(int row, int col_offset, byte val[]) {
    if((row<0)||(row>=nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    if((col_offset<0)||(col_offset+val.length>ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setBlock."); //$NON-NLS-1$
    }
    for(int cindex = col_offset, nc = val.length+col_offset; cindex<nc; cindex++) {
      data[row][cindex] = val[cindex-col_offset];
    }
  }

  /**
   *  Sets a lattice cell to a new value.
   *
   * @param  row
   * @param  col
   * @param  val
   */
  public void setCell(int row, int col, byte val) {
    data[row][col] = val;
    // note that increasing x corresponds to an increaing column index
  }

  /**
   *  Gets a lattice cell value.
   *
   * @param  row
   * @param  col
   * @return      the cell value.
   */
  public int getCell(int row, int col) {
    return data[row][col];
  }

  /**  Ranomizes the lattice values. */
  public void randomize() {
    Random random = new Random();
    for(int rindex = 0, nr = data.length; rindex<nr; rindex++) {
      for(int cindex = 0, nc = data[0].length; cindex<nc; cindex++) {
        data[rindex][cindex] = (byte) random.nextInt(256);
      }
    }
  }

  /**  Shows the color associated with each value. */
  public void showLegend() {
    InteractivePanel dp = new InteractivePanel();
    dp.setPreferredSize(new java.awt.Dimension(300, 66));
    dp.setPreferredGutters(0, 0, 0, 35);
    dp.setClipAtGutter(false);
    if((legendFrame==null)||!legendFrame.isDisplayable()) {
      legendFrame = new JFrame(DisplayRes.getString("GUIUtils.Legend")); //$NON-NLS-1$
    }
    legendFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    legendFrame.setResizable(false);
    legendFrame.setContentPane(dp);
    TriangularByteLattice lattice = new TriangularByteLattice(1, 256);
    lattice.setMinMax(-128, 127, 0, 1);
    byte[][] data = new byte[1][256];
    for(int i = 0; i<256; i++) {
      data[0][i] = (byte) i;
    }
    lattice.setBlock(0, 0, data);
    dp.addDrawable(lattice);
    XAxis xaxis = new XAxis(""); //$NON-NLS-1$
    xaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    xaxis.setLocation(-0.5);
    xaxis.setEnabled(true);
    dp.addDrawable(xaxis);
    legendFrame.pack();
    legendFrame.setVisible(true);
  }

  /**
   *  Sets the color palette.
   *
   * @param  colors
   */
  public void setColorPalette(Color[] colors) {
    for(int i = 0, n = colors.length; i<n; i++) {
      this.colors[i] = colors[i];
    }
    for(int i = colors.length; i<256; i++) {
      this.colors[i] = Color.black;
    }
  }

  /**
   *  Sets the color for a single index.
   *
   * @param  i
   * @param  color
   */
  public void setIndexedColor(int i, Color color) {
    // i = i % colors.length;
    i = (i+256)%colors.length;
    colors[i] = color;
  }

  public boolean isMeasured() {
    return true;
  }

  public double getXMin() {
    return xmin;
  }

  public double getYMin() {
    return ymin;
  }

  public double getXMax() {
    return xmax;
  }

  public double getYMax() {
    return ymax;
  }

  public void createDefaultColors() {
    for(int i = 0; i<256; i++) {
      Color c = Color.getHSBColor((-0.07f+0.80f*i/255f)%1, 1, 1);
      colors[i] = c;
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
