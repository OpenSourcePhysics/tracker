package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;
import org.opensourcephysics.tools.LibraryBrowser;

/**
 * Installs mouse and touch dragging on the Library Browser dialog/window when the user
 * drags in the resource tree on the left-hand side, except when clicking/selecting
 * a node or expand handle.
 * 
 * Works in both desktop Java and transpiled SwingJS on iPad.
 */
public class LibraryBrowserDragHandler {

	private static final String DRAG_LISTENER_KEY = "LibraryBrowserDragListener_Installed"; //$NON-NLS-1$

	/**
	 * Installs the drag handler on the given LibraryBrowser instance.
	 * 
	 * @param browser the LibraryBrowser instance
	 */
	public static void install(final LibraryBrowser browser) {
		if (browser == null) return;

		// Attach to any existing components
		attachToHierarchy(browser);

		// Listen for added components (e.g. tabs, trees loaded asynchronously)
		browser.addContainerListener(new ContainerAdapter() {
			@Override
			public void componentAdded(ContainerEvent e) {
				attachToHierarchy(e.getChild());
			}
		});

		// Listen for tab changes
		attachTabListeners(browser);

		// Listen for hierarchy changes (when window or panels are added to the display)
		browser.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				attachToHierarchy(browser);
				attachTabListeners(browser);
			}
		});

		// Also attach on property changes (e.g. when collections are loaded or tabs change)
		browser.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				attachToHierarchy(browser);
				attachTabListeners(browser);
			}
		});
	}

	private static void attachTabListeners(Container root) {
		if (root == null) return;
		if (root instanceof JTabbedPane) {
			final JTabbedPane tabbedPane = (JTabbedPane) root;
			if (tabbedPane.getClientProperty(DRAG_LISTENER_KEY) == null) {
				tabbedPane.putClientProperty(DRAG_LISTENER_KEY, Boolean.TRUE);
				tabbedPane.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						attachToHierarchy(tabbedPane.getSelectedComponent());
					}
				});
				tabbedPane.addContainerListener(new ContainerAdapter() {
					@Override
					public void componentAdded(ContainerEvent e) {
						attachToHierarchy(e.getChild());
					}
				});
			}
		}
		for (Component child : root.getComponents()) {
			if (child instanceof Container) {
				attachTabListeners((Container) child);
			}
		}
	}

	/**
	 * Recursively finds any JTree and JScrollPane / JViewport in the container
	 * and attaches the drag adapter to them.
	 */
	public static void attachToHierarchy(Component comp) {
		if (comp == null) return;

		if (comp instanceof JTree) {
			attachToTree((JTree) comp);
		} else if (comp instanceof JScrollPane) {
			JScrollPane sp = (JScrollPane) comp;
			attachToViewport(sp);
			if (sp.getViewport() != null && sp.getViewport().getView() != null) {
				attachToHierarchy(sp.getViewport().getView());
			}
		}

		if (comp instanceof Container) {
			Container cont = (Container) comp;
			for (Component child : cont.getComponents()) {
				attachToHierarchy(child);
			}
		}
	}

	private static void attachToTree(final JTree tree) {
		if (tree.getClientProperty(DRAG_LISTENER_KEY) != null) {
			return; // already attached
		}
		tree.putClientProperty(DRAG_LISTENER_KEY, Boolean.TRUE);

		MouseAdapter adapter = new DragMouseAdapter(tree);
		tree.addMouseListener(adapter);
		tree.addMouseMotionListener(adapter);
	}

	private static void attachToViewport(final JScrollPane sp) {
		if (sp.getClientProperty(DRAG_LISTENER_KEY) != null) {
			return; // already attached
		}
		sp.putClientProperty(DRAG_LISTENER_KEY, Boolean.TRUE);

		if (sp.getViewport() != null) {
			MouseAdapter adapter = new DragMouseAdapter(sp.getViewport());
			sp.getViewport().addMouseListener(adapter);
			sp.getViewport().addMouseMotionListener(adapter);
		}
	}

	/**
	 * MouseAdapter that implements dragging of the top-level window when pressing
	 * on empty space of the tree or viewport.
	 */
	private static class DragMouseAdapter extends MouseAdapter {
		private final Component component;
		private final Point mouseLoc = new Point();
		private final Point windowLoc = new Point();
		private boolean isDragging = false;

		public DragMouseAdapter(Component comp) {
			this.component = comp;
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
