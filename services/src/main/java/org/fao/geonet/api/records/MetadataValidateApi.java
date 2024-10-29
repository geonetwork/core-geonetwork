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

package org.fao.geonet.api.records;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.context.ServiceContext;
import jeeves.services.ReadWriteController;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiParams;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.editing.AjaxEditUtils;
import org.fao.geonet.api.records.model.validation.Report;
import org.fao.geonet.api.records.model.validation.Reports;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.events.history.RecordValidationTriggeredEvent;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.fao.geonet.api.ApiParams.*;
import static org.fao.geonet.api.records.formatters.XsltFormatter.getSchemaLocalization;
import static org.fao.geonet.utils.Xml.getSAXBuilder;

@RequestMapping(value = {"/{portal}/api/records"})
@Tag(name = API_CLASS_RECORD_TAG, description = API_CLASS_RECORD_OPS)
@Controller("recordValidate")
@PreAuthorize("hasAuthority('Editor')")
@ReadWriteController
public class MetadataValidateApi {

    public static final String EL_ACTIVE_PATTERN = "active-pattern";
    public static final String EL_FIRED_RULE = "fired-rule";
    public static final String EL_FAILED_ASSERT = "failed-assert";
    public static final String EL_SUCCESS_REPORT = "successful-report";
    public static final String ATT_CONTEXT = "context";
    public static final String DEFAULT_CONTEXT = "??";
    @Autowired
    LanguageUtils languageUtils;
    @Autowired
    MetadataValidationRepository metadataValidationRepository;

