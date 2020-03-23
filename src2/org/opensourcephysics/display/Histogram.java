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
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 *  Histogram maps bin number to occurrences. Histogram is Drawable and can be
 *  rendered on a DrawingPanel. Histogram also implements TableModel and can be
 *  displayed in a JTable. By default, bins consist of (notation: [ inclusive, )
 *  exclusive): ..., [-1,0), [0,1), [1,2), ...
 *
 * @author     Joshua Gould
 * @author     Wolfgang Christian
 * @created    June 26, 2002
 * @version    1.1
 */
public class Histogram extends AbstractTableModel implements Measurable, LogMeasurable, Data {
  HistogramDataset histogramDataset;

  /** draw point at top of bin */
  public final static int DRAW_POINT = 0;

  /** draw bin from y min to top of bin */
  public final static int DRAW_BIN = 1;
  
  /**
   * Should histogram be drawn on a log scale?  Default is false.
   */
  public boolean logScale = false;

  /**
   * Should the height be adjusted by bin width?  Default is false.
   */
  public boolean adjustForWidth = false;

  /**
   * The visibility of the histogram
   */
  private boolean visible=true; // Paco
  
  /**
   * Force the measured condition
   */
  private boolean measured = true; // Paco 
  
  /** color of bins */
  protected Color binFillColor = Color.red;

  /** color of bins */
  protected Color binEdgeColor = Color.red;

  /** style for drawing bins */
  protected int binStyle = DRAW_BIN;

  /** maps bin number to occurrences */
  HashMap<Integer, Double> bins;

  /** width of a bin */
  double binWidth = 1;

  /** offset of the bins */
  double binOffset = 0;

  /** false if the bins are continuous */
  boolean discrete = true;

  /** binNumber*binWidth + binOffset */
  double xmin;

  /** binNumber*binWidth + binWidth + binOffset */
  double xmax;

  /** min number of occurrences for all bins */
  final int YMIN = 0;

  /** max number of occurrences for all bins */
  double ymax;

  /** the name of the histogram */
  String name;

  /** the name of the bin */
  String binColumnName;

  /** the name of the x column */
  String xColumnName;

  /** the name of the occurrences */
  String yColumnName;

  /**
   *  bin number-occurrences pairs in histogram, used for table model
   *  implementation
   */
  Map.Entry<?, ?>[] entries = new Map.Entry<?, ?>[0];

  /** whether the data has changed since the last time the entries were retrieved */
  boolean dataChanged;

  /** total occurrences in histogram */
  double sum;

  /** whether occurrences are normalized to one */
  boolean normalizedToOne = false;

  /**
   *  amount by which this histogram is shifted to the right, so that it peeks
   *  out from behind other histograms.
   */
  double barOffset;

  /** an integer ID that identifies this object */
  protected int datasetID = hashCode();

  /** Histogram constructor. */
  public Histogram() {
    binColumnName = DisplayRes.getString("Histogram.Column.BinNumber"); //$NON-NLS-1$
    xColumnName = "x";                                                  //$NON-NLS-1$
    yColumnName = DisplayRes.getString("Histogram.Column.Occurrences"); //$NON-NLS-1$
    name = DisplayRes.getString("Histogram.Title");                     //$NON-NLS-1$
    clear();
  }

  /**
   *  Reads a file and appends the data contained in the file to this Histogram.
   *  The format of the file is bins \t occurrences. Lines beginning with # and
   *  empty lines are ignored.
   *
   * @param  inputPathName            A pathname string.
   * @exception  java.io.IOException  Description of the Exception
   */
  public void read(String inputPathName) throws java.io.IOException {
    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(inputPathName));
    String s = null;
    while((s = reader.readLine())!=null) {
      s = s.trim();
      if(s.equals("")||(s.length()==0)||(s.charAt(0)=='#')) { // ignore empty lines and lines beginning with # //$NON-NLS-1$
        continue;
      }
      try {
        java.util.StringTokenizer st = new java.util.StringTokenizer(s, "\t"); //$NON-NLS-1$
        int binNumber = Integer.parseInt(st.nextToken());
        double numberOfoccurrences = Double.parseDouble(st.nextToken());
        Double prioroccurrences = bins.get(new Integer(binNumber));
        if(prioroccurrences==null) {                                           // first occurence for this bin
          bins.put(new Integer(binNumber), new Double(numberOfoccurrences));
        } else {
          numberOfoccurrences += prioroccurrences.doubleValue();               // increase occurrences for bin by prioroccurrences
          bins.put(new Integer(binNumber), new Double(numberOfoccurrences));
        }
        ymax = Math.max(numberOfoccurrences, ymax);
        xmin = Math.min(binNumber*binWidth+binOffset, xmin);
        xmax = Math.max(binNumber*binWidth+binWidth+binOffset, xmax);
      } catch(java.util.NoSuchElementException nsee) {
        nsee.printStackTrace();
      } catch(NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }
    reader.close();
    dataChanged = true;
  }

