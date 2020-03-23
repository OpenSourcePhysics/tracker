/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
/*----------------------------------------------------------------------------------------*
 * SurfaceVertex.java                                                                     *
 *                                                                                        *
 * Surface Plotter   version 1.10    14 Oct 1996                                          *
 *                   version 1.20     8 Nov 1996                                          *
 *                   version 1.30b1  17 May 1997                                          *
 *                                                                                        *
 * Copyright (c) 1996 Yanto Suryono. All Rights Reserved.                                 *
 *                                                                                        *
 * Permission to use, copy, and distribute this software for NON-COMMERCIAL purposes      *
 * and without fee is hereby granted, provided that this copyright notice appears in all  *
 * copies and this software is not modified in any way                                    *
 *                                                                                        *
 * Please send bug reports and/or corrections/suggestions to                              *
 * Yanto Suryono <d0771@cranesv.egg.kushiro-ct.ac.jp>                                     *
 *----------------------------------------------------------------------------------------*/

import java.awt.Point;

/**
 * The class <code>SurfaceVertex</code> represents a vertex in 3D space.
 *
 * @author  Yanto Suryono
 * @version 1.30b1, 17 May 1997
 * @since   1.10
 */
public final class SurfaceVertex {
  SurfacePlot surface;
  private Point projection;
  private int project_index;

  /**
   * The x coordinate
   */
  public double x;

  /**
   * The y coordinate
   */
  public double y;

  /**
   * The z coordinate
   */
  public double z;

  /**
   * The constructor of <code>SurfaceVertex</code>.
   * The x and y coordinated must be in normalized form, i.e: in the range -10 .. +10.
   *
   * @param ix the x coordinate
   * @param iy the y coordinate
   * @param iz the z coordinate
   */
  SurfaceVertex(double ix, double iy, double iz, SurfacePlot sp) {
    surface = sp;
    x = ix;
    y = iy;
    z = iz;
    project_index = sp.master_project_indexV-1;
  }

  /**
   * Determines whether this vertex is invalid, i.e has invalid coordinates value.
   *
   * @return <code>true</code> if this vertex is invalid
   */
  public final boolean isInvalid() {
    return Double.isNaN(z);
  }

  /**
   * Gets the 2D projection of the vertex.
   *
   * @return the 2D projection
   */
  public final Point projection() {
    if(project_index!=surface.master_project_indexV) {
      projection = surface.projector.project(x, y, ((z-surface.zminV)*surface.zfactorV-10));
      project_index = surface.master_project_indexV;
    }
    return projection;
  }

  public final void project() {
    projection = surface.projector.project(x, y, ((z-surface.zminV)*surface.zfactorV-10));
  }

  /**
   * Transforms coordinate values to fit the scaling factor of the
   * projector. This routine is only used for transforming center of projection
   * in Surface Plotter.
   */
  public final void transform() {
    x = x/surface.projector.getXScaling();
    y = y/surface.projector.getYScaling();
    z = ((surface.zmaxV-surface.zminV)*(z/surface.projector.getZScaling()+10)/20+surface.zminV);
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
