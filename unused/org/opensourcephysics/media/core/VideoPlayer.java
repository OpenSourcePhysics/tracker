/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

/*
 * The org.opensourcephysics.media.core package defines the Open Source Physics
 * media framework for working with video and other media.
 *
 * Copyright (c) 2017  Douglas Brown and Wolfgang Christian.
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
 * For additional information and documentation on Open Source Physics,
 * please see <http://www.opensourcephysics.org/>.
 */
package org.opensourcephysics.media.core;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Hashtable;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This is a GUI component for playing a VideoClip.
 * It uses a subclass of ClipControl to control the clip and updates
 * its display based on PropertyChangeEvents it receives from the
 * ClipControl.
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class VideoPlayer extends JComponent implements PropertyChangeListener {
	
	// static fields
  protected static Icon inOutIcon, playIcon, grayPlayIcon, pauseIcon;
  protected static Icon resetIcon, loopIcon, noloopIcon, videoClipIcon;
  protected static Icon stepIcon, grayStepIcon, backIcon, grayBackIcon;
  private static GoToDialog goToDialog;
  private static NumberFormat timeFormat = NumberFormat.getNumberInstance();
  static {
    String path = "/org/opensourcephysics/resources/media/images/in_out.gif";  //$NON-NLS-1$
    inOutIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/play.gif";  //$NON-NLS-1$
    playIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/play_gray.gif";  //$NON-NLS-1$
    grayPlayIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/pause.gif";  //$NON-NLS-1$
    pauseIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/reset.gif";  //$NON-NLS-1$
    resetIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/looping_on.gif";  //$NON-NLS-1$
    loopIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/looping_off.gif";  //$NON-NLS-1$
    noloopIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/video_clip.gif";  //$NON-NLS-1$
    videoClipIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/step.gif";  //$NON-NLS-1$
    stepIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/step_gray.gif";  //$NON-NLS-1$
    grayStepIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/back.gif";  //$NON-NLS-1$
    backIcon = ResourceLoader.getIcon(path);
    path = "/org/opensourcephysics/resources/media/images/back_gray.gif";  //$NON-NLS-1$
    grayBackIcon = ResourceLoader.getIcon(path);
  }
	
  // instance fields
  protected VideoPanel vidPanel;
  protected ClipControl clipControl;
  private String[] readoutTypes;
  private String readoutType;
  private boolean inspectorButtonVisible = true;
  protected int height = 54;
  // GUI elements
  private JToolBar toolbar;
  protected JButton readout;
  private JButton playButton, resetButton;
  private JSpinner rateSpinner;
  private JButton stepButton;
  private JButton stepSizeButton;
  private JButton backButton;
  private JButton loopButton;
  private JButton inspectorButton;
  private JSlider slider;
  private Hashtable<Integer, JLabel> sliderLabels;
  private JLabel inLabel, outLabel;
  private ActionListener readoutListener, timeSetListener, goToListener;
  private String active;
  private boolean disabled = false;

  /**
   * Constructs a VideoPlayer to play the specified video clip.
   *
   * @param panel the video panel
   * @param clip the video clip
   */
  public VideoPlayer(VideoPanel panel, VideoClip clip) {
    this(panel);
    setVideoClip(clip);
  }

  /**
   * Constructs a VideoPlayer.
   *
   * @param panel the video panel
   */
  public VideoPlayer(VideoPanel panel) {
    vidPanel = panel;
    vidPanel.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        if(vidPanel.isPlayerVisible()) {
          setBounds();
          vidPanel.repaint();
        }
      }

    });
    timeFormat.setMinimumIntegerDigits(1);
    timeFormat.setMaximumFractionDigits(3);
    timeFormat.setMinimumFractionDigits(3);

    createGUI();
    clipControl = ClipControl.getControl(new VideoClip(null));
    clipControl.addPropertyChangeListener(this);
    getVideoClip().addPropertyChangeListener(this);
    updatePlayButtons(false);
    updateSlider();
    setReadoutTypes("frame time step", "frame"); //$NON-NLS-1$ //$NON-NLS-2$
    refresh();
  }

  /**
   * Sets the video clip.
   *
   * @param clip the video clip
   */
  public void setVideoClip(VideoClip clip) {
    boolean playing = clipControl.isPlaying();
    stop();
    if(getVideoClip()==clip) {
      // save current control state
      boolean looping = clipControl.isLooping();
      double rate = clipControl.getRate();
      double duration = clipControl.getMeanFrameDuration();
      // replace clip control
      clipControl.removePropertyChangeListener(this);
      clipControl.dispose();
      clipControl = ClipControl.getControl(clip);
      clipControl.addPropertyChangeListener(this);
      // set state of new control
      clipControl.setLooping(looping);
      clipControl.setRate(rate);
      clipControl.setFrameDuration(duration);
      if(playing) {
        clipControl.play();
      }
      ClipInspector inspector = getVideoClip().inspector;
      if(inspector!=null) {
        inspector.clipControl = clipControl;
      }
    } 
    else {
      // clean up and replace old clip
      VideoClip oldClip = getVideoClip();
      oldClip.removePropertyChangeListener(this);
      oldClip.hideClipInspector();
      // dispose of old video, if any
      Video video = oldClip.getVideo();
      if(video!=null) {
        video.dispose();
      }
      oldClip.video = null;
      if(clip==null) {
        clip = new VideoClip(null);
      }
      clip.addPropertyChangeListener(this);
      // clean up and replace old clip control
      clipControl.removePropertyChangeListener(this);
      clipControl.dispose();
      clipControl = ClipControl.getControl(clip);
      clipControl.addPropertyChangeListener(this);
      // update display
      setReadoutTypes("frame time step", clip.readoutType); //$NON-NLS-1$
      updatePlayButtons(clipControl.isPlaying());
      updateLoopButton(clipControl.isLooping());
      updateReadout();
      updateSlider();
      firePropertyChange("videoclip", oldClip, clip); //$NON-NLS-1$
      System.gc();
    }
  }

  /**
   * Gets the video clip.
   *
   * @return the video clip
   */
  public VideoClip getVideoClip() {
    return clipControl.getVideoClip();
  }

  /**
   * Gets the current clip control.
   *
   * @return the clip control
   */
  public ClipControl getClipControl() {
    return clipControl;
  }

  /**
   * Sets the readout data types made available to the user.
   *
   * @param types a list of data types. Supported types are "time", "step", "frame".
   * @param typeToSelect the initially selected type
   */
  public void setReadoutTypes(String types, String typeToSelect) {
    // put supported types into map sorted by list order
    TreeMap<Integer, String> map = new TreeMap<Integer, String>();
    String list = types.toLowerCase();
    int i = list.indexOf("time"); //$NON-NLS-1$
    if(i>=0) {
      map.put(new Integer(i), "time"); //$NON-NLS-1$
    }
    i = list.indexOf("step"); //$NON-NLS-1$
    if(i>=0) {
      map.put(new Integer(i), "step"); //$NON-NLS-1$
    }
    i = list.indexOf("frame"); //$NON-NLS-1$
    if(i>=0) {
      map.put(new Integer(i), "frame"); //$NON-NLS-1$
    }
    if(map.isEmpty()) {
      return;
    }
    readoutTypes = map.values().toArray(new String[0]);
    if (typeToSelect==null)
    	typeToSelect =  readoutTypes[0];
    setReadoutType(typeToSelect);
  }

  /**
   * Sets the type of data displayed in the readout.
   *
   * @param type "time", "step", or "frame"
   */
  public void setReadoutType(String type) {
    String name = type.toLowerCase();
    String tip = " "+MediaRes.getString("VideoPlayer.Readout.ToolTip");  //$NON-NLS-1$  //$NON-NLS-2$
    if(name.indexOf("time")>=0) {                                                      //$NON-NLS-1$
      readoutType = "time";                                                            //$NON-NLS-1$
      readout.setToolTipText(MediaRes.getString("VideoPlayer.Readout.ToolTip.Time")+tip);  //$NON-NLS-1$
    } else if(name.indexOf("step")>=0) {                                               //$NON-NLS-1$
      readoutType = "step";                                                            //$NON-NLS-1$
      readout.setToolTipText(MediaRes.getString("VideoPlayer.Readout.ToolTip.Step")+tip);  //$NON-NLS-1$
    } else if(name.indexOf("frame")>=0) {                                              //$NON-NLS-1$
      readoutType = "frame";                                                           //$NON-NLS-1$
      readout.setToolTipText(MediaRes.getString("VideoPlayer.Readout.ToolTip.Frame")+tip); //$NON-NLS-1$
    }
    // add type to readoutTypes if not already present
    boolean isListed = false;
    for(int i = 0; i<readoutTypes.length; i++) {
      isListed = isListed||(readoutTypes[i].equals(readoutType));
    }
    if(!isListed) {
      String[] newList = new String[readoutTypes.length+1];
      newList[0] = readoutType;
      for(int i = 0; i<readoutTypes.length; i++) {
        newList[i+1] = readoutTypes[i];
      }
      readoutTypes = newList;
    }
    getVideoClip().readoutType = readoutType;
    updateReadout();
  }

  /**
   * Plays the clip.
   */
  public void play() {
    clipControl.play();
  }

  /**
   * Stops at the next step.
   */
  public void stop() {
    clipControl.stop();
  }

  /**
   * Steps forward one step.
   */
  public void step() {
  	stop();
    clipControl.step();
  }

  /**
   * Steps back one step.
   */
  public void back() {
  	stop();
    clipControl.back();
  }

  /**
   * Sets the play rate.
   *
   * @param rate the desired rate
   */
  public void setRate(double rate) {
    clipControl.setRate(rate);
  }

  /**
   * Gets the play rate.
   *
   * @return the current rate
   */
  public double getRate() {
    return clipControl.getRate();
  }

  /**
   * Turns on/off looping.
   *
   * @param looping <code>true</code> to turn looping on
   */
  public void setLooping(boolean looping) {
    clipControl.setLooping(looping);
  }

  /**
   * Gets the looping status.
   *
   * @return <code>true</code> if looping is on
   */
  public boolean isLooping() {
    return clipControl.isLooping();
  }

  /**
   * Sets the step number.
   *
   * @param n the desired step number
   */
  public void setStepNumber(int n) {
    clipControl.setStepNumber(n);
  }

  /**
   * Gets the step number.
   *
   * @return the current step number
   */
  public int getStepNumber() {
    return clipControl.getStepNumber();
  }

  /**
   * Gets the current frame number.
   *
   * @return the frame number
   */
  public int getFrameNumber() {
    return clipControl.getFrameNumber();
  }

  /**
   * Gets the current time in milliseconds. Includes the start time defined by
   * the video clip.
   *
   * @return the current time
   */
  public double getTime() {
    return clipControl.getTime()+clipControl.clip.getStartTime();
  }

  /**
   * Gets the start time of the specified step in milliseconds.
   * Includes the start time defined by the video clip.
   *
   * @param stepNumber the step number
   * @return the time
   */
  public double getStepTime(int stepNumber) {
  	if (stepNumber<0 || stepNumber>=clipControl.clip.getStepCount())
  		return Double.NaN;
    return clipControl.getStepTime(stepNumber)+clipControl.clip.getStartTime();
  }

  /**
   * Gets the start time of the specified frame in milliseconds.
   * Includes the start time defined by the video clip.
   *
   * @param frameNumber the frame number
   * @return the time
   */
  public double getFrameTime(int frameNumber) {
    return clipControl.clip.getStartTime()
    		+(frameNumber-clipControl.clip.getStartFrameNumber())
    		*clipControl.getMeanFrameDuration();
  }

  /**
   * Gets the mean step duration in milliseconds for the current video clip.
   *
   * @return the mean step duration
   */
  public double getMeanStepDuration() {
  	double duration = getClipControl().getMeanFrameDuration()*getVideoClip().getStepSize();
    return duration;
  }

  /**
   * Shows or hides the inspector button. The inspector button shows
   * and hides the clip inspector.
   *
   * @param visible <code>true</code> to show the inspector button
   */
  public void setInspectorButtonVisible(final boolean visible) {
    if(visible==inspectorButtonVisible) {
      return;
    }
    Runnable runner = new Runnable() {
      public void run() {
        inspectorButtonVisible = visible;
        if(visible) {
          toolbar.add(inspectorButton);
        } else {
          toolbar.remove(inspectorButton);
        }
        toolbar.revalidate();
      }

    };
    EventQueue.invokeLater(runner);
  }

  /**
   * Shows or hides the looping button.
   *
   * @param visible <code>true</code> to show the looping button
   */
  public void setLoopingButtonVisible(final boolean visible) {
    Runnable runner = new Runnable() {
      public void run() {
        if(visible) {
          toolbar.add(loopButton);
        } else {
          toolbar.remove(loopButton);
        }
        toolbar.revalidate();
      }

    };
    EventQueue.invokeLater(runner);
  }

  /**
   * Responds to property change events. VideoPlayer listens for the
   * following events: "playing", "stepnumber". "frameduration" and "looping"
   * from ClipControl, and "startframe", "stepsize", "stepcount" and "starttime"
   * from VideoClip.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if(name.equals("stepnumber")) {                               // from ClipControl //$NON-NLS-1$
      updateReadout();
      updatePlayButtons(clipControl.isPlaying());
      firePropertyChange("stepnumber", null, e.getNewValue());    // to VideoPanel //$NON-NLS-1$
    } else if(name.equals("frameduration")) {                     // from ClipControl //$NON-NLS-1$
      updateReadout();
      firePropertyChange("frameduration", null, e.getNewValue()); // to VideoPanel //$NON-NLS-1$
    } else if(name.equals("playing")) {                           // from ClipControl //$NON-NLS-1$
      boolean playing = ((Boolean) e.getNewValue()).booleanValue();
      updatePlayButtons(playing);
      firePropertyChange("playing", null, e.getNewValue());    // to VideoPanel //$NON-NLS-1$      
    } else if(name.equals("looping")) {                           // from ClipControl //$NON-NLS-1$
      boolean looping = ((Boolean) e.getNewValue()).booleanValue();
      updateLoopButton(looping);
    } else if(name.equals("rate")) {                              // from ClipControl //$NON-NLS-1$
      rateSpinner.setValue(new Double(getRate()));
    } else if(name.equals("stepcount")) {                         // from VideoClip //$NON-NLS-1$
      updatePlayButtons(clipControl.isPlaying());
      updateReadout();
      updateSlider();
    } else if(name.equals("framecount")) {                         // from VideoClip //$NON-NLS-1$
      updateSlider();
    } else if(name.equals("stepsize")) {                          // from VideoClip //$NON-NLS-1$
      updateReadout();
      updateSlider();
    } else if(name.equals("startframe")) {                        // from VideoClip //$NON-NLS-1$
    	updateReadout();
      updateSlider();
    } else if(name.equals("starttime")) {                         // from VideoClip //$NON-NLS-1$
      updateReadout();
    }
  }

  /**
   * Refreshes the GUI.
   */
  public void refresh() {
    stepButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.StepForward.ToolTip"));       //$NON-NLS-1$
    backButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.StepBack.ToolTip"));          //$NON-NLS-1$
    resetButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Reset.ToolTip")); //$NON-NLS-1$
    inspectorButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.ClipSettings.ToolTip")); //$NON-NLS-1$
    loopButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Looping.ToolTip"));           //$NON-NLS-1$
    setReadoutType(readoutType);
    updatePlayButtons(clipControl.isPlaying());
    updateLoopButton(clipControl.isLooping());
    if(getVideoClip().inspector!=null) {
      getVideoClip().inspector.refresh();
    }
  }
  
  public void setLocale(Locale locale) {
  	timeFormat = NumberFormat.getNumberInstance(locale);
  }
  
  /**
   * Enables and disables this component.
   * 
   * @param enabled true to enable
   */
  @Override
  public void setEnabled(boolean enabled) {
  	super.setEnabled(enabled);
  	disabled = !enabled;
  }

  //_________________ private methods and inner classes __________________

  /**
   * Sets the bounds of this player.
   */
  private void setBounds() {
    toolbar.revalidate();
    height = playButton.getPreferredSize().height+8;
    int y = vidPanel.getHeight()-height;
    int w = vidPanel.getWidth();
    setBounds(0, y, w, height);
    toolbar.revalidate();
  }

  /**
   * Creates the visible components of this player.
   */
  private void createGUI() {
    setLayout(new BorderLayout());
    // create toolbar
    toolbar = new JToolBar();
    toolbar.setFloatable(false);
    add(toolbar, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEtchedBorder());
    playButton = new PlayerButton(playIcon, pauseIcon);
    playButton.setDisabledIcon(grayPlayIcon);
    playButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (disabled || !playButton.isEnabled()) return;
        if(playButton.isSelected()) {
          stop();
        } 
        else {
          play();
        }
      }

    });
    playButton.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	if (disabled || !playButton.isEnabled()) return;
        if(e.getKeyCode()==KeyEvent.VK_SPACE) {
          if(playButton.isSelected()) {
            stop();
          } 
          else {
            play();
          }
        }
      }

    });
    // resetButton
    resetButton = new PlayerButton(resetIcon);
    resetButton.setPressedIcon(resetIcon);
    resetButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (disabled) return;
      	stop();
        clipControl.setStepNumber(0);
        updatePlayButtons(false);
      }
    });

    // create rate spinner
    final double minRate=0.01, maxRate=10;
    final SpinnerNumberModel model = new SpinnerNumberModel(1, minRate, maxRate, 0.1);
    rateSpinner = new JSpinner(model) {
      // override size methods so has same height as buttons
      public Dimension getPreferredSize() {
        return getMinimumSize();
      }
      public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        dim.height = Math.max(playButton.getPreferredSize().height, dim.height);
        dim.width = 5*getFont().getSize()-10*FontSizer.getLevel();
        return dim;
      }
      public Dimension getMaximumSize() {
        return getMinimumSize();
      }
    };
    final JSpinner.NumberEditor editor = new JSpinner.NumberEditor(rateSpinner, "0%"); //$NON-NLS-1$
    editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    editor.getTextField().setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
    rateSpinner.setEditor(editor);
    rateSpinner.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      	Double rate = (Double)rateSpinner.getValue();
      	setRate(rate);
      	model.setStepSize(rate>=2? 0.5: rate>=0.2? 0.1: 0.01);
      }
    });
    editor.getTextField().addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	if (e.getKeyCode()==KeyEvent.VK_ENTER) {
      		double prev = ((Double)rateSpinner.getValue()).doubleValue();
      		try {
      			// remove trailing %, if any
      			String s = editor.getTextField().getText();
      			if (s.endsWith("%")) s = s.substring(0, s.length()-1); //$NON-NLS-1$
						int i = Integer.parseInt(s);
						double rate = Math.max(i/100.0, minRate);
						rate = Math.min(rate, maxRate);
						if (rate!=prev)
							rateSpinner.setValue(new Double(rate));
						else {
							int r = (int)(prev*100);
							editor.getTextField().setText(String.valueOf(r)+"%"); //$NON-NLS-1$
						}
					} catch (NumberFormatException ex) {
						int r = (int)(prev*100);
						editor.getTextField().setText(String.valueOf(r)+"%"); //$NON-NLS-1$
					}
					editor.getTextField().selectAll();
      	}
      }

    });

    // create step button
    stepButton = new PlayerButton(stepIcon);
    stepButton.setDisabledIcon(grayStepIcon);
    stepButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (disabled) return;
      	if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==1) {
        	stop();
      		setStepNumber(getStepNumber()+5);
      	}
      	else step();
      }

    });
    // create back button
    backButton = new PlayerButton(backIcon);
    backButton.setDisabledIcon(grayBackIcon);
    backButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (disabled) return;
      	if ((e.getModifiers() & ActionEvent.SHIFT_MASK)==1) {
        	stop();
      		setStepNumber(getStepNumber()-5);
      	}
      	else back();
      }

    });
    // create mouse listener and add to step and back buttons
    MouseListener stepListener = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
      	if (disabled) return;
        if(e.getSource()==stepButton) {
          firePropertyChange("stepbutton", null, new Boolean(true)); //$NON-NLS-1$
        } else {
          firePropertyChange("backbutton", null, new Boolean(true)); //$NON-NLS-1$
        }
      }
      public void mouseExited(MouseEvent e) {
      	if (disabled) return;
        if(e.getSource()==stepButton) {
          firePropertyChange("stepbutton", null, new Boolean(false)); //$NON-NLS-1$
        } else {
          firePropertyChange("backbutton", null, new Boolean(false)); //$NON-NLS-1$
        }
      }

    };
    stepButton.addMouseListener(stepListener);
    backButton.addMouseListener(stepListener);
    // create listeners
    // inner popup menu listener classes
    readoutListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setReadoutType(e.getActionCommand());
      }
    };
    goToListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	showGoToDialog();
      }
    };
    timeSetListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    		VideoClip clip = getVideoClip();
      	Object response = JOptionPane.showInputDialog(vidPanel, 
      			MediaRes.getString("VideoPlayer.Dialog.SetTime.Message"), //$NON-NLS-1$
      			MediaRes.getString("VideoPlayer.Dialog.SetTime.Title")+" "+getFrameNumber(),  //$NON-NLS-1$ //$NON-NLS-2$
      			JOptionPane.PLAIN_MESSAGE, 
      			null, null, getTime()/1000);
      	if (response!=null) {
        	if (response.equals("")) //$NON-NLS-1$
        		clip.setStartTime(Double.NaN);
        	else try {
						double t = Double.parseDouble(response.toString());
						double t0 = t*1000-clipControl.getTime();
						clip.setStartTime(t0);
					} catch (NumberFormatException ex) {
					}          		
      	}
        ClipInspector inspector = clip.inspector;
        if (inspector!=null && inspector.isVisible()) {
        	inspector.t0Field.setValue(clip.getStartTime()/1000);
        }
      }
    };

    
    // create slider
    slider = new JSlider(0, 0, 0);
    slider.setOpaque(false);
    slider.setMinorTickSpacing(1);
    slider.setSnapToTicks(true);
    slider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        VideoClip clip = getVideoClip();
        int i = slider.getValue(); // frame number
        if (i<clip.getStartFrameNumber()) {
          slider.setValue(clip.getStartFrameNumber());
        }
        else if (i>clip.getEndFrameNumber()) {
          slider.setValue(clip.getEndFrameNumber());
        }
        else {
	        int n = clip.frameToStep(i);
	        if(n!=getStepNumber() && !disabled) {
	          setStepNumber(n);
	        }
	        else if(!clip.includesFrame(i)) {
	          slider.setValue(clip.stepToFrame(n));
	        }
        }
      }

    });
    inLabel = new JLabel(inOutIcon);
    outLabel = new JLabel(inOutIcon);
    sliderLabels = new Hashtable<Integer, JLabel>();
    sliderLabels.put(new Integer(0), inLabel);
    sliderLabels.put(new Integer(9), outLabel);
    slider.setLabelTable(sliderLabels);
    slider.setPaintLabels(true);
    final MouseListener slideMouseListener = slider.getMouseListeners()[0];
    slider.removeMouseListener(slideMouseListener);
    final MouseMotionListener slideMouseMotionListener = slider.getMouseMotionListeners()[0];
    slider.removeMouseMotionListener(slideMouseMotionListener);
    final MouseInputAdapter inOutSetter = new MouseInputAdapter() {
    	
    	float inset = 0;
    	int x;
    	int maxEndFrame;
    	
    	public void mousePressed(MouseEvent e) {
      	if (disabled) return;
      	stop();
  			maxEndFrame = getVideoClip().getEndFrameNumber();
  			if (OSPRuntime.isPopupTrigger(e)) {
          // inner popup menu listener classes
          ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
        			VideoClip clip = getVideoClip();
        			int val = clipControl.getFrameNumber();
            	if ("in".equals(e.getActionCommand())) { //$NON-NLS-1$
              	clip.setStartFrameNumber(val, maxEndFrame);
    						if (clip.inspector != null && clip.inspector.isVisible()) {
    							clip.inspector.startField.setValue(clip.getStartFrameNumber());
    						}
            	}
            	else {
              	clip.setEndFrameNumber(val);
    						if (clip.inspector != null && clip.inspector.isVisible()) {
    							clip.inspector.endField.setValue(clip.getEndFrameNumber());
    						}
            	}
            	refresh();
            }
          };
          // create popup menu and add menu items
          JPopupMenu popup = new JPopupMenu();
          JMenuItem item = new JMenuItem(MediaRes.getString("ClipInspector.Title")+"...");  //$NON-NLS-1$ //$NON-NLS-2$ 
          item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (disabled) return;
              Frame frame = null;
              Container c = vidPanel.getTopLevelAncestor();
              if(c instanceof Frame) {
                frame = (Frame) c;
              }
              ClipInspector inspector = getVideoClip().getClipInspector(clipControl, frame);
              if(inspector.isVisible()) {
                return;
              }
              Point p0 = new Frame().getLocation();
              Point loc = inspector.getLocation();
              if((loc.x==p0.x)&&(loc.y==p0.y)) {
              	// center on screen
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (dim.width - inspector.getBounds().width) / 2;
                int y = (dim.height - inspector.getBounds().height) / 2;
                inspector.setLocation(x, y);
              }
              inspector.initialize();
              inspector.setVisible(true);
            }

          });
          popup.add(item);
          popup.addSeparator();
          boolean showTrim = false;
          if(getVideoClip().getVideo()==null || getVideoClip().getVideo().getFrameCount()==1) {
          	if (getVideoClip().getFrameCount()>getVideoClip().getEndFrameNumber()+1) {
          		showTrim = true;
          	}
          }

          if (showTrim) {
          	String s = MediaRes.getString("VideoPlayer.Slider.Popup.Menu.TrimFrames"); //$NON-NLS-1$
          	item = new JMenuItem(s); 
	          item.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	              getVideoClip().trimFrameCount();
	            }
	          });
	          popup.add(item);
	          popup.addSeparator();
	  			}

    			final int frameNum = clipControl.getFrameNumber();
          if (active==null || active.equals("in")) { //$NON-NLS-1$
          	String s = MediaRes.getString("VideoPlayer.Slider.Popup.Menu.SetIn"); //$NON-NLS-1$
	          s += " ("+frameNum+")"; //$NON-NLS-1$ //$NON-NLS-2$
          	item = new JMenuItem(s); 
	          item.setActionCommand("in"); //$NON-NLS-1$
	          item.addActionListener(listener);
	          popup.add(item);
	  			}
          if (active==null || active.equals("out")) { //$NON-NLS-1$
          	String s = MediaRes.getString("VideoPlayer.Slider.Popup.Menu.SetOut"); //$NON-NLS-1$
	          s += " ("+frameNum+")"; //$NON-NLS-1$ //$NON-NLS-2$
          	item = new JMenuItem(s); 
	          item.setActionCommand("out"); //$NON-NLS-1$
	          item.addActionListener(listener);
	          popup.add(item);
	  			}
  				active = null;
  				
  				boolean includeTimeItems = false;
          for(String type: readoutTypes) {
            if (type.equals("time")) //$NON-NLS-1$
            	includeTimeItems = true;
          }
          if (includeTimeItems) {
	  				// set frame time to zero
	          popup.addSeparator();
	          if (getTime()!=0) {
		        	String s = MediaRes.getString("VideoPlayer.Popup.Menu.SetTimeToZero"); //$NON-NLS-1$
		        	item = new JMenuItem(s); 
		          item.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		            	if (disabled) return;
									double t0 = -clipControl.getTime();
									getVideoClip().setStartTime(t0);
		            }
		          });
		          item.addActionListener(readoutListener);
		          popup.add(item);
	          }
	          // set frame time
	          item = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.Menu.SetTime")); //$NON-NLS-1$
	          item.setActionCommand("time"); //$NON-NLS-1$
	          item.addActionListener(timeSetListener);
	          item.addActionListener(readoutListener);
	          popup.add(item);
          }
          // show popup menu
          popup.show(slider, e.getX(), e.getY());
  			}
  			else if (active==null) {
    			slideMouseListener.mousePressed(e);
    		}
    		else {
    			stop();
    			x = e.getX();
    			if (active=="in") { //$NON-NLS-1$
	  				int start = getVideoClip().getStartFrameNumber();
	  				vidPanel.setMessage(MediaRes.getString("VideoPlayer.InMarker.ToolTip")+": "+start); //$NON-NLS-1$ //$NON-NLS-2$
    			}
    			else if (active=="out") { //$NON-NLS-1$
	  				int end = getVideoClip().getEndFrameNumber();
	  				vidPanel.setMessage(MediaRes.getString("VideoPlayer.OutMarker.ToolTip")+": "+end); //$NON-NLS-1$ //$NON-NLS-2$
    			}
    		}
    	}
    	
    	public void mouseReleased(MouseEvent e) {
      	if (disabled) return;
  			VideoClip clip = getVideoClip();
    		if (active==null)
    			slideMouseListener.mouseReleased(e);
    		else {
  				vidPanel.setMessage(null);
    		}
    		clip.setAdjusting(false);
    	}
    	
    	public void mouseExited(MouseEvent e) {
  			vidPanel.setMouseCursor(Cursor.getDefaultCursor());
      	if (disabled) return;
  			slideMouseListener.mouseExited(e);
        firePropertyChange("slider", null, new Boolean(false)); //$NON-NLS-1$
    	}
    	
    	public void mouseMoved(MouseEvent e) {
  			active = null;
      	if (disabled) return;
    		int yMin = slider.getHeight()-inLabel.getHeight()-2;
    		if (inset==0)
    			inset = slider.getInsets().left + 7;
    		int offset = Math.min(0, getVideoClip().getFrameShift());
    		if (e.getY()>yMin) {
    			VideoClip clip = getVideoClip();
	    		double pixPerFrame = (slider.getWidth()-2*inset)/(clip.getFrameCount()-1);
	    		int start = getVideoClip().getStartFrameNumber();
	    		int x = (int)(inset+(start+offset)*pixPerFrame);
	    		String hint = " "+MediaRes.getString("VideoPlayer.InOutMarker.ToolTip");  //$NON-NLS-1$//$NON-NLS-2$
	    		if (e.getX()<x+8 && e.getX()>x-8) {
	    			active = "in"; //$NON-NLS-1$
	    	    slider.setToolTipText(
	    	    		MediaRes.getString("VideoPlayer.InMarker.ToolTip")+": "+start+hint); //$NON-NLS-1$ //$NON-NLS-2$
	    		}
	    		else {
	    			int end = getVideoClip().getEndFrameNumber();
		    		x = (int)(inset+(end+offset)*pixPerFrame);
		    		if (e.getX()<x+8 && e.getX()>x-8) {
		    			active = "out"; //$NON-NLS-1$
		    	    slider.setToolTipText(
		    	    		MediaRes.getString("VideoPlayer.OutMarker.ToolTip")+": "+end+hint); //$NON-NLS-1$ //$NON-NLS-2$
		    		}
	    		}
    		}
    		if (active==null) {
    			slideMouseMotionListener.mouseMoved(e);
    			vidPanel.setMouseCursor(Cursor.getDefaultCursor());
    	    slider.setToolTipText(MediaRes.getString("VideoPlayer.Slider.ToolTip")); //$NON-NLS-1$
    		}
    		else {
    			vidPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    		}
    	}
    	
    	public void mouseDragged(MouseEvent e) {
      	if (disabled) return;
    		if (active==null) {
	    		slideMouseMotionListener.mouseDragged(e);
	    		return;
    		}
  			VideoClip clip = getVideoClip();
    		clip.setAdjusting(true);
    		boolean increasing = e.getX()>x;
    		x = e.getX();
    		int offset = Math.min(0, getVideoClip().getFrameShift());
    		int val = Math.round((clip.getFrameCount()-1)*(e.getX()-inset)/(slider.getWidth()-2*inset));
    		if (increasing)
    			val = Math.min(val, clip.getFrameCount()-1+getVideoClip().getStepSize());
    		else
    			val = Math.min(val, clip.getFrameCount()-1);
    		val = Math.max(val-offset, 0);
    		if (active.equals("in")) { //$NON-NLS-1$
    			int prevStart = clip.getStartFrameNumber();
        	if (clip.setStartFrameNumber(val, maxEndFrame)) {
        		int newStart = clip.getStartFrameNumber();
	  				vidPanel.setMessage(MediaRes.getString("VideoPlayer.InMarker.ToolTip")+": "+newStart); //$NON-NLS-1$ //$NON-NLS-2$
            // reset start time if needed
            if (!clip.isDefaultStartTime) {
        			double startTime = clip.getStartTime();
            	startTime += (newStart-prevStart)*clipControl.getMeanFrameDuration();
            	clip.setStartTime(startTime);
            }        		
	    			clipControl.setStepNumber(0);
						if (clip.inspector != null && clip.inspector.isVisible()) {
							clip.inspector.startField.setValue(newStart);
							clip.inspector.t0Field.setValue(clip.getStartTime()/1000);
						}
    				updateReadout();
					}
    		}
    		else if (active.equals("out")) { //$NON-NLS-1$
    			if (clip.setEndFrameNumber(val)) {
    				int end = clip.getEndFrameNumber();
	  				vidPanel.setMessage(MediaRes.getString("VideoPlayer.OutMarker.ToolTip")+": "+end); //$NON-NLS-1$ //$NON-NLS-2$
          	clipControl.setStepNumber(clip.getStepCount()-1);
						if (clip.inspector != null && clip.inspector.isVisible()) {
							clip.inspector.endField.setValue(clip.getEndFrameNumber());
						}
					}
    		}
    	}
    };
    slider.addMouseListener(inOutSetter);
    slider.addMouseMotionListener(inOutSetter);
    InputMap im = slider.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap am = SwingUtilities.getUIActionMap(slider);
    if (am != null) {
    	// TODO
    	// SwingJS Slider may not implement KeyEvents yet
    am.put(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)), null);
    am.put(im.get(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0)), null);
    }
    slider.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
      	if (disabled) return;
        switch(e.getKeyCode()) {
           case KeyEvent.VK_PAGE_UP :
             back();
             break;
           case KeyEvent.VK_PAGE_DOWN :
             step();
             break;
        }
      }

    });
    // create readout
    readout = new PlayerButton() {
      // override size methods so has same height as other buttons
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = rateSpinner.getPreferredSize().height;
        return dim;
      }
      public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        dim.height = rateSpinner.getPreferredSize().height;
        return dim;
      }
      public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        dim.height = rateSpinner.getPreferredSize().height;
        return dim;
      }

    };
    readout.setForeground(new Color(204, 51, 51));
    readout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (disabled) return;
        if(readoutTypes.length<2) {
          return;
        }
        // create popup menu and add menu items
        JPopupMenu popup = new JPopupMenu();
        JMenu displayMenu = new JMenu(MediaRes.getString("VideoPlayer.Readout.Menu.Display")); //$NON-NLS-1$
        popup.add(displayMenu);
        JMenuItem item;
        for(int i = 0; i<readoutTypes.length; i++) {
          String type = readoutTypes[i];
          if(type.equals("step")) {                                                         //$NON-NLS-1$
            item = new JCheckBoxMenuItem(MediaRes.getString("VideoPlayer.Readout.MenuItem.Step"));  //$NON-NLS-1$ 
            item.setSelected(type.equals(readoutType));
            item.setActionCommand(type);
            item.addActionListener(readoutListener);
            displayMenu.add(item);
          } else if(type.equals("time")) {                                                  //$NON-NLS-1$
            item = new JCheckBoxMenuItem(MediaRes.getString("VideoPlayer.Readout.MenuItem.Time"));  //$NON-NLS-1$
            item.setSelected(type.equals(readoutType));
            item.setActionCommand(type);
            item.addActionListener(readoutListener);
            displayMenu.add(item);
            
	          popup.addSeparator();
	          if (getTime()!=0) {
		        	String s = MediaRes.getString("VideoPlayer.Popup.Menu.SetTimeToZero"); //$NON-NLS-1$
		        	item = new JMenuItem(s); 
		          item.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		            	if (disabled) return;
									double t0 = -clipControl.getTime();
									getVideoClip().setStartTime(t0);
		            }
		          });
		          item.addActionListener(readoutListener);
		          popup.add(item);
	          }

            item = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.Menu.SetTime")); //$NON-NLS-1$
            item.setActionCommand(type);
            item.addActionListener(timeSetListener);
            item.addActionListener(readoutListener);
            popup.add(item);
            item = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.Menu.GoTo")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
            item.setActionCommand(type);
            item.addActionListener(goToListener);
            popup.add(item);
           } else {
            item = new JCheckBoxMenuItem(MediaRes.getString("VideoPlayer.Readout.MenuItem.Frame")); //$NON-NLS-1$
            item.setSelected(type.equals(readoutType));
            item.setActionCommand(type);
            item.addActionListener(readoutListener);
            displayMenu.add(item);
          }
        }
