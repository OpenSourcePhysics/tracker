/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.tools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TeXParser;

/**
 * A JPanel that manages a table of objects with editable names and expressions.
 *
 * @author Douglas Brown
 */
public class FunctionEditor extends JPanel implements PropertyChangeListener {
  // static constants
	@SuppressWarnings("javadoc")
	public final static String THETA = TeXParser.parseTeX("$\\theta$"); //$NON-NLS-1$
	public final static String OMEGA = TeXParser.parseTeX("$\\omega$"); //$NON-NLS-1$
	public final static String DEGREES = "\u00B0"; //$NON-NLS-1$
  public final static int ADD_EDIT = 0;
  public final static int REMOVE_EDIT = 1;
  public final static int NAME_EDIT = 2;
  public final static int EXPRESSION_EDIT = 3;  
  final static Color LIGHT_BLUE = new Color(204, 204, 255);
  final static Color MEDIUM_RED = new Color(255, 160, 180);
  final static Color LIGHT_RED = new Color(255, 180, 200);
  final static Color LIGHT_GRAY = javax.swing.UIManager.getColor("Panel.background");   //$NON-NLS-1$
  final static Color DARK_RED = new Color(220, 0, 0);
  
  // static fields
  static DecimalFormat decimalFormat;
  static DecimalFormat sciFormat;
  protected static boolean undoEditsEnabled = true;
  protected static String[] editTypes = {"add row", //$NON-NLS-1$
    	"delete row", "edit name", "edit expression"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  static FontRenderContext frc
		  = new FontRenderContext(null,   // no AffineTransform
		                          false,  // no antialiasing
		                          false); // no fractional metrics
  
  // instance fields
  protected ParamEditor paramEditor;
  protected ArrayList<Object> objects = new ArrayList<Object>();
  protected String[] names = new String[0];
  protected ArrayList<Object> sortedObjects = new ArrayList<Object>();
  protected HashSet<String> forbiddenNames = new HashSet<String>();
  protected boolean removablesAtTop = false;
  protected Collection<Object> circularErrors = new HashSet<Object>();
  protected Collection<Object> errors = new HashSet<Object>();
  protected List<Object> evaluate = new ArrayList<Object>();
  protected Table table;
  protected TableModel tableModel = new TableModel();
  protected CellEditor tableCellEditor = new CellEditor();
  protected CellRenderer tableCellRenderer = new CellRenderer();
  protected JScrollPane tableScroller;
  protected JButton newButton;
  protected JButton cutButton;
  protected JButton copyButton;
  protected JButton pasteButton;
  protected JPanel buttonPanel;
  protected JLabel dragLabel;
  protected TitledBorder titledBorder;
  protected FunctionPanel functionPanel;
  protected AbstractButton[] customButtons;
  protected boolean anglesInDegrees;
  protected boolean usePopupEditor = true;
  protected boolean confirmChanges = true;

  static {
    decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(4);
    decimalFormat.setMinimumFractionDigits(0);
    decimalFormat.setMaximumIntegerDigits(3);
    decimalFormat.setMinimumIntegerDigits(1);
    sciFormat = new DecimalFormat("0.0000E0"); //$NON-NLS-1$
  }

  /**
   * No-arg constructor
   */
  public FunctionEditor() {
    super(new BorderLayout());
    createGUI();
    refreshGUI();
  }

  /**
   * Gets the table.
   *
   * @return the table
   */
  public Table getTable() {
    return table;
  }

  /**
   * Override getPreferredSize().
   *
   * @return the table size plus button and instruction heights
   */
  public Dimension getPreferredSize() {
    Dimension dim = table.getPreferredSize();
    dim.height += table.getTableHeader().getHeight();
    dim.height += buttonPanel.getPreferredSize().height;
    dim.height += 1.25*table.getRowHeight()+14;
    return dim;
  }

  /**
   * Replaces the current objects with new ones.
   *
   * @param newObjects a list of objects
   */
  public void setObjects(java.util.List<Object> newObjects) {
    // determine row and column selected
    int row = table.getSelectedRow();
    int col = table.getSelectedColumn();
    objects.clear();
    objects.addAll(newObjects);
    evaluateAll();
    tableModel.fireTableStructureChanged();
    // select same cell
    if(row<table.getRowCount()) {
      table.rowToSelect = row;
      table.columnToSelect = col;
    }
    table.requestFocusInWindow();
    refreshGUI();
  }

  /**
   * Gets a shallow clone of the objects list.
   *
   * @return a list of objects
   */
  public List<Object> getObjects() {
    return new ArrayList<Object>(objects);
  }

  /**
   * Gets an array containing the names of the objects.
   *
   * @return an array of names
   */
  public String[] getNames() {
    return names;
  }

  /**
   * Returns the name of the object.
   *
   * @param obj the object
   * @return the name
   */
  public String getName(Object obj) {
    return null;
  }

  /**
   * Returns the expression of the object.
   *
   * @param obj the object
   * @return the expression
   */
  public String getExpression(Object obj) {
    return null;
  }

  /**
   * Returns the description of the object.
   *
   * @param obj the object
   * @return the description
   */
  public String getDescription(Object obj) {
    return null;
  }

  /**
   * Sets the description of the object. Subclasses should override and call this
   * AFTER changing the object description.
   *
   * @param obj the object
   * @param desc the description
   */
  public void setDescription(Object obj, String desc) {
  	if (obj instanceof Parameter) {
      firePropertyChange("param_description", null, null); //$NON-NLS-1$
  	}
    firePropertyChange("description", null, null); //$NON-NLS-1$
  }

  /**
   * Returns a tooltip for the object.
   *
   * @param obj the object
   * @return the tooltip
   */
  public String getTooltip(Object obj) {
    return null;
  }

  /**
   * Gets an existing object with specified name. May return null.
   *
   * @param name the name
   * @return the object
   */
  public Object getObject(String name) {
    if((name==null)||name.equals("")) { //$NON-NLS-1$   
      return null;
    }
    Iterator<Object> it = objects.iterator();
    while(it.hasNext()) {
      Object next = it.next();
      if(name.equals(getName(next))) {
        return next;
      }
    }
    return null;
  }

  /**
   * Sets the expression of an existing named object, if any.
   *
   * @param name the name
   * @param expression the expression
   * @param postEdit true to post an undoable edit
   */
  public void setExpression(String name, String expression, boolean postEdit) {
    if((name==null)||name.equals("")) { //$NON-NLS-1$   
      return;
    }
    for(int row = 0; row<objects.size(); row++) {
      Object obj = objects.get(row);
      if (name.equals(getName(obj)) && !getExpression(obj).equals(expression)) {
        String prev = getExpression(obj);
        obj = createObject(name, expression, obj);
        objects.remove(row);
        objects.add(row, obj);
        evaluateAll();
        tableModel.fireTableStructureChanged();
        // select row
        if(row>=0) {
          table.changeSelection(row, 1, false, false);
        }
        // inform and pass undoable edit to listeners
        UndoableEdit edit = null;
        if(postEdit && undoEditsEnabled) {
          edit = getUndoableEdit(EXPRESSION_EDIT, expression, row, 1, prev, row, 1, getName(obj));
        }
        firePropertyChange("edit", getName(obj), edit); //$NON-NLS-1$
      }
    }
  }
  
  /**
   * Gets the confirmChanges flag.
   *
   * @return true if users are required to confirm changes to function names
   */
  public boolean getConfirmChanges() {
  	return confirmChanges;
  }

  /**
   * Sets the confirmChanges flag.
   *
   * @param confirm true to require users to confirm changes to function names
   */
  public void setConfirmChanges(boolean confirm) {
  	confirmChanges= confirm;
  }

  /**
   * Adds an object.
   *
   * @param obj the object
   * @param postEdit true to post an undoable edit
   * @return the added object
   */
  public Object addObject(Object obj, boolean postEdit) {
    if(obj==null) {
      return null;
    }
    // determine row number
    int row = objects.size(); // end of table
    if(isRemovable(obj)) {
      if(removablesAtTop) {
        row = getRemovableRowCount();   // after removable rows
      }
    } else if(!removablesAtTop) {
      row = row-getRemovableRowCount(); // before removable rows
    }
    return addObject(obj, row, postEdit, true);
  }

  /**
   * Adds an object at a specified row.
   *
   * @param obj the object
   * @param row the row
   * @param postEdit true to post an undoable edit
   * @param firePropertyChange true to fire a property change event
   * @return the added object
   */
  public Object addObject(Object obj, int row, boolean postEdit, boolean firePropertyChange) {
    obj = createUniqueObject(obj, getName(obj), confirmChanges);
    if(obj==null) {
      return null;
    }
    int undoRow = table.getSelectedRow();
    int undoCol = table.getSelectedColumn();
    java.util.List<Object> newObjects = new ArrayList<Object>(objects);
    newObjects.add(row, obj);
    setObjects(newObjects);
    // select new object
    table.columnToSelect = 0;
    table.rowToSelect = row;
    table.selectOnFocus = true;
    table.requestFocusInWindow();
    // inform and pass undoable edit to listeners
    UndoableEdit edit = null;
    if(postEdit && undoEditsEnabled) {
      edit = getUndoableEdit(ADD_EDIT, obj, row, 0, obj, undoRow, undoCol, getName(obj));
    }
    if(firePropertyChange) {
      firePropertyChange("edit", getName(obj), edit); //$NON-NLS-1$
    }
    refreshGUI();
    return obj;
  }

  /**
   * Removes an object.
   *
   * @param obj the object to remove
   * @param postEdit true to post an undoable edit
   * @return the removed object
   */
  public Object removeObject(Object obj, boolean postEdit) {
    if((obj==null)||!isRemovable(obj)) {
      return null;
    }
    int undoCol = table.getSelectedColumn();
    for(int undoRow = 0; undoRow<objects.size(); undoRow++) {
      Object next = objects.get(undoRow);
      if(next.equals(obj)) {
        objects.remove(obj);
        tableModel.fireTableStructureChanged();
        // select new row
        int row = (undoRow==objects.size()) ? undoRow-1 : undoRow;
        if(row>=0) {
          table.changeSelection(row, 0, false, false);
        }
        // inform and pass undoable edit to listeners
        UndoableEdit edit = null;
        if(postEdit) {
          edit = getUndoableEdit(REMOVE_EDIT, obj, row, 0, obj, undoRow, undoCol, getName(obj));
        }
        evaluateAll();
        firePropertyChange("edit", getName(obj), edit); //$NON-NLS-1$
        refreshGUI();
      }
    }
    return obj;
  }

  /**
   * Refreshes button strings based on current locale.
   */
  public void refreshStrings() {
    refreshGUI();
  }

  /**
   * Responds to property change events.
   *
   * @param e the event
   */
  public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    if(name.equals("focus")||name.equals("edit")) {      //$NON-NLS-1$ //$NON-NLS-2$
      // another table gained focus or changed
      table.clearSelection();
      table.rowToSelect = 0;
      table.columnToSelect = 0;
      table.selectOnFocus = false;
      refreshButtons();
    } else if(e.getPropertyName().equals("clipboard")) { //$NON-NLS-1$
      // clipboard contents have changed
      refreshButtons();
    }
  }

