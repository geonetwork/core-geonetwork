//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.search;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;

import java.io.Reader;
import java.util.Set;

/**
 * Default Lucene analyzer for GeoNetwork, based on WhitespaceTokenizer but with added LowercaseFilter and
 * ASCIIFoldingFilter, and optionally StopFilter.
 * <p/>
 * Reason is that with StandardAnalyzer, which GeoNetwork was using before, it tokenizes such that the character * is
 * seen as delimiting. That caused queries with wildcards analyzed using StandardAnalyzer to fail.
 *
 * @author heikki doeleman
 */
public final class GeoNetworkAnalyzer extends GeoNetworkReusableAnalyzerBase {

    private Set<String> stopwords = null;
    private boolean enablePositionIncrements = true;
    private boolean ignoreCase = true;

    /**
     * Creates this analyzer using no stopwords.
     */
    public GeoNetworkAnalyzer() {
        super();
    }

    /**
     * Creates this analyzer using the provided stopwords, which may be null.
     * 
     * @param stopwords
     */
    public GeoNetworkAnalyzer(Set<String> stopwords) {
        super();
        this.stopwords = stopwords;
    }

    /**
     * Creates a new {@link TokenStreamComponents} instance for this analyzer.
     *
     * @param fieldName the name of the fields content passed to the
     *                  {@link TokenStreamComponents} sink as a reader
     * @param reader   the reader passed to the {@link Tokenizer} constructor
     * @return the {@link TokenStreamComponents} for this analyzer.
     */
    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {

        final Tokenizer source = new WhitespaceTokenizer(reader);

        if(stopwords != null) {
            return new TokenStreamComponents(source, new ASCIIFoldingFilter(new LowerCaseFilter(new StopFilter(enablePositionIncrements, source, stopwords, ignoreCase))));
        }
        else {
            return new TokenStreamComponents(source, new ASCIIFoldingFilter(new LowerCaseFilter(source)));
        }
    }

}