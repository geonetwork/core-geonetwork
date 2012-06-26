//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.utils;

import jeeves.exceptions.XSDValidationErrorEx;
import net.sf.saxon.Configuration;
import net.sf.saxon.FeatureKeys;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.SAXOutputter;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//=============================================================================

/** General class of useful static methods
  */

public class Xml
{
	//--------------------------------------------------------------------------
	//---
	//--- Load API
	//---
	//--------------------------------------------------------------------------

    private static SAXBuilder getSAXBuilderWithoutXMLResolver(boolean validate) {
        SAXBuilder builder = new SAXBuilder(validate);
        builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return builder;
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     * @throws JDOMException
     */
	public static Element loadFile(String file) throws IOException, JDOMException
	{
		return loadFile(new File(file));
	}

	//--------------------------------------------------------------------------
	/** Loads an xml file from a URL and returns its root node */
	
	public static Element loadFile(URL url) throws IOException, JDOMException
	{
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false);//new SAXBuilder();
		Document   jdoc    = builder.build(url);

		return (Element) jdoc.getRootElement().detach();
	}

	//--------------------------------------------------------------------------
	/** Loads an xml file from a URL after posting content to the URL */
	
	public static Element loadFile(URL url, Element xmlQuery) throws IOException, JDOMException
	{
		Element result = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/xml");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(getString(xmlQuery).getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setDoOutput(true);
			PrintWriter out = new PrintWriter(connection.getOutputStream()); 
			out.print(getString(xmlQuery));
			out.close();

			SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false);//new SAXBuilder();
			Document   jdoc    = builder.build(connection.getInputStream());

