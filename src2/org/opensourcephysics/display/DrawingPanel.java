/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.JTextComponent;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.axes.CoordinateStringBuilder;
import org.opensourcephysics.display.dialogs.DrawingPanelInspector;
import org.opensourcephysics.display.dialogs.ScaleInspector;
import org.opensourcephysics.display.dialogs.XMLDrawingPanelInspector;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ToolsRes;
import org.opensourcephysics.tools.VideoTool;

/**
 * DrawingPanel renders drawable objects on its canvas.
 * DrawingPanel provides drawable objects with methods that transform from world
 * coordinates to pixel coordinates. World coordinates are defined by xmin, xmax,
 * ymin, and ymax.  These values are recalculated on-the-fly from preferred
 * values if the aspect ratio is unity; otherwise, preferred values are used.
 *
 * If xmax>xmin then the coordinate scale increases from right to left.
 * If xmax<xmin then the coordinate scale increases from left to right.
 * If ymax>ymin then the coordinate scale increases from bottom to top.
 * If ymax<ymin then the coordinate scale increases from top to bottom.
 *
 * @author Wolfgang Christian
 * @author Joshua Gould
 * @version 1.0
 */
public class DrawingPanel extends JPanel implements ActionListener, Renderable {
  protected static final boolean RECORD_PAINT_TIMES = false;                                                     // set true to test painting time
  protected long currentTime = System.currentTimeMillis();

  /** Message box location */
  public static final int BOTTOM_LEFT = 0;

  /** Message box location */
  public static final int BOTTOM_RIGHT = 1;

  /** Message box location */
  public static final int TOP_RIGHT = 2;

  /** Message box location */
  public static final int TOP_LEFT = 3;
  protected JPopupMenu popupmenu = new JPopupMenu();                                                   // right mouse click popup menu
  protected JMenuItem propertiesItem, autoscaleItem, scaleItem, zoomInItem, zoomOutItem, snapshotItem; // the menu item for the properites dialog box
  protected int leftGutter = 0, topGutter = 0, rightGutter = 0, bottomGutter = 0;
  protected int leftGutterPreferred = 0, topGutterPreferred = 0, rightGutterPreferred = 0, bottomGutterPreferred = 0;
  protected boolean clipAtGutter = true;                                   // clips the drawing at the gutter if true
  protected boolean adjustableGutter = false;                              // adjust gutter depending on panel size
  protected int width, height;                                             // the size of the panel the last time it was painted.
  protected Color bgColor = new Color(239, 239, 255);                      // background color
  protected boolean antialiasTextOn = false;
  protected boolean antialiasShapeOn = false;
  protected boolean squareAspect = false;                                  // adjust xAspect and yAspect so the drawing aspect ratio is unity
  protected boolean autoscaleX = true;
  protected boolean autoscaleY = true;
  protected boolean autoscaleXMin = true, autoscaleXMax = true;
  protected boolean autoscaleYMin = true, autoscaleYMax = true;
  protected double autoscaleMargin = 0.0;                                  // used to increase the autoscale range
  // x and y scale in world units
  protected double xminPreferred = -10.0, xmaxPreferred = 10.0;
  protected double yminPreferred = -10.0, ymaxPreferred = 10.0;
  protected double xfloor = Double.NaN, xceil = Double.NaN;
  protected double yfloor = Double.NaN, yceil = Double.NaN;
  protected double xmin = xminPreferred, xmax = xmaxPreferred;
  protected double ymin = yminPreferred, ymax = xmaxPreferred;
  // pixel scale parameters  These are set every time paintComponent is called using the size of the panel
  protected boolean fixedPixelPerUnit = false;
  protected double xPixPerUnit = 1;                                        // the x scale in pixels per unit
  protected double yPixPerUnit = 1;                                        // the y scale in pixels per unit
  protected AffineTransform pixelTransform = new AffineTransform();        // transform from world to pixel coodinates.
  protected double[] pixelMatrix = new double[6];                          // 6 values in the 3x3 pixel transformation
  protected ArrayList<Drawable> drawableList = new ArrayList<Drawable>();  // list of Drawable objects
  private volatile boolean validImage = false;                             // true if the current image is valid, false otherwise
  protected BufferedImage offscreenImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
  protected BufferedImage workingImage = offscreenImage;
  private boolean buffered = false;                                        // true will draw this component using an off-screen image
  protected TextPanel trMessageBox = new TextPanel();                      // text box in top right hand corner for message
  protected TextPanel tlMessageBox = new TextPanel();                      // text box in top left hand corner for message
  protected TextPanel brMessageBox = new TextPanel();                      // text box in lower right hand corner for message
  protected TextPanel blMessageBox = new TextPanel();                      // text box in lower left hand corner for mouse coordinates
  protected DecimalFormat scientificFormat = new DecimalFormat("0.###E0"); // coordinate display format for message box. //$NON-NLS-1$
  protected DecimalFormat decimalFormat = new DecimalFormat("0.00"); // coordinate display format for message box. //$NON-NLS-1$
  protected MouseInputAdapter mouseController = new CMController();           // handles the coordinate display on mouse actions
  protected boolean showCoordinates = false;                                  // set to true when mouse listener is added
  protected MouseInputAdapter optionController = new OptionController();      // handles optional mouse actions
  protected ZoomBox zoomBox = new ZoomBox();
  protected boolean enableZoom = true;                                        // scale can be set via a mouse drag
  protected boolean fixedScale = false;                                       // scale is fixed (not user-settable)
  protected Window customInspector;                                           // optional custom inspector for this panel
  protected Dimensioned dimensionSetter = null;
  protected Rectangle viewRect = null;                                        // the clipping rectangle within a scroll pane viewport
  // CoordinateStringBuilder converts a mouse event into a string that displays world coordinates.
  protected CoordinateStringBuilder coordinateStrBuilder = CoordinateStringBuilder.createCartesian();
  protected GlassPanel glassPanel = new GlassPanel();
  protected OSPLayout glassPanelLayout = new OSPLayout();
  protected int refreshDelay = 100;                                                     // time in ms to delay refresh events
  protected javax.swing.Timer refreshTimer = new javax.swing.Timer(refreshDelay, this); // delay before for refreshing panel
  protected VideoTool vidCap;
  protected double imageRatio = 1.0;
  protected double xLeftMarginPercentage = 0.0, xRightMarginPercentage = 0.0;
  protected double yTopMarginPercentage = 0.0, yBottomMarginPercentage = 0.0;
  protected boolean logScaleX = false;                 // set true if the this axis uses a logarithmic scale
  protected boolean logScaleY = false;                 // set true if the this axis uses a logarithmic scale
  protected int zoomDelay = 40, zoomCount;
  protected javax.swing.Timer zoomTimer;
  protected double dxmin, dxmax, dymin, dymax;
  protected PropertyChangeListener guiChangeListener;

