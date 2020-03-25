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
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

public class ColorMapper {
  // color palette types
  private static final int CUSTOM = -1;
  public static final int SPECTRUM = 0;
  public static final int GRAYSCALE = 1;
  public static final int DUALSHADE = 2;
  public static final int RED = 3;
  public static final int GREEN = 4;
  public static final int BLUE = 5;
  public static final int BLACK = 6;
  public static final int WIREFRAME = 7;     // special SurfacePlotter palette
  public static final int NORENDER = 8;      // special SurfacePlotter palette
  public static final int REDBLUE_SHADE = 9; // special SurfacePlotter palette
  private Color[] colors;
  private double floor, ceil;
  private Color floorColor = Color.darkGray;
  private Color ceilColor = Color.lightGray;
  private int numColors;
  private int paletteType;
  private JFrame legendFrame;
  protected ZExpansion zMap = null;

  /**
   * Constructor ColorMapper
   * @param _numColors
   * @param _floor
   * @param _ceil
   * @param palette
   */
  public ColorMapper(int _numColors, double _floor, double _ceil, int palette) {
    floor = _floor;
    ceil = _ceil;
    numColors = _numColors;
    setPaletteType(palette); // default colors
  }

  public void updateLegend(ZExpansion zMap) {
    if((legendFrame!=null)&&legendFrame.isVisible()&&legendFrame.isDisplayable()) {
      if(zMap==null) {
        zMap = this.zMap; //use the local map if the parameter is null
      }
      showLegend(zMap);
    }
  }

  public JFrame getLegendFrame() {
    return legendFrame;
  } // added by Paco

  /**
   * Shows the color legend.
   */
  public JFrame showLegend() {
    if(zMap!=null) {
      return showLegend(zMap);
    }
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
    GridPointData pointdata = new GridPointData(numColors+2, 1, 1);
    double[][][] data = pointdata.getData();
    double delta = (ceil-floor)/(numColors);
    double cval = floor-delta/2;
    for(int i = 0, n = data.length; i<n; i++) {
      data[i][0][2] = cval;
      cval += delta;
    }
    pointdata.setScale(floor-delta, ceil+delta, 0, 1);
    GridPlot cb = new GridPlot(pointdata);
    cb.setShowGridLines(false);
    cb.setAutoscaleZ(false, floor, ceil);
    cb.setColorPalette(colors);
    cb.update();
    dp.addDrawable(cb);
    XAxis xaxis = new XAxis(""); //$NON-NLS-1$
    xaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    xaxis.setLocation(-0.5);
    xaxis.setEnabled(true);
    dp.addDrawable(xaxis);
    legendFrame.pack();
    legendFrame.setVisible(true);
    return legendFrame;
  }

  /**
   * Shows the color legend.
   */
  JFrame showLegend(ZExpansion zMap) {
    if(zMap==null) {
      return showLegend();
    }
    InteractivePanel dp = new InteractivePanel();
    dp.setPreferredSize(new java.awt.Dimension(300, 66));
    dp.setPreferredGutters(0, 0, 0, 35);
    dp.setClipAtGutter(false);
    if((legendFrame==null)||!legendFrame.isDisplayable()) {
      legendFrame = new JFrame(DisplayRes.getString("GUIUtils.Legend")); //$NON-NLS-1$
    }
    legendFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    legendFrame.setResizable(true);
    legendFrame.setContentPane(dp);
    int numColors = 256;
    if(paletteType==CUSTOM) {
      numColors = colors.length;
    }
    GridPointData pointdata = new GridPointData(numColors+2, 1, 1);
    double[][][] data = pointdata.getData();
    double delta = (ceil-floor)/(numColors);
    double cval = floor-delta/2;
    for(int i = 0, n = data.length; i<n; i++) {
      data[i][0][2] = zMap.evaluate(cval);
      cval += delta;
    }
    pointdata.setScale(floor-delta, ceil+delta, 0, 1);
    GridPlot cb = new GridPlot(pointdata);
    cb.setShowGridLines(false);
    cb.setAutoscaleZ(false, floor, ceil);
    if(paletteType==CUSTOM) {
      cb.setColorPalette(colors);
    } else {
      cb.setPaletteType(paletteType);
    }
    cb.update();
    dp.addDrawable(cb);
    XAxis xaxis = new XAxis(""); //$NON-NLS-1$
    xaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    xaxis.setLocation(-0.5);
    xaxis.setEnabled(true);
    dp.addDrawable(xaxis);
    legendFrame.pack();
    legendFrame.setVisible(true);
    return legendFrame;
  }

