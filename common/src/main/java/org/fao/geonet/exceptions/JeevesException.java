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

package org.fao.geonet.exceptions;

import org.fao.geonet.Constants;
import org.jdom.Element;

//=============================================================================

@SuppressWarnings("serial")
public abstract class JeevesException extends RuntimeException {
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    protected String id;

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------
    protected Object object;
    /**
     * this is the error code that will be returned. For HTTP connections, this will be the status
     * code
     */

    protected int code;

    public JeevesException(String message, Object object) {
        super(message, object instanceof Throwable ? (Throwable) object : null);

        this.object = object;
        this.code = -1;
    }

    //--------------------------------------------------------------------------

    public static Element toElement(Throwable t) {
        String msg = t.getMessage();
        String cls = t.getClass().getSimpleName();
        String id = Constants.ERROR;
        Object obj = null;

        if (t instanceof JeevesException) {
            JeevesException je = (JeevesException) t;

            id = je.getId();
            obj = je.getObject();
        }

        Element error = new Element(Constants.ERROR)
            .addContent(new Element("message").setText(msg))
            .addContent(new Element("class").setText(cls))
            .addContent(getStackTrace(t, 10));

        error.setAttribute("id", id);

        if (obj != null) {
            Element elObj = new Element("object");

            if (obj instanceof Element) elObj.addContent(((Element) obj).detach());
            else elObj.setText(obj.toString());

            error.addContent(elObj);
        }

        return error;
    }

    //--------------------------------------------------------------------------

    private static Element getStackTrace(Throwable t, int depth) {
        Element stack = new Element("stack");
        boolean writing = true;
        for (StackTraceElement ste : t.getStackTrace()) {
            String clas = ste.getClassName();
            String file = ste.getFileName();
            String meth = ste.getMethodName();
            String line = Integer.toString(ste.getLineNumber());

            Element at = new Element("at");

            at.setAttribute("class", (clas == null) ? "???" : clas);
            at.setAttribute("file", (file == null) ? "???" : file);
            at.setAttribute("line", line);
            at.setAttribute("method", (meth == null) ? "???" : meth);


            if (--depth >= 0 || (clas != null && (
                clas.startsWith("org.fao") ||
                    clas.startsWith("org.wfp") ||
                    clas.startsWith("jeeves") ||
                    clas.startsWith("org.geonetwork")))) {
                writing = true;
                stack.addContent(at);
            } else if (writing) {
                stack.addContent(new Element("skip").setText("..."));
                writing = false;
            }
        }

        return stack;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //--------------------------------------------------------------------------

    public Object getObject() {
        return object;
    }

    public int getCode() {
        return code;
    }

    public String toString() {
        return getClass().getSimpleName() + " : " + getMessage();
    }
}

//=============================================================================

