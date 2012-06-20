package org.fao.geonet.kernel.search;

import jeeves.utils.Log;
import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *Geocat Specific implementation of the GeonetworkAnalyzer
 */
public class GeocatAnalyzer extends GeoNetworkReusableAnalyzerBase {

    private Set<String> stopwords = null;
    private boolean enablePositionIncrements = true;
    private boolean ignoreCase = true;


    public GeocatAnalyzer(String locale) {
        super();
        this.stopwords = defaultStopWords(locale);
        if(stopwords != null) {
            for(String sw : stopwords) {
                Log.debug("GeoNetworkAnalyzer", "stopword: " + sw);
            }
        }
    }

    /**
     * Creates a new {@link TokenStreamComponents} instance for this analyzer.
     *
     * @param fieldName the name of the fields content passed to the
     *                  {@link TokenStreamComponents} sink as a reader
     * @param reader   the reader passed to the {@link org.apache.lucene.analysis.Tokenizer} constructor
     * @return the {@link TokenStreamComponents} for this analyzer.
     */
    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {

        final Tokenizer source = new StandardTokenizer(Version.LUCENE_30, reader);

        if(stopwords != null) {
            return new TokenStreamComponents(source, new StopFilter(enablePositionIncrements, new ASCIIFoldingFilter(new LowerCaseFilter(new StandardFilter(source))), stopwords, ignoreCase));
        }
        else {
            return new TokenStreamComponents(source, new ASCIIFoldingFilter(new LowerCaseFilter(new StandardFilter(source))));
        }
    }


    public Set<String> getStopWords()
    {
        return stopwords;
    }

    /*

    JER: The following code is probably necessary so that searches with -, _, etc... work but the code doesn't work with the new lucene version


    private static HashMap<String,String[]> STOP_WORDS_CACHE = new HashMap<String,String[]>();
    private String[] _stopWords;
    private static final Set<Character> STRING_TERM = new HashSet<Character>(Arrays.asList('.', '-'));
    private static final Set<Character> NUM_TERM = new HashSet<Character>(Arrays.asList('-'));

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_30, reader);
        TokenFilter splitter = new TokenFilter(tokenizer) {
        	int index = 0;
        	int length = 0;
        	Token current = null;
        	@Override
			public Token next(Token result) throws IOException {
        		if(current!=null) {
					current.setStartOffset(current.endOffset()+1);
					current.setEndOffset(current.termLength());
        			return nextToken();
        		}
				current = input.next(result);

				if (current == null)
					return null;

				index = 0;
				length = current.termLength();
				return nextToken();
			}
			private Token nextToken() {
				Token t = current;
                Set<Character> terminators = STRING_TERM;

                if(t.type().equals("<NUM>")) {
                    terminators = NUM_TERM;
                }
                char[] buffer = t.termBuffer();
                int i = 0;
                for (; index < length; i++, index++) {
                    char c = buffer[index];
                    if(terminators.contains(c)) {
                        t.setTermLength(i+1);
                        index++;
                        return t;
                    } else {
                        buffer[i]=c;
                    }
					t.setTermLength(i+1);
				}
				current = null;
				return t;
			}
		};
        StandardFilter standard = new StandardFilter(splitter);
        StopFilter stopFilter = new StopFilter(standard, _stopWords);
        ASCIIFoldingFilter latinFilter = new ASCIIFoldingFilter(stopFilter);
        return new LowerCaseFilter(latinFilter);
    }

    private static synchronized String[] readStopWords(String locale, File file)
            throws IOException
    {

        if(!STOP_WORDS_CACHE.containsKey(locale)) {
            Set<String> words = new HashSet<String>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (String line = reader.readLine(); line != null; line = reader
                    .readLine()) {
                String trimmed = line.trim();

                if (!trimmed.startsWith("#"))
                    words.add(trimmed);
            }

            String[] specialCaseWords = defaultStopWords(locale, new String[0]);

            words.addAll(Arrays.asList(specialCaseWords));

            final String[] wordArray = words.toArray(new String[words.size()]);
            STOP_WORDS_CACHE.put(locale, wordArray);
            return wordArray;
        }
        return STOP_WORDS_CACHE.get(locale);


    }
       */
    private static Set<String> defaultStopWords(String locale)
    {
        Set<?> specialCaseWords = null;
        if ("fr".equalsIgnoreCase(locale) || "fra".equalsIgnoreCase(locale)) {
            specialCaseWords = FrenchAnalyzer.getDefaultStopSet();
        }

        if ("de".equalsIgnoreCase(locale) || "deu".equalsIgnoreCase(locale)) {
            specialCaseWords = GermanAnalyzer.getDefaultStopSet();
        }
        if (locale == null || "en".equalsIgnoreCase(locale)
                || "eng".equalsIgnoreCase(locale)) {
            specialCaseWords = StandardAnalyzer.STOP_WORDS_SET;
        }

        if(specialCaseWords==null){
            specialCaseWords=Collections.emptySet();
        }
        return new HashSet<String>((Collection<? extends String>) specialCaseWords);
    }

}
