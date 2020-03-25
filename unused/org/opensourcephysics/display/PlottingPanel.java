/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.axes.CartesianAxes;
import org.opensourcephysics.display.axes.CartesianInteractive;
import org.opensourcephysics.display.axes.CustomAxes;
import org.opensourcephysics.display.axes.DrawableAxes;
import org.opensourcephysics.display.axes.PolarAxes;
import org.opensourcephysics.display.axes.PolarType2;
import org.opensourcephysics.display.axes.XYAxis;
import org.opensourcephysics.numerics.FunctionTransform;
import org.opensourcephysics.numerics.LogBase10Function;

/**
 * A Drawing Panel that has an X axis, a Y axis, and a title.
 *
 * @author Wolfgang Christian
 */
public class PlottingPanel extends InteractivePanel {
	protected DrawableAxes axes;
	protected FunctionTransform functionTransform = new FunctionTransform();
	protected final static double log10 = Math.log(10);
	protected final static LogBase10Function logBase10Function = new LogBase10Function();

  /**
   *  Constructs a new PlottingPanel that uses the given X axis label, Y axis
   *  label, and plot title.
   *
   * @param  xlabel     The X axis label.
   * @param  ylabel     The Y axis label.
   * @param  plotTitle  The plot title.
   */
  public PlottingPanel(String xlabel, String ylabel, String plotTitle) {
    this(xlabel, ylabel, plotTitle, XYAxis.LINEAR, XYAxis.LINEAR);
  }

