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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.DeinterlaceFilter;
import org.opensourcephysics.media.core.ImageVideoRecorder;
import org.opensourcephysics.media.core.MediaRes;
import org.opensourcephysics.media.core.ScratchVideoRecorder;
import org.opensourcephysics.media.core.Video;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.media.core.VideoRecorder;
import org.opensourcephysics.media.core.VideoType;
import org.opensourcephysics.tools.FontSizer;

/**
 * A dialog for exporting videos from a TrackerPanel.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class ExportVideoDialog extends JDialog {

	protected static final String PROPERTY_EXPORTVIDEO_VIDEOSAVED = "video_saved";
	protected static final String PROPERTY_EXPORTVIDEO_VIDEOCANCELED = "video_cancelled";
	// instance fields
	protected TFrame frame;
	protected Integer panelID;
	protected JButton saveAsButton, closeButton;
	protected JComponent sizePanel, viewPanel, contentPanel, formatPanel;
	protected JComboBox<String> formatDropdown, viewDropdown, sizeDropdown, contentDropdown;
	protected JLabel clipPropertiesLabel;
	protected AffineTransform transform = new AffineTransform();
	protected BufferedImage sizedImage;
	protected HashMap<Object, JComponent> views;
	protected HashMap<Object, Dimension> sizes;
	protected Dimension fullSize;
	protected boolean isRefreshing;
	protected int mainViewContentIndex, worldViewContentIndex; // for refreshing
	protected String savedFilePath;
	protected PropertyChangeListener listener;
	protected boolean oddFirst = true;
	protected Object prevContentItem;
	protected static ExportVideoDialog videoExporter; // singleton

	/**
	 * Returns the singleton ExportVideoDialog for a specified TrackerPanel.
	 * 
	 * @param panel the TrackerPanel
	 * @return the ExportVideoDialog
	 */
	public static ExportVideoDialog getVideoDialog(TrackerPanel panel) {
		// refresh formats before instantiating
		TrackerIO.refreshVideoFormats();

		if (videoExporter == null) {
			videoExporter = new ExportVideoDialog(panel);
		}

		// refresh format dropdown
		videoExporter.refreshFormatDropdown(VideoIO.getPreferredExportExtension());
		videoExporter.setTrackerPanel(panel);
		videoExporter.setFontLevel(FontSizer.getLevel());
		return videoExporter;
	}

	protected void setFormat(String format) {
		if (format != null) {
			formatDropdown.setSelectedItem(format);
			TrackerIO.selectedVideoFormat = format;
		}
	}

	/**
	 * Gets the currently selected video format (description).
	 * 
	 * @return the format
	 */
	public Object getFormat() {
		return formatDropdown.getSelectedItem();
	}

	protected String exportFullSizeVideo(String filePath, String trkPath) {
		if (frame.getTrackerPanelForID(panelID).getVideo() == null) {
			return null;
		}
		// set dropdowns to Main View, Video only, full size
		viewDropdown.setSelectedIndex(0);
		try {
			contentDropdown.setSelectedIndex(1);
		} catch (Exception e) {
			contentDropdown.setSelectedIndex(0);
		}
		sizeDropdown.setSelectedIndex(0);
		// render
		VideoType videoType = TrackerIO.videoFormats.get(formatDropdown.getSelectedItem());
		Dimension size = sizes.get(sizeDropdown.getSelectedItem());
		render(videoType, size, false, filePath, trkPath);
		return savedFilePath;
	}

	/**
	 * Constructs a ExportVideoDialog.
	 *
	 * @param panel a TrackerPanel to supply the images
	 */
	private ExportVideoDialog(TrackerPanel panel) {
		super(panel.getTFrame(), true);
		frame = panel.getTFrame();
		panelID = panel.getID();
		setResizable(false);
		createGUI();
		refreshGUI();
		// center dialog on the screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - getBounds().width) / 2;
		int y = (dim.height - getBounds().height) / 2;
		setLocation(x, y);
	}

