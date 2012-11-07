package org.fao.geonet.services.region;

import org.jdom.Element;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public enum GeomFormat {

    WKT{
        private WKTWriter wktWriter = new WKTWriter();
        @Override
        public Element toElement(Geometry geom) {
            return new Element("geom").setText(wktWriter.write(geom));
        }
    };
    
    public abstract Element toElement(Geometry geom);
}
