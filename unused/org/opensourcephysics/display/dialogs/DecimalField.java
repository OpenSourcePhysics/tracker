/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.dialogs;
import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.JTextField;

/**
 * This is a JTextField that accepts only decimal numbers.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DecimalField extends JTextField {
  // instance fields
  private NumberFormat format = NumberFormat.getInstance();
  private double prevValue;
  private Double maxValue;
  private Double minValue;

  /**
   * Constructs a DecimalField object.
   *
   * @param columns the number of columns available for text characters
   * @param places the number of decimal places to display
   */
  public DecimalField(int columns, int places) {
    super(columns);
    setDecimalPlaces(places);
  }

  /**
   * Gets the value from the text field.
   *
   * @return the value
   */
  public double getValue() {
    double retValue;
    try {
      retValue = format.parse(getText()).doubleValue();
      if((minValue!=null)&&(retValue<minValue.doubleValue())) {
        setValue(minValue.doubleValue());
        return minValue.doubleValue();
      }
      if((maxValue!=null)&&(retValue>maxValue.doubleValue())) {
        setValue(maxValue.doubleValue());
        return maxValue.doubleValue();
      }
    } catch(ParseException e) {
      Toolkit.getDefaultToolkit().beep();
      setValue(prevValue);
      return prevValue;
    }
    return retValue;
  }

  /**
   * Formats the specified value and enters it in the text field.
   *
   * @param value the value to be entered
   */
  public void setValue(double value) {
    if(minValue!=null) {
      value = Math.max(value, minValue.doubleValue());
    }
    if(maxValue!=null) {
      value = Math.min(value, maxValue.doubleValue());
    }
    setText(format.format(value));
    prevValue = value;
  }

  /**
   * Sets the decimal places for this field.
   *
   * @param places the number of decimal places to display
   */
  public void setDecimalPlaces(int places) {
    places = Math.abs(places);
    places = Math.min(places, 5);
    format.setMinimumIntegerDigits(1);
    format.setMinimumFractionDigits(places);
    format.setMaximumFractionDigits(places);
  }

  /**
   * Sets a minimum value for this field.
   *
   * @param min the minimum allowed value
   */
  public void setMinValue(double min) {
    minValue = new Double(min);
  }

  /**
   * Sets a maximum value for this field.
   *
   * @param max the maximum allowed value
   */
  public void setMaxValue(double max) {
    maxValue = new Double(max);
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
