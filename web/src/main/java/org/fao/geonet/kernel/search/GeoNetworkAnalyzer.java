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

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.fao.geonet.constants.Geonet;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Set;

/**
 * Default Lucene analyzer for GeoNetwork, based on a modified version of WhitespaceTokenizer and with added LowercaseFilter and
 * ASCIIFoldingFilter, and optionally StopFilter.
 * <p/>
 * Reason is that with StandardAnalyzer, which GeoNetwork was using before, it tokenizes such that the character * is
 * seen as delimiting. That caused queries with wildcards analyzed using StandardAnalyzer to fail.
 *
 * @author heikki doeleman
 */
public final class GeoNetworkAnalyzer extends Analyzer {
 
    private CharArraySet stopwords;

    /**
     * Creates this analyzer using no stopwords.
     */
    public GeoNetworkAnalyzer() {
        this(null);
    }

    /**
     * 
     */
    public GeoNetworkAnalyzer(Set<String> stopwords) {
        if(stopwords == null || stopwords.isEmpty()) {
            this.stopwords = CharArraySet.EMPTY_SET;
        } else {
            this.stopwords = new CharArraySet(Geonet.LUCENE_VERSION, stopwords, true);
        }
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
        final Tokenizer source = new StandardTokenizer(Geonet.LUCENE_VERSION, reader);
        ASCIIFoldingFilter asciiFoldingFilter = new ASCIIFoldingFilter(new LowerCaseFilter(Geonet.LUCENE_VERSION, new StandardFilter(Geonet.LUCENE_VERSION, source)));
        if(CollectionUtils.isNotEmpty(this.stopwords)) {
            return new TokenStreamComponents(source, new StopFilter(Geonet.LUCENE_VERSION, asciiFoldingFilter, this.stopwords));
        }
        else {
            return new TokenStreamComponents(source, asciiFoldingFilter);
        }
    }

}