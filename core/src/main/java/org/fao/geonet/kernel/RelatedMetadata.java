package org.fao.geonet.kernel;

import jeeves.server.context.ServiceContext;
import org.jdom.Element;

/**
 * Interface for allowing classes to look up metadata relationships via spring lookup.
 * In practice, geonetwork needed to depend on GetRelations (in service package) from SearchController (in csw-server package),
 * since this can't be done this interface is added to allow lookup.
 * <p/>
 * User: Jesse
 * Date: 11/18/13
 * Time: 1:23 PM
 */
public interface RelatedMetadata {
    public Element getRelated(ServiceContext context, int metadataId, String uuid, String relationType, int from, int to,
                              boolean fast) throws Exception;
}
