/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.cabrillo.tracker.analytics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.opensourcephysics.tools.Resource;

/**
 * A program to read the launch/download "uncounted" files on the server and write to a local file.
 *
 * @author Doug Brown
 * @version 1.0
 */
public class UncountedFilesReader extends JFrame {
	
	private String NEW_LINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private String launchListPage = "list_"; //$NON-NLS-1$
	private String downloadListFile = "list__list"; //$NON-NLS-1$
	private String launchClearPage = "clear_"; //$NON-NLS-1$
	private String downloadClearFile = "clear__clear"; //$NON-NLS-1$
	private String launchPHPPath = "http://physlets.org/tracker/counter/counter.php?page="; //$NON-NLS-1$
	private String downloadPHPPath = "http://physlets.org/tracker/installers/download.php?file="; //$NON-NLS-1$

	JTextArea textArea;
	
	private UncountedFilesReader() {
		super("Uncounted PHP calls"); //$NON-NLS-1$
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		
		textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(200, 400));
		JScrollPane scroller = new JScrollPane(textArea);
		contentPane.add(scroller, BorderLayout.CENTER);
		
		JPanel buttonbar = new JPanel();
		contentPane.add(buttonbar, BorderLayout.SOUTH);
		
		Box box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 10));

		JButton button = new JButton("List"); //$NON-NLS-1$
		box.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listAll();
			}
		});
		buttonbar.add(box);
		
		box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
		
		JLabel label = new JLabel("Launches"); //$NON-NLS-1$
		label.setAlignmentX(LEFT_ALIGNMENT);
		box.add(label);
		
		button = new JButton("Clear"); //$NON-NLS-1$
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String result = send(launchPHPPath+launchClearPage);
				textArea.setText(result);
			}
		});
		box.add(button);
		
		button = new JButton("Send"); //$NON-NLS-1$
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea.getText().trim();
				if ("".equals(text)) return; //$NON-NLS-1$
				String result = send(launchPHPPath+text);
				textArea.setText(result);
			}
		});
		box.add(button);
		buttonbar.add(box);
		
		box = Box.createVerticalBox();
		box.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 20));
		
		label = new JLabel("Downloads"); //$NON-NLS-1$
		box.add(label);
	
		button = new JButton("Clear"); //$NON-NLS-1$
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String result = send(downloadPHPPath+downloadClearFile);
				textArea.setText(result);
			}
		});
		box.add(button);
		
		button = new JButton("Send"); //$NON-NLS-1$
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textArea.getText().trim();
				if ("".equals(text)) return; //$NON-NLS-1$
				String result = send(downloadPHPPath+text);
				textArea.setText(result);
			}
		});
		box.add(button);
		buttonbar.add(box);
		
		pack();
		
	}
	
	private void listAll() {
    // create StringBuffer
    StringBuffer buffer = new StringBuffer();
  	
    // append uncounted launch page names
  	String uncounted = send(launchPHPPath+launchListPage);
  	if (!"".equals(uncounted)) { //$NON-NLS-1$
	  	buffer.append("Launches:"+NEW_LINE);  		 //$NON-NLS-1$
	  	buffer.append(uncounted);  		
  	}
  	
    // append uncounted download file names
  	uncounted = send(downloadPHPPath+downloadListFile);
  	if (!"".equals(uncounted)) { //$NON-NLS-1$
  		if (!"".equals(buffer.toString())) { //$NON-NLS-1$
  			buffer.append(NEW_LINE);
  			buffer.append(NEW_LINE);
  		}
	  	buffer.append("Downloads:"+NEW_LINE);  		 //$NON-NLS-1$
	  	buffer.append(uncounted);  		
  	}

		textArea.setText(buffer.toString());
	}
	
	private String send(String command) {
    try {
			URL url = new URL(command);
			Resource res = new Resource(url);
	    return res.getString().trim();
		} catch (MalformedURLException e) {
		}
  	return null;
  }
  
  public static void main(String[] args) {
  	UncountedFilesReader app = new UncountedFilesReader();
    // center on screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - app.getBounds().width) / 2;
    int y = (dim.height - app.getBounds().height) / 2;
    app.setLocation(x, y);
    // display
  	app.setVisible(true);		
  }

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