  /**
   * Sets the scale.
   * @param _floor
   * @param _ceil
   */
  public void setScale(double _floor, double _ceil) {
    floor = _floor;
    ceil = _ceil;
    if(zMap!=null) {
      zMap.setMinMax(floor, ceil);
    }
  }

  /**
   * Converts a double to color components.
   *
   * @param value double
   * @param rgb byte[]
   * @return byte[]
   */
  public byte[] doubleToComponents(double value, byte[] rgb) {
    if(zMap!=null) {
      value = zMap.evaluate(value);
    }
    Color color = doubleToColor(value);
    rgb[0] = (byte) color.getRed();
    rgb[1] = (byte) color.getGreen();
    rgb[2] = (byte) color.getBlue();
    return rgb;
  }

  /**
   * Converts a double to a color.
   * @param value
   * @return the color
   */
  public Color doubleToColor(double value) { // Changed by Paco to use doubleToIndex
    int index = doubleToIndex(value);
    if(index<0) return floorColor;
    if(index>=colors.length) return ceilColor;
    return colors[index];
  }

 /**
   * Converts a double to an index in the color array.
   * @param value
   * @return the index in the array with the following exceptions:
   * <ul>
   *   <li>-1 if floor color</li>
   *   <li>colors.length if ceil color</li>
   * </ul> 
   */
  public int doubleToIndex(double value) { // Added by Paco
    if(zMap!=null) {
      value = zMap.evaluate(value);
    }
    if((float) floor-(float) value>Float.MIN_VALUE) {
      return -1;
    } else if((float) value-(float) ceil>Float.MIN_VALUE) {
      return colors.length;
    }
    int index = (int) (colors.length*(value-floor)/(ceil-floor));
    index = Math.max(0, index);
    return Math.min(index, colors.length-1);
  }

  /**
   * Returns the color for an index
   */
  public Color indexToColor(int index) { // Added by Paco
    if (index<0) return floorColor;
    if (index>=colors.length) return ceilColor;
    return colors[index];
  }

  /**
   * Returns the thresholds for color change. One more than colors, includes ceil and floor
   */
  public double[] getColorThresholds() { // Added by Paco
    double[] thresholds = new double[colors.length+1];
    double delta = (ceil-floor)/colors.length;
    for (int i=0,n=colors.length; i<n; i++) thresholds[i] = floor + i*delta;
    thresholds[colors.length] = ceil;
    return thresholds;
  }
  
  /**
   * Sets map for z values.
   *
   * @param map ZExpansion
   */
  public void setZMap(ZExpansion map) {
    zMap = map;
    if(zMap!=null) {
      zMap.setMinMax(floor, ceil);
    }
  }

  /**
   * Gets the floor.
   * @return
   */
  public double getFloor() {
    return floor;
  }

  /**
   * Gets the floor color;
   * @return
   */
  public Color getFloorColor() {
    return floorColor;
  }

  /**
   * Gets the ceiling color.
   * @return
   */
  public double getCeil() {
    return ceil;
  }

  /**
   * Gets the ceiling color.
   * @return
   */
  public Color getCeilColor() {
    return ceilColor;
  }

  /**
   * Gets the number of colors between the floor and ceiling values.
   * @return
   */
  public int getNumColors() {
    return numColors;
  }

  /**
   * Sets the floor and ceiling colors.
   *
   * @param _floorColor
   * @param _ceilColor
   */
  public void setFloorCeilColor(Color _floorColor, Color _ceilColor) {
    floorColor = _floorColor;
    ceilColor = _ceilColor;
  }

  /**
   * Returns the color palette.
   * @return mode
   */
  public int getPaletteType() {
    return paletteType;
  }

  /**
   * Sets the color palette.
   * @param _colors
   */
  public void setColorPalette(Color[] _colors) {
    floorColor = Color.darkGray;
    ceilColor = Color.lightGray;
    colors = _colors;
    numColors = colors.length;
    paletteType = CUSTOM;
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
    if(paletteType==CUSTOM) {
      Color newColors[] = new Color[numColors];
      for(int i = 0, n = Math.min(colors.length, numColors); i<n; i++) {
        newColors[i] = colors[i];
      }
      for(int i = colors.length; i<numColors; i++) {
        newColors[i] = colors[colors.length-1];
      }
      colors = newColors;
    } else {
      setPaletteType(paletteType);
    }
  }

  /**
   * Sets the color palette.
   * @param _paletteType
   */
  public void setPaletteType(int _paletteType) {
    paletteType = _paletteType;
    floorColor = Color.darkGray;
    ceilColor = Color.lightGray;
    if((paletteType==GRAYSCALE)||(paletteType==BLACK)) {
      floorColor = new Color(64, 64, 128);
      ceilColor = new Color(255, 191, 191);
    }
    colors = getColorPalette(numColors, paletteType);
    numColors = Math.max(2, numColors); // need at least 2 colors
  }

