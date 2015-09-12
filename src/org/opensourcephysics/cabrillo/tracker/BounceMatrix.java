/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */

package org.opensourcephysics.cabrillo.tracker;

/**
 * A subset of methods from the Jama Matrix class used for the BounceModel.
 * This incorporates the LUDecomposition and QRDecomposition classes as static
 * inner classes. 
 * Almost all javadoc and other comments have been removed for compactness.
 *
 * The entire JAMA matrix package including full documentation is available from 
 * http://math.nist.gov/javanumerics/jama
 * 
 * @author The MathWorks, Inc. and the National Institute of Standards and Technology.
 * @author Doug Brown (this file)
 * @version 5 August 1998 (Jama), 12 Jan 2012 (this file)
*/
   @SuppressWarnings("javadoc")
public class BounceMatrix {

   private double[][] A;
   private int m, n;

	public BounceMatrix (int m, int n) {
      this.m = m;
      this.n = n;
      A = new double[m][n];
   }

   public BounceMatrix (double[][] A) {
      m = A.length;
      n = A[0].length;
      for (int i = 0; i < m; i++) {
         if (A[i].length != n) {
            throw new IllegalArgumentException("All rows must have the same length."); //$NON-NLS-1$
         }
      }
      this.A = A;
   }

   public BounceMatrix (double[][] A, int m, int n) {
      this.A = A;
      this.m = m;
      this.n = n;
   }

   public double[][] getArray () {
      return A;
   }

   public double[][] getArrayCopy () {
      double[][] C = new double[m][n];
      for (int i = 0; i < m; i++) {
         for (int j = 0; j < n; j++) {
            C[i][j] = A[i][j];
         }
      }
      return C;
   }

   public int getRowDimension () {
      return m;
   }

   public int getColumnDimension () {
      return n;
   }

   public double get (int i, int j) {
      return A[i][j];
   }

   public BounceMatrix getMatrix (int i0, int i1, int j0, int j1) {
      BounceMatrix X = new BounceMatrix(i1-i0+1,j1-j0+1);
      double[][] B = X.getArray();
      try {
         for (int i = i0; i <= i1; i++) {
            for (int j = j0; j <= j1; j++) {
               B[i-i0][j-j0] = A[i][j];
            }
         }
      } catch(ArrayIndexOutOfBoundsException e) {
         throw new ArrayIndexOutOfBoundsException("Submatrix indices"); //$NON-NLS-1$
      }
      return X;
   }

   public BounceMatrix getMatrix (int[] r, int j0, int j1) {
      BounceMatrix X = new BounceMatrix(r.length,j1-j0+1);
      double[][] B = X.getArray();
      try {
         for (int i = 0; i < r.length; i++) {
            for (int j = j0; j <= j1; j++) {
               B[i][j-j0] = A[r[i]][j];
            }
         }
      } catch(ArrayIndexOutOfBoundsException e) {
         throw new ArrayIndexOutOfBoundsException("Submatrix indices"); //$NON-NLS-1$
      }
      return X;
   }

   public BounceMatrix minus (BounceMatrix B) {
  	 if (B.m != m || B.n != n) {
       throw new IllegalArgumentException("Matrix dimensions must agree."); //$NON-NLS-1$
  	 }
      BounceMatrix X = new BounceMatrix(m,n);
      double[][] C = X.getArray();
      for (int i = 0; i < m; i++) {
         for (int j = 0; j < n; j++) {
            C[i][j] = A[i][j] - B.A[i][j];
         }
      }
      return X;
   }

   public BounceMatrix times (BounceMatrix B) {
      if (B.m != n) {
         throw new IllegalArgumentException("Matrix inner dimensions must agree."); //$NON-NLS-1$
      }
      BounceMatrix X = new BounceMatrix(m,B.n);
      double[][] C = X.getArray();
      double[] Bcolj = new double[n];
      for (int j = 0; j < B.n; j++) {
         for (int k = 0; k < n; k++) {
            Bcolj[k] = B.A[k][j];
         }
         for (int i = 0; i < m; i++) {
            double[] Arowi = A[i];
            double s = 0;
            for (int k = 0; k < n; k++) {
               s += Arowi[k]*Bcolj[k];
            }
            C[i][j] = s;
         }
      }
      return X;
   }

   public BounceMatrix solve (BounceMatrix B) {
      return (m == n ? (new LUDecomposition(this)).solve(B) :
                       (new QRDecomposition(this)).solve(B));
   }

   public BounceMatrix inverse () {
      return solve(identity(m,m));
   }

   public static BounceMatrix identity (int m, int n) {
      BounceMatrix A = new BounceMatrix(m,n);
      double[][] X = A.getArray();
      for (int i = 0; i < m; i++) {
         for (int j = 0; j < n; j++) {
            X[i][j] = (i == j ? 1.0 : 0.0);
         }
      }
      return A;
   }
   
//_______________________ LUDecomposition class __________________________

   static class LUDecomposition {

     private double[][] LU;
     private int m, n, pivsign; 
     private int[] piv;

