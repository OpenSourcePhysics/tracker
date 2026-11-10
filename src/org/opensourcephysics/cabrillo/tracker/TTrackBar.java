/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2026 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.opensourcephysics.cabrillo.tracker.TButton.TStickyPopupButton;
import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.OSPRuntime.Disposable;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is a toolbar that display selected track properties in the NORTH
 * section of a MainTView.
 *
 * @author Douglas Brown
 */
public class TTrackBar extends JToolBar implements Disposable, PropertyChangeListener {

	// static fields
	protected static JButton newVersionButton;
	protected static Icon selectTrackIcon;
	protected static JButton testButton;
	protected static javax.swing.Timer testTimer;
	protected static boolean showOutOfMemoryDialog = true;
	private final static JTextField sizingField = new JTextField("1234567");
	private JLabel viewLabel = new JLabel();
	private Compact compact;

	static int testIndex;

	static {
		selectTrackIcon = Tracker.getResourceIcon("select_track.gif", true); //$NON-NLS-1$
		setTestOn(Tracker.testOn);
		/** @j2sIgnore */
		{
			setJava();
		}
	}


	// instance fields
	protected final Component toolbarEnd = Box.createGlue();
	protected int toolbarComponentHeight, numberFieldWidth;
	protected TButton trackButton;
	protected JButton maximizeButton;
	protected TStickyPopupButton trackBarChooseViewButton;
	protected JLabel mainViewIconLabel;
	protected TButton selectButton;
	protected JLabel emptyLabel = new JLabel();
	protected JPopupMenu selectPopup;
	private TFrame frame;
	private Integer panelID;

