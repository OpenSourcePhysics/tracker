package org.opensourcephysics.cabrillo.tracker;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.opensourcephysics.controls.ListChooser;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.controls.XMLProperty;
import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.DataFunctionPanel;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionAutoloadManager;
import org.opensourcephysics.tools.FunctionEditor;
import org.opensourcephysics.tools.FunctionPanel;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.ParamEditor;
import org.opensourcephysics.tools.Parameter;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * A FunctionTool for building data functions for track data.
 */
public class TrackDataBuilder extends FunctionTool {
	
	private TrackerPanel trackerPanel;
	private JButton loadButton, saveButton, autoloadButton;
	private AutoloadManager autoloadManager;

	/**
	 * Constructor.
	 * 
	 * @param trackerPanel the TrackerPanel with the tracks
	 */
	protected TrackDataBuilder(TrackerPanel trackerPanel) {
		super(trackerPanel);
		this.trackerPanel = trackerPanel;
		createButtons();
		setToolbarComponents(new Component[] {loadButton, saveButton, 
				Box.createHorizontalGlue(), autoloadButton});
		setHelpPath("data_builder_help.html"); //$NON-NLS-1$
		addPropertyChangeListener("panel", trackerPanel); //$NON-NLS-1$
		addPropertyChangeListener("function", trackerPanel); //$NON-NLS-1$
		addPropertyChangeListener("visible", trackerPanel); //$NON-NLS-1$
		ArrayList<Drawable> nogos = trackerPanel.getSystemDrawables();
		Iterator<TTrack> it = trackerPanel.getTracks().iterator();
		while (it.hasNext()) {
			TTrack track = it.next();
			if (nogos.contains(track)) continue;
			FunctionPanel panel = createFunctionPanel(track);
	    addPanel(track.getName(), panel);
		}
	}
	
	/**
	 * Creates the save, load and autoload buttons.
	 */
	protected void createButtons() {
		// create loadButton
    String imageFile = "/org/opensourcephysics/resources/tools/images/open.gif"; //$NON-NLS-1$
    Icon openIcon  = ResourceLoader.getIcon(imageFile);
		loadButton = new JButton(openIcon);
		loadButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		    JFileChooser chooser = OSPRuntime.createChooser(
		    		TrackerRes.getString("TrackerPanel.DataBuilder.Load.Title"),  //$NON-NLS-1$
		    		TrackerRes.getString("TrackerPanel.DataBuilder.Chooser.XMLFiles"),  //$NON-NLS-1$
		    		new String[] {"xml"}); //$NON-NLS-1$
		    int result = chooser.showOpenDialog(TrackDataBuilder.this);
		    if(result==JFileChooser.APPROVE_OPTION) {
		      OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
		      String fileName = chooser.getSelectedFile().getAbsolutePath();
		      XMLControl control = new XMLControlElement(fileName);
		      if (control.failedToRead()) {
		        JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
		            TrackerRes.getString("Tracker.Dialog.Invalid.Message"), //$NON-NLS-1$
		        		TrackerRes.getString("Tracker.Dialog.Invalid.Title"), //$NON-NLS-1$
		        		JOptionPane.ERROR_MESSAGE);
		        return;
		      }
		      
		      Class<?> type = control.getObjectClass();
		      if (DataFunctionPanel.class.isAssignableFrom(type)) {
		      	// determine what track type the control is for
		      	FunctionPanel dataPanel = getSelectedPanel();
		      	Class<?> panelType = null;
		      	Class<?> controlType = null;
		      	try {
							panelType = Class.forName(dataPanel.getDescription());
		      		controlType = Class.forName(control.getString("description")); //$NON-NLS-1$);
						} catch (ClassNotFoundException ex) {
						}
		      	String trackType = TrackerRes.getString("TrackerPanel.DataBuilder.TrackType.Unknown"); //$NON-NLS-1$
		      	if (controlType!=null)
		      		trackType = TrackerRes.getString(controlType.getSimpleName()+".Name").toLowerCase(); //$NON-NLS-1$
		      	
		      	if (controlType!=panelType) {
		        	String targetType = TrackerRes.getString(panelType.getSimpleName()+".Name").toLowerCase(); //$NON-NLS-1$
		          JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
		              TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongTrackType.Message1")+" \""+trackType+".\"" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		              +"\n"+TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongTrackType.Message2")+" \""+targetType+".\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		          		TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongTrackType.Title"), //$NON-NLS-1$
		          		JOptionPane.ERROR_MESSAGE);            	
		      		return;
		      	}
		      	
