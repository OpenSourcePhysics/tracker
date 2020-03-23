/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * A dialog for verifying passwords with a single public static method verify().
 *
 * @author Douglas Brown
 * @version 1.0
 */
public class Password extends JDialog {

	// instance fields
  private JLabel messageLabel;
  private JPasswordField passwordField;
  private String password;
  private boolean pass;

  /**
   * Shows a dialog and verifies user entry of the password.
   *
   * @param password the password
   * @param filename the name of the password-protected file (may be null).
   * @return true if password is null, "", or correctly verified
   */
  public static boolean verify(String password, String fileName) {
    if((password==null)||password.equals("")) {//$NON-NLS-1$
      return true; 
    }
	  Password dialog = new Password();
    dialog.password = password;
    if((fileName==null)||fileName.equals("")) {                                     //$NON-NLS-1$
      dialog.messageLabel.setText(ControlsRes.getString("Password.Message.Short")); //$NON-NLS-1$
    } else {
      dialog.messageLabel.setText(ControlsRes.getString("Password.Message.File")    //$NON-NLS-1$
                                  +" \""+XML.getName(fileName)+"\".");               //$NON-NLS-1$ //$NON-NLS-2$
    }
    dialog.pack();
    // center on screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-dialog.getBounds().width)/2;
    int y = (dim.height-dialog.getBounds().height)/2;
    dialog.setLocation(x, y);
    dialog.pass = false;
    dialog.passwordField.setText(""); //$NON-NLS-1$
    dialog.setVisible(true);
    dialog.dispose();
    return dialog.pass;
  }

  /**
   * Private constructor.
   */
  private Password() {
    super((Frame) null, true); // modal with no owner
    setTitle(ControlsRes.getString("Password.Title")); //$NON-NLS-1$
    createGUI();
    setResizable(false);
    passwordField.requestFocusInWindow();
  }

  /**
   * Creates the visible components.
   */
  private void createGUI() {
    // create input panel
    GridBagLayout gridbag = new GridBagLayout();
    JPanel inputPanel = new JPanel(gridbag);
    // create components
    messageLabel = new JLabel();
    JLabel fieldLabel = new JLabel(ControlsRes.getString("Password.Label")); //$NON-NLS-1$
    passwordField = new JPasswordField(20);
    passwordField.setToolTipText(ControlsRes.getString("Password.Tooltip")); //$NON-NLS-1$
    passwordField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String input = String.copyValueOf(passwordField.getPassword());
        if((password!=null)&&!input.equals(password)) {
          Toolkit.getDefaultToolkit().beep();
          passwordField.setText("");                                         //$NON-NLS-1$
        } else {
          pass = true;
          setVisible(false);
        }
      }

    });
    passwordField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
          passwordField.requestFocusInWindow();
          setVisible(false);
        }
      }

    });
    // create buttons
    JButton cancelButton = new JButton(ControlsRes.getString("Password.Button.Cancel")); //$NON-NLS-1$
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        passwordField.requestFocusInWindow();
        setVisible(false);
      }

    });
    JButton okButton = new JButton(ControlsRes.getString("Password.Button.Enter")); //$NON-NLS-1$
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String input = String.copyValueOf(passwordField.getPassword());
        if((password!=null)&&!input.equals(password)) {
          Toolkit.getDefaultToolkit().beep();
          passwordField.setText(""); //$NON-NLS-1$
          passwordField.requestFocusInWindow();
        } else {
          pass = true;
          setVisible(false);
        }
      }

    });
    // input panel in center
    Container contentPane = this.getContentPane();
    contentPane.add(inputPanel, BorderLayout.CENTER);
    GridBagConstraints c = new GridBagConstraints();
    // add message to input panel
    c.insets = new Insets(20, 15, 10, 15);
    gridbag.setConstraints(messageLabel, c);
    inputPanel.add(messageLabel);
    // add label and password field to input panel
    JPanel entry = new JPanel();
    entry.add(fieldLabel);
    entry.add(passwordField);
    c.gridy = 1;
    c.insets = new Insets(0, 10, 10, 10);
    gridbag.setConstraints(entry, c);
    inputPanel.add(entry);
    // button pane at bottom
    JPanel buttonPane = new JPanel();
    contentPane.add(buttonPane, BorderLayout.SOUTH);
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 4));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(okButton);
    buttonPane.add(Box.createRigidArea(new Dimension(4, 0)));
    buttonPane.add(cancelButton);
  }
  
}

/*
 * Open Source Physics software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be
 * released under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston MA 02111-1307 USA or view the license online at
 * http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2017 The Open Source Physics project
 * http://www.opensourcephysics.org
 */
