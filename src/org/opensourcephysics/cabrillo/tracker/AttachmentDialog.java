/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.IntegerField;
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
  protected int trackID;
  protected TrackerPanel trackerPanel;
  protected boolean isVisible;
  protected JButton closeButton, helpButton;
  protected ArrayList<PointMass> masses;
  protected JTable table;
	protected int cellheight = 28; // depends on font level
  protected JComboBox rendererDropdown, editorDropdown, measuringToolDropdown;
  protected PointMass dummyMass;
  protected Icon dummyIcon = new ShapeIcon(null, 21, 16);
  protected JScrollPane scrollPane;
  protected AttachmentCellRenderer attachmentCellRenderer = new AttachmentCellRenderer();
  protected TTrackRenderer trackRenderer = new TTrackRenderer();
  protected JPanel attachmentsPanel, circleFitterPanel, circleFitterStartStopPanel;
  protected JRadioButton stepsButton, tracksButton;
  protected JCheckBox relativeCheckbox;
  protected IntegerField startField, countField;
  protected JLabel startLabel, countLabel;
  protected boolean refreshing;

  
  /**
   * Constructs an AttachmentDialog.
   *
   * @param track the measuring tool
   */
  public AttachmentDialog(TTrack track) {
    super(JOptionPane.getFrameForComponent(track.trackerPanel), false);
  	trackerPanel = track.trackerPanel;
    createGUI();
    setMeasuringTool(track);
		refreshDropdowns();
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("clear", this); //$NON-NLS-1$
    TFrame frame = trackerPanel.getTFrame();
    frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
    refreshGUI();
  }

  /**
   * Responds to property change events.
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
    		deleted.removePropertyChangeListener("name", this); //$NON-NLS-1$
    		deleted.removePropertyChangeListener("color", this); //$NON-NLS-1$
    		deleted.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
    		TTrack measuringTool = TTrack.getTrack(trackID);
    		if (measuringTool!=null) {
    			if (measuringTool!=deleted) {
		    		TTrack[] attachments = measuringTool.getAttachments();
			     	for (int i = 0; i < attachments.length; i++) {
					  	if (deleted==attachments[i] || deleted==measuringTool) {
					  		attachments[i] = null;	  		
					  	}
			    	}
			    	measuringTool.refreshAttachments();
	    		}
    			else { // measuring tool has been deleted
    				trackID = 0;
    			}
    		}
    	}
  		refreshDropdowns();
    	refreshGUI();
    }
    else if (e.getPropertyName().equals("clear")) { //$NON-NLS-1$
    	for (Integer n: TTrack.activeTracks.keySet()) {
    		TTrack next = TTrack.activeTracks.get(n);
    		next.removePropertyChangeListener("name", this); //$NON-NLS-1$
    		next.removePropertyChangeListener("color", this); //$NON-NLS-1$
    		next.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
    	}
  		refreshDropdowns();
    	refreshGUI();
    }
    else if (e.getPropertyName().equals("selectedtrack") && e.getNewValue()!=null) { //$NON-NLS-1$
      TTrack track = (TTrack)e.getNewValue();
      for (int i=0; i<measuringToolDropdown.getItemCount(); i++) {
      	if (track==measuringToolDropdown.getItemAt(i)) {
      		measuringToolDropdown.setSelectedIndex(i);
      		break;
      	}
      }
    }
//    else if (e.getPropertyName().equals("step") //$NON-NLS-1$
//    		|| e.getPropertyName().equals("steps")) { //$NON-NLS-1$
//    	measuringTool.refreshAttachments();
//    }
    else if (e.getPropertyName().equals("dataPoint")) { //$NON-NLS-1$
  		TTrack measuringTool = TTrack.getTrack(trackID);
    	measuringTool.refreshAttachments();
      DefaultTableModel dm = (DefaultTableModel)table.getModel();
      dm.fireTableDataChanged();
    }
    else refreshGUI();
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
   * Disposes of this dialog.
   */
  public void dispose() {
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("clear", this); //$NON-NLS-1$
      for (TTrack p: masses) {
        p.removePropertyChangeListener("name", this); //$NON-NLS-1$
        p.removePropertyChangeListener("color", this); //$NON-NLS-1$
        p.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      }
      masses.clear();
      dummyMass.delete();
      dummyMass = null;
  		TTrack measuringTool = TTrack.getTrack(trackID);
    	if (measuringTool!=null) {
  	    measuringTool.removePropertyChangeListener("dataPoint", this); //$NON-NLS-1$  		
    	}
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
      }
    	trackerPanel.attachmentDialog = null;
      trackerPanel = null;
    }
    super.dispose();
  }

