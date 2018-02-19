/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
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

import java.beans.*;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.LaunchNode;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.ToolsRes;
import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.ResizableIcon;

/**
 * This is a toolbar that display selected track properties 
 * as well as memory and upgrade monitors/buttons.
 *
 * @author Douglas Brown
 */
public class TTrackBar extends JToolBar implements PropertyChangeListener {
	
  // static fields
  protected static Map<TrackerPanel, TTrackBar> trackbars = new HashMap<TrackerPanel, TTrackBar>();
  protected static JButton memoryButton, newVersionButton;
  protected static boolean outOfMemory = false;
  protected static Icon smallSelectIcon;
  protected static JButton testButton;
  protected static javax.swing.Timer testTimer;
  protected static boolean showOutOfMemoryDialog = true;
  protected static JDialog relaunchingDialog;
  protected static JLabel downloadLabel, relaunchLabel;
  private static JTextField sizingField = new JTextField();
  
  // instance fields
  protected TrackerPanel trackerPanel; // manages & displays track data
  protected Component toolbarEnd;
  protected int toolbarComponentHeight, numberFieldWidth;
  protected TButton trackButton;
  protected TButton selectButton;
  protected JLabel emptyLabel = new JLabel();
  protected JPopupMenu selectPopup = new JPopupMenu();

