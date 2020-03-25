/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.specialfunctions;
import java.util.ArrayList;
import org.opensourcephysics.numerics.Polynomial;

/**
 * Chebyshev defines Chebyshev polynomials Tn(x) and Un(x) using
 * the well known recurrence relationships. The information needed for this class
 * was gained from Alan Jeffrey's Handbook of Mathematical Formulas
 * and Integrals, 3rd Edition pages 290-295. Chebyshev polynomials
 * are used to solve differential equations of second order, hence why
 * we have two different types.
 *
 * This code is based on the Open Source Physics class for Hermite polynomials.
 *
 * @author Nick Dovidio
 * @version 1.0
 */
public class Chebyshev {
  static final ArrayList<Polynomial> chebyshevTList; //Stores Tn functions
  static final ArrayList<Polynomial> chebyshevUList; //Stores Un functions

  private Chebyshev() {}

  /**
   * This method returns the nth polynomial of type T. If it has already been calculated
   * it just returns it from the list. If we have not calculated it uses
   * the recursion relationship to calculate based off of the prior
   * polynomials.
   */
  public static synchronized Polynomial getPolynomialT(int n) {
    if(n<0) {
      throw new IllegalArgumentException(Messages.getString("Chebyshev.neg_degree")); //$NON-NLS-1$
    }
    if(n<chebyshevTList.size()) {
      return chebyshevTList.get(n);
    }
    Polynomial part1 = new Polynomial(new double[] {0, (2.0)});
    Polynomial p = part1.multiply(getPolynomialT(n-1)).subtract(getPolynomialT(n-2));
    chebyshevTList.add(p);
    return p;
  }

  /**
   * This method returns the nth polynomial of type U. If it has already been calculated
   * it just returns it from the list. If we have not calculated it uses
   * the recursion relationship to calculate based off of the prior
   * polynomials.
   */
  public static synchronized Polynomial getPolynomialU(int n) {
    if(n<chebyshevUList.size()) {
      return chebyshevUList.get(n);
    }
    Polynomial part1 = new Polynomial(new double[] {0, (2.0)});
    Polynomial p = part1.multiply(getPolynomialU(n-1)).subtract(getPolynomialU(n-2));
    chebyshevUList.add(p);
    return p;
  }

  /**
   * Here we used a static initialization list to calculate the first two
   * values needed for our recursive relationships.
   */
  static {
    chebyshevTList = new ArrayList<Polynomial>();
    chebyshevUList = new ArrayList<Polynomial>();
    Polynomial p = new Polynomial(new double[] {1.0});
    chebyshevTList.add(p);
    chebyshevUList.add(p);
    p = new Polynomial(new double[] {0, 1.0});
    chebyshevTList.add(p);
    p = new Polynomial(new double[] {0, 2.0});
    chebyshevUList.add(p);
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
