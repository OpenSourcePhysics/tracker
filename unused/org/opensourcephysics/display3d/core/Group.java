/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.Data;

/**
 * <p>Title: Group</p>
 * <p>Description: A Group is an element that is made of other elements</p>
 * @author Francisco Esquembre
 * @version March 2005
 * @see Element
 */
public interface Group extends Element, Data {
  /**
   * Adds an Element to this Group.
   * @param element Element
   * @see Element
   */
  public void addElement(Element element);

  /**
   * Removes an Element from this Group
   * @param element Element
   * @see Element
   */
  public void removeElement(Element element);

  /**
   * Removes all Elements from this Group
   * @see Element
   */
  public void removeAllElements();

  /**
   * Gets the cloned list of Elements in the group.
   * (Should be synchronized.)
   * @return cloned list
   */
  public java.util.List<Element> getElements();

  /**
   * Gets the elements of the group at a given index.
   * @return the given element (null if the index is not within allowed bounds)
   */
  public Element getElement(int index);

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  static abstract class Loader extends Element.Loader {
    public void saveObject(XMLControl control, Object obj) {
      super.saveObject(control, obj);
      Group group = (Group) obj;
      control.setValue("elements", group.getElements()); //$NON-NLS-1$
    }

    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      Group group = (Group) obj;
      java.util.Collection<?> elements = (java.util.Collection<?>) control.getObject("elements"); //$NON-NLS-1$
      if(elements!=null) {
        group.removeAllElements();
        java.util.Iterator<?> it = elements.iterator();
        while(it.hasNext()) {
          group.addElement((Element) it.next());
        }
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
