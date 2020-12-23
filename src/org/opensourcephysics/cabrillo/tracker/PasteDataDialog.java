/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.media.mov.MovieVideoI;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.LibraryBrowser;
import org.opensourcephysics.tools.ResourceLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * A dialog for viewing and setting document properties and metadata.
 *
 * @author Douglas Brown
 */
public class PasteDataDialog extends JDialog {
	
  protected TrackerPanel trackerPanel;
	protected JButton okButton, cancelButton;
  protected JTextArea textArea;
  protected JLabel label;
  
  /**
   * Constructor.
   *
   * @param panel the tracker panel
   */
  public PasteDataDialog(TrackerPanel panel) {
    super(panel.getTFrame(), true);
    trackerPanel = panel;
    createGUI();
    setFontLevel(FontSizer.getLevel());
    pack();
		setLocationRelativeTo(trackerPanel);
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
  
  @Override
  public void setVisible(boolean vis) {
  	super.setVisible(vis);
//  	if (!OSPRuntime.isJS)
//  		dispose();
  }
  
//_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {
  	setTitle(TrackerRes.getString("PropertiesDialog.Title")); //$NON-NLS-1$
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    textArea = new JTextArea(20, 30);
    JScrollPane scroller = new JScrollPane(textArea);
    contentPane.add(scroller, BorderLayout.CENTER);
    
    // create buttons
    okButton = new JButton(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    okButton.setForeground(new Color(0, 0, 102));
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
      	String data = textArea.getText();
        setVisible(false);
        trackerPanel.doPaste(data);
      }
    });
    cancelButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    cancelButton.setForeground(new Color(0, 0, 102));
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    // create buttonbar at bottom
    JPanel buttonbar = new JPanel();
    buttonbar.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(okButton);
    buttonbar.add(cancelButton);
  }
  
  
}
