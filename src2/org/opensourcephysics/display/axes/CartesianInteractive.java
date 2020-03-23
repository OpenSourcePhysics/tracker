/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.axes;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.Selectable;
import org.opensourcephysics.display.dialogs.DialogsRes;
import org.opensourcephysics.media.core.ScientificField;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * A cartesian axis class that provides interactive scale adjustment with a mouse.
 */
public class CartesianInteractive extends CartesianType1 implements Selectable {
  // static constant plot regions
  public static final int INSIDE = 0, HORZ_MIN = 1, HORZ_MAX = 2, VERT_MIN = 3, 
  	VERT_MAX = 4, HORZ_AXIS = 5, HORZ_AXIS_MIN = 6, HORZ_AXIS_MAX = 7, 
  	VERT_AXIS = 8, VERT_AXIS_MIN = 9, VERT_AXIS_MAX = 10, HORZ_VAR = 11, VERT_VAR = 12;
  // instance fields
  Rectangle hitRect = new Rectangle();
  boolean drawHitRect;
  ScaleSetter scaleSetter;
  JPanel scaleSetterPanel;
  AxisMouseListener axisListener;
  int mouseRegion;
  Point mouseLoc;
  double mouseX, mouseY;
  PlottingPanel plot;
  boolean enabled = true;
  boolean altDown;
  Cursor horzCenter, horzRight, horzLeft, vertCenter, vertUp, vertDown, move;

  java.util.List<ActionListener> axisListeners = new java.util.ArrayList<ActionListener>(); // Paco
  
