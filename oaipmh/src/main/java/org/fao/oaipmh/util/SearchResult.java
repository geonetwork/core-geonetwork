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

package org.fao.oaipmh.util;

import java.util.List;

import org.fao.oaipmh.exceptions.BadResumptionTokenException;

//=============================================================================

public class SearchResult {
    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    public String prefix;
    private List<Integer> ids;

    private String token;


    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public SearchResult(String prefix) {
        this.prefix = prefix;
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public int parseToken(String token) throws BadResumptionTokenException {
        if (!Lib.isInteger(token))
            throw new BadResumptionTokenException("Invalid token : " + token);

        if (ids == null) {
            throw new IllegalStateException("res.ids should not be null");
        }
        int pos = Integer.parseInt(token);

        if (pos >= ids.size())
            throw new BadResumptionTokenException("Token beyond limit : " + token);

        this.token = token;

        return pos;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the ids
     */
    public List<Integer> getIds() {
        return ids;
    }

    /**
     * @param ids the ids to set
     */
    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    /**
     * @param ids the ids to add
     */
    public void addIds(List<Integer> ids) {
        if (this.ids == null) this.ids = ids;
        else this.ids.addAll(ids);
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }


    //---------------------------------------------------------------------------

/*
    public void setupToken(ListResponse res, int pos)
	{
		if (pos < ids.size())
			res.setResumptionToken(new ResumptionToken(Integer.toString(pos)));
		else
		{
			//--- in case of a last chunk, we return an empty token

			if (token != null)
				res.setResumptionToken(new ResumptionToken(""));
		}
	}
*/

}

//=============================================================================

