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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.fao.oaipmh.OaiPmh;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.fao.oaipmh.requests.ListRequest;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

//=============================================================================

public abstract class ListResponse extends AbstractResponse {
    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    private ListRequest listReq;
    private ResumptionToken token;
    private Iterator<Element> iterator;

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public ListResponse() {
    }

    //---------------------------------------------------------------------------

    public ListResponse(ListRequest lr, Element response) {
        super(response);

        listReq = lr;
        build(response);
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public boolean hasNext() {
        if (iterator.hasNext())
            return true;

        if (token != null && !token.isTokenEmpty())
            return true;

        return false;
    }

    //---------------------------------------------------------------------------

    public Object next() throws IOException, OaiPmhException, JDOMException, SAXException, Exception {
        if (iterator.hasNext())
            return createObject(iterator.next());

        if (token == null || token.isTokenEmpty())
            throw new RuntimeException("Iterator exausted");

        build(listReq.resume(token));

        //--- just to avoid problems...
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }

        return createObject(iterator.next());
    }

    public abstract int getSize();

    //---------------------------------------------------------------------------

    public ResumptionToken getResumptionToken() {
        return token;
    }

    //---------------------------------------------------------------------------

    public void setResumptionToken(ResumptionToken token) {
        this.token = token;
    }


    //---------------------------------------------------------------------------
    //---
    //--- Protected methods
    //---
    //---------------------------------------------------------------------------

    protected abstract Object createObject(Element object);

    protected abstract String getListElementName();

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void build(Element response) {
        Element operElem = response.getChild(listReq.getVerb(), OaiPmh.Namespaces.OAI_PMH);
        Element resToken = operElem.getChild("resumptionToken", OaiPmh.Namespaces.OAI_PMH);

        token = (resToken == null) ? null : new ResumptionToken(resToken);
        iterator = operElem.getChildren(getListElementName(), OaiPmh.Namespaces.OAI_PMH).iterator();
    }

}

//=============================================================================

