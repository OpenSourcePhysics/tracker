/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.display3d.core;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLLoader;

/**
 * <p>Title: Resolution</p>
 * <p>Description: A class that provides resolution style hints for 3D
 * Elements</p>
 * <p>A resolution can be of two different types: DIVISIONS or MAX_LENGTH.</p>
 * <ul>
 *   <li><b>DIVISIONS</b>: the resolution indicates how many subdivisions
 * should the element have, up to three of them: n1, n2, and n3. The precise
 * meaning of this is left to the element, but typically consists in the
 * number of divisions in each coordinate direction.</li>
 *   <li><b>MAX_LENGTH</b>: the resolution provides a maximum length
 * that each of the individual graphical pieces of the element can have.
 * The element can then automatically divide itself in smaller pieces,
 * if necessary.</li>
 * </ul>
 * @author Francisco Esquembre
 * @version March 2005
 * @see Style
 */
public class Resolution {
  static public final int DIVISIONS = 0;
  static public final int MAX_LENGTH = 1;
  private int type = DIVISIONS;
  private double maxLength = 1.0;
  private int n1 = 1;
  private int n2 = 1;
  private int n3 = 1;

  /**
   * A constructor for a resolution of type MAX_LENGTH.
   * @param max The maximum length.
   */
  public Resolution(double max) {
    this.type = MAX_LENGTH;
    this.maxLength = max;
  }

  /**
   * A constructor for a resolution of type DIVISIONS.
   * @param n1 int the first number of subdivisions
   * @param n2 int the second number of subdivisions
   * @param n3 int the thrid number of subdivisions
   */
  public Resolution(int n1, int n2, int n3) {
    this.type = DIVISIONS;
    this.n1 = n1;
    this.n2 = n2;
    this.n3 = n3;
  }

  final public int getType() {
    return this.type;
  }

  final public double getMaxLength() {
    return this.maxLength;
  }

  final public int getN1() {
    return this.n1;
  }

  final public int getN2() {
    return this.n2;
  }

  final public int getN3() {
    return this.n3;
  }

  // ----------------------------------------------------
  // XML loader
  // ----------------------------------------------------
  public static XML.ObjectLoader getLoader() {
    return new ResolutionLoader();
  }

  public static class ResolutionLoader extends XMLLoader {
    public void saveObject(XMLControl control, Object obj) {
      Resolution res = (Resolution) obj;
      control.setValue("type", res.type);            //$NON-NLS-1$
      control.setValue("max length", res.maxLength); //$NON-NLS-1$
      control.setValue("n1", res.n1);                //$NON-NLS-1$
      control.setValue("n2", res.n2);                //$NON-NLS-1$
      control.setValue("n3", res.n3);                //$NON-NLS-1$
    }

    public Object createObject(XMLControl control) {
      return new Resolution(1, 1, 1);
    }

    public Object loadObject(XMLControl control, Object obj) {
      Resolution res = (Resolution) obj;
      res.type = control.getInt("type");               //$NON-NLS-1$
      res.maxLength = control.getDouble("max length"); //$NON-NLS-1$
      res.n1 = control.getInt("n1");                   //$NON-NLS-1$
      res.n2 = control.getInt("n2");                   //$NON-NLS-1$
      res.n3 = control.getInt("n3");                   //$NON-NLS-1$
      return obj;
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
