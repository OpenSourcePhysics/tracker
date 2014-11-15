/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;

/**
 * A general purpose mouse handler for a trackerPanel.
 *
 * @author Douglas Brown
 */
public class TMouseHandler implements InteractiveMouseHandler {

  // static fields
  static Cursor markPointCursor;
	static Cursor autoTrackCursor;
	static Cursor autoTrackMarkCursor;

  // instance fields
  Interactive iad = null;
  TPoint p = null;
  boolean stepCreated = false, autoTracked = false;
  boolean marking;
  TTrack selectedTrack;
  int frameNumber;
  Point mousePtRelativeToViewRect = new Point(); // starting position of mouse
  Point viewLoc = new Point(); // starting position of view rect
  Dimension dim = new Dimension();
  
  static {
    ImageIcon icon = new ImageIcon(
        Tracker.class.getResource("resources/images/creatept.gif")); //$NON-NLS-1$
    markPointCursor = Toolkit.getDefaultToolkit().createCustomCursor(
        icon.getImage(), new Point(8, 8), TrackerRes.getString("Tracker.Cursor.Crosshair.Description")); //$NON-NLS-1$  	
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/autotrack.gif")); //$NON-NLS-1$
    autoTrackCursor = Toolkit.getDefaultToolkit().createCustomCursor(
        icon.getImage(), new Point(9, 9), TrackerRes.getString("PointMass.Cursor.Autotrack.Description")); //$NON-NLS-1$  	
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/autotrack_mark.gif")); //$NON-NLS-1$
    autoTrackMarkCursor = Toolkit.getDefaultToolkit().createCustomCursor(
        icon.getImage(), new Point(9, 9), TrackerRes.getString("PointMass.Cursor.Autotrack.Description")); //$NON-NLS-1$  	
  }

