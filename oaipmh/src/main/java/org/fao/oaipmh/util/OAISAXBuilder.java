//=============================================================================
//=== Copyright (C) 2001-2014 Food and Agriculture Organization of the
//=== United Nations (FAO-UN), United Nations World Food Programme (WFP)
//=== and United Nations Environment Programme (UNEP)
//===
//=== This library is free software; you can redistribute it and/or
//=== modify it under the terms of the GNU Lesser General Public
//=== License as published by the Free Software Foundation; either
//=== version 2.1 of the License, or (at your option) any later version.
//===
//=== This library is distributed in the hope that it will be useful,
//=== but WITHOUT ANY WARRANTY; without even the implied warranty of
//=== MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//=== Lesser General Public License for more details.
//===
//=== You should have received a copy of the GNU Lesser General Public
//=== License along with this library; if not, write to the Free Software
//=== Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
//===
//=== Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//=== Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.oaipmh.util;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Extend SAXBuilder so that we can set security features on the XMLReader 
 * before it is used otherwise they don't have any effect 
 */
public class OAISAXBuilder extends SAXBuilder {

  public OAISAXBuilder(boolean validating) {
        super(validating);
  }

  protected XMLReader createParser() throws JDOMException {

        XMLReader parser = super.createParser();
        try {              
            parser.setFeature("http://xml.org/sax/features/external-general-entities", false);
            setFeature("http://xml.org/sax/features/external-general-entities", false);
          } catch (SAXNotRecognizedException e) {
            e.printStackTrace();
            // ignore
          } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            // ignore
          }

          try {
            parser.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            setFeature("http://xml.org/sax/features/external-parameter-entities", false);
          } catch (SAXNotRecognizedException e) {
            e.printStackTrace();
            // ignore
          } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            // ignore
        }

        return parser;
  }
}
