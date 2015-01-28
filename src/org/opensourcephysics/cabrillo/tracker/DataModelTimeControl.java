package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.opensourcephysics.media.core.VideoPlayer;

public class DataModelTimeControl extends JPanel implements PropertyChangeListener {
	
	protected DataModel model;
	private JRadioButton videoButton, dataButton;
	private double videoFrameDuration, videoStartTime;
	
	public DataModelTimeControl(DataModel model) {
		super();
//		super(new BorderLayout());
		this.model = model;
		model.addPropertyChangeListener("dataclip", this); //$NON-NLS-1$
		createGUI();
		refreshGUI();
	}
	
	protected void createGUI() {
		videoButton = new JRadioButton();
		videoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	  		// set video frame duration and start time to saved values
				model.setDataTime(false);
				VideoPlayer player = model.trackerPanel.getPlayer();
				player.getClipControl().setFrameDuration(videoFrameDuration);
				player.getVideoClip().setStartTime(videoStartTime);
				player.refresh();
			}			
		});
		dataButton = new JRadioButton();
		dataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// save current video frame duration and start time
				VideoPlayer player = model.trackerPanel.getPlayer();
				videoFrameDuration = player.getMeanStepDuration()/player.getVideoClip().getStepSize();
				videoStartTime = player.getFrameTime(0);
				// set data time flag
				model.setDataTime(true);
			}			
		});
		ButtonGroup group = new ButtonGroup();
		group.add(videoButton);
		group.add(dataButton);
		videoButton.setSelected(true);
		add(videoButton);
		add(dataButton);
	}
	
	protected void refreshGUI() {
		setBorder(BorderFactory.createTitledBorder("Time Basis"));
		videoButton.setText("Video Time");
		dataButton.setText("Data Time");
		dataButton.setEnabled(model.isDataTimeEnabled());
	}
	
  @Override
  public Dimension getMaximumSize() {
  	Dimension dim = super.getMaximumSize();
  	dim.height = getPreferredSize().height;
    return dim;
  }

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		refreshGUI();
	}
  

}