			result = (Element)jdoc.getRootElement().detach();
		} catch (Exception e) {
			System.out.println("Threw exception "+e);
			e.printStackTrace();
		}
		return result;
	}

	//--------------------------------------------------------------------------

	/** Loads an xml file and returns its root node */

	public static Element loadFile(File file) throws IOException, JDOMException
	{
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false);//new SAXBuilder();
		Document   jdoc    = builder.build(file);

		return (Element) jdoc.getRootElement().detach();
	}

	//--------------------------------------------------------------------------
	/** Loads an xml file and returns its root node (validates the xml with a dtd) */

	public static Element loadString(String data, boolean validate)
												throws IOException, JDOMException
	{
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(validate);//new SAXBuilder(validate);
		Document   jdoc    = builder.build(new StringReader(data));

		return (Element) jdoc.getRootElement().detach();
	}

	//--------------------------------------------------------------------------
	/** Loads an xml stream and returns its root node (validates the xml with a dtd) */

	public static Element loadStream(InputStream input) throws IOException, JDOMException
	{
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false);//new SAXBuilder();
		builder.setFeature("http://apache.org/xml/features/validation/schema",false);
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
		Document   jdoc    = builder.build(input);

		return (Element) jdoc.getRootElement().detach();
	}

	//--------------------------------------------------------------------------
	//---
	//--- Transform API
	//---
	//--------------------------------------------------------------------------

	/** Transform an xml tree into another using a stylesheet on disk */

	public static Element transform(Element xml, String styleSheetPath) throws Exception
	{
		JDOMResult resXml = new JDOMResult();
		transform(xml, styleSheetPath, resXml, null);
		return (Element)resXml.getDocument().getRootElement().detach();
	}

    /**
     * Transforms an xml tree into another using a stylesheet on disk.
     *
     * @param xml document to transform
     * @param xslt transformation
     * @return result
     * @throws Exception hmm
     */
	public static Element transform(Element xml, Source xslt) throws Exception
	{
		JDOMResult resXml = new JDOMResult();
		transform(xml, xslt, resXml, null, false);
		return (Element)resXml.getDocument().getRootElement().detach();
	}

    /**
     * Transforms an xml tree into another using a stylesheet on disk, with option to require non-caching
     * transformerfactory. That is useful in cases where the XSLT transformation does not reside in a file, but is
     * generated dynamically -- the caching transformerfactory throws a NullpointerException for those.
     *
     * @param xml document to transform
     * @param xslt transformation
     * @param nonCachingTransformerFactory whether to require noncaching transformerfactory
     * @return result
     * @throws Exception hmm
     */
	public static Element transform(Element xml, Source xslt, boolean nonCachingTransformerFactory) throws Exception
	{
		JDOMResult resXml = new JDOMResult();
		transform(xml, xslt, resXml, null, nonCachingTransformerFactory);
		return (Element)resXml.getDocument().getRootElement().detach();
	}

	//--------------------------------------------------------------------------
	/** Transform an xml tree into another using a stylesheet on disk and pass
	 * parameters */

	public static Element transform(Element xml, String styleSheetPath, Map<String,String> params) throws Exception
	{
		JDOMResult resXml = new JDOMResult();
		transform(xml, styleSheetPath, resXml, params);
		return (Element)resXml.getDocument().getRootElement().detach();
	}

	//--------------------------------------------------------------------------
	/** Transform an xml tree putting the result to a stream (uses a stylesheet on disk) */
	public static void transform(Element xml, String styleSheetPath, OutputStream out) throws Exception
	{
		StreamResult resStream= new StreamResult(out);
		transform(xml, styleSheetPath, resStream, null);
	}

	//--------------------------------------------------------------------------
	/** Transform an xml tree putting the result to a stream  - no parameters */
	public static void transform(Element xml, String styleSheetPath, Result result) throws Exception
	{
		transform(xml, styleSheetPath, result, null);
	}

	//--------------------------------------------------------------------------
	/** Transform an xml tree putting the result to a stream with optional parameters */

	public static void transform(Element xml, String styleSheetPath, Result result, Map<String,String> params) throws Exception {
		File styleSheet = new File(styleSheetPath);
		Source srcSheet = new StreamSource(styleSheet);
        transform(xml, srcSheet, result, params, false);
	}

    /**
     * Transforms an xml tree putting the result to a stream with optional parameters.
     *
     * @param xml document to be transformed
     * @param xslt transformation to use
     * @param result result
     * @param params parameters
     * @param nonCachingTransformerFactory whether non-caching transformerfactory is required
     * @throws Exception hmm
     */
	public static void transform(Element xml, Source xslt, Result result, Map<String,String> params, boolean nonCachingTransformerFactory) throws Exception {
		Source srcXml   = new JDOMSource(new Document((Element)xml.detach()));

		// Dear old saxon likes to yell loudly about each and every XSLT 1.0
		// stylesheet so switch it off but trap any exceptions because this
		// code is run on transformers other than saxon
        TransformerFactory transFact;
        if(nonCachingTransformerFactory == false) {
            transFact = TransformerFactoryFactory.getTransformerFactory();
        }
        // non-caching transformerfactory required
        else {
            transFact = TransformerFactoryFactory.getNonCachingTransformerFactory();
        }
		try {
			transFact.setAttribute(FeatureKeys.VERSION_WARNING,false);
			transFact.setAttribute(FeatureKeys.LINE_NUMBERING,true);
			transFact.setAttribute(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION,true);
			transFact.setAttribute(FeatureKeys.RECOVERY_POLICY,Configuration.RECOVER_SILENTLY);
			// Add the following to get timing info on xslt transformations
			//transFact.setAttribute(FeatureKeys.TIMING,true);
		} catch (IllegalArgumentException e) {
			System.out.println("WARNING: transformerfactory doesnt like saxon attributes!");
			//e.printStackTrace();
		} finally {
			Transformer t = transFact.newTransformer(xslt);
			if (params != null) {
				for (String param : params.keySet()) {
					t.setParameter(param,params.get(param));
				}
			}
			t.transform(srcXml, result);
		}
	}

   // --------------------------------------------------------------------------
   /**
   * Transform an xml tree to PDF using XSL-FOP 
     * putting the result to a stream (uses a stylesheet
   * on disk)
   */

   public static String transformFOP(String uploadDir, Element xml, String styleSheetPath)
           throws Exception {
       String file = uploadDir + UUID.randomUUID().toString () + ".pdf";

   // Step 1: Construct a FopFactory
   // (reuse if you plan to render multiple documents!)
   FopFactory fopFactory = FopFactory.newInstance();
   
   // Step 2: Set up output stream.
   // Note: Using BufferedOutputStream for performance reasons (helpful
   // with FileOutputStreams).
   OutputStream out = new BufferedOutputStream(new FileOutputStream(
           new File(file)));
   
   try {
       // Step 3: Construct fop with desired output format
   Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

   // Step 4: Setup JAXP using identity transformer
   TransformerFactory factory = TransformerFactoryFactory.getTransformerFactory();

   Source xslt = new StreamSource(new File(styleSheetPath));
		try {
			factory.setAttribute(FeatureKeys.VERSION_WARNING,false);
			factory.setAttribute(FeatureKeys.LINE_NUMBERING,true);
			factory.setAttribute(FeatureKeys.RECOVERY_POLICY,Configuration.RECOVER_SILENTLY);
		} catch (IllegalArgumentException e) {
			System.out.println("WARNING: transformerfactory doesnt like saxon attributes!");
			//e.printStackTrace();
		} finally {
   		Transformer transformer = factory.newTransformer(xslt);

   		// Step 5: Setup input and output for XSLT transformation
   		// Setup input stream
			Source src = new JDOMSource(new Document((Element)xml.detach()));
   
   		// Resulting SAX events (the generated FO) must be piped through to
   		// FOP
   		Result res = new SAXResult(fop.getDefaultHandler());

   		// Step 6: Start XSLT transformation and FOP processing
      transformer.transform(src, res);
		}
   } catch (Exception e) {         
       throw e;
   } finally {
       // Clean-up
           out.close();
       }
       
       return file;
   }
   
	//--------------------------------------------------------------------------
	//---
	//--- General stuff
	//---
	//--------------------------------------------------------------------------

    /**
     * Writes an xml element to a stream.
     *
     * @param doc
     * @param out
     * @throws IOException
     */
	public static void writeResponse(Document doc, OutputStream out) throws IOException
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, out);
	}

	//---------------------------------------------------------------------------

    /**
     * Converts an xml element to a string.
     *
     * @param data
     * @return
     */
	public static String getString(Element data)
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		return outputter.outputString(data);
	}

	//---------------------------------------------------------------------------

	public static String getString(Document data)
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		return outputter.outputString(data);
	}

	//---------------------------------------------------------------------------
	/** Creates and prepares an XPath element - simple xpath (like "a/b/c") */
	private static XPath prepareXPath(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException
	{
		XPath xp = XPath.newInstance (xpath);
		for (Namespace ns : theNSs ) {
			xp.addNamespace(ns);
		}

		return xp;
	}

	//---------------------------------------------------------------------------

    /**
     * Retrieves a single XML element given a simple xpath (like "a/b/c").
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
	public static Object selectSingle(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException {

		XPath xp = prepareXPath(xml, xpath, theNSs);

		return xp.selectSingleNode(xml);
	}

	//---------------------------------------------------------------------------

    /**
     * Retrieves a single XML element as a JDOM element given a simple xpath.
     *
     * @param xml
     * @param xpath
     * @return
     * @throws JDOMException
     */
	public static Element selectElement(Element xml, String xpath) throws JDOMException {
		return selectElement(xml, xpath, new ArrayList<Namespace>());
	}

	//---------------------------------------------------------------------------

    /**
     * Retrieves a single XML element as a JDOM element given a simple xpath.
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
	public static Element selectElement(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException {
		Object result = selectSingle(xml, xpath, theNSs);
		if (result == null) {
			return null;
		} else if (result instanceof Element) {
			Element elem = (Element)result;
			return (Element)(elem);
		} else {
			//-- Found something but not an element
			return null;
		}
	}

	//---------------------------------------------------------------------------

    /**
     * Evaluates an XPath expression on an element and returns Elements.
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
	public static List<?> selectNodes(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException {
		XPath xp = prepareXPath(xml, xpath, theNSs);
		return xp.selectNodes(xml);
	}
		
	//---------------------------------------------------------------------------

    /**
     * Evaluates an XPath expression on an element and returns Elements.
     * @param xml
     * @param xpath
     * @return
     * @throws JDOMException
     */
	public static List<?> selectNodes(Element xml, String xpath) throws JDOMException {
		return selectNodes(xml, xpath, new ArrayList<Namespace>());
	}
		
	//---------------------------------------------------------------------------

    /**
     * Evaluates an XPath expression on an element and returns string result.
     *
     * @param xml
     * @param xpath
     * @return
     * @throws JDOMException
     */
	public static String selectString(Element xml, String xpath) throws JDOMException {
		return selectString(xml, xpath, new ArrayList<Namespace>());
	}

	//---------------------------------------------------------------------------

    /**
     * Evaluates an XPath expression on an element and returns string result.
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
	public static String selectString(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException {

		XPath xp = prepareXPath(xml, xpath, theNSs);

		return xp.valueOf(xml);
	}

	//---------------------------------------------------------------------------

    /**
     * Evaluates an XPath expression on an element and returns true/false.
     *
     * @param xml
     * @param xpath
     * @return
     * @throws JDOMException
     */
	public static boolean selectBoolean(Element xml, String xpath) throws JDOMException {
		String result = selectString(xml, xpath, new ArrayList<Namespace>());
		return result.length() > 0;
	}

	//---------------------------------------------------------------------------

    /**
     * Evaluates an XPath expression on an element and returns true/false.
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
	public static boolean selectBoolean(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException {
		return selectString(xml, xpath, theNSs).length() > 0;
	}

	//---------------------------------------------------------------------------
	/** Evaluates an XPath expression on an element and returns number result */
	public static Number selectNumber(Element xml, String xpath) throws JDOMException {
		return selectNumber(xml, xpath, new ArrayList<Namespace>());
 	}

	//---------------------------------------------------------------------------
	/** Evaluates an XPath expression on an element and returns number result */
	public static Number selectNumber(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException {

		XPath xp = prepareXPath(xml, xpath, theNSs);

		return xp.numberValueOf(xml);
	}

	//---------------------------------------------------------------------------

    /**
     * Error handler that collects up validation errors.
     *
     */
	public static class ErrorHandler extends DefaultHandler {

		private int errorCount = 0;
		private String errors = " ";
		private Element xpaths;
		private Namespace ns = Namespace.NO_NAMESPACE;
		private SAXOutputter so;
		
		public void setSo(SAXOutputter so) {
			this.so = so;
		}
		
		public boolean errors() {
			return errorCount > 0;
		}

		public Element getXPaths() {
			return xpaths;
		}

		public void addMessage ( SAXParseException exception, String typeOfError ) {
			if (errorCount == 0) xpaths = new Element("xsderrors", ns);
			errorCount++;

			Element elem = (Element) so.getLocator().getNode();
			Element x = new Element("xpath", ns);
			try {
				String xpath = jeeves.utils.XPath.getXPath(elem);
				//-- remove the first element to ensure XPath fits XML passed with
				//-- root element
				if (xpath.startsWith("/")) { 
					int ind = xpath.indexOf('/',1);
					if (ind != -1) {
						xpath = xpath.substring(ind+1);
					} else {
						xpath = "."; // error to be placed on the root element
					}
				}
				x.setText(xpath);
			} catch (JDOMException e) {
				e.printStackTrace();
				x.setText("nopath");
			}
			String message = exception.getMessage() + " (Element: " + elem.getQualifiedName();
			String parentName;
			if (!elem.isRootElement()) {
				Element parent = (Element)elem.getParent();
				if (parent != null)
					parentName = parent.getQualifiedName();
				else
					parentName = "Unknown";
			} else {
				parentName = "/";
			}
			message += " with parent element: " + parentName + ")";
			
			Element m = new Element("message", ns).setText(message);
			Element errorType = new Element("typeOfError", ns).setText(typeOfError);
			Element errorNumber = new Element("errorNumber", ns).setText(String.valueOf(errorCount));
			Element e = new Element("error", ns);
			e.addContent(errorType);
			e.addContent(errorNumber);
			e.addContent(m);
			e.addContent(x);
			xpaths.addContent(e);
		}
		
		public void error( SAXParseException parseException ) throws SAXException {
			addMessage( parseException, "ERROR" );
		}

		public void fatalError( SAXParseException parseException ) throws SAXException {
			addMessage( parseException, "FATAL ERROR" );
		}

		public void warning( SAXParseException parseException ) throws SAXException {
			addMessage( parseException, "WARNING" );
		}

		/**
		 * Set namespace to use for report elements
		 * @param ns
		 */
		public void setNs(Namespace ns) {
			this.ns = ns;
		}

		public Namespace getNs() {
			return ns;
		}
	}

	//---------------------------------------------------------------------------
	/** Validates an xml document with respect to an xml schema described by .xsd file path */

	
	public static void validate(String schemaPath, Element xml) throws Exception
	{
		Element xsdXPaths = validateInfo(schemaPath,xml);
		if (xsdXPaths != null && xsdXPaths.getContent().size() > 0) throw new XSDValidationErrorEx("XSD Validation error(s)", xsdXPaths);
	}

	public static void validate(Element xml) throws Exception 
	{
		//NOTE: Create a schema object without schema file name so that schemas
		//will be obtained from whatever locations are provided in the document
		//including over the net

		Schema schema = factory().newSchema();             
		ErrorHandler eh = new ErrorHandler();
		validateRealGuts(schema, xml, eh);
	}

	//---------------------------------------------------------------------------

    /**
     * Validates an xml document with respect to an xml schema described by .xsd file path.
     *
     * @param schemaPath
     * @param xml
     * @return
     * @throws Exception
     */
	public static Element validateInfo(String schemaPath, Element xml) throws Exception
	{
		ErrorHandler eh = new ErrorHandler();
		validateGuts(schemaPath, xml, eh);
		if (eh.errors()) {
			return eh.getXPaths();
		} else {
			return null;
		}
	}

	//---------------------------------------------------------------------------

    /**
     * Validates an xml document with respect to an xml schema described by .xsd file path using supplied error handler.
     *
     * @param schemaPath
     * @param xml
     * @param eh
     * @return
     * @throws Exception
     */
	public static Element validateInfo(String schemaPath, Element xml, ErrorHandler eh)
			throws Exception {
		validateGuts(schemaPath, xml, eh);
		if (eh.errors()) {
			return eh.getXPaths();
		} else {
			return null;
		}
	}
	
	//---------------------------------------------------------------------------

    /**
     * Called by validation methods that supply an xml schema described by .xsd file path.
     *
     * @param schemaPath
     * @param xml
     * @param eh
     * @throws Exception
     */
	private static void validateGuts(String schemaPath, Element xml, ErrorHandler eh) throws Exception {
		StreamSource schemaFile = new StreamSource(new File(schemaPath));
		Schema schema = factory().newSchema(schemaFile);
		validateRealGuts(schema, xml, eh);
	}

	private static void validateRealGuts(Schema schema, Element xml, ErrorHandler eh) throws Exception {
		ValidatorHandler vh = schema.newValidatorHandler();
		vh.setErrorHandler(eh);
		SAXOutputter so = new SAXOutputter(vh);
		eh.setSo(so);
		so.output(xml);
	} 

	private static SchemaFactory factory() {
		return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}


}
