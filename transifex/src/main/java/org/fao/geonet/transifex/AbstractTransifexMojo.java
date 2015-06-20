package org.fao.geonet.transifex;

import net.sf.json.JSONObject;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

/**
 * Common properties and method.
 *
 * @author Jesse on 6/17/2015.
 */
public abstract class AbstractTransifexMojo extends AbstractMojo {
    /**
     * The format of the translation file
     */
    @Parameter(property = "files", required = true)
    protected List<TranslationFileConfig> files;
    /**
     * The transifex project url (minus the project)
     */
    @Parameter(property = "url", defaultValue = "https://www.transifex.com/api/2/project/")
    protected String url;
    /**
     * The Transifex project name.
     */
    @Parameter(property = "project", defaultValue = "core-geonetwork")
    protected String project;
    @Parameter(property = "source_language_code", defaultValue = "en")
    protected String sourceLangCode;
    /**
     * the username to use for accessing the transifex API.  Should not be in API it should be put on the commandline
     * -Dtransifex-username=my-username
     */
    @Parameter(property = "transifex-username")
    protected String username;
    /**
     * the password to use for accessing the transifex API.  Should not be in API it should be put on the commandline
     * -Dtransifex-password=my-password
     */
    @Parameter(property = "transifex-password")
    protected String password;

    public String getResourceUrl(String resourceId) {
        return cleanUrl(url + "/" + project + "/resource/" + resourceId);
    }

    public CloseableHttpResponse get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(cleanUrl(url));
        return exec(httpGet);
    }

    public CloseableHttpResponse head(String url) throws IOException {
        HttpHead head = new HttpHead(cleanUrl(url));
        return exec(head);
    }

    public CloseableHttpResponse post(String url, JSONObject data) throws IOException {
        HttpPost post = new HttpPost(cleanUrl(url));
        post.setEntity(new StringEntity(data.toString(), ContentType.create("application/json")));
        return exec(post);
    }
    public CloseableHttpResponse put(String url, JSONObject data) throws IOException {
        HttpPut put = new HttpPut(cleanUrl(url));
        put.setEntity(new StringEntity(data.toString(), ContentType.create("application/json")));
        return exec(put);
    }

    private String cleanUrl(String url) {
        return url.replaceAll("/+", "/").replace(":/","://");
    }

    private CloseableHttpResponse exec(HttpRequestBase request) throws IOException {
        request.addHeader("Accept", "application/json, text/*, text/html, text/html;level=1, */*");
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();
        getLog().info("Executing: " + request);
        return client.execute(request);
    }

    public void init() throws IOException, MojoExecutionException {
        Assert.notNull(files, "\nfiles property must be defined\n");
        Assert.notEmpty(files, "\nfiles property must have at least on definition defined\n");

        for (TranslationFileConfig file : files) {
            try {
                file.init();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        if (username == null || password == null) {
            throw new MojoExecutionException("Transifex operation skipped because username or password is not defined");
        }
        try (CloseableHttpResponse response = get(url + "/" + project)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new MojoExecutionException("Unable to access transifex server run build in offline mode:" +
                                                 response.getStatusLine().getReasonPhrase());
            }
        }
    }

    protected boolean resourceExists(TransifexReadyFile file) throws IOException {
        String resourceUrl = getResourceUrl(file.resourceId);
        try (CloseableHttpResponse response = get(resourceUrl)) {
            return response.getStatusLine().getStatusCode() == 200;
        }
    }


}
