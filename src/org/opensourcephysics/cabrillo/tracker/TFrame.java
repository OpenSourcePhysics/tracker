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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ClipInspector;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPanel;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.media.mov.MovieVideoI;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.LaunchPanel;
import org.opensourcephysics.tools.Launcher;
import org.opensourcephysics.tools.Launcher.HTMLPane;
import org.opensourcephysics.tools.LibraryBrowser;
import org.opensourcephysics.tools.LibraryComPADRE;
import org.opensourcephysics.tools.LibraryResource;
import org.opensourcephysics.tools.LibraryTreePanel;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

import javajs.async.AsyncSwingWorker;

/**
 * This is the main frame for Tracker.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TFrame extends OSPFrame implements PropertyChangeListener {

	static {
		ToolTipManager.sharedInstance().setDismissDelay(2000);
	}

	// preloading for JavaScript
	
	public static Font textLayoutFont = new JTextField().getFont();

	static {
		new TextLayout("X", textLayoutFont, OSPRuntime.frc);
	}

	// static fields

	private class TTabPanel extends JPanel {

		private Object[] objects;
		private TrackerPanel trackerPanel;
		Box toolbarBox;

		public TTabPanel(TrackerPanel trackerPanel, Object[] objects) {
			super(new BorderLayout());
			this.trackerPanel = trackerPanel;
			this.objects = objects;
		}

		public TrackerPanel getTrackerPanel() {
			return trackerPanel;
		}

		public Object[] getObjects() {
			return objects;
		}
		
		public void setToolbarVisible(boolean vis) {
			if (toolbarBox ==  null) {
				toolbarBox = Box.createVerticalBox();
				toolbarBox.add((JToolBar) objects[TFRAME_TOOLBAR]);
//				toolbarBox.add((TTrackBar) objects[TFRAME_TRACKBAR]);
			}
			if (vis) {
				add(toolbarBox, BorderLayout.NORTH);
			}
			else {
				remove(toolbarBox);
			}
		}

		@Override
		public void paintComponent(Graphics g) {
			if (!isPaintable())
				return;
			super.paintComponent(g);
		}

		public void dispose() {
			trackerPanel = null;
			objects = null;
		}

	}

	public static final String PROPERTY_TFRAME_TAB = "tab";
	public static final String PROPERTY_TFRAME_RADIANANGLES = "radian_angles";
	public static final String PROPERTY_TFRAME_WINDOWFOCUS = "windowfocus";

	protected final static String HELP_PATH = "/org/opensourcephysics/cabrillo/tracker/resources/help/"; //$NON-NLS-1$
	protected final static String WEB_HELP_PATH = "https://physlets.org/tracker/help/"; //$NON-NLS-1$
	protected final static Color YELLOW = new Color(255, 255, 105);
	private final static int DEFAULT_DIVIDER_SIZE = 10;
	private final static double MIN_DIVIDER_OFFSET = 0.07;

	private static final int TFRAME_MAINVIEW = 0;
	private static final int TFRAME_VIEWCHOOSERS = 1;
	private static final int TFRAME_SPLITPANES = 2;
	private static final int TFRAME_TOOLBAR = 3;
	private static final int TFRAME_MENUBAR = 4;
	private static final int TFRAME_TRACKBAR = 5;
	
	private static final int DEFAULT_VIEWS = 0;
	private static final int OTHER_VIEWS = 1;

	protected static final double DEFAULT_MAIN_DIVIDER = 0.67;
	protected static final double DEFAULT_RIGHT_DIVIDER = 0.57;
	protected static final double DEFAULT_LEFT_DIVIDER = 0.57;
	protected static final double DEFAULT_BOTTOM_DIVIDER = 0.50;

	private static boolean isPortraitLayout, isLayoutChanged, isLayoutAdaptive;
	public static boolean haveExportDialog = false;
	public static boolean haveThumbnailDialog = false;
	

	// instance fields
	private JToolBar playerBar;
	private JPopupMenu popup = new JPopupMenu();
	private JMenuItem closeItem;
	private JMenuBar defaultMenuBar;
	private JMenu recentMenu;
//	private Map<JPanel, Object[]> panelObjects = new HashMap<JPanel, Object[]>();
	protected JTabbedPane tabbedPane;
	protected JTextPane notesTextPane;
	protected Action saveNotesAction;
	protected JButton cancelNotesDialogButton, closeNotesDialogButton;
	protected JCheckBox displayWhenLoadedCheckbox;
	protected JDialog notesDialog;
	protected JDialog helpDialog;
	protected LibraryBrowser libraryBrowser;
	protected Launcher helpLauncher;
	protected JDialog dataToolDialog;
	protected TrackerPanel prevPanel;
	protected FileDropHandler fileDropHandler;
	protected Action openRecentAction;
	protected boolean splashing = true;
	protected ArrayList<String> loadedFiles = new ArrayList<String>();
	protected boolean anglesInRadians = Tracker.isRadians;
	protected File tabsetFile; // used when saving tabsets
	protected int framesLoaded, prevFramesLoaded; // used when loading xuggle videos
//  protected JProgressBar monitor;
	protected PrefsDialog prefsDialog;
	protected ClipboardListener clipboardListener;
	protected boolean alwaysListenToClipboard;
	private String mylang = "en";
	private JMenu languageMenu;
	protected int maximizedView = -1;
	private DataDropHandler dataDropHandler;

	/**
	 * Create a map of known arguments, setting any found arguments to null.
	 * Integer arguments are stringified and rounded in case JavaScript is passing numbers.
	 * 
	 * @param args
	 * @return HashMap
	 */
	static Map<String, Object> parseArgs(String[] args) {
		Map<String, Object> map = new HashMap<>();
		
		if (args == null || args.length == 0)
			return map;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i]; 
			if (arg == null)
				continue;
			int i1 = i;
			try {
				switch (arg) {
				case "-adaptive":
					args[i] = null;
					map.put("-adaptive", true);
					break;
				case "-bounds":
					args[i] = null;
					i1 = i + 4;
					int bx = getIntArg(args, ++i); 
					int by = getIntArg(args, ++i);
					int bw = getIntArg(args, ++i);
					int bh = getIntArg(args, ++i);
					map.put("-bounds", new Rectangle(bx, by, bw, bh));
					break;
				case "-dim":
					args[i] = null;
					i1 = i + 2;
					int w = getIntArg(args, ++i);
					int h = getIntArg(args, ++i);
					map.put("-dim", new Dimension(w, h));
					break;
				}
			} catch (NumberFormatException e) {
				System.err.println("Tracker: Could not parse argument " + arg);
				i = i1;
			}
		}
		System.out.println("Tracker.parseArgs: " + map);
		return map;
	}


	private static int getIntArg(String[] args, int i) throws NumberFormatException {
		String a = args[i];
		args[i] = null;
		/**
		 * @j2sNative return a|0;
		 */
		{
			return Integer.parseInt(a);
		}
	}


	/**
	 * Constructs an empty TFrame.
	 */
	public TFrame() {
		super("Tracker"); //$NON-NLS-1$
		init(null);
	}
	
	/**
	 * Constructs a TFrame with the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public TFrame(TrackerPanel trackerPanel) {
		super("Tracker"); //$NON-NLS-1$
		Map<String, Object> options = new HashMap<>();
		options.put("-panel", trackerPanel);
		init(options);
	}

	/**
	 * 
	 * @param options include optional -dim Dimension [-video Video | -panel TrackerPanel]
	 */
	public TFrame(Map<String, Object> options) {
		super("Tracker"); //$NON-NLS-1$
		init(options);
	}
	
	private void init(Map<String, Object> options) {
		if (options == null)
			options = new HashMap<>();
		isLayoutAdaptive = OSPRuntime.isJS && options.get("-adaptive") != null;
//		isLayoutAdaptive = true; // pig for testing
		Dimension dim = (Dimension) options.get("-dim");
		Rectangle bounds = (Rectangle) options.get("-bounds");
		Video video = (Video) options.get("-video");
		TrackerPanel panel = (video != null ? new TrackerPanel(video) : (TrackerPanel) options.get("-panel"));

		setName("Tracker"); //$NON-NLS-1$
		if (Tracker.TRACKER_ICON != null)
			setIconImage(Tracker.TRACKER_ICON.getImage());
		// set default close operation
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		TrackerRes.addPropertyChangeListener("locale", this); //$NON-NLS-1$

		// set size and limit maximized size so taskbar not covered
		Rectangle screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setMaximizedBounds(screenRect);
		// process -bounds or -dim option
		
		if (isLayoutAdaptive) {
			bounds = getAdaptiveBounds(true);
		} 
		if (bounds == null) {
			if (dim == null) {
				double extra = FontSizer.getFactor(Tracker.preferredFontLevel) - 1;
				int w = (int) Math.min(screenRect.width*0.9, (1024 + extra * 800));
				int h = (int) Math.min(screenRect.height*0.9, 3 * w / 4);
				dim = new Dimension(w, h);
			}
			// center frame on the screen
			int x = (screenRect.width - dim.width) / 2;
			int y = (OSPRuntime.isJS ? 50 : (screenRect.height - dim.height) / 2);
			// WC: place Tracker higher in html page.
			bounds = new Rectangle(x, y, dim.width, dim.height);
		} else {
			dim = new Dimension(bounds.width, bounds.height);
		}
		createGUI();
		setPreferredSize(dim);
		pack();
		setLocation(bounds.x, bounds.y);
		Rectangle rect = getBounds();
		isPortraitLayout = rect.height > rect.width;

		// set transfer handler on tabbedPane
		fileDropHandler = new FileDropHandler(this);
		// set transfer handler for CTRL-V paste
		tabbedPane.setTransferHandler(fileDropHandler);
		if (panel != null) {
			addTab(panel, ADD_NOSELECT | ADD_REFRESH, () -> {});
		}
	}


	@SuppressWarnings("unused")
	private Rectangle getAdaptiveBounds(boolean isInit) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int) (0.9 * dim.width);
		int margin = (int) (0.05 * dim.width);
		int h = (int) (0.7 * (dim.height - 80));
		Rectangle rect = new Rectangle(margin, 80, w, h);
		if (isInit) {
			Runnable onOrient = new Runnable() {

				@Override
				public void run() {
					getAdaptiveBounds(false);
				}
				
			};

			// startup
			/**
			 * @j2sNative window.addEventListener(window.onorientationchange ?
			 *            "orientationchange" : "resize", function() { 
			 *                console.log("Orientation changed");
			 *                onOrient.run$();
			 *             },
			 *            false);
			 */
		} else {
			setBounds(rect);
			validate();
			repaint();
		}
		return rect;
	}


	/**
	 * All repaints funnel through this method
	 * 
	 */
	@Override
	public void repaint(long time, int x, int y, int w, int h) {
		if (!isPaintable())
			return;
		//OSPLog.debug("TFrame repaint " + x + " " + y + " " + w + " " + h + " " + isPaintable());
		// TFrame.addTab -> initialize -> TrackerPanel.addTrack ->
		// fire(PROPERTY_TRACKERPANEL_TRACK)
		// -> TViewChooser -> PlotTView -> TFrame.repaint();

		// Window.resize -> BorderLayout.layoutContainer -> JRootPane.reshape ->
		// TFrame.repaint()

		super.repaint(time, x, y, w, h);
	}

	/**
	 * For optimization, finding out exactly who is repainting.
	 * 
	 * @param c
	 */
	public static void repaintT(Component c) {
		if (c == null)
			return;
		if (c instanceof TrackerPanel) {
			if (!((TrackerPanel) c).isPaintable()) {
				return;
			}
			((TrackerPanel) c).clearTainted();
		}
		//OSPLog.debug(Performance.timeCheckStr("TFrame.repaintT " + c.getClass().getSimpleName(), Performance.TIME_MARK));
		c.repaint();
	}

	/**
	 * Swing does not use this method. It's only for AWT.
	 * 
	 */
	@Override
	public void update(Graphics g) {
		super.paint(g);
	}

	@Override
	public void paint(Graphics g) {
		if (!isShowing())
			return;
		// RepaintManager.paintDirtyRegions -> TFrame.paint(g)
		//OSPLog.debug("TFrame.paint");
		super.paint(g);
	}

	/**
	 * Adds a tab that displays the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @param addMode ADD_SELECT | ADD_REFRESH
	 * @param whenDone
	 */
	public void addTab(final TrackerPanel trackerPanel, int addMode, Runnable whenDone) {
		boolean doSelect = ((addMode & ADD_SELECT) != 0);
		boolean doRefresh = ((addMode & ADD_REFRESH) != 0);
		int tab = getTab(trackerPanel);
		if (tab > -1) { // tab exists
			String name = trackerPanel.getTitle();
			synchronized (tabbedPane) {
				tabbedPane.setTitleAt(tab, name);
				tabbedPane.setToolTipTextAt(tab, trackerPanel.getToolTipPath());
			}
		} else {
			setIgnoreRepaint(true);
			// tab does not already exist
			// listen for changes that affect tab title
			trackerPanel.addPropertyChangeListener(VideoPanel.PROPERTY_VIDEOPANEL_DATAFILE, this); // $NON-NLS-1$
			trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, this); // $NON-NLS-1$
			// set up trackerPanel to listen for angle format property change
			addPropertyChangeListener(PROPERTY_TFRAME_RADIANANGLES, trackerPanel); // $NON-NLS-1$
			// create the tab
			// create the tab panel components
			Tracker.setProgress(30);
			Object[] objects = new Object[6];
			objects[TFRAME_MAINVIEW] = getMainView(trackerPanel);
			objects[TFRAME_VIEWCHOOSERS] = createTViews(trackerPanel);
			objects[TFRAME_SPLITPANES] = getSplitPanes(trackerPanel);
			Tracker.setProgress(50);
			objects[TFRAME_TOOLBAR] = getToolBar(trackerPanel);
			Tracker.setProgress(60);
			objects[TFRAME_MENUBAR] = getMenuBar(trackerPanel);
			objects[TFRAME_TRACKBAR] = getTrackBar(trackerPanel);
			// put the components into the tabs map
			TTabPanel tabPanel = new TTabPanel(trackerPanel, objects);
			// add the tab
			String name = trackerPanel.getTitle();
			synchronized (tabbedPane) {
				tabbedPane.addTab(name, tabPanel);
				tab = getTab(trackerPanel);
				tabbedPane.setToolTipTextAt(tab, trackerPanel.getToolTipPath());
			}
			// from here on trackerPanel's top level container is this TFrame,
			// so trackerPanel.getFrame() method will return non-null
		}

		// handle XMLproperties loaded from trk file, if any:
		// --customViewsProperty: load custom TViews
		// --selectedViewTypesProperty: set selected view types (after May 2020)
		// --selectedViewsProperty: set selected views (legacy pre-2020)

		TViewChooser[] viewChoosers = getViewChoosers(trackerPanel);
		if (trackerPanel.customViewsProperty != null) {
			// load views in array TView[chooserIndex][viewtypeIndex]
			java.util.List<Object> arrayItems = trackerPanel.customViewsProperty.getPropertyContent();
			Iterator<Object> it = arrayItems.iterator();
			while (it.hasNext()) {
				XMLProperty next = (XMLProperty) it.next();
				if (next == null)
					continue;
				try {
					String index = next.getPropertyName().substring(1);
					index = index.substring(0, index.length() - 1);
					int chooserIndex = Integer.parseInt(index);
					XMLControl[] viewControls = next.getChildControls();
					for (int j = 0; j < viewControls.length; j++) {
						@SuppressWarnings("unchecked")
						Class<? extends TView> viewClass = (Class<? extends TView>) viewControls[j].getObjectClass();
						TView view = viewChoosers[chooserIndex].getTView(viewClass);
						if (view != null) {
							viewControls[j].loadObject(view);
							viewChoosers[chooserIndex].refresh();
							viewChoosers[chooserIndex].repaint();
//						viewChoosers[chooserIndex].setSelectedViewType(view.getViewType());
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			trackerPanel.customViewsProperty = null;
		}
		// select the view types
		if (trackerPanel.selectedViewTypesProperty != null) {
			for (Object next : trackerPanel.selectedViewTypesProperty.getPropertyContent()) {
				String viewTypeString = next.toString();
				// typical next value: "<property name="array"
				// type="string">{0,1,2,3}</property>"
				int n = viewTypeString.indexOf("{");
				if (n > -1) {
					viewTypeString = viewTypeString.substring(n + 1);
					try {
						for (int i = 0; i < viewChoosers.length; i++) {
							// set selected view types of TViewChoosers only if not current type
							int desiredType = Integer.parseInt(viewTypeString.substring(0, 1));
							int currentType = viewChoosers[i].getSelectedViewType();
							if (desiredType != currentType) {
								viewChoosers[i].setSelectedViewType(Integer.parseInt(viewTypeString.substring(0, 1)));
								viewChoosers[i].refresh();
								viewChoosers[i].repaint();
							}
							viewTypeString = viewTypeString.substring(2);
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			trackerPanel.selectedViewTypesProperty = null;
		}
		if (trackerPanel.selectedViewsProperty != null) {
			List<Object> list = trackerPanel.selectedViewsProperty.getPropertyContent();
			for (int i = 0; i < list.size() && i < viewChoosers.length; i++) {
				XMLProperty next = (XMLProperty) list.get(i);
				if (next == null)
					continue;
				String viewName = ((String) next.getPropertyContent().get(0)).toLowerCase();
				// hack to handle POSSIBLE name matches in pre-JS trk (won't work for translated
				// names)
				// Spanish here is for car.trz, specifically
				int type = viewName.contains("diagrama") || viewName.contains("plot") ? TView.VIEW_PLOT
						: viewName.contains("tabla") || viewName.contains("table") ? TView.VIEW_TABLE
								: viewName.contains("mundo") || viewName.contains("world") ? TView.VIEW_WORLD
										: viewName.contains("texto") || viewName.contains("page") ? TView.VIEW_PAGE
												: -1;
				// don't select default types (viewType==i)
				if (type != i) {
					if (viewChoosers[i].getSelectedViewType() != type) {
						// BH if the XML has listed table after plot, but plot is the selected type,
						// then the selected track will be taken from the table, not the plot. 
						// much better would be to save the selected type LAST. 
						// CupsClip.zip
						viewChoosers[i].ignoreSelectedTrack = true;
					}
					viewChoosers[i].setSelectedViewType(type);
					viewChoosers[i].refresh();
					viewChoosers[i].repaint();
				}
			}
			trackerPanel.selectedViewsProperty = null;
		}
		// select the track views for plot and table
		if (trackerPanel.selectedTrackViewsProperty != null) {
			// typical value: "mass A,null;mass B,mass B;null,null;null,null"
			String val = trackerPanel.selectedTrackViewsProperty.getPropertyContent().get(0).toString();
			String[] forChoosers = val.split(";");
			for (int i = 0; i < viewChoosers.length; i++) {
				String[] selectedNames = forChoosers[i].split(",");
				TView[] tviews = viewChoosers[i].getTViews();
				// use fact that TView.VIEW_PLOT = 0 && VIEW_TABLE = 1
				for (int k = 0; k < selectedNames.length; k++) {
					if (!selectedNames[k].equals("null") && tviews[k] != null) {
						TrackChooserTView view = (TrackChooserTView)tviews[k];
						TTrack track = trackerPanel.getTrack(selectedNames[k]);
						if (view.getSelectedTrack() != track) {
							view.setSelectedTrack(track);
							if (viewChoosers[i].getSelectedView() == view) {
								viewChoosers[i].refresh();
								viewChoosers[i].repaint();
							}
						}
					}
				}
			}
			trackerPanel.selectedViewTypesProperty = null;
		}
		placeViews(trackerPanel, viewChoosers);
		initialize(trackerPanel);

		JPanel panel = (JPanel) tabbedPane.getComponentAt(tab);
		FontSizer.setFonts(panel);
		// inform all tracks of current angle display format
		for (TTrack track : trackerPanel.getTracksTemp()) {
			track.setAnglesInRadians(anglesInRadians);
		}
		trackerPanel.clearTemp();
		setIgnoreRepaint(false);
		trackerPanel.refreshTrackData(DataTable.MODE_TAB);
		if (doSelect)
			setSelectedTab(trackerPanel);
		if (doRefresh)
			refresh();

		if (whenDone != null) {
			whenDone.run();
		}
		
		Timer timer = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TTrackBar will only refresh after TFrame is visible
				if (doRefresh)
					trackerPanel.refreshTrackBar();
					//TTrackBar.getTrackbar(trackerPanel).refresh();
				// DB following line needed to autoload data functions from external files
				trackerPanel.getDataBuilder();
				trackerPanel.changed = false;
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	/**
	 * Saves all tabs if user approved. Stops if any is canceled.
	 * 
	 * @param whenEachApproved Function to apply to each TrackerPanel unless
	 *                         canceled
	 * @param whenAllApproved  Runnable to run after all have run whenEachApproved
	 * @param whenCanceled     Runnable to run if canceled
	 */
	public void saveAllTabs(Function<TrackerPanel, Void> whenEachApproved, Runnable whenAllApproved,
			Runnable whenCanceled) {
		// save all tabs in last-to-first order
		final int[] tab = { getTabCount() - 1 };
		TrackerPanel trackerPanel = getTrackerPanel(tab[0]);
		if (trackerPanel == null)
			return;
		Runnable approved = new Runnable() {
			@Override
			public void run() {
				TrackerPanel trackerPanel = getTrackerPanel(tab[0]);
				if (whenEachApproved != null) {
					whenEachApproved.apply(trackerPanel);
				}
				tab[0]--;
				if (tab[0] > -1) {
					getTrackerPanel(tab[0]).save(this, whenCanceled);
				} else if (whenAllApproved != null)
					whenAllApproved.run();
			}
		};
		trackerPanel.save(approved, whenCanceled);
	}

	protected void relaunchCurrentTabs() {
		final ArrayList<String> filenames = new ArrayList<String>();
		saveAllTabs(new Function<TrackerPanel, Void>() {
			// for each approved
			@Override
			public Void apply(TrackerPanel trackerPanel) {
				File datafile = trackerPanel.getDataFile();
				if (datafile == null) {
					String path = trackerPanel.openedFromPath;
					if (path != null) {
						datafile = new File(path);
					}
				}
				if (datafile != null) {
					String fileName = datafile.getAbsolutePath();
					if (!filenames.contains(fileName)) {
						filenames.add(fileName);
					}
				}
				return null;
			}

		}, new Runnable() {
			// whenAllApproved
			@Override
			public void run() {
				String[] args = filenames.isEmpty() ? null : filenames.toArray(new String[0]);
				TrackerStarter.relaunch(args, false);
				// TrackerStarter exits current VM after relaunching new one
			}

		}, null); // no action when cancelled

	}

	/**
	 * Removes all tabs.
	 */
	public void removeAllTabs() {
		if (!haveContent()) {
			removeTabNow(0);
			return;
		}
		ArrayList<TrackerPanel> panels = new ArrayList<TrackerPanel>();
		boolean[] cancelled = new boolean[] {false};
		saveAllTabs(
			(trackerPanel) -> {
				// when each approved, add to list
				if (!cancelled[0])
					panels.add(trackerPanel);
				return null;
			}, 
			() -> {
				// when all approved remove tabs synchronously
				for (int i = 0; i < panels.size(); i++) {		
					new TabRemover(panels.get(i)).executeSynchronously();
				}
			}, 
			() -> {
				// if cancelled
				cancelled[0] = true;
				panels.clear();
			});
	}

	
	/**
	 * An AsyncSwingWorker to remove a tab.
	 */
	class TabRemover extends AsyncSwingWorker {

		TrackerPanel trackerPanel;
		TTabPanel tabPanel;

		TabRemover(TrackerPanel trackerPanel) {
			super(null, null, 1, 0, 1);
			this.trackerPanel = trackerPanel;
		}

		@Override
		public void initAsync() {
			tabPanel = getTabPanel(trackerPanel);
			// remove the tab immediately
			// BH 2020.11.24 thread lock
			synchronized (tabbedPane) {
				trackerPanel.trackControl.dispose();
// BH why this if the next?				tabbedPane.remove(tab);
				tabbedPane.remove(tabPanel);
			}
		}

		@Override
		public int doInBackgroundAsync(int i) {
			if (trackerPanel != null)
				finishRemoveTab(trackerPanel, tabPanel);
			return 1;
		}

		@Override
		public void doneAsync() {
		}
	}

	/**
	 * Removes a tracker panel tab.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void removeTab(TrackerPanel trackerPanel) {
		int tab = getTab(trackerPanel);
		if (tab == -1)
			return; // tab not found
		Runnable whenSaved = new Runnable() {
			@Override
			public void run() {
				// remove tab asynchronously
				new TabRemover(trackerPanel).execute();
			}
		};
		trackerPanel.save(whenSaved, null);
	}

	/**
	 * Finishes removing a tracker panel and it's TTabPanel.
	 *
	 * @param trackerPanel the tracker panel
	 * @param tabPanel     the TTabPanel
	 */
	private void finishRemoveTab(TrackerPanel trackerPanel, TTabPanel tabPanel) {
//		OSPLog.debug(Performance.timeCheckStr("TFrame.removeTab start", Performance.TIME_MARK));
		//long t0 = Performance.now(0);

//		TTabPanel tabPanel = (TTabPanel) tabbedPane.getComponentAt(tab);
//		// remove the tab
//		synchronized (tabbedPane) {
//			tabbedPane.remove(tabPanel);
//		}

		// hide the info dialog if removing the currently selected tab
//		if (tab == getSelectedTab()) {
//			notesDialog.setVisible(false);
//		}
		// remove property change listeners
		trackerPanel.removePropertyChangeListener(VideoPanel.PROPERTY_VIDEOPANEL_DATAFILE, this); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, this); // $NON-NLS-1$
		removePropertyChangeListener(PROPERTY_TFRAME_RADIANANGLES, trackerPanel); // $NON-NLS-1$

		trackerPanel.selectedPoint = null;
		trackerPanel.selectedStep = null;
		trackerPanel.selectedTrack = null;

		// inform non-modal dialogs so they close: AutoTracker, CMInspector,
		// DynamicSystemInspector,
		// AttachmentDialog, ExportZipDialog, PencilControl, TableTView, TrackControl,
		// VectorSumInspector
		firePropertyChange(PROPERTY_TFRAME_TAB, trackerPanel, null); // $NON-NLS-1$

		// clean up mouse handler
		if (trackerPanel.mouseHandler != null) {
			trackerPanel.mouseHandler.selectedTrack = null;
			trackerPanel.mouseHandler.selectedPoint = null;
			trackerPanel.mouseHandler.iad = null;
		}
		// clear filter classes
		trackerPanel.clearFilters();
		// remove transfer handler
		trackerPanel.setTransferHandler(null);

		// remove property change listeners
		trackerPanel.removePropertyChangeListener("datafile", this); //$NON-NLS-1$
		trackerPanel.removePropertyChangeListener("video", this); //$NON-NLS-1$
		removePropertyChangeListener("radian_angles", trackerPanel); //$NON-NLS-1$

		// dispose of the track control, clip inspector and player bar
//		TrackControl.getControl(trackerPanel).dispose();
		ClipInspector ci = trackerPanel.getPlayer().getVideoClip().getClipInspector();
		if (ci != null) {
			ci.dispose();
		}

		// set the video to null
		trackerPanel.setVideo(null);

		Object[] objects = tabPanel.getObjects();
		// dispose of TViewChoosers and TViews
		TViewChooser[] views = (TViewChooser[]) objects[TFRAME_VIEWCHOOSERS];
		for (int i = 0; i < views.length; i++) {
			views[i].dispose();
		}

		// clean up main view--this is important as it disposes of floating JToolBar
		// videoplayer
		MainTView mainView = (MainTView) objects[TFRAME_MAINVIEW];
		mainView.dispose();
		trackerPanel.setScrollPane(null);

		// clear the drawables AFTER disposing of main view
		ArrayList<TTrack> tracks = trackerPanel.getTracks();
		trackerPanel.clear(false);
		for (TTrack track : tracks) {
			track.dispose();
		}

//		// get the tab panel and remove components from it
//		TTabPanel tabPanel = (TTabPanel) tabbedPane.getComponentAt(tab);
//		tabPanel.removeAll();
//
//		// remove the tab
//		synchronized (tabbedPane) {
//			tabbedPane.remove(tabPanel);
//		}

//		MainTView mainView = getMainView(trackerPanel);
//		TViewChooser[] views = createViews(trackerPanel);
//		JSplitPane[] panes = getSplitPanes(trackerPanel);
//		Tracker.setProgress(50);
//		TToolBar toolbar = getToolBar(trackerPanel);
//		Tracker.setProgress(60);
//		TMenuBar menubar = getMenuBar(trackerPanel);
//		TTrackBar trackbar = getTrackBar(trackerPanel);
//		// put the components into the tabs map
//		Object[] objects = new Object[] { mainView, views, panes, toolbar, menubar, trackbar };
//		panelObjects.put(panel, objects);

		// dispose of trackbar, toolbar, menubar AFTER removing tab
		TToolBar toolbar = getToolBar(trackerPanel);
		toolbar.dispose();
		TMenuBar menubar = getMenuBar(trackerPanel);
		menubar.dispose();
		TTrackBar trackbar = getTrackBar(trackerPanel);
		trackbar.dispose();
		JSplitPane[] panes = getSplitPanes(trackerPanel);
		for (int i = 0; i < panes.length; i++) {
			JSplitPane pane = panes[i];
			pane.removeAll();
		}
		for (int i = 0; i < panes.length; i++) {
			panes[i] = null;
		}

		// remove the components from the tabs map

		TActions.getActions(trackerPanel).clear();
		TActions.actionMaps.remove(trackerPanel);
		if (prefsDialog != null) {
			prefsDialog.trackerPanel = null;
		}
		Undo.undomap.remove(trackerPanel);

		trackerPanel.dispose();
		tabPanel.dispose();

		// change menubar and show floating player of newly selected tab, if any
		TTabPanel panel = (TTabPanel) tabbedPane.getSelectedComponent();
		if (panel == null) {
			//OSPLog.debug("!!! " + Performance.now(t0) + " TFrame.removeTab");
			return;
		}
		//OSPLog.debug(Performance.timeCheckStr("TFrame.removeTab 8", Performance.TIME_MARK));
		objects = panel.getObjects();
		if (objects != null) {
			setJMenuBar((JMenuBar) objects[TFRAME_MENUBAR]);
			((TTrackBar) objects[TFRAME_TRACKBAR]).refresh();
			playerBar = ((MainTView) objects[TFRAME_MAINVIEW]).getPlayerBar();
			Container frame = playerBar.getTopLevelAncestor();
			if (frame != null && frame != this)
				frame.setVisible(true);
		} else {
			// show defaultMenuBar
			setJMenuBar(defaultMenuBar);
		}
//		synchronized (tabbedPane) {
//			tabbedPane.remove(tabPanel);
//		}

//		OSPLog.debug("!!! " + Performance.now(t0) + " finished TFrame.removeTab");
//		OSPLog.debug(Performance.timeCheckStr("TFrame.removeTab end", Performance.TIME_MARK));

	}

	/**
	 * Returns the tab index for the specified tracker panel, or -1 if no tab is
	 * found.
	 *
	 * @param trackerPanel the tracker panel
	 * @return the tab index
	 */
	public int getTab(TrackerPanel trackerPanel) {
		for (int i = 0; i < getTabCount(); i++) {
			TTabPanel panel = (TTabPanel) tabbedPane.getComponentAt(i);
			if (panel.getTrackerPanel() == trackerPanel)
				return i;
		}
		return -1;
	}

	/**
	 * Returns the tab index for the specified data file, or -1 if no tab is found.
	 *
	 * @param dataFile the data file used to load the tab
	 * @return the tab index
	 */
	public int getTab(File dataFile) {
		if (dataFile == null)
			return -1;
		try {
			String path = dataFile.getCanonicalPath();
			for (int i = getTabCount() - 1; i >= 0; i--) {
				File file = ((TTabPanel) tabbedPane.getComponentAt(i)).getTrackerPanel().getDataFile();
				if (file != null && path.equals(file.getCanonicalPath())) {
					return i;
				}
			}
		} catch (IOException e) {
		}
		return -1;
	}

	/**
	 * Gets the tab count.
	 *
	 * @return the tab count
	 */
	public int getTabCount() {
		return tabbedPane.getTabCount();
	}

	/**
	 * Gets the selected tab index.
	 *
	 * @return the tab index
	 */
	public int getSelectedTab() {
		return tabbedPane == null? -1: tabbedPane.getSelectedIndex();
	}

	/**
	 * Sets the selected tab index.
	 *
	 * @param tab the tab index
	 */
	public void setSelectedTab(int tab) {
		if (tab < 0 || tab >= getTabCount())
			return;
		tabbedPane.setSelectedIndex(tab);
		TrackerPanel trackerPanel = getTrackerPanel(tab);
		if (trackerPanel != null)
			trackerPanel.refreshNotesDialog();
	}

	/**
	 * Sets the selected tab specified by tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void setSelectedTab(TrackerPanel trackerPanel) {
		setSelectedTab(getTab(trackerPanel));
	}

	/**
	 * Gets the tracker panel at the specified tab index.
	 *
	 * @param tab the tab index
	 * @return the tracker panel
	 */
	public TrackerPanel getTrackerPanel(int tab) {
		return (tab < 0 || tab >= tabbedPane.getTabCount() ? null
				: ((TTabPanel) tabbedPane.getComponentAt(tab)).getTrackerPanel());
	}

	/**
	 * Gets the panel of the selected tab, if a tab is selected.
	 * @return the selected panel or null if no tab is selected
	 */
	public TrackerPanel getSelectedPanel() {
		return getTrackerPanel(getSelectedTab());
	}

	public void addTrackerPanel(boolean changedState, Runnable whenDone) {
		TrackerPanel newPanel = new TrackerPanel(this);
		addTab(newPanel, ADD_SELECT | ADD_NOREFRESH, () -> {
			if (!changedState)
				newPanel.changed = false;
			if (whenDone == null)
				refresh();
			else
				whenDone.run();
		});
	}

	public Object[] getObjects(int tab) {
		return (tab < 0 || tab >= tabbedPane.getTabCount() ? null
				: ((TTabPanel) tabbedPane.getComponentAt(tab)).getObjects());
	}

	/**
	 * Gets the title of the specified tab.
	 *
	 * @param tab the tab index
	 * @return the title
	 */
	public String getTabTitle(int tab) {
		return tabbedPane.getTitleAt(tab);
	}

	/**
	 * Refreshes the tab for the specified tracker panel.
	 *
	 * @param panel the tracker panel
	 */
	public void refreshTab(TrackerPanel panel) {
		int tab = getTab(panel);
		tabbedPane.setTitleAt(tab, panel.getTitle());
		tabbedPane.setToolTipTextAt(tab, panel.getToolTipPath());
	}

	/**
	 * Sets the title of the specified tab.
	 *
	 * @param tab   the tab index
	 * @param title the title
	 */
	public void setTabTitle(int tab, String title) {
		tabbedPane.setTitleAt(tab, title);
	}

	protected static final int SPLIT_MAIN    = 0;
	protected static final int SPLIT_RIGHT  = 1;
	protected static final int SPLIT_LEFT   = 2;
	protected static final int SPLIT_BOTTOM = 3;

	// position orders for TViewChoosers
	protected final static int[] DEFAULT_ORDER = new int[] { 0, 1, 2, 3 };
	protected final static int[] PORTRAIT_VIEW_ORDER = new int[] { 3, 2, 1, 0 };
	protected final static int[] PORTRAIT_DIVIDER_ORDER = new int[] { 2, 3, 0, 1 };
	
	public static final int ADD_NOREFRESH = 0;
	public static final int ADD_NOSELECT = 0;
	public static final int ADD_SELECT = 1;
	public static final int ADD_REFRESH = 2;
	
	/**
	 * Places the views in an appropriate order for the specified trackerPanel.
	 *
	 * @param trackerPanel the trackerPanel
	 * @param viewChoosers an array of up to 4 TViewChoosers
	 */
	public void placeViews(TrackerPanel trackerPanel, TViewChooser[] viewChoosers) {
		if (viewChoosers == null)
			viewChoosers = new TViewChooser[0];
		int[] order = isPortraitLayout() ? PORTRAIT_VIEW_ORDER : DEFAULT_ORDER;
		TTabPanel tabPanel = getTabPanel(trackerPanel);
		Object[] objects = tabPanel.getObjects();
		TViewChooser[] choosers = (TViewChooser[]) objects[TFRAME_VIEWCHOOSERS];
		for (int i = 0; i < Math.min(viewChoosers.length, choosers.length); i++) {
			if (viewChoosers[i] != null)
				choosers[i] = viewChoosers[i];
		}
		if (order == null || order.length != viewChoosers.length) {
			order = DEFAULT_ORDER;
		}
			
		MainTView mainView = (MainTView) objects[TFRAME_MAINVIEW];
		JSplitPane[] panes = (JSplitPane[]) objects[TFRAME_SPLITPANES];
		
		if (((BorderLayout)tabPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER) != panes[SPLIT_MAIN]) {
			tabPanel.removeAll();
			tabPanel.add(panes[SPLIT_MAIN], BorderLayout.CENTER);
		}
		addPaneSafely(panes[SPLIT_MAIN], SPLIT_LEFT, panes[SPLIT_LEFT]);
		addPaneSafely(panes[SPLIT_MAIN], SPLIT_RIGHT, panes[SPLIT_RIGHT]);
		addPaneSafely(panes[SPLIT_LEFT], SPLIT_MAIN, mainView);
		addPaneSafely(panes[SPLIT_LEFT], SPLIT_BOTTOM, panes[SPLIT_BOTTOM]);
		addPaneSafely(panes[SPLIT_RIGHT], SPLIT_MAIN, choosers[order[TView.VIEW_PLOT]]);
		addPaneSafely(panes[SPLIT_RIGHT], SPLIT_BOTTOM, choosers[order[TView.VIEW_TABLE]]);
		addPaneSafely(panes[SPLIT_BOTTOM], SPLIT_RIGHT, choosers[order[TView.VIEW_WORLD]]);
		addPaneSafely(panes[SPLIT_BOTTOM], SPLIT_LEFT, choosers[order[TView.VIEW_PAGE]]);
		// add toolbars at north position
		tabPanel.setToolbarVisible(true);
	}

	public TTabPanel getTabPanel(TrackerPanel trackerPanel) {
		int tab = getTab(trackerPanel);
		return (tab >= 0 ? (TTabPanel) tabbedPane.getComponentAt(tab) : null);
	}


	private void addPaneSafely(JSplitPane pane, int where, Component c) {
		switch (where) {
		case SPLIT_MAIN:
			if (pane.getTopComponent() != c)
				pane.setTopComponent(c);
			break;
		case SPLIT_LEFT:
			if (pane.getLeftComponent() != c)
				pane.setLeftComponent(c);
			break;
		case SPLIT_RIGHT:
			if (pane.getRightComponent() != c)
				pane.setRightComponent(c);
			break;
		case SPLIT_BOTTOM:
			if (pane.getBottomComponent() != c)
				pane.setBottomComponent(c);
			break;
			
		}
	}


	/**
	 * Gets the TViewChoosers for the specified tracker panel.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return array of TViewChooser
	 */
	public TViewChooser[] getViewChoosers(TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		return (objects == null ? 
				new TViewChooser[4] : (TViewChooser[]) objects[TFRAME_VIEWCHOOSERS]);
	}
	
	/**
	 * Arranges the views for a tracker panel, showing default views under or beside
	 * the video and the opposite for non-default views.
	 * 
	 * @param trackerPanel the tracker panel
	 * @param showDefaultViews   true to show default views
	 * @param showOtherViews      true to show non-default views
	 */
	public void arrangeViews(TrackerPanel trackerPanel, boolean showDefaultViews, boolean showOtherViews) {
		if (!isLayoutAdaptive)
			return;
		// place views in the right locations
		placeViews(trackerPanel, getViewChoosers(trackerPanel));
		// set divider properties according to visibility specified
		boolean showRight = (!isPortraitLayout && showDefaultViews) || (isPortraitLayout && showOtherViews);
		setDividerLocation(trackerPanel, SPLIT_MAIN, showRight ? DEFAULT_MAIN_DIVIDER : 1.0);
		setDividerLocation(trackerPanel, SPLIT_RIGHT, DEFAULT_RIGHT_DIVIDER);
		boolean showBottom = (!isPortraitLayout && showOtherViews) || (isPortraitLayout && showDefaultViews);
		setDividerLocation(trackerPanel, SPLIT_LEFT, showBottom ? DEFAULT_LEFT_DIVIDER : 1.0);
		// bottom divider--delay needed in Java for correct placement
		SwingUtilities.invokeLater(() -> {
			setDividerLocation(trackerPanel, SPLIT_BOTTOM, DEFAULT_BOTTOM_DIVIDER);
		});
	}

	/**
	 * Gets the views for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return an array of views
	 */
	public TView[][] getTViews(TrackerPanel trackerPanel) {
		return getTViews(trackerPanel, false);
	}

	/**
	 * Gets the TViews for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @param customOnly   true to return only customized views
	 * @return TView[4][4], may be null
	 */
	public TView[][] getTViews(TrackerPanel trackerPanel, boolean customOnly) {
		TViewChooser[] choosers = getViewChoosers(trackerPanel);
		TView[][] array = new TView[choosers.length][];
		if (!customOnly) {
			for (int i = 0; i < choosers.length; i++) {
				array[i] = (choosers[i] == null ? null : choosers[i].getTViews());
			}
			return array;
		}
		// customized views only
		for (int i = 0; i < choosers.length; i++) {
			TView[] views = choosers[i].getTViews();
			if (views != null)
				for (int j = 0; j < views.length; j++) {
					TView next = views[j];
					if (next != null && next.isCustomState()) {
						if (array[i] == null)
							array[i] = new TView[4];
						array[i][j] = next;
					}
				}
		}
		return array;
	}

	/**
	 * Gets the selected TViewTypes for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return int[4] of types selected in the TViewChoosers
	 */
	public int[] getSelectedViewTypes(TrackerPanel trackerPanel) {
		TViewChooser[] choosers = getViewChoosers(trackerPanel);
		int[] selectedViews = new int[4];
		for (int i = 0; i < selectedViews.length; i++) {
			selectedViews[i] = choosers[i].getSelectedViewType();
		}
		return selectedViews;
	}

	/**
	 * Gets the selected TrackView names for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return String[4][2] of track names selected in {plot, table}
	 */
	public String getSelectedTrackViews(TrackerPanel trackerPanel) {
		StringBuffer buf = new StringBuffer();
		TViewChooser[] choosers = getViewChoosers(trackerPanel);
		for (int i = 0; i < choosers.length; i++) {
			if (i > 0)
				buf.append(";");
			
			TView[] views = choosers[i].getTViews();
			if (views[TView.VIEW_PLOT] != null) {
				PlotTView view = (PlotTView)views[TView.VIEW_PLOT];
				TTrack track = view.getSelectedTrack();
				buf.append(track == null? "null,": track.getName() + ",");
			} else
				buf.append("null,");
			
			if (views[TView.VIEW_TABLE] != null) {
				TableTView view = (TableTView)views[TView.VIEW_TABLE];
				TTrack track = view.getSelectedTrack();
				buf.append(track == null? "null": track.getName());
			} else
				buf.append("null");				
		}
		return buf.toString();
	}

	/**
	 * Determines whether a view pane is visible for the specified trackerPanel tab.
	 *
	 * @param index the view position index 
	 * @param trackerPanel the trackerPanel
	 * @return true if it is visible
	 */
	public boolean isViewPaneVisible(int index, TrackerPanel trackerPanel) {
		JSplitPane[] panes = getSplitPanes(trackerPanel);
		double[] locs = new double[panes.length];
		for (int i = 0; i < panes.length; i++) {
			int max = panes[i].getMaximumDividerLocation();
			locs[i] = 1.0 * panes[i].getDividerLocation() / max;
		}
		switch (index) {
		case 0:
			return locs[0] < 0.95 && locs[1] > 0.05;
		case 1:
			return locs[0] < 0.95 && locs[1] < 0.95;
		case 2:
			return locs[2] < 0.92 && locs[3] < 0.95; // BH was 0.95, but on my machine this is 0.926
		case 3:
			return locs[2] < 0.95 && locs[3] > 0.05;
		}
		return false;
	}

	/**
	 * Determines whether the specified views are visible in a trackerPanel tab.
	 * Views may be DEFAULT_VIEWS (TViewChoosers[0/1]) or OTHER_VIEWS (TViewChoosers[2/3])
	 * 
	 * @param whichViews DEFAULT_VIEWS or OTHER_VIEWS
	 * @param trackerPanel the trackerPanel
	 * @return true if views are visible
	 */
	public boolean areViewsVisible(int whichViews, TrackerPanel trackerPanel) {
		boolean standardLayout = getSplitPane(trackerPanel, 1).getTopComponent() == getViewChoosers(trackerPanel)[0];
		int splitPaneIndex = whichViews == DEFAULT_VIEWS && standardLayout? 
				0: whichViews == OTHER_VIEWS && !standardLayout? 
				0:
				2;
		JSplitPane pane = getSplitPane(trackerPanel, splitPaneIndex);
		int max = pane.getMaximumDividerLocation();
		int cur = pane.getDividerLocation();
		double loc = 1.0 * cur / max;
		return splitPaneIndex == 0? loc < 0.95: loc < 0.92;
	}

	/**
	 * Sets the location of a splitpane divider for a tracker panel
	 *
	 * @param trackerPanel the tracker panel
	 * @param paneIndex    the index of the split pane
	 * @param loc          the desired fractional divider location
	 */
	public void setDividerLocation(TrackerPanel trackerPanel, int paneIndex, double loc) {
		JSplitPane[] panes = getSplitPanes(trackerPanel);
		if (paneIndex < panes.length) {
			panes[paneIndex].setDividerLocation(loc);			
		}
	}

	/**
	 * Sets the location of a splitpane divider for a tracker panel
	 *
	 * @param trackerPanel the tracker panel
	 * @param paneIndex    the index of the split pane
	 * @param loc          the desired absolute divider location
	 */
	public void setDividerLocation(TrackerPanel trackerPanel, int paneIndex, int loc) {
		JSplitPane[] panes = getSplitPanes(trackerPanel);
		if (paneIndex < panes.length) {
			panes[paneIndex].setDividerLocation(loc);
		}
	}

	/**
	 * Gets a splitpane for a tracker panel
	 *
	 * @param trackerPanel the tracker panel
	 * @param paneIndex    the index of the split pane
	 * @return the splitpane
	 */
	JSplitPane getSplitPane(TrackerPanel trackerPanel, int paneIndex) {
		JSplitPane[] panes = getSplitPanes(trackerPanel);
		return (paneIndex < panes.length ? panes[paneIndex] : null);
	}

	/**
	 * Gets the main view for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a MainTView
	 */
	public MainTView getMainView(TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		MainTView mainview = objects == null ? 
				new MainTView(trackerPanel) : 
				(MainTView) objects[TFRAME_MAINVIEW];
		return mainview;
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		TrackerPanel trackerPanel;
		String name = e.getPropertyName();
		switch (name) {
		case VideoPanel.PROPERTY_VIDEOPANEL_DATAFILE:
		case TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO: // from TrackerPanel //$NON-NLS-1$
			trackerPanel = (TrackerPanel) e.getSource();
			refreshTab(trackerPanel);
			break;
		case MovieVideoI.PROPERTY_VIDEO_PROGRESS: // from currently loading (xuggle) video
			Object val = e.getNewValue(); // may be null
			String vidName = XML.forwardSlash((String) e.getOldValue());
			if (val != null)
				try {
					framesLoaded = Integer.parseInt(val.toString());
					TrackerIO.setProgress(vidName, val.toString(), framesLoaded);
				} catch (Exception ex) {
				}
			break;
		case MovieVideoI.PROPERTY_VIDEO_STALLED: // from stalled xuggle video
			String fileName = XML.getName((String) e.getNewValue());
			String s = TrackerRes.getString("TFrame.Dialog.StalledVideo.Message0") //$NON-NLS-1$
					+ "\n" + TrackerRes.getString("TFrame.Dialog.StalledVideo.Message1") //$NON-NLS-1$ //$NON-NLS-2$
					+ "\n" + TrackerRes.getString("TFrame.Dialog.StalledVideo.Message2") //$NON-NLS-1$ //$NON-NLS-2$
					+ "\n\n" + TrackerRes.getString("TFrame.Dialog.StalledVideo.Message3"); //$NON-NLS-1$ //$NON-NLS-2$
			String stop = TrackerRes.getString("TFrame.Dialog.StalledVideo.Button.Stop"); //$NON-NLS-1$
			String wait = TrackerRes.getString("TFrame.Dialog.StalledVideo.Button.Wait"); //$NON-NLS-1$
			int response = JOptionPane.showOptionDialog(TFrame.this, s,
					TrackerRes.getString("TFrame.Dialog.StalledVideo.Title") + ": " + fileName, //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] { stop, wait }, stop);
			if (response == 0) { // user chose to stop loading
				VideoIO.setCanceled(true);
				TrackerIO.closeMonitor(fileName);
			}
			break;
		case TrackerRes.PROPERTY_TRACKERRES_LOCALE: // from TrackerRes //$NON-NLS-1$
			// clear the existing menubars and actions
			TMenuBar.clear();
			TActions.clear();
			// create new actions
			Tracker.createActions();
			// create new default menubar
			createDefaultMenuBar();
			// replace and refresh the stored menubars and toolbars
			for (int i = 0; i < getTabCount(); i++) {
				Object[] objects = getObjects(i);
				MainTView mainView = (MainTView) objects[TFRAME_MAINVIEW];
				trackerPanel = mainView.getTrackerPanel();
				boolean changed = trackerPanel.changed; // save changed state and restore below
				// replace the stored menubar
				objects[TFRAME_MENUBAR] = TMenuBar.getMenuBar(trackerPanel);
				CoordAxes axes = trackerPanel.getAxes();
				if (axes != null) {
					axes.setName(TrackerRes.getString("CoordAxes.New.Name")); //$NON-NLS-1$
				}
				trackerPanel.changed = changed;
				TToolBar toolbar = (TToolBar) objects[TFRAME_TOOLBAR];
				toolbar.refresh(TToolBar.REFRESH_TFRAME_LOCALE);
				TTrackBar trackbar = (TTrackBar) objects[TFRAME_TRACKBAR];
				trackbar.refresh();
			}
			trackerPanel = getSelectedPanel();
			if (trackerPanel != null) {
				// replace current menubar
				TMenuBar menuBar = getMenuBar(trackerPanel);
				if (menuBar != null) {
					setJMenuBar(menuBar);
					
					menuBar.refresh(TMenuBar.REFRESH_TFRAME_LOCALE);
				}
				// show hint
				if (Tracker.startupHintShown) {
					trackerPanel.setMessage(TrackerRes.getString("Tracker.Startup.Hint")); //$NON-NLS-1$
				} else {
					// shows hint as side effect
					trackerPanel.setCursorForMarking(false, null);
				}
			} else {
				// show defaultMenuBar
				setJMenuBar(defaultMenuBar);
			}
			// refresh tabs
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				trackerPanel = getTrackerPanel(i);
				tabbedPane.setTitleAt(i, trackerPanel.getTitle());
				VideoPlayer player = trackerPanel.getPlayer();
				player.refresh();
				player.setLocale((Locale) e.getNewValue());
				Video vid = trackerPanel.getVideo();
				if (vid != null) {
					vid.getFilterStack().refresh();
				}
				// refresh track controls and toolbars
				TrackControl.getControl(trackerPanel).refresh();
				getToolBar(trackerPanel).refresh(TToolBar.REFRESH_TFRAME_LOCALE2);
				getTrackBar(trackerPanel).refresh();
				// refresh view panes
				TViewChooser[] choosers = getViewChoosers(trackerPanel);
				for (int j = 0; j < choosers.length; j++) {
					choosers[j].refresh();
				}
				// refresh autotracker
				if (trackerPanel.autoTracker != null) {
					trackerPanel.autoTracker.getWizard().textPaneSize = null;
					trackerPanel.autoTracker.getWizard().refreshGUI();
					trackerPanel.autoTracker.getWizard().pack();
				}
				// refresh prefs dialog
				if (prefsDialog != null && prefsDialog.isVisible()) {
					prefsDialog.refreshGUI();
				}
				// refresh pencil drawer
				PencilDrawer.getDrawer(trackerPanel).refresh();
				// refresh info dialog
				cancelNotesDialogButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
				closeNotesDialogButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
				displayWhenLoadedCheckbox.setText(TrackerRes.getString("TFrame.NotesDialog.Checkbox.ShowByDefault")); //$NON-NLS-1$
			}
			validate();
			if (helpLauncher != null) {
				// refresh navigation bar components
				Component[] search = HelpFinder.getNavComponentsFor(helpLauncher);
				Component[] comps = new Component[search.length + 2];
				System.arraycopy(search, 0, comps, 0, search.length);
				Tracker.pdfHelpButton.setText(TrackerRes.getString("Tracker.Button.PDFHelp")); //$NON-NLS-1$
				comps[comps.length - 2] = Tracker.pdfHelpButton;
				comps[comps.length - 1] = Box.createHorizontalStrut(4);
				helpLauncher.setNavbarRightEndComponents(comps);
			}
			break;
		default:
//			if (e.getSource() instanceof JSplitPane) {
//				JSplitPane p = (JSplitPane) e.getSource();
//				if (p.getName() == "LEFT(2)") {
//				OSPLog.debug(p.getName() + " " 
//				+ e.getPropertyName() + " " 
//						+ e.getOldValue() + " "
//				+ e.getNewValue() + " max=" + p.getMaximumDividerLocation()
//				+ " " + (1.0 * p.getDividerLocation() / p.getMaximumDividerLocation()));
//				}
//			}
			break;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!Tracker.splash.isVisible())
			return;
//    Tracker.setProgress(100);
		// dispose of splash automatically after short time
		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Tracker.splash.dispose();
			}
		});
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Sets the display units for angles.
	 * 
	 * @param inRadians true to display radians, false to display degrees
	 */
	public void setAnglesInRadians(boolean inRadians) {
		if (anglesInRadians == inRadians)
			return;
		anglesInRadians = inRadians;
		firePropertyChange(PROPERTY_TFRAME_RADIANANGLES, null, inRadians); // $NON-NLS-1$
	}

	/**
	 * Gets the preferences dialog.
	 * 
	 * @return the preferences dialog
	 */
	public PrefsDialog getPrefsDialog() {
		TrackerPanel trackerPanel = getSelectedPanel();
		if (prefsDialog != null) {
			if (prefsDialog.trackerPanel != trackerPanel) {
				prefsDialog.trackerPanel = trackerPanel;
				prefsDialog.refreshGUI();
			}
		} else {
			// create PrefsDialog
			prefsDialog = new PrefsDialog(trackerPanel, this);
			// center on screen
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (dim.width - prefsDialog.getBounds().width) / 2;
			int y = (dim.height - prefsDialog.getBounds().height) / 2;
			prefsDialog.setLocation(x, y);
		}
		return prefsDialog;
	}

	/**
	 * Shows the preferences dialog.
	 */
	public void showPrefsDialog() {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				PrefsDialog prefsDialog = getPrefsDialog();
				prefsDialog.setVisible(true);
				prefsDialog.requestFocus();
			}
		};
		new Thread(runner).start();
	}

	/**
	 * Shows the preferences dialog set to a specified tab.
	 * 
	 * @param tabName the name of the tab: config, runtime, video, general, display
	 */
	public void showPrefsDialog(final String tabName) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				// show prefs dialog and select video tab
				PrefsDialog prefsDialog = getPrefsDialog();
				if (tabName != null) {
					if (tabName.contains("runtime")) //$NON-NLS-1$
						prefsDialog.tabbedPane.setSelectedComponent(prefsDialog.runtimePanel);
					else if (tabName.contains("video")) //$NON-NLS-1$
						prefsDialog.tabbedPane.setSelectedComponent(prefsDialog.videoPanel);
					else if (tabName.contains("general")) //$NON-NLS-1$
						prefsDialog.tabbedPane.setSelectedComponent(prefsDialog.generalPanel);
					else if (tabName.contains("display")) //$NON-NLS-1$
						prefsDialog.tabbedPane.setSelectedComponent(prefsDialog.displayPanel);
				}
				prefsDialog.setVisible(true);
				prefsDialog.requestFocus();
			}
		};
		new Thread(runner).start();
	}

