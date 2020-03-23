/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import org.opensourcephysics.display.PlottingPanel;

public abstract class AxisFactory {
  final static String axisProperty = "org.opensourcephysics.display.axes.AxisFactory"; // system property that determines which axis to use //$NON-NLS-1$
  final static String defaultAxisFactoryInstance = "org.opensourcephysics.display.axes.CartesianType1Factory"; // default Axis implementation //$NON-NLS-1$

  public static AxisFactory newInstance() {
    String axisClass = defaultAxisFactoryInstance;
    try {
      axisClass = System.getProperty(axisProperty);
      if(axisClass==null) {
        axisClass = defaultAxisFactoryInstance;
      }
    } catch(SecurityException se) {}
    try {
      Class<?> c = Class.forName(axisClass);
      return(AxisFactory) c.newInstance();
    } catch(ClassNotFoundException cnfe) {}
    catch(InstantiationException ie) {}
    catch(IllegalAccessException iae) {}
    return null;
  }

  public static AxisFactory newInstance(String axisClass) {
    if(axisClass==null) {
      axisClass = defaultAxisFactoryInstance;
    }
    try {
      Class<?> c = Class.forName(axisClass);
      return(AxisFactory) c.newInstance();
    } catch(ClassNotFoundException cnfe) {}
    catch(InstantiationException ie) {}
    catch(IllegalAccessException iae) {}
    return null;
  }

  public abstract DrawableAxes createAxes(PlottingPanel panel);

  public static DrawableAxes createAxesType1(PlottingPanel panel) {
//    return new CartesianType1(panel);
  	// axes changed to interactive by default. D Brown 2012-01-27
    return new CartesianInteractive(panel);

  }

  public static DrawableAxes createAxesType2(PlottingPanel panel) {
    return new CartesianType2(panel);
  }

  public static DrawableAxes createAxesType3(PlottingPanel panel) {
    return new CartesianType3(panel);
  }

}

class CartesianType1Factory extends AxisFactory {
  public DrawableAxes createAxes(PlottingPanel panel) {
//  	return new CartesianType1(panel);
  	// axes changed to interactive by default. D Brown 2012-01-27
    return new CartesianInteractive(panel);
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