  /**
   * Handles a mouse action for a tracker panel.
   *
   * @param panel the tracker panel
   * @param e the mouse event
   */
  public void handleMouseAction(InteractivePanel panel,
                                MouseEvent e) {
    if (!(panel instanceof TrackerPanel)) return;
    // popup menus handled by MainTView class
  	if (OSPRuntime.isPopupTrigger(e)
  			|| panel.getZoomBox().isVisible()) return;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    if (!trackerPanel.isDrawingInImageSpace()) return;
    KeyboardFocusManager focuser =
      	KeyboardFocusManager.getCurrentKeyboardFocusManager();
    Component focusOwner = focuser.getFocusOwner();
		AutoTracker autoTracker = trackerPanel.getAutoTracker();
  	if (autoTracker.getTrack()==null)
  		autoTracker.setTrack(trackerPanel.getSelectedTrack());
   
    switch(trackerPanel.getMouseAction()) {

      // request focus and identify TPoints when moving mouse
      case InteractivePanel.MOUSE_MOVED:
//        // request focus from owners that are floating non-modal dialogs
//        // unless they are tools
//        if (focusOwner != null &&
//            focusOwner != trackerPanel &&
//            !(focusOwner instanceof javax.swing.text.JTextComponent)) {
//          String name = "unknownTool"; //$NON-NLS-1$
//          JFrame frame = null;
//          if (focusOwner instanceof JFrame) {
//            frame = (JFrame) focusOwner;
//          }
//          else if (focusOwner instanceof JComponent) {
//            Container c = ((JComponent)focusOwner).getTopLevelAncestor();
//            if (c instanceof JFrame) {
//              frame = (JFrame) c;
//            }
//          }
//          if (frame != null) name = frame.getName();
//          if (name.indexOf("Tool") == -1 && !(frame instanceof DrawingFrame)) { //$NON-NLS-1$
//            trackerPanel.requestFocusInWindow();
//          }
//        }
        selectedTrack = trackerPanel.getSelectedTrack();
        frameNumber = trackerPanel.getFrameNumber();
        iad = trackerPanel.getInteractive();
        boolean invertCursor = e.isShiftDown();
        marking = trackerPanel.setCursorForMarking(invertCursor, e);
        if (selectedTrack!=null && marking!= selectedTrack.isMarking) {
        	selectedTrack.setMarking(marking);
        	selectedTrack.repaint(trackerPanel);
        }
        if (marking) {
        	iad = null;
        }
        if (selectedTrack!=null) {
        	if (autoTracker.getWizard().isVisible() && autoTracker.getTrack()==selectedTrack) {
	        	Step step = selectedTrack.getStep(frameNumber);
	        	if (step!=null) {
	        		selectedTrack.repaint(step);
	        	}
	        }
        }
        if (TTrackBar.outOfMemory) {
        	TTrackBar.refreshMemoryButton();
        }
        break;

        // create and/or select/deselect TPoints by pressing mouse
      case InteractivePanel.MOUSE_PRESSED:
      	if (Tracker.startupHintShown) {
        	Tracker.startupHintShown = false;
        	trackerPanel.setMessage(""); //$NON-NLS-1$
      	}
        TrackControl.getControl(trackerPanel).popup.setVisible(false);
        marking = selectedTrack!=null 
        		&& trackerPanel.getCursor()==selectedTrack.getMarkingCursor(e);
        AutoTracker.KeyFrame keyFrame = getActiveKeyFrame(autoTracker);
        if (marking) {
        	iad = null;
        	boolean autoTrigger = isAutoTrackTrigger(e);
          // create step
          frameNumber = trackerPanel.getFrameNumber();
          Step step = selectedTrack.getStep(frameNumber); // may be null for point mass, offset origin, calibration pts
          int index = selectedTrack.getTargetIndex();
        	int nextIndex = index;
          if (step==null || !autoTrigger) {
          	if (autoTrigger) {
          		selectedTrack.autoMarkAt(frameNumber,
                  trackerPanel.getMouseX(), trackerPanel.getMouseY());
              step = selectedTrack.getStep(frameNumber);          		
//	          	if (step!=null) {
//	          		TPoint[] pts = step.getPoints();
//	          		// increment target index 
//	              if (pts.length>index+1) nextIndex = index+1;
//	          	}
          	}
          	else {
          		boolean newStep = step==null;
	        		step = selectedTrack.createStep(frameNumber,
	                trackerPanel.getMouseX(), trackerPanel.getMouseY());
          		TPoint[] pts = step.getPoints();
          		// increment target index if new step
              if (newStep && pts.length>index+1) nextIndex = index+1;
          	}
          }
          else if (step.getPoints()[index]==null) {
          	if (keyFrame!=null) {
          		TPoint target = keyFrame.getTarget();
          		target.setXY(trackerPanel.getMouseX(), trackerPanel.getMouseY());
          	}
        		selectedTrack.autoMarkAt(frameNumber,
                trackerPanel.getMouseX(), trackerPanel.getMouseY());
        		TPoint[] pts = step.getPoints();
        		// increment target index if possible 
            if (pts.length>index+1) nextIndex = index+1;
          }
          // if autotrack trigger, add key frame to autotracker
          if (isAutoTrackTrigger(e) && step!=null && step.getPoints()[index]!=null) {
          	TPoint target = step.getPoints()[index];
          	// remark step target if Axes/Tape/Protractor/Perspective selected and no keyframe exists
          	if (selectedTrack instanceof CoordAxes 
          			|| selectedTrack instanceof TapeMeasure 
          			|| selectedTrack instanceof PerspectiveTrack 
          			|| selectedTrack instanceof Protractor) {
          		if (autoTracker.getTrack()==selectedTrack) {
        	    	AutoTracker.FrameData frame = autoTracker.getFrame(frameNumber);
		          	if (frame.getKeyFrame()==null) {
		          		target.setXY(trackerPanel.getMouseX(), trackerPanel.getMouseY());
		          	}
          		}
          	}
          	autoTracker.addKeyFrame(target, 
          		trackerPanel.getMouseX(), trackerPanel.getMouseY());
    	  		TTrackBar.getTrackbar(trackerPanel).refresh();
          }
          
          if (step!=null && !isAutoTrackTrigger(e)) {
            trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.
                HAND_CURSOR));
            trackerPanel.setSelectedPoint(step.getDefaultPoint());
            selectedTrack.repaint(step);
            iad = p = trackerPanel.getSelectedPoint();
            stepCreated = keyFrame==null;
          }
          
          selectedTrack.setTargetIndex(nextIndex);
          autoTracker.getWizard().refreshGUI();
        }
        else if (iad instanceof TPoint) {
          // select a clicked TPoint
          p = (TPoint)iad;
      		// find associated step
      		Step step = null;
          for (TTrack track: trackerPanel.getTracks()) {
          	step = track.getStep(p, trackerPanel);
          	if (step != null) {
          		break;
          	}
          }
          
          // if control-down, manage trackerPanel.selectedSteps
      		boolean isStepSelected = trackerPanel.selectedSteps.contains(step);
        	if (e.isControlDown()) {
        		if (isStepSelected) {
        			// deselect point and remove step from selectedSteps
        			p = null;
        			trackerPanel.selectedSteps.remove(step);
        		}
        		else { // the step is not yet in selectedSteps
              if (!trackerPanel.selectedSteps.isEmpty()) {
              	// set selectedPoint to null if multiple steps are selected
          			p = null;              	
              }
        			trackerPanel.selectedSteps.add(step);
        		}
        	}
        	// else if not control-down, check if this step is in selectedSteps
        	else {
        		if (trackerPanel.selectedSteps.contains(step)) {
        			// do nothing: point is selected and step is in selectedSteps
        		}
        		else {
        			// deselect all selectedSteps
        			for (Step next: trackerPanel.selectedSteps) {
        				next.erase();
        			}
	        		trackerPanel.selectedSteps.clear();
	        		// select this point's step
	        		trackerPanel.selectedSteps.add(step);
        		}
        	}
        	if (step!=null) step.erase();
        	
          if (p instanceof Step.Handle) {
            ((Step.Handle)p).setPositionOnShaft(e.getX(), e.getY(), trackerPanel);
          }
          else if (p instanceof AutoTracker.Handle) {
            ((AutoTracker.Handle)p).setScreenLocation(e.getX(), e.getY(), trackerPanel);
          }
          if (p!=null) {
          	p.showCoordinates(trackerPanel);
          	
          }
          trackerPanel.setSelectedPoint(p);
        }
        else { // no interactive
        	boolean pointSelected = (trackerPanel.getSelectedPoint()!=null);
        	if (pointSelected) {
	          // deselect the selected point--this will post undoable edit
	          trackerPanel.setSelectedPoint(null);
        	}
        	else if (trackerPanel.selectedSteps.isChanged()) {
        		Undo.postStepSetEdit(trackerPanel.selectedSteps, trackerPanel.selectedSteps.getUndoControl());    		
        	}
        	// deselect selected steps, if any
        	for (Step step: trackerPanel.selectedSteps) {
        		step.erase();
        	}
        	trackerPanel.selectedSteps.clear();
        	
          if (!trackerPanel.isShowCoordinates()) {
            trackerPanel.hideMouseBox();
            trackerPanel.setMouseCursor(Cursor.getDefaultCursor());
          }
          if (e.getClickCount() == 2) {
            trackerPanel.setSelectedTrack(null);
          }
        	Rectangle rect = trackerPanel.scrollPane.getViewport().getViewRect();
        	viewLoc.setLocation(rect.getLocation());
        	Point p = e.getPoint();
        	mousePtRelativeToViewRect.setLocation(p.x-rect.x, p.y-rect.y);
        	trackerPanel.scrollPane.getViewport().getView().getSize(dim);
        	if (dim.width>rect.width || dim.height>rect.height)
        		trackerPanel.setMouseCursor(Tracker.grabCursor);
        }
        break;

