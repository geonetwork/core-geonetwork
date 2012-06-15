/**
 *
 */
package org.fao.geonet.kernel;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import jeeves.exceptions.JeevesException;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import jeeves.utils.Xml.ErrorHandler;

import org.apache.log4j.Logger;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.util.FileCopyMgr;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.DOMOutputter;

/**
 * Process metadata for validation action:
 * <ul>
 * <li>Schematron validation</li>
 * <li>Schema validation</li>
 * </ul>
 *
 */
public class Validator {
	/**
	 * Schematron cache directory. Usually web/geonetwork/schematronCache.
	 */
	private final String HTML_CACHE_DIR;

	private final Logger logger;

	public Validator(String htmlCacheDir) {
		HTML_CACHE_DIR = htmlCacheDir;
		logger = Logger.getLogger(Validator.class);
	}

	public void doValidate(MetadataSchema metadataSchema, String metadataId,
			Element metadata, String language) throws Exception {

		String schemaDir = metadataSchema.getSchemaDir();

		// XSD first...
		Element xsdXPaths = Xml.validateInfo(schemaDir + Geonet.File.SCHEMA,
				metadata);
		// TODO : could we localized XSD validation error report ?
		if (xsdXPaths != null && xsdXPaths.getContent().size() > 0) {
			Element xsd = new Element("xsderrors");
			Element idElem = new Element("id");
			idElem.setText(metadataId);
			xsd.addContent(idElem);
			throw new XSDValidationErrorEx("XSD validation errors detected",
					xsdXPaths);
		}

		// ...then schematrons
		Element schemaTronXml = getSchemaTronXmlReport(metadataSchema,
				metadata, language);
		if (schemaTronXml != null && schemaTronXml.getContent().size() > 0) {
			Element schematron = new Element("schematronerrors");
			Element idElem = new Element("id");
			idElem.setText(metadataId);
			schematron.addContent(idElem);
			throw new SchematronValidationErrorEx(
					"Schematron errors detected - see schemaTron report for "
							+ metadataId + " in htmlCache for more details",
					schematron);
		}
	}

	/**
	 *
	 * NOTE: this method assumes that enumerateTree has NOT been run on the
	 * metadata
	 *
	 * @param schema
	 * @param md
	 * @return
	 * @throws Exception
	 */
	public Element getXSDXmlReport(MetadataSchema schema, Element md)
			throws Exception {

		String schemaDir = schema.getSchemaDir();

		ErrorHandler errorHandler = new ErrorHandler();
		errorHandler.setNs(Edit.NAMESPACE);
		Element xsdErrors;
		try {
		    xsdErrors = Xml.validateInfo(schemaDir + File.separator + Geonet.File.SCHEMA,
				md, errorHandler);
		}catch (Exception e) {
		    xsdErrors = JeevesException.toElement(e);
		    return xsdErrors;
        }

		if (xsdErrors != null) {

			List<Namespace> schemaNamespaces = schema.getSchemaNS();

			// -- now get each xpath and evaluate it
			// -- xsderrors/xsderror/{message,xpath}
			List list = xsdErrors.getChildren();
			for (Object o : list) {
				Element elError = (Element) o;
				String xpath = elError.getChildText("xpath", Edit.NAMESPACE);
				String message = elError
						.getChildText("message", Edit.NAMESPACE);
				message = "\\n" + message;

				// -- get the element from the xpath and add the error message
				// to it
				Element elem = Xml.selectElement(md, xpath, schemaNamespaces);
				if (elem != null) {
					String existing = elem.getAttributeValue("xsderror",
							Edit.NAMESPACE);
					if (existing != null)
						message = existing + message;
					elem.setAttribute("xsderror", message, Edit.NAMESPACE);
				} else {
					logger
							.warn("WARNING: evaluating XPath "
									+ xpath
									+ " against metadata failed - XSD validation message: "
									+ message
									+ " will NOT be shown by the editor");
				}
			}

			return xsdErrors;
		}
		// -- no validation errors
		return xsdErrors;
	}

