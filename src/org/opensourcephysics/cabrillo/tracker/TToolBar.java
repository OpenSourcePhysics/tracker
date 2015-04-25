/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.*;
import java.text.NumberFormat;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.opensourcephysics.cabrillo.tracker.PageTView.TabView;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.media.core.ClipControl;
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
public class TToolBar extends JToolBar implements PropertyChangeListener {

  // static fields
  protected static Map<TrackerPanel, TToolBar> toolbars = new HashMap<TrackerPanel, TToolBar>();

  protected static int[] trailLengths = {1,4,15,0};
  protected static Icon newTrackIcon;
  protected static Icon trackControlIcon, trackControlOnIcon;
  protected static Icon zoomIcon;
  protected static Icon clipOffIcon, clipOnIcon;
  protected static Icon axesOffIcon, axesOnIcon;
  protected static Icon tapeOffIcon, tapeOnIcon;
  protected static Icon tapeOffRolloverIcon, tapeOnRolloverIcon;
  protected static Icon stickOffIcon, stickOnIcon;
  protected static Icon stickOffRolloverIcon, stickOnRolloverIcon;
  protected static Icon pointsOffIcon, pointsOnIcon;
  protected static Icon velocOffIcon, velocOnIcon;
  protected static Icon accelOffIcon, accelOnIcon;
  protected static Icon traceOffIcon, traceOnIcon;
  protected static Icon labelsOffIcon, labelsOnIcon;
  protected static Icon stretchOffIcon, stretchOnIcon;
  protected static Icon xmassOffIcon, xmassOnIcon;
  protected static Icon autotrackerOffIcon, autotrackerOnIcon;
  protected static Icon infoIcon, refreshIcon, htmlIcon;
  protected static Icon[] trailIcons = new Icon[4];
  protected static int[] stretchValues = new int[] {1,2,3,4,6,8,12,16,24,32};
  protected static Icon separatorIcon;
  protected static NumberFormat zoomFormat = NumberFormat.getNumberInstance();
	
	// instance fields
  protected TrackerPanel trackerPanel; // manages & displays track data
  protected boolean refreshing; // true when refreshing toolbar
  protected WindowListener infoListener;
  protected int vStretch = 1, aStretch = 1;
  protected JButton openButton, openBrowserButton, saveButton, saveZipButton;
  protected TButton newTrackButton;
  protected JButton trackControlButton, clipSettingsButton;
  protected CalibrationButton calibrationButton;
  protected JButton axesButton, zoomButton, autotrackerButton;
  protected JButton traceVisButton, pVisButton, vVisButton, aVisButton;
  protected JButton xMassButton, trailButton, labelsButton, stretchButton;
  protected int trailLength = trailLengths[trailLengths.length-2];
  protected JPopupMenu newPopup = new JPopupMenu();
  protected JPopupMenu selectPopup = new JPopupMenu();
  protected JMenu vStretchMenu, aStretchMenu;
  protected ButtonGroup vGroup, aGroup;
  protected JMenuItem showTrackControlItem, selectNoneItem, stretchOffItem;
  protected JButton notesButton, refreshButton, desktopButton;
  protected Component toolbarFiller;
  protected int toolbarComponentHeight;
  protected JMenu cloneMenu;
  protected boolean notYetCalibrated = true;
  protected ComponentListener clipSettingsDialogListener;
  protected JPopupMenu zoomPopup = new JPopupMenu();
  protected ArrayList<PageTView.TabData> pageViewTabs = new ArrayList<PageTView.TabData>();
  