        // move TPoints by dragging mouse
      case InteractivePanel.MOUSE_DRAGGED:
        p = trackerPanel.getSelectedPoint();
        TTrack track = trackerPanel.getSelectedTrack();
        if (p != null) {
        	int dx=0, dy=0;
        	if (track!=null && track.isLocked() && !(track instanceof VectorSum)) {
        		Toolkit.getDefaultToolkit().beep();
        		return;
        	}
      		// move p to current mouse location
        	p.setAdjusting(true);
        	Point scrPt = p.getScreenPosition(trackerPanel);
        	dx = e.getX() - scrPt.x;
        	dy = e.getY() - scrPt.y;
          p.setScreenPosition(e.getX(), e.getY(), trackerPanel, e);
          p.showCoordinates(trackerPanel);
        	// move other TPoints associated with selectedSteps by same amount 
        	for (Step step: trackerPanel.selectedSteps) {
        		p = step.points[0];
        		if (p==trackerPanel.getSelectedPoint()) continue;
	        	p.setAdjusting(true);
	        	scrPt = p.getScreenPosition(trackerPanel);
	          p.setScreenPosition(scrPt.x+dx, scrPt.y+dy, trackerPanel, e);
        	}
        }
        else if (trackerPanel.getCursor()!=Tracker.zoomInCursor
        		&& trackerPanel.getCursor()!=Tracker.zoomOutCursor){
        	Point p = e.getPoint();
        	Rectangle rect = trackerPanel.scrollPane.getViewport().getViewRect();
        	trackerPanel.scrollPane.getViewport().getView().getSize(dim);
          int dx = mousePtRelativeToViewRect.x-p.x+rect.x;
          int dy = mousePtRelativeToViewRect.y-p.y+rect.y;
        	int x = Math.max(0, viewLoc.x+dx);
        	x = Math.min(x, dim.width-rect.width);
        	int y = Math.max(0, viewLoc.y+dy);
        	y = Math.min(y, dim.height-rect.height);
        	if (x!=rect.x || y!=rect.y) {
            trackerPanel.setMouseCursor(Tracker.grabbedCursor);
        		rect.x = x;
        		rect.y = y;
        		trackerPanel.scrollRectToVisible(rect);
        	}
        	else {
          	viewLoc.setLocation(rect.getLocation());
          	mousePtRelativeToViewRect.setLocation(p.x-rect.x, p.y-rect.y);        		
        	}
        }
        if (trackerPanel.getSelectedStep() == null)
          trackerPanel.repaint();
        break;

