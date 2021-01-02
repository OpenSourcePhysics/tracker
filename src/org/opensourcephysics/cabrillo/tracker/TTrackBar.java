/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert Hanson
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.LibraryBrowser;
import org.opensourcephysics.tools.ResourceLoader;

import javajs.async.SwingJSUtils.Performance;

/**
 * This is a toolbar that display selected track properties in the NORTH
 * section of a TrackerPanel.
 *
 * @author Douglas Brown
 */
public class TTrackBar extends JToolBar implements PropertyChangeListener {

	// static fields
	protected static Map<TrackerPanel, TTrackBar> trackbars = new HashMap<TrackerPanel, TTrackBar>();
	protected static JButton memoryButton, newVersionButton;
	protected static boolean outOfMemory = false;
	protected static Icon selectTrackIcon;
	protected static JButton testButton;
	protected static javax.swing.Timer testTimer;
	protected static boolean showOutOfMemoryDialog = true;
	private final static JTextField sizingField = new JTextField("1234567");
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
	protected TrackerPanel trackerPanel; // manages & displays track data
	protected final Component toolbarEnd = Box.createHorizontalGlue();
	protected int toolbarComponentHeight, numberFieldWidth;
	protected TButton trackButton;
	protected JButton maximizeButton;
	protected TButton selectButton;
	protected JLabel emptyLabel = new JLabel();
	protected JPopupMenu selectPopup = new JPopupMenu();