//_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {
//  	setResizable(false);
    // create GUI components
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    
    // put attachments panel in content pane CENTER
    attachmentsPanel = new JPanel(new BorderLayout());
    contentPane.add(attachmentsPanel, BorderLayout.CENTER);
    
    
    // put measuring tool dropdown in attachments panel NORTH
    JPanel north = new JPanel();
    north.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
    measuringToolDropdown = new JComboBox();
    measuringToolDropdown.setRenderer(trackRenderer);
    measuringToolDropdown.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TTrack tool = (TTrack)measuringToolDropdown.getSelectedItem();
    		TTrack measuringTool = TTrack.getTrack(trackID);
      	if (tool==measuringTool) return;
        setMeasuringTool(tool);
      }
    });
    north.add(measuringToolDropdown);
    attachmentsPanel.add(north, BorderLayout.NORTH);
        
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
    		int cellCount = Math.max(4, table.getRowCount()+1);
    		cellCount = Math.min(10, cellCount);
    		dim.height = cellCount*cellheight+8;
    		dim.width= table.getPreferredSize().width+20;
    		return dim;
    	}
    };

    // put table in attachments panel CENTER
    JPanel center = new JPanel(new GridLayout(1,1));
    attachmentsPanel.add(center, BorderLayout.CENTER);
    center.add(scrollPane);
    center.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
   
    // steps and tracks buttons
    stepsButton = new JRadioButton();
    tracksButton = new JRadioButton();
    Action tracksOrStepsAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
      	if (refreshing) return;
      	CircleFitter fitter = (CircleFitter)TTrack.getTrack(trackID);
;
      	fitter.attachToSteps = !tracksButton.isSelected();
	    	fitter.refreshAttachments();
				refreshGUI();				
	      DefaultTableModel dm = (DefaultTableModel)table.getModel();
	      dm.fireTableDataChanged();				
			}   	
    };
    stepsButton.addActionListener(tracksOrStepsAction);
    tracksButton.addActionListener(tracksOrStepsAction);
    ButtonGroup group = new ButtonGroup();
    group.add(stepsButton);
    group.add(tracksButton);
    tracksButton.setSelected(true);
    
    // relative button
    relativeCheckbox = new JCheckBox();
    relativeCheckbox.setSelected(false);
    relativeCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
      	if (refreshing) return;
      	CircleFitter fitter = (CircleFitter)TTrack.getTrack(trackID);
;
      	fitter.isRelativeFrameNumbers = relativeCheckbox.isSelected();
      	refreshFieldsAndButtons(fitter);
	    	fitter.refreshAttachments();
				refreshGUI();				
			}   	
    });
    
    // range action, listener and fields
    final Action frameRangeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
      	CircleFitter fitter = (CircleFitter)TTrack.getTrack(trackID);
