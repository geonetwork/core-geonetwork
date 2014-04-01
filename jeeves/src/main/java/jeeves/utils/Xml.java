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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.jdom.DocType;
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
import org.mozilla.universalchardet.UniversalDetector;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import org.eclipse.core.runtime.URIUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//=============================================================================

/**
 *  General class of useful static methods.
 */
public final class Xml 
{

	public static final Namespace xsiNS = Namespace.getNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);

   //--------------------------------------------------------------------------

    /**
     *
     * @param validate
     * @return
     */
	private static SAXBuilder getSAXBuilder(boolean validate) {
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(validate);
        Resolver resolver = ResolverWrapper.getInstance();
        builder.setEntityResolver(resolver.getXmlResolver());
        return builder;
	}

    private static SAXBuilder getSAXBuilderWithoutXMLResolver(boolean validate) {
        SAXBuilder builder = new JeevesSAXBuilder(validate);
        //SAXBuilder builder = new SAXBuilder(validate);
        builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return builder;
    }

	//--------------------------------------------------------------------------

    /**
     *
     */
	public static void resetResolver() {
		Resolver resolver = ResolverWrapper.getInstance();
		resolver.reset();
	}

	//--------------------------------------------------------------------------
	//---
	//--- Load API
	//---
	//--------------------------------------------------------------------------

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

    /**
     * Loads an xml file from a URL and returns its root node.
     *
     * @param url
     * @return
     * @throws IOException
     * @throws JDOMException
     */
	public static Element loadFile(URL url) throws IOException, JDOMException
	{
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false);//new SAXBuilder();
		Document   jdoc    = builder.build(url);

		return (Element) jdoc.getRootElement().detach();
	}

	//--------------------------------------------------------------------------

    /**
     * Loads an xml file from a URL after posting content to the URL.
     *
     * @param url
     * @param xmlQuery
     * @return
     * @throws IOException
     * @throws JDOMException
     */
	public static Element loadFile(URL url, Element xmlQuery) throws IOException, JDOMException
	{
		Element result = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/xml");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(getString(xmlQuery).getBytes("UTF-8").length));
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setDoOutput(true);
			PrintStream out = new PrintStream(connection.getOutputStream(), true, "UTF-8"); 
			out.print(getString(xmlQuery));
			out.close();

			SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false);//new SAXBuilder();
			Document   jdoc    = builder.build(connection.getInputStream());

			result = (Element)jdoc.getRootElement().detach();
		} catch (Exception e) {
		    Log.error(Log.ENGINE, "Error loading URL " + url.getPath() + " .Threw exception "+e);
			e.printStackTrace();
		}
		return result;
	}

	//--------------------------------------------------------------------------

    /**
     * Loads an xml file and returns its root node.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws JDOMException
     */
	public static Element loadFile(File file) throws IOException, JDOMException
	{
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false); //new SAXBuilder();

		String convert = System.getProperty("jeeves.filecharsetdetectandconvert");

		// detect charset and convert if required
		if (convert != null && convert.equals("enabled")) { 
			byte[] content = convertFileToUTF8ByteArray(file);
			return loadStream(new ByteArrayInputStream(content));

		// no charset detection and conversion allowed
		} else { 
			Document   jdoc    = builder.build(file);
			return (Element) jdoc.getRootElement().detach();
		}

	}

	//--------------------------------------------------------------------------

    /**
     * Reads file into byte array, detects charset and converts from this  
		 * charset to UTF8
     *
     * @param file file to decode and convert to UTF8
     * @return
     * @throws IOException
     * @throws CharacterCodingException
     */

	public synchronized static byte[] convertFileToUTF8ByteArray(File file) throws IOException, CharacterCodingException {
	        FileInputStream in = null;
			DataInputStream inStream = null;
			try {
                in = new FileInputStream(file);
                inStream = new DataInputStream(in);
			byte[] buf = new byte[(int)file.length()];
			int nrRead = inStream.read(buf);
		
			UniversalDetector detector = new UniversalDetector(null);
			detector.handleData(buf, 0, nrRead);
			detector.dataEnd();

			String encoding = detector.getDetectedCharset();
			detector.reset();
			if (encoding != null) {
				if (!encoding.equals("UTF-8")) {
					Log.error(Log.JEEVES,"Detected character set "+encoding+", converting to UTF-8");
					return convertByteArrayToUTF8ByteArray(buf, encoding);
				}
			} 
			return buf;
			} finally {
			    if(in != null) {
			        IOUtils.closeQuietly(in);
			    }
			    if (inStream != null) {
			        IOUtils.closeQuietly(inStream);
			    }
			}
	}

	//--------------------------------------------------------------------------

    /**
     * Decode byte array as specified charset, then convert to UTF-8 
		 * by encoding as UTF8
     *
     * @param buf byte array to decode and convert to UTF8
     * @param charsetName charset to decode byte array into
     * @return
     * @throws CharacterCodingException
     */

	public synchronized static byte[] convertByteArrayToUTF8ByteArray(byte[] buf, String charsetName) throws CharacterCodingException {
		Charset cset;
		cset = Charset.forName(charsetName); // detected character set name
		CharsetDecoder csetDecoder = cset.newDecoder();

		Charset utf8 = Charset.forName("UTF-8");
		CharsetEncoder utf8Encoder = utf8.newEncoder();

		ByteBuffer inputBuffer = ByteBuffer.wrap(buf);

		// decode as detected character set
		CharBuffer data = csetDecoder.decode(inputBuffer);

		// encode as UTF-8
		ByteBuffer outputBuffer = utf8Encoder.encode(data);

		// remove any nulls from the end of the encoded data why? - this is a 
		// bug in the encoder???? could also be that the file has characters
		// from more than one charset?
		byte[] out = outputBuffer.array();
		int length = out.length;
		while (out[length-1] == 0) length--;

		byte[] result = new byte[length];
		System.arraycopy(out,0,result,0,length);

		// now return the converted bytes
		return result;
	}

	//--------------------------------------------------------------------------

    /**
     * Loads xml from a string and returns its root node 
		 * (validates the xml if required).
     *
     * @param data 
     * @param validate
     * @return
     * @throws IOException
     * @throws JDOMException
     */
	public static Element loadString(String data, boolean validate)
												throws IOException, JDOMException
	{
		//SAXBuilder builder = new SAXBuilder(validate);
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(validate); // oasis catalogs are used
		Document   jdoc    = builder.build(new StringReader(data));

		return (Element) jdoc.getRootElement().detach();
	}

	//--------------------------------------------------------------------------

    /**
     * Loads xml from an input stream and returns its root node.
     *
     * @param input
     * @return
     * @throws IOException
     * @throws JDOMException
     */
	public static Element loadStream(InputStream input) throws IOException, JDOMException
	{
		SAXBuilder builder = getSAXBuilderWithoutXMLResolver(false); //new SAXBuilder();
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

    /**
     * Transforms an xml tree into another using a stylesheet on disk.
     *
     * @param xml
     * @param styleSheetPath
     * @return
     * @throws Exception
     */
	public static Element transform(Element xml, String styleSheetPath) throws Exception
	{
		JDOMResult resXml = new JDOMResult();
		transform(xml, styleSheetPath, resXml, null);
		return (Element)resXml.getDocument().getRootElement().detach();
	}

	//--------------------------------------------------------------------------

    /**
     * Transforms an xml tree into another using a stylesheet on disk and pass parameters.
     *
     * @param xml
     * @param styleSheetPath
     * @param params
     * @return
     * @throws Exception
     */
	public static Element transform(Element xml, String styleSheetPath, Map<String,String> params) throws Exception
	{
		JDOMResult resXml = new JDOMResult();
		transform(xml, styleSheetPath, resXml, params);
		return (Element)resXml.getDocument().getRootElement().detach();
	}

	//--------------------------------------------------------------------------

    /**
     * Transforms an xml tree putting the result to a stream (uses a stylesheet on disk).
     *
     * @param xml
     * @param styleSheetPath
     * @param out
     * @throws Exception
     */
	public static void transform(Element xml, String styleSheetPath, OutputStream out) throws Exception
	{
		StreamResult resStream= new StreamResult(out);
		transform(xml, styleSheetPath, resStream, null);
	}

	//--------------------------------------------------------------------------

    /**
     * Transforms an xml tree putting the result to a stream  - no parameters.
     *
     * @param xml
     * @param styleSheetPath
     * @param result
     * @throws Exception
     */
	public static void transform(Element xml, String styleSheetPath, Result result) throws Exception
	{
		transform(xml, styleSheetPath, result, null);
	}

	//--------------------------------------------------------------------------

	private static class JeevesURIResolver implements URIResolver {

    /**
     *
     * @param href
     * @param base
     * @return
     * @throws TransformerException
     */
     public Source resolve(String href, String base) throws TransformerException {
        Resolver resolver = ResolverWrapper.getInstance();
        CatalogResolver catResolver = resolver.getCatalogResolver();
        if(Log.isDebugEnabled(Log.XML_RESOLVER)) {
            Log.debug(Log.XML_RESOLVER, "Trying to resolve "+href+":"+base);
        }
        Source s = catResolver.resolve(href, base);
        // If resolver has a blank XSL file use it to replace 
				// resolved file that doesn't exist...
        String blankXSLFile = resolver.getBlankXSLFile();
        if (blankXSLFile != null && s.getSystemId().endsWith(".xsl")) {
            try {
                if(Log.isDebugEnabled(Log.XML_RESOLVER)) {
                    Log.debug(Log.XML_RESOLVER, "  Check if exist " + s.getSystemId());
                }
                File f = URIUtil.toFile(new URI(s.getSystemId()));
								if(Log.isDebugEnabled(Log.XML_RESOLVER)) 
									Log.debug(Log.XML_RESOLVER, "Check on "+f.getPath()+" exists returned: "+f.exists());

            		// If the resolved resource does not exist, set it to blank file path to not trigger FileNotFound Exception
                if (!(f.exists())) {
                    if(Log.isDebugEnabled(Log.XML_RESOLVER)) {
                        Log.debug(Log.XML_RESOLVER, "  Resolved resource " + s.getSystemId() + " does not exist. blankXSLFile returned instead.");
                    }
                    s.setSystemId(blankXSLFile);
                }
            }
            catch (URISyntaxException e) {
								Log.warning(Log.XML_RESOLVER, "URI syntax problem: "+e.getMessage());
                e.printStackTrace();
					  }
        }
         
         if (Log.isDebugEnabled(Log.XML_RESOLVER) && s != null) {
             Log.debug(Log.XML_RESOLVER, "Resolved as "+s.getSystemId());
         }
         return s;
         }
     }

	//--------------------------------------------------------------------------

    /**
     * Transforms an xml tree putting the result to a stream with optional parameters.
     *
     * @param xml
     * @param styleSheetPath
     * @param result
     * @param params
     * @throws Exception
     */
	public static void transform(Element xml, String styleSheetPath, Result result, Map<String,String> params) throws Exception
	{
		File styleSheet = new File(styleSheetPath);
		Source srcXml   = new JDOMSource(new Document((Element)xml.detach()));
		Source srcSheet = new StreamSource(styleSheet);

		// Dear old saxon likes to yell loudly about each and every XSLT 1.0
		// stylesheet so switch it off but trap any exceptions because this
		// code is run on transformers other than saxon 
		TransformerFactory transFact = TransformerFactoryFactory.getTransformerFactory();
		transFact.setURIResolver(new JeevesURIResolver());
		try {
			transFact.setAttribute(FeatureKeys.VERSION_WARNING,false);
			transFact.setAttribute(FeatureKeys.LINE_NUMBERING,true);
			transFact.setAttribute(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION,false);
			transFact.setAttribute(FeatureKeys.RECOVERY_POLICY,Configuration.RECOVER_SILENTLY);
			// Add the following to get timing info on xslt transformations
			//transFact.setAttribute(FeatureKeys.TIMING,true);
		} catch (IllegalArgumentException e) {
		    Log.warning(Log.ENGINE, "WARNING: transformerfactory doesnt like saxon attributes!");
			//e.printStackTrace();
		} finally {
			Transformer t = transFact.newTransformer(srcSheet);
			if (params != null) {
				for (Map.Entry<String,String> param : params.entrySet()) {
					t.setParameter(param.getKey(),param.getValue());
				}
			}
			t.transform(srcXml, result);
		}
	}

	//--------------------------------------------------------------------------

    /**
     * Clears the cache used in the stylesheet transformer factory. This will only work for the GeoNetwork Caching
     * stylesheet transformer factory. This is a no-op for other transformer factories.
     */
	public static void clearTransformerFactoryStylesheetCache() {
		TransformerFactory transFact = TransformerFactory.newInstance();
		try {
			Method cacheMethod = transFact.getClass().getDeclaredMethod("clearCache", null);
			cacheMethod.invoke(transFact, new Object[0]);
		} catch (Exception e) {
			Log.error(Log.ENGINE, "Failed to find/invoke clearCache method - continuing ("+e.getMessage()+")");
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
	 factory.setURIResolver(new JeevesURIResolver());
   Source xslt = new StreamSource(new File(styleSheetPath));
		try {
			factory.setAttribute(FeatureKeys.VERSION_WARNING,false);
			factory.setAttribute(FeatureKeys.LINE_NUMBERING,true);
			factory.setAttribute(FeatureKeys.RECOVERY_POLICY,Configuration.RECOVER_SILENTLY);
		} catch (IllegalArgumentException e) {
		    Log.warning(Log.ENGINE, "WARNING: transformerfactory doesnt like saxon attributes!");
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

   }
   finally {
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

    /**
     *
     * @param data
     * @return
     */
	public static String getString(DocType data)
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		return outputter.outputString(data);
	}

	//---------------------------------------------------------------------------

    /**
     *
     * @param data
     * @return
     */
	public static String getString(Document data)
	{
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		return outputter.outputString(data);
	}

	//---------------------------------------------------------------------------

    /**
     * Creates and prepares an XPath element - simple xpath (like "a/b/c").
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
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
	
    /**
     * Evaluates an XPath expression on an document and returns Elements.
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
	public static List<?> selectDocumentNodes(Element xml, String xpath, List<Namespace> theNSs) throws JDOMException {
		XPath xp = XPath.newInstance (xpath);
		for (Namespace ns : theNSs ) {
			xp.addNamespace(ns);
		}
        xml = (Element)xml.clone();
        Document document = new Document(xml);
        return xp.selectNodes(document);		
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

    /**
     * Evaluates an XPath expression on an element and returns number result.
     *
     * @param xml
     * @param xpath
     * @return
     * @throws JDOMException
     */
	public static Number selectNumber(Element xml, String xpath) throws JDOMException {
		return selectNumber(xml, xpath, new ArrayList<Namespace>());
 	}

	//---------------------------------------------------------------------------

    /**
     * Evaluates an XPath expression on an element and returns number result.
     *
     * @param xml
     * @param xpath
     * @param theNSs
     * @return
     * @throws JDOMException
     */
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

    /**
     * Validates an XML document using the hints in the DocType (DTD validation) or schemaLocation attribute hint.
     *
     * @param doc
     * @throws Exception
     */
	public synchronized static void validate(Document doc) throws Exception {
		if (doc.getDocType() != null) { // assume DTD validation
			SAXBuilder builder = getSAXBuilder(true);	
			Document document = builder.build(new StringReader(getString(doc))); 
		} 

		Element xml = doc.getRootElement();
		if (xml != null) {  // try XSD validation
			String schemaLoc = xml.getAttributeValue("schemaLocation", xsiNS);
			if (schemaLoc == null || schemaLoc.equals("")) {
				throw new IllegalArgumentException("XML document missing/blank schemaLocation hints or DocType dtd - cannot validate");
			}
			validate(xml);
		} else {
			throw new IllegalArgumentException("XML document is missing root element - cannot validate");
		}
	}

	//---------------------------------------------------------------------------

    /**
     * Validates an XML document using the hints in the schemaLocation attribute.
     *
     * @param xml
     * @throws Exception
     */
	public synchronized static void validate(Element xml) throws Exception {
		Schema schema = factory().newSchema();
		ErrorHandler eh = new ErrorHandler();
		validateRealGuts(schema, xml, eh);
		if (eh.errors()) {
			Element xsdXPaths = eh.getXPaths();
			throw new XSDValidationErrorEx("XSD Validation error(s):\n"+getString(xsdXPaths), xsdXPaths);
		}
	}

	//---------------------------------------------------------------------------

    /**
     * Validates an xml document with respect to an xml schema described by .xsd file path.
     *
     * @param schemaPath
     * @param xml
     * @throws Exception
     */
	public static void validate(String schemaPath, Element xml) throws Exception
	{
		Element xsdXPaths = validateInfo(schemaPath,xml);
		if (xsdXPaths != null && xsdXPaths.getContent().size() > 0) throw new XSDValidationErrorEx("XSD Validation error(s):\n"+getString(xsdXPaths), xsdXPaths);
	}

	//---------------------------------------------------------------------------

    /**
     * Validates an xml document with respect to schemaLocation hints.
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public static Element validateInfo(Element xml) throws Exception
	{
		ErrorHandler eh = new ErrorHandler();
		Schema schema = factory().newSchema();
		validateRealGuts(schema, xml, eh);
		if (eh.errors()) {
			return eh.getXPaths();
		} else {
			return null;
		}
	}

	//---------------------------------------------------------------------------

    /**
     * Validates an xml document with respect to schemaLocation hints using supplied error handler.
     *
     * @param xml
     * @param eh
     * @return
     * @throws Exception
     */
	public static Element validateInfo(Element xml, ErrorHandler eh) throws Exception
	{
		Schema schema = factory().newSchema();
		validateRealGuts(schema, xml, eh);
		if (eh.errors()) {
			return eh.getXPaths();
		} else {
			return null;
		}
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

	//---------------------------------------------------------------------------

    /**
     * Called by all validation methods to do the real guts of the validation job.
     * @param schema
     * @param xml
     * @param eh
     * @throws Exception
     */
	private static void validateRealGuts(Schema schema, Element xml, ErrorHandler eh) throws Exception {

		Resolver resolver = ResolverWrapper.getInstance();

		ValidatorHandler vh = schema.newValidatorHandler();
		vh.setResourceResolver(resolver.getXmlResolver());
		vh.setErrorHandler(eh);

		SAXOutputter so = new SAXOutputter(vh);
		eh.setSo(so);

		so.output(xml);
	} 

	private static SchemaFactory factory() {
		return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	}


}
