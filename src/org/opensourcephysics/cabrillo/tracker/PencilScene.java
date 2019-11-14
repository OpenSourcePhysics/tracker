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
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.media.core.Trackable;

/**
 * A PencilScene is a collection of PencilDrawings and an optional PencilCaption.
 *
 * @author Douglas Brown
 */
public class PencilScene implements Trackable, Comparable, Interactive {
		
	private PencilCaption caption;
	private ArrayList<PencilDrawing> drawings = new ArrayList<PencilDrawing>();
	private boolean visible = true;
	private boolean heavy;
  private TrackerPanel trackerPanel;
	private double margin;
	boolean isCaptionPositioned;
	int startframe=0, endframe=Integer.MAX_VALUE;
	
  /**
   * Constructor.
   */
	public PencilScene() {
    // create empty caption
    caption = new PencilCaption("", 0, 0, PencilCaption.baseFont); //$NON-NLS-1$
	}
	
	@Override
	public void draw(DrawingPanel panel, Graphics g) {
		if (!visible) return;
		if (panel instanceof TrackerPanel) {
	  	TrackerPanel trackerPanel = (TrackerPanel)panel;
	  	// don't draw on World Views
	  	if (!trackerPanel.isDrawingInImageSpace()) return;
	  	if (this.trackerPanel==null) {
	  		this.trackerPanel = trackerPanel;
	  	}
	  	if (!includesFrame(trackerPanel.getFrameNumber())) {
	  		return;
	  	}
		}
		for (PencilDrawing drawing: drawings) {
			drawing.draw(panel, g);
		}
		caption.draw(panel, g);
  }
	
  /**
   * Gets the drawings.
   *
   * @return the drawings
   */
	public ArrayList<PencilDrawing> getDrawings() {
		return drawings;
	}
	
  /**
   * Gets the caption. May return null.
   *
   * @return the caption
   */
	public PencilCaption getCaption() {
		return caption;
	}
	
  /**
   * Sets the caption.
   *
   * @param caption the caption.
   */
	public void setCaption(PencilCaption caption) {
		if (caption!=null) {
			this.caption = caption;
      isCaptionPositioned = true;
		}
	}
	
  /**
   * Sets the color of the caption and all drawings.
   *
   * @param color the color
   */
	public void setColor(Color color) {
		for (PencilDrawing drawing: drawings) {
			drawing.color = color;
		}
		caption.color = color;
	}
	
  /**
   * Sets the start frame.
   *
   * @param start the desired start frame
   * @return the resulting start frame
   */
	public int setStartFrame(int start) {
		start = Math.max(0, start);
		startframe = start;
		endframe = Math.max(startframe, endframe);
		return startframe;
	}
	
  /**
   * Sets the end frame.
   *
   * @param end the desired end frame
   * @return the resulting end frame
   */
	public int setEndFrame(int end) {
		end = Math.max(startframe, end);
		endframe = end;
		return endframe;
	}
	
  /**
   * Gets a compact description suitable for the PencilControl sceneDropdown.
   *
   * @param trackerPanel the TrackerPanel with videoclip data
   * @return the description
   */
	public String getDescription(TrackerPanel trackerPanel) {
		if (this==PencilControl.dummyScene) return null;
		int last = endframe;
		if (trackerPanel.isDisplayable() && Integer.MAX_VALUE==last) {
  	  last = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
		}
		String name = TrackerRes.getString("PencilScene.Description.Default"); //$NON-NLS-1$
		int len = Math.max(10, name.length());
		if (getCaption()!=null && !"".equals(getCaption().getText())) { //$NON-NLS-1$
			name = getCaption().getText();
		}
		if (name.length()>len) {
			name = name.substring(0, len)+"..."; //$NON-NLS-1$
		}
		String s = name+" ("+startframe+"-"+last+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return s;
	}
	
	public boolean includesFrame(int frame) {
		return startframe<=frame && endframe>=frame;
	}
	
  /**
   * Gets the visibility of this scene.
   *
   * @return true if visible
   */
	public boolean isVisible() {
		return visible;
	}
	
  /**
   * Sets the visibility of this scene.
   *
   * @param vis true for visible
   */
	public void setVisible(boolean vis) {
		visible = vis;
	}
	
  /**
   * Gets the heavy state of this scene. When heavy, bold fonts and heavy lines are displayed.
   *
   * @return true if heavy
   */
	public boolean isHeavy() {
		return heavy;
	}
	
  /**
   * Sets the heavy state of this scene.
   *
   * @param heavy true for heavy lines and bold font
   */
	public void setHeavy(boolean heavy) {
		this.heavy = heavy;
		for (PencilDrawing drawing: drawings) {
			drawing.setStroke(heavy? PencilDrawer.heavyStroke: PencilDrawer.lightStroke);
		}
  	if (getCaption()!=null) {
    	Font font = getCaption().getFont();
    	font = font.deriveFont(heavy? Font.BOLD: Font.PLAIN);
    	getCaption().setFont(font);
  	}
	}
	
