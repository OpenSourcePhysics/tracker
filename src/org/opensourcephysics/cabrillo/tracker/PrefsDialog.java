/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2025 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.IntegerField;
import org.opensourcephysics.media.mov.MovieFactory;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.JREFinder;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This displays and sets preferences for a TrackerPanel.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class PrefsDialog extends JDialog {

	// static constants
	final static Color MEDIUM_RED = new Color(255, 120, 140);

	// static fields
	protected static boolean webStartWarningShown;
	protected static String userHome, javaHome;
	protected static FilenameFilter trackerJarFilter;
	protected static File codeBaseDir;

	// instance fields
	protected TFrame frame;
	protected Integer panelID;

	protected JButton okButton, cancelButton;
	protected JButton allButton, noneButton, applyButton, saveButton;
	protected JButton relaunchButton, clearRecentButton, checkForUpgradeButton;
	protected JButton clearHostButton, browseCacheButton, clearCacheButton, setCacheButton, setRunButton;
	protected JTextField cacheField, runField;
	protected JPanel checkPanel;
	protected JPanel mainButtonBar;
	protected JTabbedPane tabbedPane;
	protected JPanel configPanel, runtimePanel, videoPanel, generalPanel, actionsPanel, displayPanel;
	protected TitledBorder checkPanelBorder, lfSubPanelBorder, langSubPanelBorder, hintsSubPanelBorder,
			unitsSubPanelBorder, versionSubPanelBorder, jreSubPanelBorder, memorySubPanelBorder, runSubPanelBorder,
			videoTypeSubPanelBorder, xuggleSpeedSubPanelBorder, warningsSubPanelBorder, recentSubPanelBorder,
			cacheSubPanelBorder, logLevelSubPanelBorder, upgradeSubPanelBorder, fontSubPanelBorder,
			resetToStep0SubPanelBorder, decimalSeparatorBorder, mouseWheelSubPanelBorder,
			calibrationStickSubPanelBorder, dataGapSubPanelBorder, trailLengthSubPanelBorder,
			pointmassFootprintSubPanelBorder;

	protected IntegerField memoryField;
	protected JLabel memoryLabel, recentSizeLabel, lookFeelLabel, cacheLabel, versionLabel, runLabel;
	protected JCheckBox defaultMemoryCheckbox, hintsCheckbox, vidWarningCheckbox, showGapsCheckbox, xuggleErrorCheckbox,
			variableDurationCheckBox, resetToStep0Checkbox, autofillCheckbox;
	protected int memorySize = Tracker.requestedMemorySize;
	protected JSpinner recentSizeSpinner, runSpinner;
	protected JComboBox<String> lookFeelDropdown, languageDropdown, jreDropdown, trailLengthDropdown,
			checkForUpgradeDropdown, versionDropdown, logLevelDropdown, fontSizeDropdown;
//  protected JLabel xuggleVersionLabel;
	protected JComboBox<Footprint> footprintDropdown;
	protected JRadioButton vm32Button, vm64Button;
	protected JRadioButton movieEngineButton, noEngineButton;
	protected JRadioButton radiansButton, degreesButton;
	protected JRadioButton scrubButton, zoomButton;
	protected JRadioButton markStickEndsButton, centerStickButton;
	protected JRadioButton xuggleFastButton, xuggleSlowButton;
	protected JRadioButton defaultDecimalButton, periodDecimalButton, commaDecimalButton;
	protected OSPRuntime.Version[] trackerVersions;
	protected boolean relaunching, refreshing;

	// previous values
	protected Set<String> prevEnabled = new TreeSet<String>();
	protected int prevMemory, prevRecentCount, prevUpgradeInterval, prevFontLevel, prevFontLevelPlus,
			prevTrailLengthIndex;
	protected String prevLookFeel, prevLocaleName, prevJRE, prevTrackerJar, prevEngine, prevDecimalSeparator,
			prevPointmassFootprint;
	protected boolean prevHints, prevRadians, prevFastXuggle, prevCenterCalibrationStick, prevWarnVariableDuration,
			prevWarnNoVideoEngine, prevWarnXuggleError, prevWarnXuggleVersion, prevShowGaps, prevMarkAtCurrentFrame,
			prevClearCacheOnExit, prevUse32BitVM, prevWarnCopyFailed, prevZoomMouseWheel, prevAutofill;
	protected File prevCache;
	protected String[] prevExecutables;
	protected Level prevLogLevel;

	static {
		trackerJarFilter = new org.opensourcephysics.cabrillo.tracker.deploy.TrackerJarFilter();
		try {
			userHome = OSPRuntime.getUserHome();
			javaHome = System.getProperty("java.home"); //$NON-NLS-1$
			URL url = TrackerStarter.class.getProtectionDomain().getCodeSource().getLocation();
			File jarFile = new File(url.toURI());
			codeBaseDir = jarFile.getParentFile();
		} catch (Exception ex) {
		}
	}

	/**
	 * Constructs a PrefsDialog.
	 *
	 * @param panel the tracker panel
	 */
	public PrefsDialog(TrackerPanel panel, TFrame frame) {
		// non-modal
		super(panel==null? null: panel.getTFrame(), false);
		panelID = panel==null? null: panel.getID();
		this.frame = frame;
		setTitle(TrackerRes.getString("ConfigInspector.Title")); //$NON-NLS-1$
		findTrackerJars();
		createGUI();
	}

	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		if (vis) {
			savePrevious();
			findTrackerJars();
			refreshGUI();
		}
	}

	public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		TitledBorder[] borders = new TitledBorder[] { checkPanelBorder, lfSubPanelBorder, langSubPanelBorder,
				hintsSubPanelBorder, unitsSubPanelBorder, versionSubPanelBorder, jreSubPanelBorder,
				memorySubPanelBorder, runSubPanelBorder, videoTypeSubPanelBorder, xuggleSpeedSubPanelBorder,
				warningsSubPanelBorder, recentSubPanelBorder, cacheSubPanelBorder, logLevelSubPanelBorder,
				upgradeSubPanelBorder, fontSubPanelBorder, resetToStep0SubPanelBorder, decimalSeparatorBorder,
				mouseWheelSubPanelBorder, calibrationStickSubPanelBorder, dataGapSubPanelBorder,
				trailLengthSubPanelBorder, pointmassFootprintSubPanelBorder };
		FontSizer.setFonts(borders, level);
		@SuppressWarnings("unchecked")
		JComboBox<String>[] dropdowns = new JComboBox[] { lookFeelDropdown, languageDropdown, fontSizeDropdown,
				jreDropdown, checkForUpgradeDropdown, versionDropdown, logLevelDropdown };
		for (JComboBox<String> next : dropdowns) {
			if (next == null)
				continue;
			int n = next.getSelectedIndex();
			String[] items = new String[next.getItemCount()];
			for (int i = 0; i < items.length; i++) {
				items[i] = next.getItemAt(i);
			}
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(items);
			next.setModel(model);
			next.setSelectedItem(n);
		}
		if (footprintDropdown != null) {
			int n = footprintDropdown.getSelectedIndex();
			Footprint[] items = new Footprint[footprintDropdown.getItemCount()];
			for (int i = 0; i < items.length; i++) {
				items[i] = footprintDropdown.getItemAt(i);
			}
			DefaultComboBoxModel<Footprint> model = new DefaultComboBoxModel<Footprint>(items);
			footprintDropdown.setModel(model);
			footprintDropdown.setSelectedItem(n);
		}
	}

//_____________________________ private methods ____________________________

	/**
	 * Finds the tracker jars.
	 */
	private void findTrackerJars() {
		trackerVersions = new OSPRuntime.Version[] { new OSPRuntime.Version("0") }; //$NON-NLS-1$
		if (Tracker.trackerHome == null || codeBaseDir == null) {
			return;
		}
		String jarHome = OSPRuntime.isMac() ? codeBaseDir.getAbsolutePath() : Tracker.trackerHome;
		File dir = new File(jarHome);
		String[] fileNames = dir.list(trackerJarFilter);
		if (fileNames != null && fileNames.length > 0) {
			TreeSet<OSPRuntime.Version> versions = new TreeSet<OSPRuntime.Version>();
			for (int i = 0; i < fileNames.length; i++) {
				if ("tracker.jar".equals(fileNames[i].toLowerCase())) {//$NON-NLS-1$
					versions.add(new OSPRuntime.Version("0")); //$NON-NLS-1$
				} else {
					versions.add(new OSPRuntime.Version(fileNames[i].substring(8, fileNames[i].length() - 4)));
				}
			}
			trackerVersions = versions.toArray(new OSPRuntime.Version[versions.size()]);
		}
	}

	/**
	 * Creates the visible components of this panel.
	 */
	private void createGUI() {
		tabbedPane = new JTabbedPane();
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		// ok button
		okButton = new JButton();
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyPrefs();
				setVisible(false);
				// refresh the frame
				if (frame != null)
					frame.refresh();
			}
		});
		// cancel button
		cancelButton = new JButton();
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				revert();
				setVisible(false);
				// refresh the frame
				if (frame != null)
					frame.refresh();
			}
		});

		// relaunch button
		relaunchButton = new JButton();
		relaunchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyPrefs();
				frame.relaunchCurrentTabs();
			}
		});
		Color color = Color.WHITE;

		// configuration panel for Java only
		if (!OSPRuntime.isJS) {
			configPanel = new JPanel(new BorderLayout());
			// config checkPanel
			int n = 1 + Tracker.getFullConfig().size() / 2;
			checkPanel = new JPanel(new GridLayout(n, 2));
			checkPanel.setBackground(color);
			checkPanelBorder = BorderFactory.createTitledBorder(TrackerRes.getString("ConfigInspector.Border.Title")); //$NON-NLS-1$
			checkPanel.setBorder(checkPanelBorder);
			// config checkboxes
			Iterator<String> it = Tracker.getFullConfig().iterator();
			while (it.hasNext()) {
				String item = it.next();
				JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(item);
				checkbox.setOpaque(false);
				checkPanel.add(checkbox);
			}
			JScrollPane scroller = new JScrollPane(checkPanel);
			scroller.getVerticalScrollBar().setUnitIncrement(16);
			scroller.setPreferredSize(new Dimension(450, 200));
			configPanel.add(scroller, BorderLayout.CENTER);
			// apply button
			applyButton = new JButton();
			applyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateConfig();
					refreshGUI();
					frame.refresh();
				}
			});
			// create all and none buttons
			allButton = new JButton();
			allButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Component[] checkboxes = checkPanel.getComponents();
					for (int i = 0; i < checkboxes.length; i++) {
						JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) checkboxes[i];
						checkbox.setSelected(true);
					}
				}
			});
			noneButton = new JButton();
			noneButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Component[] checkboxes = checkPanel.getComponents();
					for (int i = 0; i < checkboxes.length; i++) {
						JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) checkboxes[i];
						checkbox.setSelected(false);
					}
				}
			});
			// save button
			saveButton = new JButton();
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveConfigAsDefault();
				}
			});
			JPanel configButtonBar = new JPanel();
			configButtonBar.add(allButton);
			configButtonBar.add(noneButton);
			configButtonBar.add(applyButton);
			configButtonBar.add(saveButton);
			configPanel.add(configButtonBar, BorderLayout.NORTH);
		} // end configuration panel

		Border etched = BorderFactory.createEtchedBorder();

		// display panel
		displayPanel = new JPanel(new BorderLayout());

		Box box = Box.createVerticalBox();
		displayPanel.add(box, BorderLayout.CENTER);

		// look&feel and language subpanels side by side
		Box horz = Box.createHorizontalBox();
		box.add(horz);

		// look and feel subpanel
