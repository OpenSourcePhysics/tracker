package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.opensourcephysics.tools.FunctionEditor;
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
	 * @param track  a ParticleDataTrack
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
	}

	/**
	 * Creates the GUI.
	 */
	@Override
	protected void createGUI() {
		super.createGUI();
		box.remove(paramEditor);
		box.remove(functionEditor);
		box.add(clipControl, 1);
		box.add(timeControl, 2);
	}

	@Override
	protected void refreshGUI() {
		super.refreshGUI();
		if (model != null) {
			ParticleDataTrack dataTrack = (ParticleDataTrack) model;
			Object dataSource = dataTrack.getSource();
			if (dataSource != null && dataSource instanceof JPanel) {
				setCustomControl((JPanel) dataSource);
			}
		}
		if (customControl != null) {
			String title = TrackerRes.getString("ParticleDataTrackFunctionPanel.Border.Title"); //$NON-NLS-1$
			customTitle.setBorder(BorderFactory.createTitledBorder(title));
		}
	}

	/**
	 * Sets the custom control panel. This can be any JPanel with GUI elements to
	 * control the external model at its source.
	 *
	 * @param panel the custom control panel
	 */
	public void setCustomControl(JPanel panel) {
		if (panel == customControl)
			return;
		if (customControl != null) {
			customTitle.remove(customControl);
			box.remove(customTitle);
		}
		customControl = panel;
		if (customControl != null) {
			if (customTitle == null) {
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
	protected String getCustomInstructions(FunctionEditor source, int selectedColumn) {
		// BH 2021.12.19 was not checking for circular errors
		return TrackerRes.getString("ParticleDataTrackFunctionPanel.Instructions.General"); //$NON-NLS-1$
	}

	@Override
	protected void tabToNext(FunctionEditor editor) {
		clipControl.requestFocusInWindow();
	}

}
