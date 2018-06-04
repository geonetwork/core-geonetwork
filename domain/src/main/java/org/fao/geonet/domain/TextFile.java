package org.fao.geonet.domain;

import javax.persistence.*;

/**
 * An entity representing a text file with mime type.
 *
 */

@Entity
@Table(name = "files")
@SequenceGenerator(name= TextFile.ID_SEQ_NAME, initialValue=1, allocationSize=1)
public class TextFile extends GeonetEntity {

    static final String ID_SEQ_NAME = "files_id_seq";

    private int _id;
    private String _content;
    private String _mimeType;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = TextFile.ID_SEQ_NAME)
    @Column(nullable = false)
    public int getId() {
        return this._id;
    }

    @Column(nullable = false)
    @Lob
    public String getContent() {
        return this._content;
    }

    @Column(nullable = false)
    public String getMimeType() {
        return this._mimeType;
    }

    public TextFile setId(int id) {
        this._id = id;
        return this;
    }

    public TextFile setMimeType(String mimeType) {
        this._mimeType = mimeType;
        return this;
    }

    public TextFile setContent(String content) {
        this._content = content;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + this._id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TextFile other = (TextFile) obj;
        return this._id == other._id;
    }

}
