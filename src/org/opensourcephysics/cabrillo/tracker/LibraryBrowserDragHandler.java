package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;
import org.opensourcephysics.tools.LibraryBrowser;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * Installs mouse and touch dragging on the Library Browser dialog/window.
 * 
 * Allows dragging the dialog by dragging anywhere inside the dialog (including
 * empty dialog frame, welcome message pane, and empty tree background), while
 * preserving interactive controls (buttons, editable text fields, scrollbars, and tree
 * node selection / folder expansion).
 * 
 * Also guarantees the Welcome screen content is rendered with proper styling
 * the very first time the Library Browser is displayed and on any subsequent display,
 * in both desktop Java and transpiled SwingJS on iPad.
 */
public class LibraryBrowserDragHandler {

	private static final String DRAG_LISTENER_KEY = "LibraryBrowserDragListener_Installed"; //$NON-NLS-1$
	private static final String WINDOW_LISTENER_KEY = "LibraryBrowser_WindowListener_Attached"; //$NON-NLS-1$

	/**
	 * Opens the Library Browser dialog and guarantees the Welcome screen is displayed.
	 * 
	 * @param frame the Tracker main frame
	 */
	public static void openLibraryBrowser(TFrame frame) {
		if (frame == null) return;
		try {
			LibraryBrowser browser = frame.getLibraryBrowser();
			if (browser != null) {
				browser.setVisible(true);
				ensureWelcomeScreen(browser);
			}
		} catch (Throwable t) {
		}
	}