  /**
   * Gets a array of colors for use in data visualization.
   *
   * Colors are similar to the colors returned by a color mapper instance.
   * @param numColors
   * @param paletteType
   * @return
   */
  static public Color[] getColorPalette(int numColors, int paletteType) {
    if(numColors<2) {
      numColors = 2;
    }
    Color colors[] = new Color[numColors];
    for(int i = 0; i<numColors; i++) {
      float level = (float) i/(numColors-1)*0.8f;
      int r = 0, b = 0;
      switch(paletteType) {
         case ColorMapper.REDBLUE_SHADE :
           r = (Math.max(0, -numColors-1+i*2)*255)/(numColors-1);
           b = (Math.max(0, numColors-1-i*2)*255)/(numColors-1);
           colors[i] = new Color(r, 0, b);
           break;
         case ColorMapper.SPECTRUM :
           level = 0.8f-level;
           colors[i] = Color.getHSBColor(level, 1.0f, 1.0f);
           break;
         case ColorMapper.GRAYSCALE :
         case ColorMapper.BLACK :
           colors[i] = new Color(i*255/(numColors-1), i*255/(numColors-1), i*255/(numColors-1));
           break;
         case ColorMapper.RED :
           colors[i] = new Color(i*255/(numColors-1), 0, 0);
           break;
         case ColorMapper.GREEN :
           colors[i] = new Color(0, i*255/(numColors-1), 0);
           break;
         case ColorMapper.BLUE :
           colors[i] = new Color(0, 0, i*255/(numColors-1));
           break;
         case ColorMapper.DUALSHADE :
         default :
           level = (float) i/(numColors-1);
           colors[i] = Color.getHSBColor(0.8f*(1-level), 1.0f, 0.2f+1.6f*Math.abs(0.5f-level));
           break;
      }
    }
    return colors;
  }

  /**
   * Gets a loader that allows a Circle to be represented as XML data.
   * Objects without XML loaders cannot be saved and retrieved from an XML file.
   *
   * @return ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new ColorMapperLoader();
  }

  /**
   * A class to save and load Circle objects in an XMLControl.
   */
  private static class ColorMapperLoader extends XMLLoader {
    /**
     * Saves the ColorMapper's data in the xml control.
     * @param control XMLControl
     * @param obj Object
     */
    public void saveObject(XMLControl control, Object obj) {
      ColorMapper mapper = (ColorMapper) obj;
      control.setValue("palette type", mapper.paletteType);   //$NON-NLS-1$
      control.setValue("number of colors", mapper.numColors); //$NON-NLS-1$
      control.setValue("floor", mapper.floor);                //$NON-NLS-1$
      control.setValue("ceiling", mapper.ceil);               //$NON-NLS-1$
      control.setValue("floor color", mapper.floorColor);     //$NON-NLS-1$
      control.setValue("ceiling color", mapper.ceilColor);    //$NON-NLS-1$
      if(mapper.paletteType==CUSTOM) {
        control.setValue("colors", mapper.colors); //$NON-NLS-1$
      }
    }

    /**
     * Creates a ColorMapper.
     * @param control XMLControl
     * @return Object
     */
    public Object createObject(XMLControl control) {
      return new ColorMapper(100, -1, 1, ColorMapper.SPECTRUM);
    }

    /**
     * Loads data from the xml control into the ColorMapper object.
     * @param control XMLControl
     * @param obj Object
     * @return Object
     */
    public Object loadObject(XMLControl control, Object obj) {
      ColorMapper mapper = (ColorMapper) obj;
      int paletteType = control.getInt("palette type");   //$NON-NLS-1$
      int numColors = control.getInt("number of colors"); //$NON-NLS-1$
      double floor = control.getDouble("floor");          //$NON-NLS-1$
      double ceil = control.getDouble("ceiling");         //$NON-NLS-1$
      if(paletteType==CUSTOM) {
        Color[] colors = (Color[]) control.getObject("colors"); //$NON-NLS-1$
        mapper.setColorPalette(colors);
      } else {
        mapper.setPaletteType(paletteType);
        mapper.setNumberOfColors(numColors);
      }
      mapper.setScale(floor, ceil);
      Color floorColor = (Color) control.getObject("floor color");  //$NON-NLS-1$
      Color ceilColor = (Color) control.getObject("ceiling color"); //$NON-NLS-1$
      mapper.setFloorCeilColor(floorColor, ceilColor);
      return obj;
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
