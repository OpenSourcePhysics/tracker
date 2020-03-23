/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.display.ResizableIcon;

/**
 * A class for setting relative font sizes by level.
 *
 * @author dobrown
 * @version 1.0
 */
public class FontSizer {
	
  /** Base font for text fields */
  public static final Font TEXT_FONT = new JTextField().getFont();

  /** Base font for buttons */
  public static final Font BUTTON_FONT = new JButton().getFont();

  /** Base font for menu accelerators */
  public static final Font ACCELERATOR_FONT = (Font)UIManager.get("MenuItem.acceleratorFont"); //$NON-NLS-1$

  /** Icons for checkbox, radio button and arrow menu items */
  public static final ResizableIcon CHECKBOXMENUITEM_ICON;
  public static final ResizableIcon RADIOBUTTONMENUITEM_ICON;
  public static final ResizableIcon CHECKBOX_ICON;
  public static final ResizableIcon RADIOBUTTON_ICON;
  public static final ResizableIcon ARROW_ICON;

  /** maximum font level */
  public static final int MAX_LEVEL = 9;

  // static fields
  static Object levelObj = new FontSizer();
  static PropertyChangeSupport support = new SwingPropertyChangeSupport(levelObj);
  static int level, integerFactor;
  static double levelFactor = 1.25; // size ratio per level
  static double factor;
  static Map<Font, Font> fontMap = new HashMap<Font, Font>();
  
