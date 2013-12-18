/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2014  Douglas Brown
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
 * <http://www.cabrillo.edu/~dbrown/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.util.*;
import java.util.logging.Level;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.opensourcephysics.cabrillo.tracker.deploy.TrackerStarter;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.IntegerField;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.tools.ExtensionsManager;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This displays and sets preferences for a TrackerPanel.
 *
 * @author Douglas Brown
 */
public class PrefsDialog extends JDialog {

  // static constants
	final static Color MEDIUM_RED = new Color(255, 120, 140);
  
  // static fields
	protected static boolean webStartWarningShown;
  protected static String userHome, javaHome;
  protected static FilenameFilter trackerJarFilter;
  protected static File codeBaseDir;

  // instance fields
  protected TrackerPanel trackerPanel;
  protected TFrame frame;
  protected JButton okButton, cancelButton;
  protected JButton allButton, noneButton, applyButton, saveButton;
  protected JButton relaunchButton, clearRecentButton, checkForUpgradeButton;
  protected JButton clearHostButton, browseCacheButton, clearCacheButton, setCacheButton, setRunButton;
  protected JTextField cacheField, runField;
  protected JPanel checkPanel;
  protected JPanel mainButtonBar;
  protected JTabbedPane tabbedPane;
  protected JPanel configPanel, runtimePanel, videoPanel, generalPanel, 
  		displayPanel;
  protected IntegerField memoryField;
  protected JLabel memoryLabel, recentSizeLabel, lookFeelLabel, cacheLabel, 
  		versionLabel, runLabel;
  protected JCheckBox defaultMemoryCheckbox, hintsCheckbox, vidWarningCheckbox, 
  		xuggleErrorCheckbox, variableDurationCheckBox;
  protected int memorySize = Tracker.requestedMemorySize;
  protected JSpinner recentSizeSpinner, runSpinner;
  protected JComboBox lookFeelDropdown, languageDropdown, jreDropdown, 
  		checkForUpgradeDropdown, versionDropdown, logLevelDropdown;
  protected JRadioButton vm32Button, vm64Button;
  protected JRadioButton xuggleButton, qtButton, noEngineButton;
  protected JRadioButton radiansButton, degreesButton;
  protected JRadioButton xuggleFastButton, xuggleSlowButton;
  protected String[] trackerVersions;
  protected String recent32bitVM, recent64bitVM;
  private boolean refreshing = false;
  
  // previous values
  protected Set<String> prevEnabled = new TreeSet<String>();
  protected int prevMemory, prevRecentCount, prevUpgradeInterval;
  protected String prevLookFeel, prevLocaleName, prevJRE, prevTrackerJar, prevEngine;
  protected boolean prevHints, prevRadians, prevFastXuggle, 
  		prevWarnNoVideoEngine, prevWarnXuggleError, prevClearCacheOnExit, prevUse32BitVM;
  protected File prevCache;
  protected String[] prevExecutables;


	static {
		trackerJarFilter = new org.opensourcephysics.cabrillo.tracker.deploy.TrackerJarFilter();
  	try {
	  	userHome = System.getProperty("user.home"); //$NON-NLS-1$
      javaHome = System.getProperty("java.home"); //$NON-NLS-1$
  		URL url = TrackerStarter.class.getProtectionDomain().getCodeSource().getLocation();
  		File jarFile = new File(url.toURI());
  		codeBaseDir = jarFile.getParentFile();
		}  
		catch (Exception ex) {}
	}
	
  /**
   * Constructs a PrefsDialog.
   *
   * @param panel the tracker panel
   * @param frame the parent TFrame
   */
  public PrefsDialog(TrackerPanel panel, TFrame frame) {
     // non-modal
    super(frame, false);
    trackerPanel = panel;
    this.frame = frame;
    setTitle(TrackerRes.getString("ConfigInspector.Title")); //$NON-NLS-1$
		findTrackerJars();
    createGUI();
  }
  
  @Override
  public void setVisible(boolean vis) {
  	super.setVisible(vis);
  	if (vis) {
  		savePrevious();
  		findTrackerJars();
      refreshGUI();
  	}
  }
  
//_____________________________ private methods ____________________________

  /**
   * Finds the tracker jars.
   */
  private void findTrackerJars() {
  	if (Tracker.trackerHome==null || codeBaseDir==null) {
			trackerVersions = new String[] {"0"}; //$NON-NLS-1$
  		return;
  	}
		String jarHome = OSPRuntime.isMac()? 
				codeBaseDir.getAbsolutePath(): Tracker.trackerHome;
		File dir = new File(jarHome);
		String[] fileNames = dir.list(trackerJarFilter);
		if (fileNames!=null && fileNames.length>0) {
			TreeSet<String> versions = new TreeSet<String>();
			for (int i=0; i<fileNames.length; i++) {
				if ("tracker.jar".equals(fileNames[i].toLowerCase())) {//$NON-NLS-1$
					versions.add("0"); //$NON-NLS-1$
				}
				else {
					versions.add(fileNames[i].substring(8, fileNames[i].length()-4));
				}
			}
			trackerVersions = versions.toArray(new String[versions.size()]);
		}
  }
 
