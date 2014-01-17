package org.fao.geonet.kernel.search;

import org.apache.commons.lang.StringUtils;
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
        String starsPreserved = "";
        String[] starSeparatedList = string.split("\\*");
        for (String starSeparatedPart : starSeparatedList) {
            String qPreserved = "";
            // ? present
            if (starSeparatedPart.indexOf('?') >= 0) {
                String[] qSeparatedList = starSeparatedPart.split("\\?");
                for (String qSeparatedPart : qSeparatedList) {
                    String analyzedPart = LuceneSearcher.analyzeQueryText(luceneIndexField, qSeparatedPart, _analyzer, _tokenizedFieldSet);
                    qPreserved += '?' + analyzedPart;
                }
                // remove leading ?
                if (qPreserved.length()>0)
                   qPreserved = qPreserved.substring(1);
                starsPreserved += '*' + qPreserved;
            }
            // no ? present
            else {
                starsPreserved += '*' + LuceneSearcher.analyzeQueryText(luceneIndexField, starSeparatedPart, _analyzer, _tokenizedFieldSet);
            }
        }
        // remove leading *
        if (!StringUtils.isEmpty(starsPreserved)) {
            starsPreserved = starsPreserved.substring(1);
        }

        // restore ending wildcard
        if (string.endsWith("*")) {
            starsPreserved += "*";
        }
        else if (string.endsWith("?")) {
            starsPreserved += "?";
        }
        return starsPreserved;
    }
}