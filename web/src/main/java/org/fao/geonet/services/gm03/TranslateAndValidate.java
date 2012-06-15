package org.fao.geonet.services.gm03;

import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
            
            StringBuffer doc = translate(xmlFilename, source, xslt, false);
            saveFile(doc, "result_" + xmlFilenameOnly);
            if(debug ) {
                saveFile(translate(xmlFilename, source, xslt, true), "intermediate_"
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

        StringBuffer doc = translate("unknown", source, xslt, false);
        saveFile(doc, "result_");
        if(debug ) {
            saveFile(translate("unknown", source, xslt, true), "intermediate_");
        }
        if (schema != null)
            validate(schema, "", doc, xslt);
    }

	private StringBuffer translate(String xmlFilename, Source source, Transformer xslt,
			boolean debug) {
		final StringWriter result = new StringWriter();
		StreamResult transformed = new StreamResult(result);
		xslt.setParameter("DEBUG", debug ? "1" : "0");
		try {
			xslt.transform(source, transformed);
		} catch (TransformerException ex) {
			System.out.println("Errors in " + xmlFilename + ":");
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
				generateTempFiles(doc, xslt, xmlFilename);
				throw new AssertionError(out.toString());
			} else {
				// System.out.println(xmlFilename + " is valid.");
			}
		} catch (SAXException ex) {
			generateTempFiles(doc, xslt, xmlFilename);
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

		saveFile(translate(xmlFilename, source, xslt, true), "errorDebug.xml");
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
}