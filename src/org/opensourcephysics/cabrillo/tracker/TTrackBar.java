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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.ResizableIcon;

/**
 * This is a toolbar that display selected track properties 
 * as well as memory and upgrade monitors/buttons.
 *
 * @author Douglas Brown
 */
public class TTrackBar extends JToolBar implements PropertyChangeListener {
	
  // static fields
  protected static Map<TrackerPanel, TTrackBar> trackbars = new HashMap<TrackerPanel, TTrackBar>();
  protected static JButton memoryButton, newVersionButton;
  protected static boolean outOfMemory = false;
  protected static Icon smallSelectIcon;
  protected static JButton testButton;
  protected static javax.swing.Timer testTimer;
  protected static boolean showOutOfMemoryDialog = true;
  
  // instance fields
  protected TrackerPanel trackerPanel; // manages & displays track data
  protected Component toolbarEnd;
  protected int toolbarComponentHeight;
  protected TButton trackButton;
  protected TButton selectButton;
  protected JLabel emptyLabel = new JLabel();
  protected JPopupMenu selectPopup = new JPopupMenu();
  static Icon pigIcon;

  static {
  	smallSelectIcon =  new ImageIcon(Tracker.class.getResource("resources/images/small_select.gif")); //$NON-NLS-1$
  	smallSelectIcon = new ResizableIcon(smallSelectIcon);
  	if (Tracker.testOn) {
	  	testButton = new JButton("test"); //$NON-NLS-1$
	  	testButton.addActionListener(new ActionListener() {
	  		public void actionPerformed(ActionEvent e) {
	    		final TFrame frame = (TFrame)testButton.getTopLevelAncestor();
	    		if (frame!=null && frame.getSelectedTab()>-1) {
	    			if (testTimer==null) {
	    				testTimer = new Timer(500, new ActionListener() {
		    	      public void actionPerformed(ActionEvent e) {
		    	  			// test action goes here		    	      	
	    	      		TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
	    	      		TTrack track = trackerPanel.getSelectedTrack();

		    	      	if (!testTimer.isRepeats()) {
		  	    				testTimer.stop();
		  	    				testTimer=null;
		  	    			}
		    	      }
		    	    });
	    				testTimer.setInitialDelay(20);
	    				testTimer.setRepeats(false);
	    				testTimer.start();
	    			} // end timer is null
	    			else {
	    				testTimer.stop();
	    				testTimer=null;
	    			}
	    		}
	  		}
	  	});
    }
    memoryButton = new TButton() {
    	public JPopupMenu getPopup() {
        JPopupMenu popup = new JPopupMenu();
  	    JMenuItem memoryItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Memory.Menu.SetSize")); //$NON-NLS-1$
  	    popup.add(memoryItem);
  	    memoryItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  	    		TFrame frame = (TFrame)memoryButton.getTopLevelAncestor();
  	    		if (frame!=null && frame.getSelectedTab()>-1) {
  	    			TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
  	    			TActions.getAction("config", trackerPanel).actionPerformed(null); //$NON-NLS-1$
  	    			Component c = frame.prefsDialog.runtimePanel;
  	    			frame.prefsDialog.tabbedPane.setSelectedComponent(c);
  	    		}
  	    	}
  	    });
  	    return popup;
    	}
    };
    Font font = memoryButton.getFont();
    memoryButton.setFont(font.deriveFont(Font.PLAIN, font.getSize()-1)); 
    memoryButton.addMouseListener(new MouseAdapter() {
    	public void mouseEntered(MouseEvent e) {
        refreshMemoryButton();
    	}
    });
		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
		Border line = BorderFactory.createLineBorder(Color.GRAY);
    memoryButton.setBorder(BorderFactory.createCompoundBorder(line, space));
    newVersionButton = new TButton() {
    	public JPopupMenu getPopup() {
        JPopupMenu popup = new JPopupMenu();
  	    JMenuItem upgradeItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Popup.MenuItem.Upgrade")); //$NON-NLS-1$
  	    popup.add(upgradeItem);
  	    upgradeItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  	    		String url = "http://"+Tracker.trackerWebsite; //$NON-NLS-1$
  	    		OSPDesktop.displayURL(url);
  	    	}
  	    });
  	    JMenuItem ignoreItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Popup.MenuItem.Ignore")); //$NON-NLS-1$
  	    popup.add(ignoreItem);
  	    ignoreItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  	    		Tracker.newerVersion = null;
  	    		Tracker.lastMillisChecked = System.currentTimeMillis();
  	    		TFrame frame = (TFrame)newVersionButton.getTopLevelAncestor();
  	    		if (frame!=null && frame.getSelectedTab()>-1) {
  	    			TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
  	    			TTrackBar trackbar = trackbars.get(trackerPanel);
  	    			trackbar.refresh();
  	    		}
  	    	}
  	    });
  	    return popup;
    	}
    };
    newVersionButton.setFont(font.deriveFont(Font.PLAIN, font.getSize()-1)); 
    newVersionButton.setForeground(Color.GREEN.darker()); 
    newVersionButton.setBorder(BorderFactory.createCompoundBorder(line, space));
    OSPLog.getOSPLog().addPropertyChangeListener("error", new PropertyChangeListener() { //$NON-NLS-1$
    	public void propertyChange(PropertyChangeEvent e) {
    		int type = Integer.parseInt(e.getNewValue().toString());
    		if (type == OSPLog.OUT_OF_MEMORY_ERROR) {
        	outOfMemory = true;
    		}
    	}
    });
  }

  /**
   * Gets the trackbar for the specified tracker panel.
   *
   * @param panel the tracker panel
   * @return the trackbar
   */
  public static synchronized TTrackBar getTrackbar(TrackerPanel panel) {
  	TTrackBar trackbar = trackbars.get(panel);
    if (trackbar == null) {
    	trackbar = new TTrackBar(panel);
    	trackbars.put(panel, trackbar);
    }
    return trackbar;
  }

  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  public void setFontLevel(int level) {
  	Object[] objectsToSize = new Object[]
  			{trackButton};
    FontSizer.setFonts(objectsToSize, level);
  }
  
  /**
   * TTrackBar constructor.
   *
   * @param panel the tracker panel
   */
  private TTrackBar(TrackerPanel panel) {
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("clear", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
    createGUI();
    refresh();
    validate();
  }

  @Override
  public void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" recycled by garbage collector"); //$NON-NLS-1$
  }

  /**
   * Gets the popup menu for the specified track.
   *
   * @param track the track
   * @return the popup menu
   */
  protected JPopupMenu getPopup(TTrack track) {
    JMenu trackMenu = track.getMenu(trackerPanel);
  	FontSizer.setFonts(trackMenu, FontSizer.getLevel());
    return trackMenu.getPopupMenu();
  }

  /**
   *  Creates the GUI.
   */
  protected void createGUI() {
    setFloatable(false);
    setBorder(BorderFactory.createEmptyBorder(3, 0, 2, 0));
    // select button
    selectButton = new TButton(smallSelectIcon) {
      protected JPopupMenu getPopup() {
      	return getSelectTrackPopup();
      }   	
    };
//    // mouse listener to reset zoom button to off state
//    addMouseListener(new MouseAdapter() {
//      public void mousePressed(MouseEvent e) {
//      	TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
//    		if (toolbar.zoomButton.getIcon()!=TToolBar.zoomOffIcon)
//    			toolbar.zoomButton.setIcon(TToolBar.zoomOffIcon);    		
//      }
//    });
    trackButton = new TButton() {
    	@Override
      protected JPopupMenu getPopup() {
    		
    		TTrack track = getTrack();
    		// special case: ParticleDataTrack
      	if (track instanceof ParticleDataTrack) { 
      		if (trackButton.context.contains("point")) { //$NON-NLS-1$
        		ParticleDataTrack dt = (ParticleDataTrack)track;
        		JMenu trackMenu = dt.getPointMenu(track.trackerPanel);
  	        FontSizer.setFonts(trackMenu, FontSizer.getLevel());
  	      	return trackMenu.getPopupMenu();
      		}
      		// else return leader's menu
      		ParticleDataTrack dt = ((ParticleDataTrack)track).getLeader();
      		JMenu trackMenu = dt.getMenu(track.trackerPanel);
	        FontSizer.setFonts(trackMenu, FontSizer.getLevel());
	      	return trackMenu.getPopupMenu();
      	}
      	
      	// general case
      	return super.getPopup();
      }

    };
		trackButton.setOpaque(false);
		emptyLabel.setOpaque(false);
		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
		Border line = BorderFactory.createLineBorder(Color.GRAY);
		trackButton.setBorder(BorderFactory.createCompoundBorder(line, space));
    // create horizontal glue for right end of toolbar
    toolbarEnd = Box.createHorizontalGlue();
  }

  /**
   * Refreshes and returns the "select track" popup menu.
   *
   * @return the popup
   */
  protected JPopupMenu getSelectTrackPopup() {
    selectPopup.removeAll();
    // add calibration tools and axes at end
//    final CoordAxes axes = trackerPanel.getAxes();
    final ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	JMenuItem item = (JMenuItem)e.getSource();
      	TTrack track = trackerPanel.getTrack(item.getText());
      	if (track==null) return;
      	if (trackerPanel.calibrationTools.contains(track)
      			|| track==trackerPanel.getAxes()) {
      		track.setVisible(true);
      	}
      	trackerPanel.setSelectedTrack(track);
      }
    };
    boolean hasTracks = false;
    ArrayList<TTrack> userTracks = trackerPanel.getUserTracks();
    for (TTrack track: userTracks) {
    	hasTracks = true;
    	JMenuItem item = new JMenuItem(track.getName("track"), track.getIcon(21, 16, "track")); //$NON-NLS-1$ //$NON-NLS-2$
    	item.addActionListener(listener);
    	selectPopup.add(item);
    }
  	if (hasTracks) {
    	selectPopup.addSeparator();
  	}
    for (TTrack track: trackerPanel.getTracks()) {
    	if (!userTracks.contains(track)) {
        if (track==trackerPanel.getAxes()
        		&& !trackerPanel.isEnabled("button.axes")) //$NON-NLS-1$
        	continue;
        if (trackerPanel.calibrationTools.contains(track)
        		&& track instanceof TapeMeasure) {
        	TapeMeasure tape = (TapeMeasure)track;
        	if (tape.isStickMode() && !trackerPanel.isEnabled("calibration.stick")) //$NON-NLS-1$
        		continue;
        	if (!tape.isStickMode() && !trackerPanel.isEnabled("calibration.tape")) //$NON-NLS-1$
        		continue;
        }
        if (track instanceof Calibration
        		&& !trackerPanel.isEnabled("calibration.points")) //$NON-NLS-1$
        	continue;
        if (track instanceof ParticleDataTrack)
        	continue;
        if (track instanceof OffsetOrigin
        		&& !trackerPanel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
        	continue;
        if (track instanceof PerspectiveTrack)
        	continue;
    		JMenuItem item = new JMenuItem(track.getName(), track.getFootprint().getIcon(21, 16));
      	item.addActionListener(listener);
      	selectPopup.add(item);
    	}
    }
    FontSizer.setFonts(selectPopup, FontSizer.getLevel());
    return selectPopup;
  }
  
  /**
   *  Refreshes the GUI.
   */
  protected void refresh() {
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh"); //$NON-NLS-1$
    Runnable runner = new Runnable() {
    	public void run() {
        selectButton.setToolTipText(TrackerRes.getString("TToolBar.Button.SelectTrack.Tooltip")); //$NON-NLS-1$
    		TTrack track = trackButton.getTrack();
    		if (track!=null) {
          track.removePropertyChangeListener("name", TTrackBar.this); //$NON-NLS-1$
          track.removePropertyChangeListener("color", TTrackBar.this); //$NON-NLS-1$
          track.removePropertyChangeListener("footprint", TTrackBar.this); //$NON-NLS-1$
        	toolbarComponentHeight = trackButton.getPreferredSize().height;
    		}
    		else {
    			CoordAxes axes = trackerPanel.getAxes();
    			if (axes!=null) {
        		trackButton.setTrack(axes);
          	toolbarComponentHeight = trackButton.getPreferredSize().height;
    			}
    		}
        removeAll();
        Dimension dime = new Dimension(toolbarComponentHeight, toolbarComponentHeight);
        selectButton.setPreferredSize(dime);
        selectButton.setMaximumSize(dime);
    		add(selectButton);
  			trackButton.context = "track"; //$NON-NLS-1$
        track = trackerPanel.getSelectedTrack();
        if (track != null && !(track instanceof PerspectiveTrack)) {
        	if (track instanceof ParticleDataTrack) {
        		TPoint p = trackerPanel.getSelectedPoint();
        		if (p!=null) {
	        		Step step = track.getStep(p, trackerPanel);
	        		if (step!=null && step.getTrack()==track) {
	        			trackButton.context = "point"; //$NON-NLS-1$
	        		}
        		}
        	}
        	trackButton.setTrack(track);
          // listen to tracks for property changes that affect icon or name
          track.addPropertyChangeListener("name", TTrackBar.this); //$NON-NLS-1$
          track.addPropertyChangeListener("color", TTrackBar.this); //$NON-NLS-1$
          track.addPropertyChangeListener("footprint", TTrackBar.this); //$NON-NLS-1$
        	add(trackButton);
          ArrayList<Component> list = track.getToolbarTrackComponents(trackerPanel);
          for (Component c: list) {
            if (c instanceof JComponent &&
                !(c instanceof JButton) && 
                !(c instanceof JCheckBox)) {
              JComponent jc = (JComponent)c;
              jc.setMaximumSize(null);
              jc.setPreferredSize(null);
              Dimension dim = jc.getPreferredSize();
              dim.height = toolbarComponentHeight;
              jc.setPreferredSize(dim);
              jc.setMaximumSize(dim);
            }
            add(c);
          }
          // selected point items
          TPoint p = trackerPanel.getSelectedPoint();
          if (p != null) {
            // a point is selected
            list = track.getToolbarPointComponents(trackerPanel, p);
            for (Component c: list) {
              if (c instanceof JComponent &&
                  !(c instanceof JButton)) {
                JComponent jc = (JComponent)c;
                jc.setMaximumSize(null);
                jc.setPreferredSize(null);
                Dimension dim = jc.getPreferredSize();
                dim.height = toolbarComponentHeight;
                jc.setPreferredSize(dim);
                jc.setMaximumSize(dim);
              }
              add(c);
            }
          }
        }
        add(toolbarEnd);
        if (testButton!=null) {
  		    add(testButton);
        }
        if (Tracker.newerVersion!=null) {
        	String s = TrackerRes.getString("TTrackBar.Button.Version"); //$NON-NLS-1$
        	newVersionButton.setText(s+" "+Tracker.newerVersion); //$NON-NLS-1$
  		    add(newVersionButton);
        }
    		memoryButton.setToolTipText(TrackerRes.getString("TTrackBar.Button.Memory.Tooltip")); //$NON-NLS-1$
//        refreshMemoryButton();
		    add(memoryButton);
        revalidate();
        repaint();
    	}
    };
    if (SwingUtilities.isEventDispatchThread()) runner.run();
    else SwingUtilities.invokeLater(runner); 
  }

  /**
   * Responds to the following events: "selectedtrack", "selectedpoint",
   * "track" from tracker panel, "footprint", "color", "name" from tracks.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("selectedtrack")) {  // selected track has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("footprint")  //$NON-NLS-1$
    		|| name.equals("color") //$NON-NLS-1$
    		|| name.equals("name")) { //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("selectedpoint")) {  // selected point has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("track")) {  // tracks have been added or removed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("clear")) {  // tracks have been cleared //$NON-NLS-1$
  		for (Integer n: TTrack.activeTracks.keySet()) {
  			TTrack track = TTrack.activeTracks.get(n);
	  		track.removePropertyChangeListener("name", TTrackBar.this); //$NON-NLS-1$
	      track.removePropertyChangeListener("color", TTrackBar.this); //$NON-NLS-1$
	      track.removePropertyChangeListener("footprint", TTrackBar.this); //$NON-NLS-1$
  		}
  		trackButton.setTrack(null);
      refresh();
    }
  }
  
  /**
   * Cleans up this trackbar
   */
  public void dispose() {
  	trackbars.remove(trackerPanel);
    removeAll();
    trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("clear", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
    for (Integer n: TTrack.activeTracks.keySet()) {
    	TTrack track = TTrack.activeTracks.get(n);
      track.removePropertyChangeListener("name", this); //$NON-NLS-1$
      track.removePropertyChangeListener("color", this); //$NON-NLS-1$
      track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
		}
		trackButton.setTrack(null);
    trackerPanel = null;
  }

  /**
   *  Refreshes the memory button.
   */
  protected static void refreshMemoryButton() {
		System.gc();
    java.lang.management.MemoryMXBean memory
				= java.lang.management.ManagementFactory.getMemoryMXBean();
		long cur = memory.getHeapMemoryUsage().getUsed()/(1024*1024);
		long max = memory.getHeapMemoryUsage().getMax()/(1024*1024);
    if (outOfMemory && showOutOfMemoryDialog) {
    	outOfMemory = false;
    	showOutOfMemoryDialog = false;
    	cur = max;
    	JOptionPane.showMessageDialog(memoryButton, 
    			TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message1")+"\n" //$NON-NLS-1$ //$NON-NLS-2$
    			+ TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message2"), //$NON-NLS-1$
    			TrackerRes.getString("Tracker.Dialog.OutOfMemory.Title"), //$NON-NLS-1$
    			JOptionPane.WARNING_MESSAGE);
    }
		String mem = TrackerRes.getString("TTrackBar.Button.Memory")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		String of = TrackerRes.getString("DynamicSystem.Parameter.Of")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		memoryButton.setText(mem+cur+"MB "+of+max+"MB"); //$NON-NLS-1$ //$NON-NLS-2$
		double used = ((double)cur)/max;
		memoryButton.setForeground(used>0.8? Color.red: Color.black);
  }

}
