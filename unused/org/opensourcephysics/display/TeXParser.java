/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display;
import java.util.HashMap;
import java.util.Map;

public class TeXParser {
  public static Map<String, String> charMap = new HashMap<String, String>();

  static {
    // upper case
    charMap.put("\\Alpha", "\u0391");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Beta", "\u0392");    //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Gamma", "\u0393");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Delta", "\u0394");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Epsilon", "\u0395"); //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Zeta", "\u0396");    //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Eta", "\u0397");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Theta", "\u0398");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Pi", "\u03a0");      //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Rho", "\u03a1");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Sigma", "\u03a3");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Tau", "\u03a4");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Phi", "\u03a6");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Chi", "\u03a7");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Psi", "\u03a8");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Omega", "\u03a9");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\Xi", "\u039e");      //$NON-NLS-1$ //$NON-NLS-2$
    // lower case
    charMap.put("\\alpha", "\u03b1");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\beta", "\u03b2");    //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\gamma", "\u03b3");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\delta", "\u03b4");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\epsilon", "\u03b5"); //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\zeta", "\u03b6");    //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\eta", "\u03b7");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\theta", "\u03b8");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\iota", "\u03b9");    //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\kappa", "\u03ba");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\lamda", "\u03bb");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\lambda", "\u03bb");  //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\mu", "\u03bc");      //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\micro", "\u03bc");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\nu", "\u03bd");      //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\xi", "\u03be");      //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\pi", "\u03c0");      //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\rho", "\u03c1");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\sigma", "\u03c3");   //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\tau", "\u03c4");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\phi", "\u03c6");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\chi", "\u03c7");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\psi", "\u03c8");     //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\omega", "\u03c9");   //$NON-NLS-1$ //$NON-NLS-2$
    // special characters
    charMap.put("\\degree", "\u00b0");  //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\sqr", "\u00b2");     // superscript 2 for square //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\pm", "\u00b1");      // plus-minus //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\neq", "\u2260");     // not equal //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\leq", "\u2264");     // less than or equals //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\geq", "\u00F7");     // greater than or equals //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\div", "\u00F7");     // divide //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\perp", "\u27c2");    // perpendicular  //$NON-NLS-1$ //$NON-NLS-2$
    charMap.put("\\parallel", "\u2225");// parallel  //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Converts TeX-like notation for Greek symbols to unicode characters.
   * @param inputStr the string in TeX-like notation
   * @return
   */
  public static String parseTeX(String inputStr) {
    if(inputStr==null) {
      return null;
    }
    String[] chunks = inputStr.split("\\$"); //$NON-NLS-1$
    // boolean mathMode=(inputStr.charAt(0)=='$');
    boolean mathMode = false;
    for(int i = 0; i<chunks.length; i++) {
      if(mathMode) {
        String val = charMap.get(chunks[i].trim());
        if(val!=null) {
          chunks[i] = val;
        }
      }
      mathMode = !mathMode;
    }
    String outStr = ""; //$NON-NLS-1$
    for(int i = 0; i<chunks.length; i++) {
      outStr += chunks[i];
    }
    return outStr;
  }

  /**
   * Removes subscripting notation from an input String.
   * This returns the root plus appended subscript but without subscripting notation.
   *
   * @param input the string with (possible) subscripting notation
   * @return the string without subscripting notation
   */
  public static String removeSubscripting(String input) {
    if(input==null) {
      return null;
    }
    return removeSubscript(input)+getSubscript(input);
  }

  /**
   * Removes the subscript from an input String.
   * This returns the root only, with no subscript.
   *
   * @param input the string with (possible) subscript
   * @return the root
   */
  public static String removeSubscript(String input) {
    if(input==null) {
      return null;
    }
    int n = input.indexOf("_"); //$NON-NLS-1$
    if(n>0) {
      return input.substring(0, n);
    }
    return input;
  }

  /**
   * Gets the subscript from an input String.
   *
   * @param input the string with (possible) subscript
   * @return the subscript, or "" if none
   */
  public static String getSubscript(String input) {
    if(input==null) {
      return null;
    }
    String subscript = "";      //$NON-NLS-1$
    int n = input.indexOf("_"); //$NON-NLS-1$
    if(n>0) {
      subscript = input.substring(n+1);
      if(subscript.startsWith("{")) {   //$NON-NLS-1$
        int m = subscript.indexOf("}"); //$NON-NLS-1$
        if(m>0) {
          subscript = subscript.substring(1, m);
        }
      }
    }
    return subscript;
  }

  /**
   * Adds a subscript to the root of an input String.
   *
   * @param root the root to which the subscript will be added
   * @param subscript the subscript
   * @return the root with subscript in subscripting notation
   */
  public static String addSubscript(String root, String subscript) {
    if(root==null) {
      return null;
    }
    if(subscript==null) {
      return root;
    }
    // trim and remove braces from subscript
    subscript = subscript.trim();
  	if(subscript.startsWith("{")) { //$NON-NLS-1$
  		int n = subscript.indexOf("}"); //$NON-NLS-1$
  		if (n==-1) n = subscript.length();
      subscript = subscript.substring(1, n).trim();
    }
  	// return root if subscript is empty
    if (subscript.equals("")) { //$NON-NLS-1$
    	return root;
    }
    // if the root is already subscripted, combine the subscripts
    String prevSubscript = getSubscript(root);
    if (prevSubscript!=null) {
    	subscript = prevSubscript+subscript;
    }
    return removeSubscript(root)+"_{"+subscript+"}"; //$NON-NLS-1$ //$NON-NLS-2$
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
