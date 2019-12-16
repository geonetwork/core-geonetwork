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

package org.fao.geonet.api.records.formatters.groovy;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.jdom.Element;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Map;

/**
 * Contains methods for accessing data regarding the current user and request, such as the current
 * UI language, username, the parameters passed to the format request, etc...
 *
 * <strong>IMPORTANT:</strong> This class cannot be used while registering handlers, sorters, etc...
 * Only when the XML processing is under way can it be used.  If used during setup of the handlers
 * then an exception will be thrown.  The reason this object is bound to the script is so that it
 * can be used within any handler and object created can be passed the object for use without having
 * to do any crazy gymnastics.
 *
 * @author Jesse on 10/20/2014.
 */
public interface Environment extends CurrentLanguageHolder {
    /**
     * Return the id of the metadata
     */
    public int getMetadataId();

    /**
     * Return the UUID of the metadata
     */
    public String getMetadataUUID();

    /**
     * The URL to use in the html when accessing resources in the formatter resource package. Simply
     * append the relative path of the resource from the formatter bundle directory.
     */
    public String getResourceUrl();

    /**
     * The url to use when making a service call.  This string will contain the full url up to but
     * not including the servive for example: http://localhost:8080/geonetwork/srv/eng/
     */
    public String getLocalizedUrl();

    /**
     * Get the current user's authentication information.
     */
    public Authentication getAuth();

    /**
     * Get the Format output type of this request. (html, xml, pdf, etc...)
     */
    public FormatType getFormatType();

    /**
     * Get the approximate size of the element that the formatter output will be embedded in
     * compared to the full device width.  Allowed options are the enum values: {@link
     * org.fao.geonet.api.records.formatters.FormatterWidth}
     */
    public FormatterWidth getEmbeddingWidth();

    /**
     * Return the map of all parameters passed to the Format service.
     */
    public Multimap<String, ParamValue> params();

    /**
     * Return the value of the first parameter with the provided name.  Null is returned if there is
     * no parameter with the given name.
     */
    public ParamValue param(String paramName);

    /**
     * Return ALL values of parameter with the provided name.
     */
    public Collection<ParamValue> paramValues(String paramName);

    /**
     * Get the request headers for the given name.
     *
     * @param name the name of the header to get
     * @return the request headers for the given name.
     */
    public Collection<String> getHeaders(String name);

    /**
     * Get the request header for the given name.
     *
     * @param name the name of the header to get
     * @return the request header for the given name.
     */
    public Optional<String> getHeader(String name);

    /**
     * Return the metadata document as a jdom Element.  json is the XML API used typically in
     * geonetwork.  This is useful when one needs to call back into the Geonetwork classes.
     */
    public Element getMetadataElement();

    /**
     * Returns the data in the index for the current metadata.
     */
    public Map<String, Collection<String>> getIndexInfo() throws Exception;

    /**
     * Get the service context
     * @return the context
     */
    public ServiceContext getContext();

    /**
     * Get a Spring Bean from Spring Application Context.
     *
     * @param clazz the type of bean to look up.
     */
    public <T> T getBean(Class<T> clazz);

    /**
     * Get a Spring Bean from Spring Application Context.
     *
     * @param name the name.
     * @param clazz the type of bean to look up.
     */
    public <T> T getBean(String name, Class<T> clazz);

    /**
     * Retrieves the background, width and mapproj parameters from settings and puts them into the
     * map config.
     */
    public MapConfig getMapConfiguration();

    /**
     * Return true is the current user has edit permissions for the current metadata
     */
    public boolean canEdit() throws Exception;
}
