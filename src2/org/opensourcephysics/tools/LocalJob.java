/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.io.Serializable;

import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;

/**
 * This is a Job implementation for osp data transfers within a single vm.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class LocalJob implements Job, Serializable {
	
	private static final long serialVersionUID = 1L;

  // instance fields
  String xml;

  /**
   * Constructs a LocalJob.
   */
  public LocalJob() {
    setXML(new Object());
  }

  /**
   * Constructs a LocalJob with a specified xml string.
   *
   * @param xml the string
   */
  public LocalJob(String xml) {
    setXML(xml);
  }

  /**
   * Constructs a LocalJob for a specified object.
   *
   * @param obj the object
   */
  public LocalJob(Object obj) {
    setXML(obj);
  }

  /**
   * Gets the xml string. Implements Job.
   *
   * @return the xml string
   */
  public String getXML() {
    return xml;
  }

  /**
   * Sets the xml string. Implements Job.
   *
   * @param xml the xml string
   */
  public void setXML(String xml) {
    if(xml!=null) {
      this.xml = xml;
    }
  }

  /**
   * Sets the xml string to that saved by the specified object.
   *
   * @param obj the object
   */
  public void setXML(Object obj) {
    XMLControl control = new XMLControlElement(obj);
    setXML(control.toXML());
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
