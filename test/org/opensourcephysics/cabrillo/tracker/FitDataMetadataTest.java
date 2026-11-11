package org.opensourcephysics.cabrillo.tracker;
import javax.swing.SwingUtilities;
import org.opensourcephysics.display.*;
import org.opensourcephysics.tools.*;

/** Real Tracker calibration -> OSP metadata, with no inferred derived-variable scale. */
public class FitDataMetadataTest {
 static int passed;
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);passed++;}
 static void near(double a,double b,String name){check(Math.abs(a-b)<1e-10,name+": "+a);}
 public static void main(String[] args)throws Exception{
  try { SwingUtilities.invokeAndWait(()->{
   TFrame window=new TFrame();TrackerPanel panel=new TrackerPanel(window);
   PointMass track=new PointMass();panel.addTrack(track);
   panel.setLengthUnit("m",false);panel.getCoords().setScaleXY(0,100,100);
   DatasetManager source=new DatasetManager();source.setName("position");source.setXPointsLinked(true);
   for(int i=0;i<3;i++){
    source.setXYColumnNames(i,"t",new String[]{"x","y","v_{x}"}[i]);
    source.append(i,new double[]{0,1,2,3},new double[]{1.1,2.9,4.9,7.1});
   }
   DataTool tool=new DataTool(source);DataToolTab tab=tool.getTab(0);tab.checkGUI();
   FitDataMetadata.attach(track,tab,source);tab.setWorkingColumns("t","x");
   FitMetadataProvider metadata;
   try {
    java.lang.reflect.Method method=DataToolTab.class.getDeclaredMethod("getFitMetadataProvider");
    method.setAccessible(true);metadata=(FitMetadataProvider)method.invoke(tab);
   } catch(Exception ex){throw new RuntimeException(ex);}
   check("x".equals(metadata.getPositionComponent("t","x")),"x versus time identified");
   check("y".equals(metadata.getPositionComponent("t","y")),"y versus time identified");
   check(metadata.getPositionComponent("x","y")==null,"position versus position not velocity");
   check(metadata.getPositionComponent("t","v_{x}")==null,"derived velocity not labeled position");
   DatasetCurveFitter fitter=tab.getCurveFitter();
   near(fitter.getYUnitsPerPixel(),.01,"x scale");
   for(double p:new double[]{.5,1,1.5,2,2.5,3,.73}){
    fitter.setUncertaintyModel(FitUncertainty.PIXELS,p);
    near(fitter.getUncertaintyModel().sigma(Double.NaN,fitter.getYUnitsPerPixel()),p/100,"fractional pixels");
   }
   tab.setWorkingColumns("t","y");near(fitter.getYUnitsPerPixel(),.01,"y scale");
   panel.getCoords().setScaleXY(0,200,100);panel.getCoords().setAngle(0,Math.PI/4);
   near(fitter.getYUnitsPerPixel(),Math.sqrt(.5/40000+.5/10000),"anisotropic rotated calibration");
   panel.getCoords().setFixedAngle(false);check(Double.isNaN(fitter.getYUnitsPerPixel()),"varying anisotropic rotation unavailable");
   panel.getCoords().setFixedAngle(true);panel.getCoords().setFixedScale(false);
   check(Double.isNaN(fitter.getYUnitsPerPixel()),"varying scale unavailable");
   panel.getCoords().setFixedScale(true);
   tab.setWorkingColumns("t","v_{x}");check(Double.isNaN(fitter.getYUnitsPerPixel()),"velocity unavailable");
   tool.removeTab(0,false);tool.dispose();window.dispose();
  }); } catch(Throwable t){t.printStackTrace();System.exit(1);}
  System.out.println("Passed: "+passed+" Tracker calibration checks");System.exit(0);
 }
}
