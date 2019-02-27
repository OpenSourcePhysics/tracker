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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.ToolsRes;

/**
 * A class to upgrade Tracker.
 *
 * @author Douglas Brown
 */
public class Upgrader {
	
	TFrame frame;
  JDialog upgradeDialog;
  JLabel downloadLabel, relaunchLabel;
	
	public Upgrader(TFrame tFrame) {
		frame = tFrame;
	}
	
	public void upgrade() {
		// get upgrade dialog
		getUpgradeDialog();
		// initialize 
		boolean[] failed = new boolean[] {false};
		int responseCode = 0; // code 200 = "OK"
		// look for upgrade tracker.jar
 		final String jarFileName = "tracker-"+Tracker.newerVersion+".jar"; //$NON-NLS-1$ //$NON-NLS-2$
		final String upgradeURL = ResourceLoader.getString("http://physlets.org/tracker/upgradeURL.txt"); //$NON-NLS-1$
		if (upgradeURL!=null && Tracker.trackerHome!=null) {
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
			upgradeWindows(failed);
		}
		else if (OSPRuntime.isMac()) { // OSX
			upgradeOSX(failed);
		}
		else if (OSPRuntime.isLinux()) {
			upgradeLinux(failed);
		}
		if (failed[0]) {
			// close upgrade dialog and display Tracker web site
			closeUpgradeDialog();
  		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
  		OSPDesktop.displayURL(websiteurl);
		}

	}
	
