/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2014  Douglas Brown
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
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

/**
 * A TTrack draws a series of visible Steps on a TrackerPanel.
 * This is an abstract class that cannot be instantiated directly.
 *
 * @author Douglas Brown
 */
public abstract class TTrack implements Interactive,
                                        Trackable,
                                        PropertyChangeListener {
	
  protected static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$
  protected static JDialog skippedStepWarningDialog;
  protected static JTextPane skippedStepWarningTextpane;
  protected static JCheckBox skippedStepWarningCheckbox;
  protected static JButton closeButton;
	protected static boolean skippedStepWarningOn = true;
  protected static FontRenderContext frc
		  = new FontRenderContext(null,   // no AffineTransform
		                          false,  // no antialiasing
		                          false); // no fractional metrics
  
  // instance fields
  protected String name = TrackerRes.getString("TTrack.Name.None"); //$NON-NLS-1$
  protected String description = ""; //$NON-NLS-1$
  protected boolean visible = true;
  protected boolean trailVisible = false;
  protected int trailLength; // controls trail length
  protected boolean locked = false;
  protected boolean enabled = true;
  protected boolean viewable = true; // determines whether Views include this track
  protected Footprint[] footprints = new Footprint[0];
  protected Footprint footprint;
  protected Footprint defaultFootprint;
  protected Color[] defaultColors = new Color[] {Color.red};
  protected StepArray steps = new StepArray();
  protected Collection<TrackerPanel> panels = new HashSet<TrackerPanel>();
  protected PropertyChangeSupport support;
  protected HashMap<String, Object> properties = new HashMap<String, Object>();
  protected DatasetManager data;
  protected HashMap<TrackerPanel, double[]> worldBounds = new HashMap<TrackerPanel, double[]>();
  protected Point2D point = new Point2D.Double();
  protected ArrayList<Component> toolbarTrackComponents = new ArrayList<Component>();
  protected ArrayList<Component> toolbarPointComponents = new ArrayList<Component>();
  protected JLabel tLabel, xLabel, yLabel, magLabel, angleLabel, stepLabel;
  protected JLabel tValueLabel, stepValueLabel;
  protected NumberField tField, xField, yField, magField;
  protected DecimalField angleField;
  protected Border fieldBorder;
  protected Component tSeparator, xSeparator, ySeparator, magSeparator,
      angleSeparator, stepSeparator;
  protected JMenu menu;
  protected boolean autoAdvance;
  protected boolean markByDefault = false, isMarking = false;
  protected JCheckBoxMenuItem visibleItem;
  protected JCheckBoxMenuItem trailVisibleItem;
  protected JCheckBoxMenuItem markByDefaultItem;
  protected JCheckBoxMenuItem autoAdvanceItem;
  protected JCheckBoxMenuItem lockedItem;
  protected JMenuItem nameItem;
  protected JMenuItem colorItem;
  protected JMenu footprintMenu;
  protected ActionListener footprintListener, circleFootprintListener;
  protected JMenuItem deleteTrackItem, deleteStepItem, clearStepsItem;
  protected JMenuItem descriptionItem;
  protected JMenuItem dataBuilderItem;
  protected JSpinner xSpinner, ySpinner;
  protected Font labelFont = new Font("arial", Font.PLAIN, 12); //$NON-NLS-1$
  protected JDialog nameDialog;
  protected JTextField nameField;
  protected Action nameAction;
  protected TrackerPanel trackerPanel;
  protected XMLProperty dataProp;
  protected Object[][] constantsLoadedFromXML;
  protected String[] dataDescriptions;
  protected boolean dataValid; // true if data is valid
	protected boolean refreshDataLater;
  protected int[] preferredColumnOrder;
  protected ArrayList<Integer> dataFrames = new ArrayList<Integer>();
  protected String partName, hint;
	protected int stepSizeWhenFirstMarked;
  protected TreeSet<Integer> keyFrames = new TreeSet<Integer>();
  // for autotracking
  protected boolean autoTrackerMarking;
  protected int targetIndex;
  // attached tracks--used by AttachmentDialog with TapeMeasure and Protractor tracks
  protected TTrack[] attachments;
  // user-editable text columns shown in DataTable view
  protected Map<String, String[]> textColumnEntries = new TreeMap<String, String[]>();
  protected ArrayList<String> textColumnNames = new ArrayList<String>();

  /**
   * Constructs a TTrack.
   */
  protected TTrack() {
    support = new SwingPropertyChangeSupport(this);
    // create toolbar components
    stepLabel = new JLabel();
    stepLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    stepValueLabel = new JLabel();
    stepValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    tValueLabel = new JLabel();
    tValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
    tField = new DecimalField(4, 3) {
    	public void setValue(double value) {
    		super.setValue(value);
    		tValueLabel.setText("("+tField.getText()+")"); //$NON-NLS-1$ //$NON-NLS-2$
    	}
    };
    tField.setUnits("s"); //$NON-NLS-1$
    // create spinners
    SpinnerModel model = new SpinnerNumberModel(0, -100, 100, 0.1);
    xSpinner = new JSpinner(model);
    JSpinner.NumberEditor editor = new JSpinner.NumberEditor(xSpinner, "0.00"); //$NON-NLS-1$
    editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    xSpinner.setEditor(editor);
    model = new SpinnerNumberModel(0, -100, 100, 0.1);
    ySpinner = new JSpinner(model);
    editor = new JSpinner.NumberEditor(ySpinner, "0.00"); //$NON-NLS-1$
    editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    ySpinner.setEditor(editor);
    // create labels and fields
    Border empty = BorderFactory.createEmptyBorder(0, 1, 0, 2);
    xLabel = new JLabel("x"); //$NON-NLS-1$
    xLabel.setBorder(empty);
    xField = new NumberField(5);
    yLabel = new JLabel("y"); //$NON-NLS-1$
    yLabel.setBorder(empty);
    yField = new NumberField(5);
    magLabel = new JLabel("r"); //$NON-NLS-1$
    magLabel.setBorder(empty);
    magField = new NumberField(5);
    magField.setMinValue(0);
    angleLabel = new JLabel("theta"); //$NON-NLS-1$
    angleLabel.setBorder(empty);
    angleField = new DecimalField(4, 1);
    angleField.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (OSPRuntime.isPopupTrigger(e)) {
      		JPopupMenu popup = new JPopupMenu();
      		JMenuItem item = new JMenuItem();
      		final boolean radians = angleField.getConversionFactor()==1;
      		item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	TFrame frame = trackerPanel.getTFrame();
            	frame.setAnglesInRadians(!radians);
            }
          });
      		item.setText(radians? 
      				TrackerRes.getString("TTrack.AngleField.Popup.Degrees"): //$NON-NLS-1$
      				TrackerRes.getString("TTrack.AngleField.Popup.Radians")); //$NON-NLS-1$
      		popup.add(item);
        	FontSizer.setFonts(popup, FontSizer.getLevel());
      		popup.show(angleField, 0, angleField.getHeight());
      	}
      }
    });
    empty = BorderFactory.createEmptyBorder(0, 3, 0, 3);
    Color grey = new Color(102, 102, 102);
    Border etch = BorderFactory.createEtchedBorder(Color.white, grey);
    fieldBorder = BorderFactory.createCompoundBorder(etch, empty);
    tField.setBorder(fieldBorder);
    xField.setBorder(fieldBorder);
    yField.setBorder(fieldBorder);
    magField.setBorder(fieldBorder);
    angleField.setBorder(fieldBorder);
    stepSeparator = Box.createRigidArea(new Dimension(4, 4));
    tSeparator = Box.createRigidArea(new Dimension(4, 4));
    xSeparator = Box.createRigidArea(new Dimension(6, 4));
    ySeparator = Box.createRigidArea(new Dimension(6, 4));
    magSeparator = Box.createRigidArea(new Dimension(6, 4));
    angleSeparator = Box.createRigidArea(new Dimension(6, 4));
    // create menu items
    visibleItem = new JCheckBoxMenuItem();
    trailVisibleItem = new JCheckBoxMenuItem();
    autoAdvanceItem = new JCheckBoxMenuItem();
    markByDefaultItem = new JCheckBoxMenuItem();
    lockedItem = new JCheckBoxMenuItem();
    deleteTrackItem = new JMenuItem();
    deleteStepItem = new JMenuItem();
    clearStepsItem = new JMenuItem();
    colorItem = new JMenuItem();
    colorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Color color = getColor();
      	Color newColor = chooseColor(color, TrackerRes.getString("TTrack.Dialog.Color.Title")); //$NON-NLS-1$
        if (newColor!=color) {
        	XMLControl control = new XMLControlElement(TTrack.this);
          setColor(newColor);
          Undo.postTrackEdit(TTrack.this, control);
        }
      }
    });
    nameDialog = new JDialog((Frame)null, null, true);
    nameDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    nameDialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
      	String newName = nameField.getText();
      	if (trackerPanel != null) 
      		trackerPanel.setTrackName(TTrack.this, newName);
      }
    });
    nameField = new JTextField(20);
    nameField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	String newName = nameField.getText();
      	if (trackerPanel != null) 
      		trackerPanel.setTrackName(TTrack.this, newName);
      }
    });
    final JLabel nameLabel = new JLabel();
    JToolBar bar = new JToolBar();
    bar.setFloatable(false);
    bar.add(nameLabel);
    bar.add(nameField);
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(bar, BorderLayout.CENTER);
    nameDialog.setContentPane(contentPane);
    nameDialog.pack();
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - nameDialog.getBounds().width) / 2;
    int y = (dim.height - nameDialog.getBounds().height) / 2;
    nameDialog.setLocation(x, y);
    nameAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        // show dialog with name of this track selected
    		FontSizer.setFonts(nameDialog, FontSizer.getLevel());
      	nameDialog.setTitle(TrackerRes.getString("TTrack.Dialog.Name.Title")); //$NON-NLS-1$
      	nameLabel.setText(TrackerRes.getString("TTrack.Dialog.Name.Label")); //$NON-NLS-1$
      	nameField.setText(getName());
      	nameField.selectAll();
      	nameDialog.pack();
        nameDialog.setVisible(true);
      }
    };
    nameItem = new JMenuItem();
    nameItem.addActionListener(nameAction);
    footprintMenu = new JMenu();
    descriptionItem = new JMenuItem();
    descriptionItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (trackerPanel != null) {
          TFrame frame = trackerPanel.getTFrame();
          if (frame != null) {
          	if (frame.notesDialog.isVisible()) {
          		frame.notesDialog.setVisible(true);
          	}
          	else frame.getToolBar(trackerPanel).notesButton.doClick();
          }
        }
      }
    });
    dataBuilderItem = new JMenuItem();
    dataBuilderItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (trackerPanel != null) {
	      	trackerPanel.getDataBuilder().setSelectedPanel(getName());
	      	trackerPanel.getDataBuilder().setVisible(true);
      	}
      }
    });
    visibleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setVisible(visibleItem.isSelected());
        TTrack.this.repaint();
      }
    });
    trailVisibleItem.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        setTrailVisible(trailVisibleItem.isSelected());
        if (!TTrack.this.isTrailVisible()) {
          // clear selected point on panels if nec
          Iterator<TrackerPanel> it = panels.iterator();
          TrackerPanel panel;
          while (it.hasNext()) {
            panel = it.next();
            Step step = panel.getSelectedStep();
            if (step != null && step.getTrack() == TTrack.this) {
              if (!(step.getFrameNumber() == panel.getFrameNumber())) {
                panel.setSelectedPoint(null);
              }
            }
          }
        }
        TTrack.this.repaint();
      }
    });
    markByDefaultItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setMarkByDefault(markByDefaultItem.isSelected());
      }
    });
    autoAdvanceItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAutoAdvance(autoAdvanceItem.isSelected());
      }
    });
    lockedItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setLocked(lockedItem.isSelected());
      }
    });
    deleteTrackItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	delete(); 
      }
    });
    deleteStepItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	trackerPanel.deletePoint(trackerPanel.getSelectedPoint());
      }
    });
    clearStepsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (isLocked()) return;
        XMLControl control = new XMLControlElement(TTrack.this);
    		for (int n = 0; n < getSteps().length; n++) {
          steps.setStep(n, null);
    		}
        for (String columnName: textColumnNames) {
        	textColumnEntries.put(columnName, new String[0]);
        }
        Undo.postTrackEdit(TTrack.this, control);
    		if (TTrack.this instanceof PointMass) {
    			((PointMass)TTrack.this).updateDerivatives();
    		}
       	AutoTracker autoTracker = trackerPanel.getAutoTracker();
       	if (autoTracker.getTrack()==TTrack.this)
       		autoTracker.reset();
				autoTracker.getWizard().setVisible(false);
        firePropertyChange("steps", null, null); //$NON-NLS-1$
        trackerPanel.repaint();
      }
    });
    footprintListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String footprintName = e.getActionCommand();
        if (getFootprint().getName().equals(footprintName)) return;
        XMLControl control = new XMLControlElement(TTrack.this);
        setFootprint(footprintName);
        Undo.postTrackEdit(TTrack.this, control);
      }
    };
    circleFootprintListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	footprintListener.actionPerformed(e);
      	CircleFootprint cfp = (CircleFootprint)getFootprint();
      	cfp.showProperties(TTrack.this);
      }
    };
  }

  /**
   * Shows and hides this track.
   *
   * @param visible <code>true</code> to show this track
   */
  public void setVisible(boolean visible) {
    Boolean prev = new Boolean(this.visible);
    this.visible = visible;
    support.firePropertyChange("visible", prev, new Boolean(visible)); //$NON-NLS-1$
    if (trackerPanel != null) trackerPanel.repaint();
  }
  
  /**
   * Removes this track from all panels that draw it. If no other objects have
   * a reference to it, this should then be garbage-collected.
   */
  public void delete() {
    if (isLocked() && !isDependent()) return;
    cleanup();
    if (trackerPanel != null) {
    	trackerPanel.setSelectedPoint(null);
      // handle case when this is the origin of current reference frame
    	ImageCoordSystem coords = trackerPanel.getCoords();
      if (coords instanceof ReferenceFrame && 
      				((ReferenceFrame)coords).getOriginTrack() == this) {
        // set coords to underlying coords
        coords = ( (ReferenceFrame) coords).getCoords();
      	trackerPanel.setCoords(coords);
      }    	
    }
    Undo.postTrackDelete(this); // posts undoable edit
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext()) {
      TrackerPanel panel = it.next();
      panel.removeTrack(this);
    }
    setTrackerPanel(null);
    repaint(); // repaints all panels
  }

  /**
   * Reports whether or not this is visible.
   *
   * @return <code>true</code> if this track is visible
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * Shows and hides the trail. If the trail is shown, all steps are
   * visible. If not, only the current step is visible.
   *
   * @param visible <code>true</code> to show trail
   */
  public void setTrailVisible(boolean visible) {
    trailVisible = visible;
  }

  /**
   * Gets the trail visibility.
   *
   * @return <code>true</code> if trail is visible
   */
  public boolean isTrailVisible() {
    return trailVisible;
  }

  /**
   * Sets the trail length.
   *
   * @param steps the trail length
   */
  public void setTrailLength(int steps) {
    trailLength = Math.max(0, steps);
  }

  /**
   * Gets the trail length.
   *
   * @return trail length
   */
  public int getTrailLength() {
  	if (isMarking) return 1;
    return trailLength;
  }

  /**
   * Locks and unlocks this track. When locked, no changes are allowed.
   *
   * @param locked <code>true</code> to lock this
   */
  public void setLocked(boolean locked) {
    this.locked = locked;
    support.firePropertyChange("locked", null, new Boolean(locked)); //$NON-NLS-1$
  }

  /**
   * Gets the locked property.
   *
   * @return <code>true</code> if this is locked
   */
  public boolean isLocked() {
    return locked;
  }

  /**
   * Sets the autoAdvance property.
   *
   * @param auto <code>true</code> to request that the video autoadvance while marking.
   */
  public void setAutoAdvance(boolean auto) {
    autoAdvance = auto;
  }

  /**
   * Gets the autoAdvance property.
   *
   * @return <code>true</code> if this is autoadvance
   */
  public boolean isAutoAdvance() {
    return autoAdvance;
  }

  /**
   * Sets the markByDefault property. When true, the mouse handler should mark
   * a point whenever the active track reports itself incomplete.
   *
   * @param mark <code>true</code> to mark by default
   */
  public void setMarkByDefault(boolean mark) {
    markByDefault = mark;
  }

  /**
   * Gets the markByDefault property. When true, the mouse handler should mark
   * a point whenever the active track reports itself incomplete.
   *
   * @return <code>true</code> if this marks by default
   */
  public boolean isMarkByDefault() {
    return markByDefault;
  }

  /**
   * Gets the color.
   *
   * @return the current color
   */
  public Color getColor() {
  	if (footprint==null)
  		return defaultColors[0];
    return footprint.getColor();
  }

  /**
   * Sets the color.
   *
   * @param color the desired color
   */
  public void setColor(Color color) {
  	if (color == null) color = defaultColors[0];
    for (int i = 0; i < footprints.length; i++)
      footprints[i].setColor(color);
    erase();
    if (trackerPanel != null) {
    	trackerPanel.changed = true;
    	if (trackerPanel.modelBuilder != null) {
      	trackerPanel.modelBuilder.refreshDropdown(null);
      }
      if (trackerPanel.dataBuilder != null) {
      	org.opensourcephysics.tools.FunctionPanel panel = 
      		trackerPanel.dataBuilder.getPanel(getName());
      	panel.setIcon(footprint.getIcon(21, 16));
      	trackerPanel.dataBuilder.refreshDropdown(null);
      }
    }
    support.firePropertyChange("color", null, color); //$NON-NLS-1$
  }
  
  /**
   * Sets the color to one of the default colors[].
   *
   * @param index the color index
   */
  public void setColorToDefault(int index) {
  	int colorIndex = index%defaultColors.length;
  	setColor(defaultColors[colorIndex]);
  }

  /**
   * Sets the default name and color for a specified tracker panel.
   *
   * @param trackerPanel the TrackerPanel
   * @param connector the string connector between the name and letter suffix
   */
  public void setDefaultNameAndColor(TrackerPanel trackerPanel, String connector) {
    String name = getName();
    int i = trackerPanel.getAlphabetIndex(name, connector);
    String letter = TrackerPanel.alphabet.substring(i, i+1);
    setName(name+connector+letter);
    setColorToDefault(i);
  }
  
  /**
   * Displays a JColorChooser and returns the selected color.
   *
   * @param color the initial color to select
   * @param title the title for the dialog
   * @return the newly selected color. or initial color if cancelled
   */
  public Color chooseColor(final Color color, String title) {
  	final JColorChooser chooser = new JColorChooser();
  	chooser.setColor(color);
  	ActionListener cancelListener = new ActionListener() {
  		public void actionPerformed(ActionEvent e) {
  			chooser.setColor(color);
  		}
  	};
  	JDialog dialog = JColorChooser.createDialog(null, title, true, 
  			chooser, null, cancelListener);
  	FontSizer.setFonts(dialog, FontSizer.getLevel());
  	dialog.setVisible(true);
  	return chooser.getColor();
  }
  
  /**
   * Gets the name of this track.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this track.
   *
   * @param newName the new name of this track
   */
  public void setName(String newName) {
    if (newName != null && !newName.trim().equals("")) { //$NON-NLS-1$
      String prevName = name;
      name = newName;
      this.repaint();
      if (trackerPanel != null) {
      	trackerPanel.changed = true;
      	if (trackerPanel.dataBuilder != null)
      		trackerPanel.dataBuilder.renamePanel(prevName, newName);
      }
      support.firePropertyChange("name", prevName, name); //$NON-NLS-1$
    }
  }

  /**
   * Gets the description of this track.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of this track.
   *
   * @param desc a description
   */
  public void setDescription(String desc) {
    if (desc == null) desc = ""; //$NON-NLS-1$
    description = desc;
  }

  /**
   * Overrides Object toString method.
   *
   * @return a description of this object
   */
  public String toString() {
    return getClass().getSimpleName()+" "+name; //$NON-NLS-1$
  }

  /**
   * Gets a message about this track to display in a message box.
   *
   * @return the message
   */
  public String getMessage() {
  	String s = getName();
  	if (partName != null) 
    	s += " "+partName; //$NON-NLS-1$
    if (isLocked()) {
      hint = TrackerRes.getString("TTrack.Locked.Hint"); //$NON-NLS-1$
    }
    if (Tracker.showHints && hint != null) 
    	s += " ("+hint+")"; //$NON-NLS-1$ //$NON-NLS-2$
    return s;
  }

  /**
   * Determines whether views and track menu include this track.
   *
   * @param viewable <code>true</code> to include this track in views
   */
  public void setViewable(boolean viewable) {
    this.viewable = viewable;
  }

  /**
   * Reports whether or not this is viewable.
   *
   * @return <code>true</code> if this track is viewable
   */
  public boolean isViewable() {
    return viewable;
  }

  /**
   * Reports whether or not this is dependent. A dependent track gets some
   * or all of its data from other tracks. Dependent tracks should override
   * this method to return true.
   *
   * @return <code>true</code> if this track is dependent
   */
  public boolean isDependent() {
    return false;
  }

  /**
   * Sets the footprint choices. The footprint is set to the first choice.
   *
   * @param choices the array of Footprints available to this track
   */
  public void setFootprints(Footprint[] choices) {
    Collection<Footprint> valid = new ArrayList<Footprint>();
    for (int i = 0; i < choices.length; i++) {
      if (choices[i] != null && choices[i].getLength() <= getFootprintLength()) {
        if (getFootprint() != null) choices[i].setColor(getColor());
        valid.add(choices[i]);
      }
    }
    if (valid.size() > 0) {
      footprints = valid.toArray(new Footprint[0]);
      setFootprint(footprints[0].getName());
    }
  }

  /**
   * Sets the footprint choices. The footprint is set to the first choice.
   * The step parameter may be used to set the footprints of secondary step
   * arrays (veloc, accel, etc).
   *
   * @param choices the array of Footprints available to this track
   * @param step the step that identifies the step array
   */
  public void setFootprints(Footprint[] choices, Step step) {
    setFootprints(choices);
  }

  /**
   * Gets the footprint choices.
   *
   * @return the array of Footprints available to this track
   */
  public Footprint[] getFootprints() {
    return footprints;
  }

  /**
   * Gets the footprint choices. The step parameter may be
   * used to get the footprints of secondary step arrays (veloc, accel, etc).
   *
   * @param step the step that identifies the step array
   * @return the array of Footprints available to this track
   */
  public Footprint[] getFootprints(Step step) {
    return footprints;
  }

  /**
   * Adds a new footprint to the current choices.
   *
   * @param footprint the footprint
   */
  public void addFootprint(Footprint footprint) {
    if (footprint.getLength() == getFootprintLength()) {
      Footprint[] prints = new Footprint[footprints.length + 1];
      System.arraycopy(footprints, 0, prints, 0, footprints.length);
      prints[footprints.length] = footprint;
      footprints = prints;
    }
  }

  /**
   * Sets the footprint to the specified choice.
   *
   * @param name the name of the desired footprint
   */
  public void setFootprint(String name) {
  	String props = null;
  	int n = name.indexOf("#"); //$NON-NLS-1$
  	if (n>-1) {
  		props = name.substring(n+1);
  		name = name.substring(0, n);
  	}
    for (int i = 0; i < footprints.length; i++)
      if (name.equals(footprints[i].getName())) {
        footprint = footprints[i];
        if (footprint instanceof CircleFootprint) {
        	((CircleFootprint)footprint).setProperties(props);
        }
        Step[] stepArray = steps.array;
        for (int j = 0; j < stepArray.length; j++)
          if (stepArray[j] != null)
            stepArray[j].setFootprint(footprint);
        repaint();
        if (trackerPanel != null) {
        	trackerPanel.changed = true;
        	if (trackerPanel.modelBuilder != null) {
          	trackerPanel.modelBuilder.refreshDropdown(null);
          }
          if (trackerPanel.dataBuilder != null) {
          	org.opensourcephysics.tools.FunctionPanel panel = 
          		trackerPanel.dataBuilder.getPanel(getName());
          	panel.setIcon(footprint.getIcon(21, 16));
          	trackerPanel.dataBuilder.refreshDropdown(null);
          }
        }
        support.firePropertyChange("footprint", null, footprint); //$NON-NLS-1$
        return;
      }
  }

  /**
   * Gets the current footprint.
   *
   * @return the footprint
   */
  public Footprint getFootprint() {
    return footprint;
  }

  /**
   * Sets the footprint to the specified choice. The step parameter may be
   * used to set the footprints of secondary step arrays (veloc, accel, etc).
   *
   * @param name the name of the desired footprint
   * @param step the step that identifies the step array
   */
  public void setFootprint(String name, Step step) {
    setFootprint(name);
  }

  /**
   * Gets the current footprint. The step parameter may be
   * used to get the footprints of secondary step arrays (veloc, accel, etc).
   *
   * @param step the step that identifies the step array
   * @return the footprint
   */
  public Footprint getFootprint(Step step) {
    return getFootprint();
  }

  /**
   * Gets the length of the steps created by this track.
   *
   * @return the footprint length
   */
  public abstract int getStepLength();

  /**
   * Gets the length of the footprints required by this track.
   *
   * @return the footprint length
   */
  public abstract int getFootprintLength();

  /**
   * Creates a new step.
   *
   * @param n the frame number
   * @param x the x coordinate in image space
   * @param y the y coordinate in image space
   * @return the new step
   */
  public abstract Step createStep(int n, double x, double y);

  /**
   * Deletes a step.
   *
   * @param n the frame number
   * @return the deleted step
   */
  public Step deleteStep(int n) {
    if (locked) return null;
    Step step = steps.getStep(n);
    if (step != null) {
      XMLControl control = new XMLControlElement(this);
      steps.setStep(n, null);
      for (String columnName: textColumnNames) {
      	String[] entries = textColumnEntries.get(columnName);
      	if (entries.length>n) {
      		entries[n] = null;
      	}
      }
      Undo.postTrackEdit(this, control);
      support.firePropertyChange("step", null, new Integer(n)); //$NON-NLS-1$
    }
    return step;
  }

  /**
   * Gets a step specified by frame number. May return null.
   *
   * @param n the frame number
   * @return the step
   */
  public Step getStep(int n) {
    return steps.getStep(n);
  }

  /**
   * Gets next visible step after the specified step. May return null.
   *
   * @param step the step
   * @param trackerPanel the tracker panel
   * @return the next visiblestep
   */
  public Step getNextVisibleStep(Step step, TrackerPanel trackerPanel) {
    Step[] steps = getSteps();
    boolean found = false;
    for (int i = 0; i < steps.length; i++) {
      // return first step after found
      if (found && steps[i] != null &&
          isStepVisible(steps[i], trackerPanel)) return steps[i];
      // find specified step
      if (steps[i] == step) found = true;
    }
    // cycle back to beginning if next step not yet identified
    if (found) {
      for (int i = 0; i < steps.length; i++) {
        // return first visible step
        if (steps[i] != null && steps[i] != step &&
            isStepVisible(steps[i], trackerPanel))
          return steps[i];
      }
    }
    return null;
  }

  /**
   * Gets first visible step before the specified step. May return null.
   *
   * @param step the step
   * @param trackerPanel the tracker panel
   * @return the previous visible step
   */
  public Step getPreviousVisibleStep(Step step, TrackerPanel trackerPanel) {
    Step[] steps = getSteps();
    boolean found = false;
    for (int i = steps.length-1; i > -1; i--) {
      // return first step after found
      if (found && steps[i] != null &&
          isStepVisible(steps[i], trackerPanel)) return steps[i];
      // find specified step
      if (steps[i] == step) found = true;
    }
    // cycle back to end if previous step not yet identified
    if (found) {
      for (int i = steps.length-1; i > -1; i--) {
        // return first visible step
        if (steps[i] != null && steps[i] != step &&
            isStepVisible(steps[i], trackerPanel))
          return steps[i];
      }
    }
    return null;
  }

  /**
   * Gets a step containing a TPoint. May return null.
   *
   * @param point a TPoint
   * @param trackerPanel the tracker panel holding the TPoint
   * @return the step containing the TPoint
   */
  public Step getStep(TPoint point, TrackerPanel trackerPanel) {
    if (point == null) return null;
    Step[] stepArray = steps.array;
    for (int j = 0; j < stepArray.length; j++)
      if (stepArray[j] != null) {
        TPoint[] points = stepArray[j].getPoints();
        for (int i = 0; i < points.length; i++)
          if (points[i] == point) return stepArray[j];
      }
    return null;
  }

  /**
   * Gets the step array. Some or all elements may be null.
   *
   * @return the step array
   */
  public Step[] getSteps() {
    return steps.array;
  }

  /**
   * Returns true if the step at the specified frame number is complete.
   * Points may be created or remarked if false.
   *
   * @param n the frame number
   * @return <code>true</code> if the step is complete, otherwise false
   */
  public boolean isStepComplete(int n) {
    return false; // enables remarking
  }
  
  /**
   * Used by autoTracker to mark a step at a match target position. 
   * 
   * @param n the frame number
   * @param x the x target coordinate in image space
   * @param y the y target coordinate in image space
   * @return the TPoint that was automarked
   */
  public TPoint autoMarkAt(int n, double x, double y) {
  	createStep(n, x, y);
  	return getMarkedPoint(n, getTargetIndex());
  }
  
  /**
   * Used by autoTracker to get the marked point for a given frame and index. 
   * 
   * @param n the frame number
   * @param index the index
   * @return the step TPoint at the index
   */
  public TPoint getMarkedPoint(int n, int index) {
  	Step step = getStep(n);
  	if (step==null) return null;
  	return step.getPoints()[index];
  }
  
  /**
   * Returns the target index for the autotracker.
   *
   * @return the point index
   */
  protected int getTargetIndex() {
  	return targetIndex;
  }

  /**
   * Sets the target index for the autotracker.
   *
   * @param index the point index
   */
  protected void setTargetIndex(int index) {
  	if (isAutoTrackable(index))
  		targetIndex = index;
  }

  /**
   * Sets the target index for the autotracker.
   *
   * @param description the description of the target
   */
  protected void setTargetIndex(String description) {
  	for (int i=0; i<getStepLength(); i++) {
  		if (description.equals(getTargetDescription(i))) {
  			setTargetIndex(i);
  			break;
  		}
  	}
  }

  /**
   * Sets the target index for the autotracker.
   *
   * @param p a TPoint associated with a step in this track
   */
  protected void setTargetIndex(TPoint p) {
		Step step = getStep(p, trackerPanel);
		if (step!=null)
  		setTargetIndex(step.getPointIndex(p));  	
  }

  /**
   * Returns a description of a target point with a given index.
   *
   * @param pointIndex the index
   * @return the description
   */
  protected String getTargetDescription(int pointIndex) {
  	return null;
  }

  /**
   * Determines if the given point index is autotrackable.
   *
   * @param pointIndex the points[] index
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable(int pointIndex) {
  	return true; // true by default--subclasses override
  }
  
  /**
   * Determines if at least one point in this track is autotrackable.
   *
   * @return true if autotrackable
   */
  protected boolean isAutoTrackable() {
  	return false; // false by default--subclasses override
  }
  
  /**
   * Returns true if this track contains no steps.
   *
   * @return <code>true</code> if this contains no steps
   */
  public boolean isEmpty() {
    Step[] array = steps.array;
    for (int n = 0; n < array.length; n++)
      if (array[n] != null) return false;
    return true;
  }
  
  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  public void setFontLevel(int level) {
  	Object[] objectsToSize = new Object[]
  			{tLabel, xLabel, yLabel, magLabel, angleLabel, stepLabel, tValueLabel, stepValueLabel,
  			tField, xField, yField, magField, angleField};
    FontSizer.setFonts(objectsToSize, level);
  }

  /**
   * Returns the DatasetManager.
   *
   * @param trackerPanel the tracker panel
   * @return the DatasetManager
   */
  public DatasetManager getData(TrackerPanel trackerPanel) {
    if (data == null) {
      data = new DatasetManager(true);
      data.setSorted(true);
    }
    if (refreshDataLater)
    	return data;
    if (!dataValid) {
    	dataValid = true;
      // refresh track data
    	refreshData(data, trackerPanel);
      // check for newly loaded dataFunctions
      if (dataProp != null) {
      	XMLControl[] children = dataProp.getChildControls();
      	outer: for (int i = 0; i < children.length; i++) {
      		// compare function name with existing datasets to avoid duplications
      		String name = children[i].getString("function_name"); //$NON-NLS-1$
      		for (Dataset next: data.getDatasets()) {
      			if (next instanceof DataFunction && next.getYColumnName().equals(name)) {
      				continue outer;
      			}
      		}
      		DataFunction f = new DataFunction(data);
      		children[i].loadObject(f);
        	f.setXColumnVisible(false);
       	  data.addDataset(f);
      	}
      	dataProp = null;
      }
      if (constantsLoadedFromXML != null) {
      	for (int i=0; i<constantsLoadedFromXML.length; i++) {
      		String name = (String)constantsLoadedFromXML[i][0];
      		double val = (Double)constantsLoadedFromXML[i][1];
      		String expression = (String)constantsLoadedFromXML[i][2];
      		data.setConstant(name, val, expression);
      	}
      	constantsLoadedFromXML = null;
      }
      // refresh dataFunctions
      ArrayList<Dataset> datasets = data.getDatasets();
      for (int i = 0; i < datasets.size(); i++) {
        if (datasets.get(i) instanceof DataFunction) {
        	((DataFunction)datasets.get(i)).refreshFunctionData();
        }    	
      }
    	DataTool tool = DataTool.getTool();
      if (trackerPanel!=null && tool.isVisible()
      		&& tool.getSelectedTab()!=null && tool.getSelectedTab().isInterestedIn(data)) {
      	tool.getSelectedTab().refreshData();
      }
    }
    return data;
  }

  /**
   * Refreshes the data in the specified DatasetManager. Subclasses should use this
   * method to refresh track-specific data sets.
   *
   * @param data the DatasetManager
   * @param trackerPanel the tracker panel
   */
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
  	/** empty block */
  }

  /**
   * Refreshes the data for a specified frame range. This default implementation
   * ignores the range arguments.
   *
   * @param data the DatasetManager
   * @param trackerPanel the tracker panel
   * @param startFrame the start frame
   * @param stepCount the step count
   */
  protected void refreshData(DatasetManager data, TrackerPanel trackerPanel,
  		int startFrame, int stepCount) {
  	refreshData(data, trackerPanel);
  }
  
  /**
   * Gets the name of a data variable. Index zero is the 
   * shared x-variable, indices 1-n+1 are the y-variables.
   *
   * @param index the dataset index
   * @return a String data name
   */
  public String getDataName(int index) {
  	if (index == 0) { // shared x-variable
      return data.getDataset(0).getXColumnName();  		
  	}
    if (index < data.getDatasets().size()+1) {
      return data.getDataset(index-1).getYColumnName();    	
    }
    return null;
  }

  /**
   * Gets the description of a data variable. Index zero is the 
   * shared x-variable, indices 1-n+1 are the y-variables.
   * Subclasses should override to provide correct descriptions.
   *
   * @param index the dataset index
   * @return a String data description
   */
  public String getDataDescription(int index) {
    return dataDescriptions!=null && index<dataDescriptions.length? dataDescriptions[index]: ""; //$NON-NLS-1$
  }
  
  /**
   * Gets the preferred order of data table columns.
   * 
   * @return a list of column indices in preferred order
   */
  public ArrayList<Integer> getPreferredDataOrder() {
    ArrayList<Integer> orderedData = new ArrayList<Integer>();
  	ArrayList<Dataset> datasets = data.getDatasets();
    if (preferredColumnOrder!=null) {
      // first add preferred indices
    	for (int i = 0; i < preferredColumnOrder.length; i++) {
    		if (!orderedData.contains(preferredColumnOrder[i]) // prevent duplicates
    				&& preferredColumnOrder[i]<datasets.size()) // prevent invalid indices
    			orderedData.add(preferredColumnOrder[i]);
      }
    }
  	// add indices not yet in array    	
		for (int i = 0; i<datasets.size(); i++) {
			if (!orderedData.contains(i)) {
  			orderedData.add(i);
			}
		}
		return orderedData;
  }

  /**
   * Gets the frame number associated with a specified variable and value.
   *
   * @param var the variable name
   * @param value the value
   * @return the frame number, or -1 if not found
   */
  public int getFrameForData(String var, double value) {
  	if (dataFrames.isEmpty() || data.getDatasets().isEmpty()) 
  		return -1;
		Dataset dataset = data.getDataset(0);
  	if (var.equals(dataset.getXColumnName())) {
  		double[] vals = dataset.getXPoints();
  		for (int i = 0; i < vals.length; i++) {
  			if (value == vals[i]) {
  				return i<dataFrames.size()? dataFrames.get(i).intValue(): -1;
  			}
  		}
  	}
  	else {
    	int n = data.getDatasetIndex(var);
    	if (n > -1) {
    		dataset = data.getDataset(n);
    		double[] vals = dataset.getYPoints();
    		for (int i = 0; i < vals.length; i++) {
    			if (value == vals[i]) {
    				return i<dataFrames.size()? dataFrames.get(i).intValue(): -1;
    			}
    		}
    	}
  	}
    return -1;
  }
  
  /**
   * Gets the text column names. 
   * 
   * @return list of column names.
   */
  public ArrayList<String> getTextColumnNames() {
  	return textColumnNames;
  }
  
  /**
   * Adds a new text column.
   * 
   * @param name the name
   * @return true if a new column was added
   */
  public boolean addTextColumn(String name) {
  	// only add new, non-null names
  	if (name==null || name.trim().equals("")) return false; //$NON-NLS-1$
  	name = name.trim();
  	for (String next: textColumnNames) {
  		if (next.equals(name)) return false;
  	}
		XMLControl control = new XMLControlElement(this);
  	textColumnNames.add(name);
  	textColumnEntries.put(name, new String[0]);
    Undo.postTrackEdit(this, control);
  	trackerPanel.changed = true;
  	this.firePropertyChange("text_column", null, name); //$NON-NLS-1$
  	return true;
  }

  /**
   * Removes a named text column.
   * 
   * @param name the name
   * @return true if the column was removed
   */
  public boolean removeTextColumn(String name) {
  	if (name==null) return false;
  	name = name.trim();
  	for (String next: textColumnNames) {
  		if (next.equals(name)) {
  			XMLControl control = new XMLControlElement(this);
  			textColumnEntries.remove(name);
		  	textColumnNames.remove(name);
        Undo.postTrackEdit(this, control);
		  	trackerPanel.changed = true;
		  	firePropertyChange("text_column", name, null); //$NON-NLS-1$
  			return true;
  		}
  	}
  	return false;
  }

  /**
   * Renames a text column.
   * 
   * @param name the existing name
   * @param newName the new name
   * @return true if renamed
   */
  public boolean renameTextColumn(String name, String newName) {
  	if (name==null) return false;
  	name = name.trim();
  	if (newName==null || newName.trim().equals("")) return false; //$NON-NLS-1$
  	newName = newName.trim();
  	for (String next: textColumnNames) {
  		if (next.equals(newName)) return false;
  	}
  	for (int i=0; i<textColumnNames.size(); i++) {
  		String next = textColumnNames.get(i);
  		if (name.equals(next)) {
		  	// found column to change
  			XMLControl control = new XMLControlElement(this);
  			textColumnNames.remove(name);
  			textColumnNames.add(i, newName);
  			String[] entries = textColumnEntries.remove(name);
  			textColumnEntries.put(newName, entries);
        Undo.postTrackEdit(this, control);
  		}
  	}
  	trackerPanel.changed = true;
  	this.firePropertyChange("text_column", name, newName); //$NON-NLS-1$
  	return true;
  }

  /**
   * Gets the entry in a text column for a specified frame. 
   * 
   * @param columnName the column name
   * @param frameNumber the frame number
   * @return the text entry (may be null)
   */
  public String getTextColumnEntry(String columnName, int frameNumber) {
  	// return null if frame number out of bounds
  	if (frameNumber<0) return null;
  	String[] entries = textColumnEntries.get(columnName);
  	// return null if text column or entry index not defined
  	if (entries==null) return null;
	  if (frameNumber>entries.length-1) return null;
	  return entries[frameNumber];
  }
  
  /**
   * Sets the text in a text column for a specified frame.
   * 
   * @param columnName the column name
   * @param frameNumber the frame number
   * @param text the text (may be null)
   * @return true if the text was changed
   */
  public boolean setTextColumnEntry(String columnName, int frameNumber, String text) {
  	if (isLocked()) return false;
  	// return if frame number out of bounds
  	if (frameNumber<0) return false;
  	String[] entries = textColumnEntries.get(columnName);
  	// return if text column not defined
  	if (entries==null) return false;
  	
	  if (text.trim().equals("")) text = null;  //$NON-NLS-1$
	  else text = text.trim();
	  
		XMLControl control = new XMLControlElement(this);
	  if (frameNumber>entries.length-1) {
  		// increase size of entries array
  		String[] newEntries = new String[frameNumber+1];
  		System.arraycopy(entries, 0, newEntries, 0, entries.length);
  		entries = newEntries;
  		textColumnEntries.put(columnName, entries);
  	}
	  
	  String prev = entries[frameNumber];
	  if (prev==text || (prev!=null && prev.equals(text))) return false;
	  // change text entry and fire property change
	  entries[frameNumber] = text;
    Undo.postTrackEdit(this, control);
  	trackerPanel.changed = true;
	  firePropertyChange("text_column", null, null); //$NON-NLS-1$
	  return true;
  }
  
  /**
   * Prepares menu items and returns a new menu.
   * Subclasses should override this method and add track-specific menu items.
   *
   * @param trackerPanel the tracker panel
   * @return a menu
   */
  public JMenu getMenu(final TrackerPanel trackerPanel) {
    // prepare menu items
    visibleItem.setText(TrackerRes.getString("TTrack.MenuItem.Visible")); //$NON-NLS-1$
    trailVisibleItem.setText(TrackerRes.getString("TTrack.MenuItem.TrailVisible")); //$NON-NLS-1$
    autoAdvanceItem.setText(TrackerRes.getString("TTrack.MenuItem.Autostep")); //$NON-NLS-1$
    markByDefaultItem.setText(TrackerRes.getString("TTrack.MenuItem.MarkByDefault")); //$NON-NLS-1$
    lockedItem.setText(TrackerRes.getString("TTrack.MenuItem.Locked")); //$NON-NLS-1$
    deleteTrackItem.setText(TrackerRes.getString("TTrack.MenuItem.Delete")); //$NON-NLS-1$
    deleteStepItem.setText(TrackerRes.getString("TTrack.MenuItem.DeletePoint")); //$NON-NLS-1$
    clearStepsItem.setText(TrackerRes.getString("TTrack.MenuItem.ClearSteps")); //$NON-NLS-1$    
    colorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
    nameItem.setText(TrackerRes.getString("TTrack.MenuItem.Name")); //$NON-NLS-1$
    footprintMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
    descriptionItem.setText(TrackerRes.getString("TTrack.MenuItem.Description")); //$NON-NLS-1$
    dataBuilderItem.setText(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
    visibleItem.setSelected(isVisible());
    lockedItem.setSelected(isLocked());
    trailVisibleItem.setSelected(isTrailVisible());
    markByDefaultItem.setSelected(isMarkByDefault());
    autoAdvanceItem.setSelected(isAutoAdvance());
    lockedItem.setEnabled(true);
    boolean cantDeleteSteps = isLocked() || isDependent();
    TPoint p = trackerPanel.getSelectedPoint();
    Step step = getStep(p, trackerPanel);

    deleteStepItem.setEnabled(!cantDeleteSteps && step!=null);
    clearStepsItem.setEnabled(!cantDeleteSteps);
    deleteTrackItem.setEnabled(!(isLocked() && !isDependent()));
    nameItem.setEnabled(!(isLocked() && !isDependent()));
    footprintMenu.removeAll();
    Footprint[] fp = getFootprints();
    JMenuItem item;
    for (int i = 0; i < fp.length; i++) {
      item = new JMenuItem(fp[i].getDisplayName(), fp[i].getIcon(21, 16));
      item.setActionCommand(fp[i].getName());
      if (fp[i] instanceof CircleFootprint) {
      	item.setText(fp[i].getDisplayName()+"..."); //$NON-NLS-1$
      	item.addActionListener(circleFootprintListener);
      }
      else {
      	item.addActionListener(footprintListener);
      }
      if (fp[i]==footprint) {
        item.setBorder(BorderFactory.createLineBorder(item.getBackground().darker()));
      }
      footprintMenu.add(item);
    }
    // return a new menu every time
    menu = new JMenu(getName());
    menu.setIcon(getFootprint().getIcon(21, 16));
    // add name and description items
    if (trackerPanel.isEnabled("track.name") || //$NON-NLS-1$
        trackerPanel.isEnabled("track.description")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
      if (trackerPanel.isEnabled("track.name")) //$NON-NLS-1$
        menu.add(nameItem);
      if (trackerPanel.isEnabled("track.description")) //$NON-NLS-1$
        menu.add(descriptionItem);
    }
    // add color and footprint items
    if (trackerPanel.isEnabled("track.color") || //$NON-NLS-1$
        trackerPanel.isEnabled("track.footprint")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
      if (trackerPanel.isEnabled("track.color")) //$NON-NLS-1$
        menu.add(colorItem);
      if (trackerPanel.isEnabled("track.footprint")) //$NON-NLS-1$
        menu.add(footprintMenu);
    }
    // add visible, trail and locked items
    if (trackerPanel.isEnabled("track.visible") || //$NON-NLS-1$
        trackerPanel.isEnabled("track.locked")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
      if (trackerPanel.isEnabled("track.visible")) //$NON-NLS-1$
        menu.add(visibleItem);
      if (trackerPanel.isEnabled("track.locked")) //$NON-NLS-1$
        menu.add(lockedItem);
    }
    // add dataBuilder item if viewable and enabled
    if (this.isViewable() && trackerPanel.isEnabled("data.builder")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
      menu.add(dataBuilderItem);
    	
    }
    // add clear steps and delete items
    if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
      if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount()-1) != null)
        menu.addSeparator();
      menu.add(deleteTrackItem);
    }
    return menu;
  }

  /**
   * Returns an empty list of track-related toolbar components. Subclasses
   * should override this method and add track-specific components.
   *
   * @param trackerPanel the tracker panel
   * @return a collection of components
   */
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
    toolbarTrackComponents.clear();
    return toolbarTrackComponents;
  }

  /**
   * Returns an empty list of point-related toolbar components. Subclasses
   * should override this method and add point-specific components.
   *
   * @param trackerPanel the tracker panel
   * @param point the TPoint
   * @return a list of components
   */
  public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel,
                                             TPoint point) {
    toolbarPointComponents.clear();
    stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
    // put step time into tField
    Step step = getStep(point, trackerPanel);
    VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    if (step != null && clip.includesFrame(step.getFrameNumber())) {
      int n = clip.frameToStep(step.getFrameNumber());
    	stepValueLabel.setText("" + n); //$NON-NLS-1$
      double t = trackerPanel.getPlayer().getStepTime(n) / 1000;
      if (t >= 0) {
        tField.setValue(t);
      }
    }
    // set tooltip for angle field
    angleField.setToolTipText(angleField.getConversionFactor()==1?
    		TrackerRes.getString("TTrack.AngleField.Radians.Tooltip"): //$NON-NLS-1$
    		TrackerRes.getString("TTrack.AngleField.Degrees.Tooltip")); //$NON-NLS-1$
    return toolbarPointComponents;
  }

  /**
   * Erases all steps on all panels.
   */
  public void erase() {
    Step[] stepArray = steps.array;
    for (int j = 0; j < stepArray.length; j++)
      if (stepArray[j] != null) stepArray[j].erase();
    if (trackerPanel!=null && trackerPanel.autoTracker!=null) {
    	AutoTracker autoTracker = trackerPanel.getAutoTracker();
    	if (autoTracker.getWizard().isVisible()
    			&& autoTracker.getTrack()==this) {
    		autoTracker.erase();
    	}
    }
  }

  /**
   * Remarks all steps on all panels.
   */
  public void remark() {
    Step[] stepArray = steps.array;
    for (int j = 0; j < stepArray.length; j++)
      if (stepArray[j] != null) stepArray[j].remark();
  }

  /**
   * Repaints all steps on all panels.
   */
  public void repaint() {
    remark();
    for (TrackerPanel next: panels) {
    	next.repaintDirtyRegion();
    }
  }

  /**
   * Erases all steps on the specified panel.
   *
   * @param trackerPanel the tracker panel
   */
  public void erase(TrackerPanel trackerPanel) {
    Step[] stepArray = steps.array;
    for (int j = 0; j < stepArray.length; j++)
      if (stepArray[j] != null) stepArray[j].erase(trackerPanel);
    if (trackerPanel.autoTracker!=null) {
	  	AutoTracker autoTracker = trackerPanel.getAutoTracker();
	  	if (autoTracker.getWizard().isVisible()
	  			&& autoTracker.getTrack()==this) {
	  		autoTracker.erase();
	  	}
    }
  }

  /**
   * Remarks all steps on the specified panel.
   *
   * @param trackerPanel the tracker panel
   */
  public void remark(TrackerPanel trackerPanel) {
    Step[] stepArray = steps.array;
    for (int j = 0; j < stepArray.length; j++)
      if (stepArray[j] != null) stepArray[j].remark(trackerPanel);
  }

  /**
   * Repaints all steps on the specified panel.
   *
   * @param trackerPanel the tracker panel
   */
  public void repaint(TrackerPanel trackerPanel) {
    remark(trackerPanel);
    trackerPanel.repaintDirtyRegion();
  }

  /**
   * Repaints the specified step on all panels. This should be used
   * instead of the Step.repaint() method to paint a new step on all
   * panels for the first time, since a new step does not know what
   * panels it is drawn on whereas the track does.
   *
   * @param step the step
   */
  public void repaint(Step step) {
    Iterator<TrackerPanel> it = panels.iterator();
    while (it.hasNext())
      step.repaint(it.next());
  }

  /**
   * Draws the steps on the tracker panel.
   *
   * @param panel the drawing panel requesting the drawing
   * @param _g the graphics context on which to draw
   */
  public void draw(DrawingPanel panel, Graphics _g) {
    if (!(panel instanceof TrackerPanel) || !visible) return;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    panels.add(trackerPanel);   // keep a list of tracker panels
    Graphics2D g = (Graphics2D)_g;
    int n = trackerPanel.getFrameNumber();
    int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
    if (trailVisible) {
    	boolean shortTrail = getTrailLength() > 0;
      Step[] stepArray = steps.array;
      for (int frame = 0; frame < stepArray.length; frame++) {
      	if (shortTrail && (n-frame > (getTrailLength()-1)*stepSize || frame>n))
      		continue;
        if (stepArray[frame] != null &&
          trackerPanel.getPlayer().getVideoClip().includesFrame(frame))
          stepArray[frame].draw(trackerPanel, g);
      }
    }
    else {
      Step step = getStep(n);
      if (step != null)
        step.draw(trackerPanel, g);
    }
  }
  
  /**
   * Finds the interactive drawable object located at the specified
   * pixel position.
   *
   * @param panel the drawing panel
   * @param xpix the x pixel position on the panel
   * @param ypix the y pixel position on the panel
   * @return the first step TPoint that is hit
   */
  public Interactive findInteractive(
         DrawingPanel panel, int xpix, int ypix) {
    if (!(panel instanceof TrackerPanel) || !visible) return null;
    TrackerPanel trackerPanel = (TrackerPanel)panel;
    Interactive iad = null;
    int n = trackerPanel.getFrameNumber();
    int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
    if (trailVisible) {
    	boolean shortTrail = getTrailLength() > 0;
      Step[] stepArray = steps.array;
      for (int frame = 0; frame < stepArray.length; frame++) {
      	if (shortTrail && (n-frame > (getTrailLength()-1)*stepSize || frame>n))
      		continue;
        if (stepArray[frame] != null &&
          trackerPanel.getPlayer().getVideoClip().includesFrame(frame)) {
          iad = stepArray[frame].findInteractive(trackerPanel, xpix, ypix);
          if (iad != null) return iad;
        }
      }
    }
    else {
      Step step = getStep(n);
      if (step != null &&
        trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
        iad = step.findInteractive(trackerPanel, xpix, ypix);
        if (iad != null) return iad;
      }
    }
    return null;
  }


  /**
   * Gets x. Tracks have no meaningful position, so returns 0.
   *
   * @return 0
   */
  public double getX () {
    return 0;
  }

  /**
   * Gets y. Tracks have no meaningful position, so returns 0.
   *
   * @return 0
   */
  public double getY () {
    return 0;
  }

  /**
   * Empty setX method.
   *
   * @param x the x position
   */
  public void setX(double x) {/** implemented by subclasses */}

  /**
   * Empty setY method.
   *
   * @param y the y position
   */
  public void setY(double y) {/** implemented by subclasses */}

  /**
   * Empty setXY method.
   *
   * @param x the x position
   * @param y the y position
   */
  public void setXY(double x, double y) {/** implemented by subclasses */}

  /**
   * Sets whether this responds to mouse hits.
   *
   * @param enabled <code>true</code> if this responds to mouse hits.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Gets whether this responds to mouse hits.
   *
   * @return <code>true</code> if this responds to mouse hits.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Reports whether information is available to set min/max values.
   *
   * @return <code>false</code> since TTrack knows only its image coordinates
   */
  public boolean isMeasured() {
    return !isEmpty();
  }

  /**
   * Gets the minimum x needed to draw this object.
   *
   * @return 0
   */
  public double getXMin() {
    return getX();
  }

  /**
   * Gets the maximum x needed to draw this object.
   *
   * @return 0
   */
  public double getXMax() {
    return getX();
  }

  /**
   * Gets the minimum y needed to draw this object.
   *
   * @return 0
   */
  public double getYMin() {
    return getY();
  }

  /**
   * Gets the maximum y needed to draw this object.
   *
   * @return 0
   */
  public double getYMax() {
    return getY();
  }

  /**
   * Gets the minimum world x needed to draw this object on the specified TrackerPanel.
   *
   * @param panel the TrackerPanel drawing this track
   * @return the minimum world x
   */
  public double getXMin(TrackerPanel panel) {
    double[] bounds = getWorldBounds(panel);
    return bounds[2];
  }

  /**
   * Gets the maximum world x needed to draw this object on the specified TrackerPanel.
   *
   * @param panel the TrackerPanel drawing this track
   * @return the maximum x of any step's footprint
   */
  public double getXMax(TrackerPanel panel) {
    double[] bounds = getWorldBounds(panel);
    return bounds[0];
  }

  /**
   * Gets the minimum world y needed to draw this object on the specified TrackerPanel.
   *
   * @param panel the TrackerPanel drawing this track
   * @return the minimum y of any step's footprint
   */
  public double getYMin(TrackerPanel panel) {
    double[] bounds = getWorldBounds(panel);
    return bounds[3];
  }

  /**
   * Gets the maximum world y needed to draw this object on the specified TrackerPanel.
   *
   * @param panel the TrackerPanel drawing this track
   * @return the maximum y of any step's footprint
   */
  public double getYMax(TrackerPanel panel) {
    double[] bounds = getWorldBounds(panel);
    return bounds[1];
  }
  
  /**
   * Sets a user property of the track.
   *
   * @param name the name of the property
   * @param value the value of the property
   */
  public void setProperty(String name, Object value) {
    properties.put(name, value);
  }

  /**
   * Gets a user property of the track. May return null.
   *
   * @param name the name of the property
   * @return the value of the property
   */
  public Object getProperty(String name) {
    return properties.get(name);
  }

  /**
   * Gets a collection of user property names for the track.
   *
   * @return a collection of property names
   */
  public Collection<String> getPropertyNames() {
    return properties.keySet();
  }

  /**
   * Responds to property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (e.getSource() instanceof TrackerPanel) {
      TrackerPanel trackerPanel = (TrackerPanel)e.getSource();
      if (name.equals("transform") //$NON-NLS-1$
      		|| name.equals("coords")) { //$NON-NLS-1$
      	dataValid = false;
        erase();
        trackerPanel.repaint();
      }
      else if (name.equals("magnification")) { //$NON-NLS-1$
        erase();
        trackerPanel.repaint();
      }
      else if (name.equals("imagespace")) //$NON-NLS-1$
        erase(trackerPanel);
      else if (name.equals("data")) //$NON-NLS-1$
      	dataValid = false;
      else if (name.equals("radian_angles")) { // angle format has changed //$NON-NLS-1$
      	setAnglesInRadians((Boolean)e.getNewValue());    	
      }
    }
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(
    PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Fires a property change event.
   *
   * @param name the name of the property
   * @param oldVal the old value of the property
   * @param newVal the new value of the property
   */
  public void firePropertyChange(String name, Object oldVal, Object newVal) {
    support.firePropertyChange(name, oldVal, newVal);
  }

  /**
   * Adds a PropertyChangeListener for a specified property.
   *
   * @param property the name of the property of interest to the listener
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(
    String property, PropertyChangeListener listener) {
    support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param listener the listener requesting removal
   */
  public void removePropertyChangeListener(
    PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener for a specified property.
   *
   * @param property the name of the property
   * @param listener the listener to remove
   */
  public void removePropertyChangeListener(
    String property, PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
  }

  //___________________________ protected methods ____________________________

  /**
   * Identifies the controlling TrackerPanel for this track (by default,
   * the first TrackerPanel that adds this track to its drawables).
   *
   * @param panel the TrackerPanel
   */
  protected void setTrackerPanel(TrackerPanel panel) {
	  trackerPanel = panel;
  }

  /**
   * Gets the world bounds of this track on the specified TrackerPanel.
   *
   * @param panel the TrackerPanel
   * @return a double[] containing xMax, yMax, xMin, yMin
   */
  protected double[] getWorldBounds(TrackerPanel panel) {
    double[] bounds = worldBounds.get(panel);
    //if (bounds != null) return bounds;
    // make a rectangle containing the world positions of the TPoints in this track
    // then convert it into world units
    bounds = new double[4];
    Rectangle2D rect = new Rectangle2D.Double();
    Step[] array = steps.array;
    for (int n = 0; n < array.length; n++) {
      if (array[n] != null) {
        TPoint[] points = array[n].getPoints();
        for (int i = 0; i < points.length; i++) {
          if (points[i] == null) continue;
          rect.add(points[i].getWorldPosition(panel));
        }
      }
    }
    // increase bounds to make room for footprint shapes
    bounds[0] = rect.getX() + 1.05 * rect.getWidth();  // xMax
    bounds[1] = rect.getY() + 1.05 * rect.getHeight(); // yMax
    bounds[2] = rect.getX() - 0.05 * rect.getWidth();  // xMin
    bounds[3] = rect.getY() - 0.05 * rect.getHeight(); // yMin
    worldBounds.put(panel, bounds);
    return bounds;
  }

  /**
   * Sets the display format for angles.
   *
   * @param radians <code>true</code> for radians, false for degrees
   */
  protected void setAnglesInRadians(boolean radians) {
    angleField.setUnits(radians? null: Tracker.DEGREES);
    angleField.setDecimalPlaces(radians? 3: 1);
    angleField.setConversionFactor(radians? 1.0: 180/Math.PI);
    angleField.setToolTipText(radians?
    		TrackerRes.getString("TTrack.AngleField.Radians.Tooltip"): //$NON-NLS-1$
    		TrackerRes.getString("TTrack.AngleField.Degrees.Tooltip")); //$NON-NLS-1$
  }

  /**
   * Cleans up associated resources when this track is deleted or cleared.
   */
  protected void cleanup() {
  }
  
  /**
   * Sets the marking flag. Flag should be true when ready to be marked by user.
   * 
   * @param marking true when marking
   */
  protected void setMarking(boolean marking) {
  	isMarking  = marking;
  }
  
  /**
   * Gets the cursor used for marking new steps.
   * 
   * @param e the input event triggering this call (may be null)
   * @return the marking cursor
   */
  protected Cursor getMarkingCursor(InputEvent e) {
  	if (e!=null && TMouseHandler.isAutoTrackTrigger(e) && trackerPanel.getVideo()!=null && isAutoTrackable(getTargetIndex())) {
  		Step step = getStep(trackerPanel.getFrameNumber());
  		if (step==null || step.getPoints()[step.getPoints().length-1]==null) {
  			return TMouseHandler.autoTrackMarkCursor;
  		}
  		
  		if (this instanceof CoordAxes || this instanceof TapeMeasure || this instanceof Protractor) {
  			AutoTracker autoTracker = trackerPanel.getAutoTracker();
	    	if (autoTracker.getTrack()==null || autoTracker.getTrack()==this) {
	    		int n = trackerPanel.getFrameNumber();
	    		AutoTracker.KeyFrame key = autoTracker.getFrame(n).getKeyFrame();
	    		if (key==null)
	    			return TMouseHandler.autoTrackMarkCursor;
	    	}  			
  		} 
  		
			return TMouseHandler.autoTrackCursor;
  	}
  	
  	return TMouseHandler.markPointCursor;
  }
  
  protected void createWarningDialog() {
    if (skippedStepWarningDialog==null 
    		&& trackerPanel!=null
    		&& trackerPanel.getTFrame()!=null) {
    	skippedStepWarningDialog = new JDialog(trackerPanel.getTFrame(), true);
    	skippedStepWarningDialog.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
        	skippedStepWarningOn = !skippedStepWarningCheckbox.isSelected();
        }
    	});
    	JPanel contentPane = new JPanel(new BorderLayout());
    	skippedStepWarningDialog.setContentPane(contentPane);
    	skippedStepWarningTextpane = new JTextPane();
    	skippedStepWarningTextpane.setEditable(false);
    	skippedStepWarningTextpane.setOpaque(false);
    	skippedStepWarningTextpane.setPreferredSize(new Dimension(400, 120));
    	skippedStepWarningTextpane.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
    	skippedStepWarningTextpane.setContentType("text"); //$NON-NLS-1$
    	skippedStepWarningTextpane.setFont(new JLabel().getFont());
    	contentPane.add(skippedStepWarningTextpane, BorderLayout.CENTER);
    	skippedStepWarningCheckbox = new JCheckBox();
    	skippedStepWarningCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
    	closeButton = new JButton();
    	closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	skippedStepWarningOn = !skippedStepWarningCheckbox.isSelected();
        	skippedStepWarningDialog.setVisible(false);
        }
      });
    	JPanel buttonbar = new JPanel();
    	buttonbar.add(skippedStepWarningCheckbox);
    	buttonbar.add(closeButton);
    	contentPane.add(buttonbar, BorderLayout.SOUTH);
    }
  }
  
  protected JDialog getStepSizeWarningDialog() {
  	createWarningDialog();
    if (skippedStepWarningDialog==null)
    	return null;
    
    skippedStepWarningDialog.setTitle(TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Title")); //$NON-NLS-1$
    String m1 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message1"); //$NON-NLS-1$
  	String m2 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message2"); //$NON-NLS-1$
  	String m3 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message3"); //$NON-NLS-1$
  	skippedStepWarningTextpane.setText(m1+"  "+m2+"  "+m3); //$NON-NLS-1$ //$NON-NLS-2$
  	skippedStepWarningCheckbox.setText(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Checkbox")); //$NON-NLS-1$
  	closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
  	skippedStepWarningDialog.pack();
  	// center on screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - skippedStepWarningDialog.getBounds().width) / 2;
    int y = (dim.height - skippedStepWarningDialog.getBounds().height) / 2;
    skippedStepWarningDialog.setLocation(x, y);
    
  	return skippedStepWarningDialog;
  }
  
  protected JDialog getSkippedStepWarningDialog() {
  	createWarningDialog();
    if (skippedStepWarningDialog==null)
    	return null;
    
    skippedStepWarningDialog.setTitle(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Title")); //$NON-NLS-1$
    String m1 = TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Message1"); //$NON-NLS-1$
  	String m3 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message3"); //$NON-NLS-1$
  	skippedStepWarningTextpane.setText(m1+"  "+m3); //$NON-NLS-1$
  	skippedStepWarningCheckbox.setText(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Checkbox")); //$NON-NLS-1$
  	closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
  	FontSizer.setFonts(skippedStepWarningDialog, FontSizer.getLevel());
  	skippedStepWarningDialog.pack();
  	// center on screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - skippedStepWarningDialog.getBounds().width) / 2;
    int y = (dim.height - skippedStepWarningDialog.getBounds().height) / 2;
    skippedStepWarningDialog.setLocation(x, y);
    
  	return skippedStepWarningDialog;
  }
  
  protected Dataset convertTextToDataColumn(String textColumnName) {
  	if (textColumnName==null || trackerPanel==null) return null;
  	// find named text column
  	String[] entries = this.textColumnEntries.get(textColumnName);
  	if (entries!=null && entries.length>0) {
  		DatasetManager data = getData(trackerPanel);
  		double[] x = data.getXPoints(0);
  		double[] values = new double[x.length];
  		for (int i=0; i<values.length; i++) {
  			if (entries.length>i) {
  				if (entries[i]==null) {
  					values[i] = Double.NaN;
  				}
  				else try {
  					values[i] = Double.parseDouble(entries[i]);
  				} catch(Exception ex) {
  					return null;
  				}
  			}
  			else values[i] = Double.NaN;
  		}
  		Dataset dataset = new Dataset();
  		dataset.append(x, values);
  		dataset.setXYColumnNames(data.getDataset(0).getXColumnName(), textColumnName, getName());
  		dataset.setMarkerColor(getColor());
  		return dataset;
  	}
  	return null;
  }
  
//______________________ inner StepArray class _______________________

  protected class StepArray {

    // instance fields
    protected int length = 5;
    protected Step[] array = new Step[length];
    private boolean autofill = false;
    protected int delta = 5;

    /**
     * Constructs a default StepArray.
     */
    public StepArray() {/** empty block */}

    /**
     * Constructs an autofill StepArray and fills the array with clones
     * of the specified step.
     *
     * @param step the step to fill the array with
     */
    public StepArray(Step step) {
      autofill = true;
      step.n = 0;
      array[0] = step;
      fill(step);
    }

    /**
     * Constructs an autofill StepArray and fills the array with clones
     * of the specified step.
     *
     * @param step the step to fill the array with
     * @param increment the array sizing increment
     */
    public StepArray(Step step, int increment) {
      autofill = true;
      step.n = 0;
      array[0] = step;
      length = increment;
      delta = increment;
      fill(step);
    }

    /**
     * Gets the step at the specified index. May return null.
     *
     * @param n the array index
     * @return the step
     */
    public Step getStep(int n) {    	
      if (n >= length) setLength(n + delta);
      return array[n];
    }

    /**
     * Sets the step at the specified index. Accepts a null step argument
     * for non-autofill arrays.
     *
     * @param n the array index
     * @param step the new step
     */
    public void setStep(int n, Step step) {
      if (autofill && step == null) return;
      if (n >= length) setLength(n + delta);
      synchronized(array) {
        array[n] = step;
      }
    }

    /**
     * Determines if this step array contains the specified step.
     *
     * @param step the new step
     * @return <code>true</code> if this contains the step
     */
    public boolean contains(Step step) {
      synchronized(array) {
        for (int i = 0; i < array.length; i++)
          if (array[i] == step) return true;
      }
      return false;
    }

    /**
     * Sets the length of the array.
     *
     * @param len the new length of the array
     */
    public void setLength(int len) {
      synchronized(array) {
        Step[] newArray = new Step[len];
        System.arraycopy(array, 0, newArray, 0, Math.min(len, length));
        array = newArray;
        if (len > length && autofill) {
          Step step = array[length - 1];
          length = len;
          fill(step);
        } else length = len;
      }      
    }
    
    /**
     * Determines if this is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
      synchronized(array) {
        for (int i = 0; i < array.length; i++)
          if (array[i]!=null) return false;
      }
    	return true;
    }

    /**
     * Determines if the specified step is preceded by a lower index step.
     * 
     * @param n the step index
     * @return true if the step is preceded
     */
    public boolean isPreceded(int n) {
      synchronized(array) {
      	int k = Math.min(n, array.length);
        for (int i = 0; i < k; i++)
          if (array[i]!=null) return true;
      }
    	return false;
    }
    
    public boolean isAutofill() {
    	return autofill;
    }

    //__________________________ private methods _________________________

    /**
     * Replaces null elements of the the array with clones of the
     * specified step.
     *
     * @param step the step to clone
     */
    private void fill(Step step) {
      for (int n = 0; n < length; n++)
        if (array[n] == null) {
          Step clone = (Step)step.clone();
          clone.n = n;
          array[n] = clone;
        }
    }
  }

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {

    /**
     * Saves an object's data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      TTrack track = (TTrack)obj;
      // name
      control.setValue("name", track.getName()); //$NON-NLS-1$
      // description
      if (!track.description.equals(""))  //$NON-NLS-1$
      	control.setValue("description", track.description); //$NON-NLS-1$
      // color
      control.setValue("color", track.getColor()); //$NON-NLS-1$
      // footprint name
      Footprint fp = track.getFootprint();
      String s = fp.getName();
      if (fp instanceof CircleFootprint) {
      	CircleFootprint cfp = (CircleFootprint)fp;
      	s+="#"+cfp.getProperties(); //$NON-NLS-1$
      }
      control.setValue("footprint", s); //$NON-NLS-1$
      // visible
      control.setValue("visible", track.isVisible()); //$NON-NLS-1$
      // trail
      control.setValue("trail", track.isTrailVisible()); //$NON-NLS-1$
      // locked
      if (track.isLocked()) control.setValue("locked", track.isLocked()); //$NON-NLS-1$
      // text columns
      if (!track.getTextColumnNames().isEmpty()) {
      	String[] names = track.getTextColumnNames().toArray(new String[0]);
      	control.setValue("text_column_names", names); //$NON-NLS-1$
      	String[][] entries = new String[names.length][];
      	for (int i=0; i< names.length; i++) {
      		entries[i] = track.textColumnEntries.get(names[i]);
      	}
      	control.setValue("text_column_entries", entries); //$NON-NLS-1$
      }
      // data functions
      if (track.trackerPanel != null) {
	      DatasetManager data = track.getData(track.trackerPanel);
	    	Iterator<Dataset> it = data.getDatasets().iterator();
	    	ArrayList<Dataset> list = new ArrayList<Dataset>();
	    	while (it.hasNext()) {
	    		Dataset dataset = it.next();
	    		if (dataset instanceof DataFunction) {
	    			list.add(dataset);
	    		}
	    	}
	    	if (!list.isEmpty()) {
		    	String[] names = data.getConstantNames();
		    	if (names.length>0) {
		    		Object[][] paramArray = new Object[names.length][3];
		    		int i = 0;
		    		for (String key: names) {
		    			paramArray[i][0] = key;
		    			paramArray[i][1] = data.getConstantValue(key);
		    			paramArray[i][2] = data.getConstantExpression(key);
		    			i++;
		    		}
		    		control.setValue("constants", paramArray); //$NON-NLS-1$
		    	}
	    		DataFunction[] f = list.toArray(new DataFunction[0]);
	    		control.setValue("data_functions", f); //$NON-NLS-1$
	    	}
      }
    }

    /**
     * Creates a new object.
     *
     * @param control the XMLControl with the object data
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
      TTrack track = (TTrack) obj;
      boolean locked = track.isLocked();
      track.setLocked(false);
      // name
      track.setName(control.getString("name")); //$NON-NLS-1$
      // description
      track.setDescription(control.getString("description")); //$NON-NLS-1$
      // color
      track.setColor((Color)control.getObject("color")); //$NON-NLS-1$
      // footprint
      String s = control.getString("footprint"); //$NON-NLS-1$
      if (s != null) track.setFootprint(s.trim());
      // visible and trail
      track.setVisible(control.getBoolean("visible")); //$NON-NLS-1$
      track.setTrailVisible(control.getBoolean("trail")); //$NON-NLS-1$
      // text columns
  		track.textColumnNames.clear();
  		track.textColumnEntries.clear();
      String[] columnNames = (String[])control.getObject("text_column_names"); //$NON-NLS-1$
      if (columnNames!=null) {
      	String[][] columnEntries = (String[][])control.getObject("text_column_entries"); //$NON-NLS-1$
      	if (columnEntries!=null) {
      		for (int i=0; i< columnNames.length; i++) {
      			track.textColumnNames.add(columnNames[i]);
      			track.textColumnEntries.put(columnNames[i], columnEntries[i]);
      		}
      	}
      }
      // data functions and constants
      track.constantsLoadedFromXML = (Object[][])control.getObject("constants"); //$NON-NLS-1$
      Iterator<Object> it = control.getPropertyContent().iterator();
      while (it.hasNext()) {
      	XMLProperty prop = (XMLProperty)it.next();
      	if (prop.getPropertyName().equals("data_functions")) { //$NON-NLS-1$
          track.dataProp = prop;
      	}
      }
      // locked
      track.setLocked(locked || control.getBoolean("locked")); //$NON-NLS-1$
      return obj;
    }
  }

  /**
   * Reports whether or not the specified step is visible.
   *
   * @param step the step
   * @param trackerPanel the tracker panel
   * @return <code>true</code> if the step is visible
   */
  public boolean isStepVisible(Step step, TrackerPanel trackerPanel) {
    if (!isVisible()) return false;
    int n =  step.getFrameNumber();
    return trackerPanel.getPlayer().getVideoClip().includesFrame(n) &&
          (trailVisible || trackerPanel.getFrameNumber() == n);
  }

}

