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

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import java.util.Set;

/**
 * @author heikki doeleman
 */
public class WildCardStringAnalyzer {
    /**
     *
     * @param string
     * @param luceneIndexField
     * @param _analyzer
     * @param _tokenizedFieldSet
     * @return
     */
    public String analyze(String string, String luceneIndexField, PerFieldAnalyzerWrapper _analyzer, Set<String> _tokenizedFieldSet) {
        // wildcards - preserve them by analyzing the parts of the search string around them separately
        // (this is because Lucene's StandardTokenizer would remove wildcards, but that's not what we want)
        StringBuilder starsPreserved = new StringBuilder();
        String[] starSeparatedList = string.split("\\*");
        for (String starSeparatedPart : starSeparatedList) {
            StringBuilder qPreserved = new StringBuilder();
            // ? present
            if (starSeparatedPart.indexOf('?') >= 0) {
                String[] qSeparatedList = starSeparatedPart.split("\\?");
                for (String qSeparatedPart : qSeparatedList) {
                    String analyzedPart = LuceneSearcher.analyzeQueryText(luceneIndexField, qSeparatedPart, _analyzer, _tokenizedFieldSet);
                    qPreserved.append('?').append(analyzedPart);
                }
                // remove leading ?
                if (qPreserved.length() > 0) {
                    qPreserved.deleteCharAt(0);
                }
                starsPreserved.append('*').append(qPreserved);
            }
            // no ? present
            else {
                starsPreserved.append('*');
                String analyzedQueryText = LuceneSearcher.analyzeQueryText(luceneIndexField, starSeparatedPart, _analyzer, _tokenizedFieldSet);
                starsPreserved.append(analyzedQueryText);
            }
        }
        // remove leading *
        if (starsPreserved.length() > 0) {
            starsPreserved.deleteCharAt(0);
        }

        // restore ending wildcard
        if (string.endsWith("*")) {
            starsPreserved.append("*");
        } else if (string.endsWith("?")) {
            starsPreserved.append("?");
        }
        return starsPreserved.toString();
    }
}
