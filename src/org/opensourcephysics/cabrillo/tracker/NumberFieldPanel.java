package org.opensourcephysics.cabrillo.tracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.NumberField;

public class NumberFieldPanel extends JPanel {
	
	static boolean useTable;
	
	private static HashMap<TrackerPanel, NumberFieldPanel[]> npPanelArrays;
	static {
		npPanelArrays = new HashMap<TrackerPanel, NumberFieldPanel[]>();
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
	private int trackerPanelID;
	private TFrame frame;
	private NFTableModel model;
	private JTable table;
	private Component positionComp;
	private JLabel sizingLabel = new JLabel();
	private MouseListener myMouseListener;
	private ActionListener myActionListener;
	
	private NumberFieldPanel(TrackerPanel tp) {
		super(useTable? new BorderLayout(): new GridBagLayout());
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
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		table.setSelectionBackground(table.getBackground());
		table.setSelectionForeground(table.getForeground());
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		if (useTable) {
			add(table, BorderLayout.CENTER);			
			table.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
	        int row = table.rowAtPoint(e.getPoint());
	        int col = table.columnAtPoint(e.getPoint());
					if (OSPRuntime.isPopupTrigger(e) && col == 1) {
	        	if (rowObjects.get(row)[col] instanceof NumberField) {
		        	NumberField field = (NumberField)rowObjects.get(row)[1];
		        	if (field instanceof TTrack.hasTrack) {
		        		TTrack.hasTrack tnf = (TTrack.hasTrack)field;
		        		TTrack track = tnf.getTrack();        		
		        		Rectangle cellRect = table.getCellRect(row, col, true);
		        		track.showFormatPopup(field, table, cellRect);
		        	}
	        	}
					}				
	  		}
			});
		}
		else {
			setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
			setBackground(new Color(245, 245, 245));
			myMouseListener = new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
					if (OSPRuntime.isPopupTrigger(e)) {
	        	if (e.getSource() instanceof NumberField) {
		        	NumberField field = (NumberField)e.getSource();
		        	if (field instanceof TTrack.hasTrack) {
		        		TTrack.hasTrack tnf = (TTrack.hasTrack)field;
		        		TTrack track = tnf.getTrack();        		
		        		track.showFormatPopup(field, null, null);
		        	}
	        	}
					}
	  		}
			};
			myActionListener = e -> {
				updateTableLayout();
			};		
		}
	}

	public void addRow(JComponent label, Component comp) {
		if (label instanceof JLabel) {
			String text = ((JLabel)label).getText();
			label = new TextLineLabel(text);
		}
		if (label instanceof TextLineLabel) {
//			label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
//			label.setBackground(new Color(2, 27, 27));

			if (comp instanceof JCheckBox) {
				((JCheckBox) comp).setText(null);
			}
			if (comp instanceof JComboBox) {
				((JComboBox<?>) comp).setBorder(BorderFactory.createEmptyBorder());
			}
      comp.addMouseListener(myMouseListener);
      if (comp instanceof NumberField) {
        ((NumberField)comp).addActionListener(myActionListener);      	
      }
			rowObjects.add(new Object[] {label, comp});
			model.fireTableRowsInserted(rowObjects.size()-1, rowObjects.size()-1);
		}
	}
	
	private TrackerPanel panel() {
		return frame.getTrackerPanelForID(trackerPanelID);
	}
	
	protected void clear() {
    for (int row = 0; row < rowObjects.size(); row++) {
      Component comp = (Component)rowObjects.get(row)[1];
      comp.removeMouseListener(myMouseListener);
      if (comp instanceof NumberField) {
        ((NumberField)comp).removeActionListener(myActionListener);      	
      }
    }
		rowObjects.clear();
		setPositionComponent(null);
		model.fireTableStructureChanged();
	}
	
	protected void setUp(boolean show) {
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
    GridBagConstraints gbc = new GridBagConstraints();
    if (!useTable)
    	removeAll();
    
    // adjust row height based on font metrics
    FontMetrics fm = table.getFontMetrics(font);
    int newRowHeight = fm.getHeight() + 8; // add padding
    table.setRowHeight(newRowHeight);
	  int totalHeight = rowObjects.size() * newRowHeight;

    // adjust column widths based on preferred sizes
    TableColumnModel columnModel = table.getColumnModel();
    int totalWidth = 0;
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
		  
		  if (!useTable) {
		  
	      gbc.fill = GridBagConstraints.BOTH;
	      gbc.ipady = 8;  // Vertical cell padding
	      gbc.ipadx = 12; // Horizontal cell padding
	      // fill gridbag
	      for (int row = 0; row < rowObjects.size(); row++) {
	        Object cellValue = rowObjects.get(row)[col];
          gbc.gridy = row;
          gbc.gridx = col;
          gbc.weightx = col == 0? 1: 0; // column 0 stays fixed
          gbc.weighty = 1.0 / rowObjects.size(); // equal heights
          gbc.fill = GridBagConstraints.BOTH;
          Component comp = (Component)cellValue;
          add(comp, gbc);
        }
      }
		  totalWidth += tableColumn.getPreferredWidth();
    }
    setSize(new Dimension(totalWidth, totalHeight));
	  revalidate();
	  repaint();
