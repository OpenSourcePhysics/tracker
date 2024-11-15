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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.Filter;
import org.opensourcephysics.media.core.FilterStack;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.Video;

/**
 * This creates a map of action name to action for many common tracker actions.
 *
 * NOTE: These actions should ONLY be called as menu or button actions. The
 * getAction() method should never be used for direct running of these action's
 * method, as some of them are asynchronous. 
 *
 * @author Douglas Brown
 */
public class TActions {

	// static fields
	/**
	 * maps trackerPanel to actions
	 * 
	 */
	static String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	protected TFrame frame;
	protected Integer panelID;

	/**
	 * Gets the action map for a TrackerPanel.
	 * 
	 * @param trackerPanel the TrackerPanel
	 * @return the Map
	 */
	public static Map<String, AbstractAction> createActions(TrackerPanel trackerPanel) {
	  return new TActions(trackerPanel).getActions();
	}
	
	
	private TActions(TrackerPanel trackerPanel) {
		frame = trackerPanel.getTFrame();
		panelID = trackerPanel.getID();
	}

	protected TrackerPanel panel() {
		return (frame == null ? null : frame.getTrackerPanelForID(panelID));
	}

	private Map<String, AbstractAction> getActions() {
		
		HashMap<String, AbstractAction> actions = new HashMap<String, AbstractAction>();

		// clear tracks
		actions.put("clearTracks", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ClearTracks")) {
					@Override
					public void actionPerformed(ActionEvent e) {
						panel().checkAndClearTracks();
					}
				});

