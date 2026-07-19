package org.fao.geonet.kernel.harvest.harvester.simpleurl;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RDF utilities to query and process DCAT feeds.
 */
public class RDFUtils {
    public static final String RESOURCE_FOLDER = "harvester-resources/simpleUrl/";

    public static final String LOGGER_NAME = "geonetwork.harvester.simpleurl";

    /**
     * Retrieve all UUIDs from the RDF feed using a SPARQL query
     *
     * @return a map of UUID and the corresponding DCAT record.
     */
    public static HashMap<String, Element> getAllUuids(String feedUrl) throws Exception {
        // Create an empty in-memory model and populate it from the graph
        Model model = ModelFactory.createMemModelMaker().createDefaultModel();
        RDFDataMgr.read(model, feedUrl);

        Element rdfModel = Xml.loadStream(IOUtils.toInputStream(toRdfString(model), StandardCharsets.UTF_8.displayName()));

        return getAllUuids(rdfModel);
    }

    /**
     * Retrieve all UUIDs from the RDF feed using a SPARQL query.
     * <p>
     * The SPARQL results structure is:
     * <pre>
     * <sparql xmlns="http://www.w3.org/2005/sparql-results#">
     *   <head>
     *     <variable xmlns="" name="subject"/>
     *     <variable xmlns="" name="predicate"/>
     *     <variable xmlns="" name="object"/>
     *     <variable xmlns="" name="pAsQName"/>
     *   </head>
     *   <results>
     *     <result>
     *       <binding xmlns="" name="subject">
     *         <uri xmlns="http://www.w3.org/2005/sparql-results#">https://apps.titellus.net/geonetwork/api/collections/main/items/8698bf0b-fceb-4f0f-989b-111e7c4af0a4</uri>
     *       </binding>
     *       <binding xmlns="" name="predicate">
     *         <uri xmlns="http://www.w3.org/2005/sparql-results#">http://www.w3.org/1999/02/22-rdf-syntax-ns#type</uri>
     *       </binding>
     *       <binding xmlns="" name="object">
     *         <uri xmlns="http://www.w3.org/2005/sparql-results#">http://www.w3.org/ns/dcat#CatalogRecord</uri>
     *       </binding>
     *       <binding xmlns="" name="pAsQName">
     *         <literal xmlns="http://www.w3.org/2005/sparql-results#">rdf:type</literal>
     *       </binding>
     *     </result>
     *     ...
     * </pre>
     * and can be processed using XSLT to build a metadata supported by the catalogue.
     *
     * @return a map of UUID and the corresponding DCAT record.
     */
    public static HashMap<String, Element> getAllUuids(Element feed) throws Exception {
        Element rdfDocument = checkForMissingRdfAbout(feed);

        // if xsi:schemaLocation is present on the rdf:RDF element Jena fails to parse the Element.
        rdfDocument.removeAttribute("schemaLocation", Geonet.Namespaces.XSI);

        Model model = ModelFactory.createMemModelMaker().createDefaultModel();
        RDFDataMgr.read(model,
            IOUtils.toInputStream(Xml.getString(rdfDocument), StandardCharsets.UTF_8),
            Lang.RDFXML);

        model = checkAndCreateMissingCatalogRecords(model);

        Query queryRecordIds = QueryFactory.create(getQueryString("extract-records-ids.rq"));
        QueryExecution qe = QueryExecutionFactory.create(queryRecordIds, model);
        ResultSet resultIds = qe.execSelect();

        HashMap<String, Element> records = new HashMap<>();
        while (resultIds.hasNext()) {
            Pair<String, Element> recordInfo = getRecordInfo(resultIds.nextSolution(), model);
            if (recordInfo != null) {
                records.put(recordInfo.one(), recordInfo.two());
            }
        }

        qe.close();
        model.close();
        return records;
    }


