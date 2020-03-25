/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * A checkbox tree cell renderer.
 *
 * @author Doug Brown
 *
 * Based on code by Santhosh Kumar T - santhosh@in.fiorano.com
 * See http://www.jroller.com/page/santhosh/20050610
 */
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {
  private CheckTreeSelectionModel selectionModel;
  private TreeCellRenderer delegate;
  private TristateCheckBox checkBox = new TristateCheckBox();

  /**
   * Constructor CheckTreeCellRenderer
   * @param delegate
   * @param selectionModel
   */
  public CheckTreeCellRenderer(TreeCellRenderer delegate, CheckTreeSelectionModel selectionModel) {
    this.delegate = delegate;
    this.selectionModel = selectionModel;
    setLayout(new BorderLayout());
    setOpaque(false);
    checkBox.setOpaque(false);
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    TreePath path = tree.getPathForRow(row);
    if(path!=null) {
      if(selectionModel.isPathOrAncestorSelected(path)) {
        checkBox.setState(TristateCheckBox.SELECTED);
      } else {
        checkBox.setState(selectionModel.isPathUnselected(path) ? TristateCheckBox.NOT_SELECTED : TristateCheckBox.PART_SELECTED);
      }
    }
    removeAll();
    add(checkBox, BorderLayout.WEST);
    add(renderer, BorderLayout.CENTER);
    return this;
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
