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
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;

/**
 * This is a Filter that changes the brightness and contrast of a source image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class BrightnessFilter extends Filter {
  // instance fields
  private int[] pixels;
  private int defaultBrightness = 0;
  private double defaultContrast = 50;
  private int brightness = defaultBrightness, previousBrightness;
  private double contrast = defaultContrast, previousContrast;
  private double slope;
  private double offset1;
  private double offset2;
  // used by inspector
  private Inspector inspector;
  private JLabel brightnessLabel;
  private IntegerField brightnessField;
  private JSlider brightnessSlider;
  private JLabel contrastLabel;
  private NumberField contrastField;
  private JSlider contrastSlider;

  /**
   * Constructs a default BrightnessFilter object.
   */
  public BrightnessFilter() {
    setBrightness(defaultBrightness);
    setContrast(defaultContrast);
    hasInspector = true;
  }

  /**
   * Sets the contrast.
   *
   * @param contrast the contrast.
   */
  public void setContrast(double contrast) {
    if (previousState==null) {
    	previousState = new XMLControlElement(this).toXML();
    	previousBrightness = brightness;
    	previousContrast = contrast;
    }
    changed = changed || this.contrast!=contrast;
    Double prev = new Double(this.contrast);
    this.contrast = contrast;
    updateFactors();
    support.firePropertyChange("contrast", prev, new Double(contrast)); //$NON-NLS-1$
  }

  /**
   * Gets the contrast.
   *
   * @return the contrast.
   */
  public double getContrast() {
    return contrast;
  }

  /**
   * Sets the brightness.
   *
   * @param brightness the brightness.
   */
  public void setBrightness(int brightness) {
    if (previousState==null) {
    	previousState = new XMLControlElement(this).toXML();
    	previousBrightness = this.brightness;
    	previousContrast = this.contrast;
    }
    changed = changed || this.brightness!=brightness;
    Integer prev = new Integer(this.brightness);
    this.brightness = brightness;
    updateFactors();
    support.firePropertyChange("brightness", prev, new Integer(brightness)); //$NON-NLS-1$
  }

  /**
   * Gets the brightness.
   *
   * @return the brightness.
   */
  public int getBrightness() {
    return brightness;
  }

  /**
   * Determines if the filter settings have changed.
   * 
   * @return true if changed
   */
  @Override
  public boolean isChanged() {
  	if (!changed) return false;
  	// changes have occurred so compare final and initial states
  	return previousBrightness!=brightness || previousContrast!=contrast;
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
    setOutputToBright();
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
      myInspector.dispose();
      myInspector = new Inspector();
    }
    inspector = myInspector;
    inspector.initialize();
    return inspector;
  }

  /**
   * Clears this filter
   */
  public void clear() {
    setBrightness(defaultBrightness);
    setContrast(defaultContrast);
    if(inspector!=null) {
      inspector.updateDisplay();
    }
  }

  /**
   * Refreshes this filter's GUI
   */
  public void refresh() {
    super.refresh();
    brightnessLabel.setText(MediaRes.getString("Filter.Brightness.Label.Brightness"));           //$NON-NLS-1$
    brightnessSlider.setToolTipText(MediaRes.getString("Filter.Brightness.ToolTip.Brightness")); //$NON-NLS-1$
    contrastLabel.setText(MediaRes.getString("Filter.Brightness.Label.Contrast"));               //$NON-NLS-1$
    contrastSlider.setToolTipText(MediaRes.getString("Filter.Brightness.ToolTip.Contrast"));     //$NON-NLS-1$
    boolean enabled = isEnabled();
    brightnessLabel.setEnabled(enabled);
    brightnessSlider.setEnabled(enabled);
    brightnessField.setEnabled(enabled);
    contrastLabel.setEnabled(enabled);
    contrastSlider.setEnabled(enabled);
    contrastField.setEnabled(enabled);
    clearButton.setText(MediaRes.getString("Dialog.Button.Reset"));                //$NON-NLS-1$
    if(inspector!=null) {
      inspector.setTitle(MediaRes.getString("Filter.Brightness.Title")); //$NON-NLS-1$
      inspector.updateDisplay();
      inspector.pack();
    }
  }

  @Override
  public void dispose() {
  	if (gIn!=null) gIn.dispose();
  	if (source!=null) source.flush();
  	if (input!=null) input.flush();
  	if (output!=null) output.flush();
  	super.dispose();
  	inspector = null;
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
   * Sets the output image pixels to a bright version of the input pixels.
   */
  private void setOutputToBright() {
    input.getRaster().getDataElements(0, 0, w, h, pixels);
    int pixel, r, g, b;
    for(int i = 0; i<pixels.length; i++) {
      pixel = pixels[i];
      r = (pixel>>16)&0xff; // red
      r = Math.max((int) (slope*(r+offset1)+offset2), 0);
      r = Math.min(r, 255);
      g = (pixel>>8)&0xff;  // green
      g = Math.max((int) (slope*(g+offset1)+offset2), 0);
      g = Math.min(g, 255);
      b = (pixel)&0xff;     // blue
      b = Math.max((int) (slope*(b+offset1)+offset2), 0);
      b = Math.min(b, 255);
      pixels[i] = (r<<16)|(g<<8)|b;
    }
    output.getRaster().setDataElements(0, 0, w, h, pixels);
  }

  /**
   * Updates factors used to convert pixel values
   */
  private void updateFactors() {
    double theta = Math.PI*contrast/200;
    double sin = Math.sin(theta);
    offset1 = sin*sin*brightness-127;
    double cos = Math.cos(theta);
    offset2 = 127+cos*cos*brightness;
    slope = sin/cos;
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
      setTitle(MediaRes.getString("Filter.Brightness.Title")); //$NON-NLS-1$
      addWindowFocusListener(new java.awt.event.WindowAdapter() {
      	@Override
      	public void windowLostFocus(java.awt.event.WindowEvent e) {
          if (isChanged() && previousState!=null) {
          	changed = false;
            support.firePropertyChange("filterChanged", previousState, BrightnessFilter.this); //$NON-NLS-1$
            previousState = null;
          }
      	}
      });

      // create brightness components
      brightnessLabel = new JLabel();
      brightnessField = new IntegerField(3);
      brightnessField.setMaxValue(128);
      brightnessField.setMinValue(-128);
      brightnessField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setBrightness(brightnessField.getIntValue());
          updateDisplay();
          brightnessField.selectAll();
        }

      });
      brightnessField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          brightnessField.selectAll();
        }
        public void focusLost(FocusEvent e) {
          setBrightness(brightnessField.getIntValue());
          updateDisplay();
        }

      });
      brightnessSlider = new JSlider(0, 0, 0);
      brightnessSlider.setMaximum(128);
      brightnessSlider.setMinimum(-128);
      brightnessSlider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      brightnessSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int i = brightnessSlider.getValue();
          if(i!=getBrightness()) {
            setBrightness(i);
            updateDisplay();
          }
        }

      });
      // create contrast components
      contrastLabel = new JLabel();
      contrastField = new DecimalField(4, 1);
      contrastField.setMaxValue(100);
      contrastField.setMinValue(0);
      contrastField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setContrast(contrastField.getValue());
          updateDisplay();
          contrastField.selectAll();
        }

      });
      contrastField.addFocusListener(new FocusListener() {
        public void focusGained(FocusEvent e) {
          contrastField.selectAll();
        }
        public void focusLost(FocusEvent e) {
          setContrast(contrastField.getValue());
          updateDisplay();
        }

      });
      contrastSlider = new JSlider(0, 0, 0);
      contrastSlider.setMaximum(100);
      contrastSlider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      contrastSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int i = contrastSlider.getValue();
          if(i!=(int) getContrast()) {
            setContrast(i);
            updateDisplay();
          }
        }

      });
      // add components to content pane
      JLabel[] labels = new JLabel[] {brightnessLabel, contrastLabel};
      JTextField[] fields = new JTextField[] {brightnessField, contrastField};
      JSlider[] sliders = new JSlider[] {brightnessSlider, contrastSlider};
      GridBagLayout gridbag = new GridBagLayout();
      JPanel panel = new JPanel(gridbag);
      setContentPane(panel);
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.EAST;
      int i = 0;
      for(; i<labels.length; i++) {
        c.gridy = i;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.gridx = 0;
        c.insets = new Insets(5, 5, 0, 2);
        gridbag.setConstraints(labels[i], c);
        panel.add(labels[i]);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.insets = new Insets(5, 0, 0, 0);
        gridbag.setConstraints(fields[i], c);
        panel.add(fields[i]);
        c.gridx = 2;
        c.insets = new Insets(5, 0, 0, 0);
        c.weightx = 1.0;
        gridbag.setConstraints(sliders[i], c);
        panel.add(sliders[i]);
      }
      JPanel buttonbar = new JPanel(new FlowLayout());
      buttonbar.add(ableButton);
      buttonbar.add(clearButton);
      buttonbar.add(closeButton);
      c.gridx = 2;
      c.gridy = i+1;
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
      brightnessField.setIntValue(getBrightness());
      contrastField.setValue(getContrast());
      brightnessSlider.setValue(getBrightness());
      contrastSlider.setValue((int) getContrast());
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
      BrightnessFilter filter = (BrightnessFilter) obj;
      control.setValue("brightness", filter.getBrightness()); //$NON-NLS-1$
      control.setValue("contrast", filter.getContrast());     //$NON-NLS-1$
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
      return new BrightnessFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      final BrightnessFilter filter = (BrightnessFilter) obj;
      if(control.getPropertyNames().contains("brightness")) { //$NON-NLS-1$
        filter.setBrightness(control.getInt("brightness"));   //$NON-NLS-1$
      }
      if(control.getPropertyNames().contains("contrast")) { //$NON-NLS-1$
        filter.setContrast(control.getDouble("contrast"));  //$NON-NLS-1$
      }
      filter.inspectorX = control.getInt("inspector_x"); //$NON-NLS-1$
      filter.inspectorY = control.getInt("inspector_y"); //$NON-NLS-1$
      filter.previousState = null;
      filter.changed = false;
      if (filter.inspector!=null) {
        filter.inspector.updateDisplay();
      }
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
