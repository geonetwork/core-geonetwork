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

package org.fao.geonet.csw.common.exceptions;

import org.fao.geonet.utils.Xml;
import org.fao.geonet.csw.common.Csw;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.List;

//=============================================================================

public abstract class CatalogException extends Exception {

    protected static final String INVALID_PARAMETER_VALUE = "InvalidParameterValue";
    protected static final String INVALID_UPDATE_SEQUENCE = "InvalidUpdateSequence";
    protected static final String MISSING_PARAMETER_VALUE = "MissingParameterValue";
    protected static final String NO_APPLICABLE_CODE = "NoApplicableCode";
    protected static final String OPERATION_NOT_SUPPORTED = "OperationNotSupported";
    protected static final String VERSION_NEGOTIATION_FAILED = "VersionNegotiationFailed";
    /**
     *
     */
    private static final long serialVersionUID = -2483411203445474288L;

    //---------------------------------------------------------------------------
    private String code;
    private String locator;

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public CatalogException(String code, String message, String locator) {
        super(message);

        this.code = code;
        this.locator = locator;
    }

    //---------------------------------------------------------------------------

    public CatalogException(String code, String message, String locator, CatalogException cause) {
        super(message, cause);

        this.code = code;
        this.locator = locator;
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public static Element marshal(CatalogException e) {
        //--- setup root node

        Element root = new Element("ExceptionReport", Csw.NAMESPACE_OWS);
        root.setAttribute("version", Csw.OWS_VERSION);
        root.addNamespaceDeclaration(Csw.NAMESPACE_XSI);
        root.setAttribute("schemaLocation", Csw.NAMESPACE_OWS.getURI() + " " + Csw.OWS_SCHEMA_LOCATIONS + "/ows/1.0.0/owsExceptionReport.xsd", Csw.NAMESPACE_XSI);

        while (e != null) {
            Element exc = new Element("Exception", Csw.NAMESPACE_OWS);
            exc.setAttribute("exceptionCode", e.getCode());

            if (e.getMessage() != null)
                exc.addContent(new Element("ExceptionText", Csw.NAMESPACE_OWS).setText(e.getMessage()));

            if (e.getLocator() != null)
                exc.setAttribute("locator", e.getLocator());

            root.addContent(exc);

            e = (CatalogException) e.getCause();
        }

        return root;
    }

    public static void unmarshal(Element response) throws Exception {
        if (!response.getName().equals("ExceptionReport"))
            return;

        Namespace ns = response.getNamespace();

        @SuppressWarnings("unchecked")
        List<Element> exceptions = response.getChildren("Exception", ns);

        if (exceptions.size() == 0)
            throw new Exception("Bad exception (no 'Exception' elem) : \n" +
                Xml.getString(response));

        CatalogException e = null;

        for (int i = exceptions.size() - 1; i >= 0; i--) {
            Element ex = exceptions.get(i);

            e = createException(ex, response, e);
        }

        throw e;
    }

    //---------------------------------------------------------------------------

    private static CatalogException createException(Element ex, Element response,
                                                    CatalogException prev) throws Exception {
        Namespace ns = response.getNamespace();

        String code = ex.getAttributeValue("exceptionCode");

        if (code == null)
            throw new Exception("Bad exception (no 'exceptionCode' attr) : \n" +
                Xml.getString(response));

        Element text = ex.getChild("ExceptionText", ns);

        String locator = ex.getAttributeValue("locator");
        String message = (text == null) ? null : text.getText();

        CatalogException e = null;

        if (code.equals(INVALID_PARAMETER_VALUE))
            e = new InvalidParameterValueEx(null, message, prev);

        if (code.equals(INVALID_UPDATE_SEQUENCE))
            e = new InvalidUpdateSequenceEx(message, prev);

        if (code.equals(MISSING_PARAMETER_VALUE))
            e = new MissingParameterValueEx(null, prev);

        if (code.equals(NO_APPLICABLE_CODE))
            e = new NoApplicableCodeEx(message, prev);

        if (code.equals(OPERATION_NOT_SUPPORTED))
            e = new OperationNotSupportedEx(null, prev);

        if (code.equals(VERSION_NEGOTIATION_FAILED))
            e = new VersionNegotiationFailedEx(message, prev);

        if (e == null)
            throw new Exception("Bad exception (unknown 'exceptionCode') : \n" +
                Xml.getString(response));

        e.locator = locator;

        return e;
    }

    //---------------------------------------------------------------------------

    public String getCode() {
        return code;
    }

    //---------------------------------------------------------------------------

    public String getLocator() {
        return locator;
    }

    //---------------------------------------------------------------------------

    public String toString() {
        String clazz = getClass().getName();
        return clazz + ": code=" + code + ", locator=" + locator + ", message=" + getMessage();
    }
}

//=============================================================================
/*

il Content-Type deve essere "application/soap+xml; charset="utf-8" "
il Content-Length deve essere settato

<?xml version="1.0" ?>

<env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
	<env:Body>
		<env:Fault>
			<env:Code>
				<env:Value>env:Sender|env:Receiver</env:Value>
		[		<env:Subcode>
					<env:Value>gn:xxx</env:Value>
				[	<env:Subcode>...</env:Subcode> ]
				</env:Subcode> ]
			</env:Code>

			<env:Reason>
				<env:Text xml:lang="en">...human readable...</env:Text>
				...
			</env:Reason>

			<env:Detail>
				<... env:encodingStyle="geonet.org/encoding/error">
					...altri figli con pi� info
				</...>
			</env:Detail>
		</env:Fault>
	</env:Body>
</env:Envelope>



	<env:Code>
		<env:Value>env:Sender|env:Receiver</env:Value>
[		<env:Subcode>
			<env:Value>gn:xxx</env:Value>
		[	<env:Subcode>...</env:Subcode> ]
		</env:Subcode> ]
	</env:Code>

	<env:Reason>
		<env:Text xml:lang="en">...human readable...</env:Text>
		...
	</env:Reason>

	<env:Detail>
		<... env:encodingStyle="geonet.org/encoding/error">
			...altri figli con pi� info
		</...>
	</env:Detail>




	<env:faultcode>env:Server</env:faultcode>
	<env:faultstring>...human readable...</env:faultstring>

	<env:detail>
		<ows:ExceptionReport>...
	</env:detail>

 */

