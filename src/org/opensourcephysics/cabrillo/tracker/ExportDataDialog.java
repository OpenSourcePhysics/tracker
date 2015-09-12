/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.*;
import javax.swing.border.*;

import org.opensourcephysics.controls.ControlsRes;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.DataTable;

/**
 * A dialog for exporting videos from a TrackerPanel.
 *
 * @author Douglas Brown
 */
public class ExportDataDialog extends JDialog {
	
	protected static ExportDataDialog dataExporter; // singleton

  // instance fields
  protected TrackerPanel trackerPanel;
  protected JButton saveAsButton, closeButton;
  protected JComponent tablePanel, delimiterPanel, contentPanel, formatPanel;
  protected JComboBox formatDropdown, delimiterDropdown, tableDropdown, contentDropdown;
  protected HashMap<Object, DataTable> tables;
  protected HashMap<DataTable, String> trackNames;
  protected boolean refreshing;
  
  /**
   * Returns the singleton ExportDataDialog for a specified TrackerPanel.
   * 
   * @param panel the TrackerPanel
   * @return the ExportDataDialog
   */
  public static ExportDataDialog getDialog(TrackerPanel panel) {
  	if (dataExporter==null) {
  		dataExporter = new ExportDataDialog(panel);
  	}
  	dataExporter.trackerPanel = panel;
  	dataExporter.refreshGUI();
  	return dataExporter;
  }
  
