/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display2d;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import org.opensourcephysics.display.OSPFrame;

public class GridTableFrame extends OSPFrame {
  GridData griddata;
  JTabbedPane tabbedPane = new JTabbedPane();
  GridDataTable[] tables;

  /**
   * Constructor GridTableFrame
   * @param griddata
   */
  public GridTableFrame(GridData griddata) {
    setTitle("Grid-Data Table"); //$NON-NLS-1$
    setSize(400, 300);
    this.griddata = griddata;
    int n = griddata.getComponentCount();
    tables = new GridDataTable[n];
    for(int i = 0; i<n; i++) {
      tables[i] = new GridDataTable(griddata, i);
      JScrollPane scrollpane = new JScrollPane(tables[i]);
      scrollpane.createHorizontalScrollBar();
      if(n==1) {
        getContentPane().add(scrollpane, BorderLayout.CENTER);
        return;
      }
      tabbedPane.addTab(griddata.getComponentName(i), scrollpane);
    }
    getContentPane().add(tabbedPane, BorderLayout.CENTER);
  }

  public void refreshTable() {
    for(int i = 0, n = tables.length; i<n; i++) {
      tables[i].refreshTable();
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
