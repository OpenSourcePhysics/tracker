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

/**
 * This is a NumberField that displays numbers in decimal format with a fixed
 * number of decimal places.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DecimalField extends NumberField {
	
	protected String defaultPattern;
	
  /**
   * Constructs a DecimalField.
   *
   * @param columns the number of character columns
   * @param places the number of decimal places to display
   */
  public DecimalField(int columns, int places) {
    super(columns);
    fixedPattern = fixedPatternByDefault = true;
    setDecimalPlaces(places);
  }

  /**
   * Sets the number of decimal places.
   *
   * @param places the number of decimal places to display
   */
  public void setDecimalPlaces(int places) {
    places = Math.min(places, 5);
    places = Math.max(places, 1);
    char d = '.';
    String pattern = "0"+d; //$NON-NLS-1$
    for(int i = 0; i<places; i++) {
      pattern += "0"; //$NON-NLS-1$
    }
    defaultPattern = pattern;
    if (userPattern.equals("")) { //$NON-NLS-1$
      format.applyPattern(pattern);
    }
  }

  // Override NumberField methods so pattern cannot change
  public void setSigFigs(int sigfigs) {}

  @Override
  public void setExpectedRange(double lower, double upper) {}

  @Override
  public void setFixedPattern(String pattern) {
  	if (pattern==null) pattern = ""; //$NON-NLS-1$
    pattern = pattern.trim();
    if (pattern.equals(userPattern)) return;
    userPattern = pattern;
    if (userPattern.equals("")) { //$NON-NLS-1$
      format.applyPattern(defaultPattern);
    }
    else {
      format.applyPattern(userPattern);
    }
    setValue(prevValue);
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
