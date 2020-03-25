/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display2d.GridData;

import javajs.async.AsyncFileChooser;

// modified by W. Christian Jan 28, 2005

/**
 * An export tool which launches a Save dialog
 *
 * @author Kipton Barros
 * @version 1.0
 */
public class ExportTool implements Tool, PropertyChangeListener {
  /*
  * TOOL is the single instance of ExportTool registered with the OSP Toolbox.
  */
  static ExportTool TOOL;
  JFileChooser fc;
  static String exportExtension = "txt"; //$NON-NLS-1$
  static Hashtable<String, ExportFormat> formats;
  JCheckBox[] checkBoxes;
  String exportName = "default";         //$NON-NLS-1$

  /**
   * Creates a new export tool.  Doesn't get activated until setXML() is called.
   */
  public ExportTool() {
    createFileChooser();
    fc.addPropertyChangeListener(this);
  }

  void createFileChooser() {
    formats = new Hashtable<String, ExportFormat>();
    registerFormat(new ExportGnuplotFormat());
    registerFormat(new ExportXMLFormat());
    // Set the "filesOfTypeLabelText" to "File Format:"
    Object oldFilesOfTypeLabelText = UIManager.put("FileChooser.filesOfTypeLabelText", //$NON-NLS-1$
      ToolsRes.getString("ExportTool.FileChooser.Label.FileFormat"));                  //$NON-NLS-1$
    // Create a new FileChooser
    fc = new JFileChooser(OSPRuntime.chooserDir);
    // Reset the "filesOfTypeLabelText" to previous value
    UIManager.put("FileChooser.filesOfTypeLabelText", oldFilesOfTypeLabelText);          //$NON-NLS-1$
    fc.setDialogType(JFileChooser.SAVE_DIALOG);
    fc.setDialogTitle(ToolsRes.getString("ExportTool.FileChooser.Title"));               //$NON-NLS-1$
    fc.setApproveButtonText(ToolsRes.getString("ExportTool.FileChooser.Button.Export")); // Set export formats //$NON-NLS-1$
    setChooserFormats();
  }
  
  /*
  void createFileChooser() {
	    formats = new Hashtable<String, ExportFormat>();
	    registerFormat(new ExportGnuplotFormat());
	    registerFormat(new ExportXMLFormat());
	    // Set the "filesOfTypeLabelText" to "File Format:"
	    Object oldFilesOfTypeLabelText = UIManager.put("FileChooser.filesOfTypeLabelText", //$NON-NLS-1$
	      ToolsRes.getString("ExportTool.FileChooser.Label.FileFormat"));                  //$NON-NLS-1$
	    // Create a new FileChooser
	    //fc = new JFileChooser(OSPRuntime.chooserDir)
	    fc = OSPRuntime.getChooser();
	     if(fc==null) {
	        return;
	     }
	     String oldTitle = fc.getDialogTitle();
	     fc.setDialogTitle("Export Data");
	     fc.showOpenDialog(null, new Runnable() {
	    	 // OK
			@Override
			public void run() {
			     org.opensourcephysics.display.OSPRuntime.chooserDir = fc.getCurrentDirectory().toString();
			     // It is critical to pass the actual file along, as it has the bytes already.
			     // XMLControlElement xml = new XMLControlElement(fc.getSelectedFile());
			     //xml.loadObject(GROrbitsApp.this); // load the data
			     fc.setDialogTitle(oldTitle);
			}
	    	 
	     }, new Runnable() {
	    	 // cancel
			@Override
			public void run() {
			     fc.setDialogTitle(oldTitle);
			}
	    	 
	     });
	    
	
	    setChooserFormats();
	  }*/

  public void propertyChange(PropertyChangeEvent evt) {
    FileFilter filter = fc.getFileFilter();
    if(filter==null) {
      return;
    }
    ExportFormat ef = formats.get(filter.getDescription());
    if((ef==null)||exportExtension.equals(ef.extension())) {
      return;
    }
    exportExtension = ef.extension();
    // bug in Java?  This doesn't seem to work.
    fc.setSelectedFile(new File(exportName+'.'+exportExtension));
  }