  /**
   * Creates the visible components of this panel.
   */
  private void createGUI() {
  	tabbedPane = new JTabbedPane();
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    contentPane.add(tabbedPane, BorderLayout.CENTER);
    
    // ok button
    okButton = new JButton();
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	applyPrefs();
        setVisible(false);
        // refresh the frame
        if (frame != null) frame.refresh();
      }
    });
    // cancel button
    cancelButton = new JButton();
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	revert();
        setVisible(false);
        // refresh the frame
        if (frame != null) frame.refresh();
      }
    });

    // relaunch button
    relaunchButton = new JButton();
    relaunchButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	applyPrefs();
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
      	TrackerStarter.main(args);
      }
    });
    
    // configuration panel
    configPanel = new JPanel(new BorderLayout());
    tabbedPane.addTab(null, configPanel);
    Color color = Color.WHITE;
    // config checkPanel
    int n = 1+Tracker.getFullConfig().size()/2;
    checkPanel = new JPanel(new GridLayout(n, 2));
    checkPanel.setBackground(color);
    TitledBorder title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("ConfigInspector.Border.Title")); //$NON-NLS-1$
    checkPanel.setBorder(title);
    // config checkboxes
    Iterator<String> it = Tracker.getFullConfig().iterator();
    while (it.hasNext()) {
      String item = it.next();
      JCheckBoxMenuItem checkbox = new JCheckBoxMenuItem(item);
      checkbox.setOpaque(false);
      checkPanel.add(checkbox);
    }
    JScrollPane scroller = new JScrollPane(checkPanel);
    scroller.setPreferredSize(new Dimension(380, 200));
    configPanel.add(scroller, BorderLayout.CENTER);
    // apply button
    applyButton = new JButton();
    applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateConfig();
        refreshGUI();
        frame.refresh();
      }
    });
    // create all and none buttons
    allButton = new JButton();
    allButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Component[] checkboxes = checkPanel.getComponents();
        for (int i = 0; i < checkboxes.length; i++) {
          JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)checkboxes[i];
          checkbox.setSelected(true);
        }
      }
    });
    noneButton = new JButton();
    noneButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Component[] checkboxes = checkPanel.getComponents();
        for (int i = 0; i < checkboxes.length; i++) {
          JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)checkboxes[i];
          checkbox.setSelected(false);
        }
      }
    });
    // save button
    saveButton = new JButton();
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	saveConfigAsDefault();
      }
    });
    JPanel configButtonBar = new JPanel();
    configButtonBar.add(allButton);
    configButtonBar.add(noneButton);
    configButtonBar.add(applyButton);
    configButtonBar.add(saveButton);
    configPanel.add(configButtonBar, BorderLayout.NORTH);
    
    Border etched = BorderFactory.createEtchedBorder();

    // display panel
    displayPanel = new JPanel(new BorderLayout());
    tabbedPane.addTab(null, displayPanel);
    Box box = Box.createVerticalBox();
    displayPanel.add(box, BorderLayout.CENTER);
    
    // look and feel subpanel
    JPanel lfSubPanel = new JPanel();
    box.add(lfSubPanel);
    lfSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.LookFeel.BorderTitle")); //$NON-NLS-1$
    lfSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    lookFeelDropdown = new JComboBox();
  	lookFeelDropdown.addItem(OSPRuntime.DEFAULT_LF.toLowerCase());
    Object selectedItem = OSPRuntime.DEFAULT_LF;
    // get alphabetical list of look/feel types
    Set<String> lfTypes = new TreeSet<String>();
    for (String next: OSPRuntime.LOOK_AND_FEEL_TYPES.keySet()) {
    	if (next.equals(OSPRuntime.DEFAULT_LF)) continue;
    	lfTypes.add(next.toLowerCase());
    	if (next.equals(Tracker.lookAndFeel))
    		selectedItem = next.toLowerCase();
    }
    for (String next: lfTypes) {
    	lookFeelDropdown.addItem(next);
    }
    lookFeelDropdown.setSelectedItem(selectedItem);
    lookFeelDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		String lf = lookFeelDropdown.getSelectedItem().toString().toUpperCase();
    		if (!lf.equals(Tracker.lookAndFeel)) {
    			Tracker.lookAndFeel = lf;
    		}
    	}
    });
    lfSubPanel.add(lookFeelDropdown); 

    // language subpanel
    JPanel langSubPanel = new JPanel();
    box.add(langSubPanel);
    langSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.Language.BorderTitle")); //$NON-NLS-1$
    langSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    languageDropdown = new JComboBox();
    Object selected = TrackerRes.getString("PrefsDialog.Language.Default"); //$NON-NLS-1$
  	languageDropdown.addItem(selected);
    for (Locale next: Tracker.locales) {
    	String s = OSPRuntime.getDisplayLanguage(next);
    	languageDropdown.addItem(s);
    	if (next.equals(Locale.getDefault()) 
    			&& next.toString().equals(Tracker.preferredLocale)) {
    		selected = s;
    	}
    }
    languageDropdown.setSelectedItem(selected);
    languageDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		String s = languageDropdown.getSelectedItem().toString();
    		if (s.equals(TrackerRes.getString("PrefsDialog.Language.Default"))) //$NON-NLS-1$
      		Tracker.setPreferredLocale(null);        
    		else for (Locale next: Tracker.locales) {
        	if (s.equals(OSPRuntime.getDisplayLanguage(next))) {
        		TrackerRes.setLocale(next);
        		Tracker.setPreferredLocale(next.toString());
        		break;
        	}
        }
    	}
    });
    langSubPanel.add(languageDropdown);    

    // hints subpanel
    hintsCheckbox = new JCheckBox();
    hintsCheckbox.setOpaque(false);
    hintsCheckbox.setSelected(Tracker.showHintsByDefault);
    hintsCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Tracker.showHintsByDefault = hintsCheckbox.isSelected();
      }
    });
    JPanel hintsSubPanel = new JPanel();
    box.add(hintsSubPanel);
    hintsSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.Hints.BorderTitle")); //$NON-NLS-1$
    hintsSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    hintsSubPanel.add(hintsCheckbox);
    
    // angle units subpanel
    JPanel unitsSubPanel = new JPanel();
    box.add(unitsSubPanel);
    unitsSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("TMenuBar.Menu.AngleUnits")); //$NON-NLS-1$
    unitsSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));

    ButtonGroup buttonGroup = new ButtonGroup();
    radiansButton = new JRadioButton();
    radiansButton.setOpaque(false);
    radiansButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
    radiansButton.setSelected(Tracker.isRadians);
    buttonGroup.add(radiansButton);
    degreesButton = new JRadioButton();
    degreesButton.setOpaque(false);
    degreesButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
    degreesButton.setSelected(!Tracker.isRadians);
    buttonGroup.add(degreesButton);
    unitsSubPanel.add(radiansButton);
    unitsSubPanel.add(degreesButton);
    
    // runtime panel
    runtimePanel = new JPanel(new BorderLayout());
    tabbedPane.addTab(null, runtimePanel);
    box = Box.createVerticalBox();
    runtimePanel.add(box, BorderLayout.CENTER);
    
    // tracker version subpanel
    JPanel versionSubPanel = new JPanel();
    box.add(versionSubPanel);
    versionSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.Version.BorderTitle")); //$NON-NLS-1$
    versionSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    int preferred = 0;
    versionDropdown = new JComboBox();
    for (int i = 0; i<trackerVersions.length; i++) {
    	String next = trackerVersions[i];
    	if (next.equals("0")) { //$NON-NLS-1$
    		String s = TrackerRes.getString("PrefsDialog.Version.Default"); //$NON-NLS-1$
    		versionDropdown.addItem(s);
    	}
    	else versionDropdown.addItem(next);
    	if (Tracker.preferredTrackerJar!=null
    			&& Tracker.preferredTrackerJar.indexOf("tracker-")>-1 //$NON-NLS-1$
    			&& Tracker.preferredTrackerJar.indexOf(next)>-1) {
    		preferred = i;
    	}
    }
    versionDropdown.setSelectedIndex(preferred);
    versionDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		Object ver = versionDropdown.getSelectedItem();
    		String jar = null;
    		if (ver!=null && !TrackerRes.getString("PrefsDialog.Version.Default").equals(ver)) { //$NON-NLS-1$
    			jar = "tracker-"+ver+".jar"; //$NON-NLS-1$ //$NON-NLS-2$
    		}
    		if (jar==null && Tracker.preferredTrackerJar!=null) {
    			Tracker.preferredTrackerJar = null;
    		}
    		else if (jar!=null && !jar.equals(Tracker.preferredTrackerJar)) {
    			Tracker.preferredTrackerJar = jar;
    		}
    	}
    });
    versionSubPanel.add(versionDropdown); 

    // jre subpanel
    JPanel jreSubPanel = new JPanel(new BorderLayout());
    box.add(jreSubPanel);
    jreSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.JRE.BorderTitle")); //$NON-NLS-1$
    jreSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    
    JPanel jreNorthPanel = new JPanel();
    jreNorthPanel.setBackground(color);
    jreSubPanel.add(jreNorthPanel, BorderLayout.NORTH);
    JPanel jreSouthPanel = new JPanel();
    jreSouthPanel.setBackground(color);
    jreSubPanel.add(jreSouthPanel, BorderLayout.SOUTH);

    int vmBitness = OSPRuntime.getVMBitness();
    vm32Button = new JRadioButton();
    vm32Button.setOpaque(false);
    vm32Button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
    vm32Button.setSelected(vmBitness==32);
    vm32Button.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		if (!vm32Button.isSelected()) return;
	    	if (OSPRuntime.isMac()) {
	    		Tracker.use32BitMode = true;
	    		// must run QT engine in 32-bit VM
	    		if (qtButton.isSelected()) return;
	    		// set engine to QT and inform user
	    		qtButton.setSelected(true);
	      	JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
          		TrackerRes.getString("PrefsDialog.Dialog.SwitchTo32.Message"),    //$NON-NLS-1$
              TrackerRes.getString("PrefsDialog.Dialog.SwitchEngine.Title"),    //$NON-NLS-1$
              JOptionPane.INFORMATION_MESSAGE);
	    	}
	    	else if (OSPRuntime.isWindows()) {	    		
	    		refreshJREDropdown(32);
	    		if (noEngineButton.isSelected()) {
	    			Tracker.engineKnown = false;
	    		}
	    	}
    	}
    });
    jreNorthPanel.add(vm32Button);
    
    vm64Button = new JRadioButton();
    vm64Button.setOpaque(false);
    vm64Button.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
    vm64Button.setSelected(vmBitness==64);
    vm64Button.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		if (!vm64Button.isSelected()) return;

    		double xuggleVersion = VideoIO.guessXuggleVersion();
	    	if (OSPRuntime.isMac()) {
	    		Tracker.use32BitMode = false;
	    		if (xuggleButton.isSelected() || noEngineButton.isSelected()) return;
	    		// if no xuggle engine, show warning
	    		if (xuggleVersion==0) {
		      	int selected = JOptionPane.showConfirmDialog(trackerPanel.getTFrame(),
	          		TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Message1")+"\n"+  //$NON-NLS-1$ //$NON-NLS-2$
	          		TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Message2")+"\n\n"+  //$NON-NLS-1$ //$NON-NLS-2$
	              TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Question"),    //$NON-NLS-1$
	              TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Title"),    //$NON-NLS-1$
	              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
	          if(selected==JOptionPane.YES_OPTION) {
	          	noEngineButton.setSelected(true);
	          }
	          else {
	          	vm32Button.setSelected(true); // revert to 32-bit VM	          	
	          }
	    			return;
	    		}
	    		// set engine to Xuggle and inform user
	    		xuggleButton.setSelected(true);
	      	JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
          		TrackerRes.getString("PrefsDialog.Dialog.SwitchTo64.Message"),    //$NON-NLS-1$
              TrackerRes.getString("PrefsDialog.Dialog.SwitchEngine.Title"),    //$NON-NLS-1$
              JOptionPane.INFORMATION_MESSAGE);
	    	}
	    	else if (OSPRuntime.isWindows()) {	    		
	    		refreshJREDropdown(64);	    			
	    		if (xuggleButton.isSelected() && xuggleVersion==5.4) return;
	    		if (noEngineButton.isSelected()) return;
	    		// if no xuggle 5.4 engine, show warning
	    		if (xuggleVersion==0 || xuggleVersion==3.4) {
	    			// inform that no engine available in 64-bit VM
		      	int selected = JOptionPane.showConfirmDialog(trackerPanel.getTFrame(),
	          		TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Message1")+"\n"+  //$NON-NLS-1$ //$NON-NLS-2$
	          		TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Message2")+"\n\n"+  //$NON-NLS-1$ //$NON-NLS-2$
	              TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Question"),    //$NON-NLS-1$
	              TrackerRes.getString("PrefsDialog.Dialog.NoEngineIn64bitVM.Title"),    //$NON-NLS-1$
	              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null);
	          if (selected==JOptionPane.YES_OPTION) {
	          	noEngineButton.setSelected(true);
	          }
	          else {
	          	vm32Button.setSelected(true); // revert to 32-bit VM	          	
	          }
	    			return;
	    		}
	    		// set engine to Xuggle and inform user
	    		xuggleButton.setSelected(true);
	      	JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
          		TrackerRes.getString("PrefsDialog.Dialog.SwitchToXuggle.Message"),    //$NON-NLS-1$
              TrackerRes.getString("PrefsDialog.Dialog.SwitchToXuggle.Title"),    //$NON-NLS-1$
              JOptionPane.INFORMATION_MESSAGE);
	    	}
    	}
    });
    jreNorthPanel.add(vm64Button);
    
    jreDropdown = new JComboBox();
    jreDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		if (refreshing) return;
    		Object selected = jreDropdown.getSelectedItem();
    		if (selected==null) return;
    		if (vm64Button.isSelected()) {
    			recent64bitVM = selected.toString();
    		}
    		else {
    			recent32bitVM = selected.toString();    			
    		}
    	}
    });
    jreSouthPanel.add(jreDropdown); 
    refreshJREDropdown(vmBitness);

