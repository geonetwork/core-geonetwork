//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
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

package org.fao.xsl.support;

import java.util.Iterator;

import org.fao.geonet.services.extent.ExtentHelper.ExtentTypeCode;
import org.jdom.Element;
import org.jdom.filter.Filter;

/**
 * Validates that there is a EX_Extent with the correct parameters. See
 * constructor.
 *
 * @author jeichar
 */
public class PolygonValidator implements Filter
{

    private static final long    serialVersionUID = 1L;
    public static final Finder   BBOX_FINDER      = new Finder("EX_GeographicBoundingBox");
    public static final Finder   EXTERIOR_FINDER  = new Finder("exterior");
    public static final Finder   INTERIOR_FINDER  = new Finder("interior");
    public static final Finder   DESC_FINDER      = new Finder("description/PT_FreeText/textGroup/LocalisedCharacterString");
    public static final Finder   EXTENT_TYPE_CODE_FINDER      = new Finder("extentTypeCode/Boolean");

    private final String         _id;
    private final ExtentTypeCode _extentTypeCode;
    private final int            _bboxElements;
    private final int            _gmlExteriors;
    private final int            _gmlInteriors;

    /**
     * @param id
     *            the first word in the description's Character String is the
     *            id.
     * @param bboxElements
     *            The number of bbox elements in the extent
     * @param gmlExteriorElements
     *            the number of exterior elements in the extent.
     * @param gmlInteriorElements
     *            the number of interior elements in the extent.
     */
    public PolygonValidator(String id, ExtentTypeCode extentTypeCode, int bboxElements, int gmlExteriorElements,
            int gmlInteriorElements)
    {
        _id = id;
        _extentTypeCode = extentTypeCode;
        _bboxElements = bboxElements;
        _gmlExteriors = gmlExteriorElements;
        _gmlInteriors = gmlInteriorElements;
    }

    public boolean matches(Object arg0)
    {
        if (arg0 instanceof Element) {
            Element elem = (Element) arg0;

            if ("EX_Extent".equals(elem.getName())) {
                String desc = ((Element) elem.getDescendants(DESC_FINDER).next()).getTextTrim();
                String id = desc.substring(0, desc.indexOf(' ')).toUpperCase();
                if (!_id.equals(id))
                    return false;

                boolean extentTypeCodeMatches = true;
                if( _extentTypeCode != ExtentTypeCode.NA){
                    Iterator descendants = elem.getDescendants(EXTENT_TYPE_CODE_FINDER);
                    if( !descendants.hasNext() ) return false;
                    String typeCode = ((Element) descendants.next()).getTextTrim();
                    switch (_extentTypeCode)
                    {
                    case INCLUDE:
                        extentTypeCodeMatches = "true".equals(typeCode);
                        break;
                    case EXCLUDE:
                        extentTypeCodeMatches = "false".equals(typeCode);
                        break;
                    default:
                        throw new RuntimeException(_extentTypeCode+" is not handled");
                    }
                }

                int bboxElements = count(elem, BBOX_FINDER);
                int exteriorElements = count(elem, EXTERIOR_FINDER);
                int interiorElements = count(elem, INTERIOR_FINDER);

                boolean matches = extentTypeCodeMatches && bboxElements == _bboxElements
                        && exteriorElements == _gmlExteriors && interiorElements == _gmlInteriors;
                return matches;
            }
            return false;
        }
        return false;
    }

    private int count(Element elem, Finder finder)
    {
        Iterator found = elem.getDescendants(finder);
        int c = 0;
        while (found.hasNext()) {
            c++;
            found.next();
        }
        return c;
    }

    @Override
    public String toString()
    {
        return "Polygon["+_id+" typeCode="+_extentTypeCode+" bbox="+_bboxElements+" gmlExterior="+_gmlExteriors+" gmlInteriors="+_gmlInteriors+"]";
    }
}
