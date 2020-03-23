/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * A class that saves and loads a GeneralPath in an XMLControl.
 */
public class GeneralPathLoader extends XMLLoader {
  public void saveObject(XMLControl control, Object obj) {
    GeneralPath shape = (GeneralPath) obj;
    // iterator with line flatness better than 0.001
    PathIterator it = shape.getPathIterator(null, 0.001);
    control.setValue("winding rule", it.getWindingRule()); //$NON-NLS-1$
    control.setValue("segments", savePathSegments(it));    //$NON-NLS-1$
  }

  public Object createObject(XMLControl control) {
    return new GeneralPath(); // default shape is a GeneralPath
  }

  /**
   * Saves the path segments in a string.
   *
   * @param it PathIterator
   * @return String
   */
  String savePathSegments(PathIterator it) {
    StringBuffer sb = new StringBuffer();
    float[] coord = new float[6];
    double x1 = 0, y1 = 0;
    while(!it.isDone()) {
      switch(it.currentSegment(coord)) {
         case PathIterator.SEG_LINETO :
           x1 = coord[0];
           y1 = coord[1];
           sb.append("<LINETO "+x1+" "+y1+">");                                              //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
           break;
         case PathIterator.SEG_MOVETO :
           x1 = coord[0];
           y1 = coord[1];
           sb.append("<MOVETO "+x1+" "+y1+">");                                              //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
           break;
         case PathIterator.SEG_CLOSE :
           sb.append("<CLOSE>");                                                             //$NON-NLS-1$
           break;
         default :
           System.out.println("Segment Type not supported. Type="+it.currentSegment(coord)); //$NON-NLS-1$
      }
      it.next();
    }
    return sb.toString();
  }

  /**
   * Loads the path with the segments from the given string.
   *
   * @param path GeneralPath
   * @param segments String
   */
  void loadPathSegments(GeneralPath path, String segments) {
    String[] segs = segments.split(">"); //$NON-NLS-1$
    for(int i = 0, n = segs.length; i<n; i++) {
      if(segs[i].startsWith("<LINETO ")) {        //$NON-NLS-1$
        String[] vals = segs[i].split(" ");       //$NON-NLS-1$
        path.lineTo(Float.parseFloat(vals[1]), Float.parseFloat(vals[2]));
      } else if(segs[i].startsWith("<MOVETO ")) { //$NON-NLS-1$
        String[] vals = segs[i].split(" ");       //$NON-NLS-1$
        path.moveTo(Float.parseFloat(vals[1]), Float.parseFloat(vals[2]));
      } else if(segs[i].startsWith("<CLOSE")) {   //$NON-NLS-1$
        path.closePath();
      }
    }
  }

  public Object loadObject(XMLControl control, Object obj) {
    GeneralPath path = (GeneralPath) obj;
    path.reset();
    path.setWindingRule(control.getInt("winding rule"));   //$NON-NLS-1$
    loadPathSegments(path, control.getString("segments")); //$NON-NLS-1$
    return path;
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
