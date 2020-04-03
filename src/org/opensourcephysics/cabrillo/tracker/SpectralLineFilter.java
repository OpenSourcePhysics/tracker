/*
 * The org.opensourcephysics.media package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2004  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
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
package org.opensourcephysics.cabrillo.tracker;

import java.beans.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

import org.opensourcephysics.controls.*;
import org.opensourcephysics.media.core.*;

/**
 * This is a Filter that draws gas spectral lines on the source image.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class SpectralLineFilter extends Filter
    implements PropertyChangeListener {

  // static fields
  private static Map<TrackerPanel, SpectralLineFilter> filters 
  		= new HashMap<TrackerPanel, SpectralLineFilter>();

  // instance fields
  private BufferedImage source, output;
  private int[] pixels;
  private int w, h;
  private Graphics2D g;
//  private TrackerPanel trackerPanel;
  protected TPoint end1, end2;
  protected Line2D line = new Line2D.Double();
  protected Color color = Color.white;
  protected BasicStroke stroke = new BasicStroke();
  protected Collection<Double> wavelengths = new ArrayList<Double>();
  private Inspector inspector;

  /**
   * Constructs a SpectralLineFilter object.
   */
  public SpectralLineFilter() {
    end1 = new TPoint();
    end2 = new TPoint();
    setWavelengths(1); // Hydrogen by default
  	hasInspector = true;
  }

  /**
   * Sets the tracker panel whose coords determine where the lines are drawn.
   *
   * @param panel a tracker panel
   */
  public void setTrackerPanel(TrackerPanel panel) {
    if (vidPanel != null)
    	vidPanel.removePropertyChangeListener("transform", this); //$NON-NLS-1$
    vidPanel = panel;
    vidPanel.addPropertyChangeListener("transform", this); //$NON-NLS-1$
    getInspector().setVisible(true);
  }

  /**
   * Applies the filter to a source image and returns the result.
   *
   * @param sourceImage the source image
   * @return the filtered image
   */
  public BufferedImage getFilteredImage(BufferedImage sourceImage) {
    if (!isEnabled()) return sourceImage;
    if (sourceImage != source) initialize(sourceImage);
    drawLines();
    return output;
  }

  /**
   * Implements abstract Filter method.
   *
   * @return the inspector
   */
  public JDialog getInspector() {
  	if (inspector == null) inspector = new Inspector();
  	if (inspector.isModal() && vidPanel != null) {
  		Frame f = JOptionPane.getFrameForComponent(vidPanel);
    	if (frame != f) {
    		frame = f;
      	inspector = new Inspector();
    	}
    }
    return inspector;
  }

  /**
   * Responds to property change events. Implements PropertyChangeListener.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    // fires "image" property change event whenever the coords change
    support.firePropertyChange("image", null, null); //$NON-NLS-1$
  }

  /**
   * Gets the spectral line filter for the specified tracker panel.
   *
   * @param panel a tracker panel
   * @return the filter
   */
  public static SpectralLineFilter getFilter(TrackerPanel panel) {
    SpectralLineFilter filter = filters.get(panel);
    if (filter == null) {
      filter = new SpectralLineFilter();
      filter.setTrackerPanel(panel);
      filters.put(panel, filter);
    }
    return filter;
  }

