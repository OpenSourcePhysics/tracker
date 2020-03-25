/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.opensourcephysics.display.Hidable;
import org.opensourcephysics.display.OSPFrame;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.numerics.DoubleArray;
import org.opensourcephysics.numerics.IntegerArray;
import org.opensourcephysics.tools.ToolsRes;

/**
 *  A Control that shows its parameters in a JTable.
 *  Custom buttons can be added.
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */
public class OSPControl extends ControlFrame implements PropertyChangeListener, MainFrame {
  OSPControlTable table = new OSPControlTable(new XMLControlElement());
  JScrollPane controlScrollPane = new JScrollPane(table);
  JTextArea messageTextArea;
  JLabel clearLabel, messageLabel, inputLabel;
  JSplitPane splitPane;
  JMenuItem translateItem;
  static final Color PANEL_BACKGROUND = javax.swing.UIManager.getColor("Panel.background"); //$NON-NLS-1$

  /**
   *  Constructs an OSPControl.
   *
   * @param  _model
   */
  public OSPControl(Object _model) {
    super(ControlsRes.getString("OSPControl.Default_Title")); //$NON-NLS-1$
    //table = new OSPControlTable(new XMLControlElement());
    System.out.println("Table created in OSP Control: "+table);
    model = _model;
    if(model!=null) {
      // added by D Brown 2006-09-10
      // modified by D Brown 2007-10-17
      if(OSPRuntime.getTranslator()!=null) {
        OSPRuntime.getTranslator().associate(this, model.getClass());
      }
      String name = model.getClass().getName();
      setTitle(name.substring(1+name.lastIndexOf("."))+ControlsRes.getString("OSPControl.Controller")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    Font labelFont = new Font("Dialog", Font.BOLD, 12); //$NON-NLS-1$
    inputLabel = new JLabel(ControlsRes.getString("OSPControl.Input_Parameters"), SwingConstants.CENTER); //$NON-NLS-1$
    inputLabel.setFont(labelFont);
    messageTextArea = new JTextArea(5, 5) {
      public void paintComponent(Graphics g) {
        if(OSPRuntime.antiAliasText) {
          Graphics2D g2 = (Graphics2D) g;
          RenderingHints rh = g2.getRenderingHints();
          rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        super.paintComponent(g);
      }

    };
    JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
    // contains a view of the control
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(inputLabel, BorderLayout.NORTH);
    topPanel.add(controlScrollPane, BorderLayout.CENTER);
    buttonPanel.setVisible(true);
    topPanel.add(buttonPanel, BorderLayout.SOUTH); // buttons are added using addButton method.
    // clear panel acts like a button to clear the message area
    JPanel clearPanel = new JPanel(new BorderLayout());
    clearPanel.addMouseListener(new ClearMouseAdapter());
    clearLabel = new JLabel(ControlsRes.getString("OSPControl.Clear")); //$NON-NLS-1$
    clearLabel.setFont(new Font(clearLabel.getFont().getFamily(), Font.PLAIN, 9));
    clearLabel.setForeground(Color.black);
    clearPanel.add(clearLabel, BorderLayout.WEST);
    // contains the messages
    JPanel bottomPanel = new JPanel(new BorderLayout());
    messageLabel = new JLabel(ControlsRes.getString("OSPControl.Messages"), SwingConstants.CENTER); //$NON-NLS-1$
    messageLabel.setFont(labelFont);
    bottomPanel.add(messageLabel, BorderLayout.NORTH);
    bottomPanel.add(messageScrollPane, BorderLayout.CENTER);
    bottomPanel.add(clearPanel, BorderLayout.SOUTH);
    Container cp = getContentPane();
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
    splitPane.setOneTouchExpandable(true);
    cp.add(splitPane, BorderLayout.CENTER);
    messageTextArea.setEditable(false);
    controlScrollPane.setPreferredSize(new Dimension(350, 200));
    controlScrollPane.setMinimumSize(new Dimension(0, 50));
    messageScrollPane.setPreferredSize(new Dimension(350, 75));
    if((OSPRuntime.getTranslator()!=null)&&(model!=null)) {
      OSPRuntime.getTranslator().associate(table, model.getClass());
    }
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation((d.width-getSize().width)/2, (d.height-getSize().height)/2); // center the frame
    init();
    ToolsRes.addPropertyChangeListener("locale", this);                      //$NON-NLS-1$
  }

  /**
   * Gets this frame.  Implementation of MainFrame interface.
   * @return OSPFrame
   */
  public OSPFrame getMainFrame() {
    return this;
  }

  /**
   * Adds a Display menu to the menu bar. Overrides OSPFrame method.
   *
   * @return the display menu
   */
  protected JMenu loadDisplayMenu() {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return null;
    }
    JMenu menu = super.loadDisplayMenu();
    translateItem = new JMenuItem();
    translateItem.setText(ControlsRes.getString("OSPControl.Translate")); //$NON-NLS-1$
    // changed by D Brown 2007-10-17
    if(OSPRuntime.getTranslator()!=null) {
      translateItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          OSPRuntime.getTranslator().showProperties(model.getClass());
          if(OSPRuntime.getTranslator() instanceof Hidable) {
            ((Hidable) OSPRuntime.getTranslator()).setKeepHidden(false);
          }
          OSPRuntime.getTranslator().setVisible(true);
        }

      });
      translateItem.setEnabled(OSPRuntime.isAuthorMode());
      languageMenu.add(translateItem, 0);
    }
    // changed by D Brown 2006-09-10
    if(languageMenu.getItemCount()>1) {
      languageMenu.insertSeparator(1);
    }
    return menu;
  }

  /**
   * Refreshes the user interface in response to display changes such as Language.
   */
  protected void refreshGUI() {
    if(messageLabel==null) {
      return;
    }
    super.refreshGUI();
    messageLabel.setText(ControlsRes.getString("OSPControl.Messages"));       //$NON-NLS-1$
    clearLabel.setText(ControlsRes.getString("OSPControl.Clear"));            //$NON-NLS-1$
    inputLabel.setText(ControlsRes.getString("OSPControl.Input_Parameters")); //$NON-NLS-1$
    table.refresh();
  }

  /**
   * Listens for property change events.
   *
   * @param e the property change event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if(name.equals("translation")||name.equals("locale")) {//$NON-NLS-1$ //$NON-NLS-2$
      refreshGUI(); 
      // forward event to other listeners
    } else {
      firePropertyChange(e.getPropertyName(), e.getOldValue(), e.getNewValue());
    }
  }

  /**
   * Initializes this control after all objects have been created.
   *
   * Override this method and change the default close operation if this control is used with an applet.
   */
  protected void init() {
    splitPane.setDividerLocation(-1);
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public Object getModel() {
    return model;
  }

  /**
   * Sets the location of the divider between the control table and the message panel.
   * @param loc int
   */
  public void setDividerLocation(int loc) {
    splitPane.setDividerLocation(loc);
  }

  /**
   * Sets the editable property of the given parameter so that it can not be changed from within the GUI.
   *
   * @param parameter String
   * @param editable boolean
   */
  public void setEditable(String parameter, boolean editable) {
    table.setEditable(parameter, editable);
  }

  /**
   * Locks the control's interface. Values sent to the control will not
   * update the display until the control is unlocked.
   *
   * @param lock boolean
   */
  public void setLockValues(boolean lock) {
    table.setLockValues(true);
  }

  /**
   *  Creates a string representation of the control parameters.
   *
   * @return    the control parameters
   */
  public String toString() {
	System.out.println("Table to string: "+table); // xxx debug
	if(table==null) return "";
    return table.toString();
  }

  /**
   *  Adds a parameter to the input display.
   *
   * @param  par  the parameter name
   * @param  val  the initial parameter value
   */
  public void setValue(String par, Object val) {
    table.setValue(par, val);
  }

  /**
   *  Adds an initial boolean value of a parameter to the input display.
   *
   * @param  par  the parameter name
   * @param  val  the initial parameter value
   */
  public void setValue(String par, boolean val) {
    table.setValue(par, val);
  }

  /**
   *  Adds an initial value of a parameter to the input display.
   *
   * @param  par  the parameter name
   * @param  val  the initial parameter value
   */
  public void setValue(String par, double val) {
    table.setValue(par, Double.toString(val));
  }

  /**
   *  Adds an initial value of a parameter to the input display.
   *
   * @param  par  the parameter name
   * @param  val  the initial parameter value
   */
  public void setValue(String par, int val) {
    table.setValue(par, Integer.toString(val));
  }

  /**
   *  Removes a parameter from the table.
   *
   * @param  par  the parameter name
   */
  public void removeParameter(String par) {
    table.setValue(par, null);
  }

  /**
   *  Reads a parameter value from the input display.
   *
   * @param  par
   * @return      double the value of of the parameter
   */
  public double getDouble(String par) {
    return table.getDouble(par);
  }

  /**
   *  Reads a parameter value from the input display.
   *
   * @param  par
   * @return      int the value of of the parameter
   */
  public int getInt(String par) {
    return table.getInt(par);
  }

  /**
   * Gets the object with the specified property name.
   * Throws an UnsupportedOperationException if the named object has not been stored.
   *
   * @param  par
   * @return the object
   */
  public Object getObject(String par) throws UnsupportedOperationException {
    return table.getObject(par);
  }

  /**
   *  Reads a parameter value from the input display.
   *
   * @param  par  the parameter name
   * @return      String the value of of the parameter
   */
  public String getString(String par) {
    return table.getString(par);
  }

  /**
   *  Reads a parameter value from the input display.
   *
   * @param  par  the parameter name
   * @return      the value of of the parameter
   */
  public boolean getBoolean(String par) {
    return table.getBoolean(par);
  }

  /**
   *  Reads the current property names.
   *
   * @return      the property names
   */
  public Collection<String> getPropertyNames() {
    return table.getPropertyNames();
  }

  /**
   *  Adds a custom button to the control's frame.
   *
   * @param  methodName  the name of the method; the method has no parameters
   * @param  text        the button's text label
   * @return             the custom button
   */
  public JButton addButton(String methodName, String text) {
    return addButton(methodName, text, null, model);
  }

  /**
   *  Adds a custom button to the control's frame.
   *
   * @param  methodName   the name of the method; the method has no parameters
   * @param  text         the button's text label
   * @param  toolTipText  the button's tool tip text
   * @return              the custom button
   */
  public JButton addButton(String methodName, String text, String toolTipText) {
    return addButton(methodName, text, toolTipText, model);
  }

  /**
   *  Adds a ControlTableListener that invokes method in the control's model.
   *  The method in the model is invoked with the table's variable name passed as a
   *  parameter.
   *
   * @param  methodName   the name of the method; the method has no parameters
   */
  public void addControlListener(String methodName) {
    addControlListener(methodName, model);
  }

  /**
   *  Adds a ControlTableListener that invokes method in the given object.
   *  The method in the target is invoked with the table's variable name passed as a
   *  parameter.
   *
   * @param  methodName   the name of the method; the method has no parameters
   * @param  target       the target for the method
   */
  public void addControlListener(String methodName, final Object target) {
    Class<?>[] parameters = new Class[] {String.class};
    try {
      final java.lang.reflect.Method m = target.getClass().getMethod(methodName, parameters);
      table.tableModel.addTableModelListener(new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          if((e.getType()!=TableModelEvent.UPDATE)||(e.getColumn()!=1)||(e.getFirstRow()<0)) {
            return;
          }
          String name = table.getValueAt(e.getFirstRow(), 0).toString();
          Object[] args = {name};
          try {
            m.invoke(target, args);
          } catch(IllegalAccessException iae) {}
          catch(java.lang.reflect.InvocationTargetException ite) {}
        }

      });
    } catch(NoSuchMethodException nsme) {
      System.err.println("The method "+methodName+"() does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  /**
   *  Prints a line of text in the message area.
   *
   * @param  s
   */
  public void println(String s) {
    print(s+"\n"); //$NON-NLS-1$
  }

  /**
   *  Prints a blank line in the message area.
   */
  public void println() {
    print("\n"); //$NON-NLS-1$
  }

  /**
   *  Prints text in the message area.
   *
   * @param  s
   */
  public void print(final String s) {
    if(SwingUtilities.isEventDispatchThread()||Thread.currentThread().getName().equals("main")) { //$NON-NLS-1$
      messageTextArea.append(s);
      return;
    }
    Runnable doLater = new Runnable() {
      public void run() {
        messageTextArea.append(s);
      }

    };
    // Update from the event queue.
    java.awt.EventQueue.invokeLater(doLater);
  }

  /**
   *  Remove all text from the message area.
   */
  public void clearMessages() {
    if(SwingUtilities.isEventDispatchThread()||Thread.currentThread().getName().equals("main")) { //$NON-NLS-1$
      messageTextArea.setText("");                                                                //$NON-NLS-1$
      return;
    }
    Runnable doLater = new Runnable() {
      public void run() {
        messageTextArea.setText(""); //$NON-NLS-1$
      }

    };
    // Update from the event queue.
    java.awt.EventQueue.invokeLater(doLater);
  }

  /**
   *  Remove all text from the data input area.
   */
  public void clearValues() {
    table.clearValues();
  }

  /**
   *  A signal that a method has completed. A message is printed in the message area.
   *
   * @param  message
   */
  public void calculationDone(String message) {
    // not implemented
    if(message!=null) {
      println(message);
    }
  }

  class ClearMouseAdapter extends java.awt.event.MouseAdapter {
    /**
     * Method mousePressed
     *
     * @param evt
     */
    public void mousePressed(java.awt.event.MouseEvent evt) {
      clearMessages();
    }

    /**
     * Method mouseEntered
     *
     * @param evt
     */
    public void mouseEntered(java.awt.event.MouseEvent evt) {
      clearLabel.setFont(new Font(clearLabel.getFont().getFamily(), Font.BOLD, 10));
      clearLabel.setText(ControlsRes.getString("OSPControl.Click_to_clear_message")); //$NON-NLS-1$
    }

    /**
     * Method mouseExited
     *
     * @param evt
     */
    public void mouseExited(java.awt.event.MouseEvent evt) {
      clearLabel.setFont(new Font(clearLabel.getFont().getFamily(), Font.PLAIN, 9));
      clearLabel.setText(ControlsRes.getString("OSPControl.Clear")); //$NON-NLS-1$
    }

  }

  /**
   * Returns an XML.ObjectLoader to save and load data for this object.
   *
   * @return the object loader
   */
  public static XML.ObjectLoader getLoader() {
    return new OSPControlLoader();
  }

  /**
 * A class to save and load data for OSPControls.
 */
  static class OSPControlLoader implements XML.ObjectLoader {
    /**
     * Saves object data to an XMLControl.
     *
     * @param prefsXMLControl the control to save to
     * @param obj the object to save
     */
    public void saveObject(XMLControl xmlControl, Object obj) {
      OSPControl ospControl = (OSPControl) obj;
      saveControlProperites(xmlControl, ospControl);
      // save the model if the control is the top level
      if(xmlControl.getLevel()==0) {
        xmlControl.setValue("model", ospControl.model); //$NON-NLS-1$
      }
    }

    protected void saveControlProperites(XMLControl xmlControl, OSPControl ospControl) {
      // save the parameters
      Iterator<String> it = ospControl.getPropertyNames().iterator();
      while(it.hasNext()) {
        String name = it.next();
        Object val = ospControl.getObject(name);
        if(val.getClass()==DoubleArray.class) {
          xmlControl.setValue(name, ((DoubleArray) val).getArray());
        } else if(val.getClass()==IntegerArray.class) {
          xmlControl.setValue(name, ((IntegerArray) val).getArray());
        } else if(val.getClass()==Boolean.class) {
          xmlControl.setValue(name, ((Boolean) val).booleanValue());
        } else if(val.getClass()==Double.class) {
          xmlControl.setValue(name, ((Double) val).doubleValue());
        } else if(val.getClass()==Integer.class) {
          xmlControl.setValue(name, ((Integer) val).intValue());
        } else if(val.getClass()==Character.class) {
          xmlControl.setValue(name, ((Character) val).toString());
        } else if(val.getClass().isArray()) {
          xmlControl.setValue(name, val);
        } else {
          xmlControl.setValue(name, val);
        }
        // xmlControl.setValue(name, val.toString());
      }
    }

    /**
     * Creates an object using data from an XMLControl.
     *
     * @param control the control
     * @return the newly created object
     */
    public Object createObject(XMLControl control) {
      return new OSPControl(null);
    }

    /**
     * Loads an object with data from an XMLControl.
     *
     * @param control the control
     * @param obj the object
     * @return the loaded object
     */
    public Object loadObject(XMLControl control, Object obj) {
      OSPControl cf = (OSPControl) obj;
      // iterate over properties and add them to table model
      Iterator<String> it = control.getPropertyNames().iterator();
      cf.table.setLockValues(true);
      while(it.hasNext()) {
        String name = it.next();
        // skip "model" object properties
        if(name.equals("model")&&control.getPropertyType(name).equals("object")) {                     //$NON-NLS-1$ //$NON-NLS-2$
          XMLControl child = control.getChildControl("model");                                         //$NON-NLS-1$
          cf.model = child.loadObject(cf.model);
          continue;
        }
        if((cf.getObject(name) instanceof OSPCombo)&&control.getPropertyType(name).equals("string")) { //$NON-NLS-1$
          OSPCombo combo = (OSPCombo) cf.getObject(name);                                              // keep the combo but select the correct item
          String itemName = control.getString(name);
          String[] items = combo.items;
          for(int i = 0, n = items.length; i<n; i++) {
            if(itemName.equals(items[i])) {
              combo.selected = i;
              break;
            }
          }
          cf.setValue(name, combo);
        } else if(control.getPropertyType(name).equals("string")) {                                    //$NON-NLS-1$
          cf.setValue(name, control.getString(name));
        } else if(control.getPropertyType(name).equals("int")) {                                       //$NON-NLS-1$
          cf.setValue(name, control.getInt(name));
        } else if(control.getPropertyType(name).equals("double")) {                                    //$NON-NLS-1$
          cf.setValue(name, control.getDouble(name));
        } else if(control.getPropertyType(name).equals("boolean")) {                                   //$NON-NLS-1$
          cf.setValue(name, control.getBoolean(name));
        } else {
          cf.setValue(name, control.getObject(name));
        }
      }
      cf.table.setLockValues(false);
      //      XMLControl child = control.getChildControl("model"); //$NON-NLS-1$
      //      if(child!=null) {
      //        cf.model = child.loadObject(cf.model);
      //      }
      return obj;
    }

  }

  /**
   * Creates an OSP control and establishes communication between the control and the model.
   *
   * Custom buttons are usually added to this control to invoke actions in the model.
   *
   * @param model Object
   * @return AnimationControl
   */
  public static OSPControl createApp(Object model) {
    OSPControl control = new OSPControl(model);
    control.setSize(300, 300);
    control.setVisible(true);
    return control;
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
