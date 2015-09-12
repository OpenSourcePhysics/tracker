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
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.opensourcephysics.tools.FontSizer;

/**
 * This displays and sets VectorSum properties.
 *
 * @author Douglas Brown
 */
public class VectorSumInspector extends JDialog
    implements PropertyChangeListener {

  // instance fields
  protected VectorSum sum;
  protected TrackerPanel trackerPanel;
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
    super(JOptionPane.getFrameForComponent(sum.trackerPanel), false);
    this.sum = sum;
    trackerPanel = sum.trackerPanel;
    if (trackerPanel != null) {
      trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
      }
    }
    // listener for the checkboxes
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {updateSum();}
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
  public void dispose() {
    checkboxPanel.removeAll();
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
      Iterator<Vector> it = trackerPanel.getDrawables(Vector.class).iterator();
      while (it.hasNext()) {
        Vector v = it.next();
        v.removePropertyChangeListener("name", this); //$NON-NLS-1$
        v.removePropertyChangeListener("color", this); //$NON-NLS-1$
        v.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      }
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
      }
    }
    super.dispose();
  }

  /**
   * Responds to property change events. VectorSumInspector listens for the
   * following events: "color", "footprint" and "name" from Vector, "track"
   * from TrackerPanel, and "tab" from TFrame.
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
   * Updates this inspector to show sum's current vectors.
   */
  protected void updateDisplay() {
    setTitle(TrackerRes.getString("VectorSumInspector.Title") //$NON-NLS-1$
             + " \"" + sum.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
    // make checkboxes for all vectors (but not vector sums) in tracker panel
    checkboxPanel.removeAll();
    Iterator<Vector> it = trackerPanel.getDrawables(Vector.class).iterator();
    while (it.hasNext()) {
      Vector v = it.next();
      v.removePropertyChangeListener("name", this); //$NON-NLS-1$
      v.removePropertyChangeListener("color", this); //$NON-NLS-1$
      v.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      v.addPropertyChangeListener("name", this); //$NON-NLS-1$
      v.addPropertyChangeListener("color", this); //$NON-NLS-1$
      v.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
      if (v instanceof VectorSum) continue; // don't include other sums
      JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(
          v.getName(), v.getFootprint().getIcon(21, 16));
      // check the checkbox and show components if vector is in the sum
      if (sum.contains(v)) {
        checkbox.setSelected(true);
      }
      checkbox.addActionListener(listener);
      checkboxPanel.add(checkbox);
    }
  	FontSizer.setFonts(checkboxPanel, FontSizer.getLevel());
    pack();
    repaint();
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
    TitledBorder title = BorderFactory.createTitledBorder(
        etched, TrackerRes.getString("VectorSumInspector.Border.Title")); //$NON-NLS-1$
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
      JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)checkboxes[i];
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
     if (trackerPanel != null) {
       if (trackerPanel.getSelectedTrack() == sum &&
           trackerPanel.getSelectedPoint() != null) {
         trackerPanel.getSelectedPoint().showCoordinates(trackerPanel);
       }
       trackerPanel.repaint();
     }
  }

  /**
   * Gets the vector with the specified name.
   *
   * @param name name of the vector
   * @return the vector
   */
  private Vector getVector(String name) {
    if (trackerPanel != null) {
      ArrayList<Vector> tracks = trackerPanel.getDrawables(Vector.class);
      for (Vector v: tracks) {
        if (v.getName() == name) return v;
      }
    }
    return null;
  }
}
