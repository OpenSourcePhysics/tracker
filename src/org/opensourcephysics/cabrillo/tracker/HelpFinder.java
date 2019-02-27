/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2019  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.Document;

import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.LaunchNode;
import org.opensourcephysics.tools.LaunchPanel;
import org.opensourcephysics.tools.Launcher;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.Launcher.HTMLPane;

/**
 * A class to search Tracker Help
 *
 * @author Douglas Brown
 */
public class HelpFinder {
	
	// map of pagekey to map of anchor to text lines
	private static Map<String, Map<String, ArrayList<String>>> pages 
			= new TreeMap<String, Map<String, ArrayList<String>>>();
	// map of pagekey to page title
	private static Map<String, String> pageNames = new TreeMap<String, String>();
	// map of anchor to section title
	private static Map<String, String> anchorNames = new TreeMap<String, String>();
	// map of pagekey to html path
	private static Map<String, String> pagePaths = new TreeMap<String, String>();
	// initial context phrase extent before/after the search phrase
	private static int contextPhraseLength = 120;
	// maximum trim taken from context phrase to render more readable
	private static int contextPhraseTrim = 15;
	// minimum length of search phrase to trigger search
	private static int minimumSearchPhraseLength = 3;
	// color shown for search terms not found
	private static Color _RED = new Color(255, 160, 180);
	// help launcher elements
	private static Launcher helpLauncher;
	private static LaunchPanel searchResultsTab;
	private static LaunchNode rootNode;
	// GUI components
	private static JTextField searchField  = new JTextField(12);
	private static JLabel searchLabel;
	private static JButton clearSearchButton;
  private static String css;
	
	static {
		initialize();		
	}
	
  /**
   * Searches for a phrase and returns a list of results.
   * Each result is a String[] {page title & subtitle, context phrase, keywords}.
   * 
   * @param searchPhrase the phrase to search for
   * @param termsFound a list to which terms found are added
   * @return list of results 
   */
	protected static ArrayList<String[]> search(String searchPhrase, ArrayList<String> termsFound) {
		// search with entire phrase
		ArrayList<String[]> results = search(searchPhrase);
		if (results.size()>0) {
			termsFound.add(searchPhrase);
			return results;
		}
		// search with individual terms
		String[] terms = searchPhrase.split(" "); //$NON-NLS-1$
		// no use continuing if only one term in the search phrase
		if (terms.length<2) return results;
		
		ArrayList<ArrayList<String[]>> allResults = new ArrayList<ArrayList<String[]>>();
		ArrayList<String[]> termResults = new ArrayList<String[]>();
		// get results for each term independently
		for (String term: terms) {
			termResults = search(term);
			allResults.add(termResults);
		}
		// go through the last set of results and look for keywords that are found in all (AND mode)
		ArrayList<String> contexts = new ArrayList<String>();
		outer: for (String[] next: termResults) {
			contexts.clear();
			String keyword = next[2];
			for (ArrayList<String[]> nextResults: allResults) {
				boolean found = false;
				inner: for (String[] result: nextResults) {
					if (keyword.equals(result[2])) {
						found = true;
						contexts.add(result[1]);
						break inner;
					}
				}
				if (!found) continue outer;
			}
			// keyword was found in all so merge contexts and add to results
			String context = getMergedContext(contexts);
			results.add(new String[] {next[0], context, next[2]});
		}
		if (results.size()>0) {
			for (String term: terms) {
				termsFound.add(term);
			}
		}
		return results;
	}
	
