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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

//import org.opensourcephysics.media.mov.MovieVideoI;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.Filter;
import org.opensourcephysics.media.core.FilterStack;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.ImageVideo;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.media.mov.SmoothPlayable;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionTool;

import javajs.async.SwingJSUtils;
import javajs.async.SwingJSUtils.Performance;

/**
 * This is the main menu for Tracker.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TMenuBar extends JMenuBar implements PropertyChangeListener {

	// static fields
	private static Map<TrackerPanel, TMenuBar> menuBars = new HashMap<TrackerPanel, TMenuBar>();
	private static XMLControl control = new XMLControlElement();

	private boolean allowRefresh = true;
	
	public void setAllowRefresh(boolean b) {
		allowRefresh = b;
	}
	
	// instance fields
	private TrackerPanel trackerPanel;
	private TFrame frame;
	private Map<String, AbstractAction> actions;

	// file menu
	private JMenu fileMenu;
	private JMenuItem file_newTabItem;
	private JMenuItem file_openItem;
	private JMenuItem file_openBrowserItem;
	
	// used in TFrame -- check!!
	JMenu file_openRecentMenu;
	
	private JMenuItem file_closeItem;
	private JMenuItem file_closeAllItem;
	private JMenuItem file_saveItem;
	private JMenuItem file_saveAsItem;
	private JMenuItem file_saveZipAsItem;
//	private JMenuItem saveVideoAsItem;
	private JMenuItem file_saveTabsetAsItem;
	private JMenu file_importMenu;
	private JMenuItem file_import_videoItem;
	private JMenuItem file_import_TRKItem;
	private JMenuItem file_import_dataItem;
	private JMenu file_exportMenu;
	private JMenuItem file_export_zipItem;
	private JMenuItem file_export_videoItem;
	private JMenuItem file_export_TRKItem;
	private JMenuItem file_export_thumbnailItem;
	private JMenuItem file_export_dataItem;
//	private JMenuItem file_export_captureVideoItem;
	private JMenuItem file_propertiesItem;
	private JMenuItem file_printFrameItem;
	private JMenuItem file_exitItem;
	// edit menu
	private JMenu editMenu;
	private JMenuItem edit_undoItem;
	private JMenuItem edit_redoItem;
	private JMenu edit_copyDataMenu;
	private JMenu edit_copyImageMenu;
	private JMenuItem edit_copyMainViewImageItem;
	private JMenuItem edit_copyFrameImageItem;
	private JMenuItem[] edit_copyViewImageItems;
	private JMenu edit_copyObjectMenu;
	private JMenuItem edit_pasteItem;
	private JCheckBoxMenuItem edit_autopasteCheckbox;
	private JMenu edit_deleteTracksMenu;
	private JMenuItem edit_delTracks_deleteSelectedPointItem;
	private JMenuItem edit_clearTracksItem;
	private JMenu edit_numberMenu;
	private JMenuItem edit_formatsItem, edit_unitsItem;
	private JMenuItem edit_configItem;
	private JMenu edit_matSizeMenu;
	private ButtonGroup matSizeGroup;
	private JMenu edit_fontSizeMenu;
	private ButtonGroup fontSizeGroup;
	private JRadioButtonMenuItem edit_matsize_videoSizeItem;
	private JMenu edit_languageMenu;
	private JMenuItem[] languageItems;
	private JMenuItem edit_lang_otherLanguageItem;
	// video menu
	private JMenu videoMenu;
	private JCheckBoxMenuItem video_videoVisibleItem;
	private JMenuItem video_goToItem;
	private JMenu video_filtersMenu;
	private JMenu video_filter_newFilterMenu;
	private JMenuItem pasteFilterItem;
	private JMenuItem clearFiltersItem;
	private JMenuItem video_openVideoItem;
	private JMenuItem video_closeVideoItem;
	private JMenu video_pasteImageMenu;
	private JMenuItem video_pasteImageItem;
	private JMenuItem pasteReplaceItem;
	private JMenuItem video_pasteImageAfterItem;
	private JMenuItem video_pasteImageBeforeItem;
	private JMenu video_importImageMenu;
	private JMenuItem addImageAfterItem;
	private JMenuItem addImageBeforeItem;
	private JMenuItem video_removeImageItem;
	private JMenuItem video_editVideoItem;
	private JMenuItem video_playAllStepsItem;
	private JMenuItem video_playXuggleSmoothlyItem;
	private JMenuItem video_aboutVideoItem;
	private JMenuItem checkDurationsItem;
	private JMenuItem video_emptyVideoItem;
	// tracks menu
	private JMenu trackMenu;
	private JMenu track_createMenu; // Tracks/New
	private JMenu track_cloneMenu; // Tracks/Clone
	private JMenu measuringToolsMenu;
	private Component[] newTrackItems;
	private JMenuItem newPointMassItem;
	private JMenuItem newCMItem;
	private JMenuItem newVectorItem;
	private JMenuItem newVectorSumItem;
	private JMenuItem newLineProfileItem;
	private JMenuItem newRGBRegionItem;
	private JMenuItem newProtractorItem;
	private JMenuItem newTapeItem;
	private JMenuItem newCircleFitterItem;
	private JCheckBoxMenuItem axesVisibleItem;
	private JMenuItem newAnalyticParticleItem;
	private JMenu newDynamicParticleMenu;
	private JMenuItem newDynamicParticleCartesianItem;
	private JMenuItem newDynamicParticlePolarItem;
	private JMenuItem newDynamicSystemItem;
	private JMenu newDataTrackMenu;
	private JMenuItem newDataTrackPasteItem;
	private JMenuItem newDataTrackFromFileItem;
	private JMenuItem newDataTrackFromEJSItem;
	private JMenuItem dataTrackHelpItem;
	private JMenuItem video_emptyTracksItem;
	// coords menu
	private JMenu coordsMenu;
	private JCheckBoxMenuItem coords_lockedCoordsItem;
	private JCheckBoxMenuItem coords_fixedOriginItem;
	private JCheckBoxMenuItem coords_fixedAngleItem;
	private JCheckBoxMenuItem coords_fixedScaleItem;
	private JMenu coords_refFrameMenu;
	private ButtonGroup refFrameGroup;
	private JRadioButtonMenuItem defaultRefFrameItem;
	private JMenuItem coords_showUnitDialogItem;
	private JMenuItem coords_emptyCoordsItem;
	// window menu
	private JMenu windowMenu;
	private JMenuItem restoreItem;
	private JCheckBoxMenuItem rightPaneItem;
	private JCheckBoxMenuItem bottomPaneItem;
	private JMenuItem trackControlItem;
	private JMenuItem notesItem;
	private JMenuItem dataBuilderItem;
	private JMenuItem dataToolItem;
	// help menu
	private JMenu helpMenu;
	// other fields
	boolean refreshing; // true when refreshing menus or redoing filter delete

	/**
	 * Returns a TMenuBar for the specified trackerPanel.
	 *
	 * @param panel the tracker panel
	 * @return a TMenuBar. May return null during instantiation.
	 */
	public static TMenuBar getMenuBar(TrackerPanel panel) {
		if (panel == null) 
			return null;
		synchronized (menuBars) {
			if (!menuBars.containsKey(panel)) {
				menuBars.put(panel, new TMenuBar(panel));
			}
		}
		return menuBars.get(panel);
	}

	/**
	 * Returns a new TMenuBar for the specified trackerPanel.
	 * @param frame 
	 *
	 * @param panel the tracker panel
	 * @return a TMenuBar
	 */
	public static void newMenuBar(TFrame frame, TrackerPanel panel) {
		TMenuBar menuBar = new TMenuBar(panel);
		frame.setMenuBar(panel, menuBar);

		synchronized (menuBars) {
			menuBars.put(panel, menuBar);
		}
	}

	protected void loadVideoMenu(JMenu vidMenu) {
		/** empty block */
	}

	/**
	 * Clears all menubars. This forces creation of new menus using new locale.
	 */
	public static void clear() {
		synchronized (menuBars) {
			menuBars.clear();
		}
	}

	/**
	 * Constructor specifying the tracker panel.
	 *
	 * @param panel the tracker panel
	 */
	private TMenuBar(TrackerPanel panel) {
		setTrackerPanel(panel);
		actions = TActions.getActions(panel);
		createGUI();
		//BH first of three? refresh();
	}

	/**
	 * Sets the TrackerPanel for this menu bar
	 *
	 * @param panel the new drawing panel
	 */
	protected void setTrackerPanel(TrackerPanel panel) {
		if (panel == null || panel == trackerPanel)
			return;
		if (trackerPanel != null) {
			trackerPanel.removePropertyChangeListener("locked", this); //$NON-NLS-1$
			trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); //$NON-NLS-1$
			trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); //$NON-NLS-1$
			trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
			trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
			trackerPanel.removePropertyChangeListener("video", this); //$NON-NLS-1$
			trackerPanel.removePropertyChangeListener("size", this); //$NON-NLS-1$
			trackerPanel.removePropertyChangeListener("datafile", this); //$NON-NLS-1$
		}
		trackerPanel = panel;
		trackerPanel.addPropertyChangeListener("locked", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("video", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("size", this); //$NON-NLS-1$
		trackerPanel.addPropertyChangeListener("datafile", this); //$NON-NLS-1$
	}

	/**
	 * Creates the menu bar.
	 */
	protected void createGUI() {
		int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		// file menu
		fileMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.File")); //$NON-NLS-1$
		add(fileMenu);
		fileMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuSelected(MenuEvent e) {	
				// ignore when menu is about to close
				if (!fileMenu.isPopupMenuVisible())
					return;
				// disable export data menu if no tables to export
				// DB getDataViews() only changes when a TableTrackView is displayed/hidden
				file_export_dataItem.setEnabled(!getDataViews().isEmpty());
				// disable saveTabsetAs item if only 1 tab is open
				TFrame frame = trackerPanel.getTFrame();
				// DB changes when tab is opened or closed
				file_saveTabsetAsItem.setEnabled(frame != null && frame.getTabCount() > 1);
			}
		});
		if (org.opensourcephysics.display.OSPRuntime.applet == null) {
			// new tab item
			file_newTabItem = new JMenuItem(actions.get("newTab")); //$NON-NLS-1$
			file_newTabItem.setAccelerator(KeyStroke.getKeyStroke('N', keyMask));
			fileMenu.addSeparator();
			// open item
			file_openItem = new JMenuItem(actions.get("open")); //$NON-NLS-1$
			file_openItem.setAccelerator(KeyStroke.getKeyStroke('O', keyMask));
			// open library browser item
			file_openBrowserItem = new JMenuItem(actions.get("openBrowser")); //$NON-NLS-1$
			// open recent
			file_openRecentMenu = new JMenu();
			// import menu
			file_importMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Import")); //$NON-NLS-1$
			file_import_videoItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Video")); //$NON-NLS-1$
			file_import_videoItem.addActionListener(actions.get("openVideo")); //$NON-NLS-1$
			file_import_videoItem.setAccelerator(KeyStroke.getKeyStroke('I', keyMask));
			file_import_TRKItem = new JMenuItem(actions.get("import")); //$NON-NLS-1$
			file_import_dataItem = new JMenuItem(actions.get("importData")); //$NON-NLS-1$
			file_importMenu.add(file_import_videoItem);
			file_importMenu.add(file_import_TRKItem);
			file_importMenu.add(file_import_dataItem);
			// close and close all items
			file_closeItem = new JMenuItem(actions.get("close")); //$NON-NLS-1$
			file_closeAllItem = new JMenuItem(actions.get("closeAll")); //$NON-NLS-1$
			fileMenu.addSeparator();
			// export menu
			file_exportMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Export")); //$NON-NLS-1$
			// export zip item
			file_export_zipItem = new JMenuItem(actions.get("saveZip")); //$NON-NLS-1$
			file_export_zipItem.setText(TrackerRes.getString("TMenuBar.MenuItem.ExportZIP") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
//      exportMenu.add(exportZipItem);
			// export video item
			file_export_videoItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.VideoClip") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
			file_export_videoItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ExportVideoDialog exporter = ExportVideoDialog.getDialog(trackerPanel);
					exporter.setVisible(true);
				}
			});
			file_exportMenu.add(file_export_videoItem);
			// export TRK item
			file_export_TRKItem = new JMenuItem(actions.get("export")); //$NON-NLS-1$
			file_exportMenu.add(file_export_TRKItem);
			// export thumbnail item
			file_export_thumbnailItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Thumbnail") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
			file_export_thumbnailItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ThumbnailDialog exporter = ThumbnailDialog.getDialog(trackerPanel, true);
					exporter.setVisible(true);
				}
			});
			file_exportMenu.add(file_export_thumbnailItem);
			// export data item
			file_export_dataItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Data")); //$NON-NLS-1$
			file_export_dataItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ExportDataDialog exporter = ExportDataDialog.getDialog(trackerPanel);
					exporter.setVisible(true);
				}
			});
			file_exportMenu.add(file_export_dataItem);
			fileMenu.addSeparator();
			// save item
			file_saveItem = new JMenuItem(actions.get("save")); //$NON-NLS-1$
			file_saveItem.setAccelerator(KeyStroke.getKeyStroke('S', keyMask));
			// saveAs item
			file_saveAsItem = new JMenuItem(actions.get("saveAs")); //$NON-NLS-1$
			// save zip item
			file_saveZipAsItem = new JMenuItem(actions.get("saveZip")); //$NON-NLS-1$
			// saveVideoAs item
