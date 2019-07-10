/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <https://www.compadre.org/osp/>
 */

package org.opensourcephysics.cabrillo.tracker.analytics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.opensourcephysics.tools.Resource;

/**
 * Reads accumulated counts on the Tracker server.
 *
 * @author Doug Brown
 * @version 1.0
 */
public class TrackerCountReader extends JFrame {
	
	private String NEW_LINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private String launchListPage = "list_"; //$NON-NLS-1$
	private String downloadListFile = "list__list"; //$NON-NLS-1$
	private String launchClearPage = "clear_"; //$NON-NLS-1$
	private String downloadClearFile = "clear__clear"; //$NON-NLS-1$
	private String launchPHPPath = "https://physlets.org/tracker/counter/counter.php?page="; //$NON-NLS-1$
	private String downloadPHPPath = "https://physlets.org/tracker/installers/download.php?file="; //$NON-NLS-1$
	private String[] actions = {"read launch counts", "read downloads", "version", "list launch log failures", "list download failures",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"clear launch log failures", "clear download failures", "test launch log", "test downloads"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private String[] versions = {"all", "5.1.0", "5.0.7", "5.0.6", "5.0.5", "5.0.4", "5.0.3", "5.0.2",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"5.0.1", "5.0.0", "4.11.0", "4.10.0", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"4.9.8", "4.97", "4.96", "4.95", "4.94",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
			"4.93", "4.92", "4.91", "4.90"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private String[] OSs = {"all", "windows", "osx", "linux"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private String[] engines = {"all", "Xuggle", "none"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	JComboBox actionDropdown, versionDropdown, osDropdown, engineDropdown;
	JLabel actionLabel, versionLabel, osLabel, engineLabel;

	JTextArea textArea;
	JButton sendButton;
	
	private TrackerCountReader() {
		super("Tracker Count Reader"); //$NON-NLS-1$
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		
		actionLabel = new JLabel("Action"); //$NON-NLS-1$
		versionLabel = new JLabel("Version"); //$NON-NLS-1$
		osLabel = new JLabel("OS"); //$NON-NLS-1$
		engineLabel = new JLabel("Engine"); //$NON-NLS-1$
		
		actionDropdown = new JComboBox(actions);
		versionDropdown = new JComboBox(versions);
		osDropdown = new JComboBox(OSs);
		engineDropdown = new JComboBox(engines);
		actionDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
		versionDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
		osDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
		engineDropdown.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
		
		Box actionBox = Box.createVerticalBox();
		actionBox.add(leftJustify(actionLabel));
		actionBox.add(actionDropdown);
		Box versionBox = Box.createVerticalBox();
		versionBox.add(leftJustify(versionLabel));
		versionBox.add(versionDropdown);
		Box osBox = Box.createVerticalBox();
		osBox.add(leftJustify(osLabel));
		osBox.add(osDropdown);
		Box engineBox = Box.createVerticalBox();
		engineBox.add(leftJustify(engineLabel));
		engineBox.add(engineDropdown);
		
		Box box = Box.createHorizontalBox();
		box.setBorder(BorderFactory.createEmptyBorder(4, 7, 2, 7));
		box.add(actionBox);
		box.add(versionBox);
		box.add(osBox);
		box.add(engineBox);
		
		sendButton = new JButton("Send"); //$NON-NLS-1$
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textArea.getForeground().equals(Color.RED.darker())) {
					String text = textArea.getText().trim();
					String result = send(launchPHPPath+text);
	      	textArea.setForeground(Color.BLACK);
					textArea.setText(result);
					return;
				}
				else {
					String[] ver = null, os = null, eng = null;
					String action = actionDropdown.getSelectedItem().toString();
					if (action.contains("list")) { //$NON-NLS-1$
						String result = null;
						if (action.contains("launch")) { //$NON-NLS-1$
							result = send(launchPHPPath+launchListPage);
					  	if ("".equals(result)) result = "(no launch log failures)"; //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							result = send(downloadPHPPath+downloadListFile);
							if ("".equals(result)) result = "(no download failures)"; //$NON-NLS-1$ //$NON-NLS-2$
						}
		      	textArea.setForeground(Color.BLACK);
						textArea.setText(result);
						return;
					}
					else if (action.equals("version")) { //$NON-NLS-1$
		      	textArea.setForeground(Color.BLACK);
						textArea.setText(send(launchPHPPath+"version")); //$NON-NLS-1$
						return;
					}
					else if (action.contains("clear")) { //$NON-NLS-1$
						String result = null;
						if (action.contains("launch")) { //$NON-NLS-1$
							result = send(launchPHPPath+launchClearPage);
							if ("".equals(result)) result = "(cleared launch log failures)"; //$NON-NLS-1$ //$NON-NLS-2$
						}
						else {
							result = send(downloadPHPPath+downloadClearFile);
							if ("".equals(result)) result = "(cleared download failures)"; //$NON-NLS-1$ //$NON-NLS-2$
						}
		      	textArea.setForeground(Color.BLACK);
						textArea.setText(result);
						return;
					}
					else { // action is "read..." or "test..."
						if (versionDropdown.getSelectedItem().equals("all")) { //$NON-NLS-1$
							ver = new String[versions.length-1];
							for (int i=0; i<ver.length; i++) {
								ver[i] = versions[i+1];
							}
						}
						else {
							ver = new String[] {versionDropdown.getSelectedItem().toString()};
						}
						if (osDropdown.getSelectedItem().equals("all")) { //$NON-NLS-1$
							os = new String[OSs.length-1];
							for (int i=0; i<os.length; i++) {
								os[i] = OSs[i+1];
							}
						}
						else {
							os = new String[] {osDropdown.getSelectedItem().toString()};
						}
						if (engineDropdown.getSelectedItem().equals("all")) { //$NON-NLS-1$
							eng = new String[engines.length-1];
							for (int i=0; i<eng.length; i++) {
								eng[i] = engines[i+1];
							}
						}
						else {
							eng = new String[] {engineDropdown.getSelectedItem().toString()};
						}
					}
					String result = send(actionDropdown.getSelectedItem().toString(), ver, os, eng);
	      	textArea.setForeground(Color.BLACK);
					String command = versionDropdown.getSelectedItem()+"_"+osDropdown.getSelectedItem(); //$NON-NLS-1$
					if (!action.contains("download")) { //$NON-NLS-1$
						command += "_"+engineDropdown.getSelectedItem(); //$NON-NLS-1$
					}
					textArea.setText(action+" "+command+": "+result); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		});
		
		JPanel top = new JPanel(new BorderLayout());
		top.add(box, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
		buttonPanel.add(sendButton, BorderLayout.NORTH);
		top.add(buttonPanel, BorderLayout.SOUTH);
		contentPane.add(top, BorderLayout.NORTH);		
		
		textArea = new JTextArea();
		textArea.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	textArea.setForeground(Color.RED.darker());
      }
    });
		JScrollPane scroller = new JScrollPane(textArea);
		scroller.setPreferredSize(new Dimension(200, 400));
		contentPane.add(scroller, BorderLayout.CENTER);
		
		pack();		
	}
	
