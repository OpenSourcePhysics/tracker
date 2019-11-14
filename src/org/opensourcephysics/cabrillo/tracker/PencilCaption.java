/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.InteractiveTextLine;

/**
 * A PencilCaption is a text caption for a PencilScene.
 *
 * @author Douglas Brown
 */
public class PencilCaption extends InteractiveTextLine {
	
	private static Cursor handCursor;
  static Font baseFont = new Font("SansSerif", Font.PLAIN, 32); //$NON-NLS-1$
	static {
    ImageIcon icon = new ImageIcon(
        Tracker.class.getResource("resources/images/hand_cursor.gif")); //$NON-NLS-1$
		handCursor = GUIUtils.createCustomCursor(icon.getImage(), new Point(5, 0), 
    	"Hand", Cursor.DEFAULT_CURSOR); //$NON-NLS-1$ 
	  XML.setLoader(PencilCaption.class, PencilCaption.getLoader());
	}
	
	private AffineTransform transform = new AffineTransform();
	private Rectangle2D bounds;
  private double offsetX, offsetY;
  private boolean isSelected;
	private double xMax, xMin, yMax, yMin;
	private Graphics graphics;

  /**
   * Constructs a PencilCaption with specified text, position and font.
   *
   * @param text the text
   * @param x the x-position
   * @param y the y-position
   * @param font the font
   */
	public PencilCaption(String text, double x, double y, Font font) {
		super(text, x, y);
		setFont(font);
	}

	@Override
	public void draw(DrawingPanel panel, Graphics g) {
		if (graphics==null) refreshBounds(g);
		graphics = g.create();
		if (getText()==null || getText().trim().equals("")) return; //$NON-NLS-1$
  	if (panel instanceof TrackerPanel) {
	  	TrackerPanel trackerPanel = (TrackerPanel)panel;
	  	if (trackerPanel.isDrawingInImageSpace()) {
	  		refreshBounds(g);
	  		Color c = g.getColor();
	  		g.setColor(color);
	  		AffineTransform toPixels = panel.getPixelTransform();
	      Point2D pt = new Point2D.Double(x, y);
	      pt = toPixels.transform(pt, pt);
	    	double mag = trackerPanel.getMagnification();
	      Graphics2D g2 = (Graphics2D) g;
	      g2.translate(pt.getX(), pt.getY());
	  	  g2.scale(mag, mag);
	      textLine.drawText(g2, 0, 0);
	  	  g2.scale(1/mag, 1/mag);
	      g2.translate(-pt.getX(), -pt.getY());
	      g.setColor(c);
	  	}
  	}
  	else {
  		Color c = g.getColor();
  		g.setColor(color);
  		AffineTransform toPixels = panel.getPixelTransform();
      Point2D pt = new Point2D.Double(x, y);
      pt = toPixels.transform(pt, pt);
    	double mag = panel.getXPixPerUnit();
      Graphics2D g2 = (Graphics2D) g;
      g2.translate(pt.getX(), pt.getY());
  	  g2.scale(mag, mag);
      textLine.drawText(g2, 0, 0);
  	  g2.scale(1/mag, 1/mag);
      g2.translate(-pt.getX(), -pt.getY());
      g.setColor(c);
  	}
  }
	
