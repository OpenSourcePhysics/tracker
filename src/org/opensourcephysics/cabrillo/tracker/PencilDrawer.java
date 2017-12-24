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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opensourcephysics.display.ColorIcon;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.InteractivePanel;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.VideoClip;
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
  protected PropertyChangeListener stepListener;
  protected ArrayList<PencilScene> scenes = new ArrayList<PencilScene>();
  protected Color pencilColor = Color.BLACK;
  protected DrawingButton pencilButton;
  protected ScenePropertiesDialog scenePropertiesDialog;
  protected KeyListener keyListener;
  protected PencilScene selectedScene;
  protected PencilScene newScene;
  
  /**
   * Constructs a PencilDrawer.
   * 
   * @param panel a TrackerPanel
   */
	private PencilDrawer(TrackerPanel panel) {
		trackerPanel = panel;
		stepListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				selectedScene = getSceneByStartFrame(trackerPanel.getFrameNumber());
			}			
		};
		trackerPanel.addPropertyChangeListener("stepnumber", stepListener); //$NON-NLS-1$
    keyListener = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	if (pencilButton==null || !pencilButton.isDisplayable()) return;
      	
        if (e.getKeyCode() == KeyEvent.VK_D) {
    			getPencilButton().setSelected(true);
  	      setDrawingsVisible(true);        	
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
  	if (drawer==null || drawer.scenes.isEmpty()) return false;
  	for (PencilScene scene: drawer.scenes) {
  		if (!scene.getDrawings().isEmpty()) return true;
  	}
  	return false;
  }

  /** 
   * Disposes the PencilDrawer for a specified TrackerPanel.
   * 
   * @param panel the TrackerPanel
   */
  public static void dispose(TrackerPanel panel) {
  	PencilDrawer drawer = drawers.get(panel);
  	if (drawer!=null) {
  		drawer.dispose();
  		drawers.remove(panel);
  	}
  }

  /** 
   * Determines if drawings (scenes) are visible.
   * 
   * @return true if drawings are visible
   */
  public boolean areDrawingsVisible() {
  	for (PencilScene scene: scenes) {
  		return scene.visible;
  	}
  	return true;
  }

  /**
   * Sets the visibility of all scenes.
   * 
   * @param vis true to show all scenes
   */
	public void setDrawingsVisible(boolean vis) {
		for (PencilScene scene: scenes) {
			scene.setVisible(vis);
		}
		trackerPanel.repaint();
	}
	
  /**
   * Creates a drawing and adds it to the selected scene. If no scene is selected
   * a new one is created;
   * 
   * @return the newly added drawing
   */
	public PencilDrawing addNewDrawingtoSelectedScene() {
		PencilScene scene = getSelectedScene();
		if (scene==null) {
			scene = addNewScene();
			selectedScene = newScene = scene;
		}
		PencilDrawing drawing = new PencilDrawing(pencilColor, scene);
		scene.getDrawings().add(drawing);
		trackerPanel.addDrawable(scene);
		trackerPanel.changed = true;
		return drawing;
	}
	
  /**
   * Creates a drawing and adds it to the selected scene. If no scene is selected
   * a new one is created.
   * 
   * @return the newly added drawing
   */
	public PencilDrawing addDrawingtoSelectedScene(PencilDrawing drawing) {
		PencilScene scene = getSelectedScene();
		if (scene==null) {
			scene = addNewScene();
			selectedScene = scene;
		}
		drawing.setPencilScene(scene);
		scene.getDrawings().add(drawing);
		trackerPanel.addDrawable(scene);
		trackerPanel.changed = true;
		return drawing;
	}
	
  /**
   * Gets the most recently drawn drawing in the selected scene. May return null.
   * 
   * @return the last drawing
   */
	public PencilDrawing getLastDrawingInSelectedScene() {
		PencilScene scene = getSelectedScene();
		if (scene!=null && !scene.getDrawings().isEmpty()) {
			return scene.getDrawings().get(scene.getDrawings().size()-1);
		}
		return null;
	}
	
  /**
   * Erases the most recently drawn drawing in the selected scene.
   */
	public void eraseLastDrawingInSelectedScene() {
		PencilDrawing drawing = getLastDrawingInSelectedScene();
		if (drawing!=null) {
			PencilScene scene = getSelectedScene();
			scene.getDrawings().remove(drawing);
			if (scene.getDrawings().isEmpty()) {
				clearScene(scene);
			}
			trackerPanel.changed = true;
		}
	}
	
  /**
   * Clears all scenes.
   */
	public void clearAllScenes() {
		for (PencilScene scene: scenes) {
			scene.getDrawings().clear();
			trackerPanel.removeDrawable(scene);
		}
		scenes.clear();
		selectedScene = null;
		trackerPanel.changed = true;
	}
	
  /**
   * Clears a scene.
   */
	public void clearScene(PencilScene scene) {
		if (scene==null) return;
		scene.getDrawings().clear();
		trackerPanel.removeDrawable(scene);
		scenes.remove(scene);
		if (scene==selectedScene) {
			selectedScene = null;
		}
		trackerPanel.changed = true;
	}
	
  /**
   * Adds a new empty scene.
   * 
   * @return the new scene
   */
	public PencilScene addNewScene() {
		PencilScene scene = new PencilScene();
		scene.setStartFrame(trackerPanel.getFrameNumber());
		scene.setEndFrame(scene.startframe);
		scenes.add(scene);
		selectedScene = scene;
  	Collections.sort(scenes);
		trackerPanel.addDrawable(scene);
		trackerPanel.changed = true;
		return scene;
	}
	
  /**
   * Replaces all scenes with new ones.
   * 
   * @return the new scene
   */
	public void setScenes(ArrayList<PencilScene> pencilScenes) {
		if (pencilScenes==null || pencilScenes==scenes) return;
		// remove existing scenes
		for (PencilScene scene: scenes) {
			trackerPanel.removeDrawable(scene);
		}
		// add new scenes
		scenes = pencilScenes;
  	Collections.sort(scenes);
		selectedScene = null;
		for (PencilScene scene: scenes) {
  		if (trackerPanel.isDisplayable() && Integer.MAX_VALUE==scene.endframe) {
    	  int last = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
  			scene.endframe = last;
  		}
			trackerPanel.addDrawable(scene);
		}
	}
	
  /**
   * Gets the selected scene. May return null.
   */
	public PencilScene getSelectedScene() {
		if (selectedScene==null && trackerPanel.isDisplayable()) {
			selectedScene = getSceneByStartFrame(trackerPanel.getFrameNumber());
		}
		return selectedScene;
	}
	
  /**
   * Gets the scene with a given start frame. May return null;
   */
	public PencilScene getSceneByStartFrame(int startFrame) {
		for (PencilScene scene: scenes) {
			if (scene.startframe==startFrame) {
				return scene;			
			}
		}
		return null;
	}
	
  /**
   * Gets the scene with a given hash code. May return null.
   */
	public PencilScene getSceneByHashCode(int hash) {
		for (PencilScene scene: scenes) {
			if (scene.hashCode()==hash) {
				return scene;			
			}
		}
		return null;
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
   * Gets the drawing properties dialog.
   */
	public ScenePropertiesDialog getDrawingPropertiesDialog(PencilScene scene) {
		if (scenePropertiesDialog==null) {
			scenePropertiesDialog = new ScenePropertiesDialog();
			// center on screen
	    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (dim.width - scenePropertiesDialog.getBounds().width) / 2;
	    int y = (dim.height - scenePropertiesDialog.getBounds().height) / 2;
	    scenePropertiesDialog.setLocation(x, y);
		}
		scenePropertiesDialog.refresh(scene);
		return scenePropertiesDialog;
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
       	PencilDrawing drawing = addNewDrawingtoSelectedScene();
      	drawing.addPoint(trackerPanel.getMouseX(), trackerPanel.getMouseY());
        trackerPanel.setMouseCursor(getPencilCursor());
    		if (Tracker.showHints) {
    			trackerPanel.setMessage(TrackerRes.getString("PencilDrawer.Hint")); //$NON-NLS-1$
    		}
        break;

      case InteractivePanel.MOUSE_DRAGGED:
      	drawing = getLastDrawingInSelectedScene();
      	if (drawing==null) break;
      	drawing.addPoint(trackerPanel.getMouseX(), trackerPanel.getMouseY());
      	trackerPanel.repaint();
        trackerPanel.setMouseCursor(getPencilCursor());
        break;

      case InteractivePanel.MOUSE_RELEASED:
      	drawing = getLastDrawingInSelectedScene();
      	if (drawing!=null && drawing.getNumberOfPoints()<=1) {
      		eraseLastDrawingInSelectedScene();
      		trackerPanel.repaint();
      	}
      	if (drawing!=null && newScene!=null) {      		
					getDrawingPropertiesDialog(newScene).setVisible(true);
      	}
				newScene = null;
        trackerPanel.setMouseCursor(getPencilCursor());
     }
  }
  
  public void dispose() {
		trackerPanel.removePropertyChangeListener("stepnumber", stepListener); //$NON-NLS-1$
		trackerPanel.removeKeyListener(keyListener);
		scenes.clear();
		if (scenePropertiesDialog!=null) {
			scenePropertiesDialog.dispose();
		}
  	trackerPanel = null;
  	scenePropertiesDialog = null;
		pencilButton = null;
		selectedScene = null;
  }
  /**
   * A button inner class to manage the pencil drawing process.
   */
  protected class DrawingButton extends TButton 
  		implements ActionListener {
  	
  	boolean showPopup; 	
    JPopupMenu popup = new JPopupMenu();
    JMenuItem drawingVisibleCheckbox, clearLastItem, clearAllItem, hidePencilItem;
    JMenu drawingColorMenu, scenesMenu;
    
    /**
     * Constructor.
     */
    private DrawingButton() {
    	super(drawingOffIcons[0]);
      setRolloverIcon(drawingOffIcons[1]);
      
      addActionListener(this);

      drawingVisibleCheckbox = new JMenuItem();
      drawingVisibleCheckbox.addActionListener(this);
      
      clearLastItem = new JMenuItem();
      clearLastItem.addActionListener(this);
      
      clearAllItem = new JMenuItem();
      clearAllItem.addActionListener(this);
      
      hidePencilItem = new JMenuItem();
      hidePencilItem.addActionListener(this);
      
      scenesMenu = new JMenu();
      
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
     * @return the popup menu
     */
    protected JPopupMenu getPopup() {
    	if (!showPopup)	return null;
    	return getPopup(true);
    }
    
    /**
     * Gets the popup menu.
     * 
     * @param forButton true if the popup menu is for the drawing button
     * @return the popup
     */
    protected JPopupMenu getPopup(boolean forButton) {
    	refresh();
      // rebuild popup menu
    	popup.removeAll();    	
    	popup.add(drawingColorMenu);
    	popup.addSeparator();
    	// refresh the scenes menu
    	scenesMenu.removeAll();
    	JMenuItem item = null;
    	JMenu menu = null;
    	VideoClip clip = trackerPanel.getPlayer().getVideoClip();
    	for (PencilScene scene: scenes) {
    		if (trackerPanel.isDisplayable() && Integer.MAX_VALUE==scene.endframe) {
      	  int last = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
    			scene.endframe = last;
    		}
    		menu = new JMenu(TrackerRes.getString("PencilDrawer.Menu.Frames.Text") //$NON-NLS-1$
    				+" "+scene.startframe+"-"+scene.endframe); //$NON-NLS-1$ //$NON-NLS-2$
    		scenesMenu.add(menu);
    		item = new JMenuItem(TrackerRes.getString("PencilDrawer.MenuItem.Properties.Text")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    		item.setActionCommand(String.valueOf(scene.hashCode()));
    		item.addActionListener(this);
    		item.setName("properties"); //$NON-NLS-1$
    		menu.add(item);
    		menu.addSeparator();
    		item = new JMenuItem(TrackerRes.getString("PencilDrawer.MenuItem.Show.Text")); //$NON-NLS-1$ 
    		item.setActionCommand(String.valueOf(scene.hashCode()));
    		item.addActionListener(this);
    		item.setName("show"); //$NON-NLS-1$
    		item.setEnabled(clip.includesFrame(scene.startframe));
    		menu.add(item);
    		menu.addSeparator();
    		item = new JMenuItem(TrackerRes.getString("PencilDrawer.MenuItem.ClearScene.Text")); //$NON-NLS-1$
    		item.setActionCommand(String.valueOf(scene.hashCode()));
    		item.addActionListener(this);
    		item.setName("erase"); //$NON-NLS-1$
    		menu.add(item);
    	}
    	scenesMenu.setEnabled(!scenes.isEmpty());
    	popup.add(scenesMenu);
    	if (forButton) {
	    	popup.addSeparator();
	    	popup.add(drawingVisibleCheckbox);
    	}
    	else {
	    	popup.addSeparator();
	    	popup.add(hidePencilItem);
    	}
    	popup.addSeparator();
    	popup.add(clearLastItem);
    	popup.add(clearAllItem);
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
	      if (!areDrawingsVisible()) {
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
        	setDrawingsVisible(!areDrawingsVisible());        	
        }
        else if (source==clearLastItem) {
        	eraseLastDrawingInSelectedScene();
        	trackerPanel.repaint();
        }
        else if (source==clearAllItem) {
        	clearAllScenes();
        	trackerPanel.repaint();
        }
        else if (source==hidePencilItem) {
  	      trackerPanel.hideMouseBox();        
  	      setSelected(false);
  	      trackerPanel.setMessage(null);
  	      refresh();
        	trackerPanel.repaint();
        }
        else { // scene items
        	try {
						int actionNumber = Integer.valueOf(source.getActionCommand());
						setDrawingsVisible(true);
						if ("show".equals(source.getName())) { //$NON-NLS-1$
							PencilScene scene = getSceneByHashCode(actionNumber);
	        		// set step number to show scene
							int stepNum = trackerPanel.getPlayer().getVideoClip().frameToStep(scene.startframe);
							trackerPanel.getPlayer().setStepNumber(stepNum);
							selectedScene = scene;
						}
						else if ("properties".equals(source.getName())) { //$NON-NLS-1$
							PencilScene scene = getSceneByHashCode(actionNumber);
							getDrawingPropertiesDialog(scene).setVisible(true);
						}
						else if ("erase".equals(source.getName())) { //$NON-NLS-1$
							PencilScene scene = getSceneByHashCode(actionNumber);
							clearScene(scene);
							trackerPanel.repaint();
						}
					} catch (Exception ex) {
					}
        }
        refresh();    		
    	}
    }
    
    /**
     * Refreshes this button.
     */
    void refresh() {
      setToolTipText(TrackerRes.getString("PencilDrawer.Button.Drawings.Tooltip")); //$NON-NLS-1$
      scenesMenu.setText(TrackerRes.getString("PencilDrawer.Menu.Drawing.Text")); //$NON-NLS-1$
      drawingColorMenu.setText(TrackerRes.getString("PencilDrawer.Menu.DrawingColor.Text")); //$NON-NLS-1$
      drawingVisibleCheckbox.setText(TrackerRes.getString("PencilDrawer.MenuItem.DrawingVisible.Text")); //$NON-NLS-1$
      clearLastItem.setText(TrackerRes.getString("PencilDrawer.MenuItem.Undo.Text")); //$NON-NLS-1$
      clearAllItem.setText(TrackerRes.getString("PencilDrawer.MenuItem.ClearAll.Text")); //$NON-NLS-1$
      hidePencilItem.setText(TrackerRes.getString("PencilDrawer.MenuItem.HidePencil.Text")); //$NON-NLS-1$
      drawingVisibleCheckbox.setIcon(areDrawingsVisible()? checkboxIcons[1]: checkboxIcons[0]);
    	clearLastItem.setEnabled(getSelectedScene()!=null);
    	clearAllItem.setEnabled(hasDrawings(trackerPanel));
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
  		if (!areDrawingsVisible()) {
	      setSelected(false);
  		}
    }
    
  }
  
  /**
   * ScenePropertiesDialog inner class
   */
  protected class ScenePropertiesDialog extends JDialog {
  	
    private JLabel startFrameLabel, endFrameLabel;
    private JSpinner startFrameSpinner, endFrameSpinner;
    private TitledBorder title;
    private DrawingPanel canvas;
    private JButton closeButton;
    private PencilScene scene;
  	
  	private ScenePropertiesDialog() {
  		super(JOptionPane.getFrameForComponent(trackerPanel), true);
  		setResizable(false);
  		title = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
  		// create end frame spinners
  		startFrameLabel = new JLabel();
  		startFrameLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
  		endFrameLabel = new JLabel();
  		endFrameLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 2));
  		startFrameSpinner = new JSpinner() {  			
  			@Override
  			public Dimension getMinimumSize() {
  				Dimension dim = super.getMinimumSize();
  				dim.width += (int)(FontSizer.getFactor()*4);
  				return dim;
  			}
  		};
  		endFrameSpinner = new JSpinner() {  			
  			@Override
  			public Dimension getMinimumSize() {
  				Dimension dim = super.getMinimumSize();
  				dim.width += (int)(FontSizer.getFactor()*4);
  				return dim;
  			}
  		};
  		startFrameSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
        	if (scene==null) return;
        	scene.setStartFrame((Integer)startFrameSpinner.getValue());
        	Collections.sort(scenes);
        	trackerPanel.repaint();
        	refresh(scene);
        }
      }); 		
  		endFrameSpinner.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
        	if (scene==null) return;
        	scene.setEndFrame((Integer)endFrameSpinner.getValue());
        	Collections.sort(scenes);
        	trackerPanel.repaint();
        	refresh(scene);
        }
      });
  		
  		canvas = new DrawingPanel();
  		canvas.setAutoscaleX(false);
  		canvas.setAutoscaleY(false);
  		canvas.setSquareAspect(true);
  		canvas.setBackground(Color.WHITE);
  		canvas.setPreferredGutters(20, 20, 20, 20);
  		canvas.setPreferredSize(new Dimension(200, 150));
  		
  		closeButton = new JButton();
  		closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					scenePropertiesDialog.setVisible(false);
				} 			
  		});
  		
  		JPanel contentPane = new JPanel(new BorderLayout());
  		setContentPane(contentPane);
  		contentPane.add(canvas, BorderLayout.NORTH);
  		
  		// range panel shows the range of visible frames
  		JPanel rangePanel = new JPanel();
  		Border outline = BorderFactory.createEtchedBorder();
  		Border combo = BorderFactory.createCompoundBorder(outline, title);
  		rangePanel.setBorder(combo);
  		contentPane.add(rangePanel, BorderLayout.CENTER);
  		rangePanel.add(startFrameLabel);
  		rangePanel.add(startFrameSpinner);
  		rangePanel.add(endFrameLabel);
  		rangePanel.add(endFrameSpinner);
      
  		// button panel
  		JPanel buttonPanel = new JPanel();
  		buttonPanel.setBorder(outline);
  		contentPane.add(buttonPanel, BorderLayout.SOUTH);
  		buttonPanel.add(closeButton);
  		 		
		}
  	
  	private void refresh(PencilScene pencilScene) {
  		canvas.removeDrawable(scene);
  		scene = pencilScene;
  		canvas.addDrawable(scene);
  		canvas.setPreferredMinMaxX(scene.getXMin(), scene.getXMax());
  		canvas.setPreferredMinMaxY(scene.getYMax(), scene.getYMin());
  		canvas.repaint();
  	  int first = trackerPanel.getPlayer().getVideoClip().getFirstFrameNumber();
  	  int last = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
  		SpinnerNumberModel model = new SpinnerNumberModel(scene.startframe, first, last, 1); // init, min, max, step
  		startFrameSpinner.setModel(model);
  		model = new SpinnerNumberModel(scene.endframe, scene.startframe, last, 1); // init, min, max, step
  		endFrameSpinner.setModel(model);
  		setTitle(TrackerRes.getString("PencilDrawer.Dialog.Properties.Title")); //$NON-NLS-1$
  		startFrameLabel.setText(TrackerRes.getString("PencilDrawer.Dialog.Properties.StartFrameLabel.Text")); //$NON-NLS-1$
  		endFrameLabel.setText(TrackerRes.getString("PencilDrawer.Dialog.Properties.EndFrameLabel.Text")); //$NON-NLS-1$
  		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
  		title.setTitle(TrackerRes.getString("PencilDrawer.Dialog.Properties.TitledBorder.Title")); //$NON-NLS-1$
  		pack();
  	}
  }
}
