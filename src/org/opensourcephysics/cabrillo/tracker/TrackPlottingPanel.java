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
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.opensourcephysics.display.*;
import org.opensourcephysics.tools.*;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.axes.*;
import org.opensourcephysics.media.core.*;

/**
 * This is a plotting panel for a track
 *
 * @author Douglas Brown
 */
public class TrackPlottingPanel extends PlottingPanel implements Tool {
	
	// static fields
	protected static final String DEFINED_AS = ": "; //$NON-NLS-1$

	// instance fields
  protected TrackerPanel trackerPanel;
  protected int trackID;
  protected DatasetManager data;
  protected HighlightableDataset dataset = new HighlightableDataset();
  protected ArrayList<TTrack> guests = new ArrayList<TTrack>();
  protected HashMap<TTrack, HighlightableDataset> guestDatasets 
  		= new HashMap<TTrack, HighlightableDataset>();
  protected JRadioButtonMenuItem[] xChoices, yChoices;
  protected ButtonGroup xGroup, yGroup;
  protected int xIndex = -1, yIndex = 0;
  protected JPopupMenu xPopup, yPopup, popup;
  protected Action dataFunctionListener, guestListener;
  protected JMenuItem copyImageItem, dataBuilderItem, dataToolItem;
  protected JMenuItem showXZeroItem, showYZeroItem;
  protected JMenuItem selectPointsItem, deselectPointsItem;
  protected JMenuItem algorithmItem, printItem, helpItem, mergeYScalesItem;
  protected JCheckBoxMenuItem linesItem, pointsItem;
  protected JMenuItem guestsItem;
  protected String xLabel, yLabel, title;
  protected TreeSet<Integer> highlightIndices = new TreeSet<Integer>(); // indices of highlighted points
  protected ItemListener xListener, yListener;
  protected PlotTrackView plotTrackView;
  protected boolean isCustom;
  protected Font font = new JTextField().getFont();
  protected Rectangle hitRect = new Rectangle(24, 24);
  protected ClickableAxes plotAxes;
  protected boolean isZoomMode;
  protected PlotMouseListener mouseListener;
  protected PropertyChangeListener playerListener;
  protected Step clickedStep;
  protected TCoordinateStringBuilder coordStringBuilder;

