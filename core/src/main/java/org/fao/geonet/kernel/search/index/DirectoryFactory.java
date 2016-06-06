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
import org.fao.geonet.kernel.search.LuceneConfig;

import java.io.IOException;
import java.util.Set;

/**
 * A factory for creating {@link org.apache.lucene.store.Directory} objects that the Lucene readers
 * and writers will use.
 *
 * User: Jesse Date: 10/18/13 Time: 11:20 AM
 */
public interface DirectoryFactory {
    /**
     * Create a brand new directory object that will contain the lucene index.
     *
     * @param config configuration to use for creating directory object.
     * @return a brand new directory object.
     */
    Directory createIndexDirectory(String indexId, LuceneConfig config) throws IOException;

    /**
     * Create a brand new directory object that will contain the lucene taxonomy.
     *
     * @param config configuration to use for creating directory object.
     * @return a brand new directory object.
     */
    Directory createTaxonomyDirectory(LuceneConfig config) throws IOException;

    /**
     * Clean out the taxonomy directory if necessary.  Delete all old index files for example.
     */
    void resetTaxonomy() throws IOException;

    /**
     * Clean out the taxonomy directory if necessary.  Delete all old index files for example.
     */
    void resetIndex() throws IOException;

    /**
     * Get the list of indices currently available.
     *
     * @return the ids
     */
    Set<String> listIndices() throws IOException;
}
