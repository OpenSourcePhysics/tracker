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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;

/**
 * This is a Filter that applies radial transformations to an image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class RadialDistortionFilter extends Filter {
	
  // static constants
  @SuppressWarnings("javadoc")
	public final static String RECTILINEAR = "Rectilinear"; //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String EQUIDISTANT = "Equidistant"; //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String EQUISOLID = "Equisolid"; //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String STEREOGRAPHIC = "Stereographic"; //$NON-NLS-1$
  @SuppressWarnings("javadoc")
	public final static String ORTHOGRAPHIC = "Orthographic"; //$NON-NLS-1$
  
  // static fields
  protected final static ArrayList<String> PROJECTION_TYPES = new ArrayList<String>();
  protected static FontRenderContext frc
		  = new FontRenderContext(null,   // no AffineTransform
		                          false,  // no antialiasing
		                          false); // no fractional metrics
  protected static double minRadius = 0.20, maxRadius = 1.0;
  protected static double minFOV = Math.PI/18, maxFOV = Math.PI-.001;
  
  // instance fields
  private int[] pixelsIn, pixelsOut; // pixel color values
  private double[] xOut, yOut, xIn, yIn; // pixel positions on input and output images
  private double pixelsToCorner; // half image diagonal in pixels
  private boolean isValidTransform = false, updatingDisplay = false;
  private double outputFOV;
  private boolean hasLowerLimit;
  private int lowerRadiusLimit = (int)(100*minRadius);
  
  // parameters
  private int interpolation = 1; // neighborhood size for color interpolation
  private double fixedRadius = 0.75; // radius (relative to corner distance) that remains fixed in corrected image
  private double sourceFOV = Math.PI/2;
  private String sourceProjectionType = RECTILINEAR;
  private String outputProjectionType = RECTILINEAR;
	private Color color = Color.GREEN;
  
  // inspector and circle
  private Inspector inspector;
  private Circle circle;

  static {
  	PROJECTION_TYPES.add(RECTILINEAR);
  	PROJECTION_TYPES.add(EQUISOLID);
  	PROJECTION_TYPES.add(EQUIDISTANT);
  	PROJECTION_TYPES.add(STEREOGRAPHIC);
  	PROJECTION_TYPES.add(ORTHOGRAPHIC);  	
  }
  /**
   * Constructor.
   */
  public RadialDistortionFilter() {
  	super();
  	circle = new Circle();
    refresh();
    hasInspector = true;
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
    setOutputToTransformed(input);
    return output;
  }

  /**
   * Gets the inspector for this filter.
   *
   * @return the inspector
   */
  public synchronized JDialog getInspector() {
  	Inspector myInspector = inspector;
    if (myInspector==null) {
    	myInspector = new Inspector();
    }
    if (myInspector.isModal() && vidPanel!=null) {
      frame = JOptionPane.getFrameForComponent(vidPanel);
      myInspector.setVisible(false);
      myInspector.dispose();
      myInspector = new Inspector();
    }
    inspector = myInspector;
    inspector.initialize();
    return inspector;
  }

  /**
   * Refreshes this filter's GUI
   */
  public void refresh() {
    super.refresh();
    ableButton.setText(isEnabled()? 
    		MediaRes.getString("Filter.Button.Disable"): //$NON-NLS-1$
    		MediaRes.getString("Filter.Button.Enable")); //$NON-NLS-1$
    if(inspector!=null) {
    	inspector.refreshGUI();
    }
  }
  
  /**
   * Sets the fixed radius fraction. Pixels at this distance from the image center remain fixed.
   * @param fraction the fixed radius as a fraction of the corner radius
   */
  public void setFixedRadius(double fraction) {
  	if (Double.isNaN(fraction)) return;
  	fraction = Math.abs(fraction);
  	fraction = Math.min(fraction, maxRadius);
  	fraction = Math.max(fraction, minRadius);
  	if (fixedRadius!=fraction) {
	  	fixedRadius = fraction;
	  	if (inspector!=null) inspector.updateDisplay();
	  	isValidTransform = false;
	    support.firePropertyChange("image", null, null); //$NON-NLS-1$
  	}
  }
  
  /**
   * Gets the fixed radius. Pixels at this relative distance from the image center remain fixed.
   * @return the fixed radius as a fraction of the center-to-corner distance
   */
  public double getFixedRadius() {
  	return fixedRadius;
  }
  
  /**
   * Sets the source field of view.
   * @param fov the diagonal field of view of the source image
   */
  public void setSourceFOV(double fov) {
  	if (Double.isNaN(fov)) return;
  	fov = Math.abs(fov);
  	fov = Math.min(fov, Math.PI - .0005);
  	fov = Math.max(fov, minFOV);
  	if (sourceFOV!=fov) {
  		sourceFOV = fov;
	  	if (inspector!=null) inspector.updateDisplay();
	  	isValidTransform = false;
	    support.firePropertyChange("image", null, null); //$NON-NLS-1$
  	}
  }
  
  /**
   * Gets the source field of view.
   * @return the diagonal field of view of the source image
   */
  public double getSourceFOV() {
  	return sourceFOV;
  }
  
  /**
   * Sets the source image projection type.
   * @param type one of the predefined types in the PROJECTION_TYPES list
   */
  public void setSourceProjectionType(String type) {
  	if (!sourceProjectionType.equals(type) && PROJECTION_TYPES.contains(type)) {
  		sourceProjectionType = type;
	  	hasLowerLimit = getAngleAtRadius(0.5, outputProjectionType, Math.PI/2)<getAngleAtRadius(0.5, sourceProjectionType, Math.PI/2);
	  	isValidTransform = false;
	  	if (inspector!=null) inspector.updateDisplay();
      support.firePropertyChange("image", null, null); //$NON-NLS-1$
  	}
  }
  
  /**
   * Gets the source image projection type.
   * @return the source projection types
   */
  public String getSourceProjectionType() {
  	return sourceProjectionType;
  }
  
  /**
   * Sets the output image projection type.
   * @param type one of the predefined types in the PROJECTION_TYPES list
   */
  public void setOutputProjectionType(String type) {
  	if (!outputProjectionType.equals(type) && PROJECTION_TYPES.contains(type)) {
  		outputProjectionType = type;
	  	hasLowerLimit = getAngleAtRadius(0.5, outputProjectionType, Math.PI/2)<getAngleAtRadius(0.5, sourceProjectionType, Math.PI/2);
	  	isValidTransform = false;
	  	if (inspector!=null) inspector.updateDisplay();
      support.firePropertyChange("image", null, null); //$NON-NLS-1$
  	}
  }
  
  /**
   * Gets the output image projection type.
   * @return the output projection type
   */
  public String getOutputProjectionType() {
  	return outputProjectionType;
  }
  
  //_____________________________ private methods _______________________

  /**
   * Gets a localized list of available projection types.
   *
   * @return the list of types
   */
  private String[] getProjectionTypeDescriptions() {
  	String[] types = new String[PROJECTION_TYPES.size()];
  	for (int i=0; i<types.length; i++) {
  		String next = PROJECTION_TYPES.get(i);
  		types[i] = MediaRes.getString("RadialDistortionFilter.ProjectionType."+next); //$NON-NLS-1$
  	}
  	return types;
  }
  
  /**
   * Creates the input and output images.
   *
   * @param image a new input image
   */
  private void initialize(BufferedImage image) {
    source = image;
    w = source.getWidth();
    h = source.getHeight();
    pixelsToCorner = Math.sqrt(w*w + h*h)/2;
    output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    pixelsIn = new int[w*h];
    pixelsOut = new int[w*h];    
    xIn = new double[w*h];
    yIn = new double[w*h];
    // output positions are integer pixel positions
    xOut = new double[w*h];
    yOut = new double[w*h];    
    for (int i=0; i<w; i++) {
    	for (int j=0; j<h; j++) {
    		xOut[j*w+i] = i;
    		yOut[j*w+i] = j;
    	}
    }
    if(source.getType()==BufferedImage.TYPE_INT_RGB) {
      input = source;
    } else {
      input = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      gIn = input.createGraphics();
    } 
    isValidTransform = false;
  }

  /**
   * Sets the output image pixels to a transformed version of the input pixels.
   *
   * @param image the input image
   */
  private void setOutputToTransformed(BufferedImage image) {
    // if needed, map the output (corrected) pixel positions to input pixel positions
  	if (!isValidTransform) transform(xOut, yOut, xIn, yIn);    
    
    // find output pixel color values by interpolating input pixel colors
    image.getRaster().getDataElements(0, 0, w, h, pixelsIn);
    for (int i=0; i<pixelsOut.length; i++) {
    	pixelsOut[i] = getColor(xIn[i], yIn[i], w, h, pixelsIn);
    }
  	output.getRaster().setDataElements(0, 0, w, h, pixelsOut);
  }
  
  /**
   * Transforms arrays of pixel position coordinates for source to output conversion.
   * 
   * @param xSource array of source x-coordinates
   * @param ySource array of source y-coordinates
   * @param xTrans array of transformed x-coordinates
   * @param yTrans array of transformed y-coordinates
   */
  private void transform(double[] xSource, double[] ySource, 
  		double[] xTrans, double[] yTrans) {
  	
    refreshScale();
    double xCenter = w/2.0, yCenter = h/2.0;
  	
  	int n = xSource.length;
  	for (int i=0; i<n; i++) {
  		double dx = xSource[i]-xCenter;
  		double dy = ySource[i]-yCenter;
	  	double r = Math.sqrt(dx*dx + dy*dy);
	  	
	  	double stretch = getStretchFactor(r);
	  	double extra = 0.0001;
    	xTrans[i] = xCenter + stretch*dx+extra;
    	yTrans[i] = yCenter + stretch*dy+extra;
  	}

  	isValidTransform = true;
  }
  
  /**
   * Finds the lower radius limit, if any, based on current projection types and source field of view.
   * The radius limit is the fixed radius below which the output field of view would exceed 180 degrees.
   * This uses Newton's method to solve for rLimit as follows:
   * 		--define f(r) = theta_source and theta_out assuming current source FOV and 180 degree output FOV
   * 		--make an initial estimate of rLimit = 1 (always too big)
   * 		--find the values of f and df/dr at the initial estimate
   * 		--use Newton's method to obtain a better estimate of rLimit
   * 		--find new values of f and df/dr
   * 		--repeat until estimate produces sufficiently small value of f
   */
  private double findRadiusLimit() {
  	double tolerance = 0.000001; // tolerance for Newton's method
  	int maxInterations = 10; // maximum interation count in Newton's method
  	
  	// define the limit and make an initial guess
  	double rLimit = 0.8;  	
  	// find f(r) and df/dr at the initial guess
  	double f = getAngleAtRadius(rLimit, sourceProjectionType, sourceFOV)
  			- getAngleAtRadius(rLimit, outputProjectionType, Math.PI);
  	double dfdr = getDerivativeAtRadius(rLimit, sourceProjectionType, sourceFOV)
  			- getDerivativeAtRadius(rLimit, outputProjectionType, Math.PI);
  	
  	// iterate using Newton's method to find the radius where f = 0
  	int i = 0;
	  for(; Math.abs(f)>tolerance && i<maxInterations; i++)  {
	    rLimit = rLimit-f/dfdr;
	  	f = getAngleAtRadius(rLimit, sourceProjectionType, sourceFOV)
	  			- getAngleAtRadius(rLimit, outputProjectionType, Math.PI);
	  	dfdr = getDerivativeAtRadius(rLimit, sourceProjectionType, sourceFOV)
	  			- getDerivativeAtRadius(rLimit, outputProjectionType, Math.PI);
	  }
	  // return Double.NaN if no root found after max iteration count
  	return i<maxInterations? rLimit: Double.NaN;
  }
  
  /**
   * Gets the angle theta at a given radius.
   * 
   * @param r the distance from the image center as a fraction of the corner distance
   * @param projectionType the projection type
   * @param fov the diagonal field of view
   * @return the angle
   */
  private double getAngleAtRadius(double r, String projectionType, double fov) {
  	if (RECTILINEAR.equals(projectionType)) { // r = L*tan(theta)
  		return Math.atan(r*Math.tan(fov/2));
  	}
  	else if (EQUIDISTANT.equals(projectionType)) { // r = L*theta
  		return r*fov/2; 
  	}
  	else if (EQUISOLID.equals(projectionType)) { // r = 2L*sin(theta/2)
  		return 2*Math.asin(r*Math.sin(fov/4));
  	}
  	else if (STEREOGRAPHIC.equals(projectionType)) { // r = 2L*tan(theta/2)
  		return 2*Math.atan(r*Math.tan(fov/4));
  	}
  	else if (ORTHOGRAPHIC.equals(projectionType)) { // r = L*sin(theta)
  		return Math.asin(r*Math.sin(fov/2));
  	}
  	return r*fov/2;   	
  }
  
  /**
   * Gets the derivative dtheta/dr at a given radius. Used to find radius limit using Newton's method.
   * 
   * @param r the distance from the image center as a fraction of the corner distance
   * @param projectionType the projection type
   * @param fov the diagonal field of view
   * @return the angle
   */
  private double getDerivativeAtRadius(double r, String projectionType, double fov) {
  	if (RECTILINEAR.equals(projectionType)) { // r = L*tan(theta)
    	double tan = Math.tan(fov/2);
    	return tan/(1+r*r*tan*tan);
  	}
  	else if (EQUIDISTANT.equals(projectionType)) { // r = L*theta
  		return fov/2; 
  	}
  	else if (EQUISOLID.equals(projectionType)) { // r = 2L*sin(theta/2)
    	double sin = Math.sin(fov/4);
    	return 2*sin/Math.sqrt(1-r*r*sin*sin);
  	}
  	else if (STEREOGRAPHIC.equals(projectionType)) { // r = 2L*tan(theta/2)
    	double tan = Math.tan(fov/4);
    	return 2*tan/(1+r*r*tan*tan);
  	}
  	else if (ORTHOGRAPHIC.equals(projectionType)) { // r = L*sin(theta)
    	double sin = Math.sin(fov/2);
    	return sin/Math.sqrt(1-r*r*sin*sin);
  	}
  	return fov/2;   	
  }
  
  /**
   * Sets the output FOV based on projection types and fixed radius.
   */
  private void refreshScale() {
  	
  	if (sourceFOV==0) return;
 
  	// determine angle at fixed radius in source image
  	double theta = getAngleAtRadius(fixedRadius, sourceProjectionType, sourceFOV);

  	// determine output FOV so angle at fixed radius is same in output image
  	if (RECTILINEAR.equals(outputProjectionType)) { // d = L*tan(theta)
  		outputFOV = 2*Math.atan(Math.tan(theta)/fixedRadius);
  	}
  	else if (EQUIDISTANT.equals(outputProjectionType)) { // d = L*theta
  		outputFOV = 2*theta/fixedRadius;
  	}
  	else if (EQUISOLID.equals(outputProjectionType)) { // d = 2L*sin(theta/2)
  		outputFOV = 4*Math.asin(Math.sin(theta/2)/fixedRadius);
  	}
  	else if (STEREOGRAPHIC.equals(outputProjectionType)) { // d = 2L*tan(theta/2)
  		outputFOV = 4*Math.atan(Math.tan(theta/2)/fixedRadius);
  	}
  	else if (ORTHOGRAPHIC.equals(outputProjectionType)) { // d = L*sin(theta)
  		outputFOV = 2*Math.asin(Math.sin(theta)/fixedRadius);
  	}

  	if (Double.isNaN(outputFOV))
  		outputFOV = maxFOV;
  	
		if (hasLowerLimit) {
		  double limit = findRadiusLimit();
			if (!Double.isNaN(limit) && limit>.005 && limit<1.01) {
	  		lowerRadiusLimit = Math.max((int)Math.floor(100*limit), (int)(100*minRadius));
			}
	  	if (outputFOV>maxFOV && fixedRadius!=lowerRadiusLimit/100.0) {
  			fixedRadius = lowerRadiusLimit/100.0;
	  		refreshScale();
	  	}
		}
		else {
	  	lowerRadiusLimit = (int)(100*minRadius);			
		}
  	  	
  }
  
  /**
   * Gets the stretch factor at a given radius in the output image
   * (source radius = output radius * stretch factor).
   * 
   * @param rOut the distance from the output image center, in pixels
   * @return the stretch factor
   */
  private double getStretchFactor(double rOut) {
  	if (rOut==0 || sourceFOV==0) return 1;
  	
  	// get theta at this output radius based on output projection type
  	double radius = rOut/pixelsToCorner;
  	double theta = getAngleAtRadius(radius, outputProjectionType, outputFOV);

  	// get source radius at the same theta based on source projection type
  	double rSource = rOut;
  	if (RECTILINEAR.equals(sourceProjectionType)) { // d = L*tan(theta)
  		double L = pixelsToCorner/Math.tan(sourceFOV/2);
  		rSource = L*Math.tan(theta);
  	}
  	else if (EQUIDISTANT.equals(sourceProjectionType)) { // d = L*theta
  		double L = 2*pixelsToCorner/sourceFOV;
  		rSource = L*theta;
  	}
  	else if (EQUISOLID.equals(sourceProjectionType)) { // d = 2L*sin(theta/2)
  		double twoL = pixelsToCorner/Math.sin(sourceFOV/4);
  		rSource = twoL*Math.sin(theta/2);
  	}
  	else if (STEREOGRAPHIC.equals(sourceProjectionType)) { // d = 2L*tan(theta/2)
  		double twoL = pixelsToCorner/Math.tan(sourceFOV/4);
  		rSource = twoL*Math.tan(theta/2);
  	}
  	else if (ORTHOGRAPHIC.equals(sourceProjectionType)) { // d = L*sin(theta)
  		double L = pixelsToCorner/Math.sin(sourceFOV/2);
  		rSource = L*Math.sin(theta);
  	}

  	return rSource/rOut;
  }
  
  /**
   * Get the interpolated color at a non-integer position (between pixels points).
   * 
   * @param x the x-coordinate of the position
   * @param y the y-coordinate of the position
   * @param w the width of the image
   * @param h the height of the image
   * @param pixelValues the color values of the pixels in the image
   */
  private int getColor(double x, double y, int w, int h, int[] pixelValues) {
  	// get base pixel position
  	int col = (int)Math.floor(x);
  	int row = (int)Math.floor(y);
  	if (col<0 || col>=w || row<0 || row>=h) {
  		return 0;  // black if not in image
  	}
  	if (col+1==w || row+1==h) {
  		return pixelValues[row*w+col];
  	}
  	  	
		double u = col==0? x: x%col;
		double v = row==0? y: y%row;

  	if (interpolation==2) {
    	// get 2x2 neighborhood pixel values
  		int[] values = new int[] {pixelValues[row*w+col], pixelValues[row*w+col+1], 
  				pixelValues[(row+1)*w+col], pixelValues[(row+1)*w+col+1]};
  		
      int[] rgb = new int[4];
      for (int j=0; j<4; j++) {
      	rgb[j] = (values[j]>>16)&0xff; // red
      }
      int r = bilinearInterpolation(u, v, rgb);
      for (int j=0; j<4; j++) {
      	rgb[j] = (values[j]>>8)&0xff;  // green
      }
      int g = bilinearInterpolation(u, v, rgb);
      for (int j=0; j<4; j++) {
      	rgb[j] = (values[j])&0xff;     // blue
      }
      int b = bilinearInterpolation(u, v, rgb);
      return (r<<16)|(g<<8)|b;
  	}
  	
		// if not interpolating, return value of nearest neighbor  		
		return u<0.5? v<0.5? pixelValues[row*w+col]: pixelValues[(row+1)*w+col]:
			v<0.5? pixelValues[row*w+col+1]: pixelValues[(row+1)*w+col+1];
  }
  
  /**
   * Returns a bilinear interpolated pixel color (int value) at a given point relative to (0,0).
   * 
   * @param x the x-position relative to 0,0 (0<=x<1)
   * @param x the y-position relative to 0,0 (0<=y<1)
   * @param values array of pixels color values [value(0,0), value(0,1), value(1,0), value(1,1)]
   * @return the interpolated color value 
   */
  private int bilinearInterpolation(double x, double y, int[] values) {
  		return (int)((1-y)*((1-x)*values[0] + x*values[2]) + y*((1-x)*values[1] + x*values[3]));  	
  }
  
  /**
   * Inner Inspector class to control filter parameters
   */
  private class Inspector extends JDialog {
  	
    JButton helpButton, colorButton;
  	JPanel contentPane;
  	JSlider radiusSlider, sourceAngleSlider;
  	JComboBox sourceTypeDropdown, outputTypeDropdown;
  	JLabel radiusLabel, sourceAngleLabel, outputAngleLabel, sourceTypeLabel, outputTypeLabel;
  	IntegerField radiusField, sourceAngleField, outputAngleField;
  	TitledBorder sourceBorder, outputBorder, circleBorder;
  	  	
    /**
     * Constructs the Inspector.
     */
    public Inspector() {
      super(frame, !(frame instanceof org.opensourcephysics.display.OSPFrame));
      setResizable(false);
      createGUI();
      refreshGUI();
      pack();
      updateDisplay();
      // center on screen
      Rectangle rect = getBounds();
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width-rect.width)/2;
      int y = (dim.height-rect.height)/2;
      setLocation(x, y);
    }
    
    /**
     * Creates the visible components.
     */
    void createGUI() {
    	helpButton = new JButton();
    	helpButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	String s = MediaRes.getString("RadialDistortionFilter.Help.Message1") //$NON-NLS-1$
		    			+"\n"+MediaRes.getString("RadialDistortionFilter.Help.Message2") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n"+MediaRes.getString("RadialDistortionFilter.Help.Message3") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n"+MediaRes.getString("RadialDistortionFilter.Help.Message4") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n"+MediaRes.getString("RadialDistortionFilter.Help.Message5") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n\n"+MediaRes.getString("RadialDistortionFilter.Help.Message6") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n"+MediaRes.getString("RadialDistortionFilter.Help.Message7"); //$NON-NLS-1$ //$NON-NLS-2$
        	
        	for (int i=0; i<PROJECTION_TYPES.size(); i++) {
        		String type = PROJECTION_TYPES.get(i);
        		s += "\n    "+(i+1)+". "+MediaRes.getString("RadialDistortionFilter.Help.Message."+type); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	}

    			s += "\n\n"+MediaRes.getString("RadialDistortionFilter.Help.Message8") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n    "+MediaRes.getString("RadialDistortionFilter.Help.Message9") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n    "+MediaRes.getString("RadialDistortionFilter.Help.Message10") //$NON-NLS-1$ //$NON-NLS-2$
		    			+"\n    "+MediaRes.getString("RadialDistortionFilter.Help.Message11"); //$NON-NLS-1$ //$NON-NLS-2$
        	
    			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(vidPanel), s,
		    			MediaRes.getString("RadialDistortionFilter.Help.Title"),  //$NON-NLS-1$
		    			JOptionPane.INFORMATION_MESSAGE);
		    }
      });
    	colorButton = new JButton();
    	colorButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // show color chooser dialog with color of this track
          Color newColor = JColorChooser.showDialog(null, 
          		MediaRes.getString("PerspectiveFilter.Dialog.Color.Title"),  //$NON-NLS-1$
          		color);
          if (newColor != null) {
          	color = newColor;
            support.firePropertyChange("color", null, newColor); //$NON-NLS-1$
          }
        }
      });
    	
      Border space = BorderFactory.createEmptyBorder(2, 2, 2, 2);

      String[] types = getProjectionTypeDescriptions();

      sourceTypeDropdown = new JComboBox(types);
      sourceTypeDropdown.setSelectedItem(MediaRes.getString("RadialDistortionFilter.ProjectionType."+sourceProjectionType)); //$NON-NLS-1$
      sourceTypeDropdown.setBorder(space);
      sourceTypeDropdown.addItemListener(new ItemListener() {
      	public void itemStateChanged(ItemEvent e) {
      		String desired = sourceTypeDropdown.getSelectedItem().toString();
          String[] types = getProjectionTypeDescriptions();
          for (int i = 0; i<types.length; i++) {
          	if (types[i].equals(desired)) {
		      		setSourceProjectionType(PROJECTION_TYPES.get(i));         		
          	}
          }
      	}
      });
      
      outputTypeDropdown = new JComboBox(types);
      outputTypeDropdown.setSelectedItem(MediaRes.getString("RadialDistortionFilter.ProjectionType."+outputProjectionType)); //$NON-NLS-1$
      outputTypeDropdown.setBorder(space);
      outputTypeDropdown.addItemListener(new ItemListener() {
      	public void itemStateChanged(ItemEvent e) {
      		String desired = outputTypeDropdown.getSelectedItem().toString();
          String[] types = getProjectionTypeDescriptions();
          for (int i = 0; i<types.length; i++) {
          	if (types[i].equals(desired)) {
		      		setOutputProjectionType(PROJECTION_TYPES.get(i));         		
          	}
          }
      	}
      });
      
      space = BorderFactory.createEmptyBorder(2, 4, 2, 4);

      sourceAngleSlider = new JSlider(5, 180, 90);
      sourceAngleSlider.setBorder(space);
      sourceAngleSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int n = sourceAngleSlider.getValue();
          setSourceFOV(n*Math.PI/180);
        }       
      });
      
      int rMax = (int)(100*maxRadius);
      int rMin = (int)(100*minRadius);
      int rFixed = (int)(100*fixedRadius);

    	radiusSlider = new JSlider(rMin, rMax, rFixed);
      radiusSlider.setBorder(space);
      radiusSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int i = radiusSlider.getValue();
          i = Math.max(i, lowerRadiusLimit);
          setFixedRadius(i/100.0);         	
        }       
      });
      radiusSlider.addMouseListener(new MouseAdapter() {
        public void mouseReleased(MouseEvent e) {
        	updateDisplay();
        }
      });
      
      space = BorderFactory.createEmptyBorder(2, 4, 2, 2);
      sourceTypeLabel = new JLabel();
      sourceTypeLabel.setBorder(space);
      outputTypeLabel = new JLabel();
      outputTypeLabel.setBorder(space);      
      sourceAngleLabel = new JLabel();
      sourceAngleLabel.setBorder(space);     
      outputAngleLabel = new JLabel();
      outputAngleLabel.setBorder(space);     
      radiusLabel = new JLabel();
      radiusLabel.setBorder(space);
      
      sourceAngleField = new IntegerField(3);
      sourceAngleField.setMaxValue(180);
      sourceAngleField.setMinValue(5);
      sourceAngleField.setUnits("\u00B0"); //$NON-NLS-1$
      sourceAngleField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int n = sourceAngleField.getIntValue();
          setSourceFOV(n*Math.PI/180);
          updateDisplay();
          sourceAngleField.selectAll();
        }

      });
      sourceAngleField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
        	sourceAngleField.selectAll();
        }
        public void focusLost(FocusEvent e) {
          int n = sourceAngleField.getIntValue();
          setSourceFOV(n*Math.PI/180);
          updateDisplay();
        }

      });

      outputAngleField = new IntegerField(3);
      outputAngleField.setMaxValue(180);
      outputAngleField.setMinValue(0);
      outputAngleField.setEditable(false);
      outputAngleField.setUnits("\u00B0"); //$NON-NLS-1$

      radiusField = new IntegerField(3);
      radiusField.setMaxValue(rMax);
      radiusField.setMinValue(rMin);
      radiusField.setUnits("%"); //$NON-NLS-1$
      radiusField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	int n = radiusField.getIntValue();
          setFixedRadius(n/100.0);
          updateDisplay();
          radiusField.selectAll();
        }

      });
      radiusField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
        	radiusField.selectAll();
        }
        public void focusLost(FocusEvent e) {
        	int n = radiusField.getIntValue();
          setFixedRadius(n/100.0);
          updateDisplay();
        }

      });

      
      // add components to content pane
      contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);
      
      JPanel buttonbar = new JPanel(new FlowLayout());
      contentPane.add(buttonbar, BorderLayout.SOUTH);
      buttonbar.add(helpButton);
      buttonbar.add(colorButton);
      buttonbar.add(ableButton);
      buttonbar.add(closeButton);
      
      space = BorderFactory.createEmptyBorder(2, 2, 2, 4);

      Box controlPanel = Box.createVerticalBox();
      contentPane.add(controlPanel, BorderLayout.CENTER);
      
      Box sourceStack = Box.createVerticalBox();
      sourceBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
      sourceStack.setBorder(sourceBorder);
      controlPanel.add(sourceStack);
      
      Box box = Box.createHorizontalBox();
      box.setBorder(space);
      box.add(sourceTypeLabel);
      box.add(sourceTypeDropdown);
      box.add(sourceAngleLabel);
      box.add(sourceAngleField);
      sourceStack.add(box);
      box = Box.createHorizontalBox();
      box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      box.add(sourceAngleSlider);
      sourceStack.add(box);

      Box outputStack = Box.createVerticalBox();
      outputBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
      outputStack.setBorder(outputBorder);
      controlPanel.add(outputStack);
      
      box = Box.createHorizontalBox();
      box.setBorder(space);
      box.add(outputTypeLabel);
      box.add(outputTypeDropdown);
      box.add(outputAngleLabel);
      box.add(outputAngleField);
      outputStack.add(box);
      
      Box circleBox = Box.createVerticalBox();
      circleBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
      circleBox.setBorder(circleBorder);
      controlPanel.add(circleBox);
      
      box = Box.createHorizontalBox();
      box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      box.add(radiusLabel);
      box.add(radiusField);
      box.add(radiusSlider);
      circleBox.add(box);
    }

    /**
     * Refreshes this inspector's GUI
     */
    void refreshGUI() {
      setTitle(MediaRes.getString("RadialDistortionFilter.Inspector.Title")); //$NON-NLS-1$
      sourceTypeLabel.setText(MediaRes.getString("RadialDistortionFilter.Label.ProjectionType")+":"); //$NON-NLS-1$ //$NON-NLS-2$
      outputTypeLabel.setText(MediaRes.getString("RadialDistortionFilter.Label.ProjectionType")+":"); //$NON-NLS-1$ //$NON-NLS-2$
      sourceAngleLabel.setText(MediaRes.getString("RadialDistortionFilter.Label.Angle")+":"); //$NON-NLS-1$ //$NON-NLS-2$
      outputAngleLabel.setText(MediaRes.getString("RadialDistortionFilter.Label.Angle")+":"); //$NON-NLS-1$ //$NON-NLS-2$
      radiusLabel.setText(MediaRes.getString("RadialDistortionFilter.Label.Diameter")+":"); //$NON-NLS-1$ //$NON-NLS-2$
      helpButton.setText(MediaRes.getString("PerspectiveFilter.Button.Help")); //$NON-NLS-1$
      colorButton.setText(MediaRes.getString("PerspectiveFilter.Button.Color")); //$NON-NLS-1$
      sourceBorder.setTitle(MediaRes.getString("RadialDistortionFilter.BorderTitle.Source")); //$NON-NLS-1$
      outputBorder.setTitle(MediaRes.getString("RadialDistortionFilter.BorderTitle.Output")); //$NON-NLS-1$
      circleBorder.setTitle(MediaRes.getString("RadialDistortionFilter.BorderTitle.Circle")); //$NON-NLS-1$
      boolean enabled = RadialDistortionFilter.this.isEnabled();
      sourceTypeLabel.setEnabled(enabled);
      outputTypeLabel.setEnabled(enabled);
      radiusLabel.setEnabled(enabled);
      sourceAngleLabel.setEnabled(enabled);      
      outputAngleLabel.setEnabled(enabled);      
      radiusField.setEnabled(enabled);
      sourceAngleField.setEnabled(enabled);      
      outputAngleField.setEnabled(enabled);      
      radiusSlider.setEnabled(enabled);
      sourceAngleSlider.setEnabled(enabled);      
      sourceTypeDropdown.setEnabled(enabled);
      outputTypeDropdown.setEnabled(enabled);
    	colorButton.setEnabled(enabled);
      Color color = enabled? GUIUtils.getEnabledTextColor(): GUIUtils.getDisabledTextColor();
      sourceBorder.setTitleColor(color);
      outputBorder.setTitleColor(color);
      circleBorder.setTitleColor(color);
      repaint();
    }
    
    /**
     * Initializes this inspector
     */
    void initialize() {
      updateDisplay();
    }

   /**
     * Updates the inspector controls to reflect the current filter settings.
     */
    void updateDisplay() {
    	if (updatingDisplay) return;
    	updatingDisplay = true;
    	refreshScale();
    	int n = (int)Math.round(180*sourceFOV/Math.PI);
      sourceAngleField.setIntValue(n);
      sourceAngleSlider.setValue(n);
    	n = (int)Math.round(180*outputFOV/Math.PI);
      outputAngleField.setIntValue(n);
      n = (int)Math.round(100*fixedRadius);
      radiusSlider.setValue(n);
      radiusField.setIntValue(n);
    	updatingDisplay = false;
    }

    @Override
    public void setVisible(boolean vis) {
    	super.setVisible(vis);
    	refreshGUI();
    	if (vidPanel!=null) {
	    	if (vis) {
	    		vidPanel.addDrawable(circle);
	      	support.firePropertyChange("visible", null, null); //$NON-NLS-1$
	      	RadialDistortionFilter.this.addPropertyChangeListener("visible", vidPanel); //$NON-NLS-1$
	    	}
	    	else {
	    		vidPanel.removeDrawable(circle);
	      	support.firePropertyChange("visible", null, null); //$NON-NLS-1$
	      	RadialDistortionFilter.this.removePropertyChangeListener("visible", vidPanel); //$NON-NLS-1$
	    	}
    	}
    	support.firePropertyChange("image", null, null); //$NON-NLS-1$
    }
  
  }
  
  /**
   * Inner Circle class draws a circle at the fixed radius.
   */
  private class Circle implements Trackable {
  	
  	TPoint center = new TPoint(), corner = new TPoint();
  	Ellipse2D ellipse = new Ellipse2D.Double(); 
  	Stroke stroke = new BasicStroke(2);
  	
    public void draw(DrawingPanel panel, Graphics g) {
    	if (!RadialDistortionFilter.super.isEnabled()) return;
    	VideoPanel vidPanel = (VideoPanel)panel;
    	BufferedImage img = vidPanel.getVideo().getImage();
    	double x = img.getWidth()/2, y = img.getHeight()/2;
    	double r = fixedRadius*pixelsToCorner;
      center.setLocation(x, y);
      corner.setLocation(x-r, y-r);
    	Point centerScreen = center.getScreenPosition(vidPanel);
    	Point cornerScreen = corner.getScreenPosition(vidPanel);
  		ellipse.setFrameFromCenter(centerScreen, cornerScreen);
  		Shape shape = stroke.createStrokedShape(ellipse);
			Graphics2D g2 = (Graphics2D)g;
      Color gcolor = g2.getColor();
      g2.setColor(color);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
      g2.fill(shape);

      g2.setColor(gcolor);
    	
    }
  }

  
  /**
   * Returns an XML.ObjectLoader to save and load filter data.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load filter data.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the filter to save
     */
    public void saveObject(XMLControl control, Object obj) {
    	RadialDistortionFilter filter = (RadialDistortionFilter) obj;
    	
    	control.setValue("fixed_radius", filter.fixedRadius); //$NON-NLS-1$
    	control.setValue("input_type", filter.sourceProjectionType); //$NON-NLS-1$
    	control.setValue("input_fov", filter.sourceFOV); //$NON-NLS-1$
    	control.setValue("output_type", filter.outputProjectionType); //$NON-NLS-1$
    	control.setValue("color", filter.color); //$NON-NLS-1$
    	
      if((filter.frame!=null) && (filter.inspector!=null) && filter.inspector.isVisible()) {
        int x = filter.inspector.getLocation().x-filter.frame.getLocation().x;
        int y = filter.inspector.getLocation().y-filter.frame.getLocation().y;
        control.setValue("inspector_x", x); //$NON-NLS-1$
        control.setValue("inspector_y", y); //$NON-NLS-1$
      }
    }

    /**
     * Creates a new filter.
     *
     * @param control the control
     * @return the new filter
     */
    public Object createObject(XMLControl control) {
      return new RadialDistortionFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      final RadialDistortionFilter filter = (RadialDistortionFilter) obj;
      
      filter.setFixedRadius(control.getDouble("fixed_radius")); //$NON-NLS-1$
    	filter.setSourceFOV(control.getDouble("input_fov")); //$NON-NLS-1$
    	filter.setSourceProjectionType(control.getString("input_type")); //$NON-NLS-1$
    	filter.setOutputProjectionType(control.getString("output_type")); //$NON-NLS-1$
    	Color color = (Color)control.getObject("color"); //$NON-NLS-1$
    	if (color!=null) filter.color = color;
    	
      filter.inspectorX = control.getInt("inspector_x"); //$NON-NLS-1$
      filter.inspectorY = control.getInt("inspector_y"); //$NON-NLS-1$
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