	private String send(String command) {
    try {
			URL url = new URL(command);
			Resource res = new Resource(url);
	    return res.getString().trim();
		} catch (Exception e) {
		}
  	return null;
  }
	
	private String send(final String action, String[] ver, String[] os, String[] eng) {
		// action is "read..." or "test..."
		int counts = 0;
		String commands = ""; //$NON-NLS-1$
		for (int i=0; i<ver.length; i++) {
			for (int j=0; j<os.length; j++) {
				if ((action.contains("test") || action.contains("read")) && action.contains("download")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					// read or test downloads
					String suffix = action.contains("read")? "__read": "_test"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					// typical Tracker-4.9.8-windows-installer.exe
					// typical Tracker-4.9.8-osx-installer.zip
					// typical Tracker-4.9.8-linux-32bit-installer.run
					// typical Tracker-4.9.8-linux-64bit-installer.run
					String osname = os[j];
					String ext = osname.equals("windows")? ".exe": osname.equals("osx")? ".zip": ".run"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					if (osname.equals("linux")) { //$NON-NLS-1$
						osname = "linux-32bit"; //$NON-NLS-1$
					}
					String command = "Tracker-"+ver[i]+"-"+osname+"-installer"+ext; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					String result = send(downloadPHPPath+command+suffix);
					commands += NEW_LINE+command+": "+result; //$NON-NLS-1$
					if (action.contains("read")) { //$NON-NLS-1$
						try {
							result = result.replaceAll(",", ""); //$NON-NLS-1$ //$NON-NLS-2$
							int n = Integer.parseInt(result);
							counts += n;
						} catch (NumberFormatException e) {
							return "failed to parse "+result; //$NON-NLS-1$
						}
					}
					if (osname.contains("linux")) { //$NON-NLS-1$
						command = "Tracker-"+ver[i]+"-linux-64bit-installer"+ext; //$NON-NLS-1$ //$NON-NLS-2$
						result = send(downloadPHPPath+command+suffix);
						commands += NEW_LINE+command+": "+result; //$NON-NLS-1$
						if (action.contains("read")) { //$NON-NLS-1$
							try {
								result = result.replaceAll(",", ""); //$NON-NLS-1$ //$NON-NLS-2$
								int n = Integer.parseInt(result);
								counts += n;
							} catch (NumberFormatException e) {
								return "failed to parse "+result; //$NON-NLS-1$
							}
						}
					}
				}
				else { // read or test launch counts
					for (int k=0; k<eng.length; k++) {
						String osname = os[j];
						if (osname.equals("osx")) osname = "macosx"; //$NON-NLS-1$ //$NON-NLS-2$
						String command = "read_"+ver[i]+"_"+osname+"_"+eng[k]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (action.contains("test")) { //$NON-NLS-1$
							command = "log_"+ver[i]+"_"+osname+"_"+eng[k]+"test"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						}
						String result = send(launchPHPPath+command);
						commands += NEW_LINE+ver[i]+"_"+osname+"_"+eng[k]+": "+result; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (action.contains("read")) { //$NON-NLS-1$
							try {
								result = result.replaceAll(",", ""); //$NON-NLS-1$ //$NON-NLS-2$
								int n = Integer.parseInt(result);
								counts += n;
							} catch (NumberFormatException e) {
								return "failed to parse "+result; //$NON-NLS-1$
							}
						}
					}
				}
			}
		}
		String s = String.valueOf(counts);
		if (action.contains("test")) { //$NON-NLS-1$
			if (action.contains("launch")) s = "launch log attempts"; //$NON-NLS-1$ //$NON-NLS-2$
			else  s = "download attempts"; //$NON-NLS-1$
		}
		if (ver.length>1 || os.length>1 || eng.length>1) {
			s += NEW_LINE+commands;
		}
		return s;
	}
  
	private Component leftJustify(Component c)  {
    Box  b = Box.createHorizontalBox();
    b.add( c );
    b.add( Box.createHorizontalGlue() );
    b.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
    return b;
	}
	
  public static void main(String[] args) {
  	TrackerCountReader app = new TrackerCountReader();
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
 * Copyright (c) 2019  The Open Source Physics project
 *                     https://www.compadre.org/osp
 */
