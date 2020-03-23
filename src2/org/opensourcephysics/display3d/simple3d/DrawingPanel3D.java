/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.simple3d;
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
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.OSPLayout;
import org.opensourcephysics.display.TextPanel;
import org.opensourcephysics.display3d.core.interaction.InteractionEvent;
import org.opensourcephysics.display3d.core.interaction.InteractionListener;
import org.opensourcephysics.tools.VideoTool;

/**
 *
 * <p>Title: DrawingPanel3D</p>
 *
 * <p>Description: The simple3D implementation of a DrawingPanel3D.</p>
 *
 * <p>Interaction: The panel has only one target, the panel itself.
 * If enabled, the panel issues MOUSE_ENTER, MOUSE_EXIT,
 * MOUSE_MOVED, and MOUSE_DRAGGED InteractionEvents with target=null.
 * When the ALT key is held, the panel also issues MOUSE_PRESSED,
 * MOUSE_DRAGGED (again), and MOUSE_RELEASED InteractionEvents.
 * In this second case, the getInfo() method of the event returns a double[3]
 * with the coordinates of the point selected.</p>
 * <p>Even if the panel is disabled, the panel can be panned, zoomed and (in 3D
 * modes) rotated and the elements in it can be enabled.</p>
 * <p>The interaction capabilities are not XML serialized.</p>
 *
 * <p>Copyright: Open Source Physics project</p>
 * @author Francisco Esquembre
 * @version June 2005
 */
public class DrawingPanel3D extends javax.swing.JPanel implements org.opensourcephysics.display.Renderable, org.opensourcephysics.display3d.core.DrawingPanel3D, Printable, ActionListener {
  static private final int AXIS_DIVISIONS = 10;
  static private final Color bgColor = new Color(239, 239, 255);
  // Configuration variables
  private double xmin, xmax, ymin, ymax, zmin, zmax;
  private VisualizationHints visHints = null;
  private Camera camera = null;
  private String imageFile = null;
  // Implementation variables
  private boolean quickRedrawOn = false, squareAspect = true;
  private double centerX, centerY, centerZ, maximumSize;
  private double aconstant, bconstant;
  private int acenter, bcenter;
  private ArrayList<Object3D> list3D = new ArrayList<Object3D>();
  private ArrayList<org.opensourcephysics.display3d.core.Element> decorationList = new ArrayList<org.opensourcephysics.display3d.core.Element>();
  private ArrayList<org.opensourcephysics.display3d.simple3d.Element> elementList = new ArrayList<org.opensourcephysics.display3d.simple3d.Element>();
  private Object3D.Comparator3D comparator = new Object3D.Comparator3D();   // see class Comparator3D below
  // Variables for decoration
  private ElementArrow xAxis, yAxis, zAxis;
  private ElementText xText, yText, zText;
  private ElementSegment[] boxSides = new ElementSegment[12];
  // Variables for interaction
  private final InteractionTarget myTarget = new InteractionTarget(null, 0);
  private int trackersVisible, keyPressed = -1;
  private int lastX = 0, lastY = 0;
  private InteractionTarget targetHit = null, targetEntered = null;
  private double[] trackerPoint = null;
  private ArrayList<InteractionListener> listeners = new ArrayList<InteractionListener>();
  private ElementSegment[] trackerLines = null;
  // Variables for painting
  volatile private boolean dirtyImage = true;                               // offscreenImage needs to be recomputed
  // the image that will be copied to the screen
  volatile private BufferedImage offscreenImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
  // the image into which we will draw
  private BufferedImage workingImage = offscreenImage;
  private javax.swing.Timer updateTimer = new javax.swing.Timer(100, this); // delay before updating the panel
  private boolean needResize = true, needsToRecompute = true;
  // Variables for Messages
  protected TextPanel trMessageBox = new TextPanel();                       // text box in top right hand corner for message
  protected TextPanel tlMessageBox = new TextPanel();                       // text box in top left hand corner for message
  protected TextPanel brMessageBox = new TextPanel();                       // text box in lower right hand corner for message
  protected TextPanel blMessageBox = new TextPanel();                       // text box in lower left hand corner for mouse coordinates
  protected GlassPanel glassPanel = new GlassPanel();
  protected OSPLayout glassPanelLayout = new OSPLayout();
  protected Rectangle viewRect = null;                                      // the clipping rectangle within a scroll pane viewport
  //CJB
  //Scale factor
  private double factorX = 1.0;
  private double factorY = 1.0;
  private double factorZ = 1.0;
  private static int axisMode = MODE_XYZ;

  /**
   * The video capture tool for this panel.
   */
  protected VideoTool vidCap;

  //CJB
  private void BuildAxesPanel(int mode) {
    if((axisMode==mode)&&(xAxis!=null)) {
      return;
    }
    axisMode = mode;
    if(xAxis==null) {
      /* Decoration of the scene */
      // Create the bounding box
      Resolution axesRes = new Resolution(AXIS_DIVISIONS, 1, 1);
      for(int i = 0, n = boxSides.length; i<n; i++) {
        boxSides[i] = new ElementSegment();
        boxSides[i].getRealStyle().setResolution(axesRes);
        boxSides[i].setPanel(this);                      // Because I don't add it to the panel in the standard way
        decorationList.add(boxSides[i]);
      }
      boxSides[0].getStyle().setLineColor(new Color(128, 0, 0));
      boxSides[3].getStyle().setLineColor(new Color(0, 128, 0));
      boxSides[8].getStyle().setLineColor(new Color(0, 0, 255));
      // Create the axes
      String[] axesLabels = visHints.getAxesLabels();
      xAxis = new ElementArrow();
      xAxis.getRealStyle().setResolution(axesRes);
      xAxis.getStyle().setFillColor(new Color(128, 0, 0));
      xAxis.setPanel(this);
      decorationList.add(xAxis);
      xText = new ElementText();
      xText.setText(axesLabels[0]);
      xText.setJustification(org.opensourcephysics.display3d.core.ElementText.JUSTIFICATION_CENTER);
      xText.getRealStyle().setLineColor(Color.BLACK);
      xText.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
      xText.setPanel(this);
      decorationList.add(xText);
      yAxis = new ElementArrow();
      yAxis.getRealStyle().setResolution(axesRes);
      yAxis.getStyle().setFillColor(new Color(0, 128, 0));
      yAxis.setPanel(this);
      decorationList.add(yAxis);
      yText = new ElementText();
      yText.setText(axesLabels[1]);
      yText.setJustification(org.opensourcephysics.display3d.core.ElementText.JUSTIFICATION_CENTER);
      yText.getRealStyle().setLineColor(Color.BLACK);
      yText.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
      yText.setPanel(this);
      decorationList.add(yText);
      zAxis = new ElementArrow();
      zAxis.getRealStyle().setResolution(axesRes);
      zAxis.getStyle().setFillColor(new Color(0, 0, 255));
      zAxis.setPanel(this);
      decorationList.add(zAxis);
      zText = new ElementText();
      zText.setText(axesLabels[2]);
      zText.setJustification(org.opensourcephysics.display3d.core.ElementText.JUSTIFICATION_CENTER);
      zText.getRealStyle().setLineColor(Color.BLACK);
      zText.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
      zText.setPanel(this);
      decorationList.add(zText);
      // Create the trackers
      trackerLines = new ElementSegment[9];
      for(int i = 0, n = trackerLines.length; i<n; i++) {
        trackerLines[i] = new ElementSegment();
        trackerLines[i].getRealStyle().setResolution(axesRes);
        trackerLines[i].setVisible(false);
        trackerLines[i].setPanel(this);
        decorationList.add(trackerLines[i]);
      }
      setCursorMode();                                   // compute the correct value for trackersVisible
      /* End of decoration */
    } else {
      resetDecoration(xmax-xmin, ymax-ymin, zmax-zmin);
    }
  }
  //CJB

