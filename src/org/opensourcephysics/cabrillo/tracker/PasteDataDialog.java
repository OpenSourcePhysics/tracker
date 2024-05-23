/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2024 Douglas Brown, Wolfgang Christian, Robert M. Hanson
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import org.opensourcephysics.tools.FontSizer;

/**
 * A dialog for pasting delimited text data in JS.
 *
 * @author Douglas Brown
 */
public class PasteDataDialog extends JDialog {

	protected TFrame frame;
	protected Integer panelID;

	protected JButton okButton, cancelButton;
	protected JTextArea messageArea;
	protected JTextArea textArea;
	protected JLabel label;

	/**
	 * Constructor.
	 *
	 * @param panel the tracker panel
	 */
	public PasteDataDialog(TrackerPanel panel) {
		super(panel.getTFrame(), true);
		frame = panel.getTFrame();
		panelID = panel.getID();
		createGUI();
		setFontLevel(FontSizer.getLevel());
		pack();
		setLocationRelativeTo(panel);
		okButton.requestFocusInWindow();
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
	}

//_____________________________ private methods ____________________________

	/**
	 * Creates the visible components of this panel.
	 */
	private void createGUI() {
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		messageArea = new JTextArea();
		messageArea.setEditable(false);
		Border empty = BorderFactory.createEmptyBorder(2, 4, 2, 4);
		Border etched = BorderFactory.createEtchedBorder();
		messageArea.setBorder(BorderFactory.createCompoundBorder(etched, empty));
		messageArea.setBackground(contentPane.getBackground());
		messageArea.setForeground(Color.black);
		contentPane.add(messageArea, BorderLayout.NORTH);

		textArea = new JTextArea(15, 30);
		textArea.setBorder(empty);
		textArea.setFont(new Font("monospaced", Font.PLAIN, textArea.getFont().getSize()));
		JScrollPane scroller = new JScrollPane(textArea);
		contentPane.add(scroller, BorderLayout.CENTER);

		// create buttons
		okButton = new JButton();
		okButton.setForeground(new Color(0, 0, 102));
		okButton.addActionListener((e) -> {
			String data = textArea.getText();
//      setVisible(false);
			frame.getTrackerPanelForID(panelID).doPaste(data);
		});
		cancelButton = new JButton();
		cancelButton.setForeground(new Color(0, 0, 102));
		cancelButton.addActionListener((e) -> {
			setVisible(false);
		});
		// create buttonbar at bottom
		JPanel buttonbar = new JPanel();
		buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
		buttonbar.add(okButton);
		buttonbar.add(cancelButton);
		contentPane.add(buttonbar, BorderLayout.SOUTH);
		refreshGUI();
	}

	/**
	 * Refreshes the visible components of this panel.
	 */
	protected void refreshGUI() {
		setTitle(TrackerRes.getString("PasteDataDialog.Title")); //$NON-NLS-1$
		okButton.setText(TrackerRes.getString("Dialog.Button.Apply")); //$NON-NLS-1$
		cancelButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		String s = TrackerRes.getString("PasteDataDialog.Message1");
		s += "\n" + TrackerRes.getString("PasteDataDialog.Message2");
		messageArea.setText(s);
	}

}
