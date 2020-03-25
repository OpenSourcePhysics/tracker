/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.util.Collection;
import java.util.Iterator;
import org.opensourcephysics.display.GUIUtils;

/**
 *  OSPApplication defines a model and a control.
 *
 * @author Douglas Brown
 * @author Wolfgang Christian
 * @version 1.1
 */
public class OSPApplication {
  Control control;
  Object model;
  Class<?> loadedControlClass, loadedModelClass;
  boolean compatibleModel = false;

  /**
   * Constructs an OSPApplication.
   *
   * @param  control
   * @param  model
   */
  public OSPApplication(Control control, Object model) {
    this.control = control;
    this.model = model;
  }

  /**
   * Set the loader to import all xml data for a compatible model.
   * @param b
   */
  public void setCompatibleModel(boolean b) {
    compatibleModel = b;
  }

  /**
   * Gets the model that was used to load this class.
   * @return
   */
  public Class<?> getLoadedModelClass() {
    return loadedModelClass;
  }

  /**
   * Gets the control that was used to load this class.
   * @return
   */
  public Class<?> getLoadedControlClass() {
    return loadedControlClass;
  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new OSPAppLoader();
  }

  /**
   * A class to save and load data for OSPControls.
   */
  static class OSPAppLoader implements XML.ObjectLoader {
    /**
     * Saves object data to an XMLControl.
     *
     * @param xmlControl the xml control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl xmlControl, Object obj) {
      OSPApplication app = (OSPApplication) obj;
      xmlControl.setValue("control", app.control); //$NON-NLS-1$
      xmlControl.setValue("model", app.model);     //$NON-NLS-1$
    }

    /**
     * Creates an object using data from an XMLControl.
     *
     * @param xmlControl the xml control
     * @return the newly created object
     */
    public Object createObject(XMLControl xmlControl) {
      Object model = xmlControl.getObject("model");                //$NON-NLS-1$
      Control control = (Control) xmlControl.getObject("control"); //$NON-NLS-1$
      return new OSPApplication(control, model);
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param xmlControl the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl xmlControl, Object obj) {
      OSPApplication app = (OSPApplication) obj;
      app.loadedControlClass = null;
      app.loadedModelClass = null;
      XMLControlElement cControl = (XMLControlElement) xmlControl.getChildControl("control"); //$NON-NLS-1$
      XMLControlElement mControl = (XMLControlElement) xmlControl.getChildControl("model");   //$NON-NLS-1$
      if((cControl==null)||(mControl==null)) {
        OSPLog.fine("OSP Application not loaded. An OSP application must have a model and a control."); //$NON-NLS-1$
        return app;
      }
      Class<?> modelClass = mControl.getObjectClass();
      Class<?> controlClass = cControl.getObjectClass();
      if((modelClass==null)||(controlClass==null)) {
        if(controlClass==null) {
          OSPLog.fine("Object not loaded. Cannot find class for control."); //$NON-NLS-1$
        }
        if(modelClass==null) {
          OSPLog.fine("Object not loaded. Cannot find class for model.");   //$NON-NLS-1$
        }
        return app;
      }
      boolean compatibleModels = app.compatibleModel;
      if(app.model!=null) {
        boolean loaderMatch = XML.getLoader(modelClass).getClass()==XML.getLoader(app.model.getClass()).getClass();
        compatibleModels = compatibleModels||(modelClass==app.model.getClass())|| // identical classes are always compatible
          (modelClass.isAssignableFrom(app.model.getClass())&&loaderMatch); // subclasses with identical loaders are assumed to be compatible
      }
      // load control data for compatible models
      app.loadedControlClass = controlClass;
      if((app.control!=null)&&(controlClass==app.control.getClass())) {
        // matched control class: load normally
        cControl.loadObject(app.control);
      } else {
        // auto-import compatible models
        cControl.loadObject(app.control, true, compatibleModels);
      }
      Collection<String> appNames = app.control.getPropertyNames();
      Iterator<String> it = cControl.getPropertyNames().iterator();
      while(it.hasNext()) {
        String name = it.next();
        if(!appNames.contains(name)) { // remove names that are not currently in the app
          app.control.setValue(name, null);
          //System.out.println("removed: "+name);
        }
      }
      app.loadedModelClass = modelClass;
      if((app.model!=null)&&(modelClass==app.model.getClass())) {
        // matched model class: load normally
        mControl.loadObject(app.model);
      } else {
        // mismatched model class: auto-import with chooser
        mControl.loadObject(app.model, true, false);
      }
      GUIUtils.repaintOSPFrames(); // make sure frames are up to date
      return app;
    }

  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
