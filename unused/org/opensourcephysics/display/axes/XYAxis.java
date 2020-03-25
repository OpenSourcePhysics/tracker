/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import org.opensourcephysics.display.DrawableTextLine;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.TextLine;

/**
 * A superclass for the x axis and y axis.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
abstract public class XYAxis implements Interactive {
  /** Field DRAW_IN_DISPLAY           */
  public static final int DRAW_IN_DISPLAY = 0;

  /** Field DRAW_IN_GUTTER           */
  public static final int DRAW_IN_GUTTER = 1;

  /** Field DRAW_AT_LOCATION           */
  public static final int DRAW_AT_LOCATION = 2;

  /** Field LINEAR           */
  public static final int LINEAR = 0;
  protected double x = 0;
  protected double y = 0;
  boolean enabled = false;                                      // enables interaction

  /** Field LOG10           */
  public static final int LOG10 = 1;
  int locationType = DRAW_IN_DISPLAY;
  int axisType = LINEAR;
  String logBase = "10";                                        //$NON-NLS-1$
  DecimalFormat labelFormat = new DecimalFormat("0.0");         //$NON-NLS-1$
  DecimalFormat integerFormat = new DecimalFormat("000");       //$NON-NLS-1$
  double label_step = -14;
  double label_start = 2;
  DrawableTextLine axisLabel = new DrawableTextLine("x", 0, 0); //$NON-NLS-1$
  Font labelFont = new Font("Dialog", Font.PLAIN, 12);          //$NON-NLS-1$
  int label_exponent = 0;
  String label_string[] = new String[0];                        // String to contain the labels.
  double label_value[] = new double[0];                         // The actual values of the axis labels
  double decade_multiplier = 1;
  int label_count = 0;                                          // The number of labels
  double location = 0;                                          // The position of the axis
  Font titleFont = new Font("Dialog", Font.PLAIN, 12);          //$NON-NLS-1$
  boolean showMajorGrid = false;
  // Color majorGridColor = new Color(223, 223, 223);  // light gray

  Color majorGridColor = new Color(0, 0, 0, 32);                // light gray

  /**
   * Constructor XYAxis
   */
  public XYAxis() {
    axisLabel.setJustification(TextLine.CENTER);
    axisLabel.setFont(labelFont);
  }

  /**
   * Draws the axis in a drawing panel.
   * @param panel
   * @param g
   */
  abstract public void draw(DrawingPanel panel, Graphics g);

  /**
   * Method setLabelFormat
   *
   * @param format
   */
  public void setLabelFormat(DecimalFormat format) {
    if(format!=null) {
      labelFormat = format;
    }
  }

  /**
   * Method setLabelFormat
   *
   * @param formatString
   */
  public void setLabelFormat(String formatString) {
    labelFormat = new DecimalFormat(formatString);
  }

  /**
   * Sets the location type.
   *
   * @param _locationType
   */
  public void setLocationType(int _locationType) {
    locationType = _locationType;
  }

  /**
   * Sets the location type.
   *
   * @param _location
   */
  public void setLocation(double _location) {
    location = _location;
  }

  /**
   * Method setAxisType
   *
   * @param type
   */
  public void setAxisType(int type) {
    axisType = type;
  }

  /**
   * Sets the title.
   *
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param title
   * @param font_name an optional font name
   */
  public void setTitle(String title, String font_name) {
    axisLabel.setText(title);
    if((font_name==null)||font_name.equals("")) { //$NON-NLS-1$
      return;
    }
    axisLabel.setFont(Font.decode(font_name));
  }

  /**
   * Sets the title.
   * @param title
   */
  public void setTitle(String title) {
    axisLabel.setText(title);
  }

  /**
   *  Set the title font. The font names understood are those understood by
   *  java.awt.Font.decode().
   *
   * @param  name  A font name.
   */
  public void setTitleFont(String name) {
    if((name!=null)&&!name.equals("")) { //$NON-NLS-1$
      titleFont = Font.decode(name);
    }
  }

  public void setShowMajorGrid(boolean show) {
    showMajorGrid = show;
  }

  protected void drawMultiplier(int xpix, int ypix, int exponent, Graphics2D g2) {
    Font oldFont = g2.getFont();
    g2.drawString("10", xpix, ypix);             //$NON-NLS-1$
    g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9.0f));
    g2.drawString(""+exponent, xpix+16, ypix-6); //$NON-NLS-1$
    g2.setFont(oldFont);
  }

  /**
   * Calculates the axis labels.
   * @param minimum
   * @param maximum
   * @param numTicks
   */
  public void calculateLabels(double minimum, double maximum, int numTicks) {
    numTicks = Math.min(19, numTicks);
    double min = minimum, max = maximum;
    if(maximum<minimum) {
      // the routines only work for min<max so switch values
      min = maximum;
      max = minimum;
    } else {
      max = maximum;
      min = minimum;
    }
    switch(axisType) {
       case LINEAR :
         calculateLinearLabels(min, max, numTicks);
         break;
       case LOG10 :
         calculateLogLabels(min, max, numTicks);
         break;
       default :
         calculateLinearLabels(min, max, numTicks);
         break;
    }
  }

  /**
   * Calculates the axis labels for logarithmic scales.
   * @param minimum
   * @param maximum
   */
  private void calculateLogLabels(double minimum, double maximum, int numTicks) {
    label_exponent = 0;
    decade_multiplier = 1;
    int label_step = 1; // shadow the member variables since we want to do integer arithmetic
    int label_start = (int) Math.ceil(minimum);
    if(label_start-minimum>0.998) {
      label_start -= 1;
    }
    int val = label_start;
    label_count = 1;
    do { // make sure that we get at least one decade
      val += label_step;
      label_count++;
    } while(val<=maximum-label_step);
    label_string = new String[label_count];
    label_value = new double[label_count];
    for(int i = 0; i<label_count; i++) {
      val = label_start+i*label_step;
      label_string[i] = integerFormat.format(val);
      label_value[i] = val;
    }
  }

  /**
   * put your documentation comment here
   * @param minimum
   * @param maximum
   */
  private void calculateLinearLabels(double minimum, double maximum, int numTicks) {
    // System.out.println("maximum="+maximum);
    double val;
    int i;
    int j;
    if((Math.abs(minimum)==0)&&(Math.abs(maximum)==0)) {
      maximum = minimum+1.0e-6;
    }
    if(Math.abs(minimum)>Math.abs(maximum)) {
      label_exponent = ((int) Math.floor(log10(Math.abs(minimum))/2.0))*2;
    } else {
      label_exponent = ((int) Math.floor(log10(Math.abs(maximum))/2.0))*2;
    }
    if((maximum-minimum)>10*numTicks*Double.MIN_VALUE) {
      label_step = RoundUp((maximum-minimum)/numTicks); // try for 8 labels
    } else {
      label_step = 1;
    }
    label_start = Math.floor(minimum/label_step)*label_step;
    while((label_step>0)&&(label_start<minimum)) {
      label_start += label_step;
    }
    val = label_start;
    label_count = 1;
    while(val<=maximum-label_step) {
      val += label_step;
      label_count++;
    }
    label_string = new String[label_count];
    label_value = new double[label_count];
    for(i = 0; i<label_count; i++) {
      val = label_start+i*label_step;
      if(label_exponent<0) {
        for(j = label_exponent; j<0; j++) {
          val *= 10;
        }
      } else {
        for(j = 0; j<label_exponent; j++) {
          val /= 10;
        }
      }
      label_string[i] = labelFormat.format(val);
      label_value[i] = val;
    }
    decade_multiplier = 1;
    if(label_exponent<0) {
      for(j = label_exponent; j<0; j++) {
        decade_multiplier /= 10;
      }
    } else {
      for(j = 0; j<label_exponent; j++) {
        decade_multiplier *= 10;
      }
    }
  }

  /**
   * Round up the passed value to a NICE value.
   */
  private double RoundUp(double val) {
    int exponent;
    int i;
    exponent = (int) (Math.floor(log10(val)));
    if(exponent<0) {
      for(i = exponent; i<0; i++) {
        val *= 10.0;
      }
    } else {
      for(i = 0; i<exponent; i++) {
        val /= 10.0;
      }
    }
    if(val>5.0) {
      val = 10.0;
    } else if(val>2.0) {
      val = 5.0;
    } else if(val>1.0) {
      val = 2.0;
    } else {
      val = 1.0;
    }
    if(exponent<0) {
      for(i = exponent; i<0; i++) {
        val /= 10.0;
      }
    } else {
      for(i = 0; i<exponent; i++) {
        val *= 10.0;
      }
    }
    return val;
  }

  // implements measurable interface

  /**
   * Gets the minimum x needed to draw this object.
   * @return minimum
   */
  public double getXMin() {
    return 0;
  }

  /**
   * Gets the maximum x needed to draw this object.
   * @return maximum
   */
  public double getXMax() {
    return 0;
  }

  /**
   * Gets the minimum y needed to draw this object.
   * @return minimum
   */
  public double getYMin() {
    return 0;
  }

  /**
   * Gets the maximum y needed to draw this object.
   * @return minimum
   */
  public double getYMax() {
    return 0;
  }

  /**
   * Determines if information is available to set min/max values.
   * Objects that store data, Datasets for example, usually return zero if data is null.
   *
   * @return true if min/max values are valid
   */
  public boolean isMeasured() {
    return false;
  }

  // implements interactive drawable interface
  public Interactive findInteractive(DrawingPanel panel, int _xpix, int _ypix) {
    return null;
  }

  public void setEnabled(boolean _enabled) {
    enabled = _enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setXY(double x, double y) {}

  public void setX(double x) {}

  public void setY(double y) {}

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  /**
   * @param x a double value
   * @return The log<sub>10</sub>
   *
   * @throws ArithmeticException
   */
  static public double log10(double x) throws ArithmeticException {
    if(x<=0.0) {
      throw new ArithmeticException("range exception"); //$NON-NLS-1$
    }
    return Math.log(x)/2.30258509299404568401;
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
