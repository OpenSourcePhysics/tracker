/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Indicates that an error occured in a numeric method.
 */
public final class NumericMethodException extends RuntimeException {
  /** Field error_value stores an optional numeric error.  */
  public double error_value;

  /** Field error_code sotes an optional error code       */
  public int error_code;

  /**
   * Constructs a <code>RuntimeException</code> with no detail  message.
     */
  public NumericMethodException() {
    super();
  }

  /**
   * Constructs a <code>RuntimeException</code> with the specified
   * detail message.
   *
   * @param  msg   the detail message.
   */
  public NumericMethodException(String msg) {
    super(msg);
  }

  /**
   * Constructs a <code>RuntimeException</code> with the specified
   * detail message, error code, and error estimate.
   *
   * @param msg
   * @param code
   * @param val
   */
  public NumericMethodException(String msg, int code, double val) {
    super(msg);
    error_code = code;
    error_value = val;
  }

  /**
   * Returns the error message string of this throwable object.
   *
   *
   */
  public String getMessage() {
    return super.getMessage()+"\n error code="+error_code+"  error value="+error_value; //$NON-NLS-1$ //$NON-NLS-2$
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