//			saveVideoAsItem = new JMenuItem(actions.get("saveVideo")); //$NON-NLS-1$
			// saveTabset item
			file_saveTabsetAsItem = new JMenuItem(actions.get("saveTabsetAs")); //$NON-NLS-1$
			fileMenu.addSeparator();
		}
		// properties item
		file_propertiesItem = new JMenuItem(actions.get("properties")); //$NON-NLS-1$
		// printFrame item
		file_printFrameItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PrintFrame")); //$NON-NLS-1$
		file_printFrameItem.setAccelerator(KeyStroke.getKeyStroke('P', keyMask));
		file_printFrameItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Component c = trackerPanel.getTFrame();
				new TrackerIO.ComponentImage(c).print();
			}
		});
		// exit item
		if (org.opensourcephysics.display.OSPRuntime.applet == null) {
			file_exitItem = new JMenuItem(actions.get("exit")); //$NON-NLS-1$
			file_exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', keyMask));
		}
		// edit menu
		editMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Edit"));//$NON-NLS-1$
		editMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuSelected(MenuEvent e) {	
				setupEditMenu();
			}
		});
		add(editMenu);
		// undo/redo items
		edit_undoItem = new JMenuItem();
		edit_undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', keyMask));
		edit_undoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				trackerPanel.setSelectedPoint(null);
				trackerPanel.selectedSteps.clear();
				Undo.undo(trackerPanel);
			}
		});
		edit_redoItem = new JMenuItem();
		edit_redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', keyMask));
		edit_redoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Undo.redo(trackerPanel);
				trackerPanel.setSelectedPoint(null);
				trackerPanel.selectedSteps.clear();
			}
		});
		// paste items
		edit_pasteItem = editMenu.add(actions.get("paste")); //$NON-NLS-1$
		edit_pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', keyMask));
		editMenu.addSeparator();
		// autopaste checkbox
		edit_autopasteCheckbox = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.Checkbox.Autopaste")); //$NON-NLS-1$
		edit_autopasteCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				trackerPanel.getTFrame().alwaysListenToClipboard = edit_autopasteCheckbox.isSelected();
				trackerPanel.getTFrame().checkClipboardListener();
			}
		});
		// copy data menu
		edit_copyDataMenu = new JMenu();

		// copy image menu
		edit_copyImageMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.CopyImage")); //$NON-NLS-1$
		edit_copyFrameImageItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CopyFrame")); //$NON-NLS-1$
		edit_copyFrameImageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Component c = trackerPanel.getTFrame();
				new TrackerIO.ComponentImage(c).copyToClipboard();
			}
		});
		edit_copyMainViewImageItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CopyMainView") + " (0)"); //$NON-NLS-1$ //$NON-NLS-2$
		edit_copyMainViewImageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new TrackerIO.ComponentImage(trackerPanel).copyToClipboard();
			}
		});
		edit_copyImageMenu.add(edit_copyMainViewImageItem);
		edit_copyViewImageItems = new JMenuItem[0];

		// copy object menu
		edit_copyObjectMenu = new JMenu();

		// pasteImage menu
		video_pasteImageMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.PasteImage")); //$NON-NLS-1$

		// pasteImage item
		ActionListener pasteImageAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Image image = TrackerIO.getClipboardImage();
				if (image != null) {
					Video video = new ImageVideo(image);
					trackerPanel.setVideo(video);
					// set step number to show image in all frames
					int n = trackerPanel.getPlayer().getVideoClip().getStepCount();
					trackerPanel.getPlayer().getVideoClip().setStepCount(n);
				}
			}
		};
		video_pasteImageItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PasteImage")); //$NON-NLS-1$
		video_pasteImageItem.addActionListener(pasteImageAction);
		// editVideoItem and saveEditsVideoItem
		video_editVideoItem = new JCheckBoxMenuItem(
				new AbstractAction(TrackerRes.getString("TMenuBar.MenuItem.EditVideoFrames")) { //$NON-NLS-1$
					@Override
					public void actionPerformed(ActionEvent e) {
						Video video = trackerPanel.getVideo();
						if (video != null && video instanceof ImageVideo) {
							boolean edit = video_editVideoItem.isSelected();
							if (!edit) {
								// convert video to non-editable?
								ImageVideo iVideo = (ImageVideo) video;
								try {
									iVideo.setEditable(false);
									refresh("menuItem.editVideoFrames !edit");
									TTrackBar.refreshMemoryButton();
								} catch (Exception e1) {
									Toolkit.getDefaultToolkit().beep();
								}
							} else {
								// warn user that memory requirements may be large
								String message = TrackerRes.getString("TMenuBar.Dialog.RequiresMemory.Message1"); //$NON-NLS-1$
								message += "\n" + TrackerRes.getString("TMenuBar.Dialog.RequiresMemory.Message2"); //$NON-NLS-1$ //$NON-NLS-2$
								int response = javax.swing.JOptionPane.showConfirmDialog(trackerPanel.getTFrame(),
										message, TrackerRes.getString("TMenuBar.Dialog.RequiresMemory.Title"), //$NON-NLS-1$
										javax.swing.JOptionPane.OK_CANCEL_OPTION,
										javax.swing.JOptionPane.INFORMATION_MESSAGE);
								if (response == javax.swing.JOptionPane.YES_OPTION) {
									boolean error = false;
									// convert video to editable
									ImageVideo iVideo = (ImageVideo) video;
									try {
										iVideo.setEditable(true);
										refresh("memory_issue");
										TTrackBar.refreshMemoryButton();
									} catch (Exception ex) {
										Toolkit.getDefaultToolkit().beep();
										error = true;
									} catch (Error er) {
										Toolkit.getDefaultToolkit().beep();
										error = true;
										throw (er);
									} finally {
										if (error) {
											// try to revert to non-editable
											try {
												iVideo.setEditable(false);
											} catch (Exception ex) {
											} catch (Error er) {
											}
											System.gc();
											refresh("memory error");
											TTrackBar.refreshMemoryButton();
										}
									}
								} else { // user canceled
									video_editVideoItem.setSelected(false);
								}
							}
						}
					}
				});

		// pasteReplace item
		pasteReplaceItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PasteReplace")); //$NON-NLS-1$
		pasteReplaceItem.addActionListener(pasteImageAction);
		// pasteAfter item
		video_pasteImageAfterItem = new JMenuItem(new AbstractAction(TrackerRes.getString("TMenuBar.MenuItem.PasteAfter")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				Image image = TrackerIO.getClipboardImage();
				if (image != null) {
					int n = trackerPanel.getFrameNumber();
					ImageVideo imageVid = (ImageVideo) trackerPanel.getVideo();
					imageVid.insert(image, n + 1);
					VideoClip clip = trackerPanel.getPlayer().getVideoClip();
					clip.setStepCount(imageVid.getFrameCount());
					trackerPanel.getPlayer().setStepNumber(clip.frameToStep(n + 1));
					refresh("menuItem.pageInsertAfter");
				}
			}
		});
		// pasteBefore item
		video_pasteImageBeforeItem = new JMenuItem(new AbstractAction(TrackerRes.getString("TMenuBar.MenuItem.PasteBefore")) { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				Image image = TrackerIO.getClipboardImage();
				if (image != null) {
					int n = trackerPanel.getFrameNumber();
					ImageVideo imageVid = (ImageVideo) trackerPanel.getVideo();
					imageVid.insert(image, n);
					VideoClip clip = trackerPanel.getPlayer().getVideoClip();
					clip.setStepCount(imageVid.getFrameCount());
					trackerPanel.getPlayer().setStepNumber(clip.frameToStep(n));
					refresh("menuItem.pastImageBefore");
				}
			}
		});
		video_pasteImageMenu.add(pasteReplaceItem);
		// delete selected point item
		edit_delTracks_deleteSelectedPointItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.DeleteSelectedPoint")); //$NON-NLS-1$
		edit_delTracks_deleteSelectedPointItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				trackerPanel.deletePoint(trackerPanel.getSelectedPoint());
			}
		});
		// delete tracks menu
		edit_deleteTracksMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.DeleteTrack")); //$NON-NLS-1$
		editMenu.add(edit_deleteTracksMenu);
		editMenu.addSeparator();
		// clear tracks item
		edit_clearTracksItem = edit_deleteTracksMenu.add(actions.get("clearTracks")); //$NON-NLS-1$
		// config item
		edit_configItem = editMenu.add(actions.get("config")); //$NON-NLS-1$
		edit_configItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, keyMask));
		// number menu
		edit_numberMenu = new JMenu(TrackerRes.getString("Popup.Menu.Numbers")); //$NON-NLS-1$
		edit_formatsItem = new JMenuItem(TrackerRes.getString("Popup.MenuItem.Formats") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		edit_formatsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TTrack track = trackerPanel.getSelectedTrack();
				NumberFormatDialog dialog = NumberFormatDialog.getNumberFormatDialog(trackerPanel, track, null);
				dialog.setVisible(true);
			}
		});
		edit_unitsItem = new JMenuItem(TrackerRes.getString("Popup.MenuItem.Units") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		edit_unitsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitsDialog dialog = trackerPanel.getUnitsDialog();
				dialog.setVisible(true);
			}
		});
		// size menu
		edit_matSizeMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.MatSize")); //$NON-NLS-1$
		final String[] sizes = new String[] { "320x240", //$NON-NLS-1$
				"480x360", //$NON-NLS-1$
				"640x480", //$NON-NLS-1$
				"800x600", //$NON-NLS-1$
				"960x720", //$NON-NLS-1$
				"1280x960" }; //$NON-NLS-1$
		Action matSizeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String theSize = e.getActionCommand();
				int i = theSize.indexOf("x"); //$NON-NLS-1$
				double w = Double.parseDouble(theSize.substring(0, i));
				double h = Double.parseDouble(theSize.substring(i + 1));
				trackerPanel.setImageSize(w, h);
			}
		};
		matSizeGroup = new ButtonGroup();
		edit_fontSizeMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.FontSize")); //$NON-NLS-1$		
		fontSizeGroup = new ButtonGroup();
		Action fontSizeAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int i = Integer.parseInt(e.getActionCommand());
				FontSizer.setLevel(i);
			}

		};
		for (int i = 0; i <= Tracker.maxFontLevel; i++) {
			String s = i == 0 ? TrackerRes.getString("TMenuBar.MenuItem.DefaultFontSize") : "+" + i; //$NON-NLS-1$ //$NON-NLS-2$
			JMenuItem item = new JRadioButtonMenuItem(s);
			item.addActionListener(fontSizeAction);
			item.setActionCommand(String.valueOf(i));
			edit_fontSizeMenu.add(item);
			fontSizeGroup.add(item);
			if (i == FontSizer.getLevel()) {
				item.setSelected(true);
			}
		}
		edit_matsize_videoSizeItem = new JRadioButtonMenuItem();
		edit_matsize_videoSizeItem.setActionCommand("0x0"); //$NON-NLS-1$
		edit_matsize_videoSizeItem.addActionListener(matSizeAction);
		matSizeGroup.add(edit_matsize_videoSizeItem);
		for (int i = 0; i < sizes.length; i++) {
			JMenuItem item = new JRadioButtonMenuItem(sizes[i]);
			item.setActionCommand(sizes[i]);
			item.addActionListener(matSizeAction);
			matSizeGroup.add(item);
		}
		// language menu
		edit_languageMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.Language")); //$NON-NLS-1$
		// set up language menu
		Action languageAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String language = e.getActionCommand();
				for (int i = 0; i < Tracker.incompleteLocales.length; i++) {
					if (language.equals(Tracker.incompleteLocales[i][0].toString())) {
						Locale locale = (Locale) Tracker.incompleteLocales[i][0];
						String lang = OSPRuntime.getDisplayLanguage(locale);
						// the following message is purposely not translated
						JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
								"This translation has not been updated since " + Tracker.incompleteLocales[i][1] //$NON-NLS-1$
										+ ".\nIf you speak " + lang + " and would like to help translate" //$NON-NLS-1$ //$NON-NLS-2$
										+ "\nplease contact Douglas Brown at dobrown@cabrillo.edu.", //$NON-NLS-1$
								"Incomplete Translation: " + lang, //$NON-NLS-1$
								JOptionPane.WARNING_MESSAGE);
					}
				}
				for (int i = 0; i < Tracker.locales.length; i++) {
					if (language.equals(Tracker.locales[i].toString())) {
						TrackerRes.setLocale(Tracker.locales[i]);
						return;
					}
				}
			}
		};
		ButtonGroup languageGroup = new ButtonGroup();
		languageItems = new JMenuItem[Tracker.locales.length];
		String currentLocale = TrackerRes.locale.toString();
		if (currentLocale.startsWith("en_")) { //$NON-NLS-1$
			// strip country from english so english language item will be recognized below
			currentLocale = "en"; //$NON-NLS-1$
		}
		for (int i = 0; i < languageItems.length; i++) {
			String lang = OSPRuntime.getDisplayLanguage(Tracker.locales[i]);
			// special handling for portuguese BR and PT
			if (Tracker.locales[i].getLanguage().equals("pt")) { //$NON-NLS-1$
				lang += " (" + Tracker.locales[i].getCountry() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			languageItems[i] = new JRadioButtonMenuItem(lang);
			languageItems[i].setActionCommand(Tracker.locales[i].toString());
			languageItems[i].addActionListener(languageAction);
			languageGroup.add(languageItems[i]);
			if (Tracker.locales[i].toString().equals(currentLocale))
				languageItems[i].setSelected(true);
		}
		for (int i = 0; i < languageItems.length; i++) {
			edit_languageMenu.add(languageItems[i]);
		}

		// add "other" language item at end
		// the following item and message is purposely not translated
		edit_lang_otherLanguageItem = new JMenuItem("Other"); //$NON-NLS-1$
		edit_languageMenu.add(edit_lang_otherLanguageItem);
		edit_lang_otherLanguageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
						"Do you speak a language not yet available in Tracker?" //$NON-NLS-1$
								+ "\nTo learn more about translating Tracker into your language" //$NON-NLS-1$
								+ "\nplease contact Douglas Brown at dobrown@cabrillo.edu.", //$NON-NLS-1$
						"New Translation", //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// create new track menu
		track_createMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewTrack")); //$NON-NLS-1$
		newPointMassItem = new JMenuItem(actions.get("pointMass")); //$NON-NLS-1$
		newCMItem = new JMenuItem(actions.get("cm")); //$NON-NLS-1$
		newVectorItem = new JMenuItem(actions.get("vector")); //$NON-NLS-1$
		newVectorSumItem = new JMenuItem(actions.get("vectorSum")); //$NON-NLS-1$
//    newOffsetItem = new JMenuItem(actions.get("offsetOrigin")); //$NON-NLS-1$
//    newCalibrationPointsItem = new JMenuItem(actions.get("calibration")); //$NON-NLS-1$
		newLineProfileItem = new JMenuItem(actions.get("lineProfile")); //$NON-NLS-1$
		newRGBRegionItem = new JMenuItem(actions.get("rgbRegion")); //$NON-NLS-1$
		newProtractorItem = new JMenuItem(actions.get("protractor")); //$NON-NLS-1$
		newTapeItem = new JMenuItem(actions.get("tape")); //$NON-NLS-1$
		newCircleFitterItem = new JMenuItem(actions.get("circleFitter")); //$NON-NLS-1$
		// clone track menu
		track_cloneMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.Clone")); //$NON-NLS-1$
		// measuring tools menu
		measuringToolsMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.MeasuringTools")); //$NON-NLS-1$
		// video menu
		videoMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Video")); //$NON-NLS-1$
		videoMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuSelected(MenuEvent e) {	
				setupVideoMenu();
			}
		});
		add(videoMenu);
		// open and close video items
		video_openVideoItem = videoMenu.add(actions.get("openVideo")); //$NON-NLS-1$
		video_closeVideoItem = videoMenu.add(actions.get("closeVideo")); //$NON-NLS-1$

		// goTo item
		video_goToItem = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.Menu.GoTo") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		video_goToItem.setAccelerator(KeyStroke.getKeyStroke('G', keyMask));
		video_goToItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VideoPlayer player = trackerPanel.getPlayer();
				player.showGoToDialog();
			}
		});

		// image video items
		video_importImageMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.AddImage")); //$NON-NLS-1$
		addImageAfterItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.AddAfter")); //$NON-NLS-1$
		addImageAfterItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int n = trackerPanel.getFrameNumber();
				int step = trackerPanel.getPlayer().getStepNumber();
				java.io.File[] files = TrackerIO.insertImagesIntoVideo(trackerPanel, n + 1);
				if (files != null) {
					String[] paths = new String[files.length];
					for (int i = 0; i < paths.length; i++) {
						paths[i] = files[i].getPath();
					}
					Undo.postImageVideoEdit(trackerPanel, paths, n + 1, step, true);
				}
				refresh("menuItem.addAfter");
			}
		});
		addImageBeforeItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.AddBefore")); //$NON-NLS-1$
		addImageBeforeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int n = trackerPanel.getFrameNumber();
				int step = trackerPanel.getPlayer().getStepNumber();
				java.io.File[] files = TrackerIO.insertImagesIntoVideo(trackerPanel, n);
				if (files != null) {
					String[] paths = new String[files.length];
					for (int i = 0; i < paths.length; i++) {
						paths[i] = files[i].getPath();
					}
					Undo.postImageVideoEdit(trackerPanel, paths, n, step, true);
				}
				refresh("menuItem.addBefore");
			}
		});
		video_importImageMenu.add(addImageBeforeItem);
		video_importImageMenu.add(addImageAfterItem);
		video_removeImageItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.RemoveImage")); //$NON-NLS-1$
		video_removeImageItem.setAccelerator(KeyStroke.getKeyStroke('R', keyMask));
		video_removeImageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImageVideo imageVid = (ImageVideo) trackerPanel.getVideo();
				int n = trackerPanel.getFrameNumber();
				String path = imageVid.remove(n);
				int len = imageVid.getFrameCount();
				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
				clip.setStepCount(len);
				int step = Math.min(n, len - 1);
				step = clip.frameToStep(step);
				trackerPanel.getPlayer().setStepNumber(step);
				if (path != null && !path.equals("")) //$NON-NLS-1$
					Undo.postImageVideoEdit(trackerPanel, new String[] { path }, n, step, false);
				refresh("menuItem.removeImage");
			}
		});
		// play all steps item
		video_playAllStepsItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PlayAllSteps"), //$NON-NLS-1$
				true);
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		video_playAllStepsItem.setSelected(clip.isPlayAllSteps());
		video_playAllStepsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VideoPlayer player = trackerPanel.getPlayer();
				VideoClip clip = player.getVideoClip();
				clip.setPlayAllSteps(video_playAllStepsItem.isSelected());
				player.setVideoClip(clip);
			}
		});
		// video visible item
		video_videoVisibleItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.VideoVisible")); //$NON-NLS-1$
		video_videoVisibleItem.setSelected(true);
		video_videoVisibleItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Video video = trackerPanel.getVideo();
				if (e.getStateChange() != ItemEvent.SELECTED && e.getStateChange() != ItemEvent.DESELECTED
						|| video == null)
					return;
				boolean visible = video_videoVisibleItem.isSelected();
				trackerPanel.setVideoVisible(visible);
			}
		});
		// play xuggle smoothly item
		video_playXuggleSmoothlyItem = new JCheckBoxMenuItem(TrackerRes.getString("XuggleVideo.MenuItem.SmoothPlay")); //$NON-NLS-1$
		video_playXuggleSmoothlyItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Video video = trackerPanel.getVideo();
				if (video instanceof SmoothPlayable) {
					if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
						((SmoothPlayable) video).setSmoothPlay(video_playXuggleSmoothlyItem.isSelected());
					}
				}
			}
		});
		// checkDurationsItem
		checkDurationsItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CheckFrameDurations") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		checkDurationsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		      	// show dialog always but with no "don't show again" button
				TrackerIO.findBadVideoFrames(trackerPanel, TrackerIO.defaultBadFrameTolerance, 
      			true, false, false); 
			}
		});
		// about video item
		video_aboutVideoItem = videoMenu.add(actions.get("aboutVideo")); //$NON-NLS-1$
		// filters and addFilter menus
		video_filtersMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.VideoFilters")); //$NON-NLS-1$
		video_filter_newFilterMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewVideoFilter")); //$NON-NLS-1$
		video_filtersMenu.add(video_filter_newFilterMenu);
		video_filtersMenu.addSeparator();
		// paste filter item
		pasteFilterItem = new JMenuItem(TrackerRes.getString("TActions.Action.Paste")); //$NON-NLS-1$
		pasteFilterItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String xml = DataTool.paste();
				XMLControl control = new XMLControlElement(xml);
				Video video = trackerPanel.getVideo();
				FilterStack stack = video.getFilterStack();
				Filter filter = (Filter) control.loadObject(null);
				stack.addFilter(filter);
				filter.setVideoPanel(trackerPanel);
			}
		});
		// clear filters item
		clearFiltersItem = video_filtersMenu.add(actions.get("clearFilters")); //$NON-NLS-1$
		// track menu
		trackMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Tracks")); //$NON-NLS-1$
		trackMenu.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
