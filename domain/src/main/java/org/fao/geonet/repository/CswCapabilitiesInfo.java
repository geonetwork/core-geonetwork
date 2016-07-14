//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.repository;

import org.fao.geonet.domain.CswCapabilitiesInfoField;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents all the fields required for Csw Capabilities Info in a given language. <p> The data
 * for Csw Capabilities info is stored in the database as one row per field.  This allows the
 * database to be flexible in the face of future requirements.  However this class makes it simpler
 * (and type safe) to access the fields. </p> <p> When loaded by the
 * CswCapabilitiesInfoFieldRepository, all the required fields are loaded and this class provides
 * easy access to the fields. </p> <p> It also provides a simple way to modify and save the fields.
 * </p>
 */
public class CswCapabilitiesInfo {
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_ABSTRACT = "abstract";
    private static final String FIELD_FEES = "fees";
    private static final String FIELD_ACCESS_CONSTRAINTS = "accessConstraints";
    private final Map<String, CswCapabilitiesInfoField> _fields = new HashMap<String, CswCapabilitiesInfoField>();
    private String _langId;

    CswCapabilitiesInfo(String landId, final List<CswCapabilitiesInfoField> allFieldsForLang) {
        this._langId = landId;
        for (CswCapabilitiesInfoField cswCapabilitiesInfoField : allFieldsForLang) {
            _fields.put(cswCapabilitiesInfoField.getFieldName(), cswCapabilitiesInfoField);
        }
    }

    /**
     * Get the value of the title field.
     */
    @Nullable
    public String getTitle() {
        final String key = FIELD_TITLE;
        return getValue(key);
    }

    /**
     * Set the value of the title field.
     *
     * @param title the new value
     */
    public void setTitle(@Nonnull final String title) {
        setValue(FIELD_TITLE, title);
    }

    /**
     * Get the value of the abstract field.
     */
    @Nullable
    public String getAbstract() {
        return getValue(FIELD_ABSTRACT);
    }

    /**
     * Set the value of the abstract field.
     *
     * @param newAbstract the new abstract value
     */
    public void setAbstract(@Nonnull final String newAbstract) {
        setValue(FIELD_ABSTRACT, newAbstract);
    }

    /**
     * Get the value of the fees field.
     */
    @Nullable
    public String getFees() {
        return getValue(FIELD_FEES);
    }

    /**
     * Set the value of the fees field.
     *
     * @param fees the new fees value.
     */
    public void setFees(@Nonnull final String fees) {
        setValue(FIELD_FEES, fees);
    }

    /**
     * Get the value of the AccessConstraints field.
     */
    @Nullable
    public String getAccessConstraints() {
        return getValue(FIELD_ACCESS_CONSTRAINTS);
    }

    /**
     * Set the value of the AccessConstraints field.
     *
     * @param accessConstraints the new constraints value
     */
    public void setAccessConstraints(@Nonnull final String accessConstraints) {
        setValue(FIELD_ACCESS_CONSTRAINTS, accessConstraints);
    }

    /**
     * Get the 3 letter language code id.
     */
    @Nonnull
    public String getLangId() {
        return _langId;
    }

    /**
     * Set the lang code for this info object.
     *
     * @param langId the language code.
     */
    void setLangId(@Nonnull final String langId) {
        this._langId = langId;
        for (CswCapabilitiesInfoField cswCapabilitiesInfoField : _fields.values()) {
            cswCapabilitiesInfoField.setLangId(langId);
        }
    }

    private void setValue(@Nonnull final String fieldTitle, @Nonnull final String newValue) {
        CswCapabilitiesInfoField field = _fields.get(fieldTitle);

        if (field == null) {
            field = new CswCapabilitiesInfoField().setFieldName(fieldTitle).setLangId(this._langId);
            _fields.put(fieldTitle, field);
        }

        field.setValue(newValue);
    }

    @Nullable
    private String getValue(@Nonnull final String key) {
        final CswCapabilitiesInfoField field = _fields.get(key);
        if (field == null) {
            return null;
        }
        return field.getValue();
    }

    /**
     * Get all the fields that this info object encapsulates.
     *
     * @return all the fields that this info object encapsulates.
     */
    public Collection<CswCapabilitiesInfoField> getFields() {
        return _fields.values();
    }
}
