/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
import java.awt.Font;
import java.awt.Graphics;
//import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import org.opensourcephysics.display.OSPRuntime.Disposable;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.ClipInspector;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FontSizer;

/**
 * This is the main toolbar for Tracker.
 *
 * @author Douglas Brown
 */
public class TToolBar extends JToolBar implements Disposable, PropertyChangeListener {

	// static fields
	final protected static int[] trailLengths = { 1, 4, 15, 0 };
	final protected static String[] trailLengthNames = { "none", "short", "long", "full" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	final protected static Icon newTrackIcon, pointmassOffIcon, pointmassOnIcon;
	final protected static Icon trackControlIcon, trackControlOnIcon, trackControlDisabledIcon;
	final protected static Icon zoomIcon;
	final protected static Icon clipOffIcon, clipOnIcon;
	final protected static Icon axesOffIcon, axesOnIcon;
	final protected static Icon calibrationToolsOffIcon, calibrationToolsOnIcon;
	final protected static Icon calibrationToolsOffRolloverIcon, calibrationToolsOnRolloverIcon;
	final protected static Icon eyeIcon, rulerIcon, rulerOnIcon;
	final protected static Icon rulerRolloverIcon, rulerOnRolloverIcon;
	final protected static Icon pointsOffIcon, pointsOnIcon;
	final protected static Icon velocOffIcon, velocOnIcon;
	final protected static Icon accelOffIcon, accelOnIcon;
	final protected static Icon traceOffIcon, traceOnIcon;
	final protected static Icon labelsOffIcon, labelsOnIcon;
	final protected static Icon stretchOffIcon, stretchOnIcon;
	final protected static Icon xmassOffIcon, xmassOnIcon;
	final protected static Icon fontSizeIcon;
	final protected static Icon memoryIcon, redMemoryIcon;
	final protected static Icon autotrackerOffIcon, autotrackerOnIcon, autotrackerDisabledIcon;
	final protected static Icon infoIcon, refreshIcon, htmlIcon, htmlDisabledIcon;
	final protected static Icon[] trailIcons = new Icon[4];
	final protected static int[] stretchValues = new int[] { 1, 2, 3, 4, 6, 8, 12, 16, 24, 32 };
	final protected static Icon separatorIcon;
	final protected static Icon checkboxOffIcon, checkboxOnIcon;
	final protected static Icon checkboxOnDisabledIcon;
	final protected static Icon pencilOffIcon, pencilOnIcon, pencilOffRolloverIcon, pencilOnRolloverIcon;
	final protected static NumberFormat zoomFormat = NumberFormat.getNumberInstance();

	public static final String REFRESH_PAGETVIEW_TABS = "PageTView.tabs";
	public static final String REFRESH_PAGETVIEW_TITLE = "PageTView.title";
	public static final String REFRESH_PAGETVIEW_URL = "PageTView.url";
	public static final String REFRESH_LINEPROFILE = "LineProfile";
	public static final String REFRESH_TFRAME_LOCALE = "TFrame.locale";
	public static final String REFRESH_TFRAME_LOCALE2 = "TFrame.locale2 ??";
	protected static final String REFRESH__CLIP_SETTINGS_HIDDEN = "clip settings hidden";
	protected static final String REFRESH__CLIP_SETTINGS_SHOWN = "clip settings shown";
	private static final String REFRESH__PROPERTY_VIDEO = "property video";
	private static final String REFRESH__PROPERTY_SELECTED_TRACK = "property selected track";
	protected static final String REFRESH__NEW_VERSION = "new version";

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
	//protected TrackerPanel trackerPanel; // manages & displays track data

	final protected WindowListener infoListener;
	final protected JButton openButton, openBrowserButton, saveButton, saveZipButton;
	final protected TButton newTrackButton;
	final protected JButton trackControlButton, clipSettingsButton;
	final protected CalibrationButton calibrationButton;
	final protected RulerButton rulerButton;
	final protected DrawingButton drawingButton;
	final protected JButton axesButton, zoomButton, autotrackerButton;
	final protected TButton eyeButton;
	final protected TButton traceVisButton, pVisButton, vVisButton, aVisButton;
	final protected TButton xMassButton, trailButton, labelsButton, stretchButton;
	protected JMenuItem pathVisMenuItem, pVisMenuItem, vVisMenuItem, aVisMenuItem;
	protected JMenuItem xMassMenuItem, labelsMenuItem;
	protected JMenu trailsMenu, stretchMenu;
	final protected JButton fontSizeButton;
	final protected JPopupMenu newPopup = new JPopupMenu();
	final protected JPopupMenu selectPopup = new JPopupMenu();
	final protected JPopupMenu eyePopup = new JPopupMenu();
	final protected JMenu vStretchMenu, aStretchMenu;
	protected ButtonGroup vGroup, aGroup;
	final protected JMenuItem showTrackControlItem, selectNoneItem, stretchOffItem;
	final protected JButton notesButton, refreshButton, desktopButton, memoryButton;
	final protected Component toolbarFiller;
	final protected JMenu cloneMenu;
	final protected ArrayList<PageTView.TabData> pageViewTabs = new ArrayList<PageTView.TabData>();
	protected JPopupMenu zoomPopup;

	static {

		newTrackIcon = Tracker.getResourceIcon("poof.gif", true); //$NON-NLS-1$
		pointmassOffIcon = Tracker.getResourceIcon("track_off.gif", true); //$NON-NLS-1$
		pointmassOnIcon = Tracker.getResourceIcon("track_on.gif", true); //$NON-NLS-1$
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
		eyeIcon = Tracker.getResourceIcon("eye.gif", true); //$NON-NLS-1$
		rulerIcon = Tracker.getResourceIcon("ruler.gif", true); //$NON-NLS-1$
		rulerOnIcon = Tracker.getResourceIcon("ruler_on.gif", true); //$NON-NLS-1$
		rulerRolloverIcon = Tracker.getResourceIcon("ruler_rollover.gif", true); //$NON-NLS-1$
		rulerOnRolloverIcon = Tracker.getResourceIcon("ruler_on_rollover.gif", true); //$NON-NLS-1$
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
		fontSizeIcon = Tracker.getResourceIcon("font_size.gif", true); //$NON-NLS-1$
		autotrackerOffIcon = Tracker.getResourceIcon("autotrack_off.gif", true); //$NON-NLS-1$
		autotrackerOnIcon = Tracker.getResourceIcon("autotrack_on.gif", true); //$NON-NLS-1$
		autotrackerDisabledIcon = Tracker.getResourceIcon("autotrack_disabled.gif", true); //$NON-NLS-1$
		infoIcon = Tracker.getResourceIcon("info.gif", true); //$NON-NLS-1$
		refreshIcon = Tracker.getResourceIcon("refresh.gif", true); //$NON-NLS-1$
		memoryIcon = Tracker.getResourceIcon("memory.gif", true); //$NON-NLS-1$
		redMemoryIcon = Tracker.getResourceIcon("memory_red.gif", true); //$NON-NLS-1$
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
	protected boolean useEyeButton = true;
	protected int vStretch = 1, aStretch = 1;
	protected int trailLengthIndex = Tracker.preferredTrailLengthIndex;
	protected boolean notYetCalibrated = true;
	protected int toolbarComponentHeight;
	private AbstractAction zoomAction;
	private TFrame frame;
	private Integer panelID;

	private static final String[] panelProps = new String[] { 
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT,
			TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK, 
			TrackerPanel.PROPERTY_TRACKERPANEL_TRACK,
			TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, 
			TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO,
			TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION,
	};


	/**
	 * TToolBar constructor.
	 *
	 * @param panel the tracker panel
	 */
	TToolBar(TrackerPanel panel) {
		this.frame = panel.frame;
		this.panelID = panel.getID();
		System.out.println("Creating toolbar for " + panel);
		panel.addListeners(panelProps, this);
		setFloatable(false);
		// create buttons
		Map<String, AbstractAction> actions = panel.getActions();
		// open and save buttons
		openButton = new TButton(actions.get("open")); //$NON-NLS-1$
		openBrowserButton = new TButton(actions.get("openBrowser")); //$NON-NLS-1$
		saveButton = new TButton(actions.get("save")); //$NON-NLS-1$
		saveButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				String fileName = panel().getTitle();
				String extension = XML.getExtension(fileName);
				if (extension == null || !extension.equals("trk")) //$NON-NLS-1$
					fileName = XML.stripExtension(fileName) + ".trk"; //$NON-NLS-1$
				saveButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Save.Tooltip")); //$NON-NLS-1$
			}
		});
		saveZipButton = new TButton(actions.get("saveZip")); //$NON-NLS-1$
		saveZipButton.addActionListener((e) -> {
				final ExportZipDialog zipDialog = ExportZipDialog.getDialog(panel());
				final boolean isVis = zipDialog.isVisible();
				Runnable runner = new Runnable() {

					@Override
					public void run() {
						zipDialog.setVisible(!isVis);
					}
				};
				SwingUtilities.invokeLater(runner);
		});
		// clip settings button
		clipSettingsButton = new TButton(clipOffIcon, clipOnIcon);
		clipSettingsButton.addActionListener((e) -> {
				panel().setClipSettingsVisible(null); // toggles visibility
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
				Rectangle rect = panel().scrollPane.getViewport().getViewRect();
				MainTView mainView = frame.getMainView(panel());
				mainView.setZoomCenter(rect.x + rect.width / 2, rect.y + rect.height / 2);
				String name = e.getActionCommand();
				if (name.equals("auto")) { //$NON-NLS-1$
					panel().setMagnification(-1);
				} else {
					double mag = Double.parseDouble(name);
					panel().setMagnification(mag / 100);
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
					panel().setMagnification(-1);
					if (zoomPopup != null)
						zoomPopup.setVisible(false);
					refreshZoomButton();
				}
			}
		});

		// new track button
		newTrackButton = new TButton(pointmassOffIcon) {

			@Override
			protected JPopupMenu getPopup() {
				TMenuBar.refreshPopup(panel(), TMenuBar.POPUPMENU_TTOOLBAR_TRACKS, newPopup);
				return newPopup;
			}
		};
		// track control button
		trackControlButton = new TButton(pointmassOffIcon, pointmassOnIcon);
