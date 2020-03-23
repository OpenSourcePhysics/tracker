/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import org.opensourcephysics.display3d.core.interaction.InteractionEvent;
import org.opensourcephysics.display3d.core.interaction.InteractionListener;
import org.opensourcephysics.tools.ToolsRes;

/**
 * <p>Title: CameraInspector</p>
 * <p>Description: This class creates an inspector for the camera.
 * The inspector is provided in a javax.swing.JPanel, so that
 * it can be used in other applications. A convenience class
 * is  provided to create a JFrame that contains the inspector.
 * </p>
 *
 * @author Francisco Esquembre
 * @version July 2005
 * @see Camera
 */
public class CameraInspector extends JPanel implements InteractionListener {
  private DrawingPanel3D panel = null;
  private Camera camera = null;
  private NumberFormat format = new java.text.DecimalFormat("0.000"); //$NON-NLS-1$
  private JTextField xField, yField, zField;
  private JTextField focusxField, focusyField, focuszField;
  private JTextField azimuthField, altitudeField;
  private JTextField rotationField, distanceField;
  private JRadioButton perspectiveRB, noperspectiveRB, planarxyRB, planarxzRB, planaryzRB;
  private java.util.AbstractList<ActionListener> listeners = new java.util.ArrayList<ActionListener>();

  /**
   * Creates a frame and, inside it, a CameraInspector.
   * @param panel DrawingPanel3D The drawing panel 3D with the inspected camera
   * @return JFrame
   */
  static public JFrame createFrame(DrawingPanel3D panel) {
    return new CameraInspectorFrame(ToolsRes.getString("CameraInspector.FrameTitle"), new CameraInspector(panel)); //$NON-NLS-1$
  }

  /**
   * Creates a frame with the given CameraInspector.
   * @param inspector CameraInspector The inspector provided
   * @return JFrame
   */
  static public JFrame createFrame(CameraInspector inspector) {
    return new CameraInspectorFrame(ToolsRes.getString("CameraInspector.FrameTitle"), inspector); //$NON-NLS-1$
  }

