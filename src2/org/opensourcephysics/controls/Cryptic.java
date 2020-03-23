/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * A class to represent an encrypted version of a UTF-8-encoded String.
 *
 * @author Doug Brown
 * @version 1.0 May 2006
 */
public class Cryptic {
  // static fields
  private static String encoding = "UTF-8";             //$NON-NLS-1$
  private static String keyFormat = "PBEWithMD5AndDES"; //$NON-NLS-1$
  private static byte[] salt = {(byte) 0x09, (byte) 0x9C, (byte) 0xC8, (byte) 0x23, (byte) 0x1E, (byte) 0xAA, (byte) 0xB3, (byte) 0x41};
  private static int interactions = 19;
  private static final String DEFAULT_PW = "ospWCMBACBJDB"; //$NON-NLS-1$
  // instance fields
  private String cryptic;                                   // the incrypted form of the input

  /**
   * Protected no-arg constructor has null cryptic.
   */
  protected Cryptic() {

  /** empty block */
  }

  /**
   * Public constructor with input string.
   *
   * @param input UTF-8 String to encrypt
   */
  public Cryptic(String input) {
    encrypt(input);
  }

  /**
   * Public constructor with input and password.
   *
   * @param input UTF-8 String to encrypt
   * @param password
   */
  public Cryptic(String input, String password) {
    encrypt(input, password);
  }

  /**
   * Encrypts the input and saves in cryptic form.
   *
   * @param input UTF-8 String to encrypt
   * @return the encrypted content
   */
  public String encrypt(String content) {
    return encrypt(content, DEFAULT_PW);
  }

  /**
   * Encrypts the input with a password and saves in cryptic form.
   *
   * @param input UTF-8 String to encrypt
   * @return the encrypted content
   */
  public String encrypt(String content, String password) {
    try {
      // create the key and parameter spec
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, interactions);
      SecretKey key = SecretKeyFactory.getInstance(keyFormat).generateSecret(keySpec);
      AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, interactions);
      // create the cipher
      Cipher ecipher = Cipher.getInstance(key.getAlgorithm());
      ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
      // get byte[] from content and encrypt with cipher
      byte[] bytes = content.getBytes(encoding);
      byte[] enc = ecipher.doFinal(bytes);
      // save encrypted bytes as string of chars 0-63
      // note this doubles the string length
      cryptic = new String(Base64Coder.encode(enc));
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return cryptic;
  }

  /**
   * Gets the decrypted string.
   * @return the decrypted string
   */
  public String decrypt() {
    return decrypt(DEFAULT_PW);
  }

  /**
   * Gets the decrypted string using a password.
   *
   * @param password the password
   * @return the decrypted string
   */
  public String decrypt(String password) {
    try {
      // create the key and parameter spec
      KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, interactions);
      SecretKey key = SecretKeyFactory.getInstance(keyFormat).generateSecret(keySpec);
      AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, interactions);
      // create the cipher
      Cipher dcipher = Cipher.getInstance(key.getAlgorithm());
      dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
      byte[] dec = null;
      try {
        dec = Base64Coder.decode(cryptic);
      } catch(IllegalArgumentException ex) {
        // decode legacy files encoded with sun encoder
        //dec = new sun.misc.BASE64Decoder().decodeBuffer(cryptic);
    	return null;
      }
      byte[] bytes = dcipher.doFinal(dec);
      return new String(bytes, encoding);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Gets the cryptic.
   * @return the encrypted version of the input
   */
  public String getCryptic() {
    return cryptic;
  }

  /**
   * Sets the cryptic.
   * @param encrypted an encrypted string
   */
  public void setCryptic(String encrypted) {
    cryptic = encrypted;
  }

  /**
   * Returns an ObjectLoader to save and load data for this class.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {
    /**
     * Saves an object's data to an XMLControl.
     *
     * @param control the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl control, Object obj) {
      Cryptic cryptic = (Cryptic) obj;
      control.setValue("cryptic", cryptic.getCryptic()); //$NON-NLS-1$
    }

    /**
     * Creates a new object.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new Cryptic();
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      Cryptic cryptic = (Cryptic) obj;
      cryptic.setCryptic(control.getString("cryptic")); //$NON-NLS-1$
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
