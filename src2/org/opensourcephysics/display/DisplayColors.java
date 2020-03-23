/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;

/**
 * Defines  color palette used by OSP components.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class DisplayColors {
  static Color[] phaseColors = null;
  //static Color[] lineColors = {Color.red, Color.green, Color.blue, Color.yellow.darker(), Color.cyan, Color.magenta};
  //static Color[] markerColors = {Color.black, Color.blue, Color.red, Color.green, Color.darkGray, Color.lightGray};
  static java.util.Dictionary<Integer, Color> lineColors = new java.util.Hashtable<Integer, Color>();
  static java.util.Dictionary<Integer, Color> markerColors = new java.util.Hashtable<Integer, Color>();

  //   static{
  //     lineColors.put(0, Color.RED);
  //     lineColors.put(1, Color.GREEN);
  //     lineColors.put(2, Color.BLUE);
  //     lineColors.put(3, Color.YELLOW.darker());
  //     lineColors.put(4, Color.CYAN.darker());
  //     lineColors.put(5, Color.MAGENTA.darker());
  //     
  //     markerColors.put(0, Color.RED.brighter().brighter());
  //     markerColors.put(1, Color.GREEN.brighter().brighter());
  //     markerColors.put(2, Color.BLUE.brighter().brighter());
  //     markerColors.put(3, Color.YELLOW.brighter());
  //     markerColors.put(4, Color.CYAN.brighter());
  //     markerColors.put(5, Color.MAGENTA.brighter());
  //   }
  static {
    lineColors.put(0, Color.RED);
    lineColors.put(1, Color.GREEN.darker());
    lineColors.put(2, Color.BLUE);
    lineColors.put(3, Color.YELLOW.darker());
    lineColors.put(4, Color.CYAN.darker());
    lineColors.put(5, Color.MAGENTA);
    markerColors.put(0, Color.RED);
    markerColors.put(1, Color.GREEN.darker());
    markerColors.put(2, Color.BLUE);
    markerColors.put(3, Color.YELLOW.darker());
    markerColors.put(4, Color.CYAN.darker());
    markerColors.put(5, Color.MAGENTA);
  }

  private DisplayColors() {}

  /**
   * Gets an array of colors.
   *
   * @return the color array
   */
  public static Color[] getPhaseToColorArray() {
    if(phaseColors==null) {
      phaseColors = new Color[256];
      for(int i = 0; i<256; i++) {
        double val = Math.abs(Math.sin(Math.PI*i/255));
        int b = (int) (255*val*val);
        val = Math.abs(Math.sin(Math.PI*i/255+Math.PI/3));
        int g = (int) (255*val*val*Math.sqrt(val));
        val = Math.abs(Math.sin(Math.PI*i/255+2*Math.PI/3));
        int r = (int) (255*val*val);
        phaseColors[i] = new Color(r, g, b);
      }
    }
    return phaseColors;
  }

  /**
   * Converts a phase angle in the range [-Pi,Pi] to a color.
   *
   * @param phi phase angle
   * @return the color
   */
  public static Color phaseToColor(double phi) {
    int index = (int) (127.5*(1+phi/Math.PI));
    index = index%255;
    if(phaseColors==null) {
      return getPhaseToColorArray()[index];
    }
    return phaseColors[index];
  }

  /**
   * Gets a random color.
   *
   * @return random color
   */
  public static Color randomColor() {
    return new Color((int) (Math.random()*255), (int) (Math.random()*255), (int) (Math.random()*255));
  }

  /**
   * Gets a line color that matches the index.
   * @param index int
   * @return Color
   */
  static public Color getLineColor(int index) {
    Color color = lineColors.get(index);
    if(color==null) {                           // create and store a new color
      float h = ((float) (index*Math.PI/12))%1; // each increment moves slightly more than 1/4 turn around color wheel
      float s = 1.0f;                           // maximum saturation for vibrant colors
      float b = 0.5f;                           // lines are often thin and should be darker for visibility on light backgrounds
      color = Color.getHSBColor(h, s, b);
      lineColors.put(index, color);
    }
    return color;
  }

  /**
   * Gets a marker color that matches the index.
   * @param index int
   * @return Color
   */
  static public Color getMarkerColor(int index) {
    Color color = markerColors.get(index);
    if(color==null) { // create and store a new color
      color = getLineColor(index).brighter().brighter();
      markerColors.put(index, color);
    }
    return color;
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
