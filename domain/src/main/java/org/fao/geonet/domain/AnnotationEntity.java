package org.fao.geonet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "AnnotationEntity")
@Access(AccessType.PROPERTY)
@SequenceGenerator(name = AnnotationEntity.ID_SEQ_NAME, initialValue = 1, allocationSize = 1)
public class AnnotationEntity implements Serializable {

    static final String ID_SEQ_NAME = "annotation_id_seq";

    private int id;
    private String uuid;
    private ObjectNode geometry;
    private Date lastRead;
    private Date lastWrite;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(nullable = false)
    @JsonIgnore
    public int getId() {
        return id;
    }

    public AnnotationEntity setId(int id) {
        this.id = id;
        return this;
    }

    @Column(unique = true)
    public String getUuid() {
        return uuid;
    }

    public AnnotationEntity setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Column(columnDefinition="TEXT")
    @Convert(converter = JsonConverter.class)
    public ObjectNode getGeometry() {
        return geometry;
    }

    public AnnotationEntity setGeometry(ObjectNode geoJson) {
        this.geometry = geoJson;
        return this;
    }

    @JsonIgnore
    public Date getLastRead() {
        return lastRead;
    }

    public AnnotationEntity setLastRead(Date lastRead) {
        this.lastRead = lastRead;
        return this;
    }

    @JsonIgnore
    public Date getLastWrite() {
        return lastWrite;
    }

    public AnnotationEntity setLastWrite(Date lastWrite) {
        this.lastWrite = lastWrite;
        return this;
    }
}
