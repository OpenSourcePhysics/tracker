/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.io.File;
import java.net.URL;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This provides undo support for Launcher.
 *
 * @author Douglas Brown
 */
public class LauncherUndo extends UndoManager {
  private Launcher launcher;

  /**
   * Constructor.
   *
   * @param launcher the Launcher to undo/redo
   */
  public LauncherUndo(Launcher launcher) {
    setLauncher(launcher);
  }

  /**
   * Sets the launcher.
   *
   * @param launcher the Launcher to undo/redo
   */
  public void setLauncher(Launcher launcher) {
    this.launcher = launcher;
  }

  /**
   * Returns true if pending edit is a link edit.
   *
   * @return true if pending edit is a link edit
   */
  public boolean canReload() {
    return this.editToBeUndone() instanceof LoadEdit;
  }

  /**
   * Gets the current launcher state. Returns null if launcher is empty
   * or if the current filename does not refer to an existing file.
   *
   * @return String array [0] = file name [1] = selected node path
   */
  public String[] getLauncherState() {
    if(launcher.tabSetName==null) {
      return null; // empty launcher
    }
    String fileName = XML.getResolvedPath(launcher.tabSetName, Launcher.tabSetBasePath);
    // prepend launch jar name to internal xsets
    if(!fileName.startsWith(Launcher.defaultFileName)&&Launcher.tabSetBasePath.equals("")) { //$NON-NLS-1$
      fileName = OSPRuntime.getLaunchJarName()+"!/"+fileName;                                //$NON-NLS-1$
    } else {
      File file = new File(fileName);
      if(!file.exists()) {
        return null;                                                                         // not a file
      }
    }
    String[] state = new String[2];
    state[0] = fileName;
    if(launcher.getSelectedNode()!=null) {
      state[1] = launcher.getSelectedNode().getPathString();
    } else {
      state[1] = (launcher.getSelectedTab()==null) ? "" : //$NON-NLS-1$
        launcher.getSelectedTab().getRootNode().name;
    }
    return state;
  }

  /**
   * A class to undo/redo a node link, open file or new tabset action.
   */
  protected class LoadEdit extends AbstractUndoableEdit {
    String[] args = new String[2], prev = new String[2];

    /**
     * Constructor specifies new file and node. Prev file and node are
     * determined from current launcher state.
     *
     * @param newArgs [0] new file name, [1] new tab and node name
     * @param prevArgs [0] prev file name, [1] prev tab and node name
     */
    public LoadEdit(String[] newArgs, String[] prevArgs) {
      if(newArgs!=null) {
        args[0] = newArgs[0];
        args[1] = (newArgs.length<2) ? "" : newArgs[1]; //$NON-NLS-1$
      }
      prev[0] = prevArgs[0];
      prev[1] = (prevArgs.length<2) ? "" : prevArgs[1]; //$NON-NLS-1$
    }

    public void undo() throws CannotUndoException {
      super.undo();
      launcher.postEdits = false;
      launcher.open(prev);
      if(args[0]==null) {
        int n = LauncherUndo.this.edits.size()-1;
        LauncherUndo.this.trimEdits(n, n);
      }
      launcher.refreshGUI();
      launcher.postEdits = true;
    }

    public void redo() throws CannotUndoException {
      super.redo();
      launcher.postEdits = false;
      launcher.open(args);
      launcher.refreshGUI();
      launcher.postEdits = true;
    }

    public String getPresentationName() {
      return "Link"; //$NON-NLS-1$
    }

  }

  /**
   * A class to undo/redo a hyperlink or user navigation action.
   */
  protected class NavEdit extends AbstractUndoableEdit {
    String undoFile, redoFile, undoNode, redoNode;
    Integer undoPage, redoPage;
    URL redoURL, undoURL;

    /**
     * Constructor. State arrays are [0] String filename (may be null), [1]
     * String nodepath, [2] Integer pagenumber, [3] URL url (may be null)
     *
     * @param oldState the prevous state (undo)
     * @param newState the new state (redo)
     */
    public NavEdit(Object[] oldState, Object[] newState) {
      undoFile = (String) oldState[0];
      redoFile = (String) newState[0];
      undoNode = (String) oldState[1];
      redoNode = (String) newState[1];
      undoPage = (Integer) oldState[2];
      redoPage = (Integer) newState[2];
      undoURL = (URL) oldState[3];
      redoURL = (URL) newState[3];
    }

    /**
     * Constructor.
     *
     * @param prev the prevous node (undo)
     * @param node the new node (redo)
     */
    public NavEdit(LaunchNode prev, LaunchNode node) {
      if(prev!=null) {
        undoNode = prev.getPathString();
        undoURL = prev.htmlURL;
        undoPage = new Integer(prev.tabNumber);
      }
      if(node!=null) {
        redoNode = node.getPathString();
        redoURL = node.htmlURL;
        redoPage = new Integer(node.tabNumber);
      }
    }

    public void undo() throws CannotUndoException {
      super.undo();
      // set file, node, page and/or URL
      if((undoFile!=null)&&!undoFile.equals(redoFile)) {
        // TODO load file
      }
      launcher.postEdits = false;
      int page = (undoPage==null) ? 0 : undoPage.intValue();
      launcher.setSelectedNode(undoNode, page, undoURL);
      launcher.postEdits = true;
    }

    public void redo() throws CannotUndoException {
      super.redo();
      // set file, node, page and/or URL
      if((redoFile!=null)&&!redoFile.equals(undoFile)) {
        // TODO load file
      }
      launcher.postEdits = false;
      int page = (redoPage==null) ? 0 : redoPage.intValue();
      launcher.setSelectedNode(redoNode, page, redoURL);
      launcher.postEdits = true;
    }

    public String getPresentationName() {
      return "Navigation"; //$NON-NLS-1$
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