	@Override
	public double getXMin() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getXMin(): Math.min(d, drawing.getXMin());
		}
		if (caption.isMeasured()) {
			d = d==0? caption.getXMin(): Math.min(d, caption.getXMin());
		}
		return d-margin;
	}

	@Override
	public double getXMax() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getXMax(): Math.max(d, drawing.getXMax());
		}
		if (caption.isMeasured()) {
			d = d==0? caption.getXMax(): Math.max(d, caption.getXMax());
		}
		return d+margin;
	}

	@Override
	public double getYMin() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getYMin(): Math.min(d, drawing.getYMin());
		}
		if (caption.isMeasured()) {
			d = d==0? caption.getYMin(): Math.min(d, caption.getYMin());
		}
		return d-margin;
	}

	@Override
	public double getYMax() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getYMax(): Math.max(d, drawing.getYMax());
		}
		if (caption.isMeasured()) {
			d = d==0? caption.getYMax(): Math.max(d, caption.getYMax());
		}
		return d+margin;
	}

	@Override
	public boolean isMeasured() {
		return true;
	}

	@Override
	public int compareTo(Object o) {
		PencilScene that = (PencilScene)o;
		int diff = this.startframe - that.startframe;
		return diff!=0? diff: (this.endframe - that.endframe);
	}
	
	@Override
	public Interactive findInteractive(DrawingPanel panel, int xpix, int ypix) {
		Interactive ia = caption.findInteractive(panel, xpix, ypix);
		if (ia!=null) {
	    if (Tracker.showHints && trackerPanel!=null) {
	  		trackerPanel.setMessage(TrackerRes.getString("PencilCaption.Hint")); //$NON-NLS-1$
	    }
			return ia;
		}
		return null;
	}

	@Override
	public void setEnabled(boolean enabled) {}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setXY(double x, double y) {
	}

	@Override
	public void setX(double x) {}

	@Override
	public void setY(double y) {}

	@Override
	public double getX() {
		return 0;
	}

	@Override
	public double getY() {
		return 0;
	}
	
  /**
   * Measures this scene to determine an appropriate margin.
   */
	protected void measure() {
		double xmin=0, xmax=0, ymin=0, ymax=0;
		for (PencilDrawing drawing: drawings) {
			xmin = xmin==0? drawing.getXMin(): Math.min(xmin, drawing.getXMin());
			xmax = xmax==0? drawing.getXMax(): Math.max(xmax, drawing.getXMax());
			ymin = ymin==0? drawing.getYMin(): Math.min(ymin, drawing.getYMin());
			ymax = ymax==0? drawing.getYMax(): Math.max(ymax, drawing.getYMax());
		}
		if (caption.isMeasured()) {
			xmin = xmin==0? caption.getXMin(): Math.min(xmin, caption.getXMin());
			xmax = xmax==0? caption.getXMax(): Math.max(xmax, caption.getXMax());
			ymin = ymin==0? caption.getYMin(): Math.min(ymin, caption.getYMin());
			ymax = ymax==0? caption.getYMax(): Math.max(ymax, caption.getYMax());
		}
		double range = Math.max(xmax-xmin, ymax-ymin);
		margin = 0.02*range;
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
    	PencilScene scene = (PencilScene) obj;
      control.setValue("frame_range", new int[] {scene.startframe, scene.endframe}); //$NON-NLS-1$
      if (!scene.getDrawings().isEmpty()) {
      	control.setValue("drawings", scene.getDrawings()); //$NON-NLS-1$
      }
      if (scene.getCaption()!=null && !"".equals(scene.getCaption().getText())) { //$NON-NLS-1$
      	control.setValue("caption", scene.getCaption()); //$NON-NLS-1$      	
      }
      if (scene.isHeavy()) {
      	control.setValue("heavy", scene.isHeavy()); //$NON-NLS-1$
      }
    }

    @Override
    public Object createObject(XMLControl control) {
      PencilScene scene = new PencilScene();
      return scene;
    }

    @Override
    public Object loadObject(XMLControl control, Object obj) {
    	PencilScene scene = (PencilScene) obj;
    	int[] frames = (int[])control.getObject("frame_range"); //$NON-NLS-1$
    	if (frames!=null) {
    		scene.startframe = frames[0];
    		scene.endframe = frames[1];
    	}
    	ArrayList<PencilDrawing> drawings = (ArrayList<PencilDrawing>)control.getObject("drawings"); //$NON-NLS-1$
    	if (drawings!=null) {
    		scene.drawings = drawings;
    	}
    	scene.setCaption((PencilCaption)control.getObject("caption")); //$NON-NLS-1$
    	// load heavy last so can apply to drawings and caption
    	scene.setHeavy(control.getBoolean("heavy")); //$NON-NLS-1$
      return scene;
    }
  }

}
