/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs.control.swing;
import java.text.ParsePosition;
import javax.swing.JTextField;
import org.opensourcephysics.ejs.control.value.DoubleValue;
import org.opensourcephysics.numerics.ParserException;
import org.opensourcephysics.numerics.SuryonoParser;

/**
 * A textfield to display double values. When this value changes,
 * it invokes both the VARIABLE_CHANGED and the ACTION actions.
 */
public class ControlParsedNumberField extends ControlNumberField {
  protected SuryonoParser parser = new SuryonoParser(0);

  // ------------------------------------------------
  // Visual component
  // ------------------------------------------------

  /**
   * Constructor ControlParsedNumberField
   * @param _visual
   */
  public ControlParsedNumberField(Object _visual) {
    super(_visual);
  }

  protected java.awt.Component createVisual(Object _visual) {
    if(_visual instanceof JTextField) {
      textfield = (JTextField) _visual;
    } else {
      textfield = new JTextField();
    }
    format = defaultFormat;
    defaultValue = 0.0;
    defaultValueSet = false;
    internalValue = new DoubleValue(defaultValue);
    textfield.setText(format.format(internalValue.value));
    textfield.addActionListener(new MyActionListener());
    textfield.addKeyListener(new MyKeyListener());
    decideColors(textfield.getBackground());
    return textfield;
  }

  // -------------------------------------
  // Private methods and inner classes
  // -------------------------------------
  private class MyActionListener implements java.awt.event.ActionListener {
    public void actionPerformed(java.awt.event.ActionEvent _e) {
      // System.out.println ("Action for "+textfield.getText());
      setColor(defaultColor);
      String text = textfield.getText().trim(); // remove whitespace
      try {
        ParsePosition parsePosition = new ParsePosition(0);
        //setInternalValue(format.parse(text).doubleValue());  // change by W. Christian
        setInternalValue(format.parse(text, parsePosition).doubleValue());
        if(text.length()==parsePosition.getIndex()) {
          return;               // return only if all characters were parsed
        }
      } catch(Exception exc) {} // Do nothing
      // see if text is a formula
      try {
        parser.parse(text);
        setInternalValue(parser.evaluate());
        return;
      } catch(ParserException exc) {
        setColor(errorColor);
        //exc.printStackTrace(System.err);
      }
    }

  }

} // End of class

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