//_____________________________ private methods ____________________________

	/**
	 * Creates the visible components of this dialog.
	 */
	private void createGUI() {
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		Box settingsPanel = Box.createVerticalBox();
		contentPane.add(settingsPanel, BorderLayout.CENTER);
		JPanel upper = new JPanel(new GridLayout(1, 2));
		JPanel lower = new JPanel(new GridLayout(1, 2));

		// size panel
		sizes = new HashMap<Object, Dimension>();
		sizePanel = Box.createVerticalBox();
		sizeDropdown = new JComboBox<String>();
		sizeDropdown.setName("ExportVideo.size");
		sizePanel.add(sizeDropdown);

		// view panel
		views = new HashMap<Object, JComponent>();
		viewPanel = new JPanel(new GridLayout(0, 1));
		viewDropdown = new JComboBox<>();
		viewDropdown.setName("ExportVideo.view");
		viewPanel.add(viewDropdown);
		viewDropdown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (!isRefreshing)
						refreshDropdowns();
				}
			}
		});

		// content panel
		contentPanel = new JPanel(new GridLayout(0, 1));
		contentDropdown = new JComboBox<>();
		contentDropdown.setName("ExportVideo.content");
		contentPanel.add(contentDropdown);
		contentDropdown.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (isRefreshing)
					return;
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					prevContentItem = e.getItem();
				}
				if (e.getStateChange() == ItemEvent.SELECTED) {
					JComponent view = views.get(viewDropdown.getSelectedItem());
					if (view == frame.getTrackerPanelForID(panelID))
						mainViewContentIndex = contentDropdown.getSelectedIndex();
					else if (view instanceof WorldTView)
						worldViewContentIndex = contentDropdown.getSelectedIndex();
					if (contentDropdown.getSelectedIndex() == 3) {
						JRadioButton odd = new JRadioButton(
								TrackerRes.getString("ExportVideoDialog.Deinterlace.OddFirst")); //$NON-NLS-1$
						odd.setSelected(oddFirst);
						JRadioButton even = new JRadioButton(
								TrackerRes.getString("ExportVideoDialog.Deinterlace.EvenFirst")); //$NON-NLS-1$
						even.setSelected(!oddFirst);
						ButtonGroup group = new ButtonGroup();
						group.add(odd);
						group.add(even);
						JPanel panel = new JPanel();
						panel.add(odd);
						panel.add(even);
						int result = JOptionPane.showConfirmDialog(ExportVideoDialog.this, panel,
								TrackerRes.getString("ExportVideoDialog.Deinterlace.Dialog.Title"), //$NON-NLS-1$
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						if (result == JOptionPane.CANCEL_OPTION && prevContentItem != null) {
							contentDropdown.setSelectedItem(prevContentItem);
							prevContentItem = null;
							return;
						}
						oddFirst = odd.isSelected();
					}
					refreshDropdowns();
				}
			}
		});

		// format panel
		formatPanel = new JPanel(new GridLayout(0, 1));
		formatDropdown = new JComboBox<>();
		formatDropdown.setName("ExportVideo.format");
		formatDropdown.setMaximumRowCount(TrackerIO.videoFormatDescriptions.size());

		formatPanel.add(formatDropdown);

		// assemble
		settingsPanel.add(upper);
		settingsPanel.add(lower);
		upper.add(viewPanel);
		upper.add(contentPanel);
		lower.add(sizePanel);
		lower.add(formatPanel);

		// buttons
		saveAsButton = new JButton();
		saveAsButton.setForeground(new Color(0, 0, 102));
		saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VideoType format = TrackerIO.videoFormats.get(formatDropdown.getSelectedItem());
				Dimension size = sizes.get(sizeDropdown.getSelectedItem());
				render(format, size, !OSPRuntime.isJS, null, null);
				TrackerIO.selectedVideoFormat = (String) formatDropdown.getSelectedItem();
			}
		});
		closeButton = new JButton();
		closeButton.setForeground(new Color(0, 0, 102));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		// buttonbar
		JPanel buttonbar = new JPanel();
		contentPane.add(buttonbar, BorderLayout.SOUTH);
		buttonbar.add(saveAsButton);
		buttonbar.add(closeButton);

		// clip properties label
		clipPropertiesLabel = new JLabel();
		clipPropertiesLabel.setHorizontalAlignment(JLabel.CENTER);
		clipPropertiesLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 2, 8));
		contentPane.add(clipPropertiesLabel, BorderLayout.NORTH);
	}

	/**
	 * Refreshes the visible components of this dialog.
	 */
	private void refreshGUI() {
		// refresh strings
		String title = TrackerRes.getString("ExportVideoDialog.Title"); //$NON-NLS-1$
		TrackerPanel panel = frame.getTrackerPanelForID(panelID);
		setTitle(title);
		// clip settings
		VideoClip clip = panel.getPlayer().getClipControl().getVideoClip();
		String framecount = MediaRes.getString("Filter.Sum.Label.FrameCount").toLowerCase(); //$NON-NLS-1$
		if (framecount.endsWith(":")) //$NON-NLS-1$
			framecount = framecount.substring(0, framecount.length() - 1);
		String startframe = MediaRes.getString("ClipInspector.Label.StartFrame").toLowerCase(); //$NON-NLS-1$
		if (startframe.endsWith(":")) //$NON-NLS-1$
			startframe = startframe.substring(0, startframe.length() - 1);
		String stepsize = MediaRes.getString("ClipInspector.Label.StepSize").toLowerCase(); //$NON-NLS-1$
		if (stepsize.endsWith(":")) //$NON-NLS-1$
			stepsize = stepsize.substring(0, stepsize.length() - 1);
		title = TrackerRes.getString("ExportVideoDialog.Label.ClipSettings") + ": " //$NON-NLS-1$ //$NON-NLS-2$
				+ framecount + " " + clip.getStepCount() + ", " //$NON-NLS-1$ //$NON-NLS-2$
				+ startframe + " " + clip.getStartFrameNumber() + ", " //$NON-NLS-1$ //$NON-NLS-2$
				+ stepsize + " " + clip.getStepSize(); //$NON-NLS-1$
		clipPropertiesLabel.setText(title);
		// subpanel titled borders
		title = TrackerRes.getString("ExportVideoDialog.Subtitle.Size"); //$NON-NLS-1$
		Border space = BorderFactory.createEmptyBorder(0, 4, 6, 4);
		Border titled = BorderFactory.createTitledBorder(title);
		int fontLevel = FontSizer.getLevel();
		FontSizer.setFonts(titled, fontLevel);
		sizePanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		title = TrackerRes.getString("ExportVideoDialog.Subtitle.View"); //$NON-NLS-1$
		titled = BorderFactory.createTitledBorder(title);
		FontSizer.setFonts(titled, fontLevel);
		viewPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		title = TrackerRes.getString("ExportVideoDialog.Subtitle.Content"); //$NON-NLS-1$
		titled = BorderFactory.createTitledBorder(title);
		FontSizer.setFonts(titled, fontLevel);
		contentPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		title = TrackerRes.getString("ExportVideoDialog.Subtitle.Format"); //$NON-NLS-1$
		titled = BorderFactory.createTitledBorder(title);
		FontSizer.setFonts(titled, fontLevel);
		formatPanel.setBorder(BorderFactory.createCompoundBorder(titled, space));
		// buttons
		saveAsButton.setText(TrackerRes.getString("ExportVideoDialog.Button.SaveAs")); //$NON-NLS-1$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$

		// refresh view dropdown
		Object selectedView = viewDropdown.getSelectedItem();
		viewDropdown.removeAllItems();
		// add trackerPanel view
		String s = TrackerRes.getString("TFrame.View.Main"); //$NON-NLS-1$
		s += " (0)"; //$NON-NLS-1$
		views.put(s, panel);
		viewDropdown.addItem(s);
		// add additional open views
		TViewChooser[] choosers = frame.getVisibleChoosers(panelID);
		for (int i = 0; i < choosers.length; i++) {
			if (choosers[i] != null) {
				String number = " (" + (i + 1) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				TView tview = choosers[i].getSelectedView();
				if (tview != null && tview.getViewType() == TView.VIEW_WORLD) {
					s = tview.getViewName() + number;
					WorldTView worldView = (WorldTView) tview;
					views.put(s, worldView);
					viewDropdown.addItem(s);
				} else if (tview != null && tview.getViewType() == TView.VIEW_PLOT) {
					s = tview.getViewName() + number;
					PlotTView plotView = (PlotTView) tview;
					TTrack track = plotView.getSelectedTrack();
					if (track != null) {
						PlotTrackView trackView = (PlotTrackView) plotView.getTrackView(track);
						views.put(s, trackView);
						viewDropdown.addItem(s);
					}
				}
			}
//        else {
//          s = TrackerRes.getString("TFrame.View.Unknown")+number; //$NON-NLS-1$
//          views.put(s, c[i]);
//        	viewDropdown.addItem(s);
//        }
		}
		// add tab view
		s = TrackerRes.getString("TMenuBar.MenuItem.CopyFrame"); //$NON-NLS-1$
		views.put(s, (JComponent) frame.getContentPane());
		viewDropdown.addItem(s);
		if (selectedView != null)
			viewDropdown.setSelectedItem(selectedView);

		pack();
		refreshDropdowns();
	}

	/**
	 * Refreshes the size and content dropdowns.
	 */
	private void refreshDropdowns() {
		isRefreshing = true;
		JComponent view = views.get(viewDropdown.getSelectedItem());
		TrackerPanel panel = frame.getTrackerPanelForID(panelID);
		// refresh content dropdown
		Video video = panel.getVideo();
		String s = null;
		contentDropdown.removeAllItems();
		if (view == panel) {
			if (video != null) {
				s = TrackerRes.getString("ExportVideoDialog.Content.VideoAndGraphics"); //$NON-NLS-1$
				contentDropdown.addItem(s);
				s = TrackerRes.getString("ExportVideoDialog.Content.VideoOnly"); //$NON-NLS-1$
				contentDropdown.addItem(s);
			}
			s = TrackerRes.getString("ExportVideoDialog.Content.GraphicsOnly"); //$NON-NLS-1$
			contentDropdown.addItem(s);
			if (video != null) {
				if (panel.getPlayer().getClipControl().getVideoClip().getStepCount() > 1) {
					s = TrackerRes.getString("ExportVideoDialog.Content.DeinterlacedVideo"); //$NON-NLS-1$
					contentDropdown.addItem(s);
				}
				contentDropdown.setSelectedIndex(mainViewContentIndex);
			}
		} else if (view instanceof WorldTView) {
			if (video != null) {
				s = TrackerRes.getString("ExportVideoDialog.Content.VideoAndGraphics"); //$NON-NLS-1$
				contentDropdown.addItem(s);
			}
			s = TrackerRes.getString("ExportVideoDialog.Content.GraphicsOnly"); //$NON-NLS-1$
			contentDropdown.addItem(s);
			if (video != null)
				contentDropdown.setSelectedIndex(worldViewContentIndex);
		} else {
			s = TrackerRes.getString("ExportVideoDialog.Content.GraphicsOnly"); //$NON-NLS-1$
			contentDropdown.addItem(s);
		}

		// refresh size dropdown
		Object selectedItem = sizeDropdown.getSelectedItem();
		sizeDropdown.removeAllItems();
		if (view == panel) {
			int contentIndex = contentDropdown.getSelectedIndex();
			if (contentIndex == 1 || contentIndex == 3) { // video only or deinterlaced/filtered video
				Dimension d = panel.getVideo().getImageSize(true);
				int w = d.width;
				int h = d.height;
				fullSize = getAcceptedDimension(w, h);
				s = fullSize.width + "x" + fullSize.height; //$NON-NLS-1$
				s += " (" + TrackerRes.getString("ExportVideoDialog.VideoSize") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sizeDropdown.addItem(s);
				sizes.put(s, fullSize);
			} else { // includes graphics
				Rectangle bounds = panel.getMatBounds();
				fullSize = getAcceptedDimension(bounds.width, bounds.height);
				s = fullSize.width + "x" + fullSize.height; //$NON-NLS-1$

				s += " (" + TrackerRes.getString("ExportVideoDialog.MatSize") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				sizeDropdown.addItem(s);
				sizes.put(s, fullSize);
			}
			// add additional sizes if acceptable
			Dimension dim = new Dimension(fullSize.width * 8 / 10, fullSize.height * 8 / 10);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width * 3 / 4, fullSize.height * 3 / 4);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width * 6 / 10, fullSize.height * 6 / 10);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width / 2, fullSize.height / 2);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width * 4 / 10, fullSize.height * 4 / 10);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width * 3 / 8, fullSize.height * 3 / 8);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width * 3 / 10, fullSize.height * 3 / 10);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width / 4, fullSize.height / 4);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
			dim = new Dimension(fullSize.width * 2 / 10, fullSize.height * 2 / 10);
			if (isAcceptedDimension(dim.width, dim.height)) {
				s = dim.width + "x" + dim.height; //$NON-NLS-1$
				sizeDropdown.addItem(s);
				sizes.put(s, dim);
			}
		} else if (view instanceof PlotTrackView) {
			Dimension dim = ((PlotTrackView) view).getPanelSize();
			fullSize = getAcceptedDimension(dim.width, dim.height);
			s = fullSize.width + "x" + fullSize.height; //$NON-NLS-1$
			sizeDropdown.addItem(s);
			sizes.put(s, fullSize);
		} else {
			Dimension dim = view.getSize();
			fullSize = getAcceptedDimension(dim.width, dim.height);
			s = fullSize.width + "x" + fullSize.height; //$NON-NLS-1$
			sizeDropdown.addItem(s);
			sizes.put(s, fullSize);
		}
		if (sizes.keySet().contains(selectedItem))
			sizeDropdown.setSelectedItem(selectedItem);
		isRefreshing = false;
	}

	/**
	 * Refreshes the format dropdown.
	 * 
	 * @param preferredExtension the preferred video file extension
	 */
	public void refreshFormatDropdown(String preferredExtension) {
		String selected = TrackerIO.getVideoFormat(preferredExtension);
		formatDropdown.removeAllItems();
		for (String format : TrackerIO.videoFormatDescriptions) {
			formatDropdown.addItem(format);
		}
		if (selected != null)
			setFormat(selected);
	}

	/**
	 * Sets the font level.
	 *
	 * @param level the desired font level
	 */
	public void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		// refresh the dropdowns
		JComboBox<?>[] dropdowns = new JComboBox<?>[] { formatDropdown, viewDropdown, sizeDropdown, contentDropdown };
		for (JComboBox<?> cb : dropdowns) {
			@SuppressWarnings("unchecked")
			JComboBox<String> next = (JComboBox<String>) cb;
			int n = next.getSelectedIndex();
			String[] items = new String[next.getItemCount()];
			for (int i = 0; i < items.length; i++) {
				items[i] = next.getItemAt(i);
			}
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(items);
			next.setModel(model);
			next.setSelectedIndex(n);
		}
		refreshGUI();
		pack();
	}

	public void setTrackerPanel(TrackerPanel panel) {
		frame = panel.getTFrame();
		panelID = panel.getID();
		refreshGUI();
	}

	/**
	 * Gets the smallest acceptable dimension >= a specified width and height. This
	 * is a work-around to avoid image artifacts introduced by the converter in
	 * xuggle.
	 * 
	 * @param w the desired width
	 * @param h the desired height
	 * @return an acceptable dimension
	 */
	private Dimension getAcceptedDimension(int w, int h) {
		// adjust both dimensions upward to mod 16 if needed
		if (!isAcceptedDimension(w, h)) {
			while (w % 16 != 0)
				w++;
			while (h % 16 != 0)
				h++;
		}
		return new Dimension(w, h);
	}

	/**
	 * Determines if a width and height are acceptable (for xuggle).
	 * 
	 * @param w the width
	 * @param h the height
	 * @return true if accepted
	 */
	private boolean isAcceptedDimension(int w, int h) {
		if (w < 160 || h < 120)
			return false;
		if (w % 4 != 0 || h % 4 != 0)
			return false;
		if (1.0 * h / w == .75)
			return true; // 4:3 aspect ratio
		if (1.0 * w / h == 1.5)
			return true; // 3:2 aspect ratio
		if (16.0 * h / w == 9.0)
			return true; // 16:9 aspect ratio
		if (w % 16 == 0 && h % 16 == 0)
			return true; // any dimensions that are mod 16
		return false;
	}

	/**
	 * Controls the visibility of the video.
	 * 
	 * @param visible true to show the video
	 */
	private void setVideoVisible(boolean visible) {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		trackerPanel.setVideoVisible(visible);
	}

	/**
	 * Renders a video of the entire current clip. The video format and size are
	 * specified in the constructor. The completed video is saved to a file chosen
	 * by the user.
	 * 
	 * @param videoType         the format
	 * @param size           the size
	 * @param showOpenDialog true to display a dialog offering to open the newly
	 *                       saved video
	 * @param trkPath 
	 */
	private void render(VideoType videoType, Dimension size, boolean showOpenDialog, String filePath, String trkPath) {
		setVisible(false);
		savedFilePath = null;
		// prepare selected view to produce desired images
		TrackerPanel panel = frame.getTrackerPanelForID(panelID);
		Video video = panel.getVideo();
		boolean videoIsVisible = video != null && video.isVisible();
		double magnification = panel.getMagnification();
		JComponent view = views.get(viewDropdown.getSelectedItem());
		if (view == panel && contentDropdown.getSelectedIndex() != 1) { // includes graphics
			// change magnification if needed
			double zoom = size.getWidth() / fullSize.getWidth();
			if (zoom != magnification) {
				panel.setMagnification(zoom);
			}
			// hide/show the video if needed
			if (contentDropdown.getSelectedIndex() == 0) // graphics and video
				setVideoVisible(true);
			else if (contentDropdown.getSelectedIndex() == 2) // graphics only
				setVideoVisible(false);
		} else if (view instanceof WorldTView) {
			// hide/show the video per content dropdown
			setVideoVisible(contentDropdown.getSelectedIndex() == 0);
		} else if (view instanceof PlotTrackView) {
			// check that all plots are visible
			PlotTrackView trackView = (PlotTrackView) view;
			Dimension extent = trackView.getViewport().getExtentSize();
			Dimension full = trackView.getViewport().getView().getSize();
			if (!extent.equals(full)) {
				JOptionPane.showMessageDialog(panel,
						TrackerRes.getString("ExportVideo.Dialog.HiddenPlots.Message"), //$NON-NLS-1$
						TrackerRes.getString("ExportVideo.Dialog.HiddenPlots.Title"), //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
				setVisible(true);
				return;
			}

		}
		// prepare the player, etc
		VideoPlayer player = panel.getPlayer();
		player.stop();
		player.setEnabled(false);
		ClipControl playControl = player.getClipControl();
		VideoClip clip = playControl.getVideoClip();
		int taskLength = clip.getStepCount() + 1; // for monitor
		VideoRecorder recorder = videoType.getRecorder();
		double duration = player.getMeanStepDuration();
		if (contentDropdown.getSelectedIndex() == 3)
			duration = duration / 2;
		recorder.setFrameDuration(duration);
		if (recorder instanceof ScratchVideoRecorder) {
			ScratchVideoRecorder svr = (ScratchVideoRecorder) recorder;
			String tabName = XML.stripExtension(panel.getTitle()).trim();
			String viewName = viewDropdown.getSelectedItem().toString().trim().toLowerCase();
			int n = viewName.indexOf(" "); //$NON-NLS-1$
			if (n > -1)
				viewName = viewName.substring(0, n);
			svr.suggestFileName(tabName + "-" + viewName); //$NON-NLS-1$
		}
		if (recorder instanceof ImageVideoRecorder) {
			ImageVideoRecorder ivr = (ImageVideoRecorder) recorder;
			ivr.setExpectedFrameCount(clip.getStepCount());
		}
		// create the video
		try {
			recorder.createVideo(filePath); // if null, user selects file with chooser
			savedFilePath = recorder.getFileName();
			if (savedFilePath == null) { // canceled by user
				// restore original magnification and video visibility
				panel.setMagnification(magnification);
				setVideoVisible(videoIsVisible);
				setVisible(true);
				player.setEnabled(true);
				recorder.reset();
				return;
			}
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(panel, ex, "Exception error creating video", //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
		}
		// deal with special case of single image
		if (clip.getStepCount() == 1) {
			try {
				for (BufferedImage image : getNextImages(size)) {
					recorder.addFrame(image);
				}
				savedFilePath = recorder.saveVideo();
				if (savedFilePath == null) {
					recorder.reset();
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(panel, ex, "Exception error ading frame", //$NON-NLS-1$
						JOptionPane.WARNING_MESSAGE);
			}
		}
		// step thru video and record images
		else {
			// create progress monitor
			String description = XML.getName(recorder.getFileName());
			ProgressMonitor monitor = new ProgressMonitor(frame,
					TrackerRes.getString("TActions.SaveClipAs.ProgressMonitor.Message") //$NON-NLS-1$
							+ " " + description, //$NON-NLS-1$
					"", 0, taskLength); //$NON-NLS-1$
			monitor.setMillisToPopup(2000);
			monitor.setProgress(1);
//			JComponent theView = view;

			// create "stepnumber" PropertyChangeListener to add frames
			listener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					int progress = ((Integer) e.getNewValue()).intValue() + 1;
					Runnable runner = new Runnable() {
						@Override
						public void run() {
							boolean done = (playControl.getStepNumber() == clip.getStepCount() - 1);
							if (!monitor.isCanceled()) {
								monitor.setNote(String
										.format(TrackerRes.getString("TActions.SaveClipAs.ProgressMonitor.Progress") //$NON-NLS-1$
												+ " %d%%.\n", progress * 100 / taskLength));
								monitor.setProgress(done ? progress + 1 : progress);								
							}
							setProgress(monitor, playControl, player, recorder, videoIsVisible, clip, size, magnification, done, showOpenDialog);
						}

					};
					EventQueue.invokeLater(runner);
				}
			};

			playControl.addPropertyChangeListener(ClipControl.PROPERTY_CLIPCONTROL_STEPNUMBER, listener); //$NON-NLS-1$
			// if video is at step 0, add first image and step forward
			if (playControl.getStepNumber() == 0) {
				String message = String.format(TrackerRes.getString("TActions.SaveClipAs.ProgressMonitor.Progress") //$NON-NLS-1$
						+ " %d%%.\n", 100 / taskLength); //$NON-NLS-1$
				monitor.setNote(message);
				// force repaint of trackerPanel before getting images
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				trackerPanel.paintImmediately(trackerPanel.getBounds());
				try {
					for (BufferedImage image : getNextImages(size)) {
						recorder.addFrame(image);
					}
					System.gc();
					playControl.step();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(panel, ex, "Exception error adding image", //$NON-NLS-1$
							JOptionPane.WARNING_MESSAGE);
					monitor.close();
					// restore original magnification and video visibility
					panel.setMagnification(magnification);
					setVideoVisible(videoIsVisible);
					player.setEnabled(true);
					recorder.reset();
					return;
				}
			}
			// if video is not at step 0, set step number to 0
			else
				playControl.setStepNumber(0);
		}
	}

	protected void setProgress(ProgressMonitor monitor, ClipControl playControl, VideoPlayer player, VideoRecorder recorder, boolean videoIsVisible, VideoClip clip, Dimension size, double magnification, boolean done, boolean showOpenDialog) {
		if (monitor.isCanceled()) {
			firePropertyChange(PROPERTY_EXPORTVIDEO_VIDEOCANCELED, null, null); //$NON-NLS-1$
			monitor.close();
			playControl.removePropertyChangeListener(ClipControl.PROPERTY_CLIPCONTROL_STEPNUMBER, listener); //$NON-NLS-1$
			// restore original magnification and video visibility
			frame.getTrackerPanelForID(panelID).setMagnification(magnification);
			setVideoVisible(videoIsVisible);
			player.setEnabled(true);
			recorder.reset();
			return;
		}
		if (done)
			playControl.removePropertyChangeListener(ClipControl.PROPERTY_CLIPCONTROL_STEPNUMBER, listener); //$NON-NLS-1$

		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		
		try {
			for (BufferedImage image : getNextImages(size)) {
				recorder.addFrame(image);
			}
			System.gc();
			// if done, save video
			if (done) {
				savedFilePath = recorder.saveVideo();
				recorder.reset();
				// restore original magnification and video visibility
				trackerPanel.setMagnification(magnification);
				setVideoVisible(videoIsVisible);
				player.setEnabled(true);
				
				// set VideoIO preferred export format
				String imageExt = XML.getExtension(savedFilePath);
				if ("zip".equals(imageExt)) {
					VideoType videoType = TrackerIO.videoFormats.get(formatDropdown.getSelectedItem());
					if (videoType instanceof VideoIO.ZipImageVideoType) {
						VideoIO.ZipImageVideoType zvt = (VideoIO.ZipImageVideoType)videoType;
						imageExt += " " + zvt.getImageExtension();
					}
				}
				if (imageExt != null)
					VideoIO.setPreferredExportExtension(imageExt);

				if (showOpenDialog) {
					int response = javax.swing.JOptionPane.showConfirmDialog(frame,
							TrackerRes.getString("ExportVideoDialog.Complete.Message1") //$NON-NLS-1$
									+ " " + XML.getName(savedFilePath) 
								    + (recorder.getCodec() == null ? "" : " (codec=" + recorder.getCodec() + ")") + XML.NEW_LINE //$NON-NLS-1$
									+ TrackerRes.getString("ExportVideoDialog.Complete.Message2"), //$NON-NLS-1$
							TrackerRes.getString("ExportVideoDialog.Complete.Title"), //$NON-NLS-1$
							javax.swing.JOptionPane.YES_NO_OPTION,
							javax.swing.JOptionPane.QUESTION_MESSAGE);
					if (response == javax.swing.JOptionPane.YES_OPTION) {
						frame.loadedFiles.remove(savedFilePath);
						File file = new File(savedFilePath);
						Runnable runner = new Runnable() {
							@Override
							public void run() {
								TrackerIO.openFileFromDialog(file, frame, TrackerIO.NULL_RUNNABLE);
							}
						};
						SwingUtilities.invokeLater(runner);
					}
				}
				firePropertyChange(PROPERTY_EXPORTVIDEO_VIDEOSAVED, null, savedFilePath); //$NON-NLS-1$
			}
			// else step to next frame
			else {
				playControl.step();
			}
		} catch (Exception ex) {
			this.savedFilePath = null;
			JOptionPane.showMessageDialog(trackerPanel, ex.toString(), "Exception saving video: ", //$NON-NLS-1$
					JOptionPane.WARNING_MESSAGE);
			monitor.close();
			playControl.removePropertyChangeListener(ClipControl.PROPERTY_CLIPCONTROL_STEPNUMBER, listener); //$NON-NLS-1$
			// restore original magnification and video visibility
			trackerPanel.setMagnification(magnification);
			setVideoVisible(videoIsVisible);
			player.setEnabled(true);
			recorder.reset();
			return;
		}
	}

	/**
	 * Gets an image of a specified size from the TrackerPanel. The view and content
	 * dropdowns are used to determine which component is rendered.
	 * 
	 * @param size the size
	 * @return a BufferedImage
	 */
	private BufferedImage[] getNextImages(Dimension size) {
		JComponent view = views.get(viewDropdown.getSelectedItem());
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		if (view == trackerPanel) { // main view
			// if content is video only, get video image and resize
			if (contentDropdown.getSelectedIndex() == 1) {
				BufferedImage img = trackerPanel.getVideo().getImage();
				return new BufferedImage[] { getResizedImage(img, size) };
			}
			// if content is deinterlaced video, get deinterlaced video images
			if (contentDropdown.getSelectedIndex() == 3) {
				DeinterlaceFilter filter = (DeinterlaceFilter) trackerPanel.getVideo().getFilterStack()
						.getFilter(DeinterlaceFilter.class);
				if (filter == null) {
					filter = new DeinterlaceFilter();
					trackerPanel.getVideo().getFilterStack().addFilter(filter);
				}
				boolean odd = filter.isOdd();
				if (odd != oddFirst)
					filter.setOdd(oddFirst);
				BufferedImage img = trackerPanel.getVideo().getImage();
				BufferedImage img1 = getResizedCopy(img, size);
				filter.setOdd(!oddFirst);
				img = trackerPanel.getVideo().getImage();
				BufferedImage img2 = getResizedCopy(img, size);
				return new BufferedImage[] { img1, img2 };
			}
			// if content includes graphics, have TrackerPanel render the mat
			return new BufferedImage[] { getResizedImage(trackerPanel.getMattedImage(), size) };
		}
		if (view instanceof WorldTView) { // world view
			WorldTView wtv = (WorldTView) view;
			// render using the full size of the worldPanel before clipping
			Dimension dim = wtv.scrollPane.getViewport().getView().getSize();
			BufferedImage image = (BufferedImage) view.createImage(dim.width, dim.height);
			image = ((WorldTView) view).render(image);	
			// rect is the clipping rectangle
			Rectangle rect = wtv.scrollPane.getViewport().getViewRect();
			return new BufferedImage[] { getClippedImage(image, rect) };
		}
		if (view instanceof PlotTrackView) { // plot view
			BufferedImage image = ((PlotTrackView) view).exportImage(size.width, size.height);
			return new BufferedImage[] { image };
		}
		// entire frame
		BufferedImage image = (BufferedImage) view.createImage(size.width, size.height);
		Graphics2D g2 = image.createGraphics();
		view.paint(g2);
		g2.dispose();
		return new BufferedImage[] { image };
	}

	/**
	 * Clips a source image and returns the clipped image. This method re-uses the
	 * same image and returns the original image if the clip is the whole image.
	 * 
	 * @param source the source image
	 * @param rect   the desired clipping Rectangle
	 * @return a BufferedImage
	 */
	private BufferedImage getClippedImage(BufferedImage source, Rectangle rect) {		
		if (rect.width == source.getWidth() && rect.height == source.getHeight())
			return source;
		if (sizedImage == null 
				|| sizedImage.getWidth() != rect.width 
				|| sizedImage.getHeight() != rect.height) {
			sizedImage = new BufferedImage(rect.width, rect.height, source.getType());
		}
		BufferedImage img = source.getSubimage(rect.x, rect.y, rect.width, rect.height);		
		Graphics2D g2 = sizedImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(img, 0, 0, null);	
		g2.dispose();
		return sizedImage;
	}

	/**
	 * Resizes a source image and returns the resized image. This method re-uses the
	 * same image and returns the original image if not resized.
	 * 
	 * @param source the source image
	 * @param size   the desired size
	 * @return a BufferedImage
	 */
	private BufferedImage getResizedImage(BufferedImage source, Dimension size) {
		if (size.width == source.getWidth() && size.height == source.getHeight())
			return source;
		if (sizedImage == null || sizedImage.getWidth() != size.width || sizedImage.getHeight() != size.height) {
			sizedImage = new BufferedImage(size.width, size.height, source.getType());
		}
		Graphics2D g2 = sizedImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(source, 0, 0, size.width, size.height, 0, 0, source.getWidth(), source.getHeight(), null);
		g2.dispose();
		return sizedImage;
	}

	/**
	 * Returns a resized copy of a source image. This method always return a new
	 * image.
	 * 
	 * @param source the source image
	 * @param size   the desired size
	 * @return a BufferedImage
	 */
	private BufferedImage getResizedCopy(BufferedImage source, Dimension size) {
		BufferedImage newImage = new BufferedImage(size.width, size.height, source.getType());
		Graphics2D g2 = newImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(source, 0, 0, size.width, size.height, 0, 0, source.getWidth(), source.getHeight(), null);
		g2.dispose();
		return newImage;
	}

	public void clear() {
		frame = null;
		panelID = null;
		views.clear();
	}
	
	@Override
	public void dispose() {
		clear();
		super.dispose();
	}

}
