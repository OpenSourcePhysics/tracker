/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
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

/**
 * A panel that displays an ArrayTable.
 *
 * @author Douglas Brown
 * @author Wolfgang Christian
 * @version 1.0
 */
public class ArrayPanel extends JPanel implements PropertyChangeListener, Data {
  JTabbedPane tabbedPane = new JTabbedPane();
  ArrayTable[] tables;
  JSpinner spinner;
  JScrollPane scrollpane;
  Object array;
  boolean changed;
  String format = null;
  int firstRowIndex = 0, firstColIndex = 0;
  boolean rowNumberVisible = true, editable = true;
  String[] colNames;
  private int ID = hashCode();

  /**
   * Constructor ArrayPanel
   */
  public ArrayPanel() {
    //System.out.println("creating array panel");
  }

  public static ArrayPanel getArrayPanel(Object arrayObj) {
    ArrayPanel arrayPanel = new ArrayPanel();
    arrayPanel.setArray(arrayObj);
    return arrayPanel;
  }

  /**
   * Gets an array panel for the specified array.
   *
   * @param arrayObj the array
   * @param name the display name for the array
   * @return the array panel
   */
  public void setArray(Object arrayObj) {
    if(!canDisplay(arrayObj)) {
      return;
    }
    if(arrayObj instanceof double[]) {
      setArray((double[]) arrayObj);
    } else if(arrayObj instanceof double[][]) {
      setArray((double[][]) arrayObj);
    } else if(arrayObj instanceof double[][][]) {
      setArray((double[][][]) arrayObj);
    } else if(arrayObj instanceof int[]) {
      setArray((int[]) arrayObj);
    } else if(arrayObj instanceof int[][]) {
      setArray((int[][]) arrayObj);
    } else if(arrayObj instanceof int[][][]) {
      setArray((int[][][]) arrayObj);
    } else if(arrayObj instanceof String[]) {
      setArray((String[]) arrayObj);
    } else if(arrayObj instanceof String[][]) {
      setArray((String[][]) arrayObj);
    } else if(arrayObj instanceof String[][][]) {
      setArray((String[][][]) arrayObj);
    } else if(arrayObj instanceof boolean[]) {
      setArray((boolean[]) arrayObj);
    } else if(arrayObj instanceof boolean[][]) {
      setArray((boolean[][]) arrayObj);
    } else if(arrayObj instanceof boolean[][][]) {
      setArray((boolean[][][]) arrayObj);
    }
    this.array = arrayObj;
  }

  /**
   * Determines if an object is an array that can be displayed.
   *
   * @param obj the object
   * @return true if it can be inspected
   */
  public static boolean canDisplay(Object obj) {
    if(obj==null) {
      return false;
    }
    if((obj instanceof double[])||(obj instanceof double[][])||(obj instanceof double[][][])||(obj instanceof int[])||(obj instanceof int[][])||(obj instanceof int[][][])||(obj instanceof boolean[])||(obj instanceof boolean[][])||(obj instanceof boolean[][][])||(obj instanceof String[])||(obj instanceof String[][])||(obj instanceof String[][][])) {
      return true;
    }
    return false;
  }

