package org.opensourcephysics.tools;

import java.awt.Frame;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JTable;

import org.opensourcephysics.display.DataPanel;

/**
 * Displays system properties.
 * 
 * @author Wolfgang Christian
 *
 */
public class DiagnosticsForSystem extends DataPanel{

  public static void aboutSystem(Frame owner) {
		    JDialog dialog = new JDialog(owner,"System Properties");  //$NON-NLS-1$
		    DiagnosticsForSystem viewer = new DiagnosticsForSystem();
		    dialog.setContentPane(viewer);
		    dialog.setSize(500, 300);
		    dialog.setVisible(true);	  
  }
  
  public  DiagnosticsForSystem(){
	setColumnNames(new String[]{"#","property","value"});   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    Enumeration<?> propEnum = System.getProperties().propertyNames();
    while(propEnum.hasMoreElements()) {
      String next = (String) propEnum.nextElement();
      String val = System.getProperty(next);
      appendRow(new String[]{next,val}) ; 
    }
	refreshTable(); // make sure the table shows the current values
	setRowNumberVisible(false);
	setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
  }
  
	// test program
	public static void main(String[] args) {
		aboutSystem(null);
  }
	
}
