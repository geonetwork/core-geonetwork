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

import io.swagger.annotations.*;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.rdf.RdfOutputManager;
import org.fao.geonet.api.records.rdf.RdfSearcher;
import org.fao.geonet.kernel.ThesaurusManager;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("catalogs")
@ReadWriteController
public class CatalogApi {

    @Autowired
    ThesaurusManager thesaurusManager;


    @ApiOperation(value = "Get catalog content as RDF",
        notes = ".",
        nickname = "getAsRdf")
    @RequestMapping(
        method = RequestMethod.GET,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            "application/rdf+xml"
        })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Return the catalog content as RDF.")
    })
    public
    @ResponseBody
    void getAsRdf(
        @ApiIgnore
        @ApiParam(hidden = true)
        @RequestParam
            Map<String,String> allRequestParams,
        HttpServletResponse response,
        HttpServletRequest request
    ) throws Exception {
        ServiceContext context = ApiUtils.createServiceContext(request);
        RdfOutputManager manager = new RdfOutputManager(
            thesaurusManager.buildResultfromThTable(context));

        // Mimic old Jeeves param style
        Element params = new Element("params");
        allRequestParams.forEach((k, v) -> {
            params.addContent(new Element(k).setText(v));
        });

        RdfSearcher rdfSearcher = new RdfSearcher(params, context);
        File rdfFile = manager.createRdfFile(context, rdfSearcher);

        try (
            ServletOutputStream out = response.getOutputStream();
            InputStream in = new FileInputStream(rdfFile);
        ) {
            byte[] bytes = new byte[1024];
            int bytesRead;

            response.setContentType("application/rdf+xml");

            while ((bytesRead = in.read(bytes)) != -1) {
                out.write(bytes, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
