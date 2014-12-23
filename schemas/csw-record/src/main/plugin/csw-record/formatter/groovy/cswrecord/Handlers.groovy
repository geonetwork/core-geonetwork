package cswrecord

import org.fao.geonet.services.metadata.format.FormatType
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