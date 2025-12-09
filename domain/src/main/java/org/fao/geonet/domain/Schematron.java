/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.domain;

import java.io.File;
import java.util.Comparator;
import java.util.Map;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

/**
 * An entity representing a schematron. It contains the file to the schematron definition, the
 * schema it belongs and if it is required or just a recommendation.
 *
 * @author delawen
 */
@Entity
@Table(name = "schematron",
    uniqueConstraints = @UniqueConstraint(columnNames = {"schemaName", "filename"}))
@Cacheable
@Access(AccessType.PROPERTY)
@SequenceGenerator(name = Schematron.ID_SEQ_NAME, initialValue = 100, allocationSize = 1)
public class Schematron extends Localized {
    public static final Comparator<? super Schematron> DISPLAY_PRIORITY_COMPARATOR = new Comparator<Schematron>() {
        @Override
        public int compare(Schematron o1, Schematron o2) {
            return Integer.compare(o1.getDisplayPriority(), o2.getDisplayPriority());
        }
    };
    static final String ID_SEQ_NAME = "schematron_id_seq";
    private final static int EXTENSION_LENGTH = ".xsl".length();
    private final static String SEPARATOR = File.separator;
    private final static String ALT_SEPARATOR;

    static {
        if (SEPARATOR.equals("\\")) {
            ALT_SEPARATOR = "/";
        } else {
            ALT_SEPARATOR = "\\";
        }
    }

    private int id;
    private String schemaName;
    private String file;
    private int displayPriority = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
    @Column(nullable = false)
    public int getId() {
        return id;
    }

    public Schematron setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public String toString() {
        return "Schematron [_id=" + id + ", isoschema=" + schemaName + ", filename="
            + file + ", description"
            + getLabelTranslations() + "]";
    }

    /**
     * @return the schema
     */
    @Column(nullable = false, name = "schemaName")
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * @param schemaName the schema to set
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * @return the file
     */
    @Column(nullable = false, name = "filename")
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "SchematronDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false, length = 255)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }

    /**
     * Defines the order in which the rules should be displayed in the schematron report.
     *
     * 0 is highest priority and should be first in the list.
     */
    public int getDisplayPriority() {
        return displayPriority;
    }

    public void setDisplayPriority(int displayPriority) {
        this.displayPriority = displayPriority;
    }

    @Transient
    public String getRuleName() {
        if (file == null) {
            return "unnamed rule";
        }
        int lastSegmentIndex = file.lastIndexOf(SEPARATOR);
        if (lastSegmentIndex < 0) {
            lastSegmentIndex = file.lastIndexOf(ALT_SEPARATOR);
        }

        if (lastSegmentIndex < 0) {
            lastSegmentIndex = 0;
        } else {
            // drop the separator character
            lastSegmentIndex += 1;
        }

        String rule = file.substring(lastSegmentIndex, file.length() - EXTENSION_LENGTH);
        String lowerCaseRuleName = rule.toLowerCase();
        for (SchematronRequirement requirement : SchematronRequirement.values()) {
            if (lowerCaseRuleName.endsWith("." + requirement.name().toLowerCase())) {
                return rule.substring(0, rule.length() - requirement.name().length() - 1);
            }
        }
        return rule;
    }

    @Transient
    public SchematronRequirement getDefaultRequirement() {
        final String lowerCaseFile = getFile().toLowerCase();
        for (SchematronRequirement requirement : SchematronRequirement.values()) {
            if (lowerCaseFile.endsWith("." + requirement.name().toLowerCase() + ".xsl")) {
                return requirement;
            }
        }
        return SchematronRequirement.REQUIRED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schematron that = (Schematron) o;

        if (displayPriority != that.displayPriority) return false;
        if (file != null ? !file.equals(that.file) : that.file != null) return false;
        if (schemaName != null ? !schemaName.equals(that.schemaName) : that.schemaName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = schemaName != null ? schemaName.hashCode() : 0;
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + displayPriority;
        return result;
    }
}
