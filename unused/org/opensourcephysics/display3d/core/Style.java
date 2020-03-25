/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import java.awt.Color;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * <p>Title: Style</p>
 * <p>Description: A class that holds display suggestions for 3D Elements.
 * Actual Elements may use or not all of the suggestions provided.</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface Style {
  static final public int CENTERED = 0;
  static final public int NORTH = 1;
  static final public int SOUTH = 2;
  static final public int EAST = 3;
  static final public int WEST = 4;
  static final public int NORTH_EAST = 5;
  static final public int NORTH_WEST = 6;
  static final public int SOUTH_EAST = 7;
  static final public int SOUTH_WEST = 8;

  public void setLineColor(Color _color);

  public Color getLineColor();

  public void setLineWidth(float _width);

  public float getLineWidth();

  public void setFillColor(Color _color);

  public Color getFillColor();

  public void setResolution(Resolution _res);

  public Resolution getResolution();

  public boolean isDrawingFill();

  public void setDrawingFill(boolean drawsFill);

  public boolean isDrawingLines();

  public void setDrawingLines(boolean drawsLines);

  public void setDepthFactor(double factor);

  public double getDepthFactor();

  public void setTexture(String file1, String file2, double transparency, boolean combine);

  public String[] getTextures();

  public double getTransparency();

  public boolean getCombine();

  public void setRelativePosition(int _position);

  public int getRelativePosition();

  public void copyTo(Style target);

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends XMLLoader {
    abstract public Object createObject(XMLControl control);

    public void saveObject(XMLControl control, Object obj) {
      Style style = (Style) obj;
      control.setValue("line color", style.getLineColor());      //$NON-NLS-1$
      control.setValue("line width", style.getLineWidth());      //$NON-NLS-1$
      control.setValue("fill color", style.getFillColor());      //$NON-NLS-1$
      control.setValue("resolution", style.getResolution());     //$NON-NLS-1$
      control.setValue("drawing fill", style.isDrawingFill());   //$NON-NLS-1$
      control.setValue("drawing lines", style.isDrawingLines()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      Style style = (Style) obj;
      style.setLineColor((Color) control.getObject("line color"));                                            //$NON-NLS-1$
      style.setLineWidth((float) control.getDouble("line width"));                                            //$NON-NLS-1$
      style.setFillColor((Color) control.getObject("fill color"));                                            //$NON-NLS-1$
      style.setResolution((org.opensourcephysics.display3d.core.Resolution) control.getObject("resolution")); //$NON-NLS-1$
      style.setDrawingFill(control.getBoolean("drawing fill"));   //$NON-NLS-1$
      style.setDrawingLines(control.getBoolean("drawing lines")); //$NON-NLS-1$
      return obj;
    }

  } // End of Loader class

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