//    jreField = new JTextField(24);
//    jreSouthPanel.add(jreField);
//    final Action setJREAction = new AbstractAction() {
//      public void actionPerformed(ActionEvent e) {
//      	String jre = jreField.getText();
//      	File javaFile = OSPRuntime.getJavaFile(jre);
//      	String path = OSPRuntime.getJREPath(javaFile);
//      	if (path==null) {
//      		if ("".equals(jre)) { //$NON-NLS-1$
//      			Tracker.preferredJRE = null;
//      		}
//      		else {
//      			Toolkit.getDefaultToolkit().beep();
//      		}
//      	}
//      	else if ("".equals(path)) { //$NON-NLS-1$
//    			Tracker.preferredJRE = null;
//      	}
//      	else if (!path.equals(Tracker.preferredJRE)) {
//    			Tracker.preferredJRE = path;
//      	}
//      	refreshTextFields();
//      	updateDisplay();
//      }
//    };
//    jreField.addKeyListener(new KeyAdapter() {
//      public void keyPressed(KeyEvent e) {
//      	jreField.setBackground(Color.yellow);
//      }
//    });
//    jreField.addFocusListener(new FocusAdapter() {
//      public void focusLost(FocusEvent e) {
//      	if (jreField.getBackground()==Color.yellow)
//      		setJREAction.actionPerformed(null);
//      }
//    });
//    jreField.addActionListener(setJREAction);
//    
//    setJREButton = new TButton(openFileIcon);
//    setJREButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        int result = JFileChooser.CANCEL_OPTION;
//        String path = Tracker.preferredJRE;
//        if (path==null || "".equals(path)) //$NON-NLS-1$
//        	path = System.getProperty("java.home"); //$NON-NLS-1$
//      	File jreDir = new File(path);
//      	if (OSPRuntime.isMac()) {
//      		// java home is ".../Contents/Home/" so move up 2 more levels
//      		jreDir = jreDir.getParentFile().getParentFile();
//      	}
//      	if (jreDir.getName().equals("jre") && //$NON-NLS-1$
//      			(jreDir.getParentFile().getName().indexOf("jdk")>-1 //$NON-NLS-1$
//      			|| jreDir.getParentFile().getName().indexOf("sun")>-1)) { //$NON-NLS-1$
//      		jreDir = jreDir.getParentFile();
//      	}
//        JFileChooser chooser = getFileChooser(jreDir.getParentFile(), true);
//        chooser.setDialogTitle(TrackerRes.getString("PrefsDialog.FileChooser.Title.JRE")); //$NON-NLS-1$
//    	  chooser.setSelectedFile(jreDir);
//        result = chooser.showDialog(PrefsDialog.this, 
//    	  		TrackerRes.getString("PrefsDialog.FileChooser.Title.JRE")); //$NON-NLS-1$
//        if(result==JFileChooser.APPROVE_OPTION) {
//          File file = chooser.getSelectedFile();
//      		if (file!=null) {
//      			jreField.setText(file.getPath());
//      			setJREAction.actionPerformed(null);
//      		}
//        }
//      }
//    });
    Border buttonBorder = BorderFactory.createEtchedBorder();
    Border space = BorderFactory.createEmptyBorder(2,2,2,2);
    buttonBorder = BorderFactory.createCompoundBorder(buttonBorder, space);
