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

package org.fao.geonet.api.records.editing;

import io.swagger.annotations.*;
import jeeves.server.UserSession;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.model.Direction;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import springfox.documentation.annotations.ApiIgnore;

import static jeeves.guiservices.session.Get.getSessionAsXML;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_OPS;
import static org.fao.geonet.api.ApiParams.API_CLASS_RECORD_TAG;
import static org.fao.geonet.api.ApiParams.API_PARAM_RECORD_UUID;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasOperation;
import static org.springframework.data.jpa.domain.Specifications.where;

@RequestMapping(value = {
    "/api/records",
    "/api/" + API.VERSION_0_1 +
        "/records"
})
@Api(value = API_CLASS_RECORD_TAG,
    tags = API_CLASS_RECORD_TAG,
    description = API_CLASS_RECORD_OPS)
@Controller("recordEditing")
@PreAuthorize("hasRole('Editor')")
@ReadWriteController
public class MetadataEditingApi {

    @Autowired
    LanguageUtils languageUtils;


    @ApiOperation(value = "Edit a record",
        notes = "Return HTML form for editing.",
        nickname = "editor")
    @RequestMapping(value = "/{metadataUuid}/editor",
        method = RequestMethod.GET,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The editor form."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public Element startEditing(
        @ApiParam(value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Tab"
        )
            @RequestParam(
                defaultValue = "simple"
            )
            String currTab,
        @RequestParam(
            defaultValue = "false"
        )
            boolean withAttributes,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession session,
        @ApiIgnore
        @ApiParam(hidden = true)
        @RequestParam
            Map<String,String> allRequestParams,
        HttpServletRequest request
        ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        boolean showValidationErrors = false;
        boolean starteditingsession = true;

        ServiceContext context = ApiUtils.createServiceContext(request);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        if (starteditingsession) {
            DataManager dm = applicationContext.getBean(DataManager.class);
            dm.startEditingSession(context, String.valueOf(metadata.getId()));
        }

        Element elMd = new AjaxEditUtils(context)
            .getMetadataEmbedded(
                context,
                String.valueOf(metadata.getId()),
                true, showValidationErrors);
        return buildEditorForm(
            currTab, session, allRequestParams,
            request, metadata.getId(), elMd, metadata.getDataInfo().getSchemaId(),
            showValidationErrors,
            context, applicationContext, false, false);
    }


    @ApiOperation(value = "Save edits",
        notes = "Save the HTML form content.",
        nickname = "saveEdits")
    @RequestMapping(value = "/{metadataUuid}/editor",
        method = RequestMethod.POST,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The editor form."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public Element saveEdits(
        @ApiParam(value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Tab"
        )
            @RequestParam(
                defaultValue = "simple"
            )
            String tab,
        @RequestParam(
            defaultValue = "false"
        )
            boolean withAttributes,
        @RequestParam(
            defaultValue = "false"
        )
            boolean withValidationErrors,
        @RequestParam(
            defaultValue = "false"
        )
            boolean minor,
        @ApiParam(
            value = "Save current edits."
        )
        @RequestParam(
            defaultValue = "false"
        )
            boolean commit,
        @ApiParam(
            value = "Save and terminate session."
        )
        @RequestParam(
            defaultValue = "false"
        )
            boolean terminate,
        @ApiParam(
            value = "Record as XML. TODO: rename xml"
        )
        @RequestParam(
            defaultValue = ""
        )
            String data,
        @ApiIgnore
        @ApiParam(hidden = true)
        @RequestParam
            Map<String,String> allRequestParams,
        HttpServletRequest request,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession httpSession
        ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);
        AjaxEditUtils ajaxEditUtils = new AjaxEditUtils(context);
//        ajaxEditUtils.preprocessUpdate(allRequestParams, context);

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dataMan = applicationContext.getBean(DataManager.class);
        UserSession session = ApiUtils.getUserSession(httpSession);

        String id = String.valueOf(metadata.getId());
        String isTemplate = allRequestParams.get(Params.TEMPLATE);

//        boolean finished = config.getValue(Params.FINISHED, "no").equals("yes");
//        boolean forget = config.getValue(Params.FORGET, "no").equals("yes");
//        boolean commit = config.getValue(Params.START_EDITING_SESSION, "no").equals("yes");

        // TODO: Use map only to avoid this conversion
        Element params = new Element ("request");
        Map<String, String> forwardedParams = new HashMap<>();
        for (Map.Entry<String, String> e : allRequestParams.entrySet()) {
            params.addContent(new Element(e.getKey()).setText(e.getValue()));
            if (!e.getKey().startsWith("_")) {
                forwardedParams.put(e.getKey(), e.getValue());
            }
        }

