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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import org.opensourcephysics.display.ColorIcon;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.VideoClip;
import org.opensourcephysics.tools.FontSizer;

/**
 * A Dialog to control PencilScenes for a PencilDrawer.
 *
 * @author Douglas Brown
 */
@SuppressWarnings("serial")
public class PencilControl extends JDialog {

	static PencilScene dummyScene = new PencilScene();
	static Icon undoIcon, redoIcon, undoDisabledIcon, redoDisabledIcon;
	static Icon trailIcon, trailSelectedIcon;
	static Icon arrowIcon, arrowSelectedIcon;
	static Icon ellipseIcon, ellipseSelectedIcon;
	private static Color lightgrey = new Color(230, 230, 230);

	static {
		undoIcon = Tracker.getResourceIcon("undo.gif", true); //$NON-NLS-1$
		redoIcon = Tracker.getResourceIcon("redo.gif", true); //$NON-NLS-1$
		undoDisabledIcon = Tracker.getResourceIcon("undo_disabled.gif", true); //$NON-NLS-1$
		redoDisabledIcon = Tracker.getResourceIcon("redo_disabled.gif", true); //$NON-NLS-1$
		trailIcon = Tracker.getResourceIcon("freeform.gif", true); //$NON-NLS-1$
		trailSelectedIcon = Tracker.getResourceIcon("freeform_selected.gif", true); //$NON-NLS-1$
		arrowIcon = Tracker.getResourceIcon("arrow.gif", true); //$NON-NLS-1$
		arrowSelectedIcon = Tracker.getResourceIcon("arrow_selected.gif", true); //$NON-NLS-1$
		ellipseIcon = Tracker.getResourceIcon("ellipse.gif", true); //$NON-NLS-1$
		ellipseSelectedIcon = Tracker.getResourceIcon("ellipse_selected.gif", true); //$NON-NLS-1$
	}

	protected TFrame frame;
	protected Integer panelID;

	private PencilDrawer drawer;
	private PencilScene selectedScene;
	private JComboBox<PencilScene> sceneDropdown;
	private DrawingPanel canvas;
	private JLabel drawingLabel, captionLabel, framesLabel, toLabel;
	private JSpinner startFrameSpinner, endFrameSpinner;
	private JTextField captionField;
	private JButton newSceneButton, deleteSceneButton;
	private JButton undoButton, redoButton, clearAllButton, closeButton, helpButton;
	private JButton trailButton, arrowButton, ellipseButton;
	private ColorButton[][] colorButtons;
	private Dimension canvasSize = new Dimension(120, 90);
	private boolean refreshing;
	private PropertyChangeListener stepListener, tabListener, clipListener;
	private int buttonWidth = 14;
	private boolean isVisible;
	private UndoableEditSupport undoSupport;
	private UndoManager undoManager;
	private AbstractAction postCaptionEditAction;
	private String prevCaptionText;
	JSpinner fontSizeSpinner;
	JCheckBox heavyCheckbox;

