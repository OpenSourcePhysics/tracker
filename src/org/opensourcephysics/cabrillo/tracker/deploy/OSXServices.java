package org.opensourcephysics.cabrillo.tracker.deploy;

import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.opensourcephysics.cabrillo.tracker.TFrame;
import org.opensourcephysics.cabrillo.tracker.Tracker;

/**
 * A class to handle apple events in OSX.
 *
 * @author Douglas Brown
 */
public class OSXServices {
	
	String status;

	Tracker tracker;
	ProxyHandler proxy;
	
	public String getStatus() {		
		return status;
	}
	
	/**
	 * Constructor used by TrackerStarter, which uses only the OpenFileHandler
	 */
	public OSXServices() {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		proxy = new ProxyHandler(null);
		proxy.isDesktop = true;
		boolean success = false;
		// Java 9+: use desktop methods with reflection so can compile/run on all JREs
		Desktop desktop = Desktop.getDesktop();
		try {			
			Class<?> openFileHandlerClass = Class.forName("java.awt.desktop.OpenFilesHandler", true, classLoader); //$NON-NLS-1$
			Object proxyHandler = Proxy.newProxyInstance(classLoader, 
					new Class[]{openFileHandlerClass} , proxy);			
			Method m = desktop.getClass().getDeclaredMethod("setOpenFileHandler", openFileHandlerClass); //$NON-NLS-1$
			m.invoke(desktop, new Object[] {proxyHandler});
			
  		Class<?> eventClass = Class.forName("java.awt.desktop.OpenFilesEvent", true, classLoader); //$NON-NLS-1$
  		proxy.getFilesMethod = eventClass.getDeclaredMethod("getFiles", (Class<?>[])null); //$NON-NLS-1$
  		
			success = true;
		} catch (Exception ex) {
			proxy.isDesktop = false;
		}
		
		// Java 8-: use com.apple.eawtApplication methods with reflection so can compile/run on all JREs
		if (!success) try {			
			Class<?> appClass = Class.forName("com.apple.eawt.Application", true, classLoader); //$NON-NLS-1$
			Method m = appClass.getDeclaredMethod("getApplication", (Class<?>[])null); //$NON-NLS-1$
			Object application = m.invoke(null, (Object[])null);

			Class<?> openFileHandlerClass = Class.forName("com.apple.eawt.OpenFilesHandler", true, classLoader); //$NON-NLS-1$
			Object proxyHandler = Proxy.newProxyInstance(classLoader, 
					new Class[]{openFileHandlerClass} , proxy);			
			m = application.getClass().getDeclaredMethod("setOpenFileHandler", openFileHandlerClass); //$NON-NLS-1$
			m.invoke(application, new Object[] {proxyHandler});	
			
  		Class<?> eventClass =  Class.forName("com.apple.eawt.AppEvent$OpenFilesEvent", true, classLoader); //$NON-NLS-1$
  		proxy.getFilesMethod = eventClass.getMethod("getFiles", (Class<?>[])null); //$NON-NLS-1$

			success = true;
		} catch (Exception ex) {
		}
		status = success? "TrackerStarter OSXServices running "+(proxy.isDesktop? "java desktop": "apple"): //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										"TrackerStarter OSXServices failed both java desktop and apple"; //$NON-NLS-1$
	}
	