//		JPanel lfSubPanel = new JPanel();
//		horz.add(lfSubPanel);
//		lfSubPanel.setBackground(color);
//
//		lfSubPanelBorder = BorderFactory.createTitledBorder(TrackerRes.getString("PrefsDialog.LookFeel.BorderTitle")); //$NON-NLS-1$
//		lfSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, lfSubPanelBorder));
//		lookFeelDropdown = new JComboBox<String>();
//		lookFeelDropdown.addItem(OSPRuntime.DEFAULT_LF.toLowerCase());
//		Object selectedItem = OSPRuntime.DEFAULT_LF;
//		// get alphabetical list of look/feel types
//		Set<String> lfTypes = new TreeSet<String>();
//		for (String next : OSPRuntime.LOOK_AND_FEEL_TYPES.keySet()) {
//			if (next.equals(OSPRuntime.DEFAULT_LF))
//				continue;
//			lfTypes.add(next.toLowerCase());
//			if (next.equals(Tracker.lookAndFeel))
//				selectedItem = next.toLowerCase();
//		}
//		for (String next : lfTypes) {
//			lookFeelDropdown.addItem(next);
//		}
//		lookFeelDropdown.setSelectedItem(selectedItem);
//		lookFeelDropdown.addItemListener(new ItemListener() {
//			@Override
//			public void itemStateChanged(ItemEvent e) {
//				String lf = lookFeelDropdown.getSelectedItem().toString().toUpperCase();
//				if (!lf.equals(Tracker.lookAndFeel)) {
//					Tracker.lookAndFeel = lf;
//				}
//			}
//		});
//		lfSubPanel.add(lookFeelDropdown);

		if (!OSPRuntime.isJS) {
			// language subpanel
			JPanel langSubPanel = new JPanel();
			horz.add(langSubPanel);
			langSubPanel.setBackground(color);
			langSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.Language.BorderTitle")); //$NON-NLS-1$
			langSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, langSubPanelBorder));
			languageDropdown = new JComboBox<String>();
			languageDropdown.addItem(TrackerRes.getString("PrefsDialog.Language.Default")); //$NON-NLS-1$
			int index = 0, selectedIndex = 0;
			for (Locale next : Tracker.getLocales()) {
				index++;
				String s = OSPRuntime.getDisplayLanguage(next);
				// special handling for portuguese BR and PT
				if (next.getLanguage().equals("pt")) { //$NON-NLS-1$
					s += " (" + next.getCountry() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				languageDropdown.addItem(s);
				if (next.equals(Locale.getDefault()) && next.toString().equals(Tracker.preferredLocale)) {
					selectedIndex = index;
				}
			}
			languageDropdown.setSelectedIndex(selectedIndex);
			languageDropdown.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					int index = languageDropdown.getSelectedIndex();
					if (index == 0)
						Tracker.setPreferredLocale(null);
					else {
						Tracker.setPreferredLocale(Tracker.getLocales()[index - 1].toString());
					}
				}
			});
			langSubPanel.add(languageDropdown);

			// font level subpanel
			JPanel fontSubPanel = new JPanel();
			horz.add(fontSubPanel);
			fontSubPanel.setBackground(color);
			fontSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.FontSize.BorderTitle")); //$NON-NLS-1$
			fontSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, fontSubPanelBorder));

			// create font size dropdown
			fontSizeDropdown = new JComboBox<String>();
			String defaultLevel = TrackerRes.getString("TMenuBar.MenuItem.DefaultFontSize"); //$NON-NLS-1$
			fontSizeDropdown.addItem(defaultLevel);
			int preferredLevel = Tracker.preferredFontLevel + Tracker.preferredFontLevelPlus;
			int maxLevel = Math.max(preferredLevel, Tracker.maxFontLevel);
			for (int i = 1; i <= maxLevel; i++) {
				String s = "+" + i; //$NON-NLS-1$
				fontSizeDropdown.addItem(s);
			}
			fontSizeDropdown.setSelectedIndex(preferredLevel);
			fontSizeDropdown.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					int preferredLevel = fontSizeDropdown.getSelectedIndex();
					Tracker.preferredFontLevel = Math.min(preferredLevel, 3);
					Tracker.preferredFontLevelPlus = preferredLevel - Tracker.preferredFontLevel;
				}
			});
			fontSubPanel.add(fontSizeDropdown);
		}
		// angle units and hints subpanels side by side
		horz = Box.createHorizontalBox();
		box.add(horz);

		// angle units subpanel
		JPanel unitsSubPanel = new JPanel();
		horz.add(unitsSubPanel);
		unitsSubPanel.setBackground(color);
		unitsSubPanelBorder = BorderFactory.createTitledBorder(TrackerRes.getString("TMenuBar.Menu.AngleUnits")); //$NON-NLS-1$
		unitsSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, unitsSubPanelBorder));

		ButtonGroup buttonGroup = new ButtonGroup();
		radiansButton = new JRadioButton();
		radiansButton.setOpaque(false);
		radiansButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		radiansButton.setSelected(Tracker.isRadians);
		buttonGroup.add(radiansButton);
		degreesButton = new JRadioButton();
		degreesButton.setOpaque(false);
		degreesButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		degreesButton.setSelected(!Tracker.isRadians);
		buttonGroup.add(degreesButton);
		unitsSubPanel.add(radiansButton);
		unitsSubPanel.add(degreesButton);

		if (!OSPRuntime.isJS) {
			// hints subpanel
			hintsCheckbox = new JCheckBox();
			hintsCheckbox.setOpaque(false);
			hintsCheckbox.setSelected(Tracker.showHintsByDefault);
			hintsCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Tracker.showHintsByDefault = hintsCheckbox.isSelected();
				}
			});
			JPanel hintsSubPanel = new JPanel();
			horz.add(hintsSubPanel);
			hintsSubPanel.setBackground(color);
			hintsSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.Hints.BorderTitle")); //$NON-NLS-1$
			hintsSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, hintsSubPanelBorder));
			hintsSubPanel.add(hintsCheckbox);
		}
		// decimal separator subpanel
		horz = Box.createHorizontalBox();
		box.add(horz);

		// decimal separator subpanel
		JPanel decimalSubPanel = new JPanel();
		horz.add(decimalSubPanel);
		decimalSubPanel.setBackground(color);
		decimalSeparatorBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("NumberFormatSetter.TitledBorder.DecimalSeparator.Text")); //$NON-NLS-1$
		decimalSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, decimalSeparatorBorder));
		Action decimalSeparatorAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.preferredDecimalSeparator = periodDecimalButton.isSelected() ? "." : //$NON-NLS-1$
					commaDecimalButton.isSelected() ? "," : null; //$NON-NLS-1$
				OSPRuntime.setPreferredDecimalSeparator(Tracker.preferredDecimalSeparator);
			}
		};
		defaultDecimalButton = new JRadioButton();
		defaultDecimalButton.setOpaque(false);
		defaultDecimalButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		defaultDecimalButton.addActionListener(decimalSeparatorAction);
		periodDecimalButton = new JRadioButton();
		periodDecimalButton.setOpaque(false);
		periodDecimalButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		periodDecimalButton.addActionListener(decimalSeparatorAction);
		commaDecimalButton = new JRadioButton();
		commaDecimalButton.setOpaque(false);
		commaDecimalButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		commaDecimalButton.addActionListener(decimalSeparatorAction);
		ButtonGroup group = new ButtonGroup();
		group.add(defaultDecimalButton);
		group.add(periodDecimalButton);
		group.add(commaDecimalButton);
		decimalSubPanel.add(defaultDecimalButton);
		decimalSubPanel.add(periodDecimalButton);
		decimalSubPanel.add(commaDecimalButton);

		// end display panel

		// create button border && openFileIcon
		Border buttonBorder = BorderFactory.createEtchedBorder();
		Border space = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		buttonBorder = BorderFactory.createCompoundBorder(buttonBorder, space);
		Icon openFileIcon = Tracker.getResourceIcon("open.gif", true); //$NON-NLS-1$

		// runtime pane--only for Java
		if (!OSPRuntime.isJS) {
			runtimePanel = new JPanel(new BorderLayout());
			box = Box.createVerticalBox();
			runtimePanel.add(box, BorderLayout.CENTER);

			// tracker version subpanel
			JPanel versionSubPanel = new JPanel();
			box.add(versionSubPanel);
			versionSubPanel.setBackground(color);
			versionSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.Version.BorderTitle")); //$NON-NLS-1$
			versionSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, versionSubPanelBorder));
			int preferred = 0;
			versionDropdown = new JComboBox<String>();
			for (int i = 0; i < trackerVersions.length; i++) {
				String next = trackerVersions[i].toString();
				if (next.equals("0")) { //$NON-NLS-1$
					String s = TrackerRes.getString("PrefsDialog.Version.Default"); //$NON-NLS-1$
					versionDropdown.addItem(s);
				} else
					versionDropdown.addItem(next);
				if (Tracker.preferredTrackerJar != null && Tracker.preferredTrackerJar.indexOf("tracker-") > -1 //$NON-NLS-1$
						&& Tracker.preferredTrackerJar.indexOf(next) > -1) {
					preferred = i;
				}
			}
			versionDropdown.setSelectedIndex(preferred);
			versionDropdown.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					Object ver = versionDropdown.getSelectedItem();
					String jar = null;
					if (ver != null && !TrackerRes.getString("PrefsDialog.Version.Default").equals(ver)) { //$NON-NLS-1$
						jar = "tracker-" + ver + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					if (jar == null && Tracker.preferredTrackerJar != null) {
						Tracker.preferredTrackerJar = null;
					} else if (jar != null && !jar.equals(Tracker.preferredTrackerJar)) {
						Tracker.preferredTrackerJar = jar;
					}
					// determine if preferred tracker will use Xuggle 3.4 or Xuggle server
					String jarName = jar == null ? "tracker.jar" : jar;
					String jarHome = OSPRuntime.isMac() ? codeBaseDir.getAbsolutePath() : Tracker.trackerHome;
					String jarPath = XML.forwardSlash(new File(jarHome, jarName).getPath());
					boolean usesServer = TrackerStarter.usesXuggleServer(jarPath);
					int bitness = usesServer ? 64 : OSPRuntime.isWindows() ? 32 : 64;
//					xuggleVersionLabel.setText(usesServer? "(uses Xuggle 5.7)": "(uses Xuggle 3.4)");
					refreshJREDropdown(bitness);
				}
			});
			versionSubPanel.add(versionDropdown);
