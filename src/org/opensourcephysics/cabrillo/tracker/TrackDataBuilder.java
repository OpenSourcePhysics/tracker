package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.opensourcephysics.controls.ListChooser;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.AbstractAutoloadManager;
import org.opensourcephysics.tools.DataFunctionPanel;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionPanel;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.ToolsRes;
import javajs.async.AsyncDialog;
import javajs.async.AsyncFileChooser;

/**
 * A FunctionTool for building data functions for track data.
 */
@SuppressWarnings("serial")
public class TrackDataBuilder extends FunctionTool {

	private static Icon openIcon, saveIcon;

	private TrackerPanel trackerPanel;
	private JButton loadButton, saveButton, autoloadButton;
	private AutoloadManager autoloadManager;

	/**
	 * Constructor.
	 * 
	 * @param trackerPanel the TrackerPanel with the tracks
	 */
	protected TrackDataBuilder(TrackerPanel trackerPanel) {
		super(trackerPanel, false, true);
		this.trackerPanel = trackerPanel;
		addPropertyChangeListener(PROPERTY_FUNCTIONTOOL_PANEL, trackerPanel); // $NON-NLS-1$
		addPropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION, trackerPanel); // $NON-NLS-1$
		addPropertyChangeListener(PROPERTY_FUNCTIONTOOL_VISIBLE, trackerPanel); // $NON-NLS-1$
		ArrayList<Drawable> nogos = trackerPanel.getSystemDrawables();
		Iterator<TTrack> it = trackerPanel.getTracks().iterator();
		while (it.hasNext()) {
			TTrack track = it.next();
			if (nogos.contains(track))
				continue;
			FunctionPanel panel = trackerPanel.createFunctionPanel(track);
			addPanel(track.getName(), panel);
		}
		setHelpPath("data_builder_help.html"); //$NON-NLS-1$
	}

	@Override
	protected void createGUI() {
		super.createGUI();
		createButtons();
		setToolbarComponents(new Component[] { loadButton, saveButton, Box.createHorizontalGlue(), autoloadButton });
	}

	/**
	 * Creates the save, load and autoload buttons.
	 */
	protected void createButtons() {
		// create loadButton
		if (openIcon == null) {
			openIcon = Tracker.getResourceIcon("open.gif", true);
			saveIcon = Tracker.getResourceIcon("save.gif", true);
		}
		loadButton = new JButton(openIcon);
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadXMLFromDialog();
			}

		});

		// create saveButton
		saveButton = new JButton(saveIcon);

		final ActionListener saveBuilderAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				XMLControl control = new XMLControlElement(TrackDataBuilder.this);
				chooseBuilderDataFunctions(control, "Save", null, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						saveBuilderAction(control);
					}
				});

			}

		};
		final ActionListener savePanelAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				XMLControl control = new XMLControlElement(getSelectedPanel());
				choosePanelDataFunctions(control, "Save", null, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						savePanelAction(control);
					}

				});

			}
		};
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu();
				JMenuItem item = new JMenuItem(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveAll.Text")); //$NON-NLS-1$
				item.setToolTipText(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveAll.Tooltip")); //$NON-NLS-1$
				popup.add(item);
				item.addActionListener(saveBuilderAction);

				String s = " " + getSelectedPanel().getName(); //$NON-NLS-1$
				item = new JMenuItem(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveOnly.Text") + s); //$NON-NLS-1$
				item.setToolTipText(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveOnly.Tooltip")); //$NON-NLS-1$
				popup.add(item);
				item.addActionListener(savePanelAction);

				FontSizer.setFonts(popup, FontSizer.getLevel());
				popup.show(saveButton, 0, saveButton.getHeight());
			}
		});

		// create autoloadButton
		autoloadButton = new JButton();
		autoloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AutoloadManager manager = getAutoloadManager();
				manager.refreshAutoloadData();
				manager.setVisible(true);
			}

		});
	}

	protected void savePanelAction(XMLControl control) {
		JFileChooser chooser = OSPRuntime.createChooser(TrackerRes.getString("TrackerPanel.DataBuilder.Save.Title"), //$NON-NLS-1$
				TrackerRes.getString("TrackerPanel.DataBuilder.Chooser.XMLFiles"), //$NON-NLS-1$
				new String[] { "xml" }); //$NON-NLS-1$
		int result = chooser.showSaveDialog(TrackDataBuilder.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
			File file = chooser.getSelectedFile();
			String fileName = file.getAbsolutePath();
			if (!"xml".equals(XML.getExtension(fileName))) { //$NON-NLS-1$
				fileName = XML.stripExtension(fileName) + ".xml"; //$NON-NLS-1$
				file = new File(fileName);
			}
			if (!TrackerIO.canWrite(file)) {
				return;
			}
			control.write(fileName);
		}

	}

	protected void saveBuilderAction(XMLControl control) {
		JFileChooser chooser = OSPRuntime.createChooser(TrackerRes.getString("TrackerPanel.DataBuilder.Save.Title"), //$NON-NLS-1$
				TrackerRes.getString("TrackerPanel.DataBuilder.Chooser.XMLFiles"), //$NON-NLS-1$
				new String[] { "xml" }); //$NON-NLS-1$
		int result = chooser.showSaveDialog(TrackDataBuilder.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
			File file = chooser.getSelectedFile();
			String fileName = file.getAbsolutePath();
			if (!"xml".equals(XML.getExtension(fileName))) { //$NON-NLS-1$
				fileName = XML.stripExtension(fileName) + ".xml"; //$NON-NLS-1$
				file = new File(fileName);
			}
			if (!TrackerIO.canWrite(file)) {
				return;
			}
			control.write(fileName);
		}

	}

	protected void loadXMLFromDialog() {

		AsyncFileChooser chooser = OSPRuntime.createChooser(TrackerRes.getString("TrackerPanel.DataBuilder.Load.Title"), //$NON-NLS-1$
				TrackerRes.getString("TrackerPanel.DataBuilder.Chooser.XMLFiles"), //$NON-NLS-1$
				new String[] { "xml" }); //$NON-NLS-1$

		chooser.showOpenDialog(TrackDataBuilder.this, new Runnable() {

			@Override
			public void run() {
				OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
				XMLControl control = new XMLControlElement(chooser.getSelectedFile());
				if (control.failedToRead()) {
					JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
							TrackerRes.getString("Tracker.Dialog.Invalid.Message"), //$NON-NLS-1$
							TrackerRes.getString("Tracker.Dialog.Invalid.Title"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				Class<?> type = control.getObjectClass();
				if (DataFunctionPanel.class.isAssignableFrom(type)) {
					loadXMLDataFunction(control);
				} else if (TrackDataBuilder.class.isAssignableFrom(type)) {
					loadXMLTrackData(control);
				} else {
					JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
							TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongType.Message"), //$NON-NLS-1$
							TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongType.Title"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}, null);
	}

	protected void loadXMLTrackData(XMLControl control) {
		// determine what the selected panel track type is
		FunctionPanel dataPanel = getSelectedPanel();
		String panelTrackType = dataPanel.getDescription();

		// find the first DataFunctionPanel with same track type
		XMLControl target = null;
		outerLoop: for (XMLProperty next : control.getPropsRaw()) {
			if (next.getPropertyName().equals("functions")) { //$NON-NLS-1$
				// found DataFunctionPanels
				XMLControl[] panels = next.getChildControls();
				for (XMLControl panelControl : panels) {
					String trackType = panelControl.getString("description"); //$NON-NLS-1$
					if (trackType == null || panelTrackType == null || !panelTrackType.equals(trackType)) {
						// wrong track type
						continue;
					}
					// found right panel
					target = panelControl;
					break outerLoop;
				}
			}
		} // end outerLoop
		XMLControl targetControl = target;
		String trackType = TrackerRes.getString("TrackerPanel.DataBuilder.TrackType.Unknown"); //$NON-NLS-1$
		if (panelTrackType != null)
			trackType = TrackerRes.getString(XML.getExtension(panelTrackType) + ".Name").toLowerCase(); //$NON-NLS-1$

		if (target == null) {
			JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
					TrackerRes.getString("TrackDataBuilder.Dialog.NoFunctionsFound.Message") + " \"" //$NON-NLS-1$ //$NON-NLS-2$
							+ trackType + ".\"", //$NON-NLS-1$
					TrackerRes.getString("TrackDataBuilder.Dialog.NoFunctionsFound.Title"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		XMLControl finalTarget = target;
		String ttype = trackType;

		choosePanelDataFunctions(target, "Load", null, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getID() == ActionEvent.ACTION_PERFORMED) {
					// load data function panel(s)
					ArrayList<FunctionPanel> panelsToLoad = new ArrayList<FunctionPanel>();
					for (String name : getPanelNames()) {
						FunctionPanel nextPanel = getPanel(name);
						if (panelTrackType.equalsIgnoreCase(nextPanel.getDescription())) {
							panelsToLoad.add(nextPanel);
						}
					}

					if (panelsToLoad.size() <= 1) {
						finalTarget.loadObject(dataPanel);
					} else {
						Object[] options = new String[] {
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.All"), //$NON-NLS-1$
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.Only") + " " //$NON-NLS-1$ //$NON-NLS-2$
										+ dataPanel.getName(),
								TrackerRes.getString("Dialog.Button.Cancel") }; //$NON-NLS-1$
						new AsyncDialog().showOptionDialog(TrackDataBuilder.this,
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Message") + " \"" //$NON-NLS-1$ //$NON-NLS-2$
										+ ttype + "\"?", //$NON-NLS-1$
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Title"), //$NON-NLS-1$
								JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0],
								new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										switch (e.getActionCommand()) {
										case "0":
											for (FunctionPanel nextPanel : panelsToLoad) {
												targetControl.loadObject(nextPanel);
											}
											break;
										case "1":
											targetControl.loadObject(dataPanel);
											break;
										}
									}

								});
					}

				}
			}
		});
	}

	protected void loadXMLDataFunction(XMLControl control) {
		// determine what track type the control is for
		FunctionPanel dataPanel = getSelectedPanel();
		Class<?> panelType = null;
		Class<?> controlType = null;
		try {
			panelType = Class.forName(dataPanel.getDescription());
			controlType = Class.forName(control.getString("description")); //$NON-NLS-1$ );
		} catch (ClassNotFoundException ex) {
		}
		String trackType = TrackerRes.getString("TrackerPanel.DataBuilder.TrackType.Unknown"); //$NON-NLS-1$
		if (controlType != null)
			trackType = TrackerRes.getString(controlType.getSimpleName() + ".Name").toLowerCase(); //$NON-NLS-1$

		if (controlType != panelType) {
			String targetType = TrackerRes.getString(panelType.getSimpleName() + ".Name").toLowerCase(); //$NON-NLS-1$
			JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
					TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongTrackType.Message1") //$NON-NLS-1$
							+ " \"" + trackType + ".\"" //$NON-NLS-1$ //$NON-NLS-2$
							+ "\n" //$NON-NLS-1$
							+ TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongTrackType.Message2") //$NON-NLS-1$
							+ " \"" + targetType + ".\"", //$NON-NLS-1$ //$NON-NLS-2$
					TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongTrackType.Title"), //$NON-NLS-1$
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		Class<?> ptype = panelType;
		Class<?> ctype = controlType;
		String ttype = trackType;
		choosePanelDataFunctions(control, "Load", null, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getID() == ActionEvent.ACTION_PERFORMED) {
					Class<?> panelType = ptype;
					ArrayList<FunctionPanel> panelsToLoad = new ArrayList<FunctionPanel>();
					for (String name : getPanelNames()) {
						FunctionPanel nextPanel = getPanel(name);
						try {
							panelType = Class.forName(nextPanel.getDescription());
						} catch (ClassNotFoundException ex) {
						}
						if (panelType == ctype) {
							panelsToLoad.add(nextPanel);
						}
					}

					if (panelsToLoad.size() <= 1) {
						control.loadObject(dataPanel);
					} else {
						Object[] options = new String[] {
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.All"), //$NON-NLS-1$
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.Only") + " " //$NON-NLS-1$ //$NON-NLS-2$
										+ dataPanel.getName(),
								TrackerRes.getString("Dialog.Button.Cancel") }; //$NON-NLS-1$
						new AsyncDialog().showOptionDialog(TrackDataBuilder.this,
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Message") + " \"" //$NON-NLS-1$ //$NON-NLS-2$
										+ ttype + "\"?", //$NON-NLS-1$
								TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Title"), //$NON-NLS-1$
								JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0],
								new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										switch (e.getActionCommand()) {
										case "0":
											for (FunctionPanel nextPanel : panelsToLoad) {
												control.loadObject(nextPanel);
											}
											break;
										case "1":
											control.loadObject(dataPanel);
											break;
										}
									}
								});
					}
				}
			}
		});
	}

	@Override
	protected void setTitles() {
		dropdownTipText = (TrackerRes.getString("TrackerPanel.DataBuilder.Dropdown.Tooltip")); //$NON-NLS-1$
		titleText = (TrackerRes.getString("TrackerPanel.DataBuilder.Title")); //$NON-NLS-1$
	}

	/**
	 * Refreshes the GUI.
	 */
	@Override
	protected void refreshGUI() {
		if (!haveGUI())
			return;
		super.refreshGUI();
		if (loadButton != null) {
			FunctionPanel panel = getSelectedPanel();
			loadButton.setEnabled(panel != null);
			saveButton.setEnabled(panel != null);
			loadButton.setToolTipText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Load.Tooltip")); //$NON-NLS-1$
			saveButton.setToolTipText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Save.Tooltip")); //$NON-NLS-1$
			autoloadButton.setText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Autoload") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
			autoloadButton.setToolTipText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Autoload.Tooltip")); //$NON-NLS-1$
		}
		setFontLevel(FontSizer.getLevel());
		if (autoloadManager != null) {
			autoloadManager.refreshGUI();
		}
	}

	@Override
	public void setFontLevel(int level) {
		if (autoloadButton == null)
			return;
		FontSizer.setFonts(new Object[] { loadButton, saveButton, autoloadButton }, level);
		if (!trackFunctionPanels.isEmpty()) {
			ArrayList<TTrack> tracks = trackerPanel.getTracks();
			FunctionPanel panel;
			TTrack track;
			for (String name : trackFunctionPanels.keySet()) {
				if ((panel = trackFunctionPanels.get(name)) != null
						&& (track = trackerPanel.getTrack(name, tracks)) != null) {
					// get new footprint icon, automatically resized to current level
					panel.setIcon(track.getIcon(21, 16, "point")); //$NON-NLS-1$
				}
			}
		}
		super.setFontLevel(level);
		validate();
		autoloadButton.revalidate();
	}

	/**
	 * Adds a FunctionPanel.
	 *
	 * @param name  a descriptive name
	 * @param panel the FunctionPanel
	 * @return the added panel
	 */
	@Override
	public FunctionPanel addPanel(String name, FunctionPanel panel) {
		super.addPanel(name, panel);
		if (!Tracker.haveDataFunctions())
			return panel;
		
		// autoload data functions, if any, for this track type
		Class<?> trackType = null;
		try {
			trackType = Class.forName(panel.getDescription());
			Tracker.loadControlStringObjects(trackType, panel);
			// load from XMLControls autoloaded from XML files in search paths
			Tracker.loadControls(trackType, panel);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return panel;
	}

	/**
	 * Chooses data functions from a DataFunctionPanel XMLControl.
	 *
	 * @param control           the XMLControl
	 * @param description       "Save" or "Load"
	 * @param selectedFunctions collection of DataFunction choices
	 * @return true if user clicked OK
	 */
	protected void choosePanelDataFunctions(XMLControl control, String description,
			Collection<String[]> selectedFunctions, ActionListener listener) {
		ArrayList<Object> originals = new ArrayList<Object>();
		ArrayList<Object> choices = new ArrayList<Object>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> expressions = new ArrayList<String>();
		ArrayList<?> functions = (ArrayList<?>) control.getObject("functions"); //$NON-NLS-1$
		for (Object next : functions) {
			String[] function = (String[]) next;
			originals.add(function);
			choices.add(function);
			names.add(function[0]);
			expressions.add(function[1]);
		}
		// select all by default
		boolean[] selected = new boolean[choices.size()];
		for (int i = 0; i < selected.length; i++) {
			selected[i] = true;
		}
		ListChooser listChooser = new ListChooser(
				TrackerRes.getString("TrackerPanel.DataBuilder." + description + ".Title"), //$NON-NLS-1$ //$NON-NLS-2$
				TrackerRes.getString("TrackerPanel.DataBuilder." + description + ".Message"), //$NON-NLS-1$ //$NON-NLS-2$
				this, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getID() == ActionEvent.ACTION_PERFORMED) {
							// compare choices with originals and remove unwanted object content
							for (Object next : originals) {
								if (!choices.contains(next)) {
									functions.remove(next);
								}
							}
							// rewrite the control with only selected functions
							control.setValue("functions", functions); //$NON-NLS-1$
						}
						listener.actionPerformed(e);
					}

				});
		listChooser.setSeparator(" = "); //$NON-NLS-1$
		// choose the elements and save
		listChooser.choose(choices, names, expressions, null, selected, null);

	}

	/**
	 * Chooses data functions from a DataBuilder XMLControl.
	 *
	 * @param control           the XMLControl
	 * @param description       "Save" or "Load"
	 * @param selectedFunctions collection of DataFunction choices
	 * @return true if user clicked OK
	 */
	@SuppressWarnings("unchecked")
	protected void chooseBuilderDataFunctions(XMLControl control, String description,
			Collection<String[]> selectedFunctions, ActionListener listener) {
		ArrayList<String[]> originals = new ArrayList<String[]>();
		ArrayList<String[]> choices = new ArrayList<String[]>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> expressions = new ArrayList<String>();
		ArrayList<String> trackTypes = new ArrayList<String>();

		Map<String, XMLControl> xmlControlMap = new TreeMap<String, XMLControl>();
		Map<String, ArrayList<Parameter>> parameterMap = new TreeMap<String, ArrayList<Parameter>>();
		Map<String, ArrayList<String[]>> functionMap = new TreeMap<String, ArrayList<String[]>>();
		for (XMLProperty prop : control.getPropsRaw()) {
			for (XMLControl xmlControl : prop.getChildControls()) {
				if (xmlControl.getObjectClass() != DataFunctionPanel.class)
					continue;

				// get track type (description) and map to panel xmlControl
				String trackType = xmlControl.getString("description"); //$NON-NLS-1$
				xmlControlMap.put(trackType, xmlControl);

				// get the list of functions for this track type
				ArrayList<String[]> functions = functionMap.get(trackType);
				if (functions == null) {
					functions = new ArrayList<String[]>();
					functionMap.put(trackType, functions);
				}
				// add functions found in this xmlControl unless already present
				ArrayList<String[]> panelFunctions = (ArrayList<String[]>) xmlControl.getObject("functions"); //$NON-NLS-1$
				outer: for (String[] f : panelFunctions) {
					// check for duplicate function names
					for (String[] existing : functions) {
						if (existing[0].equals(f[0]))
							continue outer;
					}
					functions.add(f);
				}

				// get the list of parameters for this track type
				ArrayList<Parameter> params = parameterMap.get(trackType);
				if (params == null) {
					params = new ArrayList<Parameter>();
					parameterMap.put(trackType, params);
				}
				// add parameters found in this xmlControl unless already present
				Parameter[] panelParams = (Parameter[]) xmlControl.getObject("user_parameters"); //$NON-NLS-1$
				outer: for (Parameter p : panelParams) {
					if (trackType.endsWith("PointMass") && p.getName().equals("m")) { //$NON-NLS-1$ //$NON-NLS-2$
						continue outer;
					}
					// check for duplicate parameter names
					for (Parameter existing : params) {
						if (existing.getName().equals(p.getName()))
							continue outer;
					}
					params.add(p);
				}
			}
		}

		for (String trackType : functionMap.keySet()) {
			ArrayList<String[]> functions = functionMap.get(trackType);
			for (String[] f : functions) {
				originals.add(f);
				choices.add(f);
				names.add(f[0]);
				expressions.add(f[1]);
				String shortName = XML.getExtension(trackType);
				String localized = TrackerRes.getString(shortName + ".Name"); //$NON-NLS-1$
				if (!localized.startsWith("!")) //$NON-NLS-1$
					shortName = localized;
				trackTypes.add("[" + shortName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		// select all by default
		boolean[] selected = new boolean[choices.size()];
		for (int i = 0; i < selected.length; i++) {
			selected[i] = true;
		}

		ListChooser listChooser = new ListChooser(
				TrackerRes.getString("TrackerPanel.DataBuilder." + description + ".Title"), //$NON-NLS-1$ //$NON-NLS-2$
				TrackerRes.getString("TrackerPanel.DataBuilder." + description + ".Message"), //$NON-NLS-1$ //$NON-NLS-2$
				this, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getID() == ActionEvent.ACTION_PERFORMED) {
							// compare choices with originals and remove unwanted object content
							for (String[] function : originals) {
								if (!choices.contains(function)) {
									for (String trackType : xmlControlMap.keySet()) {
										ArrayList<String[]> functions = functionMap.get(trackType);
										functions.remove(function);
									}
								}
							}
							// set functions in xmlControl for each trackType
							for (String trackType : xmlControlMap.keySet()) {
								ArrayList<String[]> functions = functionMap.get(trackType);
								ArrayList<Parameter> paramList = parameterMap.get(trackType);
								Parameter[] params = paramList.toArray(new Parameter[paramList.size()]);
								XMLControl xmlControl = xmlControlMap.get(trackType);
								xmlControl.setValue("functions", functions); //$NON-NLS-1$
								xmlControl.setValue("user_parameters", params); //$NON-NLS-1$
							}

							// keep only xmlControls that have functions and are in xmlControlMap
							for (Object next : control.getPropertyContent()) {
								if (next instanceof XMLProperty
										&& ((XMLProperty) next).getPropertyName().equals("functions")) { //$NON-NLS-1$
									XMLProperty panels = (XMLProperty) next;
									java.util.List<Object> content = panels.getPropertyContent();
									ArrayList<Object> toRemove = new ArrayList<Object>();
									for (Object child : content) {
										XMLControl xmlControl = ((XMLProperty) child).getChildControls()[0];
										if (!xmlControlMap.values().contains(xmlControl)) {
											toRemove.add(child);
										} else { // check to see if functions is empty
											ArrayList<String[]> functions = (ArrayList<String[]>) xmlControl
													.getObject("functions"); //$NON-NLS-1$
											if (functions == null || functions.isEmpty()) {
												toRemove.add(child);
											}
										}
									}
									for (Object remove : toRemove) {
										content.remove(remove);
									}
								}
							}

						}
						listener.actionPerformed(e);
					}

				});
		listChooser.setSeparator(" = "); //$NON-NLS-1$
		// choose the elements and save
		listChooser.choose(choices, names, expressions, trackTypes, selected, null);

	}

	/**
	 * Gets the autoload manager, creating it the first time called.
	 * 
	 * @return the autoload manageer
	 */
	protected AutoloadManager getAutoloadManager() {
		if (autoloadManager == null) {
			autoloadManager = new AutoloadManager(this);

			// center on screen
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - autoloadManager.getBounds().width) / 2;
			int y = (dim.height - autoloadManager.getBounds().height) / 2;
			autoloadManager.setLocation(x, y);
			
			if (Tracker.haveDataFunctions())
				Tracker.loadControlStrings(new Runnable() {

				@Override
				public void run() {
					autoloadManager.refreshAutoloadData();
				}
				
			});
		}
		autoloadManager.setFontLevel(FontSizer.getLevel());
		return autoloadManager;
	}

	/**
	 * Adds a FunctionPanel without autoloading any data functions.
	 *
	 * @param name  a descriptive name
	 * @param panel the FunctionPanel
	 */
	protected void addPanelWithoutAutoloading(String name, FunctionPanel panel) {
		super.addPanel(name, panel);
	}

	/**
	 * Disposes of this data builder.
	 */
	@Override
	public void dispose() {
		removePropertyChangeListener(PROPERTY_FUNCTIONTOOL_PANEL, trackerPanel); // $NON-NLS-1$
		removePropertyChangeListener(FunctionTool.PROPERTY_FUNCTIONTOOL_FUNCTION, trackerPanel); // $NON-NLS-1$
		removePropertyChangeListener(TTrack.PROPERTY_TTRACK_VISIBLE, trackerPanel); // $NON-NLS-1$
		ToolsRes.removePropertyChangeListener("locale", this); //$NON-NLS-1$
		if (autoloadManager != null) {
			autoloadManager.dispose();
		}
		for (String key : trackFunctionPanels.keySet()) {
			FunctionPanel next = trackFunctionPanels.get(key);
			next.setFunctionTool(null);
		}
		clearPanels();
		selectedPanel = null;
		if (trackerPanel != null)
			trackerPanel.dataBuilder = null;
		trackerPanel = null;
		super.dispose();
	}

	@Override
	public void finalize() {
		OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

	/**
	 * An AutoloadManager for DataFunctions.
	 */
	class AutoloadManager extends AbstractAutoloadManager {

		/**
		 * Constructor for a dialog.
		 * 
		 * @param dialog the dialog (DataBuilder)
		 */
		public AutoloadManager(JDialog dialog) {
			super(dialog);
		}

		@Override
		public void setVisible(boolean vis) {
			super.setVisible(vis);
			if (!vis) {
				Tracker.autoloadDataFunctions();
				Tracker.savePreferences();
				// reload autoloaded functions into existing panels
				for (String name : getPanelNames()) {
					DataFunctionPanel panel = (DataFunctionPanel) getPanel(name);
					addPanel(name, panel);
				}				
				// save non-default search paths in Tracker.preferredAutoloadSearchPaths
				Collection<String> searchPaths = getSearchPaths();
				Collection<String> defaultPaths = Tracker.getDefaultAutoloadSearchPaths();
				boolean isDefault = searchPaths.size() == defaultPaths.size();
				for (String next : searchPaths) {
					isDefault = isDefault && defaultPaths.contains(next);
				}
				if (isDefault) {
					Tracker.preferredAutoloadSearchPaths = null;
				} else {
					Tracker.preferredAutoloadSearchPaths = searchPaths.toArray(new String[searchPaths.size()]);
				}
			}
		}

		/**
		 * Refreshes the GUI.
		 */
		@Override
		protected void refreshGUI() {
			refreshAutoloadData();
			super.refreshGUI();
			String title = TrackDataBuilder.this.getTitle() + " " + getTitle(); //$NON-NLS-1$
			setTitle(title);
			setInstructions(TrackerRes.getString("TrackDataBuilder.Instructions.SelectToAutoload") //$NON-NLS-1$
					+ "\n\n" + TrackerRes.getString("TrackDataBuilder.Instructions.WhereDefined") //$NON-NLS-1$ //$NON-NLS-2$
					+ " " + TrackerRes.getString("TrackDataBuilder.Instructions.HowToAddFunction") //$NON-NLS-1$ //$NON-NLS-2$
					+ " " + TrackerRes.getString("TrackDataBuilder.Instructions.HowToAddDirectory")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * Gets a localized track name from the fully qualified track class name.
		 * 
		 * @param trackClass the class name
		 * @return the localized name
		 */
		protected String getLocalizedTrackName(String trackClass) {
			String trackName = XML.getExtension(trackClass);
			String localized = TrackerRes.getString(trackName + ".Name"); //$NON-NLS-1$
			if (!localized.startsWith("!")) //$NON-NLS-1$
				trackName = localized;
			return trackName;
		}

//		@Override
//		protected void setFunctionSelected(String filePath, String[] function, boolean select) {
//			super.setFunctionSelected(filePath, function, select);
//			Tracker.autoloadDataFunctions();			
//			// reload autoloaded functions into existing panels
//			for (String name : getPanelNames()) {
//				DataFunctionPanel panel = (DataFunctionPanel) getPanel(name);
//				addPanel(name, panel);
//			}
//
//		}

//		/**
//		 * will fail in SwingJS 
//		 * 
//		 * Sets the selection state of a file.
//		 *
//		 * @param filePath the path to the file
//		 * @param select   true/false to select/deselect the file and all its functions
//		 */
//		@Override
//		protected void setFileSelected(String filePath, boolean select) {
//			super.setFileSelected(filePath, select);			
//			Tracker.autoloadDataFunctions();
//			// reload autoloaded functions into existing panels
//			for (String name : getPanelNames()) {
//				DataFunctionPanel panel = (DataFunctionPanel) getPanel(name);
//				addPanel(name, panel);
//			}
//		}
//
		/**
		 * Refreshes the autoload data.
		 */
		@Override
		protected void refreshAutoloadData() {
			final Map<String, Map<String, ArrayList<String[]>>> data = new TreeMap<String, Map<String, ArrayList<String[]>>>();
			for (String path : getSearchPaths()) {
				Map<String, ArrayList<String[]>> functionMap = Tracker.findDataFunctions(path);
				data.put(path, functionMap);
			}
			setAutoloadData(data);
		}

		/**
		 * Gets the collection of search paths.
		 * 
		 * @return the search paths
		 */
		@Override
		public Collection<String> getSearchPaths() {
			Collection<String> paths = super.getSearchPaths();
			if (paths.isEmpty() && !initialized) {
				initialized = true;
				for (String next : Tracker.getInitialSearchPaths()) {
					paths.add(next);
					addSearchPath(next);
				}
			}
			return paths;
		}

		@Override
		protected Map<String, String[]> getExclusionsMap() {
			return Tracker.autoloadMap;
		}

	}


}
