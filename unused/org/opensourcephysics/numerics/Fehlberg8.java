/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics;

/**
 * Title:        Fehlberg8
 * Description:  An eigth order Runge-Kutta ODE solver.
 * @author       Wolfgang Christian & F. Esquembre
 * @version 1.0
 */
public class Fehlberg8 extends AbstractODESolver {
  private double[] rate1, rate2, rate3, rate4, rate5, rate6, rate7, rate8, rate9, rate10, rate11, rate12, rate13, estimated_state;

  /**
   * Constructs the RK4 ODESolver for a system of ordinary  differential equations.
   *
   * @param ode the system of differential equations.
   */
  public Fehlberg8(ODE ode) {
    super(ode);
  }

  /**
   * Initializes the ODE solver and allocates the rate and state arrays.
   * The number of differential equations is determined by invoking getState().length on the superclass.
   *
   * @param stepSize
   */
  public void initialize(double stepSize) {
    super.initialize(stepSize);
    rate1 = new double[numEqn];
    rate2 = new double[numEqn];
    rate3 = new double[numEqn];
    rate4 = new double[numEqn];
    rate5 = new double[numEqn];
    rate6 = new double[numEqn];
    rate7 = new double[numEqn];
    rate8 = new double[numEqn];
    rate9 = new double[numEqn];
    rate10 = new double[numEqn];
    rate11 = new double[numEqn];
    rate12 = new double[numEqn];
    rate13 = new double[numEqn];
    estimated_state = new double[numEqn];
  }

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getRate method to compute the rate at various intermediate states.
   *
   * The ODESolver then advances the solution and copies the new state into the
   * ODE's state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step() {
    double state[] = ode.getState();
    if(state==null) {
      return stepSize;
    }
    if(state.length!=numEqn) {
      initialize(stepSize);
    }
    ode.getRate(state, rate1);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*2./27.*rate1[i];
    }
    ode.getRate(estimated_state, rate2);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(rate1[i]+3*rate2[i])/36;
    }
    ode.getRate(estimated_state, rate3);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(rate1[i]+3*rate3[i])/24;
    }
    ode.getRate(estimated_state, rate4);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(5./12.*rate1[i]-25./16.*rate3[i]+25./16.*rate4[i]);
    }
    ode.getRate(estimated_state, rate5);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(rate1[i]+5*rate4[i]+4*rate5[i])/20;
    }
    ode.getRate(estimated_state, rate6);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(-25*rate1[i]+125*rate4[i]-260*rate5[i]+250*rate6[i])/108;
    }
    ode.getRate(estimated_state, rate7);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(31./300.*rate1[i]+61./225.*rate5[i]-2./9.*rate6[i]+13./900.*rate7[i]);
    }
    ode.getRate(estimated_state, rate8);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(2*rate1[i]-53./6.*rate4[i]+704./45.*rate5[i]-107./9.*rate6[i]+67./90.*rate7[i]+3*rate8[i]);
    }
    ode.getRate(estimated_state, rate9);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(-91./108.*rate1[i]+23./108.*rate4[i]-976./135.*rate5[i]+311./54.*rate6[i]-19./60.*rate7[i]+17./6.*rate8[i]-rate9[i]/12.);
    }
    ode.getRate(estimated_state, rate10);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(2383./4100.*rate1[i]-341./164.*rate4[i]+4496./1025.*rate5[i]-301./82.*rate6[i]+2133./4100.*rate7[i]+45./82.*rate8[i]+45./164.*rate9[i]+18./41.*rate10[i]);
    }
    ode.getRate(estimated_state, rate11);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(3./205.*rate1[i]-6./41.*rate6[i]-3./205.*rate7[i]-3./41.*rate8[i]+3./41.*rate9[i]+6./41.*rate10[i]);
    }
    ode.getRate(estimated_state, rate12);
    for(int i = 0; i<numEqn; i++) {
      estimated_state[i] = state[i]+stepSize*(-1777./4100.*rate1[i]-341./164.*rate4[i]+4496./1025.*rate5[i]-289./82.*rate6[i]+2193./4100.*rate7[i]+51./82.*rate8[i]+33./164.*rate9[i]+12./41.*rate10[i]+rate12[i]);
    }
    ode.getRate(estimated_state, rate13);
    for(int i = 0; i<numEqn; i++) {
      state[i] = state[i]+stepSize*(34./105.*rate6[i]+9./35.*rate7[i]+9./35.*rate8[i]+9./280.*rate9[i]+9./280.*rate10[i]+41./840.*rate12[i]+41./840.*rate13[i]);
    }
    return stepSize;
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
