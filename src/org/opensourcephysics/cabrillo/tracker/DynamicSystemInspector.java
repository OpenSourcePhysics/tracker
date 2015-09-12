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

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.opensourcephysics.tools.FontSizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * This displays and sets DynamicSystem properties.
 *
 * @author Douglas Brown
 */
public class DynamicSystemInspector extends JDialog
    implements PropertyChangeListener {

  // instance fields
  protected DynamicSystem system;
  protected TrackerPanel trackerPanel;
  protected boolean isVisible;
  protected int particleCount;
  protected JButton closeButton, helpButton;
  protected ActionListener changeParticleListener;
  protected JPanel[] particlePanels;
  protected JButton[] changeButtons;
  protected DynamicParticle[] selectedParticles;
  protected JLabel[] particleLabels;
  protected TButton[] particleButtons;
  protected JPanel[] labelPanels;
  protected DynamicParticle newParticle;
  protected TButton systemButton;
  protected MouseListener selectListener;

  /**
   * Constructs a DynamicSystemInspector.
   *
   * @param track the DynamicSystem
   */
  public DynamicSystemInspector(DynamicSystem track) {
    super(JOptionPane.getFrameForComponent(track.trackerPanel), false);
    system = track;
    particleCount = 2;
    trackerPanel = system.trackerPanel;
    if (trackerPanel != null) {
    	trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.addPropertyChangeListener("tab", this); //$NON-NLS-1$
      }
    }
    setResizable(false);
    createGUI();
    initialize();
    updateDisplay();
  }

  /**
   * Initializes this inspector.
   */
  public void initialize() {
    updateDisplay();
  }

  /**
   * Responds to property change events. This listens for the
   * following events: "tab" from TFrame.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("tab")) { //$NON-NLS-1$
      if (trackerPanel != null && e.getNewValue() == trackerPanel) {
        setVisible(isVisible);
      }
      else {
        boolean vis = isVisible;
        setVisible(false);
        isVisible = vis;
      }
    }
    else if (e.getPropertyName().equals("track") //$NON-NLS-1$
    		&& e.getNewValue() instanceof DynamicParticle) {
    	newParticle = (DynamicParticle)e.getNewValue();
    	updateDisplay();
    }
    else updateDisplay();
  }

  /**
   * Overrides JDialog setVisible method.
   *
   * @param vis true to show this inspector
   */
  public void setVisible(boolean vis) {
    super.setVisible(vis);
    isVisible = vis;
  }

  /**
   * Disposes of this inspector.
   */
  public void dispose() {
    if (trackerPanel != null) {
      trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
      Iterator<DynamicParticle> it = trackerPanel.getDrawables(DynamicParticle.class).iterator();
      while (it.hasNext()) {
        PointMass p = it.next();
        p.removePropertyChangeListener("name", this); //$NON-NLS-1$
        p.removePropertyChangeListener("color", this); //$NON-NLS-1$
        p.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
      }
      TFrame frame = trackerPanel.getTFrame();
      if (frame != null) {
        frame.removePropertyChangeListener("tab", this); //$NON-NLS-1$
      }
    }
    super.dispose();
  }

