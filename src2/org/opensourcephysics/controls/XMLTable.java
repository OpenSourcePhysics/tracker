/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.controls;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.opensourcephysics.display.CellBorder;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ArrayInspector;

/**
 * This is a table view of an XML control and its property contents.
 *
 * @author Douglas Brown
 */
public class XMLTable extends JTable {
  final static Color LIGHT_BLUE = new Color(196, 196, 255);
  // instance fields

  XMLTableModel tableModel;
  XMLCellRenderer xmlRenderer = new XMLCellRenderer();
  XMLValueEditor valueEditor = new XMLValueEditor();
  Color defaultBackgroundColor = Color.white;
  Map<String, Color> cellColors = new HashMap<String, Color>();         // maps property to color of cell
  Map<String, Color> selectedCellColors = new HashMap<String, Color>(); // maps property to color of selected cell
  Map<String, Color> editingCellColors = new HashMap<String, Color>();  // maps property to color of editing cell
  Color defaultEditingColor;
  PropertyChangeListener comboListener;

  /**
   * Constructor for XMLControl.
   *
   * @param control the XMLcontrol
   */
  public XMLTable(XMLControl control) {
    tableModel = new XMLTableModel(control);
    init();
  }

  /**
   * Constructor for XMLTableModel.
   *
   * @param model the XMLTableModel
   */
  public XMLTable(XMLTableModel model) {
    tableModel = model;
    init();
  }

  /**
   * Gets the currently displayed XMLControl.
   *
   * @return the XML control
   */
  public XMLControl getControl() {
    return tableModel.control;
  }

  /**
   * Enables/disables editing for the entire table.
   * Overrides the editable property of individual parameters.
   *
   * @param editable true to enable editing
   */
  public void setEditable(boolean editable) {
    tableModel.editable = editable;
  }

  /**
   * Returns true if editing is enabled for the entire table.
   * If table is editiable, editing can still be disabled for individual parameters.
   *
   * @return true if editable
   * @see setEditable(String propName, boolean editable)
   */
  public boolean isEditable() {
    return tableModel.editable;
  }

  /**
   * Enables/disables editing for a specified property name.
   * Properties are editable by default.
   *
   * @param propName the property name
   * @param editable true to enable editing
   */
  public void setEditable(String propName, boolean editable) {
    // add to uneditablePropNames list if editable is false
    if(!editable) {
      tableModel.uneditablePropNames.add(propName);
    } else {
      tableModel.uneditablePropNames.remove(propName);
    }
  }

  /**
   * Returns true if editing is enabled for the specified property.
   *
   * @param propName the name of the property
   * @return true if editable
   */
  public boolean isEditable(String propName) {
    return !tableModel.uneditablePropNames.contains(propName);
  }

  /**
   * Determines whether the given cell is editable.
   *
   * @param row the row index
   * @param col the column index
   * @return true if editable
   */
  public boolean isCellEditable(int row, int col) {
    return tableModel.editable&&tableModel.isCellEditable(row, col);
  }

  /**
   * Sets the font. Overrides JTable method
   *
   * @param font the font
   */
  public void setFont(Font font) {
    super.setFont(font);
    if(xmlRenderer!=null) {
      xmlRenderer.setFont(font);
      valueEditor.field.setFont(font);
      // resize row heights after revalidation
      Runnable runner = new Runnable() {
        public void run() {
          if(getTableHeader().getHeight()>0) { // changed by W. Christian
            setRowHeight(getTableHeader().getHeight());
          }
        }

      };
      SwingUtilities.invokeLater(runner);
    }
  }

  /**
   * Sets the color of a selected cell for a specified property name.
   * May be set to null.
   *
   * @param propName the property name
   * @param color the color of the cell when selected
   */
  public void setSelectedColor(String propName, Color color) {
    selectedCellColors.put(propName, color);
  }

  /**
   * Gets the color of a selected cell for a specified property name.
   *
   * @param propName the property name
   * @return the color
   */
  public Color getSelectedColor(String propName) {
    Color color = selectedCellColors.get(propName);
    return(color==null) ? LIGHT_BLUE : color;
  }

