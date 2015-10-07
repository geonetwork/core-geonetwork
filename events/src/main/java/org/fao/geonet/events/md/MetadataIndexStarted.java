/**
 *
 */
package org.fao.geonet.events.md;

import org.fao.geonet.domain.Metadata;
import org.jdom.Element;

import java.util.Vector;

/**
 * Event launched when the indexation of a metadata record is previous to start, allowing to update the fields for indexing.
 *
 * @author Jose García
 */
public class MetadataIndexStarted extends MetadataEvent {

    private static final long serialVersionUID = 5119421930299384126L;

    private  Vector<Element> indexFields;


    /**
     * @param metadata
     */
    public MetadataIndexStarted(Metadata metadata, Vector<Element> indexFields) {
        super(metadata);
        this.indexFields = indexFields;
    }


    public Vector<Element> getIndexFields() {
        return indexFields;
    }
}
