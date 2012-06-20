package org.fao.geonet.kernel.reusable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jeeves.xlink.URIMapper;

public class SharedObjectUriMapper implements URIMapper {
    Pattern idExtractor = Pattern.compile("(.*?)\\?.*?(id=[^&]+).*");
    Pattern wfsIdExtractor = Pattern.compile("(.*?)\\?.*?(wfs=[^&]+).*");
    Pattern typenameExtractor = Pattern.compile("(.*?)\\?.*?(typename=[^&]+).*");
    Pattern thesaurusExtractor = Pattern.compile(".*(thesaurus=[^&]+).*");
    
    public String map( String uri ) {
        if (uri.contains("/xml.extent.get?")) {
            Matcher extentIdMatcher = idExtractor.matcher(uri);
            extentIdMatcher.matches();

            Matcher wfsIdMatcher = wfsIdExtractor.matcher(uri);
            wfsIdMatcher.matches();
            
            Matcher typenameMatcher = typenameExtractor.matcher(uri);
            typenameMatcher.matches();
            
            String baseHRef = extentIdMatcher.group(1);
            String id = extentIdMatcher.group(2);
            String wfsId = wfsIdMatcher.group(2);
            String typenameId = typenameMatcher.group(2);
            
            return baseHRef+"?"+id+"&"+wfsId+"&"+typenameId;
        } else if (uri.contains("/xml.user.get?")) {
            Matcher userIdMatcher = idExtractor.matcher(uri);
            userIdMatcher.matches();
            return userIdMatcher.group(1)+"?"+userIdMatcher.group(2);
        } else if (uri.contains("/xml.keyword.get?")) {
            Matcher idMatcher = idExtractor.matcher(uri);
            idMatcher.matches();

            Matcher thesaurusMatcher = thesaurusExtractor.matcher(uri);
            thesaurusMatcher.matches();
            
            return idMatcher.group(1)+"?"+idMatcher.group(2)+"&"+thesaurusMatcher.group(1);
        }
        return uri;
    }

}