//    setJREButton.setBorder(buttonBorder);
//    setJREButton.setContentAreaFilled(false);
//    jreSouthPanel.add(setJREButton);
    
    // memory subpanel
    JPanel memorySubPanel = new JPanel();
    box.add(memorySubPanel);
    memorySubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.Memory.BorderTitle")); //$NON-NLS-1$
    memorySubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    memorySubPanel.addMouseListener(new MouseAdapter() {
    	public void mousePressed(MouseEvent e) {
  			requestFocusInWindow();
    	}
    });
    defaultMemoryCheckbox = new JCheckBox();
    defaultMemoryCheckbox.setOpaque(false);
    memoryLabel = new JLabel("MB"); //$NON-NLS-1$
    memoryField = new IntegerField(4);
    memoryField.setMinValue(Tracker.minimumMemorySize);
    memoryField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
    		if (memorySize!=memoryField.getIntValue()) {
        	memorySize = memoryField.getIntValue();
    		}
      }
    });
    memoryField.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (defaultMemoryCheckbox.isSelected()) {
      		defaultMemoryCheckbox.doClick(0);
      	}
      }
    });
    memoryField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	memorySize = memoryField.getIntValue();
      }
    });
    defaultMemoryCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	boolean selected = defaultMemoryCheckbox.isSelected();
      	if (selected) {
        	memoryField.setEnabled(false);
        	memoryLabel.setEnabled(false);
      		memoryField.setText(null);
      	}
      	else {
        	memoryField.setEnabled(true);
        	memoryLabel.setEnabled(true);
      		memoryField.setValue(memorySize);
      		memoryField.requestFocusInWindow();
      		memoryField.selectAll();
      	}
      }
    });
    if (Tracker.preferredMemorySize>-1)
    	memoryField.setValue(Tracker.preferredMemorySize);
    else {
    	defaultMemoryCheckbox.setSelected(true);
    	memoryField.setEnabled(false);
    	memoryLabel.setEnabled(false);
  		memoryField.setText(null);
    }
    memorySubPanel.add(defaultMemoryCheckbox);
    memorySubPanel.add(Box.createRigidArea(new Dimension(40, 1)));
    memorySubPanel.add(memoryField);
    memorySubPanel.add(memoryLabel);
    
    // run subpanel
    JPanel runSubPanel = new JPanel();
    box.add(runSubPanel);
    runSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.Run.BorderTitle")); //$NON-NLS-1$
    runSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    
    final Action setRunAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	String path = runField.getText();
      	int n = (Integer)runSpinner.getValue();
      	ArrayList<String> paths = new ArrayList<String>();
      	if (Tracker.prelaunchExecutables.length>n) { // deal with existing entry
      		if (path.equals(Tracker.prelaunchExecutables[n])) // no change
      			return;
      		if ("".equals(path)) { // eliminate entry //$NON-NLS-1$
      			Tracker.prelaunchExecutables[n] = path;
      			path = null; // done with this
      		}
      		else { // change entry
      			Tracker.prelaunchExecutables[n] = path;
      			path = null; // done with this
      		}
      	}
      	// clean and relist existing entries
    		for (String next: Tracker.prelaunchExecutables) {
    			if (next!=null && !"".equals(next) && !paths.contains(next)) //$NON-NLS-1$
    				paths.add(next);
    		}
    		// add new entry, if any
    		if (path!=null && !"".equals(path) && !paths.contains(path)) //$NON-NLS-1$
    			paths.add(path);
      	Tracker.prelaunchExecutables = paths.toArray(new String[0]);
      	for (int i = 0; i<Tracker.prelaunchExecutables.length; i++) {
      		if (Tracker.prelaunchExecutables[i].equals(path)) {
      			runSpinner.setValue(i);
      			break;
      		}
      	}
      	refreshTextFields();
      }
    };
    SpinnerModel model = new SpinnerNumberModel(0, 0, 6, 1);
    runSpinner = new JSpinner(model);
    JSpinner.NumberEditor editor = new JSpinner.NumberEditor(runSpinner);
    runSpinner.setEditor(editor);
    runSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if(runField.getBackground()==Color.yellow) {
        	setRunAction.actionPerformed(null);
        } else {
          refreshTextFields();
        }
      }
    });
    runSubPanel.add(runSpinner);
    runField = new JTextField(27);
    runSubPanel.add(runField);    
    runField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	runField.setBackground(Color.yellow);
      }
    });
    runField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (runField.getBackground()==Color.yellow)
      		setRunAction.actionPerformed(null);
      }
    });
    runField.addActionListener(setRunAction);
    
    Icon openFileIcon = new ImageIcon(Tracker.class.getResource("resources/images/open.gif")); //$NON-NLS-1$
    setRunButton = new TButton(openFileIcon);
    setRunButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int result = JFileChooser.CANCEL_OPTION;
        JFileChooser chooser = getFileChooser(new File(Tracker.trackerHome), false);
        chooser.setDialogTitle(TrackerRes.getString("PrefsDialog.FileChooser.Title.Run")); //$NON-NLS-1$
        result = chooser.showOpenDialog(PrefsDialog.this);
        if(result==JFileChooser.APPROVE_OPTION) {
          File file = chooser.getSelectedFile();
      		if (file!=null) {
      			runField.setText(file.getPath());
      			setRunAction.actionPerformed(null);
      		}
        }
      }
    });
    setRunButton.setBorder(buttonBorder);
    setRunButton.setContentAreaFilled(false);
    runSubPanel.add(setRunButton);
    
    // video panel
    videoPanel = new JPanel(new BorderLayout());
    tabbedPane.addTab(null, videoPanel);
    box = Box.createVerticalBox();
    videoPanel.add(box, BorderLayout.CENTER);
    
    // videoType subpanel
    JPanel videoTypeSubPanel = new JPanel();
    box.add(videoTypeSubPanel);
    videoTypeSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.VideoPref.BorderTitle")); //$NON-NLS-1$
    videoTypeSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));    
    boolean xuggleInstalled = VideoIO.isEngineInstalled(VideoIO.ENGINE_XUGGLE);

    xuggleButton = new JRadioButton();
    xuggleButton.setOpaque(false);
    xuggleButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
    xuggleButton.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
        xuggleFastButton.setEnabled(xuggleButton.isSelected());
        xuggleSlowButton.setEnabled(xuggleButton.isSelected());
        xuggleErrorCheckbox.setEnabled(xuggleButton.isSelected());
    		if (!xuggleButton.isSelected()) return;
  			Tracker.engineKnown = true;
  			// OSX: if 32-bit, set preferred VM to 64-bit and inform user
    		if (OSPRuntime.isMac() && vm32Button.isSelected()) {
      		vm64Button.setSelected(true);
	      	JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
          		TrackerRes.getString("PrefsDialog.Dialog.SwitchToXuggle64.Message"),    //$NON-NLS-1$
              TrackerRes.getString("PrefsDialog.Dialog.SwitchVM.Title"),    //$NON-NLS-1$
              JOptionPane.INFORMATION_MESSAGE);
    		}
      	// Windows: if xuggle 3.4 and 64-bit, set preferred VM to 32-bit and inform user    		
    		else if (OSPRuntime.isWindows() && VideoIO.guessXuggleVersion()==3.4 && vm64Button.isSelected()) {
      		boolean has32BitVM = ExtensionsManager.getManager().getDefaultJRE(32)!=null;
      		if (has32BitVM) {
	      		vm32Button.setSelected(true);
		      	JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
	          		TrackerRes.getString("PrefsDialog.Dialog.SwitchToXuggle32.Message"),    //$NON-NLS-1$
	              TrackerRes.getString("PrefsDialog.Dialog.SwitchVM.Title"),    //$NON-NLS-1$
	              JOptionPane.INFORMATION_MESSAGE);
      		}
      		else { // help user download 32-bit VM
      			Object[] options = new Object[] {
      					TrackerRes.getString("PrefsDialog.Button.ShowHelpNow"),    //$NON-NLS-1$
	              TrackerRes.getString("Dialog.Button.OK")}; //$NON-NLS-1$
      			int response = JOptionPane.showOptionDialog(trackerPanel.getTFrame(),
	          		TrackerRes.getString("PrefsDialog.Dialog.No32bitVMXuggle.Message")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
	          		TrackerRes.getString("PrefsDialog.Dialog.No32bitVM.Message"), //$NON-NLS-1$
	              TrackerRes.getString("PrefsDialog.Dialog.No32bitVM.Title"), //$NON-NLS-1$
	              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
      			noEngineButton.setSelected(true);
      			if (response==0) {
      				trackerPanel.getTFrame().showHelp("install", 0); //$NON-NLS-1$
      			}
      		}
      	}
    	}
    });
    xuggleButton.setEnabled(xuggleInstalled);
    
    qtButton = new JRadioButton();
    qtButton.setOpaque(false);
    qtButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
    qtButton.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		if (!qtButton.isSelected()) return;
    		if (vm32Button.isSelected()) return;
  			Tracker.engineKnown = true;
      	// if 64-bit, set preferred VM to 32-bit and inform user
    		boolean has32BitVM = OSPRuntime.isMac() || ExtensionsManager.getManager().getDefaultJRE(32)!=null;
    		if (has32BitVM) {
      		vm32Button.setSelected(true); // triggers selection of default or recent 32-bit VM
	      	JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
          		TrackerRes.getString("PrefsDialog.Dialog.SwitchToQT.Message"),    //$NON-NLS-1$
              TrackerRes.getString("PrefsDialog.Dialog.SwitchVM.Title"),    //$NON-NLS-1$
              JOptionPane.INFORMATION_MESSAGE);
    		}
    		else { // help user download 32-bit VM
    			Object[] options = new Object[] {
    					TrackerRes.getString("PrefsDialog.Button.ShowHelpNow"),    //$NON-NLS-1$
              TrackerRes.getString("Dialog.Button.OK")}; //$NON-NLS-1$
    			int response = JOptionPane.showOptionDialog(trackerPanel.getTFrame(),
          		TrackerRes.getString("PrefsDialog.Dialog.No32bitVMQT.Message")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
          		TrackerRes.getString("PrefsDialog.Dialog.No32bitVM.Message"), //$NON-NLS-1$
              TrackerRes.getString("PrefsDialog.Dialog.No32bitVM.Title"), //$NON-NLS-1$
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
    			noEngineButton.setSelected(true);
    			if (response==0) {
    				trackerPanel.getTFrame().showHelp("install", 0); //$NON-NLS-1$
    			}
    		}
    	}
    });
    qtButton.setEnabled(VideoIO.isEngineInstalled(VideoIO.ENGINE_QUICKTIME));
    
    noEngineButton= new JRadioButton();
    noEngineButton.setOpaque(false);
    noEngineButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
    noEngineButton.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		if (!noEngineButton.isSelected()) return;
    		VideoIO.setEngine(VideoIO.ENGINE_NONE);
  			Tracker.engineKnown = true;
    	}
    });

    videoTypeSubPanel.add(xuggleButton);
    videoTypeSubPanel.add(qtButton);
    videoTypeSubPanel.add(noEngineButton);
    
    // xuggle speed subpanel
    JPanel xuggleSpeedSubPanel = new JPanel();
    box.add(xuggleSpeedSubPanel);
    xuggleSpeedSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.Xuggle.Speed.BorderTitle")); //$NON-NLS-1$
    if (!xuggleInstalled)
    	title.setTitleColor(new Color(153, 153, 153));
    xuggleSpeedSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));    
    buttonGroup = new ButtonGroup();
    xuggleFastButton = new JRadioButton();
    xuggleFastButton.setOpaque(false);
    xuggleFastButton.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 10));
    xuggleFastButton.setSelected(xuggleInstalled && Tracker.isXuggleFast);
    buttonGroup.add(xuggleFastButton);
    xuggleSlowButton = new JRadioButton();
    xuggleSlowButton.setOpaque(false);
    xuggleSlowButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
    xuggleSlowButton.setSelected(xuggleInstalled && !Tracker.isXuggleFast);
    buttonGroup.add(xuggleSlowButton);
    xuggleSpeedSubPanel.add(xuggleFastButton);
    xuggleSpeedSubPanel.add(xuggleSlowButton);
        
    // warnings subpanel
    vidWarningCheckbox = new JCheckBox();
    vidWarningCheckbox.setOpaque(false);
    vidWarningCheckbox.setSelected(Tracker.warnNoVideoEngine);
    vidWarningCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Tracker.warnNoVideoEngine = vidWarningCheckbox.isSelected();
      }
    });
    xuggleErrorCheckbox = new JCheckBox();
    xuggleErrorCheckbox.setOpaque(false);
    xuggleErrorCheckbox.setSelected(Tracker.warnXuggleError);
    xuggleErrorCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Tracker.warnXuggleError = xuggleErrorCheckbox.isSelected();
      }
    });
    variableDurationCheckBox = new JCheckBox();
    variableDurationCheckBox.setOpaque(false);
    variableDurationCheckBox.setSelected(Tracker.warnVariableDuration);
    variableDurationCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Tracker.warnVariableDuration = variableDurationCheckBox.isSelected();
      }
    });
    JPanel warningsSubPanel = new JPanel(new BorderLayout());
    box.add(warningsSubPanel);
    warningsSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.NoVideoWarning.BorderTitle")); //$NON-NLS-1$
    warningsSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    JPanel warningsNorthPanel = new JPanel();
    warningsNorthPanel.setBackground(color);
    warningsSubPanel.add(warningsNorthPanel, BorderLayout.NORTH);
    JPanel warningsCenterPanel = new JPanel();
    warningsCenterPanel.setBackground(color);
    warningsSubPanel.add(warningsCenterPanel, BorderLayout.CENTER);

    warningsNorthPanel.add(vidWarningCheckbox);
    warningsNorthPanel.add(xuggleErrorCheckbox);
    warningsCenterPanel.add(variableDurationCheckBox);
    
    // set selected states of engine buttons AFTER creating the xugglefast, xuggleslow and warnxuggle buttons
    if (VideoIO.getEngine().equals(VideoIO.ENGINE_QUICKTIME)
    		&& VideoIO.getVideoType("QT", null)!=null) { //$NON-NLS-1$
	    qtButton.setSelected(true);
    }
    else if (VideoIO.getEngine().equals(VideoIO.ENGINE_XUGGLE)
    		&& VideoIO.getVideoType("Xuggle", null)!=null) { //$NON-NLS-1$
	    xuggleButton.setSelected(true);
    }
    else noEngineButton.setSelected(true);


    
    // "general" panel
    generalPanel = new JPanel(new BorderLayout());
    tabbedPane.addTab(null, generalPanel);
    box = Box.createVerticalBox();
    generalPanel.add(box, BorderLayout.CENTER);
    
    // recent menu subpanel
    JPanel recentSubPanel = new JPanel();
    box.add(recentSubPanel);
    recentSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.RecentFiles.BorderTitle")); //$NON-NLS-1$
    recentSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    // create clear recent button
    clearRecentButton = new JButton();
    clearRecentButton.setEnabled(!Tracker.recentFiles.isEmpty());
    clearRecentButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Tracker.recentFiles.clear();
      	if (trackerPanel!=null) TMenuBar.getMenuBar(trackerPanel).refresh();
        clearRecentButton.setEnabled(false);
      }
    });
    recentSubPanel.add(clearRecentButton);

    // create recent size spinner
    JPanel spinnerPanel = new JPanel();
    spinnerPanel.setOpaque(false);
    spinnerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    n = Tracker.recentFilesSize;
    model = new SpinnerNumberModel(n, 0, 12, 1);
    recentSizeSpinner = new JSpinner(model);
    editor = new JSpinner.NumberEditor(recentSizeSpinner, "0"); //$NON-NLS-1$
    editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    recentSizeSpinner.setEditor(editor);
    spinnerPanel.add(recentSizeSpinner);
    recentSizeLabel = new JLabel();
    spinnerPanel.add(recentSizeLabel);
    recentSubPanel.add(spinnerPanel);
    
    // cache subpanel
    JPanel cacheSubPanel = new JPanel(new BorderLayout());
    box.add(cacheSubPanel);
    cacheSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.CacheFiles.BorderTitle")); //$NON-NLS-1$
    cacheSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    
    // cacheNorthPanel: label, field and browse cache button
    JPanel cacheNorthPanel = new JPanel();
    cacheNorthPanel.setBackground(color);
    cacheLabel = new JLabel();
    cacheNorthPanel.add(cacheLabel);
    cacheField = new JTextField(27);
    cacheNorthPanel.add(cacheField);
    final Action setCacheAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
      	String cachePath = XML.stripExtension(cacheField.getText());
    		Tracker.setCache(cachePath);      
  			refreshTextFields();
      }
    };
    cacheField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	cacheField.setBackground(Color.yellow);
      }
    });
    cacheField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	if (cacheField.getBackground()==Color.yellow)
      		setCacheAction.actionPerformed(null);
      }
    });
    cacheField.addActionListener(setCacheAction);
    
    browseCacheButton = new TButton(openFileIcon);
    browseCacheButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    		File cache = ResourceLoader.getOSPCache();
      	Desktop desktop = Desktop.getDesktop();
      	try {
    			desktop.open(cache);
    		} catch (IOException ex) {
    		}
      }
    });
    browseCacheButton.setBorder(buttonBorder);
    browseCacheButton.setContentAreaFilled(false);
		cacheNorthPanel.add(browseCacheButton);
    cacheSubPanel.add(cacheNorthPanel, BorderLayout.NORTH);
    
    // cacheSouthPanel: clear cache button and set cache button
    JPanel cacheSouthPanel = new JPanel();
    cacheSouthPanel.setBackground(color);
    clearHostButton = new JButton();
    clearHostButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	// get list of host directories 
    		File cache = ResourceLoader.getOSPCache();
    		if (cache==null) return;
      	final File[] hosts = cache.listFiles(ResourceLoader.OSP_CACHE_FILTER);
      	
      	// make popup menu with items to clear individual hosts
        JPopupMenu popup = new JPopupMenu();
        ActionListener clearAction = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		for (File host: hosts) {
        			if (host.getAbsolutePath().equals(e.getActionCommand())) {
        				ResourceLoader.clearOSPCacheHost(host);
            		refreshTextFields();
            		return;
        			}
        		}
        	}
        };
        for (File next: hosts) {
        	String host = next.getName().substring(4).replace('_', '.');
          long bytes = getFileSize(next);
        	long size = bytes/(1024*1024);
        	if (bytes>0) {
        		if (size>0) host += " ("+size+" MB)"; //$NON-NLS-1$ //$NON-NLS-2$
        		else host += " ("+bytes/1024+" kB)"; //$NON-NLS-1$ //$NON-NLS-2$
        	}
          JMenuItem item = new JMenuItem(host);
          item.setActionCommand(next.getAbsolutePath());
          popup.add(item);
          item.addActionListener(clearAction);
        }
        popup.show(clearHostButton, 0, clearHostButton.getHeight());       		
      }
    });
    cacheSouthPanel.add(clearHostButton);
    
    clearCacheButton = new JButton();
    clearCacheButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    		File cache = ResourceLoader.getOSPCache();
				ResourceLoader.clearOSPCache(cache, false);
    		refreshTextFields();
      }
    });
		cacheSouthPanel.add(clearCacheButton);
		
    setCacheButton = new JButton();
    setCacheButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	File newCache = ResourceLoader.chooseOSPCache(trackerPanel.getTFrame());
    		if (newCache!=null) {
    			cacheField.setText(newCache.getPath());
    			setCacheAction.actionPerformed(null);
    		}
      }
    });
		cacheSouthPanel.add(setCacheButton);


    cacheSubPanel.add(cacheSouthPanel, BorderLayout.SOUTH);
    
    // check for upgrades subpanel
    checkForUpgradeButton = new JButton();
    checkForUpgradeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Tracker.loadCurrentVersion(true);
    		Tracker.lastMillisChecked = System.currentTimeMillis();
  			if (trackerPanel!=null) TTrackBar.getTrackbar(trackerPanel).refresh();
    		String message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.None.Message"); //$NON-NLS-1$
    		if (Tracker.newerVersion!=null) { // new version available
    			message = TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message1") //$NON-NLS-1$
    					+" "+Tracker.newerVersion+" " //$NON-NLS-1$ //$NON-NLS-2$
    					+TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Message2") //$NON-NLS-1$
    					+XML.NEW_LINE+Tracker.trackerWebsite;
    		}
  			JOptionPane.showMessageDialog(PrefsDialog.this, 
  					message, 
  					TrackerRes.getString("PrefsDialog.Dialog.NewVersion.Title"),  //$NON-NLS-1$
  					JOptionPane.INFORMATION_MESSAGE);
      }
    });
    logLevelDropdown = new JComboBox();
    String defaultLevel = TrackerRes.getString("PrefsDialog.Version.Default").toUpperCase(); //$NON-NLS-1$
    selected = defaultLevel;
    logLevelDropdown.addItem(defaultLevel);
    for (int i=OSPLog.levels.length-1; i>=0; i--) {
    	String s = OSPLog.levels[i].toString();
    	logLevelDropdown.addItem(s);
    	if (OSPLog.levels[i].equals(Tracker.preferredLogLevel) 
    			&& !Tracker.preferredLogLevel.equals(Tracker.DEFAULT_LOG_LEVEL)) {
    		selected = s;
    	}   	
    }
    logLevelDropdown.setSelectedItem(selected);
    logLevelDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		String s = logLevelDropdown.getSelectedItem().toString();
        Level level = OSPLog.parseLevel(s);
        if (level==null) level = Tracker.DEFAULT_LOG_LEVEL;
        Tracker.preferredLogLevel = level;
    	}
    });
    JPanel logLevelSubPanel = new JPanel();
    box.add(logLevelSubPanel);
    logLevelSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.LogLevel.BorderTitle")); //$NON-NLS-1$
    logLevelSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    logLevelSubPanel.add(logLevelDropdown);
    
    checkForUpgradeDropdown = new JComboBox();
    selected = null;
    for (String next: Tracker.checkForUpgradeChoices) {
    	String s = TrackerRes.getString(next);
    	checkForUpgradeDropdown.addItem(s);
    	if (Tracker.checkForUpgradeIntervals.get(next).equals(
    			Tracker.checkForUpgradeInterval)) {
    		selected = s;
    	}
    }
    checkForUpgradeDropdown.setSelectedItem(selected);
    checkForUpgradeDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		String s = checkForUpgradeDropdown.getSelectedItem().toString();
        for (String next: Tracker.checkForUpgradeChoices) {
        	if (s.equals(TrackerRes.getString(next))) {
        		Tracker.checkForUpgradeInterval = Tracker.checkForUpgradeIntervals.get(next);
        		break;
        	}
        }
    	}
    });
    JPanel dropdownPanel = new JPanel();
    dropdownPanel.setOpaque(false);
    dropdownPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    JPanel upgradeSubPanel = new JPanel();
    box.add(upgradeSubPanel);
    upgradeSubPanel.setBackground(color);
    title = BorderFactory.createTitledBorder(
    		TrackerRes.getString("PrefsDialog.Upgrades.BorderTitle")); //$NON-NLS-1$
    upgradeSubPanel.setBorder(BorderFactory.createCompoundBorder(etched, title));
    upgradeSubPanel.add(checkForUpgradeButton);
    dropdownPanel.add(checkForUpgradeDropdown);
    upgradeSubPanel.add(dropdownPanel);
    
    // main button bar
    mainButtonBar = new JPanel();
    mainButtonBar.add(relaunchButton);
    mainButtonBar.add(okButton);
    mainButtonBar.add(cancelButton);
    contentPane.add(mainButtonBar, BorderLayout.SOUTH);
    
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	if (tabbedPane.getSelectedComponent()==runtimePanel) {
      		defaultMemoryCheckbox.setEnabled(!OSPRuntime.isWebStart());
          if (OSPRuntime.isWebStart() && !webStartWarningShown) {
          	webStartWarningShown = true;
          	JOptionPane.showMessageDialog(PrefsDialog.this, 
          			TrackerRes.getString("PrefsDialog.Dialog.WebStart.Message"),  //$NON-NLS-1$
          			TrackerRes.getString("PrefsDialog.Dialog.WebStart.Title"),  //$NON-NLS-1$
          			JOptionPane.INFORMATION_MESSAGE);
          }
      	}
      }
    });
    
    // add VM buttons to buttongroups
    buttonGroup = new ButtonGroup();
    buttonGroup.add(vm32Button);
    buttonGroup.add(vm64Button);
    
    // add engine buttons to buttongroups
    buttonGroup = new ButtonGroup();
    buttonGroup.add(xuggleButton);
    buttonGroup.add(qtButton);
    buttonGroup.add(noEngineButton);
    
    // enable/disable buttons
    xuggleFastButton.setEnabled(xuggleButton.isSelected());
    xuggleSlowButton.setEnabled(xuggleButton.isSelected());
    xuggleErrorCheckbox.setEnabled(xuggleButton.isSelected());
    if (OSPRuntime.isWindows()) {
	    vm32Button.setEnabled(!ExtensionsManager.getManager().getPublicJREs(32).isEmpty());
	    vm64Button.setEnabled(!ExtensionsManager.getManager().getPublicJREs(64).isEmpty());
    }
    else if (OSPRuntime.isLinux()) {
    	int bitness = OSPRuntime.getVMBitness();
	    vm32Button.setEnabled(bitness==32);
	    vm64Button.setEnabled(bitness==64);    	
    }
    refreshGUI();
  }
  
  private void savePrevious() {
		prevEnabled.clear();
		if (trackerPanel!=null) prevEnabled.addAll(trackerPanel.getEnabled()); 
		prevMemory = Tracker.preferredMemorySize;
		prevLookFeel = Tracker.lookAndFeel;
		prevRecentCount = Tracker.recentFilesSize;
		prevLocaleName = Tracker.preferredLocale;
		prevHints = Tracker.showHintsByDefault;
		prevRadians = Tracker.isRadians;
		prevFastXuggle = Tracker.isXuggleFast;
		prevJRE = Tracker.preferredJRE;
		prevTrackerJar = Tracker.preferredTrackerJar;
		prevExecutables = Tracker.prelaunchExecutables;
		prevWarnNoVideoEngine = Tracker.warnNoVideoEngine;
		prevWarnXuggleError = Tracker.warnXuggleError;
		prevCache = ResourceLoader.getOSPCache();
		prevUpgradeInterval = Tracker.checkForUpgradeInterval;
		prevUse32BitVM = Tracker.use32BitMode;
		prevEngine = VideoIO.getEngine();
  }
  
  private void revert() {
  	if (trackerPanel!=null) trackerPanel.setEnabled(prevEnabled);
  	Tracker.preferredMemorySize = prevMemory;
		Tracker.lookAndFeel = prevLookFeel;
		Tracker.recentFilesSize = prevRecentCount;
		Tracker.setPreferredLocale(prevLocaleName);
		Tracker.showHintsByDefault = prevHints;
		Tracker.isRadians = prevRadians;
		Tracker.isXuggleFast = prevFastXuggle;
		Tracker.preferredJRE = prevJRE;
		Tracker.preferredTrackerJar = prevTrackerJar;
		Tracker.prelaunchExecutables = prevExecutables;
		Tracker.warnNoVideoEngine = prevWarnNoVideoEngine;
		Tracker.warnXuggleError = prevWarnXuggleError;
		ResourceLoader.setOSPCache(prevCache);
		Tracker.checkForUpgradeInterval = prevUpgradeInterval;
		Tracker.use32BitMode = prevUse32BitVM;
		// reset JRE dropdown to initial state
    int vmBitness = OSPRuntime.getVMBitness();
    if (vmBitness==32) {
    	recent32bitVM = null;
    	vm32Button.setSelected(true);
    }
    else {
    	recent64bitVM = null;
    	vm64Button.setSelected(true);
    }
  }
  
  /**
   * Updates the configuration to reflect the current checkbox states.
   */
  private void updateConfig() {
  	if (trackerPanel==null) return;
  	// get the checkboxes
    Component[] checkboxes = checkPanel.getComponents();
    for (int i = 0; i < checkboxes.length; i++) {
      JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)checkboxes[i];
      if (checkbox.isSelected())
        trackerPanel.getEnabled().add(checkbox.getText());
      else
        trackerPanel.getEnabled().remove(checkbox.getText());
    }
  }
  
  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    cancelButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    saveButton.setText(TrackerRes.getString("ConfigInspector.Button.SaveAsDefault")); //$NON-NLS-1$
    okButton.setText(TrackerRes.getString("PrefsDialog.Button.Save")); //$NON-NLS-1$
    applyButton.setText(TrackerRes.getString("Dialog.Button.Apply")); //$NON-NLS-1$
    applyButton.setEnabled(trackerPanel!=null);
    allButton.setText(TrackerRes.getString("Dialog.Button.All")); //$NON-NLS-1$
    noneButton.setText(TrackerRes.getString("Dialog.Button.None")); //$NON-NLS-1$
    relaunchButton.setText(TrackerRes.getString("PrefsDialog.Button.Relaunch")); //$NON-NLS-1$
    clearRecentButton.setText(TrackerRes.getString("PrefsDialog.Button.ClearRecent")); //$NON-NLS-1$
    cacheLabel.setText(TrackerRes.getString("PrefsDialog.Label.Path")+":"); //$NON-NLS-1$ //$NON-NLS-2$
    clearCacheButton.setToolTipText(TrackerRes.getString("PrefsDialog.Button.ClearCache.Tooltip")); //$NON-NLS-1$
    clearHostButton.setText(TrackerRes.getString("PrefsDialog.Button.ClearHost")); //$NON-NLS-1$
    clearHostButton.setToolTipText(TrackerRes.getString("PrefsDialog.Button.ClearHost.Tooltip")); //$NON-NLS-1$
    setCacheButton.setText(TrackerRes.getString("PrefsDialog.Button.SetCache")); //$NON-NLS-1$
    checkForUpgradeButton.setText(TrackerRes.getString("PrefsDialog.Button.CheckForUpgrade")); //$NON-NLS-1$
    recentSizeLabel.setText(TrackerRes.getString("PrefsDialog.Label.RecentSize")); //$NON-NLS-1$
    defaultMemoryCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.DefaultSize")); //$NON-NLS-1$
    hintsCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.HintsOn")); //$NON-NLS-1$    
    vm32Button.setText(TrackerRes.getString("PrefsDialog.Checkbox.32BitVM")); //$NON-NLS-1$
    vm64Button.setText(TrackerRes.getString("PrefsDialog.Checkbox.64BitVM")); //$NON-NLS-1$
    xuggleButton.setText(TrackerRes.getString("PrefsDialog.Button.Xuggle")); //$NON-NLS-1$
    qtButton.setText(TrackerRes.getString("PrefsDialog.Button.QT")); //$NON-NLS-1$
  	noEngineButton.setText(TrackerRes.getString("PrefsDialog.Button.NoEngine")); //$NON-NLS-1$
    radiansButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Radians")); //$NON-NLS-1$
    degreesButton.setText(TrackerRes.getString("TMenuBar.MenuItem.Degrees")); //$NON-NLS-1$
    xuggleFastButton.setText(TrackerRes.getString("PrefsDialog.Xuggle.Fast")); //$NON-NLS-1$
    xuggleSlowButton.setText(TrackerRes.getString("PrefsDialog.Xuggle.Slow")); //$NON-NLS-1$
    vidWarningCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.WarnIfNoEngine")); //$NON-NLS-1$    
    variableDurationCheckBox.setText(TrackerRes.getString("PrefsDialog.Checkbox.WarnVariableDuration")); //$NON-NLS-1$    
    xuggleErrorCheckbox.setText(TrackerRes.getString("PrefsDialog.Checkbox.WarnIfXuggleError")); //$NON-NLS-1$    
    setTabTitle(configPanel, TrackerRes.getString("PrefsDialog.Tab.Configuration.Title")); //$NON-NLS-1$
    setTabTitle(runtimePanel, TrackerRes.getString("PrefsDialog.Tab.Runtime.Title")); //$NON-NLS-1$
    setTabTitle(videoPanel, TrackerRes.getString("PrefsDialog.Tab.Video.Title")); //$NON-NLS-1$
    setTabTitle(displayPanel, TrackerRes.getString("PrefsDialog.Tab.Display.Title")); //$NON-NLS-1$
    setTabTitle(generalPanel, TrackerRes.getString("PrefsDialog.Tab.General.Title")); //$NON-NLS-1$
    refreshTextFields();
    pack();
    updateDisplay();
  }
  
  private void refreshJREDropdown(int vmBitness) {
  	refreshing = true; // suppresses dropdown actions
    
    // replace items in dropdown
    jreDropdown.removeAllItems();
    ExtensionsManager manager = ExtensionsManager.getManager();
    Set<String> availableJREs = manager.getAllJREs(vmBitness);
    for (String next: availableJREs) {
    	jreDropdown.addItem(next);
    }
    
    // set selected item
    String selectedItem = null;
  	if (vmBitness==32 && recent32bitVM!=null) {
  		selectedItem = recent32bitVM;
  	}
  	else if (vmBitness==64 && recent64bitVM!=null) {
  		selectedItem = recent64bitVM;
  	}
    if (selectedItem==null) {
    	selectedItem = Tracker.preferredJRE;
      if (selectedItem==null || !availableJREs.contains(selectedItem)) {
      	selectedItem = vmBitness==32? Tracker.preferredJRE32: Tracker.preferredJRE64;
        if (selectedItem==null || !availableJREs.contains(selectedItem)) {
        	selectedItem = manager.getDefaultJRE(vmBitness);
        }
    	}
    }
    jreDropdown.setSelectedItem(selectedItem);
    
    // save selected item for future refreshing
		if (vmBitness==32) {
			recent32bitVM = selectedItem;
		}
		else {
			recent64bitVM = selectedItem;
		}
    refreshing = false;
  }
  
  private void refreshTextFields() {
  	// run field
  	int n = (Integer)runSpinner.getValue();
  	if (Tracker.prelaunchExecutables.length>n
  			&& Tracker.prelaunchExecutables[n]!=null) {
	    runField.setText(Tracker.prelaunchExecutables[n]);
	    runField.setToolTipText(Tracker.prelaunchExecutables[n]);
	    runField.setBackground(new File(Tracker.prelaunchExecutables[n]).exists()? Color.white: MEDIUM_RED);
  	}
  	else {
  		runField.setText(null);
  		runField.setToolTipText(null);
  		runField.setBackground(Color.white);  		
  	}
  	
  	// cache field and button
    String s = TrackerRes.getString("PrefsDialog.Button.ClearCache"); //$NON-NLS-1$
    File cache = ResourceLoader.getOSPCache();
    if (cache!=null) {
      cacheField.setText(cache.getPath());
      cacheField.setToolTipText(cache.getAbsolutePath());
      cacheField.setBackground(cache.canWrite()? Color.white: MEDIUM_RED);
      long bytes = getFileSize(cache);
    	long size = bytes/(1024*1024);
    	if (bytes>0) {
    		if (size>0) s += " ("+size+" MB)"; //$NON-NLS-1$ //$NON-NLS-2$
    		else s += " ("+bytes/1024+" kB)"; //$NON-NLS-1$ //$NON-NLS-2$
    	}
    }
    else {
      cacheField.setText(""); //$NON-NLS-1$
      cacheField.setToolTipText(""); //$NON-NLS-1$
      cacheField.setBackground(MEDIUM_RED);
    }
    clearCacheButton.setText(s);
    boolean isEmpty = cache==null || !cache.exists() 
				|| cache.listFiles(ResourceLoader.OSP_CACHE_FILTER).length==0;
		clearCacheButton.setEnabled(!isEmpty);
		clearHostButton.setEnabled(!isEmpty);
  }

  /**
   * Applies and saves the current preferences.
   */
  private void applyPrefs() {
    // look/feel, language, video, hints are set directly by components
  	// update configuration
    updateConfig();
    // update recent menu
    Integer val = (Integer)recentSizeSpinner.getValue();
    Tracker.setRecentSize(val);
    if (trackerPanel!=null) TMenuBar.getMenuBar(trackerPanel).refresh();
  	// update preferred memory size
    if (defaultMemoryCheckbox.isSelected())
  		Tracker.preferredMemorySize = -1;
    else
    	Tracker.preferredMemorySize = memoryField.getIntValue();
    // update preferred JRE
		Object selected = jreDropdown.getSelectedItem();
		if (selected !=null && !selected.equals(Tracker.preferredJRE)) {
			Tracker.preferredJRE = selected.toString();
			if (ExtensionsManager.getManager().is32BitVM(Tracker.preferredJRE)) {
				Tracker.preferredJRE32 = Tracker.preferredJRE;				
			}
			else Tracker.preferredJRE64 = Tracker.preferredJRE;
		}
		// video engine
		if (xuggleButton.isSelected() && xuggleButton.isEnabled()) {
			VideoIO.setEngine(VideoIO.ENGINE_XUGGLE);
		}
		else if (qtButton.isSelected() && qtButton.isEnabled()) {
			VideoIO.setEngine(VideoIO.ENGINE_QUICKTIME);
		}
		else VideoIO.setEngine(VideoIO.ENGINE_NONE);
		
    Tracker.isRadians = radiansButton.isSelected();
		Tracker.isXuggleFast = xuggleFastButton.isSelected();
		if (frame!=null) frame.setAnglesInRadians(Tracker.isRadians);
    // save the tracker and tracker_starter preferences
    String path = Tracker.savePreferences();
		if (path!=null)
			OSPLog.info("saved tracker preferences in "+XML.getAbsolutePath(new File(path))); //$NON-NLS-1$		
  }
  
  /**
   * Saves the current configuration as the default.
   */
  private void saveConfigAsDefault() {
    Component[] checkboxes = checkPanel.getComponents();
    Set<String> enabled = new TreeSet<String>();
    for (int i = 0; i < checkboxes.length; i++) {
      JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)checkboxes[i];
      if (checkbox.isSelected())
      	enabled.add(checkbox.getText());
    }