;
    		fitter.setAttachmentStartFrame(startField.getIntValue());   		
    		fitter.setAttachmentFrameCount(countField.getIntValue());
    		refreshFieldsAndButtons(fitter);
        fitter.refreshAttachments();
		    DefaultTableModel dm = (DefaultTableModel)table.getModel();
		    dm.fireTableDataChanged();
		    fitter.trackerPanel.repaint();
			}
    };

    FocusListener frameRangeFocusListener = new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (e.getSource()==startField && startField.getBackground()!=Color.yellow) return;
      	if (e.getSource()==countField && countField.getBackground()!=Color.yellow) return;
      	frameRangeAction.actionPerformed(null);
      }
    };
    startField = new IntegerField(3);
    startField.addActionListener(frameRangeAction);
    startField.addFocusListener(frameRangeFocusListener);
    countField = new IntegerField(2);
    countField.addActionListener(frameRangeAction);
    countField.addFocusListener(frameRangeFocusListener);
    
  	startLabel = new JLabel();
  	startLabel.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
  	countLabel = new JLabel();
  	countLabel.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
    
    // put circleFitter panel in attachments panel SOUTH
    circleFitterPanel = new JPanel(new BorderLayout());
    circleFitterPanel.setBorder(BorderFactory.createTitledBorder("")); //$NON-NLS-1$
    
    // put steps and tracks buttons in circleFitterPanel NORTH
    JPanel buttonbar = new JPanel();
    circleFitterPanel.add(buttonbar, BorderLayout.NORTH);
    buttonbar.add(stepsButton);
    buttonbar.add(tracksButton);
    
    // create circleFitterStartStopPanel
    circleFitterStartStopPanel = new JPanel(new BorderLayout());
    Border empty = BorderFactory.createEmptyBorder(0,4,0,4);
    Border etched = BorderFactory.createEtchedBorder();
    circleFitterStartStopPanel.setBorder(BorderFactory.createCompoundBorder(empty, etched));
    
    
    // put start and end frame controls in circleFitterStartStopPanel CENTER
    buttonbar = new JPanel();
    buttonbar.add(startLabel);
    buttonbar.add(startField);
//    buttonbar.add(startSpinner);
    buttonbar.add(countLabel);
    buttonbar.add(countField);
