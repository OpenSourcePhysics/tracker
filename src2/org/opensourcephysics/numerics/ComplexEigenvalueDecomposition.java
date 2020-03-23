/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/**
    Adapted to OSP by Javier E. Hasbun and Wolfgang Christian, 2009.
  <P>
    @Copyright (c) 2017
    This software is to support the Open Source Physics library
    http://www.opensourcephysics.org under the terms of the GNU General Public
    License (GPL) as published by the Free Software Foundation.
 **/
package org.opensourcephysics.numerics;

/**
 * A Java Complex Eigenvalue Decomposition based on an Ada version of a NAG Fortran library subroutine.
 * Some Fortran labels have been preserved for traceability
 */
public class ComplexEigenvalueDecomposition implements java.io.Serializable {
  /**
   * Constructs the EigenvalueDecomposition.
   */
  public ComplexEigenvalueDecomposition() {}

  /**
   * Obtains the eigenvalues and eigenvectors of a complex matrix.
   *
   * Input
   * @parameter Complex double [][] matrix A containing the data
   * @parameter Complex double [] empty vector lambda
   * @parameter Complex double [][] empty eigenvector matrix vec
   * @parameter unset single element boolean [] fail
   *
   * Ouput:
   * matrix A is unmodified
   * vector lambda contains the complex eigenvalues
   * matrix vec contains the complex eigenvectors
   * parameter fails is set to true if the eigen fails to succeed
   * The procedure eigen computes both the eigenvalues and eigenvectors
   * of arbitrary n by n complex matrix.
   */
  public static void eigen(Complex A[][], Complex lambda[], Complex vec[][], boolean fail[]) {
    //System.out.println("Eigen.eigen(A, lambda, vec, fail)");
    // driver for computing eigenvalues and eigenvectors
    if((A==null)||(lambda==null)||(vec==null)) {
      System.out.println("Error in Eigen.eigen,"+" null or inconsistent array sizes."); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    int n = A.length;
    if((A[0].length!=n)||(vec.length!=n)||(vec[0].length!=n)||(lambda.length!=n)) {
      System.out.println("Error in Eigen.eigen,"+" inconsistent array sizes."); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    fail[0] = false;
    // special cases
    if(n<1) {
      System.out.println("zero size matrix"); //$NON-NLS-1$
      return;
    }
    int rowcol[] = new int[n];
    Complex B[][] = new Complex[n][n];
    ComplexMatrix.copy(A, B);
    if(n==1) {
      lambda[0] = B[0][0];
      vec[0][0] = new Complex(1.0, 0.0);
      return;
    }
    if(n==2) {
      twobytwo(B, lambda, vec);
      return;
    }
    //System.out.println("calling cxhess");
    cxhess(B, rowcol);
    for(int i = 0; i<n; i++) {
      lambda[i] = new Complex(-999.0, -999.0);
    }
    //System.out.println("calling cxeig2c");
    cxeig2c(B, lambda, vec, rowcol, fail);
  } // end eigen

  private static void twobytwo(Complex A[][], Complex lambda[], Complex vec[][]) {
    Complex b, c, rad, l1, l2;
    Complex Z[] = new Complex[2];
    double t;
    b = A[0][0].add(A[1][1]);              // negative
    c = A[0][0].mul(A[1][1]);
    c = c.subtract(A[0][1].mul(A[1][0]));
    rad = (b.mul(b)).subtract(c.mul(4.0)); // a==1
    rad = rad.sqrt();
    l1 = (b.add(rad)).div(2.0);
    l2 = (b.subtract(rad)).div(2.0);
    lambda[0] = l1;
    lambda[1] = l2;
    // eigenvectors in columns
    Z[0] = A[0][1].neg();
    Z[1] = A[0][0].subtract(l1);
    t = ComplexMatrix.norm2(Z);
    vec[0][0] = Z[0].div(t);
    vec[1][0] = Z[1].div(t);
    Z[0] = A[1][1].subtract(l2);
    Z[1] = A[1][0].neg();
    t = ComplexMatrix.norm2(Z);
    vec[0][1] = Z[0].div(t);
    vec[1][1] = Z[1].div(t);
  }

  private static double sumabs(Complex Z) {
    return Math.abs(Z.re())+Math.abs(Z.im());
  } // end sumabs

  private static void cxhess(Complex A[][], int rowcol[]) {
    int i, k, t;
    Complex x;
    Complex y;
    //System.out.println("cxhess(A, rowcol)");
    int n = A.length; // checked before call
    for(int j = 0; j<n; j++) {
      rowcol[j] = j;
    }
    k = 0;
    for(int m = k+1; m<n-1; m++) {            // main reduction loop
      i = m;
      x = new Complex(0.0, 0.0);
      for(int j = m; j<n; j++) {
        if(sumabs(A[j][m-1])>sumabs(x)) {
          x = A[j][m-1];
          i = j;
        }
      }
      if(i!=m) {
        //  rowcol row column interchange of H
        t = rowcol[m];
        rowcol[m] = rowcol[i];
        rowcol[i] = t;
        for(int j = m-1; j<n; j++) {
          y = A[i][j];
          A[i][j] = A[m][j];
          A[m][j] = y;
        }
        for(int j = 0; j<n; j++) {            // for J in 1..N loop
          y = A[j][i];
          A[j][i] = A[j][m];
          A[j][m] = y;
        }
      }
      if(sumabs(x)!=0.0) {
        for(int ii = m+1; ii<n; ii++) {
          y = A[ii][m-1];
          if(sumabs(y)>0.0) {
            y = y.div(x);
            A[ii][m-1] = y;
            for(int j = m; j<n; j++) {
              A[ii][j] = A[ii][j].subtract(y.mul(A[m][j]));
            }
            for(int j = 0; j<n; j++) {
              A[j][m] = A[j][m].add(y.mul(A[j][ii]));
            }
          }                                   // end if
          A[ii][m-1] = new Complex(0.0, 0.0); // just cleanup
        }
      }                                       // end if
    }                                         // end main reduction loop
    //System.out.println("result of cxhess=");
    //ComplexMatrix.print(A);
    //System.out.println("based on rowcol interchanges=");
    //Matrix.print(rowcol);
    //System.out.println(" ");
  }                                           // end cxhess

  private static void cxeig2c(Complex A[][], Complex lambda[], Complex vec[][], int rowcol[], boolean fail[]) {
    int j, k, m, mm, low, its, itn, ien;
    double anorm = 0.0;
    double ahr, aahr, acc, xr, xi, yr, yi, zr;
    Complex accnorm;
    Complex x, y, z, yy, T, S;
    int n = A.length; // checked in driver
    low = 0;
    acc = Math.pow(2.0, -23);
    //System.out.println("acc="+acc+" = 2^-23");
    T = new Complex(0.0, 0.0);
    itn = 30*n; // heuristic on maximum iterations
    ComplexMatrix.identity(vec); // initialize to identity Matrix
    // starting from Hessenberg reduction
    for(int ii = n-2; ii>0; ii--) { // for i in reverse A'FIRST+1..A'LAST-1 loop
      j = rowcol[ii];
      for(k = ii+1; k<n; k++) {     // for K in i+1..A'LAST loop
        vec[k][ii] = A[k][ii-1];
      }
      if(ii!=j) {
        for(k = ii; k<n; k++) {     // for k in i..A'LAST loop
          vec[ii][k] = vec[j][k];
          vec[j][k] = new Complex(0.0, 0.0);
        }
        vec[j][ii] = new Complex(1.0, 0.0);
      }
    }
    ien = n-1; // used as subscript, loop test <=ien
    // ien is decremented
    while(low<=ien) {                     // 260
      //System.out.println("260 low="+low+", ien="+ien);
      its = 0;
      // look for small single subdiagonal element
      L280:
      while(true) {                       // 280
        //System.out.println("in 280");
        k = low;
        // for kk in reverse low+1..ien loop  // 300
        for(int kk = ien; kk>low; kk--) { // 300
          //System.out.println("300 kk="+kk);
          ahr = sumabs(A[kk][kk-1]);
          aahr = acc*(sumabs(A[kk-1][kk-1])+sumabs(A[kk][kk]));
          if(ahr<=aahr) {
            k = kk;
            break;
          }
        }                                 // 300
        //System.out.println("exiting 300 with k="+k);
        if(k==ien) {
          break L280;                     // exit L280 when k = ien;  // 780
        }
        if(itn<=0) {
          fail[0] = true;
          return;
        }
        // compute shift
        if((its==10)||(its==20)) {
          S = new Complex(Math.abs(A[ien][ien-1].re())+Math.abs(A[ien-1][ien-2].re()), Math.abs(A[ien][ien-1].im())+Math.abs(A[ien-1][ien-2].im()));
        } else {
          S = A[ien][ien];
          x = A[ien-1][ien].mul(A[ien][ien-1]);
          if(sumabs(x)>0.0) {
            y = (A[ien-1][ien-1].subtract(S)).div(new Complex(2.0, 0.0));
            z = ((y.mul(y)).add(x)).sqrt();
            if(y.re()*z.re()+y.im()*z.im()<0.0) {
              z = z.neg();
            }
            yy = y.add(z);
            S = S.subtract(x.div(yy));
          }                                 // end if;
        }                                   // end if;  //  400
        for(int i = low; i<=ien; i++) {     // for i in low..ien loop  // 420
          A[i][i] = A[i][i].subtract(S);
        }                                   // end loop;  //  420
        T = T.add(S);
        its = its+1;
        itn = itn-1;
        j = k+1;
        // look for two consecutive small sub-diagonal elements
        xr = sumabs(A[ien-1][ien-1]);
        yr = sumabs(A[ien][ien-1]);
        zr = sumabs(A[ien][ien]);
        m = k;
        for(mm = ien-1; mm>=j; mm--) {      // for mm in reverse j..ien-1 loop  // 460
          //System.out.println("460 mm="+mm+", m="+m+", j="+j);
          yi = yr;
          yr = sumabs(A[mm][mm-1]);
          xi = zr;
          zr = xr;
          xr = sumabs(A[mm-1][mm-1]);
          if(yr<=(acc*zr/yi*(zr+xr+xi))) {
            m = mm;
            break;
          }
        }                                   // end loop;  //  460
        // triangular decomposition  A = L*R
        for(int i = m+1; i<=ien; i++) {     // for i in m+1..ien loop  // 620
          //System.out.println("620 mm="+mm+", m="+m+", i="+i);
          x = A[i-1][i-1];
          y = A[i][i-1];
          if(sumabs(x)>=sumabs(y)) {
            z = y.div(x);
            lambda[i] = new Complex(-1.0, 0.0);
          } else {
            // interchange rows of A
            for(int jj = i-1; jj<n; jj++) { // for j in i-1..n loop  // 540
              z = A[i-1][jj];
              A[i-1][jj] = A[i][jj];
              A[i][jj] = z;
            }                               // end loop;  //  540
            z = x.div(y);
            lambda[i] = new Complex(1.0, 0.0);
          }                                 // end if;
          A[i][i-1] = z;
          for(int jj = i; jj<n; jj++) {     // for j in i .. N loop  // 600
            A[i][jj] = A[i][jj].subtract(z.mul(A[i-1][jj]));
          }                                 // end loop;  //  600
        }                                   // end loop;  //  620
        // composition R*L = H
        for(int jj = m+1; jj<=ien; jj++) {  // for j in m+1..ien loop  // 760
          x = A[jj][jj-1];
          A[jj][jj-1] = new Complex(0.0, 0.0);
          // interchange columns of A and vec if necessary
          if(lambda[jj].re()>0.0) {
            for(int i = low; i<=jj; i++) {  // for i in low .. j loop  // 660
              z = A[i][jj-1];
              A[i][jj-1] = A[i][jj];
              A[i][jj] = z;
            }                               // end loop;  //  660
            for(int i = low; i<n; i++) {    // for i in low .. N loop  // 680
              z = vec[i][jj-1];
              vec[i][jj-1] = vec[i][jj];
              vec[i][jj] = z;
            }                               // end loop;  //  680
          }                                 // end if
          // end interchange columns
          for(int i = low; i<=jj; i++) {    // for i in low..j loop  // 720
            A[i][jj-1] = A[i][jj-1].add(x.mul(A[i][jj]));
          }                                 // 720
          for(int i = low; i<n; i++) {      // for i in low..N loop  // 740
            vec[i][jj-1] = vec[i][jj-1].add(x.mul(vec[i][jj]));
          }                                 // 740
          // end accumulate transformations
        }                                   // 760
      }                                     // 280
      // a root found
      lambda[ien] = A[ien][ien].add(T);
      ien = ien-1;
    }                                       // end loop;  // 260 while
    // all roots found
    for(int i = 0; i<n; i++) {        // for i in A'RANGE loop
      anorm = anorm+sumabs(lambda[i]);
      for(int jj = i+1; jj<n; jj++) { // for j in i + 1 .. A'LAST loop
        anorm = anorm+sumabs(A[i][jj]);
      }
    }
    accnorm = new Complex(anorm*Math.pow(2.0, -23), 0.0);
    if((anorm==0.0)||(n<2)) {
      return; // done
    }
    // back substitute to set up vec of upper triangular form
    for(ien = n-1; ien>low; ien--) {      // for ien in reverse low+1..N loop
      x = lambda[ien];
      for(int i = ien-1; i>=low; i--) {   // for i in reverse low .. ien - 1 loop
        z = A[i][ien];
        for(int jj = i+1; jj<ien; jj++) { // for j in i+1..ien-1 loop
          z = z.add(A[i][jj].mul(A[jj][ien]));
        }
        y = x.subtract(lambda[i]);
        if(sumabs(y)==0.0) {
          y = accnorm;
        }
        A[i][ien] = z.div(y);
      }
    }
    // multiply by transformation Matrix to give vec of original full Matrix
    for(int jj = n-1; jj>=0; jj--) { // for j in reverse A'RANGE loop
      for(int i = 0; i<n; i++) {     // for i in A'RANGE loop
        z = vec[i][jj];
        for(k = 0; k<jj; k++) {      // for k in A'first..j-1 loop
          z = z.add(vec[i][k].mul(A[k][jj]));
        }
        vec[i][jj] = z;
      }
    }
  }                                  // end cxeig2c

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