//			xuggleVersionLabel = new JLabel();	
//			xuggleVersionLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
//			versionSubPanel.add(xuggleVersionLabel);
			// jre subpanel
//			JPanel jreSubPanel = new JPanel(new BorderLayout());
			JPanel jreSubPanel = new JPanel();
			box.add(jreSubPanel);
			jreSubPanel.setBackground(color);
			jreSubPanelBorder = BorderFactory.createTitledBorder(TrackerRes.getString("PrefsDialog.JRE.BorderTitle")); //$NON-NLS-1$
			jreSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, jreSubPanelBorder));

			JPanel jreNorthPanel = new JPanel();
			jreNorthPanel.setBackground(color);
//			jreSubPanel.add(jreNorthPanel, BorderLayout.NORTH);
			JPanel jreSouthPanel = new JPanel();
			jreSouthPanel.setBackground(color);
//			jreSubPanel.add(jreSouthPanel, BorderLayout.SOUTH);

			int vmBitness = OSPRuntime.getVMBitness();
			vm32Button = new JRadioButton();
			vm32Button.setOpaque(false);
			vm32Button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
			vm32Button.setSelected(vmBitness == 32);
			vm32Button.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!vm32Button.isSelected())
						return;
					if (OSPRuntime.isWindows()) {
						refreshJREDropdown(32);
					}
				}
			});
			jreNorthPanel.add(vm32Button);

			vm64Button = new JRadioButton();
			vm64Button.setOpaque(false);
			vm64Button.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
			vm64Button.setSelected(vmBitness == 64);
			vm64Button.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!vm64Button.isSelected())
						return;
					refreshJREDropdown(64);
				}
			});
//			jreNorthPanel.add(vm64Button);

			jreDropdown = new JComboBox<String>();
//			jreSouthPanel.add(jreDropdown);
			jreSubPanel.add(jreDropdown);
			refreshJREDropdown(vmBitness);

			// memory subpanel
			JPanel memorySubPanel = new JPanel();
			box.add(memorySubPanel);
			memorySubPanel.setBackground(color);
			memorySubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.Memory.BorderTitle")); //$NON-NLS-1$
			memorySubPanel.setBorder(BorderFactory.createCompoundBorder(etched, memorySubPanelBorder));
			memorySubPanel.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					requestFocusInWindow();
				}
			});
			defaultMemoryCheckbox = new JCheckBox();
			defaultMemoryCheckbox.setOpaque(false);
			memoryLabel = new JLabel("MB"); //$NON-NLS-1$
			memoryField = new IntegerField(4);
			memoryField.setMinValue(TrackerStarter.MINIMUM_MEMORY_SIZE);
			memoryField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if (memorySize != memoryField.getIntValue()) {
						memorySize = memoryField.getIntValue();
					}
				}
			});
			memoryField.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (defaultMemoryCheckbox.isSelected()) {
						defaultMemoryCheckbox.doClick(0);
					}
				}
			});
			memoryField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					memorySize = memoryField.getIntValue();
				}
			});
			defaultMemoryCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean selected = defaultMemoryCheckbox.isSelected();
					if (selected) {
						memoryField.setEnabled(false);
						memoryLabel.setEnabled(false);
						memoryField.setText(null);
					} else {
						memoryField.setEnabled(true);
						memoryLabel.setEnabled(true);
						memoryField.setValue(memorySize);
						memoryField.requestFocusInWindow();
						memoryField.selectAll();
					}
				}
			});
			if (Tracker.preferredMemorySize > -1)
				memoryField.setValue(Tracker.preferredMemorySize);
			else {
				defaultMemoryCheckbox.setSelected(true);
				memoryField.setEnabled(false);
				memoryLabel.setEnabled(false);
				memoryField.setText(null);
			}
			memorySubPanel.add(defaultMemoryCheckbox);
			memorySubPanel.add(Box.createRigidArea(new Dimension(40, 1)));
			memorySubPanel.add(memoryField);
			memorySubPanel.add(memoryLabel);

			// run subpanel
			JPanel runSubPanel = new JPanel();
			box.add(runSubPanel);
			runSubPanel.setBackground(color);
			runSubPanelBorder = BorderFactory.createTitledBorder(TrackerRes.getString("PrefsDialog.Run.BorderTitle")); //$NON-NLS-1$
			runSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, runSubPanelBorder));

			final Action setRunAction = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String path = runField.getText();
					int n = (Integer) runSpinner.getValue();
					ArrayList<String> paths = new ArrayList<String>();
					if (Tracker.prelaunchExecutables.length > n) { // deal with existing entry
						if (path.equals(Tracker.prelaunchExecutables[n])) // no change
							return;
						if ("".equals(path)) { // eliminate entry //$NON-NLS-1$
							Tracker.prelaunchExecutables[n] = path;
							path = null; // done with this
						} else { // change entry
							Tracker.prelaunchExecutables[n] = path;
							path = null; // done with this
						}
					}
					// clean and relist existing entries
					for (String next : Tracker.prelaunchExecutables) {
						if (next != null && !"".equals(next) && !paths.contains(next)) //$NON-NLS-1$
							paths.add(next);
					}
					// add new entry, if any
					if (path != null && !"".equals(path) && !paths.contains(path)) //$NON-NLS-1$
						paths.add(path);
					Tracker.prelaunchExecutables = paths.toArray(new String[0]);
					for (int i = 0; i < Tracker.prelaunchExecutables.length; i++) {
						if (Tracker.prelaunchExecutables[i].equals(path)) {
							runSpinner.setValue(i);
							break;
						}
					}
					refreshTextFields();
				}
			};
			runSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 6, 1));
			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(runSpinner);
			runSpinner.setEditor(editor);
			runSpinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (runField.getBackground() == Color.yellow) {
						setRunAction.actionPerformed(null);
					} else {
						refreshTextFields();
					}
				}
			});
			runSubPanel.add(runSpinner);
			runField = new JTextField(27);
			runSubPanel.add(runField);
			runField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					runField.setBackground(Color.yellow);
				}
			});
			runField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if (runField.getBackground() == Color.yellow)
						setRunAction.actionPerformed(null);
				}
			});
			runField.addActionListener(setRunAction);

			setRunButton = new TButton(openFileIcon);
			setRunButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int result = JFileChooser.CANCEL_OPTION;
					File f = Tracker.trackerHome == null ? new File(".") : new File(Tracker.trackerHome); //$NON-NLS-1$
					JFileChooser chooser = getFileChooser(f, false);
					chooser.setDialogTitle(TrackerRes.getString("PrefsDialog.FileChooser.Title.Run")); //$NON-NLS-1$
					result = chooser.showOpenDialog(PrefsDialog.this);
					if (result == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						if (file != null) {
							runField.setText(file.getPath());
							setRunAction.actionPerformed(null);
						}
					}
				}
			});
			setRunButton.setBorder(buttonBorder);
			setRunButton.setContentAreaFilled(false);
			runSubPanel.add(setRunButton);
		} // end runtime panel

		// video panel
		videoPanel = new JPanel(new BorderLayout());
		box = Box.createVerticalBox();
		videoPanel.add(box, BorderLayout.CENTER);

		boolean movieEngineInstalled = MovieFactory.hasVideoEngine();

		// mouse wheel subpanel
		JPanel mouseWheelSubPanel = new JPanel();
		box.add(mouseWheelSubPanel);
		mouseWheelSubPanel.setBackground(color);
		mouseWheelSubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.Mousewheel.BorderTitle")); //$NON-NLS-1$
		mouseWheelSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, mouseWheelSubPanelBorder));

		zoomButton = new JRadioButton();
		zoomButton.setOpaque(false);
		zoomButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(zoomButton);
		scrubButton = new JRadioButton();
		scrubButton.setOpaque(false);
		scrubButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));

		buttonGroup.add(scrubButton);
		if (Tracker.scrubMouseWheel)
			scrubButton.setSelected(true);
		else
			zoomButton.setSelected(true);
		mouseWheelSubPanel.add(zoomButton);
		mouseWheelSubPanel.add(scrubButton);
		ActionListener mouseWheelAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.scrubMouseWheel = scrubButton.isSelected();
			}
		};
		zoomButton.addActionListener(mouseWheelAction);
		scrubButton.addActionListener(mouseWheelAction);

		// videoType subpanel
