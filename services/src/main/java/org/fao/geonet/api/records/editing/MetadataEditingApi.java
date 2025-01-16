/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;
import jeeves.services.ReadWriteController;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.api.records.MetadataUtils;
import org.fao.geonet.api.records.model.Direction;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.events.history.RecordUpdatedEvent;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataStatus;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.metadata.StatusActionsFactory;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmitter;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;

import static jeeves.guiservices.session.Get.getSessionAsXML;
import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.kernel.setting.Settings.METADATA_WORKFLOW_AUTOMATIC_UNPUBLISH_INVALID_MD;
import static org.fao.geonet.kernel.setting.Settings.METADATA_WORKFLOW_FORCE_VALIDATION_ON_MD_SAVE;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.*;
import static org.springframework.data.jpa.domain.Specification.where;

@RequestMapping(value = {"/{portal}/api/records"})
@Tag(name = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("recordEditing")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class MetadataEditingApi {

    @Autowired
    LanguageUtils languageUtils;

    @Autowired
    SchemaManager schemaManager;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    IMetadataIndexer metadataIndexer;

    @Autowired
    MetadataDraftRepository metadataDraftRepository;

    @Autowired
    private StatusValueRepository statusValueRepository;

    @io.swagger.v3.oas.annotations.Operation(summary = "Edit a record", description = "Return HTML form for editing.")
    @RequestMapping(value = "/{metadataUuid}/editor", method = RequestMethod.GET, consumes = {
        MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The editor form."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    public void startEditing(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = "Tab") @RequestParam(defaultValue = "") String currTab,
        @RequestParam(defaultValue = "false") boolean withAttributes,
        @Parameter(hidden = true) HttpSession session,
        @Parameter(hidden = true) @RequestParam Map<String, String> allRequestParams,
        HttpServletRequest request, HttpServletResponse response) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        boolean showValidationErrors = false;

        ServiceContext context = ApiUtils.createServiceContext(request);
        ApplicationContext applicationContext = ApplicationContextHolder.get();

        SettingManager sm = context.getBean(SettingManager.class);

        // Code to handle the flag METADATA_EDITING_CREATED_DRAFT:
        //   1) Editing an approved metadata, without a working copy creates the working copy and should set
        //      METADATA_EDITING_CREATED_DRAFT = true, to remove the working copy if the user cancel the editor form.
        //
        //   2) Editing an approved metadata, with a working copy should NOT set
        //      METADATA_EDITING_CREATED_DRAFT = true, in this case should NOT be removed the working copy
        //      if the user cancel the editor form.
        boolean isEnabledWorkflow = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);
        boolean flagCreateDraftFromApprovedMetadata = false;
        if (isEnabledWorkflow) {
            flagCreateDraftFromApprovedMetadata = (metadataDraftRepository.findOneByUuid(metadata.getUuid()) == null);
        }

        // Start editing session
        IMetadataUtils dm = applicationContext.getBean(IMetadataUtils.class);
        Integer id2 = dm.startEditingSession(context, String.valueOf(metadata.getId()));

        // Maybe we are redirected to another metadata?
        if (id2 != metadata.getId()) {

            StringBuilder sb = new StringBuilder("?");

            Enumeration<String> parameters = request.getParameterNames();

            // As this editor will redirect, make sure there is something to go
            // back that makes sense and prevent a loop:
            boolean hasPreviousURL = false;

            while (parameters.hasMoreElements()) {
                String key = parameters.nextElement();
                sb.append(key + "=" + request.getParameter(key) + "%26");
                if (key.equalsIgnoreCase("redirectUrl")) {
                    hasPreviousURL = true;
                }
            }

            if (!hasPreviousURL) {
                sb.append("redirectUrl=catalog.edit");
            }

            context.getUserSession().setProperty(Geonet.Session.METADATA_EDITING_CREATED_DRAFT, flagCreateDraftFromApprovedMetadata);

            Element el = new Element("script");
            el.setText("window.location.hash = decodeURIComponent(\"#/metadata/" + id2 + sb.toString() + "\")");
            String elStr = Xml.getString(el);
            response.getWriter().print(elStr);
            return;
        }
        // End of start editing session

        Element elMd = new AjaxEditUtils(context).getMetadataEmbedded(context, String.valueOf(metadata.getId()), true,
            showValidationErrors);
        buildEditorForm(currTab, session, allRequestParams, request, elMd, metadata.getDataInfo().getSchemaId(),
            context, applicationContext, false, false, response);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Save edits", description = "Save the HTML form content.")
    @RequestMapping(value = "/{metadataUuid}/editor", method = RequestMethod.POST, consumes = {
        MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The editor form."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)
    })
    @ResponseBody
    public void saveEdits(
        @Parameter(description = API_PARAM_RECORD_UUID,
            required = true)
        @PathVariable
            String metadataUuid,
        @Parameter(
            description = "Tab"
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
        @Parameter(
            description = "Submit for review directly after save.")
        @RequestParam(defaultValue = StatusValue.Status.DRAFT)
            String status,
        @Parameter(
            description = "Save current edits."
        )
        @RequestParam(
            defaultValue = "false"
        )
            boolean commit,
        @Parameter(
            description = "Save and terminate session."
        )
        @RequestParam(
            defaultValue = "false"
        )
            boolean terminate,
        @Parameter(
            description = "Record as XML. TODO: rename xml"
        )
        @RequestParam(
            defaultValue = ""
        )
            String data,
        @Parameter(hidden = true)
        @RequestParam
            Map<String, String> allRequestParams,
        HttpServletRequest request,
        HttpServletResponse response,
        @Parameter(hidden = true)
            HttpSession httpSession
    ) throws Exception {

        Log.trace(Geonet.DATA_MANAGER, "Saving metadata editing with UUID " + metadataUuid);
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);
        AjaxEditUtils ajaxEditUtils = new AjaxEditUtils(context);
        // ajaxEditUtils.preprocessUpdate(allRequestParams, context);

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dataMan = applicationContext.getBean(DataManager.class);
        UserSession session = ApiUtils.getUserSession(httpSession);
        IMetadataValidator validator = applicationContext.getBean(IMetadataValidator.class);
        BaseMetadataStatus statusRepository = ApplicationContextHolder.get().getBean(BaseMetadataStatus.class);
        String id = String.valueOf(metadata.getId());
        Log.trace(Geonet.DATA_MANAGER, " > ID of the record to edit: " + id);
        String isTemplate = allRequestParams.get(Params.TEMPLATE);
        SettingManager sm = context.getBean(SettingManager.class);
        // boolean finished = config.getValue(Params.FINISHED, "no").equals("yes");
        // boolean forget = config.getValue(Params.FORGET, "no").equals("yes");
        // boolean commit = config.getValue(Params.START_EDITING_SESSION,
        // "no").equals("yes");
        boolean isEditor = session.getProfile().equals(Profile.Editor);
        boolean isReviewer = session.getProfile().equals(Profile.Reviewer);
        boolean isAdmin = session.getProfile().equals(Profile.Administrator);

        // Checks when workflow enabled if the user is allowed
        boolean isEnabledWorkflow = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ENABLE);
        if (isEnabledWorkflow && isEditor && !statusRepository.canEditorEdit(metadata.getId())) {
            throw new NotAllowedException("Editing is allowed only in Draft state for the current profile.");
        }

        // TODO: Use map only to avoid this conversion
        Log.trace(Geonet.DATA_MANAGER, " > Getting parameters from request");
        Element params = new Element("request");
        Map<String, String> forwardedParams = new HashMap<>();
        for (Map.Entry<String, String> e : allRequestParams.entrySet()) {
            params.addContent(new Element(e.getKey()).setText(e.getValue()));
            if (!e.getKey().startsWith("_")) {
                forwardedParams.put(e.getKey(), e.getValue());
            }
        }


        if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
            Log.trace(Geonet.DATA_MANAGER, " > Setting type of record " + MetadataType.lookup(isTemplate));
        }
        int iLocalId = Integer.parseInt(id);
        Log.trace(Geonet.DATA_MANAGER, " > Id is " + iLocalId);
        dataMan.setTemplateExt(iLocalId, MetadataType.lookup(isTemplate));


        // --- use StatusActionsFactory and StatusActions class to possibly
        // --- change status as a result of this edit (use onEdit method)
        Log.trace(Geonet.DATA_MANAGER, " > Trigger status actions based on this edit");
        StatusActionsFactory saf = context.getBean(StatusActionsFactory.class);
        StatusActions sa = saf.createStatusActions(context);
        sa.onEdit(iLocalId, minor);
        Element beforeMetadata = dataMan.getMetadata(context, String.valueOf(metadata.getId()), false, false, false);

        IndexingMode indexingMode = terminate ? IndexingMode.full : IndexingMode.core;

        if (StringUtils.isNotEmpty(data)) {
            Log.trace(Geonet.DATA_MANAGER, " > Updating metadata through data manager");
            Element md = Xml.loadString(data, false);
            String changeDate = null;
            boolean updateDateStamp = !minor;
            boolean ufo = true;


            dataMan.updateMetadata(context, id, md, withValidationErrors, ufo, context.getLanguage(), changeDate,
                updateDateStamp, indexingMode);

            if (terminate) {
                XMLOutputter outp = new XMLOutputter();
                String xmlBefore = outp.outputString(beforeMetadata);
                String xmlAfter = outp.outputString(md);
                new RecordUpdatedEvent(Long.parseLong(id), session.getUserIdAsInt(), xmlBefore, xmlAfter)
                    .publish(applicationContext);
            }
        } else {
            Log.trace(Geonet.DATA_MANAGER, " > Updating contents");
            ajaxEditUtils.updateContent(params, false, true, indexingMode);

            Element afterMetadata = dataMan.getMetadata(context, String.valueOf(metadata.getId()), false, false, false);

            if (terminate) {
                XMLOutputter outp = new XMLOutputter();
                String xmlBefore = outp.outputString(beforeMetadata);
                String xmlAfter = outp.outputString(afterMetadata);
                new RecordUpdatedEvent(Long.parseLong(id), session.getUserIdAsInt(), xmlBefore, xmlAfter)
                    .publish(applicationContext);
            }
        }

        // -----------------------------------------------------------------------
        // --- update element and return status
        // Element elResp = new Element(Jeeves.Elem.RESPONSE);
        // elResp.addContent(new Element(Geonet.Elem.ID).setText(id));
        // elResp.addContent(new Element(Geonet.Elem.SHOWVALIDATIONERRORS)
        // .setText(String.valueOf(withValidationErrors)));
        //// boolean justCreated = Util.getParam(params, Params.JUST_CREATED, null) !=
        // null;
        //// if (justCreated) {
        //// elResp.addContent(new Element(Geonet.Elem.JUSTCREATED).setText("true"));
        //// }
        // elResp.addContent(new
        // Element(Params.MINOREDIT).setText(String.valueOf(minor)));

        // --- if finished then remove the XML from the session
        if ((commit) && (!terminate)) {
            return;
        }
        if (terminate) {
            Log.trace(Geonet.DATA_MANAGER, " > Closing editor");

            boolean forceValidationOnMdSave = sm.getValueAsBool(METADATA_WORKFLOW_FORCE_VALIDATION_ON_MD_SAVE);

            boolean reindex = false;

            String lang = String.valueOf(languageUtils.parseAcceptLanguage(request.getLocales()));
            ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages",
                new Locale(lang));

            // Save validation if the forceValidationOnMdSave is enabled
            if (forceValidationOnMdSave) {
                validator.doValidate(metadata, context.getLanguage());
                reindex = true;
            }

            // Automatically change the workflow state after save
            if (isEnabledWorkflow) {
                boolean isAllowedSubmitApproveInvalidMd = sm.getValueAsBool(Settings.METADATA_WORKFLOW_ALLOW_SUBMIT_APPROVE_INVALID_MD);
                if (((status.equals(StatusValue.Status.SUBMITTED))
                    || (status.equals(StatusValue.Status.APPROVED)))
                    && !isAllowedSubmitApproveInvalidMd) {

                    if (!forceValidationOnMdSave) {
                        validator.doValidate(metadata, context.getLanguage());
                    }
                    boolean isInvalid = MetadataUtils.retrieveMetadataValidationStatus(metadata, context);

                    if (isInvalid) {
                        throw new NotAllowedException("Metadata is invalid: can't be submitted or approved")
                            .withMessageKey("exception.resourceInvalid.metadata")
                            .withDescriptionKey("exception.resourceInvalid.metadata.description");
                    }
                }


                if (status.equals(StatusValue.Status.SUBMITTED)) {
                    // Only editors can submit a record
                    if (isEditor || isAdmin) {
                        Integer changeToStatus = Integer.parseInt(StatusValue.Status.SUBMITTED);
                        StatusValue statusValue = statusValueRepository.findById(changeToStatus).get();

                        MetadataStatus metadataStatus = new MetadataStatus();

                        metadataStatus.setMetadataId(metadata.getId());
                        metadataStatus.setUuid(metadata.getUuid());
                        metadataStatus.setChangeDate(new ISODate());
                        metadataStatus.setUserId(session.getUserIdAsInt());
                        metadataStatus.setStatusValue(statusValue);
                        metadataStatus.setChangeMessage(messages.getString("metadata_save_submit_text"));

                        List<MetadataStatus> listOfStatusChange = new ArrayList<>(1);
                        listOfStatusChange.add(metadataStatus);
                        sa.onStatusChange(listOfStatusChange, true);
                    } else {
                        throw new SecurityException(String.format("Only users with editor profile can submit."));
                    }
                }
                if (status.equals(StatusValue.Status.APPROVED)) {
                    // Only reviewers can approve
                    if (isReviewer || isAdmin) {
                        Integer changeToStatus = Integer.parseInt(StatusValue.Status.APPROVED);
                        StatusValue statusValue = statusValueRepository.findById(changeToStatus).get();

                        MetadataStatus metadataStatus = new MetadataStatus();

                        metadataStatus.setMetadataId(metadata.getId());
                        metadataStatus.setUuid(metadata.getUuid());
                        metadataStatus.setChangeDate(new ISODate());
                        metadataStatus.setUserId(session.getUserIdAsInt());
                        metadataStatus.setStatusValue(statusValue);
                        metadataStatus.setChangeMessage(messages.getString("metadata_save_approve_text"));

                        List<MetadataStatus> listOfStatusChange = new ArrayList<>(1);
                        listOfStatusChange.add(metadataStatus);
                        sa.onStatusChange(listOfStatusChange, true);
                    } else {
                        throw new SecurityException(String.format("Only users with review profile can approve."));
                    }
                }
                reindex = true;
            }

            boolean automaticUnpublishInvalidMd = sm.getValueAsBool(METADATA_WORKFLOW_AUTOMATIC_UNPUBLISH_INVALID_MD);
            boolean isUnpublished = false;

            // Unpublish the metadata automatically if the setting
            // automaticUnpublishInvalidMd is enabled and
            // the metadata becomes invalid
            if (automaticUnpublishInvalidMd) {
                final OperationAllowedRepository operationAllowedRepo = context
                    .getBean(OperationAllowedRepository.class);

                boolean isPublic = (operationAllowedRepo.count(where(hasMetadataId(id))
                    .and(hasOperation(ReservedOperation.view)).and(hasGroupId(ReservedGroup.all.getId()))) > 0);

                if (isPublic) {
                    final MetadataValidationRepository metadataValidationRepository = context
                        .getBean(MetadataValidationRepository.class);

                    boolean isInvalid = (metadataValidationRepository
                        .count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(Integer.parseInt(id))) > 0);

                    if (isInvalid) {
                        isUnpublished = true;
                        operationAllowedRepo
                            .deleteAll(where(hasMetadataId(id)).and(hasGroupId(ReservedGroup.all.getId())));
                    }

                    reindex = true;
                }

            }

            if (reindex) {
                Log.trace(Geonet.DATA_MANAGER, " > Reindexing record");
                metadataIndexer.indexMetadata(id, DirectIndexSubmitter.INSTANCE, IndexingMode.full);
            }

            // Reindex the metadata table record to update the field _statusWorkflow that contains the composite
            // status of the published and draft versions
            if (metadata instanceof MetadataDraft) {
                Metadata metadataApproved = metadataRepository.findOneByUuid(metadata.getUuid());

                if (metadataApproved != null) {
                    metadataIndexer.indexMetadata(String.valueOf(metadataApproved.getId()), DirectIndexSubmitter.INSTANCE, IndexingMode.full);
                }
            }

            ajaxEditUtils.removeMetadataEmbedded(session, id);
            dataMan.endEditingSession(id, session);

            if (isEnabledWorkflow) {
                // After saving & close remove the information to remove the draft copy if the user cancels the editor
                context.getUserSession().removeProperty(Geonet.Session.METADATA_EDITING_CREATED_DRAFT);
            }

            if (isUnpublished) {
                throw new IllegalStateException(String.format("Record saved but as it was invalid at the end of "
                    + "the editing session. The public record '%s' was unpublished.", metadata.getUuid()));
            } else {
                return;
            }
        }

        // if (!finished && !forget && commit) {
        // dataMan.startEditingSession(context, id);
        // }
        Element elMd = new AjaxEditUtils(context).getMetadataEmbedded(context, id, true,
            withValidationErrors);

        buildEditorForm(tab, httpSession, forwardedParams, request, elMd, metadata.getDataInfo().getSchemaId(), context,
            applicationContext, false, false, response);

        if (isEnabledWorkflow) {
            // After saving the form remove the information to remove the draft copy if the user cancels the editor
            context.getUserSession().removeProperty(Geonet.Session.METADATA_EDITING_CREATED_DRAFT);
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Cancel edits", description = "Cancel current editing session.")
    @RequestMapping(value = "/{metadataUuid}/editor", method = RequestMethod.DELETE, consumes = {
        MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Editing session cancelled.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseBody
    public void cancelEdits(@Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
                            @Parameter(hidden = true) @RequestParam Map<String, String> allRequestParams,
                            HttpServletRequest request, @Parameter(hidden = true) HttpSession httpSession) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

        ApplicationContext applicationContext = ApplicationContextHolder.get();
        DataManager dataMan = applicationContext.getBean(DataManager.class);
        ServiceContext context = ApiUtils.createServiceContext(request);
        dataMan.cancelEditingSession(context, String.valueOf(metadata.getId()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Add element", description = "")
    @RequestMapping(value = "/{metadataUuid}/editor/elements", method = RequestMethod.PUT, consumes = {
        MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Element added."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    public void addElement(@Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
                           @Parameter(description = "Reference of the insertion point.", required = true) @RequestParam String ref,
                           @Parameter(description = "Name of the element to add (with prefix)", required = true) @RequestParam String name,
                           @Parameter(description = "Empty for inserting element, `geonet:attribute` for attributes.", required = false) @RequestParam(required = false) String child,
                           @Parameter(description = "Should attributes be shown on the editor snippet?", required = false) @RequestParam(defaultValue = "false") boolean displayAttributes,
                           @Parameter(hidden = true) @RequestParam Map<String, String> allRequestParams,
                           HttpServletRequest request, HttpServletResponse response,
                           @Parameter(hidden = true) HttpSession httpSession) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);

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
        List<Element> elResp = new AjaxEditUtils(context).addElementEmbedded(ApiUtils.getUserSession(httpSession),
            String.valueOf(metadata.getId()), ref, name, child);
        Element md = null;

        EditLib editLib = context.getBean(DataManager.class).getEditLib();

        for(Element el: elResp) {
            if (md == null) {
                EditLib.tagForDisplay(el);
                md = (Element) findRoot(el).clone();
                EditLib.removeDisplayTag(el);
            } else {
                Element el2 = editLib.findElement(md, el.getChild("element", Edit.NAMESPACE).getAttribute("ref").getValue());
                EditLib.tagForDisplay(el2);
                md = (Element) md.clone();
                EditLib.removeDisplayTag(el2);
            }

        }


        buildEditorForm(allRequestParams.get("currTab"), httpSession, allRequestParams, request, md,
            metadata.getDataInfo().getSchemaId(), context, applicationContext, true, true, response);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Reorder element", description = "")
    @RequestMapping(value = "/{metadataUuid}/editor/elements/{direction}", method = RequestMethod.PUT, consumes = {
        MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Element reordered."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseBody
    public void reorderElement(@Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
                           @Parameter(description = "Reference of the element to move.", required = true) @RequestParam String ref,
                           @Parameter(description = "Direction", required = true) @PathVariable Direction direction,
                           @Parameter(description = "Should attributes be shown on the editor snippet?", required = false) @RequestParam(defaultValue = "false") boolean displayAttributes,
                           @Parameter(hidden = true) @RequestParam Map<String, String> allRequestParams,
                           HttpServletRequest request, @Parameter(hidden = true) HttpSession httpSession) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        new AjaxEditUtils(context).swapElementEmbedded(ApiUtils.getUserSession(httpSession),
            String.valueOf(metadata.getId()), ref, direction == Direction.down);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete element", description = "")
    @RequestMapping(value = "/{metadataUuid}/editor/elements", method = RequestMethod.DELETE, consumes = {
        MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Element removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseBody
    public void deleteElement(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = "Reference of the element to remove.", required = true) @RequestParam String[] ref,
        @Parameter(description = "Name of the parent.", required = true) @RequestParam String parent,
        @Parameter(description = "Should attributes be shown on the editor snippet?", required = false) @RequestParam(defaultValue = "false") boolean displayAttributes,
        HttpServletRequest request, @Parameter(hidden = true) HttpSession httpSession) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        String id = String.valueOf(metadata.getId());

        for (int i = 0; i < ref.length; i++) {
            new AjaxEditUtils(context).deleteElementEmbedded(ApiUtils.getUserSession(httpSession), id, ref[i], parent);
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Delete attribute", description = "")
    @RequestMapping(value = "/{metadataUuid}/editor/attributes", method = RequestMethod.DELETE, consumes = {
        MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasAuthority('Editor')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Attribute removed.", content = {@Content(schema = @Schema(hidden = true))}),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    @ResponseBody
    public void deleteAttribute(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = "Reference of the attribute to remove.", required = true) @RequestParam String ref,
        @Parameter(description = "Should attributes be shown on the editor snippet?", required = false) @RequestParam(defaultValue = "false") boolean displayAttributes,
        HttpServletRequest request, @Parameter(hidden = true) HttpSession httpSession) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ServiceContext context = ApiUtils.createServiceContext(request);

        new AjaxEditUtils(context).deleteAttributeEmbedded(ApiUtils.getUserSession(httpSession),
            String.valueOf(metadata.getId()), ref);
    }

    private Element findRoot(Element element) {
        if (element.isRootElement() || element.getParentElement() == null)
            return element;
        return findRoot(element.getParentElement());
    }

    /**
     * This is a lightweight representation of the legacy Jeeves XML processed by
     * XSLT. Only element required for the editor are created.
     */
    private void buildEditorForm(String tab, HttpSession session, Map<String, String> allRequestParams,
                                 HttpServletRequest request, Element xml, String schema, ServiceContext context,
                                 ApplicationContext applicationContext, boolean isEmbedded, boolean embedded, HttpServletResponse response)
        throws Exception {

        UserSession userSession = ApiUtils.getUserSession(session);
        Element root = buildResourceDocument(applicationContext, context, userSession);
        root.addContent(xml);
        Element gui = root.getChild("gui");
        gui.addContent(new Element("currTab").setText(tab));
        // This flag is used to generate top tool bar or not
        gui.addContent(new Element("reqService").setText(embedded ? "embedded" : "md.edit"));
        String iso3langCode = languageUtils.getIso3langCode(request.getLocales());
        gui.addContent(new Element("language").setText(iso3langCode));
        gui.addContent(getSchemaStrings(schema, context));

        Element requestParams = new Element("request");
        for (Map.Entry<String, String> e : allRequestParams.entrySet()) {
            requestParams.addContent(new Element(e.getKey()).setText(e.getValue()));
        }
        root.addContent(requestParams);

        GeonetworkDataDirectory dataDirectory = applicationContext.getBean(GeonetworkDataDirectory.class);
        Path xslt = dataDirectory.getWebappDir()
            .resolve(isEmbedded ? "xslt/ui-metadata/edit/edit-embedded.xsl" : "xslt/ui-metadata/edit/edit.xsl");

        Xml.transform(root, xslt, response.getOutputStream());
    }

    private Element buildResourceDocument(ApplicationContext applicationContext, ServiceContext context,
                                          UserSession userSession) throws JDOMException, SQLException, IOException {
        // <gui>
        // Unused now
        // <xml name="strings" file="xml/strings.xml"/>
        // Unused now
        // <call name="isolanguages"
        // class="org.fao.geonet.guiservices.isolanguages.Get"/>
        // <call name="session" class="jeeves.guiservices.session.Get"/>
        // <call name="env" class="org.fao.geonet.guiservices.util.Env"/>
        // <call name="systemConfig" class="org.fao.geonet.services.config.Get"/>
        // <call name="results"
        // class="org.fao.geonet.guiservices.search.GetResultsInfo"/>
        // <call name="schemalist" class="org.fao.geonet.guiservices.schemas.Get"/>
        // <call name="svnmanager"
        // class="org.fao.geonet.guiservices.util.GetSvnDetails"/>
        //
        // <!-- this service adds labels and codelists from all schemas -->
        // <call name="schemas"
        // class="org.fao.geonet.guiservices.schemas.GetSchemaInfo"/>
        // </gui>

        Element root = new Element("root");
        Element gui = new Element("gui");
        gui.addContent(applicationContext.getBean(SettingManager.class).getAllAsXML(true));
        gui.addContent(getSessionAsXML(userSession));

        ThesaurusManager th = applicationContext.getBean(ThesaurusManager.class);
        gui.addContent(new Element("thesaurus").addContent(th.buildResultfromThTable(context)));
        // TODO: Add request parameters
        // <output sheet="../xslt/ui-metadata/edit/edit.xsl">
        // <call name="thesaurus" class=".services.thesaurus.GetList"/>
        // <call name="currTab" class=".guiservices.util.GetCurrentMDTab"/>
        // <xml name="i18n" file="xml/i18n.xml"/>
        root.addContent(gui);
        return root;
    }

    public Element getSchemaStrings(String schemaToLoad, ServiceContext context) throws Exception {
        Element schemas = new Element("schemas");

        for (String schema : schemaManager.getSchemas()) {
            // Load schema and schema dependency localization files
            if (schema.equals(schemaToLoad) || schemaToLoad.startsWith(schema)
                || schemaManager.getDependencies(schemaToLoad).contains(schema)) {
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
                    context.error(e);
                }
            }
        }
        return schemas;
    }
}
