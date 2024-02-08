package org.fao.geonet.data;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringSubstitutor;
import org.fao.geonet.data.model.gdal.GdalDataset;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MetadataExtractor {
    public static void main(String[] args) throws Exception {
        System.out.println("ogrinfo");

        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        // OGR -json parameter is only available in GDAL 3.7+
//        String defaultOgrCommand = "docker run --rm -v ${dataFolder}:/data ghcr.io/osgeo/gdal:ubuntu-small-latest ogrinfo -json -so -al /data/${dataFile}";
        String defaultOgrCommand = "ogrinfo -json -so -al ${dataFolder}/${dataFile}";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("dataFolder", args.length > 0 ? args[0] : "/data");
        parameters.put("dataFile", args.length > 1 ? args[1] : "Emerald_2022_BIOREGION.csv");
        String command = StringSubstitutor.replace(defaultOgrCommand, parameters);
        System.out.println(command);
        ProcessBuilder builder = new ProcessBuilder(
                isWindows ? "cmd.exe" : "/bin/sh",
                isWindows ? "/c" : "-c",
                command);
        builder.redirectErrorStream(true);
        Process p = builder.start();

        String ogrInfo = StreamUtils.copyToString(p.getInputStream(), StandardCharsets.UTF_8);
        try {
            ObjectMapper mapper = new ObjectMapper();
            GdalDataset dataset = mapper.readValue(ogrInfo, GdalDataset.class);
            System.out.println(dataset.getDriverLongName());
            dataset.getLayers().forEach(layer -> {
                System.out.println(layer.getName());
                System.out.println(layer.getFeatureCount());
            });
        } catch (JsonParseException jsonParseException) {
            System.err.println(ogrInfo);
            System.err.println(jsonParseException.getMessage());
        }
    }
}
