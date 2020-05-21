package test.bsml;
//package ca.virology.lib.io.writer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
public class BSMLDocument {

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
	public BSMLDocument() {
	}
	
	/**
	 * Write the document to a writer. The writer is not closed.
	 * 
	 * @param writer if null, to System.out
	 */
	public boolean write(Writer writer) {
				
		try {
			// can't do this with com.sun.org.apache.xerces 
			// doc.setStandalone(true);
			
			Transformer t;
			t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, BSMLConstants.PUBLIC_ID);
			t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, BSMLConstants.DTD_URI);
			t.setOutputProperty(OutputKeys.INDENT, "yes");

			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			t.transform(new DOMSource(doc), new StreamResult(writer == null ?  new PrintWriter(System.out) : writer));
		} catch (TransformerException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

	/**
	 * test for BSMLFeaturedSequence code
	 */
	public static boolean test() {
		
		testXMLToDoc("<bsml>\n     <defn />\n<Sequence>\n\ntesting<sequence>\ntesting\n</sequence></Sequence></bsml>");
		testXMLToDoc("<bsml>\n     <defn />\n    </bsml>");
		String s = testDocToXML();
		System.out.println(s);
		
		String check =  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
	"<!DOCTYPE Bsml PUBLIC \"-//Labbook, Inc. BSML DTD//EN\" \"http://www.labbook.com/dtd/bsml3_1.dtd\">\n" + 
	"<Bsml testing=\"true\">\n" + 
	"  <Definitions id=\"def\" value=\"defvalue\">\n" + 
	"    <Div1/>\n" + 
	"    <Div2/>\n" + 
	"  </Definitions>\n" + 
	"</Bsml>\n";
		// BH I do not know why Java is putting standalone 
		s = s.replaceAll("\r",  "").replace("standalone=\"yes\"", "standalone=\"no\"");
		System.out.println(s.length() + " " + check.length());
		System.out.println(s.equals(check));
		testXMLToDoc(s);
		
		return true;
	}


	private static String testDocToXML() {
		BSMLDocument out = new BSMLDocument();
		Document doc = out.getDocument();
		Element root = doc.createElement("Bsml");
		root.setAttribute("testing", "true");
		doc.appendChild(root);
		Element defns = doc.createElement("Definitions");
		defns.setAttribute("id", "def");
		defns.setAttribute("value", "defvalue");
		
		root.appendChild(defns);
		defns.appendChild(doc.createElement("Div1"));
		defns.appendChild(doc.createElement("Div2"));
		StringWriter w = new StringWriter();
		out.write(w);
		return w.toString();
	}

	private static void testXMLToDoc(String s) {


		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        dbf.setValidating(true);
        dbf.setCoalescing(true);
        dbf.setNamespaceAware(true);
        try {
	        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
	        dbf.setFeature("http://xml.org/sax/features/validation", false);
	        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
	        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            
            InputSource is = new InputSource(new StringReader(s));
            Document d = db.parse(is);
            //System.out.println(d.getNodeName() + " " + d.getNodeValue() + " " + d.getNodeType());
            NodeList list = d.getChildNodes();
            for (int i = 0; i < list.getLength(); i++)
            	testNode(list.item(i));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(e);
        }
	}

	private static void testNode(Node node) {
		System.out.println(node.getNodeName() + " [" + node.getNodeValue() + ("," + node.getTextContent()).replace("\n", "\\n") + "]");
		if (node.hasChildNodes()) {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				testNode(list.item(i));
			}
		}
	}

}
