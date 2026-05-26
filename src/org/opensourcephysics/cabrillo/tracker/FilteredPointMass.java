package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FontSizer;

public class FilteredPointMass extends PointMass implements PropertyChangeListener {
	
	int sourceID;
	MotionFilter filter;
	JMenuItem motionFilterItem, closeItem;
	String sourceName = "";
	boolean isOpen = false;
	double rmsDevX, rmsDevY;
	private final static String[] panelEventsParticleModel = new String[] { 
			VideoClip.PROPERTY_VIDEOCLIP_STARTFRAME,  // ParticleModel
			VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT, // ParticleModel
	};
	
	
	public FilteredPointMass(PointMass pointMass, MotionFilter motionFilter) {
		super(pointMass.getMass());
		sourceID = pointMass.getID();
		filter = motionFilter;
		pointMass.addPropertyChangeListenerSafely(this);
		setColor(pointMass.getColor().darker());
	}
	
	@Override
	protected void createGUI() {
		super.createGUI();
		motionFilterItem = new JMenuItem();
		motionFilterItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TTrack source = TTrack.getTrack(sourceID);
				if (source == null)
					return;
				refreshPositions(false);
				MotionFilterDialog dialog = source.tp.getFilterDialog();
				dialog.setTargetMass(FilteredPointMass.this);
				FontSizer.setFonts(dialog, FontSizer.getLevel());				
				dialog.pack();
				dialog.setVisible(true);
			}
		});
		closeItem = new JMenuItem();
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PointMass source = (PointMass)TTrack.getTrack(sourceID);
				if (source == null || source.tp == null)
					return;
				MotionFilterDialog dialog = source.tp.getFilterDialog();
				dialog.setVisible(false);
				source.tp.removeTrack(FilteredPointMass.this);
			}
		});
		
	}
	
	public void setMotionFilter(MotionFilter motionFilter) {
		filter = motionFilter;
		PointMass source = (PointMass)TTrack.getTrack(sourceID);
		if (source != null) {
			source.filter = filter;
			if (source.tp != null) {
				source.tp.changed = true;
			}
		}
		refreshPositions(true);
		repaint();
		refreshDataLater = false;
		updateDerivatives();
		fireStepsChanged();
	}
	
	public MotionFilter getMotionFilter() {
		return filter;
	}
	
	@Override
	public boolean isDependent() {
		return true;
	}
	
	@Override
	public void setName(String name) {
		// ignored
	}

	@Override
	public void setTrackerPanel(TrackerPanel panel) {
		if (tp != null) {
			removePanelEvents(panelEventsParticleModel);
		}
		super.setTrackerPanel(panel);
		if (tp != null) {
			addPanelEvents(panelEventsParticleModel);
		}
	}
	@Override
	public void delete() {
		isOpen = isOpen();
		TTrack source = TTrack.getTrack(sourceID);
		if (source == null)
			return;
		source.removePropertyChangeListener(this);
		removePanelEvents(panelEventsParticleModel);
		delete(false);
	}
	
	@Override
	public String getName() {
		TTrack source = TTrack.getTrack(sourceID);
		if (source != null)
			sourceName = source.getName();
		name = TrackerRes.getString("FilteredPointMass.Name.Prefix") + " " + sourceName;
		return name;
	}
	
	@Override
	public ArrayList<Component> getToolbarPointComponents(TrackerPanel panel, TPoint point) {
		ArrayList<Component> list = super.getToolbarPointComponents(panel, point);
		xField.setEnabled(false);
		yField.setEnabled(false);
		magField.setEnabled(false);
		angleField.setEnabled(false);
		return list;
	}
	
	@Override
	public ArrayList<Component> getToolbarTrackComponents(TrackerPanel panel) {
		ArrayList<Component> list = super.getToolbarTrackComponents(panel);
		massField.setEnabled(false);
		return list;
	}
	
	@Override
	public JMenu getMenu(TrackerPanel panel, JMenu menu0) {
		JMenu menu = super.getMenu(panel, menu0);

		menu.remove(dataBuilderItem);
		menu.remove(nameItem);
		menu.remove(descriptionItem);
		menu.remove(autoAdvanceItem);
		menu.remove(markByDefaultItem);
		menu.remove(lockedItem);
		menu.remove(deleteStepItem);
		menu.remove(clearStepsItem);
		menu.remove(showFilteredItem);
		menu.remove(deleteTrackItem);
		
		// add setMotionFilter item at top
		motionFilterItem.setText(TrackerRes.getString("FilteredPointMass.MenuItem.SetFilter.Text")); //$NON-NLS-1$
		menu.insert(motionFilterItem, 0);
		menu.insertSeparator(1);
		// add close item at bottom
		closeItem.setText(TrackerRes.getString("FilteredPointMass.MenuItem.Close.Text")); //$NON-NLS-1$
		menu.addSeparator();
		menu.add(closeItem);

		// wherever two separators are together, remove one
		boolean doAgain = true;
		while(doAgain) {
			doAgain = false;
			boolean isSeparator = false;
			int i = menu.getItemCount() - 1;
			for (; i >= 0; i--) {
				Component next = menu.getMenuComponent(i);
				if (next instanceof JMenuItem) {
					isSeparator = false;
					continue;
				}
				if (isSeparator) {
					// found second in a row
					doAgain = true;
					menu.remove(menu.getMenuComponent(i));
					break;
				}
				// found first
				isSeparator = true;
			}
		}
		
		return menu;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (!isOpen())
			return;
		String prop = e.getPropertyName();
		if (prop.equals("step") || prop.equals("steps")) {
			// source has changed
			refreshPositions(true);
			repaint();
			refreshDataLater = false;
			updateDerivatives();
			fireStepsChanged();
		}
		super.propertyChange(e);
		
		boolean refresh = true;
		switch (prop) {
		case ImageCoordSystem.PROPERTY_COORDS_TRANSFORM:
			refresh = e.getNewValue() != null;
		case VideoClip.PROPERTY_VIDEOCLIP_STARTFRAME:
		case VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT:
		case VideoClip.PROPERTY_VIDEOCLIP_STEPSIZE:
			if (refresh) {
				refreshPositions(true);
				refreshDataLater = false;
				updateDerivatives();
				fireStepsChanged();	
			}
		}
	}
	
	protected boolean isOpen() {
		PointMass source = (PointMass)TTrack.getTrack(sourceID);
		if (source == null || source.tp == null)
			return false;
		// true if trackerPanel contains this track
		return isOpen || source.tp.getTrack(getName()) != null;		
	}
	
	@Override
	public Step deleteStep(int n) {
		return null;
	}
	
	protected void refreshPositions(boolean full) {
		if (!isOpen())
			return;

		PointMass source = (PointMass)TTrack.getTrack(sourceID);
		if (source == null || source.tp == null || steps == null)
			return;
		
		VideoClip clip = source.tp.getPlayer().getVideoClip();
		Step[] stepArray = source.getSteps();
		int len = stepArray.length;
		int stepCount = clip.getStepCount();
		double[] dataX = new double[stepCount];
		double[] dataY = new double[stepCount];
		boolean[] valid = new boolean[stepCount];
		// first assemble world positions of source
		
		for (int i = 0; i < len; i++) {
			if (!clip.includesFrame(i))
				continue;
			if (i >= stepArray.length)
				break;
			int n = clip.frameToStep(i);
			PositionStep curStep = (PositionStep)stepArray[i];
			valid[n] = curStep!=null? true: false;
			if (curStep != null) {
				TPoint p = curStep.getPosition();
				Point2D wp = p.getWorldPosition(source.tp);
				dataX[n] = wp.getX();
				dataY[n] = wp.getY();
				if (Double.isNaN(dataX[n]) || Double.isNaN(dataY[n])) {
					valid[n] = false;
				}
			}
		}
		
		// then obtain the filtered positions and determine rmsDev
		double[] filteredX = filter == null? dataX: filter.apply(dataX, valid);
		double[] filteredY = filter == null? dataY: filter.apply(dataY, valid);
		
		double sumOfSquaresX = 0, sumOfSquaresY = 0, dev = 0;
		int count = 0;
		for (int n = 0; n < filteredX.length; n++) {
			if (!valid[n])
				continue;
			count++;
			dev = filteredX[n] - dataX[n];
			sumOfSquaresX += dev * dev;
			dev = filteredY[n] - dataY[n];
			sumOfSquaresY += dev * dev;
		}
		rmsDevX = count==0? 0: Math.sqrt(sumOfSquaresX / count);
		rmsDevY = count==0? 0: Math.sqrt(sumOfSquaresY / count);
		
		if (full) {		
			// then set positions of this filteredPointMass
			loading = true; // suppresses firing step property changes
			steps.setLength(len);
			Step[] mySteps = getSteps();
			TPoint p = new TPoint();
			for (int i = 0; i < stepCount; i++) {
				int frame = clip.stepToFrame(i);
				if (frame >= mySteps.length)
					break;
				PositionStep myStep = (PositionStep)mySteps[frame];
				if (valid[i]) {
					// determine image position
					p.setWorldPosition(filteredX[i], filteredY[i], source.tp, frame);
					if (myStep == null) {
						myStep = (PositionStep) createStep(frame, p.x, p.y);
					}
					else {
						myStep.getPosition().setPosition(p);
					}
				}
				else { // invalid--Step should be null
					mySteps[frame] = null;
				}
			}
		}
		loading = false;
		refreshDataLater = false;
		dataValid = false;
		getData(tp); // refreshes datasets
	}
	
	public double getRMSDev() {
		return Math.sqrt(rmsDevX*rmsDevX + rmsDevY*rmsDevY);
	}
	
	/**
	 * Returns an ObjectLoader to save and load data for this class.
	 *
	 * @return the object loader
	 */
	public static XML.ObjectLoader getLoader() {
		return new Loader();
	}

	/**
	 * A class to save and load data for this class. This does nothing
	 * since filter, color and footprint are saved by source PointMass
	 */
	static class Loader implements XML.ObjectLoader {

		@Override
		public void saveObject(XMLControl control, Object obj) {
		}

		@Override
		public Object createObject(XMLControl control) {
			return null;
		}

		@Override
		public Object loadObject(XMLControl control, Object obj) {
			return obj;
		}
	}

}
