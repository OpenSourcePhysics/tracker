/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.specialfunctions;
import java.util.HashMap;
import java.util.Map;
import org.opensourcephysics.numerics.Function;

/*
 * Bessel function of order n by J E Hasbun for the OSP library.
 * Ref : http : //www.alglib.net/specialfunctions
 *
 * Using the Algorithm of Stephen L.Moshier Returns the Bessel function
 * of order n, where n is a (possibly negative) integer. The ratio of jn(x)
 * to j0(x) is computed by backward recurrence.First the ratio jn / jn -
 * is found by a continued fraction expansion.Then the recurrence
 * relating successive orders is applied until j0 or j1 is reached.
 * If n = 0 or 1 the routine for j0 or j1 is called directly.
 * ACCURACY - Absolute error :
 * arithmetic range #trials peak rms
 * IEEE 0, 3050004.4e-167.9e-17
 * Not suitable for large n or x. Use jv() (fractional order) instead.
 *
 * OSP getFunction(n) method added by W. Christian in order to use the OSP Function interface.
 *
 * @author Javier E Hasbun 2008.
 * @author Wolfgang Christian
 */
public class Bessel {
  static final Map<Integer, Function> functionMap = new HashMap<Integer, Function>();
  static final Map<Integer, Function> derivativeMap = new HashMap<Integer, Function>();

  /**
   * Gets the Bessel function with the given order.
   */
  public static synchronized Function getFunction(int n) {
    if(n<0) {
      throw new IllegalArgumentException(Messages.getString("Bessel.0.neg_order")); //$NON-NLS-1$
    }
    Function f = functionMap.get(n);
    if(f!=null) {
      return f;
    }
    f = new BesselFunction(n);
    functionMap.put(n, f); // polynomial was not in the list so add it.
    return f;
  }

  /**
   * Gets the derivative of the Bessel function with the given order.
   */
  public static synchronized Function getDerivative(int n) {
    if(n<0) {
      throw new IllegalArgumentException(Messages.getString("Bessel.1.neg_order")); //$NON-NLS-1$
    }
    Function f = derivativeMap.get(n);
    if(f!=null) {
      return f;
    }
    f = new BesselDerivative(n);
    derivativeMap.put(n, f); // polynomial was not in the list so add it.
    return f;
  }

  /**
   * Computes the Bessel function of order n at x.
   * @param n
   * @param x
   * @return
   */
  public static double besseln(int n, double x) {
    int sg, k;
    double y, tmp, pk, xk, pkm1, r, pkm2;
    if(n<0) {
      n = -n;
      if(n%2==0) {
        sg = 1;
      } else {
        sg = -1;
      }
    } else {
      sg = 1;
    }
    if(x<0) {
      if(n%2!=0) {
        sg = -sg;
      }
      x = -x;
    }
    if(n==0) {
      y = sg*bessel0(x);
      return y;
    }
    if(n==1) {
      y = sg*bessel1(x);
      return y;
    }
    if(n==2) {
      if(x==0) {
        y = 0;
      } else {
        y = sg*(2.0*bessel1(x)/x-bessel0(x));
      }
      return y;
    }
    if(x<1.e-12) {
      y = 0;
      return y;
    }
    k = 53;
    pk = 2*(n+k);
    tmp = pk;
    xk = x*x;
    while(k!=0) {
      pk = pk-2.0;
      tmp = pk-xk/tmp;
      k = k-1;
    }
    tmp = x/tmp;
    pk = 1.0;
    pkm1 = 1.0/tmp;
    k = n-1;
    r = 2*k;
    while(k!=0) {
      pkm2 = (pkm1*r-pk*x)/x;
      pk = pkm1;
      pkm1 = pkm2;
      r = r-2.0;
      k = k-1;
    }
    if(Math.abs(pk)>Math.abs(pkm1)) {
      tmp = bessel1(x)/pk;
    } else {
      tmp = bessel0(x)/pkm1;
    }
    return sg*tmp;
  }

  /**
   * Computes the derivative of the Bessel function of order n at x.
   * @param n
   * @param x
   * @return
   */
  public static double besselnDerivative(int n, double x) {
    int m, qm, qp, nm, np;
    double bjn, bjnm, bjnp;
    m = n;
    if(n==0) {
      bjn = besseln(1, x);
      return -bjn;
    }
    qm = 1;
    qp = 1;
    nm = m-1;
    np = m+1;
    if(m<0) {
      if(nm<0) {
        nm = -nm;
        qm = -1;
      }
      if(np<0) {
        np = -np;
        qp = -1;
      }
    }
    bjnm = besseln(nm, x);
    bjnp = besseln(np, x);
    bjnm = Math.pow(qm, nm)*bjnm;
    bjnp = Math.pow(qp, np)*bjnp;
    return(bjnm-bjnp)/2.;
  }

