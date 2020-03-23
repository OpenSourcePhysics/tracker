package javajs.async;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.function.Function;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

/**
 * A simple Asynchronous file chooser for JavaScript and Java.
 * 
 * Requires an OK runnable; JavaScript can return notification of cancel for
 * file reading only, not saving.
 * 
 * @author Bob Hanson
 */

public class AsyncFileChooser extends JFileChooser implements PropertyChangeListener {

	private int optionSelected;
	private Runnable ok, cancel; // sorry, no CANCEL in JavaScript for file open
	private boolean isAsyncSave = true;
	private static boolean notified;

	public AsyncFileChooser() {
		super();
	}

	public AsyncFileChooser(File file) {
		super(file);
	}

	public AsyncFileChooser(File file, FileSystemView view) {
		super(file, view);
	}

	@Deprecated
	@Override
	public int showDialog(Component frame, String btnText) {
		// This one can come from JFileChooser - default is OPEN
		return super.showDialog(frame, btnText);
	}

	private int err() {
		try {
			throw new java.lang.IllegalAccessException("Warning! AsyncFileChooser interface bypassed!");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return JFileChooser.ERROR_OPTION;
	}

	@Deprecated
	@Override
	public int showOpenDialog(Component frame) {
		return err();
	}

	@Override
	public int showSaveDialog(Component frame) {
		isAsyncSave  = false;
		return super.showSaveDialog(frame);
	}

	/**
	 * 
	 * @param frame
	 * @param btnLabel "open" or "save"
	 * @param ok
	 * @param cancel must be null; JavaScript cannot capture a cancel from a file dialog
	 */
	public void showDialog(Component frame, String btnLabel, Runnable ok, Runnable cancel) {
		this.ok = ok;
		if (getDialogType() != JFileChooser.SAVE_DIALOG && cancel != null)
			notifyCancel();
		process(super.showDialog(frame, btnLabel));
	}

	/**
	 * 
	 * @param frame
	 * @param ok
	 * @param cancel must be null; JavaScript cannot capture a cancel from a file dialog
	 */
	public void showOpenDialog(Component frame, Runnable ok, Runnable cancel) {
		this.ok = ok;
		if (cancel != null)
			notifyCancel();
		process(super.showOpenDialog(frame));
	}

	/**
	 * 
	 * This just completes the set. It is not necessary for JavaScript, because JavaScript
	 * will just throw up a simple modal OK/Cancel message anyway.
	 * 
	 * @param frame
	 * @param ok
	 * @param cancel must be null
	 */
	public void showSaveDialog(Component frame, Runnable ok, Runnable cancel) {
		this.ok = ok;
		this.cancel = cancel;
		process(super.showSaveDialog(frame));
	}

	
	/**
	 * Locate a file for input or output. Note that JavaScript will not return on cancel for OPEN_DIALOG.
	 * 
	 * @param title       The title for the dialog
	 * @param mode        OPEN_DIALOG or SAVE_DIALOG
	 * @param processFile function to use when complete
	 */
	  public static void getFileAsync(Component parent, String title, int mode, Function<File, Void> processFile) {
		  // BH no references to this method. So changing its signature for asynchonous use
		  // And it didn't do as advertised - ran System.exit(0) if canceled
	    // create and display a file dialog
		AsyncFileChooser fc = new AsyncFileChooser();
		fc.setDialogTitle(title);
		Runnable after = new Runnable() {

			@Override
			public void run() {
				processFile.apply(fc.getSelectedFile());
			}
	
		};
		if (mode == JFileChooser.OPEN_DIALOG) {
			fc.showOpenDialog(parent, after, after);  
		} else {
			fc.showSaveDialog(parent, after, after);  
		}
				
	  }
	    
		/**
		 * Run yes.run() if a file doesn't exist or if the user allows it, else run no.run()
		 * @param parent
		 * @param filename
		 * @param title
		 * @param yes (approved)
		 * @param no (optional)
		 */
		public static void checkReplaceFileAsync(Component parent, File outfile, String title, Runnable yes, Runnable no) {
			if (outfile.exists()) {
				AsyncDialog.showYesNoAsync(parent,
						outfile + " exists. Replace it?", null, new ActionListener() {
		
							@Override
							public void actionPerformed(ActionEvent e) {
								switch (e.getID()) {
								case JOptionPane.YES_OPTION:
									yes.run();
									break;
								default:
									if (no != null)
										no.run();
									break;
								}
							}
		
						});
		
			} else {
				yes.run();
			}
		
		}

	private void notifyCancel() {
		if (!notified) {
			System.err.println("developer note: JavaScript cannot fire a FileChooser CANCEL action");
		}
		notified = true;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case "SelectedFile":
		case "SelectedFiles":
			process(optionSelected = (evt.getNewValue() == null ? CANCEL_OPTION : APPROVE_OPTION));
			break;
		}
	}

	private void process(int ret) {
		if (ret != -(-ret))
			return; // initial JavaScript return is NaN
		optionSelected = ret;
		File f = getSelectedFile();
		if (f == null) {
			if (cancel != null)
				cancel.run();
		} else {
			if (ok != null)
				ok.run();
		}
	}

	public int getSelectedOption() {
		return optionSelected;
	}

	public static byte[] getFileBytes(File f) {
		return /** @j2sNative f.秘bytes || */null;
	}

}
