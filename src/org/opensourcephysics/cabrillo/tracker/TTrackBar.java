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
 * <https://opensourcephysics.github.io/tracker/tracker/>.
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
import java.net.HttpURLConnection;
import java.net.URL;
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
import javax.swing.Timer;
import javax.swing.border.Border;

import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.OSPRuntime.Disposable;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
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
	protected TButton selectButton;
	protected JLabel emptyLabel = new JLabel();
	protected JPopupMenu selectPopup = new JPopupMenu();

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
									
									Upgrader upgrader = new Upgrader(frame);
									String upgradeURL = upgrader.getUpgradeURL();
									System.out.println("pig upgradeURL "+upgradeURL);
									String trackerJarName = "tracker-" + Tracker.latestVersion + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
									
									// look for upgrade tracker.jar
									if (upgradeURL != null && Tracker.trackerHome != null) {
										String upgradeFile = upgradeURL + trackerJarName;
										try {
											URL url = new URL(upgradeFile);
											HttpURLConnection huc = (HttpURLConnection) url.openConnection();
											int responseCode = huc.getResponseCode();
											System.out.println("pig response code "+responseCode+" from "+upgradeFile);
										} catch (Exception ex) {
										}
									}

																				
//									TrackerPanel trackerPanel = frame.getSelectedPanel();
//									VideoClip clip = trackerPanel.getPlayer().getVideoClip();
//									String path = clip.getVideoPath();
//									path = XML.forwardSlash(path);
//									path = ResourceLoader.getNonURIPath(path);
//									
//									if (!"".equals(path)) {
//										path = XML.stripExtension(path) + ".zip";
//										String src = trackerPanel.openedFromPath;
//										
//										// assemble command 
//										final ArrayList<String> cmd = new ArrayList<String>();
//										cmd.add("C:/Program Files/Java/jre1.8.0_321/bin/java");
//										cmd.add("-Djava.awt.headless=true");
//										cmd.add("-jar");
//										cmd.add("C:/Program Files/Tracker/tracker.jar");
//										cmd.add("-headless");
//										cmd.add(src);
//										cmd.add("-exportVideo");
//										cmd.add(path);
//										
//										ProcessBuilder builder = new ProcessBuilder(cmd);
//										try {
//											Process process = builder.start();
//											int n = process.waitFor();
//											System.out.println("pig done "+n);
//										} catch (Exception e1) {
//											e1.printStackTrace();
//										}
//									    
//									}
									
											
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
				/**
				 * @j2sNative
				 */
				{
					buildUpgradePopup(popup);
				}
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
		Object[] objectsToSize = new Object[] { newVersionButton, trackButton, sizingField, testButton };
		FontSizer.setFonts(objectsToSize, level);
		numberFieldWidth = sizingField.getPreferredSize().width;
	}

	private static final String[] panelProps = new String[] { 
			TrackerPanel.PROPERTY_TRACKERPANEL_TRACK,
			TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, 
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, 
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT,
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
//					TFrame frame = trackerPanel.getTFrame();
//					if (frame.getMaximizedView() < 0)
//						frame.maximizeView(trackerPanel, 4);
//					else 
//						frame.restoreViews(trackerPanel);
				}
			}
		});

		setFloatable(false);
		viewLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 0));
		if (OSPRuntime.isJS) {
		  viewLabel.setEnabled(false); 
		  // disables touch to select text
		}
		// select button
		selectButton = new TButton(selectTrackIcon) {
			@Override
			protected JPopupMenu getPopup() {
				return getSelectTrackPopup();
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
		Border empty = BorderFactory.createEmptyBorder(7, 3, 7, 3);
		Border etched = BorderFactory.createEtchedBorder();
		maximizeButton = new TButton(TViewChooser.MAXIMIZE_ICON, TViewChooser.RESTORE_ICON);
		maximizeButton.setBorder(BorderFactory.createCompoundBorder(etched, empty));
		maximizeButton.setToolTipText(TrackerRes.getString("TViewChooser.Maximize.Tooltip")); //$NON-NLS-1$
		maximizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean maximize = (frame.getMaximizedView() == TView.VIEW_UNSET);
				if (maximize) {
					frame.saveCurrentDividerLocations(panel());
					frame.maximizeView(panel(), TView.VIEW_MAIN);
				} else {
					frame.restoreViews(panel());
				}
				maximizeButton.setSelected(maximize);
				if (OSPRuntime.isJS) {
					maximizeButton.setIcon(maximize? TViewChooser.RESTORE_ICON: TViewChooser.MAXIMIZE_ICON);
				}
				maximizeButton.setToolTipText(maximize ? TrackerRes.getString("TViewChooser.Restore.Tooltip") : //$NON-NLS-1$
					TrackerRes.getString("TViewChooser.Maximize.Tooltip")); //$NON-NLS-1$
			}
		});

