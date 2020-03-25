/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.opensourcephysics.display.OSPRuntime;

/**
 * A NumberField is a JTextField that formats and displays numbers.
 * This default implementation displays very small and very large numbers
 * in scientific notation and intermediate-value numbers in decimal form.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class NumberField extends JTextField {
	
	// constants
	public static final Color DISABLED_COLOR = new Color(120, 120, 120);
	public static final String INTEGER_PATTERN = "0"; //$NON-NLS-1$
	public static final String DECIMAL_1_PATTERN = "0.0"; //$NON-NLS-1$
  public static final String DECIMAL_2_PATTERN = "0.00"; //$NON-NLS-1$
  public static final String DECIMAL_3_PATTERN = "0.000"; //$NON-NLS-1$
	
  // instance fields
  protected DecimalFormat format = (DecimalFormat) NumberFormat.getInstance();
  protected double prevValue;
  protected Double maxValue;
  protected Double minValue;
  protected int sigfigs;
  protected boolean fixedPattern = false;
  protected String[] patterns = new String[5];
  protected double[] ranges = {0.1, 10, 100, 1000};
  protected String units;
  protected double conversionFactor = 1.0;
  protected String userPattern = ""; //$NON-NLS-1$
  protected boolean fixedPatternByDefault;

  /**
   * Constructs a NumberField with default sigfigs (4)
   *
   * @param columns the number of character columns
   */
  public NumberField(int columns) {
    this(columns, 4);
  }

  /**
   * Constructs a NumberField with specified significant figures.
   *
   * @param columns the number of character columns
   * @param sigfigs the number of significant figures
   */
  public NumberField(int columns, int sigfigs) {
    super(columns);
    setBackground(Color.white);
    setDisabledTextColor(DISABLED_COLOR);
    setText("0"); //$NON-NLS-1$
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	if (!isEditable()) return;
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          // delay background change so other listeners can look for yellow
          Runnable runner = new Runnable() {
            public synchronized void run() {
              setBackground(Color.white);
              setValue(getValue());
            }

          };
          SwingUtilities.invokeLater(runner);
        } else {
          setBackground(Color.yellow);
        }
      }

    });
    addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (!isEditable()) return;
        // delay background change so other listeners can look for yellow
        Runnable runner = new Runnable() {
          public synchronized void run() {
            setBackground(Color.white);
            setValue(getValue());
          }

        };
        SwingUtilities.invokeLater(runner);
      }

    });
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
      	if (!isEditable()) return;
        if(e.getClickCount()==2) {
          selectAll();
        }
      }

    });
    setSigFigs(sigfigs);
  }

  /**
   * Gets the value from the text field.
   *
   * @return the value
   */
  public double getValue() {
    String s = getText().trim();
    if ("".equals(s)) return prevValue; //$NON-NLS-1$
    
    // strip units, if any
    if((units!=null)&&!units.equals("")) { //$NON-NLS-1$
      int n = s.indexOf(units);
      while(n>-1) {
        s = s.substring(0, n);
        n = s.indexOf(units);
      }
    }
    if(s.equals(format.format(prevValue*conversionFactor))) {
      return prevValue;
    }
    double retValue;
    try {
      retValue = format.parse(s).doubleValue()/conversionFactor;
      if(minValue!=null && retValue<minValue) {
        setValue(minValue);
        return minValue;
      }
      if(maxValue!=null && retValue>maxValue) {
        setValue(maxValue);
        return maxValue;
      }
    } catch(ParseException e) {
      Toolkit.getDefaultToolkit().beep();
      setValue(prevValue);
      return prevValue;
    }
    return retValue;
  }

  /**
   * Formats the specified value and enters it in the text field.
   *
   * @param value the value to be entered
   */
  public void setValue(double value) {
    if(!isVisible()) {
      return;
    }
    
    if(minValue!=null) {
      value = Math.max(value, minValue.doubleValue());
    }
    if(maxValue!=null) {
      value = Math.min(value, maxValue.doubleValue());
    }
    prevValue = value;
    
    // display value: include factor, format, units
    value = conversionFactor*value;
    setFormatFor(value);
    String s = getFormat().format(value);
    if(units!=null) {
      s += units;
    }
    if(!s.equals(getText())) {
      setText(s);
    }
  }
  
  /**
   * Sets the expected range of values for this number field.
   * Note this does not set a firm max or min--only an expectation.
   *
   * @param lower the lower end of the range
   * @param upper the upper end of the range
   */
  public void setExpectedRange(double lower, double upper) {
    fixedPattern = fixedPatternByDefault = true;
    double range = Math.max(Math.abs(lower), Math.abs(upper));
    if((range<0.1)||(range>=1000)) {     // scientific format
      String s = "";                     //$NON-NLS-1$
      for(int i = 0; i<sigfigs-1; i++) {
        s += "0";                        //$NON-NLS-1$
      }
      format.applyPattern("0."+s+"E0"); //$NON-NLS-1$ //$NON-NLS-2$
    } else {                             // decimal format
      int n;
      if(range<1) {
        n = sigfigs;
      } else if(range<10) {
        n = sigfigs-1;
      } else if(range<100) {
        n = sigfigs-2;
      } else {
        n = sigfigs-3;
      }
      String s = "";                     //$NON-NLS-1$
      for(int i = 0; i<n; i++) {
        s += "0";                        //$NON-NLS-1$
      }
      if(s.equals("")) {				 //$NON-NLS-1$
        format.applyPattern("0");        //$NON-NLS-1$ 
      } else {
        format.applyPattern("0."+s);    //$NON-NLS-1$
      }
    }
  }

  /**
   * Sets the number of significant figures for this number field.
   *
   * @param sigfigs the number of significant figures (between 2 and 6)
   */
  public void setSigFigs(int sigfigs) {
    if(this.sigfigs==sigfigs) {
      return;
    }
    ranges = new double[] {0.1, 10, 100, 1000};
    sigfigs = Math.max(sigfigs, 2);
    this.sigfigs = Math.min(sigfigs, 6);
    char d = '.';
    if(sigfigs==2) {
      patterns[0] = "0"+d+"0E0";                            // value < 1 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[1] = "0"+d+"0";                              // value < 10 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[2] = "0";                                    // value < 100 //$NON-NLS-1$
      patterns[3] = "0"+d+"0E0";                            // value < 1000 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[4] = "0"+d+"0E0";                            // value > 1000 //$NON-NLS-1$ //$NON-NLS-2$
    } else if(sigfigs==3) {
      patterns[0] = "0"+d+"00E0";                           // value < 1 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[1] = "0"+d+"00";                             // value < 10 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[2] = "0"+d+"0";                              // value < 100 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[3] = "0";                                    // value < 1000 //$NON-NLS-1$
      patterns[4] = "0"+d+"00E0";                           // value > 1000 //$NON-NLS-1$ //$NON-NLS-2$
    } else if(sigfigs>=4) {
      patterns[0] = "0"+d+"000E0";                          // value < 1 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[1] = "0"+d+"000";                            // value < 10 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[2] = "0"+d+"00";                             // value < 100 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[3] = "0"+d+"0";                              // value < 1000 //$NON-NLS-1$ //$NON-NLS-2$
      patterns[4] = "0"+d+"000E0";                          // value > 1000 //$NON-NLS-1$ //$NON-NLS-2$
      int n = sigfigs-4;
      for(int i = 0; i<n; i++) {
        for(int j = 0; j<patterns.length; j++) {
          patterns[j] = "0"+d+"0"+patterns[j].substring(2); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
  }

  /**
   * Sets a minimum value for this field.
   *
   * @param min the minimum allowed value
   */
  public void setMinValue(double min) {
    if(Double.isNaN(min)) {
      minValue = null;
    } else {
      minValue = new Double(min);
    }
  }

  /**
   * Sets a maximum value for this field.
   *
   * @param max the maximum allowed value
   */
  public void setMaxValue(double max) {
    if(Double.isNaN(max)) {
      maxValue = null;
    } else {
      maxValue = new Double(max);
    }
  }

  /**
   * Sets the units.
   *
   * @param units the units
   */
  public void setUnits(String units) {
    // replace old units with new in text
    double val = getValue();
    this.units = units;
    setValue(val);
  }

  /**
   * Gets the units.
   *
   * @return units the units
   */
  public String getUnits() {
    return units;
  }
  
  public void setConversionFactor(double factor) {  	
  	conversionFactor = factor;
  	setValue(prevValue);
  }

  public double getConversionFactor() {  	
  	return conversionFactor;
  }

  /**
   * Gets the format for this field.
   *
   * @return the format
   */
  public DecimalFormat getFormat() {
    format.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
    return format;
  }

  /**
   * Sets the format for a specified value.
   *
   * @param value the value to be displayed
   */
  public void setFormatFor(double value) {
    if(fixedPattern) {
      return;
    }
    value = Math.abs(value);
    if (value==0) {
    	if (sigfigs==1) {
	      format.applyPattern(INTEGER_PATTERN);    	
    	}
    	else if (sigfigs==2) {
	      format.applyPattern(DECIMAL_1_PATTERN);    	
    	}
    	else if (sigfigs==3) {
	      format.applyPattern(DECIMAL_2_PATTERN);    	
    	}
    	else {
        format.applyPattern(DECIMAL_3_PATTERN);    	
    	}
    } else if(value<ranges[0]) {
      format.applyPattern(patterns[0]);
    } else if(value<ranges[1]) {
      format.applyPattern(patterns[1]);
    } else if(value<ranges[2]) {
      format.applyPattern(patterns[2]);
    } else if(value<ranges[3]) {
      format.applyPattern(patterns[3]);
    } else {
      format.applyPattern(patterns[4]);
    }
  }

  /**
   * Sets the patterns for this field. The patterns are applied as follows:
   * value<0.1: patterns[0]
   * value<10: patterns[1]
   * value<100: patterns[2]
   * value<1000: patterns[3]
   * value>=1000: patterns[4]
   *
   * @param patterns the desired patterns
   */
  public void setPatterns(String[] patterns) {
  	setPatterns(patterns, new double[] {0.1, 10, 100, 1000});
  }

  /**
   * Sets the patterns and limits for this field. The patterns are applied as follows:
   * value<limits[0]: patterns[0]
   * value<limits[1]: patterns[1]
   * value<limits[2]: patterns[2]
   * value<limits[3]: patterns[3]
   * value>=limits[3]: patterns[4]
   *
   * @param patterns the desired patterns
   * @param limits the limits that determine which pattern to use
   */
  public void setPatterns(String[] patterns, double[] limits) {
    if(patterns.length>4 && limits.length>3) {
      this.patterns = patterns;
      ranges = limits;
    }
  }

  /**
   * Sets a fixed user pattern.
   *
   * @param pattern the desired pattern (may be null)
   */
  public void setFixedPattern(String pattern) {
  	if (pattern==null) pattern = ""; //$NON-NLS-1$
    pattern = pattern.trim();
    if (pattern.equals(userPattern)) return;
    userPattern = pattern;
    if (userPattern.equals("")) { //$NON-NLS-1$
      fixedPattern = fixedPatternByDefault;
      setFormatFor(getValue());
    }
    else {
      fixedPattern = true;      
      format.applyPattern(userPattern);
    }
    setValue(prevValue);
  }
  
  /**
   * Gets the fixed user pattern.
   *
   * @return the pattern
   */
  public String getFixedPattern() {
    return userPattern;
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
