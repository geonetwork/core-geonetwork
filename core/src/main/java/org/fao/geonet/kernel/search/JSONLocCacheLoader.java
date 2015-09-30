package org.fao.geonet.kernel.search;

import com.google.common.io.Resources;
import org.fao.geonet.Constants;
import org.fao.geonet.util.XslUtil;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.servlet.ServletContext;

/**
 * Create a cache based on JSON translation files.
 *
 * All JSON files are combined in one cache (like the client app does).
 */
public final class JSONLocCacheLoader implements Callable<Map<String, String>> {
    private final String langCode;

    /**
     * The list of files to load.
     */
    private static final List<String> files = Arrays.asList(
            new String[]{"core", "admin", "editor", "search"});

    private ConfigurableApplicationContext applicationContext;

    public JSONLocCacheLoader(ConfigurableApplicationContext context, String langCode) {
        this.langCode = langCode;
        this.applicationContext = context;
    }

    @Override
    public Map<String, String> call() throws Exception {
        Map<String, String> translations = new HashMap<String, String>();

        ServletContext servletContext = applicationContext.getBean(ServletContext.class);

        String iso2letterLangCode = XslUtil.twoCharLangCode(langCode, "eng");

        for (String file : files) {
            URL resource = servletContext.getResource("/catalog/locales/" + iso2letterLangCode + "-" + file + ".json");
            if (resource == null) {
                resource = servletContext.getResource("/catalog/locales/en-" + file + ".json");
            }
            addJSONLocalizationFile(translations, resource);
        }
        return translations;
    }

    public static String cacheKey(final String langCode) {
        return "json:" + langCode;
    }

    /**
     * Populate the cache by loading all JSON files.
     *
     * @param translation
     * @param file
     * @throws IOException
     * @throws JDOMException
     */
    @SuppressWarnings("unchecked")
    private void addJSONLocalizationFile(Map<String, String> translation, URL file)
            throws IOException, JDOMException {
        if (file != null) {
            try {
                JSONObject json =
                        new JSONObject(Resources.toString(file, Constants.CHARSET));

                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    translation.put(key, json.getString(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}