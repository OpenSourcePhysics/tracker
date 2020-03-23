/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;
import org.opensourcephysics.controls.ListChooser;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This is a JDialog that displays and controls a checkbox jar tree.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class JarTreeDialog extends JDialog {
  protected File rootFile;
  protected JarTreeModel jarModel;
  protected JTree jarTree;
  protected CheckTreeManager checkManager;
  protected TreePath[] selectionPaths;
  protected UndoableEditSupport undoSupport;
  protected UndoManager undoManager;
  protected JButton okButton, undoButton, redoButton, languagesButton;
  protected boolean ignoreEvents;
  protected int prevRow;
  protected Icon jarIcon, jarFileIcon, jarFolderIcon, fileIcon;

  /**
   * Constructor.
   *
   * @param owner the owner frame
   * @param root the root directory
   */
  public JarTreeDialog(Frame owner, File root) {
    super(owner, true);
    rootFile = root;
    createGUI();
    // set up the undo system
    undoManager = new UndoManager();
    undoSupport = new UndoableEditSupport();
    undoSupport.addUndoableEditListener(undoManager);
    CheckTreeSelectionModel checkModel = checkManager.getSelectionModel();
    checkModel.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if(ignoreEvents) {
          return;
        }
        TreePath[] prev = (TreePath[]) e.getOldValue();
        TreePath[] curr = (TreePath[]) e.getNewValue();
        TreePath path = jarTree.getSelectionPath();
        int row = jarTree.getRowForPath(path);
        SelectionEdit edit = new SelectionEdit(prev, prevRow, curr, row);
        undoSupport.postEdit(edit);
        prevRow = row;
        refresh();
      }

    });
    refresh();
    // center on screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-getBounds().width)/2;
    int y = (dim.height-getBounds().height)/2;
    setLocation(x, y);
  }

  /**
   * Gets the selected paths as relative path strings.
   *
   * @return an array of string paths relative to the root directory
   */
  public String[] getSelectionRelativePaths() {
    if(selectionPaths==null) {
      return null;
    }
    ArrayList<String> temp = new ArrayList<String>();
    for(int i = 0; i<selectionPaths.length; i++) {
      temp.add(getRelativePath(selectionPaths[i]));
    }
    return temp.toArray(new String[0]);
  }

  /**
   * Sets the selected relative path strings.
   *
   * @param paths an array of string paths relative to the root directory
   */
  public void setSelectionRelativePaths(String[] paths) {
    ArrayList<TreePath> temp = new ArrayList<TreePath>();
    for(int i = 0; i<paths.length; i++) {
      temp.add(getTreePath(paths[i]));
    }
    TreePath[] treePaths = temp.toArray(new TreePath[0]);
    setSelectionPaths(treePaths);
  }

  /**
   * Gets the selected paths as TreePaths that start with the root.
   *
   * @return an array of selected TreePaths
   */
  public TreePath[] getSelectionPaths() {
    return selectionPaths;
  }

  /**
   * Sets the selected TreePaths.
   *
   * @param treePaths an array of TreePaths that start with the root
   */
  public void setSelectionPaths(TreePath[] treePaths) {
    ignoreEvents = true;
    CheckTreeSelectionModel checkModel = checkManager.getSelectionModel();
    if(treePaths==null) {
      checkModel.setSelectionPaths(new TreePath[0]);
    } else {
      checkModel.setSelectionPaths(treePaths);
    }
    ignoreEvents = false;
    refresh();
  }

  /**
   * Converts a TreePath to a relative path string.
   * The TreePath is assumed to start with the root directory.
   *
   * @param path the TreePath
   * @return the relative path
   */
  private String getRelativePath(TreePath path) {
    Object[] nodes = path.getPath();
    StringBuffer buffer = new StringBuffer();
    Object jarNode = rootFile;
    // skip node[0] since it is the root
    for(int j = 1; j<nodes.length; j++) {
      if(buffer.toString().endsWith(".jar")) { //$NON-NLS-1$
        buffer.append("!");                    //$NON-NLS-1$
      }
      if(j>1) {
        buffer.append("/");                    //$NON-NLS-1$
      }
      buffer.append(nodes[j].toString());
      jarNode = jarModel.getChild(jarNode, nodes[j].toString());
    }
    // add an end slash if jarNode is directory
    if(((jarNode instanceof File)&&((File) jarNode).isDirectory())||((jarNode instanceof JarTreeModel.JarNode)&&!((JarTreeModel.JarNode) jarNode).isLeaf())) {
      buffer.append("/"); //$NON-NLS-1$
    }
    return buffer.toString();
  }

  /**
   * Gets the TreePath associated with a path relative to the rootFile
   *
   * @param relativePath the path
   * @return the TreePath
   */
  private TreePath getTreePath(String relativePath) {
    String path = XML.forwardSlash(relativePath);
    TreePath treePath = new TreePath(rootFile);
    Object parent = rootFile;
    while(parent!=null) {
      Object child = null;
      int n = path.indexOf("/"); //$NON-NLS-1$
      if(n>-1) {
        String name = path.substring(0, n);
        path = path.substring(n+1);
        if(name.endsWith("!")) { //$NON-NLS-1$
          name = name.substring(0, name.length()-1);
        }
        child = jarModel.getChild(parent, name);
      } else {
        child = jarModel.getChild(parent, path);
      }
      if(child!=null) {
        treePath = treePath.pathByAddingChild(child);
      }
      parent = child;
    }
    return treePath;
  }

  /**
   * Refreshes the buttonbar.
   */
  protected void refresh() {
    CheckTreeSelectionModel checkModel = checkManager.getSelectionModel();
    selectionPaths = checkModel.getSelectionPaths();
    okButton.setEnabled(!checkModel.isSelectionEmpty());
    undoButton.setEnabled(undoManager.canUndo());
    redoButton.setEnabled(undoManager.canRedo());
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    setTitle(ToolsRes.getString("JarTreeDialog.Title")); //$NON-NLS-1$
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setPreferredSize(new Dimension(480, 320));
    setContentPane(contentPane);
    // create icons
    String imageFile = "/org/opensourcephysics/resources/tools/images/jarfile.gif"; //$NON-NLS-1$
    jarIcon = ResourceLoader.getIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/jarcontent.gif"; //$NON-NLS-1$
    jarFileIcon = ResourceLoader.getIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/jarfolder.gif";  //$NON-NLS-1$
    jarFolderIcon = ResourceLoader.getIcon(imageFile);
    imageFile = "/org/opensourcephysics/resources/tools/images/whitefile.gif";  //$NON-NLS-1$
    fileIcon = ResourceLoader.getIcon(imageFile);
    // create the tree model and tree
    jarModel = new JarTreeModel(rootFile);
    jarTree = new JTree(jarModel);
    jarTree.setSelectionRow(0);
    jarTree.setCellRenderer(new JarRenderer());
    // add checkboxes to the tree
    checkManager = new CheckTreeManager(jarTree);
    // put the tree in a scroller
    JScrollPane scroller = new JScrollPane(jarTree);
    // put titled border around tree
    Border etched = BorderFactory.createEtchedBorder();
    TitledBorder title = BorderFactory.createTitledBorder(etched, ToolsRes.getString("JarTreeDialog.Border.Title")); //$NON-NLS-1$
    scroller.setBorder(title);
    contentPane.add(scroller, BorderLayout.CENTER);
    // create ok button
    okButton = new JButton(ToolsRes.getString("JarTreeDialog.Button.OK")); //$NON-NLS-1$
    okButton.setForeground(new Color(0, 0, 102));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    // create cancel button
    JButton cancelButton = new JButton(ToolsRes.getString("JarTreeDialog.Button.Cancel")); //$NON-NLS-1$
    cancelButton.setForeground(new Color(0, 0, 102));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        selectionPaths = null;
        setVisible(false);
      }

    });
    // create undo button
    undoButton = new JButton(ToolsRes.getString("JarTreeDialog.Button.Undo")); //$NON-NLS-1$
    undoButton.setForeground(new Color(0, 0, 102));
    undoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undoManager.undo();
      }

    });
    // create redo button
    redoButton = new JButton(ToolsRes.getString("JarTreeDialog.Button.Redo")); //$NON-NLS-1$
    redoButton.setForeground(new Color(0, 0, 102));
    redoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undoManager.redo();
      }

    });
    // create languages button
    languagesButton = new JButton(ToolsRes.getString("JarTreeDialog.Button.Languages")); //$NON-NLS-1$
    languagesButton.setForeground(new Color(0, 0, 102));
    languagesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // create a list chooser
        ListChooser dialog = new ListChooser(ToolsRes.getString("JarTreeDialog.Chooser.Languages.Title"), //$NON-NLS-1$
          ToolsRes.getString("JarTreeDialog.Chooser.Languages.Message"),                                  //$NON-NLS-1$
            JarTreeDialog.this);
        // create the lists and fill with Locales
        ArrayList<Locale> choices = new ArrayList<Locale>();
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Locale> originals = new ArrayList<Locale>();
        Locale[] locales = OSPRuntime.getInstalledLocales();
        boolean[] selected = new boolean[locales.length];
        boolean[] disabled = new boolean[locales.length];
        Object[] rootPath = new Object[] {jarModel.getRoot()};
        Collection<Object[]> jarPaths = jarModel.getDescendantPaths(rootPath);
        CheckTreeSelectionModel checkModel = checkManager.getSelectionModel();
        for(int i = 0; i<locales.length; i++) {
          choices.add(locales[i]);
          originals.add(locales[i]);
          names.add(locales[i].getDisplayLanguage(ToolsRes.resourceLocale));
          String lang = locales[i].getLanguage();
          if(locales[i]==Locale.ENGLISH) {
            selected[i] = true;
            disabled[i] = true;
          }
          // look thru jar paths to see if locale is selected
          else {
            for(Iterator<Object[]> it = jarPaths.iterator(); it.hasNext(); ) {
              // find paths containing ".properties" and "display_res_xx"
              Object[] array = it.next();
              String s = array[array.length-1].toString();                                                // file namae
              if((s.indexOf(".properties")>-1                                                             //$NON-NLS-1$
                )&&(s.indexOf("display_res_"+lang)>-1)) {                                                 //$NON-NLS-1$
                // see if path or ancestor is selected
                TreePath thePath = new TreePath(array);
                if(thePath.toString().indexOf(OSPRuntime.getLaunchJarName())>-1) {
                  selected[i] = checkModel.isPathOrAncestorSelected(thePath);
                  break;
                }
              }
            }
          }
        }
        // show the list chooser for user input
        if(dialog.choose(choices, names, null, selected, disabled)) {                                     // false if canceled
          Collection<TreePath> removePaths = new ArrayList<TreePath>();
          Collection<TreePath> addPaths = new ArrayList<TreePath>();
          // compare choices with originals
          for(Iterator<Locale> it = originals.iterator(); it.hasNext(); ) {
            Locale next = it.next();
            String lang = next.getLanguage();
            for(Iterator<Object[]> pathIt = jarPaths.iterator(); pathIt.hasNext(); ) {
              // find paths containing ".properties" and "_xx"
              Object[] array = pathIt.next();                                                             // get next path
              String s = array[array.length-1].toString();                                                // get last path object
              if((s.indexOf(".properties")>-1                                                             //$NON-NLS-1$
                )&&(s.indexOf("_"+lang)>-1)) {                                                            //$NON-NLS-1$
                TreePath propPath = new TreePath(array);
                // don't change paths in jars other than the launch jar
                if(propPath.toString().indexOf(OSPRuntime.getLaunchJarName())>-1) {
                  // eliminate unselected languages
                  if(!choices.contains(next)) {
                    removePaths.add(propPath);
                  }
                  // add selected languages
                  else {
                    addPaths.add(propPath);
                  }
                }
              }
            }
          }
          TreePath[] paths = removePaths.toArray(new TreePath[0]);
          checkManager.getSelectionModel().removeSelectionPaths(paths);
          paths = addPaths.toArray(new TreePath[0]);
          checkManager.getSelectionModel().addSelectionPaths(paths);
          refresh();
        }
      }

    });
    // create and add buttonbar at bottom
    JPanel buttonbar = new JPanel(new FlowLayout());
    buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    // add buttons to buttonbar
    buttonbar.add(languagesButton);
    buttonbar.add(undoButton);
    buttonbar.add(redoButton);
    buttonbar.add(okButton);
    buttonbar.add(cancelButton);
    pack();
  }

  /**
   * A class to undo/redo tree node selections.
   */
  protected class SelectionEdit extends AbstractUndoableEdit {
    TreePath[] undo;
    TreePath[] redo;
    int undoRow;
    int redoRow;

    /**
     * Constructor SelectionEdit
     * @param undoPaths
     * @param redoPaths
     */
    public SelectionEdit(TreePath[] undoPaths, TreePath[] redoPaths) {
      undo = undoPaths;
      redo = redoPaths;
    }

    /**
     * Constructor SelectionEdit
     * @param undoPaths
     * @param undoRow
     * @param redoPaths
     * @param redoRow
     */
    public SelectionEdit(TreePath[] undoPaths, int undoRow, TreePath[] redoPaths, int redoRow) {
      undo = undoPaths;
      redo = redoPaths;
      this.undoRow = undoRow;
      this.redoRow = redoRow;
    }

    public void undo() throws CannotUndoException {
      super.undo();
      ignoreEvents = true;
      CheckTreeSelectionModel checkModel = checkManager.getSelectionModel();
      checkModel.setSelectionPaths(undo);
      jarTree.setSelectionRow(undoRow);
      ignoreEvents = false;
      refresh();
      prevRow = undoRow;
    }

    public void redo() throws CannotUndoException {
      super.redo();
      ignoreEvents = true;
      CheckTreeSelectionModel checkModel = checkManager.getSelectionModel();
      checkModel.setSelectionPaths(redo);
      jarTree.setSelectionRow(redoRow);
      ignoreEvents = false;
      refresh();
      prevRow = redoRow;
    }

    public String getPresentationName() {
      return "Change Selection"; //$NON-NLS-1$
    }

  }

  /**
   * A cell renderer to display jar and file nodes.
   */
  protected class JarRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      if(value instanceof File) {
        File file = (File) value;
        if(file.getName().endsWith(".jar")) { //$NON-NLS-1$
          setIcon(jarIcon);
        } else if(leaf) {
          setIcon(fileIcon);
        }
      } else if(value instanceof JarTreeModel.JarNode) {
        setIcon(leaf ? jarFileIcon : jarFolderIcon);
      }
      return this;
    }

  }
  //  // main method for testing
  //  public static void main(String[] args) {
  //    JFrame frame = new JFrame();
  //    frame.setSize(400,600);
  //    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  //    frame.setVisible(true);
  //    File root = new File(OSPRuntime.getUserHome());
  //    JarTreeDialog tree = new JarTreeDialog(frame, root);
  //    tree.setVisible(true);
  //    String[] paths = tree.getSelectionRelativePaths();
  //    if (paths == null) System.out.println("no paths"); //$NON-NLS-1$
  //    else for (int i = 0; i < paths.length; i++) {
  //      System.out.println(paths[i]);
  //    }
  //  }

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