//		JPanel videoTypeSubPanel = new JPanel();
//    box.add(videoTypeSubPanel);
//		videoTypeSubPanel.setBackground(color);
//		videoTypeSubPanelBorder = BorderFactory
//				.createTitledBorder(TrackerRes.getString("PrefsDialog.VideoPref.BorderTitle")); //$NON-NLS-1$
//		videoTypeSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, videoTypeSubPanelBorder));
//
		movieEngineButton = new JRadioButton();
		movieEngineButton.setOpaque(false);
		movieEngineButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		movieEngineButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				xuggleFastButton.setEnabled(movieEngineButton.isSelected());
				xuggleSlowButton.setEnabled(movieEngineButton.isSelected());
				xuggleErrorCheckbox.setEnabled(movieEngineButton.isSelected());
			}
		});
		movieEngineButton.setEnabled(movieEngineInstalled);

		noEngineButton = new JRadioButton();
		noEngineButton.setOpaque(false);
		noEngineButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
		noEngineButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (!noEngineButton.isSelected())
					return;
				// BH! Doug, please check.
//    		VideoIO.setEngine(VideoIO.ENGINE_NONE);
			}
		});
//
//		videoTypeSubPanel.add(movieEngineButton);
//		videoTypeSubPanel.add(noEngineButton);

		if (!OSPRuntime.isJS) {
			// xuggle speed subpanel
			JPanel xuggleSpeedSubPanel = new JPanel();
			box.add(xuggleSpeedSubPanel);
			xuggleSpeedSubPanel.setBackground(color);
			xuggleSpeedSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.Xuggle.Speed.BorderTitle")); //$NON-NLS-1$
			if (!movieEngineInstalled)
				xuggleSpeedSubPanelBorder.setTitleColor(GUIUtils.getDisabledTextColor());
			xuggleSpeedSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, xuggleSpeedSubPanelBorder));
			buttonGroup = new ButtonGroup();
			xuggleFastButton = new JRadioButton();
			xuggleFastButton.setOpaque(false);
			xuggleFastButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
			xuggleFastButton.setSelected(movieEngineInstalled && Tracker.isXuggleFast);
			buttonGroup.add(xuggleFastButton);
			xuggleSlowButton = new JRadioButton();
			xuggleSlowButton.setOpaque(false);
			xuggleSlowButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
			xuggleSlowButton.setSelected(movieEngineInstalled && !Tracker.isXuggleFast);
			buttonGroup.add(xuggleSlowButton);
			xuggleSpeedSubPanel.add(xuggleFastButton);
			xuggleSpeedSubPanel.add(xuggleSlowButton);
		}

		// warnings subpanel
		JPanel warningsSubPanel = new JPanel(new BorderLayout());
		box.add(warningsSubPanel);
		warningsSubPanel.setBackground(color);
		warningsSubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.NoVideoWarning.BorderTitle")); //$NON-NLS-1$
		warningsSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, warningsSubPanelBorder));
		JPanel warningsNorthPanel = new JPanel();
		warningsNorthPanel.setBackground(color);
		warningsSubPanel.add(warningsNorthPanel, BorderLayout.NORTH);
		JPanel warningsCenterPanel = new JPanel();
		warningsCenterPanel.setBackground(color);
		JPanel warningsSouthPanel = new JPanel();
		warningsSouthPanel.setBackground(color);
		JPanel centerSouthPanel = new JPanel(new BorderLayout());
		centerSouthPanel.add(warningsCenterPanel, BorderLayout.NORTH);
		centerSouthPanel.add(warningsSouthPanel, BorderLayout.CENTER);
		warningsSubPanel.add(centerSouthPanel, BorderLayout.CENTER);

		if (!OSPRuntime.isJS) {
			vidWarningCheckbox = new JCheckBox();
			vidWarningCheckbox.setOpaque(false);
			vidWarningCheckbox.setSelected(Tracker.warnNoVideoEngine);
			vidWarningCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Tracker.warnNoVideoEngine = vidWarningCheckbox.isSelected();
				}
			});
			xuggleErrorCheckbox = new JCheckBox();
			xuggleErrorCheckbox.setOpaque(false);
			xuggleErrorCheckbox.setSelected(Tracker.warnXuggleError);
			xuggleErrorCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Tracker.warnXuggleError = xuggleErrorCheckbox.isSelected();
				}
			});
			warningsNorthPanel.add(vidWarningCheckbox);
			warningsCenterPanel.add(xuggleErrorCheckbox);
		}
		variableDurationCheckBox = new JCheckBox();
		variableDurationCheckBox.setOpaque(false);
		variableDurationCheckBox.setSelected(Tracker.warnVariableDuration);
		variableDurationCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.warnVariableDuration = variableDurationCheckBox.isSelected();
			}
		});

		warningsNorthPanel.add(variableDurationCheckBox);

		// set selected states of engine buttons AFTER creating the xugglefast,
		// xuggleslow and warnxuggle buttons
//		if (MovieFactory.hasVideoEngine()) {
//			movieEngineButton.setSelected(true);
//		} else
//			noEngineButton.setSelected(true);
		// end video panel

		// actions panel
		actionsPanel = new JPanel(new BorderLayout());
		box = Box.createVerticalBox();
		actionsPanel.add(box, BorderLayout.CENTER);

		horz = Box.createHorizontalBox();
		box.add(horz);

		// marking subpanel
		JPanel markingSubPanel = new JPanel();
		horz.add(markingSubPanel);
		markingSubPanel.setBackground(color);
		resetToStep0SubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.Marking.BorderTitle")); //$NON-NLS-1$
		markingSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, resetToStep0SubPanelBorder));

		resetToStep0Checkbox = new JCheckBox();
		resetToStep0Checkbox.setOpaque(false);
		resetToStep0Checkbox.setSelected(!Tracker.markAtCurrentFrame);
		resetToStep0Checkbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.markAtCurrentFrame = !resetToStep0Checkbox.isSelected();
			}
		});
		markingSubPanel.add(resetToStep0Checkbox);

		// calibration stick subpanel
		JPanel calibrationStickSubPanel = new JPanel();
		box.add(calibrationStickSubPanel);
		calibrationStickSubPanel.setBackground(color);
		calibrationStickSubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.CalibrationStick.BorderTitle")); //$NON-NLS-1$
		calibrationStickSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, calibrationStickSubPanelBorder));
		markStickEndsButton = new JRadioButton();
		markStickEndsButton.setOpaque(false);
		markStickEndsButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(markStickEndsButton);
		centerStickButton = new JRadioButton();
		centerStickButton.setOpaque(false);
		centerStickButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
		buttonGroup.add(centerStickButton);
		if (Tracker.centerCalibrationStick)
			centerStickButton.setSelected(true);
		else
			markStickEndsButton.setSelected(true);
		calibrationStickSubPanel.add(markStickEndsButton);
		calibrationStickSubPanel.add(centerStickButton);
		ActionListener calStickAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.centerCalibrationStick = centerStickButton.isSelected();
			}
		};
		markStickEndsButton.addActionListener(calStickAction);
		centerStickButton.addActionListener(calStickAction);

		// data gaps subpanel
		JPanel dataGapSubPanel = new JPanel();
		box.add(dataGapSubPanel);
		dataGapSubPanel.setBackground(color);
		dataGapSubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.DataGap.BorderTitle")); //$NON-NLS-1$
		dataGapSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, dataGapSubPanelBorder));
		
		showGapsCheckbox = new JCheckBox();
		showGapsCheckbox.setOpaque(false);
		showGapsCheckbox.setSelected(Tracker.showGaps);
		showGapsCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.showGaps = showGapsCheckbox.isSelected();
			}
		});
		dataGapSubPanel.add(showGapsCheckbox);

		autofillCheckbox = new JCheckBox();
		autofillCheckbox.setOpaque(false);
		autofillCheckbox.setSelected(Tracker.enableAutofill);
		autofillCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.enableAutofill = autofillCheckbox.isSelected();
				if (panelID != null) {
					TFrame.repaintT(frame.getTrackerPanelForID(panelID));
				}
			}
		});
		dataGapSubPanel.add(autofillCheckbox);
		
		// footprint and trail length subpanels side by side in horz box
		horz = Box.createHorizontalBox();
		box.add(horz);
	
		// pointmass footprint subpanel
		JPanel footprintSubPanel = new JPanel();
		horz.add(footprintSubPanel);
		footprintSubPanel.setBackground(color);
	
		pointmassFootprintSubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.PointMassFootprint.BorderTitle")); //$NON-NLS-1$
		footprintSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, pointmassFootprintSubPanelBorder));
		footprintDropdown = new JComboBox<Footprint>();
		footprintDropdown.setRenderer(new FootprintRenderer());
		Footprint[] footprints = new Footprint[PointMass.footprintNames.length];
		for (int i = 0; i < footprints.length; i++) {
			String name = PointMass.footprintNames[i];
			if (name.equals("CircleFootprint.Circle")) { //$NON-NLS-1$
				footprints[i] = CircleFootprint.getFootprint(name);
			} else {
				footprints[i] = PointShapeFootprint.getFootprint(name);
			}
		}
		for (int i = 0; i < footprints.length; i++) {
			footprintDropdown.addItem(footprints[i]);
		}
		footprintSubPanel.add(footprintDropdown);
		final ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				footprintDropdown.repaint();
			}
		};
		footprintDropdown.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (refreshing)
					return;
				Footprint footprint = (Footprint) footprintDropdown.getSelectedItem();
				if (footprint instanceof CircleFootprint) {
					CircleFootprint cfp = (CircleFootprint) footprint;
					cfp.showProperties(frame, al);
					Tracker.preferredPointMassFootprint = footprint.getName() + "#" + cfp.getProperties(); //$NON-NLS-1$
				} else
					Tracker.preferredPointMassFootprint = footprint.getName();
			}
		});
	
		// trailLength subpanel
//		JPanel trailLengthSubPanel = new JPanel();
		// trails subpanel
		JPanel trailLengthSubPanel = new JPanel();
		trailLengthSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, trailLengthSubPanelBorder));				
		horz.add(trailLengthSubPanel);
		trailLengthSubPanel.setBackground(color);
	
		trailLengthSubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.Trails.BorderTitle")); //$NON-NLS-1$
		trailLengthSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, trailLengthSubPanelBorder));
		trailLengthDropdown = new JComboBox<String>();
		trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.NoTrail")); //$NON-NLS-1$
		trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.ShortTrail")); //$NON-NLS-1$
		trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.LongTrail")); //$NON-NLS-1$
		trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.FullTrail")); //$NON-NLS-1$
		trailLengthSubPanel.add(trailLengthDropdown);
		// end actions panel

		// general panel
		generalPanel = new JPanel(new BorderLayout());
		box = Box.createVerticalBox();
		generalPanel.add(box, BorderLayout.CENTER);