  /**
   * Computes nt zeroes of the n-th order Bessel function
   * @param n
   * @param nt
   * @return
   */
  public static double[] besselnZeros(int n, int nt) {
    int l;
    double x, x0, bjn, djn;
    double[] rj0 = new double[nt];
    if(n<0) {
      n = -n;
    }
    if(n<=20) {
      x = 2.82141+1.15859*n;
    } else {
      x = n+1.85576*Math.pow(n, 0.33333)+1.03315/Math.pow(n, 0.33333);
    }
    l = 0;
    while(true) {
      while(true) {
        x0 = x;
        bjn = besseln(n, x);
        djn = besselnDerivative(n, x);
        x = x-bjn/djn; // Newton-Raphson step
        if(!(Math.abs(x-x0)>1.0e-6)) {
          break;
        }
      }
      rj0[l] = x;
      l = l+1;
      x = x+Math.PI+(0.0972+0.0679*n-0.000354*Math.pow(n, 2))/l;
      if(!(l<nt)) {
        break;
      }
    }
    return rj0;
  }

  /*
   * Comutes the Bessel function of order 0 at x.
   * Ref : http : //www.alglib.net/specialfunctions
   * Using the Algorithm of Stephen L.Moshier
   * The domain is divided into the intervals[0, 5] and (5, infinity).
   * In the first interval the following rational approximation is used:
   *      2        2
   * (w - r ) (w - r ) P (w) / Q (w)
   *      1        2   3       8
   * where w = x^2 and the two r 'sare zeros of the function. In the second
   * interval, the Hankel asymptotic expansion is employed with two
   * rational functions of degree 6 / 6 and 7 / 7.
   */
  public static double bessel0(double x) {
    double nn, pzero, qzero, xsq, p1, q1, y;
    double[] zz = new double[2];
    double[] p = {26857.86856980014981415848441, -40504123.71833132706360663322, 25071582855.36881945555156435, -8085222034853.793871199468171, 1434354939140344.111664316553, -136762035308817138.6865416609, 6382059341072356562.289432465, -117915762910761053603.8440800, 493378725179413356181.6813446};
    double[] q = {1.0, 1363.063652328970604442810507, 1114636.098462985378182402543, 669998767.2982239671814028660, 312304311494.1213172572469442, 112775673967979.8507056031594, 30246356167094626.98627330784, 5428918384092285160.200195092, 493378725179413356211.3278438};
    if(x<0) {
      x = -x;
    }
    if(x>8.0) {
      zz = besselasympt0(x);
      pzero = zz[0];
      qzero = zz[1];
      nn = x-Math.PI/4;
      y = Math.sqrt(2/Math.PI/x)*(pzero*Math.cos(nn)-qzero*Math.sin(nn));
      return y;
    }
    xsq = x*x;
    p1 = p[0];
    for(int i = 1; i<9; i++) {
      p1 = p[i]+p1*xsq;
    }
    q1 = q[0];
    for(int i = 1; i<9; i++) {
      q1 = q[i]+q1*xsq;
    }
    return p1/q1;
  }

  /*
   * Computes the Bessel function of order 1.
   * Ref : http : //www.alglib.net/specialfunctions
   * Using the Algorithm of Stephen L.Moshier
   * The domain is divided into the intervals[0, 8] and (8, infinity).
   * In the first interval a 24term Chebyshev expansion is used.In the
   * second, the asymptotic trigonometric representation is employed
   * using two rational functions of degree 5 / 5.
   */
  public static double bessel1(double x) {
    double s, pzero, qzero, nn, p1, q1, y, xsq;
    double[] zz = new double[2];
    double[] p = {2701.122710892323414856790990, -4695753.530642995859767162166, 3413234182.301700539091292655, -1322983480332.126453125473247, 290879526383477.5409737601689, -35888175699101060.50743641413, 2316433580634002297.931815435, -66721065689249162980.20941484, 581199354001606143928.050809};
    double[] q = {1.0, 1606.931573481487801970916749, 1501793.594998585505921097578, 1013863514.358673989967045588, 524371026216.7649715406728642, 208166122130760.7351240184229, 60920613989175217.46105196863, 11857707121903209998.37113348, 1162398708003212287858.529400};
    s = Math.signum(x);
    if(x<0) {
      x = -x;
    }
    if(x>8.0) {
      zz = besselasympt1(x);
      pzero = zz[0];
      qzero = zz[1];
      nn = x-3*Math.PI/4;
      y = Math.sqrt(2/Math.PI/x)*(pzero*Math.cos(nn)-qzero*Math.sin(nn));
      if(s<0) {
        y = -y;
      }
      return y;
    }
    xsq = x*x;
    p1 = p[0];
    for(int i = 1; i<9; i++) {
      p1 = p[i]+p1*xsq;
    }
    q1 = q[0];
    for(int i = 1; i<9; i++) {
      q1 = q[i]+q1*xsq;
    }
    return s*x*p1/q1;
  }

