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

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasOperation;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.nio.file.Path;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.api.records.editing.AjaxEditUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * For editing : update leaves information. Access is restricted.
 */
@Deprecated
public class Update extends NotInReadOnlyModeService {
    private ServiceConfig config;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        config = params;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        AjaxEditUtils ajaxEditUtils = new AjaxEditUtils(context);
        ajaxEditUtils.preprocessUpdate(params, context);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        IMetadataValidator validator = gc.getBean(IMetadataValidator.class);
        UserSession session = context.getUserSession();

        String id = Utils.getIdentifierFromParameters(params, context);
        String isTemplate = Util.getParam(params, Params.TEMPLATE, "n");
        String showValidationErrors = Util.getParam(params, Params.SHOWVALIDATIONERRORS, "false");
        String data = params.getChildText(Params.DATA);
        String minor = Util.getParam(params, Params.MINOREDIT, "false");

        boolean finished = config.getValue(Params.FINISHED, "no").equals("yes");
        boolean forget = config.getValue(Params.FORGET, "no").equals("yes");
        boolean commit = config.getValue(Params.START_EDITING_SESSION, "no").equals("yes");

        if (!forget) {
            int iLocalId = Integer.parseInt(id);
            dataMan.setTemplateExt(iLocalId, MetadataType.lookup(isTemplate));

            //--- use StatusActionsFactory and StatusActions class to possibly
            //--- change status as a result of this edit (use onEdit method)
            StatusActionsFactory saf = context.getBean(StatusActionsFactory.class);
            StatusActions sa = saf.createStatusActions(context);
            sa.onEdit(iLocalId, minor.equals("true"));

            if (data != null) {
                Element md = Xml.loadString(data, false);

                String changeDate = null;
                boolean validate = showValidationErrors.equals("true");
                boolean updateDateStamp = !minor.equals("true");
                boolean ufo = true;
                boolean index = true;
                dataMan.updateMetadata(context, id, md, validate, ufo, index, context.getLanguage(), changeDate, updateDateStamp);
            } else {
                ajaxEditUtils.updateContent(params, false, true);
            }
        } else {
            dataMan.cancelEditingSession(context, id);
        }

        //-----------------------------------------------------------------------
        //--- update element and return status

        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        elResp.addContent(new Element(Geonet.Elem.ID).setText(id));
        String tocIndex = params.getChildText(Geonet.Elem.TOC_INDEX);
        elResp.addContent(new Element(Geonet.Elem.TOC_INDEX).setText(tocIndex));
        elResp.addContent(new Element(Geonet.Elem.SHOWVALIDATIONERRORS).setText(showValidationErrors));
        boolean justCreated = Util.getParam(params, Params.JUST_CREATED, null) != null;
        if (justCreated) {
            elResp.addContent(new Element(Geonet.Elem.JUSTCREATED).setText("true"));
        }
        elResp.addContent(new Element(Params.MINOREDIT).setText(minor));

        //--- if finished then remove the XML from the session
        if (finished) {
            ajaxEditUtils.removeMetadataEmbedded(session, id);

            dataMan.endEditingSession(id, session);
        }

		if (finished && !forget && !commit) {
			SettingManager sm = gc.getBean(SettingManager.class);

			boolean forceValidationOnMdSave = sm.getValueAsBool("metadata/workflow/forceValidationOnMdSave");

			boolean reindex = false;

			// Save validation if the forceValidationOnMdSave is enabled
			if (forceValidationOnMdSave && !showValidationErrors.equals("true")) {
				final IMetadataUtils metadataRepository = gc.getBean(IMetadataUtils.class);
				AbstractMetadata metadata = metadataRepository.findOne(id);

				validator.doValidate(metadata, context.getLanguage());
				reindex = true;
			}

			boolean automaticUnpublishInvalidMd = sm.getValueAsBool("metadata/workflow/automaticUnpublishInvalidMd");

			// Unpublish the metadata automatically if the setting automaticUnpublishInvalidMd is enabled and
			// the metadata becomes invalid
			if (automaticUnpublishInvalidMd) {
				final OperationAllowedRepository operationAllowedRepo = context.getBean(OperationAllowedRepository.class);

				boolean isPublic = (operationAllowedRepo.count(where(hasMetadataId(id)).and(hasOperation(ReservedOperation
						.view)).and(hasGroupId(ReservedGroup.all.getId()))) > 0);

				if (isPublic) {
					final MetadataValidationRepository metadataValidationRepository = gc.getBean(MetadataValidationRepository.class);

					boolean isInvalid =
							(metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(Integer.parseInt(id))) > 0);

					if (isInvalid) {
						operationAllowedRepo.deleteAll(where(hasMetadataId(id)).and(hasGroupId(ReservedGroup.all.getId())));
					}

					reindex = true;
				}

			}

			if (reindex) {
				dataMan.indexMetadata(id, true, null);
			}


		}
        if (!finished && !forget && commit) {
            dataMan.startEditingSession(context, id);
        }
        return elResp;
    }
}
