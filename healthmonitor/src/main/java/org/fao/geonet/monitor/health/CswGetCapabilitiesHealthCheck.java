package org.fao.geonet.monitor.health;

import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.local.LocalServiceRequest;
import jeeves.server.sources.ServiceRequest.InputMethod;
import jeeves.utils.Xml;

import org.fao.geonet.csw.common.Csw;
import org.jdom.Element;

import com.yammer.metrics.core.HealthCheck;

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
                try {
					LocalServiceRequest request = LocalServiceRequest.create("local://csw?request=GetCapabilities&service=CSW");
					request.setDebug(false);
					request.setLanguage("eng");
					request.setInputMethod(InputMethod.GET);
					Element result = context.execute(request);

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
