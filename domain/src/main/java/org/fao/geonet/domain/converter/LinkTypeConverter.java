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
import org.fao.geonet.domain.LinkType;
import org.fao.geonet.domain.UserSearchFeaturedType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.beans.PropertyEditorSupport;

@Converter
public class LinkTypeConverter
    extends PropertyEditorSupport
    implements AttributeConverter<LinkType, String> {

    @Override
    public void setAsText(final String linkType) throws IllegalArgumentException {
        UserSearchFeaturedType value = null;

        if (StringUtils.isNotEmpty(linkType) && linkType.length() == 1) {
            value = UserSearchFeaturedType.byChar(linkType.charAt(0));
        }
        setValue(value);
    }

    @Override
    public String convertToDatabaseColumn(LinkType attribute) {
        return attribute.asString();
    }

    @Override
    public LinkType convertToEntityAttribute(String dbdata) {
        return LinkType.findByValue(dbdata);
    }

}
