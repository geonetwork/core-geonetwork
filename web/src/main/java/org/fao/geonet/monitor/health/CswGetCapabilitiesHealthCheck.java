package org.fao.geonet.monitor.health;

import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.requests.GetCapabilitiesRequest;
import org.jdom.Element;

/**
 * Checks to ensure that the CSW subsystem is accessible and functioning
 * <p/>
 * User: jeichar
 * Date: 3/26/12
 * Time: 9:01 AM
 */
public class CswGetCapabilitiesHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck("Csw GetCapabilities") {
            @Override
            protected Result check() throws Exception {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                String host = gc.getSettingManager().getValue(Geonet.Settings.SERVER_HOST);
                String port = gc.getSettingManager().getValue(Geonet.Settings.SERVER_PORT);
                final String baseUrl = context.getBaseUrl();
                try {
                    GetCapabilitiesRequest getCapabilities = new GetCapabilitiesRequest();

                    getCapabilities.setHost(host);
                    getCapabilities.setPort(port == null ? 80 : Integer.parseInt(port));
                    getCapabilities.setAddress(baseUrl + "/srv/eng/csw");
                    Element result = getCapabilities.execute();
                    if (result.getChild("ServiceIdentification", Csw.NAMESPACE_OWS) == null)
                        return Result.unhealthy("Capabilities did not have a 'ServiceIdentification' element as expected.  Xml: " + Xml.getString(result));
                    return Result.healthy();
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }
}
