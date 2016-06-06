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

package org.fao.oaipmh.responses;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.fao.geonet.domain.ISODate;
import org.fao.oaipmh.util.SearchResult;
import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.exceptions.BadResumptionTokenException;
import org.fao.oaipmh.requests.TokenListRequest;
import org.jdom.Element;

//=============================================================================


public class GeonetworkResumptionToken extends ResumptionToken {

    public static final String SEPARATOR = "/-/";
    private Integer listSize;
    private Integer cursor;
    private Integer pos;
    private String set = "";
    private String from = "";
    private String until = "";
    private String prefix = "";
    private Boolean isReset = false;
    private String randomid;
    private SearchResult res;
    private SecureRandom random = new SecureRandom();

    /**
     * Default constructor. Builds a GeonetworkResumptionToken.
     */
    public GeonetworkResumptionToken(Element rt) {
        build(rt);
    }

    /**
     * Default constructor. Builds a GeonetworkResumptionToken.
     */
    public GeonetworkResumptionToken(TokenListRequest req) throws BadResumptionTokenException {

        String strToken = req.getResumptionToken();

        if (strToken == null) {

            if (req.getFrom() != null)
                from = req.getFrom().toString();
            if (req.getUntil() != null)
                until = req.getUntil().toString();
            if (req.getSet() != null)
                set = req.getSet();
            prefix = req.getMetadataPrefix();


            randomid = generateRandomString();

        } else {

            parseToken(strToken);
        }
    }

    /**
     * Default constructor. Builds a GeonetworkResumptionToken.
     */
    public GeonetworkResumptionToken(TokenListRequest req, SearchResult res) throws BadResumptionTokenException {
        this(req);
        this.res = res;
    }

    public static String buildKey(TokenListRequest req) throws BadResumptionTokenException {
        GeonetworkResumptionToken temp = new GeonetworkResumptionToken(req);
        return temp.getKey();
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static int getPos(TokenListRequest req) throws BadResumptionTokenException {
        GeonetworkResumptionToken temp = new GeonetworkResumptionToken(req);
        return temp.getPos();
    }

    public String getToken() {
        if (isReset)
            return ""; // we are at the last chunk
        return getKey() + SEPARATOR + pos;
    }

    public void setToken(String token) {
        try {
            parseToken(token);
        } catch (BadResumptionTokenException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean isTokenEmpty() {
        return isReset;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getKey() {
        return set + SEPARATOR + prefix + SEPARATOR + from + SEPARATOR + until
            + SEPARATOR + randomid;
    }

    public SearchResult getRes() {
        return res;
    }

    public void setRes(SearchResult res) {
        this.res = res;
    }

    public void reset() {
        isReset = true;
    }

    //---------------------------------------------------------------------------

    public void setupToken(int newpos) {
        if (newpos < res.getIds().size()) // update token so that it refers to the next chunk
            setPos(newpos);
        else {
            reset();    // reset token to indicate last chunk
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public Element toXml() {
        Element root = new Element("resumptionToken", OaiPmh.Namespaces.OAI_PMH);

        root.setText(getToken());

        if (getExpirDate() != null)
            root.setAttribute("expirationDate", getExpirDate().toString());

        if (listSize != null)
            root.setAttribute("completeListSize", listSize.toString());

        if (cursor != null)
            root.setAttribute("cursor", cursor.toString());

        return root;
    }

    private void build(Element rt) {
        try {
            parseToken(rt.getText());
        } catch (BadResumptionTokenException e) {
            throw new RuntimeException(e);
        }

        String expDt = rt.getAttributeValue("expirationDate");
        String listSz = rt.getAttributeValue("completeListSize");
        String curs = rt.getAttributeValue("cursor");

        setExpirDate((expDt == null) ? null : new ISODate(expDt));
        listSize = (listSz == null) ? null : Integer.valueOf(listSz);
        cursor = (curs == null) ? null : Integer.valueOf(curs);
    }

    private void parseToken(String strToken) throws BadResumptionTokenException {

        String[] temp = strToken.split(SEPARATOR);

        if (temp.length != 6)
            throw new BadResumptionTokenException("unknown resumptionToken format: " + strToken);

        set = temp[0];
        prefix = temp[1];
        from = temp[2];
        until = temp[3];
        randomid = temp[4];

        pos = Integer.parseInt(temp[5]);
    }

    public String generateRandomString() {
        return new BigInteger(130, random).toString(36);
    }

}

//=============================================================================
