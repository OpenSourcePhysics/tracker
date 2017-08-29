/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2017  Douglas Brown
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.opensourcephysics.display.ColorIcon;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.tools.FontSizer;

/**
 * A PencilDrawer draws and manages PencilDrawings for a TrackerPanel.
 *
 * @author Douglas Brown
 */
public class PencilDrawer {
	
  protected static Color[] pencilColors = {Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
		Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.WHITE};
  protected static ResizableIcon[] drawingOnColorIcons = new ResizableIcon[2*pencilColors.length];
  protected static ResizableIcon[] drawingOffIcons = new ResizableIcon[2];
	protected static Cursor[] pencilCursors = new Cursor[pencilColors.length];
  protected static ResizableIcon[] checkboxIcons = new ResizableIcon[2];
  protected static ResizableIcon[] selectedColorIcons = new ResizableIcon[pencilColors.length];
  private static HashMap<TrackerPanel, PencilDrawer> drawers = new HashMap<TrackerPanel, PencilDrawer>();

  static {
    drawingOnColorIcons[0] = new ResizableIcon(Tracker.class.getResource("resources/images/black_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[1] = new ResizableIcon(Tracker.class.getResource("resources/images/red_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[2] = new ResizableIcon(Tracker.class.getResource("resources/images/green_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[3] = new ResizableIcon(Tracker.class.getResource("resources/images/blue_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[4] = new ResizableIcon(Tracker.class.getResource("resources/images/yellow_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[5] = new ResizableIcon(Tracker.class.getResource("resources/images/magenta_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[6] = new ResizableIcon(Tracker.class.getResource("resources/images/cyan_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[7] = new ResizableIcon(Tracker.class.getResource("resources/images/white_pencil.gif")); //$NON-NLS-1$
    drawingOnColorIcons[8] = new ResizableIcon(Tracker.class.getResource("resources/images/black_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOnColorIcons[9] = new ResizableIcon(Tracker.class.getResource("resources/images/red_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOnColorIcons[10] = new ResizableIcon(Tracker.class.getResource("resources/images/green_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOnColorIcons[11] = new ResizableIcon(Tracker.class.getResource("resources/images/blue_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOnColorIcons[12] = new ResizableIcon(Tracker.class.getResource("resources/images/yellow_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOnColorIcons[13] = new ResizableIcon(Tracker.class.getResource("resources/images/magenta_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOnColorIcons[14] = new ResizableIcon(Tracker.class.getResource("resources/images/cyan_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOnColorIcons[15] = new ResizableIcon(Tracker.class.getResource("resources/images/white_pencil_rollover.gif")); //$NON-NLS-1$
    drawingOffIcons[0] = new ResizableIcon(Tracker.class.getResource("resources/images/inactive_pencil.gif")); //$NON-NLS-1$
    drawingOffIcons[1] = new ResizableIcon(Tracker.class.getResource("resources/images/inactive_pencil_rollover.gif")); //$NON-NLS-1$
    checkboxIcons[0] = new ResizableIcon(Tracker.class.getResource("resources/images/box_unchecked.gif")); //$NON-NLS-1$
    checkboxIcons[1] = new ResizableIcon(Tracker.class.getResource("resources/images/box_checked.gif")); //$NON-NLS-1$
    ImageIcon icon = new ImageIcon(
        Tracker.class.getResource("resources/images/black_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[0] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/red_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[1] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/green_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[2] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/blue_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[3] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/yellow_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[4] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/magenta_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[5] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/cyan_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[6] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 
    icon = new ImageIcon(
        Tracker.class.getResource("resources/images/white_pencil_cursor.gif")); //$NON-NLS-1$
    pencilCursors[7] = GUIUtils.createCustomCursor(icon.getImage(), new Point(1, 15), 
    		TrackerRes.getString("PencilDrawer.Cursor.Description"), Cursor.MOVE_CURSOR); //$NON-NLS-1$ 

  	for (int i=0; i<PencilDrawing.pencilColors.length; i++) {
      selectedColorIcons[i] = new ResizableIcon(new ColorIcon(PencilDrawing.pencilColors[i], Color.GRAY, 16, 16));
  	}
  }
  
  protected TrackerPanel trackerPanel;
  protected ArrayList<PencilDrawing> pencilDrawings = new ArrayList<PencilDrawing>();
  protected boolean drawingsVisible = true;
  protected Color pencilColor = Color.BLACK;
  protected DrawingButton pencilButton;
  protected KeyListener keyListener;
  
  /** 
   * Gets the PencilDrawer for a specified TrackerPanel.
   * 
   * @param panel the TrackerPanel
   * @return the PencilDrawer
   */
  public static PencilDrawer getDrawer(TrackerPanel panel) {
  	PencilDrawer drawer = drawers.get(panel);
  	if (drawer==null) {
  		drawer = new PencilDrawer(panel);
  		drawers.put(panel, drawer);
  	}
  	return drawer;
  }

  /** 
   * Determines if a TrackerPanel is actively drawing.
   * 
   * @param panel the TrackerPanel
   * @return true if drawing
   */
  public static boolean isDrawing(TrackerPanel panel) {
  	PencilDrawer drawer = drawers.get(panel);
  	if (drawer!=null) {
	  	DrawingButton button = drawer.pencilButton;
	  	return button!=null && button.isSelected();
  	}
  	return false;
  }

  /** 
   * Determines if any drawings exist for a given TrackerPanel.
   * 
   * @param panel the TrackerPanel
   * @return true if drawings exist
   */
  public static boolean hasDrawings(TrackerPanel panel) {
  	PencilDrawer drawer = drawers.get(panel);
  	return drawer!=null && !drawer.pencilDrawings.isEmpty();
  }

  /**
   * Constructs a PencilDrawer.
   * 
   * @param panel a TrackerPanel
   */
	PencilDrawer(TrackerPanel panel) {
		trackerPanel = panel;
    keyListener = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	if (pencilButton==null || !pencilButton.isDisplayable()) return;
      	
        if (e.getKeyCode() == KeyEvent.VK_D) {
    			getPencilButton().setSelected(true);
  	      if (!drawingsVisible) {
          	setDrawingsVisible(true);        	
  	      }
          trackerPanel.setMouseCursor(getPencilCursor());
      		if (Tracker.showHints) {
      			trackerPanel.setMessage(TrackerRes.getString("PencilDrawer.Hint")); //$NON-NLS-1$
      		}
        }
      }
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_D) {
    			getPencilButton().setSelected(false);
    			trackerPanel.setMouseCursor(Cursor.getDefaultCursor());
    			trackerPanel.setMessage(null);
        	boolean marking = trackerPanel.setCursorForMarking(trackerPanel.isShiftKeyDown, e);
        	TTrack selectedTrack = trackerPanel.getSelectedTrack();
          if (selectedTrack!=null && marking!=selectedTrack.isMarking) {
          	selectedTrack.setMarking(marking);
          }
        }
      }
    };
	}

  /**
   * Sets the visibility of all drawings.
   * 
   * @param vis true to show all drawings
   */
	public void setDrawingsVisible(boolean vis) {
		drawingsVisible = vis;
		for (PencilDrawing drawing: pencilDrawings) {
			drawing.visible = vis;
		}
		trackerPanel.repaint();
	}
	
  /**
   * Creates a new drawing on this panel.
   * 
   * @return the new drawing
   */
	public PencilDrawing createPencilDrawing() {
		return addPencilDrawing(new PencilDrawing(pencilColor));
	}
	
  /**
   * Adds a drawing to this panel.
   * 
   * @param drawing the drawing to add
   * @return the added drawing
   */
	public PencilDrawing addPencilDrawing(PencilDrawing drawing) {
		drawing.visible = drawingsVisible;
		pencilDrawings.add(drawing);
		trackerPanel.addDrawable(drawing);
		trackerPanel.changed = true;
		return drawing;
	}
	
  /**
   * Gets the most recently added drawing. May return null.
   * 
   * @return the last drawing
   */
	public PencilDrawing getLastPencilDrawing() {
		if (pencilDrawings.isEmpty()) return null;
		return pencilDrawings.get(pencilDrawings.size()-1);
	}
	
  /**
   * Removes the most recently added drawing.
   * 
   * @return the removed drawing
   */
	public PencilDrawing removeLastPencilDrawing() {
		PencilDrawing drawing = getLastPencilDrawing();
		if (drawing!=null) {
			pencilDrawings.remove(drawing);
			trackerPanel.removeDrawable(drawing);
			trackerPanel.changed = true;
		}
		return drawing;
	}
	
  /**
   * Clears all pencil drawings.
   */
	public void clearPencilDrawings() {
		for (PencilDrawing drawing: pencilDrawings) {
			trackerPanel.removeDrawable(drawing);
		}
		pencilDrawings.clear();
		trackerPanel.changed = true;
	}
	
  /**
   * Gets a button to control this PencilDrawer.
   */
	public DrawingButton getPencilButton() {
		if (pencilButton==null) {
			pencilButton = new DrawingButton();
			trackerPanel.addKeyListener(keyListener);
		}
		return pencilButton;
	}
	
  /**
   * Gets a suitable pencil cursor for drawing.
   */
  public Cursor getPencilCursor() {
    for (int i=0; i<pencilColors.length; i++) {
    	if (pencilColor.equals(pencilColors[i])) {
    		return pencilCursors[i];
    	}
    }
  	return pencilCursors[0];
  }
  
  /**
   * Handles the drawing mouse actions.
   *
   * @param e the mouse event
   */
  public void handleMouseAction(MouseEvent e) {
  	
    switch(trackerPanel.getMouseAction()) {

      case InteractivePanel.MOUSE_MOVED:
        trackerPanel.setMouseCursor(getPencilCursor());
    		if (Tracker.showHints) {
    			trackerPanel.setMessage(TrackerRes.getString("PencilDrawer.Hint")); //$NON-NLS-1$
    		}
      	break;
        	
      case InteractivePanel.MOUSE_PRESSED:
       	PencilDrawing drawing = createPencilDrawing();
      	drawing.addPoint(trackerPanel.getMouseX(), trackerPanel.getMouseY());
        trackerPanel.setMouseCursor(getPencilCursor());
    		if (Tracker.showHints) {
    			trackerPanel.setMessage(TrackerRes.getString("PencilDrawer.Hint")); //$NON-NLS-1$
    		}
        break;

      case InteractivePanel.MOUSE_DRAGGED:
      	drawing = getLastPencilDrawing();
      	if (drawing==null) break;
      	drawing.addPoint(trackerPanel.getMouseX(), trackerPanel.getMouseY());
      	trackerPanel.repaint();
        trackerPanel.setMouseCursor(getPencilCursor());
        break;

      case InteractivePanel.MOUSE_RELEASED:
      	drawing = getLastPencilDrawing();
      	if (drawing!=null && drawing.getNumberOfPoints()<=1) {
      		removeLastPencilDrawing();
      		trackerPanel.repaint();
      	}
        trackerPanel.setMouseCursor(getPencilCursor());
     }
  }
  
  
  /**
   * A button to manage the pencil drawing process.
   */
  protected class DrawingButton extends TButton 
  		implements ActionListener {
  	
  	boolean showPopup; 	
    JPopupMenu popup = new JPopupMenu();
    JMenuItem drawingVisibleCheckbox, clearDrawingsItem, hidePencilItem;
    JMenu drawingColorMenu;
    
    /**
     * Constructor.
     */
    private DrawingButton() {
    	super(drawingOffIcons[0]);
      setRolloverIcon(drawingOffIcons[1]);
      
      addActionListener(this);

      drawingVisibleCheckbox = new JMenuItem();
      drawingVisibleCheckbox.setSelected(drawingsVisible);
      drawingVisibleCheckbox.addActionListener(this);
      
      clearDrawingsItem = new JMenuItem();
      clearDrawingsItem.addActionListener(this);
      
      hidePencilItem = new JMenuItem();
      hidePencilItem.addActionListener(this);
      
      drawingColorMenu = new JMenu();
      final AbstractAction colorAction = new AbstractAction() {
  			@Override
  			public void actionPerformed(ActionEvent e) {
  				JMenuItem item = (JMenuItem)e.getSource();
  				int i = Integer.parseInt(item.getActionCommand());
  				pencilColor = PencilDrawing.pencilColors[i];
  				setSelected(true);
  	      setDrawingsVisible(true);        	
  				refresh();
      		if (Tracker.showHints) {
      			trackerPanel.setMessage(TrackerRes.getString("PencilDrawer.Hint")); //$NON-NLS-1$
      		}
      		trackerPanel.getZoomBox().hide();
          trackerPanel.setMouseCursor(getPencilCursor());
  			}    	
      };      
    	for (int i=0; i<PencilDrawing.pencilColors.length; i++) {
        JMenuItem item = new JMenuItem(new ResizableIcon(new ColorIcon(PencilDrawing.pencilColors[i], Color.GRAY, 24, 16)));
        item.setActionCommand(String.valueOf(i));
        item.addActionListener(colorAction);
        item.setSelected(true);
        drawingColorMenu.add(item);      		
    	}
      
      // mouse listener to distinguish between popup and tool visibility actions
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
        	int w = drawingOffIcons[0].getIconWidth();
        	int dw = getWidth()-w;
        	// show popup if right side of button clicked
        	showPopup = e.getX()>(w*18/28 + dw/2);
        }
      });
    }

  	
    /**
     * Overrides TButton method.
     *
     * @return the popup, or null if the right side of this button was clicked
     */
    protected JPopupMenu getPopup() {
    	if (!showPopup)	return null;
    	return getPopup(true);
    }
    
    /**
     * Overrides TButton method.
     *
     * @return the popup, or null if the right side of this button was clicked
     */
    protected JPopupMenu getPopup(boolean forButton) {
    	refresh();
      // rebuild popup menu
    	popup.removeAll();    	
    	popup.add(drawingColorMenu);
    	if (forButton) {
	    	popup.addSeparator();
	    	popup.add(drawingVisibleCheckbox);
    	}
    	popup.addSeparator();
    	popup.add(clearDrawingsItem);
    	clearDrawingsItem.setEnabled(hasDrawings(trackerPanel));
    	if (!forButton) {
	    	popup.addSeparator();
	    	popup.add(hidePencilItem);
    	}
    	FontSizer.setFonts(popup, FontSizer.getLevel());
    	return popup;
    }
    
    /**
     * Responds to action events from both this button and the popup items.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource()==DrawingButton.this) { // button action: activate/deactivate drawing
    		if (showPopup) return;
	      trackerPanel.setSelectedPoint(null);
	      trackerPanel.hideMouseBox();        
	      setSelected(!isSelected());
	      if (!drawingsVisible) {
        	setDrawingsVisible(true);        	
	      }
	      if (isSelected() && Tracker.showHints) {
      		trackerPanel.setMessage(TrackerRes.getString("PencilDrawer.Hint")); //$NON-NLS-1$
	      }
	      else {
	      	trackerPanel.setMessage(null);
	      }
    	}
    	else { // menu item actions
      	trackerPanel.setSelectedPoint(null);
        JMenuItem source = (JMenuItem)e.getSource();
        if (source==drawingVisibleCheckbox) {
        	setDrawingsVisible(!drawingsVisible);        	
        }
        else if (source==clearDrawingsItem) {
        	clearPencilDrawings();
        	trackerPanel.repaint();
        }
        else if (source==hidePencilItem) {
  	      trackerPanel.hideMouseBox();        
  	      setSelected(false);
  	      trackerPanel.setMessage(null);
  	      refresh();
        	trackerPanel.repaint();
        }
        refresh();    		
    	}
    }
    
    /**
     * Refreshes this button.
     */
    void refresh() {
      setToolTipText(TrackerRes.getString("PencilDrawer.Button.Drawings.Tooltip")); //$NON-NLS-1$
      drawingColorMenu.setText(TrackerRes.getString("PencilDrawer.Menu.DrawingColor.Text")); //$NON-NLS-1$
      drawingVisibleCheckbox.setText(TrackerRes.getString("PencilDrawer.MenuItem.DrawingVisible.Text")); //$NON-NLS-1$
      clearDrawingsItem.setText(TrackerRes.getString("PencilDrawer.MenuItem.ClearDrawings.Text")); //$NON-NLS-1$
      hidePencilItem.setText(TrackerRes.getString("PencilDrawer.MenuItem.HidePencil.Text")); //$NON-NLS-1$
      drawingVisibleCheckbox.setIcon(drawingsVisible? checkboxIcons[1]: checkboxIcons[0]);
      // set color icons
      for (int i=0; i<PencilDrawing.pencilColors.length; i++) {
      	if (pencilColor.equals(PencilDrawing.pencilColors[i])) {
      		drawingOnColorIcons[i].resize(FontSizer.getIntegerFactor());
      		drawingOnColorIcons[8+i].resize(FontSizer.getIntegerFactor());
      		selectedColorIcons[i].resize(FontSizer.getIntegerFactor());
      		setSelectedIcon(drawingOnColorIcons[i]);
          setRolloverSelectedIcon(drawingOnColorIcons[8+i]);
          drawingColorMenu.setIcon(selectedColorIcons[i]);
      		break;
      	}
      }
  		if (!drawingsVisible) {
	      setSelected(false);
  		}
    }
    
  }
}
