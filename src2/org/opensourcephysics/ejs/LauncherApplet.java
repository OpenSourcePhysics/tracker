/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.ejs;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Constructor;
import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;
import org.opensourcephysics.display.DrawingFrame;

/**
 * A utility class to launch simulations
 */
public class LauncherApplet extends JApplet {
  private JFrame _parentFrame = null;
  public Model _model = null;
  public Simulation _simulation = null;
  public View _view = null;

  // --------------- Application part ----------
  /*
  static public void main(String[] args) {
    Model model = null;
    String simClass = null, window = null;
    // for (int i=0; i<args.length; i++) System.out.println ("Arg["+i+"] = <"+args[i]+">");
    if(args.length>0) {
      simClass = args[0];
    }
    if(args.length>1) {
      window = args[1];
    }
    LauncherApplet la = new LauncherApplet();
    if(window!=null) {
      la._parentFrame = new JFrame();
      la._parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      la._parentFrame.setTitle("Captured");
      la._parentFrame.setLocation(400, 0);
      la._parentFrame.setVisible(true);
      la._model = createModel(simClass, window, la._parentFrame, null);
      la._simulation = la._model.getSimulation();
      la._view = la._model.getView();
      la.captureWindow(la._model.getView(), window);
    } else {
      la._model = createModel(simClass, null, null, null);
      la._simulation = la._model.getSimulation();
      la._view = la._model.getView();
    }
  } */
  // ----------------- Applet part -----------------
  public String getParameter(String key, String def) {
    return((getParameter(key)!=null) ? getParameter(key) : def);
  }

  public void _play() {
    _simulation.play();
  }

  public void _pause() {
    _simulation.pause();
  }

  public void _step() {
    _simulation.step();
  }

  public void _setFPS(int _fps) {
    _simulation.setFPS(_fps);
  }

  public void _setDelay(int _delay) {
    _simulation.setDelay(_delay);
  }

  public void _reset() {
    _simulation.reset();
  }

  public void _initialize() {
    _simulation.initialize();
  }

  public boolean _saveState(String _filename) {
    return _simulation.saveState(_filename);
  }

  public boolean _readState(String _filename) {
    return _simulation.readState(_filename, getCodeBase());
  }

  public boolean _setVariables(String _command, String _delim, String _arrayDelim) {
    return _simulation.setVariables(_command, _delim, _arrayDelim);
  }

  public boolean _setVariables(String _command) {
    return _simulation.setVariables(_command);
  }

  public String _getVariable(String _varName) {
    return _simulation.getVariable(_varName);
  }

  public void _resetView() {
    _view.reset();
  }

  /** Initialize the applet */
  public void init() {
    String simClass = null;
    String windowToCapture = null;
    try {
      simClass = this.getParameter("simulation", null); //$NON-NLS-1$
    } catch(Exception e) {
      e.printStackTrace();
    }
    try {
      windowToCapture = this.getParameter("capture", null); //$NON-NLS-1$
    } catch(Exception e) {
      e.printStackTrace();
    }
    if((windowToCapture!=null)&&(getParentFrame()!=null)&&(getParentFrame()!=null)) {
      // System.out.println ("Parent Frame is "+getParentFrame().getName());
      _model = createModel(simClass, windowToCapture, getParentFrame(), getCodeBase());
      _simulation = _model.getSimulation();
      _view = _model.getView();
      captureWindow(_model.getView(), windowToCapture);
    } else {
      _model = createModel(simClass, null, null, getCodeBase());
      _simulation = _model.getSimulation();
      _view = _model.getView();
    }
  }

  // public void destroy(){
  // System.out.println("LauncherApplet destroy Method.");
  // super.destroy();
  // }
  //
  // public void stop(){
  // System.out.println("LauncherApplet stop Method.");
  // super.stop();
  // }
  //
  // public void start(){
  // System.out.println("LauncherApplet start Method.");
  // super.start();
  // }
  //
  // public String getAppletInfo () {
  // return "An applet to launch a Simulation.";
  // }
  public String[][] getParameterInfo() {
    String[][] pinfo = {
      {"simulation", "String", "The simulation"},                        //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      {"capture", "String", "The name of the component to be captured"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    };
    return pinfo;
  }

  private java.awt.Frame getParentFrame() {
    java.awt.Container parent = getParent();
    while(parent!=null) {
      if(parent instanceof java.awt.Frame) {
        return(java.awt.Frame) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  // ------ Common stuff
  static Model createModel(String simClass, String _ownerName, java.awt.Frame _ownerFrame, java.net.URL _codebase) {
    Model aModel = null;
    if((_ownerName!=null)||(_codebase!=null)) {
      try { // Instantiate a model with the given name and three parameters
        Class<?> c = Class.forName(simClass);
        Constructor<?>[] constructors = c.getConstructors();
        for(int i = 0; i<constructors.length; i++) {
          Class<?>[] parameters = constructors[i].getParameterTypes();
          if((parameters.length==3)&&parameters[0].isAssignableFrom(_ownerName.getClass())&&parameters[1].isAssignableFrom(_ownerFrame.getClass())&&parameters[2].isAssignableFrom(_codebase.getClass())) {
            aModel = (Model) constructors[i].newInstance(new Object[] {_ownerName, _ownerFrame, _codebase});
            break;
          }
        }
      } catch(Exception exc) {
        exc.printStackTrace();
        aModel = null;
      }
    }
    if(aModel==null) {
      try { // Now try a simple constructor
        Class<?> aClass = Class.forName(simClass);
        aModel = (Model) aClass.newInstance();
      } catch(Exception exc) {
        exc.printStackTrace();
        return null;
      }
    }
    return aModel;
  }

  private void captureWindow(View _aView, String _aWindow) {
    if(_aWindow==null) {
      return;
    }
    RootPaneContainer root;
    if(_parentFrame!=null) {
      root = _parentFrame;
    } else {
      root = this;
    }
    Component comp = _aView.getComponent(_aWindow);
    if(comp==null) {
      return;
    }
    //Dimension size = comp.getSize();
    if(comp instanceof DrawingFrame) {
      comp.setVisible(true);
      Container contentPane = ((RootPaneContainer) comp).getContentPane();
      contentPane.setVisible(true);
      root.setContentPane(contentPane);
      Component glassPane = ((RootPaneContainer) comp).getGlassPane();
      root.setGlassPane(glassPane);
      glassPane.setVisible(true);
      ((DrawingFrame) comp).setKeepHidden(true);
      ((DrawingFrame) comp).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    } else if(comp instanceof JDialog) {
      comp.setVisible(true);
      Container contentPane = ((RootPaneContainer) comp).getContentPane();
      contentPane.setVisible(true);
      root.setContentPane(contentPane);
      Component glassPane = ((RootPaneContainer) comp).getGlassPane();
      root.setGlassPane(glassPane);
      glassPane.setVisible(true);
      ((JDialog) comp).dispose();
    } else {
      root.getContentPane().setLayout(new java.awt.BorderLayout());
      root.getContentPane().add(comp, java.awt.BorderLayout.CENTER);
      root.getContentPane().validate();
      Container oldParent = comp.getParent();
      if(oldParent!=null) {
        oldParent.validate();
      }
    }
    if(_parentFrame!=null) {
      _parentFrame.pack();
    }
  }

} // End of class

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
