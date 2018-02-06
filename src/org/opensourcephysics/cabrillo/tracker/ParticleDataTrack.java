/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2018  Douglas Brown
 * 
 * Tracker is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Tracker is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Tracker; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston MA 02111-1307 USA or view the license online at
 * <http://www.gnu.org/copyleft/gpl.html>
 * 
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.DataClip;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This is a particle model with steps based on world positions defined in a Data object.
 * The Data object is an "external model" associated with a source (eg path, URL, Tool, null)
 * The Data must define one or more data arrays in matched x-y pairs and may include a time array "t" 
 *
 * @author Douglas Brown
 */
public class ParticleDataTrack extends ParticleModel implements DataTrack {
	
	private static String startupFootprint = "CircleFootprint.FilledCircle#5 outline"; //$NON-NLS-1$
	
	private DataClip dataClip;
	private Data sourceData;
  private double[] xData={0}, yData={0}, tData={0};
  private Point2D[] tracePosition; // used by getNextTracePositions() method
  private int stepCounter;
  private Object dataSource; // may be ParticleDataTrack leader 
  private boolean useDataTime;
  protected String pointName="", modelName=""; //$NON-NLS-1$ //$NON-NLS-2$
  private ArrayList<ParticleDataTrack> morePoints = new ArrayList<ParticleDataTrack>();
  private JMenu pointsMenu, linesMenu, allFootprintsMenu;
  private JButton reloadButton;
  private JMenuItem allColorItem, lineColorItem;
  protected String pendingDataString, prevDataString;
  protected Footprint modelFootprint;
  protected Footprint[] modelFootprints = new Footprint[0];
  protected boolean modelFootprintVisible = false;
  private JCheckBoxMenuItem linesVisibleCheckbox, linesClosedCheckbox, linesBoldCheckbox;
  private JCheckBox autoPasteCheckbox;
  private ActionListener allFootprintsListener, allCircleFootprintsListener;
  private boolean autoPasteEnabled = true;
	
	/**
	 * Public constructor.
	 * 
	 * @param data the Data object
	 * @param source the data source object (null if data is pasted)
	 * @throws Exception if the data does not define x and y-datasets
	 */
	public ParticleDataTrack(Data data, Object source) throws Exception {
		this(source);
		getDataClip().addPropertyChangeListener(this);
  	String name = data.getName();
  	if (name==null || name.trim().equals("")) { //$NON-NLS-1$
  		name = TrackerRes.getString("ParticleDataTrack.New.Name"); //$NON-NLS-1$
  	}
  	name = name.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
		setName(name);
		// next line throws Exception if the data does not define x and y-columns
		setData(data);
	}
	
