/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

public class VectorColorMapper {
  public static final int SPECTRUM = 0;
  public static final int RED = 1;
  public static final int BLUE = 2;
  public static final int GREEN = 3;
  public static final int GRAY = 4;
  public static final int BLACK = 5; // solid black
  private static final Color RED_COMP;
  private static final Color GREEN_COMP;
  private static final Color BLUE_COMP;
  private Color background = Color.WHITE;
  private Color[] colors;
  private Color[] compColors;
  private double ceil, floor;
  private int numColors;
  private int paletteType;
  private JFrame legendFrame;
  private VectorPlot legendPlot;
  private InteractivePanel legendPanel;

  static {
    float[] hsb = Color.RGBtoHSB(255, 0, 0, null);
    RED_COMP = Color.getHSBColor((hsb[0]+0.5f)%1, hsb[1], hsb[2]);   // complement red hue
    hsb = Color.RGBtoHSB(0, 255, 0, null);
    GREEN_COMP = Color.getHSBColor((hsb[0]+0.5f)%1, hsb[1], hsb[2]); // complement green hue
    hsb = Color.RGBtoHSB(0, 255, 0, null);
    BLUE_COMP = Color.getHSBColor((hsb[0]+0.5f)%1, hsb[1], hsb[2]);  // complement blue hue
  }

  /**
   * Constructor VectorColorMapper
   * @param _numColors
   * @param _ceil
   */
  public VectorColorMapper(int _numColors, double _ceil) {
    ceil = _ceil;
    numColors = _numColors;
    floor = (numColors<2) ? 0 : ceil/(numColors-1);
    paletteType = SPECTRUM;
    createSpectrumPalette(); // default colors
  }

  /**
   * Sets the number of colors
   * @param _numColors
   */
  public void setNumberOfColors(int _numColors) {
    if(_numColors==numColors) {
      return;
    }
    numColors = _numColors;
    floor = (numColors<2) ? 0 : ceil/(numColors-1);
    setPaletteType(paletteType);
  }

  public double getFloor() {
    return floor;
  }

  public double getCeiling() {
    return ceil;
  }

  /**
 * Sets the color palette.
 * @param _paletteType
 */
  public void setPaletteType(int _paletteType) {
    if(paletteType==_paletteType && numColors==colors.length) {
      return;
    }
    floor = (numColors<2) ? 0 : ceil/(numColors-1);
    paletteType = _paletteType;
    switch(paletteType) { // computes new palette
       case RED :
         createRedSpectrumPalette();
         return;
       case BLUE :
         createBlueSpectrumPalette();
         return;
       case GREEN :
         createGreenSpectrumPalette();
         return;
       case BLACK :       // always black; no need to compute anything
         return;
       case GRAY :
         createGraySpectrumPalette();
         return;
       default :          // spectrum of colors from light blue toward blue toward red toward black
         createSpectrumPalette();
    }
  }

  /**
   * Checks to see if background color matches the color palette background.
   * @param backgroundColor
   */
  public void checkPallet(Color backgroundColor) {
    if(background==backgroundColor) {
      return; // backgrounds match
    }
    background = backgroundColor;
    switch(paletteType) { // compute palette using new background
       case RED :
         createRedSpectrumPalette();
         return;
       case BLUE :
         createBlueSpectrumPalette();
         return;
       case GREEN :
         createGreenSpectrumPalette();
         return;
       case BLACK :       // always black; no need to compute anything
         return;
       case GRAY :
         createGraySpectrumPalette();
         return;
       default :          // spectrum of colors from light blue toward blue toward red toward black
         createSpectrumPalette();
    }
  }

  /**
   * Sets the scale.
   *
   * @param _ceil
   */
  public void setScale(double _ceil) {
    ceil = _ceil;
    floor = (numColors<2) ? 0 : ceil/(numColors-1);
  }

