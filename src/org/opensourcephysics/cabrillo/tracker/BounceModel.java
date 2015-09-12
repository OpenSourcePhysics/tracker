/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */

package org.opensourcephysics.cabrillo.tracker;



/**
 * A LinearModelWithStep maps a parameter vector to a
 * series of data points at equally spaced time intervals.
 *
 * For handling 2D data, the parameters are a num_params x 2 Matrix.
 *
 * This class requires the JAMA matrix package from 
 * http://math.nist.gov/javanumerics/jama
 *
 * Mon Oct 31 14:02:07 PDT 2011 Kevin Karplus
 *
 * @author Kevin Karplus
 */
public class BounceModel 
{
  
  private final BounceMatrix model;		/* model*params should match data in least-squares sense */
  private final BounceMatrix inverse_model;  /* inverse_model*data sets params */
  
  private final double step_at;		/* where is a step modeled */
  private final boolean use_step;       /* one more parameter than polynomial, to fit step at step_at */
  private final boolean use_unknown_step; /* two more paramters than polynomial,
  					to try to fit step somewhere  near middle of window
					*/
  
  private final int degree; 		// degree of polynomial
  private final int num_params; 	// degree+1 + (use_unknown_step? 2: (use_step? 1: 0));
  
  /** constructor  for n data points using polynomial of degree d
   *   plus a Dirac delta in the acceleration (step in velocity) at time s
   *
   * That is, data[t] will be fitted to 
   *		sum_i<=d param[i]*t**i for t<s
   *		sum_i<=d param[i]*t**i + param[d+1]*(t-s) for s<=t
   *
   *  If the time of the step is <=0 or >=num_data-1, then it is not
   *	  possible to fit an extra parameter, so it is omitted from the model,
   *      and a pure polynomial is used
   *		sum_i<=d param[i]*t**i 
   *  
   * If the time of the step is Double.NaN, then the step time is
   *  unknown and two extra parameters are needed to estimate s, 
   *	which is assumed to be in range (0.5*num_data-1, 0.5*num_data).
   *  
   *
   *  Other than 0 to turn it off, the step should not be located at an integer.
   *
   * @param	num_data
   * @param deg 
   * @param when_step when step in velocity happens
   */
  public BounceModel(int num_data, int deg, double when_step)
  {   
	degree= deg;
	
	// There must be at least one data point on each side of a step
	use_unknown_step = Double.isNaN(when_step);
	if (use_unknown_step)
	{   use_step=false;
	    step_at= (num_data+1)/2;	// looking for step in (step_at-1,step_at)
	}
	else
	{   use_step = when_step>0 && when_step<num_data-1;	
	    step_at = use_step? when_step: 0;
	}
	num_params = degree+1 + (use_unknown_step? 2: (use_step? 1: 0));
	
	double [][] mapping1D = new double[num_data][num_params];
	for (int t=0; t<num_data; t++)
	{   int power = 1;
	    for (int d=0; d<=degree; d++)
	    {   mapping1D[t][d] = power;
		power *= t;
	    }
	    if (this.usesStep())
	    {    mapping1D[t][degree+1] =  t>=step_at? (t-step_at): 0;
	    }
	    if (use_unknown_step)
	    {    mapping1D[t][degree+2] =  t>=step_at? 1: 0;
	    }
	}
	
	model = new BounceMatrix(mapping1D);
//     	model.print(8,2);		// debugging output
	inverse_model = model.inverse();
  }
  
  
  /** where is there a step?
   * 
   * @return time of step in velocity
   */
  public double getStepAt()
  {   if (use_unknown_step) return Double.NaN;  
      return step_at;
  }  
  