  /**
   *  Creates a string representation of this Histogram. The bins are displayed
   *  in ascending order. The format of this string is bin number \t occurrences.
   *  Each bin starts on a new line.
   *
   * @return    A String with the number of occurrences for each bin.
   * @see       #toString
   */
  public String toSortedString() {
    Set<Integer> keySet = bins.keySet();
    Object[] keys = keySet.toArray();
    Arrays.sort(keys);
    String s = "x\tx"; //$NON-NLS-1$
    StringBuffer buf = new StringBuffer(s.length()*keys.length);
    for(int i = 0; i<keys.length; i++) {
      Object key = keys[i];
      buf.append(key);
      buf.append("\t"); //$NON-NLS-1$
      buf.append(bins.get(keys[i]));
      buf.append("\n"); //$NON-NLS-1$
    }
    return buf.toString();
  }

  /**
   *  Creates a string representation of this Histogram. The format is bin
   *  number\t occurrences. Each new bin starts on a new line.
   *
   * @return    A String with the number of occurrences for each bin.
   */
  public String toString() {
    Set<Integer> set = bins.keySet();
    Iterator<Integer> keys = set.iterator();
    String s = "x\tx"; //$NON-NLS-1$
    StringBuffer buf = new StringBuffer(s.length()*set.size());
    while(keys.hasNext()) {
      Integer binNumber = keys.next();
      Double occurrences = bins.get(binNumber);
      buf.append(binNumber);
      buf.append("\t"); //$NON-NLS-1$
      buf.append(occurrences);
      buf.append("\n"); //$NON-NLS-1$
    }
    return buf.toString();
  }

  /**
   *  Computes the hash code (bin number) for the specified value
   *
   * @param  value
   * @return        the hash code
   */
  public int hashCode(double value) {
    return(int) (Math.floor((value-binOffset)/binWidth));
  }

  /**
   *  Append a value with number of occurrences to the Histogram.
   *
   * @param  value
   * @param  numberOfoccurrences
   */
  public synchronized void append(double value, double numberOfoccurrences) {
    sum += numberOfoccurrences;
    int binNumber = hashCode(value);
    // Determine if there have previously been any occurrences for this bin
    Double occurrences = bins.get(new Integer(binNumber));
    if(occurrences==null) {                             // first occurence for this bin
      bins.put(new Integer(binNumber), new Double(numberOfoccurrences));
    } else {
      // need to put Objects in HashMap, but can only add doubles
      numberOfoccurrences += occurrences.doubleValue(); // increase occurrences for bin by numberOfoccurrences
      bins.put(new Integer(binNumber), new Double(numberOfoccurrences));
    }
    ymax = Math.max(numberOfoccurrences, ymax);
    xmin = Math.min(binNumber*binWidth+binOffset, xmin);
    xmax = Math.max(binNumber*binWidth+binWidth+binOffset, xmax);
    dataChanged = true;
  }

  /**
   *  Appends a value with 1 occurence.
   *
   * @param  value
   */
  public void append(double value) {
    append(value, 1);
  }

