package org.opensourcephysics.tools;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * A multiline tooltip UI based on open source code from http://code.ohloh.net.
 */
public class MultiLineToolTipUI extends BasicToolTipUI {
	
	private static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();
	private static JTextArea textArea ;
	
	private CellRendererPane rendererPane;
		
	public static ComponentUI createUI(JComponent c) {
	  return sharedInstance;
	}
	
	/**
	 * Constructor
	 */
	protected MultiLineToolTipUI() {
	  super();
	}
	
	@Override
	public void installUI(JComponent c) {
    super.installUI(c);
    rendererPane = new CellRendererPane();
    c.add(rendererPane);
	}
	
	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);		
	  c.remove(rendererPane);
	  rendererPane = null;
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
	  Dimension size = c.getSize();
	  textArea.setBackground(c.getBackground());
		rendererPane.paintComponent(g, textArea, c, 1, 1, size.width-1, size.height-1, true);
	}
	
	@Override
	public Dimension getPreferredSize(JComponent c) {
		String tipText = ((JToolTip)c).getTipText();
		if (tipText==null)
			return new Dimension(0,0);
		textArea = new JTextArea(tipText);
		textArea.setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
	  rendererPane.removeAll();
		rendererPane.add(textArea);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(false);

		Dimension dim = textArea.getPreferredSize();		
		dim.height += 2;
		dim.width += 2;
		return dim;
	}
	
	@Override
	public Dimension getMinimumSize(JComponent c) {
	  return getPreferredSize(c);
	}
	
	@Override
	public Dimension getMaximumSize(JComponent c) {
	  return getPreferredSize(c);
	}
}

