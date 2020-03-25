/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A DrawableBuffer contains an image of drawable objects.  This image is displayed
 * on a drawing panel whenever when the draw method is invoked.
 *
 * A drawble buffer should be used to render complex drawable objects that change infrequently.
 * Use the updateImage method to generate a new image when the properties of
 * the drawable objects change.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class DrawableBuffer implements Drawable, Measurable {
  Image image;
  boolean invalid = true;
  ArrayList<Drawable> drawableList = new ArrayList<Drawable>(); // list of Drawable objects
  Color background = Color.white;
  boolean measured = false;
  double xmin, xmax, ymin, ymax;
  boolean visible = true;

  /**
   * Constructor DrawableBuffer
   *
   */
  public DrawableBuffer() {}

  /**
   * Constructor DrawableBuffer
   *
   * @param drawable
   */
  public DrawableBuffer(Drawable drawable) {
    addDrawable(drawable);
  }

  /**
   * Adds a drawable object to the drawing buffer.
   * @param drawable
   */
  public synchronized void addDrawable(Drawable drawable) {
    if(!drawableList.contains(drawable)) {
      drawableList.add(drawable);
    }
    invalidateImage();
  }

  /**
   * Method setBackground
   *
   * @param color
   */
  public void setBackground(Color color) {
    background = color;
  }

  /**
   * Sets the bufferrer's visible flag.
   * @param vis boolean
   */
  public void setVisible(boolean vis) {
    visible = vis;
  }

  /**
   * Reads the visible flag.
   * @return boolean
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Remove all drawable objects from the drawing buffer.
   */
  public synchronized void clear() {
    drawableList.clear();
    invalidateImage();
  }

  /**
   * Invalidates the image so that it is redrawn during the next repaint operation.
   *
   */
  public void invalidateImage() {
    measured = false;
    ArrayList<Drawable> tempList = new ArrayList<Drawable>(drawableList);
    Iterator<Drawable> it = tempList.iterator();
    xmin = Double.MAX_VALUE;
    xmax = -Double.MAX_VALUE;
    ymin = Double.MAX_VALUE;
    ymax = -Double.MAX_VALUE;
    while(it.hasNext()) {
      Drawable drawable = it.next();
      if((drawable instanceof Measurable)&&((Measurable) drawable).isMeasured()) {
        xmin = Math.min(xmin, ((Measurable) drawable).getXMin());
        xmax = Math.max(xmax, ((Measurable) drawable).getXMax());
        ymin = Math.min(ymin, ((Measurable) drawable).getYMin());
        ymax = Math.max(ymax, ((Measurable) drawable).getYMax());
        measured = true;
      }
    }
    invalid = true;
  }

  /**
   * Updates the image using the given drawing panel to set the dimension.
   *
   * @param drawingPanel
   */
  public void updateImage(DrawingPanel drawingPanel) {
    invalid = false;
    Image newImage = image;
    int iw = drawingPanel.getWidth();
    int ih = drawingPanel.getHeight();
    if((image==null)||(image.getWidth(drawingPanel)!=iw)||(image.getHeight(drawingPanel)!=ih)) {
      newImage = drawingPanel.createImage(iw, ih);
    }
    if(newImage==null) {
      return;
    }
    Graphics g = newImage.getGraphics();
    if(g!=null) {
      if(background==null) {
        g.clearRect(0, 0, iw, ih);
      } else {
        g.setColor(background);
        g.fillRect(0, 0, iw, ih);
      }
      paintMyDrawableList(drawingPanel, g);
      g.dispose();
    }
    image = newImage;
  }

  /**
   * Method draw
   *
   * @param drawingPanel
   * @param g
   */
  public void draw(DrawingPanel drawingPanel, Graphics g) {
    if(!visible) {
      return;
    }
    if(invalid||(image==null)||(image.getWidth(drawingPanel)!=drawingPanel.getWidth())||(image.getHeight(drawingPanel)!=drawingPanel.getHeight())) {
      updateImage(drawingPanel);
    }
    if(image!=null) {
      g.drawImage(image, 0, 0, drawingPanel);
    }
  }

  /**
   * Method getXMin
   *
   *
   * @return x min
   */
  public double getXMin() {
    return xmin;
  }

  /**
   * Method getXMax
   *
   *
   * @return x max
   */
  public double getXMax() {
    return xmax;
  }

  /**
   * Method getYMin
   *
   *
   * @return ymin
   */
  public double getYMin() {
    return ymin;
  }

  /**
   * Method getYMax
   *
   *
   * @return ymax
   */
  public double getYMax() {
    return ymax;
  }

  /**
   * Tests to see if the buffer has an object with a valid measure.
   *
   * @return true if any object in the drawable list is measured
   */
  public boolean isMeasured() {
    return measured;
  }

  /**
   * Paints the drawable objects onto the image.
   * @param g
   */
  private void paintMyDrawableList(DrawingPanel drawingPanel, Graphics g) {
    ArrayList<Drawable> tempList = new ArrayList<Drawable>(drawableList);
    Iterator<Drawable> it = tempList.iterator();
    while(it.hasNext()) {
      Drawable drawable = it.next();
      drawable.draw(drawingPanel, g);
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
