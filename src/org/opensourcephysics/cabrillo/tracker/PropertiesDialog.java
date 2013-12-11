/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2013  Douglas Brown
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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.tools.ResourceLoader;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * A dialog for viewing and setting document properties and metadata.
 *
 * @author Douglas Brown
 */
public class PropertiesDialog extends JDialog {
	
  final static Color DARK_RED = new Color(220, 0, 0);
  final static Color MEDIUM_RED = new Color(255, 120, 140);
  final static Color LIGHT_RED = new Color(255, 180, 200);
  
  protected TrackerPanel trackerPanel;
	protected JButton okButton, cancelButton;
  protected JTextField authorField, contactField;
  protected JLabel authorLabel, contactLabel;
  protected JTabbedPane tabbedPane;
  protected JPanel metaPanel, videoPanel, trkPanel;
  protected JTable videoTable, trkTable;
  protected String[] vidProps = new String[6], vidValues = new String[6];
  protected ArrayList<String> trkProps = new ArrayList<String>(), 
  		trkValues = new ArrayList<String>();
  protected boolean hasVid;
  
  /**
   * Constructor.
   *
   * @param panel the tracker panel
   */
  public PropertiesDialog(TrackerPanel panel) {
    super(panel.getTFrame(), true);
    trackerPanel = panel;
    createGUI();
    pack();
    okButton.requestFocusInWindow();
  }

//_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {
  	setTitle(TrackerRes.getString("PropertiesDialog.Title")); //$NON-NLS-1$
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
  	tabbedPane = new JTabbedPane();
    contentPane.add(tabbedPane, BorderLayout.CENTER);

