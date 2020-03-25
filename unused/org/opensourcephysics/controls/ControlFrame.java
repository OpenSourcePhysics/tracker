/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.PrintUtils;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ToolsRes;

import javajs.async.AsyncFileChooser;

/**
 * A frame with menu items for saving and loading control parameters
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
abstract public class ControlFrame extends OSPFrame implements Control {
	final static int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	protected Object model; // the object that will be controlled
	protected JMenuItem[] languageItems;
	protected JMenu languageMenu;
	protected JMenu fileMenu;
	protected JMenu editMenu;
	protected JMenu displayMenu;
	protected JMenuItem readItem, clearItem, printFrameItem, saveFrameAsEPSItem;
	protected JMenuItem saveAsItem;
	protected JMenuItem copyItem;
	protected JMenuItem inspectItem;
	protected JMenuItem sizeUpItem;
	protected JMenuItem sizeDownItem;
	protected OSPApplication ospApp;
	protected XMLControlElement xmlDefault;

	protected ControlFrame(String title) {
		super(title);
		createMenuBar();
		setName("controlFrame"); //$NON-NLS-1$
	}

	private void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		if (!OSPRuntime.appletMode) {
			setJMenuBar(menuBar);
		}
		fileMenu = new JMenu(ControlsRes.getString("ControlFrame.File")); //$NON-NLS-1$
		editMenu = new JMenu(ControlsRes.getString("ControlFrame.Edit")); //$NON-NLS-1$
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		readItem = new JMenuItem(ControlsRes.getString("ControlFrame.Load_XML")); //$NON-NLS-1$
		saveAsItem = new JMenuItem(ControlsRes.getString("ControlFrame.Save_XML")); //$NON-NLS-1$
		inspectItem = new JMenuItem(ControlsRes.getString("ControlFrame.Inspect_XML")); //$NON-NLS-1$
		clearItem = new JMenuItem(ControlsRes.getString("ControlFrame.Clear_XML")); //$NON-NLS-1$
		clearItem.setEnabled(false);
		copyItem = new JMenuItem(ControlsRes.getString("ControlFrame.Copy")); //$NON-NLS-1$
		printFrameItem = new JMenuItem(DisplayRes.getString("DrawingFrame.PrintFrame_menu_item")); //$NON-NLS-1$
		saveFrameAsEPSItem = new JMenuItem(DisplayRes.getString("DrawingFrame.SaveFrameAsEPS_menu_item")); //$NON-NLS-1$
		JMenu printMenu = new JMenu(DisplayRes.getString("DrawingFrame.Print_menu_title")); //$NON-NLS-1$
		if (OSPRuntime.applet == null) {
			fileMenu.add(readItem);
		}
		if (OSPRuntime.applet == null) {
			fileMenu.add(saveAsItem);
		}
		fileMenu.add(inspectItem);
		fileMenu.add(clearItem);
		if (OSPRuntime.applet == null) {
			fileMenu.add(printMenu);
		}
		printMenu.add(printFrameItem);
		printMenu.add(saveFrameAsEPSItem);
		editMenu.add(copyItem);
		copyItem.setAccelerator(KeyStroke.getKeyStroke('C', MENU_SHORTCUT_KEY_MASK));
		copyItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copy();
			}

		});
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke('S', MENU_SHORTCUT_KEY_MASK));
		saveAsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveXML();
			}

		});
		inspectItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inspectXML(); // cannot use a static method here because of run-time binding
			}

		});
		readItem.setAccelerator(KeyStroke.getKeyStroke('L', MENU_SHORTCUT_KEY_MASK));
		readItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// readParameters(); // cannot use a static method here because of run-time
				// binding
				loadXML((String) null);
			}

		});
		clearItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((xmlDefault == null) || (model == null)) {
					return;
				}
				xmlDefault = null;
				clearItem.setEnabled(false);
				if (model instanceof Calculation) {
					((Calculation) model).resetCalculation();
					((Calculation) model).calculate();
				} else if (model instanceof Animation) {
					((Animation) model).stopAnimation();
					((Animation) model).resetAnimation();
					((Animation) model).initializeAnimation();
				}
				GUIUtils.repaintOSPFrames();
			}

		});
		// print item
		printFrameItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PrintUtils.printComponent(ControlFrame.this);
			}

		});
		// save as EPS item
		saveFrameAsEPSItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PrintUtils.saveComponentAsEPS(ControlFrame.this);
				} catch (IOException ex) {
				}
			}

		});
		// display menu
		loadDisplayMenu();
		// help menu
		JMenu helpMenu = new JMenu(ControlsRes.getString("ControlFrame.Help")); //$NON-NLS-1$
		menuBar.add(helpMenu);
		JMenuItem aboutItem = new JMenuItem(ControlsRes.getString("ControlFrame.About")); //$NON-NLS-1$
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OSPRuntime.showAboutDialog(ControlFrame.this);
			}

		});
		helpMenu.add(aboutItem);
		JMenuItem sysItem = new JMenuItem(ControlsRes.getString("ControlFrame.System")); //$NON-NLS-1$
		sysItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ControlUtils.showSystemProperties(true);
			}

		});
		helpMenu.add(sysItem);
		JMenuItem showItem = new JMenuItem(ControlsRes.getString("ControlFrame.Display_All_Frames")); //$NON-NLS-1$
		showItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				org.opensourcephysics.display.GUIUtils.showDrawingAndTableFrames();
			}

		});
		helpMenu.add(showItem);
		helpMenu.addSeparator();
		JMenuItem logItem = new JMenuItem(ControlsRes.getString("ControlFrame.Message_Log")); //$NON-NLS-1$
		logItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OSPLog.showLog();
			}

		});

		helpMenu.add(logItem);
		validate();
	}

	/**
	 * Adds a Display menu to the menu bar. Overrides OSPFrame method.
	 *
	 * @return the display menu
	 */
	protected JMenu loadDisplayMenu() {
		JMenuBar menuBar = getJMenuBar();
		if (menuBar == null) {
			return null;
		}
		displayMenu = super.loadDisplayMenu();
		if (displayMenu == null) {
			displayMenu = new JMenu();
			displayMenu.setText(ControlsRes.getString("ControlFrame.Display")); //$NON-NLS-1$
			menuBar.add(displayMenu);
		}
		// language menu
		languageMenu = new JMenu();
		languageMenu.setText(ControlsRes.getString("ControlFrame.Language")); //$NON-NLS-1$
		Action languageAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				String language = e.getActionCommand();
				OSPLog.finest("setting language to " + language); //$NON-NLS-1$
				Locale[] locales = OSPRuntime.getInstalledLocales();
				for (int i = 0; i < locales.length; i++) {
					if (language.equals(locales[i].getDisplayName())) {
						ToolsRes.setLocale(locales[i]);
						return;
					}
				}
			}

		};
		Locale[] locales = OSPRuntime.getInstalledLocales();
		ButtonGroup languageGroup = new ButtonGroup();
		languageItems = new JMenuItem[locales.length];
		for (int i = 0; i < locales.length; i++) {
			languageItems[i] = new JRadioButtonMenuItem(locales[i].getDisplayName(locales[i]));
			languageItems[i].setActionCommand(locales[i].getDisplayName());
			languageItems[i].addActionListener(languageAction);
			languageMenu.add(languageItems[i]);
			languageGroup.add(languageItems[i]);
		}
		for (int i = 0; i < locales.length; i++) {
			if (locales[i].getLanguage().equals(ToolsRes.getLanguage())) {
				languageItems[i].setSelected(true);
			}
		}
		if (OSPRuntime.isAuthorMode() || !OSPRuntime.isLauncherMode()) {
			displayMenu.add(languageMenu); // add the menu if program is stand-alone or if user is authoring.
		}
		// font menu
		JMenu fontMenu = new JMenu(DisplayRes.getString("DrawingFrame.Font_menu_title")); //$NON-NLS-1$
		displayMenu.add(fontMenu);
		JMenuItem sizeUpItem = new JMenuItem(ControlsRes.getString("ControlFrame.Increase_Font_Size")); //$NON-NLS-1$
		sizeUpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontSizer.levelUp();
			}

		});
		fontMenu.add(sizeUpItem);
		final JMenuItem sizeDownItem = new JMenuItem(ControlsRes.getString("ControlFrame.Decrease_Font_Size")); //$NON-NLS-1$
		sizeDownItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FontSizer.levelDown();
			}

		});
		fontMenu.add(sizeDownItem);
		fontMenu.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sizeDownItem.setEnabled(FontSizer.getLevel() > 0);
			}

		});
		return displayMenu;
	}

	/**
	 * Refreshes the user interface in response to display changes such as Language.
	 */
	protected void refreshGUI() {
		super.refreshGUI();
		createMenuBar();
	}

	/** Saves a file containing the control parameters to the disk. */
	public void save() {
		ControlUtils.saveToFile(this, ControlFrame.this);
	}

	/** Loads a file containing the control parameters from the disk. */
	public void readParameters() {
		ControlUtils.loadParameters(this, ControlFrame.this);
	}

	/** Copies the data in the table to the system clipboard */
	public void copy() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(this.toString());
		clipboard.setContents(stringSelection, stringSelection);
	}

	public void saveXML() {
		JFileChooser chooser = OSPRuntime.getChooser();
		if (chooser == null) {
			return;
		}
		String oldTitle = chooser.getDialogTitle();
		chooser.setDialogTitle(ControlsRes.getString("ControlFrame.Save_XML_Data")); //$NON-NLS-1$
		chooser.setDialogTitle(oldTitle);
		// int result = chooser.showSaveDialog(null);
		int result = -1;
		try {
			result = chooser.showSaveDialog(null);
		} catch (Throwable e) {
			System.err.println("InterruptedException in saveXML()().");
			e.printStackTrace();
		}
		chooser.setDialogTitle(oldTitle);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			// check to see if file already exists
			org.opensourcephysics.display.OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
			String fileName = file.getAbsolutePath();
			// String fileName = XML.getRelativePath(file.getAbsolutePath());
			if ((fileName == null) || fileName.trim().equals("")) { //$NON-NLS-1$
				return;
			}
			int i = fileName.toLowerCase().lastIndexOf(".xml"); //$NON-NLS-1$
			if (i != fileName.length() - 4) {
				fileName += ".xml"; //$NON-NLS-1$
				file = new File(fileName);
			}
			if (/** @j2sNative false && */
			file.exists()) {
				int selected = JOptionPane.showConfirmDialog(null, "Replace existing " + file.getName() + "?",
						"Replace File", JOptionPane.YES_NO_CANCEL_OPTION);
				if (selected != JOptionPane.YES_OPTION) {
					return;
				}
			}
			XMLControl xml = new XMLControlElement(getOSPApp());
			xml.write(fileName);
		}
	}

	public void loadXML(String[] args) {
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				loadXML(args[i]);
			}
		}
	}

	/**
	 * Loads xml data into the model.
	 *
	 * @param xml
	 * @param compatibleModel true if model is known to be compatible with the app
	 */
	public void loadXML(XMLControlElement xml, boolean compatibleModel) {
		if (xml == null) {
			OSPLog.finer("XML data not found in ControlFrame loadXML method."); //$NON-NLS-1$
			return;
		}
		// load xml into app if xml object class is an OSPApplication
		// if(xml.getObjectClass().isAssignableFrom(OSPApplication.class)) {
		if (OSPApplication.class.isAssignableFrom(xml.getObjectClass())) {
			ospApp = getOSPApp();
			ospApp.compatibleModel = compatibleModel;
			xml.loadObject(getOSPApp());
			ospApp.compatibleModel = false;
			if (model.getClass() == ospApp.getLoadedModelClass()) { // classes match so we have a new default
				xmlDefault = xml;
				clearItem.setEnabled(true);
			} else if (ospApp.getLoadedModelClass() != null) { // a model was imported; create xml from current model
				xmlDefault = new XMLControlElement(getOSPApp());
				clearItem.setEnabled(true);
			} else { // model is null
				xmlDefault = null;
				clearItem.setEnabled(false);
				JOptionPane.showMessageDialog(this, "Model specified in file not found.", //$NON-NLS-1$
						"Data not loaded.", JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			}
		} else {
			JOptionPane.showMessageDialog(this, "Data for: " + xml.getObjectClass() + ".", //$NON-NLS-1$ //$NON-NLS-2$
					"OSP Application data not found.", JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
		}
	}

	public void loadXML(String fileName) {
		if ((fileName == null) || fileName.trim().equals("")) { //$NON-NLS-1$
			loadXML();
			return;
		}
		loadXML(new XMLControlElement(fileName), false);
	}

	public void loadXML() {
		AsyncFileChooser fc = OSPRuntime.getChooser(); // static Chooser
		if (fc == null)
			return;
		String oldTitle = fc.getDialogTitle();
		fc.showOpenDialog(ControlFrame.this, new Runnable() {

			@Override
			public void run() {
				fc.setDialogTitle(oldTitle);
				File file = fc.getSelectedFile();
				if (file == null)
					return;
				String fileName = fc.getSelectedFile().getAbsolutePath();
				loadXML(new XMLControlElement(fileName), false);
			}

		}, null);
	}

	public void inspectXML() {
		// display a TreePanel in a modal dialog
		XMLControl xml = new XMLControlElement(getOSPApp());
		JDialog dialog = new JDialog((java.awt.Frame) null, true);
		dialog.setTitle("XML Inspector");
		XMLTreePanel treePanel = new XMLTreePanel(xml);
		dialog.setContentPane(treePanel);
		dialog.setSize(new Dimension(600, 300));
		dialog.setVisible(true);
	}

	/**
	 * Gets the OSP Application that is controlled by this frame.
	 * 
	 * @return
	 */
	public OSPApplication getOSPApp() {
		if (ospApp == null) {
			ospApp = new OSPApplication(this, model);
		}
		return ospApp;
	}

}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017 The Open Source Physics project
 * http://www.opensourcephysics.org
 */