  /**
   * DrawingPanel constructor.
   */
  public DrawingPanel() {
    glassPanel.setLayout(glassPanelLayout);
    super.setLayout(new BorderLayout());
    glassPanel.add(trMessageBox, OSPLayout.TOP_RIGHT_CORNER);
    glassPanel.add(tlMessageBox, OSPLayout.TOP_LEFT_CORNER);
    glassPanel.add(brMessageBox, OSPLayout.BOTTOM_RIGHT_CORNER);
    glassPanel.add(blMessageBox, OSPLayout.BOTTOM_LEFT_CORNER);
    glassPanel.setOpaque(false);
    super.add(glassPanel, BorderLayout.CENTER);
    setBackground(bgColor);
    setPreferredSize(new Dimension(300, 300));
    showCoordinates = true; // show coordinates by default
    addMouseListener(mouseController);
    addMouseMotionListener(mouseController);
    addOptionController();
    // invalidate the buffered image if the size changes
    addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        invalidateImage(); // validImage = false;
      }

    });
    buildPopupmenu();
    refreshTimer.setRepeats(false);
    refreshTimer.setCoalesce(true);
    setFontLevel(FontSizer.getLevel());
    zoomTimer = new javax.swing.Timer(zoomDelay, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // reset and hide the zoom box
        zoomBox.xlast = zoomBox.xstop = zoomBox.xstart = 0;
        zoomBox.ylast = zoomBox.ystop = zoomBox.ystart = 0;
        zoomBox.visible = zoomBox.dragged = false;
        int steps = 4;
        if(zoomCount<steps) {
          zoomCount++;
          double xmin = getXMin()+dxmin/steps;
          double xmax = getXMax()+dxmax/steps;
          double ymin = getYMin()+dymin/steps;
          double ymax = getYMax()+dymax/steps;
          setPreferredMinMax(xmin, xmax, ymin, ymax);
          repaint(); // repaint the panel with the new scale
        } else {
          zoomTimer.stop();
          invalidateImage();
          repaint();
        }
      }

    });
    zoomTimer.setInitialDelay(0);
    // create guiChangeListener to change font size and refresh GUI
    // added by D Brown 29 mar 2016
    guiChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
      	if (e.getPropertyName().equals("level")) { //$NON-NLS-1$
	        int level = ((Integer) e.getNewValue()).intValue();
	        setFontLevel(level);
      	}
      	else if (e.getPropertyName().equals("locale")) { //$NON-NLS-1$
          // set the default decimal separator
      		Locale locale = (Locale)e.getNewValue();
      		DecimalFormat format = (DecimalFormat)NumberFormat.getInstance(locale);
          OSPRuntime.setDefaultDecimalSeparator(format.getDecimalFormatSymbols().getDecimalSeparator());
      		refreshDecimalSeparators();
      		refreshGUI();
      	}
      }
    };
    FontSizer.addPropertyChangeListener("level", guiChangeListener); //$NON-NLS-1$
    ToolsRes.addPropertyChangeListener("locale", guiChangeListener); //$NON-NLS-1$
  }

  /**
   * Refreshes the user interface in response to display changes such as Language.
   */
  protected void refreshGUI() {
    zoomInItem.setText(DisplayRes.getString("DisplayPanel.Zoom_in_menu_item"));    //$NON-NLS-1$
    zoomOutItem.setText(DisplayRes.getString("DisplayPanel.Zoom_out_menu_item"));  //$NON-NLS-1$
    scaleItem.setText(DisplayRes.getString("DrawingFrame.Scale_menu_item"));       //$NON-NLS-1$
    autoscaleItem.setText(DisplayRes.getString("DrawingFrame.Autoscale_menu_item")); //$NON-NLS-1$
    snapshotItem.setText(DisplayRes.getString("DisplayPanel.Snapshot_menu_item")); //$NON-NLS-1$
    propertiesItem.setText(DisplayRes.getString("DrawingFrame.InspectMenuItem"));  //$NON-NLS-1$
  }

  /**
   * Refreshes the decimal separators.
   */
  protected void refreshDecimalSeparators() {  
    scientificFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
    decimalFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
  }
  
  /**
   * Sets the font level.
   *
   * @param level the level
   */
  protected void setFontLevel(int level) {
    Font font = FontSizer.getResizedFont(trMessageBox.font, level);
    trMessageBox.font = font;
    tlMessageBox.font = font;
    brMessageBox.font = font;
    blMessageBox.font = font;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the font factor.
   *
   * @param factor the factor
   */
  public void setFontFactor(double factor) {
    Font font = FontSizer.getResizedFont(trMessageBox.font, factor);
    trMessageBox.font = font;
    tlMessageBox.font = font;
    brMessageBox.font = font;
    blMessageBox.font = font;
    invalidateImage(); // validImage = false;
    repaint();
  }
  
  /**
   * Builds the default popup menu for this panel.
   */
  protected void buildPopupmenu() {
    popupmenu.removeAll();
    popupmenu.setEnabled(true);
    ActionListener listener = new PopupmenuListener();
    if(isZoom()) {
      zoomInItem = new JMenuItem(DisplayRes.getString("DisplayPanel.Zoom_in_menu_item"));   //$NON-NLS-1$
      zoomInItem.addActionListener(listener);
      popupmenu.add(zoomInItem);
      zoomOutItem = new JMenuItem(DisplayRes.getString("DisplayPanel.Zoom_out_menu_item")); //$NON-NLS-1$
      zoomOutItem.addActionListener(listener);
      popupmenu.add(zoomOutItem);
    }
    if(!isFixedScale()) {
      autoscaleItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Autoscale_menu_item")); //$NON-NLS-1$
      autoscaleItem.addActionListener(listener);
      popupmenu.add(autoscaleItem);
      scaleItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Scale_menu_item"));         //$NON-NLS-1$
      scaleItem.addActionListener(listener);
      popupmenu.add(scaleItem);
      popupmenu.addSeparator();
    }
    snapshotItem = new JMenuItem(DisplayRes.getString("DisplayPanel.Snapshot_menu_item")); //$NON-NLS-1$
    snapshotItem.addActionListener(listener);
    popupmenu.add(snapshotItem);
    popupmenu.addSeparator();
    propertiesItem = new JMenuItem(DisplayRes.getString("DrawingFrame.InspectMenuItem")); //$NON-NLS-1$
    propertiesItem.addActionListener(listener);
    popupmenu.add(propertiesItem);
  }

  /**
   * Sets the size of the margin during an autoscale operation.
   *
   * @param _autoscaleMargin
   */
  public void setAutoscaleMargin(double _autoscaleMargin) {
    if(autoscaleMargin==_autoscaleMargin) {
      return;
    }
    autoscaleMargin = _autoscaleMargin;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the X left and right margins during an autoscale operation.
   *
   * @param _percentage
   */
  public void setXMarginPercentage(double _percentage) {
    if((xLeftMarginPercentage==_percentage)&&(xRightMarginPercentage==_percentage)) {
      return;
    }
    xLeftMarginPercentage = xRightMarginPercentage = _percentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the X left and right margins during an autoscale operation.
   *
   * @param _percentage
   */
  public void setXMarginPercentage(double _leftPercentage, double _rightPercentage) {
    if((xLeftMarginPercentage==_leftPercentage)&&(xRightMarginPercentage==_rightPercentage)) {
      return;
    }
    xLeftMarginPercentage = _leftPercentage;
    xRightMarginPercentage = _rightPercentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the X left margin during an autoscale operation.
   *
   * @param _percentage
   */
  public void setXLeftMarginPercentage(double _percentage) {
    if(xLeftMarginPercentage==_percentage) {
      return;
    }
    xLeftMarginPercentage = _percentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the X left margin during an autoscale operation.
   *
   * @param _percentage
   */
  public void setXRightMarginPercentage(double _percentage) {
    if(xRightMarginPercentage==_percentage) {
      return;
    }
    xRightMarginPercentage = _percentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the Y top and bottom margin during an autoscale operation.
   *
   * @param _percentage
   */
  public void setYMarginPercentage(double _percentage) {
    if((yTopMarginPercentage==_percentage)&&(yBottomMarginPercentage==_percentage)) {
      return;
    }
    yTopMarginPercentage = yBottomMarginPercentage = _percentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the Y top and bottom margin during an autoscale operation.
   *
   * @param _percentage
   */
  public void setYMarginPercentage(double _bottomPercentage, double _topPercentage) {
    if((yBottomMarginPercentage==_bottomPercentage)&&(yTopMarginPercentage==_topPercentage)) {
      return;
    }
    yTopMarginPercentage = _topPercentage;
    yBottomMarginPercentage = _bottomPercentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the Y top margin during an autoscale operation.
   *
   * @param _percentage
   */
  public void setYTopMarginPercentage(double _percentage) {
    if(yTopMarginPercentage==_percentage) {
      return;
    }
    yTopMarginPercentage = _percentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the extra percentage on the X left margin during an autoscale operation.
   *
   * @param _percentage
   */
  public void setYBottomMarginPercentage(double _percentage) {
    if(yBottomMarginPercentage==_percentage) {
      return;
    }
    yBottomMarginPercentage = _percentage;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the panel to exclude the gutter from the drawing.
   *
   * @param clip <code>true<\code> to clip; <code>false<\code> otherwise
   */
  public void setClipAtGutter(boolean clip) {
    if(clipAtGutter==clip) {
      return;
    }
    clipAtGutter = clip;
    invalidateImage(); // validImage = false;
  }

  /**
   * Gets the clip at gutter flag.
   *
   * @return  <code>true<\code> if drawing is clipped at the gutter; <code>false<\code> otherwise
   */
  public boolean isClipAtGutter() {
    return clipAtGutter;
  }

  /**
   * Sets adjustable gutters.  Axes are allowed to adjust the gutter size.
   *
   * @param fixed <code>true<\code> if gutters remain constant
   */
  public void setAdjustableGutter(boolean adjustable) {
    if(adjustableGutter==adjustable) {
      return;
    }
    adjustableGutter = adjustable;
    invalidateImage(); // validImage = false;
  }

  /**
   * Gets the adjustableGutter flag.  Adjustable gutters change as the panel is resized.
   *
   * @return  <code>true<\code> if gutters are adjustable
   */
  public boolean isAdjustableGutter() {
    return adjustableGutter;
  }

  /**
   * Sets the mouse cursor.
   * @param cursor
   */
  public void setMouseCursor(Cursor cursor) {
    Container c = getTopLevelAncestor();
    setCursor(cursor);
    if(c!=null) {
      c.setCursor(cursor);
    }
  }

  /**
   * Checks the image to see if the working image has the correct Dimension.
   *
   * Checking is done in the event dispatch thread.
   *
   * @return <code>true <\code> if the offscreen image matches the panel;  <code>false <\code> otherwise
   */
  protected boolean checkWorkingImage() {
    Runnable runImageCheck = new Runnable() {
      public void run() {
        workingImage();
      }

    };
    if(SwingUtilities.isEventDispatchThread()) {
      return workingImage();
    }
    try {
      SwingUtilities.invokeAndWait(runImageCheck);
      return true;
    } catch(Exception ex) {
      OSPLog.finest("Exception in Check Working Image:"+ex.toString()); //$NON-NLS-1$
      return false;
    }
  }

  /**
   * Checks the image to see if the working image has the correct Dimension.
   *
   * @return <code>true <\code> if the offscreen image matches the panel;  <code>false <\code> otherwise
   */
  private boolean workingImage() {
    Rectangle r = getBounds();
    int width = (int) r.getWidth();
    int height = (int) r.getHeight();
    if((width<=2)||(height<=2)) {
      return false; // panel is too small to draw anything useful
    }
    if((workingImage==null)||(width!=workingImage.getWidth())||(height!=workingImage.getHeight())) {
      this.workingImage = getGraphicsConfiguration().createCompatibleImage(width, height);
      invalidateImage(); // validImage = false; // buffer image is not valid
    }
    if(this.workingImage==null) { // image could not be created
      invalidateImage();          // validImage = false;        // buffer image is not valid
      return false;
    }
    return true; // the buffered image has been created and is the correct size
  }

  /**
   *  Performs the action for the refresh timer by rendering (redrawing) the panel.
   *
   * @param  evt
   */
  public void actionPerformed(ActionEvent evt) {
    if(!isValidImage()) {
      render();
    }
  }

  /**
   * Gets the iconified flag from the top level frame.
   *
   * @return boolean true if frame is iconified; false otherwise
   */
  public boolean isIconified() {
    Component c = getTopLevelAncestor();
    if(c instanceof Frame) {
      return(((Frame) c).getExtendedState()&Frame.ICONIFIED)==1;
    }
    return false;
  }

  /**
   * Paints all drawables onto an offscreen image buffer and copies this image onto the screen.
   *
   * @return the image buffer
   */
  public BufferedImage render() {
    if(!isShowing()||isIconified()) {
      return offscreenImage; // no need to draw if the frame is not visible
    }
    if(buffered&&checkWorkingImage()) {
      validImage = true; // drawing into the working image will produce a valid image
      render(workingImage);
      // swap the images
      BufferedImage temp = offscreenImage;
      offscreenImage = workingImage;
      workingImage = temp;
    }
    // always update a Swing component from the event thread
    Runnable doNow = new Runnable() {
      public void run() {
        paintImmediately(getVisibleRect());
      }

    };
    try {
      if(SwingUtilities.isEventDispatchThread()) {
        paintImmediately(getVisibleRect());
      } else { // paint within the event thread
        SwingUtilities.invokeAndWait(doNow);
      }
    } catch(InvocationTargetException ex1) {}
    catch(InterruptedException ex1) {}
    if(vidCap!=null) {
      if(buffered) { // buffered image exists so use it.
        vidCap.addFrame(offscreenImage);
      } else {       // render the image if the buffer does not exist
        // inefficient as the image may be rendered twice during every animation step
        if(vidCap.isRecording()) {
          vidCap.addFrame(render());
        }
      }
    }
    return offscreenImage;
  }

  /**
   * Paints all drawables onto an image.
   *
   * @param image
   * @return the image buffer
   */
  public BufferedImage render(BufferedImage image) {
    Graphics osg = image.getGraphics();
    imageRatio =((float) getWidth()<=0)?1: image.getWidth()/(float) getWidth();  // ratio of image to panel width
    if(osg!=null) {
      paintEverything(osg);
      if(image==workingImage) {
        zoomBox.paint(osg);               // paint the zoom
      }
      Rectangle viewRect = this.viewRect; // reference for thread safety
      if(viewRect!=null) {
        Rectangle r = new Rectangle(0, 0, image.getWidth(null), image.getHeight(null));
        glassPanel.setBounds(r);
        glassPanelLayout.checkLayoutRect(glassPanel, r);
        glassPanel.render(osg);
        glassPanel.setBounds(viewRect);
        glassPanelLayout.checkLayoutRect(glassPanel, viewRect);
      } else {
        glassPanel.render(osg);
      }
      osg.dispose();
    }
    imageRatio = 1.00;
    return image;
  }

  public int getWidth() {
    return(int) (imageRatio*super.getWidth());  // effective width when rendering images
  }

  public int getHeight() {
    return(int) (imageRatio*super.getHeight());  // effective height when rendering images
  }

  /**
   * Gets the ratio of the drawing image to the panel.
   * @return double
   */
  public double getImageRatio() {
    return imageRatio;
  }

  /**
   * Invalidate the offscreen image so that it is rendered during the next repaint operation if buffering is enabled.
   */
  public void invalidateImage() {
    validImage = false;
  }
  
  /**
   * Validate the offscreen image to insure that the render method will execute.
   */
  public void validateImage() {
    validImage = true;
  }

  protected boolean isValidImage() {
    return validImage;
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    boolean resetBuffered = buffered;
    /**
    if(!OSPRuntime.isJS && g2.getDeviceConfiguration().getDevice().getType()==GraphicsDevice.TYPE_PRINTER) {
      buffered = false;
      //System.out.println("buffer off");
    } **/
    super.paint(g);
    paintEverything(g);
    buffered = resetBuffered;
  }

  public void paintComponentTEST(Graphics g) {
	  paintEverything(g);
  }
  
  /**
   * Paints this component.
   * @param g
   */
  public void paintComponent(Graphics g) {
    if(OSPRuntime.disableAllDrawing) {
      g.setColor(bgColor);
      g.setColor(Color.RED);
      g.fillRect(0, 0, getWidth(), getHeight());
      return;
    }
    viewRect = findViewRect(); // find the clipping rectangle within a scroll pane viewport
    if(buffered) {                                 // paint bufferImage onto screen
      if(!validImage||(getWidth()!=offscreenImage.getWidth())||(getHeight()!=offscreenImage.getHeight())) {
        if((getWidth()!=offscreenImage.getWidth())||(getHeight()!=offscreenImage.getHeight())) {
          g.setColor(Color.WHITE);
          g.setColor(Color.CYAN);
          g.fillRect(0, 0, getWidth(), getHeight());
        } else {
          g.drawImage(offscreenImage, 0, 0, null); // copy old image to the screen for now
        }
        refreshTimer.start();                      // image is not valid so start refresh timer
      } else {                                     // current image is valid and has correct size
        g.drawImage(offscreenImage, 0, 0, null);   // copy image to the screen
      }
    } else {                                       // paint directly onto the graphics buffer
      validImage = true;                           // painting everything gives a valid onscreen image
      paintEverything(g);
    }
    zoomBox.paint(g);
    //    if(enableZoom||zoomMode) { // zoom box is always painted on top
    //      zoomBox.paint(g);
    //    }
  }

  /**
   * Gets the clipping rectangle within a scroll pane viewport.
   *
   * @return the clipping rectangle
   */
  protected Rectangle getViewRect() {
    return viewRect;
  }

  /**
   * Finds the clipping rectangle if this panel is within a scroll pane viewport.
   */
  protected Rectangle findViewRect() {
    Rectangle rect = null;
    Container c = getParent();
    while(c!=null) {
      if(c instanceof JViewport) {
        rect = ((JViewport) c).getViewRect();
        glassPanel.setBounds(rect);
        glassPanelLayout.checkLayoutRect(glassPanel, rect);
        break;
      }
      c = c.getParent();
    }
    return rect;
  }

  /**
   * Computes the size of the gutters. Objects, such as axes, can perform this method
   * by implementing the Dimensioned interface.
   */
  protected void computeGutters() {
    if(dimensionSetter!=null) {
      Dimension interiorDimension = dimensionSetter.getInterior(this);
      if(interiorDimension!=null) {
        squareAspect = false;
        leftGutter = rightGutter = Math.max(0, getWidth()-interiorDimension.width)/2;
        topGutter = bottomGutter = Math.max(0, getHeight()-interiorDimension.height)/2;
      }
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
  }

  /**
   * Paints after the panel iterates through its list of Drawables.
   * @param g Graphics
   */
  protected void paintLast(Graphics g) {}

  /**
   * Paints everything inside this component.
   *
   * @param g
   */
  protected void paintEverything(Graphics g) {
    if(RECORD_PAINT_TIMES) {
      long time = System.currentTimeMillis();
      System.out.println("elapsed time(s)="+((int) (time-currentTime)/1000.0)); //$NON-NLS-1$
      currentTime = time;
    }
    // the following statement has been moved to paintComponent
    // viewRect = findViewRect(); // finds the clipping rectangle within a scroll pane viewport
    computeGutters(); // last chance to set the gutters
    ArrayList<Drawable> tempList = getDrawables(); // holds a clone of the drawable object list
    scale(tempList); // sets the world-coordinate scale based on the autoscale values
    setPixelScale(); // sets the pixel scale and the world-to-pixel affine transformation matrix
	if (!OSPRuntime.isMac()) {  //Rendering hint bug in Mac Snow Leopard 
		if (antialiasTextOn) {
			((Graphics2D) g).setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		} else {
			((Graphics2D) g).setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
		if (antialiasShapeOn) {
			((Graphics2D) g).setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			((Graphics2D) g).setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_OFF);
		}
	}
    // ready to draw everything
    if(!validImage) {
      return; // abort drawing
    }
    paintFirst(g); // PlottingPanel uses this method to paint axes
    if(!validImage) {
      return; // abort drawing
    }
    paintDrawableList(g, tempList);
    if(!validImage) {
      return; // abort drawing
    }
    paintLast(g); // does nothing yet but can be used to add a legend, etc
    if(RECORD_PAINT_TIMES) {
      System.out.println("paint time (ms)="+(int) (System.currentTimeMillis()-currentTime)+'\n'); //$NON-NLS-1$
    }
  }

  /**
   * Autoscale the x axis using min and max values.
   * from measurable objects.
   * @param autoscale
   */
  public void setAutoscaleX(boolean autoscale) {
    if((autoscaleX==autoscale)&&(autoscaleXMax==autoscale)&&(autoscaleXMin==autoscale)) {
      return;
    }
    autoscaleX = autoscaleXMax = autoscaleXMin = autoscale;
    invalidateImage(); // validImage = false;
  }

  /**
   * Determines if the x axis autoscale property is true.
   * @return <code>true<\code> if autoscaled.
   */
  public boolean isAutoscaleX() {
    return autoscaleX;
  }

  /**
   * Determines if the horizontal maximum value is autoscaled.
   * @return <code>true<\code> if xmax is autoscaled.
   */
  public boolean isAutoscaleXMax() {
    return autoscaleXMax;
  }

  /**
   * Determines if the horizontal minimum value is autoscaled.
   * @return <code>true<\code> if xmin is autoscaled.
   */
  public boolean isAutoscaleXMin() {
    return autoscaleXMin;
  }

  /**
   * Autoscale the y axis using min and max values.
   * from measurable objects.
   * @param autoscale
   */
  public void setAutoscaleY(boolean autoscale) {
    if((autoscaleY==autoscale)&&(autoscaleYMax==autoscale)&&(autoscaleYMin==autoscale)) {
      return;
    }
    autoscaleY = autoscaleYMax = autoscaleYMin = autoscale;
    invalidateImage(); // validImage = false;
  }

  /**
   * Determines if the y axis autoscale property is true.
   * @return <code>true<\code> if autoscaled.
   */
  public boolean isAutoscaleY() {
    return autoscaleY;
  }

  /**
   * Determines if the vertical maximum value is autoscaled.
   * @return <code>true<\code> if ymax is autoscaled.
   */
  public boolean isAutoscaleYMax() {
    return autoscaleYMax;
  }

  /**
   * Determines if the vertical minimum value is autoscaled.
   * @return <code>true<\code> if ymin is autoscaled.
   */
  public boolean isAutoscaleYMin() {
    return autoscaleYMin;
  }

  /**
   * Gets the logScaleX value.
   *
   * @return boolean
   */
  public boolean isLogScaleX() {
    return logScaleX;
  }

  /**
   * Gets the logScaleY value.
   *
   * @return boolean
   */
  public boolean isLogScaleY() {
    return logScaleY;
  }

  /**
   * Moves and resizes this component. The new location of the top-left
   * corner is specified by <code>x</code> and <code>y</code>, and the
   * new size is specified by <code>width</code> and <code>height</code>.
   * @param x The new <i>x</i>-coordinate of this component.
   * @param y The new <i>y</i>-coordinate of this component.
   * @param width The new <code>width</code> of this component.
   * @param height The new <code>height</code> of this
   * component.
   */
  public void setBounds(int x, int y, int width, int height) {
    if((getBounds().x==x)&&(getBounds().y==y)&&(getBounds().width==width)&&(getBounds().height==height)) {
      return;
    }
    super.setBounds(x, y, width, height);
    invalidateImage(); // validImage = false;
  }

  public void setBounds(Rectangle r) {
    if(getBounds().equals(r)) {
      return;
    }
    super.setBounds(r);
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the buffered image option.
   *
   * Buffered panels copy the offscreen image into the panel during a repaint unless the image
   * has been invalidated.  Use the render() method to draw the image immediately.
   *
   * @param _buffered
   */
  public void setBuffered(boolean _buffered) {
    if(buffered==_buffered) {
      return;
    }
    buffered = _buffered;
    if(buffered) {             // turn off Java buffering because we are doing our own
      setDoubleBuffered(false);
    } else {                   // small default image is not used
      workingImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
      offscreenImage = workingImage;
      setDoubleBuffered(true); // use Java's buffer
    }
    invalidateImage(); // validImage = false;
  }

  public boolean isBuffered() {
    return buffered;
  }

  /**
   * Makes the component visible or invisible.
   * Overrides <code>JComponent.setVisible</code>.
   *
   * @param vis  true to make the component visible; false to
   *            make it invisible
   */
  public void setVisible(boolean vis) {
    if(this.isVisible()==vis) {
      return;
    }
    super.setVisible(vis);
    invalidateImage(); // validImage = false;
  }

  /**
   * Limits the xmin and xmax values during autoscaling so that the mininimum value
   * will be no greater than the floor and the maximum value will be no
   * smaller than the ceil.
   *
   * Setting a floor or ceil value to <code>Double.NaN<\code> will disable that limit.
   *
   * @param floor the xfloor value
   * @param ceil the xceil value
   */
  public void limitAutoscaleX(double floor, double ceil) {
    if(ceil-floor<Float.MIN_VALUE) { // insures that floor and ceiling some separation
      floor = 0.9*floor-Float.MIN_VALUE;
      ceil = 1.1*ceil+Float.MIN_VALUE;
    }
    xfloor = floor;
    xceil = ceil;
  }

  /**
   * Limits ymin and ymax values during autoscaling so that the mininimum value
   * will be no greater than the floor and the maximum value will be no
   * smaller than the ceil.
   *
   * Setting a floor or ceil value to <code>Double.NaN<\code> will disable that limit.
   *
   * @param floor the yfloor value
   * @param ceil the yceil value
   */
  public void limitAutoscaleY(double floor, double ceil) {
    if(ceil-floor<Float.MIN_VALUE) { // insures that floor and ceiling some separation
      floor = 0.9*floor-Float.MIN_VALUE;
      ceil = 1.1*ceil+Float.MIN_VALUE;
    }
    yfloor = floor;
    yceil = ceil;
  }

  /**
   * Sets the scale using pixels per unit.
   *
   * @param enable boolean enable fixed pixels per unit
   * @param xPixPerUnit double
   * @param yPixPerUnit double
   */
  public void setPixelsPerUnit(boolean enable, double xPixPerUnit, double yPixPerUnit) {
    if((fixedPixelPerUnit==enable)&&(this.xPixPerUnit==xPixPerUnit)&&(this.yPixPerUnit==yPixPerUnit)) {
      return;
    }
    fixedPixelPerUnit = enable;
    this.xPixPerUnit = xPixPerUnit;
    this.yPixPerUnit = yPixPerUnit;
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the preferred scale in the vertical and horizontal direction.
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   * @param invalidateImage invalidates image if min/max have changed
   */
  public void setPreferredMinMax(double xmin, double xmax, double ymin, double ymax, boolean invalidateImage) {
    autoscaleX = autoscaleXMin = autoscaleXMax = false;
    autoscaleY = autoscaleYMin = autoscaleYMax = false;
    if((xminPreferred==xmin)&&(xmaxPreferred==xmax)&&(yminPreferred==ymin)&&(ymaxPreferred==ymax)) {
      return;
    }
    if(Double.isNaN(xmin)) {
      autoscaleXMin = true;
      xmin = xminPreferred;
    }
    if(Double.isNaN(xmax)) {
      autoscaleXMax = true;
      xmax = xmaxPreferred;
    }
    autoscaleX = autoscaleXMin||autoscaleXMax;
    if(xmin==xmax) {
      xmin = 0.9*xmin-0.5;
      xmax = 1.1*xmax+0.5;
    }
    xminPreferred = xmin;
    xmaxPreferred = xmax;
    if(Double.isNaN(ymin)) {
      autoscaleYMin = true;
      ymin = yminPreferred;
    }
    if(Double.isNaN(ymax)) {
      autoscaleYMax = true;
      ymax = ymaxPreferred;
    }
    autoscaleY = autoscaleYMin||autoscaleYMax;
    if(ymin==ymax) {
      ymin = 0.9*ymin-0.5;
      ymax = 1.1*ymax+0.5;
    }
    yminPreferred = ymin;
    ymaxPreferred = ymax;
    if(invalidateImage) {
      invalidateImage();
    }
  }

  /**
   * Sets the preferred scale in the vertical and horizontal direction.
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   */
  public void setPreferredMinMax(double xmin, double xmax, double ymin, double ymax) {
    setPreferredMinMax(xmin, xmax, ymin, ymax, false);
  }

  /**
   * Sets the preferred scale in the horizontal direction.
   * @param xmin the minimum value
   * @param xmax the maximum value
   */
  public void setPreferredMinMaxX(double xmin, double xmax) {
    autoscaleX = autoscaleXMin = autoscaleXMax = false;
    if((xminPreferred==xmin)&&(xmaxPreferred==xmax)) {
      return;
    }
    if(Double.isNaN(xmin)) {
      autoscaleXMin = true;
      xmin = xminPreferred;
    }
    if(Double.isNaN(xmax)) {
      autoscaleXMax = true;
      xmax = xmaxPreferred;
    }
    autoscaleX = autoscaleXMin||autoscaleXMax;
    if(xmin==xmax) {
      xmin = 0.9*xmin-0.5;
      xmax = 1.1*xmax+0.5;
    }
    xminPreferred = xmin;
    xmaxPreferred = xmax;
    invalidateImage();
  }

  /**
   * Sets the preferred scale in the vertical direction.
   * @param ymin
   * @param ymax
   */
  public void setPreferredMinMaxY(double ymin, double ymax) {
    autoscaleY = autoscaleYMin = autoscaleYMax = false;
    if((yminPreferred==ymin)&&(ymaxPreferred==ymax)) {
      return;
    }
    if(Double.isNaN(ymin)) {
      autoscaleYMin = true;
      ymin = yminPreferred;
    }
    if(Double.isNaN(ymax)) {
      autoscaleYMax = true;
      ymax = ymaxPreferred;
    }
    autoscaleY = autoscaleYMin||autoscaleYMax;
    if(ymin==ymax) {
      ymin = 0.9*ymin-0.5;
      ymax = 1.1*ymax+0.5;
    }
    yminPreferred = ymin;
    ymaxPreferred = ymax;
    invalidateImage();
  }

  /**
   * Sets the aspect ratio for horizontal to vertical to unity when <code>true<\code>.
   * @param val
   */
  public void setSquareAspect(boolean val) {
    if(squareAspect==val) {
      return;
    }
    squareAspect = val;
    invalidateImage(); // validImage = false;
    repaint();
  }

  /**
   * Determines if the number of pixels per unit is the same for both x and y.
   * @return <code>true<\code> if squareAspect
   */
  public boolean isSquareAspect() {
    return squareAspect;
  }

  /**
   * Set flag for text antialiasing.
   */
  public void setAntialiasTextOn(boolean on) {
    antialiasTextOn = on;
  }

  /**
   * Gets flag for text antialiasing.
   */
  public boolean isAntialiasTextOn() {
    return antialiasTextOn;
  }

  /**
   * Set flag for shape antialiasing.
   */
  public void setAntialiasShapeOn(boolean on) {
    antialiasShapeOn = on;
  }

  /**
   * Gets flag for shape antialiasing.
   */
  public boolean isAntialiasShapeOn() {
    return antialiasShapeOn;
  }

  /**
   * Determines if the x and y point is inside.
   *
   * @param x the coordinate in world units
   * @param y the coordinate in world units
   *
   * @return <code>true<\code> if point is inside; <code>false<\code> otherwise
   */
  public boolean isPointInside(double x, double y) {
    if(xmin<xmax) {
      if(x<xmin) {
        return false;
      }
      if(x>xmax) {
        return false;
      }
    } else { // max is less than min so scale decreases to the right
      if(x>xmin) {
        return false;
      }
      if(x<xmax) {
        return false;
      }
    }
    if(ymin<ymax) {
      if(y<ymin) {
        return false;
      }
      if(y>ymax) {
        return false;
      }
    } else { // max is less than min so scale decreases to the right
      if(y>ymin) {
        return false;
      }
      if(y<ymax) {
        return false;
      }
    }
    return true;
  }

  /**
   * Determines if the scale is fixed.
   * @return <code>true<\code> if scale is fixed
   */
  public boolean isFixedScale() {
    return fixedScale;
  }

  /**
   * Sets the fixed scale property. If fixed, the user cannot change the scale.
   * @param fixed <code>true<\code> to prevent user changes to scale
   */
  public void setFixedScale(boolean fixed) {
    if(fixedScale==fixed) {
      return;
    }
    fixedScale = fixed;
    buildPopupmenu();
  }

  /**
   * Determines if the user can change scale by dragging the mouse.
   * @return <code>true<\code> if zoom is enabled and scale is not fixed
   */
  public boolean isZoom() {
    return enableZoom&&!isFixedScale();
  }

  /**
   * Sets the zoom option to allow the user to change scale by dragging the mouse.
   * @param _enableZoom <code>true<\code> if zoom is enabled
   */
  public void setZoom(boolean _enableZoom) {
    if(enableZoom==_enableZoom) {
      return;
    }
    enableZoom = _enableZoom;
    buildPopupmenu();
  }

  /**
   * Zooms out by a factor of two.
   */
  protected void zoomOut() {
    // find center of zoom box
    int xPix = (zoomBox.xstart+zoomBox.xstop)/2;
    int yPix = (zoomBox.ystart+zoomBox.ystop)/2;
    double xCenter = pixToX(xPix);
    double yCenter = pixToY(yPix);
    // determine distances from center to edges 
    double dx = Math.abs(xmax-xmin);
    double dy = Math.abs(ymax-ymin);
    // set up zoomTimer parameters and start
    dxmin = xCenter-dx-getXMin();
    dxmax = xCenter+dx-getXMax();
    dymin = yCenter-dy-getYMin();
    dymax = yCenter+dy-getYMax();
    zoomCount = 0;
    zoomTimer.start();
  }

  /**
   * Returns the internal ZoomBox object
   * @return ZoomBox
   */
  public ZoomBox getZoomBox() {
    return zoomBox;
  }

  /**
   * Zooms in to the current zoom box.
   */
  protected void zoomIn() {
    // set up zoomTimer parameters and start
    dxmin = pixToX(Math.min(zoomBox.xstart, zoomBox.xstop))-getXMin();
    dxmax = pixToX(Math.max(zoomBox.xstart, zoomBox.xstop))-getXMax();
    dymin = pixToY(Math.max(zoomBox.ystart, zoomBox.ystop))-getYMin();
    dymax = pixToY(Math.min(zoomBox.ystart, zoomBox.ystop))-getYMax();
    zoomCount = 0;
    zoomTimer.start();
  }

  /**
   * Creates a snapshot using an image of the content.
   */
  public void snapshot() {
    int w = (isVisible()) ? getWidth() : getPreferredSize().width;
    int h = (isVisible()) ? getHeight() : getPreferredSize().height;
    if((w==0)||(h==0)) {
      return;
    }
    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    render(image);
    MeasuredImage mi = new MeasuredImage(image, pixToX(0), pixToX(w), pixToY(h), pixToY(0));
    
    // create ImageFrame using reflection--code change by D Brown 1/6/14
    OSPFrame frame = null;
    try {
			Class<?> c = Class.forName("org.opensourcephysics.frames.ImageFrame"); //$NON-NLS-1$
			Constructor<?>[] constructors = c.getConstructors();
			for(int i = 0; i<constructors.length; i++) {
			  Class<?>[] parameters = constructors[i].getParameterTypes();
			  if(parameters.length==1 && parameters[0]==MeasuredImage.class) {
			    frame = (OSPFrame) constructors[i].newInstance(new Object[] {mi});
			    break;
			  }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    if (frame==null) return;
    
    frame.setTitle(DisplayRes.getString("Snapshot.Title")); //$NON-NLS-1$
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setKeepHidden(false);    
    FontSizer.setFonts(frame, FontSizer.getLevel());
    frame.pack();
    frame.setVisible(true);
  }

  /**
   * Determines if the user can examine and change the scale at run-time by right-clicking.
   * @return <code>true<\code> if inspector is enabled
   */
  public boolean hasInspector() {
    return(popupmenu!=null)&&popupmenu.isEnabled();
  }

  /**
   * Enables the popup inspector option.
   * The default inspector shows a popup menu by right-clicking.
   *
   * @param isEnabled <code>true<\code> if the inspector option is enabled; <code>false<\code> otherwise
   */
  public void enableInspector(boolean isEnabled) {
    popupmenu.setEnabled(isEnabled);
  }

  /**
   * Gets the popup menu.
   */
  public JPopupMenu getPopupMenu() {
    return popupmenu;
  }

  /**
   * Sets the popup menu.
   */
  public void setPopupMenu(JPopupMenu menu) {
    popupmenu = menu;
  }

  /**
   * Shows the drawing panel properties inspector.
   */
  public void showInspector() {
    if(customInspector==null) {
      // DrawingPanelInspector.getInspector(this);  // this static inspector is the same for all drawing panels.
      XMLDrawingPanelInspector.getInspector(this); // this static inspector is the same for all drawing panels.
    } else {
      customInspector.setVisible(true);
    }
  }

  /**
   * Hides the drawing panel properties inspector.
   */
  public void hideInspector() {
    if(customInspector==null) {
      DrawingPanelInspector.hideInspector(); // this static inspector is the same for all drawing panels.
    } else {
      customInspector.setVisible(false);
    }
  }

  /**
   * Sets a custom  properties inspector window.
   *
   * @param w the new inspector window
   */
  public void setCustomInspector(Window w) {
    if(customInspector!=null) {
      customInspector.setVisible(false); // hide the current inspector window
    }
    customInspector = w;
  }

  /**
   * Sets the video tool. May be set to null.
   *
   * @param videoCap the video capture tool
   */
  public void setVideoTool(VideoTool videoCap) {
    if(vidCap!=null) {
      vidCap.setVisible(false); // hide the current video capture tool
    }
    vidCap = videoCap;
    if(vidCap!=null) {
      setBuffered(true);
    }
  }

  /**
   * Gets the video capture tool. May be null.
   *
   * @return the video capture tool
   */
  public VideoTool getVideoTool() {
    return vidCap;
  }

  /**
   * Gets the ratio of pixels per unit in the x and y directions.
   * @return the aspect ratio
   */
  public double getAspectRatio() {
    return(pixelMatrix[3]==1) ? 1 : Math.abs(pixelMatrix[0]/pixelMatrix[3]);
  }

  /**
   * Gets the number of pixels per world unit in the x direction.
   * @return pixels per unit
   */
  public double getXPixPerUnit() {
    return pixelMatrix[0];
  }

  /**
   * Gets the number of pixels per world unit in the y direction.
   * Y pixels per unit is positive if y increases from bottom to top.
   *
   * @return pixels per unit
   */
  public double getYPixPerUnit() {
    return -pixelMatrix[3];
  }

  /**
   * Gets the larger of x or y pixels per world unit.
   * @return pixels per unit
   */
  public double getMaxPixPerUnit() {
    return Math.max(Math.abs(pixelMatrix[0]), Math.abs(pixelMatrix[3]));
  }

  /**
   * Gets the x world coordinate for the left-hand side of the drawing area.
   * @return xmin
   */
  public double getXMin() {
    return xmin;
  }

  /**
   * Gets the preferred x world coordinate for the left-hand side of the drawing area.
   * @return xmin
   */
  public double getPreferredXMin() {
    return xminPreferred;
  }

  /**
   * Gets the x world coordinate for the right-hand side of the drawing area.
   * @return xmax
   */
  public double getXMax() {
    return xmax;
  }

  /**
   * Gets the preferred x world coordinate for the right-hand side of the drawing area.
   * @return xmin
   */
  public double getPreferredXMax() {
    return xmaxPreferred;
  }

  /**
   * Gets the y world coordinate for the top of the drawing area.
   * @return ymax
   */
  public double getYMax() {
    return ymax;
  }

  /**
   * Gets the preferred y world coordinate for the top of the drawing area.
   * @return xmin
   */
  public double getPreferredYMax() {
    return ymaxPreferred;
  }

  /**
   * Gets the y world coordinate for the bottom of the drawing area.
   * @return ymin
   */
  public double getYMin() {
    return ymin;
  }

  /**
   * Gets the preferred y world coordinate for the bottom of the drawing area.
   * @return xmin
   */
  public double getPreferredYMin() {
    return yminPreferred;
  }

  /**
   * Gets the CoordinateStringBuilder that converts mouse events into a string showing world coordinates.
   * @return CoordinateStringBuilder
   */
  public CoordinateStringBuilder getCoordinateStringBuilder() {
    return coordinateStrBuilder;
  }

  /**
   * Sets the CoordinateStringBuilder that converts mouse events into a string showing world coordinates.
   */
  public void setCoordinateStringBuilder(CoordinateStringBuilder builder) {
    coordinateStrBuilder = builder;
  }

  /**
   * Gets the scale that will be used when the panel is drawn.
   * @return Rectangle2D
   */
  public Rectangle2D getScale() {
    setPixelScale();
    return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
  }

  /**
   * Gets the rectangle that bounds all measurable objects.
   *
   * @return Rectangle2D
   */
  public Rectangle2D getMeasure() {
    double xmin = Double.MAX_VALUE;
    double xmax = -Double.MAX_VALUE;
    double ymin = Double.MAX_VALUE;
    double ymax = -Double.MAX_VALUE;
    boolean measurableFound = false;
    ArrayList<Drawable> tempList = getDrawables(); // this clones the list of drawables
    Iterator<Drawable> it = tempList.iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(!(obj instanceof Measurable)||!((Measurable) obj).isMeasured()) {
        continue;               // object is not measurable or measure is not set
      }
      Measurable measurable = (Measurable) obj;
      double gxmax = measurable.getXMax();
      double gxmin = measurable.getXMin();
      if(logScaleX&&(measurable instanceof LogMeasurable)) {
        gxmax = ((LogMeasurable) measurable).getXMaxLogscale();
        gxmin = ((LogMeasurable) measurable).getXMinLogscale();
      }
      double gymax = measurable.getYMax();
      double gymin = measurable.getYMin();
      //System.out.println("measurable="+measurable+" is Log Measurable?"+ (measurable instanceof LogMeasurable));
      if(logScaleY&&(measurable instanceof LogMeasurable)) {
        gymax = ((LogMeasurable) measurable).getYMaxLogscale();
        gymin = ((LogMeasurable) measurable).getYMinLogscale();
        //System.out.println("ymin="+gymin+ "  ymax="+gymax+  "    measurable="+measurable);
      }
      if(!Double.isNaN(gxmax)&&!Double.isNaN(gxmin)&&!Double.isNaN(gymax)&&!Double.isNaN(gymin)) {
        xmin = Math.min(xmin, gxmin);
        xmax = Math.max(xmax, gxmax);
        ymin = Math.min(ymin, gymin);
        ymax = Math.max(ymax, gymax);
        measurableFound = true; // we have at least one valid min-max measure
      }
    }
    if(measurableFound) {
      return new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
    }
    return new Rectangle2D.Double(0, 0, 0, 0);
  }

  /**
   * Gets the affine transformation that converts from world to pixel coordinates.
   * @return the affine transformation
   */
  public AffineTransform getPixelTransform() {
    return(AffineTransform) pixelTransform.clone();
  }

  /**
   * Retrieves the 6 specifiable values in the pixel transformation
   * matrix and places them into an array of double precisions values.
   * The values are stored in the array as
   * {&nbsp;m00&nbsp;m10&nbsp;m01&nbsp;m11&nbsp;m02&nbsp;m12&nbsp;}.
   *
   * @return the transformation matrix
   */
  public double[] getPixelMatrix() {
    return pixelMatrix;
  }

  /**
   *  Calculates min and max values and the affine transformation based on the
   *  current size of the panel and the squareAspect boolean.
   */
  public void setPixelScale() {
    xmin = xminPreferred;             // start with the preferred min-max values.
    xmax = xmaxPreferred;
    ymin = yminPreferred;
    ymax = ymaxPreferred;
    leftGutter = leftGutterPreferred; // start with default gutter values
    topGutter = topGutterPreferred;
    rightGutter = rightGutterPreferred;
    bottomGutter = bottomGutterPreferred;
    width = getWidth();
    height = getHeight();
    if(fixedPixelPerUnit) { // the user has specified a fixed pixel scale
      xmin = (xmaxPreferred+xminPreferred)/2-Math.max(width-leftGutter-rightGutter-1, 1)/xPixPerUnit/2;
      xmax = (xmaxPreferred+xminPreferred)/2+Math.max(width-leftGutter-rightGutter-1, 1)/xPixPerUnit/2;
      ymin = (ymaxPreferred+yminPreferred)/2-Math.max(height-bottomGutter-topGutter-1, 1)/yPixPerUnit/2;
      ymax = (ymaxPreferred+yminPreferred)/2+Math.max(height-bottomGutter-topGutter-1, 1)/yPixPerUnit/2;
      pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin*xPixPerUnit+leftGutter, ymax*yPixPerUnit+topGutter);
      pixelTransform.getMatrix(pixelMatrix); // puts the transformation into the pixel matrix
      return;
    }
    xPixPerUnit = Math.max(width-leftGutter-rightGutter, 1)/(xmax-xmin);
    yPixPerUnit = Math.max(height-bottomGutter-topGutter, 1)/(ymax-ymin); // the y scale in pixels
    if(squareAspect) {
      double stretch = Math.abs(xPixPerUnit/yPixPerUnit);
      if(stretch>=1) {                                                          // make the x range bigger so that aspect ratio is one
        stretch = Math.min(stretch, width);                                     // limit the stretch
        xmin = xminPreferred-(xmaxPreferred-xminPreferred)*(stretch-1)/2.0;
        xmax = xmaxPreferred+(xmaxPreferred-xminPreferred)*(stretch-1)/2.0;
        xPixPerUnit = Math.max(width-leftGutter-rightGutter, 1)/(xmax-xmin);  // the x scale in pixels per unit
      } else {                                                                  // make the y range bigger so that aspect ratio is one
        stretch = Math.max(stretch, 1.0/height);                                // limit the stretch
        ymin = yminPreferred-(ymaxPreferred-yminPreferred)*(1.0/stretch-1)/2.0;
        ymax = ymaxPreferred+(ymaxPreferred-yminPreferred)*(1.0/stretch-1)/2.0;
        yPixPerUnit = Math.max(height-bottomGutter-topGutter, 1)/(ymax-ymin); // the y scale in pixels per unit
      }
    }
    pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin*xPixPerUnit+leftGutter, ymax*yPixPerUnit+topGutter);
    pixelTransform.getMatrix(pixelMatrix); // puts the transformation into the pixel matrix
  }

  /**
   * Recomputes the pixel transforamtion based on the current minimum and maximum values and the gutters.
   */
  public void recomputeTransform() {
    xPixPerUnit = Math.max(width-leftGutter-rightGutter, 1)/(xmax-xmin);
    yPixPerUnit = Math.max(height-bottomGutter-topGutter, 1)/(ymax-ymin); // the y scale in pixels
    pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin*xPixPerUnit+leftGutter, ymax*yPixPerUnit+topGutter);
    pixelTransform.getMatrix(pixelMatrix); // puts the transformation into the pixel matrix
  }

  /**
   * Projects a 2D or 3D world coordinate to a pixel coordinate.
   *
   * An (x, y) point will project to (xpix, ypix).
   * An (x, y, z) point will project to (xpix, ypix).
   * An (x, y, delta_x, delta_y) point will project to (xpix, ypix, delta_xpix, delta_ypix).
   * An (x, y, z, delta_x, delta_y, delta_z) point will project to (xpix, ypix, delta_xpix, delta_ypix).
   *
   * @param coordinate
   * @param pixel
   * @return pixel
   */
  public double[] project(double[] coordinate, double[] pixel) {
    switch(coordinate.length) {
       case 2 :                                                                               // input is x,y
       case 3 :                                                                               // input is x,y,z
         pixel[0] = xToGraphics(coordinate[0]);                                               // x
         pixel[1] = yToGraphics(coordinate[1]);                                               // y
         break;
       case 4 :                                                                               // input is x,y,dx,dy
         pixel[0] = xToGraphics(coordinate[0]);                                               // x
         pixel[1] = yToGraphics(coordinate[1]);                                               // y
         pixel[2] = xPixPerUnit*coordinate[2];                                                // dx
         pixel[3] = yPixPerUnit*coordinate[3];                                                // dy
         break;
       case 6 :                                                                               // input is x,y,z,dx,dy,dz
         pixel[0] = xToGraphics(coordinate[0]);                                               // x
         pixel[1] = yToGraphics(coordinate[1]);                                               // y
         pixel[2] = xPixPerUnit*coordinate[3];                                                // dx
         pixel[3] = yPixPerUnit*coordinate[4];                                                // dy
         break;
       default :
         throw new IllegalArgumentException("Method project not supported for this length."); //$NON-NLS-1$
    }
    return pixel;
  }

  /**
   * Converts pixel to x world units.
   * @param pix
   * @return x panel units
   */
  public double pixToX(int pix) {
    return xmin+(pix-leftGutter)/xPixPerUnit;
  }

  /**
   * Converts x from world to pixel units.
   * @param x
   * @return the pixel value of the x coordinate
   */
  public int xToPix(double x) {
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

  /**
   * Converts x from world to graphics device units.
   * @param x
   * @return the graphics device value of the x coordinate
   */
  public float xToGraphics(double x) {
    float pix = (float) (pixelMatrix[0]*x+pixelMatrix[4]);
    if(pix>Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if(pix<Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    return pix;
  }

  /**
   * Converts pixel to x world units.
   * @param pix
   * @return x panel units
   */
  public double pixToY(int pix) {
    return ymax-(pix-topGutter)/yPixPerUnit;
  }

  /**
   * Converts y from world to pixel units.
   * @param y
   * @return the pixel value of the y coordinate
   */
  public int yToPix(double y) {
    // double pix = (ymax - y) * yPixPerUnit + topGutter;
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
   * Converts y from world to graphics device units.
   * @param y
   * @return the graphics device value of the x coordinate
   */
  public float yToGraphics(double y) {
    float pix = (float) (pixelMatrix[3]*y+pixelMatrix[5]);
    if(pix>Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    }
    if(pix<Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    return pix;
  }

  /**
   * Sets axis scales if autoscale is true using the max and min values of the measurable objects.
   */
  public void scale() {
    ArrayList<Drawable> tempList = getDrawables(); // clone the list of drawables
    scale(tempList);
  }

  /**
   * Sets axis scales if autoscale is true using the max and min values of the objects in the given list.
   */
  protected void scale(ArrayList<Drawable> tempList) {
    if(autoscaleX) {
      scaleX(tempList);
    }
    if(autoscaleY) {
      scaleY(tempList);
    }
  }

  /**
   * Sets the scale based on the max and min values of all measurable objects.
   *
   * Autoscale flags are not respected.
   */
  public void measure() {
    ArrayList<Drawable> tempList = getDrawables(); // this clones the list of drawables
    scaleX(tempList);
    scaleY(tempList);
    setPixelScale();
    invalidateImage(); // validImage = false;
  }

  /**
   * Sets the x axis scale based on the max and min values of all measurable objects.  Autoscale flag is not respected.
   */
  protected void scaleX() {
    ArrayList<Drawable> tempList = getDrawables(); // this clones the list of drawables
    scaleX(tempList);
  }

  /**
   * Sets the x axis scale based on the max and/or min values of all measurable objects.
   * Autoscale flag is not respected.
   */
  protected void scaleX(ArrayList<Drawable> tempList) {
    double newXMin = Double.MAX_VALUE;
    double newXMax = -Double.MAX_VALUE;
    boolean measurableFound = false;
    Iterator<Drawable> it = tempList.iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(!(obj instanceof Measurable)) {
        continue;               // object is not measurable
      }
      Measurable measurable = (Measurable) obj;
      if(!measurable.isMeasured()) {
        continue;               // objects' measure not yet set
      }
      double xmi = measurable.getXMin(), xma = measurable.getXMax();
      if(logScaleX&&(measurable instanceof LogMeasurable)) {
        xmi = ((LogMeasurable) measurable).getXMinLogscale();
        xma = ((LogMeasurable) measurable).getXMaxLogscale();
      }
      if(!Double.isNaN(xmi)&&!Double.isNaN(xma)) {
        newXMin = Math.min(newXMin, xmi);
        newXMin = Math.min(newXMin, xma);
        newXMax = Math.max(newXMax, xma);
        newXMax = Math.max(newXMax, xmi);
        measurableFound = true; // we have at least one valid min-max measure
      }
    }
    // do not change change values unless there is at least one measurable object.
    if(measurableFound) {
      if(logScaleX&&((xLeftMarginPercentage>0.0)||(xRightMarginPercentage>0.0))) {  // logscale 
          newXMax *= 1+xRightMarginPercentage/100.0;
          newXMin /= 1+xLeftMarginPercentage/100.0;
      }else if(!logScaleX&&((xLeftMarginPercentage>0.0)||(xRightMarginPercentage>0.0))) {
          double xMed = (newXMin+newXMax)/2, xLen = (newXMax-newXMin)/2;
          newXMax = xMed+xLen*(1.0+xRightMarginPercentage/100.0);
          newXMin = xMed-xLen*(1.0+xLeftMarginPercentage/100.0);
      }
      if(newXMax-newXMin<Float.MIN_VALUE) { // range is too small to be meaningful for plotting; newXMax-newXMin is always positive
        if(Double.isNaN(xfloor)) {
          newXMin = 0.9*newXMin-0.5;
        } else {
          newXMin = Math.min(newXMin, xfloor);
        }
        if(Double.isNaN(xceil)) {
          newXMax = 1.1*newXMax+0.5;
        } else {
          newXMax = Math.max(newXMax, xceil);
        }
      }
      double range = Math.max(newXMax-newXMin,Float.MIN_VALUE);              // range will always be positive
      while(Math.abs((newXMax+range)/range)>1e5) { // limit autoscale to 5 decimal places
        range *= 2;                                // increase the range
        newXMin -= range;
        newXMax += range;
      }
      if(autoscaleXMin) {
        xminPreferred = newXMin-autoscaleMargin*range;
      }
      if(autoscaleXMax) {
        xmaxPreferred = newXMax+autoscaleMargin*range;
      }
    } else {                                       // we don't have any measurables
      if(!Double.isNaN(xfloor)&&autoscaleXMin) {
        xminPreferred = xfloor;
      }
      if(!Double.isNaN(xceil)&&autoscaleXMax) {
        xmaxPreferred = xceil;
      }
    }
    if(!Double.isNaN(xfloor)) {
      xminPreferred = Math.min(xfloor, xminPreferred);
    }
    if(!Double.isNaN(xceil)) {
      xmaxPreferred = Math.max(xceil, xmaxPreferred);
    }
    // final check to see if range is too small
    if(Math.abs(xmaxPreferred-xminPreferred)<Float.MIN_VALUE) {
      // center scale around xmaxPreferred
      xminPreferred = 0.9*xmaxPreferred-Float.MIN_VALUE;
      xmaxPreferred = 1.1*xmaxPreferred+Float.MIN_VALUE;
    }
  }

  /**
   * Sets the y axis scale based on the max and min values of all measurable objects. Autoscale flag is not respected.
   */
  protected void scaleY() {
    ArrayList<Drawable> tempList = getDrawables(); // this clones the list of drawables
    scaleY(tempList);
  }

  /**
   * Sets the y axis scale based on the max and min values of all measurable objects. Autoscale flag is not respected.
   */
  protected void scaleY(ArrayList<Drawable> tempList) {
    double newYMin = Double.MAX_VALUE;
    double newYMax = -Double.MAX_VALUE;
    boolean measurableFound = false;
    Iterator<Drawable> it = tempList.iterator();
    while(it.hasNext()) {
      Object obj = it.next();
      if(!(obj instanceof Measurable)) {
        continue; // object is not measurable
      }
      Measurable measurable = (Measurable) obj;
      if(!measurable.isMeasured()) {
        continue; // objects' measure not yet set
      }
      double ymi = measurable.getYMin(), yma = measurable.getYMax();
      //System.out.println("measurable="+measurable+" is Log Measurable?"+ (measurable instanceof LogMeasurable));
      if(logScaleY&&(measurable instanceof LogMeasurable)) {
        yma = ((LogMeasurable) measurable).getYMaxLogscale();
        ymi = ((LogMeasurable) measurable).getYMinLogscale();
        //System.out.println("ymin="+ymi+ "  ymax="+yma+  "    measurable="+measurable);
      }
      if(!Double.isNaN(ymi)&&!Double.isNaN(yma)) {
        newYMin = Math.min(newYMin, ymi);
        newYMin = Math.min(newYMin, yma);
        newYMax = Math.max(newYMax, yma);
        newYMax = Math.max(newYMax, ymi);
        measurableFound = true;
      }
    }
    // do not change change values unless there is at least one measurable object.
    if(measurableFound) {
      if(logScaleY&&((yTopMarginPercentage>0.0)||(yBottomMarginPercentage>0.0))) {
        newYMax *= 1.0+yTopMarginPercentage/100.0;
        newYMin /= 1.0+yBottomMarginPercentage/100.0;
      }else if(!logScaleY&&((yTopMarginPercentage>0.0)||(yBottomMarginPercentage>0.0))) {
          double yMed = (newYMin+newYMax)/2, yLen = (newYMax-newYMin)/2;
          newYMax = yMed+yLen*(1.0+yTopMarginPercentage/100.0);
          newYMin = yMed-yLen*(1.0+yBottomMarginPercentage/100.0);
      }
      if(newYMax-newYMin<Float.MIN_VALUE) {
        if(Double.isNaN(yfloor)) {
          newYMin = 0.9*newYMin-0.5;
        } else {
          newYMin = Math.min(newYMin, yfloor);
        }
        if(Double.isNaN(yceil)) {
          newYMax = 1.1*newYMax+0.5;
        } else {
          newYMax = Math.max(newYMax, yceil);
        }
      }
      double range = Math.max(newYMax-newYMin,Float.MIN_VALUE);
      while(Math.abs((newYMax+range)/range)>1e5) { // limit autoscale to 5 decimal places
        range *= 2;                                // increase the range
        newYMin -= range;
        newYMax += range;
      }
      if(autoscaleYMin) {
        yminPreferred = newYMin-autoscaleMargin*range;
      }
      if(autoscaleYMax) {
        ymaxPreferred = newYMax+autoscaleMargin*range;
      }
    } else {                                       // we don't have any measurables
      if(!Double.isNaN(yfloor)&&autoscaleYMin) {
        yminPreferred = yfloor;
      }
      if(!Double.isNaN(yceil)&&autoscaleYMax) {
        ymaxPreferred = yceil;
      }
    }
    if(!Double.isNaN(yfloor)) {
      yminPreferred = Math.min(yfloor, yminPreferred);
    }
    if(!Double.isNaN(yceil)) {
      ymaxPreferred = Math.max(yceil, ymaxPreferred);
    }
    // final check for minimum separation
    if(Math.abs(ymaxPreferred-yminPreferred)<Float.MIN_VALUE) {
      // center scale around ymaxPreferred
      yminPreferred = 0.9*ymaxPreferred-Float.MIN_VALUE;
      ymaxPreferred = 1.1*ymaxPreferred+Float.MIN_VALUE;
    }
  }

  /**
   * Paints all the drawable objects in the panel.
   * @param g
   */
  protected void paintDrawableList(Graphics g, ArrayList<Drawable> tempList) {
    if(tempList==null) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g;
    Iterator<Drawable> it = tempList.iterator();
    Shape clipShape = g2.getClip();
    int w = getWidth()-leftGutter-rightGutter;
    int h = getHeight()-bottomGutter-topGutter;
    if((w<0)||(h<0)) {
      return;
    }
    if(clipAtGutter) {
      g2.clipRect(leftGutter, topGutter, w, h);
    }
    if(!tempList.isEmpty()&&(tempList.get(0) instanceof False3D)) {
      tempList.get(0).draw(this, g2);
    } else {
      while(it.hasNext()) {
        if(!validImage) {
          break; // abort drawing
        }
        Drawable drawable = it.next();
        drawable.draw(this, g2);
      }
    }
    g2.setClip(clipShape);
  }

  /**
   * Gets the glass panel.
   *
   * The glass panel is a trasparent panel that contians the messages boxes and other compotnents.
   * @return JPanel
   */
  public JPanel getGlassPanel() {
    return glassPanel;
  }

  public void setIgnoreRepaint(boolean ignoreRepaint) {
    super.setIgnoreRepaint(ignoreRepaint);
    glassPanel.setIgnoreRepaint(ignoreRepaint);
  }

  /**
   * Gets the object that sets the gutters for this panel.
   * @return Dimensioned
   */
  public Dimensioned getDimensionSetter() {
    return dimensionSetter;
  }

  /**
   * Adds a drawable object to the drawable list.
   * @param drawable
   */
  public void addDrawable(Drawable drawable) {
    synchronized(drawableList) {
      if((drawable!=null)&&!drawableList.contains(drawable)) {
        drawableList.add(drawable);
        invalidateImage(); // validImage = false;
      }
    }
    if(drawable instanceof Dimensioned) {
      dimensionSetter = ((Dimensioned) drawable);
    }
  }

  /**
   * Adds a collection of drawable objects to the drawable list.
   * @param drawables
   */
  public void addDrawables(Collection<Drawable> drawables) {
    synchronized(drawableList) {
      Iterator<Drawable> it = drawables.iterator();
      while(it.hasNext()) {
        Object obj = it.next();
        if(obj instanceof Drawable) {
          addDrawable((Drawable) obj);
        }
      }
    }
  }

  /**
   * Adds a drawable object to the drawable list at the given index.
   * @param drawable
   */
  public void addDrawableAtIndex(int index, Drawable drawable) {
    synchronized(drawableList) {
      if((drawable!=null)&&!drawableList.contains(drawable)) {
        drawableList.add(index, drawable);
        invalidateImage(); // validImage = false;
      }
    }
    if(drawable instanceof Dimensioned) {
      dimensionSetter = ((Dimensioned) drawable);
    }
  }

  /**
   * Replaces a drawable object with another drawable.
   *
   * @param oldDrawable Drawable
   * @param newDrawable Drawable
   */
  public void replaceDrawable(Drawable oldDrawable, Drawable newDrawable) {
    synchronized(drawableList) {
      if((oldDrawable!=null)&&drawableList.contains(oldDrawable)) {
        int i = drawableList.indexOf(oldDrawable);
        drawableList.set(i, newDrawable);
        if(newDrawable instanceof Dimensioned) {
          dimensionSetter = ((Dimensioned) newDrawable);
        }
      } else {
        addDrawable(newDrawable); // oldDrawable does not exist
      }
    }
  }

  /**
   * Removes a drawable object from the drawable list.
   * @param drawable
   */
  public void removeDrawable(Drawable drawable) {
    synchronized(drawableList) {
      drawableList.remove(drawable);
    }
    if(drawable instanceof Dimensioned) {
      dimensionSetter = null;
    }
  }

  /**
   * Removes all objects of the given class from the drawable list.
   *
   * Assignable subclasses are NOT removed.  Interfaces CANNOT be specified.
   *
   * @param c the class
   * @see #removeDrawables(Class c)
   */
  public <T extends Drawable> void removeObjectsOfClass(Class<T> c) {
    synchronized(drawableList) {
      Iterator<Drawable> it = drawableList.iterator();
      while(it.hasNext()) {
        Object element = it.next();
        if(element.getClass()==c) {
          it.remove();
          if(element instanceof Dimensioned) {
            dimensionSetter = null;
          }
        }
      }
    }
  }

  /**
   * Removes all objects assignable to the given class from the drawable list.
   * Interfaces can be specified.
   *
   * @param c the class
   * @see #removeObjectsOfClass(Class c)
   */
  public <T extends Drawable> void removeDrawables(Class<T> c) {
    synchronized(drawableList) {
      Iterator<Drawable> it = drawableList.iterator();
      while(it.hasNext()) {
        Object element = it.next();
        if(c.isInstance(element)) {
          it.remove();
          if(element instanceof Dimensioned) {
            dimensionSetter = null;
          }
        }
      }
    }
  }

  /**
   * Removes the option controller.
   *
   * The option controller may interfere with other mouse actions
   */
  public void removeOptionController() {
    removeMouseListener(optionController);
    removeMouseMotionListener(optionController);
  }

  /**
   * Removes the option controller.
   *
   * The option controller may interfere with other mouse actions
   */
  public void addOptionController() {
    addMouseListener(optionController);
    addMouseMotionListener(optionController);
  }

  /**
   * Removes all drawable objects from the drawable list.
   */
  public void clear() {
    synchronized(drawableList) {
      drawableList.clear();
    }
    dimensionSetter = null;
  }

  /**
   * Gets the cloned list of Drawable objects.
   *
   * This is a shallow clone.  The same objects will be in both the drawable list and the
   * cloned list.
   * @return cloned list
   */
  public ArrayList<Drawable> getDrawables() {
    synchronized(drawableList) {
      return new ArrayList<Drawable>(drawableList);
    }
  }

  /**
   * Gets Drawable objects of an assignable type. The list contains
   * objects that are assignable from the class or interface.
   *
   * Returns a shallow clone.  The same objects will be in the drawable list and the
   * cloned list.
   *
   * @param type the type of Drawable object
   *
   * @return the cloned list
   *
   * @see #getObjectOfClass(Class c)
   */
  public <T extends Drawable> ArrayList<T> getDrawables(Class<T> type) {
    ArrayList<Drawable> all = null;
    synchronized(drawableList) {
      all = new ArrayList<Drawable>(drawableList);
    }
    ArrayList<T> objects = new ArrayList<T>();
    for(Drawable d : all) {
      if(type.isInstance(d)) {
        objects.add(type.cast(d));
      }
    }
    return objects;
  }

  /**
   * Gets objects of a specific class from the drawables list.
   *
   * Assignable subclasses are NOT returned.  Interfaces CANNOT be specified.
   * The same objects will be in the drawable list and the cloned list.
   *
   * @param type the class of the object
   *
   * @return the list
   * @see #getDrawables(Class c)
   */
  public <T extends Drawable> ArrayList<T> getObjectOfClass(Class<T> type) {
    ArrayList<Drawable> all = null;
    synchronized(drawableList) {
      all = new ArrayList<Drawable>(drawableList);
    }
    ArrayList<T> objects = new ArrayList<T>();
    for(Drawable d : all) {
      if(d.getClass()==type) {
        objects.add(type.cast(d));
      }
    }
    return objects;
  }

  /**
   * Gets the gutters.
   */
  public int[] getGutters() {
    return new int[] {leftGutter, topGutter, rightGutter, bottomGutter};
  }

  /**
   * Sets the gutters using the given array.
   * @param gutters int[]
   */
  public void setGutters(int[] gutters) {
    leftGutter = gutters[0];
    topGutter = gutters[1];
    rightGutter = gutters[2];
    bottomGutter = gutters[3];
  }

  /**
   * Sets gutters around the drawing area.
   * @param left
   * @param top
   * @param right
   * @param bottom
   */
  public void setGutters(int left, int top, int right, int bottom) {
    leftGutter = left;
    topGutter = top;
    rightGutter = right;
    bottomGutter = bottom;
  }

  /**
   * Sets preferred gutters around the drawing area.
   * @param left
   * @param top
   * @param right
   * @param bottom
   */
  public void setPreferredGutters(int left, int top, int right, int bottom) {
    leftGutterPreferred = leftGutter = left;
    topGutterPreferred = topGutter = top;
    rightGutterPreferred = rightGutter = right;
    bottomGutterPreferred = bottomGutter = bottom;
  }

  /**
   * Resets the gutters to their preferred values.
   */
  public void resetGutters() {
    leftGutter = leftGutterPreferred;
    topGutter = topGutterPreferred;
    rightGutter = rightGutterPreferred;
    bottomGutter = bottomGutterPreferred;
  }

  /**
   * Gets the bottom gutter of this DrawingPanel.
   *
   * @return bottom gutter
   */
  public int getBottomGutter() {
    return bottomGutter;
  }

  /**
   * Gets the bottom gutter of this DrawingPanel.
   *
   * @return right gutter
   */
  public int getTopGutter() {
    return topGutter;
  }

  /**
   * Gets the left gutter of this DrawingPanel.
   *
   * @return left gutter
   */
  public int getLeftGutter() {
    return leftGutter;
  }

  /**
   * Gets the right gutter of this DrawingPanel.
   *
   * @return right gutter
   */
  public int getRightGutter() {
    return rightGutter;
  }

  /**
   * Shows a message in a yellow text box in the lower right hand corner.
   *
   * @param msg
   */
  public void setMessage(String msg) {
    brMessageBox.setText(msg); // the default message box
  }

  /**
   * Shows a message in a yellow text box.
   *
   * location 0=bottom left
   * location 1=bottom right
   * location 2=top right
   * location 3=top left
   *
   * @param msg
   * @param location
   */
  public void setMessage(String msg, int location) {
    switch(location) {
       case 0 : // usually used for mouse coordiantes
         blMessageBox.setText(msg);
         break;
       case 1 :
         brMessageBox.setText(msg);
         break;
       case 2 :
         trMessageBox.setText(msg);
         break;
       case 3 :
         tlMessageBox.setText(msg);
         break;
    }
  }

  /**
   * Show the coordinates in the text box in the lower left hand corner.
   *
   * @param show
   */
  public void setShowCoordinates(boolean show) {
    if(showCoordinates&&!show) {
      this.removeMouseListener(mouseController);
      this.removeMouseMotionListener(mouseController);
    } else if(!showCoordinates&&show) {
      this.addMouseListener(mouseController);
      this.addMouseMotionListener(mouseController);
    }
    showCoordinates = show;
  }

  /**
   * Returns true if an event starts or ends a zoom operation. Used by
   * OptionController. Method added by D Brown 04 Nov 2011.
   *
   * @param e a mouse event
   * @return true if a zoom event
   */
  public boolean isZoomEvent(MouseEvent e) {
  	return OSPRuntime.isPopupTrigger(e);
  }

  /**
   * The CMController class handles mouse related events in order to display
   * coordinates in the mouse box.
   */
  private class CMController extends MouseInputAdapter {
    /**
     * Handle the mouse pressed event.
     * @param e
     */
    public void mousePressed(MouseEvent e) {
      String s = coordinateStrBuilder.getCoordinateString(DrawingPanel.this, e);
      blMessageBox.setText(s);
    }

    /**
     * Handle the mouse released event.
     * @param e
     */
    public void mouseReleased(MouseEvent e) {
      blMessageBox.setText(null);
    }

    /**
     * Handle the mouse entered event.
     * @param e
     */
    public void mouseEntered(MouseEvent e) {
      if(showCoordinates) {
        setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
    }

    /**
     * Handle the mouse exited event.
     * @param e
     */
    public void mouseExited(MouseEvent e) {
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Handle the mouse dragged event.
     * @param e
     */
    public void mouseDragged(MouseEvent e) {
      String s = coordinateStrBuilder.getCoordinateString(DrawingPanel.this, e);
      blMessageBox.setText(s);
    }

  }

  /**
   * ZoomBox creates an on-screen rectangle using XORMode for fast redrawing.
   */
  public class ZoomBox {
    int xstart, ystart; // start of mouse down
    int xstop, ystop;   // most recent mouse drag
    int xlast, ylast;   // corner position during last drawing
    boolean visible = false;
    boolean dragged = false;
    boolean showUndraggedBox = true;

    /**
     * Starts the zoom by saving the corner location.
     *
     * @param xpix
     * @param ypix
     */
    public void startZoom(int xpix, int ypix) {
      if(!isZoom()) {
        return;
      }
      visible = true;
      dragged = false;
      xlast = xstop = xstart = xpix;
      ylast = ystop = ystart = ypix;
      repaint();
    }

    /**
     * Hides the zoom box.
     */
    public void hide() {
      visible = false;
      repaint();
    }

    /**
     * Sets the showUndraggedBox flag.
     * 
     * @param show true to show a zoom box when the mouse is not dragged
     */
    public void setShowUndraggedBox(boolean show) {
    	showUndraggedBox = show;
    }

		/**
		 * Drags the corner of the ZoomBox. Drag uses XORMode drawing to first erase and
		 * then repaint the box.
		 *
		 * @param xpix
		 * @param ypix
		 */
		public void drag(int xpix, int ypix) {
			if (!visible) {
				return;
			}
			dragged = true;
			xstop = xpix;
			ystop = ypix;
			Graphics g = getGraphics();
			if (g == null) {
				return;
			}
			g.setXORMode(Color.green);
			g.drawRect(Math.min(xstart, xlast), Math.min(ystart, ylast), Math.abs(xlast - xstart),
					Math.abs(ylast - ystart));
			xlast = xstop;
			ylast = ystop;
			g.drawRect(Math.min(xstart, xlast), Math.min(ystart, ylast), Math.abs(xlast - xstart),
					Math.abs(ylast - ystart));
			g.setPaintMode();
			g.dispose();
		}

    /**
     * Paints the ZoomBox after dragging is complete.
     *
     * @param g
     */
    void paint(Graphics g) {
      if(!visible) {
        return;
      }
      if((xstop==xstart)||(ystop==ystart)) {
        return;
      }
      g.setColor(Color.magenta);
      g.drawRect(Math.min(xstart, xstop), Math.min(ystart, ystop), Math.abs(xstop-xstart), Math.abs(ystop-ystart));
    }

    /**
     * Reports the drag status of the zoom box.
     *
     * @return true if the zoom box has been dragged
     */
    public boolean isDragged() {
      return dragged&&(xstop!=xstart)&&(ystop!=ystart);
    }

    /**
     * Gets the visibility of the zoom box.
     *
     * @return true if visible
     */
    public boolean isVisible() {
      return visible;
    }

    /**
     * Reports the zoom rectangle in pixel units.
     */
    public Rectangle reportZoom() {
      int xmin = Math.min(xstart, xstop);
      int xmax = Math.max(xstart, xstop);
      int ymin = Math.min(ystart, ystop);
      int ymax = Math.max(ystart, ystop);
      return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
    }

  }

  class PopupmenuListener implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      zoomBox.visible = false;
      repaint();
      String cmd = evt.getActionCommand();
      if(cmd.equals(DisplayRes.getString("DrawingFrame.InspectMenuItem"))) {            //$NON-NLS-1$
        showInspector();
      } else if(cmd.equals(DisplayRes.getString("DisplayPanel.Snapshot_menu_item"))) {  //$NON-NLS-1$
        snapshot();
      } else if(cmd.equals(DisplayRes.getString("DisplayPanel.Zoom_in_menu_item"))) {   //$NON-NLS-1$
        setAutoscaleX(false);
        setAutoscaleY(false);
        zoomIn();                                                                       // sets zoomMode to true
      } else if(cmd.equals(DisplayRes.getString("DisplayPanel.Zoom_out_menu_item"))) {  //$NON-NLS-1$
        setAutoscaleX(false);
        setAutoscaleY(false);
        zoomOut();
      } else if(cmd.equals(DisplayRes.getString("DrawingFrame.Autoscale_menu_item"))) { //$NON-NLS-1$
        double nan = Double.NaN;
        setPreferredMinMax(nan, nan, nan, nan);
      } else if(cmd.equals(DisplayRes.getString("DrawingFrame.Scale_menu_item"))) {     //$NON-NLS-1$
        ScaleInspector plotInspector = new ScaleInspector(DrawingPanel.this);
        plotInspector.setLocationRelativeTo(DrawingPanel.this);
        plotInspector.updateDisplay();
        FontSizer.setFonts(plotInspector, FontSizer.getLevel()); 
        plotInspector.pack();
        plotInspector.setVisible(true);
      }
    }

  }

  /**
   * OptionController handles mouse actions including zoom.
   */
  class OptionController extends MouseInputAdapter {
    /**
     * Handles the mouse pressed event.
     * @param e
     */
    public void mousePressed(MouseEvent e) {
      if(isZoomEvent(e)) {
        zoomBox.startZoom(e.getX(), e.getY());
      } else {
        zoomBox.visible = false;
        repaint();
      }
    }

    /**
     * Handles the mouse dragged event.
     * @param e
     */
    public void mouseDragged(MouseEvent e) {
      zoomBox.drag(e.getX(), e.getY());
    }

    /**
     * Handles the mouse released event.
     *
     * @param e
     */
    public void mouseReleased(MouseEvent e) {
      if(isZoomEvent(e)&&(popupmenu!=null)&&popupmenu.isEnabled()) {
        if(isZoom()&&!zoomBox.isDragged()&& zoomBox.showUndraggedBox) {
          Dimension dim = viewRect==null? getSize(): viewRect.getSize();
          dim.width -= getLeftGutter()+getRightGutter();
          dim.height -= getTopGutter()+getBottomGutter();
          zoomBox.xstart = e.getX()-dim.width/4;
          zoomBox.xstop = e.getX()+dim.width/4;
          zoomBox.ystart = e.getY()-dim.height/4;
          zoomBox.ystop = e.getY()+dim.height/4;
          zoomBox.visible = true;
          repaint();
        }
        JPopupMenu popup = getPopupMenu();
        if (popup!=null)
        	popup.show(e.getComponent(), e.getX(), e.getY());
        return;
      } 
      else if(OSPRuntime.isPopupTrigger(e)&&(popupmenu==null)&&(customInspector!=null)) {
        customInspector.setVisible(true);
        return;
      }
    }

    /**
     * Method mouseMoved
     *
     * @param e
     */
    public void mouseMoved(MouseEvent e) {
      KeyboardFocusManager focuser =
      		KeyboardFocusManager.getCurrentKeyboardFocusManager();
      Component focusOwner = focuser.getFocusOwner();
      if (focusOwner != null && !(focusOwner instanceof JTextComponent)) {
      	requestFocusInWindow();
      }
    }

  }

  class GlassPanel extends JPanel {
    public void render(Graphics g) {
      try {
      Component[] c = glassPanelLayout.getComponents();
      for(int i = 0, n = c.length; i<n; i++) {
        if(c[i]==null) {
          continue;
        }
        g.translate(c[i].getX(), c[i].getY());
        c[i].print(g);
        g.translate(-c[i].getX(), -c[i].getY());
      }
      } catch(Exception ex) {/* do nothing if drawing fails*/ }
    }  
  }

  /**
   * Returns an XML.ObjectLoader to save and load object data.
   *
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new DrawingPanelLoader();
  }

  /**
   * A class to save and load DrawingPanel data.
   */
  static class DrawingPanelLoader implements XML.ObjectLoader {
    /**
     * Saves DrawingPanel data in an XMLControl.
     *
     * @param control the control
     * @param obj the DrawingPanel to save
     */
    public void saveObject(XMLControl control, Object obj) {
      DrawingPanel panel = (DrawingPanel) obj;
      control.setValue("preferred x min", panel.getPreferredXMin()); //$NON-NLS-1$
      control.setValue("preferred x max", panel.getPreferredXMax()); //$NON-NLS-1$
      control.setValue("preferred y min", panel.getPreferredYMin()); //$NON-NLS-1$
      control.setValue("preferred y max", panel.getPreferredYMax()); //$NON-NLS-1$
      control.setValue("autoscale x", panel.isAutoscaleX());         //$NON-NLS-1$
      control.setValue("autoscale y", panel.isAutoscaleY());         //$NON-NLS-1$
      control.setValue("square aspect", panel.isSquareAspect());     //$NON-NLS-1$
      control.setValue("drawables", panel.getDrawables());           //$NON-NLS-1$
    }

    /**
     * Creates a DrawingPanel.
     *
     * @param control the control
     * @return the newly created panel
     */
    public Object createObject(XMLControl control) {
      DrawingPanel panel = new DrawingPanel();
      double xmin = control.getDouble("preferred x min"); //$NON-NLS-1$
      double xmax = control.getDouble("preferred x max"); //$NON-NLS-1$
      double ymin = control.getDouble("preferred y min"); //$NON-NLS-1$
      double ymax = control.getDouble("preferred y max"); //$NON-NLS-1$
      panel.setPreferredMinMax(xmin, xmax, ymin, ymax); // this sets autoscale to false
      if(control.getBoolean("autoscale x")) { //$NON-NLS-1$
        panel.setAutoscaleX(true);
      }
      if(control.getBoolean("autoscale y")) { //$NON-NLS-1$
        panel.setAutoscaleY(true);
      }
      return panel;
    }

    /**
     * Loads a DrawingPanel with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      DrawingPanel panel = (DrawingPanel) obj;
      double xmin = control.getDouble("preferred x min"); //$NON-NLS-1$
      double xmax = control.getDouble("preferred x max"); //$NON-NLS-1$
      double ymin = control.getDouble("preferred y min"); //$NON-NLS-1$
      double ymax = control.getDouble("preferred y max"); //$NON-NLS-1$
      panel.setPreferredMinMax(xmin, xmax, ymin, ymax); // this sets autoscale to false
      panel.squareAspect = control.getBoolean("square aspect"); //$NON-NLS-1$
      if(control.getBoolean("autoscale x")) { //$NON-NLS-1$
        panel.setAutoscaleX(true);
      }
      if(control.getBoolean("autoscale y")) { //$NON-NLS-1$
        panel.setAutoscaleY(true);
      }
      // load the drawables
      Collection<?> drawables = Collection.class.cast(control.getObject("drawables")); //$NON-NLS-1$
      if(drawables!=null) {
        panel.clear();
        Iterator<?> it = drawables.iterator();
        while(it.hasNext()) {
          panel.addDrawable((Drawable) it.next());
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
 *
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
