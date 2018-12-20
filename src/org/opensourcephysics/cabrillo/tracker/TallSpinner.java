package org.opensourcephysics.cabrillo.tracker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * Spinner with same preferred height as another component.
 */
class TallSpinner extends JSpinner {

	Component comp;

	TallSpinner(SpinnerModel model, Component heightComponent) {
		super(model);
		comp = heightComponent;
		JFormattedTextField tf = getTextField();
		tf.setEnabled(false);
		tf.setDisabledTextColor(Color.BLACK);
	}

	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.height = comp.getPreferredSize().height;
		return dim;
	}

	public JFormattedTextField getTextField() {
		return ((JSpinner.DefaultEditor) getEditor()).getTextField();
	}

	public void addMouseListenerToAll(MouseAdapter mouseOverListener) {
		for (int i = 0; i < getComponentCount(); i++) {
			getComponent(i).addMouseListener(mouseOverListener);
		}
		getTextField().addMouseListener(mouseOverListener);
	}

}