//_________________________________ private methods _________________________

	/**
	 * Creates the default views of the specified tracker panel.
	 * 
	 * @param mainView
	 *
	 * @param trackerPanel the tracker panel
	 * @return a TViewChooser[numberOfViews] array.
	 */
	private static TViewChooser[] createTViews(TrackerPanel trackerPanel) {
		if (!Tracker.allowViews) {
			//OSPLog.debug("TFrame allowViews is false");
			return new TViewChooser[4];
		}
		return new TViewChooser[] { 
				new TViewChooser(trackerPanel, TView.VIEW_PLOT),
				new TViewChooser(trackerPanel, TView.VIEW_TABLE), 
				new TViewChooser(trackerPanel, TView.VIEW_WORLD),
				new TViewChooser(trackerPanel, TView.VIEW_PAGE) 
		};
	}

	/**
	 * Gets the split panes for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return an array of split panes
	 */
	JSplitPane[] getSplitPanes(final TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		if (objects != null) {
			return (JSplitPane[]) objects[TFRAME_SPLITPANES];
		}
		JSplitPane[] panes = new JSplitPane[4];
		panes[SPLIT_MAIN] = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); // left/right split 
		panes[SPLIT_RIGHT] = new JSplitPane(JSplitPane.VERTICAL_SPLIT);  // plot/table split
		panes[SPLIT_LEFT] = new JSplitPane(JSplitPane.VERTICAL_SPLIT); // video/bottom split
		panes[SPLIT_BOTTOM] = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) // page/world split
		{
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(0,0);
			}
		}; 
		panes[SPLIT_MAIN].setName("TOP(0)");
		panes[SPLIT_RIGHT].setName("RIGHT(1)");
		panes[SPLIT_LEFT].setName("LEFT(2)");
		panes[SPLIT_BOTTOM].setName("BOTTOM(3)");
		setDefaultWeights(panes);
		return panes;
	}

	private static void setDefaultWeights(JSplitPane[] panes) {
		setDefaultWeight(panes[SPLIT_MAIN], 1.0);
		setDefaultWeight(panes[SPLIT_RIGHT], 0.5); // right shared, trackerPanel expands
		setDefaultWeight(panes[SPLIT_LEFT], 1.0);
		setDefaultWeight(panes[SPLIT_BOTTOM], 0.5); // bottom shared, trackerPanel expands
	}

	private static void setDefaultWeight(JSplitPane pane, double d) {
		pane.setDividerSize(DEFAULT_DIVIDER_SIZE);
		pane.setResizeWeight(d); 
		pane.setOneTouchExpandable(true);
	}


	void maximizeView(TrackerPanel trackerPanel, int viewIndex) {
		maximizedView = viewIndex;
		JSplitPane[] panes = getSplitPanes(trackerPanel);
		for (int i = 0; i < panes.length; i++) {
			panes[i].setDividerSize(0);
		}
		int[] order = (isPortraitLayout() ? PORTRAIT_VIEW_ORDER : DEFAULT_ORDER);
		int viewPosition = viewIndex < order.length? order[viewIndex]: viewIndex;
		switch (viewPosition) {
		case TView.VIEW_PLOT: // right upper
			panes[SPLIT_RIGHT].setResizeWeight(1); 
			setDividerLocation(trackerPanel, SPLIT_MAIN, 0.0);
			setDividerLocation(trackerPanel, SPLIT_RIGHT, 1.0);
			break;
		case TView.VIEW_TABLE: // right lower
			panes[SPLIT_RIGHT].setResizeWeight(0); 
			setDividerLocation(trackerPanel, SPLIT_MAIN, 0.0);
			setDividerLocation(trackerPanel, SPLIT_RIGHT, 0.0);
			break;
		case TView.VIEW_WORLD: // bottom right
			panes[SPLIT_BOTTOM].setResizeWeight(0); 
			setDividerLocation(trackerPanel, SPLIT_MAIN, 1.0);
			setDividerLocation(trackerPanel, SPLIT_LEFT, 0.0);
			setDividerLocation(trackerPanel, SPLIT_BOTTOM, 0.0);
			break;
		case TView.VIEW_PAGE: // bottom left
			panes[SPLIT_BOTTOM].setResizeWeight(1); 
			setDividerLocation(trackerPanel, SPLIT_MAIN, 1.0);
			int max = panes[0].getMaximumDividerLocation();
			setDividerLocation(trackerPanel, SPLIT_LEFT, 0.0);
			setDividerLocation(trackerPanel, SPLIT_BOTTOM, max);
			break;
		case TView.VIEW_MAIN: // main video view
			setDividerLocation(trackerPanel, SPLIT_MAIN, 1.0);
			setDividerLocation(trackerPanel, SPLIT_LEFT, 1.0);
			setDividerLocation(trackerPanel, SPLIT_BOTTOM, 0.0);			
		}
		TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
		menubar.setMenuTainted(TMenuBar.MENU_WINDOW, true);
//		int tab = getTab(trackerPanel);
//		if (tab == -1) return;
//		TTabPanel tabPanel = (TTabPanel) tabbedPane.getComponentAt(tab);
//		tabPanel.setToolbarVisible(false);	
//		tabPanel.revalidate();
	}
	
	void saveCurrentDividerLocations(TrackerPanel trackerPanel) {
		if (maximizedView > -1)
			return;
		if (trackerPanel.dividerLocs == null)
			trackerPanel.dividerLocs =  new double[4];
		for (int i = 0; i < trackerPanel.dividerFractions.length; i++) {
			JSplitPane pane = getSplitPane(trackerPanel, i);
			int max = pane.getMaximumDividerLocation();
			int cur = Math.min(pane.getDividerLocation(), max); // sometimes cur > max !!??
			trackerPanel.dividerLocs[i] = cur;
			double fraction = 1.0 * cur / max;
			fraction = fraction < MIN_DIVIDER_OFFSET && (i == 1 || i == 3)? 0: fraction;
			fraction = fraction > 1-MIN_DIVIDER_OFFSET? 1: fraction;
			trackerPanel.dividerFractions[i] = fraction;
		}
	}

	void restoreViews(TrackerPanel trackerPanel) {
//		if (maximizedView < 0)
//			return;
		for (int i = 0; i < trackerPanel.dividerFractions.length; i++) {
			if (trackerPanel.dividerLocs == null)
				setDividerLocation(trackerPanel, i, trackerPanel.dividerFractions[i]);
			else
				setDividerLocation(trackerPanel, i, (int)trackerPanel.dividerLocs[i]);
		}
		setDefaultWeights(getSplitPanes(trackerPanel));
		maximizedView = -1;
		TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
		menubar.setMenuTainted(TMenuBar.MENU_WINDOW, true);
		if (isLayoutChanged) {
			frameResized();
		}

//		int tab = getTab(trackerPanel);
//		if (tab == -1) return;
//		TTabPanel tabPanel = (TTabPanel) tabbedPane.getComponentAt(tab);
//		tabPanel.setToolbarVisible(true);	
	}
	
	/**
	 * Gets the toolbar for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a TToolBar
	 */
	public TToolBar getToolBar(TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		if (objects != null) {
			return (TToolBar) objects[TFRAME_TOOLBAR];
		}
		return TToolBar.getToolbar(trackerPanel);
	}

	/**
	 * Gets the menubar for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a TMenuBar
	 */
	public TMenuBar getMenuBar(TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		if (objects != null && objects[TFRAME_MENUBAR] != null) {
			return (TMenuBar) objects[TFRAME_MENUBAR];
		}
		return TMenuBar.getMenuBar(trackerPanel);
	}

	/**
	 * Sets the menubar for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @param menubar      a TMenuBar
	 */
	public void setMenuBar(TrackerPanel trackerPanel, TMenuBar menubar) {
		Object[] objects = getObjects(trackerPanel);
		if (objects != null && objects.length > 4) {
			objects[TFRAME_MENUBAR] = menubar;
			setJMenuBar(menubar);
		}
	}

	/**
	 * Gets the selected track bar for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return a TSelectedTrackBar
	 */
	public TTrackBar getTrackBar(TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		if (objects != null) {
			return (TTrackBar) objects[TFRAME_TRACKBAR];
		}
		return TTrackBar.getTrackbar(trackerPanel);
	}

	/**
	 * Refreshes the open recent files menu.
	 *
	 * @param menu the menu to refresh
	 */
	public void refreshOpenRecentMenu(final JMenu menu) {
		if (!OSPRuntime.isJS) /** @j2sNative */
		{
			synchronized (Tracker.recentFiles) {
				menu.setText(TrackerRes.getString("TMenuBar.Menu.OpenRecent")); //$NON-NLS-1$
				menu.setEnabled(!Tracker.recentFiles.isEmpty());
				if (openRecentAction == null) {
					openRecentAction = new AbstractAction() {
						@Override
						public void actionPerformed(ActionEvent e) {
							doRecentFiles(e.getActionCommand());
						}
					};
				}
				menu.removeAll();
				menu.setEnabled(!Tracker.recentFiles.isEmpty());
				for (String next : Tracker.recentFiles) {
					JMenuItem item = new JMenuItem(XML.getName(next));
					FontSizer.setFont(item);
					item.setActionCommand(next);
					item.setToolTipText(next);
					item.addActionListener(openRecentAction);
					menu.add(item);
				}
			}
		}
	}

	protected void doRecentFiles(String path) {
		URL url = null;
		if (!ResourceLoader.isHTTP(path)) {
			File file = new File(path);
			if (!file.exists()) {
				int n = path.indexOf("!"); //$NON-NLS-1$
				if (n >= 0 && !(file = new File(path.substring(0, n))).exists()) {
					try {
						url = new URL(path);
					} catch (MalformedURLException e1) {
					}
				}
			}
			if (!file.exists() && url == null) {
				Tracker.recentFiles.remove(path);
				TrackerPanel panel = getSelectedPanel();
				if (panel != null) {
					TMenuBar.refreshMenus(panel, TMenuBar.REFRESH_TFRAME_OPENRECENT);
				}
				JOptionPane.showMessageDialog(this, TrackerRes.getString("TFrame.Dialog.FileNotFound.Message") //$NON-NLS-1$
						+ "\n" + MediaRes.getString("VideoIO.Dialog.Label.Path") + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						+ path, TrackerRes.getString("TFrame.Dialog.FileNotFound.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		doOpenURL(path);
	}

	/**
	 * Refreshes the GUI.
	 */
	public void refresh() {
		TrackerPanel trackerPanel = getSelectedPanel();
		if (trackerPanel == null)
			return;
		getMenuBar(trackerPanel).refresh(TMenuBar.REFRESH_TFRAME_REFRESH);
		getToolBar(trackerPanel).refresh(TToolBar.REFRESH_TFRAME_REFRESH_TRUE);
		getTrackBar(trackerPanel).refresh();
		for (Container next : getViewChoosers(trackerPanel)) {
			if (next instanceof TViewChooser) {
				TViewChooser chooser = (TViewChooser) next;
				chooser.refreshMenus();
			}
		}
		trackerPanel.refreshNotesDialog();
//    checkMemory();
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	@Override
	public void setFontLevel(int level) {
		if (!OSPRuntime.allowSetFonts)
			return;
		try {
			super.setFontLevel(level);
			if (libraryBrowser != null) {
				libraryBrowser.setFontLevel(level);
			}

		} catch (Exception e) {
		}
		if (tabbedPane == null)
			return;
//		FontSizer.setFonts(tabbedPane, level);

		textLayoutFont = FontSizer.getResizedFont(textLayoutFont, level);

		for (int i = 0; i < getTabCount(); i++) {
			TrackerPanel trackerPanel = getTrackerPanel(i);
			trackerPanel.setFontLevel(level);
		}

		if (haveExportDialog) {
			// lazy initialization
			ExportZipDialog.setFontLevels(level);
			if (ExportVideoDialog.videoExporter != null) {
				ExportVideoDialog.videoExporter.setFontLevel(level);
			}
		}
		if (haveThumbnailDialog) {
			// lazy initialization
			if (ThumbnailDialog.thumbnailDialog != null) {
				FontSizer.setFonts(ThumbnailDialog.thumbnailDialog, level);
				ThumbnailDialog.thumbnailDialog.refreshGUI();
			}
		}
		if (prefsDialog != null) {
			prefsDialog.refreshGUI();
		}
		if (libraryBrowser != null) {
			libraryBrowser.setFontLevel(level);
		}

		FontSizer.setFonts(notesDialog, level);
		FontSizer.setFonts(OSPLog.getOSPLog(), level);
		if (Tracker.readmeDialog != null) {
			FontSizer.setFonts(Tracker.readmeDialog, level);
		}
		if (Tracker.startLogDialog != null) {
			FontSizer.setFonts(Tracker.startLogDialog, level);
		}
		FontSizer.setFonts(defaultMenuBar, level);
		if (helpLauncher != null) {
			helpLauncher.setFontLevel(level);
			for (int i = 0; i < helpLauncher.getTabCount(); i++) {
				LaunchPanel tab = helpLauncher.getTab(i);
				if (level > 0) {
					String newValue = "help" + level + ".css"; //$NON-NLS-1$ //$NON-NLS-2$
					tab.getHTMLSubstitutionMap().put("help.css", newValue); //$NON-NLS-1$
				} else {
					tab.getHTMLSubstitutionMap().remove("help.css"); //$NON-NLS-1$
				}
			}
			for (int i = 0; i < helpLauncher.getHTMLTabCount(); i++) {
				HTMLPane pane = helpLauncher.getHTMLTab(i);
				pane.editorPane.getDocument().putProperty(Document.StreamDescriptionProperty, null);
			}
			helpLauncher.setDivider((int) (175 * FontSizer.getFactor(level)));
			helpLauncher.refreshSelectedTab();
		}
	}

	/**
	 * Gets the library browser.
	 *
	 * @return the library browser
	 */
	protected LibraryBrowser getLibraryBrowser() {
		if (libraryBrowser == null) {
			try {
				LibraryComPADRE.desiredOSPType = "Tracker"; //$NON-NLS-1$
				
//    	JDialog dialog = new JDialog(this, false);
//    	libraryBrowser = LibraryBrowser.getBrowser(dialog);
				libraryBrowser = LibraryBrowser.getBrowser(null);

				libraryBrowser.addOSPLibrary(LibraryBrowser.TRACKER_LIBRARY);
				libraryBrowser.addOSPLibrary(LibraryBrowser.SHARED_LIBRARY);
				libraryBrowser
						.addComPADRECollection(LibraryComPADRE.TRACKER_SERVER_TREE + LibraryComPADRE.PRIMARY_ONLY);
				libraryBrowser.refreshCollectionsMenu();
				libraryBrowser.addPropertyChangeListener(LibraryBrowser.PROPERTY_LIBRARY_TARGET,
						new PropertyChangeListener() { // $NON-NLS-1$
							@Override
								public void propertyChange(PropertyChangeEvent e) {
								// if HINT_LOAD_RESOURCE, then e.getNewValue() is LibraryResource to load
								if (LibraryBrowser.HINT_LOAD_RESOURCE == e.getOldValue()) {
									
									LibraryResource record = (LibraryResource)e.getNewValue();
									String toCancel = " ["+TrackerRes.getString("TFrame.LibraryBrowser.Message.Cancel")+"]";
									String loading = " "+TrackerRes.getString("Tracker.Splash.Loading")+" \"";
									String message = loading + record.getName() + "\"" + toCancel;
									libraryBrowser.setMessage(message, Color.YELLOW);
									libraryBrowser.setComandButtonEnabled(false);
									VideoIO.setCanceled(false);

									// invoke later so libraryBrowser message can refresh
									SwingUtilities.invokeLater(() -> {
										loadLibraryRecord(record);
									});

								}
								// if HINT_DOWNLOAD_RESOURCE, then e.getNewValue() is downloaded File
								else if (LibraryBrowser.HINT_DOWNLOAD_RESOURCE == e.getOldValue()) {
									File file = (File)e.getNewValue();
									if (file != null && file.exists()) {
//										TrackerIO.open(file.getPath(), TFrame.this);
									}
								}
							}
						});
				LibraryBrowser.fireHelpEvent = true;
				libraryBrowser.addPropertyChangeListener("help", new PropertyChangeListener() { //$NON-NLS-1$
					@Override
					public void propertyChange(PropertyChangeEvent e) {
						showHelp("library", 0); //$NON-NLS-1$
					}
				});
//				libraryBrowser.setFontLevel(FontSizer.getLevel());
//      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//      int x = (dim.width - dialog.getBounds().width) / 2;
//      int y = (dim.height - dialog.getBounds().height) / 2;
//      dialog.setLocation(x, y);
//				libraryBrowser.setVisible(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return libraryBrowser;
	}

	/**
	 * Gets the properties dialog for a specified TrackerPanel.
	 *
	 * @param trackerPanel
	 * @return the properties dialog
	 */
	protected PropertiesDialog getPropertiesDialog(TrackerPanel trackerPanel) {
		// return a new dialog every time
		PropertiesDialog dialog = new PropertiesDialog(trackerPanel);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - dialog.getBounds().width) / 2;
		int y = (dim.height - dialog.getBounds().height) / 2;
		dialog.setLocation(x, y);
		return dialog;
	}

	/**
	 * Gets the help dialog.
	 *
	 * @return the help dialog
	 */
	protected Component getHelpDialog() {
		if (helpDialog == null) {
			helpDialog = new JDialog(this, TrackerRes.getString("TFrame.Dialog.Help.Title"), false); //$NON-NLS-1$
			String help_path = HELP_PATH + "help_set.xml"; //$NON-NLS-1$
//      String lang = TrackerRes.locale.getLanguage();
//      String webHelp = helpPathWeb+"help_"+lang+"/help_set.xml"; //$NON-NLS-1$ //$NON-NLS-2$
//      Resource res = ResourceLoader.getResource(webHelp);
//      if (res!=null) { 
//      	help_path = res.getString(); // open launcher with xml string
//      }
//      System.out.println(help_path);
			helpLauncher = new Launcher(help_path, false, (JPanel) helpDialog.getContentPane());
			helpLauncher.popupEnabled = false;
			int level = FontSizer.getLevel();
			if (helpLauncher.getTabCount() > 0) {
				LaunchPanel tab = helpLauncher.getTab(0);
				if (level > 0) {
					String newValue = "help" + level + ".css"; //$NON-NLS-1$ //$NON-NLS-2$
					tab.getHTMLSubstitutionMap().put("help.css", newValue); //$NON-NLS-1$
				} else {
					tab.getHTMLSubstitutionMap().remove("help.css"); //$NON-NLS-1$
				}
			}
			helpLauncher.setDivider((int) (175 * FontSizer.getFactor(level)));

			// navigation bar and search components
			helpLauncher.setNavigationVisible(true);

			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension dim = helpLauncher.getSize();
			dim.width = Math.min((9 * screen.width) / 10, (int) ((1 + level * 0.35) * dim.width));
			dim.height = Math.min((9 * screen.height) / 10, (int) ((1 + level * 0.35) * dim.height));
			helpLauncher.setSize(dim);

			// BH 2021.03.21 we are already using the contentPane
			//helpDialog.setContentPane(helpLauncher.getContentPane());
			FontSizer.setFonts(helpDialog, FontSizer.getLevel());

			helpDialog.pack();
			int x = (screen.width - helpDialog.getBounds().width) / 2;
			int y = (screen.height - helpDialog.getBounds().height) / 2;
			helpDialog.setLocation(x, y);
		}
		// refresh navigation bar components in case locale has changed
		Component[] search = HelpFinder.getNavComponentsFor(helpLauncher);
		Component[] comps = new Component[search.length + 2];
		System.arraycopy(search, 0, comps, 0, search.length);
		Tracker.pdfHelpButton.setText(TrackerRes.getString("Tracker.Button.PDFHelp")); //$NON-NLS-1$
		comps[comps.length - 2] = Tracker.pdfHelpButton;
		comps[comps.length - 1] = Box.createHorizontalStrut(4);
		helpLauncher.setNavbarRightEndComponents(comps);
		return helpDialog;
	}

	/**
	 * Shows a specified help topic.
	 *
	 * @param selectedNode the name of the help node to be displayed
	 */
	protected void showHelp(String selectedNode) {
		getHelpDialog(); // create dialog and launcher if needed
		helpLauncher.setSelectedNode(selectedNode);
		helpDialog.setVisible(true);
	}

	/**
	 * Shows a specified help topic by keyword: gettingstarted, install, linux, GUI,
	 * video, filters, tracks, coords, axes, tape, offset, calibration, pointmass,
	 * cm, vector, vectorsum, profile, rgbregion, particle, plot, datatable, xml,
	 * etc.
	 *
	 * @param keywords   the keywords of the help node to be displayed
	 * @param pageNumber the html page number
	 */
	protected void showHelp(String keywords, int pageNumber) {
		boolean firstTime = helpDialog == null;
		getHelpDialog(); // create dialog and launcher if needed
		if (keywords == null && firstTime) {
			keywords = "help"; //$NON-NLS-1$
		}
		helpLauncher.setSelectedNodeByKey(keywords, pageNumber);
		if (firstTime)
			helpLauncher.clearHistory();
		helpDialog.setVisible(true);
	}

	/**
	 * Shows the track control if any user tracks are present.
	 *
	 * @param panel the tracker panel
	 */
	protected void showTrackControl(final TrackerPanel panel) {
		if (panel.getUserTracks().size() > 0) {
			SwingUtilities.invokeLater(() -> {
				TrackControl tc = TrackControl.getControl(panel);
				if (tc.positioned && !tc.isEmpty()) {
					tc.setVisible(true);
				}
			});
		}
	}

	/**
	 * Shows the notes, if any.
	 *
	 * @param panel the tracker panel
	 */
	protected void showNotes(final TrackerPanel panel) {
		final JButton button = getToolBar(panel).notesButton;
		SwingUtilities.invokeLater(() -> {
			TTrack track = panel.getSelectedTrack();
			if (!panel.hideDescriptionWhenLoaded
					&& ((track != null && track.getDescription() != null && !track.getDescription().trim().equals("")) //$NON-NLS-1$
							|| (track == null && panel.getDescription() != null && !panel.getDescription().trim().equals("")))) { //$NON-NLS-1$
				if (!button.isSelected())
					button.doClick();
			} else if (button.isSelected())
				button.doClick();
		});
	}

//  /**
//   * Checks the current memory usage. If the total memory being used approaches 
//   * the max available, this reopens Tracker in a new larger java vm.
//   */
//  public void checkMemory() {
//  	System.gc();
//  	Runtime runtime = Runtime.getRuntime();
//  	double total = runtime.totalMemory();
//  	double max = runtime.maxMemory();
//  	JOptionPane.showMessageDialog(this, "memory "+total+" of "+max); //$NON-NLS-1$ //$NON-NLS-2$
//  	if (total/max > 0.6 && OSPRuntime.getLaunchJarPath() != null) {
//  		int result = JOptionPane.showConfirmDialog(this, "Resize memory to "+2*max+"?"); //$NON-NLS-1$ //$NON-NLS-2$
//      if (result != JOptionPane.YES_OPTION) return;
//  		// save trackerPanel fileNames
//  		ArrayList<File> files = new ArrayList<File>();
//  		for (int i = 0; i < getTabCount(); i++) {
//  			File file = getTrackerPanel(i).getDataFile();
//  			if (file != null) files.add(file);
//  		}
//  		// dispose of this frame 
//  		this.dispose();
//  		// launch Tracker in new vm
//      // construct the command to execute
//      final java.util.Vector<String> cmd = new java.util.Vector<String>();
//      cmd.add("java"); //$NON-NLS-1$
//      String classPath = OSPRuntime.getLaunchJarPath();
//      // convert colons to semicolons
//      classPath = classPath.replace(':', ';');
//      // replace semicolons with platform-dependent path separator
//      char pathSeparator = System.getProperty("path.separator").charAt(0);   //$NON-NLS-1$
//      classPath = classPath.replace(';', pathSeparator);
//      cmd.add("-classpath");                                                 //$NON-NLS-1$
//      cmd.add(classPath);
//      cmd.add(Tracker.class.getName());
//      String memoryArg = "-Xmx"+2*max; //$NON-NLS-1$
//      cmd.add(memoryArg);
//      memoryArg = "-Xms"+2*max; //$NON-NLS-1$
//      cmd.add(memoryArg);
//      Iterator<File> it = files.iterator();
//      while (it.hasNext()) {
//      	String arg = it.next().getPath();
//        cmd.add(arg);
//      }
//      // launch thread for new VM
//      Runnable launchRunner = new Runnable() {
//         public void run() {
//            OSPLog.finer(cmd.toString());
//            String[] cmdarray = cmd.toArray(new String[0]);
//            try {
//               Process proc = Runtime.getRuntime().exec(cmdarray);
//               BufferedInputStream errStream=new BufferedInputStream(proc.getErrorStream());
//               StringBuffer buff= new StringBuffer();
//               while(true){
//                 int datum=errStream.read();
//                 if(datum==-1) break;
//                 buff.append((char)datum);
//               }
//               errStream.close();
//               String msg=buff.toString().trim();
//               if(msg.length()>0){
//                 OSPLog.info("error buffer: " + buff.toString()); //$NON-NLS-1$
//               }
//            } catch(Exception ex) {
//               ex.printStackTrace();
//            }
//         }
//      };
//      Thread relauncher = new Thread(launchRunner);
//      relauncher.setPriority(Thread.NORM_PRIORITY);
//      relauncher.start();           
//  	}
//  }

	/**
	 * Gets the object array for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return the object array
	 */
	private Object[] getObjects(TrackerPanel trackerPanel) {
		return getObjects(getTab(trackerPanel));
	}

	/**
	 * Gets the (singleton) clipboard listener.
	 *
	 * @return the ClipboardListener
	 */
	protected ClipboardListener getClipboardListener() {
		if (clipboardListener == null && OSPRuntime.allowAutopaste) {
			clipboardListener = new ClipboardListener(this);
			clipboardListener.start();
		}
		return clipboardListener;
	}

	/**
	 * Starts or ends the clipboard listener as needed.
	 */
	protected void checkClipboardListener() {
		// do we need clipboard listener?
		SwingUtilities.invokeLater(() -> {
			boolean needListener = alwaysListenToClipboard;
			if (!needListener) {
				// do any pasted data tracks exist?
				try {
					for (int i = 0; i < getTabCount(); i++) {
						TrackerPanel trackerPanel = getTrackerPanel(i);
						ArrayList<DataTrack> list = trackerPanel.getDrawablesTemp(DataTrack.class);
						// do any tracks have null source?
						for (int m = 0, n = list.size(); m < n; m++) {
							DataTrack next = list.get(m);
							if (next.getSource() == null) {
								// null source, so data is pasted
								needListener = true;
								break;
							}
						}
						list.clear();
					}
				} catch (Exception ex) {
				}
			}

			if (needListener) {
				getClipboardListener();
			} else {
				if (clipboardListener == null)
					return;
				// end existing listener
				clipboardListener.end();
				clipboardListener = null;
			}
		});
	}

	/**
	 * Creates the GUI.
	 */
	private void createGUI() {
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				frameResized();
			}
		});
		// add focus listener to notify ParticleDataTracks and other listeners
		addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				firePropertyChange(PROPERTY_TFRAME_WINDOWFOCUS, null, null); // $NON-NLS-1$
			}
		});
		// create notes actions and dialog
		saveNotesAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (notesTextPane.getBackground() == Color.WHITE)
					return;
				String desc = notesTextPane.getText();
				TrackerPanel trackerPanel = getSelectedPanel();
				if (trackerPanel != null && notesDialog.getName() != "canceled") { //$NON-NLS-1$
					trackerPanel.changed = true;
					TTrack track = trackerPanel.getTrack(notesDialog.getName());
					if (track != null && !desc.equals(track.getDescription())) {
						track.setDescription(desc);
					} else if (!desc.equals(trackerPanel.getDescription())) {
						trackerPanel.setDescription(desc);
						trackerPanel.hideDescriptionWhenLoaded = !displayWhenLoadedCheckbox.isSelected();
					}
				}
				notesTextPane.setBackground(Color.WHITE);
				cancelNotesDialogButton.setEnabled(false);
				closeNotesDialogButton.setEnabled(true);
				closeNotesDialogButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
			}
		};
		notesDialog = new JDialog(this, false) {
			@Override
			public void setVisible(boolean vis) {
				super.setVisible(vis);

				TrackerPanel trackerPanel = getSelectedPanel();
				if (trackerPanel != null) {
					getToolBar(trackerPanel).notesButton.setSelected(vis);
				}
			}
		};
		JPanel infoContentPane = new JPanel(new BorderLayout());
		notesDialog.setContentPane(infoContentPane);
		notesTextPane = new JTextPane();
		notesTextPane.setBackground(Color.WHITE);
		notesTextPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					String url = e.getURL().toString();
					org.opensourcephysics.desktop.OSPDesktop.displayURL(url);
				}
			}
		});
		notesTextPane.setPreferredSize(new Dimension(420, 200));
		notesTextPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				TrackerPanel trackerPanel = getSelectedPanel();
				if (!trackerPanel.isEnabled("notes.edit")) //$NON-NLS-1$
					return;
				notesTextPane.setBackground(YELLOW);
				closeNotesDialogButton.setText(TrackerRes.getString("PrefsDialog.Button.Save")); //$NON-NLS-1$
				cancelNotesDialogButton.setEnabled(true);
			}
		});
		notesTextPane.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.getOppositeComponent() != cancelNotesDialogButton)
					saveNotesAction.actionPerformed(null);
			}
		});
		JScrollPane textScroller = new JScrollPane(notesTextPane);
		infoContentPane.add(textScroller, BorderLayout.CENTER);
		JPanel buttonbar = new JPanel(new FlowLayout());
		infoContentPane.add(buttonbar, BorderLayout.SOUTH);
		displayWhenLoadedCheckbox = new JCheckBox(TrackerRes.getString("TFrame.NotesDialog.Checkbox.ShowByDefault")); //$NON-NLS-1$
		displayWhenLoadedCheckbox.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TrackerPanel trackerPanel = getSelectedPanel();
				if (trackerPanel != null) {
					trackerPanel.hideDescriptionWhenLoaded = !displayWhenLoadedCheckbox.isSelected();
				}
			}
		});
		buttonbar.add(displayWhenLoadedCheckbox);
		buttonbar.add(Box.createHorizontalStrut(50));
		cancelNotesDialogButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
		cancelNotesDialogButton.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notesDialog.setName("canceled"); //$NON-NLS-1$
				notesDialog.setVisible(false);
			}
		});
		buttonbar.add(cancelNotesDialogButton);
		closeNotesDialogButton = new JButton(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		closeNotesDialogButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notesDialog.setVisible(false);
			}
		});
		buttonbar.add(closeNotesDialogButton);
		notesDialog.pack();
		// create the tabbed pane
		tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
		setContentPane(new JPanel(new BorderLayout()));
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		// create the default menubar
		createDefaultMenuBar();
		// add listener to change menubar, toolbar, track control when tab changes
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				doTabStateChanged();
			}
		});
		closeItem = new JMenuItem();
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeTab(getSelectedPanel());
			}
		});
		popup.add(closeItem);
		tabbedPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				TrackerPanel panel = getSelectedPanel();
				if (panel == null || !panel.isEnabled("file.close")) //$NON-NLS-1$
					return;
				if (OSPRuntime.isPopupTrigger(e)) {
					closeItem.setText(TrackerRes.getString("TActions.Action.Close") + " \"" //$NON-NLS-1$ //$NON-NLS-2$
							+ tabbedPane.getTitleAt(getSelectedTab()) + "\""); //$NON-NLS-1$
					FontSizer.setFonts(popup, FontSizer.getLevel());
					popup.show(tabbedPane, e.getX(), e.getY());
				}
			}
		});
	}

	protected void doTabStateChanged() {
		TrackerPanel newPanel = null;
		TrackerPanel oldPanel = prevPanel;

		// hide exportZipDialog
		if (haveExportDialog && ExportVideoDialog.videoExporter != null) {
			ExportVideoDialog.videoExporter.trackerPanel = null;
		}
		if (haveThumbnailDialog && ThumbnailDialog.thumbnailDialog != null) {
			ThumbnailDialog.thumbnailDialog.trackerPanel = null;
		}
		// update prefsDialog
		if (prefsDialog != null) {
			prefsDialog.trackerPanel = null;
		}
		// clean up items associated with old panel
		if (playerBar != null) {
			Container frame = playerBar.getTopLevelAncestor();
			if (frame != null && frame != this)
				frame.setVisible(false);
		}
		if (prevPanel != null) {
			if (prevPanel.dataBuilder != null) {
				boolean vis = prevPanel.dataToolVisible;
				prevPanel.dataBuilder.setVisible(false);
				prevPanel.dataToolVisible = vis;
			}
			if (prevPanel.getPlayer() != null) {
				ClipInspector ci = prevPanel.getPlayer().getVideoClip().getClipInspector();
				if (ci != null)
					ci.setVisible(false);
			}
			Video vid = prevPanel.getVideo();
			if (vid != null) {
				vid.getFilterStack().setInspectorsVisible(false);
			}
		}
		// refresh current tab items
		Object[] objects = getObjects(tabbedPane.getSelectedIndex());
		if (objects == null) {
			// show defaultMenuBar
			setJMenuBar(defaultMenuBar);
		} else {
			MainTView mainView = (MainTView) objects[TFRAME_MAINVIEW];
			newPanel = mainView.getTrackerPanel();
			prevPanel = newPanel;
			// update prefsDialog
			if (prefsDialog != null) {
				prefsDialog.trackerPanel = newPanel;
			}
			// refresh the notes dialog and button
			newPanel.refreshNotesDialog();
			JButton notesButton = getToolBar(newPanel).notesButton;
			notesButton.setSelected(notesDialog.isVisible());
			// refresh trackbar
			((TTrackBar) objects[TFRAME_TRACKBAR]).refresh();
			// refresh and replace menu bar
			TMenuBar menubar = (TMenuBar) objects[TFRAME_MENUBAR];

//  menubar.refresh();
			setJMenuBar(menubar);
			// show floating player
			playerBar = mainView.getPlayerBar();
			Container frame = playerBar.getTopLevelAncestor();
			if (frame != null && frame != this)
				frame.setVisible(true);
			if (newPanel.dataBuilder != null)
				newPanel.dataBuilder.setVisible(newPanel.dataToolVisible);
			Video vid = newPanel.getVideo();
			if (vid != null) {
				vid.getFilterStack().setInspectorsVisible(true);
			}
		}
		// update prefsDialog
		if (prefsDialog != null && prefsDialog.isVisible()) {
			prefsDialog.refreshGUI();
		}
		firePropertyChange(PROPERTY_TFRAME_TAB, oldPanel, newPanel); // $NON-NLS-1$
		// BH added 2020.11.24
		clearHoldPainting();
		repaintT(newPanel);
	}

	protected void frameResized() {
		TrackerPanel trackerPanel = getSelectedPanel();
		if (!isLayoutAdaptive || trackerPanel == null)
			return;
		Rectangle rect = getBounds();
		isLayoutChanged = isPortraitLayout != (rect.height > rect.width);
		if (maximizedView > -1) {
			maximizeView(trackerPanel, maximizedView);
			trackerPanel.dividerLocs = null;
			return;
		}
		if (isLayoutChanged) {
			// determine if dimensions are portrait or landscape and arrange views
			isPortraitLayout = rect.height > rect.width;
			for (int i = 0; i < getTabCount(); i++) {
				trackerPanel = getTrackerPanel(i);
				boolean defaultViewsVisible = areViewsVisible(DEFAULT_VIEWS, trackerPanel);
				boolean moreViewsVisible = areViewsVisible(OTHER_VIEWS, trackerPanel);
				arrangeViews(trackerPanel, defaultViewsVisible, moreViewsVisible);
			}
		}
		isLayoutChanged = false;
	}

	private void createDefaultMenuBar() {
		// create the default (empty) menubar
		int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		defaultMenuBar = new DeactivatingMenuBar();
		setJMenuBar(defaultMenuBar);
		// file menu
		JMenu fileMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.File")); //$NON-NLS-1$
		defaultMenuBar.add(fileMenu);
		// new tab item
		JMenuItem newItem = new JMenuItem(TrackerRes.getString("TActions.Action.NewTab")); //$NON-NLS-1$
		newItem.setAccelerator(KeyStroke.getKeyStroke('N', keyMask));
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTrackerPanel(false, null);
			}
		});
		fileMenu.add(newItem);
		if (!OSPRuntime.isApplet) {
			fileMenu.addSeparator();
			// open file item
			Icon icon = Tracker.getResourceIcon("open.gif", true); //$NON-NLS-1$
			JMenuItem openItem = new JMenuItem(TrackerRes.getString("TActions.Action.Open"), icon); //$NON-NLS-1$
			openItem.setAccelerator(KeyStroke.getKeyStroke('O', keyMask));
			openItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doOpenFileFromDialog();
				}
			});
			fileMenu.add(openItem);
			// open recent menu
			recentMenu = new JMenu();
			fileMenu.add(recentMenu);
			fileMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					refreshOpenRecentMenu(recentMenu);
				}

				@Override
				public void menuDeselected(MenuEvent e) {
				}

				@Override
				public void menuCanceled(MenuEvent e) {
				}

			});

			fileMenu.addSeparator();
			// openBrowser item
			icon = Tracker.getResourceIcon("open_catalog.gif", true); //$NON-NLS-1$
			JMenuItem openBrowserItem = new JMenuItem(TrackerRes.getString("TActions.Action.OpenBrowser"), icon); //$NON-NLS-1$
			openBrowserItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getLibraryBrowser().setVisible(true);
				}
			});
			fileMenu.add(openBrowserItem);
			fileMenu.addSeparator();
			// exit item
			JMenuItem exitItem = new JMenuItem(TrackerRes.getString("TActions.Action.Exit")); //$NON-NLS-1$
			exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', keyMask));
			exitItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Tracker.exit();
				}
			});
			fileMenu.add(exitItem);
		}
		// edit menu
		JMenu editMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Edit")); //$NON-NLS-1$
		defaultMenuBar.add(editMenu);
		// language menu
		languageMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.Language")); //$NON-NLS-1$
		languageMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				setLangMenu(languageMenu);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});
		editMenu.add(languageMenu);
		checkLocale();
		// preferences item
		JMenuItem prefsItem = new JMenuItem(TrackerRes.getString("TActions.Action.Config")); //$NON-NLS-1$
		prefsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPrefsDialog();
			}
		});
		editMenu.addSeparator();
		editMenu.add(prefsItem);

