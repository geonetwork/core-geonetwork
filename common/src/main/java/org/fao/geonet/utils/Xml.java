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

package org.fao.geonet.utils;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import net.sf.saxon.Configuration;
import net.sf.saxon.FeatureKeys;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
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
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

import static org.fao.geonet.Constants.ENCODING;

//=============================================================================

/**
 *  General class of useful static methods.
 */
public final class Xml
{

	public static final Namespace xsiNS = Namespace.getNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
    static final NioPathAwareEntityResolver PATH_RESOLVER = new NioPathAwareEntityResolver();

    //--------------------------------------------------------------------------

    /**
     *
     * @param validate
     * @return
     */
	private static SAXBuilder getSAXBuilder(boolean validate, Path base) {
		SAXBuilder builder = getSAXBuilderWithPathXMLResolver(validate, base);
        Resolver resolver = ResolverWrapper.getInstance();
        builder.setEntityResolver(resolver.getXmlResolver());
        return builder;
	}

    private static SAXBuilder getSAXBuilderWithPathXMLResolver(boolean validate, Path base) {
        SAXBuilder builder = new SAXBuilder(validate);
        builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        NioPathHolder.setBase(base);
        builder.setEntityResolver(Xml.PATH_RESOLVER);
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
        Path path = pathFromUrl(url);
        SAXBuilder builder = getSAXBuilderWithPathXMLResolver(false, path);//new SAXBuilder();
		Document   jdoc    = builder.build(url);

		return (Element) jdoc.getRootElement().detach();
	}

