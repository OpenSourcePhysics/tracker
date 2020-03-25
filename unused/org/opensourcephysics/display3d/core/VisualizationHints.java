/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * <p>Title: VisualizationHints</p>
 * <p>Description: Hints to a DrawingPanel3D about how it should look.
 * Hints can be ignored by the panel, depending on the implementation.</p>
 * @author Francisco Esquembre
 * @version March 2005
 */
public interface VisualizationHints {
  int DECORATION_NONE = 0;
  int DECORATION_AXES = 1;
  int DECORATION_CUBE = 2;
  int CURSOR_NONE = 0;
  int CURSOR_XYZ = 1;
  int CURSOR_CUBE = 2;
  int CURSOR_CROSSHAIR = 3;

  /**
   * Types of decoration displayed. One of the following
   * <ul>
   *   <li><b>DECORATION_NONE</b>: No decoration</li>
   *   <li><b>DECORATION_AXES</b>: Display labelled axes</li>
   *   <li><b>DECORATION_CUBE</b>: Display the bounding box</li>
   * </ul>
   * @param type the desired value
   */
  public void setDecorationType(int type);

  public int getDecorationType();

  /**
   * Sets the labels for the X, Y, and Z axes (when the axes are visible).
   * @param labels a String[] array with at least three elements
   */
  public void setAxesLabels(String[] labels);

  public String[] getAxesLabels();

  /**
   * The cursor type when interacting with the panel. One of the following
   * <ul>
   *   <li><b>CURSOR_NONE</b>: No cursor lines are shown.</li>
   *   <li><b>CURSOR_XYZ</b>: X,Y, and Z lines are displayed. The default.</li>
   *   <li><b>CURSOR_CUBE</b>: A cube from the origing to the point is shown.</li>
   *   <li><b>CURSOR_CROSSHAIR</b>: Lines parallel to the axes that cross at the given point are shown.</li>
   * </ul>
   * @param mode the desired value
   */
  public void setCursorType(int type);

  public int getCursorType();

  /**
   * Whether the panel should try to remove hidden lines
   * @param remove the desired value
   */
  public void setRemoveHiddenLines(boolean remove);

  public boolean isRemoveHiddenLines();

  /**
   * Whether the panel can draw quickly when it is dragged for a
   * new view point
   * @param allow the desired value
   */
  public void setAllowQuickRedraw(boolean allow);

  public boolean isAllowQuickRedraw();

  /**
   * Whether the panel should display far objects darker
   * @param useIt the desired value
   */
  public void setUseColorDepth(boolean useIt);

  public boolean isUseColorDepth();

  /**
   * At which location should the panel display the coordinates
   * when dragging a point
   * The location must be one of the following:
   * <ul>
   *   <li> DrawingPanel3D.BOTTOM_LEFT
   *   <li> DrawingPanel3D.BOTTOM_RIGHT
   *   <li> DrawingPanel3D.TOP_RIGHT
   *   <li> DrawingPanel3D.TOP_LEFT
   * </ul>
   * A negative value for the location means
   */
  public void setShowCoordinates(int location);

  public int getShowCoordinates();

  /**
   * Sets the format to display the X coordinate when dragging a point
   * @param format String parameter for a new java.text.DecimalFormat
   */
  public void setXFormat(String format);

  public String getXFormat();

  /**
   * Sets the format to display the Y coordinate when dragging a point
   * @param format String parameter for a new java.text.DecimalFormat
   */
  public void setYFormat(String format);

  public String getYFormat();

  /**
   * Sets the format to display the Z coordinate when dragging a point
   * @param format String parameter for a new java.text.DecimalFormat
   */
  public void setZFormat(String format);

  public String getZFormat();

  /**
   * Copies its data from another set of hints
   * @param hints
   */
  public void copyFrom(VisualizationHints hints);

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  abstract static class Loader extends XMLLoader {
    abstract public Object createObject(XMLControl control);

    public void saveObject(XMLControl control, Object obj) {
      VisualizationHints hints = (VisualizationHints) obj;
      control.setValue("decoration type", hints.getDecorationType());       //$NON-NLS-1$
      control.setValue("cursor type", hints.getCursorType());               //$NON-NLS-1$
      control.setValue("remove hidden lines", hints.isRemoveHiddenLines()); //$NON-NLS-1$
      control.setValue("allow quick redraw", hints.isAllowQuickRedraw());   //$NON-NLS-1$
      control.setValue("use color depth", hints.isUseColorDepth());         //$NON-NLS-1$
      control.setValue("show coordinates at", hints.getShowCoordinates());  //$NON-NLS-1$
      control.setValue("x format", hints.getXFormat());                     //$NON-NLS-1$
      control.setValue("y format", hints.getYFormat());                     //$NON-NLS-1$
      control.setValue("z format", hints.getZFormat());                     //$NON-NLS-1$
      control.setValue("axes labels", hints.getAxesLabels());               //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      VisualizationHints hints = (VisualizationHints) obj;
      hints.setDecorationType(control.getInt("decoration type"));            //$NON-NLS-1$
      hints.setCursorType(control.getInt("cursor type"));                    //$NON-NLS-1$
      hints.setRemoveHiddenLines(control.getBoolean("remove hidden lines")); //$NON-NLS-1$
      hints.setAllowQuickRedraw(control.getBoolean("allow quick redraw"));   //$NON-NLS-1$
      hints.setUseColorDepth(control.getBoolean("use color depth"));         //$NON-NLS-1$
      hints.setShowCoordinates(control.getInt("show coordinates at"));       //$NON-NLS-1$
      hints.setXFormat(control.getString("x format"));                       //$NON-NLS-1$
      hints.setYFormat(control.getString("y format"));                       //$NON-NLS-1$
      hints.setZFormat(control.getString("z format"));                       //$NON-NLS-1$
      hints.setAxesLabels((String[]) control.getObject("axes labels"));      //$NON-NLS-1$
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
