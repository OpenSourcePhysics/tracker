package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.opensourcephysics.tools.UserFunctionEditor;

/**
 * A function panel for a DataModel.
 *
 * @author Douglas Brown
 */
public class DataModelFunctionPanel extends ModelFunctionPanel {
	
	private DataModelClipControl clipControl;
	private DataModelTimeControl timeControl;
	private JPanel customControl;
	private JPanel customTitle;

  /**
   * Constructor.
   *
   * @param editor the user function editor
   * @param track a DataModel
   */
  public DataModelFunctionPanel(DataModel track) {
  	// must pass a UserFunctionEditor (never used) to the superclass
  	super(new UserFunctionEditor(), track);
  	model = track;
  	setName(track.getName());
  	
  	// create and assemble GUI
  	clipControl = new DataModelClipControl((DataModel)model);
  	timeControl = new DataModelTimeControl((DataModel)model);
  	box.remove(paramEditor);
  	box.remove(functionEditor);
		box.add(clipControl, 1);
		box.add(timeControl, 2);
  }
  
  /**
   * Sets the custom control panel. This can be any JPanel with GUI elements 
   * to control the external model at its source.
   *
   * @param panel the custom control panel
   */
  public void setCustomControl(JPanel panel) {
  	if (panel==customControl) return;
  	if (customControl!=null) {
  		customTitle.remove(customControl);
  		box.remove(customTitle);
  	}
  	customControl = panel;
  	if (customControl!=null) {
			if (customTitle==null) {
				customTitle = new JPanel(new BorderLayout());
			}
			customTitle.setBorder(BorderFactory.createTitledBorder("Data Source Control"));
			customTitle.add(customControl, BorderLayout.CENTER);
  		box.add(customTitle, 3);
  	}
  }
  
  /**
	 * Refreshes the GUI.
	 */
  protected void refreshGUI() {
  	super.refreshGUI();
  	if (model!=null) {
	  	DataModel dataModel = (DataModel)model;
			Object dataSource = dataModel.getSource();
			if (dataSource!=null && dataSource instanceof JPanel) {
				setCustomControl((JPanel)dataSource);
			}  		
  	}
  }


  
  // pig need to give instructions, do help
  

}
