/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * Gamma function  by J E Hasbun for the OSP library.
 * Ref : http : //www.alglib.net/specialfunctions
 * Using the Algorithm of Stephen L. Moshier
 *
 * @author Javier E Hasbun 2008.
 */
package org.opensourcephysics.numerics.specialfunctions;
import org.opensourcephysics.numerics.Function;

public class Gamma implements Function {
  /**
   * Implements the Function interface for the gamma function .
   *
   * @param x
   * @return gamma function at x
   */
  public double evaluate(double x) {
    return gamma(x);
  }

  public static double gamma(double x) {
    /*
     * Input parameters:
     *       X   -   argument
     * Domain:
     *       0 < X < 171.6
     *    -170 < X < 0, X is not an integer.
     * Relative error:
     * arithmetic   domain     # trials      peak         rms
     * IEEE    -170,-33      20000       2.3e-15     3.3e-16
     * IEEE     -33,  33     20000       9.4e-16     2.2e-16
     * IEEE      33, 171.6   20000       2.3e-15     3.2e-16
     */
    double sgngam, q, z, y, p1, q1;
    int ip, p;
    double[] pp = {1.60119522476751861407E-4, 1.19135147006586384913E-3, 1.04213797561761569935E-2, 4.76367800457137231464E-2, 2.07448227648435975150E-1, 4.94214826801497100753E-1, 9.99999999999999996796E-1};
    double[] qq = {-2.31581873324120129819E-5, 5.39605580493303397842E-4, -4.45641913851797240494E-3, 1.18139785222060435552E-2, 3.58236398605498653373E-2, -2.34591795718243348568E-1, 7.14304917030273074085E-2, 1.00000000000000000320};
    sgngam = 1;
    q = Math.abs(x);
    if(q>33.0) {
      if(x<0.0) {
        p = (int) Math.floor(q);
        ip = Math.round(p);
        if(ip%2==0) {
          sgngam = -1;
        }
        z = q-p;
        if(z>0.5) {
          p = p+1;
          z = q-p;
        }
        z = q*Math.sin(Math.PI*z);
        z = Math.abs(z);
        z = Math.PI/(z*gammastirf(q));
      } else {
        z = gammastirf(x);
      }
      y = sgngam*z;
      return y;
    }
    z = 1;
    while(x>=3) {
      x = x-1;
      z = z*x;
    }
    while(x<0) {
      if(x>-0.000000001) {
        y = z/((1+0.5772156649015329*x)*x);
        return y;
      }
      z = z/x;
      x = x+1;
    }
    while(x<2) {
      if(x<0.000000001) {
        y = z/((1+0.5772156649015329*x)*x);
        return y;
      }
      z = z/x;
      x = x+1.0;
    }
    if(x==2) {
      y = z;
      return y;
    }
    x = x-2.0;
    p1 = pp[0];
    for(int i = 1; i<7; i++) {
      p1 = pp[i]+p1*x;
    }
    q1 = qq[0];
    for(int i = 1; i<8; i++) {
      q1 = qq[i]+q1*x;
    }
    return z*p1/q1;
  }

  private static double gammastirf(double x) {
    double p1, w, y, v;
    w = 1/x;
    double[] pp = {7.87311395793093628397E-4, -2.29549961613378126380E-4, -2.68132617805781232825E-3, 3.47222221605458667310E-3, 8.33333333333482257126E-2};
    p1 = pp[0];
    for(int i = 1; i<5; i++) {
      p1 = pp[i]+p1*x;
    }
    w = 1+w*p1;
    y = Math.exp(x);
    if(x>143.01608) {
      v = Math.pow(x, 0.5*x-0.25);
      y = v*(v/y);
    } else {
      y = Math.pow(x, x-0.5)/y;
    }
    return 2.50662827463100050242*y*w;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must also be released
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
