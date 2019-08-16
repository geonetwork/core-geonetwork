package org.fao.geonet.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"url", "typeName"}))
public class MessageProducerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private WfsHarvesterParamEntity wfsHarvesterParamEntity;
    private String cronExpression;

    public WfsHarvesterParamEntity getWfsHarvesterParam() {
        return wfsHarvesterParamEntity;
    }

    public void setWfsHarvesterParam(WfsHarvesterParamEntity wfsHarvesterParamEntity) {
        this.wfsHarvesterParamEntity = wfsHarvesterParamEntity;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpession) {
        this.cronExpression = cronExpession;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