  /**
   * Sets the background color of the value field for a specified property name.
   * May be set to null.
   *
   * @param propName the property name
   * @param color the color
   */
  public void setBackgroundColor(String propName, Color color) {
    cellColors.put(propName, color);
  }

  /**
   * Gets the background color for a specified property name.
   *
   * @param propName the property name
   * @return the color
   */
  public Color getBackgroundColor(String propName) {
    Color color = cellColors.get(propName);
    return(color==null) ? defaultBackgroundColor : color;
  }

  /**
   * Sets the default color of the editor for a specified property name.
   * May be set to null.
   *
   * @param propName the property name
   * @param color the color of the cell when being edited
   */
  public void setEditingColor(String propName, Color color) {
    editingCellColors.put(propName, color);
  }

  /**
   * Sets the color of the editor for a specified property name.
   *
   * @param propName the property name
   * @return the color
   */
  public Color getEditingColor(String propName) {
    Color color = editingCellColors.get(propName);
    return(color==null) ? defaultEditingColor : color;
  }

  /**
   * Returns the renderer for a cell specified by row and column.
   *
   * @param row the row number
   * @param column the column number
   * @return the cell renderer
   */
  public TableCellRenderer getCellRenderer(int row, int column) {
    return xmlRenderer;
  }

  /**
   * Returns the editor for a cell specified by row and column.
   *
   * @param row the row number
   * @param column the column number
   * @return the cell editor
   */
  public TableCellEditor getCellEditor(int row, int column) {
    return valueEditor;
  }

  /**
   * A cell renderer for an xml table.
   */
  class XMLCellRenderer extends DefaultTableCellRenderer {
    Color lightGreen = new Color(204, 255, 204);                          // for double-clickable cells
    Color lightGray = javax.swing.UIManager.getColor("Panel.background"); //$NON-NLS-1$
    Font font = new JTextField().getFont();

    // Constructor

    /**
     * Constructor XMLCellRenderer
     */
    public XMLCellRenderer() {
      super();
      setOpaque(true); // make background visible
      setForeground(Color.black);
      setFont(font);
    }

