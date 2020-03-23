/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.controls.ControlsRes;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.dialogs.DialogsRes;

/**
 * ToolsRes provides access to string resources for OSPControls and fires a property change event
 * when the locale changes.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class ToolsRes {
  // static fields
  static final String BUNDLE_NAME = "org.opensourcephysics.resources.tools.tools"; //$NON-NLS-1$
  static Locale resourceLocale = Locale.getDefault();
  static ResourceBundle res = ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale);
  static Object resObj = new ToolsRes();
  static PropertyChangeSupport support = new SwingPropertyChangeSupport(resObj);

  /**
   * Private constructor to prevent instantiation.
   */
  private ToolsRes() {
    /** empty block */
  }

  /**
   * Gets the localized value of a string. If no localized value is found, the
   * key is returned surrounded by exclamation points.
   *
   * @param key the string to localize
   * @return the localized string
   */
  static public String getString(String key) {
    try {
      return res.getString(key);
    } catch(MissingResourceException ex) {
      return "!"+key+"!"; //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   * Gets the language for this locale.
   * @return String
   */
  public static String getLanguage() {
    return resourceLocale.getLanguage();
  }

  /**
   * Sets the locale.
   *
   * @param loc the locale
   */
  public static void setLocale(Locale loc) {	  
	if(org.opensourcephysics.js.JSUtil.isJS) return;
    if(resourceLocale==loc) {
      return;
    }
    Locale prev = resourceLocale;
    resourceLocale = loc;
    // get the new resource bundle for the tool and other OSP resource objects
    res = ResourceBundle.getBundle("org.opensourcephysics.resources.tools.tools", resourceLocale); //$NON-NLS-1$
    ControlsRes.setLocale(resourceLocale);
    DisplayRes.setLocale(resourceLocale);
    DialogsRes.setLocale(resourceLocale);
    // use reflection for EjsRes
    String className = "org.opensourcephysics.ejs.EjsRes"; //$NON-NLS-1$
    try {
			Class<?> resClass = Class.forName(className);
			Method method = resClass.getMethod("setLocale", new Class[] {Locale.class});  //$NON-NLS-1$
			method.invoke(null, new Object[] {resourceLocale});
		} catch (Exception ex) {
		}
    support.firePropertyChange("locale", prev, resourceLocale); //$NON-NLS-1$
  }

  /**
   * Adds a PropertyChangeListener.
   *
   * @param property the name of the property (only "locale" accepted)
   * @param listener the object requesting property change notification
   */
  public static void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    if(property.equals("locale")) { //$NON-NLS-1$
      support.addPropertyChangeListener(property, listener);
    }
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param property the name of the property (only "locale" accepted)
   * @param listener the listener requesting removal
   */
  public static void removePropertyChangeListener(String property, PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
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
