/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import org.opensourcephysics.display.Dimensioned;
import org.opensourcephysics.display.DrawableTextLine;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.OSPLayout;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.TextLine;
import org.opensourcephysics.tools.FontSizer;

/**
 *  A modified version of the ptolemy.plot.PlotBox class designed to work with
 *  the OSP drawing framework. See <a href="http://ptolemy.eecs.berkeley.edu/java/">
 *  Ptolemy Group Java</a> at UC Berkeley for more information. This class
 *  provides a labeled box within which to place a data plot. A title, X and Y
 *  axis labels, and tick marks are all supported. The tick marks for the axes
 *  are usually computed automatically from the ranges. Every attempt is made to
 *  choose reasonable positions for the tick marks regardless of the data ranges
 *  (powers of ten multiplied by 1, 2, or 5 are used). However, they can also be
 *  specified explicitly using the methods addXTick and addYTick. A <i>label</i>
 *  is a string that must be surrounded by quotation marks if it contains any
 *  spaces. A <i>position</i> is a number giving the location of the tick mark
 *  along the axis. For example, a horizontal axis for a frequency domain plot
 *  might have tick marks as follows: <pre>XTicks: -PI -3.14159, -PI/2 -1.570795, 0 0, PI/2 1.570795, PI 3.14159</pre>
 *  Tick marks could also denote years, months, days of the week, etc. Exponents
 *  are not drawn if min and max values are between 0 and 1000 and a linear
 *  scale is used. <p>
 *
 *  The X and Y axes can also use a logarithmic scale. The grid labels represent
 *  powers of 10. Note that if a logarithmic scale is used, then the values (before the log of the value is taken) must
 *  be positive. Non-positive values will be silently dropped. By default, tick
 *  marks are connected by a light grey background grid.
 *
 * @author     J. Gould
 * @author     W. Christian
 * @created    October 10, 2002
 */
// TO DO- add legends, change right gutter for legends
public class CartesianType1 extends AbstractAxes implements CartesianAxes, Dimensioned {
  /** The range of the data to be plotted. */
  double yMax, yMin, xMax, xMin;

  /** Whether to draw the axes using a logarithmic scale. */
  boolean xlog = false, ylog = false;

  /** For use in calculating log base 10. A log times this is a log base 10. */
  final static double LOG10SCALE = 1/Math.log(10);

  /**
   *  The range of the plot as labeled (multiply by 10^exp for actual range.
   */
  double ytickMax, ytickMin, xtickMax, xtickMin;

  /** The power of ten by which the range numbers should be multiplied. */
  int yExponent, xExponent;
  /* Fonts defined in superclass. */
  // Font labelFont = new Font("Dialog", Font.PLAIN, 12);
  // Font superscriptFont = new Font("Dialog", Font.PLAIN, 9);
  // Font titleFont = new Font("Dialog", Font.BOLD, 14);

  /** FontMetric information. */
  FontMetrics labelFontMetrics = null, superscriptFontMetrics = null, titleFontMetrics = null;

  /** Used for log axes. Index into vector of axis labels. */
  int gridCurJuke = 0;

  /** Used for log axes. Base of the grid. */
  double gridBase;

  /** The title and label strings. */
  protected DrawableTextLine xLine = new DrawableTextLine("x", 0, 0); //$NON-NLS-1$
  protected DrawableTextLine yLine = new DrawableTextLine("y", 0, 0); //$NON-NLS-1$

  /** If XTicks or YTicks are given/ */
  ArrayList<Double> xticks = null, yticks = null;
  ArrayList<String> xticklabels = null, yticklabels = null;
  DecimalFormat numberFormat = new DecimalFormat();
  DecimalFormat scientificFormat = new DecimalFormat("0.0E0");         //$NON-NLS-1$

  /** Whether to draw a background grid. */
  boolean drawMajorXGrid = true;
  boolean drawMinorXGrid = false;
  boolean drawMajorYGrid = true;
  boolean drawMinorYGrid = false;

  /** current gutters fot plot. The initial values are the default values */
  private int topGutter = 25;
  private int bottomGutter = 45;
  private int leftGutter = 45;
  private int rightGutter = 25;

  /** length of a tick mark in pixels NOTE: subjective tick length. */
  private int tickLength = 5;
  private boolean adjustGutters = true;

  /**
   *  Constructor for the AxesType1 object
   *
   * @param  panel  the panel on which this axes is drawn
   */
  public CartesianType1(PlottingPanel panel) {
    super(panel);
    defaultTopGutter = topGutter;
    defaultBottomGutter = bottomGutter;
    defaultLeftGutter = leftGutter;
    defaultRightGutter = rightGutter;
    labelFont = new Font("Dialog", Font.PLAIN, 12);      //$NON-NLS-1$
    superscriptFont = new Font("Dialog", Font.PLAIN, 9); //$NON-NLS-1$
    xLine.setJustification(TextLine.CENTER);
    xLine.setFont(labelFont);
    xLine.setPixelXY(true);
    yLine.setJustification(TextLine.CENTER);
    yLine.setFont(labelFont);
    yLine.setTheta(Math.PI/2);
    yLine.setPixelXY(true);
    titleLine.setJustification(TextLine.CENTER);
    titleLine.setFont(titleFont);
    titleLine.setPixelXY(true);
    if(panel==null) {
      return;
    }
    panel.setPreferredGutters(leftGutter, topGutter, rightGutter, bottomGutter);
    measureFonts(panel);
    panel.setAxes(this);
    panel.setCoordinateStringBuilder(CoordinateStringBuilder.createCartesian());
    // resize fonts in order to adjust gutters
    resizeFonts(FontSizer.getFactor(FontSizer.getLevel()), panel);
  }