		      	if (choosePanelDataFunctions(control, "Load", null)) { //$NON-NLS-1$
		      		// load data function panel(s)
		      		ArrayList<FunctionPanel> panelsToLoad = new ArrayList<FunctionPanel>();
		          for (String name: getPanelNames()) {
		          	FunctionPanel nextPanel = getPanel(name);
		          	try {
									panelType = Class.forName(nextPanel.getDescription());
								} catch (ClassNotFoundException ex) {
								}
		          	if (panelType==controlType) {
		            	panelsToLoad.add(nextPanel);
		          	}
		          }
		      		
		      		result = 1; // default if only one track to load
		      		if (panelsToLoad.size()>1) {
		        		Object[] options = new String[] {
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.All"),  //$NON-NLS-1$
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.Only")+" "+dataPanel.getName(),  //$NON-NLS-1$ //$NON-NLS-2$
		        				TrackerRes.getString("Dialog.Button.Cancel")}; //$NON-NLS-1$
		        		result = JOptionPane.showOptionDialog(TrackDataBuilder.this, 
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Message")+" \""+trackType+"\"?",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Title"),  //$NON-NLS-1$
		        				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		      		}
		      		if (result==0) {
		            for (FunctionPanel nextPanel: panelsToLoad) {
		              control.loadObject(nextPanel);
		            }
		      		}
		      		else if (result==1) {
		          	control.loadObject(dataPanel);            			
		      		}
		        }
		      }
		      else if (TrackDataBuilder.class.isAssignableFrom(type)) {
		      	// determine what the selected panel track type is
		      	FunctionPanel dataPanel = getSelectedPanel();
		      	String panelTrackType = dataPanel.getDescription();

		      	// find the first DataFunctionPanel with same track type
			    	XMLControl targetControl = null;
		        outerLoop: for (Object next: control.getPropertyContent()) {
		        	if (next instanceof XMLProperty 
		        			&& ((XMLProperty)next).getPropertyName().equals("functions")) { //$NON-NLS-1$
		        		// found DataFunctionPanels
		        		XMLControl[] panels = ((XMLProperty)next).getChildControls();
		        		for (XMLControl panelControl: panels) {
		        			String trackType = panelControl.getString("description"); //$NON-NLS-1$
		        			if (trackType==null || panelTrackType==null || !panelTrackType.equals(trackType)) {
		        				// wrong track type
		        				continue;
		        			}
		        			// found right panel
	        				targetControl = panelControl;
	        				break outerLoop;        					
		        		}
		        	}
		        } // end outerLoop
		      	
		      	String trackType = TrackerRes.getString("TrackerPanel.DataBuilder.TrackType.Unknown"); //$NON-NLS-1$
		      	if (panelTrackType!=null)
		      		trackType = TrackerRes.getString(XML.getExtension(panelTrackType)+".Name").toLowerCase(); //$NON-NLS-1$
		      	
		      	if (targetControl==null) {
		          JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
		              TrackerRes.getString("TrackDataBuilder.Dialog.NoFunctionsFound.Message")+" \""+trackType+".\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		          		TrackerRes.getString("TrackDataBuilder.Dialog.NoFunctionsFound.Title"), //$NON-NLS-1$
		          		JOptionPane.ERROR_MESSAGE);            	
		      		return;
		      	}
		      	
		      	
		      	if (choosePanelDataFunctions(targetControl, "Load", null)) { //$NON-NLS-1$
		      		// load data function panel(s)
		      		ArrayList<FunctionPanel> panelsToLoad = new ArrayList<FunctionPanel>();
		          for (String name: getPanelNames()) {
		          	FunctionPanel nextPanel = getPanel(name);
		          	if (panelTrackType.equalsIgnoreCase(nextPanel.getDescription())) {
		            	panelsToLoad.add(nextPanel);
		          	}
		          }
		      		
		      		result = 1; // default if only one track to load
		      		if (panelsToLoad.size()>1) {
		        		Object[] options = new String[] {
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.All"),  //$NON-NLS-1$
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Button.Only")+" "+dataPanel.getName(),  //$NON-NLS-1$ //$NON-NLS-2$
		        				TrackerRes.getString("Dialog.Button.Cancel")}; //$NON-NLS-1$
		        		result = JOptionPane.showOptionDialog(TrackDataBuilder.this, 
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Message")+" \""+trackType+"\"?",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		        				TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.Load.Title"),  //$NON-NLS-1$
		        				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		      		}
		      		if (result==0) {
		            for (FunctionPanel nextPanel: panelsToLoad) {
		            	targetControl.loadObject(nextPanel);
		            }
		      		}
		      		else if (result==1) {
		      			targetControl.loadObject(dataPanel);            			
		      		}
		        }
		      }
		  		else {
		        JOptionPane.showMessageDialog(trackerPanel.getTFrame(), 
		            TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongType.Message"), //$NON-NLS-1$
		        		TrackerRes.getString("TrackerPanel.DataBuilder.Dialog.WrongType.Title"), //$NON-NLS-1$
		        		JOptionPane.ERROR_MESSAGE);
		  		}
		
		    }
		  }
		
		});

		// create saveButton
    imageFile = "/org/opensourcephysics/resources/tools/images/save.gif"; //$NON-NLS-1$
    Icon saveIcon = ResourceLoader.getIcon(imageFile);
		saveButton = new JButton(saveIcon);
		
		final ActionListener saveBuilderAction = new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		  	XMLControl control = new XMLControlElement(TrackDataBuilder.this);
		   	if (chooseBuilderDataFunctions(control, "Save", null)) { //$NON-NLS-1$
		      JFileChooser chooser = OSPRuntime.createChooser(
		      		TrackerRes.getString("TrackerPanel.DataBuilder.Save.Title"),  //$NON-NLS-1$
		      		TrackerRes.getString("TrackerPanel.DataBuilder.Chooser.XMLFiles"),  //$NON-NLS-1$
		      		new String[] {"xml"}); //$NON-NLS-1$
		      int result = chooser.showSaveDialog(TrackDataBuilder.this);
		      if (result==JFileChooser.APPROVE_OPTION) {
		        OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
		        File file = chooser.getSelectedFile();
		        String fileName = file.getAbsolutePath();
		        if (!"xml".equals(XML.getExtension(fileName))) { //$NON-NLS-1$
		        	fileName = XML.stripExtension(fileName)+".xml"; //$NON-NLS-1$
		        	file = new File(fileName);
		        }
		        if(!TrackerIO.canWrite(file)) {
		        	return;
		        }
		        control.write(fileName);
		      }
		    }
		  }
		
		};
		final ActionListener savePanelAction = new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
      	XMLControl control = new XMLControlElement(getSelectedPanel());
       	if (choosePanelDataFunctions(control, "Save", null)) { //$NON-NLS-1$
          JFileChooser chooser = OSPRuntime.createChooser(
          		TrackerRes.getString("TrackerPanel.DataBuilder.Save.Title"),  //$NON-NLS-1$
          		TrackerRes.getString("TrackerPanel.DataBuilder.Chooser.XMLFiles"),  //$NON-NLS-1$
          		new String[] {"xml"}); //$NON-NLS-1$
          int result = chooser.showSaveDialog(TrackDataBuilder.this);
          if (result==JFileChooser.APPROVE_OPTION) {
            OSPRuntime.chooserDir = chooser.getCurrentDirectory().toString();
		        File file = chooser.getSelectedFile();
		        String fileName = file.getAbsolutePath();
		        if (!"xml".equals(XML.getExtension(fileName))) { //$NON-NLS-1$
		        	fileName = XML.stripExtension(fileName)+".xml"; //$NON-NLS-1$
		        	file = new File(fileName);
		        }
		        if(!TrackerIO.canWrite(file)) {
		        	return;
		        }
		        control.write(fileName);
          }
        }
		  }
		
		};
	  saveButton.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item = new JMenuItem(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveAll.Text"));  //$NON-NLS-1$
        item.setToolTipText(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveAll.Tooltip")); //$NON-NLS-1$
        popup.add(item);
        item.addActionListener(saveBuilderAction);
        
        String s = " "+getSelectedPanel().getName(); //$NON-NLS-1$
        item = new JMenuItem(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveOnly.Text")+s);  //$NON-NLS-1$
        item.setToolTipText(TrackerRes.getString("TrackDataBuilder.MenuItem.SaveOnly.Tooltip")); //$NON-NLS-1$
        popup.add(item);
        item.addActionListener(savePanelAction);

        FontSizer.setFonts(popup, FontSizer.getLevel());
        popup.show(saveButton, 0, saveButton.getHeight());       		
		  }
	  });

		// create autoloadButton
		autoloadButton = new JButton();
	  autoloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	      getAutoloadManager().refreshAutoloadData();
	      getAutoloadManager().setVisible(true);	
	    }
	
	  });
	}
	
	/**
	 * Refreshes the GUI.
	 */
  protected void refreshGUI() {
  	super.refreshGUI();
  	dropdown.setToolTipText(TrackerRes.getString("TrackerPanel.DataBuilder.Dropdown.Tooltip")); //$NON-NLS-1$
		setTitle(TrackerRes.getString("TrackerPanel.DataBuilder.Title")); //$NON-NLS-1$
		if (loadButton!=null) {
			FunctionPanel panel = getSelectedPanel();
			loadButton.setEnabled(panel!=null);
			saveButton.setEnabled(panel!=null);
			loadButton.setToolTipText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Load.Tooltip")); //$NON-NLS-1$
			saveButton.setToolTipText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Save.Tooltip")); //$NON-NLS-1$
			autoloadButton.setText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Autoload")+"..."); //$NON-NLS-1$ //$NON-NLS-2$
			autoloadButton.setToolTipText(TrackerRes.getString("TrackerPanel.DataBuilder.Button.Autoload.Tooltip")); //$NON-NLS-1$
		}
		if (autoloadManager!=null) {
			autoloadManager.refreshGUI();
		}
	}
  		
	/**
	 * Creates a new DataFunctionPanel for a track, autoloading appropriate data functions.
	 * 
	 * @param track the track
	 */
  protected FunctionPanel createFunctionPanel(TTrack track) {
    FunctionPanel panel = new DataFunctionPanel(track.getData(trackerPanel));
  	panel.setIcon(track.getFootprint().getIcon(21, 16));
  	Class<?> type = track.getClass();
  	if (PointMass.class.isAssignableFrom(type))
  		panel.setDescription(PointMass.class.getName());
  	else if (Vector.class.isAssignableFrom(type))
  		panel.setDescription(Vector.class.getName());
  	else if (RGBRegion.class.isAssignableFrom(type))
  		panel.setDescription(RGBRegion.class.getName());
  	else if (LineProfile.class.isAssignableFrom(type))
  		panel.setDescription(LineProfile.class.getName());
  	else panel.setDescription(type.getName());
    final ParamEditor paramEditor = panel.getParamEditor();
    if (track instanceof PointMass) {
    	final PointMass m = (PointMass)track;
	  	Parameter param = (Parameter)paramEditor.getObject("m"); //$NON-NLS-1$
	  	if (param==null) {
	  		param = new Parameter("m", String.valueOf(m.getMass())); //$NON-NLS-1$
	  		param.setDescription(TrackerRes.getString("ParticleModel.Parameter.Mass.Description")); //$NON-NLS-1$
	      paramEditor.addObject(param, false);
	  	}
  		param.setNameEditable(false); // mass name not editable
      paramEditor.addPropertyChangeListener(new PropertyChangeListener() {
  		  public void propertyChange(PropertyChangeEvent e) {
  		  	if ("m".equals(e.getOldValue())) { //$NON-NLS-1$
  		  		Parameter param = (Parameter)paramEditor.getObject("m"); //$NON-NLS-1$
  		  		if (m.getMass() != param.getValue()) {
  		      	m.setMass(param.getValue());
  		      	m.massField.setValue(m.getMass());
  		      }
  		  	}
  		  }
  		});
      m.addPropertyChangeListener("mass", new PropertyChangeListener() { //$NON-NLS-1$
  		  public void propertyChange(PropertyChangeEvent e) {
  		  	Parameter param = (Parameter)paramEditor.getObject("m"); //$NON-NLS-1$
  		  	double newMass = (Double)e.getNewValue();
  		    if (newMass != param.getValue()) {
  		    	paramEditor.setExpression("m", String.valueOf(newMass), false); //$NON-NLS-1$
  		  	}
  		  }
  		});
    }
    return panel;
  }

  /**
   * Adds a FunctionPanel.
   *
   * @param name a descriptive name
   * @param panel the FunctionPanel
   * @return the added panel
   */
  @Override
  public FunctionPanel addPanel(String name, FunctionPanel panel) {
    super.addPanel(name, panel);

    // load default data functions, if any, for this track type
  	Class<?> trackType = null;
  	try {
			trackType = Class.forName(panel.getDescription());
			} catch (ClassNotFoundException ex) {
		}
  	// load from Strings read from tracker.prefs (deprecated Dec 2014)
    for (String xml: Tracker.dataFunctionControlStrings) {
    	XMLControl control = new XMLControlElement(xml);
	  	// determine what track type the control is for
	  	Class<?> controlTrackType = null;
	  	try {
	  		controlTrackType = Class.forName(control.getString("description")); //$NON-NLS-1$);
			} catch (Exception ex) {
			}
	  	
	  	if (controlTrackType==trackType) {
	  		control.loadObject(panel);            			
	  	}	  	
    }	
    
    // load from XMLControls autoloaded from XML files in user home and code base
    for (XMLControl control: Tracker.dataFunctionControls) {
	  	// determine what track type the control is for
	  	Class<?> controlTrackType = null;
	  	try {
	  		controlTrackType = Class.forName(control.getString("description")); //$NON-NLS-1$);
			} catch (Exception ex) {
			}
	  	
	  	if (controlTrackType==trackType) {
	  		// copy the control for modification if any functions are autoload_off
	  		XMLControl copyControl = new XMLControlElement(control);
	  		eliminateUnwantedFunctions(copyControl);
	  		// change duplicate function names without requiring user confirmation
	  		FunctionEditor editor = panel.getFunctionEditor();
	  		boolean confirmChanges = editor.getConfirmChanges();
	  		editor.setConfirmChanges(false);
	  		copyControl.loadObject(panel);            			
	  		editor.setConfirmChanges(confirmChanges);
	  	}	  	
    }
    
    return panel;
  }
  
  /**
   * Chooses data functions from a DataFunctionPanel XMLControl.
   *
   * @param control the XMLControl
   * @param description "Save" or "Load"
   * @param selectedFunctions collection of DataFunction choices
   * @return true if user clicked OK
   */
	protected boolean choosePanelDataFunctions(XMLControl control, String description, 
			Collection<String[]> selectedFunctions) {
	  ListChooser listChooser = new ListChooser(
	  		TrackerRes.getString("TrackerPanel.DataBuilder."+description+".Title"), //$NON-NLS-1$ //$NON-NLS-2$
	  		TrackerRes.getString("TrackerPanel.DataBuilder."+description+".Message"), //$NON-NLS-1$ //$NON-NLS-2$
	      this);
    listChooser.setSeparator(" = "); //$NON-NLS-1$
	  // choose the elements and save
	  ArrayList<Object> originals = new ArrayList<Object>();
	  ArrayList<Object> choices = new ArrayList<Object>();
	  ArrayList<String> names = new ArrayList<String>();
	  ArrayList<String> expressions = new ArrayList<String>();
	  ArrayList<?> functions = (ArrayList<?>)control.getObject("functions"); //$NON-NLS-1$
	  
	  for (Object next: functions) {
	  	String[] function = (String[])next;          	
			originals.add(function);
			choices.add(function);
			names.add(function[0]);
			expressions.add(function[1]);
	  }
	  // select all by default
	  boolean[] selected = new boolean[choices.size()];
	  for (int i = 0; i<selected.length; i++) {
	  	selected[i] = true;
	  }
	  if (listChooser.choose(choices, names, expressions, selected)) {
	    // compare choices with originals and remove unwanted object content
	    for (Object next: originals) {
	      if (!choices.contains(next)) {
	        functions.remove(next);
	      }
	    }
	    // rewrite the control with only selected functions
	    control.setValue("functions", functions); //$NON-NLS-1$
	    return true;
	  }
	  return false;
	}
	
  /**
   * Chooses data functions from a DataBuilder XMLControl.
   *
   * @param control the XMLControl
   * @param description "Save" or "Load"
   * @param selectedFunctions collection of DataFunction choices
   * @return true if user clicked OK
   */
  @SuppressWarnings("unchecked")
  protected boolean chooseBuilderDataFunctions(XMLControl control, String description, Collection<String[]> selectedFunctions) {
    ListChooser listChooser = new ListChooser(
    		TrackerRes.getString("TrackerPanel.DataBuilder."+description+".Title"), //$NON-NLS-1$ //$NON-NLS-2$
    		TrackerRes.getString("TrackerPanel.DataBuilder."+description+".Message"), //$NON-NLS-1$ //$NON-NLS-2$
        this);
    listChooser.setSeparator(" = "); //$NON-NLS-1$
    // choose the elements and save
    ArrayList<String[]> originals = new ArrayList<String[]>();
    ArrayList<String[]> choices = new ArrayList<String[]>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> expressions = new ArrayList<String>();
    ArrayList<String> trackTypes = new ArrayList<String>();
    	    
    Map<String, XMLControl> xmlControlMap = new TreeMap<String, XMLControl>();
    Map<String, ArrayList<Parameter>> parameterMap = new TreeMap<String, ArrayList<Parameter>>();
    Map<String, ArrayList<String[]>> functionMap = new TreeMap<String, ArrayList<String[]>>();
    for (Object obj: control.getPropertyContent()) {
    	if (obj instanceof XMLProperty) {
    		XMLProperty prop = (XMLProperty)obj;
    		for (XMLControl xmlControl: prop.getChildControls()) {
    			if (xmlControl.getObjectClass()!=DataFunctionPanel.class) continue;
    			
    			// get track type (description) and map to panel xmlControl
    			String trackType = xmlControl.getString("description"); //$NON-NLS-1$
    			xmlControlMap.put(trackType, xmlControl);
    			
    			// get the list of functions for this track type
    			ArrayList<String[]> functions = functionMap.get(trackType);
    	    if (functions==null) {
    	    	functions = new ArrayList<String[]>();
    	    	functionMap.put(trackType, functions);
    	    }
    	    // add functions found in this xmlControl unless already present
    	    ArrayList<String[]> panelFunctions = (ArrayList<String[]>)xmlControl.getObject("functions"); //$NON-NLS-1$
    	    outer: for (String[] f: panelFunctions) {
    	    	// check for duplicate function names
    	    	for (String[] existing: functions) {
    	    		if (existing[0].equals(f[0])) continue outer;
    	    	}
    	    	functions.add(f);
    	    }
    	    
    			// get the list of parameters for this track type
    			ArrayList<Parameter> params = parameterMap.get(trackType);
    	    if (params==null) {
    	    	params = new ArrayList<Parameter>();
    	    	parameterMap.put(trackType, params);
    	    }
    	    // add parameters found in this xmlControl unless already present
    	    Parameter[] panelParams = (Parameter[])xmlControl.getObject("user_parameters"); //$NON-NLS-1$
    	    outer: for (Parameter p: panelParams) {
    	    	if (trackType.endsWith("PointMass") && p.getName().equals("m")) { //$NON-NLS-1$ //$NON-NLS-2$
    	    		continue outer;
    	    	}
    	    	// check for duplicate parameter names
    	    	for (Parameter existing: params) {
    	    		if (existing.getName().equals(p.getName())) continue outer;
    	    	}
    	    	params.add(p);
    	    }
    		}
    	}
    }
    
    for (String trackType: functionMap.keySet()) {
    	ArrayList<String[]> functions = functionMap.get(trackType);
	    for (String[] f: functions) {
	  		originals.add(f);
	  		choices.add(f);
	  		names.add(f[0]);
	  		expressions.add(f[1]);
	  		String shortName = XML.getExtension(trackType);
	  		String localized = TrackerRes.getString(shortName+".Name"); //$NON-NLS-1$
	  		if (!localized.startsWith("!")) shortName = localized; //$NON-NLS-1$
	  		trackTypes.add("["+shortName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
	    }
    }
    // select all by default
    boolean[] selected = new boolean[choices.size()];
    for (int i = 0; i<selected.length; i++) {
    	selected[i] = true;
    }
    
    if (listChooser.choose(choices, names, expressions, trackTypes, selected)) {
      // compare choices with originals and remove unwanted object content
      for (String[] function: originals) {
        if (!choices.contains(function)) {
          for (String trackType: xmlControlMap.keySet()) {
          	ArrayList<String[]> functions = functionMap.get(trackType);
          	functions.remove(function);
          }
        }
      }
      // set functions in xmlControl for each trackType
      for (String trackType: xmlControlMap.keySet()) {
      	ArrayList<String[]> functions = functionMap.get(trackType);
      	ArrayList<Parameter> paramList = parameterMap.get(trackType);
      	Parameter[] params = paramList.toArray(new Parameter[paramList.size()]);
      	XMLControl xmlControl = xmlControlMap.get(trackType);
      	xmlControl.setValue("functions", functions); //$NON-NLS-1$
      	xmlControl.setValue("user_parameters", params); //$NON-NLS-1$
      }
      
      // keep only xmlControls that have functions and are in xmlControlMap
      for (Object next: control.getPropertyContent()) {
      	if (next instanceof XMLProperty && ((XMLProperty)next).getPropertyName().equals("functions")) { //$NON-NLS-1$
      		XMLProperty panels = (XMLProperty)next;
          java.util.List<Object> content = panels.getPropertyContent();
	        ArrayList<Object> toRemove = new ArrayList<Object>();
      		for (Object child: content) {
      			XMLControl xmlControl = ((XMLProperty)child).getChildControls()[0];
      			if (!xmlControlMap.values().contains(xmlControl)) {
      				toRemove.add(child);
      			}
      			else { // check to see if functions is empty
        			ArrayList<String[]> functions = (ArrayList<String[]>)xmlControl.getObject("functions"); //$NON-NLS-1$
      				if (functions==null || functions.isEmpty()) {
      					toRemove.add(child);
      				}
      			}
      		}
      		for (Object remove: toRemove) {
      			content.remove(remove);
      		}
      	}
      }       
      
      return true;
    }
    return false;
  }
  
	/**
	 * Eliminates unwanted function entries from a DataFunctionPanel XMLControl. 
	 * Typical (but incomplete) control:
	 * 
	 *	<object class="org.opensourcephysics.tools.DataFunctionPanel">
	 *	   <property name="description" type="string">org.opensourcephysics.cabrillo.tracker.PointMass</property>
	 *	   <property name="functions" type="collection" class="java.util.ArrayList">
	 *	       <property name="item" type="array" class="[Ljava.lang.String;">
	 *	           <property name="[0]" type="string">Ug</property>
	 *	           <property name="[1]" type="string">m*g*y</property>
	 *	        </property>
	 *	   </property>
	 *		 <property name="autoload_off_Ug" type="boolean">true</property>
	 *	</object>
	 *
	 * @param panelControl the XMLControl to modify
	 */
	protected void eliminateUnwantedFunctions(XMLControl panelControl) {
    for (Object prop: panelControl.getPropertyContent()) {
    	if (prop instanceof XMLProperty 
    			&& ((XMLProperty)prop).getPropertyName().equals("functions")) { //$NON-NLS-1$
    		// found functions
    		XMLProperty functions = (XMLProperty)prop;
        java.util.List<Object> items = functions.getPropertyContent();
        ArrayList<XMLProperty> toRemove = new ArrayList<XMLProperty>();
    		for (Object child: items) {
    			XMLProperty item = (XMLProperty)child;
    			XMLProperty nameProp = (XMLProperty)item.getPropertyContent().get(0);
    			String functionName = (String)nameProp.getPropertyContent().get(0);
    			if (panelControl.getBoolean("autoload_off_"+functionName)) { //$NON-NLS-1$
    				toRemove.add(item);
    			}
    		}
    		for (XMLProperty next: toRemove) {
    			items.remove(next);
    		}            	        		
    	}
    }
	}
	
	/**
	 * Gets the autoload manager, creating it the first time called.
	 * 
	 * @return the autoload manageer
	 */
  protected AutoloadManager getAutoloadManager() {
  	if (autoloadManager==null) {
  		autoloadManager = new AutoloadManager(this);
  		// center on screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      int x = (dim.width - autoloadManager.getBounds().width) / 2;
      int y = (dim.height - autoloadManager.getBounds().height) / 2;
      autoloadManager.setLocation(x, y);
      
      if (!Tracker.dataFunctionControlStrings.isEmpty()) {
    		final String userhome = System.getProperty("user.home"); //$NON-NLS-1$
    		if (userhome!=null) {
	      	Runnable runner = new Runnable() {
	      		public void run() {
			      	int response = JOptionPane.showConfirmDialog(TrackDataBuilder.this, 
			      			TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Message1") //$NON-NLS-1$
			      			+ "\n"+TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Message2") //$NON-NLS-1$ //$NON-NLS-2$
			      			+ "\n\n"+TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Message3"),  //$NON-NLS-1$ //$NON-NLS-2$
			      			TrackerRes.getString("TrackDataBuilder.Dialog.ConvertAutoload.Title"),  //$NON-NLS-1$
			      			JOptionPane.YES_NO_OPTION);
			      	if (response==JOptionPane.YES_OPTION) {		      		
			      		TrackDataBuilder builder = new TrackDataBuilder(new TrackerPanel());
			      		int i = 0;
			      		for (String next: Tracker.dataFunctionControlStrings) {
			      			XMLControl panelControl = new XMLControlElement(next);
			      			DataFunctionPanel panel = new DataFunctionPanel(new DatasetManager());
			      			panelControl.loadObject(panel);
			      			builder.addPanelWithoutAutoloading("panel"+i, panel); //$NON-NLS-1$
			      			i++;
			      		}
			      		File file = new File(userhome, "TrackerConvertedAutoloadFunctions.xml"); //$NON-NLS-1$
			      		XMLControl control = new XMLControlElement(builder);
			      		control.write(file.getAbsolutePath());
			      		Tracker.dataFunctionControlStrings.clear();
			      		autoloadManager.refreshAutoloadData();
			      	}
	      			
	      		}
	      	};
	      	SwingUtilities.invokeLater(runner);
    		}
      }      
  	}
  	return autoloadManager;
  }
  
  /**
   * Adds a FunctionPanel without autoloading any data functions.
   *
   * @param name a descriptive name
   * @param panel the FunctionPanel
   */
  protected void addPanelWithoutAutoloading(String name, FunctionPanel panel) {
    super.addPanel(name, panel);
  }
    
    
  /**
	 * A FunctionAutoloadManager for DataFunctions.
	 */
  class AutoloadManager extends FunctionAutoloadManager {
  	
  	/**
  	 * Constructor for a dialog.
  	 * 
  	 * @param dialog the dialog (DataBuilder)
  	 */
  	public AutoloadManager(JDialog dialog) {
			super(dialog);
		}
  	
  	/**
  	 * Refreshes the GUI.
  	 */
  	protected void refreshGUI() {
  		refreshAutoloadData();
  		super.refreshGUI();
  		String title = TrackDataBuilder.this.getTitle()+" "+getTitle(); //$NON-NLS-1$
			setTitle(title);		
  		setInstructions(TrackerRes.getString("TrackDataBuilder.Instructions.SelectToAutoload") //$NON-NLS-1$
  				+ "\n\n"+TrackerRes.getString("TrackDataBuilder.Instructions.WhereDefined") //$NON-NLS-1$ //$NON-NLS-2$
  				+" "+TrackerRes.getString("TrackDataBuilder.Instructions.HowToAddFunction")); //$NON-NLS-1$ //$NON-NLS-2$
  	}
  	
  	/**
  	 * Gets a localized track name from the fully qualified track class name.
  	 * 
  	 * @param trackClass the class name
  	 * @return the localized name
  	 */
  	protected String getLocalizedTrackName(String trackClass) {
  		String trackName = XML.getExtension(trackClass);
			String localized = TrackerRes.getString(trackName+".Name"); //$NON-NLS-1$
			if (!localized.startsWith("!")) //$NON-NLS-1$
				trackName = localized;
			return trackName;
  	}

    /**
	   * Called when the status of a function has changed.
	   *
	   * @param id the function identifier {String directory, String filePath, Object[] function}
     */
  	@Override
    @SuppressWarnings("unchecked")
    protected void functionChanged(Object[] id) {
    	Object[] f = (Object[])id[2];
	    XMLControl control = new XMLControlElement((String)id[1]);    
	    if (control.failedToRead()) {
	      return;
	    }

	    Class<?> type = control.getObjectClass();
	    if (TrackDataBuilder.class.isAssignableFrom(type)) {
	    	// find target DataFunctionPanel XMLControl with the desired function
	    	XMLControl targetControl = null;
        outerLoop: for (Object next: control.getPropertyContent()) {
        	if (next instanceof XMLProperty 
        			&& ((XMLProperty)next).getPropertyName().equals("functions")) { //$NON-NLS-1$
        		// found DataFunctionPanels
        		XMLControl[] panels = ((XMLProperty)next).getChildControls();
        		for (XMLControl panelControl: panels) {
        			String trackType = panelControl.getString("description"); //$NON-NLS-1$
        			if (trackType==null || !f[3].equals(getLocalizedTrackName(trackType))) {
        				// wrong track type
        				continue;
        			}
        			ArrayList<String[]> functions = (ArrayList<String[]>)panelControl.getObject("functions"); //$NON-NLS-1$
        			for (String[] func: functions) {
        				if (func[0].equals(f[0])) {
	        				targetControl = panelControl;
	        				break outerLoop;        					
        				}
        			}
        		}
        	}
        } // end outerLoop
	    	
	    	// set autoload_off property for function name
	    	if (targetControl!=null) {
	    		boolean on = (Boolean)f[2];
	    		if (!on) {
	    			targetControl.setValue("autoload_off_"+f[0], !on); //$NON-NLS-1$
	    		}
	    		else {
	    			targetControl.setValue("autoload_off_"+f[0], null); //$NON-NLS-1$
	    		}
		    	control.write((String)id[1]);  
	    	}
	    }
	    Tracker.autoloadDataFunctions();
	    refreshAutoloadData();
	    // reload autoloaded functions into existing panels
	    for (String name: TrackDataBuilder.this.getPanelNames()) {
	    	DataFunctionPanel panel = (DataFunctionPanel)TrackDataBuilder.this.getPanel(name);
	    	addPanel(name, panel);
	    }
    }
  	
  	/**
  	 * Refreshes the autoload data.
  	 */
  	protected void refreshAutoloadData() {
      // display data functions, if any, in (a) user home and (b) code base
    	final Map<String, Map<String, ArrayList<Object[]>>> data 
    		= new TreeMap<String, Map<String, ArrayList<Object[]>>>();
      String userhome = System.getProperty("user.home"); //$NON-NLS-1$
      if (userhome!=null) {
        Map<String, ArrayList<Object[]>> functionMap = Tracker.findDataFunctions(userhome);
        data.put(userhome, functionMap);
      }
      String codebase = OSPRuntime.getLaunchJarDirectory();
      if (codebase!=null) {
      	Map<String, ArrayList<Object[]>> functionMap = Tracker.findDataFunctions(codebase);
        data.put(codebase, functionMap);
      }      
      setAutoloadData(data);
  	}
  }
  
}
