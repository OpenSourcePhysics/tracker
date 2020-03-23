/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.desktop.ostermiller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Vector;

/**
 * A stripped down version of the Browser class published by Stephen Ostermiller for use by the OSP project.
 *
 * Allows URLs to be opened in the system browser on Windows and Unix.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/Browser.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class Browser {
  /**
   * A list of commands to try in order to display the url.
   * The url is put into the command using MessageFormat, so
   * the URL will be specified as {0} in the command.
   * Some examples of commands to try might be:<br>
   * <code>rundll32 url.dll,FileProtocolHandler {0}</code></br>
   * <code>netscape {0}</code><br>
   * These commands are passed in order to exec until something works
   * when displayURL is used.
   *
   * @since ostermillerutils 1.00.00
   */
  public static String[] exec = null;

  /**
   * Determine appropriate commands to start a browser on the current
   * operating system.  On windows: <br>
   * <code>rundll32 url.dll,FileProtocolHandler {0}</code></br>
   * On other operating systems, the "which" command is used to
   * test if Mozilla, netscape, and lynx(xterm) are available (in that
   * order).
   *
   * @since ostermillerutils 1.00.00
   */
  public static void init() {
    exec = defaultCommands();
  }

  /**
   * Retrieve the default commands to open a browser for this system.
   *
   * @since ostermillerutils 1.00.00
   */
  public static String[] defaultCommands() {
    String[] exec = null;
    if(System.getProperty("os.name").startsWith("Windows")) {      //$NON-NLS-1$ //$NON-NLS-2$
      exec = new String[] {
        "rundll32 url.dll,FileProtocolHandler {0}",                //$NON-NLS-1$
      };
    } else if(System.getProperty("os.name").startsWith("Mac")) {   //$NON-NLS-1$ //$NON-NLS-2$
      Vector<String> browsers = new Vector<String>();
      try {
        Process p = Runtime.getRuntime().exec("which open");       //$NON-NLS-1$
        if(p.waitFor()==0) {
          browsers.add("open {0}");                                //$NON-NLS-1$
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      if(browsers.size()>0) {
        exec = browsers.toArray(new String[0]);
      }
    } else if(System.getProperty("os.name").startsWith("SunOS")) { //$NON-NLS-1$ //$NON-NLS-2$
      exec = new String[] {"/usr/dt/bin/sdtwebclient {0}"};        //$NON-NLS-1$
    } else {
      Vector<String> browsers = new Vector<String>();
      try {
        Process p = Runtime.getRuntime().exec("which firebird");   //$NON-NLS-1$
        if(p.waitFor()==0) {
          browsers.add("firebird -remote openURL({0})");           //$NON-NLS-1$
          browsers.add("firebird {0}");                            //$NON-NLS-1$
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      try {
        Process p = Runtime.getRuntime().exec("which mozilla");    //$NON-NLS-1$
        if(p.waitFor()==0) {
          browsers.add("mozilla -remote openURL({0})");            //$NON-NLS-1$
          browsers.add("mozilla {0}");                             //$NON-NLS-1$
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      try {
        Process p = Runtime.getRuntime().exec("which opera");      //$NON-NLS-1$
        if(p.waitFor()==0) {
          browsers.add("opera -remote openURL({0})");              //$NON-NLS-1$
          browsers.add("opera {0}");                               //$NON-NLS-1$
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      try {
        Process p = Runtime.getRuntime().exec("which galeon");     //$NON-NLS-1$
        if(p.waitFor()==0) {
          browsers.add("galeon {0}");                              //$NON-NLS-1$
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      try {
        Process p = Runtime.getRuntime().exec("which konqueror");  //$NON-NLS-1$
        if(p.waitFor()==0) {
          browsers.add("konqueror {0}");                           //$NON-NLS-1$
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      try {
        Process p = Runtime.getRuntime().exec("which netscape");   //$NON-NLS-1$
        if(p.waitFor()==0) {
          browsers.add("netscape -remote openURL({0})");           //$NON-NLS-1$
          browsers.add("netscape {0}");                            //$NON-NLS-1$
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      try {
        Process p = Runtime.getRuntime().exec("which xterm");      //$NON-NLS-1$
        if(p.waitFor()==0) {
          p = Runtime.getRuntime().exec("which lynx");             //$NON-NLS-1$
          if(p.waitFor()==0) {
            browsers.add("xterm -e lynx {0}");                     //$NON-NLS-1$
          }
        }
      } catch(IOException e) {}
      catch(InterruptedException e) {}
      if(browsers.size()>0) {
        exec = browsers.toArray(new String[0]);
      }
    }
    return exec;
  }

  /**
   * Display a URL in the system browser.
   *
   * Browser.init() should be called before calling this function or
   * Browser.exec should be set explicitly.
   *
   * For security reasons, the URL will may not be passed directly to the
   * browser as it is passed to this method.  The URL may be made safe for
   * the exec command by URLEncoding the URL before passing it.
   *
   * @param url the url to display
   * @throws IOException if the url is not valid or the browser fails to star
   *
   * @since ostermillerutils 1.00.00
   */
  public static void displayURL(String url) throws IOException {
    if((exec==null)||(exec.length==0)) {
      if(System.getProperty("os.name").startsWith("Mac")) {                                             //$NON-NLS-1$ //$NON-NLS-2$
        boolean success = false;
        try {
          Class<?> nSWorkspace;
          if(new File("/System/Library/Java/com/apple/cocoa/application/NSWorkspace.class").exists()) { //$NON-NLS-1$
            // Mac OS X has NSWorkspace, but it is not in the classpath, add it.
            //ClassLoader classLoader = new URLClassLoader(new URL[]{new File("/System/Library/Java").toURL()}); //$NON-NLS-1$
            ClassLoader classLoader = new URLClassLoader(new URL[] {new File("/System/Library/Java").toURI().toURL()}); //$NON-NLS-1$
            nSWorkspace = Class.forName("com.apple.cocoa.application.NSWorkspace", true, classLoader);    //$NON-NLS-1$
          } else {
            nSWorkspace = Class.forName("com.apple.cocoa.application.NSWorkspace");                       //$NON-NLS-1$
          }
          Method sharedWorkspace = nSWorkspace.getMethod("sharedWorkspace", new Class[] {});              //$NON-NLS-1$
          Object workspace = sharedWorkspace.invoke(null, new Object[] {});
          Method openURL = nSWorkspace.getMethod("openURL", new Class[] {Class.forName("java.net.URL")}); //$NON-NLS-1$ //$NON-NLS-2$
          success = ((Boolean) openURL.invoke(workspace, new Object[] {new java.net.URL(url)})).booleanValue();
          //success = com.apple.cocoa.application.NSWorkspace.sharedWorkspace().openURL(new java.net.URL(url));
        } catch(Exception x) {}
        if(!success) {
          try {
            Class<?> mrjFileUtils = Class.forName("com.apple.mrj.MRJFileUtils");                                 //$NON-NLS-1$
            Method openURL = mrjFileUtils.getMethod("openURL", new Class[] {Class.forName("java.lang.String")}); //$NON-NLS-1$ //$NON-NLS-2$
            openURL.invoke(null, new Object[] {url});
            //com.apple.mrj.MRJFileUtils.openURL(url);
          } catch(Exception x) {
            System.err.println(x.getMessage());
            throw new IOException("Browser launch failed.");           //$NON-NLS-1$
          }
        }
      } else {
        throw new IOException("Browser execute command not defined."); //$NON-NLS-1$
      }
    } else {
      // for security, see if the url is valid.
      // this is primarily to catch an attack in which the url
      // starts with a - to fool the command line flags, bu
      // it could catch other stuff as well, and will throw a
      // MalformedURLException which will give the caller of this
      // function useful information.
      new URL(url);
      // escape any weird characters in the url.  This is primarily
      // to prevent an attacker from putting in spaces
      // that might fool exec into allowing
      // the attacker to execute arbitrary code.
      StringBuffer sb = new StringBuffer(url.length());
      for(int i = 0; i<url.length(); i++) {
        char c = url.charAt(i);
        if(((c>='a')&&(c<='z'))||((c>='A')&&(c<='Z'))||((c>='0')&&(c<='9'))||(c=='.')||(c==':')||(c=='&')||(c=='@')||(c=='/')||(c=='?')||(c=='%')||(c=='+')||(c=='=')||(c=='#')||(c=='-')||(c=='\\')) {
          //characters that are necessary for URLs and should be safe
          //to pass to exec.  Exec uses a default string tokenizer with
          //the default arguments (whitespace) to separate command line
          //arguments, so there should be no problem with anything bu
          //whitespace.
          sb.append(c);
        } else {
          c = (char) (c&0xFF);                                                                           // get the lowest 8 bits (URLEncoding)
          if(c<0x10) {
            sb.append("%0"+Integer.toHexString(c));                                                      //$NON-NLS-1$
          } else {
            sb.append("%"+Integer.toHexString(c));                                                       //$NON-NLS-1$
          }
        }
      }
      String[] messageArray = new String[1];
      messageArray[0] = sb.toString();
      String command = null;
      boolean found = false;
      // try each of the exec commands until something works
      try {
        for(int i = 0; (i<exec.length)&&!found; i++) {
          try {
            // stick the url into the command
            command = MessageFormat.format(exec[i], (Object[]) messageArray);
            // parse the command line.
            Vector<String> argsVector = new Vector<String>();
            BrowserCommandLexer lex = new BrowserCommandLexer(new StringReader(command));
            String t;
            while((t = lex.getNextToken())!=null) {
              argsVector.add(t);
            }
            String[] args = new String[argsVector.size()];
            args = argsVector.toArray(args);
            // the windows url protocol handler doesn't work well with file URLs.
            // Correct those problems here before continuing
            // Java File.toURL() gives only one / following file: bu
            // we need two.
            // If there are escaped characters in the url, we will have
            // to create an Internet shortcut and open that, as the command
            // line version of the rundll doesn't like them.
            if(args[0].equals("rundll32")&&args[1].equals("url.dll,FileProtocolHandler")) {              //$NON-NLS-1$ //$NON-NLS-2$
              if(args[2].startsWith("file:/")) {                                                         //$NON-NLS-1$
                if(args[2].charAt(6)!='/') {
                  args[2] = "file://"+args[2].substring(6);                                              //$NON-NLS-1$
                }
                if(args[2].charAt(7)!='/') {
                  args[2] = "file:///"+args[2].substring(7);                                             //$NON-NLS-1$
                }
              } else if(args[2].toLowerCase().endsWith("html")||args[2].toLowerCase().endsWith("htm")) { //$NON-NLS-1$ //$NON-NLS-2$
              }
            }
            // start the browser
            Process p = Runtime.getRuntime().exec(args);
            // give the browser a bit of time to fail.
            // I have found that sometimes sleep doesn't work
            // the first time, so do it twice.  My tests
            // seem to show that 1000 milliseconds is enough
            // time for the browsers I'm using.
            for(int j = 0; j<2; j++) {
              try {
                Thread.sleep(1000);
              } catch(InterruptedException inte) {}
            }
            if(p.exitValue()==0) {
              // this is a weird case.  The browser exited after
              // a couple seconds saying that it successfully
              // displayed the url.  Either the browser is lying
              // or the user closed it *really* quickly.  Oh well.
              found = true;
            }
          } catch(IOException x) {
            // the command was not a valid command.
            System.err.println("Warning: "+x.getMessage());                 //$NON-NLS-1$
          }
        }
        if(!found) {
          // we never found a command that didn't terminate with an error.
          throw new IOException("Browser launch failed.");                  //$NON-NLS-1$
        }
      } catch(IllegalThreadStateException e) {
        // the browser is still running.  This is a good sign.
        // lets just say that it is displaying the url right now!
      }
    }
  }

  /**
   * Display the URLs, each in their own window, in the system browser.
   *
   * Browser.init() should be called before calling this function or
   * Browser.exec should be set explicitly.
   *
   * If more than one URL is given an HTML page containing JavaScript will
   * be written to the local drive, that page will be opened, and it will
   * open the rest of the URLs.
   *
   * @param urls the list of urls to display
   * @throws IOException if the url is not valid or the browser fails to star
   *
   * @since ostermillerutils 1.00.00
   */
  public static void displayURLs(String[] urls) throws IOException {
    if((urls==null)||(urls.length==0)) {
      return;
    }
    if(urls.length==1) {
      displayURL(urls[0]);
      return;
    }
    File shortcut = File.createTempFile("DisplayURLs", ".html"); //$NON-NLS-1$ //$NON-NLS-2$
    shortcut = shortcut.getCanonicalFile();
    shortcut.deleteOnExit();
    PrintWriter out = new PrintWriter(new FileWriter(shortcut));
    out.println("<!-- saved from url=(0014)about:internet -->");              //$NON-NLS-1$
    out.println("<html>");                                                    //$NON-NLS-1$
    out.println("<head>");                                                    //$NON-NLS-1$
    out.println("<title> Open URLs </title>");                                //$NON-NLS-1$
    out.println("<script language=\"javascript\" type=\"text/javascript\">"); //$NON-NLS-1$
    out.println("function displayURLs(){");                                   //$NON-NLS-1$
    for(int i = 1; i<urls.length; i++) {
      out.println("window.open(\""+urls[i]+"\", \"_blank\", \"toolbar=yes,location=yes,directories=yes,status=yes,menubar=yes,scrollbars=yes,resizable=yes\");"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    out.println("location.href=\""+urls[0]+"\";");             //$NON-NLS-1$ //$NON-NLS-2$
    out.println("}");                                          //$NON-NLS-1$
    out.println("</script>");                                  //$NON-NLS-1$
    out.println("</head>");                                    //$NON-NLS-1$
    out.println("<body onload=\"javascript:displayURLs()\">"); //$NON-NLS-1$
    out.println("<noscript>");                                 //$NON-NLS-1$
    for(int i = 0; i<urls.length; i++) {
      out.println("<a target=\"_blank\" href=\""+urls[i]+"\">"+urls[i]+"</a><br>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    out.println("</noscript>"); //$NON-NLS-1$
    out.println("</body>");     //$NON-NLS-1$
    out.println("</html>");     //$NON-NLS-1$
    out.close();
    displayURL(shortcut.toURI().toURL().toString());
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
