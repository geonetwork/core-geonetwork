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

package org.fao.geonet.kernel.harvest.harvester;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import org.apache.hc.core5.http.HttpException;
import org.apache.commons.jcs3.access.exception.CacheException;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.exceptions.*;
import org.jdom.JDOMException;

import jeeves.server.context.ServiceContext;
import org.slf4j.LoggerFactory;

/**
 * <p> Master class for harvesting errors. </p> <p> Example of usage: </p> <code> SQLException e =
 * ...; HarvestError harvestError = new HarvestError(e, log); harvestError.setDescription("Error
 * updating the source logos from " + params.name + ": \n" + e.getSQLState());
 * harvestError.setHint("Check the original resource (" + sourceUuid + ") is correctly defined.");
 * this.errors.add(harvestError); </code>
 *
 * @author delawen
 */
public class HarvestError extends AbstractHarvestError {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Geonet.HARVESTER);

    /**
     * Exception that caused the harvest error.
     */
    private Throwable origin = null;

    /**
     * Description of the error.
     */
    private String description = null;

    /**
     * If there is any hint on how to solve the error, this is it.
     *
     * Example: check the username and password
     */
    private String hint = "Check with your administrator the error.";

    public HarvestError(ServiceContext context, InvalidParameterValueEx ex) {
    	super(context);

        this.origin = ex;
        this.description = "The server didn't accept one of the parameters"
            + " sent on the request.\n The parameter rejected was: "
            + ex.getLocator() + " (" + ex.getMessage() + ")";

        this.hint = "Check that geonetwork supports the exact version of"
            + " the server you are trying to connect";
        printLog();
    }

    public HarvestError(ServiceContext context, Throwable ex) {
        super(context);

        this.origin = ex;
        this.description = ex.getMessage();
        if (this.description == null || this.description.isEmpty()) {
            this.description = ex.getClass().toString();
        }
        // Do not print log, as it is a very generic exception
        // leave it to main caller
    }
    public HarvestError(ServiceContext context, CacheException ex) {
        super(context);

        this.origin = ex;
        this.description = "Failed to update Jeeves cache: " + ex.getMessage();
        printLog();
    }

    public HarvestError(ServiceContext context, JDOMException ex) {
        super(context);

        this.origin = ex;
        this.description = "There was an error processing the response. " + ex.getMessage();
        printLog();
    }

    public HarvestError(ServiceContext context, BadServerResponseEx e) {
        super(context);

        this.origin = e;
        this.description = "The server returned an answer that could not be processed: "
            + e.getObject();
        this.hint = "Check the harvester is correctly configured.";
        printLog();
    }

    public HarvestError(ServiceContext context, BadSoapResponseEx e) {
        super(context);

        this.origin = e;
        this.description = "The server returned an answer that could not be processed.";
        this.hint = "Check the harvester is correctly configured.";
        printLog();
    }

    public HarvestError(ServiceContext context, HttpException e) {
        super(context);

        this.origin = e;
        this.description = "There was an error trying to reach some URL. " + e.getMessage();
        printLog();
    }

    public HarvestError(ServiceContext context, IOException e) {
        super(context);

        this.origin = e;
        this.description = "There was an error trying to reach an URI. " + e.getMessage();
        printLog();
    }

    public HarvestError(ServiceContext context, MalformedURLException e) {
        super(context);

        this.origin = e;
        this.description = "There was an error trying to reach an URL. ";
        this.hint = "Check the configuration of the harvest.";
        printLog();
    }

    public HarvestError(ServiceContext context, UnsupportedEncodingException e) {
        super(context);

        this.origin = e;
        this.description = "The metadata is defined on an unsupported encoding: "
            + e.getMessage();
        this.hint = "Check with your administrator the encoding error.";
        printLog();
    }

    public HarvestError(ServiceContext context, java.text.ParseException e) {
        super(context);

        this.origin = e;
        this.description = "Unable to parse the metadata.";
        this.hint = "Check with your administrator about possible "
            + "network errors or corrupt data on harvested server.";
        printLog();
    }

    public HarvestError(ServiceContext context, SQLException e) {
        super(context);

        this.origin = e;
        this.description = "There was an error while updating the database: "
            + e.getSQLState();
        this.hint = "Check with your administrator that the database is not corrupted.";
        printLog();
    }

    public HarvestError(ServiceContext context, UserNotFoundEx e) {
        super(context);

        this.origin = e;
        this.description = "Couldn't log in to harvest using " + e.getObject();
        this.hint = "Check username and password.";
        printLog();
    }

    public HarvestError(ServiceContext context, SchemaMatchConflictException e, String name) {
        super(context);

        this.origin = e;
        this.description = "Couldn't match the schema for " + name;
        this.hint = "Check that the schemas used are defined on geonetwork.";
        printLog();
    }
    public HarvestError(ServiceContext context, SchemaMatchConflictException e) {
        super(context);

        this.origin = e;
        this.description = "Couldn't match the schema.";
        this.hint = "Check that the schemas used are defined on geonetwork.";
        printLog();
    }

    public HarvestError(ServiceContext context, NoSchemaMatchesException e, String name) {
        super(context);

        this.origin = e;
        this.description = "Couldn't recognize the schema for " + name;
        this.hint = "Check that the schemas used are defined on geonetwork.";
        printLog();
    }

    public HarvestError(ServiceContext context, NoSchemaMatchesException e) {
        super(context);

        this.origin = e;
        this.description = "Couldn't recognize the schema.";
        this.hint = "Check that the schemas used are defined on geonetwork.";
        printLog();
    }

    public HarvestError(ServiceContext context, BadXmlResponseEx e, String capabUrl) {
        super(context);
        this.origin = e;
        this.description = "The response returned from the server doesn't look like XML";
        this.hint = "Check the URL is ok: " + capabUrl;
        printLog();
    }

    public HarvestError(ServiceContext context, BadXmlResponseEx e) {
        super(context);
        this.origin = e;
        this.description = "The response returned from the server doesn't look like XML. " + e.getMessage();
        this.hint = "Check the URL is ok.";
        printLog();
    }

    public Throwable getOrigin() {
        return origin;
    }

    public void setOrigin(Throwable origin) {
        this.origin = origin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public String toString() {
        return this.origin.getClass() + "[" + this.description + " -> "
            + this.hint + "]";
    }

    /**
     * Generic print on log of current harvest error
     */
    public void printLog() {
        if (this.description != null) {
            LOGGER.warn(this.description);
        }
        if (this.hint != null) {
            LOGGER.warn(this.hint);
        }
        if (this.origin != null) {
            LOGGER.error(this.origin.toString());
        }
    }
}
