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

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FontSizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * This displays and sets point attachments.
 *
 * @author Douglas Brown
 */
public class AttachmentDialog extends JDialog
    implements PropertyChangeListener {
	

  // instance fields
  protected TTrack measuringTool;
  protected int endPointCount;
  protected TTrack[] attachedMass;
  protected String[] endPointName;
  
  protected TrackerPanel trackerPanel;
  protected boolean isVisible;
  protected JButton closeButton, helpButton;
  protected ArrayList<? extends TTrack> masses;
  protected PointMass dummyMass;
  protected JTable table;
	protected int cellheight = 28; // depends on font level
  protected JComboBox rendererDropdown, editorDropdown, measuringToolDropdown;
  protected Icon dummyIcon = new ShapeIcon(null, 21, 16);
  protected JScrollPane scrollPane;
  protected AttachmentCellRenderer attachmentCellRenderer = new AttachmentCellRenderer();
  protected TTrackRenderer trackRenderer = new TTrackRenderer();

  
  /**
   * Constructs an AttachmentControl.
   *
   * @param track the measuring tool
   */
  public AttachmentDialog(TTrack track) {
    super(JOptionPane.getFrameForComponent(track.trackerPanel), false);
    createGUI();
    setMeasuringTool(track);
		refreshDropdowns();
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    TFrame frame = trackerPanel.getTFrame();
    frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
    refreshDisplay();
  }

  /**
   * Responds to property change events. This listens for the
   * following events: "tab" from TFrame.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("tab")) { //$NON-NLS-1$
      if (trackerPanel != null && e.getNewValue() == trackerPanel) {
        setVisible(isVisible);
      }
      else {
        boolean vis = isVisible;
        setVisible(false);
        isVisible = vis;
      }
    }
    else if (e.getPropertyName().equals("track")) { //$NON-NLS-1$
    	TTrack deleted = (TTrack)e.getOldValue();
    	if (deleted!=null) {
    		deleted.removePropertyChangeListener("step", this); //$NON-NLS-1$
    		deleted.removePropertyChangeListener("steps", this); //$NON-NLS-1$
    		deleted.removePropertyChangeListener("name", this); //$NON-NLS-1$
    		deleted.removePropertyChangeListener("color", this); //$NON-NLS-1$
    		deleted.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
	     	for (int i = 0; i < endPointCount; i++) {
			  	if (deleted==attachedMass[i] || deleted==measuringTool) {
			  		attachedMass[i] = null;	  		
			  	}
	    	}
	   		refreshMeasuringTool();
    	}
  		refreshDropdowns();
    	refreshDisplay();
    }
    else if (e.getPropertyName().equals("step") //$NON-NLS-1$
    		|| e.getPropertyName().equals("steps")) { //$NON-NLS-1$
    	refreshMeasuringTool();
    }
    else refreshDisplay();
  }

  /**
   * Overrides JDialog setVisible method.
   *
   * @param vis true to show this inspector
   */
  public void setVisible(boolean vis) {
    super.setVisible(vis);
    isVisible = vis;
  }

  /**
   * Disposes of this inspector.
   */
  public void dispose() {
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
      for (TTrack p: masses) {
        p.removePropertyChangeListener("name", this); //$NON-NLS-1$
        p.removePropertyChangeListener("color", this); //$NON-NLS-1$
        p.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      }
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
      }
    }
    super.dispose();
  }

