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
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * This is a Filter that returns only one field of an interlaced video image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DeinterlaceFilter extends Filter {
  // instance fields
  private int[] pixels;
  private boolean isOdd;
  private Inspector inspector;
  private JRadioButton odd;
  private JRadioButton even;

  /**
   * Constructs a default DeinterlaceFilter object.
   */
  public DeinterlaceFilter() {
    hasInspector = true;
  }

  /**
   * Sets the field to odd or even.
   *
   * @param odd true to extract the odd field
   */
  public void setOdd(boolean odd) {
    boolean prev = isOdd;
    isOdd = odd;
    support.firePropertyChange("odd", prev, odd); //$NON-NLS-1$
  }

  /**
   * Gets whether the extracted field is odd.
   *
   * @return true if the odd field is extracted
   */
  public boolean isOdd() {
    return isOdd;
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
    setOutputToField();
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
    odd.setText(MediaRes.getString("Filter.Deinterlace.Button.Odd"));   //$NON-NLS-1$
    even.setText(MediaRes.getString("Filter.Deinterlace.Button.Even")); //$NON-NLS-1$
    if(inspector!=null) {
      inspector.setTitle(MediaRes.getString("Filter.Deinterlace.Title")); //$NON-NLS-1$
      inspector.pack();
    }
    boolean enabled = isEnabled();
    odd.setEnabled(enabled);
    even.setEnabled(enabled);
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
   * Sets the output image pixels to a doubled version of the
   * input field pixels.
   */
  private void setOutputToField() {
    input.getRaster().getDataElements(0, 0, w, h, pixels);
    if(h%2!=0) {
      h = h-1; // make sure we have an even number of rows
    }
    // copy pixels in every other row
    for(int i = 0; i<h/2-1; i++) {
      for(int j = 0; j<w; j++) {
        if(isOdd) {
          pixels[w*2*i+j] = pixels[w*(2*i+1)+j];
        } else {
          pixels[w*(2*i+1)+j] = pixels[w*2*i+j];
        }
      }
    }
    output.getRaster().setDataElements(0, 0, w, h, pixels);
  }

  /**
   * Inner Inspector class to control filter parameters
   */
  private class Inspector extends JDialog {
    // instance fields
    ButtonGroup group;

    /**
     * Constructs the Inspector.
     */
    public Inspector() {
      super(frame, !(frame instanceof org.opensourcephysics.display.OSPFrame));
      setTitle(MediaRes.getString("Filter.Deinterlace.Title")); //$NON-NLS-1$
      setResizable(false);
      createGUI();
      initialize();
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
      // create radio buttons
      odd = new JRadioButton();
      even = new JRadioButton();
      // create radio button group
      group = new ButtonGroup();
      group.add(odd);
      group.add(even);
      ActionListener select = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setOdd(group.isSelected(odd.getModel()));
        }

      };
      even.addActionListener(select);
      odd.addActionListener(select);
      JPanel panel = new JPanel(new FlowLayout());
      panel.add(odd);
      panel.add(even);
      // assemble buttons
      JPanel buttonbar = new JPanel(new FlowLayout());
      buttonbar.add(ableButton);
      buttonbar.add(closeButton);
      JPanel contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);
      contentPane.add(panel, BorderLayout.CENTER);
      contentPane.add(buttonbar, BorderLayout.SOUTH);
    }

    /**
     * Initializes this inspector
     */
    void initialize() {
      updateDisplay();
    }

    /**
     * Updates this inspector to reflect the current filter settings.
     */
    void updateDisplay() {
      if(isOdd) {
        group.setSelected(odd.getModel(), true);
      } else {
        group.setSelected(even.getModel(), true);
      }
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
      DeinterlaceFilter filter = (DeinterlaceFilter) obj;
      if(filter.isOdd()) {
        control.setValue("field", "odd");  //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        control.setValue("field", "even"); //$NON-NLS-1$ //$NON-NLS-2$
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
      return new DeinterlaceFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      final DeinterlaceFilter filter = (DeinterlaceFilter) obj;
      if(control.getPropertyNames().contains("field")) { //$NON-NLS-1$
        if(control.getString("field").equals("odd")) { //$NON-NLS-1$ //$NON-NLS-2$
          filter.setOdd(true);                           
        } else {
          filter.setOdd(false);
        }
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