//    buttonbar.add(endSpinner);
    circleFitterStartStopPanel.add(buttonbar, BorderLayout.CENTER);
    
    // put relative checkbox in circleFitterStartStopPanel SOUTH
    buttonbar = new JPanel();
    circleFitterStartStopPanel.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(relativeCheckbox);
    

    // help and close buttons
    helpButton = new JButton();
    helpButton.setForeground(new Color(0, 0, 102));
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    		TTrack measuringTool = TTrack.getTrack(trackID);
      	String keyword = measuringTool==null? "circle":  //$NON-NLS-1$
      		measuringTool instanceof Protractor? "protractor":  //$NON-NLS-1$
      		measuringTool instanceof TapeMeasure? "tape": "circle"; //$NON-NLS-1$ //$NON-NLS-2$
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
    
    // put help and close button in content pane SOUTH
    buttonbar = new JPanel();
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(helpButton);
    buttonbar.add(closeButton);
  }
  
  /**
   * Sets the measuring tool.
   */
  protected void setMeasuringTool(TTrack tool) {
		TTrack measuringTool = TTrack.getTrack(trackID);
  	if (measuringTool!=null) {
	    measuringTool.removePropertyChangeListener("dataPoint", this); //$NON-NLS-1$  		
  	}
  	
    measuringTool = tool;
  	trackID = measuringTool.getID();
	  measuringTool.addPropertyChangeListener("dataPoint", this); //$NON-NLS-1$  
	  
    measuringTool.refreshAttachments();
    refreshDropdowns();
    if (measuringTool instanceof CircleFitter) {
    	CircleFitter fitter = (CircleFitter)measuringTool;
	    refreshFieldsAndButtons(fitter);
    }
    DefaultTableModel dm = (DefaultTableModel)table.getModel();
    dm.fireTableDataChanged();
    refreshGUI();
  }

  /**
   * Refreshes the attachment and measuring tool dropdowns.
   */
  protected void refreshDropdowns() {
		masses = trackerPanel.getDrawables(PointMass.class);
    for (TTrack p: masses) {
      p.removePropertyChangeListener("name", this); //$NON-NLS-1$
      p.removePropertyChangeListener("color", this); //$NON-NLS-1$
      p.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
    }
		TTrack measuringTool = TTrack.getTrack(trackID);
		if (measuringTool!=null && measuringTool instanceof TapeMeasure) {
			// can't attach calibration stick to models--creates circular dependency
			TapeMeasure tape = (TapeMeasure)measuringTool;
			if (tape.isStickMode()) {
				masses.removeAll(trackerPanel.getDrawables(ParticleModel.class));
			}
		}
    for (TTrack p: masses) {
      p.addPropertyChangeListener("name", this); //$NON-NLS-1$
      p.addPropertyChangeListener("color", this); //$NON-NLS-1$
      p.addPropertyChangeListener("footprint", this); //$NON-NLS-1$
    }
		FontSizer.setFonts(rendererDropdown, FontSizer.getLevel());
		rendererDropdown.setModel(new AttachmentComboBoxModel());
		FontSizer.setFonts(editorDropdown, FontSizer.getLevel());
		editorDropdown.setModel(new AttachmentComboBoxModel());
    
		FontSizer.setFonts(measuringToolDropdown, FontSizer.getLevel());
		java.util.Vector<TTrack> tools = new java.util.Vector<TTrack>();
    for (TTrack track: trackerPanel.getTracks()) {
    	if (track instanceof TapeMeasure
    			|| track instanceof Protractor
    			|| track instanceof CircleFitter) {
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
    if (!tools.isEmpty() && measuringTool!=null) {
    	measuringToolDropdown.setSelectedItem(measuringTool);
    }
    else {
    	// measuring tool is null, so set it to first in list, if any
    	for (TTrack next: tools) {
    		setMeasuringTool(next);
    		break;
    	}
    }
  }
  
  /**
   * Refreshes the start and end fields based on the state of a CircleFitter.
   * Also refreshes the button state.
   * 
   * @param fitter the CircleFitter
   */
  protected void refreshFieldsAndButtons(CircleFitter fitter) {
  	if (fitter.attachToSteps && fitter.isRelativeFrameNumbers) {
      startField.getFormat().applyPattern("+#;-#"); //$NON-NLS-1$
  	}
  	else {
      startField.getFormat().applyPattern("#;-#"); //$NON-NLS-1$
  	}
  	int min = fitter.isRelativeFrameNumbers? 
  			1-trackerPanel.getPlayer().getVideoClip().getFrameCount(): 
  				trackerPanel.getPlayer().getVideoClip().getFirstFrameNumber();
  	int max = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
  	startField.setMaxValue(max);
  	startField.setMinValue(min);
    startField.setIntValue(fitter.isRelativeFrameNumbers? fitter.relativeStart: fitter.absoluteStart);
  	countField.setMaxValue(CircleFitter.maxDataPointCount);
  	countField.setMinValue(1);
    countField.setIntValue(fitter.getAttachmentFrameCount());
    
    refreshing = true;
    stepsButton.setSelected(fitter.attachToSteps);
    relativeCheckbox.setSelected(fitter.isRelativeFrameNumbers);
    refreshing = false;
  }
  
  /**
   * Updates this inspector to show the system's current attachments.
   */
  protected void refreshGUI() {
    setTitle(TrackerRes.getString("AttachmentInspector.Title")); //$NON-NLS-1$
    helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$  
    closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
    dummyMass.setName(TrackerRes.getString("DynamicSystemInspector.ParticleName.None")); //$NON-NLS-1$
  	startLabel.setText(TrackerRes.getString("AttachmentInspector.Label.StartFrame")); //$NON-NLS-1$
  	countLabel.setText(TrackerRes.getString("AttachmentInspector.Label.FrameCount")); //$NON-NLS-1$
  	stepsButton.setText(TrackerRes.getString("AttachmentInspector.Button.Steps")); //$NON-NLS-1$
  	tracksButton.setText(TrackerRes.getString("AttachmentInspector.Button.Tracks")); //$NON-NLS-1$
  	relativeCheckbox.setText(TrackerRes.getString("AttachmentInspector.Checkbox.Relative")); //$NON-NLS-1$
  	stepsButton.setToolTipText(TrackerRes.getString("AttachmentInspector.Button.Steps.Tooltip")); //$NON-NLS-1$
  	tracksButton.setToolTipText(TrackerRes.getString("AttachmentInspector.Button.Tracks.Tooltip")); //$NON-NLS-1$
  	relativeCheckbox.setToolTipText(TrackerRes.getString("AttachmentInspector.Checkbox.Relative.Tooltip")); //$NON-NLS-1$
    TitledBorder border = (TitledBorder)circleFitterPanel.getBorder();
  	border.setTitle(TrackerRes.getString("AttachmentInspector.Border.Title.AttachTo")); //$NON-NLS-1$
    
  	// refresh layout to include/exclude circle fitter items
  	boolean hasCircleFitterPanel = attachmentsPanel.getComponentCount()>2;
  	boolean hasStartStopPanel = circleFitterPanel.getComponentCount()>1;
  	boolean changedLayout = false;
		TTrack measuringTool = TTrack.getTrack(trackID);
  	if (measuringTool instanceof CircleFitter) {
      // put circleFitter panel in attachments panel SOUTH
      changedLayout = !hasCircleFitterPanel;
      attachmentsPanel.add(circleFitterPanel, BorderLayout.SOUTH);

      CircleFitter fitter = (CircleFitter)measuringTool;
      if (!fitter.attachToSteps) {
        changedLayout = changedLayout || hasStartStopPanel;
        circleFitterPanel.remove(circleFitterStartStopPanel);      	
      }
      else {
      	if (fitter.isRelativeFrameNumbers) {
//      		startLabel.setText(TrackerRes.getString("AttachmentInspector.Label.Offset")); //$NON-NLS-1$
      	}
        changedLayout = changedLayout || !hasStartStopPanel;
        circleFitterPanel.add(circleFitterStartStopPanel, BorderLayout.CENTER);      	
      }
  	}
  	else {
      attachmentsPanel.remove(circleFitterPanel);
      changedLayout = hasCircleFitterPanel;
  	}
  	if (changedLayout) {
  		pack();
  	}
    repaint();
  }
  
  public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		FontSizer.setFonts(attachmentCellRenderer.label, level);
		FontSizer.setFonts(table, level);
		FontSizer.setFonts(circleFitterPanel, level);
		FontSizer.setFonts(circleFitterStartStopPanel, level);
		refreshDropdowns();
		pack();
  }
  

  
  /**
   * A class to provide model data for the attachment table.
   */
  class AttachmentTableModel extends DefaultTableModel {
    public int getRowCount() {
  		TTrack measuringTool = TTrack.getTrack(trackID);
    	if (measuringTool==null) return 0;
    	
    	if (measuringTool instanceof CircleFitter) {
    		CircleFitter fitter = (CircleFitter)measuringTool;
    		if (fitter.attachToSteps) {
    			return 1;
    		}
    	}
    	TTrack[] attachments = measuringTool.getAttachments();
    	return attachments==null?	0: attachments.length;
    }

    public int getColumnCount() {return 2;}

    public Object getValueAt(int row, int col) {
  		TTrack measuringTool = TTrack.getTrack(trackID);
    	if (col==0) {
		    return measuringTool.getAttachmentDescription(row);
    	}
    	return measuringTool.getAttachments()[row];
    }
    
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
  		TTrack measuringTool = TTrack.getTrack(trackID);
			TTrack[] attachments = measuringTool.getAttachments();
			if (attachments[row]!=null) {
				attachments[row].removePropertyChangeListener("step", measuringTool); //$NON-NLS-1$
				attachments[row].removePropertyChangeListener("steps", measuringTool); //$NON-NLS-1$
			}
			attachments[row] = obj==dummyMass? null: (PointMass)obj;
    	measuringTool.refreshAttachments();
			refreshGUI();
			
      DefaultTableModel dm = (DefaultTableModel)table.getModel();
      dm.fireTableDataChanged();
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
				Icon icon = track==dummyMass? 
						new ResizableIcon(dummyIcon): 
						track.getFootprint().getIcon(21, 16);
				int factor = FontSizer.getIntegerFactor();
				if (icon instanceof ResizableIcon) {
					((ResizableIcon)icon).resize(factor);
				}
				setIcon(icon);
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