//    table.revalidate();
//    table.repaint();
	}
  
  @Override
	public void setFont(Font font) {
  	super.setFont(font);
  	if (table != null) {
	  	table.setFont(font.deriveFont(Font.BOLD));
	  	updateTableLayout();
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

    @Override
    public Object getValueAt(int row, int col) {
    	if (col < 0) return null;
      return (col == 0) ? getLabelText(row) : 
      	rowObjects.get(row)[1] instanceof NumberField? getFieldText(row) :
      		rowObjects.get(row)[1] instanceof JCheckBox? isChecked(row): null;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
    	if (col == 1) {
      	if (rowObjects.get(row)[1] instanceof NumberField) {
      		NumberField nf = (NumberField)rowObjects.get(row)[1];
      		return nf.isEnabled();
      	}    		
    	}
      return col == 1; // only the value column is editable
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 1) {
        	if (rowObjects.get(row)[1] instanceof NumberField) {
	        	NumberField field = (NumberField)rowObjects.get(row)[1];
	        	// set yellow background so actionListener will recognize that value has changed
	        	field.setBackground(Color.YELLOW);
	        	field.setValue((double)value);
	        	field.postActionEvent();
	        	field.setBackground(Color.WHITE);
        	}
        	else if (rowObjects.get(row)[1] instanceof JCheckBox) {
        		JCheckBox checkbox = (JCheckBox)rowObjects.get(row)[1];
        		if (checkbox.isSelected() != (Boolean) value)
        			checkbox.doClick(0);
        	}
          fireTableCellUpdated(row, col);
      		javax.swing.SwingUtilities.invokeLater(()-> {
             updateTableLayout();
      		});
        }
    }
    
    private Object getLabelText(int row) {
    	TextLineLabel label = (TextLineLabel)rowObjects.get(row)[0];
    	String text = label.getText();    
    	// deal with subscripts
    	if (text.contains("_{")) {
				return "<html><b>"+TeXParser.removeSubscript(text)
					+"<sub style=\"font-size: 80%;\">"
					+TeXParser.getSubscript(text)
					+ "</b></sub></html>";
    	}
    	return "<html><b>"+text + "</b></html>";
    	
    }
    
    private String getFieldText(int row) {
    	NumberField field = (NumberField)rowObjects.get(row)[1];
	    return field.getText();
    }

    private boolean isChecked(int row) {
    	JCheckBox box = (JCheckBox)rowObjects.get(row)[1];
	    return box.isSelected();
    }

  }
  
  class NFTable extends JTable {
  	
	   private final NFCellEditor nfCellEditor = new NFCellEditor();
	   private final NFCellRenderer nfCellRenderer = new NFCellRenderer();
	   
	   public NFTable(NFTableModel model) {
	      super(model);
	   }
	
	   @Override
	   public TableCellEditor getCellEditor(int row, int column) {
     	 if (column==1 && rowObjects.get(row)[1] instanceof JCheckBox) {
    		 return table.getDefaultEditor(Boolean.class);
    	 }    		
	     return nfCellEditor;
	   }
	   
	   @Override
		 public TableCellRenderer getCellRenderer(int row, int column) {
       if (column==1 && rowObjects.get(row)[1] instanceof JCheckBox) {
      	 return table.getDefaultRenderer(Boolean.class);
       }    		
       else if (column==1 && rowObjects.get(row)[1] instanceof NumberField) {
      		NumberField nf = (NumberField)rowObjects.get(row)[1];
      		nfCellRenderer.setForeground(nf.isEnabled()? Color.BLACK: Color.GRAY);
      	  return nfCellRenderer;
       }    		
	  	 
	     TableCellRenderer renderer = super.getCellRenderer(row, column);
	     if (renderer instanceof DefaultTableCellRenderer) {
	    	 ((DefaultTableCellRenderer)renderer).setHorizontalAlignment(
	    			 column==0? SwingConstants.CENTER: SwingConstants.LEFT);
	     }
	     return renderer;
		 }
   
	   @Override
	   public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        // block selection on non-editable cells
        if (!isCellEditable(row, column)) {
            return;
        }
        super.changeSelection(row, column, toggle, extend);
	   }
	}
  
  
  /**
   * An editor for the NumberFields.
   */
  class NFCellEditor extends DefaultCellEditor {

    private final JTextField textField;
    private NumberField nf;
    private Color textColor;

    public NFCellEditor() {
        super(new JTextField());
        textField = (JTextField) getComponent();
        textField.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
        textColor = textField.getForeground();
         // single-click activation
        setClickCountToStart(1);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
	  	if (column == 1) {
	  		JComponent comp = (JComponent)rowObjects.get(row)[1];
	  		if (comp instanceof NumberField) {
	  			nf = (NumberField)comp;
	  			String text = nf.getText();
          if (nf.getUnits() != null && text.endsWith(nf.getUnits())) {
            text = text.substring(0, text.length() - nf.getUnits().length()).trim();
          }
          textField.setText(text);
          textField.setForeground(nf.isEnabled()? textColor: Color.GRAY);
//	          textField.selectAll(); // highlight the number so typing replaces it immediately
          return textField;
	  		}
	  	}
      textField.setText(null);
      return textField;
    }

    // called when the user finishes editing
    @Override
    public Object getCellEditorValue() {
    	if (nf != null) {
    		// the NumberField itself does the parsing!
    		return nf.getValue(textField.getText());
    	}
    	return null;
    }
  }

  class NFCellRenderer extends DefaultTableCellRenderer {
    private final Border padding = BorderFactory.createEmptyBorder(0, 2, 0, 0);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // add padding
        setBorder(BorderFactory.createCompoundBorder(getBorder(), padding));
        return this;
    }
  };	
}


