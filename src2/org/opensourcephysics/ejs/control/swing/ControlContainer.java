/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import org.opensourcephysics.ejs.control.ControlElement;
import org.opensourcephysics.ejs.control.value.BooleanValue;

/**
 * A configurable Container
 */
public abstract class ControlContainer extends ControlSwingElement {
  static private final BooleanValue falseValue = new BooleanValue(false);
  protected java.util.Vector<ControlElement> radioButtons = new java.util.Vector<ControlElement>();
  protected java.util.Vector<ControlElement> children = new java.util.Vector<ControlElement>();

  /**
   * Constructor ControlContainer
   * @param _visual
   */
  public ControlContainer(Object _visual) {
    super(_visual);
  }

  // This is not final since windows may change the default case (this one)
  public java.awt.Container getContainer() {
    return(java.awt.Container) getVisual();
  }

  // ------------------------------------------------
  // Own methods
  // ------------------------------------------------

  /**
   * adds a child control
   * @param _child the child control
   */
  public void add(ControlElement _child) {
    children.add(_child);
    java.awt.Container container = getContainer();
    java.awt.LayoutManager layout = container.getLayout();
    // This is set by Ejs to allow changing the natural order of childhood
    String indexInParent = _child.getProperty("_ejs_indexInParent_"); //$NON-NLS-1$
    int index = -1;
    if(indexInParent!=null) {
      index = Integer.parseInt(indexInParent);
    }
    _child.setProperty("_ejs_indexInParent_", null); //$NON-NLS-1$
    if(layout instanceof java.awt.BorderLayout) {
      String pos = _child.getProperty("position"); //$NON-NLS-1$
      if(pos!=null) {
        container.add(_child.getComponent(), ConstantParser.constraintsConstant(pos).getString(), index);
      } else {
        container.add(_child.getComponent(), java.awt.BorderLayout.CENTER, index);
      }
    } else {
      container.add(_child.getComponent(), index);
    }
    adjustSize();
    if(_child instanceof ControlRadioButton) {
      radioButtons.add(_child);
      ((ControlRadioButton) _child).setParent(this);
    }
    // Now propagate my own font, foreground and background;
    propagateProperty(_child, "font", getProperty("font"));             //$NON-NLS-1$ //$NON-NLS-2$
    propagateProperty(_child, "foreground", getProperty("foreground")); //$NON-NLS-1$ //$NON-NLS-2$
    propagateProperty(_child, "background", getProperty("background")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void adjustSize() {
    getContainer().validate();
    getContainer().repaint();
    resizeContainer(getContainer());
    resizeContainer(getComponent().getParent());
  }

  private static void resizeContainer(java.awt.Container _container) {
    if(_container==null) {
      return;
    }
    java.awt.Rectangle b = _container.getBounds();
    _container.setBounds(b.x, b.y, b.width+1, b.height+1);
    _container.setBounds(b.x, b.y, b.width, b.height);
    _container.validate();
    _container.repaint();
  }

  /**
   * Returns the vector of children
   */
  public java.util.Vector<ControlElement> getChildren() {
    return children;
  }

  /**
   * removes a child control
   * @param _child the child control
   */
  public void remove(ControlElement _child) {
    children.remove(_child);
    java.awt.Container container = getContainer();
    container.remove(_child.getComponent());
    container.validate();
    container.repaint();
    if(_child instanceof ControlRadioButton) {
      radioButtons.remove(_child);
      ((ControlRadioButton) _child).setParent(null);
    }
  }

  public void informRadioGroup(ControlRadioButton _source, boolean _state) {
    if(_state==false) {
      return;
    }
    for(java.util.Enumeration<ControlElement> e = radioButtons.elements(); e.hasMoreElements(); ) {
      ControlRadioButton rb = (ControlRadioButton) e.nextElement();
      if(rb!=_source) {
        boolean wasActive = rb.isActive();
        rb.setActive(false);
        rb.setValue(ControlRadioButton.VARIABLE, falseValue);
        rb.reportChanges();
        rb.setActive(wasActive);
      }
    }
  }

  // ------------------------------------------------
  // Private methods
  // ------------------------------------------------
  private void propagateProperty(ControlElement _child, String _property, String _value) {
    if(_child.getProperty(_property)==null) {
      _child.setProperty(_property, _value);
    }
  }

  private void propagateProperty(String _property, String _value) {
    for(int i = 0; i<children.size(); i++) {
      propagateProperty(children.elementAt(i), _property, _value);
    }
  }

  // ------------------------------------------------
  // Properties
  // ------------------------------------------------
  public String getPropertyInfo(String _property) {
    if(_property.equals("visible")) { //$NON-NLS-1$
      return "boolean";               // not HIDDEN //$NON-NLS-1$
    }
    return super.getPropertyInfo(_property);
  }

  public ControlElement setProperty(String _property, String _value) {
    ControlElement returnValue = super.setProperty(_property, _value);
    if(_property.equals("font")||_property.equals("foreground")||_property.equals("background")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      propagateProperty(_property, _value);
    }
    return returnValue;
  }

} // End of class

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