	/**
	 * Private constructor used by all.
	 * 
	 * @param source the data source
	 */
	private ParticleDataTrack(Object source) {
		dataSource = source;
		tracePosition = new Point2D[] {point};
		tracePtsPerStep = 1;
		
		// set footprint and model footprint
		setFootprint(startupFootprint);
		defaultFootprint = getFootprint();
		// set model footprint only if this is the leader
		if (!(source instanceof ParticleDataTrack)) {
	    modelFootprints = new Footprint[]
	        {MultiLineFootprint.getFootprint("Footprint.Lines"), //$NON-NLS-1$
	    		MultiLineFootprint.getFootprint("Footprint.BoldLines")}; //$NON-NLS-1$
			modelFootprint = modelFootprints[0];
		}
		
		// menu items
	  pointsMenu = new JMenu();
	  linesMenu = new JMenu();

    allFootprintsListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String footprintName = e.getActionCommand();
        if (getFootprint().getName().equals(footprintName)) return;
        
        XMLControl control = new XMLControlElement(new TrackProperties(ParticleDataTrack.this));
        
        // set footprint
      	for (ParticleDataTrack next: allPoints()) {
      		next.setFootprint(footprintName);
    			next.erase();
      	}
      	// post edit
        Undo.postTrackDisplayEdit(ParticleDataTrack.this, control);
      	trackerPanel.repaint();
      }
    };
    allCircleFootprintsListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	
        XMLControl control = new XMLControlElement(new TrackProperties(ParticleDataTrack.this));

        // set footprint
        String footprintName = e.getActionCommand();
      	for (ParticleDataTrack next: allPoints()) {
      		next.setFootprint(footprintName);
      	}
      	
      	// set circle properties
      	CircleFootprint cfp = (CircleFootprint)getFootprint();
      	cfp.showProperties(ParticleDataTrack.this);
      	for (ParticleDataTrack next: allPoints()) {
    			CircleFootprint cf = (CircleFootprint)next.getFootprint(); 
    			cf.setProperties(cfp.getProperties());
    			next.erase();
      	}
      	// post edit
        Undo.postTrackDisplayEdit(ParticleDataTrack.this, control);
      	trackerPanel.repaint();
      }
    };

    allColorItem = new JMenuItem();
		allColorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Color color = getColor();
      	Color newColor = chooseColor(color, TrackerRes.getString("TTrack.Dialog.Color.Title")); //$NON-NLS-1$
        if (newColor!=color) {
        	XMLControl control = new XMLControlElement(new TrackProperties(ParticleDataTrack.this));
        	for (ParticleDataTrack next: allPoints()) {
        		next.setColor(newColor);
        	}
        	getLeader().setLineColor(newColor);
          Undo.postTrackDisplayEdit(ParticleDataTrack.this, control);
        }
      }
    });
		lineColorItem = new JMenuItem();
		lineColorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Color color = getColor();
      	Color newColor = chooseColor(color, TrackerRes.getString("TTrack.Dialog.Color.Title")); //$NON-NLS-1$
        if (newColor!=color) {
        	XMLControl control = new XMLControlElement(new TrackProperties(ParticleDataTrack.this));
        	getLeader().setLineColor(newColor);         
          Undo.postTrackDisplayEdit(ParticleDataTrack.this, control);
        }
      }
    });
		linesVisibleCheckbox = new JCheckBoxMenuItem();
		linesVisibleCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (refreshing) return;
      	modelFootprintVisible = linesVisibleCheckbox.isSelected();
      	erase();
      	trackerPanel.repaint();
      }
    });
		linesClosedCheckbox = new JCheckBoxMenuItem();
		linesClosedCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (refreshing) return;
        Footprint f = getModelFootprint();
        if (f instanceof MultiLineFootprint) {
        	((MultiLineFootprint)f).setClosed(linesClosedCheckbox.isSelected());
        	erase();
        	trackerPanel.repaint();
        }
      }
    });
		linesBoldCheckbox = new JCheckBoxMenuItem();
		linesBoldCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (refreshing) return;
      	MultiLineFootprint mlf = (MultiLineFootprint)getModelFootprint();
      	Color c = mlf.getColor();
      	boolean closed = mlf.isClosed();
      	if (linesBoldCheckbox.isSelected()) {
      		getLeader().setModelFootprint("Footprint.BoldLines"+"#"+closed); //$NON-NLS-1$ //$NON-NLS-2$
      	}
      	else {
      		getLeader().setModelFootprint("Footprint.Lines"+"#"+closed); //$NON-NLS-1$ //$NON-NLS-2$
      	}
      	getModelFootprint().setColor(c);
      	erase();
      	trackerPanel.repaint();
      }
    });
		allFootprintsMenu = new JMenu();
    
	}
	
	/**
	 * Private constructor for making additional point tracks.
	 * 
	 * @param data Object[] {String name, double[2][] xyData}
	 * @param parent the parent
	 */
	private ParticleDataTrack(Object[] data, ParticleDataTrack parent) {
		this(parent);
		parent.morePoints.add(this);
		dataClip = parent.getDataClip();
		getDataClip().addPropertyChangeListener(this);
		setPointName(data[0].toString());
		setColor(parent.getColor());
		Footprint f = parent.getFootprint();
		String fname = f.getName();
		setFootprint(fname);
		if (f instanceof CircleFootprint) {
			CircleFootprint cf = (CircleFootprint)f;
			CircleFootprint cfnew = (CircleFootprint)getFootprint(); 
			cfnew.setProperties(cf.getProperties());
		}
		double[][] xyData = (double[][])data[1];
		setCoreData(xyData, true);
	}
	
	/**
	 * Private constructor for XMLLoader.
	 * 
	 * @param data the Data object
	 */
	private ParticleDataTrack(double[][] coreData, ArrayList<Object[]> pointData) {
		this(null);
		getDataClip().addPropertyChangeListener(this);
		try {
			setCoreData(coreData, true);
		} catch (Exception e) {}		
		
		for (int i = 0; i< pointData.size(); i++) {
			// get the new data
			Object[] next = pointData.get(i);				
			double[][] xyArray = (double[][])next[1];				
			ParticleDataTrack target = new ParticleDataTrack(next, this);
			target.setTrackerPanel(trackerPanel);
			if (trackerPanel!=null) {
				trackerPanel.addTrack(target);
			}
			
			// set target's data
			target.setCoreData(xyArray, true);
		}
	}
	
	@Override
  protected void delete(boolean postEdit) {
    if (isLocked() && !isDependent()) return;
    if (trackerPanel != null) {
    	trackerPanel.setSelectedPoint(null);
      trackerPanel.selectedSteps.clear();
			trackerPanel.getTFrame().removePropertyChangeListener("windowfocus", this); //$NON-NLS-1$

      // handle case when this is the origin of current reference frame
    	ImageCoordSystem coords = trackerPanel.getCoords();
      if (coords instanceof ReferenceFrame && 
      				((ReferenceFrame)coords).getOriginTrack() == this) {
        // set coords to underlying coords
        coords = ( (ReferenceFrame) coords).getCoords();
      	trackerPanel.setCoords(coords);
      }    	
    }
    if (postEdit) {
    	Undo.postTrackDelete(this); // posts undoable edit
    }
    for (TTrack track: morePoints) {
    	track.delete(false); // don't post undoable edit
    }
    morePoints.clear();
    super.delete(false); // don't post undoable edit
  }
	
	protected void setModelFootprint(String name) {
		if (this!=getLeader()) {
			getLeader().setModelFootprint(name);
			return;
		}
  	String props = null;
  	int n = name.indexOf("#"); //$NON-NLS-1$
  	if (n>-1) {
  		props = name.substring(n+1);
  		name = name.substring(0, n);
  	}
    for (int i = 0; i < modelFootprints.length; i++) {
      if (name.equals(modelFootprints[i].getName())) {
        modelFootprint = modelFootprints[i];
        if (props!=null && modelFootprint instanceof MultiLineFootprint) {
    			MultiLineFootprint mlf = (MultiLineFootprint)modelFootprint;
    			try {
						boolean closed = Boolean.parseBoolean(props);
						mlf.setClosed(closed);
					} catch (Exception e) {
					}
        }
        break;
      }
    }
	}
	
	protected Footprint getModelFootprint() {
		return getLeader().modelFootprint;
	}
	
	protected String getModelFootprintName() {
		String s = getModelFootprint().getName();
		if (getModelFootprint() instanceof MultiLineFootprint) {
			MultiLineFootprint mlf = (MultiLineFootprint)getModelFootprint();
			s+="#"+mlf.isClosed(); //$NON-NLS-1$
		}
		return s;
	}

	/**
	 * Returns a menu with items that control this track.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	public JMenu getMenu(TrackerPanel trackerPanel) {
		if (getLeader()!=this) {
			return getPointMenu(trackerPanel);
		}
		
		JMenu menu = super.getMenu(trackerPanel);
		menu.setIcon(getIcon(21, 16, "model")); //$NON-NLS-1$
		menu.removeAll();
		
		// refresh points menu
		pointsMenu.setText(TrackerRes.getString("ParticleDataTrack.Menu.Points")); //$NON-NLS-1$
		pointsMenu.removeAll();		
		// add point menus
		ArrayList<ParticleDataTrack> points = allPoints();
		for (ParticleDataTrack next: points) {
			JMenu pointMenu = next.getPointMenu(trackerPanel);
			pointsMenu.add(pointMenu);
		}

		// refresh lines menu
		refreshing = true;
		if (points.size()>1) {
	    lineColorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
	    linesVisibleCheckbox.setText(visibleItem.getText());
	    linesVisibleCheckbox.setSelected(modelFootprintVisible);
	    linesClosedCheckbox.setText(TrackerRes.getString("ParticleDataTrack.Checkbox.Closed")); //$NON-NLS-1$
	    linesClosedCheckbox.setSelected(getModelFootprint() instanceof MultiLineFootprint
	    		&& ((MultiLineFootprint)getModelFootprint()).isClosed());
			linesMenu.setText(TrackerRes.getString("ParticleDataTrack.Menu.Lines")); //$NON-NLS-1$
			linesBoldCheckbox.setText(TrackerRes.getString("CircleFootprint.Dialog.Checkbox.Bold")); //$NON-NLS-1$
			linesBoldCheckbox.setSelected(getModelFootprint().getName().indexOf("Bold")>-1); //$NON-NLS-1$
			linesMenu.removeAll();		
			// add pertinent items
			linesMenu.add(lineColorItem);
			linesMenu.addSeparator();
			linesMenu.add(linesVisibleCheckbox);
			linesMenu.add(linesBoldCheckbox);
			if (points.size()>2) {
				linesMenu.add(linesClosedCheckbox);
			}
		}
		refreshing = false;
		
		// refresh allFootprint menu
    allFootprintsMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
    allFootprintsMenu.removeAll();
    Footprint[] fp = getFootprints();
    JMenuItem item;
    for (int i = 0; i < fp.length; i++) {
      item = new JMenuItem(fp[i].getDisplayName(), fp[i].getIcon(21, 16));
      item.setActionCommand(fp[i].getName());
      if (fp[i] instanceof CircleFootprint) {
      	item.setText(fp[i].getDisplayName()+"..."); //$NON-NLS-1$
      	item.addActionListener(allCircleFootprintsListener);
      }
      else {
      	item.addActionListener(allFootprintsListener);
      }
      if (fp[i]==footprint) {
        item.setBorder(BorderFactory.createLineBorder(item.getBackground().darker()));
      }
      allFootprintsMenu.add(item);
    }

    allColorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
    
		// assemble menu
		menu.add(modelBuilderItem);
		menu.addSeparator();
		menu.add(descriptionItem);
		menu.addSeparator();
		menu.add(allColorItem);
		menu.add(allFootprintsMenu);
		menu.addSeparator();
		menu.add(pointsMenu);
		if (points.size()>1) {
			menu.add(linesMenu);
		}
		menu.addSeparator();
		menu.add(visibleItem);
//		menu.addSeparator();
//		menu.add(dataBuilderItem);
		menu.addSeparator();
		menu.add(deleteTrackItem);
		return menu;
	}
	
	/**
	 * Returns a menu with items associated with this track's point properties.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return a menu
	 */
	protected JMenu getPointMenu(TrackerPanel trackerPanel) {
    // prepare menu items
    colorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
    footprintMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
    velocityMenu.setText(TrackerRes.getString("PointMass.MenuItem.Velocity")); //$NON-NLS-1$
    accelerationMenu.setText(TrackerRes.getString("PointMass.MenuItem.Acceleration")); //$NON-NLS-1$
		JMenu menu = getLeader()!=this? super.getMenu(trackerPanel): new JMenu();
		menu.setText(getPointName());
    menu.setIcon(getFootprint().getIcon(21, 16));
		menu.removeAll();
		menu.add(colorItem);
		menu.add(footprintMenu);
		menu.addSeparator();
		menu.add(velocityMenu);
		menu.add(accelerationMenu);		
    if (trackerPanel.isEnabled("model.stamp")) { //$NON-NLS-1$
			menu.addSeparator();
			menu.add(stampItem);
    }
		return menu;
	}
	
  @Override
	public Icon getIcon(int w, int h, String context) {
  	// for point context, return footprint icon
		if (context.contains("point")) { //$NON-NLS-1$
			return getFootprint().getIcon(w, h);
		}
		// for other contexts, return combination icon
		ArrayList<ShapeIcon> shapeIcons = new ArrayList<ShapeIcon>();
		for (TTrack track: getLeader().allPoints()) {
			Icon icon = track.getFootprint().getIcon(w, h);
			if (icon instanceof ShapeIcon) {
				shapeIcons.add((ShapeIcon)icon);
			}
		}
		return new ComboIcon(shapeIcons);
	}
	
  @Override
  public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
  	// create reload button here to insure that TFrame is defined
  	if (getLeader().reloadButton==null) {
  		final TFrame frame = trackerPanel.getTFrame();
  		frame.checkClipboardListener();
  		final int h = TTrackBar.getTrackbar(trackerPanel).toolbarComponentHeight;
  		getLeader().reloadButton = new JButton() {
      	public Dimension getMaximumSize() {
      		Dimension dim = super.getMaximumSize();
      		dim.height = h;
      		return dim;
      	}
  		};
  		
  		getLeader().reloadButton.setEnabled(false);
  		getLeader().reloadButton.setOpaque(false);
  		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
  		Border line = BorderFactory.createLineBorder(Color.GRAY);
  		getLeader().reloadButton.setBorder(BorderFactory.createCompoundBorder(line, space));
  		
  		getLeader().reloadButton.addActionListener(new ActionListener() {
  			public void actionPerformed(ActionEvent e) {
  				int tab = frame.getSelectedTab();
  				TrackerPanel panel = frame.getTrackerPanel(tab);
  				if (panel!=null) {
  					if (getLeader().dataSource==null) { // data is pasted
  						TActions.getAction("paste", panel).actionPerformed(null); //$NON-NLS-1$
  					}
  					else if (getLeader().dataSource instanceof String) { // data is from a file
  						panel.importData(getLeader().dataSource.toString(), null);
  					}
  					getLeader().prevDataString = getLeader().pendingDataString;
  					getLeader().reloadButton.setEnabled(false);
  					TTrackBar.getTrackbar(panel).refresh();
  				}
  			}			
  		});
  		frame.addPropertyChangeListener("windowfocus", getLeader()); //$NON-NLS-1$
  	}
  	if (autoPasteCheckbox==null) {
  		// also create autoPasteCheckbox
  		autoPasteCheckbox = new JCheckBox();
  		autoPasteCheckbox.setOpaque(false);
  		autoPasteCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
  		autoPasteCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setAutoPasteEnabled(autoPasteCheckbox.isSelected());
					if (ParticleDataTrack.this.trackerPanel==null) return;
					TFrame frame = ParticleDataTrack.this.trackerPanel.getTFrame();
					if (frame==null) return;
		    	if (isAutoPasteEnabled()) {
		      	ClipboardListener clipboardListener = frame.getClipboardListener();
		        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		        Transferable data = clipboard.getContents(null);
		        if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
		          try {
			        	String s = (String)data.getTransferData(DataFlavor.stringFlavor);
		            if (ParticleDataTrack.getImportableDataName(s)!=null) {
				      		clipboardListener.processContents(data);
//		        	    Action paste = TActions.getAction("paste", ParticleDataTrack.this.trackerPanel); //$NON-NLS-1$
//		        			paste.actionPerformed(null);
		            }
		          }
		          catch (Exception ex) {          	
		          }
		        }
  					getLeader().prevDataString = getLeader().pendingDataString;
  					getLeader().reloadButton.setEnabled(false);
  					TTrackBar.getTrackbar(ParticleDataTrack.this.trackerPanel).refresh();

		    	}