  /**
   * Sets custom buttons on the button panel. Setting buttons to null
   * removes all buttons from this editor.
   */
  public void setCustomButtons(AbstractButton[] buttons) {
    customButtons = buttons;
    if((buttons==null)||(buttons.length==0)) {
      remove(buttonPanel);
      return;
    }
    buttonPanel.removeAll();
    for(int i = 0; i<buttons.length; i++) {
      buttonPanel.add(buttons[i]);
    }
    add(buttonPanel, BorderLayout.NORTH);
  }
  
  /**
   * sets the usePopupEditor flag.
   * 
   * @param popup true to use the popup editor.
   */
  public void setUsePopupEditor(boolean popup) {
  	usePopupEditor = popup;
  }

  /**
   * Gets an undoable edit.
   *
   * @param type may be ADD_EDIT, REMOVE_EDIT, NAME_EDIT, or EXPRESSION_EDIT 
   * @param redo the new state
   * @param redoRow the newly selected row
   * @param redoCol the newly selected column
   * @param undo the previous state
   * @param undoRow the previously selected row
   * @param undoCol the previously selected column
   * @param name the name of the edited object
   */
  protected UndoableEdit getUndoableEdit(int type, Object redo, int redoRow, int redoCol, Object undo, int undoRow, int undoCol, String name) {
    if(type==EXPRESSION_EDIT) {
      ArrayList<AbstractButton> selectedButtons = new ArrayList<AbstractButton>();
      undo = new Object[] {undo, selectedButtons};
      redo = new Object[] {redo, selectedButtons};
      if(customButtons!=null) {
        for(AbstractButton b : customButtons) {
          if(b.isSelected()) {
            selectedButtons.add(b);
          }
        }
      }
    }
    return new DefaultEdit(type, redo, redoRow, redoCol, undo, undoRow, undoCol, name);
  }

  /**
   * Determines if an object's name is editable.
   *
   * @param obj the object
   * @return true if the name is editable
   */
  public boolean isNameEditable(Object obj) {
    return true;
  }

  /**
   * Determines if an object's expression is editable.
   *
   * @param obj the object
   * @return true if the expression is editable
   */
  public boolean isExpressionEditable(Object obj) {
    return true;
  }

  /**
   * Determines if an object is removable.
   *
   * @param obj the object
   * @return true if removable
   */
  protected boolean isRemovable(Object obj) {
    return !isImportant(obj)&&isNameEditable(obj)&&isExpressionEditable(obj);
  }

  /**
   * Determines if an object is important.
   *
   * @param obj the object
   * @return true if important
   */
  protected boolean isImportant(Object obj) {
    return false;
  }

  /**
   * Sets the anglesInDegrees flag. Angles are displayed in degrees
   * when true, radians when false.
   *
   * @param degrees true to display angles in degrees
   */
  public void setAnglesInDegrees(boolean degrees) {
    anglesInDegrees = degrees;
    table.repaint();
  }

  /**
   * Evaluates all current objects.
   */
  public void evaluateAll() {
    // refresh names array
    if(names.length!=objects.size()) {
      names = new String[objects.size()];
    }
    for(int i = 0; i<names.length; i++) {
      names[i] = getName(objects.get(i));
    }
    // sort the objects by name length
    sortedObjects.clear();
    if(objects.size()>0) {
      sortedObjects.add(objects.get(0));
      for(int i = 1; i<objects.size(); i++) {
        int size = sortedObjects.size();
        for(int j = 0; j<size; j++) {
          Object obj = objects.get(i);
          String name = getName(obj);
          if(name.length()>getName(sortedObjects.get(j)).length()) {
            sortedObjects.add(j, obj);
            break;
          } else if(j==size-1) {
            sortedObjects.add(obj);
          }
        }
      }
    }
    // check for circular references
    circularErrors.clear();
    for(Iterator<Object> it = objects.iterator(); it.hasNext(); ) {
      Object next = it.next();
      String name = getName(next);
      if(getReferences(name, null).contains(name)) {
        circularErrors.add(next);
      }
    }
    // find all functions that reference circular errors
    errors.clear();
    for(Iterator<Object> it = objects.iterator(); it.hasNext(); ) {
      Object next = it.next();
      String name = getName(next);
      for(Iterator<Object> it2 = circularErrors.iterator(); it2.hasNext(); ) {
        String badName = getName(it2.next());
        if(getReferences(name, null).contains(badName)) {
          errors.add(next);
          break;
        }
      }
    }
    // establish evaluation order
    evaluate.clear();
    ArrayList<Object> temp = new ArrayList<Object>(objects);
    temp.removeAll(errors);
    ArrayList<String> names = new ArrayList<String>();
    while(!temp.isEmpty()) {
      for(int i = 0; i<temp.size(); i++) {
        Object next = temp.get(i);
        String name = getName(next);
        Set<String> references = getReferences(name, null);
        if(names.containsAll(references)) {
          evaluate.add(next);
          names.add(name);
        }
      }
      temp.removeAll(evaluate);
    }
  }
  
  /**
   * Determines if a test expression is valid.
   *
   * @param statement
   * @return true if valid
   */
  protected boolean isValidExpression(String expression) {
  	Parameter p = new Parameter("xxzz", expression); //$NON-NLS-1$
  	String s = getVariablesString(""); //$NON-NLS-1$
  	String start = ToolsRes.getString("FunctionPanel.Instructions.ValueCell"); //$NON-NLS-1$
  	if (!s.startsWith(start)) {
  		return !Double.isNaN(p.evaluate(new Parameter[0]));
  	}
  	String[] names = s.substring(start.length()).split(" "); //$NON-NLS-1$
  	ArrayList<Object> temp = new ArrayList<Object>();
  	for (String name: names) {
  		Parameter next = new Parameter(name, "1"); //$NON-NLS-1$
  		temp.add(next);
  	}
  	return !Double.isNaN(p.evaluate(temp));
  }

  /**
   * Gets the names of functions referenced in a named function expression
   * either directly or indirectly.
   *
   * @param name the name of the function
   * @param references a Set to add references to (may be null)
   * @return the set filled with names of referenced functions
   */
  protected Set<String> getReferences(String name, Set<String> references) {
    if(references==null) {
      references = new HashSet<String>();
    }
    Object obj = getObject(name);
    if(obj!=null) {
      String eqn = getExpression(obj);
      List<Object> directReferences = new ArrayList<Object>();
      for(Iterator<Object> it = sortedObjects.iterator(); it.hasNext(); ) {
        Object next = it.next();
        if(next==obj) {
          continue;
        }
        name = getName(next);
        if (obj instanceof UserFunction) {
        	// replace function names with # to prevent finding "x" in "exp", etc
          UserFunction func = (UserFunction)obj;
          String[] functionNames = func.getFunctionNames();
          for (String s: functionNames) {
            eqn = eqn.replaceAll(s, "#"); //$NON-NLS-1$
          }
        }
        if(eqn.indexOf(name)>-1) {
          directReferences.add(next);
          if(!references.contains(name)) {
            references.add(name);
            references.addAll(getReferences(name, references));
          }
        }
      }
      setReferences(obj, directReferences);
    }
    return references;
  }

