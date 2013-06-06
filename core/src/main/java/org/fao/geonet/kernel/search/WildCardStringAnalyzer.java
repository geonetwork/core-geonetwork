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
                qPreserved.deleteCharAt(0);
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
        }
        else if (string.endsWith("?")) {
            starsPreserved.append("?");
        }
        return starsPreserved.toString();
    }
}