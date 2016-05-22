package org.opensourcephysics.cabrillo.tracker;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;

public class DataTrackTimeControl extends JPanel implements PropertyChangeListener {
	
	protected DataTrack dataTrack;
	private JRadioButton videoButton, dataButton;
	
	/**
	 * Constructor.
	 * 
	 * @param track the DataTrack
	 */
	public DataTrackTimeControl(DataTrack track) {
		super();
		dataTrack = track;
		track.addPropertyChangeListener(this);
		createGUI();
		refreshGUI();
	}
	
	/**
	 * Creates the GUI.
	 */
	protected void createGUI() {
		videoButton = new JRadioButton();
		videoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setTimeSourceToDataTrack(false);
			}			
		});
		dataButton = new JRadioButton();
		dataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setTimeSourceToDataTrack(true);
			}			
		});
		ButtonGroup group = new ButtonGroup();
		group.add(videoButton);
		group.add(dataButton);
		videoButton.setSelected(true);
		add(videoButton);
		add(dataButton);
	}
	
	/**
	 * Refreshes the GUI.
	 */
	protected void refreshGUI() {
		setBorder(BorderFactory.createTitledBorder(TrackerRes.getString("DataTrackTimeControl.Border.Title"))); //$NON-NLS-1$
		videoButton.setText(TrackerRes.getString("DataTrackTimeControl.Button.Video")); //$NON-NLS-1$
		dataButton.setText(TrackerRes.getString("DataTrackTimeControl.Button.Data")); //$NON-NLS-1$
		dataButton.setEnabled(dataTrack.isTimeDataAvailable());
		boolean dataSelected = ClipControl.isTimeSource(dataTrack);
		dataButton.setSelected(dataSelected);
		videoButton.setSelected(!dataSelected);
		// following line needed to display titled border correctly when a DataTrack is created
		FontSizer.setFonts(getBorder(), FontSizer.getLevel());
	}
	
	/**
	 * Sets the DataTrack time source flag.
	 * 
	 * @param isTrackTimeSource true to use DataTrack time, false to use video time
	 */
	protected void setTimeSourceToDataTrack(boolean isTrackTimeSource) {
		if (dataTrack.getVideoPanel()==null) return;
		VideoPlayer player = dataTrack.getVideoPanel().getPlayer();
		player.getClipControl().setTimeSource(isTrackTimeSource? dataTrack: null);
		player.refresh();
		if (dataTrack instanceof ParticleDataTrack) {
			((ParticleDataTrack)dataTrack).refreshInitialTime();
		}
	}
	
	/**
	 * Gets the DataTrack time source flag.
	 * 
	 * @return true if using DataTrack time
	 */
	protected boolean isTimeSourceDataTrack() {
		if (dataTrack.getVideoPanel()==null) return false;
		VideoPlayer player = dataTrack.getVideoPanel().getPlayer();
		return player.getClipControl().getTimeSource()==dataTrack;
	}
	
  @Override
  public Dimension getMaximumSize() {
  	Dimension dim = super.getMaximumSize();
  	dim.height = getPreferredSize().height;
    return dim;
  }

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		refreshGUI();
	}

}
