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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This is a Filter that rotates the source image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class RotateFilter extends Filter {
	// static constants
	private static final int NONE = -1, CCW_90 = 0, CW_90 = 1, FULL_180 = 2;
	private static final int[] types = {NONE, CCW_90, CW_90, FULL_180};
	private static final String[] typeNames = {"None", "CCW",  //$NON-NLS-1$ //$NON-NLS-2$
		"CW", "180"}; //$NON-NLS-1$ //$NON-NLS-2$
	private static Icon cwIcon, ccwIcon;
	
	static {
    String path = "/org/opensourcephysics/resources/media/images/cw.gif";  //$NON-NLS-1$
    cwIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/ccw.gif";  //$NON-NLS-1$
    ccwIcon = ResourceLoader.getIcon(path);
	}
	
  // instance fields
  private int[] pixelsIn, pixelsOut;
  private int rotationType = NONE; // no rotation
  // inspector fields
  private Inspector inspector;
  private JRadioButtonMenuItem[] buttons = new JRadioButtonMenuItem[4];
  private ButtonGroup buttonGroup;
  private JCheckBox reverseCheckbox;
  private JComponent rotationPanel;
  private JComponent reversePanel;
  private boolean reverse;

  /**
   * Constructs a RotateFilter object.
   */
  public RotateFilter() {
    refresh();
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
    setOutputToRotate(input);
    return output;
  }

  /**
   * Sets the rotation type.
   *
   * @param type 
   */
  private void setRotationType(int type) {
    if (type!=rotationType) {
    	rotationType = type;
    	source = null; // forces re-initialization
      support.firePropertyChange("rotate", null, null); //$NON-NLS-1$
    }
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
   * Refreshes this filter's GUI
   */
  public void refresh() {
    super.refresh();
    if(inspector!=null) {
      inspector.setTitle(MediaRes.getString("Filter.Rotate.Title")); //$NON-NLS-1$
      rotationPanel.setBorder(BorderFactory.createTitledBorder(MediaRes.getString("Filter.Rotate.Label.Rotate"))); //$NON-NLS-1$
      for (int i = 0; i<buttons.length; i++) {
      	buttons[i].setEnabled(isEnabled());
      	buttons[i].setText(MediaRes.getString("Filter.Rotate.Button."+typeNames[i])); //$NON-NLS-1$
      }
      reverseCheckbox.setText(MediaRes.getString("Filter.Rotate.Checkbox.Reverse")); //$NON-NLS-1$
      reverseCheckbox.setSelected(reverse);
    }
  }

  //_____________________________ private methods _______________________

  /**
   * Creates the input and output images and ColorConvertOp.
   *
   * @param image a new input image
   */
  private void initialize(BufferedImage image) {
    source = image;
    w = source.getWidth();
    h = source.getHeight();
    pixelsIn = new int[w*h];
    pixelsOut = new int[w*h];    
    if (rotationType==CW_90 || rotationType==CCW_90)
    	output = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);
    else
      output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    if(source.getType()==BufferedImage.TYPE_INT_RGB) {
      input = source;
    } else {
      input = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
      gIn = input.createGraphics();
    }
  }

  /**
   * Sets the output image pixels to a rotated version of the input pixels.
   *
   * @param image the input image
   */
  private void setOutputToRotate(BufferedImage image) {
    image.getRaster().getDataElements(0, 0, w, h, pixelsIn);
    int last = w*h-1;
    if (rotationType>NONE || reverse) {
	    for(int i = 0; i<pixelsIn.length; i++) {
	    	if (rotationType==NONE) { // no rotation, just reversed
	    		int row = i/w;
	    		int col = w-(i%w)-1;
	    		pixelsOut[w*row+col] = pixelsIn[i];
		    }
	    	else if (rotationType==CW_90) {
	    		if (reverse) {
		    		int col = h-(i/w)-1;
		    		int row = w-(i%w)-1;
		    		pixelsOut[h*row+col] = pixelsIn[i];
	    		}
	    		else {
		    		int col = h-(i/w)-1;
		    		int row = i%w;
		    		pixelsOut[h*row+col] = pixelsIn[i];
	    		}
	    	}
	    	else if (rotationType==CCW_90) {
	    		if (reverse) {
		    		int col = i/w;
		    		int row = i%w;
		    		pixelsOut[h*row+col] = pixelsIn[i];
	    		}
	    		else {
		    		int col = i/w;
		    		int row = w-(i%w)-1;
		    		pixelsOut[h*row+col] = pixelsIn[i];
	    		}
	    	}
	    	else { // 180 degrees
	    		if (reverse) {
		    		int row = h-(i/w)-1;
		    		int col = i%w;
		    		pixelsOut[w*row+col] = pixelsIn[i];
	    		}
	    		else
	    			pixelsOut[last-i] = pixelsIn[i];
	    	}
	    }
    }
    if (rotationType==NONE && !reverse)
  		output.getRaster().setDataElements(0, 0, w, h, pixelsIn);
    else if (rotationType==CW_90 || rotationType==CCW_90)
  		output.getRaster().setDataElements(0, 0, h, w, pixelsOut);
  	else
    	output.getRaster().setDataElements(0, 0, w, h, pixelsOut);
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
      setTitle(MediaRes.getString("Filter.Rotate.Title")); //$NON-NLS-1$
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
      // create and assemble radio buttons
    	int leftBorder = 40; // space to left of buttons
      rotationPanel = Box.createVerticalBox();
      buttonGroup = new ButtonGroup();
      ActionListener selector = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	for (int i = 0; i<buttons.length; i++) {
        		if (buttons[i].isSelected()) {
        			setRotationType(types[i]);
        			break;
        		}
        	}
        }
      };
      for(int i = 0; i<buttons.length; i++) {
        buttons[i] = new JRadioButtonMenuItem();
        buttons[i].setSelected(rotationType==types[i]);
        buttons[i].addActionListener(selector);
        buttons[i].setBorder(BorderFactory.createEmptyBorder(2, leftBorder, 2, 2));
        buttons[i].setHorizontalTextPosition(SwingConstants.LEFT);
        if (types[i]==CW_90)
        	buttons[i].setIcon(cwIcon);
        else if (types[i]==CCW_90)
        	buttons[i].setIcon(ccwIcon);
        buttonGroup.add(buttons[i]);
      	rotationPanel.add(buttons[i]);
      }
      // create and assemble reverse checkbox
      reversePanel = Box.createVerticalBox();
      reverseCheckbox = new JCheckBox();
      reverseCheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	reverse = reverseCheckbox.isSelected();
          support.firePropertyChange("rotate", null, null); //$NON-NLS-1$
        }
      });
      reversePanel.add(reverseCheckbox);
      reverseCheckbox.setBorder(BorderFactory.createEmptyBorder(2, leftBorder+7, 2, 2));
      // add components to content pane
      JPanel contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);
      contentPane.add(rotationPanel, BorderLayout.NORTH);
      contentPane.add(reversePanel, BorderLayout.CENTER);
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
      updateDisplay();
    }

    /**
     * Updates this inspector to reflect the current filter settings.
     */
    void updateDisplay() {
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
      RotateFilter filter = (RotateFilter) obj;
      if (filter.rotationType>NONE)
      	control.setValue("rotation", RotateFilter.typeNames[filter.rotationType+1]); //$NON-NLS-1$
      control.setValue("reverse", filter.reverse); //$NON-NLS-1$
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
      return new RotateFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      final RotateFilter filter = (RotateFilter) obj;
      String typeName = control.getString("rotation"); //$NON-NLS-1$ // could be null
      for (int i = 0; i<RotateFilter.typeNames.length; i++) {
      	if (RotateFilter.typeNames[i].equals(typeName))
      		filter.rotationType = RotateFilter.types[i];
      }
      filter.reverse = control.getBoolean("reverse"); //$NON-NLS-1$
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
