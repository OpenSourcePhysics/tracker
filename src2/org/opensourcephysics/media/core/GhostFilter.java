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

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This is a Filter that produces fading ghost images of bright objects
 * on a dark background.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class GhostFilter extends Filter {
  // instance fields
  protected int[] pixels, values;
  private double fade;
  private double defaultFade = 0.05;
  // inspector fields
  private Inspector inspector;
  private JLabel fadeLabel;
  private NumberField fadeField;
  private JSlider fadeSlider;

  /**
   * Constructs a GhostFilter object with default fade.
   */
  public GhostFilter() {
    setFade(defaultFade);
    hasInspector = true;
  }

  /**
   * Sets the fade.
   *
   * @param fade the fraction by which a ghost image fades each time it is
   * rendered. A fade of 0 never fades, while a fade of 1 fades completely
   * and so is never seen.
   */
  public void setFade(double fade) {
    Double prev = new Double(this.fade);
    this.fade = Math.min(Math.abs(fade), 1);
    support.firePropertyChange("fade", prev, new Double(fade)); //$NON-NLS-1$
  }

  /**
   * Gets the fade.
   *
   * @return the fade.
   * @see #setFade
   */
  public double getFade() {
    return fade;
  }

  /**
   * Overrides the setEnabled method to force reinitialization.
   *
   * @param enabled <code>true</code> if this is enabled.
   */
  public void setEnabled(boolean enabled) {
    if(isEnabled()==enabled) {
      return;
    }
    source = null;
    super.setEnabled(enabled);
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
    setOutputToGhost();
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
   * Clears ghosts.
   */
  public void clear() {
    source = null;
    support.firePropertyChange("image", null, null); //$NON-NLS-1$
  }

  /**
   * Refreshes this filter's GUI
   */
  public void refresh() {
    super.refresh();
    if(inspector!=null) {
      inspector.setTitle(MediaRes.getString("Filter.Ghost.Title")); //$NON-NLS-1$
	    fadeLabel.setText(MediaRes.getString("Filter.Ghost.Label.Fade"));           //$NON-NLS-1$
	    fadeSlider.setToolTipText(MediaRes.getString("Filter.Ghost.ToolTip.Fade")); //$NON-NLS-1$
	    boolean enabled = isEnabled();
	    fadeLabel.setEnabled(enabled);
	    fadeSlider.setEnabled(enabled);
	    fadeField.setEnabled(enabled);
      inspector.pack();
    }
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
    pixels = new int[w*h];
    values = new int[w*h];
    if(source.getType()==BufferedImage.TYPE_INT_RGB) {
      input = source;
    } else {
      input = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      gIn = input.createGraphics();
    }
    output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    output.createGraphics().drawImage(source, 0, 0, null);
    output.getRaster().getDataElements(0, 0, w, h, pixels);
    int pixel, r, g, b;
    for(int i = 0; i<values.length; i++) {
      pixel = pixels[i];
      r = (pixel>>16)&0xff;  // red
      g = (pixel>>8)&0xff;   // green
      b = (pixel)&0xff;      // blue
      values[i] = (r+g+b)/3; // value
    }
  }

  /**
   * Sets the output image pixels to a ghost of the input pixels.
   */
  private void setOutputToGhost() {
    input.getRaster().getDataElements(0, 0, w, h, pixels);
    int pixel, r, g, b, v, ghost;
    for(int i = 0; i<pixels.length; i++) {
      pixel = pixels[i];
      r = (pixel>>16)&0xff;                       // red
      g = (pixel>>8)&0xff;                        // green
      b = (pixel)&0xff;                           // blue
      v = (r+g+b)/3;                              // value of current input pixel
      ghost = (int) ((1-fade)*values[i]);         // faded value of prev input
      if(ghost>v) {
        pixels[i] = (ghost<<16)|(ghost<<8)|ghost; // grey
        values[i] = ghost;
      } else {
        values[i] = v;
      }
    }
    output.getRaster().setDataElements(0, 0, w, h, pixels);
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
      setTitle(MediaRes.getString("Filter.Ghost.Title")); //$NON-NLS-1$
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
      fadeLabel = new JLabel();
      fadeField = new DecimalField(4, 2);
      fadeField.setMaxValue(0.5);
      fadeField.setMinValue(0);
      fadeField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setFade(fadeField.getValue());
          updateDisplay();
          fadeField.selectAll();
        }

      });
      fadeField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          fadeField.selectAll();
        }
        public void focusLost(FocusEvent e) {
          setFade(fadeField.getValue());
          updateDisplay();
        }

      });
      fadeSlider = new JSlider(0, 0, 0);
      fadeSlider.setMaximum(50);
      fadeSlider.setMinimum(0);
      fadeSlider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      fadeSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int i = fadeSlider.getValue();
          if(i!=(int) (getFade()*100)) {
            setFade(i/100.0);
            updateDisplay();
          }
        }

      });
      // add components to content pane
      GridBagLayout gridbag = new GridBagLayout();
      JPanel panel = new JPanel(gridbag);
      setContentPane(panel);
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.EAST;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      c.gridx = 0;
      c.insets = new Insets(5, 5, 0, 2);
      gridbag.setConstraints(fadeLabel, c);
      panel.add(fadeLabel);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.gridx = 1;
      c.insets = new Insets(5, 0, 0, 0);
      gridbag.setConstraints(fadeField, c);
      panel.add(fadeField);
      c.gridx = 2;
      c.insets = new Insets(5, 0, 0, 0);
      c.weightx = 1.0;
      gridbag.setConstraints(fadeSlider, c);
      panel.add(fadeSlider);
      JPanel buttonbar = new JPanel(new FlowLayout());
      buttonbar.add(ableButton);
      buttonbar.add(clearButton);
      buttonbar.add(closeButton);
      c.gridx = 2;
      c.gridy = 1;
      gridbag.setConstraints(buttonbar, c);
      panel.add(buttonbar);
    }

    /**
     * Initializes this inspector
     */
    void initialize() {
      updateDisplay();
      refresh();
    }

    /**
     * Updates this inspector to reflect the current filter settings.
     */
    void updateDisplay() {
      fadeField.setValue(getFade());
      fadeSlider.setValue((int) (100*getFade()));
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
      GhostFilter filter = (GhostFilter) obj;
      control.setValue("fade", filter.getFade()); //$NON-NLS-1$
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
      return new GhostFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      final GhostFilter filter = (GhostFilter) obj;
      if(control.getPropertyNames().contains("fade")) { //$NON-NLS-1$
        filter.setFade(control.getDouble("fade"));      //$NON-NLS-1$
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
