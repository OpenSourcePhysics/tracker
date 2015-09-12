/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A CircleFootprint returns a circle for a Point[] of length 1.
 */
public class CircleFootprint implements Footprint, Cloneable {
	
	// static fields
	protected static float plainStrokeSize = 1.0f;
	protected static float boldStrokeSize = 2.0f;

  // instance fields
  protected String name;
  protected Ellipse2D circle = new Ellipse2D.Double();
  protected Ellipse2D center = new Ellipse2D.Double();
  protected Shape highlight;
  protected Shape outline;
  protected Shape spot;
  protected AffineTransform transform = new AffineTransform();
  protected Color color = new Color(255, 0, 0, 127);
  protected Color highlightColor = Color.red;
  protected Shape[] hitShapes = new Shape[1];
  protected BasicStroke highlightStroke, outlineStroke;
  protected boolean outlined=true, spotted;
  protected int alpha = 0;
  protected CircleDialog dialog;
  protected int r;
  protected int prevRadius;
  protected float prevStrokeSize;
  protected boolean prevSpot;

  /**
   * Constructs a CircleFootprint.
   *
   * @param name the name
   * @param radius radius of the footprint
   */
  public CircleFootprint(String name, int radius) {
    this.name = name;
    setRadius(radius);
    setStroke(new BasicStroke(1.0f));
    center.setFrame(-1, -1, 2, 2);
  }
  
  /**
   * Clones a CircleFootprint.
   *
   * @return the clone
   */
  protected Object clone() throws CloneNotSupportedException {
  	CircleFootprint clone = (CircleFootprint)super.clone();
  	clone.circle = new Ellipse2D.Double();
  	return clone;
  }

  /**
   * Gets a named footprint.
   *
   * @param name the name of the footprint
   * @return the footprint
   */
  public static Footprint getFootprint(String name) {
    Iterator<Footprint> it = footprints.iterator();
    while(it.hasNext()) {
    	CircleFootprint footprint = (CircleFootprint)it.next();
      if (name == footprint.getName()) try {
      	Footprint fp = (CircleFootprint)footprint.clone();
        return fp;
      } catch(CloneNotSupportedException ex) {ex.printStackTrace();}
    }
    return null;
  }

  /**
   * Gets the name of this footprint.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the display name of the footprint.
   *
   * @return the localized display name
   */
  public String getDisplayName() {
  	return TrackerRes.getString(name);
  }

  /**
   * Gets the minimum point array length required by this footprint.
   *
   * @return the length
   */
  public int getLength() {
    return 1;
  }

  /**
   * Gets the icon.
   *
   * @param w width of the icon
   * @param h height of the icon
   * @return the icon
   */
  public Icon getIcon(int w, int h) {
  	int realRadius = r;
  	setRadius(outlined? 5: 6);
    Shape shape = getShape(new Point[] {new Point()});
    Area area = null;
    if (spotted) {
      area = new Area(spot);    	
    }
    if (outlined) {
    	if (area==null)
    		area = new Area(outline);
    	else
    		area.add(new Area(outline));
    }
    ShapeIcon icon = new ShapeIcon(shape, area, w, h);
    icon.setColor(color, highlightColor);
  	setRadius(realRadius);
    return icon;
  }

