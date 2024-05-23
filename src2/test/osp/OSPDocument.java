package test.osp;
//package ca.virology.lib.io.writer;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opensourcephysics.tools.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class to generalize writing of a BSML xml document, removing any reference to 
 * specific implementations such as org.apache.xml or com.sun.org.apache.xml (so that
 * that can be overridden seamlessly in JavaScript).
 * 
 * @author hansonr
 *
 */
public class OSPDocument {

	private Document doc;

	
	/**
	 * Get the document, if it was initialized successfully.
	 * 
	 * @return the w3c.dom.Document, or null if there was a problem creating it.
	 */
	public Document getDocument() {
		if (doc == null)
			try {
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				System.out.println(doc.getClass().getName());
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		return  doc;
	}

	/**
	 * Initialize the document and set its data type and output parameters.
	 * 
	 */
	public OSPDocument() {
	}
	

	/**
	 * test for BSMLFeaturedSequence code
	 */
	public static boolean test() {

		URL url = OSPDocument.class.getResource("car.trk");
		String data = new String(ResourceLoader.getURLContents(url));
		testXMLToDoc(data);
		return true;
	}



	private static void testXMLToDoc(String s) {


		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        dbf.setValidating(false);
        dbf.setCoalescing(true);
        dbf.setNamespaceAware(false);
        try {
	        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
	        dbf.setFeature("http://xml.org/sax/features/validation", false);
	        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            
            InputSource is = new InputSource(new StringReader(s));
            Document d = db.parse(is);
            System.out.println(d.getNodeName() + " " + d.getNodeValue() + " " + d.getNodeType());
            NodeList list = d.getChildNodes();
            for (int i = 0; i < list.getLength(); i++)
            	testNode(list.item(i), "");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(e);
        }
	}

	private static void testNode(Node node, String prefix) {
		String name = node.getNodeName();
		NamedNodeMap map = node.getAttributes();
		if (map == null) {
			String t = node.getTextContent().trim();
			if (t.length() > 0)
				System.out.println(prefix + "value=\"" + t + "\"");
		} else {
			System.out.println("\n" + prefix + name);
			for (int i = 0, n = map.getLength(); i < n; i++) {
				Node at = map.item(i);
				System.out.println(prefix + "  " + at.getNodeName() + "=\"" + at.getNodeValue() + "\"");
			}
		}
		if (node.hasChildNodes()) {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				testNode(list.item(i), prefix + "  ");
			}
		}
	}

}
