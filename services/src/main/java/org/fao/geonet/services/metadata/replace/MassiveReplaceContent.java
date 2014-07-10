package org.fao.geonet.services.metadata.replace;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.*;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Set;


/**
 * Service to apply replacements to a metadata selection.
 *
 * With the parameter test=true, the service tests the replacements, returning a report with all the changes applied.
 *
 * Request:
 * <request>
 *     <mdsection-1398155513728>metadata</mdsection-1398155513728>
 *     <mdfield-1398155513728>id.contact.individualName</mdfield-1398155513728>
 *     <replaceValue-1398155513728>Juan</replaceValue-1398155513728>
 *     <searchValue-1398155513728>Jose</searchValue-1398155513728>
 *     <test>true</test>
 * </request>
 *
 * @author Jose García
 */
public class MassiveReplaceContent extends NotInReadOnlyModeService {
    private boolean fullResponse = false;

    public void init(String appPath, ServiceConfig params) throws Exception {
        // Used to return different response depending on UI invoking the service:
        //  - ExtJs UI: fullResponse = true
        //  - Angular UI: fullResponse = false or not provided
        fullResponse = Boolean.parseBoolean(params.getValue("fullResponse", "false"));
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context)
            throws Exception {
        String process = "massive-content-update";
        MassiveReplaceReport report = new MassiveReplaceReport(process);

        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getBean(DataManager.class);
        UserSession session = context.getUserSession();

        // Clear previous report
        session.removeProperty("BATCH_PROCESSING_REPORT");

        // Apply the process to the selection
        Set<Integer> metadata = new HashSet<Integer>();

        context.info("Get selected metadata");
        SelectionManager sm = SelectionManager.getManager(session);

        synchronized (sm.getSelection("metadata")) {
            report.setTotalRecords(sm.getSelection("metadata").size());
            MassiveXslMetadataReindexer m = new MassiveXslMetadataReindexer(dataMan,
                    sm.getSelection("metadata").iterator(),
                    process, params,
                    context, metadata, report);
            m.process();
        }

        // Add the report to the session
        session.setProperty("BATCH_PROCESSING_REPORT", report);


        if (fullResponse) {
            return new Element(Jeeves.Elem.RESPONSE)
                .addContent(
                        report.toXml())
                .addContent(
                        (Element) params.clone())
                .addContent(
                        new Element("test").setText(params.getChildText("test")));
        } else {
            return report.toXml();
        }
    }
}
