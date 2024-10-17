/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.api.exception;

import java.util.Locale;

import org.fao.geonet.exceptions.LocalizedException;

public class GeonetMaxUploadSizeExceededException extends LocalizedException {

    public GeonetMaxUploadSizeExceededException() {
        super();
    }

    public GeonetMaxUploadSizeExceededException(String message) {
        super(message);
    }

    public GeonetMaxUploadSizeExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeonetMaxUploadSizeExceededException(Throwable cause) {
        super(cause);
    }

    protected String getResourceBundleBeanQualifier() {
        return "apiMessages";
    }

    @Override
    public GeonetMaxUploadSizeExceededException withMessageKey(String messageKey) {
        super.withMessageKey(messageKey);
        return this;
    }

    @Override
    public GeonetMaxUploadSizeExceededException withMessageKey(String messageKey, Object[] messageKeyArgs) {
        super.withMessageKey(messageKey, messageKeyArgs);
        return this;
    }

    @Override
    public GeonetMaxUploadSizeExceededException withDescriptionKey(String descriptionKey) {
        super.withDescriptionKey(descriptionKey);
        return this;
    }

    @Override
    public GeonetMaxUploadSizeExceededException withDescriptionKey(String descriptionKey, Object[] descriptionKeyArgs) {
        super.withDescriptionKey(descriptionKey, descriptionKeyArgs);
        return this;
    }

    @Override
    public GeonetMaxUploadSizeExceededException withLocale(Locale locale) {
        super.withLocale(locale);
        return this;
    }
}
