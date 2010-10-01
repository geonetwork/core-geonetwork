package org.fao.geonet.kernel.search;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;

import java.io.IOException;
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
public final class GeoNetworkAnalyzer extends Analyzer {
    
         @Override
         public TokenStream tokenStream(String fieldName, Reader reader) {
             TokenStream ts = new WhitespaceTokenizer(reader);
             ts = new LowerCaseFilter(ts);
             ts = new ASCIIFoldingFilter(ts);
             return ts;
         }

         @Override
         public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
           TokenStream ts = (TokenStream) getPreviousTokenStream();
           if (ts == null) {
             ts = tokenStream(null, reader);
             setPreviousTokenStream(ts);
           }
           else {
             ts.reset();
           }
           return ts;
         }

}
