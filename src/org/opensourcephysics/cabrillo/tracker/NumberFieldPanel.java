package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.tools.FontSizer;

public class NumberFieldPanel extends JPanel {
	
	private static HashMap<TrackerPanel, NumberFieldPanel[]> npPanelArrays;
	private static Border emptyBorder;
	static {
		npPanelArrays = new HashMap<TrackerPanel, NumberFieldPanel[]>();
		emptyBorder = BorderFactory.createEmptyBorder(0, 4, 0, 0);
	}
	
	public static NumberFieldPanel[] getNFPanelArray(TrackerPanel tp) {
		NumberFieldPanel[] array = npPanelArrays.get(tp);
		if (array == null) {
			array = new NumberFieldPanel[] 
				{new NumberFieldPanel(tp), new NumberFieldPanel(tp)};
			npPanelArrays.put(tp, array);
		}
		return array;
	}
	
	private ArrayList<Object[]> rowObjects = new ArrayList<Object[]>();
	private int trackerPanelID; // trackerpanel
	private TFrame frame;
	private NFTableModel model;
	private NFTable table;
	private Component positionComp;
	private JLabel sizingLabel = new JLabel();
	
	private NumberFieldPanel(TrackerPanel tp) {
		super(new BorderLayout());
		trackerPanelID = tp.getID();
		frame = tp.getTFrame();
		createGUI();

		frame.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
	      if (isVisible()) refreshFloatingBounds();
	    }
		});
		super.setVisible(false);
	}
	
	public void createGUI() {
		model = new NFTableModel();
		table = new NFTable(model);
		table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(table, BorderLayout.CENTER);
		
		table.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());
        if (row >= 0 && col == 1) {
            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
            	Object obj = rowObjects.get(row)[1];
            	NumberField field = obj instanceof NumberField? (NumberField)obj: null;
              boolean startedEditing = table.editCellAt(row, col, e);
              
              if (startedEditing && field != null) {
                // translate the table coords to field coords
                Point editorPoint = SwingUtilities.convertPoint(table, e.getPoint(), field);                 
                // create a cloned MouseEvent
                MouseEvent newEvent = new MouseEvent(
                		field,
                    e.getID(),
                    e.getWhen(),
                    e.getModifiersEx(),
                    editorPoint.x,
                    editorPoint.y,
                    e.getClickCount(),
                    true,
                    e.getButton()
                );
                field.dispatchEvent(newEvent);
              }
            }
        	}
  			}
		});
	}

	public void addRow(JComponent label, Component comp) {
		if (label instanceof JLabel) {
			String text = ((JLabel)label).getText();
			label = new TextLineLabel(text);
		}
		if (label instanceof TextLineLabel) {
			rowObjects.add(new Object[] {label, comp});
			model.fireTableRowsInserted(rowObjects.size()-1, rowObjects.size()-1);
		}
	}
	
	private TrackerPanel panel() {
		return frame.getTrackerPanelForID(trackerPanelID);
	}
	
	public void clear() {
		rowObjects.clear();
		setPositionComponent(null);
		model.fireTableStructureChanged();
	}
	
	public void setUp(boolean show) {
		if (show && getParent() == null) {			
			frame.getLayeredPane().add(this, JLayeredPane.PALETTE_LAYER);
		} else if (!show) {
			frame.getLayeredPane().remove(this);
		}		
	}
	
	public void setPositionComponent(Component c) {
		positionComp = c;
		refreshFloatingBounds();
	}
	
	public void refreshFloatingBounds() {
    if (positionComp == null || !positionComp.isShowing() || getParent() == null) {
       return;
    }
    
    // the parent is the JLayeredPane
    Component layeredPane = getParent(); 
    
    // find bottom left corner of positionComp relative to the layeredPane
    Point p = new Point(0, positionComp.getHeight());
    Point pt = SwingUtilities.convertPoint(positionComp, p, layeredPane);
    Dimension dim = getPreferredSize();
    
    // make sure second panel stays to the right of first
    NumberFieldPanel nfp = getNFPanelArray(panel())[0];
    if (this != nfp) {
    	Rectangle rect = nfp.getBounds();
    	pt.x = Math.max(pt.x, rect.x + rect.width + 2);
    }

    // set the location boundaries
    setBounds(pt.x, pt.y + 6, dim.width, dim.height);
    
    // request a validation and repaint
//    layeredPane.revalidate();
//    layeredPane.repaint();
	}
	
	public Component getPositionComponent() {
		return positionComp;
	}
	
	public int getRowCount() {
		return rowObjects.size();
	}
	
  public void updateTableLayout() {
  	if (table==null) return;
    Font font = table.getFont();
    
    // adjust row height based on font metrics
    FontMetrics fm = table.getFontMetrics(font);
    int newRowHeight = fm.getHeight() + 8; // add padding
    table.setRowHeight(newRowHeight);
    
    // adjust column widths based on preferred sizes
    TableColumnModel columnModel = table.getColumnModel();
	  for (int col = 0; col < 2; col++) {
	    TableColumn tableColumn = columnModel.getColumn(col);         
	    // find the widest string in a column
	    int maxWidth = 0;
	    for (int row = 0; row < rowObjects.size(); row++) {
	       Object cellValue = rowObjects.get(row)[col];
	       int cellWidth = 0;
	       if (cellValue instanceof TextLineLabel) {
	       	 TextLineLabel label = (TextLineLabel)cellValue;
	       	 String text = label.getText();
	      	 if (text.contains("_{")) { //$NON-NLS-1$
	     			 text = TeXParser.removeSubscripting(text);
	     		 }
	         sizingLabel.setFont(label.getFont());
	      	 sizingLabel.setText(text);
           cellWidth = sizingLabel.getPreferredSize().width;            	 
	       }
	       else if (cellValue instanceof NumberField) {
	         sizingLabel.setFont(font);
	      	 NumberField field = (NumberField)cellValue;
	      	 sizingLabel.setText(field.getText());
           cellWidth = sizingLabel.getPreferredSize().width;            	 
	       }
	       else if (cellValue instanceof JCheckBox) {
	         sizingLabel.setFont(font);
	         JCheckBox box = (JCheckBox)cellValue;
           cellWidth = box.getPreferredSize().width;            	 
	       }
	       else if (cellValue instanceof JComboBox) {
	         sizingLabel.setFont(font);
					 JComboBox<?> box = (JComboBox<?>)cellValue;
					 cellWidth = box.getPreferredSize().width;            	 
	       }
	       maxWidth = Math.max(maxWidth, cellWidth);
	    }
	    maxWidth = Math.max(maxWidth, 20); // minimum size
	    // set the new preferred width with padding
		  tableColumn.setPreferredWidth(maxWidth + 8); 
    }
    table.revalidate();
    table.repaint();
	}
  
  @Override
	public void setFont(Font font) {
  	super.setFont(font);
  	if (table != null) {
	  	table.setFont(font.deriveFont(Font.BOLD));
	  	updateTableLayout();
  	}
  }
 

	   
	class NFTableCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

		@Override
	  public Component getTableCellRendererComponent(JTable table, Object value, 
	          boolean isSelected, boolean hasFocus, int row, int column) {
	  	if (column == 1) {
	  		return getTableCellEditorComponent(table, value, 
	          isSelected, row, column);
	  	}
	  	JComponent label = (JComponent)rowObjects.get(row)[0];
			FontSizer.setFonts(label, FontSizer.getLevel());
			return label;
	  }

	  @Override
	  public Component getTableCellEditorComponent(JTable table, Object value, 
	          boolean isSelected, int row, int column) {
	  	if (column == 1) {
	  		JComponent comp = (JComponent)rowObjects.get(row)[1];
	  		if (comp instanceof NumberField) {
					((NumberField)comp).setBorder(emptyBorder);
	  		}
	  		else if (comp instanceof JCheckBox) {
	  			((JCheckBox)comp).setText(null);
	  		}
	  		else {
	  			for (int i = 0; i < comp.getComponentCount(); i++) {
	  				Component c = comp.getComponent(i);
	  				if (c instanceof JCheckBox) {
			  			((JCheckBox)c).setText(null);	  					
	  				}
	  			}
	  		}
				FontSizer.setFonts(comp, FontSizer.getLevel());
  			model.fireTableRowsInserted(model.getRowCount()==0? -1: 0, model.getRowCount() - 1);
  			return comp;
	  	}
	  	return null;
	  }

	  @Override
	  public Object getCellEditorValue() {
//	  	if (editingField != null) {
//	  		double val = editingField.getValue();
//				editingField = null;
//	      return val;
//	  	}
//	  	if (editingCheckbox != null) {
//	  		boolean checked = editingCheckbox.isSelected();
//				editingCheckbox = null;
//	      return checked;
//	  	}
	  	return null;
	  }
	}

  class NFTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Name", "Value"};

    @Override
    public int getRowCount() { return rowObjects.size(); }

    @Override
    public int getColumnCount() { return 2; }

    @Override
    public String getColumnName(int col) { 
    	return columnNames[col]; 
    }

