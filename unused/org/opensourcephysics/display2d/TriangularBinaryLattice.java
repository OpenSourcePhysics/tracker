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
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Measurable;

/**
 *  A TriangularBinaryLattice is an array where each array element has a value
 *  of 0 or 1.
 *
 * @author     Joshua Gould
 * @author     Wolfgang Christian
 * @created    May 13, 2003
 * @version    1.0
 */
public class TriangularBinaryLattice implements Measurable {
  byte[] packedData;
  int nrow, ncol;
  boolean visible = true;
  double xmin, ymin, xmax, ymax;
  double xminLattice, yminLattice, xmaxLattice, ymaxLattice;
  Color zeroColor = Color.red, oneColor = Color.blue;
  final static double SQRT3_OVER2 = Math.sqrt(3)/2.0;
  final static int BITS_PER_BYTE = 8;
  final static int radius = 3;
  final static int diameter = radius*2;

  /**
   *  Constructs a binary lattice with the given size.
   *
   * @param  _nrow  the number of rows
   * @param  _ncol  the number of columns
   */
  public TriangularBinaryLattice(int _nrow, int _ncol) {
    nrow = _nrow;
    ncol = _ncol;
    int len = ((ncol+(BITS_PER_BYTE-1))/BITS_PER_BYTE)*nrow; // each row starts on a byte boundary
    packedData = new byte[len];
    // default colors are red and blue
    // col in x direction, row in y direction
    xminLattice=xmin = 0;
    xmaxLattice=xmax = ncol-0.5;
    ymin = nrow*SQRT3_OVER2-SQRT3_OVER2;
    if(ymin==0) {
      ymin = SQRT3_OVER2; // FIXME
    }
    yminLattice=ymin;
    ymaxLattice=ymax = 0; // zero is on top
  }

  /**
   * Resize the lattice.
   * @param _nx number of x sites
   * @param _ny number of y sites
   */
  public void resizeLattice(int _nrow, int _ncol) {
    nrow = _nrow;
    ncol = _ncol;
    int len = ((ncol+(BITS_PER_BYTE-1))/BITS_PER_BYTE)*nrow; // each row starts on a byte boundary
    packedData = new byte[len];
    // default colors are red and blue
    // col in x direction, row in y direction
    xminLattice=xmin = 0;
    xmaxLattice=xmax = ncol-0.5;
    ymin = nrow*SQRT3_OVER2-SQRT3_OVER2;
    if(ymin==0) {
      ymin = SQRT3_OVER2; // FIXME
    }
    yminLattice=ymin;
    ymaxLattice=ymax = 0; // zero is on top
  }

