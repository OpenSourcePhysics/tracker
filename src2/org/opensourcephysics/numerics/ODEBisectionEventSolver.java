/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;
import java.util.Enumeration;
import java.util.Vector;

/**
 * ODEBisectionEventSolver is an ODEEventSolver that uses
 * the bisection method for root finding.
 *
 * @author       Francisco Esquembre (March 2004)
 */
public class ODEBisectionEventSolver implements ODEEventSolver, ODEAdaptiveSolver {
  /**
   * Maximum number of bisections allowed
   */
  static final public int MAX = 50;
  /* Implementation variables */
  protected int size;
  protected double[] statea;
  protected ODESolver solver;
  protected TriggerODE triggerOde;
  protected Vector<StateEvent> eventList = new Vector<StateEvent>();
  protected Vector<StateEvent> happened = new Vector<StateEvent>();
  protected int errorCode = ODEAdaptiveSolver.NO_ERROR;
  protected boolean eventHappened = false; // added by W. Christian

  /**
   * Creates a new solver that uses the bisection method for finding the events.
   * Example of use:
   *   solver = new BisectionEventSolver (anOde,org.opensourcephysics.numerics.RK4.class);
   *   solver.addEvent(aStateEvent);
   *   // for the rest it works as any other ODESolver.
   * Tested status:
   *   Tested with fixed step solvers.
   *   Tested with adaptive algorithms.
   *   Tested with interpolation algorithms. (See the checks for ODEInterpolationSolver!)
   *     Thought it could probably be improved a bit by changing initialize() to
   *     a (non-yet-existing) synchronize()...
   *     Performance is quite good in examples, though.
   *
   *   Fails with Zeno-type problems (as most others :-)
   *
   * @param ode The ode to solve
   * @param solverClass The ODESolver class to use.
   */
  public ODEBisectionEventSolver(ODE ode, Class<?> solverClass) {
    triggerOde = new TriggerODE(ode);
    try {                                                                                  // Create the solver by reflection
      Class<?>[] c = {ODE.class};
      Object[] o = {triggerOde};
      java.lang.reflect.Constructor<?> constructor = solverClass.getDeclaredConstructor(c);
      solver = (ODESolver) constructor.newInstance(o);
    } catch(Exception _exc) {                                                              // Use RK4 as default solver
      _exc.printStackTrace();
      System.err.println("BisectionEventSolver: Solver class "+solverClass+" not found!"); //$NON-NLS-1$ //$NON-NLS-2$
      System.err.println("  I will use RK4 as default solver.");                           //$NON-NLS-1$
      solver = new RK4(triggerOde);
    }
  }

  /**
   *  Adds a StateEvent to the list of events
   * @param event The event to be added
   */
  public void addEvent(StateEvent event) {
    eventList.add(event);
  }

  /**
   *  Removes a StateEvent from the list of events
   * @param event The event to be removed
   */
  public void removeEvent(StateEvent event) {
    eventList.remove(event);
  }

  // --- Implementation of ODESolver
  public void initialize(double stepSize) {
    // This is for solvers that copy the state, such as ODEInterpolationSolvers
    triggerOde.readRealState();
    // Reserve my own space
    size = triggerOde.getState().length;
    statea = new double[size];
    solver.initialize(stepSize); // Defer to the real solver
  }

  public void setStepSize(double stepSize) {
    solver.setStepSize(stepSize); // Defer to the real solver
  }

  public double getStepSize() {
    return solver.getStepSize(); // Defer to the real solver
  }

  public void setTolerance(double tol) {
    if(solver instanceof ODEAdaptiveSolver) {
      ((ODEAdaptiveSolver) solver).setTolerance(tol);
    }
  }

  public double getTolerance() {
    if(solver instanceof ODEAdaptiveSolver) {
      return((ODEAdaptiveSolver) solver).getTolerance();
    }
    return 0.0;
  }

  /**
   * Gets the eventHappend flag.  The falg is true if an event occured during the last step.
   * @return boolean
   */
  public boolean getEventHappened() {
    return eventHappened;
  }

