/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.Toolkit;

/**
 * This is a NumberField that accepts only integers.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class IntegerField extends NumberField {
  /**
   * Constructs an IntegerField.
   *
   * @param columns the number of columns available for text characters
   */
  public IntegerField(int columns) {
    super(columns);
    fixedPattern = fixedPatternByDefault = true;
    format.setParseIntegerOnly(true);
    setIntValue((int) prevValue);
  }

  /**
   * Gets the integer value from the text field.
   *
   * @return the value
   */
  public int getIntValue() {
    int retValue;
    String s = getText().trim();
    // strip units, if any
    if((units!=null)&&!units.equals("")) { //$NON-NLS-1$
      int n = s.indexOf(units);
      while(n>-1) {
        s = s.substring(0, n);
        n = s.indexOf(units);
      }
    }
    if(s.equals(format.format(prevValue))) {
      return(int) prevValue;
    }
    try {
      retValue = Integer.parseInt(s);
//      retValue = format.parse(s).intValue();
      if((minValue!=null)&&(retValue<minValue.intValue())) {
        setIntValue(minValue.intValue());
        return minValue.intValue();
      }
      if((maxValue!=null)&&(retValue>maxValue.intValue())) {
        setIntValue(maxValue.intValue());
        return maxValue.intValue();
      }
    } catch(Exception e) {
      Toolkit.getDefaultToolkit().beep();
      setIntValue((int) prevValue);
      return(int) prevValue;
    }
    return retValue;
  }

  /**
   * Sets the integer value.
   *
   * @param value the value
   */
  public void setIntValue(int value) {
    if(minValue!=null) {
      value = Math.max(value, minValue.intValue());
    }
    if(maxValue!=null) {
      value = Math.min(value, maxValue.intValue());
    }
    String s = format.format(value);
    if(units!=null) {
      s += units;
    }
    if(!s.equals(getText())) {
      setText(s);
    }
    prevValue = value;
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
