/*
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
 */

package org.fao.geonet.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.fao.geonet.domain.Constants;

/**
 *
 * @author Emanuele Tajariol <etj at geo-solutions.it>
 */
@Converter
public class BooleanToYNConverter implements AttributeConverter<Boolean, Character> {

    @Override
    public Character convertToDatabaseColumn(Boolean attribute)
    {
        return attribute != null ? Constants.toYN_EnabledChar(attribute) : Constants.YN_FALSE;
    }

    @Override
    public Boolean convertToEntityAttribute(Character dbData)
    {
        return dbData != null ? Constants.toBoolean_fromYNChar(dbData) : Boolean.FALSE;
    }
}
