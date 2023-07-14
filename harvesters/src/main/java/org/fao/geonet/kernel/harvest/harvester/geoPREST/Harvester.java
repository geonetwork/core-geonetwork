//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geoPREST;

import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.exceptions.BadSoapResponseEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


//=============================================================================

class Harvester implements IHarvester<HarvestResult> {
    private final AtomicBoolean cancelMonitor;
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------
    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    private Logger log;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    private GeoPRESTParams params;

    //---------------------------------------------------------------------------
    private ServiceContext context;

    //---------------------------------------------------------------------------
    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();

    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, GeoPRESTParams params) {

        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;

    }

    public HarvestResult harvest(Logger log) throws Exception {

        this.log = log;
        //--- perform all searches

        XmlRequest request = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(params.baseUrl + "/rest/find/document"));

        Set<RecordInfo> records = new HashSet<RecordInfo>();

        for (Search s : params.getSearches()) {
            if (cancelMonitor.get()) {
                return new HarvestResult();
            }

            try {
                records.addAll(search(request, s));
            } catch (Exception t) {
                log.error("Unknown error trying to harvest");
                log.error(t.getMessage());
                log.error(t);
                errors.add(new HarvestError(context, t));
            } catch (Throwable t) {
                log.fatal("Something unknown and terrible happened while harvesting");
                log.fatal(t.getMessage());
                log.error(t);
                errors.add(new HarvestError(context, t));
            }
        }

        if (params.isSearchEmpty()) {
            try {
                log.debug("Doing an empty search");
                records.addAll(search(request, Search.createEmptySearch()));
            } catch (Exception t) {
                log.error("Unknown error trying to harvest");
                log.error(t.getMessage());
                log.error(t);
                errors.add(new HarvestError(context, t));
            } catch(Throwable t) {
                log.fatal("Something unknown and terrible happened while harvesting");
                log.fatal(t.getMessage());
                log.error(t);
                errors.add(new HarvestError(context, t));
            }
        }

        log.info("Total records processed in all searches :" + records.size());

        //--- align local node

        Aligner aligner = new Aligner(cancelMonitor, log, context, params);

        return aligner.align(records, errors);
    }

    /**
     * Does REST search request.
     */
    private Set<RecordInfo> search(XmlRequest request, Search s) throws Exception {
        request.clearParams();

        request.addParam("searchText", s.freeText);
        request.addParam("max", params.maxResults);
        Element response = doSearch(request);

        Set<RecordInfo> records = new HashSet<RecordInfo>();

        if (log.isDebugEnabled())
            log.debug("Number of child elements in response: " + response.getChildren().size());

        String rss = response.getName();
        if (!rss.equals("rss")) {
            throw new OperationAbortedEx("Missing 'rss' element in\n", Xml.getString(response));
        }

        Element channel = response.getChild("channel");
        if (channel == null) {
            throw new OperationAbortedEx("Missing 'channel' element in \n", Xml.getString(response));
        }

        @SuppressWarnings("unchecked")
        List<Element> list = channel.getChildren();

        for (Element record : list) {
            if (cancelMonitor.get()) {
                return Collections.emptySet();
            }

            if (!record.getName().equals("item")) continue; // skip all the other crap
            RecordInfo recInfo = getRecordInfo((Element) record.clone());
            if (recInfo != null) records.add(recInfo);
        }

        log.info("Records added to result list : " + records.size());

        return records;
    }

    private Element doSearch(XmlRequest request) throws OperationAbortedEx {
        try {
            log.info("Searching on : " + params.getName());
            Element response = request.execute();
            if (log.isDebugEnabled()) {
                log.debug("Sent request " + request.getSentData());
                log.debug("Search results:\n" + Xml.getString(response));
            }
            return response;
        } catch (BadSoapResponseEx e) {
            errors.add(new HarvestError(context, e));
            throw new OperationAbortedEx("Raised exception when searching: "
                + e.getMessage(), e);
        } catch (BadXmlResponseEx e) {
            errors.add(new HarvestError(context, e));
            throw new OperationAbortedEx("Raised exception when searching: "
                + e.getMessage(), e);
        } catch (IOException e) {
            errors.add(new HarvestError(context, e));
            throw new OperationAbortedEx("Raised exception when searching: "
                + e.getMessage(), e);
        }
    }

    private RecordInfo getRecordInfo(Element record) {
        if (log.isDebugEnabled()) log.debug("getRecordInfo : " + Xml.getString(record));

        String identif = "";

        // get uuid and date modified
        try {
            // uuid is in <guid> child
            String guidLink = record.getChildText("guid");
            if (guidLink != null) {
                guidLink = URLDecoder.decode(guidLink, Constants.ENCODING);
                identif = StringUtils.substringAfter(guidLink, "id=");
            }
            if (identif.length() == 0) {
                log.warning("Record doesn't have a uuid : " + Xml.getString(record));
                return null; // skip this one
            }

            String modified = record.getChildText("pubDate");
            // convert the pubDate to a known format (ISOdate)
            Date modDate = parseDate(modified);
            modified = new ISODate(modDate.getTime(), false).toString();
            if (modified != null && modified.length() == 0) modified = null;

            if (log.isDebugEnabled())
                log.debug("getRecordInfo: adding " + identif + " with modification date " + modified);
            return new RecordInfo(identif, modified);
        } catch (UnsupportedEncodingException e) {
            HarvestError harvestError = new HarvestError(context, e);
            harvestError.setDescription(harvestError.getDescription() + "\n record: " + Xml.getString(record));
            errors.add(harvestError);
        } catch (ParseException e) {
            HarvestError harvestError = new HarvestError(context, e);
            harvestError.setDescription(harvestError.getDescription() + "\n record: " + Xml.getString(record));
            errors.add(new HarvestError(context, e));
        }

        // we get here if we couldn't get the UUID or date modified
        return null;

    }

    /**
     * Parse the date provided in the pubDate field.
     *
     * The field may be formatted according to different languages: e.g.
     * <ul>
     * <li>"Mon, 04 Feb 2013 10:19:00 +1000"</li>
     * <li>"Fr, 24 Mrz 2017 10:58:59 +0100"</li>
     * </ul>
     *
     * This method also provides a workaround for
     *    https://bugs.openjdk.java.net/browse/JDK-8136539
     *
     * @param pubDate the date to parse
     * @return
     */
    protected Date parseDate(String pubDate) throws ParseException {
        Locale[] wellKnownLocales = {Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale.ITALIAN};

        for (Locale locale : wellKnownLocales) {
            DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(locale);

            try {
                ZonedDateTime date = ZonedDateTime.parse(pubDate, formatter);
                return Date.from(date.toInstant());
            } catch (DateTimeParseException e) {
                // workaround for https://bugs.openjdk.java.net/browse/JDK-8136539
                if(locale == Locale.GERMAN && (pubDate.toLowerCase(Locale.GERMAN).contains("mrz")
                    || pubDate.toLowerCase(Locale.GERMAN).contains("mär"))) {
                    try {
                        log.info("Applying MRZ workaround to '" + pubDate + "'");
                        String wad = pubDate.toLowerCase(Locale.GERMAN).replace("mrz", "mar");
                        wad = wad.replace("mär", "mar");
                        ZonedDateTime workedAroundDate = ZonedDateTime.parse(wad, formatter);
                        return Date.from(workedAroundDate.toInstant());
                    } catch (DateTimeParseException ex) {}
                }
                log.debug("Date '" + pubDate + "' is not parsable according to " + locale);
            }
        }

        throw new ParseException("Can't parse date '" + pubDate + "'", 0);
    }

    public List<HarvestError> getErrors() {
        return errors;
    }
}

// =============================================================================