//    @Override
//    public Class<?> getColumnClass(int col) { 
//    	return col==1? double.class: String.class; 
//    }

    @Override
    public Object getValueAt(int row, int col) {
      return (col == 0) ? getName(row) : 
      	rowObjects.get(row)[1] instanceof NumberField? getValue(row) :
      		rowObjects.get(row)[1] instanceof JCheckBox? isChecked(row): null;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 1; // only the value column is editable
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 1) {
//        	if (rowObjects.get(row)[1] instanceof NumberField) {
//	        	NumberField field = (NumberField)rowObjects.get(row)[1];
//	        	field.setValue((double)value);
//        	}
//        	else if (rowObjects.get(row)[1] instanceof JCheckBox) {
//        		JCheckBox checkbox = (JCheckBox)rowObjects.get(row)[1];
//        		checkbox.setSelected((boolean) value);
//        	}
          fireTableCellUpdated(row, col);
        }
    }
    
    private Object getName(int row) {
    	JComponent label = (JComponent)rowObjects.get(row)[0];
    	return label;
    }
    
    private double getValue(int row) {
    	NumberField field = (NumberField)rowObjects.get(row)[1];
	    return field.getValue();
    }

    private boolean isChecked(int row) {
    	JCheckBox field = (JCheckBox)rowObjects.get(row)[1];
	    return field.isSelected();
    }

  }
  
  class NFTable extends JTable {
  	
	   private final NFTableCell nfCell = new NFTableCell();
	   
	   public NFTable(NFTableModel model) {
	      super(model);
	   }
	
	   @Override
	   public TableCellEditor getCellEditor(int row, int column) {
	       return nfCell;
	   }
	   
	   @Override
		 public TableCellRenderer getCellRenderer(int row, int column) {
		     return nfCell;   	
		 }
   
	}
	
}
