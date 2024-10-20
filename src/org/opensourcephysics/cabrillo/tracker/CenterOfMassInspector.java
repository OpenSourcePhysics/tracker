/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.opensourcephysics.tools.FontSizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * This displays and sets CenterOfMass properties.
 *
 * @author Douglas Brown
 */
public class CenterOfMassInspector extends JDialog implements PropertyChangeListener {

	// instance fields
	protected TFrame frame;
	protected Integer panelID;
	protected CenterOfMass cm;
	protected JButton okButton;
	protected JPanel checkboxPanel;
	protected ActionListener listener;
	protected boolean isVisible;

	/**
	 * Constructs a CenterOfMassInspector.
	 *
	 * @param track the center of mass track
	 */
	public CenterOfMassInspector(CenterOfMass track) {
		super(JOptionPane.getFrameForComponent(track.tp), false);
		cm = track;
		frame = track.tp.getTFrame();
		panelID = track.tp.getID();
		track.tp.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		if (frame != null) {
			frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
		}
		// listener for the point mass checkboxes
		listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateCM();
			}
		};
		setResizable(false);
		createGUI();
		initialize();
		pack();
	}

	/**
	 * Initializes this inpector.
	 */
	public void initialize() {
		updateDisplay();
	}

	/**
	 * Responds to property change events. This listens for the following events:
	 * TFrame.PROPERTY_TFRAME_TAB.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TFrame.PROPERTY_TFRAME_TAB:			
			if (e.getNewValue() != null && !frame.isRemovingAll() 
					&& panelID != null && ((TrackerPanel)e.getNewValue()).getID() == panelID) {
				setVisible(isVisible);
			} else {
				boolean vis = isVisible;
				setVisible(false);
				isVisible = vis;
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
		default:
			updateDisplay();
			break;
		}

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
	 * Disposes of this inspector.
	 */
	@Override
	public void dispose() {
		checkboxPanel.removeAll();
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
			ArrayList<PointMass> masses = trackerPanel.getDrawablesTemp(PointMass.class);
			for (int i = 0, n = masses.size(); i < n; i++) {
				masses.get(i).removeListenerNCF(this);
			}
			masses.clear();
			if (frame != null) {
				frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			}
			trackerPanel = null;
		}
		super.dispose();
	}

//_____________________________ private methods ____________________________

	/**
	 * Creates the visible components of this panel.
	 */
	private void createGUI() {
		JPanel inspectorPanel = new JPanel(new BorderLayout());
		setContentPane(inspectorPanel);
		// create checkboxPanel
		checkboxPanel = new JPanel(new GridLayout(0, 1));
		Border etched = BorderFactory.createEtchedBorder();
		TitledBorder title = BorderFactory.createTitledBorder(etched,
				TrackerRes.getString("CenterOfMassInspector.Border.Title")); //$NON-NLS-1$
		checkboxPanel.setBorder(title);
		inspectorPanel.add(checkboxPanel, BorderLayout.CENTER);
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
	 * Updates the center of mass to reflect the current checkbox states.
	 */
	private void updateCM() {
		// get the checkbox array
		Component[] checkboxes = checkboxPanel.getComponents();
		for (int i = 0; i < checkboxes.length; i++) {
			JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) checkboxes[i];
			// get the pointmass
			PointMass m = getPointMass(checkbox.getActionCommand());
			if (checkbox.isSelected() && !cm.containsMass(m))
				cm.addMass(m);
			if (!checkbox.isSelected() && cm.containsMass(m))
				cm.removeMass(m);
		}
		TFrame.repaintT(cm.tp);
	}

	/**
	 * Gets the point mass with the specified name.
	 *
	 * @param name name of the point mass
	 * @return the point mass
	 */
	private PointMass getPointMass(String name) {
		return frame.getTrackerPanelForID(panelID).getTrackByName(PointMass.class, name);
	}

	/**
	 * Updates this inspector to show cm's current masses.
	 */
	protected void updateDisplay() {
		setTitle(TrackerRes.getString("CenterOfMassInspector.Title") //$NON-NLS-1$
				+ " \"" + cm.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		// make checkboxes for all point masses in tracker panel
		checkboxPanel.removeAll();
		ArrayList<PointMass> masses = frame.getTrackerPanelForID(panelID).getDrawablesTemp(PointMass.class);
		for (int i = 0, n = masses.size(); i < n; i++) {
			PointMass m = masses.get(i);
			m.removeListenerNCF(this);
			m.addListenerNCF(this);
			if (m instanceof CenterOfMass)
				continue; // don't include other cms
			JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(m.getName(), m.getFootprint().getIcon(21, 16));
			// check the checkbox if m is in the cm
			if (cm.containsMass(m))
				checkbox.setSelected(true);
			checkbox.addActionListener(listener);
			checkboxPanel.add(checkbox);
		}
		masses.clear();
		FontSizer.setFonts(checkboxPanel, FontSizer.getLevel());
		pack();
		TFrame.repaintT(this);
	}

}
