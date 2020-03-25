/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This is a Filter that changes the pixel dimensions of an image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ResizeFilter extends Filter {
  // instance fields
  private double widthFactor = 1.0;
  private double heightFactor = 1.0;
  private Graphics2D gOut;
  // inspector fields
  private Inspector inspector;
  private JLabel widthLabel;
  private JLabel heightLabel;
  private JLabel inputLabel;
  private JLabel outputLabel;
  private IntegerField widthInField;
  private IntegerField heightInField;
  private IntegerField widthOutField;
  private IntegerField heightOutField;

  /**
   * Constructs a ResizeFilter.
   */
  public ResizeFilter() {
    hasInspector = true;
  }

  /**
   * Sets the width factor.
   *
   * @param factor the factor by which the width is resized
   */
  public void setWidthFactor(double factor) {
    source = null; // forces reinitialization
    Double prev = new Double(widthFactor);
    widthFactor = Math.min(Math.abs(factor), 10);
    widthFactor = Math.max(widthFactor, 0.01);
    support.firePropertyChange("width", prev, new Double(widthFactor)); //$NON-NLS-1$
  }

  /**
   * Sets the hieght factor.
   *
   * @param factor the factor by which the width is resized
   */
  public void setHeightFactor(double factor) {
    source = null; // forces reinitialization
    Double prev = new Double(heightFactor);
    heightFactor = Math.min(Math.abs(factor), 10);
    heightFactor = Math.max(heightFactor, 0.01);
    support.firePropertyChange("height", prev, new Double(heightFactor)); //$NON-NLS-1$
  }

  /**
   * Gets the width factor.
   *
   * @return the factor.
   */
  public double getWidthFactor() {
    return widthFactor;
  }

  /**
   * Gets the height factor.
   *
   * @return the factor.
   */
  public double getHeightFactor() {
    return heightFactor;
  }

  /**
   * Applies the filter to a source image and returns the result.
   *
   * @param sourceImage the source image
   * @return the filtered image
   */
  public BufferedImage getFilteredImage(BufferedImage sourceImage) {
    if(!isEnabled()) {
      return sourceImage;
    }
    if(sourceImage!=source) {
      initialize(sourceImage);
    }
    if(sourceImage!=input) {
      gIn.drawImage(source, 0, 0, null);
    }
    gOut.drawImage(input, 0, 0, null);
    return output;
  }

  /**
   * Implements abstract Filter method.
   *
   * @return the inspector
   */
  public synchronized JDialog getInspector() {
  	Inspector myInspector = inspector;
    if (myInspector==null) {
    	myInspector = new Inspector();
    }
    if (myInspector.isModal() && vidPanel!=null) {
      frame = JOptionPane.getFrameForComponent(vidPanel);
      myInspector.setVisible(false);
      myInspector.dispose();
      myInspector = new Inspector();
    }
    inspector = myInspector;
    inspector.initialize();
    return inspector;
  }

  /**
   * Refreshes this filter's GUI
   */
  public void refresh() {
    super.refresh();
    widthLabel.setText(MediaRes.getString("Filter.Resize.Label.Width"));   //$NON-NLS-1$
    heightLabel.setText(MediaRes.getString("Filter.Resize.Label.Height")); //$NON-NLS-1$
    inputLabel.setText(MediaRes.getString("Filter.Resize.Label.Input"));   //$NON-NLS-1$
    outputLabel.setText(MediaRes.getString("Filter.Resize.Label.Output")); //$NON-NLS-1$
    if(inspector!=null) {
      inspector.setTitle(MediaRes.getString("Filter.Resize.Title")); //$NON-NLS-1$
      inspector.pack();
    }
    boolean enabled = isEnabled();
    inputLabel.setEnabled(enabled);
    outputLabel.setEnabled(enabled);
    heightLabel.setEnabled(enabled);
    widthLabel.setEnabled(enabled);
    widthInField.setEnabled(enabled);
    heightInField.setEnabled(enabled);
    widthOutField.setEnabled(enabled);
    heightOutField.setEnabled(enabled);
    int wOut = (int) (w*widthFactor);
    int hOut = (int) (h*heightFactor);
    widthInField.setIntValue(w);
    widthOutField.setIntValue(wOut);
    heightInField.setIntValue(h);
    heightOutField.setIntValue(hOut);
  }

  //_____________________________ private methods _______________________

  /**
   * Creates and initializes the input and output images.
   *
   * @param sourceImage a new source image
   */
  private void initialize(BufferedImage sourceImage) {
    source = sourceImage;
    w = source.getWidth();
    h = source.getHeight();
    // look for DV format and resize for square pixels by default
    if((w==720)&&(h==480)&&(widthFactor==1.0)&&(heightFactor==1.0)) {
      widthFactor = 0.889;
    }
    int wOut = (int) (w*widthFactor);
    int hOut = (int) (h*heightFactor);
    if(source.getType()==BufferedImage.TYPE_INT_RGB) {
      input = source;
    } else {
      input = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      gIn = input.createGraphics();
    }
    output = new BufferedImage(wOut, hOut, BufferedImage.TYPE_INT_RGB);
    AffineTransform transform = AffineTransform.getScaleInstance(widthFactor, heightFactor);
    gOut = output.createGraphics();
    gOut.setTransform(transform);
  }

  /**
   * Inner Inspector class to control filter parameters
   */
  private class Inspector extends JDialog {
    /**
     * Constructs the Inspector.
     */
    public Inspector() {
      super(frame, !(frame instanceof org.opensourcephysics.display.OSPFrame));
      setTitle(MediaRes.getString("Filter.Resize.Title")); //$NON-NLS-1$
      setResizable(false);
      createGUI();
      refresh();
      pack();
      // center on screen
      Rectangle rect = getBounds();
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width-rect.width)/2;
      int y = (dim.height-rect.height)/2;
      setLocation(x, y);
    }

    /**
     * Creates the visible components.
     */
    void createGUI() {
      // create components
      inputLabel = new JLabel();
      outputLabel = new JLabel();
      widthLabel = new JLabel();
      widthInField = new IntegerField(4);
      widthInField.setEditable(false);
      widthInField.format.applyPattern("0"); //$NON-NLS-1$
      widthOutField = new IntegerField(4);
      widthOutField.format.applyPattern("0"); //$NON-NLS-1$
//      widthOutField.setMaxValue(2000);
//      widthOutField.setMinValue(10);
      widthOutField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setWidthFactor(1.0*widthOutField.getIntValue()/w);
          refresh();
          widthOutField.selectAll();
        }

      });
      widthOutField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          widthOutField.selectAll();
        }
        public void focusLost(FocusEvent e) {
          setWidthFactor(1.0*widthOutField.getIntValue()/w);
          refresh();
        }

      });
      heightLabel = new JLabel();
      heightInField = new IntegerField(4);
      heightInField.setEditable(false);
      heightInField.format.applyPattern("0"); //$NON-NLS-1$
      heightOutField = new IntegerField(4);
      heightOutField.format.applyPattern("0"); //$NON-NLS-1$
