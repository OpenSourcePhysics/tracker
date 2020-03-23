/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.ArrayTable;

/**
 * A dialog that displays an ArrayTable.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ArrayInspector extends JDialog implements PropertyChangeListener {
  // instance fields
  JTabbedPane tabbedPane = new JTabbedPane();
  ArrayTable[] tables;
  JSpinner spinner;
  JScrollPane scrollpane;
  Object array;
  boolean changed;

  /**
   * Gets an array inspector for the specified array XMLProperty.
   *
   * @param arrayProp the array XMLProperty
   * @return the array inspector
   */
  public static ArrayInspector getInspector(XMLProperty arrayProp) {
    if(!arrayProp.getPropertyType().equals("array")) { //$NON-NLS-1$
      return null;
    }
    // get base component type and depth
    Class<?> type = arrayProp.getPropertyClass();
    while(type.getComponentType()!=null) {
      type = type.getComponentType();
    }
    if(type.getName().equals("double")||   //$NON-NLS-1$
      type.getName().equals("int")||       //$NON-NLS-1$
        type.getName().equals("boolean")|| //$NON-NLS-1$
          type.equals(String.class)) {     // node is double, int or string array
      String name = arrayProp.getPropertyName();
      XMLProperty parent = arrayProp.getParentProperty();
      while(!(parent instanceof XMLControl)) {
        name = parent.getPropertyName();
        arrayProp = parent;
        parent = parent.getParentProperty();
      }
      XMLControl arrayControl = (XMLControl) parent;
      Object arrayObj = arrayControl.getObject(name);
      if(arrayObj==null) {
        return null;
      }
      return getInspector(arrayObj, name);
    }
    return null;
  }

  /**
   * Gets an array inspector for the specified array.
   *
   * @param arrayObj the array
   * @param name the display name for the array
   * @return the array inspector
   */
  public static ArrayInspector getInspector(Object arrayObj, String name) {
    ArrayInspector inspector = null;
    if(arrayObj instanceof double[]) {
      double[] array = (double[]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof double[][]) {
      double[][] array = (double[][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof double[][][]) {
      double[][][] array = (double[][][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof int[]) {
      int[] array = (int[]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof int[][]) {
      int[][] array = (int[][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof int[][][]) {
      int[][][] array = (int[][][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof String[]) {
      String[] array = (String[]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof String[][]) {
      String[][] array = (String[][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof String[][][]) {
      String[][][] array = (String[][][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof boolean[]) {
      boolean[] array = (boolean[]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof boolean[][]) {
      boolean[][] array = (boolean[][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    } else if(arrayObj instanceof boolean[][][]) {
      boolean[][][] array = (boolean[][][]) arrayObj;
      inspector = new org.opensourcephysics.tools.ArrayInspector(array, name);
    }
    if(inspector!=null) {
      inspector.array = arrayObj;
    }
    return inspector;
  }

  /**
   * Determines if an XMLProperty can be inspected with an array inspector.
   *
   * @param arrayProp the XMLProperty
   * @return true if it can be inspected
   */
  public static boolean canInspect(XMLProperty arrayProp) {
    if(!arrayProp.getPropertyType().equals("array")) { //$NON-NLS-1$
      return false;
    }
    String name = arrayProp.getPropertyName();
    XMLProperty parent = arrayProp.getParentProperty();
    while(!(parent instanceof XMLControl)) {
      name = parent.getPropertyName();
      arrayProp = parent;
      parent = parent.getParentProperty();
    }
    XMLControl arrayControl = (XMLControl) parent;
    Object arrayObj = arrayControl.getObject(name);
    return canInspect(arrayObj);
  }

  /**
   * Determines if an object is an array that can be inspected.
   *
   * @param obj the object
   * @return true if it can be inspected
   */
  public static boolean canInspect(Object obj) {
    if(obj==null) {
      return false;
    }
    if((obj instanceof double[])||(obj instanceof double[][])||(obj instanceof double[][][])||(obj instanceof int[])||(obj instanceof int[][])||(obj instanceof int[][][])||(obj instanceof boolean[])||(obj instanceof boolean[][])||(obj instanceof boolean[][][])||(obj instanceof String[])||(obj instanceof String[][])||(obj instanceof String[][][])) {
      return true;
    }
    return false;
  }

  /**
   * Gets the array.
   *
   * @return the array
   */
  public Object getArray() {
    return array;
  }

  /**
   * Listens for cell events (data changes) from ArrayTable.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    // forward event to listeners
    changed = true;
    firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
  }

  /**
   * Sets the editable property.
   *
   * @param editable true to allow editing of the cell values
   */
  public void setEditable(boolean editable) {
    for(int i = 0; i<tables.length; i++) {
      tables[i].setEditable(editable);
    }
  }

  /**
   * Refresh the data in the table.
   */
  public void refreshTable() {
    for(int i = 0; i<tables.length; i++) {
      tables[i].refreshTable();
    }
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    setSize(400, 300);
    setContentPane(new JPanel(new BorderLayout()));
    scrollpane = new JScrollPane(tables[0]);
    if(tables.length>1) {
      // create spinner
      SpinnerModel model = new SpinnerNumberModel(0, 0, tables.length-1, 1);
      spinner = new JSpinner(model);
      JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
      editor.getTextField().setFont(tables[0].getFont());
      spinner.setEditor(editor);
      spinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int i = ((Integer) spinner.getValue()).intValue();
          scrollpane.setViewportView(tables[i]);
        }

      });
      Dimension dim = spinner.getMinimumSize();
      spinner.setMaximumSize(dim);
      getContentPane().add(scrollpane, BorderLayout.CENTER);
      JToolBar toolbar = new JToolBar();
      toolbar.setFloatable(false);
      toolbar.add(new JLabel(" index ")); //$NON-NLS-1$
      toolbar.add(spinner);
      toolbar.add(Box.createHorizontalGlue());
      getContentPane().add(toolbar, BorderLayout.NORTH);
    } else {
      scrollpane.createHorizontalScrollBar();
      getContentPane().add(scrollpane, BorderLayout.CENTER);
    }
  }

  //_____________________________private constructors___________________________
  private ArrayInspector() {
    super((Frame) null, true); // modal dialog
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        if(changed) {
          firePropertyChange("arrayData", null, null); //$NON-NLS-1$
        }
      }

    });
  }

  private ArrayInspector(int[] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: int[row]");                       //$NON-NLS-1$
  }

  private ArrayInspector(int[] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": int[row]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(int[][] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: int[row][column]");               //$NON-NLS-1$
  }

  private ArrayInspector(int[][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": int[row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(int[][][] array) {
    this();
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
    setTitle("Array: int[index][row][column]"); //$NON-NLS-1$
  }

  private ArrayInspector(int[][][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": int[index][row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(double[] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: double[row]");                    //$NON-NLS-1$
  }

  private ArrayInspector(double[] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": double[row]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(double[][] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: double[row][column]");            //$NON-NLS-1$
  }

  private ArrayInspector(double[][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": double[row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(double[][][] array) {
    this();
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
    setTitle("Array: double[index][row][column]"); //$NON-NLS-1$
  }

  private ArrayInspector(double[][][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": double[index][row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(String[] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: String[row]");                    //$NON-NLS-1$
  }

  private ArrayInspector(String[] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": String[row]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(String[][] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: String[row][column]");            //$NON-NLS-1$
  }

  private ArrayInspector(String[][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": String[row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(String[][][] array) {
    this();
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
    setTitle("Array: String[index][row][column]"); //$NON-NLS-1$
  }

  private ArrayInspector(String[][][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": String[index][row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(boolean[] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: boolean[row]");                   //$NON-NLS-1$
  }

  private ArrayInspector(boolean[] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": boolean[row]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(boolean[][] array) {
    this();
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    createGUI();
    setTitle("Array: boolean[row][column]");           //$NON-NLS-1$
  }

  private ArrayInspector(boolean[][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": boolean[row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private ArrayInspector(boolean[][][] array) {
    this();
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    }
    createGUI();
    setTitle("Array: boolean[index][row][column]"); //$NON-NLS-1$
  }

  private ArrayInspector(boolean[][][] array, String arrayName) {
    this(array);
    setTitle("Array \""+arrayName+"\": boolean[index][row][column]"); //$NON-NLS-1$ //$NON-NLS-2$
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