    // Returns a label for the specified cell.
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      setForeground(Color.BLACK);
      if(value==null) {
        value = ""; // changed by W. Christian to trap for null values //$NON-NLS-1$
      }
      String propName = (String) tableModel.getValueAt(row, 0);
      Class<?> childClass = null;
      Object childObj = null;
      if(column==0) { // property name
        if(isSelected) {
          //setBackground(new Color(255, 192, 255));
          setBackground(LIGHT_BLUE);
        } else {
          setBackground(lightGray);
        }
        setHorizontalAlignment(SwingConstants.LEFT);
        String text = value.toString();
        if(OSPRuntime.getTranslator()!=null) {
          text = OSPRuntime.getTranslator().getProperty(XMLTable.this, text);
        }
        setText(text);
        //setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224),1));
        setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
        return this;
      }
      if(value instanceof XMLProperty) {                // object, array or collection type
        XMLProperty prop = (XMLProperty) value;
        XMLProperty parent = prop.getParentProperty();
        childClass = parent.getPropertyClass();
        String className = XML.getSimpleClassName(childClass);
        XMLControl control = (XMLControl) parent.getParentProperty();
        childObj = control.getObject(parent.getPropertyName());
        // array type
        if(parent.getPropertyType().equals("array")) {  //$NON-NLS-1$
          Object array = childObj;
          // determine if base component type is primitive and count array elements
          Class<?> baseType = array.getClass().getComponentType();
          int count = Array.getLength(array);
          int insert = className.indexOf("[]")+1;       //$NON-NLS-1$
          className = className.substring(0, insert)+count+className.substring(insert);
          while(baseType.getComponentType()!=null) {
            baseType = baseType.getComponentType();
            array = Array.get(array, 0);
            if(array==null) {
              break;
            }
            count = Array.getLength(array);
            insert = className.indexOf("[]", insert)+1; //$NON-NLS-1$
            className = className.substring(0, insert)+count+className.substring(insert);
          }
        }
        // general object types
        if((childClass!=OSPCombo.class                  // OSPCombo handled below
          )&&(childClass!=Boolean.class                 // Boolean handled below
            )&&(childClass!=Character.class)) {         // Char handled below
          setText(className);
          setBackground(isInspectable(parent) ? lightGreen : lightGray);
          //setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
          //setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
          setBorder(new CellBorder(new Color(240, 240, 240)));
          setHorizontalAlignment(SwingConstants.CENTER);
          if(isSelected&&isInspectable(parent)) {
            setBackground(getSelectedColor(propName));
            setForeground(Color.RED);
          }
          return this;
        }
      }
      // int, double, boolean, string and special object types
      if(isSelected) {
        setBackground(getSelectedColor(propName));
        setForeground(Color.RED);
      } else {
        setBackground(getBackgroundColor(propName));
      }
      setHorizontalAlignment(SwingConstants.LEFT);
      if((childClass==OSPCombo.class)||(childClass==Boolean.class)||(childClass==Character.class)) {
        setText(childObj.toString());
      } else {
        setText(value.toString());
      }
      //setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240)));
      setBorder(new CellBorder(new Color(240, 240, 240)));
      //setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
      if(!tableModel.editable||tableModel.uneditablePropNames.contains(propName)) {
        setForeground(Color.GRAY); // override all other color settings
      }
      return this;
    }

  }

  /**
   * A cell editor for an xml table.
   */
  class XMLValueEditor extends AbstractCellEditor implements TableCellEditor {
    JPanel panel = new JPanel(new BorderLayout());
    JTextField field = new JTextField();
    int keepFocus = -2;
    OSPCombo combo; // may be null

    // Constructor.
    XMLValueEditor() {
      defaultEditingColor = field.getSelectionColor();
      panel.add(field, BorderLayout.CENTER);
      panel.setOpaque(false);
      field.setBorder(BorderFactory.createLineBorder(new Color(128, 128, 128), 1));
      //field.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 0));
      field.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          stopCellEditing();
          keepFocus = -2;
        }

      });
      field.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            stopCellEditing();
            keepFocus = -2;
          } else if(field.isEnabled()) {
            field.setBackground(Color.yellow);
          }
        }

      });
      field.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          if(e.isTemporary()) {
            return;
          }
          if(field.getBackground()!=defaultBackgroundColor) {
            stopCellEditing();
          }
          int i = XMLTable.this.getSelectedRow();
          if(keepFocus==i) {
            keepFocus = -2;
          } else {
            XMLTable.this.requestFocusInWindow();
          }
        }

      });
      // OSPCombo case
      field.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          if(combo!=null) {
            if(combo.getPropertyChangeListeners("index").length>0) {      //$NON-NLS-1$
              combo.removePropertyChangeListener("index", comboListener); //$NON-NLS-1$
              combo.setVisible(false);
              stopCellEditing();
            } else {
              // remove and add combo listener
              combo.removePropertyChangeListener("index", comboListener); //$NON-NLS-1$
              combo.addPropertyChangeListener("index", comboListener);    //$NON-NLS-1$
              combo.showPopup(field);
            }
          }
        }

      });
    }

    // Gets the component to be displayed while editing.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
      combo = null;
      String propName = (String) tableModel.getValueAt(row, 0);
      field.setBackground(defaultBackgroundColor);
      field.setSelectionColor(getEditingColor(propName));
      final int rowNumber = row;
      final int colNumber = column;
      if(value instanceof XMLControl) {                                           // object type
        final XMLControl childControl = (XMLControl) value;
        // Color case
        if(childControl.getObjectClass()==Color.class) {
          Color color = (Color) childControl.loadObject(null);
          String title = ControlsRes.getString("XMLTable.ColorChooser.Title");    //$NON-NLS-1$
          Color newColor = JColorChooser.showDialog(null, title, color);
          if((newColor!=null)&&!color.equals(newColor)) {
            childControl.saveObject(newColor);
            tableModel.fireTableCellUpdated(row, column);
          }
          return null;
        }
        // Character case
        if(childControl.getObjectClass()==Character.class) {
          Character c = (Character) childControl.loadObject(null);
          field.setEditable(true);
          field.setText(c.toString());
          return panel;
        }
        // OSPCombo case
        if(childControl.getObjectClass()==OSPCombo.class) {
          combo = (OSPCombo) childControl.loadObject(null);
          combo.row = row;
          combo.column = column;
          field.setText(combo.toString());
          field.setEditable(false);
          return panel;
        }
        // Boolean case
        if(childControl.getObjectClass()==Boolean.class) {
          Boolean bool = (Boolean) childControl.loadObject(null);
          int n = bool.booleanValue() ? 0 : 1;
          combo = new OSPCombo(new String[] {"true", "false"}, n);                //$NON-NLS-1$//$NON-NLS-2$
          combo.row = row;
          combo.column = column;
          field.setText(bool.toString());
          field.setEditable(false);
          combo.addPropertyChangeListener("value", new PropertyChangeListener() { //$NON-NLS-1$
            public void propertyChange(PropertyChangeEvent e) {
              OSPCombo combo = (OSPCombo) e.getSource();
              Boolean bool = new Boolean(combo.getSelectedIndex()==0);
              childControl.saveObject(bool);
            }

          });
          return panel;
        }
        XMLTableInspector inspector = new XMLTableInspector(childControl, isEditable());
        // listen for "xmlData" changes in inspector
        inspector.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            // signal listeners when inspector closes and xml data is changed
            if(e.getPropertyName().equals("xmlData")) { //$NON-NLS-1$
              tableModel.fireTableCellUpdated(rowNumber, colNumber);
            }
          }

        });
        // offset new inspector relative to parent container
        Container cont = XMLTable.this.getTopLevelAncestor();
        Point p = cont.getLocationOnScreen();
        inspector.setLocation(p.x+30, p.y+30);
        inspector.setVisible(true);
        return null;
      } else if(value instanceof XMLProperty) {                                              // collection or array type
        XMLProperty prop = (XMLProperty) value;
        XMLProperty parent = prop.getParentProperty();
        if(parent.getPropertyType().equals("collection")) {                                  //$NON-NLS-1$
          String name = parent.getPropertyName();
          parent = parent.getParentProperty();
          if(parent instanceof XMLControl) {
            XMLControl cControl = new XMLControlElement();
            Collection<?> c = (Collection<?>) ((XMLControl) parent).getObject(name);
            Iterator<?> it = c.iterator();
            int i = 0;
            while(it.hasNext()) {
              Object next = it.next();
              cControl.setValue("item_"+i, next);                                            //$NON-NLS-1$
              i++;
            }
            XMLTableInspector inspector = new XMLTableInspector(cControl);
            inspector.setTitle(ControlsRes.getString("XMLTable.Inspector.Title")+name+"\""); //$NON-NLS-1$//$NON-NLS-2$
            // offset new inspector relative to parent container
            Container cont = XMLTable.this.getTopLevelAncestor();
            Point p = cont.getLocationOnScreen();
            inspector.setLocation(p.x+30, p.y+30);
            inspector.setVisible(true);
            cont.transferFocus();
          }
        }
        // display an array inspector if available
        XMLProperty arrayProp = prop.getParentProperty();
        ArrayInspector arrayInspector = ArrayInspector.getInspector(arrayProp);
        if(arrayInspector!=null) {
          String name = arrayProp.getPropertyName();
          parent = arrayProp.getParentProperty();
          while(!(parent instanceof XMLControl)) {
            name = parent.getPropertyName();
            arrayProp = parent;
            parent = parent.getParentProperty();
          }
          final XMLControl arrayControl = (XMLControl) parent;
          final String arrayName = name;
          final Object arrayObj = arrayInspector.getArray();
          arrayInspector.setEditable(tableModel.editable);
          // listen for "cell" and "arrayData" changes in the array inspector
          arrayInspector.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
              if(e.getPropertyName().equals("cell")) {                                       //$NON-NLS-1$
                // set new value in array control
                arrayControl.setValue(arrayName, arrayObj);
              }
              // signal listeners when inspector closes and array data is changed
              else if(e.getPropertyName().equals("arrayData")) {                             //$NON-NLS-1$
                tableModel.fireTableCellUpdated(rowNumber, colNumber);
              }
            }

          });
          // offset new arrayInspector relative to parent container
          Container cont = XMLTable.this.getTopLevelAncestor();
          Point p = cont.getLocationOnScreen();
          arrayInspector.setLocation(p.x+30, p.y+30);
          arrayInspector.setVisible(true);
          cont.transferFocus();
        }
        return null;
      } // end XMLProperty case
      // value is string
      field.setEditable(true);
      if(value!=null) {
        field.setText(value.toString());
      }
      return panel;
    }

    // Determines when editing starts.
    public boolean isCellEditable(EventObject e) {
      if(e instanceof MouseEvent) {
        MouseEvent me = (MouseEvent) e;
        XMLTable table = (XMLTable) me.getSource();
        int row = table.rowAtPoint(me.getPoint());
        keepFocus = row;
        Object value = tableModel.getValueAt(row, 1);
        // handle special object cases
        if(value instanceof XMLControl) {
          XMLControl childControl = (XMLControl) value;
          if((childControl.getObjectClass()==OSPCombo.class)||(childControl.getObjectClass()==Boolean.class)||(childControl.getObjectClass()==Character.class)) {
            return true;
          }
        }
        if((value instanceof String)||(me.getClickCount()==2)) {
          return true;
        }
      } else if(e instanceof ActionEvent) {
        keepFocus = -2;
        return true;
      }
      return false;
    }

    // Called when editing is completed.
    public Object getCellEditorValue() {
      XMLTable.this.requestFocusInWindow();
      if(field.getBackground()!=defaultBackgroundColor) {
        field.setBackground(defaultBackgroundColor);
        return field.getText();
      }
      return null;
    }

  }

  // refreshes the table
  public void refresh() {
    Runnable runner = new Runnable() {
      public synchronized void run() {
        tableChanged(new TableModelEvent(tableModel, TableModelEvent.HEADER_ROW));
      }

    };
    if(SwingUtilities.isEventDispatchThread()) {
      runner.run();
    } else {
      SwingUtilities.invokeLater(runner);
    }
  }

  public void tableChanged(TableModelEvent e) {
    // pass the tablemodel event to property change listeners
    firePropertyChange("tableData", null, e); //$NON-NLS-1$
    super.tableChanged(e);
  }

  /**
   *    Adds a listener that invokes the given method in the given object when the xml data changes.
   *    The method in the target is invoked with the table's variable name passed as a
   *    parameter.  The method will be invoked for all parameter changes.
   *
   *   @param  methodName   the name of the method
   *   @param  target       the target for the method
   */
  public void addControlListener(String methodName, final Object target) {
    addControlListener(null, methodName, target);
  }

  /**
   *  Adds a listener that invokes the given method in the given object when the xml data changes.
   *  The method in the target is invoked with the table's variable name passed as a
   *  parameter. The method will be invoked for all parameter changes if the parameter name is null.
   *
   * @param  parameterName the name of the parameter that will invoke the method
   * @param  methodName    the name of the method that will be invoked
   * @param  target        the target class for the method
   */
  public void addControlListener(final String parameterName, String methodName, final Object target) {
    Class<?>[] parameters = new Class[] {String.class};
    try {
      final java.lang.reflect.Method m = target.getClass().getMethod(methodName, parameters);
      tableModel.addTableModelListener(new TableModelListener() {
        final String par = parameterName;
        public void tableChanged(TableModelEvent e) {
          if((e.getType()!=TableModelEvent.UPDATE)||(e.getColumn()!=1)||(e.getFirstRow()<0)) {
            return;
          }
          String name = getValueAt(e.getFirstRow(), 0).toString();
          if((par==null)||par.equals(name)) { // invoke the method for all parameters if the parameter name is null
            Object[] args = {name};
            try {
              m.invoke(target, args);
            } catch(Exception ex) {
              ex.printStackTrace();
            }
          }
        }

      });
    } catch(NoSuchMethodException nsme) {
      System.err.println(ControlsRes.getString("XMLTable.ErrorMessage.NoMethod") //$NON-NLS-1$
                         +" "+methodName+"()");                                  //$NON-NLS-1$//$NON-NLS-2$
    }
  }

  // initializes the table
  private void init() {
    setModel(tableModel);
    JTableHeader header = getTableHeader();
    header.setReorderingAllowed(false);
    header.setForeground(Color.BLACK); // set header text color
    setGridColor(Color.BLACK);
    // Override the default tab behavior
    InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    Object key = im.get(tab);
    // SwingJS cannot capture TAB in this way
    if (key != null) {
     Action prevTabAction = getActionMap().get(key); 
     Action tabAction = new AbstractAction() { 
       public void actionPerformed(ActionEvent e) {
        // tab to the next editable cell
        prevTabAction.actionPerformed(e);
        JTable table = (JTable) e.getSource();
        int rowCount = table.getRowCount();
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        while(!table.isCellEditable(row, column)) {
          if(column==0) {
            column = 1;
          } else {
            row += 1;
          }
          if(row==rowCount) {
            row = 0;
          }
          if((row==table.getSelectedRow())&&(column==table.getSelectedColumn())) {
            break;
          }
        }
        table.changeSelection(row, column, false, false);
       }
     };
     getActionMap().put(key, tabAction);
    } else {
    	System.out.println("No previous tab action");
    }

    // enter key starts editing
    Action enterAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        // start editing
        JTable table = (JTable) e.getSource();
        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();
        table.editCellAt(row, column, e);
        Component comp = table.getEditorComponent();
        if(comp instanceof JPanel) {
          JPanel panel = (JPanel) comp;
          comp = panel.getComponent(0);
          if(comp instanceof JTextField) {
            JTextField field = (JTextField) comp;
            OSPCombo combo = null;
            Object value = tableModel.getValueAt(row, column);
            if(value instanceof XMLControl) {
              final XMLControl control = (XMLControl) value;
              if(control.getObjectClass()==OSPCombo.class) {
                combo = (OSPCombo) control.loadObject(null);
              } else if(control.getObjectClass()==Boolean.class) {
                Boolean bool = (Boolean) control.loadObject(null);
                int n = bool.booleanValue() ? 0 : 1;
                combo = new OSPCombo(new String[] {"true", "false"}, n);                //$NON-NLS-1$//$NON-NLS-2$
                combo.addPropertyChangeListener("value", new PropertyChangeListener() { //$NON-NLS-1$
                  public void propertyChange(PropertyChangeEvent e) {
                    OSPCombo combo = (OSPCombo) e.getSource();
                    Boolean bool = new Boolean(combo.getSelectedIndex()==0);
                    control.saveObject(bool);
                  }

                });
              }
            }
            if(combo==null) {
              field.requestFocus();
              field.selectAll();
            } else {
              // add combo listener and show popup
              combo.row = row;
              combo.column = column;
              combo.removePropertyChangeListener("index", comboListener);               //$NON-NLS-1$
              combo.addPropertyChangeListener("index", comboListener);                  //$NON-NLS-1$
              combo.showPopup(field);
            }
          }
        }
      }

    };
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    getActionMap().put(im.get(enter), enterAction);
    // create OSPCombo listener
    comboListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        OSPCombo combo = (OSPCombo) e.getSource();
        int n = ((Integer) e.getOldValue()).intValue();
        if(n!=combo.getSelectedIndex()) {
          combo.firePropertyChange("value", n, combo.getSelectedIndex()); //$NON-NLS-1$
          tableModel.fireTableCellUpdated(combo.row, combo.column);
        }
        combo.removePropertyChangeListener("index", this);                //$NON-NLS-1$
        valueEditor.stopCellEditing();
      }

    };
  }

  // determines whether the specified property is inspectable
  private boolean isInspectable(XMLProperty prop) {
    if(prop.getPropertyType().equals("object")) { //$NON-NLS-1$
      return true;
    }
    if(prop.getPropertyType().equals("array")) { //$NON-NLS-1$
      return ArrayInspector.canInspect(prop);
    }
    if(prop.getPropertyType().equals("collection")) { //$NON-NLS-1$
      return true;
    }
    return false;
  }

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
