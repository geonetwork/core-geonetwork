package org.fao.geonet.transifex;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.fao.geonet.Constants;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Downloads the non-english (translation) files and updates the local translation files with the downloaded translations.
 */
@Mojo(name = "get-translations",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresOnline = true)
public class GetTranslationsMojo extends AbstractTransifexMojo {

    private LangMap langMap = new LangMap();

    public void execute() throws MojoExecutionException {
        try {

            init();

            Set<String> twoCharLangs = getTranslationLanguages();

            Set<String> knownToExist = Sets.newHashSet();

            for (TranslationFileConfig file : files) {
                try {
                    file.init();
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
                for (String twoCharLang : twoCharLangs) {
                    List<TransifexReadyFile> fromTransifex = Lists.newArrayList();
                    for (TransifexReadyFile readyFile : file.getTransifexFiles()) {
                        if (knownToExist.contains(readyFile.resourceId) || resourceExists(readyFile)) {
                            knownToExist.add(readyFile.resourceId);
                            String translation = getTranslation(readyFile, twoCharLang);
                            if (translation != null) {
                                fromTransifex.add(new TransifexReadyFile(readyFile.resourceId, readyFile.transifexName, translation,
                                        readyFile.categories));
                            }
                        }
                    }
                    if (!fromTransifex.isEmpty()) {
                        file.writeTranslations(langMap.getThreeChar(twoCharLang), fromTransifex);
                    }
                }

            }

            getLog().info(String.format("Get Translations: %s, %s, %s", files.get(0).path, files.get(0).layout, files.get(0).id));
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Set<String> getTranslationLanguages() throws IOException {
        Set<String> languages = Sets.newHashSet();
        try (CloseableHttpResponse response = get(url + "/" + project + "/languages/");) {
            byte[] rawData = ByteStreams.toByteArray(response.getEntity().getContent());
            JSONArray array = JSONArray.fromObject(new String(rawData, Constants.CHARSET));
            for (int i = 0; i < array.size(); i++) {
                String lang = array.optJSONObject(i).getString("language_code");
                if (lang != null) {
                    langMap.getThreeChar(lang);
                    languages.add(lang);
                }
            }
        }
        return languages;
    }

    private String getTranslation(TransifexReadyFile transifexReadyFile, String language) throws IOException {
        boolean hasTranslatedStrings = false;
        try (CloseableHttpResponse response = get(getResourceUrl(transifexReadyFile.resourceId) + "/translation/" + language + "/?mode=onlytranslated")) {
            byte[] rawData = ByteStreams.toByteArray(response.getEntity().getContent());
            JSONObject content = JSONObject.fromObject(new String(rawData, Constants.CHARSET)).getJSONObject("content");
            hasTranslatedStrings  = hasData(content);
        }
        if (hasTranslatedStrings) {
            try (CloseableHttpResponse response = get(getResourceUrl(transifexReadyFile.resourceId) + "/translation/" + language + "/")) {
                byte[] rawData = ByteStreams.toByteArray(response.getEntity().getContent());
                return JSONObject.fromObject(new String(rawData, Constants.CHARSET)).getString("content");
            }
        }
        return null;
    }

    private boolean hasData(JSONObject content) {
        Iterator keys = content.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (!content.getString(key).trim().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) throws MojoExecutionException {
        GetTranslationsMojo getTransifexMojo = new GetTranslationsMojo();
        getTransifexMojo.files = Lists.newArrayList();

        TranslationFileConfig translationFileConfig = new TranslationFileConfig();
        translationFileConfig.path = "C:\\GitHub\\geonetwork\\geocat_trunk\\schemas\\iso19139\\src\\main\\plugin\\iso19139\\loc";
        translationFileConfig.fileName = "labels.xml";
        translationFileConfig.formatClass = "SchemaPluginLabelsFormat";
        translationFileConfig.id = "test-labelsxml";
        translationFileConfig.name = "Test Labels XML file";
        translationFileConfig.layout = Layout.DIR;
        translationFileConfig.categories = Sets.newHashSet("iso19139", "schema", "labels.xml", "loc");
        getTransifexMojo.files.add(translationFileConfig);

        getTransifexMojo.project = "core-geonetwork";
        getTransifexMojo.password = "8GrHwnQztT3NSDjR";
        getTransifexMojo.username = "jeichar";
        getTransifexMojo.sourceLangCode = "en";
        getTransifexMojo.url = "https://www.transifex.com/api/2/project/";

        Logger.getRootLogger().addAppender(new ConsoleAppender());
        Logger.getRootLogger().setLevel(Level.ERROR);

        getTransifexMojo.execute();
    }
}