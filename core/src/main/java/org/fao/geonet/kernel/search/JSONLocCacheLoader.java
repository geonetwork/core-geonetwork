package org.fao.geonet.kernel.search;

import jeeves.server.sources.http.ServletPathFinder;
import org.fao.geonet.Constants;
import org.fao.geonet.util.XslUtil;
import org.jdom.JDOMException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

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

        ServletPathFinder pathFinder =
                new ServletPathFinder(applicationContext.getBean(ServletContext.class));
        Path path = pathFinder.getAppPath();

        // FIXME: When using mvn jetty:run, the app path points
        // to the web module. In that case the resource is in the web-ui module
        // Published using overlay by jetty plugin. Can be better ?
        String devPath = "web/src/main/webapp";
        if (path.toString().contains(devPath)) {
            String webUiDeployedModulePath =
                    path.toString().replace(devPath, "web/target/geonetwork");
            path = Paths.get(webUiDeployedModulePath);
        }

        String iso2letterLangCode = XslUtil.twoCharLangCode(langCode, "eng");

        for (String file : files) {
            addJSONLocalizationFile(translations,
                    path.resolve("catalog").resolve("locales")
                            .resolve(iso2letterLangCode + "-" + file + ".json"));
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
    private void addJSONLocalizationFile(Map<String, String> translation, Path file)
            throws IOException, JDOMException {
        if (Files.exists(file)) {
            try {
                JSONObject json =
                        new JSONObject(
                                new String(Files.readAllBytes(file), Constants.CHARSET));

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