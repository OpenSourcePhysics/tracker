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

import java.beans.*;
import java.lang.reflect.Method;
import java.util.*;
import java.awt.datatransfer.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.opensourcephysics.media.core.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.*;

/**
 * This is the main menu for Tracker.
 *
 * @author Douglas Brown
 */
public class TMenuBar extends JMenuBar implements PropertyChangeListener {

  // static fields
  private static Map<TrackerPanel, TMenuBar> menuBars = new HashMap<TrackerPanel, TMenuBar>();
  private static XMLControl control = new XMLControlElement();

  // instance fields
  protected TrackerPanel trackerPanel;
  protected TFrame frame;
  protected Map<String, AbstractAction> actions;

  // file menu
  protected JMenu fileMenu;
  protected JMenuItem newTabItem;
  protected JMenuItem openItem;
  protected JMenuItem openURLItem;
  protected JMenuItem openBrowserItem;
  protected JMenu openRecentMenu;
  protected JMenuItem closeItem;
  protected JMenuItem closeAllItem;
  protected JMenuItem saveItem;
  protected JMenuItem saveAsItem;
  protected JMenuItem saveZipAsItem;
  protected JMenuItem saveVideoAsItem;
  protected JMenuItem saveTabsetAsItem;
  protected JMenu importMenu;
  protected JMenuItem importVideoItem;
  protected JMenuItem importTRKItem;
  protected JMenuItem importDataItem;
  protected JMenu exportMenu;
  protected JMenuItem exportZipItem;
  protected JMenuItem exportVideoItem;
  protected JMenuItem exportTRKItem;
  protected JMenuItem exportThumbnailItem;
  protected JMenuItem exportDataItem;
  protected JMenuItem captureVideoItem;
  protected JMenuItem propertiesItem;
  protected JMenuItem printFrameItem;
  protected JMenuItem exitItem;
  // edit menu
  protected JMenu editMenu;
  protected JMenuItem undoItem;
  protected JMenuItem redoItem;
  protected JMenu copyDataMenu;
  protected JMenu copyImageMenu;
  protected JMenuItem copyMainViewImageItem;
  protected JMenuItem copyFrameImageItem;
  protected JMenuItem[] copyViewImageItems;
  protected JMenu copyObjectMenu;
  protected JMenuItem pasteItem;
  protected JCheckBoxMenuItem autopasteCheckbox;
  protected JMenu deleteTracksMenu;
  protected JMenuItem deleteSelectedPointItem;
  protected JMenuItem clearTracksItem;
  protected JMenu numberMenu;
  protected JMenuItem formatsItem, unitsItem;
  protected JMenuItem configItem;
  protected JMenu matSizeMenu;
  protected ButtonGroup matSizeGroup;
  protected JMenu fontSizeMenu;
  protected ButtonGroup fontSizeGroup;
  protected JRadioButtonMenuItem videoSizeItem;
  protected JMenu languageMenu;
  protected JMenuItem[] languageItems;
  protected JMenuItem otherLanguageItem;
  protected JMenuItem propsItem;
  // video menu
  protected JMenu videoMenu;
  protected JCheckBoxMenuItem videoVisibleItem;
  protected JMenuItem goToItem;
  protected JMenu filtersMenu;
  protected JMenu newFilterMenu;
  protected JMenuItem pasteFilterItem;
  protected JMenuItem clearFiltersItem;
  protected JMenuItem openVideoItem;
  protected JMenuItem closeVideoItem;
  protected JMenu pasteImageMenu;
  protected JMenuItem pasteImageItem;
  protected JMenuItem pasteReplaceItem;
  protected JMenuItem pasteImageAfterItem;
  protected JMenuItem pasteImageBeforeItem;
  protected JMenu importImageMenu;
  protected JMenuItem addImageAfterItem;
  protected JMenuItem addImageBeforeItem;
  protected JMenuItem removeImageItem;
  protected JMenuItem editVideoItem;
  protected JMenuItem playAllStepsItem;
  protected JMenuItem playXuggleSmoothlyItem;
  protected JMenuItem aboutVideoItem;
  protected JMenuItem checkDurationsItem;
  protected JMenuItem emptyVideoItem;
  // tracks menu
  protected JMenu trackMenu;
  protected JMenu createMenu; // Tracks/New
  protected JMenu cloneMenu; // Tracks/Clone
  protected JMenu measuringToolsMenu;
  protected Component[] newTrackItems;
  protected JMenuItem newPointMassItem;
  protected JMenuItem newCMItem;
  protected JMenuItem newVectorItem;
  protected JMenuItem newVectorSumItem;
  protected JMenuItem newLineProfileItem;
  protected JMenuItem newRGBRegionItem;
  protected JMenuItem newProtractorItem;
  protected JMenuItem newTapeItem;
  protected JMenuItem newCircleFitterItem;
  protected JCheckBoxMenuItem axesVisibleItem;
  protected JMenuItem newAnalyticParticleItem;
  protected JMenu newDynamicParticleMenu;
  protected JMenuItem newDynamicParticleCartesianItem;
  protected JMenuItem newDynamicParticlePolarItem;
  protected JMenuItem newDynamicSystemItem;
  protected JMenu newDataTrackMenu;
  protected JMenuItem newDataTrackPasteItem;
  protected JMenuItem newDataTrackFromFileItem;
  protected JMenuItem newDataTrackFromEJSItem;
  protected JMenuItem dataTrackHelpItem;
  protected JMenuItem emptyTracksItem;
  // coords menu
  protected JMenu coordsMenu;
  protected JCheckBoxMenuItem lockedCoordsItem;
  protected JCheckBoxMenuItem fixedOriginItem;
  protected JCheckBoxMenuItem fixedAngleItem;
  protected JCheckBoxMenuItem fixedScaleItem;
  protected JMenu refFrameMenu;
  protected ButtonGroup refFrameGroup;
  protected JRadioButtonMenuItem defaultRefFrameItem;
  protected JMenuItem showUnitDialogItem;
  protected JMenuItem emptyCoordsItem;
  // window menu
  protected JMenu windowMenu;
  protected JMenuItem restoreItem;
  protected JCheckBoxMenuItem rightPaneItem;
  protected JCheckBoxMenuItem bottomPaneItem;
  protected JMenuItem trackControlItem;
  protected JMenuItem notesItem;
  protected JMenuItem dataBuilderItem;
  protected JMenuItem dataToolItem;
  // help menu
  protected JMenu helpMenu;
  // other fields
  protected boolean refreshing; // true when refreshing menus or redoing filter delete

  /**
   * Returns a TMenuBar for the specified trackerPanel.
   *
   * @param  panel the tracker panel
   * @return a TMenuBar
   */
  public static synchronized TMenuBar getMenuBar(TrackerPanel panel) {
    TMenuBar bar = menuBars.get(panel);
    if (bar == null) {
      bar = new TMenuBar(panel);
      menuBars.put(panel, bar);
    }
    return bar;
  }
  
  /**
   * Returns a new TMenuBar for the specified trackerPanel.
   *
   * @param  panel the tracker panel
   * @return a TMenuBar
   */
  public static synchronized TMenuBar getNewMenuBar(TrackerPanel panel) {
    TMenuBar bar = new TMenuBar(panel);
    menuBars.put(panel, bar);
    return bar;
  }
  
  protected void loadVideoMenu(JMenu vidMenu) {
  	/** empty block */
  }
  
  /**
   * Clears all menubars. This forces creation of new menus using new locale.
   */
  public static void clear() {
  	menuBars.clear();
  }
  
  /**
   * TrackerFrame constructor specifying the tracker panel.
   *
   * @param  panel the tracker panel
   */
  private TMenuBar(TrackerPanel panel) {
    setTrackerPanel(panel);
    actions = TActions.getActions(panel);
    createGUI();
    refresh();
  }

