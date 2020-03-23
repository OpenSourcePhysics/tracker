/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.specialfunctions;
import org.opensourcephysics.numerics.Function;

/**
 * Computes the error function for a real argument.
 *
 * Can be instantiated as a Function or can be used with static methods.
 *
 */
public class ErrorFunction implements Function {
  /**
   * Evaluates the error function in order to implement the Function interface.
   *
   * @param x
   * @return error function at x
   */
  public double evaluate(double x) {
    return errf(x);
  }

  /**
   * Error function.
   * @param x
   * @return value of error function at x
   */
  public static double errf(double x) {
    if(x>26.0) {
      return 1.0;
    } else if(x<-5.5) {
      return -1.0;
    } else {
      double absx, c, p, q;
      absx = Math.abs(x);
      if(absx<=0.5) {
        c = x*x;
        p = ((-0.356098437018154e-1*c+0.699638348861914e1)*c+0.219792616182942e2)*c+0.242667955230532e3;
        q = ((c+0.150827976304078e2)*c+0.911649054045149e2)*c+0.215058875869861e3;
        return x*p/q;
      }
      if(x<0.0) {
        return -(1.0-Math.exp(-x*x)*nonexperfc(absx));
      }
      return 1.0-Math.exp(-x*x)*nonexperfc(absx);
    }
  }

  private static double nonexperfc(double x) {
    double absx, c, p, q;
    absx = Math.abs(x);
    if(absx<=0.5) {
      return Math.exp(x*x)*errf(x);
    } else if(absx<4.0) {
      c = absx;
      p = ((((((-0.136864857382717e-6*c+0.564195517478974e0)*c+0.721175825088309e1)*c+0.431622272220567e2)*c+0.152989285046940e3)*c+0.339320816734344e3)*c+0.451918953711873e3)*c+0.300459261020162e3;
      q = ((((((c+0.127827273196294e2)*c+0.770001529352295e2)*c+0.277585444743988e3)*c+0.638980264465631e3)*c+0.931354094850610e3)*c+0.790950925327898e3)*c+0.300459260956983e3;
      return((x>0.0) ? p/q : Math.exp(x*x)*2.0-p/q);
    } else {
      c = 1.0/x/x;
      p = (((0.223192459734185e-1*c+0.278661308609648e0)*c+0.226956593539687e0)*c+0.494730910623251e-1)*c+0.299610707703542e-2;
      q = (((c+0.198733201817135e1)*c+0.105167510706793e1)*c+0.191308926107830e0)*c+0.106209230528468e-1;
      c = (c*(-p)/q+0.564189583547756)/absx;
      return((x>0.0) ? c : Math.exp(x*x)*2.0-c);
    }
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
