/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet.harvester.wfsfeatures;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.harvester.wfsfeatures.event.WFSHarvesterEvent;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.harvester.wfsfeatures.worker.EsClient;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder;
import org.geonetwork.messaging.JMSMessager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

import static org.fao.geonet.harvester.wfsfeatures.worker.EsWFSFeatureIndexer.deleteFeatures;

/**
 * Created by fgravin on 10/29/15.
 */
@Controller
@RequestMapping(value = {
        "/api/workers/data/wfs/actions",
        "/api/" + API.VERSION_0_1 + "/workers/data/wfs/actions"
})
@Api(value = "workers",
        tags= "workers",
        description = "Workers related operations")
public class WFSHarvesterApi {
    @Autowired
    private JMSMessager jmsMessager;

    @ApiOperation(value = "Index a WFS feature type",
            nickname = "indexWfsFeatureType")
    @RequestMapping(value = "start",
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public JSONObject indexWfs(
            @RequestBody WFSHarvesterParameter config) throws Exception {

        // TODO: Check user is authenticated ?
        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("indexedFeatures",
                sendMessage(config));

        return result;
    }

    @Autowired
    EsClient client;

    @ApiOperation(value = "Delete a WFS feature type",
        nickname = "deleteWfsFeatureType")
    @RequestMapping(
//    @RequestMapping(value = "/{serviceUrl:.*}/{typeName:.*}",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.ALL_VALUE,
        method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public JSONObject deleteWfs(
        @RequestParam
        String serviceUrl,
        @RequestParam
        String typeName) throws Exception {


        deleteFeatures(serviceUrl, typeName, Logger.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME), client);

        // TODO: Check user is authenticated ?
        JSONObject result = new JSONObject();
        result.put("success", true);
//        result.put("indexedFeatures",
//            sendMessage(config));

        return result;
    }

    private JSONObject sendMessage(WFSHarvesterParameter parameters) {
        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();
        WFSHarvesterEvent event = new WFSHarvesterEvent(appContext, parameters);
        // TODO: Messages should be node specific eg. srv channel ?
        jmsMessager.sendMessage(WFSHarvesterRouteBuilder.MESSAGE_HARVEST_WFS_FEATURES, event);

        JSONObject j = new JSONObject();
        j.put("url", parameters.getUrl());
        j.put("featureType", parameters.getTypeName());

        return j;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            Exception.class})
    public Object exceptionHandler(final Exception exception) {
            exception.printStackTrace();
            return  new HashMap() {{
                    put("result", "failed");
                    put("type", "file_not_found");
                    put("message", exception.getClass() + " " + exception.getMessage());
                }};
        }
}