  /**
   *  Constructs a new PlottingPanel that uses the given X axis type and Y axis
   *  type.
   *
   * @param  _xAxisType  The X axis type.
   * @param  _yAxisType  The Y axis type.
   */
  public PlottingPanel(int _xAxisType, int _yAxisType) {
    this("x", "y", DisplayRes.getString("PlottingPanel.DefaultTitle"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      _xAxisType, _yAxisType);
  }

  /**
   *  Constructs a new PlottingPanel with cartesian axes that use the given X axis label, Y axis
   *  label, and plot title.
   *
   * @param  xlabel      The X axis label.
   * @param  ylabel      The Y axis label.
   * @param  plotTitle   The plot title.
   * @param  xAxisType   Description of Parameter
   * @param  yAxisType   Description of Parameter
   */
  public PlottingPanel(String xlabel, String ylabel, String plotTitle, int xAxisType, int yAxisType) {
//    axes = new CartesianType1(this);
  	// axes changed to interactive by default. D Brown 2012-01-27
    axes = new CartesianInteractive(this);
    axes.setXLabel(xlabel, null);
    axes.setYLabel(ylabel, null);
    axes.setTitle(plotTitle, null);
    functionTransform.setXFunction(logBase10Function); // set function transforms but do not apply functions
    functionTransform.setYFunction(logBase10Function);
    if(xAxisType==XYAxis.LOG10) {
      logScaleX = true;
    }
    if(yAxisType==XYAxis.LOG10) {
      logScaleY = true;
    }
    setLogScale(logScaleX, logScaleY);
  }

  /**
   * Gets the interactive drawable that was accessed by the last mouse event.
   *
   * This methods overrides the default implemenation in order to check for draggable axes.
   *
   * @return the interactive object
   */
  public Interactive getInteractive() {
    Interactive iad = null;
    iad = super.getInteractive();
    if((iad==null)&&(axes instanceof Interactive)) {
      // check for draggable axes
      iad = ((Interactive) axes).findInteractive(this, mouseEvent.getX(), mouseEvent.getY());
    }
    return iad;
  }

  /**
   * Gets the axes.
   *
   * @return the axes
   */
  public DrawableAxes getAxes() {
    return axes;
  }

  /**
   * Sets the axes.
   *
   * @param _axes the new axes
   */
  public void setAxes(DrawableAxes _axes) {
    axes = _axes;
    if(axes==null) {
      axes = new CustomAxes(this);
      setPreferredGutters(0, 0, 0, 0);
      setClipAtGutter(false);
      axes.setVisible(false);
    } else {
      setClipAtGutter(true);
    }
  }

  /**
   *  Converts this panel to polar coordinates
   *
   * @param plotTitle String
   * @param deltaR double
   */
  public void setPolar(String plotTitle, double deltaR) {
    if(logScaleX||logScaleY) {
      System.err.println("The axes type cannot be swithed when using logarithmetic scales."); //$NON-NLS-1$
      return;
    }
    PolarAxes axes = new PolarType2(this);
    axes.setDeltaR(deltaR);        // radial coordinate separation
    axes.setDeltaTheta(Math.PI/8); // spokes are separate by PI/8
    setTitle(plotTitle);
    setSquareAspect(true);
    setClipAtGutter(true);
  }

  /**
   *  Converts this panel to cartesian coordinates.
   *
   *
   * @param xLabel String
   * @param yLabel String
   * @param plotTitle String
   */
  public void setCartesian(String xLabel, String yLabel, String plotTitle) {
//    axes = new CartesianType1(this);
  	// axes changed to interactive by default. D Brown 2012-01-27
    axes = new CartesianInteractive(this);
    axes.setXLabel(xLabel, null);
    axes.setYLabel(yLabel, null);
    axes.setTitle(plotTitle, null);
    setClipAtGutter(true);
  }

  /**
   *  Sets the label for the X (horizontal) axis.
   *
   * @param  label  the label
   */
  public void setXLabel(String label) {
    axes.setXLabel(label, null);
  }

  /**
   *  Sets the label for the Y (vertical) axis.
   *
   * @param  label  the label
   */
  public void setYLabel(String label) {
    axes.setYLabel(label, null);
  }

  /**
   *  Sets the title.
   *
   * @param  title  the title
   */
  public void setTitle(String title) {
    axes.setTitle(title, null);
  }

  /**
   *  Sets the label and font for the X (horizontal) axis.
   *  If the font name is null, the font is unchanged.
   *
   *  @param  label  the label
   *  @param font_name the optional new font
   */
  public void setXLabel(String label, String font_name) {
    axes.setXLabel(label, font_name);
  }

  /**
   *  Sets the label and font  for the Y (vertical) axis.
   *  If the font name is null, the font is unchanged.
   *
   *  @param  label  the label
   *  @param font_name the optional new font
   */
  public void setYLabel(String label, String font_name) {
    axes.setYLabel(label, font_name);
  }

  /**
   *  Sets the title and font.
   *  If the font name is null, the font is unchanged.
   *
   *  @param title
   *  @param font_name the optional new font
   */
  public void setTitle(String title, String font_name) {
    axes.setTitle(title, font_name);
  }

  /**
   * Sets the visibility of the axes.
   * Axes that are not visible will not be drawn.
   *
   * @param isVisible
   */
  public void setAxesVisible(boolean isVisible) {
    axes.setVisible(isVisible);
  }

  /**
   * Sets Cartesian axes to log scale.
   * @param  _logScaleX  The new logScale value
   * @param  _logScaleY  The new logScale value
   */
  public void setLogScale(boolean _logScaleX, boolean _logScaleY) {
    if(axes instanceof CartesianAxes) {
      ((CartesianAxes) axes).setXLog(_logScaleX);
      logScaleX = _logScaleX;
    } else {
      logScaleX = false;
    }
    if(axes instanceof CartesianAxes) {
      ((CartesianAxes) axes).setYLog(_logScaleY);
      logScaleY = _logScaleY;
    } else {
      logScaleY = false;
    }
  }

  /**
   * Sets Cartesian x-axes to log scale.
   * @param  _logScaleX  The new logScale value
   */
  public void setLogScaleX(boolean _logScaleX) {
    if(axes instanceof CartesianAxes) {
      ((CartesianAxes) axes).setXLog(_logScaleX);
      logScaleX = _logScaleX;
    } else {
      logScaleX = false;
    }
  }

  /**
   * Sets Cartesian axes to log scale.
   * @param  _logScaleY  The new logScale value
   */
  public void setLogScaleY(boolean _logScaleY) {
    if(axes instanceof CartesianAxes) {
      ((CartesianAxes) axes).setYLog(_logScaleY);
      logScaleY = _logScaleY;
    } else {
      logScaleY = false;
    }
  }

  /**
   * Computes the size of the gutters using a Dimensioned object.
   *
   * Gutters are usually set by the axes to insure that there is enough space for axes labels.  Other objects
   * can, however, perform this function by implementing the Dimensioned interface.
   */
  protected void computeGutters() {
    resetGutters();
    Dimension interiorDimension = null;
    // dimensionSetter specifies the size of the drawable area
    if(dimensionSetter!=null) {
      interiorDimension = dimensionSetter.getInterior(this);
    }
    // give the axes a chance to set the gutters and the dimension
    if(axes instanceof Dimensioned) {
      Dimension axesInterior = ((Dimensioned) axes).getInterior(this);
      if(axesInterior!=null) {
        interiorDimension = axesInterior;
      }
    }
    if(interiorDimension!=null) { // use the dimensionSetter to set the gutters
      squareAspect = false;
      adjustableGutter = false;
      leftGutter = rightGutter = Math.max(0, getWidth()-interiorDimension.width)/2;
      topGutter = bottomGutter = Math.max(0, getHeight()-interiorDimension.height)/2;
    }
  }

  /**
   * Paints before the panel iterates through its list of Drawables.
   * @param g Graphics
   */
  protected void paintFirst(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight()); // fill the component with the background color
    g.setColor(Color.black);                   // restore the default drawing color
    if((leftGutterPreferred>0)||(topGutterPreferred>0)||(rightGutterPreferred>0)||(bottomGutterPreferred>0)) {
      axes.draw(this, g); // draw the axes
    }
  }

