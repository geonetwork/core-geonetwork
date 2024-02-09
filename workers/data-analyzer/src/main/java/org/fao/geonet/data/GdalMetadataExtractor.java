package org.fao.geonet.data;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.fao.geonet.data.model.gdal.GdalDataset;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A GDAL based metadata extractor.
 * <p>
 * Requires GDAL 3.7+ to support -json output parameter.
 */
public class GdalMetadataExtractor {

    // "docker run --rm -v ${dataFolder}:/data ghcr.io/osgeo/gdal:ubuntu-small-latest ogrinfo"
    @Value("${data.ingester.gdal.ogrinfo}")
    private String ogrinfoApp = "ogrinfo";

    private final String defaultOgrinfoDescribe = " -json -so -al ${datasetConnection}";

    @Value("${data.ingester.gdal.ogrinfo.describe:}")
    private String ogrinfoDescribe;

    private final String defaultOgrinfoVersion = " --version";

    @Value("${data.ingester.gdal.ogrinfo.version:}")
    private String ogrinfoVersion;

    private static final String LOGGER_NAME = "geonetwork.data.analysis.gdal";
    private static final Logger LOGGER = LoggerFactory.getLogger(GdalMetadataExtractor.LOGGER_NAME);

    @Autowired
    private GeonetworkDataDirectory geonetworkDataDir;

    public String getVersion() throws IOException {
        try {
            return execute(ogrinfoApp + (StringUtils.isNotEmpty(ogrinfoVersion) ? ogrinfoVersion : defaultOgrinfoVersion));
        } catch (IOException e) {
            throw new IOException(String.format("GDAL metadata extractor not available. %s", e.getMessage()));
        }
    }

    public boolean isAvailable() throws IOException {
        return StringUtils.isNotEmpty(getVersion());
    }

    private boolean isValidDatasource(String datasetConnection) {
        return true; // Check valid file or connection
    }

    public GdalDataset analyze(String datasetConnection) throws IOException {
        if (!isValidDatasource(datasetConnection)) {
            throw new IOException(String.format("Invalid datasource name %s.", datasetConnection));
        }
        String defaultOgrCommand = ogrinfoApp
                + (StringUtils.isNotEmpty(ogrinfoDescribe) ? ogrinfoDescribe : defaultOgrinfoDescribe);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("datasetConnection", datasetConnection);

        try {
            String ogrInfo = execute(defaultOgrCommand, parameters);
            ObjectMapper mapper = new ObjectMapper();
            GdalDataset dataset = mapper.readValue(ogrInfo, GdalDataset.class);
            LOGGER.debug(dataset.getDriverLongName());
            return dataset;
        } catch (JsonParseException jsonParseException) {
            throw new IOException(String.format("Failed to analyze %s. Error is %s", datasetConnection, jsonParseException.getMessage()));
        } catch (IOException e) {
            throw new IOException(String.format("Failed to analyze %s. Error is %s", datasetConnection, e.getMessage()));
        }
    }

    private String execute(String command) throws IOException {
        return execute(command, new HashMap<>());
    }

    private String execute(String command, Map<String, String> parameters) throws IOException {
        parameters.put("metadataDir", geonetworkDataDir.getMetadataDataDir().toString());
        command = StringSubstitutor.replace(command, parameters);
        LOGGER.debug(command);

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder(isWindows ? "cmd.exe" : "/bin/sh", isWindows ? "/c" : "-c", command);
        builder.redirectErrorStream(true);

        Process p = builder.start();
        return StreamUtils.copyToString(p.getInputStream(), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        new GdalMetadataExtractor().analyze(args[0]);
    }


    public String getOgrinfoApp() {
        return ogrinfoApp;
    }

    public void setOgrinfoApp(String ogrinfoApp) {
        this.ogrinfoApp = ogrinfoApp;
    }

    public String getOgrinfoDescribe() {
        return ogrinfoDescribe;
    }

    public void setOgrinfoDescribe(String ogrinfoDescribe) {
        this.ogrinfoDescribe = ogrinfoDescribe;
    }

    public String getOgrinfoVersion() {
        return ogrinfoVersion;
    }

    public void setOgrinfoVersion(String ogrinfoVersion) {
        this.ogrinfoVersion = ogrinfoVersion;
    }
}
