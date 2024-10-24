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
package org.fao.geonet.api;

import javax.xml.bind.annotation.XmlRootElement;

import org.fao.geonet.exceptions.ILocalizedException;
import org.springframework.util.StringUtils;

/**
 * Represents an API error message to be send to the requesting agent.
 * Created by juanluisrp on 10/06/2016.
 */
@XmlRootElement
public class ApiError {
    private String message;
    private String code;
    private String description;
    private Exception exception;

    public ApiError() {
    }

    public ApiError(String code) {
        this(code, null, null);
    }

    public ApiError(String code, String message) {
        this(code, message, null);
    }


    public ApiError(String code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public ApiError(String code, Exception exception) {
        this(code,
            exception.getMessage(),
            null
        );
        this.exception = exception;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        String localizedMessage=null;
        // If the exception extends ILocalizedException then get the local message from the exception.
        if (this.exception != null && this.exception instanceof ILocalizedException) {
            localizedMessage =  ((ILocalizedException)this.exception).getLocalizedMessage();
        }
        if (StringUtils.isEmpty(localizedMessage)) {
            return this.message;
        } else {
            return localizedMessage;
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        String localizedDescription=null;
        // If the exception extends ILocalizedException then get the local description from the exception.
        if (this.exception != null && this.exception instanceof ILocalizedException) {
            localizedDescription = ((ILocalizedException)this.exception).getLocalizedDescription();
        }
        if (StringUtils.isEmpty(localizedDescription)) {
            return this.description;
        } else {
            return localizedDescription;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
