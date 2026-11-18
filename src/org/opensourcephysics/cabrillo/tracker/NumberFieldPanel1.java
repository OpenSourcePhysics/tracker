package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.display.TeXParser;
import org.opensourcephysics.media.core.NumberField;

public class NumberFieldPanel1 extends JPanel {
	
	private static HashMap<TrackerPanel, NumberFieldPanel1[]> npPanelArrays;
	static {
		npPanelArrays = new HashMap<TrackerPanel, NumberFieldPanel1[]>();
	}
	
	public static NumberFieldPanel1[] getNFPanelArray(TrackerPanel tp) {
		NumberFieldPanel1[] array = npPanelArrays.get(tp);
		if (array == null) {
			array = new NumberFieldPanel1[] 
				{new NumberFieldPanel1(tp), new NumberFieldPanel1(tp)};
			npPanelArrays.put(tp, array);
		}
		return array;
	}
	
	private ArrayList<Object[]> rowObjects = new ArrayList<Object[]>();
	private int trackerPanelID;
	private TFrame frame;
	private Component positionComp;
	private JLabel sizingLabel = new JLabel();
	private MouseListener myMouseListener;
	private ActionListener myActionListener;
	
	private NumberFieldPanel1(TrackerPanel tp) {
		super(new GridBagLayout());
		trackerPanelID = tp.getID();
		frame = tp.getTFrame();
		createGUI();

		super.setVisible(false);
	}
	
	public void createGUI() {
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
			javax.swing.SwingUtilities.invokeLater(()-> {
				updateLayout();
			});
		};		
		frame.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
	      if (isVisible()) refreshFloatingBounds();
	    }
		});
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
    NumberFieldPanel1 nfp = getNFPanelArray(panel())[0];
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
	
  public void updateLayout() {
  	if (rowObjects == null) return;
    
    // determine row height based on font metrics
    FontMetrics fm = getFontMetrics(getFont());
    int rowHeight = fm.getHeight() + 8; // add padding
	  int totalHeight = rowObjects.size() * rowHeight;

    // determine column widths based on preferred size of sizingLabel
	  int[] columnWidths = new int[] {0, 0};
    int totalWidth = 0;
	  for (int col = 0; col < 2; col++) {
	    // find the widest string in a column
	    for (int row = 0; row < rowObjects.size(); row++) {
	       Object next = rowObjects.get(row)[col];
	       int w = 0;
	       if (next instanceof TextLineLabel) {
	       	 TextLineLabel label = (TextLineLabel)next;
	       	 String text = label.getText();
	      	 if (text.contains("_{")) { //$NON-NLS-1$
	     			 text = TeXParser.removeSubscripting(text);
	     		 }
	         sizingLabel.setFont(label.getFont());
	      	 sizingLabel.setText(text);
           w = sizingLabel.getPreferredSize().width;            	 
	       }
	       else if (next instanceof NumberField) {
	      	 NumberField field = (NumberField)next;
	         sizingLabel.setFont(field.getFont());
	      	 sizingLabel.setText(field.getText());
           w = sizingLabel.getPreferredSize().width;            	 
	       }
	       else if (next instanceof JComponent) {
	         JComponent comp = (JComponent)next;
           w = comp.getPreferredSize().width;            	 
	       }
	       columnWidths[col] = Math.max(columnWidths[col], w);
	    }
	    columnWidths[col] = Math.max(columnWidths[col]+8, 28); // minimum size
		  
      totalWidth += columnWidths[col];
    }

    setSize(new Dimension(totalWidth, totalHeight));
    
    GridBagConstraints gbc = new GridBagConstraints();
    removeAll();
	  for (int col = 0; col < 2; col++) {
      gbc.fill = GridBagConstraints.BOTH;
      gbc.ipady = 0;  // vertical cell padding
      gbc.ipadx = 0; // horizontal cell padding
      Dimension dim = new Dimension(columnWidths[col], rowHeight);
      for (int row = 0; row < rowObjects.size(); row++) {
        gbc.gridy = row;
        gbc.gridx = col;
        gbc.weightx = col == 0? 1: 0; // ??
        gbc.weighty = 0.5; // equal row heights
        gbc.fill = GridBagConstraints.BOTH;
      	Component comp = (Component)rowObjects.get(row)[col];
        comp.setPreferredSize(dim);
        add(comp, gbc);
      }
	  }
	  revalidate();
	  repaint();
	}
  
  @Override
	public void setFont(Font font) {
  	super.setFont(font);
	  updateLayout();
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
  
}


