package org.opensourcephysics.resources.ejs;
import java.io.IOException;

/**
 * Spanish resource loader for OSP display class.  Resource strings are obtained from properites file.
 * @author Wolfgang Christian
*/
public class ejs_res_es extends ejs_res {
  /**
   * Constructor ejs_res_es
   * @throws IOException
   */
  public ejs_res_es() throws IOException {
    super(ejs_res.class.getResourceAsStream("ejs_res_es.properties")); //$NON-NLS-1$
  }

}