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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This is a Filter that produces a grayscale version of the source.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class GrayScaleFilter extends Filter {
  // static constants
  private static final double WT_R_VID = 0.30;
  private static final double WT_G_VID = 0.59;
  private static final double WT_B_VID = 0.11;
  private static final double WT_FLAT = 1.0/3;
  // instance fields
  private int[] pixels;
  private double redWt, greenWt, blueWt;
  // inspector fields
  private Inspector inspector;
  private JRadioButton vidButton, flatButton, customButton;
  private ButtonGroup buttons;
  private JLabel[] colorLabels = new JLabel[3];
  private NumberField[] colorFields = new NumberField[3];
  private JComponent typePanel;
  private JComponent rgbPanel;

  /**
   * Constructs a GrayScaleFilter object.
   */
  public GrayScaleFilter() {
    setWeights(WT_R_VID, WT_G_VID, WT_B_VID);
    hasInspector = true;
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
    setOutputToGray();
    return output;
  }

  /**
   * Gets the inspector for this filter.
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
   * Sets the weighting factors.
   *
   * @param r red factor
   * @param g green factor
   * @param b blue factor
   */
  public void setWeights(double r, double g, double b) {
    redWt = r;
    greenWt = g;
    blueWt = b;
  }

  /**
   * Refreshes this filter's GUI
   */
  public void refresh() {
    super.refresh();
    if(inspector!=null) {
      inspector.setTitle(MediaRes.getString("Filter.GrayScale.Title")); //$NON-NLS-1$
    }
    typePanel.setBorder(BorderFactory.createTitledBorder(MediaRes.getString("Filter.GrayScale.Label.Type"))); //$NON-NLS-1$
    rgbPanel.setBorder(BorderFactory.createTitledBorder(MediaRes.getString("Filter.GrayScale.Label.Weight"))); //$NON-NLS-1$
    vidButton.setText(MediaRes.getString("Filter.GrayScale.Button.Video"));     //$NON-NLS-1$
    flatButton.setText(MediaRes.getString("Filter.GrayScale.Button.Flat"));     //$NON-NLS-1$
    customButton.setText(MediaRes.getString("Filter.GrayScale.Button.Custom")); //$NON-NLS-1$
    colorLabels[0].setText(MediaRes.getString("Filter.GrayScale.Label.Red"));   //$NON-NLS-1$
    colorLabels[1].setText(MediaRes.getString("Filter.GrayScale.Label.Green")); //$NON-NLS-1$
    colorLabels[2].setText(MediaRes.getString("Filter.GrayScale.Label.Blue"));  //$NON-NLS-1$
    vidButton.setEnabled(isEnabled());
    flatButton.setEnabled(isEnabled());
    customButton.setEnabled(isEnabled());
    for(int i = 0; i<3; i++) {
      colorFields[i].setEditable(buttons.isSelected(customButton.getModel()));
      colorFields[i].setEnabled(isEnabled());
      colorLabels[i].setEnabled(isEnabled());
    }
  }

  //_____________________________ private methods _______________________

  /**
   * Creates the input and output images.
   *
   * @param image a new input image
   */
  private void initialize(BufferedImage image) {
    source = image;
    w = source.getWidth();
    h = source.getHeight();
    pixels = new int[w*h];
    output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    if(source.getType()==BufferedImage.TYPE_INT_RGB) {
      input = source;
    } else {
      input = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      gIn = input.createGraphics();
    }
  }

  /**
   * Sets the output image pixels to the grayscale of the input pixels.
   */
  private void setOutputToGray() {
    input.getRaster().getDataElements(0, 0, w, h, pixels);
    int pixel, r, g, b, v;
    for(int i = 0; i<pixels.length; i++) {
      pixel = pixels[i];
      r = (pixel>>16)&0xff;         // red
      g = (pixel>>8)&0xff;          // green
      b = (pixel)&0xff;             // blue
      v = getGray(r, g, b);
      pixels[i] = (v<<16)|(v<<8)|v; // grey
    }
    output.getRaster().setDataElements(0, 0, w, h, pixels);
  }

  /**
   * Returns the brightness.
   *
   * @return the brightness
   */
  private int getGray(int r, int g, int b) {
    double gray = (redWt*r+greenWt*g+blueWt*b)/(redWt+greenWt+blueWt);
    return(int) gray;
  }

  private void setWeights(double[] weights) {
    redWt = weights[0];
    greenWt = weights[1];
    blueWt = weights[2];
  }

  private double[] getWeights() {
    return new double[] {redWt, greenWt, blueWt};
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
      setResizable(false);
      createGUI();
      setTitle(MediaRes.getString("Filter.GrayScale.Title")); //$NON-NLS-1$
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
      for(int i = 0; i<3; i++) {
        colorLabels[i] = new JLabel();
        colorFields[i] = new DecimalField(3, 2);
        colorFields[i].setMaxValue(1.0);
        colorFields[i].setMinValue(0.0);
        colorFields[i].addActionListener(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            readFields((NumberField) e.getSource());
          }

        });
        colorFields[i].addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            ((NumberField) e.getSource()).selectAll();
          }
          public void focusLost(FocusEvent e) {
            readFields((NumberField) e.getSource());
          }

        });
      }
      // put rgb fields in the rgb panel
      GridBagLayout gridbag = new GridBagLayout();
      rgbPanel = new JPanel(gridbag);
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.EAST;
      for(int i = 0; i<3; i++) {
        c.gridy = i;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.gridx = 0;
        c.insets = new Insets(3, 20, 0, 3);
        gridbag.setConstraints(colorLabels[i], c);
        rgbPanel.add(colorLabels[i]);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.insets = new Insets(3, 0, 0, 5);
        gridbag.setConstraints(colorFields[i], c);
        rgbPanel.add(colorFields[i]);
      }
      // create radio buttons
      vidButton = new JRadioButton();
      flatButton = new JRadioButton();
      customButton = new JRadioButton();
      // create radio button group
      buttons = new ButtonGroup();
      buttons.add(vidButton);
      buttons.add(flatButton);
      buttons.add(customButton);
      ActionListener select = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(buttons.isSelected(vidButton.getModel())) {
            setWeights(WT_R_VID, WT_G_VID, WT_B_VID);
          } else if(buttons.isSelected(flatButton.getModel())) {
            setWeights(WT_FLAT, WT_FLAT, WT_FLAT);
          }
          refresh();
          updateDisplay();
          support.firePropertyChange("weight", null, null); //$NON-NLS-1$
        }

      };
      vidButton.addActionListener(select);
      flatButton.addActionListener(select);
      customButton.addActionListener(select);
      // put radio buttons in a box
      typePanel = Box.createVerticalBox();
      typePanel.add(vidButton);
      typePanel.add(flatButton);
      typePanel.add(customButton);
      // add components to content pane
      JPanel contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);
      contentPane.add(typePanel, BorderLayout.WEST);
      contentPane.add(rgbPanel, BorderLayout.EAST);
      JPanel buttonbar = new JPanel(new FlowLayout());
      buttonbar.add(ableButton);
      buttonbar.add(closeButton);
      contentPane.add(buttonbar, BorderLayout.SOUTH);
    }

    void readFields(NumberField source) {
      double[] rgb = new double[3];
      for(int i = 0; i<3; i++) {
        rgb[i] = colorFields[i].getValue();
      }
      setWeights(rgb);
      updateDisplay();
      support.firePropertyChange("weight", null, null); //$NON-NLS-1$
      source.selectAll();
    }

    /**
     * Initializes this inspector
     */
    void initialize() {
      if((redWt==WT_R_VID)&&(greenWt==WT_G_VID)&&(blueWt==WT_B_VID)) {
        vidButton.setSelected(true);
      } else if((redWt==WT_FLAT)&&(greenWt==WT_FLAT)&&(blueWt==WT_FLAT)) {
        flatButton.setSelected(true);
      } else {
        customButton.setSelected(true);
      }
      refresh();
      updateDisplay();
    }

    /**
     * Updates this inspector to reflect the current filter settings.
     */
    void updateDisplay() {
      colorFields[0].setValue(redWt);
      colorFields[1].setValue(greenWt);
      colorFields[2].setValue(blueWt);
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
      GrayScaleFilter filter = (GrayScaleFilter) obj;
      control.setValue("weights", filter.getWeights()); //$NON-NLS-1$
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
      return new GrayScaleFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      final GrayScaleFilter filter = (GrayScaleFilter) obj;
      if(control.getPropertyNames().contains("weights")) {          //$NON-NLS-1$
        filter.setWeights((double[]) control.getObject("weights")); //$NON-NLS-1$
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
