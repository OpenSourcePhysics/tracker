/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2015  Douglas Brown
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
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.tools.FontSizer;

import java.io.File;

/**
 * A dialog for saving thumbnail images of a TrackerPanel.
 *
 * @author Douglas Brown
 */
public class ThumbnailDialog extends JDialog {
	
	protected static ThumbnailDialog thumbnailDialog; // singleton
	protected static String[] viewNames = new String[] {"WholeFrame", "MainView", "VideoOnly"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	protected static String[] formatNames = new String[] {"png", "jpg"}; //$NON-NLS-1$ //$NON-NLS-2$
  protected static Dimension defaultSize = new Dimension(320, 240);
  protected static VideoFileFilter[] fileFilters = new VideoFileFilter[formatNames.length];
  protected static PropertyChangeListener fileChooserListener;
  protected static JTextComponent chooserField;
  protected static boolean settingsOnly;

  // instance fields
  protected TrackerPanel trackerPanel;
  protected JButton saveAsButton, closeButton;
  protected JComponent sizePanel, viewPanel, formatPanel;
  protected JComboBox formatDropdown, viewDropdown, sizeDropdown;
  protected DefaultComboBoxModel formatModel, viewModel;
  protected AffineTransform transform = new AffineTransform();
  protected BufferedImage sizedImage;
  protected HashMap<Object, Dimension> sizes;
  protected Dimension fullSize = new Dimension(), thumbSize;
  protected boolean isRefreshing;
  protected String savedFilePath;
  protected JPanel buttonbar;
  
  /**
   * Returns the singleton ThumbnailDialog for a specified TrackerPanel.
   * 
   * @param panel the TrackerPanel
   * @param withSaveButton true to include the "Save As" button
   * @return the ThumbnailDialog
   */
  public static ThumbnailDialog getDialog(TrackerPanel panel, boolean withSaveButton) {
  	// create singleton if it does not yet exist
  	if (thumbnailDialog==null) {
  		thumbnailDialog = new ThumbnailDialog(panel);
  		fileChooserListener = new PropertyChangeListener() {
 			  public void propertyChange(PropertyChangeEvent e) {
 			  	if (chooserField!=null) {
	 			  	VideoFileFilter filter = (VideoFileFilter)e.getNewValue();
			  		final String ext = filter.getDefaultExtension();
	 			    Runnable runner = new Runnable() {
	 			      public void run() {
	 			        String name = XML.stripExtension(chooserField.getText())+"."+ext; //$NON-NLS-1$ 
	 			        chooserField.setText(name);
	 			      }
	 			    };
	 			    SwingUtilities.invokeLater(runner);
 			  	}
 			  }
 			};
 			// set up file filters
  		for (int i=0; i<formatNames.length; i++) {
		    VideoType type = VideoIO.getVideoType(null, formatNames[i]);
		    for (VideoFileFilter filter: type.getFileFilters()) {
		    	if (filter.getDefaultExtension().equals(formatNames[i])) {
		    		fileFilters[i] = filter;
		    		break;
		    	}
		    }
  		}
      // set up chooser field
    	JFileChooser chooser = TrackerIO.getChooser();
      String temp = "untitled.tmp"; //$NON-NLS-1$
      chooser.setSelectedFile(new File(temp));
      chooserField = getTextComponent(chooser, temp);
      chooser.setSelectedFile(new File("")); //$NON-NLS-1$      
  	}
  	
  	settingsOnly = !withSaveButton;
  	if (thumbnailDialog.trackerPanel != panel) {
	  	thumbnailDialog.trackerPanel = panel;
  	}
  	thumbnailDialog.refreshGUI();
  	return thumbnailDialog;
  }
  
  /**
   * Saves a thumbnail image of the current frame using the current dropdown choices.
   * 
   * @param filePath the path to the target image file (may be null)
   * @return the saved image file, or null if cancelled or failed
   */
  public File saveThumbnail(String filePath) {
  	int i = formatDropdown.getSelectedIndex();
    String format = formatNames[i];
  	if (filePath==null) {
    	JFileChooser chooser = TrackerIO.getChooser();
	    chooser.setAcceptAllFileFilterUsed(false);	    
	    for (FileFilter filter: chooser.getChoosableFileFilters())
	    	chooser.removeChoosableFileFilter(filter);
	    for (VideoFileFilter filter: fileFilters) {
	    	chooser.addChoosableFileFilter(filter);
	    }
	    chooser.setFileFilter(fileFilters[i]);
	    chooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, fileChooserListener);
      chooser.setDialogTitle(TrackerRes.getString("ThumbnailDialog.Chooser.SaveThumbnail.Title")); //$NON-NLS-1$
      String tabName = XML.stripExtension(trackerPanel.getTitle());
      chooser.setSelectedFile(new File(tabName+"_thumbnail."+format));  //$NON-NLS-1$
      File[] files = TrackerIO.getChooserFiles("save"); //$NON-NLS-1$
	    chooser.removePropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, fileChooserListener);
      if (files==null || files.length==0)
      	return null;
      filePath = files[0].getAbsolutePath();
      VideoFileFilter selectedFilter = (VideoFileFilter)chooser.getFileFilter();
      String ext = selectedFilter.getDefaultExtension();
    	setFormat(ext);
      if (!selectedFilter.accept(files[0])) {
      	filePath = XML.stripExtension(filePath)+"."+ext; //$NON-NLS-1$
      	if (!TrackerIO.canWrite(new File(filePath))) return null;
      }
  	}
  	if (XML.getExtension(filePath)==null)
  		filePath = XML.stripExtension(filePath)+"."+format; //$NON-NLS-1$
    Dimension size = sizes.get(sizeDropdown.getSelectedItem());
    BufferedImage thumb = getThumbnailImage(size);
	  File thumbnail = VideoIO.writeImageFile(thumb, filePath);
    return thumbnail;
  }
  
  /**
   * Gets a thumbnail image of the TrackerPanel.
   * 
   * @return a BufferedImage
   */
  public BufferedImage getThumbnail() {
    Dimension size = sizes.get(sizeDropdown.getSelectedItem());
		return getThumbnailImage(size);
  }
  
  /**
   * Sets the image format (filename extension).
   *
   * @param format "png" or "jpg"
   */
  public void setFormat(String format) {
  	for (int i=0; i<formatNames.length; i++) {
  		if (format!=null && formatNames[i].equals(format.toLowerCase())) {
  			formatDropdown.setSelectedIndex(i);
  			break;
  		}
  	}
  }
  
  /**
   * Gets the image format (filename extension).
   *
   * @return "png" or "jpg"
   */
  public String getFormat() {
  	return formatNames[formatDropdown.getSelectedIndex()];
  }
  
