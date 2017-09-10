/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2017  Douglas Brown
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

import java.awt.Graphics;
import java.util.ArrayList;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Measurable;
import org.opensourcephysics.media.core.Trackable;

/**
 * A PencilDrawing is a Trackable Trail.
 *
 * @author Douglas Brown
 */
public class PencilScene implements Trackable, Measurable, Comparable {
	
	ArrayList<PencilDrawing> drawings = new ArrayList<PencilDrawing>();
	boolean visible = true;
	int startframe=0, endframe=Integer.MAX_VALUE;
	
  /**
   * 
   * Constructs a PencilScene.
   */
	PencilScene() {
	}

	@Override
	public void draw(DrawingPanel panel, Graphics g) {
		if (!visible) return;
		for (PencilDrawing drawing: drawings) {
			drawing.draw(panel, g);
		}
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
   * Sets the start frame.
   *
   * @param start the desired start frame
   * @return the final start frame
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
   * @return the final end frame
   */
	public int setEndFrame(int end) {
		end = Math.max(startframe, end);
		endframe = end;
		return endframe;
	}
	
  /**
   * Sets the visibility of this scene.
   *
   * @param vis
   */
	public void setVisible(boolean vis) {
		visible = vis;;
	}

	@Override
	public double getXMin() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getXMin(): Math.min(d, drawing.getXMin());
		}
		return d;
	}

	@Override
	public double getXMax() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getXMax(): Math.max(d, drawing.getXMax());
		}
		return d;
	}

	@Override
	public double getYMin() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getYMin(): Math.min(d, drawing.getYMin());
		}
		return d;
	}

	@Override
	public double getYMax() {
		double d = 0;
		for (PencilDrawing drawing: drawings) {
			d = d==0? drawing.getYMax(): Math.max(d, drawing.getYMax());
		}
		return d;
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
	
  /**
   * Returns the XML.ObjectLoader for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load TDrawing data in an XMLControl.
   */
  private static class Loader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
    	PencilScene scene = (PencilScene) obj;
      control.setValue("frame_range", new int[] {scene.startframe, scene.endframe}); //$NON-NLS-1$
      control.setValue("drawings", scene.getDrawings()); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new PencilScene();
    }

    public Object loadObject(XMLControl control, Object obj) {
    	PencilScene scene = (PencilScene) obj;
    	int[] frames = (int[])control.getObject("frame_range"); //$NON-NLS-1$
    	if (frames!=null) {
    		scene.startframe = frames[0];
    		scene.endframe = frames[1];
    	}
    	ArrayList<PencilDrawing> drawings = (ArrayList<PencilDrawing>)control.getObject("drawings"); //$NON-NLS-1$
    	if (drawings!=null) {
    		for (PencilDrawing drawing: drawings) {
    			drawing.setPencilScene(scene);
    		}
    		scene.drawings = drawings;
    	}
      return scene;
    }

  }

}
