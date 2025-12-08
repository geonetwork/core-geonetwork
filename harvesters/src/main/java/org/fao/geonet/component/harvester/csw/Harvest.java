//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.component.harvester.csw;

import com.google.common.base.Function;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.DefaultHttpRequestRetryHandler;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.CswHarvesterResponseExecutionService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.csw.services.SupportedResourceType;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.harvesting.Util;
import org.fao.geonet.util.ISOPeriod;
import org.fao.geonet.util.MailSender;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * CSW Harvest operation.
 * <p>
 * OGC 07-006: "This is the pull mechanism that 'pulls' data into the catalogue. That is, this
 * operation only references the data to be inserted or updated in the catalogue, and it is the job
 * of the catalogue service to resolve the reference, fetch that data, and process it into the
 * catalogue."
 *
 * @author heikki doeleman
 */
public class Harvest extends AbstractOperation implements CatalogService {

    static final String NAME = "Harvest";
    private ApplicationContext applicationContext;
    private String operationId = NAME;
    private Protocol protocol;

    public Harvest() {
    }

    /**
     * Returns name of this CSW operation.
     *
     * @return name
     */
    public String getName() {
        return NAME;
    }

    /**
     * Executes a CSW Harvest request, see OGC 07-006 section 10 dot 12.
     *
     * @param request        - the request
     * @param serviceContext - used everywhere in GeoNetwork
     * @return response xml
     * @throws CatalogException hmm
     */
    public Element execute(Element request, ServiceContext serviceContext) throws CatalogException {
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST)) {
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest execute, request is:\n" + Xml.getString(request));
        }
        try {
            this.applicationContext = serviceContext.getApplicationContext();
            checkService(request);
            checkVersion(request);
            String resourceType = checkResourceType(request);
            checkResourceFormat(request);
            String source = checkSource(request);

            // Define an id for the operation to be used for harvester node name identification and response handler info.
            operationId = "CSW.HarvestOperation:" + UUID.randomUUID().toString();

            // create a new harvester node
            Element node = createHarvestNode(request, resourceType, source, serviceContext);

            Element response;

            //
            // OGC 07-006 10.12.4.4 :
            // The ResponseHandler parameter is a flag that indicates how the Harvest operation should be processed by
            // a CSW server.
            //
            String responseHandler = request.getChildText("ResponseHandler", Csw.NAMESPACE_CSW);

            //
            // no response handler requested: synchronous execution. Note that client can ask for synchronous execution
            // of a periodic harvester; in that case only the result of the first run are returned synchronously, and
            // results of future runs cannot be tracked by client.

            // OGC 07-006 10.12.4.4 :
            // If the parameter is not present, then the Harvest operation is processed synchronously meaning that the
            // client sends the Harvest request to a CSW and then waits to receive a HarvestResponse or exception
            // message as described in Subclause 10.3.7. The CSW immediately processes the Harvest request, while the
            // client waits for a response. The problem with this mode of operation is that the client may timeout
            // waiting for the server to process the request.
            //
            if (StringUtils.isEmpty(responseHandler)) {
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                    Log.debug(Geonet.CSW_HARVEST, "CSW Harvest executes synchronously");
                response = doHarvest(node, serviceContext, Mode.SYNCHRONOUS);
            }
            //
            // response handler requested: asynchronous execution.
            //
            // OGC 07-006 10.12.4.4 :
            // If the parameter is present, the Harvest operation is processed asynchronously. In this case, the server
            // responds immediately to a client's request with an acknowledgement message as defined in Subclause
            // 10.8.4.13. The acknowledgment message echoes the clientâ€™s request, using the <EchoedRequest> element,
            // and may include an optionally generated request identifier using the <RequestId> element. The
            // acknowledgement message tells the client that the request has been received and notification of
            // completion will be send to the URL specified as the value of the ResponseHandler parameter. The Harvest
            // request may then be processed at some later time taking as much time as is required to complete the
            // operation. When the operation is completed, a HarvestResponse message or exception message per Subclause
            // 10.3.7 (if a problem was encountered) is sent to the URL specified as the value of the ResponseHandler
            // parameter.
            else {
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                    Log.debug(Geonet.CSW_HARVEST, "CSW Harvest executes asynchronously");
                checkResponseHandler(responseHandler);
                // Immediate acknowledgement answer.
                response = createAcknowledgeResponse(request);
                // run harvester
                doHarvest(node, serviceContext, Mode.ASYNCHRONOUS);
                // deal with results asynchronously
                asynchronousHarvestResponse(node, responseHandler, serviceContext);
            }
            if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                Log.debug(Geonet.CSW_HARVEST, "CSW Harvest execute returns:\n" + Xml.getString(response));
            return response;
        } catch (CatalogException x) {
            Log.error(Geonet.CSW_HARVEST, x.getMessage(), x);
            throw x;
        } catch (Exception x) {
            Log.error(Geonet.CSW_HARVEST, x.getMessage(), x);
            throw new NoApplicableCodeEx("ERROR: " + x.getMessage());
        }
    }

    /**
     * Checks whether the responsehandler uses a supported protocol.
     *
     * @param responseHandler - url to send results to
     * @throws InvalidParameterValueEx hmm
     */
    private void checkResponseHandler(String responseHandler) throws InvalidParameterValueEx {
        this.protocol = Protocol.validate(responseHandler);
        if (this.protocol == null) {
            throw new InvalidParameterValueEx("ResponseHandler", "Unsupported protocol in responseHandler " + responseHandler + ". Supported protocols are: ftp://, http://, and mailto:");
        }
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest checkResponseHandler: OK");
    }

    /**
     * Polls periodically whether the harvester is still running and when not, creates a
     * HarvestResponse and sends it to the url in responseHandler.
     * <p>
     * This method must not block the execute() method, therefore it starts a separate thread.
     *
     * @param harvester       - the harvester
     * @param responseHandler - url to send results to
     * @param serviceContext  - all over the place
     */
    private void asynchronousHarvestResponse(Element harvester, String responseHandler, ServiceContext serviceContext) {
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "Asynchronous harvest run requested for " + responseHandler + ", starting in 1 minute");
        long delay = 1;
        CswHarvesterResponseExecutionService.getExecutionService().schedule(
            new AsyncHarvestResponse(harvester, responseHandler, serviceContext), delay, TimeUnit.MINUTES);
    }

    /**
     * Verifies ResourceType is supported.
     * <p>
     * OGC 07-006 10.12.4.2 : The ResourceType parameter references a document that defines the
     * structure of the resource being harvested. For high interoperability, this resource should be
     * an XML document, and the ResourceType parameter string value should be a URI that references
     * the structure of that XML document (i.e., its XML Schema namespace identifier URI). If a
     * server can harvest resources in the schema of an information model it supports, the
     * ResourceType URI should be the same as the outputSchema parameter URI defined for the
     * GetRecords operation.
     *
     * @param request - the request
     * @return requested ResourceType if it is supported
     * @throws InvalidParameterValueEx hmm
     * @throws MissingParameterValueEx hmm
     */
    private String checkResourceType(Element request) throws MissingParameterValueEx, InvalidParameterValueEx {
        String resourceType = request.getChildText("ResourceType", Csw.NAMESPACE_CSW);
        //
        // resourcetype is a required parameter
        //
        if (resourceType == null) {
            throw new MissingParameterValueEx("ResourceType");
        }
        SupportedResourceType supportedResourceType = SupportedResourceType.fromString(resourceType);
        //
        // resource type not supported
        //
        if (supportedResourceType == null) {
            throw new InvalidParameterValueEx("ResourceType", "ResourceType not supported: " + resourceType);
        }
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest checkResourceType OK, returns: " + resourceType);
        return resourceType;
    }

    /**
     * Verifies ResourceFormat is supported.
     * <p>
     * OGC 07-006 10.12.4.3 : The ResourceFormat parameter is used to indicate the encoding used for
     * the resource being harvested. This parameter is included to support the harvesting of
     * metadata resources available in various formats such as plain text, XML or HTML. The values
     * of this parameter shall be a MIME type. If the parameter is not specified then the default
     * value of application/xml shall be assumed.
     *
     * @param request - the request
     * @throws InvalidParameterValueEx hmm
     */
    private void checkResourceFormat(Element request) throws InvalidParameterValueEx {
        String resourceFormat = request.getChildText("ResourceFormat", Csw.NAMESPACE_CSW);
        if (StringUtils.isNotEmpty(resourceFormat) && !resourceFormat.equals("application/xml")) {
            throw new InvalidParameterValueEx("ResourceFormat", "ResourceFormat not supported: " + resourceFormat + ". This catalog only supports XML metadata.");
        }
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest checkResourceFormat: OK");
    }

    /**
     * Verifies Source parameter is present and well-formed.
     * <p>
     * OGC 07-006 10.12.4.1 : The Source parameter is used to specify a URI reference to the
     * metadata resource to be harvested.
     *
     * @param request - the request
     * @return - the harvesting target uri
     * @throws InvalidParameterValueEx hmm
     * @throws MissingParameterValueEx hmm
     */
    private String checkSource(Element request) throws MissingParameterValueEx, InvalidParameterValueEx {
        String source = request.getChildText("Source", Csw.NAMESPACE_CSW);
        //
        // source is a required parameter
        //
        if (source == null) {
            throw new MissingParameterValueEx("Source");
        }
        //
        // check that source is a valid url by constructing URL object from it
        //
        try {
            new URL(source);
        }
        // not a valid url
        catch (MalformedURLException x) {
            throw new InvalidParameterValueEx("Source", "Invalid source URL:" + source + " - " + x.getMessage());
        }

        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest checkSource OK, returns: " + source);
        return source;
    }

    /**
     * Creates request from KVP GET request parameters.
     * <p>
     * See OGC 07-006 10.12.2.
     *
     * @param params - params
     * @return adapted getrequest
     */

    public Element adaptGetRequest(Map<String, String> params) {

        String service = params.get("service");
        String version = params.get("version");
        String source = params.get("Source");
        String resourceType = params.get("ResourceType");
        String resourceFormat = params.get("ResourceFormat");
        String responseHandler = params.get("ResponseHandler");
        String harvestInterval = params.get("HarvestInterval");

        Element request = new Element(getName(), Csw.NAMESPACE_CSW);

        setAttrib(request, "service", service);
        setAttrib(request, "version", version);
        setAttrib(request, "Source", source);
        setAttrib(request, "ResourceType", resourceType);
        setAttrib(request, "ResourceFormat", resourceFormat);
        setAttrib(request, "ResponseHandler", responseHandler);
        setAttrib(request, "HarvestInterval", harvestInterval);

        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest adaptGetRequest returns:\n" + Xml.getString(request));
        return request;
    }

    /**
     * Returns domain values information for specific parameters.
     *
     * @param parameterName - name of param
     * @return parameter values
     * @throws CatalogException hmm
     */

    public Element retrieveValues(String parameterName) throws CatalogException {
        return null;
    }

    /**
     * Creates a CSW Harvester configuration and saves it do database.
     *
     * @param request      - the request
     * @param resourceType - type of resource to harvest
     * @param source       - where to harvest from
     * @param context      - we need that in all methods
     * @return harvester the harvester
     * @throws Exception hmm
     */
    private Element createHarvestNode(Element request, String resourceType, String source, ServiceContext context) throws Exception {
        // TODO use resource type in OutputSchema. Current CSW harvesting client does not allow for setting that.
        //
        // create configuration for the harvester
        //
        Element node = new Element("node");
        // Only CSW harvester could be configured here.
        node = node.setAttribute("type", "csw");

        Element site = new Element("site");
        Element name = new Element("name").addContent(operationId);
        site.addContent(name);

        Element capabilitiesUrl = new Element("capabilitiesUrl");
        capabilitiesUrl.addContent(source);
        site.addContent(capabilitiesUrl);

        Element eleIcon = new Element("icon").addContent("csw.gif");
        site.addContent(eleIcon);

        // if CSW node is protected by HTTP/BA
        // heikki: then it can't be harvested with csw harvest !
        Element account = new Element("account");
        Element username = new Element("username");
        Element password = new Element("password");
        Element use = new Element("use").addContent("false");

        account.addContent(use);
        account.addContent(username);
        account.addContent(password);
        site.addContent(account);

        node.addContent(site);

        // Harvester interval
        Element options = new Element("options");
        Element eleEvery = new Element("every");
        Element eleOneRun = new Element("oneRunOnly");
        String harvestInterval = request.getChildText("HarvestInterval", Csw.NAMESPACE_CSW);
        //
        // single run harvester
        //
        if (StringUtils.isEmpty(harvestInterval) || harvestInterval.equals(ISOPeriod.ZERO_DURATION)) {
            // heikki: why 90 ? it's one run only. Could be empty ?
            eleEvery.addContent("90");
            eleOneRun.addContent("true");
        }
        //
        // periodic harvester
        //
        else {
            int intervalInMinutes = ISOPeriod.iso8601Period2Minutes(harvestInterval);
            eleEvery.addContent(Integer.toString(intervalInMinutes));
            eleOneRun.addContent("false");
        }
        options.addContent(eleEvery);
        options.addContent(eleOneRun);
        node.addContent(options);

        // no search criteria supported in csw harvest, leave empty
        Element searches = new Element("searches");
        node.addContent(searches);

        // no privileges settings supported in csw harvest; use GN-specific setting (if enabled, make metadata public)
        GeonetContext geonetContext = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = geonetContext.getBean(SettingManager.class);
        boolean metadataPublic = sm.getValueAsBool(Settings.SYSTEM_CSW_METADATA_PUBLIC, false);
        if (metadataPublic) {
            // <privileges>
            //   <group id="1">
            //     <operation name="view" />
            //     <operation name="dynamic" />
            //     <operation name="featured" />
            //   </group>
            // </privileges>
            Element privileges = new Element("privileges");
            Element group = new Element("group");
            group.setAttribute("id", "1");
            Element operation1 = new Element("operation");
            operation1.setAttribute("name", "view");
            group.addContent(operation1);
            Element operation2 = new Element("operation");
            operation2.setAttribute("name", "dynamic");
            group.addContent(operation2);
            Element operation3 = new Element("operation");
            operation3.setAttribute("name", "featured");
            group.addContent(operation3);
            privileges.addContent(group);
            node.addContent(privileges);
        }

        /*
         heikki: not so easy as we can't identify harvesters from their target url or anything else

         for the moment, no updates take place, the harvester is simply saved

        // Check if harvester already exist
        // FIXME : Unable to find the getHarvesterID method from patch provided for now id is null
        // String id = hm.getHarvesterID(source);
		// Here we should do an update of an existing node if exist.

//		if (id == null) {
//
//		} else {
//			node.setAttribute("id", id);
//			if (!hm.update(dbms, node))
//				return null;
//		}

         */

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        HarvestManager hm = gc.getBean(HarvestManager.class);
        String uuid = hm.addHarvesterReturnUUID(node);
        node.setAttribute("uuid", uuid);
        node.addContent(new Element("info"));
        AbstractHarvester harvester = hm.getHarvester(uuid);
        String id = harvester.getID();
        node.setAttribute("id", id);

        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "** CSW Harvest createHarvestNode returns:\n" + Xml.getString(node));
        return node;
    }

    /**
     * Creates a HarvestResponse containing results from harvester.
     *
     * @param harvester - the harvester
     * @param context   - here, there and everywhere
     * @return - response
     * @throws Exception hmm
     */
    private Element createHarvestResponse(Element harvester, ServiceContext context) throws Exception {

        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "createHarvestResponse for harvester:\n" + Xml.getString(harvester));

        // http://schemas.opengis.net/csw/2.0.2/CSW-publication.xsd#HarvestResponse :
        // The content of the response varies depending on the presence of the ResponseHandler element. If present, then
        // the catalogue should verify the request and respond immediately with an csw:Acknowledgement element in the
        // response. The catalogue must then attempt to harvest the resource at some later time and send the response
        // message to the location specified by the value of the ResponseHandler element using the indicated protocol
        // (e.g. ftp, mailto, http). If the ResponseHandler element is absent, then the catalogue must attempt to
        // harvest the resource immediately and include a TransactionResponse element in the response. In any case, if
        // the harvest attempt is successful the response shall include summary representations of the newly created
        // catalogue item(s).
        Element harvestResponse = new Element("HarvestResponse", Csw.NAMESPACE_CSW);

        Element error = harvester.getChild("error");
        // successful harvesting run
        if (error == null) {
            Element transactionResponse = new Element("TransactionResponse", Csw.NAMESPACE_CSW);

            // Reports the total number of catalogue items modified by a transaction request (i.e, inserted, updated,
            // deleted). If the client did not specify a requestId, the server may assign one (a URI value).
            Element transactionSummary = new Element("TransactionSummary", Csw.NAMESPACE_CSW);
            Element info = harvester.getChild("info");
            Element result = info.getChild("result");
            Element totalInserted = new Element("totalInserted", Csw.NAMESPACE_CSW).setText(result.getChildText("added"));
            Element totalUpdated = new Element("totalUpdated", Csw.NAMESPACE_CSW).setText(result.getChildText("updated"));
            Element totalDeleted = new Element("totalDeleted", Csw.NAMESPACE_CSW).setText(result.getChildText("removed"));
            transactionSummary.addContent(totalInserted);
            transactionSummary.addContent(totalUpdated);
            transactionSummary.addContent(totalDeleted);
            transactionResponse.addContent(transactionSummary);
            // Returns a "brief" view of any newly created catalogue records. The handle attribute may reference a
            // particular statement in the corresponding transaction request.
            // TODO: impossible to implement with current harvesters because they do not return a list of UUIDs of the inserted metadata.
            // Element insertResult = new Element("InsertResult", Csw.NAMESPACE_CSW);
            // transactionResponse.addContent(insertResult);
            harvestResponse.addContent(transactionResponse);
        }
        // unsuccessful harvesting run
        else {
            harvestResponse.addContent(createExceptionReport(error));
        }
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "createHarvestResponse returns:\n" + Xml.getString(harvestResponse));
        return harvestResponse;
    }

    /**
     * Creates exception report.
     * <p>
     * OGC 07-006 section 10.3.7 : In the event that a catalogue service encounters an error while
     * processing a request or receives an unrecognised request, it shall generate an XML document
     * indicating that an error has occurred. The format of the XML error response is specified by,
     * and shall validate against, the exception response schema defined in clause 8 of the OWS
     * Common Implementation Specification [OGC 05-008c1].
     *
     * @param error error element from harvester node
     * @return exception report
     */
    private Element createExceptionReport(Element error) {
        // Report message returned to the client that requested any OWS operation when the server detects an error while
        // processing that operation request.
        Element exceptionReport = new Element("ExceptionReport", Csw.NAMESPACE_OWS);
        // Specification version for OWS operation. The string value shall contain one x.y.z "version" value (e.g.,
        // "2.1.3"). A version number shall contain three non-negative integers separated by decimal points, in the form
        // "x.y.z". The integers y and z shall not exceed 99. Each version shall be for the Implementation Specification
        // (document) and the associated XML Schemas to which requested operations will conform. An Implementation
        // Specification version normally specifies XML Schemas against which an XML encoded operation response must
        // conform and should be validated. See Version negotiation subclause for more information.
        exceptionReport.setAttribute("version", "2.0.2");
        // An Exception element describes one detected error that a server chooses to convey to the client.
        Element exception = new Element("Exception", Csw.NAMESPACE_OWS);
        // Ordered sequence of text strings that describe this specific exception or error. The contents of these
        // strings are left open to definition by each server implementation. A server is strongly encouraged to include
        // at least one ExceptionText value, to provide more information about the detected error than provided by the
        // exceptionCode. When included, multiple ExceptionText values shall provide hierarchical information about one
        // detected error, with the most significant information listed first.
        Element exceptionText = new Element("ExceptionText", Csw.NAMESPACE_OWS);
        String exceptionClass = error.getChildText("class");
        String exceptionMessage = error.getChildText("message");
        exceptionText.setText(exceptionClass + ": " + exceptionMessage);
        exception.addContent(exceptionText);
        // A code representing the type of this exception, which shall be selected from a set of exceptionCode values
        // specified for the specific service operation and server.
        exception.setAttribute("exceptionCode", "TransactionFailure");
        exceptionReport.addContent(exception);
        return exceptionReport;
    }

    /**
     * Creates Acknowledge response for asynchronous CSW requests.
     * <p>
     * OGC 07-006 section 10.8.4.14 : The acknowledgment message shall echo the exact XML text of
     * the client's request, using the <EchoedRequest> element, and may include an optionally
     * generated request identifier using the <RequestId> element. The echoed request is used to
     * correlate the acknowledgement message with the originating request.
     *
     * @param asyncRequest - the request
     * @return acknowledgement response
     */
    private Element createAcknowledgeResponse(Element asyncRequest) {

        Element response = new Element(getName() + "Response", Csw.NAMESPACE_CSW);
        Element acknowledgement = new Element("Acknowledgement", Csw.NAMESPACE_CSW);

        String timeStamp = new ISODate().toString();
        acknowledgement.setAttribute("timeStamp", timeStamp);

        Element echoedRequest = new Element("EchoedRequest", Csw.NAMESPACE_CSW);
        echoedRequest.addContent(asyncRequest);
        acknowledgement.addContent(echoedRequest);

        Element requestId = new Element("RequestId", Csw.NAMESPACE_CSW);
        requestId.addContent("urn:uuid:" + UUID.randomUUID().toString());
        acknowledgement.addContent(requestId);

        response.addContent(acknowledgement);

        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest createAcknowledgeResponse returns:\n" + Xml.getString(response));
        return response;
    }

    /**
     * Runs the harvester. In synchronous mode, waits for it to finish.
     *
     * @param harvester - the harvester
     * @param context   - everywhere in GN !
     * @param mode      - sync or async
     * @return result of harvest
     * @throws Exception hmm
     */
    private Element doHarvest(Element harvester, ServiceContext context, Mode mode) throws Exception {
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "doHarvest start in mode " + mode.toString());

        // params
        String id = harvester.getAttributeValue("id");
        Element activeParams = new Element("request");
        Element idele = new Element("id");
        idele.addContent(id);
        activeParams.addContent(idele);

        // run
        Element response = Util.exec(activeParams, context, new Util.Job() {

            public OperResult execute(HarvestManager hm, String id) throws Exception {
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                    Log.debug(Geonet.CSW_HARVEST, "doHarvest starting harvester job");
                hm.start(id);
                return hm.run(id);
            }
        });

        if (mode == Mode.SYNCHRONOUS) {
            if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                Log.debug(Geonet.CSW_HARVEST, "doHarvest waiting for harvester to finish");
            waitForHarvesterToFinish(harvester, context);
            if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                Log.debug(Geonet.CSW_HARVEST, "doHarvest finished waiting for harvester to finish");
            response = createHarvestResponse(harvester, context);
        }
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "doHarvest returns\n" + Xml.getString(response));

        return response;
    }

    /**
     * Returns whether the harvester is running.
     *
     * @param harvester - the harvester
     * @param context   - service context: all over GeoNetwork
     * @return whether it is running
     * @throws Exception hmm
     */
    private boolean isRunning(Element harvester, ServiceContext context) throws Exception {
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "isRunning harvester:\n" + Xml.getString(harvester));

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        String uuid = harvester.getAttribute("uuid").getValue();
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "uuid: " + uuid);
        AbstractHarvester abstractHarvester = gc.getBean(HarvestManager.class).getHarvester(uuid);
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "abstractHarvester: " + abstractHarvester);

        if (abstractHarvester == null) {
            return false;
        }
        abstractHarvester.addInfo(harvester);
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "abstractHarvester added info: " + Xml.getString(harvester));

        // GeoNetwork has a bug that whenever addInfo() is called, a <running> element is added again (not overwritten);
        // so you need to check for the existence of a <running>false</running> element to determine whether harvester is
        // still running
        // boolean running = harvester.getChild("info").getChildText("running").equals("true");

        @SuppressWarnings({"unchecked"})
        List<Element> runningElements = harvester.getChild("info").getChildren("running");
        for (Element runningElement : runningElements) {
            if (runningElement.getText().equals("false")) {
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                    Log.debug(Geonet.CSW_HARVEST, "isRunning returns: false");
                return false;
            }
        }
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "isRunning returns: true");
        return true;
    }

    /**
     * Suspends execution until harvester is not running, checking every 30 seconds.
     *
     * @param harvester - the harvester
     * @param context   - all over the place
     * @throws Exception hmm
     */
    private void waitForHarvesterToFinish(Element harvester, ServiceContext context) throws Exception {
        Thread.sleep(30000);
        while (isRunning(harvester, context)) {
            if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                Log.debug(Geonet.CSW_HARVEST, "CSW Harvest waitForHarvesterToFinish: harvester still running");
            // poll every 30 seconds
            Thread.sleep(30000);
        }
        if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
            Log.debug(Geonet.CSW_HARVEST, "CSW Harvest waitForHarvesterToFinish: harvester no longer running");
    }

    /**
     * Modes of operation.
     */
    private enum Mode {
        /**
         * Synchronous mode.
         */
        SYNCHRONOUS,
        /**
         * Asynchronous mode.
         */
        ASYNCHRONOUS
    }

    /**
     * Supported protocols for ResponseHandlers.
     */
    private enum Protocol {
        /**
         * File Transfer Protocol.
         */
        FTP {
            public String toString() {
                return "ftp://";
            }
        },
        /**
         * Hypertext Transfer Protocol.
         */
        HTTP {
            public String toString() {
                return "http://";
            }
        },
        /**
         * Electronic mail.
         */
        EMAIL {
            public String toString() {
                return "mailto:";
            }
        };

        /**
         * Returns the enum value that has a toString starting with the requested string, or null if
         * not found.
         *
         * @param string - string to match
         * @return matching protocol or null if not found
         */
        public static Protocol validate(String string) {
            if (StringUtils.isNotEmpty(string)) {
                for (Protocol protocol : Protocol.values()) {
                    if (string.startsWith(protocol.toString())) {
                        return protocol;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Class to deal with asynchronous HarvestResponse.
     */
    private class AsyncHarvestResponse implements RunnableFuture<Object> {
        Semaphore ready = new Semaphore(0);
        private Element harvester;
        private String responseHandler;
        private ServiceContext serviceContext;

        /**
         * Constructor checks if the responseHandler uses a supported protocol.
         *
         * @param harvester       - the harvester
         * @param responseHandler - url to send result to
         * @param serviceContext  - everywhere in GN !
         */
        AsyncHarvestResponse(Element harvester, String responseHandler, ServiceContext serviceContext) {
            try {
                checkResponseHandler(responseHandler);
            } catch (InvalidParameterValueEx x) {
                throw new ExceptionInInitializerError("WARNING: unsupported protocol in responseHandler " + responseHandler + ", failed to create AsyncHarvestResponse");
            }
            this.harvester = harvester;
            this.responseHandler = responseHandler;
            this.serviceContext = serviceContext;
        }

        /**
         * Sends Harvest response using email.
         *
         * @param harvestResponse response to send
         */
        private void sendByEmail(String harvestResponse) {
            GeonetContext geonetContext = (GeonetContext) serviceContext.getHandlerContext(Geonet.CONTEXT_NAME);
            SettingManager settingManager = geonetContext.getBean(SettingManager.class);
            String host = settingManager.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
            String port = settingManager.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT);
            String to = responseHandler.substring(Protocol.EMAIL.toString().length());
            MailSender sender = new MailSender(serviceContext);
            sender.send(host, Integer.parseInt(port),
                settingManager.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_USERNAME),
                settingManager.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PASSWORD),
                settingManager.getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_SSL),
                settingManager.getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_TLS),
                settingManager.getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_IGNORE_SSL_CERTIFICATE_ERRORS),
                settingManager.getValue(Settings.SYSTEM_FEEDBACK_EMAIL),
                "GeoNetwork CSW Server", to, null, "Asynchronous CSW Harvest results delivery", harvestResponse);
        }

        /**
         * Sends Harvest response using FTP.
         *
         * @param harvestResponse response to send
         */
        private void sendByFTP(String harvestResponse) {
            FTPClient ftpClient = null;
            try {
                ftpClient = new FTPClient();

                // parse ftp uri
                URI ftpUri = new URI(responseHandler);
                String host = ftpUri.getHost();
                int port = ftpUri.getPort();
                String path = ftpUri.getPath();
                String userInfo = ftpUri.getUserInfo();
                String user = null;
                String password = null;
                if (StringUtils.isNotEmpty(userInfo)) {
                    user = userInfo.substring(0, userInfo.indexOf(':'));
                    password = userInfo.substring(userInfo.indexOf(':') + 1);
                }
                if (port > 0) {
                    ftpClient.connect(host, port);
                } else {
                    ftpClient.connect(host);
                }
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                    Log.debug(Geonet.CSW_HARVEST, "Connected to " + host + ".");
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST))
                    Log.debug(Geonet.CSW_HARVEST, ftpClient.getReplyString());
                // check if connection is OK
                int reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                    Log.warning(Geonet.CSW_HARVEST, "Warning: FTP server refused connection. Not sending asynchronous CSW Harvest results to " + responseHandler);
                    return;
                }
                // set timeout to 5 minutes
                ftpClient.setControlKeepAliveTimeout(300);

                // login
                if (user != null && password != null) {
                    ftpClient.login(user, password);
                } else {
                    ftpClient.login("anonymous", "");
                }
                // cd to directory
                if (StringUtils.isNotEmpty(path)) {
                    ftpClient.changeWorkingDirectory(path);
                }

                //
                // transfer file
                //
                String filename = "CSW.Harvest.result";
                InputStream is = new ByteArrayInputStream(harvestResponse.getBytes(Constants.ENCODING));
                ftpClient.storeFile(filename, is);
                is.close();
                ftpClient.logout();
            }
            // never mind, just log it
            catch (IOException x) {
                Log.error(Geonet.CSW_HARVEST, "WARNING: " + x.getMessage() + " (this exception is swallowed)", x);
            }
            // never mind, just log it
            catch (URISyntaxException x) {
                Log.error(Geonet.CSW_HARVEST, "WARNING: " + x.getMessage() + " (this exception is swallowed)", x);
            } finally {
                if (ftpClient != null && ftpClient.isConnected()) {
                    try {
                        ftpClient.disconnect();
                    }
                    // never mind, just log it
                    catch (IOException x) {
                        Log.error(Geonet.CSW_HARVEST, "WARNING: " + x.getMessage() + " (this exception is swallowed)", x);
                    }
                }
            }
        }

        /**
         * Sends Harvest response using HTTP POST.
         *
         * @param harvestResponse response to send
         */
        private void sendByHTTP(String harvestResponse) {
            HttpPost method = new HttpPost(responseHandler);
            try {
                RequestConfig.Builder config = RequestConfig.custom();
                method.setEntity(new StringEntity(harvestResponse));
                config.setAuthenticationEnabled(false);
                method.setConfig(config.build());

                final String requestHost = method.getUri().getHost();
                final ClientHttpResponse httpResponse = applicationContext.getBean(GeonetHttpRequestFactory.class).execute(method,
                    new Function<HttpClientBuilder, Void>() {
                        @Nullable
                        @Override
                        public Void apply(@Nonnull HttpClientBuilder input) {
                            SettingManager settingManager = applicationContext.getBean(SettingManager.class);
                            Lib.net.setupProxy(settingManager, input, requestHost);
                            input.setRetryStrategy(new DefaultHttpRequestRetryHandler());
                            return null;
                        }
                    });
                if (httpResponse.getStatusCode() != HttpStatus.OK) {
                    // never mind, just log it
                    Log.warning(Geonet.CSW_HARVEST, "WARNING: Failed to send HarvestResponse to responseHandler " + responseHandler + ", HTTP status is " + httpResponse.getStatusText());
                }
            } catch (IOException x) {
                // never mind, just log it
                Log.warning(Geonet.CSW_HARVEST, "WARNING: " + x.getMessage() + " (this exception is swallowed)", x);
            } finally {
                method.reset();
            }
        }

        /**
         * Sends a HarvestResponse to the destination specified in responseHandler. Supports http,
         * email and ftp.
         * <p>
         * OGC 07-006 10.12.5: .. send it to the URI specified by the ResponseHandler parameter
         * using the protocol encoded therein. Common protocols are ftp for sending the response to
         * a ftp server and mailto which may be used to send the response to an email address.
         *
         * @param harvestResponse - the response to send
         */
        private void send(Element harvestResponse) {
            if (Log.isDebugEnabled(Geonet.CSW_HARVEST)) {
                Log.debug(Geonet.CSW_HARVEST, "AsyncHarvestResponse send started");
            }

            String harvestResponseString = Xml.getString(harvestResponse);
            if (Log.isDebugEnabled(Geonet.CSW_HARVEST)) {
                Log.debug(Geonet.CSW_HARVEST, "Sending HarvestResponse to " + responseHandler);
            }

            switch (protocol) {
                case EMAIL:
                    sendByEmail(harvestResponseString);
                    break;
                case FTP:
                    sendByFTP(harvestResponseString);
                    break;
                case HTTP:
                    sendByHTTP(harvestResponseString);
                    break;
                default:
                    // shouldn't happen
                    Log.warning(Geonet.CSW_HARVEST, "WARNING: unsupported protocol for responseHandler " + responseHandler + ". " +
                        "HarvestResponse is not sent.");
            }
            if (Log.isDebugEnabled(Geonet.CSW_HARVEST)) {
                Log.debug(Geonet.CSW_HARVEST, "AsyncHarvestResponse send finished");
            }
        }

        /**
         * Polls periodically wether this harvester is still running and when it has finished
         * creates a HarvestResponse and sends it to the url in responseHandler.
         */

        public void run() {
            try {
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST)) {
                    Log.debug(Geonet.CSW_HARVEST, "AsyncHarvestResponse run started");
                }
                waitForHarvesterToFinish(harvester, serviceContext);
                Element harvestResponse = createHarvestResponse(harvester, serviceContext);
                send(harvestResponse);
                ready.release();
                if (Log.isDebugEnabled(Geonet.CSW_HARVEST)) {
                    Log.debug(Geonet.CSW_HARVEST, "AsyncHarvestResponse run finished");
                }
            } catch (Exception x) {
                Log.error(Geonet.CSW_HARVEST, ("ERROR: AsyncHarvestResponse " + x.getMessage() + " (this exception is swallowed)"), x);
            }
        }

        /**
         * Attempts to cancel execution of this task.  This attempt will fail if the task has
         * already completed, has already been cancelled, or could not be cancelled for some other
         * reason. If successful, and this task has not started when <tt>cancel</tt> is called, this
         * task should never run.  If the task has already started, then the
         * <tt>mayInterruptIfRunning</tt> parameter determines whether the thread executing this
         * task should be interrupted in an attempt to stop the task.
         * <p/>
         * <p>After this method returns, subsequent calls to {@link #isDone} will always return
         * <tt>true</tt>.  Subsequent calls to {@link #isCancelled} will always return <tt>true</tt>
         * if this method returned <tt>true</tt>.
         *
         * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this task should be
         *                              interrupted; otherwise, in-progress tasks are allowed to
         *                              complete
         * @return <tt>false</tt> if the task could not be cancelled, typically because it has
         * already completed normally; <tt>true</tt> otherwise
         */

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        /**
         * Returns <tt>true</tt> if this task was cancelled before it completed normally.
         *
         * @return <tt>true</tt> if this task was cancelled before it completed
         */

        public boolean isCancelled() {
            return false;
        }

        /**
         * Returns <tt>true</tt> if this task completed.
         * <p/>
         * Completion may be due to normal termination, an exception, or cancellation -- in all of
         * these cases, this method will return <tt>true</tt>.
         *
         * @return <tt>true</tt> if this task completed
         */

        public boolean isDone() {
            return false;
        }

        /**
         * Waits if necessary for the computation to complete, and then retrieves its result.
         *
         * @return the computed result
         * @throws java.util.concurrent.CancellationException if the computation was cancelled
         * @throws java.util.concurrent.ExecutionException    if the computation threw an exception
         * @throws InterruptedException                       if the current thread was interrupted
         *                                                    while waiting
         */

        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        /**
         * Waits if necessary for at most the given time for the computation to complete, and then
         * retrieves its result, if available.
         *
         * @param timeout the maximum time to wait
         * @param unit    the time unit of the timeout argument
         * @return the computed result
         * @throws java.util.concurrent.CancellationException if the computation was cancelled
         * @throws java.util.concurrent.ExecutionException    if the computation threw an exception
         * @throws InterruptedException                       if the current thread was interrupted
         *                                                    while waiting
         * @throws java.util.concurrent.TimeoutException      if the wait timed out
         */

        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

}