//_____________________________ private methods _______________________

  /**
   * Initializes the image.
   *
   * @param image a new source image
   */
  private void initialize(BufferedImage image) {
    source = image; // assumes image is TYPE_INT_RGB
    w = source.getWidth();
    h = source.getHeight();
    output = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    g = output.createGraphics();
    g.setPaint(color);
    pixels = new int[w * h];
  }

  /**
   * Draws the lines on the image.
   */
  private void drawLines() {
    if (vidPanel == null) return;
    source.getRaster().getDataElements(0, 0, w, h, pixels);
    output.getRaster().setDataElements(0, 0, w, h, pixels);
    int n = vidPanel.getFrameNumber();
    AffineTransform transform = vidPanel.getCoords().getToImageTransform(n);
    Iterator<Double> it = wavelengths.iterator();
    while (it.hasNext()) {
      double lambda = it.next().doubleValue();
      end1.setXY(lambda, -200);
      transform.transform(end1, end1);
      end2.setXY(lambda, 200);
      transform.transform(end2, end2);
      line.setLine(end1, end2);
      Shape shape = stroke.createStrokedShape(line);
      g.fill(shape);
    }
  }

  /**
   * Sets the spectral line wavelengths for a specified element.
   *
   * @param element the atomic number of the element
   */
  private void setWavelengths(int element) {
    wavelengths.clear();
    switch(element) {
      case 1: // Hydrogen
        wavelengths.add(new Double(410.2));
        wavelengths.add(new Double(434.1));
        wavelengths.add(new Double(486.1));
        wavelengths.add(new Double(656.3));
        break;
      case 2: // Helium
        wavelengths.add(new Double(447.1));
        wavelengths.add(new Double(471.3));
        wavelengths.add(new Double(492.2));
        wavelengths.add(new Double(501.6));
        wavelengths.add(new Double(587.6));
        wavelengths.add(new Double(667.8));
        wavelengths.add(new Double(706));
        break;
      case 10: // Neon
        wavelengths.add(new Double(540.1));
        wavelengths.add(new Double(585.2));
        wavelengths.add(new Double(588.2));
        wavelengths.add(new Double(603.0));
        wavelengths.add(new Double(607.4));
        wavelengths.add(new Double(616.4));
        wavelengths.add(new Double(621.7));
        wavelengths.add(new Double(626.6));
        wavelengths.add(new Double(633.4));
        wavelengths.add(new Double(638.3));
        wavelengths.add(new Double(640.2));
        wavelengths.add(new Double(650.6));
        wavelengths.add(new Double(659.9));
        wavelengths.add(new Double(692.9));
        wavelengths.add(new Double(703.2));
        break;
      case 80: // Mercury
        wavelengths.add(new Double(435.8));
        wavelengths.add(new Double(546.1));
        wavelengths.add(new Double(577.0));
        wavelengths.add(new Double(579.1));
        wavelengths.add(new Double(404.7));
        wavelengths.add(new Double(407.8));
        wavelengths.add(new Double(491.6));
        break;
   }
    support.firePropertyChange("image", null, null); //$NON-NLS-1$
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
      setTitle(TrackerRes.getString("SpectralLineFilter.Title")); //$NON-NLS-1$
      setResizable(false);
      setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      addComponentListener(new ComponentAdapter() {
        public void componentShown(ComponentEvent e) {
          SpectralLineFilter.this.setEnabled(true);
        }
        public void componentHidden(ComponentEvent e) {
          SpectralLineFilter.this.setEnabled(false);
        }
      });
      createGUI();
      pack();
    }

    /**
     * Creates the visible components.
     */
    void createGUI() {
      // create dropdown
      final JComboBox dropdown = new JComboBox();
      Object item = new ChemicalElement(TrackerRes.getString("SpectralLineFilter.H"), 1); //$NON-NLS-1$
      dropdown.addItem(item);
      item = new ChemicalElement(TrackerRes.getString("SpectralLineFilter.He"), 2); //$NON-NLS-1$
      dropdown.addItem(item);
      item = new ChemicalElement(TrackerRes.getString("SpectralLineFilter.Ne"), 10); //$NON-NLS-1$
      dropdown.addItem(item);
      item = new ChemicalElement(TrackerRes.getString("SpectralLineFilter.Hg"), 80); //$NON-NLS-1$
      dropdown.addItem(item);
      dropdown.setSelectedIndex(0);
      dropdown.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ChemicalElement element = (ChemicalElement)dropdown.getSelectedItem();
          setWavelengths(element.z);
        }
      });
      // add components to content pane
      JPanel buttonbar = new JPanel(new FlowLayout());
      setContentPane(buttonbar);
      buttonbar.add(dropdown);
    }

    class ChemicalElement {
      String name;
      int z;
      ChemicalElement(String element, int atomicNumber) {
        name = element;
        z = atomicNumber;
      }
      public String toString() {
        return name;
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
    public void saveObject(XMLControl control, Object obj) {/** not yet implemented */}

    /**
     * Creates a new filter.
     *
     * @param control the control
     * @return the new filter
     */
    public Object createObject(XMLControl control) {
      return new SpectralLineFilter();
    }

    /**
     * Loads a filter with data from an XMLControl.
     *
     * @param control the control
     * @param obj the filter
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      return obj;
    }
  }
}