    protected static Path pathFromUrl(URL url) {
        Path path = null;
        try {
            path = IO.toPath(url.toURI());
        } catch (Exception e) {
            // not a path
        }
        return path;
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
			connection.setRequestProperty("Content-Length", "" + Integer.toString(getString(xmlQuery).getBytes(ENCODING).length));
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setDoOutput(true);
			PrintStream out = new PrintStream(connection.getOutputStream(), true, ENCODING); 
			out.print(getString(xmlQuery));
			out.close();

            Path path = pathFromUrl(url);
			SAXBuilder builder = getSAXBuilderWithPathXMLResolver(false, path);//new SAXBuilder();
			Document   jdoc    = builder.build(connection.getInputStream());

			result = (Element)jdoc.getRootElement().detach();
		} catch (Exception e) {
		    Log.error(Log.ENGINE, "Error loading URL " + url.getPath() + " .Threw exception "+e);
			e.printStackTrace();
		}
		return result;
	}

	//--------------------------------------------------------------------------

    public static Element loadFile(Path file) throws JDOMException, NoSuchFileException {
        try {
            SAXBuilder builder = getSAXBuilderWithPathXMLResolver(false, file); //new SAXBuilder();

            String convert = System.getProperty("jeeves.filecharsetdetectandconvert", "");

            // detect charset and convert if required
            if (convert.equals("enabled")) {
                byte[] content = convertFileToUTF8ByteArray(file);
                return loadStream(new ByteArrayInputStream(content));

                // no charset detection and conversion allowed
            } else {
                try (InputStream in = Files.newInputStream(file)) {
                    Document jdoc = builder.build(in);
                    return (Element) jdoc.getRootElement().detach();
                }
            }
        } catch (JDOMException e) {
            throw new JDOMException("Error occurred while trying to load an xml file: " + file, e);
        } catch (NoSuchFileException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Error occurred while trying to load an xml file: " + file, e);
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

	public synchronized static byte[] convertFileToUTF8ByteArray(Path file) throws IOException {
        try (DataInputStream inStream = new DataInputStream(Files.newInputStream(file))) {
            byte[] buf = new byte[(int) Files.size(file)];
            int nrRead = inStream.read(buf);

            UniversalDetector detector = new UniversalDetector(null);
            detector.handleData(buf, 0, nrRead);
            detector.dataEnd();

            String encoding = detector.getDetectedCharset();
            detector.reset();
            if (encoding != null) {
                if (!encoding.equals(ENCODING)) {
                    Log.error(Log.JEEVES, "Detected character set " + encoding + ", converting to UTF-8");
                    return convertByteArrayToUTF8ByteArray(buf, encoding);
                }
            }
            return buf;
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

		Charset utf8 = Charset.forName(ENCODING);
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
		SAXBuilder builder = getSAXBuilderWithPathXMLResolver(validate, null); // oasis catalogs are used
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
		SAXBuilder builder = getSAXBuilderWithPathXMLResolver(false, null); //new SAXBuilder();
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
	public static Element transform(Element xml, Path styleSheetPath) throws Exception {
        JDOMResult resXml = new JDOMResult();
        transform(xml, styleSheetPath, resXml, null);
        return (Element)resXml.getDocument().getRootElement().detach();
    }
	//--------------------------------------------------------------------------

    /**
     * Transforms an xml tree into another using a stylesheet on disk and pass parameters.
     *
     *
     * @param xml
     * @param styleSheetPath
     * @param params
     * @return
     * @throws Exception
     */
	public static Element transform(Element xml, Path styleSheetPath, Map<String, Object> params) throws Exception
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
	public static void transform(Element xml, Path styleSheetPath, OutputStream out) throws Exception
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
		transform(xml, IO.toPath(styleSheetPath), result, null);
	}


    public static Element transformWithXmlParam(Element xml, String styleSheetPath, String xmlParamName, String xmlParam) throws Exception
    {
        JDOMResult resXml = new JDOMResult();

        File styleSheet = new File(styleSheetPath);
        Source srcSheet = new StreamSource(styleSheet);
        transformWithXmlParam(xml, srcSheet, resXml, xmlParamName, xmlParam);

        return (Element)resXml.getDocument().getRootElement().detach();
    }

    /**
     * Transforms an xml tree putting the result to a stream. Sends xml snippet as parameter.
     * @param xml
     * @param xslt
     * @param result
     * @param xmlParamName
     * @param xmlParam
     * @throws Exception
     */
    public static void transformWithXmlParam(Element xml, Source xslt, Result result,
                                             String xmlParamName, String xmlParam) throws Exception {
        Source srcXml   = new JDOMSource(new Document((Element)xml.detach()));

        // Dear old saxon likes to yell loudly about each and every XSLT 1.0
        // stylesheet so switch it off but trap any exceptions because this
        // code is run on transformers other than saxon
        TransformerFactory transFact;
        transFact = TransformerFactoryFactory.getTransformerFactory();

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
            if (xmlParam != null) {
                t.setParameter(xmlParamName, new StreamSource(new StringReader(xmlParam)));
            }
            t.transform(srcXml, result);
        }
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

         boolean isFile;
         try {
             final Path file = Paths.get(new URI(s.getSystemId()));
             isFile = Files.isRegularFile(file);
         } catch (Exception e) {
             isFile = false;
         }

        // If resolver has a blank XSL file use it to replace
        // resolved file that doesn't exist...
        String blankXSLFile = resolver.getBlankXSLFile();
        if (blankXSLFile != null && s.getSystemId().endsWith(".xsl") && !isFile) {
            try {
                if(Log.isDebugEnabled(Log.XML_RESOLVER)) {
                    Log.debug(Log.XML_RESOLVER, "  Check if exist " + s.getSystemId());
                }

                Path f;
                f = resolvePath(s);
                if(Log.isDebugEnabled(Log.XML_RESOLVER))
                    Log.debug(Log.XML_RESOLVER, "Check on "+f+" exists returned: "+Files.exists(f));
                // If the resolved resource does not exist, set it to blank file path to not trigger FileNotFound Exception

                if (!Files.exists(f)) {
                    if(Log.isDebugEnabled(Log.XML_RESOLVER)) {
                        Log.debug(Log.XML_RESOLVER, "  Resolved resource " + s.getSystemId() + " does not exist. blankXSLFile returned instead.");
                    }
                    s.setSystemId(blankXSLFile);
                } else {
                    s.setSystemId(f.toUri().toASCIIString());
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

    protected static Path resolvePath(Source s) throws URISyntaxException {
        Path f;
        final String systemId = s.getSystemId().replaceAll("%5C", "/");
        try {
            f = IO.toPath(new URI(systemId));
        } catch (FileSystemNotFoundException e) {
            f = IO.toPath(systemId);
        }
        return f;
    }

    //--------------------------------------------------------------------------

    /**
     * Transforms an xml tree putting the result to a stream with optional parameters.
     *
     *
     * @param xml
     * @param styleSheetPath
     * @param result
     * @param params
     * @throws Exception
     */
	public static void
    transform(Element xml, Path styleSheetPath, Result result, Map<String, Object> params) throws Exception
	{
        NioPathHolder.setBase(styleSheetPath);
		Source srcXml   = new JDOMSource(new Document((Element)xml.detach()));
        try (InputStream in = Files.newInputStream(styleSheetPath)) {
            Source srcSheet = new StreamSource(in, styleSheetPath.toUri().toASCIIString());

            // Dear old saxon likes to yell loudly about each and every XSLT 1.0
            // stylesheet so switch it off but trap any exceptions because this
            // code is run on transformers other than saxon
            TransformerFactory transFact = TransformerFactoryFactory.getTransformerFactory();
            transFact.setURIResolver(new JeevesURIResolver());
            try {
                transFact.setAttribute(FeatureKeys.VERSION_WARNING, false);
                transFact.setAttribute(FeatureKeys.LINE_NUMBERING, true);
                transFact.setAttribute(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, false);
                transFact.setAttribute(FeatureKeys.RECOVERY_POLICY, Configuration.RECOVER_SILENTLY);
                // Add the following to get timing info on xslt transformations
                //transFact.setAttribute(FeatureKeys.TIMING,true);
            } catch (IllegalArgumentException e) {
                Log.warning(Log.ENGINE, "WARNING: transformerfactory doesnt like saxon attributes!");
                //e.printStackTrace();
            } finally {
                transFact.setURIResolver(new JeevesURIResolver());
                Transformer t = transFact.newTransformer(srcSheet);
                if (params != null) {
                    for (Map.Entry<String, Object> param : params.entrySet()) {
                        t.setParameter(param.getKey(), param.getValue());
                    }
                }
                t.transform(srcXml, result);
            }
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
			Class<?> class1 = transFact.getClass();
            Method cacheMethod = class1.getDeclaredMethod("clearCache");
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

   public static Path transformFOP(Path uploadDir, Element xml, String styleSheetPath)
           throws Exception {
       Path file = uploadDir.resolve(UUID.randomUUID().toString() + ".pdf");

   // Step 1: Construct a FopFactory
   // (reuse if you plan to render multiple documents!)
   FopFactory fopFactory = FopFactory.newInstance();
   
   // Step 2: Set up output stream.
   // Note: Using BufferedOutputStream for performance reasons

   try (OutputStream out = Files.newOutputStream(file);
        OutputStream bufferedOut = new BufferedOutputStream(out)) {
       // Step 3: Construct fop with desired output format
       Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, bufferedOut);

       // Step 4: Setup JAXP using identity transformer
       TransformerFactory factory = TransformerFactoryFactory.getTransformerFactory();
       factory.setURIResolver(new JeevesURIResolver());
       Source xslt = new StreamSource(new File(styleSheetPath));
       try {
           factory.setAttribute(FeatureKeys.VERSION_WARNING, false);
           factory.setAttribute(FeatureKeys.LINE_NUMBERING, true);
           factory.setAttribute(FeatureKeys.RECOVERY_POLICY, Configuration.RECOVER_SILENTLY);
       } catch (IllegalArgumentException e) {
           Log.warning(Log.ENGINE, "WARNING: transformerfactory doesnt like saxon attributes!");
           //e.printStackTrace();
       } finally {
           Transformer transformer = factory.newTransformer(xslt);

           // Step 5: Setup input and output for XSLT transformation
           // Setup input stream
           Source src = new JDOMSource(new Document((Element) xml.detach()));

           // Resulting SAX events (the generated FO) must be piped through to
           // FOP
           Result res = new SAXResult(fop.getDefaultHandler());

           // Step 6: Start XSLT transformation and FOP processing
           transformer.transform(src, res);
       }

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
     * Converts an xml element to JSON
     * 
     * @param xml the XML element
     * @return the JSON response
     * 
     * @throws IOException
     */
    public static String getJSON (Element xml) throws IOException {
        return Xml.getJSON(Xml.getString(xml));
    }
    /**
     * Converts an xml string to JSON
     * 
     * @param xml the XML element
     * @return the JSON response
     * 
     * @throws IOException
     */
    public static String getJSON (String xml) throws IOException {
        XMLSerializer xmlSerializer = new XMLSerializer();
        
        // Disable type hints. When enable, a type attribute in the root 
        // element will throw NPE.
        // http://sourceforge.net/mailarchive/message.php?msg_id=27646519
        xmlSerializer.setTypeHintsEnabled(false);
        xmlSerializer.setTypeHintsCompatibility(false);
        JSON json = xmlSerializer.read(xml);
        return json.toString(2);
    }
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

    /**
     * Search in metadata all matching element for the filter
     * and return a list of uuid separated by or to be used in a
     * search on uuid. Extract uuid from matched element if
     * elementName is null or from the elementName child.
     *
     * @param element
     * @param elementFilter Filter to get element descendants
     * @param elementName   Child element to get value from. If null, filtered element value is returned
     * @param elementNamespace
     * @param attributeName Attribute name to get value from. If null, TODO: improve
     * @return
     */
    public static Set<String> filterElementValues(Element element,
                                                     ElementFilter elementFilter,
                                                     String elementName,
                                                     Namespace elementNamespace,
                                                     String attributeName) {
        @SuppressWarnings("unchecked")
        Iterator<Element> i = element.getDescendants(elementFilter);
        Set<String> values = new HashSet<String>();
        boolean first = true;
        while (i.hasNext()) {
            Element e = i.next();
            String uuid = elementName == null && attributeName == null?
                    e.getText() :
                    (attributeName == null ?
                            e.getChildText(elementName, elementNamespace) :
                            e.getAttributeValue(attributeName)
                    );
            if (uuid != null && !uuid.isEmpty()) {
                values.add(uuid);
            }
        }
        return values;
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
				String xpath = org.fao.geonet.utils.XPath.getXPath(elem);
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
			SAXBuilder builder = getSAXBuilder(true, null);
			builder.build(new StringReader(getString(doc))); 
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
	public static void validate(Path schemaPath, Element xml) throws Exception
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
	public static Element validateInfo(Path schemaPath, Element xml) throws Exception
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
	public static Element validateInfo(Path schemaPath, Element xml, ErrorHandler eh)
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
	private static void validateGuts(Path schemaPath, Element xml, ErrorHandler eh) throws Exception {
        PathStreamSource schemaFile = new PathStreamSource(schemaPath);
        schemaFile.setSystemId(schemaPath.toUri().toASCIIString());

        final SchemaFactory factory = factory();
        NioPathHolder.setBase(schemaPath);
        Resolver resolver = ResolverWrapper.getInstance();
        factory.setResourceResolver(resolver.getXmlResolver());
        Schema schema = factory.newSchema(schemaFile);
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


    /**
     * Create and XPath expression for identified Element.
     *
     * @param element element within the xml to find an XPath for.
     *
     * return xpath or null if there was an error.
     */
    public static String getXPathExpr(Content element) {
        StringBuilder builder = new StringBuilder();
        if (!doCreateXpathExpr(element, builder)) {
            return null;
        } else {
            return builder.toString();
        }
    }

    /**
     * Create and XPath expression for identified Element.
     *
     * @param attribute element within the xml to find an XPath for.
     *
     * return xpath or null if there was an error.
     */
    public static String getXPathExpr(Attribute attribute) {
        StringBuilder builder = new StringBuilder();
        if (!doCreateXpathExpr(attribute, builder)) {
            return null;
        } else {
            return builder.toString();
        }
    }

    private static boolean doCreateXpathExpr(Object content, StringBuilder builder) {
        if (builder.length() > 0) {
            builder.insert(0, "/");
        }

        Element parentElement;
        if (content instanceof Element) {
            Element element = (Element) content;
            final List<Attribute> attributes = element.getAttributes();
            doCreateAttributesXpathExpr(builder, attributes);
            final String textTrim = element.getTextTrim();
            if (!textTrim.isEmpty()) {
                boolean addToCondition = builder.length() > 0 && builder.charAt(0) == '[';

                if (!addToCondition) {
                    builder.insert(0, "']");
                } else {
                    builder.deleteCharAt(0);
                    builder.insert(0, "' and ");
                }

                builder.insert(0, textTrim).insert(0, "[normalize-space(text()) = '");

            }
            builder.insert(0, element.getName());
            if (element.getNamespacePrefix() != null && !element.getNamespacePrefix().trim().isEmpty()) {
                builder.insert(0, ':').insert(0, element.getNamespacePrefix());
            }
            parentElement = element.getParentElement();
        } else if (content instanceof Text) {
            final Text text = (Text) content;
            builder.insert(0, "text()");
            parentElement = text.getParentElement();
        } else if (content instanceof Attribute) {
            Attribute attribute = (Attribute) content;
            builder.insert(0, attribute.getName());
            if (attribute.getNamespacePrefix() != null && !attribute.getNamespacePrefix().trim().isEmpty()) {
                builder.insert(0, ':').insert(0, attribute.getNamespacePrefix());
            }
            builder.insert(0, '@');
            parentElement = attribute.getParent();
        } else {
            parentElement = null;
        }

        if (parentElement != null && parentElement.getParentElement() != null) {
            return doCreateXpathExpr(parentElement, builder);
        }
        return true;
    }

    private static void doCreateAttributesXpathExpr(StringBuilder builder, List<Attribute> attributes) {
        if (!attributes.isEmpty()) {
            StringBuilder attBuilder = new StringBuilder("[");
            for (Attribute attribute : attributes) {
                if (attBuilder.length() > 1) {
                    attBuilder.append(" and ");
                }
                attBuilder.append('@');
                if (attribute.getNamespacePrefix() != null && !attribute.getNamespacePrefix().trim().isEmpty()) {
                    attBuilder.append(attribute.getNamespacePrefix()).append(':');
                }
                attBuilder.append(attribute.getName()).append(" = '").append(attribute.getValue()).append('\'');
            }
            attBuilder.append("]");

            builder.insert(0, attBuilder);
        }
    }

}
