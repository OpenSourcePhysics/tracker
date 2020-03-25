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
 * This NumberField displays numbers in scientific format.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ScientificField extends NumberField {
  private double zeroLimit = .0000000001;

  /**
   * Constructs a ScientificField with default sigfigs (4).
   *
   * @param columns the number of character columns
   */
  public ScientificField(int columns) {
    this(columns, 4);
  }

  /**
   * Constructs a ScientificField with specified sigfigs.
   *
   * @param columns the number of character columns
   * @param sigfigs the significant figures
   */
  public ScientificField(int columns, int sigfigs) {
    super(columns, sigfigs);
//    char d = format.getDecimalFormatSymbols().getDecimalSeparator();
    char d = '.';
    fixedPattern = fixedPatternByDefault = true;
    String s = ""; //$NON-NLS-1$
    for(int i = 0; i<this.sigfigs-1; i++) {
      s += "0"; //$NON-NLS-1$
    }
    format.applyPattern("0"+d+s+"E0"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Overrides NumberField setValue method.
   *
   * @param value the value to be entered
   */
  public void setValue(double value) {
    if(Math.abs(value)<zeroLimit) {
      value = 0;
    }
    super.setValue(value);
  }

  // Override NumberField methods so pattern cannot change
  public void setSigFigs(int sigfigs) {
    if(this.sigfigs==sigfigs) {
      return;
    }
    sigfigs = Math.max(sigfigs, 2);
    this.sigfigs = Math.min(sigfigs, 6);
  }

  public void setExpectedRange(double lower, double upper) {}

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
