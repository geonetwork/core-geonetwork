//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
import java.util.List;
import java.util.Map;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.fao.oaipmh.exceptions.BadVerbException;
import org.fao.oaipmh.requests.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

//=============================================================================

public class OaiPmhFactory {
    /**
     * Private constructor to avoid instantiate the class.
     */
    private OaiPmhFactory() {

    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static AbstractRequest parse(ConfigurableApplicationContext applicationContext, Map<String, String> params) throws BadVerbException, BadArgumentException {
        //--- duplicate parameters because the below procedure will consume them

        Map<String, String> params2 = new HashMap<>();


        for (Map.Entry<String, String> param : params.entrySet()) {
            params2.put(param.getKey(), param.getValue());
        }

        params = params2;

        //--- proper processing

        String verb = consumeMan(params, "verb", List.of(IdentifyRequest.VERB,
            GetRecordRequest.VERB,
            ListIdentifiersRequest.VERB,
            ListMetadataFormatsRequest.VERB,
            ListRecordsRequest.VERB,
            ListSetsRequest.VERB));

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
        return consumeMan(params, name, null);
    }

    private static String consumeMan(Map<String, String> params, String name, List<String> allowedValues) throws BadArgumentException {
        String value = params.get(name);

        if (value == null) {
            if (allowedValues != null && !allowedValues.isEmpty()) {
                throw new BadArgumentException("Missing '" + name + "' parameter, allowed values are: " + String.join(", ", allowedValues));
            } else {
                throw new BadArgumentException("Missing '" + name + "' parameter");
            }
        }

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
        if (params.keySet().isEmpty())
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

        req.setIdentifier(consumeMan(params, OaiPmh.ParamNames.IDENTIFIER));
        req.setMetadataPrefix(consumeMan(params, OaiPmh.ParamNames.METADATA_PREFIX));

        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListIdentifiersRequest handleListIdentifiers(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListIdentifiersRequest req = new ListIdentifiersRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        if (params.containsKey(OaiPmh.ParamNames.RESUMPTION_TOKEN))
            req.setResumptionToken(consumeMan(params, OaiPmh.ParamNames.RESUMPTION_TOKEN));
        else {
            req.setMetadataPrefix(consumeMan(params, OaiPmh.ParamNames.METADATA_PREFIX));
            req.setSet(consumeOpt(params, OaiPmh.ParamNames.SET));

            req.setFrom(consumeDate(params, OaiPmh.ParamNames.FROM));
            req.setUntil(consumeDate(params, OaiPmh.ParamNames.UNTIL));
        }

        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListMetadataFormatsRequest handleListMdFormats(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListMetadataFormatsRequest req = new ListMetadataFormatsRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        req.setIdentifier(consumeOpt(params, OaiPmh.ParamNames.IDENTIFIER));
        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListRecordsRequest handleListRecords(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListRecordsRequest req = new ListRecordsRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        if (params.containsKey(OaiPmh.ParamNames.RESUMPTION_TOKEN))
            req.setResumptionToken(consumeMan(params, OaiPmh.ParamNames.RESUMPTION_TOKEN));
        else {
            req.setMetadataPrefix(consumeMan(params, OaiPmh.ParamNames.METADATA_PREFIX));
            req.setSet(consumeOpt(params, OaiPmh.ParamNames.SET));

            req.setFrom(consumeDate(params, OaiPmh.ParamNames.FROM));
            req.setUntil(consumeDate(params, OaiPmh.ParamNames.UNTIL));
        }

        checkConsumption(params);

        return req;
    }

    //---------------------------------------------------------------------------

    private static ListSetsRequest handleListSets(ApplicationContext applicationContext, Map<String, String> params)
        throws BadArgumentException {

        ListSetsRequest req = new ListSetsRequest(applicationContext.getBean(GeonetHttpRequestFactory.class));

        if (params.containsKey(OaiPmh.ParamNames.RESUMPTION_TOKEN))
            req.setResumptionToken(consumeMan(params, OaiPmh.ParamNames.RESUMPTION_TOKEN));

        checkConsumption(params);

        return req;
    }
}

//=============================================================================


