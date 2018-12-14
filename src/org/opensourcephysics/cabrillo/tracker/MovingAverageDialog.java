package org.opensourcephysics.cabrillo.tracker;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to specify parameters of moving average filter
 */
public class MovingAverageDialog extends JDialog {
	private JButton buttonOK, buttonCancel;
	private TallSpinner stepSpinner;
	private JLabel stepSpinnerLabel;

	public boolean decided = false;
	public int result = 1;
	private TTrack targetTrack;
	private TrackerPanel trackerPanel;

	public MovingAverageDialog(TrackerPanel tp, TTrack track) {
		super(JOptionPane.getFrameForComponent(tp), true);

		trackerPanel = tp;
		targetTrack = track;


		JPanel contentPane = new JPanel();

		stepSpinnerLabel = new JLabel(TrackerRes.getString("MovingAverageDialog.PointsToAverage"));
		//stepSpinnerLabel.setText("Points to average:");



		buttonOK = new JButton(TrackerRes.getString("MovingAverageDialog.OK"));
		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				result = (Integer) stepSpinner.getValue();
				setVisible(false);
				if (targetTrack instanceof PointMass){
					applyToPointMass();
				}
			}
		});

		buttonCancel = new JButton(TrackerRes.getString("MovingAverageDialog.Cancel"));
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});
		stepSpinner = new TallSpinner(
				new SpinnerNumberModel(2, 1, 100, 1),
				buttonOK
		);

		contentPane.add(stepSpinnerLabel);
		contentPane.add(stepSpinner);
		contentPane.add(buttonOK);
		contentPane.add(buttonCancel);
		add(contentPane, BorderLayout.SOUTH);
		pack();
	}

	private void applyToPointMass(){
		PointMass pointMass = (PointMass) targetTrack;
		pointMass.applyMovingAverage(result);
	}
}
