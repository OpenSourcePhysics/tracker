package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.opensourcephysics.cabrillo.tracker.TTrack.TextLineLabel;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.NumberField;

public class NumberFieldPanel extends JPanel {

	private GridBagConstraints gbc = new GridBagConstraints();

	private static HashMap<TrackerPanel, NumberFieldPanel[]> npPanelArrays;
	static {
		npPanelArrays = new HashMap<TrackerPanel, NumberFieldPanel[]>();
	}

	public static NumberFieldPanel[] getNFPanelArray(TrackerPanel tp) {
		NumberFieldPanel[] array = npPanelArrays.get(tp);
		if (array == null) {
			array = new NumberFieldPanel[] { new NumberFieldPanel(tp), new NumberFieldPanel(tp) };
			npPanelArrays.put(tp, array);
		}
		return array;
	}

	private ArrayList<Object[]> rowObjects = new ArrayList<Object[]>();
	private int trackerPanelID;
	private TFrame frame;
	private Component positionComp;
	private MouseListener myMouseListener;
	private ActionListener myActionListener;

	private NumberFieldPanel(TrackerPanel tp) {
		super(new GridBagLayout());
		trackerPanelID = tp.getID();
		frame = tp.getTFrame();
		createGUI();
		super.setVisible(false);
	}

	public void createGUI() {
		gbc.fill = GridBagConstraints.BOTH;
		gbc.ipady = 8; // Vertical cell padding
		gbc.ipadx = 12; // Horizontal cell padding
		setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		setBackground(new Color(245, 245, 245));
		myMouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (OSPRuntime.isPopupTrigger(e)) {
					if (e.getSource() instanceof NumberField) {
						NumberField field = (NumberField) e.getSource();
						if (field instanceof TTrack.hasTrack) {
							TTrack.hasTrack tnf = (TTrack.hasTrack) field;
							TTrack track = tnf.getTrack();
							track.showFormatPopup(field, null, null);
						}
					}
				}
			}
		};
		myActionListener = e -> {
			javax.swing.SwingUtilities.invokeLater(()-> {
				refreshFloatingBounds();
			});
		};		
		frame.addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
	      if (isVisible()) refreshFloatingBounds();
	    }
		});
	}

	public void addRow(JComponent label, JComponent comp) {
		if (label instanceof JLabel) {
			String text = ((JLabel) label).getText();
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
				((NumberField) comp).addActionListener(myActionListener);
			}
			int n = rowObjects.size();
			rowObjects.add(new Object[] { label, comp });
			addToGrid(n, label, comp);
		}
	}

	private TrackerPanel panel() {
		return frame.getTrackerPanelForID(trackerPanelID);
	}

	protected void clear() {
		for (int row = 0; row < rowObjects.size(); row++) {
			Component comp = (Component) rowObjects.get(row)[1];
			comp.removeMouseListener(myMouseListener);
			if (comp instanceof NumberField) {
				((NumberField) comp).removeActionListener(myActionListener);
			}
		}
		rowObjects.clear();
		setPositionComponent(null);
		removeAll();
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
		Point p = new Point(0, positionComp.getParent().getHeight());
		Point pt = SwingUtilities.convertPoint(positionComp, p, layeredPane);
		Dimension dim = getPreferredSize();

		// make sure second panel stays to the right of first
		NumberFieldPanel nfp = getNFPanelArray(panel())[0];
		if (this != nfp) {
			Rectangle rect = nfp.getBounds();
			pt.x = Math.max(pt.x, rect.x + rect.width + 2);
		}

		// set the location boundaries
		setBounds(pt.x, pt.y - 10, dim.width, dim.height);
		revalidate();
	}

	public Component getPositionComponent() {
		return positionComp;
	}

	public int getRowCount() {
		return rowObjects.size();
	}

	public void addToGrid(int row, JComponent label, JComponent comp) {
		// fill gridbag
		gbc.gridy = row;
		gbc.gridx = 0;
		gbc.weightx = 0; // column 0 stays fixed
		gbc.weighty = 1;
//					gbc.weighty = 1.0 / rowObjects.size(); // equal heights
		add(label, gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		add(comp, gbc);
	}

	public void updateLayout() {
		refreshFloatingBounds();
	}

}
