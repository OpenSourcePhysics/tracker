/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */
package org.opensourcephysics.display;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * An <CODE>Icon</CODE> that can be resized.
 * 
 * @author Doug Brown
 */
public class ResizableIcon implements Icon {

  protected int baseWidth, baseHeight, w, h;
  protected BufferedImage baseImage;
  protected Icon icon;

  /**
   * Creates a <CODE>ResizableIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   */
  public ResizableIcon(URL location) {
    this(new ImageIcon(location));
  }

  /**
   * Creates a <CODE>ResizableIcon</CODE> from the specified Icon.
   * 
   * @param the icon to resize
   */
  public ResizableIcon(Icon icon) {
  	// prevent nesting resizable icons
  	while (icon instanceof ResizableIcon) {
  		icon = ((ResizableIcon)icon).icon;
  	}
  	if(icon==null) {
  		baseWidth = w=0;
  		baseHeight = h=0;
  	}else {
	    this.icon = icon;
	    baseWidth = w = icon.getIconWidth();
	    baseHeight = h = icon.getIconHeight();
  	}
  }

  @Override
  public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
    if (icon == null) {
      return;
    }
    if (baseImage==null || baseImage.getWidth()!=baseWidth || baseImage.getHeight()!=baseHeight) {
    	baseImage = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_ARGB);    	
    }
    Graphics2D g2 = baseImage.createGraphics();
    g2.setComposite(AlphaComposite.Clear); 
    g2.setColor(Color.red);
    g2.fillRect(0, 0, baseWidth, baseHeight); 
    g2.setComposite(AlphaComposite.SrcOver);
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
    g.drawImage(baseImage, x, y, w, h, c);
  }

  @Override
  public int getIconWidth() {
    return w;
  }

  @Override
  public int getIconHeight() {
    return h;
  }
  
  /**
   * Gets the base icon which is resized by this ResizableIcon.
   * 
   * @return the base icon
   */
  public Icon getBaseIcon() {
  	return icon;
  }
  
  /**
   * Magnifies the icon by a specified integer factor.
   * 
   * @param factor the factor
   */
  public void resize(int factor) {
  	int n = Math.max(factor, 1);
  	w = n*baseWidth;
  	h = n*baseHeight;
  }
  
}