  static {
  	smallSelectIcon =  new ImageIcon(Tracker.class.getResource("resources/images/small_select.gif")); //$NON-NLS-1$
  	smallSelectIcon = new ResizableIcon(smallSelectIcon);
  	if (Tracker.testOn) {
	  	testButton = new JButton("test"); //$NON-NLS-1$
	  	testButton.addActionListener(new ActionListener() {
	  		public void actionPerformed(ActionEvent e) {
	    		final TFrame frame = (TFrame)testButton.getTopLevelAncestor();
	    		if (frame!=null && frame.getSelectedTab()>-1) {
	    			if (testTimer==null) {
	    				testTimer = new Timer(500, new ActionListener() {
		    	      public void actionPerformed(ActionEvent e) {
		    	  			// test action goes here
		    	      			    	      	
//		    	      	Tracker.newerVersion = "6.7.8";
//		    	      	TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
//		    	      	TTrackBar.getTrackbar(trackerPanel).refresh();
		    	      	
		    	      	if (!testTimer.isRepeats()) {
		  	    				testTimer.stop();
		  	    				testTimer=null;
		  	    			}
		    	      }
		    	    });
	    				testTimer.setInitialDelay(20);
	    				testTimer.setRepeats(false);
	    				testTimer.start();
	    			} // end timer is null
	    			else {
	    				testTimer.stop();
	    				testTimer=null;
	    			}
	    		}
	  		}
	  	});
    }
    memoryButton = new TButton() {
    	public JPopupMenu getPopup() {
        JPopupMenu popup = new JPopupMenu();
  	    JMenuItem memoryItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Memory.Menu.SetSize")); //$NON-NLS-1$
  	    popup.add(memoryItem);
  	    memoryItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  	    		TFrame frame = (TFrame)memoryButton.getTopLevelAncestor();
          	Object response = JOptionPane.showInputDialog(frame, 
                TrackerRes.getString("TTrackBar.Dialog.SetMemory.Message"),      //$NON-NLS-1$
                TrackerRes.getString("TTrackBar.Dialog.SetMemory.Title"),        //$NON-NLS-1$
                JOptionPane.PLAIN_MESSAGE, null, null, String.valueOf(Tracker.preferredMemorySize));
            if (response!=null && !"".equals(response.toString())) { //$NON-NLS-1$ 
            	String s = response.toString();
          		try {
          			double d = Double.parseDouble(s);
								d = Math.rint(d);
								int n = (int)d;
								if (n<0) n = -1; // default
								else n = Math.max(n, 32); // not less than 32MB
								if (n!=Tracker.preferredMemorySize) {
									Tracker.preferredMemorySize = n;								
			          	int ans = JOptionPane.showConfirmDialog(frame, 
			          			TrackerRes.getString("TTrackBar.Dialog.Memory.Relaunch.Message"),  //$NON-NLS-1$
			          			TrackerRes.getString("TTrackBar.Dialog.Memory.Relaunch.Title"),  //$NON-NLS-1$
			          			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			          	if (ans==JOptionPane.YES_OPTION) {
			          		Tracker.savePreferences();
			          		ArrayList<String> filenames = new ArrayList<String>();
			        			for (int i = 0; i<frame.getTabCount(); i++) {
			        				TrackerPanel next = frame.getTrackerPanel(i);
			        				if (!next.save()) return;
			        				File datafile = next.getDataFile();
			        				if (datafile!=null) {
			        	    		String fileName = datafile.getAbsolutePath();
			        	    		filenames.add(fileName);
			        				}
			        			}
			        			String[] args = filenames.isEmpty()? null: filenames.toArray(new String[0]);
			            	TrackerStarter.relaunch(args, false);
			          	}
								}
							} catch (Exception ex) {
							}
            }    				
//  	    		TFrame frame = (TFrame)memoryButton.getTopLevelAncestor();
//  	    		if (frame!=null && frame.getSelectedTab()>-1) {
//  	    			TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
//  	    			TActions.getAction("config", trackerPanel).actionPerformed(null); //$NON-NLS-1$
//  	    			Component c = frame.prefsDialog.runtimePanel;
//  	    			frame.prefsDialog.tabbedPane.setSelectedComponent(c);
//  	    		}
  	    	}
  	    });
  	    return popup;
    	}
    };
    Font font = memoryButton.getFont();
    memoryButton.setFont(font.deriveFont(Font.PLAIN, font.getSize()-1)); 
    memoryButton.addMouseListener(new MouseAdapter() {
    	public void mouseEntered(MouseEvent e) {
        refreshMemoryButton();
    	}
    });
		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
		Border line = BorderFactory.createLineBorder(Color.GRAY);
    memoryButton.setBorder(BorderFactory.createCompoundBorder(line, space));
    newVersionButton = new TButton() {
    	public JPopupMenu getPopup() {
        JPopupMenu popup = new JPopupMenu();
  	    JMenuItem upgradeItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Popup.MenuItem.Upgrade")); //$NON-NLS-1$
  	    popup.add(upgradeItem);
  	    upgradeItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
    				final TFrame frame = (TFrame)newVersionButton.getTopLevelAncestor();
    				// create relaunching dialog
  	    		if (relaunchingDialog==null) {
  	    			relaunchingDialog = new JDialog(frame, false);
	    				JPanel panel = new JPanel();
	    				panel.setBorder(BorderFactory.createEtchedBorder());
	    				relaunchingDialog.setContentPane(panel);	    				
	    				relaunchingDialog.setTitle(TrackerRes.getString("TTrackBar.Dialog.Relaunch.Title.Text")); //$NON-NLS-1$
	    				Box box = Box.createVerticalBox();
	    				relaunchingDialog.getContentPane().add(box);
	    				downloadLabel = new JLabel(); 
	    				downloadLabel.setBorder(BorderFactory.createEmptyBorder(10, 6, 6, 6));
	    				box.add(downloadLabel);
	    				relaunchLabel = new JLabel(); 
	    				relaunchLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 16, 6));
	    				box.add(relaunchLabel);
  	    		}
  	    		// look for upgrade tracker.jar
    				final boolean[] failed = new boolean[] {false};
  	    		int responseCode = 0; // code 200 = "OK"
   	    		final String jarFileName = "tracker-"+Tracker.newerVersion+".jar"; //$NON-NLS-1$ //$NON-NLS-2$
  	    		final String upgradeURL = ResourceLoader.getString("http://physlets.org/tracker/upgradeURL.txt"); //$NON-NLS-1$
  	    		if (upgradeURL!=null && Tracker.trackerHome!=null) {
	    				// see if the jar file is found at this url
	  	    		String upgradeFile = upgradeURL.trim()+jarFileName;
	  	    		try {
	  	    	    URL url = new URL(upgradeFile);
	  	    	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
	  	    	    responseCode = huc.getResponseCode();
		  	    	} catch (Exception ex) {
		  	    	}
  	    		}
      			if (responseCode!=200) { 
      				// jar file not found
      				failed[0] = true;
    				}
    				else if (OSPRuntime.isWindows()) {
    					// check for upgrade installer
	    				final String upgradeInstallerName = "TrackerUpgrade-"+Tracker.newerVersion+"-windows-installer.exe"; //$NON-NLS-1$ //$NON-NLS-2$
	  	    		final String upgradeInstallerURL = upgradeURL.trim()+upgradeInstallerName;
	  	    		responseCode = 0;
	  	    		try {
	  	    	    URL url = new URL(upgradeInstallerURL);
	  	    	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
	  	    	    responseCode = huc.getResponseCode();
		  	    	} catch (Exception ex) {
		  	    	}
	  	    		if (responseCode==200) { // upgrade installer exists
		  	    		// let user specify download directory
		  	    		String home = System.getProperty("user.home"); //$NON-NLS-1$
		  	    		File downloadDir = new File(home+"/Downloads"); //$NON-NLS-1$
		  	    		downloadDir = chooseDownloadDirectory(frame, downloadDir);
	    					if (downloadDir==null) { 
	    						// user cancelled
	    						return;
	    					}
	    					if (!downloadDir.exists()) {
	    						// failed to specify valid download directory
		      				OSPLog.warning("download directory does not exist: "+downloadDir); //$NON-NLS-1$
	    						failed[0] = true;
	    					}
	    					else {
	  	    				// inform user of intended action and ask permission
			          	int ans = JOptionPane.showConfirmDialog(frame, 
			          			TrackerRes.getString("TTrackBar.Dialog.Download.Upgrade.Message1")+" "+downloadDir.getPath()+"." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			          			+XML.NEW_LINE+TrackerRes.getString("TTrackBar.Dialog.Download.Upgrade.Message2")+XML.NEW_LINE,  //$NON-NLS-1$
			          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
			          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			          	if (ans!=JOptionPane.OK_OPTION) {
			          		return;
			          	}
	  	    				// download and launch installer in separate thread
	  	  	    		final File downloads = downloadDir;
	  	  	    		Runnable runner = new Runnable() {
	  	  	    			public void run() {
		  	    					// show relaunching dialog during download
			  	    				downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
			  	    						+" "+downloads.getPath()+".")); //$NON-NLS-1$ //$NON-NLS-2$
			  	    				relaunchLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.RelaunchLabel.Upgrade.Text"))); //$NON-NLS-1$
			  	    				relaunchingDialog.pack();
			  	    				// center on TFrame
			  	    		    relaunchingDialog.setLocationRelativeTo(frame);
			  	    				relaunchingDialog.setVisible(true);
			  	    				
  				  	    		// download upgrade installer			  	    				
			  	  	    		File installer = new File(downloads, upgradeInstallerName);
			  	  	    		installer = ResourceLoader.download(upgradeInstallerURL, installer, false);
			  	  	    		if (installer!=null && installer.exists()) {
			  	  	    			// launch the upgrade installer and close Tracker
												try {
													// assemble command: pass tracker home as parameter
						    	    		ArrayList<String> cmd = new ArrayList<String>();
						    	    		cmd.add("cmd"); //$NON-NLS-1$
						    	    		cmd.add("/c"); //$NON-NLS-1$
						    	    		cmd.add(installer.getPath());
						    	    		cmd.add("--tracker-home"); //$NON-NLS-1$
						    	    		cmd.add(Tracker.trackerHome);
						    	    		
						    	    		// log command
						    	    		String message = ""; //$NON-NLS-1$
						    	    		for (String next: cmd) {
						    	    			message += next + " "; //$NON-NLS-1$
						    	    		}
						    	    		OSPLog.info("executing command: " + message); //$NON-NLS-1$ 

						    	    		ProcessBuilder builder = new ProcessBuilder(cmd);
													Process p = builder.start();
													if (isAlive(p)) {
					  	  	    			// set preferred tracker to default
					  	  	    			Tracker.preferredTrackerJar = null;
					  	  	    			Tracker.savePreferences();
					  	  	    			// exit Tracker
														TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
														if (trackerPanel!=null) {
															Action exit = TActions.getAction("exit", trackerPanel); //$NON-NLS-1$
															exit.actionPerformed(null);
														}
														else {
															System.exit(0);
														}
													}
													else {
														// upgrade installer launch failure
							      				OSPLog.warning("failed to launch upgrade installer"); //$NON-NLS-1$
														failed[0] = true;	  	    			
													}
												} catch (Exception ex) {
						      				OSPLog.warning("exception: "+ex); //$NON-NLS-1$
							  	    		failed[0] = true;	  	    			
												}
			  	  	    		}
					  	    		else {
					  	    			// failed to download upgrade installer
					      				OSPLog.warning("failed to download upgrade installer"); //$NON-NLS-1$
						  	    		failed[0] = true;	  	    			
					  	    		} 
  		  	      			if (failed[0]) {
  		  	    					// close relaunching dialog and display Tracker web site
  		  	      				relaunchingDialog.setVisible(false);
  		  	      				relaunchingDialog.dispose();
  		  		  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
  		  		  	    		OSPDesktop.displayURL(websiteurl);
  		  	    				}
  	  	    				}
	  	  	    		}; // end runnable
	  	  	    		new Thread(runner).start();
	    					}
	  	    		} // end upgrade installer 
	  	    		else {
	  	    			// no upgrade installer so download tracker.jar
  	    				// inform user of intended action and ask permission
  	          	int ans = JOptionPane.showConfirmDialog(frame, 
  	          			TrackerRes.getString("TTrackBar.Dialog.Download.Message1")+" "+Tracker.trackerHome+"." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  	          			+XML.NEW_LINE+TrackerRes.getString("TTrackBar.Dialog.Download.Message2")+XML.NEW_LINE,  //$NON-NLS-1$
  	          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
  	          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
  	          	if (ans!=JOptionPane.OK_OPTION) {
  	          		// user cancelled
  	          		return;
  	          	}
   	  	    		Runnable runner = new Runnable() {
  	  	    			public void run() {
	  	    					// show relaunching dialog during downloads
  	  	    				downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Text") //$NON-NLS-1$
  	  	    						+" "+Tracker.trackerHome+".")); //$NON-NLS-1$ //$NON-NLS-2$
  	  	    				relaunchLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.RelaunchLabel.Text"))); //$NON-NLS-1$
  	  	    				relaunchingDialog.pack();
  	  	    				// center on TFrame
  	  	    		    relaunchingDialog.setLocationRelativeTo(frame);
  	  	    				relaunchingDialog.setVisible(true);
  	  	    				
    		      			// download new tracker jar
  	  	  	    		File jarFile = new File(Tracker.trackerHome, jarFileName);
  	   	  	    		String jarURL = upgradeURL.trim()+jarFileName;	  	    		
    		      			jarFile = ResourceLoader.download(jarURL, jarFile, false);

    		      			// also download new Tracker.exe if available
  	  	    				String starterName = "Tracker.exe"; //$NON-NLS-1$
  	  	  	    		String starterURL = upgradeURL.trim()+starterName;
  	  	  	    		int responseCode = 0;
    		  	    		try {
    		  	    	    URL url = new URL(starterURL);
    		  	    	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
    		  	    	    responseCode = huc.getResponseCode();
    			  	    	} catch (Exception ex) {
    			  	    	}
    		      			if (responseCode==200) {
    		      				// Tracker.exe is available
    	  	  	    		File starterTarget = new File(Tracker.trackerHome, starterName);
    	  	  	    		ResourceLoader.download(starterURL, starterTarget, true);
    		      			}

  	  	  	    		if (jarFile!=null && jarFile.exists()) { // new jar successfully downloaded
  	  	  	    			// launch new Tracker version
  	  	  	      		ArrayList<String> filenames = new ArrayList<String>();
  	  	  	    			for (int i = 0; i<frame.getTabCount(); i++) {
  	  	  	    				TrackerPanel next = frame.getTrackerPanel(i);
  	  	  	    				if (!next.save()) {
  	  	  	    					// user aborted the relaunch
  	  	  	    					relaunchingDialog.setVisible(false);
  	  	  	    					return;
  	  	  	    				}
  	  	  	    				File datafile = next.getDataFile();
  	  	  	    				if (datafile!=null) {
  	  	  	    	    		filenames.add(datafile.getAbsolutePath());
  	  	  	    				}
  	  	  	    			}
  	  	  	    			String[] args = filenames.isEmpty()? null: filenames.toArray(new String[0]);
  	  	  	  	    	System.setProperty(TrackerStarter.PREFERRED_TRACKER_JAR, jarFile.getAbsolutePath());
  	  	  	  	    	System.setProperty(TrackerStarter.TRACKER_NEW_VERSION, jarURL);
  	  	  	  	    	TrackerStarter.relaunch(args, false);
  	  	  	    		}
  	  	  	    		else {
				      				OSPLog.warning("failed to download new version"); //$NON-NLS-1$
  	  	  	    			failed[0] = true;
  	  	  	    		}
  	  	      			if (failed[0]) {
  	  	    					// close relaunching dialog and display Tracker web site
  	  	      				relaunchingDialog.setVisible(false);
  	  	      				relaunchingDialog.dispose();
  	  		  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
  	  		  	    		OSPDesktop.displayURL(websiteurl);
  	  	    				}
  	  	    			}
  	  	    		}; // end runnable
  	  	    		new Thread(runner).start();
	  	    		} // end new tracker.jar
    				} // end windows
      			else if (OSPRuntime.isMac()) { // OSX
	    				// see if a TrackerUpgrade zip is available
	    				final String zipFileName = "TrackerUpgrade-"+Tracker.newerVersion+"-osx-installer.zip"; //$NON-NLS-1$ //$NON-NLS-2$
	  	    		final String zipURL = upgradeURL.trim()+zipFileName;
	  	    		responseCode = 0;
	  	    		try {
	  	    	    URL url = new URL(zipURL);
	  	    	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
	  	    	    responseCode = huc.getResponseCode();
		  	    	} catch (Exception ex) {
		  	    	}
	  	    		if (responseCode==200) { // upgrade installer exists
		  	    		// let user specify download directory
		  	    		String home = System.getProperty("user.home"); //$NON-NLS-1$
		  	    		File downloadDir = new File(home+"/Downloads"); //$NON-NLS-1$
		  	    		downloadDir = chooseDownloadDirectory(frame, downloadDir);
	    					if (downloadDir==null) { 
	    						// user cancelled
	    						return;
	    					}
	    					if (!downloadDir.exists()) {
	    						// failed to specify valid download directory
		      				OSPLog.warning("download directory does not exist: "+downloadDir); //$NON-NLS-1$
	    						failed[0] = true;
	    					}
	    					else {
	  	    				// inform user of intended action and ask permission
			          	int ans = JOptionPane.showConfirmDialog(frame, 
			          			TrackerRes.getString("TTrackBar.Dialog.Download.Upgrade.Message1")+" "+downloadDir.getPath()+"." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			          			+XML.NEW_LINE+TrackerRes.getString("TTrackBar.Dialog.Download.Upgrade.Message2")+XML.NEW_LINE,  //$NON-NLS-1$
			          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
			          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			          	if (ans!=JOptionPane.OK_OPTION) {
			          		return;
			          	}
		
	    						final File downloads = downloadDir;
		      				// download, unzip and run installer in separate thread
			  	    		Runnable runner = new Runnable() {
			  	    			public void run() {
		  	    					// show relaunching dialog during download
			  	    				downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
			  	    						+" "+downloads.getPath()+".")); //$NON-NLS-1$ //$NON-NLS-2$
			  	    				relaunchLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.RelaunchLabel.Upgrade.Text"))); //$NON-NLS-1$
			  	    				relaunchingDialog.pack();
			  	    				// center on TFrame
			  	    		    relaunchingDialog.setLocationRelativeTo(frame);
			  	    				relaunchingDialog.setVisible(true);
			  	    				
			  	    				// download zip file
			  	  	    		File zipFile = new File(downloads, zipFileName);
			  	  	    		zipFile = ResourceLoader.download(zipURL, zipFile, false);
			  	  	    		if (zipFile!=null && zipFile.exists()) {
			  	  	    			// use ditto to unzip
			  	  	    			String path = zipFile.getPath();
			  	  	    			ArrayList<String> cmd = new ArrayList<String>();
					    	    		cmd.add("ditto"); //$NON-NLS-1$
					    	    		cmd.add("-x"); //$NON-NLS-1$
					    	    		cmd.add("-k"); //$NON-NLS-1$
					    	    		cmd.add("-rsrcFork"); //$NON-NLS-1$
					    	    		cmd.add(path);
					    	    		cmd.add(downloads.getPath());			  	  	    			
					      				try {
						    	    		ProcessBuilder builder = new ProcessBuilder(cmd);
													Process p = builder.start();
					      					// wait for process to finish, then delete zip file
					      	        p.waitFor();		
					      	        zipFile.delete();
					      	        // upgrade installer should now be unzipped
					      	        
					      	        // run upgrade installer
					  	    				String appName = "TrackerUpgrade-"+Tracker.newerVersion+"-osx-installer.app"; //$NON-NLS-1$ //$NON-NLS-2$
					  	  	    		File installer = new File(downloads, appName);
					  	  	    		if (installer!=null && installer.exists()) {
														// assemble command: pass Tracker.app path as parameter
					  	  	    			String trackerApp = new File(Tracker.trackerHome).getParentFile().getParent();
							    	    		cmd.clear();
							    	    		cmd.add("open"); //$NON-NLS-1$
							    	    		cmd.add(installer.getPath());
							    	    		cmd.add("--tracker-app"); //$NON-NLS-1$
							    	    		cmd.add(trackerApp);
							    	    		
							    	    		// log command
							    	    		String message = ""; //$NON-NLS-1$
							    	    		for (String next: cmd) {
							    	    			message += next + " "; //$NON-NLS-1$
							    	    		}
							    	    		OSPLog.info("executing command: " + message); //$NON-NLS-1$ 

							    	    		builder = new ProcessBuilder(cmd);
														p = builder.start();
														if (isAlive(p)) {
											    		TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
											    		if (trackerPanel!=null) {
											    			Action exit = TActions.getAction("exit", trackerPanel); //$NON-NLS-1$
											    			exit.actionPerformed(null);
											    		}
											    		else {
											    			System.exit(0);
											    		}
														}
								  	    		else {
								      				OSPLog.warning("failed to launch upgrade installer"); //$NON-NLS-1$
									  	    		failed[0] = true;	  	    			
								  	    		}
					  	  	    		}
							  	    		else {
							      				OSPLog.warning("failed to unzip upgrade installer"); //$NON-NLS-1$
								  	    		failed[0] = true;	  	    			
							  	    		}
												} catch (Exception ex) {
						      				OSPLog.warning("exception: "+ex); //$NON-NLS-1$
							  	    		failed[0] = true;	  	    			
												}
			  	  	    		}	  	  	    		
					  	    		else {
					      				OSPLog.warning("failed to download zipped upgrade installer"); //$NON-NLS-1$
						  	    		failed[0] = true;	  	    			
					  	    		} 	  	    			
			  	      			if (failed[0]) {
			  	    					// close relaunching dialog and display Tracker web site
			  	      				relaunchingDialog.setVisible(false);
			  	      				relaunchingDialog.dispose();
			  		  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
			  		  	    		OSPDesktop.displayURL(websiteurl);
			  	    				}
			  	    			}
			  	    		};  // end runnable
			  	    		new Thread(runner).start();
		      			}
	  	    		}
      			}
      			else if (OSPRuntime.isLinux()) {
	    				// see if a TrackerUpgrade file is available
	    				int bitness = OSPRuntime.getVMBitness();
	    				// see if a TrackerUpgrade zip is available
	    				final String upgradeFileName = "TrackerUpgrade-"+Tracker.newerVersion+"-linux-"+bitness+"bit-installer.run"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	  	    		final String fileURL = upgradeURL.trim()+upgradeFileName;
	  	    		responseCode = 0;
	  	    		try {
	  	    	    URL url = new URL(fileURL);
	  	    	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
	  	    	    responseCode = huc.getResponseCode();
		  	    	} catch (Exception ex) {
		  	    	}
	  	    		if (responseCode==200) { // upgrade installer exists
		  	    		// let user specify download directory
		  	    		String home = System.getProperty("user.home"); //$NON-NLS-1$
		  	    		File downloadDir = new File(home+"/Downloads"); //$NON-NLS-1$
		  	    		downloadDir = chooseDownloadDirectory(frame, downloadDir);
	    					if (downloadDir==null) { 
	    						// user cancelled
	    						return;
	    					}
	    					if (!downloadDir.exists()) {
	    						// failed to specify valid download directory
		      				OSPLog.warning("download directory does not exist: "+downloadDir); //$NON-NLS-1$
	    						failed[0] = true;
	    					}
	    					else {
	  	    				// inform user of intended action and ask permission
			          	int ans = JOptionPane.showConfirmDialog(frame, 
			          			TrackerRes.getString("TTrackBar.Dialog.Download.Upgrade.Message1")+" "+downloadDir.getPath()+"." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			          			+XML.NEW_LINE+TrackerRes.getString("TTrackBar.Dialog.Download.Upgrade.Message2")+XML.NEW_LINE,  //$NON-NLS-1$
			          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
			          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			          	if (ans!=JOptionPane.OK_OPTION) {
			          		return;
			          	}
		
	    						final File downloads = downloadDir;
		      				// download and run installer in separate thread
			  	    		Runnable runner = new Runnable() {
			  	    			public void run() {
		  	    					// show relaunching dialog during download
			  	    				downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
			  	    						+" "+downloads.getPath()+".")); //$NON-NLS-1$ //$NON-NLS-2$
			  	    				relaunchLabel.setText(""); //$NON-NLS-1$
			  	    				relaunchingDialog.pack();
			  	    				// center on TFrame
			  	    		    relaunchingDialog.setLocationRelativeTo(frame);
			  	    				relaunchingDialog.setVisible(true);
			  	    				
			  	    				// download upgrade installer
			  	  	    		File installer = new File(downloads, upgradeFileName);
			  	  	    		installer = ResourceLoader.download(fileURL, installer, false);
			  	  	    		if (installer!=null && installer.exists()) {
				  	    				relaunchingDialog.setVisible(false);
				  	    				installer.setExecutable(true, false);
				  	    				
					      	      // create text field to display copy-able command for Terminal
			  	  	    			final JTextField field = new JTextField();
			  	  	    			field.setBackground(Color.white);
			  	  	    			field.setEditable(false);
			  	  	    			field.addMouseListener(new MouseAdapter() {
			  	  	    	    	public void mousePressed(MouseEvent e) {
					  	  	    			field.selectAll();
			  	  	    	    	}
			  	  	    	    });

												// assemble command: pass tracker home as parameter
			  	  	    			String cmd = "sudo "+installer.getPath(); //$NON-NLS-1$
					    	    		cmd += "--tracker-home "+Tracker.trackerHome; //$NON-NLS-1$
			  	  	    			
					    	    		// log command
					    	    		OSPLog.info("executing command: " + cmd); //$NON-NLS-1$ 

					    	    		field.setText(cmd);
			  	  	    			JPanel panel = new JPanel(new BorderLayout());
			  	  	    			JLabel label1 = new JLabel(TrackerRes.getString("TTrackBar.Dialog.LinuxCommand.Message1")); //$NON-NLS-1$
			  	  	    			label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
			  	  	    			JLabel label2 = new JLabel(TrackerRes.getString("TTrackBar.Dialog.LinuxCommand.Message2")); //$NON-NLS-1$
			  	  	    			label2.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 4));
			  	  	    			JLabel label3 = new JLabel(TrackerRes.getString("TTrackBar.Dialog.LinuxCommand.Message3")); //$NON-NLS-1$
			  	  	    			label3.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 4));
			  	  	    			Box box = Box.createVerticalBox();
			  	  	    			box.add(label1);
			  	  	    			box.add(label2);
			  	  	    			box.add(label3);
			  	  	    			panel.add(box, BorderLayout.NORTH);
			  	  	    			panel.add(field, BorderLayout.SOUTH);
			  	  	    			
			  	  	    			JOptionPane.showMessageDialog(frame, 
			  	  	    					panel, TrackerRes.getString("TTrackBar.Dialog.LinuxCommand.Title"),  //$NON-NLS-1$
			  	  	    					JOptionPane.INFORMATION_MESSAGE);
			  	  	    			
			  	  	    			// copy command to the clipboard
//				  	  	    		// following lines don't work on Ubuntu (known issue as of Jan 2018)
//				  	            StringSelection data = new StringSelection(cmd);
//				  	            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//				  	            clipboard.setContents(data, data);

												TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
												if (trackerPanel!=null) {
													Action exit = TActions.getAction("exit", trackerPanel); //$NON-NLS-1$
													exit.actionPerformed(null);
												}
												else {
													System.exit(0);
												}
			  	  	    		}
			  	  	    		else {
					      				OSPLog.warning("failed to download upgrade installer"); //$NON-NLS-1$
			  	  	    			failed[0] = true;
			  	  	    		}
					      			if (failed[0]) {
			  	    					// close relaunching dialog and display Tracker web site
			  	      				relaunchingDialog.setVisible(false);
			  	      				relaunchingDialog.dispose();
						  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
						  	    		OSPDesktop.displayURL(websiteurl);
					    				}
			  	    			}
			  	    		}; // end runnable
			  	    		new Thread(runner).start();
		      			}
	  	    		}
	  	    		else {
	      				OSPLog.warning("no upgrade installer found on server"); //$NON-NLS-1$
	  	    			failed[0] = true;
	  	    		}	  	    		
      			} // end linux action
      			if (failed[0]) {
    					// close relaunching dialog and display Tracker web site
      				relaunchingDialog.setVisible(false);
      				relaunchingDialog.dispose();
	  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
	  	    		OSPDesktop.displayURL(websiteurl);
    				}
  	    	} // end upgrade action
  	    }); // end upgrade menu item
  	    
  	    JMenuItem learnMoreItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Popup.MenuItem.LearnMore")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
  	    popup.add(learnMoreItem);
  	    learnMoreItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  					// go to Tracker change log
  	    		String websiteurl = "https://"+Tracker.trackerWebsite+"/change_log.html"; //$NON-NLS-1$ //$NON-NLS-2$
  	    		OSPDesktop.displayURL(websiteurl);
  	    	}
  	    });
  	    JMenuItem homePageItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Popup.MenuItem.TrackerHomePage")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
  	    popup.add(homePageItem);
  	    homePageItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  					// go to Tracker web site
  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
  	    		OSPDesktop.displayURL(websiteurl);
  	    	}
  	    });
  	    JMenuItem ignoreItem = new JMenuItem(
  	    		TrackerRes.getString("TTrackBar.Popup.MenuItem.Ignore")); //$NON-NLS-1$
  	    popup.add(ignoreItem);
  	    ignoreItem.addActionListener(new ActionListener() {
  	    	public void actionPerformed(ActionEvent e) {
  	    		Tracker.newerVersion = null;
  	    		Tracker.lastMillisChecked = System.currentTimeMillis();
  	    		TFrame frame = (TFrame)newVersionButton.getTopLevelAncestor();
  	    		if (frame!=null && frame.getSelectedTab()>-1) {
  	    			TrackerPanel trackerPanel = frame.getTrackerPanel(frame.getSelectedTab());
  	    			TTrackBar trackbar = trackbars.get(trackerPanel);
  	    			trackbar.refresh();
  	    		}
  	    	}
  	    });
  	    return popup;
    	}
    };
    newVersionButton.setFont(font.deriveFont(Font.PLAIN, font.getSize()-1)); 
    newVersionButton.setForeground(Color.GREEN.darker()); 
    newVersionButton.setBorder(BorderFactory.createCompoundBorder(line, space));
    OSPLog.getOSPLog().addPropertyChangeListener("error", new PropertyChangeListener() { //$NON-NLS-1$
    	public void propertyChange(PropertyChangeEvent e) {
    		int type = Integer.parseInt(e.getNewValue().toString());
    		if (type == OSPLog.OUT_OF_MEMORY_ERROR) {
        	outOfMemory = true;
    		}
    	}
    });
  }

  /**
   * Gets the trackbar for the specified tracker panel.
   *
   * @param panel the tracker panel
   * @return the trackbar
   */
  public static synchronized TTrackBar getTrackbar(TrackerPanel panel) {
  	TTrackBar trackbar = trackbars.get(panel);
    if (trackbar == null) {
    	trackbar = new TTrackBar(panel);
    	trackbars.put(panel, trackbar);
    }
    return trackbar;
  }

  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  public void setFontLevel(int level) {
  	Object[] objectsToSize = new Object[]
  			{trackButton, sizingField};
    FontSizer.setFonts(objectsToSize, level);
		sizingField.setText("1234567"); //$NON-NLS-1$
		numberFieldWidth = sizingField.getPreferredSize().width;
  }
  
  /**
   * TTrackBar constructor.
   *
   * @param panel the tracker panel
   */
  private TTrackBar(TrackerPanel panel) {
    trackerPanel = panel;
    trackerPanel.addPropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("clear", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.addPropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
    createGUI();
    refresh();
    validate();
  }

  @Override
  public void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" recycled by garbage collector"); //$NON-NLS-1$
  }

  /**
   * Gets the popup menu for the specified track.
   *
   * @param track the track
   * @return the popup menu
   */
  protected JPopupMenu getPopup(TTrack track) {
    JMenu trackMenu = track.getMenu(trackerPanel);
  	FontSizer.setFonts(trackMenu, FontSizer.getLevel());
    return trackMenu.getPopupMenu();
  }

  /**
   *  Creates the GUI.
   */
  protected void createGUI() {
    setFloatable(false);
    setBorder(BorderFactory.createEmptyBorder(3, 0, 2, 0));
    // select button
    selectButton = new TButton(smallSelectIcon) {
      protected JPopupMenu getPopup() {
      	return getSelectTrackPopup();
      }   	
    };
//    // mouse listener to reset zoom button to off state
//    addMouseListener(new MouseAdapter() {
//      public void mousePressed(MouseEvent e) {
//      	TToolBar toolbar = TToolBar.getToolbar(trackerPanel);
//    		if (toolbar.zoomButton.getIcon()!=TToolBar.zoomOffIcon)
//    			toolbar.zoomButton.setIcon(TToolBar.zoomOffIcon);    		
//      }
//    });
    trackButton = new TButton() {
    	@Override
      protected JPopupMenu getPopup() {
    		
    		TTrack track = getTrack();
    		// special case: ParticleDataTrack
      	if (track instanceof ParticleDataTrack) { 
      		if (trackButton.context.contains("point")) { //$NON-NLS-1$
        		ParticleDataTrack dt = (ParticleDataTrack)track;
        		JMenu trackMenu = dt.getPointMenu(track.trackerPanel);
  	        FontSizer.setFonts(trackMenu, FontSizer.getLevel());
  	      	return trackMenu.getPopupMenu();
      		}
      		// else return leader's menu
      		ParticleDataTrack dt = ((ParticleDataTrack)track).getLeader();
      		JMenu trackMenu = dt.getMenu(track.trackerPanel);
	        FontSizer.setFonts(trackMenu, FontSizer.getLevel());
	      	return trackMenu.getPopupMenu();
      	}
      	
      	// general case
      	return super.getPopup();
      }

    };
		trackButton.setOpaque(false);
		emptyLabel.setOpaque(false);
		Border space = BorderFactory.createEmptyBorder(1, 4, 1, 4);
		Border line = BorderFactory.createLineBorder(Color.GRAY);
		trackButton.setBorder(BorderFactory.createCompoundBorder(line, space));
    // create horizontal glue for right end of toolbar
    toolbarEnd = Box.createHorizontalGlue();
  }

  /**
   * Refreshes and returns the "select track" popup menu.
   *
   * @return the popup
   */
  protected JPopupMenu getSelectTrackPopup() {
    selectPopup.removeAll();
    // add calibration tools and axes at end
//    final CoordAxes axes = trackerPanel.getAxes();
    final ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	JMenuItem item = (JMenuItem)e.getSource();
      	TTrack track = trackerPanel.getTrack(item.getText());
      	if (track==null) return;
      	if (trackerPanel.calibrationTools.contains(track)
      			|| track==trackerPanel.getAxes()) {
      		track.setVisible(true);
      	}
      	trackerPanel.setSelectedTrack(track);
      }
    };
    boolean hasTracks = false;
    ArrayList<TTrack> userTracks = trackerPanel.getUserTracks();
    for (TTrack track: userTracks) {
    	hasTracks = true;
    	JMenuItem item = new JMenuItem(track.getName("track"), track.getIcon(21, 16, "track")); //$NON-NLS-1$ //$NON-NLS-2$
    	item.addActionListener(listener);
    	selectPopup.add(item);
    }
  	if (hasTracks) {
    	selectPopup.addSeparator();
  	}
    for (TTrack track: trackerPanel.getTracks()) {
    	if (!userTracks.contains(track)) {
        if (track==trackerPanel.getAxes()
        		&& !trackerPanel.isEnabled("button.axes")) //$NON-NLS-1$
        	continue;
        if (trackerPanel.calibrationTools.contains(track)
        		&& track instanceof TapeMeasure) {
        	TapeMeasure tape = (TapeMeasure)track;
        	if (tape.isStickMode() && !trackerPanel.isEnabled("calibration.stick")) //$NON-NLS-1$
        		continue;
        	if (!tape.isStickMode() && !trackerPanel.isEnabled("calibration.tape")) //$NON-NLS-1$
        		continue;
        }
        if (track instanceof Calibration
        		&& !trackerPanel.isEnabled("calibration.points")) //$NON-NLS-1$
        	continue;
        if (track instanceof ParticleDataTrack)
        	continue;
        if (track instanceof OffsetOrigin
        		&& !trackerPanel.isEnabled("calibration.offsetOrigin")) //$NON-NLS-1$
        	continue;
        if (track instanceof PerspectiveTrack)
        	continue;
    		JMenuItem item = new JMenuItem(track.getName(), track.getFootprint().getIcon(21, 16));
      	item.addActionListener(listener);
      	selectPopup.add(item);
    	}
    }
    FontSizer.setFonts(selectPopup, FontSizer.getLevel());
    return selectPopup;
  }
  
  /**
   *  Refreshes the GUI.
   */
  protected void refresh() {
    Tracker.logTime(getClass().getSimpleName()+hashCode()+" refresh"); //$NON-NLS-1$
    Runnable runner = new Runnable() {
    	public void run() {
    		sizingField.setText("1234567"); //$NON-NLS-1$
    		numberFieldWidth = sizingField.getPreferredSize().width;
        selectButton.setToolTipText(TrackerRes.getString("TToolBar.Button.SelectTrack.Tooltip")); //$NON-NLS-1$
    		TTrack track = trackButton.getTrack();
    		if (track!=null) {
          track.removePropertyChangeListener("name", TTrackBar.this); //$NON-NLS-1$
          track.removePropertyChangeListener("color", TTrackBar.this); //$NON-NLS-1$
          track.removePropertyChangeListener("footprint", TTrackBar.this); //$NON-NLS-1$
        	toolbarComponentHeight = trackButton.getPreferredSize().height;
    		}
    		else {
    			CoordAxes axes = trackerPanel.getAxes();
    			if (axes!=null) {
        		trackButton.setTrack(axes);
          	toolbarComponentHeight = trackButton.getPreferredSize().height;
    			}
    		}
        removeAll();
        Dimension dime = new Dimension(toolbarComponentHeight, toolbarComponentHeight);
        selectButton.setPreferredSize(dime);
        selectButton.setMaximumSize(dime);
    		add(selectButton);
  			trackButton.context = "track"; //$NON-NLS-1$
        track = trackerPanel.getSelectedTrack();
        if (track != null && !(track instanceof PerspectiveTrack)) {
        	if (track instanceof ParticleDataTrack) {
        		TPoint p = trackerPanel.getSelectedPoint();
        		if (p!=null) {
	        		Step step = track.getStep(p, trackerPanel);
	        		if (step!=null && step.getTrack()==track) {
	        			trackButton.context = "point"; //$NON-NLS-1$
	        		}
        		}
        	}
        	trackButton.setTrack(track);
          // listen to tracks for property changes that affect icon or name
          track.addPropertyChangeListener("name", TTrackBar.this); //$NON-NLS-1$
          track.addPropertyChangeListener("color", TTrackBar.this); //$NON-NLS-1$
          track.addPropertyChangeListener("footprint", TTrackBar.this); //$NON-NLS-1$
        	add(trackButton);
          ArrayList<Component> list = track.getToolbarTrackComponents(trackerPanel);
          for (Component c: list) {
            if (c instanceof JComponent &&
                !(c instanceof JButton) && 
                !(c instanceof JCheckBox)) {
              JComponent jc = (JComponent)c;
              int w = jc.getPreferredSize().width;
              jc.setMaximumSize(null);
              jc.setPreferredSize(null);
              Dimension dim = jc.getPreferredSize();
              dim.height = toolbarComponentHeight;
              if(jc instanceof NumberField) {
              	dim.width = Math.max(numberFieldWidth, dim.width);
              }
              else if (jc instanceof TextLineLabel) {
              	dim.width = w;                	
              }
              jc.setPreferredSize(dim);
              jc.setMaximumSize(dim);
            }
            add(c);
          }
          // selected point items
          TPoint p = trackerPanel.getSelectedPoint();
          if (p != null) {
            // a point is selected
            list = track.getToolbarPointComponents(trackerPanel, p);
            for (Component c: list) {
              if (c instanceof JComponent 
              		&& !(c instanceof JButton)) {
                JComponent jc = (JComponent)c;
                int w = jc.getPreferredSize().width;
                jc.setMaximumSize(null);
                jc.setPreferredSize(null);
                Dimension dim = jc.getPreferredSize();
                dim.height = toolbarComponentHeight;
                if(jc instanceof NumberField) {
                	dim.width = Math.max(numberFieldWidth, dim.width);
                }
                else if (jc instanceof TextLineLabel) {
                	dim.width = w;                	
                }
                jc.setPreferredSize(dim);
                jc.setMaximumSize(dim);
              }
              add(c);
            }
          }
        }
        add(toolbarEnd);
        if (testButton!=null) {
  		    add(testButton);
        }
        if (Tracker.newerVersion!=null) {
        	String s = TrackerRes.getString("TTrackBar.Button.Version"); //$NON-NLS-1$
        	newVersionButton.setText(s+" "+Tracker.newerVersion); //$NON-NLS-1$
  		    add(newVersionButton);
        }
    		memoryButton.setToolTipText(TrackerRes.getString("TTrackBar.Button.Memory.Tooltip")); //$NON-NLS-1$
//        refreshMemoryButton();
		    add(memoryButton);
        revalidate();
        repaint();
    	}
    };
    if (SwingUtilities.isEventDispatchThread()) runner.run();
    else SwingUtilities.invokeLater(runner); 
  }
  
  /**
   *  Resizes a NumberField.
   */
  protected void resizeField(NumberField field) {
  	// do nothing if the field is not displayed
  	if (getComponentIndex(field)<0) return;
    field.setMaximumSize(null);
    field.setPreferredSize(null);
    Dimension dim = field.getPreferredSize();
    dim.height = toolbarComponentHeight;
    dim.width = Math.max(numberFieldWidth, dim.width);
    field.setMaximumSize(dim);
    field.setPreferredSize(dim);
		revalidate();
  }

  /**
   *  Refreshes the decimal separators of displayed NumberFields.
   */
  protected void refreshDecimalSeparators() {
  	for (Component next: getComponents()) {
  		if (next instanceof NumberField) {
  			NumberField field = (NumberField)next;
  			field.setValue(field.getValue());
  		}
  	}
  }

  /**
   * Responds to the following events: "selectedtrack", "selectedpoint",
   * "track" from tracker panel, "footprint", "color", "name" from tracks.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if (name.equals("selectedtrack")) {  // selected track has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("footprint")  //$NON-NLS-1$
    		|| name.equals("color") //$NON-NLS-1$
    		|| name.equals("name")) { //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("selectedpoint")) {  // selected point has changed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("track")) {  // tracks have been added or removed //$NON-NLS-1$
      refresh();
    }
    else if (name.equals("clear")) {  // tracks have been cleared //$NON-NLS-1$
  		for (Integer n: TTrack.activeTracks.keySet()) {
  			TTrack track = TTrack.activeTracks.get(n);
	  		track.removePropertyChangeListener("name", TTrackBar.this); //$NON-NLS-1$
	      track.removePropertyChangeListener("color", TTrackBar.this); //$NON-NLS-1$
	      track.removePropertyChangeListener("footprint", TTrackBar.this); //$NON-NLS-1$
  		}
  		trackButton.setTrack(null);
      refresh();
    }
  }
  
  /**
   * Cleans up this trackbar
   */
  public void dispose() {
  	trackbars.remove(trackerPanel);
    removeAll();
    trackerPanel.removePropertyChangeListener("track", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("clear", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("selectedtrack", this); //$NON-NLS-1$
    trackerPanel.removePropertyChangeListener("selectedpoint", this); //$NON-NLS-1$
    for (Integer n: TTrack.activeTracks.keySet()) {
    	TTrack track = TTrack.activeTracks.get(n);
      track.removePropertyChangeListener("name", this); //$NON-NLS-1$
      track.removePropertyChangeListener("color", this); //$NON-NLS-1$
      track.removePropertyChangeListener("footprint", this); //$NON-NLS-1$
		}
		trackButton.setTrack(null);
    trackerPanel = null;
  }

  /**
   * Uses a JFileChooser to select a download directory.
   * @param parent a component to own the file chooser
   * @return the chosen file
   */
  public static File chooseDownloadDirectory(Component parent, File likely) {
    JFileChooser chooser = new JFileChooser(likely);
    if (OSPRuntime.isMac())
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    else
    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    javax.swing.filechooser.FileFilter folderFilter = new javax.swing.filechooser.FileFilter() {
      // accept directories only
      public boolean accept(File f) {
      	if (f==null) return false;
        return f.isDirectory();
      }
      public String getDescription() {
        return ToolsRes.getString("LibraryTreePanel.FolderFileFilter.Description"); //$NON-NLS-1$
      } 	     	
    };
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(folderFilter);
    String text = TrackerRes.getString("TTrackBar.Chooser.DownloadDirectory"); //$NON-NLS-1$
    chooser.setDialogTitle(text);
  	FontSizer.setFonts(chooser, FontSizer.getLevel());
	  int result = chooser.showDialog(parent, TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    if (result==JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile();
    }
  	return null;
  }
  /**
   *  Refreshes the memory button.
   */
  protected static void refreshMemoryButton() {
		System.gc();
    java.lang.management.MemoryMXBean memory
				= java.lang.management.ManagementFactory.getMemoryMXBean();
		long cur = memory.getHeapMemoryUsage().getUsed()/(1024*1024);
		long max = memory.getHeapMemoryUsage().getMax()/(1024*1024);
    if (outOfMemory && showOutOfMemoryDialog) {
    	outOfMemory = false;
    	showOutOfMemoryDialog = false;
    	cur = max;
    	JOptionPane.showMessageDialog(memoryButton, 
    			TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message1")+"\n" //$NON-NLS-1$ //$NON-NLS-2$
    			+ TrackerRes.getString("Tracker.Dialog.OutOfMemory.Message2"), //$NON-NLS-1$
    			TrackerRes.getString("Tracker.Dialog.OutOfMemory.Title"), //$NON-NLS-1$
    			JOptionPane.WARNING_MESSAGE);
    }
		String mem = TrackerRes.getString("TTrackBar.Button.Memory")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		String of = TrackerRes.getString("DynamicSystem.Parameter.Of")+" "; //$NON-NLS-1$ //$NON-NLS-2$
		memoryButton.setText(mem+cur+"MB "+of+max+"MB"); //$NON-NLS-1$ //$NON-NLS-2$
		double used = ((double)cur)/max;
		memoryButton.setForeground(used>0.8? Color.red: Color.black);
  }

  private static boolean isAlive(Process process) {
  	try {
  		process.exitValue();
  		return false;
  	} catch (Exception e) {
  		return true;
  	}
  }
}
