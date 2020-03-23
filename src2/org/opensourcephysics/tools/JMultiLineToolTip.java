package org.opensourcephysics.tools;

import javax.swing.JToolTip;

/**
 * A multiline tooltip based on open source code from http://code.ohloh.net.
 *
 */
public class JMultiLineToolTip extends JToolTip {
  /**
   * Constructor
   */
  public JMultiLineToolTip() {
    setUI(new MultiLineToolTipUI());
  }
}