/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.axes.XAxis;
import org.opensourcephysics.display.axes.XYAxis;

/**
 * ComplexDataset stores and plots a complex dataset (x,z) where the dependent variable
 * has real and imaginary parts,  z=(real, imaginary).
 *
 * In Re_Im mode, both the real and imaginary parts are shown as separate curves.
 * In Phase mode, the vertical coordinate represents magnitude and color represents phase.
 * ComplexDataset is Drawable and can be rendered on a DrawingPanel.
 * ComplexDataset extends AbstractTableModel and can be rendered in a JTable.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class ComplexDataset extends AbstractTableModel implements Drawable, Measurable, Data {
  static final double PI2 = Math.PI*2;

  /** AMP height equal to |z|..          */
  public static final int AMP_CURVE = 0;   // marker type

  /** RE_IM real and imaginary curves.          */
  public static final int RE_IM_CURVE = 1; // marker type

  /** PHASE_CURVE the phase is shown as color.          */
  public static final int PHASE_CURVE = 2; // marker type

  /** PHASE_BAR the phase is shown as the bar's color */
  public static final int PHASE_BAR = 3;   // marker type

  /** Field POST           */
  public final static int PHASE_POST = 4;  // marker type
  
  /** visible in drawing panel */
  protected boolean visible = true;
  
  /** affect autoscaled drawing panels  */
  protected boolean measurable = true;

  protected double[] xpoints;              // array of x points
  protected double[] re_points;            // array of y points
  protected double[] im_points;            // array of y points
  protected double[] amp_points;           // array of amplitude points
  protected int index;                     // the current index of the array
  private int markerShape = PHASE_CURVE;
  private int markerSize = 5;              // the size in pixels of the marker
  private boolean centered = true;         // center the y values on the x axis.
  // private boolean showAmp        = true;         // Separate Real and Imaginary curves
  // private boolean showReIm       = false;         // Separate Real and Imaginary curves
  private boolean showPhase = true;        // Show phase as color
  private double xmin;                     // the minimum x value in the dataset
  private double xmax;                     // the maximum x value in the dataset
  private double ampmin;                   // the minimum x value in the dataset
  private double ampmax;                   // the maximum x value in the dataset
  private double remax;                    // the maximum real value in the dataset
  private double remin;                    // the minimum real value in the dataset
  private double immax;                    // the maximum real value in the dataset
  private double immin;                    // the minimum real value in the dataset
  private boolean sorted = false;          // sort the data by increasing x
  private boolean connected = true;
  private int initialSize;                 // the initial size of the array
  // private Color       reColor=Color.red;             // the color of the real data line
  // private Color       imColor=Color.blue;             // the color of the real data line

  private Color lineColor = Color.black;
  private GeneralPath ampPath;             // used to draw line plots
  private Trail reTrail = new Trail(), imTrail = new Trail();
  private String name = "Complex Data";    //$NON-NLS-1$
  private String xColumnName = "x";        // the name of the x data //$NON-NLS-1$
  private String reColumnName = "re";      // the name of the y data //$NON-NLS-1$
  private String imColumnName = "im";      // the name of the y data //$NON-NLS-1$
  private int stride = 1;                  // the data stride in table view
  private AffineTransform flip;
  Dataset reDataset;
  Dataset imDataset;
  int datasetID = hashCode();

  /**
   * Dataset constructor.
   */
  public ComplexDataset() {
    reTrail.color = Color.RED;
    imTrail.color = Color.BLUE;
    initialSize = 10;
    xColumnName = "x";   //$NON-NLS-1$
    reColumnName = "re"; //$NON-NLS-1$
    imColumnName = "im"; //$NON-NLS-1$
    ampPath = new GeneralPath();
    index = 0;
    flip = new AffineTransform(1, 0, 0, -1, 0, 0);
    clear();
  }

  /**
   * Shows the phase legend.
   */
  public JFrame showLegend() {
    InteractivePanel panel = new InteractivePanel();
    panel.setPreferredGutters(5, 5, 5, 25);
    DrawingFrame frame = new DrawingFrame(DisplayRes.getString("GUIUtils.PhaseLegend"), panel); //$NON-NLS-1$
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setJMenuBar(null);
    panel.addDrawable(new Phase());
    XAxis xaxis = new XAxis(DisplayRes.getString("ComplexDataset.Legend.XAxis")); //$NON-NLS-1$
    xaxis.setLocationType(XYAxis.DRAW_AT_LOCATION);
    xaxis.setEnabled(true); // enable the dragging
    panel.setClipAtGutter(false);
    panel.addDrawable(xaxis);
    panel.setSquareAspect(false);
    panel.setPreferredMinMax(-Math.PI, Math.PI, -1, 1);
    frame.setSize(300, 120);
    frame.setVisible(true);
    return frame;
  }

  class Phase implements Drawable {
    public void draw(DrawingPanel panel, Graphics g) {
      int w = panel.getWidth()-5+1;
      int h = panel.getHeight()-25;
      for(int i = 5; i<w; i++) {
        double theta = Math.PI*(-1+2*((float) i)/w);
        Color c = DisplayColors.phaseToColor(theta);
        g.setColor(c);
        g.drawLine(i, 5, i, h);
      }
    }

  }

  /**
   * Gets the valid measure flag.  The measure is valid if the min and max values have been set.
   *
   * @return <code>true<\code> if measure is valid
   */
  public boolean isMeasured() {
    if(index<1) {
      return false;
    }
    return measurable;
  }

  /**
   * Gets the x world coordinate for the left hand side of the panel.
   * @return xmin
   */
  public double getXMin() {
    return xmin;
  }

  /**
   * Gets the x world coordinate for the right hand side of the panel.
   * @return xmax
   */
  public double getXMax() {
    return xmax;
  }

  /**
   * Gets y world coordinate for the bottom of the panel.
   * @return ymin
   */
  public double getYMin() {
    if(markerShape==RE_IM_CURVE) {
      return -ampmax;
    } else if(centered&&((markerShape==PHASE_BAR)||(markerShape==PHASE_CURVE))) {
      return -ampmax/2;
    } else {
      return 0;
    }
  }

  /**
   * Gets y world coordinate for the top of the panel.
   * @return ymax
   */
  public double getYMax() {
    if(markerShape==RE_IM_CURVE) {
      return ampmax;
    } else if(centered&&((markerShape==PHASE_BAR)||(markerShape==PHASE_CURVE))) {
      return ampmax/2;
    }
    if(markerShape==PHASE_POST) {
      return 1.1*ampmax;
    }
    return ampmax;
  }

  /**
   * Gets a copy of the xpoints array.
   * @return xpoints[]
   */
  public double[] getXPoints() {
    double[] temp = new double[index];
    System.arraycopy(xpoints, 0, temp, 0, index);
    return temp;
  }

  /**
   * Gets a copy of the real points array.
   * @return repoints[]
   */
  public double[] getRePoints() {
    double[] temp = new double[index];
    System.arraycopy(re_points, 0, temp, 0, index);
    return temp;
  }

  /**
   * Gets a copy of the imaginary points array.
   * @return impoints[]
   */
  public double[] getImPoints() {
    double[] temp = new double[index];
    System.arraycopy(im_points, 0, temp, 0, index);
    return temp;
  }

  /**
   * Gets a copy of the ypoints array.
   * @return ypoints[]
   */
  public double[] getYPoints() {
    double[] temp = new double[index];
    System.arraycopy(amp_points, 0, temp, 0, index);
    return temp;
  }

  /**
* Gets a data array containing both x and y values.
*
* @return a double[index][2] array of data
*/
  public double[][] getPoints() {
    double[][] temp = new double[index][3];
    for(int i = 0; i<index; i++) {
      temp[i] = new double[] {xpoints[i], re_points[i], im_points[i]};
    }
    return temp;
  }

  /**
   * Sets the data point marker.
   * Shapes are:
   *   AMP_CURVE
   *   RE_IM_CURVE
   *   PHASE_CURVE
   *   PHASE_BAR
   *   PHASE_POST
   *
   * @param _markerShape
   */
  public void setMarkerShape(int _markerShape) {
    markerShape = _markerShape;
  }

  /**
   * Gets the marker shape.
   *
   * @return int
   */
  public int getMarkerShape() {
    return markerShape;
  }

  /**
   * Gets the marker size.
   *
   * @return int
   */
  public int getMarkerSize() {
    return markerSize;
  }

  /**
   * Sets the marker size.
   * @param size int
   */
  public void setMarkerSize(int size) {
    markerSize = size;
  }

  /**
   * Sets the sorted flag. Data is sorted by increasing x.
   *
   * @param _sorted <code>true<\code> to sort
   */
  public void setSorted(boolean _sorted) {
    sorted = _sorted;
    if(sorted) {
      insertionSort();
    }
  }

  /**
   * Sets the data stride for table view.
   *
   * A stride of i will show every i-th point.
   *
   * @param _stride
   */
  public void setStride(int _stride) {
    stride = _stride;
    stride = Math.max(1, stride);
  }

  /**
   * Gets the sorted flag.
   *
   * @return <code>true<\code> if the data is sorted
   */
  public boolean isSorted() {
    return sorted;
  }
  
  /**
   * Sets the visibility of this Dataset in a DrawingPanel.
   *
   * @param b <code>true<\code> if dataset is visible
   */
  public void setVisible(boolean b) {
	  visible = b;
  }
  
  /**
   * Gets the visibility of this dataset in the DrawingPanel.
   * @return boolean
   */
  public boolean getVisible() {
    return visible;
  }
  
  /**
   * Sets the measurable property.  Measurable objects affect panel autoscaling.
   *
   * @param b <code>true<\code> if points are connected
   */
  public void setMeasurable(boolean b) {
	  measurable = b;
  }
  
  /**
   * Gets the measurable property. 
   * @return boolean
   */
  public boolean getMeasurable() {
    return measurable;
  }

  /**
   * Sets the data connected flag.  Points are connected by straight lines.
   * @param _connected <code>true<\code> if points are connected
   */
  public void setConnected(boolean _connected) {
    connected = _connected;
  }

  /**
   * Sets the centered flag.   Centered complex numbers are shown extending above
   * and below the y axis.
   *
   * @param _centered <code>true<\code> if data is centered
   */
  public void setCentered(boolean _centered) {
    centered = _centered;
  }

  /**
   * Gets the data connected flag.
   * @return <code>true<\code> if points are connected
   */
  public boolean isConnected() {
    return connected;
  }

  /**
   * Sets the color of the lines connecting data points.
   * @param _lineColor
   */
  public void setLineColor(Color _lineColor) {
    lineColor = _lineColor;
    reTrail.color = lineColor;
    imTrail.color = lineColor;
  }

  /**
   * Sets the color of the lines connecting data points.
   * @param reColor the real component color
   * @param imColor the imaginary component color
   */
  public void setLineColor(Color reColor, Color imColor) {
    lineColor = reColor;
    reTrail.color = reColor;
    imTrail.color = imColor;
  }

  /**
   * Line colors for Data interface.
   * @return
   */
  public java.awt.Color[] getLineColors() {
    return new Color[] {lineColor, lineColor};
  }

  /**
   * Gets the line color.
   *
   * @return the line color
   */
  public Color getLineColor() {
    return lineColor;
  }

  /**
   * Fill colors to Data interface.
   * @return
   */
  public java.awt.Color[] getFillColors() {
    return new Color[] {lineColor, lineColor};
  }

  /**
   * Fill color to use for this data
   * @return
   */
  public java.awt.Color getFillColor() {
    return lineColor;
  }

  /**
   * Sets the column names when rendering this dataset in a JTable.
   *
   * @param _xColumnName String
   * @param _reColumnName String
   * @param _imColumnName String
   */
  public void setXYColumnNames(String _xColumnName, String _reColumnName, String _imColumnName) {
    xColumnName = TeXParser.parseTeX(_xColumnName);
    reColumnName = TeXParser.parseTeX(_reColumnName);
    imColumnName = TeXParser.parseTeX(_imColumnName);
  }

  /**
   * Sets the column names when rendering this dataset in a JTable.
   *
   * @param _xColumnName String
   * @param _reColumnName String
   * @param _imColumnName String
   * @param datasetName String
   */
  public void setXYColumnNames(String _xColumnName, String _reColumnName, String _imColumnName, String datasetName) {
    setXYColumnNames(_xColumnName, _reColumnName, _imColumnName);
    name = TeXParser.parseTeX(datasetName);
  }

  /**
   * Appends (x, re, im) datum to the Dataset.
   *
   * @param x double
   * @param re double
   * @param im double
   */
  public void append(double x, double re, double im) {
    if(Double.isNaN(x)||Double.isInfinite(x)||Double.isNaN(re)||Double.isInfinite(re)||Double.isNaN(im)||Double.isInfinite(im)) {
      return;
    }
    if(index>=xpoints.length) {
      setCapacity(xpoints.length*2);
    }
    xpoints[index] = x;
    re_points[index] = re;
    im_points[index] = im;
    double amp = Math.sqrt(re*re+im*im);
    // generalPath.append(new Rectangle2D.Double(x, y, 0, 0), true);
    if(index==0) {
      ampPath.moveTo((float) x, (float) amp);
    } else {
      ampPath.lineTo((float) x, (float) amp);
    }
    reTrail.addPoint(x, re);
    imTrail.addPoint(x, im);
    xmax = Math.max(x, xmax);
    xmin = Math.min(x, xmin);
    remin = Math.min(re, remin);
    remax = Math.max(re, remax);
    immin = Math.min(im, immin);
    immax = Math.max(im, immax);
    ampmin = Math.min(amp, ampmin);
    ampmax = Math.max(amp, ampmax);
    index++;
    // move the new datum if x is less than the last value.
    if(sorted&&(index>1)&&(x<xpoints[index-2])) {
      moveDatum(index-1); // the new datum is out of place to move it
      recalculatePath();
      // System.out.println ("data moved");
    }
  }

  /**
   * Appends x, real, and imaginary arrays to the Dataset.
   * @param _xpoints
   * @param _repoints
   * @param _impoints
   */
  public void append(double[] _xpoints, double[] _repoints, double[] _impoints) {
    if(_xpoints==null) {
      return;
    }
    if((_repoints==null)||(_impoints==null)||(_xpoints.length!=_repoints.length)||(_xpoints.length!=_impoints.length)) {
      throw new IllegalArgumentException("Array lenghts must be equal to append data."); //$NON-NLS-1$
    }
    boolean badData = false;
    for(int i = 0; i<_xpoints.length; i++) {
      if(Double.isNaN(_xpoints[i])||Double.isInfinite(_xpoints[i])||Double.isNaN(_repoints[i])||Double.isInfinite(_repoints[i])||Double.isNaN(_impoints[i])||Double.isInfinite(_impoints[i])) {
        badData = true;
        continue;
      }
      xmax = Math.max(_xpoints[i], xmax);
      xmin = Math.min(_xpoints[i], xmin);
      remin = Math.min(_repoints[i], remin);
      remax = Math.max(_repoints[i], remax);
      immin = Math.min(_impoints[i], immin);
      immax = Math.max(_impoints[i], immax);
      double amp = Math.sqrt(_repoints[i]*_repoints[i]+_impoints[i]*_impoints[i]);
      ampmin = Math.min(amp, ampmin);
      ampmax = Math.max(amp, ampmax);
      if((index==0)&&(i==0)) {
        ampPath.moveTo((float) _xpoints[i], (float) amp);
      } else {
        ampPath.lineTo((float) _xpoints[i], (float) amp);
      }
      reTrail.addPoint(_xpoints[i], _repoints[i]);
      imTrail.addPoint(_xpoints[i], _impoints[i]);
    }
    int pointsAdded = _xpoints.length;
    int availableSpots = xpoints.length-index;
    if(pointsAdded>availableSpots) {
      setCapacity(2*(xpoints.length+pointsAdded)); // FIX ME
    }
    System.arraycopy(_xpoints, 0, xpoints, index, pointsAdded);
    System.arraycopy(_repoints, 0, re_points, index, pointsAdded);
    System.arraycopy(_impoints, 0, im_points, index, pointsAdded);
    index += pointsAdded;
    if(badData) {
      cleanBadData();
    }
    if(sorted) {
      insertionSort();
    }
  }

  /**
   * Appends x and z data to the Dataset.
   *
   * Z array has length twice that of x array.
   *<PRE>
   *    Re(z) = z[2*i]
   *    Im(z) = z[2*i + 1]
   *</PRE>
   *
   * @param _xpoints
   * @param _zpoints
   */
  public void append(double[] _xpoints, double[] _zpoints) {
    if(_xpoints==null) {
      return;
    }
    if((_zpoints==null)||(2*_xpoints.length!=_zpoints.length)) {
      throw new IllegalArgumentException("Length of z array must be twice the length of the x array."); //$NON-NLS-1$
    }
    boolean badData = false;
    int pointsAdded = _xpoints.length;
    int availableSpots = xpoints.length-index;
    if(pointsAdded>availableSpots) {
      setCapacity(2*(xpoints.length+pointsAdded));
    }
    for(int i = 0; i<_xpoints.length; i++) {
      if(Double.isNaN(_xpoints[i])||Double.isInfinite(_xpoints[i])||Double.isNaN(_zpoints[2*i])||Double.isInfinite(_zpoints[2*i])||Double.isNaN(_zpoints[2*i+1])||Double.isInfinite(_zpoints[2*i+1])) {
        badData = true;
        continue;
      }
      xmax = Math.max(_xpoints[i], xmax);
      xmin = Math.min(_xpoints[i], xmin);
      remin = Math.min(_zpoints[2*i], remin);
      remax = Math.max(_zpoints[2*i], remax);
      immin = Math.min(_zpoints[2*i+1], immin);
      immax = Math.max(_zpoints[2*i+1], immax);
      double amp = Math.sqrt(_zpoints[2*i]*_zpoints[2*i]+_zpoints[2*i+1]*_zpoints[2*i+1]);
      ampmin = Math.min(amp, ampmin);
      ampmax = Math.max(amp, ampmax);
      xpoints[index+i] = _xpoints[i];
      re_points[index+i] = _zpoints[2*i];
      im_points[index+i] = _zpoints[2*i+1];
      if((index==0)&&(i==0)) {
        ampPath.moveTo((float) _xpoints[i], (float) amp);
      } else {
        ampPath.lineTo((float) _xpoints[i], (float) amp);
      }
      reTrail.addPoint(_xpoints[i], _zpoints[2*i]);
      imTrail.addPoint(_xpoints[i], _zpoints[2*i+1]);
    }
    index += pointsAdded;
    if(badData) {
      cleanBadData();
    }
    if(sorted) {
      insertionSort();
    }
  }

  /**
   * Sets the ID number of this Data.
   *
   * @param id the ID number
   */
  public void setID(int id) {
    datasetID = id;
  }

  /**
   * Returns a unique identifier for this Data.
   *
   * @return the ID number
   */
  public int getID() {
    return datasetID;
  }

  private void cleanBadData() {
    for(int i = 0; i<index; i++) {
      if(Double.isNaN(xpoints[i])||Double.isInfinite(xpoints[i])||Double.isNaN(re_points[i])||Double.isInfinite(re_points[i])||Double.isNaN(im_points[i])||Double.isInfinite(im_points[i])) {
        if((index==1)||(i==index-1)) { // we only have one point and it is a bad point!
          index--;
          break;                       // exit the loop
        }
        System.arraycopy(xpoints, i+1, xpoints, i, index-i-1);
        System.arraycopy(re_points, i+1, re_points, i, index-i-1);
        System.arraycopy(im_points, i+1, im_points, i, index-i-1);
        index--;
      }
    }
  }

  /**
   * Sets the array size.
   * @param newCapacity
   */
  private void setCapacity(int newCapacity) {
    double[] tempx = xpoints;
    xpoints = new double[newCapacity];
    System.arraycopy(tempx, 0, xpoints, 0, tempx.length);
    double[] tempre = re_points;
    re_points = new double[newCapacity];
    System.arraycopy(tempre, 0, re_points, 0, tempre.length);
    double[] tempim = im_points;
    im_points = new double[newCapacity];
    System.arraycopy(tempim, 0, im_points, 0, tempim.length);
    double[] tempamp = amp_points;
    amp_points = new double[newCapacity];
    System.arraycopy(tempamp, 0, amp_points, 0, tempamp.length);
  }

  /**
   * Draw this Dataset in the drawing panel.
   * @param drawingPanel
   * @param g
   */
  public void draw(DrawingPanel drawingPanel, Graphics g) {
    if(!visible) {
        return;
    }
    Graphics2D g2 = (Graphics2D) g;
    switch(markerShape) {
       case AMP_CURVE :
         drawLinePlot(drawingPanel, g2);
         break;
       case RE_IM_CURVE :
         drawReImPlot(drawingPanel, g2);
         break;
       case PHASE_CURVE :
         drawPhaseCurve(drawingPanel, g2);
         break;
       case PHASE_BAR :
         drawPhaseBars(drawingPanel, g2);
         break;
       case PHASE_POST :
         drawPhasePosts(drawingPanel, g2);
         break;
    }
  }

  /**
   * Clear all data from this Dataset.
   */
  public void clear() {
    index = 0;
    xpoints = new double[initialSize];
    re_points = new double[initialSize];
    im_points = new double[initialSize];
    amp_points = new double[initialSize];
    ampPath.reset();
    reTrail.clear();
    imTrail.clear();
    resetXYMinMax();
  }

  /**
   * Create a string representation of the data.
   * @return  the data
   */
  public String toString() {
    if(index==0) {
      return "Dataset empty."; //$NON-NLS-1$
    }
    String s = xpoints[0]+"\t"+re_points[0]+"\t"+im_points[0]+"\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    StringBuffer b = new StringBuffer(index*s.length());
    for(int i = 0; i<index; i++) {
      b.append(xpoints[i]);
      b.append('\t');
      b.append(re_points[i]);
      b.append('\t');
      b.append(im_points[i]);
      b.append('\n');
      // s += xpoints[i] + "\t" + ypoints[i] + "\n";
    }
    return b.toString();
  }

  /**
   * Gets the number of columns for rendering in a JTable.
   * @return the count
   */
  public int getColumnCount() {
    return 3;
  }

  /**
   * Gets the number of rows for rendering in a JTable.
   * @return the count
   */
  public int getRowCount() {
    return(index+stride-1)/stride;
  }

  /**
   * Gets the name of the colummn for rendering in a JTable
   * @param columnIndex
   * @return the name
   */
  public String getColumnName(int columnIndex) {
    switch(columnIndex) {
       case 0 :
         return xColumnName;
       case 1 :
         return reColumnName;
       case 2 :
         return imColumnName;
    }
    return ""; //$NON-NLS-1$
  }

  /**
   * Gets an x or y value for rendering in a JTable.
   * @param rowIndex
   * @param columnIndex
   * @return the datum
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    rowIndex = rowIndex*stride;
    switch(columnIndex) {
       case 0 :
         return new Double(xpoints[rowIndex]);
       case 1 :
         return new Double(re_points[rowIndex]);
       case 2 :
         return new Double(im_points[rowIndex]);
    }
    return new Double(0);
  }

  /**
   * Gets the type of object for JTable entry.
   * @param columnIndex
   * @return the class
   */
  public Class<?> getColumnClass(int columnIndex) {
    return Double.class;
  }

  /**
   * Reset the minimum and maximum values.
   */
  private void resetXYMinMax() {
    // changed by W. Christian
    xmin = Double.MAX_VALUE;
    xmax = -Double.MAX_VALUE;
    remax = -Double.MAX_VALUE;
    remin = Double.MAX_VALUE;
    immax = -Double.MAX_VALUE;
    immin = Double.MAX_VALUE;
    ampmax = -Double.MAX_VALUE;
    ampmin = Double.MAX_VALUE;
    for(int i = 0; i<index; i++) {
      xmin = Math.min(xpoints[i], xmin);
      xmax = Math.max(xpoints[i], xmax);
      remax = Math.max(re_points[i], remax);
      remin = Math.min(re_points[i], remin);
      immax = Math.max(im_points[i], immax);
      immin = Math.min(im_points[i], immin);
      ampmax = Math.max(amp_points[i], ampmax);
      ampmin = Math.min(amp_points[i], ampmin);
    }
  }

  /**
   * Perform an insertion sort of the data set.
   *
   * Since data will be partially sorted this should be fast.
   * Added by W. Christian.
   */
  protected void insertionSort() {
    boolean dataChanged = false;
    if(index<2) {
      return; // need at least two points to sort.
    }
    for(int i = 1; i<index; i++) {
      if(xpoints[i]<xpoints[i-1]) { // is the i-th datum smaller?
        dataChanged = true;
        moveDatum(i);
      }
    }
    if(dataChanged) {
      recalculatePath();
    }
  }

  /**
   * Recalcualte the general path.
   */
  protected void recalculatePath() {
    ampPath.reset();
    if(index<1) {
      return;
    }
    float amp = (float) Math.sqrt(re_points[0]*re_points[0]+im_points[0]*im_points[0]);
    ampPath.moveTo((float) xpoints[0], amp);
    for(int i = 1; i<index; i++) {
      amp = (float) Math.sqrt(re_points[i]*re_points[i]+im_points[i]*im_points[i]);
      ampPath.lineTo((float) xpoints[i], amp);
    }
  }

  /**
   * Move an out-of-place datum into its correct position.
   * @param loc the datum
   */
  protected void moveDatum(int loc) {
    if(loc<1) {
      return; // zero-th point cannot be out-of-place
    }
    double x = xpoints[loc]; // save the old values
    double re = re_points[loc];
    double im = im_points[loc];
    for(int i = 0; i<index; i++) {
      if(xpoints[i]>x) { // find the insertion point
        System.arraycopy(xpoints, i, xpoints, i+1, loc-i);
        xpoints[i] = x;
        System.arraycopy(re_points, i, re_points, i+1, loc-i);
        re_points[i] = re;
        System.arraycopy(im_points, i, im_points, i+1, loc-i);
        im_points[i] = im;
        return;
      }
    }
  }

  /**
   * Draw the lines connecting the data points.
   * @param drawingPanel
   * @param g2
   */
  protected void drawLinePlot(DrawingPanel drawingPanel, Graphics2D g2) {
    AffineTransform at = (AffineTransform) (drawingPanel.getPixelTransform().clone());
    Shape s = ampPath.createTransformedShape(at);
    g2.setColor(lineColor);
    g2.draw(s);
    if(showPhase) {
      at.concatenate(flip);
      s = ampPath.createTransformedShape(at);
      g2.draw(s);
    }
  }

  /**
   * Draw the lines connecting the data points.
   * @param drawingPanel
   * @param g2
   */
  protected void drawReImPlot(DrawingPanel drawingPanel, Graphics2D g2) {
    reTrail.draw(drawingPanel, g2);
    imTrail.draw(drawingPanel, g2);
  }

  /**
   * Draw the phase as color.
   * @param drawingPanel
   * @param g2
   */
  protected void drawPhaseCurve(DrawingPanel drawingPanel, Graphics2D g2) {
    double[] xpoints = this.xpoints;
    double[] re_points = this.re_points;
    double[] im_points = this.im_points;
    int index = this.index;
    if(index<1) {
      return;
    }
    if((xpoints.length<index)||(xpoints.length!=re_points.length)||(xpoints.length!=im_points.length)) {
      return;
    }
    int yorigin = drawingPanel.yToPix(0);
    int[] xpix = new int[4];
    int[] ypix = new int[4];
    xpix[2] = drawingPanel.xToPix(xpoints[0]);
    double oldY = Math.sqrt(re_points[0]*re_points[0]+im_points[0]*im_points[0]);
    ypix[3] = drawingPanel.yToPix(-oldY);
    ypix[2] = drawingPanel.yToPix(oldY);
    double oldRe = re_points[0];
    double oldIm = im_points[0];
    for(int i = 0; i<index; i++) {
      double re = re_points[i];
      double im = im_points[i];
      double y = Math.sqrt(re*re+im*im);
      if(y>0) {
        g2.setColor(DisplayColors.phaseToColor(Math.atan2((im+oldIm)/2, (oldRe+re)/2)));
      }
      xpix[0] = drawingPanel.xToPix(xpoints[i]);
      if(centered) {
        ypix[0] = drawingPanel.yToPix(-y/2);
        ypix[1] = drawingPanel.yToPix(y/2);
      } else {
        ypix[0] = yorigin;
        ypix[1] = drawingPanel.yToPix(y);
      }
      // g2.drawLine(xnew,drawingPanel.yToPix(-y),xnew,drawingPanel.yToPix(y));
      xpix[1] = xpix[0];
      xpix[3] = xpix[2];
      g2.fillPolygon(xpix, ypix, 4);
      xpix[2] = xpix[0];
      ypix[3] = ypix[0];
      ypix[2] = ypix[1];
      oldIm = im;
      oldRe = re;
      oldY = y;
    }
  }

  /**
   * Draw the phase as a colored bar.
   * @param drawingPanel
   * @param g2
   */
  protected void drawPhaseBars(DrawingPanel drawingPanel, Graphics2D g2) {
    if(index<1) {
      return;
    }
    double[] xpoints = this.xpoints;
    double[] re_points = this.re_points;
    double[] im_points = this.im_points;
    if((xpoints.length<index)||(xpoints.length!=re_points.length)||(xpoints.length!=im_points.length)) {
      return;
    }
    int barWidth = (int) (0.5+(drawingPanel.xToPix(xmax)-drawingPanel.xToPix(xmin))/(2.0*(index-1)));
    barWidth = Math.min(markerSize, barWidth);
    int yorigin = drawingPanel.yToPix(0);
    for(int i = 0; i<index; i++) {
      double re = re_points[i];
      double im = im_points[i];
      double y = Math.sqrt(re*re+im*im);
      g2.setColor(DisplayColors.phaseToColor(Math.atan2(im, re)));
      int xpix = drawingPanel.xToPix(xpoints[i]);
      int height = Math.abs(yorigin-drawingPanel.yToPix(y));
      if(centered) {
        g2.fillRect(xpix-barWidth, yorigin-height/2, 2*barWidth+1, height);
      } else {
        g2.fillRect(xpix-barWidth, yorigin-height, 2*barWidth+1, height);
      }
    }
  }

  /**
   * Draw the phase as a colored post.
   * @param drawingPanel
   * @param g2
   */
  protected void drawPhasePosts(DrawingPanel drawingPanel, Graphics2D g2) {
    if(index<1) {
      return;
    }
    double[] xpoints = this.xpoints;
    double[] re_points = this.re_points;
    double[] im_points = this.im_points;
    if((xpoints.length<index)||(xpoints.length!=re_points.length)||(xpoints.length!=im_points.length)) {
      return;
    }
    int postWidth = (int) (0.5+(drawingPanel.xToPix(xmax)-drawingPanel.xToPix(xmin))/(2.0*(index-1)));
    postWidth = Math.min(markerSize, postWidth);
    for(int i = 0; i<index; i++) {
      double re = re_points[i];
      double im = im_points[i];
      double y = Math.sqrt(re*re+im*im);
      drawPost(drawingPanel, g2, xpoints[i], y, postWidth, DisplayColors.phaseToColor(Math.atan2(im, re)));
    }
  }

  private void drawPost(DrawingPanel drawingPanel, Graphics2D g2, double x, double y, int postWidth, Color fillColor) {
    Color edgeColor = Color.BLACK;
    int xp = drawingPanel.xToPix(x);
    int yp = drawingPanel.yToPix(y);
    int size = postWidth*2+1;
    int bottom = Math.min(drawingPanel.yToPix(0), drawingPanel.yToPix(drawingPanel.getYMin()));
    Shape shape = new Rectangle2D.Double(xp-postWidth, yp-postWidth, size, size);
    g2.setColor(edgeColor);
    g2.drawLine(xp, yp, xp, bottom);
    g2.setColor(fillColor);
    g2.fill(shape);
    g2.setColor(edgeColor);
    g2.draw(shape);
  }

  /**
* Returns the XML.ObjectLoader for this class.
*
* @return the object loader
*/
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * Sets a name that can be used to identify the dataset.
   *
   * @param name String
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the name.
   *
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * The column names to be used in the data display tool
   * @return
   */
  public String[] getColumnNames() {
    return new String[] {"Re", "Im"}; //$NON-NLS-1$ //$NON-NLS-2$
  }

  public double[][] getData2D() {
    double[][] data = new double[3][index];
    data[0] = getXPoints();
    data[1] = getRePoints();
    data[2] = getImPoints();
    return data;
  }

  public double[][][] getData3D() {
    return null;
  }

  public ArrayList<Dataset> getDatasets() {
    if((reDataset==null)||(imDataset==null)) {
      reDataset = new Dataset(Color.RED, Color.RED, true);
      imDataset = new Dataset(Color.BLUE, Color.BLUE, true);
    }
    reDataset.clear();
    imDataset.clear();
    reDataset.setXYColumnNames(xColumnName, reColumnName, "Re("+name+")"); //$NON-NLS-1$ //$NON-NLS-2$
    imDataset.setXYColumnNames(xColumnName, imColumnName, "Im("+name+")"); //$NON-NLS-1$ //$NON-NLS-2$
    reDataset.append(getXPoints(), getRePoints());
    imDataset.append(getXPoints(), getImPoints());
    ArrayList<Dataset> list = new ArrayList<Dataset>();
    list.add(reDataset);
    list.add(imDataset);
    return list;
  }

  /**
   * Some elements (a Group, for instance) do not contain data, but a list of subelements which do.
   * This method is used by Data displaying tools to create as many pages as needed.
   * @return A list of DataInformation elements, null if the element itself is a DataInformation
   */
  public java.util.List<Data> getDataList() {
    if((reDataset==null)||(imDataset==null)) {
      reDataset = new Dataset(Color.RED, Color.RED, true);
      imDataset = new Dataset(Color.BLUE, Color.BLUE, true);
    }
    reDataset.clear();
    imDataset.clear();
    reDataset.setXYColumnNames(xColumnName, reColumnName, "Re("+name+")"); //$NON-NLS-1$ //$NON-NLS-2$
    imDataset.setXYColumnNames(xColumnName, imColumnName, "Im("+name+")"); //$NON-NLS-1$ //$NON-NLS-2$
    reDataset.append(getXPoints(), getRePoints());
    imDataset.append(getXPoints(), getImPoints());
    ArrayList<Data> list = new ArrayList<Data>();
    list.add(reDataset);
    list.add(imDataset);
    return list;
  }

  /**
  * A class to save and load Dataset data in an XMLControl.
  */
  private static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      ComplexDataset data = (ComplexDataset) obj;
      control.setValue("points", data.getPoints());            //$NON-NLS-1$
      // control.setValue("x_points", data.getXPoints());
      // control.setValue("y_points", data.getYPoints());
      control.setValue("marker_shape", data.getMarkerShape()); //$NON-NLS-1$
      control.setValue("marker_size", data.getMarkerSize());   //$NON-NLS-1$
      control.setValue("sorted", data.isSorted());             //$NON-NLS-1$
      control.setValue("connected", data.isConnected());       //$NON-NLS-1$
      control.setValue("name", data.name);                     //$NON-NLS-1$
      control.setValue("x_name", data.xColumnName);            //$NON-NLS-1$
      control.setValue("re_name", data.reColumnName);          //$NON-NLS-1$
      control.setValue("im_name", data.imColumnName);          //$NON-NLS-1$
      control.setValue("line_color", data.lineColor);          //$NON-NLS-1$
      control.setValue("index", data.index);                   //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new ComplexDataset();
    }

    public Object loadObject(XMLControl control, Object obj) {
      ComplexDataset data = (ComplexDataset) obj;
      double[][] points = (double[][]) control.getObject("points"); //$NON-NLS-1$
      if((points!=null)&&(points[0]!=null)) {
        data.clear();
        for(int i = 0; i<points.length; i++) {
          data.append(points[i][0], points[i][1], points[i][2]);
        }
      }
      // for backward compatibility
      double[] xPoints = (double[]) control.getObject("x_points"); //$NON-NLS-1$
      double[] yPoints = (double[]) control.getObject("y_points"); //$NON-NLS-1$
      if((xPoints!=null)&&(yPoints!=null)) {
        data.clear();
        data.append(xPoints, yPoints);
      }
      if(control.getPropertyNames().contains("marker_shape")) { //$NON-NLS-1$
        data.setMarkerShape(control.getInt("marker_shape"));    //$NON-NLS-1$
      }
      if(control.getPropertyNames().contains("marker_size")) { //$NON-NLS-1$
        data.setMarkerSize(control.getInt("marker_size"));     //$NON-NLS-1$
      }
      data.setSorted(control.getBoolean("sorted"));       //$NON-NLS-1$
      data.setConnected(control.getBoolean("connected")); //$NON-NLS-1$
      data.name = control.getString("name");            //$NON-NLS-1$
      data.xColumnName = control.getString("x_name");   //$NON-NLS-1$
      data.reColumnName = control.getString("re_name"); //$NON-NLS-1$
      data.imColumnName = control.getString("im_name"); //$NON-NLS-1$
      Color color = (Color) control.getObject("line_color"); //$NON-NLS-1$
      if(color!=null) {
        data.lineColor = color;
      }
      data.index = control.getInt("index"); //$NON-NLS-1$
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
