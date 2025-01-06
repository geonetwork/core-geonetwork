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

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.fao.geonet.api.exception.*;
import org.fao.geonet.api.tools.i18n.LanguageUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.doi.client.DoiClientException;
import org.fao.geonet.exceptions.ILocalizedException;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.exceptions.UserNotFoundEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.inspire.validator.InspireValidatorException;
import org.fao.geonet.util.FileUtil;
import org.fao.geonet.utils.Log;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 *
 */
@ControllerAdvice
public class GlobalExceptionController {

    private final static String API_EXCEPTION_MESSAGE_PREFIX = "api.exception.";
    private final static String API_EXCEPTION_MESSAGE_POSTFIX = ".message";
    private final static String API_EXCEPTION_DESCRIPTION_POSTFIX = ".description";

    @Autowired
    private LanguageUtils languageUtils;

    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        NotAllowedException.class
    })
    public ApiError notAllowedHandler(final Exception exception, final HttpServletRequest request) {
        storeApiErrorCause(exception);

        if (contentTypeNeedsBody(request)) {
            updateExceptionLocale(exception, request);
            ApiError apiError = new ApiError("forbidden", exception);
            //FIXME not sure why exception message is stored in the message for this exception but stored in the description for the other exceptions.
            //    to fix, would need to identify all usages and change it to use the description.
            apiError.setMessage(exception.getMessage());
            return apiError;
        } else {
            return null;
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        ServiceNotAllowedEx.class
    })
    public Object unauthorizedHandler(final Exception exception) {
        storeApiErrorCause(exception);

        return new ApiError("unauthorized", exception);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        SecurityException.class,
    })
    public Object securityHandler(final Exception exception) {
        storeApiErrorCause(exception);

        return new ApiError("forbidden", exception);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        AccessDeniedException.class
    })
    public Object securityHandler(final HttpServletRequest request, final Exception exception) {
        storeApiErrorCause(exception);

        if (contentTypeNeedsBody(request)) {
                ApiError response = null;
            try {
                // Create a ForbiddenException with the required message keys so that the error contains is based on the request locale
                throw new ForbiddenException(exception)
                    .withMessageKey("api.exception.forbidden")
                    .withDescriptionKey("api.exception.forbidden.description");
            } catch (ForbiddenException forbiddenException) {
                updateExceptionLocale(forbiddenException, request);
                return new ApiError("forbidden", forbiddenException);
            }
        } else {
            return null;
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        MaxUploadSizeExceededException.class,
        RemoteFileTooLargeException.class
    })
    public ApiError maxFileExceededHandler(final Exception exception, final HttpServletRequest request) {
        Exception ex;
        long contentLength = request.getContentLengthLong();
        // As MaxUploadSizeExceededException is a spring exception, we need to convert it to a localized exception so that it can be translated.
        if (exception instanceof MaxUploadSizeExceededException) {
            ex = new GeonetMaxUploadSizeExceededException("uploadedResourceSizeExceededException", exception)
                .withMessageKey("exception.maxUploadSizeExceeded",
                    new String[]{FileUtil.humanizeFileSize(((MaxUploadSizeExceededException) exception).getMaxUploadSize())})
                .withDescriptionKey("exception.maxUploadSizeExceeded.description",
                    new String[]{FileUtil.humanizeFileSize(contentLength),
                        FileUtil.humanizeFileSize(((MaxUploadSizeExceededException) exception).getMaxUploadSize())});
        } else if (exception instanceof RemoteFileTooLargeException) {
            long maxUploadSize = ((RemoteFileTooLargeException) exception).getMaxUploadSize();
            long remoteFileSize = ((RemoteFileTooLargeException) exception).getRemoteFileSize();
            if (remoteFileSize == -1) {
                ex = new GeonetMaxUploadSizeExceededException("uploadedResourceSizeExceededException", exception)
                    .withMessageKey("exception.maxUploadSizeExceeded",
                        new String[]{FileUtil.humanizeFileSize(maxUploadSize)})
                    .withDescriptionKey("exception.maxUploadSizeExceededUnknownSize.description",
                        new String[]{FileUtil.humanizeFileSize(maxUploadSize)});
            } else {
                ex = new GeonetMaxUploadSizeExceededException("uploadedResourceSizeExceededException", exception)
                    .withMessageKey("exception.maxUploadSizeExceeded",
                        new String[]{FileUtil.humanizeFileSize(maxUploadSize)})
                    .withDescriptionKey("exception.maxUploadSizeExceeded.description",
                        new String[]{FileUtil.humanizeFileSize(remoteFileSize),
                            FileUtil.humanizeFileSize(maxUploadSize)});
            }
        } else {
            ex = exception;
        }

        storeApiErrorCause(ex);

        return new ApiError("max_file_exceeded", ex);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        Exception.class,
        InspireValidatorException.class,
        RuntimeException.class
    })
    public ApiError runtimeExceptionHandler(final Exception exception, final HttpServletRequest request) {
        storeApiErrorCause(exception);

        Log.error(Geonet.GEONETWORK, exception.getMessage(), exception);

        if (contentTypeNeedsBody(request)) {
            return new ApiError("runtime_exception", exception);
        } else {
            return null;
        }

    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        FeatureNotEnabledException.class
    })
    public ApiError runtimeExceptionHandler(final HttpServletRequest request, final Exception exception) {
        storeApiErrorCause(exception);

        if (contentTypeNeedsBody(request)) {
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getMessageKey())) {
                ((ILocalizedException) exception).setMessageKey("api.exception.resourceNotFound");
            }
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getDescriptionKey())) {
                ((ILocalizedException) exception).setDescriptionKey("api.exception.resourceNotFound.description");
            }
            updateExceptionLocale(exception, request);
            return new ApiError("feature_disabled", exception);
        } else {
            return null;
        }
    }


    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        WebApplicationException.class
    })
    public ApiError webappExceptionHandler(final Exception exception) {
        storeApiErrorCause(exception);

        return new ApiError("webapplication_exception", exception);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        FileNotFoundException.class,
        GeoPublisherException.class,
        NoResultsFoundException.class})
    public ApiError NotFoundHandler(final Exception exception) {
        storeApiErrorCause(exception);

        return new ApiError("not_found", exception);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        UserNotFoundEx.class,
        ResourceNotFoundException.class})
    public ApiError resourceNotFoundHandler(final HttpServletRequest request, final Exception exception) {
        storeApiErrorCause(exception);

        if (contentTypeNeedsBody(request)) {
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getMessageKey())) {
                ((ILocalizedException) exception).setMessageKey("api.exception.resourceNotFound");
            }
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getDescriptionKey())) {
                ((ILocalizedException) exception).setDescriptionKey("api.exception.resourceNotFound.description");
            }
            updateExceptionLocale(exception, request);
            return new ApiError("resource_not_found", exception);
        } else {
            return null;
        }
    }

    /**
     * If exception implements the ILocalizedException then update the locale based on the request.
     *
     * @param exception exception to be updated
     * @param request   containing locale information
     */
    private void updateExceptionLocale(Exception exception, HttpServletRequest request) {
        if (exception instanceof ILocalizedException) {
            if (((ILocalizedException) exception).getLocale() == null) {
                ((ILocalizedException) exception).setLocale(languageUtils.parseAcceptLanguage(request.getLocales()));
            }
        }
    }

    /**
     * @param request the HTTP request object.
     * @return true if the content type is allowed to have a body when returning an error to the client, false if the
     * response should contain an empty body.
     */
    private boolean contentTypeNeedsBody(HttpServletRequest request) {
        boolean needsBody;
        List<MediaType> requestMediaTypes = resolveMediaTypes(new ServletWebRequest(request));
        Set<MediaType> allowedContentTypes = Sets.newHashSet(
            MediaType.APPLICATION_XML,
            MediaType.APPLICATION_XHTML_XML,
            MediaType.APPLICATION_JSON
        );

        // If requested "text/html" with as preferred option on top of
        // the allowed content types, should contain empty body.
        int posHtmlMediaType = requestMediaTypes.indexOf(MediaType.TEXT_HTML);
        boolean requestedHtmlMediaType = false;

        if (posHtmlMediaType == 0) {
            requestedHtmlMediaType = true;
        } else if (posHtmlMediaType > -1) {
            // Intersect allowed mediatypes + text/html with requested
            // media types to check if text/html has precedence.
            List<MediaType> allowedContentTypesAndHtml =
                Arrays.asList(MediaType.APPLICATION_XML,
                    MediaType.APPLICATION_XHTML_XML,
                    MediaType.APPLICATION_JSON,
                    MediaType.TEXT_HTML);

            List<MediaType> requestMediaTypes2 = new ArrayList<>(requestMediaTypes);

            requestMediaTypes2.retainAll(allowedContentTypesAndHtml);
            requestedHtmlMediaType = (requestMediaTypes2.indexOf(MediaType.TEXT_HTML) == 0);
        }

        needsBody = !Collections.disjoint(allowedContentTypes, requestMediaTypes) &&
            !requestedHtmlMediaType;
        return needsBody;
    }

    public List<MediaType> resolveMediaTypes(NativeWebRequest request) {

        String header = request.getHeader(HttpHeaders.ACCEPT);
        if (!StringUtils.hasText(header)) {
            return Collections.emptyList();
        }
        try {
            List<MediaType> mediaTypes = MediaType.parseMediaTypes(header);
            MediaType.sortBySpecificityAndQuality(mediaTypes);
            return mediaTypes;
        } catch (InvalidMediaTypeException ex) {
            return Collections.emptyList();
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        ResourceAlreadyExistException.class})
    public ApiError resourceAlreadyExistHandler(final HttpServletRequest request, final Exception exception) {
        storeApiErrorCause(exception);

        if (contentTypeNeedsBody(request)) {
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getMessageKey())) {
                ((ILocalizedException) exception).setMessageKey("api.exception.resourceAlreadyExists");
            }
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getDescriptionKey())) {
                ((ILocalizedException) exception).setDescriptionKey("api.exception.resourceAlreadyExists.description");
            }
            updateExceptionLocale(exception, request);
            return new ApiError("resource_already_exist", exception);
        } else {
            return null;
        }

    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiError missingParameterHandler(final Exception exception) {
        storeApiErrorCause(exception);

        return new ApiError("required_parameter_missing", exception);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        UnsatisfiedServletRequestParameterException.class,
        IllegalArgumentException.class,
        XSDValidationErrorEx.class,
        JSONException.class,
        MultipartException.class,
        DoiClientException.class
    })
    public ApiError unsatisfiedParameterHandler(final Exception exception, final HttpServletRequest request) {
        storeApiErrorCause(exception);

        if (contentTypeNeedsBody(request)) {
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getMessageKey())) {
                ((ILocalizedException) exception).setMessageKey("api.exception.unsatisfiedRequestParameter");
            }
            if (exception instanceof ILocalizedException && StringUtils.isEmpty(((ILocalizedException) exception).getDescriptionKey())) {
                ((ILocalizedException) exception).setDescriptionKey("api.exception.unsatisfiedRequestParameter.description");
            }
            updateExceptionLocale(exception, request);
            return new ApiError("unsatisfied_request_parameter", exception);
        } else {
            return null;
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = {@Content(mediaType = APPLICATION_JSON_VALUE)})
    @ExceptionHandler({
        MissingResourceException.class
    })
    public ApiError missingResourceHandler(final Exception exception) {
        storeApiErrorCause(exception);

        return new ApiError("missing_resource_parameter", exception);
    }

    /**
     * Stores the exception in the request attributes to be processed by the TransactionManager and
     * rollback the transaction.
     *
     * @param exception
     */
    private void storeApiErrorCause(final Exception exception) {
        RequestContextHolder.currentRequestAttributes().setAttribute("exception", exception, RequestAttributes.SCOPE_REQUEST);
    }
}
