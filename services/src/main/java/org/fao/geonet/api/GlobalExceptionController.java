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

import org.fao.geonet.api.exception.GeoPublisherException;
import org.fao.geonet.api.exception.NoResultsFoundException;
import org.fao.geonet.api.exception.ResourceAlreadyExistException;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.io.FileNotFoundException;
import java.util.MissingResourceException;

/**
 */
@ControllerAdvice
public class GlobalExceptionController {

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({
        ServiceNotAllowedEx.class
    })
    public Object unauthorizedHandler(final Exception exception) {
        return new ApiError("unauthorized", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        MaxUploadSizeExceededException.class
    })
    public ApiError maxFileExceededHandler(final Exception exception) {
        return new ApiError("max_file_exceeded", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        HttpMessageNotReadableException.class
    })
    public ApiError runtimeExceptionHandler(final Exception exception) {
        return new ApiError("runtime_exception", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
        FileNotFoundException.class,
        GeoPublisherException.class,
        NoResultsFoundException.class})
    public ApiError NotFoundHandler(final Exception exception) {
        return new ApiError("not_found", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
        UserNotFoundEx.class,
        ResourceNotFoundException.class})
    public ApiError resourceNotFoundHandler(final Exception exception) {
        return new ApiError("resource_not_found", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        ResourceAlreadyExistException.class})
    public ApiError resourceAlreadyExistHandler(final Exception exception) {
        return new ApiError("resource_already_exist", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiError missingParameterHandler(final Exception exception) {
        return new ApiError("required_parameter_missing", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        UnsatisfiedServletRequestParameterException.class,
        IllegalArgumentException.class,
        XSDValidationErrorEx.class,
        MultipartException.class
    })
    public ApiError unsatisfiedParameterHandler(final Exception exception) {
        return new ApiError("unsatisfied_request_parameter", exception.getClass().getSimpleName(),
            exception.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
        MissingResourceException.class
    })
    public ApiError missingResourceHandler(final Exception exception) {
        return new ApiError("missing_resource_parameter", exception.getClass().getSimpleName(),
            exception.getMessage());
    }
}
