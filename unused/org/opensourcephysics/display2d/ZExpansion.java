/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import org.opensourcephysics.numerics.Function;

public class ZExpansion implements Function {
  double expansion = 1;
  double k = 1;
  double min = -1;
  double max = 1;
  double center = 0;
  double a1, a2 = 1;

  /**
   * Constructor ZExpansion
   * @param expansion
   */
  public ZExpansion(double expansion) {
    this.expansion = Math.abs(expansion);
  }

  public void setExpansion(double expansion) {
    this.expansion = Math.abs(expansion);
    setMinMax(min, max);
  }

  public void setMinMax(double min, double max) {
    this.min = min;
    this.max = max;
    if(min==max) {
      k = 0;
      a1 = a2 = center = min;
    } else if((min<=0)&&(max>=0)) {
      center = 0;
      k = expansion/Math.max(-min, max);
      a1 = max/(1-Math.exp(-k*max));
      a2 = -min/(1-Math.exp(k*min));
    } else if(min>0) { // min and max positive
      center = min;
      k = expansion/(max-min);
      a1 = a2 = max/(1-Math.exp(-k*(max-min)));
    } else {           // min and max negative
      center = max;
      k = expansion/(max-min);
      a1 = a2 = -min/(1-Math.exp(k*(max-min)));
    }
  }

  public double evaluate(double z) {
    z = z-center; // shift
    if(z>=0) {
      return a1*(1-Math.exp(-k*z));
    }
    return -a2*(1-Math.exp(k*z));
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
