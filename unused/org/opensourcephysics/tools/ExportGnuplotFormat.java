/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display2d.GridData;

/**
* Text format, compatible with Octave and Gnuplot
*
* @author Kipton Barros
* @version 1.0
*/
public class ExportGnuplotFormat implements ExportFormat {
  public String description() {
    return "Text"; //$NON-NLS-1$
  }

  public String extension() {
    return "txt"; //$NON-NLS-1$
  }

  /*
   * Writes indexed x, y data as a (n, 2) matrix, in text format.
   *
   * @param  file
   */
  void exportDataset(PrintWriter pw, Dataset data, int index) throws IOException {
    double[] x = data.getXPoints();
    double[] y = data.getYPoints();
    pw.print("\n# name: data"+index+"\n"+"# type: matrix\n"+"# rows: "+x.length+"\n"+"# columns: "+2+"\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    for(int j = 0; j<x.length; j++) {
      pw.println(x[j]+" "+y[j]); //$NON-NLS-1$
    }
  }

  void exportGridData(PrintWriter pw, GridData gridData, int index) throws IOException {
    // double[][] data = gridData.getData()[0];
    int nx = gridData.getNx(); // data.length;
    int ny = gridData.getNy(); // data[0].length;
    double x0 = gridData.getLeft();
    // double x1 = gridData.getRight();
    double dx = gridData.getDx();
    pw.println("\n# name: col_range"+index+"\n"+"# type: matrix\n"+"# rows: 1\n"+"# columns: "+nx); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    for(int i = 0; i<nx; i++) {
      pw.print((x0+i*dx)+" "); //$NON-NLS-1$
    }
    pw.println("\n"); //$NON-NLS-1$
    double y0 = gridData.getTop();
    // double y1 = gridData.getBottom();
    double dy = gridData.getDy();
    pw.println("# name: row_range"+index+"\n"+"# type: matrix\n"+"# rows: 1\n"+"# columns: "+ny); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    for(int i = 0; i<ny; i++) {
      pw.print((y0+i*dy)+" "); //$NON-NLS-1$
    }
    pw.println("\n"); //$NON-NLS-1$
    int nc = gridData.getComponentCount(); // number of components
    // added by W. Christian
    for(int c = 0; c<nc; c++) {                                                                                // iterate over the number of data components in the grid
      String cname = gridData.getComponentName(c);
      pw.println("# name: grid_"+index+'_'+cname+'\n'+"# type: matrix\n"+"# rows: "+ny+'\n'+"# columns: "+nx); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      for(int i = 0; i<ny; i++) {
        for(int j = 0; j<nx; j++) {
          // pw.print(data[j][i] + " ");  // removed by W. Christian
          // the getValue method works for all types of grid data
          pw.print(gridData.getValue(j, i, c)+" "); //$NON-NLS-1$
        }
        pw.println();
      }
    }
  }

  public void export(File file, List<Object> data) {
    try {
      FileWriter fw = new FileWriter(file);
      PrintWriter pw = new PrintWriter(fw);
      pw.println("# Created by the Open Source Physics library");                                       //$NON-NLS-1$
      Iterator<Object> it = data.iterator();
      for(int i = 0; it.hasNext(); i++) {
        Object o = it.next();
        if(o instanceof Dataset) {
          exportDataset(pw, (Dataset) o, i);
        } else if(o instanceof GridData) {
          exportGridData(pw, (GridData) o, i);
        }
      }
      pw.close();
    } catch(IOException e) {
      JOptionPane.showMessageDialog(null, ToolsRes.getString("ExportFormat.Dialog.WriteError.Message"), //$NON-NLS-1$
        ToolsRes.getString("ExportFormat.Dialog.WriteError.Title"),                                     //$NON-NLS-1$
          JOptionPane.ERROR_MESSAGE);
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
