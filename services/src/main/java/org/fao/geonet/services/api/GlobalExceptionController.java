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

package org.fao.geonet.services.api;

import org.fao.geonet.services.api.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Created by francois on 22/01/16.
 */
@ControllerAdvice
public class GlobalExceptionController {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MaxUploadSizeExceededException.class
    })
    public Object maxFileExceededHandler(final Exception exception) {
        exception.printStackTrace();
        return  new HashMap<String, String>() {{
            put("result", "failed");
            put("type", "max_file_exceeded");
            put("message", exception.getClass() + " " + exception.getMessage());
        }};
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            FileNotFoundException.class})
    public Object fileNotFoundHandler(final Exception exception) {
        exception.printStackTrace();
        return  new HashMap<String, String>() {{
            put("result", "failed");
            put("type", "file_not_found");
            put("message", exception.getClass() + " " + exception.getMessage());
        }};
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            ResourceNotFoundException.class})
    public Object resourceNotFoundHandler(final Exception exception) {
        return  new HashMap<String, String>() {{
            put("result", "failed");
            put("type", "resource_not_found");
            put("message", exception.getClass() + " " + exception.getMessage());
        }};
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object missingParameterHandler(final Exception exception) {
        return  new HashMap<String, String>() {{
            put("result", "failed");
            put("type", "required_parameter_missing");
            put("message", exception.getMessage());
        }};
    }
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            UnsatisfiedServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public Object unsatisfiedParameterHandler(final Exception exception) {
        return  new HashMap<String, String>() {{
            put("result", "failed");
            put("type", "unsatisfied_request_parameter");
            put("message", exception.getMessage());
        }};
    }
}