  static {
  	newTrackIcon =  new ImageIcon(Tracker.class.getResource("resources/images/poof.gif")); //$NON-NLS-1$
  	trackControlIcon =  new ImageIcon(Tracker.class.getResource("resources/images/track_control.gif")); //$NON-NLS-1$
  	trackControlOnIcon =  new ImageIcon(Tracker.class.getResource("resources/images/track_control_on.gif")); //$NON-NLS-1$
    zoomIcon = new ImageIcon(Tracker.class.getResource("resources/images/zoom.gif")); //$NON-NLS-1$
    clipOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/clip_off.gif")); //$NON-NLS-1$
    clipOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/clip_on.gif")); //$NON-NLS-1$
    axesOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/axes.gif")); //$NON-NLS-1$
    axesOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/axes_on.gif")); //$NON-NLS-1$
    tapeOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/tape.gif")); //$NON-NLS-1$
    tapeOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/tape_on.gif")); //$NON-NLS-1$
    tapeOffRolloverIcon = new ImageIcon(Tracker.class.getResource("resources/images/tape_rollover.gif")); //$NON-NLS-1$
    tapeOnRolloverIcon = new ImageIcon(Tracker.class.getResource("resources/images/tape_on_rollover.gif")); //$NON-NLS-1$
    stickOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/stick.gif")); //$NON-NLS-1$
    stickOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/stick_on.gif")); //$NON-NLS-1$
    stickOffRolloverIcon = new ImageIcon(Tracker.class.getResource("resources/images/stick_rollover.gif")); //$NON-NLS-1$
    stickOnRolloverIcon = new ImageIcon(Tracker.class.getResource("resources/images/stick_on_rollover.gif")); //$NON-NLS-1$
    pointsOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/positions.gif")); //$NON-NLS-1$
    pointsOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/positions_on.gif")); //$NON-NLS-1$
    velocOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/velocities.gif")); //$NON-NLS-1$
    velocOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/velocities_on.gif")); //$NON-NLS-1$
    accelOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/accel.gif")); //$NON-NLS-1$
    accelOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/accel_on.gif")); //$NON-NLS-1$
    traceOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/trace.gif")); //$NON-NLS-1$
    traceOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/trace_on.gif")); //$NON-NLS-1$
    labelsOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/labels.gif")); //$NON-NLS-1$
    labelsOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/labels_on.gif")); //$NON-NLS-1$
    stretchOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/stretch.gif")); //$NON-NLS-1$
    stretchOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/stretch_on.gif")); //$NON-NLS-1$
    xmassOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/x_mass.gif")); //$NON-NLS-1$
    xmassOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/x_mass_on.gif")); //$NON-NLS-1$
    autotrackerOffIcon = new ImageIcon(Tracker.class.getResource("resources/images/autotrack_off.gif")); //$NON-NLS-1$
    autotrackerOnIcon = new ImageIcon(Tracker.class.getResource("resources/images/autotrack_on.gif")); //$NON-NLS-1$
    infoIcon = new ImageIcon(Tracker.class.getResource("resources/images/info.gif")); //$NON-NLS-1$
    refreshIcon = new ImageIcon(Tracker.class.getResource("resources/images/refresh.gif")); //$NON-NLS-1$
    htmlIcon = new ImageIcon(Tracker.class.getResource("resources/images/html.gif")); //$NON-NLS-1$
    trailIcons[0] = new ImageIcon(Tracker.class.getResource("resources/images/trails_off.gif")); //$NON-NLS-1$
    trailIcons[1] = new ImageIcon(Tracker.class.getResource("resources/images/trails_1.gif")); //$NON-NLS-1$
    trailIcons[2] = new ImageIcon(Tracker.class.getResource("resources/images/trails_2.gif")); //$NON-NLS-1$
    trailIcons[3] = new ImageIcon(Tracker.class.getResource("resources/images/trails_on.gif")); //$NON-NLS-1$
    separatorIcon = new ImageIcon(Tracker.class.getResource("resources/images/separator.gif")); //$NON-NLS-1$
  	zoomFormat.setMaximumFractionDigits(0);
  }

  /**
   * TToolBar constructor.
   *
   * @param  panel the tracker panel
   */
  private TToolBar(TrackerPanel panel) {
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("video", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("magnification", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
    createGUI();
    refresh(true);
    validate();
  }

  /**
   *  Creates the GUI.
   */
  protected void createGUI() {
    setFloatable(false);
    // create buttons
    final Map<String, AbstractAction> actions = TActions.getActions(trackerPanel);
    // open and save buttons
  	openButton = new TButton(actions.get("open")); //$NON-NLS-1$
  	openBrowserButton = new TButton(actions.get("openBrowser")); //$NON-NLS-1$
    saveButton = new TButton(actions.get("save")); //$NON-NLS-1$
    saveButton.addMouseListener(new MouseAdapter() {
    	public void mouseEntered(MouseEvent e) {
        String fileName = trackerPanel.getTitle();
        String extension = XML.getExtension(fileName);
        if (extension==null || !extension.equals("trk")) //$NON-NLS-1$
        	fileName = XML.stripExtension(fileName)+".trk"; //$NON-NLS-1$
        saveButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Save.Tooltip") //$NON-NLS-1$
        		+ " \"" + fileName + "\""); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    });
    saveZipButton = new TButton(actions.get("saveZip")); //$NON-NLS-1$
    // clip settings button
    clipSettingsDialogListener = new ComponentAdapter() {
    	public void componentHidden(ComponentEvent e) {      	
    		refresh(false);
    	}    	 
    };
    clipSettingsButton = new TButton(clipOffIcon,clipOnIcon);
    clipSettingsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
      	ClipControl clipControl = trackerPanel.getPlayer().getClipControl();
        TFrame frame = trackerPanel.getTFrame();
        ClipInspector inspector = clip.getClipInspector(clipControl, frame);
        if(inspector.isVisible()) {
          inspector.setVisible(false);
          return;
        }
        FontSizer.setFonts(inspector, FontSizer.getLevel());
        inspector.pack();
        Point p0 = new Frame().getLocation();
        Point loc = inspector.getLocation();
        if((loc.x==p0.x)&&(loc.y==p0.y)) {
          // center inspector on the main view
        	Rectangle rect = trackerPanel.getVisibleRect();
          Point p = frame.getMainView(trackerPanel).scrollPane.getLocationOnScreen();
          int x = p.x+(rect.width-inspector.getBounds().width)/2;
          int y = p.y+(rect.height-inspector.getBounds().height)/2;
          inspector.setLocation(x, y);
        }
        inspector.initialize();
        inspector.removeComponentListener(clipSettingsDialogListener);
        inspector.addComponentListener(clipSettingsDialogListener);
        inspector.setVisible(true);
        refresh(false);
      }

    });
    // axes button
    axesButton = new TButton(axesOffIcon,axesOnIcon);
    axesButton.addActionListener(actions.get("axesVisible")); //$NON-NLS-1$
    
    // calibration button
    calibrationButton = new CalibrationButton();

