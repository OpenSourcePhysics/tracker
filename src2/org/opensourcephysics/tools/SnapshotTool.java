/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jibble.epsgraphics.EpsGraphics2D;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.Renderable;
import org.opensourcephysics.media.gif.GIFEncoder;

/**
 * This provides a simple way to capture screen images.
 *
 * @author Francisco Esquembre (http://fem.um.es)
 * @author Wolfgang Christian
 * @version 1.0
 */
public class SnapshotTool implements Tool {
  // ---- Localization
  static private final String BUNDLE_NAME = "org.opensourcephysics.resources.tools.tools"; //$NON-NLS-1$
  static private ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME);

  static public void setLocale(Locale locale) {
    res = ResourceBundle.getBundle(BUNDLE_NAME, locale);
  }

  static public String getString(String key) {
    try {
      return res.getString(key);
    } catch(MissingResourceException e) {
      return '!'+key+'!';
    }
  }

  /**
   * The singleton shared translator tool.
   */
  private static SnapshotTool TOOL = new SnapshotTool();
  private static JFileChooser chooser;

  /**
   * Gets the shared SnapshotTool.
   *
   * @return the shared SnapshotTool
   */
  public static SnapshotTool getTool() {
    if(TOOL==null) {
      TOOL = new SnapshotTool();
    }
    return TOOL;
  }

  static protected void createChooser() {
    String[] names = javax.imageio.ImageIO.getWriterFormatNames();
    String[] allNames = new String[names.length+2];
    allNames[0] = "gif"; //$NON-NLS-1$
    allNames[1] = "eps"; //$NON-NLS-1$
    for(int i = 0; i<names.length; i++) {
      allNames[i+2] = names[i];
    }
    chooser = OSPRuntime.createChooser(res.getString("SnapshotTool.ImageFiles"), allNames); //$NON-NLS-1$
  }

  /**
   * Private constructor.
   */
  private SnapshotTool() {
    String name = "SnapshotTool"; //$NON-NLS-1$
    createChooser();
    Toolbox.addTool(name, this);
  }

  /**
   * Sends a job to this tool and specifies a tool to reply to.
   *
   * @param job the Job
   * @param replyTo the tool to notify when the job is complete (may be null)
   * @throws RemoteException
   */
  public void send(Job job, Tool replyTo) throws RemoteException {}

  /**
   * Saves the image produced by a component
   * <p>
   * @param filename the name of a file
   * @param component the component to get the image from
   * @return true if the file was correctly saved
   */
  public boolean saveImage(String filename, Component component) {
    return saveImage(filename, component, null);
  }

  /**
   * Saves the image produced by a component to an output stream
   * <p>
   * @param filename the name of a file (the extension indicates the format). If null, teh user will be prompted for a name
   * @param component the component to get the image from
   * @param output An optional output stream to save to. If null the image is saved to a file
   * @return true if the image was correctly saved
   */
  public boolean saveImage(String filename, Component component, java.io.OutputStream output) {
    return saveImage(filename, component, output, 1.0);
  }

  /**
   * Saves the (possibly scaled) image produced by a component to an output stream
   * <p>
   * @param filename the name of a file (the extension indicates the format). If null, the user will be prompted for a name
   * @param component the component to get the image from
   * @param output An optional output stream to save to. If null the image is saved to a file
   * @param scale A scale factor that resizes the image. A value of 1 uses the actual size.
   * @return true if the image was correctly saved
   */
  public boolean saveImage(String filename, Component component, java.io.OutputStream output, double scale) {
    // Generate the image to display in the chooser (will be final if scale = 1)
    if(component==null) {
      return false;
    }
    Component originalComponent = component;
    if(component instanceof JFrame) {
      component = ((JFrame) component).getContentPane();
    } else if(component instanceof JDialog) {
      component = ((JDialog) component).getContentPane();
    }
    BufferedImage bi = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    if(component instanceof Renderable) {
      bi = ((Renderable) component).render(bi);
    } else {
      java.awt.Graphics g = bi.getGraphics();
      component.paint(g);
      g.dispose();
    }
    // we need either an output stream or a file name to save the image.
    if((output==null)&&(filename==null)) {                                 // Select target file and scale
      JLabel label = new JLabel();
      label.setIcon(new ImageIcon(bi));
      /* This code is temporarily disabled because it doesn't work properly for all types of components
      */
      JLabel labelScale = new JLabel(res.getString("SnapshotTool.Scale")); //$NON-NLS-1$
      labelScale.setBorder(new javax.swing.border.EmptyBorder(0, 5, 0, 5));
      JTextField scaleField = new JTextField(Double.toString(scale));
      JPanel scalePanel = new JPanel(new BorderLayout());
      scalePanel.add(labelScale, BorderLayout.WEST);
      scalePanel.add(scaleField, BorderLayout.CENTER);
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(label, BorderLayout.CENTER);
      panel.add(scalePanel, BorderLayout.SOUTH);
      chooser.setAccessory(panel);
      //        chooser.setAccessory(label);
      chooser.setSelectedFile(new File("default.jpg"));                    // filename is null so set a default name. //$NON-NLS-1$
      filename = OSPRuntime.chooseFilename(chooser);
      scale = Double.parseDouble(scaleField.getText());
    }
    if(filename==null) {
      return false; // user hit cancel or file name not given
    }
    // Find the desired format
    String format = "jpg"; //$NON-NLS-1$
    int index = filename.lastIndexOf('.');
    if(index>=0) {
      format = filename.substring(index+1).toLowerCase();
    } else {
      filename = filename+"."+format; //$NON-NLS-1$
    }
    boolean supported = isImageFormatSupported(format);
    if(!(supported||"gif".equalsIgnoreCase(format)||"eps".equalsIgnoreCase(format))) {                                         //$NON-NLS-1$ //$NON-NLS-2$
      String[] message = new String[] {res.getString("SnapshotTool.FormatNotSupported"),                                       //$NON-NLS-1$
                                       res.getString("SnapshotTool.PreferredFormats")};                                        //$NON-NLS-1$
      JOptionPane.showMessageDialog((JFrame) null, message, res.getString("SnapshotTool.Error"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
      return false;
    }
    Dimension originalSize = null;
    Component componentResized = null;
    int finalWidth = component.getWidth(), finalHeight = component.getHeight();
    // Resize if scale is not 1
    if(scale<=0.0) {
      scale = 1.0;
    }
    if(scale!=1.0) {                       // Take the image again, now enlarged
      originalSize = originalComponent.getSize();
      finalWidth = (int) (originalSize.width*scale);
      finalHeight = (int) (originalSize.height*scale);
      // Do the rescaling. Also required for EPS images
      if(component instanceof Renderable) {
        bi = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_3BYTE_BGR);
        bi = ((Renderable) component).render(bi);
        component.invalidate();
      } else {                             // resize the Swing element prior to taking the picture
        componentResized = originalComponent;
        componentResized.setSize(finalWidth, finalHeight);
        componentResized.validate();
        bi = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        java.awt.Graphics g = bi.getGraphics();
        component.paint(g);
        g.dispose();
        finalWidth = component.getWidth(); // for the case of EPS files
        finalHeight = component.getHeight();
      }
    }
    boolean result = true;
    try {                                                                               // Do the job
      if(output==null) {
        output = new java.io.FileOutputStream(filename);
      }
      if(supported) {
        result = javax.imageio.ImageIO.write(bi, format, output);
      } else if("eps".equalsIgnoreCase(format)) {                                       //$NON-NLS-1$
        EpsGraphics2D g = new EpsGraphics2D("", output, 0, 0, finalWidth, finalHeight); //$NON-NLS-1$
        g.drawImage(bi, new java.awt.geom.AffineTransform(), null);
        //          component.paint(g); This won't work for resized Renderables
        g.scale(0.24, 0.24);                                                            // Set resolution to 300 dpi (0.24 = 72/300)
        // g.setColorDepth(EpsGraphics2D.BLACK_AND_WHITE); // Black & white
        g.close();
      } else {                                                                          // use GIFEncoder
        try {
          GIFEncoder encoder = new GIFEncoder(bi);
          encoder.Write(output);
        } catch(Exception exc) {
          result = false;
          exc.printStackTrace();
        }
      }
      output.close();
    } catch(Exception _exc) {
      _exc.printStackTrace();
      result = false;
    }
    // Undo possible resizing
    if(componentResized!=null) {
      componentResized.setSize(originalSize.width, originalSize.height);
      componentResized.validate();
    }
    return result;
  }

  static public boolean isImageFormatSupported(String format) {
    try {
      String[] names = javax.imageio.ImageIO.getWriterFormatNames();
      for(int i = 0; i<names.length; i++) {
        if(names[i].equalsIgnoreCase(format)) {
          return true;
        }
      }
    } catch(Exception ex) {}
    return false;
  }

  // methods and classes below added by Doug Brown, 9 June 2007

  /**
   * Copies the specified image to the system clipboard.
   *
   * @param image the image to copy
   */
  public void copyImage(Image image) {
    TransferImage transfer = new TransferImage(image);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transfer, null);
  }

  /**
   * Returns the image on the clipboard, if any.
   *
   * @return the image, or null if none found
   */
  public Image getClipboardImage() {
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    try {
      if((t!=null)&&t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
        Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
        return image;
      }
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Copies an image of a component to the clipboard
   *
   * @param component the component to copy
   */
  public void copyImage(Component component) {
    new ComponentImage(component).copyToClipboard();
  }

  /**
   * Prints an image of a component
   *
   * @param component the component to print
   */
  public void printImage(Component component) {
    new ComponentImage(component).print();
  }

  /**
   * ComponentImage class for creating and printing images of components.
   */
  class ComponentImage implements Printable {
    private BufferedImage image;
    Component c;

    /**
     * Constructor ComponentImage
     * @param comp
     */
    public ComponentImage(Component comp) {
      c = comp;
      if(comp instanceof JFrame) {
        comp = ((JFrame) comp).getContentPane();
      } else if(comp instanceof JDialog) {
        comp = ((JDialog) comp).getContentPane();
      }
      image = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
      if(comp instanceof Renderable) {
        image = ((Renderable) comp).render(image);
      } else {
        java.awt.Graphics g = image.getGraphics();
        comp.paint(g);
        g.dispose();
      }
    }

    public Image getImage() {
      return image;
    }

    public void copyToClipboard() {
      copyImage(image);
    }

    public void print() {
      PrinterJob printerJob = PrinterJob.getPrinterJob();
      PageFormat format = new PageFormat();
      java.awt.print.Book book = new java.awt.print.Book();
      book.append(this, format);
      printerJob.setPageable(book);
      if(printerJob.printDialog()) {
        try {
          printerJob.print();
        } catch(PrinterException pe) {
          // JOptionPane.showMessageDialog(c,
          // TrackerRes.getString("TActions.Dialog.PrintError.Message"), //$NON-NLS-1$
          // TrackerRes.getString("TActions.Dialog.PrintError.Title"), //$NON-NLS-1$
          // JOptionPane.ERROR_MESSAGE);
        }
      }
    }

    /**
      * Implements Printable.
      *
      * @param g the printer graphics
      * @param pageFormat the format
      * @param pageIndex the page number
      * @return status code
      */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
      if(pageIndex>=1) { // only one page available
        return Printable.NO_SUCH_PAGE;
      }
      if(g==null) {
        return Printable.NO_SUCH_PAGE;
      }
      Graphics2D g2 = (Graphics2D) g;
      double scalex = pageFormat.getImageableWidth()/image.getWidth();
      double scaley = pageFormat.getImageableHeight()/image.getHeight();
      double scale = Math.min(scalex, scaley);
      scale = Math.min(scale, 1.0); // don't magnify images--only reduce if nec
      g2.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
      g2.scale(scale, scale);
      g2.drawImage(image, 0, 0, null);
      return Printable.PAGE_EXISTS;
    }

  }

  /**
   * Transferable class for copying images to the system clipboard.
   */
  class TransferImage implements Transferable {
    private Image image;

    /**
     * Constructor TransferImage
     * @param image
     */
    public TransferImage(Image image) {
      this.image = image;
    }

    public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] {DataFlavor.imageFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
      return DataFlavor.imageFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
      if(!isDataFlavorSupported(flavor)) {
        throw new UnsupportedFlavorException(flavor);
      }
      return image;
    }

  }
  // end methods and classes added by Doug Brown, 9 June 2007

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