	/**
	 * TTrackBar constructor.
	 *
	 * @param panel the tracker panel
	 */
	TTrackBar(TrackerPanel panel) {
		System.out.println("Creating trackbar for " + panel);
		this.frame = panel.getTFrame();
		this.panelID = panel.getID();
		
		panel.addListeners(panelProps, this);
//		createGUI();
//		refresh();
//		validate();
	}

	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}
	
	private static void setTestOn(boolean on) {
		if (on) {
			testButton = new JButton("test"); //$NON-NLS-1$
			testButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final TFrame frame = (TFrame) testButton.getTopLevelAncestor();					
					if (frame != null && frame.getSelectedPanel() != null) {
						if (testTimer == null) {
							testTimer = new Timer(20, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									// test action goes here	
									OSPRuntime.setCompact(!OSPRuntime.isCompact(frame));									
									TrackerPanel tp = frame.getSelectedPanel();
									TTrackBar trackbar = frame.getTrackBar(tp.getID(), false);
									trackbar.rebuild();

//									
//									testIndex++;
//									testButton.setEnabled(true);								
											
									if (!testTimer.isRepeats()) {
										testTimer.stop();
										testTimer = null;
									}
								}
							});
							testTimer.setInitialDelay(0);
							testTimer.setRepeats(false);
							testTimer.start();
						} // end timer is null
						else {
							testTimer.stop();
							testTimer = null;
						}
					}
				}
			});
		}

	}
	
	/**
	 * @j2sIgnore
	 */
	protected static void buildUpgradePopup(JPopupMenu popup) {
		JMenuItem upgradeItem = new JMenuItem(TrackerRes.getString("TTrackBar.Popup.MenuItem.Upgrade")); //$NON-NLS-1$
		popup.add(upgradeItem);
		upgradeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final TFrame frame = (TFrame) newVersionButton.getTopLevelAncestor();
				new Upgrader(frame).upgrade();
			}
		});

		JMenuItem learnMoreItem = new JMenuItem(
				TrackerRes.getString("TTrackBar.Popup.MenuItem.LearnMore") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		popup.add(learnMoreItem);
		learnMoreItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// go to Tracker change log
				String websiteurl = "https://" + Tracker.trackerWebsite + "/change_log.html"; //$NON-NLS-1$ //$NON-NLS-2$
				OSPDesktop.displayURL(websiteurl);
			}
		});
		JMenuItem homePageItem = new JMenuItem(
				TrackerRes.getString("TTrackBar.Popup.MenuItem.TrackerHomePage") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		popup.add(homePageItem);
		homePageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// go to Tracker web site
				String websiteurl = "https://" + Tracker.trackerWebsite; //$NON-NLS-1$
				OSPDesktop.displayURL(websiteurl);
			}
		});
		JMenuItem ignoreItem = new JMenuItem(TrackerRes.getString("TTrackBar.Popup.MenuItem.Ignore")); //$NON-NLS-1$
		popup.add(ignoreItem);
		ignoreItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.checkedForNewerVersion = false;
				Tracker.newerVersion = null;
				Tracker.lastMillisChecked = System.currentTimeMillis();
				TFrame frame = (TFrame) newVersionButton.getTopLevelAncestor();
				TrackerPanel trackerPanel = (frame == null ? null : frame.getSelectedPanel());
				if (trackerPanel != null) {
					trackerPanel.taintEnabled();
					trackerPanel.getToolBar(true).refresh(TToolBar.REFRESH__NEW_VERSION);
				}
			}
		});
		FontSizer.setFonts(popup, FontSizer.getLevel());

	}


	/**
	 * @j2sIgnore
	 */
	private static void setJava() {
		OSPLog.addPropertyChangeListener(OSPRuntime.PROPERTY_ERROR_OUTOFMEMORY, new PropertyChangeListener() { //$NON-NLS-1$
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getNewValue().equals(OSPRuntime.OUT_OF_MEMORY_ERROR)) {
					OSPRuntime.outOfMemory = true;
				}
			}
		});

		newVersionButton = new TButton() {
			@Override
			public JPopupMenu getPopup() {
				JPopupMenu popup = new JPopupMenu();
				if (!OSPRuntime.isJS)
					buildUpgradePopup(popup);
				return popup;
			}
		};
		Font font = newVersionButton.getFont();
		newVersionButton.setFont(font.deriveFont(Font.PLAIN, font.getSize() - 1));
		newVersionButton.setForeground(Color.GREEN.darker());
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	public void setFontLevel(int level) {
		// sizingField is always present. The desktop-only newVersionButton is null
		// in Tracker Online, and FontSizer examines the first array element before
		// it filters null entries.
		Object[] objectsToSize = new Object[] 
				{ sizingField, newVersionButton, trackButton, testButton, 
						maximizeButton};
		FontSizer.setFonts(objectsToSize, level);
		numberFieldWidth = sizingField.getPreferredSize().width;
		getCompact().setFontLevel(level);
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		Component[] comps = getComponents();
		for (int i = 0; i < comps.length; i++) {
			comps[i].setEnabled(enable);
		}
	}

	private static final String[] panelProps = new String[] { 
			TrackerPanel.PROPERTY_TRACKERPANEL_TRACK,
			TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, 
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, 
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT
	};

	/**
	 * Gets the popup menu for the specified track.
	 *
	 * @param track the track
	 * @return the popup menu
	 */
	protected JPopupMenu getPopup(TTrack track) {
		JMenu trackMenu = track.getMenu(panel(), new JMenu());
		FontSizer.setFonts(trackMenu, FontSizer.getLevel());
		return trackMenu.getPopupMenu();
	}

	/**
	 * Creates the GUI.
	 */
	protected void createGUI() {
		// mouselistener for testing
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					maximizeButton.doClick(0);
				}
			}
		});

		setFloatable(false);
		viewLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
		if (OSPRuntime.isJS) {
		  viewLabel.setEnabled(false); 
		  // disables touch to select text
		}
		// select button
		selectButton = new TButton(selectTrackIcon) {
			@Override
			protected JPopupMenu getPopup() {
				if (selectPopup == null)
					selectPopup = new JPopupMenu();
				return getSelectTrackPopup(selectPopup);
			}
		};
		

		trackButton = new TButton() {
			@Override
			protected JPopupMenu getPopup() {

				TTrack track = getTrack();
				// special case: ParticleDataTrack
				if (track instanceof ParticleDataTrack) {
					if (trackButton.context.contains("point")) { //$NON-NLS-1$
						ParticleDataTrack dt = (ParticleDataTrack) track;
						JMenu trackMenu = dt.getPointMenu(track.tp);
						FontSizer.setFonts(trackMenu, FontSizer.getLevel());
						return trackMenu.getPopupMenu();
					}
					// else return leader's menu
					ParticleDataTrack dt = ((ParticleDataTrack) track).getLeader();
					JMenu trackMenu = dt.getMenu(track.tp, new JMenu());
					FontSizer.setFonts(trackMenu, FontSizer.getLevel());
					return trackMenu.getPopupMenu();
				}

				// general case
				return super.getPopup();
			}

		};
		trackButton.setOpaque(false);
		emptyLabel.setOpaque(false);
		
		// maximize button
		maximizeButton = new TButton(TViewChooser.MAXIMIZE_ICON, TViewChooser.RESTORE_ICON);
		maximizeButton.setBorder(BorderFactory.createCompoundBorder(TButton.ETCHED, TButton.EMPTY));
		maximizeButton.setToolTipText(TrackerRes.getString("TViewChooser.Maximize.Tooltip")); //$NON-NLS-1$
		maximizeButton.setHorizontalTextPosition(TButton.LEFT);
		maximizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean maximize = (panel().getMaximizedView() == TView.VIEW_UNSET);
				if (maximize) {
					frame.maximizeView(panel(), TView.VIEW_MAIN);
				} else if (!OSPRuntime.isSmallScreen(frame)) {
					frame.restoreViews(panel());
				}
				rebuild();
			}
		});
		// choose view button
		trackBarChooseViewButton = new TButton.TStickyPopupButton(frame, panelID, "TrackBar");

		mainViewIconLabel = new JLabel(TToolBar.pointmassOffIcon);
		mainViewIconLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 6));
