package org.fao.geonet.monitor.health;

import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.requests.GetRecordsRequest;
import org.jdom.Element;

/**
 * Checks to ensure that the CSW subsystem is accessible and functioning
 * <p/>
 * User: jeichar
 * Date: 3/26/12
 * Time: 9:01 AM
 */
public class CswGetRecordsHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck("Csw GetRecords") {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                String host = gc.getSettingManager().getValue(Geonet.Settings.SERVER_HOST);
                String port = gc.getSettingManager().getValue(Geonet.Settings.SERVER_PORT);
                final String baseUrl = context.getBaseUrl();
                try {
                    GetRecordsRequest getRecords = new GetRecordsRequest();

                    getRecords.setHost(host);
                    getRecords.setPort(port == null ? 80 : Integer.parseInt(port));
                    getRecords.setAddress(baseUrl + "/srv/eng/csw");
                    getRecords.setResultType(ResultType.HITS);
                    getRecords.setMaxRecords("1");
                    Element result = getRecords.execute();
                    if (result.getChild("SearchResults", Csw.NAMESPACE_CSW) == null)
                        return Result.unhealthy("GetRecords request did not contain a SearchResults Element as Expected.  Xml = " + Xml.getString(result));
                    return Result.healthy();
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }
}