  private int xToPix(double x, DrawingPanel panel) {
    double[] pixelMatrix = panel.getPixelMatrix();
    double pix = pixelMatrix[0]*x+pixelMatrix[4];
    if(pix>Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if(pix<Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    // return  (int)Math.round(pix);
    return(int) Math.floor((float) pix); // gives better registration with affine transformation
  }

  private int yToPix(double y, DrawingPanel panel) {
    double[] pixelMatrix = panel.getPixelMatrix();
    double pix = pixelMatrix[3]*y+pixelMatrix[5];
    if(pix>Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if(pix<Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    // return  (int)Math.round(pix);
    return(int) Math.floor((float) pix); // gives better registration with affine transformation
  }

  /**
   * Gets the preferred gutters to insure that titles and tick marks are properly displayed in the given drawing panel.
   * @return int[]
   */
  private int getLeftGutter(DrawingPanel panel) {
    int gutter = 40;
    if(ylog) {
      return gutter+10;
    }
    int height = panel.getHeight()-topGutter-bottomGutter;
    int numberYTickMarks = 2+height/(labelFontMetrics.getHeight()+10);
    numberYTickMarks = (int) (numberYTickMarks/panel.getImageRatio());
    double yTickSize = roundUp((ytickMax-ytickMin)/numberYTickMarks);
    // Compute y starting point so it is a multiple of yTickSize.
    double yStart = yTickSize*Math.ceil(ytickMin/yTickSize);
    int numfracdigits = numFracDigits(yTickSize);
    double chop = Math.abs(yTickSize/100);
    if((yTickSize==0)||(Math.abs((ytickMax-yStart)/yTickSize)>50)) {
      return gutter;
    }
    double ypos = yStart;
    int sh = labelFontMetrics.getHeight();
    for(int i = 0; i<=numberYTickMarks; i++) {
      String yticklabel = formatNum(ypos, numfracdigits, chop);
      int sw = labelFontMetrics.stringWidth(yticklabel);
      gutter = Math.max(sw+2*sh, gutter);
      ypos += yTickSize;
    }
    return Math.min(gutter, panel.getWidth());
  }

  /**
   * Draws the plot by implementing the drawable interface.
   * Most of the drawing is done in the DrawPlot method after the gutters are set.
   *
   * @param  panel
   * @param  g
   */
  public void draw(DrawingPanel panel, Graphics g) {
    if(!visible) {
      return;
    }
    topGutter = panel.getTopGutter();
    bottomGutter = panel.getBottomGutter();
    leftGutter = panel.getLeftGutter();
    rightGutter = panel.getRightGutter();
    yMax = panel.getYMax();
    yMin = panel.getYMin();
    xMax = panel.getXMax();
    xMin = panel.getXMin();
    if(xMax<xMin) {
      double temp = xMax;
      xMax = xMin;
      xMin = temp;
    }
    if(yMax<yMin) {
      double temp = yMax;
      yMax = yMin;
      yMin = temp;
    }
    setXRange(xMin, xMax);
    setYRange(yMin, yMax);
    // the following line changed by Doug Brown 2009-03-19
    //    if(adjustGutters && panel.isAutoscaleY()) {
    if(adjustGutters) {
      leftGutter = Math.max(leftGutter, getLeftGutter(panel));
      if(leftGutter!=panel.getLeftGutter()) {
        panel.setGutters(leftGutter, topGutter, rightGutter, bottomGutter);
        panel.recomputeTransform();
      }
    }
    numberFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
    scientificFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
    drawPlot(panel, g);
  }

  /*
   *   Add a legend (displayed at the upper right) for the specified data set with
   *   the specified string. Short strings generally fit better than long strings.
   *   If the string is empty, or the argument is null, then no legend is added.
   *   @param  label     The feature to be added to the XTick attribute
   *   @param  position  The feature to be added to the XTick attribute
   */
  /*
   *   public void addLegend(int dataset, String legend) {
   *   if(legend == null || legend.equals("")) {
   *   return;
   *   }
   *   legendStrings.add(legend);
   *   legendDatasets.add(new Integer(dataset));
   *   }
   */

  /**
   *  Specify a tick mark for the X axis. The label given is placed on the axis
   *  at the position given by <i>position</i> . If this is called once or more,
   *  automatic generation of tick marks is disabled. The tick mark will appear
   *  only if it is within the X range.
   *
   * @param  label     The label for the tick mark.
   * @param  position  The position on the X axis.
   */
  public void addXTick(String label, double position) {
    if(xticks==null) {
      xticks = new ArrayList<Double>();
      xticklabels = new ArrayList<String>();
    }
    xticks.add(new Double(position));
    xticklabels.add(label);
  }

  /**
   *  Specify a tick mark for the Y axis. The label given is placed on the axis
   *  at the position given by <i>position</i> . If this is called once or more,
   *  automatic generation of tick marks is disabled. The tick mark will appear
   *  only if it is within the Y range.
   *
   * @param  label     The label for the tick mark.
   * @param  position  The position on the Y axis.
   */
  public void addYTick(String label, double position) {
    if(yticks==null) {
      yticks = new ArrayList<Double>();
      yticklabels = new ArrayList<String>();
    }
    yticks.add(new Double(position));
    yticklabels.add(label);
  }

  /**
   *  Set the label font, which is used for axis labels and legend labels. The
   *  font names understood are those understood by java.awt.Font.decode().
   *
   * @param  name  A font name.
   */
  public void setLabelFont(String name) {
    if((name==null)||name.equals("")) { //$NON-NLS-1$
      return;
    }
    labelFont = Font.decode(name);
    // labelFontMetrics = getFontMetrics(labelFont);  FIX_ME
  }

  /**
   *  Set the title of the graph.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  title the title
   * @param font_name an optional font name
   */
  public void setTitle(String title, String font_name) {
    titleLine.setText(title);
    if((font_name==null)||font_name.equals("")) { //$NON-NLS-1$
      // resize fonts in order to adjust gutters
      resizeFonts(FontSizer.getFactor(FontSizer.getLevel()), drawingPanel);
      return;
    }
    titleLine.setFont(Font.decode(font_name));
    setTitleFont(font_name);
    // resize fonts in order to adjust gutters
    resizeFonts(FontSizer.getFactor(FontSizer.getLevel()), drawingPanel);
  }

  /**
   *  Set the title font. The font names understood are those understood by
   *  java.awt.Font.decode().
   *
   * @param  name  A font name.
   */
  public void setTitleFont(String name) {
    if((name==null)||name.equals("")) { //$NON-NLS-1$
      return;
    }
    titleFont = Font.decode(name);
    titleLine.setFont(titleFont);
    // titleFontMetrics = getFontMetrics(titleFont); FIXME
  }

  /**
   * Set the label for the X (horizontal) axis.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  label the label
   * @param font_name an optional font name
   */
  public void setXLabel(String label, String font_name) {
    xLine.setText(label);
    if((font_name==null)||font_name.equals("")) { //$NON-NLS-1$
      return;
    }
    xLine.setFont(Font.decode(font_name));
    setLabelFont(font_name);
  }

  /**
   *  Specify whether the X axis is drawn with a logarithmic scale.
   *
   * @param  xlog  If true, logarithmic axis is used.
   */
  public void setXLog(boolean xlog) {
    this.xlog = xlog;
  }

  /**
   *  Set the label for the Y (vertical) axis.
   * The font names understood are those understood by java.awt.Font.decode().
   * If the font name is null, the font remains unchanged.
   *
   * @param  label the label
   * @param font_name an optional font name
   */
  public void setYLabel(String label, String font_name) {
    yLine.setText(label);
    if((font_name==null)||font_name.equals("")) { //$NON-NLS-1$
      return;
    }
    yLine.setFont(Font.decode(font_name));
    setLabelFont(font_name);
  }

  /**
   *  Specify whether the Y axis is drawn with a logarithmic scale.
   *
   * @param  ylog  If true, logarithmic axis is used.
   */
  public void setYLog(boolean ylog) {
    this.ylog = ylog;
  }

  /**
   *  Get the title of the graph, or an empty string if there is none.
   *
   * @return    The title.
   */
  public String getTitle() {
    return titleLine.getText();
  }

  /**
   *  Get the label for the X (horizontal) axis, or null if none has been set.
   *
   * @return    The X label.
   */
  public String getXLabel() {
    return xLine.getText();
  }

  /**
   *  Return whether the X axis is drawn with a logarithmic scale.
   *
   * @return    True if the X axis is logarithmic.
   */
  public boolean isXLog() {
    return xlog;
  }

  /**
   *  Get the label for the Y (vertical) axis, or null if none has been set.
   *
   * @return    The Y label.
   */
  public String getYLabel() {
    return yLine.getText();
  }

  /**
   *  Return whether the Y axis is drawn with a logarithmic scale.
   *
   * @return    True if the Y axis is logarithmic.
   */
  public boolean isYLog() {
    return ylog;
  }

  /**
   * Resizes fonts by the specified factor.
   *
   * @param factor the factor
   * @param panel the drawing panel on which these axes are drawn
   */
  public void resizeFonts(double factor, DrawingPanel panel) {
    super.resizeFonts(factor, panel);
    if(xLine==null) {
      return;
    }
    xLine.setFont(labelFont);
    yLine.setFont(labelFont);
    int left = (int) (defaultLeftGutter*factor);
    int bottom = (int) (defaultBottomGutter*factor);
    
    // the following 6 lines changed by Doug Brown to make space for titles
    int top = (int) (defaultTopGutter*(1+factor)/2);
    if (getTitle()!=null && !getTitle().equals("")) { //$NON-NLS-1$
	    top = (int) (defaultTopGutter*factor);    	
    }
    int right = (int) (defaultRightGutter*(1+factor)/2);
    panel.setPreferredGutters(left, top, right, bottom);
    
    //     int top = (int) (defaultTopGutter*factor);
    //     int right = (int) (defaultRightGutter*factor);
    //     panel.setGutters(left, top, right, bottom);
    measureFonts(panel);
  }

  /**
   *  Draws the axes onto the specified panel
   *
   * @param  panel
   * @param  graphics
   */
  protected void drawPlot(DrawingPanel panel, Graphics graphics) {
    Color foreground = panel.getForeground();
    int panelHeight = panel.getHeight();
    int panelWidth = panel.getWidth();
    Shape previousClip = graphics.getClip();
    Font previousFont = graphics.getFont();
    graphics.clipRect(0, 0, panelWidth, panelHeight);
    graphics.setFont(labelFont);
    graphics.setColor(foreground);
    int lrx = panelWidth-rightGutter;                   // lower right x
    int lry = panelHeight-bottomGutter;                 // lower right y
    // NOTE: We assume a one-line title.
    int titlefontheight = titleFontMetrics.getHeight(); // Vertical space for title, if appropriate
    int vSpaceForTitle = titlefontheight;               // CHANGED
    // Number of vertical tick marks depends on the height of the font for labeling ticks and the height of the window.
    int labelheight = labelFontMetrics.getHeight();
    int halflabelheight = labelheight/2;
    // NOTE: 5 pixel padding on bottom.
    int yStartPosition = panelHeight-5;                 // starting position for axes, for y axis top, for x axis right
    int xStartPosition = panelWidth-5-OSPLayout.macOffset;
    if(xlog) {
      xExponent = (int) Math.floor(xtickMin);
    }
    if((xExponent!=0)&&(xticks==null)) {
      String superscript = Integer.toString(xExponent);
      xStartPosition -= superscriptFontMetrics.stringWidth(superscript);
      graphics.setFont(superscriptFont);
      if(!xlog) {
        graphics.drawString(superscript, xStartPosition, yStartPosition-halflabelheight);
        xStartPosition -= labelFontMetrics.stringWidth("x 10");      //$NON-NLS-1$
        graphics.setFont(labelFont);
        graphics.drawString("x 10", xStartPosition, yStartPosition); //$NON-NLS-1$
      }
    }
    int height = panelHeight-topGutter-bottomGutter;
    // //////////////// vertical axis
    int numberYTickMarks = 2+height/(labelheight+10);
    numberYTickMarks = (int) (numberYTickMarks/panel.getImageRatio());
    // Compute y increment.
    double yTickSize = roundUp((ytickMax-ytickMin)/numberYTickMarks);
    // Compute y starting point so it is a multiple of yTickSize.
    double yStart = yTickSize*Math.ceil(ytickMin/yTickSize);
    int width = panelWidth-rightGutter-leftGutter; // drawable width
    // int width= Math.abs(panel.xToPix( panel.getXMax())-panel.xToPix( panel.getXMin()));
    if(interiorColor!=null) {
      graphics.setColor(interiorColor);
      graphics.fillRect(leftGutter, topGutter, width, height);
    }
    int xCoord1 = leftGutter+tickLength;
    int xCoord2 = lrx-tickLength;
    int numfracdigits = numFracDigits(yTickSize);
    int yTickWidth = 12;
    if(yticks==null) {
      // auto-ticks
      ArrayList<Double> ygrid = null;
      double yTmpStart = yStart;
      if(ylog) {
        ygrid = gridInit(yStart, yTickSize, true, null);
        yTmpStart = gridStep(ygrid, yStart, yTickSize, ylog);
      }
      // Set to false if we don't need the exponent
      boolean needExponent = ylog;
      boolean firstIteration = true;
      graphics.setColor(foreground);
      double chop = Math.abs(yTickSize/100);
      int counter = numberYTickMarks;
      for(double ypos = yTmpStart; ypos<=ytickMax; ypos = gridStep(ygrid, ypos, yTickSize, ylog)) {
        if(--counter<0) {
          break;
        }
        String yticklabel = null;
        if(ylog) {
          yticklabel = formatLogNum(ypos, numfracdigits);
          if(yticklabel.indexOf('e')!=-1) {
            needExponent = false;
          }
        } else {
          yticklabel = formatNum(ypos, numfracdigits, chop);
        }
        int yCoord1 = 0;
        if(ylog||(yExponent==0)) {
          yCoord1 = yToPix(ypos, panel);
        } else {
          yCoord1 = yToPix(ypos*Math.pow(10, yExponent), panel);
        }
        // The lowest label is shifted up slightly to avoid colliding with x labels.
        int offset = labelheight/4;                               // changed from zero by W. Christian
        if(firstIteration&&!ylog) {
          firstIteration = false;
          offset = 0;                                             // changed by W. Christian
          // offset = -labelheight / 8;
        }
        graphics.drawLine(leftGutter, yCoord1, xCoord1, yCoord1); // draw tick marks on both sides of plot, y tick marks are drawn horizontally
        // graphics.drawLine(lrx, yCoord1, xCoord2, yCoord1);
        graphics.drawLine(width+leftGutter-1, yCoord1, xCoord2, yCoord1);
        if(drawMajorYGrid&&(yCoord1>=topGutter)&&(yCoord1<=lry)) {                // draw grid line
          graphics.setColor(gridcolor);
          graphics.drawLine(xCoord1, yCoord1, xCoord2, yCoord1);
          graphics.setColor(foreground);
        }
        int labelWidth = labelFontMetrics.stringWidth(yticklabel);
        // NOTE: 4 pixel spacing between axis and labels.
        graphics.drawString(yticklabel, leftGutter-labelWidth-4, yCoord1+offset); // draw tick label
        int sw = labelFontMetrics.stringWidth(yticklabel);
        yTickWidth = Math.max(yTickWidth, sw);
      }
      if(ylog||drawMinorYGrid) {
        // Draw in grid lines that don't have labels.
        ArrayList<Double> unlabeledgrid = gridInit(yStart, yTickSize, false, ygrid);
        if(unlabeledgrid.size()>0) {
          // If the step is greater than 1, clamp it to 1 so that
          // we draw the unlabeled grid lines for each
          // integer interval.
          double tmpStep = (yTickSize>1.0) ? 1.0 : yTickSize;
          for(double ypos = gridStep(unlabeledgrid, yStart, tmpStep, ylog); ypos<=ytickMax; ypos = gridStep(unlabeledgrid, ypos, tmpStep, ylog)) {
            // int yCoord1 = yToPix(ypos * Math.pow(10, yExponent), panel);
            int yCoord1 = yToPix(ypos, panel);
            if((yCoord1!=topGutter)&&(yCoord1!=lry)) {
              graphics.setColor(gridcolor);
              graphics.drawLine(leftGutter+1, yCoord1, lrx-1, yCoord1);
              graphics.setColor(foreground);
            }
          }
        }
        if(needExponent) {
          yExponent = (int) Math.floor(yTmpStart);
        } else {
          yExponent = 0;
        }
      }
      // Draw scaling annotation for y axis.
      if(yExponent!=0) {
        graphics.drawString("x 10", 2, vSpaceForTitle);                                                                           //$NON-NLS-1$
        graphics.setFont(superscriptFont);
        graphics.drawString(Integer.toString(yExponent), labelFontMetrics.stringWidth("x 10")+2, vSpaceForTitle-halflabelheight); //$NON-NLS-1$
        graphics.setFont(labelFont);
      }
    } else {                                                       // ticks have been explicitly specified
      Iterator<Double> nt = yticks.iterator();
      for(Iterator<String> nl = yticklabels.iterator(); nl.hasNext(); ) {
        String label = nl.next();
        int sw = labelFontMetrics.stringWidth(label);
        yTickWidth = Math.max(yTickWidth, sw);
        double ypos = ((nt.next())).doubleValue();
        if((ypos>yMax)||(ypos<yMin)) {
          continue;
        }
        int yCoord1 = yToPix(ypos*Math.pow(10, yExponent), panel); // bottomGutterY - (int) ((ypos - yMin) * yscale);
        int offset = 0;
        if(ypos<lry-labelheight) {
          offset = halflabelheight;
        }
        graphics.drawLine(leftGutter, yCoord1, xCoord1, yCoord1);
        // graphics.drawLine(lrx, yCoord1, xCoord2, yCoord1);
        graphics.drawLine(width+leftGutter-1, yCoord1, xCoord2, yCoord1);
        if(drawMajorYGrid&&(yCoord1>=topGutter)&&(yCoord1<=lry)) {
          graphics.setColor(gridcolor);
          graphics.drawLine(xCoord1, yCoord1, xCoord2, yCoord1);
          graphics.setColor(foreground);
        }
        // NOTE: 3 pixel spacing between axis and labels.
        graphics.drawString(label, leftGutter-labelFontMetrics.stringWidth(label)-3, yCoord1+offset);
      }
    }
    // ////////////////// horizontal axis
    int yCoord1 = topGutter+tickLength;
    int yCoord2 = lry-tickLength;
    int charwidth = labelFontMetrics.stringWidth("8"); //$NON-NLS-1$
    if(xticks==null) { // auto-ticks
      // Number of x tick marks.
      // Need to start with a guess and converge on a solution here.
      int numberXTickMarks = 10;
      numberXTickMarks = (int) (numberXTickMarks/panel.getImageRatio());
      double xTickSize = 0.0;
      numfracdigits = 0;
      if(xlog) {
        // X axes log labels will be at most 6 chars: -1E-02
        numberXTickMarks = 4+width/((charwidth*6)+10);
        numberXTickMarks = (int) (numberXTickMarks/panel.getImageRatio());
      } else {
        // Limit to 10 iterations
        int count = 0;
        while(count++<=10) {
          xTickSize = roundUp((xtickMax-xtickMin)/numberXTickMarks);
          // Compute the width of a label for this xTickSize
          numfracdigits = numFracDigits(xTickSize);
          // Number of integer digits is the maximum of two endpoints
          int intdigits = numIntDigits(xtickMax);
          int inttemp = numIntDigits(xtickMin);
          if(intdigits<inttemp) {
            intdigits = inttemp;
          }
          // Allow two extra digits (decimal point and sign).
          int maxlabelwidth = charwidth*(numfracdigits+2+intdigits);
          // Compute new estimate of number of ticks.
          int savenx = numberXTickMarks;
          // NOTE: 10 additional pixels between labels.
          // NOTE: Try to ensure at least two tick marks.
          numberXTickMarks = 2+width/(maxlabelwidth+10);
          numberXTickMarks = (int) (numberXTickMarks/panel.getImageRatio());
          if((numberXTickMarks-savenx<=1)||(savenx-numberXTickMarks<=1)) {
            break;
          }
        }
      }
      xTickSize = (xlog) ? roundUp(0.8*(xtickMax-xtickMin)/numberXTickMarks) : roundUp((xtickMax-xtickMin)/numberXTickMarks);
      numfracdigits = numFracDigits(xTickSize);
      // Compute x starting point so it is a multiple of xTickSize.
      double xStart = xTickSize*Math.ceil(xtickMin/xTickSize);
      // NOTE: Following disables first tick.  Not a good idea?
      // if (xStart < xMin) xStart += xTickSize;
      ArrayList<Double> xgrid = null;
      double xTmpStart = xStart;
      if(xlog) {
        xStart = xTickSize*Math.floor(xtickMin/xTickSize);
        xgrid = gridInit(xStart, xTickSize, true, null);
        // xgrid = gridInit(xStart, xTickSize);
        xTmpStart = gridRoundUp(xgrid, xStart);
      }
      // Set to false if we don't need the exponent
      boolean needExponent = xlog;
      // Label the x axis.  The labels are quantized so that
      // they don't have excess resolution.
      graphics.setColor(foreground);
      double chop = Math.abs(yTickSize/100);
      int counter = numberXTickMarks;
      for(double xpos = xTmpStart; xpos<=xtickMax; xpos = gridStep(xgrid, xpos, xTickSize, xlog)) {
        if(--counter<0) {
          break;
        }
        String xticklabel = null;
        boolean hasExponent = false;
        if(xlog) {
          xticklabel = formatLogNum(xpos, numfracdigits);
          if(xticklabel.indexOf('e')!=-1) {
            needExponent = false;
            hasExponent = true;
          }
        } else {
          xticklabel = formatNum(xpos, numfracdigits, chop);
        }
        if(xlog||(xExponent==0)) {                                       // exponent is drawn if greater than 1
          xCoord1 = xToPix(xpos, panel);
        } else {
          xCoord1 = xToPix(xpos*Math.pow(10, xExponent), panel);
        }
        graphics.drawLine(xCoord1, topGutter, xCoord1, yCoord1);         // draw tick mark
        // graphics.drawLine(xCoord1, bottomGutterY, xCoord1, yCoord2);
        graphics.drawLine(xCoord1, height+topGutter-1, xCoord1, yCoord2);
        if(drawMajorXGrid&&(xCoord1>=leftGutter)&&(xCoord1<=lrx)) {      // draw grid line
          graphics.setColor(gridcolor);
          graphics.drawLine(xCoord1, yCoord1, xCoord1, yCoord2);
          graphics.setColor(foreground);
        }
        int labxpos = xCoord1-labelFontMetrics.stringWidth(xticklabel)/2;
        if(hasExponent) {
          graphics.drawString(xticklabel, labxpos+7, lry+3+labelheight); // draw tick label
        } else {
          // NOTE: 3 pixel spacing between axis and labels.
          graphics.drawString(xticklabel, labxpos, lry+3+labelheight);   // draw tick label
        }
      }
      if(xlog||drawMinorXGrid) {
        // Draw in grid lines that don't have labels.
        // If the step is greater than 1, clamp it to 1 so that
        // we draw the unlabeled grid lines for each
        // integer interval.
        double tmpStep = (xTickSize>1.0) ? 1.0 : xTickSize;
        // Recalculate the start using the new step.
        xTmpStart = tmpStep*Math.ceil(xtickMin/tmpStep);
        ArrayList<Double> unlabeledgrid = gridInit(xTmpStart, tmpStep, false, xgrid);
        if(unlabeledgrid.size()>0) {
          for(double xpos = gridStep(unlabeledgrid, xTmpStart, tmpStep, xlog); xpos<=xtickMax; xpos = gridStep(unlabeledgrid, xpos, tmpStep, xlog)) {
            // xCoord1 = panel.xToPix(xpos);
            xCoord1 = xToPix(xpos, panel);
            if((xCoord1!=leftGutter)&&(xCoord1!=lrx)) {                // draw grid line
              graphics.setColor(gridcolor);
              graphics.drawLine(xCoord1, topGutter+1, xCoord1, lry-1);
              graphics.setColor(foreground);
            }
          }
        }
        if(needExponent) {
          xExponent = (int) Math.floor(xTmpStart);
          graphics.setFont(superscriptFont);
          graphics.drawString(Integer.toString(xExponent), xStartPosition, yStartPosition-halflabelheight);
          xStartPosition -= labelFontMetrics.stringWidth("x 10");      //$NON-NLS-1$
          graphics.setFont(labelFont);
          graphics.drawString("x 10", xStartPosition, yStartPosition); //$NON-NLS-1$
        } else {
          xExponent = 0;
        }
      }
    } else {
      // ticks have been explicitly specified
      Iterator<Double> nt = xticks.iterator();
      Iterator<String> nl = xticklabels.iterator();
      // Code contributed by Jun Wu (jwu@inin.com.au)
      double preLength = 0.0;
      while(nl.hasNext()) {
        String label = nl.next();
        double xpos = ((nt.next())).doubleValue();
        // If xpos is out of range, ignore.
        if((xpos>xMax)||(xpos<xMin)) {
          continue;
        }
        // Find the center position of the label.
        xCoord1 = xToPix(xpos*Math.pow(10, xExponent), panel);         // leftGutter + (int) ((xpos - xMin) * xscale);
        // Find  the start position of x label.
        int labxpos = xCoord1-labelFontMetrics.stringWidth(label)/2;
        // If the labels are not overlapped, proceed.
        if(labxpos>preLength) {
          // calculate the length of the label
          preLength = xCoord1+labelFontMetrics.stringWidth(label)/2+10;
          // Draw the label.
          // NOTE: 3 pixel spacing between axis and labels.
          graphics.drawString(label, labxpos, lry+3+labelheight);
          // Draw the label mark on the axis
          graphics.drawLine(xCoord1, topGutter, xCoord1, yCoord1);
          // graphics.drawLine(xCoord1, bottomGutterY, xCoord1, yCoord2);
          graphics.drawLine(xCoord1, height+topGutter-1, xCoord1, yCoord2);
          // Draw the grid line
          if(drawMajorXGrid&&(xCoord1>=leftGutter)&&(xCoord1<=lrx)) {
            graphics.setColor(gridcolor);
            graphics.drawLine(xCoord1, yCoord1, xCoord1, yCoord2);
            graphics.setColor(foreground);
          }
        }
      }
    }
    // ////////////////// Draw title and axis labels now.
    // Center the title and X label over the plotting region, not
    // the window.
    graphics.setColor(foreground);
    if(titleLine!=null) {
      // titleLine.setX((panel.getXMax()+panel.getXMin())/2);
      // titleLine.setY(panel.getYMax()+5/panel.getYPixPerUnit());
      titleLine.setX(panel.getLeftGutter()/2+(panel.getWidth()-panel.getRightGutter())/2);
      if(panel.getTopGutter()>1.2*labelFontMetrics.getHeight()) {
        //titleLine.setY(panel.getTopGutter()/2+5);
        titleLine.setY(panel.getTopGutter()-0.6*labelFontMetrics.getHeight());
      } else {
        //titleLine.setY(25);
        titleLine.setY(panel.getTopGutter()+1.5*labelFontMetrics.getHeight());
      }
      titleLine.setColor(foreground);
      titleLine.draw(panel, graphics);
    }
    if(xLine!=null) {
      double mid = leftGutter/2.0+(panel.getWidth()-rightGutter)/2.0;
      xLine.setX(mid);
      int yoff = panel.getBottomGutter()-2*labelFontMetrics.getHeight();
      xLine.setY(panel.getHeight()-Math.max(0, yoff));
      //xLine.setY(panel.getHeight()-8);
      xLine.setColor(foreground);
      xLine.draw(panel, graphics);
    }
    if(yLine!=null) {
      double mid = topGutter/2.0+(panel.getHeight()-bottomGutter)/2;
      yLine.setY(mid);
      double x = panel.getLeftGutter()-yTickWidth-0.7*labelFontMetrics.getHeight();
      yLine.setX(Math.max(12, x));
      //yLine.setX(15);
      yLine.setColor(foreground);
      yLine.draw(panel, graphics);
    }
    graphics.setColor(foreground);
    graphics.drawRect(leftGutter, topGutter, width-1, height-1);
    graphics.setFont(previousFont);
    graphics.setClip(previousClip);
  }

  // /////////////////////////////////////////////////////////////////
  // //                         private methods                   ////
  /*
   *   Draw the legend in the upper right corner and return the width
   *   (in pixels)  used up.  The arguments give the upper right corner
   *   of the region where the legend should be placed.
   */
  /*
   *   private int drawLegend(Graphics graphics, int urx, int ury) {
   *   Ignore if there is no graphics object to draw on.
   *   if(graphics == null) {
   *   return 0;
   *   }
   *   FIXME: consolidate all these for efficiency
   *   Font previousFont = graphics.getFont();
   *   graphics.setFont(labelFont);
   *   int spacing = labelFontMetrics.getHeight();
   *   Iterator v = legendStrings.iterator();
   *   Iterator i = legendDatasets.iterator();
   *   int ypos = ury + spacing;
   *   int maxwidth = 0;
   *   while(v.hasNext()) {
   *   String legend = (String) v.next();
   *   NOTE: relies on legendDatasets having the same num. of entries.
   *   int dataset = ((Integer) i.next()).intValue();
   *   if(dataset >= 0) {
   *   if(usecolor) {
   *   Points are only distinguished up to the number of colors
   *   int color = dataset % colors.length;
   *   graphics.setColor(colors[color]);
   *   }
   *   graphics.setColor(foreground);
   *   int width = labelFontMetrics.stringWidth(legend);
   *   if(width > maxwidth) {
   *   maxwidth = width;
   *   }
   *   graphics.drawString(legend, urx - 15 - width, ypos);
   *   ypos += spacing;
   *   }
   *   }
   *   graphics.setFont(previousFont);
   *   return 22 + maxwidth;
   *   NOTE: subjective spacing parameter.
   *   }
   */

  /**
   *  Return the number as a String for use as a label on a logarithmic axis.
   *  Since this is a log plot, number passed in will not have too many digits to
   *  cause problems. If the number is an integer, then we print 1e<num>. If the
   *  number is not an integer, then print only the fractional components.
   *
   * @param  num            Description of the Parameter
   * @param  numfracdigits  Description of the Parameter
   * @return                Description of the Return Value
   */
  private String formatLogNum(double num, int numfracdigits) {
    String results;
    int exponent = (int) num;
    // Determine the exponent, prepending 0 or -0 if necessary.
    if((exponent>=0)&&(exponent<10)) {
      results = "0"+exponent;       //$NON-NLS-1$
    } else {
      if((exponent<0)&&(exponent>-10)) {
        results = "-0"+(-exponent); //$NON-NLS-1$
      } else {
        results = Integer.toString(exponent);
      }
    }
    // Handle the mantissa.
    if(num>=0.0) {
      if(num-(int) (num)<0.001) {
        results = "1e"+results; //$NON-NLS-1$
      } else {
        results = formatNum(Math.pow(10.0, (num-(int) num)), numfracdigits, Float.MIN_VALUE);
      }
    } else {
      if(-num-(int) (-num)<0.001) {
        results = "1e"+results; //$NON-NLS-1$
      } else {
        results = formatNum(Math.pow(10.0, (num-(int) num))*10, numfracdigits, Float.MIN_VALUE);
      }
    }
    return results;
  }

  /**
   *  Return a string for displaying the specified number using the specified
   *  number of digits after the decimal point. NOTE: java.text.NumberFormat in
   *  Netscape 4.61 has a bug where it fails to round numbers instead it
   *  truncates them. As a result, we don't use java.text.NumberFormat, instead
   *  We use the method from Ptplot1.3
   *
   * @param  num            Description of the Parameter
   * @param  numfracdigits  Description of the Parameter
   * @return                Description of the Return Value
   */
  private String formatNum(double num, int numfracdigits, double chop) {
    if(num==0) {
      return "0"; //$NON-NLS-1$
    }
    NumberFormat numberFormat;
    if((Math.abs(num)<0.01)&&(Math.abs(num)>chop)) {
      numberFormat = this.scientificFormat;
      // numberFormat.setMinimumFractionDigits(numfracdigits-4);
      // numberFormat.setMaximumFractionDigits(numfracdigits-4);
    } else {
      numberFormat = this.numberFormat;
      numberFormat.setMinimumFractionDigits(numfracdigits);
      numberFormat.setMaximumFractionDigits(numfracdigits);
    }
    return numberFormat.format(num);
  }

  /**
   *  Determine what values to use for log axes. Based on initGrid() from
   *  xgraph.c by David Harrison.
   *
   * @param  low      Description of the Parameter
   * @param  step     Description of the Parameter
   * @param  labeled  Description of the Parameter
   * @param  oldgrid  Description of the Parameter
   * @return          Description of the Return Value
   */
  private ArrayList<Double> gridInit(double low, double step, boolean labeled, ArrayList<Double> oldgrid) {
    // How log axes work:
    // gridInit() creates a vector with the values to use for the
    // log axes.  For example, the vector might contain
    // {0.0 0.301 0.698}, which could correspond to
    // axis labels {1 1.2 1.5 10 12 15 100 120 150}
    //
    // gridStep() gets the proper value.  gridInit is cycled through
    // for each integer log value.
    //
    // Bugs in log axes:
    // * Sometimes not enough grid lines are displayed because the
    // region is small.  This bug is present in the original xgraph
    // binary, which is the basis of this code.  The problem is that
    // as ratio gets closer to 1.0, we need to add more and more
    // grid marks.
    ArrayList<Double> grid = new ArrayList<Double>(10);
    // grid.add(new Double(0.0));
    double ratio = Math.pow(10.0, step);
    int ngrid = 1;
    if(labeled) {
      // Set up the number of grid lines that will be labeled
      if(ratio<=3.5) {
        if(ratio>2.0) {
          ngrid = 2;
        } else if(ratio>1.26) {
          ngrid = 5;
        } else if(ratio>1.125) {
          ngrid = 10;
        } else {
          ngrid = (int) Math.rint(1.0/step);
          ngrid = 10;
        }
      }
    } else {
      // Set up the number of grid lines that will not be labeled
      if(ratio>10.0) {
        ngrid = 1;
      } else if(ratio>3.0) {
        ngrid = 2;
      } else if(ratio>2.0) {
        ngrid = 5;
      } else if(ratio>1.125) {
        ngrid = 10;
      } else {
        ngrid = 100;
      }
      // Note: we should keep going here, but this increases the
      // size of the grid array and slows everything down.
    }
    int oldgridi = 0;
    for(int i = 0; i<ngrid; i++) {
      double gridval = i*1.0/ngrid*10;
      double logval = LOG10SCALE*Math.log(gridval);
      if(logval==Double.NEGATIVE_INFINITY) {
        logval = 0.0;
      }
      // If oldgrid is not null, then do not draw lines that
      // were already drawn in oldgrid.  This is necessary
      // so we avoid obliterating the tick marks on the plot borders.
      if((oldgrid!=null)&&(oldgridi<oldgrid.size())) {
        // Cycle through the oldgrid until we find an element
        // that is equal to or greater than the element we are
        // trying to add.
        while((oldgridi<oldgrid.size())&&(oldgrid.get(oldgridi)).doubleValue()<logval) {
          oldgridi++;
        }
        if(oldgridi<oldgrid.size()) {
          // Using == on doubles is bad if the numbers are close,
          // but not exactly equal.
          if(Math.abs((oldgrid.get(oldgridi)).doubleValue()-logval)>0.00001) {
            grid.add(new Double(logval));
          }
        } else {
          grid.add(new Double(logval));
        }
      } else {
        grid.add(new Double(logval));
      }
    }
    // gridCurJuke and gridBase are used in gridStep();
    gridCurJuke = 0;
    if(low==-0.0) {
      low = 0.0;
    }
    gridBase = Math.floor(low);
    double x = low-gridBase;
    // Set gridCurJuke so that the value in grid is greater than
    // or equal to x.  This sets us up to process the first point.
    for(gridCurJuke = -1; (gridCurJuke+1)<grid.size()&&(x>=(grid.get(gridCurJuke+1)).doubleValue()); gridCurJuke++) {}
    return grid;
  }

  /**
   *  Round pos up to the nearest value in the grid.
   *
   * @param  grid  Description of the Parameter
   * @param  pos   Description of the Parameter
   * @return       Description of the Return Value
   */
  private double gridRoundUp(ArrayList<Double> grid, double pos) {
    double x = pos-Math.floor(pos);
    int i;
    for(i = 0; (i<grid.size())&&(x>=(grid.get(i)).doubleValue()); i++) {}
    if(i>=grid.size()) {
      return pos;
    }
    return Math.floor(pos)+(grid.get(i)).doubleValue();
  }

  /**
   *  Used to find the next value for the axis label. For non-log axes, we just
   *  return pos + step. For log axes, we read the appropriate value in the grid
   *  ArrayList, add it to gridBase and return the sum. We also take care to
   *  reset gridCurJuke if necessary. Note that for log axes, gridInit() must be
   *  called before calling gridStep(). Based on stepGrid() from xgraph.c by
   *  David Harrison.
   *
   * @param  grid     Description of the Parameter
   * @param  pos      Description of the Parameter
   * @param  step     Description of the Parameter
   * @param  logflag  Description of the Parameter
   * @return          Description of the Return Value
   */
  private double gridStep(ArrayList<Double> grid, double pos, double step, boolean logflag) {
    // W. Christian
    if(step==0) {
      step = 1; // check for zero step size added to avoid of infinite loop
    }
    if(logflag) {
      if(++gridCurJuke>=grid.size()) {
        gridCurJuke = 0;
        gridBase += Math.ceil(step);
      }
      if(gridCurJuke>=grid.size()) {
        return pos+step;
      }
      return gridBase+(grid.get(gridCurJuke)).doubleValue();
    }
    if(pos+step==pos) { // step can be too small!
      while(pos+step==pos) {
        step *= 2;
      }
      return pos+step;
    }
    return pos+step;
  }

  /**
   *  Measure the various fonts. You only want to call this once.
   *
   * @param  panel  Description of the Parameter
   */
  private void measureFonts(JPanel panel) {
    labelFontMetrics = panel.getFontMetrics(labelFont);
    superscriptFontMetrics = panel.getFontMetrics(superscriptFont);
    titleFontMetrics = panel.getFontMetrics(titleFont);
  }

  /**
   *  Return the number of fractional digits required to display the given
   *  number. No number larger than 15 is returned (if more than 15 digits are
   *  required, 15 is returned).
   *
   * @param  num  Description of the Parameter
   * @return      Description of the Return Value
   */
  private int numFracDigits(double num) {
    int numdigits = 0;
    while((numdigits<=15)&&(num!=Math.floor(num))) {
      num *= 10.0;
      numdigits += 1;
    }
    return numdigits;
  }

  /**
   *  Return the number of integer digits required to display the given number.
   *  No number larger than 15 is returned (if more than 15 digits are required,
   *  15 is returned).
   *
   * @param  num  Description of the Parameter
   * @return      Description of the Return Value
   */
  private int numIntDigits(double num) {
    int numdigits = 0;
    while((numdigits<=15)&&(int) num!=0.0) {
      num /= 10.0;
      numdigits += 1;
    }
    return numdigits;
  }

  /**
   *  Given a number, round up to the nearest power of ten times 1, 2, or 5.
   *  Note: The argument must be strictly positive.
   *
   * @param  val  Description of the Parameter
   * @return      Description of the Return Value
   */
  private double roundUp(double val) {
    int exponent = (int) Math.floor(Math.log(val)*LOG10SCALE);
    val *= Math.pow(10, -exponent);
    if(val>5.0) {
      val = 10.0;
    } else if(val>2.0) {
      val = 5.0;
    } else if(val>1.0) {
      val = 2.0;
    } else {
      val = 1.0;
    }
    val *= Math.pow(10, exponent);
    return val;
  }

  /**
   *  Internal implementation of setXRange, so that it can be called when
   *  autoranging.
   *
   * @param  min  The new xRange value
   * @param  max  The new xRange value
   */
  private void setXRange(double min, double max) {
    // Find the exponent.
    double largest = Math.max(Math.abs(xMin), Math.abs(xMax));
    double range = Math.abs(xMax-xMin);
    if((xMin>=0)&&(xMax<=1000)&&(range>0.1)&&!xlog) {
      xExponent = 0;
    } else {
      xExponent = (int) Math.floor(Math.log(largest)*LOG10SCALE);
    }
    // Use the exponent only if it's larger than 1 in magnitude.
    if((xExponent>1)||(xExponent<-1)) {
      double xs = 1.0/Math.pow(10.0, xExponent);
      xtickMin = xMin*xs;
      xtickMax = xMax*xs;
    } else {
      xtickMin = xMin;
      xtickMax = xMax;
      xExponent = 0;
    }
  }

  /**
   *  Internal implementation of setYRange, so that it can be called when
   *  autoranging.
   *
   * @param  min  The new yRange value
   * @param  max  The new yRange value
   */
  private void setYRange(double min, double max) {
    // Find the exponent.
    double largest = Math.max(Math.abs(yMin), Math.abs(yMax));
    if((yMin>=0)&&(yMax<=1000)&&!ylog) {
      yExponent = 0;
    } else {
      yExponent = (int) Math.floor(Math.log(largest)*LOG10SCALE);
    }
    // Use the exponent only if it's larger than 1 in magnitude.
    if((yExponent>1)||(yExponent<-1)) {
      double ys = 1.0/Math.pow(10.0, yExponent);
      ytickMin = yMin*ys;
      ytickMax = yMax*ys;
    } else {
      ytickMin = yMin;
      ytickMax = yMax;
      yExponent = 0;
    }
  }

  /**
   * Shows a grid line for every x axis major tickmark.
   * Also disables minor grid if showGrid is false.
   *
   * @param  showGrid  The new drawMajorXGrid value
   */
  public void setShowMajorXGrid(boolean showGrid) {
    drawMajorXGrid = showGrid;
    if(!showGrid) {
      drawMinorXGrid = showGrid;
    }
  }

  /**
   *  Shows a grid line for every x axis minor tickmark.
   *
   * @param  showGrid  The new drawMinorXGrid value
   */
  public void setShowMinorXGrid(boolean showGrid) {
    drawMinorXGrid = showGrid;
  }

  /**
   * Shows a grid line for every y axis major tickmark.
   * Also disables minor grid if showGrid is false.
   *
   * @param  showGrid  The new drawMajorYGrid value
   */
  public void setShowMajorYGrid(boolean showGrid) {
    drawMajorYGrid = showGrid;
    if(!showGrid) {
      drawMinorYGrid = showGrid;
    }
  }

  /**
   * Shows a grid line for every y axis minor tickmark.
   *
   * @param  showGrid  The new drawMinorYGrid value
   */
  public void setShowMinorYGrid(boolean showGrid) {
    drawMinorYGrid = showGrid;
  }

  public void setX(double x) {}

  public void setY(double y) {}

  public double getX() {
    return 0;
  }

  public double getY() {
    return 0;
  }

  /**
   * Implements the Dimensioned interface.
   *
   * @param panel DrawingPanel
   * @return Dimension
   */
  public Dimension getInterior(DrawingPanel panel) {
    if(panel.getDimensionSetter()==null) {
      adjustGutters = true;
    } else {
      adjustGutters = false; // dimension setter object will set the gutters.
    }
    return null;
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