	private static String getMergedContext(ArrayList<String> contexts) {
		ArrayList<String> phrases = new ArrayList<String>();
		ArrayList<String> cleanStarts = new ArrayList<String>();
		ArrayList<String> cleanEnds = new ArrayList<String>();
		
		for (String next: contexts) {
			if (!next.startsWith("...")) { //$NON-NLS-1$
				cleanStarts.add(next.substring(0, Math.min(14, next.length())));
			}
			if (!next.endsWith("...")) { //$NON-NLS-1$
				cleanEnds.add(next.substring(Math.max(0, next.length()-14)));
			}
			next = next.replaceAll("\\.\\.\\.", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (phrases.isEmpty()) {
				phrases.add(next);
				continue;
			}
			boolean merged = false;
			// check each phrase to see if one is substring of other
			ArrayList<String> testing = new ArrayList<String>(phrases);
			for (String context: testing) {
				if (context.contains(next)) {
					merged = true;
					break;
				}
				if (next.contains(context)) {
					phrases.remove(context);
					phrases.add(next);
					merged = true;
					break;
				}
			}
			if (!merged && next.length()>15) {
				String toMatch = next.substring(0, 15);
				for (String context: testing) {
  				int n = context.indexOf(toMatch);
  				if (n>0) {
  					phrases.remove(context);
  					context = context.substring(0, n) + next;
  					phrases.add(context);
  					merged = true;
  					break;
  				}
				}
			
				if (!merged) {
					int len = next.length();
					toMatch = next.substring(len-15, len); 
  				for (String context: testing) {
  					int n = context.indexOf(toMatch);
    				if (n>0) {
    					phrases.remove(context);
    					context = next+context.substring(n+15);
    					phrases.add(context);
    					merged = true;
    					break;
    				}
  				}
				}
			}
			if (!merged) {
				phrases.add(next);
			}
		}
		// assemble final context
		String context = ""; //$NON-NLS-1$
		boolean addEllipsis = true;
		for (String next: phrases) {
			if  (!addEllipsis) context += "\" | \""; //$NON-NLS-1$
			addEllipsis = true;
			for (String cleanStart: cleanStarts) {
				if (next.startsWith(cleanStart)) addEllipsis = false;
			}
			if  (addEllipsis) context += "..."; //$NON-NLS-1$
			context += next;
			addEllipsis = true;
			for (String cleanEnd: cleanEnds) {
				if (next.endsWith(cleanEnd)) addEllipsis = false;
			}
		}
		if  (addEllipsis) context += "..."; //$NON-NLS-1$
		return context.trim();
	}
	
	protected static Component[] getNavComponentsFor(Launcher launcher) {
		helpLauncher = launcher;
		searchLabel.setText(TrackerRes.getString("HelpFinder.Label.SearchFor.Text")+":"); //$NON-NLS-1$ //$NON-NLS-2$
		searchLabel.setToolTipText(TrackerRes.getString("HelpFinder.Label.SearchFor.Tooltip")); //$NON-NLS-1$
		searchField.setToolTipText(TrackerRes.getString("HelpFinder.Label.SearchFor.Tooltip")); //$NON-NLS-1$
		clearSearchButton.setText(TrackerRes.getString("HelpFinder.Button.ClearResults.Text")); //$NON-NLS-1$
		clearSearchButton.setToolTipText(TrackerRes.getString("HelpFinder.Button.ClearResults.Tooltip")); //$NON-NLS-1$
		return new Component[] {searchLabel, searchField, Box.createHorizontalStrut(4), clearSearchButton, TToolBar.getSeparator()};
	}
	
  /**
   * Searches for a phrase and returns a list of results.
   * Each result is a String[] {page title & subtitle, context phrase, keywords}.
   * 
   * @param searchPhrase the phrase to search for
   * @return list of results 
   */
	private static ArrayList<String[]> search(String searchPhrase) {
		searchPhrase = searchPhrase.toLowerCase();
		ArrayList<String> keywordsFound = new ArrayList<String>();
		ArrayList<String[]> results = new ArrayList<String[]>();
		// search page titles
		for (String pageKey: pageNames.keySet()) {
			String pageTitle = pageNames.get(pageKey);
			if (pageTitle.toLowerCase().contains(searchPhrase)) {
				// found pagekey, so get the anchor and context
				inner: for (String anchor: anchorNames.keySet()) {
					String sectionTitle = anchorNames.get(anchor);
					if (sectionTitle!=null && sectionTitle.equals(pageTitle)) {
						// found the anchor so get the context: 1st line of text, if any
						Map<String, ArrayList<String>> anchors = pages.get(pageKey);
						ArrayList<String> lines = anchors.get(anchor);
						String line = (lines!=null && lines.size()>0)? lines.get(0): pageTitle;
						String context = getContextPhrase(line, searchPhrase);
						String keyword = pageKey+"#"+anchor; //$NON-NLS-1$
						String fullPath = pagePaths.get(pageKey)+"#"+anchor; //$NON-NLS-1$
						String[] result = new String[] {pageTitle, context, fullPath};
						results.add(result);
						keywordsFound.add(keyword);
						break inner;
					}
				}
			}
		}
		// search section titles
		for (String anchor: anchorNames.keySet()) {
			String section = anchorNames.get(anchor);
			if (section!=null && section.toLowerCase().contains(searchPhrase)) {
				// found the anchor, so get the pagekey and context
				for (String pageKey: pages.keySet()) {
					Map<String, ArrayList<String>> anchors = pages.get(pageKey);
					for (String next: anchors.keySet()) {
						if (next.equals(anchor)) {
							// found the pageKey for the anchor
							String keyword = pageKey+"#"+anchor; //$NON-NLS-1$
							if (!keywordsFound.contains(keyword)) { 
								// get the context: 1st line of text, if any
								String pageName = pageNames.get(pageKey);
								ArrayList<String> lines = anchors.get(anchor);
								String line = (lines!=null && lines.size()>0)? lines.get(0): pageName;
								String context = getContextPhrase(line, searchPhrase);
								String name = pageName+": "+section; //$NON-NLS-1$								
								String fullPath = pagePaths.get(pageKey)+"#"+anchor; //$NON-NLS-1$
								String[] result = new String[] {name, context, fullPath};
								results.add(result);
								keywordsFound.add(keyword); 
							}
						}
					}
				}
			}
		}
		// search pages
		for (String pageKey: pages.keySet()) {
			Map<String, ArrayList<String>> anchors = pages.get(pageKey);
			for (String anchor: anchors.keySet()) {
				String keyword = pageKey+"#"+anchor; //$NON-NLS-1$
				ArrayList<String> lines = anchors.get(anchor);
				for (String line: lines) {
					if (!keywordsFound.contains(keyword)) { 
						int n = line.toLowerCase().indexOf(searchPhrase);
						if (n>-1) {
							String phrase = getContextPhrase(line, searchPhrase);
							String name = pageNames.get(pageKey);
							String section = anchorNames.get(anchor);
							if (section!=null && !section.equals(name)) {
								name += ": "+section; //$NON-NLS-1$
							}
							String fullPath = pagePaths.get(pageKey)+"#"+anchor; //$NON-NLS-1$
							String[] result = new String[] {name, phrase, fullPath};
							results.add(result);
							keywordsFound.add(keyword); 
						}
					}
				}
			}
		}
		return results;
	}
	
	private static void initialize() {
		// initialize the maps
  	URL url = Tracker.class.getResource("resources/help/tracker_topics.xml"); //$NON-NLS-1$
  	String xml = ResourceLoader.getString(url.toExternalForm());
  	XMLControl control = new XMLControlElement(xml);
  	ArrayList<LaunchNode> children = (ArrayList<LaunchNode>)control.getObject("child_nodes"); //$NON-NLS-1$
  	for (LaunchNode next: children) {
  		String pagekey = next.getKeywords();
  		String name = next.getName();
  		pageNames.put(pagekey, name);
  		String path  = next.getDisplayTab(0).getURL().toString();
  		pagePaths.put(pagekey, path);
  		if (css==null) {
  			css = XML.getDirectoryPath(path)+"/help.css"; //$NON-NLS-1$
  		}
    	String html = ResourceLoader.getString(path);
    	Map<String, ArrayList<String>> map = getAnchors(html);
    	pages.put(pagekey, map);
  	}
  	
  	// create the search field, label and button
  	searchField = new JTextField(20) {
  		@Override
  		public Dimension getMaximumSize() {
  			return new Dimension((int)(100*FontSizer.getFactor()), clearSearchButton.getPreferredSize().height);
  		}
  	};
    searchField.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
      	
        if(e.getKeyCode()==KeyEvent.VK_ENTER) {
          String searchPhrase = stripExtraSpace(searchField.getText(), " "); //$NON-NLS-1$
          if (searchPhrase.length()>=minimumSearchPhraseLength) {
          	
          	ArrayList<String> found = new ArrayList<String>();
          	ArrayList<String[]> results = search(searchPhrase, found);
          	if (results.size()==0) {
  	          searchField.setBackground(_RED);
          		return;
          	}
	          searchField.setBackground(Color.white);
	          
	          // write results in HTML file
          	File file = writeResultsFile(searchPhrase, results, found);
          	if (file==null) return;
          	
          	// create and display results node
      			LaunchNode node = new LaunchNode("\""+searchPhrase+"\""); //$NON-NLS-1$ //$NON-NLS-2$
        		node.addDisplayTab(null, file.getAbsolutePath(), null);
        		node.getDisplayTab(0).getURL();  // so display tab url is not null
           	displayResultsNode(node);
          }
        } 
        else {
          searchField.setBackground(Color.yellow);
        }
      }

    });
    searchLabel = new JLabel();
    searchLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
    
    clearSearchButton = new JButton();
    clearSearchButton.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				helpLauncher.setSelectedTab(searchResultsTab);
				helpLauncher.removeSelectedTab();
				searchField.setText(null);
		    clearSearchButton.setEnabled(false);
		    searchResultsTab = null;
		    rootNode = null;
			}    	
    });
    clearSearchButton.setEnabled(false);
	}
	
	private static void displayResultsNode(LaunchNode node) {
		if (rootNode==null) {
			rootNode = new LaunchNode(TrackerRes.getString("HelpFinder.SearchResults")); //$NON-NLS-1$
		}
		if (searchResultsTab!=null) {
    	helpLauncher.setSelectedTab(searchResultsTab);
			helpLauncher.removeSelectedTab();
		}
		rootNode.add(node);
		ArrayList<LaunchNode> children = new ArrayList<LaunchNode>();
		for (int i=0; i<rootNode.getChildCount(); i++) {
			children.add((LaunchNode)rootNode.getChildAt(i));
		}
		File file = writeSummaryFile(children);
		if (rootNode.getDisplayTabCount()>0) {
			rootNode.removeDisplayTab(0);
		}
		rootNode.addDisplayTab(null, file.getAbsolutePath(), null);
		rootNode.getDisplayTab(0).getURL();  // so display tab url is not null
		
  	helpLauncher.addTab(rootNode); // also selects the tab
  	searchResultsTab = helpLauncher.getSelectedTab();
  	
    if (FontSizer.getLevel()>0) {
    	String newValue = "help"+FontSizer.getLevel()+".css"; //$NON-NLS-1$ //$NON-NLS-2$
    	searchResultsTab.getHTMLSubstitutionMap().put("help.css", newValue); //$NON-NLS-1$
    	helpLauncher.setFontLevel(FontSizer.getLevel());
	    for (int i=0; i<helpLauncher.getHTMLTabCount(); i++) {
	    	HTMLPane pane = helpLauncher.getHTMLTab(i);
	    	pane.editorPane.getDocument().putProperty(Document.StreamDescriptionProperty, null);
	    }
    }
    
  	searchResultsTab.setSelectedNode(node);
    clearSearchButton.setEnabled(true);
	}
	
	private static Map<String, ArrayList<String>> getAnchors(String html) {
		// clean the html
		html = clean(html);
		Map<String, ArrayList<String>> anchorMap = new TreeMap<String, ArrayList<String>>();
		
  	String[] sections = html.split("<h1|<h3"); //$NON-NLS-1$
  	for (int i=1; i< sections.length; i++) {  		
  		// find section-identifying ID anchor
    	String[] anchorSplit = sections[i].split("<a name=\""); //$NON-NLS-1$
    	if (anchorSplit.length<2) continue;    	
    	String anchor = anchorSplit[1].substring(0, anchorSplit[1].indexOf("\"")); //$NON-NLS-1$
    	
    	// find anchor name
    	// strip end anchor tag, if any
    	anchorSplit[1] = stripTag(anchorSplit[1], "</a");  //$NON-NLS-1$
    	String name = null;
  		int m = anchorSplit[1].indexOf(">"); //$NON-NLS-1$
  		int n = anchorSplit[1].indexOf("</h3>"); //$NON-NLS-1$
  		int p = anchorSplit[1].indexOf("</h1>"); //$NON-NLS-1$
    	if (m>0 && n>0 && p<0) {
    		name = anchorSplit[1].substring(m+1, n);
    	}
    	else if (m>0 && n<0 && p>0) {
    		name = anchorSplit[1].substring(m+1, p);
    	}
    	
    	// strip section numbering
    	try {
				Integer.parseInt(name.substring(0, 1));
				name = name.substring(name.indexOf(" ")+1, name.length()); //$NON-NLS-1$
			} catch (Exception e) {
			}
    	
    	anchorNames.put(anchor, name);
    	
    	// after finding the ID anchor, strip any other anchor tags
    	sections[i] = stripTag(sections[i], "<a href"); //$NON-NLS-1$
    	sections[i] = stripTag(sections[i], "</a"); //$NON-NLS-1$
    	ArrayList<String> lines = new ArrayList<String>();
    	String[] split = sections[i].split("<p>"); //$NON-NLS-1$
    	for (int j=1; j< split.length; j++) {
    		n = split[j].indexOf("</p>"); //$NON-NLS-1$
    		if (n>-1) {
    			split[j] = split[j].substring(0, n).trim();
    			if ("".equals(split[j])) continue; //$NON-NLS-1$
    		}
    		else {
    			// flag HTML code with nested lists that cause missing </p> after substitutions
    			System.out.println("no ending </p> in "+split[j]); //$NON-NLS-1$
    			System.out.println("found in section: "+sections[i]); //$NON-NLS-1$
    		}
    		// eliminate any extra spaces
    		split[j] = stripExtraSpace(split[j], " "); //$NON-NLS-1$
    		lines.add(split[j]);
    	}
    	anchorMap.put(anchor, lines);
  	}
  	return anchorMap;
	}
	
	private static String clean(String text) {
		text = text.replaceAll("<h5>", "<p>"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("</h5>", "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("<blockquote>", "<p>"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("</blockquote>", "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("<li>", "<p>"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("</li>", "</p>"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("<p align=\"center\">", "<p>"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("<br>", " "); //$NON-NLS-1$ //$NON-NLS-2$
		text = text.replaceAll("&quot;", "\""); //$NON-NLS-1$ //$NON-NLS-2$
		text = stripTag(text, "<img"); //$NON-NLS-1$
		text = stripTag(text, "<b"); //$NON-NLS-1$
		text = stripTag(text, "</b"); //$NON-NLS-1$
		text = stripTag(text, "<em"); //$NON-NLS-1$
		text = stripTag(text, "</em"); //$NON-NLS-1$
		text = stripTag(text, "<strong"); //$NON-NLS-1$
		text = stripTag(text, "</strong"); //$NON-NLS-1$
		text = stripTag(text, "<ol"); //$NON-NLS-1$
		text = stripTag(text, "</ol"); //$NON-NLS-1$
		text = stripTag(text, "<ul"); //$NON-NLS-1$
		text = stripTag(text, "</ul"); //$NON-NLS-1$
		return text;
	}
	
	private static String getContextPhrase(String line, String searchPhrase) {
		int n = line.toLowerCase().indexOf(searchPhrase);
		if (n>-1) {
			int start = Math.max(0, n-contextPhraseLength);
			int end = Math.min(line.length(), n+searchPhrase.length()+contextPhraseLength);
			String phrase = line.substring(start, end);
			start = phrase.toLowerCase().indexOf(searchPhrase);
			end = start+searchPhrase.length();
			n = phrase.indexOf(" "); //$NON-NLS-1$
			if (n>-1 && n<start-contextPhraseLength+contextPhraseTrim) {
				phrase = "..."+phrase.substring(n+1); //$NON-NLS-1$
			}
			start = phrase.toLowerCase().indexOf(searchPhrase);
			end = start+searchPhrase.length();
			n = phrase.lastIndexOf(" "); //$NON-NLS-1$
			if (n>end+contextPhraseLength-contextPhraseTrim) {
				phrase = phrase.substring(0, n)+"..."; //$NON-NLS-1$
			}
			return phrase;
		}
		else {
			String phrase = line.substring(0, Math.min(line.length(), 2*contextPhraseLength+contextPhraseTrim));
			n = phrase.lastIndexOf(" "); //$NON-NLS-1$
			if (n>contextPhraseLength) {
				phrase = phrase.substring(0, n)+"..."; //$NON-NLS-1$
			}
			return phrase;
		}
	}
	
	private static String stripTag(String text, String tag) {
		int i = text.indexOf(tag);
		while (i>0) {
			String later = text.substring(i);
			text = text.substring(0, i);
			int j = later.indexOf(">"); //$NON-NLS-1$
			if (j<0) break;
			later = later.substring(j+1);
			text += later;
			i = text.indexOf(tag);
		}
		return text;
	}
	
	private static String stripExtraSpace(String text, String space) {
		text = text.trim();
		String extraSpace = " "+space; //$NON-NLS-1$
		int n = text.indexOf(extraSpace);
		while (n>-1) {
			text = text.replaceAll(extraSpace, space);
			n = text.indexOf(extraSpace);
		}
		return text;
	}
	
  /**
   * Writes an HTML results file from scratch, using the current field text.
   */
  private static File writeResultsFile(String searchPhrase, ArrayList<String[]> results, 
  		ArrayList<String> termsFound) {
  	// get HTML code first
    String htmlCode = getResultsHTMLCode(searchPhrase, results, termsFound);
    
    // use hashcode for unique filename
    String fileName = "search"+results.hashCode()+".tmp"; //$NON-NLS-1$ //$NON-NLS-2$
    
    // write temporary HTML file in default OSPRuntime path
    ArrayList<String> paths = OSPRuntime.getDefaultSearchPaths();
    for (String path: paths) {
    	// attempt to write html file
	    File target = new File(path, fileName);
	    target = writeFile(htmlCode, target);
	    if (target!=null) {
	      target.deleteOnExit();
	    	return target;
	    }
    }

    String pathlist = ""; //$NON-NLS-1$
    for (String path: paths) {
    	pathlist += "\n"+path+","; //$NON-NLS-1$ //$NON-NLS-2$
    }
    pathlist = pathlist.substring(0, pathlist.length()-1);
    // if all attempts failed, inform user and return null
    JOptionPane.showMessageDialog(searchField.getTopLevelAncestor(), 
    		TrackerRes.getString("HelpFinder.Dialog.UnableToWrite.Text")+pathlist, //$NON-NLS-1$
    		TrackerRes.getString("HelpFinder.Dialog.UnableToWrite.Title"), //$NON-NLS-1$
    		JOptionPane.ERROR_MESSAGE);
    return null;
  }
  
  /**
   * Writes an HTML summary file from scratch, using the current field text.
   */
  private static File writeSummaryFile(ArrayList<LaunchNode> resultNodes) {
  	// get HTML code first
    String htmlCode = getSummaryHTMLCode(resultNodes);
    
    String fileName = "search_summary.tmp"; //$NON-NLS-1$
    
    // write temporary HTML file in default OSPRuntime path
    ArrayList<String> paths = OSPRuntime.getDefaultSearchPaths();
    for (String path: paths) {
    	// attempt to write html file
	    File target = new File(path, fileName);
	    target = writeFile(htmlCode, target);
	    if (target!=null) {
	      target.deleteOnExit();
	    	return target;
	    }
    }
    return null;
  }
  
  /**
   * Writes a text file.
   * 
   * @param text the text
   * @param target the File to write
   * @return the written File, or null if failed
   */
  private static File writeFile(String text, File target) {
    try {
      FileWriter fout = new FileWriter(target);
      fout.write(text);
      fout.close();
	  	return target;
    } catch(Exception ex) {}
    return null;
  }
  

  
  /**
   * Gets the html code for a resource with specified properties.
   * Note this code is for display in the LibraryBrowser, and has no stylesheet of its own.
   * 
   * @param title the name of the resource
   * @param description a description of the resource
   * @param authors authors
   * @param contact author contact information or institution
   *
   * @return the html code
   */
	private static String getResultsHTMLCode(String searchPhrase, ArrayList<String[]> searchResults,	
			ArrayList<String> termsToHighlight) {
  	StringBuffer buffer = new StringBuffer();
    buffer.append (
    		"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"); //$NON-NLS-1$
    buffer.append(
    		"\n  <html>"); //$NON-NLS-1$
    buffer.append(
    		"\n    <head>"); //$NON-NLS-1$
    buffer.append(
    		"\n"+getStyleSheetCode()); //$NON-NLS-1$
    buffer.append(
    		"\n      <meta http-equiv=\"content-type\" content=\"text/html;charset=iso-8859-1\">"); //$NON-NLS-1$
    buffer.append(
    		"\n    </head>\n"); //$NON-NLS-1$
    buffer.append(
    		"\n    <body>"); //$NON-NLS-1$
    buffer.append(
    		"\n    <h2>"+TrackerRes.getString("HelpFinder.ResultsFor")  //$NON-NLS-1$//$NON-NLS-2$
    		+" \""+searchPhrase+"\"</h2>"); //$NON-NLS-1$ //$NON-NLS-2$
    for (String[] next: searchResults) {
    	buffer.append(getResultsHTMLBody(next, termsToHighlight));
    }
    buffer.append(
    		"\n    </body>"); //$NON-NLS-1$
    buffer.append(
    		"\n  </html>"); //$NON-NLS-1$
    return buffer.toString();
	}
	  	
  /**
   * Gets the html code for a resource with specified properties.
   * Note this code is for display in the LibraryBrowser, and has no stylesheet of its own.
   * 
   * @param title the name of the resource
   * @param description a description of the resource
   * @param authors authors
   * @param contact author contact information or institution
   *
   * @return the html code
   */
	private static String getSummaryHTMLCode(ArrayList<LaunchNode> resultNodes) {
  	StringBuffer buffer = new StringBuffer();
    buffer.append (
    		"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">"); //$NON-NLS-1$
    buffer.append(
    		"\n  <html>"); //$NON-NLS-1$
    buffer.append(
    		"\n    <head>"); //$NON-NLS-1$
    buffer.append(
    		"\n"+getStyleSheetCode()); //$NON-NLS-1$
    buffer.append(
    		"\n      <meta http-equiv=\"content-type\" content=\"text/html;charset=iso-8859-1\">"); //$NON-NLS-1$
    buffer.append(
    		"\n    </head>\n"); //$NON-NLS-1$
    buffer.append(
    		"\n    <body>"); //$NON-NLS-1$
    buffer.append(
    		"\n    <h2>"+TrackerRes.getString("HelpFinder.ResultsFor")+":</h2>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    buffer.append(getSummaryHTMLBody(resultNodes));
    buffer.append(
    		"\n    </body>"); //$NON-NLS-1$
    buffer.append(
    		"\n  </html>"); //$NON-NLS-1$
    return buffer.toString();
	}
	  	
  /**
   * Gets html <body> code for a search result.
   * 
   * @param title the name of the resource
   * @param description a description of the resource
   * @param authors authors
   * @param contact author contact information or institution
   *
   * @return the html path
   */
	private static String getResultsHTMLBody(String[] result, ArrayList<String> highlightTerms) {
  	StringBuffer buffer = new StringBuffer();
    buffer.append("\n<p><a href=\""+result[2]+"\"><strong>"  //$NON-NLS-1$//$NON-NLS-2$
    		+result[0]+"</strong></a>"); //$NON-NLS-1$
    String context = result[1];
    for (String term: highlightTerms) {
    	context = addHighlights(context, term);
    }
    buffer.append(" \""+context+"\"</p>"); //$NON-NLS-1$ //$NON-NLS-2$
    return buffer.toString();
	}
	  	
  /**
   * Gets html <body> code for a search result.
   * 
   * @param title the name of the resource
   * @param description a description of the resource
   * @param authors authors
   * @param contact author contact information or institution
   *
   * @return the html path
   */
	private static String getSummaryHTMLBody(ArrayList<LaunchNode> resultNodes) {
  	StringBuffer buffer = new StringBuffer();
  	buffer.append("<blockquote>");  //$NON-NLS-1$
  	for (LaunchNode next: resultNodes) {
  		buffer.append("<h4><a href=\""+next.getDisplayTab(0).getURL()+"\">"  //$NON-NLS-1$//$NON-NLS-2$
    		+next+"</a></h4>"); //$NON-NLS-1$
  	}
  	buffer.append("</blockquote>");  //$NON-NLS-1$
    return buffer.toString();
	}
	  	
  /**
   * Returns the html code for a stylesheet.
   * 
   * @return the style sheet code
   */
  private static String getStyleSheetCode() {
		return "<link href=\""+css+"\" rel=\"stylesheet\" type=\"text/css\">"; //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  /**
   * Returns the html code for a stylesheet.
   * 
   * @return the style sheet code
   */
  private static String addHighlights(String text, String highlight) {
  	highlight = highlight.toLowerCase();
  	StringBuffer output = new StringBuffer();
  	int n = text.toLowerCase().indexOf(highlight);
  	while (n>-1) {
			output.append(text.substring(0, n));
  		String term = text.substring(n, n+highlight.length());
  		text = text.substring(n+highlight.length());
  		output.append("<strong>"+term+"</strong>"); //$NON-NLS-1$ //$NON-NLS-2$
  		n = text.toLowerCase().indexOf(highlight);
  	}
		output.append(text);
  	return output.toString();
  }
  
}