//		    	else if (frame.clipboardListener!=null && !frame.clipboardListener.hasAutoPasteTargets()) {
//		    		frame.clipboardListener.end();
//		    		frame.clipboardListener = null;
//		    	}
		    	if (ParticleDataTrack.this.trackerPanel.getSelectedTrack()==ParticleDataTrack.this) {
				    TTrackBar trackbar = TTrackBar.getTrackbar(ParticleDataTrack.this.trackerPanel);
				    trackbar.refresh();
		    	}
				}  			
  		});
  	}
  	getLeader().reloadButton.setText(getLeader().dataSource==null?
  			TrackerRes.getString("ParticleDataTrack.Button.Paste.Text"): //$NON-NLS-1$
  			TrackerRes.getString("ParticleDataTrack.Button.Reload.Text")); //$NON-NLS-1$
    ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
    if (trackerPanel.getSelectedPoint()==null) {
	    list.remove(massLabel);
	    list.remove(massField);
	    if (getSource()==null) { // data was pasted
		    autoPasteCheckbox.setText(TrackerRes.getString("TMenuBar.MenuItem.AutoPasteData.Text")); //$NON-NLS-1$
		    autoPasteCheckbox.setSelected(isAutoPasteEnabled());
		    list.add(autoPasteCheckbox);
		    list.add(mSeparator);
	    }
	    if (getLeader().reloadButton.isEnabled()) {
	    	list.add(getLeader().reloadButton);
	    }
    }
    return list;
  }
  
	/**
	 * Determines if autopaste is enabled for this track.
	 * 
	 * @return true if autopaste is enabled
	 */
  protected boolean isAutoPasteEnabled() {
  	// only the leader is autopaste enabled
  	return autoPasteEnabled;
  }
  
	/**
	 * Sets the autoPasteEnabled flag for this track.
	 * 
	 * @param enable true to enable autopasting
	 */
  protected void setAutoPasteEnabled(boolean enable) {
  	// only the leader is autopaste enabled  	
  	getLeader().autoPasteEnabled = enable;
  }
  
	/**
	 * Gets the point name. The point name is appended to the 
	 * lead track name for most buttons and dropdowns.
	 * 
	 * @return the point name
	 */
  protected String getPointName() {
  	if (pointName==null) {
  		pointName = ""; //$NON-NLS-1$
  	}
  	if ("".equals(pointName)) { //$NON-NLS-1$
  		ArrayList<ParticleDataTrack> pts = getLeader().allPoints();
  		if (pts.size()>1) {
				for (int i=0; i<pts.size(); i++) {
					if (pts.get(i)==this) {
						pointName = alphabet.substring(i, i+1);
					}
				}
  		}
  	}
  	// count duplicates
  	int count = 0, i = 0;
  	for (ParticleDataTrack next: getLeader().allPoints()) {
			if (pointName.equals(next.pointName)) {
				count++;
				if (next==this) {
					i = count;
				}
			}
		}
  	if (count>1) {
  		return pointName+" "+i; //$NON-NLS-1$
  	}
  	return pointName;
  }

	/**
	 * Sets the point name. The point name is appended to the 
	 * leader's track name to name the point.
	 * 
	 * @param newName the point name
	 */
  protected void setPointName(String newName) {
  	if (newName==null) newName = ""; //$NON-NLS-1$
  	// strip parentheses and brackets from name
  	int n = newName.indexOf("("); //$NON-NLS-1$
  	if (n>-1) {
  		newName =newName.substring(0, n).trim();
  	}
  	else {
    	n = newName.indexOf("["); //$NON-NLS-1$
    	if (n>-1) {
    		newName =newName.substring(0, n).trim();
    	}
  	}
  	pointName = newName;
  	boolean changed = false;
  	for (ParticleDataTrack next: getLeader().allPoints()) {
  		String fullName = next.getFullName();
  		changed = changed || !fullName.equals(next.name);
			next.name = fullName;
		}
  	if (changed) {
  		support.firePropertyChange("name", null, null); //$NON-NLS-1$
  	}
  }

	/**
	 * Gets a list of all points in this track.
	 * 
	 * @return the points
	 */
  protected ArrayList<ParticleDataTrack> allPoints() {
  	ArrayList<ParticleDataTrack> points = new ArrayList<ParticleDataTrack>();
  	points.add(this);
  	if (morePoints!=null) {
  		points.addAll(morePoints);
  	}
  	return points;
  }

	/**
	 * Sets the colors of all points and line in this track. Used for undo/redo.
	 * 
	 * @param colors array of colors
	 */
  protected void setAllColors(Color[] colors) {
  	ArrayList<ParticleDataTrack> points = allPoints();
  	// array length may not match points size if new data has been loaded
  	int len = Math.min(points.size(), colors.length-1);
  	for (int i=0; i<len; i++) {
  		points.get(i).setColor(colors[i]);
  	}
  	// set the color of all model footprints so changing to/from bold always shows correct color
  	for (int i=0; i< modelFootprints.length; i++) {
	  	modelFootprints[i].setColor(colors[colors.length-1]);  		
  	}
  	erase();
  	trackerPanel.repaint();
  }

	/**
	 * Sets the footprints of all points in this track. Used for undo/redo.
	 * 
	 * @param footprints array of footprints
	 */
  protected void setAllFootprints(String[] footprints) {
  	ArrayList<ParticleDataTrack> points = allPoints();
  	// array length may not match points size if new data has been loaded
  	int len = Math.min(points.size(), footprints.length-1);
  	for (int i=0; i<len; i++) {
  		points.get(i).setFootprint(footprints[i]);
  	}
  	setModelFootprint(footprints[footprints.length-1]);
  	erase();
  	trackerPanel.repaint();
  }

	/**
	 * Gets the full name (model & point) for this track.
	 * 
	 * @return the full name
	 */
  public String getFullName() {
  	return getLeader().modelName+" "+getPointName(); //$NON-NLS-1$
  }
  
  @Override
  public String getName(String context) {
  	// point context: full name (eg "example A" or "example elbow")
  	if (context.contains("point")) { //$NON-NLS-1$
  		return getName();
  	}
  	// for other contexts, return modelName only (eg "example")
  	return getLeader().modelName;  	  	
  }
  
  @Override
  public void setName(String newName) {  	
  	// set the model name if this is the leader
  	if (getLeader()==this) {
    	// ignore if newName equals current full name
    	if (getFullName().equals(newName)) return;
  		modelName = newName;
  		// set name of all points
  		for (ParticleDataTrack next: allPoints()) {
  			next.name = next.getFullName();
  		}
  	}
  	// do nothing for other points
  }

  @Override
  public void setColor(Color color) {
  	super.setColor(color);
  	if (getLeader()!=this) {
  		getLeader().support.firePropertyChange("color", null, color); //$NON-NLS-1$
  	}
  }
  
	/**
	 * Sets the line color for the modelFootprint.
	 * 
	 * @param color the color
	 */
  public void setLineColor(Color color) {
  	if (getLeader()==this) {
  		modelFootprint.setColor(color);
  		support.firePropertyChange("color", null, color); //$NON-NLS-1$
  		erase();
  		if (trackerPanel!=null) {
  			trackerPanel.repaint();
  		}
  	}
  }
  
  @Override
  public void setFootprint(String name) {
  	super.setFootprint(name);
  	if (getLeader()!=this) {
  		getLeader().support.firePropertyChange("footprint", null, getLeader().footprint); //$NON-NLS-1$
  	}
  }
	/**
	 * Returns the lead track (index=0)
	 * 
	 * @return the leader (may be this track)
	 */
	public ParticleDataTrack getLeader() {
		if (dataSource instanceof ParticleDataTrack) {
			return (ParticleDataTrack)dataSource;
		}
		return this;
	}
	
	/**
	 * Sets the Data. Data must define columns "x" and "y".
	 * If time data is included, it is assumed to be in seconds.
	 * 
	 * @param data the Data object
	 * @throws Exception if the data does not define x and y-columns
	 */
	public void setData(Data data) throws Exception {
		OSPLog.finer("Setting new data"); //$NON-NLS-1$
		
		// the following line throws an exception if (x, y) data is not found
		ArrayList<Object[]> pointData = getPointData(data);
		sourceData = data;
		
		// save current time array for comparison
		double[] tPrev = tData;
		// set core {x,y,t} data for the leader (this)
		Object[] coreData = pointData.get(0);
		setPointName(coreData[0].toString());
		double[][] xyArray = (double[][])coreData[1];				
		double[] timeArray = getTimeData(data);				
		if (timeArray!=null && xyArray[0].length!=timeArray.length) {
			throw new Exception("Time data has incorrect array length"); //$NON-NLS-1$
		}
		
		double[][] coreArray = new double[][] {xyArray[0], xyArray[1], timeArray};		
		setCoreData(coreArray, true);
		
		// set {x,y} for additional points
		for (int i = 1; i<pointData.size(); i++) {
			// get the new data
			Object[] next = pointData.get(i);				
			coreArray = (double[][])next[1];				
				
			// if needed, create new track
			if (i>morePoints.size()) {
				ParticleDataTrack target = new ParticleDataTrack(next, this);
				target.setTrackerPanel(trackerPanel);
				if (trackerPanel!=null) {
					trackerPanel.addTrack(target);
				}
			}
			else {
				ParticleDataTrack target = morePoints.get(i-1);
				// set target's data
				target.setCoreData(coreArray, true);
				// set target's pointName
				target.setPointName(next[0].toString());
			}
		}
		// delete surplus points, last one first
		for (int i=morePoints.size()-1; i>=pointData.size()-1; i--) {
			ParticleDataTrack next = morePoints.remove(i);
			next.delete(false); // don't post undoable edit
		}
		// reset point names of all tracks to refresh full names
		for (ParticleDataTrack next: allPoints()) {
			next.setPointName(next.pointName);
		}
		// check for changed time data 
		if (tData!=null && tData.length>1 && tPrev!=null && tPrev.length>1 && getVideoPanel()!=null) {
			boolean changed = tData[0]!=tPrev[0] || (tData[1]-tData[0])!=(tPrev[1]-tPrev[0]);
			VideoPlayer player = getVideoPanel().getPlayer();
			boolean isDataTime = player.getClipControl().getTimeSource()==this;
			if (changed && isDataTime && functionPanel!=null) {
				ParticleDataTrackFunctionPanel dtPanel = (ParticleDataTrackFunctionPanel)functionPanel;
				dtPanel.refreshTimeSource();
			}
		}
	}
	
	/**
	 * Gets the model data. This can return null if loaded from XMLControl.
	 * 
	 * @return the data (may return null)
	 */
	public Data getData() {
		return sourceData;
	}
	
	/**
	 * Gets the data source.
	 * 
	 * @return the source (may return null)
	 */
	public Object getSource() {
		return dataSource;
	}
	
	/**
	 * Sets the data source.
	 * 
	 * @return the source (may return null)
	 */
	public void setSource(Object source) {
		dataSource = source;
	}
	
	/**
	 * Gets the data clip.
	 * 
	 * @return the data clip
	 */
	public DataClip getDataClip() {
		if (dataClip==null) {
			dataClip = new DataClip();
		}
		return dataClip;
	}
	
	/**
	 * Gets the trackerPanel video clip.
	 * 
	 * @return the video clip (null if not yet added to TrackerPanel)
	 */
	public VideoClip getVideoClip() {
		if (trackerPanel==null) {
			return null;
		}
		return trackerPanel.getPlayer().getVideoClip();
	}
	
	@Override
	public boolean isVisible() {
		if (getLeader()!=this) {
			return getLeader().isVisible();
		}
		return super.isVisible();
	}
	
	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		if (getLeader()!=this && vis!=getLeader().isVisible()) {
			getLeader().setVisible(vis);
		}
		for (TTrack next: morePoints) {
			next.setVisible(vis);
		}
	}
	
	/**
	 * Gets the end data index.
	 * 
	 * @return the end index
	 */
  public int getEndIndex() {
  	// determine the end index corresponding to the end frame
  	int stepCount = getEndFrame()-getStartFrame();
  	int index = dataClip.getStartIndex() + stepCount*dataClip.getStride();
  	return Math.min(index, dataClip.getDataLength()-1);
  }

	/**
	 * Gets the (start) time for a given step.
	 * 
	 * @return the time
	 */
  public double getStepTime(int step) {
  	if (tData==null) return Double.NaN;
  	int index = getDataClip().stepToIndex(step);
  	if (index<tData.length) return tData[index];
  	return Double.NaN;
  }
  
	/**
	 * Determines if time is defined by the Data.
	 * 
	 * @return true if time data is available
	 */
  public boolean isTimeDataAvailable() {
  	if (dataClip==null || getVideoClip()==null) return false;
  	int n = Math.max(dataClip.getStride(), dataClip.getStartIndex());
  	return tData!=null && tData.length>n;
  }
  
	/**
	 * Gets the data-based video start time in seconds if available
	 * 
	 * @return the start time (assumed in seconds), or Double.NaN if unavailable
	 */
  public double getVideoStartTime() {
  	if (!isTimeDataAvailable()) return Double.NaN;
	  double t0 = tData[getDataClip().getStartIndex()];
	  double duration = getFrameDuration();
		return t0-duration*(getStartFrame()-getVideoClip().getStartFrameNumber());  	
  }

	/**
	 * Gets the data-based frame duration in seconds if available
	 * 
	 * @return the frame duration (assumed in seconds), or Double.NaN if unavailable
	 */
  public double getFrameDuration() {
  	if (!isTimeDataAvailable()) return Double.NaN;
		return tData[getDataClip().getStride()]-tData[0];
  }

  @Override
	public void setStartFrame(int n) {
		if (n==getStartFrame()) return;
		n = Math.max(n, 0); // not less than zero
		VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		int end = clip.getLastFrameNumber();
		n = Math.min(n, end); // not greater than clip end
		startFrame = n;
		refreshInitialTime();
    adjustVideoClip();
		lastValidFrame = -1;
		for (ParticleDataTrack next: morePoints) {
			next.lastValidFrame = -1;
		}
		trackerPanel.repaint();
		firePropertyChange("startframe", null, getStartFrame()); //$NON-NLS-1$
		if (trackerPanel!=null) {
			trackerPanel.getModelBuilder().refreshSpinners();
			int stepNum = clip.frameToStep(startFrame);
			trackerPanel.getPlayer().setStepNumber(stepNum);
		}
	}
  
  @Override
	public void setEndFrame(int n) {
  	// set dataclip length
  	getDataClip().setClipLength((n-getStartFrame()+1));
  	trackerPanel.getModelBuilder().refreshSpinners();
	}
  
  @Override
	protected void refreshInitialTime() {
  	if (trackerPanel==null || trackerPanel.getPlayer()==null) {
	  	super.refreshInitialTime();
  		return;			
  	}
		if (!ClipControl.isTimeSource(this) || !isTimeDataAvailable()) {
	  	super.refreshInitialTime();
  		return;			
		}
		
		// this DataTrack is the current time source, so set it again to refresh values
		ClipControl clipControl = trackerPanel.getPlayer().getClipControl();
		clipControl.setTimeSource(this); // refreshes start time and frame duration

		// refresh init editor to show data start time
		Parameter param = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		double tZero = tData[getDataClip().getStartIndex()];
		String t = timeFormat.format(tZero);
		if (!timeFormat.format(param.getValue()).equals(t)) {
			boolean prev = refreshing;
			refreshing = true;
			getInitEditor().setExpression("t", t, false); //$NON-NLS-1$
			refreshing = prev;
		}
	}

  @Override
  public int getStartFrame() {
  	if (getLeader()!=this) {
  		return getLeader().getStartFrame();
  	}
		return startFrame;
	}
	
  @Override
	public int getEndFrame() {
  	// determine end frame based on start frame and clip length
  	int clipEnd = getStartFrame()+getDataClip().getClipLength()-1;
  	int videoEnd = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
  	return Math.min(clipEnd, videoEnd);
	}
  
	@Override
	Point2D[] getNextTracePositions() {
		stepCounter++;
		int index = getDataIndexAtVideoStepNumber(stepCounter);
		if (index<0 || index>=xData.length || index>=yData.length) {
			return null;
		}
    point.setLocation(xData[index], yData[index]);
		return tracePosition;
	}
	
	/**
	 * Converts video step number to data index.
	 * 
	 * @param videoStepNumber
	 * @return the data index, or -1 if none
	 */
	protected int getDataIndexAtVideoStepNumber(int videoStepNumber) {
		VideoClip vidClip = trackerPanel.getPlayer().getVideoClip();
		DataClip dataClip = getDataClip();
		int len = dataClip.getAvailableClipLength();
		int frameNum = vidClip.stepToFrame(videoStepNumber);
		int dataStepNumber = frameNum-getStartFrame();
		boolean validData = dataStepNumber>=0 && dataStepNumber<len;
		int index = getDataClip().stepToIndex(dataStepNumber);
		return validData? index: -1;
	}
	
	@Override
  public void setColorToDefault(int index) {
  	super.setColorToDefault(index);
		for (TTrack next: morePoints) {
			next.setColor(this.getColor());
		}
		// set modelFootprint color
		getModelFootprint().setColor(this.getColor());
  }

	@Override
	protected void setTrackerPanel(TrackerPanel panel) {
		if (panel==null && trackerPanel!=null) {
  		trackerPanel.getTFrame().checkClipboardListener();
		}
		super.setTrackerPanel(panel);
		for (TTrack next: morePoints) {
			next.setTrackerPanel(panel);
		}
		if (panel==null) {
			return;
		}
		
		VideoClip videoClip = panel.getPlayer().getVideoClip();
		int length = videoClip.getLastFrameNumber()-videoClip.getFirstFrameNumber()+1;
		dataClip.setClipLength(Math.min(length, dataClip.getClipLength()));
		firePropertyChange("videoclip", null, null); //$NON-NLS-1$
		if (useDataTime) {
			panel.getPlayer().getClipControl().setTimeSource(this);
			firePropertyChange("timedata", null, null); //$NON-NLS-1$			
		}
	}
	
	public DatasetManager getData(TrackerPanel panel) {
		DatasetManager data = super.getData(panel);
		return data;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		super.propertyChange(e);
		// listen for changes to the video
		if (e.getPropertyName().equals("video")) { //$NON-NLS-1$
//			if (e.getPropertyName().equals("frameshift")) { //$NON-NLS-1$
////				int frameshift = (Integer)e.getNewValue();
//				VideoClip videoClip = getVideoPanel().getPlayer().getVideoClip();
//
//				int startFrame = getStartFrame();
//				startFrame = Math.max(startFrame, videoClip.getFirstFrameNumber());
//				startFrame = Math.min(startFrame, videoClip.getLastFrameNumber());
//						
//				setStartFrame(startFrame);
//			}
			firePropertyChange("videoclip", null, null); //$NON-NLS-1$
	    lastValidFrame = -1;
	    repaint();
		}
		// listen for changes to the dataclip
		else if (e.getSource()==dataClip) {
			refreshInitialTime();
	    adjustVideoClip();
			firePropertyChange("dataclip", null, null); //$NON-NLS-1$
	    lastValidFrame = -1;
	    repaint();
		}
		// listen for clipboard changes
		else if (e.getPropertyName().equals("windowfocus") && trackerPanel!=null //$NON-NLS-1$
				&& trackerPanel.getTFrame()!=null 
				&& trackerPanel==trackerPanel.getTFrame().getTrackerPanel(trackerPanel.getTFrame().getSelectedTab())) {
			if (this==getLeader()) {
				// get current data string and compare with previous
				String dataString = null;
				if (dataSource==null) {  // data was pasted
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    Transferable data = clipboard.getContents(null);
			    if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			    	try {
							dataString = (String)data.getTransferData(DataFlavor.stringFlavor);
						} catch (Exception ex) {}
			    }
				}
				else if (dataSource instanceof String) { // data loaded from a file
					dataString = ResourceLoader.getString(dataSource.toString());
				}
				if (dataString!=null) {
					setPendingDataString(dataString);
				}
			}
		}
	}
	
	protected void setPendingDataString(String dataString) {
		String dataName = getImportableDataName(dataString);
		boolean dataChanged = modelName.equals(dataName) && !dataString.equals(prevDataString);
		pendingDataString = dataString;
		reloadButton.setEnabled(dataChanged);
		if (trackerPanel.getSelectedTrack() instanceof ParticleDataTrack
				&& allPoints().contains(trackerPanel.getSelectedTrack())) {
			TTrackBar.getTrackbar(trackerPanel).refresh();
		}
	}
	
	@Override
	protected void initializeFunctionPanel() {
		// create panel
		functionPanel = new ParticleDataTrackFunctionPanel(this);
		// create mass and initial time parameters
		createTimeParameter();
	}
	
  @Override
	protected void reset() {
		// clear existing steps
		for (int i=0; i<steps.array.length; i++) {
			Step step = steps.getStep(i);
			if (step!=null) {
				step.erase();
			}
			steps.setStep(i, null);
		}
		
		// get coordinate system
		ImageCoordSystem coords = trackerPanel.getCoords();
    // get underlying coords if appropriate
    boolean useDefault = isUseDefaultReferenceFrame();
    while (useDefault && coords instanceof ReferenceFrame) {
      coords = ( (ReferenceFrame) coords).getCoords();
    }
    
		// get data index and firstFrameInVideoClip
		VideoClip vidClip = getVideoClip();
	  int firstFrameInVideoClip = vidClip.getStartFrameNumber();	  
		int index = getDataIndexAtVideoStepNumber(0); // index will be -1 if none
		if (index>-1) {
	    point.setLocation(xData[index], yData[index]);
			AffineTransform transform = coords.getToImageTransform(firstFrameInVideoClip);
		  transform.transform(point, point);
		}
	  
  	// mark a step at firstFrameInVideoClip unless dataclip length is zero
  	steps.setLength(firstFrameInVideoClip+1);
  	for (int i = 0; i<steps.array.length; i++) {
  		if (i<firstFrameInVideoClip || index==-1)
  			steps.setStep(i, null);
  		else {
  			PositionStep step = createPositionStep(this, i, point.getX(), point.getY());
    		step.setFootprint(getFootprint());	  			
        steps.setStep(i, step);
        refreshData(data, trackerPanel, firstFrameInVideoClip, 1);
  		}	  			
  	}
  	
  	// reset v and a arrays
  	getVArray(trackerPanel).setLength(0);
  	getAArray(trackerPanel).setLength(0);
	  
  	// reset trace data
    traceX = new double[] {point.getX()};
    traceY = new double[] {point.getY()};
		lastValidFrame = firstFrameInVideoClip;
		stepCounter = 0;
	}
  
	@Override
	public void setData(Data data, Object source) throws Exception {
		setData(data);
		setSource(source);
	}

	@Override
	public VideoPanel getVideoPanel() {
		return trackerPanel;
	}
	
	@Override
	protected PositionStep createPositionStep(PointMass track, int n, double x, double y) {
		ParticleDataTrack dt = (ParticleDataTrack)track;
		PositionStep newStep;
		if (track==getLeader())	newStep = new MultiPositionStep(dt, n, x, y);
		else newStep = new PositionStep(dt, n, x, y);
		newStep.valid = !Double.isNaN(x) && !Double.isNaN(y);
		return newStep;
	}
	
	/**
	 * Gets the data array.
	 * 
	 * @return double[][] {x, y, t}
	 */
	public double[][] getDataArray() {
		return new double[][] {xData, yData, tData};
	}
	
	/**
	 * Informs this track that values have been appended to the Data.
	 * 
	 * @param data Data containing newly appended values
	 * @throws Exception if (x, y) data not found
	 */
	public void appendData(Data data) throws Exception {
		// following line throws exception if (x, y) not found
		ArrayList<Object[]> pointData = getPointData(data);

		sourceData = data;
		Object[] coreData = pointData.get(0);
		double[][] xyArray = (double[][])coreData[1];				
		double[][] oldData = getDataArray();
		int n = oldData[0].length;
		if (xyArray[0].length<=n) {
			// inform user that no new data was found
			TFrame frame = trackerPanel!=null? trackerPanel.getTFrame(): null;
			JOptionPane.showMessageDialog(frame, 
					TrackerRes.getString("ParticleDataTrack.Dialog.NoNewData.Message"), //$NON-NLS-1$
					TrackerRes.getString("ParticleDataTrack.Dialog.NoNewData.Title"), //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		double[] timeArray = getTimeData(data); // may be null
		double[][] newData = new double[][] {xyArray[0], xyArray[1], timeArray};
		for (int i=0; i<newData.length; i++) {
			if (newData[i]!=null && oldData[i]!=null) {
				System.arraycopy(oldData[i], 0, newData[i], 0, n);
			}
		}
		setCoreData(newData, false);
		
		// append values to other points
		int len = Math.min(pointData.size()-1, morePoints.size());
		for (int i = 0; i<len; i++) {
			ParticleDataTrack target = morePoints.get(i);
			// get the new data
			Object[] next = pointData.get(i+1);				
			xyArray = (double[][])next[1];								
			// set target's data
			target.setCoreData(xyArray, true);
		}

	}
	
	/**
	 * Gets the time data from a Data object.
	 * 
	 * @param data the Data object
	 * @return the t array, or null if none found
	 */
	private static double[] getTimeData(Data data) {
		ArrayList<Dataset> datasets = data.getDatasets();
		for (Dataset dataset: datasets) {
			// look at x-column
			String s = dataset.getXColumnName().toLowerCase();
	  	int n = s.indexOf("("); //$NON-NLS-1$
	  	if (n>-1) {
	  		s =s.substring(0, n).trim();
	  	}
	  	else {
	    	n = s.indexOf("["); //$NON-NLS-1$
	    	if (n>-1) {
	    		s =s.substring(0, n).trim();
	    	}
	  	}
			if (s.equals("t") || s.equals("time"))	return dataset.getXPoints(); //$NON-NLS-1$ //$NON-NLS-2$
			
			// look at y-column
			s = dataset.getYColumnName().toLowerCase();
	  	n = s.indexOf("("); //$NON-NLS-1$
	  	if (n>-1) {
	  		s =s.substring(0, n).trim();
	  	}
	  	else {
	    	n = s.indexOf("["); //$NON-NLS-1$
	    	if (n>-1) {
	    		s =s.substring(0, n).trim();
	    	}
	  	}
			if (s.equals("t") || s.equals("time"))	return dataset.getYPoints(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}
	
	protected static String getImportableDataName(String s) {
  	// see if s is importable dataString
		DatasetManager manager = DataTool.parseData(s, null);
		if (manager==null) return null;
		try {
			getPointData(manager); // throws exception if no (x, y) data defined
    	String name = manager.getName();
    	if (name.trim().equals("")) { //$NON-NLS-1$
    		name = TrackerRes.getString("ParticleDataTrack.New.Name"); //$NON-NLS-1$
    	}
    	name = name.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
    	return name;
		} catch (Exception e) {
//			return null;
		}
//		boolean hasX = false, hasY = false;
//    for (Dataset next: manager.getDatasets()) {
//    	if (next.getYColumnName().toLowerCase().startsWith("x")) { //$NON-NLS-1$
//    		hasX = true;
//    	}
//    	else if (next.getYColumnName().toLowerCase().startsWith("y")) { //$NON-NLS-1$
//    		hasY = true;
//    	}
//    }
//    if (hasX && hasY){
//    	String name = manager.getName();
//    	if (name.trim().equals("")) { //$NON-NLS-1$
//    		name = TrackerRes.getString("ParticleDataTrack.New.Name"); //$NON-NLS-1$
//    	}
//    	name = name.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
//    	return name;
//    }
    return null;
	}
	
	protected static boolean isImportableClipboard(Clipboard clipboard) {
    Transferable data = clipboard.getContents(null);
    if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
    	try {
				String s = (String)data.getTransferData(DataFlavor.stringFlavor);
				return getImportableDataName(s)!=null;
			} catch (Exception e) {
			}
    }
    return false;
	}
	
	protected static ParticleDataTrack getTrackForDataString(String dataString, TrackerPanel trackerPanel) {
  	if (dataString==null) return null;
		ArrayList<ParticleDataTrack> tracks = trackerPanel.getDrawables(ParticleDataTrack.class);
  	for (ParticleDataTrack next: tracks) {
  		if (dataString.equals(next.prevDataString)) return next;
  	}
  	return null;
	}
	
	protected static ParticleDataTrack getTrackForData(Data data, TrackerPanel trackerPanel) {
  	// find DataTrack with matching name
  	String name = data.getName();
  	if (name==null || name.trim().equals("")) { //$NON-NLS-1$
  		name = TrackerRes.getString("ParticleDataTrack.New.Name"); //$NON-NLS-1$
  	}
  	name = name.replaceAll("_", " "); //$NON-NLS-1$ //$NON-NLS-2$
  	TTrack track = trackerPanel.getTrack(name);
  	// if name collisions occur, look for modified name
  	int i = 1;
  	while (track!=null && track.getClass()!=ParticleDataTrack.class) {
  		String nextName = getNextName(name, i++);
    	track = trackerPanel.getTrack(nextName);
    	if (track==null || track.getClass()==ParticleDataTrack.class) {
    		// stop looking and set new data name
  			Class<?> type = data.getClass();
  			Method method;
				try {
					method = type.getMethod("setName", new Class[] {String.class}); //$NON-NLS-1$
	  			method.invoke(data, new Object[] {nextName});
				} catch (Exception e) {}
    	}
  	}
  	
  	// if not found by name, check for matching ID
  	if (track==null) {
	  	int id = data.getID();
  		for (ParticleDataTrack model: trackerPanel.getDrawables(ParticleDataTrack.class)) {
  			Data existingData = model.getData();
  			if (existingData!=null && id==existingData.getID()) {
  				track = model;
  				break;
  			}
  		}
  	}
  	return (ParticleDataTrack)track;
	}
	
	protected static String getNextName(String original, int increment) {
		// see if original name contained an appended letter or number
		if (original.lastIndexOf(" ")==original.length()-2) { //$NON-NLS-1$
			String core = original.substring(0, original.length()-2);
			char coreChar = original.charAt(original.length()-1);
			for (char c = '0'; c <= '9'; c++) {
			  if (c==coreChar) {
			  	char newChar = (char)(c+increment);
			  	return core+" "+newChar; //$NON-NLS-1$
			  }
			}
			for (char c = 'a'; c <= 'z'; c++) {
			  if (c==coreChar) {
			  	char newChar = (char)(c+increment);
			  	return core+" "+newChar; //$NON-NLS-1$
			  }
			}
			for (char c = 'A'; c <= 'Z'; c++) {
			  if (c==coreChar) {
			  	char newChar = (char)(c+increment);
			  	return core+" "+newChar; //$NON-NLS-1$
			  }
			}
		}
		return original+increment;
	}
	
	/**
	 * Gets named (x, y) point data from a Data object.
	 * 
	 * @param data the Data object
	 * @return list of Object[] {String name, double[2][] xyData}
	 * @throws Exception if (x, y) data not defined, empty or inconsistent
	 */
	private static ArrayList<Object[]> getPointData(Data data) throws Exception {
		if (data==null) throw new Exception("Data is null"); //$NON-NLS-1$
		ArrayList<Dataset> datasets = data.getDatasets();
		if (datasets==null) throw new Exception("Data contains no datasets"); //$NON-NLS-1$

		ArrayList<Object[]> results = new ArrayList<Object[]>();
//		boolean foundX = false, foundY = false;
		String colName = null;
		Dataset prevDataset = null;
		for (Dataset dataset: datasets) {			
			// look for columns with paired xy names
			double[][] xy = new double[2][];
			if (xy[0]==null && dataset.getXColumnName().toLowerCase().startsWith("x"))	{ //$NON-NLS-1$
				colName = dataset.getXColumnName().substring(1).trim();
				xy[0] = dataset.getXPoints();
			}
			else if (xy[0]==null && dataset.getYColumnName().toLowerCase().startsWith("x")) { //$NON-NLS-1$
				colName = dataset.getYColumnName().substring(1).trim();
				xy[0] = dataset.getYPoints();
			}
			else if (xy[0]==null && dataset.getXColumnName().toLowerCase().endsWith("x")) { //$NON-NLS-1$
				colName = dataset.getXColumnName().substring(0, dataset.getXColumnName().length()-1).trim();
				xy[0] = dataset.getXPoints();
			}
			else if (xy[0]==null && dataset.getYColumnName().toLowerCase().endsWith("x")) { //$NON-NLS-1$
				colName = dataset.getYColumnName().substring(0, dataset.getYColumnName().length()-1).trim();
				xy[0] = dataset.getYPoints();
			}
			if (xy[1]==null && dataset.getXColumnName().toLowerCase().startsWith("y"))	{ //$NON-NLS-1$
				if (colName==null) {
					xy[1] = dataset.getXPoints();
					colName = dataset.getXColumnName().substring(1).trim();
				}
				else if (dataset.getXColumnName().substring(1).trim().equals(colName)) {
					// match
					xy[1] = dataset.getXPoints();
				}
			}
			else if (xy[1]==null && dataset.getYColumnName().toLowerCase().startsWith("y")) { //$NON-NLS-1$
				if (colName==null) {
					colName = dataset.getYColumnName().substring(1).trim();
					xy[1] = dataset.getYPoints();
				}
				else if (dataset.getYColumnName().substring(1).trim().equals(colName)) {
					// match
					xy[1] = dataset.getYPoints();
				}
			}
			else if (xy[1]==null && dataset.getXColumnName().toLowerCase().endsWith("y"))	{ //$NON-NLS-1$
				if (colName==null) {
					xy[1] = dataset.getXPoints();
					colName = dataset.getXColumnName().substring(0, dataset.getXColumnName().length()-1).trim();
				}
				else if (dataset.getXColumnName().substring(0, dataset.getXColumnName().length()-1).trim().equals(colName)) {
					// match
					xy[1] = dataset.getXPoints();
				}
			}
			else if (xy[1]==null && dataset.getYColumnName().toLowerCase().endsWith("y")) { //$NON-NLS-1$
				if (colName==null) {
					colName = dataset.getYColumnName().substring(0, dataset.getYColumnName().length()-1).trim();
					xy[1] = dataset.getYPoints();
				}
				else if (dataset.getYColumnName().substring(0, dataset.getYColumnName().length()-1).trim().equals(colName)) {
					// match
					xy[1] = dataset.getYPoints();
				}
			}
			
			// if all data are present, add to results and continue to next dataset
			if (xy[0]!=null && xy[1]!=null && colName!=null) {
				results.add(new Object[] {colName, xy});
				colName = null;
				continue;
			}
			
			// not all data is present
			if (colName!=null && prevDataset!=null) { // partial data is present, so look at previous dataset
				if (xy[0]==null && prevDataset.getXColumnName().toLowerCase().startsWith("x") //$NON-NLS-1$
						&& prevDataset.getXColumnName().substring(1).trim().equals(colName))	{
					xy[0] = prevDataset.getXPoints();
				}
				else if (xy[0]==null && prevDataset.getYColumnName().toLowerCase().startsWith("x") //$NON-NLS-1$
						&& prevDataset.getYColumnName().substring(1).trim().equals(colName))	{
					xy[0] = prevDataset.getYPoints();
				}
				else if (xy[0]==null && prevDataset.getXColumnName().toLowerCase().endsWith("x") //$NON-NLS-1$
						&& prevDataset.getXColumnName().substring(0, prevDataset.getXColumnName().length()-1).trim().equals(colName))	{
					xy[0] = prevDataset.getXPoints();
				}
				else if (xy[0]==null && prevDataset.getYColumnName().toLowerCase().endsWith("x") //$NON-NLS-1$
						&& prevDataset.getYColumnName().substring(0, prevDataset.getYColumnName().length()-1).trim().equals(colName))	{
					xy[0] = prevDataset.getYPoints();
				}
				if (xy[1]==null && prevDataset.getXColumnName().toLowerCase().startsWith("y") //$NON-NLS-1$
						&& prevDataset.getXColumnName().substring(1).trim().equals(colName))	{
					xy[1] = prevDataset.getXPoints();
				}
				else if (xy[1]==null && prevDataset.getYColumnName().toLowerCase().startsWith("y") //$NON-NLS-1$
						&& prevDataset.getYColumnName().substring(1).trim().equals(colName))	{
					xy[1] = prevDataset.getYPoints();
				}				
				else if (xy[1]==null && prevDataset.getXColumnName().toLowerCase().endsWith("y") //$NON-NLS-1$
						&& prevDataset.getXColumnName().substring(0, prevDataset.getXColumnName().length()-1).trim().equals(colName))	{
					xy[1] = prevDataset.getXPoints();
				}
				else if (xy[1]==null && prevDataset.getYColumnName().toLowerCase().endsWith("y") //$NON-NLS-1$
						&& prevDataset.getYColumnName().substring(0, prevDataset.getYColumnName().length()-1).trim().equals(colName))	{
					xy[1] = prevDataset.getYPoints();
				}				
			}
			
			prevDataset = dataset;
			// if all data are present, add to results
			if (xy[0]!=null && xy[1]!=null && colName!=null) {
				colName = colName.replaceAll("_", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				results.add(new Object[] {colName, xy});
				prevDataset = null;
			}
			
			colName = null;
		}	// end for loop
		
		// if no paired datasets are found check for unnamed data
		if (results.isEmpty()) {
			double[][] xy = new double[2][];
			for (Dataset dataset: datasets) {			
				if (xy[0]==null && dataset.getYColumnName().equals("?"))	{ //$NON-NLS-1$
					xy[0] = dataset.getYPoints();					
				}
				else if (xy[0]!=null && xy[1]==null && dataset.getYColumnName().equals("?")) { //$NON-NLS-1$
					xy[1] = dataset.getYPoints();
					break;
				}
			}
			// if all data are present, add to results
			if (xy[0]!=null && xy[1]!=null) {
				colName = ""; //$NON-NLS-1$
				results.add(new Object[] {colName, xy});
			}
		}

		if (results.isEmpty()) {
			throw new Exception("Position data (x, y) not defined"); //$NON-NLS-1$
		}
		// check first data array for matching data length, etc
		Object[] result = results.get(0);
		double[][] dataArray = (double[][])result[1];
		
		if (dataArray[0].length==0 || dataArray[1].length==0) {
			throw new Exception("Position data is empty"); //$NON-NLS-1$
		}
		if (dataArray[0].length!=dataArray[1].length) {
			throw new Exception("X and Y data have different array lengths"); //$NON-NLS-1$
		}
		
		return results;
	}
	

  //___________________________________  private methods ____________________________
  
	/**
	 * This adds the initial time parameter to the function panel.
	 */
	private void createTimeParameter() {		
		Parameter param = new Parameter("t", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		param.setNameEditable(false);
		param.setDescription(TrackerRes.getString("ParticleModel.Parameter.InitialTime.Description")); //$NON-NLS-1$
		functionPanel.getInitEditor().addObject(param, false);
		getInitEditor().addPropertyChangeListener(new PropertyChangeListener() {
		  public void propertyChange(PropertyChangeEvent e) {
		  	if (refreshing) return;
		  	if ("t".equals(e.getOldValue()) && trackerPanel != null) { //$NON-NLS-1$
		  		Parameter param = (Parameter)getInitEditor().getObject("t"); //$NON-NLS-1$
		      VideoClip clip = trackerPanel.getPlayer().getVideoClip();
		      double timeOffset = param.getValue()*1000 - clip.getStartTime();
		      double dt = trackerPanel.getPlayer().getMeanStepDuration();
		      int n = clip.getStartFrameNumber();
		      boolean mustRound = timeOffset%dt>0;
		      n += clip.getStepSize()*(int)Math.round(timeOffset/dt);
		      setStartFrame(n);
		      if (getStartFrame()!=n || mustRound)
		      	Toolkit.getDefaultToolkit().beep();
		  	}
		  }
		});
	}
	
	/**
	 * Sets the data as array {x, y, t}. If time data is included, it is assumed to be in seconds.
	 * The t array may be null, but x and y are required.
	 * 
	 * @param data the data array {x, y, t}
	 * @param reset true to redraw all frames
	 */
	private void setCoreData(double[][] data, boolean reset) {
		xData = data[0];
		yData = data[1];
		tData = data.length>2? data[2]: null;
		getDataClip().setDataLength(data[0].length);
		firePropertyChange("dataclip", null, dataClip); //$NON-NLS-1$
    adjustVideoClip();
		if (reset) {
			lastValidFrame = -1;
			refreshSteps();
			firePropertyChange("steps", null, null); //$NON-NLS-1$
		}
		invalidWarningShown = true;
		repaint();
	}
	
	/**
	 * Adjusts the video clip by (a) extending it if it currently ends 
	 * at the last frame and the data clip extends past that point, or (b)
	 * trimming it if it has extra frames past the last frame in the data clip.
	 */
	private void adjustVideoClip() {
		if (trackerPanel==null) return;
		// determine if video clip ends at last frame
		VideoClip vidClip = trackerPanel.getPlayer().getVideoClip();
		int videoEndFrame = vidClip.getEndFrameNumber();
		boolean isLast = videoEndFrame==vidClip.getLastFrameNumber();
		int dataEndFrame = getStartFrame()+getDataClip().getAvailableClipLength()-1;
		if (isLast && dataEndFrame>videoEndFrame) {
			vidClip.extendEndFrameNumber(dataEndFrame);
		}
		else if (dataEndFrame<videoEndFrame && vidClip.getExtraFrames()>0) {
			// trim surplus extra frames
			int needed = vidClip.getExtraFrames()-(videoEndFrame-dataEndFrame);
			vidClip.setExtraFrames(needed);
		}
	}
	
//___________________________________ inner classes _________________________________
	
	class ComboIcon implements Icon {
		
		ArrayList<ShapeIcon> shapeIcons;
		
		ComboIcon(ArrayList<ShapeIcon> icons) {
			shapeIcons = icons;
		}
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (shapeIcons.size()==1) {
				shapeIcons.get(0).paintIcon(c, g, x, y);
			}
			else {
				Graphics2D g2 = (Graphics2D)g;
				AffineTransform restoreTransform = g2.getTransform();
				int w = getIconWidth();
				int h = getIconHeight();
				g2.scale(0.7, 0.7);
				int n = shapeIcons.size();
				for (int i=0; i<n; i++) {
					if (i%2==0) { // even points above
						shapeIcons.get(i).paintIcon(c, g, x+i*w/(n), y);
					}
					else { // odd points below
						shapeIcons.get(i).paintIcon(c, g, x+i*w/(n), y+h/2);
					}					
				}
				g2.setTransform(restoreTransform);
			}
		}
	
		@Override
		public int getIconWidth() {
			return shapeIcons.get(0).getIconWidth();
		}
	
		@Override
		public int getIconHeight() {
			return shapeIcons.get(0).getIconHeight();
		}
		
	}
	
//__________________________ static methods ___________________________

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

    public void saveObject(XMLControl control, Object obj) {
      ParticleDataTrack dataTrack = (ParticleDataTrack)obj;
      // save mass
      control.setValue("mass", dataTrack.getMass()); //$NON-NLS-1$
      // save track data
      XML.getLoader(TTrack.class).saveObject(control, obj);
      //save model name as name
      control.setValue("name", dataTrack.modelName); //$NON-NLS-1$
//      // save initial values
//      Parameter[] inits = model.getInitEditor().getParameters();
//    	control.setValue("initial_values", inits); //$NON-NLS-1$
      // save the data
	    control.setValue("x", dataTrack.xData); //$NON-NLS-1$
	    control.setValue("y", dataTrack.yData); //$NON-NLS-1$
	    control.setValue("t", dataTrack.tData); //$NON-NLS-1$
	    // save point name
	    control.setValue("pointname", dataTrack.pointName); //$NON-NLS-1$
	    // save additional point data: x, y, mass, point name, color, footprint
	    for (int i=0; i<dataTrack.morePoints.size(); i++) {
	    	ParticleDataTrack pointTrack = dataTrack.morePoints.get(i);
	      // save the data
		    control.setValue("x"+i, pointTrack.xData); //$NON-NLS-1$
		    control.setValue("y"+i, pointTrack.yData); //$NON-NLS-1$
		    // save point name
	      control.setValue("mass"+i, pointTrack.getMass()); //$NON-NLS-1$
		    control.setValue("pointname"+i, pointTrack.pointName); //$NON-NLS-1$
	      // save color
	      control.setValue("color"+i, pointTrack.getColor()); //$NON-NLS-1$
	      // footprint name
	      control.setValue("footprint"+i, pointTrack.getFootprintName()); //$NON-NLS-1$
	    }
	    
	    // save the dataclip
	    control.setValue("dataclip", dataTrack.getDataClip()); //$NON-NLS-1$
      // save start and end frames (if custom)
      if (dataTrack.getStartFrame()>0)
      	control.setValue("start_frame", dataTrack.getStartFrame()); //$NON-NLS-1$
	    // save useDataTime flag
	    control.setValue("use_data_time", ClipControl.isTimeSource(dataTrack)); //$NON-NLS-1$
	    // save modelFootprint properties
	    control.setValue("model_footprint", dataTrack.getModelFootprintName()); //$NON-NLS-1$
	    control.setValue("model_footprint_color", dataTrack.getModelFootprint().getColor()); //$NON-NLS-1$
	    if (dataTrack.modelFootprintVisible) {
	    	control.setValue("model_footprint_visible", true); //$NON-NLS-1$
	    }
  		// save inspector size and position
  		if (dataTrack.modelBuilder != null &&
  						dataTrack.trackerPanel != null && 
  						dataTrack.trackerPanel.getTFrame() != null) {
  			// save inspector location relative to frame
  			TFrame frame = dataTrack.trackerPanel.getTFrame();
  			int x = dataTrack.modelBuilder.getLocation().x - frame.getLocation().x;
  			int y = dataTrack.modelBuilder.getLocation().y - frame.getLocation().y;
    		control.setValue("inspector_x", x); //$NON-NLS-1$
    		control.setValue("inspector_y", y); //$NON-NLS-1$  			
    		control.setValue("inspector_h", dataTrack.modelBuilder.getHeight()); //$NON-NLS-1$ 
    		control.setValue("inspector_visible", dataTrack.modelBuilder.isVisible()); //$NON-NLS-1$
  		}
    }

    public Object createObject(XMLControl control){
    	double[][] coreData = new double[3][];
    	coreData[0] = (double[])control.getObject("x"); //$NON-NLS-1$
    	coreData[1] = (double[])control.getObject("y"); //$NON-NLS-1$
    	coreData[2] = (double[])control.getObject("t"); //$NON-NLS-1$
    	int i = 0;
    	ArrayList<Object[]> pointData = new ArrayList<Object[]>();
    	double[][] next = new double[2][];
    	next[0] = (double[])control.getObject("x"+i); //$NON-NLS-1$
    	while (next[0]!=null) {
    		next[1] = (double[])control.getObject("y"+i); //$NON-NLS-1$)
    		String name = control.getString("pointname"+i); //$NON-NLS-1$
    		pointData.add(new Object[] {name, next});
    		i++;
    		next = new double[2][];
    		next[0] = (double[])control.getObject("x"+i); //$NON-NLS-1$
    	}
      return new ParticleDataTrack(coreData, pointData);
    }

    public Object loadObject(XMLControl control, Object obj) {
    	ParticleDataTrack dataTrack = (ParticleDataTrack)obj;
      // load track data and mass
      XML.getLoader(TTrack.class).loadObject(control, obj);
      dataTrack.mass = control.getDouble("mass"); //$NON-NLS-1$
      // load pointname
      dataTrack.setPointName(control.getString("pointname")); //$NON-NLS-1$
      // load dataclip
      XMLControl dataClipControl = control.getChildControl("dataclip"); //$NON-NLS-1$
      if (dataClipControl!=null) {
      	dataClipControl.loadObject(dataTrack.getDataClip());
      }
      // load point properties: mass, color, footprint
      for (int i=0; i<dataTrack.morePoints.size(); i++) {
      	ParticleDataTrack child = dataTrack.morePoints.get(i);
      	child.setMass(control.getDouble("mass"+i)); //$NON-NLS-1$
      	child.setColor((Color)control.getObject("color"+i)); //$NON-NLS-1$
      	child.setFootprint(control.getString("footprint"+i)); //$NON-NLS-1$
      }
      // load useDataTime flag
      dataTrack.useDataTime = control.getBoolean("use_data_time"); //$NON-NLS-1$
      // load start frame
      int n = control.getInt("start_frame"); //$NON-NLS-1$
  		if (n!=Integer.MIN_VALUE)
  			dataTrack.startFrame = n;
  		else {
  			dataTrack.startFrameUndefined = true;
  		}
	    // load modelFootprint properties
  		if (control.getPropertyNames().contains("model_footprint")) { //$NON-NLS-1$
		    dataTrack.setModelFootprint(control.getString("model_footprint")); //$NON-NLS-1$
		    dataTrack.modelFootprintVisible = control.getBoolean("model_footprint_visible"); //$NON-NLS-1$
		    dataTrack.modelFootprint.setColor((Color)control.getObject("model_footprint_color")); //$NON-NLS-1$
  		}
  		else {
		    dataTrack.modelFootprint.setColor(dataTrack.getColor());
  		}
      dataTrack.inspectorX = control.getInt("inspector_x"); //$NON-NLS-1$
      dataTrack.inspectorY = control.getInt("inspector_y"); //$NON-NLS-1$
      dataTrack.inspectorH = control.getInt("inspector_h"); //$NON-NLS-1$
      dataTrack.showModelBuilder = control.getBoolean("inspector_visible"); //$NON-NLS-1$
      return dataTrack;
    }
  }

}
