/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.opensourcephysics.controls.Cryptic;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLTreePanel;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This provides a GUI for encrypting and decrypting XMLControls.
 *
 * @author Douglas Brown
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EncryptionTool extends JFrame implements Tool {
  // static fields
  private static Dimension dim = new Dimension(720, 500);
  // instance fields
  private XMLTreePanel treePanel;
  private JPanel contentPane = new JPanel(new BorderLayout());
  private JobManager jobManager = new JobManager(this);
  private JTextField passwordField;
  private JCheckBox encryptedCheckBox;
  private JCheckBox previewCheckBox;
  private String fileName;
  private JMenuItem openItem;
  private JMenuItem saveItem;
  private JMenuItem saveAsItem;
  private JLabel passwordLabel;
  private JMenu fileMenu;
  private JMenu helpMenu;
  private JMenuItem exitItem;
  private JMenuItem logItem;
  private JMenuItem aboutItem;
  private Icon openIcon;
  private JButton openButton;
  private Icon saveIcon;
  private JButton saveButton;

  /**
   * A shared encryption tool.
   */
  private static final EncryptionTool ENCRYPTION_TOOL = new EncryptionTool();

  /**
   * Gets the shared EncryptionTool.
   *
   * @return the shared EncryptionTool
   */
  public static EncryptionTool getTool() {
    return ENCRYPTION_TOOL;
  }

  /**
   * Constructs a blank EncryptionTool.
   */
  public EncryptionTool() {
    String name = "EncryptionTool"; //$NON-NLS-1$
    setName(name);
    createGUI();
    refreshGUI();
    Toolbox.addTool(name, this);
    // Toolbox.addRMITool(name, this);
  }

  /**
   * Constructs a EncryptionTool and opens the specified xml file.
   *
   * @param fileName the name of the xml file
   */
  public EncryptionTool(String fileName) {
    this();
    open(fileName);
  }

  /**
   * Opens an xml file specified by name.
   *
   * @param fileName the file name
   * @return the file name, if successfully opened
   */
  public String open(String fileName) {
    OSPLog.fine("opening "+fileName); //$NON-NLS-1$
    // read the file into an XML control
    XMLControlElement control = new XMLControlElement();
    control.setDecryptPolicy(XMLControlElement.NEVER_DECRYPT);
    control.read(fileName);
    if(control.failedToRead()) {
      return null;
    }
    String pass = control.getPassword();
    if(pass==null) {
      passwordField.setText(null);
      displayXML(control);
      encryptedCheckBox.setEnabled(true);
    } else if(passwordField.getText().equals(pass)) {
      displayXML(decrypt(control));
      encryptedCheckBox.setEnabled(true);
    } else {
      displayXML(control);
      encryptedCheckBox.setEnabled(false);
    }
    this.fileName = fileName;
    refreshGUI();
    return fileName;
  }

  /**
   * Sends a job to this tool and specifies a tool to reply to.
   *
   * @param job the Job
   * @param replyTo the tool to notify when the job is complete (may be null)
   * @throws RemoteException
   */
  public void send(Job job, Tool replyTo) throws RemoteException {
    // read xml into XMLControl and display the control
    XMLControlElement control = new XMLControlElement();
    control.setDecryptPolicy(XMLControlElement.NEVER_DECRYPT);
    control.readXML(job.getXML());
    if(control.failedToRead()) {
      return;
    }
    String pass = control.getPassword();
    if(pass==null) {
      passwordField.setText(null);
      displayXML(control);
    } else if(passwordField.getText().equals(pass)) {
      displayXML(decrypt(control));
    } else {
      displayXML(control);
    }
    fileName = null;
    refreshGUI();
    // log the job with the job manager
    jobManager.log(job, replyTo);
  }

  /**
   * Refreshes the GUI.
   */
  public void refreshGUI() {
    String title = ToolsRes.getString("EncryptionTool.Title"); //$NON-NLS-1$
    if(fileName!=null) {
      title += ": "+fileName; //$NON-NLS-1$
    }
    setTitle(title);
    openButton.setToolTipText(ToolsRes.getString("EncryptionTool.Button.Open.ToolTip"));               //$NON-NLS-1$
    saveButton.setToolTipText(ToolsRes.getString("EncryptionTool.Button.Save.ToolTip"));               //$NON-NLS-1$
    passwordLabel.setText(ToolsRes.getString("EncryptionTool.Label.Password"));                        //$NON-NLS-1$
    passwordField.setToolTipText(ToolsRes.getString("EncryptionTool.PasswordField.ToolTip"));          //$NON-NLS-1$
    encryptedCheckBox.setText(ToolsRes.getString("EncryptionTool.CheckBox.Encrypted"));                //$NON-NLS-1$
    encryptedCheckBox.setToolTipText(ToolsRes.getString("EncryptionTool.CheckBox.Encrypted.ToolTip")); //$NON-NLS-1$
    previewCheckBox.setText(ToolsRes.getString("EncryptionTool.CheckBox.Preview"));                    //$NON-NLS-1$
    previewCheckBox.setToolTipText(ToolsRes.getString("EncryptionTool.CheckBox.Preview.ToolTip"));     //$NON-NLS-1$
    fileMenu.setText(ToolsRes.getString("EncryptionTool.Menu.File"));                                  //$NON-NLS-1$
    openItem.setText(ToolsRes.getString("EncryptionTool.MenuItem.Open"));                              //$NON-NLS-1$
    saveItem.setText(ToolsRes.getString("EncryptionTool.MenuItem.Save"));                              //$NON-NLS-1$
    saveAsItem.setText(ToolsRes.getString("EncryptionTool.MenuItem.SaveAs"));                          //$NON-NLS-1$
    exitItem.setText(ToolsRes.getString("EncryptionTool.MenuItem.Exit"));                              //$NON-NLS-1$
    helpMenu.setText(ToolsRes.getString("EncryptionTool.Menu.Help"));                                  //$NON-NLS-1$
    logItem.setText(ToolsRes.getString("EncryptionTool.MenuItem.Log"));                                //$NON-NLS-1$
    aboutItem.setText(ToolsRes.getString("EncryptionTool.MenuItem.About"));                            //$NON-NLS-1$
    saveButton.setEnabled(encryptedCheckBox.isEnabled());
    saveItem.setEnabled(encryptedCheckBox.isEnabled());
    saveAsItem.setEnabled(encryptedCheckBox.isEnabled());
    XMLControlElement control = getCurrentControl();
    encryptedCheckBox.setSelected((control!=null)&&(control.getPassword()!=null));
    passwordLabel.setEnabled(encryptedCheckBox.isSelected());
    passwordField.setEnabled(encryptedCheckBox.isSelected());
    previewCheckBox.setEnabled(encryptedCheckBox.isEnabled()&&encryptedCheckBox.isSelected());
    previewCheckBox.setSelected((control!=null)&&(control.getObjectClass()==Cryptic.class));
  }

  /**
   * Main entry point when used as application.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    EncryptionTool tool = getTool();
    tool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    tool.open("Untitled.xset"); //$NON-NLS-1$
    tool.setVisible(true);
  }

  // ______________________________ private methods _____________________________

  /**
   * Gets the currently displayed XMLControlElement.
   *
   * @return the XMLControlElement
   */
  private XMLControlElement getCurrentControl() {
    if(treePanel==null) {
      return null;
    }
    XMLControl control = treePanel.getControl();
    if(control instanceof XMLControlElement) {
      return(XMLControlElement) control;
    }
    return null;
  }

  /**
   * Displays an XMLControlElement.
   *
   * @param control the XMLControlElement
   */
  private void displayXML(XMLControlElement control) {
    if(treePanel!=null) {
      contentPane.remove(treePanel);
    }
    treePanel = new XMLTreePanel(control, false);
    contentPane.add(treePanel, BorderLayout.CENTER);
    validate();
    //    treePanel.setSelectedNode("xml_password"); //$NON-NLS-1$
    refreshGUI();
  }

  /**
   * Sets the password of the currently displayed control.
   *
   * @param password the password
   */
  private void setPassword(String password) {
    XMLControlElement control = getCurrentControl();
    if(control==null) {
      return;
    }
    String pass = control.getPassword();
    if(!encryptedCheckBox.isEnabled()) {
      boolean verified = password.equals(pass);
      if(verified) {
        displayXML(decrypt(control));
        encryptedCheckBox.setEnabled(true);
      } else {
        Toolkit.getDefaultToolkit().beep();
        OSPLog.fine("Bad password: "+password);                  //$NON-NLS-1$
      }
    } else if(control.getObjectClass()==Cryptic.class) {
      // decrypt control, change password, and re-encrypt
      XMLControlElement temp = decrypt(control);
      temp.setPassword(password);
      temp = encrypt(temp);
      control.setValue("cryptic", temp.getString("cryptic"));    //$NON-NLS-1$ //$NON-NLS-2$
      treePanel.refresh();
    } else {
      // change password
      if(password.equals("")&&!encryptedCheckBox.isSelected()) { //$NON-NLS-1$
        password = null;
      }
      control.setPassword(password);
      treePanel.refresh();
      //      treePanel.setSelectedNode("xml_password"); //$NON-NLS-1$
    }
    refreshGUI();
  }

  /**
   * Encrypts the specified XMLcontrol using a Cryptic object.
   *
   * @param control the XMLControl
   * @return the encrypted control
   */
  private XMLControlElement encrypt(XMLControlElement control) {
    // return controls that are already encrypted
    if(control.getObjectClass()==Cryptic.class) {
      return control;
    }
    // encrypt control and set its password
    String xml = control.toXML();
    Cryptic cryptic = new Cryptic(xml);
    XMLControlElement encrypted = new XMLControlElement(cryptic);
    encrypted.setPassword(control.getPassword());
    return encrypted;
  }

  /**
   * Decrypts the specified XMLcontrol.
   *
   * @param control the XMLControl
   * @return the decrypted control
   */
  private XMLControlElement decrypt(XMLControlElement control) {
    if(control.getObjectClass()!=Cryptic.class) {
      return control;
    }
    Cryptic cryptic = (Cryptic) control.loadObject(null);
    // get the decrypted xml, make new control and set password
    String xml = cryptic.decrypt();
    XMLControlElement decrypted = new XMLControlElement(xml);
    return decrypted;
  }

  /**
   * Opens an xml file selected with a chooser.
   *
   * @return the name of the opened file, or null if none opened
   */
  private String open() {
    int result = OSPRuntime.getChooser().showOpenDialog(this);
    if(result==JFileChooser.APPROVE_OPTION) {
      OSPRuntime.chooserDir = OSPRuntime.getChooser().getCurrentDirectory().toString();
      String fileName = OSPRuntime.getChooser().getSelectedFile().getAbsolutePath();
      fileName = XML.getRelativePath(fileName);
      return open(fileName);
    }
    return null;
  }

  /**
   * Saves the current xml control to the specified file.
   *
   * @param fileName the file name
   * @return the name of the saved file, or null if not saved
   */
  private String save(String fileName) {
    if((fileName==null)||fileName.equals("")) { //$NON-NLS-1$   
      return null;
    }
    if(passwordField.getBackground()==Color.yellow) {
      passwordField.setBackground(Color.white);
      setPassword(passwordField.getText());
    }
    XMLControlElement control = getCurrentControl();
    if(control==null) {
      return null;
    }
    if(control.getObjectClass()==Cryptic.class) {
      control = decrypt(control);
    }
    if(control.write(fileName)==null) {
      return null;
    }
    this.fileName = fileName;
    refreshGUI();
    return fileName;
  }

  /**
   * Saves the currently displayed xml control to a file selected with a chooser.
   *
   * @return the name of the saved file, or null if not saved
   */
  private String saveAs() {
    int result = OSPRuntime.getChooser().showSaveDialog(this);
    if(result==JFileChooser.APPROVE_OPTION) {
      OSPRuntime.chooserDir = OSPRuntime.getChooser().getCurrentDirectory().toString();
      File file = OSPRuntime.getChooser().getSelectedFile();
      // check to see if file already exists
      if(file.exists()) {
        int selected = JOptionPane.showConfirmDialog(this, ToolsRes.getString("EncryptionTool.Dialog.ReplaceFile.Message")+" "+file.getName()+"?", //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
          ToolsRes.getString("EncryptionTool.Dialog.ReplaceFile.Title"), //$NON-NLS-1$
            JOptionPane.YES_NO_CANCEL_OPTION);
        if(selected!=JOptionPane.YES_OPTION) {
          return null;
        }
      }
      String fileName = file.getAbsolutePath();
      if((fileName==null)||fileName.trim().equals("")) {                 //$NON-NLS-1$
        return null;
      }
      return save(XML.getRelativePath(fileName));
    }
    return null;
  }

  /**
   * Creates the GUI.
   */
  private void createGUI() {
    // configure the frame
    contentPane.setPreferredSize(dim);
    setContentPane(contentPane);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    contentPane.add(toolbar, BorderLayout.NORTH);
    String imageFile = "/org/opensourcephysics/resources/tools/images/open.gif"; //$NON-NLS-1$
    openIcon = ResourceLoader.getIcon(imageFile);
    openButton = new JButton(openIcon);
    openButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open();
      }

    });
    toolbar.add(openButton);
    imageFile = "/org/opensourcephysics/resources/tools/images/save.gif"; //$NON-NLS-1$
    saveIcon = ResourceLoader.getIcon(imageFile);
    saveButton = new JButton(saveIcon);
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save(fileName);
      }

    });
    toolbar.add(saveButton);
    toolbar.addSeparator();
    passwordLabel = new JLabel();
    passwordLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    passwordField = new JTextField(20);
    passwordField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          passwordField.setBackground(Color.white);
          setPassword(passwordField.getText());
        } else if(e.getKeyChar()!=KeyEvent.CHAR_UNDEFINED) {
          passwordField.setBackground(Color.yellow);
        }
      }

    });
    passwordField.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        if(passwordField.getBackground()==Color.yellow) {
          passwordField.setBackground(Color.white);
          setPassword(passwordField.getText());
        }
      }

    });
    toolbar.add(passwordLabel);
    toolbar.add(passwordField);
    encryptedCheckBox = new JCheckBox(""); //$NON-NLS-1$
    encryptedCheckBox.setEnabled(false);
    encryptedCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // if selected, set password to current passwordField text
        if(encryptedCheckBox.isSelected()) {
          setPassword(passwordField.getText());
        }
        // else decrypt and set password to ""
        else {
          XMLControlElement control = getCurrentControl();
          if(control.getObjectClass()==Cryptic.class) {
            control = decrypt(control);
            control.setPassword(null);
            displayXML(control);
          }
          setPassword(""); //$NON-NLS-1$
        }
      }

    });
    encryptedCheckBox.setContentAreaFilled(false);
    toolbar.add(encryptedCheckBox);
    previewCheckBox = new JCheckBox(""); //$NON-NLS-1$
    previewCheckBox.setOpaque(false);
    previewCheckBox.setEnabled(false);
    previewCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        XMLControlElement control = getCurrentControl();
        if(previewCheckBox.isSelected()) {
          displayXML(encrypt(control));
        } else {
          displayXML(decrypt(control));
        }
      }

    });
    toolbar.add(previewCheckBox);
    // create the menu bar
    int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    JMenuBar menubar = new JMenuBar();
    // file menu
    fileMenu = new JMenu();
    menubar.add(fileMenu);
    // open item
    openItem = new JMenuItem();
    openItem.setAccelerator(KeyStroke.getKeyStroke('O', keyMask));
    openItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        open();
      }

    });
    fileMenu.add(openItem);
    fileMenu.addSeparator();
    // save item
    saveItem = new JMenuItem();
    saveItem.setAccelerator(KeyStroke.getKeyStroke('S', keyMask));
    saveItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save(fileName);
      }

    });
    saveItem.setEnabled(false);
    fileMenu.add(saveItem);
    // save as item
    saveAsItem = new JMenuItem();
    saveAsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveAs();
      }

    });
    saveAsItem.setEnabled(false);
    fileMenu.add(saveAsItem);
    exitItem = new JMenuItem(ToolsRes.getString("MenuItem.Exit")); //$NON-NLS-1$
    exitItem.setAccelerator(KeyStroke.getKeyStroke('Q', keyMask));
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }

    });
    fileMenu.addSeparator();
    fileMenu.add(exitItem);
    // help menu
    helpMenu = new JMenu();
    menubar.add(helpMenu);
    // log item
    logItem = new JMenuItem();
    logItem.setAccelerator(KeyStroke.getKeyStroke('L', keyMask));
    logItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Point p0 = new Frame().getLocation();
        JFrame frame = OSPLog.showLog();
        if((frame.getLocation().x==p0.x)&&(frame.getLocation().y==p0.y)) {
          Point p = getLocation();
          frame.setLocation(p.x+28, p.y+28);
        }
      }

    });
    helpMenu.add(logItem);
    helpMenu.addSeparator();
    // about item
    aboutItem = new JMenuItem();
    aboutItem.setAccelerator(KeyStroke.getKeyStroke('A', keyMask));
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String toolname = ToolsRes.getString("EncryptionTool.About.ToolName");                                             //$NON-NLS-1$
        String aboutString = toolname+OSPRuntime.VERSION+XML.NEW_LINE+ToolsRes.getString("EncryptionTool.About.OSPName")+XML.NEW_LINE //$NON-NLS-1$
                             +"www.opensourcephysics.org";                                                                //$NON-NLS-1$
        JOptionPane.showMessageDialog(EncryptionTool.this, aboutString, ToolsRes.getString("EncryptionTool.About.Title"), //$NON-NLS-1$
          JOptionPane.INFORMATION_MESSAGE);
      }

    });
    helpMenu.add(aboutItem);
    setJMenuBar(menubar);
    pack();
    // center this on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-getBounds().width)/2;
    int y = (dim.height-getBounds().height)/2;
    setLocation(x, y);
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
