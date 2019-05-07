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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.ImageVideo;
import org.opensourcephysics.media.core.ImageVideoType;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.JarTool;
import org.opensourcephysics.tools.LaunchBuilder;
import org.opensourcephysics.tools.LibraryResource;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * A dialog for exporting Tracker ZIP files. Steps are:
 * 1. create temp folder in target directory which will contain all files to be zipped
 * 2. write or copy the video clip to a video subfolder
 * 3. write or copy HTML pages, stylesheets and image files into html and image subfolders
 * 4. write the converted Tracker data file in the temp folder
 * 5. zip the temp folder
 * 6. delete temp folder
 *
 * @author Douglas Brown
 */
public class ExportZipDialog extends JDialog {
	
	protected static final String DEFAULT_VIDEO_EXTENSION = "jpg"; //$NON-NLS-1$
	
	protected static ExportZipDialog zipExporter; // singleton
  protected static String videoSubdirectory = "videos"; //$NON-NLS-1$
  protected static String htmlSubdirectory = "html"; //$NON-NLS-1$
  protected static String imageSubdirectory = "images"; //$NON-NLS-1$
  protected static Color labelColor = new Color(0, 0, 102);
  protected static String preferredExtension = DEFAULT_VIDEO_EXTENSION; 

  // instance fields
	protected ExportVideoDialog videoExporter;
  protected TrackerPanel trackerPanel;
  protected TFrame frame;
  protected AddedFilesDialog addedFilesDialog;
  protected Icon openIcon;
  protected JPanel videoPanel, tabsPanel, thumbnailPanel, centerPanel, imagePanel;
  protected Box metadataBox;
  protected TitledBorder videoBorder, thumbnailBorder, metadataBorder, tabsBorder;
  protected JButton saveButton, closeButton, addFilesButton, thumbnailButton, loadHTMLButton, helpButton;
  protected JComboBox formatDropdown;
  protected ArrayList<EntryField> tabTitleFields = new ArrayList<EntryField>();
  protected ArrayList<JCheckBox> tabCheckboxes = new ArrayList<JCheckBox>();
  protected JLabel formatLabel, authorLabel, contactLabel, descriptionLabel, keywordsLabel;
  protected JLabel thumbnailDisplay, urlLabel, titleLabel, htmlLabel;
  protected JCheckBox clipCheckbox, showThumbnailCheckbox;
  protected ArrayList<JLabel> labels = new ArrayList<JLabel>();
  protected EntryField authorField, contactField, keywordsField, urlField, titleField, htmlField;
  protected String targetName, targetDirectory, targetVideo, targetExtension;
  protected JTextArea filelistPane, descriptionPane;  
  protected VideoListener videoExportListener;
  protected XMLControl control;
	protected boolean addThumbnail=true, isWaitingForVideo=false;
	protected ArrayList<ParticleModel> badModels; // particle models with start frames not included in clip
	protected String videoIOPreferredExtension;
  
  /**
   * Returns the singleton ZipResourceDialog ready to create a zip resource for a TrackerPanel.
   * 
   * @param panel the TrackerPanel
   * @return the ZipResourceDialog
   */
  public static ExportZipDialog getDialog(TrackerPanel panel) {
  	boolean reset = false;
  	if (zipExporter==null) {
  		zipExporter = new ExportZipDialog(panel);
  		zipExporter.setFontLevel(FontSizer.getLevel());
  		reset = true;
  	}
  	reset = reset || zipExporter.trackerPanel!=panel;
  	if (reset) {
	  	zipExporter.trackerPanel = panel;
	  	zipExporter.control = new XMLControlElement(panel);
	  	zipExporter.videoExporter = ExportVideoDialog.getDialog(panel);
	  	zipExporter.addThumbnail = true;
	  	zipExporter.addedFilesDialog.addedFiles.clear();
	  	zipExporter.htmlField.setText(zipExporter.htmlField.getDefaultText());
	  	zipExporter.htmlField.setForeground(zipExporter.htmlField.getEmptyForeground());
	  	zipExporter.htmlField.setBackground(Color.white);
			zipExporter.clipCheckbox.setSelected(panel.getVideo()!=null);			
  		if (panel.openedFromPath!=null) {
  			File htmlFile = new File(panel.openedFromPath);
  			if (TrackerIO.trzFileFilter.accept(htmlFile)) {
  				String baseName = XML.stripExtension(XML.getName(panel.openedFromPath));
  				String htmlPath = panel.openedFromPath+"!/html/"+baseName+"_info.html"; //$NON-NLS-1$ //$NON-NLS-2$
  				htmlFile = new File(htmlPath);
  				zipExporter.refreshFieldsFromHTML(htmlFile);
  			}
  		}
	  	zipExporter.titleField.requestFocusInWindow();
	  }
  	zipExporter.refreshFormatDropdown();
  	zipExporter.refreshThumbnail();
  	zipExporter.refreshGUI();
  	
  	return zipExporter;
  }
  
  /**
   * Private constructor.
   *
   * @param panel a TrackerPanel
   */
  private ExportZipDialog(TrackerPanel panel) {
    super(panel.getTFrame(), false);
    trackerPanel = panel;
    frame = panel.getTFrame();
    videoExporter = ExportVideoDialog.getDialog(panel);
    createGUI();
    refreshGUI();
    // center dialog on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - getBounds().width) / 2;
    int y = (dim.height - getBounds().height) / 2;
    setLocation(x, y);
  }

