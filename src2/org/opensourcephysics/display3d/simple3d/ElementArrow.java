/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.awt.Color;
import java.awt.Graphics2D;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementArrow</p>
 * <p>Description: An Arrow using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementArrow extends ElementSegment implements org.opensourcephysics.display3d.core.ElementArrow {
  // -------------------------------------
  // Super methods overwritten
  // -------------------------------------
  void draw(Graphics2D _g2, int _index) {
    // Allow the panel to adjust color according to depth
    Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), objects[_index].getDistance());
    if(_index<(div-1)) {
      _g2.setStroke(getRealStyle().getLineStroke());
      _g2.setColor(theColor);
      _g2.drawLine(aCoord[_index], bCoord[_index], aCoord[_index+1], bCoord[_index+1]);
    } else { // Draw the head
      Color theFillColor = getDrawingPanel3D().projectColor(getRealStyle().getFillColor(), objects[_index].getDistance());
      drawHead(_g2, aCoord[_index], bCoord[_index], theColor, theFillColor);
    }
  }

  synchronized void drawQuickly(Graphics2D _g2) {
    if(!isReallyVisible()) {
      return;
    }
    if(hasChanged()) {
      computeDivisions();
      projectPoints();
    } else if(needsToProject()) {
      projectPoints();
    }
    drawHead(_g2, aCoord[0], bCoord[0], getRealStyle().getLineColor(), getRealStyle().getFillColor());
  }

  // -------------------------------------
  // The head
  // -------------------------------------
  static final private double ARROW_CST = 0.35;
  static final private double ARROW_MAX = 25.0;
  private int headPoints = 0;
  private int headA[] = new int[10], headB[] = new int[10]; // Used to display the head

  void projectPoints() {
    super.projectPoints();
    // Now compute the head
    double a = aCoord[div]-aCoord[0];
    double b = bCoord[div]-bCoord[0];
    double h = Math.sqrt(a*a+b*b);
    // FKH 20020331
    if(h==0.0) {
      headPoints = 0;
      return;
    }
    a = ARROW_CST*a/h;
    b = ARROW_CST*b/h;
    if(h>ARROW_MAX) {
      a *= ARROW_MAX/h;
      b *= ARROW_MAX/h;
    }
    int p0 = (int) (aCoord[div]-a*h);
    int q0 = (int) (bCoord[div]-b*h);
    a *= h/2.0;
    b *= h/2.0;
    headPoints = 6;
    headA[0] = p0;
    headB[0] = q0;
    headA[1] = p0-(int) b;
    headB[1] = q0+(int) a;
    headA[2] = aCoord[div];
    headB[2] = bCoord[div];
    headA[3] = p0+(int) b;
    headB[3] = q0-(int) a;
    headA[4] = p0;
    headB[4] = q0;
  }

  private void drawHead(Graphics2D _g2, int a1, int b1, Color _color, Color _fill) {
    _g2.setStroke(getRealStyle().getLineStroke());
    if(headPoints==0) {
      _g2.setColor(_color);
      _g2.drawLine(a1, b1, aCoord[div], bCoord[div]);
      return;
    }
    int n = headPoints-1;
    headA[n] = a1;
    headB[n] = b1;
    if((_fill!=null)&&getRealStyle().isDrawingFill()) {
      _g2.setPaint(_fill);
      _g2.fillPolygon(headA, headB, n);
    }
    _g2.setColor(_color);
    _g2.drawPolyline(headA, headB, headPoints);
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------

  /**
   * Returns an XML.ObjectLoader to save and load object data.
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  static private class Loader extends org.opensourcephysics.display3d.core.ElementArrow.Loader {
    public Object createObject(XMLControl control) {
      return new ElementArrow();
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
