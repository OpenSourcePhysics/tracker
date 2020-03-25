package org.opensourcephysics.tools;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class DiagnosticsForThreads extends JPanel {
  private ThreadViewerTableModel tableModel = new ThreadViewerTableModel();

  public DiagnosticsForThreads() {

    JTable table = new JTable(tableModel);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    // code added Feb 2014 by Doug Brown
  	FontSizer.setFonts(table, FontSizer.getLevel());
  	Font font = table.getFont();
  	table.setRowHeight(font.getSize()+4);
  	table.getTableHeader().setFont(font);
    // end added code

    TableColumnModel colModel = table.getColumnModel();
    int numColumns = colModel.getColumnCount();

    for (int i = 0; i < numColumns - 1; i++) {
      TableColumn col = colModel.getColumn(i);

      col.sizeWidthToFit();
      col.setPreferredWidth(col.getWidth() + 5);
      col.setMaxWidth(col.getWidth() + 5);
    }

    JScrollPane sp = new JScrollPane(table);

    setLayout(new BorderLayout());
    add(sp, BorderLayout.CENTER);
  }

  public void dispose() {
    tableModel.stopRequest();
  }

  protected void finalize() throws Throwable {
    dispose();
  }

  public static void aboutThreads() {
	    JDialog dialog = new JDialog(); 
	    DiagnosticsForThreads viewer = new DiagnosticsForThreads();
	    dialog.setContentPane(viewer);

	    // code added Feb 2014 by Doug Brown
	    int level = FontSizer.getLevel();
	    int w = (int)(600*(1+level*0.2));
	    int h = (int)(300*(1+level*0.2));
	    dialog.setSize(w, h);
	    // end added code
	    
      // center on screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width - dialog.getBounds().width) / 2;
      int y = (dim.height - dialog.getBounds().height) / 2;
      dialog.setLocation(x, y);
      dialog.setTitle("Threads"); //$NON-NLS-1$
	    dialog.setVisible(true);	  
  }

  public static void main(String[] args) {
    JFrame f = new JFrame(); 
    DiagnosticsForThreads viewer = new DiagnosticsForThreads();

    f.setContentPane(viewer);
    f.setSize(500, 300);
    f.setVisible(true);
      
    f.setDefaultCloseOperation(1);

    // Keep the main thread from exiting by blocking
    // on wait() for a notification that never comes.
    Object lock = new Object();
    synchronized (lock) {
      try {
        lock.wait();
      } catch (InterruptedException x) {
      }
    }
  }
}

class ThreadViewerTableModel extends AbstractTableModel {
  private Object dataLock;

  private int rowCount;

  private Object[][] cellData;

  private Object[][] pendingCellData;

  private final int columnCount;

  private final String[] columnName;

  @SuppressWarnings("rawtypes")
private final Class[] columnClass;

  private Thread internalThread;

  private volatile boolean noStopRequested;

  public ThreadViewerTableModel() {
    rowCount = 0;
    cellData = new Object[0][0];

    String[] names = { "Priority", "Alive", "Daemon", "Interrupted","ThreadGroup", "Thread Name" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    columnName = names;

    @SuppressWarnings("rawtypes")
	Class[] classes = { Integer.class, Boolean.class, Boolean.class, Boolean.class, String.class, String.class };
    columnClass = classes;

    columnCount = columnName.length;

    dataLock = new Object();

    noStopRequested = true;
    Runnable r = new Runnable() {
      public void run() {
        try {
          runWork();
        } catch (Exception x) {
          // in case ANY exception slips through
          x.printStackTrace();
        }
      }
    };

    if(org.opensourcephysics.js.JSUtil.isJS) {
    	System.err.println("Warning:  Diagnostics for Threads are not supported in JavaScript.");
    }else {
        internalThread = new Thread(r, "ThreadViewer"); //$NON-NLS-1$
        internalThread.setPriority(Thread.MAX_PRIORITY - 2);
        internalThread.setDaemon(true);
    	internalThread.start();
    }
  }

  private void runWork() {
    Runnable transferPending = new Runnable() {
      public void run() {
        transferPendingCellData();
        fireTableDataChanged();
      }
    };

    while (noStopRequested) {
      try {
        createPendingCellData();
        SwingUtilities.invokeAndWait(transferPending);
        Thread.sleep(5000);
      } catch (InvocationTargetException tx) {
        tx.printStackTrace();
        stopRequest();
      } catch (InterruptedException x) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public void stopRequest() {
    noStopRequested = false;
    internalThread.interrupt();
  }

  public boolean isAlive() {
    return internalThread.isAlive();
  }

  private void createPendingCellData() {
    Thread[] thread = findAllThreads();
    Object[][] cell = new Object[thread.length][columnCount];

    for (int i = 0; i < thread.length; i++) {
      Thread t = thread[i];
      Object[] rowCell = cell[i];

      rowCell[0] = new Integer(t.getPriority());
      rowCell[1] = new Boolean(t.isAlive());
      rowCell[2] = new Boolean(t.isDaemon());
      rowCell[3] = new Boolean(t.isInterrupted());
      rowCell[4] = t.getThreadGroup().getName();
      rowCell[5] = t.getName();
    }

    synchronized (dataLock) {
      pendingCellData = cell;
    }
  }

  private void transferPendingCellData() {
    synchronized (dataLock) {
      cellData = pendingCellData;
      rowCount = cellData.length;
    }
  }

  public int getRowCount() {
    return rowCount;
  }

  public Object getValueAt(int row, int col) {
    return cellData[row][col];
  }

  public int getColumnCount() {
    return columnCount;
  }

  public Class<?> getColumnClass(int columnIdx) {
    return columnClass[columnIdx];
  }

  public String getColumnName(int columnIdx) {
    return columnName[columnIdx];
  }

  public static Thread[] findAllThreads() {
    ThreadGroup group = Thread.currentThread().getThreadGroup();

    ThreadGroup topGroup = group;

    while (group != null) {
      topGroup = group;
      group = group.getParent();
    }

    int estimatedSize = topGroup.activeCount() * 2;
    Thread[] slackList = new Thread[estimatedSize];

    int actualSize = topGroup.enumerate(slackList);

    Thread[] list = new Thread[actualSize];
    System.arraycopy(slackList, 0, list, 0, actualSize);

    return list;
  }
}
