package javajs.async;

/**
 * A package to manage asynchronous aspects of SwingJS
 * 
 * The javajs.async package simplifies the production of methods that can be
 * used equally well in Java and in JavaScript for handling "pseudo-modal"
 * blocking in JavaScript, meaning the user is locked out of other interactions,
 * as in Java, but the code is not actually blocking the thread.
 * 
 * Included in this package are
 * 
 * Async
 * 
 * Provides few simple generic static methods.
 * 
 * Async.isJS() -- true if we are running this in JavaScript
 * 
 * Async.javaSleep() -- bypassing Thread.sleep() for JavaScript; allowing it for
 * Java
 * 
 * 
 * AsyncDialog
 * 
 * Provides several very useful methods that act as a replacement for direct
 * JOptionPane or JDialog calls, including both a synchronous callback
 * (manifested in Java) and an asynchronous callback (JavaScript), both
 * resulting in the same effect, except for the fact that the JavaScript code
 * has returned immediately from the call with an "ignore me" reference, while
 * Java is waiting at that call to return a value from JOptionPane (which is
 * saved by AsyncDialog and not delivered as a return value).
 * 
 * AsyncDialog does not extend JOptionPane, but it mirrors that classes public
 * methods. There are a LOT of public methods in JOPtionPane. I suppose we
 * should implement them all. In practice, AsyncDialog calls standard
 * JOptionPane static classes for the dialogs.
 * 
 * Initially, the following methods are implemented:
 * 
 * public void showConfirmDialog(Component frame, Object message, String title,
 * ActionListener a)
 * 
 * public void showConfirmDialog(Component frame, Object message, String title,
 * int optionType, ActionListener a)
 * 
 * public void showConfirmDialog(Component frame, Object message, String title,
 * int optionType, int messageType, ActionListener a)
 * 
 * public void showInputDialog(Component frame, Object message, ActionListener
 * a)
 * 
 * public void showInputDialog(Component frame, Object message, String title,
 * int messageType, Icon icon, Object[] selectionValues, Object
 * initialSelectionValue, ActionListener a)
 * 
 * public void showMessageDialog(Component frame, Object message, ActionListener
 * a)
 * 
 * public void showOptionDialog(Component frame, Object message, String title,
 * int optionType, int messageType, Icon icon, Object[] options, Object
 * initialValue, ActionListener a)
 * 
 * 
 * All nonstatic methods, requiring new AsyncDialog(), also require an
 * ActionListener. This listener will get a call to actionPerformed(ActionEvent)
 * where:
 * 
 * event.getSource() is a reference to the originating AsyncDialog (super
 * JOptionPane) for all information that a standard JOptionPane can provide,
 * along with the two methods int getOption() and Object getChoice().
 * 
 * event.getID() is a reference to the standard JOptionPane int return code.
 * 
 * event.getActionCommand() also holds a value, but it may or may not be of
 * value.
 * 
 * 
 * A few especially useful methods are static, allowing just one or two expected
 * callbacks of interest:
 * 
 * AsyncDialog.showOKAsync(Component parent, Object message, String title,
 * Runnable ok)
 * 
 * AsyncDialog.showYesAsync (Component parent, Object message, String title,
 * Runnable yes)
 * 
 * AsyncDialog.showYesNoAsync (Component parent, Object message, String title,
 * Runnable yes, Runnable no)
 * 
 * These methods provide a fast way to adjust JOptionPane calls to be
 * asynchronous.
 * 
 * 
 * 
 * AsyncFileChooser extends javax.swing.JFileChooser
 * 
 * Accepted constructors include:
 * 
 * public AsyncFileChooser()
 * 
 * public AsyncFileChooser(File file)
 * 
 * public AsyncFileChooser(File file, FileSystemView view)
 * 
 * (Note, however, that FileSystemView has no equivalent in JavaScript.)
 * 
 * It's three public methods include:
 * 
 * public void showDialog(Component frame, String btnLabel, Runnable ok,
 * Runnable cancel)
 * 
 * public void showOpenDialog(Component frame, Runnable ok, Runnable cancel)
 * 
 * public void showSaveDialog(Component frame, Runnable ok, Runnable cancel)
 * 
 * 
 * ActionListener is not needed here, as the instance of new AsyncFileChooser()
 * already has direct access to all the JFileChooser public methods such as
 * getSelectedFile() and getSelectedFiles().
 * 
 * As a subclass of JFileChooser, it accepts all three public showXXXX methods
 * of JFileChooser, namely:
 * 
 * public void showDialog(Component frame, String btnLabel)
 * 
 * public void showOpenDialog(Component frame)
 * 
 * public void showSaveDialog(Component frame)
 * 
 * 
 * None of these are recommended. AsyncFileChooser will indicate errors if the
 * first of these two are called. (showSaveDialog is fine, as it is modal even
 * in JavaScript. However it is not recommended that showSaveDialog(Component
 * frame) be used, as in the future browsers may implement some sort of file
 * saver in HTML5.
 * 
 * 
 * 
 * AsyncColorChooser
 * 
 * 
 * AsyncColorChooser accesses JColorChooser asynchronously, using a private
 * SwingJS setting that tells JColorChooser to report back to it with property
 * changes. It is constructed using new AsyncColorChooser() and implements just
 * two methods:
 * 
 * public void showDialog(Component component, String title, Color initialColor,
 * ActionListener listener)
 * 
 * public Color getSelectedColor()
 * 
 * 
 * The listener will get an actionPerformed(ActionEvent) callback with
 * event.getID() equal to the color value or 0 if canceled. The
 * getSelectedColor() method may also be called from this callback to retrieve
 * the associated java.awt.Color object, using
 * 
 * ((AsyncColorChooser)e.getSource()).getSelectedColor()
 * 
 * As in Java, a null value for the selected color indicates that the
 * JColorChooser was closed.
 * 
 * Bob Hanson 2019.11.07
 * 
 * 
 * @author Bob Hanson hansonr_at_stolaf.edu
 *
 */
public class Async {

	public static boolean isJS() {
		return  (/** @j2sNative 1 ? true : */false);
	}

	/**
	 * No sleep in JavaScript
	 * @param ms
	 */
	public static void javaSleep(int ms) {
		if (!isJS()) {
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
			}
		}
	
	}

}
