/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2015  Douglas Brown
 * 
 * Tracker is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Tracker is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Tracker; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston MA 02111-1307 USA or view the license online at
 * <http://www.gnu.org/copyleft/gpl.html>
 * 
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * String resources for tracker classes.
 * 
 * @author Douglas Brown
 * @version 1.0
 */

public class TrackerRes {

	// static fields
	static Locale locale = Locale.getDefault();
	static ResourceBundle res = ResourceBundle.getBundle(
					"org.opensourcephysics.cabrillo.tracker.resources.tracker",  //$NON-NLS-1$
					locale);
	static Object resObj = new TrackerRes();
  static PropertyChangeSupport support = new SwingPropertyChangeSupport(resObj);

	/**
	 * Private constructor to prevent instantiation.
	 */
	private TrackerRes() {/** empty block */}

	/**
	 * Gets the localized value of a string. If no localized value is found, the
	 * key is returned surrounded by exclamation points.
	 * 
	 * @param key the string to localize
	 * @return the localized string
	 */
	public static String getString(String key) {
		try {
			return res.getString(key);
		}
		catch (MissingResourceException ex) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

  /**
   * Sets the locale.
   *
   * @param loc the locale
   */
	public static void setLocale(Locale loc) {
		if (locale == loc) return;
		Locale prev = locale;
		locale = loc;
		// get the new resource bundle
		res = ResourceBundle.getBundle(
						"org.opensourcephysics.cabrillo.tracker.resources.tracker",  //$NON-NLS-1$
						locale);
		org.opensourcephysics.media.core.MediaRes.setLocale(loc);
		org.opensourcephysics.controls.ControlsRes.setLocale(loc);
		org.opensourcephysics.tools.ToolsRes.setLocale(loc);
		support.firePropertyChange("locale", prev, locale); //$NON-NLS-1$
	}
	
  /**
   * Adds a PropertyChangeListener.
   *
   * @param property the name of the property (only "locale" accepted) 
   * @param listener the object requesting property change notification
   */
	public static void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    if (property.equals("locale")) //$NON-NLS-1$
    	support.addPropertyChangeListener(property, listener);
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
