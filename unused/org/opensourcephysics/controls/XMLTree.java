/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * This is an XML tree in a scroller.
 *
 * @author Douglas Brown
 */
public class XMLTree {
  // static fields
  protected static Icon hiliteIcon;
  // instance fields
  protected XMLTreeNode root;
  protected JTree tree;
  protected JScrollPane scroller;
  protected XMLControl control;
  protected java.util.List<XMLProperty> selectedProps = new java.util.ArrayList<XMLProperty>();
  protected Class<?> hilite = Object.class;

  /**
   * Constructs a tree view of an XMLControl
   *
   * @param control the XMLControl
   */
  public XMLTree(XMLControl control) {
    this.control = control;
    createGUI();
  }

  /**
   * Gets the tree.
   *
   * @return the tree
   */
  public JTree getTree() {
    return tree;
  }

  /**
   * Gets the selected xml properties.
   *
   * @return a list of currently selected properties
   */
  public java.util.List<XMLProperty> getSelectedProperties() {
    selectedProps.clear();
    TreePath[] paths = tree.getSelectionPaths();
    if(paths!=null) {
      for(int i = 0; i<paths.length; i++) {
        XMLTreeNode node = (XMLTreeNode) paths[i].getLastPathComponent();
        selectedProps.add(node.getProperty());
      }
    }
    return selectedProps;
  }

  /**
   * Gets the scroll pane with view of the tree
   *
   * @return the scroll pane
   */
  public JScrollPane getScrollPane() {
    return scroller;
  }

  /**
   * Sets the highlighted class.
   *
   * @param type the class to highlight
   */
  public void setHighlightedClass(Class<?> type) {
    if(type!=null) {
      hilite = type;
      scroller.repaint();
    }
  }

  /**
   * Gets the highlighted class.
   *
   * @return the highlighted class
   */
  public Class<?> getHighlightedClass() {
    return hilite;
  }

  /**
   * Selects the highlighted properties.
   */
  public void selectHighlightedProperties() {
    Enumeration<?> e = root.breadthFirstEnumeration();
    while(e.hasMoreElements()) {
      XMLTreeNode node = (XMLTreeNode) e.nextElement();
      XMLProperty prop = node.getProperty();
      Class<?> type = prop.getPropertyClass();
      if((type!=null)&&hilite.isAssignableFrom(type)) {
        TreePath path = new TreePath(node.getPath());
        tree.addSelectionPath(path);
        tree.scrollPathToVisible(path);
      }
    }
  }

  /**
   * Shows the highlighted properties.
   */
  public void showHighlightedProperties() {
    Enumeration<?> e = root.breadthFirstEnumeration();
    while(e.hasMoreElements()) {
      XMLTreeNode node = (XMLTreeNode) e.nextElement();
      XMLProperty prop = node.getProperty();
      Class<?> type = prop.getPropertyClass();
      if((type!=null)&&hilite.isAssignableFrom(type)) {
        TreePath path = new TreePath(node.getPath());
        tree.scrollPathToVisible(path);
      }
    }
  }

  /**
   * Creates the GUI and listeners.
   */
  protected void createGUI() {
    // create icons
    String imageFile = "/org/opensourcephysics/resources/controls/images/hilite.gif"; //$NON-NLS-1$
    hiliteIcon = new ImageIcon(XMLTree.class.getResource(imageFile));
    // create root and tree
    root = new XMLTreeNode(control);
    tree = new JTree(root);
    tree.setCellRenderer(new HighlightRenderer());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    // put tree in a scroller
    scroller = new JScrollPane(tree);
    scroller.setPreferredSize(new Dimension(200, 200));
  }

  /**
   * A cell renderer to show launchable nodes.
   */
  private class HighlightRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      XMLTreeNode node = (XMLTreeNode) value;
      XMLProperty prop = node.getProperty();
      Class<?> type = prop.getPropertyClass();
      if((type!=null)&&hilite.isAssignableFrom(type)) {
        setIcon(hiliteIcon);
      }
      return this;
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
