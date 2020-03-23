/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.opensourcephysics.display.OSPRuntime;

/**
 * ControlsRes provides access to internationalized string resources for OSPControls.
 *
 * @author Wolfgang Christian
 * @version 1.0
 */
public class ControlsRes {
  // static constants for speed
  public static String ANIMATION_NEW;
  public static String ANIMATION_INIT;
  public static String ANIMATION_STEP;
  public static String ANIMATION_RESET;
  public static String ANIMATION_START;
  public static String ANIMATION_STOP;
  public static String ANIMATION_RESET_TIP;
  public static String ANIMATION_INIT_TIP;
  public static String ANIMATION_START_TIP;
  public static String ANIMATION_STOP_TIP;
  public static String ANIMATION_NEW_TIP;
  public static String ANIMATION_STEP_TIP;
  public static String CALCULATION_CALC;
  public static String CALCULATION_RESET;
  public static String CALCULATION_CALC_TIP;
  public static String CALCULATION_RESET_TIP;
  public static String XML_NAME;
  public static String XML_VALUE;
  static final String BUNDLE_NAME = "org.opensourcephysics.resources.controls.controls_res"; //$NON-NLS-1$
  static ResourceBundle res;

  // private constructor because all methods are static
  private ControlsRes() {}

  static {
    String language = Locale.getDefault().getLanguage();
    Locale resourceLocale = Locale.ENGLISH;
    for(Locale locale : OSPRuntime.getInstalledLocales()) {
      if(locale.getLanguage().equals(language)) {
        resourceLocale = locale;
        break;
      }
    }
    res = ResourceBundle.getBundle(BUNDLE_NAME, resourceLocale);
    setLocalStrings();
  }

  private static String getString(final ResourceBundle bundle, final String key) {
    try {
      return bundle.getString(key);
    } catch(final MissingResourceException ex) {
      return '|'+key+'|';
    }
  }

  public static void setLocale(Locale locale) {
	if(org.opensourcephysics.js.JSUtil.isJS) return;
    res = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    setLocalStrings();
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
  * Gets the local strings.  Static strings are used for speed to avoid having to call the resource object.
  */
  private static void setLocalStrings() {
    ANIMATION_NEW = getString(res, "ANIMATION_NEW");                 //$NON-NLS-1$
    ANIMATION_INIT = getString(res, "ANIMATION_INIT");               //$NON-NLS-1$
    ANIMATION_STEP = getString(res, "ANIMATION_STEP");               //$NON-NLS-1$
    ANIMATION_RESET = getString(res, "ANIMATION_RESET");             //$NON-NLS-1$
    ANIMATION_START = getString(res, "ANIMATION_START");             //$NON-NLS-1$
    ANIMATION_STOP = getString(res, "ANIMATION_STOP");               //$NON-NLS-1$
    ANIMATION_RESET_TIP = getString(res, "ANIMATION_RESET_TIP");     //$NON-NLS-1$
    ANIMATION_INIT_TIP = getString(res, "ANIMATION_INIT_TIP");       //$NON-NLS-1$
    ANIMATION_START_TIP = getString(res, "ANIMATION_START_TIP");     //$NON-NLS-1$
    ANIMATION_STOP_TIP = getString(res, "ANIMATION_STOP_TIP");       //$NON-NLS-1$
    ANIMATION_NEW_TIP = getString(res, "ANIMATION_NEW_TIP");         //$NON-NLS-1$
    ANIMATION_STEP_TIP = getString(res, "ANIMATION_STEP_TIP");       //$NON-NLS-1$
    CALCULATION_CALC = getString(res, "CALCULATION_CALC");           //$NON-NLS-1$
    CALCULATION_RESET = getString(res, "CALCULATION_RESET");         //$NON-NLS-1$
    CALCULATION_CALC_TIP = getString(res, "CALCULATION_CALC_TIP");   //$NON-NLS-1$
    CALCULATION_RESET_TIP = getString(res, "CALCULATION_RESET_TIP"); //$NON-NLS-1$
    XML_NAME = getString(res, "XML_NAME");                           //$NON-NLS-1$
    XML_VALUE = getString(res, "XML_VALUE");                         //$NON-NLS-1$
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
