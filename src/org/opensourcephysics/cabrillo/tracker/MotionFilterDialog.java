/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;

/**
 * Dialog that selects and configures the {@link MotionFilter} applied to point mass
 * positions.
 *
 * @author D Brown, using Tracker Filter contribution
 */
@SuppressWarnings("serial")
public class MotionFilterDialog extends JDialog {

	private static final String FILTER_NONE = "none"; //$NON-NLS-1$
	private static final String FILTER_MOVING_AVG = "ma"; //$NON-NLS-1$
	private static final String FILTER_BUTTERWORTH = "butter"; //$NON-NLS-1$
	private static final String FILTER_SAV_GOLAY = "sg"; //$NON-NLS-1$
	
	private String urlButterworth = "https://en.wikipedia.org/wiki/Butterworth_filter";
	private String urlSG = "https://en.wikipedia.org/wiki/Savitzky%E2%80%93Golay_filter";
	private String urlZeroPhase = "https://community.sw.siemens.com/s/article/butterworth-filter-regular-and-zero-phase";

	protected TFrame frame;
	protected Integer panelID;
	private int htmlFontSize = 14;

	protected ArrayList<FilteredPointMass> targetMasses = new ArrayList<FilteredPointMass>();

	private JRadioButton noneButton, maButton, butterButton, sgButton;
	private TitledBorder choiceBorder, paramsBorder;
	private JTextPane infoPane;
	private NumberField rmsField;


	private JPanel choices, params, upper, rmsReadout;
	private JSpinner maWindowSpinner;
	private JSpinner butterOrderSpinner, butterCutoffSpinner;
	private JLabel butterRateLabel, rmsLabel;
	private JSpinner sgWindowSpinner, sgPolySpinner;

	private JButton okButton, cancelButton;

	private MotionFilter prevFilter;
	private boolean updating;

	public MotionFilterDialog(TrackerPanel panel) {
		super(panel.getTFrame(), true);
		frame = panel.getTFrame();
		panelID = panel.getID();
		createGUI();
		pack();
		okButton.requestFocusInWindow();
	}

	protected void setTargetMass(FilteredPointMass mass) {
		targetMasses.clear();
		targetMasses.add(mass);
		refreshGUI();
	}

	protected void setTargetMasses(ArrayList<FilteredPointMass> masses) {
		targetMasses.clear();
		targetMasses.addAll(masses);
		refreshGUI();
	}

