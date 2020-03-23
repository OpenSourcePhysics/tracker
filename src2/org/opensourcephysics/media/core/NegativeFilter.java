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
import java.awt.image.BufferedImage;
import javax.swing.JDialog;

/**
 * This is a Filter that produces a negative version of the source.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class NegativeFilter extends Filter {
  // instance fields
  private int[] pixels;

  /**
   * Constructs a NegativeFilter object.
   */
  public NegativeFilter() {
    refresh();
  }

  /**
   * Applies the filter to a source image and returns the result.
   *
   * @param sourceImage the source image
   * @return the filtered image
   */
  public BufferedImage getFilteredImage(BufferedImage sourceImage) {
    if(!isEnabled()) {
      return sourceImage;
    }
    if(sourceImage!=source) {
      initialize(sourceImage);
    }
    if(sourceImage!=input) {
      gIn.drawImage(source, 0, 0, null);
    }
    setOutputToNegative(input);
    return output;
  }

  /**
   * Implements abstract Filter method.
   *
   * @return the inspector
   */
  public JDialog getInspector() {
    return null;
  }

  //_____________________________ private methods _______________________

  /**
   * Creates the input and output images and ColorConvertOp.
   *
   * @param image a new input image
   */
  private void initialize(BufferedImage image) {
    source = image;
    w = source.getWidth();
    h = source.getHeight();
    pixels = new int[w*h];
    output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    if(source.getType()==BufferedImage.TYPE_INT_RGB) {
      input = source;
    } else {
      input = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      gIn = input.createGraphics();
    }
  }

  /**
   * Sets the output image pixels to the negative of the input pixels.
   *
   * @param image the input image
   */
  private void setOutputToNegative(BufferedImage image) {
    image.getRaster().getDataElements(0, 0, w, h, pixels);
    int pixel, r, g, b;
    for(int i = 0; i<pixels.length; i++) {
      pixel = pixels[i];
      r = 255-((pixel>>16)&0xff); // neg red
      g = 255-((pixel>>8)&0xff);  // neg green
      b = 255-((pixel)&0xff);     // neg blue
      pixels[i] = (r<<16)|(g<<8)|b;
    }
    output.getRaster().setDataElements(0, 0, w, h, pixels);
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
