/*
 * The tracker package defines a set of video/image analysis tools built on the
 * Open Source Physics framework by Wolfgang Christian.
 * 
 * Copyright (c) 2021 Douglas Brown, Wolfgang Christian, Robert M. Hanson
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
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.swing.event.SwingPropertyChangeSupport;

import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.ResourceLoader.Bundle;

/**
 * String resources for tracker classes.
 * 
 * @author Douglas Brown
 * @version 1.0
 */

public class TrackerRes {

	public static final String PROPERTY_TRACKERRES_LOCALE = "locale"; //$NON-NLS-1$
	
	// static fields
	static Locale locale = Locale.getDefault();
	// BH 2020.04.13 using explicit FORMAT_PROPERTIES here to avoid three attempts
	// to load .js files
	static Bundle res = ResourceLoader.getBundle(
					"org.opensourcephysics.cabrillo.tracker.resources.tracker",  //$NON-NLS-1$
					locale);

	static PropertyChangeSupport support = new SwingPropertyChangeSupport(new TrackerRes());

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
		res = ResourceLoader.getBundle(
						"org.opensourcephysics.cabrillo.tracker.resources.tracker",  //$NON-NLS-1$
						locale);
		try {
		org.opensourcephysics.media.core.MediaRes.setLocale(loc);
		org.opensourcephysics.controls.ControlsRes.setLocale(loc);
		org.opensourcephysics.tools.ToolsRes.setLocale(loc);
		support.firePropertyChange(PROPERTY_TRACKERRES_LOCALE, prev, locale); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
  /**
   * Adds a PropertyChangeListener.
   *
   * @param property the name of the property (only "locale" accepted) 
   * @param listener the object requesting property change notification
   */
	public static void addPropertyChangeListener(String property, PropertyChangeListener listener) {
    if (property.equals(PROPERTY_TRACKERRES_LOCALE)) 
    	support.addPropertyChangeListener(property, listener);
  }

  /**
   * Removes a PropertyChangeListener.
   *
   * @param listener the listener requesting removal
   */
	public static void removePropertyChangeListener(String property, PropertyChangeListener listener) {
    support.removePropertyChangeListener(property, listener);
  }
}