//    applyButton.doClick(0);
    Tracker.setDefaultConfig(enabled);
  }
  
  /**
   * Updates this dialog to show the TrackerPanel's current preferences.
   */
  protected void updateDisplay() {
  	// configuration
    Component[] checkboxes = checkPanel.getComponents();
    for (int i = 0; i < checkboxes.length; i++) {
      // check the checkbox if its text is in the current config
      JCheckBoxMenuItem checkbox = (JCheckBoxMenuItem)checkboxes[i];
      Set<String> enabled = trackerPanel!=null? trackerPanel.getEnabled(): Tracker.getDefaultConfig(); 
      checkbox.setSelected(enabled.contains(checkbox.getText()));
    }
    // memory size
  	defaultMemoryCheckbox.setSelected(Tracker.preferredMemorySize<0);
  	memoryField.setEnabled(Tracker.preferredMemorySize>=0);
  	memoryLabel.setEnabled(Tracker.preferredMemorySize>=0);
    if (Tracker.preferredMemorySize>=0)
    	memoryField.setValue(Tracker.preferredMemorySize);
    else {
  		memoryField.setText(null);
    }
    // look and feel
    if (Tracker.lookAndFeel!=null)
    	lookFeelDropdown.setSelectedItem(Tracker.lookAndFeel.toLowerCase());
    // recent files
    recentSizeSpinner.setValue(Tracker.recentFilesSize);
    // hints
    hintsCheckbox.setSelected(Tracker.showHintsByDefault);
    // warnings
    vidWarningCheckbox.setSelected(Tracker.warnNoVideoEngine);
    variableDurationCheckBox.setSelected(Tracker.warnVariableDuration);
    xuggleErrorCheckbox.setSelected(Tracker.warnXuggleError);
    // locale
    for (Locale next: Tracker.locales) {
    	if (next.equals(Locale.getDefault())) {
    		languageDropdown.setSelectedItem(OSPRuntime.getDisplayLanguage(next));
    		break;
    	}
    }
    
    // video
    if (VideoIO.getEngine().equals(VideoIO.ENGINE_QUICKTIME)) {
	    qtButton.setSelected(true);
    }
    else if (VideoIO.getEngine().equals(VideoIO.ENGINE_XUGGLE)) {
	    xuggleButton.setSelected(true);
    }
    repaint();
  }
  
  /**
   * Sets the title of a specified tab.
   */
  private void setTabTitle(JPanel tab, String title) {
  	for (int i = 0; i<tabbedPane.getTabCount(); i++) {
  		if (tabbedPane.getComponentAt(i)==tab)
  			tabbedPane.setTitleAt(i, title);
  	}
  }
  
  /**
   * Gets the total size of a folder.
   * 
   * @param folder the folder
   * @return the size in bytes
   */
  private long getFileSize(File folder) {
    if (folder==null) return 0;
    long foldersize = 0;
    File cache = ResourceLoader.getOSPCache();
    File[] files = folder.equals(cache)? 
    		folder.listFiles(ResourceLoader.OSP_CACHE_FILTER):
    		folder.listFiles();
    if (files==null) return 0;
    for (int i = 0; i < files.length; i++) {
	    if (files[i].isDirectory()) {
	    	foldersize += getFileSize(files[i]);
	    } else {
	    	foldersize += files[i].length();
	    }
    }
    return foldersize;
  }

  /**
   * Gets a file chooser.
   * 
   * @param file the initial file to select
   * @param useJREFilter true if setting JRE
   * @return the file chooser
   */
  protected static JFileChooser getFileChooser(File file, boolean useJREFilter) {
		JFileChooser chooser = new JFileChooser(file);
		if (useJREFilter) {
	    FileFilter folderFilter = new FileFilter() {
	      // accept directories or "jre/jdk" files only
	      public boolean accept(File f) {
	      	if (f==null) return false;
	        if (f.isDirectory()) return true;
	        if (f.getPath().indexOf("jre")>-1) return true; //$NON-NLS-1$
	        if (f.getPath().indexOf("jdk")>-1) return true; //$NON-NLS-1$
	        return false;
	      }
	      public String getDescription() {
	        return TrackerRes.getString("PrefsDialog.FileFilter.JRE"); //$NON-NLS-1$
	      } 	     	
	    };
	    if (OSPRuntime.isMac())
	      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    else
	    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    chooser.addChoosableFileFilter(folderFilter);
		}
    return chooser;
  }

}
