/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputAdapter;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.HighlightableDataset;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.Measurable;
import org.opensourcephysics.display.MeasuredImage;
import org.opensourcephysics.display.MessageDrawable;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.display.axes.CartesianInteractive;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.DataRefreshTool;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.DataToolTab;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.Job;
import org.opensourcephysics.tools.LocalJob;
import org.opensourcephysics.tools.Tool;

/**
 * This is a plotting panel for a track
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TrackPlottingPanel extends PlottingPanel implements Tool {
	

	private static final int VAR_NAME_NULL = Integer.MIN_VALUE;
	private static final int VAR_NOT_FOUND = -2;

	// instance fields
	protected TFrame frame;
	protected Integer panelID;
	protected int trackID;

	protected DatasetManager datasetManager;
	protected HighlightableDataset dataset = new HighlightableDataset();
	protected ArrayList<TTrack> guests = new ArrayList<TTrack>();
	protected HashMap<TTrack, HighlightableDataset> guestDatasets = new HashMap<TTrack, HighlightableDataset>();
	
	private int xIndex = -1, yIndex = 0;
	private String xName, yName;
	
	// GUI (lazy)
	
	protected JPopupMenu xPopup, yPopup;
	protected JPopupMenu popup;

	protected JCheckBoxMenuItem linesItem, pointsItem;
	protected JMenuItem dataToolItem;
	private JRadioButtonMenuItem[] xChoices, yChoices;
	private ButtonGroup xGroup, yGroup;
	
	protected Action dataFunctionListener, guestListener;
	private JMenuItem copyImageItem, dataBuilderItem; 
	private JMenuItem showXZeroItem, showYZeroItem;
	private JMenuItem selectPointsItem, deselectPointsItem;
	private JMenuItem algorithmItem, printItem, helpItem, mergeYScalesItem;
	private JMenuItem guestsItem;
	
	protected String xLabel, yLabel, title;
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

	protected boolean linesItemSelected = true;

	protected boolean pointsItemSelected = true;

	private Map<String, Integer> htVarToItem = new LinkedHashMap<>();

	private int datasetCount;

	boolean selectionEnabled = true;
	final public BitSet bsFrameHighlights = new BitSet();

	/**
	 * Constructs a TrackPlottingPanel for a track.
	 *
	 * @param track the track
	 * @param data  the track's data
	 */
	public TrackPlottingPanel(TTrack track, DatasetManager data) {
		super(" ", " ", " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		displayCoordsOnMouseMoved = false;
		frame = track.tframe;
		panelID = track.tp.getID();	
		trackID = track.getID();
		this.datasetManager = data;
		dataset.setConnected(true);
		dataset.setMarkerShape(Dataset.SQUARE);
		// set new CoordinateStringBuilder
		coordStringBuilder = new TCoordinateStringBuilder();
		setCoordinateStringBuilder(coordStringBuilder);
		// don't create radio buttons and popups to set x and y variables
		setVariables();


		// add plotMouseListener
		mouseListener = new PlotMouseListener();
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				VideoPlayer player = trackerPanel.getPlayer();
				if (!player.isEnabled())
					return;
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					if (e.isShiftDown()) {
						int n = player.getStepNumber() - 5;
						player.setStepNumber(n);
					} else
						player.back();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					if (e.isShiftDown()) {
						int n = player.getStepNumber() + 5;
						player.setStepNumber(n);
					} else
						player.step();
					break;
				case KeyEvent.VK_HOME:
					player.setStepNumber(0);
					break;
				case KeyEvent.VK_END:
					VideoClip clip = player.getVideoClip();
					player.setStepNumber(clip.getStepCount() - 1);
					break;
				case KeyEvent.VK_DELETE: // delete selected steps
					trackerPanel.deleteSelectedSteps();
					if (trackerPanel.getSelectedPoint() != null
							&& trackerPanel.getSelectingPanelID() == trackerPanel.getID()) {
						trackerPanel.deletePoint(trackerPanel.getSelectedPoint());
					}
					return;

				}
			}
		});
	}

	@Override 
	protected void initAxes() {
		// create clickable axes
		setAxes(plotAxes = new ClickableAxes(this));
	}
	
	@Override
	public void send(Job job, Tool replyTo) {
		XMLControlElement control = new XMLControlElement();
		control.readXML(job.getXML());
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
		TFrame.repaintT(this);
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
	@Override
	public void setXLabel(String label) {
		dataset.setXYColumnNames(label, yLabel);
		xLabel = label;
		String xStr = TeXParser.removeSubscripting(xLabel) + "="; //$NON-NLS-1$
		String yStr = "  " + TeXParser.removeSubscripting(yLabel) + "="; //$NON-NLS-1$ //$NON-NLS-2$
		getCoordinateStringBuilder().setCoordinateLabels(xStr, yStr);
		// add units to label
		TTrack track = TTrack.getTrack(trackID);
		if (track.tp != null) {
			String units = track.tp.getUnits(track, label);
			if (!"".equals(units)) { //$NON-NLS-1$
				label += " (" + units.trim() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
	@Override
	public void setYLabel(String label) {
		yLabel = label;
		dataset.setXYColumnNames(xLabel, label);
		String xStr = TeXParser.removeSubscripting(xLabel) + "="; //$NON-NLS-1$
		String yStr = "  " + TeXParser.removeSubscripting(yLabel) + "="; //$NON-NLS-1$ //$NON-NLS-2$
		getCoordinateStringBuilder().setCoordinateLabels(xStr, yStr);
		// add units to label
		TTrack track = TTrack.getTrack(trackID);
		if (track.tp != null) {
			String units = track.tp.getUnits(track, label);
			if (!"".equals(units)) { //$NON-NLS-1$
				label += " (" + units.trim() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
	@Override
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
		if (guest == null || guests.contains(guest))
			return;
		guests.add(guest);
		isCustom = true;
		HighlightableDataset guestDataset = guestDatasets.get(guest);
		if (guestDataset == null) {
			guestDataset = new HighlightableDataset();
			guestDatasets.put(guest, guestDataset);
		}
		guest.removeStepListener(plotTrackView); //$NON-NLS-1$
		guest.addStepListener(plotTrackView); //$NON-NLS-1$
	}

	/**
	 * Removes a guest track.
	 *
	 * @param guest the track
	 */
	public void removeGuest(TTrack guest) {
		guests.remove(guest);
		guest.removeStepListener(plotTrackView); //$NON-NLS-1$
	}

	/**
	 * Overrides DrawingPanel scale method
	 */
	@Override
	public void scale(ArrayList<Drawable> list) {
		if (autoscaleXMin && !autoscaleXMax) {
			scaleXMin();
		} else if (!autoscaleXMin && autoscaleXMax) {
			scaleXMax();
		} else if (autoscaleXMin && autoscaleXMax) {
			scaleX(list);
		}
		if (autoscaleYMin && !autoscaleYMax) {
			scaleYMin();
		} else if (!autoscaleYMin && autoscaleYMax) {
			scaleYMax();
		} else if (autoscaleYMin && autoscaleYMax) {
			scaleY(list);
		}
	}
	
	/**
	 * Gets the popup menu.
	 */
	@Override
	public JPopupMenu getPopupMenu() {
		if (!Tracker.allowMenuRefresh)
			return null;	
		if (popupmenu == null) {
			buildPopupMenu();
		}
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
		guestsItem.setText(TrackerRes.getString("TrackPlottingPanel.Popup.Menu.CompareWith") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
		if (plotTrackView.getPlotCount() > 1) {
			popupmenu.add(mergeYScalesItem, 3);
		} else
			popupmenu.remove(mergeYScalesItem);
		// include showXZeroItem and showYZeroItem only when they can change the scale
		popupmenu.remove(showXZeroItem);
		popupmenu.remove(showYZeroItem);
		if (getXMin() * getXMax() > 0) {
			String s = TeXParser.removeSubscripting(dataset.getColumnName(0));
			s = TrackerRes.getString("TrackPlottingPanel.Popup.MenuItem.ShowZero") + " " + s + "=0"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			showXZeroItem.setText(s);
			popupmenu.insert(showXZeroItem, popupmenu.getComponentIndex(scaleItem));
		}
		if (getYMin() * getYMax() > 0) {
			String s = TeXParser.removeSubscripting(dataset.getColumnName(1));
			s = TrackerRes.getString("TrackPlottingPanel.Popup.MenuItem.ShowZero") + " " + s + "=0"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			showYZeroItem.setText(s);
			popupmenu.insert(showYZeroItem, popupmenu.getComponentIndex(scaleItem));
		}

		// refresh guests item
		TTrack track = TTrack.getTrack(trackID);
		Class<? extends TTrack> type = track instanceof PointMass ? PointMass.class
				: track instanceof Vector ? Vector.class : track.getClass();
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		ArrayList<? extends TTrack> tracks = trackerPanel.getDrawablesTemp(type);
		tracks.removeAll(trackerPanel.calibrationTools);
		tracks.remove(track);
		guestsItem.setEnabled(!tracks.isEmpty());
		tracks.clear();
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
		BufferedImage image = new TrackerIO.ComponentImage(TViewChooser.getChooserParent(this)).getImage();
		int w = image.getWidth();
		int h = image.getHeight();
		if ((w == 0) || (h == 0)) {
			return;
		}
		MeasuredImage mi = new MeasuredImage(image, 0, w, h, 0);

		// create ImageFrame using reflection
		OSPFrame frame = null;
		try {
			Class<?> type = Class.forName("org.opensourcephysics.frames.ImageFrame"); //$NON-NLS-1$
			Constructor<?>[] constructors = type.getConstructors();
			for (int i = 0; i < constructors.length; i++) {
				Class<?>[] parameters = constructors[i].getParameterTypes();
				if (parameters.length == 1 && parameters[0] == MeasuredImage.class) {
					frame = (OSPFrame) constructors[i].newInstance(new Object[] { mi });
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (frame == null)
			return;

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
	@Override
	protected void buildPopupMenu() {
		if (popup == null) {
			popup = new JPopupMenu() {
				@Override
				public void setVisible(boolean vis) {
					super.setVisible(vis);
					if (!vis)
						zoomBox.hide();
				}
			};
			setPopupMenu(popup);
			super.buildPopupMenu(); 
			linesItem = new JCheckBoxMenuItem("lines", linesItemSelected);
			pointsItem = new JCheckBoxMenuItem("points", pointsItemSelected);	
			dataToolItem = new JMenuItem("datatool");

			linesItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dataset.setConnected(linesItem.isSelected());
					for (TTrack next : guests) {
						HighlightableDataset nextDataset = guestDatasets.get(next);
						nextDataset.setConnected(linesItem.isSelected());
					}
					isCustom = true;
					TFrame.repaintT(TrackPlottingPanel.this);
				}
			});
			linesItem.setSelected(true);
			// points menu item
			pointsItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (pointsItem.isSelected()) {
						dataset.setMarkerShape(Dataset.SQUARE);
						for (TTrack next : guests) {
							HighlightableDataset nextDataset = guestDatasets.get(next);
							nextDataset.setMarkerShape(Dataset.SQUARE);
						}
					} else {
						dataset.setMarkerShape(Dataset.NO_MARKER);
						for (TTrack next : guests) {
							HighlightableDataset nextDataset = guestDatasets.get(next);
							nextDataset.setMarkerShape(Dataset.NO_MARKER);
						}
					}
					isCustom = true;
					TFrame.repaintT(TrackPlottingPanel.this);
				}
			});
			pointsItem.setSelected(true);
			// showZero menu items
			showXZeroItem = new JMenuItem();
			showXZeroItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showZeroOnAxis("x"); //$NON-NLS-1$
				}
			});
			showYZeroItem = new JMenuItem();
			showYZeroItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showZeroOnAxis("y"); //$NON-NLS-1$
				}
			});
			printItem = new JMenuItem();
			printItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// find TViewChooser that owns this view and print it
					TViewChooser chooser = getOwner();
					if (chooser != null) {
						new TrackerIO.ComponentImage(chooser).print();
					}
				}
			});
			// dataTool item
			dataToolItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showDataTool();
				}
			});
			// algorithm item
			algorithmItem = new JMenuItem();
			algorithmItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DerivativeAlgorithmDialog dialog = frame.getTrackerPanelForID(panelID).getAlgorithmDialog();
					TTrack track = TTrack.getTrack(trackID);
					if (track instanceof PointMass) {
						dialog.setTargetMass((PointMass) track);
					}
					FontSizer.setFonts(dialog, FontSizer.getLevel());
					dialog.pack();
					dialog.setVisible(true);
				}
			});
			// copy image item
			Action copyImageAction = new AbstractAction() {
				@Override
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
				@Override
				public void actionPerformed(ActionEvent e) {
					TTrack track = TTrack.getTrack(trackID);
					TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
					trackerPanel.getDataBuilder().setSelectedPanel(track.getName());
					trackerPanel.getDataBuilder().setVisible(true);
				}
			};
			dataBuilderItem = new JMenuItem();
			dataBuilderItem.addActionListener(dataFunctionListener);
			// help item
			helpItem = new JMenuItem();
			helpItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Container c = getTopLevelAncestor();
					if (c instanceof TFrame) {
						TFrame frame = (TFrame) c;
						frame.showHelp("plot", 0); //$NON-NLS-1$
					}
				}
			});
			// mergeYAxesItem
			mergeYScalesItem = new JMenuItem();
			mergeYScalesItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switch (plotTrackView.getPlotCount()) {
					case 2:
						plotTrackView.syncYAxes(plotTrackView.plots[0], plotTrackView.plots[1]);
						break;
					case 3:
						plotTrackView.syncYAxes(plotTrackView.plots);
						break;
					}
				}
			});
		}
		// guests item
		guestsItem = new JMenuItem();
		guestsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PlotGuestDialog dialog = frame.getTrackerPanelForID(panelID).getPlotGuestDialog(TrackPlottingPanel.this);
				dialog.setLocationRelativeTo(TrackPlottingPanel.this);
				dialog.setVisible(true);
			}
		});

		Action selectAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectAction(e.getSource());
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
		if (panelID != null) {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
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

	protected void selectAction(Object source) {
		// find limits of zoom box
		Rectangle rect = zoomBox.reportZoom();
		double x = pixToX(rect.x);
		double x2 = pixToX(rect.x + rect.width);
		double y = pixToY(rect.y + rect.height);
		double y2 = pixToY(rect.y);
		double xmin = Math.min(x, x2);
		double xmax = Math.max(x, x2);
		double ymin = Math.min(y, y2);
		double ymax = Math.max(y, y2);
		// look for all points in the dataset that fall within limits
		double[] xPoints = dataset.getXPointsRaw();
		double[] yPoints = dataset.getYPointsRaw();
		int len = dataset.getIndex();
		TTrack track = TTrack.getTrack(trackID);
		TreeSet<Integer> frames = new TreeSet<Integer>();
		for (int i = 0; i < len; i++) {
			if (Double.isNaN(xPoints[i]) || Double.isNaN(yPoints[i]))
				continue;
			if (xPoints[i] >= xmin && xPoints[i] <= xmax && yPoints[i] >= ymin && yPoints[i] <= ymax) {
				// found one: add its frame number to frames set
				int frame = track.getFrameForData(getXLabel(), getYLabel(),
						new double[] { xPoints[i], yPoints[i] });
				if (frame >= 0) {
					frames.add(frame);
				}
			}
		}
		// add or remove steps from selectedSteps
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		for (int frame : frames) {
			Step step = track.getStep(frame);
			if (source == selectPointsItem)
				trackerPanel.selectedSteps.add(step);
			else
				trackerPanel.selectedSteps.remove(step);
			step.erase();
		}
		fireRepaint(track);
	}

	/**
	 * Overrides DrawingPanel method to prevent changes to glassPanel and deal with
	 * multiple plots.
	 */
	@Override
	public Rectangle findViewRect() {
		JViewport c = GUIUtils.getParentViewport(this);
		Rectangle rect = (c == null ? super.findViewRect() : c.getViewRect());
		// transform rect to this plotting panel space
		Rectangle bounds = getBounds();
		rect = rect.intersection(bounds);
		rect.y -= bounds.y;
		return rect;
	}

	/**
	 * Rescales this plot so the zero value is included in the range ymin-ymax.
	 * 
	 * @return true if the plot scale is changed.
	 */
	protected void showZeroOnAxis(String axis) {
		if (axis.equals("x")) { //$NON-NLS-1$
			if (xmin * xmax > 0) { // either both pos or both neg
				if (xmax > 0)
					xmin = 0;
				else
					xmax = 0;
				setPreferredMinMax(xmin, xmax, ymin, ymax);
				TFrame.repaintT(this); // repaint the panel with the new scale
				isCustom = true;
			}
		} else {
			if (ymin * ymax > 0) { // either both pos or both neg
				if (ymax > 0)
					ymin = 0;
				else
					ymax = 0;
				setPreferredMinMax(xmin, xmax, ymin, ymax);
				TFrame.repaintT(this); // repaint the panel with the new scale
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
			if (newXMin == xmaxPreferred) { // bracket the value
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
			if (xminPreferred == newXMax) { // bracket the value
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
		double range = 0;
		Measurable dataset = getDataset();
		if (dataset != null && dataset.isMeasured()) {
			if (!Double.isNaN(dataset.getYMin())) {
				newYMin = Math.min(newYMin, dataset.getYMin());
			}
			if (newYMin == ymaxPreferred) {
				newYMin = 0.9 * newYMin - 0.5;
			}
			range = ymaxPreferred - newYMin;
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
		double range = 0;
		if (dataset != null && dataset.isMeasured()) {
			if (!Double.isNaN(dataset.getYMax())) {
				newYMax = Math.max(newYMax, dataset.getYMax());
			}
			if (yminPreferred == newYMax) {
				newYMax = 1.1 * newYMax + 0.5;
			}
			range = newYMax - yminPreferred;
			ymaxPreferred = newYMax + autoscaleMargin * range;
		}
		if (!Double.isNaN(yceil)) {
			ymaxPreferred = Math.max(yceil, ymaxPreferred);
		}
	}

	/**
	 * Gets the TViewChooser that owns (displays) this panel.
	 * 
	 * @return the TViewChooser. May return null.
	 */
	protected TViewChooser getOwner() {
		return plotTrackView.getOwner();
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
		if (xIndex == -1)
			xData = datasetManager.getDataset(0);
		else
			xData = datasetManager.getDataset(xIndex);
		Dataset yData = datasetManager.getDataset(yIndex);
		TTrack track = TTrack.getTrack(trackID);
		String xTitle = xData.getColumnName(xIndex >= 0 ? 1 : 0);
		String yTitle = yData.getColumnName(1);
		setTitle(track.getName() + " (" + xTitle + ", " + yTitle + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setXLabel(xTitle);
		setYLabel(yTitle);
		boolean xIsAngle = xTitle.startsWith(Tracker.THETA) || xTitle.startsWith(Tracker.OMEGA)
				|| xTitle.startsWith(Tracker.ALPHA);
		boolean yIsAngle = yTitle.startsWith(Tracker.THETA) || yTitle.startsWith(Tracker.OMEGA)
				|| yTitle.startsWith(Tracker.ALPHA);
		boolean degrees = (panelID != null && frame != null && !frame.getAnglesInRadians());

		// refresh the coordStringBuilder
		coordStringBuilder.setUnitsAndPatterns(track, xTitle, yTitle);
		// refresh the main dataset
		refreshDataset(dataset, datasetManager, xIsAngle, yIsAngle, degrees);
		// add dataset to this plot panel
		addDrawable(dataset);

		int n = guests.size();
		if (n > 0) {
			// refresh guest datasets
			// first eliminate any guests that may have been deleted
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			ArrayList<TTrack> tracks = trackerPanel.getTracks();
			// BH count down for removal
			
			for (int i = n; --i >= 0;) {
				// check if guest still exists in tracker panel
				if ((track = guests.get(i)) != null && trackerPanel.getTrack(track.getName(), tracks) == null) {
					guests.remove(i);
					n--;
				}
			}
			tracks.clear();
			// now add drawables for guests
			if (n > 0 && track != null) {
				for (int i = 0; i < n; i++) {
					track = guests.get(i);
					DatasetManager manager = track.getData(track.tp, plotTrackView.datasetIndex);
					HighlightableDataset dataset = guestDatasets.get(track);
					dataset.setMarkerColor(track.getColor());
					dataset.setHighlightColor(track.getColor());
					refreshDataset(dataset, manager, xIsAngle, yIsAngle, degrees);
					addDrawable(dataset);
				}
			}
		}

		if (track instanceof LineProfile)
			return; // no highlights on line profile plots since not frame-based data

		// refresh highlighted indices
		BitSet bsSteps = bsFrameHighlights;
		if (track != null && !track.dataFrames.isEmpty()) {
			int frameNum = bsSteps.nextSetBit(0);
			if (bsSteps.cardinality() != 1 || track.dataFrames.size() <= frameNum
					|| track.dataFrames.get(frameNum) != frameNum) {
				bsSteps = new BitSet();
				int dataIndex = 0;
				int end = track.dataFrames.size();
				outer: for (; frameNum >= 0; frameNum = bsFrameHighlights.nextSetBit(frameNum + 1)) {
					for (; dataIndex < end; dataIndex++) {
						int k = track.dataFrames.get(dataIndex);
						if (k == frameNum) {
							bsSteps.set(dataIndex);
							continue outer;
						}
					}
				}
			}
		}
		dataset.setHighlights(bsSteps);

		// refresh plot coordinates
		int plotIndex = -1;
		if (bsSteps.cardinality() == 1) {
			plotIndex = bsSteps.nextSetBit(0);
		}
		showPlotCoordinates(plotIndex);

	}

	/**
	 * Refreshes the data in a dataset based on current x and y index.
	 * 
	 * @param hds      the dataset to refresh
	 * @param manager  the DatasetManager with the data columns
	 * @param xIsAngle true if the x index is an angle
	 * @param yIsAngle true if the y index is an angle
	 * @param degrees  true if angle units are degrees
	 */
	protected void refreshDataset(HighlightableDataset hds, DatasetManager manager, boolean xIsAngle, boolean yIsAngle,
			boolean degrees) {

		// get the dataset for the current x and y indices
		// assign quasi-unique ID to dataset based on data and indices
		int id = manager.hashCode() & 0xffff;
		hds.setID(id + xIndex * 100 + yIndex * 10);
		// set the track-specified dataset properties
		hds.setConnected(dataset.isConnected());
		hds.setMarkerShape(dataset.getMarkerShape());

		// clear and refill dataset with x- and y-axis variables
		hds.clear();
		// xIndex == -1 indicates x column variable (same for all datasets)
		// xIndex >= 0 indicates y column variable of specified dataset
		Dataset xData = manager.getDataset(xIndex >= 0 ? xIndex : 0);
		Dataset yData = manager.getDataset(yIndex);
		xData.setYColumnVisible(true);
		yData.setYColumnVisible(true);
		int xcol = (xIndex >= 0 ? 1 : 0);
		// use x mean value for filler points (y = Double.NaN)
		double xMean = getMean(xcol == 1 ? xData.getYPoints() : xData.getXPoints());
		// append data to dataset
		int n;
		// x == x here means !Double.isNaN(x);
		if (xMean != xMean || (n = yData.getRowCount()) == 0)
			return;
		
//		long t0 = System.currentTimeMillis();
		//OSPLog.debug("TrackPlottingPanel refreshDataSet " + toString() + " " + xIndex + " " + yIndex + " t0=" + t0);

		double[] _x = new double[n];
		double[] _y = new double[n];
		for (int i = 0; i < n; i++) {
			double x = xData.getValueAt(i, xcol);
			double y = yData.getValueAt(i, 1);
			if (x == x) {
				if (xIsAngle && degrees) {
					x *= 180 / Math.PI;
				}
				if (y == y && yIsAngle && degrees) {
					y *= 180 / Math.PI;
				}
			} else {
				x = xMean;
				y = Double.NaN;
			}
			_x[i] = x;
			_y[i] = y;
		}
		hds.append(_x, _y);
		//OSPLog.debug("TrackPlottingPanel refreshDataSet2 " + (System.currentTimeMillis() - t0));
	}

	/**
	 * Shows plot coordinates at the specified dataset index.
	 * 
	 * @param index the index
	 */
	protected void showPlotCoordinates(int index) {
		String msg = ""; //$NON-NLS-1$
		if (index >= 0 && dataset.getIndex() > index) {
			double x = dataset.getXPoints()[index];
			double y = dataset.getYPoints()[index];
			TTrack track = TTrack.getTrack(trackID);
			msg = coordStringBuilder.getCoordinateString(track.tp, x, y);
			setMessage(msg, MessageDrawable.BOTTOM_LEFT);
		}
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
	@Override
	public void setPreferredMinMax(double xmin, double xmax, double ymin, double ymax, boolean invalidateImage) {
		frame.getTrackerPanelForID(panelID).changed = true;
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
	@Override
	public void setPreferredMinMaxX(double xmin, double xmax) {
		frame.getTrackerPanelForID(panelID).changed = true;
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
	@Override
	public void setPreferredMinMaxY(double ymin, double ymax) {
		frame.getTrackerPanelForID(panelID).changed = true;
		isCustom = true;
		super.setPreferredMinMaxY(ymin, ymax);
	}

	/**
	 * Overrides requestFocusInWindow. This declines the focus if scale setter is
	 * visible.
	 */
	@Override
	public boolean requestFocusInWindow() {
		return plotAxes.getScaleSetter().isVisible() && super.requestFocusInWindow();
	}
	
// BH note that all other highlights use frameNumber directly, not getDataIndex	
//	/**
//	 * Adds a highlight for the specified frame number.
//	 *
//	 * @param frameNumber the frame number
//	 */
//	protected void addHighlight(int frameNumber) {
//		// add data index to highlightIndices if found
//		int index = TTrack.getTrack(trackID).getDataIndex(frameNumber);
//		if (index > -1)
//			bsHighlight.set(index);
//	}

	/**
	 * Sets the x variable by name.
	 *
	 * @param name the name of the dataset to plot on the x axis
	 */
	protected void setXVariable(String name) {
		int n = getVarIndexFromName(name);
		switch (n) {
		case VAR_NAME_NULL:
			break;
		case VAR_NOT_FOUND:
			xName = name;
			break;
		default:
			xName = name;
			if (xIndex != n) {
				xIndex = n;
				if (plotTrackView != null)
					plotTrackView.syncXAxesTo(this);
			}
			break;
		}
	}

	/**
	 * Gets the x variable name.
	 *
	 * @return the name of the x variable
	 */
	protected String getXVariable() {
		return xName;
	}

	/**
	 * Sets the y variable by name.
	 *
	 * @param name the name of the dataset to plot on the y axis
	 */
	protected void setYVariable(String name) {
		int n = getVarIndexFromName(name);
		switch (n) {
		case VAR_NAME_NULL:
			break;
		case VAR_NOT_FOUND:
			yName = name;
			break;
		default:
			yName = name;
			if (yIndex != n) {
				yIndex = n;
				super.setPreferredMinMaxY(Double.NaN, Double.NaN);
			}
			break;
		}
	}

	/**
	 * htVarToItem is set to the Dataset index for both X and Y, with 
	 * -1 reserved for the independent variable (usually t).
	 * 
	 * @param name with or without definition and with or without subscripts
	 * 
	 * @return
	 */
	private int getVarIndexFromName(String name) {
		if ((name = TrackView.trimDefined(name)) == null)
			return VAR_NAME_NULL;
		Integer ii = htVarToItem.get(name);
		return (ii == null ? VAR_NOT_FOUND : ii.intValue());
	}

	/**
	 * Gets the y variable name.
	 *
	 * @return the name of the y variable, without the defined information
	 */
	protected String getYVariable() {
		return yName;
	}

	/**
	 * Sets the PlotTrackView that owns this.
	 * 
	 * @param view the PlotTrackView
	 */
	protected void setPlotTrackView(PlotTrackView view) {
		if (playerListener == null) {
			playerListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					if (clickedStep == null)
						return;
					TPoint pt = clickedStep.getDefaultPoint();
					TrackerPanel trackerPanel = getPlotPanel();
					trackerPanel.setSelectedPoint(pt);
					if (pt != null) {
						pt.showCoordinates(trackerPanel);
					}
					clickedStep = null;
					TFrame.repaintT(TrackPlottingPanel.this);
				}
			};
		}
		plotTrackView = view;
		TrackerPanel trackerPanel = getPlotPanel();
		VideoPlayer player = trackerPanel.getPlayer();
		player.removePropertyChangeListener(VideoPlayer.PROPERTY_VIDEOPLAYER_STEPNUMBER, playerListener); //$NON-NLS-1$
		player.addPropertyChangeListener(VideoPlayer.PROPERTY_VIDEOPLAYER_STEPNUMBER, playerListener); //$NON-NLS-1$
	}

	private TrackerPanel getPlotPanel() {
		return plotTrackView.frame.getTrackerPanelForID(plotTrackView.panelID);
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

	protected void createXYPopups() {
		xPopup = new JPopupMenu();
		yPopup = new JPopupMenu();
		createVarItems();
		for (int i = 0; i < xChoices.length; i++) 
			xPopup.add(xChoices[i]);
		for (int i = 0; i < yChoices.length; i++) 
			yPopup.add(yChoices[i]);
		String def = TrackerRes.getString("TView.Menuitem.Define");
		JMenuItem item = new JMenuItem(def); //$NON-NLS-1$
		item.addActionListener(dataFunctionListener);
		xPopup.addSeparator();
		xPopup.add(item);
		item = new JMenuItem(def); //$NON-NLS-1$
		item.addActionListener(dataFunctionListener);
		yPopup.addSeparator();
		yPopup.add(item);
	}
	
	protected void createVarItems() {
		// make listeners for the button states
		xListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (selectionEnabled && e.getStateChange() == ItemEvent.SELECTED) {
					JMenuItem item = (JMenuItem) e.getSource();
					setXVariable(item.getText());
					plotData();
					isCustom = true;
					frame.getTrackerPanelForID(panelID).changed = true;
					repaint();
				}
			}
		};
		yListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (selectionEnabled && e.getStateChange() == ItemEvent.SELECTED) {
					JMenuItem item = (JMenuItem) e.getSource();
					setYVariable(item.getText());
					plotData();
					isCustom = true;
					frame.getTrackerPanelForID(panelID).changed = true;
					repaint();
				}
			}
		};
		xGroup = new ButtonGroup();
		yGroup = new ButtonGroup();
		// create radio buttons and popups to set x and y variables
		xChoices = new JRadioButtonMenuItem[datasetCount + 1];
		yChoices = new JRadioButtonMenuItem[datasetCount];
		TTrack track = TTrack.getTrack(trackID);
		for (Entry<String, Integer> e : htVarToItem.entrySet()) {
			String name = e.getKey();
			int i = e.getValue().intValue() + 1;
			String desc = track.getDataDescription(i);
			if (desc != null && desc.length() > 0)
				name += TrackView.DEFINED_AS + track.getDataDescription(i);
			xChoices[i] = new JRadioButtonMenuItem(name);
			xChoices[i].setFont(font);
			xChoices[i].setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
			xChoices[i].addItemListener(xListener);
			xGroup.add(xChoices[i]);
			if (i == 0)
				continue;
			i--;
			yChoices[i] = new JRadioButtonMenuItem(name);
			yChoices[i].setFont(font);
			yChoices[i].setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
			yChoices[i].addItemListener(yListener);
			yGroup.add(yChoices[i]);
		}
	}

	public void updateVarSelection() {
		// prevent listener actions while setting
		selectionEnabled = false;
		xChoices[xIndex + 1].setSelected(true);
		yChoices[yIndex].setSelected(true);
		selectionEnabled = true;
	}

	protected void setVariables() {
		// find the selected menu item
		datasetCount = datasetManager.getDatasetsRaw().size();
		boolean smaller = yChoices == null ? false : datasetCount < yChoices.length;
		String xName = getXVariable();
		String yName = getYVariable();
		xPopup = yPopup = null;
		htVarToItem.clear();
		TTrack track = TTrack.getTrack(trackID);
		boolean foundY = false, foundX = false;
		String name = TeXParser.removeSubscripting(track.getDataName(0)); // linked x-variable
		htVarToItem.put(name,  Integer.valueOf(-1));
		xIndex = -1;
		if (name == xName)
			foundX = true;
		for (int i = 0; i < datasetCount; i++) { // y-variables
			name = TeXParser.removeSubscripting(track.getDataName(i + 1));
			boolean isXVar = name.equals(xName);
			boolean isYVar = name.equals(yName);
			htVarToItem.put(name,  Integer.valueOf(i));
			if (isXVar) {
				xIndex = i;
				foundX = true;
			}
			if (isYVar) {
				yIndex = i;
				this.yName = yName;
				foundY = true;
			}
		}
		// add define data function items to x and y lists
		// check indices
		if (xIndex >= datasetCount || (smaller && !foundX))
			xIndex = -1;
		if (yIndex >= datasetCount || (smaller && !foundY))
			yIndex = 0;
		if (!foundX)
			setXVariable(xName);
		if (!foundY)
			setYVariable(yName);
	}

	private void copyProperties(Dataset source, Dataset destination) {
		XMLControl control = new XMLControlElement(source); // convert the source to xml
		Dataset.getLoader().loadObject(control, destination); // copy the data to the destination
	}

	/**
	 * Pads a dataset with NaN values where needed.
	 *
	 * @param dataset   the dataset
	 * @param newXArray expanded array of independent variable values
	 */
	private void padDataset(Dataset dataset, double[] newXArray) {
		double[] xArray = dataset.getXPoints();
		double[] yArray = dataset.getYPoints();
		Map<Double, Double> valueMap = new HashMap<Double, Double>();
		for (int k = 0; k < xArray.length; k++) {
			valueMap.put(xArray[k], yArray[k]);
		}
		// pad y-values of nextOut with NaN where needed
		double[] newYArray = new double[newXArray.length];
		for (int k = 0; k < newXArray.length; k++) {
			double x = newXArray[k];
			newYArray[k] = valueMap.keySet().contains(x) ? valueMap.get(x) : Double.NaN;
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
		@Override
		public ScaleSetter getScaleSetter() {
			ScaleSetter setter = super.getScaleSetter();
			FontSizer.setFonts(setter);
			return setter;
		}

		// Overrides CartesianInteractive method
		@Override
		protected boolean hasHorzVariablesPopup() {
			return true;
		}

		// Overrides CartesianInteractive method
		@Override
		protected JPopupMenu getHorzVariablesPopup() {
			if (xPopup == null)
				createXYPopups();
			FontSizer.setFonts(xPopup, FontSizer.getLevel());
			updateVarSelection();
			return xPopup;
		}

		// Overrides CartesianInteractive method
		@Override
		protected boolean hasVertVariablesPopup() {
			return true;
		}

		// Overrides CartesianInteractive method
		@Override
		protected JPopupMenu getVertVariablesPopup() {
			if (yPopup == null)
				createXYPopups();
			FontSizer.setFonts(yPopup, FontSizer.getLevel());
			updateVarSelection();
			return yPopup;
		}

	}

	@Override
	public boolean isShowCoordinates() {
//		boolean inside = region == CartesianInteractive.INSIDE
//				&& getCursor() == Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		boolean inside = mouseListener.region == CartesianInteractive.INSIDE;
		boolean stepSelected = getPlotPanel().selectedSteps.size() == 1;
		return inside && !stepSelected && showCoordinates;
	}

	/**
	 * A Mouse Listener that selects data points and displays the Data Tool.
	 */
	class PlotMouseListener extends MouseInputAdapter {
		int region;
		Interactive iad;

		@Override
		public void mouseEntered(MouseEvent e) {
			mouseEvent = e;
			mouseAction = MOUSE_ENTERED;
//      requestFocusInWindow();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			mouseEvent = e;
			mouseAction = MOUSE_EXITED;
			setMouseCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			mouseEvent = e;
			mouseAction = MOUSE_MOVED;
			TTrack track = TTrack.getTrack(trackID);
			if (!(track instanceof LineProfile))
				iad = getInteractive();
			Point p = e.getPoint();
			region = getRegion(p);
//			setShowCoordinates(region == CartesianInteractive.INSIDE
//					&& getCursor() == Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		
			if (region == CartesianInteractive.INSIDE) {
				setToolTipText(TrackerRes.getString("TrackPlottingPanel.RightDrag.Hint")); //$NON-NLS-1$
				if (isShowCoordinates()) {
					if (iad == dataset)
						showPlotCoordinates(dataset.getHitIndex());
					else
						displayCoordinates(e);
				}
			} else {
				setToolTipText(null);
				if (getPlotPanel().selectedSteps.size() != 1)
					setMessage(null, 0);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			mouseEvent = e;
			mouseAction = MOUSE_PRESSED;
			Point p = e.getPoint();
			region = getRegion(p);
			TTrack track = TTrack.getTrack(trackID);
			// if dataset is iad, select data point
			if (iad == dataset) {
				TrackerPanel trackerPanel = getPlotPanel();
				showPlotCoordinates(dataset.getHitIndex());
				// determine frame number
				int frame = track.getFrameForData(getXLabel(), getYLabel(),
						new double[] { dataset.getX(), dataset.getY() });
				if (frame > -1) {
					Step step = track.getStep(frame);
					StepSet steps = trackerPanel.selectedSteps;
					if (e.isControlDown()) {
						// add or remove step
						if (step != null) {
							if (steps.contains(step))
								steps.remove(step);
							else
								steps.add(step);
							step.erase();
							fireRepaint(track);
						}
					} else if (step != null ){
						VideoPlayer player = trackerPanel.getPlayer();
						if (player.getFrameNumber() == frame) {
							TPoint pt = step.getDefaultPoint();
							trackerPanel.setSelectedPoint(pt);
							if (pt != null) {
								pt.showCoordinates(trackerPanel);
							}
							step.erase();
							fireRepaint(track);
						}
						else {
							// set clickedStep so TrackPlottingPanel will select it later
							clickedStep = step;
							int stepNumber = player.getVideoClip().frameToStep(frame);
							player.setStepNumber(stepNumber);
						}
					}
					return;
				}
			} else if (region == CartesianInteractive.INSIDE && e.getClickCount() == 2
					&& frame.getTrackerPanelForID(panelID).isEnabled("data.tool")) { //$NON-NLS-1$ // double click
				showDataTool();
			}
//			displayCoordinates(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			mouseEvent = e;
			mouseAction = MOUSE_DRAGGED;
			Point p = e.getPoint();
			region = getRegion(p);
			if (getInteractive() == null) {
				if (region != CartesianInteractive.INSIDE) {
					setMouseCursor(Cursor.getDefaultCursor());
					if (isShowCoordinates())
						setMessage(null, MessageDrawable.BOTTOM_LEFT);
				} else
					setMouseCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
			TFrame.repaintT(TrackPlottingPanel.this);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mouseEvent = e;
			mouseAction = MOUSE_RELEASED;
			TTrack track = TTrack.getTrack(trackID);
			if (!(track instanceof LineProfile) && getInteractive() != null)
				setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//			if (getCursor() == Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)) {
//	      messages.setMessage(null, MessageDrawable.BOTTOM_LEFT);  //BL message box
//			}
		}

		@Override
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
				if (p.x < l || p.y < t || p.x > plotDim.width - r || p.y > plotDim.height - b) {
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

	public void fireRepaint(TTrack track) {
		TFrame.repaintT(frame.getTrackerPanelForID(panelID));
		track.fireStepsChanged();
	}

	/**
	 * A class to save and load object data.
	 */
	static class Loader implements XML.ObjectLoader {

		/**
		 * Saves object data.
		 *
		 * @param control the control to save to
		 * @param obj     the TrackerPanel object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			TrackPlottingPanel plot = (TrackPlottingPanel) obj;
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
			control.setValue("points", plot.dataset.getMarkerShape() != Dataset.NO_MARKER); //$NON-NLS-1$
			if (!plot.guests.isEmpty()) {
				String[] guestNames = new String[plot.guests.size()];
				for (int i = 0; i < guestNames.length; i++) {
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
		@Override
		public Object createObject(XMLControl control) {
			return null;
		}

		/**
		 * Loads an object with data from an XMLControl.
		 *
		 * @param control the control
		 * @param obj     the object
		 * @return the loaded object
		 */
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			TrackPlottingPanel plot = (TrackPlottingPanel) obj;
			String[] vars = new String[2];
			vars[0] = control.getString("x_var"); //$NON-NLS-1$
			vars[1] = control.getString("y_var"); //$NON-NLS-1$
			// convert legacy variable names
			TTrack track = TTrack.getTrack(plot.trackID);
			boolean isPointMass = (track instanceof PointMass);
			for (int i = 0; i < 2; i++) {
				if (vars[i] != null) {
					switch (vars[i]) {
					case "theta":
						vars[i] = (isPointMass ? "\u03b8r" : "\u03b8"); //$NON-NLS-1$ $NON-NLS-2$
						break;
					case "theta_v": //$NON-NLS-1$
						vars[i] = "\u03b8v"; //$NON-NLS-1$ 
						break;
					case "theta_a": //$NON-NLS-1$
						vars[i] = "\u03b8a"; //$NON-NLS-1$ 
						break;
					case "theta_p": //$NON-NLS-1$
						vars[i] = "\u03b8p"; //$NON-NLS-1$
						break;
					case "n":
						if (isPointMass)
							vars[i] = "step"; //$NON-NLS-1$
						break;
					case "KE": //$NON-NLS-1$
						vars[i] = "K"; //$NON-NLS-1$
						break;
					case "x-comp": //$NON-NLS-1$
						vars[i] = "x"; //$NON-NLS-1$
						break;
					case "y-comp": //$NON-NLS-1$
						vars[i] = "y"; //$NON-NLS-1$
						break;
					case "x_tail": //$NON-NLS-1$
						vars[i] = "xtail"; //$NON-NLS-1$
						break;
					case "y_tail": //$NON-NLS-1$
						vars[i] = "ytail"; //$NON-NLS-1$
						break;
					}
				}
			}
			plot.setXVariable(vars[0]);
			plot.setYVariable(vars[1]);
			if (control.getBoolean("scaled")) { //$NON-NLS-1$
				double xmin = control.getDouble("xmin"); //$NON-NLS-1$
				double xmax = control.getDouble("xmax"); //$NON-NLS-1$
				double ymin = control.getDouble("ymin"); //$NON-NLS-1$
				double ymax = control.getDouble("ymax"); //$NON-NLS-1$
				plot.setPreferredMinMax(xmin, xmax, ymin, ymax, false);
			}
			if (control.getPropertyNamesRaw().contains("lines")) //$NON-NLS-1$
				plot.dataset.setConnected(control.getBoolean("lines")); //$NON-NLS-1$
			if (control.getPropertyNamesRaw().contains("points")) { //$NON-NLS-1$
				if (control.getBoolean("points")) { //$NON-NLS-1$
					plot.dataset.setMarkerShape(Dataset.SQUARE);
				} else
					plot.dataset.setMarkerShape(Dataset.NO_MARKER);
			}
			String[] guestnames = (String[]) control.getObject("guests"); //$NON-NLS-1$
			if (guestnames != null) {
				TrackerPanel trackerPanel = plot.frame.getTrackerPanelForID(plot.panelID);
				ArrayList<TTrack> tracks = trackerPanel.getTracks();
				for (String name : guestnames) {
					TTrack guest = trackerPanel.getTrack(name, tracks);
					plot.addGuest(guest);
				}
			}
			plot.plotData();
			return obj;
		}
	}

	public void showDataTool() {
		DataTool tool = DataTool.getTool(true);
		DataToolTab tab = tool.getTab(datasetManager);
		tool.setUseChooser(false);
		tool.setSaveChangesOnClose(false);
		DatasetManager toSend = new DatasetManager();
		DataRefreshTool refresher = DataRefreshTool.getTool(datasetManager);
		toSend.setID(datasetManager.getID());
		TTrack track = TTrack.getTrack(trackID);
		toSend.setName(track.getName());
		int i = 0;
		// always include linked independent variable first
		Dataset nextIn = datasetManager.getDataset(0);
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
			for (double t : tArray) {
				tSet.add(t);
			}
			for (TTrack guest : guests) {
				DatasetManager guestData = guest.getData(guest.tp, plotTrackView.datasetIndex);
				Dataset nextGuestIn = guestData.getDataset(0);
				double[] guestTArray = nextGuestIn.getXPoints();
				for (double t : guestTArray) {
					tSet.add(t);
				}
			}
			tArray = new double[tSet.size()];
			Double[] temp = tSet.toArray(new Double[tArray.length]);
			for (int k = 0; k < tArray.length; k++) {
				tArray[k] = temp[k];
			}
			// finished expanding tArray
			// pad nextOut with NaNs
			padDataset(nextOut, tArray);
		}
		// add the x and y datasets
		if (xIndex >= 0) {
			nextIn = datasetManager.getDataset(xIndex);
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
		nextIn = datasetManager.getDataset(yIndex);
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
		for (TTrack guest : guests) {
			DatasetManager guestData = guest.getData(guest.tp);
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
				if (tab != null) {
					String newName = tab.getColumnName(nextOut.getID());
					if (newName != null) {
						nextOut.setXYColumnNames(nextOut.getXColumnName(), newName);
					}
				} else {
					String newName = nextOut.getYColumnName() + "_{" + guest.getName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
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
				if (tab != null) {
					String newName = tab.getColumnName(nextOut.getID());
					if (newName != null) {
						nextOut.setXYColumnNames(nextOut.getXColumnName(), newName);
					}
				} else {
					String newName = nextOut.getYColumnName() + "_{" + guest.getName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
					nextOut.setXYColumnNames(nextOut.getXColumnName(), newName);
				}
				// pad nextOut with NaNs
				padDataset(nextOut, tArray);
			}
		}
		// get data tool and send it the job
		tool.send(new LocalJob(toSend), refresher);
		tab = tool.getTab(toSend);
		if (tab != null) {
			tab.setWorkingColumns(xColName, yColName);
		}
		tool.setVisible(true);
	}

	public void clearPopup() {
		popup = null;
		popupmenu = null;
	}

	public void setHighlights(BitSet highlightFrames) {
		bsFrameHighlights.clear();
		bsFrameHighlights.or(highlightFrames);
	}

	@Override
	public void repaint() {
		if (panelID == null || !frame.getTrackerPanelForID(panelID).isPaintable()) {
			return;
		}
		super.repaint();
	}


	@Override
	public void dispose() {
		//System.out.println("TrackPlottingPanel.dispose " + panelID);
		if (playerListener != null) {
			getPlotPanel().getPlayer()
					.removePropertyChangeListener(VideoPlayer.PROPERTY_VIDEOPLAYER_STEPNUMBER, playerListener); // $NON-NLS-1$
		}
		for (TTrack guest : guests) {
			guest.removeStepListener(plotTrackView); // $NON-NLS-1$
		}
		guests.clear();
		guestDatasets.clear();
		datasetManager = null;
		plotTrackView = null;
		panelID = null;
		frame = null;
		// MEMORY LEAK -- was missing super.dispose()
		super.dispose();
	}

	@Override
	public void finalize() {
		OSPLog.finalized(this);
	}

	@Override
	public String toString() {
		return "[TrackPlottingPanel " + id + " " + TTrack.getTrack(trackID).getName() + " " + yName + " vs. " + xName + " ]"; 
	}


	
}