	private void upgradeWindows(final boolean[] failed) {
		// check for upgrade installer
 		final String jarFileName = "tracker-"+Tracker.newerVersion+".jar"; //$NON-NLS-1$ //$NON-NLS-2$
		final String upgradeURL = ResourceLoader.getString("http://physlets.org/tracker/upgradeURL.txt"); //$NON-NLS-1$
		final String upgradeInstallerName = "TrackerUpgrade-"+Tracker.newerVersion+"-windows-installer.exe"; //$NON-NLS-1$ //$NON-NLS-2$
		final String upgradeInstallerURL = upgradeURL.trim()+upgradeInstallerName;
		int responseCode = 0;
		try {
	    URL url = new URL(upgradeInstallerURL);
	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
	    responseCode = huc.getResponseCode();
  	} catch (Exception ex) {
  	}
		if (responseCode==200) { // upgrade installer exists
  		File downloadDir = OSPRuntime.getDownloadDir();
  		// let user choose
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
				// download and launch installer in separate thread
    		final File downloads = downloadDir;
    		Runnable runner = new Runnable() {
    			public void run() {
  	    		File installer = new File(downloads, upgradeInstallerName);
  					// show relaunching dialog during download
    				downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
    						+" "+installer.getPath()+".")); //$NON-NLS-1$ //$NON-NLS-2$
    				relaunchLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.RelaunchLabel.Upgrade.Text"))); //$NON-NLS-1$
    				upgradeDialog.pack();
    				// center on TFrame
    		    upgradeDialog.setLocationRelativeTo(frame);
    				upgradeDialog.setVisible(true);
    				
  	    		// download upgrade installer			  	    				
  	    		installer = ResourceLoader.download(upgradeInstallerURL, installer, true);
  	    		// close dialog when done downloading
    				closeUpgradeDialog();
  	    		if (installer!=null && installer.exists()) {
	    				// get OK to run installer and close Tracker
	          	int ans = JOptionPane.showConfirmDialog(frame, 
	          			TrackerRes.getString("Upgrader.Dialog.Downloaded.Message1")+" "+installer.getPath()+"." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	          			+XML.NEW_LINE+TrackerRes.getString("Upgrader.Dialog.Downloaded.Message2")+XML.NEW_LINE,  //$NON-NLS-1$
	          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
	          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
	          	if (ans!=JOptionPane.OK_OPTION) {
	          		return;
	          	}
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
    					// close upgrade dialog and display Tracker web site
      				closeUpgradeDialog();
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
  				upgradeDialog.pack();
  				// center on TFrame
  		    upgradeDialog.setLocationRelativeTo(frame);
  				upgradeDialog.setVisible(true);
  				
    			// download new tracker jar
	    		File jarFile = new File(Tracker.trackerHome, jarFileName);
  	    		String jarURL = upgradeURL.trim()+jarFileName;	  	    		
    			jarFile = ResourceLoader.download(jarURL, jarFile, true);

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
	      				closeUpgradeDialog();
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
  					// close upgrade dialog and display Tracker web site
    				closeUpgradeDialog();
  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
  	    		OSPDesktop.displayURL(websiteurl);
  				}
  			}
  		}; // end runnable
  		new Thread(runner).start();
		} // end new tracker.jar
	}
	
	private void upgradeOSX(final boolean[] failed) {
		// see if a TrackerUpgrade zip is available
		final String upgradeURL = ResourceLoader.getString("http://physlets.org/tracker/upgradeURL.txt"); //$NON-NLS-1$
		final String zipFileName = "TrackerUpgrade-"+Tracker.newerVersion+"-osx-installer.zip"; //$NON-NLS-1$ //$NON-NLS-2$
		final String zipURL = upgradeURL.trim()+zipFileName;
		int responseCode = 0;
		try {
	    URL url = new URL(zipURL);
	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
	    responseCode = huc.getResponseCode();
  	} catch (Exception ex) {
  	}
		if (responseCode==200) { // upgrade installer exists
  		File downloadDir = OSPRuntime.getDownloadDir();
  		// let user choose
  		downloadDir = chooseDownloadDirectory(frame, downloadDir);
			if (downloadDir==null) { 
				// user cancelled
				return;
			}
			if (!downloadDir.exists()) {
				// OSX chooser is weird--sometimes returns desired folder as filename also
				File parent = downloadDir.getParentFile();
				if (parent!=null && parent.getName().equals(downloadDir.getName())) {
					downloadDir = parent;
				}
			}
			if (!downloadDir.exists()) {
				// failed to specify valid download directory
				OSPLog.warning("download directory does not exist: "+downloadDir); //$NON-NLS-1$
				failed[0] = true;
			}
			else {
				final File downloads = downloadDir;
				// download, unzip and run installer in separate thread
    		Runnable runner = new Runnable() {
    			public void run() {
    				String appName = "TrackerUpgrade-"+Tracker.newerVersion+"-osx-installer.app"; //$NON-NLS-1$ //$NON-NLS-2$
  					// show relaunching dialog during download
    				downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
    						+" "+downloads.getPath()+"/"+appName+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    				relaunchLabel.setText(""); //$NON-NLS-1$
    				upgradeDialog.pack();
    				// center on TFrame
    		    upgradeDialog.setLocationRelativeTo(frame);
    				upgradeDialog.setVisible(true);
    				
    				// download zip file
  	    		File zipFile = new File(downloads, zipFileName);
  	    		zipFile = ResourceLoader.download(zipURL, zipFile, true);
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
        				closeUpgradeDialog();
      	        File appFile = new File(downloads, appName);
  	    				// get OK to run installer
		          	int ans = JOptionPane.showConfirmDialog(frame, 
		          			TrackerRes.getString("Upgrader.Dialog.Downloaded.Message1")+" "+appFile.getPath()+"." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		          			+XML.NEW_LINE+TrackerRes.getString("Upgrader.Dialog.Downloaded.Message2")+XML.NEW_LINE,  //$NON-NLS-1$
		          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
		          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
		          	if (ans!=JOptionPane.OK_OPTION) {
		          		return;
		          	}
	
      	        // run upgrade installer
  	  	    		File installer = new File(downloads, appName);
  	  	    		if (installer!=null && installer.exists()) {
		    	    		Desktop.getDesktop().open(new File(installer.getPath()));
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
    					// close upgrade dialog and display Tracker web site
      				closeUpgradeDialog();
	  	    		String websiteurl = "https://"+Tracker.trackerWebsite; //$NON-NLS-1$
	  	    		OSPDesktop.displayURL(websiteurl);
    				}
      			
    			}
    		};  // end runnable
    		new Thread(runner).start();
			}
		}
	}
	
	private void upgradeLinux(final boolean[] failed) {
		final String upgradeURL = ResourceLoader.getString("http://physlets.org/tracker/upgradeURL.txt"); //$NON-NLS-1$
		// see if a TrackerUpgrade file is available
		int bitness = OSPRuntime.getVMBitness();
		// see if a TrackerUpgrade zip is available
		final String upgradeFileName = "TrackerUpgrade-"+Tracker.newerVersion+"-linux-"+bitness+"bit-installer.run"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final String fileURL = upgradeURL.trim()+upgradeFileName;
		int responseCode = 0;
		try {
	    URL url = new URL(fileURL);
	    HttpURLConnection huc = (HttpURLConnection)url.openConnection();
	    responseCode = huc.getResponseCode();
  	} catch (Exception ex) {
  	}
		if (responseCode==200) { // upgrade installer exists
  		File downloadDir = OSPRuntime.getDownloadDir();
  		// let user choose
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
				final File downloads = downloadDir;
				// download and run installer in separate thread
    		Runnable runner = new Runnable() {
    			public void run() {
  					// show relaunching dialog during download
    				downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
    						+" "+downloads.getPath()+"/"+upgradeFileName+".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  					// show relaunching dialog during download
    				relaunchLabel.setText(""); //$NON-NLS-1$
    				FontSizer.setFonts(upgradeDialog, FontSizer.getLevel());
    				upgradeDialog.pack();
    				// center on TFrame
    		    upgradeDialog.setLocationRelativeTo(frame);
    				upgradeDialog.setVisible(true);
    				
    				// download upgrade installer
  	    		File installer = new File(downloads, upgradeFileName);
  	    		installer = ResourceLoader.download(fileURL, installer, true);
    				closeUpgradeDialog();
  	    		if (installer!=null && installer.exists()) {
	    				installer.setExecutable(true, false);
	    				
      	      // create text field to display copy-able command for Terminal
  	    			final JTextField field = new JTextField(10);
  	    			field.setBackground(Color.white);
  	    			field.setEditable(false);
  	    			field.addMouseListener(new MouseAdapter() {
  	    	    	public void mousePressed(MouseEvent e) {
  	  	    			field.selectAll();
  	    	    	}
  	    	    });

							// assemble command: pass tracker home as parameter
  	    			String cmd = "sudo "+installer.getPath(); //$NON-NLS-1$
    	    		cmd += " --tracker-home "+Tracker.trackerHome; //$NON-NLS-1$
  	    			
    	    		// log command
    	    		OSPLog.info("execution command: " + cmd); //$NON-NLS-1$ 

    	    		field.setText(cmd);
  	    			JPanel panel = new JPanel(new BorderLayout());
  	    			JLabel label1 = new JLabel(TrackerRes.getString("Upgrader.Dialog.Downloaded.Message1") //$NON-NLS-1$
  	    					+" "+installer.getPath()+"."); //$NON-NLS-1$ //$NON-NLS-2$
  	    			label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
  	    			JLabel label2 = new JLabel(TrackerRes.getString("Upgrader.Dialog.Downloaded.Linux.Message2")); //$NON-NLS-1$
  	    			label2.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 4));
  	    			JLabel label3 = new JLabel(TrackerRes.getString("Upgrader.Dialog.Downloaded.Linux.Message3")); //$NON-NLS-1$
  	    			label3.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 4));
  	    			Box box = Box.createVerticalBox();
  	    			box.add(label1);
  	    			box.add(label2);
  	    			box.add(field);
  	    			box.add(label3);
  	    			panel.add(box, BorderLayout.NORTH);
  	    			
	    				// inform user of required action and intent to close Tracker
  	    			int ans = JOptionPane.showConfirmDialog(frame, 
	          			panel,
	          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
	          			JOptionPane.OK_CANCEL_OPTION, 
	          			JOptionPane.INFORMATION_MESSAGE);
	          	if (ans!=JOptionPane.OK_OPTION) {
	          		return;
	          	}

  	    			// copy command to the clipboard
//	  	    		// following lines don't work on Ubuntu (known issue as of Jan 2018)
//	            StringSelection data = new StringSelection(cmd);
//	            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//	            clipboard.setContents(data, data);

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
    					// close upgrade dialog and display Tracker web site
      				closeUpgradeDialog();
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
	}
	
	private void closeUpgradeDialog() {
		upgradeDialog.setVisible(false);
		upgradeDialog.dispose();
		upgradeDialog = null;
	}
	
  /**
   * Uses a JFileChooser to select a download directory.
   * 
   * @param parent a component to own the file chooser
   * @param likely the default likely directory
   * @return the chosen directory file
   */
  private File chooseDownloadDirectory(Component parent, File likely) {

  	JFileChooser chooser = new JFileChooser() {
      public void approveSelection() {
          if (getSelectedFile().isFile()) {
              return;
          } else
              super.approveSelection();
      }
		};
    chooser.setDialogTitle(TrackerRes.getString("TTrackBar.Chooser.DownloadDirectory")); //$NON-NLS-1$		
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    javax.swing.filechooser.FileFilter folderFilter = new javax.swing.filechooser.FileFilter() {
      // accept directories only
      public boolean accept(File f) {
      	if (f==null) return false;
      	if (f.getName().endsWith(".app")) return false; //$NON-NLS-1$
        return f.isDirectory();
      }
      public String getDescription() {
        return ToolsRes.getString("LibraryTreePanel.FolderFileFilter.Description"); //$NON-NLS-1$
      } 	     	
    };
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(folderFilter);
    chooser.setCurrentDirectory(new File(likely.getParent()));
    chooser.setSelectedFile(likely);
    if (OSPRuntime.isMac()) {
    	chooser.updateUI();
    }
  	FontSizer.setFonts(chooser, FontSizer.getLevel());
	  int result = chooser.showDialog(parent, TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
    if (result==JFileChooser.APPROVE_OPTION) {
      return chooser.getSelectedFile();
    }
  	return null;
  }
  
	private JDialog getUpgradeDialog() {
		// create relaunching dialog
		if (upgradeDialog==null) {
			upgradeDialog = new JDialog(frame, false);
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createEtchedBorder());
			upgradeDialog.setContentPane(panel);	    				
			upgradeDialog.setTitle(TrackerRes.getString("TTrackBar.Dialog.Relaunch.Title.Text")); //$NON-NLS-1$
			Box box = Box.createVerticalBox();
			upgradeDialog.getContentPane().add(box);
			downloadLabel = new JLabel(); 
			downloadLabel.setBorder(BorderFactory.createEmptyBorder(10, 6, 6, 6));
			box.add(downloadLabel);
			relaunchLabel = new JLabel(); 
			relaunchLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 16, 6));
		}
		return upgradeDialog;
	}
	
  private boolean isAlive(Process process) {
  	try {
  		process.exitValue();
  		return false;
  	} catch (Exception e) {
  		return true;
  	}
  }

}
