package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.opensourcephysics.tools.FunctionEditor;
import org.opensourcephysics.tools.ToolsRes;
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
  	MouseAdapter listener = new MouseAdapter() {
  		@Override
  		public void mousePressed(MouseEvent e) {
  			clearSelection();
  		}
  	};
  	clipControl = new DataTrackClipControl(track);
  	clipControl.addMouseListenerToAll(listener);
  	timeControl = new DataTrackTimeControl(track);
  	timeControl.addMouseListener(listener);
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
   * Refreshes the time source.
   */
  protected void refreshTimeSource() {
  	timeControl.setTimeSourceToDataTrack(timeControl.isTimeSourceDataTrack());
  }
  
  @Override
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

  @Override
  protected void refreshInstructions(FunctionEditor source, boolean editing, int selectedColumn) {
    StyledDocument doc = instructions.getStyledDocument();
    Style style = doc.getStyle("blue");                                                //$NON-NLS-1$
    String s = TrackerRes.getString("ParticleDataTrackFunctionPanel.Instructions.General"); //$NON-NLS-1$
    if(!editing && hasInvalidExpressions()) {                            // error condition
      s = ToolsRes.getString("FunctionPanel.Instructions.BadCell");           //$NON-NLS-1$
      style = doc.getStyle("red");                                            //$NON-NLS-1$
    }
    instructions.setText(s);
    int len = instructions.getText().length();
    doc.setCharacterAttributes(0, len, style, false);
    revalidate();
  }
  
  @Override  
	protected void tabToNext(FunctionEditor editor) {
  	clipControl.requestFocusInWindow();
  }


  
}
