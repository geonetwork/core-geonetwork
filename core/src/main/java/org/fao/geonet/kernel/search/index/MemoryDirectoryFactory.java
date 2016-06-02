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

package org.fao.geonet.kernel.search.index;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.fao.geonet.kernel.search.LuceneConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * An in memory-only directory factory.
 * <p/>
 * User: Jesse Date: 10/18/13 Time: 1:41 PM
 */
public class MemoryDirectoryFactory implements DirectoryFactory {
    @Override
    public Directory createIndexDirectory(final String indexId, final LuceneConfig config) throws IOException {
        return new RAMDirectory();
    }

    @Override
    public Directory createTaxonomyDirectory(final LuceneConfig config) throws IOException {
        return new RAMDirectory();
    }

    @Override
    public void resetTaxonomy() throws IOException {
        // nothing needed
    }

    @Override
    public void resetIndex() throws IOException {
        // nothing needed
    }

    @Override
    public Set<String> listIndices() {
        return Collections.emptySet();
    }
}
