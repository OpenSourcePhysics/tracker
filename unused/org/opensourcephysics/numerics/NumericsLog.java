/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import java.lang.reflect.Method;
import org.opensourcephysics.display.OSPRuntime;

/**
 * Logs numerics messages to the OSPLog using reflection.
 * Messages are not logged if the OSPLog class is not available.
 *
 * @author W. Christian
 * @version 1.0
 */
public class NumericsLog {
  static String logName = "org.opensourcephysics.controls.OSPLog"; //$NON-NLS-1$
  static Class<?> logClass;

  private NumericsLog() {}                                         // private to prohibit instantiation

  static {
    if(OSPRuntime.loadOSPLog) {
      try {
        logClass = Class.forName(logName);
      } catch(ClassNotFoundException ex) {
        logClass = null;
        OSPRuntime.loadOSPLog = false;
      }
    }
  }

  /**
   * Logs a fine debugging message in the OSPLog.
   *
   * @param msg the message
   */
  public static void fine(String msg) {
    if(logClass==null) {
      return;
    }
    try {
      Method m = logClass.getMethod("fine", new Class[] {String.class}); //$NON-NLS-1$
      // target is null because the fine method in the OSPLog class is static
      m.invoke(null, new Object[] {msg});
    } catch(Exception ex) {}
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
