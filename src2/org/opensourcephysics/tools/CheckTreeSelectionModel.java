/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A checkbox tree selection model.
 *
 * @author Doug Brown
 *
 * Based on code by Santhosh Kumar T - santhosh@in.fiorano.com
 * See http://www.jroller.com/page/santhosh/20050610
 */
public class CheckTreeSelectionModel extends DefaultTreeSelectionModel {
  private TreeModel model;
  private PropertyChangeSupport support;

  /**
   * Constructor.
   *
   * @param model a TreeModel
   */
  public CheckTreeSelectionModel(TreeModel model) {
    this.model = model;
    setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    support = new SwingPropertyChangeSupport(this);
  }

  /**
   * Returns true if neither the path nor any descendant is selected.
   *
   * @param path the path to test
   */
  public boolean isPathUnselected(TreePath path) {
    if(isSelectionEmpty()) {
      return true; // nothing is selected
    }
    if(isPathOrAncestorSelected(path)) {
      return false; // path is selected
    }
    TreePath[] selectionPaths = getSelectionPaths();
    for(int i = 0; i<selectionPaths.length; i++) {
      if(path.isDescendant(selectionPaths[i])) { // found a selected descendant
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the path or any ancestor is selected.
   *
   * @param path the path to test
   */
  public boolean isPathOrAncestorSelected(TreePath path) {
    while((path!=null)&&!isPathSelected(path)) {
      path = path.getParentPath();
    }
    return path!=null;
  }

  public void setSelectionPaths(TreePath[] paths) {
    super.clearSelection();
    addSelectionPaths(paths);
  }

  /**
   * Adds paths to the current selection
   *
   * @param paths the paths to add
   */
  public void addSelectionPaths(TreePath[] paths) {
    if(paths==null) {
      return;
    }
    TreePath[] prev = getSelectionPaths();
    // unselect all descendants of paths
    for(int i = 0; i<paths.length; i++) {
      if(isSelectionEmpty()) {
        break; // nothing to unselect
      }
      TreePath path = paths[i];
      TreePath[] selectionPaths = getSelectionPaths();
      ArrayList<TreePath> toBeRemoved = new ArrayList<TreePath>();
      for(int j = 0; j<selectionPaths.length; j++) {
        if(path.isDescendant(selectionPaths[j])) {
          toBeRemoved.add(selectionPaths[j]);
        }
      }
      super.removeSelectionPaths(toBeRemoved.toArray(new TreePath[0]));
    }
    // if all siblings selected, unselect them and select parent recursively, 
    // otherwise select the path
    for(int i = 0; i<paths.length; i++) {
      TreePath path = paths[i];
      TreePath temp = null;
      while(isSiblingsSelected(path)) {
        temp = path;
        if(path.getParentPath()==null) {
          break;
        }
        path = path.getParentPath();
      }
      if(temp!=null) {
        if(temp.getParentPath()!=null) {
          addSelectionPath(temp.getParentPath());
        } else { // selected path is root, so unselect all others
          if(!isSelectionEmpty()) {
            removeSelectionPaths(getSelectionPaths());
          }
          super.addSelectionPaths(new TreePath[] {temp});
        }
      } else {
        super.addSelectionPaths(new TreePath[] {path});
      }
    }
    support.firePropertyChange("treepaths", prev, getSelectionPaths()); //$NON-NLS-1$
  }

  /**
   * Removes paths from the current selection
   *
   * @param paths the paths to remove
   */
  public void removeSelectionPaths(TreePath[] paths) {
    if(isSelectionEmpty()) {
      return; // nothing to remove
    }
    TreePath[] prev = getSelectionPaths();
    for(int i = 0; i<paths.length; i++) {
      TreePath path = paths[i];
      if(path.getPathCount()==1) { // root node
        super.removeSelectionPaths(new TreePath[] {path});
      } else {
        if(isPathSelected(path)) {
          super.removeSelectionPaths(new TreePath[] {path});
        } else {
          unselectAncestor(path);
        }
      }
    }
    support.firePropertyChange("treepaths", prev, getSelectionPaths()); //$NON-NLS-1$
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param listener the object requesting property change notification
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param listener the listener requesting removal
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    support.removePropertyChangeListener(listener);
  }

  /**
   * Determines whether all siblings of given path are selected
   *
   * @param path the path to test
   * @return true if all siblings selected
   */
  private boolean isSiblingsSelected(TreePath path) {
    TreePath parent = path.getParentPath();
    if(parent==null) {
      return true;
    }
    Object node = path.getLastPathComponent();
    Object parentNode = parent.getLastPathComponent();
    int childCount = model.getChildCount(parentNode);
    for(int i = 0; i<childCount; i++) {
      Object childNode = model.getChild(parentNode, i);
      if(childNode.equals(node)) {
        continue;
      }
      if(!isPathSelected(parent.pathByAddingChild(childNode))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Unselects the ancestor of a path and selects ancestor's descendants
   * other than those for which the path is a descendant.
   *
   * @param path the path
   */
  private void unselectAncestor(TreePath path) {
    // find selected ancestor 
    Stack<TreePath> stack = new Stack<TreePath>();
    stack.push(path);
    TreePath ancestor = path.getParentPath();
    while((ancestor!=null)&&!isPathSelected(ancestor)) {
      stack.push(ancestor);
      ancestor = ancestor.getParentPath();
    }
    if(ancestor==null) { // no selected ancestors!
      return;
    }
    stack.push(ancestor);
    // for each path in the stack, unselect it and select all child nodes
    while(!stack.isEmpty()) {
      TreePath next = stack.pop();
      super.removeSelectionPaths(new TreePath[] {next});
      if(stack.isEmpty()) {
        return;
      }
      Object node = next.getLastPathComponent();
      int childCount = model.getChildCount(node);
      for(int i = 0; i<childCount; i++) {
        Object child = model.getChild(node, i);
        super.addSelectionPaths(new TreePath[] {next.pathByAddingChild(child)});
      }
    }
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
