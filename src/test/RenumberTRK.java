package test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import org.opensourcephysics.controls.ControlsRes;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.controls.XMLTreeNode;
import org.opensourcephysics.controls.XMLTreePanel;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.ResourceLoader;

import javajs.async.AsyncFileChooser;


public class RenumberTRK {
	
	XMLTreePanel treePanel;
	String filePath;
	
	public static void main(String[] args) {
		RenumberTRK app = new RenumberTRK();
		app.open();
	}
	
	/**
	 * Inspects the drawing frame by using an xml document tree.
	 */
	public void inspectXML(String path) {
		XMLControlElement xml = new XMLControlElement(path);
		// display a TreePanel in a modal dialog
		if (treePanel == null) {
			treePanel = new MyXMLTreePanel(xml);
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setTitle("XML Inspector");
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(treePanel, BorderLayout.CENTER);
			JPanel buttonbar = new JPanel();
			panel.add(buttonbar, BorderLayout.SOUTH);
			JButton savebutton = new JButton("Save Changes");
			savebutton.addActionListener((e) -> {
				treePanel.getControl().write(filePath);
			});
			buttonbar.add(savebutton);
			JButton openbutton = new JButton("Open TRK...");
			openbutton.addActionListener((e) -> {
				open();
			});
			buttonbar.add(openbutton);
			frame.setContentPane(panel);
			frame.setSize(new Dimension(800, 800));
			frame.setVisible(true);
		}
		else {
			treePanel.getControl().readXML(xml.toXML());
			treePanel.refresh();
		}
		treePanel.setSelectedNode("tracks");
	}

	/**
	 * Opens trk file.
	 */  
	public void open() {
		AsyncFileChooser chooser = OSPRuntime.getChooser();
		if (chooser == null) {
			return;
		}
		String chooserPath = (String)OSPRuntime.getPreference("file_chooser_directory");
		if (chooserPath != null) {
			chooser.setCurrentDirectory(new File(chooserPath));
		}
		chooser.setDialogTitle("Open");
		chooser.resetChoosableFileFilters();
		chooser.setAcceptAllFileFilterUsed(true);
		Runnable ok = new Runnable() {
			@Override
			public void run() {
				File file = chooser.getSelectedFile();
				OSPRuntime.setPreference("file_chooser_directory", file.getParent());
				OSPRuntime.savePreferences();
				String path = file.getAbsolutePath();
				
				// check for trk file
				if (path.toLowerCase().endsWith(".trk")) {
					filePath = file.getAbsolutePath();
					inspectXML(filePath);
				}
			}			
		};
		chooser.showOpenDialog(null, ok, () -> {});
	}

	class MyXMLTreePanel extends XMLTreePanel {
	  public MyXMLTreePanel(XMLControl control) {
	    super(control);
	  }
	  
	  @Override
	  protected void createGUI() {
	  	super.createGUI();
	  	
	  	// replace popup
	    popup = new JPopupMenu();
	    JMenuItem item = new JMenuItem("Renumber");
	    popup.add(item);
	    item.addActionListener(new ActionListener() {
	      @Override
	      public void actionPerformed(ActionEvent e) {
	        XMLTreeNode node = (XMLTreeNode) tree.getLastSelectedPathComponent();
	        XMLProperty prop = node.getProperty();
	        // get xml
	        String xml = prop.toString();
	        String controlXML = getControl().toXML();
	        int insertAt = controlXML.indexOf(xml);
	        if (insertAt == -1)
	        	return;
	        String pre = controlXML.substring(0, insertAt);
	        String post = controlXML.substring(insertAt + xml.length());
	        int k = getShift();
	        if (k == 0)
	        	return;
	        // go through entire array and change numbering
	        StringBuffer buf = new StringBuffer();
	        String toMatch = "property name=\"[";
	        int n = xml.indexOf(toMatch);
	        while (n > -1) {
	        	buf.append(xml.substring(0, n + toMatch.length()));
	        	xml = xml.substring(n + toMatch.length());
	        	n = xml.indexOf("]");
		        int i = Integer.parseInt(xml.substring(0, n));
		        buf.append(String.valueOf(i + k));
		        xml = xml.substring(n);
		        n = xml.indexOf(toMatch);	        	
	        }
	        buf.append(xml);
	        xml = buf.toString();
	        controlXML = pre + xml + post;
	        getControl().readXML(controlXML);
	        TreePath treePath = tree.getSelectionPath();

	        refresh(treePath);	        
	      }
	    });

	  }
	  
	  int getShift() {
      String shift = JOptionPane.showInputDialog("Shift array numbering by:");
      if ("".equals(shift.trim()))
      	return 0;
      try {
      	return Integer.parseInt(shift);
      } catch (Exception e) {
      	
      }
	  	return getShift();
	  }
	  
	  public void refresh(TreePath path) {
	  	refresh();
	    setSelectedNode(path);
	  }
	  
	  protected MouseListener getMouseListener() {
	  	return new MouseAdapter() {
	      @Override
	      public void mouseClicked(MouseEvent e) {
	        if (OSPRuntime.isPopupTrigger(e)) {
	          // select node and show popup menu
	          TreePath path = tree.getPathForLocation(e.getX(), e.getY());
	          if(path==null) {
	            return;
	          }
	          tree.setSelectionPath(path);
	          XMLTreeNode node = (XMLTreeNode) tree.getLastSelectedPathComponent();
		        XMLProperty prop = node.getProperty();
	      		if (prop.getPropertyType() == XMLProperty.TYPE_ARRAY
	      				&& prop.getPropertyName().equals("framedata")) { //$NON-NLS-1$	      			
	            popup.show(tree, e.getX(), e.getY()+8);	      			
	      		}
	        }
	      }
	    };	  	
	  }	 	  
	}
	
}