  /*
  * Builds the accessory pane for file chooser
  */
  void buildAccessory(List<Object> data) {
    checkBoxes = new JCheckBox[data.size()];
    JPanel checkPanel = new JPanel(new GridLayout(0, 1));
    // Create the list of data objects and put it in a scroll pane.
    for(int i = 0; i<data.size(); i++) {
      String s = ToolsRes.getString("ExportTool.FileChooser.DataType.Unknown")+i; //$NON-NLS-1$
      Color c = Color.BLACK;
      Object o = data.get(i);
      if(o instanceof Dataset) {
        Dataset d = (Dataset) o;
        // BUG: come up with better name
        s = ToolsRes.getString("ExportTool.FileChooser.DataType.Dataset")+i;      //$NON-NLS-1$
        c = d.getFillColor();
      } else if(o instanceof GridData) {
        s = ToolsRes.getString("ExportTool.FileChooser.DataType.GridData")+i;     //$NON-NLS-1$
      }
      checkBoxes[i] = new JCheckBox(s);
      checkBoxes[i].setSelected(true);
      checkBoxes[i].setForeground(c);
      checkBoxes[i].setBackground(Color.WHITE);
      checkPanel.add(checkBoxes[i]);
    }
    JScrollPane scrollPane = new JScrollPane(checkPanel);
    scrollPane.getViewport().setBackground(Color.WHITE);
    JPanel p = new JPanel(new BorderLayout());
    if(data.size()==0) {
      p.add(new JLabel(ToolsRes.getString("ExportTool.FileChooser.Heading.NoData")),         //$NON-NLS-1$
        BorderLayout.NORTH);
    } else {
      p.add(new JLabel(ToolsRes.getString("ExportTool.FileChooser.Heading.ExportableData")), //$NON-NLS-1$
        BorderLayout.NORTH);
    }
    p.add(scrollPane, BorderLayout.CENTER);
    p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    fc.setAccessory(p); // always show the exportable data
  }

  /*
  * Set dialog formats in file chooser
  */
  void setChooserFormats() {
    fc.resetChoosableFileFilters();
    fc.setAcceptAllFileFilterUsed(false);
    for(Enumeration<String> e = formats.keys(); e.hasMoreElements(); ) {
      final String desc = e.nextElement();
      fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
        public boolean accept(File f) {
          return f!=null;
        }
        public String getDescription() {
          return desc;
        }

      });
    }
  }

  /*
  * Gets all data objects from XML
  */
  List<Object> getDataObjects(XMLControlElement control) {
    List<Object> ret = new ArrayList<Object>();
    ret.addAll(control.getObjects(Dataset.class));
    ret.addAll(control.getObjects(GridData.class));
    return ret;
  }

  /*
  * Filters out data objects to export based on the checkBoxes
  */
  List<Object> filterDataObjects(List<Object> data) {
    Vector<Object> ret = new Vector<Object>();
    for(int i = 0; i<data.size(); i++) {
      if(checkBoxes[i].isSelected()) {
        ret.add(data.get(i));
      }
    }
    return ret;
  }

  /**
   * Register a new export format.
   */
  static public void registerFormat(ExportFormat format) {
    formats.put(format.description(), format);
  }

  /*
  * Displays the export dialog with a given XML file.
  */
  public void send(Job job, Tool replyTo) throws RemoteException {
    XMLControlElement control = new XMLControlElement();
    try {
      control.readXML(job.getXML());
    } catch(RemoteException ex) {}
    OSPLog.finest(control.toXML());
    // Load all data objects into 'data'
    List<Object> data = getDataObjects(control);
    // Set export dialog to list appropriate data objects
    buildAccessory(data);
    // Set selected file in home directory
    fc.setSelectedFile(new File(exportName+'.'+exportExtension));
    // Show the export dialog, and wait for user input
    int returnVal = fc.showSaveDialog(null);
    // If user clicked export, write the file
    if(returnVal==JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      // Check to see if file already exists
      if(file.exists()) {
        int selected = JOptionPane.showConfirmDialog(null, ToolsRes.getString("Tool.Dialog.ReplaceFile.Message")+" "+file.getName()+"?", //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
          ToolsRes.getString("Tool.Dialog.ReplaceFile.Title"), //$NON-NLS-1$
            JOptionPane.YES_NO_CANCEL_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return;
        }
      }
      String description = fc.getFileFilter().getDescription();
      formats.get(description).export(file, filterDataObjects(data));
      if(file.getName().endsWith(exportExtension)) {
        exportName = file.getName().substring(0, file.getName().length()-1-exportExtension.length());
        // System.out.println("new name="+exportName);
      }
    }
  }

  /**
   * Gets the shared Tool.
   *
   * @return the shared ExportTool
   */
  public static ExportTool getTool() {
    if(TOOL==null) {
      TOOL = new ExportTool();
      Toolbox.addTool("ExportTool", TOOL); //$NON-NLS-1$
    }
    return TOOL;
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