  /**
   * Creates a JPanel with a CameraInspector
   * @param panel DrawingPanel3D The drawing panel 3D with the inspected camera
   */
  public CameraInspector(DrawingPanel3D panel) {
    this.panel = panel;
    this.camera = panel.getCamera();
    panel.addInteractionListener(this);
    ActionListener fieldListener = new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        String cmd = evt.getActionCommand();
        JTextField field = (JTextField) evt.getSource();
        double value = 0.0;
        try {
          value = format.parse(field.getText()).doubleValue();
        } catch(java.text.ParseException exc) {
          value = 0.0;
        }
        if(cmd.equals("x")) {                                                                                   //$NON-NLS-1$
          camera.setXYZ(value, camera.getY(), camera.getZ());
        } else if(cmd.equals("y")) {                                                                            //$NON-NLS-1$
          camera.setXYZ(camera.getX(), value, camera.getZ());
        } else if(cmd.equals("z")) {                                                                            //$NON-NLS-1$
          camera.setXYZ(camera.getX(), camera.getY(), value);
        } else if(cmd.equals("focusx")) {                                                                       //$NON-NLS-1$
          camera.setFocusXYZ(value, camera.getFocusY(), camera.getFocusZ());
        } else if(cmd.equals("focusy")) {                                                                       //$NON-NLS-1$
          camera.setFocusXYZ(camera.getFocusX(), value, camera.getFocusZ());
        } else if(cmd.equals("focusz")) {                                                                       //$NON-NLS-1$
          camera.setFocusXYZ(camera.getFocusX(), camera.getFocusY(), value);
        } else if(cmd.equals("azimuth")) {                                                                      //$NON-NLS-1$
          camera.setAzimuth(value);
        } else if(cmd.equals("altitude")) {                                                                     //$NON-NLS-1$
          camera.setAltitude(value);
        } else if(cmd.equals("rotation")) {                                                                     //$NON-NLS-1$
          camera.setRotation(value);
        } else if(cmd.equals("screen")) {                                                                       //$NON-NLS-1$
          camera.setDistanceToScreen(value);
        }
        CameraInspector.this.panel.repaint();
        updateFields();
        ActionEvent event = new ActionEvent(CameraInspector.this, ActionEvent.ACTION_PERFORMED, "FieldChange"); //$NON-NLS-1$
        for(java.util.Iterator<ActionListener> it = listeners.iterator(); it.hasNext(); ) {
          (it.next()).actionPerformed(event);
        }
      }

    };
    ActionListener buttonListener = new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        String cmd = evt.getActionCommand();
        if(cmd.equals("reset")) {                                                                                //$NON-NLS-1$
          camera.reset();
          CameraInspector.this.panel.repaint();
          updateFields();
        } else if(cmd.equals("perspective")) {                                                                   //$NON-NLS-1$
          camera.setProjectionMode(Camera.MODE_PERSPECTIVE);
        } else if(cmd.equals("perspective_on")) {                                                                //$NON-NLS-1$
          camera.setProjectionMode(Camera.MODE_PERSPECTIVE_ON);
        } else if(cmd.equals("no_perspective")) {                                                                //$NON-NLS-1$
          camera.setProjectionMode(Camera.MODE_NO_PERSPECTIVE);
        } else if(cmd.equals("perspective_off")) {                                                               //$NON-NLS-1$
          camera.setProjectionMode(Camera.MODE_PERSPECTIVE_OFF);
        } else if(cmd.equals("planarXY")) {                                                                      //$NON-NLS-1$
          camera.setProjectionMode(Camera.MODE_PLANAR_XY);
        } else if(cmd.equals("planarXZ")) {                                                                      //$NON-NLS-1$
          camera.setProjectionMode(Camera.MODE_PLANAR_XZ);
        } else if(cmd.equals("planarYZ")) {                                                                      //$NON-NLS-1$
          camera.setProjectionMode(Camera.MODE_PLANAR_YZ);
        }
        camera.reset();
        ActionEvent event = new ActionEvent(CameraInspector.this, ActionEvent.ACTION_PERFORMED, "ButtonChange"); //$NON-NLS-1$
        for(java.util.Iterator<ActionListener> it = listeners.iterator(); it.hasNext(); ) {
          (it.next()).actionPerformed(event);
        }
      }

    };
    setLayout(new BorderLayout());
    JPanel projectionPanel = new JPanel(new GridLayout(2, 3));
    projectionPanel.setBorder(new TitledBorder(ToolsRes.getString("CameraInspector.ProjectionMode"))); //$NON-NLS-1$
    ButtonGroup group = new ButtonGroup();
    perspectiveRB = new JRadioButton(ToolsRes.getString("CameraInspector.Perspective")); //$NON-NLS-1$
    perspectiveRB.setActionCommand("perspective"); //$NON-NLS-1$
    perspectiveRB.addActionListener(buttonListener);
    projectionPanel.add(perspectiveRB);
    group.add(perspectiveRB);
    planarxyRB = new JRadioButton(ToolsRes.getString("CameraInspector.PlanarXY")); //$NON-NLS-1$
    planarxyRB.setActionCommand("planarXY"); //$NON-NLS-1$
    planarxyRB.addActionListener(buttonListener);
    projectionPanel.add(planarxyRB);
    group.add(planarxyRB);
    planaryzRB = new JRadioButton(ToolsRes.getString("CameraInspector.PlanarYZ")); //$NON-NLS-1$
    planaryzRB.setActionCommand("planarYZ"); //$NON-NLS-1$
    planaryzRB.addActionListener(buttonListener);
    projectionPanel.add(planaryzRB);
    group.add(planaryzRB);
    noperspectiveRB = new JRadioButton(ToolsRes.getString("CameraInspector.NoPerspective")); //$NON-NLS-1$
    noperspectiveRB.setActionCommand("no_perspective"); //$NON-NLS-1$
    noperspectiveRB.addActionListener(buttonListener);
    projectionPanel.add(noperspectiveRB);
    group.add(noperspectiveRB);
    planarxzRB = new JRadioButton(ToolsRes.getString("CameraInspector.PlanarXZ")); //$NON-NLS-1$
    planarxzRB.setActionCommand("planarXZ"); //$NON-NLS-1$
    planarxzRB.addActionListener(buttonListener);
    projectionPanel.add(planarxzRB);
    group.add(planarxzRB);
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(projectionPanel, BorderLayout.CENTER);
    add(projectionPanel, BorderLayout.NORTH);
    JPanel labelPanel = new JPanel(new GridLayout(0, 1));
    JPanel fieldPanel = new JPanel(new GridLayout(0, 1));
    JPanel label2Panel = new JPanel(new GridLayout(0, 1));
    JPanel field2Panel = new JPanel(new GridLayout(0, 1));
    xField = createRow(labelPanel, fieldPanel, "X", fieldListener);                 //$NON-NLS-1$
    yField = createRow(labelPanel, fieldPanel, "Y", fieldListener);                 //$NON-NLS-1$
    zField = createRow(labelPanel, fieldPanel, "Z", fieldListener);                 //$NON-NLS-1$
    focusxField = createRow(label2Panel, field2Panel, "FocusX", fieldListener);     //$NON-NLS-1$
    focusyField = createRow(label2Panel, field2Panel, "FocusY", fieldListener);     //$NON-NLS-1$
    focuszField = createRow(label2Panel, field2Panel, "FocusZ", fieldListener);     //$NON-NLS-1$
    azimuthField = createRow(labelPanel, fieldPanel, "Azimuth", fieldListener);     //$NON-NLS-1$
    altitudeField = createRow(labelPanel, fieldPanel, "Altitude", fieldListener);   //$NON-NLS-1$
    // createRow (label2Panel, field2Panel, null,fieldListener); // emptyrow
    rotationField = createRow(label2Panel, field2Panel, "Rotation", fieldListener); //$NON-NLS-1$
    distanceField = createRow(label2Panel, field2Panel, "Screen", fieldListener);   //$NON-NLS-1$
    // createRow (label2Panel, field2Panel, null,fieldListener); // emptyrow
    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.add(labelPanel, BorderLayout.WEST);
    leftPanel.add(fieldPanel, BorderLayout.CENTER);
    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(label2Panel, BorderLayout.WEST);
    rightPanel.add(field2Panel, BorderLayout.CENTER);
    JPanel centerPanel = new JPanel(new GridLayout(1, 0));
    centerPanel.setBorder(new TitledBorder(ToolsRes.getString("CameraInspector.CameraParameters"))); //$NON-NLS-1$
    centerPanel.add(leftPanel);
    centerPanel.add(rightPanel);
    add(centerPanel, BorderLayout.CENTER);
    JButton resetButton = new JButton(ToolsRes.getString("CameraInspector.ResetCamera")); //$NON-NLS-1$
    resetButton.setActionCommand("reset"); //$NON-NLS-1$
    resetButton.addActionListener(buttonListener);
    add(resetButton, BorderLayout.SOUTH);
    updateFields();
  }

  /**
   * Sets the format for the fields in the inspector
   * @param format NumberFormat
   */
  public void setFormat(NumberFormat format) {
    this.format = format;
  }

  /**
   * Adds a listener to any change in the camera settings
   * @param defaultFormat NumberFormat
   */
  public void addActionListener(ActionListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener to any change in the camera settings
   * @param defaultFormat NumberFormat
   */
  public void removeActionListener(ActionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Public as result of the implementation. Not to be used directly by final users.
   * @param _event InteractionEvent
   */
  public void interactionPerformed(InteractionEvent _event) {
    if(_event.getSource()!=panel) {
      return;
    }
    if(_event.getInfo()!=null) {
      return; // It is not changing the camera
    }
    updateFields();
  }

  public void updateFields() {
    switch(camera.getProjectionMode()) {
       default :
       case Camera.MODE_PERSPECTIVE :
       case Camera.MODE_PERSPECTIVE_ON :
         perspectiveRB.setSelected(true);
         break;
       case Camera.MODE_NO_PERSPECTIVE :
       case Camera.MODE_PERSPECTIVE_OFF :
         noperspectiveRB.setSelected(true);
         break;
       case Camera.MODE_PLANAR_XY :
         planarxyRB.setSelected(true);
         break;
       case Camera.MODE_PLANAR_XZ :
         planarxzRB.setSelected(true);
         break;
       case Camera.MODE_PLANAR_YZ :
         planaryzRB.setSelected(true);
         break;
    }
    xField.setText(format.format(camera.getX()));
    yField.setText(format.format(camera.getY()));
    zField.setText(format.format(camera.getZ()));
    focusxField.setText(format.format(camera.getFocusX()));
    focusyField.setText(format.format(camera.getFocusY()));
    focuszField.setText(format.format(camera.getFocusZ()));
    azimuthField.setText(format.format(camera.getAzimuth()));
    altitudeField.setText(format.format(camera.getAltitude()));
    rotationField.setText(format.format(camera.getRotation()));
    distanceField.setText(format.format(camera.getDistanceToScreen()));
  }

  static private JTextField createRow(JPanel labelParent, JPanel fieldParent, String labelText, ActionListener listener) {
    if(labelText==null) { // create an empty row
      labelParent.add(new JLabel());
      fieldParent.add(new JLabel());
      return null;
    }
    //    if(labelText.length()<14) {}
    JLabel label = new JLabel(ToolsRes.getString("CameraInspector."+labelText)); //$NON-NLS-1$
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setBorder(new EmptyBorder(0, 3, 0, 3));
    JTextField field = new JTextField(4);
    field.setActionCommand(labelText.toLowerCase());
    field.addActionListener(listener);
    labelParent.add(label);
    fieldParent.add(field);
    return field;
  }

}

class CameraInspectorFrame extends JFrame {
  private CameraInspector inspector;

  /**
   * Constructor CameraInspectorFrame
   * @param title
   * @param anInspector
   */
  public CameraInspectorFrame(String title, CameraInspector anInspector) {
    super(title);
    inspector = anInspector;
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(inspector, BorderLayout.CENTER);
    pack();
  }

  public void setVisible(boolean vis) {
    super.setVisible(vis);
    if(vis) {
      inspector.updateFields();
    }
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