     public LUDecomposition (BounceMatrix A) {

        LU = A.getArrayCopy();
        m = A.getRowDimension();
        n = A.getColumnDimension();
        piv = new int[m];
        for (int i = 0; i < m; i++) {
           piv[i] = i;
        }
        pivsign = 1;
        double[] LUrowi;
        double[] LUcolj = new double[m];

        for (int j = 0; j < n; j++) {

           for (int i = 0; i < m; i++) {
              LUcolj[i] = LU[i][j];
           }
           
           for (int i = 0; i < m; i++) {
              LUrowi = LU[i];
              int kmax = Math.min(i,j);
              double s = 0.0;
              for (int k = 0; k < kmax; k++) {
                 s += LUrowi[k]*LUcolj[k];
              }
              LUrowi[j] = LUcolj[i] -= s;
           }

           int p = j;
           for (int i = j+1; i < m; i++) {
              if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                 p = i;
              }
           }
           if (p != j) {
              for (int k = 0; k < n; k++) {
                 double t = LU[p][k]; LU[p][k] = LU[j][k]; LU[j][k] = t;
              }
              int k = piv[p]; piv[p] = piv[j]; piv[j] = k;
              pivsign = -pivsign;
           }
           
           if (j < m & LU[j][j] != 0.0) {
              for (int i = j+1; i < m; i++) {
                 LU[i][j] /= LU[j][j];
              }
           }
        }
     }

     public boolean isNonsingular () {
        for (int j = 0; j < n; j++) {
           if (LU[j][j] == 0)
              return false;
        }
        return true;
     }

     public BounceMatrix solve (BounceMatrix B) {
        if (B.getRowDimension() != m) {
           throw new IllegalArgumentException("Matrix row dimensions must agree."); //$NON-NLS-1$
        }
        if (!this.isNonsingular()) {
           throw new RuntimeException("Matrix is singular."); //$NON-NLS-1$
        }
        // Copy right hand side with pivoting
        int nx = B.getColumnDimension();
        BounceMatrix Xmat = B.getMatrix(piv,0,nx-1);
        double[][] X = Xmat.getArray();

        // Solve L*Y = B(piv,:)
        for (int k = 0; k < n; k++) {
           for (int i = k+1; i < n; i++) {
              for (int j = 0; j < nx; j++) {
                 X[i][j] -= X[k][j]*LU[i][k];
              }
           }
        }
        // Solve U*X = Y;
        for (int k = n-1; k >= 0; k--) {
           for (int j = 0; j < nx; j++) {
              X[k][j] /= LU[k][k];
           }
           for (int i = 0; i < k; i++) {
              for (int j = 0; j < nx; j++) {
                 X[i][j] -= X[k][j]*LU[i][k];
              }
           }
        }
        return Xmat;
     }
  }
   
//_________________________ QRDecomposition class __________________________  
   
  static class QRDecomposition {

	    private double[][] QR;
	    private int m, n;
	    private double[] Rdiag;

	    public QRDecomposition (BounceMatrix A) {
	       QR = A.getArrayCopy();
	       m = A.getRowDimension();
	       n = A.getColumnDimension();
	       Rdiag = new double[n];

	       for (int k = 0; k < n; k++) {
	          // Compute 2-norm of k-th column without under/overflow.
	          double nrm = 0;
	          for (int i = k; i < m; i++) {
	             nrm = hypot(nrm,QR[i][k]);
	          }

	          if (nrm != 0.0) {
	             // Form k-th Householder vector.
	             if (QR[k][k] < 0) {
	                nrm = -nrm;
	             }
	             for (int i = k; i < m; i++) {
	                QR[i][k] /= nrm;
	             }
	             QR[k][k] += 1.0;

	             // Apply transformation to remaining columns.
	             for (int j = k+1; j < n; j++) {
	                double s = 0.0; 
	                for (int i = k; i < m; i++) {
	                   s += QR[i][k]*QR[i][j];
	                }
	                s = -s/QR[k][k];
	                for (int i = k; i < m; i++) {
	                   QR[i][j] += s*QR[i][k];
	                }
	             }
	          }
	          Rdiag[k] = -nrm;
	       }
	    }

	    public boolean isFullRank () {
	       for (int j = 0; j < n; j++) {
	          if (Rdiag[j] == 0)
	             return false;
	       }
	       return true;
	    }

	    public BounceMatrix solve (BounceMatrix B) {
	       if (B.getRowDimension() != m) {
	          throw new IllegalArgumentException("Matrix row dimensions must agree."); //$NON-NLS-1$
	       }
	       if (!this.isFullRank()) {
	          throw new RuntimeException("Matrix is rank deficient."); //$NON-NLS-1$
	       }
	       
	       // Copy right hand side
	       int nx = B.getColumnDimension();
	       double[][] X = B.getArrayCopy();

	       // Compute Y = transpose(Q)*B
	       for (int k = 0; k < n; k++) {
	          for (int j = 0; j < nx; j++) {
	             double s = 0.0; 
	             for (int i = k; i < m; i++) {
	                s += QR[i][k]*X[i][j];
	             }
	             s = -s/QR[k][k];
	             for (int i = k; i < m; i++) {
	                X[i][j] += s*QR[i][k];
	             }
	          }
	       }
	       // Solve R*X = Y;
	       for (int k = n-1; k >= 0; k--) {
	          for (int j = 0; j < nx; j++) {
	             X[k][j] /= Rdiag[k];
	          }
	          for (int i = 0; i < k; i++) {
	             for (int j = 0; j < nx; j++) {
	                X[i][j] -= X[k][j]*QR[i][k];
	             }
	          }
	       }
	       return (new BounceMatrix(X,n,nx).getMatrix(0,n-1,0,nx-1));
	    }
	    
	    public double hypot(double a, double b) {
	      double r;
	      if (Math.abs(a) > Math.abs(b)) {
	         r = b/a;
	         r = Math.abs(a)*Math.sqrt(1+r*r);
	      } 
	      else if (b != 0) {
	         r = a/b;
	         r = Math.abs(b)*Math.sqrt(1+r*r);
	      } 
	      else {
	         r = 0.0;
	      }
	      return r;
	   }

	 }
}
