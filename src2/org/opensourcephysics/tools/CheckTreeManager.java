/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * A checkbox mouse and tree selection listener.
 *
 * @author Douglas Brown
 *
 * Based on code by Santhosh Kumar T - santhosh@in.fiorano.com
 * See http://www.jroller.com/page/santhosh/20050610
 */
public class CheckTreeManager extends MouseAdapter implements TreeSelectionListener, MouseMotionListener {
  private CheckTreeSelectionModel selectionModel;
  private JTree tree = new JTree();
  int hotspot = new JCheckBox().getPreferredSize().width;
  boolean ignoreEvents = false;

  /**
   * Constructor.
   *
   * @param tree a JTree
   */
  public CheckTreeManager(JTree tree) {
    this.tree = tree;
    selectionModel = new CheckTreeSelectionModel(tree.getModel());
    tree.setCellRenderer(new CheckTreeCellRenderer(tree.getCellRenderer(), selectionModel));
    tree.addMouseListener(this);
    tree.addMouseMotionListener(this);
    selectionModel.addTreeSelectionListener(this);
  }

  /**
   * Handles mouse moved events.
   *
   * @param e the mouse event
   */
  public void mouseMoved(MouseEvent e) {
    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    if(path==null) {
      return;
    }
    if((e.getX()>tree.getPathBounds(path).x+hotspot-3)||(e.getX()<tree.getPathBounds(path).x+2)) {
      tree.setCursor(Cursor.getDefaultCursor());
    } else {
      tree.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
  }

  /**
   * Handles mouse click events.
   *
   * @param e the mouse event
   */
  public void mouseClicked(MouseEvent e) {
    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
    if(path==null) {
      return;
    }
    if(e.getX()>tree.getPathBounds(path).x+hotspot) {
      return;
    }
    boolean selected = selectionModel.isPathOrAncestorSelected(path);
    try {
      ignoreEvents = true;
      if(selected) {
        selectionModel.removeSelectionPath(path);
      } else {
        selectionModel.addSelectionPath(path);
      }
    } finally {
      ignoreEvents = false;
      tree.treeDidChange();
    }
  }

  public CheckTreeSelectionModel getSelectionModel() {
    return selectionModel;
  }

  public void valueChanged(TreeSelectionEvent e) {
    if(!ignoreEvents) {
      tree.treeDidChange();
    }
  }

  public void mouseDragged(MouseEvent e) {
    /** empty block */
  }

}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017 The Open Source Physics project
 * http://www.opensourcephysics.org
 */
