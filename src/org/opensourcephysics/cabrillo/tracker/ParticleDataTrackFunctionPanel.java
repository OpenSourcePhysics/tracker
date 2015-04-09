package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.opensourcephysics.tools.UserFunctionEditor;

/**
 * A function panel for a ParticleDataTrack.
 *
 * @author Douglas Brown
 */
public class ParticleDataTrackFunctionPanel extends ModelFunctionPanel {
	
	private DataTrackClipControl clipControl;
	private DataTrackTimeControl timeControl;
	private JPanel customControl;
	private JPanel customTitle;

  /**
   * Constructor.
   *
   * @param editor the user function editor
   * @param track a ParticleDataTrack
   */
  public ParticleDataTrackFunctionPanel(ParticleDataTrack track) {
  	// must pass a UserFunctionEditor (never used) to the superclass
  	super(new UserFunctionEditor(), track);
  	model = track;
  	setName(track.getName());  	
  	
  	// create and assemble GUI
  	clipControl = new DataTrackClipControl(track);
  	timeControl = new DataTrackTimeControl(track);
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
			customTitle.add(customControl, BorderLayout.CENTER);
  		box.add(customTitle, 3);
  		refreshGUI();
  	}
  }
  
  /**
	 * Refreshes the GUI.
	 */
  protected void refreshGUI() {
  	super.refreshGUI();
  	if (model!=null) {
	  	ParticleDataTrack dataTrack = (ParticleDataTrack)model;
			Object dataSource = dataTrack.getSource();
			if (dataSource!=null && dataSource instanceof JPanel) {
				setCustomControl((JPanel)dataSource);
			}  		
  	}
  	if (customControl!=null) {
  		String title = TrackerRes.getString("ParticleDataTrackFunctionPanel.Border.Title"); //$NON-NLS-1$
			customTitle.setBorder(BorderFactory.createTitledBorder(title));
  	}
  }


  
  // pig need to give instructions, do help
  

}
