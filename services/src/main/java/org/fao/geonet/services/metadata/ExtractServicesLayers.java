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


// Author : Pierre Mauduit <pierre.mauduit@camptocamp.com>
//
// This webservice allows retrieval of multiple layers / services from
// a selection set.
//
package org.fao.geonet.services.metadata;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@Controller
public class ExtractServicesLayers {

    @RequestMapping(value = "/{portal}/{lang}/selection.layers")
    @ResponseBody
    public JSONObject getLayersFromSelectedMetadatas(
        @PathVariable String lang,
        @RequestParam(value = "id", required = false) String paramId,
        final NativeWebRequest webRequest) throws Exception {

        final ServiceManager serviceManager = ApplicationContextHolder.get().getBean(ServiceManager.class);
        ServiceContext context = serviceManager.createServiceContext("selection.layers", lang, webRequest.getNativeRequest(HttpServletRequest.class));

        DataManager dm = context.getBean(DataManager.class);
        UserSession us = context.getUserSession();
        SelectionManager sm = SelectionManager.getManager(us);

        JSONObject ret = new JSONObject();
        JSONArray services = new JSONArray();
        JSONArray layers = new JSONArray();

        ArrayList<String> lst = new ArrayList<String>();

        // case #1 : #id parameter is undefined
        if (paramId == null) {
            synchronized (sm.getSelection("metadata")) {
                for (Iterator<String> iter = sm.getSelection("metadata").iterator(); iter.hasNext(); ) {
                    String uuid = iter.next();
                    String id = dm.getMetadataId(uuid);
                    lst.add(id);
                }
            }
        } else { // case #2 : id parameter has been passed
            lst.add(paramId);
        }

        for (Iterator<String> iter = lst.iterator(); iter.hasNext(); ) {
            String id = iter.next();
            String uuid = dm.getMetadataUuid(id);

            Element curMd = dm.getMetadata(context, id, false, false, false);

            XPath xpath = XPath.newInstance("gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine");

            List<Element> elems;

            try {
                @SuppressWarnings("unchecked")
                List<Element> tmp = xpath.selectNodes(curMd);
                elems = tmp;
            } catch (Exception e) {
                // Bad XML input ?
                continue;
            }

            for (Element curnode : elems) {
                XPath pLinkage = XPath.newInstance("gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
                XPath pProtocol = XPath.newInstance("gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString");
                XPath pName = XPath.newInstance("gmd:CI_OnlineResource/gmd:name/gco:CharacterString");
                XPath pDescription = XPath.newInstance("gmd:CI_OnlineResource/gmd:description/gco:CharacterString");

                Element eLinkage = (Element) pLinkage.selectSingleNode(curnode);
                Element eProtocol = (Element) pProtocol.selectSingleNode(curnode);
                Element eName = (Element) pName.selectSingleNode(curnode);
                Element eDescription = (Element) pDescription.selectSingleNode(curnode);

                if (eLinkage == null) {
                    continue;
                }
                if (eProtocol == null) {
                    continue;
                }
                if (eName == null) {
                    continue;
                }

                String sLinkage = eLinkage.getValue();
                String sProtocol = eProtocol.getValue();
                String sName = eName.getValue();
                String sDescription = eDescription != null ? eDescription.getValue() : "";

                if ((sLinkage == null) || (sLinkage.equals(""))) {
                    continue;
                }
                if ((sProtocol == null) || (sProtocol.equals(""))) {
                    continue;
                }

                String sProto2 = "WMS"; // by default

                if (sProtocol.contains("OGC:WMS")) {
                    sProto2 = "WMS";
                } else if (sProtocol.contains("OGC:WFS")) {
                    sProto2 = "WFS";
                } else if (sProtocol.contains("OGC:WCS")) {
                    sProto2 = "WMS";
                } else {
                    continue;
                }

                // If no name, we are on a service
                if ((sName == null) || (sName.equals(""))) {
                    JSONObject serviceObj = new JSONObject();
                    serviceObj.put("owsurl", sLinkage);
                    serviceObj.put("owstype", sProto2);
                    serviceObj.put("text", sDescription);
                    serviceObj.put("mdid", id);
                    serviceObj.put("muuid", uuid);
                    services.add(serviceObj);
                }
                // else it is a Layer
                else {
                    JSONObject layerObj = new JSONObject();
                    layerObj.put("owsurl", sLinkage);
                    layerObj.put("owstype", sProto2);
                    layerObj.put("layername", sName);
                    layerObj.put("title", sDescription);
                    layerObj.put("mdid", id);
                    layerObj.put("muuid", uuid);
                    layers.add(layerObj);
                }
            } // for
        } // iterates

        ret.put("layers", layers);
        ret.put("services", services);

        return ret;

    }
}
