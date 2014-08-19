//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.geonet.services.reusable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.reusable.DeletedObjects;
import org.fao.geonet.kernel.reusable.MetadataRecord;
import org.fao.geonet.kernel.reusable.ReplacementStrategy;
import org.fao.geonet.kernel.reusable.SendEmailParameter;
import org.fao.geonet.kernel.reusable.Utils;
import org.jdom.Element;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Deletes the objects from deleted reusable object table and unpublishes the
 * referencing metadata
 * 
 * @author jeichar
 */
public class Delete implements Service
{

    public Element exec(Element params, ServiceContext context) throws Exception
    {
    	boolean testing = Boolean.parseBoolean(Util.getParam(params, "testing", "false"));
    	String[] ids = Util.getParamText(params, "id").split(",");

        // PMT c2c : fixing potential security flaw by
        // sanitazing user inputs : every entries here should
        // be cast correctly as integer (or else the service will
        // throw an exception but avoid malicious SQL queries to be
        // passed to the DB server).
        
        // TODO : check if it was possible before to pass UUIDs
        for (int i = 0 ; i < ids.length ; i++) {
        	ids[i] = Integer.toString(Integer.parseInt(ids[i]));
        }
        	
        
        Log.debug(Geocat.Module.REUSABLE, "Starting to delete following rejected objects: ("
                + Arrays.toString(ids) + ")");

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        final String baseUrl = Utils.mkBaseURL(context.getBaseUrl(), gc.getSettingManager());

        Collection<String> metadataIds = new HashSet<String>();
        Multimap<String/* ownerid */, String/* metadataid */> emailInfo = HashMultimap.create();

        for (String id : ids) {
            Set<MetadataRecord> md = Utils.getReferencingMetadata(context, DeletedObjects.createFindMetadataReferences(),
                    Arrays.asList(DeletedObjects.getLuceneIndexField()), id, false, false, ReplacementStrategy.ID_FUNC);
            for (MetadataRecord metadataRecord : md) {
                metadataIds.add(metadataRecord.id);

                // compile a list of email addresses for notifications
                for (MetadataRecord record : md) {
                    emailInfo.put(record.ownerId, record.id);
                }
            }
        }

        if (!emailInfo.isEmpty()) {
            String msg = Utils.translate(context.getAppPath(), context.getLanguage(), "unpublishMetadata/message",
                    " / ");
            String msgHeader = "";
            String subject = Utils.translate(context.getAppPath(), context.getLanguage(), "unpublishMetadata/subject",
                    " / ");

            SendEmailParameter args = new SendEmailParameter(context, dbms, msg, emailInfo, baseUrl, msgHeader, subject, testing);
            Utils.sendEmail(args);
        }

        Utils.unpublish(metadataIds, context);
        for (String metadataId : metadataIds) {
            gc.getDataManager().indexMetadata(dbms, metadataId, true, context, false, false, true);
        }
        DeletedObjects.delete(dbms, ids);

        Log.debug(Geocat.Module.REUSABLE, "Successfully deleted following rejected objects: \n("
                + Arrays.toString(ids) + ")");

        Log.debug(Geocat.Module.REUSABLE,
                "Unpublished following metadata a result of deleting rejected objects: \n("
                        + Arrays.toString(metadataIds.toArray()) + ")");

        return null;
    }

    public void init(String appPath, ServiceConfig params) throws Exception
    {
    }

}
