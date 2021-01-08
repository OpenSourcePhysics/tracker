package org.opensourcephysics.media.xuggle;

import java.util.Locale;
import java.util.MissingResourceException;

import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.ResourceLoader.Bundle;

/**
 * XuggleRes provides access to string resources for Xuggle Diagnostics.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class XuggleRes {
	// static fields
	// BH keeping this simple by leaving the resources in their old place.
	static final String BUNDLE_NAME = "org.opensourcephysics.resources.xuggle.xuggle"; //$NON-NLS-1$
	static Locale resourceLocale = Locale.ENGLISH;
	static Bundle res = ResourceLoader.getBundle(BUNDLE_NAME, resourceLocale);
	static {
		String language = Locale.getDefault().getLanguage();
		for (Locale locale : OSPRuntime.getInstalledLocales()) {
			if (locale.getLanguage().equals(language)) {
				resourceLocale = locale;
				break;
			}
		}
		res = ResourceLoader.getBundle(BUNDLE_NAME, resourceLocale);
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private XuggleRes() {
		/** empty block */
	}

	/**
	 * Gets the localized value of a string. If no localized value is found, the key
	 * is returned surrounded by exclamation points.
	 *
	 * @param key the string to localize
	 * @return the localized string
	 */
	static public String getString(String key) {
		try {
			return res.getString(key);
		} catch (MissingResourceException ex) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gets the language for this locale.
	 * 
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
		if (resourceLocale == loc) {
			return;
		}
		resourceLocale = loc;
		// get the new resource bundle
		res = ResourceLoader.getBundle(BUNDLE_NAME, resourceLocale); // $NON-NLS-1$
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
 * Copyright (c) 2021  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