//		// video menu
//		JMenu videoMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Video")); //$NON-NLS-1$
//		videoMenu.setEnabled(false);
//		defaultMenuBar.add(videoMenu);
//		// tracks menu
//		JMenu tracksMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Tracks")); //$NON-NLS-1$
//		tracksMenu.setEnabled(false);
//		defaultMenuBar.add(tracksMenu);
//		// coords menu
//		JMenu coordsMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Coords")); //$NON-NLS-1$
//		coordsMenu.setEnabled(false);
//		defaultMenuBar.add(coordsMenu);
		// help menu
		defaultMenuBar.add(TMenuBar.getTrackerHelpMenu(null, null));
	}

	private void checkLocale() {
		if (TrackerRes.locale != Locale.ENGLISH && TrackerRes.locale != Locale.US) {
			// try for exact match (unlikely)
			Locale[] locales = Tracker.getLocales();
			for (int i = 0; i < locales.length; i++) {
				Locale loc = locales[i];
				if (loc.equals(TrackerRes.locale)) {
					setLanguage(loc.toString());
					return;
				}
			}
			// match just country
			for (int i = 0; i < locales.length; i++) {
				Locale loc = locales[i];
				if (loc.getLanguage().equals(TrackerRes.locale.getLanguage())) {
					setLanguage(loc.getLanguage());
					return;
				}
			}
		}

	}

	public void setLangMenu(JMenu languageMenu) {
		Action languageAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLanguage(e.getActionCommand());
			}
		};
		languageMenu.removeAll();
		ButtonGroup languageGroup = new ButtonGroup();
		JMenuItem selected = null;
		Locale[] locales = Tracker.getLocales();
		for (int i = 0; i < locales.length; i++) {
			Locale loc = locales[i];
			String lang = OSPRuntime.getDisplayLanguage(loc);
			String co = loc.getCountry();
			// special handling for portuguese BR and PT
			if (co != null && co != "") {
				lang += " (" + co + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			} else if (!OSPRuntime.isJS && loc.getLanguage().equals("ko")) {
				lang = "Korean";// BH characters not working in Java
			}
			JMenuItem item = new JRadioButtonMenuItem(lang);
			item.setActionCommand(loc.toString());
			item.addActionListener(languageAction);
			languageMenu.add(item);
			languageGroup.add(item);
			if (loc.equals(TrackerRes.locale)) {
				selected = item;
			}
		}
		// add "other" language item at end
		// the following item and message is purposely not translated
		JMenuItem otherLanguageItem = new JMenuItem("Other"); //$NON-NLS-1$
		languageMenu.addSeparator();
		languageMenu.add(otherLanguageItem);
		otherLanguageItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(TFrame.this, "Do you speak a language not yet available in Tracker?" //$NON-NLS-1$
						+ "\nTo learn more about translating Tracker into your language" //$NON-NLS-1$
						+ "\nplease contact Douglas Brown at dobrown@cabrillo.edu.", //$NON-NLS-1$
						"New Translation", //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		(selected == null ? languageMenu.getItem(0) : selected).setSelected(true);
		FontSizer.setMenuFonts(languageMenu);
	}

	protected void setLanguage(String language) {
		if (language.equals(mylang))
			return;
		mylang = language;
		Locale[] locales = Tracker.getLocales();
		for (int i = 0; i < Tracker.incompleteLocales.length; i++) {
			if (language.equals(Tracker.incompleteLocales[i][0].toString())) {
				Locale locale = (Locale) Tracker.incompleteLocales[i][0];
				String lang = OSPRuntime.getDisplayLanguage(locale);
				// the following message is purposely not translated
				JOptionPane.showMessageDialog(this,
						"This translation has not been updated since " + Tracker.incompleteLocales[i][1] //$NON-NLS-1$
								+ ".\nIf you speak " + lang + " and would like to help translate" //$NON-NLS-1$ //$NON-NLS-2$
								+ "\nplease contact Douglas Brown at dobrown@cabrillo.edu.", //$NON-NLS-1$
						"Incomplete Translation: " + lang, //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
				break;
			}
		}
		
		for (int i = 0; i < locales.length; i++) {
			if (language.equals(locales[i].toString())) {
				TrackerRes.setLocale(locales[i]);
				return;
			}
		}
	}

	DataDropHandler getDataDropHandler() {
		return (dataDropHandler == null ? (dataDropHandler = new DataDropHandler()) : dataDropHandler);
	}
	
	/**
	 * Initializes a new tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	private void initialize(TrackerPanel trackerPanel) {
		TMenuBar.getMenuBar(trackerPanel).setAllowRefresh(false);
		Tracker.setProgress(81);
		trackerPanel.initialize(fileDropHandler);

		// set divider locations
		// validate in advance of setting divider locations
		// to ensure dividers are set properly
		validate(); 
		boolean portrait = isPortraitLayout();
		if (trackerPanel.dividerLocs == null) {
			setDividerLocation(trackerPanel, SPLIT_MAIN, portrait? 1.0: DEFAULT_MAIN_DIVIDER);
			setDividerLocation(trackerPanel, SPLIT_RIGHT, DEFAULT_RIGHT_DIVIDER);
			setDividerLocation(trackerPanel, SPLIT_LEFT, portrait? DEFAULT_LEFT_DIVIDER: 1.0);
//			setDividerLocation(trackerPanel, SPLIT_BOTTOM, 1.0); // becomes previous
			setDividerLocation(trackerPanel, SPLIT_BOTTOM, DEFAULT_BOTTOM_DIVIDER);
		} else {
			int w = 0;
			int[] order = portrait ? PORTRAIT_DIVIDER_ORDER : DEFAULT_ORDER;
			for (int i = 0; i < order.length; i++) {
				JSplitPane pane = getSplitPane(trackerPanel, i);
				if (i == SPLIT_MAIN)
					w = pane.getMaximumDividerLocation();
				int max = i == SPLIT_BOTTOM ? w : pane.getMaximumDividerLocation();
				double loc = trackerPanel.dividerLocs[order[i]];
				loc = getConvertedDividerLoc(i, loc);
				pane.setDividerLocation((int) (loc * max));
			}
			trackerPanel.dividerLocs = null;
		}
		validate(); // after setting divider locations
		trackerPanel.initialize(null);
		Tracker.setProgress(90);
		TMenuBar.getMenuBar(trackerPanel).setAllowRefresh(true);
//		saveCurrentDividerLocations(trackerPanel);
	}
	
	protected static boolean isPortraitLayout() {
		return isLayoutAdaptive && isPortraitLayout;
	}
	
	/**
	 * Converts and returns converted divider location (0.0 <= loc <= 1.0).
	 * No conversion is made if not portrait layout.
	 * 
	 * @param splitPaneIndex 0-3
	 * @param loc the divider loc
	 * @return the converted divider loc
	 */
	protected double getConvertedDividerLoc(int splitPaneIndex, double loc) {
		if (isPortraitLayout())
			switch(splitPaneIndex) {
			case SPLIT_MAIN:
				return loc > 0.92? 1.0: DEFAULT_MAIN_DIVIDER;
			case SPLIT_RIGHT:
				return loc > 0.92? 1.0: loc < 0.08? 0.0: DEFAULT_RIGHT_DIVIDER;
			case SPLIT_LEFT:
				return loc > 0.92? 1.0: DEFAULT_LEFT_DIVIDER;
			case SPLIT_BOTTOM:
				return loc > 0.92? 1.0: loc < 0.08? 0.0: DEFAULT_BOTTOM_DIVIDER;
			}
		return loc;
	}

	/**
	 * a nonnegative number; when 0 we are painting; when positive, some operation
	 * is holding repaints
	 * 
	 */
	private int paintHold = 0;

	/**
	 * Increment/decrement the paintHold counter. Will not decrement below 0.
	 * 
	 * @param b true to increment the counter; false to decrement
	 * 
	 */
	public void holdPainting(boolean b) {
		if (!Tracker.doHoldRepaint)
			return;
			paintHold += (b ? 1 : paintHold > 0 ? -1 : 0);
			//System.out.println("TFrame.paintHold " + paintHold);
//		OSPLog.debug("TFrame.paintHold=" + paintHold);
//		if (b || paintHold == 0)
//			tabbedPane.setVisible(!b);
	}

	/**
	 * check the paintHold counter
	 * 
	 * @return true if paintHold is zero
	 * 
	 */
	public boolean isPaintable() {
//		System.out.println("TFrame.isPaintable " + paintHold + " " + isVisible() + " " + !getIgnoreRepaint());

		return isVisible() &&
		paintHold == 0
		 && !getIgnoreRepaint()
		;
	}

	public boolean hasPaintHold() {
		return paintHold != 0;
	}

	/**
	 * For emergency use only!
	 * 
	 */
	public void clearHoldPainting() {
//		if (paintHold != 0) {
//			OSPLog.debug("TFrame.paintHold cleared ");
//		}
		paintHold = 0;
	}

	/**
	 * Adds a component to those following this frame. When the frame is displaced
	 * the component will be displaced equally.
	 * 
	 * @param c   the component
	 * @param pt0 the initial location of this frame
	 * 
	 */
	public void addFollower(Component c, Point ignored) {
		Point pt0 = getLocation();
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				// determine displacement
				Point fp = getLocation();
				int dx = fp.x - pt0.x;
				int dy = fp.y - pt0.y;
				// set pt0 to current location
				pt0.x = fp.x;
				pt0.y = fp.y;
				// displace c
				Point p = c.getLocation();
				p.x += dx;
				p.y += dy;
				c.setLocation(p);
			}
		});
	}

	public static void addMenuListener(JMenu m, Runnable r) {
		m.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				r.run();
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});
	}

	/**
	 * @return true if there is at least one tab and it is not changed and it has the default title
	 */
  boolean haveContent() {
		return (getTabCount() > 0 && (getTrackerPanel(0).changed
				|| !tabbedPane.getTitleAt(0).equals(TrackerRes.getString("TrackerPanel.NewTab.Name")))); //$NON-NLS-1$
	}

	/**
	 * @return the tab number to remove if the specified TrackerPanel is present and clean, or -1 if not
	 */
  int getRemovableTabNumber(TrackerPanel panel) {
  	int tab = getTab(panel);
		boolean clean = tab > -1 && !panel.changed
				&& tabbedPane.getTitleAt(tab).equals(TrackerRes.getString("TrackerPanel.NewTab.Name")); //$NON-NLS-1$
		return clean? tab: -1;
	}

	/**
	 * Returns a clean TrackerPanel.
	 * Uses the blank untitled TrackerPanel in frame tab 0 if it is unchanged (JS only)
	 *
	 * @return a clean TrackerPanel.
	 */
	synchronized TrackerPanel getCleanTrackerPanel() {
		if (getTabCount() == 0 || haveContent() || !OSPRuntime.isJS)
			return new TrackerPanel(this);
		TrackerPanel panel = getTrackerPanel(0);
		JSplitPane[] panes = getSplitPanes(panel);
		setDefaultWeights(panes);
//		panel.changed = true;	
		return panel;
	}

	/**
	 * Remove the first tab if it is empty and there are at least n tabs  (1 or 2)
	 */
	public void removeEmptyTabIfTabCountGreaterThan(int n) {
		if (getTabCount() > n && !haveContent())
			removeTabNow(0);
	}

	public void removeTabNow(int i) {
		TrackerPanel tp = getTrackerPanel(i);
		if (tp != null)
		new TabRemover(tp).executeSynchronously();
	}

	private String lastExperiment = "";
	
	/**
	 * runnable for when loadObject is complete, from TrackerIO
	 */
	public Function<List<String>, Void> whenObjectLoadingComplete;

	/**
	 * Replace any open tabs with a single tab loaded with the given path.
	 * JavaScript only?
	 * 
	 * Called from Tracker (for JavaScript) and TMenuBar (for testing in Java)
	 * 
	 * @param path
	 * @author Bob Hanson
	 */
	public void loadExperimentURL(String path) {
		if(path != null && !path.startsWith("http")) { // assume path is relative to html page
			path = "https://./" + path;
//			String base=OSPRuntime.getDocbase();;
//			path=base+path;
			OSPLog.fine("Loading Tracker experiment path="+path);
		}
		if (path == null && (path = GUIUtils.showInputDialog(this, "Load Experiment", "Load Experiment",
				JOptionPane.QUESTION_MESSAGE, lastExperiment)) == null)
			return;
		if (TrackerIO.isVideo(new File(path))) {
			loadVideo(path, false, null, null); // imports video into current tab
			return;
		}		
		if (getTabCount() > 0)
			removeAllTabs();
		try {
			doOpenURL(path);
		} catch (Throwable t) {
			removeAllTabs();
		}
	}
	
	protected void loadLibraryRecord(LibraryResource record) {	
		openLibraryResource(record, () -> {
			TrackerPanel trackerPanel = getSelectedPanel();
			Timer timer = new Timer(200, (ev) -> {
				libraryBrowser.doneLoading();
				requestFocus();	
				if (trackerPanel != null) {
					trackerPanel.changed = false;
					repaintT(trackerPanel);
				}
			});
			timer.setRepeats(false);
			timer.start();
		});
	}
	
	public void openLibraryResource(LibraryResource record, Runnable whenDone) {
		try {
			libraryBrowser.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			String target = record.getAbsoluteTarget();
			if (!ResourceLoader.isHTTP(target)) {
				target = ResourceLoader.getURIPath(XML.getResolvedPath(record.getTarget(), record.getBasePath()));
			}
			// download comPADRE targets to osp cache
			if (target.indexOf("document/ServeFile.cfm?") >= 0) { //$NON-NLS-1$
				String fileName = record.getProperty("download_filename"); //$NON-NLS-1$
				try {
					target = ResourceLoader.downloadToOSPCache(target, fileName, false).toURI().toString();
					if (VideoIO.isCanceled())
						return;
				} catch (Exception ex) {
					String s = TrackerRes.getString("TFrame.Dialog.LibraryError.Message"); //$NON-NLS-1$
					JOptionPane.showMessageDialog(libraryBrowser, s + " \"" + record.getName() + "\"", //$NON-NLS-1$ //$NON-NLS-2$
							TrackerRes.getString("TFrame.Dialog.LibraryError.Title"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			String lcTarget = target.toLowerCase();
			if (lcTarget.endsWith(".trk") || ResourceLoader.isJarZipTrz(lcTarget, false)) {
				if (ResourceLoader.getResourceZipURLsOK(target) == null) {
					String s = TrackerRes.getString("TFrame.Dialog.LibraryError.FileNotFound.Message"); //$NON-NLS-1$
					JOptionPane.showMessageDialog(libraryBrowser, s + " \"" + XML.getName(target) + "\"", //$NON-NLS-1$ //$NON-NLS-2$
							TrackerRes.getString("TFrame.Dialog.LibraryError.FileNotFound.Title"), //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
					libraryBrowser.setVisible(true);
					return;
				}
				try {
					ArrayList<String> uriPaths = new ArrayList<String>();
					uriPaths.add(target);
					VideoIO.loader = TrackerIO.openFromLibrary(uriPaths, this, whenDone);
					whenDone = null;
				} catch (Throwable t) {
				}
				return;
			}
			if (TrackerIO.isVideo(new File(target))) {
				loadVideo(target, true, libraryBrowser, whenDone);
				whenDone = null;
				return;
			}
			String path = target;
			for (String ext : VideoIO.KNOWN_VIDEO_EXTENSIONS) {
				if (lcTarget.endsWith("." + ext)) {
					if (libraryBrowser != null)
						libraryBrowser.setMessage(null, null);
					VideoIO.handleUnsupportedVideo(path, ext, null, getSelectedPanel(), "TFrame known video ext");
					return;
				}
			}
		} finally {
			libraryBrowser.setCursor(Cursor.getDefaultCursor());
			if (whenDone != null)
				whenDone.run();
		}
	}

	/**
	 * Java only; from ExportVideoDialog
	 * 
	 * @param path
	 */
	public void doOpenExportedAndUpdateLibrary(String path) {
		loadedFiles.remove(path);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		TrackerIO.openFileFromDialog(new File(path), this, () -> {
			// open the TR Z in the Library Browser
			setCursor(Cursor.getDefaultCursor());
			libraryBrowser.open(path);
			libraryBrowser.setVisible(true);
			Timer timer = new Timer(1000, (e) -> {
				LibraryTreePanel treePanel = libraryBrowser.getSelectedTreePanel();
				if (treePanel != null) {
					treePanel.refreshSelectedNode();
				}
			});
			timer.setRepeats(false);
			timer.start();
		});
	}

	
	public void doOpenFileFromDialog() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		TrackerIO.openFileFromDialog(null, this, () -> {
			setCursor(Cursor.getDefaultCursor());
		});
	}

	public void doOpenURL(String url) {
		TrackerPanel selected = getSelectedPanel();
		if (selected != null) {
			selected.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		TrackerIO.openURL(url, this, () -> {
			setCursor(Cursor.getDefaultCursor());
			if (selected != null) {
				selected.setMouseCursor(Cursor.getDefaultCursor());
			}
		});
	}


	/**
	 * Loads (imports) a video file or image stack into a tab after caching its contents locally.
	 * Tab may be a new tab or the currently selected tab.
	 * 
	 * @param path path to the video
	 * @param asNewTab true to load into a new tab
	 * @param whenDone optional Runnable
	 */
	void loadVideo(String path, boolean asNewTab, LibraryBrowser libraryBrowser, Runnable whenDone) {
		// from loadExperimentURL and openLibraryResource actions
		if (!VideoIO.checkMP4(path, libraryBrowser, getSelectedPanel()))
			return;					
		// load a video file or a directory containing images
		File localFile = ResourceLoader.download(path, null, false);
		Runnable importer = new Runnable() {
			@Override
			public void run() {
				TrackerIO.importVideo(XML.getAbsolutePath(localFile), getSelectedPanel(), whenDone);							
			}		
		};
		if (asNewTab)
			addTrackerPanel(false, importer);
		else {
			importer.run();
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
		 * @param obj     the object to save
		 */
		@Override
		public void saveObject(XMLControl control, Object obj) {
			TFrame frame = (TFrame) obj;
			// save tabs with data files or unchanged videos
			// save both relative paths (relative to tabsetFile) and absolute paths
			String relativeTo = frame.tabsetFile != null ? XML.getDirectoryPath(XML.getAbsolutePath(frame.tabsetFile))
					: XML.getUserDirectory();
			relativeTo = XML.forwardSlash(relativeTo);
			ArrayList<String[]> pathList = new ArrayList<String[]>();
			for (int i = 0; i < frame.getTabCount(); i++) {
				TrackerPanel trackerPanel = frame.getTrackerPanel(i);
				File file = trackerPanel.getDataFile();
				if (file != null) {
					String path = XML.getAbsolutePath(file);
					String relativePath = XML.getPathRelativeTo(path, relativeTo);
					pathList.add(new String[] { path, relativePath });
				} else {
					Video video = trackerPanel.getVideo();
					if (!trackerPanel.changed && video != null) {
						String path = (String) video.getProperty("absolutePath"); //$NON-NLS-1$
						if (path != null) {
							path = XML.forwardSlash(path);
							String relativePath = XML.getPathRelativeTo(path, relativeTo);
							pathList.add(new String[] { path, relativePath });
						}
					}
				}
			}
			String[][] paths = pathList.toArray(new String[0][0]);
			control.setValue("tabs", paths); //$NON-NLS-1$
		}

		/**
		 * Creates a new object. This returns null--must load an existing TFrame.
		 *
		 * @param control the XMLControl with the object data
		 * @return the newly created object
		 */
		@Override
		public Object createObject(XMLControl control) {
			return null;
		}

		/**
		 * Loads an object with data from an XMLControl synchronously
		 *
		 * @param control the control
		 * @param obj     the object
		 * @return the loaded object
		 */
		@Override
		public Object loadObject(XMLControl control, Object obj) {
			TFrame frame = (TFrame) obj;
			String[][] tabs = (String[][]) control.getObject("tabs"); //$NON-NLS-1$
			if (tabs == null)
				return loadObjectFinally(frame, null);
			FileFilter videoFilter = new VideoFileFilter();
			String base = control.getString("basepath"); //$NON-NLS-1$
			File dataFile = null;
//			boolean prev = TrackerIO.isLoadInSeparateThread();
//			TrackerIO.setLoadInSeparateThread("TFrame.loadObject0", false);
			List<String> files = new ArrayList<>();
			for (String[] next : tabs) {
				File file = null;
				Resource res = null;
				if (base != null) {
					file = new File(base, next[1]); // next[1] is relative path
					res = ResourceLoader.getResource(file.getPath());
				}
				if (res == null) {
					file = new File(XML.getUserDirectory(), next[1]);
					res = ResourceLoader.getResource(file.getPath());
				}
				if (res == null && next[0] != null) {
					file = new File(next[0]); // next[0] is absolute path
					res = ResourceLoader.getResource(file.getPath());
				}
				if (res == null) {
					if (OSPRuntime.isJS) {
						JOptionPane.showMessageDialog(frame, "\"" + next[1] + "\" " //$NON-NLS-1$ //$NON-NLS-2$
								+ MediaRes.getString("VideoClip.Dialog.VideoNotFound.Message")); //$NON-NLS-1$
						continue;
					} else /** @j2sNative */
					{
						int i = JOptionPane.showConfirmDialog(frame, "\"" + next[1] + "\" " //$NON-NLS-1$ //$NON-NLS-2$
								+ MediaRes.getString("VideoClip.Dialog.VideoNotFound.Message"), //$NON-NLS-1$
								TrackerRes.getString("TFrame.Dialog.FileNotFound.Title"), //$NON-NLS-1$
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (i == JOptionPane.YES_OPTION) {
							TrackerIO.getChooser().setSelectedFile(file);
							@SuppressWarnings("deprecation")
							java.io.File[] a = TrackerIO.getChooserFiles("open"); //$NON-NLS-1$
							if (a != null) {
								file = a[0];
							} else {
								continue;
							}
						} else {
							continue;
						}
					}
				}
				// BH! but if the file is set in the prev block, res is still null. So this will
				// not work?
				if (res != null && !videoFilter.accept(file)) {
					if (dataFile == null)
						dataFile = file;
					files.add(XML.getAbsolutePath(file));
				}
			}
			File file0 = dataFile;
			if (frame.whenObjectLoadingComplete != null) {
				files.add(0, XML.getAbsolutePath(dataFile));
				frame.whenObjectLoadingComplete.apply(files);
				frame.whenObjectLoadingComplete = null;
				return frame;
			}
			TrackerIO.openFiles(frame, files, () -> {
				loadObjectFinally(frame, file0);
			});
//			TrackerIO.setLoadInSeparateThread("TFrame.loadObject1", prev);
			return frame;
		}

		/**
		 * Just the tail end of loadObject
		 * 
		 * @param frame
		 * @param dataFile
		 * @return
		 */
		protected TFrame loadObjectFinally(TFrame frame, File dataFile) {
			if (frame.whenObjectLoadingComplete != null) {
				frame.whenObjectLoadingComplete.apply(new ArrayList<>());
				frame.whenObjectLoadingComplete = null;
			}
			frame.setSelectedTab(dataFile);
			return frame;
		}

		// BH 2020.04.16 just a sketch; not implemented yet.

//		/**
//		 * The same as above, just asynchronous (untested).
//		 * 
//		 * @param control
//		 * @param frame
//		 * @param whenDone
//		 */
//		public synchronized void loadObjectAsync(XMLControl control, TFrame frame, Function<Object, Void> whenDone) {
//			Object tabs = control.getObject("tabs"); //$NON-NLS-1$
//			if (tabs == null) {
//				loadObjectFinally(frame, null, whenDone);
//				return;
//			}
//			new State(control, frame, whenDone, (String[][]) tabs).start();
//		}
//
//		/**
//		 * The essential SwingJS state machine works in Java and JavaScript and consists
//		 * of:
//		 * 
//		 * 1) a set of all possible states (as final static int) Typically, these are
//		 * INIT, LOOP (or NEXT), and DONE, but they could be far more complex.
//		 * 
//		 * 2) a set of final and nonfinal fields that persist only as long as the state
//		 * exists.
//		 * 
//		 * 3) at least one loop defining the course of actions for the state.
//		 * 
//		 * Action starts with starting of a StateHelper dedicated to this State. When
//		 * complete, you can provide a "whenDone" Function or Runnable. Or configure it
//		 * any way you want.
//		 * 
//		 * Action can be interrupted (reversibly) any time by calling
//		 * stateHelper.interrupt().
//		 * 
//		 * Action can be made synchronous or restarted using stateHelper.next(STATE_XXX)
//		 * or asynchronous using stateHelper.delayedState(ms, STATE_XXX).
//		 * 
//		 * @author hansonr
//		 *
//		 */
//		private class State implements StateMachine {
//
//			// possible states
//
//			final static int STATE_IDLE = -1; // used sometimes for animation holds; not used in this class
//
//			final static int STATE_INIT = 0;
//			final static int STATE_NEXT = 1;
//			final static int STATE_DONE = 2;
//
//			private final StateHelper stateHelper;
//			private final TFrame frame;
//			private final String[][] tabs;
//			private final String base;
//			private final VideoFileFilter videoFilter;
//			private final boolean wasLoadThread;
//			private final Function<Object, Void> whenDone;
//
//			private int index;
//			private File dataFile;
//
//			public State(XMLControl control, TFrame frame, Function<Object, Void> whenDone, String[][] tabs) {
//
//				this.whenDone = whenDone;
//				this.frame = frame;
//				this.base = control.getString("basepath"); //$NON-NLS-1$
//				this.tabs = tabs;
//				videoFilter = new VideoFileFilter();
//				wasLoadThread = TrackerIO.isLoadInSeparateThread();
//				stateHelper = new StateHelper(this);
//				TrackerIO.setLoadInSeparateThread("TFrame.State0", false);
//			}
//
//			public void start() {
//				stateHelper.next(STATE_INIT);
//
//			}
//
//			@Override
//			public synchronized boolean stateLoop() {
//
//				while (stateHelper.isAlive()) {
//					switch (stateHelper.getState()) {
//					case STATE_INIT:
//						index = 0;
//						stateHelper.setState(STATE_NEXT);
//						continue;
//					case STATE_NEXT:
//						// for (String[] next : tabs) {
//						if (index >= tabs.length)
//							return stateHelper.next(STATE_DONE);
//						String[] next = tabs[index];
//						File file = null;
//						Resource res = null;
//						if (base != null) {
//							file = new File(base, next[1]); // next[1] is relative path
//							res = ResourceLoader.getResource(file.getPath());
//						}
//						if (res == null) {
//							file = new File(XML.getUserDirectory(), next[1]);
//							res = ResourceLoader.getResource(file.getPath());
//						}
//						if (res == null && next[0] != null) {
//							file = new File(next[0]); // next[0] is absolute path
//							res = ResourceLoader.getResource(file.getPath());
//						}
//						if (res != null) {
//							processResource(res, file);
//							continue; // STATE_NEXT
//						}
//						int i = JOptionPane.showConfirmDialog(frame, "\"" + next[1] + "\" " //$NON-NLS-1$ //$NON-NLS-2$
//								+ MediaRes.getString("VideoClip.Dialog.VideoNotFound.Message"), //$NON-NLS-1$
//								TrackerRes.getString("TFrame.Dialog.FileNotFound.Title"), //$NON-NLS-1$
//								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//						if (i != JOptionPane.YES_OPTION) {
//							continue;
//						}
//						TrackerIO.getChooser().setSelectedFile(file);
//						TrackerIO.getChooserFilesAsync("open", new Function<File[], Void>() {
//
//							@Override
//							public Void apply(File[] files) {
//								if (files != null) {
//									File file = files[0];
//									// BH makes more sense to me...
//									processResource(null, file);
//								}
//								stateHelper.next(STATE_NEXT);
//								return null;
//							}
//						});
//						// } // end for
//						break;
//					case STATE_DONE:
//						TrackerIO.setLoadInSeparateThread("TFrame.State1", wasLoadThread);
//						loadObjectFinally(frame, dataFile, whenDone);
//						break;
//					default:
//					case STATE_IDLE:
//						break;
//					}
//				}
//				return false;
//			}
//
//			private void processResource(Resource res, File file) {
//				if (res != null && !videoFilter.accept(file)) {
//					if (dataFile == null)
//						dataFile = file;
//					TrackerIO.openTabFileAsync(file, frame, null);
//				}
//			}
//
//		}

	}

	public void setSelectedTab(File dataFile) {
		int n = getTab(dataFile);
		//OSPLog.finest("TFrame selecting first tabset tab at index " + n); //$NON-NLS-1$
		setSelectedTab(n);
	}

	public class DataDropHandler extends TransferHandler {

		@SuppressWarnings("deprecation")
		DataFlavor df = DataFlavor.plainTextFlavor;
		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			 return (support.getTransferable().isDataFlavorSupported(df));
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			try {
				getSelectedPanel().importDataAsync((String) t.getTransferData(df), null, null);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

	}

	/**
	 *  An empty JDialog that serves as a modal blocker when the progress monitor is visible.
	 */
	private JDialog frameBlocker;

	public void setFrameBlocker(boolean blocking) {
		if (blocking) {
			if (frameBlocker != null)
				System.out.println("TrackerIO async already blocking!");
			frameBlocker = new JDialog(this, true);
//			frameBlocker.setSize(10, 10);
//			SwingUtilities.invokeLater(() -> {
//				frameBlocker.setVisible(true);
//			});
		} else if (frameBlocker != null) {
//			frameBlocker.setVisible(false);
			frameBlocker = null;
		}
		getJMenuBar().setEnabled(!blocking);
		tabbedPane.setEnabled(!blocking);
		getContentPane().setVisible(!blocking);
	}
	
	@Override
	public void setJMenuBar(JMenuBar bar) {
		super.setJMenuBar(bar);
		bar.setEnabled(frameBlocker == null);
	}
	
	static class DeactivatingMenuBar extends JMenuBar {
		@Override
		public void setEnabled(boolean b) {
			super.setEnabled(b);
			Component[] c = getComponents();
			for (int i = 0; i < c.length; i++)
				c[i].setEnabled(b);
		}
	}


}
