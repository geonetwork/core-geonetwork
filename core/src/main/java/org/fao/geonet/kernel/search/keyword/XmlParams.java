package org.fao.geonet.kernel.search.keyword;

public final class XmlParams {
    /**
     * If defined this param declares the keyword text to search for
     * 
     * Optional
     */
    public static final String pKeyword = "pKeyword";
    /**
     * The type of search to execute.  Values accepted are: 0,1,2 or one of the Enumeration KeywordSearchType
     * 
     * Optional {@link KeywordSearchType#MATCH} is the default
     */
    public static final String pTypeSearch = "pTypeSearch";
    /**
     * the maximum number of results to return.  Must be a positive integer
     * 
     * Optional
     */
    public static final String maxResults = "maxResults";
    /**
     * the number of results to skip before returning values (for paging)
     * Must be an integer > 0 and exactly one thesaurus must be defined.  
     * 
     * Optional
     */
    public static final String offset = "offset";
    /**
     * type of thesaurus to search in if pThesauri is not defined
     * 
     * Optional
     */
    public static final String pType = "pType";
    /**
     * A thesaurus to search.  Several pThesauri Elements can be defined in request
     * 
     * Optional default is to search all thesauri
     */
    public static final String pThesauri = "pThesauri";
    /**
     * A language to load when a keyword is found.  Several pLang Elements can be defined in request
     * 
     * Optional default is to only load the language of the current gui language
     */
    public static final String pLang = "pLang";
    /**
     * If present then a search clause for the uri is included
     */
    public static final String pUri = "pUri";
}