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

package cswrecord

import org.fao.geonet.api.records.formatters.FormatType
import org.geotools.geometry.jts.ReferencedEnvelope
import org.geotools.referencing.CRS

import static java.lang.Double.parseDouble

public class Handlers extends dublincore.Handlers {


    public static final String BOUNDING_BOX = 'ows:BoundingBox'

    public Handlers(handlers, f, env) {
        super(handlers, f, env, 'csw:Record')
        excludedEls << BOUNDING_BOX
    }

    public void addDefaultHandlers() {
        super.addDefaultHandlers()
        handlers.add name: 'BBoxes', select: BOUNDING_BOX, handleBoundingBox
    }

    def handleBoundingBox = { el ->
        def lowerCorner = el.'ows:LowerCorner'.text().trim().split(' ')
        def upperCorner = el.'ows:UpperCorner'.text().trim().split(' ')

        def crsAtt = el['@crs']
        if (upperCorner.length > 1 && lowerCorner.length > 1) {
            try {
                String crsCode = crsAtt.text().trim()
                if (!crsCode.isEmpty()) {
                    if (crsCode.contains(":::")) {
                        crsCode = crsCode.split(":::", 2)[1]
                    }
                } else {
                    crsCode = "EPSG:4326"
                }

                def crs = CRS.decode(crsCode.trim())
                ReferencedEnvelope bbox = new ReferencedEnvelope(parseDouble(lowerCorner[0]), parseDouble(upperCorner[0]),
                        parseDouble(lowerCorner[1]), parseDouble(upperCorner[1]), crs)

                def model = [
                        label     : f.nodeLabel(el),
                        s         : bbox.getMinY(),
                        n         : bbox.getMaxY(),
                        w         : bbox.getMinX(),
                        e         : bbox.getMaxX(),
                        geomproj  : "EPSG:${CRS.lookupEpsgCode(crs, false)}",
                        minwidth: mapConfig.getWidth() / 4,
                        minheight: mapConfig.getWidth() / 4,
                        mapconfig   : this.env.mapConfiguration
                ]

                return handlers.fileResult("html/bbox.html", model)
            } catch (Throwable t) {
                // skip and use default
            }
        }

        def data = el.'ows:LowerCorner'.text().trim() + ", " + el.'ows:UpperCorner'.text().trim()
        if (!crsAtt.text().isEmpty()) {
            data += " (${crsAtt.text()})"
        }

        return handlers.fileResult("html/2-level-entry.html", [label: f.nodeLabel(el), childData: data])
    }

    @Override
    public String getAbstract(el) {
        def abstractVal = firstNonEmpty(el.'dct:abstract')
        if (abstractVal.isEmpty()) {
            abstractVal = super.getAbstract(el);
        }
        return abstractVal
    }

}
