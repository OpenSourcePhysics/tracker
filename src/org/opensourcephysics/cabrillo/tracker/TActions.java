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

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.Filter;
import org.opensourcephysics.media.core.FilterStack;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import javajs.async.AsyncSwingWorker;

/**
 * This creates a map of action name to action for many common tracker actions.
 *
 * @author Douglas Brown
 */
public class TActions {

	// static fields
	static Map<TrackerPanel, Map<String, AbstractAction>> actionMaps = new HashMap<TrackerPanel, Map<String, AbstractAction>>(); // maps
																																	// trackerPanel
																																	// to
																																	// actions
																																	// map
	static String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Private constructor.
	 */
	private TActions() {
		/** empty block */
	}

	/**
	 * Gets an action for a TrackerPanel.
	 * 
	 * @param key          the name of the action
	 * @param trackerPanel the TrackerPanel
	 * @return the Action
	 */
	public static Action getAction(String key, TrackerPanel trackerPanel) {
		return getActions(trackerPanel).get(key);
	}

	/**
	 * Clears all actions. This forces creation of new ones using new locale.
	 */
	public static void clear() {
		actionMaps.clear();
	}

	/**
	 * Gets the action map for a TrackerPanel.
	 * 
	 * @param trackerPanel the TrackerPanel
	 * @return the Map
	 */
	@SuppressWarnings("serial")
	public static Map<String, AbstractAction> getActions(TrackerPanel trackerPanel) {
		Map<String, AbstractAction> actions = actionMaps.get(trackerPanel);
		if (actions != null)
			return actions;
		// create new actionMap
		actions = new HashMap<String, AbstractAction>();
		actionMaps.put(trackerPanel, actions);

		// clear tracks
		actions.put("clearTracks", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ClearTracks"), //$NON-NLS-1$
						null) {
					@Override
					public void actionPerformed(ActionEvent e) {
						clearTracks(trackerPanel);
					}
				});

