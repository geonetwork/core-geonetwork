/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_LOCALRATING_ENABLE;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.userfeedback.RatingsSetting;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.BadServerResponseEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet.GeonetHarvester;
import org.fao.geonet.kernel.harvest.harvester.geonet.GeonetParams;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;

@RequestMapping(value = {
    "/{portal}/api/records",
    "/{portal}/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordSocial")
@ReadWriteController
public class MetadataSocialApi {

    @Autowired
    LanguageUtils languageUtils;


    @ApiOperation(
        value = "Rate a record",
        notes = "User rating of metadata. If the metadata was harvested using the 'GeoNetwork' protocol and the " +
            "system setting localrating/enable is false (the default), the user's rating is shared between " +
            "GN nodes in this harvesting network. If the metadata was not harvested or if " +
            "localrating/enable is true then 'local rating' is applied, counting only rating from users of " +
            "this node.<br/>" +
            "When a remote rating is applied, the local rating is not updated. It will be updated on the next " +
            "harvest run (FIXME ?).",
        nickname = "rate")
    @RequestMapping(
        value = "/{metadataUuid}/rate",
        method = RequestMethod.PUT
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New rating value."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_VIEW)
    })
    public
    @ResponseBody
    Integer rateRecord(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Rating",
            required = true
        )
        @RequestBody(
            required = true
        )
            Integer rating,
        HttpServletRequest request
    )
        throws Exception {
        AbstractMetadata metadata = ApiUtils.canViewRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        String ip = context.getIpAddress();
        if (ip == null) {
            ip = "???.???.???.???";
        }

        if (rating < 0 || rating > 5) {
            throw new BadParameterEx(String.format(
                "Parameter rating MUST be between 1 and 5. Value %s is invalid."), rating);
        }

        String harvUuid = metadata.getHarvestInfo().getUuid();

        // look up value of localrating/enable
        DataManager dataManager = appContext.getBean(DataManager.class);
        HarvestManager hm = appContext.getBean(HarvestManager.class);
        SettingManager settingManager = appContext.getBean(SettingManager.class);
        String localRating = settingManager.getValue(SYSTEM_LOCALRATING_ENABLE);

        if (localRating.equals(RatingsSetting.BASIC) || harvUuid == null) {
            //--- metadata is local, just rate it
            rating = dataManager.rateMetadata(metadata.getId(), ip, rating);
        } else {
            //--- the metadata is harvested, is type=geonetwork?
            AbstractHarvester ah = hm.getHarvester(harvUuid);
            if (ah.getType().equals(GeonetHarvester.TYPE)) {
                rating = setRemoteRating(context, (GeonetParams) ah.getParams(), metadataUuid, rating);
            } else {
                rating = -1;
            }
        }
        return rating;
    }

    /**
     * TODO GN-API: Needs update on new API
     */
    private int setRemoteRating(ServiceContext context, GeonetParams params, String uuid, int rating) throws Exception {
        if (context.isDebugEnabled()) context.debug("Rating remote metadata with uuid:" + uuid);

        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(params.host));

        Lib.net.setupProxy(context, req);

        req.setAddress(params.getServletPath() + "/srv/eng/" + Geonet.Service.XML_METADATA_RATE);
        req.clearParams();
        req.addParam("uuid", uuid);
        req.addParam("rating", rating);

        Element response = req.execute();

        if (!response.getName().equals(Params.RATING)) {
            throw new BadServerResponseEx(response);
        }

        return Integer.parseInt(response.getText());
    }
}
