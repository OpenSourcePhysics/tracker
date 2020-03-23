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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.OSPRuntime;

/**
 * This is a JPanel for managing Functions and supporting Parameters.
 *
 * @author Douglas Brown
 */
public class FunctionPanel extends JPanel implements PropertyChangeListener {
  // instance fields
  protected FunctionTool functionTool;
  protected ParamEditor paramEditor;
  protected FunctionEditor functionEditor;
  protected Container box;
  protected JTextPane instructions;
  private JButton undoButton;
  private JButton redoButton;
  private UndoableEditSupport undoSupport;
  protected UndoManager undoManager;
  private int varBegin, varEnd;
  protected JTextField tableEditorField;
  protected String prevName, description;
  private Icon icon;

  /**
   * Constructor FunctionPanel
   * @param editor
   */
  public FunctionPanel(FunctionEditor editor) {
    super(new BorderLayout());
    functionEditor = editor;
    editor.functionPanel = this;
    createGUI();
    refreshGUI();
  }

  /**
   * Gets the ParamEditor.
   *
   * @return the param editor
   */
  public ParamEditor getParamEditor() {
    return paramEditor;
  }

  /**
   * Gets the function editor.
   *
   * @return the function editor
   */
  public FunctionEditor getFunctionEditor() {
    return functionEditor;
  }

  /**
   * Gets the function table.
   *
   * @return the table
   */
  public FunctionEditor.Table getFunctionTable() {
    return functionEditor.getTable();
  }

  /**
   * Gets the parameter table.
   *
   * @return the table
   */
  public FunctionEditor.Table getParamTable() {
    return paramEditor.getTable();
  }

  /**
   * Gets an appropriate label for the FunctionTool dropdown.
   *
   * @return a label string
   */
  public String getLabel() {
    return ToolsRes.getString("FunctionPanel.Label"); //$NON-NLS-1$
  }

  /**
   * Gets the display name for the FunctionTool dropdown. By default, this
   * returns the name of this panel.
   *
   * @return the display name
   */
  public String getDisplayName() {
    return getName();
  }

  /**
   * Override getPreferredSize().
   *
   * @return the preferred size
   */
  public Dimension getPreferredSize() {
    Dimension dim = super.getPreferredSize();
    dim.width = paramEditor.buttonPanel.getPreferredSize().width;
    return dim;
  }

  /**
   * Adds names to the forbidden set.
   * 
   * @param names the names
   */
  protected void addForbiddenNames(String[] names) {
    for(int i = 0; i<names.length; i++) {
      functionEditor.forbiddenNames.add(names[i]);
      if(paramEditor!=null) {
        paramEditor.forbiddenNames.add(names[i]);
      }
    }
  }

  /**
   * Listens for property changes "edit" and "function"
   *
   * @param e the event
   */
  public void propertyChange(PropertyChangeEvent e) {
    if(e.getPropertyName().equals("edit")) {                                 //$NON-NLS-1$
      // Parameter or Function has been edited
      if((e.getNewValue() instanceof UndoableEdit)) {
        // post undo edit
        undoSupport.postEdit((UndoableEdit) e.getNewValue());
      }
      refreshFunctions();
      refreshGUI();
      if (functionTool!=null && functionEditor.getObjects().size()>0) {
        String functionName = (String) e.getOldValue();
        String prevName = null;
        if(e.getNewValue() instanceof FunctionEditor.DefaultEdit) {
          FunctionEditor.DefaultEdit edit = (FunctionEditor.DefaultEdit) e.getNewValue();
          if(edit.editType==FunctionEditor.NAME_EDIT) {
            prevName = edit.undoObj.toString();
          }
        } else if (e.getNewValue() instanceof String) {
          prevName = e.getNewValue().toString();
        }
        functionTool.firePropertyChange("function", prevName, functionName); //$NON-NLS-1$
      }
    } else if(e.getPropertyName().equals("function")) {                      //$NON-NLS-1$
      // function has been added or removed       
      refreshFunctions();
      refreshGUI();
      if(functionTool!=null) {
      	functionTool.refreshGUI();
        functionTool.firePropertyChange("function", null, null);             //$NON-NLS-1$
      }
    } else if (e.getPropertyName().equals("description") && functionTool!=null) {   //$NON-NLS-1$
      functionTool.firePropertyChange("description", null, null);    //$NON-NLS-1$
    }
  }

