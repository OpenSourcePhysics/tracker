/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.jibble.epsgraphics.EpsGraphics2D;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.gif.GIFEncoder;

public class GUIUtils {
	
	// colors used to "disable/enable" TitledBorders and other non-Components
  private static Color enabledColor, disabledColor;
  static {
  	enabledColor = UIManager.getColor("Label.foreground"); //$NON-NLS-1$
  	if (enabledColor==null)
  		enabledColor = Color.BLACK;
  	disabledColor = UIManager.getColor("Label.disabledForeground"); //$NON-NLS-1$
  	if (disabledColor==null)
    	disabledColor = UIManager.getColor("Label.disabledText"); //$NON-NLS-1$
  	if (disabledColor==null)
  		disabledColor = Color.LIGHT_GRAY;
  }

  private GUIUtils() {} // prohibits instantiation

  /**
   * Converts TeX-like notation for Greek symbols to unicode characters.
   * @param input
   * @return
   * @deprecated  use TeXParser class.
   */
  public static String parseTeX(String input) {
    return TeXParser.parseTeX(input);
  }

  /**
   * Removes TeX subscripting from the input.
   * @param input
   * @return
   * @deprecated  use TeXParser class.
   */
  public static String removeSubscripting(String input) {
    return TeXParser.removeSubscripting(input);
  }

  /**
   * Finds an instance of a class in the given container.
   *
   * @param container Container
   * @param c Class
   * @return Component
   */
  public static Component findInstance(Container container, Class<?> c) {
    if((container==null)||c.isInstance(container)) {
      return container;
    }
    Component[] components = container.getComponents();
    for(int i = 0, n = components.length; i<n; i++) {
      if(c.isInstance(components[i])) {
        return components[i];
      }
      if(components[i] instanceof Container) {
        Component comp = findInstance((Container) components[i], c);
        if(c.isInstance(comp)) {
          return comp;
        }
      }
    }
    return null;
  }

