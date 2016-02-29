package org.fao.geonet.services.api.tools.i18n;


import java.util.*;

/**
 * Created by francois on 05/02/16.
 */
public class LanguageUtils {
    private final Set<String> iso3code;
    private final String defaultLanguage;
    Collection<Locale> locales = new ArrayList<>();

    public LanguageUtils(final Set<String> localesToLoad,
                         final String defaultLanguage) {
        iso3code = Collections.unmodifiableSet(localesToLoad);
        this.defaultLanguage = defaultLanguage;
        for(String l : iso3code) {
            locales.add(Locale.forLanguageTag(l));
        }
    }
//    Require Java 8
//    public String parseAcceptLanguage(final String language) {
//        List<Locale.LanguageRange> list = Locale.LanguageRange.parse(language);
//        Locale locale = Locale.lookup(list, locales);
//        if (locale != null) {
//            return locale.getISO3Language();
//        } else {
//            return defaultLanguage;
//        }
//    }

    public Locale parseAcceptLanguage(final Enumeration<Locale> listOfLocales) {
        while (listOfLocales.hasMoreElements()) {
            Locale l = listOfLocales.nextElement();
            if (iso3code.contains(l.getISO3Language())) {
                return l;
            }
        }
        return Locale.forLanguageTag(defaultLanguage);
    }
}