    /**
     * Schematron report has an odd structure:
     *
     * <pre>
     * <code>
     * &lt;svrl:active-pattern  ... />
     * &lt;svrl:fired-rule  ... />
     * &lt;svrl:failed-assert ... />
     * &lt;svrl:successful-report ... />
     * </code>
     * </pre>
     * <p/>
     * This method restructures the xml to be:
     *
     * <pre>
     * <code>
     * &lt;svrl:active-pattern  ... >
     *     &lt;svrl:fired-rule  ... >
     *         &lt;svrl:failed-assert ... />
     *         &lt;svrl:successful-report ... />
     *     &lt;svrl:fired-rule  ... >
     * &lt;svrl:active-pattern>
     * </code>
     * </pre>
     */
    public static void restructureReportToHavePatternRuleHierarchy(Element errorReport) {
        final Iterator patternFilter = errorReport
            .getDescendants(new ElementFilter(EL_ACTIVE_PATTERN, Geonet.Namespaces.SVRL));
        @SuppressWarnings("unchecked")
        List<Element> patterns = Lists.newArrayList(patternFilter);
        for (Element pattern : patterns) {
            final Element parentElement = pattern.getParentElement();
            Element currentRule = null;
            @SuppressWarnings("unchecked") final List<Element> children = parentElement.getChildren();

            int index = children.indexOf(pattern) + 1;
            while (index < children.size() && !children.get(index).getName().equals(EL_ACTIVE_PATTERN)) {
                Element next = children.get(index);
                if (EL_FIRED_RULE.equals(next.getName())) {
                    currentRule = next;
                    next.detach();
                    pattern.addContent(next);
                } else {
                    if (currentRule == null) {
                        // odd but could happen I suppose
                        currentRule = new Element(EL_FIRED_RULE, Geonet.Namespaces.SVRL).setAttribute(ATT_CONTEXT,
                            DEFAULT_CONTEXT);
                        pattern.addContent(currentRule);
                    }

                    next.detach();
                    currentRule.addContent(next);

                }
            }
            if (pattern.getChildren().isEmpty()) {
                pattern.detach();
            }
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Validate a record", description = "User MUST be able to edit the record to validate it. "
        + "FIXME : id MUST be the id of the current metadata record in session ?")
    @RequestMapping(value = "/{metadataUuid}/validate/internal", method = RequestMethod.PUT, produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('Editor')")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Validation report."),
        @ApiResponse(responseCode = "403", description = ApiParams.API_RESPONSE_NOT_ALLOWED_CAN_EDIT)})
    public @ResponseBody
    Reports validateRecord(
        @Parameter(description = API_PARAM_RECORD_UUID, required = true) @PathVariable String metadataUuid,
        @Parameter(description = "Validation status. Should be provided only in case of SUBTEMPLATE validation. If provided for another type, throw a BadParameter Exception", required = false) @RequestParam(required = false) Boolean isvalid,
        HttpServletRequest request,  @Parameter(hidden = true) HttpSession session) throws Exception {
        AbstractMetadata metadata = ApiUtils.canEditRecord(metadataUuid, request);
        ApplicationContext appContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        DataManager dataManager = appContext.getBean(DataManager.class);

        String id = String.valueOf(metadata.getId());
        String schemaName = dataManager.getMetadataSchema(id);

        Locale locale = languageUtils.parseAcceptLanguage(request.getLocales());

        boolean isSubtemplate = metadata.getDataInfo().getType() == MetadataType.SUB_TEMPLATE;
        boolean validSet = (isvalid != null);
        if (!isSubtemplate && validSet) {
            throw new BadParameterEx(
                "Parameter isvalid can't be set if it is not a Subtemplate. You cannot force validation of a metadata or a template.");
        }
        if (isSubtemplate && !validSet) {
            throw new BadParameterEx("Parameter isvalid MUST be set for subtemplate.");
        }
        if (isSubtemplate) {
            MetadataValidation metadataValidation = new MetadataValidation()
                .setId(new MetadataValidationId(metadata.getId(), "subtemplate"))
                .setStatus(isvalid ? MetadataValidationStatus.VALID : MetadataValidationStatus.INVALID)
                .setRequired(true).setNumTests(0).setNumFailures(0);
            this.metadataValidationRepository.save(metadataValidation);
            dataManager.indexMetadata(("" + metadata.getId()), DirectIndexSubmittor.INSTANCE);
            new RecordValidationTriggeredEvent(metadata.getId(),
                ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(),
                metadataValidation.getStatus().getCode()).publish(appContext);
            return new Reports();
        }

        // --- validate metadata from session
        Element errorReport;
        try {
            errorReport = new AjaxEditUtils(context).validateMetadataEmbedded(ApiUtils.getUserSession(session), id,
                locale.getISO3Language());
        } catch (NullPointerException e) {
            // TODO: Improve NPE catching exception
            throw new BadParameterEx(String.format("To validate a record, the record MUST be in edition."),
                metadataUuid);
        }

        restructureReportToHavePatternRuleHierarchy(errorReport);

        // --- update element and return status
        Element elResp = new Element("root");
        elResp.addContent(new Element(Geonet.Elem.ID).setText(id));
        elResp.addContent(new Element("language").setText(locale.getISO3Language()));
        elResp.addContent(new Element("schema").setText(dataManager.getMetadataSchema(id)));
        elResp.addContent(errorReport);
        Element schematronTranslations = new Element("schematronTranslations");

        final SchematronRepository schematronRepository = context.getBean(SchematronRepository.class);
        // --- add translations for schematrons
        final List<Schematron> schematrons = schematronRepository.findAllBySchemaName(schemaName);

        MetadataSchema metadataSchema = dataManager.getSchema(schemaName);
        Path schemaDir = metadataSchema.getSchemaDir();
        SAXBuilder builder = getSAXBuilder(false);

        for (Schematron schematron : schematrons) {
            // it contains absolute path to the xsl file
            String rule = schematron.getRuleName();

            Path file = schemaDir.resolve("loc").resolve(locale.getISO3Language()).resolve(rule + ".xml");

            Document document;
            if (Files.isRegularFile(file)) {
                try (InputStream in = IO.newInputStream(file)) {
                    document = builder.build(in);
                }
                Element element = document.getRootElement();

                Element s = new Element(rule);
                element.detach();
                s.addContent(element);
                schematronTranslations.addContent(s);
            }
        }
        elResp.addContent(schematronTranslations);

        // TODO: Avoid XSL
        GeonetworkDataDirectory dataDirectory = context.getBean(GeonetworkDataDirectory.class);
        Path validateXsl = dataDirectory.getWebappDir().resolve("xslt/services/metadata/validate.xsl");
        Map<String, Object> params = new HashMap<>();
        params.put("rootTag", "reports");
        List<Element> elementList = getSchemaLocalization(metadata.getDataInfo().getSchemaId(),
            locale.getISO3Language());
        for (Element e : elementList) {
            elResp.addContent(e);
        }
        final Element transform = Xml.transform(elResp, validateXsl, params);

        Reports response = (Reports) Xml.unmarshall(transform, Reports.class);

        List<Report> reports = response.getReport();

        if (reports != null) {
            int value = 1;
            for (Report report : reports) {
                if (!report.getSuccess().equals(report.getTotal())) {
                    value = 0;
                }
            }
            new RecordValidationTriggeredEvent(metadata.getId(),
                ApiUtils.getUserSession(request.getSession()).getUserIdAsInt(), Integer.toString(value))
                .publish(appContext);
        }
        return response;
    }
}