  /**
   * Constructs a ExportDataDialog.
   *
   * @param panel a TrackerPanel to supply the images
   */
  private ExportDataDialog(TrackerPanel panel) {
    super(JOptionPane.getFrameForComponent(panel), true);
    trackerPanel = panel;
    setResizable(false);
    createGUI();
    refreshGUI();
    // center dialog on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - getBounds().width) / 2;
    int y = (dim.height - getBounds().height) / 2;
    setLocation(x, y);
  }

//_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this dialog.
   */
  private void createGUI() {
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    Box settingsPanel = Box.createVerticalBox();
    contentPane.add(settingsPanel, BorderLayout.CENTER);
    JPanel upper = new JPanel(new GridLayout(1, 2));
    JPanel lower = new JPanel(new GridLayout(1, 2));
    
    // table panel
    tables = new HashMap<Object, DataTable>();
    trackNames = new HashMap<DataTable, String>();
    tablePanel = Box.createVerticalBox();
    tableDropdown = new JComboBox();
  	tablePanel.add(tableDropdown);
    
    // delimiter panel
    delimiterPanel = new JPanel(new GridLayout(0, 1));
    delimiterDropdown = new JComboBox();
    delimiterPanel.add(delimiterDropdown);
    delimiterDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		if (refreshing) return;
    		if (e.getStateChange()==ItemEvent.SELECTED) {
    			Object selected = delimiterDropdown.getSelectedItem();
        	boolean isAdd = selected.equals(
        			TrackerRes.getString("ExportDataDialog.Delimiter.Add")); //$NON-NLS-1$
        	boolean isRemove = selected.equals(
        			TrackerRes.getString("ExportDataDialog.Delimiter.Remove")); //$NON-NLS-1$
        	String delimiter = TrackerIO.getDelimiter();
    			if (isAdd) {
          	Object response = JOptionPane.showInputDialog(ExportDataDialog.this, 
                TrackerRes.getString("TableTrackView.Dialog.CustomDelimiter.Message"),      //$NON-NLS-1$
                TrackerRes.getString("TableTrackView.Dialog.CustomDelimiter.Title"),        //$NON-NLS-1$
                JOptionPane.PLAIN_MESSAGE, null, null, delimiter);
            if (response!=null && !"".equals(response.toString())) { //$NON-NLS-1$ 
            	String s = response.toString();
              TrackerIO.setDelimiter(s);
              TrackerIO.addCustomDelimiter(s);
            }    				
            refreshGUI();
    			}
    			else if (isRemove) {
          	String[] choices = TrackerIO.customDelimiters.values().toArray(new String[1]);
          	Object response = JOptionPane.showInputDialog(ExportDataDialog.this, 
                TrackerRes.getString("TableTrackView.Dialog.RemoveDelimiter.Message"),      //$NON-NLS-1$
                TrackerRes.getString("TableTrackView.Dialog.RemoveDelimiter.Title"),        //$NON-NLS-1$
                JOptionPane.PLAIN_MESSAGE, null, choices, null);
            if (response!=null) {
            	String s = response.toString();
              TrackerIO.removeCustomDelimiter(s);
            }
            refreshGUI();
    			}
    			else {
    				if (TrackerIO.delimiters.keySet().contains(selected))
    					TrackerIO.setDelimiter(TrackerIO.delimiters.get(selected));
    				else if (TrackerIO.customDelimiters.keySet().contains(selected))
    					TrackerIO.setDelimiter(TrackerIO.customDelimiters.get(selected));
    			}
    		}
    	}
    });
    ListCellRenderer renderer = delimiterDropdown.getRenderer();
    delimiterDropdown.setRenderer(new SeparatorRenderer(renderer));
    
    // content panel
    contentPanel = new JPanel(new GridLayout(0, 1));
    contentDropdown = new JComboBox();
  	contentPanel.add(contentDropdown);
    
    // format panel
    formatPanel = new JPanel(new GridLayout(0, 1));
    formatDropdown = new JComboBox();
    formatPanel.add(formatDropdown);
    
    // assemble 
    settingsPanel.add(upper);
    settingsPanel.add(lower);
    upper.add(tablePanel);
    upper.add(contentPanel);
    lower.add(formatPanel);
    lower.add(delimiterPanel);
    
    // buttons
    saveAsButton = new JButton();
    saveAsButton.setForeground(new Color(0, 0, 102));
    saveAsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	JFileChooser chooser = TrackerIO.getChooser();
  	    chooser.setAcceptAllFileFilterUsed(true);
        chooser.setDialogTitle(TrackerRes.getString("ExportDataDialog.Chooser.SaveData.Title")); //$NON-NLS-1$
        chooser.setSelectedFile(new File(""));  //$NON-NLS-1$
        File[] files = TrackerIO.getChooserFiles("save"); //$NON-NLS-1$
        if (files==null || files.length==0)
        	return;
      	DataTable table = tables.get(tableDropdown.getSelectedItem());
      	boolean asFormatted = formatDropdown.getSelectedItem().equals(
      			TrackerRes.getString("TableTrackView.MenuItem.Formatted")); //$NON-NLS-1$
      	boolean allCells = contentDropdown.getSelectedItem().equals(
      			TrackerRes.getString("ExportDataDialog.Content.AllCells")); //$NON-NLS-1$
        String trackName = trackNames.get(table)+XML.NEW_LINE;
        trackName = trackName.replace(' ', '_');
      	if (allCells) {
      		// get current selection state
      		int[] selectedRows = table.getSelectedRows();
      		int[] selectedCols = table.getSelectedColumns();
      		// select all
          table.selectAll();
          // get data and write to output file
          StringBuffer buf = TrackerIO.getData(table, asFormatted);
          write(files[0], trackName+buf.toString());
          // restore previous selection state
          table.clearSelection();
          for (int row: selectedRows)
          	table.addRowSelectionInterval(row, row);
          for (int col: selectedCols)
          	table.addColumnSelectionInterval(col, col);
      	}
      	else {
          // get data and write to output file
      		StringBuffer buf = TrackerIO.getData(table, asFormatted);
          write(files[0], trackName+buf.toString());
      	}
      }
    });
    closeButton = new JButton();
    closeButton.setForeground(new Color(0, 0, 102));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    // buttonbar
    JPanel buttonbar = new JPanel();
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(saveAsButton);
    buttonbar.add(closeButton);
  }
  
  /**
   * Refreshes the visible components of this dialog.
   */
  private void refreshGUI() {
  	refreshing = true;
  	// refresh strings
  	String title = TrackerRes.getString("ExportDataDialog.Title"); //$NON-NLS-1$
  	setTitle(title);
    title = TrackerRes.getString("ExportDataDialog.Subtitle.Table"); //$NON-NLS-1$
    Border space = BorderFactory.createEmptyBorder(0, 4, 6, 4);
    Border titled = BorderFactory.createTitledBorder(title);
    tablePanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
    title = TrackerRes.getString("ExportDataDialog.Subtitle.Delimiter"); //$NON-NLS-1$
    titled = BorderFactory.createTitledBorder(title);
    delimiterPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
    title = TrackerRes.getString("ExportDataDialog.Subtitle.Content"); //$NON-NLS-1$
    titled = BorderFactory.createTitledBorder(title);
    contentPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
    title = TrackerRes.getString("ExportDataDialog.Subtitle.Format"); //$NON-NLS-1$
    titled = BorderFactory.createTitledBorder(title);
    formatPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
    saveAsButton.setText(TrackerRes.getString("ExportVideoDialog.Button.SaveAs")); //$NON-NLS-1$
    closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
    
    // refresh dropdowns
    // delimiters
    Object selectedItem = null;
    String delim = TrackerIO.getDelimiter();
    delimiterDropdown.removeAllItems();
    // standard delimiters
    for (String key: TrackerIO.delimiters.keySet()) {
    	delimiterDropdown.addItem(key);
    	if (delim.equals(TrackerIO.delimiters.get(key)))
    		selectedItem = key;
    }
    // custom delimiters
  	boolean hasCustom = !TrackerIO.customDelimiters.isEmpty();
    if (hasCustom) {
	    delimiterDropdown.addItem(new JSeparator(JSeparator.HORIZONTAL));
	    for (String key: TrackerIO.customDelimiters.keySet()) {
	    	delimiterDropdown.addItem(key);
	    	if (delim.equals(TrackerIO.customDelimiters.get(key)))
	    		selectedItem = key;
	    }
    }
    // add and remove delimiter items
    delimiterDropdown.addItem(new JSeparator(JSeparator.HORIZONTAL));
    String s = TrackerRes.getString("ExportDataDialog.Delimiter.Add"); //$NON-NLS-1$
    delimiterDropdown.addItem(s);
    if (hasCustom) {
	    s = TrackerRes.getString("ExportDataDialog.Delimiter.Remove"); //$NON-NLS-1$
	    delimiterDropdown.addItem(s);
    }
    delimiterDropdown.setSelectedItem(selectedItem);
    
  	// tables
    selectedItem = tableDropdown.getSelectedItem();
    tableDropdown.removeAllItems();
    Container[] c = trackerPanel.getTFrame().getViews(trackerPanel);
    boolean hasSelection = false;
    for (int i = 0; i < c.length; i++) {
      if (trackerPanel.getTFrame().isViewOpen(i, trackerPanel)) {
        String number = " ("+(i+1)+")"; //$NON-NLS-1$ //$NON-NLS-2$
      	if (c[i] instanceof TViewChooser) {
          TViewChooser chooser = (TViewChooser)c[i];
          TView tview = chooser.getSelectedView();
          if (tview instanceof TableTView) {
	          TableTView tableView = (TableTView)tview;
	          TTrack track = tableView.getSelectedTrack();
	          if (track!=null) {
		          s = track.getName() + number;
	          	TableTrackView trackView = (TableTrackView)tableView.getTrackView(track);
	          	trackNames.put(trackView.dataTable, track.getName());
		          tables.put(s, trackView.dataTable);
		        	tableDropdown.addItem(s);
		          int[] selectedRows = trackView.dataTable.getSelectedRows();
		          if (selectedRows.length > 0) {
		          	hasSelection = true;
		          }
	          }
          }
        }
      }
    }
  	if (selectedItem!=null)
  		tableDropdown.setSelectedItem(selectedItem);
    
    // formats
    selectedItem = formatDropdown.getSelectedItem();
    formatDropdown.removeAllItems();
  	formatDropdown.addItem(TrackerRes.getString("TableTrackView.MenuItem.Unformatted")); //$NON-NLS-1$
  	formatDropdown.addItem(TrackerRes.getString("TableTrackView.MenuItem.Formatted")); //$NON-NLS-1$
  	if (selectedItem!=null)
  		formatDropdown.setSelectedItem(selectedItem);

  	// content
    contentDropdown.removeAllItems();
    contentDropdown.addItem(TrackerRes.getString("ExportDataDialog.Content.AllCells")); //$NON-NLS-1$
    if (hasSelection) {
    	s = TrackerRes.getString("ExportDataDialog.Content.SelectedCells"); //$NON-NLS-1$
    	contentDropdown.insertItemAt(s, 0);
    	contentDropdown.setSelectedItem(s);
    }
  	
		pack();
		refreshing = false;
  }
  
  /**
   * Writes a string to a file.
   *
   * @param file the file
   * @param content the string to write
   * @return the path of the saved file or null if failed
   */
  public String write(File file, String content) {
    if(file.exists()&&!file.canWrite()) {
  		JOptionPane.showMessageDialog(trackerPanel, 
  				ControlsRes.getString("Dialog.ReadOnly.Message"),  //$NON-NLS-1$
  				ControlsRes.getString("Dialog.ReadOnly.Title"),  //$NON-NLS-1$
  				JOptionPane.PLAIN_MESSAGE);
      return null;
    }
    try {
      FileOutputStream stream = new FileOutputStream(file);
      java.nio.charset.Charset charset = java.nio.charset.Charset.forName("UTF-8"); //$NON-NLS-1$
      Writer out = new BufferedWriter(new OutputStreamWriter(stream, charset));
      out.write(content);
      out.flush();
      out.close();
      if(file.exists()) {
        return XML.getAbsolutePath(file);
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Custom renderer to separator in dropdown list
   */
  class SeparatorRenderer extends JLabel implements ListCellRenderer {
  	
  	ListCellRenderer renderer;

  	SeparatorRenderer(ListCellRenderer renderer) {
      this.renderer = renderer;
    }
  	
    public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus) {
      if (value instanceof JSeparator)
        return (JSeparator)value;
      return renderer.getListCellRendererComponent(
      		list, value, index, isSelected, cellHasFocus);
    }
  }
}
