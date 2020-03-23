package org.opensourcephysics.resources.display;
import java.io.IOException;
import java.io.InputStream;
import java.util.PropertyResourceBundle;

/**
 * Default resource loader for the OSP display package.  Resources are obtained from properties.
 * 
 * Defining a Java resource class speeds up resource loading, particularly for applets because
 * a connection to the server is not required. 
 * 
 * @author Wolfgang Christian
*/
public class display_res extends PropertyResourceBundle {
  // relative path to strings
  static String res = "display_res.properties"; //$NON-NLS-1$

  /**
   * Constructor tools
   * @throws IOException
   */
  public display_res() throws IOException {
    this(display_res.class.getResourceAsStream(res));
  }

  /**
   * Constructor tools
   * @param stream
   * @throws IOException
   */
  public display_res(InputStream stream) throws IOException {
    super(stream);
  }

}