//_____________________ private constructor and methods ____________________________

  /**
   * Constructs a ThumbnailDialog.
   *
   * @param panel a TrackerPanel to supply the images
   */
  private ThumbnailDialog(TrackerPanel panel) {
    super(panel.getTFrame(), true);
    trackerPanel = panel;
    setResizable(false);
    createGUI();
    refreshGUI();
    // center dialog on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width - getBounds().width) / 2;
    int y = (dim.height - getBounds().height) / 2;
    setLocation(x, y);
  }

  /**
   * Creates the visible components of this dialog.
   */
  private void createGUI() {
    JPanel contentPane = new JPanel(new BorderLayout());
    setContentPane(contentPane);
    
    Box settingsPanel = Box.createVerticalBox();
    settingsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 2));
    contentPane.add(settingsPanel, BorderLayout.CENTER);
    JPanel upper = new JPanel(new GridLayout(1, 1));
    JPanel lower = new JPanel(new GridLayout(1, 2));
    
    // size panel
    sizes = new HashMap<Object, Dimension>();
    sizePanel = Box.createVerticalBox();
    sizeDropdown = new JComboBox();
  	sizePanel.add(sizeDropdown);
    
    // view panel
    viewPanel = Box.createVerticalBox();
    viewModel = new DefaultComboBoxModel();
    viewDropdown = new JComboBox(viewModel);
    viewPanel.add(viewDropdown);
    viewDropdown.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		if (e.getStateChange()==ItemEvent.SELECTED) {
    			if (!isRefreshing)
    				refreshSizeDropdown();
    		}
    	}
    });
   
    // format panel
    formatPanel = Box.createVerticalBox();
    formatModel = new DefaultComboBoxModel();
    formatDropdown = new JComboBox(formatModel);
    formatPanel.add(formatDropdown);
    
    // assemble 
    settingsPanel.add(upper);
    settingsPanel.add(lower);
    upper.add(viewPanel);
    lower.add(sizePanel);
    lower.add(formatPanel);
    
    // buttons
    saveAsButton = new JButton();
    saveAsButton.setForeground(new Color(0, 0, 102));
    saveAsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        saveThumbnail(null);
      }
    });
    closeButton = new JButton();
    closeButton.setForeground(new Color(0, 0, 102));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        firePropertyChange("accepted", null, null); //$NON-NLS-1$
      }
    });
    // buttonbar
    buttonbar = new JPanel();
    contentPane.add(buttonbar, BorderLayout.SOUTH);
    buttonbar.add(saveAsButton);
    buttonbar.add(closeButton);    
  }
  
  /**
   * Refreshes the visible components of this dialog.
   */
  protected void refreshGUI() {
  	// refresh strings
  	String resource = settingsOnly? "ThumbnailDialog.Settings.Title": "ThumbnailDialog.Title"; //$NON-NLS-1$ //$NON-NLS-2$
  	String title = TrackerRes.getString(resource);
  	setTitle(title);
  	
  	// subpanel titled borders
  	title = TrackerRes.getString("ExportVideoDialog.Subtitle.Size"); //$NON-NLS-1$
    Border space = BorderFactory.createEmptyBorder(0, 4, 6, 4);
    Border titled = BorderFactory.createTitledBorder(title);
  	int fontLevel = FontSizer.getLevel();
    FontSizer.setFonts(titled, fontLevel);
    sizePanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
    title = TrackerRes.getString("ThumbnailDialog.Subtitle.Image"); //$NON-NLS-1$
    titled = BorderFactory.createTitledBorder(title);
    FontSizer.setFonts(titled, fontLevel);
    viewPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
    title = TrackerRes.getString("ExportVideoDialog.Subtitle.Format"); //$NON-NLS-1$
    titled = BorderFactory.createTitledBorder(title);
    FontSizer.setFonts(titled, fontLevel);
    formatPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
    
    // buttons
    saveAsButton.setText(TrackerRes.getString("ExportVideoDialog.Button.SaveAs")); //$NON-NLS-1$
  	if (settingsOnly) {
  		buttonbar.remove(saveAsButton);
	    closeButton.setText(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
  	}
  	else {
  		buttonbar.add(saveAsButton, 0);  		
	    closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
  	}
    // dropdowns
  	isRefreshing = true;  	
  	int index = formatDropdown.getSelectedIndex();
  	index = Math.max(index, 0);
  	formatModel.removeAllElements();
    for (int i=0; i< formatNames.length; i++) {
    	String format = TrackerRes.getString("ThumbnailDialog.Format."+formatNames[i].toUpperCase()); //$NON-NLS-1$
    	formatModel.addElement(format);
    }
    formatDropdown.setSelectedIndex(index);  	
  	int lastIndex = trackerPanel.getVideo()==null? viewNames.length-2: viewNames.length-1;
  	index = Math.min(viewDropdown.getSelectedIndex(), lastIndex);
  	index = Math.max(index, 0);
  	viewModel.removeAllElements();
    for (int i=0; i<=lastIndex; i++) {
    	String view = TrackerRes.getString("ThumbnailDialog.View."+viewNames[i]); //$NON-NLS-1$
    	viewModel.addElement(view);
    } 
    viewDropdown.setSelectedIndex(index); 
  	isRefreshing = false;
		refreshSizeDropdown();

		pack();
  }
  
  /**
   * Refreshes the size dropdown.
   */
  private void refreshSizeDropdown() {
  	isRefreshing = true;
  	Object selectedItem = sizeDropdown.getSelectedItem();
  	sizeDropdown.removeAllItems();
  	sizes.clear();
		switch(viewDropdown.getSelectedIndex()) {
			case 1: // video and graphics
		  	Rectangle bounds = trackerPanel.getMat().mat;
				fullSize.setSize(bounds.getWidth(), bounds.getHeight());
		  	thumbSize = getFullThumbnailSize(fullSize);
				break;
			case 2: // video only
				BufferedImage image = trackerPanel.getVideo().getImage();
				fullSize.setSize(image.getWidth(), image.getHeight());
				thumbSize = getFullThumbnailSize(fullSize);
				break;
			default: // entire frame
				fullSize.setSize(trackerPanel.getTFrame().getSize());
				thumbSize = getFullThumbnailSize(fullSize);
		}
		// add full-size item
		if (isAcceptedDimension(fullSize.width, fullSize.height)) {
  		String s = fullSize.width+"x"+fullSize.height; //$NON-NLS-1$
  		sizeDropdown.addItem(s);
  		sizes.put(s, fullSize);
		}		
		// add half-size item
		Dimension dim = new Dimension(fullSize.width/2, fullSize.height/2);
		if (dim.width>thumbSize.width && dim.height>thumbSize.height
			&& isAcceptedDimension(dim.width, dim.height)) {
  		String s = dim.width+"x"+dim.height; //$NON-NLS-1$
  		sizeDropdown.addItem(s);
  		sizes.put(s, dim);
		}
		// add "full-thumb-size" item and make it the default
		String s = thumbSize.width+"x"+thumbSize.height; //$NON-NLS-1$
		Object defaultItem = s;
		sizeDropdown.addItem(s);
		sizes.put(s, thumbSize);
		
		// add additional sizes if acceptable
		double[] factor = new double[] {0.75, 0.5, 0.375, 0.25};
		for (int i=0; i<factor.length; i++) {
			dim = new Dimension((int)(thumbSize.width*factor[i]), (int)(thumbSize.height*factor[i]));
			if (isAcceptedDimension(dim.width, dim.height)) {
	  		s = dim.width+"x"+dim.height; //$NON-NLS-1$
	  		sizeDropdown.addItem(s);
	  		sizes.put(s, dim);
			}
		}
		// select previous or default size
		sizeDropdown.setSelectedItem(sizes.keySet().contains(selectedItem)? selectedItem: defaultItem);
  	isRefreshing = false;
  }

  /**
   * Gets the "full-sized" thumbnail dimension for a specified image size.
   * 
   * @param w the desired width
   * @param h the desired height
   * @return an acceptable dimension
   */
  private Dimension getFullThumbnailSize(Dimension imageSize) {
  	// determine image resize factor
    double widthFactor = defaultSize.getWidth()/imageSize.width;
    double heightFactor = defaultSize.getHeight()/imageSize.height;
    double factor = Math.min(widthFactor, heightFactor);

    // determine default dimensions of thumbnail image
    int w = (int)(imageSize.width*factor);
    int h = (int)(imageSize.height*factor);
  	
    // return default size
    return new Dimension(w, h);
  }
  
  /**
   * Determines if a width and height are acceptable.
   * Returns true if height greater than 60 or width greater than 80.
   * 
   * @param w the width
   * @param h the height
   * @return true if accepted
   */
  private boolean isAcceptedDimension(int w, int h) {
  	if (w>=80 || h>=60) return true;
  	return false;
  }
  
  /**
   * Gets an image of a specified size from the TrackerPanel.
   * The view dropdown determines which component is rendered.
   * 
   * @param size the size
   * @return a BufferedImage
   */
  private BufferedImage getThumbnailImage(Dimension size) {
  	BufferedImage rawImage;
		switch(viewDropdown.getSelectedIndex()) {
			case 1: // video and graphics
		  	rawImage = trackerPanel.renderMat();
		  	break;
			case 2: // video only
	  		rawImage = trackerPanel.getVideo().getImage();
		  	break;
			default: // entire frame
		    TFrame frame = trackerPanel.getTFrame();
		  	rawImage = (BufferedImage)frame.createImage(fullSize.width, fullSize.height);
		    Graphics2D g2 = rawImage.createGraphics();
		    frame.paint(g2);
		    g2.dispose();
		}
		return getResizedImage(rawImage, size);
  }
  
  /**
   * Resizes a source image and returns the resized image.
   * 
   * @param source the source image
   * @param size the desired size
   * @return a BufferedImage
   */
  private BufferedImage getResizedImage(BufferedImage source, Dimension size) {
  	if (size.width==source.getWidth() && size.height==source.getHeight())
  	  return source;
  	if (sizedImage==null
  			|| sizedImage.getWidth()!=size.width 
  			|| sizedImage.getHeight()!=size.height)	{
  		sizedImage = new BufferedImage(size.width, size.height, source.getType());
  	}
    Graphics2D g2 = sizedImage.createGraphics();
//    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
    g2.drawImage(source, 0, 0, size.width, size.height, 
    		0, 0, source.getWidth(), source.getHeight(), null);
    g2.dispose();
	  return sizedImage;
  }
  
  private static JTextComponent getTextComponent(Container c, String toMatch) {
    Component[] comps = c.getComponents();
    for(int i = 0; i<comps.length; i++) {
      if((comps[i] instanceof JTextComponent)&&toMatch.equals(((JTextComponent) comps[i]).getText())) {
        return(JTextComponent) comps[i];
      }
      if(comps[i] instanceof Container) {
        JTextComponent tc = getTextComponent((Container) comps[i], toMatch);
        if(tc!=null) {
          return tc;
        }
      }
    }
    return null;
  }

}
