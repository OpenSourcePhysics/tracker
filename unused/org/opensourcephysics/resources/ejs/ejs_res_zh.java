package org.opensourcephysics.resources.ejs;
import java.io.IOException;

/**
 * Chinese resource loader for OSP display class.  Resource strings are obtained from superclass.
 * @author Wolfgang Christian
*/
public class ejs_res_zh extends ejs_res {
  /**
   * Constructor ejs_res_zh
   * @throws IOException
   */
  public ejs_res_zh() throws IOException {
    super(ejs_res.class.getResourceAsStream("ejs_res_zh_TW.properties")); //$NON-NLS-1$
  }

}