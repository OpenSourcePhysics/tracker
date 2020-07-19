package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.tools.FontSizer;

/**
 * Superclass for Protractor and TapeMeasure, both of which have a clickable
 * editable interface for changing the value.
 * 
 * @author hansonr
 *
 */
public abstract class InputTrack extends TTrack {


	abstract protected boolean checkKeyFrame();

	abstract protected NumberField createInputField();

	abstract protected void endEditing(Step step);

	abstract protected Rectangle getLayoutBounds(Step step);

	abstract protected void refreshStep(Step step);

	abstract protected void setInputValue(Step step);

	protected NumberField inputField;
	protected NumberFormat format;
	protected MouseListener editListener;
	
	protected boolean editing;
	protected boolean fixedPosition = true;

	public InputTrack() {
		inputField = createInputField();
		inputField.setBorder(null);
		format = inputField.getFormat();
//			inputPanel = new JPanel(null);
//			inputPanel.setOpaque(false);
//			inputPanel.add(inputField);
		// add inputField action listener to exit editing mode
		inputField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopEditing();
			}
		});
		// add inputField focus listener
		inputField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				inputField.selectAll();
			}

			@Override
			public void focusLost(FocusEvent e) {
				stopEditing();
			}
		});

		// add mouse listener to toggle editing mode
		editListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				stopEditing();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseClickedAction(e.getPoint());
			}
		};
	}

	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (trackerPanel != null) {
			trackerPanel.removeMouseListener(editListener);
		}
		setTrackerPanelWithListeners(panel);
		if (trackerPanel != null) {
			trackerPanel.addMouseListener(editListener);
		}
	}
	

	protected void setEditAction(Step step, Point pt) {
		if (editing) {
			trackerPanel.setSelectedTrack(this);
			FontSizer.setFonts(inputField, FontSizer.getLevel());
			inputField.setForeground(footprint.getColor());
			Dimension d = inputField.getPreferredSize();
			Rectangle bounds = getLayoutBounds(step);
			inputField.setBounds(bounds.x, bounds.y - 5, d.width, d.height);
			trackerPanel.add(inputField);
			Border space = BorderFactory.createEmptyBorder(0, 1, 1, 0);
			Color color = getFootprint().getColor();
			Border line = BorderFactory.createLineBorder(color);
			inputField.setBorder(BorderFactory.createCompoundBorder(line, space));
			setInputValue(step);
			inputField.requestFocus();
		} else { // end editing
			endEditing(step);
			trackerPanel.remove(inputField);
			invalidateData(null);
			TFrame.repaintT(trackerPanel);
			TTrackBar.getTrackbar(trackerPanel).refresh();
		}
	}

	/**
	 * Gets the fixed property.
	 *
	 * @return <code>true</code> if fixed
	 */
	public boolean isFixedPosition() {
		return fixedPosition;
	}


	/**
	 * Overrides TTrack setTrailVisible method to keep trails hidden.
	 *
	 * @param visible ignored
	 */
	@Override
	public void setTrailVisible(boolean visible) {
		/** empty block */
	}

	/**
	 * Overrides TTrack deleteStep method to prevent deletion.
	 *
	 * @param n the frame number
	 * @return the deleted step
	 */
	@Override
	public Step deleteStep(int n) {
		return null;
	}

	
	/**
	 * Returns the key step for a given step. The key step defines the positions of
	 * the tape ends.
	 * 
	 * @param step the step
	 * @return the key step
	 */
	protected Step getKeyStep(Step step) {
		int key = 0;
		if (!isFixedPosition()) {
			for (int i : keyFrames) {
				if (i <= step.n)
					key = i;
			}
		}
		return getStep(key);
	}

	/**
	 * Overrides TTrack getStep method to provide fixedTape behavior.
	 *
	 * @param n the frame number
	 * @return the step
	 */
	@Override
	public Step getStep(int n) {
		Step step = steps.getStep(n);
		refreshStep(step);
		return step;
	}

	/**
	 * Sets the editing flag.
	 *
	 * @param edit   <code>true</code> to edit the scale
	 * @param target the tape step that handles the edit process
	 * @param pt     TODO
	 */
	protected void setEditing(boolean edit, Step target, Point pt) {
		editing = edit;
		if (checkKeyFrame()) {
			// if not fixed, add target frame to key frames
			if (!isFixedPosition())
				keyFrames.add(target.n);
			// replace target with key frame step
			target = getKeyStep(target);
		}
		final Step step = target;
		Runnable runner = new Runnable() {

			@Override
			public void run() {
				setEditAction(step, pt);
			}

		};
		EventQueue.invokeLater(runner);
	}

	protected void stopEditing() {
		if (editing)
			setEditing(false, getStep(trackerPanel.getFrameNumber()), null);
	}

	protected void mouseClickedAction(Point pt) {
		if (isLocked())
			return;
		int n = trackerPanel.getFrameNumber();
		TapeStep step = (TapeStep) getStep(n);
		if (step == null)
			return;
		Rectangle bounds = step.layoutBounds.get(trackerPanel);
		if (bounds != null && bounds.contains(pt)) {
			// readout was clicked
			if (checkAttachments()) {
				trackerPanel.setSelectedTrack(this);
				return;
			}
			setEditing(true, step, pt);
		}
	}

	protected void setMagValue() {
		inputField.setValue(magField.getValue());
		// repaint current step
		int n = trackerPanel.getFrameNumber();
		Step tape = getStep(n);
		if (tape != null) {
			tape.repaint();
		}
	}
	

	@Override
	protected int getAttachmentLength() {
		return getFootprintLength();
	}
	
	/**
	 * Determines if the given point index is autotrackable.
	 *
	 * @param pointIndex the points[] index
	 * @return true if autotrackable
	 */
	@Override
	protected boolean isAutoTrackable(int pointIndex) {
		return isAutoTrackable() && pointIndex < getAttachmentLength();
	}



}
