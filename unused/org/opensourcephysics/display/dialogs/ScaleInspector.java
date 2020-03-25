/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display.dialogs;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.media.core.DecimalField;
import org.opensourcephysics.media.core.NumberField;

/**
 * This displays and sets DrawingPanel scale properties.
 *
 * @author Douglas Brown
 * @author Wolfgang Christian
 * @version 1.1
 * 01 Dec 2007
 */
public class ScaleInspector extends JDialog {
  // instance fields
  protected DrawingPanel drawingPanel;
  protected JPanel dataPanel;
  protected JLabel xMinLabel, xMaxLabel, yMinLabel, yMaxLabel;
  protected NumberField xMinField, xMaxField, yMinField, yMaxField;
  protected JCheckBox xMinCheckBox, xMaxCheckBox, yMinCheckBox, yMaxCheckBox;
  protected JButton okButton;

  /**
   * Constructs a PanelInspector.
   *
   * @param panel the track plotting panel
   */
  public ScaleInspector(DrawingPanel panel) {
    // modal if panel has a frame
    super(JOptionPane.getFrameForComponent(panel), JOptionPane.getFrameForComponent(panel)!=null);
    drawingPanel = panel;
    setTitle(DialogsRes.SCALE_SCALE);
    setResizable(false);
    createGUI();
    pack();
  }

  // _____________________________ private methods ____________________________

  /**
   * Creates the visible components for the clip.
   */
  private void createGUI() {
    JPanel inspectorPanel = new JPanel(new BorderLayout());
    setContentPane(inspectorPanel);
    JPanel controlPanel = new JPanel(new BorderLayout());
    inspectorPanel.add(controlPanel, BorderLayout.SOUTH);
    // create actions, labels, fields and check boxes
    final Action setXAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        double xMin = xMinCheckBox.isSelected() ? Double.NaN : xMinField.getValue();
        double xMax = xMaxCheckBox.isSelected() ? Double.NaN : xMaxField.getValue();
        drawingPanel.setPreferredMinMaxX(xMin, xMax);
        drawingPanel.paintImmediately(drawingPanel.getBounds());
        updateDisplay();
      }

    };
    final Action setYAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        double yMin = yMinCheckBox.isSelected() ? Double.NaN : yMinField.getValue();
        double yMax = yMaxCheckBox.isSelected() ? Double.NaN : yMaxField.getValue();
        drawingPanel.setPreferredMinMaxY(yMin, yMax);
        drawingPanel.paintImmediately(drawingPanel.getBounds());
        updateDisplay();
      }

    };
    // xMin
    xMinLabel = new JLabel(DialogsRes.SCALE_MIN);
    xMinField = new DecimalField(4, 2);
    xMinField.setMaximumSize(xMinField.getPreferredSize());
    xMinField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setXAction.actionPerformed(null);
        xMinField.requestFocusInWindow();
      }

    });
    xMinField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setXAction.actionPerformed(null);
      }

    });
    xMinCheckBox = new JCheckBox(DialogsRes.SCALE_AUTO);
    xMinCheckBox.addActionListener(setXAction);
    xMaxLabel = new JLabel(DialogsRes.SCALE_MAX);
    xMaxField = new DecimalField(4, 2);
    xMaxField.setMaximumSize(xMaxField.getPreferredSize());
    xMaxField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setXAction.actionPerformed(null);
        xMaxField.requestFocusInWindow();
      }

    });
    xMaxField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setXAction.actionPerformed(null);
      }

    });
    xMaxCheckBox = new JCheckBox(DialogsRes.SCALE_AUTO);
    xMaxCheckBox.addActionListener(setXAction);
    // yMin
    yMinLabel = new JLabel(DialogsRes.SCALE_MIN);
    yMinField = new DecimalField(4, 2);
    yMinField.setMaximumSize(yMinField.getPreferredSize());
    yMinField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setYAction.actionPerformed(null);
        yMinField.requestFocusInWindow();
      }

    });
    yMinField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setYAction.actionPerformed(null);
      }

    });
    yMinCheckBox = new JCheckBox(DialogsRes.SCALE_AUTO);
    yMinCheckBox.addActionListener(setYAction);
    // yMax
    yMaxLabel = new JLabel(DialogsRes.SCALE_MAX);
    yMaxField = new DecimalField(4, 2);
    yMaxField.setMaximumSize(yMaxField.getPreferredSize());
    yMaxField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setYAction.actionPerformed(null);
        yMaxField.requestFocusInWindow();
      }

    });
    yMaxField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        setYAction.actionPerformed(null);
      }

    });
    yMaxCheckBox = new JCheckBox(DialogsRes.SCALE_AUTO);
    yMaxCheckBox.addActionListener(setYAction);
    // create panels and add labels, fields and check boxes
    JPanel xPanel = new JPanel(new GridLayout(2, 1));
    String title = DialogsRes.SCALE_HORIZONTAL;
    xPanel.setBorder(BorderFactory.createTitledBorder(title));
    JPanel yPanel = new JPanel(new GridLayout(2, 1));
    title = DialogsRes.SCALE_VERTICAL;
    yPanel.setBorder(BorderFactory.createTitledBorder(title));
    dataPanel = new JPanel(new GridLayout(2, 1));
    dataPanel.setBorder(BorderFactory.createEtchedBorder());
    controlPanel.add(dataPanel, BorderLayout.CENTER);
    Box box;
    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(xMaxLabel);
    box.add(xMaxField);
    box.add(xMaxCheckBox);
    xPanel.add(box);
    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(xMinLabel);
    box.add(xMinField);
    box.add(xMinCheckBox);
    xPanel.add(box);
    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(yMaxLabel);
    box.add(yMaxField);
    box.add(yMaxCheckBox);
    yPanel.add(box);
    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(yMinLabel);
    box.add(yMinField);
    box.add(yMinCheckBox);
    yPanel.add(box);
    dataPanel.add(yPanel);
    dataPanel.add(xPanel);
    // set alignments
    xMinLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    xMaxLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    yMinLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    yMaxLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    xMinField.setAlignmentX(Component.RIGHT_ALIGNMENT);
    xMaxField.setAlignmentX(Component.RIGHT_ALIGNMENT);
    yMinField.setAlignmentX(Component.RIGHT_ALIGNMENT);
    yMaxField.setAlignmentX(Component.RIGHT_ALIGNMENT);
    xMinCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
    xMaxCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
    yMinCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
    yMaxCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
    // create ok button
    okButton = new JButton(DialogsRes.SCALE_OK);
    okButton.setForeground(new Color(0, 0, 102));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    // create buttonbar and add button
    JPanel buttonbar = new JPanel();
    controlPanel.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(okButton);
  }

  /**
   * Updates this inspector to reflect the current settings.
   */
  public void updateDisplay() {
    xMinCheckBox.setSelected(drawingPanel.isAutoscaleXMin());
    xMinField.setEnabled(!xMinCheckBox.isSelected());
    xMinField.setValue(drawingPanel.getXMin());
    xMaxCheckBox.setSelected(drawingPanel.isAutoscaleXMax());
    xMaxField.setEnabled(!xMaxCheckBox.isSelected());
    xMaxField.setValue(drawingPanel.getXMax());
    yMinCheckBox.setSelected(drawingPanel.isAutoscaleYMin());
    yMinField.setEnabled(!yMinCheckBox.isSelected());
    yMinField.setValue(drawingPanel.getYMin());
    yMaxCheckBox.setSelected(drawingPanel.isAutoscaleYMax());
    yMaxField.setEnabled(!yMaxCheckBox.isSelected());
    yMaxField.setValue(drawingPanel.getYMax());
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
