//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import jeeves.constants.Jeeves;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;

//=============================================================================

/**
 * Utility class used by HarvestManager to schedule background activities.
 * <p>
 * Please note that background activities make use of a shared service context and
 * do not have access to the user session unless you take special care
 * to provide a service context for their use.
 * </p>
 */
public class Util {
    //--------------------------------------------------------------------------
    //---
    //--- Job interface
    //---
    //--------------------------------------------------------------------------

    /**
     * Utility method used to schedule job on a number of metadata records.
     * <p>
     * Exec will process the provided job for each id provided as part of params.
     * </p>
     * @param params Element listing harvesters to run
     * @param context Service context used t look up GeonetContext
     * @param job Job to run for each indicated harvester
     * @return Response structured with each harvest job and their
     * @throws Exception
     */
    public static Element exec(Element params, ServiceContext context, Job job) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        HarvestManager hm = gc.getBean(HarvestManager.class);

        @SuppressWarnings("unchecked")
        List<Element> paramList = params.getChildren();

        Element response = new Element(Jeeves.Elem.RESPONSE);

        for (Element el : paramList) {
            String id = el.getText();
            String res = job.execute(hm, id).toString();

            el = new Element("id")
                .setText(id)
                .setAttribute(new Attribute("status", res));

            response.addContent(el);
        }

        return response;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Exec service: executes the job on all input ids returning the status
    //---               for each one
    //---
    //--------------------------------------------------------------------------

    /**
     * Execute job to run on all input ids, the status is returned for each one.
     */
    public interface Job {
        /**
         * Execute job on input id, returning status.
         *
         * @param hm HarvestManager scheduling activity
         * @param id harvester id
         * @return operation result indicating job status
         * @throws Exception
         */
        OperResult execute(HarvestManager hm, String id) throws Exception;
    }
}

//=============================================================================

