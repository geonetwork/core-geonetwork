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

import com.google.common.base.Function;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.reusable.ContactsStrategy;
import org.fao.geonet.kernel.reusable.DeletedObjects;
import org.fao.geonet.kernel.reusable.ExtentsStrategy;
import org.fao.geonet.kernel.reusable.FormatsStrategy;
import org.fao.geonet.kernel.reusable.KeywordsStrategy;
import org.fao.geonet.kernel.reusable.MetadataRecord;
import org.fao.geonet.kernel.reusable.ReplacementStrategy;
import org.fao.geonet.kernel.reusable.Utils;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Deletes the objects from deleted reusable object table and unpublishes the
 * referencing metadata
 * 
 * @author jeichar
 */
public class DeleteUnused implements Service {

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        String appPath = context.getAppPath();
        String baseUrl = Utils.mkBaseURL(context.getBaseUrl(), gc.getSettingManager());
        String language = context.getLanguage();
        try {

            process(new ContactsStrategy(dbms, appPath, baseUrl, language, context.getSerialFactory()), context, dbms);
            process(new ExtentsStrategy(baseUrl, appPath, gc.getExtentManager(), language), context, dbms);
            process(new FormatsStrategy(dbms, appPath, baseUrl, language, context.getSerialFactory()), context, dbms);
            process(new KeywordsStrategy(gc.getThesaurusManager(), appPath, baseUrl, language), context, dbms);
            processDeleted(context, dbms);

            return new Element("status").setText("true");
        } catch (Throwable e) {
            return new Element("status").setText("false");
        }
    }

    private void process(ReplacementStrategy strategy, ServiceContext context, Dbms dbms) throws Exception {
        UserSession userSession = context.getUserSession();
        @SuppressWarnings("unchecked")
        List<Element> nonValidated = strategy.find(userSession, false).getChildren();
        List<String> toDelete = new ArrayList<String>();
        final Function<String, String> idConverter = strategy.numericIdToConcreteId(userSession);
        
	    List<String> luceneFields = new LinkedList<String>();
	    luceneFields.addAll(Arrays.asList(strategy.getInvalidXlinkLuceneField()));

        for (Element element : nonValidated) {
            String objId = element.getChildTextTrim(ReplacementStrategy.REPORT_ID);

            Set<MetadataRecord> md = Utils.getReferencingMetadata(context, strategy, luceneFields, objId, false, false, idConverter);
            if (md.isEmpty()) {
                toDelete.add(objId);
            }
        }
        Log.info(Geocat.Module.REUSABLE, "Deleting Reusable objects " + toDelete);
        if (toDelete.size() > 0)
            strategy.performDelete(toDelete.toArray(new String[toDelete.size()]), dbms, userSession, null);
    }

    private void processDeleted(ServiceContext context, Dbms dbms) throws Exception {
        @SuppressWarnings("unchecked")
        List<Element> nonValidated = DeletedObjects.list(dbms).getChildren();
        List<String> toDelete = new ArrayList<String>();
        final Function<String, String> idConverter = ReplacementStrategy.ID_FUNC;

        List<String> fields = Arrays.asList(DeletedObjects.getLuceneIndexField());

        for (Element element : nonValidated) {
            String objId = element.getChildTextTrim(ReplacementStrategy.REPORT_ID);

			Set<MetadataRecord> md = Utils.getReferencingMetadata(context, DeletedObjects.createFindMetadataReferences(), fields, objId,
                    false, false, idConverter);
            if (md.isEmpty()) {
                toDelete.add(objId);
            }
        }
        if (toDelete.size() > 0)
            DeletedObjects.delete(dbms, toDelete.toArray(new String[toDelete.size()]));
    }

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

}