		// new tab
		actions.put("newTab", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("TActions.Action.NewTab"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							frame.addTrackerPanel(true, null);
						}
					}
				}, true));

		// pastexml or data
		actions.put("paste", //$NON-NLS-1$

				new AbstractAction(TrackerRes.getString("TActions.Action.Paste")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						OSPRuntime.paste((data) -> {
							if (data == null)
								return;
							String dataString = pasteXMLAction(trackerPanel, data);
							if (dataString != null)
								trackerPanel.importDataAsync(dataString, null, null);
						});

					}
				});

		// open
		actions.put("open", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Open"), //$NON-NLS-1$
						Tracker.getResourceIcon("open.gif", true)) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							frame.doOpenFileFromDialog();
						}
					}
				});

		// open url
		actions.put("openURL", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.OpenURL")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						String input = GUIUtils.showInputDialog(trackerPanel.getTFrame(),
								TrackerRes.getString("TActions.Dialog.OpenURL.Message") //$NON-NLS-1$
										+ ":                             ", //$NON-NLS-1$
								TrackerRes.getString("TActions.Dialog.OpenURL.Title"), //$NON-NLS-1$
								JOptionPane.PLAIN_MESSAGE, null);
						if (input == null || input.trim().equals("")) { //$NON-NLS-1$
							return;
						}
						Resource res = ResourceLoader.getResource(input.toString().trim());
						if (res == null || res.getURL() == null) {
							JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
									TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Message") //$NON-NLS-1$
											+ "\n\"" + input.toString().trim() + "\"", //$NON-NLS-1$ //$NON-NLS-2$
									TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Title"), //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							frame.removeEmptyTab(0);
							frame.doOpenURL(res.getURL().toExternalForm());
						}
					}
				});

		// openBrowser
		actions.put("openBrowser", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("TActions.Action.OpenBrowser"), //$NON-NLS-1$
						Tracker.getResourceIcon("open_catalog.gif", true) //$NON-NLS-1$
				) {
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
//							boolean isVisible = frame.getLibraryBrowser().isVisible();
//							frame.getLibraryBrowser().setVisible(!isVisible);
							frame.getLibraryBrowser().setVisible(true);
						}
					}
				}, true));

		// properties
		actions.put("properties", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Properties")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							frame.getPropertiesDialog(trackerPanel).setVisible(true);
						}

					}
				});

		// close tab
		actions.put("close", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Close")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							frame.removeTab(trackerPanel);
						}
					}
				});

		// close all tabs
		actions.put("closeAll", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.CloseAll")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							frame.removeAllTabs();
						}
					}
				});

		// import file
		actions.put("import", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.importFile(trackerPanel);
					}
				});

		// import data
		actions.put("importData", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportData")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						getAction("dataTrack", trackerPanel).actionPerformed(e); //$NON-NLS-1$
					}
				});

		// save current tab
		actions.put("save", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Save"), //$NON-NLS-1$
						Tracker.getResourceIcon("save.gif", true)) { // $NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.save(trackerPanel.getDataFile(), trackerPanel);
						trackerPanel.refreshNotesDialog();
					}
				});

		// save tab as
		actions.put("saveAs", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.SaveAs"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.save(null, trackerPanel);
						trackerPanel.refreshNotesDialog();
					}
				});

		// save zip resource
		actions.put("saveZip", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.SaveZip") + "...", //$NON-NLS-1$ //$NON-NLS-2$
						Tracker.getResourceIcon("save_zip.gif", true) //$NON-NLS-1$
				) {
					@Override
					public void actionPerformed(ActionEvent e) {
						ExportZipDialog zipDialog = ExportZipDialog.getDialog(trackerPanel);
						zipDialog.setVisible(true);
					}
				});

		// save tabset as
		actions.put("saveTabsetAs", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.SaveFrame"), //$NON-NLS-1$
						null) {
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.saveTabset(null, trackerPanel.getTFrame());
						trackerPanel.refreshNotesDialog();
					}
				});

		// save video
		// BH This action is not implemented?
		// needs a variety of changes to make it work properly
		actions.put("saveVideo", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.SaveVideoAs")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.saveVideo(null, trackerPanel);
					}
				});

		// export XML file
		actions.put("export", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.exportXMLFile(trackerPanel);
					}
				});

		// delete track
		actions.put("deleteTrack", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Delete"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						// action command is name of track to delete
						TTrack track = trackerPanel.getTrack(e.getActionCommand());
						if (track != null)
							track.delete();
					}
				});

		actions.put("config", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Config"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						frame.showPrefsDialog();
					}
				});

		// axesVisible
		actions.put("axesVisible", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.AxesVisible"), //$NON-NLS-1$
						Tracker.getResourceIcon("axes.gif", true) //$NON-NLS-1$
				) {
					@Override
					public void actionPerformed(ActionEvent e) {
						CoordAxes axes = trackerPanel.getAxes();
						if (axes == null)
							return;
						boolean visible = !axes.isVisible();
						axes.setVisible(visible);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.hideMouseBox();
						if (visible && trackerPanel.getSelectedTrack() == null)
							trackerPanel.setSelectedTrack(axes);
						else if (!visible && trackerPanel.getSelectedTrack() == axes)
							trackerPanel.setSelectedTrack(null);
//				TFrame.repaintT(trackerPanel);
					}
				});

		// videoFilter
		actions.put("videoFilter", //$NON-NLS-1$
				getAsyncAction(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Video video = trackerPanel.getVideo();
						if (video == null)
							return;
						trackerPanel.setVideoVisible(true);
						FilterStack filterStack = video.getFilterStack();
						Filter filter = null;
						Map<String, Class<? extends Filter>> filterClasses = trackerPanel.getFilters();
						Class<? extends Filter> filterClass = filterClasses.get(e.getActionCommand());
						if (filterClass != null) {
							try {
								filter = filterClass.newInstance();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							if (filter != null) {
								filterStack.addFilter(filter);
								filter.setVideoPanel(trackerPanel);
								JDialog inspector = filter.getInspector();
								if (inspector != null) {
									inspector.setVisible(true);
								}
							}
						}
						TFrame.repaintT(trackerPanel);
					}
				}, true));

		// about video
		actions.put("aboutVideo", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.AboutVideo"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							PropertiesDialog dialog = frame.getPropertiesDialog(trackerPanel);
							if (trackerPanel.getVideo() != null)
								dialog.tabbedPane.setSelectedIndex(1);
							dialog.setVisible(true);
						}
					}
				});

		// print
		actions.put("print", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Print"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						new TrackerIO.ComponentImage(trackerPanel).print();
					}
				});

		// exit
		actions.put("exit", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Exit"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TFrame frame = trackerPanel.getTFrame();
						if (frame != null) {
							// save tabs in try/catch block so always closes
//					try {
//						int[] i = {0};
//						TrackerPanel panel = frame.getTrackerPanel(i[0]);
//						if (panel == null)
//							System.exit(0);
//						
//						Runnable whenSaved = new Runnable() {
//							@Override
//							public void run() {
//								i[0]++;
//								TrackerPanel panel = frame.getTrackerPanel(i[0]);
//								if (panel != null)
//									panel.save(whenSaved, null);
//								else 
//									System.exit(0);
//							}
//						};
//						panel.save(whenSaved, null);
//					} catch (Exception ex) {
//						System.exit(0);
//					}
						}
					}
				});

		// new point mass
		actions.put("pointMass", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("PointMass.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						PointMass pointMass = new PointMass();
						pointMass.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(pointMass);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(pointMass);

						if (!Tracker.markAtCurrentFrame) {
							trackerPanel.getPlayer().setStepNumber(0);
						}
						// offer to add new mass if single cm exists
						ArrayList<CenterOfMass> list = trackerPanel.getDrawablesTemp(CenterOfMass.class);
						if (list.size() == 1) {
							CenterOfMass cm = list.get(0);
							int result = JOptionPane.showConfirmDialog(trackerPanel,
									"Add " + pointMass.getName() + " to center of mass \"" + //$NON-NLS-1$ //$NON-NLS-2$
							cm.getName() + "\"?" + newline + //$NON-NLS-1$
							"Note: \"" + cm.getName() + "\" will disappear until  " + //$NON-NLS-1$ //$NON-NLS-2$
							pointMass.getName() + " is marked!", //$NON-NLS-1$
									TrackerRes.getString("TActions.Dialog.NewPointMass.Title"), //$NON-NLS-1$
									JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (result == JOptionPane.YES_OPTION) {
								cm.addMass(pointMass);
							}
						}
						list.clear();
					}
				}, true));

		// new center of mass
		actions.put("cm", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("CenterOfMass.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						CenterOfMass cm = new CenterOfMass();
						cm.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(cm);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(cm);
						CenterOfMassInspector cmInspector = cm.getInspector();
						cmInspector.setVisible(true);
					}
				}, true));

		// new vector
		actions.put("vector", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("Vector.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						Vector vec = new Vector();
						vec.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(vec);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(vec);
						if (!Tracker.markAtCurrentFrame) {
							trackerPanel.getPlayer().setStepNumber(0);
						}
					}
				}, true));

		// new vector sum
		actions.put("vectorSum", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("VectorSum.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						VectorSum sum = new VectorSum();
						sum.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(sum);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(sum);
						VectorSumInspector sumInspector = sum.getInspector();
						sumInspector.setVisible(true);
					}
				}, true));

		// new offset origin item
		actions.put("offsetOrigin", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("OffsetOrigin.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						OffsetOrigin offset = new OffsetOrigin();
						offset.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(offset);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(offset);
						trackerPanel.getAxes().setVisible(true);
					}
				}, true));

		// new calibration item
		actions.put("calibration", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("Calibration.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						Calibration cal = new Calibration();
						cal.setDefaultNameAndColor(trackerPanel, " ");
						trackerPanel.addTrack(cal);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(cal);
						trackerPanel.getAxes().setVisible(true);
					}
				}, true));

		// new line profile item
		actions.put("lineProfile", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("LineProfile.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TTrack lineProfile = new LineProfile();
						lineProfile.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(lineProfile);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(lineProfile);
					}
				}, true));

		// new RGBRegion item
		actions.put("rgbRegion", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("RGBRegion.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TTrack rgb = new RGBRegion();
						rgb.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(rgb);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(rgb);
						if (!Tracker.markAtCurrentFrame) {
							trackerPanel.getPlayer().setStepNumber(0);
						}
					}
				}, true));

		// new analytic particle item
		actions.put("analyticParticle", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("AnalyticParticle.Name"), null) {//$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						final AnalyticParticle model = new AnalyticParticle();
						model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(model);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(model);
						FunctionTool inspector = model.getModelBuilder();
						model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
						inspector.setVisible(true);
					}
				}, true));

		// new dynamic particle item
		actions.put("dynamicParticle", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("DynamicParticle.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						DynamicParticle model = new DynamicParticle();
						model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(model);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(model);
						FunctionTool inspector = model.getModelBuilder();
						model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
						inspector.setVisible(true);
					}
				}, true));

		// new dynamic particle polar item
		actions.put("dynamicParticlePolar", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("DynamicParticlePolar.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						DynamicParticle model = new DynamicParticlePolar();
						model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(model);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(model);
						FunctionTool inspector = model.getModelBuilder();
						model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
						inspector.setVisible(true);
					}
				}, true));

		// new dynamic system item
		actions.put("dynamicSystem", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("DynamicSystem.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						DynamicSystem model = new DynamicSystem();
						model.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(model);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(model);
						FunctionTool inspector = model.getModelBuilder();
						model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
						inspector.setVisible(true);
						DynamicSystemInspector systemInspector = model.getSystemInspector();
						systemInspector.setVisible(true);
					}
				}, true));

		// new DataTrack from text file item
		actions.put("dataTrack", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("ParticleDataTrack.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						// choose file and import data
						TrackerIO.getChooserFilesAsync("open data", //$NON-NLS-1$
								(files) -> {
									if (files == null) {
										return null;
									}
									String filePath = files[0].getAbsolutePath();
									trackerPanel.importDataAsync(filePath, null, null);
									return null;
								});
					}
				});

		// new (read-only) tape measure
		actions.put("tape", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("TapeMeasure.Name"), null) {//$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TapeMeasure tape = new TapeMeasure();
						tape.setReadOnly(true);
						tape.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						tape.getRuler().setVisible(true);
						// place tape at center of viewport
						MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
						Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
						int xpix = rect.x + rect.width / 2;
						int ypix = rect.y + rect.height / 2;
						double x = trackerPanel.pixToX(xpix);
						double y = trackerPanel.pixToY(ypix);
						tape.createStep(0, x - 100, y, x + 100, y); // length 200 image units
						trackerPanel.addTrack(tape);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(tape);
					}
				}, true));

		// new protractor
		actions.put("protractor", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("Protractor.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						Protractor protractor = new Protractor();
						protractor.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						protractor.getRuler().setVisible(true);
						trackerPanel.addTrack(protractor);
						// place vertex of protractor at center of viewport
						MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
						Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
						int xpix = rect.x + rect.width / 2;
						int ypix = rect.y + rect.height / 2;
						double x = trackerPanel.pixToX(xpix);
						double y = trackerPanel.pixToY(ypix);
						// if origin is nearby, center on it instead
						TPoint origin = trackerPanel.getAxes().getOrigin();
						if (Math.abs(origin.x - x) < 20 && Math.abs(origin.y - y) < 20) {
							x = origin.x;
							y = origin.y;
						}
						ProtractorStep step = (ProtractorStep) protractor.getStep(0);
						step.moveVertexTo(x, y);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(protractor);
					}
				}, true));

		// new circle track
		actions.put("circleFitter", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("CircleFitter.Name"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						CircleFitter track = new CircleFitter();
						track.setDefaultNameAndColor(trackerPanel, " "); //$NON-NLS-1$
						trackerPanel.addTrack(track);
						trackerPanel.setSelectedPoint(null);
						trackerPanel.selectedSteps.clear();
						trackerPanel.setSelectedTrack(track);
					}
				}, true));
		// clone track action
		actions.put("cloneTrack", //$NON-NLS-1$
				getAsyncAction(new AbstractAction() { // $NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						cloneAction(trackerPanel, e.getActionCommand());
					}
				}, true));

		// clear filters action
		actions.put("clearFilters", //$NON-NLS-1$
				new AbstractAction(// $NON-NLS-1$
						TrackerRes.getString("TActions.Action.ClearFilters"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						Video video = trackerPanel.getVideo();
						if (video != null) {
							ArrayList<String> xml = new ArrayList<String>();
							FilterStack stack = video.getFilterStack();
							for (Filter filter : stack.getFilters()) {
								xml.add(new XMLControlElement(filter).toXML());
								PerspectiveTrack track = PerspectiveTrack.filterMap.get(filter);
								if (track != null) {
									trackerPanel.removeTrack(track);
									track.dispose();
								}
							}
							stack.clear();
							if (e != null) {
								Undo.postFilterClear(trackerPanel, xml);
							}
						}
					}
				});

		// open video
		actions.put("openVideo", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportVideo"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.importVideo(trackerPanel, null);// , TrackerIO.NULL_RUNNABLE);
					}
				});

		// close video
		actions.put("closeVideo", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.CloseVideo"), null) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						trackerPanel.setVideo(null);
						TFrame.repaintT(trackerPanel);
						trackerPanel.setImageSize(640, 480);
						TMenuBar.refreshMenus(trackerPanel, TMenuBar.REFRESH_TACTIONS_OPENVIDEO);
					}
				});

		// reference frame
		actions.put("refFrame", //$NON-NLS-1$
				new AbstractAction() { // $NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						JMenuItem item = (JMenuItem) e.getSource();
						trackerPanel.setReferenceFrame(item.getActionCommand());
					}
				});

		return actions;
	}

	protected static void cloneAction(TrackerPanel trackerPanel, String name) {
		TTrack track = trackerPanel.getTrack(name);
		if (track == null)
			return;
		// add digit to end of name
		int n = 1;
		try {
			String number = name.substring(name.length() - 1);
			n = Integer.parseInt(number) + 1;
			name = name.substring(0, name.length() - 1);
		} catch (Exception ex) {
		}
		// increment digit if necessary
		Set<String> names = new HashSet<String>();
		for (TTrack next : trackerPanel.getTracksTemp()) {
			names.add(next.getName());
		}
		trackerPanel.clearTemp();
		try {
			while (names.contains(name + n)) {
				n++;
			}
		} catch (Exception ex) {
		}
		// create XMLControl of track, assign new name, and copy to clipboard
		XMLControl control = new XMLControlElement(track);
		control.setValue("name", name + n); //$NON-NLS-1$
		// StringSelection data = new StringSelection(control.toXML());
//			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//			clipboard.setContents(data, data);
		// now paste
		pasteXMLAction(trackerPanel, control.toXML());
	}

	/**
	 * 
	 * @param trackerPanel
	 * @param data
	 * @return null if consumed, String data if not
	 */
	static String pasteXMLAction(TrackerPanel trackerPanel, String data) {
		try {
			XMLControl control = new XMLControlElement();
			control.readXML(data);
			Class<?> type = control.getObjectClass();
			if (type == null || control.failedToRead()) {
				return data;
			}
			if (TrackerPanel.class.isAssignableFrom(type)) {
				control.loadObject(trackerPanel);
				return null;
			}
			if (ImageCoordSystem.class.isAssignableFrom(type)) {
				XMLControl state = new XMLControlElement(trackerPanel.getCoords());
				control.loadObject(trackerPanel.getCoords());
				Undo.postCoordsEdit(trackerPanel, state);
				return null;
			}
			Object o = control.loadObject(null);
			if (o instanceof TTrack) {
				TTrack track = (TTrack) o;
				trackerPanel.addTrack(track);
				trackerPanel.setSelectedTrack(track);
				return null;
			}
			if (o instanceof VideoClip) {
				VideoClip clip = (VideoClip) o;
				VideoClip prev = trackerPanel.getPlayer().getVideoClip();
				XMLControl state = new XMLControlElement(prev);
				// make new XMLControl with no stored object
				state = new XMLControlElement(state.toXML());
				trackerPanel.getPlayer().setVideoClip(clip);
				Undo.postVideoReplace(trackerPanel, state);
				return null;
			}
		} catch (Exception ex) {
		}
		return data;
	}

	protected static void clearTracks(TrackerPanel trackerPanel) {
		// check for locked tracks and get list of xml strings for undoableEdit
		ArrayList<String> xml = new ArrayList<String>();
		boolean locked = false;
		ArrayList<org.opensourcephysics.display.Drawable> keepers = trackerPanel.getSystemDrawables();
		for (TTrack track : trackerPanel.getTracksTemp()) {
			if (keepers.contains(track))
				continue;
			xml.add(new XMLControlElement(track).toXML());
			locked = locked || (track.isLocked() && !track.isDependent());
		}
		trackerPanel.clearTemp();
		if (locked) {
			int i = JOptionPane.showConfirmDialog(trackerPanel,
					TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Message"), //$NON-NLS-1$
					TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Title"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (i != 0)
				return;
		}
		
		// post edit and clear tracks
		Undo.postTrackClear(trackerPanel, xml);
		trackerPanel.clearTracks();
	}

	@SuppressWarnings("serial")
	private static AbstractAction getAsyncAction(Action a, boolean useSeparateThread) {
		Object nameObj = a.getValue(Action.NAME);
		String name = nameObj == null ? null : nameObj.toString();
		Object iconObj = a.getValue(Action.SMALL_ICON);
		Icon icon = iconObj == null ? null : (Icon) iconObj;
		return new AbstractAction(name, icon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AsyncSwingWorker(null, null, 0, 0, 1) {

					@Override
					public void initAsync() {
					}

					@Override
					public int doInBackgroundAsync(int i) {
						if (this.getProgressAsync() > 0)
							return 1;
						if (useSeparateThread) {
							AsyncSubtask task = this.new AsyncSubtask(1);
							task.start(new Runnable() {

								@Override
								public void run() {
									a.actionPerformed(e);
									task.done();
								}
							});
						} else {
							a.actionPerformed(e);
						}
						return 1;
					}

					@Override
					public void doneAsync() {
					}

				}.execute();
			}
		};
	}

}
