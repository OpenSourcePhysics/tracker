/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.opensourcephysics.display.CellBorder;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This displays statistics of data columns in a DataToolTable.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class DataToolStatsTable extends JTable {
  // instance fields
  DataToolTable dataTable;
  StatsTableModel statsModel;
  DataToolTable.LabelRenderer labelRenderer;
  NumberRenderer numberRenderer = new NumberRenderer(3);
  Object[][] statsData; // statsData[col] contains stats for a table column

  /**
   * Constructor.
   *
   * @param table the datatable
   */
  public DataToolStatsTable(DataToolTable table) {
    dataTable = table;
    statsModel = new StatsTableModel();
    // add mouse listeners for table
    addMouseMotionListener(new MouseInputAdapter() {
    	@Override
      public void mouseMoved(MouseEvent e) {
        int col = columnAtPoint(e.getPoint());
        int labelCol = convertColumnIndexToView(0);
        if(col==labelCol) {
          setToolTipText(null);
        }
        else {
          int row = rowAtPoint(e.getPoint());
         	Object val = getValueAt(row, col);
         	Object stat = getValueAt(row, labelCol);
        	String name = dataTable.getColumnName(col);            
          setToolTipText(stat+"_"+name+" = "+val); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    });
    addMouseListener(new MouseInputAdapter() {
    	@Override
    	public void mouseEntered(MouseEvent e) {
    		dataTable.dataToolTab.refreshStatusBar(dataTable.dataToolTab.getCorrelationString());
    	}
    });
    
    init();
  }

  /**
   * Initializes the table.
   */
  protected void init() {
    dataTable.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
      public void columnAdded(TableColumnModelEvent e) {
        /** empty block */
      }
      public void columnRemoved(TableColumnModelEvent e) {
        /** empty block */
      }
      public void columnSelectionChanged(ListSelectionEvent e) {
        /** empty block */
      }
      public void columnMarginChanged(ChangeEvent e) {
        refreshTable();
      }
      public void columnMoved(TableColumnModelEvent e) {
        refreshTable();
      }

    });
    // create statistics data array
    refreshStatistics();
    // set and configure table model and header
    setModel(statsModel);
    setGridColor(Color.blue);
    setTableHeader(null); // no table header
    labelRenderer = dataTable.labelRenderer;
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    ListSelectionModel selectionModel = dataTable.getSelectionModel();
    selectionModel.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        // workaround to prevent exceptions
        if(e.getFirstIndex()>-1) {
          refreshStatistics();
        }
      }

    });
    refreshCellWidths();
  }

  /**
   * Returns labels that describe the statistics.
   *
   * @return labels for max, min, mean, SD, SE and data count
   */
  private String[] getStatLabels() {
    return new String[] {ToolsRes.getString("Table.Entry.Max"),           //$NON-NLS-1$
                         ToolsRes.getString("Table.Entry.Min"),           //$NON-NLS-1$
                         ToolsRes.getString("Table.Entry.Mean"),          //$NON-NLS-1$
                         ToolsRes.getString("Table.Entry.StandardDev"),   //$NON-NLS-1$
                         ToolsRes.getString("Table.Entry.StandardError"), //$NON-NLS-1$
                         ToolsRes.getString("Table.Entry.Count")};        //$NON-NLS-1$
  }

  /**
   * Calculates statistical values for a data array.
   *
   * @param data the data array
   * @return the max, min, mean, SD, SE and non-NaN data count
   */
  private Object[] getStatistics(double[] data) {
    double max = -Double.MAX_VALUE;
    double min = Double.MAX_VALUE;
    double sum = 0.0;
    double squareSum = 0.0;
    int count = 0;
    for(int i = 0; i<data.length; i++) {
      if(Double.isNaN(data[i])) {
        continue;
      }
      count++;
      max = Math.max(max, data[i]);
      min = Math.min(min, data[i]);
      sum += data[i];
      squareSum += data[i]*data[i];
    }
    double mean = sum/count;
    double sd = (count<2) ? Double.NaN : Math.sqrt((squareSum-count*mean*mean)/(count-1));
    if(max==-Double.MAX_VALUE) {
      max = Double.NaN;
    }
    if(min==Double.MAX_VALUE) {
      min = Double.NaN;
    }
    return new Object[] {new Double(max), new Double(min), new Double(mean), new Double(sd), new Double(sd/Math.sqrt(count)), new Integer(count)};
  }

  /**
   *  Refresh the data display in this table.
   */
  public void refreshTable() {
    Runnable refresh = new Runnable() {
      public synchronized void run() {
        tableChanged(new TableModelEvent(statsModel, TableModelEvent.HEADER_ROW));
        refreshCellWidths();
      }

    };
    if(SwingUtilities.isEventDispatchThread()) {
      refresh.run();
    } else {
      SwingUtilities.invokeLater(refresh);
    }
  }

  /**
   *  Refresh the statistics data.
   */
  public void refreshStatistics() {
    // assemble statistics data array
    TableModel model = dataTable.getModel();
    int[] rows = dataTable.getSelectedRows();
    int[] cols = dataTable.getSelectedColumns();
    statsData = new Object[model.getColumnCount()][0];
    ArrayList<Double> datalist = new ArrayList<Double>();
    // for each column, assemble valid selected data and get stats
    statsData[0] = getStatLabels();
    for(int j = 1; j<model.getColumnCount(); j++) {
      datalist.clear();
      for(int i = 0; i<model.getRowCount(); i++) {
        Double val = (Double) model.getValueAt(i, j);
        if(val==null) {
          val = new Double(Double.NaN);
        }
        datalist.add(val);
      }
      double[] x = new double[datalist.size()];
      for(int i = 0; i<x.length; i++) {
        x[i] = datalist.get(i).doubleValue();
      }
      double[] selected = x;
      if(rows.length>0) {
        // is column selected?
        boolean colSelected = false;
        int col = dataTable.convertColumnIndexToView(j);
        for(int k = 0; k<cols.length; k++) {
          colSelected = colSelected||(col==cols[k]);
        }
        if(colSelected) {
          selected = new double[rows.length];
          for(int i = 0; i<rows.length; i++) {
            selected[i] = x[rows[i]];
          }
        }
      }
      statsData[j] = getStatistics(selected);
    }
    refreshTable();
  }

  /**
   *  Refresh the cell widths in the table.
   */
  public void refreshCellWidths() {
    // set width of columns
    if(getColumnCount()!=dataTable.getColumnCount()) {
      return;
    }
    for(int i = 0; i<getColumnCount(); i++) {
      TableColumn propColumn = getColumnModel().getColumn(i);
      TableColumn dataColumn = dataTable.getColumnModel().getColumn(i);
      propColumn.setMaxWidth(dataColumn.getWidth());
      propColumn.setMinWidth(dataColumn.getWidth());
      propColumn.setWidth(dataColumn.getWidth());
    }
  }

  /**
   *  Refresh the GUI.
   */
  public void refreshGUI() {
  	numberRenderer.format.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
    refreshStatistics();
  }

  /**
   * Returns the renderer for a cell specified by row and column.
   *
   * @param row the row number
   * @param column the column number
   * @return the cell renderer
   */
  public TableCellRenderer getCellRenderer(int row, int column) {
    int i = dataTable.convertColumnIndexToModel(column);
    if(i==0) {
      return labelRenderer;
    }
    return numberRenderer;
  }

  public void setFont(Font font) {
    super.setFont(font);
    if(numberRenderer!=null) {
      numberRenderer.font = font;
    }
    setRowHeight(font.getSize()+4);
  }

  /**
   * A class to provide model data for this table.
   */
  class StatsTableModel extends AbstractTableModel {
    public String getColumnName(int col) {
      return dataTable.getColumnName(col);
    }

    public int getRowCount() {
      return statsData[0].length;
    }

    public int getColumnCount() {
      return dataTable.getModel().getColumnCount();
    }

    public Object getValueAt(int row, int col) {
      int i = dataTable.convertColumnIndexToModel(col);
      return statsData[i][row];
    }

    public boolean isCellEditable(int row, int col) {
      return false;
    }

    public Class<?> getColumnClass(int c) {
      return getValueAt(0, c).getClass();
    }

  }

  /**
   * A class to render numbers and strings.
   */
  class NumberRenderer extends JLabel implements TableCellRenderer {
    DecimalFormat format = (DecimalFormat)NumberFormat.getInstance();
    Font font;

    /**
     * Constructor NumberRenderer
     * @param sigfigs
     */
    public NumberRenderer(int sigfigs) {
      sigfigs = Math.min(sigfigs, 6);
      if(format instanceof DecimalFormat) {
        String pattern = "0.0"; //$NON-NLS-1$
        for(int i = 0; i<sigfigs-1; i++) {
          pattern += "0";       //$NON-NLS-1$
        }
        pattern += "E0";        //$NON-NLS-1$
        ((DecimalFormat) format).applyPattern(pattern);
      }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if(font==null) {
        font = getDefaultRenderer(String.class).getTableCellRendererComponent(DataToolStatsTable.this, "", false, false, 0, 0).getFont(); //$NON-NLS-1$
      }
      setFont(font);
      setHorizontalAlignment(SwingConstants.TRAILING);
      setBorder(new CellBorder(new Color(240, 240, 240)));
      if(value instanceof Integer) {
        setText(String.valueOf(value));
      } else {
        setText(format.format(value));
      }
      return this;
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