  /**
   * Gets the footprint mark.
   *
   * @param points a Point array
   * @return the mark
   */
  public Mark getMark(Point[] points) {
    final Shape shape = getShape(points);
    final Shape outline = this.outline;
    final Shape highlight = this.highlight;
    final Shape spot = this.spot;
    return new Mark() {
      public void draw(Graphics2D g, boolean highlighted) {
        Paint gpaint = g.getPaint();
        g.setPaint(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.fill(shape);
        g.setPaint(highlightColor);
        if (spotted) {
        	g.fill(spot);
        }
        if (outlined) {
        	g.fill(outline);
        }
        if (highlighted) {
        	g.fill(highlight);
        }
        g.setPaint(gpaint);
      }

      public Rectangle getBounds(boolean highlighted) {
        Rectangle bounds = shape.getBounds();
        if (highlighted) bounds.add(highlight.getBounds());
        return bounds;
      }
    };
  }

  /**
   * Gets the hit shapes.
   *
   * @return the hit shapes
   */
  public Shape[] getHitShapes() {
    return hitShapes;
  }

  /**
   * Sets the stroke.
   *
   * @param stroke the stroke
   */
  public void setStroke(BasicStroke stroke) {
    outlineStroke = stroke;
  	highlightStroke = new BasicStroke(stroke.getLineWidth()+1.0f);
  }

  /**
   * Gets the stroke. May return null;
   *
   * @return the stroke
   */
  public BasicStroke getStroke() {
    return null;
  }

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
    this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    highlightColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Gets the color.
   *
   * @return the color
   */
  public Color getColor() {
    return highlightColor;
  }

  /**
   * Sets the radius.
   *
   * @param radius the radius
   */
  public void setRadius(int radius) {
  	r = radius;
    circle.setFrame(-r, -r, 2*r, 2*r);
  }

  /**
   * Sets the outlined flag.
   *
   * @param outline true to draw an outline around the circle
   */
  public void setOutlined(boolean outline) {
    outlined = outline;
  }

  /**
   * Sets the spotted flag.
   *
   * @param drawSpot true to draw a spot at the center of the circle
   */
  public void setSpotShown(boolean drawSpot) {
    spotted = drawSpot;
  }

  /**
   * Sets the alpha of the fill.
   *
   * @param alpha 0 for transparent, 255 for solid
   */
  public void setAlpha(int alpha) {
    this.alpha = alpha;
    setColor(color);
  }

  /**
   * Gets the properties for saving.
   *
   * @return the properties "r outline spot bold"
   */
  public String getProperties() {
  	String s = r+" "; //$NON-NLS-1$
  	if (outlined)
  		s+="outline "; //$NON-NLS-1$
  	if (spotted)
  		s+="spot "; //$NON-NLS-1$
  	if (outlineStroke.getLineWidth()>plainStrokeSize)
  		s+="bold "; //$NON-NLS-1$
    return s;
  }

  /**
   * Sets the properties when loading.
   *
   * @param props the properties "r outline spot bold"
   */
  public void setProperties(String props) {
  	if (props==null) return;
    int n = props.indexOf(" "); //$NON-NLS-1$
    String radius = props.substring(0, n);
    try {
			setRadius(Integer.parseInt(radius));
		} catch (NumberFormatException e) {
		}
    setOutlined(props.indexOf("outline")>-1); //$NON-NLS-1$
    setSpotShown(props.indexOf("spot")>-1); //$NON-NLS-1$
    float f = props.indexOf("bold")>-1? boldStrokeSize: plainStrokeSize; //$NON-NLS-1$
    setStroke(new BasicStroke(f));
  }

  /**
   * Shows the properties dialog.
   *
   * @param track the track using this footprint
   */
  public void showProperties(TTrack track) {
    if (dialog==null) {
    	dialog = new CircleDialog(track);
      // center on screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width - dialog.getBounds().width) / 2;
      int y = (dim.height - dialog.getBounds().height) / 2;
      dialog.setLocation(x, y);
    }
    dialog.boldCheckbox.setSelected(outlineStroke.getLineWidth()>plainStrokeSize);
    dialog.spotCheckbox.setSelected(spotted);
    dialog.spinner.setValue(r);
    prevSpot = spotted;
    prevStrokeSize = outlineStroke.getLineWidth();
    prevRadius = r;
    dialog.setVisible(true);
  }

  /**
   * Gets the fill shape for a specified point.
   *
   * @param points an array of points
   * @return the fill shape
   */
  public Shape getShape(Point[] points) {
    Point p = points[0];
    transform.setToTranslation(p.x, p.y);
    Shape c = transform.createTransformedShape(circle);
    highlight = highlightStroke.createStrokedShape(c);    
    outline = outlineStroke.createStrokedShape(c);
    spot = transform.createTransformedShape(center);
    hitShapes[0] = spot; // ignored by PointMass!
    return c;
  }
  
  private class CircleDialog extends JDialog {
  	
  	TTrack track;
  	JSpinner spinner;
  	JLabel spinnerLabel;
  	JButton okButton, cancelButton;
  	JCheckBox boldCheckbox, spotCheckbox;
  	
    /**
     * Constructs a CircleDialog for a specified track.
     * @param track the track
     */
    public CircleDialog(TTrack track) {
      super(track.trackerPanel.getTFrame(), true);
      this.track = track;
      setTitle(TrackerRes.getString("CircleFootprint.Dialog.Title")); //$NON-NLS-1$
      setResizable(false);
      setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
      createGUI();
      pack();
      okButton.requestFocusInWindow();
    }

    /**
     * Creates the visible components.
     */
    void createGUI() {
      JPanel contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);
      JPanel upper = new JPanel();
      upper.setBorder(BorderFactory.createEtchedBorder());
      contentPane.add(upper, BorderLayout.NORTH);
      // add spinner label and spinner
      spinnerLabel = new JLabel(TrackerRes.getString("CircleFootprint.Dialog.Label.Radius")); //$NON-NLS-1$
      spinnerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
      upper.add(spinnerLabel);
      SpinnerModel model = new SpinnerNumberModel(3, 3, 100, 1);
      spinner = new JSpinner(model);
      JFormattedTextField tf = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
    	tf.setEnabled(false);
    	tf.setDisabledTextColor(Color.BLACK);
      ChangeListener listener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int radius = (Integer)spinner.getValue();
          if (radius==r) return;
          setRadius(radius);
          track.trackerPanel.changed = true;
          track.repaint();
        }
    	};
      spinner.addChangeListener(listener);
      upper.add(spinner);
      // add bold label and checkbox
      boldCheckbox = new JCheckBox(TrackerRes.getString("CircleFootprint.Dialog.Checkbox.Bold")); //$NON-NLS-1$
      boldCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
      boldCheckbox.setOpaque(false);
      boldCheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
  		  	float f = boldCheckbox.isSelected()? boldStrokeSize: plainStrokeSize;
  		  	setStroke(new BasicStroke(f));
          track.trackerPanel.changed = true;
          track.repaint();
        }
      });
      upper.add(boldCheckbox);
      spotCheckbox = new JCheckBox(TrackerRes.getString("CircleFootprint.Dialog.Checkbox.CenterSpot")); //$NON-NLS-1$
      spotCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
      spotCheckbox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
  		  	setSpotShown(spotCheckbox.isSelected());
          track.trackerPanel.changed = true;
          track.repaint();
        }
      });
      upper.add(spotCheckbox);
      // add close button
      JPanel lower = new JPanel();
      contentPane.add(lower, BorderLayout.SOUTH);
      okButton = new JButton(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
  		  	setVisible(false);
  		  	track.setFootprint(CircleFootprint.this.getName());
        }
      });
      lower.add(okButton);
      cancelButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
	        setSpotShown(prevSpot);
	        setStroke(new BasicStroke(prevStrokeSize));
	        setRadius(prevRadius);
          track.repaint();
  		  	setVisible(false);
        }
      });
      lower.add(cancelButton);
    }
  }

  // static fields
  private static Collection<Footprint> footprints = new HashSet<Footprint>();

  // static constants
  private static final CircleFootprint CIRCLE;
  private static final CircleFootprint FILLED_CIRCLE;

  // static initializers
  static {
    CIRCLE = new CircleFootprint("CircleFootprint.Circle", 4); //$NON-NLS-1$
    footprints.add(CIRCLE);
    FILLED_CIRCLE = new CircleFootprint("CircleFootprint.FilledCircle", 8); //$NON-NLS-1$
    FILLED_CIRCLE.setSpotShown(true);
    FILLED_CIRCLE.setAlpha(102);
    footprints.add(FILLED_CIRCLE);
  }
}

