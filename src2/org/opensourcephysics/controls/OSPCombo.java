/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

/**
 * A JPopupMenu with String items from which to choose.
 *
 * @author Doug Brown
 */
public class OSPCombo extends JPopupMenu {
  // instance fields
  protected String[] items;
  protected int selected;
  //protected JTextField display;
  int row, column;

  /**
   * Constructor that specifies initial selected index.
   *
   * @param  choices an array of string items
   * @param  initial the initial selected index
   */
  public OSPCombo(String[] choices, int initial) {
    items = choices;
    selected = initial;
  }

  /**
   * Constructor that selects index 0.
   *
   * @param  choices an array of string items
   */
  public OSPCombo(String[] choices) {
    this(choices, 0);
  }

  /**
   * Returns the selected index.
   *
   * @return the selected index
   */
  public int getSelectedIndex() {
    return selected;
  }

  /**
   * Returns the items String[].
   *
   * @return the items
   */
  public String[] getItems() {
    return items;
  }

  /**
   * Shows the popup immediately below the specified field. If item is selected,
   * sets the field text and fires property change.
   *
   * @param field the field that displays the selected string
   */
  public void showPopup(final JTextField display) {
    //display = field;
    Action selectAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        int prev = selected;
        selected = Integer.parseInt(e.getActionCommand());
        display.setText(items[selected]);
        OSPCombo.this.firePropertyChange("index", prev, -1); //$NON-NLS-1$        
      }

    };
    removeAll();
    for(int i = 0; i<items.length; i++) {
      String next = items[i].toString();
      JMenuItem item = new JMenuItem(next);
      item.setFont(display.getFont());
      item.addActionListener(selectAction);
      item.setActionCommand(String.valueOf(i));
      add(item);
    }
    int popupHeight = 8+getComponentCount()*display.getHeight();
    setPopupSize(display.getWidth(), popupHeight);
    show(display, 0, display.getHeight());
  }

  /**
   * Returns the selected String.
   *
   * @return the currently selected String
   */
  public String toString() {
    return items[selected];
  }

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load XML data for this class.
   */
  static class Loader implements XML.ObjectLoader {
    public void saveObject(XMLControl control, Object obj) {
      OSPCombo combo = (OSPCombo) obj;
      control.setValue("items", combo.items);    //$NON-NLS-1$
      control.setValue("index", combo.selected); //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      String[] items = (String[]) control.getObject("items"); //$NON-NLS-1$
      int index = control.getInt("index");                    //$NON-NLS-1$
      return new OSPCombo(items, index);
    }

    public Object loadObject(XMLControl control, Object obj) {
      //OSPCombo combo =(OSPCombo)obj;
      //combo.selected=control.getInt("index");//$NON-NLS-1$
      //combo.items=(String[])control.getObject("items"); //$NON-NLS-1$
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
