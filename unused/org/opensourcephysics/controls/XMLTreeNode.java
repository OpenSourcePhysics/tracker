/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This is a DefaultMutableTreeNode for an XML JTree.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class XMLTreeNode extends DefaultMutableTreeNode {
  // instance fields
  protected XMLProperty prop;
  private boolean inspectable = false;

  /**
   * Constructs a node with an XMLProperty
   *
   * @param property the XMLProperty
   */
  public XMLTreeNode(XMLProperty property) {
    prop = property;
    setUserObject(this);
    Iterator<?> it = property.getPropertyContent().iterator();
    while(it.hasNext()) {
      Object next = it.next();
      if(next instanceof XMLProperty) {
        XMLProperty prop = (XMLProperty) next;
        // go down one level if prop's only content is an XMLControl
        List<?> content = prop.getPropertyContent();
        if(content.size()==1) {
          next = content.get(0);
          if(next instanceof XMLControl) {
            prop = (XMLProperty) next;
          }
        }
        add(new XMLTreeNode(prop));
      }
    }
    // determine if this node is inspectable
    if(prop.getPropertyType().equals("array")) {                          //$NON-NLS-1$
      // get base component type and depth
      Class<?> type = prop.getPropertyClass();
      if(type==null) {
        return;
      }
      while(type.getComponentType()!=null) {
        type = type.getComponentType();
      }
      if(type.getName().equals("double")||type.getName().equals("int")) { // node is double or int array //$NON-NLS-1$ //$NON-NLS-2$
        XMLProperty proper = prop;
        XMLProperty parent = proper.getParentProperty();
        while(!(parent instanceof XMLControl)) {
          proper = parent;
          parent = parent.getParentProperty();
        }
        // get array depth
        type = proper.getPropertyClass();
        int i = 0;
        while(type.getComponentType()!=null) {
          type = type.getComponentType();
          i++;
        }
        if(i<=3) {
          inspectable = true;
        }
      }
    }
  }

  /**
   * Gets the XMLProperty.
   *
   * @return the XMLProperty
   */
  public XMLProperty getProperty() {
    return prop;
  }

  /**
   * Gets the XMLProperty.
   *
   * @return the XMLProperty
   */
  public boolean isInspectable() {
    return inspectable;
  }

  /**
   * This is used by the tree node to get a node label.
   *
   * @return the display name of the node
   */
  public String toString() {
    // return the child "name" property of a control element, if any
    if(prop instanceof XMLControl) {
      XMLControl control = (XMLControl) prop;
      String name = control.getString("name"); //$NON-NLS-1$
      if((name!=null)&&!name.equals("")) {     //$NON-NLS-1$
        return name;
      }
    }
    return prop.getPropertyName();
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
