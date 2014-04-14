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

import org.apache.commons.io.FileUtils;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specifications;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * Creates a metadata copying data from a given template.
 */
public class Create extends NotInReadOnlyModeService {
    boolean useEditTab = false;

    public void init(String appPath, ServiceConfig params) throws Exception {
        useEditTab = params.getValue("editTab", "false").equals("true");
    }
    
	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm = gc.getBean(DataManager.class);

		String child = Util.getParam(params, Params.CHILD, "n");
		String isTemplate = Util.getParam(params, Params.TEMPLATE, "n");
		String id = "";
		String uuid = "";
		boolean haveAllRights = Boolean.valueOf(Util.getParam(params, Params.FULL_PRIVILEGES, "false"));
		
		// does the request contain a UUID ?
		try {
			uuid = Util.getParam(params, Params.UUID);
			// lookup ID by UUID
			id = dm.getMetadataId(uuid);
		}
		catch(BadInputEx x) {
			try {
				id = Util.getParam(params, Params.ID);
				uuid = dm.getMetadataUuid(id);
			}
			// request does not contain ID
			catch(BadInputEx xx) {
				// give up
				throw new Exception("Request must contain a UUID or an ID");
			}		
		}
		
		String groupOwner= Util.getParam(params, Params.GROUP);
		
		// TODO : Check user can create a metadata in that group
		UserSession user = context.getUserSession();
		if (user.getProfile() != Profile.Administrator) {
            final Specifications<UserGroup> spec = where(UserGroupSpecs.hasProfile(Profile.Editor))
                    .and(UserGroupSpecs.hasUserId(user.getUserIdAsInt()))
                    .and(UserGroupSpecs.hasGroupId(Integer.valueOf(groupOwner)));

            final List<UserGroup> userGroups = context.getBean(UserGroupRepository.class).findAll(spec);

			if (userGroups.size() == 0) {
				throw new ServiceNotAllowedEx("Service not allowed. User needs to be Editor in group with id " + groupOwner);
			}
		}
		
		//--- query the data manager

        String newId = dm.createMetadata(context, id, groupOwner,
                gc.getBean(SettingManager.class).getSiteId(), context.getUserSession().getUserIdAsInt(),
												  (child.equals("n")?null:uuid), isTemplate, haveAllRights);

        try {
          copyDataDir(context, id, newId, Params.Access.PUBLIC);
          copyDataDir(context, id, newId, Params.Access.PRIVATE);
        } catch (IOException e) {
          Log.warning(Geonet.DATA_MANAGER, "Error while copying metadata resources. " + e.getMessage() +  
              ". Metadata is created but without resources from record with id:" + id);
        }
        
        Element response = new Element(Jeeves.Elem.RESPONSE);
        response.addContent(new Element(Geonet.Elem.JUSTCREATED).setText("true"));
        
        String sessionTabProperty = useEditTab ? Geonet.Session.METADATA_EDITING_TAB : Geonet.Session.METADATA_SHOW;
        
        // Set current tab for new editing session if defined.
        Element elCurrTab = params.getChild(Params.CURRTAB);
        if (elCurrTab != null) {
            context.getUserSession().setProperty(sessionTabProperty, elCurrTab.getText());
        }
        response.addContent(new Element(Geonet.Elem.ID).setText(newId));
		return response;
	}

    private void copyDataDir(ServiceContext context, String oldId, String newId, String access) throws IOException {
        final String sourceDir = Lib.resource.getDir(context, access, oldId);
        final String destDir = Lib.resource.getDir(context, access, newId);
        if (new File(sourceDir).exists()) {
            if (!new File(destDir).mkdirs()){
                Log.warning(Geonet.GEONETWORK, "Error creating the metadata data directory.");
            }

            FileUtils.copyDirectory(new File(sourceDir), new File(destDir));
        }
    }
}
