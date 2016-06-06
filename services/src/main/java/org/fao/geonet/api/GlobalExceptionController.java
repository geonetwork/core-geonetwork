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

import org.fao.geonet.api.exception.NoResultsFoundException;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.MissingResourceException;

/**
 */
@ControllerAdvice
public class GlobalExceptionController {

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
        SecurityException.class
    })
    public Object unauthorizedHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "unauthorized");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        MaxUploadSizeExceededException.class
    })
    public Object maxFileExceededHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "max_file_exceeded");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            FileNotFoundException.class,
            NoResultsFoundException.class})
    public Object NotFoundHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "not_found");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        UserNotFoundEx.class,
        ResourceNotFoundException.class})
    public Object resourceNotFoundHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "resource_not_found");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        ResourceAlreadyExistException.class})
    public Object resourceAlreadyExistHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "resource_already_exist");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object missingParameterHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "required_parameter_missing");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        UnsatisfiedServletRequestParameterException.class,
        IllegalArgumentException.class,
        MultipartException.class
    })
    public Object unsatisfiedParameterHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "unsatisfied_request_parameter");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        MissingResourceException.class
    })
    public Object missingResourceHandler(final Exception exception) {
        return new LinkedHashMap<String, String>() {{
            put("code", "missing_resource_parameter");
            put("message", exception.getClass().getSimpleName());
            put("description", exception.getMessage());
        }};
    }
}