//      heightOutField.setMaxValue(1000);
//      heightOutField.setMinValue(100);
      heightOutField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setHeightFactor(1.0*heightOutField.getIntValue()/h);
          refresh();
          heightOutField.selectAll();
        }

      });
      heightOutField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          heightOutField.selectAll();
        }
        public void focusLost(FocusEvent e) {
          setHeightFactor(1.0*heightOutField.getIntValue()/h);
          refresh();
        }

      });
      // add components to content pane
      JPanel contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);
      GridBagLayout gridbag = new GridBagLayout();
      JPanel panel = new JPanel(gridbag);
      contentPane.add(panel, BorderLayout.CENTER);
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.CENTER;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.5;
      c.gridx = 1;
      c.gridy = 0;
      c.insets = new Insets(4, 2, 0, 2);
      gridbag.setConstraints(widthLabel, c);
      panel.add(widthLabel);
      c.gridx = 2;
      c.insets = new Insets(4, 0, 0, 8);
      gridbag.setConstraints(heightLabel, c);
      panel.add(heightLabel);
      c.gridx = 0;
      c.gridy = 1;
      c.anchor = GridBagConstraints.EAST;
      c.insets = new Insets(0, 4, 0, 2);
      c.weightx = 0.2;
      gridbag.setConstraints(inputLabel, c);
      panel.add(inputLabel);
      c.gridy = 2;
      gridbag.setConstraints(outputLabel, c);
      panel.add(outputLabel);
      c.gridy = 1;
      c.gridx = 1;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(4, 2, 0, 2);
      gridbag.setConstraints(widthInField, c);
      panel.add(widthInField);
      c.gridx = 2;
      c.insets = new Insets(4, 0, 0, 8);
      gridbag.setConstraints(heightInField, c);
      panel.add(heightInField);
      c.gridy = 2;
      c.gridx = 1;
      c.insets = new Insets(4, 2, 0, 2);
      gridbag.setConstraints(widthOutField, c);
      panel.add(widthOutField);
      c.gridx = 2;
      c.insets = new Insets(4, 0, 0, 8);
      gridbag.setConstraints(heightOutField, c);
      panel.add(heightOutField);
      JPanel buttonbar = new JPanel(new FlowLayout());
      buttonbar.add(ableButton);
      buttonbar.add(closeButton);
      contentPane.add(buttonbar, BorderLayout.SOUTH);
    }

    /**
     * Initializes this inspector
     */
    void initialize() {
      refresh();
    }

  }

  /**
   * Returns an XML.ObjectLoader to save and load filter data.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load filter data.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the filter to save
     */
    public void saveObject(XMLControl control, Object obj) {
      ResizeFilter filter = (ResizeFilter) obj;
      control.setValue("width_factor", filter.widthFactor);   //$NON-NLS-1$
      control.setValue("height_factor", filter.heightFactor); //$NON-NLS-1$
      if((filter.frame!=null)&&(filter.inspector!=null)&&filter.inspector.isVisible()) {
        int x = filter.inspector.getLocation().x-filter.frame.getLocation().x;
        int y = filter.inspector.getLocation().y-filter.frame.getLocation().y;
        control.setValue("inspector_x", x); //$NON-NLS-1$
        control.setValue("inspector_y", y); //$NON-NLS-1$
      }
    }

    /**
     * Creates a new filter.
     *
     * @param control the control
     * @return the new filter
     */
    public Object createObject(XMLControl control) {
      return new ResizeFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      final ResizeFilter filter = (ResizeFilter) obj;
      if(control.getPropertyNames().contains("width_factor")) {   //$NON-NLS-1$
        filter.setWidthFactor(control.getDouble("width_factor")); //$NON-NLS-1$
      }
      if(control.getPropertyNames().contains("height_factor")) {    //$NON-NLS-1$
        filter.setHeightFactor(control.getDouble("height_factor")); //$NON-NLS-1$
      }
      filter.inspectorX = control.getInt("inspector_x"); //$NON-NLS-1$
      filter.inspectorY = control.getInt("inspector_y"); //$NON-NLS-1$
      return obj;
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