	/**
	 * Not used in geocat.ch. We only use XML reporting with styling made by
	 * XSL.
	 *
	 * @param schemaPath
	 * @param metadata
	 * @param metadataId
	 * @param language
	 * @return
	 * @throws Exception
	 */
	public void doSchemaTronReport(MetadataSchema metadataSchema,
			Element metadata, String metadataId, String language)
			throws Exception {
		String dirId = "SchematronReport" + metadataId;
		String outDir = HTML_CACHE_DIR + File.separator + dirId;
		String inDir = HTML_CACHE_DIR + File.separator + "schematronscripts";

		logger.debug("Schematron report (language:" + language + ")");

		// copy the schematron templates for the output report
		FileCopyMgr.copyFiles(inDir, outDir);

		// convert the JDOM document to a DOM document
		Document mdDoc = new Document(metadata);
		DOMOutputter domOut = new DOMOutputter();
		org.w3c.dom.Document mdDomDoc = domOut.output(mdDoc);

		String[] rules = metadataSchema.getSchematronRules();

		for (String rule : rules) {
			logger.debug(" - rule:" + rule);

			// set up the inputs to/output from the XSLT transformer and run it
			// xslt transformer
			String schemaTronReport = rule;
			StreamSource xsltSource = new StreamSource(new File(
					schemaTronReport));

			// output schematron-errors.html
			String fileOut = outDir + File.separator + rule + "-errors-.html";
			File fileResult = new File(fileOut);
			Source source = new DOMSource(mdDomDoc);

			try {
				Transformer xformer = TransformerFactory.newInstance()
						.newTransformer(xsltSource);
				xformer.setParameter("lang", language);
				Result result = new StreamResult(fileResult.toURI().getPath());
				xformer.transform(source, result);
			} catch (TransformerConfigurationException e) {
				System.out.println(e);
			} catch (TransformerException e) {
				System.out.println(e);
			}

			// now place anchors in the metadata xml so that schematron-report
			// can
			// show the problems with the XML

			String schemaTronAnchors = metadataSchema.getSchemaDir()
					+ File.separator + Geonet.File.SCHEMATRON_VERBID;
			StreamSource xsltAnchorSource = new StreamSource(new File(
					schemaTronAnchors));
			// output schematron-out.html
			String fileSchemaTronOut = outDir + File.separator + rule
					+ "-out.html";
			File schemaTronOut = new File(fileSchemaTronOut);
			try {
				Transformer xformer = TransformerFactory.newInstance()
						.newTransformer(xsltAnchorSource);
				xformer.setParameter("lang", language);
				Result result = new StreamResult(schemaTronOut.toURI()
						.getPath());
				xformer.transform(source, result);
			} catch (TransformerConfigurationException e) {
				System.out.println(e);
			} catch (TransformerException e) {
				System.out.println(e);
			}
		}
	}

	/**
	 * Create schematron report for each rules defined for the schema.
	 * Return all report in a XML fragment.
	 *
	 *
	 * @param mds
	 * @param metadata
	 * @param language
	 * @return
	 * @throws Exception
	 */
	public Element getSchemaTronXmlReport(MetadataSchema mds, Element metadata,
			String language) throws Exception {
		logger.debug("XML Schematron report (language:" + language + ")");

		// NOTE: this method assumes that you've run enumerateTree on the
		// metadata
		String[] rules = mds.getSchematronRules();

		Element schemaTronXmlOut = new Element("schematronerrors",
				Edit.NAMESPACE);

		for (String rule : rules) {
			logger.debug(" - rule:" + rule);

			String schemaTronXmlXslt = mds.getSchemaDir() + File.separator
					+ rule;

			// -- create a report for current rules.
			Element report = new Element("report", Edit.NAMESPACE);
			report.setAttribute("rule", rule.substring(0, rule.indexOf(".xsl")),
					Edit.NAMESPACE);

			try {
				Map<String, String> param = new HashMap<String, String>();
				param.put("lang", language);

				report.addContent(Xml.transform(metadata, schemaTronXmlXslt,
						param));

			} catch (Exception e) {
				Log.warning(Geonet.DATA_MANAGER, "WARNING: schematron xslt "
						+ schemaTronXmlXslt + " failed");
				e.printStackTrace();
			}

			// -- append report to main XML report.
			schemaTronXmlOut.addContent(report);
		}

		return schemaTronXmlOut;
	}

}
