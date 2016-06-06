//=============================================================================
//===  Copyright (C) 2009 World Meteorological Organization
//===  This program is free software; you can redistribute it and/or modify
//===  it under the terms of the GNU General Public License as published by
//===  the Free Software Foundation; either version 2 of the License, or (at
//===  your option) any later version.
//===
//===  This program is distributed in the hope that it will be useful, but
//===  WITHOUT ANY WARRANTY; without even the implied warranty of
//===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===  General Public License for more details.
//===
//===  You should have received a copy of the GNU General Public License
//===  along with this program; if not, write to the Free Software
//===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===  Contact: Timo Proescholdt
//===  email: tproescholdt_at_wmo.int
//==============================================================================

package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;

import org.fao.geonet.exceptions.MissingParameterEx;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.util.z3950.*;
import org.fao.geonet.services.util.z3950.jzkitextensions.GNProfileService;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jzkit.search.ExplainInformationDTO;
import org.jzkit.search.LandscapeSpecification;
import org.jzkit.search.SearchSessionFactory;
import org.jzkit.search.StatelessSearchResultsPageDTO;
import org.jzkit.search.landscape.SimpleLandscapeSpecification;
import org.jzkit.search.provider.iface.SearchException;
import org.jzkit.search.util.QueryModel.InvalidQueryException;
import org.jzkit.search.util.RecordModel.ArchetypeRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.ExplicitRecordFormatSpecification;
import org.jzkit.search.util.RecordModel.RecordFormatSpecification;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.*;

//=============================================================================

/**
 * SRU service. Perform a SRU websearch via JZkit implements rearchAndRetrieve and Explain
 * operations
 *
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 */

/**
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>'
 *
 */
public class SRUSearch implements Service {
    public static final int SRU_records_per_page = 10;
    public static final String OP = "operation";
    public static final String OP_SR_QUERY = "query";
    public static final String OP_SR_VERSION = "version";
    public static final String OP_SR_STYLESH = "stylesheet";
    public static final String OP_SR_STARTREC = "startrecord";
    public static final String OP_SR_MAXREC = "maximumrecords";
    public static final String OP_SR_RECPACK = "recordpacking";
    public static final String OP_SR_RECSCHEMA = "recordschema";
    public static final String OP_SR_RECXPATH = "recordxpath";
    public static final String OP_SR_SORTKEYS = "sortkeys";
    public static final String OP_SR_EXTRADATA = "extrarequestdata";
    public static final String OP_EXPL_RECPACK = "query";
    public static final String OP_EXPL_VERSION = "version";
    public static final String OP_EXPL_STYLESH = "stylesheet";
    public static final int ERROR_OP_NOT_SUPPORTED = 1;
    public static final int ERROR_VERSION_NOT_SUPPORTED = 2;
    public static final int ERROR_SRUATTRIBUTE_NOT_SUPPORTED = 3;
    public static final int ERROR_SRUOP_NOT_SUPPORTED = 4;
    private static RecordFormatSpecification request_spec = new ArchetypeRecordFormatSpecification("F");
    private SearchSessionFactory searchsessionfact;

    private Hashtable<String, String> contextSets;

