/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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
import java.awt.Graphics;
//import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.opensourcephysics.cabrillo.tracker.PageTView.TabView;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.ClipInspector;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FontSizer;

import javajs.async.SwingJSUtils.Performance;

/**
 * This is the main toolbar for Tracker.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TToolBar extends JToolBar implements PropertyChangeListener {

	// static fields
	final protected static Map<TrackerPanel, TToolBar> toolbars = new HashMap<TrackerPanel, TToolBar>();
	final protected static int[] trailLengths = { 1, 4, 15, 0 };
	final protected static String[] trailLengthNames = { "none", "short", "long", "full" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	final protected static Icon newTrackIcon;
	final protected static Icon trackControlIcon, trackControlOnIcon, trackControlDisabledIcon;
	final protected static Icon zoomIcon;
	final protected static Icon clipOffIcon, clipOnIcon;
	final protected static Icon axesOffIcon, axesOnIcon;
	final protected static Icon calibrationToolsOffIcon, calibrationToolsOnIcon;
	final protected static Icon calibrationToolsOffRolloverIcon, calibrationToolsOnRolloverIcon;
	final protected static Icon pointsOffIcon, pointsOnIcon;
	final protected static Icon velocOffIcon, velocOnIcon;
	final protected static Icon accelOffIcon, accelOnIcon;
	final protected static Icon traceOffIcon, traceOnIcon;
	final protected static Icon labelsOffIcon, labelsOnIcon;
	final protected static Icon stretchOffIcon, stretchOnIcon;
	final protected static Icon xmassOffIcon, xmassOnIcon;
	final protected static Icon fontSmallerIcon, fontBiggerIcon, fontSmallerDisabledIcon, fontBiggerDisabledIcon;
	final protected static Icon autotrackerOffIcon, autotrackerOnIcon, autotrackerDisabledIcon;
	final protected static Icon infoIcon, refreshIcon, htmlIcon, htmlDisabledIcon;
	final protected static Icon[] trailIcons = new Icon[4];
	final protected static int[] stretchValues = new int[] { 1, 2, 3, 4, 6, 8, 12, 16, 24, 32 };
	final protected static Icon separatorIcon;
	final protected static Icon checkboxOffIcon, checkboxOnIcon;
	final protected static ResizableIcon checkboxOnDisabledIcon;
	final protected static Icon pencilOffIcon, pencilOnIcon, pencilOffRolloverIcon, pencilOnRolloverIcon;
	final protected static NumberFormat zoomFormat = NumberFormat.getNumberInstance();

	public static int defTrailLength = trailLengths[Tracker.trailLengthIndex];

	// false
	public static final String REFRESH_PAGETVIEW_TABS = "PageTView.tabs";
	public static final String REFRESH_PAGETVIEW_TITLE = "PageTView.title";
	public static final String REFRESH_PAGETVIEW_URL = "PageTView.url";
	public static final String REFRESH_LINEPROFILE = "LineProfile";
	public static final String REFRESH_TFRAME_LOCALE = "TFrame.locale";
	public static final String REFRESH_TFRAME_LOCALE2 = "TFrame.locale2 ??";
	protected static final String REFRESH__CLIP_SETTINGS_HIDDEN = "clip settings hidden";
	protected static final String REFRESH__CLIP_SETTINGS_ACTION = "clip settings action";
	private static final String REFRESH__PROPERTY_VIDEO = "property video";
	private static final String REFRESH__PROPERTY_SELECTED_TRACK = "property selected track";

	public static final String REFRESH_PREFS_TRUE = "PrefsDialog";
	public static final String REFRESH_TFRAME_REFRESH_TRUE = "TFrame.refresh";
	protected static final String REFRESH__REFRESH_ACTION_TRUE = "refresh action";
	protected static final String REFRESH__TRAIL_BUTTON_ACTION_TRUE = "trail button action";
	protected static final String REFRESH__VSTRETCH_ACTION_TRUE = "vstretch action";
	protected static final String REFRESH__ASTRETCH_ACTION_TRUE = "astretch action";
	protected static final String REFRESH__STRETCHOFF_ACTION_TRUE = "stretchoff action";
	private static final String REFRESH__CREATE_GUI_TRUE = "create gui";
	private static final String REFRESH__PROPERTY_TRACK_TRUE = "property track";
	private static final String REFRESH__PROPERTY_CLEAR_TRUE = "property track clear";

	// instance fields
	/** effectively final */
	protected TrackerPanel trackerPanel; // manages & displays track data

	final protected WindowListener infoListener;
	final protected JButton openButton, openBrowserButton, saveButton, saveZipButton;
	final protected TButton newTrackButton;
	final protected JButton trackControlButton, clipSettingsButton;
	final protected CalibrationButton calibrationButton;
	final protected DrawingButton drawingButton;
	final protected JButton axesButton, zoomButton, autotrackerButton;
	final protected JButton traceVisButton, pVisButton, vVisButton, aVisButton;
	final protected JButton xMassButton, trailButton, labelsButton, stretchButton;
	final protected JButton fontSmallerButton, fontBiggerButton;
	final protected JPopupMenu newPopup = new JPopupMenu();
	final protected JPopupMenu selectPopup = new JPopupMenu();
	final protected JMenu vStretchMenu, aStretchMenu;
	protected ButtonGroup vGroup, aGroup;
	final protected JMenuItem showTrackControlItem, selectNoneItem, stretchOffItem;
	final protected JButton notesButton, refreshButton, desktopButton;
	final protected Component toolbarFiller;
	final protected JMenu cloneMenu;
	final protected ComponentListener clipSettingsDialogListener;
	final protected ArrayList<PageTView.TabData> pageViewTabs = new ArrayList<PageTView.TabData>();
    protected JPopupMenu zoomPopup;
	
	static {

		newTrackIcon = Tracker.getResourceIcon("poof.gif", true); //$NON-NLS-1$
		trackControlIcon = Tracker.getResourceIcon("track_control.gif", true); //$NON-NLS-1$
		trackControlOnIcon = Tracker.getResourceIcon("track_control_on.gif", true); //$NON-NLS-1$
		trackControlDisabledIcon = Tracker.getResourceIcon("track_control_disabled.gif", true); //$NON-NLS-1$
		zoomIcon = Tracker.getResourceIcon("zoom.gif", true); //$NON-NLS-1$
		clipOffIcon = Tracker.getResourceIcon("clip_off.gif", true); //$NON-NLS-1$
		clipOnIcon = Tracker.getResourceIcon("clip_on.gif", true); //$NON-NLS-1$
		axesOffIcon = Tracker.getResourceIcon("axes.gif", true); //$NON-NLS-1$
		axesOnIcon = Tracker.getResourceIcon("axes_on.gif", true); //$NON-NLS-1$
		calibrationToolsOffIcon = Tracker.getResourceIcon("calibration_tool.gif", true); //$NON-NLS-1$
		calibrationToolsOnIcon = Tracker.getResourceIcon("calibration_tool_on.gif", true); //$NON-NLS-1$
		calibrationToolsOffRolloverIcon = Tracker.getResourceIcon("calibration_tool_rollover.gif", true); //$NON-NLS-1$
		calibrationToolsOnRolloverIcon = Tracker.getResourceIcon("calibration_tool_on_rollover.gif", true); //$NON-NLS-1$
		pointsOffIcon = Tracker.getResourceIcon("positions.gif", true); //$NON-NLS-1$
		pointsOnIcon = Tracker.getResourceIcon("positions_on.gif", true); //$NON-NLS-1$
		velocOffIcon = Tracker.getResourceIcon("velocities.gif", true); //$NON-NLS-1$
		velocOnIcon = Tracker.getResourceIcon("velocities_on.gif", true); //$NON-NLS-1$
		accelOffIcon = Tracker.getResourceIcon("accel.gif", true); //$NON-NLS-1$
		accelOnIcon = Tracker.getResourceIcon("accel_on.gif", true); //$NON-NLS-1$
		traceOffIcon = Tracker.getResourceIcon("trace.gif", true); //$NON-NLS-1$
		traceOnIcon = Tracker.getResourceIcon("trace_on.gif", true); //$NON-NLS-1$
		labelsOffIcon = Tracker.getResourceIcon("labels.gif", true); //$NON-NLS-1$
		labelsOnIcon = Tracker.getResourceIcon("labels_on.gif", true); //$NON-NLS-1$
		stretchOffIcon = Tracker.getResourceIcon("stretch.gif", true); //$NON-NLS-1$
		stretchOnIcon = Tracker.getResourceIcon("stretch_on.gif", true); //$NON-NLS-1$
		xmassOffIcon = Tracker.getResourceIcon("x_mass.gif", true); //$NON-NLS-1$
		xmassOnIcon = Tracker.getResourceIcon("x_mass_on.gif", true); //$NON-NLS-1$
		fontSmallerIcon = Tracker.getResourceIcon("font_smaller.gif", true); //$NON-NLS-1$
		fontBiggerIcon = Tracker.getResourceIcon("font_bigger.gif", true); //$NON-NLS-1$
		fontSmallerDisabledIcon = Tracker.getResourceIcon("font_smaller_disabled.gif", true); //$NON-NLS-1$
		fontBiggerDisabledIcon = Tracker.getResourceIcon("font_bigger_disabled.gif", true); //$NON-NLS-1$
		autotrackerOffIcon = Tracker.getResourceIcon("autotrack_off.gif", true); //$NON-NLS-1$
		autotrackerOnIcon = Tracker.getResourceIcon("autotrack_on.gif", true); //$NON-NLS-1$
		autotrackerDisabledIcon = Tracker.getResourceIcon("autotrack_disabled.gif", true); //$NON-NLS-1$
		infoIcon = Tracker.getResourceIcon("info.gif", true); //$NON-NLS-1$
		refreshIcon = Tracker.getResourceIcon("refresh.gif", true); //$NON-NLS-1$
		// oops refreshIcon = new
		// ResizableIcon(Tracker.getClassResource("resources/images/refresh.gif"));
		// //$NON-NLS-1$
		htmlIcon = Tracker.getResourceIcon("html.gif", true); //$NON-NLS-1$
		htmlDisabledIcon = Tracker.getResourceIcon("html_disabled.gif", true); //$NON-NLS-1$
		trailIcons[0] = Tracker.getResourceIcon("trails_off.gif", true); //$NON-NLS-1$
		trailIcons[1] = Tracker.getResourceIcon("trails_1.gif", true); //$NON-NLS-1$
		trailIcons[2] = Tracker.getResourceIcon("trails_2.gif", true); //$NON-NLS-1$
		trailIcons[3] = Tracker.getResourceIcon("trails_on.gif", true); //$NON-NLS-1$
		separatorIcon = Tracker.getResourceIcon("separator.gif", true); //$NON-NLS-1$
		checkboxOffIcon = Tracker.getResourceIcon("box_unchecked.gif", true); //$NON-NLS-1$
		checkboxOnIcon = Tracker.getResourceIcon("box_checked.gif", true); //$NON-NLS-1$
		checkboxOnDisabledIcon = (ResizableIcon) Tracker.getResourceIcon("box_checked_disabled.gif", true); //$NON-NLS-1$
		pencilOffIcon = Tracker.getResourceIcon("pencil_off.gif", true); //$NON-NLS-1$
		pencilOnIcon = Tracker.getResourceIcon("pencil_on.gif", true); //$NON-NLS-1$
		pencilOffRolloverIcon = Tracker.getResourceIcon("pencil_off_rollover.gif", true); //$NON-NLS-1$
		pencilOnRolloverIcon = Tracker.getResourceIcon("pencil_on_rollover.gif", true); //$NON-NLS-1$
		zoomFormat.setMaximumFractionDigits(0);
	}

	protected boolean refreshing; // true when refreshing toolbar
	protected int vStretch = 1, aStretch = 1;
	protected int trailLength = defTrailLength;
	protected boolean notYetCalibrated = true;
	protected int toolbarComponentHeight;
	private AbstractAction zoomAction;

	/**
	 * TToolBar constructor.
	 *
	 * @param panel the tracker panel
	 */
	private TToolBar(TrackerPanel panel) {
		trackerPanel = panel;
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, this);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this);
// BH testing final status
//		createGUI();
//	}
//
//	/**
//	 * Creates the GUI.
//	 */
//	protected void createGUI() {
		setFloatable(false);
		// create buttons
		Map<String, AbstractAction> actions = TActions.getActions(trackerPanel);
		// open and save buttons
		openButton = new TButton(actions.get("open")); //$NON-NLS-1$
		openBrowserButton = new TButton(actions.get("openBrowser")); //$NON-NLS-1$
		saveButton = new TButton(actions.get("save")); //$NON-NLS-1$
		saveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				String fileName = trackerPanel.getTitle();
				String extension = XML.getExtension(fileName);
				if (extension == null || !extension.equals("trk")) //$NON-NLS-1$
					fileName = XML.stripExtension(fileName) + ".trk"; //$NON-NLS-1$
				saveButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Save.Tooltip")); //$NON-NLS-1$
			}
		});
		saveZipButton = new TButton(actions.get("saveZip")); //$NON-NLS-1$
		saveZipButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final ExportZipDialog zipDialog = ExportZipDialog.getDialog(trackerPanel);
				final boolean isVis = zipDialog.isVisible();
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						zipDialog.setVisible(!isVis);
					}
				};
				SwingUtilities.invokeLater(runner);
			}
		});
		// clip settings button
		clipSettingsDialogListener = new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				refresh(REFRESH__CLIP_SETTINGS_HIDDEN);
			}
		};
		clipSettingsButton = new TButton(clipOffIcon, clipOnIcon);
		clipSettingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
				ClipControl clipControl = trackerPanel.getPlayer().getClipControl();
				TFrame frame = trackerPanel.getTFrame();
				ClipInspector inspector = clip.getClipInspector(clipControl, frame);
				if (inspector.isVisible()) {
					inspector.setVisible(false);
					return;
				}
				FontSizer.setFonts(inspector, FontSizer.getLevel());
				inspector.pack();
				Point p0 = new JFrame().getLocation();
				Point loc = inspector.getLocation();
				if ((loc.x == p0.x) && (loc.y == p0.y)) {
					// center inspector on the main view
					Rectangle rect = trackerPanel.getVisibleRect();
					Point p = frame.getMainView(trackerPanel).scrollPane.getLocationOnScreen();
					int x = p.x + (rect.width - inspector.getBounds().width) / 2;
					int y = p.y + (rect.height - inspector.getBounds().height) / 2;
					inspector.setLocation(x, y);
				}
				inspector.initialize();
				inspector.removeComponentListener(clipSettingsDialogListener);
				inspector.addComponentListener(clipSettingsDialogListener);
				inspector.setVisible(true);
				refresh(TToolBar.REFRESH__CLIP_SETTINGS_ACTION);
			}

		});
		// axes button
		axesButton = new TButton(axesOffIcon, axesOnIcon);
		axesButton.addActionListener(actions.get("axesVisible")); //$NON-NLS-1$

		// calibration button
		calibrationButton = new CalibrationButton();

		// zoom button
		zoomAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// set zoom center to center of current viewport
				Rectangle rect = trackerPanel.scrollPane.getViewport().getViewRect();
				MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
				mainView.setZoomCenter(rect.x + rect.width / 2, rect.y + rect.height / 2);
				String name = e.getActionCommand();
				if (name.equals("auto")) { //$NON-NLS-1$
					trackerPanel.setMagnification(-1);
				} else {
					double mag = Double.parseDouble(name);
					trackerPanel.setMagnification(mag / 100);
				}
				refreshZoomButton();
			}
		};
		
		zoomButton = new TButton(zoomIcon) {
			@Override
			protected JPopupMenu getPopup() {
				return createZoomPopup();
			}
		};
		zoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					trackerPanel.setMagnification(-1);
					if (zoomPopup != null)
						zoomPopup.setVisible(false);
					refreshZoomButton();
				}
			}
		});

		// new track button
		newTrackButton = new TButton(newTrackIcon) {
			@Override
			protected JPopupMenu getPopup() {
				TMenuBar.refreshPopup(trackerPanel, TMenuBar.POPUPMENU_TTOOLBAR_TRACKS, newPopup);
				return newPopup;
			}
		};
		// track control button
		trackControlButton = new TButton(trackControlIcon, trackControlOnIcon);
		trackControlButton.setDisabledIcon(trackControlDisabledIcon);
		trackControlButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TrackControl tc = TrackControl.getControl(trackerPanel);
				tc.setVisible(!tc.isVisible());
			}
		});
		// autotracker button
		autotrackerButton = new TButton(autotrackerOffIcon, autotrackerOnIcon);
		autotrackerButton.setDisabledIcon(autotrackerDisabledIcon);
		autotrackerButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); // workaround--shouldn't need this...
			}
		});
		autotrackerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autotrackerButton.setSelected(!autotrackerButton.isSelected());
				AutoTracker autoTracker = trackerPanel.getAutoTracker(true);
				if (autoTracker.getTrack() == null) {
					TTrack track = trackerPanel.getSelectedTrack();
					if (track == null) {
						for (TTrack next : trackerPanel.getTracks()) {
							if (!next.isAutoTrackable())
								continue;
							track = next;
							break;
						}
					}
					autoTracker.setTrack(track);
				}
				autoTracker.getWizard().setVisible(autotrackerButton.isSelected());
				TFrame.repaintT(trackerPanel);
			}
		});
		final Action refreshAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton button = (JButton) e.getSource();
				button.setSelected(!button.isSelected());
				refresh(TToolBar.REFRESH__REFRESH_ACTION_TRUE);
			}
		};
		// p visible button
		pVisButton = new TButton(pointsOffIcon, pointsOnIcon);
		pVisButton.setSelected(true);
		pVisButton.addActionListener(refreshAction);
		// v visible button
		vVisButton = new TButton(velocOffIcon, velocOnIcon);
		vVisButton.addActionListener(refreshAction);
		// a visible button
		aVisButton = new TButton(accelOffIcon, accelOnIcon);
		aVisButton.addActionListener(refreshAction);
		// trace visible button
		traceVisButton = new TButton(traceOffIcon, traceOnIcon);
		traceVisButton.addActionListener(refreshAction);
		// trail button
		trailButton = new TButton() {
			@Override
			protected JPopupMenu getPopup() {
				JPopupMenu popup = new JPopupMenu();
				ActionListener listener = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int n = Integer.parseInt(e.getActionCommand());
						trailLength = trailLengths[n];
						trailButton.setSelected(trailLength != 1);
						refresh(REFRESH__TRAIL_BUTTON_ACTION_TRUE);
						TFrame.repaintT(trackerPanel);
					}
				};
				ButtonGroup group = new ButtonGroup();
				JMenuItem item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.NoTrail")); //$NON-NLS-1$
				item.setSelected(trailLength == trailLengths[0]);
				item.setActionCommand(String.valueOf(0));
				item.addActionListener(listener);
				popup.add(item);
				group.add(item);
				item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.ShortTrail")); //$NON-NLS-1$
				item.setSelected(trailLength == trailLengths[1]);
				item.setActionCommand(String.valueOf(1));
				item.addActionListener(listener);
				popup.add(item);
				group.add(item);
				item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.LongTrail")); //$NON-NLS-1$
				item.setSelected(trailLength == trailLengths[2]);
				item.setActionCommand(String.valueOf(2));
				item.addActionListener(listener);
				popup.add(item);
				group.add(item);
				item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.FullTrail")); //$NON-NLS-1$
				item.setSelected(trailLength == trailLengths[3]);
				item.setActionCommand(String.valueOf(3));
				item.addActionListener(listener);
				popup.add(item);
				group.add(item);
				FontSizer.setFonts(popup, FontSizer.getLevel());
				return popup;
			}
		};
		trailButton.setSelected(true);

		// labels visible button
		labelsButton = new TButton(labelsOffIcon, labelsOnIcon);
		labelsButton.setSelected(!Tracker.hideLabels);
		labelsButton.addActionListener(refreshAction);
		// x mass button
		xMassButton = new TButton(xmassOffIcon, xmassOnIcon);
		xMassButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshAction.actionPerformed(e);
				TTrack track = trackerPanel.getSelectedTrack();
				if (track instanceof PointMass) {
					trackerPanel.getTFrame().getTrackBar(trackerPanel).refresh();
				}
			}
		});
		// stretch button
		vStretchMenu = new JMenu();
		vStretchMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				if (vGroup != null)
					return;
				vGroup = new ButtonGroup();
				for (int i = 0; i < stretchValues.length; i++) {
					String s = String.valueOf(stretchValues[i]);
					JMenuItem item = new JRadioButtonMenuItem("x" + s); //$NON-NLS-1$
					if (i == 0)
						item.setText(TrackerRes.getString("TrackControl.StretchVectors.None")); //$NON-NLS-1$
					item.setActionCommand(s);
					item.setSelected(vStretch == stretchValues[i]);
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							int n = Integer.parseInt(e.getActionCommand());
							trackerPanel.setSelectedPoint(null);
							trackerPanel.selectedSteps.clear();
							vStretch = n;
							refresh(REFRESH__VSTRETCH_ACTION_TRUE);
						}
					});
					vGroup.add(item);
					vStretchMenu.add(item);
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});
		aStretchMenu = new JMenu();
		aStretchMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				if (aGroup != null)
					return;
				aGroup = new ButtonGroup();
				for (int i = 0; i < stretchValues.length; i++) {
					String s = String.valueOf(stretchValues[i]);
					JMenuItem item = new JRadioButtonMenuItem("x" + s); //$NON-NLS-1$
					if (i == 0)
						item.setText(TrackerRes.getString("TrackControl.StretchVectors.None")); //$NON-NLS-1$
					item.setActionCommand(s);
					item.setSelected(aStretch == stretchValues[i]);
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							int n = Integer.parseInt(e.getActionCommand());
							trackerPanel.setSelectedPoint(null);
							trackerPanel.selectedSteps.clear();
							aStretch = n;
							refresh(REFRESH__ASTRETCH_ACTION_TRUE);
						}
					});
					aGroup.add(item);
					aStretchMenu.add(item);
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});
		stretchOffItem = new JMenuItem();
		stretchOffItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				vStretch = 1;
				aStretch = 1;
				refresh(REFRESH__STRETCHOFF_ACTION_TRUE);
			}
		});

		stretchButton = new TButton(stretchOffIcon, stretchOnIcon) {
			@Override
			protected JPopupMenu getPopup() {
				JPopupMenu popup = new JPopupMenu();
				popup.add(vStretchMenu);
				popup.add(aStretchMenu);
				popup.addSeparator();
				popup.add(stretchOffItem);
				FontSizer.setFonts(popup, FontSizer.getLevel());
				return popup;
			}
		};

		// font buttons
		fontSmallerButton = new TButton(fontSmallerIcon);
		fontSmallerButton.setDisabledIcon(fontSmallerDisabledIcon);
		fontSmallerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = FontSizer.getLevel();
				FontSizer.setLevel(i - 1);
				fontSmallerButton.setEnabled(FontSizer.getLevel() > FontSizer.MIN_LEVEL);
				fontBiggerButton.setEnabled(FontSizer.getLevel() < FontSizer.MAX_LEVEL);
			}
		});
		fontBiggerButton = new TButton(fontBiggerIcon);
		fontBiggerButton.setDisabledIcon(fontBiggerDisabledIcon);
		fontBiggerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = FontSizer.getLevel();
				FontSizer.setLevel(i + 1);
				fontSmallerButton.setEnabled(FontSizer.getLevel() > FontSizer.MIN_LEVEL);
				fontBiggerButton.setEnabled(FontSizer.getLevel() < FontSizer.MAX_LEVEL);
			}
		});

		// horizontal glue for right end of toolbar
		toolbarFiller = Box.createHorizontalGlue();
		// info button
		infoListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				notesButton.setSelected(false);
			}
		};
		drawingButton = new DrawingButton();
		notesButton = new TButton(infoIcon);
		notesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doNotesAction();
			}
		});
		refreshButton = new TButton(refreshIcon) {
			@Override
			protected JPopupMenu getPopup() {
				return getRefreshBtnPopup();
			}

		};
		desktopButton = new TButton(htmlIcon) {
			@Override
			protected JPopupMenu getPopup() {
				return getDesktopBtnPopup();
			}

		};
		desktopButton.setDisabledIcon(htmlDisabledIcon);

		// create menu items
		cloneMenu = new JMenu();
		showTrackControlItem = new JCheckBoxMenuItem();
		showTrackControlItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TrackControl tc = TrackControl.getControl(trackerPanel);
				tc.setVisible(showTrackControlItem.isSelected());
			}
		});
		selectNoneItem = new JMenuItem();
		selectNoneItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				trackerPanel.setSelectedTrack(null);
			}
		});
		refresh(REFRESH__CREATE_GUI_TRUE);
		validate();
	}

	protected JPopupMenu createZoomPopup() {
		if (zoomPopup == null) {
			zoomPopup = new JPopupMenu();
		}
		zoomPopup.removeAll();
		JMenuItem item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ToFit")); //$NON-NLS-1$
		item.setActionCommand("auto"); //$NON-NLS-1$
		item.addActionListener(zoomAction);
		zoomPopup.add(item);
		zoomPopup.addSeparator();
		for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
			int n = (int) (100 * TrackerPanel.ZOOM_LEVELS[i]);
			String m = String.valueOf(n);
			item = new JMenuItem(m + "%"); //$NON-NLS-1$
			item.setActionCommand(m);
			item.addActionListener(zoomAction);
			zoomPopup.add(item);
		}
		// FontSizer.setFonts(zoomPopup, FontSizer.getLevel());
		return zoomPopup;
	}

	protected JPopupMenu getDesktopBtnPopup() {
		JPopupMenu popup = new JPopupMenu();
		if (!trackerPanel.supplementalFilePaths.isEmpty()) {
			JMenu fileMenu = new JMenu(TrackerRes.getString("TToolbar.Button.Desktop.Menu.OpenFile")); //$NON-NLS-1$
			popup.add(fileMenu);
			for (String next : trackerPanel.supplementalFilePaths) {
				String title = XML.getName(next);
				String path = next;
				JMenuItem item = new JMenuItem(title);
				item.setActionCommand(path);
				item.setToolTipText(path);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String path = e.getActionCommand();
						OSPDesktop.displayURL(path);
					}
				});
				fileMenu.add(item);
			}
		}
		if (!pageViewTabs.isEmpty()) {
			JMenu pageMenu = new JMenu(TrackerRes.getString("TToolbar.Button.Desktop.Menu.OpenPage")); //$NON-NLS-1$
			popup.add(pageMenu);
			for (PageTView.TabData next : pageViewTabs) {
				if (next.url == null)
					continue;
				String title = next.title;
				String path = trackerPanel.pageViewFilePaths.get(next.text);
				if (path == null) {
					path = next.url.toExternalForm();
				}
				JMenuItem item = new JMenuItem(title);
				item.setActionCommand(path);
				item.setToolTipText(path);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String path = e.getActionCommand();
						OSPDesktop.displayURL(path);
					}
				});
				pageMenu.add(item);
			}
		}
		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	protected JPopupMenu getRefreshBtnPopup() {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem item = new JMenuItem(TrackerRes.getString("TToolbar.Button.Refresh.Popup.RefreshNow")); //$NON-NLS-1$
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// offer to clear RGBRegion data that are valid and visible in a view
				ArrayList<RGBRegion> regions = trackerPanel.getDrawables(RGBRegion.class);
				ArrayList<RGBRegion> regionsToClear = new ArrayList<RGBRegion>();
				if (!regions.isEmpty()) {
					for (RGBRegion next : regions) {
						if (trackerPanel.isTrackViewDisplayed(next) && next.dataValid) {
							regionsToClear.add(next);
						}
					}
				}
				if (!regionsToClear.isEmpty()) {
					// get user confirmation
					String list = " "; //$NON-NLS-1$
					for (RGBRegion next : regionsToClear) {
						list += next.getName() + ", "; //$NON-NLS-1$
					}
					list = list.substring(0, list.length() - 2);
					int i = JOptionPane.showConfirmDialog(trackerPanel.getTopLevelAncestor(),
							TrackerRes.getString("TToolBar.Dialog.ClearRGB.Message1") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
					TrackerRes.getString("TToolBar.Dialog.ClearRGB.Message2"), //$NON-NLS-1$
							TrackerRes.getString("TToolBar.Dialog.ClearRGB.Title") + list, //$NON-NLS-1$
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (i == JOptionPane.YES_OPTION) {
						for (RGBRegion next : regionsToClear) {
							next.clearData();
						}
					}
				}
				trackerPanel.refreshTrackData(DataTable.MODE_REFRESH);
				trackerPanel.eraseAll();
				trackerPanel.repaintDirtyRegion();
			}
		});
		popup.add(item);
		popup.addSeparator();
		item = new JCheckBoxMenuItem(TrackerRes.getString("TToolbar.Button.Refresh.Popup.AutoRefresh")); //$NON-NLS-1$
		item.setSelected(trackerPanel.isAutoRefresh());
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();
				trackerPanel.setAutoRefresh(item.isSelected());
				if (trackerPanel.isAutoRefresh()) {
					trackerPanel.refreshTrackData(DataTable.MODE_REFRESH);
					trackerPanel.eraseAll();
					trackerPanel.repaintDirtyRegion();
				}
			}
		});
		popup.add(item);
		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	protected void doNotesAction() {
		TFrame frame = trackerPanel.getTFrame();
		if (frame != null && frame.getTrackerPanel(frame.getSelectedTab()) == trackerPanel) {
			frame.notesDialog.removeWindowListener(infoListener);
			frame.notesDialog.addWindowListener(infoListener);
			// position info dialog if first time shown
			// or if trackerPanel specifies location
			Point p0 = new JFrame().getLocation();
			if (trackerPanel.infoX != Integer.MIN_VALUE || frame.notesDialog.getLocation().x == p0.x) {
				int x, y;
				Point p = frame.getLocationOnScreen();
				if (trackerPanel.infoX != Integer.MIN_VALUE) {
					Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					x = Math.max(p.x + trackerPanel.infoX, 0);
					x = Math.min(x, dim.width - frame.notesDialog.getWidth());
					y = Math.max(p.y + trackerPanel.infoY, 0);
					y = Math.min(y, dim.height - frame.notesDialog.getHeight());
					trackerPanel.infoX = Integer.MIN_VALUE;
				} else {
					Point pleft = TToolBar.this.getLocationOnScreen();
					Dimension dim = frame.notesDialog.getSize();
					Dimension wdim = TToolBar.this.getSize();
					x = pleft.x + (int) (0.5 * (wdim.width - dim.width));
					y = p.y + 16;
				}
				frame.notesDialog.setLocation(x, y);
			}
			notesButton.setSelected(!frame.notesDialog.isVisible());
			frame.notesDialog.setVisible(notesButton.isSelected());
			trackerPanel.refreshNotesDialog();
		}
	}

	protected void refreshZoomButton() {
		double zoom = trackerPanel.getMagnification() * 100;
		zoomButton.setText(zoomFormat.format(zoom) + "%"); //$NON-NLS-1$
	}

	private Timer refreshTimer;

	private boolean disposed;

	private int enabledCount;
	private boolean allowRebuild = true;

	@Override
	protected void paintChildren(Graphics g) {
		if (!OSPRuntime.isJS)
			super.paintChildren(g);

	}

	/**
	 * Refreshes the GUI using a private singleton timer.
	 * 
	 * @param refreshTrackProperties true to refresh the track display properties
	 */
	protected synchronized void refresh(String whereFrom) {
		if (disposed || !trackerPanel.isPaintable())
			return;
		OSPLog.debug("TToolBar refresh from " + whereFrom);
		boolean doRefresh = false;
		switch (whereFrom) {
		case REFRESH_TFRAME_LOCALE:
		case REFRESH_TFRAME_LOCALE2:
			doRefresh = true;
			aGroup = vGroup = null;
			break;
		case REFRESH_PREFS_TRUE:
		case REFRESH_TFRAME_REFRESH_TRUE:
		case REFRESH__REFRESH_ACTION_TRUE:
		case REFRESH__TRAIL_BUTTON_ACTION_TRUE:
		case REFRESH__VSTRETCH_ACTION_TRUE:
		case REFRESH__ASTRETCH_ACTION_TRUE:
		case REFRESH__STRETCHOFF_ACTION_TRUE:
		case REFRESH__CREATE_GUI_TRUE:
		case REFRESH__PROPERTY_TRACK_TRUE:
		case REFRESH__PROPERTY_CLEAR_TRUE:
			doRefresh = true;
			break;
		}

		boolean refreshTrackProperties = doRefresh;
		if (refreshTimer != null) {
			refreshTimer.stop();
		}
		refreshTimer = new Timer(200, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!disposed)
					refreshAsync(refreshTrackProperties);
				refreshTimer = null;
			}

		});
		refreshTimer.setRepeats(false);
		refreshTimer.start();
	}

	/**
	 * Not while playing
	 * 
	 * @param b
	 */
	public void setAllowRefresh(boolean b) {
		allowRebuild = b;
	}

	protected void refreshAsync(boolean refreshTrackProperties) {
		long t0 = Performance.now(0);
		OSPLog.debug(Performance.timeCheckStr("TToolBar refreshAsync", Performance.TIME_MARK));
		refreshing = true; // signals listeners that items are being refreshed (not implemented)
		int enabledCount = trackerPanel.getEnabledCount();
		boolean trackerPanelTainted = (enabledCount != this.enabledCount);
		this.enabledCount = enabledCount;
		if (trackerPanelTainted && allowRebuild) {
			rebuild();
		}
		checkEnabled(refreshTrackProperties);
		refreshing = false;
		OSPLog.debug("!!! " + Performance.now(t0) + " TToolBar refresh async");
	}

	private void rebuild() {
		// assemble buttons
		removeAll();
		if (org.opensourcephysics.display.OSPRuntime.applet == null) {
			if (trackerPanel.isEnabled("file.open")) { //$NON-NLS-1$
				add(openButton);
			}
			if (trackerPanel.isEnabled("file.save")) { //$NON-NLS-1$
				add(saveButton);
			}
			if (getComponentCount() > 0)
				add(getSeparator());
			if (trackerPanel.isEnabled("file.library")) { //$NON-NLS-1$
				add(openBrowserButton);
				if (trackerPanel.isEnabled("file.save")) { //$NON-NLS-1$
					add(saveZipButton);
				}
			}
			if (getComponentCount() > 0)
				add(getSeparator());
		}
		boolean addSeparator = false;
		if (trackerPanel.isEnabled("button.clipSettings")) {//$NON-NLS-1$
			add(clipSettingsButton);
			addSeparator = true;
		}
		if (trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
			add(calibrationButton);
			addSeparator = true;
		}
		if (trackerPanel.isEnabled("button.axes")) {//$NON-NLS-1$
			add(axesButton);
			addSeparator = true;
		}
		if (addSeparator)
			add(getSeparator());
		if (trackerPanel.isCreateTracksEnabled()) {
			add(newTrackButton);
		}
		add(trackControlButton);
		if (trackerPanel.isEnabled("track.autotrack")) //$NON-NLS-1$
			add(autotrackerButton);
		add(getSeparator());
		add(zoomButton);
		add(getSeparator());
		if (trackerPanel.isEnabled("button.trails") //$NON-NLS-1$
				|| trackerPanel.isEnabled("button.labels")) { //$NON-NLS-1$
			if (trackerPanel.isEnabled("button.trails")) //$NON-NLS-1$
				add(trailButton);
			if (trackerPanel.isEnabled("button.labels")) //$NON-NLS-1$
				add(labelsButton);
			add(getSeparator());
		}
		if (trackerPanel.isEnabled("button.path") //$NON-NLS-1$
				|| trackerPanel.isEnabled("button.x") //$NON-NLS-1$
				|| trackerPanel.isEnabled("button.v") //$NON-NLS-1$
				|| trackerPanel.isEnabled("button.a")) {//$NON-NLS-1$
			if (trackerPanel.isEnabled("button.path")) //$NON-NLS-1$
				add(traceVisButton);
			if (trackerPanel.isEnabled("button.x")) //$NON-NLS-1$
				add(pVisButton);
			if (trackerPanel.isEnabled("button.v")) //$NON-NLS-1$
				add(vVisButton);
			if (trackerPanel.isEnabled("button.a")) //$NON-NLS-1$
				add(aVisButton);
			add(getSeparator());
		}
		if (trackerPanel.isEnabled("button.stretch") //$NON-NLS-1$
				|| trackerPanel.isEnabled("button.xMass")) { //$NON-NLS-1$
			if (trackerPanel.isEnabled("button.stretch")) //$NON-NLS-1$
				add(stretchButton);
			if (trackerPanel.isEnabled("button.xMass")) //$NON-NLS-1$
				add(xMassButton);
			add(getSeparator());
		}
		add(fontSmallerButton);
		add(fontBiggerButton);
		add(getSeparator());
		add(toolbarFiller);
		if (trackerPanel.isEnabled("button.drawing")) //$NON-NLS-1$
			add(drawingButton);
		add(desktopButton);
		add(notesButton);
		add(refreshButton);

//		FontSizer.setFont(newTrackButton);
//		FontSizer.setFont(zoomButton);
		OSPLog.debug(Performance.timeCheckStr("TToolBar rebuild", Performance.TIME_MARK));

		validate();

		OSPLog.debug(Performance.timeCheckStr("TToolBar rebuild validate", Performance.TIME_MARK));

		TFrame.repaintT(this);
	}

	private void checkEnabled(boolean refreshTracks) {
		refreshZoomButton();
		calibrationButton.refresh();
		drawingButton.refresh();
		stretchButton.setSelected(vStretch > 1 || aStretch > 1);
		stretchOffItem.setText(TrackerRes.getString("TToolBar.MenuItem.StretchOff")); //$NON-NLS-1$
		stretchOffItem.setEnabled(vStretch > 1 || aStretch > 1);
		setMenuText();
		fontSmallerButton.setEnabled(FontSizer.getLevel() > FontSizer.MIN_LEVEL);
		fontBiggerButton.setEnabled(FontSizer.getLevel() < FontSizer.MAX_LEVEL);
		if (trackerPanel.getPlayer() != null) {
			VideoClip clip = trackerPanel.getPlayer().getVideoClip();
			ClipInspector inspector = clip.getClipInspector();
			clipSettingsButton.setSelected(inspector != null && inspector.isVisible());
		}
		CoordAxes axes = trackerPanel.getAxes();
		if (axes != null) {
			axesButton.setSelected(axes.isVisible());
			axes.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
			axes.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
		}
		ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
		trackControlButton.setEnabled(!tracks.isEmpty());
		autotrackerButton.setEnabled(trackerPanel.getVideo() != null);
		// refresh all tracks
		if (refreshTracks) {
			refreshTracks();
		}
		OSPLog.debug(Performance.timeCheckStr("TToolBar refreshAsync tracks", Performance.TIME_MARK));
		TPoint pt = trackerPanel.getSelectedPoint();
		if (pt != null)
			pt.showCoordinates(trackerPanel);

		// set trails icon
		for (int i = trailLengths.length; --i >= 0;) {
			if (trailLength == trailLengths[i]) {
				if (trailIcons[i] != trailButton.getIcon())
					trailButton.setIcon(trailIcons[i]);
				FontSizer.setFont(trailButton);
				break;
			}
		}

		// refresh pageViewTabs list
		pageViewTabs.clear();
		TFrame frame = trackerPanel.getTFrame();
		if (frame != null) {
			TView[][] views = frame.getTViews(trackerPanel);
			for (TView[] next : views) {
				for (TView view : next) {
					if (view == null)
						continue;
					if (view.getViewType() == TView.VIEW_PAGE) {
						PageTView page = (PageTView) view;
						for (TabView tab : page.tabs) {
							if (tab.data.url != null) {
								pageViewTabs.add(tab.data);
							}
						}
					}
				}
			}
			sortPageViewTabs();
		}
		OSPLog.debug(Performance.timeCheckStr("TToolBar refreshAsync sortPageView", Performance.TIME_MARK));

		boolean hasPageURLs = !pageViewTabs.isEmpty();
		desktopButton.setEnabled(hasPageURLs || !trackerPanel.supplementalFilePaths.isEmpty());
	}

	private void refreshTracks() {
		ArrayList<TTrack> tracks = trackerPanel.getTracks();
		double totalMass = 0;
		int massCount = 0;
		for (TTrack track : tracks) {
			if (track instanceof PointMass && !(track instanceof CenterOfMass) && !(track instanceof DynamicSystem)) {
				PointMass p = (PointMass) track;
				totalMass += p.getMass();
				massCount++;
			}
		}
		boolean doRepaint = false;
		for (TTrack track : tracks) {
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this); // $NON-NLS-1$
			track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this); // $NON-NLS-1$
			// refresh track display properties from current button states
			track.setTrailLength(trailLength);
			track.setTrailVisible(trailButton.isSelected());
			if (track instanceof PointMass) {
				PointMass p = (PointMass) track;
				p.setTraceVisible(traceVisButton.isSelected());
				p.setPositionVisible(trackerPanel, pVisButton.isSelected());
				p.setVVisible(trackerPanel, vVisButton.isSelected());
				p.setAVisible(trackerPanel, aVisButton.isSelected());
				p.setLabelsVisible(trackerPanel, labelsButton.isSelected());
				Footprint[] footprints = p.getVelocityFootprints();
				for (int i = 0; i < footprints.length; i++) {
					if (footprints[i] instanceof ArrowFootprint) {
						ArrowFootprint arrow = (ArrowFootprint) footprints[i];
						if (xMassButton.isSelected()) {
							arrow.setStretch(vStretch * massCount * p.getMass() / totalMass);
							arrow.setSolidHead(false);
						} else {
							arrow.setStretch(vStretch);
							arrow.setSolidHead(false);
						}
					}
				}
				footprints = p.getAccelerationFootprints();
				for (int i = 0; i < footprints.length; i++) {
					if (footprints[i] instanceof ArrowFootprint) {
						ArrowFootprint arrow = (ArrowFootprint) footprints[i];
						if (xMassButton.isSelected()) {
							arrow.setStretch(aStretch * massCount * p.getMass() / totalMass);
							arrow.setSolidHead(true);
						} else {
							arrow.setStretch(aStretch);
							arrow.setSolidHead(true);
						}
					}
				}
				doRepaint = true;
//				if (false)
//					p.repaint();
			} else if (track instanceof Vector) {
				Vector v = (Vector) track;
				v.setLabelsVisible(labelsButton.isSelected());
				Footprint[] footprints = v.getFootprints();
				for (int i = 0; i < footprints.length; i++) {
					if (footprints[i] instanceof ArrowFootprint) {
						ArrowFootprint arrow = (ArrowFootprint) footprints[i];
						arrow.setStretch(vStretch);
					}
				}
				doRepaint = true;
//				if (false)
//					v.repaint();
			}
		}
		if (doRepaint) {
			for (int i = 0; i < trackerPanel.panelAndWorldViews.size(); i++) {
				trackerPanel.panelAndWorldViews.get(i).repaint();
			}
		}
	}

	private void setMenuText() {
		vStretchMenu.setText(TrackerRes.getString("PointMass.MenuItem.Velocity")); //$NON-NLS-1$
		aStretchMenu.setText(TrackerRes.getString("PointMass.MenuItem.Acceleration")); //$NON-NLS-1$
		openButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Open.Tooltip")); //$NON-NLS-1$
		openBrowserButton.setToolTipText(TrackerRes.getString("TToolBar.Button.OpenBrowser.Tooltip")); //$NON-NLS-1$
		saveZipButton.setToolTipText(TrackerRes.getString("TToolBar.Button.SaveZip.Tooltip")); //$NON-NLS-1$
		clipSettingsButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.ClipSettings.ToolTip")); //$NON-NLS-1$
		axesButton.setToolTipText(TrackerRes.getString("TToolbar.Button.AxesVisible.Tooltip")); //$NON-NLS-1$
		zoomButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Zoom.Tooltip")); //$NON-NLS-1$
		notesButton.setToolTipText(TrackerRes.getString("TActions.Action.Description")); //$NON-NLS-1$
		refreshButton.setToolTipText(TrackerRes.getString("TToolbar.Button.Refresh.Tooltip")); //$NON-NLS-1$
		desktopButton.setToolTipText(TrackerRes.getString("TToolbar.Button.Desktop.Tooltip")); //$NON-NLS-1$
		pVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Positions.ToolTip")); //$NON-NLS-1$
		vVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Velocities.ToolTip")); //$NON-NLS-1$
		aVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Accelerations.ToolTip")); //$NON-NLS-1$
		xMassButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Xmass.ToolTip")); //$NON-NLS-1$
		trailButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Trails.ToolTip")); //$NON-NLS-1$
		labelsButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Labels.ToolTip")); //$NON-NLS-1$
		stretchButton.setToolTipText(TrackerRes.getString("TrackControl.Button.StretchVectors.ToolTip")); //$NON-NLS-1$
		traceVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Trace.ToolTip")); //$NON-NLS-1$
		newTrackButton.setText(TrackerRes.getString("TrackControl.Button.NewTrack")); //$NON-NLS-1$
		newTrackButton.setToolTipText(TrackerRes.getString("TrackControl.Button.NewTrack.ToolTip")); //$NON-NLS-1$
		trackControlButton.setToolTipText(TrackerRes.getString("TToolBar.Button.TrackControl.Tooltip")); //$NON-NLS-1$
		autotrackerButton.setToolTipText(TrackerRes.getString("TToolBar.Button.AutoTracker.Tooltip")); //$NON-NLS-1$
		fontSmallerButton.setToolTipText(TrackerRes.getString("TrackControl.Button.FontSmaller.ToolTip")); //$NON-NLS-1$
		fontBiggerButton.setToolTipText(TrackerRes.getString("TrackControl.Button.FontBigger.ToolTip")); //$NON-NLS-1$
	}

	/**
	 * Disposes of this toolbar
	 */
	public void dispose() {
		disposed = true;
		if (refreshTimer != null)
			refreshTimer.stop();
		refreshTimer = null;
		toolbars.remove(trackerPanel);
		removeAll();
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, this);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT, this);
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this);
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this);
		}
		pageViewTabs.clear();
		trackerPanel = null;
	}

	@Override
	public void finalize() {
		OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

	/**
	 * Responds to the property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String name = e.getPropertyName();
		switch (name) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO:
		case TTrack.PROPERTY_TTRACK_LOCKED:
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT:
			refresh(REFRESH__PROPERTY_VIDEO);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK:
			// refresh info dialog if visible
			trackerPanel.refreshNotesDialog();
			refresh(REFRESH__PROPERTY_SELECTED_TRACK);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION:
			refreshZoomButton();
			break;
		case TTrack.PROPERTY_TTRACK_VISIBLE:
			if (e.getSource() == trackerPanel.getAxes()) {
				axesButton.setSelected(trackerPanel.getAxes().isVisible());
			} else {
				calibrationButton.refresh();
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getOldValue() != null) { // track has been removed
				TTrack track = (TTrack) e.getOldValue();
				trackerPanel.calibrationTools.remove(track);
				trackerPanel.visibleTools.remove(track);
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this); // $NON-NLS-1$
				if (trackerPanel.visibleTools.isEmpty()) {
					calibrationButton.setSelected(false);
				}
			}
			refresh(REFRESH__PROPERTY_TRACK_TRUE);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			for (TTrack track : TTrack.activeTracks.values()) {
				trackerPanel.calibrationTools.remove(track);
				trackerPanel.visibleTools.remove(track);
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this); // $NON-NLS-1$
			}
			calibrationButton.setSelected(false);
			refresh(REFRESH__PROPERTY_CLEAR_TRUE);
			break;
		}
	}

	public static JButton getSeparator() {
		JButton b = new JButton(separatorIcon);
		b.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		b.setOpaque(false);
		b.setContentAreaFilled(false);
		return b;
	}

	private void sortPageViewTabs() {
		Collections.sort(pageViewTabs, new Comparator<PageTView.TabData>() {
			@Override
			public int compare(PageTView.TabData one, PageTView.TabData two) {
				return (one.title.toLowerCase().compareTo(two.title.toLowerCase()));
			}
		});
	}

	/**
	 * Gets the toolbar for the specified tracker panel.
	 *
	 * @param panel the tracker panel
	 * @return the toolbar
	 */
	public static synchronized TToolBar getToolbar(TrackerPanel panel) {
		TToolBar toolbar = toolbars.get(panel);
		if (toolbar == null) {
			toolbar = new TToolBar(panel);
			toolbars.put(panel, toolbar);
		}
		return toolbar;
	}

	/**
	 * Returns an XML.ObjectLoader to save and load object data.
	 *
	 * @return the XML.ObjectLoader
	 */
	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	/**
	 * A class to save and load object data.
	 */
	static class Loader implements XML.ObjectLoader {

		/**
		 * Saves object data.
		 *
		 * @param control the control to save to
		 * @param obj     the TrackerPanel object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			TToolBar toolbar = (TToolBar) obj;
			control.setValue("trace", toolbar.traceVisButton.isSelected()); //$NON-NLS-1$
			control.setValue("position", toolbar.pVisButton.isSelected()); //$NON-NLS-1$
			control.setValue("velocity", toolbar.vVisButton.isSelected()); //$NON-NLS-1$
			control.setValue("acceleration", toolbar.aVisButton.isSelected()); //$NON-NLS-1$
			control.setValue("labels", toolbar.labelsButton.isSelected()); //$NON-NLS-1$
			control.setValue("multiply_by_mass", toolbar.xMassButton.isSelected()); //$NON-NLS-1$
			control.setValue("trail_length", toolbar.trailLength); //$NON-NLS-1$
			control.setValue("stretch", toolbar.vStretch); //$NON-NLS-1$
			control.setValue("stretch_acceleration", toolbar.aStretch); //$NON-NLS-1$
		}

		/**
		 * Creates an object.
		 *
		 * @param control the control
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return null;
		}

		/**
		 * Loads an object with data from an XMLControl.
		 *
		 * @param control the control
		 * @param obj     the object
		 * @return the loaded object
		 */
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			TToolBar toolbar = (TToolBar) obj;
			toolbar.traceVisButton.setSelected(control.getBoolean("trace")); //$NON-NLS-1$
			toolbar.pVisButton.setSelected(control.getBoolean("position")); //$NON-NLS-1$
			toolbar.vVisButton.setSelected(control.getBoolean("velocity")); //$NON-NLS-1$
			toolbar.aVisButton.setSelected(control.getBoolean("acceleration")); //$NON-NLS-1$
			toolbar.labelsButton.setSelected(control.getBoolean("labels")); //$NON-NLS-1$
			toolbar.xMassButton.setSelected(control.getBoolean("multiply_by_mass")); //$NON-NLS-1$
			toolbar.trailLength = control.getInt("trail_length"); //$NON-NLS-1$
			toolbar.vStretch = control.getInt("stretch"); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("stretch_acceleration")) { //$NON-NLS-1$
				toolbar.aStretch = control.getInt("stretch_acceleration"); //$NON-NLS-1$
			} else
				toolbar.aStretch = toolbar.vStretch;
			return obj;
		}
	}

	/**
	 * A button to manage the creation and visibility of calibration tools.
	 */
	protected class CalibrationButton extends TButton implements ActionListener {

		boolean showPopup;
		JPopupMenu popup = new JPopupMenu();

		/**
		 * Constructor.
		 */
		private CalibrationButton() {
			setIcons(calibrationToolsOffIcon, calibrationToolsOnIcon);
			setRolloverIcon(calibrationToolsOffRolloverIcon);
			setRolloverSelectedIcon(calibrationToolsOnRolloverIcon);
			// mouse listener to distinguish between popup and tool visibility actions
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					int w = calibrationToolsOffRolloverIcon.getIconWidth();
					int dw = calibrationButton.getWidth() - w;
					// show popup if right side of button clicked or if no tools selected
					showPopup = e.getX() > (18 + dw / 2) || trackerPanel.visibleTools.isEmpty();
				}
			});
			addActionListener(this);
		}

		/**
		 * Overrides TButton method.
		 *
		 * @return the popup, or null if the right side of this button was clicked
		 */
		@Override
		protected JPopupMenu getPopup() {
			if (!showPopup)
				return null;
			// rebuild popup menu
			popup.removeAll();
			JMenuItem item;
			for (TTrack track : trackerPanel.calibrationTools) {
				item = new JCheckBoxMenuItem(track.getName());
				item.setSelected(trackerPanel.visibleTools.contains(track));
				item.setActionCommand(track.getName());
				item.addActionListener(this);
				popup.add(item);
			}
			// new tools menu
			JMenu newToolsMenu = getCalibrationToolsMenu();
			if (newToolsMenu.getItemCount() > 0) {
				if (!trackerPanel.calibrationTools.isEmpty())
					popup.addSeparator();
				popup.add(newToolsMenu);
			}
			FontSizer.setFonts(popup);
			return popup;
		}

		protected JMenu getCalibrationToolsMenu() {
			// new tools menu
			JMenu newToolsMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewTrack")); //$NON-NLS-1$
			JMenuItem item;
			if (trackerPanel.isEnabled("calibration.stick")) { //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("Stick.Name")); //$NON-NLS-1$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TapeMeasure track = new TapeMeasure();
						track.setColor(Color.BLUE);
						track.setStickMode(true);
						double scale = trackerPanel.getCoords().getScaleX(0);
						track.setCalibrator(scale == 1.0? 1.0: null);
						// assign a default name
						String name = TrackerRes.getString("CalibrationStick.New.Name"); //$NON-NLS-1$
						int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
						String letter = TrackerPanel.alphabet.substring(i, i + 1);
						track.setName(name + " " + letter); //$NON-NLS-1$
						trackerPanel.addTrack(track);
						calibrationButton.setSelected(true);

						// show all tools in visibleTools list
						for (TTrack next : trackerPanel.visibleTools) {
							showCalibrationTool(next);
						}

						// mark immediately if preferred
						if (Tracker.centerCalibrationStick) {
							// place at center of viewport
							MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
							Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
							int xpix = rect.x + rect.width / 2;
							int ypix = rect.y + rect.height / 2;
							double x = trackerPanel.pixToX(xpix);
							double y = trackerPanel.pixToY(ypix);
							track.createStep(0, x - 100, y - 20, x + 100, y - 20); // length 200 image units
						}

						trackerPanel.setSelectedTrack(track);
					}
				});
				newToolsMenu.add(item);
			}

			if (trackerPanel.isEnabled("calibration.tape")) { //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("CalibrationTapeMeasure.Name")); //$NON-NLS-1$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TapeMeasure track = new TapeMeasure();
						track.setColor(Color.BLUE);
						track.setReadOnly(false);
						track.setCalibrator(null);
						// assign a default name
						String name = TrackerRes.getString("CalibrationTapeMeasure.New.Name"); //$NON-NLS-1$
						int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
						String letter = TrackerPanel.alphabet.substring(i, i + 1);
						track.setName(name + " " + letter); //$NON-NLS-1$
						trackerPanel.addTrack(track);
						calibrationButton.setSelected(true);

						// show all tools in visibleTools list
						for (TTrack next : trackerPanel.visibleTools) {
							showCalibrationTool(next);
						}

						// mark immediately if preferred
						if (Tracker.centerCalibrationStick) {
							// place at center of viewport
							MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
							Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
							int xpix = rect.x + rect.width / 2;
							int ypix = rect.y + rect.height / 2;
							double x = trackerPanel.pixToX(xpix);
							double y = trackerPanel.pixToY(ypix);
							track.createStep(0, x - 100, y + 20, x + 100, y + 20); // length 200 image units
						}

						trackerPanel.setSelectedTrack(track);
					}
				});
				newToolsMenu.add(item);
			}

			if (trackerPanel.isEnabled("calibration.points")) { //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("Calibration.Name")); //$NON-NLS-1$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Calibration track = new Calibration();
						// assign a default name
						String name = TrackerRes.getString("Calibration.New.Name"); //$NON-NLS-1$
						int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
						String letter = TrackerPanel.alphabet.substring(i, i + 1);
						track.setName(name + " " + letter); //$NON-NLS-1$

						trackerPanel.addTrack(track);
						calibrationButton.setSelected(true);
						// show all tools in visibleTools list
						for (TTrack next : trackerPanel.visibleTools) {
							showCalibrationTool(next);
						}
						trackerPanel.setSelectedTrack(track);
						trackerPanel.getAxes().setVisible(true);
					}
				});
				newToolsMenu.add(item);
			}

			if (trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("OffsetOrigin.Name")); //$NON-NLS-1$
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						OffsetOrigin track = new OffsetOrigin();
						// assign a default name
						String name = TrackerRes.getString("OffsetOrigin.New.Name"); //$NON-NLS-1$
						int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
						String letter = TrackerPanel.alphabet.substring(i, i + 1);
						track.setName(name + " " + letter); //$NON-NLS-1$

						trackerPanel.addTrack(track);
						calibrationButton.setSelected(true);
						// show all tools in visibleTools list
						for (TTrack next : trackerPanel.visibleTools) {
							showCalibrationTool(next);
						}
						trackerPanel.setSelectedTrack(track);
						trackerPanel.getAxes().setVisible(true);
					}
				});
				newToolsMenu.add(item);
			}
			return newToolsMenu;
		}

		/**
		 * Responds to action events from both this button and the popup items.
		 *
		 * @param e the action event
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == calibrationButton) { // button action: show/hide tools
				if (showPopup)
					return;
				trackerPanel.setSelectedPoint(null);
				trackerPanel.selectedSteps.clear();
				trackerPanel.hideMouseBox();
				if (!calibrationButton.isSelected()) {
					calibrationButton.setSelected(true);
					// show tools in visibleTools list
					for (TTrack track : trackerPanel.visibleTools) {
						showCalibrationTool(track);
					}
				} else {
					calibrationButton.setSelected(false);
					// hide all tools
					for (TTrack track : trackerPanel.calibrationTools) {
						hideCalibrationTool(track);
					}
				}
				TFrame.repaintT(trackerPanel);
			} else { // menuItem action
						// see which item changed and show/hide corresponding tool
				trackerPanel.setSelectedPoint(null);
				trackerPanel.selectedSteps.clear();
				JMenuItem source = (JMenuItem) e.getSource();
				for (TTrack track : trackerPanel.calibrationTools) {
					if (e.getActionCommand().equals(track.getName())) {
						if (source.isSelected()) {
							trackerPanel.visibleTools.add(track);
							calibrationButton.setSelected(true);
							// show only tools in visibleTools
							for (TTrack next : trackerPanel.visibleTools) {
								showCalibrationTool(next);
							}
						} else {
							hideCalibrationTool(track);
							trackerPanel.visibleTools.remove(track);
							boolean toolsVisible = false;
							for (TTrack next : trackerPanel.visibleTools) {
								toolsVisible = toolsVisible || next.isVisible();
							}
							calibrationButton.setSelected(toolsVisible);
						}
					}
				}
				refresh();
			}
		}

		/**
		 * Shows a calibration tool.
		 *
		 * @param track a calibration tool
		 */
		void showCalibrationTool(TTrack track) {
			track.erase();
			track.setVisible(true);
			if (track instanceof Calibration) {
				int n = trackerPanel.getFrameNumber();
				Step step = track.getStep(n);
				if (step == null || step.getPoints()[1] == null) {
					trackerPanel.setSelectedTrack(track);
				}
			} else if (track instanceof OffsetOrigin) {
				int n = trackerPanel.getFrameNumber();
				Step step = track.getStep(n);
				if (step == null) {
					trackerPanel.setSelectedTrack(track);
				}
			}
		}

		/**
		 * Hides a calibration tool.
		 *
		 * @param track a calibration tool
		 */
		void hideCalibrationTool(TTrack track) {
			track.setVisible(false);
			if (trackerPanel.getSelectedTrack() == track) {
				trackerPanel.setSelectedTrack(null);
			}
		}

		/**
		 * Refreshes this button.
		 */
		void refresh() {
			setToolTipText(TrackerRes.getString("TToolbar.Button.TapeVisible.Tooltip")); //$NON-NLS-1$
			// add PROPERTY_TTRACK_VISIBLE property change listeners to calibration tools
			for (TTrack track : trackerPanel.calibrationTools) {
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, TToolBar.this); // $NON-NLS-1$
				track.addPropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, TToolBar.this); // $NON-NLS-1$
			}
			// check visibility of tools and state of menu items
			boolean toolsVisible = false;
			for (TTrack track : trackerPanel.calibrationTools) {
				toolsVisible = toolsVisible || track.isVisible();
			}
			if (notYetCalibrated && toolsVisible) {
				notYetCalibrated = false;
				setSelected(true);
			}
		}

	} // end calibration button

	/**
	 * A button to manage the visibility of the pencil scenes and control dialog
	 */
	protected class DrawingButton extends TButton implements ActionListener {

		boolean showPopup;
		JPopupMenu popup;
		JMenuItem drawingVisibleCheckbox;

		/**
		 * Constructor.
		 */
		private DrawingButton() {
			setIcons(pencilOffIcon, pencilOnIcon);
			setRolloverIcon(pencilOffRolloverIcon);
			setRolloverSelectedIcon(pencilOnRolloverIcon);
			addActionListener(this);

			// mouse listener to distinguish between popup and tool visibility actions
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					int w = getIcon().getIconWidth();
					int dw = getWidth() - w;
					// show popup if right side of button clicked
					showPopup = e.getX() > (w * 18 / 28 + dw / 2);
				}
			});

			drawingVisibleCheckbox = new JMenuItem();
			drawingVisibleCheckbox.setSelected(true);
			drawingVisibleCheckbox.setDisabledIcon(checkboxOnDisabledIcon);
			drawingVisibleCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					drawingVisibleCheckbox.setSelected(!drawingVisibleCheckbox.isSelected());
					trackerPanel.setSelectedPoint(null);
					trackerPanel.selectedSteps.clear();
					PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
					drawer.setDrawingsVisible(drawingVisibleCheckbox.isSelected());
					TFrame.repaintT(trackerPanel);
				}
			});
			popup = new JPopupMenu();
			popup.add(drawingVisibleCheckbox);
		}

		@Override
		protected JPopupMenu getPopup() {
			if (!showPopup)
				return null;
			refresh();
			FontSizer.setFonts(popup);
			checkboxOnDisabledIcon.resize(FontSizer.getIntegerFactor());
			return popup;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (showPopup)
				return;
			trackerPanel.setSelectedPoint(null);
			trackerPanel.selectedSteps.clear();
			trackerPanel.hideMouseBox();
			setSelected(!isSelected());
			PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
			drawer.getDrawingControl().setVisible(isSelected());
			if (isSelected()) {
				if (drawer.scenes.isEmpty()) {
					drawer.addNewScene();
				} else {
					PencilScene scene = drawer.getSceneAtFrame(trackerPanel.getFrameNumber());
					drawer.getDrawingControl().setSelectedScene(scene);
				}
				drawer.setDrawingsVisible(true);
			}
		}

		/**
		 * Refreshes this button.
		 */
		void refresh() {
			setToolTipText(TrackerRes.getString("TToolBar.Button.Drawings.Tooltip")); //$NON-NLS-1$
			drawingVisibleCheckbox.setText(TrackerRes.getString("TToolBar.MenuItem.DrawingsVisible.Text")); //$NON-NLS-1$
			PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
			drawingVisibleCheckbox.setSelected(drawer.areDrawingsVisible());
			drawingVisibleCheckbox.setIcon(drawer.areDrawingsVisible() ? checkboxOnIcon : checkboxOffIcon);
			drawingVisibleCheckbox.setEnabled(!PencilDrawer.isDrawing(trackerPanel));
		}

	}

}
