//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
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
package org.fao.geonet.jms.message;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Message for JMS exchange.
 *
 * @author heikki doeleman
 */
public abstract class Message implements Encodable {

    /**
     * Encodes the messages as an XML string.
     *
     * @return XML string
     */
    public String encode() {
        OutputStream os = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(os);
        encoder.writeObject(this);
        encoder.close();
        return os.toString();
    }

    /**
     * Decodes XML string to a Message.
     *
     * @param xml XML string
     * @return message
     */
    public abstract Message decode(String xml);

    //TODO: To test
    /*public Message decode(String xml) throws Exception {
       XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));

        decoder.close();

        return (Message) Class.forName(this.getClass().getName()).cast(decoder.readObject());
    }*/

}