  /**
   * Clears the selection.
   */
  protected void clearSelection() {
    getFunctionTable().clearSelection();
    getParamTable().clearSelection();
    refreshInstructions(null, false, -1);
  }

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
    // create textpane and color styles for instructions
    instructions = new JTextPane() {
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
    instructions.setEditable(false);
    instructions.setOpaque(false);
    instructions.setFocusable(false);
    instructions.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    StyledDocument doc = instructions.getStyledDocument();
    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setFontFamily(def, "SansSerif"); //$NON-NLS-1$
    Style blue = doc.addStyle("blue", def); //$NON-NLS-1$
    StyleConstants.setBold(blue, false);
    StyleConstants.setForeground(blue, Color.blue);
    Style red = doc.addStyle("red", blue); //$NON-NLS-1$
    StyleConstants.setBold(red, true);
    StyleConstants.setForeground(red, Color.red);
    instructions.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(varEnd==0) {
          return;
        }
        instructions.setCaretPosition(varBegin);
        instructions.moveCaretPosition(varEnd);
        tableEditorField.replaceSelection(instructions.getSelectedText());
        tableEditorField.setBackground(Color.yellow);
      }
      public void mouseExited(MouseEvent e) {
        if(!hasCircularErrors()&&!hasInvalidExpressions()) {
          StyledDocument doc = instructions.getStyledDocument();
          Style blue = doc.getStyle("blue"); //$NON-NLS-1$
          doc.setCharacterAttributes(0, instructions.getText().length(), blue, false);
          varBegin = varEnd = 0;
        }
      }

    });
    instructions.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        varBegin = varEnd = 0;
        // select and highlight the variable under mouse
        String text = instructions.getText();
        // first separate the instructions from the variables
        int startVars = text.indexOf(": ");     //$NON-NLS-1$
        if(startVars==-1) {
          return;
        }
        startVars += 2;
        String vars = text.substring(startVars);
        StyledDocument doc = instructions.getStyledDocument();
        Style blue = doc.getStyle("blue"); //$NON-NLS-1$
        Style red = doc.getStyle("red");   //$NON-NLS-1$
        int beginVar = instructions.viewToModel(e.getPoint())-startVars;
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
    // create box and function editors
    box = Box.createVerticalBox();
    add(box, BorderLayout.CENTER);
    if (functionEditor instanceof DataFunctionEditor) {
    	DataFunctionEditor dfEditor = (DataFunctionEditor)functionEditor;
      paramEditor = new ParamEditor(dfEditor.getData());
    }
    else paramEditor = new ParamEditor();
    paramEditor.functionPanel = this;
    functionEditor.setParamEditor(paramEditor);
    paramEditor.setFunctionEditors(new FunctionEditor[] {functionEditor});
    box.add(paramEditor);
    box.add(functionEditor);
    paramEditor.addPropertyChangeListener(this);
    paramEditor.addPropertyChangeListener(functionEditor);
    functionEditor.addPropertyChangeListener(this);
    functionEditor.addPropertyChangeListener(paramEditor);
    JScrollPane scroller = new JScrollPane(instructions) {
      public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        Font font = instructions.getFont();
        dim.height = Math.max(dim.height, font.getSize()*4);
        return dim;
      }

    };
    box.add(scroller);
    // set up the undo system
    undoManager = new UndoManager();
    undoSupport = new UndoableEditSupport();
    undoSupport.addUndoableEditListener(undoManager);
    undoButton = new JButton();
    undoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undoManager.undo();
      }

    });
    redoButton = new JButton();
    redoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undoManager.redo();
      }

    });
  }

  /**
   * Refreshes the GUI.
   */
  protected void refreshGUI() {
    undoButton.setText(ToolsRes.getString("DataFunctionPanel.Button.Undo"));                //$NON-NLS-1$
    undoButton.setToolTipText(ToolsRes.getString("DataFunctionPanel.Button.Undo.Tooltip")); //$NON-NLS-1$
    redoButton.setText(ToolsRes.getString("DataFunctionPanel.Button.Redo"));                //$NON-NLS-1$
    redoButton.setToolTipText(ToolsRes.getString("DataFunctionPanel.Button.Redo.Tooltip")); //$NON-NLS-1$   
    undoButton.setEnabled(undoManager.canUndo());
    redoButton.setEnabled(undoManager.canRedo());
    if(functionTool!=null && functionTool.getSelectedPanel()==this) {
    	boolean needsButtons = true;
    	for (Component c: functionTool.buttonbar.getComponents()) {
    		if (c == undoButton) needsButtons = false;
    	}
    	if (needsButtons) {
	      functionTool.buttonbar.removeAll();
	      functionTool.buttonbar.add(functionTool.helpButton);
	      functionTool.buttonbar.add(undoButton);
	      functionTool.buttonbar.add(redoButton);
	      functionTool.buttonbar.add(functionTool.fontButton);
	      functionTool.buttonbar.add(functionTool.closeButton);
    	}
      paramEditor.refreshGUI();
      functionEditor.refreshGUI();
    }
//    refreshInstructions(null, false, -1);
  }

  /**
   * Sets the font level.
   *
   * @param level the level
   */
  protected void setFontLevel(int level) {
    FontSizer.setFonts(this, level);
    FontSizer.setFonts(undoButton, level);
    FontSizer.setFonts(redoButton, level);
  }

  /**
   * Gets the description for this panel.
   * 
   * @return the description
   */
  public String getDescription() {
    return description==null? "": description; //$NON-NLS-1$
  }

  /**
   * Sets the description for this panel.
   * 
   * @param desc the description
   */
  public void setDescription(String desc) {
    description = desc;
  }

  /**
   * Gets the Icon for this panel, if any.
   * 
   * @return the icon
   */
  public Icon getIcon() {
    return icon;
  }

  /**
   * Sets the Icon for this panel.
   * 
   * @param icon the icon
   */
  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  /**
   * Refreshes the functions.
   */
  protected void refreshFunctions() {
    functionEditor.evaluateAll();
  }

  /**
   * Sets the FunctionTool. This method is called by the tool to
   * which this panel is added.
   *
   * @param tool the FunctionTool
   */
  public void setFunctionTool(FunctionTool tool) {
    functionTool = tool;
  }

  /**
   * Tabs to the next editor.
   *
   * @param editor the current editor
   */
  protected void tabToNext(FunctionEditor editor) {
    if(editor==functionEditor) {
      functionTool.helpButton.requestFocusInWindow();
    } else {
      functionEditor.newButton.requestFocusInWindow();
    }
  }

  /**
   * Refreshes the instructions based on selected cell.
   *
   * @param source the function editor (may be null)
   * @param editing true if the table is editing
   * @param selectedColumn the selected table column, or -1 if none
   */
  protected void refreshInstructions(FunctionEditor source, boolean editing, int selectedColumn) {
    StyledDocument doc = instructions.getStyledDocument();
    Style style = doc.getStyle("blue");                                                //$NON-NLS-1$
    String s = isEmpty() ? ToolsRes.getString("FunctionPanel.Instructions.GetStarted") //$NON-NLS-1$
                         : ToolsRes.getString("FunctionPanel.Instructions.General")   //$NON-NLS-1$
                           +"  "+ToolsRes.getString("FunctionPanel.Instructions.EditDescription"); //$NON-NLS-1$ //$NON-NLS-2$
    if(!editing&&hasCircularErrors()) {                                       // error condition
      s = ToolsRes.getString("FunctionPanel.Instructions.CircularErrors");    //$NON-NLS-1$
      style = doc.getStyle("red");                                            //$NON-NLS-1$
    } else if(!editing&&hasInvalidExpressions()) {                            // error condition
      s = ToolsRes.getString("FunctionPanel.Instructions.BadCell");           //$NON-NLS-1$
      style = doc.getStyle("red");                                            //$NON-NLS-1$
    } else if(source!=null) {
      if((selectedColumn==0)&&editing) {                                      // editing name
        s = ToolsRes.getString("FunctionPanel.Instructions.NameCell");        //$NON-NLS-1$
      } else if((selectedColumn==1)&&editing) {                               // editing expression
        s = source.getVariablesString(": "); //$NON-NLS-1$
      } else if(selectedColumn>-1) {
        s = ToolsRes.getString("FunctionPanel.Instructions.EditCell");        //$NON-NLS-1$
        if(selectedColumn==0) {
          s += "  "+ToolsRes.getString("FunctionPanel.Instructions.NameCell"); //$NON-NLS-1$//$NON-NLS-2$
          s += "\n"+ToolsRes.getString("FunctionPanel.Instructions.EditDescription"); //$NON-NLS-1$//$NON-NLS-2$
        } else {
          s += " "+ToolsRes.getString("FunctionPanel.Instructions.Help");     //$NON-NLS-1$//$NON-NLS-2$
        }
      }
    }
    instructions.setText(s);
    int len = instructions.getText().length();
    doc.setCharacterAttributes(0, len, style, false);
    revalidate();
  }

  protected boolean isEmpty() {
    return(functionEditor.getObjects().size()==0)&&(paramEditor.getObjects().size()==0);
  }

  protected boolean hasInvalidExpressions() {
    return functionEditor.containsInvalidExpressions()||paramEditor.containsInvalidExpressions();
  }

  protected boolean hasCircularErrors() {
    return !(functionEditor.circularErrors.isEmpty()&&paramEditor.circularErrors.isEmpty());
  }

  /**
   * Disposes of this panel.
   */
  protected void dispose() {
    paramEditor.removePropertyChangeListener(this);
    paramEditor.removePropertyChangeListener(functionEditor);
    functionEditor.removePropertyChangeListener(this);
    functionEditor.removePropertyChangeListener(paramEditor);
    functionEditor.setParamEditor(null);
    paramEditor.setFunctionEditors(new FunctionEditor[0]);
    functionEditor.setFunctionPanel(null);
    functionEditor = null;
    paramEditor.setFunctionPanel(null);
    paramEditor = null;
    setFunctionTool(null);
  }

  @Override
  public void finalize() {
  	OSPLog.finer(getClass().getSimpleName()+" resources released by garbage collector"); //$NON-NLS-1$
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
