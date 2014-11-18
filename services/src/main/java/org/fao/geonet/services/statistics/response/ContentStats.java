package org.fao.geonet.services.statistics.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates the data calculated by the {@link org.fao.geonet.services.statistics.ContentStatistics#exec()} service.
 *
 * @author Jesse on 11/17/2014.
 */
@XmlRootElement(name = "response")
public class ContentStats {
    private long totalNonTemplateMetadata;
    private long totalHarvestedNonTemplateMetadata;
    private long totalTemplateMetadata;
    private long totalSubTemplateMetadata;
    private long totalPublicMetadata;

    public void setTotalNonTemplateMetadata(long totalNonTemplateMetadata) {
        this.totalNonTemplateMetadata = totalNonTemplateMetadata;
    }

    @XmlElement(name = "nb_metadata")
    public long getTotalNonTemplateMetadata() {
        return totalNonTemplateMetadata;
    }

    public void setTotalHarvestedNonTemplateMetadata(long totalHarvestedNonTemplateMetadata) {
        this.totalHarvestedNonTemplateMetadata = totalHarvestedNonTemplateMetadata;
    }

    @XmlElement(name = "nb_harvested")
    public long getTotalHarvestedNonTemplateMetadata() {
        return totalHarvestedNonTemplateMetadata;
    }

    public void setTotalTemplateMetadata(long totalTemplateMetadata) {
        this.totalTemplateMetadata = totalTemplateMetadata;
    }

    @XmlElement(name = "nb_template")
    public long getTotalTemplateMetadata() {
        return totalTemplateMetadata;
    }

    public void setTotalSubTemplateMetadata(long totalSubTemplateMetadata) {
        this.totalSubTemplateMetadata = totalSubTemplateMetadata;
    }

    @XmlElement(name = "nb_subtemplate")
    public long getTotalSubTemplateMetadata() {
        return totalSubTemplateMetadata;
    }

    public void setTotalPublicMetadata(long totalPublicMetadata) {
        this.totalPublicMetadata = totalPublicMetadata;
    }

    @XmlElement(name = "nb_metadata_public")
    public long getTotalPublicMetadata() {
        return totalPublicMetadata;
    }
}