    /** where is there a step?
     * If there is a step in a model which estimates time as  well as size of step,
     * we need the model parameters to say where the step was fitted.
     * 
     * @param  model_param parameters from fit
     * @return time of step in velocity
     */
    public double getStepAt(BounceMatrix model_param)
    {   
	if (!use_unknown_step) return step_at;
	
	double[][] param_array = model_param.getArray();
	int dimension = model_param.getColumnDimension();
	    
	double guess_step = 0;	// where do fit_parameters estimate the step
	// get weighted average of extra time past step_at
	double weight=0;
	for (int dim=0; dim<dimension; dim++)
	{   double dv=param_array[degree+1][dim];	// estimate of the velocity step size
	    double x=param_array[degree+2][dim];	// estimate of how much more displacement there is than if the step were at step_at
	    if (dv==0) continue;
//	    double extra_t = x/dv;
	    guess_step += dv*x;	// add extra_t * dv^2
	    weight += dv*dv;	// dv^2
	}
	if (weight>0) guess_step /= weight;
	return step_at - guess_step;
    }
  
  
  /** fit parameters for model to a window of (x,y) points
   * 
   * Returned result:
   *	fitted paramters
   *	returns null if fitting is not possible,
   *	which can happen if 
   *		start, start+index_step, ...,  start+(num_data-1)*index_step 
   *			goes out of range for either data array
   *	    or	xdata or ydata is Double.NaN for one of the specified points
   *
   *  Time t=0 corresponds to subscript "start" 
   *  Time t=1 to start+time_step 
   *
   * @param xData	array of x values
   * @param yData	array of y values
   * @param start	subscript of first (x,y) pair for window
   * @param index_step	increment between subscripts for subsequent data points
   * @return 		LinearModelParams containing parameters and residual square error
   */
  public BounceParameters fit_xy(double []xData, double[] yData, int start, int index_step)
  {
	int num_data = model.getRowDimension();
	int last_index = start+(num_data-1)*index_step;
	if (start<0 || last_index>=xData.length || last_index>=yData.length)
	{   return null;
	}
	
	// copy data points into a matrix
	BounceMatrix data_matrix = new BounceMatrix(num_data,2);
	double [][] data=data_matrix.getArray();
	for (int t=0; t<num_data; t++)
	{   data[t][0] = xData[start+index_step*t];
	    data[t][1] = yData[start+index_step*t];
	    if (Double.isNaN(data[t][0]) || Double.isNaN(data[t][1]))
	    {   return null;
	    }
	}
	
	BounceMatrix params = inverse_model.times(data_matrix);
	
	double[][] error_array =model.times(params).minus(data_matrix).getArray();
	double square_error=0;
	for (int t=0; t<num_data; t++)
	{   square_error += error_array[t][0]*error_array[t][0];
	    square_error += error_array[t][1]*error_array[t][1];
	}

	if (!use_unknown_step)
	{	return new BounceParameters(this, params, square_error);
	}
	
	// try various reasonable guesses for step time and refit with 
	// fixed step.

	BounceParameters best_fit=null;
        double[][] params_array= params.getArray();	
	double combined_step = 0;	// where do fit_parameters estimate the step
					// get weighted average of separate x and y estimates
	double weight=0;
	for (int dim=0; dim<2; dim++)
	{   double dv=params_array[degree+1][dim];	// estimate of the velocity step size
	    double extra=params_array[degree+2][dim];	// estimate of how much more displacement there is than if the step were at step_at
	    if (dv==0) continue;

	    double try_step= step_at -extra/dv ;
	    if (try_step<0) {try_step=0.001;}
	    else if (try_step>=num_data-1) {try_step=num_data-1.001;}

	    BounceModel step_model= new BounceModel(num_data, degree, try_step);
	    BounceParameters fit_step = step_model.fit_xy(xData, yData, start, index_step);
	    if (null==best_fit || best_fit.getError()>fit_step.getError())
	    {    best_fit = fit_step;
	    }

	    combined_step += dv*dv*try_step;	
	    weight += dv*dv;	// dv^2
	}
	if (weight>0) combined_step /= weight;
	
	BounceModel step_model= new BounceModel(num_data, degree, combined_step);
	BounceParameters fit_step = step_model.fit_xy(xData, yData, start, index_step);
	if (null==best_fit || best_fit.getError()>fit_step.getError())
	{    best_fit = fit_step;
	}
	
	return best_fit;
  }

  /** fit parameters for model to a window of (x,y) points, with a specified step
   * 		to be removed before fitting
   * Returned result:
   *	fitted parameters, with record of extra step
   *
   *	returns null if fitting is not possible,
   *	which can happen if 
   *		start, start+index_step, ...,  start+(num_data-1)*index_step 
   *			goes out of range for either data array
   *	    or	xdata or ydata is Double.NaN for one of the specified points
   *
   *  Time t=0 corresponds to subscript "start" 
   *  Time t=1 to start+time_step 
   *
   * @param xData	array of x values
   * @param yData	array of y values
   * @param start	subscript of first (x,y) pair for window
   * @param index_step	increment between subscripts for subsequent data points
   * @param initial_step_at	what time the predefined step happens 0<=t<num_data
   * @param initial_step_size
   * @return 		LinearModelParams containing parameters and residual square error
   */
  public BounceParameters fit_xy(double []xData, double[] yData, int start, int index_step,
  	double initial_step_at, double[] initial_step_size
	)
  {
	if (use_unknown_step || (use_step && step_at != initial_step_at))
	{    throw new RuntimeException("Can't fit with an initial step if the model already tries to fit a step"); //$NON-NLS-1$
	}
	
	int num_data = model.getRowDimension();
	int last_index = start+(num_data-1)*index_step;
	if (start<0 || last_index>=xData.length || last_index>=yData.length)
	{   return null;
	}
	
	// copy data points into a matrix
	BounceMatrix data_matrix = new BounceMatrix(num_data,2);
	double [][] data=data_matrix.getArray();
	for (int t=0; t<num_data; t++)
	{   data[t][0] = xData[start+index_step*t] - (t>initial_step_at? initial_step_size[0]*(t-initial_step_at): 0);
	    data[t][1] = yData[start+index_step*t] - (t>initial_step_at? initial_step_size[1]*(t-initial_step_at): 0);
	    if (Double.isNaN(data[t][0]) || Double.isNaN(data[t][1]))
	    {   return null;
	    }
	}
	
	BounceMatrix params = inverse_model.times(data_matrix);
	
	double[][] error_array =model.times(params).minus(data_matrix).getArray();
	double square_error=0;
	for (int t=0; t<num_data; t++)
	{   square_error += error_array[t][0]*error_array[t][0];
	    square_error += error_array[t][1]*error_array[t][1];
	}

	return new BounceParameters(this, params, square_error, initial_step_at, initial_step_size);
  }