	/**
	 * Gets the trackbar for the specified tracker panel.
	 *
	 * @param panel the tracker panel
	 * @return the trackbar
	 */
	public static synchronized TTrackBar getTrackbar(TrackerPanel panel) {
		TTrackBar trackbar = trackbars.get(panel);
		if (trackbar == null) {
			trackbar = new TTrackBar(panel);
			trackbars.put(panel, trackbar);
		}
		return trackbar;
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
							testTimer = new Timer(500, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									// test action goes here
//									TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
									
//									AutoTracker autoTracker = trackerPanel.getAutoTracker(false);
//									java.awt.image.BufferedImage image = autoTracker.getTemplateMatcher().getTemplate();
//									String filePath = "D:/Documents/Tracker/testing/pig_template.gif";
//									if (testIndex++ == 0)
//										VideoIO.writeImageFile(image, filePath);
//									image = ResourceLoader.getBufferedImage(filePath, java.awt.image.BufferedImage.TYPE_INT_ARGB);
//									autoTracker.getTemplateMatcher().setTemplate(image);
									
//									boolean allViews = testIndex % 2 != 0;
//									boolean portrait = (testIndex/2) % 2 == 0;
//									frame.arrangeViews(trackerPanel, portrait, allViews);
//									TFrame.isPortraitLayout = portrait;
//									testIndex++;
									
//									long t0 = Performance.now(0);
//									String url = "https://iwant2study.org/lookangejss/02_newtonianmechanics_7gravity/trz/angrybirdtracking.trz";
//									String filePath = "D:/Documents/Tracker/testing/angry"+testIndex+".trz";
//									OSPLog.debug("testIndex " + testIndex);
//									if (testIndex % 2 == 0) {
//										OSPLog.debug("copying to " + filePath);
//										try {
//											ResourceLoader.copyURLtoFile(url, filePath);
//										} catch (IOException e1) {
//											// TODO Auto-generated catch block
//											e1.printStackTrace();
//										}
//									}
//									else {
//										OSPLog.debug("downloading to " + filePath);
//										ResourceLoader.download(url, new File(filePath), true);
//									}
//									testIndex++;
//									OSPLog.debug("finished " + filePath +" in "+ Performance.now(t0));

//		    	      	TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());		 
//		    	      	TrackControl.getControl(trackerPanel).refresh();
//		    	      	TTrack track = trackerPanel.getSelectedTrack();

//		    	      	Tracker.checkedForNewerVersion = false;
//					  			Tracker.testString = "5.1.3"; //$NON-NLS-1$
//					  			Tracker.loadCurrentVersion(true, false);
//					  			Tracker.testString = null;

//			    	    	Map<String, String> map = System.getenv();
//			    	    	for (String key: map.keySet()) {
//			    	    		System.out.println("environment "+key+" = "+map.get(key));
//			    	    	}
//			    	    	for (Object key: System.getProperties().keySet()) {
//			    	    		System.out.println("property "+key+" = "+System.getProperties().get(key));
//			    	    	}		    	      			    	      	

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
	private static void setJava() {
		OSPLog.getOSPLog().addPropertyChangeListener("error", new PropertyChangeListener() { //$NON-NLS-1$
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				int type = Integer.parseInt(e.getNewValue().toString());
				if (type == OSPLog.OUT_OF_MEMORY_ERROR) {
					outOfMemory = true;
				}
			}
		});

		memoryButton = new TButton() {
			@Override
			public JPopupMenu getPopup() {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem memoryItem = new JMenuItem(TrackerRes.getString("TTrackBar.Memory.Menu.SetSize")); //$NON-NLS-1$
				popup.add(memoryItem);
				memoryItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = (TFrame) memoryButton.getTopLevelAncestor();
						String response = GUIUtils.showInputDialog(frame,
								TrackerRes.getString("TTrackBar.Dialog.SetMemory.Message"), //$NON-NLS-1$
								TrackerRes.getString("TTrackBar.Dialog.SetMemory.Title"), //$NON-NLS-1$
								JOptionPane.PLAIN_MESSAGE, String.valueOf(Tracker.preferredMemorySize));
						if (response != null && !"".equals(response)) { //$NON-NLS-1$
							String s = response;
							try {
								double d = Double.parseDouble(s);
								d = Math.rint(d);
								int n = (int) d;
								if (n < 0)
									n = -1; // default
								else
									n = Math.max(n, 32); // not less than 32MB
								if (n != Tracker.preferredMemorySize) {
									Tracker.preferredMemorySize = n;
									int ans = JOptionPane.showConfirmDialog(frame,
											TrackerRes.getString("TTrackBar.Dialog.Memory.Relaunch.Message"), //$NON-NLS-1$
											TrackerRes.getString("TTrackBar.Dialog.Memory.Relaunch.Title"), //$NON-NLS-1$
											JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
									if (ans == JOptionPane.YES_OPTION) {

										Tracker.savePreferences();
										frame.relaunchCurrentTabs();
									}
								}
							} catch (Exception ex) {
							}
						}
					}
				});
				FontSizer.setFonts(popup, FontSizer.getLevel());
				return popup;
			}
		};
		Font font = memoryButton.getFont();
		memoryButton.setFont(font.deriveFont(Font.PLAIN, font.getSize() - 1));
		memoryButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				refreshMemoryButton();
			}
		});
		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
		Border line = BorderFactory.createLineBorder(Color.GRAY);
		memoryButton.setBorder(BorderFactory.createCompoundBorder(line, space));
		newVersionButton = new TButton() {
			@Override
			public JPopupMenu getPopup() {
				JPopupMenu popup = new JPopupMenu();
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
						Tracker.newerVersion = null;
						Tracker.lastMillisChecked = System.currentTimeMillis();
						TFrame frame = (TFrame) newVersionButton.getTopLevelAncestor();
						TrackerPanel trackerPanel = (frame == null ? null : frame.getSelectedPanel());
						if (trackerPanel != null) {
							trackbars.get(trackerPanel).refresh();
						}
					}
				});
				FontSizer.setFonts(popup, FontSizer.getLevel());
				return popup;
			}
		};
		newVersionButton.setFont(font.deriveFont(Font.PLAIN, font.getSize() - 1));
		newVersionButton.setForeground(Color.GREEN.darker());
		newVersionButton.setBorder(BorderFactory.createCompoundBorder(line, space));
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	public void setFontLevel(int level) {
		Object[] objectsToSize = new Object[] { newVersionButton, trackButton, sizingField, memoryButton, testButton };
		FontSizer.setFonts(objectsToSize, level);
		numberFieldWidth = sizingField.getPreferredSize().width;
	}

	/**
	 * TTrackBar constructor.
	 *
	 * @param panel the tracker panel
	 */
	private TTrackBar(TrackerPanel panel) {
		trackerPanel = panel;
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); // $NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, this); // $NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this); // $NON-NLS-1$
		createGUI();
//		refresh();
//		validate();
	}

	@Override
	public void finalize() {
		OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

	/**
	 * Gets the popup menu for the specified track.
	 *
	 * @param track the track
	 * @return the popup menu
	 */
	protected JPopupMenu getPopup(TTrack track) {
		JMenu trackMenu = track.getMenu(trackerPanel, new JMenu());
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
//					if (frame.maximizedView < 0)
//						frame.maximizeView(trackerPanel, 4);
//					else 
//						frame.restoreViews(trackerPanel);
				}
			}
		});

		setFloatable(false);
