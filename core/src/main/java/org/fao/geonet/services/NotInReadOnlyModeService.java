package org.fao.geonet.services;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.logging.LogFactory;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

/**
 * Base class for services that should not run their normal execution path if GeoNetwork is in read-only mode.
 * @author heikki doeleman
 */
public abstract class NotInReadOnlyModeService extends MailSendingService {
    private org.apache.commons.logging.Log log = LogFactory.getLog(NotInReadOnlyModeService.class);

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {}

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        // READONLYMODE
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        if (!gc.isReadOnly()) {
            return serviceSpecificExec(params, context);
        } else {
            log.debug("GeoNetwork is operating in read-only mode. Service execution skipped.");
            return null;
        }
    }

    /**
     * Contains the code for normal execution, when GeoNetwork is not in read-only mode.
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    public abstract Element serviceSpecificExec(Element params, ServiceContext context) throws Exception;
}