package org.opensourcephysics.cabrillo.tracker;

import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.DataFunction;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.tools.DataToolTab;
import org.opensourcephysics.tools.FitMetadataProvider;

/** Live, session-only metadata adapter for OSP Data Tool. */
final class FitDataMetadata implements FitMetadataProvider {
    private final TTrack track;
    private final DataToolTab tab;
    private final DatasetManager source;
    private FitDataMetadata(TTrack track,DataToolTab tab,DatasetManager source) {
        this.track=track;this.tab=tab;this.source=source;
    }
    static void attach(TTrack track,DataToolTab tab,DatasetManager source) {
        if(tab!=null)tab.setFitMetadataProvider(new FitDataMetadata(track,tab,source));
    }
    private String variable(String column) {
        for(Dataset dataset:source.getDatasetsRaw()) {
            if(column.equals(tab.getColumnName(dataset.getID(),0)))return dataset.getXColumnName();
            String local=tab.getColumnName(dataset.getID(),1);
            if(column.equals(local) && !(dataset instanceof DataFunction))return dataset.getYColumnName();
        }
        return null;
    }
    @Override public String getUnits(String column) {
        String variable=variable(column);
        return variable==null || track.tp==null?null:track.tp.getDataUnits(track,variable);
    }
    @Override public double getYUnitsPerPixel(String column) {
        String variable=variable(column);
        if(track.tp==null || track.tp.getLengthUnit()==null || track.getClass()!=PointMass.class || !("x".equals(variable)||"y".equals(variable)))return Double.NaN;
        ImageCoordSystem coords=track.tp.getCoords();
        // A common physical uncertainty requires a common conversion. A moving
        // origin is harmless; changing scale or anisotropic rotation is not.
        if(!coords.isFixedScale())return Double.NaN;
        int n=track.tp.getFrameNumber();
        double sx=coords.getScaleX(n),sy=coords.getScaleY(n);
        if(!coords.isFixedAngle() && Math.abs(sx-sy)>1e-12*Math.max(Math.abs(sx),Math.abs(sy)))return Double.NaN;
        double a="x".equals(variable)?coords.imageToWorldXComponent(n,1,0):coords.imageToWorldYComponent(n,1,0);
        double b="x".equals(variable)?coords.imageToWorldXComponent(n,0,1):coords.imageToWorldYComponent(n,0,1);
        double scale=Math.hypot(a,b);
        return Double.isNaN(scale)||Double.isInfinite(scale)||scale<=0?Double.NaN:scale;
    }
}
