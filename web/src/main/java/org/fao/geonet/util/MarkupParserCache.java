package org.fao.geonet.util;

import java.util.HashMap;
import java.util.Map;

import jeeves.utils.Log;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;

public final class MarkupParserCache {
    private static final Map<String, MarkupLanguage> cache = new HashMap<String, MarkupLanguage>(10);
    private MarkupParserCache(){}
    public static synchronized MarkupParser lookup(String markupLanguage) {
        MarkupLanguage lang = cache.get(markupLanguage);
        if (lang == null) {
            try {
                lang = (MarkupLanguage) Class.forName(markupLanguage.toString()).newInstance();
            } catch (Exception e) {
                Log.error("Markup class setting is invalid:"+markupLanguage+".  The class must extend MarkupLanguage and must have a 0 arg constructor.", e);
                throw new RuntimeException(e);
            }
            cache.put(markupLanguage, lang);
        }
        return new MarkupParser(lang);
    }
    
    
}