  /**
   * Sets the TrackerPanel for this menu bar
   *
   * @param panel the new drawing panel
   */
  protected void setTrackerPanel(TrackerPanel panel) {
    if (panel == null || panel == trackerPanel) return;
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener("locked", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("clear", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("video", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("size", this); //$NON-NLS-1$
      trackerPanel.removePropertyChangeListener("datafile", this); //$NON-NLS-1$
    }
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("locked", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("clear", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("video", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("size", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("datafile", this); //$NON-NLS-1$
  }

  /**
   *  Creates the menu bar.
   */
  protected void createGUI() {
    int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // file menu
    fileMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.File")); //$NON-NLS-1$
    add(fileMenu);
    fileMenu.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {mousePressed(e);}
      public void mousePressed(MouseEvent e) {
        // ignore when menu is about to close
        if (!fileMenu.isPopupMenuVisible()) return;
        // disable export data menu if no tables to export
        exportDataItem.setEnabled(!getDataViews().isEmpty());
        // disable saveTabsetAs item if only 1 tab is open
        TFrame frame = trackerPanel.getTFrame();
        saveTabsetAsItem.setEnabled(frame!=null && frame.getTabCount()>1);
      }
    });
    if( org.opensourcephysics.display.OSPRuntime.applet == null) {
      // new tab item
      newTabItem = new JMenuItem(actions.get("newTab")); //$NON-NLS-1$
      newTabItem.setAccelerator(KeyStroke.getKeyStroke('N', keyMask));
      fileMenu.addSeparator();
      // open item
      openItem = new JMenuItem(actions.get("open")); //$NON-NLS-1$
      openItem.setAccelerator(KeyStroke.getKeyStroke('O', keyMask));
      // open URL item
      openURLItem = new JMenuItem(actions.get("openURL")); //$NON-NLS-1$
      // open library browser item
      openBrowserItem = new JMenuItem(actions.get("openBrowser")); //$NON-NLS-1$
      // open recent
      openRecentMenu = new JMenu();
      // import menu
      importMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Import")); //$NON-NLS-1$
      importVideoItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Video")); //$NON-NLS-1$
      importVideoItem.addActionListener(actions.get("openVideo")); //$NON-NLS-1$
      importVideoItem.setAccelerator(KeyStroke.getKeyStroke('I', keyMask));
      importTRKItem = new JMenuItem(actions.get("import")); //$NON-NLS-1$
      importDataItem = new JMenuItem(actions.get("importData")); //$NON-NLS-1$
      importMenu.add(importVideoItem);
      importMenu.add(importTRKItem);
      importMenu.add(importDataItem);
      // close and close all items
      closeItem = new JMenuItem(actions.get("close")); //$NON-NLS-1$
      closeAllItem = new JMenuItem(actions.get("closeAll")); //$NON-NLS-1$
      fileMenu.addSeparator();
      // export menu
      exportMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Export")); //$NON-NLS-1$
      // export zip item
      exportZipItem = new JMenuItem(actions.get("saveZip")); //$NON-NLS-1$
      exportZipItem.setText(TrackerRes.getString("TMenuBar.MenuItem.ExportZIP")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
      exportMenu.add(exportZipItem);
      // export video item
      exportVideoItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.VideoClip")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
      exportVideoItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	ExportVideoDialog exporter = ExportVideoDialog.getDialog(trackerPanel);
        	exporter.setVisible(true);
        }
      });
      exportMenu.add(exportVideoItem);
      // export TRK item
      exportTRKItem = new JMenuItem(actions.get("export")); //$NON-NLS-1$
      exportMenu.add(exportTRKItem);
      // export thumbnail item
      exportThumbnailItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Thumbnail")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
      exportThumbnailItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	ThumbnailDialog exporter = ThumbnailDialog.getDialog(trackerPanel, true);
        	exporter.setVisible(true);
        }
      });
      exportMenu.add(exportThumbnailItem);
      // export data item
      exportDataItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Data")); //$NON-NLS-1$
      exportDataItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	ExportDataDialog exporter = ExportDataDialog.getDialog(trackerPanel);
        	exporter.setVisible(true);
        }
      });
      exportMenu.add(exportDataItem);
      fileMenu.addSeparator();
      // save item
      saveItem = new JMenuItem(actions.get("save")); //$NON-NLS-1$
      saveItem.setAccelerator(KeyStroke.getKeyStroke('S', keyMask));
      // saveAs item
      saveAsItem = new JMenuItem(actions.get("saveAs")); //$NON-NLS-1$
      // save zip item
      saveZipAsItem = new JMenuItem(actions.get("saveZip")); //$NON-NLS-1$
      // saveVideoAs item
      saveVideoAsItem = new JMenuItem(actions.get("saveVideo")); //$NON-NLS-1$
      // saveTabset item
      saveTabsetAsItem = new JMenuItem(actions.get("saveTabsetAs")); //$NON-NLS-1$
      fileMenu.addSeparator();
    }
    // properties item
    propertiesItem = new JMenuItem(actions.get("properties")); //$NON-NLS-1$
    // printFrame item
    printFrameItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PrintFrame")); //$NON-NLS-1$
    printFrameItem.setAccelerator(KeyStroke.getKeyStroke('P', keyMask));
    printFrameItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Component c = trackerPanel.getTFrame();
      	new TrackerIO.ComponentImage(c).print();
      }
    });    
    // exit item
    if( org.opensourcephysics.display.OSPRuntime.applet == null) {
      exitItem = new JMenuItem(actions.get("exit")); //$NON-NLS-1$
      exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', keyMask));
    }
    // edit menu
    editMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Edit")); //$NON-NLS-1$
    editMenu.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {mousePressed(e);}
      public void mousePressed(MouseEvent e) {
        // ignore when menu is about to close
        if (!editMenu.isPopupMenuVisible()) return;
        // enable deleteSelectedPoint item if a selection exists
        Step step = trackerPanel.getSelectedStep();
        TTrack track = trackerPanel.getSelectedTrack();
        boolean cantDeleteSteps = track==null || track.isLocked() || track.isDependent();
        deleteSelectedPointItem.setEnabled(!cantDeleteSteps && step!=null);
        // enable and refresh paste item if clipboard contains xml string data
        String paste = actions.get("paste").getValue(Action.NAME).toString(); //$NON-NLS-1$
      	pasteItem.setText(paste);
      	pasteItem.setEnabled(false);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable data = clipboard.getContents(null);
        if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          try {
	        	String s = (String)data.getTransferData(DataFlavor.stringFlavor);
            control.readXML(s);
            Class<?> type = control.getObjectClass();
            if (control.failedToRead() && ParticleDataTrack.getImportableDataName(s)!=null) {
            	paste = TrackerRes.getString("ParticleDataTrack.Button.Paste.Text"); //$NON-NLS-1$
            	pasteItem.setEnabled(true);
            	pasteItem.setText(paste);
            }
            else if (TTrack.class.isAssignableFrom(type)) {
              pasteItem.setEnabled(true);
            	String name = control.getString("name"); //$NON-NLS-1$
            	pasteItem.setText(paste+" "+name); //$NON-NLS-1$
            }
            else if (ImageCoordSystem.class.isAssignableFrom(type)) {
              pasteItem.setEnabled(true);
            	pasteItem.setText(paste+" "+TrackerRes.getString("TMenuBar.MenuItem.Coords")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else if (VideoClip.class.isAssignableFrom(type)) {
              pasteItem.setEnabled(true);
            	pasteItem.setText(paste+" "+TrackerRes.getString("TMenuBar.MenuItem.VideoClip")); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
          catch (Exception ex) {          	
          }
        }
        // refresh copyData menu
        TreeMap<Integer, TableTrackView> dataViews = getDataViews();
        copyDataMenu.removeAll();
        copyDataMenu.setEnabled(!dataViews.isEmpty());
        if (dataViews.isEmpty()) {
        	copyDataMenu.setText(TrackerRes.getString("TableTrackView.Action.CopyData")); //$NON-NLS-1$
        }
        else if (dataViews.size()==1) {
        	Integer key = dataViews.firstKey();
        	TableTrackView view = dataViews.get(key);
        	view.refreshCopyDataMenu(copyDataMenu);
        	String text = copyDataMenu.getText();
        	copyDataMenu.setText(text+" ("+key+")"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else {
        	copyDataMenu.setText(TrackerRes.getString("TableTrackView.Action.CopyData")); //$NON-NLS-1$
        	for (int key: dataViews.keySet()) {
	        	TableTrackView view = dataViews.get(key);
	        	JMenu menu = new JMenu();
	        	copyDataMenu.add(view.refreshCopyDataMenu(menu));
	        	String text = menu.getText();
	        	menu.setText(text+" ("+key+")"); //$NON-NLS-1$ //$NON-NLS-2$
        	}
        }
        // refresh copyImage menu--include only open views
        copyImageMenu.remove(copyFrameImageItem);
        final Container[] views = trackerPanel.getTFrame().getViews(trackerPanel);
        // check that array size is correct and if not, make new menu items
        if (copyViewImageItems.length != views.length) {
          Action copyView = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
              int i = Integer.parseInt(e.getActionCommand());
              new TrackerIO.ComponentImage(views[i]).copyToClipboard();
            }
          };
          copyViewImageItems = new JMenuItem[views.length];
          for (int i = 0; i < views.length; i++) {
            copyViewImageItems[i] = new JMenuItem();
            String command = String.valueOf(i);
            copyViewImageItems[i].setActionCommand(command);
            copyViewImageItems[i].setAction(copyView);
          }        	
        }
        // add menu items for open views
        for (int i = 0; i < views.length; i++) {
          if (trackerPanel.getTFrame().isViewOpen(i, trackerPanel)) {
          	String viewname = null;
            if (views[i] instanceof TViewChooser) {
              TViewChooser chooser = (TViewChooser)views[i];
              viewname = chooser.getSelectedView().getViewName();
            }
            else viewname = TrackerRes.getString("TFrame.View.Unknown"); //$NON-NLS-1$
            copyViewImageItems[i].setText(viewname + " ("+(i+1)+")"); //$NON-NLS-1$  //$NON-NLS-2$ 
            String command = String.valueOf(i);
            copyViewImageItems[i].setActionCommand(command);
            copyImageMenu.add(copyViewImageItems[i]);
          }
          else {
            copyImageMenu.remove(copyViewImageItems[i]);
          }
        }
        copyImageMenu.add(copyFrameImageItem);
      	FontSizer.setFonts(editMenu, FontSizer.getLevel());        
        editMenu.revalidate();
      }
    });
    add(editMenu);
    // undo/redo items
    undoItem = new JMenuItem();
    undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', keyMask));
    undoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	trackerPanel.setSelectedPoint(null);
        trackerPanel.selectedSteps.clear();
      	Undo.undo(trackerPanel);
      }
    });    
    redoItem = new JMenuItem();
    redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', keyMask));
    redoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Undo.redo(trackerPanel);
      	trackerPanel.setSelectedPoint(null);
        trackerPanel.selectedSteps.clear();
      }
    });    
    // paste items
    pasteItem = editMenu.add(actions.get("paste")); //$NON-NLS-1$
    pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', keyMask));
    editMenu.addSeparator();
    // autopaste checkbox
    autopasteCheckbox = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.Checkbox.Autopaste")); //$NON-NLS-1$
    autopasteCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	trackerPanel.getTFrame().alwaysListenToClipboard = autopasteCheckbox.isSelected();
      	trackerPanel.getTFrame().checkClipboardListener();
      }
    });    
    // copy data menu
    copyDataMenu = new JMenu();
    
    // copy image menu
    copyImageMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.CopyImage")); //$NON-NLS-1$
    copyFrameImageItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CopyFrame")); //$NON-NLS-1$
    copyFrameImageItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Component c = trackerPanel.getTFrame();
      	new TrackerIO.ComponentImage(c).copyToClipboard();
      }
    });    
    copyMainViewImageItem = new JMenuItem(
    				TrackerRes.getString("TMenuBar.MenuItem.CopyMainView") + " (0)"); //$NON-NLS-1$ //$NON-NLS-2$ 
    copyMainViewImageItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	new TrackerIO.ComponentImage(trackerPanel).copyToClipboard();
      }
    }); 
    copyImageMenu.add(copyMainViewImageItem);
    copyViewImageItems = new JMenuItem[0];
    
    // copy object menu
    copyObjectMenu = new JMenu();
    
    // pasteImage menu
    pasteImageMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.PasteImage")); //$NON-NLS-1$
    
    // pasteImage item
    ActionListener pasteImageAction = new AbstractAction() {
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
    pasteImageItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PasteImage")); //$NON-NLS-1$
    pasteImageItem.addActionListener(pasteImageAction);
    // editVideoItem and saveEditsVideoItem
    editVideoItem = new JCheckBoxMenuItem(new AbstractAction(
				TrackerRes.getString("TMenuBar.MenuItem.EditVideoFrames")) { //$NON-NLS-1$
		  public void actionPerformed(ActionEvent e) {
        Video video = trackerPanel.getVideo();
		  	if (video!=null && video instanceof ImageVideo) {
		  		boolean edit = editVideoItem.isSelected();
		  		if (!edit) {
			  		// convert video to non-editable?
			  		ImageVideo iVideo = (ImageVideo)video;
			  		try {
							iVideo.setEditable(false);
							refresh();
							TTrackBar.refreshMemoryButton();
						} catch (Exception e1) {
							Toolkit.getDefaultToolkit().beep();
						}
		  		}
		  		else {
			  		// warn user that memory requirements may be large
			    	String message = TrackerRes.getString("TMenuBar.Dialog.RequiresMemory.Message1"); //$NON-NLS-1$ 
			    	message += "\n"+TrackerRes.getString("TMenuBar.Dialog.RequiresMemory.Message2"); //$NON-NLS-1$ //$NON-NLS-2$ 
			    	int response = javax.swing.JOptionPane.showConfirmDialog(
			    			trackerPanel.getTFrame(), 
			    			message,
			    			TrackerRes.getString("TMenuBar.Dialog.RequiresMemory.Title"), //$NON-NLS-1$ 
			    			javax.swing.JOptionPane.OK_CANCEL_OPTION, 
			    			javax.swing.JOptionPane.INFORMATION_MESSAGE);
			    	if (response == javax.swing.JOptionPane.YES_OPTION) {
			    		boolean error = false;
				  		// convert video to editable
				  		ImageVideo iVideo = (ImageVideo)video;
				  		try {
								iVideo.setEditable(true);
								refresh();
								TTrackBar.refreshMemoryButton();
							} catch (Exception ex) {
								Toolkit.getDefaultToolkit().beep();
								error = true;
							} catch (Error er) {
								Toolkit.getDefaultToolkit().beep();
								error = true;
					  		throw(er);
							} finally {
								if (error) {
						  		// try to revert to non-editable
						  		try {
										iVideo.setEditable(false);
									} catch (Exception ex) {
									} catch (Error er) {}
					  			System.gc();
									refresh();
									TTrackBar.refreshMemoryButton();
								}
							}
			    	}
			    	else { // user canceled
			    		editVideoItem.setSelected(false);
			    	}
		  		}
		  	}
		  }
		});
    
    // pasteReplace item
    pasteReplaceItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PasteReplace")); //$NON-NLS-1$
    pasteReplaceItem.addActionListener(pasteImageAction);
    // pasteAfter item
    pasteImageAfterItem = new JMenuItem(new AbstractAction(
    				TrackerRes.getString("TMenuBar.MenuItem.PasteAfter")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Image image = TrackerIO.getClipboardImage();
        if (image != null) {
        	int n = trackerPanel.getFrameNumber();
  				ImageVideo imageVid = (ImageVideo)trackerPanel.getVideo();
  				imageVid.insert(image, n+1);
  				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
  				clip.setStepCount(imageVid.getFrameCount());
  				trackerPanel.getPlayer().setStepNumber(clip.frameToStep(n+1));
        	refresh();
        }
      }
    });
    // pasteBefore item
    pasteImageBeforeItem = new JMenuItem(new AbstractAction(
    				TrackerRes.getString("TMenuBar.MenuItem.PasteBefore")) { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        Image image = TrackerIO.getClipboardImage();
        if (image != null) {
        	int n = trackerPanel.getFrameNumber();
  				ImageVideo imageVid = (ImageVideo)trackerPanel.getVideo();
  				imageVid.insert(image, n);
  				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
  				clip.setStepCount(imageVid.getFrameCount());
  				trackerPanel.getPlayer().setStepNumber(clip.frameToStep(n));
        	refresh();
        }
      }
    });
    pasteImageMenu.add(pasteReplaceItem);
    // delete selected point item    
    deleteSelectedPointItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.DeleteSelectedPoint")); //$NON-NLS-1$
    deleteSelectedPointItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	trackerPanel.deletePoint(trackerPanel.getSelectedPoint());
      }
    });
    // delete tracks menu
    deleteTracksMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.DeleteTrack")); //$NON-NLS-1$
    editMenu.add(deleteTracksMenu);    
    editMenu.addSeparator();
    // clear tracks item
    clearTracksItem = deleteTracksMenu.add(actions.get("clearTracks")); //$NON-NLS-1$
    // config item
    configItem = editMenu.add(actions.get("config")); //$NON-NLS-1$
    configItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, keyMask));
    // number menu
    numberMenu = new JMenu(TrackerRes.getString("Popup.Menu.Numbers")); //$NON-NLS-1$
    formatsItem = new JMenuItem(TrackerRes.getString("Popup.MenuItem.Formats")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    formatsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	TTrack track = trackerPanel.getSelectedTrack();
        NumberFormatDialog dialog = NumberFormatDialog.getNumberFormatDialog(trackerPanel, track, null);
  	    dialog.setVisible(true);
  	  }	
    });
    unitsItem = new JMenuItem(TrackerRes.getString("Popup.MenuItem.Units")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    unitsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        UnitsDialog dialog = trackerPanel.getUnitsDialog();
  	    dialog.setVisible(true);
  	  }	
    });
    // size menu
    matSizeMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.MatSize")); //$NON-NLS-1$
    final String[] sizes = new String[] {"320x240", //$NON-NLS-1$
                                         "480x360", //$NON-NLS-1$
                                         "640x480", //$NON-NLS-1$
                                         "800x600", //$NON-NLS-1$
                                         "960x720", //$NON-NLS-1$
                                         "1280x960"}; //$NON-NLS-1$
    Action matSizeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        String theSize = e.getActionCommand();
        int i = theSize.indexOf("x"); //$NON-NLS-1$
        double w = Double.parseDouble(theSize.substring(0, i));
        double h = Double.parseDouble(theSize.substring(i+1));
        trackerPanel.setImageSize(w, h);
      }
    };
    matSizeGroup = new ButtonGroup();
    fontSizeMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.FontSize")); //$NON-NLS-1$
    fontSizeGroup = new ButtonGroup();
    Action fontSizeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        int i = Integer.parseInt(e.getActionCommand());
      	FontSizer.setLevel(i);
      }

    };
    for(int i = 0; i<=Tracker.maxFontLevel; i++) {
    	String s = i==0? TrackerRes.getString("TMenuBar.MenuItem.DefaultFontSize"): "+"+i; //$NON-NLS-1$ //$NON-NLS-2$
      JMenuItem item = new JRadioButtonMenuItem(s);
      item.addActionListener(fontSizeAction);
      item.setActionCommand(String.valueOf(i)); 
      fontSizeMenu.add(item);
      fontSizeGroup.add(item);
      if(i==FontSizer.getLevel()) {
        item.setSelected(true);
      }
    }
    videoSizeItem = new JRadioButtonMenuItem();
    videoSizeItem.setActionCommand("0x0"); //$NON-NLS-1$
    videoSizeItem.addActionListener(matSizeAction);
    matSizeGroup.add(videoSizeItem);
    for (int i = 0; i < sizes.length; i++) {
      JMenuItem item = new JRadioButtonMenuItem(sizes[i]);
      item.setActionCommand(sizes[i]);
      item.addActionListener(matSizeAction);
      matSizeGroup.add(item);
    }
    // language menu
		languageMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.Language")); //$NON-NLS-1$
		// set up language menu
    Action languageAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        String language = e.getActionCommand();
        for (int i = 0; i < Tracker.incompleteLocales.length; i++) {
          if (language.equals(Tracker.incompleteLocales[i][0].toString())) {
          	Locale locale = (Locale)Tracker.incompleteLocales[i][0];
          	String lang = OSPRuntime.getDisplayLanguage(locale);
          	// the following message is purposely not translated
          	JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
          			"This translation has not been updated since "+Tracker.incompleteLocales[i][1] //$NON-NLS-1$
          			+".\nIf you speak "+lang+" and would like to help translate" //$NON-NLS-1$ //$NON-NLS-2$ 
          			+"\nplease contact Douglas Brown at dobrown@cabrillo.edu.",  //$NON-NLS-1$
          			"Incomplete Translation: "+lang,  //$NON-NLS-1$
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
    		lang+=" ("+Tracker.locales[i].getCountry()+")"; //$NON-NLS-1$ //$NON-NLS-2$
    	}
      languageItems[i] = new JRadioButtonMenuItem(lang);
      languageItems[i].setActionCommand(Tracker.locales[i].toString());
      languageItems[i].addActionListener(languageAction);
      languageGroup.add(languageItems[i]);
      if (Tracker.locales[i].toString().equals(currentLocale))
      	languageItems[i].setSelected(true);
    }
    for (int i = 0; i < languageItems.length; i++) {
      languageMenu.add(languageItems[i]);
    }
    
    // add "other" language item at end
  	// the following item and message is purposely not translated
    otherLanguageItem = new JMenuItem("Other"); //$NON-NLS-1$
    languageMenu.add(otherLanguageItem);
    otherLanguageItem.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
	    			"Do you speak a language not yet available in Tracker?" //$NON-NLS-1$
	    			+"\nTo learn more about translating Tracker into your language" //$NON-NLS-1$ 
	    			+"\nplease contact Douglas Brown at dobrown@cabrillo.edu.",  //$NON-NLS-1$
	    			"New Translation",  //$NON-NLS-1$
	    			JOptionPane.INFORMATION_MESSAGE);
    	}
    });
		
    // create new track menu
    createMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewTrack")); //$NON-NLS-1$
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
    cloneMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.Clone")); //$NON-NLS-1$
    // measuring tools menu
    measuringToolsMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.MeasuringTools")); //$NON-NLS-1$
    // video menu
    videoMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Video")); //$NON-NLS-1$
    videoMenu.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {mousePressed(e);}
      public void mousePressed(MouseEvent e) {
        // enable paste image item if clipboard contains image data
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable data = clipboard.getContents(null);
        boolean b = data!=null && data.isDataFlavorSupported(DataFlavor.imageFlavor);
        pasteImageMenu.setEnabled(b);
        pasteImageItem.setEnabled(b);
        boolean filterOnClipboard = false;
        String pasteFilterText = TrackerRes.getString("TActions.Action.Paste"); //$NON-NLS-1$
        String xml = DataTool.paste();
        if (xml!=null && xml.contains("<?xml")) { //$NON-NLS-1$
        	XMLControl control = new XMLControlElement(xml);  
        	filterOnClipboard = Filter.class.isAssignableFrom(control.getObjectClass());        	
	        if (filterOnClipboard) {
	        	String filterName = control.getObjectClass().getSimpleName();
            int i = filterName.indexOf("Filter"); //$NON-NLS-1$
            if (i>0 && i<filterName.length()-1) {
              filterName = filterName.substring(0, i);
            }
            filterName = MediaRes.getString("VideoFilter."+filterName); //$NON-NLS-1$
	        	pasteFilterText += " "+filterName; //$NON-NLS-1$
	        }
        }
        pasteFilterItem.setEnabled(filterOnClipboard);
        pasteFilterItem.setText(pasteFilterText);
        Video video = trackerPanel.getVideo();
        if (video != null) {
        	boolean vis = trackerPanel.getPlayer().getClipControl().videoVisible;
          videoVisibleItem.setSelected(video.isVisible() || vis);          
          // replace filters menu if used in popup
          boolean showFiltersMenu = trackerPanel.isEnabled("video.filters"); //$NON-NLS-1$
          boolean hasNoFiltersMenu = true;
          for (int i=0; i<videoMenu.getItemCount(); i++) {
          	JMenuItem item = videoMenu.getItem(i);
          	if (item==filtersMenu) hasNoFiltersMenu = false;
          }
          if (hasNoFiltersMenu && showFiltersMenu) {
	        	videoMenu.remove(checkDurationsItem);
	        	videoMenu.remove(aboutVideoItem);
	        	int i = videoMenu.getMenuComponentCount()-1;
	        	for (; i>=0; i--) {
	        		Component next = videoMenu.getMenuComponent(i);
	        		if (next instanceof JMenuItem)
	        			break;
	        		videoMenu.remove(next);
	        	}
	        	videoMenu.addSeparator();
	        	videoMenu.add(filtersMenu);
	        	videoMenu.addSeparator();
	        	videoMenu.remove(checkDurationsItem);
	        	videoMenu.add(aboutVideoItem);
          }
        }
        
      	videoMenu.revalidate();
      }
    });
    add(videoMenu);
    // open and close video items
    openVideoItem = videoMenu.add(actions.get("openVideo")); //$NON-NLS-1$
    closeVideoItem = videoMenu.add(actions.get("closeVideo")); //$NON-NLS-1$
    
    // goTo item
    goToItem = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.Menu.GoTo")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    goToItem.setAccelerator(KeyStroke.getKeyStroke('G', keyMask));
    goToItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	VideoPlayer player = trackerPanel.getPlayer();
      	player.showGoToDialog();
      }
    });
   
    // image video items
    importImageMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.AddImage")); //$NON-NLS-1$
    addImageAfterItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.AddAfter")); //$NON-NLS-1$
    addImageAfterItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	int n = trackerPanel.getFrameNumber();
      	int step = trackerPanel.getPlayer().getStepNumber();
      	java.io.File[] files = TrackerIO.insertImagesIntoVideo(trackerPanel, n+1);
	    	if (files != null) {
	    		String[] paths = new String[files.length];
	    		for (int i = 0; i < paths.length; i++) {
	    			paths[i] = files[i].getPath();
	    		}
	    		Undo.postImageVideoEdit(trackerPanel, paths, n+1, step, true);
	    	}
      	refresh();
      }
    });    
    addImageBeforeItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.AddBefore")); //$NON-NLS-1$
    addImageBeforeItem.addActionListener(new ActionListener() {
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
      	refresh();
      }
    });
    importImageMenu.add(addImageBeforeItem);
    importImageMenu.add(addImageAfterItem);
    removeImageItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.RemoveImage")); //$NON-NLS-1$
    removeImageItem.setAccelerator(KeyStroke.getKeyStroke('R', keyMask));
    removeImageItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	ImageVideo imageVid = (ImageVideo)trackerPanel.getVideo();
      	int n = trackerPanel.getFrameNumber();
      	String path = imageVid.remove(n);
      	int len = imageVid.getFrameCount();
				VideoClip clip = trackerPanel.getPlayer().getVideoClip();
				clip.setStepCount(len);
				int step = Math.min(n, len-1);
				step = clip.frameToStep(step);
				trackerPanel.getPlayer().setStepNumber(step);
      	if (path != null && !path.equals("")) //$NON-NLS-1$
      		Undo.postImageVideoEdit(trackerPanel, new String[] {path}, n, step, false);
				refresh();
      }
    }); 
    // play all steps item
    playAllStepsItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.PlayAllSteps"), //$NON-NLS-1$
                                             true);
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();    
    playAllStepsItem.setSelected(clip.isPlayAllSteps());
    playAllStepsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        VideoPlayer player = trackerPanel.getPlayer();
        VideoClip clip = player.getVideoClip();
        clip.setPlayAllSteps(playAllStepsItem.isSelected());
        player.setVideoClip(clip);
      }
    });
    // video visible item
    videoVisibleItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.VideoVisible")); //$NON-NLS-1$
    videoVisibleItem.setSelected(true);
    videoVisibleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Video video = trackerPanel.getVideo();
        if (video == null) return;
        if (e.getStateChange() == ItemEvent.SELECTED ||
            e.getStateChange() == ItemEvent.DESELECTED) {
          boolean visible = videoVisibleItem.isSelected();
          video.setVisible(visible);
          trackerPanel.getPlayer().getClipControl().videoVisible = visible;
          trackerPanel.setVideo(video); // triggers image change event
        }
      }
    });
    // play xuggle smoothly item
    playXuggleSmoothlyItem = new JCheckBoxMenuItem(TrackerRes.getString("XuggleVideo.MenuItem.SmoothPlay")); //$NON-NLS-1$
    playXuggleSmoothlyItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Video video = trackerPanel.getVideo();
  			String xuggleName = "org.opensourcephysics.media.xuggle.XuggleVideo"; //$NON-NLS-1$
        if (video==null || !(video.getClass().getName().equals(xuggleName))) return;
        if (e.getStateChange() == ItemEvent.SELECTED ||
            e.getStateChange() == ItemEvent.DESELECTED) {
          boolean smooth = playXuggleSmoothlyItem.isSelected();
        	try {
      			Class<?> xuggleClass = Class.forName(xuggleName);
      			Method method = xuggleClass.getMethod("setSmoothPlay", new Class[] {Boolean.class});  //$NON-NLS-1$
      			method.invoke(video, new Object[] {smooth});
      		} catch (Exception ex) {
      		}    
        }
      }
    });
    //  checkDurationsItem   
    checkDurationsItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CheckFrameDurations")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    checkDurationsItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
      	TrackerIO.findBadVideoFrames(trackerPanel, TrackerIO.defaultBadFrameTolerance, 
      			true, false, false); // show dialog always but with no "don't show again" button
	    }
	  });
    // about video item
    aboutVideoItem = videoMenu.add(actions.get("aboutVideo")); //$NON-NLS-1$
    // filters and addFilter menus
    filtersMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.VideoFilters")); //$NON-NLS-1$
    newFilterMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewVideoFilter")); //$NON-NLS-1$
    filtersMenu.add(newFilterMenu);
    filtersMenu.addSeparator();
    // paste filter item
    pasteFilterItem = new JMenuItem(TrackerRes.getString("TActions.Action.Paste")); //$NON-NLS-1$
    pasteFilterItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String xml = DataTool.paste();
      	XMLControl control = new XMLControlElement(xml);     	
        Video video = trackerPanel.getVideo();
        FilterStack stack = video.getFilterStack();
        Filter filter = (Filter)control.loadObject(null);
        stack.addFilter(filter);
        filter.setVideoPanel(trackerPanel);
      }
    });
    // clear filters item
    clearFiltersItem = filtersMenu.add(actions.get("clearFilters")); //$NON-NLS-1$
    // track menu
    trackMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Tracks")); //$NON-NLS-1$
    trackMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { 
      	refresh(); 
      }
    });
    trackMenu.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {mousePressed(e);}
      public void mousePressed(MouseEvent e) {
        // ignore when menu is about to close
        if (!trackMenu.isPopupMenuVisible()) return;
        if (createMenu.getItemCount() > 0) 
        	trackMenu.add(createMenu, 0);
        // disable newDataTrackPasteItem unless pastable data is on the clipboard
        newDataTrackPasteItem.setEnabled(false);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable data = clipboard.getContents(null);
        if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          try {
	        	String s = (String)data.getTransferData(DataFlavor.stringFlavor);
            newDataTrackPasteItem.setEnabled(ParticleDataTrack.getImportableDataName(s)!=null);
          } catch (Exception ex) {}
        }
      }
    });
    add(trackMenu);
    // axes visible item
    axesVisibleItem = new JCheckBoxMenuItem(actions.get("axesVisible")); //$NON-NLS-1$
    // coords menu
    coordsMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Coords")); //$NON-NLS-1$
    coordsMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) { 
      	refresh();
      }
    });
    add(coordsMenu);
    
    // units item
    showUnitDialogItem = new JMenuItem(TrackerRes.getString("Popup.MenuItem.Units")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    coordsMenu.add(showUnitDialogItem);
    coordsMenu.addSeparator();
    showUnitDialogItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
        UnitsDialog dialog = trackerPanel.getUnitsDialog();
  	    dialog.setVisible(true);
		  }
		});
    
    // locked coords item
    lockedCoordsItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsLocked")); //$NON-NLS-1$
    coordsMenu.add(lockedCoordsItem);
    lockedCoordsItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        ImageCoordSystem coords = trackerPanel.getCoords();
        coords.setLocked(lockedCoordsItem.isSelected());
      }
    });
    coordsMenu.addSeparator();
    // fixed origin item
    fixedOriginItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsFixedOrigin")); //$NON-NLS-1$
    fixedOriginItem.setSelected(true);
    coordsMenu.add(fixedOriginItem);
    fixedOriginItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        int n = trackerPanel.getFrameNumber();
        ImageCoordSystem coords = trackerPanel.getCoords();
        XMLControl currentState = new XMLControlElement(trackerPanel.getCoords());
        coords.setFixedOrigin(fixedOriginItem.isSelected(), n);
    		if (!refreshing)
    			Undo.postCoordsEdit(trackerPanel, currentState);
      }
    });
    // fixed angle item
    fixedAngleItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsFixedAngle")); //$NON-NLS-1$
    fixedAngleItem.setSelected(true);
    coordsMenu.add(fixedAngleItem);
    fixedAngleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        int n = trackerPanel.getFrameNumber();
        ImageCoordSystem coords = trackerPanel.getCoords();
        XMLControl currentState = new XMLControlElement(trackerPanel.getCoords());
        coords.setFixedAngle(fixedAngleItem.isSelected(), n);
    		if (!refreshing)
    			Undo.postCoordsEdit(trackerPanel, currentState);
      }
    });
    // fixed scale item
    fixedScaleItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CoordsFixedScale")); //$NON-NLS-1$
    fixedScaleItem.setSelected(true);
    coordsMenu.add(fixedScaleItem);
    fixedScaleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        int n = trackerPanel.getFrameNumber();
        ImageCoordSystem coords = trackerPanel.getCoords();
        XMLControl currentState = new XMLControlElement(trackerPanel.getCoords());
        coords.setFixedScale(fixedScaleItem.isSelected(), n);
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
    refFrameMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.CoordsRefFrame")); //$NON-NLS-1$
    coordsMenu.add(refFrameMenu);
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
    newDataTrackFromFileItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.DataFile")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    newDataTrackFromFileItem.addActionListener(actions.get("dataTrack")); //$NON-NLS-1$
    newDataTrackFromEJSItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.EJS")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    newDataTrackFromEJSItem.addActionListener(actions.get("dataTrackFromEJS")); //$NON-NLS-1$
    newDataTrackPasteItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Clipboard")); //$NON-NLS-1$
    newDataTrackPasteItem.addActionListener(actions.get("paste")); //$NON-NLS-1$
    dataTrackHelpItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.DataTrackHelp")); //$NON-NLS-1$
    dataTrackHelpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        getFrame().showHelp("datatrack", 0); //$NON-NLS-1$
      }
    });
    // window menu
    windowMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Window")); //$NON-NLS-1$
    windowMenu.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {mousePressed(e);}
      public void mousePressed(MouseEvent e) {
      	trackerPanel.getTFrame().refreshWindowMenu(trackerPanel);
      	FontSizer.setFonts(windowMenu, FontSizer.getLevel());
      }
    });
    add(windowMenu);
    // restoreItem
    restoreItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Restore")); //$NON-NLS-1$
    restoreItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
      	trackerPanel.restoreViews();
	    }
	  });
    // right Pane item
    rightPaneItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.WindowRight"), false); //$NON-NLS-1$
    rightPaneItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (getFrame() != null) {
          JSplitPane pane = frame.getSplitPane(trackerPanel, 0);
          if (rightPaneItem.isSelected()) {
            pane.setDividerLocation(frame.defaultRightDivider);
          }
          else {
            pane.setDividerLocation(1.0);
          }
        }
      }
    });
    // bottom Pane item
    bottomPaneItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.WindowBottom"), false); //$NON-NLS-1$
    bottomPaneItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (getFrame() != null) {
          JSplitPane pane = frame.getSplitPane(trackerPanel, 2);
          if (bottomPaneItem.isSelected()) {
            pane.setDividerLocation(frame.defaultBottomDivider);
          }
          else {
            pane.setDividerLocation(1.0);
          }
        }
      }
    });
    // trackControlItem
    trackControlItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.TrackControl")); //$NON-NLS-1$
    trackControlItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	    	TrackControl tc = TrackControl.getControl(trackerPanel);
	    	tc.setVisible(!tc.isVisible());
	    }
	  });
	  // notesItem
	  notesItem = new JCheckBoxMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Description")); //$NON-NLS-1$
	  notesItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
        if (getFrame() != null) {
        	if (frame.notesDialog.isVisible()) {
        		frame.notesDialog.setVisible(false);
        	}
        	else frame.getToolBar(trackerPanel).notesButton.doClick();
        }
	    }
	  });
	  // dataBuilder item
    String s = TrackerRes.getString("TMenuBar.MenuItem.DataFunctionTool"); //$NON-NLS-1$
    s += " ("+ TrackerRes.getString("TView.Menuitem.Define") +")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  dataBuilderItem = new JCheckBoxMenuItem(s);
	  dataBuilderItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	    	FunctionTool builder = trackerPanel.getDataBuilder();
	    	if (builder.isVisible()) builder.setVisible(false);
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
    s += " ("+ TrackerRes.getString("TableTrackView.Popup.MenuItem.Analyze") +")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  dataToolItem = new JCheckBoxMenuItem(s);
	  dataToolItem.addActionListener(new ActionListener() {
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
          	for  (TView view: next) {
          		if (view instanceof PlotTView) {
          			PlotTView v = (PlotTView)view;
          			TrackView trackView = v.getTrackView(v.getSelectedTrack());
          			PlotTrackView plotView = (PlotTrackView)trackView;
          			if (plotView != null) {
          				for (TrackPlottingPanel plot: plotView.getPlots()) {
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
            	for  (TView view: next) {
            		if (view instanceof TableTView) {
            			TableTView v = (TableTView)view;
            			TrackView trackView = v.getTrackView(v.getSelectedTrack());
            			TableTrackView tableView = (TableTrackView)trackView;
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
    helpMenu = getTrackerHelpMenu(trackerPanel);
    add(helpMenu);
    // empty menu items
  	emptyVideoItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Empty")); //$NON-NLS-1$
  	emptyVideoItem.setEnabled(false);
  	emptyTracksItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Empty")); //$NON-NLS-1$
  	emptyTracksItem.setEnabled(false);
  	emptyCoordsItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Empty")); //$NON-NLS-1$
  	emptyCoordsItem.setEnabled(false);
  }

  /**
   * Gets the help menu.
   *
   * @return the help menu
   */
  protected static JMenu getTrackerHelpMenu(final TrackerPanel trackerPanel) {
    // help menu
    int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    final JMenu helpMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Help")); //$NON-NLS-1$
    
    // Tracker help items
    JMenuItem startItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.GettingStarted")); //$NON-NLS-1$
    startItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Container c = helpMenu.getTopLevelAncestor();
        if (c instanceof TFrame) {
          TFrame frame = (TFrame) c;
	        frame.showHelp("gettingstarted", 0); //$NON-NLS-1$
        }
      }
    });
    helpMenu.add(startItem);    
    JMenuItem helpItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.TrackerHelp")); //$NON-NLS-1$
    helpItem.setAccelerator(KeyStroke.getKeyStroke('H', keyMask));
    helpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Container c = helpMenu.getTopLevelAncestor();
        if (c instanceof TFrame) {
          TFrame frame = (TFrame) c;
	        frame.showHelp(null, 0);
        }
      }
    });
    helpMenu.add(helpItem);
    JMenuItem translatedHelpItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.TranslatedHelp")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    translatedHelpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String lang = TrackerRes.locale.getLanguage();
      	if ("en".equals(lang)) { //$NON-NLS-1$
      		OSPDesktop.displayURL("https://"+Tracker.trackerWebsite+"/help/frameset.html"); //$NON-NLS-1$ //$NON-NLS-2$
      	}
      	else {
      		String helpURL = "https://translate.google.com/translate?hl=en&sl=en&tl="+lang //$NON-NLS-1$
      									+ "&u=http://physlets.org/tracker/help/frameset.html"; //$NON-NLS-1$
      		OSPDesktop.displayURL(helpURL);      
      	}
      }
    });
    helpMenu.add(translatedHelpItem);
    
    if (Tracker.trackerHome!=null && Tracker.readmeAction!=null) 
    	helpMenu.add(Tracker.readmeAction);
    
    // hints item
    final JMenuItem hintsItem = new JCheckBoxMenuItem(TrackerRes.getString("Tracker.MenuItem.Hints")); //$NON-NLS-1$
    hintsItem.setSelected(Tracker.showHints);
    hintsItem.addActionListener(new ActionListener() {
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
  	        for (TView[] next: views) {
  	        	for  (TView view: next) {
  	        		if (view instanceof PlotTView) {
  	        			PlotTView v = (PlotTView)view;
  	        			TrackView trackView = v.getTrackView(v.getSelectedTrack());
  	        			PlotTrackView plotView = (PlotTrackView)trackView;
  	        			if (plotView != null) {
  	        				for (TrackPlottingPanel plot: plotView.getPlots()) {
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
    
    //diagnostics menu
    boolean showDiagnostics = trackerPanel==null?
    		Tracker.getDefaultConfig().contains("help.diagnostics"): //$NON-NLS-1$
    		trackerPanel.isEnabled("help.diagnostics"); //$NON-NLS-1$
    if (showDiagnostics) {
	    helpMenu.addSeparator();
	    JMenu diagMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Diagnostics")); //$NON-NLS-1$
	    helpMenu.add(diagMenu);        
	    JMenuItem logItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.MessageLog")); //$NON-NLS-1$
	    logItem.setAccelerator(KeyStroke.getKeyStroke('L', keyMask));
	    logItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	      	Point p = new Frame().getLocation(); // default location of new frame or dialog
	        OSPLog log = OSPLog.getOSPLog();
	    		FontSizer.setFonts(log, FontSizer.getLevel());
	        if (log.getLocation().x==p.x && log.getLocation().y==p.y) {
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
	    if (Tracker.startLogAction!=null) {
	    	JMenuItem item = diagMenu.add(Tracker.startLogAction);
	    	item.setToolTipText(System.getenv("START_LOG")); //$NON-NLS-1$
	    }
	    if (Tracker.trackerPrefsAction!=null) {
	    	JMenuItem item = diagMenu.add(Tracker.trackerPrefsAction);
	    	item.setToolTipText(XML.forwardSlash(Tracker.prefsPath));
	    }
	    diagMenu.addSeparator();    
	    if (Tracker.aboutJavaAction != null) diagMenu.add(Tracker.aboutJavaAction);
	    if (Tracker.aboutXuggleAction != null) diagMenu.add(Tracker.aboutXuggleAction);
	    if (Tracker.aboutThreadsAction != null) diagMenu.add(Tracker.aboutThreadsAction);
    } // end diagnostics menu
    
    
    helpMenu.addSeparator();
    JMenuItem checkForUpgradeItem = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.CheckForUpgrade.Text")); //$NON-NLS-1$
    checkForUpgradeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Tracker.showUpgradeStatus(trackerPanel);
      }
    });
    helpMenu.add(checkForUpgradeItem);    

    if (Tracker.aboutTrackerAction != null) helpMenu.add(Tracker.aboutTrackerAction);
    return helpMenu;
  }

  /**
   * Gets the menu for the specified track.
   *
   * @param track the track
   * @return the track's menu
   */
  protected JMenu getMenu(TTrack track) {
    JMenu menu = track.getMenu(trackerPanel);
    ImageCoordSystem coords = trackerPanel.getCoords();
    if (coords.isLocked() &&
        coords instanceof ReferenceFrame &&
        track == ((ReferenceFrame)coords).getOriginTrack()) {
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
  	FontSizer.setFonts(menu, FontSizer.getLevel());
    return menu;
  }

  /**
   *  Refreshes the menubar.
   */
  protected void refresh() {
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh"); //$NON-NLS-1$
    Runnable runner = new Runnable() {
      public synchronized void run() {
        refreshing = true; // signals listeners that items are being refreshed
        CoordAxes axes = trackerPanel.getAxes();
        JMenuItem item;
        JMenu menu;
        TTrack track = trackerPanel.getSelectedTrack();
        ArrayList<TTrack> userTracks = trackerPanel.getUserTracks();
        boolean hasTracks = !userTracks.isEmpty();

        // refresh video menu
        Video video = trackerPanel.getVideo();
        boolean hasVideo = (video != null);
        videoMenu.removeAll();
        // import video item at top
        boolean importEnabled = trackerPanel.isEnabled("video.import") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("video.open"); //$NON-NLS-1$
        if (importEnabled &&  org.opensourcephysics.display.OSPRuntime.applet == null) {
        	if (hasVideo) openVideoItem.setText(TrackerRes.getString("TMenuBar.MenuItem.Replace")); //$NON-NLS-1$
        	else openVideoItem.setText(TrackerRes.getString("TActions.Action.ImportVideo")); //$NON-NLS-1$
          videoMenu.add(openVideoItem);
        }
        // close video item
        if (hasVideo) { 
        	if (trackerPanel.isEnabled("video.close")) //$NON-NLS-1$
        		videoMenu.add(closeVideoItem);
        }
        if (videoMenu.getItemCount() > 0)
          videoMenu.addSeparator();
        
        videoMenu.add(goToItem);
        videoMenu.addSeparator();
        
        if (importEnabled && hasVideo && video instanceof ImageVideo) {
        	editVideoItem.setSelected(((ImageVideo)video).isEditable());
      		videoMenu.add(editVideoItem);
          videoMenu.addSeparator();
      	}
        // pasteImage items
        if (importEnabled)
        	videoMenu.add(hasVideo? pasteImageMenu: pasteImageItem);
        if (hasVideo) {
        	boolean isEditableVideo = importEnabled && video instanceof ImageVideo
        			&& ((ImageVideo)video).isEditable();
          if (isEditableVideo && importEnabled) {
            pasteImageMenu.add(pasteImageBeforeItem);
            pasteImageMenu.add(pasteImageAfterItem);
          	videoMenu.add(importImageMenu);
          	videoMenu.add(removeImageItem);
            removeImageItem.setEnabled(video.getFrameCount() > 1);
          }
          else {
            pasteImageMenu.remove(pasteImageBeforeItem);
            pasteImageMenu.remove(pasteImageAfterItem);
          }
          // video visible and playAllSteps items
          if (trackerPanel.isEnabled("video.visible")) { //$NON-NLS-1$
            if (videoMenu.getItemCount() > 0)
              videoMenu.addSeparator();
            videoMenu.add(videoVisibleItem);
          }
          VideoClip clip = trackerPanel.getPlayer().getVideoClip();
          playAllStepsItem.setSelected(clip.isPlayAllSteps());
          videoMenu.add(playAllStepsItem);
          // smooth play item for xuggle videos
          boolean isXuggleVideo = false;
          VideoType videoType = (VideoType)video.getProperty("video_type"); //$NON-NLS-1$
          if (videoType!=null && videoType.getClass().getSimpleName().contains(VideoIO.ENGINE_XUGGLE)) {
      			String xuggleName = "org.opensourcephysics.media.xuggle.XuggleVideo"; //$NON-NLS-1$
          	try {
        			Class<?> xuggleClass = Class.forName(xuggleName);
        			Method method = xuggleClass.getMethod("isSmoothPlay", (Class[])null);  //$NON-NLS-1$
        			Boolean smooth = (Boolean)method.invoke(video, (Object[])null);
            	playXuggleSmoothlyItem.setSelected(smooth);
              videoMenu.add(playXuggleSmoothlyItem);
              isXuggleVideo = true;
        		} catch (Exception ex) {
        		}              	
          }
          // video filters menu
          if (trackerPanel.isEnabled("video.filters")) { //$NON-NLS-1$
            // clear filters menu
            filtersMenu.removeAll();
            // add newFilter menu
            filtersMenu.add(newFilterMenu);
            // add filter items to the newFilter menu
            newFilterMenu.removeAll();
            synchronized(trackerPanel.getFilters()) {
	          	for (String name: trackerPanel.getFilters().keySet()) {
	              String shortName = name;
	              int i = shortName.lastIndexOf('.');
	              if (i > 0 && i < shortName.length() - 1) {
	                shortName = shortName.substring(i + 1);
	              }
	              i = shortName.indexOf("Filter"); //$NON-NLS-1$
	              if (i > 0 && i < shortName.length() - 1) {
	                shortName = shortName.substring(0, i);
	              }
	              shortName = MediaRes.getString("VideoFilter."+shortName); //$NON-NLS-1$
	              item = new JMenuItem(shortName);
	              item.setActionCommand(name);
	              item.addActionListener(actions.get("videoFilter")); //$NON-NLS-1$
	              newFilterMenu.add(item);
	          	}
            }
            // get current filter stack
            FilterStack stack = video.getFilterStack();
            // listen to the stack for filter changes
            stack.removePropertyChangeListener("filter", TMenuBar.this); //$NON-NLS-1$
            stack.addPropertyChangeListener("filter", TMenuBar.this); //$NON-NLS-1$
            // add current filters, if any, to the filters menu
            if (!stack.getFilters().isEmpty()) {
              filtersMenu.addSeparator();
              Iterator<Filter> it2 = stack.getFilters().iterator();
              while (it2.hasNext()) {
                Filter filter = it2.next();
                menu = filter.getMenu(video);
                filtersMenu.add(menu);
              }
            }
            // add paste filter item
            filtersMenu.addSeparator();
            filtersMenu.add(pasteFilterItem);
            // add clearFiltersItem
            if (!stack.getFilters().isEmpty()) {
              filtersMenu.addSeparator();
              filtersMenu.add(clearFiltersItem);
            }
            
            if (videoMenu.getItemCount() > 0)
              videoMenu.addSeparator();
            videoMenu.add(filtersMenu);
          }
          videoMenu.addSeparator();
      		if (isXuggleVideo) videoMenu.add(checkDurationsItem);
      		videoMenu.add(aboutVideoItem);
        }
        // update save and close items
        if( org.opensourcephysics.display.OSPRuntime.applet == null) {
          saveItem.setEnabled(trackerPanel.getDataFile()!=null);
          String name = trackerPanel.getTitle();
          name = " \"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$
          closeItem.setText(TrackerRes.getString("TActions.Action.Close") + name); //$NON-NLS-1$
          saveItem.setText(TrackerRes.getString("TActions.Action.Save") + name); //$NON-NLS-1$
        }
        // clear the track and deleteTracks menus
        trackMenu.removeAll();
        deleteTracksMenu.removeAll();
        // add deleteSelectedPoint item
        deleteTracksMenu.add(deleteSelectedPointItem);
        deleteTracksMenu.addSeparator();
        // clear the ref frame menu and button group
        refFrameMenu.removeAll();
        Enumeration<AbstractButton> e = refFrameGroup.getElements();
        while(e.hasMoreElements()) {
          refFrameGroup.remove(e.nextElement());
        }
        // update coords menu items
        ImageCoordSystem coords = trackerPanel.getCoords();
        boolean defaultCoords = !(coords instanceof ReferenceFrame);
        lockedCoordsItem.setSelected(coords.isLocked());
        fixedOriginItem.setSelected(coords.isFixedOrigin());
        fixedAngleItem.setSelected(coords.isFixedAngle());
        fixedScaleItem.setSelected(coords.isFixedScale());
        fixedOriginItem.setEnabled(defaultCoords && !coords.isLocked());
        fixedAngleItem.setEnabled(defaultCoords && !coords.isLocked());
        boolean stickAttached = false;
        ArrayList<TapeMeasure> tapes = trackerPanel.getDrawables(TapeMeasure.class);
        for (TapeMeasure tape: tapes) {
        	if (tape.isStickMode() && tape.attachments!=null 
        			&& (tape.attachments[0]!=null || tape.attachments[1]!=null)) {
        		stickAttached = true;
        		break;
        	}
        }
        fixedScaleItem.setEnabled(defaultCoords && !coords.isLocked() && !stickAttached);
        refFrameMenu.setEnabled(!coords.isLocked());
        // add default reference frame item
        refFrameGroup.add(defaultRefFrameItem);
        refFrameMenu.add(defaultRefFrameItem);
        PointMass originTrack = null; // the track currently serving as origin
        if (coords instanceof ReferenceFrame)
          originTrack = ((ReferenceFrame)coords).getOriginTrack();
        if (originTrack == null) defaultRefFrameItem.setSelected(true);
        // refresh file menu
        fileMenu.removeAll();
        if( org.opensourcephysics.display.OSPRuntime.applet == null) {
          if (trackerPanel.isEnabled("file.new")) { //$NON-NLS-1$
            fileMenu.add(newTabItem);
          }
          if (trackerPanel.isEnabled("file.open")) { //$NON-NLS-1$
            if (fileMenu.getItemCount() > 0)
              fileMenu.addSeparator();
            fileMenu.add(openItem);
            fileMenu.add(openURLItem);
            TFrame frame = trackerPanel.getTFrame();
            if (frame!=null) {
            	frame.refreshOpenRecentMenu(openRecentMenu);
            	fileMenu.add(openRecentMenu);
            }
          }
          boolean showLib = trackerPanel.isEnabled("file.open") || trackerPanel.isEnabled("file.export"); //$NON-NLS-1$ //$NON-NLS-2$
					if (showLib && trackerPanel.isEnabled("file.library")) { //$NON-NLS-1$
						if (fileMenu.getItemCount() > 0)
						   fileMenu.addSeparator();
						if (trackerPanel.isEnabled("file.open")) fileMenu.add(openBrowserItem); //$NON-NLS-1$
						if (trackerPanel.isEnabled("file.export")) fileMenu.add(saveZipAsItem); //$NON-NLS-1$
					}
					if (trackerPanel.isEnabled("file.close")) { //$NON-NLS-1$
            if (fileMenu.getItemCount() > 0)
              fileMenu.addSeparator();
            fileMenu.add(closeItem);
            fileMenu.add(closeAllItem);
          }
          if (trackerPanel.isEnabled("file.save") //$NON-NLS-1$
          		|| trackerPanel.isEnabled("file.saveAs")) { //$NON-NLS-1$
            if (fileMenu.getItemCount() > 0)
              fileMenu.addSeparator();
            if (trackerPanel.isEnabled("file.save")) //$NON-NLS-1$
              fileMenu.add(saveItem);
            if (trackerPanel.isEnabled("file.saveAs")) { //$NON-NLS-1$
              fileMenu.add(saveAsItem);
              if (trackerPanel.getVideo()!=null) {
              	fileMenu.add(saveVideoAsItem);
              }
              fileMenu.add(saveTabsetAsItem);
            }
          }
          if (trackerPanel.isEnabled("file.import") //$NON-NLS-1$
          		|| trackerPanel.isEnabled("file.export")) { //$NON-NLS-1$
            if (fileMenu.getItemCount() > 0)
              fileMenu.addSeparator();
	          if (trackerPanel.isEnabled("file.import")) //$NON-NLS-1$
	            fileMenu.add(importMenu);
	          if (trackerPanel.isEnabled("file.export")) //$NON-NLS-1$
	            fileMenu.add(exportMenu);
          }
        }
        if (fileMenu.getItemCount() > 0) 
        	fileMenu.addSeparator();
        fileMenu.add(propertiesItem);
        if (trackerPanel.isEnabled("file.print")) { //$NON-NLS-1$
          if (fileMenu.getItemCount() > 0) 
          	fileMenu.addSeparator();
          fileMenu.add(printFrameItem);
        }
        // exit menu always added except in applets
        if( org.opensourcephysics.display.OSPRuntime.applet == null) {
          if (fileMenu.getItemCount() > 0) 
          	fileMenu.addSeparator();
          fileMenu.add(exitItem);
        }
        // refresh edit menu
        editMenu.removeAll();
        if (trackerPanel.isEnabled("edit.undoRedo")) { //$NON-NLS-1$
	      	undoItem.setText(TrackerRes.getString("TMenuBar.MenuItem.Undo")); //$NON-NLS-1$
	      	undoItem.setText(Undo.getUndoDescription(trackerPanel));
	      	editMenu.add(undoItem);
	      	undoItem.setEnabled(Undo.canUndo(trackerPanel));
	      	redoItem.setText(TrackerRes.getString("TMenuBar.MenuItem.Redo")); //$NON-NLS-1$
	      	redoItem.setText(Undo.getRedoDescription(trackerPanel));
	      	editMenu.add(redoItem);
	      	redoItem.setEnabled(Undo.canRedo(trackerPanel));
        }
        // refresh copyData, copyImage and copyObject menus
        if (trackerPanel.isEnabled("edit.copyData") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("edit.copyImage") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("edit.copyObject")) { //$NON-NLS-1$
	        if (editMenu.getItemCount() > 0) 
	        	editMenu.addSeparator();
	        
	        if (trackerPanel.isEnabled("edit.copyData")) { //$NON-NLS-1$
		        editMenu.add(copyDataMenu); // refreshed in edit menu mouse listener
		        TreeMap<Integer, TableTrackView> dataViews = getDataViews();
		        copyDataMenu.setEnabled(!dataViews.isEmpty());
		        if (dataViews.isEmpty()) {
		        	copyDataMenu.setText(TrackerRes.getString("TableTrackView.Action.CopyData")); //$NON-NLS-1$
		        }
		        else {
		        	Integer key = dataViews.firstKey();
		        	TableTrackView view = dataViews.get(key);
		        	view.refreshCopyDataMenu(copyDataMenu);
		        	String text = copyDataMenu.getText();
		        	copyDataMenu.setText(text+" ("+key+")"); //$NON-NLS-1$ //$NON-NLS-2$
		        }
	        }
	        if (trackerPanel.isEnabled("edit.copyImage")) { //$NON-NLS-1$
	        	editMenu.add(copyImageMenu);
	        }
        
	        // copy object menu
	        if (trackerPanel.isEnabled("edit.copyObject")) { //$NON-NLS-1$
	          editMenu.add(copyObjectMenu);
	          copyObjectMenu.setText(TrackerRes.getString("TMenuBar.Menu.CopyObject")); //$NON-NLS-1$
	          copyObjectMenu.removeAll();
	          Action copyObjectAction = new AbstractAction() {
	            public void actionPerformed(ActionEvent e) {
	            	String s = ((JMenuItem)e.getSource()).getActionCommand();
	            	if ("coords".equals(s)) { //$NON-NLS-1$
	            		TrackerIO.copyXML(trackerPanel.getCoords());
	            	}
	            	else if ("clip".equals(s)) { //$NON-NLS-1$
	            		TrackerIO.copyXML(trackerPanel.getPlayer().getVideoClip());
	            	}
	            	else { // must be a track
	            		TTrack track = trackerPanel.getTrack(s);
	                if (track != null) 
	                	TrackerIO.copyXML(track);
	            	}
	            }
	          };
	          // copy videoclip and coords items
	          item = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.Coords")); //$NON-NLS-1$
	        	item.setActionCommand("coords"); //$NON-NLS-1$
	        	item.addActionListener(copyObjectAction);
	        	copyObjectMenu.add(item);
	          item = new JMenuItem(TrackerRes.getString("TMenuBar.MenuItem.VideoClip")); //$NON-NLS-1$
	        	item.setActionCommand("clip"); //$NON-NLS-1$
	        	item.addActionListener(copyObjectAction);
	        	copyObjectMenu.add(item);
	          // copy track items
	          for (TTrack next: trackerPanel.getTracks()) {
	          	if (next==trackerPanel.getAxes() || next instanceof PerspectiveTrack)
	          		continue;
	          	item = new JMenuItem(next.getName());
	          	item.setActionCommand(next.getName());
	          	item.addActionListener(copyObjectAction);
	          	copyObjectMenu.add(item);
	          }
	        }
        }
        
        // paste and autopaste items
        if (trackerPanel.isEnabled("edit.paste")) { //$NON-NLS-1$
          if (editMenu.getItemCount() > 0) editMenu.addSeparator();
          editMenu.add(pasteItem);
          TFrame frame = trackerPanel.getTFrame();
          if (frame!=null) {
            autopasteCheckbox.setSelected(frame.alwaysListenToClipboard);
            editMenu.add(autopasteCheckbox);
          }
        }
        
        // delete and clear menus
        if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
          if (editMenu.getItemCount() > 0) editMenu.addSeparator();
          if (trackerPanel.isEnabled("track.delete") || hasTracks) { //$NON-NLS-1$
          	editMenu.add(deleteTracksMenu);
          }
        }
        // number menu
        if (trackerPanel.isEnabled("number.formats") || trackerPanel.isEnabled("number.units")) { //$NON-NLS-1$ //$NON-NLS-2$
	        if (editMenu.getItemCount() > 0) editMenu.addSeparator();
	        editMenu.add(numberMenu);
	        numberMenu.removeAll();
	        if (trackerPanel.isEnabled("number.formats")) numberMenu.add(formatsItem); //$NON-NLS-1$
	        if (trackerPanel.isEnabled("number.units")) numberMenu.add(unitsItem); //$NON-NLS-1$
        }
        // add size menu
        if (trackerPanel.isEnabled("edit.matSize")) { //$NON-NLS-1$
          if (editMenu.getItemCount() > 0) editMenu.addSeparator();
        	editMenu.add(matSizeMenu);
        }
        if (editMenu.getItemCount() > 0) editMenu.addSeparator();
      	editMenu.add(fontSizeMenu);
        refreshMatSizes(video);
        languageMenu.removeAll();
        for (int i = 0; i < Tracker.locales.length; i++) {
          languageMenu.add(languageItems[i]);
        }
        languageMenu.addSeparator();
        languageMenu.add(otherLanguageItem);
        if (editMenu.getItemCount() > 0) editMenu.addSeparator();
        editMenu.add(languageMenu);
        if (editMenu.getItemCount() > 0) editMenu.addSeparator();
        editMenu.add(configItem);
        // refresh new tracks menu
        createMenu.removeAll();
        if (trackerPanel.isEnabled("new.pointMass") || //$NON-NLS-1$
            trackerPanel.isEnabled("new.cm")) { //$NON-NLS-1$
          if (trackerPanel.isEnabled("new.pointMass")) createMenu.add(newPointMassItem); //$NON-NLS-1$
          if (trackerPanel.isEnabled("new.cm")) createMenu.add(newCMItem); //$NON-NLS-1$
        }
        if (trackerPanel.isEnabled("new.vector") || //$NON-NLS-1$
            trackerPanel.isEnabled("new.vectorSum")) { //$NON-NLS-1$
          if (createMenu.getItemCount() > 0) createMenu.addSeparator();
          if (trackerPanel.isEnabled("new.vector")) createMenu.add(newVectorItem); //$NON-NLS-1$
          if (trackerPanel.isEnabled("new.vectorSum")) createMenu.add(newVectorSumItem); //$NON-NLS-1$
        }
        if (trackerPanel.isEnabled("new.lineProfile") || //$NON-NLS-1$
            trackerPanel.isEnabled("new.RGBRegion")) { //$NON-NLS-1$
          if (createMenu.getItemCount() > 0) createMenu.addSeparator();
          if (trackerPanel.isEnabled("new.lineProfile")) //$NON-NLS-1$
          	createMenu.add(newLineProfileItem);
          if (trackerPanel.isEnabled("new.RGBRegion")) //$NON-NLS-1$
          	createMenu.add(newRGBRegionItem);
        }
        if (trackerPanel.isEnabled("new.analyticParticle") //$NON-NLS-1$
            || trackerPanel.isEnabled("new.dynamicParticle") //$NON-NLS-1$
            || trackerPanel.isEnabled("new.dynamicTwoBody") //$NON-NLS-1$
            || trackerPanel.isEnabled("new.dataTrack")) { //$NON-NLS-1$
          if (createMenu.getItemCount() > 0) createMenu.addSeparator();
          if (trackerPanel.isEnabled("new.analyticParticle"))  //$NON-NLS-1$
          	createMenu.add(newAnalyticParticleItem); 
          if (trackerPanel.isEnabled("new.dynamicParticle") //$NON-NLS-1$
          		|| trackerPanel.isEnabled("new.dynamicTwoBody")) { //$NON-NLS-1$
          	createMenu.add(newDynamicParticleMenu);
          	newDynamicParticleMenu.removeAll();
            if (trackerPanel.isEnabled("new.dynamicParticle")) { //$NON-NLS-1$
	          	newDynamicParticleMenu.add(newDynamicParticleCartesianItem);
	          	newDynamicParticleMenu.add(newDynamicParticlePolarItem);
            }
            if (trackerPanel.isEnabled("new.dynamicTwoBody")) //$NON-NLS-1$
            	newDynamicParticleMenu.add(newDynamicSystemItem);
          }
          if (trackerPanel.isEnabled("new.dataTrack")) { //$NON-NLS-1$
            createMenu.add(newDataTrackMenu);
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
          if (createMenu.getItemCount() > 0) createMenu.addSeparator();
          createMenu.add(measuringToolsMenu);
          measuringToolsMenu.removeAll();
          if (trackerPanel.isEnabled("new.tapeMeasure")) measuringToolsMenu.add(newTapeItem); //$NON-NLS-1$
          if (trackerPanel.isEnabled("new.protractor")) measuringToolsMenu.add(newProtractorItem); //$NON-NLS-1$
          if (trackerPanel.isEnabled("new.circleFitter")) measuringToolsMenu.add(newCircleFitterItem); //$NON-NLS-1$
        }
        // calibration tools menu
        if (trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
          if (createMenu.getItemCount() > 0) createMenu.addSeparator();
          TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
          TToolBar.CalibrationButton calibrationButton = toolbar.calibrationButton;
          JMenu calibrationToolsMenu = calibrationButton.getCalibrationToolsMenu();
          calibrationToolsMenu.setText(TrackerRes.getString("TMenuBar.Menu.CalibrationTools")); //$NON-NLS-1$
          createMenu.add(calibrationToolsMenu);
        }
        newTrackItems = createMenu.getMenuComponents();
        // refresh coords menu
        coordsMenu.removeAll();
        coordsMenu.add(showUnitDialogItem);
        if (trackerPanel.isEnabled("coords.locked")) { //$NON-NLS-1$
          if (coordsMenu.getItemCount() > 0) coordsMenu.addSeparator();
          coordsMenu.add(lockedCoordsItem);
        }
        if (trackerPanel.isEnabled("coords.origin") || //$NON-NLS-1$
            trackerPanel.isEnabled("coords.angle") || //$NON-NLS-1$
            trackerPanel.isEnabled("coords.scale")) { //$NON-NLS-1$
          if (coordsMenu.getItemCount() > 0) coordsMenu.addSeparator();
          if (trackerPanel.isEnabled("coords.origin")) coordsMenu.add(fixedOriginItem); //$NON-NLS-1$
          if (trackerPanel.isEnabled("coords.angle")) coordsMenu.add(fixedAngleItem); //$NON-NLS-1$
          if (trackerPanel.isEnabled("coords.scale")) coordsMenu.add(fixedScaleItem); //$NON-NLS-1$
//          coordsMenu.add(applyCurrentFrameToAllItem);
        }
        if (trackerPanel.isEnabled("coords.refFrame")) { //$NON-NLS-1$
          if (coordsMenu.getItemCount() > 0) coordsMenu.addSeparator();
          coordsMenu.add(refFrameMenu);
        }
        // refresh track menu
        if (createMenu.getItemCount() > 0) trackMenu.add(createMenu);
        cloneMenu.removeAll();
        if (hasTracks && trackerPanel.isEnabled("new.clone"))  //$NON-NLS-1$
        	trackMenu.add(cloneMenu);
        // clearTracksItem enabled only when there are tracks
        clearTracksItem.setEnabled(hasTracks);
        if (hasTracks && trackMenu.getItemCount() > 0) trackMenu.addSeparator();
        Iterator<TTrack> it = userTracks.iterator();
        // for each track
        while(it.hasNext()) {
          track = it.next();
          track.removePropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
          track.addPropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
          String trackName = track.getName("track"); //$NON-NLS-1$
          // add delete item to edit menu for each track
          item = new JMenuItem(trackName);
          item.setIcon(track.getIcon(21, 16, "track")); //$NON-NLS-1$
          item.addActionListener(actions.get("deleteTrack")); //$NON-NLS-1$
          item.setEnabled(!track.isLocked() || track.isDependent());
          deleteTracksMenu.add(item);
          // add item to clone menu for each track
          item = new JMenuItem(trackName);
          item.setIcon(track.getIcon(21, 16, "track")); //$NON-NLS-1$
          item.addActionListener(actions.get("cloneTrack")); //$NON-NLS-1$
          cloneMenu.add(item);
          // add each track's submenu to track menu
          menu = getMenu(track);
          trackMenu.add(menu);
          // if track is point mass, add reference frame menu items
          if (track instanceof PointMass) {
            item = new JRadioButtonMenuItem(trackName);
            item.addActionListener(actions.get("refFrame")); //$NON-NLS-1$
            refFrameGroup.add(item);
            refFrameMenu.add(item);
            if (track == originTrack) item.setSelected(true);
          }
        }
        if (trackerPanel.isEnabled("edit.clear")) { //$NON-NLS-1$
          if (deleteTracksMenu.getItemCount() > 0) deleteTracksMenu.addSeparator();
        	deleteTracksMenu.add(clearTracksItem);
        }
        // add axes and calibration tools to track menu
        if (trackerPanel.isEnabled("button.axes") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
          if (trackMenu.getItemCount() > 0) trackMenu.addSeparator();
          if (axes != null && trackerPanel.isEnabled("button.axes")) { //$NON-NLS-1$
            track = axes;
            track.removePropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
            track.addPropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
            // get track menu
            menu = getMenu(track);
            trackMenu.add(menu);
          }
          if (!trackerPanel.calibrationTools.isEmpty()) {
          	for (TTrack next: trackerPanel.getTracks()) {
          		if (trackerPanel.calibrationTools.contains(next)) {
          			if (next instanceof TapeMeasure) {
          				TapeMeasure tape = (TapeMeasure)next;
          				if (tape.isStickMode()
          						&& !trackerPanel.isEnabled("calibration.stick")) //$NON-NLS-1$
          					continue;
          				if (!tape.isStickMode()
          						&& !trackerPanel.isEnabled("calibration.tape")) //$NON-NLS-1$
          					continue;
          			}
          			if (next instanceof Calibration
          					&& !trackerPanel.isEnabled("calibration.points")) //$NON-NLS-1$
          				continue;
          			if (next instanceof OffsetOrigin
          					&& !trackerPanel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
          				continue;
	          		next.removePropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
	          		next.addPropertyChangeListener("locked", TMenuBar.this); //$NON-NLS-1$
		            // get track menu
		            menu = getMenu(next);
		            trackMenu.add(menu);
          		}
          	}
          }
        }
        deleteTracksMenu.setEnabled(hasTracks);
        // hack to eliminate extra separator at end of video menu
        int n = videoMenu.getMenuComponentCount();
        if (n>0 && videoMenu.getMenuComponent(n-1) instanceof JSeparator) {
        	videoMenu.remove(n-1);
        }
        // add empty menu items to menus with no items
        if (videoMenu.getItemCount()==0) {
        	videoMenu.add(emptyVideoItem);
        }
        if (trackMenu.getItemCount()==0) {
        	trackMenu.add(emptyTracksItem);
        }
        if (coordsMenu.getItemCount()==0) {
        	coordsMenu.add(emptyCoordsItem);
        }
        
        // replace help menu
        TMenuBar.this.remove(helpMenu);
        helpMenu = getTrackerHelpMenu(trackerPanel);
        TMenuBar.this.add(helpMenu);
        FontSizer.setFonts(TMenuBar.this, FontSizer.getLevel());
        refreshing = false;
      }
    };
    if (SwingUtilities.isEventDispatchThread()) runner.run();
    else SwingUtilities.invokeLater(runner); 
  }

  /**
   * Cleans up this menubar
   */
  public void dispose() {
  	menuBars.remove(trackerPanel);
    trackerPanel.removePropertyChangeListener("locked", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("clear", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("video", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("size", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("datafile", this); //$NON-NLS-1$
    Video video = trackerPanel.getVideo();
    if (video!=null) {
    	video.getFilterStack().removePropertyChangeListener("filter", TMenuBar.this); //$NON-NLS-1$
    }
    for (Integer n: TTrack.activeTracks.keySet()) {
    	TTrack track = TTrack.activeTracks.get(n);
      track.removePropertyChangeListener("locked", this); //$NON-NLS-1$
    }
    actions.clear();
    actions = null;
    TActions.actionMaps.remove(trackerPanel);
    for (int i = 0; i < copyViewImageItems.length; i++) {
      copyViewImageItems[i] = null;
    }
    trackerPanel = null;
  }

  @Override
  public void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" recycled by garbage collector"); //$NON-NLS-1$
  }

  /**
   * Responds to the following events: "selectedtrack", "selectedpoint",
   * "track", "clear", "video" from tracker panel, "filter" from filter stack,
   * "datafile" from VideoPanel.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("selectedtrack")) {  // selected track has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("datafile")) {        // datafile has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("selectedpoint")) {  // selected point has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("video")) {     // video has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("size")) {     // image size has changed //$NON-NLS-1$
      refresh();
    }
    else if (!refreshing && name.equals("filter")) {    // filter has been added or removed //$NON-NLS-1$
      // post undoable edit if individual filter was removed
    	Filter filter = (Filter)e.getOldValue();
    	if (filter != null) {
    		Undo.postFilterDelete(trackerPanel, filter);
    	}
    	refresh();
    }
    else if (name.equals("track")) {     // track has been added or removed //$NON-NLS-1$
      if (e.getOldValue() instanceof TTrack) {      // track has been removed
      	TTrack track = (TTrack)e.getOldValue();
        track.removePropertyChangeListener("locked", this); //$NON-NLS-1$
        trackerPanel.setSelectedTrack(null);
      }
      refresh();
    }
    else if (name.equals("clear")) {     // tracks have been cleared //$NON-NLS-1$
      for (Integer n: TTrack.activeTracks.keySet()) {
      	TTrack track = TTrack.activeTracks.get(n);
        track.removePropertyChangeListener("locked", this); //$NON-NLS-1$
      }
      refresh();
    }
    else if (name.equals("locked")) {      // track or coords locked/unlocked //$NON-NLS-1$
      refresh();
    }
  }
  
  protected TreeMap<Integer, TableTrackView> getDataViews() {
  	TreeMap<Integer, TableTrackView> dataViews = new TreeMap<Integer, TableTrackView>();
  	if (trackerPanel.getTFrame()==null)
  		return dataViews;
    Container[] c = trackerPanel.getTFrame().getViews(trackerPanel);
    for (int i = 0; i < c.length; i++) {
      if (trackerPanel.getTFrame().isViewOpen(i, trackerPanel)) {
      	if (c[i] instanceof TViewChooser) {
          TViewChooser chooser = (TViewChooser)c[i];
          TView tview = chooser.getSelectedView();
          if (tview instanceof TableTView) {
	          TableTView tableView = (TableTView)tview;
	          TTrack track = tableView.getSelectedTrack();
	          if (track!=null) {
	          	for (Step step: track.getSteps()) {
	          		if (step!=null) {
	          			TableTrackView trackView = (TableTrackView)tableView.getTrackView(track);
	          			dataViews.put(i+1, trackView);
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
    if (frame == null) frame = trackerPanel.getTFrame();
    return frame;
  }
  
  protected void refreshMatSizes(Video video) {
  	// determine if default size is being used
  	boolean videoSizeItemShown = false;
  	for (Component c: matSizeMenu.getMenuComponents()) {
  		videoSizeItemShown = videoSizeItemShown || c==videoSizeItem;
  	}
  	boolean isDefaultSize = !videoSizeItemShown || videoSizeItem.isSelected();
    matSizeMenu.removeAll();
    int vidWidth = 1;
    int vidHeight = 1;
    if (video!=null) {
      vidWidth = video.getImage().getWidth();
      vidHeight = video.getImage().getHeight();
      String s = TrackerRes.getString("TMenuBar.Menu.Video"); //$NON-NLS-1$
      String description = " ("+s.toLowerCase()+")"; //$NON-NLS-1$ //$NON-NLS-2$
      videoSizeItem.setText(vidWidth + "x" + vidHeight+description); //$NON-NLS-1$
      videoSizeItem.setActionCommand(vidWidth + "x" + vidHeight); //$NON-NLS-1$
    	if (isDefaultSize && trackerPanel!=null && trackerPanel.getMat()!=null) {
      	Dimension dim = trackerPanel.getMat().mat.getSize();
      	if (vidWidth!=dim.width || vidHeight!=dim.height) {
          trackerPanel.setImageSize(vidWidth, vidHeight);
      	}
    	}
    }
    else videoSizeItem.setActionCommand("0x0"); //$NON-NLS-1$
    int imageWidth = (int)trackerPanel.getImageWidth();
    int imageHeight = (int)trackerPanel.getImageHeight();
    for (Enumeration<AbstractButton> e = matSizeGroup.getElements(); e.hasMoreElements();) {
      JRadioButtonMenuItem next = (JRadioButtonMenuItem)e.nextElement();
      String s = next.getActionCommand();
      int i = s.indexOf("x"); //$NON-NLS-1$
      int w = Integer.parseInt(s.substring(0, i));
      int h = Integer.parseInt(s.substring(i + 1));
      if (w >= vidWidth & h >= vidHeight) {
        matSizeMenu.add(next);
        if (next != videoSizeItem &&
            next.getActionCommand().equals(videoSizeItem.getActionCommand())) {
          matSizeMenu.remove(next);
        }
      }
      if (w==vidWidth && h==vidHeight) {
      	videoSizeItem.setSelected(true);
      }
      else if (w==imageWidth && h==imageHeight) {
        next.setSelected(true);
      }
    }
  }
}
