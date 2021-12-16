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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.opensourcephysics.cabrillo.tracker.AutoTracker.FrameData;
import org.opensourcephysics.cabrillo.tracker.AutoTracker.KeyFrameData;
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

	static final int STATE_MARK = 1;
	static final int STATE_AUTO = 2;
	static final int STATE_AUTOMARK = 3;

	// instance fields
	Interactive iad = null;
	TPoint selectedPoint = null;
	boolean stepCreated = false, autoTracked = false;
	boolean marking;
	TTrack selectedTrack;
	int frameNumber;
	Point mousePtRelativeToViewRect = new Point(); // starting position of mouse
	Point viewLoc = new Point(); // starting position of view rect
	Dimension dim = new Dimension();

	static {
		ImageIcon icon = (ImageIcon) Tracker.getResourceIcon("creatept.gif", false); //$NON-NLS-1$
		markPointCursor = GUIUtils.createCustomCursor(icon.getImage(), new Point(8, 8),
				TrackerRes.getString("Tracker.Cursor.Crosshair.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$
		icon = (ImageIcon) Tracker.getResourceIcon("autotrack.gif", false); //$NON-NLS-1$
		autoTrackCursor = GUIUtils.createCustomCursor(icon.getImage(), new Point(9, 9),
				TrackerRes.getString("PointMass.Cursor.Autotrack.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$
		icon = (ImageIcon) Tracker.getResourceIcon("autotrack_mark.gif", false); //$NON-NLS-1$
		autoTrackMarkCursor = GUIUtils.createCustomCursor(icon.getImage(), new Point(9, 9),
				TrackerRes.getString("Tracker.Cursor.Autotrack.Keyframe.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$
	}

	/**
	 * Handles a mouse action for a tracker panel.
	 *
	 * @param panel the tracker panel
	 * @param e     the mouse event
	 */
	@Override
	public void handleMouseAction(InteractivePanel panel, MouseEvent e) {
		if (!(panel instanceof TrackerPanel))
			return;
		TrackerPanel trackerPanel = (TrackerPanel) panel;

		// popup menus handled by MainTView class
		if (OSPRuntime.isPopupTrigger(e) || panel.getZoomBox().isVisible()) {
			iad = null;
			return;
		}

		if (!trackerPanel.isDrawingInImageSpace())
			return;

		// pencil drawing actions handled by PencilDrawer
		if (PencilDrawer.isDrawing(trackerPanel)) {
			PencilDrawer.getDrawer(trackerPanel).handleMouseAction(e);
			return;
		}

		KeyboardFocusManager focuser = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		Component focusOwner = focuser.getFocusOwner();
		// BH! Do we always have to do this?

		AutoTracker autoTracker = trackerPanel.getAutoTracker(false);
		if (autoTracker != null && autoTracker.getTrack() == null)
			autoTracker.setTrack(trackerPanel.getSelectedTrack());
		switch (trackerPanel.getMouseAction()) {

		// request focus and identify TPoints when moving mouse
		case InteractivePanel.MOUSE_MOVED:
			selectedTrack = trackerPanel.getSelectedTrack();
			frameNumber = trackerPanel.getFrameNumber();
			iad = trackerPanel.getInteractive();
			boolean invertCursor = e.isShiftDown();
			marking = trackerPanel.setCursorForMarking(invertCursor, e);
			if (selectedTrack != null && marking != selectedTrack.isMarking) {
				selectedTrack.setMarking(marking);
			}
			if (marking) {
				iad = null;
				if (selectedTrack != null && selectedTrack.ttype == TTrack.TYPE_TAPEMEASURE) {
					TapeMeasure tape = (TapeMeasure) selectedTrack;
					if (tape.isIncomplete) {
						// this call refreshes the position of end2 but leaves tape incomplete
						tape.createStep(frameNumber, 0, 0, trackerPanel.getMouseX(), trackerPanel.getMouseY());
					}
				}
			}
			if (selectedTrack != null) {
				if (autoTracker != null && autoTracker.getWizard().isVisible()
						&& autoTracker.getTrack() == selectedTrack) {
					Step step = selectedTrack.getStep(frameNumber);
					if (step != null) {
						selectedTrack.repaintStep(step);
					}
				}
			}
			if (OSPRuntime.outOfMemory) {
				TToolBar.refreshMemoryButton(trackerPanel);
			}
			break;

		// create and/or select/deselect TPoints by pressing mouse
		case InteractivePanel.MOUSE_PRESSED:
			if (Tracker.startupHintShown) {
				Tracker.startupHintShown = false;
				trackerPanel.setMessage(""); //$NON-NLS-1$
			}
			TrackControl.getControl(trackerPanel).popup.setVisible(false);
			marking = selectedTrack != null && trackerPanel.getCursor() == selectedTrack.getMarkingCursor(e);
			if (marking) {
				markPoint(trackerPanel, e, autoTracker);
				return;
			}
			if (iad instanceof TPoint) {
				selectPoint(trackerPanel, e);
				return;
			}
			clearInteractive(trackerPanel, e);
			return;
		case InteractivePanel.MOUSE_DRAGGED:
			// move TPoints by dragging mouse
			selectedPoint = trackerPanel.getSelectedPoint();
			TTrack track = trackerPanel.getSelectedTrack();
			if (selectedPoint != null) {
				int dx = 0, dy = 0;
				if (track != null && track.isLocked() && !(track instanceof VectorSum)) {
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				// move p to current mouse location
				selectedPoint.setAdjusting(true, e);
				Point scrPt = selectedPoint.getScreenPosition(trackerPanel);
				dx = e.getX() - scrPt.x;
				dy = e.getY() - scrPt.y;
				selectedPoint.setScreenPosition(e.getX(), e.getY(), trackerPanel, e);
				selectedPoint.showCoordinates(trackerPanel);
				// move other TPoints associated with selectedSteps by same amount
				trackerPanel.selectedSteps.setChanged(true);
				for (Step step : trackerPanel.selectedSteps) {
					selectedPoint = step.points[0];
					if (selectedPoint == trackerPanel.getSelectedPoint())
						continue;
					selectedPoint.setAdjusting(true, e);
					scrPt = selectedPoint.getScreenPosition(trackerPanel);
					selectedPoint.setScreenPosition(scrPt.x + dx, scrPt.y + dy, trackerPanel, e);
				}
			} else if (!Tracker.isZoomInCursor(trackerPanel.getCursor())
					&& !Tracker.isZoomOutCursor(trackerPanel.getCursor())) {
				Rectangle rect = trackerPanel.scrollPane.getViewport().getViewRect();
				trackerPanel.scrollPane.getViewport().getView().getSize(dim);
				int dx = mousePtRelativeToViewRect.x - e.getPoint().x + rect.x;
				int dy = mousePtRelativeToViewRect.y - e.getPoint().y + rect.y;
				int x = Math.max(0, viewLoc.x + dx);
				x = Math.min(x, dim.width - rect.width);
				int y = Math.max(0, viewLoc.y + dy);
				y = Math.min(y, dim.height - rect.height);
				if (x != rect.x || y != rect.y) {
					trackerPanel.setMouseCursor(Tracker.grabCursor);
					rect.x = x;
					rect.y = y;
					trackerPanel.scrollRectToVisible(rect);
				} else {
					viewLoc.setLocation(rect.getLocation());
					mousePtRelativeToViewRect.setLocation(e.getPoint().x - rect.x, e.getPoint().y - rect.y);
				}
			}
			if (trackerPanel.getSelectedStep() == null)
				TFrame.repaintT(trackerPanel);
			break;
		case InteractivePanel.MOUSE_RELEASED:
			// snap vectors and/or autoAdvance when releasing mouse
			Cursor c = trackerPanel.getCursor();
			if (!Tracker.isZoomInCursor(c) && !Tracker.isZoomOutCursor(c)) {
				trackerPanel.setMouseCursor(Cursor.getDefaultCursor());
			}
			trackerPanel.requestFocusInWindow();
			selectedPoint = trackerPanel.getSelectedPoint();
			if (selectedPoint != null) {
				selectedPoint.setAdjusting(false, e);
				if (selectedPoint instanceof VectorStep.Handle) {
					((VectorStep.Handle) selectedPoint).snap(trackerPanel);
				}
			}
			// if autoAdvance, advance to next frame after step creation
			if (stepCreated && selectedTrack != null && selectedTrack.isAutoAdvance()) {
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

	private void clearInteractive(TrackerPanel trackerPanel, MouseEvent e) {
		// no interactive
		if (trackerPanel.getSelectedPoint() != null) {
			// deselect the selected point--this will post undoable edit if changed
			trackerPanel.setSelectedPoint(null);
		}
		// erase and clear selected steps, if any
		TTrack[] tracks = trackerPanel.selectedSteps.getTracks();
		for (Step step : trackerPanel.selectedSteps) {
			step.erase();
		}
		trackerPanel.selectedSteps.clear(); // triggers undoable edit if changed
		for (TTrack next : tracks) {
			next.fireStepsChanged();
		}
		if (!trackerPanel.isShowCoordinates()) {
			trackerPanel.hideMouseBox();
			trackerPanel.setMouseCursor(Cursor.getDefaultCursor());
		}
		if (e.getClickCount() == 2) {
			trackerPanel.setSelectedTrack(null);
		}
		Rectangle rect = trackerPanel.scrollPane.getViewport().getViewRect();
		viewLoc.setLocation(rect.getLocation());
		mousePtRelativeToViewRect.setLocation(e.getPoint().x - rect.x, e.getPoint().y - rect.y);
		trackerPanel.scrollPane.getViewport().getView().getSize(dim);
		Cursor c = trackerPanel.getCursor();
		if ((dim.width > rect.width || dim.height > rect.height) && !Tracker.isZoomInCursor(c)
				&& !Tracker.isZoomOutCursor(c)) {
			trackerPanel.setMouseCursor(Tracker.grabCursor);
		}
	}

	private void selectPoint(TrackerPanel trackerPanel, MouseEvent e) {
		// select a clicked TPoint
		selectedPoint = (TPoint) iad;
		// find associated step and track
		Step step = null;
		TTrack stepTrack = null;
		for (TTrack track : trackerPanel.getTracksTemp()) {
			step = track.getStep(selectedPoint, trackerPanel);
			if (step != null) {
				stepTrack = track;
				break;
			}
		}
		trackerPanel.clearTemp();

		// if control-down, manage trackerPanel.selectedSteps
		boolean isStepSelected = trackerPanel.selectedSteps.contains(step);
		boolean selectedStepsChanged = false;
		if (e.isControlDown()) {
			if (isStepSelected) {
				// deselect point and remove step from selectedSteps
				selectedPoint = null;
				trackerPanel.selectedSteps.remove(step);
				selectedStepsChanged = true;
			} else { // the step is not yet in selectedSteps
				if (!trackerPanel.selectedSteps.isEmpty()) {
					// set selectedPoint to null if multiple steps are selected
					selectedPoint = null;
				}
				trackerPanel.selectedSteps.add(step);
				selectedStepsChanged = true;
			}
		}
		// else if not control-down, check if this step is in selectedSteps
		else {
			if (trackerPanel.selectedSteps.contains(step)) {
				// do nothing: point is selected and step is in selectedSteps
			} else {
				// deselect existing selectedSteps
				boolean stepsIncludeSelectedPoint = false;
				for (Step next : trackerPanel.selectedSteps) {
					next.erase();
					stepsIncludeSelectedPoint = stepsIncludeSelectedPoint
							|| next.getPoints()[0] == trackerPanel.getSelectedPoint();
				}

				trackerPanel.selectedSteps.clear();
				// add this point's step
				trackerPanel.selectedSteps.add(step);
				selectedStepsChanged = true;

				if (stepsIncludeSelectedPoint) {
					trackerPanel.pointState.setLocation(trackerPanel.getSelectedPoint()); // prevents posting
																							// undoable edit
				}
			}
		}
		if (selectedStepsChanged && stepTrack != null) {
			stepTrack.firePropertyChange(TTrack.PROPERTY_TTRACK_STEPS, TTrack.HINT_STEPS_SELECTED, null); // $NON-NLS-1$
		}

		if (step != null)
			step.erase();

		if (selectedPoint instanceof AutoTracker.Handle) {
			((AutoTracker.Handle) selectedPoint).setScreenLocation(e.getX(), e.getY(), trackerPanel);
		} else if (selectedPoint instanceof Ruler.Handle) {
			((Ruler.Handle) selectedPoint).setScreenLocation(e.getX(), e.getY(), trackerPanel);
		}
		if (selectedPoint != null) {
			selectedPoint.setAdjusting(true, e);
			selectedPoint.showCoordinates(trackerPanel);
			trackerPanel.setSelectedPoint(selectedPoint);
		}
		if (selectedPoint instanceof Step.Handle) {
			((Step.Handle) selectedPoint).setPositionOnLine(e.getX(), e.getY(), trackerPanel);
		}
	}

	private void markPoint(TrackerPanel trackerPanel, MouseEvent e, AutoTracker autoTracker) {
		iad = null;
		boolean autotrackTrigger = (AutoTracker.isAutoTrackTrigger(e) && selectedTrack.isAutoTrackable()
				&& trackerPanel.getVideo() != null);
		KeyFrameData keyFrameData = (autotrackTrigger ? getActiveKeyFrame(autoTracker) : null);
		// create step
		frameNumber = trackerPanel.getFrameNumber();
		Step step = selectedTrack.getStep(frameNumber); // may be null for point mass, offset origin,
														// calibration pts
		int index = selectedTrack.getTargetIndex();
		int nextIndex = index;
		if (step == null || !autotrackTrigger) {
			if (autotrackTrigger) {
				selectedTrack.autoMarkAt(frameNumber, trackerPanel.getMouseX(), trackerPanel.getMouseY());
				step = selectedTrack.getStep(frameNumber);
			} else {
				boolean newStep = (step == null);
				if (selectedTrack.ttype == TTrack.TYPE_POINTMASS) {
					selectedTrack.keyFrames.add(frameNumber);
				}
				step = selectedTrack.createStep(frameNumber, trackerPanel.getMouseX(), trackerPanel.getMouseY());
				if (selectedTrack.ttype == TTrack.TYPE_POINTMASS) {
					PointMass m = (PointMass) selectedTrack;
					if (m.isAutofill()) {
						m.markInterpolatedSteps((PositionStep) step, true);
					}
				}
				trackerPanel.newlyMarkedPoint = step.getDefaultPoint();
				TPoint[] pts = step.getPoints();
				// increment target index if new step
				if (newStep && pts.length > index + 1)
					nextIndex = index + 1;
			}
		} else if (step.getPoints()[index] == null) {
			if (keyFrameData != null) {
				TPoint target = keyFrameData.getTarget();
				target.setXY(trackerPanel.getMouseX(), trackerPanel.getMouseY());
			}
			selectedTrack.autoMarkAt(frameNumber, trackerPanel.getMouseX(), trackerPanel.getMouseY());
			TPoint[] pts = step.getPoints();
			// increment target index if possible
			if (pts.length > index + 1)
				nextIndex = index + 1;
		}
		// if autotrack trigger, add key frame to autotracker
		if (autotrackTrigger && step != null && step.getPoints()[index] != null) {
			TPoint target = step.getPoints()[index];
			// remark step target if Axes/Tape/Protractor/Perspective selected and no
			// keyframe exists
			// make sure autoTracker is instantiated at this point
			if (autoTracker == null) {
				autoTracker = trackerPanel.getAutoTracker(true);
				autoTracker.setTrack(trackerPanel.getSelectedTrack());
			}
			if (autoTracker.getTrack() == selectedTrack) {
				switch (selectedTrack.ttype) {
				case TTrack.TYPE_COORDAXES:
				case TTrack.TYPE_TAPEMEASURE:
				case TTrack.TYPE_PERSPECTIVE:
				case TTrack.TYPE_PROTRACTOR:
					if (autoTracker.getOrCreateFrameData(frameNumber).getKeyFrameData() == null) {
							target.setXY(trackerPanel.getMouseX(), trackerPanel.getMouseY());
						}
					break;
				}
			}
			autoTracker.addKeyFrame(target, trackerPanel.getMouseX(), trackerPanel.getMouseY());
			trackerPanel.refreshTrackBar();
		}

		if (step != null && !autotrackTrigger) {
			trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			trackerPanel.setSelectedPoint(step.getDefaultPoint());
			selectedTrack.repaintStep(step);
			iad = selectedPoint = trackerPanel.getSelectedPoint();
			stepCreated = keyFrameData == null;
		}

		selectedTrack.setTargetIndex(nextIndex);
		if (autoTracker != null && autoTracker.getWizard().isVisible()) {
			autoTracker.getWizard().refreshGUI();
		}
	}

	protected KeyFrameData getActiveKeyFrame(AutoTracker autoTracker) {
		FrameData frameData;
		return (selectedTrack != null && autoTracker != null 
				&& selectedTrack == autoTracker.getTrack()
				&& autoTracker.getWizard().isVisible() 
				&& (frameData = autoTracker.getOrCreateFrameData(frameNumber)).getKeyFrameData()
						== frameData ? (KeyFrameData) frameData : null);
	}

}
