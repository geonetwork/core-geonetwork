//=============================================================================
//===  Copyright (C) 2009 World Meteorological Organization
//===  This program is free software; you can redistribute it and/or modify
//===  it under the terms of the GNU General Public License as published by
//===  the Free Software Foundation; either version 2 of the License, or (at
//===  your option) any later version.
//===
//===  This program is distributed in the hope that it will be useful, but
//===  WITHOUT ANY WARRANTY; without even the implied warranty of
//===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===  General Public License for more details.
//===
//===  You should have received a copy of the GNU General Public License
//===  along with this program; if not, write to the Free Software
//===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===  Contact: Timo Proescholdt
//===  email: tproescholdt_at_wmo.int
//==============================================================================

package org.fao.geonet.services.util.z3950.provider.GN;

import org.fao.geonet.utils.Log;
import org.fao.geonet.ContextContainer;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.services.util.z3950.GNXMLQuery;
import org.jzkit.search.provider.iface.IRQuery;
import org.jzkit.search.provider.iface.Searchable;
import org.jzkit.search.util.ResultSet.IRResultSet;
import org.jzkit.search.util.ResultSet.IRResultSetStatus;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Observer;

//import org.fao.geonet.services.util.z3950.GNSearchTask;

/**
 * @author 'Timo Proescholdt <tproescholdt@wmo.int>' interface between JZkit and GN. not currently
 *         used
 */
public class GNSearchable implements Searchable {

    @SuppressWarnings("rawtypes")
    private Map recordArchetypes;
    private int timeout;

    private ApplicationContext ctx;

    public GNSearchable() {
        if (Log.isDebugEnabled(Geonet.SRU))
            Log.debug(Geonet.SRU, "creating GNSearchable");
    }

    public void close() {

    }

    public void setTimeout(int i) {
        this.timeout = i;
    }

    public IRResultSet evaluate(IRQuery q) {

        return this.evaluate(q, null, null);

    }

    public IRResultSet evaluate(IRQuery q, Object userInfo) {
        return this.evaluate(q, userInfo, null);
    }

    public IRResultSet evaluate(IRQuery q, Object userInfo, Observer[] observers) {

        if (Log.isDebugEnabled(Geonet.SRU))
            Log.debug(Geonet.SRU, "evaluating...");

        ContextContainer cnt = (ContextContainer) ctx.getBean("ContextGateway");

        GNResultSet result = null;

        try {
            result = new GNResultSet(new GNXMLQuery(q, ctx), userInfo, observers, cnt.getSrvctx()); // SRUResultSet(observers, base_url,
            // getCQLString(q), code);
            result.evaluate(timeout);
            result.setStatus(IRResultSetStatus.COMPLETE);
        } catch (Exception e) {
            Log.error(Geonet.SRU, e.getMessage(), e);
            if (result != null)
                result.setStatus(IRResultSetStatus.FAILURE);
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    public Map getRecordArchetypes() {
        return this.recordArchetypes;
    }

    public void setRecordArchetypes(@SuppressWarnings("rawtypes")
                                        Map recordSyntaxArchetypes) {
        this.recordArchetypes = recordSyntaxArchetypes;

    }

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.ctx = ctx;

    }

}
