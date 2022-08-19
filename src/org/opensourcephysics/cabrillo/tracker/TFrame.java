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
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import org.opensourcephysics.display.OSPRuntime.Disposable;
import org.opensourcephysics.media.core.ClipInspector;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
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
import org.opensourcephysics.tools.ToolsRes;

/**
 * This is the main frame for Tracker.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class TFrame extends OSPFrame implements PropertyChangeListener {

	// preloading for JavaScript

	public static Font textLayoutFont = new JTextField().getFont();

	static {
		ToolTipManager.sharedInstance().setDismissDelay(2000);
		new TextLayout("X", textLayoutFont, OSPRuntime.frc);
	}

	// static fields

	class TTabPanel extends JPanel implements Disposable {

		private Object[] objects;
		Integer panelID;
		Box toolbarBox;

		public TTabPanel(TrackerPanel trackerPanel, Object[] objects) {
			super(new BorderLayout());
			panelID = trackerPanel.getID();
			this.objects = objects;
		}

		public TrackerPanel getTrackerPanel() {
			return getTrackerPanelForID(panelID);
		}

		public Object[] getObjects() {
			return objects;
		}

		public void setToolbarVisible(boolean vis) {
			if (toolbarBox == null) {
				int i = panelID.intValue();
				TToolBar bar = _atoolbars[i];
				if (bar == null)
					return;
				toolbarBox = Box.createVerticalBox();
				toolbarBox.add(bar);
			}
			if (vis) {
				add(toolbarBox, BorderLayout.NORTH);
			} else {
				remove(toolbarBox);
			}
		}

		@Override
		public void paintComponent(Graphics g) {
			if (!isPaintable())
				return;
			super.paintComponent(g);
		}

		@Override
		public void dispose() {
			panelID = null;
			objects = null;
			toolbarBox = null;
			removeAll();
			//System.out.println("TFrame.TTabPanel.dispose");
		}

		@Override
		public void finalize() {
			OSPLog.finalized(this);
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

	protected static final int DEFAULT_VIEWS = 0;
	protected static final int OTHER_VIEWS = 1;

	protected static final double DEFAULT_MAIN_DIVIDER = 0.67;
	protected static final double DEFAULT_RIGHT_DIVIDER = 0.57;
	protected static final double DEFAULT_LEFT_DIVIDER = 0.57;
	protected static final double DEFAULT_BOTTOM_DIVIDER = 0.50;

	protected static boolean isPortraitOrientation;
	private static boolean isLayoutChanged;
	private static boolean isLayoutAdaptive;
	
	public static boolean haveExportDialog;
	public static boolean haveThumbnailDialog;

	// instance fields

	protected ClipboardListener clipboardListener;
	protected LibraryBrowser libraryBrowser;
	protected Launcher helpLauncher;
	private JToolBar playerBar;
	protected JDialog helpDialog;
	protected JDialog dataToolDialog;
	protected PrefsDialog prefsDialog;

	private DataDropHandler dataDropHandler;
	protected FileDropHandler fileDropHandler;
	
	private JPopupMenu popup = new JPopupMenu();
	private JMenuItem closeItem;
	private DefaultMenuBar defaultMenuBar;
	private JMenu recentMenu;
	protected JTabbedPane tabbedPane;
	
	protected Action saveNotesAction;
	protected Action openRecentAction;
	
	protected ArrayList<String> loadedFiles = new ArrayList<String>();

	protected File tabsetFile; // used when saving tabsets
	protected String currentLangugae = "en";
	protected Integer prevPanelID;
	protected int maximizedView = TView.VIEW_UNSET;
	protected int framesLoaded, prevFramesLoaded; // used when loading xuggle videos
	protected boolean splashing = true;

	
	private boolean anglesInRadians = Tracker.isRadians;
	private boolean alwaysListenToClipboard;
	

	private Notes notes;

	/**
	 * Create a map of known arguments, setting any found arguments to null. Integer
	 * arguments are stringified and rounded in case JavaScript is passing numbers.
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
	 * @param options include optional -dim Dimension [-video Video | -panel
	 *                TrackerPanel]
	 */
	public TFrame(Map<String, Object> options) {
		super("Tracker"); //$NON-NLS-1$
		init(options);
	}

	private void init(Map<String, Object> options) {
		setTitle("Tracker" + (OSPRuntime.isJS? " Online": ""));
		if (options == null)
			options = new HashMap<>();
		isLayoutAdaptive = (options.get("-adaptive") != null);
		Dimension dim = (Dimension) options.get("-dim");
		Rectangle bounds = (Rectangle) options.get("-bounds");
		Video video = (Video) options.get("-video");
		TrackerPanel panel = (video != null ? new TrackerPanel(this, video) : (TrackerPanel) options.get("-panel"));
		setName("Tracker"); //$NON-NLS-1$
		if (Tracker.TRACKER_ICON != null)
			setIconImage(Tracker.TRACKER_ICON.getImage());
		// set default close operation
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		TrackerRes.addListener(this); // $NON-NLS-1$

		// set size and limit maximized size so taskbar not covered
		Rectangle screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		if (OSPRuntime.isJS)
			// DB when this is set in Java the frame doesn't maximize fully!
			setMaximizedBounds(screenRect);
		// process -bounds or -dim option

		if (isLayoutAdaptive) {
			bounds = getAdaptiveBounds(true);
		}
		if (bounds == null) {
			if (dim == null) {
				double extra = FontSizer.getFactor(Tracker.preferredFontLevel) - 1;
				int w = (int) Math.min(screenRect.width * 0.9, (1024 + extra * 800));
				int h = (int) Math.min(screenRect.height * 0.9, 3 * w / 4);
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
		isPortraitOrientation = rect.height > rect.width;
		
		// set transfer handler on tabbedPane
		fileDropHandler = new FileDropHandler(this);
		// set transfer handler for CTRL-V paste
		tabbedPane.setTransferHandler(fileDropHandler);
		if (panel != null) {
			addTab(panel, ADD_NOSELECT | ADD_REFRESH, () -> {
			});
		}
	}

	@SuppressWarnings("unused")
	private Rectangle getAdaptiveBounds(boolean isInit) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//		int w = (int) (0.9 * dim.width);
//		int margin = (int) (0.05 * dim.width);
//		int h = (int) (0.7 * (dim.height - 80));
		int w = (int) (0.99 * dim.width);
		int margin = (int) (0.005 * dim.width);
		int h = (int) (0.98 * (dim.height - 2));
		Rectangle rect = new Rectangle(margin, 2, w, h);
		if (isInit) {
			// JS only
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
				 *            console.log("Orientation changed"); onOrient.run$(); }, false);
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
		// OSPLog.debug(Performance.timeCheckStr("TFrame.repaintT " +
		// c.getClass().getSimpleName(), Performance.TIME_MARK));
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
		// OSPLog.debug("TFrame.paint");
		super.paint(g);
	}

	/**
	 * Adds a tab that displays the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @param addMode      ADD_SELECT | ADD_REFRESH
	 * @param whenDone
	 */
	public void addTab(final TrackerPanel trackerPanel, int addMode, Runnable whenDone) {
		boolean doSelect = ((addMode & ADD_SELECT) != 0);
		boolean doRefresh = ((addMode & ADD_REFRESH) != 0);
		Integer panelID = trackerPanel.getID();
		int tab = getTab(panelID);
		TTabPanel tabPanel = null;
		if (tab >= 0) { // tab exists
			String name = trackerPanel.getTitle();
			synchronized (tabbedPane) {
				tabbedPane.setTitleAt(tab, name);
				tabbedPane.setToolTipTextAt(tab, trackerPanel.getToolTipPath());
			}
			tabPanel = getTabPanel(trackerPanel);
		} else {
			setIgnoreRepaint(true);

			// tab does not already exist
			// listen for changes that affect tab title

			Object[] objects = new Object[3];
			tabPanel = new TTabPanel(trackerPanel, objects);

			trackerPanel.addPropertyChangeListener(VideoPanel.PROPERTY_VIDEOPANEL_DATAFILE, this); // $NON-NLS-1$
			trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO, this); // $NON-NLS-1$
			// set up trackerPanel to listen for angle format property change
			addPropertyChangeListener(PROPERTY_TFRAME_RADIANANGLES, trackerPanel); // $NON-NLS-1$
			// create the tab panel components
			// Note thta MainTView will create a TTrackBar.
			objects[TFRAME_MAINVIEW] = new MainTView(trackerPanel);
			objects[TFRAME_SPLITPANES] = getSplitPanes(trackerPanel);
			objects[TFRAME_VIEWCHOOSERS] = createTViews(trackerPanel);
			String name = trackerPanel.getTitle();
			synchronized (tabbedPane) {
				tabbedPane.addTab(name, tabPanel);
				tab = getTab(panelID);
				tabbedPane.setToolTipTextAt(tab, trackerPanel.getToolTipPath());
			}

			getToolBar(panelID, true);
			getMenuBar(panelID, true);
			//getTrackBar(panelID, true);
		}

		// from here on trackerPanel's top level container is this TFrame,
		// so trackerPanel.getFrame() method will return non-null

		setupAddedPanel(tabPanel, tab, trackerPanel, doSelect, doRefresh, whenDone);
		doTabStateChanged();
	}

	private void setupAddedPanel(TTabPanel tabPanel, int tab, TrackerPanel trackerPanel, boolean doSelect,
			boolean doRefresh, Runnable whenDone) {
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
						TrackChooserTView view = (TrackChooserTView) tviews[k];
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

		placeViews(tabPanel, trackerPanel, viewChoosers);
		// add toolbars at north position
		tabPanel.setToolbarVisible(true);
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
		Integer panelID = trackerPanel.getID();
		OSPRuntime.trigger(100, (e) -> {
				// TTrackBar will only refresh after TFrame is visible
				TrackerPanel tp = getTrackerPanelForID(panelID);
				if (doRefresh)
					tp.refreshTrackBar();
				// TTrackBar.getTrackbar(trackerPanel).refresh();
				// DB following line needed to autoload data functions from external files
				tp.getDataBuilder();
				tp.changed = false;
		});
	}

	/**
	 * Saves all tabs if user approved. Stops if any is canceled.
	 * @param isExit TODO
	 * @param whenEachApproved Function to apply to each TrackerPanel unless
	 *                         canceled
	 * @param whenAllApproved  Runnable to run after all have run whenEachApproved
	 * @param whenCanceled     Runnable to run if canceled
	 */
	public void saveAllTabs(boolean isExit, Function<Integer, Void> whenEachApproved, Runnable whenAllApproved, Runnable whenCanceled) {
		// save all tabs in last-to-first order
		final int[] tab = { getTabCount() - 1 };
		TrackerPanel trackerPanel = getTrackerPanelForTab(tab[0]);
		if (trackerPanel == null)
			return;
		Function<Boolean, Void> whenClosed = new Function<Boolean, Void>() {
			@Override
			public Void apply(Boolean doSave) {
				TrackerPanel trackerPanel = getTrackerPanelForTab(tab[0]);
				if ((!isExit || doSave) && whenEachApproved != null) {
					whenEachApproved.apply(trackerPanel.getID());
				}
				tab[0]--;
				if (tab[0] > -1) {
					getTrackerPanelForTab(tab[0]).askSaveIfChanged(this, whenCanceled);
				} else if (whenAllApproved != null)
					whenAllApproved.run();
				return null;
			}
		};
		
		trackerPanel.askSaveIfChanged(whenClosed, whenCanceled);
	}

	protected void relaunchCurrentTabs() {
		final ArrayList<String> filenames = new ArrayList<String>();
		saveAllTabs(false, new Function<Integer, Void>() {
			// for each approved
			@Override
			public Void apply(Integer panelID) {
				TrackerPanel trackerPanel = getTrackerPanelForID(panelID);
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
	public void removeAllTabs(boolean isExit) {
		if (!haveContent() && getTabCount() == 1) {
			removeTabNow(0);
			Disposable.dump();
			return;
		}
		hideNotes();
		ArrayList<Integer> panels = new ArrayList<Integer>();
		boolean[] cancelled = new boolean[] { false };
		removingAll = true;
		saveAllTabs(false, (panelID) -> {
			// when each approved, add to list
			if (!cancelled[0])
				panels.add(panelID);
			return null;
		}, () -> {
			if (isExit)
				System.exit(0);
			// when all approved remove tabs synchronously
			while (panels.size() > 0) {
				removeTabSynchronously(getTrackerPanelForID(panels.remove(0)));
			}
			Disposable.dump();
			checkMemTest();
			removingAll = false;
		}, () -> {
			// if cancelled
			cancelled[0] = true;
			panels.clear();
			removingAll = false;
		});
	}

	private void checkMemTest() {
		if (!OSPRuntime.isJS) { //TEST_BH
			System.gc();
			System.gc();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.gc();
			System.gc();
			System.out.println("TFrame memory:" + OSPRuntime.getMemoryStr());
		}

	}

//	/**
//	 * An AsyncSwingWorker to remove a tab.
//	 */
//	class TabRemover extends AsyncSwingWorker {
//
//		TrackerPanel trackerPanel;
//		TTabPanel tabPanel;
//
//		TabRemover(TrackerPanel trackerPanel) {
//			super(null, null, 1, 0, 1);
//			this.trackerPanel = trackerPanel.ref();
//		}
//
//		@Override
//		public void initAsync() {
//		}
//
//		@Override
//		public int doInBackgroundAsync(int i) {
//			removeTabSynchronously(trackerPanel);
//			return 1;
//		}
//
//		@Override
//		public void doneAsync() {
//		}
//	}

	private void hideNotes() {
		if (notesVisible()) {
			notes.setVisible(false);
		}
	}
	
	public final static int STATE_ACTIVE      = 0;
	public final static int STATE_BLOCKED     = 2;
	public final static int STATE_REMOVING    = 3;

	private int state = STATE_ACTIVE;
	private boolean removingAll;
	
	@Override
	public int getState() {
		return state;
	}
	
	public boolean isRemovingAll() {
		return removingAll;
	}

	/**
	 * Removes a tracker panel tab. This method is called from Tracker.testFinal as
	 * well as action listeners for the tab popup menu and File close menu items.
	 *
	 * @param trackerPanel the tracker panelf
	 */
	public boolean doCloseAction(TrackerPanel trackerPanel) {
		if (getTab(trackerPanel.getID()) < 0)
			return false;

		Function<Boolean, Void> removeTab = (doSave) -> {
			removeTabSynchronously(trackerPanel);
			return null;
		};
		trackerPanel.askSaveIfChanged(removeTab, null);
		return true;
	}

	public void removeTabSynchronously(TrackerPanel trackerPanel) {
		if (trackerPanel == null)
			return;

		state = STATE_REMOVING;
		
		Integer panelID = trackerPanel.getID();
		int id = panelID.intValue();
		int tab = getTab(panelID);

		//System.out.println("TFrame sync remove for " + trackerPanel);
		TTabPanel tabPanel = getTabPanel(trackerPanel);

		closeAllDialogs(trackerPanel, tabPanel);

		// remove the tab immediately
		// BH 2020.11.24 thread lock
		// BH 2021.08.13 removed
		// Ah, the trick is that the next call will trigger TFrame.doTabStateChanged, 
		// which takes care of dialogs by firing PROPERTY_TFRAME_TAB
		try {
	//		synchronized (tabbedPane) {
			tabbedPane.remove(tab);
			tabbedPane.remove(tabPanel);
	//		}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (trackerPanel.trackControl != null) {
			deallocate(trackerPanel.trackControl);
		}

		Object[] objects = tabPanel.getObjects();

		// dispose of TViewChoosers and TViews
		TViewChooser[] sideViews = (TViewChooser[]) objects[TFRAME_VIEWCHOOSERS];
		if (sideViews != null)
			for (int i = 0; i < sideViews.length; i++) {
				deallocate(sideViews[i]);
				sideViews[i] = null;
			}
		objects[TFRAME_VIEWCHOOSERS] = null;

		// clean up main view--this is important as it disposes of floating JToolBar
		// videoplayer
		if (objects[TFRAME_MAINVIEW] != null)
			deallocate((Disposable) objects[TFRAME_MAINVIEW]);
		objects[TFRAME_MAINVIEW] = null;

		// BH MEMORY LEAK WAS HERE. By setting objects == null, toolbar, menubar, and
		// trackbar could not be found. We need objects here.

		// BH NO! objects = null;

		// dispose of trackbar, toolbar, menubar AFTER removing tab

		Disposable.deallocate(_atoolbars, id);
		Disposable.deallocate(_atrackbars, id);
		Disposable.deallocate(_amenubars, id);

		JSplitPane[] panes = (JSplitPane[]) objects[TFRAME_SPLITPANES];
		for (int i = 0; i < panes.length; i++) {
			JSplitPane pane = panes[i];
			pane.removeAll();
		}
		for (int i = 0; i < panes.length; i++) {
			panes[i] = null;
		}
		objects[TFRAME_SPLITPANES] = null;

		// remove the components from the tabs map

		if (prefsDialog != null) {
			prefsDialog.panelID = null;
		}
		Undo.undomap.remove(panelID);
		
		Disposable.deallocate(_apanels, id);		
		deallocatePanelID(panelID);
		System.gc();
		Disposable.deallocate(tabPanel);
		removePropertyChangeListener(TFrame.PROPERTY_TFRAME_RADIANANGLES, trackerPanel); // $NON-NLS-1$
		firePropertyChange(PROPERTY_TFRAME_TAB, trackerPanel, null); // $NON-NLS-1$

		trackerPanel = null;


		// change menubar and show floating player of newly selected tab, if any
		
		tabPanel = (TTabPanel) tabbedPane.getSelectedComponent();
		// OSPLog.debug(Performance.timeCheckStr("TFrame.removeTab 8",
		// Performance.TIME_MARK));
		objects = (tabPanel == null ? null : tabPanel.getObjects());
		JMenuBar currentBar = getJMenuBar();
		if (currentBar == defaultMenuBar) {
		} else if (objects == null) {
			// show defaultMenuBar
			setJMenuBar(defaultMenuBar);
			// we need to also remove this menubar from the _amenubars array
			Disposable.deallocate(_amenubars, id);
		} else if (tabPanel != null){
			// need id of new tab being displayed, not the one removed
			id = tabPanel.panelID.intValue(); // not nec., but a reminder that panelID is an Integer not int
			setJMenuBar(getMenuBar(id, true));
			getTrackBar(id, true).refresh();
			playerBar = ((MainTView) objects[TFRAME_MAINVIEW]).getPlayerBar();
			// could be moved from Main
			Container frame = playerBar.getTopLevelAncestor();
			if (frame != null && frame != this)
				frame.setVisible(true);
		}
		
		if (getTabCount() == 0) {
			clearAllReferences();
		}

		state = (frameBlocker == null ? STATE_ACTIVE : STATE_BLOCKED);

	}

	private void clearAllReferences() {
		if (notes != null) {
			notes.dispose();
			notes = null;
		}
		playerBar = null;
//		System.out.println("TFrame.clearAllReferences");
	}

	private void closeAllDialogs(TrackerPanel trackerPanel, TTabPanel tabPanel) {
		if (notesVisible() && !trackerPanel.getTitle().equals("Untitled")) {
			notes.dispose();
		}
	}

	/**
	 * Returns the tab index for the specified tracker panel, or -1 if no tab is
	 * found.
	 *
	 * @param ppanel the tracker panel
	 * @return the tab index
	 */
	public int getTab(Integer panelID) {
		for (int i = getTabCount(); --i >= 0;) {
			TTabPanel panel = (TTabPanel) tabbedPane.getComponentAt(i);
			if (panel.panelID == panelID)
				return i;
		}
		return -1;
	}

	/**
	 * Returns the tab index for the specified tracker panel based on panelID, or -1
	 * if no tab is found.
	 *
	 * @param tp the tracker panel
	 * @return the tab index
	 */
	public int getTabForID(Integer panelID) {
		for (int i = getTabCount(); --i >= 0;) {
			TTabPanel panel = (TTabPanel) tabbedPane.getComponentAt(i);
			if (panel.getTrackerPanel().getID() == panelID)
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
			for (int i = getTabCount(); --i >= 0;) {
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
		return tabbedPane == null ? -1 : tabbedPane.getSelectedIndex();
	}

	/**
	 * Sets the selected tab index.
	 *
	 * @param tab the tab index
	 */
	public void setSelectedTab(int tab) {
		if (tab < 0 || tab >= getTabCount())
			return;
		
		// note: This next call will trigger TFrame.doTabSTateChanged

		tabbedPane.setSelectedIndex(tab);
		updateNotesDialog(getTrackerPanelForTab(tab));
	}

	public void setSelectedTab(File dataFile) {
		setSelectedTab(getTab(dataFile));
	}

	/**
	 * Sets the selected tab specified by tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 */
	public void setSelectedTab(TrackerPanel trackerPanel) {
		setSelectedTab(getTab(trackerPanel.getID()));
	}

	/**
	 * Gets the tracker panel at the specified tab index.
	 *
	 * @param tab the tab index
	 * @return the tracker panel
	 */
	public TrackerPanel getTrackerPanelForTab(int tab) {
		return (tab < 0 || tab >= tabbedPane.getTabCount() ? null
				: ((TTabPanel) tabbedPane.getComponentAt(tab)).getTrackerPanel());
	}

	/**
	 * Gets the panel of the selected tab, if a tab is selected.
	 * 
	 * @return the selected panel or null if no tab is selected
	 */
	public TrackerPanel getSelectedPanel() {
		return getTrackerPanelForTab(getSelectedTab());
	}

	public void addTrackerPanel(boolean changedState, Runnable whenDone) {
		TrackerPanel newPanel = new TrackerPanel(this);
		Integer panelID = newPanel.getID();
		addTab(newPanel, ADD_SELECT | ADD_NOREFRESH, () -> {
			if (!changedState)
				getTrackerPanelForID(panelID).changed = false;
			if (whenDone == null)
				refresh();
			else
				whenDone.run();
		});
	}

	/**
	 * Gets the title of the specified tab.
	 *
	 * @param tab the tab index
	 * @return the title
	 */
	public String getTabTitle(int tab) {
		return (tab < 0 ? null : tabbedPane.getTitleAt(tab));
	}

	/**
	 * Refreshes the tab for the specified tracker panel.
	 *
	 * @param panel the tracker panel
	 */
	public void refreshTab(TrackerPanel panel) {
		int tab = getTab(panel.getID());
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

	protected static final int SPLIT_MAIN_RIGHT = 0;
	protected static final int SPLIT_PLOT_TABLE = 1;
	protected static final int SPLIT_MAIN_BOTTOM = 2;
	protected static final int SPLIT_WORLD_PAGE = 3;

	protected static final int SIDEVIEW_RIGHT_TOP = 0;
	protected static final int SIDEVIEW_RIGHT_BOTTOM = 1;
	protected static final int SIDEVIEW_BOTTOM_LEFT = 2;
	protected static final int SIDEVIEW_BOTTOM_RIGHT = 3;

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
	public void placeViews(TTabPanel tabPanel, TrackerPanel trackerPanel, TViewChooser[] viewChoosers) {
		if (viewChoosers == null)
			viewChoosers = new TViewChooser[0];
		int[] order = isPortraitLayout() ? PORTRAIT_VIEW_ORDER : DEFAULT_ORDER;
		Object[] objects = tabPanel.getObjects();
		TViewChooser[] choosers = (TViewChooser[]) objects[TFRAME_VIEWCHOOSERS];
		if (choosers != null)
			for (int i = 0; i < Math.min(viewChoosers.length, choosers.length); i++) {
				if (viewChoosers[i] != null)
					choosers[i] = viewChoosers[i];
			}
		if (order == null || order.length != viewChoosers.length) {
			order = DEFAULT_ORDER;
		}

		MainTView mainView = (MainTView) objects[TFRAME_MAINVIEW];
		JSplitPane[] panes = (JSplitPane[]) objects[TFRAME_SPLITPANES];

		if (((BorderLayout) tabPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER) != panes[SPLIT_MAIN_RIGHT]) {
			tabPanel.removeAll();
			tabPanel.add(panes[SPLIT_MAIN_RIGHT], BorderLayout.CENTER);
		}
		addPaneSafely(panes[SPLIT_MAIN_RIGHT], SPLIT_MAIN_BOTTOM, panes[SPLIT_MAIN_BOTTOM]);
		addPaneSafely(panes[SPLIT_MAIN_RIGHT], SPLIT_PLOT_TABLE, panes[SPLIT_PLOT_TABLE]);
		addPaneSafely(panes[SPLIT_MAIN_BOTTOM], SPLIT_MAIN_RIGHT, mainView);
		addPaneSafely(panes[SPLIT_MAIN_BOTTOM], SPLIT_WORLD_PAGE, panes[SPLIT_WORLD_PAGE]);
		if (choosers != null) {
			addPaneSafely(panes[SPLIT_PLOT_TABLE], SPLIT_MAIN_RIGHT, choosers[order[TView.VIEW_PLOT]]);
			addPaneSafely(panes[SPLIT_PLOT_TABLE], SPLIT_WORLD_PAGE, choosers[order[TView.VIEW_TABLE]]);
			addPaneSafely(panes[SPLIT_WORLD_PAGE], SPLIT_PLOT_TABLE, choosers[order[TView.VIEW_WORLD]]);
			addPaneSafely(panes[SPLIT_WORLD_PAGE], SPLIT_MAIN_BOTTOM, choosers[order[TView.VIEW_PAGE]]);
		}
	}

	public TTabPanel getTabPanel(TrackerPanel trackerPanel) {
		int tab = getTab(trackerPanel.getID());
		return (tab >= 0 ? (TTabPanel) tabbedPane.getComponentAt(tab) : null);
	}

	private void addPaneSafely(JSplitPane pane, int where, Component c) {
		switch (where) {
		case SPLIT_MAIN_RIGHT:
			if (pane.getTopComponent() != c)
				pane.setTopComponent(c);
			break;
		case SPLIT_PLOT_TABLE:
			if (pane.getRightComponent() != c)
				pane.setRightComponent(c);
			break;
		case SPLIT_MAIN_BOTTOM:
			if (pane.getLeftComponent() != c)
				pane.setLeftComponent(c);
			break;
		case SPLIT_WORLD_PAGE:
			if (pane.getBottomComponent() != c)
				pane.setBottomComponent(c);
			break;

		}
	}

	/**
	 * Arranges the views for a tracker panel, showing default views under or beside
	 * the video and the opposite for non-default views.
	 * 
	 * @param trackerPanel     the tracker panel
	 * @param showDefaultViews true to show default views
	 * @param showOtherViews   true to show non-default views
	 */
	public void arrangeViews(TrackerPanel trackerPanel, boolean showDefaultViews, boolean showOtherViews) {
		if (!isLayoutAdaptive)
			return;
		// place views in the right locations
		TTabPanel tabPanel = getTabPanel(trackerPanel);
		placeViews(tabPanel, trackerPanel, getViewChoosers(trackerPanel));
		// add toolbars at north position
		tabPanel.setToolbarVisible(true);
		// set divider properties according to visibility specified
		boolean showRight = (isPortraitOrientation ? showOtherViews :  showDefaultViews);
		setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, showRight ? DEFAULT_MAIN_DIVIDER : 1.0);
		setDividerLocation(trackerPanel, SPLIT_PLOT_TABLE, DEFAULT_RIGHT_DIVIDER);
		boolean showBottom = (isPortraitOrientation ? showDefaultViews : showOtherViews);
//		boolean showBottom = (!isPortraitOrientation && showOtherViews) || (isPortraitOrientation && showDefaultViews);
		setDividerLocation(trackerPanel, SPLIT_MAIN_BOTTOM, showBottom ? DEFAULT_LEFT_DIVIDER : 1.0);
		// bottom divider--delay needed in Java for correct placement
		Integer panelID = trackerPanel.getID();
		SwingUtilities.invokeLater(() -> {
			setDividerLocation(getTrackerPanelForID(panelID), SPLIT_WORLD_PAGE, DEFAULT_BOTTOM_DIVIDER);
		});
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
		if (choosers == null)
			return null;
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
	 * Find all selected panels of the given type and add them to the list. Use
	 * VIEW_UNSET to get all views.
	 * 
	 * @param panelID
	 * @param viewType [ VIEW_PLOT VIEW_TEXT VIEW_WORLD VIEW_PAGE VIEW_UNSET ]
	 * @param list     the return list, or null to start a new list
	 * @return list
	 */
	public List<TView> getTViews(Integer panelID, int viewType, List<TView> list) {
		if (list == null)
			list = new ArrayList<>();		
		TViewChooser[] choosers = getViewChoosers(panelID);
		for (int i = 0; i < choosers.length; i++) {
			if (choosers[i] == null)
				continue;
			TView[] views = choosers[i].getTViews();
			if (viewType == TView.VIEW_UNSET) {
				for (int j = 0; j < views.length; j++) {
					if (views[j] != null)
						list.add(views[j]);					
				}
			} else if (choosers[i].getSelectedViewType() == viewType) {
				list.add(views[viewType]);
			}
		}
		return list;
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
				PlotTView view = (PlotTView) views[TView.VIEW_PLOT];
				TTrack track = view.getSelectedTrack();
				buf.append(track == null ? "null," : track.getName() + ",");
			} else
				buf.append("null,");

			if (views[TView.VIEW_TABLE] != null) {
				TableTView view = (TableTView) views[TView.VIEW_TABLE];
				TTrack track = view.getSelectedTrack();
				buf.append(track == null ? "null" : track.getName());
			} else
				buf.append("null");
		}
		return buf.toString();
	}

	/**
	 * Determines whether a view pane is visible for the specified trackerPanel tab.
	 *
	 * @param position the view position index, a number from 0 to 3
	 * @param tp       the trackerPanel
	 * @return true if it is visible
	 */
	public boolean isViewPaneVisible(int position, Integer panelID) {
		JSplitPane[] panes = getSplitPanes(panelID);
		double[] locs = new double[panes.length];
		for (int i = 0; i < panes.length; i++) {
			int max = panes[i].getMaximumDividerLocation();
			locs[i] = 1.0 * panes[i].getDividerLocation() / max;
		}
		switch (position) {
		case SPLIT_MAIN_RIGHT:
			return locs[SPLIT_MAIN_RIGHT] < 0.95 && locs[SPLIT_PLOT_TABLE] > 0.05;
		case SPLIT_PLOT_TABLE:
			return locs[SPLIT_MAIN_RIGHT] < 0.95 && locs[SPLIT_PLOT_TABLE] < 0.95;
		case SPLIT_MAIN_BOTTOM:
			return locs[SPLIT_MAIN_BOTTOM] < 0.92 && locs[SPLIT_WORLD_PAGE] < 0.95; // BH was 0.95, but on my machine this is 0.926
		case SPLIT_WORLD_PAGE:
			return locs[SPLIT_MAIN_BOTTOM] < 0.95 && locs[SPLIT_WORLD_PAGE] > 0.05;
		}
		return false;
	}

	/**
	 * Determines whether the specified views are visible in a trackerPanel tab.
	 * Views may be DEFAULT_VIEWS (TViewChoosers[0/1]) or OTHER_VIEWS
	 * (TViewChoosers[2/3])
	 * 
	 * @param whichViews   DEFAULT_VIEWS or OTHER_VIEWS
	 * @param trackerPanel the trackerPanel
	 * @return true if views are visible
	 */
	public boolean areViewsVisible(int whichViews, TrackerPanel trackerPanel) {
		boolean standardLayout = getSplitPane(trackerPanel, 1).getTopComponent() == getViewChoosers(trackerPanel)[0];
		int splitPaneIndex = whichViews == DEFAULT_VIEWS && standardLayout ? 0
				: whichViews == OTHER_VIEWS && !standardLayout ? 0 : 2;
		JSplitPane pane = getSplitPane(trackerPanel, splitPaneIndex);
		int max = pane.getMaximumDividerLocation();
		int cur = pane.getDividerLocation();
		double loc = 1.0 * cur / max;
		return splitPaneIndex == 0 ? loc < 0.95 : loc < 0.92;
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
		return (objects == null ? null
				: objects[TFRAME_MAINVIEW] == null ? new MainTView(trackerPanel)
						: (MainTView) objects[TFRAME_MAINVIEW]);
	}

	/**
	 * Responds to property change events.
	 *
	 * @param e the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		TrackerPanel panel;
		Integer panelID;
		switch (e.getPropertyName()) {
		case VideoPanel.PROPERTY_VIDEOPANEL_DATAFILE:
		case TrackerPanel.PROPERTY_TRACKERPANEL_VIDEO: // from TrackerPanel //$NON-NLS-1$
			panel = (TrackerPanel) e.getSource();
			refreshTab(panel);
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
		case ToolsRes.OSP_PROPERTY_LOCALE: // from TrackerRes //$NON-NLS-1$
			// clear the existing menubars and actions
			Disposable.deallocate(_amenubars, _bsPanelIDs);
			// create new actions
			Tracker.createActions();
			// create new default menubar
			checkLocale();
			setJMenuBar(defaultMenuBar = new DefaultMenuBar());
			// replace and refresh the stored menubars and toolbars
			for (int i = getTabCount(); --i >= 0;) {
				Object[] objects = getObjects(i);
				MainTView mainView = (MainTView) objects[TFRAME_MAINVIEW];
				panel = mainView.getTrackerPanel();
				panelID = panel.getID();
				boolean changed = panel.changed; // save changed state and restore below
				// replace the stored menubar
				Disposable.deallocate(_amenubars, panelID.intValue());
				getMenuBar(panelID, true);
				CoordAxes axes = panel.getAxes();
				if (axes != null) {
					axes.setName(TrackerRes.getString("CoordAxes.New.Name")); //$NON-NLS-1$
				}
				panel.changed = changed;
				getToolBar(panelID, true).refresh(TToolBar.REFRESH_TFRAME_LOCALE);
				getTrackBar(panelID, true).refresh();
			}
			panel = getSelectedPanel();
			if (panel != null) {
				// replace current menubar
				TMenuBar menuBar = getMenuBar(panel.getID(), false);
				if (menuBar != null) {
					setJMenuBar(menuBar);

					menuBar.refresh(TMenuBar.REFRESH_TFRAME_LOCALE);
				}
				// show hint
				if (Tracker.startupHintShown) {
					panel.setMessage(TrackerRes.getString("Tracker.Startup.Hint")); //$NON-NLS-1$
				} else {
					// shows hint as side effect
					panel.setCursorForMarking(false, null);
				}
			} else {
				// show defaultMenuBar
				setJMenuBar(defaultMenuBar);
			}
			// refresh tabs
			for (int i = tabbedPane.getTabCount(); --i >= 0;) { // BH reversed
				panel = getTrackerPanelForTab(i);
				panelID = panel.getID();
				tabbedPane.setTitleAt(i, panel.getTitle());
				VideoPlayer player = panel.getPlayer();
				player.refresh();
				player.setLocale((Locale) e.getNewValue());
				Video vid = panel.getVideo();
				if (vid != null) {
					vid.getFilterStack().refresh();
				}
				// refresh track controls and toolbars
				TrackControl.getControl(panel).refresh();
				getToolBar(panelID, false).refresh(TToolBar.REFRESH_TFRAME_LOCALE2);
				getTrackBar(panelID, false).refresh();
				// refresh view panes
				TViewChooser[] choosers = getViewChoosers(panel);
				for (int j = 0; j < choosers.length; j++) {
					choosers[j].refresh();
				}
				// refresh autotracker
				if (panel.autoTracker != null) {
					panel.autoTracker.getWizard().textPaneSize = null;
					panel.autoTracker.getWizard().refreshGUI();
					panel.autoTracker.getWizard().pack();
				}
				// refresh prefs dialog
				if (prefsDialog != null && prefsDialog.isVisible()) {
					prefsDialog.refreshGUI();
				}
				// refresh pencil drawer
				PencilDrawer.getDrawer(panel).refresh();
				// refresh info dialog
				if (notesVisible())
					notes.refreshTextAndFonts();
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
		OSPRuntime.trigger(1000, (e) -> {
				Tracker.splash.dispose();
		});
	}


	public boolean isAnglesInRadians() {
		return anglesInRadians;
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
			if (prefsDialog.panelID != trackerPanel.getID()) {
				prefsDialog.panelID = trackerPanel.getID();
				prefsDialog.refreshGUI();
			}
		} else {
			// create PrefsDialog
			prefsDialog = new PrefsDialog(trackerPanel);
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
			// OSPLog.debug("TFrame allowViews is false");
			return new TViewChooser[4];
		}
		return new TViewChooser[] { newTViewChooser(trackerPanel, TView.VIEW_PLOT),
				newTViewChooser(trackerPanel, TView.VIEW_TABLE), newTViewChooser(trackerPanel, TView.VIEW_WORLD),
				newTViewChooser(trackerPanel, TView.VIEW_PAGE) };
	}

	private static TViewChooser newTViewChooser(TrackerPanel trackerPanel, int view) {
		TViewChooser c = new TViewChooser(trackerPanel, view);
//		Disposable.allocate(c);
		return c;
	}

	JSplitPane[] getSplitPanes(Integer panelID) {
		return getSplitPanes(getTrackerPanelForID(panelID));
	}

	/**
	 * Gets the split panes for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return an array of split panes
	 */
	JSplitPane[] getSplitPanes(final TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		if (objects != null && objects[TFRAME_SPLITPANES] != null) {
			return (JSplitPane[]) objects[TFRAME_SPLITPANES];
		}
		JSplitPane[] panes = new JSplitPane[4];
		panes[SPLIT_MAIN_RIGHT] = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT); // left/right split
		panes[SPLIT_PLOT_TABLE] = new JSplitPane(JSplitPane.VERTICAL_SPLIT); // plot/table split
		panes[SPLIT_MAIN_BOTTOM] = new JSplitPane(JSplitPane.VERTICAL_SPLIT); // video/bottom split
		panes[SPLIT_WORLD_PAGE] = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) // page/world split
		{
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(0, 0);
			}
		};
		panes[SPLIT_MAIN_RIGHT].setName("TOP(0)");
		panes[SPLIT_PLOT_TABLE].setName("RIGHT(1)");
		panes[SPLIT_MAIN_BOTTOM].setName("LEFT(2)");
		panes[SPLIT_WORLD_PAGE].setName("BOTTOM(3)");
		setDefaultWeights(panes);
		return panes;
	}

	private static void setDefaultWeights(JSplitPane[] panes) {
		setDefaultWeight(panes[SPLIT_MAIN_RIGHT], 1.0);
		setDefaultWeight(panes[SPLIT_PLOT_TABLE], 0.5); // right shared, trackerPanel expands
		setDefaultWeight(panes[SPLIT_MAIN_BOTTOM], 1.0);
		setDefaultWeight(panes[SPLIT_WORLD_PAGE], 0.5); // bottom shared, trackerPanel expands
	}

	private static void setDefaultWeight(JSplitPane pane, double d) {
		pane.setDividerSize(DEFAULT_DIVIDER_SIZE);
		pane.setResizeWeight(d);
		pane.setOneTouchExpandable(true);
	}

	public int getMaximizedView() {
		return maximizedView;
	}

	void maximizeView(TrackerPanel trackerPanel, int viewIndex) {
		maximizedView = viewIndex;
		JSplitPane[] panes = getSplitPanes(trackerPanel);
		for (int i = 0; i < panes.length; i++) {
			panes[i].setDividerSize(0);
		}
		int[] order = (isPortraitLayout() ? PORTRAIT_VIEW_ORDER : DEFAULT_ORDER);
		int viewPosition = viewIndex < order.length ? order[viewIndex] : viewIndex;
		switch (viewPosition) {
		case TView.VIEW_PLOT: // right upper
			panes[SPLIT_PLOT_TABLE].setResizeWeight(1);
			setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, 0.0);
			setDividerLocation(trackerPanel, SPLIT_PLOT_TABLE, 1.0);
			break;
		case TView.VIEW_TABLE: // right lower
			panes[SPLIT_PLOT_TABLE].setResizeWeight(0);
			setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, 0.0);
			setDividerLocation(trackerPanel, SPLIT_PLOT_TABLE, 0.0);
			break;
		case TView.VIEW_WORLD: // bottom right
			panes[SPLIT_WORLD_PAGE].setResizeWeight(0);
			setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, 1.0);
			setDividerLocation(trackerPanel, SPLIT_MAIN_BOTTOM, 0.0);
			setDividerLocation(trackerPanel, SPLIT_WORLD_PAGE, 0.0);
			break;
		case TView.VIEW_PAGE: // bottom left
			panes[SPLIT_WORLD_PAGE].setResizeWeight(1);
			setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, 1.0);
			int max = panes[0].getMaximumDividerLocation();
			setDividerLocation(trackerPanel, SPLIT_MAIN_BOTTOM, 0.0);
			setDividerLocation(trackerPanel, SPLIT_WORLD_PAGE, max);
			break;
		case TView.VIEW_MAIN: // main video view
			setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, 1.0);
			setDividerLocation(trackerPanel, SPLIT_MAIN_BOTTOM, 1.0);
			setDividerLocation(trackerPanel, SPLIT_WORLD_PAGE, 0.0);
		}
		TMenuBar menubar = getMenuBar(trackerPanel.getID(), true);
		menubar.setMenuTainted(TMenuBar.MENU_VIEW, true);