//BH!				refresh("menuItem.trackMenu.stateChanged");
			}
		});
		trackMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuSelected(MenuEvent e) {	
				// ignore when menu is about to close
				if (!trackMenu.isPopupMenuVisible())
					return;
				
				// DB createMenu is empty ONLY if all track types are excluded in Preferences--VERY rare
				if (track_createMenu.getItemCount() > 0)
					trackMenu.add(track_createMenu, 0);
				
				// disable newDataTrackPasteItem unless pastable data is on the clipboard
				// DB this only needs checking when clipboard contents have changed
				newDataTrackPasteItem.setEnabled(false);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable data = clipboard.getContents(null);
				if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						String s = (String) data.getTransferData(DataFlavor.stringFlavor);
						newDataTrackPasteItem.setEnabled(ParticleDataTrack.getImportableDataName(s) != null);
					} catch (Exception ex) {
					}
				}
			}
		});
		add(trackMenu);
		// axes visible item
		axesVisibleItem = new JCheckBoxMenuItem(actions.get("axesVisible")); //$NON-NLS-1$
		// coords menu
		coordsMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Coords")); //$NON-NLS-1$
		coordsMenu.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
//BH!				refresh("menuItem.coordsMenu.stateChanged");
			}
		});
		add(coordsMenu);

		// units item
		coords_showUnitDialogItem = new JMenuItem(TrackerRes.getString("Popup.MenuItem.Units") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		coordsMenu.add(coords_showUnitDialogItem);
		coordsMenu.addSeparator();
		coords_showUnitDialogItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitsDialog dialog = trackerPanel.getUnitsDialog();
				dialog.setVisible(true);
			}
		});

		// locked coords item
		coords_lockedCoordsItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsLocked")); //$NON-NLS-1$
		coordsMenu.add(coords_lockedCoordsItem);
		coords_lockedCoordsItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				ImageCoordSystem coords = trackerPanel.getCoords();
				coords.setLocked(coords_lockedCoordsItem.isSelected());
			}
		});
		coordsMenu.addSeparator();
		// fixed origin item
		coords_fixedOriginItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsFixedOrigin")); //$NON-NLS-1$
		coords_fixedOriginItem.setSelected(true);
		coordsMenu.add(coords_fixedOriginItem);
		coords_fixedOriginItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int n = trackerPanel.getFrameNumber();
				ImageCoordSystem coords = trackerPanel.getCoords();
				XMLControl currentState = new XMLControlElement(trackerPanel.getCoords());
				coords.setFixedOrigin(coords_fixedOriginItem.isSelected(), n);
				if (!refreshing)
					Undo.postCoordsEdit(trackerPanel, currentState);
			}
		});
		// fixed angle item
		coords_fixedAngleItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsFixedAngle")); //$NON-NLS-1$
		coords_fixedAngleItem.setSelected(true);
		coordsMenu.add(coords_fixedAngleItem);
		coords_fixedAngleItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int n = trackerPanel.getFrameNumber();
				ImageCoordSystem coords = trackerPanel.getCoords();
				XMLControl currentState = new XMLControlElement(trackerPanel.getCoords());
				coords.setFixedAngle(coords_fixedAngleItem.isSelected(), n);
				if (!refreshing)
					Undo.postCoordsEdit(trackerPanel, currentState);
			}
		});
		// fixed scale item
		coords_fixedScaleItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsFixedScale")); //$NON-NLS-1$
		coords_fixedScaleItem.setSelected(true);
		coordsMenu.add(coords_fixedScaleItem);
		coords_fixedScaleItem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int n = trackerPanel.getFrameNumber();
				ImageCoordSystem coords = trackerPanel.getCoords();
				XMLControl currentState = new XMLControlElement(trackerPanel.getCoords());
				coords.setFixedScale(coords_fixedScaleItem.isSelected(), n);
				if (!refreshing)
					Undo.postCoordsEdit(trackerPanel, currentState);
			}
		});
		coordsMenu.addSeparator();
//    applyCurrentFrameToAllItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.AllFramesLikeCurrent")); //$NON-NLS-1$
//    coordsMenu.add(applyCurrentFrameToAllItem);
//    applyCurrentFrameToAllItem.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        int n = trackerPanel.getFrameNumber();
//        ImageCoordSystem coords = trackerPanel.getCoords();
//        coords.setAllValuesToFrame(n);
//      }
//    });
		coordsMenu.addSeparator();

		// reference frame menu
		coords_refFrameMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.CoordsRefFrame")); //$NON-NLS-1$
		coordsMenu.add(coords_refFrameMenu);
		// reference frame radio button group
		refFrameGroup = new ButtonGroup();
		defaultRefFrameItem = new JRadioButtonMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsDefault"), true); //$NON-NLS-1$
		defaultRefFrameItem.addActionListener(actions.get("refFrame")); //$NON-NLS-1$
		// model particles
		newAnalyticParticleItem = new JMenuItem(TrackerRes.getString("AnalyticParticle.Name")); //$NON-NLS-1$
		newAnalyticParticleItem.addActionListener(actions.get("analyticParticle")); //$NON-NLS-1$
		newDynamicParticleMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.DynamicParticle")); //$NON-NLS-1$
		newDynamicParticleCartesianItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Cartesian")); //$NON-NLS-1$
		newDynamicParticleCartesianItem.addActionListener(actions.get("dynamicParticle")); //$NON-NLS-1$
		newDynamicParticlePolarItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Polar")); //$NON-NLS-1$
		newDynamicParticlePolarItem.addActionListener(actions.get("dynamicParticlePolar")); //$NON-NLS-1$
		newDynamicSystemItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.TwoBody")); //$NON-NLS-1$
		newDynamicSystemItem.addActionListener(actions.get("dynamicSystem")); //$NON-NLS-1$
		newDataTrackMenu = new JMenu(TrackerRes.getString("ParticleDataTrack.Name")); //$NON-NLS-1$
		newDataTrackFromFileItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.DataFile") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		newDataTrackFromFileItem.addActionListener(actions.get("dataTrack")); //$NON-NLS-1$
		newDataTrackFromEJSItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.EJS") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		newDataTrackFromEJSItem.addActionListener(actions.get("dataTrackFromEJS")); //$NON-NLS-1$
		newDataTrackPasteItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Clipboard")); //$NON-NLS-1$
		newDataTrackPasteItem.addActionListener(actions.get("paste")); //$NON-NLS-1$
		dataTrackHelpItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.DataTrackHelp")); //$NON-NLS-1$
		dataTrackHelpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getFrame().showHelp("datatrack", 0); //$NON-NLS-1$
			}
		});
		// window menu
		windowMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Window")); //$NON-NLS-1$
		windowMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuSelected(MenuEvent e) {	
				// DB refreshWindowMenu needs work--currently totally rebuilds entire Window menu
				refreshWindowMenu();
				FontSizer.setFonts(windowMenu);
			}
		});
		add(windowMenu);
		// restoreItem
		restoreItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Restore")); //$NON-NLS-1$
		restoreItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				trackerPanel.restoreViews();
			}
		});
		// right Pane item
		rightPaneItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.WindowRight"), false); //$NON-NLS-1$
		rightPaneItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getFrame() != null) {
					JSplitPane pane = frame.getSplitPane(trackerPanel, 0);
					if (rightPaneItem.isSelected()) {
						pane.setDividerLocation(frame.defaultRightDivider);
					} else {
						pane.setDividerLocation(1.0);
					}
				}
			}
		});
		// bottom Pane item
		bottomPaneItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.WindowBottom"), false); //$NON-NLS-1$
		bottomPaneItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getFrame() != null) {
					JSplitPane pane = frame.getSplitPane(trackerPanel, 2);
					if (bottomPaneItem.isSelected()) {
						pane.setDividerLocation(frame.defaultBottomDivider);
					} else {
						pane.setDividerLocation(1.0);
					}
				}
			}
		});
		// trackControlItem
		trackControlItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.TrackControl")); //$NON-NLS-1$
		trackControlItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TrackControl tc = TrackControl.getControl(trackerPanel);
				tc.setVisible(!tc.isVisible());
			}
		});
		// notesItem
		notesItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Description")); //$NON-NLS-1$
		notesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getFrame() != null) {
					if (frame.notesDialog.isVisible()) {
						frame.notesDialog.setVisible(false);
					} else
						frame.getToolBar(trackerPanel).notesButton.doClick();
				}
			}
		});
		// dataBuilder item
		String s = TrackerRes.getString("TMenuBar.MenuItem.DataFunctionTool"); //$NON-NLS-1$
		s += " (" + TrackerRes.getString("TView.Menuitem.Define") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		dataBuilderItem = new JCheckBoxMenuItem(s);
		dataBuilderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FunctionTool builder = trackerPanel.getDataBuilder();
				if (builder.isVisible())
					builder.setVisible(false);
				else {
					TTrack track = trackerPanel.getSelectedTrack();
					if (track != null)
						builder.setSelectedPanel(track.getName());
					builder.setVisible(true);
				}
			}
		});
		// dataTool item
		s = TrackerRes.getString("TMenuBar.MenuItem.DatasetTool"); //$NON-NLS-1$
		s += " (" + TrackerRes.getString("TableTrackView.Popup.MenuItem.Analyze") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		dataToolItem = new JCheckBoxMenuItem(s);
		dataToolItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DataTool tool = DataTool.getTool();
				if (tool.isVisible()) {
					tool.setVisible(false);
					return;
				}
				// send some data to the tool
				boolean sent = false;
				TView[][] views = getFrame().getTViews(trackerPanel);
				String[] selectedViews = getFrame().getSelectedTViews(trackerPanel);
				for (int i = 0; i < selectedViews.length; i++) {
					String s = selectedViews[i];
					if (s != null && s.toLowerCase().startsWith("plot") && i < views.length) { //$NON-NLS-1$
						TView[] next = views[i];
						for (TView view : next) {
							if (view instanceof PlotTView) {
								PlotTView v = (PlotTView) view;
								TrackView trackView = v.getTrackView(v.getSelectedTrack());
								PlotTrackView plotView = (PlotTrackView) trackView;
								if (plotView != null) {
									for (TrackPlottingPanel plot : plotView.getPlots()) {
										plot.dataToolItem.doClick();
										sent = true;
									}
								}
							}
						}
					}
				}
				// no plot views were visible, so look for table views
				if (!sent) {
					for (int i = 0; i < selectedViews.length; i++) {
						String s = selectedViews[i];
						if (s != null && s.toLowerCase().startsWith("table") && i < views.length) { //$NON-NLS-1$
							TView[] next = views[i];
							for (TView view : next) {
								if (view instanceof TableTView) {
									TableTView v = (TableTView) view;
									TrackView trackView = v.getTrackView(v.getSelectedTrack());
									TableTrackView tableView = (TableTrackView) trackView;
									if (tableView != null) {
										tableView.dataToolItem.doClick();
									}
								}
							}
						}
					}
				}
				tool.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				tool.setVisible(true);
			}
		});
		// help menu
		helpMenu = getTrackerHelpMenu(trackerPanel, null);
		add(helpMenu);
		// empty menu items
		video_emptyVideoItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Empty")); //$NON-NLS-1$
		video_emptyVideoItem.setEnabled(false);
		video_emptyTracksItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Empty")); //$NON-NLS-1$
		video_emptyTracksItem.setEnabled(false);
		coords_emptyCoordsItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Empty")); //$NON-NLS-1$
		coords_emptyCoordsItem.setEnabled(false);
	}

	protected void setupVideoMenu() {
		// enable paste image item if clipboard contains image data
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable data = clipboard.getContents(null);
		boolean b = data != null && data.isDataFlavorSupported(DataFlavor.imageFlavor);
		video_pasteImageMenu.setEnabled(b);
		video_pasteImageItem.setEnabled(b);
		
		// enable pasteFilterItem if VideoFilter xml on clipboard
		// DB this only needs checking when clipboard contents have changed
		boolean filterOnClipboard = false;
		String pasteFilterText = TrackerRes.getString("TActions.Action.Paste"); //$NON-NLS-1$
		String xml = DataTool.paste();
		if (xml != null && xml.contains("<?xml")) { //$NON-NLS-1$
			XMLControl control = new XMLControlElement(xml);
			filterOnClipboard = Filter.class.isAssignableFrom(control.getObjectClass());
			if (filterOnClipboard) {
				String filterName = control.getObjectClass().getSimpleName();
				int i = filterName.indexOf("Filter"); //$NON-NLS-1$
				if (i > 0 && i < filterName.length() - 1) {
					filterName = filterName.substring(0, i);
				}
				filterName = MediaRes.getString("VideoFilter." + filterName); //$NON-NLS-1$
				pasteFilterText += " " + filterName; //$NON-NLS-1$
			}
		}
		pasteFilterItem.setEnabled(filterOnClipboard);
		pasteFilterItem.setText(pasteFilterText);
		
		// refresh video filters menu
		// DB this only changes when a video filter is added or removed
		Video video = trackerPanel.getVideo();
		if (video != null) {
			boolean vis = trackerPanel.getPlayer().getClipControl().videoVisible;
			video_videoVisibleItem.setSelected(video.isVisible() || vis);
			// replace filters menu if used in popup
			// DB maybe using same menu in popup is not good idea??
			boolean showFiltersMenu = trackerPanel.isEnabled("video.filters"); //$NON-NLS-1$
			boolean hasNoFiltersMenu = true;
			for (int i = 0; i < videoMenu.getItemCount(); i++) {
				JMenuItem item = videoMenu.getItem(i);
				if (item == video_filtersMenu)
					hasNoFiltersMenu = false;
			}
			if (hasNoFiltersMenu && showFiltersMenu) {
				videoMenu.remove(checkDurationsItem);
				videoMenu.remove(video_aboutVideoItem);
				int i = videoMenu.getMenuComponentCount() - 1;
				for (; i >= 0; i--) {
					Component next = videoMenu.getMenuComponent(i);
					if (next instanceof JMenuItem)
						break;
					videoMenu.remove(next);
				}
				videoMenu.addSeparator();
				videoMenu.add(video_filtersMenu);
				videoMenu.addSeparator();
				videoMenu.remove(checkDurationsItem);
				videoMenu.add(video_aboutVideoItem);
			}
		}
//		videoMenu.revalidate();
	}

	protected void setupEditMenu() {

		// enable deleteSelectedPoint item if a selection exists
		Step step = trackerPanel.getSelectedStep();
		TTrack track = trackerPanel.getSelectedTrack();
		boolean cantDeleteSteps = track == null || track.isLocked() || track.isDependent();
		edit_delTracks_deleteSelectedPointItem.setEnabled(!cantDeleteSteps && step != null);

		// refresh paste item
		// DB refreshPassteItem only needed if clipboard contents changed since last
		// time
		refreshPasteItem();

		// refresh copyData menu
		// DB as above, getDataViews() only changes when a TableTrackView is
		// displayed/hidden
		TreeMap<Integer, TableTrackView> dataViews = getDataViews();
		edit_copyDataMenu.removeAll();
		edit_copyDataMenu.setEnabled(!dataViews.isEmpty());
		if (dataViews.isEmpty()) {
			edit_copyDataMenu.setText(TrackerRes.getString("TableTrackView.Action.CopyData")); //$NON-NLS-1$
		} else if (dataViews.size() == 1) {
			Integer key = dataViews.firstKey();
			TableTrackView view = dataViews.get(key);
			view.refreshCopyDataMenu(edit_copyDataMenu);
			String text = edit_copyDataMenu.getText();
			edit_copyDataMenu.setText(text + " (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			edit_copyDataMenu.setText(TrackerRes.getString("TableTrackView.Action.CopyData")); //$NON-NLS-1$
			for (int key : dataViews.keySet()) {
				TableTrackView view = dataViews.get(key);
				JMenu menu = new JMenu();
				edit_copyDataMenu.add(view.refreshCopyDataMenu(menu));
				String text = menu.getText();
				menu.setText(text + " (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// refresh copyImage menu--include only open views
		// DB copyImageMenu needs refresh only when a view has been
		// opened/closed/changed
		edit_copyImageMenu.remove(edit_copyFrameImageItem);
		final Container[] views = trackerPanel.getTFrame().getViews(trackerPanel);
		// check that array size is correct and if not, make new menu items
		if (edit_copyViewImageItems.length != views.length) {
			Action copyView = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int i = Integer.parseInt(e.getActionCommand());
					new TrackerIO.ComponentImage(views[i]).copyToClipboard();
				}
			};
			edit_copyViewImageItems = new JMenuItem[views.length];
			for (int i = 0; i < views.length; i++) {
				edit_copyViewImageItems[i] = new JMenuItem();
				String command = String.valueOf(i);
				edit_copyViewImageItems[i].setActionCommand(command);
				edit_copyViewImageItems[i].setAction(copyView);
			}
		}
		// add menu items for open views
		for (int i = 0; i < views.length; i++) {
			if (trackerPanel.getTFrame().isViewOpen(i, trackerPanel)) {
				String viewname = null;
				if (views[i] instanceof TViewChooser) {
					TViewChooser chooser = (TViewChooser) views[i];
					TView tview = chooser.getSelectedView();
					viewname = tview == null ? TrackerRes.getString("TFrame.View.Unknown")
							: tview.getViewName();
				} else
					viewname = TrackerRes.getString("TFrame.View.Unknown"); //$NON-NLS-1$
				edit_copyViewImageItems[i].setText(viewname + " (" + (i + 1) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				String command = String.valueOf(i);
				edit_copyViewImageItems[i].setActionCommand(command);
				edit_copyImageMenu.add(edit_copyViewImageItems[i]);
			} else {
				edit_copyImageMenu.remove(edit_copyViewImageItems[i]);
			}
		}
		edit_copyImageMenu.add(edit_copyFrameImageItem);
		// end refresh copyImageMenu

//		FontSizer.setFonts(editMenu);
//		editMenu.revalidate();
	}

	/**
	 * Refreshes the Window menu for a TrackerPanel.
	 *
	 * @param trackerPanel the TrackerPanel
	 */
	public void refreshWindowMenu() {
		// refresh window menu
		TFrame frame = trackerPanel.getTFrame();
		JSplitPane pane = frame.getSplitPane(trackerPanel, 0);
		int max = pane.getMaximumDividerLocation();
		int cur = pane.getDividerLocation();
		double loc = 1.0 * cur / max;
		TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
		menubar.rightPaneItem.setSelected(loc < 0.99);
		pane = frame.getSplitPane(trackerPanel, 2);
		max = pane.getMaximumDividerLocation();
		cur = pane.getDividerLocation();
		loc = 1.0 * cur / max;
		menubar.bottomPaneItem.setSelected(loc < .99);
		TrackControl tc = TrackControl.getControl(trackerPanel);
		menubar.trackControlItem.setSelected(tc.isVisible());
		menubar.trackControlItem.setEnabled(!tc.isEmpty());
		menubar.notesItem.setSelected(frame.notesDialog.isVisible());
		menubar.dataBuilderItem.setSelected(trackerPanel.dataBuilder != null && trackerPanel.dataBuilder.isVisible());
		menubar.dataToolItem.setSelected(DataTool.getTool().isVisible());

		// rebuild window menu
		menubar.windowMenu.removeAll();
		boolean maximized = false;
		Container[] views = frame.getViews(trackerPanel);
		for (int i = 0; i < views.length; i++) {
			if (views[i] instanceof TViewChooser) {
				TViewChooser chooser = (TViewChooser) views[i];
				if (chooser.maximized) {
					maximized = true;
					break;
				}
			}
		}
		if (maximized) {
			menubar.windowMenu.add(menubar.restoreItem);
		} else {
			menubar.windowMenu.add(menubar.rightPaneItem);
			menubar.windowMenu.add(menubar.bottomPaneItem);
		}
		menubar.windowMenu.addSeparator();
		menubar.windowMenu.add(menubar.trackControlItem);
		menubar.windowMenu.add(menubar.notesItem);
		if (trackerPanel.isEnabled("data.builder") //$NON-NLS-1$
				|| trackerPanel.isEnabled("data.tool")) { //$NON-NLS-1$
			menubar.windowMenu.addSeparator();
			if (trackerPanel.isEnabled("data.builder")) //$NON-NLS-1$
				menubar.windowMenu.add(menubar.dataBuilderItem);
			if (trackerPanel.isEnabled("data.tool")) //$NON-NLS-1$
				menubar.windowMenu.add(menubar.dataToolItem);
		}
		for (int i = 0, n = frame.getTabCount(); i < n; i++) {
			if (i == 0)
				menubar.windowMenu.addSeparator();
			JMenuItem tabItem = new JRadioButtonMenuItem(frame.getTabTitle(i));
			tabItem.setActionCommand(String.valueOf(i));
			tabItem.setSelected(i == frame.getSelectedTab());
			tabItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int j = Integer.parseInt(e.getActionCommand());
					frame.setSelectedTab(j);
				}
			});
			menubar.windowMenu.add(tabItem);
		}
		FontSizer.setMenuFonts(menubar.windowMenu);
	}


	/**
	 * Gets the help menu.
	 *
	 * @return the help menu
	 */
	protected static JMenu getTrackerHelpMenu(final TrackerPanel trackerPanel, JMenu hMenu) {
		// help menu
		int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		if (hMenu == null)
			hMenu = new JMenu();
		else 
			hMenu.removeAll();
		hMenu.setText(TrackerRes.getString("TMenuBar.Menu.Help")); //$NON-NLS-1$

		JMenu helpMenu = hMenu;
		// Tracker help items
		JMenuItem startItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.GettingStarted") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		startItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String quickStartURL = "https://www.youtube.com/watch?v=n4Eqy60yYUY"; //$NON-NLS-1$
				OSPDesktop.displayURL(quickStartURL);
//        Container c = helpMenu.getTopLevelAncestor();
//        if (c instanceof TFrame) {
//          TFrame frame = (TFrame) c;
//	        frame.showHelp("gettingstarted", 0); //$NON-NLS-1$
//        }
			}
		});
		helpMenu.add(startItem);
		JMenuItem helpItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.TrackerHelp")); //$NON-NLS-1$
		helpItem.setAccelerator(KeyStroke.getKeyStroke('H', keyMask));
		helpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Container c = helpMenu.getTopLevelAncestor();
				if (c instanceof TFrame) {
					TFrame frame = (TFrame) c;
					frame.showHelp(null, 0);
				}
			}
		});
		helpMenu.add(helpItem);
		JMenuItem onlineHelpItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.OnlineHelp") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		onlineHelpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String lang = TrackerRes.locale.getLanguage();
				if ("en".equals(lang)) { //$NON-NLS-1$
					OSPDesktop.displayURL("https://" + Tracker.trackerWebsite + "/help/frameset.html"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					String english = Locale.ENGLISH.getDisplayLanguage(TrackerRes.locale);
					String language = TrackerRes.locale.getDisplayLanguage(TrackerRes.locale);
					String message = TrackerRes.getString("TMenuBar.Dialog.Translate.Message1") //$NON-NLS-1$
							+ "\n" + TrackerRes.getString("TMenuBar.Dialog.Translate.Message2") + " " + language + "." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							+ "\n" + TrackerRes.getString("TMenuBar.Dialog.Translate.Message3"); //$NON-NLS-1$ //$NON-NLS-2$
					TFrame frame = trackerPanel == null ? null : trackerPanel.getTFrame();
					int response = javax.swing.JOptionPane.showOptionDialog(frame, message,
							TrackerRes.getString("TMenuBar.Dialog.Translate.Title"), //$NON-NLS-1$
							JOptionPane.YES_NO_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null,
							new String[] { english, language, TrackerRes.getString("Dialog.Button.Cancel") }, //$NON-NLS-1$
							language);
					if (response == 1) { // language translation
						String helpURL = "https://translate.google.com/translate?hl=en&sl=en&tl=" + lang //$NON-NLS-1$
								+ "&u=https://physlets.org/tracker/help/frameset.html"; //$NON-NLS-1$
						OSPDesktop.displayURL(helpURL);
					} else if (response == 0) { // english
						OSPDesktop.displayURL("https://" + Tracker.trackerWebsite + "/help/frameset.html"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		});
		helpMenu.add(onlineHelpItem);
		JMenuItem discussionHelpItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.ForumHelp") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		discussionHelpItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String helpURL = "https://www.compadre.org/osp/bulletinboard/ForumDetails.cfm?FID=57"; //$NON-NLS-1$
				OSPDesktop.displayURL(helpURL);
			}
		});
		helpMenu.add(discussionHelpItem);

		if (!OSPRuntime.isJS && Tracker.trackerHome != null && Tracker.readmeAction != null)
			helpMenu.add(Tracker.readmeAction);

		// hints item
		final JMenuItem hintsItem = new JCheckBoxMenuItem(TrackerRes.getString("Tracker.MenuItem.Hints")); //$NON-NLS-1$
		hintsItem.setSelected(Tracker.showHints);
		hintsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.showHints = hintsItem.isSelected();
				Tracker.startupHintShown = false;
				Container c = helpMenu.getTopLevelAncestor();
				if (c instanceof TFrame) {
					TFrame frame = (TFrame) c;
					int tab = frame.getSelectedTab();
					if (tab > -1) {
						TrackerPanel panel = frame.getTrackerPanel(tab);
						panel.setCursorForMarking(false, null);
						TView[][] views = frame.getTViews(panel);
						for (TView[] next : views) {
							for (TView view : next) {
								if (view instanceof PlotTView) {
									PlotTView v = (PlotTView) view;
									TrackView trackView = v.getTrackView(v.getSelectedTrack());
									PlotTrackView plotView = (PlotTrackView) trackView;
									if (plotView != null) {
										for (TrackPlottingPanel plot : plotView.getPlots()) {
											plot.plotData();
										}
									}
								}
							}
						}
					}
				}
			}
		});
		if (!org.opensourcephysics.display.OSPRuntime.isMac()) {
			helpMenu.addSeparator();
			helpMenu.add(hintsItem);
		}

		// diagnostics menu
		boolean showDiagnostics = trackerPanel == null ? Tracker.getDefaultConfig().contains("help.diagnostics") : //$NON-NLS-1$
				trackerPanel.isEnabled("help.diagnostics"); //$NON-NLS-1$
		if (showDiagnostics) {
			helpMenu.addSeparator();
			JMenu diagMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Diagnostics")); //$NON-NLS-1$
			helpMenu.add(diagMenu);
			JMenuItem logItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.MessageLog")); //$NON-NLS-1$
			logItem.setAccelerator(KeyStroke.getKeyStroke('L', keyMask));
			logItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Point p = new Frame().getLocation(); // default location of new frame or dialog
					OSPLog log = OSPLog.getOSPLog();
					FontSizer.setFonts(log, FontSizer.getLevel());
					if (log.getLocation().x == p.x && log.getLocation().y == p.y) {
						// center on screen
						Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
						int x = (dim.width - log.getBounds().width) / 2;
						int y = (dim.height - log.getBounds().height) / 2;
						log.setLocation(x, y);
					}
					log.setVisible(true);
				}
			});
			diagMenu.add(logItem);
			if (Tracker.startLogAction != null) {
				JMenuItem item = diagMenu.add(Tracker.startLogAction);
				item.setToolTipText(System.getenv("START_LOG")); //$NON-NLS-1$
			}
			if (Tracker.trackerPrefsAction != null) {
				JMenuItem item = diagMenu.add(Tracker.trackerPrefsAction);
				item.setToolTipText(XML.forwardSlash(Tracker.prefsPath));
			}
			diagMenu.addSeparator();
			if (Tracker.aboutJavaAction != null)
				diagMenu.add(Tracker.aboutJavaAction);
			if (Tracker.aboutXuggleAction != null)
				diagMenu.add(Tracker.aboutXuggleAction);
			if (Tracker.aboutThreadsAction != null)
				diagMenu.add(Tracker.aboutThreadsAction);
		} // end diagnostics menu

		helpMenu.addSeparator();
		JMenuItem checkForUpgradeItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CheckForUpgrade.Text")); //$NON-NLS-1$
		checkForUpgradeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.showUpgradeStatus(trackerPanel);
			}
		});
		if (!OSPRuntime.isJS) {
			helpMenu.add(checkForUpgradeItem);
		}

		if (Tracker.aboutTrackerAction != null)
			helpMenu.add(Tracker.aboutTrackerAction);
		FontSizer.setMenuFonts(helpMenu);
		return helpMenu;
	}

	/**
	 * Gets the menu for the specified track.
	 *
	 * @param track the track
	 * @return the track's menu
	 */
	protected JMenu createTrackMenu(TTrack track) {
		JMenu menu = track.getMenu(trackerPanel);
		menu.setName("track");
		ImageCoordSystem coords = trackerPanel.getCoords();
		if (coords.isLocked() && coords instanceof ReferenceFrame
				&& track == ((ReferenceFrame) coords).getOriginTrack()) {
			for (int i = 0; i < menu.getItemCount(); i++) {
				JMenuItem item = menu.getItem(i);
				if (item != null && item.getText().equals(TrackerRes.getString("TMenuBar.MenuItem.CoordsLocked"))) { //$NON-NLS-1$
					menu.getItem(i).setEnabled(false);
					break;
				}
			}
		}
		if (track == trackerPanel.getAxes()) {
			int i = 0;
			for (; i < menu.getItemCount(); i++) {
				JMenuItem item = menu.getItem(i);
				if (item != null && item.getText().equals(TrackerRes.getString("TTrack.MenuItem.Visible"))) { //$NON-NLS-1$
					menu.remove(i);
					break;
				}
			}
			axesVisibleItem.setSelected(track.isVisible());
			menu.insert(axesVisibleItem, i);
		}
		FontSizer.setMenuFonts(menu);
		return menu;
	}

	/**
	 * Refreshes the menubar.
	 * @param whereFrom 
	 */
	protected void refresh(String whereFrom) {
		if (!allowRefresh || getFrame() != null && !frame.isPainting()) {
			OSPLog.debug("TMenuBar.refresh skipping " + whereFrom );
			return;
		}
		OSPRuntime.postEvent(new Runnable() {
			@Override
			public synchronized void run() {
				refreshAll(whereFrom);
			}
		});
	}

	static final String TTOOLBAR_TRACKS        = "TTooBar.tracks";
	static final String TFRAME_BOTTOM          = "TFrame.bottom";
	static final String TFRAME_RIGHT           = "TFrame.right";
	static final String MAINTVIEW_POPUP        = "MainTView.popup";

	static final String REFRESH_TFRAME_LOCALE            = "TFrame.locale";
	static final String REFRESH_TOOLBAR_POPUP            = "TToolBar.popup";
	static final String REFRESH_TFRAME                   = "TFrame.refresh";
	static final String REFRESH_PROPERTY_                = "property:?";
	static final String REFRESH_TRACKERIO_DONELOADING_   = "TrackerIO.doneLoading:?";
	static final String REFRESH_TRACKERIO_OPENFRAME      = "TrackerIO.aferOpenFrame";
	static final String REFRESH_TRACKERIO_BEFORESETVIDEO = "TrackerIO.beforeSetVideo";
    static final String REFRESH_TRACKERIO_SAVE           = "TrackerIO.save";
	static final String REFRESH_TRACKERIO_SAVETABSET     = "TrackerIO.saveTabset";
	static final String REFRESH_TRACKERIO_SAVEVIDEO      = "TrackerIO.saveVideoOK";
	static final String REFRESH_TPANEL_SETTRACKNAME      = "TrackerPanel.setTrackName";
	static final String REFRESH_PREFS_CLEARRECENT        = "PrefsDialog.clearRecent";
	static final String REFRESH_PREFS_APPLYPREFS         = "PrefsDialog.applyPrefs";
	static final String REFRESH_TACTIONS_OPENVIDEO       = "TActions.openVideo";
	static final String REFRESH_TFRAME_OPENRECENT        = "TFrame.openRecent";
	static final String REFRESH_UNDO                     = "Undo.refreshMenus";
	
	protected void refreshAll(String whereFrom) {
		Tracker.logTime(getClass().getSimpleName() + hashCode() + " refresh"); //$NON-NLS-1$
		OSPLog.debug("TMenuBar.refreshAll - rebuilding TMenuBar "
		+ whereFrom + " haveFrame=" + (frame != null));
		if (!Tracker.allowMenuRefresh)
			return;
		refreshing = true; // signals listeners that items are being refreshed
		try {
			switch (whereFrom) {
			case REFRESH_TOOLBAR_POPUP:
				refreshTracksCreateMenu();
				break;
			case REFRESH_TRACKERIO_SAVE:
			case REFRESH_TRACKERIO_SAVETABSET:
			case REFRESH_TRACKERIO_SAVEVIDEO:
			case REFRESH_PROPERTY_:
			case REFRESH_TACTIONS_OPENVIDEO:
			case REFRESH_TRACKERIO_OPENFRAME:
			case REFRESH_TRACKERIO_BEFORESETVIDEO:
				break;
			case REFRESH_TPANEL_SETTRACKNAME:
				refreshTrackMenuTexts(trackerPanel.getUserTracks());
				break;
			case REFRESH_TFRAME_OPENRECENT:
			case REFRESH_PREFS_CLEARRECENT:
				refreshRecentFilesMenu();
				break;
			case REFRESH_TRACKERIO_DONELOADING_:
			case REFRESH_PREFS_APPLYPREFS:
			case REFRESH_UNDO:
			case REFRESH_TFRAME_LOCALE:
			case REFRESH_TFRAME:
			default:
				OSPLog.debug(Performance.timeCheckStr("TMenuBar refreshAll full rebuild start", Performance.TIME_MARK));
				if (OSPRuntime.isJS) {
					// signals SwingJS that there is no need to do anything with the DOM during this
					// process
					// of rebuilding the menu.
					OSPRuntime.jsutil.setUIEnabled(this, false);
				}
				long t0 = Performance.now(0);
				refreshFileMenu();
				refreshEditMenu();
				refreshVideoMenu();
				refreshCoordsMenu();
				refreshTrackMenu();
				refreshHelpMenu();

				OSPLog.debug("!!! " + Performance.now(t0) + " TMenuBar.refreshAll");
				
				
				//FontSizer.setFonts(this, FontSizer.getLevel());
				if (OSPRuntime.isJS) {
					OSPRuntime.jsutil.setUIEnabled(this, true);
				}
			}
		} catch (Throwable t) {
			System.out.println(t);//t.printStackTrace();
		}
		refreshing = false;

	}

	/**
	 * Refreshes and returns the "new tracks" popup menu.
	 *
	 * @return the popup
	 */
	protected JPopupMenu refreshTracksPopup(JPopupMenu newPopup) {
		newPopup.removeAll();
		refresh(REFRESH_TOOLBAR_POPUP);
		for (Component c : newTrackItems) {
			newPopup.add(c);
			if (c == newDataTrackMenu) {
				// disable newDataTrackPasteItem unless pastable data is on the clipboard
				newDataTrackPasteItem.setEnabled(false);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable data = clipboard.getContents(null);
				if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						String s = (String) data.getTransferData(DataFlavor.stringFlavor);
						newDataTrackPasteItem.setEnabled(ParticleDataTrack.getImportableDataName(s) != null);
					} catch (Exception ex) {
					}
				}
			}
		}
		if (track_cloneMenu.getItemCount() > 0 && trackerPanel.isEnabled("new.clone")) { //$NON-NLS-1$
			newPopup.addSeparator();
			newPopup.add(track_cloneMenu);
		}
		return newPopup;
	}

	
	
	protected void refreshHelpMenu() {
		getTrackerHelpMenu(trackerPanel, helpMenu);
	}

	protected void refreshTrackMenu() {
		ArrayList<TTrack> userTracks = trackerPanel.getUserTracks();
		boolean hasTracks = !userTracks.isEmpty();
		CoordAxes axes = trackerPanel.getAxes();
		TTrack track = trackerPanel.getSelectedTrack();
		// refresh track menu
		trackMenu.removeAll();
		if (track_createMenu.getItemCount() > 0)
			trackMenu.add(track_createMenu);
		track_cloneMenu.removeAll();
		if (hasTracks && trackerPanel.isEnabled("new.clone")) //$NON-NLS-1$
			trackMenu.add(track_cloneMenu);
		// clearTracksItem enabled only when there are tracks
		edit_clearTracksItem.setEnabled(hasTracks);
		if (hasTracks && trackMenu.getItemCount() > 0)
			trackMenu.addSeparator();
		PointMass originTrack = getOriginTrack();
		// for each track
		for (int i = 0, n = userTracks.size(); i < n; i++) {
			track = userTracks.get(i);
			track.removePropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
			track.addPropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
			String trackName = track.getName("track"); //$NON-NLS-1$
			// add delete item to edit menu for each track
			JMenuItem item = new JMenuItem(trackName);
			item.setName("track");
			item.setIcon(track.getIcon(21, 16, "track")); //$NON-NLS-1$
			item.addActionListener(actions.get("deleteTrack")); //$NON-NLS-1$
			item.setEnabled(!track.isLocked() || track.isDependent());
			edit_deleteTracksMenu.add(item);
			// add item to clone menu for each track
			item = new JMenuItem(trackName);
			item.setName("track");
			item.setIcon(track.getIcon(21, 16, "track")); //$NON-NLS-1$
			item.addActionListener(actions.get("cloneTrack")); //$NON-NLS-1$
			track_cloneMenu.add(item);
			// add each track's submenu to track menu
			trackMenu.add(createTrackMenu(track));
			// if track is point mass, add reference frame menu items
			if (track instanceof PointMass) {
				item = new JRadioButtonMenuItem(trackName);
				item.addActionListener(actions.get("refFrame")); //$NON-NLS-1$
				refFrameGroup.add(item);
				coords_refFrameMenu.add(item);
				if (track == originTrack)
					item.setSelected(true);
			}
		}
		refreshTrackMenuTexts(userTracks);
		if (trackerPanel.isEnabled("edit.clear")) { //$NON-NLS-1$
			if (edit_deleteTracksMenu.getItemCount() > 0)
				edit_deleteTracksMenu.addSeparator();
			edit_deleteTracksMenu.add(edit_clearTracksItem);
		}
		// add axes and calibration tools to track menu
		if (trackerPanel.isEnabled("button.axes") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
			if (trackMenu.getItemCount() > 0)
				trackMenu.addSeparator();
			if (axes != null && trackerPanel.isEnabled("button.axes")) { //$NON-NLS-1$
				track = axes;
				track.removePropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
				track.addPropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
//				trackMenu.add(createTrackMenu(track));
			}
			if (!trackerPanel.calibrationTools.isEmpty()) {
				for (TTrack next : trackerPanel.getTracks()) {
					if (trackerPanel.calibrationTools.contains(next)) {
						if (next instanceof TapeMeasure) {
							TapeMeasure tape = (TapeMeasure) next;
							if (tape.isStickMode() && !trackerPanel.isEnabled("calibration.stick")) //$NON-NLS-1$
								continue;
							if (!tape.isStickMode() && !trackerPanel.isEnabled("calibration.tape")) //$NON-NLS-1$
								continue;
						}
						if (next instanceof Calibration && !trackerPanel.isEnabled("calibration.points")) //$NON-NLS-1$
							continue;
						if (next instanceof OffsetOrigin && !trackerPanel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
							continue;
						next.removePropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
						next.addPropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
						trackMenu.add(createTrackMenu(next));
					}
				}
			}
		}
		refreshTracksCreateMenu();
	}

	private void refreshTrackMenuTexts(ArrayList<TTrack> userTracks) {
		int jd = 0, jc = 0, jt = 0, jp = 0;
		for (int i = 0, n = userTracks.size(); i < n; i++) {
			TTrack track = userTracks.get(i);
			String trackName = track.getName("track"); //$NON-NLS-1$
			jd = setNextTrackMenuText(edit_deleteTracksMenu, jd, trackName);
			jc = setNextTrackMenuText(track_cloneMenu, jc, trackName);
			jt = setNextTrackMenuText(trackMenu, jt, trackName);
			if (track instanceof PointMass) {
				jp = setNextTrackMenuText(coords_refFrameMenu, jp, trackName);
			}
		}
	}

	/**
	 * somewhere in this menu are n JMenuItems that have the name "track". 
	 * These are the ones that need renaming.
	 * 
	 * @param menu
	 * @param j
	 * @param trackName
	 * @return
	 */
	private int setNextTrackMenuText(JMenu menu, int j, String trackName) {
		Component c = null;
		try {
			int n = menu.getMenuComponentCount();
		while (j < n && !("track".equals((c = menu.getMenuComponent(j)).getName()))) {
			if (++j >= n)
	 			return j;
		}
		if (c != null)
			((JMenuItem) c).setText(trackName);
		} catch (Throwable t) {
			System.out.println("????" + t);
		}
		return j;
	}

	private void refreshTracksCreateMenu() {
		// refresh new tracks menu
		track_createMenu.removeAll();
		if (trackerPanel.isEnabled("new.pointMass") || //$NON-NLS-1$
		trackerPanel.isEnabled("new.cm")) { //$NON-NLS-1$
			if (trackerPanel.isEnabled("new.pointMass")) //$NON-NLS-1$
				track_createMenu.add(newPointMassItem);
			if (trackerPanel.isEnabled("new.cm")) //$NON-NLS-1$
				track_createMenu.add(newCMItem);
		}
		if (trackerPanel.isEnabled("new.vector") || //$NON-NLS-1$
		trackerPanel.isEnabled("new.vectorSum")) { //$NON-NLS-1$
			if (track_createMenu.getItemCount() > 0)
				track_createMenu.addSeparator();
			if (trackerPanel.isEnabled("new.vector")) //$NON-NLS-1$
				track_createMenu.add(newVectorItem);
			if (trackerPanel.isEnabled("new.vectorSum")) //$NON-NLS-1$
				track_createMenu.add(newVectorSumItem);
		}
		if (trackerPanel.isEnabled("new.lineProfile") || //$NON-NLS-1$
		trackerPanel.isEnabled("new.RGBRegion")) { //$NON-NLS-1$
			if (track_createMenu.getItemCount() > 0)
				track_createMenu.addSeparator();
			if (trackerPanel.isEnabled("new.lineProfile")) //$NON-NLS-1$
				track_createMenu.add(newLineProfileItem);
			if (trackerPanel.isEnabled("new.RGBRegion")) //$NON-NLS-1$
				track_createMenu.add(newRGBRegionItem);
		}
		if (trackerPanel.isEnabled("new.analyticParticle") //$NON-NLS-1$
				|| trackerPanel.isEnabled("new.dynamicParticle") //$NON-NLS-1$
				|| trackerPanel.isEnabled("new.dynamicTwoBody") //$NON-NLS-1$
				|| trackerPanel.isEnabled("new.dataTrack")) { //$NON-NLS-1$
			if (track_createMenu.getItemCount() > 0)
				track_createMenu.addSeparator();
			if (trackerPanel.isEnabled("new.analyticParticle")) //$NON-NLS-1$
				track_createMenu.add(newAnalyticParticleItem);
			if (trackerPanel.isEnabled("new.dynamicParticle") //$NON-NLS-1$
					|| trackerPanel.isEnabled("new.dynamicTwoBody")) { //$NON-NLS-1$
				track_createMenu.add(newDynamicParticleMenu);
				newDynamicParticleMenu.removeAll();
				if (trackerPanel.isEnabled("new.dynamicParticle")) { //$NON-NLS-1$
					newDynamicParticleMenu.add(newDynamicParticleCartesianItem);
					newDynamicParticleMenu.add(newDynamicParticlePolarItem);
				}
				if (trackerPanel.isEnabled("new.dynamicTwoBody")) //$NON-NLS-1$
					newDynamicParticleMenu.add(newDynamicSystemItem);
			}
			if (trackerPanel.isEnabled("new.dataTrack")) { //$NON-NLS-1$
				track_createMenu.add(newDataTrackMenu);
				newDataTrackMenu.removeAll();
				newDataTrackMenu.add(newDataTrackFromFileItem);
				newDataTrackMenu.add(newDataTrackFromEJSItem);
				newDataTrackMenu.add(newDataTrackPasteItem);
				newDataTrackMenu.addSeparator();
				newDataTrackMenu.add(dataTrackHelpItem);
			}
		}
		if (trackerPanel.isEnabled("new.tapeMeasure") || //$NON-NLS-1$
		trackerPanel.isEnabled("new.protractor") || //$NON-NLS-1$
		trackerPanel.isEnabled("new.circleFitter")) { //$NON-NLS-1$
			if (track_createMenu.getItemCount() > 0)
				track_createMenu.addSeparator();
			track_createMenu.add(measuringToolsMenu);
			measuringToolsMenu.removeAll();
			if (trackerPanel.isEnabled("new.tapeMeasure")) //$NON-NLS-1$
				measuringToolsMenu.add(newTapeItem);
			if (trackerPanel.isEnabled("new.protractor")) //$NON-NLS-1$
				measuringToolsMenu.add(newProtractorItem);
			if (trackerPanel.isEnabled("new.circleFitter")) //$NON-NLS-1$
				measuringToolsMenu.add(newCircleFitterItem);
		}
		// calibration tools menu
		if (trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
			if (track_createMenu.getItemCount() > 0)
				track_createMenu.addSeparator();
			TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
			TToolBar.CalibrationButton calibrationButton = toolbar.calibrationButton;
			JMenu calibrationToolsMenu = calibrationButton.getCalibrationToolsMenu();
			calibrationToolsMenu.setText(TrackerRes.getString("TMenuBar.Menu.CalibrationTools")); //$NON-NLS-1$
			track_createMenu.add(calibrationToolsMenu);
		}
		newTrackItems = track_createMenu.getMenuComponents();
	}

	protected void refreshEditMenu() {
		boolean hasTracks = !trackerPanel.getUserTracks().isEmpty();
		editMenu.removeAll();
		if (trackerPanel.isEnabled("edit.undoRedo")) { //$NON-NLS-1$
			edit_undoItem.setText(TrackerRes.getString("TMenuBar.MenuItem.Undo")); //$NON-NLS-1$
			edit_undoItem.setText(Undo.getUndoDescription(trackerPanel));
			editMenu.add(edit_undoItem);
			edit_undoItem.setEnabled(Undo.canUndo(trackerPanel));
			edit_redoItem.setText(TrackerRes.getString("TMenuBar.MenuItem.Redo")); //$NON-NLS-1$
			edit_redoItem.setText(Undo.getRedoDescription(trackerPanel));
			editMenu.add(edit_redoItem);
			edit_redoItem.setEnabled(Undo.canRedo(trackerPanel));
		}
		// refresh copyData, copyImage and copyObject menus
		if (trackerPanel.isEnabled("edit.copyData") //$NON-NLS-1$
				|| trackerPanel.isEnabled("edit.copyImage") //$NON-NLS-1$
				|| trackerPanel.isEnabled("edit.copyObject")) { //$NON-NLS-1$
			if (editMenu.getItemCount() > 0)
				editMenu.addSeparator();

			if (trackerPanel.isEnabled("edit.copyData")) { //$NON-NLS-1$
				editMenu.add(edit_copyDataMenu); // refreshed in edit menu mouse listener
				TreeMap<Integer, TableTrackView> dataViews = getDataViews();
				edit_copyDataMenu.setEnabled(!dataViews.isEmpty());
				if (dataViews.isEmpty()) {
					edit_copyDataMenu.setText(TrackerRes.getString("TableTrackView.Action.CopyData")); //$NON-NLS-1$
				} else {
					Integer key = dataViews.firstKey();
					TableTrackView view = dataViews.get(key);
					view.refreshCopyDataMenu(edit_copyDataMenu);
					String text = edit_copyDataMenu.getText();
					edit_copyDataMenu.setText(text + " (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (trackerPanel.isEnabled("edit.copyImage")) { //$NON-NLS-1$
				editMenu.add(edit_copyImageMenu);
			}

			// copy object menu
			if (trackerPanel.isEnabled("edit.copyObject")) { //$NON-NLS-1$
				editMenu.add(edit_copyObjectMenu);
				edit_copyObjectMenu.setText(TrackerRes.getString("TMenuBar.Menu.CopyObject")); //$NON-NLS-1$
				edit_copyObjectMenu.removeAll();
				Action copyObjectAction = new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String s = ((JMenuItem) e.getSource()).getActionCommand();
						if ("coords".equals(s)) { //$NON-NLS-1$
							TrackerIO.copyXML(trackerPanel.getCoords());
						} else if ("clip".equals(s)) { //$NON-NLS-1$
							TrackerIO.copyXML(trackerPanel.getPlayer().getVideoClip());
						} else { // must be a track
							TTrack track = trackerPanel.getTrack(s);
							if (track != null)
								TrackerIO.copyXML(track);
						}
					}
				};
				// copy videoclip and coords items
				JMenuItem item = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Coords")); //$NON-NLS-1$
				item.setActionCommand("coords"); //$NON-NLS-1$
				item.addActionListener(copyObjectAction);
				edit_copyObjectMenu.add(item);
				item = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.VideoClip")); //$NON-NLS-1$
				item.setActionCommand("clip"); //$NON-NLS-1$
				item.addActionListener(copyObjectAction);
				edit_copyObjectMenu.add(item);
				// copy track items
				for (TTrack next : trackerPanel.getTracks()) {
					if (next == trackerPanel.getAxes() || next instanceof PerspectiveTrack)
						continue;
					item = new JMenuItem(next.getName());
					item.setActionCommand(next.getName());
					item.addActionListener(copyObjectAction);
					edit_copyObjectMenu.add(item);
				}
			}
		}

		// paste and autopaste items
		if (trackerPanel.isEnabled("edit.paste")) { //$NON-NLS-1$
			if (editMenu.getItemCount() > 0)
				editMenu.addSeparator();
			editMenu.add(edit_pasteItem);
			TFrame frame = trackerPanel.getTFrame();
			if (frame != null) {
				edit_autopasteCheckbox.setSelected(frame.alwaysListenToClipboard);
				editMenu.add(edit_autopasteCheckbox);
			}
		}

		edit_deleteTracksMenu.removeAll();
		edit_deleteTracksMenu.add(edit_delTracks_deleteSelectedPointItem);
		edit_deleteTracksMenu.addSeparator();
		edit_deleteTracksMenu.setEnabled(hasTracks);

		// delete and clear menus
		if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
			if (editMenu.getItemCount() > 0)
				editMenu.addSeparator();
			if (trackerPanel.isEnabled("track.delete") || hasTracks) { //$NON-NLS-1$
				editMenu.add(edit_deleteTracksMenu);
			}
		}
		// number menu
		if (trackerPanel.isEnabled("number.formats") || trackerPanel.isEnabled("number.units")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (editMenu.getItemCount() > 0)
				editMenu.addSeparator();
			editMenu.add(edit_numberMenu);
			edit_numberMenu.removeAll();
			if (trackerPanel.isEnabled("number.formats")) //$NON-NLS-1$
				edit_numberMenu.add(edit_formatsItem);
			if (trackerPanel.isEnabled("number.units")) //$NON-NLS-1$
				edit_numberMenu.add(edit_unitsItem);
		}
		// add size menu
		if (trackerPanel.isEnabled("edit.matSize")) { //$NON-NLS-1$
			if (editMenu.getItemCount() > 0)
				editMenu.addSeparator();
			editMenu.add(edit_matSizeMenu);
		}
		if (editMenu.getItemCount() > 0)
			editMenu.addSeparator();
		editMenu.add(edit_fontSizeMenu);
		refreshMatSizes(trackerPanel.getVideo());
		edit_languageMenu.removeAll();
		for (int i = 0; i < Tracker.locales.length; i++) {
			edit_languageMenu.add(languageItems[i]);
		}
		edit_languageMenu.addSeparator();
		edit_languageMenu.add(edit_lang_otherLanguageItem);
		if (editMenu.getItemCount() > 0)
			editMenu.addSeparator();
		editMenu.add(edit_languageMenu);
		if (editMenu.getItemCount() > 0)
			editMenu.addSeparator();
		editMenu.add(edit_configItem);
		FontSizer.setMenuFonts(editMenu);
	}

	protected void refreshFileMenu() {
		// refresh file menu
		fileMenu.removeAll();
		if (org.opensourcephysics.display.OSPRuntime.applet == null) {
			// update save and close items
			file_saveItem.setEnabled(trackerPanel.getDataFile() != null);
			String name = trackerPanel.getTitle();
			name = " \"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			file_closeItem.setText(TrackerRes.getString("TActions.Action.Close") + name); //$NON-NLS-1$
			file_saveItem.setText(TrackerRes.getString("TActions.Action.Save") + name); //$NON-NLS-1$
		}
		if (org.opensourcephysics.display.OSPRuntime.applet == null) {
			if (trackerPanel.isEnabled("file.new")) { //$NON-NLS-1$
				fileMenu.add(file_newTabItem);
			}
			if (trackerPanel.isEnabled("file.open")) { //$NON-NLS-1$
				if (fileMenu.getItemCount() > 0)
					fileMenu.addSeparator();
				fileMenu.add(file_openItem);
//    fileMenu.add(openURLItem);
				fileMenu.add(file_openRecentMenu);
				refreshRecentFilesMenu();
			}
			boolean showLib = trackerPanel.isEnabled("file.open") || trackerPanel.isEnabled("file.export"); //$NON-NLS-1$ //$NON-NLS-2$
			if (showLib && trackerPanel.isEnabled("file.library")) { //$NON-NLS-1$
				if (fileMenu.getItemCount() > 0)
					fileMenu.addSeparator();
				if (trackerPanel.isEnabled("file.open")) //$NON-NLS-1$
					fileMenu.add(file_openBrowserItem);
//				if (trackerPanel.isEnabled("file.export")) fileMenu.add(saveZipAsItem); //$NON-NLS-1$
			}
			if (trackerPanel.isEnabled("file.close")) { //$NON-NLS-1$
				if (fileMenu.getItemCount() > 0)
					fileMenu.addSeparator();
				fileMenu.add(file_closeItem);
				fileMenu.add(file_closeAllItem);
			}
			if (trackerPanel.isEnabled("file.save") //$NON-NLS-1$
					|| trackerPanel.isEnabled("file.saveAs")) { //$NON-NLS-1$
				if (fileMenu.getItemCount() > 0)
					fileMenu.addSeparator();
				if (trackerPanel.isEnabled("file.save")) //$NON-NLS-1$
					fileMenu.add(file_saveItem);
				if (trackerPanel.isEnabled("file.saveAs")) { //$NON-NLS-1$
					fileMenu.add(file_saveAsItem);
//      if (trackerPanel.getVideo()!=null) {
//      	fileMenu.add(saveVideoAsItem);
//      }
					fileMenu.add(file_saveZipAsItem);
					fileMenu.add(file_saveTabsetAsItem);
				}
			}
			if (trackerPanel.isEnabled("file.import") //$NON-NLS-1$
					|| trackerPanel.isEnabled("file.export")) { //$NON-NLS-1$
				if (fileMenu.getItemCount() > 0)
					fileMenu.addSeparator();
				if (trackerPanel.isEnabled("file.import")) //$NON-NLS-1$
					fileMenu.add(file_importMenu);
				if (trackerPanel.isEnabled("file.export")) //$NON-NLS-1$
					fileMenu.add(file_exportMenu);
			}
		}
		if (fileMenu.getItemCount() > 0)
			fileMenu.addSeparator();
		fileMenu.add(file_propertiesItem);
		if (trackerPanel.isEnabled("file.print")) { //$NON-NLS-1$
			if (fileMenu.getItemCount() > 0)
				fileMenu.addSeparator();
			fileMenu.add(file_printFrameItem);
		}
		// exit menu always added except in applets
		if (org.opensourcephysics.display.OSPRuntime.applet == null) {
			if (fileMenu.getItemCount() > 0)
				fileMenu.addSeparator();
			fileMenu.add(file_exitItem);
		}
		FontSizer.setMenuFonts(fileMenu);
	}

	private void refreshRecentFilesMenu() {
		TFrame frame = trackerPanel.getTFrame();
		if (frame != null) {
			frame.refreshOpenRecentMenu(file_openRecentMenu);
		}
	}

	protected void refreshCoordsMenu() {

		// refresh coords menu
		coordsMenu.removeAll();
		coordsMenu.add(coords_showUnitDialogItem);
		if (trackerPanel.isEnabled("coords.locked")) { //$NON-NLS-1$
			if (coordsMenu.getItemCount() > 0)
				coordsMenu.addSeparator();
			coordsMenu.add(coords_lockedCoordsItem);
		}
		if (trackerPanel.isEnabled("coords.origin") || //$NON-NLS-1$
		trackerPanel.isEnabled("coords.angle") || //$NON-NLS-1$
		trackerPanel.isEnabled("coords.scale")) { //$NON-NLS-1$
			if (coordsMenu.getItemCount() > 0)
				coordsMenu.addSeparator();
			if (trackerPanel.isEnabled("coords.origin")) //$NON-NLS-1$
				coordsMenu.add(coords_fixedOriginItem);
			if (trackerPanel.isEnabled("coords.angle")) //$NON-NLS-1$
				coordsMenu.add(coords_fixedAngleItem);
			if (trackerPanel.isEnabled("coords.scale")) //$NON-NLS-1$
				coordsMenu.add(coords_fixedScaleItem);
//  coordsMenu.add(applyCurrentFrameToAllItem);
		}
		if (trackerPanel.isEnabled("coords.refFrame")) { //$NON-NLS-1$
			if (coordsMenu.getItemCount() > 0)
				coordsMenu.addSeparator();
			coordsMenu.add(coords_refFrameMenu);
		}


		// clear the ref frame menu and button group
		coords_refFrameMenu.removeAll();
		Enumeration<AbstractButton> e = refFrameGroup.getElements();
		while (e.hasMoreElements()) {
			refFrameGroup.remove(e.nextElement());
		}
		
		// update coords menu items
		ImageCoordSystem coords = trackerPanel.getCoords();
		boolean defaultCoords = !(coords instanceof ReferenceFrame);
		coords_lockedCoordsItem.setSelected(coords.isLocked());
		coords_fixedOriginItem.setSelected(coords.isFixedOrigin());
		coords_fixedAngleItem.setSelected(coords.isFixedAngle());
		coords_fixedScaleItem.setSelected(coords.isFixedScale());
		coords_fixedOriginItem.setEnabled(defaultCoords && !coords.isLocked());
		coords_fixedAngleItem.setEnabled(defaultCoords && !coords.isLocked());
		boolean stickAttached = false;
		ArrayList<TapeMeasure> tapes = trackerPanel.getDrawables(TapeMeasure.class);
		for (int i = 0, n = tapes.size(); i < n; i++) {
			TapeMeasure tape = tapes.get(i);
			if (tape.isStickMode() && tape.attachments != null
					&& (tape.attachments[0] != null || tape.attachments[1] != null)) {
				stickAttached = true;
				break;
			}
		}
		coords_fixedScaleItem.setEnabled(defaultCoords && !coords.isLocked() && !stickAttached);
		coords_refFrameMenu.setEnabled(!coords.isLocked());
		// add default reference frame item
		refFrameGroup.add(defaultRefFrameItem);
		coords_refFrameMenu.add(defaultRefFrameItem);
		PointMass originTrack = getOriginTrack();
		if (originTrack == null)
			defaultRefFrameItem.setSelected(true);
		FontSizer.setMenuFonts(coordsMenu);
	}

	/**
	 * 
	 * @return the track currently serving as origin
	 */
	private PointMass getOriginTrack() {
		ImageCoordSystem coords = trackerPanel.getCoords();
		return (coords instanceof ReferenceFrame ? ((ReferenceFrame) coords).getOriginTrack() : null);
	}

	protected void refreshVideoMenu() {
		Video video = trackerPanel.getVideo();
		boolean hasVideo = (video != null);
		videoMenu.removeAll();
		// import video item at top
		boolean importEnabled = trackerPanel.isEnabled("video.import") //$NON-NLS-1$
				|| trackerPanel.isEnabled("video.open"); //$NON-NLS-1$
		if (importEnabled && org.opensourcephysics.display.OSPRuntime.applet == null) {
			if (hasVideo)
				video_openVideoItem.setText(TrackerRes.getString("TMenuBar.MenuItem.Replace")); //$NON-NLS-1$
			else
				video_openVideoItem.setText(TrackerRes.getString("TActions.Action.ImportVideo")); //$NON-NLS-1$
			videoMenu.add(video_openVideoItem);
		}
		// close video item
		if (hasVideo) {
			if (trackerPanel.isEnabled("video.close")) //$NON-NLS-1$
				videoMenu.add(video_closeVideoItem);
		}
		if (videoMenu.getItemCount() > 0)
			videoMenu.addSeparator();

		videoMenu.add(video_goToItem);
		videoMenu.addSeparator();

		if (importEnabled && hasVideo && video instanceof ImageVideo) {
			video_editVideoItem.setSelected(((ImageVideo) video).isEditable());
			videoMenu.add(video_editVideoItem);
			videoMenu.addSeparator();
		}
		// pasteImage items
		if (importEnabled)
			videoMenu.add(hasVideo ? video_pasteImageMenu : video_pasteImageItem);
		if (!hasVideo)
			return;
		boolean isEditableVideo = importEnabled && video instanceof ImageVideo && ((ImageVideo) video).isEditable();
		if (isEditableVideo && importEnabled) {
			video_pasteImageMenu.add(video_pasteImageBeforeItem);
			video_pasteImageMenu.add(video_pasteImageAfterItem);
			videoMenu.add(video_importImageMenu);
			videoMenu.add(video_removeImageItem);
			video_removeImageItem.setEnabled(video.getFrameCount() > 1);
		} else {
			video_pasteImageMenu.remove(video_pasteImageBeforeItem);
			video_pasteImageMenu.remove(video_pasteImageAfterItem);
		}
		// video visible and playAllSteps items
		if (trackerPanel.isEnabled("video.visible")) { //$NON-NLS-1$
			if (videoMenu.getItemCount() > 0)
				videoMenu.addSeparator();
			videoMenu.add(video_videoVisibleItem);
		}
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		video_playAllStepsItem.setSelected(clip.isPlayAllSteps());
		videoMenu.add(video_playAllStepsItem);
		// smooth play item for xuggle videos
		if (video instanceof SmoothPlayable) {
			video_playXuggleSmoothlyItem.setSelected(((SmoothPlayable) video).isSmoothPlay());
			videoMenu.add(video_playXuggleSmoothlyItem);
		}
		// video filters menu
		if (trackerPanel.isEnabled("video.filters")) { //$NON-NLS-1$
			// clear filters menu
			video_filtersMenu.removeAll();
			// add newFilter menu
			video_filtersMenu.add(video_filter_newFilterMenu);
			// add filter items to the newFilter menu
			video_filter_newFilterMenu.removeAll();
			synchronized (trackerPanel.getFilters()) {
				for (String name : trackerPanel.getFilters().keySet()) {
					String shortName = name;
					int i = shortName.lastIndexOf('.');
					if (i > 0 && i < shortName.length() - 1) {
						shortName = shortName.substring(i + 1);
					}
					i = shortName.indexOf("Filter"); //$NON-NLS-1$
					if (i > 0 && i < shortName.length() - 1) {
						shortName = shortName.substring(0, i);
					}
					shortName = MediaRes.getString("VideoFilter." + shortName); //$NON-NLS-1$
					JMenuItem item = new JMenuItem(shortName);
					item.setActionCommand(name);
					item.addActionListener(actions.get("videoFilter")); //$NON-NLS-1$
					video_filter_newFilterMenu.add(item);
				}
			}
			// get current filter stack
			FilterStack stack = video.getFilterStack();
			// listen to the stack for filter changes
			stack.removePropertyChangeListener("filter", TMenuBar.this); //$NON-NLS-1$
			stack.addPropertyChangeListener("filter", TMenuBar.this); //$NON-NLS-1$
			// add current filters, if any, to the filters menu
			if (!stack.getFilters().isEmpty()) {
				video_filtersMenu.addSeparator();
				Iterator<Filter> it2 = stack.getFilters().iterator();
				while (it2.hasNext()) {
					Filter filter = it2.next();
					video_filtersMenu.add(filter.getMenu(video));
				}
			}
			// add paste filter item
			video_filtersMenu.addSeparator();
			video_filtersMenu.add(pasteFilterItem);
			// add clearFiltersItem
			if (!stack.getFilters().isEmpty()) {
				video_filtersMenu.addSeparator();
				video_filtersMenu.add(clearFiltersItem);
			}

			if (videoMenu.getItemCount() > 0)
				videoMenu.addSeparator();
			videoMenu.add(video_filtersMenu);
		}
		videoMenu.addSeparator();
//	if (isXtractorType) videoMenu.add(checkDurationsItem);
		videoMenu.add(video_aboutVideoItem);
		
		// hack to eliminate extra separator at end of video menu
		int n = videoMenu.getMenuComponentCount();
		if (n > 0 && videoMenu.getMenuComponent(n - 1) instanceof JSeparator) {
			videoMenu.remove(n - 1);
		}

		// add empty menu items to menus with no items
		if (videoMenu.getItemCount() == 0) {
			videoMenu.add(video_emptyVideoItem);
		}
		if (trackMenu.getItemCount() == 0) {
			trackMenu.add(video_emptyTracksItem);
		}
		if (coordsMenu.getItemCount() == 0) {
			coordsMenu.add(coords_emptyCoordsItem);
		}
		FontSizer.setMenuFonts(videoMenu);
	}
	
	protected void refreshPasteItem() {
		// enable and refresh paste item if clipboard contains xml string data		
		String paste = actions.get("paste").getValue(Action.NAME).toString(); //$NON-NLS-1$
		edit_pasteItem.setText(paste);
		edit_pasteItem.setEnabled(false);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable data = clipboard.getContents(null);
		if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String s = (String) data.getTransferData(DataFlavor.stringFlavor);
				control.readXML(s);
				Class<?> type = control.getObjectClass();
				if (control.failedToRead() && ParticleDataTrack.getImportableDataName(s) != null) {
					paste = TrackerRes.getString("ParticleDataTrack.Button.Paste.Text"); //$NON-NLS-1$
					edit_pasteItem.setEnabled(true);
					edit_pasteItem.setText(paste);
				} else if (TTrack.class.isAssignableFrom(type)) {
					edit_pasteItem.setEnabled(true);
					String name = control.getString("name"); //$NON-NLS-1$
					edit_pasteItem.setText(paste + " " + name); //$NON-NLS-1$
				} else if (ImageCoordSystem.class.isAssignableFrom(type)) {
					edit_pasteItem.setEnabled(true);
					edit_pasteItem.setText(paste + " " + TrackerRes.getString("TMenuBar.MenuItem.Coords")); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (VideoClip.class.isAssignableFrom(type)) {
					edit_pasteItem.setEnabled(true);
					edit_pasteItem.setText(paste + " " + TrackerRes.getString("TMenuBar.MenuItem.VideoClip")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch (Exception ex) {
			}
		}

	}

	/**
	 * Cleans up this menubar
	 */
	public void dispose() {
		menuBars.remove(trackerPanel);
		trackerPanel.removePropertyChangeListener("locked", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_TRACK, this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR, this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("video", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("size", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("datafile", this); //$NON-NLS-1$
		Video video = trackerPanel.getVideo();
		if (video != null) {
			video.getFilterStack().removePropertyChangeListener("filter", TMenuBar.this); //$NON-NLS-1$
		}
		for (Integer n : TTrack.activeTracks.keySet()) {
			TTrack track = TTrack.activeTracks.get(n);
			track.removePropertyChangeListener("locked", this); //$NON-NLS-1$
		}
		actions.clear();
		actions = null;
		TActions.actionMaps.remove(trackerPanel);
		for (int i = 0; i < edit_copyViewImageItems.length; i++) {
			edit_copyViewImageItems[i] = null;
		}
		trackerPanel = null;
	}

	@Override
	public void finalize() {
		OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
	}

	/**
	 * Responds to the following events: "selectedtrack", "selectedpoint", "track",
	 * "clear", "video" from tracker panel, "filter" from filter stack, "datafile"
	 * from VideoPanel.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		switch (e.getPropertyName()) {
		case "selectedtrack": // selected track has changed //$NON-NLS-1$
		case "datafile": // datafile has changed //$NON-NLS-1$
		case "selectedpoint": // selected point has changed //$NON-NLS-1$
		case "video": // video has changed //$NON-NLS-1$
		case "size": // image size has changed //$NON-NLS-1$
		case "locked": // track or coords locked/unlocked //$NON-NLS-1$
			break;
		case "filter": // filter has been added or removed //$NON-NLS-1$
			if (refreshing) {
				return;
			}
			// post undoable edit if individual filter was removed
			Filter filter = (Filter) e.getOldValue();
			if (filter != null) {
				Undo.postFilterDelete(trackerPanel, filter);
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_TRACK: // track has been added or removed //$NON-NLS-1$
			if (e.getOldValue() instanceof TTrack) { // track has been removed
				TTrack track = (TTrack) e.getOldValue();
				track.removePropertyChangeListener("locked", this); //$NON-NLS-1$
				trackerPanel.setSelectedTrack(null);
			}
			break;
		case TrackerPanel.PROPERTY_TRACKERPANEL_CLEAR: // tracks have been cleared //$NON-NLS-1$
			for (Integer n : TTrack.activeTracks.keySet()) {
				TTrack track = TTrack.activeTracks.get(n);
				track.removePropertyChangeListener("locked", this); //$NON-NLS-1$
			}
			break;
		default:
			return;
		}
		refresh(REFRESH_PROPERTY_ + " " + e.getPropertyName());

	}

	protected TreeMap<Integer, TableTrackView> getDataViews() {
		TreeMap<Integer, TableTrackView> dataViews = new TreeMap<Integer, TableTrackView>();
		if (trackerPanel.getTFrame() == null)
			return dataViews;
		Container[] c = trackerPanel.getTFrame().getViews(trackerPanel);
		for (int i = 0; i < c.length; i++) {
			if (trackerPanel.getTFrame().isViewOpen(i, trackerPanel)) {
				if (c[i] instanceof TViewChooser) {
					TViewChooser chooser = (TViewChooser) c[i];
					TView tview = chooser.getSelectedView();
					if (tview!=null && tview instanceof TableTView) {
						TableTView tableView = (TableTView) tview;
						TTrack track = tableView.getSelectedTrack();
						if (track != null) {
							for (Step step : track.getSteps()) {
								if (step != null) {
									TableTrackView trackView = (TableTrackView) tableView.getTrackView(track);
									dataViews.put(i + 1, trackView);
								}
							}
						}
					}
				}
			}
		}
		return dataViews;
	}

	private TFrame getFrame() {
		if (frame == null)
			frame = trackerPanel.getTFrame();
		return frame;
	}

	protected void refreshMatSizes(Video video) {
		// determine if default size is being used
		boolean videoSizeItemShown = false;
		for (Component c : edit_matSizeMenu.getMenuComponents()) {
			videoSizeItemShown = videoSizeItemShown || c == edit_matsize_videoSizeItem;
		}
		boolean isDefaultSize = !videoSizeItemShown || edit_matsize_videoSizeItem.isSelected();
		edit_matSizeMenu.removeAll();
		int vidWidth = 1;
		int vidHeight = 1;
		if (video != null) {
			Dimension d = trackerPanel.getVideo().getImageSize();
			vidWidth = d.width;
			vidHeight = d.height;
			String s = TrackerRes.getString("TMenuBar.Menu.Video"); //$NON-NLS-1$
			String description = " (" + s.toLowerCase() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			edit_matsize_videoSizeItem.setText(vidWidth + "x" + vidHeight + description); //$NON-NLS-1$
			edit_matsize_videoSizeItem.setActionCommand(vidWidth + "x" + vidHeight); //$NON-NLS-1$
			if (isDefaultSize && trackerPanel != null && trackerPanel.getMat() != null) {
				Dimension dim = trackerPanel.getMat().mat.getSize();
				if (vidWidth != dim.width || vidHeight != dim.height) {
					trackerPanel.setImageSize(vidWidth, vidHeight);
				}
			}
		} else
			edit_matsize_videoSizeItem.setActionCommand("0x0"); //$NON-NLS-1$
		int imageWidth = (int) trackerPanel.getImageWidth();
		int imageHeight = (int) trackerPanel.getImageHeight();
		for (Enumeration<AbstractButton> e = matSizeGroup.getElements(); e.hasMoreElements();) {
			JRadioButtonMenuItem next = (JRadioButtonMenuItem) e.nextElement();
			String s = next.getActionCommand();
			int i = s.indexOf("x"); //$NON-NLS-1$
			int w = Integer.parseInt(s.substring(0, i));
			int h = Integer.parseInt(s.substring(i + 1));
			if (w >= vidWidth & h >= vidHeight) {
				edit_matSizeMenu.add(next);
				if (next != edit_matsize_videoSizeItem && next.getActionCommand().equals(edit_matsize_videoSizeItem.getActionCommand())) {
					edit_matSizeMenu.remove(next);
				}
			}
			if (w == vidWidth && h == vidHeight) {
				edit_matsize_videoSizeItem.setSelected(true);
			} else if (w == imageWidth && h == imageHeight) {
				next.setSelected(true);
			}
		}
	}

	public static void setSelectedItem(TrackerPanel panel, String item, boolean open) {
		TMenuBar menubar = getMenuBar(panel);
		if (menubar != null) {
			switch (item) {
			case TFRAME_BOTTOM:
				menubar.bottomPaneItem.setSelected(open);
				break;
			case TFRAME_RIGHT:
				menubar.rightPaneItem.setSelected(open);
				break;
			}
		}
	}

	public static void refreshPopup(TrackerPanel panel, String item, JPopupMenu menu, boolean isOpening) {
		TMenuBar menubar = getMenuBar(panel);
		if (menubar != null) {
			switch (item) {
			case TTOOLBAR_TRACKS:
				menubar.refreshTracksPopup(menu);
				return;
			case MAINTVIEW_POPUP:
				menubar.refreshMainTViewPopup(menu, isOpening);
			}
		}
	}

	private void refreshMainTViewPopup(JPopupMenu popup, boolean isOpening) {
		if (!isOpening) {
			videoMenu.add(video_filtersMenu);
			return;
		}
		// video filters menu
		if (trackerPanel.getVideo() != null && trackerPanel.isEnabled("video.filters")) { //$NON-NLS-1$
		JMenu filtersMenu = video_filtersMenu;
			if (filtersMenu.getItemCount() > 0) {
				popup.addSeparator();
				popup.add(filtersMenu);
			}
		}
		if (track_createMenu.getItemCount() == 0) {
			 refreshTracksCreateMenu();
			for (Component c : newTrackItems) {
				track_createMenu.add(c);
			}
		}
		JMenu tracksMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Tracks")); //$NON-NLS-1$
		if (track_createMenu.getItemCount() > 0)
			tracksMenu.add(track_createMenu);
		if (track_cloneMenu.getItemCount() > 0 && trackerPanel.isEnabled("new.clone")) //$NON-NLS-1$
			tracksMenu.add(track_cloneMenu);
		// get list of tracks for track menus
		CoordAxes axes = trackerPanel.getAxes();
		ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
		// add track items
		if (!tracks.isEmpty()) {
			if (tracksMenu.getItemCount() > 0)
				tracksMenu.addSeparator();
			for (int i = 0, n = tracks.size(); i < n; i++) {
				tracksMenu.add(createTrackMenu(tracks.get(i)));
			}
		}
		// add axes and calibration tool items
		if (trackerPanel.isEnabled("button.axes") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
				|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
			if (tracksMenu.getItemCount() > 0)
				tracksMenu.addSeparator();
			if (axes != null && trackerPanel.isEnabled("button.axes")) { //$NON-NLS-1$
				tracksMenu.add(createTrackMenu(axes));
			}
			if (!trackerPanel.calibrationTools.isEmpty()) {
				for (TTrack next : trackerPanel.getTracks()) {
					if (trackerPanel.calibrationTools.contains(next)) {
						if (next instanceof TapeMeasure) {
							TapeMeasure tape = (TapeMeasure) next;
							if (tape.isStickMode() && !trackerPanel.isEnabled("calibration.stick")) //$NON-NLS-1$
								continue;
							if (!tape.isStickMode() && !trackerPanel.isEnabled("calibration.tape")) //$NON-NLS-1$
								continue;
						}
						if (next instanceof Calibration && !trackerPanel.isEnabled("calibration.points")) //$NON-NLS-1$
							continue;
						if (next instanceof OffsetOrigin && !trackerPanel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
							continue;
						tracksMenu.add(createTrackMenu(next));
					}
				}
			}
		}
		if (tracksMenu.getItemCount() > 0) {
			popup.addSeparator();
			popup.add(tracksMenu);
		}
	}

	public static void refreshMenus(TrackerPanel trackerPanel, String whereFrom) {
		TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
		if (menubar != null) {
			menubar.frame = trackerPanel.getTFrame();
			menubar.refresh(whereFrom);
		}
	}

}