		// new tab
		actions.put("newTab", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("TActions.Action.NewTab")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						
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
						if (OSPRuntime.isJS) {
							panel().getPasteDataDialog().setVisible(true);
						} else {
							OSPRuntime.paste((data) -> {
								panel().doPaste(data);
							});
						}

					}
				});

		// open
		actions.put("open", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Open"), //$NON-NLS-1$
						Tracker.getResourceIcon("open.gif", true)) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						panel().setSelectedPoint(null);
						panel().selectedSteps.clear();
						
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
						panel().openURLFromDialog();
					}
				});

		// openBrowser
		actions.put("openBrowser", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("TActions.Action.OpenBrowser")) { //$NON-NLS-1$						
					@Override
					public void actionPerformed(ActionEvent e) {
						
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
						
						if (frame != null) {
							frame.getPropertiesDialog(panel()).setVisible(true);
						}

					}
				});

		// close tab
		actions.put("close", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Close")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						
						if (frame != null) {
							frame.doCloseAction(panel());
						}
					}
				});

		// close all tabs
		actions.put("closeAll", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.CloseAll")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						
						if (frame != null) {
							frame.removeAllTabs(false);
						}
					}
				});

		// import file
		actions.put("import", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.importFile(panel());
					}
				});

		// import data
		actions.put("importData", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportData")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						dataTrackActionAsync(panel());
					}
				});

		// save current tab
		actions.put("save", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Save"), //$NON-NLS-1$
						Tracker.getResourceIcon("save.gif", true)) { // $NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerPanel trackerPanel = panel();
						TrackerIO.save(trackerPanel.getDataFile(), trackerPanel);
						trackerPanel.refreshNotesDialog();
					}
				});

		// save tab as
		actions.put("saveAs", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.SaveAs")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerPanel trackerPanel = panel();
						TrackerIO.save(null, trackerPanel);
						trackerPanel.refreshNotesDialog();
					}
				});

		// save zip resource (TRZ)
		actions.put("saveZip", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.SaveZip") + "...", //$NON-NLS-1$ //$NON-NLS-2$
						Tracker.getResourceIcon("save_zip.gif", true) //$NON-NLS-1$
				) {
					@Override
					public void actionPerformed(ActionEvent e) {
						ExportZipDialog.getDialog(panel()).setVisible(true);
					}
				});

		// save tabset as
		actions.put("saveTabsetAs", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.SaveFrame")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerPanel trackerPanel = panel();
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
						TrackerIO.saveVideo(null, panel(), false, true);
					}
				});

		// export XML file
		actions.put("export", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.exportXMLFile(panel());
					}
				});

		// delete track
		actions.put("deleteTrack", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Delete")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						// action command is name of track to delete
						TTrack track = panel().getTrack(e.getActionCommand());
						if (track != null)
							track.delete();
					}
				});

		actions.put("config", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Config")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {						
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
						panel().toggleAxesVisible();
					}
				});

		// videoFilter
		actions.put("videoFilter", //$NON-NLS-1$
				getAsyncAction(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						panel().addVideoFilter(e.getActionCommand());
					}
				}, true));

		// about video
		actions.put("aboutVideo", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.AboutVideo")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						
						if (frame != null) {
							TrackerPanel trackerPanel = panel();
							PropertiesDialog dialog = frame.getPropertiesDialog(trackerPanel);
							if (trackerPanel.getVideo() != null)
								dialog.tabbedPane.setSelectedIndex(trackerPanel.openedFromPath == null? 0: 1);
							dialog.setVisible(true);
						}
					}
				});

		// print
		actions.put("print", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Print")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						new TrackerIO.ComponentImage(panel()).print();
					}
				});

		// exit
		actions.put("exit", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.Exit")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						exitAction(panel());
					}
				});

		// new point mass
		actions.put("pointMass", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("PointMass.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						pointMassAction(panel());
					}
				}, true));

		// new center of mass
		actions.put("cm", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("CenterOfMass.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						cmAction(panel());
					}
				}, true));

		// new vector
		actions.put("vector", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("Vector.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						vectorAction(panel());
					}
				}, true));

		// new vector sum
		actions.put("vectorSum", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("VectorSum.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						vectorSumAction(panel());
					}
				}, true));

		// new offset origin item
		actions.put("offsetOrigin", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("OffsetOrigin.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						offsetOriginAction(panel());
					}
				}, true));

		// new calibration item
		actions.put("calibration", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("Calibration.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						calibrationAction(panel());
					}
				}, true));

		// new line profile item
		actions.put("lineProfile", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("LineProfile.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						lineProfileAction(panel());
					}
				}, true));

		// new RGBRegion item
		actions.put("rgbRegion", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("RGBRegion.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						rgbRegionAction(panel());
					}
				}, true));

		// new analytic particle item
		actions.put("analyticParticle", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("AnalyticParticle.Name")) {//$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						analyticalParticleAction(panel());
					}
				}, true));

		// new dynamic particle item
		actions.put("dynamicParticle", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("DynamicParticle.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						dynamicParticleAction(panel());
					}
				}, true));

		// new dynamic particle polar item
		actions.put("dynamicParticlePolar", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("DynamicParticlePolar.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						dynamicParticlePolarAction(panel());
					}
				}, true));

		// new dynamic system item
		actions.put("dynamicSystem", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("DynamicSystem.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						dynamicSystemAction(panel());
					}
				}, true));

		// new (read-only) tape measure
		actions.put("tape", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("TapeMeasure.Name")) {//$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						tapeAction(panel());
					}
				}, true));

		// new protractor
		actions.put("protractor", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("Protractor.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						protractorAction(panel());
					}
				}, true));

		// new circle track
		actions.put("circleFitter", //$NON-NLS-1$
				getAsyncAction(new AbstractAction(TrackerRes.getString("CircleFitter.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						circleFitterAction(panel());
					}
				}, true));
		// clone track action
		actions.put("cloneTrack", //$NON-NLS-1$
				getAsyncAction(new AbstractAction() { // $NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						cloneAction(panel(), e.getActionCommand());
					}
				}, true));

		// clear filters action
		actions.put("clearFilters", //$NON-NLS-1$
				new AbstractAction(// $NON-NLS-1$
						TrackerRes.getString("TActions.Action.ClearFilters")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						clearFiltersAction(panel(), true);
					}
				});

		// new DataTrack from text file item
		actions.put("dataTrack", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("ParticleDataTrack.Name")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						dataTrackActionAsync(panel());
					}
				});

		// open video
		actions.put("openVideo", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.ImportVideo")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerIO.importVideo(panel(), null);// , TrackerIO.NULL_RUNNABLE);
					}
				});

		// close video
		actions.put("closeVideo", //$NON-NLS-1$
				new AbstractAction(TrackerRes.getString("TActions.Action.CloseVideo")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						TrackerPanel trackerPanel = panel();
						trackerPanel.setVideo(null);
						TFrame.repaintT(trackerPanel);
						trackerPanel.setImageSize(640, 480);
						frame.refreshMenus(trackerPanel, TMenuBar.REFRESH_TACTIONS_OPENVIDEO);
					}
				});

		// reference frame
		actions.put("refFrame", //$NON-NLS-1$
				new AbstractAction() { // $NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						JMenuItem item = (JMenuItem) e.getSource();
						panel().setReferenceFrame(item.getActionCommand());
					}
				});

		return actions;
	}

	private static TTrack addTrack(TTrack t, TrackerPanel p) {
		t.setDefaultNameAndColor(p, " "); //$NON-NLS-1$
		p.addTrack(t);
		p.setSelectedPoint(null);
		p.selectedSteps.clear();
		p.setSelectedTrack(t);
		return t;
	}

	private static void addParticle(ParticleModel model, TrackerPanel trackerPanel, boolean isDynamic) {
		ModelBuilder builder = model.getModelBuilder();
		// builder may be null if model track not yet added to trackerPanel
		if (builder != null) {
			builder.setVisible(false);
		}
		SwingUtilities.invokeLater(() -> {
			addTrack(model, trackerPanel);
			model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
			if (isDynamic) {
				((DynamicSystem) model).getSystemInspector().setVisible(true);
			}
			model.getModelBuilder().refreshDropdown(model.getName());
			model.getModelBuilder().setVisible(true);
		});
	}

	protected static void analyticalParticleAction(TrackerPanel trackerPanel) {
		addParticle(new AnalyticParticle(), trackerPanel, false);
	}

	public static void dynamicParticleAction(TrackerPanel trackerPanel) {
		addParticle(new DynamicParticle(), trackerPanel, false);
	}

	public static void dynamicParticlePolarAction(TrackerPanel trackerPanel) {
		addParticle(new DynamicParticlePolar(), trackerPanel, false);
	}
	protected static void dynamicSystemAction(TrackerPanel trackerPanel) {
				addParticle(new DynamicSystem(), trackerPanel, true);
	}

	protected static void rgbRegionAction(TrackerPanel trackerPanel) {
		addTrack(new RGBRegion(), trackerPanel);
		if (!Tracker.markAtCurrentFrame) {
			trackerPanel.getPlayer().setStepNumber(0);
		}
	}

	protected static void lineProfileAction(TrackerPanel trackerPanel) {
		addTrack(new LineProfile(), trackerPanel);
	}

	protected static void calibrationAction(TrackerPanel trackerPanel) {
		addTrack(new Calibration(), trackerPanel);
		trackerPanel.getAxes().setVisible(true);
	}

	protected static void offsetOriginAction(TrackerPanel trackerPanel) {
		addTrack(new OffsetOrigin(), trackerPanel);
		trackerPanel.getAxes().setVisible(true);
	}

	protected static void vectorSumAction(TrackerPanel trackerPanel) {
		((VectorSum) addTrack(new VectorSum(), trackerPanel)).getInspector().setVisible(true);
	}

	protected static void vectorAction(TrackerPanel trackerPanel) {
		addTrack(new Vector(), trackerPanel);
		if (!Tracker.markAtCurrentFrame) {
			trackerPanel.getPlayer().setStepNumber(0);
		}
	}

	protected static void cmAction(TrackerPanel trackerPanel) {
		((CenterOfMass) addTrack(new CenterOfMass(), trackerPanel)).getInspector().setVisible(true);
	}

	protected static void tapeAction(TrackerPanel trackerPanel) {
		TapeMeasure tape = new TapeMeasure();
		tape.setReadOnly(true);
		// place tape at center of viewport
		MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
		Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
		int xpix = rect.x + rect.width / 2;
		int ypix = rect.y + rect.height / 2;
		double x = trackerPanel.pixToX(xpix);
		double y = trackerPanel.pixToY(ypix);
		tape.createStep(0, x - 100, y, x + 100, y); // length 200 image units
		addTrack(tape, trackerPanel);
		tape.getRuler().setVisible(true);
	}

	protected static void circleFitterAction(TrackerPanel trackerPanel) {
		addTrack(new CircleFitter(), trackerPanel);
	}

	protected static void protractorAction(TrackerPanel trackerPanel) {
		Protractor protractor = new Protractor();
		protractor.getRuler().setVisible(true);
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
		addTrack(protractor, trackerPanel);
		// move vertex AFTER adding to TrackerPanel
		step.moveVertexTo(x, y);
	}

	protected static void pointMassAction(TrackerPanel trackerPanel) {
		PointMass pointMass = new PointMass();
		addTrack(pointMass, trackerPanel);
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

	protected static void dataTrackActionAsync(TrackerPanel trackerPanel) {
		// choose file and import data
		TFrame frame = trackerPanel.getTFrame();
		TrackerIO.getChooserFilesAsync(frame, //$NON-NLS-1$
				"open data", (files) -> {
					if (files == null) {
						return null;
					}
					String filePath = files[0].getAbsolutePath();
					trackerPanel.importDataAsync(filePath, null, null);
					return null;
				});
	}

	public static void cloneAction(TrackerPanel trackerPanel, String name) {
		trackerPanel.cloneNamed(name);
	}

//	@SuppressWarnings("serial")
	/**
	 * Use SwingUtilities.invokeLater to ensure that any mouse action on a menu item has completed prior to this action's running.
	 * 
	 * @param a
	 * @param useSeparateThread
	 * @return
	 */
	private static AbstractAction getAsyncAction(AbstractAction a, boolean useSeparateThread) {
		Object nameObj = a.getValue(Action.NAME);
		String name = nameObj == null ? null : nameObj.toString();
//		Object iconObj = a.getValue(Action.SMALL_ICON);
//		Icon icon = iconObj == null ? null : (Icon) iconObj;
		return new AbstractAction(name) {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(()->{
					a.actionPerformed(e);
				});
//				new AsyncSwingWorker(null, null, 0, 0, 1) {
//
//					@Override
//					public void initAsync() {
//					}
//
//					@Override
//					public int doInBackgroundAsync(int i) {
//						if (this.getProgressAsync() > 0)
//							return 1;
//						if (useSeparateThread) {
//							AsyncSubtask task = this.new AsyncSubtask(1);
//							task.start(new Runnable() {
//
//								@Override
//								public void run() {
//									a.actionPerformed(e);
//									task.done();
//								}
//							});
//						} else {
//							a.actionPerformed(e);
//						}
//						return 1;
//					}
//
//					@Override
//					public void doneAsync() {
//					}
//
//				}.execute();
			}
		};
	}

	public static void clearFiltersAction(TrackerPanel trackerPanel, boolean andUndo) {
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
			if (andUndo) {
				Undo.postFilterClear(trackerPanel, xml);
			}
		}
	}

	public static void exitAction(TrackerPanel trackerPanel) {
		if (trackerPanel == null) {
			Tracker.exit();
			return;
		}
		TFrame frame = trackerPanel.getTFrame();
		if (frame != null) {
			frame.removeAllTabs(true);
		}
	}


}
