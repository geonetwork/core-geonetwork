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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The datatype of a setting value.
 *
 * @author Jesse
 * @see Setting
 */
public enum SettingDataType {
    STRING {
        @Override
        public boolean validate(String value) {
            return true;
        }
    },
    INT {
        @Override
        public boolean validate(String value) {
            try {
                Integer.parseInt(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    },
    BOOLEAN {
        @Override
        public boolean validate(String value) {
            try {
                Boolean.parseBoolean(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    },
    JSON {
        @Override
        public boolean validate(String json) {
            try {
                final JsonParser parser =
                    new ObjectMapper()
                        .getJsonFactory()
                        .createJsonParser(json);
                while (parser.nextToken() != null) {
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    };

    public abstract boolean validate(String value);
}
