//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.database;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseHarvesterUtilTest {

    @Test
    public void sanitizeFieldName_handlesNullEmptyAndWhitespace() {
        assertEquals("", DatabaseHarvesterUtil.sanitizeFieldName(null));
        assertEquals("", DatabaseHarvesterUtil.sanitizeFieldName(""));
        assertEquals("", DatabaseHarvesterUtil.sanitizeFieldName("   "));
    }

    @Test
    public void sanitizeFieldName_removesInvalidPrefixAndChars() {
        assertEquals("field1", DatabaseHarvesterUtil.sanitizeFieldName("123field-1"));
        assertEquals("name2", DatabaseHarvesterUtil.sanitizeFieldName("@name-2"));
        assertEquals("abc", DatabaseHarvesterUtil.sanitizeFieldName("a-b c"));
    }

    @Test
    public void sanitizeFieldName_validFieldNames() {
        assertEquals("field_1", DatabaseHarvesterUtil.sanitizeFieldName("field_1"));
        assertEquals("_field1", DatabaseHarvesterUtil.sanitizeFieldName("_field1"));
        assertEquals("field1", DatabaseHarvesterUtil.sanitizeFieldName("field1"));
    }

    @Test
    public void sanitizeTableName_handlesNullEmptyAndWhitespace() {
        assertEquals("", DatabaseHarvesterUtil.sanitizeTableName(null));
        assertEquals("", DatabaseHarvesterUtil.sanitizeTableName(""));
        assertEquals("", DatabaseHarvesterUtil.sanitizeTableName("   "));
    }

    @Test
    public void sanitizeTableName_removesInvalidPrefixAndChars() {
        assertEquals("table1", DatabaseHarvesterUtil.sanitizeTableName("99table-1"));
        assertEquals("table", DatabaseHarvesterUtil.sanitizeTableName("@table"));
        assertEquals("schema.table", DatabaseHarvesterUtil.sanitizeTableName("@schema.table"));
        assertEquals("schema.abc", DatabaseHarvesterUtil.sanitizeTableName("@sch-ema.123abc"));
        assertEquals("a.b", DatabaseHarvesterUtil.sanitizeTableName("1a.$b"));
    }

    @Test
    public void sanitizeTableName_validTableNames() {
        assertEquals("table123", DatabaseHarvesterUtil.sanitizeTableName("table123"));
        assertEquals("_table", DatabaseHarvesterUtil.sanitizeTableName("_table"));
        assertEquals("schema.table", DatabaseHarvesterUtil.sanitizeTableName("schema.table"));
        assertEquals("schema._table", DatabaseHarvesterUtil.sanitizeTableName("schema._table"));
    }


}
