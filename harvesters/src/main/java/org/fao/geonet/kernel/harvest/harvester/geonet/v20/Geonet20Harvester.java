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

package org.fao.geonet.kernel.harvest.harvester.geonet.v20;

import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

//=============================================================================

public class Geonet20Harvester extends AbstractHarvester<HarvestResult, GeonetParams> {

    private GeonetResult result;

    private String servletName;

    @Override
    protected GeonetParams createParams() {
        return new GeonetParams(dataMan);
    }

    protected void storeNodeExtra(GeonetParams params, String path,
                                  String siteId, String optionsId) throws SQLException {
        setParams(params);

        harvesterSettingsManager.add("id:" + siteId, "host", params.host);

        //--- store search nodes

        for (Search s : params.getSearches()) {
            String searchID = harvesterSettingsManager.add(path, "search", "");

            harvesterSettingsManager.add("id:" + searchID, "freeText", s.freeText);
            harvesterSettingsManager.add("id:" + searchID, "title", s.title);
            harvesterSettingsManager.add("id:" + searchID, "abstract", s.abstrac);
            harvesterSettingsManager.add("id:" + searchID, "keywords", s.keywords);
            harvesterSettingsManager.add("id:" + searchID, "digital", s.digital);
            harvesterSettingsManager.add("id:" + searchID, "hardcopy", s.hardcopy);
            harvesterSettingsManager.add("id:" + searchID, "siteId", s.siteId);
        }
    }

    public void addHarvestInfo(Element info, String id, String uuid) {
        super.addHarvestInfo(info, id, uuid);

        String small = params.host +
            "/srv/en/resources.get2?access=public&uuid=" + uuid + "&fname=";

        info.addContent(new Element("smallThumbnail").setText(small));
    }

    protected void doAddInfo(Element node) {
        //--- if the harvesting is not started yet, we don't have any info

        if (result == null)
            return;

        //--- ok, add proper info

        Element info = node.getChild("info");

        for (HarvestResult ar : result.alResult) {
            Element site = new Element("search");
            site.setAttribute("siteId", ar.siteId);

            add(site, "total", ar.totalMetadata);
            add(site, "added", ar.addedMetadata);
            add(site, "updated", ar.updatedMetadata);
            add(site, "unchanged", ar.unchangedMetadata);
            add(site, "skipped", ar.schemaSkipped + ar.uuidSkipped);
            add(site, "removed", ar.locallyRemoved);
            add(site, "doesNotValidate", ar.doesNotValidate);

            info.addContent(site);
        }
    }

    public Element getResult() {
        return new Element("result"); // HarvestHistory not supported for this old harvester
    }

    public void doHarvest(Logger log) throws Exception {
        CategoryMapper localCateg = new CategoryMapper(context);

        final URL url = new URL(params.host);

        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class)
            .createXmlRequest(url.getHost(), url.getPort());

        servletName = url.getPath();

        Lib.net.setupProxy(context, req);

        String name = getParams().getName();

        //--- login

        if (params.isUseAccount()) {
            log.info("Login into : " + name);

            req.setAddress(servletName + "/srv/en/" + Geonet.Service.XML_LOGIN);
            req.addParam("username", params.getUsername());
            req.addParam("password", params.getPassword());

            Element response = req.execute();

            if (!response.getName().equals("ok"))
                throw new UserNotFoundEx(params.getUsername());
        }
        if (cancelMonitor.get()) {
            return;
        }

        //--- search

        result = new GeonetResult();

        Aligner aligner = new Aligner(cancelMonitor, log, req, params, dataMan, metadataManager, context, localCateg);

        for (Search s : params.getSearches()) {
            if (cancelMonitor.get()) {
                return;
            }

            log.info("Searching on : " + name + "/" + s.siteId);

            req.setAddress(servletName + "/srv/en/" + Geonet.Service.XML_SEARCH);

            Element searchResult = req.execute(s.createRequest());

            if (log.isDebugEnabled()) log.debug("Obtained:\n" + Xml.getString(searchResult));

            //--- site alignment
            HarvestResult ar = aligner.align(searchResult, s.siteId);

            //--- collect some stats
            result.alResult.add(ar);
        }

        //--- logout

        if (params.isUseAccount()) {
            log.info("Logout from : " + name);

            req.clearParams();
            req.setAddress("/" + params.getServletPath() + "/srv/en/" + Geonet.Service.XML_LOGOUT);
        }

        metadataManager.flush();
    }
}

//=============================================================================

class GeonetResult {
    public ArrayList<HarvestResult> alResult = new ArrayList<HarvestResult>();
}