  public void setVisible (boolean _vis) {
    this.visible = _vis;  
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

  /**  Randomizes the lattice values. */
  public void randomize() {
    Random random = new Random();
    random.nextBytes(packedData);
  }

  /**
   *  Randomizes the lattice values with the specified probability. A probability
   *  of 1 indicates that all cells will be occupied.
   *
   * @param  probability  the probability of a site being occupied, between 0.0
   *      and 1.0.
   */
  public void randomize(double probability) {
    if((probability<0)||(probability>1)) {
      throw new IllegalArgumentException("Probability must be between 0 and 1"); //$NON-NLS-1$
    }
    Random random = new Random();
    for(int i = 0, size = packedData.length; i<size; i++) {
      byte packedcell = 0;
      for(int j = BITS_PER_BYTE; j>0; j--) {
        int mask = 0x80>>>(j-1);                  // start with 0x10000000 and shift right, 0x80 = 128
        double d = random.nextDouble();           // generates number between 0 inclusive and 1 exclusive
        if(d>=probability) {                      // set jth bit to 0
          packedcell = (byte) (packedcell&~mask); // AND bitwise with mask complement will clear index bit
        } else {                                  // set jth bit to 1
          packedcell = (byte) (packedcell|mask);  // OR bitwise with mask operation will set index bit
        }
      }
      packedData[i] = packedcell;
    }
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
    // drawableDelegate.draw(panel, g);
    Shape oldClip = g.getClip();
    g.setClip(null);
    double xScale=(xmax-xmin)/(xmaxLattice-xminLattice);
    double yScale=-(ymax-ymin)/(ymaxLattice-yminLattice);
    int row = 0;
    int column = 0;
    for(int i = 0, size = packedData.length; i<size; i++) {
      byte packedCell = packedData[i];
      for(int j = BITS_PER_BYTE; (j>0)&&(column<ncol); j--) {
        byte val = (byte) (packedCell>>>(j-1));
        int one_or_zero = (val&1);
        if(one_or_zero==0) {
          g.setColor(zeroColor);
        } else {
          g.setColor(oneColor);
        }
        // each row is sqrt(3)/2 offset from previous row
        if((row%2)==1) {
          double x=(column+0.5)*xScale+xmin;
          double y=row*SQRT3_OVER2*yScale+ymin;
          g.fillOval(panel.xToPix(x)-radius, panel.yToPix(y)-radius, diameter, diameter); // shift to right by 0.5
        } else {
          double x=column*xScale+xmin;
          double y=row*SQRT3_OVER2*yScale+ymin;
          g.fillOval(panel.xToPix(x)-radius, panel.yToPix(y)-radius, diameter, diameter);
        }
        if(column==(ncol-1)) {
          column = 0;
          row++;
          break;
        }
        column++;
      }
    }
    g.setClip(oldClip);
  }

  /**
   *  Scales this lattice to the given values in world units.
   *
   * @param  xmin
   * @param  xmax
   * @param  ymin
   * @param  ymax
   */
  public void setMinMax(double xmin, double xmax, double ymin, double ymax) {
    this.xmin = xmin;
    this.xmax = xmax;
    this.ymin = ymin;
    this.ymax = ymax;
  }

  /**
   *  Sets a block of cells to new values. A cell is set to 1 if the value is >0;
   *  the cell is set to zero otherwise
   *
   * @param  row_offset
   * @param  col_offset
   * @param  val         the array of values
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
        int arrayIndex = rindex*((ncol+7)/8)+cindex/8; // each row starts on a byte boundary
        byte packedcell = packedData[arrayIndex];
        int mask = 0x80>>>(cindex%8);                  // start with 0x10000000 and shift right
        if(val[rindex-row_offset][cindex-col_offset]<=0) {
          packedcell = (byte) (packedcell&~mask);      // AND bitwise with mask complement will clear index bit
        } else {
          packedcell = (byte) (packedcell|mask);       // OR bitwise with mask operation will set index bit
        }
        packedData[arrayIndex] = packedcell;
      }
    }
  }

  /**
   *  Sets a block of cells to new values. A cell is set to 1 if the value is >0;
   *  the cell is set to zero otherwise
   *
   * @param  row_offset
   * @param  col_offset
   * @param  val         the array of values
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
        int arrayIndex = rindex*((ncol+7)/8)+cindex/8; // each row starts on a byte boundary
        byte packedcell = packedData[arrayIndex];
        int mask = 0x80>>>(cindex%8);                  // start with 0x10000000 and shift right
        if(val[rindex-row_offset][cindex-col_offset]<=0) {
          packedcell = (byte) (packedcell&~mask);      // AND bitwise with mask complement will clear index bit
        } else {
          packedcell = (byte) (packedcell|mask);       // OR bitwise with mask operation will set index bit
        }
        packedData[arrayIndex] = packedcell;
      }
    }
  }

  /**
   *  Sets a column of cells to new values. A cell is set to 1 if the value is >
   *  0; the cell is set to zero otherwise
   *
   * @param  row_offset
   * @param  col
   * @param  val         the array of values
   */
  public void setCol(int row_offset, int col, int val[]) {
    if((row_offset<0)||(row_offset+val.length>nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    if((col<0)||(col>=ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    for(int rindex = row_offset, nr = val.length+row_offset; rindex<nr; rindex++) {
      int arrayIndex = rindex*((ncol+7)/8)+col/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(col%8);                  // start with 0x10000000 and shift right
      if(val[rindex-row_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   *  Sets a column of cells to new values. A cell is set to 1 if the value is >
   *  0; the cell is set to zero otherwise
   *
   * @param  row_offset
   * @param  col
   * @param  val         the array of values
   */
  public void setCol(int row_offset, int col, byte val[]) {
    if((row_offset<0)||(row_offset+val.length>nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    if((col<0)||(col>=ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setCol."); //$NON-NLS-1$
    }
    for(int rindex = row_offset, nr = val.length+row_offset; rindex<nr; rindex++) {
      int arrayIndex = rindex*((ncol+7)/8)+col/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(col%8);                  // start with 0x10000000 and shift right
      if(val[rindex-row_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   *  Sets a row of cells to new values starting at the given column. A cell is
   *  set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param  row
   * @param  col_offset  the colum offset
   * @param  val         the value
   */
  public void setRow(int row, int col_offset, int val[]) {
    if((row<0)||(row>=nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    if((col_offset<0)||(col_offset+val.length>ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    for(int cindex = col_offset, nc = val.length+col_offset; cindex<nc; cindex++) {
      int arrayIndex = row*((ncol+7)/8)+cindex/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(cindex%8);               // start with 0x10000000 and shift right
      if(val[cindex-col_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   *  Sets a row of cells to new values starting at the given column. A cell is
   *  set to 1 if the value is >0; the cell is set to zero otherwise
   *
   * @param  row
   * @param  col_offset  the colum offset
   * @param  val         the value
   */
  public void setRow(int row, int col_offset, byte val[]) {
    if((row<0)||(row>=nrow)) {
      throw new IllegalArgumentException("Row index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    if((col_offset<0)||(col_offset+val.length>ncol)) {
      throw new IllegalArgumentException("Column index out of range in binary lattice setRow."); //$NON-NLS-1$
    }
    for(int cindex = col_offset, nc = val.length+col_offset; cindex<nc; cindex++) {
      int arrayIndex = row*((ncol+7)/8)+cindex/8; // each row starts on a byte boundary
      byte packedcell = packedData[arrayIndex];
      int mask = 0x80>>>(cindex%8);               // start with 0x10000000 and shift right
      if(val[cindex-col_offset]<=0) {
        packedcell = (byte) (packedcell&~mask);   // AND bitwise with mask complement will clear index bit
      } else {
        packedcell = (byte) (packedcell|mask);    // OR bitwise with mask operation will set index bit
      }
      packedData[arrayIndex] = packedcell;
    }
  }

  /**
   *  Sets a lattice cell to a new value. A cell should take on a value of 0 or
   *  1.
   *
   * @param  _row
   * @param  _col
   * @param  val
   */
  public void setCell(int _row, int _col, int val) {
    if((_row<0)||(_row>=nrow)||(_col<0)||(_col>=ncol)) {
      throw new IllegalArgumentException("Cell row or column index out of range.  row="+_row+"  col="+_col); //$NON-NLS-1$ //$NON-NLS-2$
    }
    int arrayIndex = _row*((ncol+7)/8)+_col/8; // each row starts on a byte boundary
    byte packedcell = packedData[arrayIndex];
    int mask = 0x80>>>(_col%8);                // start with 0x10000000 and shift right
    if(val<=0) {
      packedcell = (byte) (packedcell&~mask); // AND bitwise with mask complement will clear index bit
    } else {
      packedcell = (byte) (packedcell|mask);  // OR bitwise with mask operation will set index bit
    }
    packedData[arrayIndex] = packedcell;
  }

  /**
   *  Gets a lattice cell value. Cell values are zero or one.
   *
   * @param  _row
   * @param  _col
   * @return       the cell value.
   */
  public byte getCell(int _row, int _col) {
    byte packedcell = packedData[_row*((ncol+7)/8)+_col/8]; // each row starts on a byte boundary
    int mask = 0x80>>>(_col%8);                             // start with 0x10000000 and shift right
    if((packedcell&mask)>0) {
      return 1;
    }
    return 0;
  }

  /**
   *  Sets the color palette. The color at the 0th index of the array is set to
   *  the zero color.
   *
   * @param  colors
   */
  public void setColorPalette(Color[] colors) {
    if(colors.length!=2) {
      throw new IllegalArgumentException("Array must have length of 2"); //$NON-NLS-1$
    }
    zeroColor = colors[0];
    oneColor = colors[1];
  }

  /**
   *  Sets the color for a single index.
   *
   * @param  i      the value to set the color for.
   * @param  color
   */
  public void setIndexedColor(int i, Color color) {
    if(i==0) {
      zeroColor = color;
    } else {
      oneColor = color;
    }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(nrow*ncol+nrow);
    //int row = 0;
    int column = 0;
    for(int i = 0, size = packedData.length; i<size; i++) {
      byte packedCell = packedData[i];
      for(int j = BITS_PER_BYTE; (j>0)&&(column<ncol); j--) {
        byte val = (byte) (packedCell>>>(j-1));
        int one_or_zero = (val&1);
        if(one_or_zero==0) {
          sb.append("0");    //$NON-NLS-1$
        } else {
          sb.append("1");    //$NON-NLS-1$
        }
        if(column==(ncol-1)) {
          if(i!=(size-1)) {
            sb.append("\n"); //$NON-NLS-1$
          }
          column = 0;
         // row++;
          break;
        }
        column++;
      }
    }
    return sb.toString();
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
