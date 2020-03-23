/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.opensourcephysics.display.DrawingPanel;

/**
 * This obtains line profile data from a video image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class TLineProfile extends TLine {
  // instance fields
  protected int[] pixels = new int[0];
  protected int[] values = new int[0];

  /**
   * Constructs a TLineProfile with specified end points.
   *
   * @param x1 x-component of line end 1
   * @param y1 y-component of line end 1
   * @param x2 x-component of line end 2
   * @param y2 y-component of line end 2
   */
  public TLineProfile(double x1, double y1, double x2, double y2) {
    end1 = new LineEnd(x1, y1);
    end2 = new LineEnd(x2, y2);
  }

  /**
   * Override the draw method to get the profile data.
   *
   * @param panel the drawing panel requesting the drawing
   * @param g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!(panel instanceof VideoPanel)) {
      return;
    }
    VideoPanel vidPanel = (VideoPanel) panel;
    if(!isVisible()) {
      return;
    }
    super.draw(vidPanel, g);
    getProfileData(vidPanel);
  }

  /**
   * Gets the line profile.
   *
   * @return the line profile
   */
  public int[] getProfile() {
    return values;
  }

  //________________________ protected methods ________________________

  /**
   * Gets the line profile data.
   *
   * @param vidPanel the video panel
   */
  protected void getProfileData(VideoPanel vidPanel) {
    if(vidPanel.getVideo()==null) {
      return;
    }
    int length = (int) end1.distance(end2);
    if(length!=pixels.length) {
      pixels = new int[length];
      values = new int[length];
    }
    BufferedImage image = vidPanel.getVideo().getImage();
    if(image.getType()==BufferedImage.TYPE_INT_RGB) {
      try {
        int x = Math.min((int) end1.getX(), (int) end2.getX());
        int y = (int) end1.getY();
        image.getRaster().getDataElements(x, y, length, 1, pixels);
        for(int i = 0; i<pixels.length; i++) {
          int pixel = pixels[i];
          int r = (pixel>>16)&0xff; // red
          int g = (pixel>>8)&0xff;  // green
          int b = (pixel)&0xff;     // blue
          values[i] = (r+g+b)/3;
        }
      } catch(ArrayIndexOutOfBoundsException ex) {
        ex.printStackTrace();
      }
    }
  }

  //______________________ inner LineEnd class ____________________________

  /**
   * Inner class restricts line ends to horizontal translations.
   */
  private class LineEnd extends TPoint {
    /**
     * Constructs a LineEnd object with specified image coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public LineEnd(double x, double y) {
      super(x, y);
    }

    /**
     * Overrides TPoint setXY to allow only horizontal displacements.
     *
     * @param x the x position
     * @param y the y position
     */
    public void setXY(double x, double y) {
      setLocation(x, getY());
    }

    /**
     * Overrides TPoint translate to allow ends to be translated
     * vertically.
     *
     * @param dx the x displacement
     * @param dy the y displacement
     */
    public void translate(double dx, double dy) {
      setLocation(getX()+dx, getY()+dy);
    }

    /**
     * Overrides TPoint getBounds method
     *
     * @param vidPanel the video panel
     * @return the bounding rectangle
     */
    public Rectangle getBounds(VideoPanel vidPanel) {
      return TLineProfile.this.getBounds(vidPanel);
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
