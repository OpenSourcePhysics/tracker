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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.opensourcephysics.display.DrawingPanel;

/**
 * This displays and the DrawingPanel autoscale properties.
 *
 * @author W. Christian
 * @version 1.0
 */
public class AutoScaleInspector extends JDialog {
  // instance fields
  protected DrawingPanel plotPanel;
  protected JPanel dataPanel;
  protected JCheckBox xAutoscaleCheckBox;
  protected JCheckBox yAutoscaleCheckBox;
  protected JButton okButton;

  /**
   * Constructs an Autoscale Inspector.
   *
   * @param panel the track plotting panel
   */
  public AutoScaleInspector(DrawingPanel panel) {
    super((Frame) null, true); // modal dialog with no owner
    plotPanel = panel;
    setTitle(DialogsRes.AUTOSCALE_AUTOSCALE);
    setResizable(false);
    createGUI();
    pack();
  }

  // _____________________________ private methods ____________________________

  /**
   * Creates the visible components for the clip.
   */
  private void createGUI() {
    xAutoscaleCheckBox = new JCheckBox(DialogsRes.AUTOSCALE_AUTO+" x"); //$NON-NLS-1$
    xAutoscaleCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        plotPanel.setAutoscaleX(xAutoscaleCheckBox.isSelected());
        plotPanel.scale();
        updateDisplay();
        plotPanel.repaint();
      }

    });
    yAutoscaleCheckBox = new JCheckBox(DialogsRes.AUTOSCALE_AUTO+" y"); //$NON-NLS-1$
    yAutoscaleCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        plotPanel.setAutoscaleY(yAutoscaleCheckBox.isSelected());
        plotPanel.scale();
        updateDisplay();
        plotPanel.repaint();
      }

    });
    JPanel inspectorPanel = new JPanel(new BorderLayout());
    setContentPane(inspectorPanel);
    JPanel controlPanel = new JPanel(new BorderLayout());
    inspectorPanel.add(controlPanel, BorderLayout.SOUTH);
    // create panels and add labels, fields and check boxes
    JPanel xPanel = new JPanel(new GridLayout(1, 2));
    xPanel.setBorder(BorderFactory.createTitledBorder(DialogsRes.AUTOSCALE_ZOOM_WARNING));
    dataPanel = new JPanel(new GridLayout(1, 1));
    dataPanel.setBorder(BorderFactory.createEtchedBorder());
    controlPanel.add(dataPanel, BorderLayout.CENTER);
    Box box;
    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    xPanel.add(xAutoscaleCheckBox);
    xPanel.add(box);
    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    xPanel.add(box);
    box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    xPanel.add(yAutoscaleCheckBox);
    dataPanel.add(xPanel);
    xAutoscaleCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
    yAutoscaleCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
    // create ok button
    okButton = new JButton(DialogsRes.AUTOSCALE_OK);
    okButton.setForeground(new Color(0, 0, 102));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    // create buttonbar and add buttons
    // JPanel buttonbar = new JPanel(new GridLayout(1, 2));
    JPanel buttonbar = new JPanel();
    controlPanel.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(okButton);
    // buttonbar.add(cancelButton);
  }

  /**
   * Updates this inpector to reflect the current settings.
   */
  public void updateDisplay() {
    xAutoscaleCheckBox.setSelected(plotPanel.isAutoscaleX());
    yAutoscaleCheckBox.setSelected(plotPanel.isAutoscaleY());
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