        int iLocalId = Integer.parseInt(id);
        dataMan.setTemplateExt(iLocalId, MetadataType.lookup(isTemplate));

        //--- use StatusActionsFactory and StatusActions class to possibly
        //--- change status as a result of this edit (use onEdit method)
        StatusActionsFactory saf = context.getBean(StatusActionsFactory.class);
        StatusActions sa = saf.createStatusActions(context);
        sa.onEdit(iLocalId, minor);

        if (StringUtils.isNotEmpty(data)) {
            Element md = Xml.loadString(data, false);
            String changeDate = null;
            boolean updateDateStamp = !minor;
            boolean ufo = true;
            boolean index = true;
            dataMan.updateMetadata(context, id, md,
                withValidationErrors, ufo, index,
                context.getLanguage(), changeDate, updateDateStamp);
        } else {
            ajaxEditUtils.updateContent(params, false, true);
        }

        //-----------------------------------------------------------------------
        //--- update element and return status
//        Element elResp = new Element(Jeeves.Elem.RESPONSE);
//        elResp.addContent(new Element(Geonet.Elem.ID).setText(id));
//        elResp.addContent(new Element(Geonet.Elem.SHOWVALIDATIONERRORS)
//            .setText(String.valueOf(withValidationErrors)));
////        boolean justCreated = Util.getParam(params, Params.JUST_CREATED, null) != null;
////        if (justCreated) {
////            elResp.addContent(new Element(Geonet.Elem.JUSTCREATED).setText("true"));
////        }
//        elResp.addContent(new Element(Params.MINOREDIT).setText(String.valueOf(minor)));

        //--- if finished then remove the XML from the session
        if ((commit) && (!terminate)) {
            return null;
        }
        if (terminate) {
            SettingManager sm = context.getBean(SettingManager.class);

            boolean forceValidationOnMdSave = sm.getValueAsBool("metadata/workflow/forceValidationOnMdSave");

            boolean reindex = false;

            // Save validation if the forceValidationOnMdSave is enabled
            if (forceValidationOnMdSave) {
                dataMan.doValidate(metadata.getDataInfo().getSchemaId(), metadata.getId() + "",
                    new Document(metadata.getXmlData(false)), context.getLanguage());
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
                    final MetadataValidationRepository metadataValidationRepository = context.getBean(MetadataValidationRepository.class);

                    boolean isInvalid =
                        (metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(Integer.parseInt(id))) > 0);

                    if (isInvalid) {
                        operationAllowedRepo.deleteAll(where(hasMetadataId(id)).and(hasGroupId(ReservedGroup.all.getId())));
                    }

                    reindex = true;
                }

            }

            if (reindex) {
                dataMan.indexMetadata(id, true);
            }

            ajaxEditUtils.removeMetadataEmbedded(session, id);
            dataMan.endEditingSession(id, session);
            return null;
        }