        // snap vectors and/or autoAdvance when releasing mouse
      case InteractivePanel.MOUSE_RELEASED:
        trackerPanel.setMouseCursor(Cursor.getDefaultCursor());
        trackerPanel.requestFocusInWindow();
        p = trackerPanel.getSelectedPoint();
        if (p != null) {
        	p.setAdjusting(false);
        	if (p instanceof VectorStep.Handle) {
            ((VectorStep.Handle)p).snap(trackerPanel);
          }
        }
        // if autoAdvance, advance to next frame after step creation
        if (stepCreated && selectedTrack != null
            && selectedTrack.isAutoAdvance()) {
          trackerPanel.getPlayer().step();
          trackerPanel.hideMouseBox();
          stepCreated = false;
        }
    		autoTracked = false;
        break;
        
      case InteractivePanel.MOUSE_ENTERED:
        // request focus from owners other than text fields
        if (focusOwner != null && !(focusOwner instanceof JTextComponent)) {
        	trackerPanel.requestFocusInWindow();
        }
    }
  }
  
  protected static boolean isAutoTrackTrigger(InputEvent e) {
  	if (e.isControlDown()) return true;
  	return false;
  }

  protected AutoTracker.KeyFrame getActiveKeyFrame(AutoTracker autoTracker) {
		if (selectedTrack!=null && autoTracker.getWizard().isVisible()
				&& autoTracker.getTrack()==selectedTrack) {
  		AutoTracker.FrameData frame = autoTracker.getFrame(frameNumber);
			if (frame!=null && frame.getKeyFrame()==frame) {
				return (AutoTracker.KeyFrame)frame;
			}
		}
  	return null;
  }
}
