package org.fao.geonet.kernel.search;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.Tokenizer;

import java.io.Reader;

/**
 *
 * Default Lucene analyzer for GeoNetwork. Based on WhitespaceAnalyzer but with added
 * LowercaseFilter and ASCIIFoldingFilter to the tokenstream.
 *
 * Reason is that if using Standardanalyzer, like GeoNetwork did before, it tokenizes such that the character * is
 * seen as delimiting. That caused queries with wildcards analyzed using StandardAnalyzer to fail.
 *
 *
 * @author heikki doeleman
 */
public final class GeoNetworkAnalyzer extends GeonetWorkReusableAnalyzerBase {


    @Override
    protected TokenStreamComponents createComponents(final String fieldName,
                                                     final Reader reader) {

        final Tokenizer source = new WhitespaceTokenizer(reader);
        return new TokenStreamComponents(source, new ASCIIFoldingFilter(new LowerCaseFilter(
                source)));
    }
}
