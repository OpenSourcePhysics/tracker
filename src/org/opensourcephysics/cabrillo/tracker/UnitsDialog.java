/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2026 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.tools.FontSizer;

/**
 * A dialog to set time. length, mass and angle units.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class UnitsDialog extends JDialog {
	
  // instance fields
  private TFrame frame;
  private Integer panelID;
  private String prevTabL, prevTabM, prevTabT;
  private String prevDefL, prevDefM, prevDefT;
  private boolean prevTabRadians, prevDefRadians;
  private ControlPanel tabPanel, prefsPanel;
  private JTabbedPane tabbedPane;
  private JButton acceptButton, revertButton, unitsSIButton;

  
  /**
   * Constructs a UnitsDialog for a TrackerPanel.
   *
   * @param trackerPanel the TrackerPanel
   */
  public UnitsDialog(TrackerPanel trackerPanel) {
    super(JOptionPane.getFrameForComponent(trackerPanel), true);
  	panelID = trackerPanel.getID();
  	frame = trackerPanel.getTFrame();
    createGUI();
    refreshGUI();
  }

  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEtchedBorder());
    setContentPane(contentPane);
    
  	TrackerPanel panel = frame.getTrackerPanelForID(panelID);  
  	prevTabL = panel.getLengthUnit();
  	prevTabM = panel.getMassUnit();
  	prevTabT = panel.getTimeUnit();
  	prevTabRadians = panel.isAnglesInRadians();
  	prevDefL = Tracker.preferredLengthUnit;
  	prevDefM = Tracker.preferredMassUnit;
  	prevDefT = Tracker.preferredTimeUnit;
  	prevDefRadians = Tracker.isRadians;
  	
    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);		
    tabPanel = new ControlPanel();
		prefsPanel = new ControlPanel();
		tabbedPane.addTab("", tabPanel); //$NON-NLS-1$
		tabbedPane.addTab("", prefsPanel); //$NON-NLS-1$
		// add change listener after adding tabs to prevent start-up event firing
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				refreshGUI();
			}
		});
		tabbedPane.setSelectedComponent(tabPanel);
		contentPane.add(tabbedPane, BorderLayout.CENTER);   
		
    // close button
    acceptButton = new JButton();
    acceptButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    
    // revert button
    revertButton = new JButton();
    revertButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
    		if (tabPanel==tabbedPane.getSelectedComponent()) {
        	TrackerPanel panel = frame.getTrackerPanelForID(panelID);  
        	panel.setLengthUnit(prevTabL, true);
        	panel.setMassUnit(prevTabM, true);
        	panel.setTimeUnit(prevTabT, true);
        	panel.setAnglesInRadians(prevTabRadians);
    		}
    		else {
    			Tracker.preferredLengthUnit = prevDefL;
    			Tracker.preferredMassUnit = prevDefM;
    			Tracker.preferredTimeUnit = prevDefT;    			
    			Tracker.isRadians = prevDefRadians;
    		}
      	refreshGUI();
      }
    });
    
    // SI button
    unitsSIButton = new JButton();
    unitsSIButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
    		if (tabPanel==tabbedPane.getSelectedComponent()) {
        	TrackerPanel panel = frame.getTrackerPanelForID(panelID);  
        	panel.setLengthUnit(Tracker.SI_LENGTH_UNIT, true);
        	panel.setMassUnit(Tracker.SI_MASS_UNIT, true);
        	panel.setTimeUnit(Tracker.SI_TIME_UNIT, true);
        	panel.anglesInRadians = true;
    		}
    		else {
    			Tracker.preferredLengthUnit = Tracker.SI_LENGTH_UNIT;
    			Tracker.preferredMassUnit = Tracker.SI_MASS_UNIT;
    			Tracker.preferredTimeUnit = Tracker.SI_TIME_UNIT;
    			Tracker.isRadians = true;
    		}
      	refreshGUI();
      }
    });
    
    JPanel buttonbar = new JPanel();
    buttonbar.add(unitsSIButton);
    buttonbar.add(revertButton);
    buttonbar.add(acceptButton);

		contentPane.add(buttonbar, BorderLayout.SOUTH);   
  }
  
  /**
   * Updates the GUI.
   */
  protected void refreshGUI() {
    setTitle(TrackerRes.getString("UnitsDialog.Title")); //$NON-NLS-1$
    acceptButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    revertButton.setText(TrackerRes.getString("UnitsDialog.Button.Revert.Text")); //$NON-NLS-1$
    unitsSIButton.setText(TrackerRes.getString("UnitsDialog.Button.SI.Text")); //$NON-NLS-1$
		tabbedPane.setTitleAt(0, TrackerRes.getString("UnitsDialog.Tab.Tab")); //$NON-NLS-1$
		tabbedPane.setTitleAt(1, TrackerRes.getString("UnitsDialog.Tab.Preferred")); //$NON-NLS-1$
		if (tabPanel==tabbedPane.getSelectedComponent()) {
			tabPanel.refreshGUI();			
		}
		else {
			prefsPanel.refreshGUI();			
		}
		// enable/disable buttons
  	TrackerPanel panel = frame.getTrackerPanelForID(panelID);  
		boolean unchanged = tabPanel==tabbedPane.getSelectedComponent()?
		  	prevTabL.equals(panel.getLengthUnit())
		  	&& prevTabM.equals(panel.getMassUnit())
		  	&& prevTabT.equals(panel.getTimeUnit())
		  	&& prevTabRadians == panel.isAnglesInRadians():
		  	
    		Tracker.preferredLengthUnit.equals(prevDefL)
    		&& Tracker.preferredMassUnit.equals(prevDefM)
    		&& Tracker.preferredTimeUnit.equals(prevDefT)    			
    		&& Tracker.isRadians == prevDefRadians;
		revertButton.setEnabled(!unchanged);		
		
		boolean isSI = tabPanel==tabbedPane.getSelectedComponent()?
				Tracker.SI_LENGTH_UNIT.equals(panel.getLengthUnit())
		  	&& Tracker.SI_MASS_UNIT.equals(panel.getMassUnit())
		  	&& Tracker.SI_TIME_UNIT.equals(panel.getTimeUnit())
		  	&& panel.isAnglesInRadians() == true:
		  	
    		Tracker.preferredLengthUnit.equals(Tracker.SI_LENGTH_UNIT)
    		&& Tracker.preferredMassUnit.equals(Tracker.SI_MASS_UNIT)
    		&& Tracker.preferredTimeUnit.equals(Tracker.SI_TIME_UNIT)    			
    		&& Tracker.isRadians == true;
		unitsSIButton.setEnabled(!isSI);		

		pack();
    TFrame.repaintT(this);
  }
  
  /**
   * Sets the font level
   * 
   * @param level the level
   */
  protected void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		refreshGUI();
		pack();
  }
  
  
  /**
   * A JPanel with controls
   */
  class ControlPanel extends JPanel {
  	
    private JLabel lengthLabel, massLabel, timeLabel;
    private ArrayList<JLabel> labels;
    private JRadioButton degreesButton, radiansButton;
    private JCheckBox visibleCheckbox;
    private JTextField lengthUnitField, massUnitField, timeUnitField;
    private TitledBorder unitsBorder, angleBorder;

    /**
     * Constructor
     * 
     */
  	ControlPanel() {
  		super(new BorderLayout());
  		createGUI();
  	}
  	
    /**
     * Creates the visible components of this panel.
     */
    private void createGUI() {

    	TrackerPanel tp = frame.getTrackerPanelForID(panelID);  

    	// labels
    	lengthLabel = new JLabel();
    	lengthLabel.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
    	lengthLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    	massLabel = new JLabel();
    	massLabel.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
    	massLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    	timeLabel = new JLabel();
    	timeLabel.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
    	timeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
    	labels = new ArrayList<JLabel>();
      labels.add(lengthLabel);
      labels.add(massLabel);
      labels.add(timeLabel);
    	
      // fields
      lengthUnitField = new UnitField(5);
      massUnitField = new UnitField(5);
      timeUnitField = new UnitField(5);
      
      // angle unit buttons
      degreesButton = new JRadioButton();
      radiansButton = new JRadioButton();
      Action angleUnitAction = new AbstractAction() {
  			@Override
  			public void actionPerformed(ActionEvent e) {
  				if (ControlPanel.this==tabPanel) {
          	TrackerPanel panel = frame.getTrackerPanelForID(panelID);  
	  				if (panel.anglesInRadians==radiansButton.isSelected()) return;
	        	panel.setAnglesInRadians(radiansButton.isSelected());
  				}
  				else {
  					Tracker.isRadians = radiansButton.isSelected();
  				}
  				UnitsDialog.this.refreshGUI();
  			}
      };
      degreesButton.setAction(angleUnitAction);
      radiansButton.setAction(angleUnitAction);
      ButtonGroup group = new ButtonGroup();
      group.add(degreesButton);
      group.add(radiansButton);
      degreesButton.setSelected(!tp.isAnglesInRadians());
      radiansButton.setSelected(tp.isAnglesInRadians());
      
      // visible checkbox
      visibleCheckbox = new JCheckBox();
   
      visibleCheckbox.setSelected(frame.getTrackerPanelForID(panelID).isUnitsVisible());
      visibleCheckbox.setAction(new AbstractAction() {
  			@Override
  			public void actionPerformed(ActionEvent e) {
  				frame.getTrackerPanelForID(panelID).setUnitsVisible(visibleCheckbox.isSelected());
      		refreshGUI();
  			}   	
      });
      
       // titled borders
      unitsBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
      angleBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
      
      // assemble
    	Box box = Box.createVerticalBox();
    	box.setBorder(unitsBorder);
    	add(box, BorderLayout.NORTH);
    	
      JPanel panel = new JPanel();
      panel.add(lengthLabel);
      panel.add(lengthUnitField);
      box.add(panel);
      panel = new JPanel();
      panel.add(massLabel);
      panel.add(massUnitField);
      box.add(panel);
      panel = new JPanel();
      panel.add(timeLabel);
      panel.add(timeUnitField);
      box.add(panel);
      panel = new JPanel();
      panel.add(visibleCheckbox);
      box.add(panel);
      
      panel = new JPanel();
      panel.setBorder(angleBorder);
      panel.add(degreesButton);
      panel.add(radiansButton);
      add(panel, BorderLayout.CENTER);
      
    }
    
    protected void refreshGUI() {
    	if (this==tabPanel) {
	    	TrackerPanel panel = frame.getTrackerPanelForID(panelID);  
	    	lengthUnitField.setText(panel.lengthUnit);
	    	massUnitField.setText(panel.massUnit);
	    	timeUnitField.setText(panel.getTimeUnit());
	      visibleCheckbox.setSelected(panel.isUnitsVisible());
	      degreesButton.setSelected(!panel.isAnglesInRadians());
	      radiansButton.setSelected(panel.isAnglesInRadians());
    	}
    	else {
	    	lengthUnitField.setText(Tracker.preferredLengthUnit);
	    	massUnitField.setText(Tracker.preferredMassUnit);
	    	timeUnitField.setText(Tracker.preferredTimeUnit);
//	      visibleCheckbox.setSelected(panel.isUnitsVisible());    		
	      degreesButton.setSelected(!Tracker.isRadians);
	      radiansButton.setSelected(Tracker.isRadians);
    	}
      setTitle(TrackerRes.getString("UnitsDialog.Title")); //$NON-NLS-1$
    	lengthLabel.setText(TrackerRes.getString("NumberFormatSetter.Help.Dimensions.2")); //$NON-NLS-1$
    	massLabel.setText(TrackerRes.getString("NumberFormatSetter.Help.Dimensions.4")); //$NON-NLS-1$
    	timeLabel.setText(TrackerRes.getString("NumberFormatSetter.Help.Dimensions.3")); //$NON-NLS-1$
      degreesButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Degrees")); //$NON-NLS-1$
      radiansButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Radians")); //$NON-NLS-1$
      unitsBorder.setTitle(TrackerRes.getString("UnitsDialog.Border.LMT.Text")); //$NON-NLS-1$
      angleBorder.setTitle(TrackerRes.getString("NumberFormatSetter.TitledBorder.Units.Text")); //$NON-NLS-1$
      
    	visibleCheckbox.setText(TrackerRes.getString("UnitsDialog.Checkbox.Visible.Text")); //$NON-NLS-1$
    	visibleCheckbox.setToolTipText(TrackerRes.getString("UnitsDialog.Checkbox.Visible.Tooltip")); //$NON-NLS-1$
    	
     	timeUnitField.setBackground(Color.WHITE);
     	massUnitField.setBackground(Color.WHITE);
     	lengthUnitField.setBackground(Color.WHITE);
     	
     	// set label sizes
      labels.add(lengthLabel);
      labels.add(massLabel);
      labels.add(timeLabel);
      // set label sizes
      int w = 0;
      for(JLabel next: labels) {
        next.setPreferredSize(null);
        w = Math.max(w, next.getPreferredSize().width+1);
      }
      Dimension labelSize = lengthLabel.getPreferredSize();
      labelSize.width = w;
      for(JLabel next: labels) {
        next.setPreferredSize(labelSize);
      }
     
    }
    
  	/**
  	 * Sets the length or mass unit based on the current text in a UnitField
  	 * 
  	 * @param field the length or mass field
  	 */
  	private void setUnit(UnitField field) {
  		String s = field.getText();
  		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
  		if (this==tabPanel) {
	  		if (field == lengthUnitField) {
	  			trackerPanel.setLengthUnit(s, true);
	  		} else if (field == massUnitField) {
	  			trackerPanel.setMassUnit(s, true);
	  		} else if (field == timeUnitField) {
	  			trackerPanel.setTimeUnit(s, true);
	  		}
  		}
  		else {
	  		if (field == lengthUnitField) {
	  			String prev = trackerPanel.getLengthUnit();
	  			trackerPanel.setLengthUnit(s, false);
	  			Tracker.preferredLengthUnit = trackerPanel.getLengthUnit();
	  			trackerPanel.setLengthUnit(prev, false);	  			
	  		} else if (field == massUnitField) {
	  			String prev = trackerPanel.getMassUnit();
	  			trackerPanel.setMassUnit(s, false);
	  			Tracker.preferredMassUnit = trackerPanel.getMassUnit();
	  			trackerPanel.setMassUnit(prev, false);	  			
	  		} else if (field == timeUnitField) {
	  			String prev = trackerPanel.getTimeUnit();
	  			trackerPanel.setTimeUnit(s, false);
	  			Tracker.preferredTimeUnit = trackerPanel.getTimeUnit();
	  			trackerPanel.setTimeUnit(prev, false);	  			
	  		} 			
  		}
			UnitsDialog.this.refreshGUI();
  	}
    
    /**
     * A JTextField for setting units
     */
    class UnitField extends JTextField {
    	
      /**
       * Constructor
       * 
       * @param len the field length
       */
    	UnitField(int len) {
    		super(len);
        addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {      
            if(e.getKeyCode()==KeyEvent.VK_ENTER) {
              setUnit(UnitField.this);
            } 
            else {
              setBackground(Color.yellow);
            }
          }
        });
        
        addFocusListener(new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
      			if (getBackground()==Color.yellow) {
              setUnit(UnitField.this);
      			}
          }

        });

      }
    }

    
  }
  
}
