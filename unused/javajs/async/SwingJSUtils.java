package javajs.async;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 * A set of generally useful SwingJS-related methods. Includes:
 * 
 * alternatives to using getCodeBase() for loading resources, due to issues in
 * Eclipse setting that incorrectly (but no problem in JavaScript)
 * 
 * 
 * 
 * @author hansonr
 *
 */
public class SwingJSUtils {
	/**
	 * Set the dimension for the applet prior to j2sApplet's call to 
	 * run the applet. Must be used to create a static field:
	 * 
	 * <code>
	 *   private static Dimension dim = 
	 * </code>
	 * 
	 * 
	 * Then, if it is desired also to have Java also set this, add
	 * 
	 *  if (dim != null) setSize(dim);  
	 *  
	 *  to the applet's init() method.
	 * 
	 * @param w
	 * @param h
	 * @return the Dimension
	 * 
	 * @author hansonr
	 */
	public static Dimension setDim(int w, int h) {
		String baseURI = (/** @j2sNative document.body.baseURI || */
		null);
		boolean isTest = (baseURI == null || baseURI.indexOf("_applet.html") >= 0);
		if (!isTest)
			return null;
		/**
		 * @j2sNative
		 * 
		 * 			J2S.thisApplet.__Info.width = w; J2S.thisApplet.__Info.height = h;
		 */
		return new Dimension(w, h);
	}

