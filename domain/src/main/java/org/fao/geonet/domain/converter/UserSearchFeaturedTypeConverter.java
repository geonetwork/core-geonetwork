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

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.UserSearchFeaturedType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.beans.PropertyEditorSupport;

@Converter
public class UserSearchFeaturedTypeConverter
    extends PropertyEditorSupport
    implements AttributeConverter<UserSearchFeaturedType, Character> {

    @Override
    public void setAsText(final String featuredType) throws IllegalArgumentException {
        UserSearchFeaturedType value = null;

        if (StringUtils.isNotEmpty(featuredType) && featuredType.length() == 1) {
            value = UserSearchFeaturedType.byChar(featuredType.charAt(0));
        }
        setValue(value);
    }

    @Override
    public Character convertToDatabaseColumn(UserSearchFeaturedType attribute) {
        return attribute == null ? null : attribute.asChar();
    }

    @Override
    public UserSearchFeaturedType convertToEntityAttribute(Character dbdata) {
        return UserSearchFeaturedType.byChar( dbdata );
    }

}
