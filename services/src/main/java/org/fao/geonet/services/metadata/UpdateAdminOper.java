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

package org.fao.geonet.services.metadata;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.jdom.Document;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Stores all operations allowed for a metadata for each groups.
 *
 * In order to set a value for a group use _<groupId>_<operationId>.
 *
 * By default, all operations are removed and then added according to the parameter. In order to set
 * or unset existing operations, add the update parameter with value true and set the off/on status
 * for each operations (eg. _<groupId>_<operationId>=<off|on>.
 *
 * Called by the metadata.admin service (ie. privileges panel).
 *
 * Sample URL: http://localhost:8080/geonetwork/srv/eng/metadata.admin?update=true&id=13962&_1_0=off&_1_1=off&_1_5=off&_1_6=off
 */
@Deprecated
public class UpdateAdminOper extends NotInReadOnlyModeService {
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

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        UserSession us = context.getUserSession();

        String id = Utils.getIdentifierFromParameters(params, context);
        boolean update = Util.getParam(params, Params.UPDATEONLY, "false").equals("true");
		SettingManager sm = context.getBean(SettingManager.class);

		boolean allowPublishInvalidMd = sm.getValueAsBool("metadata/workflow/allowPublishInvalidMd");

        //-----------------------------------------------------------------------
        //--- check access

        Metadata info = context.getBean(MetadataRepository.class).findOne(id);

        if (info == null)
            throw new MetadataNotFoundEx(id);

        //-----------------------------------------------------------------------
        //--- remove old operations

        boolean skip = false;

        //--- in case of owner, privileges for groups 0,1 and GUEST are disabled
        //--- and are not sent to the server. So we cannot remove them

        boolean isAdmin = Profile.Administrator == us.getProfile();
        boolean isReviewer = Profile.Reviewer == us.getProfile();


        if (us.getUserIdAsInt() == info.getSourceInfo().getOwner() && !isAdmin && !isReviewer) {
            skip = true;
        }

        if (!update) {
            dm.deleteMetadataOper(context, id, skip);
        }

        //-----------------------------------------------------------------------
        //--- set new ones

        @SuppressWarnings("unchecked")
        List<Element> list = params.getChildren();

        Pattern opParamPatter = Pattern.compile("_([0-9]+)_([0-9]+)");
        for (Element el : list) {
            String name  = el.getName();
            Matcher matcher = opParamPatter.matcher(name);
            if (matcher.matches()) {
                String groupId = matcher.group(1);
                String operId  = matcher.group(2);

                // Never set editing for reserved group
                if (Integer.parseInt(operId) == ReservedOperation.editing.getId() &&
                    ReservedGroup.isReserved(Integer.valueOf(groupId))) {
                    continue;
                }

				if (!update) {
					// For privileges to ALL group, check if it's allowed or not to publish invalid metadata
					if (groupId.equals(ReservedGroup.all.getId() + "") && (!allowPublishInvalidMd)) {
						if (!canPublishToAllGroup(context, dm, Integer.parseInt(id))) {
							continue;
						}
					}

					dm.setOperation(context, id, groupId, operId);
				} else {
					boolean publish = "on".equals(el.getTextTrim());
					if (publish) {

						// For privileges to ALL group, check if it's allowed or not to publish invalid metadata
						if (groupId.equals(ReservedGroup.all.getId() + "") && (!allowPublishInvalidMd)) {
							if (!canPublishToAllGroup(context, dm, Integer.parseInt(id))) {
								continue;
							}
						}

						dm.setOperation(context, id, groupId, operId);
					} else {
						dm.unsetOperation(context, id, groupId, operId);
					}
				}
			}
		}

        //--- index metadata
        dm.indexMetadata(id, true, null);

        //--- return id for showing
		return new Element(Jeeves.Elem.RESPONSE).addContent(new Element(Geonet.Elem.ID).setText(id));
	}

	/**
	 * For privileges to ALL group, check if it's allowed or not to publish invalid metadata.
	 *
	 * @param context
	 * @param dm
	 * @param mdId
	 * @return
     * @throws Exception
     */
	private boolean canPublishToAllGroup(ServiceContext context, DataManager dm, int mdId) throws Exception {
		MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
		MetadataValidationRepository metadataValidationRepository = context.getBean(MetadataValidationRepository.class);

		boolean hasValidation =
				(metadataValidationRepository.count(MetadataValidationSpecs.hasMetadataId(mdId)) > 0);

		if (!hasValidation) {
			Metadata metadata = metadataRepository.findOne(mdId);

			dm.doValidate(metadata.getDataInfo().getSchemaId(), mdId + "",
					new Document(metadata.getXmlData(false)), context.getLanguage());
			dm.indexMetadata(mdId + "", true, null);
		}

		boolean isInvalid =
				(metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(mdId)) > 0);

		return !isInvalid;
	}
}
