package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import org.opensourcephysics.display.OSPRuntime;

/**
 * Installs enhanced touch and mouse window resizing on the Tracker main frame
 * and dialog windows, specifically ensuring smooth corner drag-resizing on iPad (iOS Safari)
 * and mobile browsers in SwingJS, while preserving desktop functionality.
 */
public class TWindowResizeHandler {

	private static final String RESIZE_HANDLER_KEY = "TWindowResizeHandler_Installed"; //$NON-NLS-1$
	private static final int MIN_WIDTH = 320;
	private static final int MIN_HEIGHT = 220;

	/**
	 * Installs the window resize handler on the specified window.
	 *
	 * @param window the top-level Window (TFrame or JDialog)
	 */
	public static void install(final Window window) {
		if (window == null) return;

		if (window instanceof RootPaneContainer) {
			JRootPane rp = ((RootPaneContainer) window).getRootPane();
			if (rp != null) {
				if (rp.getClientProperty(RESIZE_HANDLER_KEY) != null) {
					setupResizer(window);
					return; // already installed
				}
				rp.putClientProperty(RESIZE_HANDLER_KEY, Boolean.TRUE);
			}
		}

		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				setupResizer(window);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				setupResizer(window);
			}
		});

		setupResizer(window);

		// In SwingJS, Resizer DOM node may be created asynchronously when window peer finishes initializing
		if (OSPRuntime.isJS) {
			OSPRuntime.trigger(100, (e) -> setupResizer(window));
			OSPRuntime.trigger(500, (e) -> setupResizer(window));
			OSPRuntime.trigger(1500, (e) -> setupResizer(window));
		}
	}

	/**
	 * Configures the SwingJS window resizer DOM element for touch responsiveness and visual affordance.
	 *
	 * @param window the window whose resizer should be configured
	 */
	public static void setupResizer(final Window window) {
		if (window == null) return;
		if (!OSPRuntime.isJS) return;

		/**
		 * @j2sNative
		 * try {
		 *   var frame = window;
		 *   var viewer = (frame.getFrameViewer$ ? frame.getFrameViewer$() : (frame.秘frameViewer || null));
		 *   if (!viewer && window.getRootPane$) {
		 *     var rp = window.getRootPane$();
		 *     viewer = (rp && rp.getFrameViewer$ ? rp.getFrameViewer$() : (rp ? rp.秘frameViewer : null));
		 *   }
		 *   var resizer = (viewer && viewer.getResizer$ ? viewer.getResizer$() : null);
		 *   if (!resizer && viewer && viewer.newResizer) {
		 *     resizer = viewer.newResizer();
		 *   }
		 *   if (resizer && resizer.show$) {
		 *     resizer.show$();
		 *   }
		 *   if (!resizer) return;
		 *   
		 *   var resizerNode = (resizer.getDOMNode$ ? resizer.getDOMNode$() : (resizer.resizer || null));
		 *   if (!resizerNode && resizer.rootPane) {
		 *     var id = resizer.rootPane.秘htmlName + "_resizer";
		 *     resizerNode = document.getElementById(id);
		 *   }
		 *   if (!resizerNode) return;
		 *   
		 *   var rubberBandNode = resizer.rubberBand;
		 *   if (!rubberBandNode && resizer.rootPane) {
		 *     var rbid = resizer.rootPane.秘htmlName + "_resizer_rb";
		 *     rubberBandNode = document.getElementById(rbid);
		 *   }
		 *   
		 *   // Ensure rubberBand does not block touch / pointer interactions
		 *   if (rubberBandNode) {
		 *     rubberBandNode.style.pointerEvents = "none";
		 *   }
		 *   
		 *   // 1. Enlarge touch target to 44x44 points (Apple HIG recommended minimum)
		 *   resizerNode.style.width = "44px";
		 *   resizerNode.style.height = "44px";
		 *   resizerNode.style.marginLeft = "-36px";
		 *   resizerNode.style.marginTop = "-36px";
		 *   resizerNode.style.touchAction = "none";
		 *   resizerNode.style.zIndex = "100002";
		 *   resizerNode.style.userSelect = "none";
		 *   resizerNode.style.webkitUserSelect = "none";
		 *   resizerNode.style.cursor = "nwse-resize";
		 *   
		 *   // 2. Visible diagonal corner grip lines for clear visual feedback on touch screens
		 *   resizerNode.style.opacity = "0.75";
		 *   resizerNode.style.backgroundImage = "linear-gradient(135deg, transparent 0%, transparent 50%, #888888 50%, #888888 56%, transparent 56%, transparent 68%, #888888 68%, #888888 74%, transparent 74%, transparent 86%, #888888 86%, #888888 92%, transparent 92%)";
		 *   resizerNode.style.backgroundRepeat = "no-repeat";
		 *   resizerNode.style.backgroundPosition = "right bottom";
		 *   resizerNode.style.backgroundSize = "22px 22px";
		 *   
		 *   // 3. Attach dedicated touch listeners with passive: false to prevent iOS Safari scrolling
		 *   if (!resizerNode._tTouchAttached) {
		 *     resizerNode._tTouchAttached = true;
		 *     
		 *     resizerNode.addEventListener("touchstart", function(e) {
		 *       if (!e.touches || e.touches.length === 0) return;
		 *       e.preventDefault();
		 *       e.stopPropagation();
		 *       
		 *       var touch0 = e.touches[0];
		 *       var startX = touch0.pageX;
		 *       var startY = touch0.pageY;
		 *       var startW = frame.getWidth$ ? frame.getWidth$() : (frame.width || 800);
		 *       var startH = frame.getHeight$ ? frame.getHeight$() : (frame.height || 600);
		 *       var startLoc = frame.getLocation$ ? frame.getLocation$() : { x: 0, y: 0 };
		 *       
		 *       resizerNode.style.opacity = "1.0";
		 *       if (rubberBandNode) {
		 *         rubberBandNode.style.width = startW + "px";
		 *         rubberBandNode.style.height = startH + "px";
		 *         rubberBandNode.style.display = "block";
		 *         rubberBandNode.style.pointerEvents = "none";
		 *       }
		 *       
		 *       var onTouchMove = function(me) {
		 *         if (!me.touches || me.touches.length === 0) return;
		 *         me.preventDefault();
		 *         me.stopPropagation();
		 *         var t = me.touches[0];
		 *         var dx = t.pageX - startX;
		 *         var dy = t.pageY - startY;
		 *         var curW = Math.max(320, Math.round(startW + dx));
		 *         var curH = Math.max(220, Math.round(startH + dy));
		 *         if (rubberBandNode) {
		 *           rubberBandNode.style.width = curW + "px";
		 *           rubberBandNode.style.height = curH + "px";
		 *         }
		 *         resizerNode.style.left = (curW - 4) + "px";
		 *         resizerNode.style.top = (curH - 4) + "px";
		 *       };
		 *       
		 *       var onTouchEnd = function(ue) {
		 *         ue.preventDefault();
		 *         ue.stopPropagation();
		 *         window.removeEventListener("touchmove", onTouchMove, { passive: false, capture: true });
		 *         window.removeEventListener("touchend", onTouchEnd, { passive: false, capture: true });
		 *         window.removeEventListener("touchcancel", onTouchEnd, { passive: false, capture: true });
		 *         
		 *         resizerNode.style.opacity = "0.75";
		 *         if (rubberBandNode) {
		 *           rubberBandNode.style.display = "none";
		 *         }
		 *         
		 *         var endTouch = (ue.changedTouches && ue.changedTouches.length > 0 ? ue.changedTouches[0] : touch0);
		 *         var dx = endTouch.pageX - startX;
		 *         var dy = endTouch.pageY - startY;
		 *         var finalW = Math.max(320, Math.round(startW + dx));
		 *         var finalH = Math.max(220, Math.round(startH + dy));
		 *         
		 *         if (frame.setSize$I$I) {
		 *           frame.setSize$I$I(finalW, finalH);
		 *         } else if (frame.setBounds$I$I$I$I) {
		 *           frame.setBounds$I$I$I$I(startLoc.x, startLoc.y, finalW, finalH);
		 *         }
		 *         if (frame.setPreferredSize$java_awt_Dimension) {
		 *           frame.setPreferredSize$java_awt_Dimension(Clazz.new_(java.awt.Dimension.c$$I$I, [finalW, finalH]));
		 *         }
		 *         if (frame.validate$) {
		 *           frame.validate$();
		 *         }
		 *         if (frame.repaint$) {
		 *           frame.repaint$();
		 *         }
		 *         if (resizer && resizer.setPosition$I$I) {
		 *           resizer.setPosition$I$I(0, 0);
		 *         } else {
		 *           resizerNode.style.left = (finalW - 4) + "px";
		 *           resizerNode.style.top = (finalH - 4) + "px";
		 *         }
		 *         if (frame.frameResized$) {
		 *           frame.frameResized$();
		 *         }
		 *         if (frame.getSelectedPanel$) {
		 *           var tp = frame.getSelectedPanel$();
		 *           if (tp && org.opensourcephysics.cabrillo.tracker.TFrame && org.opensourcephysics.cabrillo.tracker.TFrame.repaintT) {
		 *             org.opensourcephysics.cabrillo.tracker.TFrame.repaintT(tp);
		 *           }
		 *         }
		 *       };
		 *       
		 *       window.addEventListener("touchmove", onTouchMove, { passive: false, capture: true });
		 *       window.addEventListener("touchend", onTouchEnd, { passive: false, capture: true });
		 *       window.addEventListener("touchcancel", onTouchEnd, { passive: false, capture: true });
		 *     }, { passive: false });
		 *   }
		 * } catch (ex) {}
		 */
		{
		}
	}
}