//      	final VideoClip clip = getVideoClip();
//        final Video video = clip.getVideo();
//        if (video!=null && video.getFrameCount()>1) {
//	        item = new JMenuItem(MediaRes.getString("VideoPlayer.Readout.Menu.Renumber")); //$NON-NLS-1$
//	        item.setActionCommand("frame"); //$NON-NLS-1$
//	        item.addActionListener(new ActionListener() {
//	          public void actionPerformed(ActionEvent e) {
//	          	final int vidFrame = video.getFrameNumber();
//	          	Object response = JOptionPane.showInputDialog(vidPanel, 
//	          			MediaRes.getString("VideoPlayer.Dialog.SetFrameNumber.Message"), //$NON-NLS-1$
//	          			MediaRes.getString("VideoPlayer.Dialog.SetFrameNumber.Title")+" "+vidFrame,  //$NON-NLS-1$ //$NON-NLS-2$
//	          			JOptionPane.PLAIN_MESSAGE, 
//	          			null, null, getFrameNumber());
//	          	if (response!=null) {
//	            	if (!response.equals("")) try { //$NON-NLS-1$
//									int n = Integer.parseInt(response.toString());
//									int shift = vidFrame-n;
//			          	int start = clip.getStartFrameNumber();
//			          	int count = clip.getStepCount();
//			            clip.setFrameShift(shift, start, count);
//			            updateSlider();
//								} catch (NumberFormatException ex) {
//								}          		
//	          	}
//	          }
//	
//	        });
//	        item.addActionListener(readoutListener);
//	        popup.add(item);
//        }
        // show popup menu
        popup.show(readout, 0, readout.getHeight());
      }
    });
    readout.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (OSPRuntime.isPopupTrigger(e))
        	readout.doClick(0);
      }
    });
    // create stepSize button
    stepSizeButton = new PlayerButton() {
      // override size methods so has same height as other buttons
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = rateSpinner.getPreferredSize().height;
        return dim;
      }
      public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        dim.height = rateSpinner.getPreferredSize().height;
        return dim;
      }
      public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        dim.height = rateSpinner.getPreferredSize().height;
        return dim;
      }

    };
    stepSizeButton.setForeground(new Color(204, 51, 51));
    stepSizeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (disabled) return;
        // inner popup menu listener class
        ActionListener listener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
          	int frameNumber = getFrameNumber();
          	VideoClip clip = getVideoClip();
          	try {
							int n = Integer.parseInt(e.getActionCommand());
							clip.setStepSize(n);
						} catch (NumberFormatException ex) {
							String cur = String.valueOf(getVideoClip().getStepSize());
							Object input = JOptionPane.showInputDialog(vidPanel, 
									MediaRes.getString("VideoPlayer.Dialog.StepSize.Message"), //$NON-NLS-1$ 
									MediaRes.getString("VideoPlayer.Dialog.StepSize.Title"), //$NON-NLS-1$ 
									JOptionPane.PLAIN_MESSAGE, 
									null, null, cur);
							if (input!=null) {
								int n = Integer.parseInt(input.toString());
								clip.setStepSize(n);
							}
						}
						setStepNumber(clip.frameToStep(frameNumber));
						if (clip.inspector != null && clip.inspector.isVisible()) {
							clip.inspector.stepSizeField.setValue(clip.getStepSize());
						}
          }
        };
        // create popup menu and add menu items
        JPopupMenu popup = new JPopupMenu();
        for(int i = 1; i<6; i++) {
        	JMenuItem item = new JMenuItem(String.valueOf(i)); 
          item.addActionListener(listener);
          popup.add(item);
        }
        popup.addSeparator();
      	JMenuItem item = new JMenuItem(MediaRes.getString("VideoPlayer.Button.StepSize.Other")); //$NON-NLS-1$ 
        item.addActionListener(listener);
        popup.add(item);
        // show popup menu
        popup.show(stepSizeButton, 0, stepSizeButton.getHeight());
      }
    });
    stepSizeButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (OSPRuntime.isPopupTrigger(e))
        	stepSizeButton.doClick(0);
      }
    });
    // create inspector button
    inspectorButton = new PlayerButton(videoClipIcon);
    inspectorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	if (disabled) return;
        Frame frame = null;
        Container c = vidPanel.getTopLevelAncestor();
        if(c instanceof Frame) {
          frame = (Frame) c;
        }
        ClipInspector inspector = getVideoClip().getClipInspector(clipControl, frame);
        if(inspector.isVisible()) {
          return;
        }
        Point p0 = new Frame().getLocation();
        Point loc = inspector.getLocation();
        if((loc.x==p0.x)&&(loc.y==p0.y)) {
        	// center on screen
          Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
          int x = (dim.width - inspector.getBounds().width) / 2;
          int y = (dim.height - inspector.getBounds().height) / 2;
          inspector.setLocation(x, y);
        }
        inspector.initialize();
        inspector.setVisible(true);
      }

    });
    // create loop button
    loopButton = new PlayerButton(noloopIcon, loopIcon);
    loopButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        setLooping(!loopButton.isSelected());
      }

    });
    loopButton.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_SPACE) {
          setLooping(!loopButton.isSelected());
        }
      }

    });
    // add components to toolbar
    toolbar.add(readout);
    toolbar.add(rateSpinner);
    toolbar.add(resetButton);
    toolbar.add(playButton);
    toolbar.add(slider);
    toolbar.add(backButton);
    toolbar.add(stepSizeButton);
    toolbar.add(stepButton);
    toolbar.add(loopButton);
    if(inspectorButtonVisible) {
      toolbar.add(inspectorButton);
    }
  }

  /**
   * Updates the play buttons based on the specified play state.
   *
   * @param playing <code>true</code> if the video is playing
   */
  private void updatePlayButtons(final boolean playing) {
  	Runnable runner = new Runnable() {
  		public void run() {
  	    int stepCount = getVideoClip().getStepCount();
  	    boolean canPlay = stepCount>1;
  	    playButton.setEnabled(canPlay && (playing || getStepNumber()<stepCount-1));
  	    stepButton.setEnabled(canPlay && (playing || getStepNumber()<stepCount-1));
  	    backButton.setEnabled(canPlay && (playing || getStepNumber()>0));
  	    playButton.setSelected(playing);
  	    if(playing) {
  	      playButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Pause.ToolTip")); //$NON-NLS-1$
  	      playButton.setPressedIcon(pauseIcon);
  	      playButton.setIcon(pauseIcon);
  	    } 
  	    else {
  	      playButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.Play.ToolTip"));  //$NON-NLS-1$
  	      playButton.setPressedIcon(playIcon);
  	      playButton.setIcon(playIcon);
  	    }  			
  		}
  	};
    if(SwingUtilities.isEventDispatchThread()) runner.run();
    else SwingUtilities.invokeLater(runner);
  }

  /**
   * Updates the loop button based on the specified looping state.
   *
   * @param looping <code>true</code> if the video is looping
   */
  private void updateLoopButton(boolean looping) {
    if(looping==loopButton.isSelected()) {
      return;
    }
    loopButton.setSelected(looping);
    if(looping) {
      loopButton.setPressedIcon(loopIcon);
      loopButton.setIcon(loopIcon);
    } else {
      loopButton.setPressedIcon(noloopIcon);
      loopButton.setIcon(noloopIcon);
    }
  }

  /**
   * Updates the slider and readout based on the current step number.
   */
  private void updateReadout() {
    // update slider position
  	int frameNumber = clipControl.getFrameNumber();
    int startFrame = getVideoClip().getStartFrameNumber();
    int endFrame = getVideoClip().getEndFrameNumber();
    if (frameNumber < startFrame)
    	clipControl.setStepNumber(0);
    else if (frameNumber > endFrame)
    	clipControl.setStepNumber(getVideoClip().getStepCount());
    slider.setValue(clipControl.getFrameNumber());
	  // update readout
    int stepNumber = clipControl.getStepNumber();
    String display;
    if(readoutType.equals("step")) {         //$NON-NLS-1$
      if(stepNumber<10) {
        display = "00"+stepNumber;           //$NON-NLS-1$
      } else if(stepNumber<100) {
        display = "0"+stepNumber;            //$NON-NLS-1$
      } else {
        display = ""+stepNumber;             //$NON-NLS-1$
      }
    } else if(readoutType.equals("frame")) { //$NON-NLS-1$
      int n = clipControl.getFrameNumber();
      if(n<10) {
        display = "00"+n;                    //$NON-NLS-1$
      } else if(n<100) {
        display = "0"+n;                     //$NON-NLS-1$
      } else {
        display = ""+n;                      //$NON-NLS-1$
      }
    } else {
    	// default readout is time
    	// set formatting based on mean step duration
    	if (timeFormat instanceof DecimalFormat) {
    		DecimalFormat format = (DecimalFormat)timeFormat;
	    	double dur = getMeanStepDuration(); // millisec
	    	if (dur<10) {
	    		format.applyPattern("0.00E0"); //$NON-NLS-1$
	    	}
	    	else if (dur<100) {
	    		format.applyPattern(NumberField.DECIMAL_3_PATTERN);
	    	}
	    	else if (dur<1000) {
	    		format.applyPattern(NumberField.DECIMAL_2_PATTERN);
	    	}
	    	else if (dur<10000) {
	    		format.applyPattern(NumberField.DECIMAL_1_PATTERN);
	    	}
	    	else {
	    		format.applyPattern("0.00E0"); //$NON-NLS-1$
	    	}
    	}
      display = timeFormat.format(getTime()/1000.0);
    }
    readout.setText(display);
    // update rate spinner
    rateSpinner.setValue(getRate());
    // update stepSizeButton
    stepSizeButton.setText(""+getVideoClip().getStepSize()); //$NON-NLS-1$
    // set font sizes
    FontSizer.setFonts(readout, FontSizer.getLevel());
    FontSizer.setFonts(rateSpinner, FontSizer.getLevel());
    FontSizer.setFonts(stepSizeButton, FontSizer.getLevel());
    // update tooltips
    stepSizeButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.StepSize.ToolTip")); //$NON-NLS-1$
    rateSpinner.setToolTipText(MediaRes.getString("VideoPlayer.Spinner.Rate.ToolTip")); //$NON-NLS-1$
    // if at last step, update play button
    if (stepNumber==getVideoClip().getStepCount()-1)
    	updatePlayButtons(clipControl.isPlaying());
  }

  /**
   * Updates the slider based on the current in and out points.
   */
  private void updateSlider() {
    // update slider
    VideoClip clip = getVideoClip();
    slider.setMinimum(Math.max(0, -clip.getFrameShift()));
    slider.setMaximum(slider.getMinimum()+clip.getFrameCount()-1);
    sliderLabels.clear();
    sliderLabels.put(new Integer(clip.getStartFrameNumber()), inLabel);
    sliderLabels.put(new Integer(clip.getEndFrameNumber()), outLabel);
	  slider.repaint();  	
  }
  
  public void showGoToDialog() {
  	if (goToDialog==null) {
    	goToDialog = new GoToDialog(this);
    	// center dialog on videoPanel view
    	Container c = VideoPlayer.this.getParent();
    	while (c!=null) {
    		if (c instanceof JSplitPane) {
          Dimension dim = c.getSize();
          Point p = c.getLocationOnScreen();
          int x = (dim.width - goToDialog.getBounds().width) / 2;
          int y = (dim.height - goToDialog.getBounds().height) / 2;
          goToDialog.setLocation(p.x+x, p.y+y);
          break;
    		}
      	c = c.getParent();	      		
    	}
  	}
  	else {
  		goToDialog.setPlayer(this);
  	}
  	goToDialog.setVisible(true);

  }
  
  /**
   * PlayerButton inner class
   */
  protected class PlayerButton extends JButton {
  	
    /**
     * Constructs a PlayerButton.
     */
    public PlayerButton() {
  		setOpaque(false);
  		setBorderPainted(false);
      addMouseListener(new MouseAdapter() {
      	public void mouseEntered(MouseEvent e) {
      		setBorderPainted(true);
      	}

      	public void mouseExited(MouseEvent e) {
      		setBorderPainted(false);
      	}

      });
    }
    
    /**
     * Constructs a PlayerButton with an icon.
     *
     * @param icon the icon
     */
    public PlayerButton(Icon icon) {
  		this();
  		setIcon(icon);
    }
    
    /**
     * Constructs a PlayerButton with icons for selected and unselected states.
     *
     * @param off the unselected state icon
     * @param on the selected state icon
     */
    public PlayerButton(Icon off, Icon on) {
  		this();
  		setIcon(off);
  		setSelectedIcon(on);
    }
    
  }
  
  /**
   * GoToDialog inner class
   */
  protected static class GoToDialog extends JDialog {
  	
  	static HashMap<VideoPlayer, String[]> prev = new HashMap<VideoPlayer, String[]>();
  	
  	VideoPlayer player;
    JButton okButton, cancelButton;
    JLabel frameLabel, timeLabel, stepLabel;
    JTextField frameField, timeField, stepField;
    KeyAdapter keyListener;
    FocusAdapter focusListener;
    String prevFrame, prevTime, prevStep;
    Color error_red = new Color(255, 140, 160);
  	
  	public GoToDialog(VideoPlayer vidPlayer) {
  		super(JOptionPane.getFrameForComponent(vidPlayer.vidPanel), true);
  		setPlayer(vidPlayer);
  		JPanel contentPane = new JPanel(new BorderLayout());
  		setContentPane(contentPane);
      // create buttons
  		okButton = new JButton(DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
  		okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	String input = stepField.getText();
        	if (input!=null && !input.equals("")) try { //$NON-NLS-1$
  					int n = Integer.parseInt(input);
  					player.clipControl.setStepNumber(n);
  					player.refresh();
  				} catch (NumberFormatException ex) {
  				}          		
          setVisible(false);
        }
      });
      cancelButton = new JButton(DisplayRes.getString("GUIUtils.Cancel")); //$NON-NLS-1$
      cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setVisible(false);
        }
      });

      // create key and focus listeners
      keyListener = new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
        	JTextField field = (JTextField)e.getSource();
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          	okButton.doClick(0);
          } 
          else {
          	field.setBackground(Color.white);
          }
        }
        public void keyReleased(KeyEvent e) {
        	JTextField field = (JTextField)e.getSource();
          if(e.getKeyCode()!=KeyEvent.VK_ENTER) {
          	setValues(field);
          }
        }

      };
      focusListener = new FocusAdapter() {
        public void focusLost(FocusEvent e) {
    			JTextField field = (JTextField)e.getSource();
    			field.setBackground(Color.white);
        }
      };

      // create input fields and labels
      frameField = new JTextField(6);
      frameField.addKeyListener(keyListener);
      frameField.addFocusListener(focusListener);
      timeField = new JTextField(6);
      timeField.addKeyListener(keyListener);
      timeField.addFocusListener(focusListener);
      stepField = new JTextField(6);
      stepField.addKeyListener(keyListener);
      stepField.addFocusListener(focusListener);
      frameLabel = new JLabel();
      timeLabel = new JLabel();
      stepLabel = new JLabel();
      
      // assemble 
      Box box = Box.createVerticalBox();
      JPanel framePanel = new JPanel();
      framePanel.add(frameLabel);
      framePanel.add(frameField);
      box.add(framePanel);
      JPanel timePanel = new JPanel();
      timePanel.add(timeLabel);
      timePanel.add(timeField);
      box.add(timePanel);
      JPanel stepPanel = new JPanel();
      stepPanel.add(stepLabel);
      stepPanel.add(stepField);
      box.add(stepPanel);
      contentPane.add(box, BorderLayout.CENTER);
      
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(okButton);
      buttonPanel.add(cancelButton);
      contentPane.add(buttonPanel, BorderLayout.SOUTH);
      refreshGUI();
      pack();
  	}
  	
  	public void refreshGUI() {
  		setTitle(MediaRes.getString("VideoPlayer.GoToDialog.Title")); //$NON-NLS-1$
  		okButton.setText(DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
  		cancelButton.setText(DisplayRes.getString("GUIUtils.Cancel")); //$NON-NLS-1$
      frameLabel.setText(MediaRes.getString("VideoPlayer.Readout.MenuItem.Frame")+":"); //$NON-NLS-1$ //$NON-NLS-2$
      timeLabel.setText(MediaRes.getString("VideoPlayer.Readout.MenuItem.Time")+" (s):"); //$NON-NLS-1$ //$NON-NLS-2$
      stepLabel.setText(MediaRes.getString("VideoPlayer.Readout.MenuItem.Step")+":"); //$NON-NLS-1$ //$NON-NLS-2$
      // set label sizes
      ArrayList<JLabel> labels = new ArrayList<JLabel>();
      labels.add(frameLabel);
      labels.add(timeLabel);
      labels.add(stepLabel);
      FontRenderContext frc = new FontRenderContext(null, false, false); 
      Font font = frameLabel.getFont();
      //display panel labels
      int w = 0;
      for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
        JLabel next = it.next();
        Rectangle2D rect = font.getStringBounds(next.getText()+" ", frc); //$NON-NLS-1$
        w = Math.max(w, (int) rect.getWidth()+1);
      }
      Dimension labelSize = new Dimension(w, 20);
      for(Iterator<JLabel> it = labels.iterator(); it.hasNext(); ) {
        JLabel next = it.next();
        next.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        next.setPreferredSize(labelSize);
        next.setHorizontalAlignment(SwingConstants.TRAILING);
      }
  		
  	}
  	
  	public void setPlayer(VideoPlayer vidPlayer) {
  		if (player!=null && player!=vidPlayer) {
  			prev.put(player, new String[] {prevFrame, prevTime, prevStep});
  			String[] former = prev.get(vidPlayer);
  			if (former!=null) {
  				prevFrame = former[0];
  				prevTime = former[1];
  				prevStep = former[2];
  				frameField.setText(prevFrame);
  				timeField.setText(prevTime);
  				stepField.setText(prevStep);
  			}
  		}
  		player = vidPlayer;
  	}
  	
  	private void setValues(JTextField inputField) {
    	String input = inputField.getText();
    	if ("".equals(input)) { //$NON-NLS-1$
				prevFrame = ""; //$NON-NLS-1$
				prevTime = ""; //$NON-NLS-1$
				prevStep = ""; //$NON-NLS-1$
    	}
    	else {
	  		VideoClip clip = player.getVideoClip();
	  		if (inputField==frameField) {
					prevFrame = input;
	      	try {
						int frameNum = Integer.parseInt(input);
						int entered = frameNum;
						frameNum = Math.max(clip.getFirstFrameNumber(), frameNum);
						frameNum = Math.min(clip.getEndFrameNumber(), frameNum);
						int stepNum = clip.frameToStep(frameNum);
						frameNum = clip.stepToFrame(stepNum);
						double t = player.getStepTime(stepNum)/1000;
						prevTime = timeFormat.format(t);
						prevStep = String.valueOf(stepNum);
						if (frameNum!=entered) {
							frameField.setBackground(error_red);
						}
					} catch (NumberFormatException ex) {
						prevTime = ""; //$NON-NLS-1$
						prevStep = ""; //$NON-NLS-1$
						frameField.setBackground(error_red);
					}          		  			
	  		}
	  		else if (inputField==timeField) {
					prevTime = input;
	      	try {
	      		input = input.replaceAll(",", "."); //$NON-NLS-1$ //$NON-NLS-2$
						double t = Double.valueOf(input)*1000; // millisec
						// find step number
						double dt = player.getMeanStepDuration();
						int n = (int)((t-clip.getStartTime())/dt);
						int stepNum = Math.max(0, n);
						stepNum = Math.min(stepNum, clip.getStepCount()-1);
						int frameNum = clip.stepToFrame(stepNum);
						double tmin = player.getFrameTime(clip.getFirstFrameNumber());
						double tmax = player.getFrameTime(clip.getLastFrameNumber());
						if (t<tmin || t>tmax) {
							timeField.setBackground(error_red);
						}
						prevFrame = String.valueOf(frameNum);
						prevStep = String.valueOf(stepNum);
					} catch (NumberFormatException ex) {
						prevFrame = ""; //$NON-NLS-1$
						prevStep = ""; //$NON-NLS-1$
						timeField.setBackground(error_red);
					}     
	  		}
	  		else {
	      	try {
						int stepNum = Integer.parseInt(input);
						stepNum = Math.max(0, stepNum);
						stepNum = Math.min(clip.getStepCount()-1, stepNum);
						int frameNum = clip.stepToFrame(stepNum);
						double t = player.getStepTime(stepNum)/1000;
						prevFrame = String.valueOf(frameNum);
						prevTime = timeFormat.format(t);
						prevStep = String.valueOf(stepNum);
					} catch (NumberFormatException ex) {
					}          		  			
	  		}
    	}
			frameField.setText(prevFrame);
			timeField.setText(prevTime);
			stepField.setText(prevStep);
  	}
  	
  	public void setVisible(boolean vis) {
  		if (vis) {
  	    prevFrame = ""; //$NON-NLS-1$
  	    prevTime = ""; //$NON-NLS-1$
  	    prevStep = ""; //$NON-NLS-1$
  			frameField.setText(prevFrame);
  			timeField.setText(prevTime);
  			stepField.setText(prevStep);
  			frameField.setBackground(Color.white);
  			timeField.setBackground(Color.white);
  			stepField.setBackground(Color.white);
  			FontSizer.setFonts(this, FontSizer.getLevel());
  	    refreshGUI();
  	    pack();
  		}
  		super.setVisible(vis);
  	}
  	
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
 * Copyright (c) 2017  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
