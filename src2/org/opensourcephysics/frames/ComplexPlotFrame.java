/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.frames;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.ComplexDataset;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.DataTableFrame;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.display.PlottingPanel;

/**
 * A DrawingFrame that plots a complex function.
 *
 * @author W. Christian
 * @version 1.0
 */
public class ComplexPlotFrame extends DrawingFrame {
  protected ComplexDataset complexDataset = new ComplexDataset();
  protected DataTable dataTable = new DataTable();
  JMenuItem ampPhaseItem, reImItem, postItem, barItem;
  protected DataTableFrame tableFrame;

  /**
   * Constructs a PlotComplexFrame with the given frame title and axes labels.
   *
   * @param xlabel String
   * @param ylabel String
   * @param frameTitle String
   */
  public ComplexPlotFrame(String xlabel, String ylabel, String frameTitle) {
    super(new PlottingPanel(xlabel, ylabel, frameTitle));
    setTitle(frameTitle);
    drawingPanel.addDrawable(complexDataset);
    dataTable.add(complexDataset);
    addMenuItems();
    setAnimated(true);
    setAutoclear(true);
  }

  /**
   * Gets the complex dataset that is being plotted.
   * @return
   */
  public ComplexDataset getComplexDataset() {
    return complexDataset;
  }

  /**
   * Adds Views menu items on the menu bar.
   */
  protected void addMenuItems() {
    JMenuBar menuBar = getJMenuBar();
    if(menuBar==null) {
      return;
    }
    JMenu helpMenu = this.removeMenu(DisplayRes.getString("DrawingFrame.Help_menu_item")); //$NON-NLS-1$
    JMenu menu = getMenu(DisplayRes.getString("DrawingFrame.Views_menu"));                 //$NON-NLS-1$
    if(menu==null) {
      menu = new JMenu(DisplayRes.getString("DrawingFrame.Views_menu")); //$NON-NLS-1$
      menuBar.add(menu);
      menuBar.validate();
    } else {                                                             // add a separator if tools already exists
      menu.addSeparator();
    }
    if(helpMenu!=null) {
      menuBar.add(helpMenu);
    }
    ButtonGroup menubarGroup = new ButtonGroup();
    // amp and phase
    ampPhaseItem = new JRadioButtonMenuItem(DisplayRes.getString("ComplexPlotFrame.MenuItem.AmpPhase")); //$NON-NLS-1$
    menubarGroup.add(ampPhaseItem);
    ampPhaseItem.setSelected(true);
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToAmpAndPhaseView();
      }

    };
    ampPhaseItem.addActionListener(actionListener);
    menu.add(ampPhaseItem);
    // post view
    postItem = new JRadioButtonMenuItem(DisplayRes.getString("ComplexPlotFrame.MenuItem.PostView")); //$NON-NLS-1$
    menubarGroup.add(postItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToPostView();
      }

    };
    postItem.addActionListener(actionListener);
    menu.add(postItem);
    // bar view
    barItem = new JRadioButtonMenuItem(DisplayRes.getString("ComplexPlotFrame.MenuItem.BarView")); //$NON-NLS-1$
    menubarGroup.add(barItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToPhaseBarView();
      }

    };
    barItem.addActionListener(actionListener);
    menu.add(barItem);
    // re im component view
    reImItem = new JRadioButtonMenuItem(DisplayRes.getString("ComplexPlotFrame.MenuItem.RealImaginary")); //$NON-NLS-1$
    menubarGroup.add(reImItem);
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        convertToReImView();
      }

    };
    reImItem.addActionListener(actionListener);
    menu.add(reImItem);
    menu.addSeparator();
    // add a menu item to show the data table
    JMenuItem tableItem = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
    tableItem.setAccelerator(KeyStroke.getKeyStroke('T', MENU_SHORTCUT_KEY_MASK));
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showDataTable(true);
      }

    };
    tableItem.addActionListener(actionListener);
    menu.add(tableItem);
    JMenuItem legendItem = new JMenuItem(DisplayRes.getString("GUIUtils.PhaseLegend")); //$NON-NLS-1$
    actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        complexDataset.showLegend();
      }

    };
    legendItem.addActionListener(actionListener);
    menu.add(legendItem);
    // add data table to the popup menu
    if((drawingPanel!=null)&&(drawingPanel.getPopupMenu()!=null)) {
      JMenuItem item = new JMenuItem(DisplayRes.getString("DrawingFrame.DataTable_menu_item")); //$NON-NLS-1$
      item.addActionListener(actionListener);
      drawingPanel.getPopupMenu().add(item);
    }
  }

  public void setCentered(boolean centered) {
    complexDataset.setCentered(centered);
  }

  public void convertToPostView() {
    complexDataset.setMarkerShape(ComplexDataset.PHASE_POST);
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
    postItem.setSelected(true);
  }

  public void convertToAmpAndPhaseView() {
    complexDataset.setMarkerShape(ComplexDataset.PHASE_CURVE);
    complexDataset.setCentered(true);
    drawingPanel.invalidateImage();
    drawingPanel.repaint();
    ampPhaseItem.setSelected(true);
  }

  public void convertToPhaseBarView() {
    complexDataset.setMarkerShape(ComplexDataset.PHASE_BAR);
    complexDataset.setCentered(false);
    drawingPanel.invalidateImage();
    barItem.setSelected(true);
    drawingPanel.repaint();
  }

  public void convertToReImView() {
    complexDataset.setMarkerShape(ComplexDataset.RE_IM_CURVE);
    drawingPanel.invalidateImage();
    reImItem.setSelected(true);
    drawingPanel.repaint();
  }

  /**
   *  Appends an (x, re, im) datum to the Dataset with the given index.
   *
   * @param  x
   * @param  re
   * @param  im
   */
  public void append(double x, double re, double im) {
    complexDataset.append(x, re, im);
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   * Appends x and z data to the Dataset.
   *
   * Z array has length twice that of x array.
   * <PRE>
   *    Re(z) = z[2*i]
   *    Im(z) = z[2*i + 1]
   * </PRE>
   *
   * @param x
   * @param z
   */
  public void append(double[] x, double[] z) {
    complexDataset.append(x, z);
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   * Appends (x, re, im) arrays to the Dataset.
   *
   * @param  xpoints
   * @param  re
   * @param  im
   */
  public void append(double[] xpoints, double[] re, double[] im) {
    complexDataset.append(xpoints, re, im);
    if((tableFrame!=null)&&tableFrame.isShowing()) {
      dataTable.refreshTable();
    }
  }

  /**
   *  Sets the connected flag for all datasets.
   *
   * @param connected true if connected; false otherwise
   */
  public void setConnected(boolean connected) {
    complexDataset.setConnected(connected);
  }

  /**
   * Clears drawable objects added by the user from this frame.
   */
  public void clearDrawables() {
    drawingPanel.clear(); // removes all drawables
    drawingPanel.addDrawable(complexDataset);
  }

  /**
   * Gets Drawable objects added by the user to this frame.
   *
   * @return the list
   */
  public synchronized ArrayList<Drawable> getDrawables() {
    ArrayList<Drawable> list = super.getDrawables();
    list.remove(complexDataset);
    return list;
  }

  /**
   * Gets Drawable objects added by the user of an assignable type. The list contains
   * objects that are assignable from the class or interface.
   *
   * @param c the type of Drawable object
   *
   * @return the cloned list
   *
   * @see #getObjectOfClass(Class c)
   */
  public synchronized <T extends Drawable> ArrayList<T> getDrawables(Class<T> c) {
    ArrayList<T> list = super.getDrawables(c);
    list.remove(complexDataset);
    return list;
  }

  /**
   * Clears the data from all datasets and removes all objects from the drawing panel except the dataset manager.
   *
   * Dataset properties are preserved because only the data is cleared.
   */
  public void clearData() {
    complexDataset.clear();
    if(dataTable!=null) {
      dataTable.refreshTable();
    }
    if(drawingPanel!=null) {
      drawingPanel.invalidateImage();
    }
  }

  /**
   * Shows or hides the data table.
   *
   * @param show boolean
   */
  public synchronized void showDataTable(boolean show) {
    if(show) {
      if((tableFrame==null)||!tableFrame.isDisplayable()) {
        tableFrame = new DataTableFrame(getTitle()+" "+DisplayRes.getString("TableFrame.TitleAddOn.Data"), dataTable); //$NON-NLS-1$ //$NON-NLS-2$
        tableFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      }
      dataTable.refreshTable();
      tableFrame.setVisible(true);
    } else {
      tableFrame.setVisible(false);
      tableFrame.dispose();
      tableFrame = null;
    }
  }

  public static XML.ObjectLoader getLoader() {
    return new ComplexPlotFrameLoader();
  }

  static protected class ComplexPlotFrameLoader extends DrawingFrame.DrawingFrameLoader {
    /**
    * Creates a PlotFame.
    *
    * @param  control XMLControl
    * @return Object
    */
    public Object createObject(XMLControl control) {
      ComplexPlotFrame frame = new ComplexPlotFrame("x", "y", DisplayRes.getString("ComplexPlotFrame.Title")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return frame;
    }

    /**
     * Loads the object with data from the control.
     *
     * @param control XMLControl
     * @param obj Object
     * @return Object
     */
    public Object loadObject(XMLControl control, Object obj) {
      super.loadObject(control, obj);
      ComplexPlotFrame frame = ((ComplexPlotFrame) obj);
      ArrayList<ComplexDataset> list = frame.getObjectOfClass(ComplexDataset.class);
      if(list.size()>0) { // assume the first ComplexDataset belongs to this frame
        frame.complexDataset = list.get(0);
        frame.dataTable.clear();
        frame.dataTable.add(frame.complexDataset);
      }
      return obj;
    }

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