//		int tab = getTab(trackerPanel);
//		if (tab == -1) return;
//		TTabPanel tabPanel = (TTabPanel) tabbedPane.getComponentAt(tab);
//		tabPanel.setToolbarVisible(false);	
//		tabPanel.revalidate();
	}

	void saveCurrentDividerLocations(TrackerPanel trackerPanel) {
		if (maximizedView != TView.VIEW_UNSET)
			return;
		if (trackerPanel.dividerLocs == null)
			trackerPanel.dividerLocs = new double[4];
		for (int i = 0; i < trackerPanel.dividerFractions.length; i++) {
			JSplitPane pane = getSplitPane(trackerPanel, i);
			int max = pane.getMaximumDividerLocation();
			int cur = Math.min(pane.getDividerLocation(), max); // sometimes cur > max !!??
			trackerPanel.dividerLocs[i] = cur;
			double fraction = 1.0 * cur / max;
			fraction = fraction < MIN_DIVIDER_OFFSET && (i == 1 || i == 3) ? 0 : fraction;
			fraction = fraction > 1 - MIN_DIVIDER_OFFSET ? 1 : fraction;
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
				setDividerLocation(trackerPanel, i, (int) trackerPanel.dividerLocs[i]);
		}
		setDefaultWeights(getSplitPanes(trackerPanel));
		maximizedView = TView.VIEW_UNSET;
		TMenuBar menubar = getMenuBar(trackerPanel.getID(), true);
		menubar.setMenuTainted(TMenuBar.MENU_VIEW, true);
		if (isLayoutChanged) {
			frameResized();
		}

//		int tab = getTab(trackerPanel);
//		if (tab == -1) return;
//		TTabPanel tabPanel = (TTabPanel) tabbedPane.getComponentAt(tab);
//		tabPanel.setToolbarVisible(true);	
	}

	/**
	 * Gets the trackbar for the specified tracker panel.
	 *
	 * @param ppanel the tracker panel
	 * @param forceNew true to create a new trackbar if null; false to return null
	 * @return a TTrackBar
	 */
	public TTrackBar getTrackBar(Integer panelID, boolean forceNew) {
		int i = panelID.intValue();
		TTrackBar bar = _atrackbars[i];
		if (bar == null && forceNew) {
				_atrackbars[i] = bar = new TTrackBar(_apanels[i]);
		}
		return bar;
	}

	/**
	 * Gets the toolbar for the specified tracker panel.
	 *
	 * @param ppanel the tracker panel
	 * @param forceNew true to create a new toolbar if null; false to return null
	 * @return a TToolBar
	 */
	public TToolBar getToolBar(Integer panelID, boolean forceNew) {
		int i = panelID.intValue();
		TToolBar bar = _atoolbars[i];
		if (bar == null && forceNew) {
				_atoolbars[i] = new TToolBar(_apanels[i]);
		}
		return bar;
	}

	/**
	 * From TrackPanel.Loader. This will load into the objects[] array for the tab
	 * as soon as it is available.
	 * 
	 * @param trackerPanel
	 * @param toolbar
	 */
	public void setToolBar(TrackerPanel trackerPanel, TToolBar toolbar) {
		int i = trackerPanel.getID();
		TToolBar old = _atoolbars[i];
		if (old != null)
			Disposable.deallocate(old);
		_atoolbars[i] = toolbar;
	}

	/**
	 * Gets the menubar for the specified tracker panel.
	 *
	 * @param ppanel   the tracker panel
	 * @param forceNew true to create a new bar if null; false to return null
	 * @return a TMenuBar
	 */
	public TMenuBar getMenuBar(Integer panelID, boolean forceNew) {
		TrackerPanel panel = getTrackerPanelForID(panelID);
		int i = panelID.intValue();
		TMenuBar bar = _amenubars[i];
		if (bar == null && forceNew) {
			bar = _amenubars[i] = new TMenuBar(panel);
			FontSizer.setFonts(bar);
		}
		return bar;
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
					refreshMenus(panel, TMenuBar.REFRESH_TFRAME_OPENRECENT);
				}
				sayFileNotFound(path);
				return;
			}
		}
		doOpenURL(path);
	}

	/**
	 * Refreshes the GUI.
	 */
	public void refresh() {
		TrackerPanel panel = getSelectedPanel();
		if (panel == null)
			return;
		Integer panelID = panel.getID();
		TMenuBar mb = getMenuBar(panelID, false);
		if (mb != null)
			mb.refresh(TMenuBar.REFRESH_TFRAME_REFRESH);
		TToolBar tb = getToolBar(panelID, false);
		if (tb != null)
			tb.refresh(TToolBar.REFRESH_TFRAME_REFRESH_TRUE);
		TTrackBar rb = getTrackBar(panelID, false);
		if (rb != null)
			rb.refresh();
		TViewChooser[] choosers = getViewChoosers(panel);
		if (choosers != null) {
			for (Container next : choosers) {
				if (next instanceof TViewChooser) {
					TViewChooser chooser = (TViewChooser) next;
					chooser.refreshMenus();
				}
			}
		}
		updateNotesDialog(panel);
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

		for (int i = getTabCount(); --i >= 0;) {
			TrackerPanel trackerPanel = getTrackerPanelForTab(i);
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
		if (notesVisible())
			notes.refreshTextAndFonts();
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

									LibraryResource record = (LibraryResource) e.getNewValue();
									String toCancel = " ["
											+ TrackerRes.getString("TFrame.LibraryBrowser.Message.Cancel") + "]";
									String loading = " " + TrackerRes.getString("Tracker.Splash.Loading") + " \"";
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
									File file = (File) e.getNewValue();
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
			// helpDialog.setContentPane(helpLauncher.getContentPane());
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
	 * Gets the object array for the specified tracker panel.
	 *
	 * @param trackerPanel the tracker panel
	 * @return the object array
	 */
	private Object[] getObjects(TrackerPanel trackerPanel) {
		return getObjects(getTab(trackerPanel.getID()));
	}

	public Object[] getObjects(int tab) {
		return (tab < 0 || tab >= tabbedPane.getTabCount() ? null
				: ((TTabPanel) tabbedPane.getComponentAt(tab)).getObjects());
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

	public boolean getAlwaysListenToClipboard() {
		return alwaysListenToClipboard;
	}

	public void setAlwaysListenToClipboard(boolean b) {
		alwaysListenToClipboard = b;
		checkClipboardListener();
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
					for (int i = getTabCount(); --i >= 0;) {
						TrackerPanel trackerPanel = getTrackerPanelForTab(i);
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
				getNotes().save();
			}
		};
		// now lazy -- createNotesDialog();

		// create the tabbed pane
		tabbedPane = new JTabbedPane(SwingConstants.BOTTOM);
		setContentPane(new JPanel(new BorderLayout()));
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		// create the default menubar
//		TrackerRes.locale = Locale.forLanguageTag("es");
		checkLocale();
		setJMenuBar(defaultMenuBar = new DefaultMenuBar());
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
				hideNotes();
				doCloseAction(getSelectedPanel());
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

	protected Notes getNotes() {
		if (notes == null)
			notes = new Notes();
		return notes;
	}

	protected void doTabStateChanged() {
		Object[] objects = getObjects(tabbedPane.getSelectedIndex());
		MainTView mainView = (objects == null ? null : (MainTView) objects[TFRAME_MAINVIEW]);
		TrackerPanel newPanel = (mainView == null ? null : mainView.getTrackerPanel());
		if (mainView == null && objects != null)
			return;
		TrackerPanel oldPanel = (newPanel != null && prevPanelID == newPanel.panelID ? newPanel : deactivateOldTrackerPanel(prevPanelID));
		System.out.println("TFrame.doTabStateChanged state=" + state + " " + oldPanel + "----->" + newPanel);
		// refresh current tab items
		if (objects == null) {
			// show defaultMenuBar
			setJMenuBar(defaultMenuBar);
		} else if (mainView != null && newPanel != null) {
			prevPanelID = newPanel.getID();
			// update prefsDialog
			if (prefsDialog != null) {
				prefsDialog.panelID = newPanel.getID();
			}
			
			if (oldPanel != null)
				oldPanel.isNotesVisible = notesVisible();
			if (notes != null)
				notes.dialog.setVisible(newPanel.isNotesVisible);
			// refresh the notes dialog and button
			updateNotesDialog(newPanel);
			
			Integer panelID = newPanel.getID();
			TToolBar bar = getToolBar(panelID, true);
			if (bar != null) {
				bar.notesButton.setSelected(notesVisible());
			}
			// refresh trackbar
			TTrackBar tbar = getTrackBar(panelID, true);
			if (tbar != null)
				tbar.refresh();
			// refresh and replace menu bar
			TMenuBar menubar = getMenuBar(panelID, true);
			if (menubar != null)
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
		if (oldPanel != newPanel)
			firePropertyChange(PROPERTY_TFRAME_TAB, oldPanel, newPanel); // $NON-NLS-1$
		// BH added 2020.11.24
		clearHoldPainting();
		repaintT(newPanel);
	}

	private TrackerPanel deactivateOldTrackerPanel(Integer panelID) {
		TrackerPanel oldPanel = getTrackerPanelForID(panelID);

		// BH: This next sounds like a good idea to me, some reason to have have this?
		
//		// hide exportZipDialog
//		if (haveExportDialog && ExportVideoDialog.videoExporter != null) {
//			ExportVideoDialog.videoExporter.trackerPanel = null;
//		}
//		if (haveThumbnailDialog && ThumbnailDialog.thumbnailDialog != null) {
//			ThumbnailDialog.thumbnailDialog.trackerPanel = null;
//		}
		// update prefsDialog
		if (prefsDialog != null) {
			prefsDialog.panelID = null;
		}
		// clean up items associated with old panel
		// BH: I don't think we would have two different frames here, would we?
		if (playerBar != null) {
			Container frame = playerBar.getTopLevelAncestor();
			if (frame != null && frame != this)
				frame.setVisible(false);
		}
		if (oldPanel != null) {
			if (oldPanel.dataBuilder != null) {
				boolean vis = oldPanel.dataToolVisible;
				oldPanel.dataBuilder.setVisible(false);
				oldPanel.dataToolVisible = vis;
			}
			if (oldPanel.getPlayer() != null) {
				VideoClip clip = oldPanel.getPlayer().getVideoClip();
				ClipInspector ci = (clip == null ? null : clip.getClipInspector());
				if (ci != null)
					ci.setVisible(false);
			}
			Video vid = oldPanel.getVideo();
			if (vid != null) {
				vid.getFilterStack().setInspectorsVisible(false);
			}
		}
		return oldPanel;
	}

	protected void frameResized() {
		TrackerPanel trackerPanel = getSelectedPanel();
		if (!isLayoutAdaptive || trackerPanel == null)
			return;
		Rectangle rect = getBounds();
		isLayoutChanged = isPortraitOrientation != (rect.height > rect.width);
		if (maximizedView != TView.VIEW_UNSET) {
			maximizeView(trackerPanel, maximizedView);
			trackerPanel.dividerLocs = null;
			return;
		}
		if (isLayoutChanged) {
			// determine if dimensions are portrait or landscape and arrange views
			isPortraitOrientation = !isPortraitOrientation;
			for (int i = getTabCount(); --i >= 0;) { // bh rev order
				trackerPanel = getTrackerPanelForTab(i);
				boolean defaultViewsVisible = areViewsVisible(DEFAULT_VIEWS, trackerPanel);
				boolean moreViewsVisible = areViewsVisible(OTHER_VIEWS, trackerPanel);
				arrangeViews(trackerPanel, defaultViewsVisible, moreViewsVisible);
			}
			isLayoutChanged = false;
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
		TMenuBar mbar = getMenuBar(trackerPanel.getID(), false);
		if (mbar != null)
			mbar.setAllowRefresh(false);
		trackerPanel.initialize(fileDropHandler);

		// set divider locations
		// validate in advance of setting divider locations
		// to ensure dividers are set properly
		validate();
		boolean portrait = isPortraitLayout();
		if (trackerPanel.dividerLocs == null) {
			setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, portrait ? 1.0 : DEFAULT_MAIN_DIVIDER);
			setDividerLocation(trackerPanel, SPLIT_MAIN_RIGHT, 1.0);
			setDividerLocation(trackerPanel, SPLIT_PLOT_TABLE, DEFAULT_RIGHT_DIVIDER);
			setDividerLocation(trackerPanel, SPLIT_MAIN_BOTTOM, portrait ? DEFAULT_LEFT_DIVIDER : 1.0);
//			setDividerLocation(trackerPanel, SPLIT_BOTTOM, 1.0); // becomes previous
			setDividerLocation(trackerPanel, SPLIT_WORLD_PAGE, DEFAULT_BOTTOM_DIVIDER);
		} else {
			int w = 0;
			int[] order = portrait ? PORTRAIT_DIVIDER_ORDER : DEFAULT_ORDER;
			for (int i = 0; i < order.length; i++) {
				JSplitPane pane = getSplitPane(trackerPanel, i);
				if (i == SPLIT_MAIN_RIGHT)
					w = pane.getMaximumDividerLocation();
				int max = i == SPLIT_WORLD_PAGE ? w : pane.getMaximumDividerLocation();
				double loc = trackerPanel.dividerLocs[order[i]];
				loc = getConvertedDividerLoc(i, loc);
				pane.setDividerLocation((int) (loc * max));
			}
			trackerPanel.dividerLocs = null;
		}
		validate(); // after setting divider locations
		trackerPanel.initialize(null);
		mbar = getMenuBar(trackerPanel.getID(), false);
		if (mbar != null)
			mbar.setAllowRefresh(true);
//		saveCurrentDividerLocations(trackerPanel);
	}

	protected static boolean isPortraitLayout() {
		return isLayoutAdaptive && isPortraitOrientation;
	}

	/**
	 * Converts and returns converted divider location (0.0 <= loc <= 1.0). No
	 * conversion is made if not portrait layout.
	 * 
	 * @param splitPaneIndex 0-3
	 * @param loc            the divider loc
	 * @return the converted divider loc
	 */
	protected double getConvertedDividerLoc(int splitPaneIndex, double loc) {
		if (isPortraitLayout())
			switch (splitPaneIndex) {
			case SPLIT_MAIN_RIGHT:
				return loc > 0.92 ? 1.0 : DEFAULT_MAIN_DIVIDER;
			case SPLIT_PLOT_TABLE:
				return loc > 0.92 ? 1.0 : loc < 0.08 ? 0.0 : DEFAULT_RIGHT_DIVIDER;
			case SPLIT_MAIN_BOTTOM:
				return loc > 0.92 ? 1.0 : DEFAULT_LEFT_DIVIDER;
			case SPLIT_WORLD_PAGE:
				return loc > 0.92 ? 1.0 : loc < 0.08 ? 0.0 : DEFAULT_BOTTOM_DIVIDER;
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
		// System.out.println("TFrame.paintHold " + paintHold);
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

		return isVisible() && paintHold == 0 && !getIgnoreRepaint();
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
	 * THIS WAS A MEMORY LEAK. It is the responsibility of the follower to detach
	 * itself when appropriate.
	 * 
	 * @param c   the component
	 * @param pt0 the initial location of this frame
	 * 
	 */
	public ComponentListener addFollower(Component c, Point ignored) {
		Point pt0 = getLocation();
		ComponentListener listener = new ComponentAdapter() {
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
		};
		addComponentListener(listener);
		return listener;
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
	 * @return true if there is at least one tab and it is not changed and it has
	 *         the default title
	 */
	boolean haveContent() {
		return (getTabCount() > 0 && (getTrackerPanelForTab(0).changed
				|| !tabbedPane.getTitleAt(0).equals(TrackerRes.getString("TrackerPanel.NewTab.Name")))); //$NON-NLS-1$
	}

	/**
	 * @return the tab number to remove if the specified TrackerPanel is present and
	 *         clean, or -1 if not
	 */
	int getRemovableTabNumber(Integer panelID) {
		int tab = getTab(panelID);
		boolean clean = tab > -1 && !getTrackerPanelForID(panelID).changed
				&& tabbedPane.getTitleAt(tab).equals(TrackerRes.getString("TrackerPanel.NewTab.Name")); //$NON-NLS-1$
		return clean ? tab : -1;
	}

	/**
	 * Returns a clean TrackerPanel.
	 *
	 * @return a clean TrackerPanel.
	 */
	synchronized TrackerPanel getCleanTrackerPanel() {
		TrackerPanel panel;
// BH bad idea -- tab is flushed; no need for this.
//		if (getTabCount() == 0 || haveContent() || !OSPRuntime.isJS) {
			panel = new TrackerPanel(this);
//		} else {
//			panel = getTrackerPanelForTab(0);
//			JSplitPane[] panes = getSplitPanes(panel);
//			setDefaultWeights(panes);
//		}
		return panel;
	}

	/**
	 * Remove the first tab if it is empty and there are at least n tabs (1 or 2)
	 */
	public void removeEmptyTabIfTabCountGreaterThan(int n) {
		if (getTabCount() > n && !haveContent())
			removeTabNow(0);
	}

	public void removeTabNow(int i) {
		TrackerPanel tp = getTrackerPanelForTab(i);
		if (tp != null)
			removeTabSynchronously(tp);// new TabRemover(tp).executeSynchronously();
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
		if (path != null && !path.startsWith("http")) { // assume path is relative to html page
			path = "https://./" + path;
//			String base=OSPRuntime.getDocbase();;
//			path=base+path;
			OSPLog.fine("Loading Tracker experiment path=" + path);
		}
		if (path == null && (path = GUIUtils.showInputDialog(this, "Load Experiment", "Load Experiment",
				JOptionPane.QUESTION_MESSAGE, lastExperiment)) == null)
			return;
		if (TrackerIO.isVideo(new File(path))) {
			loadVideo(path, false, null, null); // imports video into current tab
			return;
		}
		if (getTabCount() > 0)
			removeAllTabs(false);
		try {
			doOpenURL(path);
		} catch (Throwable t) {
			removeAllTabs(false);
		}
	}

	protected void loadLibraryRecord(LibraryResource record) {
		openLibraryResource(record, () -> {
			Integer panelID = getSelectedPanelID();

			OSPRuntime.trigger(200, (ev) -> {
				libraryBrowser.doneLoading();
				requestFocus();
				if (panelID != null) {
					TrackerPanel panel = getTrackerPanelForID(panelID);
					panel.changed = false;
					repaintT(panel);
					if (panel.openedFromPath != null)
						Tracker.addRecent(panel.openedFromPath, false);
				}
			});
		});
	}

	private Integer getSelectedPanelID() {
		TrackerPanel panel = getSelectedPanel();
		return (panel == null ? null : panel.getID());
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
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (path == null)
			return;
		loadedFiles.remove(path);
		TrackerIO.openFileFromDialog(new File(path), this, () -> {
			// open the TR Z in the Library Browser
			setCursor(Cursor.getDefaultCursor());
			libraryBrowser.open(path);
			libraryBrowser.setVisible(true);
			OSPRuntime.trigger(1000, (e) -> {
				LibraryTreePanel treePanel = libraryBrowser.getSelectedTreePanel();
				if (treePanel != null) {
					treePanel.refreshSelectedNode();
				}
			});
		});
	}

	public void doOpenFileFromDialog() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		TrackerIO.openFileFromDialog(null, this, () -> {
			setCursor(Cursor.getDefaultCursor());
		});
		if (!OSPRuntime.isJS)
			setCursor(Cursor.getDefaultCursor());
	}

	public void doOpenURL(String url) {
		TrackerPanel selected = getSelectedPanel();
		if (selected != null) {
			selected.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		TrackerIO.openURL(url, this, () -> {
			// MEMORY LEAK - selected may have been destroyed!
			setCursor(Cursor.getDefaultCursor());
			TrackerPanel panel = getSelectedPanel();
			if (panel != null) {
				panel.setMouseCursor(Cursor.getDefaultCursor());
			}
		});
	}

	/**
	 * Loads (imports) a video file or image stack into a tab after caching its
	 * contents locally. Tab may be a new tab or the currently selected tab.
	 * 
	 * @param path     path to the video
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
			// save tabs with a defined data file or openedFromPath, and unchanged videos
			// save both relative paths (relative to tabsetFile) and absolute paths
			String relativeTo = frame.tabsetFile != null ? XML.getDirectoryPath(XML.getAbsolutePath(frame.tabsetFile))
					: XML.getUserDirectory();
			relativeTo = XML.forwardSlash(relativeTo);
			ArrayList<String[]> pathList = new ArrayList<String[]>();
			for (int i = 0; i < frame.getTabCount(); i++) {
				TrackerPanel trackerPanel = frame.getTrackerPanelForTab(i);
				File file = trackerPanel.getDataFile();
				if (trackerPanel.openedFromPath != null)
					file = new File(trackerPanel.openedFromPath);
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
				if (res != null) {
					if (!videoFilter.accept(file)) {
						if (dataFile == null)
							dataFile = file;
					}
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
	 * An empty JDialog that serves as a modal blocker when the progress monitor is
	 * visible.
	 */
	private Object frameBlocker;

	public void setFrameBlocker(boolean blocking, TrackerPanel panel) {
		getJMenuBar().setEnabled(!blocking);
		tabbedPane.setEnabled(!blocking);
		getContentPane().setVisible(!blocking);
		state = (blocking ? STATE_BLOCKED : STATE_ACTIVE);
		if (blocking) {
			frameBlocker = new Object();
			if (notesVisible())
				setNotesVisible(false);
			panel = getSelectedPanel();
			if (panel != null)
				panel.onBlocked();
		} else if (frameBlocker != null) {
			frameBlocker = null;
			if (panel != null) // null for file not found
				panel.onLoaded();
		}
	}

	public void setNotesVisible(boolean b) {
		notes.setVisible(b);
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
	
	public class DefaultMenuBar extends DeactivatingMenuBar {
		DefaultMenuBar() {
			int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			// file menu
			JMenu fileMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.File")); //$NON-NLS-1$
			add(fileMenu);
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
			// if (!OSPRuntime.isApplet) {
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
					checkMemTest();
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
			// }
			// edit menu
			JMenu editMenu = new JMenu(TrackerRes.getString("TMenuBar.Menu.Edit")); //$NON-NLS-1$
			add(editMenu);
			// language menu
			JMenu languageMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.Language")); //$NON-NLS-1$
			languageMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					TMenuBar.setLangMenu(languageMenu, TFrame.this);
				}

				@Override
				public void menuDeselected(MenuEvent e) {
				}

				@Override
				public void menuCanceled(MenuEvent e) {
				}

			});
			editMenu.add(languageMenu);
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
			add(TMenuBar.getTrackerHelpMenu(null, null));
		}
		

	}

	protected void checkLocale() {
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

	protected void setLanguage(String language) {
		if (language.equals(currentLangugae))
			return;
		currentLangugae = language;
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


	/**
	 * An inner class for TFrame that handles all notes, including lazy
	 * initialization.
	 * 
	 * @author hansonr
	 *
	 */
	private class Notes {

		private JDialog dialog;
		private JTextPane textPane;
		private JButton cancelDialogButton, closeDialogButton;
		private JCheckBox displayWhenLoadedCheckbox;
		private int thisFontLevel;
		private Integer panelID;

		private Notes() {
			createNotesGUI();
		}

		private void dispose() {
			WindowListener[] a = dialog.getWindowListeners();
			for (int i = a.length; --i >= 0;)
				dialog.removeWindowListener(a[i]);
			dialog.setVisible(false);
		}

		private void createNotesGUI() {
			dialog = new JDialog(TFrame.this, false) {

				@Override
				public void setVisible(boolean vis) {
					super.setVisible(vis);

					TrackerPanel panel = getSelectedPanel();
					if (panel != null) {
						TToolBar tbar = getToolBar(panel.getID(), false);
						if (tbar != null)
							tbar.notesButton.setSelected(vis);
					}
				}
			};
			textPane = new JTextPane();
			textPane.setBackground(Color.WHITE);
			textPane.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						String url = e.getURL().toString();
						org.opensourcephysics.desktop.OSPDesktop.displayURL(url);
					}
				}
			});
			textPane.setPreferredSize(new Dimension(420, 200));
			textPane.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					TrackerPanel trackerPanel = getSelectedPanel();
					if (!trackerPanel.isEnabled("notes.edit")) //$NON-NLS-1$
						return;
					textPane.setBackground(YELLOW);
					closeDialogButton.setText(TrackerRes.getString("PrefsDialog.Button.Save")); //$NON-NLS-1$
					cancelDialogButton.setEnabled(true);
				}
			});
			textPane.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if (e.getOppositeComponent() != cancelDialogButton)
						saveNotesAction.actionPerformed(null);
				}
			});
			displayWhenLoadedCheckbox = new JCheckBox(
					TrackerRes.getString("TFrame.NotesDialog.Checkbox.ShowByDefault")); //$NON-NLS-1$
			displayWhenLoadedCheckbox.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TrackerPanel trackerPanel = getSelectedPanel();
					if (trackerPanel != null) {
						trackerPanel.hideDescriptionWhenLoaded = !displayWhenLoadedCheckbox.isSelected();
					}
				}
			});
			JPanel buttonbar = new JPanel(new FlowLayout());
			buttonbar.add(displayWhenLoadedCheckbox);
			buttonbar.add(Box.createHorizontalStrut(50));
			cancelDialogButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
			cancelDialogButton.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.setName("canceled"); //$NON-NLS-1$
					dialog.setVisible(false);
				}
			});
			buttonbar.add(cancelDialogButton);
			closeDialogButton = new JButton(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
			closeDialogButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
			});
			buttonbar.add(closeDialogButton);
			JPanel infoContentPane = new JPanel(new BorderLayout());
			infoContentPane.add(new JScrollPane(textPane), BorderLayout.CENTER);
			infoContentPane.add(buttonbar, BorderLayout.SOUTH);
			dialog.setContentPane(infoContentPane);
			dialog.pack();
		}

		private void save() {
			if (textPane.getBackground() == Color.WHITE)
				return;
			String desc = textPane.getText();
			TrackerPanel trackerPanel = getSelectedPanel();
			if (trackerPanel != null && dialog.getName() != "canceled") { //$NON-NLS-1$
				trackerPanel.changed = true;
				TTrack track = trackerPanel.getTrack(dialog.getName());
				if (track != null && !desc.equals(track.getDescription())) {
					track.setDescription(desc);
				} else if (!desc.equals(trackerPanel.getDescription())) {
					trackerPanel.setDescription(desc);
					trackerPanel.hideDescriptionWhenLoaded = !displayWhenLoadedCheckbox.isSelected();
				}
			}
			textPane.setBackground(Color.WHITE);
			cancelDialogButton.setEnabled(false);
			closeDialogButton.setEnabled(true);
			closeDialogButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		}

		boolean needPosition = true;

		private void setVisible(boolean b) {
			if (b && needPosition) {
				needPosition = false;
				TrackerPanel trackerPanel = getTrackerPanelForID(panelID);
				Point p0 = new JFrame().getLocation();
				if (trackerPanel.infoX != Integer.MIN_VALUE || dialog.getLocation().x == p0.x) {
					int x, y;
					Point p = getLocationOnScreen();
					if (trackerPanel.infoX != Integer.MIN_VALUE) {
						Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
						x = Math.max(p.x + trackerPanel.infoX, 0);
						x = Math.min(x, dim.width - dialog.getWidth());
						y = Math.max(p.y + trackerPanel.infoY, 0);
						y = Math.min(y, dim.height - dialog.getHeight());
						trackerPanel.infoX = Integer.MIN_VALUE;
					} else {
						TToolBar toolbar = getToolBar(panelID, true);
						Point pleft = toolbar.getLocationOnScreen();
						Dimension dim = dialog.getSize();
						Dimension wdim = toolbar.getSize();
						x = pleft.x + (int) (0.5 * (wdim.width - dim.width));
						y = p.y + 16;
					}
					dialog.setLocation(x, y);
				}
				System.out.println("TFrame.notes " + dialog.isVisible());
			}

			dialog.setVisible(b);
		}

		private boolean isVisible() {
			return dialog.isVisible();
		}

		private void updateDialog(TrackerPanel panel) {
			// notesDialog will be present
			textPane.setEditable(panel.isEnabled("notes.edit"));
			saveNotesAction.actionPerformed(null);
			TTrack track = panel.selectedTrack;
			if (track != null) {
				textPane.setText(track.getDescription());
				dialog.setName(track.getName());
				dialog.setTitle(TrackerRes.getString("TActions.Dialog.Description.Title") //$NON-NLS-1$
						+ " \"" + track.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				textPane.setText(panel.getDescription());
				dialog.setName(null);
				String tabName = getTabTitle(getSelectedTab());
				dialog.setTitle(TrackerRes.getString("TActions.Dialog.Description.Title") //$NON-NLS-1$
						+ " \"" + tabName + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			textPane.setBackground(Color.WHITE);
			cancelDialogButton.setEnabled(false);
			closeDialogButton.setEnabled(true);
			// now check selected panel
			panel = getSelectedPanel();
			displayWhenLoadedCheckbox.setEnabled(panel != null);
			if (panel != null) {
				displayWhenLoadedCheckbox.setSelected(!panel.hideDescriptionWhenLoaded);
			}
			refreshTextAndFonts();
		}

		private JDialog getDialog() {
			refreshTextAndFonts();
			return dialog;
		}

		private void refreshTextAndFonts() {
			cancelDialogButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
			closeDialogButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
			displayWhenLoadedCheckbox.setText(TrackerRes.getString("TFrame.NotesDialog.Checkbox.ShowByDefault")); //$NON-NLS-1$
			int level = FontSizer.getLevel();
			if (level != thisFontLevel) {
				thisFontLevel = level;
				FontSizer.setFonts(dialog, level);
				dialog.pack();
			}
		}

		private void setDialog(TrackerPanel panel, WindowListener infoListener) {
			panelID = panel.getID();
			dialog.removeWindowListener(infoListener);
			dialog.addWindowListener(infoListener);
			TrackerPanel trackerPanel = getSelectedPanel();
			// position info dialog if first time shown
			// or if trackerPanel specifies location
			setVisible(true);
			updateNotesDialog(trackerPanel);
		}

	}

	/**
	 * Check if the notesDialog has been created and is visible
	 * 
	 * @return true only if the notesDialog is not null and is visible
	 */
	boolean notesVisible() {
		return (notes != null && notes.isVisible());
	}

	/**
	 * Updates the TFrame info dialog if visible.
	 * 
	 * @param panel the referring panel, not necessary the selected panel
	 * 
	 */
	void updateNotesDialog(TrackerPanel panel) {
		if (panel != null && notesVisible())
			notes.updateDialog(panel);
	}

	JDialog getNotesDialog() {
		return (notes == null ? notes = new Notes() : notes).getDialog();
	}

	void setNotesDialog(TrackerPanel trackerPanel, WindowListener infoListener) {
		getNotes().setDialog(trackerPanel, infoListener);
	}

	public void disposeOf(TrackerPanel trackerPanel) {
		if (prevPanelID == trackerPanel.getID())
			prevPanelID = null;
	}

	/**
	 * Gets the TViewChoosers for the specified tracker panel.
	 * 
	 * @param trackerPanel the tracker panel
	 * @return array of TViewChooser
	 */
	public TViewChooser[] getViewChoosers(TrackerPanel trackerPanel) {
		Object[] objects = getObjects(trackerPanel);
		return (objects == null ? new TViewChooser[4] : (TViewChooser[]) objects[TFRAME_VIEWCHOOSERS]);
	}

	public TViewChooser[] getViewChoosers(Integer panelID) {
		Object[] objects = getObjects(getTab(panelID));
		return (objects == null ? new TViewChooser[4] : (TViewChooser[]) objects[TFRAME_VIEWCHOOSERS]);
	}

	public TViewChooser[] getVisibleChoosers(Integer panelID) {
		TViewChooser[] choosers = getViewChoosers(getTrackerPanelForID(panelID));
		TViewChooser[] ret = new TViewChooser[4];
		for (int i = 0; i < choosers.length; i++) {
			ret[i] = (isViewPaneVisible(i, panelID) ? choosers[i] : null);
		}
		return ret;
	}

	public void removeTabSynchronously(Integer panelID) {
		removeTabSynchronously(getTrackerPanelForID(panelID));
	}

	public void refreshMenus(TrackerPanel trackerPanel, String whereFrom) {
		TMenuBar menubar = getMenuBar(trackerPanel.getID(), false);
		if (menubar != null) {
			menubar.refresh(whereFrom);
		}
	}

	private final BitSet _bsPanelIDs = new BitSet();

	private final static int MAX_PID = 127;

	public Integer allocatePanel(TrackerPanel trackerPanel) {
		int i = _bsPanelIDs.nextClearBit(0);
		if (i > MAX_PID) {
			System.err.println("MAX_PID EXCEEDED");
			// now what?
			throw new ArrayIndexOutOfBoundsException("Too many panels!");
		}
		_bsPanelIDs.set(i);
		_apanels[i] = trackerPanel;
		return Integer.valueOf(i);
	}


	public void deallocatePanelID(Integer panelID) {
		int i = panelID.intValue();
		_apanels[i] = null;
		_bsPanelIDs.clear(i);
	}
	
	public void deallocate(Disposable obj) {
		Disposable.deallocate(obj);
	}

	private TrackerPanel[] _apanels = new TrackerPanel[MAX_PID];
	private TMenuBar[] _amenubars = new TMenuBar[MAX_PID];
	private TTrackBar[] _atrackbars = new TTrackBar[MAX_PID];
	private TToolBar[] _atoolbars = new TToolBar[MAX_PID];
	private Timer memoryTimer;
 
	{
		Disposable.allocate(_apanels, "_apanels");
		Disposable.allocate(_amenubars, "_amenubars");
		Disposable.allocate(_atrackbars, "_atrackbars");
		Disposable.allocate(_atoolbars, "_atoolbars");
	}
	
	public TrackerPanel getTrackerPanelForID(Integer panelID) {
		return (panelID == null ? null : _apanels[panelID.intValue()]);
	}
	
	private static final int MEMORY_TIMER_DELAY_MS = 15000;

	public void startMemoryTimer() {
		if (MEMORY_TIMER_DELAY_MS > 0)
		memoryTimer = new Timer(MEMORY_TIMER_DELAY_MS, (e) -> {
			System.gc();
			TrackerPanel panel = getSelectedPanel();
			if (panel != null)
				TToolBar.refreshMemoryButton(panel);
		});
		memoryTimer.setRepeats(true);
		memoryTimer.start();
	}

	public static void main(String[] args) {

		// The TFrame is necessary for providing access to the panel via a panelID.

		TFrame f = new TFrame();
		TrackerPanel tp = new TrackerPanel(f);
		System.out.println(tp);
		System.out.println(f.getTrackerPanelForID(tp.getID()));
		System.exit(0);
	}

	public void sayFileNotFound(String path) {
		JOptionPane.showMessageDialog(this, TrackerRes.getString("TFrame.Dialog.FileNotFound.Message") //$NON-NLS-1$
				+ "\n" + MediaRes.getString("VideoIO.Dialog.Label.Path") + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ path, TrackerRes.getString("TFrame.Dialog.FileNotFound.Title"), //$NON-NLS-1$
				JOptionPane.WARNING_MESSAGE);
	}


}
