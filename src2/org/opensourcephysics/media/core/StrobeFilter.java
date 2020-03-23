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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This is a Filter that produces fading strobe images.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class StrobeFilter extends Filter {
  // instance fields
  protected int[] pixels, prevPixels;
  private double fade;
  private double defaultFade = 0;
  private boolean brightTrails = false;
  // inspector fields
  private Inspector inspector;
  private JLabel fadeLabel;
  private NumberField fadeField;
  private JSlider fadeSlider;
  private JRadioButton darkButton, brightButton;

  /**
   * Constructs a StrobeFilter object with default fade.
   */
  public StrobeFilter() {
    setFade(defaultFade);
    hasInspector = true;
  }

  /**
   * Sets the fade.
   *
   * @param fade the fraction by which the strobe image fades each time it is
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
   * Sets the bright trails flag. When true, trails are bright on dark
   * backgrounds and fade to black. Otherwise trails are dark on bright
   * backgrounds and fade to white.
   *
   * @param light true for bright trails on dark backgrounds
   */
  public void setBrightTrails(boolean bright) {
    brightTrails = bright;
  	clear();
  }

  /**
   * Gets the bright trails flag.
   *
   * @return true if trails are bright
   * @see #setBrightTrails
   */
  public boolean isBrightTrails() {
    return brightTrails;
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
    setOutputToStrobe();
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
   * Clears strobe images.
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
      inspector.setTitle(MediaRes.getString("Filter.Strobe.Title")); //$NON-NLS-1$
	    fadeLabel.setText(MediaRes.getString("Filter.Ghost.Label.Fade"));           //$NON-NLS-1$
	    fadeSlider.setToolTipText(MediaRes.getString("Filter.Ghost.ToolTip.Fade")); //$NON-NLS-1$
	    brightButton.setText(MediaRes.getString("Filter.Strobe.RadioButton.Bright"));           //$NON-NLS-1$
	    brightButton.setToolTipText(MediaRes.getString("Filter.Strobe.RadioButton.Bright.Tooltip")); //$NON-NLS-1$
	    darkButton.setText(MediaRes.getString("Filter.Strobe.RadioButton.Dark"));           //$NON-NLS-1$
	    darkButton.setToolTipText(MediaRes.getString("Filter.Strobe.RadioButton.Dark.Tooltip")); //$NON-NLS-1$

	    boolean enabled = isEnabled();
	    brightButton.setEnabled(enabled);
	    darkButton.setEnabled(enabled);
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
    prevPixels = new int[w*h];
    if(source.getType()==BufferedImage.TYPE_INT_RGB) {
      input = source;
    } else {
      input = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      gIn = input.createGraphics();
    }
    output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    output.createGraphics().drawImage(source, 0, 0, null);
    output.getRaster().getDataElements(0, 0, w, h, pixels);
    for(int i = 0; i<prevPixels.length; i++) {
      prevPixels[i] = pixels[i]; // value
    }
  }

  /**
   * Sets the output image pixels to a strobe of the input pixels.
   */
  private void setOutputToStrobe() {
    input.getRaster().getDataElements(0, 0, w, h, pixels);
    int pixel, r, g, b, val, rprev, gprev, bprev, valprev;
    for(int i = 0; i<pixels.length; i++) {
      pixel = pixels[i];
      r = (pixel>>16)&0xff;                       // red
      g = (pixel>>8)&0xff;                        // green
      b = (pixel)&0xff;                           // blue
      val = (r+g+b)/3;                            // value of current input pixel
      rprev = (prevPixels[i]>>16)&0xff;           // previous red
      gprev = (prevPixels[i]>>8)&0xff;            // previous green
      bprev = (prevPixels[i])&0xff;               // previous blue
      valprev = (rprev+gprev+bprev)/3;            // previous value
      if (brightTrails) { // bright trails fade to black
      	valprev = (int) ((1-fade)*valprev);         // faded previous value
        if(valprev>val) {
          rprev = (int) ((1-fade)*rprev);           // faded red
          gprev = (int) ((1-fade)*gprev);           // faded green
          bprev = (int) ((1-fade)*bprev);           // faded blue
          pixels[i] = (rprev<<16)|(gprev<<8)| bprev; 
        }
      }
      else { // dark trails fade to white
      	valprev = (int) (255-(1-fade)*(255-valprev)); // faded previous value
        if(val>valprev) {
          rprev = (int) (255-(1-fade)*(255-rprev));   // faded red
          gprev = (int) (255-(1-fade)*(255-gprev));   // faded green
          bprev = (int) (255-(1-fade)*(255-bprev));   // faded blue
          pixels[i] = (rprev<<16)|(gprev<<8)| bprev; 
        }
      }
      prevPixels[i] = pixels[i];
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
      setTitle(MediaRes.getString("Filter.Strobe.Title")); //$NON-NLS-1$
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
      fadeLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
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

      ButtonGroup group = new ButtonGroup();
      ActionListener brightDarkAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setBrightTrails(brightButton.isSelected());
        }
      };
      brightButton = new JRadioButton();
      darkButton = new JRadioButton();
      group.add(brightButton);
      group.add(darkButton);
      darkButton.setSelected(!brightTrails);
      brightButton.addActionListener(brightDarkAction);
      darkButton.addActionListener(brightDarkAction);
      
      JPanel panel = new JPanel(new BorderLayout());
      setContentPane(panel);
      JPanel fadePanel = new JPanel(new FlowLayout());
      fadePanel.add(fadeLabel);
      fadePanel.add(fadeField);
      fadePanel.add(fadeSlider);
      panel.add(fadePanel, BorderLayout.NORTH);
      
      JPanel brightDarkBar = new JPanel(new FlowLayout());
      brightDarkBar.add(brightButton);
      brightDarkBar.add(darkButton);
      panel.add(brightDarkBar, BorderLayout.CENTER);
      
      JPanel buttonbar = new JPanel(new FlowLayout());
      buttonbar.add(ableButton);
      buttonbar.add(clearButton);
      buttonbar.add(closeButton);
      panel.add(buttonbar, BorderLayout.SOUTH);
            
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
      if (isBrightTrails()) brightButton.setSelected(true);
      else darkButton.setSelected(true);
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
    	StrobeFilter filter = (StrobeFilter) obj;
      control.setValue("fade", filter.getFade()); //$NON-NLS-1$
      if (filter.isBrightTrails()) {
	      control.setValue("bright_trails", filter.isBrightTrails()); //$NON-NLS-1$      	
      }
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
      return new StrobeFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      StrobeFilter filter = (StrobeFilter) obj;
      if(control.getPropertyNames().contains("fade")) { //$NON-NLS-1$
        filter.setFade(control.getDouble("fade"));      //$NON-NLS-1$
      }
      filter.setBrightTrails(control.getBoolean("bright_trails"));      //$NON-NLS-1$
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
