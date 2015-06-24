package org.fao.geonet.transifex;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
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
import java.util.List;

/**
 * Uploads the english translations to UpdateTransifex
 */
@Mojo(name = "update-transifex",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresOnline = true)
public class UpdateTransifexMojo extends AbstractTransifexMojo {

    public void execute() throws MojoExecutionException {
        try {
            init();

            for (TranslationFileConfig file : files) {
                List<TransifexReadyFile> transifexFiles = file.getTransifexFiles();
                for (TransifexReadyFile transifexFile : transifexFiles) {
                    if (resourceExists(transifexFile)) {
                        updateResource(transifexFile);
                    } else {
                        createResource(transifexFile);
                    }
                }
            }
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void updateResource(TransifexReadyFile transifexFile) throws IOException, MojoExecutionException {
        JSONObject data = new JSONObject();
        data.put("content", "\"" + transifexFile.data + "\"");
        try (CloseableHttpResponse response = put(getResourceUrl(transifexFile.resourceId) + "/content", data)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                String reasonPhrase = response.getStatusLine().getReasonPhrase();
                String error = new String(ByteStreams.toByteArray(response.getEntity().getContent()), Constants.CHARSET);
                getLog().error("Error updating resource: " + error);
                throw new MojoExecutionException("Error updating transifex resource: (" + statusCode + ") " + reasonPhrase);
            }
        }
    }

    private void createResource(TransifexReadyFile transifexFile) throws IOException, MojoExecutionException {
        JSONObject data = new JSONObject();
        data.put("slug", transifexFile.resourceId);
        data.put("name", transifexFile.transifexName);
        data.put("i18n_type", "KEYVALUEJSON");
        data.put("priority", 0);
        data.put("categories", Joiner.on(",").join(transifexFile.categories));
        data.put("content", "\"" + transifexFile.data + "\"");

        try (CloseableHttpResponse response = post(url + "/" + project + "/resources/", data)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 201) {
                String reasonPhrase = response.getStatusLine().getReasonPhrase();
                String error = new String(ByteStreams.toByteArray(response.getEntity().getContent()), Constants.CHARSET);
                getLog().error("Error updating resource: " + error);
                getLog().error("Post Data: \n" + data.toString(2));
                throw new MojoExecutionException("Error updating transifex resource: (" + statusCode + ") " + reasonPhrase);
            }
        }
    }

    public static void main(String[] args) throws MojoExecutionException {
        UpdateTransifexMojo updateTransifexMojo = new UpdateTransifexMojo();
        updateTransifexMojo.files = Lists.newArrayList();

        TranslationFileConfig translationFileConfig = new TranslationFileConfig();
        translationFileConfig.path = "C:\\GitHub\\geonetwork\\geocat_trunk\\schemas\\csw-record\\src\\main\\plugin\\csw-record\\loc";
        translationFileConfig.fileName = "strings.xml";
        translationFileConfig.formatClass = "SimpleElementFormat";
        translationFileConfig.id = "test-codelistsxml";
        translationFileConfig.name = "Test codelists XML file";
        translationFileConfig.layout = Layout.PREFIX;
        translationFileConfig.categories = Sets.newHashSet("iso19139", "schema", "codelists.xml", "loc");
        updateTransifexMojo.files.add(translationFileConfig);

        updateTransifexMojo.project = "test-project-64";
        updateTransifexMojo.password = "8GrHwnQztT3NSDjR";
        updateTransifexMojo.username = "jeichar";
        updateTransifexMojo.url = "https://www.transifex.com/api/2/project/";

        Logger.getRootLogger().addAppender(new ConsoleAppender());
        Logger.getRootLogger().setLevel(Level.ERROR);

        updateTransifexMojo.execute();
    }
}