  static {
	  Icon baseIcon = (Icon)UIManager.get("CheckBoxMenuItem.checkIcon"); //$NON-NLS-1$
	  CHECKBOXMENUITEM_ICON = new ResizableIcon(baseIcon);
  	UIManager.put("CheckBoxMenuItem.checkIcon", CHECKBOXMENUITEM_ICON); //$NON-NLS-1$
	  baseIcon = (Icon)UIManager.get("CheckBox.icon"); //$NON-NLS-1$
	  CHECKBOX_ICON = new ResizableIcon(baseIcon);
  	UIManager.put("CheckBox.icon", CHECKBOX_ICON); //$NON-NLS-1$
  	baseIcon = (Icon)UIManager.get("RadioButtonMenuItem.checkIcon"); //$NON-NLS-1$
  	RADIOBUTTONMENUITEM_ICON = new ResizableIcon(baseIcon);
  	UIManager.put("RadioButtonMenuItem.checkIcon", RADIOBUTTONMENUITEM_ICON); //$NON-NLS-1$
  	baseIcon = (Icon)UIManager.get("RadioButton.icon"); //$NON-NLS-1$
  	RADIOBUTTON_ICON = new ResizableIcon(baseIcon);
  	UIManager.put("RadioButton.icon", RADIOBUTTON_ICON); //$NON-NLS-1$
  	baseIcon = (Icon)UIManager.get("Menu.arrowIcon"); //$NON-NLS-1$
  	ARROW_ICON = new ResizableIcon(baseIcon);
  	UIManager.put("Menu.arrowIcon", ARROW_ICON); //$NON-NLS-1$
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private FontSizer() {
    /** empty block */
  }

  /**
   * Sets the font level and informs all listeners.
   *
   * @param n a non-negative integer level
   */
  public static void setLevel(int n) {
  	n = Math.min(n, MAX_LEVEL);
    level = Math.max(n, 0);
    factor = getFactor(level);
    integerFactor = getIntegerFactor(level);
    
    CHECKBOXMENUITEM_ICON.resize(integerFactor);
    CHECKBOX_ICON.resize(integerFactor);
    ARROW_ICON.resize(integerFactor);
    RADIOBUTTONMENUITEM_ICON.resize(integerFactor);
    RADIOBUTTON_ICON.resize(integerFactor);
        
    Font font = getResizedFont(TEXT_FONT, level);    
    UIManager.put("OptionPane.messageFont", font); //$NON-NLS-1$
    UIManager.put("TextField.font", font); //$NON-NLS-1$
    UIManager.put("ToolTip.font", font);     //$NON-NLS-1$
    UIManager.put("TabbedPane.font", font);     //$NON-NLS-1$

    font = getResizedFont(ACCELERATOR_FONT, level);    
    UIManager.put("MenuItem.acceleratorFont", font);     //$NON-NLS-1$
        
  	font= getResizedFont(BUTTON_FONT, level);    
    UIManager.put("OptionPane.buttonFont", font); //$NON-NLS-1$
    
    support.firePropertyChange("level", null, new Integer(level)); //$NON-NLS-1$
  }

  /**
   * Gets the current font level.
   *
   * @return the level
   */
  public static int getLevel() {
    return level;
  }

  /**
   * Increments the font level.
   */
  public static void levelUp() {
    setLevel(level+1);
  }

  /**
   * Decrements the font level.
   */
  public static void levelDown() {
    setLevel(level-1);
  }

  /**
   * Gets the current factor.
   *
   * @return the factor
   */
  public static double getFactor() {
    return factor;
  }

  /**
   * Gets the current integer factor.
   *
   * @return the integer factor
   */
  public static int getIntegerFactor() {
  	return integerFactor;
  }

  /**
   * Sets the fonts of an object to a specified level.
   *
   * @param obj the object
   * @param level the level
   */
  public static void setFonts(Object obj, int level) {
  	if (obj instanceof Object[]) {
  		for (Object next: ((Object[])obj)) {
  			setFonts(next, level);
  		}
  		return;
  	}
  	if (obj instanceof Collection) {
  		for (Object next: ((Collection<?>)obj)) {
  			setFonts(next, level);
  		}
  		return;
  	}
    double factor = getFactor(level);
    if(obj instanceof Container) {
      setFontFactor((Container) obj, factor);
    } else if(obj instanceof TitledBorder) {
      setFontFactor((TitledBorder) obj, factor);
    } else if(obj instanceof Component) {
      setFontFactor((Component) obj, factor);
    }
  }

  /**
   * Resizes a font to a specified level.
   *
   * @param font the font
   * @param level the level
   * @return the resized font
   */
  public static Font getResizedFont(Font font, int level) {
    return getResizedFont(font, getFactor(level));
  }

  /**
   * Resizes a font by a specified factor.
   *
   * @param font the font
   * @param factor the factor
   * @return the resized font
   */
  public static Font getResizedFont(Font font, double factor) {
    if(font==null) {
      return null;
    }
    // get base font for this font
    Font base = fontMap.get(font);
    if(base==null) {
      base = font;
      fontMap.put(font, base);
    }
    // derive new font from base
    float size = (float) (base.getSize()*factor);
    font = base.deriveFont(size);
    fontMap.put(font, base);
    return font;
  }

  /**
   * Gets the factor corresponding to a specified level.
   *
   * @param level the level
   * @return the factor
   */
  public static double getFactor(int level) {
    // get font size factor (= levelFactor^level)
    double factor = 1.0;
    for(int i = 0; i<level; i++) {
      factor *= levelFactor;
    }
    return factor;
  }

  /**
   * Gets the integer factor corresponding to a specified level.
   *
   * @param level the level
   * @return the integer factor
   */
  public static int getIntegerFactor(int level) {
  	return Math.round(Math.round(getFactor(level)));
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param property the name of the property (only "level" accepted)
   * @param listener the object requesting property change notification
   */
  public static void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    if(property.equals("level")) { //$NON-NLS-1$
      support.addPropertyChangeListener(property, listener);
    }
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param property the name of the property (only "level" accepted)
   * @param listener the listener requesting removal
   */
  public static void removePropertyChangeListener(String property, PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
  }

  //_______________________________ private methods ____________________________

  /**
   * Increases a container's normal font sizes by the specified factor.
   *
   * @param c a container
   * @param factor the factor
   */
  private static void setFontFactor(Container c, double factor) {
    // get resized container font
    Font font = getResizedFont(c.getFont(), factor);
    // Added by Paco. This must be here because JComponent inherits from Container
    if (c instanceof javax.swing.JComponent) {
      Border border = ((javax.swing.JComponent) c).getBorder();
      if(border instanceof TitledBorder) {
        setFontFactor((TitledBorder) border, factor);
      }
    }
    // End of added by Paco
    if (c instanceof JMenu) {
      setMenuFont((JMenu) c, font);
    } 
    else {
      c.setFont(font);
      // iterate through child components
      for(int i = 0; i<c.getComponentCount(); i++) {
        Component co = c.getComponent(i);
        if((co instanceof Container)) {
          setFontFactor((Container) co, factor);
        } else {
          setFontFactor(co, factor);
        }
      }
    }
    // added by Doug Brown June 2015 to resize icons along with fonts
    if (c instanceof JButton) {
    	JButton button = (JButton)c;
    	for (int i=0; i<5; i++) {
    		Icon icon = i==0? button.getIcon(): 
    			i==2? button.getSelectedIcon(): 
      		i==3? button.getRolloverIcon(): 
        	i==4? button.getRolloverSelectedIcon():
        	button.getDisabledIcon();
  			if (icon!=null && icon instanceof ResizableIcon) {
  				((ResizableIcon)icon).resize(getIntegerFactor());
  			}
    	}
    }
    else if (c instanceof JMenu) {
    	JMenu menu = (JMenu)c;
    	Icon icon = menu.getIcon();
			if (icon!=null && icon instanceof ResizableIcon) {
				((ResizableIcon)icon).resize(getIntegerFactor());
			}
    	for (int i=0; i< menu.getItemCount(); i++) {
    		JMenuItem item = menu.getItem(i);
    		if (item!=null) {
    			setFontFactor(item, factor);
    		}
    	}
    }
    else if (c instanceof AbstractButton)  {
    	// BH SwingJS "getIcon" is a method in AbstractButton, JFileChooser, JOptionPane, 
    	// JLabel, and DefaultTreeCellRenderer. 
    	// It is fair to assume we can just check for AbstractButton?
    	// Otherwise we are depending upon throwing an Exception, which is
    	// OK, but not really good form. 
    	Icon icon = ((AbstractButton) c).getIcon();
// formerly:
//			Method m = c.getClass().getMethod("getIcon", (Class<?>[])null); //$NON-NLS-1$
//			if (m != null) {
//				Icon icon = (Icon)m.invoke(c, (Object[])null);
				if (icon!=null && icon instanceof ResizableIcon) {
					((ResizableIcon)icon).resize(getIntegerFactor());
				}
			}
//		} catch (Exception e) {
//		}
    // end of code added by Doug

    c.repaint();
  }

  /**
   * Increases a component's normal font sizes by the specified factor.
   *
   * @param c a component
   * @param factor the factor
   */
  private static void setFontFactor(Component c, double factor) {
    // get resized component font
    Font font = getResizedFont(c.getFont(), factor);
    c.setFont(font);
    // Added by Paco
    if (c instanceof javax.swing.JComponent) {
      Border border = ((javax.swing.JComponent) c).getBorder();
      if(border instanceof TitledBorder) {
        setFontFactor((TitledBorder) border, factor);
      }
    }
    // End of added by Paco
  }

  /**
   * Increases a titled border's normal font size by the specified factor.
   *
   * @param b a titled border
   * @param factor the factor
   */
  private static void setFontFactor(TitledBorder b, double factor) {
    // get resized border font
  	Font font = b.getTitleFont();
  	if (font==null) {
  		font = UIManager.getFont("TitledBorder.font"); //$NON-NLS-1$
  	}
    font = getResizedFont(font, factor);
    b.setTitleFont(font);
  }

  /**
   * Sets the menu font.
   *
   * @param m a menu
   * @param font the font
   */
  private static void setMenuFont(JMenu m, Font font) {
    m.setFont(font);
    for(int i = 0; i<m.getMenuComponentCount(); i++) {
      m.getMenuComponent(i).setFont(font);
      if(m.getMenuComponent(i) instanceof JMenu) {
        setMenuFont((JMenu) m.getMenuComponent(i), font);
      }
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
