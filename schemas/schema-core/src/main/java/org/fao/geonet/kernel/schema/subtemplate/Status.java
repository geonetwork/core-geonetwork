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

package org.fao.geonet.kernel.schema.subtemplate;

import java.util.stream.Collector;

public class Status {
    protected String msg = "";
    private boolean error = false;

    public Status() {
    }

    public boolean isError() {
        return error;
    }

    public Status add(Status status) {
        if (status.isError()) {
            msg = msg + "|" + status.msg;
            error = true;
        }
        return this;
    }

    public String getMsg() {
        return this.msg;
    }

    static public class Failure extends Status {

        public Failure(String msg) {
            this.msg = msg;
        }

        public boolean isError() {
            return true;
        }
    }

    public static Collector<Status, Status, Status> STATUS_COLLECTOR = Collector.of(
            () -> new Status(),
            (result, status) -> result.add(status),
            (st1, st2) -> {return st1.add(st2);});
}