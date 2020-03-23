/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core.interaction;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 *
 * <p>Title: InteractionEvent</p>
 *
 * <p>Description: This class is used to describe the basic interaction with
 * a 3d element. It extends ActionEvent in order to allow for the object
 * generating the event to include an object with additional information
 * and the mouse event which was used in the interaction.</p>
 * <p> It is up to the interacted element to decide which information (object)
 * to pass along.
*
* <p>Copyright: Open Source Physics project</p>
*
* @author Francisco Esquembre
* @version June 2005
*/
public class InteractionEvent extends ActionEvent {
  /**
   * ID for the action of pressing the mouse on the element
   */
  static public final int MOUSE_PRESSED = AWTEvent.RESERVED_ID_MAX+1;

  /**
   * ID for the action of dragging the mouse on the element
   */
  static public final int MOUSE_DRAGGED = AWTEvent.RESERVED_ID_MAX+2;

  /**
   * ID for the action of releasing the mouse on the element
   */
  static public final int MOUSE_RELEASED = AWTEvent.RESERVED_ID_MAX+3;

  /**
   * ID for the action of entering (lingering on) the element
   */
  static public final int MOUSE_ENTERED = AWTEvent.RESERVED_ID_MAX+4;

  /**
   * ID for the action of exiting the element
   */
  static public final int MOUSE_EXITED = AWTEvent.RESERVED_ID_MAX+5;

  /**
   * ID for the action of moving the mouse on the element
   */
  static public final int MOUSE_MOVED = AWTEvent.RESERVED_ID_MAX+6;
  private Object info;
  private MouseEvent mouseEvent;

  /**
   * Constructor for the event
   * @param _source Object The object which generated the event.
   * @param _id int An integer which identifies the type of event.
   * @param _command String An action command associated to the event.
   * @param _info Object The object provided as additional information.
   * @param _mouseEvent MouseEvent The mouse event which generated the interaction event.
   * It is useful to extract additional information such as the number of mouse
   * clicks or the modifier keys and mouse buttons that were down during the
   * event.
   */
  public InteractionEvent(Object _source, int _id, String _command, Object _info, MouseEvent _mouseEvent) {
    super(_source, _id, _command);
    this.info = _info;
    this.mouseEvent = _mouseEvent;
  }

  /**
   * The object with additional information provided by the source.
   * @return Object
   */
  public Object getInfo() {
    return this.info;
  }

  /**
   * The mouse event which generated the interaction event.
   * It is useful to extract additional information such as the number of mouse
   * clicks or the modifier keys and mouse buttons that were down during the
   * event.
   * @return MouseEvent
   */
  public MouseEvent getMouseEvent() {
    return this.mouseEvent;
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