  /**
   * Gets the object being displayed.
   *
   * @return
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
   * Sets the same numeric display format for all columns
   *
   * @param _format String
   */
  public void setNumericFormat(String _format) {
    this.format = _format;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setNumericFormat(_format);
    }
  }

  /**
   * Sets the numeric display format for each column
   *
   * @param _format String[]
   */
  public void setNumericFormat(String[] _format) {
    this.format = null;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setNumericFormat(_format);
    }
  }

  /**
   * Sets the display row number flag. Table displays row number.
   *
   * @param vis <code>true<\code> if table display row number
   */
  public void setRowNumberVisible(boolean vis) {
    this.rowNumberVisible = vis;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setRowNumberVisible(vis); // refreshes the table
    }
  }

  /**
   * Sets the column names in the table models.
   *
   * @param names
   */
  public void setColumnNames(String[] names) {
    for(int i = 0; i<tables.length; i++) {
      tables[i].setColumnNames(names);
    }
  }

  /**
   * Sets the column names in each table model separately.
   *
   * @param names
   */
  public void setColumnNames(String[][] names) {
    int n = Math.min(tables.length, names.length); // ensure correct dimensions
    for(int i = 0; i<n; i++) {
      tables[i].setColumnNames(names[i]);
    }
  }

  // Preferred column width changes by Willy Gerber

  /**
   * Sets this column's preferred width of the given column.
   * The minimum width is set to zero and the maximum width is set to 300.
   *
   * @param ncol the column
   * @param nwidth the preferred width
   */
  public void setPreferredColumnWidth(int ncol, int nwidth) {
    for(int table = 0; table<tables.length; table++) {
      javax.swing.table.TableColumn column = tables[table].getColumnModel().getColumn(ncol);
      column.setMinWidth(0);
      column.setMaxWidth(300);
      column.setPreferredWidth(nwidth);
    }
  }

  /**
   * Sets this column's preferred width of the entire table.
   * The minimum width is set to zero and the maximum width is set to 300.
   *
   * @param nwidth the preferred width
   */
  public void setPreferredColumnWidth(int nwidth) {
    for(int table = 0; table<tables.length; table++) {
      for(int col = 0; col<tables[table].getColumnCount(); col++) {
        javax.swing.table.TableColumn column = tables[table].getColumnModel().getColumn(col);
        column.setMinWidth(0);
        column.setMaxWidth(300);
        column.setPreferredWidth(nwidth);
      }
    }
  }

  /**
   * Sets the alignment of the contents of the given column along the X axis.
   * The alignment constants are defined in the SwingConstants class.
   *
   * @param ncol the column
   * @param align  One of the following constants defined in <code>SwingConstants</code>:
   *           <code>LEFT</code>,
   *           <code>CENTER</code> (the default for image-only labels),
   *           <code>RIGHT</code>,
   *           <code>LEADING</code> (the default for text-only labels) or
   *           <code>TRAILING</code>.
   */
  public void setColumnAlignment(int ncol, int align) {
    for(int table = 0; table<tables.length; table++) {
      for(int row = 0; row<tables[table].getRowCount(); row++) {
        javax.swing.table.TableCellRenderer renderer = tables[table].getCellRenderer(row, ncol);
        ((JLabel) renderer).setHorizontalAlignment(align);
      }
    }
  }

  /**
   * Sets the alignment of the contents of all table columns along the X axis.
   * The alignment constants are defined in the SwingConstants class.
   *
   * @param align  One of the following constants defined in <code>SwingConstants</code>:
   *           <code>LEFT</code>,
   *           <code>CENTER</code> (the default for image-only labels),
   *           <code>RIGHT</code>,
   *           <code>LEADING</code> (the default for text-only labels) or
   *           <code>TRAILING</code>.
   */
  public void setColumnAlignment(int align) {
    for(int table = 0; table<tables.length; table++) {
      for(int row = 0; row<tables[table].getRowCount(); row++) {
        for(int col = 0; col<tables[table].getColumnCount(); col++) {
          javax.swing.table.TableCellRenderer renderer = tables[table].getCellRenderer(row, col);
          ((JLabel) renderer).setHorizontalAlignment(align);
        }
      }
    }
  }

  // End of changes by Willy Gerber
  public int getFirstRowIndex() {
    return this.firstRowIndex;
  }

  /**
   * Sets the first row's index.
   *
   * @param index
   */
  public void setFirstRowIndex(int index) {
    this.firstRowIndex = index;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setFirstRowIndex(index);
    }
  }

  /**
   * Sets the first column's index.
   *
   * @param index
   */
  public void setFirstColIndex(int index) {
    this.firstColIndex = index;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setFirstColIndex(index);
    }
  }

  /**
   * Sets the column's lock flag.
   *
   * @param column   int
   * @param locked   boolean
   */
  public void setColumnLock(int columnIndex, boolean locked) {
    for(int i = 0; i<tables.length; i++) {
      tables[i].setColumnLock(columnIndex, locked);
    }
  }

  /**
   * Sets the lock flag for multiple columns.  Previously set locks are cleared.
   *
   * @param locked   boolean array
   */
  public void setColumnLocks(boolean[] locked) {
    for(int i = 0; i<tables.length; i++) {
      tables[i].setColumnLocks(locked);
    }
  }

  /**
   * Sets the editable property for the entire panel.
   *
   * @param editable true to allow editing of the cell values
   */
  public void setEditable(boolean _editable) {
    this.editable = _editable;
    for(int i = 0; i<tables.length; i++) {
      tables[i].setEditable(_editable);
    }
  }

  /**
   * Sets the transposed property for the array.
   * A transposed array switches its row and column values in the display.
   *
   * @param transposed
   */
  public void setTransposed(boolean transposed) {
    for(int i = 0; i<tables.length; i++) {
      tables[i].setTransposed(transposed);
    }
  }

  /**
   * Sets the font for this component.
   *
   * @param font the desired <code>Font</code> for this component
   * @see java.awt.Component#getFont
   */
  public void setFont(Font font){ // Added by Paco
    super.setFont(font);
    if (tables!=null) for(int i = 0; i<tables.length; i++) tables[i].setFont(font);
  }
  
  /**
   * Sets the foreground color of this component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param fg  the desired foreground <code>Color</code> 
   * @see java.awt.Component#getForeground
   */
  public void setForeground(Color color){ // Added by Paco
    super.setForeground(color);
    if (tables!=null) for(int i = 0; i<tables.length; i++) tables[i].setForeground(color);
  }

  /**
   * Sets the background color of this component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param fg  the desired background <code>Color</code> 
   * @see java.awt.Component#getBackground
   */
  public void setBackground(Color color){ // Added by Paco
    super.setBackground(color);
    if (tables!=null) for(int i = 0; i<tables.length; i++) tables[i].setBackground(color);
  }

  /**
   * Sets the data foreground color of this component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param fg  the desired foreground <code>Color</code> 
   */
  public void setDataForeground(Color color){ // Added by Paco
    if (tables!=null) for(int i = 0; i<tables.length; i++) tables[i].setDataForeground(color);
    refreshTable();
  }

  /**
   * Sets the data background color of this component.  It is up to the
   * look and feel to honor this property, some may choose to ignore
   * it.
   *
   * @param fg  the desired background <code>Color</code> 
   * @see java.awt.Component#getBackground
   */
  public void setDataBackground(Color color){ // Added by Paco
    if (tables!=null) for(int i = 0; i<tables.length; i++) tables[i].setDataBackground(color);
    refreshTable();
  }

  /**
   * Sets the table's auto resize mode when the table is resized.
   *
   * @param   mode One of 5 legal values:
   *                   AUTO_RESIZE_OFF,
   *                   AUTO_RESIZE_NEXT_COLUMN,
   *                   AUTO_RESIZE_SUBSEQUENT_COLUMNS,
   *                   AUTO_RESIZE_LAST_COLUMN,
   *                   AUTO_RESIZE_ALL_COLUMNS
   */
  public void setAutoResizeMode(int mode) {
    if (tables!=null) for(int i = 0; i<tables.length; i++) tables[i].setAutoResizeMode(mode);
    refreshTable();
  }

  // -------------------------------
  // Getters
  // -------------------------------
  public int getNumColumns() {
    return tables[0].getColumnCount();
  }

  //-------------------------------
  // Getters
  // -------------------------------

  /**
   * Refresh the data in all the tables.
   */
  public void refreshTable() {
    for(int i = 0; i<tables.length; i++) {
      tables[i].refreshTable();
    }
  }

  /**
   * Sets the <code>Timer</code>'s initial time delay (in milliseconds)
   * to wait after the timer is started
   * before firing the first event.
   * @param delay
   */
  public void setRefreshDelay(int delay) {
    for(int i = 0; i<tables.length; i++) {
      tables[i].setRefreshDelay(delay);
    }
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    this.removeAll(); // remove old elements Paco: be careful with this. If you use the ArrayPanel as a normal JPanel
    // and have added another component, it will be lost!
    this.setPreferredSize(new Dimension(400, 300));
    this.setLayout(new BorderLayout());
    scrollpane = new JScrollPane(tables[0]);
    if(tables.length>1) {
      // create spinner
      SpinnerModel model = new SpinnerNumberModel(0, 0, tables.length-1, 1);
      spinner = new JSpinner(model);
      JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
      editor.getTextField().setFont(tables[0].indexRenderer.getFont());
      spinner.setEditor(editor);
      spinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          int i = ((Integer) spinner.getValue()).intValue();
          scrollpane.setViewportView(tables[i]);
        }

      });
      Dimension dim = spinner.getMinimumSize();
      spinner.setMaximumSize(dim);
      add(scrollpane, BorderLayout.CENTER);
      JToolBar toolbar = new JToolBar();
      toolbar.setFloatable(false);
      toolbar.add(new JLabel(" index ")); //$NON-NLS-1$
      toolbar.add(spinner);
      toolbar.add(Box.createHorizontalGlue());
      add(toolbar, BorderLayout.NORTH);
    } else {
      scrollpane.createHorizontalScrollBar();
      add(scrollpane, BorderLayout.CENTER);
    }
    this.validate();                      // refresh the display
  }
  
  //_____________________________private constructors___________________________
  private void setArray(int[] array) {
    if(this.array instanceof int[]) { // && ((int[])this.array).length==array.length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(int[][] array) {
    if(this.array instanceof int[][]) { //&& ((int[][])this.array).length==array.length  && ((int[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(int[][][] array) {
    /* Isn't this too much? Paco
    if (this.array instanceof int[][][] && ((int[][][])this.array).length==array.length ) {
      boolean quickChange = true;
      int[][][] thisArray = (int[][][]) this.array;
      for (int i=0; i<thisArray.length; i++) {
        if (thisArray[i].length!=array[i].length || thisArray[i][0].length!=array[i][0].length) {
          quickChange = false;
          break;
        }
      }
      if (quickChange) {
        for (int i=0; i<thisArray.length; i++) tables[i].tableModel.setArray(array[i]);
        return;
      }
    }
    */
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
      tables[i].setFont(getFont());
      tables[i].setForeground(getForeground());
      tables[i].setBackground(getBackground());
    }
    createGUI();
  }

  private void setArray(double[] array) {
    if(this.array instanceof double[]) { // ((double[])this.array).length==array.length) { // Paco added this
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(double[][] array) {
    if(this.array instanceof double[][]) { //&& ((double[][])this.array).length==array.length && ((double[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(double[][][] array) {
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
      tables[i].setFont(getFont());
      tables[i].setForeground(getForeground());
      tables[i].setBackground(getBackground());
    }
    createGUI();
  }

  private void setArray(String[] array) {
    if(this.array instanceof String[]) { // && ((String[])this.array).length==array.length) { // Paco added this
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(String[][] array) {
    if(this.array instanceof String[][]) { //&& ((String[][])this.array).length==array.length && ((String[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(String[][][] array) {
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
      tables[i].setFont(getFont());
      tables[i].setForeground(getForeground());
      tables[i].setBackground(getBackground());
    }
    createGUI();
  }

  private void setArray(boolean[] array) {
    if(this.array instanceof boolean[]) { // && ((boolean[])this.array).length==array.length) { // Paco added this
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(boolean[][] array) {
    if(this.array instanceof boolean[][]) { //&& ((boolean[][])this.array).length==array.length && ((boolean[][])this.array)[0].length==array[0].length) { // Paco
      tables[0].tableModel.setArray(array);
      return;
    }
    tables = new ArrayTable[1];
    tables[0] = new ArrayTable(array);
    tables[0].addPropertyChangeListener("cell", this); //$NON-NLS-1$
    tables[0].setFont(getFont());
    tables[0].setForeground(getForeground());
    tables[0].setBackground(getBackground());
    createGUI();
  }

  private void setArray(boolean[][][] array) {
    tables = new ArrayTable[array.length];
    for(int i = 0; i<tables.length; i++) {
      tables[i] = new ArrayTable(array[i]);
      tables[i].addPropertyChangeListener("cell", this); //$NON-NLS-1$
      tables[i].setFont(getFont());
      tables[i].setForeground(getForeground());
      tables[i].setBackground(getBackground());
    }
    createGUI();
  }

  // The following methods were added to implement the Data interface for double[][] arrays.

  /**
   * Gets column names from Table Model.
   * Implementation of Data interface.
   */
  public String[] getColumnNames() {
    double[][] data = getData2D();
    if(data==null) {
      return null; // no data
    }
    int n = data.length;
    if((colNames==null)||(n!=colNames.length)) { // every array columns should have a name
      colNames = new String[n];
    }
    String[] modelNames = tables[0].tableModel.columnNames;
    int stop = (modelNames==null) ? 0 : Math.min(n, modelNames.length-1);
    for(int i = 0; i<stop; i++) {    // assign model names to Data columns
      colNames[i] = modelNames[i+1]; // skip zero column name because it is the row index
    }
    for(int i = stop; i<n; i++) { // assign default column names
      colNames[i] = "C"+(i+1);    //$NON-NLS-1$
    }
    return colNames;
  }

  /**
   * Gets double[][] data from the Table Model and transposes this array if necessary.
   * Implementation of Data interface.
   */
  public double[][] getData2D() {
    if((tables==null)||(tables[0]==null)) {
      return null;
    }
    boolean transposed = tables[0].tableModel.transposed;
    double[][] data = tables[0].tableModel.doubleArray2;
    if(!transposed&&(data!=null)) { // first index of the array is the DataPanel column
      int r = data.length;
      int c = 0;
      for(int i = 0; i<r; i++) {    // find the largest index to set the number of columns
        c = Math.max(c, data[i].length);
      }
      double[][] tdata = new double[c][r];
      for(int i = 0; i<r; i++) {
        int ci = data[i].length;
        for(int j = 0; j<ci; j++) {
          tdata[j][i] = data[i][j];
        }
        for(int j = ci; j<c; j++) {
          tdata[j][i] = Double.NaN;
        }
      }
      return tdata;
    }
    return data;
  }

  /**
   * Not used because double[][][] is not used in any OSP Tools.
   * Implementation of Data interface method.
   */
  public double[][][] getData3D() {
    return null;
  }

  /**
   * Not used because Data is stored in this object, not in a list of Data objects.
   * Implementation of Data interface.
   */
  public List<Data> getDataList() {
    return null;
  }

  /**
   * Not used Data because is stored in 2D arrays.
   * Implementation of Data interface.
   */
  public ArrayList<Dataset> getDatasets() {
    return null;
  }

  /**
   * Fill colors for columns are not specified. Client should assign colors.
   * Implementation of Data interface.
   */
  public Color[] getFillColors() {
    return null;
  }

  /**
   * Lines colors for columns are not specified.  Client should assign colors.
   * Implementation of Data interface.
   */
  public Color[] getLineColors() {
    return null;
  }

  /**
   * Gets the Data ID.
   */
  public int getID() {
    boolean transposed = tables[0].tableModel.transposed;
    if(transposed) {
      return ID^0xffff; // exclusive OR to reverse and produce new ID
    }
    return ID;
  }

  /**
   * Sets the Data ID.
   */
  public void setID(int id) {
    ID = id;
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
