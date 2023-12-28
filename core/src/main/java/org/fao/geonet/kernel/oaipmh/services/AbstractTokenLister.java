/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.oaipmh.services;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.kernel.oaipmh.ResumptionTokenCache;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Log;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.fao.oaipmh.exceptions.BadResumptionTokenException;
import org.fao.oaipmh.exceptions.NoRecordsMatchException;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.TokenListRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.GeonetworkResumptionToken;
import org.fao.oaipmh.responses.ListResponse;
import org.fao.oaipmh.util.SearchResult;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

import jeeves.server.context.ServiceContext;


public abstract class AbstractTokenLister implements OaiPmhService {

    protected ResumptionTokenCache cache;

    private SettingManager settingMan;
    private SchemaManager schemaMan;

    protected AbstractTokenLister(ResumptionTokenCache cache, SettingManager sm, SchemaManager scm) {
        this.cache = cache;
        this.settingMan = sm;
        this.schemaMan = scm;
    }

    /**
     * @return the mode
     */
    public int getMode() {
        return settingMan.getValueAsInt(Settings.SYSTEM_OAI_MDMODE);
    }

    /**
     *
     * @return
     */
    public int getMaxRecords() {
        return settingMan.getValueAsInt(Settings.SYSTEM_OAI_MAXRECORDS);
    }

    /**
     * Get the dateFrom Possible values are taken from the LuceneQueryBuilder class (hard coding)
     *
     * @return the dateFrom
     */
    public String getDateFrom() {
        // Default mode is set to OaiPmhDispatcher.MODE_MODIFIDATE
        String dateFrom = "dateFrom";
        if (getMode() == OaiPmhDispatcher.MODE_TEMPEXTEND) {
            dateFrom = "extFrom";
        }
        return dateFrom;
    }

    /**
     * Get the dateUntil Possible values are taken from the LuceneQueryBuilder class (hard coding)
     *
     * @return the dateUntil
     */
    public String getDateUntil() {
        // Default mode is set to OaiPmhDispatcher.MODE_MODIFIDATE
        String dateUntil = "dateTo";
        if (getMode() == OaiPmhDispatcher.MODE_TEMPEXTEND) {
            dateUntil = "extTo";
        }
        return dateUntil;
    }

    public AbstractResponse execute(AbstractRequest request,
                                    ServiceContext context) throws Exception {

        if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER, "OAI " + this.getClass().getSimpleName() + " execute: ");

        TokenListRequest req = (TokenListRequest) request;

        SearchResult result;

        String strToken = req.getResumptionToken();
        GeonetworkResumptionToken token = null;


        int pos = 0;

        if (strToken == null) {
            if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
                Log.debug(Geonet.OAI_HARVESTER, "OAI " + this.getClass().getSimpleName() + " : new request (no resumptionToken)");
            Element params = new Element("request");

            ISODate from = req.getFrom();
            ISODate until = req.getUntil();
            String set = req.getSet();
            String prefix = req.getMetadataPrefix();

            if (from != null) {
                String sFrom = from.isDateOnly() ? from.getDateAsString() : from.toString();
                params.addContent(new Element(getDateFrom()).setText(sFrom));
            }

            if (until != null) {
                String sTo = until.isDateOnly() ? until.getDateAsString() : until.toString();
                params.addContent(new Element(getDateUntil()).setText(sTo));
            }

            if (from != null && until != null && from.timeDifferenceInSeconds(until) > 0)
                throw new BadArgumentException("From is greater than until");

            if (set != null)
                params.addContent(new Element("category").setText(set));

            params.addContent(new Element("_schema").setText(prefix));

            // now do the search
            result = new SearchResult(prefix);
            if (schemaMan.existsSchema(prefix)) {
                result.setIds(Lib.search(context, params));
            } else {
                // collect up all the schemas that we can convert to create prefix,
                // search ids and add to the result set
                List<String> schemas = getSchemasThatCanConvertTo(prefix);
                for (String schema : schemas) {
                    params.removeChild("_schema");
                    params.addContent(new Element("_schema").setText(schema));
                    result.addIds(Lib.search(context, (Element) params.clone()));
                }
                if (schemas.isEmpty()) result.setIds(new ArrayList<>());
            }

            if (result.getIds().isEmpty()) {
                throw new NoRecordsMatchException("No results");
            }

            // we only need a new token if the result set is big enough
            if (result.getIds().size() > getMaxRecords()) {
                token = new GeonetworkResumptionToken(req, result);
                cache.storeResumptionToken(token);
            }

        } else {
            token = cache.getResumptionToken(GeonetworkResumptionToken.buildKey(req));
            if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
                Log.debug(Geonet.OAI_HARVESTER, "OAI ListRecords : using ResumptionToken :" + GeonetworkResumptionToken.buildKey(req));

            if (token == null)
                throw new BadResumptionTokenException("No session for token : " + GeonetworkResumptionToken.buildKey(req));

            result = token.getRes();

            pos = GeonetworkResumptionToken.getPos(req);
        }

        ListResponse res = processRequest(req, pos, result, context);
        pos = pos + res.getSize();

        if (token == null && res.getSize() == 0)
            throw new NoRecordsMatchException("No results");

        if (token != null) token.setupToken(pos);
        res.setResumptionToken(token);

        return res;


    }

    //---------------------------------------------------------------------------

    /**
     * Get list of schemas that can convert to the prefix
     */

    private List<String> getSchemasThatCanConvertTo(String prefix) {
        List<String> result = new ArrayList<>();
        for (String schema : schemaMan.getSchemas()) {
            if (Lib.existsConverter(schemaMan.getSchemaDir(schema), prefix)) {
                result.add(schema);
            }
        }
        return result;
    }

    public abstract String getVerb();

    public abstract ListResponse processRequest(TokenListRequest req, int pos, SearchResult result, ServiceContext context) throws Exception;

}