	/**
	 * Constructor used by Tracker, which uses the DockIcon, AboutHandler, QuitHandler
	 * and PreferencesHandler.
	 * 
	 * @param app the Tracker application
	 */
	public OSXServices(Tracker app) {
		tracker = app;
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		proxy = new ProxyHandler(tracker);
		proxy.isDesktop = true;
		boolean success = false;
		
		// Java 9+: use desktop methods with reflection so can compile/run on all JREs
		Desktop desktop = Desktop.getDesktop();
		try {			
			Class<?> aboutHandlerClass = Class.forName("java.awt.desktop.AboutHandler", true, classLoader); //$NON-NLS-1$
			Class<?> quitHandlerClass = Class.forName("java.awt.desktop.QuitHandler", true, classLoader); //$NON-NLS-1$
			Class<?> prefsHandlerClass = Class.forName("java.awt.desktop.PreferencesHandler", true, classLoader); //$NON-NLS-1$

			Object proxyHandler = Proxy.newProxyInstance(classLoader, 
					new Class[]{aboutHandlerClass, quitHandlerClass, prefsHandlerClass} , proxy);			
			Method m = desktop.getClass().getDeclaredMethod("setAboutHandler", aboutHandlerClass); //$NON-NLS-1$
			m.invoke(desktop, new Object[] {proxyHandler});
			m = desktop.getClass().getDeclaredMethod("setQuitHandler", quitHandlerClass); //$NON-NLS-1$
			m.invoke(desktop, new Object[] {proxyHandler});
			m = desktop.getClass().getDeclaredMethod("setPreferencesHandler", prefsHandlerClass); //$NON-NLS-1$
			m.invoke(desktop, new Object[] {proxyHandler});
			
			// set dock icon image
			Class<?> taskClass = Class.forName("java.awt.Taskbar", true, classLoader); //$NON-NLS-1$
			m = taskClass.getDeclaredMethod("getTaskbar", (Class<?>[])null); //$NON-NLS-1$
			Object taskbar = m.invoke(null, (Object[])null);
			m = taskClass.getDeclaredMethod("setIconImage", Image.class); //$NON-NLS-1$
			m.invoke(taskbar, new Object[] {Tracker.TRACKER_ICON_256.getImage()});
			
  		Class<?> quitResponseClass = Class.forName("java.awt.desktop.QuitResponse", true, classLoader); //$NON-NLS-1$
  		proxy.cancelQuitMethod = quitResponseClass.getDeclaredMethod("cancelQuit", (Class<?>[])null); //$NON-NLS-1$
  		proxy.performQuitMethod = quitResponseClass.getDeclaredMethod("performQuit", (Class<?>[])null); //$NON-NLS-1$
  		success = true;

		} catch (Exception ex) {
			proxy.isDesktop = false;
		}
		
		// Java 8-: use com.apple.eawtApplication methods with reflection so can compile/run on all JREs
		if (!proxy.isDesktop) try {			
			Class<?> appClass = Class.forName("com.apple.eawt.Application", true, classLoader); //$NON-NLS-1$
			Method m = appClass.getDeclaredMethod("getApplication", (Class<?>[])null); //$NON-NLS-1$
			Object application = m.invoke(null, (Object[])null);

			Class<?> aboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler", true, classLoader); //$NON-NLS-1$
			Class<?> quitHandlerClass = Class.forName("com.apple.eawt.QuitHandler", true, classLoader); //$NON-NLS-1$
			Class<?> prefsHandlerClass = Class.forName("com.apple.eawt.PreferencesHandler", true, classLoader); //$NON-NLS-1$

			Object proxyHandler = Proxy.newProxyInstance(classLoader, 
					new Class[]{aboutHandlerClass, quitHandlerClass, prefsHandlerClass} , proxy);			
			m = appClass.getDeclaredMethod("setAboutHandler", aboutHandlerClass); //$NON-NLS-1$
			m.invoke(application, new Object[] {proxyHandler});
			m = appClass.getDeclaredMethod("setQuitHandler", quitHandlerClass); //$NON-NLS-1$
			m.invoke(application, new Object[] {proxyHandler});
			m = appClass.getDeclaredMethod("setPreferencesHandler", prefsHandlerClass); //$NON-NLS-1$
			m.invoke(application, new Object[] {proxyHandler});
			
			// set dock icon
			m = appClass.getDeclaredMethod("setDockIconImage", Image.class); //$NON-NLS-1$
			m.invoke(application, new Object[] {Tracker.TRACKER_ICON_256.getImage()});
			
  		Class<?> quitResponseClass = Class.forName("com.apple.eawt.QuitResponse", true, classLoader); //$NON-NLS-1$
  		proxy.cancelQuitMethod = quitResponseClass.getDeclaredMethod("cancelQuit", (Class<?>[])null); //$NON-NLS-1$
  		proxy.performQuitMethod = quitResponseClass.getDeclaredMethod("performQuit", (Class<?>[])null); //$NON-NLS-1$

  		success = true;

		} catch (Exception ex) {
		}

		status = success? "\nTracker OSXServices running "+(proxy.isDesktop? "java desktop": "apple"): //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"\nTracker OSXServices failed both java desktop and apple"; //$NON-NLS-1$
	}
	
	/**
	 * The InvocationHandler class used as proxy to handle apple or desktop events.
	 */
	public static class ProxyHandler implements java.lang.reflect.InvocationHandler {
    
		Tracker tracker;
		boolean isDesktop;
		Method getFilesMethod, cancelQuitMethod, performQuitMethod;
		
		/**
		 * Constructor.
		 * 
		 * @param app the Tracker application; may be null
		 */
		ProxyHandler(Tracker app) {
			tracker = app;
		}
		
    @Override
		public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
      try {
        if (m.getName().equals("handleAbout")) { //$NON-NLS-1$
			    Tracker.showAboutTracker();
        }
        else if (m.getName().equals("handlePreferences")) { //$NON-NLS-1$
	    		if (tracker!=null) {
		  			TFrame frame = tracker.getFrame();
		  			frame.showPrefsDialog();
		  		}
        }
        else if (m.getName().equals("openFiles")) { //$NON-NLS-1$
        	// args[0] is OpenFilesEvent e with list of files to open
      		List<File> files = (List<File>)getFilesMethod.invoke(args[0], (Object[])null);
	    		String[] launchArgs = new String[files.size()];
	    		for (int i=0; i<launchArgs.length; i++) {
	    			launchArgs[i] = files.get(i).getAbsolutePath();
	    		}
	    		TrackerStarter.launchTracker(launchArgs);					 							
        }
        else if (m.getName().equals("handleQuitRequestWith")) { //$NON-NLS-1$
	    		if (tracker!=null) {
		  		  TFrame frame = tracker.getFrame();
		  		  if (frame != null) {
		  		    for (int i = 0; i < frame.getTabCount(); i++) {
		  		    	// save tabs in try/catch block so always closes
		  		      try {
		  						if (!frame.getTrackerPanel(i).save()) {
					        	// args[1] is QuitResponse
	  		        		cancelQuitMethod.invoke(args[1], (Object[])null);
		  							return null;
		  						}
		  					} catch (Exception ex) {
		  					}
		  		    }
		  		  }
		  		}
        	// args[1] is QuitResponse
	    		performQuitMethod.invoke(args[1], (Object[])null);
        }
      } catch (Exception e) {
        throw new RuntimeException("invocation exception: " + e.getMessage()); //$NON-NLS-1$
      }
	    return null;
	  }
	}
}