  /**
   *  Appends values from an input file. Each value is separated by a \n
   *
   * @param  inputPathName    A pathname string.
   * @exception  IOException  Description of the Exception
   */
  public void append(String inputPathName) throws IOException {
    BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(inputPathName));
    String s = null;
    while((s = br.readLine())!=null) {
      s = s.trim();
      if(s.equals("")||(s.length()==0)||(s.charAt(0)=='#')) { //$NON-NLS-1$
        continue;
      }
      try {
        double d = Double.parseDouble(s);
        append(d, 1);
      } catch(NumberFormatException nfe) {
        nfe.printStackTrace();
      }
    }
    br.close();
  }

  /**
   *  Appends an array of values with 1 occurence.
   *
   * @param  values
   */
  public void append(double[] values) {
    for(int i = 0; i<values.length; i++) {
      append(values[i], 1);
    }
  }

  /**
   * Sets the visibility of the histogram
   * @param visibility
   */
  public void setVisible(boolean visibility) {
    this.visible = visibility;
  }
  
  /**
   *  Draws this histogram in the drawing panel.
   *
   * @param  drawingPanel
   * @param  g
   */
  public synchronized void draw(DrawingPanel drawingPanel, Graphics g) {
    if(bins.size()==0 || !visible) {
      return;
    }
    Shape oldClip = g.getClip();
    g.setColor(binFillColor);
    g.clipRect(0, 0, drawingPanel.getWidth(), drawingPanel.getHeight());
    for(Iterator<Integer> keys = bins.keySet().iterator(); keys.hasNext(); ) {
      Integer binNumber = keys.next();
      Double d = (bins.get(binNumber));
      if(d==null) {
        return;
      }
      double occurrences = d.doubleValue();
      if(normalizedToOne) {
        occurrences /= sum;
      }
      if(binStyle==DRAW_BIN) {
        drawBin(drawingPanel, g, binNumber.intValue(), occurrences);
      } else {
        drawPoint(drawingPanel, g, binNumber.intValue(), occurrences);
      }
    }
    g.setClip(oldClip);
  }

  /** Clears all data from this histogram and resets min and max values. */
  public synchronized void clear() {
    bins = new HashMap<Integer, Double>();
    xmin = Integer.MAX_VALUE;
    xmax = Integer.MIN_VALUE;
    ymax = Integer.MIN_VALUE;
    sum = 0;
    dataChanged = true;
  }

  /**
   *  Gets an array of bin number-occurrences pairs
   *
   * @return    The entries.
   */
  public Map.Entry<?, ?>[] entries() {
    updateEntries();
    return entries;
  }

  /**
   *  Sets the style for drawing this histogram. Options are DRAW_POINT, which
   *  draws a point at the top of the bin, and DRAW_BIN which draws the entire
   *  bin down to the x axis. Default is DRAW_BIN.
   *
   * @param  style
   */
  public void setBinStyle(int style) {
    binStyle = style;
  }

  /**
   *  Sets the discrete flag.
   *
   * @param  _discrete  <code>true<\code> if bins are discrete, <code>false<\code> if bins are continuous.
   */
  public void setDiscrete(boolean _discrete) {
    discrete = _discrete;
  }

  /**
   *  Sets the offset of the bins. Default is 0.
   *
   * @param  _binOffset
   */
  public void setBinOffset(double _binOffset) {
    binOffset = _binOffset;
  }

  /**
   *  Set the offset of the bars as a fraction of a bin width. The offset is the
   *  amount by which this histogram is shifted to the right, so that it peeks
   *  out from behind later histograms when displayed in a DrawingPanel.
   *
   * @param  _barOffset  The new barOffset value
   */
  public void setBarOffset(double _barOffset) {
    barOffset = _barOffset;
  }

  /**
   * Line color to use for this data
   * @return
   */
  public java.awt.Color getLineColor() {
    return binEdgeColor;
  }

  /**
   * Line colors for Data interface.
   * @return
   */
  public java.awt.Color[] getLineColors() {
    return new Color[] {binEdgeColor, binEdgeColor};
  }

  /**
   * Fill color to use for this data
   * @return
   */
  public java.awt.Color getFillColor() {
    return binFillColor;
  }

  /**
   * Fill colors for Data interface.
   * @return
   */
  public java.awt.Color[] getFillColors() {
    return new Color[] {binFillColor, binFillColor};
  }

  /**
   *  Sets the bin color.
   *
   * @param  binColor
   */
  public void setBinColor(Color binColor) {
    binFillColor = binColor;
    binEdgeColor = binColor;
  }

  /**
   * Sets the bin's fill and edge colors.  If the fill color is null the bin is not filled.
   *
   * @param fillColor
   * @param edgeColor
   */
  public void setBinColor(Color fillColor, Color edgeColor) {
    binFillColor = fillColor;
    binEdgeColor = edgeColor;
  }

  /**
   *  Sets the width of a bin.
   *
   * @param  _binWidth
   */
  public void setBinWidth(double _binWidth) {
    binWidth = _binWidth;
  }

  /**
   * Sets a name that can be used to identify the dataset.
   *
   * @param name String
   */
  public void setName(String name) {
    this.name = TeXParser.parseTeX(name);
  }

  /**
   * Gets the dataset name.
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
    return new String[] {xColumnName, yColumnName};
  }

  /**
   * Some elements (a Group, for instance) do not contain data, but a list of subelements which do.
   * This method is used by Data displaying tools to create as many pages as needed.
   * @return A list of DataInformation elements, null if the element itself is a DataInformation
   */
  public java.util.List<Data> getDataList() {
    return null;
  }

  /**
   *  Sets the column names when rendering this histogram in a JTable.
   *
   * @param  _binColumnName
   * @param  _yColumnName
   */
  public void setXYColumnNames(String _xColumnName, String _yColumnName) {
    xColumnName = TeXParser.parseTeX(_xColumnName);
    yColumnName = TeXParser.parseTeX(_yColumnName);
  }

  /**
   *  Sets the column names when rendering this histogram in a JTable.
   *
   * @param  _binColumnName
   * @param  _yColumnName
   * @param _name String  the name of the histogram
   */
  public void setXYColumnNames(String _xColumnName, String _yColumnName, String _name) {
    xColumnName = TeXParser.parseTeX(_xColumnName);
    yColumnName = TeXParser.parseTeX(_yColumnName);
    name = TeXParser.parseTeX(_name);
  }

  /**
   *  Normalizes the occurrences in this histogram to one.
   *
   * @param  b
   */
  public void setNormalizedToOne(boolean b) {
    normalizedToOne = b;
  }

  /**
   *  Gets the width of a bin.
   *
   * @return    The bin width.
   */
  public double getBinWidth() {
    return binWidth;
  }

  /**
   *  Gets the offset of the bins.
   *
   * @return    The bin offset.
   */
  public double getBinOffset() {
    return binOffset;
  }

  /**
   *  Gets the x world coordinate for the left hand side of this histogram.
   *
   * @return    xmin
   */
  public double getXMin() {
    return(discrete&&(bins.size()>1)) ? xmin-binWidth : xmin;
  }

  /**
   *  Gets the x world coordinate for the right hand side of this histogram.
   *
   * @return    xmax
   */
  public double getXMax() {
    return xmax;
  }

  /**
   *  Gets the y world coordinate for the bottom of this histogram.
   *
   * @return    minimum y value
   */
  public double getYMin() {
    return YMIN;
  }

  /**
   *  Gets the y world coordinate for the top of this histogram.
   *
   * @return    xmax
   */
  public double getYMax() {
    double max = (normalizedToOne ? ymax/sum : ymax);
    if(adjustForWidth) {
      max = max/getBinWidth();
    }
    if(logScale) {
      max = Math.log(max);
    }
    return max;
  }

  /**
   * Gets the minimum x needed to draw this object on a log scale.
   * @return minimum
   */
  public double getXMinLogscale() {
    double xmin = getXMin();
    if(xmin>0) {
      return xmin;
    }
    return binOffset;
  }

  /**
   * Gets the maximum x needed to draw this object on a log scale.
   * @return maximum
   */
  public double getXMaxLogscale() {
    double xmax = getXMax();
    if(xmax>0) {
      return xmax;
    }
    return binOffset+10;
  }

  /**
   * Gets the minimum y needed to draw this object on a log scale.
   * @return minimum
   */
  public double getYMinLogscale() {
    return 1;
  }

  /**
   * Gets the maximum y needed to draw this object on a log scale on a log scale.
   * @return maximum
   */
  public double getYMaxLogscale() {
    return Math.max(10, getYMax()); // make sure we have at least one decade on log scale
  }

  /**
   *  Gets the valid measure flag. The measure is valid if this histogram is not
   *  empty.
   *
   * @return    <code>true<\code> if measure is valid.
   */
  public boolean isMeasured() {
    return bins.size()>0 && measured; // Paco
  }

  /**
   * Forces the measured condition of the histogram
   * @param visibility
   */
  public void setMeasured(boolean measure) { // Paco
    this.measured = measure;
  }
  
  /**
   *  Gets the name of the column for rendering in a JTable
   *
   * @param  column  the column whose value is to be queried
   * @return         the name
   */
  public String getColumnName(int column) {
    if(column==0) {
      return binColumnName;
    } else if(column==1) {
      return xColumnName;
    } else {
      return yColumnName;
    }
  }

  /**
   *  Gets the number of rows for rendering in a JTable.
   *
   * @return    the count
   */
  public int getRowCount() {
    return bins.size();
  }

  /**
   *  Gets the name of the colummn for rendering in a JTable
   *
   * @return    the name
   */
  public int getColumnCount() {
    return 3;
  }

  /**
   *  Gets a bin number or occurrences for bin number for rendering in a JTable.
   *
   * @param  row     the row whose value is to be queried
   * @param  column  the column whose value is to be queried
   * @return         the datum
   */
  public Object getValueAt(int row, int column) {
    updateEntries();
    Map.Entry<?, ?> entry = entries[row];
    if(column==0) {
      return entry.getKey();
    }
    if(column==1) {
      return new Double(((Integer) entry.getKey()).doubleValue()*binWidth+binWidth/2.0+binOffset);
    }
    if(normalizedToOne) {
      Double d = (Double) entry.getValue();
      return new Double(d.doubleValue()/sum);
    }
    return entry.getValue();
  }

  /**
   *  Gets the type of object for JTable entry.
   *
   * @param  columnIndex  the column whose value is to be queried
   * @return              the class
   */
  public Class<?> getColumnClass(int columnIndex) {
    return((columnIndex==0) ? Integer.class : Double.class);
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

  /**
   *  Draws a point at the top of a bin.
   *
   * @param  drawingPanel
   * @param  g
   * @param  binNumber
   * @param  occurrences
   */
  protected void drawPoint(DrawingPanel drawingPanel, Graphics g, int binNumber, double occurrences) {
    int px = drawingPanel.xToPix(getLeftMostBinPosition(binNumber)); // leftmost position of bin
    int py = drawingPanel.yToPix(occurrences);
    int pointRadius = 2;
    if(discrete) {
      g.fillRect(px-pointRadius, py-pointRadius, pointRadius*2, pointRadius*2);
    } else { // continous, draw entire bin
      int px2 = drawingPanel.xToPix(getRightMostBinPosition(binNumber));
      int pWidth = px2-px;
      g.fillRect(px, py, pWidth, pointRadius*2);
    }
  }

  /**
   *  Draws a filled bin.
   *
   * @param  drawingPanel
   * @param  g
   * @param  binNumber
   * @param  occurrences
   */
  protected void drawBin(DrawingPanel drawingPanel, Graphics g, int binNumber, double occurrences) {
    if(adjustForWidth) {
      occurrences = occurrences/getBinWidth();
    }
    if(logScale) {
      occurrences = Math.max(0, Math.log(occurrences));
    }
    int binlx = drawingPanel.xToPix(getLeftMostBinPosition(binNumber));
    if(discrete) {
      if(binEdgeColor!=null) {
        g.setColor(binEdgeColor);
        g.drawLine(binlx, drawingPanel.yToPix(YMIN), binlx, drawingPanel.yToPix(occurrences));
      }
    } else { // continous, draw entire bin
      int binrx = drawingPanel.xToPix(getRightMostBinPosition(binNumber));
      int pWidth = binrx-binlx;
      double pHeight = drawingPanel.getYPixPerUnit()*occurrences;
      java.awt.geom.Rectangle2D.Double rect = new java.awt.geom.Rectangle2D.Double(binlx, drawingPanel.yToPix(occurrences), pWidth, pHeight);
      Graphics2D g2 = (Graphics2D) g;
      if(binFillColor!=null) {
        g.setColor(binFillColor);
        g2.fill(rect);
      }
      if(binEdgeColor!=null) {
        g.setColor(binEdgeColor);
        g2.draw(rect);
      }
    }
  }

  /**
   *  Gets an array containing the bin centers.
   *
   * @return   the bins
   */
  public double[] getXPoints() {
    int nbins = 1+(int) ((xmax-xmin)/binWidth);
    if(nbins<1) {
      return new double[0];
    }
    double[] xdata = new double[nbins];
    for(int i = 0; i<nbins; i++) {
      xdata[i] = xmin+i*binWidth+binOffset+binWidth/2;
      //System.out.println("number="+i+"  x="+xdata[i]);
    }
    return xdata;
  }

  /**
   * Gets an array containing the values within the bins.
   *
   * @return    the values of the bins
   */
  public double[] getYPoints() {
    int nbins = 1+(int) ((xmax-xmin)/binWidth);
    if(nbins<1) {
      return new double[0];
    }
    double[] ydata = new double[nbins];
    for(int i = 0; i<nbins; i++) {
      Integer binNumber = new Integer(i);
      Double bin = bins.get(binNumber);
      ydata[i] = (bin==null) ? 0 : bin.doubleValue();
      //System.out.println("number"+binNumber.intValue()+"  x="+data[0][i]+ "  occurrences="+data[1][i]);
    }
    return ydata;
  }

  /**
   * Gets a data array containing both the bin centers and the values within the bins.
   *
   * @return a double[index][2] array of data
   */
  public double[][] getPoints() {
    int nbins = 1+(int) ((xmax-xmin)/binWidth);
    if(nbins<1) {
      return new double[2][0];
    }
    double[][] data = new double[2][nbins];
    int iStart = (int) (xmin/binWidth);
    for(int i = 0; i<nbins; i++) {
      Integer binNumber = new Integer(i+iStart);
      Double bin = bins.get(binNumber);
      data[0][i] = xmin+i*binWidth+binOffset+binWidth/2;
      data[1][i] = (bin==null) ? 0 : bin.doubleValue();
      // System.out.println("number"+binNumber.intValue()+"  x="+data[0][i]+ "  occurances="+data[1][i]);
    }
    return data;
  }

  /**
   * Gets a data array containing both the bin centers and the values within the bins.
   *
   * @return a double[index][2] array of data
   */
  public double[][] getLogPoints() {
    int nbins = (int) Math.round((xmax-xmin)/binWidth);
    if(nbins<1) {
      return new double[2][0];
    }
    double[][] data = new double[2][nbins];
    int iStart = (int) (xmin/binWidth);
    for(int i = 0; i<nbins; i++) {
      Integer binNumber = new Integer(i+iStart);
      Double bin = bins.get(binNumber);
      data[0][i] = xmin+i*binWidth+binOffset+binWidth/2;
      data[1][i] = (bin==null) ? 0 : bin.doubleValue();
      data[1][i] = (data[1][i]>0) ? Math.log(data[1][i]) : 0;
    }
    return data;
  }

  /**
   * Method getLeftMostBinPosition
   *
   * @param binNumber
   *
   * @return position
   */
  public double getLeftMostBinPosition(int binNumber) {
    return binNumber*binWidth+binOffset+binWidth*barOffset;
  }

  /**
   * Method getRightMostBinPosition
   *
   * @param binNumber
   *
   * @return position
   */
  public double getRightMostBinPosition(int binNumber) {
    return binNumber*binWidth+binWidth+binOffset+binWidth*barOffset;
  }

  /**
   *  Updates the bin number-occurrences array if data has changed since the last
   *  update
   */
  private synchronized void updateEntries() {
    if(dataChanged) {
      entries = bins.entrySet().toArray(entries);
      dataChanged = false;
    }
  }

  public double[][] getData2D() {
    double[][] data = (logScale) ? getLogPoints() : getPoints();
    return data;
  }

  public double[][][] getData3D() {
    return null;
  }

  public ArrayList<Dataset> getDatasets() {
    double[][] data = (logScale) ? getLogPoints() : getPoints();
    double[] bins = data[0];
    double[] vals = data[1];
    //double start=bins[0]-getBinWidth()/2;
    //double stop=bins[bins.length-1]+getBinWidth()/2;
    double start = 0;
    double stop = getBinWidth();
    if((bins!=null)&&(bins.length>0)) {
      start = bins[0]-getBinWidth()/2;
      stop = bins[bins.length-1]+getBinWidth()/2;
    }
    if(histogramDataset==null) {
      histogramDataset = new HistogramDataset(start, stop, getBinWidth());
    } else {
      histogramDataset.clear();
      histogramDataset.setBinWidth(start, stop, getBinWidth());
    }
    histogramDataset.setXYColumnNames(xColumnName, yColumnName, name);
    histogramDataset.append(bins, vals);
    histogramDataset.setMarkerColor(binFillColor, binEdgeColor);
    ArrayList<Dataset> list = new ArrayList<Dataset>();
    list.add(histogramDataset);
    return list;
  }

  /**
* Returns the XML.ObjectLoader for this class.
*
* @return the object loader
*/
  public static XML.ObjectLoader getLoader() {
    return new HistogramLoader();
  }

  /**
   * A class to save and load Dataset data in an XMLControl.
   */
  protected static class HistogramLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      Histogram his = (Histogram) obj;
      double[][] points = (his.logScale) ? his.getLogPoints() : his.getPoints();
      double[] bins = points[0];
      double[] vals = points[1];
      control.setValue("log_scale", his.logScale);              //$NON-NLS-1$
      control.setValue("discrete", his.discrete);               //$NON-NLS-1$
      control.setValue("adjust_for_width", his.adjustForWidth); //$NON-NLS-1$
      control.setValue("bin_fill_color", his.binFillColor);     //$NON-NLS-1$
      control.setValue("bin_edge_color", his.binEdgeColor);     //$NON-NLS-1$
      control.setValue("bin_style", his.binStyle);              //$NON-NLS-1$
      control.setValue("bin_width", his.binWidth);              //$NON-NLS-1$
      control.setValue("bin_offset", his.binOffset);            //$NON-NLS-1$
      control.setValue("name", his.name);                       //$NON-NLS-1$
      control.setValue("x_column_name", his.xColumnName);       //$NON-NLS-1$
      control.setValue("y_column_name", his.yColumnName);       //$NON-NLS-1$
      control.setValue("bin_column_name", his.binColumnName);   //$NON-NLS-1$
      control.setValue("bins", bins);                           //$NON-NLS-1$
      control.setValue("vals", vals);                           //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new Histogram();
    }

    public Object loadObject(XMLControl control, Object obj) {
      Histogram his = (Histogram) obj;
      double[] bins = (double[]) control.getObject("bins"); //$NON-NLS-1$
      double[] vals = (double[]) control.getObject("vals"); //$NON-NLS-1$
      his.name = control.getString("name");                           //$NON-NLS-1$
      his.xColumnName = control.getString("x_column_name");           //$NON-NLS-1$
      his.yColumnName = control.getString("y_column_name");           //$NON-NLS-1$
      his.binColumnName = control.getString("bin_column_name");       //$NON-NLS-1$
      his.logScale = control.getBoolean("log_scale");                 //$NON-NLS-1$
      his.discrete = control.getBoolean("discrete");                  //$NON-NLS-1$
      his.adjustForWidth = control.getBoolean("adjust_for_width");    //$NON-NLS-1$
      his.binFillColor = (Color) control.getObject("bin_fill_color"); //$NON-NLS-1$
      his.binEdgeColor = (Color) control.getObject("bin_edge_color"); //$NON-NLS-1$
      his.binStyle = control.getInt("bin_style");                     //$NON-NLS-1$
      his.binWidth = control.getDouble("bin_width");                  //$NON-NLS-1$
      his.binOffset = control.getDouble("bin_offset");                //$NON-NLS-1$
      his.adjustForWidth = control.getBoolean("adjust_for_width");    //$NON-NLS-1$
      if((bins!=null)&&(vals!=null)) {
        for(int i = 0, n = bins.length; i<n; i++) {
          his.append(bins[i], vals[i]);
        }
      }
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