  /**
   * Advances the ODE as usual, except if an event takes place.
   * Then it finds the event point and applies the actions
   * @return The actual step taken
   */
  public double step() { // Step from t=a to t=b(=a+dt)
    errorCode = ODEAdaptiveSolver.NO_ERROR;
    eventHappened = false;
    double t = 0, origDt = solver.getStepSize();
    do {
      triggerOde.readRealState();                                                                           // Prepare the faked ODE
      System.arraycopy(triggerOde.getState(), 0, statea, 0, size);                                          // Set statea
      // values at b
      double dt = solver.step();
      double[] state = triggerOde.getState();
      // Find which events have happened
      happened.clear();
      for(Enumeration<StateEvent> e = eventList.elements(); e.hasMoreElements(); ) {
        StateEvent evt = e.nextElement();
        if(evt.evaluate(state)<=-evt.getTolerance()) {
          happened.add(evt);                                                                                // This event actually happened!
        }
      }
      // Check for no event
      if(happened.size()==0) {
        triggerOde.updateRealState();
        solver.setStepSize(origDt);
        return dt;
      }
      eventHappened = true;
      //      System.out.println ("An event!");
      // else System.out.println ("N of events = "+happened.size()+" First is = "+happened.elementAt(0));
      /*
         This is the moment of truth!
         We need to find the precise instant of time for the first event
       */
      // Go back to statea
      triggerOde.setState(statea);
      // Check first for a itself
      // This is to make sure that when two events happen at the same
      // time they will be found at the exact same instant.
      // This is important for accuracy of results and better performance.
      StateEvent eventFound = null;
      for(Enumeration<StateEvent> e = happened.elements(); e.hasMoreElements(); ) {
        StateEvent evt = e.nextElement();
        if(Math.abs(evt.evaluate(statea))<evt.getTolerance()) {                                             // Found at a itself
          eventFound = evt;
          // System.out.println("Found at a = " + state[state.length - 1]);
          break;                                                                                            // No need to continue
        }
      }
      if(eventFound==null) {                                                                                // Now find by subdivision
        // This synchronizes our triggerOde state with the state of the ODEInterpolatorSolver
        if(solver instanceof ODEInterpolationSolver) {
          solver.initialize(solver.getStepSize());
        }
        for(int i = 0; i<MAX; i++) {                                                                        // Start the subdivision
          // System.out.println ("Subdividing i = "+i+ "  t = "+state[state.length-1]);
          solver.setStepSize(dt *= 0.5);                                                                    // Take half the step
          double c = solver.step();
          state = triggerOde.getState();
          StateEvent previousFound = null;
          for(Enumeration<StateEvent> e = happened.elements(); e.hasMoreElements(); ) {
            StateEvent evt = e.nextElement();
            double f_i = evt.evaluate(state);
            if(f_i<=-evt.getTolerance()) {
              previousFound = evt;
              break;
            }
            if(f_i<evt.getTolerance()) {
              eventFound = evt;                                                                             // Do not break in case there is a previous one
            }
          }
          if(previousFound!=null) {
            /* Eliminate events that may come later (This is not so necessary) */
            for(Enumeration<StateEvent> e = happened.elements(); e.hasMoreElements(); ) {
              StateEvent evt = e.nextElement();
              if((evt!=previousFound)&&(evt.evaluate(state)>-evt.getTolerance())) {
                happened.remove(evt);
              }
            }
            triggerOde.setState(statea);                                                                    // go back to a
            // This synchronizes our triggerOde state with the state of the ODEInterpolatorSolver
            if(solver instanceof ODEInterpolationSolver) {
              solver.initialize(solver.getStepSize());
            }
          } else {                                                                                          // Advance to new position
            t = t+c;
            System.arraycopy(state, 0, statea, 0, size);
            if(eventFound!=null) {                                                                          // We found it!
              // System.out.println ("Found at "+state[state.length-1]);
              break;
            }
          }
        }                                                                                                   // End of the subdivision scheme
        // The event is any of those which remain in the list of happened
        if(eventFound==null) {                                                                              // If this happens, the event is most likely poorly designed!
          eventFound = happened.elementAt(0);
          System.err.println("BisectionEventSolver Warning : Event not found after "+MAX+" subdivisions."); //$NON-NLS-1$ //$NON-NLS-2$
          System.err.println("  Event = "+eventFound);                                                 //$NON-NLS-1$
          System.err.println("  Please check your event algorithm or decrease the initial stepTime."); //$NON-NLS-1$
          errorCode = ODEAdaptiveSolver.BISECTION_EVENT_NOT_FOUND;
        }
      }
      // System.out.println ("We are at time = "+state[state.length-1]);
      // Update real ODE
      triggerOde.updateRealState();
      if(eventFound.action()) {
        if(solver instanceof ODEInterpolationSolver) {
          triggerOde.readRealState();
          solver.initialize(origDt);
        } else {
          solver.setStepSize(origDt);
        }
        return t;
      }
      // System.out.println("t = " + t);
      if(solver instanceof ODEInterpolationSolver) {
        triggerOde.readRealState();
        solver.initialize(origDt-t);
      } else {
        solver.setStepSize(origDt-t);
      }
    } while(t<origDt);
    solver.setStepSize(origDt);
    return t;
  }

  /**
   * Gets the error code.
   * Error codes:
   *   ODEAdaptiveSolver.NO_ERROR
   *   ODEAdaptiveSolver.DID_NOT_CONVERGE
   *   ODEAdaptiveSolver.BISECTION_EVENT_NOT_FOUND=2;
   * @return int
   */
  public int getErrorCode() {
    return errorCode;
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
