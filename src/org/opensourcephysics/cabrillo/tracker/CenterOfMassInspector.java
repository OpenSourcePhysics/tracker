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
public class CenterOfMassInspector extends JDialog
    implements PropertyChangeListener {

  // instance fields
  protected CenterOfMass cm;
  protected TrackerPanel trackerPanel;
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
    super(JOptionPane.getFrameForComponent(track.trackerPanel), false);
    cm = track;
    trackerPanel = cm.trackerPanel;
    if (trackerPanel != null) {
    	trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
      }
    }
    // listener for the point mass checkboxes
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {updateCM();}
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
   * Responds to property change events. This listens for the
   * following events: "tab" from TFrame.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("tab")) { //$NON-NLS-1$
      if (trackerPanel != null && e.getNewValue() == trackerPanel) {
        setVisible(isVisible);
      }
      else {
        boolean vis = isVisible;
        setVisible(false);
        isVisible = vis;
      }
    }
    else updateDisplay();
  }

  /**
   * Overrides JDialog setVisible method.
   *
   * @param vis true to show this inspector
   */
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
  public void dispose() {
    checkboxPanel.removeAll();
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
      Iterator<PointMass> it = trackerPanel.getDrawables(PointMass.class).iterator();
      while (it.hasNext()) {
        PointMass p = it.next();
        p.removePropertyChangeListener("name", this); //$NON-NLS-1$
        p.removePropertyChangeListener("color", this); //$NON-NLS-1$
        p.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      }
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
      }
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
      JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)checkboxes[i];
      // get the pointmass
      PointMass m = getPointMass(checkbox.getActionCommand());
      if (checkbox.isSelected() && !cm.containsMass(m))
        cm.addMass(m);
      if (!checkbox.isSelected() && cm.containsMass(m))
        cm.removeMass(m);
    }
    cm.trackerPanel.repaint();
  }

  /**
   * Gets the point mass with the specified name.
   *
   * @param name name of the point mass
   * @return the point mass
   */
  private PointMass getPointMass(String name) {
    ArrayList<PointMass> masses = trackerPanel.getDrawables(PointMass.class);
    for (PointMass m: masses) {
      if (m.getName() == name) return m;
    }
    return null;
  }

  /**
   * Updates this inspector to show cm's current masses.
   */
  protected void updateDisplay() {
    setTitle(TrackerRes.getString("CenterOfMassInspector.Title") //$NON-NLS-1$
             + " \"" + cm.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
    // make checkboxes for all point masses in tracker panel
    checkboxPanel.removeAll();
    Iterator<PointMass> it = trackerPanel.getDrawables(PointMass.class).iterator();
    while (it.hasNext()) {
      PointMass m = it.next();
      m.removePropertyChangeListener("name", this); //$NON-NLS-1$
      m.removePropertyChangeListener("color", this); //$NON-NLS-1$
      m.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      m.addPropertyChangeListener("name", this); //$NON-NLS-1$
      m.addPropertyChangeListener("color", this); //$NON-NLS-1$
      m.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
      if (m instanceof CenterOfMass) continue; // don't include other cms
      JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(
          m.getName(), m.getFootprint().getIcon(21, 16));
      // check the checkbox if m is in the cm
      if (cm.containsMass(m)) checkbox.setSelected(true);
      checkbox.addActionListener(listener);
      checkboxPanel.add(checkbox);
    }
  	FontSizer.setFonts(checkboxPanel, FontSizer.getLevel());
    pack();
    repaint();
  }

}