  /**
   * Subclasses implement to set objects referenced in an object's expression.
   */
  protected void setReferences(Object obj, List<Object> referencedObjects) {
    /** empty block */
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
  	titledBorder = BorderFactory.createTitledBorder(""); //$NON-NLS-1$
  	setBorder(titledBorder);
    // create table and scroller
    table = new Table(tableModel);
    tableScroller = new JScrollPane(table);
    tableScroller.createHorizontalScrollBar();
    add(tableScroller, BorderLayout.CENTER);
    buttonPanel = new JPanel(new FlowLayout());
    newButton = new JButton();
    newButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String name = getDefaultName();
        Object obj = createUniqueObject(null, name, false);
        addObject(obj, true);
      }

    });
    cutButton = new JButton();
    cutButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] array = getSelectedObjects();
        copy(array);
        for(int i = array.length; i>0; i--) {
          removeObject(array[i-1], true);
        }
        evaluateAll();
      }

    });
    copyButton = new JButton();
    copyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copy(getSelectedObjects());
      }

    });
    pasteButton = new JButton();
    pasteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        paste();
      }

    });
    buttonPanel.add(newButton);
    buttonPanel.add(copyButton);
    buttonPanel.add(cutButton);
    buttonPanel.add(pasteButton);
    add(buttonPanel, BorderLayout.NORTH);
    MouseListener tableFocuser = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        table.requestFocusInWindow();
        functionPanel.clearSelection();
      }

    };
    table.getTableHeader().addMouseListener(tableFocuser);
    tableScroller.addMouseListener(tableFocuser);
    buttonPanel.addMouseListener(tableFocuser);
    buttonPanel.addMouseListener(tableFocuser);
    addMouseListener(tableFocuser);
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
  	sciFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
  	decimalFormat.setDecimalFormatSymbols(OSPRuntime.getDecimalFormatSymbols());
    int[] rows = table.getSelectedRows();
    int col = table.getSelectedColumn();
    tableModel.fireTableStructureChanged(); // refreshes table header strings
    revalidate();
    for(int i = 0; i<rows.length; i++) {
      table.addRowSelectionInterval(rows[i], rows[i]);
    }
    if(rows.length>0) {
      table.setColumnSelectionInterval(col, col);
      table.requestFocusInWindow();
    }
    newButton.setText(ToolsRes.getString("FunctionEditor.Button.New"));                    //$NON-NLS-1$
    newButton.setToolTipText(ToolsRes.getString("FunctionEditor.Button.New.Tooltip"));     //$NON-NLS-1$
    cutButton.setText(ToolsRes.getString("FunctionEditor.Button.Cut"));                    //$NON-NLS-1$
    cutButton.setToolTipText(ToolsRes.getString("FunctionEditor.Button.Cut.Tooltip"));     //$NON-NLS-1$
    copyButton.setText(ToolsRes.getString("FunctionEditor.Button.Copy"));                  //$NON-NLS-1$
    copyButton.setToolTipText(ToolsRes.getString("FunctionEditor.Button.Copy.Tooltip"));   //$NON-NLS-1$
    pasteButton.setText(ToolsRes.getString("FunctionEditor.Button.Paste"));                //$NON-NLS-1$
    pasteButton.setToolTipText(ToolsRes.getString("FunctionEditor.Button.Paste.Tooltip")); //$NON-NLS-1$
    titledBorder.setTitle(ToolsRes.getString("FunctionEditor.Border.Title")); //$NON-NLS-1$
    refreshButtons();
  }
  
  /**
   * Sets the border title.
   */
  public void setBorderTitle(String title) {
    titledBorder.setTitle(title);	
  }

  /**
   * Refreshes button states.
   */
  protected void refreshButtons() {
    boolean b = getSelectedObject()!=null;
    copyButton.setEnabled(b);
    cutButton.setEnabled(b&&isRemovable(getSelectedObject()));
    pasteButton.setEnabled(getClipboardContents()!=null);
  }

  /**
   * Gets the param editor that defines parameters for functions.
   */
  protected ParamEditor getParamEditor() {
    return paramEditor;
  }

  /**
   * Sets the param editor that defines parameters for functions.
   * By default, the editor pasted in is ignored unless not yet set.
   */
  protected void setParamEditor(ParamEditor editor) {
    if((paramEditor==null)&&(editor!=null)) {
      paramEditor = editor;
      evaluateAll();
      refreshGUI();
    }
  }

  /**
   * Gets the FunctionPanel that manages this editor.
   */
  public FunctionPanel getFunctionPanel() {
    return functionPanel;
  }

  /**
   * Sets the FunctionPanel that contains this editor.
   *
   * @param panel the function panel
   */
  public void setFunctionPanel(FunctionPanel panel) {
    functionPanel = panel;
  }

  /**
   * Returns the default name for newly created objects.
   */
  protected String getDefaultName() {
    return ToolsRes.getString("FunctionEditor.New.Name.Default"); //$NON-NLS-1$
  }

  /**
   * Returns a String with the names of variables available for expressions.
   * This default returns the names of all objects in this panel
   * except the selected object.
   */
  protected String getVariablesString(String separator) {
    StringBuffer vars = new StringBuffer(""); //$NON-NLS-1$
    int init = vars.length();
    boolean firstItem = true;
    String nameToSkip = getName(getSelectedObject());
    for(int i = 0; i<names.length; i++) {
      if(names[i].equals(nameToSkip)) {
        continue;
      }
      if(!firstItem) {
        vars.append(" "); //$NON-NLS-1$
      }
      vars.append(names[i]);
      firstItem = false;
    }
    if(vars.length()==init) {
      return ToolsRes.getString("FunctionPanel.Instructions.Help"); //$NON-NLS-1$
    }
    return ToolsRes.getString("FunctionPanel.Instructions.ValueCell") //$NON-NLS-1$
           +separator+vars.toString(); 
  }

  /**
   * Returns the number of removable rows.
   */
  private int getRemovableRowCount() {
    int i = 0;
    for(Iterator<Object> it = objects.iterator(); it.hasNext(); ) {
      Object obj = it.next();
      if(isRemovable(obj)) {
        i++;
      }
    }
    return i;
  }

  /**
   * Returns the number of editable rows.
   */
  protected int getPartlyEditableRowCount() {
    int i = 0;
    for(Iterator<Object> it = objects.iterator(); it.hasNext(); ) {
      Object obj = it.next();
      if(isNameEditable(obj)||isExpressionEditable(obj)) {
        i++;
      }
    }
    return i;
  }

  /**
   * Returns true if the object expression is invalid.
   */
  protected boolean isInvalidExpression(Object obj) {
    return false;
  }

  /**
   * Returns true if any objects have invalid expressions.
   */
  public boolean containsInvalidExpressions() {
    for(Iterator<Object> it = objects.iterator(); it.hasNext(); ) {
      if(isInvalidExpression(it.next())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Copies an array of objects to the clipboard.
   *
   * @param array the array
   */
  private void copy(Object[] array) {
    if((array!=null)&&(array.length>0)) {
      XMLControl control = new XMLControlElement(this);
      control.setValue("selected", array);         //$NON-NLS-1$
      StringSelection ss = new StringSelection(control.toXML());
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(ss, ss);
      pasteButton.setEnabled(true);
      firePropertyChange("clipboard", null, null); //$NON-NLS-1$
    }
  }

  /**
   * Pastes the clipboard contents.
   */
  protected void paste() {
    XMLControl[] controls = getClipboardContents();
    if(controls==null) {
      return;
    }
    for(int i = 0; i<controls.length; i++) {
      // create a new object
      Object obj = controls[i].loadObject(null);
      addObject(obj, true);
    }
    evaluateAll();
  }

  /**
   * Gets the clipboard contents.
   */
  protected XMLControl[] getClipboardContents() {
    try {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable tran = clipboard.getContents(null);
      String dataString = (String) tran.getTransferData(DataFlavor.stringFlavor);
      if(dataString!=null) {
        XMLControlElement control = new XMLControlElement();
        control.readXML(dataString);
        if(control.getObjectClass()==this.getClass()) {
          java.util.List<Object> list = control.getPropertyContent();
          for(int i = 0; i<list.size(); i++) {
            Object next = list.get(i);
            if(next instanceof XMLProperty) {
              XMLProperty prop = (XMLProperty) next;
              if(prop.getPropertyName().equals("selected")) { //$NON-NLS-1$
                return prop.getChildControls();
              }
            }
          }
          return null;
        }
      }
    } catch(Exception ex) {
      /** empty block */
    }
    return null;
  }

  /**
   * Returns the currently selected object, if any.
   */
  protected Object getSelectedObject() {
    int row = table.getSelectedRow();
    if(row==-1) {
      return null;
    }
    return objects.get(row);
  }

  /**
   * Returns the currently selected objects, if any.
   */
  protected Object[] getSelectedObjects() {
    int[] rows = table.getSelectedRows();
    Object[] selected = new Object[rows.length];
    for(int i = 0; i<rows.length; i++) {
      selected[i] = objects.get(rows[i]);
    }
    return selected;
  }

  /**
   * Creates an object with specified name and expression. An existing object
   * may be passed in for modification or cloning, but there is no guarantee
   * the same object will be returned.
   *
   * @param name the name
   * @param expression the expression
   * @param obj an object to assign values (may be null)
   * @return the object
   */
  protected Object createObject(String name, String expression, Object obj) {
    return null;
  }

  /**
   * Returns true if a name is forbidden or in use.
   *
   * @param obj the object (may be null)
   * @param name the proposed name for the object
   * @return true if disallowed
   */
  protected boolean isDisallowedName(Object obj, String name) {
    if(forbiddenNames.contains(name)) {
      return true;
    }
    Iterator<Object> it = objects.iterator();
    while(it.hasNext()) {
      Object next = it.next();
      if(next==obj) {
        continue;
      }
      if(name.equals(getName(next))) {
        return true;
      }
    }
    if((paramEditor!=null)&&(paramEditor!=this)) { // check for
      Parameter[] params = paramEditor.getParameters();
      for(int i = 0; i<params.length; i++) {
        if((params[i]!=obj)&&name.equals(params[i].getName())) {
          return true;
        }
      }
    }
    try {
			Double.parseDouble(name);
			return true;
		} catch (NumberFormatException e) {
		}
    return false;
  }

  /**
   * Returns a valid function name by removing spaces and symbols.
   *
   * @param proposed the proposed name
   * @return a valid name
   */
  private String getValidName(String proposedName) {
    if(proposedName==null || proposedName.trim().equals("")) { //$NON-NLS-1$   
      return "";                         //$NON-NLS-1$
    }
    String name = proposedName;
    ArrayList<String> invalid = getInvalidTokens(name);
    while(!invalid.isEmpty()) {
      for(Iterator<String> it = invalid.iterator(); it.hasNext(); ) {
        String next = it.next();
        int n = name.indexOf(next);
        while(n>-1) {
          name = (n==0) ? name.substring(next.length()) : name.substring(0, n)+name.substring(n+next.length());
          n = name.indexOf(next);
        }
      }
      Object input = JOptionPane.showInputDialog(FunctionEditor.this, 
      		ToolsRes.getString("FunctionEditor.Dialog.InvalidName.Message"), //$NON-NLS-1$
      		ToolsRes.getString("FunctionEditor.Dialog.InvalidName.Title"), //$NON-NLS-1$
          JOptionPane.WARNING_MESSAGE, null, null, name);
      if(input==null) {
        return null;
      }
      if(input.equals(name)) {
        break;
      }
      name = input.toString();
      invalid = getInvalidTokens(name);
    }
  	// warn users and don't accept names that start with a number
  	try {
  		String test = name.substring(0, Math.min(1, name.length()));
			Double.parseDouble(test);
    	JOptionPane.showMessageDialog(FunctionEditor.this, 
    			ToolsRes.getString("FunctionEditor.Dialog.InvalidNumberInName.Text"), //$NON-NLS-1$
    			ToolsRes.getString("FunctionEditor.Dialog.InvalidName.Title"), //$NON-NLS-1$
    			JOptionPane.WARNING_MESSAGE);
			return ""; //$NON-NLS-1$
		} catch (NumberFormatException e) {
		}
    return name;
  }

  /**
   * Returns a list of invalid tokens found in the name.
   * Invalid tokens include spaces and mathematical symbols.
   *
   * @param name the name
   * @return a list of invalid tokens
   */
  private ArrayList<String> getInvalidTokens(String name) {
    ArrayList<String> invalid = new ArrayList<String>();
    if(name.indexOf(" ")>-1) { //$NON-NLS-1$
      invalid.add(" "); //$NON-NLS-1$
    }
    String[] suspects = FunctionTool.parserOperators;
    for(int i = 0; i<suspects.length; i++) {
      if(name.indexOf(suspects[i])>-1) {
        invalid.add(suspects[i]);
      }
    }
    return invalid;
  }

  /**
   * Creates an object with a unique name.
   *
   * @param obj the object (may be null)
   * @param proposedName the proposed name
   * @param confirmChanges true to require user to confirm changes
   * @return the object
   */
  protected Object createUniqueObject(Object obj, String proposedName, boolean confirmChanges) {
    // construct a unique name from that proposed if nec
    proposedName = getValidName(proposedName);
    if(proposedName==null || proposedName.trim().equals("")) { //$NON-NLS-1$   
      return null;
    }
    String name = proposedName;
    while(isDisallowedName(obj, proposedName)) {
      int i = 0;
      while(isDisallowedName(obj, name)) {
        i++;
        name = proposedName+i;
      }
      if(!confirmChanges) {
        break;
      }
      Object input = JOptionPane.showInputDialog(this, "\""+proposedName+"\" "+ //$NON-NLS-1$ //$NON-NLS-2$
        ToolsRes.getString("FunctionEditor.Dialog.DuplicateName.Message"),      //$NON-NLS-1$
          ToolsRes.getString("FunctionEditor.Dialog.DuplicateName.Title"),      //$NON-NLS-1$
            JOptionPane.WARNING_MESSAGE, null, null, name);
      if(input==null) {
        return null;
      }
      if(input.equals("")||input.equals(name)) {                                //$NON-NLS-1$   
        break;
      }
      name = proposedName = input.toString();
    }
    String expression = (obj==null) ? "0" : getExpression(obj); //$NON-NLS-1$
    return createObject(name, expression, obj);
  }

  /**
   * Class description
   *
  */
  public class Table extends JTable {
    public boolean selectOnFocus = true;
    int rowToSelect, columnToSelect;

    // constructor
    Table(TableModel model) {
      setModel(model);
      setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      setColumnSelectionAllowed(false);
      getTableHeader().setReorderingAllowed(false);
      setGridColor(Color.BLACK);
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          int row = rowAtPoint(e.getPoint());
          int col = columnAtPoint(e.getPoint());
        	if (OSPRuntime.isPopupTrigger(e)) {
        		String name = getValueAt(row, 0).toString();
        		if (name.contains(THETA) || name.contains(OMEGA)) {
        			JPopupMenu popup = new JPopupMenu();
        			JMenuItem item = new JMenuItem();
        			item.setText(anglesInDegrees?
        					ToolsRes.getString("FunctionEditor.Popup.MenuItem.SwitchToRadians"): //$NON-NLS-1$
        					ToolsRes.getString("FunctionEditor.Popup.MenuItem.SwitchToDegrees")); //$NON-NLS-1$
        	    item.addActionListener(new ActionListener() {
        	      public void actionPerformed(ActionEvent e) {
        	      	setAnglesInDegrees(!anglesInDegrees);
                  FunctionEditor.this.firePropertyChange("angles_in_radians", null, !anglesInDegrees); //$NON-NLS-1$
        	      }
        	    });
        			popup.add(item);
        			popup.show(table, e.getX(), e.getY());
        		}
        	}
          table.rowToSelect = row;
          table.columnToSelect = col;
          if(!tableModel.isCellEditable(row, col)) {
            functionPanel.clearSelection();
            selectOnFocus = false;
          } else if(e.getClickCount()==1) {
            functionPanel.refreshInstructions(FunctionEditor.this, false, col);
            selectOnFocus = table.hasFocus();
          }
        }

        public void mouseClicked(MouseEvent e) {
        	if (OSPRuntime.isPopupTrigger(e)) {
            int col = columnAtPoint(e.getPoint());
            if (col!=0) return;
            int row = rowAtPoint(e.getPoint());
            if (tableModel.isCellEditable(row, col)) {
            	String name = (String)table.getValueAt(row, col);
            	Object obj = getObject(name);
            	String desc = getDescription(obj);
            	String message = ToolsRes.getString("FunctionEditor.Dialog.SetDescription.Message"); //$NON-NLS-1$
              message += " \""+name+"\""; //$NON-NLS-1$ //$NON-NLS-2$
            	Object input = JOptionPane.showInputDialog(FunctionEditor.this, 
              		message,
              		ToolsRes.getString("FunctionEditor.Dialog.SetDescription.Title"), //$NON-NLS-1$
                  JOptionPane.PLAIN_MESSAGE, null, null, desc);
              if (input==null || input.equals(desc)) {
                return;
              }
              desc = input.toString();
            	setDescription(obj, desc);
            }
        	}
        }

      });
      addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
          firePropertyChange("focus", null, null); //$NON-NLS-1$
          if(getRowCount()==0) {
            functionPanel.tabToNext(FunctionEditor.this);
            return;
          }
          if(selectOnFocus&&(getRowCount()>0)) {
            selectCell(rowToSelect, columnToSelect);
            int col = table.getSelectedColumn();
            functionPanel.refreshInstructions(FunctionEditor.this, false, col);
          }
          selectOnFocus = true;
        }
        public void focusLost(FocusEvent e) {
          rowToSelect = Math.max(0, getSelectedRow());
          columnToSelect = Math.max(0, getSelectedColumn());
        }

      });
      // enter key action starts editing
      InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      Action enterAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          // start editing
          JTable table = (JTable) e.getSource();
          int row = table.getSelectedRow();
          int column = table.getSelectedColumn();
          table.editCellAt(row, column, e);
          FunctionEditor.this.tableCellEditor.field.requestFocus();
          FunctionEditor.this.tableCellEditor.field.selectAll();
        }

      };
      KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
      getActionMap().put(im.get(enter), enterAction);
      // tab key tabs to next editable cell or component
      KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
      Action tabAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          int rowCount = table.getRowCount();
          int row = table.rowToSelect;
          int col = table.columnToSelect;
          boolean atEnd = ((col==1)&&(row==rowCount-1));
          // determine next cell row and column
          col = (col==0) ? 1 : 0;
          row = (col==0) ? ((row==getRowCount()-1) ? 0 : row+1) : row;
          if(table.isEditing()) {
            table.rowToSelect = row;
            table.columnToSelect = col;
            tableCellEditor.stopCellEditing();
          }
          if(atEnd) {
            functionPanel.tabToNext(FunctionEditor.this);
            table.clearSelection();
          } else {
            table.requestFocusInWindow();
            selectCell(row, col);
            functionPanel.refreshInstructions(FunctionEditor.this, false, col);
          }
        }

      };
      getActionMap().put(im.get(tab), tabAction);
    }

    public void selectCell(int row, int col) {
      // trap for high row numbers
      if(row==getRowCount()) {
        row = getRowCount()-1;
        col = 0;
      }
      if(row==-1) {
        return;
      }
      while(!isCellEditable(row, col)) {
        if(col==0) {
          col = 1;
        } else {
          col = 0;
          row += 1;
        }
        if(row==getRowCount()) {
          row = 0;
        }
        if((row==getSelectedRow())&&(col==getSelectedColumn())) {
          break;
        }
      }
      table.rowToSelect = row;
      table.columnToSelect = col;
      table.changeSelection(row, col, false, false);
    }

    // gets the cell editor
    public TableCellEditor getCellEditor(int row, int column) {
      return tableCellEditor;
    }

    // gets the cell renderer
    public TableCellRenderer getCellRenderer(int row, int column) {
      return tableCellRenderer;
    }

    public void setFont(Font font) {
      super.setFont(font);
      getTableHeader().setFont(font);
      tableCellRenderer.font = font;
      tableCellEditor.field.setFont(font);
      int size = Math.max(font.getSize(), 24);
      tableCellEditor.popupField.setFont(font.deriveFont((float)size));
      setRowHeight(font.getSize()+4);
    }

  }

  /**
   * The table model.
   */
  protected class TableModel extends AbstractTableModel {
    boolean settingValue = false;

    // returns the number of columns in the table
    public int getColumnCount() {
      return 2;
    }

    // returns the number of rows in the table
    public int getRowCount() {
      return objects.size();
    }

    // gets the name of the column
    public String getColumnName(int col) {
      return(col==0) ? ToolsRes.getString("FunctionEditor.Table.Column.Name") : //$NON-NLS-1$
        ToolsRes.getString("FunctionEditor.Table.Column.Value");                //$NON-NLS-1$
    }

    // gets the value in a cell
    public Object getValueAt(int row, int col) {
      Object obj = objects.get(row);
      String name = getName(obj);
      if (col==0) return name;
      String expression = getExpression(obj);
      try {
        // for angles in degrees, convert
	      if (anglesInDegrees && 
	      		(name.indexOf(THETA)>-1 || name.indexOf(OMEGA)>-1)) {
	        // use periods as decimal separators for parsing
	        // but don't make substitutions in "if" statements since they use commas
	        String express = expression;
	        if(express.indexOf("if")==-1) { //$NON-NLS-1$
	        	express = express.replaceAll(",", "."); //$NON-NLS-1$ //$NON-NLS-2$
	        }
					double value = Double.parseDouble(express);
	      	String s = format(value*180/Math.PI, 0.0001);
					if (name.indexOf(THETA)>-1)
						s += DEGREES;
					return s;		      	
	      }
	      // for other names, return expression with appropriate decimal separator
        if(expression.indexOf("if")==-1) { //$NON-NLS-1$
		      boolean isComma = ','==sciFormat.getDecimalFormatSymbols().getDecimalSeparator();
	        String express = expression;
        	if (isComma) express = express.replaceAll("\\.", ","); //$NON-NLS-1$ //$NON-NLS-2$
        	else express = express.replaceAll(",", "."); //$NON-NLS-1$ //$NON-NLS-2$
        	return express;
        }
	      return expression;
//	      return format(value, 0);
			} catch (NumberFormatException e) {
			}
      return expression;
    }

    // changes the value of a cell
    public void setValueAt(Object value, int row, int col) {
      if(settingValue) {
        return;
      }
      if(value instanceof String) {
        String val = (String) value;
        int n = val.indexOf(DEGREES);
        if (n>-1)
        	val = val.substring(0, n);
        // get previous state for undoable edit
        String prev = null;
        int type = 0;
        Object obj = objects.get(row);
        if(col==0) { // name
          prev = getName(obj);
          type = NAME_EDIT;
          settingValue = true;
          if(!val.equals(prev)) {
            obj = createUniqueObject(obj, val, true);
            // name may have changed
            val = getName(obj);
          }
          settingValue = false;
          if (obj==null || val.equals(prev)) {
            functionPanel.refreshInstructions(FunctionEditor.this, false, 0);
            return;
          }
          objects.remove(row);
          objects.add(row, obj);
        } 
        else {  // expression
          prev = getExpression(obj);
          type = EXPRESSION_EDIT;
          if(val.equals(prev)) {
            functionPanel.refreshInstructions(FunctionEditor.this, false, 1);
            return;
          }
          if(val.equals("")) {                          //$NON-NLS-1$   
            val = "0";                                  //$NON-NLS-1$  
          }
          String name = getName(obj);          
          if (anglesInDegrees && 
          		(name.indexOf(THETA)>-1 || name.indexOf(OMEGA)>-1)) {
            try {
      				double d = Double.parseDouble(val);
      				val = String.valueOf(d*Math.PI/180);
      			} catch (NumberFormatException e) {  				
      			}
          }
          obj = createObject(getName(obj), val, obj);
          objects.remove(row);
          objects.add(row, obj);
        }
        evaluateAll();
        table.repaint();
        UndoableEdit edit = null;
        if (undoEditsEnabled) {
	        // create undoable edit
	        edit = getUndoableEdit(type, val, row, col, prev, row, col, getName(obj));
        }
	      // inform listeners
	      firePropertyChange("edit", getName(obj), edit); //$NON-NLS-1$
        functionPanel.refreshInstructions(FunctionEditor.this, false, col);
      }
    }

    // determines if a cell is editable
    public boolean isCellEditable(int row, int col) {
      Object obj = objects.get(row);
      return((col==0)&&isNameEditable(obj))||((col==1)&&isExpressionEditable(obj));
    }

  }

  private class CellEditor extends AbstractCellEditor implements TableCellEditor {
    JPanel panel = new JPanel(new BorderLayout());
    JTextField field = new JTextField();
    boolean keyPressed = false;
    boolean mouseClicked = false;
    JDialog popupEditor;
    JPanel editorPane;
    JPanel dragPane;
    JTextField popupField = new JTextField();
    JTextPane variablesPane;
    JButton revertButton;
    ValueMouseControl valueMouseController;
    int minPopupWidth, varBegin, varEnd;
    Object prevObject;
    String prevName, prevExpression;

    // Constructor.
    CellEditor() {
      panel.add(field, BorderLayout.CENTER);
      panel.setOpaque(false);
      panel.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 2));
      field.setBorder(null);
      field.setEditable(true);
      field.setFont(field.getFont().deriveFont(18f));
      field.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            keyPressed = true;
            stopCellEditing();
          } else {
            field.setBackground(Color.yellow);
          }
        }

      });
      field.addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) {
        	if (usePopupEditor) {
            stopCellEditing();
            undoEditsEnabled = true;
        	}
          mouseClicked = false;
          table.clearSelection();
        }
        public void focusLost(FocusEvent e) {
          if(!mouseClicked) {
            stopCellEditing();
          }
          if(keyPressed) {
            keyPressed = false;
            table.requestFocusInWindow();
          }
        }

      });
    }
    
    // Gets the component to be displayed while editing.
    public Component getTableCellEditorComponent(JTable atable, Object value, boolean isSelected, int row, int column) {
      table.rowToSelect = row;
      table.columnToSelect = column;
      if (usePopupEditor) {
      	undoEditsEnabled = false;
      	JDialog popup = getPopupEditor();
        if (functionPanel.functionTool!=null) {
        	// set font level of popup editor
	      	int level = functionPanel.functionTool.getFontLevel();
	      	FontSizer.setFonts(popup, level);
        }      	
        dragLabel.setText(ToolsRes.getString("FunctionEditor.DragLabel.Text")); //$NON-NLS-1$

	      prevObject = objects.get(row);
	      if (prevObject!=null) {
	      	prevName = getName(prevObject);
	      	prevExpression = getExpression(prevObject);
	      }

        String val = value.toString();
        if (prevObject!=null && column>0) {
        	if (val.endsWith(DEGREES)) {
        		val = val.substring(0, val.length()-1);
        	}
        	else {
        		val = prevExpression;
        	}
        }
        
        popupField.setText(val);
	      popupField.requestFocusInWindow();
	  		try {
					String s = popupField.getText();
					setInitialValue(s);
				} catch (NumberFormatException ex) {
				}

	      popupField.selectAll();
	      popupField.setBackground(Color.WHITE);
	      if (column==1) {
		      variablesPane.setText(getVariablesString(":\n")); //$NON-NLS-1$
	        StyledDocument doc = variablesPane.getStyledDocument();
	        Style blue = doc.getStyle("blue"); //$NON-NLS-1$
	        doc.setCharacterAttributes(0, variablesPane.getText().length(), blue, false);
	        popup.getContentPane().add(variablesPane, BorderLayout.CENTER);
	      }
	      else {
	        popup.getContentPane().remove(variablesPane);
	      }
	      Rectangle cell = table.getCellRect(row, column, true);
	      minPopupWidth = cell.width+2;
	      Dimension dim = resizePopupEditor();
	      Point p = table.getLocationOnScreen();
	      popup.setLocation(p.x+cell.x+cell.width/2-dim.width/2, 
	      		p.y+cell.y+cell.height/2-dim.height/2);
	      popup.setVisible(true);
      }
      else {
      	field.setText(value.toString());
        functionPanel.refreshInstructions(FunctionEditor.this, true, column);
        functionPanel.tableEditorField = field;
      }
      return panel;
    }
    
    void setInitialValue(final String stringValue) {
    	Runnable runner = new Runnable() {
    		public void run() {
		    	JDialog editor = getPopupEditor();
    			try {
    				String val=stringValue.replaceAll(",","."); //$NON-NLS-1$ //$NON-NLS-2$
    				if ("".equals(val)) val = "0"; //$NON-NLS-1$ //$NON-NLS-2$
    				double value = Double.parseDouble(val);
    				valueMouseController.prevValue = value;
    				popupField.setToolTipText(ToolsRes.getString("FunctionEditor.PopupField.Tooltip")); //$NON-NLS-1$
    				revertButton.setToolTipText(ToolsRes.getString("FunctionEditor.Button.Revert.Tooltip")); //$NON-NLS-1$
    				variablesPane.setToolTipText(ToolsRes.getString("FunctionEditor.VariablesPane.Tooltip")); //$NON-NLS-1$
    	  		int row = table.rowToSelect;
    	  		String tooltip = ToolsRes.getString("FunctionEditor.DragLabel.Tooltip"); //$NON-NLS-1$
            dragLabel.setToolTipText(tooltip);
    	  		String name = (String)table.getValueAt(row, 0);
    	  		if (!name.equals("t")) { //$NON-NLS-1$
    	      	editor.getContentPane().add(dragPane, BorderLayout.SOUTH);
    	  		}
    	      else {
    	      	editor.getContentPane().remove(dragPane);
    	      }
    			} catch (NumberFormatException e) {
    				editor.getContentPane().remove(dragPane);
    			}
        	editor.pack();
    		}
    	};
      if(SwingUtilities.isEventDispatchThread()) {
      	runner.run();
      } else {
        SwingUtilities.invokeLater(runner);
      }
    }

    // Determines when editing starts.
    public boolean isCellEditable(EventObject e) {
      if(e instanceof MouseEvent) {
        firePropertyChange("focus", null, null); //$NON-NLS-1$
        MouseEvent me = (MouseEvent) e;
        if(me.getClickCount()==2) {
          mouseClicked = true;
          Runnable runner = new Runnable() {
            public synchronized void run() {
              field.selectAll();
            }
          };
          SwingUtilities.invokeLater(runner);
          return true;
        }
      } else if((e==null)||(e instanceof ActionEvent)) {
        return true;
      }
      return false;
    }
    
    // Called when editing is completed.
    public Object getCellEditorValue() {
    	if (usePopupEditor) {
        popupField.setBackground(Color.WHITE);
    	}
      field.setBackground(Color.WHITE);
      // revalidate table to keep cell widths correct (workaround)
      Runnable runner = new Runnable() {
        public synchronized void run() {
        	table.revalidate();
        }
      };
      SwingUtilities.invokeLater(runner);
      return field.getText();
    }

    // Resizes the popup editor and returns the new size
    private Dimension resizePopupEditor() {
    	String s = popupField.getText();
    	Font font = popupField.getFont();
    	Rectangle rect = font.getStringBounds(s, frc).getBounds();
    	int h = rect.height;
    	int w = Math.max(minPopupWidth, rect.width+32);
    	if (table.columnToSelect==1) {
	    	s = variablesPane.getText();
	    	int n = s.indexOf("\n"); //$NON-NLS-1$
	    	s = s.substring(n+1);
	    	font = variablesPane.getFont().deriveFont(Font.BOLD);
	    	rect = font.getStringBounds(s, frc).getBounds();
	    	w = Math.max(w, rect.width);
    	}
    	Dimension dim = new Dimension(w, h);    	
    	editorPane.setPreferredSize(dim);
      popupEditor.pack();
      dim.width = popupEditor.getWidth();
      return dim;
    }
    
    // Gets the popup editor
    private JDialog getPopupEditor() {
    	if (popupEditor==null) {
        popupField.setEditable(true);
        Font font = popupField.getFont().deriveFont(24f);
      	int level = functionPanel.functionTool.getFontLevel();
        font = FontSizer.getResizedFont(font, level);
        popupField.setFont(font);
        popupField.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            if(e.getKeyCode()==KeyEvent.VK_ENTER) {
            	String text = popupField.getText().trim();
            	char separator = sciFormat.getDecimalFormatSymbols().getDecimalSeparator();
              // warn of if statements that fail if user expects comma separator to work
              if (separator==',') {
              	if (!isValidExpression(text)) {
	              	// warn that if statements can use only periods for separators
              		JOptionPane.showMessageDialog(FunctionEditor.this,
              				ToolsRes.getString("FunctionEditor.Dialog.IfStatementError.Message1") //$NON-NLS-1$
              				+"\n"+ToolsRes.getString("FunctionEditor.Dialog.IfStatementError.Message2"),  //$NON-NLS-1$ //$NON-NLS-2$
              				ToolsRes.getString("FunctionEditor.Dialog.IfStatementError.Title"),  //$NON-NLS-1$
              				JOptionPane.ERROR_MESSAGE);
              		return;
              	}
              }

            	// restore previous name and expression for undoable edit
            	// in case they were changed with mouse
          		int row = table.rowToSelect;
	            objects.remove(row);
	            objects.add(row, prevObject);
	            if (table.columnToSelect==1) {
//	          		setExpression(prevName, prevExpression, false);
	              table.setValueAt(prevExpression, row, 1);
	            }
	            // be sure editor field has correct text
              field.setText(text);

            	undoEditsEnabled = true;
              keyPressed = true;
            	popupEditor.setVisible(false);
	            if (table.columnToSelect==1) {
	            	// explicitly set value in case focus listener not triggered!
	              table.setValueAt(text, row, 1);
	            }
              field.requestFocusInWindow(); // triggers call to stopCellEditing()
              field.selectAll();
            } 
            else {
            	popupField.setBackground(Color.yellow);
            	resizePopupEditor();
            }
          }
          public void keyReleased(KeyEvent e) {
            if(e.getKeyCode()==KeyEvent.VK_ENTER)
            	return;
          	setInitialValue(popupField.getText());
          }
        });
        
        // create variables pane
        variablesPane = new JTextPane() {
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
        variablesPane.setEditable(false);
        variablesPane.setFocusable(false);
        variablesPane.setBorder(popupField.getBorder());
        font = popupField.getFont().deriveFont(14f);
        font = FontSizer.getResizedFont(font, level);
        variablesPane.setFont(font);
        StyledDocument doc = variablesPane.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(def, "SansSerif"); //$NON-NLS-1$
        Style blue = doc.addStyle("blue", def); //$NON-NLS-1$
        StyleConstants.setBold(blue, false);
        StyleConstants.setForeground(blue, Color.blue);
        Style red = doc.addStyle("red", blue); //$NON-NLS-1$
        StyleConstants.setBold(red, true);
        StyleConstants.setForeground(red, Color.red);
        varBegin = varEnd = 0;
        variablesPane.addMouseListener(new MouseAdapter() {
          public void mousePressed(MouseEvent e) {
            if(varEnd==0) {
              return;
            }
            variablesPane.setCaretPosition(varBegin);
            variablesPane.moveCaretPosition(varEnd);
            popupField.replaceSelection(variablesPane.getSelectedText());
            popupField.setBackground(Color.yellow);
          	setInitialValue(popupField.getText());
          }
          public void mouseExited(MouseEvent e) {
            StyledDocument doc = variablesPane.getStyledDocument();
            Style blue = doc.getStyle("blue"); //$NON-NLS-1$
            doc.setCharacterAttributes(0, variablesPane.getText().length(), blue, false);
            varBegin = varEnd = 0;
          }

        });
        variablesPane.addMouseMotionListener(new MouseMotionAdapter() {
          public void mouseMoved(MouseEvent e) {
            varBegin = varEnd = 0;
            // select and highlight the variable under mouse
            String text = variablesPane.getText();
            // first separate the instructions from the variables
            int startVars = text.indexOf(":\n");     //$NON-NLS-1$
            if(startVars==-1) {
              return;
            }
            startVars += 2;
            String vars = text.substring(startVars);
            StyledDocument doc = variablesPane.getStyledDocument();
            Style blue = doc.getStyle("blue"); //$NON-NLS-1$
            Style red = doc.getStyle("red");   //$NON-NLS-1$
            int beginVar = variablesPane.viewToModel(e.getPoint())-startVars;
            if(beginVar<0) {  // mouse is over instructions
              doc.setCharacterAttributes(0, text.length(), blue, false);
              return;
            }
            while (beginVar>0) {
	            // back up to preceding space
	            String s = vars.substring(0, beginVar);
	            if (s.endsWith(" ")) //$NON-NLS-1$
	            	break;
	            beginVar--;
            }
            varBegin = beginVar+startVars;
            // find following comma, space or end
            String s = vars.substring(beginVar);
            int len = s.indexOf(",");          //$NON-NLS-1$
            if(len==-1) len = s.indexOf(" ");  //$NON-NLS-1$
            if(len==-1) len = s.length();
           // set variable bounds and character style
            varEnd = varBegin+len;
            doc.setCharacterAttributes(0, varBegin, blue, false);
            doc.setCharacterAttributes(varBegin, len, red, false);
            doc.setCharacterAttributes(varEnd, text.length()-varEnd, blue, false);
          }
        });

        // create drag pane
        dragPane = new JPanel(new BorderLayout());
        dragPane.setBackground(new Color(240, 255, 240)); // very light green
        dragLabel = new JLabel();
        dragLabel.setHorizontalAlignment(JLabel.CENTER);
        Border line = BorderFactory.createLineBorder(LIGHT_BLUE);
        Border space = BorderFactory.createEmptyBorder(2, 3, 2, 3);        
        dragLabel.setBorder(BorderFactory.createCompoundBorder(line, space));
        font = popupField.getFont().deriveFont(12f);
        font = FontSizer.getResizedFont(font, level);
        dragLabel.setFont(font);
        dragLabel.setForeground(Color.green.darker().darker());
        dragPane.add(dragLabel, BorderLayout.CENTER);
        
        valueMouseController = new ValueMouseControl(tableCellEditor);
        dragLabel.addMouseListener(valueMouseController);
        dragLabel.addMouseMotionListener(valueMouseController);
        
        // make revert button
        String imageFile = "/org/opensourcephysics/resources/tools/images/close.gif"; //$NON-NLS-1$
        Icon icon = ResourceLoader.getIcon(imageFile);
        revertButton = new JButton(icon);
        line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        space = BorderFactory.createEmptyBorder(0,2,0,2);
        revertButton.setBorder(BorderFactory.createCompoundBorder(line, space));
        revertButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
          	if (prevObject!=null) {
	          	// restore original value
          		if (table.columnToSelect==1) {
          			table.setValueAt(prevExpression, table.rowToSelect, 1);
          			field.setText(prevExpression);
          		}
	            else {
          			table.setValueAt(prevName, table.rowToSelect, 0);
          			field.setText(prevName);
	            }
          		
//	            objects.remove(row);
//	            objects.add(row, prevObject);
//	            if (table.getSelectedColumn()==1) {
//	          		setExpression(prevName, prevExpression, false);
//	          		field.setText(prevExpression);
//	            }
          		stopCellEditing();
              undoEditsEnabled = true;
          	}
          	popupField.setBackground(Color.WHITE);
          	popupEditor.setVisible(false);
          }
        });
        // assemble components
	      Frame frame = JOptionPane.getFrameForComponent(FunctionEditor.this);
	      popupEditor = new JDialog(frame, true);
	      popupEditor.setUndecorated(true);
	      popupEditor.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
	      JPanel contentPane = new JPanel(new BorderLayout());
	      popupEditor.setContentPane(contentPane);
	      editorPane = new JPanel(new BorderLayout());
	      editorPane.setBackground(Color.WHITE);
	      editorPane.add(popupField, BorderLayout.CENTER);
	      editorPane.add(revertButton, BorderLayout.EAST);
	      contentPane.add(editorPane, BorderLayout.NORTH);
	      // variablesPane and dragPane are added/removed from contentPane as needed
    	}
    	return popupEditor;
    }

  }

  private class CellRenderer extends DefaultTableCellRenderer {
    Font font = new JTextField().getFont();

    /**
     * Constructor CellRenderer
     */
    public CellRenderer() {
      super();
      setOpaque(true); // make background visible
      setFont(font);
      setHorizontalAlignment(SwingConstants.LEFT);
      setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
    }

    // Returns a label for the specified cell.
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
      String val = value.toString();
      if (col==0 && (FunctionEditor.this instanceof UserFunctionEditor)) {
	      val = FitBuilder.localize(val);
      }
    	setText(val);
    	
      Object obj = objects.get(row);
      String tooltip = getTooltip(obj);
      String tooltipText = (col==0 && tooltip!=null)? tooltip: 
      	(col==0)? ToolsRes.getString("FunctionEditor.Table.Cell.Name.Tooltip"): //$NON-NLS-1$
        ToolsRes.getString("FunctionEditor.Table.Cell.Value.Tooltip"); //$NON-NLS-1$
      if (tooltip==null && col==0) {
      	tooltipText += " ("+ToolsRes.getString("FunctionEditor.Tooltip.HowToEdit")+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
      setToolTipText(tooltipText);

      if((col==1)&&circularErrors.contains(obj)) {
        setToolTipText(ToolsRes.getString("FunctionEditor.Table.Cell.CircularErrors.Tooltip")); //$NON-NLS-1$
        setForeground(DARK_RED);
        if(isSelected) {
          setBackground(MEDIUM_RED);
        } else {
          setBackground(LIGHT_RED);
        }
      } 
      else if((col==1)&&isInvalidExpression(obj)) {                                           // error condition
        setToolTipText(ToolsRes.getString("FunctionEditor.Table.Cell.Invalid.Tooltip"));        //$NON-NLS-1$
        setForeground(DARK_RED);
        if(isSelected) {
          setBackground(MEDIUM_RED);
        } else {
          setBackground(LIGHT_RED);
        }
      } 
      else if(((col==0)&&!isNameEditable(obj))||((col==1)&&!isExpressionEditable(obj))) {     // uneditable cell
        setForeground(Color.BLACK);
        setBackground(LIGHT_GRAY);
      } 
      else {
        if(isSelected) {                                                                        // selected editable cell
          setForeground(hasFocus ? Color.BLUE : Color.BLACK);
          setBackground(LIGHT_BLUE);
        } else {                                                                                // unselected editable cell
          setForeground(Color.BLACK);
          setBackground(Color.WHITE);
        }
      }
      setFont(((col==0)&&isImportant(obj)) ? font.deriveFont(Font.BOLD) : font);
      refreshButtons();
      return this;
    }

  }
  
  /**
   * A MouseAdapter to change numerical values in a CellEditor by dragging a mouse.
   */
  private class ValueMouseControl extends MouseAdapter {
  	
  	CellEditor cellEditor;
  	double prevValue, newValue;
  	Point startingPoint;
  	int logDelta; // log (base 10) of step size delta
  	
    /**
     * Constructor.
     * @param editor the CellEditor
     */
  	private ValueMouseControl(CellEditor editor) {
  		cellEditor = editor;
  	}
  	
    /**
     * Determines logDelta from a String value and relative level.
     * Relative level is a number from 0 to 1 (eg relative mouse position within a component).
     * Strings may be in decimal (eg 0.00) or scientific (eg 0.000E0) format.
     * 
     * @param val the value string
     * @param relativeX number from 0-1 (higher relativeX ==> farther right in string digits)
     * @return the log base 10 of the step size delta
     */
  	int getLogDelta(String val, double relativeX) {
  		if ("".equals(val)) return 0; //$NON-NLS-1$
  		int digits = val.length();
  		int powerOfTen = 0;
  		// handle decimal point
  		int decimal = val.replaceAll(",",".").indexOf("."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		if (decimal>-1) {
  			digits--;
  		}
  		// handle minus sign
  		int minus = val.indexOf("-"); //$NON-NLS-1$
  		if (minus>-1) {
  			digits--;
  			decimal--;
  		}
  		// handle sci format
  		int exp = val.indexOf("E"); //$NON-NLS-1$
  		if (exp>-1) {
  			String exponent = val.substring(exp+1);
  			digits -= exponent.length()+1;
  			powerOfTen = Integer.parseInt(exponent);
  		}
  		// determine power of ten
  		int selectableDigits = exp>-1? digits: digits+1;
  		int selectedDigit = (int)Math.floor(relativeX*selectableDigits);
  		int integerDigits = decimal>-1? decimal: digits;
  		powerOfTen += integerDigits-selectedDigit-1;
  		return powerOfTen;
  	}
  	
    /**
     * Determines the text selection start index for a String.
     * Strings may be in decimal (eg 0.00) or scientific (eg 0.000E0) format.
     * 
     * @param val the value string
     * @return the text selection start index
     */
  	int getSelectionIndex(String val) {
  		int powerOfTen = logDelta;
  		int digits = val.length();
  		int offset = 0;
  		// handle decimal point
  		int decimal = val.replaceAll(",",".").indexOf("."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  		if (decimal>-1) {
  			digits--;
  		}
  		// handle minus sign
  		int minus = val.indexOf("-"); //$NON-NLS-1$
  		if (minus>-1) {
  			digits--;
  			offset = 1;
  			decimal--;
  		}
  		// handle sci format
  		int exp = val.indexOf("E"); //$NON-NLS-1$
  		if (exp>-1) {
  			String exponent = val.substring(exp+1);
  			digits -= exponent.length()+1;
  			powerOfTen -= Integer.parseInt(exponent);
  		}
  		int integerDigits = decimal>-1? decimal: digits;
  		int selectedDigit = integerDigits-powerOfTen-1;
  		offset += decimal>-1 && selectedDigit>=decimal? 1: 0;
  		int selectionIndex = selectedDigit+offset;
  		return selectionIndex;
  	}
  	
  	@Override
  	public void mousePressed(MouseEvent e) {
  		int row = table.rowToSelect;
  		String name = (String)table.getValueAt(row, 0);
  		// return if this control is not suitable for a variable (eg time)
  		if (name.equals("t")) { //$NON-NLS-1$
  			startingPoint = null;
  			return;
  		}
  		startingPoint = e.getPoint();
  		newValue = prevValue;
  		double level = 1.0*startingPoint.x/cellEditor.dragPane.getWidth();
  		logDelta = getLogDelta(cellEditor.popupField.getText(), level);
  	}
  	
  	@Override
  	public void mouseReleased(MouseEvent e) {
  		if (startingPoint==null) return;
  		prevValue = newValue;
  		startingPoint = null;
  		// deselect text
  		String val = cellEditor.popupField.getText();
  		cellEditor.popupField.select(val.length(), val.length());   
  	}
  	
  	@Override
  	public void mouseDragged(MouseEvent e) {
  		if (startingPoint==null || Double.isNaN(prevValue)) return;
  		// use shift key accelerator
  		int pixelsPerStep = e.isShiftDown()? 1: 10;
			int d = (e.getPoint().x-startingPoint.x)/pixelsPerStep;
			// change value in steps of delta
			double delta = Math.pow(10, logDelta);
			newValue = prevValue+d*delta;
			String s = format(newValue, 0);
  		int row = table.rowToSelect;
  		table.setValueAt(s, row, 1);
  		cellEditor.popupField.setText(s);
  		cellEditor.popupField.setBackground(Color.yellow);
      cellEditor.popupField.requestFocusInWindow();
  		String val = cellEditor.popupField.getText();
  		int index = getSelectionIndex(val);
  		cellEditor.popupField.select(index, index+1);     
  	}
  	
  	@Override
  	public void mouseMoved(MouseEvent e) {
  		double level = 1.0*e.getPoint().x/cellEditor.dragPane.getWidth();
  		String val = cellEditor.popupField.getText();
  		logDelta = getLogDelta(val, level);
  		int index = getSelectionIndex(val);
  		cellEditor.popupField.select(index, index+1);
  	}
  	
  	@Override
  	public void mouseEntered(MouseEvent e) {
  		cellEditor.dragPane.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)); 
  	}
  	
  	@Override
  	public void mouseExited(MouseEvent e) {
  		cellEditor.dragPane.setCursor(Cursor.getDefaultCursor()); 
  	}
  }

  /**
   * A class to undo/redo edits.
   */
  protected class DefaultEdit extends AbstractUndoableEdit {
    Object redoObj, undoObj;
    int redoRow, redoCol, undoRow, undoCol, editType;
    String name;

    /**
     * A class to undo/redo edits.
     *
     * @param type may be ADD_EDIT, REMOVE_EDIT, NAME_EDIT, or EXPRESSION_EDIT 
     * @param newVal the new object, name or expression
     * @param newRow the row selected
     * @param newCol the col selected
     * @param prevVal the previous object, name or expression
     * @param prevRow the previous row selected
     * @param prevCol the previous col selected
     * @param name the name of the edited object
     */
    public DefaultEdit(int type, Object newVal, int newRow, int newCol, Object prevVal, int prevRow, int prevCol, String name) {
      editType = type;
      redoObj = newVal;
      undoObj = prevVal;
      redoRow = newRow;
      redoCol = newCol;
      undoRow = prevRow;
      undoCol = prevCol;
      this.name = name;
      OSPLog.finer(editTypes[type]+": \""+name+"\"" //$NON-NLS-1$ //$NON-NLS-2$
                   +"\nold value: "+prevVal         //$NON-NLS-1$
                   +"\nnew value: "+newVal);        //$NON-NLS-1$
    }

    // undoes the change
    public void undo() throws CannotUndoException {
      super.undo();
      undoEditsEnabled = false;
      switch(editType) {
         case ADD_EDIT : {
           removeObject(undoObj, false);
           break;
         }
         case REMOVE_EDIT : {
           addObject(undoObj, undoRow, false, true);
           break;
         }
         case NAME_EDIT : {
           Object obj = objects.get(undoRow);
           String expression = getExpression(obj);
           name = undoObj.toString();
           String prevName = redoObj.toString();
           obj = createObject(name, expression, obj);
           objects.remove(undoRow);
           objects.add(undoRow, obj);
           evaluateAll();
           firePropertyChange("edit", name, prevName); //$NON-NLS-1$
           break;
         }
         case EXPRESSION_EDIT : {
           Object obj = objects.get(undoRow);
           Object[] undoArray = (Object[]) undoObj;    // array is {expression, buttons}
           obj = createObject(name, undoArray[0].toString(), obj);
           objects.remove(undoRow);
           objects.add(undoRow, obj);
           evaluateAll();
           firePropertyChange("edit", name, null);     //$NON-NLS-1$
         }
      }
      // select cell and request keyboard focus
      table.rowToSelect = undoRow;
      table.columnToSelect = undoCol;
      getTable().selectOnFocus = true;
      getTable().requestFocusInWindow();
      refreshGUI();
      undoEditsEnabled = true;
    }

    // redoes the change
    public void redo() throws CannotUndoException {
      super.redo();
      undoEditsEnabled = false;
      switch(editType) {
         case ADD_EDIT : {
           addObject(redoObj, redoRow, false, true);
           break;
         }
         case REMOVE_EDIT : {
           removeObject(redoObj, false);
           break;
         }
         case NAME_EDIT : {
           Object obj = objects.get(redoRow);
           String expression = getExpression(obj);
           name = redoObj.toString();
           String prevName = undoObj.toString();
           obj = createObject(name, expression, obj);
           objects.remove(redoRow);
           objects.add(redoRow, obj);
           evaluateAll();
           firePropertyChange("edit", name, prevName); //$NON-NLS-1$
           break;
         }
         case EXPRESSION_EDIT : {
           Object obj = objects.get(redoRow);
           Object[] redoArray = (Object[]) redoObj;    // array is {expression, buttons}
           ArrayList<?> buttons = (ArrayList<?>) redoArray[1];
           for(Object next : buttons) {
             AbstractButton b = (AbstractButton) next;
             if(!b.isSelected()) {
               b.doClick(0);
             }
           }
           obj = createObject(name, redoArray[0].toString(), obj);
           objects.remove(redoRow);
           objects.add(redoRow, obj);
           evaluateAll();
           firePropertyChange("edit", name, null);     //$NON-NLS-1$
         }
      }
      // select cell and request keyboard focus
      table.rowToSelect = redoRow;
      table.columnToSelect = redoCol;
      getTable().selectOnFocus = true;
      getTable().requestFocusInWindow();
      refreshGUI();
      undoEditsEnabled = true;
    }

    // returns the presentation name
    public String getPresentationName() {
      if(editType==REMOVE_EDIT) {
        return "Deletion"; //$NON-NLS-1$
      }
      return "Edit"; //$NON-NLS-1$
    }

  }

  /**
   * Formats a number.
   *
   * @param value the number
   * @param zeroLevel the level below which the value is considered zero
   * @return the formatted string
   */
  public static String format(double value, double zeroLevel) {
    if(Math.abs(value)<zeroLevel) {
      value = 0;
    }
    int rounded = (int) Math.round(value);
    if(Math.abs(value-rounded)<zeroLevel) {
      value = rounded;
    }
    double absVal = Math.abs(value);
    boolean scientific = ((absVal<0.01)&&(value!=0))||(absVal>=1000);
    String s = scientific ? sciFormat.format(value) : decimalFormat.format(value);
    // eliminate trailing "0000x" and "9999x"
    int n = s.indexOf("E");                     // exponential symbol //$NON-NLS-1$
    String tail = (n>-1) ? s.substring(n) : ""; //$NON-NLS-1$
    s = (n>-1) ? s.substring(0, n) : s;
    n = s.indexOf("0000"); //$NON-NLS-1$
    if(n>1) {
      s = s.substring(0, n+1); // leave one trailing zero
    }
    n = s.indexOf("9999"); //$NON-NLS-1$
    if(n>1) {
      s = s.substring(0, n);
      DecimalFormatSymbols symbols = sciFormat.getDecimalFormatSymbols();
      char separator = symbols.getDecimalSeparator();
      int m = s.indexOf(separator);
      if(m==s.length()-1) {
        int i = Integer.parseInt(s.substring(0, m))+1;
        s = Integer.toString(i)+separator+"0"; //$NON-NLS-1$
      } else {
        int i = Integer.parseInt(s.substring(n-1))+1;
        s = s.substring(0, n-1)+i;
      }
    }
    return s+tail;
  }
  
  /**
   * Rounds a number.
   *
   * @param value the number
   * @param sigfigs the number of significant figures in the rounded value
   * @return the rounded value
   */
  public static double round(double value, int sigfigs) {
  	if (value==0) return value;
  	int multiplier = value<0? -1: +1;
  	value = Math.abs(value);
  	// increase or decrease value by factors of 10 to get sigfigs
  	double limit = Math.pow(10, sigfigs-1);
  	int power = 0;
  	while (value<limit) {
  		value *= 10;
  		power++;
  	}
  	while (value>10*limit) {
  		value /= 10;
  		power--;
  	}
  	value = Math.round(value);
  	return multiplier*value/Math.pow(10, power);
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