    // zoom button
    Action zoomAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	// set zoom center to center of current viewport
      	Rectangle rect = trackerPanel.scrollPane.getViewport().getViewRect();
      	MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
      	mainView.setZoomCenter(rect.x+rect.width/2, rect.y+rect.height/2);
        String name = e.getActionCommand();
        if (name.equals("auto")) { //$NON-NLS-1$
          trackerPanel.setMagnification(-1);
        }
        else {
          double mag = Double.parseDouble(name);
          trackerPanel.setMagnification(mag/100);
        }
        refreshZoomButton();
      }
    };         
    JMenuItem item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ToFit")); //$NON-NLS-1$
    item.setActionCommand("auto"); //$NON-NLS-1$
    item.addActionListener(zoomAction);
    zoomPopup.add(item);
    zoomPopup.addSeparator();
    for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
    	int n = (int)(100*TrackerPanel.ZOOM_LEVELS[i]);
      String m = String.valueOf(n);
      item = new JMenuItem(m+"%"); //$NON-NLS-1$
      item.setActionCommand(m);
      item.addActionListener(zoomAction);
      zoomPopup.add(item);
    }
    zoomButton = new TButton(zoomIcon) {
    	protected JPopupMenu getPopup() {
      	FontSizer.setFonts(zoomPopup, FontSizer.getLevel());
        return zoomPopup;       		
    	}
    };
    zoomButton.addMouseListener(new MouseAdapter() {
    	public void mouseClicked(MouseEvent e) {
    		if (e.getClickCount()==2) {
    			trackerPanel.setMagnification(-1);
    			zoomPopup.setVisible(false);
    			refreshZoomButton();
    		}
    	}
    });

    // new track button
    newTrackButton = new TButton(newTrackIcon) {
      protected JPopupMenu getPopup() {
      	return getNewTracksPopup();
      }   	
    };
    // track control button
    trackControlButton = new TButton(trackControlIcon, trackControlOnIcon);
    trackControlButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TrackControl tc = TrackControl.getControl(trackerPanel);
      	tc.setVisible(!tc.isVisible());
      }
    });    
    // autotracker button
    autotrackerButton = new TButton(autotrackerOffIcon, autotrackerOnIcon);
    autotrackerButton.addMouseListener(new MouseAdapter() {
    	public void mouseEntered(MouseEvent e) {    		
    		requestFocus(); // workaround--shouldn't need this...
    	}
    });
    autotrackerButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	autotrackerButton.setSelected(!autotrackerButton.isSelected());
        AutoTracker autoTracker = trackerPanel.getAutoTracker();
        if (autoTracker.getTrack()==null) {
	        TTrack track = trackerPanel.getSelectedTrack();
	        if (track==null) {
	          for (TTrack next: trackerPanel.getTracks()) {
	          	if (!next.isAutoTrackable()) continue;
	          	track = next;
	          	break;
	          }
	        }
        	autoTracker.setTrack(track);
        }
        autoTracker.getWizard().setVisible(autotrackerButton.isSelected());
        trackerPanel.repaint();
      }
    });    
    final Action refreshAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        JButton button = (JButton)e.getSource();
        button.setSelected(!button.isSelected());
        refresh(true);
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
      protected JPopupMenu getPopup() {
      	JPopupMenu popup = new JPopupMenu();
      	ActionListener listener = new ActionListener() {
      		public void actionPerformed(ActionEvent e) {
      			int n = Integer.parseInt(e.getActionCommand());
      			trailLength = trailLengths[n];
          	trailButton.setSelected(trailLength!=1);
            refresh(true);
          	trackerPanel.repaint();
      		}
      	};
      	ButtonGroup group = new ButtonGroup();
      	JMenuItem item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.NoTrail")); //$NON-NLS-1$
      	item.setSelected(trailLength==trailLengths[0]);
      	item.setActionCommand(String.valueOf(0));
      	item.addActionListener(listener);
      	popup.add(item);
      	group.add(item);
      	item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.ShortTrail")); //$NON-NLS-1$
      	item.setSelected(trailLength==trailLengths[1]);
      	item.setActionCommand(String.valueOf(1));
      	item.addActionListener(listener);
      	popup.add(item);
      	group.add(item);
      	item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.LongTrail")); //$NON-NLS-1$
      	item.setSelected(trailLength==trailLengths[2]);
      	item.setActionCommand(String.valueOf(2));
      	item.addActionListener(listener);
      	popup.add(item);
      	group.add(item);
      	item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.FullTrail")); //$NON-NLS-1$
      	item.setSelected(trailLength==trailLengths[3]);
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
    labelsButton.setSelected(true);
    labelsButton.addActionListener(refreshAction);
    // x mass button
    xMassButton = new TButton(xmassOffIcon, xmassOnIcon);
    xMassButton.addActionListener(new ActionListener() {
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
  	aStretchMenu = new JMenu();
  	// velocity stretch menu
  	ActionListener vListener = new ActionListener() {
  		public void actionPerformed(ActionEvent e) {
  			int n = Integer.parseInt(e.getActionCommand());
      	trackerPanel.setSelectedPoint(null);
        vStretch = n;
        refresh(true);
  		}
  	};
  	vGroup = new ButtonGroup();
  	for (int i=0; i<stretchValues.length; i++) {
  		String s = String.valueOf(stretchValues[i]);
    	item = new JRadioButtonMenuItem("x"+s); //$NON-NLS-1$
    	if (i==0)
    		item.setText(TrackerRes.getString("TrackControl.StretchVectors.None")); //$NON-NLS-1$
    	item.setActionCommand(s);
    	item.setSelected(vStretch==stretchValues[i]);
    	item.addActionListener(vListener);
    	vStretchMenu.add(item);
    	vGroup.add(item);
  	}
  	// acceleration stretch menu
  	ActionListener aListener = new ActionListener() {
  		public void actionPerformed(ActionEvent e) {
  			int n = Integer.parseInt(e.getActionCommand());
      	trackerPanel.setSelectedPoint(null);
        aStretch = n;
        refresh(true);
  		}
  	};
  	aGroup = new ButtonGroup();
  	for (int i=0; i<stretchValues.length; i++) {
  		String s = String.valueOf(stretchValues[i]);
    	item = new JRadioButtonMenuItem("x"+s); //$NON-NLS-1$
    	if (i==0)
    		item.setText(TrackerRes.getString("TrackControl.StretchVectors.None")); //$NON-NLS-1$
    	item.setActionCommand(s);
    	item.setSelected(aStretch==stretchValues[i]);
    	item.addActionListener(aListener);
    	aStretchMenu.add(item);
    	aGroup.add(item);
  	}
  	stretchOffItem = new JMenuItem();
  	stretchOffItem.addActionListener(new ActionListener() {
  		public void actionPerformed(ActionEvent e) {
        vStretch = 1;
        aStretch = 1;
        refresh(true);
  		}
  	});


    stretchButton = new TButton(stretchOffIcon, stretchOnIcon) {
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
    
    // horizontal glue for right end of toolbar
    toolbarFiller = Box.createHorizontalGlue();
    // info button
    infoListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        notesButton.setSelected(false);
      }
    };
    notesButton = new TButton(infoIcon);
    notesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TFrame frame = trackerPanel.getTFrame();
        if (frame != null && frame.getTrackerPanel(frame.getSelectedTab()) == trackerPanel) {
          frame.notesDialog.removeWindowListener(infoListener);
          frame.notesDialog.addWindowListener(infoListener);
          // position info dialog if first time shown
        	// or if trackerPanel specifies location
          Point p0 = new Frame().getLocation();
          if (trackerPanel.infoX != Integer.MIN_VALUE ||
          				frame.notesDialog.getLocation().x==p0.x) {
          	int x, y;
            Point p = frame.getLocationOnScreen();
          	if (trackerPanel.infoX != Integer.MIN_VALUE) {
              Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        			x = Math.max(p.x + trackerPanel.infoX, 0);
        			x = Math.min(x, dim.width-frame.notesDialog.getWidth());
        			y = Math.max(p.y + trackerPanel.infoY, 0);
        			y = Math.min(y, dim.height-frame.notesDialog.getHeight());
          		trackerPanel.infoX = Integer.MIN_VALUE;
          	}
          	else {
              Point pleft = TToolBar.this.getLocationOnScreen();
              Dimension dim = frame.notesDialog.getSize();
              Dimension wdim = TToolBar.this.getSize();
              x = pleft.x + (int)(0.5 * (wdim.width - dim.width));
              y = p.y + 16;
          	}
            frame.notesDialog.setLocation(x, y);
          }
          notesButton.setSelected(!frame.notesDialog.isVisible());
          frame.notesDialog.setVisible(notesButton.isSelected());
          trackerPanel.refreshNotesDialog();
        }
      }
    });
    refreshButton = new TButton(refreshIcon) {
      protected JPopupMenu getPopup() {
      	JPopupMenu popup = new JPopupMenu();
      	JMenuItem item = new JMenuItem(TrackerRes.getString("TToolbar.Button.Refresh.Popup.RefreshNow")); //$NON-NLS-1$
      	item.addActionListener(new ActionListener() {
      		public void actionPerformed(ActionEvent e) {
	          trackerPanel.refreshTrackData();
	          trackerPanel.eraseAll();
	          trackerPanel.repaintDirtyRegion();
      		}
      	});
      	popup.add(item);
      	popup.addSeparator();
      	item = new JCheckBoxMenuItem(TrackerRes.getString("TToolbar.Button.Refresh.Popup.AutoRefresh")); //$NON-NLS-1$
      	item.setSelected(trackerPanel.isAutoRefresh);
      	item.addActionListener(new ActionListener() {
      		public void actionPerformed(ActionEvent e) {
      			JMenuItem item = (JMenuItem)e.getSource();
	          trackerPanel.isAutoRefresh = item.isSelected();
	          if (trackerPanel.isAutoRefresh) {
		          trackerPanel.refreshTrackData();
		          trackerPanel.eraseAll();
		          trackerPanel.repaintDirtyRegion();
	          }
      		}
      	});
      	popup.add(item);
      	FontSizer.setFonts(popup, FontSizer.getLevel());
      	return popup;
      }

    };
    desktopButton = new TButton(htmlIcon) {
      protected JPopupMenu getPopup() {
      	JPopupMenu popup = new JPopupMenu();
      	if (!trackerPanel.supplementalFilePaths.isEmpty()) {
	      	JMenu fileMenu = new JMenu(TrackerRes.getString("TToolbar.Button.Desktop.Menu.OpenFile")); //$NON-NLS-1$
	      	popup.add(fileMenu);
	        for (String next: trackerPanel.supplementalFilePaths) {
	        	String title = XML.getName(next);
	        	String path = next;
	        	JMenuItem item = new JMenuItem(title);
	        	item.setActionCommand(path);
	        	item.setToolTipText(path);
	        	item.addActionListener(new ActionListener() {
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
	        for (PageTView.TabData next: pageViewTabs) {
	        	if (next.url==null) continue;
	        	String title = next.title;
	        	String path = trackerPanel.pageViewFilePaths.get(next.text);
	        	if (path==null) {
	        		path = next.url.toExternalForm();
	        	}
	        	JMenuItem item = new JMenuItem(title);
	        	item.setActionCommand(path);
	        	item.setToolTipText(path);
	        	item.addActionListener(new ActionListener() {
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

    };

//    desktopButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//      	// popup menu with file names or page titles
//        for (String path: trackerPanel.desktopFiles) {
//        	OSPDesktop.displayURL(path);
//        }
//      }
//    });
    // create menu items
    cloneMenu = new JMenu();
    showTrackControlItem = new JCheckBoxMenuItem();
    showTrackControlItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TrackControl tc = TrackControl.getControl(trackerPanel);
      	tc.setVisible(showTrackControlItem.isSelected());
      }
    });    
    selectNoneItem = new JMenuItem();
    selectNoneItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	trackerPanel.setSelectedTrack(null);
      }
    });
  }
  
  protected void refreshZoomButton() {
  	double zoom = trackerPanel.getMagnification()*100;
    zoomButton.setText(zoomFormat.format(zoom)+"%"); 	 //$NON-NLS-1$
  }

  /**
   * Refreshes the GUI.
   * @param refreshTrackProperties true to refresh the track display properties
   */
  protected void refresh(final boolean refreshTrackProperties) {
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh"); //$NON-NLS-1$
    Runnable runner = new Runnable() {
    	public void run() {
        refreshing = true; // signals listeners that items are being refreshed
        refreshZoomButton();
        calibrationButton.refresh();
        stretchButton.setSelected(vStretch>1 || aStretch>1);
        stretchOffItem.setText(TrackerRes.getString("TToolBar.MenuItem.StretchOff")); //$NON-NLS-1$
        stretchOffItem.setEnabled(vStretch>1 || aStretch>1);
        // refresh stretch items
        Enumeration<AbstractButton> en = vGroup.getElements();
        for (;en.hasMoreElements();) {
        	AbstractButton next = en.nextElement();
        	if (next.getActionCommand().equals(String.valueOf(vStretch))) {
        		next.setSelected(true);
        	}
        }
        en = aGroup.getElements();
        for (;en.hasMoreElements();) {
        	AbstractButton next = en.nextElement();
        	if (next.getActionCommand().equals(String.valueOf(aStretch))) {
        		next.setSelected(true);
        	}
        }

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
      	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
        ClipInspector inspector = clip.getClipInspector();
        clipSettingsButton.setSelected(inspector!=null && inspector.isVisible());
        CoordAxes axes = trackerPanel.getAxes();
        if (axes != null) {
        	axesButton.setSelected(axes.isVisible());
        	axes.removePropertyChangeListener("visible", TToolBar.this); //$NON-NLS-1$
          axes.addPropertyChangeListener("visible", TToolBar.this); //$NON-NLS-1$
        }
        ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
        trackControlButton.setEnabled(!tracks.isEmpty());
        autotrackerButton.setEnabled(trackerPanel.getVideo()!=null);
        // count independent masses
        double totalMass = 0;
        int massCount = 0;
        for (TTrack track: tracks) {
	        if (track instanceof PointMass 
	        		&& !(track instanceof CenterOfMass)
	        		&& !(track instanceof DynamicSystem)) {
	          PointMass p = (PointMass)track;
	          totalMass += p.getMass();
	          massCount++;
	        }
        }
        // refresh all tracks
        if (refreshTrackProperties) 
        	for (TTrack track: trackerPanel.getTracks()) {
          track.removePropertyChangeListener("locked", TToolBar.this); //$NON-NLS-1$
          track.addPropertyChangeListener("locked", TToolBar.this); //$NON-NLS-1$
          // refresh track display properties from current button states
          track.setTrailLength(trailLength);
          track.setTrailVisible(trailButton.isSelected());
          if (track instanceof PointMass) {
            PointMass p = (PointMass)track;
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
                }
                else {
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
                }
                else {
                  arrow.setStretch(aStretch);
                  arrow.setSolidHead(true);
                }
              }
            }
            p.repaint();
          }
          else if (track instanceof Vector) {
          	Vector v = (Vector)track;
            v.setLabelsVisible(labelsButton.isSelected());
            Footprint[] footprints = v.getFootprints();
            for (int i = 0; i < footprints.length; i++) {
              if (footprints[i] instanceof ArrowFootprint) {
                ArrowFootprint arrow = (ArrowFootprint) footprints[i];
                arrow.setStretch(vStretch);
              }
            }
            v.repaint();
          }
        }
        TPoint pt = trackerPanel.getSelectedPoint();
        if (pt != null) pt.showCoordinates(trackerPanel);

        // set trails icon
        for (int i = 0; i < trailLengths.length; i++) {
          if (trailLength == trailLengths[i]) {
          	trailButton.setIcon(trailIcons[i]);
          	trailButton.setSelectedIcon(trailIcons[i]);
          }
        }
        
        // refresh pageViewTabs list
        pageViewTabs.clear();
        TFrame frame = trackerPanel.getTFrame();
        if (frame!=null) {
        TView[][] views = frame.getTViews(trackerPanel);
	        for (TView[] next: views) {
	        	if (next==null) continue;
	        	for (TView view: next) {
	        		if (view==null) continue;
	        		if (view instanceof PageTView) {
	        			PageTView page = (PageTView)view;
	        			for (TabView tab: page.tabs) {
	        				if (tab.data.url!=null) {
	        					pageViewTabs.add(tab.data);
	        				}
	        			}
	        		}
	        	}
	        }
	        sortPageViewTabs();
        }

        // assemble buttons
        removeAll();
        if ( org.opensourcephysics.display.OSPRuntime.applet == null) {
	        if (trackerPanel.isEnabled("file.open")) { //$NON-NLS-1$
	        	add(openButton);
	        }
	        if (trackerPanel.isEnabled("file.save")) { //$NON-NLS-1$
	        	add(saveButton);
	        }
          boolean showLib = trackerPanel.isEnabled("file.library") //$NON-NLS-1$
          		&& (trackerPanel.isEnabled("file.open") || trackerPanel.isEnabled("file.export")); //$NON-NLS-1$ //$NON-NLS-2$
	        if (showLib && getComponentCount()>0)
	        	add(getSeparator());
	        if (trackerPanel.isEnabled("file.open") && trackerPanel.isEnabled("file.library")) { //$NON-NLS-1$ //$NON-NLS-2$
	        	add(openBrowserButton);
	        }
	        if (trackerPanel.isEnabled("file.export") && trackerPanel.isEnabled("file.library")) { //$NON-NLS-1$ //$NON-NLS-2$
	        	add(saveZipButton);
	        }
        }
        if (getComponentCount()>0)
        	add(getSeparator()); // first separator
        boolean addSecondSeparator = false;
        if (trackerPanel.isEnabled("button.clipSettings")) {//$NON-NLS-1$
        	add(clipSettingsButton);
        	addSecondSeparator = true;
        }
        if (trackerPanel.isEnabled("calibration.stick") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.tape") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.points") //$NON-NLS-1$
        		|| trackerPanel.isEnabled("calibration.offsetOrigin")) { //$NON-NLS-1$
        	add(calibrationButton);
        	addSecondSeparator = true;
        }
        if (trackerPanel.isEnabled("button.axes")) {//$NON-NLS-1$
        	add(axesButton);
        	addSecondSeparator = true;
        }
        if (addSecondSeparator)
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
        		|| trackerPanel.isEnabled("button.a") ) {//$NON-NLS-1$
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
        }
        add(toolbarFiller);
        add(desktopButton);
        add(notesButton);
        boolean hasPageURLs = !pageViewTabs.isEmpty();
        desktopButton.setEnabled(hasPageURLs || !trackerPanel.supplementalFilePaths.isEmpty());
        add(refreshButton);
        
        FontSizer.setFonts(newTrackButton, FontSizer.getLevel());
        FontSizer.setFonts(zoomButton, FontSizer.getLevel());
        
        validate();
        repaint();
        refreshing = false;
    	}
    };
    if (SwingUtilities.isEventDispatchThread()) runner.run();
    else SwingUtilities.invokeLater(runner); 
  }

  /**
   * Responds to the following events: "selectedtrack", "selectedpoint",
   * "track" from tracker panel, "locked" from tracks, "visible" from tape
   * and axes.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("video")) {  // video has changed //$NON-NLS-1$
      refresh(false);
    }
    else if (name.equals("selectedtrack")) {  // selected track has changed //$NON-NLS-1$
      // refresh info dialog if visible
      trackerPanel.refreshNotesDialog();
      refresh(false);
    }
    else if (name.equals("magnification")) {  // magnification has changed //$NON-NLS-1$
    	refreshZoomButton();
    }
    else if (name.equals("visible")) { // axes or calibration tool visibility //$NON-NLS-1$
      if (e.getSource()==trackerPanel.getAxes()) {
	    	CoordAxes axes = trackerPanel.getAxes();
	      axesButton.setSelected(axes.isVisible());
      }
      else {
      	calibrationButton.refresh();
      }
    }
    else if (name.equals("locked")) {    // track has been locked or unlocked //$NON-NLS-1$
      refresh(false);
    }
    else if (name.equals("selectedpoint")) {  // selected point has changed //$NON-NLS-1$
      refresh(false);
    }
    else if (name.equals("track")) {     // track has been added or removed //$NON-NLS-1$
      if (e.getNewValue()==null && e.getOldValue()!=null) {      // track has been removed
        TTrack track = (TTrack)e.getOldValue();
  			trackerPanel.calibrationTools.remove(track);       
  	    trackerPanel.visibleTools.remove(track);
      	track.removePropertyChangeListener("visible", TToolBar.this); //$NON-NLS-1$
      	if (trackerPanel.visibleTools.isEmpty()) {
      		calibrationButton.setSelected(false);
      	}
      }
      refresh(true);
    }
  }
  
  /**
   * Refreshes and returns the "new tracks" popup menu.
   *
   * @return the popup
   */
  protected JPopupMenu getNewTracksPopup() {
    newPopup.removeAll();
    TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
    menubar.refresh();
  	for (Component c: menubar.newTrackItems) {
      newPopup.add(c);    
  	}
    if (menubar.cloneMenu.getItemCount()>0 && trackerPanel.isEnabled("new.clone")) { //$NON-NLS-1$
    	newPopup.addSeparator();
      newPopup.add(menubar.cloneMenu);    
    }
    return newPopup;
  }
  
  private JButton getSeparator() {
  	JButton b = new JButton(separatorIcon);
  	b.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
  	b.setOpaque(false);
  	b.setContentAreaFilled(false);
  	return b;
  }
  
  private void sortPageViewTabs() {
  	Collections.sort(pageViewTabs, new Comparator<PageTView.TabData> () {
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
     * @param obj the TrackerPanel object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      TToolBar toolbar  = (TToolBar)obj;
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
    public Object createObject(XMLControl control){
      return null;
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
    	TToolBar toolbar  = (TToolBar)obj;
      toolbar.traceVisButton.setSelected(control.getBoolean("trace")); //$NON-NLS-1$
      toolbar.pVisButton.setSelected(control.getBoolean("position")); //$NON-NLS-1$
      toolbar.vVisButton.setSelected(control.getBoolean("velocity")); //$NON-NLS-1$
      toolbar.aVisButton.setSelected(control.getBoolean("acceleration")); //$NON-NLS-1$
      toolbar.labelsButton.setSelected(control.getBoolean("labels")); //$NON-NLS-1$
      toolbar.xMassButton.setSelected(control.getBoolean("multiply_by_mass")); //$NON-NLS-1$
      toolbar.trailLength = control.getInt("trail_length"); //$NON-NLS-1$   
      toolbar.vStretch = control.getInt("stretch"); //$NON-NLS-1$
      if (control.getPropertyNames().contains("stretch_acceleration")) { //$NON-NLS-1$ 
      	toolbar.aStretch = control.getInt("stretch_acceleration"); //$NON-NLS-1$  
      }
      else toolbar.aStretch = toolbar.vStretch;
      return obj;
    }
  }
  
  /**
   * A class to manage the creation and visibility of calibration tools.
   */
  protected class CalibrationButton extends TButton 
  		implements ActionListener {
  	
  	boolean showPopup;
    JPopupMenu popup = new JPopupMenu();
    
    /**
     * Constructor.
     */
    private CalibrationButton() {
    	setIcons(tapeOffIcon, tapeOnIcon);
      setRolloverIcon(tapeOffRolloverIcon);
      setRolloverSelectedIcon(tapeOnRolloverIcon);
      // mouse listener to distinguish between popup and tool visibility actions
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
        	int w = stickOffRolloverIcon.getIconWidth();
        	int dw = calibrationButton.getWidth()-w;
        	// show popup if right side of button clicked or if no tools selected
        	showPopup = e.getX()>(18 + dw/2) || trackerPanel.visibleTools.isEmpty();
        }
      });
      addActionListener(this);
    }

  	
    /**
     * Overrides TButton method.
     *
     * @return the popup, or null if the right side of this button was clicked
     */
    protected JPopupMenu getPopup() {
    	if (!showPopup)	return null;
      // rebuild popup menu
    	popup.removeAll();    	
      JMenuItem item;
      for (TTrack track: trackerPanel.calibrationTools) {
        item = new JCheckBoxMenuItem(track.getName());
        item.setSelected(trackerPanel.visibleTools.contains(track));
        item.setActionCommand(track.getName());
        item.addActionListener(this);
      	popup.add(item);
      }
      // new tools menu
      JMenu newToolsMenu = getCalibrationToolsMenu();
      if (newToolsMenu.getItemCount()>0) {
      	if (!trackerPanel.calibrationTools.isEmpty())
      		popup.addSeparator();
	      popup.add(newToolsMenu);
      }
      FontSizer.setFonts(popup, FontSizer.getLevel());	          
    	return popup;
    }
    
    protected JMenu getCalibrationToolsMenu() {
      // new tools menu
      JMenu newToolsMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewTrack")); //$NON-NLS-1$
      JMenuItem item;
      if (trackerPanel.isEnabled("calibration.stick")) { //$NON-NLS-1$
        item = new JMenuItem(TrackerRes.getString("Stick.Name")); //$NON-NLS-1$
	      item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	    			TapeMeasure track = new TapeMeasure();
	          track.setColor(Color.BLUE);
	        	track.setStickMode(true);
	          // assign a default name
	        	String name = TrackerRes.getString("CalibrationStick.New.Name"); //$NON-NLS-1$
	          int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
	          String letter = TrackerPanel.alphabet.substring(i, i+1);
	          track.setName(name+" "+letter); //$NON-NLS-1$
	          
	    			Rectangle rect = trackerPanel.getMat().mat;
	    			double x = rect.x+rect.width/2; // center of mat
	    			double y = rect.y+rect.height/2; // center of mat
	  				trackerPanel.addTrack(track);
	          calibrationButton.setSelected(true);
		      	// show all tools in visibleTools list
		      	for (TTrack next: trackerPanel.visibleTools) {
			      	showCalibrationTool(next);
		      	}
	  				track.createStep(0, x-50, y-10, x+50, y-10);
	    			trackerPanel.setSelectedTrack(track);
	        }
	      });
	      newToolsMenu.add(item);
      }
      
      if (trackerPanel.isEnabled("calibration.tape")) { //$NON-NLS-1$
	      item = new JMenuItem(TrackerRes.getString("CalibrationTapeMeasure.Name")); //$NON-NLS-1$
	      item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	    			TapeMeasure track = new TapeMeasure();
	          track.setColor(Color.BLUE);
	    			track.setReadOnly(false);
	          // assign a default name
	        	String name = TrackerRes.getString("CalibrationTapeMeasure.New.Name"); //$NON-NLS-1$
	          int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
	          String letter = TrackerPanel.alphabet.substring(i, i+1);
	          track.setName(name+" "+letter); //$NON-NLS-1$
	
	          Rectangle rect = trackerPanel.getMat().mat;
	    			double x = rect.x+rect.width/2; // center of mat
	    			double y = rect.y+rect.height/2; // center of mat
	  				trackerPanel.addTrack(track);
	          calibrationButton.setSelected(true);
		      	// show all tools in visibleTools list
		      	for (TTrack next: trackerPanel.visibleTools) {
			      	showCalibrationTool(next);
		      	}
	  				track.createStep(0, x-50, y-30, x+50, y-30);
	    			trackerPanel.setSelectedTrack(track);
	        }
	      });
	      newToolsMenu.add(item);
      }

      if (trackerPanel.isEnabled("calibration.points")) { //$NON-NLS-1$
	      item = new JMenuItem(TrackerRes.getString("Calibration.Name")); //$NON-NLS-1$
	      item.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	    			Calibration track = new Calibration();
	          // assign a default name
	        	String name = TrackerRes.getString("Calibration.New.Name"); //$NON-NLS-1$
	          int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
	          String letter = TrackerPanel.alphabet.substring(i, i+1);
	          track.setName(name+" "+letter); //$NON-NLS-1$
	
	  				trackerPanel.addTrack(track);
	          calibrationButton.setSelected(true);
		      	// show all tools in visibleTools list
		      	for (TTrack next: trackerPanel.visibleTools) {
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
	        public void actionPerformed(ActionEvent e) {
	        	OffsetOrigin track = new OffsetOrigin();
	          // assign a default name
	        	String name = TrackerRes.getString("OffsetOrigin.New.Name"); //$NON-NLS-1$
	          int i = trackerPanel.getAlphabetIndex(name, " "); //$NON-NLS-1$
	          String letter = TrackerPanel.alphabet.substring(i, i+1);
	          track.setName(name+" "+letter); //$NON-NLS-1$
	
	  				trackerPanel.addTrack(track);
	          calibrationButton.setSelected(true);
		      	// show all tools in visibleTools list
		      	for (TTrack next: trackerPanel.visibleTools) {
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
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource()==calibrationButton) { // button action: show/hide tools
    		if (showPopup) return;
	      trackerPanel.setSelectedPoint(null);
	      trackerPanel.hideMouseBox();        
	      if (!calibrationButton.isSelected()) {
	      	calibrationButton.setSelected(true);
	      	// show tools in visibleTools list
	      	for (TTrack track: trackerPanel.visibleTools) {
		      	showCalibrationTool(track);
	      	}
	      }
	      else { 
	      	calibrationButton.setSelected(false);
	      	// hide all tools
	      	for (TTrack track: trackerPanel.calibrationTools) {
		      	hideCalibrationTool(track);
	      	}
	      }
	      trackerPanel.repaint();
    	}
    	else { // menuItem action
    		// see which item changed and show/hide corresponding tool
      	trackerPanel.setSelectedPoint(null);
        JMenuItem source = (JMenuItem)e.getSource();
        for (TTrack track: trackerPanel.calibrationTools) {
          if (e.getActionCommand().equals(track.getName())) {
        		if (source.isSelected()) {
        			trackerPanel.visibleTools.add(track);
    	      	calibrationButton.setSelected(true);
    	      	// show only tools in visibleTools
    	      	for (TTrack next: trackerPanel.visibleTools) {
    		      	showCalibrationTool(next);
    	      	}        			
        		}
        		else {
        			hideCalibrationTool(track);
        			trackerPanel.visibleTools.remove(track);
        			boolean toolsVisible = false;
    	      	for (TTrack next: trackerPanel.visibleTools) {
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
  			if (step==null || step.getPoints()[1]==null) {
  				trackerPanel.setSelectedTrack(track);
  			}
  		}
  		else if (track instanceof OffsetOrigin) {
  			int n = trackerPanel.getFrameNumber();
  			Step step = track.getStep(n);
  			if (step==null) {
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
    	if (trackerPanel.getSelectedTrack()==track) {
      	trackerPanel.setSelectedTrack(null);
    	}
    }

    /**
     * Refreshes this button.
     */
    void refresh() {
      setToolTipText(TrackerRes.getString("TToolbar.Button.TapeVisible.Tooltip")); //$NON-NLS-1$        	
      // add "visible" property change listeners to calibration tools
      for (TTrack track: trackerPanel.calibrationTools) {
      	track.removePropertyChangeListener("visible", TToolBar.this); //$NON-NLS-1$
      	track.addPropertyChangeListener("visible", TToolBar.this); //$NON-NLS-1$      	
      }
      // check visibility of tools and state of menu items
      boolean toolsVisible = false;
      for (TTrack track: trackerPanel.calibrationTools) {
		  	toolsVisible = toolsVisible || track.isVisible();
      }
      if (notYetCalibrated && toolsVisible) {
      	notYetCalibrated = false;
	      setSelected(true);
      }
    }
    
  }

}
