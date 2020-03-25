/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

/**
 * A checkbox with checked, unchecked and part-checked states
 *
 * @author Dr. Heinz M. Kabutz
 * see http://www.javaspecialists.co.za/archive/Issue082.html
 */
public class TristateCheckBox extends JCheckBox {
  /** This is a type-safe enumerated type */
  public static class State {
    private State() {
      /** empty block */
    }

  }

  public static final State NOT_SELECTED = new State();
  public static final State SELECTED = new State();
  public static final State PART_SELECTED = new State();
  private final TristateDecorator model;

  /**
   * Constructor TristateCheckBox
   * @param text
   * @param icon
   * @param initial
   */
  public TristateCheckBox(String text, Icon icon, State initial) {
    super(text, icon);
    // Add a listener for when the mouse is pressed
    super.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        grabFocus();
        model.nextState();
      }

    });
    // Reset the keyboard action map
    ActionMap map = new ActionMapUIResource();
    map.put("pressed", new AbstractAction() { //$NON-NLS-1$
      public void actionPerformed(ActionEvent e) {
        grabFocus();
        model.nextState();
      }

    });
    map.put("released", null); //$NON-NLS-1$
    SwingUtilities.replaceUIActionMap(this, map);
    // set the model to the adapted model
    model = new TristateDecorator(getModel());
    setModel(model);
    setState(initial);
  }

  /**
   * Constructor TristateCheckBox
   * @param text
   * @param initial
   */
  public TristateCheckBox(String text, State initial) {
    this(text, null, initial);
  }

  /**
   * Constructor TristateCheckBox
   * @param text
   */
  public TristateCheckBox(String text) {
    this(text, PART_SELECTED);
  }

  /**
   * Constructor TristateCheckBox
   */
  public TristateCheckBox() {
    this(null);
  }

  /** No one may add mouse listeners, not even Swing! */
  public void addMouseListener(MouseListener l) {
    /** empty block */
  }

  /**
   * Set the new state to either SELECTED, NOT_SELECTED or
   * PART_SELECTED.  If state == null, it is treated as PART_SELECTED.
   */
  public void setState(State state) {
    model.setState(state);
  }

  /** Return the current state, which is determined by the
   * selection status of the model. */
  public State getState() {
    return model.getState();
  }

  public void setSelected(boolean b) {
    if(b) {
      setState(SELECTED);
    } else {
      setState(NOT_SELECTED);
    }
  }

  /**
   * Exactly which Design Pattern is this?  Is it an Adapter,
   * a Proxy or a Decorator?  In this case, my vote lies with the
   * Decorator, because we are extending functionality and
   * "decorating" the original model with a more powerful model.
   */
  private class TristateDecorator implements ButtonModel {
    private final ButtonModel other;

    private TristateDecorator(ButtonModel other) {
      this.other = other;
    }

    private void setState(State state) {
      if(state==NOT_SELECTED) {
        other.setArmed(false);
        setPressed(false);
        setSelected(false);
      } else if(state==SELECTED) {
        other.setArmed(false);
        setPressed(false);
        setSelected(true);
      } else { // either "null" or DONT_CARE
        other.setArmed(true);
        setPressed(true);
        setSelected(true);
      }
    }

    /**
     * The current state is embedded in the selection / armed
     * state of the model.
     *
     * We return the SELECTED state when the checkbox is selected
     * but not armed, PART_SELECTED state when the checkbox is
     * selected and armed (grey) and NOT_SELECTED when the
     * checkbox is deselected.
     */
    private State getState() {
      if(isSelected()&&!isArmed()) {
        // normal black tick
        return SELECTED;
      } else if(isSelected()&&isArmed()) {
        // don't care grey tick
        return PART_SELECTED;
      } else {
        // normal deselected
        return NOT_SELECTED;
      }
    }

    /** We rotate between NOT_SELECTED, SELECTED and PART_SELECTED.*/
    private void nextState() {
      State current = getState();
      if(current==NOT_SELECTED) {
        setState(SELECTED);
      } else if(current==SELECTED) {
        setState(PART_SELECTED);
      } else if(current==PART_SELECTED) {
        setState(NOT_SELECTED);
      }
    }

    /** Filter: No one may change the armed status except us. */
    public void setArmed(boolean b) {
      /** empty block */
    }

    /** We disable focusing on the component when it is not
     * enabled. */
    public void setEnabled(boolean b) {
      setFocusable(b);
      other.setEnabled(b);
    }

    /** All these methods simply delegate to the "other" model
     * that is being decorated. */
    public boolean isArmed() {
      return other.isArmed();
    }

    public boolean isSelected() {
      return other.isSelected();
    }

    public boolean isEnabled() {
      return other.isEnabled();
    }

    public boolean isPressed() {
      return other.isPressed();
    }

    public boolean isRollover() {
      return other.isRollover();
    }

    public void setSelected(boolean b) {
      other.setSelected(b);
    }

    public void setPressed(boolean b) {
      other.setPressed(b);
    }

    public void setRollover(boolean b) {
      other.setRollover(b);
    }

    public void setMnemonic(int key) {
      other.setMnemonic(key);
    }

    public int getMnemonic() {
      return other.getMnemonic();
    }

    public void setActionCommand(String s) {
      other.setActionCommand(s);
    }

    public String getActionCommand() {
      return other.getActionCommand();
    }

    public void setGroup(ButtonGroup group) {
      other.setGroup(group);
    }

    public void addActionListener(ActionListener l) {
      other.addActionListener(l);
    }

    public void removeActionListener(ActionListener l) {
      other.removeActionListener(l);
    }

    public void addItemListener(ItemListener l) {
      other.addItemListener(l);
    }

    public void removeItemListener(ItemListener l) {
      other.removeItemListener(l);
    }

    public void addChangeListener(ChangeListener l) {
      other.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
      other.removeChangeListener(l);
    }

    public Object[] getSelectedObjects() {
      return other.getSelectedObjects();
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
