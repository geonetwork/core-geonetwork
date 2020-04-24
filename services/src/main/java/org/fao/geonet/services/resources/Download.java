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

package org.fao.geonet.services.resources;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.ResourceNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.resources.handlers.IResourceDownloadHandler;
import org.fao.geonet.util.MailSender;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//=============================================================================

/**
 * Sends the resource to the client
 */
@Controller
@Deprecated
public class Download {

    @Autowired
    private DataManager dataManager;
    @Autowired
    private ServiceManager serviceManager;

    @RequestMapping(value = "/{portal}/{lang}/resources.get")
    public HttpEntity<byte[]> exec(@PathVariable String lang,
                                   @RequestParam(value = Params.ID, required = false) String idParam,
                                   @RequestParam(value = Params.UUID, required = false) String uuidParam,
                                   @RequestParam(Params.FNAME) String fname,
                                   @RequestParam(value = Params.ACCESS, defaultValue = Params.Access.PUBLIC) String access,
                                   NativeWebRequest request) throws Exception {
        String id = null;
        if (idParam != null) {
            id = idParam;
        } else if (uuidParam != null) {
            id = dataManager.getMetadataId(uuidParam);
        } else {
            throw new Exception("Request must contain a UUID ("
                + Params.UUID + ") or an ID (" + Params.ID + ")");
        }

        final HttpServletRequest httpServletRequest = request.getNativeRequest(HttpServletRequest.class);
        final ServiceContext context = serviceManager.createServiceContext("resources.get", lang, httpServletRequest);
        boolean doNotify = false;

        FilePathChecker.verify(fname);

		if (access.equals(Params.Access.PRIVATE))
		{
			Lib.resource.checkPrivilege(context, id, ReservedOperation.download);
			doNotify = true;
		}

		// Build the response
        final Store store = context.getBean("resourceStore", Store.class);
		if (uuidParam == null) {
            uuidParam = dataManager.getMetadataUuid(id);
        }
        try (Store.ResourceHolder resource = store.getResource(context, uuidParam, MetadataResourceVisibility.parse(access), fname, true)) {
            Path file = resource.getPath();
            context.info("File is : " + file);

            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            SettingManager sm = gc.getBean(SettingManager.class);
            DataManager dm = gc.getBean(DataManager.class);

            //--- increase metadata popularity
            if (access.equals(Params.Access.PRIVATE))
                dm.increasePopularity(context, id);

            //--- send email notification
            if (doNotify) {
                String host = sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_HOST);
                String port = sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PORT);
                String from = sm.getValue(Settings.SYSTEM_FEEDBACK_EMAIL);

                String fromDescr = "GeoNetwork administrator";

                if (host.trim().length() == 0 || from.trim().length() == 0) {
                    if (context.isDebugEnabled()) {
                        context.debug("Skipping email notification");
                    }
                } else {
                    if (context.isDebugEnabled()) {
                        context.debug("Sending email notification for file : " + file);
                    }

                    // send emails about downloaded file to groups with notify privilege

                    OperationAllowedRepository opAllowedRepo = context.getBean(OperationAllowedRepository.class);
                    final GroupRepository groupRepository = context.getBean(GroupRepository.class);

                    List<OperationAllowed> opsAllowed = opAllowedRepo.findByMetadataId(id);

                    for (OperationAllowed opAllowed: opsAllowed) {
                        if (opAllowed.getId().getOperationId() != ReservedOperation.notify.getId())
                            continue;
                        Group group = groupRepository.findOne(opAllowed.getId().getGroupId());
                        String name = group.getName();
                        String email = group.getEmail();

                        if (email != null && email.trim().length() != 0) {
                            // TODO i18n
                            String subject = "File " + fname + " has been downloaded";
                            String message = "GeoNetwork notifies you, as contact person of group " + name + " that data file " + fname + " belonging metadata " + id
                                    + " has beed downloaded from address " + context.getIpAddress() + ".";

                            try {
                                MailSender sender = new MailSender(context);
                                sender.send(host, Integer.parseInt(port), sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_USERNAME), sm.getValue(Settings.SYSTEM_FEEDBACK_MAILSERVER_PASSWORD),
                                        sm.getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_SSL), sm.getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_TLS),
                                        sm.getValueAsBool(Settings.SYSTEM_FEEDBACK_MAILSERVER_IGNORE_SSL_CERTIFICATE_ERRORS), from,
                                        fromDescr, email, null, subject, message);
                            } catch (Exception e) {
                                Log.error(Geonet.RESOURCES, e.getMessage(), e);
                            }
                        }
                    }
                }
            }

            IResourceDownloadHandler downloadHook = (IResourceDownloadHandler) context.getApplicationContext().getBean("resourceDownloadHandler");
            return downloadHook.onDownload(context, request, Integer.parseInt(id), fname, file);
        }
    }
}

//=============================================================================