//        if (!finished && !forget && commit) {
//            dataMan.startEditingSession(context, id);
//        }
        Element elMd = new AjaxEditUtils(context)
            .getMetadataEmbedded(
                context,
                String.valueOf(id),
                true, withValidationErrors);
        return buildEditorForm(
            tab, httpSession, forwardedParams,
            request, metadata.getId(), elMd, metadata.getDataInfo().getSchemaId(),
            withValidationErrors,
            context, applicationContext, false, false);
    }


    @ApiOperation(value = "Cancel edits",
        notes = "Cancel current editing session.",
        nickname = "cancelEdits")
    @RequestMapping(value = "/{metadataUuid}/editor",
        method = RequestMethod.DELETE,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Editing session cancelled."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public void cancelEdits(
        @ApiParam(value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiIgnore
        @ApiParam(hidden = true)
        @RequestParam
            Map<String,String> allRequestParams,
        HttpServletRequest request,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dataMan = applicationContext.getBean(DataManager.class);
        ServiceContext context = ApiUtils.createServiceContext(request);
        dataMan.cancelEditingSession(context, String.valueOf(metadata.getId()));
    }




    @ApiOperation(value = "Add element",
        notes = "",
        nickname = "addElement")
    @RequestMapping(value = "/{metadataUuid}/editor/elements",
        method = RequestMethod.PUT,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Element added."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public Element addElement(
        @ApiParam(value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Reference of the insertion point.",
            required = true)
        @RequestParam
            String ref,
        @ApiParam(
            value = "Name of the element to add (with prefix)",
            required = true)
        @RequestParam
            String name,
        @ApiParam(
            value = "Use geonet:attribute for attributes or child name.",
            required = false)
        @RequestParam(
            required = false
        )
            String child,
        @ApiIgnore
        @ApiParam(hidden = true)
        @RequestParam
            Map<String,String> allRequestParams,
        HttpServletRequest request,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        // -- build the element to be added
        // -- Here we do mark the element that is added
        // -- then we traverse up the tree to the root
        // -- clone from the root and return the clone
        // -- this is done so that the style sheets have
        // -- access to important information like the
        // -- document language and other locales
        // -- this is important for multilingual editing
        // --
        // -- Note that the metadata-embedded.xsl stylesheet
        // -- only applies the templating to the added element, not to
        // -- the entire metadata so performance should not be a big issue
        Element elResp = new AjaxEditUtils(context).addElementEmbedded(
            ApiUtils.getUserSession(httpSession),
            String.valueOf(metadata.getId()),
            ref, name, child);
        EditLib.tagForDisplay(elResp);
        Element md = (Element) findRoot(elResp).clone();
        EditLib.removeDisplayTag(elResp);

        return buildEditorForm(
            allRequestParams.get("currTab"), httpSession, allRequestParams,
            request, metadata.getId(), md, metadata.getDataInfo().getSchemaId(),
            false,
            context, applicationContext, true, true);
    }





    @ApiOperation(value = "Reorder element",
        notes = "",
        nickname = "reorderElement")
    @RequestMapping(value = "/{metadataUuid}/editor/elements/{direction}",
        method = RequestMethod.PUT,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Element reordered."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public void addElement(
        @ApiParam(
            value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Reference of the element to move.",
            required = true)
        @RequestParam
            String ref,
        @ApiParam(
            value = "Direction",
            required = true)
        @PathVariable
            Direction direction,
        @ApiIgnore
        @ApiParam(hidden = true)
        @RequestParam
            Map<String,String> allRequestParams,
        HttpServletRequest request,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        new AjaxEditUtils(context).swapElementEmbedded(
            ApiUtils.getUserSession(httpSession),
            String.valueOf(metadata.getId()),
            ref,
            direction == Direction.down);
    }





    @ApiOperation(value = "Delete element",
        notes = "",
        nickname = "deleteElement")
    @RequestMapping(value = "/{metadataUuid}/editor/elements",
        method = RequestMethod.DELETE,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Element removed."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public void deleteElement(
        @ApiParam(value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Reference of the element to remove.",
            required = true)
        @RequestParam
            String[] ref,
        @ApiParam(
            value = "Name of the parent.",
            required = true)
        @RequestParam
            String parent,
        HttpServletRequest request,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        String id = String.valueOf(metadata.getId());

        for (int i = 0; i < ref.length; i++) {
            new AjaxEditUtils(context).deleteElementEmbedded(
                ApiUtils.getUserSession(httpSession),
                id, ref[i], parent);
        }
    }

    @ApiOperation(value = "Delete attribute",
        notes = "",
        nickname = "deleteAttribute")
    @RequestMapping(value = "/{metadataUuid}/editor/attributes",
        method = RequestMethod.DELETE,
        consumes = {
            MediaType.ALL_VALUE
        },
        produces = {
            MediaType.APPLICATION_XML_VALUE
        })
    @PreAuthorize("hasRole('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Attribute removed."),
        @ApiResponse(code = 403, message = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public void deleteAttribute(
        @ApiParam(value = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @ApiParam(
            value = "Reference of the attribute to remove.",
            required = true)
        @RequestParam
            String ref,
        HttpServletRequest request,
        @ApiIgnore
        @ApiParam(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        Metadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        new AjaxEditUtils(context).deleteAttributeEmbedded(
            ApiUtils.getUserSession(httpSession),
            String.valueOf(metadata.getId()),
            ref);
    }

    private Element findRoot(Element element) {
        if (element.isRootElement() || element.getParentElement() == null) return element;
        return findRoot(element.getParentElement());
    }


    /**
     * This is a lightweight representation of the
     * legacy Jeeves XML processed by XSLT. Only
     * element required for the editor are created.
     */
    private Element buildEditorForm(
        String tab,
        HttpSession session,
        Map<String, String> allRequestParams,
        HttpServletRequest request,
        int id,
        Element xml,
        String schema,
        boolean showValidationErrors,
        ServiceContext context,
        ApplicationContext applicationContext, boolean isEmbedded, boolean embedded) throws Exception {


        UserSession userSession = ApiUtils.getUserSession(session);
        Element root = buildResourceDocument(applicationContext, context, userSession);
        root.addContent(xml);
        Element gui = root.getChild("gui");
        gui.addContent(
            new Element("currTab").setText(tab));
        // This flag is used to generate top tool bar or not
        gui.addContent(
            new Element("reqService").setText(embedded ? "embedded" : "md.edit"));
        String iso3langCode = languageUtils.getIso3langCode(request.getLocales());
        gui.addContent(
            new Element("language").setText(iso3langCode));
        gui.addContent(
            getSchemaStrings(schema, context)
        );

        Element requestParams = new Element ("request");
        for (Map.Entry<String, String> e : allRequestParams.entrySet()) {
            requestParams.addContent(new Element(e.getKey()).setText(e.getValue()));
        }
        root.addContent(requestParams);

        GeonetworkDataDirectory dataDirectory = applicationContext.getBean(GeonetworkDataDirectory.class);
        Path xslt = dataDirectory.getWebappDir().resolve(
            isEmbedded ?
                "xslt/ui-metadata/edit/edit-embedded.xsl" :
                "xslt/ui-metadata/edit/edit.xsl");
        return Xml.transform(root, xslt);
    }



    private Element buildResourceDocument(
        ApplicationContext applicationContext,
        ServiceContext context,
        UserSession userSession) throws JDOMException, SQLException, IOException {
//        <gui>
//        Unused now
//        <xml name="strings" file="xml/strings.xml"/>
//        Unused now
//        <call name="isolanguages" class="org.fao.geonet.guiservices.isolanguages.Get"/>
//        <call name="session" class="jeeves.guiservices.session.Get"/>
//        <call name="env" class="org.fao.geonet.guiservices.util.Env"/>
//        <call name="systemConfig" class="org.fao.geonet.services.config.Get"/>
//        <call name="results" class="org.fao.geonet.guiservices.search.GetResultsInfo"/>
//        <call name="schemalist" class="org.fao.geonet.guiservices.schemas.Get"/>
//        <call name="svnmanager" class="org.fao.geonet.guiservices.util.GetSvnDetails"/>
//
//        <!-- this service adds labels and codelists from all schemas -->
//        <call name="schemas" class="org.fao.geonet.guiservices.schemas.GetSchemaInfo"/>
//        </gui>

        Element root = new Element("root");
        Element gui = new Element("gui");
        gui.addContent(applicationContext.getBean(SettingManager.class).getAllAsXML(true));
        gui.addContent(getSessionAsXML(userSession));

        ThesaurusManager th = applicationContext.getBean(ThesaurusManager.class);
        gui.addContent(new Element("thesaurus").addContent(th.buildResultfromThTable(context)));
        // TODO: Add request parameters
//        <output sheet="../xslt/ui-metadata/edit/edit.xsl">
//        <call name="thesaurus" class=".services.thesaurus.GetList"/>
//        <call name="currTab" class=".guiservices.util.GetCurrentMDTab"/>
//        <xml name="i18n" file="xml/i18n.xml"/>
        root.addContent(gui);
        return root;
    }

    @Autowired
    SchemaManager schemaManager;

    public Element getSchemaStrings(String schemaToLoad, ServiceContext context) throws Exception {
        Element schemas = new Element("schemas");

        for (String schema : schemaManager.getSchemas()) {
            // Load schema and schema dependency localization files
            if (schema.equals(schemaToLoad) ||
                schemaToLoad.startsWith(schema) ||
                schemaManager.getDependencies(schemaToLoad).contains(schema)
                ) {
                try {
                    Map<String, XmlFile> schemaInfo = schemaManager.getSchemaInfo(schema);

                    for (Map.Entry<String, XmlFile> entry : schemaInfo.entrySet()) {
                        XmlFile xf = entry.getValue();
                        String fname = entry.getKey();
                        Element response = xf.exec(new Element("junk"), context);
                        response.setName(FilenameUtils.removeExtension(fname));
                        response.removeAttribute("noNamespaceSchemaLocation", Geonet.Namespaces.XSI);
                        Element schemaElem = new Element(schema);
                        schemaElem.addContent(response);
                        schemas.addContent(schemaElem);
                    }
                } catch (Exception e) {
                    context.error("Failed to load localization file for schema " + schema + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return schemas;
    }
}