//		setBorder(BorderFactory.createEmptyBorder(3, 0, 2, 0));
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
						JMenu trackMenu = dt.getPointMenu(track.trackerPanel);
						FontSizer.setFonts(trackMenu, FontSizer.getLevel());
						return trackMenu.getPopupMenu();
					}
					// else return leader's menu
					ParticleDataTrack dt = ((ParticleDataTrack) track).getLeader();
					JMenu trackMenu = dt.getMenu(track.trackerPanel, new JMenu());
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
				TFrame frame = trackerPanel.getTFrame();
				boolean maximize = frame.maximizedView < 0;
				if (maximize) {
					frame.saveCurrentDividerLocations(trackerPanel);
					frame.maximizeView(trackerPanel, TView.VIEW_MAIN);
				} else {
					frame.restoreViews(trackerPanel);
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
		// add calibration tools and axes at end
//    final CoordAxes axes = trackerPanel.getAxes();
		final ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();
				TTrack track = trackerPanel.getTrack(item.getText());
				if (track == null)
					return;
				if (trackerPanel.calibrationTools.contains(track) || track == trackerPanel.getAxes()) {
					track.setVisible(true);
				}
				trackerPanel.setSelectedTrack(track);
			}
		};
		boolean hasTracks = false;
		ArrayList<TTrack> userTracks = trackerPanel.getUserTracks();
		for (TTrack track : userTracks) {
			hasTracks = true;
			JMenuItem item = new JMenuItem(track.getName("track"), track.getIcon(21, 16, "track")); //$NON-NLS-1$ //$NON-NLS-2$
			item.addActionListener(listener);
			selectPopup.add(item);
		}
		if (hasTracks) {
			selectPopup.addSeparator();
		}
		for (TTrack track : trackerPanel.getTracks()) {
			if (!userTracks.contains(track)) {
				if (track == trackerPanel.getAxes() && !trackerPanel.isEnabled("button.axes")) //$NON-NLS-1$
					continue;
				if (trackerPanel.calibrationTools.contains(track) && track instanceof TapeMeasure) {
					TapeMeasure tape = (TapeMeasure) track;
					if (tape.isStickMode() && !trackerPanel.isEnabled("calibration.stick")) //$NON-NLS-1$
						continue;
					if (!tape.isStickMode() && !trackerPanel.isEnabled("calibration.tape")) //$NON-NLS-1$
						continue;
				}
				if (track instanceof Calibration && !trackerPanel.isEnabled("calibration.points")) //$NON-NLS-1$
					continue;
				if (track instanceof ParticleDataTrack)
					continue;
				if (track instanceof OffsetOrigin && !trackerPanel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
					continue;
				if (track instanceof PerspectiveTrack)
					continue;
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
	protected void refresh() {
		if (!trackerPanel.isPaintable())
			return;
		OSPRuntime.postEvent(() -> {
			rebuild();
		});

	}

	protected void rebuild() {
		numberFieldWidth = sizingField.getPreferredSize().width;
		selectButton.setToolTipText(TrackerRes.getString("TToolBar.Button.SelectTrack.Tooltip")); //$NON-NLS-1$
		OSPLog.debug(Performance.timeCheckStr("TTrackbar.rebuild0", Performance.TIME_MARK));

		removeAll();
		TTrack track = trackButton.getTrack();
		if (track == null) {
			CoordAxes axes = trackerPanel.getAxes();
			if (axes != null) {
				trackButton.setTrack(axes);
			}
		} else {
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); // $NON-NLS-1$
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); // $NON-NLS-1$
		}
		toolbarComponentHeight = selectButton.getPreferredSize().height;

		add(selectButton);
		trackButton.context = "track"; //$NON-NLS-1$
		track = trackerPanel.getSelectedTrack();
		if (track != null && !(track instanceof PerspectiveTrack)) {
			if (track instanceof ParticleDataTrack) {
				TPoint p = trackerPanel.getSelectedPoint();
				if (p != null) {
					Step step = track.getStep(p, trackerPanel);
					if (step != null && step.getTrack() == track) {
						trackButton.context = "point"; //$NON-NLS-1$
					}
				}
			}
			trackButton.setTrack(track);
			// listen to tracks for property changes that affect icon or name
			track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); // $NON-NLS-1$
			track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
			track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); // $NON-NLS-1$
			add(trackButton);
			ArrayList<Component> list = track.getToolbarTrackComponents(trackerPanel);
			for (Component c : list) {
				if (c instanceof JComponent && !(c instanceof JButton) && !(c instanceof JCheckBox)) {
					updateSize((JComponent) c);
				}
				add(c);
			}
			// selected point items
			TPoint p = trackerPanel.getSelectedPoint();
			if (p != null) {
				// a point is selected
				list = track.getToolbarPointComponents(trackerPanel, p);
				for (Component c : list) {
					if (c instanceof JComponent && !(c instanceof JButton)) {
						updateSize((JComponent) c);
					}
					add(c);
				}
			}
		}
		add(toolbarEnd);
		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			if (testButton != null) {
//				add(testButton);
			}
			if (Tracker.newerVersion != null) {
				String s = TrackerRes.getString("TTrackBar.Button.Version"); //$NON-NLS-1$
				newVersionButton.setText(s + " " + Tracker.newerVersion); //$NON-NLS-1$
				add(newVersionButton);
			}
			memoryButton.setToolTipText(TrackerRes.getString("TTrackBar.Button.Memory.Tooltip")); //$NON-NLS-1$
			// refreshMemoryButton();
