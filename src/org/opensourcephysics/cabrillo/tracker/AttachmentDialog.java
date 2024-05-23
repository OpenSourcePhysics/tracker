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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.IntegerField;
import org.opensourcephysics.tools.FontSizer;

/**
 * This displays and sets point attachments.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class AttachmentDialog extends JDialog implements PropertyChangeListener {

	// instance fields
	protected int trackID;
	protected TFrame frame;
	protected Integer panelID;

	protected boolean isVisible;
	protected JButton closeButton, helpButton;
	protected ArrayList<TTrack> attachableTracks;
	protected JTable table;
	protected int cellheight = 28; // depends on font level
	protected JComboBox<Object> rendererDropdown, editorDropdown;
	protected JComboBox<TTrack> measuringToolDropdown;
	protected TTrack dummyMass;
	protected Icon dummyIcon = new ShapeIcon(null, 21, 16);
	protected JScrollPane scrollPane;
	protected AttachmentCellRenderer attachmentCellRenderer = new AttachmentCellRenderer();
	protected TTrackRenderer trackCellRenderer = new TTrackRenderer();
	/**
	 * in JavaScript, editor must have its own renderer
	 */
	protected TTrackRenderer trackEditorRenderer = new TTrackRenderer();
	protected TTrackRenderer toolRenderer = new TTrackRenderer();
	protected JPanel attachmentsPanel, circleFitterPanel, circleFitterStartStopPanel;
	protected JRadioButton stepsButton, tracksButton;
	protected JCheckBox relativeCheckbox;
	protected IntegerField startField, countField;
	protected JLabel startLabel, countLabel;
	protected boolean refreshing;
	private ComponentListener myFollower;

	private static final String[] panelProps = new String[] { TrackerPanel.PROPERTY_TRACKERPANEL_TRACK,
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, };

	/**
	 * Constructs an AttachmentDialog.
	 *
	 * @param track the measuring tool
	 */
	public AttachmentDialog(TTrack track) {
		super(JOptionPane.getFrameForComponent(track.tp), false);
		panelID = track.tp.getID();
		frame = track.tframe;
		createGUI();
		setMeasuringTool(track);
		refreshDropdowns();
		track.tp.addListeners(panelProps, this);
		myFollower = frame.addFollower(this, null);
		frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
		refreshGUI();
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TFrame.PROPERTY_TFRAME_TAB:
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			if (!frame.isRemovingAll() && panelID != null && e.getNewValue() == trackerPanel) {
				setVisible(isVisible);
			} else {
				boolean vis = isVisible;
				setVisible(false);
				isVisible = vis;
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			TTrack deleted = (TTrack) e.getOldValue();
			if (deleted != null) {
				deleted.removeListenerNCF(this);
				TTrack measuringTool = TTrack.getTrack(trackID);
				if (measuringTool != null) {
					if (measuringTool != deleted) {
						TTrack[] attachments = measuringTool.getAttachments();
						for (int i = 0; i < attachments.length; i++) {
							if (deleted == attachments[i] || deleted == measuringTool) {
								attachments[i] = null;
							}
						}
						measuringTool.refreshAttachments();
					} else { // measuring tool has been deleted
						trackID = 0;
					}
				}
			}
			refreshDropdowns();
			refreshGUI();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK:
			if (e.getNewValue() != null) { // $NON-NLS-1$
				TTrack track = (TTrack) e.getNewValue();
				for (int i = 0; i < measuringToolDropdown.getItemCount(); i++) {
					if (track == measuringToolDropdown.getItemAt(i)) {
						measuringToolDropdown.setSelectedIndex(i);
						break;
					}
				}
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			for (TTrack t : TTrack.getValues()) {
				t.removeListenerNCF(this);
			}
			refreshDropdowns();
			refreshGUI();
			break;
		case CircleFitter.PROPERTY_CIRCLEFITTER_DATAPOINT:
			TTrack measuringTool = TTrack.getTrack(trackID);
			measuringTool.refreshAttachments();
			DefaultTableModel dm = (DefaultTableModel) table.getModel();
			dm.fireTableDataChanged();
			break;
		default:
			refreshGUI();
			break;
		}
	}

	/**
	 * Overrides JDialog setVisible method.
	 *
	 * @param vis true to show this inspector
	 */
	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		isVisible = vis;
	}

	/**
	 * Disposes of this dialog.
	 */
	@Override
	public void dispose() {
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			trackerPanel.removeListeners(panelProps, this);
			for (TTrack p : attachableTracks) {
				p.removeListenerNCF(this);
			}
			attachableTracks.clear();
			dummyMass.delete();
			dummyMass = null;
			TTrack measuringTool = TTrack.getTrack(trackID);
			if (measuringTool.ttype == TTrack.TYPE_CIRCLEFITTER) {
				measuringTool.removePropertyChangeListener(CircleFitter.PROPERTY_CIRCLEFITTER_DATAPOINT, this); // $NON-NLS-1$
			}
			if (frame != null) {
				frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this);
				frame.removeComponentListener(myFollower);
				myFollower = null;
			}
			trackerPanel.attachmentDialog = null;
			panelID = null;
			frame = null;
		}
		super.dispose();
	}

