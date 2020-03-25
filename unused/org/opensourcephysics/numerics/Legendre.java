/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import java.util.ArrayList;

/**
 * Legendre defines Legendre Polynomials based on of Alan Jeffrey's
 * Handbook of Mathematical Formulas an Integrals. Please see page 286-288.
 * Information also obtained from: http://mathworld.wolfram.com/LegendrePolynomial.html
 *
 * This code is based on the Open Source Physics class for Hermite polynomials.
 *
 * @author Nick Dovidio
 * @version 1.0
 */
public class Legendre {
  static final ArrayList<Polynomial> legendreList; //Stores our functions

  private Legendre() {}

  /**
   * Gets the nth Legendre polynomial. If it has already been calculated
   * it just returns it from the list. If we have not calculated it uses
   * the recursion relationship to calculate based off of the prior
   * polynomials.
   */
  public static synchronized Polynomial getPolynomial(int n) {
    if(n<legendreList.size()) {
      return legendreList.get(n);
    }
    Polynomial part1 = new Polynomial(new double[] {0, (2*(n-1)+1)});
    Polynomial p1 = getPolynomial(n-1).multiply(part1);
    Polynomial p2 = getPolynomial(n-2).multiply(n-1);
    Polynomial p = p1.subtract(p2).multiply(1.0/n);
    System.out.println("n="+n); //$NON-NLS-1$
    legendreList.add(p);
    return p;
  }

  /**
   * We have a static initialization list that initializes our array
   * and the first two polynomials. These first two values are used to
   * calculate the recursion.
   */
  static {
    legendreList = new ArrayList<Polynomial>();
    Polynomial p = new Polynomial(new double[] {1.0});
    legendreList.add(p);
    p = new Polynomial(new double[] {0, 1.0});
    legendreList.add(p);
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
