/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A dialog that displays an editable table of XMLControl properties.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class XMLTableInspector extends JDialog implements PropertyChangeListener {
  // static fields
  static String frameTitle = ControlsRes.getString("XMLTableInspector.Title"); //$NON-NLS-1$
  // instance fields
  private XMLTable table;
  private boolean changed;

  /**
   * Constructs editable modal inspector for specified XMLControl.
   *
   * @param control the xml control
   */
  public XMLTableInspector(XMLControl control) {
    this(control, true, true);
  }

  /**
   * Constructs modal inspector for specified XMLControl and sets editable flag.
   *
   * @param control the xml control
   * @param editable true to enable editing
   */
  public XMLTableInspector(XMLControl control, boolean editable) {
    this(control, editable, true);
  }

  /**
   * Constructs inspector for specified XMLControl and sets editable and modal flags.
   *
   * @param control the xml control
   * @param editable true to enable editing
   * @param modal true if modal
   */
  public XMLTableInspector(XMLControl control, boolean editable, boolean modal) {
    super((Frame) null, modal);
    createGUI();
    XMLTable table = new XMLTable(control);
    table.setEditable(editable);
    setTable(table);
    String s = XML.getExtension(control.getObjectClassName());
    setTitle(frameTitle+" "+s+" \""+control.getPropertyName()+"\" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        if(changed) {
          firePropertyChange("xmlData", null, null);                  //$NON-NLS-1$
          changed = false;
        }
      }

    });
  }

  /**
   * Constructs inspector using an OSPControlTable and sets the editable and modal flags.
   *
   * @param editable true to enable editing
   * @param modal true if modal
   */
  public XMLTableInspector(boolean editable, boolean modal) {
    super((Frame) null, modal);
    table = new OSPControlTable(new XMLControlElement());
    table.setEditable(editable);
    // listen for "cell" changes in arrays
    table.addPropertyChangeListener("cell", this);      //$NON-NLS-1$
    // listen for "tableData" changes in the table
    table.addPropertyChangeListener("tableData", this); //$NON-NLS-1$
    createGUI();
    String s = XML.getExtension(getXMLControl().getObjectClassName());
    setTitle(frameTitle+" "+s+" \""+getXMLControl().getPropertyName()+"\" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  /**
   * Gets the Control associated with this table.
   *
   * @return Control
   */
  public Control getControl() {
    return(table instanceof Control) ? (Control) table : table.tableModel.control;
  }

  /**
   * Listens for property change events from XMLTable.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    changed = true;
    // forward event to listeners
    firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
  }

  /**
   * Gets the XMLTable.
   *
   * @return the table
   */
  public XMLTable getTable() {
    return table;
  }

  /**
   * Sets the XMLTable.
   *
   * @return the table
   */
  public void setTable(XMLTable xmlTable) {
    if(table!=null) {
      table.removePropertyChangeListener("cell", this);      //$NON-NLS-1$
      table.removePropertyChangeListener("tableData", this); //$NON-NLS-1$
      xmlTable.setEditable(table.isEditable());
    }
    table = xmlTable;
    // listen for "cell" changes in arrays
    table.addPropertyChangeListener("cell", this);      //$NON-NLS-1$
    // listen for "tableData" changes in the table
    table.addPropertyChangeListener("tableData", this); //$NON-NLS-1$
    JScrollPane scrollpane = new JScrollPane(table);
    scrollpane.createHorizontalScrollBar();
    getContentPane().add(scrollpane, BorderLayout.CENTER);
  }

  /**
   * Gets the XMLControl associated with this table.
   *
   * @return XMLControl
   */
  public XMLControl getXMLControl() {
    return table.tableModel.control;
  }

  // creates the GUI
  private void createGUI() {
    setSize(400, 300);
    setContentPane(new JPanel(new BorderLayout()));
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