    private static Element checkForMissingRdfAbout(Element rdfModel) throws Exception {
        List ns = rdfModel.getAdditionalNamespaces();
        List<Element> nodeWithNoRdfAbout =
            Xml.selectNodes(rdfModel,
                ".//*[local-name() = 'Dataset' or local-name() = 'DataService'][not(@rdf:about)]", ns
            );
        nodeWithNoRdfAbout.forEach(n -> {
            try {
                Object httpIdentifier = Xml.selectSingle(n, "dct:identifier[matches(., '^http(s)://.*$')]/text()", ns);
                if (httpIdentifier != null) {
                    n.setAttribute("rdf:about", (String) httpIdentifier);
                }
            } catch (JDOMException e) {
            }
        });
        return rdfModel;
    }

    private static Model checkAndCreateMissingCatalogRecords(Model model) throws IOException, URISyntaxException {
        Query queryExtractNoRec = QueryFactory.create(getQueryString("extract-resources-no-records.rq"));
        QueryExecution qe = QueryExecutionFactory.create(queryExtractNoRec, model);

        Model newModel = model;
        ResultSet resultIds = qe.execSelect();
        while (resultIds.hasNext()) {
            QuerySolution solution = resultIds.nextSolution();
            newModel = createCatalogRecord(
                newModel,
                solution.get("resourceId").toString(),
                solution.contains("catalogId") ? solution.get("catalogId").toString() : ""
            );
        }
        return newModel;
    }


    private static InputStream getResourceAsStream(String resourcePath) {
        return RDFUtils.class.getClassLoader().getResourceAsStream(resourcePath);
    }

    /**
     * Convert a model back to RDF XML
     * Only used for debugging
     */
    private static String toRdfString(Model model) {
        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.RDFXML);
        return out.toString();
    }


    private static String getQueryString(String queryFile) throws IOException, URISyntaxException {
        return IOUtils.toString(getResourceAsStream(RESOURCE_FOLDER + "sparql/" + queryFile), Constants.CHARSET);
    }

    private static Model createCatalogRecord(Model model, String resourceId, String catalogId) throws IOException, URISyntaxException {
        String recordUUID = resourceId;
        Date now = new Date();
        String localQuery = getQueryString("add-CatalogRecord.rq")
            .replace("%recordID%", recordUUID + "-record")
            .replace("%recordUUID%", recordUUID)
            .replace("%resourceId%", resourceId)
            // TODO: Should we set modified of catalog record to the date of publication?
            // If not, they will be popup on top of search by date
            .replace("%modifiedDate%", new ISODate(now.getTime(), false).toString())
            .replace("%catalogId%", catalogId);

        Query queryFixBlankNodes = QueryFactory.create(localQuery);
        QueryExecution qe = QueryExecutionFactory.create(queryFixBlankNodes, model);
        Model newModel = qe.execConstruct();
        qe.close();
        return newModel;
    }


    private static Pair<String, Element> getRecordInfo(QuerySolution solution, Model model) {
        try {
            String recordId = solution.get("recordId").toString();
            String resourceId = solution.get("resourceId").toString();
            String baseRecordUUID = solution.get("baseRecordUUID").toString();

            String localQueryBuildRecord = getQueryString("build-record.rq")
                .replace("%recordId%", recordId)
                .replace("%resourceId%", resourceId);

            Query queryRecord = QueryFactory.create(localQueryBuildRecord);
            QueryExecution qe = QueryExecutionFactory.create(queryRecord, model);
            ResultSet results = qe.execSelect();

            if (results.hasNext()) {
                ByteArrayOutputStream outxml = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsXML(outxml, results);
                Element sparqlResults = Xml.loadStream(
                    new ByteArrayInputStream(outxml.toByteArray()));
                qe.close();

                Map<String, Object> params = new HashMap<>();
                params.put("recordUUID", baseRecordUUID);

                // TODO: Update record only if modified is more recent than local
//                Literal modifiedLiteral = solution.getLiteral("modified");
//                String modified;
//                if (modifiedLiteral != null) {
//                    modified = DateUtil.convertToISOZuluDateTime(modifiedLiteral.getString());
//                }

                return Pair.read(baseRecordUUID, sparqlResults);
            } else {
                qe.close();
            }

        } catch (JDOMException | IOException | URISyntaxException e) {
            Log.error(LOGGER_NAME, String.format(
                "Error extracting record info using SPARQL. Error is: %s", e.getMessage()));
        }

        // we get here if we couldn't get the UUID or date modified
        return null;
    }
}