  @Override
  public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
  	if (!isEnabled() || !(panel instanceof TrackerPanel)) return null;
  	if ("".equals(getText().trim())) return null; //$NON-NLS-1$
  	if (bounds==null) return null;
  	TrackerPanel trackerPanel = (TrackerPanel)panel;
  	if (!PencilDrawer.isDrawing(trackerPanel)) return null;
  	double mag = trackerPanel.getMagnification();
  	transform.setToTranslation(panel.xToPix(x-bounds.getWidth()/2), panel.yToPix(y-bounds.getHeight()));
  	transform.scale(mag, mag);
    Shape s = transform.createTransformedShape(bounds);
    if (s.contains(xpix, ypix)) {
    	return this;
    }
  	return null;
  }
  
  @Override
  public void setText(String text) {
  	super.setText(text);
  	refreshBounds(graphics);
  }
  
  @Override
  public void setFont(Font font) {
  	super.setFont(font);
  	refreshBounds(graphics);
  }
  
  @Override
  public boolean isMeasured() {
    return !"".equals(getText()); //$NON-NLS-1$
  }

  @Override
  public double getXMin() {
    return xMin;
  }

  @Override
  public double getXMax() {
    return xMax;
  }

  @Override
  public double getYMin() {
    return yMin;
  }

  @Override
  public double getYMax() {
    return yMax;
  }
  
  /**
   * Handles mouse events for a TrackerPanel.
   *
   * @param trackerPanel the TrackerPanel
   * @param e the mouse event
   */
  protected boolean handleMouseAction(MouseEvent e, TrackerPanel trackerPanel) {
  	// set cursor to custom hand cursor that mimics System hand cursor
    trackerPanel.setMouseCursor(handCursor);

    switch(trackerPanel.getMouseAction()) {

      case InteractivePanel.MOUSE_MOVED:
	  		break;
	  		
      case InteractivePanel.MOUSE_PRESSED:
      	isSelected = true;
      	offsetX = trackerPanel.getMouseX()-x;
      	offsetY = trackerPanel.getMouseY()-y;
	  		PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
	  		PencilScene scene = drawer.getSceneWithCaption(this);
	  		drawer.drawingControl.setSelectedScene(scene);
        break;
      	
      case InteractivePanel.MOUSE_DRAGGED:
      	if (isSelected) {
		  		setXY(trackerPanel.getMouseX()-offsetX, trackerPanel.getMouseY()-offsetY);
		  		trackerPanel.repaint();
      	}
        break;

      case InteractivePanel.MOUSE_RELEASED:
      	isSelected = false;
        return true;
      	
      case InteractivePanel.MOUSE_ENTERED:
      	isSelected = false;
        break;
      	
      case InteractivePanel.MOUSE_EXITED:
      	isSelected = false;
        break;
      	
      case InteractivePanel.MOUSE_CLICKED:
      	isSelected = false;
        break;
      	  
    }
    return false;
  }
  
  /**
   * Refreshes the string bounds and min/max values.
   *
   * @param g the Graphics drawing this caption
   */
  private void refreshBounds(Graphics g) {
  	if (g==null) return;
    bounds = textLine.getStringBounds(g);
    xMax = x+bounds.getWidth()/2;
    xMin = x-bounds.getWidth()/2;
    yMax = y+bounds.getHeight()/2;
    yMin = y-bounds.getHeight();
    bounds.setRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()*0.4);
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
   * A class to save and load PencilScene data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    @Override
    public void saveObject(XMLControl control, Object obj) {
    	PencilCaption caption = (PencilCaption) obj;
    	control.setValue("text", caption.getText()); //$NON-NLS-1$      	
    	control.setValue("font_size", caption.getFont().getSize()); //$NON-NLS-1$      	
    	control.setValue("colorRGB", caption.color.getRGB()); //$NON-NLS-1$
    	double[] loc = new double[] {caption.getX(), caption.getY()};
    	control.setValue("position", loc); //$NON-NLS-1$     	
    }

    @Override
    public Object createObject(XMLControl control) {
    	String text = control.getString("text"); //$NON-NLS-1$
    	double[] loc = (double[])control.getObject("position"); //$NON-NLS-1$
    	int size = control.getInt("font_size"); //$NON-NLS-1$
    	float fontSize = size;
    	Font font = baseFont.deriveFont(fontSize);
      return new PencilCaption(text, loc[0], loc[1], font);
    }

    @Override
    public Object loadObject(XMLControl control, Object obj) {
    	PencilCaption caption = (PencilCaption) obj;
    	caption.color = new Color(control.getInt("colorRGB")); //$NON-NLS-1$
    	return caption;
    }
  }

}
