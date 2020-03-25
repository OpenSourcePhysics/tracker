/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ToolsRes;

/**
 * MessageFrame displays text messages.
 *
 * The static MessageFrame object displays logger messages when a program is run as a applet.
 *
 * @author W. Christian
 * @version 1.0
 */
public class MessageFrame extends JFrame {
  static final Color DARK_GREEN = new Color(0, 128, 0), DARK_BLUE = new Color(0, 0, 128), DARK_RED = new Color(128, 0, 0);
  static Style black, red, blue, green, magenta, gray;
  static volatile MessageFrame APPLET_MESSAGEFRAME;
  //static Level levelOSP = Level.CONFIG;
  static int levelOSP = Level.CONFIG.intValue();
  private static int SEVERE = Level.SEVERE.intValue(), WARNING = Level.WARNING.intValue(), INFO = Level.INFO.intValue(), CONFIG = Level.CONFIG.intValue(), FINE = Level.FINE.intValue(), FINER = Level.FINER.intValue(), FINEST = Level.FINEST.intValue();
  private static ArrayList<JRadioButtonMenuItem> buttonList = new ArrayList<JRadioButtonMenuItem>();
  private JTextPane textPane = new JTextPane();

  private MessageFrame() {
    // create the panel, text pane and scroller
    setTitle(ControlsRes.getString("MessageFrame.DefaultTitle")); //$NON-NLS-1$
    JPanel logPanel = new JPanel(new BorderLayout());
    logPanel.setPreferredSize(new Dimension(480, 240));
    setContentPane(logPanel);
    logPanel.setPreferredSize(new Dimension(200, 300));
    logPanel.setMinimumSize(new Dimension(100, 100));
    textPane.setEditable(false);
    textPane.setAutoscrolls(true);
    black = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    red = textPane.addStyle("red", black); //$NON-NLS-1$
    StyleConstants.setForeground(red, DARK_RED);
    blue = textPane.addStyle("blue", black); //$NON-NLS-1$
    StyleConstants.setForeground(blue, DARK_BLUE);
    green = textPane.addStyle("green", black); //$NON-NLS-1$
    StyleConstants.setForeground(green, DARK_GREEN);
    magenta = textPane.addStyle("magenta", black); //$NON-NLS-1$
    StyleConstants.setForeground(magenta, Color.MAGENTA);
    gray = textPane.addStyle("gray", black); //$NON-NLS-1$
    StyleConstants.setForeground(gray, Color.GRAY);
    JScrollPane textScroller = new JScrollPane(textPane);
    textScroller.setWheelScrollingEnabled(true);
    logPanel.add(textScroller, BorderLayout.CENTER);
    //FontSizer.setFonts(this, FontSizer.getLevel());
    //textPane.setFont(textPane.getFont().deriveFont(16F));
    pack();
  }

  /**
   * Shows the static APPLET_MESSAGEFRAME that is being used to show logger messages in applet mode.
   * @param b boolean
   */
  public static JFrame showLog(boolean b) {
    if((APPLET_MESSAGEFRAME==null)||!APPLET_MESSAGEFRAME.isDisplayable()) {
      createAppletMessageFrame();
    }
    APPLET_MESSAGEFRAME.setVisible(b);
    return APPLET_MESSAGEFRAME;
  }