//_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {
    changeParticleListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	final JButton button = (JButton)e.getSource();
      	final int n = button==changeButtons[0]? 0: 1;
      	final Action cloneAction = TActions.getAction("cloneTrack", trackerPanel); //$NON-NLS-1$
      	final Action cartesianAction = TActions.getAction("dynamicParticle", trackerPanel); //$NON-NLS-1$
      	final Action polarAction = TActions.getAction("dynamicParticlePolar", trackerPanel); //$NON-NLS-1$
        JPopupMenu popup = new JPopupMenu();
        boolean hasPopupItems = false;
  	    JMenu cloneMenu = new JMenu(
  	    		TrackerRes.getString("TMenuBar.MenuItem.Clone")); //$NON-NLS-1$
  	    Iterator<DynamicParticle> it = trackerPanel.getDrawables(DynamicParticle.class).iterator();
  	    while (it.hasNext()) {
  	    	DynamicParticle p = it.next();
  	      if (p instanceof DynamicSystem) continue; // no other systems
  	      // add items to clone menu
  	    	final JMenuItem cloneItem = new JMenuItem(p.getName(), p.getFootprint().getIcon(21, 16));
  	    	cloneItem.setActionCommand(p.getName());
  	    	cloneItem.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
			      	newParticle = null;
			      	cloneAction.actionPerformed(e);
	          	if (newParticle!=null) {
	          		newParticle.getInspector();
		          	selectedParticles[n] = newParticle;
				      	updateSystem();
	          	}
			      }
			    });
  	    	cloneMenu.add(cloneItem);
  	      if (p==selectedParticles[0] 
  	          || p==selectedParticles[1]
          		|| p.system!=null) continue;
  	      // add items to popup menu
  	      hasPopupItems = true;
  	    	final JMenuItem item = new JMenuItem(p.getName(), p.getFootprint().getIcon(21, 16)) {
  	    		public Dimension getPreferredSize() {
  	    			Dimension dim = super.getPreferredSize();
  	    			int w = button.getPreferredSize().width-2;
  	    			dim.width = Math.max(w, dim.width);
  	    			return dim;
  	    		}
  	    	};
  	    	item.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
	          	selectedParticles[n] = getParticle(item.getText());
	          	updateSystem();
			      }
			    });
  	    	popup.add(item);
  	    }
  	    if (hasPopupItems)
  	    	popup.addSeparator();
  	    JMenu newMenu = new JMenu(
  	    		TrackerRes.getString("TrackControl.Button.NewTrack"))  { //$NON-NLS-1$
	    		public Dimension getPreferredSize() {
	    			Dimension dim = super.getPreferredSize();
	    			int w = button.getPreferredSize().width-2;
	    			dim.width = Math.max(w, dim.width);
	    			return dim;
	    		}
  	    };
  	    popup.add(newMenu);
  	    
  	    JMenuItem cartesianItem = new JMenuItem(
  	    		TrackerRes.getString("TMenuBar.MenuItem.Cartesian")); //$NON-NLS-1$
  	    cartesianItem.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		      	newParticle = null;
          	cartesianAction.actionPerformed(e);
          	if (newParticle!=null) {
          		newParticle.getInspector();
	          	selectedParticles[n] = newParticle;
			      	updateSystem();
          	}
		      }
		    });
  	    newMenu.add(cartesianItem);
  	    JMenuItem polarItem = new JMenuItem(
  	    		TrackerRes.getString("TMenuBar.MenuItem.Polar")); //$NON-NLS-1$
  	    polarItem.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		      	newParticle = null;
          	polarAction.actionPerformed(e);
          	if (newParticle!=null) {
          		newParticle.getInspector();
	          	selectedParticles[n] = newParticle;
			      	updateSystem();
          	}
		      }
		    });
  	    newMenu.add(polarItem);
  	    if (cloneMenu.getItemCount() > 0)
  	    	popup.add(cloneMenu);
  	    JMenuItem noneItem = new JMenuItem(
  	    		TrackerRes.getString("DynamicSystemInspector.ParticleName.None")); //$NON-NLS-1$
  	    noneItem.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		      	newParticle = null;
          	selectedParticles[n] = null;
		      	updateSystem();
		      }
		    });
  	    popup.addSeparator();
  	    popup.add(noneItem); 	    
  	    
      	FontSizer.setFonts(popup, FontSizer.getLevel());
        popup.show(button, 0, button.getHeight());
      }
    };
  	selectListener = new MouseAdapter() {
  		public void mousePressed(MouseEvent e) {
  			TButton button = (TButton)e.getSource();
  			TTrack track = getParticle(button.getText());
  			trackerPanel.setSelectedTrack(track);
  		}
  		public void mouseExited(MouseEvent e) {
  			closeButton.requestFocusInWindow();
  		}
  	};
    // create GUI components
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    JPanel inspectorPanel = new JPanel(new GridLayout(1, 2));
    contentPane.add(inspectorPanel, BorderLayout.CENTER);
    particlePanels = new JPanel[particleCount];
    changeButtons = new JButton[particleCount];
    particleLabels = new JLabel[particleCount];
    particleButtons = new TButton[particleCount];
    labelPanels = new JPanel[particleCount];
    for (int i = 0; i < particleCount; i++) {
    	particlePanels[i] = new JPanel(new BorderLayout());
    	inspectorPanel.add(particlePanels[i]);
    	changeButtons[i] = new JButton();
    	changeButtons[i].addActionListener(changeParticleListener);
    	particleLabels[i] = new JLabel(
    			TrackerRes.getString("DynamicSystemInspector.ParticleName.None")) { //$NON-NLS-1$
    		public Dimension getPreferredSize() {
    			Dimension dim = super.getPreferredSize();
    			dim.height = systemButton.getPreferredSize().height;
    			return dim;
    		}
    	};
	  	particleLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
    	particleLabels[i].setAlignmentX(Component.CENTER_ALIGNMENT);
    	particleButtons[i] = new TButton();
    	particleButtons[i].setContentAreaFilled(false);
    	particleButtons[i].addMouseListener(selectListener);
    	particleButtons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
    	labelPanels[i] = new JPanel();
    	labelPanels[i].setLayout(new BoxLayout(labelPanels[i], BoxLayout.Y_AXIS));
    	labelPanels[i].add(particleLabels[i]);
    	particlePanels[i].add(labelPanels[i], BorderLayout.NORTH);
    	JPanel center = new JPanel();
    	center.add(changeButtons[i]);
    	particlePanels[i].add(center, BorderLayout.CENTER);
    }
    // create buttons and buttonbar
    systemButton = new TButton();
  	systemButton.setText(system.getName());
  	systemButton.setIcon(system.getFootprint().getIcon(21, 16));
    systemButton.setContentAreaFilled(false);
    systemButton.addMouseListener(selectListener);
    systemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    helpButton = new JButton();
    helpButton.setForeground(new Color(0, 0, 102));
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackerPanel.getTFrame().showHelp("system", 0); //$NON-NLS-1$
      }
    });
    closeButton = new JButton();
    closeButton.setForeground(new Color(0, 0, 102));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    JPanel buttonbar = new JPanel();
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(helpButton);
    buttonbar.add(closeButton);
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(systemButton);
    panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
    contentPane.add(panel, BorderLayout.NORTH);
  }

  /**
   * Updates the system to reflect the current particle selection.
   */
  private void updateSystem() {
  	if (selectedParticles[0]==null && selectedParticles[1]==null) {
    	system.setParticles(new DynamicParticle[0]);
  	}
  	else if (selectedParticles[0]==null)
    	system.setParticles(new DynamicParticle[] {selectedParticles[1]});
  	else if (selectedParticles[1]==null)
    	system.setParticles(new DynamicParticle[] {selectedParticles[0]});  	
  	else system.setParticles(selectedParticles);
  	if (newParticle==null)
  		newParticle = system;
  	system.getInspector().setSelectedPanel(newParticle.getName());
  	updateDisplay();
  	this.setVisible(true);
  }

  /**
   * Gets the particle with the specified name.
   *
   * @param name name of the particle
   * @return the particle
   */
  private DynamicParticle getParticle(String name) {
    ArrayList<DynamicParticle> particles = trackerPanel.getDrawables(DynamicParticle.class);
    for (DynamicParticle p: particles) {
      if (p.getName().equals(name)) return p;
    }
    return null;
  }

  /**
   * Updates this inspector to show the system's current particles.
   */
  protected void updateDisplay() {
    setTitle(TrackerRes.getString("DynamicSystemInspector.Title")); //$NON-NLS-1$
    helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$  
    closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$  
    selectedParticles = new DynamicParticle[particleCount];
  	systemButton.setText(system.getName());
  	systemButton.setIcon(system.getFootprint().getIcon(21, 16));
  	systemButton.setToolTipText(
  			TrackerRes.getString("TrackControl.Button.Properties.ToolTip") //$NON-NLS-1$
	  		+" "+system.getName());  //$NON-NLS-1$
    boolean empty = true;
  	for (int i = 0; i < particleCount; i++) {
	    Border etched = BorderFactory.createEtchedBorder();
	    TitledBorder title = BorderFactory.createTitledBorder(etched,
	        TrackerRes.getString("DynamicSystemInspector.Border.Title")+" "+(i+1)); //$NON-NLS-1$ //$NON-NLS-2$
	    particlePanels[i].setBorder(title);
    	changeButtons[i].setText(
    			TrackerRes.getString("DynamicSystemInspector.Button.Change")); //$NON-NLS-1$
    	labelPanels[i].removeAll();
    	if (system.particles.length>i && system.particles[i]!=null) {
    		empty = false;
      	selectedParticles[i] = system.particles[i];
      	particleButtons[i].setText(selectedParticles[i].getName());
      	particleButtons[i].setIcon(selectedParticles[i].getFootprint().getIcon(21, 16));
      	particleButtons[i].setToolTipText(
      			TrackerRes.getString("TrackControl.Button.Properties.ToolTip") //$NON-NLS-1$
  		  		+" "+selectedParticles[i].getName());  //$NON-NLS-1$

	    	labelPanels[i].setLayout(new BoxLayout(labelPanels[i], BoxLayout.Y_AXIS));
      	labelPanels[i].add(particleButtons[i]);
    	}
    	else {
      	selectedParticles[i] = null;
      	particleLabels[i].setText(
      			TrackerRes.getString("DynamicSystemInspector.ParticleName.None")); //$NON-NLS-1$
	    	labelPanels[i].setLayout(new BorderLayout());
      	labelPanels[i].add(particleLabels[i]);
    	}
  	}
  	changeButtons[particleCount-1].setEnabled(!empty);
  	changeButtons[0].requestFocusInWindow();
    pack();
    repaint();
  }
  
}
