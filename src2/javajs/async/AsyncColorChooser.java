package javajs.async;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JColorChooser;
import javax.swing.plaf.UIResource;

/**
 * A simple Asynchronous file chooser for JavaScript; synchronous with Java.
 * 
 * Allows two modes -- using an ActionListener (setAction(ActionListener) or constructor(ActionListener))
 * 
 * @author Bob Hanson
 */

public class AsyncColorChooser implements PropertyChangeListener {

	private ActionListener listener;
	private Color selectedColor;

	public void showDialog(Component component, String title, Color initialColor, ActionListener listener) {
		setListener(listener);
		process(JColorChooser.showDialog(component, title, initialColor));
		unsetListener();
	}

	public Color getSelectedColor() {
		return selectedColor;
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// JavaScript only
		Color c = (Color) evt.getNewValue();
		
		switch (evt.getPropertyName()) {
		case "SelectedColor":
			process(c);
			break;
		}
	}

	private void setListener(ActionListener a) {
		listener = a;
		/** @j2sNative Clazz.load("javax.swing.JColorChooser");javax.swing.JColorChooser.listener = this */
	}

	private void unsetListener() {
		/** @j2sNative javax.swing.JColorChooser.listener = null */
	}

	
	
	private void process(Color c) {
		if (c instanceof UIResource)
			return;
		selectedColor = c;
		listener.actionPerformed(new ActionEvent(this, c == null ? 0 : c.getRGB(), c == null ? null : c.toString()));
	}
	
}
