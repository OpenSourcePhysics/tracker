/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import java.awt.Font;
import org.opensourcephysics.controls.XMLControl;

/**
 * <p>Title: ElementSegment</p>
 * <p>Description: A 3D Segment.</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface ElementText extends Element {
  /**
   * Center the Text over the point
   */
  public final static int JUSTIFICATION_CENTER = 0;

  /**
   * Position the Text to the Left of the  point
   */
  public final static int JUSTIFICATION_LEFT = 1;

  /**
   * Position the Text to the Right of the  point
   */
  public final static int JUSTIFICATION_RIGHT = 2;

  /**
   * Sets the text to be displayed
   * @param text the String
   */
  public void setText(String text);

  /**
   * Gets the text displayed
   */
  public String getText();

  /**
   * Sets the font for the text
   * @param font Font
   */
  public void setFont(Font font);

  /**
   * Gets the font of the text
   * @return Font
   */
  public Font getFont();

  /**
   * Sets the justification for the text
   * @param font Font
   */
  public void setJustification(int justification);

  /**
   * Gets the justification of the text
   * @return Font
   */
  public int getJustification();

  /**
   * Sets the rotation angle for the text. Default is 0.
   * @param angle the rotation angle
   */
  public void setRotationAngle(double angle);

  /**
   * Gets the rotation angle for the text
   */
  public double getRotationAngle();

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      ElementText element = (ElementText) obj;
      control.setValue("text", element.getText());                    //$NON-NLS-1$
      control.setValue("font", element.getFont());                    //$NON-NLS-1$
      control.setValue("justification", element.getJustification());  //$NON-NLS-1$
      control.setValue("rotation angle", element.getRotationAngle()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ElementText element = (ElementText) obj;
      element.setText(control.getString("text"));                    //$NON-NLS-1$
      element.setFont((Font) control.getObject("font"));             //$NON-NLS-1$
      element.setJustification(control.getInt("justification"));     //$NON-NLS-1$
      element.setRotationAngle(control.getDouble("rotation angle")); //$NON-NLS-1$
      return obj;
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
