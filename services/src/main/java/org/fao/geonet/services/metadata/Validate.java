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

import com.google.common.collect.Lists;
import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.fao.geonet.utils.IO;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * For editing : update leaves information. Access is restricted
 * Validate current metadata record in session.
 * <p/>
 * FIXME : id MUST be the id of the current metadata record in session ?
 */
public class Validate extends NotInReadOnlyModeService {
    static final String EL_ACTIVE_PATTERN = "active-pattern";
    static final String EL_FIRED_RULE = "fired-rule";
    static final String EL_FAILED_ASSERT = "failed-assert";
    static final String EL_SUCCESS_REPORT = "successful-report";
    static final String ATT_CONTEXT = "context";
    static final String DEFAULT_CONTEXT = "??";

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
        DataManager dataMan = gc.getBean(DataManager.class);

        UserSession session = context.getUserSession();

        String id = Utils.getIdentifierFromParameters(params, context);
        String schemaName = dataMan.getMetadataSchema(id);

        //--- validate metadata from session
        Element errorReport = new AjaxEditUtils(context).validateMetadataEmbedded(session, id, context.getLanguage());

        restructureReportToHavePatternRuleHierarchy(errorReport);

        //--- update element and return status
        Element elResp = new Element(Jeeves.Elem.RESPONSE);
        elResp.addContent(new Element(Geonet.Elem.ID).setText(id));
        elResp.addContent(new Element("schema").setText(dataMan.getMetadataSchema(id)));
        elResp.addContent(errorReport);
        Element schematronTranslations = new Element("schematronTranslations");

        final SchematronRepository schematronRepository = context.getBean(SchematronRepository.class);
        // --- add translations for schematrons
        final List<Schematron> schematrons = schematronRepository.findAllBySchemaName(schemaName);

        MetadataSchema metadataSchema = dataMan.getSchema(schemaName);
        Path schemaDir = metadataSchema.getSchemaDir();
        SAXBuilder builder = new SAXBuilder();

        for (Schematron schematron : schematrons) {
            // it contains absolute path to the xsl file
            String rule = schematron.getRuleName();

            Path file = schemaDir.resolve("loc").resolve(context.getLanguage()).resolve(rule + ".xml");

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

        return elResp;
    }

    /**
     * Schematron report has an odd structure:
     * <pre><code>
     * &lt;svrl:active-pattern  ... />
     * &lt;svrl:fired-rule  ... />
     * &lt;svrl:failed-assert ... />
     * &lt;svrl:successful-report ... />
     * </code></pre>
     * <p/>
     * This method restructures the xml to be:
     * <pre><code>
     * &lt;svrl:active-pattern  ... >
     *     &lt;svrl:fired-rule  ... >
     *         &lt;svrl:failed-assert ... />
     *         &lt;svrl:successful-report ... />
     *     &lt;svrl:fired-rule  ... >
     * &lt;svrl:active-pattern>
     * </code></pre>
     *
     * @param errorReport
     */
    static void restructureReportToHavePatternRuleHierarchy(Element errorReport) {
        final Iterator patternFilter = errorReport.getDescendants(new ElementFilter(EL_ACTIVE_PATTERN, Geonet.Namespaces.SVRL));
        @SuppressWarnings("unchecked")
        List<Element> patterns = Lists.newArrayList(patternFilter);
        for (Element pattern : patterns) {
            final Element parentElement = pattern.getParentElement();
            Element currentRule = null;
            @SuppressWarnings("unchecked")
            final List<Element> children = parentElement.getChildren();

            int index = children.indexOf(pattern) + 1;
            while(index < children.size() && !children.get(index).getName().equals(EL_ACTIVE_PATTERN)) {
                Element next = children.get(index);
                if (EL_FIRED_RULE.equals(next.getName())) {
                    currentRule = next;
                    next.detach();
                    pattern.addContent(next);
                } else {
                    if (currentRule == null) {
                        // odd but could happen I suppose
                        currentRule = new Element(EL_FIRED_RULE, Geonet.Namespaces.SVRL).
                                setAttribute(ATT_CONTEXT, DEFAULT_CONTEXT);
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
}