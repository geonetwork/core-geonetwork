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

package org.fao.geonet.kernel.search;

import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;

import java.io.Closeable;
import java.io.IOException;

public class IndexAndTaxonomy implements Closeable {
    public final TaxonomyReader taxonomyReader;
    public final GeonetworkMultiReader indexReader;
    public final long version;

    public IndexAndTaxonomy(long version, GeonetworkMultiReader indexReader, TaxonomyReader taxonomyReader) {
        super();
        this.taxonomyReader = taxonomyReader;
        this.indexReader = indexReader;
        this.version = version;
    }

    @Override
    public void close() throws IOException {
        indexReader.releaseToNRTManager();
        taxonomyReader.decRef();
    }
}