	/**
	 * Installs the drag handler and welcome screen renderer on the given LibraryBrowser instance.
	 * 
	 * @param browser the LibraryBrowser instance
	 */
	public static void install(final LibraryBrowser browser) {
		if (browser == null) return;

		// Attach to browser and all existing components in the dialog
		attachToHierarchy(browser, browser);

		// Also attach to the top-level window / root pane if available
		attachToWindow(browser);

		// Listen for tab changes
		attachTabListeners(browser, browser);

		// Ensure welcome screen is rendered and formatted immediately
		ensureWelcomeScreen(browser);

		// Listen for added components (e.g. tabs, trees, welcome panes loaded asynchronously)
		browser.addContainerListener(new ContainerAdapter() {
			@Override
			public void componentAdded(ContainerEvent e) {
				attachToHierarchy(e.getChild(), browser);
				ensureWelcomeScreen(browser);
			}
		});

		// Listen for component shown on browser itself
		browser.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				ensureWelcomeScreen(browser);
			}
			@Override
			public void componentResized(ComponentEvent e) {
				ensureWelcomeScreen(browser);
			}
		});

		// Listen for hierarchy changes (when window or panels are added to the display)
		browser.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				attachToHierarchy(browser, browser);
				attachToWindow(browser);
				attachTabListeners(browser, browser);
				ensureWelcomeScreen(browser);
			}
		});

		// Also attach on property changes (e.g. when collections are loaded or tabs change)
		browser.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				attachToHierarchy(browser, browser);
				attachToWindow(browser);
				attachTabListeners(browser, browser);
				ensureWelcomeScreen(browser);
			}
		});
	}

	/**
	 * Finds any JEditorPane in the browser (e.g. htmlAboutPane) and ensures it renders
	 * the welcome message properly in both desktop Java and SwingJS on iPad.
	 * 
	 * @param browser the LibraryBrowser instance
	 */
	public static void ensureWelcomeScreen(final LibraryBrowser browser) {
		if (browser == null) return;

		// If tabs are currently open, do not overwrite tabs with welcome screen
		try {
			if (browser.getTabCount() > 0) {
				return;
			}
		} catch (Throwable t) {
		}

		// Ensure htmlScroller is placed in Center if tabCount == 0
		try {
			java.lang.reflect.Method m = LibraryBrowser.class.getDeclaredMethod("refreshGUI"); //$NON-NLS-1$
			m.setAccessible(true);
			m.invoke(browser);
		} catch (Throwable t) {
		}
		/**
		 * @j2sNative
		 * if (browser && browser.refreshGUI$) browser.refreshGUI$();
		 */
		{
		}

		final JEditorPane ep = findEditorPane(browser);
		if (ep != null) {
			refreshWelcomePane(ep);
		}

		// In SwingJS, also ensure browser outer DOM node is styled and visible
		/**
		 * @j2sNative
		 * try {
		 *   if (browser.ui && browser.ui.domNode) {
		 *     browser.ui.domNode.style.display = "block";
		 *     browser.ui.domNode.style.backgroundColor = "#ffffff";
		 *   }
		 * } catch (ex) {}
		 */
		{
		}

		// Ensure drag adapters and window listeners are attached
		attachToHierarchy(browser, browser);
		attachToWindow(browser);
	}

	/**
	 * Finds the htmlAboutPane in the given LibraryBrowser.
	 * 
	 * @param browser the LibraryBrowser instance
	 * @return the JEditorPane if found, or null
	 */
	public static JEditorPane findEditorPane(final LibraryBrowser browser) {
		if (browser == null) return null;

		// 1. Check direct field in SwingJS transpiled code
		final JEditorPane[] holder = new JEditorPane[1];
		/**
		 * @j2sNative
		 * if (browser && browser.htmlAboutPane) {
		 *   holder[0] = browser.htmlAboutPane;
		 * }
		 */
		{
		}
		if (holder[0] != null) {
			return holder[0];
		}

		// 2. Check direct field via reflection in desktop Java
		try {
			java.lang.reflect.Field f = LibraryBrowser.class.getDeclaredField("htmlAboutPane"); //$NON-NLS-1$
			f.setAccessible(true);
			Object obj = f.get(browser);
			if (obj instanceof JEditorPane) {
				return (JEditorPane) obj;
			}
		} catch (Throwable t) {
		}

		// 3. Search component hierarchy
		return findEditorPaneInHierarchy(browser);
	}

	private static JEditorPane findEditorPaneInHierarchy(Component comp) {
		if (comp instanceof JEditorPane) {
			return (JEditorPane) comp;
		}
		if (comp instanceof Container) {
			Container cont = (Container) comp;
			for (Component child : cont.getComponents()) {
				JEditorPane ep = findEditorPaneInHierarchy(child);
				if (ep != null) return ep;
			}
		}
		return null;
	}

	/**
	 * Refreshes the welcome message pane and guarantees the HTML content is injected
	 * into both the Java JEditorPane document and the SwingJS HTML DOM on iPad.
	 * 
	 * @param ep the JEditorPane instance
	 */
	public static void refreshWelcomePane(final JEditorPane ep) {
		if (ep == null) return;

		final String welcomeHtml = getWelcomeHTML();

		// Configure JEditorPane in desktop Java
		try {
			ep.setContentType("text/html"); //$NON-NLS-1$
			ep.setEditable(false);
			ep.setFocusable(false);
			ep.setOpaque(true);
			ep.setBackground(Color.WHITE);
			ep.setForeground(Color.BLACK);
			ep.setText(welcomeHtml);
			ep.setCaretPosition(0);
			ep.revalidate();
			ep.repaint();
		} catch (Throwable t) {
		}

		// Also schedule on EDT for layout realization
		SwingUtilities.invokeLater(() -> {
			try {
				ep.setText(welcomeHtml);
				ep.setCaretPosition(0);
				ep.revalidate();
				ep.repaint();
			} catch (Throwable t) {
			}
		});

		// In SwingJS on iPad / Safari: directly inject and style the DOM elements
		/**
		 * @j2sNative
		 * try {
		 *   var thePane = (typeof ep != "undefined" && ep != null ? ep : (this.$finals$ && this.$finals$.ep ? this.$finals$.ep : null));
		 *   if (!thePane) return;
		 *   var rawHTML = welcomeHtml;
		 *   
		 *   var injectDOM = function() {
		 *     try {
		 *       var ui = (thePane.ui || (thePane.getUI ? thePane.getUI() : null));
		 *       if (ui) {
		 *         ui.mytext = null;
		 *         ui.rawHTML = null;
		 *         ui.currentHTML = null;
		 *         if (ui.setText$S) {
		 *           try { ui.setText$S(rawHTML); } catch(e) {}
		 *         }
		 *         if (ui.domNode) {
		 *           ui.domNode.style.width = "100%";
		 *           ui.domNode.style.height = "100%";
		 *           ui.domNode.style.minHeight = "350px";
		 *           ui.domNode.style.overflow = "auto";
		 *           ui.domNode.style.display = "block";
		 *           ui.domNode.style.backgroundColor = "#ffffff";
		 *           ui.domNode.style.webkitOverflowScrolling = "touch";
		 *           ui.domNode.style.boxSizing = "border-box";
		 *           
		 *           var target = ui.bodyNode || ui.domNode;
		 *           target.style.width = "100%";
		 *           target.style.backgroundColor = "#ffffff";
		 *           target.style.color = "#000000";
		 *           target.style.display = "block";
		 *           target.style.boxSizing = "border-box";
		 *           target.style.padding = "20px";
		 *           target.style.fontFamily = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif";
		 *           
		 *           if (!target.innerHTML || target.innerHTML.indexOf("Open Source Physics") < 0) {
		 *             target.innerHTML = rawHTML;
		 *           }
		 *         }
		 *         if (ui.domNode && ui.domNode.parentElement) {
		 *           ui.domNode.parentElement.style.width = "100%";
		 *           ui.domNode.parentElement.style.height = "100%";
		 *           ui.domNode.parentElement.style.overflow = "auto";
		 *           ui.domNode.parentElement.style.backgroundColor = "#ffffff";
		 *         }
		 *         if (ui.domNode && ui.domNode.parentElement && ui.domNode.parentElement.parentElement) {
		 *           ui.domNode.parentElement.parentElement.style.width = "100%";
		 *           ui.domNode.parentElement.parentElement.style.height = "100%";
		 *           ui.domNode.parentElement.parentElement.style.backgroundColor = "#ffffff";
		 *         }
		 *       }
		 *     } catch (e) {}
		 *   };
		 *   
		 *   injectDOM();
		 *   if (window.requestAnimationFrame) {
		 *     window.requestAnimationFrame(injectDOM);
		 *   }
		 *   setTimeout(injectDOM, 30);
		 *   setTimeout(injectDOM, 100);
		 *   setTimeout(injectDOM, 250);
		 *   setTimeout(injectDOM, 500);
		 *   setTimeout(injectDOM, 1000);
		 * } catch (ex) {}
		 */
		{
		}
	}

	private static String getWelcomeHTML() {
		String bannerUrl = null;
		try {
			Resource res = ResourceLoader.getResource("/org/opensourcephysics/resources/tools/images/compadre_banner.jpg"); //$NON-NLS-1$
			if (res != null && res.getURL() != null) {
				bannerUrl = res.getURL().toString();
			}
		} catch (Throwable t) {
		}
		if (bannerUrl == null) {
			bannerUrl = "https://opensourcephysics.github.io/tracker-website/images/compadre_banner.jpg"; //$NON-NLS-1$
		}

		return "<html><head><style>" //$NON-NLS-1$
				+ "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #222222; background-color: #ffffff; padding: 15px; font-size: 14px; line-height: 1.6; }\n" //$NON-NLS-1$
				+ "h1 { color: #003366; font-size: 20px; text-align: center; margin-top: 10px; margin-bottom: 16px; font-weight: bold; }\n" //$NON-NLS-1$
				+ "p { color: #333333; margin-bottom: 12px; font-size: 14px; }\n" //$NON-NLS-1$
				+ "ul { margin-left: 24px; margin-bottom: 15px; padding-left: 0; }\n" //$NON-NLS-1$
				+ "li { color: #333333; margin-bottom: 6px; font-size: 14px; }\n" //$NON-NLS-1$
				+ "a { color: #0066cc; text-decoration: underline; }\n" //$NON-NLS-1$
				+ "</style></head><body>" //$NON-NLS-1$
				+ "<div style=\"max-width: 800px; margin: 0 auto; color: #222222;\">" //$NON-NLS-1$
				+ "<div style=\"text-align: center; margin-bottom: 18px;\"><img src=\"" + bannerUrl + "\" alt=\"ComPADRE Banner\" style=\"max-width: 100%; height: auto; border-radius: 4px;\" onerror=\"this.style.display='none'\"></div>" //$NON-NLS-1$ //$NON-NLS-2$
				+ "<h1 style=\"color: #003366; font-size: 20px; text-align: center; margin-top: 10px; margin-bottom: 16px; font-weight: bold;\">Open Source Physics Library Browser</h1>" //$NON-NLS-1$
				+ "<p style=\"color: #333333; margin-bottom: 12px; font-size: 14px;\">Use the OSP Library Browser to browse online collections of Tracker projects, EJS simulations and other learning resources.</p>" //$NON-NLS-1$
				+ "<ul style=\"margin-left: 24px; margin-bottom: 15px;\">" //$NON-NLS-1$
				+ "<li style=\"color: #333333; margin-bottom: 6px; font-size: 14px;\">Open a collection by choosing from the <strong>Collections</strong> menu or entering a URL directly in the toolbar as with a web browser.</li>" //$NON-NLS-1$
				+ "<li style=\"color: #333333; margin-bottom: 6px; font-size: 14px;\">Collections are organized and displayed in a tree. Each tree node is a resource or sub-collection. Click a node to learn about the resource or double-click to download and/or open it in Tracker, EJS, DataTool or your web browser.</li>" //$NON-NLS-1$
				+ "<li style=\"color: #333333; margin-bottom: 6px; font-size: 14px;\">To build your own collection choose <strong>File | New Collection</strong>. Add your own resources or copy and paste from other collections. Collections are saved as xml documents that contain references to the actual resource files. For more information, choose Help.</li>" //$NON-NLS-1$
				+ "</ul>" //$NON-NLS-1$
				+ "<p style=\"color: #333333; margin-bottom: 12px; font-size: 14px;\"><strong>ComPADRE</strong> is a network of online resource collections and community web sites supporting physics education with content, tools, and expert advice. Open a ComPADRE collection by choosing from the <strong>Collections | ComPADRE Library</strong> menu.</p>" //$NON-NLS-1$
				+ "<p style=\"color: #333333; margin-bottom: 12px; font-size: 14px;\">You can help build the ComPADRE collection by reviewing resources, participating in discussions, and adding your own OSP resources. For more information, see <a href=\"https://www.compadre.org/osp/\" target=\"_blank\" style=\"color: #0066cc; text-decoration: underline;\">https://www.compadre.org/osp/</a>. " //$NON-NLS-1$
				+ "To recommend a resource for ComPADRE, visit <a href=\"https://www.compadre.org/osp/items/suggest.cfm\" target=\"_blank\" style=\"color: #0066cc; text-decoration: underline;\">Suggest a Resource</a>. Contact Wolfgang Christian, the OSP Collection editor, for more information.</p>" //$NON-NLS-1$
				+ "</div></body></html>"; //$NON-NLS-1$
	}

	/**
	 * Attaches drag listeners and window listeners to the window, root pane, and layered pane.
	 */
	private static void attachToWindow(final LibraryBrowser browser) {
		if (browser == null) return;
		Window w = SwingUtilities.getWindowAncestor(browser);
		if (w == null && browser.getTopLevelAncestor() instanceof Window) {
			w = (Window) browser.getTopLevelAncestor();
		}
		final Window[] wHolder = new Window[] { w };
		/**
		 * @j2sNative
		 * if (!wHolder[0] && org.opensourcephysics.tools.LibraryBrowser.frame) {
		 *   wHolder[0] = org.opensourcephysics.tools.LibraryBrowser.frame;
		 * }
		 */
		{
		}
		w = wHolder[0];

		if (w != null) {
			attachDragAdapter(w, browser);
			if (w instanceof RootPaneContainer) {
				JRootPane rp = ((RootPaneContainer) w).getRootPane();
				if (rp != null) {
					if (rp.getClientProperty(WINDOW_LISTENER_KEY) != null) {
						return; // already attached
					}
					rp.putClientProperty(WINDOW_LISTENER_KEY, Boolean.TRUE);

					attachDragAdapter(rp, browser);
					if (rp.getLayeredPane() != null) {
						attachDragAdapter(rp.getLayeredPane(), browser);
					}
					if (rp.getContentPane() != null) {
						attachDragAdapter(rp.getContentPane(), browser);
					}
				}
			}

			w.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent e) {
					ensureWelcomeScreen(browser);
				}
				@Override
				public void componentResized(ComponentEvent e) {
					ensureWelcomeScreen(browser);
				}
			});

			w.addWindowListener(new WindowAdapter() {
				@Override
				public void windowOpened(WindowEvent e) {
					ensureWelcomeScreen(browser);
				}
				@Override
				public void windowActivated(WindowEvent e) {
					ensureWelcomeScreen(browser);
				}
			});
		}
	}

	private static void attachTabListeners(Container root, final LibraryBrowser browser) {
		if (root == null) return;
		if (root instanceof JTabbedPane) {
			final JTabbedPane tabbedPane = (JTabbedPane) root;
			if (tabbedPane.getClientProperty(DRAG_LISTENER_KEY) == null) {
				tabbedPane.putClientProperty(DRAG_LISTENER_KEY, Boolean.TRUE);
				tabbedPane.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						attachToHierarchy(tabbedPane.getSelectedComponent(), browser);
					}
				});
				tabbedPane.addContainerListener(new ContainerAdapter() {
					@Override
					public void componentAdded(ContainerEvent e) {
						attachToHierarchy(e.getChild(), browser);
					}
				});
			}
		}
		for (Component child : root.getComponents()) {
			if (child instanceof Container) {
				attachTabListeners((Container) child, browser);
			}
		}
	}

	/**
	 * Recursively finds components in the container and attaches the drag adapter to them.
	 */
	public static void attachToHierarchy(Component comp) {
		attachToHierarchy(comp, null);
	}

	public static void attachToHierarchy(Component comp, LibraryBrowser browser) {
		if (comp == null) return;

		// Attach to this component if eligible
		attachDragAdapter(comp, browser);

		if (comp instanceof JScrollPane) {
			JScrollPane sp = (JScrollPane) comp;
			if (sp.getViewport() != null) {
				attachDragAdapter(sp.getViewport(), browser);
				if (sp.getViewport().getView() != null) {
					attachToHierarchy(sp.getViewport().getView(), browser);
				}
			}
		}

		if (comp instanceof Container) {
			Container cont = (Container) comp;
			for (Component child : cont.getComponents()) {
				attachToHierarchy(child, browser);
			}
		}
	}

	/**
	 * Determines whether a component should receive the window drag listener.
	 * Interactive components (buttons, editable text fields, dropdowns, scrollbars)
	 * are excluded so their primary interactions are preserved.
	 */
	private static boolean isEligibleForDrag(Component comp) {
		if (comp == null) return false;
		// Skip buttons and menu items so clicking them still functions
		if (comp instanceof AbstractButton) return false;
		// Skip combo boxes and scroll bars
		if (comp instanceof JComboBox) return false;
		if (comp instanceof JScrollBar) return false;
		// Skip editable text fields (allow user to type in search/command fields)
		if (comp instanceof JTextComponent && ((JTextComponent) comp).isEditable()) {
			return false;
		}
		// Skip split pane divider so column resizing still works
		if (comp.getClass().getName().contains("Divider")) return false;

		return true;
	}

	private static void attachDragAdapter(final Component comp, final LibraryBrowser browser) {
		if (!isEligibleForDrag(comp)) return;

		if (comp instanceof JComponent) {
			JComponent jc = (JComponent) comp;
			if (jc.getClientProperty(DRAG_LISTENER_KEY) != null) {
				return; // already attached
			}
			jc.putClientProperty(DRAG_LISTENER_KEY, Boolean.TRUE);
		}

		DragMouseAdapter adapter = new DragMouseAdapter(comp, browser);
		comp.addMouseListener(adapter);
		comp.addMouseMotionListener(adapter);
	}

	/**
	 * MouseAdapter that implements dragging of the top-level window when pressing
	 * on empty space of the dialog, tree, or viewports.
	 */
	private static class DragMouseAdapter extends MouseAdapter {
		private final Component component;
		private final LibraryBrowser browser;
		private final Point mouseLoc = new Point();
		private final Point windowLoc = new Point();
		private boolean isDragging = false;

		public DragMouseAdapter(Component comp, LibraryBrowser browser) {
			this.component = comp;
			this.browser = browser;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			isDragging = false;
			if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
				return;
			}

			// If event is on a JTree, hit-test to see if user clicked a node
			if (component instanceof JTree) {
				JTree tree = (JTree) component;
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path != null) {
					return; // Clicked on a node -> normal tree selection / open
				}
				int row = tree.getClosestRowForLocation(e.getX(), e.getY());
				if (row >= 0) {
					Rectangle bounds = tree.getRowBounds(row);
					if (bounds != null && e.getY() >= bounds.y && e.getY() <= bounds.y + bounds.height
							&& e.getX() <= bounds.x + bounds.width) {
						return; // Clicked near row expander handle or node text
					}
				}
			}

			Window w = SwingUtilities.getWindowAncestor(component);
			if (w == null && browser != null) {
				w = SwingUtilities.getWindowAncestor(browser);
			}
			if (w == null && browser != null && browser.getTopLevelAncestor() instanceof Window) {
				w = (Window) browser.getTopLevelAncestor();
			}
			final Window[] wHolder = new Window[] { w };
			/**
			 * @j2sNative
			 * if (!wHolder[0] && org.opensourcephysics.tools.LibraryBrowser.frame) {
			 *   wHolder[0] = org.opensourcephysics.tools.LibraryBrowser.frame;
			 * }
			 */
			{
			}
			w = wHolder[0];

			if (w != null) {
				Point startPt = getScreenLocation(e, component);
				if (startPt != null) {
					mouseLoc.setLocation(startPt);
					windowLoc.setLocation(w.getLocation());
					isDragging = true;
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (!isDragging) return;
			Window w = SwingUtilities.getWindowAncestor(component);
			if (w == null && browser != null) {
				w = SwingUtilities.getWindowAncestor(browser);
			}
			if (w == null && browser != null && browser.getTopLevelAncestor() instanceof Window) {
				w = (Window) browser.getTopLevelAncestor();
			}
			final Window[] wHolder = new Window[] { w };
			/**
			 * @j2sNative
			 * if (!wHolder[0] && org.opensourcephysics.tools.LibraryBrowser.frame) {
			 *   wHolder[0] = org.opensourcephysics.tools.LibraryBrowser.frame;
			 * }
			 */
			{
			}
			w = wHolder[0];

			if (w != null) {
				Point curPt = getScreenLocation(e, component);
				if (curPt != null) {
					int dx = curPt.x - mouseLoc.x;
					int dy = curPt.y - mouseLoc.y;
					w.setLocation(windowLoc.x + dx, windowLoc.y + dy);
					w.repaint();
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			isDragging = false;
		}
	}

	/**
	 * Gets the screen or page location of a mouse event, compatible with desktop Java
	 * and touch events in SwingJS on iPad.
	 */
	private static Point getScreenLocation(MouseEvent e, Component comp) {
		int[] pt = new int[2];
		boolean found = false;
		/**
		 * @j2sNative
		 * try {
		 *   var je = (e && e.bdata ? e.bdata.jqevent : null);
		 *   if (je) {
		 *     var oe = je.originalEvent || je;
		 *     var t = (oe.touches && oe.touches.length > 0 ? oe.touches[0] : 
		 *             (oe.changedTouches && oe.changedTouches.length > 0 ? oe.changedTouches[0] : 
		 *             (oe.targetTouches && oe.targetTouches.length > 0 ? oe.targetTouches[0] : null)));
		 *     var px = (t ? t.pageX : (je.pageX != null ? je.pageX : null));
		 *     var py = (t ? t.pageY : (je.pageY != null ? je.pageY : null));
		 *     if (px == null && window.J2S && J2S._mousePageX != null) {
		 *       px = J2S._mousePageX;
		 *       py = J2S._mousePageY;
		 *     }
		 *     if (px != null && isFinite(px) && py != null && isFinite(py)) {
		 *       pt[0] = Math.round(px);
		 *       pt[1] = Math.round(py);
		 *       found = true;
		 *     }
		 *   }
		 * } catch (ex) {}
		 */
		{
		}
		if (found) {
			return new Point(pt[0], pt[1]);
		}
		try {
			Point p = e.getLocationOnScreen();
			if (p != null && (p.x != 0 || p.y != 0)) {
				return p;
			}
		} catch (Throwable t) {
		}
		if (comp != null && comp.isShowing()) {
			try {
				Point p = comp.getLocationOnScreen();
				return new Point(p.x + e.getX(), p.y + e.getY());
			} catch (Throwable t) {
			}
		}
		return e.getPoint();
	}
}
