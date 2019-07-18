//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.utils;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.fao.geonet.Constants;
import org.fao.geonet.utils.nio.NioPathAwareCatalogResolver;

import java.nio.file.Path;
import java.util.Vector;

//=============================================================================

/**
 * Utility that handles the CatalogResolver and XmlResolver and observes the ProxyInfo subject class
 * to obtain proxy info needed for resolver
 */

public final class Resolver implements ProxyInfoObserver {

    /**
     * Active readers count
     */
    private static int activeReaders = 0;
    /**
     * Active writers count
     */
    private static int activeWriters = 0;
    private ProxyInfo proxyInfo;
    private XmlResolver xmlResolver;
    private CatalogResolver catResolver;
    private Path oasisCatalogPath;
    /**
     * When path is resolved to a non existing file, return this file.
     */
    private String blankXSLFile;

    //--------------------------------------------------------------------------

    public Resolver() {
        this.oasisCatalogPath = null;
        beforeWrite();
        try {
            setUpXmlResolver();
        } finally {
            afterWrite();
        }
    }

    public Resolver(Path oasisCatalogPath) {
        this.oasisCatalogPath = oasisCatalogPath;
        beforeWrite();
        try {
            setUpXmlResolver();
        } finally {
            afterWrite();
        }
    }

    //--------------------------------------------------------------------------

    private void setUpXmlResolver() {
        CatalogManager catMan = new CatalogManager();
        catMan.setAllowOasisXMLCatalogPI(false);
        catMan.setCatalogClassName("org.apache.xml.resolver.Catalog");
        String catFiles = null;
        if(this.oasisCatalogPath == null) {
            catFiles = System.getProperty(Constants.XML_CATALOG_FILES);
            if (catFiles == null) catFiles = "";
        } else {
            catFiles = this.oasisCatalogPath.toString();
        }
        if (Log.isDebugEnabled(Log.JEEVES))
            Log.debug(Log.JEEVES, "Using oasis catalog files " + catFiles);

        setBlankXSLFile(System.getProperty(Constants.XML_CATALOG_BLANKXSLFILE));

        catMan.setCatalogFiles(catFiles);
        catMan.setIgnoreMissingProperties(true);
        catMan.setPreferPublic(true);
        catMan.setRelativeCatalogs(false);
        catMan.setUseStaticCatalog(false);
        String catVerbosity = System.getProperty(Constants.XML_CATALOG_VERBOSITY);
        if (catVerbosity == null) catVerbosity = "1";
        int iCatVerb = 1;
        try {
            iCatVerb = Integer.parseInt(catVerbosity);
        } catch (NumberFormatException nfe) {
            Log.error(Log.JEEVES, "Failed to parse " + Constants.XML_CATALOG_VERBOSITY + " " + catVerbosity, nfe);
        }
        if (Log.isDebugEnabled(Log.JEEVES))
            Log.debug(Log.JEEVES, "Using catalog resolver verbosity " + iCatVerb);
        catMan.setVerbosity(iCatVerb);

        catResolver = new NioPathAwareCatalogResolver(catMan);

        @SuppressWarnings("unchecked")
        Vector<String> catalogs = catResolver.getCatalog().getCatalogManager().getCatalogFiles();
        String[] cats = new String[catalogs.size()];
        System.arraycopy(catalogs.toArray(), 0, cats, 0, catalogs.size());

        if (proxyInfo == null) proxyInfo = new ProxyInfo();
        ProxyParams proxyParams = proxyInfo.getProxyParams();
        xmlResolver = new XmlResolver(cats, proxyParams);
    }

    //--------------------------------------------------------------------------

    public void reset() {
        beforeWrite();
        try {
            setUpXmlResolver();
        } finally {
            afterWrite();
        }
    }

    //--------------------------------------------------------------------------

    public XmlResolver getXmlResolver() {
        beforeRead();
        try {
            return xmlResolver;
        } finally {
            afterRead();
        }
    }

    //--------------------------------------------------------------------------

    public CatalogResolver getCatalogResolver() {
        beforeRead();
        try {
            return catResolver;
        } finally {
            afterRead();
        }
    }

    //--------------------------------------------------------------------------

    public void update(ProxyInfo proxyInfo) {
        beforeWrite();
        try {
            this.proxyInfo = proxyInfo;
            proxyInfo.getProxyParams();  // call to initialize
            setUpXmlResolver();
        } finally {
            afterWrite();
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

    public String getBlankXSLFile() {
        return blankXSLFile;
    }

    public void setBlankXSLFile(String blankXSLFile) {
        this.blankXSLFile = blankXSLFile;
    }

}

//=============================================================================