//_____________________________ private methods ____________________________

	/**
	 * Creates the visible components of this panel.
	 */
	private void createGUI() {
//  	setResizable(false);
		// create GUI components
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		// put attachments panel in content pane CENTER
		attachmentsPanel = new JPanel(new BorderLayout());
		contentPane.add(attachmentsPanel, BorderLayout.CENTER);

		// put measuring tool dropdown in attachments panel NORTH
		JPanel north = new JPanel();
		north.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		measuringToolDropdown = new JComboBox<>();
		measuringToolDropdown.setRenderer(toolRenderer);
		measuringToolDropdown.addActionListener((e) -> {
			TTrack tool = (TTrack) measuringToolDropdown.getSelectedItem();
			TTrack measuringTool = TTrack.getTrack(trackID);
			if (tool == measuringTool)
				return;
			setMeasuringTool(tool);
		});
		north.add(measuringToolDropdown);
		attachmentsPanel.add(north, BorderLayout.NORTH);

		dummyMass = new PointMass();

		rendererDropdown = new JComboBox<>(new AttachmentComboBoxModel());
		rendererDropdown.setRenderer(trackCellRenderer);
		editorDropdown = new JComboBox<>(new AttachmentComboBoxModel());
		editorDropdown.setRenderer(trackCellRenderer);

		table = new JTable(new AttachmentTableModel()) {
			@Override
			public void setFont(Font font) {
				super.setFont(font);
				cellheight = font.getSize() + 16;
				setRowHeight(cellheight);
				int w = (int) (60 * (1 + FontSizer.getLevel() * 0.3));
				getColumnModel().getColumn(0).setPreferredWidth(w);
				getColumnModel().getColumn(1).setPreferredWidth(2 * w);
				getTableHeader().setPreferredSize(new Dimension(w, cellheight));
			}
		};
		attachmentCellRenderer = new AttachmentCellRenderer();
		table.setDefaultRenderer(TTrack.class, attachmentCellRenderer);
		table.setDefaultRenderer(String.class, attachmentCellRenderer);
		table.setRowHeight(cellheight);

		TableCellEditor editor = new AttachmentCellEditor();
		table.getColumnModel().getColumn(1).setCellEditor(editor);
		scrollPane = new JScrollPane(table) {
			@Override
			public Dimension getPreferredSize() {
				Dimension dim = super.getPreferredSize();
				int cellCount = Math.max(4, table.getRowCount() + 1);
				cellCount = Math.min(10, cellCount);
				dim.height = cellCount * cellheight + 8;
				dim.width = table.getPreferredSize().width + 20;
				return dim;
			}
		};

		// put table in attachments panel CENTER
		JPanel center = new JPanel(new GridLayout(1, 1));
		attachmentsPanel.add(center, BorderLayout.CENTER);
		center.add(scrollPane);
		center.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		// steps and tracks buttons
		stepsButton = new JRadioButton();
		tracksButton = new JRadioButton();
		Action tracksOrStepsAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (refreshing)
					return;
				CircleFitter fitter = (CircleFitter) TTrack.getTrack(trackID);
				fitter.attachToSteps = !tracksButton.isSelected();
				fitter.refreshAttachments();
				refreshGUI();
				DefaultTableModel dm = (DefaultTableModel) table.getModel();
				dm.fireTableDataChanged();
			}
		};
		stepsButton.addActionListener(tracksOrStepsAction);
		tracksButton.addActionListener(tracksOrStepsAction);
		ButtonGroup group = new ButtonGroup();
		group.add(stepsButton);
		group.add(tracksButton);
		tracksButton.setSelected(true);

		// relative button
		relativeCheckbox = new JCheckBox();
		relativeCheckbox.setSelected(false);
		relativeCheckbox.addActionListener((e) -> {
			if (refreshing)
				return;
			CircleFitter fitter = (CircleFitter) TTrack.getTrack(trackID);
			fitter.isRelativeFrameNumbers = relativeCheckbox.isSelected();
			refreshFieldsAndButtons(fitter);
			fitter.refreshAttachments();
			refreshGUI();
		});

		// range action, listener and fields
		final Action frameRangeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CircleFitter fitter = (CircleFitter) TTrack.getTrack(trackID);
				fitter.setAttachmentStartFrame(startField.getIntValue());
				fitter.setAttachmentFrameCount(countField.getIntValue());
				refreshFieldsAndButtons(fitter);
				fitter.refreshAttachments();
				DefaultTableModel dm = (DefaultTableModel) table.getModel();
				dm.fireTableDataChanged();
				TFrame.repaintT(fitter.tp);
			}
		};

		FocusListener frameRangeFocusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.getSource() == startField && startField.getBackground() != Color.yellow)
					return;
				if (e.getSource() == countField && countField.getBackground() != Color.yellow)
					return;
				frameRangeAction.actionPerformed(null);
			}
		};
		startField = new IntegerField(3);
		startField.addActionListener(frameRangeAction);
		startField.addFocusListener(frameRangeFocusListener);
		countField = new IntegerField(2);
		countField.addActionListener(frameRangeAction);
		countField.addFocusListener(frameRangeFocusListener);

		startLabel = new JLabel();
		startLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		countLabel = new JLabel();
		countLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

		// put circleFitter panel in attachments panel SOUTH
		circleFitterPanel = new JPanel(new BorderLayout());
		circleFitterPanel.setBorder(BorderFactory.createTitledBorder("")); //$NON-NLS-1$

		// put steps and tracks buttons in circleFitterPanel NORTH
		JPanel buttonbar = new JPanel();
		circleFitterPanel.add(buttonbar, BorderLayout.NORTH);
		buttonbar.add(stepsButton);
		buttonbar.add(tracksButton);

		// create circleFitterStartStopPanel
		circleFitterStartStopPanel = new JPanel(new BorderLayout());
		Border empty = BorderFactory.createEmptyBorder(0, 4, 0, 4);
		Border etched = BorderFactory.createEtchedBorder();
		circleFitterStartStopPanel.setBorder(BorderFactory.createCompoundBorder(empty, etched));

		// put start and end frame controls in circleFitterStartStopPanel CENTER
		buttonbar = new JPanel();
		buttonbar.add(startLabel);
		buttonbar.add(startField);
