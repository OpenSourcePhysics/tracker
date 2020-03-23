/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

/**
 *  TableFrame displays a DataTable with a scroll pane in a frame.
 *
 * @author     Joshua Gould
 * @author     Wolfgang Christian
 * @created    August 16, 2002
 * @version    1.0
 */
public class DataTableFrame extends OSPFrame {
  protected JMenuBar menuBar;
  protected JMenu fileMenu;
  protected JMenu editMenu;
  protected JMenuItem saveAsItem;
  protected DataTable table;

  /**
   *  TableFrame Constructor
   *
   * @param  table  Description of the Parameter
   */
  public DataTableFrame(DataTable table) {
    this(DisplayRes.getString("DataTableFrame.DefaultTitle"), table); //$NON-NLS-1$
  }

  /**
   *  TableFrame Constructor
   *
   * @param  title
   * @param  _table  Description of the Parameter
   */
  public DataTableFrame(String title, DataTable _table) {
    super(title);
    table = _table;
    JScrollPane scrollPane = new JScrollPane(table);
    Container c = getContentPane();
    c.add(scrollPane, BorderLayout.CENTER);
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    pack();
    // setVisible(true);
    if(!OSPRuntime.appletMode) {
      createMenuBar();
      loadDisplayMenu();
    }
  }

  /**
   * Adds a Display menu to the menu bar.
   */
  protected JMenu loadDisplayMenu() {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    JMenu displayMenu = new JMenu();
    displayMenu.setText(DisplayRes.getString("DataTableFrame.Display_menu_title")); //$NON-NLS-1$
    menuBar.add(displayMenu);
    JMenuItem setFontItem = new JMenuItem(DisplayRes.getString("DataTableFrame.NumberFormat_menu_item_title")); //$NON-NLS-1$
    setFontItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setNumberFormat();
      }

    });
    displayMenu.add(setFontItem);
    return displayMenu;
  }

  private void createMenuBar() {
    menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    fileMenu = new JMenu(DisplayRes.getString("DataTableFrame.File_menu_item_title")); //$NON-NLS-1$
    editMenu = new JMenu(DisplayRes.getString("DataTableFrame.Edit_menu_item_title")); //$NON-NLS-1$
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    JMenuItem saveAsItem = new JMenuItem(DisplayRes.getString("DataTableFrame.SaveAs_menu_item_title"));      //$NON-NLS-1$
    JMenuItem copyItem = new JMenuItem(DisplayRes.getString("DataTableFrame.Copy_menu_item_title"));          //$NON-NLS-1$
    JMenuItem selectAlItem = new JMenuItem(DisplayRes.getString("DataTableFrame.SelectAll_menu_item_title")); //$NON-NLS-1$
    fileMenu.add(saveAsItem);
    editMenu.add(copyItem);
    editMenu.add(selectAlItem);
    copyItem.setAccelerator(KeyStroke.getKeyStroke('C', DrawingFrame.MENU_SHORTCUT_KEY_MASK));
    copyItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copy();
      }

    });
    selectAlItem.setAccelerator(KeyStroke.getKeyStroke('A', DrawingFrame.MENU_SHORTCUT_KEY_MASK));
    selectAlItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        table.selectAll();
      }

    });
    saveAsItem.setAccelerator(KeyStroke.getKeyStroke('S', DrawingFrame.MENU_SHORTCUT_KEY_MASK));
    saveAsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveAs();
      }

    });
    validate();
  }

  void setNumberFormat() {
    int digits = table.getMaximumFractionDigits();
    String str = JOptionPane.showInputDialog(this, DisplayRes.getString("DataTableFrame.NumberOfDigits_option_pane_title"), ""+digits); //$NON-NLS-1$ //$NON-NLS-2$
    if(str==null) {
      return;
    }
    digits = Integer.parseInt(str);
    digits = Math.max(digits, 1);
    table.setMaximumFractionDigits(Math.min(digits, 16));
    table.refreshTable();
  }

  /** Copies the data in the table to the system clipboard */
  public void copy() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    int[] selectedRows = table.getSelectedRows();
    int[] selectedColumns = table.getSelectedColumns();
    StringBuffer buf = getSelectedData(selectedRows, selectedColumns);
    StringSelection stringSelection = new StringSelection(buf.toString());
    clipboard.setContents(stringSelection, stringSelection);
  }

  /**
   *  Refresh the data in the DataTable, as well as other changes to the table,
   *  such as row number visibility. Changes to the TableModels displayed in the
   *  table will not be visible until this method is called.
   */
  public void refreshTable() {
    table.refreshTable();
  }

  /**
   *  Gets the data selected by the user in the table.
   *
   * @param  selectedRows     Description of the Parameter
   * @param  selectedColumns  Description of the Parameter
   * @return                  the selected data.
   */
  public StringBuffer getSelectedData(int[] selectedRows, int[] selectedColumns) {
    StringBuffer buf = new StringBuffer();
    for(int i = 0; i<selectedRows.length; i++) {
      for(int j = 0; j<selectedColumns.length; j++) {
        int row = i;
        int temp = table.convertColumnIndexToModel(selectedColumns[j]);
        if(table.isRowNumberVisible()) {
          if(temp==0) {
            continue;
          }
        }
        Object value = table.getValueAt(row, selectedColumns[j]); // column converted to model
        if(value!=null) {
          buf.append(value);
        }
        buf.append("\t");                                         //$NON-NLS-1$
      }
      buf.append("\n");                                           //$NON-NLS-1$
    }
    return buf;
  }

  /**
 * Sorts  the table using the given column.
 * @param col int
 */
  public void sort(int col) {
    table.sort(col);
  }

  /**
   *  Pops open a save file dialog to save the data in this table to a file.
   */
  public void saveAs() {
    File file = GUIUtils.showSaveDialog(this);
    if(file==null) {
      return;
    }
    int firstRow = 0;
    int lastRow = table.getRowCount()-1;
    int lastColumn = table.getColumnCount()-1;
    int firstColumn = 0;
    if(table.isRowNumberVisible()) {
      firstColumn++;
    }
    int[] selectedRows = new int[lastRow+1];
    int[] selectedColumns = new int[lastColumn+1];
    for(int i = firstRow; i<=lastRow; i++) {
      selectedRows[i] = i;
    }
    for(int i = firstColumn; i<=lastColumn; i++) {
      selectedColumns[i] = i;
    }
    try {
      FileWriter fw = new FileWriter(file);
      PrintWriter pw = new PrintWriter(fw);
      pw.print(getSelectedData(selectedRows, selectedColumns));
      pw.close();
    } catch(IOException e) {
      JOptionPane.showMessageDialog(this, DisplayRes.getString("DataTableFrame.SaveErrorMessage"), DisplayRes.getString("DataTableFrame.Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
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
