/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * A OSP layout lays out a container, arranging and resizing its components to fit
 * in the corners or in one of the border layout regions.
 *
 * @author W. Christian
 * @version 1.0
 */
public class OSPLayout extends BorderLayout {
  public static int macOffset;
  ArrayList<Component> list = new ArrayList<Component>();

  /**
   * The top left corner layout constraint.
   */
  public static final String TOP_LEFT_CORNER = "TopLeftCorner";         //$NON-NLS-1$

  /**
   * The top right corner layout constraint.
   */
  public static final String TOP_RIGHT_CORNER = "TopRightCorner";       //$NON-NLS-1$

  /**
   * The bottom left corner layout constraint.
   */
  public static final String BOTTOM_LEFT_CORNER = "BottomLeftCorner";   //$NON-NLS-1$

  /**
   * The bottom right layout constraint.
   */
  public static final String BOTTOM_RIGHT_CORNER = "BottomRightCorner"; //$NON-NLS-1$

  /**
   * The bottom right layout constraint.
   */
  public static final String CENTERED = "Centered";                     //$NON-NLS-1$

  /**
   * Constant to specify components location to be the top left corner portion of the layout.
   */
  Component topLeftCorner;

  /**
   * Constant to specify components location to be the top right corner portion of the layout.
   */
  Component topRightCorner;

  /**
   * Constant to specify components location to be the bottom left corner portion of the layout.
   */
  Component bottomLeftCorner;

  /**
   * Constant to specify components location to be the bottom right corner portion of the layout.
   */
  Component bottomRightCorner;

  /**
   * Constant to specify components location to be centered in the layout.
   */
  Component centeredComp;
  Rectangle layoutRect = new Rectangle(0, 0, 0, 0);
  Component[] components = new Component[0];

  static {
    try {                                                                      // system properties may not be readable in some environments
      macOffset = ("Mac OS X".equals(System.getProperty("os.name"))) ? 16 : 0; //$NON-NLS-1$ //$NON-NLS-2$
    } catch(SecurityException ex) {}
  }

  /**
   * Constructs a new OSP layout with no gaps between components.
   */
  public OSPLayout() {
    this(0, 0);
  }

  /**
   * Constructs a new OSP layout with the specified gaps between components.
   * The horizontal gap is specified by <code>hgap</code>
   * and the vertical gap is specified by <code>vgap</code>.
   * @param   hgap   the horizontal gap.
   * @param   vgap   the vertical gap.
   */
  public OSPLayout(int hgap, int vgap) {
    super(hgap, vgap);
  }

  public void addLayoutComponent(Component comp, Object constraints) {
    if(!list.contains(comp)) {
      list.add(comp);
      components = list.toArray(new Component[0]);
    }
    synchronized(comp.getTreeLock()) {
      if((constraints instanceof String)&&"TopLeftCorner".equals(constraints)) {            //$NON-NLS-1$
        list.remove(topLeftCorner);
        topLeftCorner = comp;
      } else if((constraints instanceof String)&&"TopRightCorner".equals(constraints)) {    //$NON-NLS-1$
        list.remove(topRightCorner);
        topRightCorner = comp;
      } else if((constraints instanceof String)&&"BottomLeftCorner".equals(constraints)) {  //$NON-NLS-1$
        list.remove(bottomLeftCorner);
        bottomLeftCorner = comp;
      } else if((constraints instanceof String)&&"BottomRightCorner".equals(constraints)) { //$NON-NLS-1$
        list.remove(bottomRightCorner);
        bottomRightCorner = comp;
      } else if((constraints instanceof String)&&"Centered".equals(constraints)) {          //$NON-NLS-1$
        list.remove(centeredComp);
        centeredComp = comp;
      } else {
        super.addLayoutComponent(comp, constraints);
      }
    }
  }

  /**
   * Removes the specified component from this border layout. This
   * method is called when a container calls its <code>remove</code> or
   * <code>removeAll</code> methods. Most applications do not call this
   * method directly.
   * @param   comp   the component to be removed.
   * @see     java.awt.Container#remove(java.awt.Component)
   * @see     java.awt.Container#removeAll()
   */
  public void removeLayoutComponent(Component comp) {
    if(list.contains(comp)) {
      list.remove(comp);
      components = list.toArray(new Component[0]);
    }
    synchronized(comp.getTreeLock()) {
      if(comp==topLeftCorner) {
        topLeftCorner = null;
      } else if(comp==topRightCorner) {
        topRightCorner = null;
      } else if(comp==bottomLeftCorner) {
        bottomLeftCorner = null;
      } else if(comp==bottomRightCorner) {
        bottomRightCorner = null;
      } else if(comp==centeredComp) {
        centeredComp = null;
      } else {
        super.removeLayoutComponent(comp);
      }
    }
  }

  /**
   * Lays out the container argument using this layout.
   * @param target Container
   */
  public void layoutContainer(Container target) {
    super.layoutContainer(target);
    doMyLayout(target);
  }

  /**
   * Lays out a single component by setting the component's bounds.
   * @param target Container
   */
  public boolean quickLayout(Container target, Component c) {
    if((target==null)||(c==null)) {
      return false;
    }
    Insets insets = target.getInsets();
    int top = insets.top;
    int bottom = target.getHeight()-insets.bottom;
    int left = insets.left;
    int right = target.getWidth()-insets.right;
    if(topLeftCorner==c) {
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds(left, top, d.width, d.height);
    } else if(topRightCorner==c) {
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds(right-d.width, top, d.width, d.height);
    } else if(bottomLeftCorner==c) {
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds(left, bottom-d.height, d.width, d.height);
    } else if(bottomRightCorner==c) {
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds(right-d.width-macOffset, bottom-d.height, d.width, d.height);
    } else if(centeredComp==c) {
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds((right-left-d.width)/2, (bottom-top-d.height)/2, d.width, d.height);
    } else {
      return false;
    }
    return true;
  }

  public void checkLayoutRect(Container c, Rectangle viewRect) {
    if(layoutRect.equals(viewRect)) {
      return;
    }
    // doMyLayout(c);
    layoutContainer(c);
  }

  public Component[] getComponents() {
    return components;
  }

  void doMyLayout(Container target) {
    Insets insets = target.getInsets();
    int top = insets.top;
    int bottom = target.getHeight()-insets.bottom;
    int left = insets.left;
    int right = target.getWidth()-insets.right;
    Component c = null;
    if(topLeftCorner!=null) {
      c = topLeftCorner;
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds(left, top, d.width, d.height);
    }
    if(topRightCorner!=null) {
      c = topRightCorner;
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds(right-d.width, top, d.width, d.height);
    }
    if(bottomLeftCorner!=null) {
      c = bottomLeftCorner;
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds(left, bottom-d.height, d.width, d.height);
    }
    if(bottomRightCorner!=null) {
      c = bottomRightCorner;
      Dimension d = c.getPreferredSize();
      c.setSize(d.width+macOffset, d.height);
      c.setBounds(right-d.width-macOffset, bottom-d.height, d.width, d.height);
    }
    if(centeredComp!=null) {
      c = centeredComp;
      Dimension d = c.getPreferredSize();
      c.setSize(d.width, d.height);
      c.setBounds((right-left-d.width)/2, (bottom-top-d.height)/2, d.width, d.height);
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