  /**
   * Shows all drawing and table frames.
   *
   * Usually invoked when a model is initialized but may be invoked at other times
   * to show frames that have been closed.
   */
  public static void showDrawingAndTableFrames() {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(!frames[i].isDisplayable()) {
        continue;
      }
      if((frames[i].getName()!=null)&&(frames[i].getName().indexOf("Tool")>-1)) { //$NON-NLS-1$
        continue;
      }
      if(OSPFrame.class.isInstance(frames[i])) {
        if(DataTableFrame.class.isInstance(frames[i])) {
          ((DataTableFrame) frames[i]).refreshTable();
        }
        frames[i].setVisible(true);
        ((OSPFrame) frames[i]).invalidateImage();                                 // make sure buffers are up to date
        frames[i].repaint();                                                      // repaint if frame is already showing
        frames[i].toFront();
      }
    }
    if((OSPRuntime.applet!=null)) {
      OSPRuntime.applet.getRootPane().repaint();
    }
  }

  /**
   * Renders all OSPFrames whose animated property is true.
   *
   * Usually invoked by an animation thread after every animation step.
   */
  public static void renderAnimatedFrames() {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(!frames[i].isDisplayable()||!OSPFrame.class.isInstance(frames[i])) {
        continue;
      }
      if(((OSPFrame) frames[i]).isAnimated()) {
        ((OSPFrame) frames[i]).render();
      }
    }
    if((OSPRuntime.applet!=null)&&(OSPRuntime.applet instanceof Renderable)) {
      ((Renderable) OSPRuntime.applet).render();
    }
  }

  /**
   * Repaints all OSPFrames whose animated property is true.
   *
   * Usually invoked by a control's single-step button.
   */
  public static void repaintAnimatedFrames() {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(!frames[i].isDisplayable()||!OSPFrame.class.isInstance(frames[i])) {
        continue;
      }
      if(((OSPFrame) frames[i]).isAnimated()) {
        ((OSPFrame) frames[i]).invalidateImage(); // make sure buffers are up to date
        ((OSPFrame) frames[i]).repaint();
      }
    }
  }

  /**
   * Repaints all OSPFrames.
   */
  public static void repaintOSPFrames() {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(!frames[i].isVisible()||!frames[i].isDisplayable()||!OSPFrame.class.isInstance(frames[i])) {
        continue;
      }
      ((OSPFrame) frames[i]).repaint();
    }
  }

  /**
   * Clears the data in animated DrawingFrames and repaints the frame's content.
   *
   * All frames are cleared if <code> clearAll<\code> is true; otherwise only frames whose <code>autoClear<\code> flag is
   * true will be cleared.
   *
   * @param clearAll  clears all frames if true
   */
  public static void clearDrawingFrameData(boolean clearAll) {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(!frames[i].isDisplayable()) {
        continue;
      }
      if(OSPFrame.class.isInstance(frames[i])) {
        OSPFrame frame = ((OSPFrame) frames[i]);
        if(clearAll||frame.isAutoclear()) {
          frame.clearDataAndRepaint();
        }
      }
    }
  }

  /**
   * Sets the IgnorRepaint for all animated frames to the given value.
   * @param ignoreRepaint boolean
   */
  public static void setAnimatedFrameIgnoreRepaint(boolean ignoreRepaint) {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(!frames[i].isDisplayable()||!DrawingFrame.class.isInstance(frames[i])) {
        continue;
      }
      if(((DrawingFrame) frames[i]).isAnimated()) {
        DrawingPanel dp = ((DrawingFrame) frames[i]).getDrawingPanel();
        if(dp!=null) {
          dp.setIgnoreRepaint(ignoreRepaint);
        }
      }
    }
  }

  /**
   * Enables and disables the menu bars in DrawingFrames and DrawingFrame3D.
   *
   * Usually invoked when a model is initialized but may be invoked at other times.
   */
  public static void enableMenubars(boolean enable) {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(!frames[i].isDisplayable()) {
        continue;
      }
      if((frames[i].getName()!=null)&&(frames[i].getName().indexOf("Tool")>-1)) {       //$NON-NLS-1$
        continue;
      }
      Class<?> frame3d = null;
      try {
        frame3d = Class.forName("org.opensourcephysics.display3d.core.DrawingFrame3D"); //$NON-NLS-1$
      } catch(ClassNotFoundException ex) {}
      if(DrawingFrame.class.isInstance(frames[i])||((frame3d!=null)&&frame3d.isInstance(frames[i]))) {
        JMenuBar bar = ((JFrame) frames[i]).getJMenuBar();
        if(bar!=null) {
          for(int j = 0, n = bar.getMenuCount(); j<n; j++) {
            bar.getMenu(j).setEnabled(enable);
          }
        }
      }
    }
  }

  /**
   * Disposes all OSP frames except the given frame.
   *
   * Usually invoked when the control window is being closed.
   *
   * @param frame will not be disposed
   */
  public static void closeAndDisposeOSPFrames(Frame frame) {
    Frame[] frames = Frame.getFrames();
    for(int i = 0; i<frames.length; i++) {
      if(frames[i]==frame) {
        continue;
      }
      // if (frames[i] instanceof org.opensourcephysics.controls.Launcher.LauncherFrame)continue;
      if(OSPFrame.class.isInstance(frames[i])) {
        ((OSPFrame) frames[i]).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ((OSPFrame) frames[i]).setVisible(false);
        ((OSPFrame) frames[i]).dispose();
      }
    }
  }

  /**
   *   Pops up a "Save File" file chooser dialog and takes user through process of saving a file.
   *
   *   @param    parent  the parent component of the dialog,
   *                     can be <code>null</code>;
   *                    see <code>showDialog</code> in class JFileChooser for details
   *   @return   the file or null if an error occurred:
   */
  public static File showSaveDialog(Component parent) {
    return showSaveDialog(parent, DisplayRes.getString("GUIUtils.Title.Save")); //$NON-NLS-1$
  }

  /**
   *   Pops up a "Save File" file chooser dialog and takes user through process of saving a file.
   *
   *   @param    parent  the parent component of the dialog,
   *                     can be <code>null</code>;
   *                    see <code>showDialog</code> in class JFileChooser for details
   *   @param    title
   *   @return   the file or null if an error occurred:
   */
  public static File showSaveDialog(Component parent, String title) {
    // JFileChooser fileChooser =  new JFileChooser();
    JFileChooser fileChooser = OSPRuntime.getChooser();
    // fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if(fileChooser==null) {
      return null;
    }
    String oldTitle = fileChooser.getDialogTitle();
    fileChooser.setDialogTitle(title);
    int result = fileChooser.showSaveDialog(parent);
    fileChooser.setDialogTitle(oldTitle);
    if(result!=JFileChooser.APPROVE_OPTION) {
      return null;
    }
    OSPRuntime.chooserDir = fileChooser.getCurrentDirectory().toString();
    File file = fileChooser.getSelectedFile();
    if(file.exists()) {
      int selected = JOptionPane.showConfirmDialog(parent, DisplayRes.getString("DrawingFrame.ReplaceExisting_message")+" "+file.getName()+DisplayRes.getString("DrawingFrame.QuestionMark"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), //$NON-NLS-1$
          JOptionPane.YES_NO_CANCEL_OPTION);
      if(selected!=JOptionPane.YES_OPTION) {
        return null;
      }
    }
    return file;
  }

  public static File showOpenDialog(Component parent) {
    JFileChooser fileChooser = OSPRuntime.getChooser(); // new JFileChooser();
    int result = fileChooser.showOpenDialog(parent);
    if(result!=JFileChooser.APPROVE_OPTION) {
      return null;
    }
    OSPRuntime.chooserDir = fileChooser.getCurrentDirectory().toString();
    File file = fileChooser.getSelectedFile();
    return file;
  }
  
  /**
   * Returns the enabled text color.
   * @return the enabled color
   */
  public static Color getEnabledTextColor() {
  	return enabledColor;
  }

  /**
   * Returns the disabled text color.
   * @return the disabled color
   */
  public static Color getDisabledTextColor() {
  	return disabledColor;
  }

  /**
   * Test the time to render a drawable component.
   * @param drawable
   */
  public static void timingTest(Drawable drawable) {
    DrawingPanel dp = new DrawingPanel();
    DrawingFrame df = new DrawingFrame(dp);
    df.setVisible(true);
    dp.addDrawable(drawable);
    dp.scale();
    dp.setPixelScale();
    Graphics g2 = dp.getGraphics();
    if(g2==null) {
      return;
    }
    long startTime = System.currentTimeMillis();
    drawable.draw(dp, g2);                                                     // first drawing often takes longer because of initialization
    System.out.print("first drawing="+(System.currentTimeMillis()-startTime)); //$NON-NLS-1$
    startTime = System.currentTimeMillis(); // reset the time
    for(int i = 0; i<5; i++) {
      drawable.draw(dp, g2);
    }
    System.out.println("  avg time/drawing="+((System.currentTimeMillis()-startTime)/5)); //$NON-NLS-1$
    g2.dispose();
  }

  /**
   *  Saves the contents of the specified component in the given file format. Note method requires Java 1.4
   *
   * @param  comp
   * @param  outputFile  the output file
   * @param  outputFileFormat output file format. One of eps, gif, jpeg, or png
   */
  public static void saveImage(JComponent comp, File outputFile, String outputFileFormat) throws IOException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(outputFile);
      if(outputFileFormat.equals("eps")) {                                                     //$NON-NLS-1$
        EpsGraphics2D g = new EpsGraphics2D("", fos, 0, 0, comp.getWidth(), comp.getHeight()); //$NON-NLS-1$
        comp.paint(g);
        g.scale(0.24, 0.24);                                                                   // Set resolution to 300 dpi (0.24 = 72/300)
        // g.setColorDepth(EpsGraphics2D.BLACK_AND_WHITE); // Black & white
        g.close();
      } 
      else {
        BufferedImage bi = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics g = bi.getGraphics();
        comp.paint(g);
        g.dispose();
        if (outputFileFormat.equals("gif")) { //$NON-NLS-1$
          GIFEncoder encoder = new GIFEncoder(bi);
          encoder.Write(fos);
        }
        else {
        	ImageIO.write(bi, outputFileFormat, fos);
        }
        fos.close();
      }
    } catch (Exception e) {
			e.printStackTrace();
		} finally {
      if(fos!=null) {
        fos.close();
      }
    }
  }

  /**
   *  Saves the contents of the specified component in the given file format.  Pops
   *  open a save file dialog to allow the user to select the output file. Note method requires Java 1.4
   *
   * @param  component   comp the component
   * @param  ouputFileFormat output file format. One of eps, jpeg, or png
   * @param  parent  dialog parent
   */
  public static void saveImage(JComponent component, String ouputFileFormat, Component parent) {
    File outputFile = GUIUtils.showSaveDialog(component, DisplayRes.getString("GUIUtils.Title.SaveImage")); //$NON-NLS-1$
    if(outputFile==null) {
      return;
    }
    try {
      GUIUtils.saveImage(component, outputFile, ouputFileFormat);
    } catch(IOException ioe) {
      JOptionPane.showMessageDialog(parent, "An error occurred while saving the file "+outputFile.getName()+".'"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
  
  public static void saveImageAs(JComponent component,String type, String title, String description, String[] extensions) {
	    JFileChooser chooser = OSPRuntime.createChooser(title, description, extensions);
	    String fileName = OSPRuntime.chooseFilename(chooser);
	    if(fileName==null) {
	      return;
	    }
	    File file = new File(fileName);
	    String extension = XML.getExtension(fileName);
	    if((extension==null)||".".equals(extension)) {                                                                      //$NON-NLS-1$
	      fileName = XML.stripExtension(fileName)+"."+extensions[0];                                                        //$NON-NLS-1$
	      file = new File(fileName);
	      if(file.exists()) {
	        int selected = JOptionPane.showConfirmDialog(null, DisplayRes.getString("DrawingFrame.ReplaceExisting_message") //$NON-NLS-1$
	          +" "+file.getName()+DisplayRes.getString("DrawingFrame.QuestionMark"), //$NON-NLS-1$ //$NON-NLS-2$
	            DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"),       //$NON-NLS-1$
	              JOptionPane.YES_NO_CANCEL_OPTION);
	        if(selected!=JOptionPane.YES_OPTION) {
	          return;
	        }
	      }
	    }
	    try {
	      GUIUtils.saveImage(component, file, type);
	    } catch(IOException ex) {
	      ex.printStackTrace();
	    }
	  }
  
  /**
   * Creates a custom cursor from an image. If an exception occurs, a predefined cursor is returned.
   *
   * @param image the Image
   * @param hotspot the position of the cursor hotspot
   * @param name the name of the cursor
   * @param predefinedCursorType one of the predefined Cursor types
   */
  public static Cursor createCustomCursor(Image image, Point hotspot, String name, int predefinedCursorType) {
	  try {
			return Toolkit.getDefaultToolkit().createCustomCursor(image, hotspot, name);
		} catch (Exception ex) {
		  try {
				return Cursor.getPredefinedCursor(predefinedCursorType);
			} catch (Exception ex1) {
			}
		}
	  return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
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
