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

import java.util.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.opensourcephysics.cabrillo.tracker.Vector;
import org.opensourcephysics.tools.FontSizer;

/**
 * This displays and sets VectorSum properties.
 *
 * @author Douglas Brown
 */
public class VectorSumInspector extends JDialog implements PropertyChangeListener {

	// instance fields

	protected TFrame frame;
	protected Integer panelID;

	protected VectorSum sum;
	protected JButton okButton;
	protected JPanel mainPanel;
	protected JPanel checkboxPanel;
	protected JPanel sumPanel;
	protected ActionListener listener;
	protected boolean isVisible;

	/**
	 * Constructs a VectorSumInspector.
	 *
	 * @param sum the vector sum
	 */
	public VectorSumInspector(VectorSum sum) {
		// nonmodal
		super(JOptionPane.getFrameForComponent(sum.tp), false);
		this.sum = sum;
		frame = sum.frame;
		panelID = sum.tp.getID();
		if (panelID != null) {
			sum.tp.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
			if (frame != null) {
				frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			}
		}
		// listener for the checkboxes
		listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSum();
			}
		};
		setTitle(TrackerRes.getString("VectorSumInspector.Title") //$NON-NLS-1$
				+ " \"" + sum.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		setResizable(false);
		createGUI();
		initialize();
		pack();
	}

	/**
	 * Overrides JDialog setVisible method.
	 *
	 * @param vis true to show this inspector
	 */
	@Override
	public void setVisible(boolean vis) {
		if (vis) {
			FontSizer.setFonts(this, FontSizer.getLevel());
			pack();
		}
		super.setVisible(vis);
		isVisible = vis;
	}

	/**
	 * Initializes this inpector.
	 */
	public void initialize() {
		updateDisplay();
	}

	/**
	 * Disposes of this inpector.
	 */
	@Override
	public void dispose() {
		checkboxPanel.removeAll();
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
			ArrayList<Vector> list = trackerPanel.getDrawablesTemp(Vector.class);
			for (int k = 0, n = list.size(); k < n; k++) {
				list.get(k).removeListenerNCF(this);
			}
			list.clear();
			if (frame != null) {
				frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			}
			panelID = null;
		}
		super.dispose();
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(TFrame.PROPERTY_TFRAME_TAB)) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			if (trackerPanel != null && e.getNewValue() == trackerPanel) {
				setVisible(isVisible);
			} else {
				boolean vis = isVisible;
				setVisible(false);
				isVisible = vis;
			}
		} else {
			updateDisplay();
		}
	}

	/**
	 * Updates this inspector to show sum's current vectors.
	 */
	protected void updateDisplay() {
		setTitle(TrackerRes.getString("VectorSumInspector.Title") //$NON-NLS-1$
				+ " \"" + sum.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		// make checkboxes for all vectors (but not vector sums) in tracker panel
		checkboxPanel.removeAll();
		ArrayList<Vector> list = frame.getTrackerPanelForID(panelID).getDrawablesTemp(Vector.class);
		for (int k = 0, n = list.size(); k < n; k++) {
			Vector v = list.get(k);
			v.removeListenerNCF(this);
			v.addListenerNCF(this);
			if (v instanceof VectorSum)
				continue; // don't include other sums
			JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(v.getName(), v.getFootprint().getIcon(21, 16));
			// check the checkbox and show components if vector is in the sum
			if (sum.contains(v)) {
				checkbox.setSelected(true);
			}
			checkbox.addActionListener(listener);
			checkboxPanel.add(checkbox);
		}
		list.clear();
		FontSizer.setFonts(checkboxPanel, FontSizer.getLevel());
		pack();
		TFrame.repaintT(this);
	}

//_____________________________ private methods ____________________________

	/**
	 * Creates the visible components of this panel.
	 */
	private void createGUI() {
		JPanel inspectorPanel = new JPanel(new BorderLayout());
		setContentPane(inspectorPanel);
		// create mainPanel
		mainPanel = new JPanel(new GridLayout(1, 0));
		Border etched = BorderFactory.createEtchedBorder();
		TitledBorder title = BorderFactory.createTitledBorder(etched,
				TrackerRes.getString("VectorSumInspector.Border.Title")); //$NON-NLS-1$
		mainPanel.setBorder(title);
		inspectorPanel.add(mainPanel, BorderLayout.CENTER);
		// create checkboxPanel
		checkboxPanel = new JPanel(new GridLayout(0, 1));
		mainPanel.add(checkboxPanel);
		// create sumPanel
		sumPanel = new JPanel(new GridLayout(0, 2));
		// create ok button
		okButton = new JButton(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
		okButton.setForeground(new Color(0, 0, 102));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		// create buttonbar at bottom
		JPanel buttonbar = new JPanel(new GridLayout(1, 3));
		buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
		inspectorPanel.add(buttonbar, BorderLayout.SOUTH);
		Box box = Box.createHorizontalBox();
		buttonbar.add(box);
		buttonbar.add(okButton);
		box = Box.createHorizontalBox();
		buttonbar.add(box);
	}

	/**
	 * Updates the vector sum to reflect the current checkbox states.
	 */
	private void updateSum() {
		// get the checkbox array
		Component[] checkboxes = checkboxPanel.getComponents();
		for (int i = 0; i < checkboxes.length; i++) {
			JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) checkboxes[i];
			// get the vector
			Vector v = getVector(checkbox.getActionCommand());
			if (checkbox.isSelected() && !sum.contains(v)) {
				// add and show selected vectors
				sum.addVector(v);
				v.setVisible(true);
			}
			if (!checkbox.isSelected() && sum.contains(v))
				// remove unselected vectors
				sum.removeVector(v);
		}
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			if (trackerPanel.getSelectedTrack() == sum && trackerPanel.getSelectedPoint() != null) {
				trackerPanel.getSelectedPoint().showCoordinates(trackerPanel);
			}
			TFrame.repaintT(trackerPanel);
		}
	}

	/**
	 * Gets the vector with the specified name.
	 *
	 * @param name name of the vector
	 * @return the vector
	 */
	private Vector getVector(String name) {
		return (panelID == null ? null : frame.getTrackerPanelForID(panelID).getTrackByName(Vector.class, name));
	}
}