	private void createGUI() {
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		
		choices = new JPanel(new GridLayout(4, 1, 0, 0));
		params = new JPanel(new GridLayout(4, 1, 0, 0));
		upper = new JPanel(new GridLayout(1, 2, 0, 0));
		upper.add(choices);
		upper.add(params);
		contentPane.add(upper, BorderLayout.NORTH);

		choiceBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
		Border empty = BorderFactory.createEmptyBorder(3, 2, 3, 2);
		choices.setBorder(BorderFactory.createCompoundBorder(empty, choiceBorder));
		paramsBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
		params.setBorder(BorderFactory.createCompoundBorder(empty, paramsBorder));
		
		ButtonGroup group = new ButtonGroup();
		Action chooser = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updating) return;
				refreshParams(e.getActionCommand());
				upper.revalidate();
				applyCurrent();
				refreshInfo();
			}
		};

		noneButton = makeRadio(group, chooser, FILTER_NONE);
		maButton = makeRadio(group, chooser, FILTER_MOVING_AVG);
		butterButton = makeRadio(group, chooser, FILTER_BUTTERWORTH);
		sgButton = makeRadio(group, chooser, FILTER_SAV_GOLAY);

		choices.add(noneButton);
		choices.add(maButton);
		choices.add(sgButton);
		choices.add(butterButton);
		
		maWindowSpinner = new MySpinner(new SpinnerNumberModel(5, 3, 99, 2));
		maWindowSpinner.addChangeListener(applyOnChange());
		sgWindowSpinner = new MySpinner(new SpinnerNumberModel(7, 5, 99, 2));
		sgWindowSpinner.addChangeListener(applyOnChange());
		sgPolySpinner = new MySpinner(new SpinnerNumberModel(2, 1, 6, 1));
		sgPolySpinner.addChangeListener(applyOnChange());
		butterOrderSpinner = new MySpinner(new SpinnerNumberModel(4, 1, 8, 1));
		butterOrderSpinner.addChangeListener(applyOnChange());
		butterCutoffSpinner = new MySpinner(new SpinnerNumberModel(6.0, 0.1, 1000.0, 0.5));
		butterCutoffSpinner.addChangeListener(applyOnChange());
		butterRateLabel = new JLabel("--"); //$NON-NLS-1$

		rmsField = new NumberField(0, 3);

		infoPane = new JTextPane();
		infoPane.setEditable(false);
		infoPane.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		infoPane.addHyperlinkListener(new HyperlinkListener() {
	    @Override
	    public void hyperlinkUpdate(HyperlinkEvent e) {
	        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
	          OSPDesktop.browse(e.getURL().toString());
	        }
	        else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {	        	
	        		infoPane.setToolTipText(butterButton.isSelected()? 
	        			e.getURL().toString():
	        			sgButton.isSelected()? urlSG: null);
	        }
	        else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
	        	infoPane.setToolTipText(null);
	        }
	    }
		});
		JScrollPane infoScroll = new JScrollPane(infoPane);
		infoPane.setText(TrackerRes.getString("FilterDialog.SavitzkyGolay.Description")); //$NON-NLS-1$
		contentPane.add(infoScroll, BorderLayout.CENTER);

		okButton = new JButton();
		okButton.setForeground(new Color(0, 0, 102));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton = new JButton();
		cancelButton.setForeground(new Color(0, 0, 102));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				revert();
				setVisible(false);
			}
		});
		JPanel buttonbar = new JPanel();
		buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
		buttonbar.add(okButton);
		buttonbar.add(cancelButton);
		contentPane.add(buttonbar, BorderLayout.SOUTH);

		refreshGUI();
	}

	private JRadioButton makeRadio(ButtonGroup g, Action a, String cmd) {
		JRadioButton b = new JRadioButton();
		b.setActionCommand(cmd);
		b.addActionListener(a);
		g.add(b);
		return b;
	}
	
	private JPanel getRMSReadout() {
		if (rmsReadout == null) {
			rmsReadout = new JPanel(new BorderLayout());
			Box box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			JLabel label = new JLabel(TrackerRes.getString("FilterDialog.Readout.RMS")); //$NON-NLS-1$
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));		
			box.add(label);
			rmsLabel = new JLabel();
			box.add(rmsLabel);
			rmsReadout.add(box, BorderLayout.CENTER);
		}
		return rmsReadout;
	}

	private JPanel getMovingAveragePanel() {
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JLabel label = new JLabel(TrackerRes.getString("FilterDialog.Param.Window")); //$NON-NLS-1$
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));		
		box.add(label);
		box.add(maWindowSpinner);
		JPanel p = new JPanel(new BorderLayout());
		p.add(box, BorderLayout.CENTER);
		FontSizer.setFont(p);
		return p;
	}

	private JPanel[] getButterworthPanels() {
		JPanel p1 = new JPanel(new BorderLayout());
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JLabel label = new JLabel(TrackerRes.getString("FilterDialog.Param.Order")); //$NON-NLS-1$
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));		
		box.add(label);
		box.add(butterOrderSpinner);
		p1.add(box, BorderLayout.CENTER);
		
		JPanel p2 = new JPanel(new BorderLayout());
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		label = new JLabel(TrackerRes.getString("FilterDialog.Param.Cutoff")); //$NON-NLS-1$
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));		
		box.add(label);
		box.add(butterCutoffSpinner);
		p2.add(box, BorderLayout.CENTER);
	
		JPanel p3 = new JPanel(new BorderLayout());
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		label = new JLabel(TrackerRes.getString("FilterDialog.Param.SampleRate")); //$NON-NLS-1$
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));		
		box.add(label);
		box.add(butterRateLabel);
		p3.add(box, BorderLayout.CENTER);
		
		return new JPanel[] {p1, p2, p3};
	}
	
	private JPanel[] getSavitzkyGolayPanels() {
		JPanel p1 = new JPanel(new BorderLayout());
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		JLabel label = new JLabel(TrackerRes.getString("FilterDialog.Param.Window")); //$NON-NLS-1$
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));		
		box.add(label);
		box.add(sgWindowSpinner);
		p1.add(box, BorderLayout.CENTER);
		FontSizer.setFont(p1);
		
		JPanel p2 = new JPanel(new BorderLayout());
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		label = new JLabel(TrackerRes.getString("FilterDialog.Param.PolyOrder")); //$NON-NLS-1$
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));		
		box.add(label);
		box.add(sgPolySpinner);
		p2.add(box, BorderLayout.CENTER);
		FontSizer.setFont(p2);
		
		return new JPanel[] {p1, p2};
	}


	private ChangeListener applyOnChange() {
		return new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (updating) return;
				applyCurrent();
				refreshInfo();
			}
		};
	}

	private double estimateSampleRateHz() {
		try {
			TrackerPanel panel = frame.getTrackerPanelForID(panelID);
			VideoPlayer player = panel.getPlayer();
			double meanMs = player.getMeanStepDuration();
			if (meanMs > 0) return 1000.0 / meanMs;
		} catch (Exception ignored) {}
		return 30.0;
	}

	private void applyCurrent() {
		if (targetMasses.isEmpty()) return;
		MotionFilter f = buildFilterFromUI();
		for (FilteredPointMass m : targetMasses) {
			m.getFormatMap();
			m.setMotionFilter(f == null ? null : f.copy());
			rmsField.setValue(m.getRMSDev());
			rmsLabel.setText(rmsField.getText()); //$NON-NLS-1$
		}
	}

	private MotionFilter buildFilterFromUI() {
		if (maButton.isSelected()) {
			int w = (Integer) maWindowSpinner.getValue();
			return new MovingAverageFilter(w);
		} else if (butterButton.isSelected()) {
			int order = (Integer) butterOrderSpinner.getValue();
			double cutoff = ((Number) butterCutoffSpinner.getValue()).doubleValue();
			double fs = estimateSampleRateHz();
			return new ButterworthFilter(order, cutoff, fs);
		} else if (sgButton.isSelected()) {
			int w = (Integer) sgWindowSpinner.getValue();
			int p = (Integer) sgPolySpinner.getValue();
			return new SavitzkyGolayFilter(w, p);
		}
		return null;
	}
	
	private void refreshParams(String filterName) {
		if (filterName == null)
			filterName = FILTER_NONE;
		params.removeAll();
		switch(filterName) {
		case FILTER_NONE:
			params.add(new JPanel());
			params.add(new JPanel());
			params.add(new JPanel());
			params.add(getRMSReadout());
			break;
		case FILTER_MOVING_AVG:
			params.add(getMovingAveragePanel());
			params.add(new JPanel());
			params.add(new JPanel());
			params.add(getRMSReadout());
			break;
		case FILTER_BUTTERWORTH:
			JPanel[] panels = getButterworthPanels();
			params.add(panels[0]);
			params.add(panels[1]);
			params.add(panels[2]);
			params.add(getRMSReadout());
			break;
		case FILTER_SAV_GOLAY:
			panels = getSavitzkyGolayPanels();
			params.add(panels[0]);
			params.add(panels[1]);
			params.add(new JPanel());
			params.add(getRMSReadout());	
		}
		FontSizer.setFonts(params, FontSizer.getLevel());

	}

	private void refreshGUI() {
		if (targetMasses.size() == 0)
			return;
		// FilterDialog.Title.Text
		String title = TrackerRes.getString("FilterDialog.Title.Text");
		TTrack source = TTrack.getTrack(targetMasses.get(0).sourceID);
		setTitle(title+" \""+source.getName()+"\""); //$NON-NLS-1$ //$NON-NLS-2$
		choiceBorder.setTitle(TrackerRes.getString("FilterDialog.TitledBorder.Choose")+":"); //$NON-NLS-1$
		paramsBorder.setTitle(TrackerRes.getString("FilterDialog.TitledBorder.Params")+":"); //$NON-NLS-1$
		okButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
		cancelButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
		noneButton.setText(TrackerRes.getString("FilterDialog.None.Name")); //$NON-NLS-1$
		maButton.setText(TrackerRes.getString("FilterDialog.MovingAverage.Name")); //$NON-NLS-1$
		butterButton.setText(TrackerRes.getString("FilterDialog.Butterworth.Name")); //$NON-NLS-1$
		sgButton.setText(TrackerRes.getString("FilterDialog.SavitzkyGolay.Name")); //$NON-NLS-1$
		if (butterRateLabel != null) {
			butterRateLabel.setText(String.format("%.2f Hz", estimateSampleRateHz())); //$NON-NLS-1$
		}
	}

	private void refreshInfo() {
		infoPane.setContentType("text/html");
		int fontSize = Math.round(Math.round(FontSizer.getFactor() * htmlFontSize));
		String s = "<html><body style='font-family: Arial; font-size: "+fontSize+"pt;'>";
		if (noneButton.isSelected()) {
			s += TrackerRes.getString("FilterDialog.None.Description"); //$NON-NLS-1$
		} else if (maButton.isSelected()) {
			s += TrackerRes.getString("FilterDialog.MovingAverage.Description"); //$NON-NLS-1$
		} else if (butterButton.isSelected()) {
			s += TrackerRes.getString("FilterDialog.Butterworth.Description"); //$NON-NLS-1$
			s += " For more, see <a href='"+urlButterworth+"'>Wikipedia</a>"; //$NON-NLS-1$
			s += " or <a href='"+urlZeroPhase+"'>Zero-phase</a>"; //$NON-NLS-1$
		} else if (sgButton.isSelected()) {
			s += TrackerRes.getString("FilterDialog.SavitzkyGolay.Description"); //$NON-NLS-1$
			s += " For more, see <a href='"+urlSG+"'>Wikipedia</a>";
		}
		s += "</body></html>";
		infoPane.setText(s);
	}

	private void initialize() {
		updating = true;
		try {
			FilteredPointMass fpm = targetMasses.isEmpty() ? null : targetMasses.get(0);
			MotionFilter current = fpm == null? null : fpm.getMotionFilter();
			prevFilter = current == null ? null : current.copy();			
			loadParamsForFilter(current);
			if (fpm != null) {
				rmsField.setValue(fpm.getRMSDev());
				rmsLabel.setText(rmsField.getText()); //$NON-NLS-1$
			}
		} finally {
			updating = false;
		}
		refreshInfo();
	}

	private void loadParamsForFilter(MotionFilter f) {

		if (f == null) {
			noneButton.setSelected(true);
			refreshParams(FILTER_NONE);
		}
		else if (f instanceof MovingAverageFilter) {
			MovingAverageFilter ma = (MovingAverageFilter) f;
			maWindowSpinner.setValue(ma.getWindow());
			maButton.setSelected(true);
			refreshParams(FILTER_MOVING_AVG);
		} else if (f instanceof ButterworthFilter) {
			ButterworthFilter bw = (ButterworthFilter) f;
			butterOrderSpinner.setValue(bw.getOrder());
			butterCutoffSpinner.setValue(bw.getCutoffHz());
			if (butterRateLabel != null)
				butterRateLabel.setText(String.format("%.2f Hz", bw.getSampleRateHz())); //$NON-NLS-1$
			butterButton.setSelected(true);
			refreshParams(FILTER_BUTTERWORTH);
		} else if (f instanceof SavitzkyGolayFilter) {
			SavitzkyGolayFilter sg = (SavitzkyGolayFilter) f;
			sgWindowSpinner.setValue(sg.getWindow());
			sgPolySpinner.setValue(sg.getPolyOrder());
			sgButton.setSelected(true);
			refreshParams(FILTER_SAV_GOLAY);
		}
	}

	private void revert() {
		for (FilteredPointMass m : targetMasses) {
			m.setMotionFilter(prevFilter == null ? null : prevFilter.copy());
		}
	}

	@Override
	public void setVisible(boolean vis) {
		if (vis)
			initialize();
		if (getLocation().x == 0) {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - getBounds().width) / 2;
			int y = (dim.height - getBounds().height) / 2;
			setLocation(x, y);
		}
		super.setVisible(vis);
	}

	protected void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		FontSizer.setFonts(maWindowSpinner, level);
		FontSizer.setFonts(butterOrderSpinner, level);
		FontSizer.setFonts(butterCutoffSpinner, level);
		FontSizer.setFonts(butterRateLabel, level);
		FontSizer.setFonts(sgWindowSpinner, level);
		FontSizer.setFonts(sgPolySpinner, level);

		FontSizer.setFonts(choiceBorder, level);
		FontSizer.setFonts(paramsBorder, level);
		
		// set preferred size
		int w = (int) (320 * (1 + level * .35));
		int h = (int) (140 * (1 + level * .35));
		infoPane.setPreferredSize(new Dimension(w, h));
		pack();
	}

	@Override
	public void dispose() {
		panelID = null;
		frame = null;
		super.dispose();
	}
	
	class MySpinner extends JSpinner {
		
		public MySpinner(SpinnerNumberModel model) {
			super(model);
		}
		
		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
		
		@Override
		public Dimension getPreferredSize() {
			if (this == maWindowSpinner)
				return getMinimumSize();
			return maWindowSpinner.getMinimumSize();
		}

	};

}
