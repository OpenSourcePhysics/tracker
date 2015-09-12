/*
 * The tracker.deploy package defines classes for launching and installing Tracker.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker.deploy;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A FilenameFilter to find tracker jar files.
 *
 * @author Douglas Brown
 */
public class TrackerJarFilter implements FilenameFilter {
	
  public boolean accept(File dir, String name) {
    String fileName = getName(name).toLowerCase();
    if (!fileName.endsWith(".jar")) return false; //$NON-NLS-1$
    if (fileName.equals("tracker.jar")) return true; //$NON-NLS-1$
    if (fileName.startsWith("tracker-")) { //$NON-NLS-1$
    	// attempt to get version number
    	String version = fileName.substring(8);
    	int len = version.length();
    	version = version.substring(0, len-4); // strips ".jar"
    	try {
				Double.parseDouble(version);
      	return true;
			} catch (Exception ex1) {
	    	try {
	    		String snapshot = "-snapshot"; //$NON-NLS-1$
	    		int n = version.indexOf(snapshot);
	    		if (n>-1) {
	    			version = version.substring(0, n);
						Double.parseDouble(version);
		      	return true;
	    		}
				} catch (Exception ex2) {
				}

			}
    }
    return false;
  }
  
  /**
   * Gets the name from the specified path.
   *
   * @param path the full path
   * @return the name alone
   */
  private String getName(String path) {
    if(path==null) return ""; //$NON-NLS-1$
    int i = path.lastIndexOf("/"); //$NON-NLS-1$
    if(i==-1) i = path.lastIndexOf("\\"); //$NON-NLS-1$
    if(i!=-1) return path.substring(i+1);
    return path;
  }

}
