package org.fao.geonet.services.region;

import java.util.Collection;

import jeeves.server.context.ServiceContext;

public interface RegionsDAO {

    Request createSearchRequest(ServiceContext context) throws Exception;

}
