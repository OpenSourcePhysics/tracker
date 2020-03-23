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
import java.util.Collection;
import org.opensourcephysics.display.Drawable;

/**
 * This defines methods used by drawable images.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public interface DrawableImage extends Drawable {
  /**
   * Gets the image as a BufferedImage.
   *
   * @return the image as a BufferedImage
   */
  public BufferedImage getImage();

  /**
   * Shows or hides the image.
   *
   * @param visible <code>true</code> to make the image visible
   */
  public void setVisible(boolean visible);

  /**
   * Gets the visibility of the image.
   *
   * @return <code>true</code> if the image is visible
   */
  public boolean isVisible();

  /**
   * Sets the filter stack.
   *
   * @param stack the new filter stack
   */
  public void setFilterStack(FilterStack stack);

  /**
   * Gets the filter stack.
   *
   * @return the filter stack
   */
  public FilterStack getFilterStack();

  /**
   * Sets a user property of the image.
   *
   * @param name the name of the property
   * @param value the value of the property
   */
  public void setProperty(String name, Object value);

  /**
   * Gets a user property of the image. May return null.
   *
   * @param name the name of the property
   * @return the value of the property
   */
  public Object getProperty(String name);

  /**
   * Gets an array of user properties names.
   *
   * @return a mapping of property names to values
   */
  public Collection<String> getPropertyNames();

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
