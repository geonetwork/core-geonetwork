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

import jeeves.utils.Log;
import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.domain.IndexLanguage;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
public final class GeoNetworkAnalyzer extends GeoNetworkReusableAnalyzerBase {
 
    private Set<String> stopwords = new HashSet<String>();
    private boolean enablePositionIncrements = true;
    private boolean ignoreCase = true;
    
    /*
    private synchronized void readStopwords(File stopwordsDir) {
        if(stopwordsMap.keySet().size() > 0) {
            System.out.println("stopwords already loaded. Restart app to re-load");
        }
        if(!stopwordsDir.exists() || !stopwordsDir.isDirectory()) {
            Log.warning("GeoNetworkAnalyzer", "Invalid stopwords directory " + stopwordsDir.getAbsolutePath() + ", not using any stopwords");
            return;
        }
        else {
            System.out.println("loading stopwords");
            for(File stopwordsFile : stopwordsDir.listFiles()) {
                System.out.println("stopwords file " + stopwordsFile.getName());
                String language = stopwordsFile.getName().substring(0, stopwordsFile.getName().indexOf('.'));
                System.out.println("language: " + language);
                if(language.length() != 2) {
                    System.out.println("HUH " + language);
                }
                // look up stopwords for that language
                Set<String> stopwordsForLanguage = StopwordFileParser.parse(stopwordsFile.getAbsolutePath());         
                if(stopwordsForLanguage != null) {
                    stopwordsMap.put(language, stopwordsForLanguage);
                }
            }
        }        
    }
    */
        
    /**
     * Creates this analyzer using no stopwords.
     */
    public GeoNetworkAnalyzer() {
        super();
    }

    /**
     * 
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
        final Tokenizer source = new StandardTokenizer(Version.LUCENE_30, reader);
        if(CollectionUtils.isNotEmpty(this.stopwords)) {
            return new TokenStreamComponents(source, new StopFilter(enablePositionIncrements, new ASCIIFoldingFilter(new LowerCaseFilter(new StandardFilter(source))), this.stopwords, ignoreCase));
        }
        else {
            return new TokenStreamComponents(source, new ASCIIFoldingFilter(new LowerCaseFilter(new StandardFilter(source))));
        }
    }

}