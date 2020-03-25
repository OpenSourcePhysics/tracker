/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * This modal dialog lets the user choose items from a tree view of an XMLControl.
 */
public class XMLTreeChooser extends JDialog {
  // instance fields
  private JPanel scrollPane;
  private XMLTree tree;
  private JLabel textLabel;
  private boolean applyChanges = false;

  /**
   * Constructs a dialog with the specified title and text.
   *
   * @param title the title of the dialog
   * @param text the label text
   */
  public XMLTreeChooser(String title, String text) {
    this(title, text, null);
  }

  /**
   * Constructs a dialog with the specified title, text and owner.
   *
   * @param title the title of the dialog
   * @param text the label text
   * @param comp the component that owns the dialog (may be null)
   */
  public XMLTreeChooser(String title, String text, Component comp) {
    super(JOptionPane.getFrameForComponent(comp), true);
    setTitle(title);
    textLabel = new JLabel(" "+text); //$NON-NLS-1$
    textLabel.setHorizontalTextPosition(SwingConstants.LEFT);
    // create the buttons
    JButton cancelButton = new JButton(ControlsRes.getString("Chooser.Button.Cancel"));       //$NON-NLS-1$
    JButton okButton = new JButton(ControlsRes.getString("Chooser.Button.OK"));               //$NON-NLS-1$
    JButton selectAllButton = new JButton(ControlsRes.getString("Chooser.Button.SelectAll")); //$NON-NLS-1$
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }

    });
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyChanges = true;
        setVisible(false);
      }

    });
    selectAllButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tree.selectHighlightedProperties();
      }

    });
    getRootPane().setDefaultButton(okButton);
    // lay out the header pane
    JPanel headerPane = new JPanel();
    headerPane.setLayout(new BoxLayout(headerPane, BoxLayout.X_AXIS));
    headerPane.add(textLabel);
    headerPane.add(Box.createHorizontalGlue());
    headerPane.add(selectAllButton);
    headerPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    // lay out the scroll pane
    scrollPane = new JPanel(new BorderLayout());
    scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    // lay out the button pane
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(okButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPane.add(cancelButton);
    // add everything to the content pane
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setPreferredSize(new Dimension(340, 340));
    setContentPane(contentPane);
    contentPane.add(headerPane, BorderLayout.NORTH);
    contentPane.add(scrollPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);
    pack();
    // center dialog on the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    int x = (dim.width-this.getBounds().width)/2;
    int y = (dim.height-this.getBounds().height)/2;
    setLocation(x, y);
  }

  /**
   * Highlights objects of a specified class in a tree view of an XMLControl
   * and returns those selected by the user, if any.
   *
   * @param control the XMLControl
   * @param type the class to be highlighted
   * @return a list of selected objects
   */
  public java.util.List<XMLProperty> choose(XMLControl control, Class<?> type) {
    ArrayList<XMLProperty> list = new ArrayList<XMLProperty>();
    tree = new XMLTree(control);
    tree.setHighlightedClass(type);
    // tree.showHighlightedProperties();
    tree.selectHighlightedProperties();
    textLabel.setIcon(XMLTree.hiliteIcon);
    scrollPane.removeAll();
    scrollPane.add(tree.getScrollPane(), BorderLayout.CENTER);
    validate();
    applyChanges = false;
    setVisible(true);
    if(applyChanges) {
      java.util.List<XMLProperty> props = tree.getSelectedProperties();
      Iterator<XMLProperty> it = props.iterator();
      while(it.hasNext()) {
        XMLProperty prop = it.next();
        Class<?> propClass = prop.getPropertyClass();
        if((propClass!=null)&&type.isAssignableFrom(propClass)) {
          list.add(prop);
        }
      }
    }
    return list;
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
