//=============================================================================
//===    Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

/**
 * BaseGeonetParams is an abstract class that extends {@link AbstractParams} and provides
 * a framework for managing parameters related to GeoNetwork harvesting and configuration.
 * This class supports the handling of searches, group copy policies, and various
 * configuration options for GeoNetwork-based harvesting processes.
 *
 * @param <S> A type parameter extending BaseSearch, representing the search-related configuration.
 */
public abstract class BaseGeonetParams<S extends BaseSearch> extends AbstractParams {

    public String host;
    public boolean createRemoteCategory;
    public boolean mefFormatFull;

    /**
     * The filter is a process (see schema/process folder) which depends on the schema. It could be
     * composed of parameter which will be sent to XSL transformation using the following syntax :
     * <pre>
     * anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
     * </pre>
     */
    public String xslfilter;

    protected String node;
    protected Boolean useChangeDateForUpdate;
    protected ArrayList<S> alSearches = new ArrayList<>();
    protected ArrayList<Group> alCopyPolicy = new ArrayList<>();

    public BaseGeonetParams(DataManager dm) {
        super(dm);
    }

    @Override
    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element policy = node.getChild("groupsCopyPolicy");
        Element searches = node.getChild("searches");

        host = Util.getParam(site, "host", "");

        this.setNode(Util.getParam(site, "node", "srv"));
        this.setUseChangeDateForUpdate(Util.getParam(site, "useChangeDateForUpdate", false));

        createRemoteCategory = Util.getParam(site, "createRemoteCategory", false);
        mefFormatFull = Util.getParam(site, "mefFormatFull", false);
        xslfilter = Util.getParam(site, "xslfilter", "");

        addSearches(searches);
        addCopyPolicy(policy);
    }

    @Override
    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element searches = node.getChild("searches");
        Element policy = node.getChild("groupsCopyPolicy");

        host = Util.getParam(site, "host", host);
        this.setNode(Util.getParam(site, "node", this.getNode()));
        this.setUseChangeDateForUpdate(Util.getParam(site, "useChangeDateForUpdate", false));
        createRemoteCategory = Util.getParam(site, "createRemoteCategory", createRemoteCategory);
        mefFormatFull = Util.getParam(site, "mefFormatFull", mefFormatFull);
        xslfilter = Util.getParam(site, "xslfilter", "");


        //--- if some search queries are given, we drop the previous ones and
        //--- set these new ones
        if (searches != null)
            addSearches(searches);

        if (policy != null)
            addCopyPolicy(policy);
    }

    public Iterable<S> getSearches() {
        return alSearches;
    }

    public Iterable<Group> getGroupCopyPolicy() {
        return alCopyPolicy;
    }

    public String getServletPath() {
        if (StringUtils.isNotEmpty(host)) {
            try {
                return new URL(host).getPath();
            } catch (MalformedURLException ex) {
                Log.error(Geonet.HARVEST_MAN, ex.getMessage(), ex);
            }
        }

        return "";
    }

    public boolean isSearchEmpty() {
        return alSearches.isEmpty();
    }

    public abstract BaseGeonetParams<S> copy();

    public String getNode() {
        if (this.node == null) {
            //default node
            this.setNode("srv");
        }
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public boolean useChangeDateForUpdate() {
        if (this.useChangeDateForUpdate == null) {
            this.setUseChangeDateForUpdate(false);
        }
        return useChangeDateForUpdate;
    }

    public void setUseChangeDateForUpdate(Boolean useChangeDateForUpdate) {
        if (useChangeDateForUpdate == null) {
            useChangeDateForUpdate = false;
        }
        this.useChangeDateForUpdate = useChangeDateForUpdate;
    }

    @Override
    public String getIcon() {
        return null;
    }


    protected abstract void addSearches(Element searches) throws BadInputEx;

    protected void addCopyPolicy(Element policy) throws BadInputEx {
        alCopyPolicy.clear();

        if (policy == null)
            return;

        for (Object o : policy.getChildren("group")) {
            Element group = (Element) o;

            alCopyPolicy.add(new Group(group));
        }
    }
}