  /**
   * Constructor DrawingPanel3D
   */
  public DrawingPanel3D() {
    // GlassPanel for messages
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
    visHints = new VisualizationHints(this);
    camera = new Camera(this);
    addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(java.awt.event.ComponentEvent e) {
        needResize = true;
        dirtyImage = true;
      }

    });
    IADMouseController mouseController = new IADMouseController();
    addMouseListener(mouseController);
    addMouseMotionListener(mouseController);
    addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent _e) {
        keyPressed = _e.getKeyCode();
        //            System.out.println("Key = "+keyPressed);
      }
      public void keyReleased(java.awt.event.KeyEvent _e) {
        keyPressed = -1;
      }

    });
    this.setFocusable(true);
    //CJB
    //Build the axes
    BuildAxesPanel(axisMode);
    //CJB
    // Set default for displayMode
    if(camera.is3dMode()) {
      visHints.setDecorationType(org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_CUBE);
      visHints.setUseColorDepth(true);
    } else {
      visHints.setDecorationType(org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_NONE);
      visHints.setUseColorDepth(false);
    }
    setPreferredMinMax(-1, 1, -1, 1, -1, 1);
  }

  // ---------------------------------
  // Begin W. Christian additions and changes
  // ---------------------------------

  /**
   * Performs an action for the update timer by rendering a new background image
   * @param  evt
   */
  public void actionPerformed(ActionEvent evt) { // render a new image if the current image is dirty
    if(dirtyImage||needsUpdate()) {
      render(); // renders the scene from within the timer thread
    }
  }

  public void setIgnoreRepaint(boolean ignoreRepaint) {
    super.setIgnoreRepaint(ignoreRepaint);
    glassPanel.setIgnoreRepaint(ignoreRepaint);
  }

  /**
   * Update the panel's buffered image from within a separate timer thread.
   */
  private void updatePanel() {
    if(getIgnoreRepaint()) {
      return; // the animation thread will take care of the update
    }
    updateTimer.setRepeats(false); // perform only one render event
    updateTimer.setCoalesce(true); // coalesce render events
    updateTimer.start();           // start update timer
  }

  /**
   * Paints the component by copying the offscreen image into the graphics context.
   * @param g Graphics
   */
  public void paintComponent(Graphics g) {
    // find the clipping rectangle within a scroll pane viewport
    viewRect = null;
    Container c = getParent();
    while(c!=null) {
      if(c instanceof JViewport) {
        viewRect = ((JViewport) c).getViewRect();
        glassPanel.setBounds(viewRect);
        glassPanelLayout.checkLayoutRect(glassPanel, viewRect);
        break;
      }
      c = c.getParent();
    }
    int xoff = (getWidth()-offscreenImage.getWidth())/2;
    int yoff = (getHeight()-offscreenImage.getHeight())/2;
    g.drawImage(offscreenImage, xoff, yoff, null); // copy image to the center of the panel
    if(dirtyImage||needsUpdate()) { // Paco : can this be commented out?
      updatePanel();                // starts an update timer event
    }
  }

  /**
   * Invalidates this component.  This component and all parents
   * above it are marked as needing to be laid out.  This method can
   * be called often, so it needs to execute quickly.
   * @see       #validate
   * @see       #doLayout
   * @see       LayoutManager
   * @since     JDK1.0
   */
  public void invalidate() {
    needResize = true;
    super.invalidate();
  }

  public BufferedImage render(BufferedImage image) {
    Graphics g = image.getGraphics();
    paintEverything(g, image.getWidth(null), image.getHeight(null));
    Rectangle viewRect = this.viewRect; // reference for thread safety
    if(viewRect!=null) {
      Rectangle r = new Rectangle(0, 0, image.getWidth(null), image.getHeight(null));
      glassPanel.setBounds(r);
      glassPanelLayout.checkLayoutRect(glassPanel, r);
      glassPanel.render(g);
      glassPanel.setBounds(viewRect);
      glassPanelLayout.checkLayoutRect(glassPanel, viewRect);
    } else {
      glassPanel.render(g);
    }
    g.dispose(); // Disposes of the graphics context and releases any system resources that it is using.
    return image;
  }

  public BufferedImage render() {
    if(!isShowing()||isIconified()) { // don't render if panel cannot be seen
      needsToRecompute = true;        // make sure we recompute later when we are showing
      return null;                    // no need to render if the frame is not visible
    }
    BufferedImage workingImage = checkImageSize(this.workingImage);
    if(workingImage==null) return workingImage;
    synchronized(workingImage) {          // do not let threads access workingImage while it is being painted
      if(needResize) {
        computeConstants(workingImage.getWidth(), workingImage.getHeight());
        needResize = false;
      }
      render(workingImage);
      // swap the images
      this.workingImage = offscreenImage; // use current offscreen image for the next drawing
      offscreenImage = workingImage;      // recently drawn image is now the offscreenImage
      dirtyImage = false;                 // offscreenImage is up to date
    }
    // the offscreenImage is now ready to be copied to the screen
    // always update a Swing component from the event thread
    if(SwingUtilities.isEventDispatchThread()) {
      paintImmediately(getVisibleRect()); // we are already within the event thread so DO IT!
    } else {                              // paint within the event thread
      Runnable doNow = new Runnable() {   // runnable object will be called by invokeAndWait
        public void run() {
          paintImmediately(getVisibleRect());
        }

      };
      try {
        SwingUtilities.invokeAndWait(doNow);
      } // wait for the paint operation to finish; should be fast
        catch(InvocationTargetException ex) {}
      catch(InterruptedException ex) {}
    }
    if((vidCap!=null)&&(offscreenImage!=null)&&vidCap.isRecording()) { // buffered image should exists so use it.
      vidCap.addFrame(offscreenImage);
    }
    return workingImage;
  }

  /**
   * Whether the image is dirty or any of the elements has changed
   * @return boolean
   */
  private final boolean needsUpdate() {
    for(Iterator<Element> it = elementList.iterator(); it.hasNext(); ) {
      if((it.next()).getElementChanged()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks the image to see if the working image has the correct Dimension.
   * @return <code>true <\code> if the offscreen image matches the panel;  <code>false <\code> otherwise
   */
  private BufferedImage checkImageSize(BufferedImage image) {
    int width = getWidth(), height = getHeight();
    //System.err.println(" checkImageSize w="+width+"  h= "+height + "buffered image = "+image);
    if((width<=2)||(height<=2)) { // image is too small to draw anything useful
    	//System.err.println(" width and height too small");
      return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    }
    if((image==null)||(width!=image.getWidth())||(height!=image.getHeight())) {
      // a new image with the correct size will be created	
      //System.err.println("begin create compatible image w="+width+"  h= "+height + " image"+image);
      if(org.opensourcephysics.js.JSUtil.isJS) {
    	  image=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	  //image = getGraphicsConfiguration().createCompatibleImage(width, height);  // WC:  Check to see if bug is fixed
    	  //System.err.println("image ="+image);
      } else{
    	  image = getGraphicsConfiguration().createCompatibleImage(width, height);
      }
      //System.err.println("begin create compatible image w="+width+"  h= "+height + " image"+image);
      return image;
    }
    return image; // given image is the correct size
  }

  /**
   * Gets the iconified flag from the top level frame.
   * @return boolean true if frame is iconified; false otherwise
   */
  private boolean isIconified() {
    Component c = getTopLevelAncestor();
    if(c instanceof Frame) {
      return(((Frame) c).getExtendedState()&Frame.ICONIFIED)==1;
    }
    return false;
  }

  // ---------------------------------
  // end of W. Christian additions and changes
  // ---------------------------------
  // ---------------------------------
  // Implementation of core.DrawingPanel3D
  // ---------------------------------
  public java.awt.Component getComponent() {
    return this;
  }

  public void setBackgroundImage(String _imageFile) {
    this.imageFile = _imageFile;
  }

  public String getBackgroundImage() {
    return this.imageFile;
  }

  public void setPreferredMinMax(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
    this.xmin = minX;
    this.xmax = maxX;
    this.ymin = minY;
    this.ymax = maxY;
    this.zmin = minZ;
    this.zmax = maxZ;
    centerX = (xmax+xmin)/2.0;
    centerY = (ymax+ymin)/2.0;
    centerZ = (zmax+zmin)/2.0;
    maximumSize = getMaximum3DSize();
    resetDecoration(xmax-xmin, ymax-ymin, zmax-zmin);
    camera.reset();
    needsToRecompute = true;
    dirtyImage = true;
  }

  final public double getPreferredMinX() {
    return this.xmin;
  }

  final public double getPreferredMaxX() {
    return this.xmax;
  }

  final public double getPreferredMinY() {
    return this.ymin;
  }

  final public double getPreferredMaxY() {
    return this.ymax;
  }

  final public double getPreferredMinZ() {
    return this.zmin;
  }

  final public double getPreferredMaxZ() {
    return this.zmax;
  }

  final double[] getCenter() {
    return new double[] {centerX, centerY, centerZ};
  }

  final double getMaximum3DSize() {
    double dx = xmax-xmin, dy = ymax-ymin, dz = zmax-zmin;
    switch(camera.getProjectionMode()) {
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XY :
         return Math.max(dx, dy);
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XZ :
         return Math.max(dx, dz);
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_YZ :
         return Math.max(dy, dz);
       default :
         return Math.max(Math.max(dx, dy), dz); /* 3D */
    }
  }

  public void zoomToFit() {
    double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
    double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
    double[] firstPoint = new double[3], secondPoint = new double[3];
    Iterator<org.opensourcephysics.display3d.core.Element> it = getElements().iterator();
    while(it.hasNext()) {
      ((org.opensourcephysics.display3d.simple3d.Element) it.next()).getExtrema(firstPoint, secondPoint);
      minX = Math.min(Math.min(minX, firstPoint[0]), secondPoint[0]);
      maxX = Math.max(Math.max(maxX, firstPoint[0]), secondPoint[0]);
      minY = Math.min(Math.min(minY, firstPoint[1]), secondPoint[1]);
      maxY = Math.max(Math.max(maxY, firstPoint[1]), secondPoint[1]);
      minZ = Math.min(Math.min(minZ, firstPoint[2]), secondPoint[2]);
      maxZ = Math.max(Math.max(maxZ, firstPoint[2]), secondPoint[2]);
    }
    double max = Math.max(Math.max(maxX-minX, maxY-minY), maxZ-minZ);
    if(max==0.0) {
      max = 2;
    }
    if(minX>=maxX) {
      minX = maxX-max/2;
      maxX = minX+max;
    }
    if(minY>=maxY) {
      minY = maxY-max/2;
      maxY = minY+max;
    }
    if(minZ>=maxZ) {
      minZ = maxZ-max/2;
      maxZ = minZ+max;
    }
    setPreferredMinMax(minX, maxX, minY, maxY, minZ, maxZ);
  }

  public void setSquareAspect(boolean square) {
    // added by W. Christian
    if(squareAspect!=square) { // only recompute if there is a change
      needsToRecompute = true;
      updatePanel();
    }
    squareAspect = square;
    // computeConstants(); // removed by W. Christian
  }

  public boolean isSquareAspect() {
    return squareAspect;
  }

  public org.opensourcephysics.display3d.core.VisualizationHints getVisualizationHints() {
    return visHints;
  }

  public org.opensourcephysics.display3d.core.Camera getCamera() {
    return camera;
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
   * Sets the video capture tool. May be set to null.
   *
   * @param videoCap the video capture tool
   */
  public void setVideoTool(VideoTool videoCap) {
    if(vidCap!=null) {
      vidCap.setVisible(false); // hide the current video capture tool
    }
    vidCap = videoCap;
  }

  public void addElement(org.opensourcephysics.display3d.core.Element element) {
    if(!(element instanceof org.opensourcephysics.display3d.simple3d.Element)) {
      throw new UnsupportedOperationException("Can't add element to panel (incorrect implementation)"); //$NON-NLS-1$
    }
    if(!elementList.contains(element)) {
      elementList.add((org.opensourcephysics.display3d.simple3d.Element) element);
    }
    //CJB
    //Scale factor
    switch(DrawingPanel3D.axisMode) {
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_XZY :
         ((Element) element).setSizeX(((Element) element).getSizeX()*this.factorX);
         ((Element) element).setSizeZ(((Element) element).getSizeY()*this.factorZ);
         ((Element) element).setSizeY(((Element) element).getSizeZ()*this.factorY);
         ((Element) element).setX(((Element) element).getX()*this.factorX);
         ((Element) element).setZ(((Element) element).getY()*this.factorZ);
         ((Element) element).setY(((Element) element).getZ()*this.factorY);
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YXZ :
         ((Element) element).setSizeY(((Element) element).getSizeX()*this.factorY);
         ((Element) element).setSizeX(((Element) element).getSizeY()*this.factorX);
         ((Element) element).setSizeZ(((Element) element).getSizeZ()*this.factorZ);
         ((Element) element).setY(((Element) element).getX()*this.factorY);
         ((Element) element).setX(((Element) element).getY()*this.factorX);
         ((Element) element).setZ(((Element) element).getZ()*this.factorZ);
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_YZX :
         ((Element) element).setSizeZ(((Element) element).getSizeX()*this.factorZ);
         ((Element) element).setSizeX(((Element) element).getSizeY()*this.factorX);
         ((Element) element).setSizeY(((Element) element).getSizeZ()*this.factorY);
         ((Element) element).setZ(((Element) element).getX()*this.factorZ);
         ((Element) element).setX(((Element) element).getY()*this.factorX);
         ((Element) element).setY(((Element) element).getZ()*this.factorY);
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZXY :
         ((Element) element).setSizeY(((Element) element).getSizeX()*this.factorY);
         ((Element) element).setSizeZ(((Element) element).getSizeY()*this.factorZ);
         ((Element) element).setSizeX(((Element) element).getSizeZ()*this.factorX);
         ((Element) element).setY(((Element) element).getX()*this.factorY);
         ((Element) element).setZ(((Element) element).getY()*this.factorZ);
         ((Element) element).setX(((Element) element).getZ()*this.factorX);
         break;
       case org.opensourcephysics.display3d.core.DrawingPanel3D.MODE_ZYX :
         ((Element) element).setSizeZ(((Element) element).getSizeX()*this.factorZ);
         ((Element) element).setSizeY(((Element) element).getSizeY()*this.factorY);
         ((Element) element).setSizeX(((Element) element).getSizeZ()*this.factorX);
         ((Element) element).setZ(((Element) element).getX()*this.factorZ);
         ((Element) element).setY(((Element) element).getY()*this.factorY);
         ((Element) element).setX(((Element) element).getZ()*this.factorX);
         break;
       default :
         ((Element) element).setSizeX(((Element) element).getSizeX()*this.factorX);
         ((Element) element).setSizeY(((Element) element).getSizeY()*this.factorY);
         ((Element) element).setSizeZ(((Element) element).getSizeZ()*this.factorZ);
         ((Element) element).setX(((Element) element).getX()*this.factorX);
         ((Element) element).setY(((Element) element).getY()*this.factorY);
         ((Element) element).setZ(((Element) element).getZ()*this.factorZ);
       //CJB
    }
    ((Element) element).setPanel(this);
    dirtyImage = true; // element has been added so image is dirty
  }

  public void removeElement(org.opensourcephysics.display3d.core.Element element) {
    elementList.remove(element);
    dirtyImage = true; // element has been added so image is dirty
  }

  public void removeAllElements() {
    elementList.clear();
    dirtyImage = true; // element has been added so image is dirty
  }

  public synchronized java.util.List<org.opensourcephysics.display3d.core.Element> getElements() {
    return new ArrayList<org.opensourcephysics.display3d.core.Element>(elementList);
  }

  //CJB
  public void setScaleFactor(double factorX, double factorY, double factorZ) {
    this.factorX = factorX;
    this.factorY = factorY;
    this.factorZ = factorZ;
  }

  public double getScaleFactorX() {
    return factorX;
  }

  public double getScaleFactorY() {
    return factorY;
  }

  public double getScaleFactorZ() {
    return factorZ;
  }

  public void setAxesMode(int mode) {
    BuildAxesPanel(mode);
    for(int i = 0; i<elementList.size(); i++) {
      Element el = elementList.get(i);
      el.setXYZ(el.getX(), el.getY(), el.getZ());
      el.setSizeXYZ(el.getSizeX(), el.getSizeY(), el.getSizeZ());
    }
  }

  public int getAxesMode() {
    return axisMode;
  }

  //CJB

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
   * The location must be one of the following:
   * <ul>
   *   <li> DrawingPanel3D.BOTTOM_LEFT;
   *   <li> DrawingPanel3D.BOTTOM_RIGHT;
   *   <li> DrawingPanel3D.TOP_RIGHT;
   *   <li> DrawingPanel3D.TOP_LEFT;
   * </ul>
   * @param msg
   * @param location
   */
  public void setMessage(String msg, int location) {
    switch(location) {
       case BOTTOM_LEFT :
         blMessageBox.setText(msg);
         break;
       default :
       case BOTTOM_RIGHT :
         brMessageBox.setText(msg);
         break;
       case TOP_RIGHT :
         trMessageBox.setText(msg);
         break;
       case TOP_LEFT :
         tlMessageBox.setText(msg);
         break;
    }
  }

  // ---------------------------------
  // Implementation of core.InteractionSource
  // ---------------------------------
  public org.opensourcephysics.display3d.core.interaction.InteractionTarget getInteractionTarget(int target) {
    return myTarget;
  }

  public void addInteractionListener(InteractionListener listener) {
    if((listener==null)||listeners.contains(listener)) {
      return;
    }
    listeners.add(listener);
  }

  public void removeInteractionListener(InteractionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Invokes the interactionPerformed() method of all registered
   * interaction listeners
   * @param event InteractionEvent
   */
  private void invokeActions(InteractionEvent event) {
    Iterator<InteractionListener> it = listeners.iterator();
    while(it.hasNext()) {
      it.next().interactionPerformed(event);
    }
  }

  // ----------------------------------------------------
  // All the painting stuff
  // ----------------------------------------------------

  /**
   * Paints everyting assuming an object of the given width and height in pixels.
   * @param g Graphics
   * @param width int
   * @param height int
   */
  private synchronized void paintEverything(Graphics g, int width, int height) {
    // W. Christian recompute scale if something has changed
    if(needsToRecompute||(width!=getWidth())||(height!=getHeight())) {
      computeConstants(width, height);
    }
    java.util.List<org.opensourcephysics.display3d.core.Element> tempList = getElements();
    tempList.addAll(decorationList);
    g.setColor(getBackground());
    g.fillRect(0, 0, width, height); // fill the component with the background color
    paintDrawableList(g, tempList);
  }

  private void paintDrawableList(Graphics g, java.util.List<org.opensourcephysics.display3d.core.Element> tempList) {
    Graphics2D g2 = (Graphics2D) g;
    Iterator<org.opensourcephysics.display3d.core.Element> it = tempList.iterator();
    if(quickRedrawOn||!visHints.isRemoveHiddenLines()) { // Do a quick sketch of the scene
      while(it.hasNext()) {
        ((Element) it.next()).drawQuickly(g2);
      }
      return;
    }
    // Collect objects, sort and draw them one by one. Takes time!!!
    list3D.clear();
    while(it.hasNext()) { // Collect all Objects3D
      Object3D[] objects = ((Element) it.next()).getObjects3D();
      if(objects==null) {
        continue;
      }
      for(int i = 0, n = objects.length; i<n; i++) {
        // providing NaN as distance can be used by Drawables3D to hide a given Object3D
        if(!Double.isNaN(objects[i].getDistance())) {
          list3D.add(objects[i]);
        }
      }
    }
    if(list3D.size()<=0) {
      return;
    }
    Object3D[] objects = list3D.toArray(new Object3D[0]);
    Arrays.sort(objects, comparator);
    for(int i = 0, n = objects.length; i<n; i++) {
      Object3D obj = objects[i];
      obj.getElement().draw(g2, obj.getIndex());
    }
  }

  // ----------------------------------------------------
  // Printable interface
  // ----------------------------------------------------
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
    if(pageIndex>=1) {
      return Printable.NO_SUCH_PAGE;
    }
    if(g==null) {
      return Printable.NO_SUCH_PAGE;
    }
    Graphics2D g2 = (Graphics2D) g;
    double scalex = pageFormat.getImageableWidth()/getWidth();
    double scaley = pageFormat.getImageableHeight()/getHeight();
    double scale = Math.min(scalex, scaley);
    g2.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
    g2.scale(scale, scale);
    paintEverything(g2, getWidth(), getHeight());
    return Printable.PAGE_EXISTS;
  }

  // ----------------------------------------------------
  // Projection, package and private methods
  // ----------------------------------------------------

  /**
   * This will be called by VisualizationHints whenever hints change.
   * @see VisualizationHints
   */
  void hintChanged(int hintThatChanged) {
    switch(hintThatChanged) {
       case VisualizationHints.HINT_ANY : {
         String[] labels = visHints.getAxesLabels();
         xText.setText(labels[0]);
         yText.setText(labels[1]);
         zText.setText(labels[2]);
         setCursorMode();
       }
       // do not break!
       case VisualizationHints.HINT_DECORATION_TYPE :
         switch(visHints.getDecorationType()) {
            case org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_NONE :
              for(int i = 0, n = boxSides.length; i<n; i++) {
                boxSides[i].setVisible(false);
              }
              xAxis.setVisible(false);
              yAxis.setVisible(false);
              zAxis.setVisible(false);
              xText.setVisible(false);
              yText.setVisible(false);
              zText.setVisible(false);
              break;
            case org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_CUBE :
              for(int i = 0, n = boxSides.length; i<n; i++) {
                boxSides[i].setVisible(true);
              }
              xAxis.setVisible(false);
              yAxis.setVisible(false);
              zAxis.setVisible(false);
              xText.setVisible(false);
              yText.setVisible(false);
              zText.setVisible(false);
              break;
            case org.opensourcephysics.display3d.core.VisualizationHints.DECORATION_AXES :
              for(int i = 0, n = boxSides.length; i<n; i++) {
                boxSides[i].setVisible(false);
              }
              xAxis.setVisible(true);
              yAxis.setVisible(true);
              zAxis.setVisible(true);
              xText.setVisible(true);
              yText.setVisible(true);
              zText.setVisible(true);
              break;
         }
         break;
       case VisualizationHints.HINT_AXES_LABELS :
         String[] labels = visHints.getAxesLabels();
         xText.setText(labels[0]);
         yText.setText(labels[1]);
         zText.setText(labels[2]);
         break;
       case VisualizationHints.HINT_CURSOR_TYPE :
         setCursorMode();
         break;
       case VisualizationHints.HINT_SHOW_COORDINATES :
         break; // Actually no dirtyImage is needed...
    }
    dirtyImage = true; // hint has changed so image is dirtry
  }

  /**
   * This will be called by Camera whenever it changes.
   * @see Camera
   */
  void cameraChanged(int howItChanged) {
    //     System.out.println ("Camera mode is now "+camera.getProjectionMode());
    switch(howItChanged) {
       case Camera.CHANGE_MODE :
         double dx = xmax-xmin, dy = ymax-ymin, dz = zmax-zmin;
         maximumSize = getMaximum3DSize();
         resetDecoration(dx, dy, dz);
         // W. Christian constants should be computed when everything is painted
         // computeConstants();
         needsToRecompute = true;
         updatePanel(); // always update if we change display mode
         break;
    }
    reportTheNeedToProject();
    dirtyImage = true; // camera has changed so image is dirtry
  }

  /**
   * Converts a 3D point of the scene into a 2D point of the screen.
   * It also provides a number measuring the relative distance of the point
   * to the camera.
   * distance = 1.0 means at the center of the scene,
   * distance > 1.0 means farther than the center of the scene,
   * distance < 1.0 means closer than the center of the scene,
   * @param coordinate The coordinates of the point of the scene
   * The input coordinates are not modified.
   * @param pixel A place-holder for the coordinates of the point of the screen.
   * It returns a,b and the distance
   * @return The coordinates of the point of the screen and a number
   * which reports about the distance to us
   */
  double[] project(double[] p, double[] pixel) {
    double[] projected = camera.getTransformation().direct(p.clone());
    double factor = 1.8;
    switch(camera.getProjectionMode()) {
       case org.opensourcephysics.display3d.core.Camera.MODE_NO_PERSPECTIVE :
       case org.opensourcephysics.display3d.core.Camera.MODE_PERSPECTIVE_OFF :
         factor = 1.3;
         break;
       case org.opensourcephysics.display3d.core.Camera.MODE_PERSPECTIVE :
       case org.opensourcephysics.display3d.core.Camera.MODE_PERSPECTIVE_ON :
         factor = 1;
         break;
    }
    pixel[0] = acenter+projected[0]*factor*aconstant;
    pixel[1] = bcenter-projected[1]*factor*bconstant;
    pixel[2] = projected[2];
    return pixel;
  }

  /**
   * Converts a world size at a given point into a size in the screen
   * @param p double[] The coordinates of the point at which the 3D
   * size was measured.
   * @param size double[] The size in the X,Y,Z coordinates
   * @param pixelSize double[] A place-holder for the result
   * @return double[] returns the same input pixelSize
   */
  double[] projectSize(double[] p, double[] size, double[] pixelSize) {
    camera.projectSize(p, size, pixelSize);
    double factor = 1.8;
    switch(camera.getProjectionMode()) {
       case org.opensourcephysics.display3d.core.Camera.MODE_NO_PERSPECTIVE :
       case org.opensourcephysics.display3d.core.Camera.MODE_PERSPECTIVE_OFF :
         factor = 1.3;
         break;
       case org.opensourcephysics.display3d.core.Camera.MODE_PERSPECTIVE :
       case org.opensourcephysics.display3d.core.Camera.MODE_PERSPECTIVE_ON :
         factor = 1;
         break;
    }
    pixelSize[0] *= factor*aconstant;
    pixelSize[1] *= factor*bconstant;
    return pixelSize;
  }

  /**
   * Computes the display color of a given drawable3D based on its original color and its depth.
   * Transparency of the original color is not affected.
   * @param _aColor the original color
   * @param _depth the depth value of the color
   */
  Color projectColor(Color _aColor, double _depth) {
    if(!visHints.isUseColorDepth()) {
      return _aColor;
    }
    // if      (_depth<0.9) return _aColor.brighter().brighter();
    // else if (_depth>1.1) return _aColor.darker().darker();
    // else return _aColor;
    float[] crc = new float[4]; // Stands for ColorRGBComponent
    try {
      _aColor.getRGBComponents(crc);
      // Do not affect transparency
      for(int i = 0; i<3; i++) {
        crc[i] /= _depth;
        crc[i] = (float) Math.max(Math.min(crc[i], 1.0), 0.0);
      }
      return new Color(crc[0], crc[1], crc[2], crc[3]);
    } catch(Exception _exc) {
      return _aColor;
    }
  }

  /**
   * Converts a point on the screen into a world point
   * It only works properly for planar display modes
   */
  private double[] worldPoint(int a, int b) {
    double factor = 1.8;
    switch(camera.getProjectionMode()) {
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XY :
         return new double[] {centerX+(a-acenter)/(factor*aconstant), centerY+(bcenter-b)/(factor*bconstant), zmax};
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XZ :
         return new double[] {centerX+(a-acenter)/(factor*aconstant), ymax, centerZ+(bcenter-b)/(factor*bconstant)};
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_YZ :
         return new double[] {xmax, centerY+(a-acenter)/(factor*aconstant), centerZ+(bcenter-b)/(factor*bconstant)};
       default : /* 3D */
         return new double[] {centerX, centerY, centerZ};
    }
  }

  /**
   * Converts into a world distance a distance on the screen
   * It only works properly for planar display modes
   */
  private double[] worldDistance(int dx, int dy) {
    double factor = 1.8;
    switch(camera.getProjectionMode()) {
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XY :
         return new double[] {dx/(factor*aconstant), -dy/(factor*bconstant), 0.0};
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_XZ :
         return new double[] {dx/(factor*aconstant), 0.0, -dy/(factor*bconstant)};
       case org.opensourcephysics.display3d.core.Camera.MODE_PLANAR_YZ :
         return new double[] {0.0, dx/(factor*aconstant), -dy/(factor*bconstant)};
       default : /* 3D */
         return new double[] {dx/(1.3*aconstant), dy/(1.3*bconstant), 0.0};
    }
  }

  /**
   * Computes the constants for the given size in pixels.
   * @param width int
   * @param height int
   */
  private void computeConstants(int width, int height) {
    acenter = width/2;
    bcenter = height/2;
    if(squareAspect) {
      width = height = Math.min(width, height);
    }
    aconstant = 0.5*width/maximumSize;
    bconstant = 0.5*height/maximumSize;
    reportTheNeedToProject();
    needsToRecompute = false;
  }

  private void reportTheNeedToProject() {
    Iterator<org.opensourcephysics.display3d.core.Element> it = getElements().iterator();
    while(it.hasNext()) {
      ((Element) it.next()).setNeedToProject(true);
    }
    it = new ArrayList<org.opensourcephysics.display3d.core.Element>(decorationList).iterator();
    while(it.hasNext()) {
      ((Element) it.next()).setNeedToProject(true);
    }
  }

  private void resetDecoration(double _dx, double _dy, double _dz) {
    boxSides[0].setXYZ(xmin, ymin, zmin);
    boxSides[0].setSizeXYZ(_dx, 0.0, 0.0);
    boxSides[1].setXYZ(xmax, ymin, zmin);
    boxSides[1].setSizeXYZ(0.0, _dy, 0.0);
    boxSides[2].setXYZ(xmin, ymax, zmin);
    boxSides[2].setSizeXYZ(_dx, 0.0, 0.0);
    boxSides[3].setXYZ(xmin, ymin, zmin);
    boxSides[3].setSizeXYZ(0.0, _dy, 0.0);
    boxSides[4].setXYZ(xmin, ymin, zmax);
    boxSides[4].setSizeXYZ(_dx, 0.0, 0.0);
    boxSides[5].setXYZ(xmax, ymin, zmax);
    boxSides[5].setSizeXYZ(0.0, _dy, 0.0);
    boxSides[6].setXYZ(xmin, ymax, zmax);
    boxSides[6].setSizeXYZ(_dx, 0.0, 0.0);
    boxSides[7].setXYZ(xmin, ymin, zmax);
    boxSides[7].setSizeXYZ(0.0, _dy, 0.0);
    boxSides[8].setXYZ(xmin, ymin, zmin);
    boxSides[8].setSizeXYZ(0.0, 0.0, _dz);
    boxSides[9].setXYZ(xmax, ymin, zmin);
    boxSides[9].setSizeXYZ(0.0, 0.0, _dz);
    boxSides[10].setXYZ(xmax, ymax, zmin);
    boxSides[10].setSizeXYZ(0.0, 0.0, _dz);
    boxSides[11].setXYZ(xmin, ymax, zmin);
    boxSides[11].setSizeXYZ(0.0, 0.0, _dz);
    xAxis.setXYZ(xmin, ymin, zmin);
    xAxis.setSizeXYZ(_dx, 0.0, 0.0);
    xText.setXYZ(xmax+_dx*0.02, ymin, zmin);
    yAxis.setXYZ(xmin, ymin, zmin);
    yAxis.setSizeXYZ(0.0, _dy, 0.0);
    yText.setXYZ(xmin, ymax+_dy*0.02, zmin);
    zAxis.setXYZ(xmin, ymin, zmin);
    zAxis.setSizeXYZ(0.0, 0.0, _dz);
    zText.setXYZ(xmin, ymin, zmax+_dz*0.02);
  }

  // ----------------------------------------------------
  // Private methods for the cursor
  // ----------------------------------------------------
  private void setCursorMode() {
    switch(visHints.getCursorType()) {
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_NONE :
         trackersVisible = 0;
         break;
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_CUBE :
         trackersVisible = 9;
         break;
       default :
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_XYZ :
         trackersVisible = 3;
         break;
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_CROSSHAIR :
         trackersVisible = 3;
         break;
    }
  }

  private void showTrackers(boolean value) {
    for(int i = 0, n = trackerLines.length; i<n; i++) {
      if(i<trackersVisible) {
        trackerLines[i].setVisible(value);
      } else {
        trackerLines[i].setVisible(false);
      }
    }
  }

  private void positionTrackers() {
    switch(visHints.getCursorType()) {
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_NONE :
         return;
       default :
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_XYZ :
         trackerLines[0].setXYZ(trackerPoint[0], ymin, zmin);
         trackerLines[0].setSizeXYZ(0, trackerPoint[1]-ymin, 0);
         trackerLines[1].setXYZ(xmin, trackerPoint[1], zmin);
         trackerLines[1].setSizeXYZ(trackerPoint[0]-xmin, 0, 0);
         trackerLines[2].setXYZ(trackerPoint[0], trackerPoint[1], zmin);
         trackerLines[2].setSizeXYZ(0, 0, trackerPoint[2]-zmin);
         break;
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_CUBE :
         trackerLines[0].setXYZ(xmin, trackerPoint[1], trackerPoint[2]);
         trackerLines[0].setSizeXYZ(trackerPoint[0]-xmin, 0, 0);
         trackerLines[1].setXYZ(trackerPoint[0], ymin, trackerPoint[2]);
         trackerLines[1].setSizeXYZ(0, trackerPoint[1]-ymin, 0);
         trackerLines[2].setXYZ(trackerPoint[0], trackerPoint[1], zmin);
         trackerLines[2].setSizeXYZ(0, 0, trackerPoint[2]-zmin);
         trackerLines[3].setXYZ(trackerPoint[0], ymin, zmin);
         trackerLines[3].setSizeXYZ(0, trackerPoint[1]-ymin, 0);
         trackerLines[4].setXYZ(xmin, trackerPoint[1], zmin);
         trackerLines[4].setSizeXYZ(trackerPoint[0]-xmin, 0, 0);
         trackerLines[5].setXYZ(trackerPoint[0], ymin, zmin);
         trackerLines[5].setSizeXYZ(0, 0, trackerPoint[2]-zmin);
         trackerLines[6].setXYZ(xmin, ymin, trackerPoint[2]);
         trackerLines[6].setSizeXYZ(trackerPoint[0]-xmin, 0, 0);
         trackerLines[7].setXYZ(xmin, trackerPoint[1], zmin);
         trackerLines[7].setSizeXYZ(0, 0, trackerPoint[2]-zmin);
         trackerLines[8].setXYZ(xmin, ymin, trackerPoint[2]);
         trackerLines[8].setSizeXYZ(0, trackerPoint[1]-ymin, 0);
         break;
       case org.opensourcephysics.display3d.core.VisualizationHints.CURSOR_CROSSHAIR :
         trackerLines[0].setXYZ(xmin, trackerPoint[1], trackerPoint[2]);
         trackerLines[0].setSizeXYZ(xmax-xmin, 0.0, 0.0);
         trackerLines[1].setXYZ(trackerPoint[0], ymin, trackerPoint[2]);
         trackerLines[1].setSizeXYZ(0.0, ymax-ymin, 0.0);
         trackerLines[2].setXYZ(trackerPoint[0], trackerPoint[1], zmin);
         trackerLines[2].setSizeXYZ(0.0, 0.0, zmax-zmin);
         break;
    }
  }

  // ----------------------------------------------------
  // Interaction
  // ----------------------------------------------------
  private InteractionTarget getTargetHit(int x, int y) {
    Iterator<org.opensourcephysics.display3d.core.Element> it = getElements().iterator();
    InteractionTarget target = null;
    while(it.hasNext()) {
      target = ((Element) it.next()).getTargetHit(x, y);
      if(target!=null) {
        return target;
      }
    }
    return null;
  }

  private void setMouseCursor(Cursor cursor) {
    Container c = getTopLevelAncestor();
    setCursor(cursor);
    if(c!=null) {
      c.setCursor(cursor);
    }
  }

  private void displayPosition(double[] _point) {
    visHints.displayPosition(camera.getProjectionMode(), _point);
  }

  // returns true if the tracker was moved
  private boolean mouseDraggedComputations(java.awt.event.MouseEvent e) {
    if(e.isControlDown()) { // Panning
      if(camera.is3dMode()) {
        double fx = camera.getFocusX(), fy = camera.getFocusY(), fz = camera.getFocusZ();
        double dx = (e.getX()-lastX)*maximumSize*0.01, dy = (e.getY()-lastY)*maximumSize*0.01;
        switch(keyPressed) {
           case 88 :        // X is pressed
             if((camera.cosAlpha>=0)&&(Math.abs(camera.sinAlpha)<camera.cosAlpha)) {
               camera.setFocusXYZ(fx+dy, fy, fz);
             } else if((camera.sinAlpha>=0)&&(Math.abs(camera.cosAlpha)<camera.sinAlpha)) {
               camera.setFocusXYZ(fx+dx, fy, fz);
             } else if((camera.cosAlpha<0)&&(Math.abs(camera.sinAlpha)<-camera.cosAlpha)) {
               camera.setFocusXYZ(fx-dy, fy, fz);
             } else {
               camera.setFocusXYZ(fx-dx, fy, fz);
             }
             break;
           case 89 :        // Y is pressed
             if((camera.cosAlpha>=0)&&(Math.abs(camera.sinAlpha)<camera.cosAlpha)) {
               camera.setFocusXYZ(fx, fy-dx, fz);
             } else if((camera.sinAlpha>=0)&&(Math.abs(camera.cosAlpha)<camera.sinAlpha)) {
               camera.setFocusXYZ(fx, fy+dy, fz);
             } else if((camera.cosAlpha<0)&&(Math.abs(camera.sinAlpha)<-camera.cosAlpha)) {
               camera.setFocusXYZ(fx, fy+dx, fz);
             } else {
               camera.setFocusXYZ(fx, fy-dy, fz);
             }
             break;
           case 90 :        // Z is pressed
             if(camera.cosBeta>=0) {
               camera.setFocusXYZ(fx, fy, fz+dy);
             } else {
               camera.setFocusXYZ(fx, fy, fz-dy);
             }
             break;
           default :
             if(camera.cosBeta<0) {
               dy = -dy;
             }
             if((camera.cosAlpha>=0)&&(Math.abs(camera.sinAlpha)<camera.cosAlpha)) {
               camera.setFocusXYZ(fx, fy-dx, fz+dy);
             } else if((camera.sinAlpha>=0)&&(Math.abs(camera.cosAlpha)<camera.sinAlpha)) {
               camera.setFocusXYZ(fx+dx, fy, fz+dy);
             } else if((camera.cosAlpha<0)&&(Math.abs(camera.sinAlpha)<-camera.cosAlpha)) {
               camera.setFocusXYZ(fx, fy+dx, fz-dy);
             } else {
               camera.setFocusXYZ(fx-dx, fy, fz-dy);
             }
             break;
        }
      }
      return false;
    }                       // End of panning
    if(e.isShiftDown()) { // Zooming
      camera.setDistanceToScreen(camera.getDistanceToScreen()-(e.getY()-lastY)*maximumSize*0.01);
      return false;
    }
    if(camera.is3dMode()&&(targetHit==null)&&!e.isAltDown()) { // Rotating (in 3D)
      camera.setAzimuthAndAltitude(camera.getAzimuth()-(e.getX()-lastX)*0.01, camera.getAltitude()+(e.getY()-lastY)*0.005);
      return false;
    }
    if(trackerPoint==null) {
      return true;
    }
    // In all other cases, you are moving the tracker
    double[] point = worldDistance(e.getX()-lastX, e.getY()-lastY);
    if(!camera.is3dMode()) { // 2D modes
      switch(keyPressed) {
         case 88 :
           trackerPoint[0] += point[0];
           break;            // X is pressed
         case 89 :
           trackerPoint[1] += point[1];
           break;            // Y is pressed
         case 90 :
           trackerPoint[2] += point[2];
           break;            // Z is pressed
         default :
           trackerPoint[0] += point[0];
           trackerPoint[1] += point[1];
           trackerPoint[2] += point[2];
           break;            // No key is pressed
      }
    }                        // End of 2D modes
      else {
      switch(keyPressed) {
         case 88 :           // X is pressed
           if((camera.cosAlpha>=0)&&(Math.abs(camera.sinAlpha)<camera.cosAlpha)) {
             trackerPoint[0] += point[1];
           } else if((camera.sinAlpha>=0)&&(Math.abs(camera.cosAlpha)<camera.sinAlpha)) {
             trackerPoint[0] -= point[0];
           } else if((camera.cosAlpha<0)&&(Math.abs(camera.sinAlpha)<-camera.cosAlpha)) {
             trackerPoint[0] -= point[1];
           } else {
             trackerPoint[0] += point[0];
           }
           break;
         case 89 :           // Y is pressed
           if((camera.cosAlpha>=0)&&(Math.abs(camera.sinAlpha)<camera.cosAlpha)) {
             trackerPoint[1] += point[0];
           } else if((camera.sinAlpha>=0)&&(Math.abs(camera.cosAlpha)<camera.sinAlpha)) {
             trackerPoint[1] += point[1];
           } else if((camera.cosAlpha<0)&&(Math.abs(camera.sinAlpha)<-camera.cosAlpha)) {
             trackerPoint[1] -= point[0];
           } else {
             trackerPoint[1] -= point[1];
           }
           break;
         case 90 :           // Z is pressed
           if(camera.cosBeta>=0) {
             trackerPoint[2] -= point[1];
           } else {
             trackerPoint[2] -= point[2];
           }
           break;
         default :           // No key is pressed
           if(camera.cosBeta>=0) {
             trackerPoint[2] -= point[1];
           } else {
             trackerPoint[2] += point[1];
           }
           if((camera.cosAlpha>=0)&&(Math.abs(camera.sinAlpha)<camera.cosAlpha)) {
             trackerPoint[1] += point[0];
           } else if((camera.sinAlpha>=0)&&(Math.abs(camera.cosAlpha)<camera.sinAlpha)) {
             trackerPoint[0] -= point[0];
           } else if((camera.cosAlpha<0)&&(Math.abs(camera.sinAlpha)<-camera.cosAlpha)) {
             trackerPoint[1] -= point[0];
           } else {
             trackerPoint[0] += point[0];
           }
           break;
      }
    }                        // End of 3D modes
    return true;
  }

  private void resetInteraction() {
    targetHit = null;
    showTrackers(false);
    displayPosition(null);
    // blMessageBox.setText(null);
    // repaint();  removed by W. Christian
    dirtyImage = true;
    updatePanel();
  }

  /**
   * The inner class that will handle all mouse related events.
   */
  private class IADMouseController extends MouseInputAdapter {
    public void mousePressed(MouseEvent _evt) {
      requestFocus();
      if(_evt.isPopupTrigger()||(_evt.getModifiers()==InputEvent.BUTTON3_MASK)) {
        return;
      }
      //         quickRedrawOn = visHints.isAllowQuickRedraw() || keyPressed==83;  // 's' is pressed
      /*
      if(visHints.isAllowQuickRedraw()&&((_evt.getModifiers()&InputEvent.BUTTON1_MASK)!=0)) {
         quickRedrawOn = true;
      } else {
         quickRedrawOn = false;
      }
*/
      lastX = _evt.getX();
      lastY = _evt.getY();
      targetHit = getTargetHit(lastX, lastY);
      if(targetHit!=null) {
        Element el = targetHit.getElement();
        trackerPoint = el.getHotSpot(targetHit);
        el.invokeActions(new InteractionEvent(el, InteractionEvent.MOUSE_PRESSED, targetHit.getActionCommand(), targetHit, _evt));
        trackerPoint = el.getHotSpot(targetHit);     // because the listener may change the position of the element
      } else if(myTarget.isEnabled()) {              // No interactive has been hit
        if((!camera.is3dMode())||_evt.isAltDown()) { // In 2D by default, in 3D only if you hold ALT down
          // You are trying to track a given point
          trackerPoint = worldPoint(_evt.getX(), _evt.getY());
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_PRESSED, myTarget.getActionCommand(), trackerPoint, _evt));
        } else {
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_PRESSED, myTarget.getActionCommand(), null, _evt));
          resetInteraction();
          return;
        }
      } else {
        resetInteraction();
        return;
      }
      displayPosition(trackerPoint);
      positionTrackers();
      showTrackers(true);
      // repaint();  removed by W. Christian
      dirtyImage = true;
      updatePanel();
    }

    public void mouseReleased(MouseEvent _evt) {
      if(_evt.isPopupTrigger()||(_evt.getModifiers()==InputEvent.BUTTON3_MASK)) {
        return;
      }
      if(targetHit!=null) {
        Element el = targetHit.getElement();
        el.invokeActions(new InteractionEvent(el, InteractionEvent.MOUSE_RELEASED, targetHit.getActionCommand(), targetHit, _evt));
      } else if(myTarget.isEnabled()) {
        if((!camera.is3dMode())||_evt.isAltDown()) { // In 2D by default, in 3D only if you hold ALT down
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_RELEASED, myTarget.getActionCommand(), trackerPoint, _evt));
        } else {
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_RELEASED, myTarget.getActionCommand(), null, _evt));
        }
      }
      quickRedrawOn = false;
      resetInteraction();
      // setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseDragged(MouseEvent _evt) {
      if(_evt.isPopupTrigger()||(_evt.getModifiers()==InputEvent.BUTTON3_MASK)) {
        return;
      }
      quickRedrawOn = visHints.isAllowQuickRedraw()&&(keyPressed!=83);
      boolean trackerMoved = mouseDraggedComputations(_evt);
      lastX = _evt.getX();
      lastY = _evt.getY();
      if(!trackerMoved) { // Report any listener that the projection has changed. Data is NULL!
        invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_DRAGGED, myTarget.getActionCommand(), null, _evt));
        resetInteraction();
        return;
      }
      if(targetHit!=null) {
        Element el = targetHit.getElement();
        el.updateHotSpot(targetHit, trackerPoint);
        el.invokeActions(new InteractionEvent(el, InteractionEvent.MOUSE_DRAGGED, targetHit.getActionCommand(), targetHit, _evt));
        trackerPoint = el.getHotSpot(targetHit); // The listener may change the position of the element
        displayPosition(trackerPoint);
        positionTrackers();
        showTrackers(true);                      // should trackers appear only in 3D mode?
      } else if(myTarget.isEnabled()) {
        invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_DRAGGED, myTarget.getActionCommand(), trackerPoint, _evt));
        displayPosition(trackerPoint);
        positionTrackers();
        showTrackers(true); // should trackers appear only in 3D mode?
      }
      // repaint();  removed by W. Christian
      dirtyImage = true;
      updatePanel();
    }

    public void mouseEntered(MouseEvent _evt) {
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      if(myTarget.isEnabled()) {
        invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_ENTERED, myTarget.getActionCommand(), null, _evt));
      }
      targetHit = targetEntered = null;
    }

    public void mouseExited(MouseEvent _evt) {
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      if(myTarget.isEnabled()) {
        invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_EXITED, myTarget.getActionCommand(), null, _evt));
      }
      targetHit = targetEntered = null;
    }

    public void mouseClicked(MouseEvent _evt) {}

    public void mouseMoved(MouseEvent _evt) {
      InteractionTarget target = getTargetHit(_evt.getX(), _evt.getY());
      if(target!=null) {
        if(targetEntered==null) {
          target.getElement().invokeActions(new InteractionEvent(target.getElement(), InteractionEvent.MOUSE_ENTERED, target.getActionCommand(), target, _evt));
        }
        setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      } else { // No target under the cursor
        if(targetEntered!=null) {
          targetEntered.getElement().invokeActions(new InteractionEvent(targetEntered.getElement(), InteractionEvent.MOUSE_EXITED, targetEntered.getActionCommand(), targetEntered, _evt));
        } else if(myTarget.isEnabled()) {
          invokeActions(new InteractionEvent(DrawingPanel3D.this, InteractionEvent.MOUSE_MOVED, myTarget.getActionCommand(), null, _evt));
        }
        setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
      targetEntered = target;
    }

  }

  private class GlassPanel extends javax.swing.JPanel {
    public void render(Graphics g) {
      Component[] c = glassPanelLayout.getComponents();
      for(int i = 0, n = c.length; i<n; i++) {
        if(c[i]==null) {
          continue;
        }
        g.translate(c[i].getX(), c[i].getY());
        c[i].print(g);
        g.translate(-c[i].getX(), -c[i].getY());
      }
    }

  }

  // ----------------------------------------------------
  // Lights
  // ----------------------------------------------------
  public void setLightEnabled(boolean _state, int nlight) {} // simple3d supports no light control

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------

  /**
   * Returns an XML.ObjectLoader to save and load object data.
   * @return the XML.ObjectLoader
   */
  public static XML.ObjectLoader getLoader() {
    return new DrawingPanel3DLoader();
  }

  static private class DrawingPanel3DLoader extends org.opensourcephysics.display3d.core.DrawingPanel3D.Loader {
    public Object createObject(XMLControl control) {
      return new DrawingPanel3D();
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      DrawingPanel3D panel = (DrawingPanel3D) obj;
      // Load the visualization hints
      VisualizationHints hints = (VisualizationHints) control.getObject("visualization hints"); //$NON-NLS-1$
      hints.setPanel(panel);
      panel.visHints = hints;
      panel.hintChanged(VisualizationHints.HINT_DECORATION_TYPE);
      // Load the camera
      Camera cam = (Camera) control.getObject("camera"); //$NON-NLS-1$
      cam.setPanel(panel);
      panel.camera = cam;
      panel.cameraChanged(Camera.CHANGE_ANY);
      // Order a render()
      panel.needsToRecompute = true;
      panel.dirtyImage = true; // new data so image is dirtry
      panel.updatePanel();
      return obj;
    }

  } // End of static class DrawingPanel3DLoader

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