//		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
//		Border line = BorderFactory.createLineBorder(Color.GRAY);
//		trackButton.setBorder(BorderFactory.createCompoundBorder(line, space));
		// create horizontal glue for right end of toolbar
	}
	
	int test = 0;

	
	/**
	 * Refreshes and returns the "select track" popup menu.
	 *
	 * @return the popup
	 */
	protected JPopupMenu getSelectTrackPopup(JPopupMenu popup) {
		popup.removeAll();
		// add measuring tools, calibration tools and axes at end
//    final CoordAxes axes = trackerPanel.getAxes();
		final ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();
				TrackerPanel panel = panel();
				TTrack track = panel.getTrack(item.getText());
				if (track == null)
					return;
				if (panel.calibrationTools.contains(track) || panel.measuringTools.contains(track)
						|| track == panel.getAxes()) {
					track.setVisible(true);
				}
				panel.setSelectedTrack(track);
			}
		};
		boolean hasTracks = false;
		TrackerPanel panel = panel();
		ArrayList<TTrack> userTracks = panel.getUserTracks();
		for (TTrack track : userTracks) {
			hasTracks = true;
			JMenuItem item = new JMenuItem(track.getName("track"), track.getIcon(21, 16, "track")); //$NON-NLS-1$ //$NON-NLS-2$
			item.addActionListener(listener);
			popup.add(item);
		}
		if (hasTracks) {
			popup.addSeparator();
		}
		for (TTrack track : panel.getTracks()) {
			if (!userTracks.contains(track)) {
				if (track == panel.getAxes() && !panel.isEnabled("button.axes")) //$NON-NLS-1$
					continue;
				switch (track.ttype) {
				case TTrack.TYPE_TAPEMEASURE:
					if (panel.calibrationTools.contains(track)) {
						TapeMeasure tape = (TapeMeasure) track;
						if (tape.isStickMode() ? !panel.isEnabled("calibration.stick") //$NON-NLS-1$
								: !panel.isEnabled("calibration.tape")) //$NON-NLS-1$
							continue;
					}
					break;
				case TTrack.TYPE_CALIBRATION:
					if (!panel.isEnabled("calibration.points")) //$NON-NLS-1$
						continue;
					break;
				case TTrack.TYPE_POINTMASS:
					if (track instanceof ParticleDataTrack)
						continue;
					break;
				case TTrack.TYPE_OFFSETORIGIN:
					if (!panel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
						continue;
					break;
				case TTrack.TYPE_PERSPECTIVE:
					continue;
				}
				JMenuItem item = new JMenuItem(track.getName(), track.getFootprint().getIcon(21, 16));
				item.addActionListener(listener);
				popup.add(item);
			}
		}
		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	/**
	 * Refreshes the GUI.
	 */ 
	@SuppressWarnings("deprecation")
	protected void refresh() {
		// check to see if a build has already been done since the last refresh request
		if (!panel().isPaintable() || buildRequested)
			return;
		if (selectButton == null)
			createGUI();
		buildRequested = true;
		OSPRuntime.postEvent(() -> {
			rebuild();
		});

	}
	
	private Compact getCompact() {
		if (compact == null) {
			compact = new Compact();
			}
		return compact;
			}
	
	protected void refreshNumberEditors(boolean later) {
		getCompact().refreshNumberEditors(later);
		}
	
	private boolean buildRequested;
	
	protected void rebuild() {
		if (selectButton == null)
			createGUI();
		buildRequested = false;
		numberFieldWidth = sizingField.getPreferredSize().width;
		selectButton.setToolTipText(TrackerRes.getString("TToolBar.Button.SelectTrack.Tooltip")); //$NON-NLS-1$
		removeAll();
		TrackerPanel panel = panel();
		TTrack track = trackButton.getTrack();
		if (track == null) {
			CoordAxes axes = panel.getAxes();
			if (axes != null) {
				trackButton.setTrack(axes);
			}
		} else {
			track.removeListenerNCF(this);
		}

		add(selectButton);
		trackButton.context = "track"; //$NON-NLS-1$
		track = panel.getSelectedTrack();
		if (track != null && track.ttype != TTrack.TYPE_PERSPECTIVE) {
			if (track instanceof ParticleDataTrack) {
				TPoint p = panel.getSelectedPoint();
				if (p != null) {
					Step step = track.getStep(p, panel);
					if (step != null && step.getTrack() == track) {
						trackButton.context = "point"; //$NON-NLS-1$
					}
				}
			}
			trackButton.setTrack(track);
			// listen to tracks for property changes that affect icon or name
			track.addListenerNCF(this);
			add(trackButton);
			
			toolbarComponentHeight = selectButton.getPreferredSize().height;
			
			if (OSPRuntime.isCompact(frame)) {
				getCompact().buildCompact(panel);
				return;
					}
			
			ArrayList<Component> list = track.getToolbarTrackComponents(panel);
				for (Component c : list) {
					if (c instanceof JComponent && !(c instanceof JButton) && !(c instanceof JCheckBox)) {
						updateSize((JComponent) c);
						if (c instanceof NumberField) {
							NumberField field = (NumberField)c;
							field.setBorder(TTrack.fieldBorder);
						}
					}
					add(c);
				}
			// selected point items
			TPoint p = panel.getSelectedPoint();
			if (p instanceof Crosshair.Control) {
				p = ((Crosshair.Control)p).getTarget().getMarkedPoint();
			}
			if (p != null) {
				// a point is selected
				list = track.getToolbarPointComponents(panel, p);
					for (Component c : list) {
						if (c instanceof JComponent && !(c instanceof JButton)) {
							updateSize((JComponent) c);
							if (c instanceof NumberField) {
								NumberField field = (NumberField)c;
								field.setBorder(TTrack.fieldBorder);
							}
						}
						add(c);
					}
				}
			}
		
		finishRebuilding(panel);
		}

	protected void finishRebuilding(TrackerPanel panel) {
		ArrayList<TTrack> userTracks = panel.getUserTracks();
		TFrame frame = panel.getTFrame();
		if ((userTracks == null || userTracks.isEmpty())
				&& panel.measuringTools.isEmpty()) {
			// no data-generating tracks exist
			// close right/bottom pane if only views there are plots and tables
			TViewChooser[] choosers = frame.getViewChoosers(panelID);
			boolean close = true;
			for (int i = 0; i < choosers.length; i++) {
				if (choosers[i] == null)
					continue;
				int viewType = choosers[i].getSelectedViewType();
				if (viewType == TView.VIEW_WORLD || viewType == TView.VIEW_PAGE)
					close = false;
			}
			if (close) {
				if (!TFrame.isPortraitOrientation)
					frame.setDividerLocation(panel, TFrame.SPLIT_MAIN_RIGHT, 1.0); 			
				else 
					frame.setDividerLocation(panel, TFrame.SPLIT_MAIN_BOTTOM, 1.0); 
			}
			
		}

		add(toolbarEnd);
		/** @j2sIgnore */
		{
			if (testButton != null) {
				add(testButton);
			}
		}
		maximizeButton.setText(TrackerRes.getString("TFrame.View.Main"));
		add(mainViewIconLabel);
		add(maximizeButton);
		boolean small = OSPRuntime.isSmallScreen(frame);
		boolean mobile = OSPRuntime.isMobile();
		if (panel().getMaximizedView() != TView.VIEW_UNSET) {
			maximizeButton.setIcon(small? null: TViewChooser.RESTORE_ICON);
			maximizeButton.setToolTipText(small? null: TrackerRes.getString("TViewChooser.Restore.Tooltip"));//$NON-NLS-1$
			trackBarChooseViewButton.setToolTipText(TrackerRes.getString("TViewChooser.NextView.Tooltip")); //$NON-NLS-1$
			add(trackBarChooseViewButton);			
		}
		else {
			maximizeButton.setIcon(small? null: TViewChooser.MAXIMIZE_ICON);
			maximizeButton.setToolTipText(small? null: TrackerRes.getString("TViewChooser.Maximize.Tooltip"));//$NON-NLS-1$
		}

		FontSizer.setFonts(maximizeButton);
		refreshNumberEditors(true);
		revalidate();
		TFrame.repaintT(this);
		
	}

	@Override
	public void paint(Graphics g) {
		if (selectButton == null) {
			// this happens once before the frame is ready
			return;
		}
		super.paint(g);
	}
	private void updateSize(JComponent jc) {
		int w = jc.getPreferredSize().width;
		jc.setMaximumSize(null);
		jc.setPreferredSize(null);
		Dimension dim = jc.getPreferredSize();
		
		dim.height = toolbarComponentHeight;
		if (jc instanceof NumberField) {
			dim.width = Math.max(numberFieldWidth, dim.width);
		} else if (jc instanceof TextLineLabel) {
			dim.width = w;
		} else if (jc instanceof JLabel) {
			// following code is workaround for labels being too often truncated
			JLabel lab = (JLabel) jc;
			lab.setToolTipText(lab.getText()); // in case truncated
			dim.width += 4; // more room so truncation less likely
		} 
		jc.setPreferredSize(dim);
		jc.setMaximumSize(dim);
	}

	/**
	 * Resizes a NumberField.
	 * 
	 * @param field the number field
	 */
	protected void resizeField(NumberField field) {
		// do nothing if the field is not displayed
		if (getComponentIndex(field) < 0)
			return;
		field.setMaximumSize(null);
		field.setPreferredSize(null);
		Dimension dim = field.getPreferredSize();
		dim.height = toolbarComponentHeight;
		dim.width = Math.max(numberFieldWidth, dim.width);
		field.setMaximumSize(dim);
		field.setPreferredSize(dim);
		revalidate();
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK:
		case TTrack.PROPERTY_TTRACK_FOOTPRINT:
		case TTrack.PROPERTY_TTRACK_NAME:
		case TTrack.PROPERTY_TTRACK_COLOR:
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT:
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			// tracks have been added or removed
			refresh();
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			// tracks have been cleared 
			for (TTrack t : TTrack.getValues()) {
				t.removeListenerNCF(this);
			}
			if (trackButton != null)
				trackButton.setTrack(null);
			refresh();
			break;
		}
	}

	/**
	 * Cleans up this trackbar
	 */
	@Override
	public void dispose() {
		removeAll();
		panel().removeListeners(panelProps, this);
		for (TTrack t : TTrack.getValues()) {
			t.removeListenerNCF(this);
		}
		if (trackButton != null)
			trackButton.setTrack(null);
		panelID = null;
		frame = null;
	}

	protected TrackerPanel panel() {
		return (frame == null 
				? 
						null 
						: frame.getTrackerPanelForID(panelID));
	}


	@Override
	public String toString() {
		return "[TTrackBar " + panelID + "]";
	}

	/**
	 * A private class to consolidate everything compact about the trackbar.
	 * 
	 */

	private class Compact {
		
		protected NumberFieldPanel[] numberEditors;
		protected TButton trackNumberEditorButton, pointNumberEditorButton;

		Compact() {
			numberEditors = NumberFieldPanel.getNFPanelArray(panel());
			numberEditors[0].setUp(true);
			numberEditors[1].setUp(true);

			// number editor buttons
			trackNumberEditorButton = new TButton(TViewChooser.RESTORE_ICON, TViewChooser.MAXIMIZE_ICON);
			trackNumberEditorButton.addActionListener((e)-> {
				trackNumberEditorButton.setSelected(!trackNumberEditorButton.isSelected());
				refreshNumberEditor(0);
			});
			pointNumberEditorButton = new TButton(TViewChooser.RESTORE_ICON, TViewChooser.MAXIMIZE_ICON);
			pointNumberEditorButton.addActionListener((e)-> {
				pointNumberEditorButton.setSelected(!pointNumberEditorButton.isSelected());
				refreshNumberEditor(1);
			});		
		}
		
		/**
		 * Sets the font level.
		 *
		 * @param level the desired font level
		 */
		public void setFontLevel(int level) {
			// sizingField is always present. The desktop-only newVersionButton is null
			// in Tracker Online, and FontSizer examines the first array element before
			// it filters null entries.
			Object[] objectsToSize = new Object[] 
					{numberEditors[0], numberEditors[1]};
			FontSizer.setFonts(objectsToSize, level);
			refreshNumberEditors(true);
		}
		
		protected void buildCompact(TrackerPanel panel) {
			TTrack track = trackButton.getTrack();
			TPoint p = panel.getSelectedPoint();
			if (p instanceof Crosshair.Control) {
				p = ((Crosshair.Control)p).getTarget().getMarkedPoint();
			}
			ArrayList<Component> trackComps = track.getToolbarTrackComponents(panel);
			ArrayList<Component> pointComps = track.getToolbarPointComponents(panel, p);
			track.stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
			track.stepValueLabel.setText(panel.getStepNumber() + ":"); //$NON-NLS-1$
			updateSize(track.stepLabel);
			updateSize(track.stepValueLabel);
		
			if (track instanceof PointMass) {
				buildCompactForPointMass((PointMass)track, panel);
				return;
			}
			else if (track instanceof RGBRegion) {
				buildCompactForRGB((RGBRegion)track, panel, trackComps, pointComps);
				return;
			}
			else if (track instanceof LineProfile) {
				buildCompactForLineProfile((LineProfile)track, panel, trackComps);
				return;
			}
			else if (track instanceof CoordAxes) {
				buildCompactForCoordAxes((CoordAxes)track, panel, trackComps);
				return;
			}
			
			ArrayList<Component> toolbarComps = new ArrayList<Component>();
			numberEditors[0].clear();
			boolean empty = true;
			for (int j = 0; j < trackComps.size(); j++) {
				if (trackComps.get(j) instanceof NumberField && trackComps.get(j-1) instanceof JComponent) {
						NumberField field = (NumberField)trackComps.get(j);
						JComponent label = (JComponent)trackComps.get(j-1);
						numberEditors[0].addRow(label, field);
						empty = false;
				}
				else if (trackComps.get(j) instanceof JCheckBox) {
					JCheckBox checkbox = (JCheckBox)trackComps.get(j);
					TextLineLabel label = new TextLineLabel(checkbox.getText());
					numberEditors[0].addRow(label, checkbox);
					empty = false;
				}
			}
			if (!empty) {
				numberEditors[0].updateTableLayout();
				add(trackNumberEditorButton);
			}
			if (!toolbarComps.isEmpty()) {
				for (int j = 0; j < toolbarComps.size(); j++) {
					add(toolbarComps.get(j));
				}
			}

			// selected point items
			if (p != null) {
				// a point is selected
				trackComps = track.getToolbarPointComponents(panel, p);
				toolbarComps.clear();
				Component stepLabel = null;
				numberEditors[1].clear();
				empty = true;
				for (int j = 0; j < trackComps.size(); j++) {
					Component c = trackComps.get(j);
					if (c == track.stepLabel) {
						updateSize((JComponent) c);
						add(c);
						stepLabel = c;
					}
					else if (c == track.stepValueLabel) {
						updateSize((JComponent) c);
						add(c);
					}
					else if (c instanceof NumberField && trackComps.get(j-1) instanceof JComponent) {
							NumberField field = (NumberField)c;
							JComponent label = (JComponent)trackComps.get(j-1);
							numberEditors[1].addRow(label, field);
							empty = false;
					}
					else if (trackComps.get(j) instanceof JCheckBox && trackComps.get(j-1) instanceof JComponent) {
						JCheckBox checkbox = (JCheckBox)trackComps.get(j);
						JComponent label = (JComponent)trackComps.get(j-1);
						numberEditors[0].addRow(label, checkbox);
						empty = false;
					}
				}
				if (!empty) {
					numberEditors[1].updateTableLayout();
					numberEditors[1].setPositionComponent(stepLabel);
					add(pointNumberEditorButton);
				}
				if (!toolbarComps.isEmpty()) {
					for (int j = 0; j < toolbarComps.size(); j++) {
						add(toolbarComps.get(j));
					}
				}
			}
			
			finishRebuilding(panel);			
		}
		
		protected void buildCompactForPointMass(PointMass pm, TrackerPanel panel) {
			// add track items to toolbar
			add(trackNumberEditorButton);
			// add track items to NumberFieldPanel
			numberEditors[0].clear();		
			numberEditors[0].addRow(pm.massLabel, pm.massField);
			numberEditors[0].updateTableLayout();			
			
			numberEditors[1].clear();	
			if (panel.getSelectedPoint() != null) {
				// add items to toolbar
				add(pm.stepLabel);
				add(pm.stepValueLabel);
				add(pointNumberEditorButton);
				// add items to NumberFieldPanel
				numberEditors[1].addRow(pm.xLabel, pm.xField);
				numberEditors[1].addRow(pm.yLabel, pm.yField);
				numberEditors[1].addRow(pm.magLabel, pm.magField);
				numberEditors[1].addRow(pm.angleLabel, pm.angleField);
				numberEditors[1].updateTableLayout();				
				numberEditors[1].setPositionComponent(pm.stepLabel);
			}
			
			finishRebuilding(panel);			
		}
				
		protected void buildCompactForRGB(RGBRegion rgn, TrackerPanel panel,
				ArrayList<Component> trackComps, ArrayList<Component> pointComps) {
			// add track items to NumberFieldPanel
			numberEditors[0].clear();		
			TextLineLabel label = new TextLineLabel(TrackerRes.getString("AutoTracker.Label.Shape"));
			numberEditors[0].addRow(label, rgn.shapeTypeDropdown);			
			if (rgn.shapeType != RGBRegion.SHAPE_POLYGON) {
				numberEditors[0].addRow(rgn.widthLabel, rgn.widthField);
				numberEditors[0].addRow(rgn.heightLabel, rgn.heightField);				
			}			
			numberEditors[0].updateTableLayout();	
						
			// add track items to toolbar
			add(trackNumberEditorButton);
			if (trackComps.contains(rgn.unmarkedLabel)) {
				add(rgn.stepSeparator);	
				add(rgn.unmarkedLabel);	
			}
			else if (trackComps.contains(rgn.editPolygonButton)) {
				add(rgn.editPolygonButton);	
			}
			else if (trackComps.contains(rgn.helpLabel)) {
				add(rgn.stepSeparator);	
				add(rgn.helpLabel);	
			}
			
			numberEditors[1].clear();	
			if (panel.getSelectedPoint() != null
					&& panel.getSelectedPoint() instanceof RGBStep.Position) {
				// add items to toolbar
				add(rgn.stepLabel);
				add(rgn.stepValueLabel);
				add(pointNumberEditorButton);
				// add items to NumberFieldPanel
				numberEditors[1].addRow(rgn.xLabel, rgn.xField);
				numberEditors[1].addRow(rgn.yLabel, rgn.yField);
				numberEditors[1].updateTableLayout();				
				numberEditors[1].setPositionComponent(rgn.stepLabel);
			}
			
			if (pointComps != null && pointComps.contains(rgn.editPolygonButton)) {
				add(rgn.editPolygonButton);	
			}
			finishRebuilding(panel);			
		}
				
		protected void buildCompactForLineProfile(LineProfile profile, TrackerPanel panel,
				ArrayList<Component> trackComps) {
			// add track items to NumberFieldPanel
			numberEditors[0].clear();		
			numberEditors[0].addRow(profile.spreadLabel, profile.spreadField);			
			numberEditors[0].updateTableLayout();	
			numberEditors[1].clear();	
						
			// add track items to toolbar
			add(trackNumberEditorButton);
			if (trackComps.contains(profile.unmarkedLabel)) {
				add(profile.stepSeparator);	
				add(profile.unmarkedLabel);	
			}

			finishRebuilding(panel);			
		}

		protected void buildCompactForCoordAxes(CoordAxes axes, TrackerPanel panel,
				ArrayList<Component> trackComps) {
			// add track items to toolbar
			add(trackNumberEditorButton);
			add(axes.stepLabel);
			add(axes.stepValueLabel);
			add(pointNumberEditorButton);
			
			// add track items to NumberFieldPanels
			numberEditors[0].clear();			
			TextLineLabel label = new TextLineLabel(axes.gridCheckbox.getText());
			numberEditors[0].addRow(label, axes.gridBox);
			numberEditors[0].updateTableLayout();	
			
			numberEditors[1].clear();	
			numberEditors[1].addRow(axes.xLabel, axes.xField);
			numberEditors[1].addRow(axes.yLabel, axes.yField);
			numberEditors[1].addRow(axes.angleLabel, axes.angleField);
			numberEditors[1].updateTableLayout();	
			numberEditors[1].setPositionComponent(axes.stepLabel);
						
			finishRebuilding(panel);			
		}
		

		protected void refreshNumberEditor(int i) {
			if (trackNumberEditorButton==null) return;
			
			if (i==0) {
				if (OSPRuntime.isCompact(frame)
						&& trackNumberEditorButton.isSelected() 
						&& panel().getSelectedTrack() != null 
						&& numberEditors[0].getRowCount() > 0
						&& frame.isMainViewVisible(panel())
						&& frame.getSelectedPanel() == panel()) {
					numberEditors[0].updateTableLayout();
					numberEditors[0].setVisible(true);		
					numberEditors[0].setPositionComponent(trackButton);
				}
				else {
					numberEditors[0].setVisible(false);
				}
			}
			else {
				if (OSPRuntime.isCompact(frame)
						&& pointNumberEditorButton.isSelected() 
						&& panel().getSelectedTrack() != null 
//						&& panel().getSelectedPoint() != null 
						&& numberEditors[1].getRowCount() > 0
						&& frame.isMainViewVisible(panel())
						&& frame.getSelectedPanel() == panel()) {
					Component c = numberEditors[1].getPositionComponent();
					numberEditors[1].updateTableLayout();
					numberEditors[1].setVisible(true);
					numberEditors[1].setPositionComponent(c != null? c: pointNumberEditorButton);
				}
				else {
					numberEditors[1].setVisible(false);
				}
			}
		}
		
		protected void refreshNumberEditors(boolean later) {
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					refreshNumberEditor(0);
					refreshNumberEditor(1);
				}
			};
			if (later)
				SwingUtilities.invokeLater(runner);
			else
				runner.run();
		}
		

		
	}

}