    // Tracker File tab    
    trkPanel = new JPanel(new BorderLayout());
    tabbedPane.addTab(TrackerRes.getString("PropertiesDialog.Tab.TrackerFile"), trkPanel); //$NON-NLS-1$
    trkProps.add(TrackerRes.getString("TActions.Dialog.AboutVideo.Name")); //$NON-NLS-1$
  	trkProps.add(TrackerRes.getString("TActions.Dialog.AboutVideo.Path")); //$NON-NLS-1$
  	String path = XML.forwardSlash(trackerPanel.openedFromPath);
  	path = ResourceLoader.getNonURIPath(path);
  	String name = XML.getName(path);
  	trkValues.add(name);
  	trkValues.add(path);
    TableModel model = new TRKTableModel();
    trkTable = new JTable(model);
    trkTable.setBackground(trkPanel.getBackground());
    trkTable.setDefaultRenderer(String.class, new PropertyCellRenderer());
    trkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    trkTable.setColumnSelectionAllowed(true);
    trkTable.getColumnModel().getColumn(0).setPreferredWidth(50);
    trkTable.getColumnModel().getColumn(1).setPreferredWidth(250);
    trkPanel.add(trkTable.getTableHeader(), BorderLayout.NORTH);
    trkPanel.add(trkTable, BorderLayout.CENTER);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(20000);
    ToolTipManager.sharedInstance().registerComponent(trkTable);
    JButton button = new JButton(TrackerRes.getString("PropertiesDialog.Button.CopyFilePath")); //$NON-NLS-1$
    button.setForeground(new Color(0, 0, 102));
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	trkTable.setRowSelectionInterval(1, 1);
      	trkTable.setColumnSelectionInterval(1, 1);
      	String s = trkTable.getValueAt(1, 1).toString();
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(s);
        clipboard.setContents(stringSelection, stringSelection);
      }
    });
    button.setEnabled(trackerPanel.openedFromPath!=null);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(button);	    
    trkPanel.add(buttonPanel, BorderLayout.SOUTH);	    
    
    // Video tab
  	Video video = trackerPanel.getVideo();
  	hasVid = video!=null;
  	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
  	if (hasVid || clip.getVideoPath()!=null) {
	    videoPanel = new JPanel(new BorderLayout());
	    tabbedPane.addTab(TrackerRes.getString("TMenuBar.Menu.Video"), videoPanel); //$NON-NLS-1$
	    NumberFormat format = NumberFormat.getNumberInstance();
	    format.setMinimumIntegerDigits(1);
	    format.setMinimumFractionDigits(1);
	    format.setMaximumFractionDigits(1);
	    name = hasVid? XML.getName((String)video.getProperty("name")): null; //$NON-NLS-1$
	    path = clip.getVideoPath();
	    path = XML.forwardSlash(path);
	    path = ResourceLoader.getNonURIPath(path);
	    String type = null;
	    String size = null;
	    String length = null;
	    String fps = null;
	    if (hasVid) {
		    VideoType videoType = (VideoType)video.getProperty("video_type"); //$NON-NLS-1$
		    type = videoType==null? 
		    		video.getClass().getSimpleName(): 
		    		videoType.getDescription();
		    // eliminate extension list and replace with video engine if xuggle or QT
		    int n = type.lastIndexOf("("); //$NON-NLS-1$
		    if (n>-1) {
		    	type = type.substring(0, n);
		    	if (video.getClass().getSimpleName().contains(VideoIO.ENGINE_XUGGLE)) {
		    		type += "(Xuggle)"; //$NON-NLS-1$
		    	}
		    	else if (video.getClass().getSimpleName().contains(VideoIO.ENGINE_QUICKTIME)) {
		    		type += "(QuickTime)"; //$NON-NLS-1$
		    	}
		    }
		    size = video.getImage().getWidth()+" x "+video.getImage().getHeight(); //$NON-NLS-1$
		    length = video.getFrameCount()+" "; //$NON-NLS-1$
		    length += TrackerRes.getString("TActions.Dialog.AboutVideo.Frames"); //$NON-NLS-1$
//		    double duration = video.getDuration()/1000.0;
//		    duration = video.getFrameCount()<=1? 0: duration;
//		    length += ", "+format.format(duration)+" "; //$NON-NLS-1$ //$NON-NLS-2$
//		    length += TrackerRes.getString("TActions.Dialog.AboutVideo.Seconds"); //$NON-NLS-1$
		    double dt = trackerPanel.getPlayer().getClipControl().getMeanFrameDuration();
		    double frameRate = video.getFrameCount()<=1? 0: 1000/dt;
		    fps = frameRate==0? "": format.format(frameRate)+" ";	 //$NON-NLS-1$ //$NON-NLS-2$
		    if (frameRate>0)
		    	fps += TrackerRes.getString("TActions.Dialog.AboutVideo.FramesPerSecond"); //$NON-NLS-1$
		    ArrayList<Integer> badFrames = TrackerIO.findBadVideoFrames(trackerPanel, TrackerIO.defaultBadFrameTolerance, 
      			false, false, false); // don't show dialog, just get bad frames
		    if (!badFrames.isEmpty()) {
		    	fps += " ("+TrackerRes.getString("TActions.Dialog.AboutVideo.FramesPerSecond.NotConstant")+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    }
	    }
	    vidProps[0] = TrackerRes.getString("TActions.Dialog.AboutVideo.Name"); //$NON-NLS-1$
	    vidProps[1] = TrackerRes.getString("TActions.Dialog.AboutVideo.Path"); //$NON-NLS-1$
	    vidProps[2] = TrackerRes.getString("TActions.Dialog.AboutVideo.Type"); //$NON-NLS-1$
	    vidProps[3] = TrackerRes.getString("TActions.Dialog.AboutVideo.Size"); //$NON-NLS-1$
	    vidProps[4] = TrackerRes.getString("TActions.Dialog.AboutVideo.Length"); //$NON-NLS-1$
	    vidProps[5] = TrackerRes.getString("TActions.Dialog.AboutVideo.FrameRate"); //$NON-NLS-1$
	    vidValues[0] = name;
	    vidValues[1] = path;
	    vidValues[2] = type;
	    vidValues[3] = size;
	    vidValues[4] = length;
	    vidValues[5] = fps;
	    model = new VideoTableModel();
	    videoTable = new JTable(model);
	    videoTable.setBackground(videoPanel.getBackground());
	    videoTable.setDefaultRenderer(String.class, new PropertyCellRenderer());
	    videoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    videoTable.setColumnSelectionAllowed(true);
	    videoTable.getColumnModel().getColumn(0).setPreferredWidth(50);
	    videoTable.getColumnModel().getColumn(1).setPreferredWidth(250);
	    videoPanel.add(videoTable.getTableHeader(), BorderLayout.NORTH);
	    videoPanel.add(videoTable, BorderLayout.CENTER);
	    ToolTipManager.sharedInstance().registerComponent(videoTable);
	    button = new JButton(TrackerRes.getString("PropertiesDialog.Button.CopyVideoPath")); //$NON-NLS-1$
	    button.setForeground(new Color(0, 0, 102));
	    button.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	      	videoTable.setRowSelectionInterval(1, 1);
	      	videoTable.setColumnSelectionInterval(1, 1);
	      	String s = videoTable.getValueAt(1, 1).toString();
	        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	        StringSelection stringSelection = new StringSelection(s);
	        clipboard.setContents(stringSelection, stringSelection);
	      }
	    });
	    button.setEnabled(!path.equals("")); //$NON-NLS-1$
	    buttonPanel = new JPanel();
	    buttonPanel.add(button);	    
	    videoPanel.add(buttonPanel, BorderLayout.SOUTH);	    
  	}    
    
    // Metadata tab
    metaPanel = new JPanel(new BorderLayout());
    tabbedPane.addTab(TrackerRes.getString("PropertiesDialog.Tab.Metadata"), metaPanel);     //$NON-NLS-1$
    authorLabel = new JLabel(TrackerRes.getString("PropertiesDialog.Label.Author")); //$NON-NLS-1$
    authorField = new JTextField(30);
    authorField.setText(trackerPanel.author);
    JToolBar authorbar = new JToolBar();
    authorbar.setBorder(BorderFactory.createEmptyBorder(6, 4, 2, 4));
    authorbar.setFloatable(false);
    authorbar.setOpaque(false);
    authorbar.add(authorLabel);
    authorbar.add(authorField);
    
    contactLabel = new JLabel(TrackerRes.getString("PropertiesDialog.Label.Contact")); //$NON-NLS-1$
	  contactField = new JTextField(30);
	  contactField.setText(trackerPanel.contact);
	  JToolBar contactbar = new JToolBar();
	  contactbar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
	  contactbar.setFloatable(false);
	  contactbar.setOpaque(false);
	  contactbar.add(contactLabel);
	  contactbar.add(contactField);
  
    setLabelSizes();
	  Box box = Box.createVerticalBox();
    box.add(authorbar);
    box.add(contactbar);
    metaPanel.add(box, BorderLayout.NORTH);
    
    // create OK button
    okButton = new JButton(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    okButton.setForeground(new Color(0, 0, 102));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String s = authorField.getText();
      	trackerPanel.author = "".equals(s)? null: s; //$NON-NLS-1$
      	s = contactField.getText();
      	trackerPanel.contact = "".equals(s)? null: s; //$NON-NLS-1$
        setVisible(false);
      }
    });
    // create cancel button
    cancelButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    cancelButton.setForeground(new Color(0, 0, 102));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    // create buttonbar at bottom
    JPanel buttonbar = new JPanel();
    buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(okButton);
    buttonbar.add(cancelButton);
  }
  
  private void setLabelSizes() {
    ArrayList<JLabel> labels = new ArrayList<JLabel>();
    labels.add(authorLabel);
    labels.add(contactLabel);
    FontRenderContext frc = new FontRenderContext(null, false, false);                                        
    Font font = authorLabel.getFont();
    int w = 0;
    for(JLabel next: labels) {
      Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
      w = Math.max(w, (int) rect.getWidth()+1);
    }
    Dimension labelSize = new Dimension(w, 20);
    for(JLabel next: labels) {
      next.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 2));
      next.setPreferredSize(labelSize);
      next.setHorizontalAlignment(SwingConstants.TRAILING);
    }
  }

  /**
   * A class to provide model data for the video table.
   */
  class VideoTableModel extends AbstractTableModel {
    public int getRowCount() {return vidProps.length;}

    public int getColumnCount() {return 2;}

    public Object getValueAt(int row, int col) {return col==0? vidProps[row]: vidValues[row];}
    
    public String getColumnName(int col) {
    	return col==0? TrackerRes.getString("PropertiesDialog.Header.Property"): //$NON-NLS-1$
    		TrackerRes.getString("PropertiesDialog.Header.Value"); //$NON-NLS-1$
    }
    
    public Class<?> getColumnClass(int col) {return String.class;}
  }
  
  /**
   * A class to provide model data for the trk table.
   */
  class TRKTableModel extends AbstractTableModel {
    public int getRowCount() {return trkProps.size();}

    public int getColumnCount() {return 2;}

    public Object getValueAt(int row, int col) {
    	return col==0? trkProps.get(row): trkValues.get(row);
    }
    
    public String getColumnName(int col) {
    	return col==0? TrackerRes.getString("PropertiesDialog.Header.Property"): //$NON-NLS-1$
    		TrackerRes.getString("PropertiesDialog.Header.Value"); //$NON-NLS-1$
    }
    
    public Class<?> getColumnClass(int col) {return String.class;}
  }
  
  /**
   * A class to render table cells.
   */
  class PropertyCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object val,
        boolean selected, boolean hasFocus, int row, int col) {
			setToolTipText(row==1 && col==1 && val!=null? val.toString(): null);
			setBackground(Color.white);
			Component c = super.getTableCellRendererComponent(table, val, selected, hasFocus, row, col);
			boolean red = col==1 && table == videoTable && !hasVid && val!=null;
			setForeground(red? DARK_RED: Color.black);
			if (red) {
				setBackground(selected? MEDIUM_RED: LIGHT_RED);
			}
			return c;
    }
  }
 
}
