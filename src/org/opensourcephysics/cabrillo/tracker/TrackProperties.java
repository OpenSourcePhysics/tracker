package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.util.ArrayList;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

/**
 * A class used for name, footprint and color edits.
 */
public class TrackProperties {
	String name;
	String[] footprints;
	Color[] colors;
	
	TrackProperties(TTrack track) {
		name = track.getName();
		if (track instanceof ParticleDataTrack) {
			ParticleDataTrack dt = (ParticleDataTrack) track;
			ArrayList<ParticleDataTrack> mpoints = dt.morePoints;
			int np = mpoints.size();
			colors = new Color[np + 2];
			colors[0] = dt.getColor();
			colors[colors.length - 1] = dt.getModelFootprint().getColor();
			for (int i = 0; i < np; i++) {
				colors[i + 1] = mpoints.get(i).getColor();
			}
			footprints = new String[np + 2];
			footprints[0] = dt.getFootprintName();
			footprints[footprints.length - 1] = dt.getModelFootprintName();
			for (int i = 0; i < np; i++) {
				footprints[i + 1] = mpoints.get(i).getFootprintName();
			}
		} else {
			footprints = new String[] { track.getFootprintName() };
			colors = new Color[] { track.getColor() };
		}
	}
	
	TrackProperties(String name, String[] footprints, Color[] colors) {
		this.name = name;
		this.footprints = footprints;
		this.colors = colors;
	}
	
  public static XML.ObjectLoader getLoader() {
    return new Loader();
  }

  /**
   * A class to save and load data for this class.
   */
  static class Loader implements XML.ObjectLoader {

		@Override
		public void saveObject(XMLControl control, Object obj) {
			TrackProperties props = (TrackProperties)obj;
			control.setValue("name", props.name); //$NON-NLS-1$
			control.setValue("footprints", props.footprints); //$NON-NLS-1$
			control.setValue("colors", props.colors); //$NON-NLS-1$
		}

		@Override
		public Object createObject(XMLControl control) {
			String name = control.getString("name"); //$NON-NLS-1$
			String[] footprints = (String[])control.getObject("footprints"); //$NON-NLS-1$
			Color[] colors = (Color[])control.getObject("colors"); //$NON-NLS-1$
			return new TrackProperties(name, footprints, colors);
		}

		@Override
		public Object loadObject(XMLControl control, Object obj) {
			return obj;
		}
  
  }
}