  /**
   * Sets the font level.
   *
   * @param level the desired font level
   */
  public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		// refresh the dropdowns
		int n = formatDropdown.getSelectedIndex();
		Object[] items = new Object[formatDropdown.getItemCount()];
		for (int i=0; i<items.length; i++) {
			items[i] = formatDropdown.getItemAt(i);
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel(items);
		formatDropdown.setModel(model);
		formatDropdown.setSelectedItem(n);
    // reset label sizes
    FontRenderContext frc = new FontRenderContext(null, false, false); 
    Font font = titleLabel.getFont();
    int w = 0;
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
      w = Math.max(w, (int) rect.getWidth()+1);
    }
    int h = titleField.getMinimumSize().height;
    Dimension labelSize = new Dimension(w, h);
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      next.setPreferredSize(labelSize);
    }
		pack();
		
    // set font level of addedFilesDialog
		FontSizer.setFonts(addedFilesDialog, level);
		addedFilesDialog.pack();
  }
  
  //_____________________________ private methods ____________________________

  /**
   * Creates the visible components of this dialog.
   */
  private void createGUI() {
    String path = "/org/opensourcephysics/cabrillo/tracker/resources/images/open.gif"; //$NON-NLS-1$
    openIcon = new ResizableIcon(new ImageIcon(this.getClass().getResource(path)));
    Color color = UIManager.getColor("Label.disabledForeground"); //$NON-NLS-1$
    if (color!=null) UIManager.put("ComboBox.disabledForeground", color); //$NON-NLS-1$
    videoExportListener = new VideoListener();
    
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    
    // video panel
    videoPanel = new JPanel();
	  clipCheckbox = new JCheckBox();
	  clipCheckbox.setSelected(true);
    clipCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	refreshGUI();
      }
    });
    formatLabel = new JLabel(); 
    formatLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
    formatDropdown = new JComboBox(videoExporter.getFormats());

    
    videoPanel.add(clipCheckbox);
    videoPanel.add(formatLabel);
    videoPanel.add(formatDropdown);
    
    // tabs panel 
    tabsPanel = new JPanel();
                 	      
    // button bar   
    JPanel buttonbar = new JPanel();
    helpButton = new JButton();
    helpButton.setForeground(new Color(0, 0, 102));
    helpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        frame.showHelp("zip", 0); //$NON-NLS-1$
      }
    });
    addFilesButton = new JButton();
    addFilesButton.setForeground(labelColor);
    addFilesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	// show added files dialog
      	addedFilesDialog.refreshGUI();
      	addedFilesDialog.refreshFileList();
      	addedFilesDialog.setVisible(true);
      }
    });
    saveButton = new JButton();
    saveButton.setForeground(labelColor);
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String description = descriptionPane.getText().trim();
        if (!"".equals(description) && "".equals(trackerPanel.getDescription())) { //$NON-NLS-1$ //$NON-NLS-2$
        	trackerPanel.setDescription(description);
        	trackerPanel.hideDescriptionWhenLoaded = true;
        }
      	// if saving clip, warn if there are particle models with start frames not included in clip
      	if (clipCheckbox.isSelected()) {
	      	badModels = getModelsNotInClips();
	      	if (!badModels.isEmpty()) {
	    	    // show names of bad models and offer to exclude them from export
	    	    String names = ""; //$NON-NLS-1$
	    	    for (ParticleModel next: badModels) {
	    	    	if (!"".equals(names)) { //$NON-NLS-1$
	    	    		names += ", "; //$NON-NLS-1$
	    	    	}
	    	    	names += "'"+next.getName()+"'"; //$NON-NLS-1$ //$NON-NLS-2$
	    	    }
	    	  	int response = javax.swing.JOptionPane.showConfirmDialog(
	    	  			frame,	    			
	    	  			TrackerRes.getString("ZipResourceDialog.BadModels.Message1") //$NON-NLS-1$ 
	    	  			+"\n"+TrackerRes.getString("ZipResourceDialog.BadModels.Message2") //$NON-NLS-1$ //$NON-NLS-2$ 
	    	  			+"\n"+TrackerRes.getString("ZipResourceDialog.BadModels.Message3") //$NON-NLS-1$ //$NON-NLS-2$ 
	    	  			+"\n\n"+names //$NON-NLS-1$ 
	    	  			+"\n\n"+TrackerRes.getString("ZipResourceDialog.BadModels.Question"), //$NON-NLS-1$ //$NON-NLS-2$ 
	    	  			TrackerRes.getString("ZipResourceDialog.BadModels.Title"), //$NON-NLS-1$ 
	    	  			javax.swing.JOptionPane.YES_NO_CANCEL_OPTION, 
	    	  			javax.swing.JOptionPane.WARNING_MESSAGE);
	    	  	if (response!=javax.swing.JOptionPane.YES_OPTION) {
	    	  		return;
	    	  	}
	      	}
      	}
      	
      	// define the target filename and create empty zip list
      	final ArrayList<File> zipList = defineTarget();
      	if (zipList==null) return;
      	setVisible(false);      	     	

      	// use separate thread to add files to the ziplist and create the TRZ file
      	Runnable runner = new Runnable() {
      		public void run() {
		      	String thumbPath = addThumbnail(zipList);      	
		      	addHTMLInfo(thumbPath, zipList);      	
          	addVideosAndTRKs(zipList);
          	addFiles(zipList);
          	saveZip(zipList);
      		}
      	};
      	new Thread(runner).start();      	
      }     
    });
    
    closeButton = new JButton();
    closeButton.setForeground(labelColor);
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });
    buttonbar.add(helpButton);
    buttonbar.add(addFilesButton);
    buttonbar.add(saveButton);
    buttonbar.add(closeButton);
    
    Border toolbarBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);
    
    // metadata
    metadataBox = Box.createVerticalBox();
    
    // HTML file
    htmlLabel = new JLabel();
    htmlField = new EntryField(30) {
  		protected String getDefaultText() {
  			return TrackerRes.getString("ZipResourceDialog.HTMLField.DefaultText"); //$NON-NLS-1$
  		}
    };
    htmlField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    htmlField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	refreshGUI();     	
      }
    });
    loadHTMLButton = new JButton(openIcon);
    loadHTMLButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 2));
    loadHTMLButton.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    loadHTMLButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = TrackerIO.getChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle(TrackerRes.getString("ZipResourceDialog.FileChooser.OpenHTML.Title"));  //$NON-NLS-1$
        chooser.setFileFilter(LaunchBuilder.getHTMLFilter());
      	File[] files = TrackerIO.getChooserFiles("open any"); //$NON-NLS-1$
        chooser.removeChoosableFileFilter(LaunchBuilder.getHTMLFilter());
      	if (files==null) return; // cancelled by user
        htmlField.setText(XML.getRelativePath(files[0].getPath()));    
        refreshFieldsFromHTML(files[0]);
	      refreshGUI();
      }

    });
    JToolBar htmlbar = new JToolBar();
    htmlbar.setBorder(toolbarBorder);
    htmlbar.setFloatable(false);
    htmlbar.setOpaque(false);
    htmlbar.add(htmlLabel);
    htmlbar.add(htmlField);
    htmlbar.add(loadHTMLButton);    

    // title
    titleLabel = new JLabel();
    titleField = new EntryField(30);
    titleField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    JToolBar titlebar = new JToolBar();
    titlebar.setBorder(toolbarBorder);
    titlebar.setFloatable(false);
    titlebar.setOpaque(false);
    titlebar.add(titleLabel);
    titlebar.add(titleField);
    // description
    descriptionLabel = new JLabel();
    descriptionPane = new JTextArea();
    descriptionPane.setLineWrap(true);
    descriptionPane.setWrapStyleWord(true);
    descriptionPane.getDocument().putProperty("parent", descriptionPane); //$NON-NLS-1$
    descriptionPane.getDocument().addDocumentListener(EntryField.documentListener);
    descriptionPane.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
      	descriptionPane.setBackground(Color.white);
      }
    });
    JToolBar descriptionbar = new JToolBar();
    descriptionbar.setBorder(toolbarBorder);
    descriptionbar.setFloatable(false);
    descriptionbar.setOpaque(false);
    descriptionbar.add(descriptionLabel);
    JScrollPane scroller = new JScrollPane(descriptionPane) {
    	public Dimension getPreferredSize() {
    		int w = titleField.getPreferredSize().width;
    		return new Dimension(w, 60);
    	}
    };
    scroller.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    descriptionbar.add(scroller);
    // author
    authorLabel = new JLabel();
    authorField = new EntryField(30);
    authorField.setText(trackerPanel.author);
		authorField.setBackground(Color.white);
    authorField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    JToolBar authorbar = new JToolBar();
    authorbar.setBorder(toolbarBorder);
    authorbar.setFloatable(false);
    authorbar.setOpaque(false);
    authorbar.add(authorLabel);
    authorbar.add(authorField);
    // contact
    contactLabel = new JLabel();
    contactField = new EntryField(30);
    contactField.setText(trackerPanel.contact);
    contactField.setBackground(Color.white);
    contactField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    JToolBar contactbar = new JToolBar();
    contactbar.setBorder(toolbarBorder);
    contactbar.setFloatable(false);
    contactbar.setOpaque(false);
    contactbar.add(contactLabel);
    contactbar.add(contactField);
    // keywords
    keywordsLabel = new JLabel();
    keywordsField = new EntryField(30);
    keywordsField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    JToolBar keywordsbar = new JToolBar();
    keywordsbar.setBorder(toolbarBorder);
    keywordsbar.setFloatable(false);
    keywordsbar.setOpaque(false);
    keywordsbar.add(keywordsLabel);
    keywordsbar.add(keywordsField);	  
    // URL
    urlLabel = new JLabel();
    urlField = new EntryField(30);
    urlField.setAlignmentY(JToolBar.TOP_ALIGNMENT);
    urlField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	refreshGUI();     	
      }
    });
    JToolBar urlbar = new JToolBar();
    urlbar.setBorder(toolbarBorder);
    urlbar.setFloatable(false);
    urlbar.setOpaque(false);
    urlbar.add(urlLabel);
    urlbar.add(urlField);
    
    metadataBox.add(htmlbar);
    metadataBox.add(titlebar);
    metadataBox.add(descriptionbar);
    metadataBox.add(authorbar);
    metadataBox.add(contactbar);
    metadataBox.add(keywordsbar);
    metadataBox.add(urlbar);

    // thumbnail panel
    thumbnailDisplay = new JLabel();
    Border line = BorderFactory.createLineBorder(Color.black);
    Border empty = BorderFactory.createEmptyBorder(0, 2, 0, 2);
    thumbnailDisplay.setBorder(BorderFactory.createCompoundBorder(empty, line));
    thumbnailButton = new JButton();
    thumbnailButton.setForeground(labelColor);
    thumbnailButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	ThumbnailDialog.getDialog(trackerPanel, false).setVisible(true);
      }
    });
    showThumbnailCheckbox = new JCheckBox();
    showThumbnailCheckbox.setSelected(false);
    showThumbnailCheckbox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (showThumbnailCheckbox.isSelected()) {
          thumbnailPanel.add(imagePanel, BorderLayout.CENTER);
      	}
      	else {
      		thumbnailPanel.remove(imagePanel);
      	}
      	pack();
      	repaint();
      }
    });
    thumbnailPanel = new JPanel(new BorderLayout());
    imagePanel = new JPanel();
    imagePanel.add(thumbnailDisplay);
    if (showThumbnailCheckbox.isSelected()) {
    	thumbnailPanel.add(imagePanel, BorderLayout.CENTER);
    }
    JPanel panel = new JPanel();
    panel.add(thumbnailButton);
    panel.add(showThumbnailCheckbox);
    thumbnailPanel.add(panel, BorderLayout.NORTH);
  	ThumbnailDialog dialog = ThumbnailDialog.getDialog(trackerPanel, false);
  	dialog.addPropertyChangeListener("accepted", new PropertyChangeListener() { //$NON-NLS-1$
		  public void propertyChange(PropertyChangeEvent e) {
		    refreshThumbnail();
		  }
  	});
    refreshThumbnail();
    
    // assemble    
    centerPanel = new JPanel(new BorderLayout());
    JPanel upper = new JPanel(new BorderLayout());
	  upper.add(tabsPanel, BorderLayout.NORTH);  		
	  upper.add(videoPanel, BorderLayout.SOUTH);  		
    centerPanel.add(upper, BorderLayout.NORTH);    
    centerPanel.add(thumbnailPanel, BorderLayout.CENTER);    
    contentPane.add(metadataBox, BorderLayout.NORTH);
    contentPane.add(centerPanel, BorderLayout.CENTER);
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    
    labels.add(authorLabel);
    labels.add(contactLabel);
    labels.add(descriptionLabel);
    labels.add(keywordsLabel);
    labels.add(urlLabel);
    labels.add(titleLabel);
    labels.add(htmlLabel);
    
  	tabsBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
  	tabsPanel.setBorder(tabsBorder);
  	tabsBorder.setTitleColor(labelColor);
  	videoBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
  	videoPanel.setBorder(videoBorder);
  	videoBorder.setTitleColor(labelColor);
  	thumbnailBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
  	thumbnailBorder.setTitleColor(labelColor);
  	thumbnailPanel.setBorder(thumbnailBorder);
  	metadataBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
  	metadataBorder.setTitleColor(labelColor);
  	metadataBox.setBorder(metadataBorder);
  	
		addedFilesDialog = new AddedFilesDialog();
		addedFilesDialog.setLocationRelativeTo(ExportZipDialog.this);
		
  }
  
  /**
   * Refreshes the thumbnail image based on the current ThumbnailDialog settings.
   */
  private void refreshThumbnail() {
  	ThumbnailDialog thumbnailDialog = ThumbnailDialog.getDialog(trackerPanel, false);
  	BufferedImage image = thumbnailDialog.getThumbnail();
		thumbnailDisplay.setIcon(new ImageIcon(image));
		pack();
  }
  
  /**
   * Refreshes the visible components of this dialog.
   */
  private void refreshGUI() {
  	// refresh strings
  	String title = TrackerRes.getString("ZipResourceDialog.Title"); //$NON-NLS-1$
  	setTitle(title);
  	 
    // borders
  	metadataBorder.setTitle(TrackerRes.getString("ZipResourceDialog.Border.Title.Documentation")); //$NON-NLS-1$
    videoBorder.setTitle(TrackerRes.getString("ZipResourceDialog.Border.Title.Video")); //$NON-NLS-1$
    tabsBorder.setTitle(TrackerRes.getString("ExportZipDialog.Border.Title.Tabs")); //$NON-NLS-1$
    thumbnailBorder.setTitle(TrackerRes.getString("ZipResourceDialog.Border.Title.Thumbnail")); //$NON-NLS-1$
    
    // buttons
    clipCheckbox.setText(TrackerRes.getString("ZipResourceDialog.Checkbox.TrimVideo")); //$NON-NLS-1$
    helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$  
    addFilesButton.setText(TrackerRes.getString("ZipResourceDialog.Button.AddFiles")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    saveButton.setText(TrackerRes.getString("ExportVideoDialog.Button.SaveAs")); //$NON-NLS-1$
    closeButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
    thumbnailButton.setText(TrackerRes.getString("ZipResourceDialog.Button.ThumbnailSettings")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
    showThumbnailCheckbox.setText(TrackerRes.getString("ZipResourceDialog.Checkbox.PreviewThumbnail")); //$NON-NLS-1$
    
    // labels
  	formatLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Format")+": "); //$NON-NLS-1$ //$NON-NLS-2$
  	htmlLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.HTML")); //$NON-NLS-1$
    titleLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Title")); //$NON-NLS-1$
    descriptionLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Description")); //$NON-NLS-1$
    authorLabel.setText(TrackerRes.getString("PropertiesDialog.Label.Author")); //$NON-NLS-1$
    contactLabel.setText(TrackerRes.getString("PropertiesDialog.Label.Contact")); //$NON-NLS-1$
    keywordsLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Keywords")); //$NON-NLS-1$
    urlLabel.setText(TrackerRes.getString("ZipResourceDialog.Label.Link")); //$NON-NLS-1$
    
    // tooltips
  	htmlLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.HTML")+": "); //$NON-NLS-1$ //$NON-NLS-2$
  	htmlField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.HTML")+": "); //$NON-NLS-1$ //$NON-NLS-2$
    titleLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Title")); //$NON-NLS-1$
    titleField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Title")); //$NON-NLS-1$
    descriptionLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Description")); //$NON-NLS-1$
    descriptionPane.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Description")); //$NON-NLS-1$
    authorLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Author")); //$NON-NLS-1$
    authorField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Author")); //$NON-NLS-1$
    contactLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Contact")); //$NON-NLS-1$
    contactField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Contact")); //$NON-NLS-1$
    keywordsLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Keywords")); //$NON-NLS-1$
    keywordsField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Keywords")); //$NON-NLS-1$
    urlLabel.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Link")); //$NON-NLS-1$
    urlField.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.Link")); //$NON-NLS-1$
    clipCheckbox.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.TrimVideo")); //$NON-NLS-1$
    addFilesButton.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.AddFiles")); //$NON-NLS-1$
    thumbnailButton.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.ThumbnailSettings")); //$NON-NLS-1$
    loadHTMLButton.setToolTipText(TrackerRes.getString("ZipResourceDialog.Tooltip.LoadHTML")); //$NON-NLS-1$

    // set label sizes
    FontRenderContext frc = new FontRenderContext(null, false, false); 
    Font font = titleLabel.getFont();
    int w = 0;
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
      w = Math.max(w, (int) rect.getWidth()+1);
    }
    int h = titleField.getMinimumSize().height;
    Dimension labelSize = new Dimension(w, h);
    for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
      JLabel next = it.next();
      next.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
      next.setPreferredSize(labelSize);
      next.setHorizontalAlignment(SwingConstants.TRAILING);
      next.setAlignmentY(Box.TOP_ALIGNMENT);
    }
    
  	// set html field properties
  	String path = htmlField.getText().trim();
  	Resource res = null;
  	if (!path.equals(htmlField.getDefaultText()) && !path.equals("")) {  //$NON-NLS-1$
  		res = ResourceLoader.getResource(path);
	  	htmlField.setForeground(res==null? Color.red: EntryField.defaultForeground);
  	}
    htmlField.setBackground(Color.white);
  		
  	// set url field properties
  	path = urlField.getText().trim();
  	if (!path.equals("")) {  //$NON-NLS-1$
    	try {
				new URL(path); // throws exception if malformed
		  	urlField.setForeground(EntryField.defaultForeground);
			} catch (MalformedURLException e) {
		  	urlField.setForeground(Color.red);
			}
  	}
  	
  	// enable/disable urlField and descriptionPane
  	urlField.setEnabled(res==null);
  	urlLabel.setEnabled(res==null);
  	descriptionPane.setEnabled(res==null);
  	descriptionLabel.setEnabled(res==null);
  	
  	// tabsPanel
  	// get list of current tab titles
		ArrayList<String> currentTabs = new ArrayList<String>();
		for (int i=0; i<frame.getTabCount(); i++) {
			currentTabs.add(frame.getTabTitle(i));
		}
		
		// add checkboxes and entry fields if needed
  	final String equalsign = " ="; //$NON-NLS-1$
  	if (tabCheckboxes.size()>currentTabs.size()) {
  		ArrayList<JCheckBox> tempboxes = new ArrayList<JCheckBox>();
  		ArrayList<EntryField> tempfields = new ArrayList<EntryField>();
  		// collect checkboxes and entry fields found in current tabs
  		for (int i=0; i<tabCheckboxes.size(); i++) {
  			String s = tabCheckboxes.get(i).getText();
  			if (s.endsWith(equalsign)) {
  				s = s.substring(0, equalsign.length());
  			}
        if (currentTabs.contains(s)) {
        	tempboxes.add(tabCheckboxes.get(i));
        	tempfields.add(tabTitleFields.get(i));
        }
  		}
  		tabCheckboxes = tempboxes;
  		tabTitleFields = tempfields;
  	}
  	if (tabCheckboxes.size()<currentTabs.size()) {
  		// compare current tab names with previous existing tabs
  		for (int i=0; i<tabCheckboxes.size(); i++) { 
  			JCheckBox existing = tabCheckboxes.get(i);
  			if (!existing.getText().equals(currentTabs.get(i))) {
  	      EntryField field = tabTitleFields.get(i);
  	      // strip extension for initial tab title
  	      String s = XML.stripExtension(currentTabs.get(i));
  	      field.setText(s);  				
  	      field.setBackground(Color.WHITE);
  			}
  		}
  		// add new checkboxes and entry fields
  		for (int i=tabCheckboxes.size(); i<frame.getTabCount(); i++) { 
  			JCheckBox cb = new JCheckBox();
  			cb.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						refreshGUI();
						// request focus
						JCheckBox cb = (JCheckBox)e.getSource();
						cb.requestFocusInWindow();
					}
  			});
  			if (i==frame.getSelectedTab()) {
  				cb.setSelected(true);
  			}
	      tabCheckboxes.add(cb);
	      EntryField field = new EntryField();
	      // strip extension for initial tab title
	      String s = XML.stripExtension(currentTabs.get(i));
	      field.setText(s);
	      tabTitleFields.add(field);
	      field.setBackground(Color.WHITE);
  		}  		
  	}
  	
  	// reassemble tabs panel
  	if (tabCheckboxes.size()==frame.getTabCount()) { // should always be true at this point
      tabsPanel.removeAll();
      Box stack = Box.createVerticalBox();
			tabsPanel.add(stack);
      Box box = null;
  		for (int i=0; i<frame.getTabCount(); i++) {
  			if (i%3==0) {
  				box = Box.createHorizontalBox();
	  			stack.add(box);
  			}
  			JCheckBox next = tabCheckboxes.get(i);
  			next.setText(next.isSelected()? currentTabs.get(i)+" =": currentTabs.get(i)); //$NON-NLS-1$
  			box.add(next);
  			if (next.isSelected()) {
  				box.add(tabTitleFields.get(i));
  			}
  			box.add(Box.createHorizontalStrut(6));
   		}
  	}
  	
  	// enable video panel if any tabs have videos
    boolean hasVideo = trackerPanel.getVideo()!=null;
		for (int i=0; i<frame.getTabCount(); i++) { 
			hasVideo = hasVideo || (frame.getTrackerPanel(i)!=null && frame.getTrackerPanel(i).getVideo()!=null);
		}
    clipCheckbox.setEnabled(hasVideo);
    if (!hasVideo) {
    	clipCheckbox.setSelected(false);
    }
  	formatDropdown.setEnabled(clipCheckbox.isSelected());
  	formatLabel.setEnabled(clipCheckbox.isSelected());    	
  	  		
		pack();
		repaint();
  }
  
  /**
   * Refreshes the format dropdown.
   */
  private void refreshFormatDropdown() {
  	ExportVideoDialog.refreshFormats();
  	videoExporter.refreshFormatDropdown(preferredExtension);
    formatDropdown.removeAllItems();
    for (Object format: videoExporter.getFormats()) {
    	formatDropdown.addItem(format);    	
    }
    formatDropdown.setSelectedItem(videoExporter.getFormat());
  }

  /**
   * Refreshes the text fields by reading the HTML code of a file.
   * @param htmlFile the file to read
   */
  private void refreshFieldsFromHTML(File htmlFile) {
		String html = ResourceLoader.getString(htmlFile.getAbsolutePath());
		if (html==null) return;
		String title = ResourceLoader.getTitleFromHTMLCode(html);
		if (title!=null) {
			titleField.setText(title);
			titleField.setBackground(Color.white);
		}
		ArrayList<String[]> metadata = getMetadataFromHTML(html);
		for (int i=metadata.size()-1; i>=0; i--) {
			// go backwards so if multiple authors, first one in the list is only one changed
			String[] next = metadata.get(i);
			String key = next[0];
			String value = next[1];
			if (LibraryResource.META_AUTHOR.toLowerCase().contains(key.toLowerCase())) {
				authorField.setText(value);
				authorField.setBackground(Color.white);
			}
			else if (LibraryResource.META_CONTACT.toLowerCase().contains(key.toLowerCase())) {
				contactField.setText(value);
				contactField.setBackground(Color.white);
			}
			else if (LibraryResource.META_KEYWORDS.toLowerCase().contains(key.toLowerCase())) {
				keywordsField.setText(value);
				keywordsField.setBackground(Color.white);
			}
			else if ("description".contains(key.toLowerCase())) { //$NON-NLS-1$
				descriptionPane.setText(value);
				descriptionPane.setBackground(Color.white);
			}
			else if ("url".contains(key.toLowerCase())) { //$NON-NLS-1$
				urlField.setText(value);
				urlField.setBackground(Color.white);
			}
		}
  }
  
  /**
   * Add videos and TRK files to the zip list.  
   * @param zipList the list of files to be zipped
   */
  private void addVideosAndTRKs(final ArrayList<File> zipList) {
		isWaitingForVideo = false;
		final String[] videoPath = new String[1];
		videoExporter.setFormat(formatDropdown.getSelectedItem());
		final PropertyChangeListener listener = new PropertyChangeListener() {
	  	public void propertyChange(PropertyChangeEvent e) {
	  		videoPath[0] = null; // stays null if video_cancelled
	  		if (e.getPropertyName().equals("video_saved")) { //$NON-NLS-1$
	  			// videoPath is new value from event (different from original path for image videos)
	  			videoPath[0] = e.getNewValue().toString();    	  			
	  		}
    		isWaitingForVideo = false;
	  	}
		};
    videoExporter.addPropertyChangeListener("video_saved", listener);  //$NON-NLS-1$
    videoExporter.addPropertyChangeListener("video_cancelled", listener);     //$NON-NLS-1$
		// save VideoIO preferred export format
		videoIOPreferredExtension = VideoIO.getPreferredExportExtension();
		
  	// process TrackerPanels according to checkbox status
		for (int i = 0; i<tabCheckboxes.size(); i++) {
			JCheckBox box = tabCheckboxes.get(i);
			if (!box.isSelected()) continue;
  		TrackerPanel nextTrackerPanel = frame.getTrackerPanel(i);
  		if (nextTrackerPanel==null) continue;
  		
  		// get tab title to add to video and TRK names
  		String tabTitle = i>=tabTitleFields.size()? null: tabTitleFields.get(i).getText().trim();
  		String trkPath = getTRKTarget(tabTitle);
  		
  		// export or copy video, if any
    	if (clipCheckbox.isSelected()) {
	    	// export video clip using videoExporter
      	videoExporter.setTrackerPanel(nextTrackerPanel);
    		// define the path for the exported video
    		VideoType format = ExportVideoDialog.formats.get(formatDropdown.getSelectedItem());
    		String extension = format.getDefaultExtension();
    		videoPath[0] = getVideoTarget(tabTitle, extension);
    		// set the waiting flag
    		isWaitingForVideo = true;
    		// render the video (also sets VideoIO preferred extension to this one)
    		videoExporter.exportFullSizeVideo(videoPath[0]);
    	}      	
    	else { // original or no video
  	  	Video vid = nextTrackerPanel.getVideo();
    		if (vid!=null && vid.getProperty("absolutePath")!=null) { //$NON-NLS-1$
    	  	String originalPath = (String)vid.getProperty("absolutePath"); //$NON-NLS-1$
    			// copy or extract original video to target directory
    	  	String vidDir = getTempDirectory()+videoSubdirectory;
      		videoPath[0] = vidDir+"/"+XML.getName(originalPath); //$NON-NLS-1$
      		// check if target video file already exists
      		boolean videoexists = new File(videoPath[0]).exists();
      		if (!videoexists) {
	    	  	new File(vidDir).mkdirs();
	  		    if (!copyOrExtractFile(originalPath, new File(videoPath[0]))) {
	  	  	  	javax.swing.JOptionPane.showMessageDialog(
	  	  	  			ExportZipDialog.this,	    			
	  	  	  			TrackerRes.getString("ZipResourceDialog.Dialog.ExportFailed.Message"), //$NON-NLS-1$ 
	  	  	  			TrackerRes.getString("ZipResourceDialog.Dialog.ExportFailed.Title"), //$NON-NLS-1$ 
	  	  	  			javax.swing.JOptionPane.ERROR_MESSAGE);
	  	  	  	return;
	  		    }
      		}
    	  	// if image video, then copy/extract additional image files
          if (vid instanceof ImageVideo) {
          	ImageVideo imageVid = (ImageVideo)vid;
          	String[] paths = imageVid.getValidPaths();
          	// first path is originalPath relative to base
          	int n = originalPath.indexOf(XML.getName(paths[0]));
          	if (n>0) {
          		String base = originalPath.substring(0, n);
            	for (String path: paths) {
            		String name = XML.getName(path);
            		path = base+name;
            		if (path.equals(originalPath)) continue;
            		File target = new File(vidDir+"/"+name); //$NON-NLS-1$
        		    if (!copyOrExtractFile(path, target)) {
        	  	  	javax.swing.JOptionPane.showMessageDialog(
        	  	  			ExportZipDialog.this,	    			
        	  	  			TrackerRes.getString("ZipResourceDialog.Dialog.ExportFailed.Message"), //$NON-NLS-1$ 
        	  	  			TrackerRes.getString("ZipResourceDialog.Dialog.ExportFailed.Title"), //$NON-NLS-1$ 
        	  	  			javax.swing.JOptionPane.ERROR_MESSAGE);
        	  	  	return;
        		    }
            	}
          	}
          }
    		}   		
    	} // end setting up video 
    	
    	// wait for video to be ready--when isWaitingForVideo is false
    	while (isWaitingForVideo) {
    		try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    	}
    	// video should be ready at this point
    	// add video file to ziplist
    	if (videoPath[0]!=null) {
    		File videoFile = new File(videoPath[0]);
    		// add to ziplist unless it is a duplicate
    		if (!zipList.contains(videoFile)) {
    			zipList.add(videoFile);
    		}
    	}
    	
    	// create and modify TrackerPanel XMLControl
      XMLControl control = new XMLControlElement(nextTrackerPanel);
    	// modify video path, clip settings of XMLControl
    	if (clipCheckbox.isSelected()) {
    		modifyControlForClip(nextTrackerPanel, control, videoPath[0], trkPath);
    	}
    	else if (nextTrackerPanel.getVideo()!=null) {
  	    XMLControl videoControl = control.getChildControl("videoclip").getChildControl("video"); //$NON-NLS-1$ //$NON-NLS-2$
  	    if (videoControl!=null) {
  	    	videoControl.setValue("path", XML.getPathRelativeTo(videoPath[0], getTempDirectory())); //$NON-NLS-1$
  	    }	
    	}
    	
    	// add local HTML files to zipList and modify XMLControl accordingly
    	ArrayList<String> htmlPaths = getHTMLPaths(control);
    	if (!htmlPaths.isEmpty()) {
  	  	String xml = control.toXML();
  	  	for (String nextHTMLPath: htmlPaths) {
  	  		String path = copyAndAddHTMLPage(nextHTMLPath, zipList);
  	  		if (path!=null) {
  	  			xml = substitutePathInText(xml, nextHTMLPath, path, ">", "<"); //$NON-NLS-1$ //$NON-NLS-2$
  	  		}
  	  	}
  	  	control = new XMLControlElement(xml);
    	}
    	
    	// write XMLControl to TRK file and add to zipList
      trkPath = control.write(trkPath);    
      File trkFile = new File(trkPath);
  		// add to ziplist unless it is a duplicate
  		if (!zipList.contains(trkFile)) {
  			zipList.add(trkFile);
  		}
    	
  	} // end of nextTrackerPanel
  	
    videoExporter.removePropertyChangeListener("video_saved", listener);  //$NON-NLS-1$
    videoExporter.removePropertyChangeListener("video_cancelled", listener);     //$NON-NLS-1$
  } 
  
  /**
   * Adds "added files" to the zip list
   * @param zipList the list of files to be zipped
   */
  private void addFiles(ArrayList<File> zipList) {
  	
  	// add "added files"
  	for (File file: addedFilesDialog.addedFiles) {
  		String next = file.getAbsolutePath();
  		boolean isHTML = XML.getExtension(next).startsWith("htm"); //$NON-NLS-1$
  		if (isHTML) {
  			copyAndAddHTMLPage(next, zipList);
  		}
  		else {
    		String dir = getTempDirectory();
    		File targetFile = new File(dir, XML.getName(next));
    		VideoIO.copyFile(file, targetFile);
  			zipList.add(targetFile);
  		}
  	}
  	
  }

  /**
   * Saves a zip resource containing the files in the list. 
   * @param zipList the list of files to be zipped
   */
  private void saveZip(ArrayList<File> zipList) {  	
  	// define zip target and compress with JarTool
  	File target = new File(getZIPTarget());
  	if (JarTool.compress(zipList, target, null)) {
  		// offer to open the newly created zip file
  		openZip(target.getAbsolutePath());
  		// delete temp directory after short delay
      Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
      		File temp = new File(getTempDirectory());
          ResourceLoader.deleteFile(temp);
         }
      });
  		timer.setRepeats(false);
  		timer.start();
  	}
  }
  
  /**
   * Writes an HTML info file from scratch, using the current field text.
   */
  private File writeHTMLInfo(String thumbPath, String redirectPath) {
  	File htmlTarget = new File(getHTMLDirectory());
  	htmlTarget.mkdirs();
  	htmlTarget = new File(htmlTarget, targetName+"_info.html"); //$NON-NLS-1$
  	thumbPath = XML.getPathRelativeTo(thumbPath, getHTMLDirectory());
    String title = titleField.getText().trim();
    String description = descriptionPane.getText().trim();
    String author = authorField.getText().trim();
    String contact = contactField.getText().trim();
    String keywords = keywordsField.getText().trim();
    String uri = urlField.getText().trim();
    
    Map<String, String> metadata = new TreeMap<String, String>();
    if (!"".equals(author)) metadata.put("author", author); //$NON-NLS-1$ //$NON-NLS-2$
    if (!"".equals(contact)) metadata.put("contact", contact); //$NON-NLS-1$ //$NON-NLS-2$
    if (!"".equals(keywords)) metadata.put("keywords", keywords); //$NON-NLS-1$ //$NON-NLS-2$
    if (!"".equals(description)) metadata.put("description", description); //$NON-NLS-1$ //$NON-NLS-2$
    if (!"".equals(uri)) metadata.put("URL", uri); //$NON-NLS-1$ //$NON-NLS-2$
    
    String htmlCode = LibraryResource.getHTMLCode(title, LibraryResource.TRACKER_TYPE,
    		thumbPath, description, author, contact, uri, null, metadata);
    
    // insert redirect comment immediately after <html> tag
    if (redirectPath!=null) {
    	String comment = "\n<!--redirect: "+redirectPath+"-->"; //$NON-NLS-1$ //$NON-NLS-2$
    	int n = htmlCode.indexOf("<html>"); //$NON-NLS-1$
    	htmlCode = htmlCode.substring(0, n+6)+comment+htmlCode.substring(n+6);
    }
    
    return writeFile(htmlCode, htmlTarget);
  }
  
  /**
   * Writes a text file.
   * 
   * @param text the text
   * @param target the File to write
   * @return the written File, or null if failed
   */
  private File writeFile(String text, File target) {
    try {
      FileWriter fout = new FileWriter(target);
      fout.write(text);
      fout.close();
	  	return target;
    } catch(Exception ex) {}
    return null;
  }
  
  /**
   * Writes a thumbnail image to the temp directory and adds it to the zip list.
   * @param zipList the list of files to be zipped
   * @return the absolute path to the image, or null if failed
   */
  private String addThumbnail(ArrayList<File> zipList) {
	  // use ThumbnailDialog to write image to temp folder and add to zip list
  	ThumbnailDialog dialog = ThumbnailDialog.getDialog(trackerPanel, false);
  	String ext = dialog.getFormat();
	  String thumbPath = getTempDirectory()+targetName+"_thumbnail."+ext; //$NON-NLS-1$
	  File thumbnail = dialog.saveThumbnail(thumbPath);
	  if (thumbnail==null) return null;
	  zipList.add(thumbnail);
	  return thumbPath;
  }
  
  /**
   * Copies, downloads or extracts a file to a target.
   * @param filePath the path
   * @param targetFile the target file
   * @return true if successful
   */
  private boolean copyOrExtractFile(String filePath, File targetFile) {
  	String lowercase = filePath.toLowerCase();
  	// if file is on server, download it
  	if (filePath.startsWith("http")) { //$NON-NLS-1$
  		targetFile = ResourceLoader.download(filePath, targetFile, false);
  	}
  	// if file is in zip or jar, then extract it
  	else if (lowercase.contains("trz!") || lowercase.contains("jar!") || lowercase.contains("zip!")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    targetFile = JarTool.extract(filePath, targetFile);  		
  	}
  	// otherwise copy it      	  	
  	else VideoIO.copyFile(new File(filePath), targetFile);
  	return targetFile.exists();
  }

  
  /**
   * Adds an appropriate HTML info file to the temp directory and adds it to the zip list.
   * This looks first for a file specified in the htmlField, then for a file with name "zipName_info.html",
   * and failing that writes a file from scratch.
   * @param thumbPath the path to the thumbnail image
   * @param zipList the list of files to be zipped
   * @return true if succeeds
   */
  private boolean addHTMLInfo(String thumbPath, ArrayList<File> zipList) {
  	// see if HTML info resource is defined in htmlField
  	Resource res = ResourceLoader.getResource(htmlField.getText().trim());
  	if (res==null) {
    	// look for HTML info resource in target directory
    	File[] files = new File(targetDirectory).listFiles();
    	boolean added = false;
    	for (File next: files) {
    		String name = XML.stripExtension(next.getName());
    		String ext = XML.getExtension(next.getName());
    		if ("html".equals(ext) || "htm".equals(ext)) { //$NON-NLS-1$ //$NON-NLS-2$
    			if (name.equals(targetName) || name.equals(targetName+"_info")) { //$NON-NLS-1$
    				// look first in added files
    		  	for (File file: addedFilesDialog.addedFiles) {  		  		
    		  		added = added || file.getName().equals(next.getName());
    		  	}
  		  		if (!added) {
  		  			// offer to add HTML to zip
  		  	  	int response = javax.swing.JOptionPane.showConfirmDialog(
  		  	  			frame,	    			
  		  	  			TrackerRes.getString("ZipResourceDialog.AddHTMLInfo.Message1") //$NON-NLS-1$ 
  		  	  			+" \""+next.getName()+"\"\n" //$NON-NLS-1$ //$NON-NLS-2$
  		  	  			+TrackerRes.getString("ZipResourceDialog.AddHTMLInfo.Message2"), //$NON-NLS-1$ 
  		  	  			TrackerRes.getString("ZipResourceDialog.AddHTMLInfo.Title"), //$NON-NLS-1$ 
  		  	  			javax.swing.JOptionPane.YES_NO_OPTION, 
  		  	  			javax.swing.JOptionPane.QUESTION_MESSAGE);
  		  	  	if (response == javax.swing.JOptionPane.YES_OPTION) {
  		  	  		res = ResourceLoader.getResource(next.getAbsolutePath());
  		  	  	}
  		  		}
    			}
    		}
    	}  		
  	}
  	
  	String redirect = null; // used below in writeHTMLInfo method
  	if (res!=null) {
  		if (res.getFile()!=null) {
	  		// resource is a local file, so write temp target and add to zip list
	  		String html = res.getString();
	  		if (html!=null && html.trim().startsWith("<!DOCTYPE html")) { //$NON-NLS-1$
					File htmlTarget = writeTempHTMLTarget(html, res);
					if (htmlTarget!=null) {
						String path = copyAndAddHTMLPage(htmlTarget.getAbsolutePath(), zipList);
						if (!htmlTarget.equals(res.getFile()))
							htmlTarget.delete();
			  		return path!=null;
					}
				}
	  	}
  		else { // resource is a web file 			
  			redirect = res.getAbsolutePath();
  		}
  	}
  	  	
		// write HTML info from scratch
  	File htmlTarget = writeHTMLInfo(thumbPath, redirect);
		if (htmlTarget==null) return false;
		if (!"".equals(htmlSubdirectory)) { //$NON-NLS-1$
			htmlTarget = htmlTarget.getParentFile();
		}

		if (!zipList.contains(htmlTarget))
			zipList.add(htmlTarget);
  	return true;
  }
  
  /**
   * Modifies a TrackerPanel XMLControl to work with a trimmed video clip.
   * @param toModify the XMLControl to be modified 
   * @param videoPath the exported video path (may be null)
   */
  private void modifyControlForClip(TrackerPanel tPanel, XMLControl toModify, String videoPath, String trkPath) {
    VideoPlayer player = tPanel.getPlayer();
    
    // videoclip--convert frame count, start frame, step size and frame shift but not start time or step count
    XMLControl clipXMLControl = toModify.getChildControl("videoclip"); //$NON-NLS-1$
    VideoClip realClip = player.getVideoClip();
    clipXMLControl.setValue("video_framecount", clipXMLControl.getInt("stepcount")); //$NON-NLS-1$ //$NON-NLS-2$
    clipXMLControl.setValue("startframe", 0); //$NON-NLS-1$
    clipXMLControl.setValue("stepsize", 1); //$NON-NLS-1$
//    clipXMLControl.setValue("frameshift", 0); //$NON-NLS-1$
    if (videoPath!=null) {
	    // modify videoControl with correct video type, and add delta_t for image videos
  		VideoType format = ExportVideoDialog.formats.get(formatDropdown.getSelectedItem());
  		Video newVideo = format.getVideo(videoPath);
  		clipXMLControl.setValue("video", newVideo); //$NON-NLS-1$
  		
	    XMLControl videoControl = clipXMLControl.getChildControl("video"); //$NON-NLS-1$
	    if (videoControl!=null) {
	    	videoControl.setValue("path", XML.getPathRelativeTo(videoPath, XML.getDirectoryPath(trkPath))); //$NON-NLS-1$
	    	videoControl.setValue("filters", null); //$NON-NLS-1$
	    	if (format instanceof ImageVideoType) {
	    		videoControl.setValue("paths", null); //$NON-NLS-1$ // eliminates unneeded full list of image files
	    		videoControl.setValue("delta_t", player.getMeanStepDuration()); //$NON-NLS-1$

	    	}
	    }	
    } 
           
    // clipcontrol
    XMLControl clipControlControl = toModify.getChildControl("clipcontrol"); //$NON-NLS-1$
    clipControlControl.setValue("delta_t", player.getMeanStepDuration()); //$NON-NLS-1$
    clipControlControl.setValue("frame", 0); //$NON-NLS-1$

    // imageCoordSystem
    XMLControl coordsControl = toModify.getChildControl("coords"); //$NON-NLS-1$
    Object array = coordsControl.getObject("framedata"); //$NON-NLS-1$
    ImageCoordSystem.FrameData[] coordKeyFrames = (ImageCoordSystem.FrameData[])array;
    Map<Integer, Integer> newFrameNumbers = new TreeMap<Integer, Integer>();
    
    int newFrameNum = 0;
    for (int i = 0; i<coordKeyFrames.length; i++) {
    	if (coordKeyFrames[i]==null) continue;
    	if (i>=realClip.getEndFrameNumber()) break;
    	newFrameNum = Math.max(realClip.frameToStep(i), 0);
    	if (i>realClip.getStartFrameNumber() && !realClip.includesFrame(i))
    		newFrameNum++;
    	newFrameNumbers.put(newFrameNum, i);    	
    }
    ImageCoordSystem.FrameData[] newKeyFrames = new ImageCoordSystem.FrameData[newFrameNum+1];
    for (Integer k: newFrameNumbers.keySet()) {
    	newKeyFrames[k] = coordKeyFrames[newFrameNumbers.get(k)];
    }
    coordsControl.setValue("framedata", newKeyFrames);  		    	         //$NON-NLS-1$
    
    // tracks
    // first remove bad models, if any
    if (!badModels.isEmpty()) {
      ArrayList<?> tracks = ArrayList.class.cast(toModify.getObject("tracks")); //$NON-NLS-1$
      for (Iterator<?> it = tracks.iterator(); it.hasNext();) {
        TTrack track = (TTrack)it.next();
        if (badModels.contains(track)) {
        	it.remove();
        }
      }
      toModify.setValue("tracks", tracks); //$NON-NLS-1$
    }
    // then modify frame references in track XMLcontrols
    for (Object next: toModify.getPropertyContent()) {
    	if (next instanceof XMLProperty) {
    		XMLProperty prop = (XMLProperty)next;
    		if (prop.getPropertyName().equals("tracks")) {		    	        			 //$NON-NLS-1$    			
	        for (Object obj: prop.getPropertyContent()) {
	        	// every item is an XMLProperty
        		XMLProperty item = (XMLProperty)obj;
        		// the content of each item is the track control
        		XMLControl trackControl = (XMLControl)item.getPropertyContent().get(0);
        		Class<?> trackType = trackControl.getObjectClass();
        		if (PointMass.class.equals(trackType)) {
    	        array = trackControl.getObject("framedata"); //$NON-NLS-1$
    	        PointMass.FrameData[] pointMassKeyFrames = (PointMass.FrameData[])array;
    	        newFrameNumbers.clear();
    	        newFrameNum = 0;
    	        for (int i = 0; i<pointMassKeyFrames.length; i++) {
    	        	if (pointMassKeyFrames[i]==null || !realClip.includesFrame(i)) continue;
    	        	newFrameNum = realClip.frameToStep(i); // new frame number equals step number
    	        	newFrameNumbers.put(newFrameNum, i);    	        	
    	        }
    	        PointMass.FrameData[] newKeys = new PointMass.FrameData[newFrameNum+1];
    	        for (Integer k: newFrameNumbers.keySet()) {
    	        	newKeys[k] = pointMassKeyFrames[newFrameNumbers.get(k)];
    	        }
    	        trackControl.setValue("framedata", newKeys);        			 //$NON-NLS-1$
        		}
        		
        		else if (Vector.class.isAssignableFrom(trackType)) {
    	        array = trackControl.getObject("framedata"); //$NON-NLS-1$
    	        Vector.FrameData[] vectorKeyFrames = (Vector.FrameData[])array;
    	        newFrameNumbers.clear();
    	        newFrameNum = 0;
    	        for (int i = 0; i<vectorKeyFrames.length; i++) {
    	        	if (vectorKeyFrames[i]==null || !realClip.includesFrame(i)) continue;
    	        	newFrameNum = realClip.frameToStep(i);
    	        	newFrameNumbers.put(newFrameNum, i);    	        	
    	        }
    	        Vector.FrameData[] newKeys = new Vector.FrameData[newFrameNum+1];
    	        for (Integer k: newFrameNumbers.keySet()) {
    	        	newKeys[k] = vectorKeyFrames[newFrameNumbers.get(k)];
    	        	newKeys[k].independent = newKeys[k].xc!=0 || newKeys[k].yc!=0;
    	        }
    	        trackControl.setValue("framedata", newKeys);        			 //$NON-NLS-1$
        		}

        		else if (ParticleModel.class.isAssignableFrom(trackType)) {
    	        int frameNum = trackControl.getInt("start_frame"); //$NON-NLS-1$
    	        if (frameNum>0) {
	    	        int newStartFrameNum = realClip.frameToStep(frameNum);
	  	        	// start frame should round up
	  	        	if (frameNum>realClip.getStartFrameNumber() && !realClip.includesFrame(frameNum))
	  	        		newStartFrameNum++;
	    	        trackControl.setValue("start_frame", newStartFrameNum); //$NON-NLS-1$
    	        }
    	        frameNum = trackControl.getInt("end_frame"); //$NON-NLS-1$
    	        if (frameNum>0) {
	    	        int newEndFrameNum = realClip.frameToStep(frameNum);
	  	        	// end frame should round down
	    	        trackControl.setValue("end_frame", newEndFrameNum); //$NON-NLS-1$
    	        }
        		}
        		
        		else if (Calibration.class.equals(trackType) || OffsetOrigin.class.equals(trackType)) {
    	        array = trackControl.getObject("world_coordinates"); //$NON-NLS-1$
    	        double[][] calKeyFrames = (double[][])array;
    	        newFrameNumbers.clear();
    	        newFrameNum = 0;
    	        for (int i = 0; i<calKeyFrames.length; i++) {
    	        	if (calKeyFrames[i]==null) continue;
    	        	newFrameNum = realClip.frameToStep(i);
    	        	newFrameNumbers.put(newFrameNum, i);    	        	
    	        }
    	        double[][] newKeys = new double[newFrameNum+1][];
    	        for (Integer k: newFrameNumbers.keySet()) {
    	        	newKeys[k] = calKeyFrames[newFrameNumbers.get(k)];
    	        }
    	        trackControl.setValue("world_coordinates", newKeys);        			 //$NON-NLS-1$
        		}

        		else if (CircleFitter.class.equals(trackType)) {
    	        int frameNum = trackControl.getInt("absolute_start"); //$NON-NLS-1$
    	        if (frameNum>0) {
	    	        int newStartFrameNum = realClip.frameToStep(frameNum);
	  	        	// start frame should round up
	  	        	if (frameNum>realClip.getStartFrameNumber() && !realClip.includesFrame(frameNum))
	  	        		newStartFrameNum++;
	    	        trackControl.setValue("absolute_start", newStartFrameNum); //$NON-NLS-1$
    	        }
    	        frameNum = trackControl.getInt("absolute_end"); //$NON-NLS-1$
    	        if (frameNum>0) {
	    	        int newEndFrameNum = realClip.frameToStep(frameNum);
	  	        	// end frame should round down
	    	        trackControl.setValue("absolute_end", newEndFrameNum); //$NON-NLS-1$
    	        }
    	        // change and trim keyframe numbers
    	        array = trackControl.getObject("framedata"); //$NON-NLS-1$
    	        double[][] keyFrameData = (double[][])array;
    	        ArrayList<double[]> newKeyFrameData = new ArrayList<double[]>();
    	        newFrameNumbers.clear();
    	        newFrameNum = 0;
    	        for (int i = 0; i<keyFrameData.length; i++) {
    	        	if (keyFrameData[i]==null) continue;
    	        	double[] stepData = keyFrameData[i];
    	        	int keyFrameNum = (int)stepData[0];
    	        	newFrameNum = realClip.frameToStep(keyFrameNum);
    	        	if (newFrameNum>realClip.getLastFrameNumber()
    	        			|| newFrameNum<realClip.getFirstFrameNumber()) continue;
    	        	// change frame number in step data and add to the new key frame data
    	        	stepData[0] = newFrameNum;
    	        	newKeyFrameData.add(stepData);
//    	        	newFrameNumbers.put(newFrameNum, i);  // maps to stepData index       	
    	        }
//    	        double[][] newKeys = new double[newFrameNum+1][];
//    	        for (Integer k: newFrameNumbers.keySet()) {
//    	        	double[] stepData = keyFrameData[newFrameNumbers.get(k)];
//    	        	newKeys[k] = keyFrameData[newFrameNumbers.get(k)];
//    	        }
    	        double[][] newKeyData = newKeyFrameData.toArray(new double[newKeyFrameData.size()][]);
    	        trackControl.setValue("framedata", newKeyData);        			 //$NON-NLS-1$
        		}

        		else if (TapeMeasure.class.equals(trackType)) {
    	        array = trackControl.getObject("framedata"); //$NON-NLS-1$
    	        TapeMeasure.FrameData[] tapeKeyFrames = (TapeMeasure.FrameData[])array;
    	        if (tapeKeyFrames.length>0) {
	    	        newFrameNumbers.clear();
	    	        newFrameNum = 0;
	    	        int newKeysLength = 0;
	    	        int nonNullIndex = 0; 
	    	        for (int i = 0; i<=realClip.getEndFrameNumber(); i++) {
	    	        	if (i<tapeKeyFrames.length && tapeKeyFrames[i]!=null) {
	    	        		nonNullIndex = i;
	    	        	}
	    	        	if (!realClip.includesFrame(i)) continue;
	    	        	newFrameNum = realClip.frameToStep(i); // new frame number equals step number
	    	        	if (nonNullIndex>-1) {
		    	        	newFrameNumbers.put(newFrameNum, nonNullIndex); 
		    	        	newKeysLength = newFrameNum+1;
	    	        		nonNullIndex = -1;
	    	        	}
	    	        	else if (i<tapeKeyFrames.length) {
		    	        	newFrameNumbers.put(newFrameNum, i);    	        	    	        		
		    	        	newKeysLength = newFrameNum+1;
	    	        	}
	    	        }
	    	        TapeMeasure.FrameData[] newKeys = new TapeMeasure.FrameData[newKeysLength];
	    	        for (Integer k: newFrameNumbers.keySet()) {
	    	        	newKeys[k] = tapeKeyFrames[newFrameNumbers.get(k)];
	    	        }
	    	        trackControl.setValue("framedata", newKeys);        			 //$NON-NLS-1$
	        		}
        		}

        		else if (Protractor.class.equals(trackType)) {
    	        array = trackControl.getObject("framedata"); //$NON-NLS-1$
    	        double[][] protractorData = (double[][])array;
    	        newFrameNumbers.clear();
    	        newFrameNum = 0;
    	        int nonNullIndex = 0; 
    	        for (int i = 0; i<protractorData.length; i++) {
    	        	if (i>realClip.getEndFrameNumber()) break;
    	        	if (protractorData[i]!=null) {
    	        		nonNullIndex = i;
    	        	}
    	        	if (!realClip.includesFrame(i)) continue;
    	        	newFrameNum = realClip.frameToStep(i); // new frame number equals step number
    	        	if (nonNullIndex>-1) {
	    	        	newFrameNumbers.put(newFrameNum, nonNullIndex);    	        	    	        		
    	        		nonNullIndex = -1;
    	        	}
    	        	else {
	    	        	newFrameNumbers.put(newFrameNum, i);    	        	    	        		
    	        	}
    	        }
    	        double[][] newKeys = new double[newFrameNum+1][];
    	        for (Integer k: newFrameNumbers.keySet()) {
    	        	newKeys[k] = protractorData[newFrameNumbers.get(k)];
    	        }
    	        trackControl.setValue("framedata", newKeys);        			 //$NON-NLS-1$
        		}
	        }
    		}
    	}
    	
    }
  }
  
  /**
   * Returns a list of particle models with start frames not included in the video clips.
   * These models cannot be exported.
   */
  private ArrayList<ParticleModel> getModelsNotInClips() {
  	ArrayList<ParticleModel> allModels = new ArrayList<ParticleModel>();
  	// process TrackerPanels according to checkbox status
		for (int i = 0; i<tabCheckboxes.size(); i++) {
			JCheckBox box = tabCheckboxes.get(i);
			if (!box.isSelected()) continue;
  		TrackerPanel nextTrackerPanel = frame.getTrackerPanel(i);
  		if (nextTrackerPanel==null) continue;
    	VideoClip clip = nextTrackerPanel.getPlayer().getVideoClip();
    	ArrayList<ParticleModel> models = nextTrackerPanel.getDrawables(ParticleModel.class);
    	for (Iterator<ParticleModel> it = models.iterator(); it.hasNext();) {
    		ParticleModel model = it.next();
    		if (clip.includesFrame(model.getStartFrame())) {
    			it.remove();
    		}
    	}
    	allModels.addAll(models);
		}
  	return allModels;
  }

  
  /**
   * Returns a list of local HTML paths found in the specified XMLControl.
   * 
   * @param control XMLControl for a TrackerPanel
   * @return the list
   */
  private ArrayList<String> getHTMLPaths(XMLControl control) {
		ArrayList<String> pageViews = new ArrayList<String>(); // html pages used in page views
		// extract page view HTML paths
		String xml = control.toXML();
		int j = xml.indexOf("PageTView$TabView"); //$NON-NLS-1$
		while (j>-1) { // page view exists
			xml = xml.substring(j+17);
			String s = "<property name=\"text\" type=\"string\">"; //$NON-NLS-1$
			j = xml.indexOf(s);
			if (j>-1) {
				xml = xml.substring(j+s.length());
				j = xml.indexOf("</property>"); //$NON-NLS-1$
				String text = xml.substring(0, j);
	      Resource res = ResourceLoader.getResource(text);
				if (res!=null && res.getFile()!=null) { // exclude web files
					pageViews.add(text);
				}
			}
			j = xml.indexOf("PageTView$TabView"); //$NON-NLS-1$				
		}
		return pageViews;
  }
  
  /**
   * Returns a list of local image paths found in the specified HTML document.
   * 
   * @param html the HTML code
   * @param basePath the absolute path to the parent directory of the HTML file
   * @param pre a String that should precede image paths
   * @param post a String that should follow image paths
   * @return the list
   */
  private ArrayList<String> getImagePaths(String html, String basePath, String pre, String post) {
		ArrayList<String> images = new ArrayList<String>();
		// extract image paths from html text
		int j = html.indexOf(pre);
		while (j>-1) { // image reference found
			html = html.substring(j+pre.length());
			j = html.indexOf(post);
			if (j>-1) {
				String text = html.substring(0, j); // the image path specified in the html itself
				String path = XML.getResolvedPath(text, basePath);
	      Resource res = ResourceLoader.getResource(path);
				if (res!=null && res.getFile()!=null) { // exclude web files
					images.add(text);
				}
			}
			j = html.indexOf(pre);				
		}
		return images;
  }
  
  /**
   * Copies an HTML file to the temp directory and adds the copy to the target list.
   * @param htmlPath the path to the original HTML file
   * @param zipList the list of files to be zipped
   * @return the relative path to the copy, or null if failed
   */
  private String copyAndAddHTMLPage(String htmlPath, ArrayList<File> zipList) {
  	// read html text
  	String html = null;
  	Resource res = ResourceLoader.getResource(htmlPath);
  	if (res!=null) {
  		html = res.getString();
  	}
  	if (html!=null) {
  		String htmlBasePath = XML.getDirectoryPath(htmlPath);
	  	// get target directory
	  	File htmlTarget = new File(getHTMLDirectory());
	  	htmlTarget.mkdirs();
	  	// add image files
			String pre = "<img src=\""; //$NON-NLS-1$
			String post = "\""; //$NON-NLS-1$
	  	ArrayList<String> imagePaths = getImagePaths(html, htmlBasePath, pre, post);
	  	if (!imagePaths.isEmpty()) {
	  		// copy images into target directory and modify html text
		  	File imageDir = new File(getImageDirectory());
		  	imageDir.mkdirs();
		  	for (String next: imagePaths) {
		  		String path = XML.getResolvedPath(next, htmlBasePath);
		  		res = ResourceLoader.getResource(path);
	  			// copy image and determine its path relative to target
	  			File imageTarget = new File(imageDir, XML.getName(next));
	  			if (res.getFile()!=null) {
		  			VideoIO.copyFile(res.getFile(), imageTarget);		  				
	  			}
	  			path = XML.getPathRelativeTo(imageTarget.getAbsolutePath(), getHTMLDirectory());
		  		html = substitutePathInText(html, next, path, pre, post);
		  	}
  			if (!zipList.contains(imageDir))
  				zipList.add(imageDir);
	  	}
	  	
	  	// if local stylesheet is found, copy it
	  	String css = ResourceLoader.getStyleSheetFromHTMLCode(html);
	  	if (css!=null && !css.startsWith("http:")) { //$NON-NLS-1$
	  		res = ResourceLoader.getResource(XML.getResolvedPath(css, htmlBasePath));
	  		if (res!=null) {
	  			// copy css file into HTMLTarget directory
	  			String cssName = XML.getName(css);
	  			File cssTarget = new File(htmlTarget, XML.getName(cssName));
	  			VideoIO.copyFile(res.getFile(), cssTarget);
	  			// substitute cssName in html
		  		html = substitutePathInText(html, css, cssName, "\"", "\"");	  			 //$NON-NLS-1$ //$NON-NLS-2$
	  		}	  		
	  	}
  		
	  	// write modified html text into target file
      htmlTarget = new File(htmlTarget, XML.getName(htmlPath));
      try {
	      FileWriter fout = new FileWriter(htmlTarget);
	      fout.write(html);
	      fout.close();
	      String relPath = XML.getPathRelativeTo(htmlTarget.getAbsolutePath(), getTempDirectory());
	  		if (!"".equals(htmlSubdirectory)) { //$NON-NLS-1$
	  			htmlTarget = htmlTarget.getParentFile();
	  		}

  			if (!zipList.contains(htmlTarget))
  				zipList.add(htmlTarget);
		  	return relPath;
	    } catch(Exception exc) {
	      exc.printStackTrace();
	    }
  	}
  	return null;
  }
  
  /**
   * Substitutes one path string for another in a body of text so long as the path is
   * preceded by the "pre" string and followed by the "post" string.
   * 
   * @param text the body of text
   * @param prevPath the path to replace
   * @param newPath the new path
   * @param pre a String that precedes the path
   * @param post a String that follows the path
   * @return the modified text
   */
  private String substitutePathInText(String text, String prevPath, String newPath, String pre, String post) {
  	if (prevPath.equals(newPath)) return text;  	
		int i = text.indexOf(pre+prevPath+post);
		while (i>0) {
			text = text.substring(0, i+pre.length())+newPath+text.substring(i+pre.length()+prevPath.length());
			i = text.indexOf(pre+prevPath+post);
		}
  	return text;
  }
  
  /**
   * Offers to open a newly saved zip file.
   *
   * @param path the path to the zip file
   */
  private void openZip(final String path) {
    Runnable runner1 = new Runnable() {
    	public void run() {
      	int response = javax.swing.JOptionPane.showConfirmDialog(
      			frame,	    			
      			TrackerRes.getString("ZipResourceDialog.Complete.Message1") //$NON-NLS-1$ 
      			+" \""+XML.getName(path)+"\".\n" //$NON-NLS-1$ //$NON-NLS-2$
      			+TrackerRes.getString("ZipResourceDialog.Complete.Message2"), //$NON-NLS-1$ 
      			TrackerRes.getString("ZipResourceDialog.Complete.Title"), //$NON-NLS-1$ 
      			javax.swing.JOptionPane.YES_NO_OPTION, 
      			javax.swing.JOptionPane.QUESTION_MESSAGE);
      	if (response == javax.swing.JOptionPane.YES_OPTION) {
      		frame.loadedFiles.remove(path);
      		final File file = new File(path);
          Runnable runner = new Runnable() {
          	public void run() {
    	    		TrackerIO.open(file, frame);
          	}
          };
          SwingUtilities.invokeLater(runner);
      	}
    	}
    };
    SwingUtilities.invokeLater(runner1);  	
  }
  
  /**
   * Uses a file chooser to define a new target name and directory.
   *
   * @return empty List<File> to fill with files to be zipped
   */
  protected ArrayList<File> defineTarget() {
  	// show file chooser to get directory and zip name
    JFileChooser chooser = TrackerIO.getChooser();
    chooser.setDialogTitle(TrackerRes.getString("ZipResourceDialog.FileChooser.SaveZip.Title"));  //$NON-NLS-1$
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(TrackerIO.trzFileFilter);
    chooser.setFileFilter(TrackerIO.trzFileFilter);
  	File[] files = TrackerIO.getChooserFiles("save"); //$NON-NLS-1$
    chooser.resetChoosableFileFilters();
  	if (files==null) return null; // cancelled by user
//  	targetExtension = chooser.getFileFilter()==TrackerIO.zipFileFilter? "zip": "trz"; //$NON-NLS-1$ //$NON-NLS-2$
  	
  	// define target filename and check for reserved characters, including spaces  	
  	targetName = XML.stripExtension(files[0].getName());
  	String[] reserved = new String[] {
  			"/","\\","?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  			"<",">","\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  			"|",":","*","%"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
  	for (String next: reserved) {
  		if (targetName.indexOf(next)>-1) {
  			String list = ""; //$NON-NLS-1$
  			for (int i = 1; i<reserved.length; i++) {
  				list += "    "+reserved[i]; //$NON-NLS-1$
  			}
  	  	javax.swing.JOptionPane.showMessageDialog(
  	  			frame,	    			
  	  			TrackerRes.getString("ZipResourceDialog.Dialog.BadFileName.Message") +"\n"+list, //$NON-NLS-1$ //$NON-NLS-2$
  	  			TrackerRes.getString("ZipResourceDialog.Dialog.BadFileName.Title"), //$NON-NLS-1$ 
  	  			javax.swing.JOptionPane.WARNING_MESSAGE);
  	  	return null;
  		}
  	}
  	
  	// define target directory and extension
  	targetDirectory = files[0].getParent()+"/"; //$NON-NLS-1$
  	targetExtension = "trz"; //$NON-NLS-1$
  	String ext = XML.getExtension(files[0].getName());
  	
  	// check for duplicate file if target extension not used
  	if (!targetExtension.equals(ext)) {
    	File file = new File(XML.stripExtension(files[0].getAbsolutePath())+"."+targetExtension); //$NON-NLS-1$
      if (!TrackerIO.canWrite(file)) return null;
  	}
  	 
  	// clear target video and return empty list
  	targetVideo = null;
  	return new ArrayList<File>();
  }
  
  private String getTRKTarget(String tabTitle) {
  	if (tabTitle==null || "".equals(tabTitle.trim()))  //$NON-NLS-1$
  		return getTempDirectory()+targetName+".trk"; //$NON-NLS-1$
  	else return getTempDirectory()+targetName+"_"+tabTitle+".trk"; //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  private String getVideoTarget(String tabTitle, String extension) {
  	String vidDir = getTempDirectory()+videoSubdirectory;
  	new File(vidDir).mkdirs();
  	if (tabTitle==null || "".equals(tabTitle.trim()))  //$NON-NLS-1$
  		return vidDir+"/"+targetName+"."+extension; //$NON-NLS-1$ //$NON-NLS-2$
  	else return vidDir+"/"+targetName+"_"+tabTitle+"."+extension; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
  
  private String getZIPTarget() {
  	return targetDirectory+targetName+"."+targetExtension; //$NON-NLS-1$
  }
  
  private String getHTMLDirectory() {
  	return getTempDirectory()+htmlSubdirectory+"/"; //$NON-NLS-1$
  }
  
  private String getImageDirectory() {
  	return getTempDirectory()+imageSubdirectory+"/"; //$NON-NLS-1$
  }
  
  private String getTempDirectory() {
  	return targetDirectory+targetName+"_temp/"; //$NON-NLS-1$
  }
  
  protected class VideoListener implements PropertyChangeListener {
  	
  	ArrayList<File> target;
  	ExportVideoDialog dialog;
  	
  	public void propertyChange(PropertyChangeEvent e) {
  		if (e.getPropertyName().equals("video_saved") && target!=null) { //$NON-NLS-1$
  			// event's new value is saved file name (differ from original target name for image videos)
  			targetVideo = e.getNewValue().toString();
  			// save video extension
  			preferredExtension = XML.getExtension(targetVideo);
    		// restore VideoIO preferred extension
    		VideoIO.setPreferredExportExtension(videoIOPreferredExtension);

//	      saveZip(target);
  		}
    	// clean up ExportVideoDialog
  		if (dialog!=null) {
	  		dialog.removePropertyChangeListener("video_saved", videoExportListener);  //$NON-NLS-1$
	  		dialog.removePropertyChangeListener("video_cancelled", videoExportListener);  //$NON-NLS-1$
  		}
  	}
  	void setTargetList(ArrayList<File> list) {
  		target = list;
  	}
  	void setDialog(ExportVideoDialog evd) {
  		dialog = evd;
  	}
  }
  
  protected class AddedFilesDialog extends JDialog {
  	
  	HashSet<File> addedFiles =  new HashSet<File>();
  	TreeSet<String> fileNames = new TreeSet<String>();
    JButton okButton, addButton, removeButton;
    JList fileList;
    DefaultListModel fileListModel;
  	
  	AddedFilesDialog() {
  		super(ExportZipDialog.this, true);
  		createGUI();
  	}
  	
  	void createGUI() {
      JPanel contentPane = new JPanel(new BorderLayout());
      setContentPane(contentPane);
      
      // file list
      fileListModel = new DefaultListModel();
      fileList = new JList(fileListModel);
      fileList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
		      removeButton.setEnabled(fileList.getSelectedValue()!=null);
				}      	
      });
      JScrollPane scroller = new JScrollPane(fileList);
      scroller.setPreferredSize(new Dimension(300, 150));
      contentPane.add(scroller, BorderLayout.CENTER);
      
      // button bar   
      JPanel buttonbar = new JPanel();
      addButton = new JButton();
      addButton.setForeground(labelColor);
      addButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	// show file chooser to add support files
          JFileChooser chooser = TrackerIO.getChooser();
          chooser.setDialogTitle(TrackerRes.getString("ZipResourceDialog.FileChooser.AddFile.Title"));  //$NON-NLS-1$
//          chooser.addChoosableFileFilter(LaunchBuilder.getPDFFilter());
//          chooser.setFileFilter(LaunchBuilder.getHTMLFilter());
        	File[] files = TrackerIO.getChooserFiles("open any"); //$NON-NLS-1$
          chooser.removeChoosableFileFilter(LaunchBuilder.getHTMLFilter());
          chooser.removeChoosableFileFilter(LaunchBuilder.getPDFFilter());
        	if (files==null) return; // cancelled by user
        	addedFiles.add(files[0]);
          refreshFileList();
        }
      });
      removeButton = new JButton();
      removeButton.setForeground(labelColor);
      removeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	String name = (String)fileList.getSelectedValue();
      		if (name!=null) {
          	for (Iterator<File> it = addedFiles.iterator(); it.hasNext();) {
          		File next = it.next();
          		if (name.equals(next.getName())) {
          			it.remove();
          			break;
          		}
          	}
          	refreshFileList();
      		}
        }
      });
      okButton = new JButton();
      okButton.setForeground(labelColor);
      okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setVisible(false);
        }
      });
      buttonbar.add(addButton);
      buttonbar.add(removeButton);
      buttonbar.add(okButton);
      contentPane.add(buttonbar, BorderLayout.SOUTH);
  		
      pack();
  	}
  	
  	void refreshGUI() {
  		setTitle(TrackerRes.getString("ZipResourceDialog.Dialog.AddFiles.Title")); //$NON-NLS-1$
      okButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
      addButton.setText(TrackerRes.getString("Dialog.Button.Add")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
      removeButton.setText(TrackerRes.getString("Dialog.Button.Remove")); //$NON-NLS-1$
      removeButton.setEnabled(fileList.getSelectedValue()!=null);
  	}
  	
    /**
     * Refreshes the file list.
     */
    void refreshFileList() {
    	fileListModel.clear();
    	fileNames.clear();
    	for (File next: addedFiles) {
    		fileNames.add(next.getName());
    	}
    	for (String next: fileNames) {
    		fileListModel.addElement(next);
    	}
    }
    
  }
  
  /**
   * Returns the metadata, if any, defined in HTML code
   * @param htmlCode the HTML code 
   * @return a Map containing metadata names to values found in the code
   */
	public static ArrayList<String[]> getMetadataFromHTML(String htmlCode) {
		ArrayList<String[]> results = new ArrayList<String[]>();
		if (htmlCode==null) return results;
		String[] parts = htmlCode.split("<meta name=\""); //$NON-NLS-1$
		for (int i=1; i<parts.length; i++) { // ignore parts[0]
			// parse metadata and add to array
			int n = parts[i].indexOf("\">"); //$NON-NLS-1$
			if (n>-1) {
				parts[i] = parts[i].substring(0, n);
				String divider = "\" content=\""; //$NON-NLS-1$
  			String[] subparts = parts[i].split(divider);
				if (subparts.length>1) {
					String name = subparts[0];
					String value = subparts[1];
					results.add(new String[] {name, value});
				}
			}
		}
		return results;
	}
  
  /**
   * Replaces metadata in HTML code based on current text in metadata fields
   * and writes the result to a temporary file that is added to the jar, then deleted.
   * @param htmlCode the HTML code
   * @param res the (local File) Resource that is the source of the html code
   * @return the modified code
   */
	private File writeTempHTMLTarget(String htmlCode, Resource res) {
		if (res.getFile()==null) return null;
		String title = ResourceLoader.getTitleFromHTMLCode(htmlCode);
		String newTitle = titleField.getText().trim();
		if (!"".equals(newTitle) && !newTitle.equals(title)) { //$NON-NLS-1$
			title = "<title>"+title+"</title>"; //$NON-NLS-1$ //$NON-NLS-2$
			newTitle = "<title>"+newTitle+"</title>"; //$NON-NLS-1$ //$NON-NLS-2$
			htmlCode = htmlCode.replace(title, newTitle);
		}
		ArrayList<String[]> metadata = getMetadataFromHTML(htmlCode);
		for (String type: LibraryResource.META_TYPES) {
			String newValue = type.equals(LibraryResource.META_AUTHOR)? authorField.getText().trim(): 
					type.equals(LibraryResource.META_CONTACT)? contactField.getText().trim(): 
					type.equals(LibraryResource.META_KEYWORDS)?	keywordsField.getText().trim(): null;
			String prevValue = null;
			String key = null;
			boolean found = false;
			for (String[] next: metadata) {
				if (found) break;
				key = next[0];
				if (type.toLowerCase().contains(key.toLowerCase())) {
					found = true;
					prevValue = next[1];
				}
			}
			if (!found) key = type.toLowerCase();
			htmlCode = replaceMetadataInHTML(htmlCode, key, prevValue, newValue);					
		}
		File htmlTarget = res.getFile().getParentFile();
  	htmlTarget = new File(htmlTarget, targetName+"_info.html"); //$NON-NLS-1$
  	htmlTarget = writeFile(htmlCode, htmlTarget);		
		return htmlTarget;
	}
	
  /**
   * Replaces metadata in HTML code based on current text in metadata fields.
   * @param htmlCode the HTML code 
   * @return the modified code
   */
	private String replaceMetadataInHTML(String htmlCode, String name, String prevValue, String newValue) {
		if (newValue==null || newValue.trim().equals("")) //$NON-NLS-1$
			return htmlCode;
		if (prevValue==null) {
			// write new line
			int n = htmlCode.indexOf("<meta name="); // start of first metadata tag found //$NON-NLS-1$
			if (n<0) n = htmlCode.indexOf("</head"); //$NON-NLS-1$
			if (n>-1) {
				String newCode = "<meta name=\""+name+"\" content=\""+newValue+"\">\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				htmlCode = htmlCode.substring(0, n)+newCode+htmlCode.substring(n, htmlCode.length());
			}
		}
		else if (!"".equals(newValue) && !newValue.equals(prevValue)) { //$NON-NLS-1$
			prevValue = "meta name=\""+name+"\" content=\""+prevValue+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			newValue = "meta name=\""+name+"\" content=\""+newValue+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			htmlCode = htmlCode.replace(prevValue, newValue);
		}
		return htmlCode;
	}
	
  /**
   * A JTextField for editing ZipResourceDialog fields.
   */
  protected static class EntryField extends JTextField {
  	
  	static Color defaultForeground = new JTextField().getForeground();
  	
  	EntryField() {
  		getDocument().putProperty("parent", this); //$NON-NLS-1$
      addFocusListener(focusListener);
      addActionListener(actionListener);
      getDocument().addDocumentListener(documentListener);
  	}
  	
  	EntryField(int width) {
  		super(width);
  		getDocument().putProperty("parent", this); //$NON-NLS-1$
      addFocusListener(focusListener);
      addActionListener(actionListener);
      getDocument().addDocumentListener(documentListener);
  	}
  	
  	public Dimension getPreferredSize() {
  		Dimension dim = super.getPreferredSize();
  		dim.width = Math.max(dim.width, 30);
  		dim.width = Math.min(dim.width, 100);
  		dim.width += 4;
  		return dim;
  	}
  	
  	protected String getDefaultText() {
  		return null;
  	}
  	
  	protected Color getEmptyForeground() {
  		return Color.gray;
  	}
  	
  	static DocumentListener documentListener = new DocumentListener() {   
      public void insertUpdate(DocumentEvent e) {
      	JTextComponent field = (JTextComponent)e.getDocument().getProperty("parent"); //$NON-NLS-1$
      	field.setBackground(Color.yellow);
      }
      public void removeUpdate(DocumentEvent e) {
      	JTextComponent field = (JTextComponent)e.getDocument().getProperty("parent"); //$NON-NLS-1$
      	field.setBackground(Color.yellow);
      }
			public void changedUpdate(DocumentEvent e) {}
  	};
  	
    static FocusListener focusListener = new FocusAdapter() {
      public void focusGained(FocusEvent e) {
      	EntryField field = (EntryField)e.getSource();
      	if (field.getDefaultText()!=null && field.getText().equals(field.getDefaultText())) {
      		field.setText(null);
	    		field.setForeground(defaultForeground);
      	}
      	field.selectAll();
	      field.setBackground(Color.white);
      }
      public void focusLost(FocusEvent e) {
      	EntryField field = (EntryField)e.getSource();
      	boolean fire = field.getBackground()==Color.yellow;
      	if (field.getDefaultText()!=null && "".equals(field.getText())) { //$NON-NLS-1$
      		field.setText(field.getDefaultText());
      		field.setForeground(field.getEmptyForeground());
      	}
	      field.setBackground(Color.white);
	      if (fire)	field.fireActionPerformed();
      }
    };

  	static ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	EntryField field = (EntryField)e.getSource();
        field.setBackground(Color.white);
      }
    };
    
  }
  

}