//		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
//		Border line = BorderFactory.createLineBorder(Color.GRAY);
//		trackButton.setBorder(BorderFactory.createCompoundBorder(line, space));
		// create horizontal glue for right end of toolbar
	}
	
	/**
	 * Refreshes and returns the "select track" popup menu.
	 *
	 * @return the popup
	 */
	protected JPopupMenu getSelectTrackPopup() {
		selectPopup.removeAll();
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
			selectPopup.add(item);
		}
		if (hasTracks) {
			selectPopup.addSeparator();
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
				selectPopup.add(item);
			}
		}
		FontSizer.setFonts(selectPopup, FontSizer.getLevel());
		return selectPopup;
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

	private boolean buildRequested;

	protected void rebuild() {
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
		selectButton.setForeground(Color.red);
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
			ArrayList<Component> list = track.getToolbarTrackComponents(panel);
			for (Component c : list) {
				if (c instanceof JComponent && !(c instanceof JButton) && !(c instanceof JCheckBox)) {
					updateSize((JComponent) c);
				}
				add(c);
			}
			// selected point items
			TPoint p = panel.getSelectedPoint();
			if (p != null) {
				// a point is selected
				list = track.getToolbarPointComponents(panel, p);
				for (Component c : list) {
					if (c instanceof JComponent && !(c instanceof JButton)) {
						updateSize((JComponent) c);
					}
					add(c);
				}
			}
		}

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
			
  		// show noData message if no video and no calibration tools
			// and no selected track
  		if (panel.getVideo() == null && panel.calibrationTools.isEmpty()
  				&& panel.getSelectedTrack() == null) {
  			String name = TrackerRes.getString("TFrame.View.Main");
  			String hint = TrackerRes.getString("TTrackBar.Hint.OpenFile");
  			viewLabel.setText(name + ": " + hint); //$NON-NLS-1$
  			FontSizer.setFonts(viewLabel);
  			add(viewLabel);
  		}
		}
		
		add(toolbarEnd);
		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			if (testButton != null) {
				add(testButton);
			}
		}
		add(maximizeButton);
		revalidate();
		TFrame.repaintT(this);
//		toolbarComponentHeight = selectButton.getPreferredSize().height;
	}

//	public Component add(Component c) {
//		//System.out.println("TTrackBar adding " + c);
//		return super.add(c);
//	}
//	
//	public void remove(Component c) {
//		System.out.println("TTrackBar removing " + c);
//		super.remove(c);
//	}

	@Override
	public void paint(Graphics g) {
		if (selectButton == null) {
			// this happens once before the frame is ready
			return;
		}
		//System.out.println("TTrackBar paint");
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
			refresh();
			break;
		case TTrack.PROPERTY_TTRACK_NAME:
		case TTrack.PROPERTY_TTRACK_COLOR:
		case TTrack.PROPERTY_TTRACK_FOOTPRINT:
			refresh();
			break;
		case "selectedpoint":
			// selected point has changed 
			refresh();
			break;
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

}
