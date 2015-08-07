/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.cabrillo.tracker.analytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.opensourcephysics.tools.Resource;

/**
 * A program to read the launch/download "uncounted" files on the server and write to a local file.
 *
 * @author Doug Brown
 * @version 1.0
 */
public class UpdateUncountedFilesApp {
	
	static String dataFile = "C:/Users/Doug/Eclipse/workspace_deploy/analytics/uncounted.txt"; //$NON-NLS-1$
	static String NEW_LINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	
  public static void main(String[] args) {
    
    // create StringBuffer and append date/time
    StringBuffer buffer = new StringBuffer();
  	buffer.append(getDateAndTime());
  	
    // append uncounted page/file names
  	String path = "http://physlets.org/tracker/installers/download.php?file=list__list"; //$NON-NLS-1$
  	String uncounted = getUncountedPages(path);
  	if (!"".equals(uncounted)) { //$NON-NLS-1$
	  	buffer.append(NEW_LINE);
	  	buffer.append(uncounted);  		
  	}
  	
  	path = "http://physlets.org/tracker/counter/counter.php?page=list_"; //$NON-NLS-1$
  	uncounted = getUncountedPages(path);
  	if (!"".equals(uncounted)) { //$NON-NLS-1$
	  	buffer.append(NEW_LINE);
	  	buffer.append(uncounted);  		
  	}

		// get current contents and add StringBuffer at end
		String contents = read(dataFile);
		contents += buffer.toString();
		
		// write the new contents to dataFile
		write(contents, dataFile);

  }
  
  /**
   * Reads a file.
   *
   * @param fileName the name of the file
   * @return the contents as a String
   */
  static String read(String fileName) {
    File file = new File(fileName);
    StringBuffer buffer = null;
    try {
      BufferedReader in = new BufferedReader(new FileReader(file));
      buffer = new StringBuffer();
      String line = in.readLine();
      while(line!=null) {
        buffer.append(line+NEW_LINE);
        line = in.readLine();
      }
      in.close();
    } catch(IOException ex) {
     }
    return buffer.toString();
  }
  
  /**
   * Writes a file.
   *
   * @param contents the contents to write
   * @param fileName the name of the file
   */
  static void write(String contents, String fileName) {
    File file = new File(fileName);
		try {
			FileOutputStream stream = new FileOutputStream(file);
			Charset charset = Charset.forName("UTF-8"); //$NON-NLS-1$
			OutputStreamWriter out = new OutputStreamWriter(stream, charset);
			BufferedWriter writer = new BufferedWriter(out);
			writer.write(contents);
			writer.flush();
			writer.close();
		} catch (IOException ex) {
		}
  }
  
  
  static String getDateAndTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm"); //$NON-NLS-1$
		Calendar cal = Calendar.getInstance();
		return sdf.format(cal.getTime());
  }
	
  static String getUncountedPages(String path) {
    try {
			URL url = new URL(path);
			Resource res = new Resource(url);
	    return res.getString().trim();
		} catch (MalformedURLException e) {
		}
  	return null;
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
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
