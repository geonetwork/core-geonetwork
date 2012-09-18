//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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

package org.fao.geonet.services.metadata;

import jeeves.exceptions.MissingParameterEx;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.diff.DifferenceAnnotator;
import org.fao.geonet.kernel.diff.MetadataDifference;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO javadoc.
 *
 * @author heikki doeleman
 */
public class Diff implements Service {

    /**
     * TODO javadoc.
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
	public void init(String appPath, ServiceConfig params) throws Exception {
        MetadataDifference.init(appPath);
    }

    /**
     * TODO javadoc.
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
	public Element exec(Element params, final ServiceContext context) throws Exception {
        try {
            Log.debug(Geonet.DIFF, "Diff service exec with params:\n" + Xml.getString(params));

            String id1 = Util.getParam(params, Params.FIRST, "");
            String id2 = Util.getParam(params, Params.SECOND, "");
            boolean editMode = Boolean.parseBoolean(Util.getParam(params, Params.EDITMODE, "false"));

            //
            // restrict access
            //
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            DataManager dataMan = gc.getDataManager();
            AccessManager accessMan = gc.getAccessManager();
            UserSession session = context.getUserSession();
            String userId = session.getUserId();

            List<String> idsToUse = figureOutIds(id1, id2, context, dataMan, dbms, params);
            id1 = idsToUse.get(0);
            id2 = idsToUse.get(1);

            boolean compareWithWorkspace = id1.equals(id2);
            boolean canEdit = accessMan.canEdit(context, id1) && accessMan.canEdit(context, id2);
            MdInfo info = dataMan.getMetadataInfo(dbms, id1);

            // view mode
            if(!editMode) {
                // comparing 2 different metadata
                if(!compareWithWorkspace) {
                    // allow if canEdit
                    if(!canEdit) {
                        throw new OperationNotAllowedEx("You can not compare these because you are not authorized to edit one or both of these metadata.");
                    }
                }
                // comparing with workspace copy
                else {
                    // allow if canEdit
                    if(!canEdit) {
                        throw new OperationNotAllowedEx("You can not compare this to its workspace copy because you are not authorized to edit this metadata");
                    }
                    //and md is locked
                    if(!info.isLocked) {
                        throw new OperationNotAllowedEx("You can not compare this to its workspace copy because this metadata is not locked.");
                    }
                }
            }
            // edit mode
            else {
                // allow if comparing workspace, and md is locked by you
                if(!compareWithWorkspace) {
                    throw new OperationNotAllowedEx("You can not compare two different metadata in Edit mode");
                }
                else if(info.isLocked && !info.lockedBy.equals(userId)) {
                    throw new OperationNotAllowedEx("You can not compare this to its workspace copy in Edit mode because you do not own the lock.");
                }
            }

            Element md1;
            Element md2;

            md1 = dataMan.getMetadata(context, id1, false, false, false);

            // if not compare with workspace copy
            if (!compareWithWorkspace) {
                md2 = dataMan.getMetadata(context, id2, false, false, false);
            }
            // compare with workspace copy
            else {
                md2 = dataMan.getMetadataFromWorkspace(context, id1, false, false, false, true) ;
            }

            Element diff = MetadataDifference.diff(md1, md2);

            Element annotatedMd1 = DifferenceAnnotator.addDelta(md1, diff, DifferenceAnnotator.DifferenceDirection.SOURCE);
            Element annotatedMd2 = DifferenceAnnotator.addDelta(md2, diff, DifferenceAnnotator.DifferenceDirection.TARGET);

            // in editmode, further annotate the target metadata with editing annotations, and store it in session
            if(editMode) {
                String schema = dataMan.getMetadataSchema(dbms, id2);
                dataMan.getEditLib().expandElements(schema, annotatedMd2);
                dataMan.getEditLib().getVersionForEditing(schema, id2, annotatedMd2);
                session.setProperty(Geonet.Session.METADATA_EDITING + id2, annotatedMd2);
            }

            Element response = new Element("response");
            Element source = new Element("source");
            source.addContent(annotatedMd1);
            response.addContent(source);
            Element target = new Element("target");
            target.addContent(annotatedMd2);
            response.addContent(target);

            return response;
        }
        catch (MissingParameterEx x) {
            Log.error(Geonet.DIFF, x.getMessage());
            x.printStackTrace();
            throw new Exception(x.getMessage(), x);
        }
    }

    /**
     * TODO javadoc.
     *
     * @param id1
     * @param id2
     * @param context
     * @param dataMan
     * @param dbms
     * @param params
     * @return
     * @throws Exception
     */
    private List<String> figureOutIds(String id1, String id2, ServiceContext context, DataManager dataMan, Dbms dbms, Element params) throws Exception{
        boolean useSelection = StringUtils.isEmpty(id1) && StringUtils.isEmpty(id2);
        boolean compareWithWorkspaceCopy = false;

        // If no provide the uuids of metadata to compare, use the selected metadata
        if (useSelection) {
            Log.debug(Geonet.DIFF, "Diff service exec: useSelection");

            UserSession us = context.getUserSession();
            SelectionManager sm = SelectionManager.getManager(us);
            if(Log.isDebugEnabled(Geonet.DIFF)) {
                Log.debug(Geonet.DIFF, "Diff service exec: " + sm.getSelection("metadata").size());
            }
            // Should be selected 2 metadata records to use this
            if (sm.getSelection("metadata").size() != 2) {
                throw new Exception("The metadata selection should contain 2 metadata records.");
            }
            else {
                Object[] s = sm.getSelection("metadata").toArray();

                if(Log.isDebugEnabled(Geonet.DIFF)) {
                    Log.debug(Geonet.DIFF, "Diff service exec: " + (String) s[0] + " " + (String) s[1]);
                }
                id1 = dataMan.getMetadataId(dbms, (String) s[0]);
                id2 = dataMan.getMetadataId(dbms, (String) s[1]);

                if(Log.isDebugEnabled(Geonet.DIFF)) {
                    Log.debug(Geonet.DIFF, "Diff service exec: " + id1 + " " + id2);
                }
            }
        }
        // compare with workspace copy
        else {
            if (StringUtils.isEmpty(id1)) {
                throw new MissingParameterEx(Params.FIRST, params);
            }
            //if (StringUtils.isEmpty(id2)) throw new MissingParameterEx(Params.SECOND, params);
            compareWithWorkspaceCopy = StringUtils.isEmpty(id2);
            if(compareWithWorkspaceCopy) {
                id2 = id1;
            }
        }

        List<String> result = new ArrayList<String>(2);
        result.add(id1);
        result.add(id2);
        return result;

    }
}