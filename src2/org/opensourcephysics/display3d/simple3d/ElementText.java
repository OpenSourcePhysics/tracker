/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.TextLine;

/**
 * <p>Title: ElementText</p>
 * <p>Description: A Text using the painter's algorithm</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public class ElementText extends Element implements org.opensourcephysics.display3d.core.ElementText {
  // Configuration variables
  private int justify = JUSTIFICATION_CENTER;
  private double angle = 0.0;
  // Implementation variables
  private double[] coordinates = new double[3]; // The input for all projections
  private double[] pixel = new double[3];       // The ouput of all projections
  private Object3D[] objects = new Object3D[] {new Object3D(this, 0)};
  private AffineTransform transform = new AffineTransform();
  private TextLine textLine = new TextLine();

  // -------------------------------------
  // New configuration methods
  // -------------------------------------

  /**
   * Constructor ElementText
   */
  public ElementText() {
    super();
  }

  /**
   * Constructor ElementText
   * @param text
   */
  public ElementText(String text) {
    this();
    setText(text);
  }

  public void setText(String text) {
    textLine.setText(text);
  }

  public String getText() {
    return textLine.getText();
  }

  public void setFont(Font font) {
    textLine.setFont(font);
  }

  public Font getFont() {
    return textLine.getFont();
  }

  public void setJustification(int justification) {
    this.justify = justification;
    switch(justification) {
       default :
       case JUSTIFICATION_CENTER :
         textLine.setJustification(TextLine.CENTER);
         break;
       case JUSTIFICATION_LEFT :
         textLine.setJustification(TextLine.LEFT);
         break;
       case JUSTIFICATION_RIGHT :
         textLine.setJustification(TextLine.RIGHT);
         break;
    }
  }

  public int getJustification() {
    return this.justify;
  }

  public void setRotationAngle(double angle) {
    this.angle = angle;
  }

  public double getRotationAngle() {
    return this.angle;
  }

  // -------------------------------------
  // Abstract part of Element or Parent methods overwritten
  // -------------------------------------
  Object3D[] getObjects3D() {
    if(!isReallyVisible()) {
      return null;
    }
    if(hasChanged()||needsToProject()) {
      projectPoints();
    }
    return objects;
  }

  void draw(Graphics2D _g2, int _index) {
    // Allow the panel to adjust color according to depth
    Color theColor = getDrawingPanel3D().projectColor(getRealStyle().getLineColor(), objects[0].getDistance());
    drawIt(_g2, theColor);
  }

  void drawQuickly(Graphics2D _g2) {
    if(!isReallyVisible()) {
      return;
    }
    if(hasChanged()||needsToProject()) {
      projectPoints();
    }
    drawIt(_g2, getRealStyle().getLineColor());
  }

  void getExtrema(double[] min, double[] max) {
    min[0] = 0;
    max[0] = 0;
    min[1] = 0;
    max[1] = 0;
    min[2] = 0;
    max[2] = 0;
    sizeAndToSpaceFrame(min);
    sizeAndToSpaceFrame(max);
  }

  // -------------------------------------
  // Interaction
  // -------------------------------------
  protected InteractionTarget getTargetHit(int x, int y) {
    if(!isReallyVisible()) {
      return null;
    }
    if(hasChanged()||needsToProject()) {
      projectPoints();
    }
    if(targetPosition.isEnabled()&&(Math.abs(pixel[0]-x)<SENSIBILITY)&&(Math.abs(pixel[1]-y)<SENSIBILITY)) {
      return targetPosition;
    }
    return null;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------
  private void projectPoints() {
    coordinates[0] = coordinates[1] = coordinates[2] = 0.0;
    sizeAndToSpaceFrame(coordinates);
    getDrawingPanel3D().project(coordinates, pixel);
    objects[0].setDistance(pixel[2]*getStyle().getDepthFactor());
    setElementChanged(false);
    setNeedToProject(false);
  }

  private void drawIt(Graphics2D _g2, Color _color) {
    textLine.setColor(_color);
    if(angle!=0.0) {
      AffineTransform originalTransform = _g2.getTransform();
      transform.setTransform(originalTransform);
      transform.rotate(angle, pixel[0], pixel[1]);
      _g2.setTransform(transform);
      textLine.drawText(_g2, (int) pixel[0], (int) pixel[1]);
      _g2.setTransform(originalTransform);
    } else {
      textLine.drawText(_g2, (int) pixel[0], (int) pixel[1]);
    }
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

  static private class Loader extends org.opensourcephysics.display3d.core.ElementText.Loader {
    public Object createObject(XMLControl control) {
      return new ElementText();
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