  /**
   * Constructs a set of interactive axes for a plotting panel.
   *
   * @param panel the PlottingPanel
   */
  public CartesianInteractive(PlottingPanel panel) {
    super(panel);
    plot = panel;
    axisListener = new AxisMouseListener();
    panel.addMouseListener(axisListener);
    panel.addMouseMotionListener(axisListener);
    panel.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent e) {
        if (!enabled) return;
        if((mouseRegion==INSIDE)&&!drawingPanel.isFixedScale()&&(e.getKeyCode()==java.awt.event.KeyEvent.VK_ALT)) {
          altDown = true;
          plot.setMouseCursor(getPreferredCursor());
        }
      }
      public void keyReleased(java.awt.event.KeyEvent e) {
        if (!enabled) return;
        if(e.getKeyCode()==java.awt.event.KeyEvent.VK_ALT) {
          altDown = false;
          plot.setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
      }

    });
    scaleSetter = new ScaleSetter();
    // create transparent scaleSetterPanel with no LayoutManager
    scaleSetterPanel = new javax.swing.JPanel(null);
    scaleSetterPanel.setOpaque(false);
    scaleSetterPanel.add(scaleSetter);
    plot.getGlassPanel().add(scaleSetterPanel, BorderLayout.CENTER);
  }

  /**
   * Gets the current plot region containing the mouse.
   *
   * @return one of the static plot regions defined by CartesianInteractive, or -1
   */
  public int getMouseRegion() {
    return mouseRegion;
  }

  /**
   * Draws the axes.
   *
   * @param panel the drawing panel
   * @param g the graphics context
   */
  public void draw(DrawingPanel panel, Graphics g) {
    super.draw(panel, g);
    if(drawHitRect) {
      g.drawRect(hitRect.x, hitRect.y, hitRect.width, hitRect.height);
    }
    if(!panel.isFixedScale()&&scaleSetter.isVisible()&&(scaleSetter.scaleField.getBackground()!=Color.yellow)) {
      switch(scaleSetter.region) {
         case HORZ_MIN :
           scaleSetter.scaleField.setValue(drawingPanel.getXMin());
           scaleSetter.autoscaleCheckbox.setSelected(drawingPanel.isAutoscaleXMin());
           break;
         case HORZ_MAX :
           scaleSetter.scaleField.setValue(drawingPanel.getXMax());
           scaleSetter.autoscaleCheckbox.setSelected(drawingPanel.isAutoscaleXMax());
           break;
         case VERT_MIN :
           scaleSetter.scaleField.setValue(drawingPanel.getYMin());
           scaleSetter.autoscaleCheckbox.setSelected(drawingPanel.isAutoscaleYMin());
           break;
         case VERT_MAX :
           scaleSetter.scaleField.setValue(drawingPanel.getYMax());
           scaleSetter.autoscaleCheckbox.setSelected(drawingPanel.isAutoscaleYMax());
      }
    }
  }

  // overrides CartesianType1 method
  public double getX() {
    return Double.isNaN(mouseX) ? plot.pixToX(plot.getMouseIntX()) : mouseX;
  }

  // overrides CartesianType1 method
  public double getY() {
    return Double.isNaN(mouseY) ? plot.pixToY(plot.getMouseIntY()) : mouseY;
  }

  // implements Selectable
  public void setSelected(boolean selectable) {}

  // implements Selectable
  public boolean isSelected() {
    return false;
  }

  // implements Selectable
  public void toggleSelected() {}

  // implements Selectable
  public Cursor getPreferredCursor() {
    switch(mouseRegion) {
       case HORZ_AXIS_MIN :
         if(horzLeft==null) {
           // create cursor
           String imageFile = "/org/opensourcephysics/resources/tools/images/horzleft.gif";                     //$NON-NLS-1$
           Image im = ResourceLoader.getImage(imageFile);
           horzLeft = GUIUtils.createCustomCursor(im, new Point(16, 16), 
          		 "Horizontal Left", Cursor.W_RESIZE_CURSOR); //$NON-NLS-1$
         }
         return horzLeft;
       case HORZ_AXIS_MAX :
         if(horzRight==null) {
           // create cursor
           String imageFile = "/org/opensourcephysics/resources/tools/images/horzright.gif";                      //$NON-NLS-1$
           Image im = ResourceLoader.getImage(imageFile);
           horzRight = GUIUtils.createCustomCursor(im, new Point(16, 16), 
          		 "Horizontal Right", Cursor.E_RESIZE_CURSOR); //$NON-NLS-1$
         }
         return horzRight;
       case HORZ_AXIS :
         if(horzCenter==null) {
           // create cursor
           String imageFile = "/org/opensourcephysics/resources/tools/images/horzcenter.gif";                       //$NON-NLS-1$
           Image im = ResourceLoader.getImage(imageFile);
           horzCenter = GUIUtils.createCustomCursor(im, new Point(16, 16), 
          		 "Horizontal Center", Cursor.MOVE_CURSOR); //$NON-NLS-1$
         }
         return horzCenter;
       case VERT_AXIS_MIN :
         if(vertDown==null) {
           // create cursor
           String imageFile = "/org/opensourcephysics/resources/tools/images/vertdown.gif";                   //$NON-NLS-1$
           Image im = ResourceLoader.getImage(imageFile);
           vertDown = GUIUtils.createCustomCursor(im, new Point(16, 16), 
          		 "Vertical Down", Cursor.S_RESIZE_CURSOR); //$NON-NLS-1$
         }
         return vertDown;
       case VERT_AXIS_MAX :
         if(vertUp==null) {
           // create cursor
           String imageFile = "/org/opensourcephysics/resources/tools/images/vertup.gif";                         //$NON-NLS-1$
           Image im = ResourceLoader.getImage(imageFile);
           vertUp = GUIUtils.createCustomCursor(im, new Point(16, 16), 
          		 "Vertical Up", Cursor.N_RESIZE_CURSOR);         //$NON-NLS-1$
         }
         return vertUp;
       case VERT_AXIS :
         if(vertCenter==null) {
           // create cursor
           String imageFile = "/org/opensourcephysics/resources/tools/images/vertcenter.gif";                     //$NON-NLS-1$
           Image im = ResourceLoader.getImage(imageFile);
           vertCenter = GUIUtils.createCustomCursor(im, new Point(16, 16), 
          		 "Vertical Center", Cursor.MOVE_CURSOR); //$NON-NLS-1$
         }
         return vertCenter;
       case INSIDE :
         if(move==null) {
           // create cursor
           String imageFile = "/org/opensourcephysics/resources/tools/images/movecursor.gif";             //$NON-NLS-1$
           Image im = ResourceLoader.getImage(imageFile);
           move = GUIUtils.createCustomCursor(im, new Point(16, 16), 
          		 "Move All Ways", Cursor.MOVE_CURSOR); //$NON-NLS-1$
         }
         return move;
       case HORZ_VAR :
       case VERT_VAR :
         return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }
    return Cursor.getDefaultCursor();
  }

  // implements Interactive
  public boolean isEnabled() {
    return enabled;
  }

  // implements Interactive
  public void setEnabled(boolean enable) {
    enabled = enable;
  }

  public void addAxisListener(ActionListener listener) { axisListeners.add(listener); } // Paco

  // implements Interactive
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
    if(drawingPanel.isFixedScale()) {
      return null;
    }
    if(mouseRegion>=HORZ_MIN) {
      return this;
    }
    if(mouseRegion==-1) {
      return this;
    }
    if((mouseRegion==INSIDE)&&altDown) {
      return this;
    }
    return null;
  }

  // implements Interactive
  public void setXY(double x, double y) {}

  // implements Measurable
  public boolean isMeasured() {
    return true;
  }

  // implements Measurable
  public double getXMin() {
    return drawingPanel.getXMin();
  }

  // implements Measurable
  public double getXMax() {
    return drawingPanel.getXMax();
  }

  // implements Measurable
  public double getYMin() {
    return drawingPanel.getYMin();
  }

  // implements Measurable
  public double getYMax() {
    return drawingPanel.getYMax();
  }

  /**
   * Hides the scale setter.
   */
  public void hideScaleSetter() {
    if(scaleSetter!=null) {
      scaleSetter.autoscaleCheckbox.requestFocusInWindow();
      scaleSetter.setVisible(false);
    }
  }

  /**
   * Resizes fonts by the specified factor.
   *
   * @param factor the factor
   * @param panel the drawing panel on which these axes are drawn
   */
  public void resizeFonts(double factor, DrawingPanel panel) {
    super.resizeFonts(factor, panel);
    if(scaleSetter!=null) {
      scaleSetter.scaleField.setFont(FontSizer.getResizedFont(scaleSetter.scaleField.getFont(), factor));
      scaleSetter.autoscaleCheckbox.setFont(FontSizer.getResizedFont(scaleSetter.autoscaleCheckbox.getFont(), factor));
    }
  }

  /**
   * Reports whether this provides a popup menu for setting the horizontal axis variable.
   *
   * @return true if this has a popup menu with horizontal axis variables
   */
  protected boolean hasHorzVariablesPopup() {
    return false;
  }

  /**
   * Gets a popup menu with horizontal axis variables.
   * This default method returns null; subclasses should override to return
   * a popup with associated action for setting horizontal axis variable.
   *
   * @return the popup menu
   */
  protected JPopupMenu getHorzVariablesPopup() {
    return null;
  }

  /**
   * Reports whether this provides a popup menu for setting the vertical axis variable.
   *
   * @return true if this has a popup menu with vertical axis variables
   */
  protected boolean hasVertVariablesPopup() {
    return false;
  }

  /**
   * Gets a popup menu with vertical axis variables.
   * This default method returns null; subclasses should override to return
   * a popup with associated action for setting vertical axis variable.
   *
   * @return the popup menu
   */
  protected JPopupMenu getVertVariablesPopup() {
    return null;
  }

  /**
   * Finds the plot region containing the specified point.
   *
   * @param p the point
   * @return one of the static regions defined by CartesianInteractive
   */
  protected int findRegion(Point p) {
    int l = drawingPanel.getLeftGutter();
    int r = drawingPanel.getRightGutter();
    int t = drawingPanel.getTopGutter();
    int b = drawingPanel.getBottomGutter();
    Dimension plotDim = drawingPanel.getSize();
    // horizontal axis
    int axisLen = plotDim.width-r-l;
    hitRect.setSize(axisLen/4, 12);
    hitRect.setLocation(l+axisLen/2-hitRect.width/2, plotDim.height-b-hitRect.height/2);
    if(hitRect.contains(p)) {
      return HORZ_AXIS;
    }
    hitRect.setLocation(l+4, plotDim.height-b-hitRect.height/2);
    if(hitRect.contains(p)) {
      return HORZ_AXIS_MIN;
    }
    hitRect.setLocation(l+axisLen-hitRect.width-4, plotDim.height-b-hitRect.height/2);
    if(hitRect.contains(p)) {
      return HORZ_AXIS_MAX;
    }
    // vertical axis
    axisLen = plotDim.height-t-b;
    hitRect.setSize(12, axisLen/4);
    hitRect.setLocation(l-hitRect.width/2, t+axisLen/2-hitRect.height/2);
    if(hitRect.contains(p)) {
      return VERT_AXIS;
    }
    hitRect.setLocation(l-hitRect.width/2, t+4);
    if(hitRect.contains(p)) {
      return VERT_AXIS_MAX;
    }
    hitRect.setLocation(l-hitRect.width/2, t+axisLen-hitRect.height-4);
    if(hitRect.contains(p)) {
      return VERT_AXIS_MIN;
    }
    
    
    // horizontal variable
    Graphics g = drawingPanel.getGraphics();
    try {
	    int w = xLine.getWidth(g)+8;
	    int h = xLine.getHeight(g);
	    hitRect.setSize(w, h);
	    int x = (int) (xLine.getX()-w/2);
	    int y = (int) (xLine.getY()-h/2-xLine.getFontSize()/3);
	    hitRect.setLocation(x, y);
	    if(hitRect.contains(p)&&hasHorzVariablesPopup()) {
	      return HORZ_VAR;
	    }
	    // vertical variable: drawn sideways, so width<->height reversed
	    w = yLine.getHeight(g);
	    h = yLine.getWidth(g)+8;
	    hitRect.setSize(w, h);
	    x = (int) (yLine.getX()-w/2-yLine.getFontSize()/3);
	    y = (int) (yLine.getY()-h/2-1);
	    hitRect.setLocation(x, y);
	    if(hitRect.contains(p)&&hasVertVariablesPopup()) {
	      return VERT_VAR;
	    }
	    // inside
	    if(!((p.x<l)||(p.y<t)||(p.x>plotDim.width-r)||(p.y>plotDim.height-b))) {
	      return INSIDE;
	    }
	    // scale setter regions
	    ScientificField field = scaleSetter.scaleField;
	    Dimension fieldDim = field.getPreferredSize();
	    hitRect.setSize(fieldDim);
	    double xmin = drawingPanel.getXMin();
	    double xmax = drawingPanel.getXMax();
	    double ymin = drawingPanel.getYMin();
	    double ymax = drawingPanel.getYMax();
	    int offset = 8; // approx distance from axis to hitRect for scale setter 
	    // horizontal min
	    hitRect.setLocation(l-12, plotDim.height-b+6+offset);
	    if(hitRect.contains(p)) {
	      Point hitLoc = hitRect.getLocation(); // relative to plotPanel 
	      scaleSetter.add(scaleSetter.autoscaleCheckbox, BorderLayout.NORTH);
	      scaleSetter.validate();
	      Point fieldLoc = field.getLocation(); // relative to scaleSetter
	      Dimension size = scaleSetter.getPreferredSize();
	      scaleSetter.setBounds(hitLoc.x-fieldLoc.x, hitLoc.y-fieldLoc.y-offset, size.width, size.height);
	      return HORZ_MIN;
	    }
	    // horizontal max
	    hitRect.setLocation(plotDim.width-r-fieldDim.width+12, plotDim.height-b+6+offset);
	    if(hitRect.contains(p)) {
	      field.setExpectedRange(xmin, xmax);
	      Point hitLoc = hitRect.getLocation(); // relative to plotPanel  
	      scaleSetter.add(scaleSetter.autoscaleCheckbox, BorderLayout.NORTH);
	      scaleSetter.validate();
	      Point fieldLoc = field.getLocation(); // relative to scaleSetter
	      Dimension size = scaleSetter.getPreferredSize();
	      scaleSetter.setBounds(hitLoc.x-fieldLoc.x, hitLoc.y-fieldLoc.y-offset, size.width, size.height);
	      return HORZ_MAX;
	    }
	    // vertical min
	    hitRect.setLocation(l-fieldDim.width-1-offset, plotDim.height-b-fieldDim.height+8);
	    if(hitRect.contains(p)) {
	      field.setExpectedRange(ymin, ymax);
	      Point hitLoc = hitRect.getLocation(); // relative to plotPanel  
	      scaleSetter.add(scaleSetter.autoscaleCheckbox, BorderLayout.EAST);
	      scaleSetter.validate();
	      Point fieldLoc = field.getLocation(); // relative to scaleSetter
	      int minLoc = hitLoc.x-fieldLoc.x;
	      Dimension size = scaleSetter.getPreferredSize();
	      scaleSetter.setBounds(Math.max(minLoc, 1-fieldLoc.x), hitLoc.y-fieldLoc.y, size.width, size.height);
	      return VERT_MIN;
	    }
	    // vertical max
	    hitRect.setLocation(l-fieldDim.width-1-offset, t-8);
	    if(hitRect.contains(p)) {
	      field.setExpectedRange(ymin, ymax);
	      Point hitLoc = hitRect.getLocation(); // relative to plotPanel  
	      scaleSetter.add(scaleSetter.autoscaleCheckbox, BorderLayout.EAST);
	      scaleSetter.validate();
	      Point fieldLoc = field.getLocation(); // relative to scaleSetter
	      int minLoc = hitLoc.x-fieldLoc.x;
	      Dimension size = scaleSetter.getPreferredSize();
	      scaleSetter.setBounds(Math.max(minLoc, 1-fieldLoc.x), hitLoc.y-fieldLoc.y, size.width, size.height);
	      return VERT_MAX;
	    }
    } finally {
    	g.dispose();
    }
    return -1;
  }

  /**
   * Gets the scale setter.
   *
   * @return the ScaleSetter dialog
   */
  public ScaleSetter getScaleSetter() {
    // refresh autoscale checkbox text if needed (eg, new Locale)
    String s = DialogsRes.SCALE_AUTO;
    if(!s.equals(scaleSetter.autoscaleCheckbox.getText())) {
      scaleSetter.autoscaleCheckbox.setText(s);
    }
    return scaleSetter;
  }

  /**
   * A dialog with value field and autoscale checkbox.
   */
  public class ScaleSetter extends JPanel {
    Action scaleAction;
    JCheckBox autoscaleCheckbox;
    ScientificField scaleField;
    int region;             // determines which axis and end are active
    boolean pinned = false; // prevents hiding this when true

    private ScaleSetter() {
      super(new BorderLayout());
      scaleAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          scaleField.setBackground(Color.white);
          pinned = false;
          boolean auto = autoscaleCheckbox.isSelected();
          boolean horzAxis = true;
          double min = auto ? Double.NaN : scaleField.getValue();
          double max = min;
          switch(region) {
             case HORZ_MIN :
               max = drawingPanel.isAutoscaleXMax()? Double.NaN: drawingPanel.getXMax();
               break;
             case HORZ_MAX :
               min = drawingPanel.isAutoscaleXMin()? Double.NaN: drawingPanel.getXMin();
               break;
             case VERT_MIN :
               horzAxis = false;
               max = drawingPanel.isAutoscaleYMax()? Double.NaN: drawingPanel.getYMax();
               break;
             case VERT_MAX :
               horzAxis = false;
               min = drawingPanel.isAutoscaleYMin()? Double.NaN: drawingPanel.getYMin();
          }
          if(horzAxis) {
            drawingPanel.setPreferredMinMaxX(min, max);
          } else {
            drawingPanel.setPreferredMinMaxY(min, max);
          }
          Rectangle bounds = drawingPanel.getBounds();
          bounds.setLocation(0, 0);
          drawingPanel.paintImmediately(bounds);
        }

      };
      autoscaleCheckbox = new JCheckBox();
      autoscaleCheckbox.setBorder(BorderFactory.createEmptyBorder(1, 2, 2, 1));
      autoscaleCheckbox.setBackground(drawingPanel.getBackground());
      autoscaleCheckbox.setHorizontalTextPosition(SwingConstants.RIGHT);
      autoscaleCheckbox.addActionListener(scaleAction);
      scaleField = new ScientificField(6, 3) {
        public Dimension getPreferredSize() {
          Dimension dim = super.getPreferredSize();
          dim.width -= 4;
          return dim;
        }

      };
      scaleField.addActionListener(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          autoscaleCheckbox.setSelected(false);
          scaleAction.actionPerformed(null);
        }

      });
      scaleField.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          if(scaleField.getBackground()==Color.yellow) {
            autoscaleCheckbox.setSelected(false);
            scaleAction.actionPerformed(null);
          }
        }

      });
      scaleField.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          pinned = true;
          if(e.getClickCount()==2) {
            scaleField.selectAll();
          }
        }

      });
      add(scaleField, BorderLayout.CENTER);
    }

    void hideIfInactive() {
      if((scaleField.getBackground()!=Color.yellow)&&(scaleField.getSelectedText()==null)&&!pinned) {
        hideScaleSetter();
      }
    }

    void setRegion(int mouseRegion) {
      if(region!=mouseRegion) {
        autoscaleCheckbox.requestFocusInWindow();
        if(scaleField.getBackground()==Color.yellow) {
          autoscaleCheckbox.setSelected(false);
          scaleAction.actionPerformed(null);
        }
        region = mouseRegion;
        pinned = false;
        scaleField.select(20, 20); // clears selection and places caret at end
        scaleField.requestFocusInWindow();
      }
    }

  }

  /**
   * A mouse listener for handling interactivity
   */
  class AxisMouseListener extends javax.swing.event.MouseInputAdapter {
    public void mouseMoved(MouseEvent e) {
      if (!enabled) return; // Paco
      altDown = e.isAltDown();
      Point p = e.getPoint();
      mouseRegion = findRegion(p);
      if((mouseRegion>INSIDE)&&(mouseRegion<HORZ_AXIS)&&!drawingPanel.isFixedScale()) {
        getScaleSetter().setRegion(mouseRegion);
        scaleSetter.validate();
        scaleSetter.setVisible(true);
      } else {
        scaleSetter.hideIfInactive();
      }
      drawHitRect = ((mouseRegion==HORZ_VAR)||(mouseRegion==VERT_VAR));
      plot.repaint();
    }

    public void mouseDragged(MouseEvent e) {
      if (!enabled) return; // Paco
      double dx = 0, dy = 0, min = 0, max = 0;
      switch(mouseRegion) {
         case INSIDE :
           if(!altDown||drawingPanel.isFixedScale()) {
             return;
           }
           dx = (mouseLoc.x-e.getX())/plot.getXPixPerUnit();
           min = plot.getXMin()+dx;
           max = plot.getXMax()+dx;
           dx = 0;
           plot.setPreferredMinMaxX(min, max);
           dy = (e.getY()-mouseLoc.y)/plot.getYPixPerUnit();
           min = plot.getYMin()+dy;
           max = plot.getYMax()+dy;
           break;
         case HORZ_AXIS :
           dx = (mouseLoc.x-e.getX())/plot.getXPixPerUnit();
           min = plot.getXMin()+dx;
           max = plot.getXMax()+dx;
           break;
         case HORZ_AXIS_MIN :
           dx = 2*(mouseLoc.x-e.getX())/plot.getXPixPerUnit();
           min = plot.getXMin()+dx;
           max = plot.isAutoscaleXMax() ? Double.NaN : plot.getXMax();
           break;
         case HORZ_AXIS_MAX :
           dx = 2*(mouseLoc.x-e.getX())/plot.getXPixPerUnit();
           min = plot.isAutoscaleXMin() ? Double.NaN : plot.getXMin();
           max = plot.getXMax()+dx;
           break;
         case VERT_AXIS :
           dy = (e.getY()-mouseLoc.y)/plot.getYPixPerUnit();
           min = plot.getYMin()+dy;
           max = plot.getYMax()+dy;
           break;
         case VERT_AXIS_MIN :
           dy = 2*(e.getY()-mouseLoc.y)/plot.getYPixPerUnit();
           min = plot.getYMin()+dy;
           max = plot.isAutoscaleYMax() ? Double.NaN : plot.getYMax();
           break;
         case VERT_AXIS_MAX :
           dy = 2*(e.getY()-mouseLoc.y)/plot.getYPixPerUnit();
           min = plot.isAutoscaleYMin() ? Double.NaN : plot.getYMin();
           max = plot.getYMax()+dy;
           break;
      }
      if(dx!=0) {
        plot.setPreferredMinMaxX(min, max);
      } else if(dy!=0) {
        plot.setPreferredMinMaxY(min, max);
      }
      // Call any registered listener: Paco
      for (ActionListener listener : axisListeners) listener.actionPerformed(new ActionEvent(CartesianInteractive.this, e.getID(), "axis dragged")); //$NON-NLS-1$
      
      plot.invalidateImage();
      plot.repaint();
      mouseLoc = e.getPoint();
    }

    public void mousePressed(MouseEvent e) {
      if (!enabled) return; // Paco
    	plot.requestFocusInWindow();
      altDown = e.isAltDown();
      mouseLoc = e.getPoint();
      mouseX = plot.pixToX(plot.getMouseIntX());
      mouseY = plot.pixToY(plot.getMouseIntY());
      mouseRegion = findRegion(mouseLoc);
      if(scaleSetter==null) {
        return;
      }
      if((mouseRegion>INSIDE)&&(mouseRegion<HORZ_AXIS)&&!drawingPanel.isFixedScale()) {
        scaleSetter.setVisible(true);
        return;
      }
      hideScaleSetter();
      if(mouseRegion==HORZ_VAR) {
        drawHitRect = false;
        getHorzVariablesPopup().show(plot, mouseLoc.x-20, mouseLoc.y-12);
      } else if(mouseRegion==VERT_VAR) {
        drawHitRect = false;
        getVertVariablesPopup().show(plot, mouseLoc.x-20, mouseLoc.y-12);
      }
      plot.repaint();
    }

    public void mouseReleased(MouseEvent e) {
      if (!enabled) return; // Paco
      mouseX = Double.NaN;
      mouseY = Double.NaN;
    }

    public void mouseExited(MouseEvent e) {
      if (!enabled) return; // Paco
      Point p = e.getPoint();
      if(!new Rectangle(plot.getSize()).contains(p)&&(scaleSetter!=null)&&"".equals(InputEvent.getModifiersExText(e.getModifiersEx()))) { //$NON-NLS-1$
        hideScaleSetter();
      }
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
