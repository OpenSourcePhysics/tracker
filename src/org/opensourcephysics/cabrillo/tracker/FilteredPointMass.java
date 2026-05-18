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
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.tools.FontSizer;

public class FilteredPointMass extends PointMass implements PropertyChangeListener {
	
	int sourceID;
	MotionFilter filter;
	JMenuItem motionFilterItem, closeItem;
	String sourceName = "";
	boolean isDeleted;
	double rmsDevX, rmsDevY;
	
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
	public void delete() {
		TTrack source = TTrack.getTrack(sourceID);
		if (source == null)
			return;
		source.removePropertyChangeListener(this);
		delete(false);
		isDeleted = true;
	}
	
	@Override
	public String getName() {
		TTrack source = TTrack.getTrack(sourceID);
		if (source != null)
			sourceName = source.getName();
		name = sourceName + " " + TrackerRes.getString("FilteredPointMass.Name.Suffix");
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
		if (prop.equals(ImageCoordSystem.PROPERTY_COORDS_TRANSFORM)) {
			refreshPositions(false);
			refreshDataLater = false;
			updateDerivatives();
			fireStepsChanged();
		}
	}
	
	@Override
	public boolean isVisible() {
		PointMass source = (PointMass)TTrack.getTrack(sourceID);
		if (source == null || source.tp == null)
			return false;
		boolean vis = super.isVisible();
		// must be visible and included in trackerpanel tracks
		return vis && source.tp.getTrack(getName()) != null;
	}
	
	@Override
	public Step deleteStep(int n) {
		return null;
	}
	
	protected void refreshPositions(boolean full) {
		PointMass source = (PointMass)TTrack.getTrack(sourceID);
		if (source == null || source.tp == null || steps == null)
			return;
		
		Step[] stepArray = source.getSteps();
		int len = stepArray.length;
		double[] dataX = new double[len];
		double[] dataY = new double[len];
		boolean[] valid = new boolean[len];
		// first assemble world positions of source
		for (int i = 0; i < len; i++) {
			PositionStep curStep = (PositionStep)stepArray[i];
			valid[i] = curStep!=null? true: false;
			if (curStep != null) {
				// only keep those in video clip

				TPoint p = curStep.getPosition();
				Point2D wp = p.getWorldPosition(source.tp);
				dataX[i] = wp.getX();
				dataY[i] = wp.getY();
				if (Double.isNaN(dataX[i]) || Double.isNaN(dataY[i])) {
					valid[i] = false;
				}
			}
		}
		
		// then obtain the filtered positions and determine rmsDev
		double[] filteredX = filter == null? dataX: filter.apply(dataX, valid);
		double[] filteredY = filter == null? dataY: filter.apply(dataY, valid);
		
		double sumOfSquaresX = 0, sumOfSquaresY = 0, dev = 0;
		int count = 0;
		for (int i = 0; i < filteredX.length; i++) {
			if (!valid[i])
				continue;
			count++;
			dev = filteredX[i] - dataX[i];
			sumOfSquaresX += dev * dev;
			dev = filteredY[i] - dataY[i];
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
			for (int i = 0; i < len; i++) {
				PositionStep myStep = (PositionStep)mySteps[i];
				if (valid[i]) {
					// determine image position
					p.setWorldPosition(filteredX[i], filteredY[i], source.tp);
					if (myStep == null) {
						myStep = (PositionStep) createStep(i, p.x, p.y);
					}
					else {
						myStep.getPosition().setPosition(p);
					}
				}
				else { // invalid--Step should be null
					mySteps[i] = null;
				}
			}
		}
		loading = false;
		refreshDataLater = false;
		dataValid = false;
		DatasetManager data = getData(tp); // refreshes datasets
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