//		if (OSPRuntime.isJS || Tracker.testOn) {

		// recent menu subpanel--only for Java
		if (!OSPRuntime.isJS) {
			JPanel recentSubPanel = new JPanel();
			box.add(recentSubPanel);
			recentSubPanel.setBackground(color);
			recentSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.RecentFiles.BorderTitle")); //$NON-NLS-1$
			recentSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, recentSubPanelBorder));
			// create clear recent button
			clearRecentButton = new JButton();
			clearRecentButton.setEnabled(!Tracker.recentFiles.isEmpty());
			clearRecentButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Tracker.recentFiles.clear();
					if (panelID != null)
						frame.refreshMenus(frame.getTrackerPanelForID(panelID), TMenuBar.REFRESH_PREFS_CLEARRECENT);
					clearRecentButton.setEnabled(false);
				}
			});
			recentSubPanel.add(clearRecentButton);

			// create recent size spinner
			JPanel spinnerPanel = new JPanel();
			spinnerPanel.setOpaque(false);
			spinnerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
			SpinnerModel model = new SpinnerNumberModel(Tracker.recentFilesSize, 0, 12, 1);
			recentSizeSpinner = new JSpinner(model);
			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(recentSizeSpinner, "0"); //$NON-NLS-1$
			editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
			recentSizeSpinner.setEditor(editor);
			spinnerPanel.add(recentSizeSpinner);
			recentSizeLabel = new JLabel();
			spinnerPanel.add(recentSizeLabel);
			recentSubPanel.add(spinnerPanel);
		}

		// cache subpanel--only for Java
		if (!OSPRuntime.isJS) {
			JPanel cacheSubPanel = new JPanel(new BorderLayout());
			box.add(cacheSubPanel);
			cacheSubPanel.setBackground(color);
			cacheSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.CacheFiles.BorderTitle")); //$NON-NLS-1$
			cacheSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, cacheSubPanelBorder));

			// cacheNorthPanel: label, field and browse cache button
			JPanel cacheNorthPanel = new JPanel();
			cacheNorthPanel.setBackground(color);
			cacheLabel = new JLabel();
			cacheNorthPanel.add(cacheLabel);
			cacheField = new JTextField(27);
			cacheNorthPanel.add(cacheField);
			cacheField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					cacheField.setBackground(Color.yellow);
				}
			});
			cacheField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if (cacheField.getBackground() == Color.yellow)
						setCache(null);
				}
			});
			cacheField.addActionListener((e) -> {
					setCache(null);
				});

			browseCacheButton = new TButton(openFileIcon);
			browseCacheButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					File cache = ResourceLoader.getOSPCache();
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.open(cache);
					} catch (IOException ex) {
					}
				}
			});
			browseCacheButton.setBorder(buttonBorder);
			browseCacheButton.setContentAreaFilled(false);
			cacheNorthPanel.add(browseCacheButton);
			cacheSubPanel.add(cacheNorthPanel, BorderLayout.NORTH);

			// cacheSouthPanel: clear cache button and set cache button
			JPanel cacheSouthPanel = new JPanel();
			cacheSouthPanel.setBackground(color);
			clearHostButton = new JButton();
			clearHostButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// get list of host directories
					File cache = ResourceLoader.getOSPCache();
					if (cache == null)
						return;
					final File[] hosts = cache.listFiles(ResourceLoader.OSP_CACHE_FILTER);

					// make popup menu with items to clear individual hosts
					JPopupMenu popup = new JPopupMenu();
					ActionListener clearAction = new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for (File host : hosts) {
								if (host.getAbsolutePath().equals(e.getActionCommand())) {
									ResourceLoader.clearOSPCacheHost(host);
									refreshTextFields();
									return;
								}
							}
						}
					};
					for (File next : hosts) {
						String host = next.getName().substring(4).replace('_', '.');
						long bytes = getFileSize(next);
						long size = bytes / (1024 * 1024);
						if (bytes > 0) {
							if (size > 0)
								host += " (" + size + " MB)"; //$NON-NLS-1$ //$NON-NLS-2$
							else
								host += " (" + bytes / 1024 + " kB)"; //$NON-NLS-1$ //$NON-NLS-2$
						}
						JMenuItem item = new JMenuItem(host);
						item.setActionCommand(next.getAbsolutePath());
						popup.add(item);
						item.addActionListener(clearAction);
					}
					FontSizer.setFonts(popup, FontSizer.getLevel());
					popup.show(clearHostButton, 0, clearHostButton.getHeight());
				}
			});
			cacheSouthPanel.add(clearHostButton);

			clearCacheButton = new JButton();
			clearCacheButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					File cache = ResourceLoader.getOSPCache();
					ResourceLoader.clearOSPCache(cache, false);
					refreshTextFields();
				}
			});
			cacheSouthPanel.add(clearCacheButton);

			setCacheButton = new JButton();
			setCacheButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					File newCache = ResourceLoader.chooseOSPCache(frame);
					if (newCache != null) {
						setCache(newCache.getPath());
					}
				}
			});
			cacheSouthPanel.add(setCacheButton);

			cacheSubPanel.add(cacheSouthPanel, BorderLayout.SOUTH);
		} // end cache subpanel

		// log level subpanel
		logLevelDropdown = new JComboBox<String>();
		String defaultLevel = TrackerRes.getString("PrefsDialog.Version.Default").toUpperCase(); //$NON-NLS-1$
		defaultLevel += " (" + Tracker.DEFAULT_LOG_LEVEL.toString().toLowerCase() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		String selected = defaultLevel;
		logLevelDropdown.addItem(defaultLevel);
		for (int i = OSPLog.levels.length - 1; i >= 0; i--) {
			String s = OSPLog.levels[i].toString();
			logLevelDropdown.addItem(s);
			if (OSPLog.levels[i].equals(Tracker.preferredLogLevel)
					&& !Tracker.preferredLogLevel.equals(Tracker.DEFAULT_LOG_LEVEL)) {
				selected = s;
			}
		}
		logLevelDropdown.setSelectedItem(selected);
		logLevelDropdown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				String s = logLevelDropdown.getSelectedItem().toString();
				Level level = OSPLog.parseLevel(s);
				if (level == null)
					level = Tracker.DEFAULT_LOG_LEVEL;
				Tracker.preferredLogLevel = level;
			}
		});
		JPanel logLevelSubPanel = new JPanel();
		box.add(logLevelSubPanel);
		logLevelSubPanel.setBackground(color);
		logLevelSubPanelBorder = BorderFactory
				.createTitledBorder(TrackerRes.getString("PrefsDialog.LogLevel.BorderTitle")); //$NON-NLS-1$
		logLevelSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, logLevelSubPanelBorder));
		logLevelSubPanel.add(logLevelDropdown);

		// check for upgrades subpane--only for Java
		if (!OSPRuntime.isJS) {
			checkForUpgradeButton = new JButton();
			checkForUpgradeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Tracker.showUpgradeStatus(frame.getTrackerPanelForID(panelID));
				}
			});
			checkForUpgradeDropdown = new JComboBox<String>();
			selected = null;
			for (String next : Tracker.checkForUpgradeChoices) {
				String s = TrackerRes.getString(next);
				checkForUpgradeDropdown.addItem(s);
				if (Tracker.checkForUpgradeIntervals.get(next).equals(Tracker.checkForUpgradeInterval)) {
					selected = s;
				}
			}
			checkForUpgradeDropdown.setSelectedItem(selected);
			checkForUpgradeDropdown.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					String s = checkForUpgradeDropdown.getSelectedItem().toString();
					for (String next : Tracker.checkForUpgradeChoices) {
						if (s.equals(TrackerRes.getString(next))) {
							Tracker.checkForUpgradeInterval = Tracker.checkForUpgradeIntervals.get(next);
							break;
						}
					}
				}
			});
			JPanel dropdownPanel = new JPanel();
			dropdownPanel.setOpaque(false);
			dropdownPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
			JPanel upgradeSubPanel = new JPanel();
			box.add(upgradeSubPanel);
			upgradeSubPanel.setBackground(color);
			upgradeSubPanelBorder = BorderFactory
					.createTitledBorder(TrackerRes.getString("PrefsDialog.Upgrades.BorderTitle")); //$NON-NLS-1$
			upgradeSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, upgradeSubPanelBorder));
			upgradeSubPanel.add(checkForUpgradeButton);
			dropdownPanel.add(checkForUpgradeDropdown);
			upgradeSubPanel.add(dropdownPanel);
		}

		// add tabs
		if (!OSPRuntime.isJS) {
			tabbedPane.addTab(null, runtimePanel);
		}
		tabbedPane.addTab(null, displayPanel);
		tabbedPane.addTab(null, videoPanel);
		tabbedPane.addTab(null, actionsPanel);
		if (!OSPRuntime.isJS) {
			tabbedPane.addTab(null, generalPanel);
			tabbedPane.addTab(null, configPanel);
		}

		// main button bar
		mainButtonBar = new JPanel();
		mainButtonBar.add(relaunchButton);
		mainButtonBar.add(okButton);
		mainButtonBar.add(cancelButton);
		contentPane.add(mainButtonBar, BorderLayout.SOUTH);

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tabbedPane.getSelectedComponent() == runtimePanel) {
					defaultMemoryCheckbox.setEnabled(!OSPRuntime.isWebStart());
					if (OSPRuntime.isWebStart() && !webStartWarningShown) {
						webStartWarningShown = true;
						JOptionPane.showMessageDialog(PrefsDialog.this,
								TrackerRes.getString("PrefsDialog.Dialog.WebStart.Message"), //$NON-NLS-1$
								TrackerRes.getString("PrefsDialog.Dialog.WebStart.Title"), //$NON-NLS-1$
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});

		// add VM buttons to buttongroups
		buttonGroup = new ButtonGroup();
		buttonGroup.add(vm32Button);
		buttonGroup.add(vm64Button);

		if (!OSPRuntime.isJS) {
			// add engine buttons to buttongroups
			buttonGroup = new ButtonGroup();
			buttonGroup.add(movieEngineButton);
			buttonGroup.add(noEngineButton);

			// enable/disable buttons
			xuggleFastButton.setEnabled(movieEngineButton.isSelected());
			xuggleSlowButton.setEnabled(movieEngineButton.isSelected());
			xuggleErrorCheckbox.setEnabled(movieEngineButton.isSelected());
			if (OSPRuntime.isWindows()) {
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						vm32Button.setEnabled(!JREFinder.getFinder().getJREs(32).isEmpty());
						vm64Button.setEnabled(!JREFinder.getFinder().getJREs(64).isEmpty());
					}
				};
				new Thread(runner).start();
			} else if (OSPRuntime.isLinux()) {
				int bitness = OSPRuntime.getVMBitness();
				vm32Button.setEnabled(bitness == 32);
				vm64Button.setEnabled(bitness == 64);
			} else if (OSPRuntime.isMac()) {
				vm32Button.setEnabled(false);
				vm64Button.setEnabled(true);
			}
		}
		refreshGUI();
	}

	protected void setCache(String path) {
		if (path == null) {
			path = cacheField.getText();
		} else {			
			cacheField.setText(path);
		}
		ResourceLoader.setOSPCache(XML.stripExtension(path));
		refreshTextFields();
	}

	private void savePrevious() {
		prevEnabled.clear();
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			prevEnabled.addAll(trackerPanel.getEnabled());
			trackerPanel.taintEnabled();
		}
		prevLogLevel = Tracker.preferredLogLevel;
		prevMemory = Tracker.preferredMemorySize;
		prevLookFeel = Tracker.lookAndFeel;
		prevRecentCount = Tracker.recentFilesSize;
		prevLocaleName = Tracker.preferredLocale;
		prevFontLevel = Tracker.preferredFontLevel;
		prevFontLevelPlus = Tracker.preferredFontLevelPlus;
		prevHints = Tracker.showHintsByDefault;
		prevRadians = Tracker.isRadians;
		prevDecimalSeparator = Tracker.preferredDecimalSeparator;
		prevFastXuggle = Tracker.isXuggleFast;
		prevJRE = Tracker.preferredJRE;
		prevTrackerJar = Tracker.preferredTrackerJar;
		prevExecutables = Tracker.prelaunchExecutables;
		prevWarnNoVideoEngine = Tracker.warnNoVideoEngine;
		prevWarnXuggleError = Tracker.warnXuggleError;
		prevWarnVariableDuration = Tracker.warnVariableDuration;
		prevMarkAtCurrentFrame = Tracker.markAtCurrentFrame;
		prevCache = ResourceLoader.getOSPCache();
		prevUpgradeInterval = Tracker.checkForUpgradeInterval;
		prevEngine = MovieFactory.getMovieEngineName(false);
		prevZoomMouseWheel = Tracker.scrubMouseWheel;
		prevCenterCalibrationStick = Tracker.centerCalibrationStick;
		prevAutofill = Tracker.enableAutofill;
		prevShowGaps = Tracker.showGaps;
		prevTrailLengthIndex = Tracker.preferredTrailLengthIndex;
		prevPointmassFootprint = Tracker.preferredPointMassFootprint;
	}

	private void revert() {
		if (panelID != null)
			frame.getTrackerPanelForID(panelID).setEnabled(prevEnabled);
		Tracker.preferredPointMassFootprint = prevPointmassFootprint;
		Tracker.preferredMemorySize = prevMemory;
		Tracker.lookAndFeel = prevLookFeel;
		Tracker.recentFilesSize = prevRecentCount;
		Tracker.preferredLogLevel = prevLogLevel;
		Tracker.setPreferredLocale(prevLocaleName);
		Tracker.preferredFontLevel = prevFontLevel;
		Tracker.preferredFontLevelPlus = prevFontLevelPlus;
		Tracker.showHintsByDefault = prevHints;
		Tracker.isRadians = prevRadians;
		Tracker.preferredDecimalSeparator = prevDecimalSeparator;
		Tracker.isXuggleFast = prevFastXuggle;
		Tracker.preferredJRE = prevJRE;
		Tracker.preferredTrackerJar = prevTrackerJar;
		Tracker.prelaunchExecutables = prevExecutables;
		Tracker.warnNoVideoEngine = prevWarnNoVideoEngine;
		Tracker.warnXuggleError = prevWarnXuggleError;
		Tracker.warnVariableDuration = prevWarnVariableDuration;
		Tracker.scrubMouseWheel = prevZoomMouseWheel;
		Tracker.markAtCurrentFrame = prevMarkAtCurrentFrame;
		Tracker.centerCalibrationStick = prevCenterCalibrationStick;
		Tracker.enableAutofill = prevAutofill;
		Tracker.showGaps = prevShowGaps;
		Tracker.preferredTrailLengthIndex = prevTrailLengthIndex;
		ResourceLoader.setOSPCache(prevCache);
		Tracker.checkForUpgradeInterval = prevUpgradeInterval;
		if (!OSPRuntime.isJS) {
			// reset JRE dropdown to initial state
			int vmBitness = OSPRuntime.getVMBitness();
			if (vmBitness == 32) {
				vm32Button.setSelected(true);
			} else {
				vm64Button.setSelected(true);
			}
		}
	}

	/**
	 * Updates the configuration to reflect the current checkbox states.
	 */
	private void updateConfig() {
		if (panelID == null)
			return;
		// get the checkboxes
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		Component[] checkboxes = checkPanel.getComponents();
		for (int i = 0; i < checkboxes.length; i++) {
			JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) checkboxes[i];
			if (checkbox.isSelected())
				trackerPanel.getEnabled().add(checkbox.getText());
			else
				trackerPanel.getEnabled().remove(checkbox.getText());
			trackerPanel.taintEnabled();
		}
	}

	/**
	 * Refreshes the GUI.
	 */
	protected void refreshGUI() {
//    lfSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.LookFeel.BorderTitle")); //$NON-NLS-1$
		unitsSubPanelBorder.setTitle(TrackerRes.getString("TMenuBar.Menu.AngleUnits")); //$NON-NLS-1$
		resetToStep0SubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Marking.BorderTitle")); //$NON-NLS-1$
		dataGapSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.DataGap.BorderTitle")); //$NON-NLS-1$
		trailLengthSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Trails.BorderTitle")); //$NON-NLS-1$
		decimalSeparatorBorder.setTitle(TrackerRes.getString("NumberFormatSetter.TitledBorder.DecimalSeparator.Text")); //$NON-NLS-1$
		defaultDecimalButton.setText(TrackerRes.getString("NumberFormatSetter.Button.DecimalSeparator.Default")); //$NON-NLS-1$
		periodDecimalButton.setText(TrackerRes.getString("NumberFormatSetter.Button.DecimalSeparator.Period")); //$NON-NLS-1$
		commaDecimalButton.setText(TrackerRes.getString("NumberFormatSetter.Button.DecimalSeparator.Comma")); //$NON-NLS-1$
		defaultDecimalButton.setSelected(OSPRuntime.getPreferredDecimalSeparator() == null);
		periodDecimalButton.setSelected(".".equals(OSPRuntime.getPreferredDecimalSeparator())); //$NON-NLS-1$
		commaDecimalButton.setSelected(",".equals(OSPRuntime.getPreferredDecimalSeparator())); //$NON-NLS-1$
		cancelButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
		okButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
		relaunchButton.setText(TrackerRes.getString("PrefsDialog.Button.Relaunch")); //$NON-NLS-1$
		resetToStep0Checkbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.ResetToZero.Text")); //$NON-NLS-1$
		autofillCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.Autofill.Text")); //$NON-NLS-1$
		showGapsCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.ShowGaps.Text")); //$NON-NLS-1$
		radiansButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Radians")); //$NON-NLS-1$
		degreesButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Degrees")); //$NON-NLS-1$
		markStickEndsButton.setText(TrackerRes.getString("PrefsDialog.Button.MarkEnds")); //$NON-NLS-1$
		centerStickButton.setText(TrackerRes.getString("PrefsDialog.Button.Center")); //$NON-NLS-1$
		scrubButton.setText(TrackerRes.getString("PrefsDialog.Button.Scrub")); //$NON-NLS-1$
		zoomButton.setText(TrackerRes.getString("PrefsDialog.Button.Zoom")); //$NON-NLS-1$
		mouseWheelSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Mousewheel.BorderTitle")); //$NON-NLS-1$
		variableDurationCheckBox.setText(TrackerRes.getString("PrefsDialog.Checkbox.WarnVariableDuration")); //$NON-NLS-1$
		setTabTitle(displayPanel, TrackerRes.getString("PrefsDialog.Tab.Display.Title")); //$NON-NLS-1$
		setTabTitle(actionsPanel, TrackerRes.getString("PrefsDialog.Tab.Tracking.Title")); //$NON-NLS-1$
		setTabTitle(videoPanel, TrackerRes.getString("PrefsDialog.Tab.Video.Title")); //$NON-NLS-1$
		if (!OSPRuntime.isJS) {
			setTabTitle(generalPanel, TrackerRes.getString("PrefsDialog.Tab.General.Title")); //$NON-NLS-1$
			hintsCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.HintsOn")); //$NON-NLS-1$
			logLevelSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.LogLevel.BorderTitle")); //$NON-NLS-1$
			fontSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.FontSize.BorderTitle")); //$NON-NLS-1$
			langSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Language.BorderTitle")); //$NON-NLS-1$
			hintsSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Hints.BorderTitle")); //$NON-NLS-1$
			checkPanelBorder.setTitle(TrackerRes.getString("ConfigInspector.Border.Title")); //$NON-NLS-1$
			versionSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Version.BorderTitle")); //$NON-NLS-1$
			jreSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.JRE.BorderTitle")); //$NON-NLS-1$
			memorySubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Memory.BorderTitle")); //$NON-NLS-1$
			recentSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.RecentFiles.BorderTitle")); //$NON-NLS-1$
			defaultMemoryCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.DefaultSize")); //$NON-NLS-1$
			applyButton.setText(TrackerRes.getString("Dialog.Button.Apply")); //$NON-NLS-1$
			applyButton.setEnabled(panelID != null);
			allButton.setText(TrackerRes.getString("Dialog.Button.All")); //$NON-NLS-1$
			noneButton.setText(TrackerRes.getString("Dialog.Button.None")); //$NON-NLS-1$
			cacheLabel.setText(TrackerRes.getString("PrefsDialog.Label.Path") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
			clearCacheButton.setToolTipText(TrackerRes.getString("PrefsDialog.Button.ClearCache.Tooltip")); //$NON-NLS-1$
			clearHostButton.setText(TrackerRes.getString("PrefsDialog.Button.ClearHost")); //$NON-NLS-1$
			clearHostButton.setToolTipText(TrackerRes.getString("PrefsDialog.Button.ClearHost.Tooltip")); //$NON-NLS-1$
			setCacheButton.setText(TrackerRes.getString("PrefsDialog.Button.SetCache")); //$NON-NLS-1$
			saveButton.setText(TrackerRes.getString("ConfigInspector.Button.SaveAsDefault")); //$NON-NLS-1$
			checkForUpgradeButton.setText(TrackerRes.getString("PrefsDialog.Button.CheckForUpgrade")); //$NON-NLS-1$
			vm32Button.setText(TrackerRes.getString("PrefsDialog.Checkbox.32BitVM")); //$NON-NLS-1$
			vm64Button.setText(TrackerRes.getString("PrefsDialog.Checkbox.64BitVM")); //$NON-NLS-1$
			movieEngineButton.setText(TrackerRes.getString("PrefsDialog.Button.Xuggle")); //$NON-NLS-1$
			noEngineButton.setText(TrackerRes.getString("PrefsDialog.Button.NoEngine")); //$NON-NLS-1$
			xuggleFastButton.setText(TrackerRes.getString("PrefsDialog.Xuggle.Fast")); //$NON-NLS-1$
			xuggleSlowButton.setText(TrackerRes.getString("PrefsDialog.Xuggle.Slow")); //$NON-NLS-1$
			vidWarningCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.WarnIfNoEngine")); //$NON-NLS-1$
			xuggleErrorCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.WarnIfXuggleError")); //$NON-NLS-1$
//	    videoTypeSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.VideoPref.BorderTitle")); //$NON-NLS-1$
			xuggleSpeedSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Xuggle.Speed.BorderTitle")); //$NON-NLS-1$
			warningsSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.NoVideoWarning.BorderTitle")); //$NON-NLS-1$
			cacheSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.CacheFiles.BorderTitle")); //$NON-NLS-1$
			upgradeSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Upgrades.BorderTitle")); //$NON-NLS-1$
			runSubPanelBorder.setTitle(TrackerRes.getString("PrefsDialog.Run.BorderTitle")); //$NON-NLS-1$
			clearRecentButton.setText(TrackerRes.getString("PrefsDialog.Button.ClearRecent")); //$NON-NLS-1$
			recentSizeLabel.setText(TrackerRes.getString("PrefsDialog.Label.RecentSize")); //$NON-NLS-1$
			setTabTitle(configPanel, TrackerRes.getString("PrefsDialog.Tab.Configuration.Title")); //$NON-NLS-1$
			setTabTitle(runtimePanel, TrackerRes.getString("PrefsDialog.Tab.Runtime.Title")); //$NON-NLS-1$
			refreshTextFields();
		}

		setFontLevel(FontSizer.getLevel());
		// refresh trail lengths
		if (trailLengthDropdown != null) {
			trailLengthDropdown.removeAllItems();
			trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.NoTrail")); //$NON-NLS-1$
			trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.ShortTrail")); //$NON-NLS-1$
			trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.LongTrail")); //$NON-NLS-1$
			trailLengthDropdown.addItem(TrackerRes.getString("TrackControl.TrailMenu.FullTrail")); //$NON-NLS-1$
		}

		pack();
		updateDisplay();
	}

	private void refreshJREDropdown(final int vmBitness) {
		if (String.valueOf(vmBitness).equals(jreDropdown.getName()))
			return;
		jreDropdown.setName(String.valueOf(vmBitness));
		// refresh JRE dropdown in background thread
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				while (!JREFinder.isReady()) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
				Runnable refresher = new Runnable() {
					@Override
					public void run() {
						// replace items in dropdown
						jreDropdown.removeAllItems();
						JREFinder jreFinder = JREFinder.getFinder();
						Set<File> availableJREs = jreFinder.getJREs(vmBitness);
						ArrayList<String> availableJREPaths = new ArrayList<String>();
						String path = Tracker.trackerHome;
						if (OSPRuntime.isMac()) {
							path = new File(Tracker.trackerHome).getParent() + "/PlugIns/Java.runtime"; //$NON-NLS-1$
						}
						String[] bundledVMs = TrackerStarter.findBundledVMs();
						String bundledVM = vmBitness == 32 && OSPRuntime.isWindows() ? bundledVMs[1] : bundledVMs[0];

						File defaultVM = jreFinder.getDefaultJRE(vmBitness, path, true, null);
						for (File next : availableJREs) {
							String jrePath = next.getPath();
							if (bundledVM != null && jrePath.equals(bundledVM)) {
								availableJREPaths.add(jrePath);
								jreDropdown.insertItemAt(
										TrackerRes.getString("PrefsDialog.JREDropdown.BundledJRE") + " " + jrePath, 0); //$NON-NLS-1$
							} else if (defaultVM != null && jrePath.equals(defaultVM.getPath())
									&& bundledVM ==  null) {
								availableJREPaths.add(jrePath);
								jreDropdown.insertItemAt(TrackerRes.getString("PrefsDialog.JREDropdown.LatestJRE"), 0); //$NON-NLS-1$
								jreDropdown.addItem(jrePath); // duplicate latest
							} else {
								availableJREPaths.add(jrePath);
								jreDropdown.addItem(jrePath);
							}
						}

						// set selected item
						String selectedItem = Tracker.preferredJRE;
						if (selectedItem == null || !availableJREPaths.contains(selectedItem)) {
							if (bundledVM != null) {
								selectedItem = TrackerRes.getString("PrefsDialog.JREDropdown.BundledJRE") + " " //$NON-NLS-1$
										+ bundledVM; // ;
							} else {
								selectedItem = TrackerRes.getString("PrefsDialog.JREDropdown.LatestJRE"); //$NON-NLS-1$ ;
							}
						}
						jreDropdown.setSelectedItem(selectedItem);

						if (vmBitness == 32 && relaunching) {
							// check that not canceled by user
							if (!"cancel".equals(vm32Button.getName())) { //$NON-NLS-1$
								relaunching = false;
								relaunchButton.doClick(0);
							}
						}
					}
				};
				SwingUtilities.invokeLater(refresher);
			}
		};
		new Thread(runner).start();
	}

	private void refreshTextFields() {
		// run field
		int n = (Integer) runSpinner.getValue();
		if (Tracker.prelaunchExecutables.length > n && Tracker.prelaunchExecutables[n] != null) {
			runField.setText(Tracker.prelaunchExecutables[n]);
			runField.setToolTipText(Tracker.prelaunchExecutables[n]);
			runField.setBackground(new File(Tracker.prelaunchExecutables[n]).exists() ? Color.white : MEDIUM_RED);
		} else {
			runField.setText(null);
			runField.setToolTipText(null);
			runField.setBackground(Color.white);
		}

		// cache field and button
		String s = TrackerRes.getString("PrefsDialog.Button.ClearCache"); //$NON-NLS-1$
		File cache = ResourceLoader.getOSPCache();
		if (cache != null) {
			cacheField.setText(cache.getPath());
			cacheField.setToolTipText(cache.getAbsolutePath());
			cacheField.setBackground(cache.canWrite() ? Color.white : MEDIUM_RED);
			long bytes = getFileSize(cache);
			long size = bytes / (1024 * 1024);
			if (bytes > 0) {
				if (size > 0)
					s += " (" + size + " MB)"; //$NON-NLS-1$ //$NON-NLS-2$
				else
					s += " (" + bytes / 1024 + " kB)"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			cacheField.setText(""); //$NON-NLS-1$
			cacheField.setToolTipText(""); //$NON-NLS-1$
			cacheField.setBackground(MEDIUM_RED);
		}
		clearCacheButton.setText(s);
		boolean isEmpty = cache == null || !cache.exists()
				|| cache.listFiles(ResourceLoader.OSP_CACHE_FILTER).length == 0;
		clearCacheButton.setEnabled(!isEmpty);
		clearHostButton.setEnabled(!isEmpty);
	}

	/**
	 * Applies and saves the current preferences.
	 */
	private void applyPrefs() {
		// look/feel, language, video, hints, font size & decimal separator are set
		// directly by components
		
		TrackerPanel trackerPanel = panelID==null? null:
			frame.getTrackerPanelForID(panelID);
		Tracker.showGaps = showGapsCheckbox.isSelected();
		if (trailLengthDropdown != null) {
			int index = trailLengthDropdown.getSelectedIndex();
			if (index != Tracker.preferredTrailLengthIndex) {
				Tracker.preferredTrailLengthIndex = index;
				// refresh the toolbar
				if (trackerPanel != null) {
					TToolBar toolbar = trackerPanel.getToolBar(true);
					toolbar.trailLengthIndex = Tracker.preferredTrailLengthIndex;
					toolbar.trailButton.setSelected(toolbar.trailLengthIndex != 0);
					toolbar.refresh(TToolBar.REFRESH_PREFS_TRUE);
				}
			}
		}
		Tracker.isRadians = radiansButton.isSelected();
		if (frame != null)
			frame.setAnglesInRadians(Tracker.isRadians);
		if (!OSPRuntime.isJS) {
			// update recent menu
			Integer val = (Integer) recentSizeSpinner.getValue();
			Tracker.setRecentSize(val);
			if (trackerPanel != null)
				trackerPanel.refreshMenus(TMenuBar.REFRESH_PREFS_APPLYPREFS);
			// update configuration
			updateConfig();
			Tracker.isXuggleFast = xuggleFastButton.isSelected();
			// update preferred memory size
			if (defaultMemoryCheckbox.isSelected())
				Tracker.preferredMemorySize = -1;
			else
				Tracker.preferredMemorySize = memoryField.getIntValue();
			// update preferred JRE
			Object selected = jreDropdown.getSelectedItem();
			if (selected != null && !selected.equals(Tracker.preferredJRE)) {
				if (selected.toString().startsWith(TrackerRes.getString("PrefsDialog.JREDropdown.BundledJRE"))) { //$NON-NLS-1$
					Tracker.preferredJRE = null;
				} else if (selected.equals(TrackerRes.getString("PrefsDialog.JREDropdown.LatestJRE"))) { //$NON-NLS-1$
					Tracker.preferredJRE = null;
				} else {
					Tracker.preferredJRE = selected.toString();
				}
			}

		}
		// save the tracker and tracker_starter preferences
		String path = Tracker.savePreferences();
		if (path != null)
			OSPLog.info("saved tracker preferences in " + XML.getAbsolutePath(new File(path))); //$NON-NLS-1$
	}

	/**
	 * Saves the current configuration as the default.
	 */
	private void saveConfigAsDefault() {
		Component[] checkboxes = checkPanel.getComponents();
		Set<String> enabled = new TreeSet<String>();
		for (int i = 0; i < checkboxes.length; i++) {
			JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) checkboxes[i];
			if (checkbox.isSelected())
				enabled.add(checkbox.getText());
		}
//    applyButton.doClick(0);
		Tracker.setDefaultConfig(enabled);
	}

	/**
	 * Updates this dialog to show the TrackerPanel's current preferences.
	 */
	protected void updateDisplay() {
		refreshing = true;
		// look and feel
//		if (Tracker.lookAndFeel != null)
//			lookFeelDropdown.setSelectedItem(Tracker.lookAndFeel.toLowerCase());

		// footprint dropdown
		if (footprintDropdown != null) {
			int selected = 0;
			for (int i = 0; i < footprintDropdown.getItemCount(); i++) {
				Footprint footprint = footprintDropdown.getItemAt(i);
				if (Tracker.preferredPointMassFootprint != null
						&& Tracker.preferredPointMassFootprint.startsWith(footprint.getName())) {
					selected = i;
					if (footprint instanceof CircleFootprint) {
						CircleFootprint cfp = (CircleFootprint) footprint;
						int n = Tracker.preferredPointMassFootprint.indexOf("#"); //$NON-NLS-1$
						if (n > -1) {
							cfp.setProperties(Tracker.preferredPointMassFootprint.substring(n + 1));
						}
					}
					break;
				}
			}
			if (footprintDropdown.getItemCount() > selected) {
				footprintDropdown.setSelectedIndex(selected);
			}
		}

		int preferredLevel = Tracker.preferredFontLevel + Tracker.preferredFontLevelPlus;
		fontSizeDropdown.setSelectedIndex(preferredLevel);
		
		// trail length
		if (trailLengthDropdown != null)
			trailLengthDropdown.setSelectedIndex(Tracker.preferredTrailLengthIndex);

		// show gaps
		showGapsCheckbox.setSelected(Tracker.showGaps);

		// autofill
		autofillCheckbox.setSelected(Tracker.enableAutofill);

		// angle units
		radiansButton.setSelected(Tracker.isRadians);
		degreesButton.setSelected(!Tracker.isRadians);

		// new tracks reset to 0
		resetToStep0Checkbox.setSelected(!Tracker.markAtCurrentFrame);

		// mousewheel action
		if (Tracker.scrubMouseWheel)
			scrubButton.setSelected(true);
		else
			zoomButton.setSelected(true);

		// new calibration sticks
		if (Tracker.centerCalibrationStick)
			centerStickButton.setSelected(true);
		else
			markStickEndsButton.setSelected(true);

		if (!OSPRuntime.isJS) {
			// hints
			hintsCheckbox.setSelected(Tracker.showHintsByDefault);
			// locale
			int index = 0;
			Locale[] locales = Tracker.getLocales();
			for (int i = 0; i < locales.length; i++) {
				Locale next = locales[i];
				if (next.equals(Locale.getDefault())) {
					index = i + 1;
					break;
				}
			}
			languageDropdown.setSelectedIndex(index);

			// log level
			int selected = 0;
			if (!Tracker.preferredLogLevel.equals(Tracker.DEFAULT_LOG_LEVEL)) {
				for (int i = 1, count = logLevelDropdown.getItemCount(); i < count; i++) {
					String next = logLevelDropdown.getItemAt(i).toString();
					if (Tracker.preferredLogLevel.toString().equals(next)) {
						selected = i;
						break;
					}
				}
			}
			if (logLevelDropdown.getItemCount() > selected) {
				logLevelDropdown.setSelectedIndex(selected);
			}

			// recent files
			recentSizeSpinner.setValue(Tracker.recentFilesSize);
			// configuration
			Component[] checkboxes = checkPanel.getComponents();
			Set<String> enabled = (panelID == null ? Tracker.getDefaultConfig()
					: frame.getTrackerPanelForID(panelID).getEnabled());
			for (int i = 0; i < checkboxes.length; i++) {
				// check the checkbox if its text is in the current config
				JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem) checkboxes[i];
				checkbox.setSelected(enabled.contains(checkbox.getText()));
			}

			// warnings
			vidWarningCheckbox.setSelected(Tracker.warnNoVideoEngine);
			variableDurationCheckBox.setSelected(Tracker.warnVariableDuration);
			xuggleErrorCheckbox.setSelected(Tracker.warnXuggleError);

			// memory size
			defaultMemoryCheckbox.setSelected(Tracker.preferredMemorySize < 0);
			memoryField.setEnabled(Tracker.preferredMemorySize >= 0);
			memoryLabel.setEnabled(Tracker.preferredMemorySize >= 0);
			if (Tracker.preferredMemorySize >= 0)
				memoryField.setValue(Tracker.preferredMemorySize);
			else {
				memoryField.setText(null);
			}
			// tracker jar
			selected = 0;
			for (int i = 0, count = versionDropdown.getItemCount(); i < count; i++) {
				String next = versionDropdown.getItemAt(i).toString();
				if (Tracker.preferredTrackerJar != null && Tracker.preferredTrackerJar.indexOf(next) > -1 &&
				// distinguish tracker-5.9.2.jar from tracker-5.9.20210507
						(Tracker.preferredTrackerJar.indexOf(".jar")
								- Tracker.preferredTrackerJar.indexOf(next)) == next.length()) {
					selected = i;
					break;
				}
			}
			if (versionDropdown.getItemCount() > selected) {
				versionDropdown.setSelectedIndex(selected);
			}

			// JRE dropdown
			selected = 0;
			for (int i = 0, count = jreDropdown.getItemCount(); i < count; i++) {
				String next = jreDropdown.getItemAt(i).toString();
				if (next.equals(Tracker.preferredJRE)) {
					selected = i;
					break;
				}
			}
			if (jreDropdown.getItemCount() > selected) {
				jreDropdown.setSelectedIndex(selected);
			}

			// video
			if (MovieFactory.hasVideoEngine()) {
				movieEngineButton.setSelected(true);
			}

			// checkForUpgrade
			selected = 0;
			for (int i = 1, count = Tracker.checkForUpgradeChoices.size(); i < count; i++) {
				String next = Tracker.checkForUpgradeChoices.get(i);
				if (Tracker.checkForUpgradeIntervals.get(next) == Tracker.checkForUpgradeInterval) {
					selected = i;
					break;
				}
			}
			if (checkForUpgradeDropdown.getItemCount() > selected) {
				checkForUpgradeDropdown.setSelectedIndex(selected);
			}

		}
		TFrame.repaintT(this);
		refreshing = false;
	}

	/**
	 * Sets the title of a specified tab.
	 */
	private void setTabTitle(JPanel tab, String title) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getComponentAt(i) == tab)
				tabbedPane.setTitleAt(i, title);
		}
	}

	/**
	 * Relaunches after changing preferred VM to 64-bit
	 */
	public void relaunch64Bit() {
		relaunching = true;
		vm64Button.setSelected(true); // also sets default video engine??
	}

	/**
	 * Gets the total size of a folder.
	 * 
	 * @param folder the folder
	 * @return the size in bytes
	 */
	private long getFileSize(File folder) {
		if (folder == null)
			return 0;
		long foldersize = 0;
		File cache = ResourceLoader.getOSPCache();
		File[] files = folder.equals(cache) ? folder.listFiles(ResourceLoader.OSP_CACHE_FILTER) : folder.listFiles();
		if (files == null)
			return 0;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				foldersize += getFileSize(files[i]);
			} else {
				foldersize += files[i].length();
			}
		}
		return foldersize;
	}

	/**
	 * Gets a file chooser.
	 * 
	 * @param file         the initial file to select
	 * @param useJREFilter true if setting JRE
	 * @return the file chooser
	 */
	protected static JFileChooser getFileChooser(File file, boolean useJREFilter) {
		JFileChooser chooser = new JFileChooser(file);
		if (useJREFilter) {
			FileFilter folderFilter = new FileFilter() {
				// accept directories or "jre/jdk" files only
				@Override
				public boolean accept(File f) {
					if (f == null)
						return false;
					if (f.isDirectory())
						return true;
					if (f.getPath().indexOf("jre") > -1) //$NON-NLS-1$
						return true;
					if (f.getPath().indexOf("jdk") > -1) //$NON-NLS-1$
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					return TrackerRes.getString("PrefsDialog.FileFilter.JRE"); //$NON-NLS-1$
				}
			};
			if (OSPRuntime.isMac())
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			else
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.addChoosableFileFilter(folderFilter);
		}
		FontSizer.setFonts(chooser, FontSizer.getLevel());
		return chooser;
	}

	/**
	 * A class to render footprints for a dropdown
	 */
	class FootprintRenderer extends JLabel implements ListCellRenderer<Footprint> {

		FootprintRenderer() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 0));
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Footprint> list, Footprint val, int index,
				boolean selected, boolean hasFocus) {

			if (selected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			if (val != null) {
				Footprint fp = (Footprint) val;
				String name = fp.getDisplayName();
				if (fp instanceof CircleFootprint) {
					CircleFootprint cfp = (CircleFootprint) fp;
					String[] props = cfp.getProperties().split(" "); //$NON-NLS-1$
					name += " r=" + props[0]; //$NON-NLS-1$
				}
				setText(name);
				setIcon(fp.getIcon(21, 16));
			}
			return this;
		}

	}

}