  private static void createAppletMessageFrame() {
    APPLET_MESSAGEFRAME = new MessageFrame();
    APPLET_MESSAGEFRAME.setSize(300, 200);
    APPLET_MESSAGEFRAME.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    JMenuBar menuBar = new JMenuBar();
    APPLET_MESSAGEFRAME.setJMenuBar(menuBar);
    final JMenu editMenu = new JMenu(ControlsRes.getString("MessageFrame.Edit_menu")); //$NON-NLS-1$
    menuBar.add(editMenu);
    final JMenuItem clearItem = new JMenuItem(ControlsRes.getString("MessageFrame.Clear_menu_item")); //$NON-NLS-1$
    editMenu.add(clearItem);
    clearItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        APPLET_MESSAGEFRAME.textPane.setText(""); //$NON-NLS-1$
      }

    });
    final JMenu levelMenu = new JMenu(ControlsRes.getString("MessageFrame.Level_menu")); //$NON-NLS-1$
    menuBar.add(levelMenu);
    ButtonGroup menubarGroup = new ButtonGroup();
    for(int i = 0; i<OSPLog.levels.length; i++) {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(OSPLog.levels[i].getName());
      buttonList.add(item);
      levelMenu.add(item, 0);
      menubarGroup.add(item);
      if(levelOSP==OSPLog.levels[i].intValue()) {
        item.setSelected(true);
      }
      item.setActionCommand(OSPLog.levels[i].getName());
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setLevel(Level.parse(e.getActionCommand()));
        }

      });
    }
    
	FontSizer.setFonts(APPLET_MESSAGEFRAME.textPane, FontSizer.getLevel());
	FontSizer.addPropertyChangeListener("level", new PropertyChangeListener() { //$NON-NLS-1$
		public void propertyChange(PropertyChangeEvent e) {
			int level = ((Integer) e.getNewValue()).intValue();
			FontSizer.setFonts(menuBar, level);
			FontSizer.setFonts(APPLET_MESSAGEFRAME.textPane, level);
		}

	});
    ToolsRes.addPropertyChangeListener("locale", new PropertyChangeListener() {           //$NON-NLS-1$
      public void propertyChange(PropertyChangeEvent e) {
        APPLET_MESSAGEFRAME.setTitle(ControlsRes.getString("MessageFrame.DefaultTitle")); //$NON-NLS-1$
        editMenu.setText(ControlsRes.getString("MessageFrame.Edit_menu"));                //$NON-NLS-1$
        clearItem.setText(ControlsRes.getString("MessageFrame.Clear_menu"));              //$NON-NLS-1$
        levelMenu.setText(ControlsRes.getString("MessageFrame.Level_menu"));              //$NON-NLS-1$
      }

    });
  }

  /**
   * Gets the visible property.
   *
   * @return boolean
   */
  public static boolean isLogVisible() {
    if(APPLET_MESSAGEFRAME==null) {
      return false;
    }
    return APPLET_MESSAGEFRAME.isVisible();
  }

  /**
   * Clears the text.
   */
  public static void clear() {
    if(APPLET_MESSAGEFRAME!=null) {
      APPLET_MESSAGEFRAME.textPane.setText(""); //$NON-NLS-1$
    }
  }

  /**
   * Sets the logger level;
   * @param level Level
   */
  public static void setLevel(Level level) {
    levelOSP = level.intValue();
    for(int i = 0, n = Math.min(OSPLog.levels.length, buttonList.size()); i<n; i++) {
      if(levelOSP==OSPLog.levels[i].intValue()) {
        (buttonList.get(i)).setSelected(true);
      }
    }
  }

  /**
   * Gets the logger level value.
   * @return the current level value
   */
  public static int getLevelValue() {
    return levelOSP;
  }

  /**
   * Logs an severe error message.
   * @param msg String
   */
  public static void severe(String msg) {
    if(levelOSP<=SEVERE) {
      appletLog(msg, MessageFrame.red);
    }
  }

  /**
   * Logs a warning message.
   * @param msg String
   */
  public static void warning(String msg) {
    if(levelOSP<=WARNING) {
      appletLog(msg, MessageFrame.red);
    }
  }

  /**
   * Logs an information message.
   * @param msg String
   */
  public static void info(String msg) {
    if(levelOSP<=INFO) {
      appletLog(msg, MessageFrame.black);
    }
  }

  /**
   * Logs a configuration message.
   * @param msg String
   */
  public static void config(String msg) {
    if(levelOSP<=CONFIG) {
      appletLog(msg, MessageFrame.green);
    }
  }

  /**
   * Logs a fine debugging message.
   * @param msg String
   */
  public static void fine(String msg) {
    if(levelOSP<=FINE) {
      appletLog(msg, MessageFrame.blue);
    }
  }

  /**
   * Logs a finer debugging message.
   * @param msg String
   */
  public static void finer(String msg) {
    if(levelOSP<=FINER) {
      appletLog(msg, MessageFrame.blue);
    }
  }

  /**
   * Logs a finest debugging message.
   * @param msg String
   */
  public static void finest(String msg) {
    if(levelOSP<=FINEST) {
      appletLog(msg, MessageFrame.blue);
    }
  }

  private static void appletLog(final String msg, final Style style) {
    if((APPLET_MESSAGEFRAME==null)||!APPLET_MESSAGEFRAME.isDisplayable()) {
      createAppletMessageFrame();
    }

    Runnable refreshText = new Runnable() {
      public synchronized void run() {
        try {
          Document doc = APPLET_MESSAGEFRAME.textPane.getDocument();
          doc.insertString(doc.getLength(), msg+'\n', style);
          // scroll to display new message
          Rectangle rect = APPLET_MESSAGEFRAME.textPane.getBounds();
          rect.y = rect.height;
          APPLET_MESSAGEFRAME.textPane.scrollRectToVisible(rect);
        } catch(BadLocationException ex) {
          System.err.println(ex);
        }
      }

    };
    if(SwingUtilities.isEventDispatchThread()) {
      refreshText.run();
    } else {
      SwingUtilities.invokeLater(refreshText);
    }
  }
  /*
   * public static void main(String[] args) {
   *   MessageFrame.fine("test fine");
   *   APPLET_LOG.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   * }
   */

}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
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