	/**
	 * Constructs a PencilControl for a specified PencilDrawer.
	 * 
	 * @param pencilDrawer the PencilDrawer
	 */
	protected PencilControl(PencilDrawer pencilDrawer) {
		super(pencilDrawer.frame, false);
		drawer = pencilDrawer;
		frame = drawer.frame;
		panelID = drawer.panelID;
		// set up the undo system
		undoManager = new UndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(undoManager);
		createGUI();
		refreshGUI();
		pack();
		// center on screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (dim.width - getBounds().width) / 2;
		int y = (dim.height - getBounds().height) / 2;
		setLocation(x, y);
		// create PropertyChangeListeners
		stepListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// if selected scene is visible at current frame, do nothing
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				if (selectedScene != null && selectedScene.includesFrame(trackerPanel.getFrameNumber()))
					return;
				if (isVisible()) {
					setSelectedScene(drawer.getSceneAtFrame(trackerPanel.getFrameNumber()));
					refreshGUI();
				}
			}
		};
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		trackerPanel.addPropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, stepListener); // $NON-NLS-1$
		tabListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// TFrame.PROPERTY_TFRAME_TAB
				if (!frame.isRemovingAll() && e.getNewValue() == trackerPanel) {
					setVisible(isVisible);
				} else {
					boolean vis = isVisible;
					setVisible(false);
					isVisible = vis;
				}
			}
		};
		if (frame != null) {
			frame.addPropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, tabListener); // $NON-NLS-1$
		}
		clipListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				// stepcount has changed
				refreshGUI();
			}
		};
		trackerPanel.addPropertyChangeListener(VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT, clipListener); // $NON-NLS-1$
	}

	/**
	 * Creates and assembles the GUI components
	 */
	private void createGUI() {
		setResizable(false);
		// create labels
		drawingLabel = new JLabel();
		drawingLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
		captionLabel = new JLabel();
		captionLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
		framesLabel = new JLabel();
		framesLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
		toLabel = new JLabel();
		toLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

		// create buttons
		newSceneButton = new JButton() {
			@Override
			public Dimension getMaximumSize() {
				Dimension dim = super.getMaximumSize();
				dim.height = captionField.getPreferredSize().height + 4;
				return dim;
			}
		};
		newSceneButton.addActionListener((e) -> {
			drawer.addNewScene();
			refreshGUI();
		});

		deleteSceneButton = new JButton() {
			@Override
			public Dimension getMaximumSize() {
				Dimension dim = super.getMaximumSize();
				dim.height = captionField.getPreferredSize().height + 4;
				return dim;
			}

		};
		deleteSceneButton.addActionListener((e) -> {
			boolean isEmpty = selectedScene.getDrawings().isEmpty() && "".equals(selectedScene.getCaption().getText()); //$NON-NLS-1$
			drawer.removeScene(selectedScene);
			if (!isEmpty) {
				postDeletionEdit(selectedScene);
			}
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			setSelectedScene(drawer.getSceneAtFrame(trackerPanel.getFrameNumber()));
		});

		undoButton = new SideButton(undoIcon);
		undoButton.addActionListener((e) -> {
			undoManager.undo();
			refreshGUI();
		});

		redoButton = new SideButton(redoIcon);
		redoButton.addActionListener((e) -> {
			undoManager.redo();
			refreshGUI();
		});

		trailButton = new SideButton(trailIcon);
		trailButton.addActionListener((e) -> {
			drawer.style = PencilDrawing.STYLE_TRAIL;
			refreshStyleButtons();
		});

		arrowButton = new SideButton(arrowIcon);
		arrowButton.addActionListener((e) -> {
			drawer.style = PencilDrawing.STYLE_ARROW;
			refreshStyleButtons();
		});

		ellipseButton = new SideButton(ellipseIcon);
		ellipseButton.addActionListener((e) -> {
			drawer.style = PencilDrawing.STYLE_ELLIPSE;
			refreshStyleButtons();
		});

		clearAllButton = new JButton();
		clearAllButton.addActionListener((e) -> {
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			if (PencilDrawer.hasDrawings(trackerPanel)) {
				postClearEdit(drawer.scenes);
			}
			drawer.clearScenes(true);
			setSelectedScene(null);
			refreshGUI();
		});

		closeButton = new JButton();
		closeButton.addActionListener((e) -> {
			setVisible(false);
		});

		helpButton = new JButton();
		helpButton.addActionListener((e) -> {
			frame.showHelp("drawings", 0); //$NON-NLS-1$
		});

		// create pencilButtons AFTER undoButton
		Box colorBox = Box.createVerticalBox();
		colorButtons = new ColorButton[PencilDrawer.colors.length][PencilDrawer.colors[0].length];
		for (int i = 0; i < PencilDrawer.colors[0].length; i++) {
			Box b = Box.createHorizontalBox();
			for (int j = 0; j < colorButtons.length; j++) {
				colorButtons[j][i] = new ColorButton(PencilDrawer.colors[j][i]);
				b.add(colorButtons[j][i]);
			}
			colorBox.add(b);
		}

		// create checkbox
		heavyCheckbox = new JCheckBox();
		heavyCheckbox.addActionListener((e) -> {
			if (refreshing || selectedScene == null)
				return;
			selectedScene.setHeavy(heavyCheckbox.isSelected());
			repaintPanel();
			repaintCanvas();
		});

		// create spinners
		startFrameSpinner = new JSpinner();
		startFrameSpinner.addChangeListener((e) -> {
			if (selectedScene == null)
				return;
			selectedScene.setStartFrame((Integer) startFrameSpinner.getValue());
			Collections.sort(drawer.scenes);
			repaintPanel();
			refreshGUI();
		});

		endFrameSpinner = new JSpinner();
		endFrameSpinner.addChangeListener((e) -> {
			if (selectedScene == null)
				return;
			selectedScene.setEndFrame((Integer) endFrameSpinner.getValue());
			Collections.sort(drawer.scenes);
			repaintPanel();
			refreshGUI();
		});

		int defaultFontSize = PencilCaption.baseFont.getSize();
		fontSizeSpinner = new JSpinner(new SpinnerNumberModel(defaultFontSize, 12, 98, 2));
		((JSpinner.DefaultEditor) fontSizeSpinner.getEditor()).getTextField().setEditable(false);
		fontSizeSpinner.addChangeListener((e) -> {
			if (selectedScene.getCaption() == null)
				return;
			if (refreshing)
				return;
			Font font = selectedScene.getCaption().getFont();
			float size = (Integer) fontSizeSpinner.getValue();
			font = font.deriveFont(size);
			selectedScene.getCaption().setFont(font);
			repaintPanel();
			repaintCanvas();
		});

		// create dropdown
		sceneDropdown = new JComboBox<PencilScene>() {
			@Override
			public Dimension getMaximumSize() {
				Dimension dim = super.getMaximumSize();
				dim.height = deleteSceneButton.getMaximumSize().height;
				return dim;
			}
		};
		sceneDropdown.setRenderer(new SceneDropdownRenderer());
		sceneDropdown.addActionListener((e) -> {
			if (refreshing)
				return;
			PencilScene scene = (PencilScene) sceneDropdown.getSelectedItem();
			if (scene == dummyScene)
				return;
			setSelectedScene(scene);
			if (selectedScene != null) {
				goToScene(selectedScene);
			}
			captionField.requestFocusInWindow();
		});

		// create caption field
		captionField = new JTextField(16);
		captionField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					return;
				String text = captionField.getText();
				if (!selectedScene.isCaptionPositioned && !"".equals(text.trim())) { //$NON-NLS-1$
					// place caption at center of viewPort
					TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
					MainTView mainView = frame.getMainView(trackerPanel);
					Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
					int xpix = rect.x + rect.width / 2;
					int ypix = rect.y + rect.height / 2;
					double x = trackerPanel.pixToX(xpix);
					double y = trackerPanel.pixToY(ypix);
					PencilCaption caption = selectedScene.getCaption();
					caption.setXY(x, y);
					caption.setText(text);
					caption.color = drawer.color;
					selectedScene.isCaptionPositioned = true;
				} else {
					selectedScene.getCaption().setText(text);
				}
				captionField.setBackground(Color.YELLOW);
				refreshGUI();
				repaintPanel();
			}
		});
		postCaptionEditAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = selectedScene.getCaption().getText().trim();
				selectedScene.getCaption().setText(text);
				postCaptionEdit(selectedScene, prevCaptionText, text);
				captionField.setBackground(Color.WHITE);
				prevCaptionText = text;
				refreshGUI();
			}
		};
		captionField.addActionListener(postCaptionEditAction);
		captionField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (captionField.getBackground().equals(Color.YELLOW)) {
					postCaptionEditAction.actionPerformed(null);
				}
			}
		});

		// create canvas
		canvas = new DrawingPanel();
		canvas.setAutoscaleX(false);
		canvas.setAutoscaleY(false);
		canvas.setSquareAspect(true);
		canvas.setShowCoordinates(false);
		canvas.setBackground(Color.WHITE);
		canvas.setPreferredGutters(6, 6, 6, 6);
		canvas.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		canvas.setPreferredSize(canvasSize);
		canvas.setEnabled(false);

		// assemble GUI
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		setContentPane(contentPane);

		// north panel
		JPanel northPanel = new JPanel(new BorderLayout());
		contentPane.add(northPanel, BorderLayout.NORTH);

		// add canvas to north panel
		northPanel.add(canvas, BorderLayout.CENTER);

		// add drawing bar to north panel
		JToolBar drawingBar = new JToolBar();
		northPanel.add(drawingBar, BorderLayout.NORTH);
		drawingBar.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		drawingBar.setFloatable(false);
		drawingBar.setOpaque(false);
		drawingBar.add(drawingLabel);
		drawingBar.add(sceneDropdown);
		drawingBar.add(Box.createHorizontalStrut(2));
		drawingBar.add(newSceneButton);
		drawingBar.add(Box.createHorizontalStrut(2));
		drawingBar.add(deleteSceneButton);

		// add caption bar to north panel
		JToolBar captionBar = new JToolBar();
		captionBar.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 8));
		northPanel.add(captionBar, BorderLayout.SOUTH);
		captionBar.setFloatable(false);
		captionBar.setOpaque(false);
		captionBar.add(captionLabel);
		captionBar.add(captionField);
		captionBar.add(fontSizeSpinner);

		// add color box to north panel
		northPanel.add(colorBox, BorderLayout.WEST);

		// add undo/redo buttons to north panel
		Box sideBox = Box.createVerticalBox();
		northPanel.add(sideBox, BorderLayout.EAST);
		sideBox.add(arrowButton);
		sideBox.add(ellipseButton);
		sideBox.add(trailButton);
		sideBox.add(Box.createVerticalGlue());
		sideBox.add(undoButton);
		sideBox.add(redoButton);

		// center panel
		JPanel centerPanel = new JPanel();
		contentPane.add(centerPanel, BorderLayout.CENTER);

		// add frame spinners to center panel
		JToolBar controlBar = new JToolBar();
		centerPanel.add(controlBar);
		controlBar.setFloatable(false);
		controlBar.setOpaque(false);
		controlBar.setBorderPainted(false);
		controlBar.add(framesLabel);
		controlBar.add(startFrameSpinner);
		controlBar.add(toLabel);
		controlBar.add(endFrameSpinner);

		// add heavyCheckbox to center panel
		centerPanel.add(Box.createHorizontalStrut(15));
		centerPanel.add(heavyCheckbox);

		// south panel
		JPanel southPanel = new JPanel();
		southPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		contentPane.add(southPanel, BorderLayout.SOUTH);

		// add clear and close buttons to south panel
		southPanel.add(helpButton);
		southPanel.add(clearAllButton);
		southPanel.add(closeButton);

	}

	private void repaintPanel() {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		TFrame.repaintT(trackerPanel);
		trackerPanel.changed = true;
	}

	/**
	 * Sets the font level
	 * 
	 * @param level the font level
	 */
	protected void setFontLevel(int level) {
		FontSizer.setFonts(this, level);
		pack();
	}

	/**
	 * Posts a drawing edit for a specified scene.
	 * 
	 * @param drawing the drawing
	 * @param scene   the scene
	 */
	protected void postDrawingEdit(PencilDrawing drawing, PencilScene scene) {
		DrawingEdit edit = new DrawingEdit(drawing, scene);
		undoSupport.postEdit(edit);
	}

	/**
	 * Posts a caption edit for a specified scene.
	 * 
	 * @param scene   the scene
	 * @param oldText the previous caption text
	 * @param newText the new caption text
	 */
	protected void postCaptionEdit(PencilScene scene, String oldText, String newText) {
		CaptionEdit edit = new CaptionEdit(scene, oldText, newText);
		undoSupport.postEdit(edit);
	}

	/**
	 * Posts a deletion edit for a specified scene.
	 * 
	 * @param scene the scene
	 */
	protected void postDeletionEdit(PencilScene scene) {
		DeletionEdit edit = new DeletionEdit(scene);
		undoSupport.postEdit(edit);
	}

	/**
	 * Posts a clear edit.
	 * 
	 * @param scenes the cleared scenes
	 */
	protected void postClearEdit(ArrayList<PencilScene> scenes) {
		ClearEdit edit = new ClearEdit(scenes);
		undoSupport.postEdit(edit);
	}

	/**
	 * Refreshes the GUI.
	 */
	protected void refreshStyleButtons() {
		trailButton.setIcon(drawer.style == PencilDrawing.STYLE_TRAIL ? trailSelectedIcon : trailIcon);
		arrowButton.setIcon(drawer.style == PencilDrawing.STYLE_ARROW ? arrowSelectedIcon : arrowIcon);
		ellipseButton.setIcon(drawer.style == PencilDrawing.STYLE_ELLIPSE ? ellipseSelectedIcon : ellipseIcon);
	}

	/**
	 * Refreshes the GUI.
	 */
	protected void refreshGUI() {
		refreshing = true;
		setTitle(TrackerRes.getString("PencilControlDialog.Title")); //$NON-NLS-1$
		// set label and button text
		drawingLabel.setText(TrackerRes.getString("PencilControlDialog.Label.Drawing.Text") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		toLabel.setText(TrackerRes.getString("PencilControlDialog.Label.To.Text")); //$NON-NLS-1$
		captionLabel.setText(TrackerRes.getString("PencilControlDialog.Label.Caption.Text") + ":"); //$NON-NLS-1$ //$NON-NLS-2$
		framesLabel.setText(TrackerRes.getString("PencilControlDialog.Label.Frames.Text")); //$NON-NLS-1$
		newSceneButton.setText(TrackerRes.getString("PencilControlDialog.Button.NewScene.Text")); //$NON-NLS-1$
		deleteSceneButton.setText(TrackerRes.getString("PencilControlDialog.Button.DeleteScene.Text")); //$NON-NLS-1$
		clearAllButton.setText(TrackerRes.getString("PencilControlDialog.Button.ClearAll.Text")); //$NON-NLS-1$
		closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
		helpButton.setText(TrackerRes.getString("Dialog.Button.Help")); //$NON-NLS-1$
		heavyCheckbox.setText(TrackerRes.getString("PencilControlDialog.Checkbox.Heavy.Text")); //$NON-NLS-1$
		// set label and button tooltips
		newSceneButton.setToolTipText(TrackerRes.getString("PencilControlDialog.Button.NewScene.Tooltip")); //$NON-NLS-1$
		deleteSceneButton.setToolTipText(TrackerRes.getString("PencilControlDialog.Button.DeleteScene.Tooltip")); //$NON-NLS-1$
		clearAllButton.setToolTipText(TrackerRes.getString("PencilControlDialog.Button.ClearAll.Tooltip")); //$NON-NLS-1$
		heavyCheckbox.setToolTipText(TrackerRes.getString("PencilControlDialog.Checkbox.Heavy.Tooltip")); //$NON-NLS-1$
		captionField.setToolTipText(TrackerRes.getString("PencilControlDialog.Field.Caption.Tooltip")); //$NON-NLS-1$
		sceneDropdown.setToolTipText(TrackerRes.getString("PencilControlDialog.Dropdown.Drawing.Tooltip")); //$NON-NLS-1$
		fontSizeSpinner.setToolTipText(TrackerRes.getString("PencilControlDialog.Spinner.FontSize.Tooltip")); //$NON-NLS-1$
		startFrameSpinner.setToolTipText(TrackerRes.getString("PencilControlDialog.Spinner.FrameRange.Tooltip")); //$NON-NLS-1$
		endFrameSpinner.setToolTipText(TrackerRes.getString("PencilControlDialog.Spinner.FrameRange.Tooltip")); //$NON-NLS-1$
		undoButton.setToolTipText(undoManager.getUndoPresentationName());
		redoButton.setToolTipText(undoManager.getRedoPresentationName());
		// enable/disable components

		boolean enabled = selectedScene != null;
		drawingLabel.setEnabled(enabled);
		toLabel.setEnabled(enabled);
		captionLabel.setEnabled(enabled);
		framesLabel.setEnabled(enabled);
		deleteSceneButton.setEnabled(enabled);
		heavyCheckbox.setEnabled(enabled);
		fontSizeSpinner.setEnabled(enabled);
		startFrameSpinner.setEnabled(enabled);
		endFrameSpinner.setEnabled(enabled);
		captionField.setEnabled(enabled);
		undoButton.setEnabled(undoManager.canUndo());
		redoButton.setEnabled(undoManager.canRedo());
		undoButton.setIcon(undoManager.canUndo() ? undoIcon : undoDisabledIcon);
		redoButton.setIcon(undoManager.canRedo() ? redoIcon : redoDisabledIcon);
		clearAllButton.setEnabled(!drawer.scenes.isEmpty());
		sceneDropdown.setEnabled(!drawer.scenes.isEmpty());
		newSceneButton.setEnabled(selectedScene == null || !selectedScene.getDrawings().isEmpty());
		if (enabled) {
			repaintCanvas();
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			int first = trackerPanel.getPlayer().getVideoClip().getFirstFrameNumber();
			int last = trackerPanel.getPlayer().getVideoClip().getLastFrameNumber();
			// scene start frame can't be less than clip start frame
			selectedScene.startframe = Math.max(first, selectedScene.startframe);
			startFrameSpinner.setModel(new SpinnerNumberModel(selectedScene.startframe, first, last, 1));
			int end = selectedScene.endframe == Integer.MAX_VALUE ? last : selectedScene.endframe;
			endFrameSpinner.setModel(new SpinnerNumberModel(end, selectedScene.startframe, last, 1));
			heavyCheckbox.setSelected(selectedScene.isHeavy());
			drawer.color = selectedScene.getCaption().color;
			fontSizeSpinner.setValue(selectedScene.getCaption().getFont().getSize());
		} else {
			heavyCheckbox.setSelected(false);
		}
		// refresh color buttons
		for (int i = 0; i < colorButtons.length; i++) {
			for (ColorButton b : colorButtons[i]) {
				b.setToolTipText(TrackerRes.getString("PencilControlDialog.Button.Color.Tooltip")); //$NON-NLS-1$
				b.setBorder(b.color == drawer.color && enabled ? BorderFactory.createLineBorder(Color.GRAY, 2)
						: BorderFactory.createLineBorder(lightgrey, 2));
				b.icon.setColor(enabled ? b.color : Color.LIGHT_GRAY);
				b.setEnabled(enabled);
			}
		}
		// refresh scene dropdown
		sceneDropdown.removeAllItems();
		for (PencilScene scene : drawer.scenes) {
			sceneDropdown.addItem(scene);
		}
		if (selectedScene != null) {
			refreshing = false;
			sceneDropdown.setSelectedItem(selectedScene);
		} else if (!drawer.scenes.isEmpty()) {
			sceneDropdown.addItem(dummyScene);
			sceneDropdown.setSelectedItem(dummyScene);
		}
		refreshStyleButtons();
		refreshing = false;
		pack();
		repaint();
	}

	/**
	 * Repaints the canvas.
	 */
	protected void repaintCanvas() {
		selectedScene.measure();
		canvas.setPreferredMinMaxX(selectedScene.getXMin(), selectedScene.getXMax());
		// y min/max are purposely reversed in following line
		canvas.setPreferredMinMaxY(selectedScene.getYMax(), selectedScene.getYMin());
		canvas.repaint();
	}

	@Override
	public void setVisible(boolean vis) {
		super.setVisible(vis);
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		TToolBar toolbar = trackerPanel.getToolBar(true);
		toolbar.drawingButton.setSelected(vis);
		if (Tracker.showHints) {
			trackerPanel.setMessage(vis ? TrackerRes.getString("PencilDrawer.Hint") : null); //$NON-NLS-1$
		}
		isVisible = vis;
	}

	/**
	 * Gets the selected scene. May return null.
	 * 
	 * @return the selected scene
	 */
	protected PencilScene getSelectedScene() {
		return selectedScene;
	}

	/**
	 * Sets the selected scene.
	 * 
	 * @param scene the scene to select. May be null.
	 */
	protected void setSelectedScene(PencilScene scene) {
		if (selectedScene == scene)
			return;
		if (selectedScene != null)
			canvas.removeDrawable(selectedScene);
		selectedScene = scene;
		if (selectedScene != null) {
			canvas.addDrawable(selectedScene);
			prevCaptionText = selectedScene.getCaption().getText();
			captionField.setText(selectedScene.getCaption().getText());
		} else {
			captionField.setText(null);
		}
		refreshGUI();
	}

	/**
	 * Goes to a specified scene.
	 * 
	 * @param scene the scene
	 */
	private void goToScene(PencilScene scene) {
		if (scene == null)
			return;
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		if (!scene.includesFrame(trackerPanel.getFrameNumber())) {
			// set step number to show scene
			int stepNum = trackerPanel.getPlayer().getVideoClip().frameToStep(scene.startframe);
			trackerPanel.getPlayer().setStepNumber(stepNum);
		}
	}

	@Override
	public void dispose() {
		TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
		trackerPanel.removePropertyChangeListener(TrackerPanel.PROPERTY_TRACKERPANEL_STEPNUMBER, stepListener); // $NON-NLS-1$
		trackerPanel.removePropertyChangeListener(VideoClip.PROPERTY_VIDEOCLIP_STEPCOUNT, clipListener); // $NON-NLS-1$
		if (frame != null) {
			frame.removePropertyChangeListener(TFrame.PROPERTY_TFRAME_TAB, tabListener); // $NON-NLS-1$
		}
		setSelectedScene(null);
		panelID = null;
		frame = null;
	}

	/**
	 * A button class for the right side panel.
	 */
	private class SideButton extends JButton {

		public SideButton(Icon icon) {
			super(icon);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension dim = super.getPreferredSize();
			dim.width = (int) (2 * buttonWidth * FontSizer.getFactor());
			return dim;
		}
	}

	/**
	 * A button class to manage pencil colors.
	 */
	private class ColorButton extends JButton implements ActionListener {

		Color color;
		ColorIcon icon;

		/**
		 * Constructor
		 * 
		 * @param c the color
		 */
		private ColorButton(Color c) {
			color = c;
			setBackground(color);
			icon = new ColorIcon(color, buttonWidth - 4, 16);
			setIcon(new ResizableIcon(icon));
			addActionListener(this);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					if (isEnabled())
						setBorderPainted(false);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setBorderPainted(true);
				}
			});
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (drawer.color.equals(color))
				return;
			drawer.color = color;
			if (drawer.getSelectedScene() != null) {
				drawer.getSelectedScene().setColor(color);
				repaintPanel();
			}
			refreshGUI();
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			trackerPanel.setMouseCursor(drawer.getPencilCursor());
			setBorderPainted(true);
		}

	}

	/**
	 * Custom renderer for scene dropdown. List items are PencilScenes.
	 */
	private class SceneDropdownRenderer extends JLabel implements ListCellRenderer<PencilScene> {

		/**
		 * Private constructor
		 */
		private SceneDropdownRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
			setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 0));
		}

		@Override
		public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, PencilScene value,
				int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			if (value != null) {
				PencilScene scene = (PencilScene) value;
				TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
				setText(scene.getDescription(trackerPanel));
			} else {
				setText(""); //$NON-NLS-1$
			}
			return this;
		}
	}

	/**
	 * A class to undo/redo a pencil drawing for a PencilScene.
	 */
	private class DrawingEdit extends AbstractUndoableEdit {
		PencilDrawing drawing;
		PencilScene scene;

		/**
		 * Constructor.
		 * 
		 * @param drawing the drawing
		 * @param scene   the scene
		 */
		public DrawingEdit(PencilDrawing drawing, PencilScene scene) {
			this.drawing = drawing;
			this.scene = scene;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			scene.getDrawings().remove(drawing);
			update();
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			scene.getDrawings().add(drawing);
			update();
		}

		private void update() {
			setSelectedScene(scene);
			goToScene(scene);
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			TFrame.repaintT(trackerPanel);
		}

		@Override
		public String getUndoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.DrawingEdit.Undo.Text"); //$NON-NLS-1$
		}

		@Override
		public String getRedoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.DrawingEdit.Redo.Text"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo a PencilScene deletion.
	 */
	private class DeletionEdit extends AbstractUndoableEdit {
		PencilScene scene;

		/**
		 * Constructor.
		 * 
		 * @param scene the deleted scene
		 */
		public DeletionEdit(PencilScene scene) {
			this.scene = scene;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			drawer.addScene(scene);
			goToScene(scene);
			setSelectedScene(scene);
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			drawer.removeScene(scene);
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			setSelectedScene(drawer.getSceneAtFrame(trackerPanel.getFrameNumber()));
		}

		@Override
		public String getUndoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.DeletionEdit.Undo.Text"); //$NON-NLS-1$
		}

		@Override
		public String getRedoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.DeletionEdit.Redo.Text"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo clearing all PencilScenes.
	 */
	private class ClearEdit extends AbstractUndoableEdit {
		ArrayList<PencilScene> scenes = new ArrayList<PencilScene>();

		/**
		 * Constructor.
		 * 
		 * @param scenes the cleared scenes
		 */
		public ClearEdit(ArrayList<PencilScene> scenes) {
			this.scenes.addAll(scenes);
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			drawer.setScenes(scenes);
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			PencilScene scene = drawer.getSceneAtFrame(trackerPanel.getFrameNumber());
			if (scene != null) {
				setSelectedScene(scene);
			} else {
				goToScene(scenes.get(0));
			}
			TFrame.repaintT(trackerPanel);
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			drawer.clearScenes(true);
			setSelectedScene(null);
			refreshGUI();
		}

		@Override
		public String getUndoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.ClearEdit.Undo.Text"); //$NON-NLS-1$
		}

		@Override
		public String getRedoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.ClearEdit.Redo.Text"); //$NON-NLS-1$
		}

	}

	/**
	 * A class to undo/redo caption text changes.
	 */
	private class CaptionEdit extends AbstractUndoableEdit {
		String undoText, redoText;
		PencilScene scene;

		/**
		 * Constructor.
		 * 
		 * @param scene   the scene with the original caption
		 * @param newText the new text
		 */
		public CaptionEdit(PencilScene scene, String oldText, String newText) {
			this.scene = scene;
			undoText = oldText;
			redoText = newText;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			undoRedo(undoText);
		}

		@Override
		public void redo() throws CannotUndoException {
			super.redo();
			undoRedo(redoText);
		}

		private void undoRedo(String text) {
			scene.getCaption().setText(text);
			setSelectedScene(scene);
			goToScene(scene);
			captionField.setText(text);
			TrackerPanel trackerPanel = frame.getTrackerPanelForID(panelID);
			TFrame.repaintT(trackerPanel);
			repaintCanvas();
		}

		@Override
		public String getUndoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.CaptionEdit.Undo.Text"); //$NON-NLS-1$
		}

		@Override
		public String getRedoPresentationName() {
			return TrackerRes.getString("PencilControlDialog.CaptionEdit.Redo.Text"); //$NON-NLS-1$
		}

	}

}
