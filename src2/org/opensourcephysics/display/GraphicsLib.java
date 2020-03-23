/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.opensourcephysics.numerics.ArrayLib;

/**
 * Library of useful computer graphics routines such as geometry routines
 * for computing the intersection of different shapes and rendering methods
 * for computing bounds and performing optimized drawing.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GraphicsLib {
  /** Indicates no intersection between shapes */
  public static final int NO_INTERSECTION = 0;

  /** Indicates intersection between shapes */
  public static final int COINCIDENT = -1;

  /** Indicates two lines are parallel */
  public static final int PARALLEL = -2;

  /**
   * Compute the intersection of two line segments.
   * @param a the first line segment
   * @param b the second line segment
   * @param intersect a Point in which to store the intersection point
   * @return the intersection code. One of {@link #NO_INTERSECTION},
   * {@link #COINCIDENT}, or {@link #PARALLEL}.
   */
  public static int intersectLineLine(Line2D a, Line2D b, Point2D intersect) {
    double a1x = a.getX1(), a1y = a.getY1();
    double a2x = a.getX2(), a2y = a.getY2();
    double b1x = b.getX1(), b1y = b.getY1();
    double b2x = b.getX2(), b2y = b.getY2();
    return intersectLineLine(a1x, a1y, a2x, a2y, b1x, b1y, b2x, b2y, intersect);
  }

  /**
   * Compute the intersection of two line segments.
   * @param a1x the x-coordinate of the first endpoint of the first line
   * @param a1y the y-coordinate of the first endpoint of the first line
   * @param a2x the x-coordinate of the second endpoint of the first line
   * @param a2y the y-coordinate of the second endpoint of the first line
   * @param b1x the x-coordinate of the first endpoint of the second line
   * @param b1y the y-coordinate of the first endpoint of the second line
   * @param b2x the x-coordinate of the second endpoint of the second line
   * @param b2y the y-coordinate of the second endpoint of the second line
   * @param intersect a Point in which to store the intersection point
   * @return the intersection code. One of {@link #NO_INTERSECTION},
   * {@link #COINCIDENT}, or {@link #PARALLEL}.
   */
  public static int intersectLineLine(double a1x, double a1y, double a2x, double a2y, double b1x, double b1y, double b2x, double b2y, Point2D intersect) {
    double ua_t = (b2x-b1x)*(a1y-b1y)-(b2y-b1y)*(a1x-b1x);
    double ub_t = (a2x-a1x)*(a1y-b1y)-(a2y-a1y)*(a1x-b1x);
    double u_b = (b2y-b1y)*(a2x-a1x)-(b2x-b1x)*(a2y-a1y);
    if(u_b!=0) {
      double ua = ua_t/u_b;
      double ub = ub_t/u_b;
      if((0<=ua)&&(ua<=1)&&(0<=ub)&&(ub<=1)) {
        intersect.setLocation(a1x+ua*(a2x-a1x), a1y+ua*(a2y-a1y));
        return 1;
      }
      return NO_INTERSECTION;
    }
    return(((ua_t==0)||(ub_t==0)) ? COINCIDENT : PARALLEL);
  }

  /**
   * Compute the intersection of a line and a rectangle.
   * @param a1 the first endpoint of the line
   * @param a2 the second endpoint of the line
   * @param r the rectangle
   * @param pts a length 2 or greater array of points in which to store
   * the results
   * @return the intersection code. One of {@link #NO_INTERSECTION},
   * {@link #COINCIDENT}, or {@link #PARALLEL}.
   */
  public static int intersectLineRectangle(Point2D a1, Point2D a2, Rectangle2D r, Point2D[] pts) {
    double a1x = a1.getX(), a1y = a1.getY();
    double a2x = a2.getX(), a2y = a2.getY();
    double mxx = r.getMaxX(), mxy = r.getMaxY();
    double mnx = r.getMinX(), mny = r.getMinY();
    if(pts[0]==null) {
      pts[0] = new Point2D.Double();
    }
    if(pts[1]==null) {
      pts[1] = new Point2D.Double();
    }
    int i = 0;
    if(intersectLineLine(mnx, mny, mxx, mny, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    if(intersectLineLine(mxx, mny, mxx, mxy, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    if(i==2) {
      return i;
    }
    if(intersectLineLine(mxx, mxy, mnx, mxy, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    if(i==2) {
      return i;
    }
    if(intersectLineLine(mnx, mxy, mnx, mny, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    return i;
  }

  /**
   * Compute the intersection of a line and a rectangle.
   * @param l the line
   * @param r the rectangle
   * @param pts a length 2 or greater array of points in which to store
   * the results
   * @return the intersection code. One of {@link #NO_INTERSECTION},
   * {@link #COINCIDENT}, or {@link #PARALLEL}.
   */
  public static int intersectLineRectangle(Line2D l, Rectangle2D r, Point2D[] pts) {
    double a1x = l.getX1(), a1y = l.getY1();
    double a2x = l.getX2(), a2y = l.getY2();
    double mxx = r.getMaxX(), mxy = r.getMaxY();
    double mnx = r.getMinX(), mny = r.getMinY();
    if(pts[0]==null) {
      pts[0] = new Point2D.Double();
    }
    if(pts[1]==null) {
      pts[1] = new Point2D.Double();
    }
    int i = 0;
    if(intersectLineLine(mnx, mny, mxx, mny, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    if(intersectLineLine(mxx, mny, mxx, mxy, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    if(i==2) {
      return i;
    }
    if(intersectLineLine(mxx, mxy, mnx, mxy, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    if(i==2) {
      return i;
    }
    if(intersectLineLine(mnx, mxy, mnx, mny, a1x, a1y, a2x, a2y, pts[i])>0) {
      i++;
    }
    return i;
  }

  /**
   * Computes the 2D convex hull of a set of points using Graham's
   * scanning algorithm. The algorithm has been implemented as described
   * in Cormen, Leiserson, and Rivest's Introduction to Algorithms.
   *
   * The running time of this algorithm is O(n log n), where n is
   * the number of input points.
   *
   * @param pts the input points in [x0,y0,x1,y1,...] order
   * @param len the length of the pts array to consider (2 * #points)
   * @return the convex hull of the input points
   */
  public static double[] convexHull(double[] pts, int len) {
    if(len<6) {
      throw new IllegalArgumentException("Input must have at least 3 points"); //$NON-NLS-1$
    }
    int plen = len/2-1;
    float[] angles = new float[plen];
    int[] idx = new int[plen];
    int[] stack = new int[len/2];
    return convexHull(pts, len, angles, idx, stack);
  }

  /**
   * Computes the 2D convex hull of a set of points using Graham's
   * scanning algorithm. The algorithm has been implemented as described
   * in Cormen, Leiserson, and Rivest's Introduction to Algorithms.
   *
   * The running time of this algorithm is O(n log n), where n is
   * the number of input points.
   *
   * @param pts
   * @return the convex hull of the input points
   */
  public static double[] convexHull(double[] pts, int len, float[] angles, int[] idx, int[] stack) {
    // check arguments
    int plen = len/2-1;
    if(len<6) {
      throw new IllegalArgumentException("Input must have at least 3 points"); //$NON-NLS-1$
    }
    if((angles.length<plen)||(idx.length<plen)||(stack.length<len/2)) {
      throw new IllegalArgumentException("Pre-allocated data structure too small"); //$NON-NLS-1$
    }
    int i0 = 0;
    // find the starting ref point: leftmost point with the minimum y coord
    for(int i = 2; i<len; i += 2) {
      if(pts[i+1]<pts[i0+1]) {
        i0 = i;
      } else if(pts[i+1]==pts[i0+1]) {
        i0 = ((pts[i]<pts[i0]) ? i : i0);
      }
    }
    // calculate polar angles from ref point and sort
    for(int i = 0, j = 0; i<len; i += 2) {
      if(i==i0) {
        continue;
      }
      angles[j] = (float) Math.atan2(pts[i+1]-pts[i0+1], pts[i]-pts[i0]);
      idx[j++] = i;
    }
    ArrayLib.sort(angles, idx, plen);
    // toss out duplicated angles
    float angle = angles[0];
    int ti = 0, tj = idx[0];
    for(int i = 1; i<plen; i++) {
      int j = idx[i];
      if(angle==angles[i]) {
        // keep whichever angle corresponds to the most distant
        // point from the reference point
        double x1 = pts[tj]-pts[i0];
        double y1 = pts[tj+1]-pts[i0+1];
        double x2 = pts[j]-pts[i0];
        double y2 = pts[j+1]-pts[i0+1];
        double d1 = x1*x1+y1*y1;
        double d2 = x2*x2+y2*y2;
        if(d1>=d2) {
          idx[i] = -1;
        } else {
          idx[ti] = -1;
          angle = angles[i];
          ti = i;
          tj = j;
        }
      } else {
        angle = angles[i];
        ti = i;
        tj = j;
      }
    }
    // initialize our stack
    int sp = 0;
    stack[sp++] = i0;
    int j = 0;
    for(int k = 0; k<2; j++) {
      if(idx[j]!=-1) {
        stack[sp++] = idx[j];
        k++;
      }
    }
    // do graham's scan
    for(; j<plen; j++) {
      if(idx[j]==-1) {
        continue; // skip tossed out points
      }
      while(isNonLeft(i0, stack[sp-2], stack[sp-1], idx[j], pts)) {
        sp--;
      }
      stack[sp++] = idx[j];
    }
    // construct the hull
    double[] hull = new double[2*sp];
    for(int i = 0; i<sp; i++) {
      hull[2*i] = pts[stack[i]];
      hull[2*i+1] = pts[stack[i]+1];
    }
    return hull;
  }

  /**
   * Convex hull helper method for detecting a non left turn about 3 points
   */
  private static boolean isNonLeft(int i0, int i1, int i2, int i3, double[] pts) {
    double l1, l2, l4, l5, l6, angle1, angle2, angle;
    l1 = Math.sqrt(Math.pow(pts[i2+1]-pts[i1+1], 2)+Math.pow(pts[i2]-pts[i1], 2));
    l2 = Math.sqrt(Math.pow(pts[i3+1]-pts[i2+1], 2)+Math.pow(pts[i3]-pts[i2], 2));
    l4 = Math.sqrt(Math.pow(pts[i3+1]-pts[i0+1], 2)+Math.pow(pts[i3]-pts[i0], 2));
    l5 = Math.sqrt(Math.pow(pts[i1+1]-pts[i0+1], 2)+Math.pow(pts[i1]-pts[i0], 2));
    l6 = Math.sqrt(Math.pow(pts[i2+1]-pts[i0+1], 2)+Math.pow(pts[i2]-pts[i0], 2));
    angle1 = Math.acos(((l2*l2)+(l6*l6)-(l4*l4))/(2*l2*l6));
    angle2 = Math.acos(((l6*l6)+(l1*l1)-(l5*l5))/(2*l6*l1));
    angle = (Math.PI-angle1)-angle2;
    if(angle<=0.0) {
      return(true);
    }
    return(false);
  }

  /**
   * Computes the mean, or centroid, of a set of points
   * @param pts the points array, in x1, y1, x2, y2, ... arrangement.
   * @param len the length of the array to consider
   * @return the centroid as a length-2 float array
   */
  public static float[] centroid(float pts[], int len) {
    float[] c = new float[] {0, 0};
    for(int i = 0; i<len; i += 2) {
      c[0] += pts[i];
      c[1] += pts[i+1];
    }
    c[0] /= len/2;
    c[1] /= len/2;
    return c;
  }

  /**
   * Expand a polygon by adding the given distance along the line from
   * the centroid of the polyong.
   * @param pts the polygon to expand, a set of points in a float array
   * @param len the length of the range of the array to consider
   * @param amt the amount by which to expand the polygon, each point
   * will be moved this distance along the line from the centroid of the
   * polygon to the given point.
   */
  public static void growPolygon(float pts[], int len, float amt) {
    float[] c = centroid(pts, len);
    for(int i = 0; i<len; i += 2) {
      float vx = pts[i]-c[0];
      float vy = pts[i+1]-c[1];
      float norm = (float) Math.sqrt(vx*vx+vy*vy);
      pts[i] += amt*vx/norm;
      pts[i+1] += amt*vy/norm;
    }
  }

  /**
   * Compute a cardinal spline, a series of cubic Bezier splines smoothly
   * connecting a set of points. Cardinal splines maintain C(1)
   * continuity, ensuring the connected spline segments form a differentiable
   * curve, ensuring at least a minimum level of smoothness.
   * @param pts the points to interpolate with a cardinal spline
   * @param slack a parameter controlling the "tightness" of the spline to
   * the control points, 0.10 is a typically suitable value
   * @param closed true if the cardinal spline should be closed (i.e. return
   * to the starting point), false for an open curve
   * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
   * instance.
   */
  public static GeneralPath cardinalSpline(float pts[], float slack, boolean closed) {
    GeneralPath path = new GeneralPath();
    path.moveTo(pts[0], pts[1]);
    return cardinalSpline(path, pts, slack, closed, 0f, 0f);
  }

  /**
   * Compute a cardinal spline, a series of cubic Bezier splines smoothly
   * connecting a set of points. Cardinal splines maintain C(1)
   * continuity, ensuring the connected spline segments form a differentiable
   * curve, ensuring at least a minimum level of smoothness.
   * @param pts the points to interpolate with a cardinal spline
   * @param start the starting index from which to read points
   * @param npoints the number of points to consider
   * @param slack a parameter controlling the "tightness" of the spline to
   * the control points, 0.10 is a typically suitable value
   * @param closed true if the cardinal spline should be closed (i.e. return
   * to the starting point), false for an open curve
   * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
   * instance.
   */
  public static GeneralPath cardinalSpline(float pts[], int start, int npoints, float slack, boolean closed) {
    GeneralPath path = new GeneralPath();
    path.moveTo(pts[start], pts[start+1]);
    return cardinalSpline(path, pts, start, npoints, slack, closed, 0f, 0f);
  }

  /**
   * Compute a cardinal spline, a series of cubic Bezier splines smoothly
   * connecting a set of points. Cardinal splines maintain C(1)
   * continuity, ensuring the connected spline segments form a differentiable
   * curve, ensuring at least a minimum level of smoothness.
   * @param p the GeneralPath instance to use to store the result
   * @param pts the points to interpolate with a cardinal spline
   * @param slack a parameter controlling the "tightness" of the spline to
   * the control points, 0.10 is a typically suitable value
   * @param closed true if the cardinal spline should be closed (i.e. return
   * to the starting point), false for an open curve
   * @param tx a value by which to translate the curve along the x-dimension
   * @param ty a value by which to translate the curve along the y-dimension
   * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
   * instance.
   */
  public static GeneralPath cardinalSpline(GeneralPath p, float pts[], float slack, boolean closed, float tx, float ty) {
    int npoints = 0;
    for(; npoints<pts.length; ++npoints) {
      if(Float.isNaN(pts[npoints])) {
        break;
      }
    }
    return cardinalSpline(p, pts, 0, npoints/2, slack, closed, tx, ty);
  }

  /**
   * Compute a cardinal spline, a series of cubic Bezier splines smoothly
   * connecting a set of points. Cardinal splines maintain C(1)
   * continuity, ensuring the connected spline segments form a differentiable
   * curve, ensuring at least a minimum level of smoothness.
   * @param p the GeneralPath instance to use to store the result
   * @param pts the points to interpolate with a cardinal spline
   * @param start the starting index from which to read points
   * @param npoints the number of points to consider
   * @param slack a parameter controlling the "tightness" of the spline to
   * the control points, 0.10 is a typically suitable value
   * @param closed true if the cardinal spline should be closed (i.e. return
   * to the starting point), false for an open curve
   * @param tx a value by which to translate the curve along the x-dimension
   * @param ty a value by which to translate the curve along the y-dimension
   * @return the cardinal spline as a Java2D {@link java.awt.geom.GeneralPath}
   * instance.
   */
  public static GeneralPath cardinalSpline(GeneralPath p, float pts[], int start, int npoints, float slack, boolean closed, float tx, float ty) {
    // compute the size of the path
    int len = 2*npoints;
    int end = start+len;
    if(len<6) {
      throw new IllegalArgumentException("To create spline requires at least 3 points"); //$NON-NLS-1$
    }
    float dx1, dy1, dx2, dy2;
    // compute first control point
    if(closed) {
      dx2 = pts[start+2]-pts[end-2];
      dy2 = pts[start+3]-pts[end-1];
    } else {
      dx2 = pts[start+4]-pts[start];
      dy2 = pts[start+5]-pts[start+1];
    }
    // repeatedly compute next control point and append curve
    int i;
    for(i = start+2; i<end-2; i += 2) {
      dx1 = dx2;
      dy1 = dy2;
      dx2 = pts[i+2]-pts[i-2];
      dy2 = pts[i+3]-pts[i-1];
      p.curveTo(tx+pts[i-2]+slack*dx1, ty+pts[i-1]+slack*dy1, tx+pts[i]-slack*dx2, ty+pts[i+1]-slack*dy2, tx+pts[i], ty+pts[i+1]);
    }
    // compute last control point
    if(closed) {
      dx1 = dx2;
      dy1 = dy2;
      dx2 = pts[start]-pts[i-2];
      dy2 = pts[start+1]-pts[i-1];
      p.curveTo(tx+pts[i-2]+slack*dx1, ty+pts[i-1]+slack*dy1, tx+pts[i]-slack*dx2, ty+pts[i+1]-slack*dy2, tx+pts[i], ty+pts[i+1]);
      dx1 = dx2;
      dy1 = dy2;
      dx2 = pts[start+2]-pts[end-2];
      dy2 = pts[start+3]-pts[end-1];
      p.curveTo(tx+pts[end-2]+slack*dx1, ty+pts[end-1]+slack*dy1, tx+pts[0]-slack*dx2, ty+pts[1]-slack*dy2, tx+pts[0], ty+pts[1]);
      p.closePath();
    } else {
      p.curveTo(tx+pts[i-2]+slack*dx2, ty+pts[i-1]+slack*dy2, tx+pts[i]-slack*dx2, ty+pts[i+1]-slack*dy2, tx+pts[i], ty+pts[i+1]);
    }
    return p;
  }

  /**
   * Computes a set of curves using the cardinal spline approach, but
   * using straight lines for completely horizontal or vertical segments.
   * @param p the GeneralPath instance to use to store the result
   * @param pts the points to interpolate with the spline
   * @param epsilon threshold value under which to treat the difference
   * between two values to be zero. Used to determine which segments to
   * treat as lines rather than curves.
   * @param slack a parameter controlling the "tightness" of the spline to
   * the control points, 0.10 is a typically suitable value
   * @param closed true if the spline should be closed (i.e. return
   * to the starting point), false for an open curve
   * @param tx a value by which to translate the curve along the x-dimension
   * @param ty a value by which to translate the curve along the y-dimension
   * @return the stack spline as a Java2D {@link java.awt.geom.GeneralPath}
   * instance.
   */
  public static GeneralPath stackSpline(GeneralPath p, float[] pts, float epsilon, float slack, boolean closed, float tx, float ty) {
    int npoints = 0;
    for(; npoints<pts.length; ++npoints) {
      if(Float.isNaN(pts[npoints])) {
        break;
      }
    }
    return stackSpline(p, pts, 0, npoints/2, epsilon, slack, closed, tx, ty);
  }

  /**
   * Computes a set of curves using the cardinal spline approach, but
   * using straight lines for completely horizontal or vertical segments.
   * @param p the GeneralPath instance to use to store the result
   * @param pts the points to interpolate with the spline
   * @param start the starting index from which to read points
   * @param npoints the number of points to consider
   * @param epsilon threshold value under which to treat the difference
   * between two values to be zero. Used to determine which segments to
   * treat as lines rather than curves.
   * @param slack a parameter controlling the "tightness" of the spline to
   * the control points, 0.10 is a typically suitable value
   * @param closed true if the spline should be closed (i.e. return
   * to the starting point), false for an open curve
   * @param tx a value by which to translate the curve along the x-dimension
   * @param ty a value by which to translate the curve along the y-dimension
   * @return the stack spline as a Java2D {@link java.awt.geom.GeneralPath}
   * instance.
   */
  public static GeneralPath stackSpline(GeneralPath p, float pts[], int start, int npoints, float epsilon, float slack, boolean closed, float tx, float ty) {
    // compute the size of the path
    int len = 2*npoints;
    int end = start+len;
    if(len<6) {
      throw new IllegalArgumentException("To create spline requires at least 3 points"); //$NON-NLS-1$
    }
    float dx1, dy1, dx2, dy2;
    // compute first control point
    if(closed) {
      dx2 = pts[start+2]-pts[end-2];
      dy2 = pts[start+3]-pts[end-1];
    } else {
      dx2 = pts[start+4]-pts[start];
      dy2 = pts[start+5]-pts[start+1];
    }
    // repeatedly compute next control point and append curve
    int i;
    for(i = start+2; i<end-2; i += 2) {
      dx1 = dx2;
      dy1 = dy2;
      dx2 = pts[i+2]-pts[i-2];
      dy2 = pts[i+3]-pts[i-1];
      if((Math.abs(pts[i]-pts[i-2])<epsilon)||(Math.abs(pts[i+1]-pts[i-1])<epsilon)) {
        p.lineTo(tx+pts[i], ty+pts[i+1]);
      } else {
        p.curveTo(tx+pts[i-2]+slack*dx1, ty+pts[i-1]+slack*dy1, tx+pts[i]-slack*dx2, ty+pts[i+1]-slack*dy2, tx+pts[i], ty+pts[i+1]);
      }
    }
    // compute last control point
    dx1 = dx2;
    dy1 = dy2;
    dx2 = pts[start]-pts[i-2];
    dy2 = pts[start+1]-pts[i-1];
    if((Math.abs(pts[i]-pts[i-2])<epsilon)||(Math.abs(pts[i+1]-pts[i-1])<epsilon)) {
      p.lineTo(tx+pts[i], ty+pts[i+1]);
    } else {
      p.curveTo(tx+pts[i-2]+slack*dx1, ty+pts[i-1]+slack*dy1, tx+pts[i]-slack*dx2, ty+pts[i+1]-slack*dy2, tx+pts[i], ty+pts[i+1]);
    }
    // close the curve if requested
    if(closed) {
      if((Math.abs(pts[end-2]-pts[0])<epsilon)||(Math.abs(pts[end-1]-pts[1])<epsilon)) {
        p.lineTo(tx+pts[0], ty+pts[1]);
      } else {
        dx1 = dx2;
        dy1 = dy2;
        dx2 = pts[start+2]-pts[end-2];
        dy2 = pts[start+3]-pts[end-1];
        p.curveTo(tx+pts[end-2]+slack*dx1, ty+pts[end-1]+slack*dy1, tx+pts[0]-slack*dx2, ty+pts[1]-slack*dy2, tx+pts[0], ty+pts[1]);
      }
      p.closePath();
    }
    return p;
  }

  /**
   * Expand a rectangle by the given amount.
   * @param r the rectangle to expand
   * @param amount the amount by which to expand the rectangle
   */
  public static void expand(Rectangle2D r, double amount) {
    r.setRect(r.getX()-amount, r.getY()-amount, r.getWidth()+2*amount, r.getHeight()+2*amount);
  }
  // ------------------------------------------------------------------------

} // end of class GraphicsLib

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