  /** return first derivatives at time t
   *
   *  The dimensionality of the returned array of doubles is the same as
   *  the number of columns in the model_param Matrix
   *	(that would be 2 for model_param returned by fit_xy()).
   *
   *  Time t=0 corresponds to subscript "start" in the fit_xy call
   *  Time t=1 to start+time_step in the fit_xy call
   *
   * @param model_param	parameter matrix as returned by a fit_xy call
   * @param t		what time
   * @return	first derivates of x and y at time t
   */
   public double[] first_deriv(BounceMatrix model_param, double t)
   {
	int dimension = model_param.getColumnDimension();

	double [] result = new double[dimension];
	double[][] param_array = model_param.getArray();
	
	double power = 1.;		// t**(d-1)
	for (int d=1; d<=degree; d++)
	{  for (int dim=0; dim<dimension; dim++)
	   {   result[dim] += power*d *param_array[d][dim];
	   }
	   power *= t;
	}
	double guess_step = this.getStepAt(model_param);

	if (this.usesStep() && t>=guess_step)
	{  for (int dim=0; dim<dimension; dim++)
	   {   result[dim] += param_array[degree+1][dim];
	   }
	}
	return result;
   }

  /** return second derivatives at time t
   *
   *  The dimensionality of the returned array of doubles is the same as
   *  the number of columns in the model_param Matrix
   *	(that would be 2 for model_param returned by fit_xy()).
   *
   *  The Dirac delta causes some difficulty in expressing the acceleration,
   *  and it is replaced in the acceleration output (but not the
   *  velocity or data fitting) by a pulse in the interval (s-0.5, s+.5]
   *
   * @param model_param	parameter matrix as returned by a fit_xy call
   * @param t		what time step (t=0 corresponds to subscript
   *			  "start" in the fit_xy call
   * @return	second derivates of x and y at time t
   */
   public double[] second_deriv(BounceMatrix model_param, double t)
   {
	int dimension = model_param.getColumnDimension();

	double [] result = new double[dimension];
	double[][] param_array = model_param.getArray();
	
	double power = 1.;		// t**(d-2)
	for (int d=2; d<=degree; d++)
	{  
	   for (int dim=0; dim<dimension; dim++)
	   {   result[dim] += power*d*(d-1) *param_array[d][dim];
	   }
	   power *= t;
	}
	double guess_step = this.getStepAt(model_param);

	if (this.usesStep() && guess_step-0.5<t && t<=guess_step+0.5)
	{  for (int dim=0; dim<dimension; dim++)
	   {   result[dim] += param_array[degree+1][dim];
	   }
	}
	
	return result;
   }

  /** return use_step
   *
   *
   * @return	true if model uses as step, false if pure polynomial model
   */
   public boolean usesStep()
   {    
    	return use_step||use_unknown_step;
   }

  /** return step size
   *
   *  The dimensionality of the returned array of doubles is the same as
   *  the number of columns in the model_param Matrix
   *	(that would be 2 for model_param returned by fit_xy()).
   *
   *
   * @param model_param	parameter matrix as returned by a fit_xy call
   * @return	step size (0 if no step)
   */
   public double[] getStepSize(BounceMatrix model_param)
   {    
	int dimension = model_param.getColumnDimension();
	double [] result = new double[dimension];
	
	if (! this.usesStep()) { return result; }
	
	double[][] param_array = model_param.getArray();
	for (int dim=0; dim<dimension; dim++)
	{   result[dim] = param_array[degree+1][dim];
	}
	return result;
   }



}