	/**
	 * Reliably load a resource of a specific type from the code directory
	 * 
	 * adaptable - here we are returning an image or a string
	 * 
	 * @param cl       the classname of the object to return (Image.class,
	 *                 String.class) null for InputStream
	 * @param filename
	 * @return
	 * 
	 * @author hansonr
	 */
	public static Object getResource(Class<?> baseClass, String filename, Class<?> cl) {
		System.out.println("mpUtils.SwingJSUtils.getResource " + baseClass.getCanonicalName() + " " + filename);
		InputStream is = baseClass.getResourceAsStream(filename);
		if (cl == Image.class) {
			try {
				return ImageIO.read(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (cl == String.class) {
			return new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
		}
		return is;
	}

	/**
	 * Pre-fetch images during the static entry of the class. This should provide
	 * plenty of clock ticks, since the file transfer is synchronous, and all we are
	 * waiting for is the DOM image object to initialize.
	 * 
	 * @param cl
	 * @param images
	 * @param root
	 * @param nImages
	 * @param ext
	 */
	public static void loadImagesStatic(Class<?> cl, Image[] images, String root, String ext, int nImages) {
		for (int i = nImages; --i >= 0;) {

			// Bild laden und beim MediaTracker registrieren
			// MediaTracker ladekontrolle = new MediaTracker(this);

			// BH SwingJS -- adding generally useful method for loading data
			// avoiding the use of getCodeBase(), which for some reason does not work in
			// Eclipse.

			images[i] = (Image) getResource(cl, root + i + "." + ext, Image.class);
//			/**
//			 * @j2sNative
//			 * $("body").append(images[i]._imgNode);
//			 * 
//			 */
//		          ladekontrolle.addImage(scharf[i],i);
			// Warten , bis Bild ganz geladen ist

//		          try {ladekontrolle.waitForID(i);}
//		          catch (InterruptedException e)
//		             {}
		}
	}

	/**
	 * Fill an array with images based on a String[] listing
	 * @param cl reference class
	 * @param root  optional root path, ending in "/"
	 * @param names source file names
	 * @param images  array to fill
	 */
	public static void loadImagesStatic(Class<?> cl, String root, String[] names, Image[] images) {
		for (int i = names.length; --i >= 0;) {
			images[i] = (Image) getResource(cl, root + names[i], Image.class);
		}
	}

	/**
	 * Eclipse-friendly image getting
	 * 
	 * @param c
	 * @param fileName
	 * @return
	 */
	public static Image getImage(Component c, String fileName) {
		return getImage(c.getClass(), fileName);
	}

	/**
	 * Eclipse-friendly image getting
	 * 
	 * @param c
	 * @param fileName
	 * @return
	 */
	public static Image getImage(Class<?> c, String fileName) {
		return (Image) getResource(c, fileName, Image.class);
	}

	/**
	 * Clear the component graphic. BH added this for JavaScript because changing
	 * the browser zoom can change the size of the canvas for unknown reasons.
	 * 
	 * @param c
	 */
	public static void clearComponent(Component c) {
		Graphics gc = c.getGraphics();
		gc.clearRect(0, 0, c.getWidth(), c.getHeight());
		gc.dispose();
	}

	
	/**
	 * A simple interface to the machine loop, generally of the form 
	 * <code>
	 *   public boolean stateLoop() {
	 *   while (stateHepler.isAlive()) {
	 *     switch (stateHelper.getState()) {
	 *     case STATE_XXX:
	 *        ...
	 *        return stateHelper.delayState(100,STATE_YYY);
	 *     case STATE_YYY:
	 *        ...
	 *        stateHelper.setState(STATE_ZZZ);
	 *        continue;
	 *     case STATE_ZZZ:
	 *        ...
	 *        return stateHelper.delayAction(100, MY_ID, "myCommand", myListener, STATE_XXX); 	 *   
	 *     case STATE_DONE:
	 *        ...
	 *        stateHelper.interrupt();
	 *        return false;
	 *     }
	 *     return true;
	 *   }
	 *   return false;
	 *   }
	 * </code>
	 * @author hansonr
	 *
	 */
	public interface StateMachine {

		public boolean stateLoop();

	}
	/**
	 * StateHelper is a class that facilitates moving from an asychronous multithreaded model to a state-oriented model of programming
	 * for SwingJS animations and other asynchronous business.
	 *  
	 * @author hansonr
	 *
	 */
	public static class StateHelper {
		
		public static final int UNCHANGED = Integer.MIN_VALUE;

		private StateMachine machine;
		private int state;
		private int level;
		
		private boolean interrupted;
		

		public StateHelper(StateMachine machine) {
			this.machine = machine;
		}

		public void interrupt() {
			interrupted = true;
		}
		
		public boolean isInterrupted() {
			return interrupted;
		}
		
		public boolean isAlive() {
			return !interrupted;
		}
		
		public void restart() {
			interrupted = false;
		}
		
		public void setState(int state) {
			this.state = this.stateNext = state;
		}

		public int getState() {
			return state;
		}

		public void setLevel(int level) {
			this.level = this.levelNext = level;
		}

		public int getLevel() {
			return level;
		}
		
		public void setNextState(int next) {
			stateNext = next; 
		}
		
		public int getNextState() {
			return stateNext;
		}

		public int getNextLevel() {
			return levelNext;
		}

		public void setNextLevel(int next) {
			levelNext = next; 
		}

		/** 
		 * 
		 * NOTE: this method must remain private; it is accessed via p$1
		 * 
		 * @return
		 */
		private boolean nextState() {
			return next(stateNext, levelNext);
		}
		/**
		 * Set the state and run machine.stateLoop().
		 * 
		 * @param state something meaningful to the machine
		 * 
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		public boolean next(int state) {
			return next(state, 0);
		}
		
		/**
		 * Set the state and level, and then run machine.stateLoop(). Driven directly or via delayedState or delayedAction
		 * 
		 * @param state something meaningful to the machine
		 * @param level something meaningful to the machine
		 * 
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		public boolean next(int state, int level) {
			return nextStatePriv((Object) this, state, level);
		}

		private static boolean nextStatePriv(Object oThis, int state, int level) {
			StateHelper me = (StateHelper) oThis;
			if (me.interrupted)
				return false;
			if (level != UNCHANGED)
				me.level = level;
			if (state != UNCHANGED)
				me.state = state;
			return me.machine.stateLoop();
		}

		/**
		 * After the given number of milliseseconds, set the new state and run the machines stateLoop with unchanged level
		 * 
		 * @param ms the number of milliseconds to delay; 0 to execute synchronously		 * 
		 * @param stateNext  the next state to run
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		public boolean delayedState(int ms, int stateNext) {
			return delayedState(ms, stateNext, level);
		}

		private Timer stateTimer;

		private int stateNext;
		private int levelNext;
		
		/**
		 * After the given number of milliseseconds, set the new state and level, and
		 * run the machines stateLoop
		 * 
		 * @param ms        the number of milliseconds to delay; 0 to execute
		 *                  synchronously *
		 * @param stateNext the next state
		 * @param levelNext the next level
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		
		public boolean delayedState(int ms, int stateNext, int levelNext) {
			if (interrupted)
				return false;
			if (ms == 0)
				return next(stateNext, levelNext);
			if (stateNext != UNCHANGED)
				this.stateNext = stateNext;
			if (levelNext != UNCHANGED)
				this.levelNext = levelNext;
			
			/**
			 * @j2sNative
			 * var me = this;
			 * setTimeout(function(){
			 *  p$1.nextState.apply(me, []);
			 * },ms);
			 */
			{
				// Java only

				if (stateTimer == null) {
					stateTimer = new Timer(ms, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							nextState();
						}

					});
					stateTimer.setRepeats(false);
					stateTimer.start();
				} else {
					stateTimer.restart();
				}
			}
			return true;
		}

		/**
		 * Fire an actionPerformed event after a given number of milliseconds
		 * 
		 * @param ms       delay milliseconds. if 0, then this action will be called
		 *                 synchronously
		 * @param id       id for this event, possibly ACTION_PERFORMED (1001), but not
		 *                 necessarily
		 * @param command  key for ActionEvent.getCommand()
		 * @param listener ActionListener to be called.
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		public boolean delayedAction(int ms, int id, String command, ActionListener listener) {
			return delayedAction(ms, id, command, listener, UNCHANGED, UNCHANGED);
		}

		/**
		 * Fire an actionPerformed event after a given number of milliseconds
		 * 
		 * @param ms       delay milliseconds. if 0, then this action will be called
		 *                 synchronously
		 * @param id       id for this event, possibly ACTION_PERFORMED (1001), but not
		 *                 necessarily
		 * @param command  key for ActionEvent.getCommand()
		 * @param listener ActionListener to be called.
		 * 
		 * @param state    the next state to go to after this listener is called; UNCHANGED to let the listener take care of this.
		 * 
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		public boolean delayedAction(int ms, int id, String command, ActionListener listener, int state) {
			return delayedAction(ms, id, command, listener, state, UNCHANGED);
		}		
		
		/**
		 * Fire an actionPerformed event after a given number of milliseconds. Setting BOTH stateNext and levelNext to UNCHANGED (Integer.MIN_VALUE)
		 * allows the listener to handle continuing the loop.
		 * 
		 * @param ms       delay milliseconds. if 0, then this action will be called
		 *                 synchronously
		 * @param id       id for this event, possibly ACTION_PERFORMED (1001), but not
		 *                 necessarily
		 * @param command  key for ActionEvent.getCommand()
		 * @param listener ActionListener to be called.
		 * @param stateNext  state to run after the event is processed by the listener, or UNCHANGED (Integer.MIN_VALUE) to allow listener to handle this.
		 * @param levelNext  level to run after the event is processed by the listener, or UNCHANGED (Integer.MIN_VALUE) to allow listener to handle this.
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		public boolean delayedAction(int ms, int id, String command, ActionListener listener, int stateNext, int levelNext) {
			if (interrupted)
				return false; 
			ActionEvent event = new ActionEvent(this, id, command);
			if (ms == 0) {
				listener.actionPerformed(event);
				return (stateNext == UNCHANGED && levelNext == UNCHANGED || nextStatePriv(this, stateNext == UNCHANGED ? state : stateNext, levelNext == UNCHANGED ? level : levelNext));
			}
			
			StateHelper me = this;
			
			Timer timer = new Timer(ms, id == ActionEvent.ACTION_PERFORMED ? listener : new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!interrupted)
						listener.actionPerformed(event);
					if (!interrupted && (stateNext != UNCHANGED || levelNext != UNCHANGED))
						nextStatePriv(me, stateNext == UNCHANGED ? state : stateNext, levelNext == UNCHANGED ? level : levelNext);
				}
				
			});
			timer.setRepeats(false);
			timer.start();
			return true;
		}

		public static void delayedRun(int ms, Runnable runnable) {
			new StateHelper(null).delayedRun(ms, runnable, UNCHANGED, UNCHANGED);
		}

		
		/**
		 * Fire an actionPerformed event after a given number of milliseconds. Setting
		 * BOTH stateNext and levelNext to UNCHANGED (Integer.MIN_VALUE) allows the
		 * listener to handle continuing the loop.
		 * 
		 * @param ms        delay milliseconds. if 0, then this action will be called
		 *                  synchronously
		 * @param id        id for this event, possibly ACTION_PERFORMED (1001), but not
		 *                  necessarily
		 * @param command   key for ActionEvent.getCommand()
		 * @param listener  ActionListener to be called.
		 * @param stateNext state to run after the event is processed by the listener,
		 *                  or UNCHANGED (Integer.MIN_VALUE) to allow listener to handle
		 *                  this.
		 * @param levelNext level to run after the event is processed by the listener,
		 *                  or UNCHANGED (Integer.MIN_VALUE) to allow listener to handle
		 *                  this.
		 * @return not interrupted
		 * 
		 * @author Bob Hanson hansonr@stolaf.edu
		 */
		public boolean delayedRun(int ms, Runnable runnable, int stateNext, int levelNext) {
			if (interrupted)
				return false;
			if (ms == 0) {
				return (stateNext == UNCHANGED && levelNext == UNCHANGED || nextStateIfUnchanged(this, runnable,
						stateNext == UNCHANGED ? state : stateNext, levelNext == UNCHANGED ? level : levelNext));
			}
			StateHelper me = this;
			/**
			 * @j2sNative
			 * 
			 * setTimeout(function() {
			 * 
			 *    me.nextStateIfUnchanged$O$O$I$I.apply(me, [me, runnable, stateNext, levelNext]);
			 * 
			 * },ms);
			 */
			{
				Timer timer = new Timer(ms, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						nextStateIfUnchanged(me, runnable, stateNext, levelNext);
					}

				});
				timer.setRepeats(false);
				timer.start();
			}
			return true;
		}

		protected boolean nextStateIfUnchanged(Object oThis, Object runnable, int stateNext, int levelNext) {
			StateHelper me = (StateHelper)(oThis);
			if (!me.interrupted)
				((Runnable) runnable).run();
			if (!me.interrupted && (stateNext != UNCHANGED || levelNext != UNCHANGED))
				nextStatePriv(oThis, stateNext == UNCHANGED ? me.state : stateNext,
						levelNext == UNCHANGED ? me.level : levelNext);
			return true;
		}

		/**
		 * sleep and then execute the next state
		 * @param ms
		 */
		public void sleep(int ms) {
			int next = stateNext;
			delayedState(ms, next);
		}
	}
	
	/**
	 * open a "url-like" input stream
	 * @param base
	 * @param fileName
	 * @return
	 */
	public static BufferedInputStream openStream(Class<?> base, String fileName) {
		String s = (String) getResource(base, fileName, String.class);
        return new BufferedInputStream(new ByteArrayInputStream(s.getBytes()));
	}

}