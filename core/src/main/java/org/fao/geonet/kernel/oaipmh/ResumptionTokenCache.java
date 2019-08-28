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

package org.fao.geonet.kernel.oaipmh;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.oaipmh.responses.GeonetworkResumptionToken;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ResumptionTokenCache extends Thread {


    public final static int CACHE_EXPUNGE_DELAY = 10 * 1000; // 10 seconds

    private Map<String, GeonetworkResumptionToken> map;
    private static Object stopper = new Object();
    private volatile boolean running = true;
    private SettingManager settingMan;

    /**
     * Constructor
     */
    public ResumptionTokenCache(SettingManager sm) {

        this.settingMan = sm;
        if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER, "OAI cache ::init timout:" + getTimeout());

        map = Collections.synchronizedMap(new HashMap<String, GeonetworkResumptionToken>());

        this.setDaemon(true);
        this.setName("Cached Search Session Expiry Thread");
        this.start();

    }

    private static Date getUTCTime() {
        Date date = new Date();
        TimeZone tz = TimeZone.getDefault();
        Date ret = new Date(date.getTime() - tz.getRawOffset());

        if (tz.inDaylightTime(ret)) {
            Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());

            // check to make sure we have not crossed back into standard time
            // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
            if (tz.inDaylightTime(dstDate)) {
                ret = dstDate;
            }
        }
        return ret;
    }

    /**
     * @return the timeout
     */
    public long getTimeout() {
        return settingMan.getValueAsInt("system/oai/tokentimeout");
    }

    /**
     * @return the cachemaxsize
     */
    public int getCachemaxsize() {
        return settingMan.getValueAsInt("system/oai/cachesize");
    }

    public void run() {
        synchronized (stopper) {
            while (running && !isInterrupted()) {
                try {
                    stopper.wait(CACHE_EXPUNGE_DELAY);
                    if (running) {
                        expunge();
                    }
                } catch (java.lang.InterruptedException ie) {
                    Log.debug(Geonet.OAI_HARVESTER,"OAI execution error: " + ie.getMessage(), ie);
                }
            }
        }
        Log.info(Geonet.OAI_HARVESTER, "ResumptionTokenCache thread end");
    }

    private synchronized void expunge() {

        Date now = getUTCTime();

        for (Map.Entry entry : map.entrySet()) {
            if (((GeonetworkResumptionToken) entry.getValue()).getExpirDate().toDate().getTime() / 1000 < (now.getTime() / 1000)) {
                map.remove(entry.getKey());
                if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
                    Log.debug(Geonet.OAI_HARVESTER, "OAI cache ::expunge removing:" + entry.getKey());
            }
        }
    }

    // remove oldest token from cache
    private void removeLast() {
        if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER, "OAI cache ::removeLast");


        long oldest = Long.MAX_VALUE;
        Object oldkey = "";

        for (Map.Entry entry : map.entrySet()) {

            if (((GeonetworkResumptionToken) entry.getValue()).getExpirDate().getSeconds() < oldest) {
                oldkey = entry.getKey();
                oldest = ((GeonetworkResumptionToken) entry.getValue()).getExpirDate().getSeconds();
            }
        }

        map.remove(oldkey);
        if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER, "OAI cache ::removeLast removing:" + oldkey);


    }

    public synchronized GeonetworkResumptionToken getResumptionToken(String str) {
        return map.get(str);
    }

    public synchronized void storeResumptionToken(GeonetworkResumptionToken resumptionToken) {
        if (Log.isDebugEnabled(Geonet.OAI_HARVESTER))
            Log.debug(Geonet.OAI_HARVESTER, "OAI cache ::store " + resumptionToken.getKey() + " size: " + map.size());

        if (map.size() == getCachemaxsize()) {
            removeLast();
        }

        resumptionToken.setExpirDate(new ISODate(getUTCTime().getTime() + getTimeout() * 1000, false));
        map.put(resumptionToken.getKey(), resumptionToken);
    }

    public void stopRunning() {
        synchronized (stopper) {
            this.running = false;
            stopper.notify();
        }
        try {
            this.join();
            Log.info(Geonet.OAI_HARVESTER, "ResumptionTokenCache thread stopped");
        } catch (InterruptedException ignored) {
        }
    }

}
