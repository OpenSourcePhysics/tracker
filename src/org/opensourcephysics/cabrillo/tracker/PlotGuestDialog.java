/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.opensourcephysics.tools.FontSizer;

/**
 * This controls guest tracks in a TrackPlottingPanel.
 *
 * @author Douglas Brown
 */
public class PlotGuestDialog extends JDialog {

  // instance fields
  protected TrackPlottingPanel plot;
  protected TrackerPanel trackerPanel;
  protected JButton okButton, selectAllButton;
  protected JPanel checkboxPanel;
  protected ActionListener listener;
  protected TitledBorder instructions;
  protected TreeSet<Integer> allTracks = new TreeSet<Integer>();

  /**
   * Constructs a PlotGuestDialog.
   *
   * @param panel a TrackerPanel
   */
  public PlotGuestDialog(TrackerPanel panel) {
    super(JOptionPane.getFrameForComponent(panel), true);
    trackerPanel = panel;
    // listener for the checkboxes
    listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)e.getSource();
      	int id = Integer.parseInt(checkbox.getActionCommand());
	      TTrack track = TTrack.getTrack(id);
      	if (checkbox.isSelected()) plot.addGuest(track);
				else plot.removeGuest(track);
      	plot.plotData();
      }
    };
    setResizable(false);
    createGUI();
    pack();
  }
  
  public void setPlot(TrackPlottingPanel plot) {
  	this.plot = plot;
  	updateDisplay();
  }
  
//_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {
    JPanel inspectorPanel = new JPanel(new BorderLayout());
    setContentPane(inspectorPanel);
    // create checkboxPanel
    checkboxPanel = new JPanel(new GridLayout(0, 2));
    Border etched = BorderFactory.createEtchedBorder();
    instructions = BorderFactory.createTitledBorder(etched,""); //$NON-NLS-1$
    checkboxPanel.setBorder(instructions);
    inspectorPanel.add(checkboxPanel, BorderLayout.CENTER);
    // create ok button
    okButton = new JButton(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    okButton.setForeground(new Color(0, 0, 102));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    // create compareAllButton
    selectAllButton = new JButton(TrackerRes.getString("PlotGuestDialog.Button.SelectAll.Text")); //$NON-NLS-1$
    selectAllButton.setForeground(new Color(0, 0, 102));
    selectAllButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	for (Integer id: allTracks) {
		      TTrack track = TTrack.getTrack(id);
	      	plot.addGuest(track);
      	}
      	plot.plotData();
      	updateDisplay();
      }
    });
    // create buttonbar at bottom
    JPanel buttonbar = new JPanel();
    buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
    inspectorPanel.add(buttonbar, BorderLayout.SOUTH);
//    Box box = Box.createHorizontalBox();
//    buttonbar.add(box);
    buttonbar.add(selectAllButton);
    buttonbar.add(okButton);
//    box = Box.createHorizontalBox();
//    buttonbar.add(box);
  }

  /**
   * Updates this inspector to show cm's current masses.
   */
  protected void updateDisplay() {
    TTrack track = TTrack.getTrack(plot.trackID);
    setTitle(track.getName());
    instructions.setTitle(TrackerRes.getString("PlotGuestDialog.Instructions")); //$NON-NLS-1$
    // make checkboxes for all similar tracks in trackerPanel
    Class<? extends TTrack> type = track instanceof PointMass? PointMass.class:
    	track instanceof Vector? Vector.class: track.getClass();
    ArrayList<? extends TTrack> tracks = trackerPanel.getDrawables(type);
    tracks.removeAll(trackerPanel.calibrationTools);
    tracks.remove(track);
    int tracksPerColumn = 8;
    int cols = 1+(tracks.size()-1)/tracksPerColumn;
    checkboxPanel.setLayout(new GridLayout(0, cols));
    checkboxPanel.removeAll(); 
    int counter = 0;
    int h = 0;
    Box box = Box.createVerticalBox();
    allTracks.clear();
    for (TTrack next: tracks) {
    	allTracks.add(next.getID());
      JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(
          next.getName(), next.getFootprint().getIcon(21, 16));
      checkbox.setBorderPainted(false);
      // check the checkbox if next is a guest
      checkbox.setSelected(plot.guests.contains(next));
      checkbox.setActionCommand(String.valueOf(next.getID()));
      checkbox.addActionListener(listener);
      box.add(checkbox);
      h = checkbox.getPreferredSize().height;
      counter++;
      if (counter%tracksPerColumn==0) {
        checkboxPanel.add(box);
        counter = 0;
        if (checkboxPanel.getComponentCount()<cols) {
        	box = Box.createVerticalBox();
        }
      }
    }
    
    if (checkboxPanel.getComponentCount()<cols) {
    	int n = tracksPerColumn - box.getComponentCount();
    	box.add(Box.createVerticalStrut(n*h));
      checkboxPanel.add(box);
    }
  	FontSizer.setFonts(checkboxPanel, FontSizer.getLevel());
    pack();
    repaint();
  }

}
