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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
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

import java.util.Arrays;
import java.util.Comparator;

/**
 * This implements an algorithm for estimating both the first and second 
 * derivatives (in 2 dimensions) from a sequence of uniformly spaced samples.
 *
 * This assumes there may be spikes in the second derivative ("bounces"),
 * causing steps in the first derivative.
 *
 * The returned result of evaluate is
 *    result[0] = xDeriv1 (double[])
 *    result[1] = yDeriv1 (double[])
 *    result[2] = xDeriv2 (double[])
 *    result[3] = yDeriv2 (double[])
 *
 * Mon Oct 31 15:29:48 PDT 2011 Kevin Karplus
 *
 * @author Kevin Karplus
 */
public class BounceDerivatives implements Derivative
{
    // window_size is the number of data points used for fitting a model
    private int window_size;
    
    // degree is the complexity of the polynomial model
    private final int degree=2;		// constant acceleration model
    
    //  a simple polynomial model
    private BounceModel poly_model;
    
    // model that tries to fit both the time and size of the step
    private BounceModel step_model;

    
  /**
   * Evaluates two derivatives
   * at count points (start, start+index_step, ...)
   *	(with t=0 at subscript start and t=1 at subscript start+index_step)
   * The returned arrays are the same length and use the same subscripts as the input arrays.
   *
   * Input data:
   *    data[0] = parameters (int[] {spill, start, stepsize, count})
   *    data[1] = xData (double[])
   *    data[2] = yData (double[])
   *    data[3] = validData (boolean[])
   *    
   * Returned result:
   *		result[0] = xDeriv1 (double[])
   *		result[1] = yDeriv1 (double[])
   *		result[2] = xDeriv2 (double[])
   *		result[3] = yDeriv2 (double[])
   *
   * @param data the input data
   * @return Object array containing the result
   */
  public Object[] evaluate(Object[] data)
  {
    int[] params = (int[])data[0];
    window_size = 1+params[0]*2;
    int start = params[1];
    int index_step = params[2];
    int count = params[3];
    double[] x = (double[])data[1];
    double[] y = (double[])data[2];
    boolean[] validData = (boolean[])data[3];
  	
    int length=x.length;
    assert(x.length==y.length);
    
    double[] xDeriv1, yDeriv1, xDeriv2, yDeriv2;
    
    double[][] result = new double[4][];
    
    result[0] = xDeriv1 =new double[length];
    result[1] = yDeriv1 =new double[length];
    result[2] = xDeriv2 =new double[length];
    result[3] = yDeriv2 =new double[length];

    for (int n = 0; n < length; n++)
    {	// mark all the outputs as invalid
	xDeriv1[n] = yDeriv1[n] = Double.NaN;
	xDeriv2[n] = yDeriv2[n] = Double.NaN;
	
	// make sure that the input data arrays are marked with NaNs
	if (!validData[n])
	{    x[n] = y[n] = Double.NaN;
	}
    }

    if (start>=length)
    {	// this was a dummy call, probably with initial (empty) data arrays
	return result;
    }

    // reset count so indexing does not run over
    count = Math.min(count, (length-start)/index_step);

//    System.out.format("DEBUG: start=%d, index_step=%d, count=%d, length=%d\n", 
//		start, index_step, count, length);
    assert (start>=0);
    assert (start<length);
    assert (index_step>0);

    poly_model = new BounceModel(window_size, degree, 0);    
    BounceParameters[] poly_fit=new BounceParameters[count];
    //	poly_fit[c] are the parameters for polynomial fitted for a window around cth time step

    step_model = new BounceModel(window_size, degree, Double.NaN);
    BounceParameters[] step_fit=new BounceParameters[count];
    //	step_fit[c] are the parameters for model with unspecified step fit 
    //	They are initially created for windows looking for steps around each c value,
    //  but are later sorted into 


    int[]  c_at = new int[count];
    //	c_at[i] is the position of c  in the window for the model fitted around	 cth time step

    // fit models at each step 
    fit_models:
    for(int c=0; c<count; c++)
    {	int i = start+index_step*c;
	
	// We need to find a window (window_size contiguous points around i with valid data)
	// Ideally, we'd like a symmetric window, but if there is missing data,
	//	we'll have to move forward or backward.
	
	c_at[c] = window_size/2;	// where is position c in the window

	int highest_bad_index=-1;
	for (int in_w=window_size-1; in_w>=0; in_w--)
	{   int index=i + index_step*(in_w-c_at[c]);
	    if (index<0 || index>=length || Double.isNaN(x[index]) || Double.isNaN(y[index]))
	    {	highest_bad_index = in_w;
		break;
	    }
	}
//	System.out.format("DEBUG: At %d, highest_bad=%d, that is step %d\n", i, highest_bad_index, i-c_at[c]+highest_bad_index);
	if (highest_bad_index>=0)
	{   // The default window won't work.
	    // We need to try shifting the window.

	    int lowest_bad_index=window_size;
	    for (int in_w=0; in_w<window_size; in_w++)
	    {	int index=i + index_step*(in_w-c_at[c]);
		if (index<0 || index>=length || Double.isNaN(x[index]) || Double.isNaN(y[index]))
		{   lowest_bad_index = in_w;
		    break;
		}
	    }
	    
	    // We either want to move the window up so it starts after highest_bad_index
	    //	(subtracting highest_bad_index+1 from c_at[c])
	    // or move the window down so it ends before lowest_bad_index,
	    //	(subtracting lowest_bad_index-window_size from c_at[c])
	    // whichever is closest
	    
	    int move_up = highest_bad_index+1;
	    int move_down = window_size-lowest_bad_index;
	    c_at[c] -= (move_up<=move_down)? move_up: (0-move_down);
	
	    for (int in_w=window_size-1; in_w>=0; in_w--)
	    {	int index=i + index_step*(in_w-c_at[c]);
		if (index<0 || index>=length || Double.isNaN(x[index]) || Double.isNaN(y[index]))
		{  
//		    System.out.format("DEBUG: At %d, no window\n", i);
		    continue fit_models;	// moved window also failed
		}
	    }

	}
	
//	  System.out.format("DEBUG: At %d, window is %d to %d\n", i, i-c_at[c], i-c_at[c]+window_size-1);

	poly_fit[c] = poly_model.fit_xy(x,y, i-c_at[c]*index_step, index_step);
	step_fit[c] = step_model.fit_xy(x,y, i-c_at[c]*index_step, index_step);
	
//DEBUG	
//	if (step_fit[c] != null)
//	{    System.out.format("DEBUG: frame %d, step at %.3f, saves %.4g = %.2f%%\n",
//		i, i+index_step*(step_fit[c].getStepAt()-c_at[c]), poly_fit[c].getError()-step_fit[c].getError(),
//		100.*(poly_fit[c].getError()-step_fit[c].getError())/ poly_fit[c].getError());
//      }
//END DEBUG
    
    }

//DEBUG	
//    System.out.format("DEBUG: possible step times (skipping 0s):\n");
//    for(int c=0; c<count; c++)
//    {   
//        if (null==step_fit[c]) continue;
//	int i = start+index_step*c;	
//	double possible_step_time = step_fit[c].getStepAt();
//        if (possible_step_time==0.0) continue;
//	System.out.format("%.3f  ", possible_step_time + i-c_at[c]);
//    }
//    System.out.format("\n");
//END DEBUG
    
    // How useful does each step look?
    // step_value[c] is reduction in square error for c within
    //    0.5*(window_size-1) of step  for refit model vs polynomial model
    final double[] step_value = new double[count];
    
    for(int c=0; c<count; c++)
    {   
        if (null==step_fit[c]) continue;
	double possible_step_time = step_fit[c].getStepAt();
        if (possible_step_time==0.0) continue;

	int i = start+index_step*c;	
	double i_step_time = i + index_step*(possible_step_time -c_at[c]);
	
	double[] step_size = step_fit[c].getStepSize();

	for(int i_wind=Math.max(start,     (int)(i_step_time-0.5*(window_size-1)+0.999)); 
		i_wind<=Math.min(length-1, (int)(i_step_time+0.5*(window_size-1)+0.001)); 
		i_wind++)
	{   int c_wind = (i_wind-start)/index_step;
	    if (c_wind >= count) continue;
	    if (! (0<=c_wind && c_wind<count))
	    {    System.out.format("ERROR: c_wind=%d, i_wind=%d, start=%d, i_step_time=%.3f\n", //$NON-NLS-1$
	    		c_wind, i_wind, start, i_step_time);
	    }
	    if (null==poly_fit[c_wind]) continue;
	    double poly_err = poly_fit[c_wind].getError();
//DEBUG
//	    System.out.format("DEBUG: refitting, window starting at %d stepping %d, step_in_wind at %.3f\n",
//			i_wind-c_at[c_wind]*index_step, index_step, 
//			(i_step_time-i_wind)/index_step+c_at[c_wind]);
//END DEBUG
	    BounceParameters refit = 
		poly_model.fit_xy(x,y, i_wind-c_at[c_wind]*index_step, index_step, 
			(i_step_time-i_wind)/index_step+c_at[c_wind], step_size);
	    double step_err = refit.getError();
	    step_value[c] += poly_err - step_err;
	}
	
    }
    
//DEBUG
//    System.out.format("DEBUG: values of steps:\n");
//    for(int c=0; c<count; c++)
//    {   
//        if (null==step_fit[c]) continue;
//	int i = start+index_step*c;	
//	double possible_step_time = step_fit[c].getStepAt();
//        if (possible_step_time==0.0) continue;
//	double[] step_size = step_fit[c].getStepSize();
//	
//	System.out.format("    at %.3f value is %.4g (%.3f%%), step_size = %.4g %.4g\n",
//		    possible_step_time + i-c_at[c], step_value[c],
//		    100.*step_value[c]/poly_fit[c].getError(),
//		    step_size[0], step_size[1]
//		    );
//    }
//END DEBUG


    // Now choose locations for steps so that no two steps are in any window,
    //	picking the biggest step_values.
    
    // get sorted list of locations for possible steps, best first
    Integer[] best_step_locs = new Integer[count];
    for(int c=0; c<count; c++)
    {	 best_step_locs[c]=c;
    }
    Arrays.sort(best_step_locs, 
	new Comparator<Integer>() 
		{   public int compare(final Integer o1, final Integer o2) 
		    {	return Double.compare(step_value[o2],step_value[o1]);
		    }
		});

    
//DEBUG    
//    System.out.format("DEBUG: steps by decreasing value:\n");
//    for(int loc=0; loc<count; loc++)
//    {   int c = best_step_locs[loc];
//        if (null==step_fit[c]) continue;
//	int i = start+index_step*c;	
//	double possible_step_time = step_fit[c].getStepAt();
//        if (possible_step_time==0.0) continue;
//	double[] step_size = step_fit[c].getStepSize();
//	
//	System.out.format("    at %.3f value is %.4g (%.3f%%), step_size = %.4g %.4g\n",
//		    possible_step_time + i-c_at[c], step_value[c],
//		    100.*step_value[c]/poly_fit[c].getError(),
//		    step_size[0], step_size[1]
//		    );
//    }
//END DEBUG
    
    BounceParameters[] use_model= new BounceParameters[count];
    // set use_model[c] to a refit model for a nearby spike,
    // if left null, use poly_model[c]
    
    // particular choice for position c
    for(int k=0; k<count; k++)
    {	int c = best_step_locs[k];
	
	if (use_model[c] != null) continue;	// already have a step for this window
        
	if (null==step_fit[c]) continue;
	
	double poly_error = poly_fit[c].getError();
//	double model_error = step_fit[c].getError();
	final double what_is_large= 0.6 * window_size; 
	if (step_value[c] < what_is_large*poly_error)
	{   // step is not big, get rid of it
	    
//DEBUG	    
//	    int i = start+index_step*c;	
//	    double possible_step_time = step_fit[c].getStepAt();
//	    System.out.format("DEBUG: step at %.3f suppressed, because %.4g not large relative to %.4g (model_error %.4g)\n", 
//			possible_step_time + i-c_at[c], step_value[c],
//			 poly_error,
//			model_error
//			);
//END DEBUG
	    continue;
	}

//DEBUG	    
//	else
//	{   
//	    int i = start+index_step*c;	
//	    double possible_step_time = step_fit[c].getStepAt();
//	    System.out.format("DEBUG: step %.3f kept, because %.4g large relative to %.4g (model_error %.4g)\n", 
//			possible_step_time + i-c_at[c], step_value[c],
//			poly_error, model_error
//			);
//	}
//END DEBUG
		
	double possible_step_time = step_fit[c].getStepAt();
        if (possible_step_time==0.0) continue;
        double c_step_time = c +possible_step_time-c_at[c];
//	double i_step_time = start + index_step*c_step_time;
	
	double[] step_size = step_fit[c].getStepSize();

	int c_below_step = (int) c_step_time;
	
	for(int c_wind=Math.max(0,c_below_step-window_size+2); 
		c_wind<c_below_step+window_size && c_wind<count; 
		c_wind++)
	{   int i_wind = start+index_step*c_wind;
	
	    if (i_wind<start || i_wind>=length) continue;	// out of range
	    if (c_step_time <= c_wind-c_at[c_wind]) 
	    	continue;	// step before window for c_wind
	    if (c_step_time >= c_wind-c_at[c_wind]+window_size-1)
		 continue;	// step after window for c_wind
	    if (use_model[c_wind]!=null) continue; // already have a model here
	    if (null==poly_fit[c_wind]) continue;  // no fit here
//	    double poly_err = poly_fit[c_wind].getError();
//DEBUG
//	    System.out.format("DEBUG: refitting, window starting at %d stepping %d, step_in_wind at %.3f\n",
//			i_wind-c_at[c_wind]*index_step, index_step, 
//			c_step_time-c_wind+c_at[c_wind]);
//END DEBUG
	    BounceParameters refit = 
		poly_model.fit_xy(x,y, i_wind-c_at[c_wind]*index_step, index_step, 
			c_step_time-c_wind+c_at[c_wind], step_size);			
//	    double step_err = refit.getError();
	    use_model[c_wind] = refit;
	}

    }	 
    
    apply_models:
    for(int c=0; c<count; c++)
    {	int i = start+index_step*c;
	
	if (null == poly_fit[c])
	{   // couldn't fit any models here
	    continue apply_models;
	}
	
	BounceParameters use_this_model=use_model[c];	// which model has a chosen step	
	if (null==use_this_model) use_this_model=poly_fit[c];

	double []deriv1 = use_this_model.first_deriv( c_at[c]);
	xDeriv1[i] = deriv1[0];
	yDeriv1[i] = deriv1[1];

	double []deriv2 = use_this_model.second_deriv(c_at[c]);
	xDeriv2[i] = deriv2[0];
	yDeriv2[i] = deriv2[1];
    }
    return result;
  }
}
