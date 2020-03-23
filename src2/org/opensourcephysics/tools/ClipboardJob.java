/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * This is a Job implementation for osp data transfers via the clipboard.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class ClipboardJob extends LocalJob {
  static private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

  /**
   * Constructs a ClipboardJob.
   */
  public ClipboardJob() {
    /** empty block */
  }

  /**
   * Constructs a ClipboardJob with a specified xml string.
   *
   * @param xml the string
   */
  public ClipboardJob(String xml) {
    super(xml);
  }

  /**
   * Constructs a ClipboardJob for a specified object.
   *
   * @param obj the object
   */
  public ClipboardJob(Object obj) {
    super(obj);
  }

  /**
   * Gets the xml string. Implements Job.
   *
   * @return the xml string
   */
  public String getXML() {
    // this is the paste function
    try {
      Transferable data = clipboard.getContents(null);
      return(String) data.getTransferData(DataFlavor.stringFlavor);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Sets the xml string. Implements Job.
   *
   * @param xml the xml string
   */
  public void setXML(String xml) {
    // this is the copy function
    if(xml!=null) {
      StringSelection data = new StringSelection(xml);
      clipboard.setContents(data, data);
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
