/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.axes.CoordinateStringBuilder;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.media.core.XYCoordinateStringBuilder;

/**
 * A CoordinateStringBuilder with format patterns and units.
 *
 * @author Douglas Brown
 */
public class TCoordinateStringBuilder 
		extends CoordinateStringBuilder
		implements XYCoordinateStringBuilder {
	
	NumberField xField, yField;
	
	public TCoordinateStringBuilder() {
		xField = new NumberField(0);
		yField = new NumberField(0);
	}
	
  @Override
	public String getCoordinateString(DrawingPanel panel, MouseEvent e) {
    if (panel instanceof TrackerPanel) {
    	TrackerPanel trackerPanel = (TrackerPanel) panel;
	    Point2D pt = trackerPanel.getWorldMousePoint();
	    return getCoordinateString(trackerPanel, pt.getX(), pt.getY());
    }
    double x = panel.pixToX(e.getPoint().x);
    double y = panel.pixToY(e.getPoint().y);
  	return getCoordinateString(null, x, y);
  }
  
  @Override
	public String getCoordinateString(VideoPanel vidPanel, double x, double y) {
    xField.setFormatFor(x);
    String xStr = xField.format(x);
    if(xField.getUnits()!=null) xStr += xField.getUnits();
    yField.setFormatFor(y);
    String yStr = yField.format(y);
    if(yField.getUnits()!=null) yStr += yField.getUnits();
  	return xLabel+xStr+yLabel+yStr;
  }
  
  public void setUnitsAndPatterns(TTrack track, String xVar, String yVar) {
  	if (track==null || track.tp==null) return;
    xField.setUnits(track.tp.getUnits(track, xVar));    
    yField.setUnits(track.tp.getUnits(track, yVar));
    xField.setFixedPattern(track.getVarFormatPattern(xVar));
    yField.setFixedPattern(track.getVarFormatPattern(yVar));
  }

  @Override
  public void setCoordinateLabels(String xLabel, String yLabel) {
    this.xLabel = xLabel;
    this.yLabel = yLabel;
  }
  
  public void refreshDecimalSeparators() {
  	xField.refreshDecimalSeparators(true);
  	yField.refreshDecimalSeparators(true);
  }

}
