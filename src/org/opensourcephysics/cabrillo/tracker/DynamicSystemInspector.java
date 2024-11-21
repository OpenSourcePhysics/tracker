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
 * <https://opensourcephysics.github.io/tracker-website/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.opensourcephysics.tools.FontSizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * This displays and sets DynamicSystem properties.
 *
 * @author Douglas Brown
 */
public class DynamicSystemInspector extends JDialog implements PropertyChangeListener {

	// instance fields
	protected TFrame frame;
	protected Integer panelID;

	protected DynamicSystem system;
	protected boolean isVisible;
	protected int particleCount;
	protected JButton closeButton, helpButton;
	protected ActionListener changeParticleListener;
	protected JPanel[] particlePanels;
	protected JButton[] changeButtons;
	protected DynamicParticle[] selectedParticles;
	protected JLabel[] particleLabels;
	protected TButton[] particleButtons;
	protected JPanel[] labelPanels;
	protected DynamicParticle newParticle;
	protected TButton systemButton;
	protected MouseListener selectListener;

	/**
	 * Constructs a DynamicSystemInspector.
	 *
	 * @param track the DynamicSystem
	 */
	public DynamicSystemInspector(DynamicSystem track) {
		super(JOptionPane.getFrameForComponent(track.tp), false);
		system = track;
		particleCount = 2;
		TrackerPanel panel = track.tp;		
		frame = panel.getTFrame();
		panelID = panel.getID();
		if (panelID != null) {
			panel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
			if (frame != null) {
				frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			}
		}
		setResizable(false);
		createGUI();
		initialize();
	}

	/**
	 * Initializes this inspector.
	 */
	public void initialize() {
		FontSizer.setFonts(this, FontSizer.getLevel());
		updateDisplay();
	}

	/**
	 * Responds to property change events. This listens for the following events:
	 * TFrame.PROPERTY_TFRAME_TAB from TFrame.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TFrame.PROPERTY_TFRAME_TAB:
			if (panelID != null && ((TrackerPanel)e.getNewValue()).getID() == panelID) {
				setVisible(isVisible);
			} else {
				boolean vis = isVisible;
				setVisible(false);
				isVisible = vis;
			}
			return;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getNewValue() instanceof DynamicParticle) {
				newParticle = (DynamicParticle) e.getNewValue();
			}
			break;
		}
		updateDisplay();
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
	 * Disposes of this inspector.
	 */
	@Override
	public void dispose() {
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
			ArrayList<DynamicParticle> list = trackerPanel.getDrawablesTemp(DynamicParticle.class);
			for (int i = 0, ni = list.size(); i < ni; i++) {
				list.get(ni).removeListenerNCF(this);
			}
			list.clear();
			if (frame != null) {
				frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, this); // $NON-NLS-1$
			}
			frame = null;
			panelID = null;
		}
		super.dispose();
	}