//		trackControlButton = new TButton(newTrackIcon, newTrackIcon);
		trackControlButton.addActionListener((e) -> {
				TrackControl tc = TrackControl.getControl(panel());
				boolean vis = !tc.isVisible();
				if (!tc.positioned) {
					Point p = trackControlButton.getLocationOnScreen();
					tc.setLocation(p.x, p.y + trackControlButton.getHeight());
					tc.positioned = true;
				}
				tc.setVisible(vis);
				
//				if (vis && trackerPanel().getUserTracks().isEmpty()) {
//					Timer timer = new Timer(200, (ev) -> {
//						tc.newTrackButton.showPopup();
//					});
//					timer.setRepeats(false);
//					timer.start();
//				}
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
		autotrackerButton.addActionListener((e) -> {
				autotrackerButton.setSelected(!autotrackerButton.isSelected());
				AutoTracker autoTracker = panel().getAutoTracker(true);
//				if (autoTracker.getTrack() == null) {
//					TTrack track = trackerPanel().getSelectedTrack();
//					if (track == null) {
//						for (TTrack next : trackerPanel().getTracks()) {
//							if (!next.isAutoTrackable())
//								continue;
//							track = next;
//							break;
//						}
//					}
//					autoTracker.setTrack(track);
//				}
				if (autotrackerButton.isSelected()) {
					autoTracker.getWizard().setFontLevel(FontSizer.getLevel());
				}
				autoTracker.getWizard().setVisible(autotrackerButton.isSelected());
				TFrame.repaintT(panel());
		});
		final Action refreshAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JButton button = (JButton) e.getSource();
				button.setSelected(!button.isSelected());
				if (useEyeButton)
					refreshTracks(); // no need to refresh toolbar, only tracks
				else
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
						trailLengthIndex = Integer.parseInt(e.getActionCommand());
						trailButton.setSelected(trailLengthIndex != 0);
						refresh(REFRESH__TRAIL_BUTTON_ACTION_TRUE);
						TFrame.repaintT(panel());
					}
				};
				ButtonGroup group = new ButtonGroup();
				JMenuItem item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.NoTrail")); //$NON-NLS-1$
				item.setSelected(trailLengthIndex == 0);
				item.setActionCommand(String.valueOf(0));
				item.addActionListener(listener);
				popup.add(item);
				group.add(item);
				item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.ShortTrail")); //$NON-NLS-1$
				item.setSelected(trailLengthIndex == 1);
				item.setActionCommand(String.valueOf(1));
				item.addActionListener(listener);
				popup.add(item);
				group.add(item);
				item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.LongTrail")); //$NON-NLS-1$
				item.setSelected(trailLengthIndex == 2);
				item.setActionCommand(String.valueOf(2));
				item.addActionListener(listener);
				popup.add(item);
				group.add(item);
				item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.FullTrail")); //$NON-NLS-1$
				item.setSelected(trailLengthIndex == 3);
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
		xMassButton.addActionListener((e) -> {
				refreshAction.actionPerformed(e);
				TTrack track = panel().getSelectedTrack();
				if (track instanceof PointMass) {
					panel().refreshTrackBar();
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
					item.addActionListener((ae) -> {

						{
							int n = Integer.parseInt(ae.getActionCommand());
							panel().setSelectedPoint(null);
							panel().selectedSteps.clear();
							vStretch = n;
							refresh(REFRESH__VSTRETCH_ACTION_TRUE);
						}
					});
					vGroup.add(item);
					vStretchMenu.add(item);
					FontSizer.setMenuFonts(vStretchMenu);
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
					item.addActionListener((ae) -> {

						{
							int n = Integer.parseInt(ae.getActionCommand());
							panel().setSelectedPoint(null);
							panel().selectedSteps.clear();
							aStretch = n;
							refresh(REFRESH__ASTRETCH_ACTION_TRUE);
						}
					});
					aGroup.add(item);
					aStretchMenu.add(item);
					FontSizer.setMenuFonts(aStretchMenu);
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
		stretchOffItem.addActionListener((e) -> {
				vStretch = 1;
				aStretch = 1;
				refresh(REFRESH__STRETCHOFF_ACTION_TRUE);
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

		// eye button
		eyeButton = new TButton(eyeIcon) {

			@Override
			protected JPopupMenu getPopup() {
				return refreshEyePopup();
			}
		};
		// ruler button
		rulerButton = new RulerButton();
		// font size button
		fontSizeButton = new TButton(fontSizeIcon) {
			@Override
			protected JPopupMenu getPopup() {
				JPopupMenu popup = new JPopupMenu();
				for (int i = 0; i <= Tracker.maxFontLevel; i++) {
					String s = TrackerRes.getString("TMenuBar.MenuItem.Font");
					ResizableIcon icon = (ResizableIcon) Tracker.getResourceIcon("zoom.gif", true); //$NON-NLS-1$
					icon.setFixedSizeFactor(FontSizer.getIntegerFactor(i));
					JMenuItem item = new JMenuItem(s, icon);
					FontSizer.setFonts(item, i);
					int n = i;
					item.addActionListener((e) -> {
						FontSizer.setLevel(n);
					});
					popup.add(item);
					if (i == FontSizer.getLevel()) {
						item.setForeground(Color.green.darker());
					}
				}
				return popup;
			}

		};

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
		notesButton.addActionListener((e) -> {
				doNotesAction();
		});
		
		/**
		 * Java only; transpiler may ignore
		 * 
		 * @j2sNative
		 * 
		 */
		{
			memoryButton = new TButton(memoryIcon) {
				@Override
				public JPopupMenu getPopup() {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem memoryItem = new JMenuItem(TrackerRes.getString("TTrackBar.Memory.Menu.SetSize")); //$NON-NLS-1$
					popup.add(memoryItem);
					memoryItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							Tracker.askToSetMemory((TFrame) memoryButton.getTopLevelAncestor());
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
		}

		
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
		showTrackControlItem.addActionListener((e) -> {
				TrackControl tc = TrackControl.getControl(panel());
				tc.setVisible(showTrackControlItem.isSelected());
		});
		selectNoneItem = new JMenuItem();
		selectNoneItem.addActionListener((e) -> {
				panel().setSelectedTrack(null);
		});
		frame.clearHoldPainting();
		refresh(REFRESH__CREATE_GUI_TRUE);
		validate();
	}

	protected JPopupMenu createZoomPopup() {
		zoomPopup = new JPopupMenu();
		JMenuItem item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ToFit")); //$NON-NLS-1$
		item.setActionCommand("auto"); //$NON-NLS-1$
		item.addActionListener(zoomAction);
		zoomPopup.add(item);
		zoomPopup.addSeparator();
		for (int i = 0, nz = TrackerPanel.ZOOM_LEVELS.length; i < nz; i++) {
			int n = (int) (100 * TrackerPanel.ZOOM_LEVELS[i]);
			String m = String.valueOf(n);
			item = new JMenuItem(m + "%"); //$NON-NLS-1$
			item.setActionCommand(m);
			item.addActionListener(zoomAction);
			zoomPopup.add(item);
		}
		FontSizer.setFonts(zoomPopup, FontSizer.getLevel());
		return zoomPopup;
	}

	protected JPopupMenu getDesktopBtnPopup() {
		TrackerPanel panel = panel();
		JPopupMenu popup = new JPopupMenu();
		if (!panel.supplementalFilePaths.isEmpty()) {
			JMenu fileMenu = new JMenu(TrackerRes.getString("TToolbar.Button.Desktop.Menu.OpenFile")); //$NON-NLS-1$
			popup.add(fileMenu);
			for (String next : panel.supplementalFilePaths) {
				String title = XML.getName(next);
				String path = next;
				JMenuItem item = new JMenuItem(title);
				item.setActionCommand(path);
				item.setToolTipText(path);
				item.addActionListener((e) -> {
						OSPDesktop.displayURL(e.getActionCommand());
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
				String path = panel.pageViewFilePaths.get(next.text);
				if (path == null) {
					path = next.url.toExternalForm();
				}
				JMenuItem item = new JMenuItem(title);
				item.setActionCommand(path);
				item.setToolTipText(path);
				item.addActionListener((e) -> {
						OSPDesktop.displayURL(e.getActionCommand());
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
		item.addActionListener((e) -> {
			doRefreshPopup();
		});
		popup.add(item);
		popup.addSeparator();
		item = new JCheckBoxMenuItem(TrackerRes.getString("TToolbar.Button.Refresh.Popup.AutoRefresh")); //$NON-NLS-1$
		item.setSelected(panel().isAutoRefresh());
		item.addActionListener((e) -> {
			TrackerPanel panel = panel();
				panel.setAutoRefresh(((JCheckBoxMenuItem) e.getSource()).isSelected());
				if (panel.isAutoRefresh()) {
					panel.refreshTrackData(DataTable.MODE_REFRESH);
					panel.eraseAll();
					panel.repaintDirtyRegion();
				}
		});
		popup.add(item);
		FontSizer.setFonts(popup, FontSizer.getLevel());
		return popup;
	}

	protected void doRefreshPopup() {
		TrackerPanel panel = panel();
		// offer to clear RGBRegion data that are valid and visible in a view
		ArrayList<RGBRegion> regions = panel.getDrawablesTemp(RGBRegion.class);
		ArrayList<RGBRegion> regionsToClear = new ArrayList<RGBRegion>();
		if (!regions.isEmpty()) {
			for (RGBRegion next : regions) {
				if (panel.isTrackViewDisplayed(next) && next.dataValid) {
					regionsToClear.add(next);
				}
			}
		}
		regions.clear();
		if (!regionsToClear.isEmpty()) {
			// get user confirmation
			String list = " "; //$NON-NLS-1$
			for (RGBRegion next : regionsToClear) {
				list += next.getName() + ", "; //$NON-NLS-1$
			}
			list = list.substring(0, list.length() - 2);
			int i = JOptionPane.showConfirmDialog(panel.getTopLevelAncestor(),
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
		panel.refreshTrackData(DataTable.MODE_REFRESH);
		panel.eraseAll();
		panel.repaintDirtyRegion();
	}

	protected void doNotesAction() {
		if (frame != null && frame.getSelectedPanel() == panel()) {
			frame.setNotesDialog(panel(), infoListener);
			notesButton.setSelected(frame.notesVisible());
		}
	}

	protected void refreshZoomButton() {
		double zoom = panel().getMagnification() * 100;
		zoomButton.setText(zoomFormat.format(zoom) + "%"); //$NON-NLS-1$
	}
	
	/**
	 * Refreshes the memory button for a trackerPanel().
	 */
	protected static void refreshMemoryButton(TrackerPanel trackerPanel) {
		if (OSPRuntime.isJS)
			return;
		Integer panelID = trackerPanel.getID();
		TFrame frame = trackerPanel.getTFrame();
		System.gc();
		SwingUtilities.invokeLater(() -> {
			TrackerPanel panel = frame.getTrackerPanelForID(panelID);
			if (panel != null && panel.hasToolBar())
				panel.getToolBar(true).refreshMemoryButton();
		});
	}
	
	/**
	 * Refreshes the memory button. Java only, not JavaScript
	 */
	private void refreshMemoryButton() {
		System.gc();
		long[] memory = OSPRuntime.getMemory();
		if (OSPRuntime.outOfMemory && TTrackBar.showOutOfMemoryDialog) {
			OSPRuntime.outOfMemory = false;
			TTrackBar.showOutOfMemoryDialog = false;
			memory[0] = memory[1];
			JOptionPane.showMessageDialog(memoryButton,
					TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message1") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
							+ TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message2"), //$NON-NLS-1$
					TrackerRes.getString("Tracker.Dialog.OutOfMemory.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
		}
		String mem = TrackerRes.getString("TTrackBar.Button.Memory") + " "; //$NON-NLS-1$ //$NON-NLS-2$
		String of = TrackerRes.getString("DynamicSystem.Parameter.Of") + " "; //$NON-NLS-1$ //$NON-NLS-2$
		memoryButton.setToolTipText(mem + memory[0] + "MB " + of + memory[1] + "MB"); //$NON-NLS-1$ //$NON-NLS-2$
//		memoryButton.setToolTipText(TrackerRes.getString("TTrackBar.Button.Memory.Tooltip")); //$NON-NLS-1$
		double used = ((double) memory[0]) / memory[1];
		memoryButton.setIcon(used > 0.8 ? redMemoryIcon : memoryIcon);
	}

	protected JPopupMenu refreshEyePopup() {
		if (pathVisMenuItem == null) {
			int gap = 6;
			
			pathVisMenuItem = new JCheckBoxMenuItem(traceOffIcon);
			pathVisMenuItem.addActionListener((e) -> {
				traceVisButton.setSelected(!traceVisButton.isSelected());
				refresh(TToolBar.REFRESH__REFRESH_ACTION_TRUE);
			});
			pathVisMenuItem.setIconTextGap(gap);
			eyePopup.add(pathVisMenuItem);
			
			pVisMenuItem = new JCheckBoxMenuItem(pointsOffIcon);
			pVisMenuItem.addActionListener((e) -> {
				pVisButton.setSelected(!pVisButton.isSelected());
				refresh(TToolBar.REFRESH__REFRESH_ACTION_TRUE);
			});
			pVisMenuItem.setIconTextGap(gap);
			eyePopup.add(pVisMenuItem);
			
			vVisMenuItem = new JCheckBoxMenuItem(velocOffIcon);
			vVisMenuItem.addActionListener((e) -> {
				vVisButton.setSelected(!vVisButton.isSelected());
				refresh(TToolBar.REFRESH__REFRESH_ACTION_TRUE);
			});
			vVisMenuItem.setIconTextGap(gap);
			eyePopup.add(vVisMenuItem);
			
			aVisMenuItem = new JCheckBoxMenuItem(accelOffIcon);
			aVisMenuItem.addActionListener((e) -> {
				aVisButton.setSelected(!aVisButton.isSelected());
				refresh(TToolBar.REFRESH__REFRESH_ACTION_TRUE);
			});
			aVisMenuItem.setIconTextGap(gap);
			eyePopup.add(aVisMenuItem);

			eyePopup.addSeparator();

			trailsMenu = new JMenu();
			trailsMenu.setIconTextGap(gap);
			JPopupMenu trailPopup = trailButton.getPopup();
			int n = trailPopup.getComponentCount();
			for (int i = 0; i < n; i++) {
				trailsMenu.add(trailPopup.getComponent(0));				
			}
			eyePopup.add(trailsMenu);
			
			labelsMenuItem = new JCheckBoxMenuItem(labelsOffIcon);
			labelsMenuItem.addActionListener((e) -> {
				labelsButton.setSelected(!labelsButton.isSelected());
				refresh(TToolBar.REFRESH__REFRESH_ACTION_TRUE);
			});
			labelsMenuItem.setIconTextGap(gap);
			eyePopup.add(labelsMenuItem);
			
			eyePopup.addSeparator();
			
			stretchMenu = new JMenu();
			stretchMenu.setIcon(stretchOffIcon);
			stretchMenu.setIconTextGap(gap);
			eyePopup.add(stretchMenu);
			
			xMassMenuItem = new JCheckBoxMenuItem(xmassOffIcon);
			xMassMenuItem.addActionListener((e) -> {
				xMassButton.setSelected(!xMassButton.isSelected());
				refresh(TToolBar.REFRESH__REFRESH_ACTION_TRUE);
			});
			xMassMenuItem.setIconTextGap(gap);
			eyePopup.add(xMassMenuItem);			
		}
		
		// refresh text strings
		pathVisMenuItem.setText(TrackerRes.getString("TToolBar.Menuitem.Paths.Text"));
		pVisMenuItem.setText(TrackerRes.getString("TToolBar.Menuitem.Positions.Text"));
		vVisMenuItem.setText(xMassButton.isSelected()?
				TrackerRes.getString("TToolBar.Menuitem.Veloc.Text.P"):
				TrackerRes.getString("TToolBar.Menuitem.Veloc.Text.V"));
		aVisMenuItem.setText(xMassButton.isSelected()?
				TrackerRes.getString("TToolBar.Menuitem.Accel.Text.F"):
				TrackerRes.getString("TToolBar.Menuitem.Accel.Text.A"));
		trailsMenu.setText(TrackerRes.getString("TToolBar.Menu.Trails.Text"));
		labelsMenuItem.setText(TrackerRes.getString("TToolBar.Menuitem.Labels.Text"));
		stretchMenu.setText(TrackerRes.getString("TToolBar.Menu.Stretch.Text"));
		xMassMenuItem.setText(TrackerRes.getString("TToolBar.Menuitem.Xmass.Text"));
		
		// refresh stretch and trails menus
		if (stretchMenu.getItemCount() != 4) {
			stretchMenu.removeAll();
			stretchMenu.add(vStretchMenu);
			stretchMenu.add(aStretchMenu);
			stretchMenu.addSeparator();
			stretchMenu.add(stretchOffItem);
		}
		trailsMenu.setIcon(trailIcons[trailLengthIndex]);
		
		// refresh selection state to match buttons
		pathVisMenuItem.setSelected(traceVisButton.isSelected());
		pVisMenuItem.setSelected(pVisButton.isSelected());
		vVisMenuItem.setSelected(vVisButton.isSelected());
		aVisMenuItem.setSelected(aVisButton.isSelected());
		labelsMenuItem.setSelected(labelsButton.isSelected());
		xMassMenuItem.setSelected(xMassButton.isSelected());
		
		FontSizer.setFonts(eyePopup);
		return eyePopup;
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
		if (disposed || frame.hasPaintHold() || !Tracker.allowToolbarRefresh)
			return;
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
				if (!disposed) {
					//System.out.println("TToolBar refreshAsync from " + whereFrom);
					refreshAsync(refreshTrackProperties);
				}
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
		
//		long t0 = Performance.now(0);
		//OSPLog.debug(Performance.timeCheckStr("TToolBar refreshAsync", Performance.TIME_MARK));
		refreshing = true; // signals listeners that items are being refreshed (not implemented)
		int enabledCount = panel().getEnabledCount();
		boolean trackerPanelTainted = (enabledCount != this.enabledCount);
		this.enabledCount = enabledCount;
		if (trackerPanelTainted && allowRebuild) {
			rebuild();
		}
		checkEnabled(refreshTrackProperties);
		refreshing = false;
		//OSPLog.debug("!!! " + Performance.now(t0) + " TToolBar refresh async");
	}

	private void rebuild() {
		// assemble buttons
		removeAll();
		
		//if (!OSPRuntime.isApplet) {
			if (panel().isEnabled("file.open")) { //$NON-NLS-1$
				add(openButton);
			}
			if (panel().isEnabled("file.save")) { //$NON-NLS-1$
				add(saveButton);
			}
			if (getComponentCount() > 0)
				add(getSeparator());
			if (panel().isEnabled("file.library")) { //$NON-NLS-1$
				add(openBrowserButton);
				if (panel().isEnabled("file.save")) { //$NON-NLS-1$
					add(saveZipButton);
				}
			}
			if (getComponentCount() > 0)
				add(getSeparator());
		//}
		boolean addSeparator = false;
		if (panel().isEnabled("button.clipSettings")) {//$NON-NLS-1$
			add(clipSettingsButton);
			addSeparator = true;
		}
		if (panel().isEnabled("calibration.stick") //$NON-NLS-1$
				|| panel().isEnabled("calibration.tape") //$NON-NLS-1$
				|| panel().isEnabled("calibration.points") //$NON-NLS-1$
				|| panel().isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
			add(calibrationButton);
			addSeparator = true;
		}
		if (panel().isEnabled("button.axes")) {//$NON-NLS-1$
			add(axesButton);
			addSeparator = true;
		}
		if (addSeparator)
			add(getSeparator());
		if (panel().isCreateTracksEnabled()) {
//			add(newTrackButton);
		}
		add(trackControlButton);
		if (panel().isEnabled("track.autotrack")) //$NON-NLS-1$
			add(autotrackerButton);
		add(getSeparator());
		if (useEyeButton) {
			add(eyeButton);
			add(rulerButton);
		}
		else {
			if (panel().isEnabled("button.trails") //$NON-NLS-1$
					|| panel().isEnabled("button.labels")) { //$NON-NLS-1$
				if (panel().isEnabled("button.trails")) //$NON-NLS-1$
					add(trailButton);
				if (panel().isEnabled("button.labels")) //$NON-NLS-1$
					add(labelsButton);
				add(getSeparator());
			}
			if (panel().isEnabled("button.path") //$NON-NLS-1$
					|| panel().isEnabled("button.x") //$NON-NLS-1$
					|| panel().isEnabled("button.v") //$NON-NLS-1$
					|| panel().isEnabled("button.a")) {//$NON-NLS-1$
				if (panel().isEnabled("button.path")) //$NON-NLS-1$
					add(traceVisButton);
				if (panel().isEnabled("button.x")) //$NON-NLS-1$
					add(pVisButton);
				if (panel().isEnabled("button.v")) //$NON-NLS-1$
					add(vVisButton);
				if (panel().isEnabled("button.a")) //$NON-NLS-1$
					add(aVisButton);
				add(getSeparator());
			}
			if (panel().isEnabled("button.stretch") //$NON-NLS-1$
					|| panel().isEnabled("button.xMass")) { //$NON-NLS-1$
				if (panel().isEnabled("button.stretch")) //$NON-NLS-1$
					add(stretchButton);
				if (panel().isEnabled("button.xMass")) //$NON-NLS-1$
					add(xMassButton);
				add(getSeparator());
			}
			add(rulerButton);
		}
		add(getSeparator());
		add(zoomButton);
		add(fontSizeButton);
		add(getSeparator());
		add(toolbarFiller);
		if (Tracker.newerVersion != null) {
			String s = TrackerRes.getString("TTrackBar.Button.Version"); //$NON-NLS-1$
			TTrackBar.newVersionButton.setText(s + " " + Tracker.newerVersion); //$NON-NLS-1$
			add(TTrackBar.newVersionButton);
		}
		if (panel().isEnabled("button.drawing")) //$NON-NLS-1$
			add(drawingButton);
		if (desktopButton.isEnabled())
			add(desktopButton);
		add(notesButton);
		if (!OSPRuntime.isJS) {
			add(getSeparator());
			add(memoryButton);
		}
		add(refreshButton);

		if (TTrackBar.testButton != null)
			add(TTrackBar.testButton);

//		FontSizer.setFont(newTrackButton);
//		FontSizer.setFont(zoomButton);
		//OSPLog.debug(Performance.timeCheckStr("TToolBar rebuild", Performance.TIME_MARK));

		validate();

		//OSPLog.debug(Performance.timeCheckStr("TToolBar rebuild validate", Performance.TIME_MARK));

		TFrame.repaintT(this);
	}

	private void checkEnabled(boolean refreshTracks) {
		refreshZoomButton();
		calibrationButton.refresh();
		rulerButton.refresh();
		drawingButton.refresh();
		stretchButton.setSelected(vStretch > 1 || aStretch > 1);
		stretchOffItem.setText(TrackerRes.getString("TToolBar.MenuItem.StretchOff")); //$NON-NLS-1$
		stretchOffItem.setEnabled(vStretch > 1 || aStretch > 1);
		setMenuText();
		if (panel().getPlayer() != null) {
			VideoClip clip = panel().getPlayer().getVideoClip();
			ClipInspector inspector = clip.getClipInspector();
			clipSettingsButton.setSelected(inspector != null && inspector.isVisible());
		}
		CoordAxes axes = panel().getAxes();
		if (axes != null) {
			axesButton.setSelected(axes.isVisible());
			axes.updateListenerVisible(this);
		}
//		ArrayList<TTrack> tracks = trackerPanel().getUserTracks();
//		trackControlButton.setEnabled(!tracks.isEmpty());
		trackControlButton.setEnabled(true);
//		autotrackerButton.setEnabled(trackerPanel().getVideo() != null);
		// refresh all tracks
		if (refreshTracks) {
			refreshTracks();
		}
		// OSPLog.debug(Performance.timeCheckStr("TToolBar refreshAsync tracks",
		// Performance.TIME_MARK));
		TPoint pt = panel().getSelectedPoint();
		if (pt != null)
			pt.showCoordinates(panel());

		// set trails icon
		if (trailIcons[trailLengthIndex] != trailButton.getIcon()) {
			trailButton.setIcon(trailIcons[trailLengthIndex]);
			FontSizer.setFont(trailButton);
		}

		// refresh pageViewTabs list
		pageViewTabs.clear();
		if (frame != null) {
			List<TView> views = frame.getTViews(panelID, TView.VIEW_PAGE, null);
			for (int i = 0; i < views.size(); i++) {
				PageTView page = (PageTView) views.get(i);
				for (TabView tab : page.tabs) {
					if (tab.data.url != null) {
						pageViewTabs.add(tab.data);
					}
				}
			}
			sortPageViewTabs();
		}
		// OSPLog.debug(Performance.timeCheckStr("TToolBar refreshAsync sortPageView",
		// Performance.TIME_MARK));

		boolean hasPageURLs = !pageViewTabs.isEmpty();
		desktopButton.setEnabled(hasPageURLs || !panel().supplementalFilePaths.isEmpty());
		if (desktopButton.isEnabled() && desktopButton.getParent() == null)
			rebuild();
	}

	private void refreshTracks() {
		TrackerPanel panel = panel();
		ArrayList<TTrack> tracks = panel.getTracks();
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
			track.setTrailLength(trailLengths[trailLengthIndex]);
			track.setTrailVisible(trailButton.isSelected());
			if (track instanceof PointMass) {
				PointMass p = (PointMass) track;
				p.setTraceVisible(traceVisButton.isSelected());
				p.setPositionVisible(panel, pVisButton.isSelected());
				p.setVVisible(panel, vVisButton.isSelected());
				p.setAVisible(panel, aVisButton.isSelected());
				p.setLabelsVisible(panel, labelsButton.isSelected());
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
				track.erase();
//				if (false)
//					p.repaint();
			} else if (track instanceof Vector) {
				Vector v = (Vector) track;
				v.setLabelsVisible(labelsButton.isSelected());
				doRepaint = true;
//				if (false)
//					v.repaint();
			}
		}
		if (doRepaint) {
			for (int i = 0; i < panel.andWorld.size(); i++) {
				frame.getTrackerPanelForID(panel.andWorld.get(i)).repaint();
			}
		}
	}
	
	@Override
	public void paint(Graphics g) {
		if (panel() == null || !panel().isPaintable() || getComponentCount() == 0)
			return;
		super.paint(g);
	}

	private void setMenuText() {
//		trackControlButton.setText(TrackerRes.getString("TrackControl.Button.NewTrack")); //$NON-NLS-1$
		trackControlButton.setText(TrackerRes.getString("Undo.Description.Track")); //$NON-NLS-1$
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
		eyeButton.setToolTipText(TrackerRes.getString("TToolbar.Button.Eye.Tooltip")); //$NON-NLS-1$
		trackControlButton.setToolTipText(TrackerRes.getString("TToolBar.Button.TrackControl.Tooltip")); //$NON-NLS-1$
		autotrackerButton.setToolTipText(TrackerRes.getString("TToolBar.Button.AutoTracker.Tooltip")); //$NON-NLS-1$
		fontSizeButton.setToolTipText(TrackerRes.getString("TToolBar.Button.FontSize.ToolTip")); //$NON-NLS-1$
	}

	/**
	 * Disposes of this toolbar
	 */
	@Override
	public void dispose() {
		System.out.println("TToolBar.dispose " + panelID);
		
		disposed = true;
		if (refreshTimer != null)
			refreshTimer.stop();
		refreshTimer = null;
		removeAll();
		panel().removeListeners(panelProps, this);
		for (Integer n : TTrack.panelActiveTracks.keySet()) {
			TTrack track = TTrack.panelActiveTracks.get(n);
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this);
			track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this);
		}
		pageViewTabs.clear();
		panelID = null;
		frame = null;
	}

	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}

	/**
	 * Responds to the property change events.
	 *
	 * @param e the property change event
	 */

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO:
		case TTrack.PROPERTY_TTRACK_LOCKED:
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDPOINT:
			refresh(REFRESH__PROPERTY_VIDEO);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_SELECTEDTRACK:
			// refresh info dialog if visible
			panel().refreshNotesDialog();
			refresh(REFRESH__PROPERTY_SELECTED_TRACK);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_MAGNIFICATION:
			refreshZoomButton();
			break;
		case TTrack.PROPERTY_TTRACK_VISIBLE:
			if (e.getSource() == panel().getAxes()) {
				axesButton.setSelected(panel().getAxes().isVisible());
			} else {
				calibrationButton.refresh();
				rulerButton.refresh();
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK:
			if (e.getOldValue() != null) { // track has been removed
				TTrack track = (TTrack) e.getOldValue();
				panel().calibrationTools.remove(track);
				panel().visibleCalibrationTools.remove(track);
				panel().measuringTools.remove(track);
				panel().visibleMeasuringTools.remove(track);
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, this); // $NON-NLS-1$
				track.removePropertyChangeListener(TTrack.PROPERTY_TTRACK_LOCKED, this); // $NON-NLS-1$
				if (panel().visibleCalibrationTools.isEmpty()) {
					calibrationButton.setSelected(false);
				}
			}
			refresh(REFRESH__PROPERTY_TRACK_TRUE);
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR:
			for (TTrack track : TTrack.panelActiveTracks.values()) {
				panel().calibrationTools.remove(track);
				panel().visibleCalibrationTools.remove(track);
				panel().measuringTools.remove(track);
				panel().visibleMeasuringTools.remove(track);
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
			control.setValue("trail_length", TToolBar.trailLengths[toolbar.trailLengthIndex]); //$NON-NLS-1$
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
			toolbar.setTrailLength(control.getInt("trail_length")); //$NON-NLS-1$
			toolbar.vStretch = control.getInt("stretch"); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("stretch_acceleration")) { //$NON-NLS-1$
				toolbar.aStretch = control.getInt("stretch_acceleration"); //$NON-NLS-1$
			} else
				toolbar.aStretch = toolbar.vStretch;
			return obj;
		}
	}
	
	private void setTrailLength(int length) {
		if (length == Integer.MIN_VALUE) // may occur if no xml property
			return;
		//  { 1, 4, 15, 0 }
		if (length <= 0 || length > trailLengths[trailLengths.length - 2]) {
			trailLengthIndex = trailLengths.length - 1;
		}
		else for (int i = 0; i < trailLengths.length - 1; i++) {
			if (trailLengths[i] >= length) {
				trailLengthIndex = i;
				break;
			}
		}
	}

	/**
	 * A button to manage the creation and visibility of calibration tools.
	 */
	protected class CalibrationButton extends TButton implements ActionListener {

		boolean showPopup;

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
					showPopup = e.getX() > (18 + dw / 2) || panel().visibleCalibrationTools.isEmpty();
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
			JPopupMenu popup = new JPopupMenu();
			JMenuItem item;
			for (TTrack track : panel().calibrationTools) {
				item = new JCheckBoxMenuItem(track.getName());
				item.setSelected(panel().visibleCalibrationTools.contains(track));
				item.setActionCommand(track.getName());
				item.addActionListener(this);
				popup.add(item);
			}
			// new tools menu
			JMenu newToolsMenu = getCalibrationToolsMenu();
			if (newToolsMenu.getItemCount() > 0) {
				if (!panel().calibrationTools.isEmpty())
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
			if (panel().isEnabled("calibration.stick")) { //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("Stick.Name")); //$NON-NLS-1$
				item.addActionListener((e) -> {
						TapeMeasure track = new TapeMeasure();
						track.setColor(Color.BLUE);
						track.setStickMode(true);
						double scale = panel().getCoords().getScaleX(0);
						track.setCalibrator(scale == 1.0 ? 1.0 : null);
						// assign a default name
						String name = TrackerRes.getString("CalibrationStick.New.Name"); //$NON-NLS-1$
						track.setName(panel().getNextName(name, " "));
						panel().addTrack(track);
						calibrationButton.setSelected(true);

						// show all tools in visibleTools list
						for (TTrack next : panel().visibleCalibrationTools) {
							showCalibrationTool(next);
						}

						// mark immediately if preferred
						if (Tracker.centerCalibrationStick) {
							// place at center of viewport
							MainTView mainView = frame.getMainView(panel());
							Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
							int xpix = rect.x + rect.width / 2;
							int ypix = rect.y + rect.height / 2;
							double x = panel().pixToX(xpix);
							double y = panel().pixToY(ypix);
							track.createStep(0, x - 100, y - 20, x + 100, y - 20); // length 200 image units
						}

						panel().setSelectedTrack(track);
				});
				newToolsMenu.add(item);
			}

			if (panel().isEnabled("calibration.points")) { //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("Calibration.Name")); //$NON-NLS-1$
				item.addActionListener((e) -> {
						Calibration track = new Calibration();
						// assign a default name
						String name = TrackerRes.getString("Calibration.New.Name"); //$NON-NLS-1$
						track.setName(panel().getNextName(name, " "));

						panel().addTrack(track);
						calibrationButton.setSelected(true);
						// show all tools in visibleTools list
						for (TTrack next : panel().visibleCalibrationTools) {
							showCalibrationTool(next);
						}
						panel().setSelectedTrack(track);
						panel().getAxes().setVisible(true);
				});
				newToolsMenu.add(item);
			}

			if (panel().isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("OffsetOrigin.Name")); //$NON-NLS-1$
				item.addActionListener((e) -> {
						OffsetOrigin track = new OffsetOrigin();
						// assign a default name
						String name = TrackerRes.getString("OffsetOrigin.New.Name"); //$NON-NLS-1$
						track.setName(panel().getNextName(name, " "));
						panel().addTrack(track);
						calibrationButton.setSelected(true);
						// show all tools in visibleTools list
						for (TTrack next : panel().visibleCalibrationTools) {
							showCalibrationTool(next);
						}
						panel().setSelectedTrack(track);
						panel().getAxes().setVisible(true);
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
				panel().setSelectedPoint(null);
				panel().selectedSteps.clear();
				panel().hideMouseBox();
				if (!calibrationButton.isSelected()) {
					calibrationButton.setSelected(true);
					// show tools in visibleTools list
					for (TTrack track : panel().visibleCalibrationTools) {
						showCalibrationTool(track);
					}
				} else {
					calibrationButton.setSelected(false);
					// hide all tools
					for (TTrack track : panel().calibrationTools) {
						hideCalibrationTool(track);
					}
				}
				TFrame.repaintT(panel());
			} else { // menuItem action
						// see which item changed and show/hide corresponding tool
				panel().setSelectedPoint(null);
				panel().selectedSteps.clear();
				JMenuItem source = (JMenuItem) e.getSource();
				for (TTrack track : panel().calibrationTools) {
					if (e.getActionCommand().equals(track.getName())) {
						if (source.isSelected()) {
							panel().visibleCalibrationTools.add(track);
							calibrationButton.setSelected(true);
							// show only tools in visibleTools
							for (TTrack next : panel().visibleCalibrationTools) {
								showCalibrationTool(next);
							}
						} else {
							hideCalibrationTool(track);
							panel().visibleCalibrationTools.remove(track);
							boolean toolsVisible = false;
							for (TTrack next : panel().visibleCalibrationTools) {
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
				int n = panel().getFrameNumber();
				Step step = track.getStep(n);
				if (step == null || step.getPoints()[1] == null) {
					panel().setSelectedTrack(track);
				}
			} else if (track instanceof OffsetOrigin) {
				int n = panel().getFrameNumber();
				Step step = track.getStep(n);
				if (step == null) {
					panel().setSelectedTrack(track);
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
			if (panel().getSelectedTrack() == track) {
				panel().setSelectedTrack(null);
			}
		}

		/**
		 * Refreshes this button.
		 */
		void refresh() {
			setToolTipText(TrackerRes.getString("TToolbar.Button.TapeVisible.Tooltip")); //$NON-NLS-1$
			// add PROPERTY_TTRACK_VISIBLE property change listeners to calibration tools
			for (TTrack track : panel().calibrationTools) {
				track.updateListenerVisible(TToolBar.this);
			}
			// check visibility of tools and state of menu items
			boolean toolsVisible = false;
			for (TTrack track : panel().calibrationTools) {
				toolsVisible = toolsVisible || track.isVisible();
			}
			if (notYetCalibrated && toolsVisible) {
				notYetCalibrated = false;
			}
			setSelected(toolsVisible);
		}

	} // end calibration button
	
	/**
	 * A button to manage the creation and visibility of measuring tools.
	 */
	protected class RulerButton extends TButton implements ActionListener {

		boolean showPopup;

		/**
		 * Constructor.
		 */
		private RulerButton() {
			setIcons(rulerIcon, rulerOnIcon);
			setRolloverIcon(rulerRolloverIcon);
			setRolloverSelectedIcon(rulerOnRolloverIcon);
			// mouse listener to distinguish between popup and tool visibility actions
			addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					int w = rulerRolloverIcon.getIconWidth();
					int dw = rulerButton.getWidth() - w;
					// show popup if right side of button clicked or if no tools selected
					showPopup = e.getX() > (18 + dw / 2) || panel().measuringTools.isEmpty();
				}
			});
			addActionListener(this);
		}

		/**
		 * @return the popup, or null if the right side of this button was clicked
		 */

		@Override
		protected JPopupMenu getPopup() {
			if (!showPopup)
				return null;

			JPopupMenu popup = new JPopupMenu();
			JMenuItem item;
			for (TTrack track : panel().measuringTools) {
				item = new JCheckBoxMenuItem(track.getName());
				item.setSelected(panel().visibleMeasuringTools.contains(track));
				item.setActionCommand(track.getName());
				item.addActionListener(this);
				popup.add(item);
			}
			// new tools menu
			JMenu newToolsMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewTrack")); //$NON-NLS-1$
			TMenuBar.refreshMeasuringToolsMenu(panel(), newToolsMenu);
			if (newToolsMenu.getItemCount() > 0) {
				if (!panel().visibleMeasuringTools.isEmpty())
					popup.addSeparator();
				popup.add(newToolsMenu);
			}
			FontSizer.setFonts(popup);
			return popup;
		}

		/**
		 * Responds to action events from both this button and the popup items.
		 *
		 * @param e the action event
		 */

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == rulerButton) { // button action: show/hide tools
				if (showPopup)
					return;
				panel().setSelectedPoint(null);
				panel().selectedSteps.clear();
				panel().hideMouseBox();
				if (!rulerButton.isSelected()) {
					rulerButton.setSelected(true);
					// show tools in visibleMeasuringTools list
					for (TTrack track : panel().visibleMeasuringTools) {
						showMeasuringTool(track);
					}
				} else {
					rulerButton.setSelected(false);
					// hide all tools
					for (TTrack track : panel().measuringTools) {
						hideMeasuringTool(track);
					}
				}
				TFrame.repaintT(panel());
			} 
			else { // menuItem action
				// see which item changed and show/hide corresponding tool
				panel().setSelectedPoint(null);
				panel().selectedSteps.clear();
				JMenuItem source = (JMenuItem) e.getSource();
				for (TTrack track : panel().measuringTools) {
					if (e.getActionCommand().equals(track.getName())) {
						if (source.isSelected()) {
							panel().visibleMeasuringTools.add(track);
							// show only tools in visibleTools
							for (TTrack next : panel().visibleMeasuringTools) {
								showMeasuringTool(next);
							}
						} else {
							hideMeasuringTool(track);
							panel().visibleMeasuringTools.remove(track);
						}
					}
				}
			}
			refresh();
		}

		/**
		 * Shows a measuring tool.
		 *
		 * @param track a measuring tool
		 */
		void showMeasuringTool(TTrack track) {
			track.erase();
			track.setVisible(true);
			if (track instanceof CircleFitter) {
				CircleFitter fitter = (CircleFitter)track;
				CircleFitterStep step = (CircleFitterStep) fitter.getStep(0);
				if (step.getValidDataPoints().size() < 3) {
					panel().setSelectedTrack(track);
				}
			}
		}

		/**
		 * Hides a measuring tool.
		 *
		 * @param track
		 */
		void hideMeasuringTool(TTrack track) {
			track.setVisible(false);
			if (panel().getSelectedTrack() == track) {
				panel().setSelectedTrack(null);
			}
		}

		/**
		 * Refreshes this button.
		 */
		void refresh() {
			setToolTipText(TrackerRes.getString("TToolbar.Button.RulerVisible.Tooltip")); //$NON-NLS-1$
			// add PROPERTY_TTRACK_VISIBLE property change listeners to measuring tools
			for (TTrack track : panel().measuringTools) {
				track.updateListenerVisible(TToolBar.this);
			}
			// check visibility of tools
			boolean toolsVisible = false;
			for (TTrack track : panel().measuringTools) {
				if (track.isVisible()) {
					panel().visibleMeasuringTools.add(track);
					toolsVisible = true;
				}
			}
			setSelected(toolsVisible);
		}

	} // end ruler button

	
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
			drawingVisibleCheckbox.addActionListener((e) -> {drawingVisibleCheckbox.setSelected(!drawingVisibleCheckbox.isSelected());
					panel().setSelectedPoint(null);
					panel().selectedSteps.clear();
					PencilDrawer drawer = PencilDrawer.getDrawer(panel());
					drawer.setDrawingsVisible(drawingVisibleCheckbox.isSelected());
					TFrame.repaintT(panel());
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
			return popup;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (showPopup)
				return;
			panel().setSelectedPoint(null);
			panel().selectedSteps.clear();
			panel().hideMouseBox();
			setSelected(!isSelected());
			PencilDrawer drawer = PencilDrawer.getDrawer(panel());
			drawer.getDrawingControl().setVisible(isSelected());
			if (isSelected()) {
				if (drawer.scenes.isEmpty()) {
					drawer.addNewScene();
				} else {	
					PencilScene scene = drawer.getSceneAtFrame(panel().getFrameNumber());
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
			PencilDrawer drawer = PencilDrawer.getDrawer(panel());
			drawingVisibleCheckbox.setSelected(drawer.areDrawingsVisible());
			drawingVisibleCheckbox.setIcon(drawer.areDrawingsVisible() ? checkboxOnIcon : checkboxOffIcon);
			drawingVisibleCheckbox.setEnabled(!PencilDrawer.isDrawing(panel()));
		}

	}
	
	protected TrackerPanel panel() {
		return (frame == null ? null : frame.getTrackerPanelForID(panelID));
	}

	@Override
	public String toString() {
		return "[TToolBar " + panelID + "]";
	}

}