  private static double[] besselasympt0(double x) {
    double xsq, p2, q2, p3, q3, pzero, qzero;
    double[] zz = new double[2];
    double[] p = {0.0, 2485.271928957404011288128951, 153982.6532623911470917825993, 2016135.283049983642487182349, 8413041.456550439208464315611, 12332384.76817638145232406055, 5393485.083869438325262122897};
    double[] q = {1.0, 2615.700736920839685159081813, 156001.7276940030940592769933, 2025066.801570134013891035236, 8426449.050629797331554404810, 12338310.22786324960844856182, 5393485.083869438325560444960};
    double[] pp = {0.0, -4.887199395841261531199129300, -226.2630641933704113967255053, -2365.956170779108192723612816, -8239.066313485606568803548860, -10381.41698748464093880530341, -3984.617357595222463506790588};
    double[] qq = {1.0, 408.7714673983499223402830260, 15704.89191515395519392882766, 156021.3206679291652539287109, 533291.3634216897168722255057, 666745.4239319826986004038103, 255015.5108860942382983170882};
    xsq = 64.0/x/x;
    p2 = p[0];
    for(int i = 1; i<7; i++) {
      p2 = p[i]+p2*xsq;
    }
    q2 = q[0];
    for(int i = 1; i<7; i++) {
      q2 = q[i]+q2*xsq;
    }
    p3 = pp[0];
    for(int i = 1; i<7; i++) {
      p3 = pp[i]+p3*xsq;
    }
    q3 = qq[0];
    for(int i = 1; i<7; i++) {
      q3 = qq[i]+q3*xsq;
    }
    pzero = p2/q2;
    qzero = 8*p3/q3/x;
    zz[0] = pzero;
    zz[1] = qzero;
    return zz;
  }

  private static double[] besselasympt1(double x) {
    double xsq, p2, q2, p3, q3, pzero, qzero;
    double[] zz = new double[2];
    double[] p = {-1611.616644324610116477412898, -109824.0554345934672737413139, -1523529.351181137383255105722, -6603373.248364939109255245434, -9942246.505077641195658377899, -4435757.816794127857114720794};
    double[] q = {1.0, -1455.009440190496182453565068, -107263.8599110382011903063867, -1511809.506634160881644546358, -6585339.479723087072826915069, -9934124.389934585658967556309, -4435757.816794127856828016962};
    double[] pp = {35.26513384663603218592175580, 1706.375429020768002061283546, 18494.26287322386679652009819, 66178.83658127083517939992166, 85145.16067533570196555001171, 33220.91340985722351859704442};
    double[] qq = {1.0, 863.8367769604990967475517183, 37890.22974577220264142952256, 400294.4358226697511708610813, 1419460.669603720892855755253, 1819458.042243997298924553839, 708712.8194102874357377502472};
    xsq = 64.0/x/x;
    p2 = p[0];
    for(int i = 1; i<6; i++) {
      p2 = p[i]+p2*xsq;
    }
    q2 = q[0];
    for(int i = 1; i<7; i++) {
      q2 = q[i]+q2*xsq;
    }
    p3 = pp[0];
    for(int i = 1; i<6; i++) {
      p3 = pp[i]+p3*xsq;
    }
    q3 = qq[0];
    for(int i = 1; i<7; i++) {
      q3 = qq[i]+q3*xsq;
    }
    pzero = p2/q2;
    qzero = 8*p3/q3/x;
    zz[0] = pzero;
    zz[1] = qzero;
    return zz;
  }

  /**
   * Computes the Bessel function of order n.
   * @author Wolfgang Christian
   */
  static class BesselFunction implements Function {
    int n;

    BesselFunction(int n) {
      this.n = n;
    }

    /**
     * Evaluates the Bessel function.
     */
    public double evaluate(final double x) {
      if(n==0) {
        return Bessel.bessel0(x);
      }
      if(n==1) {
        return Bessel.bessel1(x);
      }
      return Bessel.besseln(n, x);
    }

  }

  /**
   * Computes the derivative of the Bessel function or order n.
   * @author Wolfgang Christian
   */
  static class BesselDerivative implements Function {
    int n;

    BesselDerivative(int n) {
      this.n = n;
    }

    /**
     * Evaluates the derivative of the Bessel function.
     */
    public double evaluate(final double x) {
      return Bessel.besselnDerivative(n, x);
    }

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
