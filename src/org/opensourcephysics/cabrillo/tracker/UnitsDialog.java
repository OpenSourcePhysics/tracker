/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.opensourcephysics.tools.FontSizer;

/**
 * A dialog to set length, mass and angle units.
 *
 * @author Douglas Brown
 */
public class UnitsDialog extends JDialog {
	
  final static Color _RED = new Color(255, 160, 180);
  static TFrame frame;

  // instance fields
  private TrackerPanel trackerPanel;
  private JLabel lengthLabel, massLabel, timeLabel;
  private ArrayList<JLabel> labels;
  private JButton closeButton;
  private JRadioButton degreesButton, radiansButton;
  private JCheckBox visibleCheckbox;
  private JTextField lengthUnitField, massUnitField, timeUnitField;
  private TitledBorder unitsBorder, angleBorder;
  
  /**
   * Constructs a UnitsDialog for a TrackerPanel.
   *
   * @param trackerPanel the TrackerPanel
   */
  public UnitsDialog(TrackerPanel trackerPanel) {
    super(JOptionPane.getFrameForComponent(trackerPanel), true);
  	this.trackerPanel = trackerPanel;
  	if (frame==null) frame = trackerPanel.getTFrame();
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
    timeUnitField = new JTextField(5);
    timeUnitField.setEditable(false);
    
    // angle unit buttons
    degreesButton = new JRadioButton();
    radiansButton = new JRadioButton();
    Action angleUnitAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (frame.anglesInRadians==radiansButton.isSelected()) return;
      	frame.setAnglesInRadians(radiansButton.isSelected());
			}
    };
    degreesButton.setAction(angleUnitAction);
    radiansButton.setAction(angleUnitAction);
    ButtonGroup group = new ButtonGroup();
    group.add(degreesButton);
    group.add(radiansButton);
    degreesButton.setSelected(!frame.anglesInRadians);
    radiansButton.setSelected(frame.anglesInRadians);
    
    // visible checkbox
    visibleCheckbox = new JCheckBox();
    visibleCheckbox.setSelected(trackerPanel.isUnitsVisible());
    visibleCheckbox.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				trackerPanel.setUnitsVisible(visibleCheckbox.isSelected());
    		refreshGUI();
			}   	
    });
    
    // close button
    closeButton = new JButton();
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    
    // titled borders
    unitsBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
    angleBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
    
    // assemble
  	Box box = Box.createVerticalBox();
  	box.setBorder(unitsBorder);
  	contentPane.add(box, BorderLayout.NORTH);
  	
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
    contentPane.add(panel, BorderLayout.CENTER);
    
    panel = new JPanel();
    panel.add(closeButton);
    contentPane.add(panel, BorderLayout.SOUTH);
  }
  
  /**
   * Updates the GUI.
   */
  protected void refreshGUI() {
    setTitle(TrackerRes.getString("UnitsDialog.Title")); //$NON-NLS-1$
    closeButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
  	lengthLabel.setText(TrackerRes.getString("NumberFormatSetter.Help.Dimensions.2")); //$NON-NLS-1$
  	massLabel.setText(TrackerRes.getString("NumberFormatSetter.Help.Dimensions.4")); //$NON-NLS-1$
  	lengthUnitField.setText(trackerPanel.lengthUnit);
  	massUnitField.setText(trackerPanel.massUnit);
  	timeUnitField.setText(trackerPanel.timeUnit);
  	timeLabel.setText(TrackerRes.getString("NumberFormatSetter.Help.Dimensions.3")); //$NON-NLS-1$
    degreesButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Degrees")); //$NON-NLS-1$
    radiansButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Radians")); //$NON-NLS-1$
    degreesButton.setSelected(!frame.anglesInRadians);
    radiansButton.setSelected(frame.anglesInRadians);
    unitsBorder.setTitle(TrackerRes.getString("UnitsDialog.Border.LMT.Text")); //$NON-NLS-1$
    angleBorder.setTitle(TrackerRes.getString("NumberFormatSetter.TitledBorder.Units.Text")); //$NON-NLS-1$
    
    boolean hasLengthUnit = trackerPanel.lengthUnit!=null; 
    boolean hasMassUnit = trackerPanel.massUnit!=null; 
  	visibleCheckbox.setText(TrackerRes.getString("UnitsDialog.Checkbox.Visible.Text")); //$NON-NLS-1$
    visibleCheckbox.setSelected(trackerPanel.isUnitsVisible());
    visibleCheckbox.setEnabled(hasLengthUnit && hasMassUnit);
  	visibleCheckbox.setToolTipText(hasLengthUnit && hasMassUnit?
  			TrackerRes.getString("UnitsDialog.Checkbox.Visible.Tooltip"): //$NON-NLS-1$
  			TrackerRes.getString("UnitsDialog.Checkbox.Visible.Disabled.Tooltip")); //$NON-NLS-1$
  	
   	lengthUnitField.setBackground(hasLengthUnit? Color.WHITE: _RED);
   	lengthUnitField.setToolTipText(hasLengthUnit? null: 
  			TrackerRes.getString("UnitsDialog.Field.Undefined.Tooltip")); //$NON-NLS-1$
   	massUnitField.setBackground(hasMassUnit? Color.WHITE: _RED);
   	massUnitField.setToolTipText(hasMassUnit? null: 
  			TrackerRes.getString("UnitsDialog.Field.Undefined.Tooltip")); //$NON-NLS-1$
   	
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
   
  	pack();
    repaint();
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
   * Sets the length or mass unit based on the current text in a UnitField
   * 
   * @param field the length or mass field
   */
  private void setUnit(UnitField field) {
  	if (field==lengthUnitField) {
  		trackerPanel.setLengthUnit(field.getText());
    	refreshGUI();
  	}
  	else if (field==massUnitField) {
  		trackerPanel.setMassUnit(field.getText());
    	refreshGUI();
  	}
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
        public void focusLost(FocusEvent e) {
    			if (getBackground()==Color.yellow) {
            setUnit(UnitField.this);
    			}
        }

      });

    }
  }
  
}
