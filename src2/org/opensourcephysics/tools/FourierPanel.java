package org.opensourcephysics.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.opensourcephysics.analysis.FourierSinCosAnalysis;
import org.opensourcephysics.display.ColorIcon;
import org.opensourcephysics.display.Data;
import org.opensourcephysics.display.DataTable;
import org.opensourcephysics.display.Dataset;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.PlottingPanel;
import org.opensourcephysics.display.TeXParser;

/**
 * A JPanel that displays a plot and table of the Fourier spectrum of an input Dataset.
 * 
 * @author Doug Brown
 */
public class FourierPanel extends JPanel {
  
	// instance fields
	protected Dataset sourceData;
  protected PlottingPanel plot;
  protected DataTable table;
  protected DatasetManager fourierManager; // local datasets: frequency vs power, cosine, sine
  protected JSplitPane splitPane;
  protected JCheckBox[] buttons;
  protected JPanel plotPanel;


  /**
   * Constructs a FourierPanel.
   */
  public FourierPanel() {
  	super(new BorderLayout());
    createGUI();
  }

  // _______________________ protected & private methods ______________________

  /**
   * Creates the GUI.
   */
  protected void createGUI() {
  	fourierManager = new DatasetManager();
  	fourierManager.setXPointsLinked(true);
    table = new DataTable();
    table.add(fourierManager);
    plot = new PlottingPanel("", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    plotPanel = new JPanel(new BorderLayout());
    plotPanel.add(plot, BorderLayout.CENTER);
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setResizeWeight(1);
    JScrollPane scroller = new JScrollPane(table);
    splitPane.setRightComponent(scroller);
    splitPane.setLeftComponent(plotPanel);
    add(splitPane, BorderLayout.CENTER);
  }
  
  /**
   * Refreshes the fourier data based on a source dataset.
   *
   * @param data the source dataset
   * @param name a name that identifies the source
   */
  protected void refreshFourierData(Dataset data, String name) {
    JDialog dialog = (JDialog)this.getTopLevelAncestor();
    dialog.setTitle(ToolsRes.getString("DataToolTab.Dialog.Fourier.Title")); //$NON-NLS-1$
  	fourierManager.removeDatasets();
    Data fourierData = createFourierData(data);
    if (fourierData==null) return;
  	ArrayList<Dataset> datasets = fourierData.getDatasets();
    createButtons(datasets);
    for (Dataset next: datasets) {
    	fourierManager.addDataset(next);
    }
    table.refreshTable();
    name = TeXParser.removeSubscripting(name);
    String x = TeXParser.removeSubscripting(data.getXColumnName());
    String y = TeXParser.removeSubscripting(data.getYColumnName());
    plot.setTitle(name+" ["+x+", "+y+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    refreshPlot();
  }
  
  /**
   * Refreshes the plot to show currrently selected variables.
   */
  protected void refreshPlot() {
  	plot.removeDrawables(Dataset.class);
  	String s = ""; //$NON-NLS-1$
  	for (int i=0; i<buttons.length; i++) {
  		if (buttons[i].isSelected()) {
  	  	Dataset data = fourierManager.getDataset(i);
  			plot.addDrawable(data);
		    plot.setXLabel(data.getXColumnName());
		    if (s.length()>0) s += ", "; //$NON-NLS-1$
		    s += data.getYColumnName();
  		}
  	}
    plot.setYLabel(s);	
    plot.repaint();
  }
  
  /**
   * Creates the plot variable buttons.
   *
   * @param datasets the fourier datasets
   */
  protected void createButtons(ArrayList<Dataset> datasets) {
  	if (buttons==null) {
  		buttons = new JCheckBox[datasets.size()];
  		JPanel buttonPanel = new JPanel();
  		plotPanel.add(buttonPanel, BorderLayout.SOUTH);
      for (int i=0; i<buttons.length; i++) {
      	Dataset next = datasets.get(i);
      	buttons[i] = new PlotCheckBox(next.getYColumnName(), next.getFillColor());
      	buttonPanel.add(buttons[i]);
      }
      buttons[0].setSelected(true);
  	}
  	
  }
  
  // ____________________________ static methods ______________________________

  /**
   * Creates a Data object containing the Fourier spectrum of the input Dataset.
   *
   * @param dataset the input
   * @return the fourier spectrum Data
   */
  public static Data createFourierData(Dataset dataset) {
  	if (dataset==null)
  		return null;
    double[] x = dataset.getXPoints();
    double[] y = dataset.getYPoints();
    if (y.length<2) return null;
    if (y.length%2==1) { // odd number of points
      double[] xnew = new double[y.length-1];
      double[] ynew = new double[xnew.length];
      System.arraycopy(x, 0, xnew, 0, xnew.length);
      System.arraycopy(y, 0, ynew, 0, ynew.length);
      dataset.clear();
      dataset.append(xnew, ynew);
      x = xnew;
      y = ynew;
    }
    FourierSinCosAnalysis fft = new FourierSinCosAnalysis();
		fft.doAnalysis(x, y, 0);
    return fft;
  }

  //____________________________________ inner classes _________________________________
  
  class PlotCheckBox extends JCheckBox {
  	
  	ColorIcon icon;
  	Color outlineColor, fillColor=Color.WHITE;
  	
  	PlotCheckBox(String text, Color color) {
  		super(text);
  		outlineColor = color;
  		icon = new ColorIcon(fillColor, outlineColor, 13, 13);
  		setIcon(icon);
      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	JCheckBox checkBox = (JCheckBox)e.getSource();
        	setSelected(checkBox.isSelected());
          refreshPlot();
        }
      });
  	}
  	
  	public void setSelected(boolean select) {
      icon.setColor(select? outlineColor: fillColor);
  		super.setSelected(select);
  	}
  	
  }
}