  /**
   *  Converts pixel to x world units.
   *
   * @param  pix
   * @return      x panel units
   */
  public double pixToX(int pix) {
    if(logScaleX) {
      return Math.pow(10, super.pixToX(pix));
    }
    return super.pixToX(pix);
  }

  /**
   *  Converts x from world to pixel units.
   *
   * @param  x
   * @return    the pixel value of the x coordinate
   */
  public int xToPix(double x) {
    if(logScaleX) {
      if(x<=0) {
        x = Math.max(Float.MIN_VALUE, xmin);
      }
      return super.xToPix(logBase10(x));
    }
    return super.xToPix(x);
  }

  /**
   * Converts x from world to graphics device units.
   * @param x
   * @return the graphics device value of the x coordinate
   */
  public float xToGraphics(double x) {
    if(logScaleX) {
      if(x<=0) {
        x = Math.max(Float.MIN_VALUE, xmin);
      }
      return super.xToGraphics(logBase10(x));
    }
    return super.xToGraphics(x);
  }

  /**
   *  Converts pixel to x world units.
   *
   * @param  pix
   * @return      x panel units
   */
  public double pixToY(int pix) {
    if(logScaleY) {
      return Math.pow(10, super.pixToY(pix));
    }
    return super.pixToY(pix);
  }

  /**
   *  Converts y from world to pixel units.
   *
   * @param  y
   * @return    the pixel value of the y coordinate
   */
  public int yToPix(double y) {
    if(logScaleY) {
      if(y<=0) {
        y = Math.max(Float.MIN_VALUE, ymin);
      }
      return super.yToPix(logBase10(y));
    }
    return super.yToPix(y);
  }

  /**
   * Converts y from world to graphics device units.
   * @param y
   * @return the graphics device value of the x coordinate
   */
  public float yToGraphics(double y) {
    if(logScaleY) {
      if(y<=0) {
        y = Math.max(Float.MIN_VALUE, ymin);
      }
      return super.yToGraphics(logBase10(y));
    }
    return super.yToGraphics(y);
  }

  /**
   * Gets the bottom gutter of this DrawingPanel.
   *
   * @return bottom gutter
   */
  public int getBottomGutter() {
    return Math.max(bottomGutter, bottomGutterPreferred);
  }

  /**
   * Gets the bottom gutter of this DrawingPanel.
   *
   * @return right gutter
   */
  public int getTopGutter() {
    return Math.max(topGutter, topGutterPreferred);
  }

  /*
   * TO DO: Fix setPixelScale-what to do if min or max is 0 and using log scale
   */