//			add(memoryButton); // pig for testing
		}
		add(maximizeButton);
		OSPLog.debug(Performance.timeCheckStr("TTrackbar.rebuild1 " + trackerPanel.getName(), Performance.TIME_MARK));
		revalidate();
		OSPLog.debug(Performance.timeCheckStr("TTrackbar.rebuild-revalidate " + (track == null ? null : track.getName()), Performance.TIME_MARK));
		//TFrame.repaintT(this);
	}

	public void paint(Graphics g) {
		System.out.println("TTrackBar paint");
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
	 * Refreshes the decimal separators of displayed NumberFields.
	 */
	protected void refreshDecimalSeparators() {
		for (Component next : getComponents()) {
			if (next instanceof NumberField) {
				NumberField field = (NumberField) next;
				field.setValue(field.getValue());
			}
		}
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
		case TTrack.PROPERTY_TTRACK_FOOTPRINT:
		case TTrack.PROPERTY_TTRACK_COLOR:
		case TTrack.PROPERTY_TTRACK_NAME:
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
			for (Integer n : TTrack.activeTracks.keySet()) {
				TTrack track = TTrack.activeTracks.get(n);
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); // $NON-NLS-1$
			}
			trackButton.setTrack(null);
			refresh();
			break;
		}
	}

	/**
	 * Cleans up this trackbar
	 */
	public void dispose() {
		trackbars.remove(trackerPanel);
		removeAll();
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_NAME, this); // $NON-NLS-1$
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_COLOR, this); // $NON-NLS-1$
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_FOOTPRINT, this); // $NON-NLS-1$
		}
		trackButton.setTrack(null);
		trackerPanel = null;
	}

	/**
	 * Refreshes the memory button.
	 */
	protected static void refreshMemoryButton() {
		if (memoryButton == null)
			return;
		System.gc();
		java.lang.management.MemoryMXBean memory = java.lang.management.ManagementFactory.getMemoryMXBean();
		long cur = memory.getHeapMemoryUsage().getUsed() / (1024 * 1024);
		long max = memory.getHeapMemoryUsage().getMax() / (1024 * 1024);
		if (outOfMemory && showOutOfMemoryDialog) {
			outOfMemory = false;
			showOutOfMemoryDialog = false;
			cur = max;
			JOptionPane.showMessageDialog(memoryButton,
					TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
							+ TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message2"), //$NON-NLS-1$
					TrackerRes.getString("Tracker.Dialog.OutOfMemory.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
		}
		String mem = TrackerRes.getString("TTrackBar.Button.Memory") + " "; //$NON-NLS-1$ //$NON-NLS-2$
		String of = TrackerRes.getString("DynamicSystem.Parameter.Of") + " "; //$NON-NLS-1$ //$NON-NLS-2$
		memoryButton.setText(mem + cur + "MB " + of + max + "MB"); //$NON-NLS-1$ //$NON-NLS-2$
		double used = ((double) cur) / max;
		memoryButton.setForeground(used > 0.8 ? Color.red : Color.black);
	}

}
