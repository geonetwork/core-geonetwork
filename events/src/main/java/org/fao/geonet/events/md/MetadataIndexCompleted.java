/**
 *
 */
package org.fao.geonet.events.md;

import org.fao.geonet.domain.Metadata;

/**
 * Event launched when the indexation of a metadata record is finished
 *
 * @author delawen
 */
public class MetadataIndexCompleted extends MetadataEvent {

    private static final long serialVersionUID = 6646733956246220509L;

    /**
     * @param metadata
     */
    public MetadataIndexCompleted(Metadata metadata) {
        super(metadata);
    }

}