  /**
   * Constructs a TrackPlottingPanel for a track.
   *
   * @param track the track
   * @param data the track's data
   */
  public TrackPlottingPanel(TTrack track, DatasetManager data) {
    super(" ", " ", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    trackerPanel = track.trackerPanel;
    trackID = track.getID();
    this.data = data;
    dataset.setConnected(true);
    dataset.setMarkerShape(Dataset.SQUARE);
    // set new CoordinateStringBuilder
    coordStringBuilder = new TCoordinateStringBuilder();
    setCoordinateStringBuilder(coordStringBuilder);

    Font font = new JTextField().getFont();
    trMessageBox.setMessageFont(font);
    tlMessageBox.setMessageFont(font);
    brMessageBox.setMessageFont(font);
    blMessageBox.setMessageFont(font);
    
    // make listeners for the button states
    xListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
      	if (e.getStateChange() == ItemEvent.SELECTED) {
	        JMenuItem item = (JMenuItem)e.getSource();
	        setXVariable(item.getText());
	        plotData();
	        isCustom = true;
          trackerPanel.changed = true;
      	}
      }
    };
    yListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
      	if (e.getStateChange() == ItemEvent.SELECTED) {
          JMenuItem item = (JMenuItem)e.getSource();
          setYVariable(item.getText());
          plotData();
          isCustom = true;
          trackerPanel.changed = true;
      	}
      }
    };
		playerListener = new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent e) {
		  	if (clickedStep == null) return;
        TPoint pt = clickedStep.getDefaultPoint();
      	plotTrackView.trackerPanel.setSelectedPoint(pt);
      	if (pt!=null) {
      		pt.showCoordinates(plotTrackView.trackerPanel);
      	}
        clickedStep = null;
        repaint();
		  }
		};
    // create radio buttons and popups to set x and y variables
    createVarChoices();
    // create clickable axes
    plotAxes = new ClickableAxes(this);
    setAxes(plotAxes);
    
    // add plotMouseListener
    mouseListener = new PlotMouseListener();
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
  			if (!plotTrackView.trackerPanel.getPlayer().isEnabled()) return;
      	switch (e.getKeyCode()) {
      		case KeyEvent.VK_PAGE_UP:      			
      			if (e.isShiftDown()) {
      				int n = plotTrackView.trackerPanel.getPlayer().getStepNumber()-5;
      				plotTrackView.trackerPanel.getPlayer().setStepNumber(n);
      			}
      			else plotTrackView.trackerPanel.getPlayer().back();
      			break;
      		case KeyEvent.VK_PAGE_DOWN:
      			if (e.isShiftDown()) {
      				int n = plotTrackView.trackerPanel.getPlayer().getStepNumber()+5;
      				plotTrackView.trackerPanel.getPlayer().setStepNumber(n);
      			}
      			else plotTrackView.trackerPanel.getPlayer().step();
      			break;
      		case KeyEvent.VK_HOME:      			
      			plotTrackView.trackerPanel.getPlayer().setStepNumber(0);
      			break;
      		case KeyEvent.VK_END:
      			VideoClip clip = plotTrackView.trackerPanel.getPlayer().getVideoClip();
      			plotTrackView.trackerPanel.getPlayer().setStepNumber(clip.getStepCount()-1);
      			break;
      		case KeyEvent.VK_DELETE:      // delete selected steps
      			plotTrackView.trackerPanel.deleteSelectedSteps();      
	            if (plotTrackView.trackerPanel.selectedPoint!=null 
	            		&& plotTrackView.trackerPanel.selectingPanel==plotTrackView.trackerPanel) {
	            	plotTrackView.trackerPanel.deletePoint(plotTrackView.trackerPanel.selectedPoint);
	            }
            return;

      	}
      }
    });
  }

  public void send(Job job, Tool replyTo) {
    XMLControlElement control = new XMLControlElement();
    try {
      control.readXML(job.getXML());
    } catch (RemoteException ex) {ex.printStackTrace();}
    ArrayList<Dataset> datasets = this.getObjectOfClass(Dataset.class);
    Iterator<Dataset> it = control.getObjects(Dataset.class).iterator();
    while (it.hasNext()) {
      Dataset newData = it.next();
      int id = newData.getID();
      for (int i = 0, n = datasets.size(); i < n; i++) {
        if (datasets.get(i).getID() == id) {
          copyProperties(newData, datasets.get(i));
          break;
        }
      }
    }
    repaint();
  }

  /**
   * Gets the dataset
   *
   * @return the dataset plotted on this panel
   */
  public HighlightableDataset getDataset() {
    return dataset;
  }

  /**
   * Sets the label for the X (horizontal) axis. Overrides PlottingPanel.
   *
   * @param label the x label.
   */
  public void setXLabel(String label) {
    dataset.setXYColumnNames(label, yLabel);
    xLabel = label;
    String xStr = TeXParser.removeSubscripting(xLabel)+"="; //$NON-NLS-1$
    String yStr = "  "+TeXParser.removeSubscripting(yLabel)+"="; //$NON-NLS-1$ //$NON-NLS-2$
    getCoordinateStringBuilder().setCoordinateLabels(xStr, yStr);
    // add units to label
    TTrack track = TTrack.getTrack(trackID);
		if (track.trackerPanel!=null) {
			String units = track.trackerPanel.getUnits(track, label);
			if (!"".equals(units)) { //$NON-NLS-1$
				label += " ("+units.trim()+")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
    super.setXLabel(label);
  }

  /**
   * Gets the label for the X (horizontal) axis.
   *
   * @return the x label
   */
  public String getXLabel() {
    return xLabel;
  }

  /**
   * Sets the label for the Y (vertical) axis. Overrides PlottingPanel.
   *
   * @param label the y label
   */
  public void setYLabel(String label) {
    yLabel = label;
    dataset.setXYColumnNames(xLabel, label);
    String xStr = TeXParser.removeSubscripting(xLabel)+"="; //$NON-NLS-1$
    String yStr = "  "+TeXParser.removeSubscripting(yLabel)+"="; //$NON-NLS-1$ //$NON-NLS-2$
    getCoordinateStringBuilder().setCoordinateLabels(xStr, yStr);
    // add units to label
    TTrack track = TTrack.getTrack(trackID);
		if (track.trackerPanel!=null) {
			String units = track.trackerPanel.getUnits(track, label);
			if (!"".equals(units)) { //$NON-NLS-1$
				label += " ("+units.trim()+")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
    super.setYLabel(label);
  }

  /**
   * Gets the label for the Y (vertical) axis.
   *
   * @return the y label
   */
  public String getYLabel() {
    return yLabel;
  }

  /**
   * Sets the title.
   *
   * @param title the title.
   */
  public void setTitle(String title) {
    super.setTitle(title);
    this.title = title;
    dataset.setName(title);
  }

  /**
   * Gets the title.
   *
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Adds a guest track. Guest tracks are plotted along with the main track.
   *
   * @param guest the track
   */
  public void addGuest(TTrack guest) {
  	if (guests.contains(guest)) return;
    guests.add(guest);
    isCustom = true;
  	HighlightableDataset guestDataset = guestDatasets.get(guest);
  	if (guestDataset==null) {
  		guestDataset = new HighlightableDataset();
  		guestDatasets.put(guest, guestDataset);
  	}
  	guest.removePropertyChangeListener("step", plotTrackView); //$NON-NLS-1$
  	guest.removePropertyChangeListener("steps", plotTrackView); //$NON-NLS-1$          		
  	guest.addPropertyChangeListener("step", plotTrackView); //$NON-NLS-1$
  	guest.addPropertyChangeListener("steps", plotTrackView); //$NON-NLS-1$
  }

  /**
   * Removes a guest track.
   *
   * @param guest the track
   */
  public void removeGuest(TTrack guest) {
    guests.remove(guest);
  	guest.removePropertyChangeListener("step", plotTrackView); //$NON-NLS-1$
  	guest.removePropertyChangeListener("steps", plotTrackView); //$NON-NLS-1$          		
  }

  /**
   * Overrides DrawingPanel scale method
   */
  public void scale(ArrayList<Drawable> list) {
    if (autoscaleXMin && !autoscaleXMax) {
      scaleXMin();
    }
    else if (!autoscaleXMin && autoscaleXMax) {
      scaleXMax();
    }
    else if (autoscaleXMin && autoscaleXMax) {
      scaleX(list);
    }
    if (autoscaleYMin && !autoscaleYMax) {
      scaleYMin();
    }
    else if (!autoscaleYMin && autoscaleYMax) {
      scaleYMax();
    }
    else if (autoscaleYMin && autoscaleYMax) {
      scaleY(list);
    }
  }
  
  /**
   * Gets the popup menu.
   */
  public JPopupMenu getPopupMenu() {
  	mergeYScalesItem.setText(TrackerRes.getString("TrackPlottingPanel.Popup.MenuItem.MergeYAxes")); //$NON-NLS-1$
	  linesItem.setText(TrackerRes.getString("TrackPlottingPanel.Popup.MenuItem.Lines")); //$NON-NLS-1$
	  pointsItem.setText(TrackerRes.getString("TrackPlottingPanel.Popup.MenuItem.Points")); //$NON-NLS-1$
	  selectPointsItem.setText(TrackerRes.getString("MainTView.Popup.MenuItem.Select")); //$NON-NLS-1$
	  deselectPointsItem.setText(TrackerRes.getString("MainTView.Popup.MenuItem.Deselect")); //$NON-NLS-1$
	  printItem.setText(TrackerRes.getString("TActions.Action.Print")); //$NON-NLS-1$
	  copyImageItem.setText(TrackerRes.getString("TMenuBar.Menu.CopyImage")); //$NON-NLS-1$
	  dataBuilderItem.setText(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
	  dataToolItem.setText(TrackerRes.getString("TableTrackView.Popup.MenuItem.Analyze")); //$NON-NLS-1$
	  algorithmItem.setText(TrackerRes.getString("Popup.MenuItem.Algorithm")); //$NON-NLS-1$
	  helpItem.setText(TrackerRes.getString("Tracker.Popup.MenuItem.Help")); //$NON-NLS-1$          
	  guestsItem.setText(TrackerRes.getString("TrackPlottingPanel.Popup.Menu.CompareWith")+"...");   //$NON-NLS-1$ //$NON-NLS-2$
    if (plotTrackView.mainView.getComponentCount()>1) {
    	popupmenu.add(mergeYScalesItem, 3);
    }
    else
    	popupmenu.remove(mergeYScalesItem);
    // include showXZeroItem and showYZeroItem only when they can change the scale
    popupmenu.remove(showXZeroItem);
    popupmenu.remove(showYZeroItem);
    if (getXMin()*getXMax()>0) {
	    String s = TeXParser.removeSubscripting(dataset.getColumnName(0));
	    s = TrackerRes.getString("TrackPlottingPanel.Popup.MenuItem.ShowZero")+" "+s+"=0"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  	showXZeroItem.setText(s);
	    popupmenu.insert(showXZeroItem, popupmenu.getComponentIndex(scaleItem));    	
    }
    if (getYMin()*getYMax()>0) {
	    String s = TeXParser.removeSubscripting(dataset.getColumnName(1));
	    s = TrackerRes.getString("TrackPlottingPanel.Popup.MenuItem.ShowZero")+" "+s+"=0"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  	showYZeroItem.setText(s);
	    popupmenu.insert(showYZeroItem, popupmenu.getComponentIndex(scaleItem));    	
    }

    // refresh guests item
    TTrack track = TTrack.getTrack(trackID);
    Class<? extends TTrack> type = track instanceof PointMass? PointMass.class:
    	track instanceof Vector? Vector.class: track.getClass();
    ArrayList<? extends TTrack> tracks = trackerPanel.getDrawables(type);
    tracks.removeAll(trackerPanel.calibrationTools);
    tracks.remove(track);
    guestsItem.setEnabled(!tracks.isEmpty());
    FontSizer.setFonts(popup, FontSizer.getLevel());
    
    // disable algorithmItem if not point mass track
    algorithmItem.setEnabled(track instanceof PointMass);
    return popupmenu;
  }

  /**
   * Creates a snapshot of this plot or, if possible, of the parent TViewChooser.
   */
  @Override
  public void snapshot() {
  	Component comp = this;
  	Container c = getParent();
  	while (c!=null) {
			if (c instanceof TViewChooser) {
				comp = c;
				break;
			}
			c = c.getParent();
  	}
    TrackerIO.ComponentImage ci = new TrackerIO.ComponentImage(comp);
    BufferedImage image = ci.getImage();
    int w = image.getWidth();
    int h = image.getHeight();
    if((w==0)||(h==0)) {
      return;
    }
    MeasuredImage mi = new MeasuredImage(image, 0, w, h, 0);
    
    // create ImageFrame using reflection
    OSPFrame frame = null;
    try {
			Class<?> type = Class.forName("org.opensourcephysics.frames.ImageFrame"); //$NON-NLS-1$
			Constructor<?>[] constructors = type.getConstructors();
			for(int i = 0; i<constructors.length; i++) {
			  Class<?>[] parameters = constructors[i].getParameterTypes();
			  if(parameters.length==1 && parameters[0]==MeasuredImage.class) {
			    frame = (OSPFrame) constructors[i].newInstance(new Object[] {mi});
			    break;
			  }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    if (frame==null) return;
    
    frame.setTitle(DisplayRes.getString("Snapshot.Title")); //$NON-NLS-1$
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setKeepHidden(false);
    FontSizer.setFonts(frame, FontSizer.getLevel());
    frame.pack();
    frame.setVisible(true);
  }
  
  /**
   * Builds the default popup menu for this panel.
   */
  protected void buildPopupmenu() {
  	if (popup==null) {
	  	popup = new JPopupMenu() {
	  		public void setVisible(boolean vis) {
	  			super.setVisible(vis);
	  			if (!vis) zoomBox.hide();
	  		}
	  	};
	  	setPopupMenu(popup);
	  	super.buildPopupmenu(); // builds snapshot item
	    // lines menu item
	    linesItem = new JCheckBoxMenuItem();
	    linesItem.setSelected(true);
	    linesItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        dataset.setConnected(linesItem.isSelected());
	        for (TTrack next: guests) {
	        	HighlightableDataset nextDataset = guestDatasets.get(next);
	        	nextDataset.setConnected(linesItem.isSelected());
	        }
	        isCustom = true;
	        repaint();
	      }
	    });
	    // points menu item
	    pointsItem = new JCheckBoxMenuItem();
	    pointsItem.setSelected(true);
	    pointsItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        if (pointsItem.isSelected()) {
	          dataset.setMarkerShape(Dataset.SQUARE);
		        for (TTrack next: guests) {
		        	HighlightableDataset nextDataset = guestDatasets.get(next);
		        	nextDataset.setMarkerShape(Dataset.SQUARE);
		        }
	        }
	        else {
	          dataset.setMarkerShape(Dataset.NO_MARKER);
		        for (TTrack next: guests) {
		        	HighlightableDataset nextDataset = guestDatasets.get(next);
		        	nextDataset.setMarkerShape(Dataset.NO_MARKER);
		        }
	        }
	        isCustom = true;
	        repaint();
	      }
	    });
	    // showZero menu items
	    showXZeroItem = new JMenuItem();
	    showXZeroItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        showZeroOnAxis("x"); //$NON-NLS-1$
	      }
	    });
	    showYZeroItem = new JMenuItem();
	    showYZeroItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        showZeroOnAxis("y"); //$NON-NLS-1$
	      }
	    });
	    printItem = new JMenuItem();
	    printItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	      	// find TViewChooser that owns this view and print it
	      	TViewChooser chooser = getOwner();
	        if (chooser != null) {
	          new TrackerIO.ComponentImage(chooser).print();
	        }
	      }
	    });
	    // dataTool item
	    dataToolItem = new JMenuItem();
	    dataToolItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
		      DataTool tool = DataTool.getTool();
		      DataToolTab tab = tool.getTab(data);
	        tool.setUseChooser(false);
	        tool.setSaveChangesOnClose(false);
	      	DatasetManager toSend = new DatasetManager();
	        DataRefreshTool refresher = DataRefreshTool.getTool(data);
	      	toSend.setID(data.getID());
	        TTrack track = TTrack.getTrack(trackID);
	      	toSend.setName(track.getName());
	      	int i = 0;
	    	  // always include linked independent variable first
	      	Dataset nextIn = data.getDataset(0);
		    	String xColName = nextIn.getXColumnName();
	      	XMLControlElement control = new XMLControlElement(nextIn);
		    	Dataset nextOut = toSend.getDataset(i++); // first dataset to send
		    	control.loadObject(nextOut, true, true); // contains indep var
		    	nextOut.setYColumnVisible(false);
		    	nextOut.setConnected(false);
		    	nextOut.setMarkerShape(Dataset.NO_MARKER);
		    	double[] tArray = nextOut.getXPoints();
		    	if (!guests.isEmpty()) {
		      	// expand tArray by collecting all values in a TreeSet
		      	TreeSet<Double> tSet = new TreeSet<Double>();
		      	for (double t: tArray) {
		      		tSet.add(t);
		      	}
			      for (TTrack guest: guests) {
			      	DatasetManager guestData = guest.getData(guest.trackerPanel);
			      	Dataset nextGuestIn = guestData.getDataset(0);
			      	double[] guestTArray = nextGuestIn.getXPoints();
			      	for (double t: guestTArray) {
			      		tSet.add(t);
			      	}
			      }
			      tArray = new double[tSet.size()];
		      	Double[] temp = tSet.toArray(new Double[tArray.length]);
		      	for (int k=0; k<tArray.length; k++) {
		      		tArray[k] = temp[k];
		      	}
		      	// finished expanding tArray
		      	// pad nextOut with NaNs
		      	padDataset(nextOut, tArray);
		    	}
	        // add the x and y datasets
	        if (xIndex >= 0) {
	        	nextIn = data.getDataset(xIndex);
	  	    	xColName = nextIn.getYColumnName();
	          control = new XMLControlElement(nextIn);
	          nextOut = toSend.getDataset(i++); // second dataset to send (if not indep var)
	  	    	control.loadObject(nextOut, true, true);
	  	    	nextOut.setMarkerColor(track.getColor());
	  	    	nextOut.setLineColor(track.getColor().darker());
	  	    	nextOut.setConnected(true);
	  	    	nextOut.setXColumnVisible(false);
			    	if (!guests.isEmpty()) {
			      	// pad nextOut with NaNs
			      	padDataset(nextOut, tArray);
			    	}
	        }
	        nextIn = data.getDataset(yIndex);
	      	String yColName = nextIn.getYColumnName();
		      if (yIndex != xIndex) {
		        control = new XMLControlElement(nextIn);
		        nextOut = toSend.getDataset(i++); // next dataset to send
			    	control.loadObject(nextOut, true, true);
			    	nextOut.setMarkerColor(track.getColor());
			    	nextOut.setLineColor(track.getColor().darker());
			    	nextOut.setConnected(true);
			    	nextOut.setXColumnVisible(false);
			    	if (!guests.isEmpty()) {
			      	// pad nextOut with NaNs
			      	padDataset(nextOut, tArray);
			    	}			    	
		      }
		      // if this plot has guests, send their data too
		      for (TTrack guest: guests) {
		      	DatasetManager guestData = guest.getData(guest.trackerPanel);
		      	refresher.addData(guestData);
		        if (xIndex >= 0) {
		        	nextIn = guestData.getDataset(xIndex);
		          control = new XMLControlElement(nextIn);
		          nextOut = toSend.getDataset(i++);
		  	    	control.loadObject(nextOut, true, true);
		  	    	nextOut.setMarkerColor(guest.getColor());
		  	    	nextOut.setLineColor(guest.getColor().darker());
		  	    	nextOut.setConnected(true);
		  	    	nextOut.setXColumnVisible(false);		  	    	
		  	    	if (tab!=null) {
		  	    		String newName = tab.getColumnName(nextOut.getID());
		  	    		if (newName!=null) {
		  	    			nextOut.setXYColumnNames(nextOut.getXColumnName(), newName);
		  	    		}
		  	    	}
		  	    	else {
		  	    		String newName = nextOut.getYColumnName()+"_{"+guest.getName()+"}"; //$NON-NLS-1$ //$NON-NLS-2$
	  	    			nextOut.setXYColumnNames(nextOut.getXColumnName(), newName);
		  	    	}
			      	// pad nextOut with NaNs
			      	padDataset(nextOut, tArray);
		        }
			      if (yIndex != xIndex) {
			        nextIn = guestData.getDataset(yIndex);
			        control = new XMLControlElement(nextIn);
			        nextOut = toSend.getDataset(i++);
				    	control.loadObject(nextOut, true, true);
				    	nextOut.setMarkerColor(guest.getColor());
				    	nextOut.setLineColor(guest.getColor().darker());
				    	nextOut.setConnected(true);
				    	nextOut.setXColumnVisible(false);
		  	    	if (tab!=null) {
		  	    		String newName = tab.getColumnName(nextOut.getID());
		  	    		if (newName!=null) {
		  	    			nextOut.setXYColumnNames(nextOut.getXColumnName(), newName);
		  	    		}
		  	    	}
		  	    	else {
		  	    		String newName = nextOut.getYColumnName()+"_{"+guest.getName()+"}"; //$NON-NLS-1$ //$NON-NLS-2$
	  	    			nextOut.setXYColumnNames(nextOut.getXColumnName(), newName);
		  	    	}
			      	// pad nextOut with NaNs
			      	padDataset(nextOut, tArray);
			      }		      	
		      }
	        // get data tool and send it the job
	        try {
	          tool.send(new LocalJob(toSend), refresher);
	          tab = tool.getTab(toSend);
	          if (tab != null) {
	          	tab.setWorkingColumns(xColName, yColName);
	          }
	          tool.setVisible(true);
	        }
	        catch(RemoteException ex) {ex.printStackTrace();}
	      }
	    });
	    // algorithm item
	    algorithmItem = new JMenuItem();
	    algorithmItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	      	DerivativeAlgorithmDialog dialog = trackerPanel.getAlgorithmDialog();
	      	TTrack track = TTrack.getTrack(trackID);
	      	if (track instanceof PointMass) {
	      		dialog.setTargetMass((PointMass)track);
	      	}
	      	FontSizer.setFonts(dialog, FontSizer.getLevel());
	      	dialog.pack();
	      	dialog.setVisible(true);
	      }
	    });
	    // copy image item
	    Action copyImageAction = new AbstractAction() {
	      public void actionPerformed(ActionEvent e) {
	      	// find TViewChooser that owns this view and copy it
	      	TViewChooser chooser = getOwner();
	        if (chooser != null) {
	          new TrackerIO.ComponentImage(chooser).copyToClipboard();
	        }
	      }
	    };
	    copyImageItem = new JMenuItem(copyImageAction);
	    // dataBuilder item
	    dataFunctionListener = new AbstractAction() {
	      public void actionPerformed(ActionEvent e) {
	        TTrack track = TTrack.getTrack(trackID);
	      	trackerPanel.getDataBuilder().setSelectedPanel(track.getName());
	      	trackerPanel.getDataBuilder().setVisible(true);
	      }
	    };
	    dataBuilderItem = new JMenuItem();
	    dataBuilderItem.addActionListener(dataFunctionListener);
	    // help item
	    helpItem = new JMenuItem();
	    helpItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	        Container c = getTopLevelAncestor();
	        if (c instanceof TFrame) {
	          TFrame frame = (TFrame)c;
		        frame.showHelp("plot", 0); //$NON-NLS-1$
	        }
	      }
	    });
	    // mergeYAxesItem
	    mergeYScalesItem = new JMenuItem();
	    mergeYScalesItem.addActionListener(new ActionListener() {
	      public void actionPerformed(ActionEvent e) {
	      	int n = plotTrackView.mainView.getComponentCount();
	      	if (n==2)
	      		plotTrackView.syncYAxes(plotTrackView.plots[0], plotTrackView.plots[1]);
	      	if (n==3)
	      		plotTrackView.syncYAxes(plotTrackView.plots);
	      }
	    });
  	}
    // guests item
    guestsItem = new JMenuItem();
    guestsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PlotGuestDialog dialog = trackerPanel.getPlotGuestDialog(TrackPlottingPanel.this);
        dialog.setVisible(true);
      }
    });
    
    Action selectAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	// find limits of zoom box
      	Rectangle rect = zoomBox.reportZoom();
        double x = pixToX(rect.x);
        double x2 = pixToX(rect.x+rect.width);
        double y = pixToY(rect.y+rect.height);
        double y2 = pixToY(rect.y);
        double xmin = Math.min(x, x2);
        double xmax = Math.max(x, x2);
        double ymin = Math.min(y, y2);
        double ymax = Math.max(y, y2);
        // look for all points in the dataset that fall within limits
        double[] xPoints = dataset.getXPoints();
        double[] yPoints = dataset.getYPoints();
        TTrack track = TTrack.getTrack(trackID);
        TreeSet<Integer> frames = new TreeSet<Integer>();
        for (int i=0; i<xPoints.length; i++) {
        	if (Double.isNaN(xPoints[i]) || Double.isNaN(yPoints[i])) continue;
        	if (xPoints[i]>=xmin && xPoints[i]<=xmax && yPoints[i]>=ymin && yPoints[i]<=ymax) {
        		// found one: add its frame number to frames set
          	int frame = track.getFrameForData(getXLabel(), getYLabel(), new double[] {xPoints[i], yPoints[i]});
          	if (frame>=0) {
          		frames.add(frame);
          	}
        	}
        }
        // add or remove steps from selectedSteps
        for (int frame: frames) {
        	Step step = track.getStep(frame);
        	if (e.getSource()==selectPointsItem) trackerPanel.selectedSteps.add(step);
        	else  trackerPanel.selectedSteps.remove(step);
      		step.erase();
        }
        trackerPanel.repaint();
    		track.firePropertyChange("steps", null, null); //$NON-NLS-1$
      }    	
    };
    selectPointsItem = new JMenuItem();
    selectPointsItem.addActionListener(selectAction);
    deselectPointsItem = new JMenuItem();
    deselectPointsItem.addActionListener(selectAction);

    popupmenu.removeAll();
    popupmenu.add(zoomInItem);
    popupmenu.add(zoomOutItem);
    popupmenu.add(autoscaleItem);
    popupmenu.add(showYZeroItem);
    popupmenu.add(showXZeroItem);
    popupmenu.add(scaleItem);
    popupmenu.addSeparator();    
    popupmenu.add(selectPointsItem);
    popupmenu.add(deselectPointsItem);
    popupmenu.addSeparator();    
    popupmenu.add(pointsItem);
    popupmenu.add(linesItem);
    if (trackerPanel!=null) {
  		if (trackerPanel.isEnabled("edit.copyImage")) { //$NON-NLS-1$
  	    popupmenu.addSeparator();
  	    popupmenu.add(copyImageItem);
  	    popupmenu.add(snapshotItem);
      } 
  		if (trackerPanel.isEnabled("plot.compare")) { //$NON-NLS-1$
	    	popupmenu.addSeparator();
  			popupmenu.add(guestsItem);
  		}
  		if (trackerPanel.isEnabled("data.builder") //$NON-NLS-1$
    			|| trackerPanel.isEnabled("data.tool")) { //$NON-NLS-1$    
	    	popupmenu.addSeparator();
      	if (trackerPanel.isEnabled("data.builder")) //$NON-NLS-1$
      		popupmenu.add(dataBuilderItem);
      	if (trackerPanel.isEnabled("data.tool")) //$NON-NLS-1$
      		popupmenu.add(dataToolItem);
      }
  		if (trackerPanel.isEnabled("data.algorithm")) { //$NON-NLS-1$
  	    popupmenu.addSeparator();
  	    popupmenu.add(algorithmItem);
      }    	
  		if (trackerPanel.isEnabled("file.print")) { //$NON-NLS-1$
  	    popupmenu.addSeparator();
  	    popupmenu.add(printItem);
      }    	
    }
    popupmenu.addSeparator();
    popupmenu.add(helpItem);
  }

  /**
   * Overrides DrawingPanel method to prevent changes to glassPanel
	 * and deal with multiple plots.
   */
  protected Rectangle findViewRect() {
    Rectangle rect = null;
    Container parent = getParent();
    while(parent!=null) {
      if(parent instanceof JViewport) {
      	// gets rect in the PlotTrackView.mainPanel space
        rect = ((JViewport) parent).getViewRect();
        // transform rect to this plotting panel space
        Rectangle bounds = getBounds();
        rect = rect.intersection(bounds);
        rect.y -= bounds.y;
        break;
      }
      parent = parent.getParent();
    }
    return rect;
  }
  
  /**
   * Rescales this plot so the zero value is included in the range ymin-ymax.
   * @return true if the plot scale is changed.
   */
  protected void showZeroOnAxis(String axis) {
  	if (axis.equals("x")) { //$NON-NLS-1$
	    if (xmin*xmax>0) { // either both pos or both neg
	    	if (xmax>0) xmin = 0;
	    	else xmax = 0;
		    setPreferredMinMax(xmin, xmax, ymin, ymax);
		    repaint(); // repaint the panel with the new scale
	  		isCustom = true;
	    }
  	}
  	else {
	    if (ymin*ymax>0) { // either both pos or both neg
	    	if (ymax>0) ymin = 0;
	    	else ymax = 0;
		    setPreferredMinMax(xmin, xmax, ymin, ymax);
		    repaint(); // repaint the panel with the new scale
	  		isCustom = true;
	    }
  	}
  }

  /**
   * Sets the min on the horizontal axis scale.
   */
  protected void scaleXMin() {
    double newXMin = Double.MAX_VALUE;
    Measurable dataset = getDataset();
    if (dataset != null && dataset.isMeasured()) {
      if (!Double.isNaN(dataset.getXMin())) {
        newXMin = Math.min(newXMin, dataset.getXMin());
      }
      if(newXMin == xmaxPreferred) {  //bracket the value
        newXMin = 0.9 * newXMin - 0.5;
      }
      double range = xmaxPreferred - newXMin;
      xminPreferred = newXMin - autoscaleMargin * range;
    }
    if (!Double.isNaN(xfloor)) {
      xminPreferred = Math.min(xfloor, xminPreferred);
    }
  }

  /**
   * Sets the max on the horizontal axis scale.
   */
  protected void scaleXMax() {
    double newXMax = -Double.MAX_VALUE;
    Measurable dataset = getDataset();
    if (dataset != null && dataset.isMeasured()) {
      if (!Double.isNaN(dataset.getXMax())) {
        newXMax = Math.max(newXMax, dataset.getXMax());
      }
      if (xminPreferred == newXMax) {  //bracket the value
        newXMax = 1.1 * newXMax + 0.5;
      }
      double range = newXMax - xminPreferred;
      xmaxPreferred = newXMax + autoscaleMargin * range;
    }
    if (!Double.isNaN(xceil)) {
      xmaxPreferred = Math.max(xceil, xmaxPreferred);
    }
  }

  /**
   * Sets the min on the vertical axis scale.
   */
  protected void scaleYMin() {
    double newYMin = Double.MAX_VALUE;
    Measurable dataset = getDataset();
    if (dataset != null && dataset.isMeasured()) {
    	if (!Double.isNaN(dataset.getYMin())) {
    		newYMin = Math.min(newYMin, dataset.getYMin());
    	}
      if(newYMin == ymaxPreferred) {
        newYMin = 0.9 * newYMin - 0.5;
      }
      double range = ymaxPreferred - newYMin;
      yminPreferred = newYMin - autoscaleMargin * range;
    }
    if (!Double.isNaN(yfloor)) {
      yminPreferred = Math.min(yfloor, yminPreferred);
    }
  }

  /**
   * Sets the max on the vertical axis scale.
   */
  protected void scaleYMax() {
    double newYMax = -Double.MAX_VALUE;
    Measurable dataset = getDataset();
    if (dataset != null && dataset.isMeasured()) {
      if(!Double.isNaN(dataset.getYMax())) {
        newYMax = Math.max(newYMax, dataset.getYMax());
      }
      if (yminPreferred == newYMax) {
        newYMax = 1.1 * newYMax + 0.5;
      }
      double range = newYMax - yminPreferred;
      ymaxPreferred = newYMax + autoscaleMargin * range;
    }
    if(!Double.isNaN(yceil)) {
      ymaxPreferred = Math.max(yceil, ymaxPreferred);
    }
  }

  /**
   * Gets the TViewChooser that owns (displays) this panel.
   * @return the TViewChooser
   */
  protected TViewChooser getOwner() {
  	if (trackerPanel == null) return null;
  	// find TViewChooser with this view
  	TFrame frame = trackerPanel.getTFrame();
  	if (frame == null) return null;
  	Container[] views = frame.getViews(trackerPanel);
  	for (int i = 0; i < views.length; i++) {
      if (views[i] instanceof TViewChooser) {
        TViewChooser chooser = (TViewChooser)views[i];
        if (chooser.getSelectedView() instanceof PlotTView) {
        	PlotTView plotView = (PlotTView)chooser.getSelectedView();
        	TrackView view = plotView.getTrackView(plotView.getSelectedTrack());
        	if (view != null && view.equals(plotTrackView)) {
        		return chooser;
        	}
        }
      }
  	}
  	return null;
  }
  
  /**
   * Plots the data.
   */
  protected void plotData() {
  	removeDrawables(Dataset.class);
  	// refresh the plot titles and determine if angles are being plotted
    Dataset xData;
    // xIndex == -1 indicates x column variable (same for all datasets)
    // xIndex >= 0 indicates y column variable of specified dataset
    if (xIndex == -1) xData = data.getDataset(0);
    else xData = data.getDataset(xIndex);
    Dataset yData = data.getDataset(yIndex);
    TTrack track = TTrack.getTrack(trackID);
    String xTitle;
    if (xIndex == -1) xTitle = xData.getColumnName(0);
    else xTitle = xData.getColumnName(1);
    String yTitle = yData.getColumnName(1);
    setTitle(track.getName()+" (" + xTitle + ", " + yTitle + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    setXLabel(xTitle);
    setYLabel(yTitle);
    boolean xIsAngle = xTitle.startsWith(Tracker.THETA)
				|| xTitle.startsWith(Tracker.OMEGA)
				|| xTitle.startsWith(Tracker.ALPHA);
    boolean yIsAngle = yTitle.startsWith(Tracker.THETA)
    		|| yTitle.startsWith(Tracker.OMEGA)
    		|| yTitle.startsWith(Tracker.ALPHA);
    boolean degrees = trackerPanel!=null 
    		&& trackerPanel.getTFrame()!=null
    		&& !trackerPanel.getTFrame().anglesInRadians;
    
    // refresh the coordStringBuilder
    coordStringBuilder.setUnitsAndPatterns(track, xTitle, yTitle);
    // refresh the main dataset
    refreshDataset(dataset, data, xIsAngle, yIsAngle, degrees);
    // add dataset to this plot panel
	  addDrawable(dataset);
    
    // refresh guest datasets
	  // first eliminate any guests that may have been deleted
    for (Iterator<TTrack> it = guests.iterator(); it.hasNext();) {    	
    	// check if guest still exists in tracker panel
    	TTrack next = it.next();
    	if (next!=null && trackerPanel.getTrack(next.getName())==null) {
    		it.remove();
    	}
    }
    // now plot guests
    for (TTrack next: guests) {
    	DatasetManager nextData = next.getData(next.trackerPanel);
    	HighlightableDataset nextDataset = guestDatasets.get(next);
    	nextDataset.setMarkerColor(next.getColor());
    	nextDataset.setHighlightColor(next.getColor());
      refreshDataset(nextDataset, nextData, xIsAngle, yIsAngle, degrees);
  	  addDrawable(nextDataset);
    }
    
    // refresh highlighted indices
    dataset.clearHighlights();
	  for (int n: highlightIndices) {
	  	dataset.setHighlighted(n, true);
  	}
	  
	  // refresh plot coordinates
    int plotIndex = -1;
  	if (highlightIndices.size()==1) {
  		plotIndex = highlightIndices.toArray(new Integer[1])[0];
  	}
    showPlotCoordinates(plotIndex);
    repaint();
  }

  /**
   * Refreshes the data in a dataset based on current x and y index.
   * @param hds the dataset to refresh
   * @param manager the DatasetManager with the data columns
   * @param xIsAngle true if the x index is an angle
   * @param yIsAngle true if the y index is an angle
   * @param degrees true if angle units are degrees
   */
  protected void refreshDataset(HighlightableDataset hds, DatasetManager manager,
  		boolean xIsAngle, boolean yIsAngle, boolean degrees) {
    // get the dataset for the current x and y indices
    // assign quasi-unique ID to dataset based on data and indices
    int id = manager.hashCode() & 0xffff;
    hds.setID(id + xIndex*100 + yIndex*10);
    // set the track-specified dataset properties
    hds.setConnected(dataset.isConnected());
    hds.setMarkerShape(dataset.getMarkerShape());
    
    // clear and refill dataset with x- and y-axis variables
    hds.clear();
    Dataset xData;
    // xIndex == -1 indicates x column variable (same for all datasets)
    // xIndex >= 0 indicates y column variable of specified dataset
    if (xIndex == -1) xData = manager.getDataset(0);
    else xData = manager.getDataset(xIndex);
    Dataset yData = manager.getDataset(yIndex);
    xData.setYColumnVisible(true);
    yData.setYColumnVisible(true);
    // use x mean value for filler points (y = Double.NaN)
    double xMean = getMean(xData.getXPoints());
    if (xIndex > -1)
      xMean = getMean(xData.getYPoints());
    // append data to dataset
    Double x = null, y = null;
    if (xMean != Double.NaN) {
    	int n = yData.getRowCount();
      for (int i = 0; i < n; i++) {
        y = (Double)yData.getValueAt(i, 1);
        if (xIndex == -1)
          x = (Double)xData.getValueAt(i, 0);
        else
          x = (Double)xData.getValueAt(i, 1);
        if (x != null && y != null) {
        	if (xIsAngle && degrees) {
        		x *= 180/Math.PI;
        	}
        	if (yIsAngle && degrees) {
        		y *= 180/Math.PI;        		
        	}
          hds.append(x, y);
        }
        else if (x != null) {
        	if (xIsAngle && degrees) {
        		x *= 180/Math.PI;
        	}
          hds.append(x.doubleValue(), Double.NaN);
        }
        else {
          hds.append(xMean, Double.NaN);
        }
      }
    }
  }

  /**
   * Shows plot coordinates at the specified dataset index.
   * @param index the index
   */
  protected void showPlotCoordinates(int index) {
    String msg = ""; //$NON-NLS-1$
    if (index >=0 && dataset.getIndex() > index) {
    	double x = dataset.getXPoints()[index];
    	double y = dataset.getYPoints()[index];
      TTrack track = TTrack.getTrack(trackID);
    	msg = coordStringBuilder.getCoordinateString(track.trackerPanel, x, y);
    }
    setMessage(msg, 0);
  }
  
  /**
   * Sets preferred min/max values. Overrides DrawingPanel method.
   * 
   * @param xmin
   * @param xmax
   * @param ymin
   * @param ymax
   * @param invalidateImage invalidates image if min/max have changed
   */
  public void setPreferredMinMax(double xmin, double xmax, double ymin, double ymax, 
  		boolean invalidateImage) {
  	trackerPanel.changed = true;
    isCustom = true;
  	super.setPreferredMinMax(xmin, xmax, ymin, ymax, invalidateImage);
  	if (plotTrackView != null) 
  		plotTrackView.syncXAxesTo(this);
  }
  
  /**
   * Sets preferred min/max x values. Overrides DrawingPanel method.
   * 
   * @param xmin
   * @param xmax
   */
  public void setPreferredMinMaxX(double xmin, double xmax) {
  	trackerPanel.changed = true;
    isCustom = true;
  	super.setPreferredMinMaxX(xmin, xmax);
  	if (plotTrackView != null) 
  		plotTrackView.syncXAxesTo(this);
  }
  
  /**
   * Sets preferred min/max y values. Overrides DrawingPanel method.
   * 
   * @param ymin
   * @param ymax
   */
  public void setPreferredMinMaxY(double ymin, double ymax) {
  	trackerPanel.changed = true;
    isCustom = true;
  	super.setPreferredMinMaxY(ymin, ymax);
  }
  
  /**
   * Overrides requestFocusInWindow. 
   * This declines the focus if scale setter is visible.
   */
  public boolean requestFocusInWindow() {
    if (plotAxes.getScaleSetter().isVisible())
    	return false;
  	return super.requestFocusInWindow();
  }
  
  /**
   * Adds a highlight for the specified frame number.
   *
   * @param frameNumber the frame number
   */
  protected void addHighlight(int frameNumber) {
    // add data index to highlightIndices if found
    TTrack track = TTrack.getTrack(trackID);
    int index = track.getDataIndex(frameNumber);
    if (index>-1) highlightIndices.add(index);  
  }

  /**
   * Sets the x variable by name.
   *
   * @param name the name of the dataset to plot on the x axis
   */
  protected void setXVariable(String name) {
  	if (name == null) return;
    // find the desired menu item
    for (int i = 0; i < xChoices.length; i++) {
  		String var = xChoices[i].getText();
  		if (name.indexOf(DEFINED_AS) == -1) {
  			// name doesn't include description, so strip from menu item
    		int n = var.indexOf(DEFINED_AS);
    		if (n > -1) var = var.substring(0, n);
  		}
      if (var.equals(name)) {
      	// set index for plotting
        xIndex = i - 1; // index -1 is data column 0
        // select menu radio button if not selected
        ButtonModel current = xGroup.getSelection();
        ButtonModel desired = xChoices[i].getModel();
        if (current == null || current != desired) {
          xChoices[i].setSelected(true);
        }
        break;
      }
    }
    if (plotTrackView != null) 
    	plotTrackView.syncXAxesTo(this);
  }

  /**
   * Gets the x variable name.
   *
   * @return the name of the x variable
   */
  protected String getXVariable() {
  	if (xChoices == null) return null;
    // find the selected menu item
  	for (int i = 0; i < xChoices.length; i++) {
      if (xChoices[i].isSelected()) {
    		String var = xChoices[i].getText();
    		int n = var.indexOf(DEFINED_AS);
    		if (n > -1) var = var.substring(0, n);
        return var;
      }
    }
  	return null;
  }

  /**
   * Sets the y variable by name.
   *
   * @param name the name of the dataset to plot on the y axis
   */
  protected void setYVariable(String name) {
  	if (name == null) return;
    // find the desired menu item
    for (int i = 0; i < yChoices.length; i++) {
  		String var = yChoices[i].getText();
  		if (name.indexOf(DEFINED_AS) == -1) {
  			// name doesn't include description, so strip from menu item
    		int n = var.indexOf(DEFINED_AS);
    		if (n > -1) var = var.substring(0, n);
  		}
      if (var.equals(name)) {
      	// set index for plotting
        yIndex = i;
        // select menu radio button if not selected
        ButtonModel current = yGroup.getSelection();
        ButtonModel desired = yChoices[i].getModel();
        if (current == null || current != desired) {
          yChoices[i].setSelected(true);
        }
        super.setPreferredMinMaxY(Double.NaN, Double.NaN);
        break;
      }
    }
  }

  /**
   * Gets the y variable name.
   *
   * @return the name of the y variable
   */
  protected String getYVariable() {
  	if (yChoices == null) return null;
    // find the selected menu item
  	for (int i = 0; i < yChoices.length; i++) {
      if (yChoices[i].isSelected()) {
    		String var = yChoices[i].getText();
    		int n = var.indexOf(DEFINED_AS);
    		if (n > -1) var = var.substring(0, n);
        return var;
      }
    }
  	return null;
  }

  /**
   * Sets the PlotTrackView that owns this.
   * @param view the PlotTrackView
   */
  protected void setPlotTrackView(PlotTrackView view) {
  	plotTrackView = view;
  	VideoPlayer player = plotTrackView.trackerPanel.getPlayer();
  	player.removePropertyChangeListener("stepnumber", playerListener); //$NON-NLS-1$
  	player.addPropertyChangeListener("stepnumber", playerListener); //$NON-NLS-1$
  }

  protected void dispose() {
  	VideoPlayer player = plotTrackView.trackerPanel.getPlayer();
  	player.removePropertyChangeListener("stepnumber", playerListener); //$NON-NLS-1$
  	for (TTrack guest: guests) {
    	guest.removePropertyChangeListener("step", plotTrackView); //$NON-NLS-1$
    	guest.removePropertyChangeListener("steps", plotTrackView); //$NON-NLS-1$
  	}
  	guests.clear();
  	guestDatasets.clear();
    data = null;
    plotTrackView = null;
    trackerPanel = null;
  }

  /**
   * Calculates the mean of a data array.
   *
   * @param data the data array
   * @return the mean
   */
  private double getMean(double[] data) {
    double sum = 0.0;
    int count = 0;
    for (int i = 0; i < data.length; i++) {
      if (Double.isNaN(data[i])) {
        continue;
      }
      count++;
      sum += data[i];
    }
    return sum / count;
  }
  
  protected void createVarChoices() {
    // find the selected menu item
    int datasetCount = data.getDatasets().size();
    boolean smaller = yChoices == null? false: datasetCount < yChoices.length;
  	String xName = getXVariable();
  	String yName = getYVariable();
    xGroup = new ButtonGroup();
    yGroup = new ButtonGroup();
    xPopup = new JPopupMenu();
    yPopup = new JPopupMenu();
    // create radio buttons and popups to set x and y variables
    xChoices = new JRadioButtonMenuItem[datasetCount+1];
    yChoices = new JRadioButtonMenuItem[datasetCount];
    TTrack track = TTrack.getTrack(trackID);
    boolean foundY = false, foundX = false;
    String name = track.getDataName(0); // linked x-variable
    name = TeXParser.removeSubscripting(name);
    if (name == xName) foundX = true;
    name += DEFINED_AS+track.getDataDescription(0);
    // add radio button items to array, popup and button group
    xChoices[0] = new JRadioButtonMenuItem(name);
    if (foundX) {
      xChoices[0].setSelected(true);
      xIndex = -1;
    }
    xChoices[0].setFont(font);
    xChoices[0].setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
    xChoices[0].addItemListener(xListener);
    xGroup.add(xChoices[0]);
    xPopup.add(xChoices[0]);
    for (int i = 0; i < datasetCount; i++) { // y-variables
      name = track.getDataName(i+1);
      name = TeXParser.removeSubscripting(name);
      boolean isXVar = name.equals(xName);
      boolean isYVar = name.equals(yName);
      foundX = foundX || isXVar;
      foundY = foundY || isYVar;
      if (!track.getDataDescription(i+1).equals("")) //$NON-NLS-1$
      name += DEFINED_AS+track.getDataDescription(i+1);
      xChoices[i+1] = new JRadioButtonMenuItem(name);
      if (isXVar) {
        xChoices[i+1].setSelected(true);
        xIndex = i;
      }
      xChoices[i+1].setFont(font);
      xChoices[i+1].setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
      xChoices[i+1].addItemListener(xListener);
      xGroup.add(xChoices[i+1]);
      xPopup.add(xChoices[i+1]);
      yChoices[i] = new JRadioButtonMenuItem(name);
      if (isYVar) {
        yChoices[i].setSelected(true);
        yIndex = i;
      }
      yChoices[i].setFont(font);
      yChoices[i].setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
      yChoices[i].addItemListener(yListener);
      yGroup.add(yChoices[i]);
      yPopup.add(yChoices[i]);
    }
    // add define data function items to x and y lists
    JMenuItem defineItem = new JMenuItem(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
    defineItem.addActionListener(dataFunctionListener);
    xPopup.addSeparator();
    xPopup.add(defineItem);
    defineItem = new JMenuItem(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
    defineItem.addActionListener(dataFunctionListener);
    yPopup.addSeparator();
    yPopup.add(defineItem);
    // check indices
    if (xIndex >= xChoices.length-1 || (smaller && !foundX)) xIndex = -1;
    if (yIndex >= yChoices.length || (smaller && !foundY)) yIndex = 0;
    if (!foundX) setXVariable(xChoices[xIndex+1].getText());
    if (!foundY) setYVariable(yChoices[yIndex].getText());
  }

  private void copyProperties(Dataset source, Dataset destination) {
    XMLControl control = new XMLControlElement(source); //convert the source to xml
    Dataset.getLoader().loadObject(control, destination); // copy the data to the destination
  }
  
  /**
   * Pads a dataset with NaN values where needed.
   *
   * @param dataset the dataset
   * @param newXArray expanded array of independent variable values
   */
  private void padDataset(Dataset dataset, double[] newXArray) {
  	double[] xArray = dataset.getXPoints(); 
  	double[] yArray = dataset.getYPoints();
  	Map<Double, Double> valueMap = new HashMap<Double, Double>();
  	for (int k=0; k<xArray.length; k++) {
  		valueMap.put(xArray[k], yArray[k]);
  	}
  	// pad y-values of nextOut with NaN where needed
  	double[] newYArray = new double[newXArray.length];
  	for (int k=0; k<newXArray.length; k++) {
  		double x = newXArray[k];
  		newYArray[k] = valueMap.keySet().contains(x)? valueMap.get(x): Double.NaN;
  	}
  	dataset.clear();
  	dataset.append(newXArray, newYArray);
  }

  /**
   * An interactive axes class that returns popup menus for x and y-variables.
   */
  class ClickableAxes extends CartesianInteractive {
    
    ClickableAxes(PlottingPanel panel) {
      super(panel);
      setDefaultGutters(defaultLeftGutter, 30, defaultRightGutter, defaultBottomGutter);
      setCoordinateStringBuilder(coordStringBuilder);
    }
    
    // Overrides CartesianInteractive method
    public ScaleSetter getScaleSetter() {
    	ScaleSetter setter = super.getScaleSetter();
    	FontSizer.setFonts(setter, FontSizer.getLevel());
    	return setter;
    }
    
    // Overrides CartesianInteractive method
    protected boolean hasHorzVariablesPopup() {
    	return true;
    }
    
    // Overrides CartesianInteractive method
    protected javax.swing.JPopupMenu getHorzVariablesPopup() {
      FontSizer.setFonts(xPopup, FontSizer.getLevel());
    	return xPopup;
    }
    
    // Overrides CartesianInteractive method
    protected boolean hasVertVariablesPopup() {
    	return true;
    }
    
    // Overrides CartesianInteractive method
    protected javax.swing.JPopupMenu getVertVariablesPopup() {
      FontSizer.setFonts(yPopup, FontSizer.getLevel());
    	return yPopup;
    }
    
  }

  /**
   * A Mouse Listener that selects data points and displays the Data Tool.
   */
  class PlotMouseListener extends MouseInputAdapter {
    int region;
    Interactive iad;
    
    public void mouseEntered(MouseEvent e) {
      mouseEvent = e;
      mouseAction = MOUSE_ENTERED;
//      requestFocusInWindow();
    }
    
    public void mouseExited(MouseEvent e) {
      mouseEvent = e;
      mouseAction = MOUSE_EXITED;
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseMoved(MouseEvent e) {
      mouseEvent = e;
      mouseAction = MOUSE_MOVED;
      TTrack track = TTrack.getTrack(trackID);
      if (!(track instanceof LineProfile))
      	iad = getInteractive();
    	Point p = e.getPoint();
    	region = getRegion(p);
  		setShowCoordinates(region==CartesianInteractive.INSIDE
  				&& getCursor()==Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      if (region==CartesianInteractive.INSIDE)
      	setToolTipText(TrackerRes.getString("TrackPlottingPanel.RightDrag.Hint")); //$NON-NLS-1$
      else
      	setToolTipText(null);
      repaint();
    }
    
    public void mousePressed(MouseEvent e) {
      mouseEvent = e;
      mouseAction = MOUSE_PRESSED;
      Point p = e.getPoint();
    	region = getRegion(p);
      TTrack track = TTrack.getTrack(trackID);
      // if dataset is iad, select data point
      if (iad == dataset) {
        showPlotCoordinates(dataset.getHitIndex());
      	// determine frame number
      	int frame = track.getFrameForData(getXLabel(), getYLabel(), new double[] {dataset.getX(), dataset.getY()});
      	if (frame > -1) {
        	Step step = track.getStep(frame);
      		if (e.isControlDown()) {
      			// add or remove step
      			if (step!=null) {
      				StepSet steps = plotTrackView.trackerPanel.selectedSteps;
      				if (steps.contains(step)) steps.remove(step);
      				else steps.add(step);
          		step.erase();
              trackerPanel.repaint();
          		track.firePropertyChange("steps", null, null); //$NON-NLS-1$
      			}
      		}
      		else {
	      		// set clickedStep so TrackPlottingPanel will select it after displaying video frame
	        	clickedStep = step;
	      		// set video frame to selected data point frame
	        	VideoPlayer player = plotTrackView.trackerPanel.getPlayer();
	        	int stepNumber = player.getVideoClip().frameToStep(frame);
	          player.setStepNumber(stepNumber);
      		}
        	return;
      	}
      }
      else if (region == CartesianInteractive.INSIDE 
      		&& e.getClickCount() == 2
        	&& trackerPanel.isEnabled("data.tool")) { //$NON-NLS-1$ // double click
        dataToolItem.doClick();
      }
      if(showCoordinates) {
        String s = coordinateStrBuilder.getCoordinateString(TrackPlottingPanel.this, e);
        blMessageBox.setText(s);
      }
    }
    
    public void mouseDragged(MouseEvent e) {    	
      mouseEvent = e;
      mouseAction = MOUSE_DRAGGED;
      Point p = e.getPoint();
      region = getRegion(p);
  		setShowCoordinates(region == CartesianInteractive.INSIDE); 
  		if (getInteractive() == null) {
	      if (region != CartesianInteractive.INSIDE) {
	      	setMouseCursor(Cursor.getDefaultCursor());
	      	setMessage(null, 0);
	      }
	      else setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  		}
    	repaint();
    }
    
    public void mouseReleased(MouseEvent e) {
      mouseEvent = e;
      mouseAction = MOUSE_RELEASED;
      TTrack track = TTrack.getTrack(trackID);
    	if (!(track instanceof LineProfile) && getInteractive() != null)
    		setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      if(getCursor() == Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)) {
        blMessageBox.setText(null);
      }
    }
    
    public void mouseClicked(MouseEvent e) {
      mouseEvent = e;
      mouseAction = MOUSE_CLICKED;
    }
    
    // returns the region containing mouse point p
    private int getRegion(Point p) {
      int region = plotAxes.getMouseRegion();
      // double-check region if axes reports INSIDE
      if (region == CartesianInteractive.INSIDE) {
	      int l = getLeftGutter();
	      int r = getRightGutter();
	      int t = getTopGutter();
	      int b = getBottomGutter();
	      Dimension plotDim = getSize();
	      if (p.x < l || p.y < t || p.x > plotDim.width-r || p.y > plotDim.height-b) {
	      	return -1;
	      }
      }
    	return region;
    }
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
      TrackPlottingPanel plot  = (TrackPlottingPanel)obj;
      control.setValue("x_var", plot.getXVariable()); //$NON-NLS-1$      
      control.setValue("y_var", plot.getYVariable()); //$NON-NLS-1$ 
      if (!plot.autoscaleXMin) {
        control.setValue("scaled", true); //$NON-NLS-1$ 
        control.setValue("xmin", plot.getPreferredXMin()); //$NON-NLS-1$ 
      }
      if (!plot.autoscaleXMax) {
        control.setValue("scaled", true); //$NON-NLS-1$ 
        control.setValue("xmax", plot.getPreferredXMax()); //$NON-NLS-1$ 
      }
      if (!plot.autoscaleYMin) {
        control.setValue("scaled", true); //$NON-NLS-1$ 
        control.setValue("ymin", plot.getPreferredYMin()); //$NON-NLS-1$ 
      }
      if (!plot.autoscaleYMax) {
        control.setValue("scaled", true); //$NON-NLS-1$ 
        control.setValue("ymax", plot.getPreferredYMax()); //$NON-NLS-1$ 
      }
      control.setValue("lines", plot.dataset.isConnected()); //$NON-NLS-1$ 
      control.setValue("points", plot.dataset.getMarkerShape()!=Dataset.NO_MARKER); //$NON-NLS-1$ 
      if (!plot.guests.isEmpty()) {
      	String[] guestNames = new String[plot.guests.size()];
      	for (int i=0; i< guestNames.length; i++) {
      		TTrack track = plot.guests.get(i);
      		guestNames[i] = track.getName();
      	}
      	control.setValue("guests", guestNames); //$NON-NLS-1$
      }
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
      TrackPlottingPanel plot  = (TrackPlottingPanel)obj;
      String[] vars = new String[2];
      vars[0] = control.getString("x_var"); //$NON-NLS-1$
      vars[1] = control.getString("y_var"); //$NON-NLS-1$
      // convert legacy variable names
      TTrack track = TTrack.getTrack(plot.trackID);
      for (int i = 0; i < 2; i++) {
        if (vars[i] != null) {
        	if (vars[i].equals("theta") && track instanceof PointMass)  //$NON-NLS-1$
        		vars[i] = "\u03b8"+"r"; //$NON-NLS-1$ //$NON-NLS-2$
        	else if (vars[i].equals("theta"))  //$NON-NLS-1$
        		vars[i] = "\u03b8"; //$NON-NLS-1$
        	else if (vars[i].equals("theta_v"))  //$NON-NLS-1$
        		vars[i] = "\u03b8"+"v"; //$NON-NLS-1$ //$NON-NLS-2$
        	else if (vars[i].equals("theta_a"))  //$NON-NLS-1$
        		vars[i] = "\u03b8"+"a"; //$NON-NLS-1$ //$NON-NLS-2$
        	else if (vars[i].equals("theta_p"))  //$NON-NLS-1$
        		vars[i] = "\u03b8"+"p"; //$NON-NLS-1$ //$NON-NLS-2$
        	else if (vars[i].equals("n") && track instanceof PointMass)  //$NON-NLS-1$
        		vars[i] = "step"; //$NON-NLS-1$
        	else if (vars[i].equals("KE"))  //$NON-NLS-1$
        		vars[i] = "K"; //$NON-NLS-1$
        	else if (vars[i].equals("x-comp"))  //$NON-NLS-1$
        		vars[i] = "x"; //$NON-NLS-1$
        	else if (vars[i].equals("y-comp"))  //$NON-NLS-1$
        		vars[i] = "y"; //$NON-NLS-1$
        	else if (vars[i].equals("x_tail"))  //$NON-NLS-1$
        		vars[i] = "xtail"; //$NON-NLS-1$
        	else if (vars[i].equals("y_tail"))  //$NON-NLS-1$
        		vars[i] = "ytail"; //$NON-NLS-1$
        }
      }
      plot.setXVariable(vars[0]);     
      plot.setYVariable(vars[1]);
      if (control.getBoolean("scaled")) { //$NON-NLS-1$
	      double xmin = control.getDouble("xmin");   //$NON-NLS-1$
	      double xmax = control.getDouble("xmax");   //$NON-NLS-1$
	      double ymin = control.getDouble("ymin");   //$NON-NLS-1$
	      double ymax = control.getDouble("ymax");   //$NON-NLS-1$
	      plot.setPreferredMinMax(xmin, xmax, ymin, ymax, false);
      }
      if (control.getPropertyNames().contains("lines"))  //$NON-NLS-1$
      	plot.dataset.setConnected(control.getBoolean("lines"));  //$NON-NLS-1$
      if (control.getPropertyNames().contains("points")) {  //$NON-NLS-1$
      	if (control.getBoolean("points")) {  //$NON-NLS-1$
      		plot.dataset.setMarkerShape(Dataset.SQUARE);
      	}
      	else plot.dataset.setMarkerShape(Dataset.NO_MARKER);
      }
      String[] guestnames = (String[])control.getObject("guests"); //$NON-NLS-1$
      if (guestnames!=null) {
      	TrackerPanel trackerPanel = plot.trackerPanel;
      	for (String name: guestnames) {
      		TTrack guest = trackerPanel.getTrack(name);
      		plot.addGuest(guest);
      	}
      }
      plot.plotData();
      return obj;
    }
  }

}

