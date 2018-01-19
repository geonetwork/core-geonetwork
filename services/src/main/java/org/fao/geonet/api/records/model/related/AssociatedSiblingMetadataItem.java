package org.fao.geonet.api.records.model.related;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "associatedSiblingMetadataItem")
public class AssociatedSiblingMetadataItem
        extends RelatedMetadataItem {

    @XmlElement(required = false)
    protected List<String> agg_isTemporalStatOf;

    @XmlElement(required = false)
    protected List<String> agg_largerWorkCitation;

    @XmlElement(required = false)
    protected List<String> agg_isDescriptionOf;

    public List<String> getAgg_isDescriptionOf() {
        return agg_isDescriptionOf;
    }

    public void setAgg_isDescriptionOf(List<String> agg_isDescriptionOf) {
        this.agg_isDescriptionOf = agg_isDescriptionOf;
    }

    public List<String> getAgg_isTemporalStatOf() {
        return agg_isTemporalStatOf;
    }

    public void setAgg_isTemporalStatOf(List<String> agg_isTemporalStatOf) {
        this.agg_isTemporalStatOf = agg_isTemporalStatOf;
    }

    public List<String> getAgg_largerWorkCitation() {
        return agg_largerWorkCitation;
    }

    public void setAgg_largerWorkCitation(List<String> agg_largerWorkCitation) {
        this.agg_largerWorkCitation = agg_largerWorkCitation;
    }
}