//    buttonbar.add(startSpinner);
		buttonbar.add(countLabel);
		buttonbar.add(countField);
//    buttonbar.add(endSpinner);
		circleFitterStartStopPanel.add(buttonbar, BorderLayout.CENTER);

		// put relative checkbox in circleFitterStartStopPanel SOUTH
		buttonbar = new JPanel();
		circleFitterStartStopPanel.add(buttonbar, BorderLayout.SOUTH);
		buttonbar.add(relativeCheckbox);

		// help and close buttons
		helpButton = new JButton();
		helpButton.setForeground(new Color(0, 0, 102));
		helpButton.addActionListener((e) -> {
			TTrack measuringTool = TTrack.getTrack(trackID);
			String keyword = measuringTool == null ? "circle" : //$NON-NLS-1$
			measuringTool.ttype == TTrack.TYPE_PROTRACTOR ? "protractor" : //$NON-NLS-1$
			measuringTool.ttype == TTrack.TYPE_TAPEMEASURE ? "tape" : "circle"; //$NON-NLS-1$ //$NON-NLS-2$
			frame.showHelp(keyword + "#attach", 0); //$NON-NLS-1$
		});
		closeButton = new JButton();
		closeButton.setForeground(new Color(0, 0, 102));
		closeButton.addActionListener((e) -> {
			setVisible(false);
		});

		// put help and close button in content pane SOUTH
		buttonbar = new JPanel();
		contentPane.add(buttonbar, BorderLayout.SOUTH);
		buttonbar.add(helpButton);
		buttonbar.add(closeButton);
	}

	/**
	 * Sets the measuring tool.
	 */
	protected void setMeasuringTool(TTrack tool) {
		TTrack measuringTool = TTrack.getTrack(trackID);
		if (measuringTool != null && measuringTool.ttype == TTrack.TYPE_CIRCLEFITTER) {
			// BH! was "!= null" but this is unique to CircleFitter
			measuringTool.removePropertyChangeListener(CircleFitter.PROPERTY_CIRCLEFITTER_DATAPOINT, this); // $NON-NLS-1$
		}

		measuringTool = tool;
		trackID = measuringTool.getID();
		if (tool.ttype == TTrack.TYPE_CIRCLEFITTER)
			measuringTool.addPropertyChangeListener(CircleFitter.PROPERTY_CIRCLEFITTER_DATAPOINT, this); // $NON-NLS-1$
		measuringTool.refreshAttachments();
		refreshDropdowns();
		if (measuringTool.ttype == TTrack.TYPE_CIRCLEFITTER) {
			CircleFitter fitter = (CircleFitter) measuringTool;
			refreshFieldsAndButtons(fitter);
		}
		DefaultTableModel dm = (DefaultTableModel) table.getModel();
		dm.fireTableDataChanged();
		refreshGUI();
	}

	/**
	 * Refreshes the attachment and measuring tool dropdowns.
	 */
	protected void refreshDropdowns() {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		if (attachableTracks == null)
			attachableTracks = new ArrayList<TTrack>();
		else
			attachableTracks.clear();
		attachableTracks.addAll(trackerPanel.getDrawables(PointMass.class));
		attachableTracks.addAll(trackerPanel.getDrawables(RGBRegion.class));
		for (TTrack p : attachableTracks) {
			p.removeListenerNCF((PropertyChangeListener) this);
		}
		TTrack measuringTool = TTrack.getTrack(trackID);
		if (measuringTool != null && measuringTool.ttype == TTrack.TYPE_TAPEMEASURE) {
			// can't attach calibration stick to models--creates circular dependency
			TapeMeasure tape = (TapeMeasure) measuringTool;
			if (tape.isStickMode()) {
				attachableTracks.removeAll(trackerPanel.getDrawablesTemp(ParticleModel.class));
			}
		}
		for (TTrack p : attachableTracks) {
			p.addListenerNCF(this);
		}
		FontSizer.setFonts(rendererDropdown, FontSizer.getLevel());
		rendererDropdown.setModel(new AttachmentComboBoxModel());
		FontSizer.setFonts(editorDropdown, FontSizer.getLevel());
		editorDropdown.setModel(new AttachmentComboBoxModel());
		FontSizer.setFonts(measuringToolDropdown, FontSizer.getLevel());
		java.util.Vector<TTrack> tools = new java.util.Vector<TTrack>();
		for (TTrack track : trackerPanel.getTracksTemp()) {
			switch (track.ttype) {
			case TTrack.TYPE_TAPEMEASURE:
			case TTrack.TYPE_PROTRACTOR:
			case TTrack.TYPE_CIRCLEFITTER:
				tools.add(track);
				break;
			}
		}
		trackerPanel.clearTemp();
		for (TTrack p : tools) {
			p.removeListenerNCF(this);
			p.addListenerNCF(this);
		}
		measuringToolDropdown.setModel(new DefaultComboBoxModel<TTrack>(tools));
		if (!tools.isEmpty() && measuringTool != null) {
			measuringToolDropdown.setSelectedItem(measuringTool);
		} else {
			// measuring tool is null, so set it to first in list, if any
			for (TTrack next : tools) {
				setMeasuringTool(next);
				break;
			}
		}
	}

	/**
	 * Refreshes the start and end fields based on the state of a CircleFitter. Also
	 * refreshes the button state.
	 * 
	 * @param fitter the CircleFitter
	 */
	protected void refreshFieldsAndButtons(CircleFitter fitter) {
		if (fitter.attachToSteps && fitter.isRelativeFrameNumbers) {
			startField.applyPattern("+#;-#"); //$NON-NLS-1$
		} else {
			startField.applyPattern("#;-#"); //$NON-NLS-1$
		}
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		int min = fitter.isRelativeFrameNumbers ? 1 - trackerPanel.getPlayer().getVideoClip().getFrameCount()
				: trackerPanel.getPlayer().getVideoClip().getFirstFrameNumber();
		int max = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
		startField.setMaxValue(max);
		startField.setMinValue(min);
		startField.setIntValue(fitter.isRelativeFrameNumbers ? fitter.relativeStart : fitter.absoluteStart);
		countField.setMaxValue(CircleFitter.maxDataPointCount);
		countField.setMinValue(1);
		countField.setIntValue(fitter.getAttachmentFrameCount());

		refreshing = true;
		stepsButton.setSelected(fitter.attachToSteps);
		relativeCheckbox.setSelected(fitter.isRelativeFrameNumbers);
		refreshing = false;
	}

	/**
	 * Updates this dialog to show the system's current attachments.
	 */
	protected void refreshGUI() {
		setTitle(TrackerRes.getString("AttachmentInspector.Title")); //$NON-NLS-1$
		helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		dummyMass.setName(TrackerRes.getString("DynamicSystemInspector.ParticleName.None")); //$NON-NLS-1$
		startLabel.setText(TrackerRes.getString("AttachmentInspector.Label.StartFrame")); //$NON-NLS-1$
		countLabel.setText(TrackerRes.getString("AttachmentInspector.Label.FrameCount")); //$NON-NLS-1$
		stepsButton.setText(TrackerRes.getString("AttachmentInspector.Button.Steps")); //$NON-NLS-1$
		tracksButton.setText(TrackerRes.getString("AttachmentInspector.Button.Tracks")); //$NON-NLS-1$
		relativeCheckbox.setText(TrackerRes.getString("AttachmentInspector.Checkbox.Relative")); //$NON-NLS-1$
		stepsButton.setToolTipText(TrackerRes.getString("AttachmentInspector.Button.Steps.Tooltip")); //$NON-NLS-1$
		tracksButton.setToolTipText(TrackerRes.getString("AttachmentInspector.Button.Tracks.Tooltip")); //$NON-NLS-1$
		relativeCheckbox.setToolTipText(TrackerRes.getString("AttachmentInspector.Checkbox.Relative.Tooltip")); //$NON-NLS-1$
		TitledBorder border = (TitledBorder) circleFitterPanel.getBorder();
		border.setTitle(TrackerRes.getString("AttachmentInspector.Border.Title.AttachTo")); //$NON-NLS-1$

		// refresh layout to include/exclude circle fitter items
		boolean hasCircleFitterPanel = attachmentsPanel.getComponentCount() > 2;
		boolean hasStartStopPanel = circleFitterPanel.getComponentCount() > 1;
		boolean changedLayout = false;
		TTrack measuringTool = TTrack.getTrack(trackID);
		if (measuringTool.ttype == TTrack.TYPE_CIRCLEFITTER) {
			// put circleFitter panel in attachments panel SOUTH
			changedLayout = !hasCircleFitterPanel;
			attachmentsPanel.add(circleFitterPanel, BorderLayout.SOUTH);

			CircleFitter fitter = (CircleFitter) measuringTool;
			if (!fitter.attachToSteps) {
				changedLayout = changedLayout || hasStartStopPanel;
				circleFitterPanel.remove(circleFitterStartStopPanel);
			} else {
				if (fitter.isRelativeFrameNumbers) {
//      		startLabel.setText(TrackerRes.getString("AttachmentInspector.Label.Offset")); //$NON-NLS-1$
				}
				changedLayout = changedLayout || !hasStartStopPanel;
				circleFitterPanel.add(circleFitterStartStopPanel, BorderLayout.CENTER);
			}
		} else {
			attachmentsPanel.remove(circleFitterPanel);
			changedLayout = hasCircleFitterPanel;
		}
		if (changedLayout) {
			pack();
		}
		TFrame.repaintT(this);
	}

	public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		FontSizer.setFonts(attachmentCellRenderer, level);
		FontSizer.setFonts(table, level);
		FontSizer.setFonts(circleFitterPanel, level);
		FontSizer.setFonts(circleFitterStartStopPanel, level);
		refreshDropdowns();
		pack();
	}

	/**
	 * A class to provide model data for the attachment table.
	 */
	class AttachmentTableModel extends DefaultTableModel {
		@Override
		public int getRowCount() {
			TTrack measuringTool = TTrack.getTrack(trackID);
			if (measuringTool == null)
				return 0;

			if (measuringTool.ttype == TTrack.TYPE_CIRCLEFITTER) {
				CircleFitter fitter = (CircleFitter) measuringTool;
				if (fitter.attachToSteps) {
					return 1;
				}
			}
			TTrack[] attachments = measuringTool.getAttachments();
			return attachments == null ? 0 : attachments.length;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int row, int col) {
			TTrack measuringTool = TTrack.getTrack(trackID);
			if (col == 0) {
				return measuringTool.getAttachmentDescription(row);
			}
			return measuringTool.getAttachments()[row];
		}

		@Override
		public String getColumnName(int col) {
			return col == 0 ? TrackerRes.getString("AttachmentInspector.Header.PointName") : //$NON-NLS-1$
					TrackerRes.getString("AttachmentInspector.Header.AttachedTo"); //$NON-NLS-1$
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return col == 0 ? String.class : TTrack.class;
		}

		@Override
		public void setValueAt(Object val, int row, int col) {
		} // empty method

	}

	/**
	 * A class to render attachment table cells.
	 */
	class AttachmentCellRenderer extends JLabel implements TableCellRenderer {

		AttachmentCellRenderer() {
			setHorizontalAlignment(SwingConstants.CENTER);
			setBackground(Color.white);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object val, boolean selected, boolean hasFocus,
				int row, int col) {

			if (col == 0) {
				setText((String) val);
				validate();
				return this;
			}

			if (val == null)
				val = dummyMass;
			rendererDropdown.setSelectedItem(val == null ? dummyMass : val);
			if (OSPRuntime.isJS) {
				return trackCellRenderer.getListCellRendererComponent(null, val, -1, selected, hasFocus);
			}
			return rendererDropdown;
		}

	}

	/**
	 * A class to edit TTrack table cells in a JComboBox.
	 */
	class AttachmentCellEditor extends DefaultCellEditor {

		AttachmentCellEditor() {
			super(editorDropdown);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			editorDropdown.setSelectedItem(value == null ? dummyMass : value);
			return super.getTableCellEditorComponent(table, value, isSelected, row, column);
		}

		@Override
		public Object getCellEditorValue() {
			Object obj = super.getCellEditorValue();
			int row = table.getSelectedRow();
			if (row < 0)
				return null;
			TTrack measuringTool = TTrack.getTrack(trackID);
			TTrack[] attachments = measuringTool.getAttachments();
			if (attachments[row] != null) {
				attachments[row].removeStepListener(measuringTool); // $NON-NLS-1$
			}
			attachments[row] = obj == dummyMass ? null : (TTrack) obj;
			measuringTool.refreshAttachments();
			refreshGUI();

			DefaultTableModel dm = (DefaultTableModel) table.getModel();
			dm.fireTableDataChanged();
			return obj;
		}

	}

	/**
	 * A class to render track labels for the attachment JComboBoxes.
	 */
	class TTrackRenderer implements ListCellRenderer<Object> {

// problems here for JavaScript
//		JLabel label = new JLabel();
		TTrackRenderer() {
//			label.setOpaque(true);
//			label.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 0));
		}

		@Override
		public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object val, int index,
				boolean selected, boolean hasFocus) {
			JLabel label = new JLabel();
			label.setOpaque(true);
			label.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 0));

			if (list != null) {
				if (selected) {
					label.setBackground(list.getSelectionBackground());
					label.setForeground(list.getSelectionForeground());
				} else {
					label.setBackground(list.getBackground());
					label.setForeground(list.getForeground());
				}
			}
			if (val != null) {
				TTrack track = (TTrack) val;
				label.setText(track.getName());
				label.setIcon(track == dummyMass ? new ResizableIcon(dummyIcon) : track.getFootprint().getIcon(21, 16));
			}
			return label;
		}

	}

	/**
	 * A class to provide model data for the attachment JComboBoxes.
	 */
	class AttachmentComboBoxModel extends DefaultComboBoxModel<Object> implements ComboBoxModel<Object> {

		Object selected = dummyMass;

		@Override
		public int getSize() {
			return attachableTracks == null ? 1 : attachableTracks.size() + 1;
		}

		@Override
		public Object getElementAt(int index) {
			return index == 0 ? dummyMass : attachableTracks.get(index - 1);
		}

		@Override
		public void setSelectedItem(Object anItem) {
			selected = anItem;
		}

		@Override
		public Object getSelectedItem() {
			return selected == null ? dummyMass : selected;
		}

	}
}
