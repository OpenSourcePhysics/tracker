/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Print utilities for OSP componets.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class PrintUtils {
  private PrintUtils() {}

  // This method is like print() above but prints to a PostScript file
  // instead of printing to a printer.
  public static void saveComponentAsEPS(Component c) throws IOException {
    // Find a factory object for printing Printable objects to PostScript.
    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    String format = "application/postscript"; //$NON-NLS-1$
    StreamPrintServiceFactory factory = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, format)[0];
    // Ask the user to select a file and open the selected file
    //JFileChooser chooser = new JFileChooser();
    JFileChooser chooser = OSPRuntime.getChooser();
    if(chooser.showSaveDialog(c)!=JFileChooser.APPROVE_OPTION) {
      return;
    }
    File f = chooser.getSelectedFile();
    FileOutputStream out = new FileOutputStream(f);
    // Obtain a PrintService that prints to that file
    StreamPrintService service = factory.getPrintService(out);
    // Do the printing with the method below
    printToService(c, service, null);
    // And close the output file.
    out.close();
  }

  public static void printComponent(Component c) {
    // Get a list of all printers that can handle Printable objects.
    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, null);
    // Set some define printing attributes
    PrintRequestAttributeSet printAttributes = new HashPrintRequestAttributeSet();
    //printAttributes.add(OrientationRequested.LANDSCAPE); // landscape mode
    printAttributes.add(OrientationRequested.PORTRAIT);                    // PORTRAIT mode
    printAttributes.add(Chromaticity.MONOCHROME);                          // print in mono
    printAttributes.add(javax.print.attribute.standard.PrintQuality.HIGH); // highest resolution
    // Display a dialog that allows the user to select one of the
    // available printers and to edit the default attributes
    PrintService service = ServiceUI.printDialog(null, 100, 100, services, null, null, printAttributes);
    // If the user canceled, don't do anything
    if(service==null) {
      return;
    }
    // Now call a method defined below to finish the printing
    printToService(c, service, printAttributes);
  }

  public static void printToService(final Component c, PrintService service, PrintRequestAttributeSet printAttributes) {
    Printable printable = new PrintableComponent(c);
    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    Doc doc = new SimpleDoc(printable, flavor, null);
    DocPrintJob job = service.createPrintJob();
    final JOptionPane pane = new JOptionPane(DisplayRes.getString("PrintUtils.Printing.Message"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
    JDialog dialog = pane.createDialog(c, DisplayRes.getString("PrintUtils.PrintDialog.Title")); //$NON-NLS-1$
    // This listener object updates the dialog as the status changes
    job.addPrintJobListener(new PrintJobAdapter() {
      public void printJobCompleted(PrintJobEvent e) {
        pane.setMessage(DisplayRes.getString("PrintUtils.PrintComplete.Message")); //$NON-NLS-1$
      }
      public void printDataTransferCompleted(PrintJobEvent e) {
        pane.setMessage(DisplayRes.getString("PrintUtils.PrintTransferred.Message")); //$NON-NLS-1$
      }
      public void printJobRequiresAttention(PrintJobEvent e) {
        pane.setMessage(DisplayRes.getString("PrintUtils.OutOfPaper.Message")); //$NON-NLS-1$
      }
      public void printJobFailed(PrintJobEvent e) {
        pane.setMessage(DisplayRes.getString("PrintUtils.PrintFailed.Message")); //$NON-NLS-1$
      }

    });
    // Show the dialog, non-modal.
    dialog.setModal(false);
    dialog.setVisible(true);
    // Now print the Doc to the DocPrintJob
    try {
      job.print(doc, printAttributes);
    } catch(PrintException e) {
      // Display any errors to the dialog box
      pane.setMessage(e.toString());
    }
  }

  /*
   * PrintableComponent copied from Java Examples in a Nutshell.
   * Copyright (c) 2017 David Flanagan.  All rights reserved.
   * This code is from the book Java Examples in a Nutshell, 3nd Edition.
   * It is provided AS-IS, WITHOUT ANY WARRANTY either expressed or implied.
   * You may study, use, and modify it for any non-commercial purpose,
   * including teaching and use in open-source projects.
   * You may distribute it non-commercially as long as you retain this notice.
   * For a commercial use license, or to purchase the book,
   * please visit http://www.davidflanagan.com/javaexamples3.
*/
  public static class PrintableComponent implements Printable {
    Component c;

    /**
     * Constructor PrintableComponent
     * @param c
     */
    public PrintableComponent(Component c) {
      this.c = c;
    }

    // This method should print the specified page number to the specified
    // Graphics object, abiding by the specified page format.
    // The printing system will call this method repeatedly to print all
    // pages of the print job.  If pagenum is greater than the last page,
    // it should return NO_SUCH_PAGE to indicate that it is done.  The
    // printing system may call this method multiple times per page.
    public int print(Graphics g, PageFormat format, int pagenum) {
      // This implemenation is always a single page
      if(pagenum>0) {
        return Printable.NO_SUCH_PAGE;
      }
      // The Java 1.2 printing API passes us a Graphics object, but we
      // can always cast it to a Graphics2D object
      Graphics2D g2 = (Graphics2D) g;
      // Translate to accomodate the requested top and left margins.
      g2.translate(format.getImageableX(), format.getImageableY());
      // Figure out how big the drawing is, and how big the page
      // (excluding margins) is
      Dimension size = c.getSize();                    // component size
      double pageWidth = format.getImageableWidth();   // Page width
      double pageHeight = format.getImageableHeight(); // Page height
      // If the component is too wide or tall for the page, scale it down
      if(size.width>pageWidth) {
        double factor = pageWidth/size.width; // How much to scale
        g2.scale(factor, factor);             // Adjust coordinate system
        pageWidth /= factor;                  // Adjust page size up
        pageHeight /= factor;
      }
      if(size.height>pageHeight) { // Do the same thing for height
        double factor = pageHeight/size.height;
        g2.scale(factor, factor);
        pageWidth /= factor;
        pageHeight /= factor;
      }
      // Now we know the component will fit on the page.  Center it by
      // translating as necessary.
      g2.translate((pageWidth-size.width)/2, (pageHeight-size.height)/2);
      // Draw a line around the outside of the drawing area and label it
      g2.drawRect(-1, -1, size.width+2, size.height+2);
      // Set a clipping region so the component can't draw outside of
      // its won bounds.
      g2.setClip(0, 0, size.width, size.height);
      // Finally, print the component by calling its paint() method.
      // This prints the background, border, and children as well.
      // For swing components, if you don't want the background, border,
      // and children, then call printComponent() instead.
      c.paint(g);
      // Tell the PrinterJob that the page number was valid
      return Printable.PAGE_EXISTS;
    }

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