    private SRUParamTester paramtester = new SRUParamTester();


    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    private static void addToDiag(Element diagnostics, String uri, String message, String details) {

        Element diagnostic = new Element("diagnostic");

        diagnostic.addContent(new Element("uri").setText(uri));
        diagnostic.addContent(new Element("message").setText(message));
        diagnostic.addContent(new Element("details").setText(details));

        diagnostics.addContent(diagnostic);
    }


    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig config) throws Exception {
        if (Log.isDebugEnabled(Geonet.SRU))
            Log.debug(Geonet.SRU, "SRUsearch::init");

        contextSets = new Hashtable<String, String>();

        contextSets.put("dc", "info:srw/cql-context-set/1/dc-v1.1");
        contextSets.put("gils", "info:srw/cql-context-set/14/gils-v1.0");
        contextSets.put("geo", "http://??");
        contextSets.put("cql", "info:srw/cql-context-set/1/cql-v1.2");
        contextSets.put("rec", "info:srw/cql-context-set/2/rec-1.1");


    }

    public Element exec(Element params, ServiceContext context) throws Exception {


        @SuppressWarnings("unchecked")
        Hashtable<String, String> myparams = parseArgs(params.getChildren());

        if (Log.isDebugEnabled(Geonet.SRU))
            Log.debug(Geonet.SRU, "SRUsearch::exec op:" + myparams.get("operation") + " version " + myparams.get("version"));


        // default op is explain

        if (!myparams.containsKey("operation")) {
            myparams.put("operation", "explain");
        }
        String op = myparams.get("operation");


        Element ret = null;

        if (op.equalsIgnoreCase("searchretrieve"))
            ret = processSearchRetrieve(myparams, context);
        else if (op.equalsIgnoreCase("explain"))
            ret = processExplain(myparams, context);
        else if (op.equalsIgnoreCase("scan"))
            ret = processScan(myparams, context);
        else {
            ret = processExplain(myparams, context);
            op = "explain";
        }
        ret.addContent(new Element("myop").setText(op.toLowerCase()));

        return ret;

    }

    /**
     * scan is not supported. Only return operation not supported diag
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    private Element processScan(Hashtable<String, String> params, ServiceContext context) throws Exception {

        if (Log.isDebugEnabled(Geonet.SRU))
            Log.debug(Geonet.SRU, "processScan");

        Element response = new Element(Jeeves.Elem.RESPONSE);

        Element diagnostics = processDiag(params, "scan");
        if (diagnostics != null) response.addContent(diagnostics);


        return response;
    }

    private Element processExplain(Hashtable<String, String> params, ServiceContext context) throws Exception {

        if (Log.isDebugEnabled(Geonet.SRU))
            Log.debug(Geonet.SRU, "processExplain");

        // has to be called first. Other methods
        //checkMandatoryParams(params, mandatoryEXPL);


        SearchSessionFactory search_session_factory = getSearchSession(context);
        ExplainInformationDTO explain = search_session_factory.explain();


        Hashtable<String, Boolean> seenContextSets = new Hashtable<String, Boolean>();

        Element response = new Element(Jeeves.Elem.RESPONSE);

        String myop = "explain";
        if (params.containsKey("operation")) {
            myop = params.get("operation");
        }

        Element diagnostics = processDiag(params, myop);
        if (diagnostics != null) response.addContent(diagnostics);

        response.setAttribute(new Attribute("servername", context.getIpAddress()));
        //response.setAttribute(new Attribute("port", "????")); //FIXME: dont know where I should get that info from. Done in stylesheet
        response.setAttribute(new Attribute("sruuri", context.getBaseUrl() + "/srv/" + context.getLanguage() + "/" + context.getService())); //FIXME: can I get the query string from somewhere?
        response.setAttribute(new Attribute("records_per_page", SRU_records_per_page + ""));

        Element indices = new Element("indices");
        for (Object o : explain.getDatabaseInfo()) {
            GNExplainInfoDTO ex = (GNExplainInfoDTO) o;

            Element index = new Element("index");
            index.setAttribute("id", ex.getId());

            for (String key : ex.getMappings().keySet()) {
                Element map = new Element("map");

                String contextSet = ex.getMappings().get(key);

                map.setAttribute("set", contextSet);
                map.setAttribute("text", key);

                seenContextSets.put(ex.getMappings().get(key), true);

                index.addContent(map);
            }

            indices.addContent(index);

        }

        Element sets = new Element("sets");
        Enumeration<String> enu = seenContextSets.keys();

        while (enu.hasMoreElements()) {

            Element set = new Element("set");
            String namespace = enu.nextElement();
            String url = "http://???";
            if (this.contextSets.containsKey(namespace)) {
                url = this.contextSets.get(namespace);
            }

            set.setAttribute(new Attribute("namespace", namespace));
            set.setAttribute(new Attribute("url", url));

            sets.addContent(set);
        }

        response.addContent(sets);
        response.addContent(indices);

        return response;
    }

    private Element processSearchRetrieve(Hashtable<String, String> params, ServiceContext context) throws Exception {

        if (Log.isDebugEnabled(Geonet.SRU))
            Log.debug(Geonet.SRU, "processSearchRetrieve");

        //checkMandatoryParams(params, mandatorySR);

        Element response = new Element(Jeeves.Elem.RESPONSE);

        Element diagnostics = processDiag(params, "searchretrieve");
        if (diagnostics != null) {
            response.addContent(diagnostics);
            return response;
        }

        try {

            int num_hits_per_page = SRU_records_per_page;
            int first_record = 1;

            if (params.get(OP_SR_MAXREC) != null)
                num_hits_per_page = Integer.parseInt(params.get(OP_SR_MAXREC));

            if (params.get(OP_SR_STARTREC) != null)
                first_record = Integer.parseInt(params.get(OP_SR_STARTREC));

            String record_schema = params.get(OP_SR_RECSCHEMA);
            if (record_schema == null)
                record_schema = "meta";

            String query = params.get(OP_SR_QUERY);


            ExplicitRecordFormatSpecification display_spec = new ExplicitRecordFormatSpecification("xml", "", "f");


            if (Log.isDebugEnabled(Geonet.SRU))
                Log.debug(Geonet.SRU, "getting reference to search session factory");


            // TODO: would be nice to move this to init method but I dont know where to get the context from there..

            // not supported by Geonetwork modules URL layout schema
            // TODO: collections could also be mapped to GeoNetwork categories?
            LandscapeSpecification landscape = new SimpleLandscapeSpecification("geonetwork");

            DefaultContextSetCQLString model = new DefaultContextSetCQLString(query, "geo", "cql", "geo");
            // we assume that all incoming queries are from the geo (attributes,structure) and cql (relation) context sets
            // if we set this to false we have to write a crosswalk for bib-1,dc....
            //model.setForceContextSet(true);

            SearchSessionFactory search_session_factory = getSearchSession(context);


            if (Log.isDebugEnabled(Geonet.SRU))
                Log.debug(Geonet.SRU, "Calling search_session_factory.getResultsPageFor");
            StatelessSearchResultsPageDTO result = search_session_factory.getResultsPageFor(null,
                model,
                landscape,
                first_record,
                num_hits_per_page,
                request_spec,
                display_spec,
                null);
            if (Log.isDebugEnabled(Geonet.SRU))
                Log.debug(Geonet.SRU, "Call to getResultsPageFor completed : " + result);

            Element myresponse = new Element("sruresponse");
            response.addContent(myresponse);

            int num_records = 0;
            String res_id = "";
            long idle = 0;

            if ((result.records != null) && (result.records.length > 0)) {
                DOMBuilder builder = new DOMBuilder();


                num_records = result.records.length;
                res_id = result.result_set_id;
                idle = result.result_set_idle_time;

                for (int i = 0; i < result.records.length; i++) {

                    Element elem = new Element("record");
                    elem.setAttribute(new Attribute("recordPosition", "" + first_record + i));

                    ExplicitRecordFormatSpecification res = result.records[i].getFormatSpecification();

                    // check if the format corresponds to what we are requesting
                    if (!res.toString().equals(display_spec.toString())) {
                        Log.error(Geonet.SRU, "error, format specification " + result.records[i].getFormatSpecification() + " does not correspond to " + display_spec + " :" + result.records[i].getOriginalObject());


                        addToDiag(elem, "info:srw/diagnostic/1/67", "Record not available in this schema", result.records[i].getOriginalObject().toString());

                        //throw new Exception("SRU error:"+result.records[i].getOriginalObject());
                    } else if (result.records[i].getOriginalObject() instanceof org.jdom.Document) {
                        org.jdom.Document doc = (org.jdom.Document) result.records[i].getOriginalObject();

                        Element e = doc.getRootElement();
                        e.detach();

                        elem.addContent(e);
                    } else if (result.records[i].getOriginalObject() instanceof Document) {
                        Document d = (Document) result.records[i].getOriginalObject();

                        // FIXME: ARHHHHH!!!!! this is inefficient... there must be another way of doing this
                        org.jdom.Document doc = builder.build(d);

                        Element e = doc.getRootElement();
                        e.detach();

                        elem.addContent(e);

                    } else {
                        String errormsg = "error: could not decode reponse object of type: " + result.records[i].getOriginalObject().getClass().getName();
                        Log.error(Geonet.CSW_SEARCH, errormsg);

                        addToDiag(elem, "info:srw/diagnostic/1/71", "Unsupported record packing", errormsg);

                        //throw new Exception(errormsg);
                    }
                    myresponse.addContent(elem);
                }
            }

            response.addContent(new Element("numrec").setText(num_records + ""));
            response.addContent(new Element("idle").setText(idle + ""));
            response.addContent(new Element("id").setText(res_id));


        } catch (InvalidQueryException e) {
            // diagnostics is known to be null
            diagnostics = new Element("diagnostics");

            addToDiag(diagnostics, "info:srw/diagnostic/1/10", "Query syntax error", e.getMessage());

            Log.error(Geonet.SRU, "InvalidQueryException" + e);

        } catch (SearchException e) {
            // diagnostics is known to be null
            diagnostics = new Element("diagnostics");

            if (e.error_code == GNProfileService.ERROR_QUERY) {
                addToDiag(diagnostics, "info:srw/diagnostic/1/16", "Unsupported index", e.getMessage());
            } else if (e.error_code == GNProfileService.ERROR_CONFIG) {
                addToDiag(diagnostics, "info:srw/diagnostic/1/1", "General system error", "Config error: " + e.getMessage());
            } else {
                addToDiag(diagnostics, "info:srw/diagnostic/1/1", "General system error", e.getMessage());
            }

            Log.error(Geonet.SRU, "SearchException" + e);

        } catch (Exception e) {

            // if there were other diag messages above (right now we return directly but this might change)
            if (diagnostics == null) diagnostics = new Element("diagnostics");
            addToDiag(diagnostics, "info:srw/diagnostic/1/1", "General system error", e.toString());
            Log.error(Geonet.SRU, "problem at backend interaction" + e);
            //e.printStackTrace();
        }

        if (diagnostics != null) response.addContent(diagnostics);

        return response;
    }

    private Set<SRUDiag> getDiag(String op, Hashtable<String, String> params) {

        HashSet<SRUDiag> set = new HashSet<SRUDiag>();

        SRUParamTestDBO res = paramtester.testParams(op, params);

        for (String param : res.getArgNotSupported()) {
            set.add(new SRUDiag("info:srw/diagnostic/1/8", "Unsupported Parameter", param));
        }

        for (String param : res.getCannotParseArg()) {
            set.add(new SRUDiag("info:srw/diagnostic/1/6", "Unsupported parameter value", param + " : " + params.get(param)));
        }

        for (String param : res.getMissingArgs()) {
            set.add(new SRUDiag("info:srw/diagnostic/1/7", "Mandatory parameter not supplied", param));
        }

        String version = params.get("version");
        if (version != null && (!(version.equals("1.1") || version.equals("1.2")))) {
            set.add(new SRUDiag("info:srw/diagnostic/1/5", "Unsupported version", "I got version: " + version + " but I support only 1.1 or 1.2"));
        }

        if (!op.equalsIgnoreCase("searchretrieve") && !op.equalsIgnoreCase("explain")) {
            set.add(new SRUDiag("info:srw/diagnostic/1/4", "Unsupported operation", op));
        }


        return set;
    }

    private SearchSessionFactory getSearchSession(ServiceContext context) {

        if (searchsessionfact == null) {

            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            ApplicationContext app_context = gc.getApplicationContext();
            this.searchsessionfact = (SearchSessionFactory) app_context.getBean("SearchSessionFactory");

        }

        return searchsessionfact;
    }

    private Hashtable<String, String> parseArgs(List<Element> params) throws MissingParameterEx {


        Hashtable<String, String> res = new Hashtable<String, String>();

        for (Iterator<Element> it = params.listIterator(); it.hasNext(); ) {

            Element e = it.next();
            String name = e.getName().toLowerCase();
            String val = e.getText().toLowerCase();
            res.put(name, val);
        }

        Hashtable<String, Boolean> opht = new Hashtable<String, Boolean>();
        opht.put("operation", true);

        //checkMandatoryParams(res, opht);

        return res;
    }

    private Element processDiag(Hashtable<String, String> params, String op) throws Exception {


        Set<SRUDiag> diag = getDiag(op, params);
        Element diagnostics = null;

        if (diag.size() > 0) {

            diagnostics = new Element("diagnostics");

            for (SRUDiag sruDiag : diag) {
                addToDiag(diagnostics, sruDiag.getUrl(), sruDiag.getMessage(), sruDiag.getDetails());
            }
        }
        return diagnostics;
    }


}
