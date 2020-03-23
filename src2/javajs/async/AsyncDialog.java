package javajs.async;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.plaf.UIResource;

/**
 * A class to manage asynchronous input, option, and confirmation dialogs.
 * 
 * @author Bob Hanson hansonr_at_stolaf.edu
 *
 */
public class AsyncDialog implements PropertyChangeListener {

// see discussion in net.sf.j2s.core/doc/Differences.txt
//
// Confirmation dialog example. Note moving the parent component into the constructor.
// Original:
//
//		private void promptQuit() {
//			int sel = JOptionPane.showConfirmDialog(null, PROMPT_EXIT, NAME, JOptionPane.YES_NO_OPTION);
//			switch (sel) {
//			case JOptionPane.YES_OPTION:
//				resultsTab.clean();
//				seqs.dispose();
//				if (fromMain) {
//					System.exit(0);
//				}
//				break;
//			}
//		}
//
// revised: 
//
//		private void promptQuitAsync() {
//			new AsyncDialog().showConfirmDialog(null, PROMPT_EXIT, NAME, JOptionPane.YES_NO_OPTION, new ActionListener() {
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//	    int sel = ((AsyncDialog)e.getSource()).getOption();
//		switch (sel) {
//		case JOptionPane.YES_OPTION:
//			resultsTab.clean();
//			seqs.dispose();
//			if (fromMain) {
//				System.exit(0);
//			}
//			break;
//		}
//	}}));
//		}

	
	public AsyncDialog() {
	}
	
	private ActionListener actionListener;
	private Object choice;
	private Object[] options;

	// These options can be supplemented as desired.

	
	/**
	 * Synchronous call; OK in JavaScript as long as we are using a JavaScript prompt() call
	 * 
	 * @param frame
	 * @param msg
	 * @return
	 */
	@Deprecated
	public static String showInputDialog(Component frame, String msg) {
		return JOptionPane.showInputDialog(frame, msg);
	}

	public void showInputDialog(Component frame, Object message, ActionListener a) {
		setListener(a);
		process(JOptionPane.showInputDialog(frame, message));
		unsetListener();
	}

	public void showInputDialog(Component frame, Object message, String title, int messageType, Icon icon,
			Object[] selectionValues, Object initialSelectionValue, ActionListener a) {
		setListener(a);
		process(JOptionPane.showInputDialog(frame, message, title, messageType, icon, selectionValues,
				initialSelectionValue));
		unsetListener();
	}

	public void showMessageDialog(Component frame, Object message, ActionListener a) {
		setListener(a);
		JOptionPane.showMessageDialog(frame, message);
		unsetListener();
		if (/** @j2sNative false || */true)
			process("" + message);
	}

	public void showOptionDialog(Component frame, Object message, String title, int optionType, int messageType,
			Icon icon, Object[] options, Object initialValue, ActionListener a) {
		actionListener = a;
		this.options = options;
		setListener(a);
		process(JOptionPane.showOptionDialog(frame, message, title, optionType, messageType, icon, options,
				initialValue));
		unsetListener();
	}

	public void showConfirmDialog(Component frame, Object message, String title, ActionListener a) {
		showConfirmDialog(frame, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, a);
	}

	public void showConfirmDialog(Component frame, Object message, String title, int optionType, ActionListener a) {
		showConfirmDialog(frame, message, title, optionType, JOptionPane.QUESTION_MESSAGE, a);
	}

	public void showConfirmDialog(Component frame, Object message, String title, int optionType, int messageType,
			ActionListener a) {
		setListener(a);
		process(JOptionPane.showConfirmDialog(frame, message, title, optionType, messageType));
		unsetListener();
	}

	/**
	 * retrieve selection from the ActionEvent, for which "this" is getSource()
	 * 
	 * @return
	 */
	public Object getChoice() {
		return choice;
	}

	public int getOption() {
		if (!(choice instanceof Integer)) {
			throw new java.lang.IllegalArgumentException("AsyncDialog.getOption called for non-Integer choice");
		}
		return ((Integer) choice).intValue();
	}

	/**
	 * A dialog option that allows for YES, NO, and CLOSE options via
	 * ActionListener. ActionEvent.getID() contains the reply.
	 * 
	 * @param parent   The parent component for the dialog
	 * @param message  The text of the message to display
	 * @param title    Optional title defaults to "Question"
	 * @param listener Handle options based on an ActionEvent
	 */
	public static void showYesNoAsync(Component parent, Object message, String title, ActionListener listener) {
		new AsyncDialog().showConfirmDialog(parent, message, (title == null ? "Question" : title),
				JOptionPane.YES_NO_OPTION, listener);
	}

	/**
	 * A dialog option that involves just a YES follower.
	 * @param parent
	 * @param message
	 * @param title TODO
	 * @param yes
	 */
	public static void showYesAsync(Component parent, Object message, String title, Runnable yes) {
		AsyncDialog.showYesNoAsync(parent, message, title, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getID() == JOptionPane.YES_OPTION) {
					yes.run();
				}
			}
			
		});
	}

	/**
	 * A dialog option that involves just an OK follower.
	 * @param parent
	 * @param message
	 * @param title
	 * @param ok
	 */
	public static void showOKAsync(Component parent, Object message, String title, Runnable ok) {
		new AsyncDialog().showConfirmDialog(parent, message, title, JOptionPane.OK_CANCEL_OPTION, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getID() == JOptionPane.OK_OPTION) {
					ok.run();
				}
			}
			
		});
	}


	private void setListener(ActionListener a) {
		actionListener = a;
		@SuppressWarnings("unused")
		Class c = JOptionPane.class; // loads the class
		/** @j2sNative c.$clazz$.listener = this */
	}

	private void unsetListener() {
		/** @j2sNative javax.swing.JOptionPane.listener = null */
	}

	/**
	 * Switch from property change to action.
	 * 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object val = evt.getNewValue();
		switch (evt.getPropertyName()) {
		case "inputValue":			
			process(val);
			break;
		case "value":
			if (val != null && options == null && !(val instanceof Integer)) {
				process(getOptionIndex(((JOptionPane) evt.getSource()).getOptions(), val));
				return;
			}
			if (options != null) {
				int i = getOptionIndex(options, val);
				val = Integer.valueOf(i >= 0 ? i : JOptionPane.CLOSED_OPTION);
			} 
			process(val);
			break;
		}
	}

	private int getOptionIndex(Object[] options, Object val) {
		if (options != null)
			for (int i = 0; i < options.length; i++) {
				if (options[i] == val)
					return i;
			}
		return -1;
	}

	private boolean processed;

	/**
	 * Return for confirm dialog.
	 * 
	 * @param ret may be JavaScript NaN, testable as ret != ret or ret != - -ret
	 */
	private void process(int ret) {
		if (ret != -(-ret) || processed)
			return;
		processed = true;
		choice = ret;
		actionListener.actionPerformed(new ActionEvent(this, ret, "SelectedOption"));
	}

	private void process(Object ret) {
		if (ret instanceof UIResource || processed)
			return;
		processed = true;
		choice = ret;
		actionListener.actionPerformed(new ActionEvent(this, 
				ret == null ? JOptionPane.CANCEL_OPTION : JOptionPane.OK_OPTION, 
						(ret == null ? null : ret.toString())));
	}


}