//_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {
  	setResizable(false);
    // create GUI components
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    
    JPanel north = new JPanel();
    north.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    measuringToolDropdown = new JComboBox();
    measuringToolDropdown.setRenderer(trackRenderer);
    measuringToolDropdown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TTrack tool = (TTrack)measuringToolDropdown.getSelectedItem();
      	if (tool==measuringTool) return;
        setMeasuringTool(tool);
      }
    });
    north.add(measuringToolDropdown);
    contentPane.add(north, BorderLayout.NORTH);
        
    dummyMass = new PointMass();
    		
    rendererDropdown = new JComboBox(new AttachmentComboBoxModel());
    rendererDropdown.setRenderer(trackRenderer);
    editorDropdown = new JComboBox(new AttachmentComboBoxModel());
    editorDropdown.setRenderer(trackRenderer);

    table = new JTable(new AttachmentTableModel()) {
    	@Override
    	public void setFont(Font font) {
    		super.setFont(font);
	      cellheight = font.getSize()+16;
    		setRowHeight(cellheight);
    		int w = (int)(60*(1+FontSizer.getLevel()*0.3));
	      getColumnModel().getColumn(0).setPreferredWidth(w);
	      getColumnModel().getColumn(1).setPreferredWidth(2*w);
    		getTableHeader().setPreferredSize(new Dimension(w, cellheight));
      }
    };
    attachmentCellRenderer = new AttachmentCellRenderer();
    table.setDefaultRenderer(PointMass.class, attachmentCellRenderer);
    table.setDefaultRenderer(String.class, attachmentCellRenderer);
    table.setRowHeight(cellheight);
    
    TableCellEditor editor = new AttachmentCellEditor();
    table.getColumnModel().getColumn(1).setCellEditor(editor);
    scrollPane = new JScrollPane(table) {
    	public Dimension getPreferredSize() {
    		Dimension dim = super.getPreferredSize();
    		dim.height = 3*cellheight+8;
    		dim.width= table.getPreferredSize().width;
    		return dim;
    	}
    };
    JPanel center = new JPanel(new GridLayout(1,1));
    contentPane.add(center, BorderLayout.CENTER);
    center.add(scrollPane);
    center.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
   
    // create buttons and buttonbar
    helpButton = new JButton();
    helpButton.setForeground(new Color(0, 0, 102));
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String keyword = measuringTool instanceof Protractor? "protractor": "tape"; //$NON-NLS-1$ //$NON-NLS-2$
        trackerPanel.getTFrame().showHelp(keyword+"#attach", 0); //$NON-NLS-1$
      }
    });
    closeButton = new JButton();
    closeButton.setForeground(new Color(0, 0, 102));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    JPanel buttonbar = new JPanel();
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(helpButton);
    buttonbar.add(closeButton);
  }
  
  protected void setMeasuringTool(TTrack tool) {
    measuringTool = tool;
    if (tool!=null) {
	    trackerPanel = measuringTool.trackerPanel;
	    endPointCount = tool instanceof TapeMeasure? 2: 3;
	    endPointName = new String[endPointCount];
	    attachedMass = measuringTool.attachments==null? new TTrack[endPointCount]: measuringTool.attachments;
    }
    else {
	    endPointCount = 0;
	    endPointName = new String[0];
	    attachedMass = new TTrack[0];    	
    }
    DefaultTableModel dm = (DefaultTableModel)table.getModel();
    dm.fireTableDataChanged();
    refreshDisplay();
  }

  /**
   * Updates the system to reflect the current particle selection.
   */
  private void refreshMeasuringTool() {
  	measuringTool.attachments = attachedMass;
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
  	for (int i = 0; i < endPointCount; i++) {
	  	if (attachedMass[i]!=null) {
	  		if (measuringTool instanceof TapeMeasure)
	  			((TapeMeasure)measuringTool).setFixedPosition(false);
	  		else if (measuringTool instanceof Protractor)
	  			((Protractor)measuringTool).setFixed(false);
	    	for (int n = clip.getStartFrameNumber(); n<=clip.getEndFrameNumber(); n++) {
	    		Step step = attachedMass[i].getStep(n);
	      	TPoint p = measuringTool.getStep(n).getPoints()[i];
	    		if (step==null) {
		      	p.detach();
	    			continue;
	    		}
	      	TPoint target = step.getPoints()[0];
	      	p.attachTo(target);
	    	}  		
	  	}
	  	else { // attached mass is null
	    	for (int n = clip.getStartFrameNumber(); n<=clip.getEndFrameNumber(); n++) {
	      	TPoint p = measuringTool.getStep(n).getPoints()[i];
	      	p.detach();
	    	}  		  		
	  	}
  	}
  	measuringTool.getToolbarTrackComponents(trackerPanel);
  }

  /**
   * Refreshes the attachment and measuring tool dropdowns.
   */
  protected void refreshDropdowns() {
		masses = trackerPanel.getDrawables(PointMass.class);
    for (TTrack p: masses) {
      p.removePropertyChangeListener("step", this); //$NON-NLS-1$
      p.removePropertyChangeListener("steps", this); //$NON-NLS-1$
      p.removePropertyChangeListener("name", this); //$NON-NLS-1$
      p.removePropertyChangeListener("color", this); //$NON-NLS-1$
      p.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      p.addPropertyChangeListener("step", this); //$NON-NLS-1$
      p.addPropertyChangeListener("steps", this); //$NON-NLS-1$
      p.addPropertyChangeListener("name", this); //$NON-NLS-1$
      p.addPropertyChangeListener("color", this); //$NON-NLS-1$
      p.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
    }
		FontSizer.setFonts(rendererDropdown, FontSizer.getLevel());
		rendererDropdown.setModel(new AttachmentComboBoxModel());
		FontSizer.setFonts(editorDropdown, FontSizer.getLevel());
		editorDropdown.setModel(new AttachmentComboBoxModel());
    
		FontSizer.setFonts(measuringToolDropdown, FontSizer.getLevel());
    Object tool = measuringToolDropdown.getSelectedItem();
		java.util.Vector<TTrack> tools = new java.util.Vector<TTrack>();
    for (TTrack track: trackerPanel.getTracks()) {
    	if (track instanceof TapeMeasure) {
    		TapeMeasure tape = (TapeMeasure)track;
//    		if (tape.isViewable())
    			tools.add(tape);
    	}
    	else if (track instanceof Protractor) {
    		tools.add(track);
    	}
    }
    for (TTrack p: tools) {
      p.removePropertyChangeListener("name", this); //$NON-NLS-1$
      p.removePropertyChangeListener("color", this); //$NON-NLS-1$
      p.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      p.addPropertyChangeListener("name", this); //$NON-NLS-1$
      p.addPropertyChangeListener("color", this); //$NON-NLS-1$
      p.addPropertyChangeListener("footprint", this); //$NON-NLS-1$    	
    }
    measuringToolDropdown.setModel(new DefaultComboBoxModel(tools));
    if (tool==measuringTool) {
    	setMeasuringTool(tools.isEmpty()? null: tools.get(0));
    }
  }
  
  /**
   * Updates this inspector to show the system's current particles.
   */
  protected void refreshDisplay() {
    setTitle(TrackerRes.getString("AttachmentInspector.Title")); //$NON-NLS-1$
    helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$  
    closeButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    dummyMass.setName(TrackerRes.getString("DynamicSystemInspector.ParticleName.None")); //$NON-NLS-1$
    if (endPointCount>0 && measuringTool!=null) {
	    if (measuringTool instanceof Protractor) {
	    	endPointName[0] = TrackerRes.getString("AttachmentInspector.Label.Vertex"); //$NON-NLS-1$
	      endPointName[1] = TrackerRes.getString("AttachmentInspector.Label.End")+" 1"; //$NON-NLS-1$ //$NON-NLS-2$
	      endPointName[2] = TrackerRes.getString("AttachmentInspector.Label.End")+" 2"; //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    else {
	      endPointName[0] = TrackerRes.getString("AttachmentInspector.Label.End")+" 1"; //$NON-NLS-1$ //$NON-NLS-2$
	      endPointName[1] = TrackerRes.getString("AttachmentInspector.Label.End")+" 2";    	 //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    measuringToolDropdown.setSelectedItem(measuringTool);
    }
    pack();
    repaint();
  }
  
  public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		FontSizer.setFonts(attachmentCellRenderer.label, level);
		FontSizer.setFonts(table, level);
		refreshDropdowns();
		pack();
  }
  

  
  /**
   * A class to provide model data for the attachment table.
   */
  class AttachmentTableModel extends DefaultTableModel {
    public int getRowCount() {return endPointCount;}

    public int getColumnCount() {return 2;}

    public Object getValueAt(int row, int col) {return col==0? endPointName[row]: attachedMass[row];}
    
    public String getColumnName(int col) {
    	return col==0? TrackerRes.getString("AttachmentInspector.Header.PointName"): //$NON-NLS-1$
    		TrackerRes.getString("AttachmentInspector.Header.AttachedTo"); //$NON-NLS-1$
    }
    
    public boolean isCellEditable(int row, int col) {return col==1;}
    
    public Class<?> getColumnClass(int col) {return col==0? String.class: PointMass.class;}
    
    public void setValueAt(Object val, int row, int col) {} // empty method

  }
  
  /**
   * A class to render attachment table cells.
   */
  class AttachmentCellRenderer implements TableCellRenderer {
  	
  	JLabel label;
  	
  	AttachmentCellRenderer() {
  		label = new JLabel();
  		label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setBackground(Color.white);
  	}
  	
    public Component getTableCellRendererComponent(JTable table, Object val,
        boolean selected, boolean hasFocus, int row, int col) {
    	
    	if (col==0) {
    		label.setText((String)val);
    		label.validate();
    		return label;
    	}
    	
    	rendererDropdown.setSelectedItem(val==null? dummyMass: val);
			return rendererDropdown;
    }
        
  }
  
  /**
   * A class to edit PointMass table cells in a JComboBox.
   */
  class AttachmentCellEditor extends DefaultCellEditor {
  	
  	AttachmentCellEditor() {
  		super(editorDropdown);
  	}
  	
    public Component getTableCellEditorComponent(JTable table, Object value,
				 boolean isSelected, int row, int column) {
    	editorDropdown.setSelectedItem(value==null? dummyMass: value);
    	return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
  	public Object getCellEditorValue() {
      Object obj = super.getCellEditorValue();
			int row = table.getSelectedRow();
			if (row<0) return null;
			attachedMass[row] = obj==dummyMass? null: (PointMass)obj;				
    	refreshMeasuringTool();
      return obj;
    }
  	
  }
 
  /**
   * A class to render track labels for the attachment JComboBoxes.
   */
  class TTrackRenderer extends JLabel implements ListCellRenderer {
  	
  	TTrackRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 0));
  	}
  	
    public Component getListCellRendererComponent(JList list, Object val, int index,
        boolean selected, boolean hasFocus) {

      if (selected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
			if (val!=null) {
				TTrack track = (TTrack)val;
				setText(track.getName());
				setIcon(track==dummyMass? dummyIcon: track.getFootprint().getIcon(21, 16));
			}
			return this;
    }

  }
  
  /**
   * A class to provide model data for the attachment JComboBoxes.
   */
  class AttachmentComboBoxModel extends AbstractListModel implements ComboBoxModel {  	
  	
  	Object selected = dummyMass;
  	
		public int getSize() {
			return masses==null? 1: masses.size()+1;
		}

		public Object getElementAt(int index) {
			return index==0? dummyMass: masses.get(index-1);
		}

		public void setSelectedItem(Object anItem) {
			selected = anItem;
		}

		public Object getSelectedItem() {
			return selected==null? dummyMass: selected;
		}
  	
  }
}
