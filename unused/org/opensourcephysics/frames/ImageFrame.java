/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.GUIUtils;
import org.opensourcephysics.display.MeasuredImage;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.PrintUtils;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.SnapshotTool;

/**
 * ImageFrame 
*/
public class ImageFrame extends OSPFrame {
  protected BufferedImage image;
  protected JMenu fileMenu, editMenu, saveImageMenu;
  protected JMenuItem copyItem, printItem, exitItem;
  protected JMenuItem epsItem, gifItem, jpgItem, pngItem;
  protected DrawingPanel drawingPanel;
  protected final static int MENU_SHORTCUT_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  protected String chooserTitle;

  /**
   * Constructs a ImageFrame with the given measured image.
   * @param measuredImage
   */
  public ImageFrame(MeasuredImage measuredImage) {
    drawingPanel = new DrawingPanel();
    setContentPane(drawingPanel);
    drawingPanel.addDrawable(measuredImage);
    drawingPanel.setPreferredMinMax(measuredImage.getXMin(), measuredImage.getXMax(), measuredImage.getYMin(), measuredImage.getYMax());
    image = measuredImage.getImage();
    int w = image.getWidth();
    int h = image.getHeight();
    // need to add 1 to preferred size in each dimension--but why??? 
    drawingPanel.setPreferredSize(new Dimension(w+1, h+1));
    createMenuBar();
    pack();
    chooserTitle = DisplayRes.getString("GUIUtils.Title.SaveImage"); //$NON-NLS-1$
  }

  /**
   * Creates a standard DrawingFrame menu bar and adds it to the frame.
   */
  private void createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    // file menu
    fileMenu = new JMenu(DisplayRes.getString("DrawingFrame.File_menu_item"));     //$NON-NLS-1$
    printItem = new JMenuItem(DisplayRes.getString("ImageFrame.Print_menu_item")); //$NON-NLS-1$
    printItem.setAccelerator(KeyStroke.getKeyStroke('P', MENU_SHORTCUT_KEY_MASK));
    if(!org.opensourcephysics.js.JSUtil.isJS) printItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PrintUtils.printComponent(drawingPanel);
      }

    });
    // save image items    
    saveImageMenu = new JMenu(DisplayRes.getString("ImageFrame.SaveAs_menu_item")); //$NON-NLS-1$
    epsItem = new JMenuItem(DisplayRes.getString("DrawingFrame.EPS_menu_item"));    //$NON-NLS-1$
    gifItem = new JMenuItem(DisplayRes.getString("DrawingFrame.GIF_menu_item"));    //$NON-NLS-1$
    jpgItem = new JMenuItem(DisplayRes.getString("DrawingFrame.JPEG_menu_item"));   //$NON-NLS-1$
    pngItem = new JMenuItem(DisplayRes.getString("DrawingFrame.PNG_menu_item"));    //$NON-NLS-1$
    saveImageMenu.add(epsItem);
    saveImageMenu.add(gifItem);
    saveImageMenu.add(jpgItem);
    saveImageMenu.add(pngItem);
    epsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String description = DisplayRes.getString("ImageFrame.EPS_filter_description");   //$NON-NLS-1$
        String[] extensions = new String[] {"eps", "EPS"};                                //$NON-NLS-1$ //$NON-NLS-2$
        GUIUtils.saveImageAs(drawingPanel, "eps", chooserTitle, description, extensions); //$NON-NLS-1$
      }

    });
    gifItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String description = DisplayRes.getString("ImageFrame.GIF_filter_description");   //$NON-NLS-1$
        String[] extensions = new String[] {"gif", "GIF"};                                //$NON-NLS-1$ //$NON-NLS-2$
        GUIUtils.saveImageAs(drawingPanel, "gif", chooserTitle, description, extensions); //$NON-NLS-1$
      }

    });
    jpgItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String description = DisplayRes.getString("ImageFrame.JPEG_filter_description"); //$NON-NLS-1$
        String[] extensions = new String[] {"jpg", "jpeg", "JPG", "JPEG"};               //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        GUIUtils.saveImageAs(drawingPanel, "jpeg", chooserTitle, description, extensions); //$NON-NLS-1$
      }

    });
    pngItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String description = DisplayRes.getString("ImageFrame.PNG_filter_description");   //$NON-NLS-1$
        String[] extensions = new String[] {"png", "PNG"};                                //$NON-NLS-1$ //$NON-NLS-2$
        GUIUtils.saveImageAs(drawingPanel, "png", chooserTitle, description, extensions); //$NON-NLS-1$
      }

    });
    if(OSPRuntime.applet==null) {
      if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(saveImageMenu);
      fileMenu.addSeparator();
      if(!org.opensourcephysics.js.JSUtil.isJS)fileMenu.add(printItem);
    }
    menuBar.add(fileMenu);
    // edit menu
    editMenu = new JMenu(DisplayRes.getString("DrawingFrame.Edit_menu_title")); //$NON-NLS-1$
    menuBar.add(editMenu);
    copyItem = new JMenuItem(DisplayRes.getString("DrawingFrame.Copy_menu_item")); //$NON-NLS-1$
    copyItem.setAccelerator(KeyStroke.getKeyStroke('C', MENU_SHORTCUT_KEY_MASK));
    copyItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BufferedImage bi = new BufferedImage(drawingPanel.getWidth(), drawingPanel.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = bi.getGraphics();
        drawingPanel.paint(g);
        g.dispose();
        SnapshotTool.getTool().copyImage(bi);
      }

    });
    editMenu.add(copyItem);
    setJMenuBar(menuBar);
    // display menu
    loadDisplayMenu();
    // help menu
    JMenu helpMenu = new JMenu(DisplayRes.getString("DrawingFrame.Help_menu_item")); //$NON-NLS-1$
    menuBar.add(helpMenu);
    JMenuItem aboutItem = new JMenuItem(DisplayRes.getString("DrawingFrame.AboutOSP_menu_item")); //$NON-NLS-1$
    aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', MENU_SHORTCUT_KEY_MASK));
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        OSPRuntime.showAboutDialog(ImageFrame.this);
      }

    });
    helpMenu.add(aboutItem);
  }

  /**
   * Adds a Display menu to the menu bar.
   */
  protected JMenu loadDisplayMenu() {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    JMenu displayMenu = new JMenu(DisplayRes.getString("DrawingFrame.Display_menu_title")); //$NON-NLS-1$
    menuBar.add(displayMenu);
    JMenu fontMenu = new JMenu(DisplayRes.getString("DrawingFrame.Font_menu_title")); //$NON-NLS-1$
    displayMenu.add(fontMenu);
    JMenuItem sizeUpItem = new JMenuItem(DisplayRes.getString("DrawingFrame.IncreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeUpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FontSizer.levelUp();
      }

    });
    fontMenu.add(sizeUpItem);
    final JMenuItem sizeDownItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DecreaseFontSize_menu_item")); //$NON-NLS-1$
    sizeDownItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        FontSizer.levelDown();
      }

    });
    fontMenu.add(sizeDownItem);
    fontMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sizeDownItem.setEnabled(FontSizer.getLevel()>0);
      }

    });
    return displayMenu;
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
