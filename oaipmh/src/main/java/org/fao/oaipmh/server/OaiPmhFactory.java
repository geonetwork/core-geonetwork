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

package org.fao.oaipmh.server;

import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.fao.oaipmh.exceptions.BadVerbException;
import org.fao.oaipmh.requests.*;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

//=============================================================================

public class OaiPmhFactory {
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static Map<String, String> extractParams(Element request) throws BadArgumentException {
        Map<String, String> params = new HashMap<String, String>();

        for (Object o : request.getChildren()) {
            Element elem = (Element) o;
            String name = elem.getName();
            String value = elem.getText();

            if (params.containsKey(name))
                throw new BadArgumentException("Parameter repeated : " + name);

            params.put(name, value);
        }

        return params;
    }

    //---------------------------------------------------------------------------

    public static AbstractRequest parse(ConfigurableApplicationContext applicationContext, Map<String, String> params) throws BadVerbException, BadArgumentException {
        //--- duplicate parameters because the below procedure will consume them

        Map<String, String> params2 = new HashMap<String, String>();


        for (Map.Entry<String, String> param : params.entrySet()) {
            params2.put(param.getKey(), param.getValue());
        }

        params = params2;

        //--- proper processing

        String verb = consumeMan(params, "verb");

        if (verb.equals(IdentifyRequest.VERB)) {
            return handleIdentify(applicationContext, params);
        }

        if (verb.equals(GetRecordRequest.VERB)) {
            return handleGetRecord(applicationContext, params);
        }

        if (verb.equals(ListIdentifiersRequest.VERB)) {
            return handleListIdentifiers(applicationContext, params);
        }

        if (verb.equals(ListMetadataFormatsRequest.VERB)) {
            return handleListMdFormats(applicationContext, params);
        }

        if (verb.equals(ListRecordsRequest.VERB)) {
            return handleListRecords(applicationContext, params);
        }

        if (verb.equals(ListSetsRequest.VERB)) {
            return handleListSets(applicationContext, params);
        }

        throw new BadVerbException("Unknown verb : " + verb);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    private static String consumeMan(Map<String, String> params, String name) throws BadArgumentException {
        String value = params.get(name);

        if (value == null)
            throw new BadArgumentException("Missing '" + name + "' parameter");

        if (value.trim().length() == 0)
            throw new BadArgumentException("Empty '" + name + "' parameter");

        params.remove(name);

        return value;
    }

    //---------------------------------------------------------------------------

    private static String consumeOpt(Map<String, String> params, String name) throws BadArgumentException {
        String value = params.get(name);

        if (value == null)
            return null;

        if (value.trim().length() == 0)
            throw new BadArgumentException("Empty '" + name + "' parameter");

        params.remove(name);

        return value;
    }

    //---------------------------------------------------------------------------

    private static ISODate consumeDate(Map<String, String> params, String name) throws BadArgumentException {
        String date = consumeOpt(params, name);

        if (date == null)
            return null;

        try {
            return new ISODate(date);
        } catch (Exception e) {
            throw new BadArgumentException("Illegal date format : " + date);
        }
    }

    //---------------------------------------------------------------------------

    private static void checkConsumption(Map<String, String> params) throws BadArgumentException {
        if (params.keySet().size() == 0)
            return;

        String extraParam = params.keySet().iterator().next();

        throw new BadArgumentException("Unknown extra parameter '" + extraParam + "'");
    }

    //---------------------------------------------------------------------------
    //---
    //--- Dispatching methods
    //---
    //---------------------------------------------------------------------------

    private static IdentifyRequest handleIdentify(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {
        checkConsumption(params);

        return new IdentifyRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));
    }

    //---------------------------------------------------------------------------

    private static GetRecordRequest handleGetRecord(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {
        GetRecordRequest req = new GetRecordRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        req.setIdentifier(consumeMan(params, "identifier"));
        req.setMetadataPrefix(consumeMan(params, "metadataPrefix"));

        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListIdentifiersRequest handleListIdentifiers(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListIdentifiersRequest req = new ListIdentifiersRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        if (params.containsKey("resumptionToken"))
            req.setResumptionToken(consumeMan(params, "resumptionToken"));
        else {
            req.setMetadataPrefix(consumeMan(params, "metadataPrefix"));
            req.setSet(consumeOpt(params, "set"));

            req.setFrom(consumeDate(params, "from"));
            req.setUntil(consumeDate(params, "until"));
        }

        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListMetadataFormatsRequest handleListMdFormats(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListMetadataFormatsRequest req = new ListMetadataFormatsRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        req.setIdentifier(consumeOpt(params, "identifier"));
        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListRecordsRequest handleListRecords(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListRecordsRequest req = new ListRecordsRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        if (params.containsKey("resumptionToken"))
            req.setResumptionToken(consumeMan(params, "resumptionToken"));
        else {
            req.setMetadataPrefix(consumeMan(params, "metadataPrefix"));
            req.setSet(consumeOpt(params, "set"));

            req.setFrom(consumeDate(params, "from"));
            req.setUntil(consumeDate(params, "until"));
        }

        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListSetsRequest handleListSets(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListSetsRequest req = new ListSetsRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        if (params.containsKey("resumptionToken"))
            req.setResumptionToken(consumeMan(params, "resumptionToken"));

        checkConsumption(params);

        return req;
    }
}

//=============================================================================


