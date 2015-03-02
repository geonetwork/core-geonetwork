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

import javax.servlet.http.HttpSession;
import java.nio.file.Path;
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

    public void init(Path appPath, ServiceConfig params) throws Exception {
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
        UserSession usersession = context.getUserSession();
//        HttpSession session = usersession.getsHttpSession();

        // Clear previous report
        usersession.removeProperty(Geonet.Session.BATCH_PROCESSING_REPORT);
//        https://github.com/geonetwork/core-geonetwork/issues/828
//        Batch processing report should be stored in HttpSession and
//        not userSession due to the move to Spring MVC.
//        Here it fails with IllegalStateException
//        FIXME
//        session.removeAttribute(Geonet.Session.BATCH_PROCESSING_REPORT);

        // Apply the process to the selection
        Set<Integer> metadata = new HashSet<Integer>();

        context.info("Get selected metadata");
        SelectionManager sm = SelectionManager.getManager(context.getUserSession());

        synchronized (sm.getSelection("metadata")) {
            report.setTotalRecords(sm.getSelection("metadata").size());
            MassiveXslMetadataReindexer m = new MassiveXslMetadataReindexer(dataMan,
                    sm.getSelection("metadata").iterator(),
                    process, params,
                    context, metadata, report);
            m.process();
        }

        // Add the report to the session
        usersession.setProperty(
                Geonet.Session.BATCH_PROCESSING_REPORT,
                report);
//        session.setAttribute(
//                Geonet.Session.BATCH_PROCESSING_REPORT,
//                report);

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
