package org.fao.geonet.kernel.harvest.harvester.cgp;

import jeeves.exceptions.BadSoapResponseEx;
import jeeves.exceptions.BadXmlResponseEx;
import jeeves.interfaces.Logger;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Wrapper class for SOAP CGP-client requests.
 *
 * @author Just van den Broecke
 */
public class CGPRequest extends SOAPRequest
{
	public static final Namespace NAMESPACE_GCH = Namespace.getNamespace("gch", "http://www.geocat.ch/2003/05/gateway/header");
	public static final Namespace NAMESPACE_GCQ = Namespace.getNamespace("gcq", "http://www.geocat.ch/2003/05/gateway/query");
	public static final Namespace NAMESPACE_GM03SMALL = Namespace.getNamespace("gm03s", "http://www.geocat.ch/2003/05/gateway/GM03Small");
	public static final Namespace NAMESPACE_GM03COMPREHENSIVE = Namespace.getNamespace("gm03c", "http://www.geocat.ch/2003/05/gateway/GM03Comprehensive");
	public static final Namespace NAMESPACE_GM03CORE = Namespace.getNamespace("gm03c", "http://www.geocat.ch/2003/05/gateway/GM03Core");
	public static final Namespace NAMESPACE_GML = Namespace.getNamespace("gml", "http://www.geocat.ch/2003/05/gateway/GML");

	public static final String CGP_VERSION = "1.0";

	/**
	 * Query request template for records using criterium (substitute symbol CRITERIA).
	 */
	public static final String QUERY_REQ =
			"<gcq:catalogGatewayRequest xmlns:gcq=\"http://www.geocat.ch/2003/05/gateway/query\">" +
					"  <gcq:queryRequest> " +
					"    CRITERIA  " +
					"    <gcq:format>   " +
					"       <gcq:profile>GM03Small</gcq:profile>" +
					"       <gcq:order/>   " +
					"    </gcq:format>   " +
					"  </gcq:queryRequest> " +
					"</gcq:catalogGatewayRequest> ";


	/**
	 * Presentation request template for single record by object id (substitute symbol OBJECTID).
	 */
	public static final String PRESENTATION_REQ =
			"<gcq:catalogGatewayRequest xmlns:gcq=\"http://www.geocat.ch/2003/05/gateway/query\">" +
					"  <gcq:presentationRequest> " +
					"    <gcq:get>OBJECTID</gcq:get>  " +
					"    <gcq:format>   " +
					"       <gcq:profile>GM03Comprehensive</gcq:profile>" +
					"       <gcq:order/>" +
					"    </gcq:format>   " +
					"  </gcq:presentationRequest> " +
					"</gcq:catalogGatewayRequest> ";

	/**
	 * Construct with URL of CGP SOAP messaging service.
	 *
	 * @param urlStr full URL to SOAP service
	 */
	public CGPRequest(String urlStr) throws MalformedURLException
	{
		super(urlStr);
	}

	/**
	 * Sends a CGP SOAP request and returns an CGP SOAP document response.
	 *
	 * @param cgpReqElm CG request element (gcq:catalogGatewayRequest)
	 */
	public Document execute(Element cgpReqElm) throws SOAPFaultEx, JDOMException, IOException, BadXmlResponseEx, BadSoapResponseEx
	{
		// Set content of SOAP Header and Body
		setHeaderContent(createHeaderContent());
		setBodyContent(cgpReqElm);

		return super.execute();
	}

	/**
	 * Get single catalog record (CGP Presentation req).
	 *
	 * @param objectId object id e.g. "xMetadatax2505"
	 * @return TRANSFER Element in GM03Comprehensive format.
	 */
	public Element getEntry(String objectId) throws SOAPFaultEx, JDOMException, IOException, BadXmlResponseEx, BadSoapResponseEx
	{
		if (objectId == null)
		{
			return null;
		}

		String reqString = PRESENTATION_REQ.replaceAll("OBJECTID", objectId);
		Document resultDoc = execute(Xml.loadString(reqString, false));
		if (resultDoc == null)
		{
			return null;
		}

		return (Element) XP_TRANSFER_GM03COMPREHENSIVE.selectSingleNode(resultDoc);
	}

	/**
	 * Query all records using specified CGP Query criteria.
	 *
	 * @return List of Elements in GM03Small format.
	 */
	public List<Element> query(CGPQueryCriteria criteria) throws SOAPFaultEx, JDOMException, IOException, BadXmlResponseEx, BadSoapResponseEx
	{
		String reqString = QUERY_REQ.replaceAll("CRITERIA", criteria.toString());
		Document resultDoc = execute(Xml.loadString(reqString, false));
		return XP_MD_GM03SMALL.selectNodes(resultDoc);
	}

	/**
	 * Construct mandatory CGP SOAP-header content Element.
	 *
	 * @return gch:requestID Element
	 */
	private Element createHeaderContent()
	{
		Element requestID = new Element("requestID", NAMESPACE_GCH);

		Element version = new Element("version", NAMESPACE_GCH);

		// HACK: the server http://www.asit.vd.ch/xml/geocat/geocat.asp expects 0.99 version
		// version.setText(getHost().indexOf(".asit.") != -1 ? "0.99" : CGP_VERSION);
		version.setText(CGP_VERSION);
		requestID.addContent(version);

		Element sendingNodeId = new Element("sendingNodeId", NAMESPACE_GCH);
		sendingNodeId.setText("geonet.geocat.ch");
		requestID.addContent(sendingNodeId);

		Element referenceId = new Element("referenceId", NAMESPACE_GCH);
		referenceId.setText(UUID.randomUUID().toString());
		requestID.addContent(referenceId);

		Element messageId = new Element("messageId", NAMESPACE_GCH);
		messageId.setText(UUID.randomUUID().toString());
		requestID.addContent(messageId);

		Element dateAndTime = new Element("dateAndTime", NAMESPACE_GCH);
		dateAndTime.setText(DATE_FORMAT.format(calendar.getTime()));
		requestID.addContent(dateAndTime);
		return requestID;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	/**
	 * Pre-compiled XPath objects for GM03 Element selection from SOAP Document.
	 */
	private static XPath XP_MD_GM03SMALL, XP_TRANSFER_GM03COMPREHENSIVE, XP_TRANSFER_GM03CORE;
	private static Logger logger;

	static
	{
		logger = Log.createLogger(Geonet.HARVESTER);

		try
		{
			// Create compiled XPath instances for reusability
			XP_MD_GM03SMALL = XPath.newInstance("//gm03s:MD_Metadata");
			XP_MD_GM03SMALL.addNamespace(NAMESPACE_GM03SMALL);
			XP_TRANSFER_GM03COMPREHENSIVE = XPath.newInstance("//gm03c:TRANSFER");
			XP_TRANSFER_GM03COMPREHENSIVE.addNamespace(NAMESPACE_GM03COMPREHENSIVE);
			XP_TRANSFER_GM03CORE = XPath.newInstance("//gm03c:TRANSFER");
			XP_TRANSFER_GM03CORE.addNamespace(NAMESPACE_GM03CORE);
		} catch (JDOMException jde)
		{
			// Very unlikely...
			logger.error("Error creating XPATH instances");
		}
	}

	private static Calendar calendar = Calendar.getInstance();
	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
}
