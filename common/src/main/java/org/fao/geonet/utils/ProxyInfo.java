//==============================================================================
//===
//=== ProxyInfo
//===
//==============================================================================
//===	Copyright (C) GeoNetwork
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

package org.fao.geonet.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Subject for observers (Jeeves classes) that need proxy info from GeoNetwork
 */
public class ProxyInfo {

    /**
     * Active readers count
     */
    private static int activeReaders = 0;
    /**
     * Active writers count
     */
    private static int activeWriters = 0;
    private final ProxyParams proxyParams = new ProxyParams();
    private List<ProxyInfoObserver> observers = new ArrayList<ProxyInfoObserver>();

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public ProxyInfo() {
    }

    //---------------------------------------------------------------------------

    public void addObserver(ProxyInfoObserver o) {
        observers.add(o);
    }

    //---------------------------------------------------------------------------

    public void removeObserver(ProxyInfoObserver o) {
        observers.remove(o);
    }

    //---------------------------------------------------------------------------

    public void setProxyInfo(String host, int port, String username, String password) {

        beforeWrite();
        proxyParams.useProxy = false;
        if (host != null) {
            if (host.trim().length() != 0 && port != 0) {
                proxyParams.proxyHost = host;
                proxyParams.proxyPort = port;
                proxyParams.useProxy = true;

                proxyParams.useProxyAuth = false;
                if (username != null) {
                    if (username.trim().length() != 0) {
                        proxyParams.username = username;
                        proxyParams.password = password;
                        proxyParams.useProxyAuth = true;
                    }
                }
            }
        }
        afterWrite();

        beforeRead();
        notifyAllObservers();
        afterRead();
    }

    //---------------------------------------------------------------------------

    public void notifyAllObservers() {
        // -- notify all observers that proxy info has changed

        Iterator<ProxyInfoObserver> i = observers.iterator();
        while (i.hasNext()) {
            ProxyInfoObserver o = (ProxyInfoObserver) i.next();
            o.update(this);
        }

    }

    //---------------------------------------------------------------------------

    public ProxyParams getProxyParams() {

        beforeRead();
        try {
            return proxyParams;
        } finally {
            afterRead();
        }
    }

    //--------------------------------------------------------------------------
    // -- Private methods
    //--------------------------------------------------------------------------

    /**
     * Invoked just before reading, waits until reading is allowed.
     */
    private synchronized void beforeRead() {
        while (activeWriters > 0) {
            try {
                wait();
            } catch (InterruptedException iex) {
            }
        }
        ++activeReaders;
    }

    //--------------------------------------------------------------------------

    /**
     * Invoked just after reading.
     */
    private synchronized void afterRead() {
        --activeReaders;
        notifyAll();
    }

    //--------------------------------------------------------------------------

    /**
     * Invoked just before writing, waits until writing is allowed.
     */
    private synchronized void beforeWrite() {
        while (activeReaders > 0 || activeWriters > 0) {
            try {
                wait();
            } catch (InterruptedException iex) {
            }
        }
        ++activeWriters;
    }

    //--------------------------------------------------------------------------

    /**
     * Invoked just after writing.
     */
    private synchronized void afterWrite() {
        --activeWriters;
        notifyAll();
    }

}

//=============================================================================

