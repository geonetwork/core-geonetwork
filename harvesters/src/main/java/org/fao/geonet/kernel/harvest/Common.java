//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest;

import org.fao.geonet.exceptions.BadParameterEx;

//=============================================================================

public class Common {

    //---------------------------------------------------------------------------
    //--- Status
    //---------------------------------------------------------------------------

    public enum Status {
        ACTIVE("active"), INACTIVE("inactive");

        //------------------------------------------------------------------------

        private String status;

        //------------------------------------------------------------------------

        private Status(String status) {
            this.status = status;
        }

        //------------------------------------------------------------------------

        public static Status parse(String status) throws BadParameterEx {

            for (Status next : values()) {
                if (next.name().equalsIgnoreCase(status)) {
                    return next;
                }
            }

            throw new BadParameterEx("type", status);
        }

        //------------------------------------------------------------------------

        public String toString() {
            return status;
        }
    }

    //---------------------------------------------------------------------------
    //--- Result
    //---------------------------------------------------------------------------

    public enum OperResult {
        OK("ok"),
        NOT_FOUND("not-found"),
        ALREADY_ACTIVE("already-active"),
        ALREADY_INACTIVE("already-inactive"),
        ALREADY_RUNNING("already-running"),
        ERROR("error");

        //------------------------------------------------------------------------

        private String result;

        //------------------------------------------------------------------------

        private OperResult(String result) {
            this.result = result;
        }

        //------------------------------------------------------------------------

        public String toString() {
            return result;
        }
    }
}

//=============================================================================

