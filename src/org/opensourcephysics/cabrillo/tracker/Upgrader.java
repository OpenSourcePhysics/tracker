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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Function;

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
	String upgradeURL, trackerJarName;

	public Upgrader(TFrame tFrame) {
		frame = tFrame;
	}

	/**
	 * Starts the upgrade process
	 */
	public void upgrade() {
		// get upgrade dialog
		getUpgradeDialog();
		// initialize
		boolean[] failed = new boolean[] { false };
		int responseCode = 0; // code 200 = "OK"
		upgradeURL = getUpgradeURL();
		trackerJarName = "tracker-" + Tracker.newerVersion + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
		
		// look for upgrade tracker.jar
		if (upgradeURL != null && Tracker.trackerHome != null) {
			String upgradeFile = upgradeURL + trackerJarName;
			try {
				URL url = new URL(upgradeFile);
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				responseCode = huc.getResponseCode();
			} catch (Exception ex) {
			}
		}
		if (responseCode != 200) {
			// jar file not found
			failed[0] = true;
		} else if (OSPRuntime.isWindows()) {
			upgradeWindows(failed);
		} else if (OSPRuntime.isMac()) { // OSX
			upgradeOSX(failed);
		} else if (OSPRuntime.isLinux()) {
			upgradeLinux(failed);
		}
		if (failed[0]) {
			// close upgrade dialog and display Tracker web site
			closeUpgradeDialog();
			String websiteurl = "https://" + Tracker.trackerWebsite; //$NON-NLS-1$
			OSPDesktop.displayURL(websiteurl);
		}

	}

	private void upgradeWindows(final boolean[] failed) {
		// check for upgrade installer
		final String upgradeInstallerName = "TrackerUpgrade-" + Tracker.newerVersion + "-windows-x64-installer.exe"; //$NON-NLS-1$ //$NON-NLS-2$
		final String upgradeInstallerURL = upgradeURL + upgradeInstallerName;
		int responseCode = 0;
		try {
			URL url = new URL(upgradeInstallerURL );
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			responseCode = huc.getResponseCode();
		} catch (Exception ex) {
		}
		if (responseCode == 200) { // upgrade installer exists
			File downloadDir = OSPRuntime.getDownloadDir();
			// let user choose
			downloadDir = chooseDownloadDirectory(frame, downloadDir);
			if (downloadDir == null) {
				// user cancelled
				return;
			}
			if (!downloadDir.exists()) {
				// failed to specify valid download directory
				OSPLog.warning("download directory does not exist: " + downloadDir); //$NON-NLS-1$
				failed[0] = true;
			} else {
				// download and launch installer in separate thread
				final File downloads = downloadDir;
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						File installer = new File(downloads, upgradeInstallerName);
						// show relaunching dialog during download
						downloadLabel
								.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
										+ " " + installer.getPath() + ".")); //$NON-NLS-1$ //$NON-NLS-2$
						relaunchLabel.setText(
								(TrackerRes.getString("TTrackBar.Dialog.Relaunch.RelaunchLabel.Upgrade.Text"))); //$NON-NLS-1$
						upgradeDialog.pack();
						// center on TFrame
						upgradeDialog.setLocationRelativeTo(frame);
						upgradeDialog.setVisible(true);

						// download upgrade installer
						String attempted = installer.getPath();
						installer = ResourceLoader.download(upgradeInstallerURL, installer, true);
						// close dialog when done downloading
						closeUpgradeDialog();
						if (installer != null && installer.exists()) {
							// get OK to run installer and close Tracker
							int ans = JOptionPane.showConfirmDialog(frame,
									TrackerRes.getString("Upgrader.Dialog.Downloaded.Message1") + " " //$NON-NLS-1$ //$NON-NLS-2$
//											+ installer.getPath() + "." //$NON-NLS-1$
											+ XML.NEW_LINE + TrackerRes.getString("Upgrader.Dialog.Downloaded.Message2") //$NON-NLS-1$
											+ XML.NEW_LINE,
									TrackerRes.getString("TTrackBar.Dialog.Download.Title"), //$NON-NLS-1$
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
							if (ans != JOptionPane.OK_OPTION) {
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
								for (String next : cmd) {
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
//									TrackerPanel trackerPanel = frame.getSelectedPanel();
//									TActions.exitAction(trackerPanel);
									TActions.exitAction(null);
									return;
								}
								// upgrade installer launch failure
								OSPLog.warning("failed to launch upgrade installer"); //$NON-NLS-1$
								failed[0] = true;

							} catch (Exception ex) {
								OSPLog.warning("exception: " + ex); //$NON-NLS-1$
								failed[0] = true;
							}
						} else {
							// failed to download upgrade installer
							OSPLog.warning("failed to download upgrade installer"); //$NON-NLS-1$
							failed[0] = true;
						}
						if (failed[0]) {
							showDownloadFailure(attempted);							
							// close upgrade dialog and display Tracker web site
							closeUpgradeDialog();
							String websiteurl = "https://" + Tracker.trackerWebsite; //$NON-NLS-1$
							OSPDesktop.displayURL(websiteurl);
						}
					}
				}; // end runnable
				new Thread(runner).start();
			}
		} // end handling upgrade installer
		else {
			// no upgrade installer so download tracker.jar if writable
			// inform user of intended action and ask permission
			int ans = JOptionPane.showConfirmDialog(frame,
					TrackerRes.getString("TTrackBar.Dialog.Download.Message1") + " " + Tracker.trackerHome + "." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ XML.NEW_LINE + TrackerRes.getString("TTrackBar.Dialog.Download.Message2") + XML.NEW_LINE, //$NON-NLS-1$
					TrackerRes.getString("TTrackBar.Dialog.Download.Title"), //$NON-NLS-1$
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (ans != JOptionPane.OK_OPTION) {
				// user cancelled
				return;
			}
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					// show relaunching dialog during downloads
					downloadLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Text") //$NON-NLS-1$
							+ " " + Tracker.trackerHome + ".")); //$NON-NLS-1$ //$NON-NLS-2$
					relaunchLabel.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.RelaunchLabel.Text"))); //$NON-NLS-1$
					upgradeDialog.pack();
					// center on TFrame
					upgradeDialog.setLocationRelativeTo(frame);
					upgradeDialog.setVisible(true);

					// download new tracker jar
					File jarFile = new File(Tracker.trackerHome, trackerJarName);
					String attempted = jarFile.getPath();
					String jarURL = upgradeURL + trackerJarName;
					jarFile = ResourceLoader.download(jarURL, jarFile, true);

					// also download new Tracker.exe if available
					String starterName = "Tracker.exe"; //$NON-NLS-1$
					String starterURL = upgradeURL + starterName;
					int responseCode = 0;
					try {
						URL url = new URL(starterURL);
						HttpURLConnection huc = (HttpURLConnection) url.openConnection();
						responseCode = huc.getResponseCode();
					} catch (Exception ex) {
					}
					if (responseCode == 200) {
						// Tracker.exe is available
						File starterTarget = new File(Tracker.trackerHome, starterName);
						ResourceLoader.download(starterURL, starterTarget, true);
					}

					if (jarFile != null && jarFile.exists()) { // new jar successfully downloaded
						// launch new Tracker version
						final String jarPath = jarFile.getAbsolutePath();
						final ArrayList<String> filenames = new ArrayList<String>();
						frame.saveAllTabs(false, new Function<Integer, Void>() {
							// for each approved
							@Override
							public Void apply(Integer panelID) {
								TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
								File datafile = trackerPanel.getDataFile();
								if (datafile == null) {
									String path = trackerPanel.openedFromPath;
									if (path != null) {
										datafile = new File(path);
									}
								}
								if (datafile != null) {
									String fileName = datafile.getAbsolutePath();
									if (!filenames.contains(fileName)) {
										filenames.add(fileName);
									}
								}
								return null;
							}

						}, new Runnable() {
							// whenAllApproved
							@Override
							public void run() {
								String[] args = filenames.isEmpty() ? null : filenames.toArray(new String[0]);
								System.setProperty(TrackerStarter.PREFERRED_TRACKER_JAR, jarPath);
								System.setProperty(TrackerStarter.TRACKER_NEW_VERSION, jarURL);
								TrackerStarter.relaunch(args, false);
								// TrackerStarter exits current VM after relaunching new one
							}

						}, new Runnable() {
							// when canceled
							@Override
							public void run() {
								closeUpgradeDialog();
							}

						});
					} else {
						OSPLog.warning("failed to download new version"); //$NON-NLS-1$
						failed[0] = true;
					}
					if (failed[0]) {
						showDownloadFailure(attempted);							
						// close upgrade dialog and display Tracker web site
						closeUpgradeDialog();
						String websiteurl = "https://" + Tracker.trackerWebsite; //$NON-NLS-1$
						OSPDesktop.displayURL(websiteurl);
					}
				}
			}; // end runnable
			new Thread(runner).start();
		} // end new tracker.jar
	}

	private void upgradeOSX(final boolean[] failed) {
		// see if a TrackerUpgrade dmg is available
		final String dmgFileName = "TrackerUpgrade-" + Tracker.newerVersion + "-osx-installer.dmg"; //$NON-NLS-1$ //$NON-NLS-2$
		final String dmgURL = upgradeURL + dmgFileName;
		int responseCode = 0;
		try {
			URL url = new URL(dmgURL);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			responseCode = huc.getResponseCode();
		} catch (Exception ex) {
		}
		if (responseCode == 200) { // upgrade installer exists
			File downloadDir = OSPRuntime.getDownloadDir();
			// let user choose
			downloadDir = chooseDownloadDirectory(frame, downloadDir);
			if (downloadDir == null) {
				// user cancelled
				return;
			}
			if (!downloadDir.exists()) {
				// OSX chooser is weird--sometimes returns desired folder as filename also
				File parent = downloadDir.getParentFile();
				if (parent != null && parent.getName().equals(downloadDir.getName())) {
					downloadDir = parent;
				}
			}
			if (!downloadDir.exists()) {
				// failed to specify valid download directory
				OSPLog.warning("download directory does not exist: " + downloadDir); //$NON-NLS-1$
				failed[0] = true;
			} else {
				final File downloads = downloadDir;
				// download, mount dmg and run installer in separate thread
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						File dmgFile = new File(downloads, dmgFileName);
						String appName = "TrackerUpgrade-" + Tracker.newerVersion + "-osx-installer.app"; //$NON-NLS-1$ //$NON-NLS-2$
						// show relaunching dialog during download
						downloadLabel
								.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
										+ " " + dmgFile.getPath() + ".")); //$NON-NLS-1$ //$NON-NLS-2$
						relaunchLabel.setText(""); //$NON-NLS-1$
						upgradeDialog.pack();
						// center on TFrame
						upgradeDialog.setLocationRelativeTo(frame);
						upgradeDialog.setVisible(true);

						// download dmg file
						String attempted = dmgFile.getPath();
						dmgFile = ResourceLoader.download(dmgURL, dmgFile, true);
						if (dmgFile != null && dmgFile.exists()) {
							// use hdiutil to mount
							String path = dmgFile.getPath();
							ArrayList<String> cmd = new ArrayList<String>();
							cmd.add("hdiutil"); //$NON-NLS-1$
							cmd.add("attach"); //$NON-NLS-1$
							cmd.add("-noverify"); //$NON-NLS-1$
							cmd.add("-autoopen"); //$NON-NLS-1$
							cmd.add(path);
							try {
								ProcessBuilder builder = new ProcessBuilder(cmd);
								Process p = builder.start();
								// read output of the process
								BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
								StringBuilder blder = new StringBuilder();
								String line = null;
								while ((line = reader.readLine()) != null) {
									blder.append(line);
								}
								String output = blder.toString(); // output is /dev node, tab, mount point
								String[] chunks = output.split("\t"); //$NON-NLS-1$
								// wait for process to finish
								p.waitFor();

								// dmgFile.delete();
								// upgrade installer dmg should now be mounted
								closeUpgradeDialog();

								if (chunks.length > 1) {
									String volume = chunks[chunks.length - 1];
									File installer = new File(volume, appName);

//	  	    				// get OK to run installer
//			          	int ans = JOptionPane.showConfirmDialog(frame, 
//			          			TrackerRes.getString("Upgrader.Dialog.Downloaded.Message1") //$NON-NLS-1$ 
//			          			+XML.NEW_LINE+TrackerRes.getString("Upgrader.Dialog.Downloaded.Message2")+XML.NEW_LINE,  //$NON-NLS-1$
//			          			TrackerRes.getString("TTrackBar.Dialog.Download.Title"),  //$NON-NLS-1$
//			          			JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
//			          	if (ans!=JOptionPane.OK_OPTION) {
//			          		return;
//			          	}

									// run upgrade installer
									cmd = new ArrayList<String>();
									cmd.add("open"); //$NON-NLS-1$
									cmd.add(installer.getCanonicalPath());
									try {
										builder = new ProcessBuilder(cmd);
										p = builder.start();
									} catch (Exception ex) {
										OSPLog.warning("exception: " + ex); //$NON-NLS-1$
										failed[0] = true;
									}
									p.waitFor();

									// exit Tracker
									TrackerPanel trackerPanel = frame.getSelectedPanel();
									TActions.exitAction(trackerPanel);
									return;
								} 
								OSPLog.warning("failed to mount upgrade installer"); //$NON-NLS-1$
							} catch (Exception ex) {
								OSPLog.warning("exception: " + ex); //$NON-NLS-1$
							}
							failed[0] = true;
						} else {
							OSPLog.warning("failed to download upgrade installer"); //$NON-NLS-1$
							failed[0] = true;
						}
						if (failed[0]) {
							showDownloadFailure(attempted);							
							// close upgrade dialog and display Tracker web site
							closeUpgradeDialog();
							String websiteurl = "https://" + Tracker.trackerWebsite; //$NON-NLS-1$
							OSPDesktop.displayURL(websiteurl);
						}

					}
				}; // end runnable
				new Thread(runner).start();
			}
		} else {
			OSPLog.warning("no upgrade installer found on server"); //$NON-NLS-1$
			failed[0] = true;
		}
	}

	private void upgradeLinux(final boolean[] failed) {
		// see if a TrackerUpgrade file is available
		// see if a TrackerUpgrade zip is available
		final String runFileName = "TrackerUpgrade-" + Tracker.newerVersion	+ "-linux-x64-installer.run"; //$NON-NLS-1$
		final String fileURL = upgradeURL + runFileName;
		int responseCode = 0;
		try {
			URL url = new URL(fileURL);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			responseCode = huc.getResponseCode();
		} catch (Exception ex) {
		}
		if (responseCode == 200) { // upgrade installer exists
			File downloadDir = OSPRuntime.getDownloadDir();
			// let user choose
			downloadDir = chooseDownloadDirectory(frame, downloadDir);
			if (downloadDir == null) {
				// user cancelled
				return;
			}
			if (!downloadDir.exists()) {
				// failed to specify valid download directory
				OSPLog.warning("download directory does not exist: " + downloadDir); //$NON-NLS-1$
				failed[0] = true;
			} else {
				final File downloads = downloadDir;
				// download and run installer in separate thread
				Runnable runner = new Runnable() {
					@Override
					public void run() {
						// show relaunching dialog during download
						downloadLabel
								.setText((TrackerRes.getString("TTrackBar.Dialog.Relaunch.DownloadLabel.Upgrade.Text") //$NON-NLS-1$
										+ " " + downloads.getPath() + "/" + runFileName + ".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						// show relaunching dialog during download
						relaunchLabel.setText(""); //$NON-NLS-1$
						FontSizer.setFonts(upgradeDialog, FontSizer.getLevel());
						upgradeDialog.pack();
						// center on TFrame
						upgradeDialog.setLocationRelativeTo(frame);
						upgradeDialog.setVisible(true);

						// download upgrade installer
						File installer = new File(downloads, runFileName);
						String attempted = installer.getPath();
						installer = ResourceLoader.download(fileURL, installer, true);
						closeUpgradeDialog();
						if (installer != null && installer.exists()) {
							installer.setExecutable(true, false);

							// create text field to display copy-able command for Terminal
							final JTextField field = new JTextField(10);
							field.setBackground(Color.white);
							field.setEditable(false);
							field.addMouseListener(new MouseAdapter() {
								@Override
								public void mousePressed(MouseEvent e) {
									field.selectAll();
								}
							});

							// assemble command: pass tracker home as parameter
							String cmd = "sudo " + installer.getPath(); //$NON-NLS-1$
							cmd += " --tracker-home " + Tracker.trackerHome; //$NON-NLS-1$

							// log command
							OSPLog.info("execution command: " + cmd); //$NON-NLS-1$

							field.setText(cmd);
							JPanel panel = new JPanel(new BorderLayout());
							JLabel label1 = new JLabel(TrackerRes.getString("Upgrader.Dialog.Downloaded.Message1")); //$NON-NLS-1$ //$NON-NLS-2$
							label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
							JLabel label2 = new JLabel(
									TrackerRes.getString("Upgrader.Dialog.Downloaded.Linux.Message2")); //$NON-NLS-1$
							label2.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 4));
							JLabel label3 = new JLabel(
									TrackerRes.getString("Upgrader.Dialog.Downloaded.Linux.Message3")); //$NON-NLS-1$
							label3.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 4));
							Box box = Box.createVerticalBox();
							box.add(label1);
							box.add(label2);
							box.add(field);
							box.add(label3);
							panel.add(box, BorderLayout.NORTH);

							// inform user of required action and intent to close Tracker
							int ans = JOptionPane.showConfirmDialog(frame, panel,
									TrackerRes.getString("TTrackBar.Dialog.Download.Title"), //$NON-NLS-1$
									JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
							if (ans != JOptionPane.OK_OPTION) {
								return;
							}

							// copy command to the clipboard
//	  	    		// following lines don't work on Ubuntu (known issue as of Jan 2018)
//	            StringSelection data = new StringSelection(cmd);
//	            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//	            clipboard.setContents(data, data);

							TrackerPanel trackerPanel = frame.getSelectedPanel();
							TActions.exitAction(trackerPanel);
							return;
						}
						OSPLog.warning("failed to download upgrade installer"); //$NON-NLS-1$
						failed[0] = true;

						if (failed[0]) {
							showDownloadFailure(attempted);							
							// close upgrade dialog and display Tracker web site
							closeUpgradeDialog();
							String websiteurl = "https://" + Tracker.trackerWebsite; //$NON-NLS-1$
							OSPDesktop.displayURL(websiteurl);
						}
					}
				}; // end runnable
				new Thread(runner).start();
			}
		} else {
			OSPLog.warning("no upgrade installer found on server"); //$NON-NLS-1$
			failed[0] = true;
		}
	}

	private void closeUpgradeDialog() {
		if (upgradeDialog != null) {
			upgradeDialog.setVisible(false);
			upgradeDialog.dispose();
			upgradeDialog = null;
		}
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
			@Override
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
			@Override
			public boolean accept(File f) {
				if (f == null)
					return false;
				if (f.getName().endsWith(".app")) //$NON-NLS-1$
					return false;
				return f.isDirectory();
			}

			@Override
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
		if (result == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}
	
	/**
	 * Gets the upgrade folder url on the server. Returns major version subfolder.
	 */
	private String getUpgradeURL() {
		int ver = OSPRuntime.getMajorVersion();
		String url = ResourceLoader.getString("https://physlets.org/tracker/upgradeURL.txt"); //$NON-NLS-1$
		return url==null? null: url.trim() + "ver" + ver + "/";
	}

	private JDialog getUpgradeDialog() {
		// create relaunching dialog
		if (upgradeDialog == null) {
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

	/**
	 * Shows a dialog when a download failure occurs
	 * 
	 * @param taret the target that failed
	 */
	private void showDownloadFailure(String target) {
		String message = TrackerRes.getString("Upgrader.Dialog.DownloadFailed.Message")
				+ " " + target;
		JOptionPane.showMessageDialog(frame, message, 
				TrackerRes.getString("Upgrader.Dialog.DownloadFailed.Title"), 
				JOptionPane.ERROR_MESSAGE);
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