//_____________________________ private methods ____________________________

	/**
	 * Creates the visible components of this panel.
	 */
	private void createGUI() {
		changeParticleListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JButton button = (JButton) e.getSource();
				final int n = button == changeButtons[0] ? 0 : 1;
				JPopupMenu popup = new JPopupMenu();
				boolean hasPopupItems = false;
				JMenu cloneMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.Clone")); //$NON-NLS-1$
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				ArrayList<DynamicParticle> list = trackerPanel.getDrawablesTemp(DynamicParticle.class);
				for (int i = 0, ni = list.size(); i < ni; i++) {
					DynamicParticle p = list.get(i);
					if (p instanceof DynamicSystem)
						continue; // no other systems
					// add items to clone menu
					final JMenuItem cloneItem = new JMenuItem(p.getName(), p.getFootprint().getIcon(21, 16));
					String name = p.getName();
					cloneItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							newParticle = null;
							TActions.cloneAction(trackerPanel, name);
							if (newParticle != null) {
								newParticle.getModelBuilder();
								selectedParticles[n] = newParticle;
								updateSystem();
							}
						}
					});
					cloneMenu.add(cloneItem);
					if (p == selectedParticles[0] || p == selectedParticles[1] || p.system != null)
						continue;
					// add items to popup menu
					hasPopupItems = true;
					final JMenuItem item = new JMenuItem(p.getName(), p.getFootprint().getIcon(21, 16)) {
						@Override
						public Dimension getPreferredSize() {
							Dimension dim = super.getPreferredSize();
							int w = button.getPreferredSize().width - 2;
							dim.width = Math.max(w, dim.width);
							return dim;
						}
					};
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							selectedParticles[n] = getParticle(item.getText());
							updateSystem();
						}
					});
					popup.add(item);
				}
				list.clear();
				if (hasPopupItems)
					popup.addSeparator();
				JMenu newMenu = new JMenu(TrackerRes.getString("TrackControl.Button.NewTrack")) { //$NON-NLS-1$
					@Override
					public Dimension getPreferredSize() {
						Dimension dim = super.getPreferredSize();
						int w = button.getPreferredSize().width - 2;
						dim.width = Math.max(w, dim.width);
						return dim;
					}
				};
				popup.add(newMenu);

				JMenuItem cartesianItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Cartesian")); //$NON-NLS-1$
				cartesianItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						newParticle = null;
						TActions.dynamicParticleAction(trackerPanel);
						if (newParticle != null) {
							newParticle.getModelBuilder();
							selectedParticles[n] = newParticle;
							updateSystem();
						}
					}
				});
				newMenu.add(cartesianItem);
				JMenuItem polarItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Polar")); //$NON-NLS-1$
				polarItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						newParticle = null;
						TActions.dynamicParticlePolarAction(trackerPanel);
						if (newParticle != null) {
							newParticle.getModelBuilder();
							selectedParticles[n] = newParticle;
							updateSystem();
						}
					}
				});
				newMenu.add(polarItem);
				if (cloneMenu.getItemCount() > 0)
					popup.add(cloneMenu);
				JMenuItem noneItem = new JMenuItem(TrackerRes.getString("DynamicSystemInspector.ParticleName.None")); //$NON-NLS-1$
				noneItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						newParticle = null;
						selectedParticles[n] = null;
						updateSystem();
					}
				});
				popup.addSeparator();
				popup.add(noneItem);

				FontSizer.setFonts(popup, FontSizer.getLevel());
				popup.show(button, 0, button.getHeight());
			}
		};
		selectListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				TButton button = (TButton) e.getSource();
				TTrack track = getParticle(button.getText());
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				trackerPanel.setSelectedTrack(track);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				closeButton.requestFocusInWindow();
			}
		};
		// create GUI components
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		JPanel inspectorPanel = new JPanel(new GridLayout(1, 2));
		contentPane.add(inspectorPanel, BorderLayout.CENTER);
		particlePanels = new JPanel[particleCount];
		changeButtons = new JButton[particleCount];
		particleLabels = new JLabel[particleCount];
		particleButtons = new TButton[particleCount];
		labelPanels = new JPanel[particleCount];
		for (int i = 0; i < particleCount; i++) {
			particlePanels[i] = new JPanel(new BorderLayout());
			inspectorPanel.add(particlePanels[i]);
			changeButtons[i] = new JButton();
			changeButtons[i].addActionListener(changeParticleListener);
			particleLabels[i] = new JLabel(TrackerRes.getString("DynamicSystemInspector.ParticleName.None")) { //$NON-NLS-1$
				@Override
				public Dimension getPreferredSize() {
					Dimension dim = super.getPreferredSize();
					dim.height = systemButton.getPreferredSize().height;
					return dim;
				}
			};
			particleLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
			particleLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);
			particleButtons[i] = new TButton();
			particleButtons[i].setContentAreaFilled(false);
			particleButtons[i].addMouseListener(selectListener);
			particleButtons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
			labelPanels[i] = new JPanel();
			labelPanels[i].setLayout(new BoxLayout(labelPanels[i], BoxLayout.Y_AXIS));
			labelPanels[i].add(particleLabels[i]);
			particlePanels[i].add(labelPanels[i], BorderLayout.NORTH);
			JPanel center = new JPanel();
			center.add(changeButtons[i]);
			particlePanels[i].add(center, BorderLayout.CENTER);
		}
		// create buttons and buttonbar
		systemButton = new TButton();
		systemButton.setText(system.getName());
		systemButton.setIcon(system.getFootprint().getIcon(21, 16));
		systemButton.setContentAreaFilled(false);
		systemButton.addMouseListener(selectListener);
		systemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		helpButton = new JButton();
		helpButton.setForeground(new Color(0, 0, 102));
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.showHelp("system", 0); //$NON-NLS-1$
			}
		});
		closeButton = new JButton();
		closeButton.setForeground(new Color(0, 0, 102));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		JPanel buttonbar = new JPanel();
		contentPane.add(buttonbar, BorderLayout.SOUTH);
		buttonbar.add(helpButton);
		buttonbar.add(closeButton);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(systemButton);
		panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
		contentPane.add(panel, BorderLayout.NORTH);
	}

	/**
	 * Updates the system to reflect the current particle selection.
	 */
	private void updateSystem() {
		if (selectedParticles[0] == null && selectedParticles[1] == null) {
			system.setParticles(new DynamicParticle[0]);
		} else if (selectedParticles[0] == null)
			system.setParticles(new DynamicParticle[] { selectedParticles[1] });
		else if (selectedParticles[1] == null)
			system.setParticles(new DynamicParticle[] { selectedParticles[0] });
		else
			system.setParticles(selectedParticles);
		if (newParticle == null)
			newParticle = system;
		system.getModelBuilder().setSelectedPanel(newParticle.getName());
		updateDisplay();
		this.setVisible(true);
	}

	/**
	 * Gets the particle with the specified name.
	 *
	 * @param name name of the particle
	 * @return the particle
	 */
	private DynamicParticle getParticle(String name) {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		return trackerPanel.getTrackByName(DynamicParticle.class, name);
	}

	/**
	 * Updates this inspector to show the system's current particles.
	 */
	protected void updateDisplay() {
		setTitle(TrackerRes.getString("DynamicSystemInspector.Title")); //$NON-NLS-1$
		helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		selectedParticles = new DynamicParticle[particleCount];
		systemButton.setText(system.getName());
		systemButton.setIcon(system.getFootprint().getIcon(21, 16));
		systemButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Properties.ToolTip") //$NON-NLS-1$
				+ " " + system.getName()); //$NON-NLS-1$
		boolean empty = true;
		for (int i = 0; i < particleCount; i++) {
			Border etched = BorderFactory.createEtchedBorder();
			TitledBorder title = BorderFactory.createTitledBorder(etched,
					TrackerRes.getString("DynamicSystemInspector.Border.Title") + " " + (i + 1)); //$NON-NLS-1$ //$NON-NLS-2$
			FontSizer.setFonts(title, FontSizer.getLevel());
			particlePanels[i].setBorder(title);
			changeButtons[i].setText(TrackerRes.getString("DynamicSystemInspector.Button.Change")); //$NON-NLS-1$
			labelPanels[i].removeAll();
			if (system.particles.length > i && system.particles[i] != null) {
				empty = false;
				selectedParticles[i] = system.particles[i];
				particleButtons[i].setText(selectedParticles[i].getName());
				particleButtons[i].setIcon(selectedParticles[i].getFootprint().getIcon(21, 16));
				particleButtons[i].setToolTipText(TrackerRes.getString("TrackControl.Button.Properties.ToolTip") //$NON-NLS-1$
						+ " " + selectedParticles[i].getName()); //$NON-NLS-1$

				labelPanels[i].setLayout(new BoxLayout(labelPanels[i], BoxLayout.Y_AXIS));
				labelPanels[i].add(particleButtons[i]);
			} else {
				selectedParticles[i] = null;
				particleLabels[i].setText(TrackerRes.getString("DynamicSystemInspector.ParticleName.None")); //$NON-NLS-1$
				labelPanels[i].setLayout(new BorderLayout());
				labelPanels[i].add(particleLabels[i]);
			}
			FontSizer.setFonts(labelPanels[i], FontSizer.getLevel());
		}
		changeButtons[particleCount - 1].setEnabled(!empty);
		changeButtons[0].requestFocusInWindow();
		pack();
		TFrame.repaintT(this);
	}

}