  /**
   * Converts a double to a the complementary color.
   * @param mag
   * @return the color
   */
  public Color doubleToCompColor(double mag) {
    if(mag<=floor) { // magnitudes less than floor are clear
      return background;
    }
    int index;
    switch(paletteType) {
       case RED :
         if(mag>=ceil) {
           return RED_COMP;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return compColors[index];                                               // shade of red
       case BLUE :
         if(mag>=ceil) {
           return BLUE_COMP;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return compColors[index];                                               // shade of blue
       case GREEN :
         if(mag>=ceil) {
           return GREEN_COMP;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return compColors[index];                                               // shade of green
       case BLACK :                                                              // complement of BLACK is WHITE
         return Color.WHITE;
       case GRAY :                                                               // cannot complement gray
         if(mag>=ceil) {
           return Color.black;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return colors[index];                                                   // shade of gray from white
       default :                                                                 // spectrum of colors from light blue toward blue toward red toward black
         Color c = getSpectrumColor(mag);
         float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
         float[] hsbBack = Color.RGBtoHSB(background.getRed(), background.getGreen(), background.getBlue(), null);
         return Color.getHSBColor((2*hsbBack[0]-hsb[0]+0.5f)%1, hsb[1], hsb[2]); // complementary hue
    }
  }

  /**
   * Converts a double to a color.
   * @param mag
   * @return the color
   */
  public Color doubleToColor(double mag) {
    if(mag<=floor) { // magnitudes less than floor are clear
      return background;
    }
    int index;
    switch(paletteType) {
       case RED :
         if(mag>=ceil) {
           return Color.RED;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return colors[index]; // shade of red
       case BLUE :
         if(mag>=ceil) {
           return Color.BLUE;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return colors[index]; // shade of blue
       case GREEN :
         if(mag>=ceil) {
           return Color.GREEN;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return colors[index]; // shade of green
       case BLACK :            // always black
         return Color.black;
       case GRAY :
         if(mag>=ceil) {
           return Color.black;
         }
         index = (int) ((numColors-1)*(mag/ceil));
         return colors[index]; // shade of gray from white
       default :               // spectrum of colors
         return getSpectrumColor(mag);
    }
  }

  private Color getSpectrumColor(double mag) {
    if((background==Color.BLACK)&&(mag>=ceil)) { // magnitude greater than max tends from RED toward pure WHITE
      int s = (int) (255*(1-ceil/mag));
      return new Color(255, s, s);
    } else if(mag>=ceil) {                       // magnitude greater than max tends from RED toward pure BLACK
      return new Color((int) (255.0*ceil/mag), 0, 0);
    }
    int index = (int) ((numColors-1)*(mag/ceil));
    return colors[index];
  }

  private void createRedSpectrumPalette() {
    colors = new Color[numColors];
    compColors = new Color[numColors];
    int bgr = background.getRed();
    int bgg = background.getGreen();
    int bgb = background.getBlue();
    for(int i = 0; i<numColors; i++) {                              // start with the background and increase toward pure RED
      int tr = bgr+(255-bgr)*i/numColors;                           // increase the red
      int tg = bgg-bgg*i/numColors;                                 // decrease the green
      int tb = bgb-bgb*i/numColors;                                 // decrease the blue
      colors[i] = new Color(tr, tg, tb);
      float[] hsb = Color.RGBtoHSB(tr, tg, tb, null);
      Color c = Color.getHSBColor((hsb[0]+0.5f)%1, hsb[1], hsb[2]); // complementary hue
      tr = bgr+(c.getRed()-bgr)*i/numColors;
      tg = bgg+(c.getGreen()-bgg)*i/numColors;
      tb = bgb+(c.getBlue()-bgb)*i/numColors;
      compColors[i] = new Color(tr, tg, tb);
    }
  }

  private void createGreenSpectrumPalette() {
    colors = new Color[numColors];
    compColors = new Color[numColors];
    int bgr = background.getRed();
    int bgg = background.getGreen();
    int bgb = background.getBlue();
    for(int i = 0; i<numColors; i++) {                              // start with the background and increase toward pure GREEN
      int tr = bgr-bgr*i/numColors;                                 // decrease the red
      int tg = bgg+(255-bgg)*i/numColors;                           // increase the green
      int tb = bgb-bgb*i/numColors;                                 // decrease the blue
      colors[i] = new Color(tr, tg, tb);
      float[] hsb = Color.RGBtoHSB(tr, tg, tb, null);
      Color c = Color.getHSBColor((hsb[0]+0.5f)%1, hsb[1], hsb[2]); // complementary hue
      tr = bgr+(c.getRed()-bgr)*i/numColors;
      tg = bgg+(c.getGreen()-bgg)*i/numColors;
      tb = bgb+(c.getBlue()-bgb)*i/numColors;
      compColors[i] = new Color(tr, tg, tb);
    }
  }

  private void createBlueSpectrumPalette() {
    colors = new Color[numColors];
    compColors = new Color[numColors];
    int bgr = background.getRed();
    int bgg = background.getGreen();
    int bgb = background.getBlue();
    for(int i = 0; i<numColors; i++) {                              // start with the background and increase toward pure BLUE
      int tr = bgr-bgr*i/numColors;                                 // decrease the red
      int tg = bgg-bgg*i/numColors;                                 // decrease the green
      int tb = bgb+(255-bgb)*i/numColors;                           // increase the blue
      colors[i] = new Color(tr, tg, tb);
      float[] hsb = Color.RGBtoHSB(tr, tg, tb, null);
      Color c = Color.getHSBColor((hsb[0]+0.5f)%1, hsb[1], hsb[2]); // complementary hue
      tr = bgr+(c.getRed()-bgr)*i/numColors;
      tg = bgg+(c.getGreen()-bgg)*i/numColors;
      tb = bgb+(c.getBlue()-bgb)*i/numColors;
      compColors[i] = new Color(tr, tg, tb);
    }
  }

  private void createGraySpectrumPalette() {
    compColors = colors = new Color[numColors]; // cannot complement gray color
    if(background==Color.BLACK) {  // special case; increase toward white
      for(int i = 0; i<numColors; i++) {
        int sat = 255*i/numColors; // increase the saturation
        colors[i] = new Color(sat, sat, sat);
      }
      return;
    }
    int bgr = background.getRed();
    int bgg = background.getGreen();
    int bgb = background.getBlue();
    for(int i = 0; i<numColors; i++) { // start with the background and increase toward BLACK
      int tr = bgr-bgr*i/numColors;    // decrease the red
      int tg = bgg-bgg*i/numColors;    // decrease the green
      int tb = bgb-bgb*i/numColors;    // decrease the blue
      colors[i] = new Color(tr, tg, tb);
    }
  }

  private void createSpectrumPalette() {
    compColors = colors = new Color[numColors]; // use same colors for complement
    int n1 = numColors/3;
    n1 = Math.max(1, n1);
    int bgr = background.getRed();
    int bgg = background.getGreen();
    int bgb = background.getBlue();
    for(int i = 0; i<n1; i++) { // start with the background and increase toward all blue
      int tr = bgr-bgr*i/n1;
      int tg = bgg-bgg*i/n1;
      int tb = Math.min(255,bgg+(255-bgb)*i/n1);
      colors[i] = new Color(tr, tg, tb);
    }
    for(int i = n1; i<numColors; i++) { // decrease blue and increase green and then red
      double sigma = n1/1.2;
      double arg1 = (i-n1)/sigma;
      double arg2 = (i-2*n1)/sigma;
      double arg3 = (i-numColors)/sigma;
      int b = (int) (255*Math.exp(-arg1*arg1));
      int g = (int) (255*Math.exp(-arg2*arg2));
      int r = (int) (255*Math.exp(-arg3*arg3));
      r = Math.min(255, r);
      b = Math.min(255, b);
      g = Math.min(255, g);
      colors[i] = new Color(r, g, b);
    }
  }

  public JFrame showLegend() {
    double floor = 0;
    double ceil = this.ceil*2;
    legendPanel = new InteractivePanel();
    legendPanel.setPreferredSize(new java.awt.Dimension(300, 120));
    legendPanel.setPreferredGutters(0, 0, 0, 35);
    legendPanel.setClipAtGutter(false);
    legendPanel.setSquareAspect(false);
    if((legendFrame==null)||!legendFrame.isDisplayable()) {
      legendFrame = new JFrame(DisplayRes.getString("GUIUtils.Legend")); //$NON-NLS-1$
    }
    legendFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    legendFrame.setResizable(true);
    legendFrame.setContentPane(legendPanel);
    int numVecs = 30;
    GridPointData pointdata = new GridPointData(numVecs, 2, 3);
    double[][][] data = pointdata.getData();
    double delta = 1.5*ceil/numVecs;
    double cval = floor-delta/2;
    for(int i = 0, n = data.length; i<n; i++) {
      data[i][1][2] = cval;
      data[i][1][3] = 0;
      data[i][1][4] = 4;
      cval += delta;
    }
    pointdata.setScale(0, 1.5*ceil+delta, 0, 1);
    legendPlot = new VectorPlot(pointdata);
    legendPlot.setPaletteType(paletteType);
    legendPlot.setAutoscaleZ(false, 0.5*ceil, ceil);
    legendPlot.update();
    legendPanel.addDrawable(legendPlot);
    XAxis xaxis = new XAxis(""); //$NON-NLS-1$
    xaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    xaxis.setLocation(-0.0);
    xaxis.setEnabled(true);
    legendPanel.addDrawable(xaxis);
    legendFrame.pack();
    legendFrame.setVisible(true);
    return legendFrame;
  }

  public JFrame getLegendFrame() {
    return legendFrame;
  }

  public void updateLegend() {
    if(legendPlot==null) {
      return;
    }
    legendPlot.setPaletteType(paletteType);
    legendPlot.setAutoscaleZ(false, 0.5*ceil, ceil);
    legendPlot.update();
    legendPanel.repaint();
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
