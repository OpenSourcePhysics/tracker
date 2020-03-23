/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * MeasuredImage contains an image and a scale in world units.
 *
 * When a MeasuredImage is added to a drawing panel, the image will scale itself to
 * the panel's world units.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class MeasuredImage implements Measurable {
  protected BufferedImage image;
  protected double xmin, xmax, ymin, ymax;
  protected boolean visible = true;

  /**
   * Constructs a MeasuredImage with a pixel scale.
   *
   */
  public MeasuredImage() {
    this(null, 0, 0, 0, 0);
  }

  /**
   * Constructs a MeasuredImage with a pixel scale.
   *
   * @param image the image
   */
  public MeasuredImage(BufferedImage image) {
    this(image, 0, image.getWidth(), 0, image.getHeight());
  }

  /**
   * Constructs a MeasuredImage with the given scale.
   *
   * @param _image
   * @param _xmin
   * @param _xmax
   * @param _ymin
   * @param _ymax
   */
  public MeasuredImage(BufferedImage _image, double _xmin, double _xmax, double _ymin, double _ymax) {
    image = _image;
    xmin = _xmin;
    xmax = _xmax;
    ymin = _ymin;
    ymax = _ymax;
  }

  public void setImage(BufferedImage _image) {
    image = _image;
  }

  public BufferedImage getImage() {
    return image;
  }

  /**
   * Sets the visibility of the lattice.
   * Drawing will be disabled if visible is false.
   *
   * @param isVisible
   */
  public void setVisible(boolean isVisible) {
    visible = isVisible;
  }

  /**
   * Draws the image on the panel.
   *
   * @param panel
   * @param g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    if(image==null) {
      panel.setMessage(DisplayRes.getString("MeasuredImage.NoImage")); //$NON-NLS-1$
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    AffineTransform gat = g2.getTransform(); // save graphics transform
    RenderingHints hints = g2.getRenderingHints();
    if (!OSPRuntime.isMac()) {  //Rendering hint bug in Mac Snow Leopard 
      g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    double sx = (xmax-xmin)*panel.xPixPerUnit/image.getWidth();
    double sy = (ymax-ymin)*panel.yPixPerUnit/image.getHeight();
    g2.transform(AffineTransform.getTranslateInstance(panel.leftGutter+panel.xPixPerUnit*(xmin-panel.xmin), panel.topGutter+panel.yPixPerUnit*(panel.ymax-ymax)));
    g2.transform(AffineTransform.getScaleInstance(sx, sy));
    g2.drawImage(image, 0, 0, panel);
    g2.setTransform(gat);        // restore graphics transform
    g2.setRenderingHints(hints); // restore the hints
  }

  public boolean isMeasured() {
    if(image==null) {
      return false;
    }
    return true;
  }

  public double getXMin() {
    return xmin;
  }

  public double getXMax() {
    return xmax;
  }

  public double getYMin() {
    return ymin;
  }

  public double getYMax() {
    return ymax;
  }

  public void setXMin(double _xmin) {
    xmin = _xmin;
  }

  public void setXMax(double _xmax) {
    xmax = _xmax;
  }

  public void setYMin(double _ymin) {
    ymin = _ymin;
  }

  public void setYMax(double _ymax) {
    ymax = _ymax;
  }

  public void setMinMax(double _xmin, double _xmax, double _ymin, double _ymax) {
    xmin = _xmin;
    xmax = _xmax;
    ymin = _ymin;
    ymax = _ymax;
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