  /**
   *  Calculates min and max values and the affine transformation based on the
   *  current size of the panel and the squareAspect boolean.
   */
  public void setPixelScale() {
    xmin = xminPreferred; // start with the preferred values.
    xmax = xmaxPreferred;
    ymin = yminPreferred;
    ymax = ymaxPreferred;
    if((dimensionSetter==null)) {                             // gutters have not been set by dimension setter
      leftGutter = Math.max(leftGutter, leftGutterPreferred); // no smaller than preferred gutters
      topGutter = Math.max(topGutter, topGutterPreferred);
      rightGutter = Math.max(rightGutter, rightGutterPreferred);
      bottomGutter = Math.max(bottomGutter, bottomGutterPreferred);
    }
    if(logScaleX) {
      xmin = logBase10(Math.max(xmin, 1.0e-30));
      xmax = logBase10(Math.max(xmax, 1.0e-30));
      if(xmin==0) { // FIX_ME
        xmin = 0.00000001;
      }
      if(xmax==0) { // FIX_ME
        xmax = Math.max(xmin+0.00000001, 0.00000001);
      }
    }
    if(logScaleY) {
      ymin = logBase10(Math.max(ymin, 1.0e-30));
      ymax = logBase10(Math.max(ymax, 1.0e-30));
      if(ymin==0) { // FIX_ME
        ymin = 0.00000001;
      }
      if(ymax==0) { // FIX_ME
        ymax = Math.max(ymin+0.00000001, 0.00000001);
      }
    }
    width = getWidth();
    height = getHeight();
    if(fixedPixelPerUnit) { // the user has specified a fixed pixel scale
      xmin = (xmaxPreferred+xminPreferred)/2-Math.max(width-leftGutter-rightGutter-1, 1)/xPixPerUnit/2;
      xmax = (xmaxPreferred+xminPreferred)/2+Math.max(width-leftGutter-rightGutter-1, 1)/xPixPerUnit/2;
      ymin = (ymaxPreferred+yminPreferred)/2-Math.max(height-bottomGutter-topGutter-1, 1)/yPixPerUnit/2;
      ymax = (ymaxPreferred+yminPreferred)/2+Math.max(height-bottomGutter-topGutter-1, 1)/yPixPerUnit/2;
      functionTransform.setTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin*xPixPerUnit+leftGutter, ymax*yPixPerUnit+topGutter);
      functionTransform.setApplyXFunction(false);
      functionTransform.setApplyYFunction(false);
      functionTransform.getMatrix(pixelMatrix); // puts the transformation into the pixel matrix
      return;
    }
    xPixPerUnit = (width-leftGutter-rightGutter)/(xmax-xmin);
    yPixPerUnit = (height-bottomGutter-topGutter)/(ymax-ymin); // the y scale in pixels
    if(squareAspect&&!adjustableGutter) {
      double stretch = Math.abs(xPixPerUnit/yPixPerUnit);
      if(stretch>=1) {                                               // make the x range bigger so that aspect ratio is one
        stretch = Math.min(stretch, width);                          // limit the stretch
        xmin = xminPreferred-(xmaxPreferred-xminPreferred)*(stretch-1)/2.0;
        xmax = xmaxPreferred+(xmaxPreferred-xminPreferred)*(stretch-1)/2.0;
        xPixPerUnit = (width-leftGutter-rightGutter)/(xmax-xmin);  // the x scale in pixels per unit
      } else {                                                       // make the y range bigger so that aspect ratio is one
        stretch = Math.max(stretch, 1.0/height);                     // limit the stretch
        ymin = yminPreferred-(ymaxPreferred-yminPreferred)*(1.0/stretch-1)/2.0;
        ymax = ymaxPreferred+(ymaxPreferred-yminPreferred)*(1.0/stretch-1)/2.0;
        yPixPerUnit = (height-bottomGutter-topGutter)/(ymax-ymin); // the y scale in pixels per unit
      }
    }
    if(squareAspect&&adjustableGutter) {         // axis min-max do not change but gutters change
      if(Math.abs(xPixPerUnit/yPixPerUnit)>=1) { // x range is smaller so make the x gutters bigger
        xPixPerUnit = yPixPerUnit;
        float gutter = (width-(float) Math.abs((xmax-xmin)*xPixPerUnit));
        leftGutter = (int) (gutter/2.0f+leftGutterPreferred-rightGutterPreferred+0.5f);
        rightGutter = (int) (gutter-leftGutter-0.5);
        leftGutter = Math.max(0, leftGutter);
        rightGutter = Math.max(0, rightGutter);
      } else {                                   // make the y gutters bigger
        yPixPerUnit = xPixPerUnit;
        float gutter = height-(float) Math.abs((ymax-ymin)*yPixPerUnit);
        topGutter = (int) (gutter/2.0f+topGutterPreferred-bottomGutterPreferred+0.5f);
        bottomGutter = (int) (gutter-topGutter);
        topGutter = Math.max(0, topGutter);
        bottomGutter = Math.max(0, bottomGutter);
      }
    }
    functionTransform.setTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin*xPixPerUnit+leftGutter, ymax*yPixPerUnit+topGutter);
    if(logScaleX) {
      functionTransform.setApplyXFunction(true);
    } else {
      functionTransform.setApplyXFunction(false);
    }
    if(logScaleY) {
      functionTransform.setApplyYFunction(true);
    } else {
      functionTransform.setApplyYFunction(false);
    }
    functionTransform.getMatrix(pixelMatrix);
  }

  /**
   * Recomputes the pixel transformation based on the current minimum and maximum values and the gutters.
   */
  public void recomputeTransform() {
    xPixPerUnit = Math.max(width-leftGutter-rightGutter, 1)/(xmax-xmin);
    yPixPerUnit = Math.max(height-bottomGutter-topGutter, 1)/(ymax-ymin); // the y scale in pixels
    functionTransform.setTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin*xPixPerUnit+leftGutter, ymax*yPixPerUnit+topGutter);
    if(logScaleX) {
      functionTransform.setApplyXFunction(true);
    } else {
      functionTransform.setApplyXFunction(false);
    }
    if(logScaleY) {
      functionTransform.setApplyYFunction(true);
    } else {
      functionTransform.setApplyYFunction(false);
    }
    functionTransform.getMatrix(pixelMatrix);
  }

  /**
   * Gets the affine transformation that converts from world to pixel coordinates.
   * @return the affine transformation
   */
  public AffineTransform getPixelTransform() {
    return(AffineTransform) functionTransform.clone();
  }

  /**
   * Method logBase10
   *
   * @param x
   *
   * @return the log
   */
  static double logBase10(double x) {
    return Math.log(x)/log10;
  }

  /**
   * Returns an XML.ObjectLoader to save and load object data.
   *
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new PlottingPanelLoader();
  }

  /**
   * A class to save and load PlottingPanel data.
   */
  static class PlottingPanelLoader extends DrawingPanelLoader {
    /**
     * Saves PlottingPanel data in an XMLControl.
     *
     * @param control the control
     * @param obj the DrawingPanel to save
     */
    public void saveObject(XMLControl control, Object obj) {
      PlottingPanel panel = (PlottingPanel) obj;
      control.setValue("title", panel.axes.getTitle());         //$NON-NLS-1$
      control.setValue("x axis label", panel.axes.getXLabel()); //$NON-NLS-1$
      control.setValue("y axis label", panel.axes.getYLabel()); //$NON-NLS-1$
      super.saveObject(control, obj);
    }

    /**
     * Creates a PlottingPanel.
     *
     * @param control the control
     * @return the newly created panel
     */
    public Object createObject(XMLControl control) {
      String title = control.getString("title");         //$NON-NLS-1$
      String xlabel = control.getString("x axis label"); //$NON-NLS-1$
      String ylabel = control.getString("y axis label"); //$NON-NLS-1$
      return new PlottingPanel(xlabel, ylabel, title);
    }

    /**
     * Loads a PlottingPanel with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      PlottingPanel panel = (PlottingPanel) obj;
      panel.setTitle(control.getString("title"));         //$NON-NLS-1$
      panel.setXLabel(control.getString("x axis label")); //$NON-NLS-1$
      panel.setYLabel(control.getString("y axis label")); //$NON-NLS-1$
      super.loadObject(control, obj);
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
