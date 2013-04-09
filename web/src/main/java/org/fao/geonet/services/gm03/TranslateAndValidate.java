package org.fao.geonet.services.gm03;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import jeeves.server.context.ServiceContext;
import jeeves.utils.TransformerFactoryFactory;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;

import org.apache.commons.lang.ArrayUtils;
import org.fao.geonet.util.XslUtil;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TranslateAndValidate {
	public static final SchemaFactory SCHEMA_FACTORY = SchemaFactory
			.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory
			.newInstance();

	public File outputDir = new File("output");

    public boolean debug = false;

	public static void main(String[] args) throws SAXException, IOException,
			TransformerConfigurationException {
		final String xslFilename = args[0];
		final String schemaFilename = args[1];
		final String[] xmlFilenames = (String[]) ArrayUtils.subarray(args, 2,
				args.length);

		new TranslateAndValidate().run(xslFilename, schemaFilename,
				xmlFilenames);
	}

    /**
     * 
     * @param xslFilename
     * @param schemaFilename
     *            if null, does not validate transformed document.
     * @param xmlFilenames
     * @throws SAXException
     * @throws TransformerConfigurationException
     * @throws IOException
     */
    public void run(String xslFilename, String schemaFilename,
            String[] xmlFilenames) throws SAXException,
            TransformerConfigurationException, IOException {

        // Compile the schema.
        // Here the schema is loaded from a java.io.File, but you could use
        // a java.net.URL or a javax.xml.transform.Source instead.
        File schemaLocation = new File(schemaFilename);
        Schema schema = null;

        // Only validate if a schemafile is provided
        if (schemaFilename != null)
            schema = SCHEMA_FACTORY.newSchema(schemaLocation);

        Transformer xslt = TRANSFORMER_FACTORY.newTransformer(new StreamSource(
                xslFilename));

        for (int i = 0; i < xmlFilenames.length; i++) {
            String xmlFilename = xmlFilenames[i];
            final String xmlFilenameOnly = new File(xmlFilename).getName();
            final StreamSource source = new StreamSource(xmlFilename);
            
            StringBuffer doc = translate(xmlFilename, source, xslt, false, null);
            saveFile(doc, "result_" + xmlFilenameOnly);
            if(debug ) {
                saveFile(translate(xmlFilename, source, xslt, true, null), "intermediate_"
                    + xmlFilenameOnly);
            }
            if (schema != null)
                validate(schema, xmlFilename, doc, xslt);
        }
    }
    /**
     * 
     * @param xslFilename
     * @param schemaFilename
     *            if null, does not validate transformed document.
     * @param xmlFilenames
     * @throws SAXException
     * @throws TransformerConfigurationException
     * @throws IOException
     */
    public void run(String xslFilename, String schemaFilename,
            Source source) throws SAXException,
            TransformerConfigurationException, IOException {

        // Compile the schema.
        // Here the schema is loaded from a java.io.File, but you could use
        // a java.net.URL or a javax.xml.transform.Source instead.
        Schema schema = null;
        if (schemaFilename != null) {
            File schemaLocation = new File(schemaFilename);
    
            // Only validate if a schemafile is provided
            schema = SCHEMA_FACTORY.newSchema(schemaLocation);
        }

        Transformer xslt = TRANSFORMER_FACTORY.newTransformer(new StreamSource(
                xslFilename));

        StringBuffer doc = translate("unknown", source, xslt, false, null);
        saveFile(doc, "result_");
        if(debug ) {
            saveFile(translate("unknown", source, xslt, true, null), "intermediate_");
        }
        if (schema != null)
            validate(schema, "", doc, xslt);
    }

	private StringBuffer translate(String xmlFilename, Source source, Transformer xslt,
			boolean debug, String uuid) {
		final StringWriter result = new StringWriter();
		StreamResult transformed = new StreamResult(result);
		xslt.setParameter("DEBUG", debug ? "1" : "0");
		if(uuid != null) {
			xslt.setParameter("uuid", uuid);
		}
		try {
			xslt.transform(source, transformed);
		} catch (TransformerException ex) {
			if(xmlFilename != null) {
				System.out.println("Errors in " + xmlFilename + ":");
			} else {
				System.out.println("Errors:");
			}
			ex.printStackTrace(System.out);
			throw new AssertionError(ex);
		}
		return result.getBuffer();
	}

	private void validate(Schema schema, final String xmlFilename,
			StringBuffer doc, Transformer xslt) throws IOException {

		Source source = new StreamSource(new StringReader(doc.toString()),
				"errorResult.xml");

		// 3. Get a validator from the schema.
		Validator validator = schema.newValidator();
		final MyErrorHandler errorHandler = new MyErrorHandler(xmlFilename);
		validator.setErrorHandler(errorHandler);

		try {
			validator.validate(source);
			if (errorHandler.hasErrors()) {
				errorHandler.printError(System.out);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				errorHandler.printError(new PrintStream(out));
				if(xmlFilename != null) {
					generateTempFiles(doc, xslt, xmlFilename);
				}
				throw new AssertionError(out.toString());
			} else {
				// System.out.println(xmlFilename + " is valid.");
			}
		} catch (SAXException ex) {
			if(xmlFilename != null) {
				generateTempFiles(doc, xslt, xmlFilename);
			}
			errorHandler.throwErrors();
		}
	}

	public void validate(Schema schema, final String xmlFilename)
			throws IOException {

		Source source = new StreamSource(new FileReader(xmlFilename));

		// 3. Get a validator from the schema.
		Validator validator = schema.newValidator();
		final MyErrorHandler errorHandler = new MyErrorHandler(xmlFilename);
		validator.setErrorHandler(errorHandler);

		try {
			validator.validate(source);
			if (errorHandler.hasErrors()) {
				errorHandler.printError(System.out);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				errorHandler.printError(new PrintStream(out));
				throw new AssertionError(out.toString());
			} else {
				// System.out.println(xmlFilename + " is valid.");
			}
		} catch (SAXException ex) {
			errorHandler.printError(System.out);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			errorHandler.printError(new PrintStream(out));
			throw new AssertionError(out.toString());
		}
	}

	private void generateTempFiles(StringBuffer doc, Transformer xslt,
			String xmlFilename) throws IOException {
		saveFile(doc, "errorResult.xml");
       final StreamSource source = new StreamSource(xmlFilename);

		saveFile(translate(xmlFilename, source, xslt, true, null), "errorDebug.xml");
	}

	protected void saveFile(StringBuffer doc, String fileName) throws IOException {
		outputDir.mkdirs();
		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(outputDir, fileName)), "UTF8"));
		writer.write(doc.toString());
		writer.close();
	}

	public static class MyErrorHandler implements ErrorHandler {
		private final String xmlFilename;
		private List<SAXParseException> exceptions = new ArrayList<SAXParseException>();

		public MyErrorHandler(String xmlFilename) {
			this.xmlFilename = xmlFilename;
		}

		public void warning(SAXParseException exception) throws SAXException {
			exceptions.add(exception);
		}

		public void error(SAXParseException exception) throws SAXException {
			exceptions.add(exception);
		}

		public void fatalError(SAXParseException exception) throws SAXException {
			exceptions.add(exception);
		}

		public boolean hasErrors() {
			return !exceptions.isEmpty();
		}

		public void printError(PrintStream out) {
			if (hasErrors()) {
				out.println(exceptions.size() + " errors in " + xmlFilename
						+ ":");
				for (int i = 0; i < exceptions.size(); i++) {
					SAXParseException exception = exceptions.get(i);
					out.println(exception.getLineNumber() + ";"
							+ exception.getColumnNumber() + ": "
							+ exception.getMessage());
				}
			}
		}

		public void throwErrors() {
			if (hasErrors()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream stream = new PrintStream(out);
				printError(stream);
				String msg = out.toString();

				throw new RuntimeException(msg, exceptions.get(0));
			}
		}
	}

	public void run(File gm03StyleSheet, File gm03Xsd, String[] xmlFilenames)
			throws Exception {
		run(gm03StyleSheet.getAbsolutePath(), (gm03Xsd != null ? gm03Xsd
				.getAbsolutePath() : null), xmlFilenames);

	}

	public static NodeInfo toCheBootstrap(NodeInfo doc, String uuid,
			String validate, String debugFileName, String webappDir)
			throws Exception {

    	final String SEP = File.separator;
    	StringBuilder xslFileName = new StringBuilder(SEP).
    			append("xsl").
    			append(SEP).
    			append("conversion").
    			append(SEP).
    			append("GM03to19139CHE" ).
    			append( SEP);
    	
    	String finalWebappDir;
		if(webappDir == null || webappDir.isEmpty()) {
    		finalWebappDir = ServiceContext.get().getAppPath();
    	} else {
    		finalWebappDir = webappDir;
    	}
		
		if(finalWebappDir.endsWith(SEP)) {
			xslFileName.deleteCharAt(0);
		}
		
		xslFileName.insert(0, finalWebappDir);
    	
    	String xml = XslUtil.writeXml(doc.getRoot());
    	AxisIterator iter = doc.iterateAxis(Axis.DESCENDANT, NodeKindTest.ELEMENT);
    	while(iter.moveNext()) {
    		NodeInfo next = (NodeInfo) iter.next();
    		if(next == null) {
    			break;
    		}
    		String lp = next.getLocalPart();
    		if(lp.startsWith("GM03_2_1")) {
    			// basic dir will work
    			xslFileName.append("version2_1"+SEP);
    			break;
    		} else if(lp.startsWith("GM03_2")) {
    			xslFileName.append("version2"+SEP);
    			break;
	    	} else if(lp.equals("GM03Comprehensive.Comprehensive") || lp.equals("GM03Core.Core.MD_Metadata")) {
	    		break;
	    	}
    		
    	}
    			
    			
    	if(xml.trim().isEmpty()) {
    		throw new IllegalArgumentException("XML document does not seem to be a GM03 variant");
    	}

		xslFileName.append("CHE03-to-19139.xsl");

    	Transformer xslt = TransformerFactoryFactory.getTransformerFactory().newTransformer(new StreamSource(xslFileName.toString()));
    	
		byte[] bytes = xml.getBytes("UTF-8");
		StreamSource source = new StreamSource(new ByteArrayInputStream(bytes));
    	
    	TranslateAndValidate instance = new TranslateAndValidate();
		StringBuffer result = instance.translate(debugFileName, source, xslt, false, uuid);
		if (debugFileName != null && !debugFileName.isEmpty()) {
			File outFile = new File(debugFileName);
			instance.outputDir = outFile.getParentFile();
			instance.outputDir.mkdirs();
			
			instance.saveFile(result, "result_"+outFile.getName());
			
			source = new StreamSource(new ByteArrayInputStream(bytes));
			instance.saveFile(instance.translate(null, source, xslt, true, uuid), "intermediate_"+outFile.getName());
		}
		

        if (validate != null && !validate.trim().isEmpty()) {
        	File schemaLocation = new File(validate);
            Schema schema = SCHEMA_FACTORY.newSchema(schemaLocation);
            instance.validate(schema, null, result, xslt);
        }
        
		Source xmlSource = new StreamSource(new ByteArrayInputStream(result.toString().getBytes("UTF-8")));
        return doc.getConfiguration().buildDocument(xmlSource);
    }

}