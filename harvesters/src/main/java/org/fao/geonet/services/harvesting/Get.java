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

package org.fao.geonet.services.harvesting;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.ObjectNotFoundEx;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.jdom.Content;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 *
 */
public class Get implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        //--- if 'id' is null all entries are returned

        @SuppressWarnings("unchecked")
        List<Element> idEls = params.getChildren("id");
        boolean onlyInfo = org.fao.geonet.Util.getParam(params, "onlyInfo", false);
        String sortField = org.fao.geonet.Util.getParam(params, "sortField", "site[1]/name[1]");

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        final SettingManager settingManager = gc.getApplicationContext().getBean(SettingManager.class);
        String[] disabledTypes = StringUtils.split(
            StringUtils.defaultIfBlank(
                settingManager.getValue(Settings.SYSTEM_HARVESTER_DISABLED_HARVESTER_TYPES),
                "").toLowerCase().replace(',', ' '),
            " ");

        List<String> ids;
        if (idEls.isEmpty()) {
            ids = Collections.singletonList(null);
        } else {
            ids = idEls.stream().map(Element::getTextTrim).collect(Collectors.toList());
        }
        final HarvestManager harvestManager = gc.getBean(HarvestManager.class);
        Element result = new Element("nodes");
        for (String id : ids) {
            Element node = harvestManager.get(id, context, sortField);

            if (node != null) {
                if (idEls.isEmpty() || id.equals("-1")) {
                    List<Element> childNodes = node.getChildren();
                    for (Element childNode : childNodes) {
                        String harvesterType = childNode.getAttributeValue("type");
                        if (Arrays.stream(disabledTypes).noneMatch(disabledType -> disabledType.equalsIgnoreCase(harvesterType))) {
                            result.addContent((Content) childNode.clone());
                        }
                    }
                } else {
                    String harvesterType = node.getAttributeValue("type");
                    if (Arrays.stream(disabledTypes).noneMatch(disabledType -> disabledType.equalsIgnoreCase(harvesterType))) {
                        result.addContent(node.detach());
                    }
                }
            } else {
                throw new ObjectNotFoundEx("No Harvester found with id: " + id);
            }
        }

        if (onlyInfo) {
            removeAllDataExceptInfo(result);
        }

        return result;
    }

    private void removeAllDataExceptInfo(Element node) {
        final List<Element> toRemove = Lists.newArrayList();
        @SuppressWarnings("unchecked")
        final List<Element> children = node.getChildren();

        for (Element harvesters : children) {
            @SuppressWarnings("unchecked")
            final List<Element> harvesterInfo = harvesters.getChildren();
            for (Element element : harvesterInfo) {
                if (!element.getName().equalsIgnoreCase("info") && !element.getName().equalsIgnoreCase("error")) {
                    toRemove.add(element);
                }
            }
        }

        for (Element element : toRemove) {
            element.detach();
        }
    }
}
